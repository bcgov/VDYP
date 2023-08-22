package application.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;

public class ReportInformationTableSceneController implements Initializable {
	@FXML
	private Spinner<Integer> startingAge;
	@FXML
	private Spinner<Integer> finishingAge;
	@FXML
	private Spinner<Integer> ageIncrement;
	@FXML
	private ChoiceBox<String> projectionType;
	@FXML
	private Button nextPageButton;
	@FXML
	private Label nextLabel;
	@FXML
	private TabPane tabPane;
	@FXML
	private Button runButton;
	@FXML
	private Button runButtonReport;
	@FXML
	private ChoiceBox<String> loggingOn;
	@FXML
	private ChoiceBox<String> loggingType;
	@FXML
	private CheckBox wholeStem;
	@FXML
	private CheckBox closeUtilization;
	@FXML
	private CheckBox netDecay;
	@FXML
	private CheckBox netDecayWaste;
	@FXML
	private CheckBox netDecayWasteBreakage;
	@FXML
	private CheckBox computedMAI;
	@FXML
	private CheckBox speciesCompostion;
	@FXML
	private CheckBox culminationValues;
	@FXML
	private GridPane dbhSliders;
	@FXML
	private Slider dbhSpecies2Slider;
	@FXML
	private Slider dbhSpecies4Slider;
	@FXML
	private TextArea modelReportText;
	@FXML
	private TextArea logFileText;
	@FXML
	public Label dbhSpecies1;
	@FXML
	public Label dbhSpecies2;
	@FXML
	private Label dbhSpecies3;
	@FXML
	private Label dbhSpecies4;

	private static final String VOLUME = "Volume";

	/**
	 * Initializes the window by setting default values for various controls.
	 *
	 *
	 * @param location  The URL of the FXML file. Unused in this method.
	 * @param resources The ResourceBundle containing locale-specific resources.
	 *                  Unused in this method.
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		setDefaults();

		projectionType.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (VOLUME.equals(newValue)) {
				volumeSelected();
			} else {
				cfsBiomassSelected();
			}
		});
	}

	/**
	 * Handles the action event when the "CFS Biomass" option is selected in the
	 * checkbox. Disables most of the Volumes Reported and Include in Report options
	 * as well as the minimum DBH sliders
	 *
	 */
	private void cfsBiomassSelected() {
		wholeStem.setDisable(true);
		closeUtilization.setDisable(true);
		netDecay.setDisable(true);
		netDecayWaste.setDisable(true);
		netDecayWasteBreakage.setDisable(true);
		computedMAI.setDisable(true);
		dbhSliders.setDisable(true);

	}

	/**
	 * Handles the action event when the "Volume" option is selected in the
	 * checkbox. Enables most of the Volumes Reported and Include in Report options
	 * as well as the minimum DBH sliders
	 *
	 */
	private void volumeSelected() {
		wholeStem.setDisable(false);
		closeUtilization.setDisable(false);
		netDecay.setDisable(false);
		netDecayWaste.setDisable(false);
		netDecayWasteBreakage.setDisable(false);
		computedMAI.setDisable(false);
		dbhSliders.setDisable(false);
	}

	/**
	 * Sets default values for various controls in the window.
	 *
	 * The default values and control states are as follows:
	 * @formatter:off
	 * 		- startingAge: Default value set to 0, with a range from 0 to 500 and an increment of 10.
	 * 		- finishingAge: Default value set to 250, with a range from 1 to 460 and an increment of 10.
	 * 		- ageIncrement: Default value set to 25, with a range from 1 to 350 and an increment of 5.
	 * 		- projectionType: Default value set to "Volume" with options "Volume" and "CFS Biomass".
	 * 		- nextPageButton: The nextPageButton control is disabled by default.
	 * 		- nextLabel: The nextLabel control is disabled by default.
	 * 		- loggingOn: Sets the choices Yes and No. Yes by default
	 * 		- loggingType: Set the choices as Basic,Intermediate and Advanced. Basic by Default
	 * 		- dbhSpecies2Slider - Sets the spot of the slider to 7.5cm+
	 * 		- dbhSpecies4Slider - Sets the spot of the slider to 12.5cm+
	 * 		- culminationValues - Set to disabled by default
	 * 		- updates dbh labels by calling the updateDBHLabels function
	 * @formatter:on
	 */
	public void setDefaults() {
		SpinnerValueFactory<Integer> startingAgeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
				0, 500, 0, 10
		); // min, max, default, increment
		startingAge.setValueFactory(startingAgeValueFactory);

