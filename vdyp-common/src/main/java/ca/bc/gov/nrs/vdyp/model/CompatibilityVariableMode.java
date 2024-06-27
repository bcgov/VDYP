package ca.bc.gov.nrs.vdyp.model;

public enum CompatibilityVariableMode {
	/**
	 * Don't apply compatibility variables
	 */
	NONE, // 0
	/**
	 * Apply compatibility variables to all but volume
	 */
	NO_VOLUME, // 1
	/**
	 * Apply compatibility variables to all components
	 */
	ALL // 2
}