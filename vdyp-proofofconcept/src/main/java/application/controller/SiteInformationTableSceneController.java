package application.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;
import javafx.scene.text.Text;

public class SiteInformationTableSceneController implements Initializable {
	@FXML
	private ChoiceBox<String> ecoZone;
	@FXML
	private ChoiceBox<String> becZone;
	@FXML
	private ChoiceBox<String> ageType;
	@FXML
	private Spinner<Double> standAge;
	@FXML
	private Spinner<Double> standHeight;
	@FXML
	private Spinner<Double> bha50SiteIndex;
	@FXML
	private Button runButton;
	@FXML
	private Button runButtonReport;
	@FXML
	private ChoiceBox<String> loggingOn;
	@FXML
	private ChoiceBox<String> loggingType;
	@FXML
	private ChoiceBox<String> siteSpecies;
	@FXML
	private ChoiceBox<String> siteIndexCurve;

	private static String[] dbhLabels;

	/**
	 * Gets the DBH labels array.
	 *
	 * @return The array of DBH labels.
	 * @throws IllegalStateException if the DBH labels are not set yet. Initialize
	 *                               them before accessing.
	 */
	public static String[] getDBHLabels() {
		if (dbhLabels == null) {
			throw new IllegalStateException("newWindow is not set yet. Make sure to initialize it before accessing.");
		}
		return dbhLabels;
	}

	/**
	 * Sets the DBH labels array.
	 *
	 * @param labels The array of DBH labels to set.
	 * @throws IllegalArgumentException if the provided labels array is null.
	 */
	public static void setDBHLabels(String[] labels) {
		if (labels == null) {
			throw new IllegalArgumentException("Labels array cannot be null.");
		}
		dbhLabels = labels;
	}

