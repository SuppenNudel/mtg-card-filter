package com.rohm.mtg.utils.cardmanager.gui;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
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

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rohm.mtg.utils.cardmanager.App;
import com.rohm.mtg.utils.cardmanager.config.FilterBlockConfig;
import com.rohm.mtg.utils.cardmanager.config.UserConfig;
import com.rohm.mtg.utils.cardmanager.config.UserConfigKey;
import com.rohm.mtg.utils.cardmanager.model.MtgTop8Scores;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueFactory;
import com.rohm.mtg.utils.cardmanager.sorting.cardvalue.CardValueStrategy;
import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory;
import com.rohm.mtg.utils.dragonshield.DragonShieldReaderFactory.CollectionReader;
import com.rohm.mtg.utils.dragonshield.collection.CollectionCard;

import de.rohmio.mtg.mtgtop8.api.endpoints.SearchEndpoint;
import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;
import de.rohmio.mtg.scryfall.api.model.CardObject;
import de.rohmio.mtg.scryfall.api.model.enums.Format;
import de.rohmio.mtg.scryfall.api.model.enums.Legality;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class PrimaryController implements Initializable {

	@FXML
	private Label lbl_loadedCards;
	@FXML
	private Label lbl_shownCards;
	@FXML
	private ToggleButton scanToggle;
	@FXML
	private VBox v_filters;

	@FXML
	private TableView<CollectionCard> cardTable;
	private Map<MtgTop8Format, TableColumn<CollectionCard, Integer>> formatColumns = new HashMap<>();

	private ObservableList<CollectionCard> masterData = FXCollections.observableArrayList();
	private FilteredList<CollectionCard> filteredData;

	private static final ExtensionFilter filterFileExtensionFilter = new ExtensionFilter("Filter-File", "*.json");

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		v_filters.getChildrenUnmodifiable().addListener((ListChangeListener<Node>) c -> updatePredicate());

		// 0. Initialize the columns.
		List<CardValueStrategy<? extends Object>> tableValues = Arrays.asList(CardValueFactory.quantity,
				CardValueFactory.name, CardValueFactory.setCode, CardValueFactory.cardNumber, CardValueFactory.printing,
				CardValueFactory.language, CardValueFactory.folder, CardValueFactory.priceBought, CardValueFactory.priceAvg, CardValueFactory.priceLow, CardValueFactory.priceTrend);
		tableValues.forEach(cvs -> {
			TableColumn<CollectionCard, String> column = new TableColumn<>(cvs.toString());
			column.setCellValueFactory(param -> new SimpleObjectProperty(cvs.convert(param.getValue())));
			cardTable.getColumns().add(column);
		});

		// TODO instead of double click use context menu
		// on row double click -> open card on scryfall
		cardTable.setRowFactory(tableView -> {
			TableRow<CollectionCard> row = new TableRow<>();
			row.setOnMouseClicked(event -> {
				if (event.getClickCount() == 2 && (!row.isEmpty())) {
					CollectionCard item = row.getItem();
//					CardObject cardObject = CardValueFactory.scryfallCards.get(item.getCardName());
					String url = String.format("https://scryfall.com/card/%s/%s/%s", item.getSetCode().toLowerCase(),
                            item.getCardNumber().replace("#", ""), item.getCardName().replace(" // ", " "));
//					String url = cardObject.getScryfall_uri();
					System.out.println("Opening: " + url);
					App.openBrowser(url);
				}
			});
			return row;
		});

		// 1. Wrap the ObservableList in a FilteredList (initially display all data).
		filteredData = new FilteredList<>(masterData, p -> true);

		lbl_shownCards.textProperty().bind(Bindings.createStringBinding(() -> {

			Integer totalCards = filteredData.stream().map(CollectionCard::getQuantity)
					.collect(Collectors.summingInt(Integer::intValue));
			return filteredData.size()+ " rows(s), "+totalCards+" card(s) shown";
		}, filteredData));

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

		addIfBlock();
	}

	@FXML
	private FilterBlockController addIfBlock() {
		FilterBlockController filterBlock = new FilterBlockController(this);
		try {
			FXMLLoader fxmlLoader = App.createFxmlLoader(FilterBlockController.class);
			fxmlLoader.setRoot(filterBlock);
			fxmlLoader.setController(filterBlock);
			fxmlLoader.load();
			v_filters.getChildren().add(filterBlock);
			return filterBlock;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void removeFilterBlock(FilterBlockController filterBlock) {
		v_filters.getChildren().remove(filterBlock);
	}

	public void updatePredicate() {
		List<FilterBlockController> filterBlocks = getAllFilterBlocks();

		Observable[] observables = filterBlocks.stream()
				.map(FilterBlockController::getPredicate).toArray(Observable[]::new);


		filteredData.predicateProperty().bind(Bindings.createObjectBinding(() -> {
			Predicate<CollectionCard> predicate = t -> true;
			for (FilterBlockController filterBlock : filterBlocks) {
				predicate = predicate.and(filterBlock.getPredicate().get());
			}
			return predicate;
		}, observables));
	}

	private List<FilterBlockController> getAllFilterBlocks() {
		List<FilterBlockController> filterBlocks = v_filters.getChildrenUnmodifiable().stream()
				.map(node -> (FilterBlockController) node).collect(Collectors.toList());
		return filterBlocks;
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
		UserConfig config = UserConfig.getConfig();
		File init_dir = config.get(UserConfigKey.INIT_DIR);
		fileChooser.setInitialDirectory(init_dir);
		fileChooser.getExtensionFilters().add(new ExtensionFilter("CSV-File", "*.csv"));
		File file = fileChooser.showOpenDialog(App.primaryStage());
		if (file != null) {
			CollectionReader collectionReader = DragonShieldReaderFactory.collection(file);
			List<CollectionCard> cards = collectionReader.getCards(true);
			masterData.setAll(cards);

			Integer totalCards = cards.stream().map(CollectionCard::getQuantity)
					.collect(Collectors.summingInt(Integer::intValue));
			lbl_loadedCards.setText(totalCards + " card(s) loaded");
			config.set(UserConfigKey.INIT_DIR, file.getParentFile());
		}
	}

	@FXML
	private void saveCurrentFilter() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(UserConfig.getConfig().get(UserConfigKey.INIT_DIR));
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Filter", "*.json"));
		File file = fileChooser.showSaveDialog(null);
		if(file == null) {
			return;
		}
		List<FilterBlockConfig> collect = getAllFilterBlocks().stream().map(block -> new FilterBlockConfig(block.getUserInput(), block.getCardValueStrategy().getKey(), block.getOperator(), block.getNot())).collect(Collectors.toList());
		String json = new Gson().toJson(collect);
		try {
			FileUtils.writeStringToFile(file, json, "UTF-8", false);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void loadFilter() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(UserConfig.getConfig().get(UserConfigKey.INIT_DIR));
		fileChooser.getExtensionFilters().add(filterFileExtensionFilter);
		File file = fileChooser.showOpenDialog(null);
		if(file == null) {
			return;
		}
		Type type = new TypeToken<List<FilterBlockConfig>>(){}.getType();
		String json = null;
		try {
			json = FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(json == null) {
			return;
		}
		List<FilterBlockConfig> filterBlockConfigs = new Gson().fromJson(json, type);
		v_filters.getChildren().clear();
		for(FilterBlockConfig filterBlockConfig : filterBlockConfigs) {
			FilterBlockController ifBlock = addIfBlock();
			ifBlock.applyConfig(filterBlockConfig);
		}
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
				CollectionCard collectionCard = param.getValue();
				CardObject scryfallCard = CardValueFactory.getScryfallCard(collectionCard.getCardName());
				Format scryfallFormat = Format.valueOf(format.toString());
				Legality legality = scryfallCard.getLegalities().get(scryfallFormat);

				IntegerProperty score = new SimpleIntegerProperty(-1);

				if (legality == Legality.BANNED) {
					score.set(-4);
				} else if (legality == Legality.NOT_LEGAL) {
					score.set(-5);
				} else if (scanToggle.isSelected()) {
					score = MtgTop8Scores.getScore(collectionCard.getCardName(), compLevels, format, startDate);
				}
				return score.asObject();
			});
			columnScore.setCellFactory(param -> new TableCell<CollectionCard, Integer>() {
				@Override
				protected void updateItem(Integer item, boolean empty) {
					super.updateItem(item, empty);

					if (item == null || empty) {
						setText(null);
						setStyle("");
					} else {
						String text;
						switch (item) {
						case -1:
							text = "Loading...";
							break;
						case SearchEndpoint.NO_MATCH:
							text = "No match";
							break;
						case SearchEndpoint.TOO_MANY_CARDS:
							text = "Too many cards";
							break;
						case -4:
							text = Legality.BANNED.name();
							break;
						case -5:
							text = Legality.NOT_LEGAL.name();
							break;
						default:
							text = String.valueOf(item);
						}
						setText(text);
						// Format date.
//			            setText(myDateFormatter.format(item));

						/*
						 * // Style all dates in March with a different color. if (item.getMonth() ==
						 * Month.MARCH) { setTextFill(Color.CHOCOLATE);
						 * setStyle("-fx-background-color: yellow"); } else { setTextFill(Color.BLACK);
						 * setStyle(""); }
						 */
					}
				}
			});
			Platform.runLater(() -> cardTable.getColumns().add(columnScore));
			formatColumns.put(format, columnScore);
		}
	}

}
