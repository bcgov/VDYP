package application;

import java.io.IOException;

import application.controller.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class Main extends Application {
	private static Stage primaryStage;

	/**
	 * Getter method to access the primaryStage from other files. Returns the Stage
	 * object representing the primary stage.
	 *
	 * @return The Stage object representing the new window.
	 * @throws IllegalStateException if newWindow is not set yet. Make sure to
	 *                               initialize it before accessing.
	 */
	public static Stage getPrimaryStage() {
		if (primaryStage == null) {
			throw new IllegalStateException(
					"primaryStage is not set yet. Make sure to initialize it before accessing."
			);
		}
		return primaryStage;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			Main.primaryStage = primaryStage;

			// Load the FXML file and create the scene and controller
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/MainWindow.fxml"));
			Parent root = loader.load();
			MainController controller = loader.getController();
			Scene scene = new Scene(root, 1000, 700);
			scene.getStylesheets().add(getClass().getResource("resources/application.css").toExternalForm());

			// Set up event handler to trigger on shortcut button presses
			setupEventHandlers(scene, controller);

			// Add icon image and title to the primary stage
			Image icon = new Image(getClass().getResource("resources/icon.png").toExternalForm());
			setStageIconAndTitle(primaryStage, "WinVDYP7.9 POC", icon);

			// Show the scene
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets up the event handlers for the specified scene and controller. The event
	 * handler listens for "Ctrl + T" key press to trigger the opening of a
	 * secondary window.
	 *
	 *
	 * @param scene      The scene to which the event handler will be attached.
	 * @param controller The MainController instance responsible for handling
	 *                   events.
	 */
	private static void setupEventHandlers(Scene scene, MainController controller) {
		scene.setOnKeyPressed(event -> {
			if (event.isControlDown() && event.getCode() == KeyCode.T) {
				try {
					controller.openSecondaryWindow();
					event.consume(); // Consume the event to prevent it from reaching the event handler again
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Sets the icon image and title for the given stage.
	 *
	 * @param stage The stage to which the icon image and title will be set.
	 * @param title The tile of the window
	 */
	public static void setStageIconAndTitle(Stage stage, String title, Image icon) {
		stage.getIcons().add(icon);
		stage.setTitle(title);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
