package application.controller;

import java.io.IOException;
import java.net.URL;
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

public class AdditionalStandAtrributesTableSceneController implements Initializable {

	@FXML
	Spinner<Double> loreyHeight;
	@FXML
	Spinner<Double> basalArea12;
	@FXML
	Spinner<Double> cuVolume; // cu = Close Utilization
	@FXML
	Spinner<Double> cuNetDecayWasteVolume;
	@FXML
	Spinner<Double> wholeStemVolume7;
	@FXML
	Spinner<Double> wholeStemVolume12;
	@FXML
	Spinner<Double> cuNetDecayVolume;
	@FXML
	private Button runButton;
	@FXML
	private Button runButtonReport;
	@FXML
	private ChoiceBox<String> loggingOn;
	@FXML
	private ChoiceBox<String> loggingType;

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
	}

	/**
	 * Enables computed values for various controls. Method is called when you want
	 * to enable the computed values and the appropriate checkbox is selected.
	 */
	public void useComputedValues() {
		loreyHeight.setDisable(true);
		basalArea12.setDisable(true);
		cuVolume.setDisable(true);
		cuNetDecayWasteVolume.setDisable(true);
		wholeStemVolume7.setDisable(true);
		wholeStemVolume12.setDisable(true);
		cuNetDecayVolume.setDisable(true);
	}

	/**
	 * Disables computed values for various controls. Method is called when you want
	 * to modify the computed values and the appropriate checkbox is selected.
	 */
	public void modifyComputedValues() {
		loreyHeight.setDisable(false);
		basalArea12.setDisable(false);
		cuVolume.setDisable(false);
		cuNetDecayWasteVolume.setDisable(false);
		wholeStemVolume7.setDisable(false);
		wholeStemVolume12.setDisable(false);
		cuNetDecayVolume.setDisable(false);
	}

	/**
	 * Sets default values for various controls in the window and disables certain controls.
	 * The default values and control states are as follows:
	 * @formatter:off
	 * 		- loreyHeight: Default value set to 21.83, with a range from 0.00 to 99.90 and an increment of 1.00. Disabled by default.
	 * 		- basalArea12: Default value set to 39.3337, with a range from 0.10 to 250.00 and an increment of 1.00. Disabled by default.
	 * 		- cuVolume: Default value set to 39.3337, with a range from 0.0 to 2500.0 and an increment of 1.00. Disabled by default.
	 * 		- cuNetDecayWasteVolume: Default value set to 245.5, with a range from 0.0 to 2500.0 and an increment of 1.00. Disabled by default.
	 * 		- wholeStemVolume7: Default value set to 332.4, with a range from 0.0 to 2500.0 and an increment of 1.00. Disabled by default.
	 * 		- wholeStemVolume12: Default value set to 332.4, with a range from 0.0 to 2500.0 and an increment of 1.00. Disabled by default.
	 * 		- cuNetDecayVolume: Default value set to 245.5, with a range from 0.0 to 2500.0 and an increment of 1.00. Disabled by default.
	 * 		- runButton: The runButton control is disabled by default.
	 * 		- runButtonReport: Disabled by default- loggingOn: Sets the choices Yes and No. Yes by default
	 * 		- loggingType: Set the choices as Basic,Intermediate and Advanced. Basic by Default
	 * 		- loggingOn: Sets the choices Yes and No. Yes by default
	 * @formatter:on
	 */
	public void setDefaults() {
		SpinnerValueFactory<Double> loreyHeightValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.00, 99.90, 21.83, 1
		);
		loreyHeight.setValueFactory(loreyHeightValueFactory);
		loreyHeight.setDisable(true);

		SpinnerValueFactory<Double> basalArea12ValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.10, 250.00, 39.3337, 1
		);
		basalArea12.setValueFactory(basalArea12ValueFactory);
		basalArea12.setDisable(true);

		SpinnerValueFactory<Double> cuVolumeValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.0, 2500.00, 39.3337, 1
		);
		cuVolume.setValueFactory(cuVolumeValueFactory);
		cuVolume.setDisable(true);

		SpinnerValueFactory<Double> cuNetDecayWasteVolumeValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.0, 2500.00, 245.5, 1
		);
		cuNetDecayWasteVolume.setValueFactory(cuNetDecayWasteVolumeValueFactory);
		cuNetDecayWasteVolume.setDisable(true);

		SpinnerValueFactory<Double> wholeStemVolume7ValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.0, 2500.00, 332.4, 1
		);
		wholeStemVolume7.setValueFactory(wholeStemVolume7ValueFactory);
		wholeStemVolume7.setDisable(true);

		SpinnerValueFactory<Double> wholeStemVolume12ValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.0, 2500.00, 332.4, 1
		);
		wholeStemVolume12.setValueFactory(wholeStemVolume12ValueFactory);
		wholeStemVolume12.setDisable(true);

		SpinnerValueFactory<Double> cuNetDecayVolumeValueFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
				0.0, 2500.00, 245.5, 1
		);
		cuNetDecayVolume.setValueFactory(cuNetDecayVolumeValueFactory);
		cuNetDecayVolume.setDisable(true);

		runButton.setDisable(true);
		runButtonReport.setDisable(true);

		loggingOn.getItems().addAll("Yes", "No");
		loggingOn.setValue("Yes");

		loggingType.getItems().addAll("Basic", "Intermediate", "Advanced");
		loggingType.setValue("Basic");
	}

	// Bottom Menu Bar functionality
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
	 * Switches the application to Scene 3 - StandDensityTableScene.
	 *
	 * @param event The ActionEvent triggering the scene switch.
	 * @throws IOException If an I/O error occurs during scene loading.
	 */
	public void switchToScene3(ActionEvent event) throws IOException {
		SiteInformationTableSceneController siteInformationTableSceneController = new SiteInformationTableSceneController();
		siteInformationTableSceneController.switchToScene3(event);
	}

	/**
	 * Switches the application to Scene 5 - ReportInformationTableScene.
	 *
	 * @param event The ActionEvent triggering the scene switch.
	 * @throws IOException If an I/O error occurs during scene loading.
	 */
	public void switchToScene5(ActionEvent event) throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/ReportInformationTableScene.fxml"));
		Parent root = loader.load();
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		Scene scene = new Scene(root);
		stage.setScene(scene);
		stage.show();
	}
	// End of bottom menu bar

}
