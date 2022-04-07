package com.rohm.mtg.utils.cardmanager;

import java.io.IOException;
import java.util.Properties;

import com.rohm.mtg.utils.cardmanager.gui.PrimaryController;

import de.rohmio.mtg.mtgtop8.api.MtgTop8Api;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * JavaFX App
 */
public class App extends Application {

	private static Scene scene;
	private static Stage primaryStage;

	@Override
	public void start(Stage stage) throws IOException {
		primaryStage = stage;
		scene = new Scene(createFxmlLoader(PrimaryController.class).load());
		stage.setScene(scene);
		stage.setTitle("Card Manager - " + getSoftwareVersion());
		stage.show();

		primaryStage.setOnCloseRequest(e -> {
			Platform.exit();
			System.exit(0);
		});
	}

	public static void setRoot(Class<? extends Initializable> controllerClass) throws IOException {
		scene.setRoot(createFxmlLoader(controllerClass).load());
	}

	public static FXMLLoader createFxmlLoader(Class<? extends Initializable> controllerClass) throws IOException {
		String fxml = controllerClass.getSimpleName().replace("Controller", "View");
		FXMLLoader fxmlLoader = new FXMLLoader(controllerClass.getResource(fxml + ".fxml"));
		return fxmlLoader;
	}

	public static Stage primaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		MtgTop8Api.RATE_LIMIT = 10;
		launch(args);
	}

	public static void openBrowser(String url) {
		try {
			Runtime.getRuntime().exec(new String[] { "rundll32", "url.dll,FileProtocolHandler", url });
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String getSoftwareVersion() {
		final Properties properties = new Properties();
		String version = null;
		try {
			properties.load(App.class.getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e) {
			e.printStackTrace();
			version = "could not read version";
		}
		version = properties.getProperty("version");
		return version;
	}

}
