package com.rohm.mtg.utils.cardmanager.gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import com.rohm.mtg.utils.cardmanager.sorting.CompareOperator;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueFactory;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueStrategy;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class FilterBlockController extends HBox implements Initializable {

	private ObjectProperty<CompareOperator> operator = new SimpleObjectProperty<>();
	private ObjectProperty<CardValueStrategy<?>> cardValueStrategy = new SimpleObjectProperty<>();
	private ObjectProperty<Object> userValueObj = new SimpleObjectProperty<>();
	private StringProperty userValue = new SimpleStringProperty();

	private ObjectProperty<Predicate<CollectionCard>> predicate = new SimpleObjectProperty<>();

	private static ObservableList<CardValueStrategy<? extends Object>> comparators;

	private PrimaryController primaryController;

	public FilterBlockController(PrimaryController primaryController) {
		this.primaryController = primaryController;
	}

	private static synchronized void loadComparators() {
		if (comparators != null) {
			return;
		}
		comparators = FXCollections.observableArrayList(CardValueFactory.allCardValues);
		comparators.sort((o1, o2) -> o1.toString().compareTo(o2.toString()));
	}

	@FXML
	private ComboBox<CardValueStrategy<? extends Object>> cbComparators;
	@FXML
	private ComboBox<CompareOperator> cbOperators;
	@FXML
	private	TextField userInput;
	@FXML
	private CheckBox notCheck;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		loadComparators();

		cbComparators.setItems(comparators);
		cardValueStrategy.bind(cbComparators.getSelectionModel().selectedItemProperty());

		FilteredList<CompareOperator> filteredOperators = new FilteredList<>(FXCollections.observableArrayList(CompareOperator.values()));
		cbOperators.setItems(filteredOperators);
		operator.bind(cbOperators.getSelectionModel().selectedItemProperty());

		cardValueStrategy.addListener((obs, oldV, newV) -> {
			CompareOperator selectedItem = cbOperators.getSelectionModel().getSelectedItem();
			if(selectedItem != null) {
				boolean applicable = selectedItem.isApplicable(newV.getClass());
				if(!applicable) {
					cbOperators.getSelectionModel().select(null);
				}
			}
			filteredOperators.setPredicate(co -> co.isApplicable(newV.getClass()));
		});

		userValue.bind(userInput.textProperty());
		userValueObj
				.bind(Bindings.createObjectBinding(() -> {
					CardValueStrategy<? extends Object> cvs = cardValueStrategy.get();
					Object result = cvs == null ? null : cvs.parse(userValue.get());
					return result;
				}, userValue));

		predicate.bind(Bindings.createObjectBinding(this::createNewPredicate, operator, cardValueStrategy, userValueObj, notCheck.selectedProperty()));
	}

	public ObjectProperty<Predicate<CollectionCard>> getPredicate() {
		return predicate;
	}

	@FXML
	private void removeThis() {
		primaryController.removeFilterBlock(this);
	}

	private Predicate<CollectionCard> createNewPredicate() {
		return new Predicate<CollectionCard>() {
			@Override
			public boolean test(CollectionCard dragonShieldCard) {
				if(dragonShieldCard.getCardName().toLowerCase().contains("token") || dragonShieldCard.getCardName().contains("Morph") || dragonShieldCard.getCardName().contains("Checklist")) {
					return false;
				}
				if (userValueObj.get() == null || (userValueObj.get() instanceof List) && ((List<?>)userValueObj.get()).isEmpty()) {
					return true;
				}
				try {
					boolean compResult = cardValueStrategy.getValue().compareWithOperator(dragonShieldCard, userValue.get(), operator.get());
					if (notCheck.isSelected()) {
						return !compResult;
					}
					return compResult;
				} catch (Exception e) {
					System.err.println("Error for " + dragonShieldCard + " in test for " + this);
					e.printStackTrace();
					return true;
				}
			}
		};
	}

	@Override
	public String toString() {
		return this.getClass()+" "+userValue+" "+operator+" "+cardValueStrategy;
	}

	public void setOperator(CompareOperator operator) {
		this.cbOperators.getSelectionModel().select(operator);
	}

	public void setCardValueStrategy(CardValueStrategy<?> cardValueStrategy) {
		this.cbComparators.getSelectionModel().select(cardValueStrategy);
	}

	public void setUserInput(String userInput) {
		this.userInput.setText(userInput);
	}

}
