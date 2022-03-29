package com.rohm.mtg.utils.cardmanager.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.rohm.mtg.utils.cardmanager.App;
import com.rohm.mtg.utils.cardmanager.config.UserConfig;
import com.rohm.mtg.utils.cardmanager.config.UserConfigKey;
import com.rohm.mtg.utils.cardmanager.model.MtgTop8Scores;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueFactory;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueStrategy;
import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory;
import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory.CollectionReader;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class PrimaryController implements Initializable {

	private CollectionReader collectionReader;

	@FXML
	private Label lbl_loadedCards;
	@FXML
	private ToggleButton scanToggle;
	@FXML
	private VBox v_filters;

	@FXML
	private TableView<CollectionCard> cardTable;
	private Map<MtgTop8Format, TableColumn<CollectionCard, Integer>> formatColumns = new HashMap<>();

	private ObservableList<CollectionCard> masterData = FXCollections.observableArrayList();
	private ObservableList<FilterBlockController> filterBlocks = FXCollections.observableArrayList();
	private FilteredList<CollectionCard> filteredData;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// 0. Initialize the columns.
		List<CardValueStrategy<? extends Object>> tableValues = Arrays.asList(
				CardValueFactory.quantity,
				CardValueFactory.name,
				CardValueFactory.setCode,
				CardValueFactory.cardNumber,
				CardValueFactory.printing,
				CardValueFactory.language,
				CardValueFactory.folder);
		tableValues.forEach(cvs -> {
			TableColumn<CollectionCard, String> column = new TableColumn<>(cvs.toString());
			column.setCellValueFactory(param -> new SimpleObjectProperty(cvs.convert(param.getValue())));
			cardTable.getColumns().add(column);
		});

		// on row double click -> open card on scryfall
		cardTable.setRowFactory(tv -> {
			TableRow<CollectionCard> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					CollectionCard card = row.getItem();
					String setCode = CardValueFactory.setCode.convert(card);
					String url = String.format("https://scryfall.com/card/%s/%s/%s", setCode.toLowerCase(),
							card.getCardNumber().replace("#", ""), card.getCardName().replace(" // ", " "));
					System.out.println("Opening: " + url);
					App.openBrowser(url);
				}
			});
			return row;
		});

		// 1. Wrap the ObservableList in a FilteredList (initially display all data).
		filteredData = new FilteredList<>(masterData, p -> true);

		// 2. Set the filter Predicate whenever the filter changes.
		// done in FilterBlockController.updatePredicate

		// 3. Wrap the FilteredList in a SortedList.
		SortedList<CollectionCard> sortedData = new SortedList<>(filteredData);

		// 4. Bind the SortedList comparator to the TableView comparator.
		sortedData.comparatorProperty().bind(cardTable.comparatorProperty());

		// 5. Add sorted (and filtered) data to the table.
		cardTable.setItems(sortedData);

		scanToggle.selectedProperty().addListener((obs, oldV, newV) -> {
			try {
				scanForStaples();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	@FXML
	private void addIfBlock() throws IOException {
		FilterBlockController ifBlock = new FilterBlockController(v_filters);
		FXMLLoader fxmlLoader = App.createFxmlLoader(FilterBlockController.class);
		fxmlLoader.setRoot(ifBlock);
		fxmlLoader.setController(ifBlock);
		fxmlLoader.load();
		filterBlocks.add(ifBlock);

		List<ObjectProperty<Predicate<CollectionCard>>> collect = filterBlocks.stream().map(FilterBlockController::getPredicate).collect(Collectors.toList());
		Observable[] array = collect.toArray(new Observable[collect.size()]);

		filteredData.predicateProperty().bind(Bindings.createObjectBinding(() -> {
		Predicate<CollectionCard> predicate = t -> true;
			for(FilterBlockController filterBlock : filterBlocks) {
				predicate = predicate.and(filterBlock.getPredicate().get());
			}
			return predicate;
		}, array));
	}

	@FXML
	private void removeLastIfBlock() {
		if (v_filters.getChildren().size() <= 0) {
			return;
		}
		v_filters.getChildren().remove(v_filters.getChildren().size() - 1);
	}

	@FXML
	private void openMtgTop8Settings() throws IOException {
		Parent loadFXML = App.createFxmlLoader(MtgTop8SettingsController.class).load();
		Scene scene = new Scene(loadFXML);
		Stage stage = new Stage();
		stage.setTitle("MtgTop8 Settings");
		stage.setScene(scene);
		stage.show();
	}

	@FXML
	private void loadCardManagerFile() throws IOException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choose Dragon Shield Card Manager File");
		fileChooser.setInitialDirectory(new File("src/test/resources"));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV-File", "*.csv"));
		File file = fileChooser.showOpenDialog(App.primaryStage());
		if (file != null) {
			masterData.clear();
			collectionReader = DragonShieldReaderFactory.collection(file);
			List<CollectionCard> cards = collectionReader.getCards(true);
			masterData.addAll(cards);

			Integer totalCards = cards.stream().map(CollectionCard::getQuantity)
					.collect(Collectors.summingInt(Integer::intValue));
			lbl_loadedCards.setText(totalCards + " card(s) loaded");
		}
	}

	@FXML
	private void saveCurrentFilter() {
		// TODO
	}

	private void scanForStaples() throws IOException {
		for (TableColumn<CollectionCard, Integer> column : formatColumns.values()) {
			Platform.runLater(() -> cardTable.getColumns().remove(column));
		}
		formatColumns.clear();

		// read user configuration
		Integer monthsAgo = UserConfig.getConfig().get(UserConfigKey.TIME_PERIOD);
		ChronoUnit timeUnit = UserConfig.getConfig().get(UserConfigKey.TIME_UNIT);
		LocalDate startDate = LocalDate.now().minus(monthsAgo, timeUnit);
		List<CompLevel> compLevels = UserConfig.getConfig().getList(UserConfigKey.COMP_LEVELS, CompLevel.class);
		List<MtgTop8Format> formats = UserConfig.getConfig().getList(UserConfigKey.FORMATS, MtgTop8Format.class);

		for (MtgTop8Format format : formats) {
			TableColumn<CollectionCard, Integer> columnScore = new TableColumn<>(format.name());
			columnScore.setCellValueFactory(param -> {
				if(scanToggle.isSelected()) {
					return MtgTop8Scores
							.getScore(param.getValue().getCardName(), compLevels, format, startDate).asObject();
				}
				return null;
			});
			Platform.runLater(() -> cardTable.getColumns().add(columnScore));
			formatColumns.put(format, columnScore);
		}
	}

}
