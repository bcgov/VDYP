package ca.bc.gov.nrs.vdyp.model;

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
			UtilizationVector closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization
	);

	/**
	 * Close utilization volume net of decay, waste and breakage for utilization index -1 through 0
	 */
	UtilizationVector getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization();

	/**
	 * Close utilization volume net of decay and waste for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
			UtilizationVector closeUtilizationVolumeNetOfDecayAndWasteByUtilization
	);

	/**
	 * Close utilization volume net of decay and waste for utilization index -1 through 0
	 */
	UtilizationVector getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization();

	/**
	 * Close utilization volume net of decay for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeNetOfDecayByUtilization(
			UtilizationVector closeUtilizationNetVolumeOfDecayByUtilization
	);

	/**
	 * Close utilization volume net of decay for utilization index -1 through 0
	 */
	UtilizationVector getCloseUtilizationVolumeNetOfDecayByUtilization();

	/**
	 * Close utilization volume for utilization index -1 through 0
	 */
	void setCloseUtilizationVolumeByUtilization(UtilizationVector closeUtilizationVolumeByUtilization);

	/**
	 * Close utilization volume for utilization index -1 through 0
	 */
	UtilizationVector getCloseUtilizationVolumeByUtilization();

	/**
	 * Whole stem volume for utilization index -1 through 0
	 */
	void setWholeStemVolumeByUtilization(UtilizationVector wholeStemVolumeByUtilization);

	/**
	 * Whole stem volume for utilization index -1 through 0
	 */
	UtilizationVector getWholeStemVolumeByUtilization();

	/**
	 * Trees per hectare for utilization index -1 through 0
	 */
	void setTreesPerHectareByUtilization(UtilizationVector treesPerHectareByUtilization);

	/**
	 * Trees per hectare for utilization index -1 through 0
	 */
	UtilizationVector getTreesPerHectareByUtilization();

	/**
	 * Quadratic mean of diameter for utilization index -1 through 0
	 */
	void setQuadraticMeanDiameterByUtilization(UtilizationVector quadraticMeanDiameterByUtilization);

	/**
	 * Quadratic mean of diameter for utilization index -1 through 0
	 */
	UtilizationVector getQuadraticMeanDiameterByUtilization();

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	void setLoreyHeightByUtilization(UtilizationVector loreyHeightByUtilization);

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	UtilizationVector getLoreyHeightByUtilization();

	/**
	 * Base area for utilization index -1 through 4
	 */
	void setBaseAreaByUtilization(UtilizationVector baseAreaByUtilization);

	/**
	 * Base area for utilization index -1 through 4
	 */
	UtilizationVector getBaseAreaByUtilization();

	static UtilizationVector emptyUtilization() {
		return new UtilizationVector(0f, 0f, 0f, 0f, 0f, 0f);
	}

	static UtilizationVector emptyLoreyHeightUtilization() {
		return new UtilizationVector(0f, 0f);
	}
}
