package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Live CFS Biomass definitions.
 * <ul>
 * <li><b>UNKNOWN</b> Indicates an error condition or an uninitialized value. This value should never be used as an
 * index for a specific conversion parameter.
 * <li><b>others</b> Indices into the {@link CfsBiomassConversionCoefficientsForSpecies} array for each of the CFS
 * Biomass conversion parameters/coefficients.
 * </ul>
 * The CFS Biomass Conversion process is based on a number of hard coded constants/coefficients which are used to adjust
 * the conversion by CFS Eco Zone and Species.
 * <ol>
 * <li>'Conversion Param Enum Defn' column of the
 * <li>'DeadConversionFactorsTable' found on the
 * <li>'Derived C Species Table' tab in the
 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
 * <li>'Documents/CFS-Biomass' folder.
 * </ol>
 */
public enum CfsLiveConversionParams implements SI32Enum<CfsLiveConversionParams> {
	UNKNOWN(-1, "UNKNOWN", "??"),

	/* Rows copied from spreadsheet appear below. */
	A(0, "Merch", "A"), //
	B(1, "Merch", "B"), //
	A_NONMERCH(2, "Non-Merch", "A"), //
	B_NONMERCH(3, "Non-Merch", "B"), //
	K_NONMERCH(4, "Non-Merch", "K"), //
	CAP_NONMERCH(5, "Non-Merch", "CAP"), //
	A_SAP(6, "Sapling", "A"), //
	B_SAP(7, "Sapling", "B"), //
	K_SAP(8, "Sapling", "K"), //
	CAP_SAP(9, "Sapling", "CAP"), //
	A1(10, "Prop Parm", "A1"), //
	A2(11, "Prop Parm", "A2"), //
	A3(12, "Prop Parm", "A3"), //
	B1(13, "Prop Parm", "B1"), //
	B2(14, "Prop Parm", "B2"), //
	B3(15, "Prop Parm", "B3"), //
	C1(16, "Prop Parm", "C1"), //
	C2(17, "Prop Parm", "C2"), //
	C3(18, "Prop Parm", "C3"), //
	MIN_VOLUME(19, "Volume", "Min"), //
	MAX_VOLUME(20, "Volume", "Max"), //
	LOW_STEMWOOD_PROP(21, "Stemwood", "Min"), //
	HIGH_STEMWOOD_PROP(22, "Stemwood", "Max"), //
	LOW_STEMBARK_PROP(23, "Stembark", "Min"), //
	HIGH_STEMBARK_PROP(24, "Stembark", "Max"), //
	LOW_BRANCHES_PROP(25, "Branches", "Min"), //
	HIGH_BRANCHES_PROP(26, "Branches", "Max"), //
	LOW_FOLIAGE_PROP(27, "Foliage", "Min"), //
	HIGH_FOLIAGE_PROP(28, "Foliage", "Max");

	private final int index;
	private final String category;
	private final String shortName;

	private CfsLiveConversionParams(int index, String category, String shortName) {
		this.index = index;
		this.category = category;
		this.shortName = shortName;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public String getCategory() {
		return category;
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	public int getOffset() {
		if (this.equals(UNKNOWN)) {
			throw new UnsupportedOperationException(
					MessageFormat.format(
							"Cannot call getIndex on {0} as it's not a standard member of the enumeration", this
					)
			);
		}

		return index;
	}

	@Override
	public String getText() {
		if (this.equals(UNKNOWN)) {
			return "";
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
	public static CfsLiveConversionParams forIndex(int index) {
		for (CfsLiveConversionParams e : CfsLiveConversionParams.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return HIGH_FOLIAGE_PROP.index - A.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsLiveConversionParams> {
		public Iterator() {
			super(values(), A, HIGH_FOLIAGE_PROP);
		}
	}
}
