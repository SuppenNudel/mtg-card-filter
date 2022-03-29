package com.rohm.mtg.utils.cardmanager.gui;

import java.net.URL;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.rohm.mtg.utils.cardmanager.config.UserConfig;
import com.rohm.mtg.utils.cardmanager.config.UserConfigKey;

import de.rohmio.mtg.mtgtop8.api.model.CompLevel;
import de.rohmio.mtg.mtgtop8.api.model.MtgTop8Format;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MtgTop8SettingsController implements Initializable {

	@FXML
	private Spinner<Integer> spinner_startAmount;
	@FXML
	private ComboBox<ChronoUnit> dropdown_timeUnit;

	@FXML
	private VBox v_compLevels;
	private Map<CompLevel, CheckBox> compLevelCheckboxes = new HashMap<>();

	@FXML
	private VBox v_formats;
	private Map<MtgTop8Format, CheckBox> formatCheckboxes = new HashMap<>();

	private ObservableList<MtgTop8Format> config_formats = FXCollections.observableArrayList();
	private ObservableList<CompLevel> config_compLevels = FXCollections.observableArrayList();

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		spinner_startAmount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 6, 1));
		dropdown_timeUnit.getItems().setAll(ChronoUnit.DAYS, ChronoUnit.MONTHS, ChronoUnit.YEARS);

		dropdown_timeUnit.setValue(ChronoUnit.MONTHS);

		config_compLevels.setAll(UserConfig.getConfig().getList(UserConfigKey.COMP_LEVELS, CompLevel.class));
		for(CompLevel compLevel : CompLevel.values()) {
			CheckBox checkBox = new CheckBox(compLevel.name());
			checkBox.setSelected(config_compLevels.contains(compLevel));
			checkBox.selectedProperty().addListener((obs, oldV, newV) -> {
				if(newV) {
					config_compLevels.add(compLevel);
				} else {
					config_compLevels.remove(compLevel);
				}
			});
			v_compLevels.getChildren().add(checkBox);
			compLevelCheckboxes.put(compLevel, checkBox);
		}
		config_compLevels.addListener((ListChangeListener<CompLevel>) c -> UserConfig.getConfig().setList(UserConfigKey.COMP_LEVELS, c.getList()));

		config_formats.setAll(UserConfig.getConfig().getList(UserConfigKey.FORMATS, MtgTop8Format.class));
		for(MtgTop8Format format : MtgTop8Format.values()) {
			CheckBox checkBox = new CheckBox(format.name());
			checkBox.setSelected(config_formats.contains(format));
			checkBox.selectedProperty().addListener((obs, oldV, newV) -> {
				if(newV) {
					config_formats.add(format);
				} else {
					config_formats.remove(format);
				}
			});
			v_formats.getChildren().add(checkBox);
			formatCheckboxes.put(format, checkBox);
		}
		config_formats.addListener((ListChangeListener<MtgTop8Format>) c -> UserConfig.getConfig().setList(UserConfigKey.FORMATS, c.getList()));

		spinner_startAmount.getValueFactory().setValue(UserConfig.getConfig().get(UserConfigKey.TIME_PERIOD));
		spinner_startAmount.valueProperty().addListener((obs, oldV, newV) -> UserConfig.getConfig().set(UserConfigKey.TIME_PERIOD, newV));

		dropdown_timeUnit.setValue(UserConfig.getConfig().get(UserConfigKey.TIME_UNIT));
		dropdown_timeUnit.valueProperty().addListener((obs, oldV, newV) -> UserConfig.getConfig().set(UserConfigKey.TIME_UNIT, newV));
	}

	@FXML
	private void save(ActionEvent event) {
	    ((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
	}

}