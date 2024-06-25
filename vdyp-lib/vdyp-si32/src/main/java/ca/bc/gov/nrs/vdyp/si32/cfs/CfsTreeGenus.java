package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Enumerate the different Tree Genus as defined by the Canadian Forest Service for the purposes of Biomass.
 * <ul>
 * <li><b>UNKNOWN</b> represents an uninitialized value or error state. This value should not be used as a valid Genus
 * value.</b>
 * <li><b>NotApplicable</b> for records that are not stocked forest land.
 * <li><b>MissingValue</b> no information is available.
 * <li><b>others</b>individual Genus values.
 * </ul>
 * <p>
 * <b>Remarks</b>
 * <p>
 * The values for 'NotApplicable' and 'MissingValue' are defined in the CFS source table but should never be used in the
 * context of VDYP as all CFS species have a Genus mapping. They are included for completeness of the source
 * definitions. They should never be used as a valid CFS Genus value.
 * <p>
 * Elements for this table are automatically generated and copy and pasted from the:
 * <ol>
 * <li>'Internal C Enum Definition' column of the
 * <li>'SpeciesTable' table found on the
 * <li>'Lookups' tab in the
 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
 * <li>'Documents/CFS-Biomass' folder.
 * </ol>
 */
public enum CfsTreeGenus implements SI32Enum<CfsTreeGenus> {
	UNKNOWN(-1, "UNKNOWN"), //

	NOT_APPLICABLE(-9, "Not applicable"), //
	MISSING_VALUE(-8, "Missing Value"), //

	SPRUCE(1, "Spruce"), //
	PINE(2, "Pine"), //
	FIR(3, "Fir"), //
	HEMLOCK(4, "Hemlock"), //
	DOUGLAS_FIR(5, "Douglas-fir"), //
	LARCH(6, "Larch"), //
	CEDAR_AND_OTHER_CONIFERS(7, "Cedar and other conifers"), //
	UNSPECIFIED_CONIFERS(8, "Unspecified conifers"), //
	POPLAR(9, "Poplar"), //
	BIRCH(10, "Birch"), //
	MAPLE(11, "Maple"), //
	OTHER_BROAD_LEAVES(12, "Other broadleaved species"), //
	UNSPECIFIED_BROAD_LEAVES(13, "Unspecified broadleaved species"); //

	private final int index;
	private final String genusName;

	private CfsTreeGenus(int index, String genusName) {
		this.index = index;
		this.genusName = genusName;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public String getGenusName() {
		return genusName;
	}

	@Override
	public int getOffset() {
		if (this.equals(UNKNOWN) || this.equals(NOT_APPLICABLE) || this.equals(MISSING_VALUE)) {
			throw new UnsupportedOperationException(
					MessageFormat.format(
							"Cannot call getIndex on {0} as it's not a standard member of the enumeration", this
					)
			);
		}

		return index - 1;
	}

	@Override
	public String getText() {
		if (this.equals(UNKNOWN)) {
			throw new UnsupportedOperationException(
					MessageFormat
							.format("Cannot call getText on {0} as it's not a standard member of the enumeration", this)
			);
		}

		return this.toString();
	}

	/**
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static CfsTreeGenus forIndex(int index) {
		for (CfsTreeGenus e : CfsTreeGenus.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return UNSPECIFIED_BROAD_LEAVES.index - SPRUCE.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsTreeGenus> {
		public Iterator() {
			super(values(), SPRUCE, UNSPECIFIED_BROAD_LEAVES);
		}
	}
}
