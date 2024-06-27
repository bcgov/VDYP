package ca.bc.gov.nrs.vdyp.model;

public enum VolumeComputeMode {
	/**
	 * set volume components to zero
	 */
	ZERO, // 0
	/**
	 * compute volumes by utilization component
	 */
	BY_UTIL, // 1
	/**
	 * As BY_UTIL but also compute Whole Stem Volume for every species
	 */
	BY_UTIL_WITH_WHOLE_STEM_BY_SPEC // 2
}