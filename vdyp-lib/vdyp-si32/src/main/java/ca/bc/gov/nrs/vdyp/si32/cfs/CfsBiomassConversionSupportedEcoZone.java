package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Lists indices to Eco Zone conversion factors for the supported CFS Biomass conversion factors.
 * <ul>
 * <li>UNKNOWN: Indicates an uninitialized value or error condition. This should never be used to indicate a valid Eco
 * Zone Index.
 * <li>other: Indices into the {@link CfsBiomassConversionCoefficients} array corresponding to the identified CFS Eco
 * Zone.
 * </ul>
 * For the BC implementation of the CFS Conversion Factors, only a subset Eco Zones are supported. This enumeration
 * lists each of those Eco Zones and their corresponding index into the Cfs*BiomassConversionCoefficients arrays.
 * <p>
 * The list of enumeration constants is automatically generated and copy and pasted into this enum definition from the:
 * <ol>
 * <li>'Conversion Param Enum Defn' column of the
 * <li>'DeadConversionFactorsTable' found on the
 * <li>'Derived C Species Table' tab in the
 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
 * <li>'Documents/CFS-Biomass' folder.
 * </ol>
 */
public enum CfsBiomassConversionSupportedEcoZone implements SI32Enum<CfsBiomassConversionSupportedEcoZone> {
	UNKNOWN(-1),

	/* 4 */
	TAIGA_PLAINS(0),
	/* 9 */
	BOREAL_PLAINS(1),
	/* 12 */
	BOREAL_CORDILLERA(2),
	/* 13 */
	PACIFIC_MARITIME(3),
	/* 14 */
	MONTANE_CORDILLERA(4);

	private final int index;

	private CfsBiomassConversionSupportedEcoZone(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getOffset() {
		if (this.equals(UNKNOWN)) {
			throw new UnsupportedOperationException(
					MessageFormat
							.format("Cannot call getIndex on {} as it's not a standard member of the enumeration", this)
			);
		}

		return index;
	}

	@Override
	public String getText() {
		if (this.equals(UNKNOWN)) {
			throw new UnsupportedOperationException(
					MessageFormat
							.format("Cannot call getText on {} as it's not a standard member of the enumeration", this)
			);
		}

		return this.toString();
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return MONTANE_CORDILLERA.index - TAIGA_PLAINS.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsBiomassConversionSupportedEcoZone> {
		public Iterator() {
			super(values(), TAIGA_PLAINS, MONTANE_CORDILLERA);
		}
	}
}