	/**
	 * Initializes the SiteInformationTableScene with default values and
	 * configurations. This method is automatically called when the associated FXML
	 * is loaded.
	 *
	 * @param location  The URL location used to resolve relative paths.
	 * @param resources The ResourceBundle containing localizable resources.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setDefaults(); // this is a function so it can also be called from menu button

		final String[] siteIndexCurves = { "AC - Huang, Titus, and Lakusta (1994ac)",
				"AT - Nigh, Krestov, and Klinka 2002", "B  - Chen and Klinka (200ac)", "BA - Nigh (2009)",
				"BG - Nigh (2009)", "BL - Chen and Klinka (2000ac)", "CW - Nigh (2000)", "DR - Nigh and Courtin (1998)",
				"E  - Nigh (2009)", "EA - Thrower (1994)", "EP - Nigh (2009)", "FD - Thrower and Goudie (1992ac)",
				"H  - Nigh (1998)", "HM - Means, Campbell, Johnson (1988ac)", "HW - Nigh (1998)",
				"L  - Briscon, Klinka, and Nigh 2002", "LA - Thrower (1994)", "LT - Thrower (1994)",
				"LW - Briscon, Klinka, and Nigh 2002", "MB - Nigh and Courtin (1998)", "PA - Thrower (1994)",
				"PF - Thrower (1994)", "PJ - Thrower (1994)", "PL - Thrower (1994)", "PW - Thrower (1994)",
				"PY - Thrower (1994)", "S  - Goudie (1984ac)(natural)", "SB - Nigh, Krestov, and Klinka 2002",
				"SE - Nigh 2015", "SS - Nigh 1997", "SW - Goudie (1984ac)(natural)", "YC - Nigh (2000)" };

		siteSpecies.setOnAction((ActionEvent event) -> {
			String selectedSpecies = siteSpecies.getValue();

			for (String curve : siteIndexCurves) {
				if (curve.startsWith(selectedSpecies)) {
					String curveLabel = curve.substring(selectedSpecies.length() + 3); // Removing the identifier and "
																						// - " part
					siteIndexCurve.setValue(curveLabel);

					double preferredWidth = computeTextWidth(curveLabel) + 30; // Calculate choicebox width and add a
																				// bit of padding
					siteIndexCurve.setPrefWidth(preferredWidth);

					break;
				}
			}
		});

	}

	/**
	 * Sets default values for various controls in the window and disables certain controls as specified in the Balsamiq mock-ups.
	 * The default values and control states are as follows:
	 * @formatter:off
	 * 		- becZone: Default value set to "IDF - Interior Douglas Fir" with options from predefined BEC Zones.
	 * 		- ecoZone: Default value set to "Select Species" with options from predefined Eco Zones.
	 * 		- ageType: Default value set to "Total" with options "Total" and "Breast".
	 * 		- standAge: Default value set to 60.0, with a range from 0.00 to 500.00 and an increment of 10.0.
	 * 		- standHeight: Default value set to 17.00, with a range from 0.00 to 99.9 and an increment of 1.0.
	 * 		- bha50SiteIndex: Default value set to 16.30, with a range from 0.00 to 60.0 and an increment of 1.0.
	 * 						  The control is disabled by default.
	 * 		- runButton: The runButton control is disabled by default.
	 * 		- runButtonReport: Disabled by default- loggingOn: Sets the choices Yes and No. Yes by default
	 * 		- loggingType: Set the choices as Basic,Intermediate and Advanced. Basic by Default
	 * 		- loggingOn: Sets the choices Yes and No. Yes by default
	 * 		- siteIndexCurve: Disabled since it updates based on siteSpecies choice
	 * @formatter:on
	 */
	public void setDefaults() {
		// Arrays containing the options for different Bec & Eco Zones
		final String[] becZones = { "AT - Alpine Tundra", "BG - Bunch Grass", "BWBS - Boreal White and Black Spruce",
				"CDF - Coastal Douglas Fir", "CWH - Coastal Western Hemlock", "ESSF - Engelmann Spruce",
				"ICH - Interior Cedar Hemlock", "IDF - Interior Douglas Fir", "MH - Mountain Hemlock",
				"MS - Montane Spruce", "pp = Ponderosa Pine", "SBPS - Sub-Boreal Pine-Spruce",
				"SBS - Sub-Boreal Spruce", "SWB - Spruce-Willow-Birch" };
		final String[] ecoZones = { "Boreal Cordillera", "Boreal Plains", "Montane Cordillera", "Pacific Maritime",
				"Taiga Plains" };

		// An array containing the options for AgeType
		final String[] ageTypes = { "Total", "Breast" };

		// Add choice box options and set defaults
		becZone.getItems().addAll(becZones); // these are defined below in the scene#2 section
		ecoZone.getItems().addAll(ecoZones);
		ageType.getItems().addAll(ageTypes);

		becZone.setValue("IDF - Interior Douglas Fir");
		ecoZone.setValue("Select Species");
		ageType.setValue("Total");

		SpinnerValueFactory<Double> standAgeValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.00, 500.00, 60.00, 10
		); // min, max, default, increment
			// Currently the default when WinVDYP is opened is 60
		standAge.setValueFactory(standAgeValueFactory);

