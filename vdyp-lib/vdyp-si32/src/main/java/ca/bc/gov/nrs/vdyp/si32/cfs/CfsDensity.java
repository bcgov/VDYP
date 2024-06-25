package ca.bc.gov.nrs.vdyp.si32.cfs;

import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

public enum CfsDensity implements SI32Enum<CfsDensity> {

	MEAN_DENSITY_INDEX(0), //
	MIN_DENSITY_INDEX(1), //
	MAX_DENSITY_INDEX(2);

	public final int index;

	CfsDensity(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return ordinal();
	}

	@Override
	public int getOffset() {
		return ordinal();
	}

	@Override
	public String getText() {
		return toString();
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return MAX_DENSITY_INDEX.index - MEAN_DENSITY_INDEX.index + 1;
	}
}