		SpinnerValueFactory<Integer> finishingAgeValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
				1, 460, 250, 10
		); // min, max, default, increment
		finishingAge.setValueFactory(finishingAgeValueFactory);

		SpinnerValueFactory<Integer> ageIncrementValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(
				1, 350, 25, 5
		); // min, max, default, increment
		ageIncrement.setValueFactory(ageIncrementValueFactory);

		projectionType.getItems().addAll(VOLUME, "CFS Biomass");
		projectionType.setValue(VOLUME);

		nextPageButton.setDisable(true);
		nextLabel.setDisable(true);

		loggingOn.getItems().addAll("Yes", "No");
		loggingOn.setValue("Yes");

		loggingType.getItems().addAll("Basic", "Intermediate", "Advanced");
		loggingType.setValue("Basic");

		dbhSpecies2Slider.setValue(10); // the labels don't line up since the distance isn't even. Instead the slider is
										// split by 5 from 0-20

		dbhSpecies4Slider.setValue(15);

		culminationValues.setDisable(true);// always disabled in WinVDYP

		updateDBHLabels();
	}

	/**
	 * Gets the dbh labels from SiteInformationTableSceneController and updates the
	 * DBH labels on the user interface.
	 */
	private void updateDBHLabels() {
		String[] dbhLabels = SiteInformationTableSceneController.getDBHLabels();
		dbhSpecies1.setText(dbhLabels[0]);
		dbhSpecies2.setText(dbhLabels[1]);
		dbhSpecies3.setText(dbhLabels[2]);
		dbhSpecies4.setText(dbhLabels[3]);
	}

	// Bottom menu bar
	/**
	 * Switches the application to Scene 4 - StandDensityTableScene.
	 *
	 * @param event The ActionEvent triggering the scene switch.
	 * @throws IOException If an I/O error occurs during scene loading.
	 */
	public void switchToScene4(ActionEvent event) throws IOException {
		StandDensityTableSceneController standDensityTableSceneController = new StandDensityTableSceneController();
		standDensityTableSceneController.switchToScene4(event);
	}

	/**
	 * Handles the cancel button action event. This method is triggered when the
	 * cancel button is clicked in the new table window. It closes this new table
	 * window.
	 *
	 * @param event The ActionEvent triggered by the cancel button click.
	 */
	public void cancelButtonAction(ActionEvent event) {
		MainController.getNewWindow().close();
	}

	/**
	 * Handles the run button action event.
	 *
	 * This method is triggered when the run model button is clicked in the table
	 * window. It adds the text and creates an entry in the log file
	 *
	 * @param event The ActionEvent triggered by the run button click.
	 * @throws IOException
	 */
	public void runButtonAction(ActionEvent event) {
		String modelRunText = """
				                                 VDYP7 Yield Table
				             Lodgepole Pine 30.0%, Poplar 30.0%, Hemlock 30.0%, Spruce 10.0%

				                Quad                       |  Whole
				    Site Lorey  Stnd                       |  Stem
				TOT   HT    HT    DIA     BA        TPH    |  VOLUME
				-------------------------------------------+-----------
				                                           |
				  0   0.0                                  |
				 25   7.7                                  |
				 50  15.0  10.9  12.3      10.6        895 |       44.2
				 75  19.4  16.3  17.8      26.1       1054 |      165.0
				100  22.0  20.1  20.6      32.0        959 |      253.2
				125  23.8  23.0  23.6      35.8        815 |      325.7
				150  25.0  25.3  27.4      37.1        631 |      369.5
				175  25.8  26.6  30.4      36.7        507 |      382.2
				200  26.5  27.2  32.4      36.5        444 |      385.7
				225  26.9  27.2  33.5      36.6        416 |      385.8
				250  27.3  27.3  34.4      36.7        394 |      386.3

				NOTE: Height 7.7 at Projection Age 25.0 is too short to generate yields for species 'PL'
				NOTE: Projected data for species 'PL' was not generated at stand age 25.0
				NOTE: Basal Area and Trees per HA computed using Default CC of 50%
				NOTE: Yields are not predicted prior to age 50.

				TABLE PROPERTIES:

				WinVDYP Version Number... 7.33b         % Stockable Area Supplied 55
				VDYP7 Core DLL Version... 7.19h         CFS Eco Zone.............
				VDYP SI Version Number... 7.13c         Trees Per Hectare........ <Not Used>
				SINDEX Version Number.... 1.51          Measured Basal Area...... <Not Used>
				Species 1................ PL (30.0%)    Starting Total Age....... 0
				Species 2................ AC (30.0%)    Finishing Total Age...... 250
				Species 3................ H  (30.0%)    Age Increment............ 25
				Species 4................ S  (10.0%)    Projected Values......... Volume
				FIP Calc Mode............ 1             Min DBH Limit: PL........ 7.5 cm+
				BEC Zone................. IDF           Min DBH Limit: AC........ 7.5 cm+
				Incl Second Species Ht... N/A           Min DBH Limit: H ........ 7.5 cm+
				% Crown Closure Supplied. 0             Min DBH Limit: S ........ 7.5 cm+

				Species Parameters...
				 Species |  % Comp | Tot Age |  BH Age |  Height |    SI   |  YTBH
				 --------+---------+---------+---------+---------+---------+---------
				    PL   |  30.0   |    60   |    54   |  17.00  |  16.30  |  6.80
				    AC   |  30.0   |   N/A   |   N/A   |   N/A   |   N/A   |   N/A
				     H   |  30.0   |   N/A   |   N/A   |   N/A   |  12.52  |  8.80
				     S   |  10.0   |   N/A   |   N/A   |   N/A   |   N/A   |   N/A

				Site Index Curves Used...
				  Age Range | Species | SI Curve Name
				 -----------+---------+-------------------------------------------------
				   50 -  250|   Hwi   | 37 - Nigh (1998)


				Additional Stand Attributes:
				----------------------------

				None Applied.""";

		LocalDateTime currentTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy ");
		String formattedTime = currentTime.format(formatter);

		String logFile = formattedTime + ": Model run and report generated" + "\n";

		modelReportText.setText(modelRunText);
		logFileText.appendText(logFile);

		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

		if (selectedTab.getText().equals("Model Parameter Selection")) {
			tabPane.getSelectionModel().selectNext();
		}

	}

}
