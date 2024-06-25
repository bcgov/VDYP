package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Lists the indices for species supported by the CFS Biomass conversion process.
 * <ul>
 * <li><b>UNKNOWN</b> indicates an uninitialized or error condition. This value should never be used to indicate an
 * actual species index value.
 * <li><b>others</b> indices into the appropriate dimension for the {@link CfsBiomassConversionCoefficients} array for
 * the indicated CFS Species.
 * </ul>
 * <b>Remarks</b>
 * <p>
 * For BC, there are a limited number of CFS defined species for which there is a set of conversion factors for within
 * specific Eco Zones.
 * <p>
 * This enumeration lists each of the species across all Eco Zones for which species specific conversion factors appear
 * at least once.
 * <p>
 * The list of enumeration constants is automatically generated and copy and pasted into this enum definition from the:
 * <ol>
 * <li>'Internal C Enum Definition' column of the
 * <li>'SpeciesTable' table found on the
 * <li>'Lookups' tab in the
 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
 * <li>'Documents/CFS-Biomass' folder.
 * </ol>
 */
public enum CfsBiomassConversionSupportedSpecies implements SI32Enum<CfsBiomassConversionSupportedSpecies> {
	UNKNOWN(-1),

	AC(0), //
	ACB(1), //
	AT(2), //
	B(3), //
	BA(4), //
	BG(5), //
	BL(6), //
	CW(7), //
	DR(8), //
	EA(9), //
	EP(10), //
	EXP(11), //
	FD(12), //
	FDC(13), //
	FDI(14), //
	H(15), //
	HM(16), //
	HW(17), //
	L(18), //
	LA(19), //
	LT(20), //
	LW(21), //
	MB(22), //
	PA(23), //
	PL(24), //
	PLC(25), //
	PLI(26), //
	PW(27), //
	PY(28), //
	S(29), //
	SB(30), //
	SE(31), //
	SS(32), //
	SW(33), //
	SX(34), //
	W(35), //
	XC(36), //
	YC(37);

	private final int index;

	private CfsBiomassConversionSupportedSpecies(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	/**
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static CfsBiomassConversionSupportedSpecies forIndex(int index) {
		for (CfsBiomassConversionSupportedSpecies e : CfsBiomassConversionSupportedSpecies.values()) {
			if (index == e.index)
				return e;
		}

		return null;
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
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return YC.index - AC.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsBiomassConversionSupportedSpecies> {
		public Iterator() {
			super(values(), AC, YC);
		}
	}
}
