package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;

/**
 * Common accessors for utilization vecors shared by Layer and Species
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public interface VdypUtilizationHolder {

	/**
	 * Close utilization volume net of decay, waste and breakage for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
			Coefficients closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization
	);

	/**
	 * Close utilization volume net of decay, waste and breakage for utilization index -1 through 0
	 */
	Coefficients getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization();

	/**
	 * Close utilization volume net of decay and waste for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
			Coefficients closeUtilizationVolumeNetOfDecayAndWasteByUtilization
	);

	/**
	 * Close utilization volume net of decay and waste for utilization index -1 through 0
	 */
	Coefficients getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization();

	/**
	 * Close utilization volume net of decay for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeNetOfDecayByUtilization(Coefficients closeUtilizationNetVolumeOfDecayByUtilization);

	/**
	 * Close utilization volume net of decay for utilization index -1 through 0
	 */
	Coefficients getCloseUtilizationVolumeNetOfDecayByUtilization();

	/**
	 * Close utilization volume for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeByUtilization(Coefficients closeUtilizationVolumeByUtilization);

	/**
	 * Close utilization volume for utilization index -1 through 0
	 */
	Coefficients getCloseUtilizationVolumeByUtilization();

	/**
	 * Whole stem volume for utilization index -1 through 0
	 */
	void setWholeStemVolumeByUtilization(Coefficients wholeStemVolumeByUtilization);

	/**
	 * Whole stem volume for utilization index -1 through 0
	 */
	Coefficients getWholeStemVolumeByUtilization();

	/**
	 * Trees per hectare for utilization index -1 through 0
	 */
	void setTreesPerHectareByUtilization(Coefficients treesPerHectareByUtilization);

	/**
	 * Trees per hectare for utilization index -1 through 0
	 */
	Coefficients getTreesPerHectareByUtilization();

	/**
	 * Quadratic mean of diameter for utilization index -1 through 0
	 */
	void setQuadraticMeanDiameterByUtilization(Coefficients quadraticMeanDiameterByUtilization);

	/**
	 * Quadratic mean of diameter for utilization index -1 through 0
	 */
	Coefficients getQuadraticMeanDiameterByUtilization();

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	void setLoreyHeightByUtilization(Coefficients loreyHeightByUtilization);

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	Coefficients getLoreyHeightByUtilization();

	/**
	 * Base area for utilization index -1 through 4
	 */
	void setBaseAreaByUtilization(Coefficients baseAreaByUtilization);

	/**
	 * Base area for utilization index -1 through 4
	 */
	Coefficients getBaseAreaByUtilization();

	static Coefficients emptyUtilization() {
		return new Coefficients(Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1);
	}

	static Coefficients emptyLoreyHeightUtilization() {
		return new Coefficients(Arrays.asList(0f, 0f), -1);
	}
}