		SpinnerValueFactory<Double> standHeightValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.00, 99.9, 17.00, 1
		);
		standHeight.setValueFactory(standHeightValueFactory);

		SpinnerValueFactory<Double> bha50SiteIndexValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.00, 60.0, 16.30, 1
		);
		bha50SiteIndex.setValueFactory(bha50SiteIndexValueFactory);
		bha50SiteIndex.setDisable(true); // Real WinVDYP uses the other two to calculate this, so it should be disable
											// by default

		runButton.setDisable(true);
		runButtonReport.setDisable(true);

		loggingOn.getItems().addAll("Yes", "No");
		loggingOn.setValue("Yes");

		loggingType.getItems().addAll("Basic", "Intermediate", "Advanced");
		loggingType.setValue("Basic");

		siteIndexCurve.setDisable(true);
	}

	// Radio button selections
	/**
	 * Handles the action event when the "Age (years)" option is selected in the
	 * float radiobutton. Disables the standAge choicebox and enables the
	 * bha50SiteIndex and standHeight choiceboxes.
	 *
	 * @param event The ActionEvent generated by the user action.
	 */
	public void standAgeSelected(ActionEvent event) {
		standAge.setDisable(true);
		bha50SiteIndex.setDisable(false);
		standHeight.setDisable(false); // since we don't know from where it's being called
	}

	/**
	 * Handles the action event when the "Height (meters)" option is selected in the
	 * float radiobutton. Disables the standHeight choicebox and enables the
	 * standAge and bha50SiteIndex choiceboxes.
	 *
	 * @param event The ActionEvent generated by the user action.
	 */
	public void standHeightSelected(ActionEvent event) {
		standHeight.setDisable(true);
		standAge.setDisable(false); // since we don't know from where it's being called
		bha50SiteIndex.setDisable(false);
	}

	/**
	 * Handles the action event when the "BHA 50 Site Index" option is selected in
	 * the float radiobutton. Disables the bha50SiteIndex choicebox and enables the
	 * standAge and standHeight choiceboxes .
	 *
	 * @param event The ActionEvent generated by the user action.
	 */
	public void siteIndexSelected(ActionEvent event) {
		bha50SiteIndex.setDisable(true);
		standAge.setDisable(false);
		standHeight.setDisable(false); // since we don't know from where it's being called
	}
	// End of radio button selections

	// Bottom Menu Bar functionality
	/**
	 * Handles the cancel button action event. This method is triggered when the
	 * cancel button is clicked in the site information table window. It closes this
	 * window.
	 *
	 * @param event The ActionEvent triggered by the cancel button click.
	 */
	public void cancelButtonAction(ActionEvent event) {
		MainController.getNewWindow().close();
	}

	/**
	 * Switches the application to Scene 1 - NewTableScene.
	 *
	 * @param event The ActionEvent triggering the scene switch.
	 * @throws IOException If an I/O error occurs during scene loading.
	 */
	public void switchToScene1(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/NewTableScene.fxml"));
		Parent root = loader.load();
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Switches the application to Scene 3 - StandDensityTableScene.
	 *
	 * @param event The ActionEvent triggering the scene switch.
	 * @throws IOException If an I/O error occurs during scene loading.
	 */
	public void switchToScene3(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/StandDensityTableScene.fxml"));
		Parent root = loader.load();
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	// Bottom Menu bar end

	/**
	 * A helper method to compute the width of a given text using a Text object.
	 *
	 * @param text The text for which the width needs to be computed.
	 * @return double The width of the provided text.
	 */
	private double computeTextWidth(String text) {
		Text helper = new Text();

		helper.setText(text);
		return helper.getLayoutBounds().getWidth();
	}

	/**
	 * Saves the choices passed in through a list of species choice boxes. It also
	 * saves the passed in labels so they can be accessed later
	 *
	 * @param speciesChoiceBoxes A list of ChoiceBox instances containing species
	 *                           choices.
	 * @throws IOException if an I/O error occurs.
	 */
	public void saveChoicesPassedIn(List<ChoiceBox<String>> speciesChoiceBoxes) throws IOException {
		if (speciesChoiceBoxes.isEmpty()) {
			return;
		}

		String[] passedDBHLabels = new String[6]; // impossible to be longer than 6
		int i = 0;
		for (ChoiceBox<String> species : speciesChoiceBoxes) {
			String speciesText = species.getValue();

			if (!speciesText.equals("Select Species")) {
				passedDBHLabels[i] = speciesText;
				i++;
			}
		}

		setDBHLabels(passedDBHLabels);
	}

	/**
	 * This displays the selected choices from the NewTableScene and adds them to
	 * the site species choice box in this scene
	 */
	public void displayChoices() {
		String[] choices = getDBHLabels();

		for (int i = 0; i < choices.length; i++) {
			if (choices[i] == null) {
				break;
			} else {
				siteSpecies.getItems().add(choices[i].substring(0, 2));
			}
		}
		siteSpecies.setValue(choices[0].substring(0, 2)); // set default
	}

}
