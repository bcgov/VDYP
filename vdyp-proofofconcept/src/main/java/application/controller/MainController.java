package application.controller;

import application.Main;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class MainController {
	private static Stage newWindow; // to pass into NewTableSceneController

	private static int newWindowCount = 0; // to track amount of new windows made

	/**
	 * Getter method to access the newWindow from other files. Returns the Stage
	 * object representing the new window.
	 *
	 * @return The Stage object representing the new window.
	 * @throws IllegalStateException if newWindow is not set yet. Make sure to
	 *                               initialize it before accessing.
	 */
	public static Stage getNewWindow() {
		if (newWindow == null) {
			throw new IllegalStateException("newWindow is not set yet. Make sure to initialize it before accessing.");
		}
		return newWindow;
	}

	/**
	 * Opens the secondary window based on the given event. The secondary window
	 * will be centered within the primary stage and adjusted to fit within the
	 * screen bounds.
	 *
	 * @param event      The Event that triggers the opening of the secondary
	 *                   window.
	 * @param fromButton A boolean indicating whether the event is from a button
	 *                   click (true) or scene event (false).
	 * @throws IOException if an I/O error occurs while opening the secondary
	 *                     window.
	 */
	public void openSecondaryWindow() throws IOException {
		newWindowCount++; // used for title

		// Set up secondary window
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/NewTableScene.fxml"));
		Parent secondaryLayout = loader.load();
		Scene secondScene = new Scene(secondaryLayout);
		secondScene.getStylesheets().add(getClass().getResource("../resources/application.css").toExternalForm());

		// Create new stage and icon
		newWindow = new Stage();
		Image icon = new Image(getClass().getResource("../resources/icon.png").toExternalForm());

		Main.setStageIconAndTitle(newWindow, "Model " + newWindowCount, icon);

		// Load in primary window
		Stage primaryStage = Main.getPrimaryStage();

		// Set position and relative size
		setSecondaryWindowPosition(newWindow, primaryStage);
		setSecondaryWindowSize(newWindow, primaryStage);

		// Set main window as owner and set modality
		newWindow.initOwner(primaryStage);

		newWindow.setScene(secondScene);
		newWindow.show();
	}

	/**
	 * Sets the position of the secondary window relative to the primary stage. The
	 * secondary window will be centered within the primary stage and adjusted to
	 * fit within the screen bounds.
	 *
	 * @param newWindow    The Stage representing the secondary window to be
	 *                     positioned.
	 * @param primaryStage The primary Stage that serves as the reference for
	 *                     positioning the secondary window.
	 */
	private void setSecondaryWindowPosition(Stage newWindow, Stage primaryStage) {
		double scaleFactor = 0.6; // Set the desired scale factor (e.g., 80%)

		double primaryWidth = primaryStage.getWidth();
		double primaryHeight = primaryStage.getHeight();

		double secondaryWidth = primaryWidth * scaleFactor;
		double secondaryHeight = primaryHeight * scaleFactor;

		double primaryX = primaryStage.getX();
		double primaryY = primaryStage.getY();
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds(); // get screen bounds

		double centerX = primaryX + (primaryWidth - secondaryWidth) / 2;
		double centerY = primaryY + (primaryHeight - secondaryHeight) / 2;

		double maxX = screenBounds.getMaxX();
		double maxY = screenBounds.getMaxY();
		double adjustedX = Math.min(Math.max(centerX, 0), maxX - secondaryWidth);
		double adjustedY = Math.min(Math.max(centerY, 0), maxY - secondaryHeight);

		// Set the position of the secondary window
		newWindow.setX(adjustedX);
		newWindow.setY(adjustedY);
	}

	/**
	 * Sets the size of the secondary window based on the scale factor relative to
	 * the primary stage size.
	 *
	 * @param newWindow    The Stage representing the secondary window to set the
	 *                     size.
	 * @param primaryStage The primary Stage used as a reference for sizing the
	 *                     secondary window.
	 */
	private void setSecondaryWindowSize(Stage newWindow, Stage primaryStage) {
		double scaleFactor = 0.6; // Set the desired scale factor (e.g., 80%)

		double primaryWidth = primaryStage.getWidth();
		double primaryHeight = primaryStage.getHeight();

		double secondaryWidth = primaryWidth * scaleFactor;
		double secondaryHeight = primaryHeight * scaleFactor;

		// Set the size of the secondary window
		newWindow.setWidth(secondaryWidth);
		newWindow.setHeight(secondaryHeight);
	}

}
