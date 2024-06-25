package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Provides an enumeration of the different types of Tree Classes as defined by the Canadian Forest Service.
 * <ul>
 * <li>UNKNOWN
 * <p>
 * Represents an error condition or an uninitialized state. This should never be considered a true tree class.
 *
 * <li>Missing
 * <p>
 * The tree class attribute is missing. This differs from the UNKNOWN indicator in that this represents a valid business
 * value of Missing (which may in turn represent an business process error) but remains a valid value for the attribute.
 *
 * <li>LiveNoPath
 * <p>
 * A tree or stand that is living with no pathological indicators.
 *
 * <li>LiveWithPath
 * <p>
 * A living tree or stand with some pathological indicators.
 *
 * <li>DeadPotential
 * <p>
 * A dead tree or stand with potential for harvesting.
 *
 * <li>DeadUseless
 * <p>
 * A dead tree or stand with no further potential.
 *
 * <li>Veteran
 * <p>
 * A living tree or stand falling into the Veteran class.
 *
 * <li>NoLongerUsed
 * <p>
 * No longer used and should be treated as equivalent to: 'LiveWithPath'.
 * </ul>
 * The definitions of this table come from Table 2 found in the 'Volume_to_Biomass.doc' file located in
 * 'Documents/CFS-Biomass'.
 */
public enum CfsTreeClass implements SI32Enum<CfsTreeClass> {
	UNKNOWN(-1, "Unknown CFS Tree Class"),

	MISSING(0, "Missing"), //
	LIVE_NO_PATH(1, "Live, no pathological indicators"), //
	LIVE_WITH_PATH(2, "Live, some patholigical indicators"), //
	DEAD_POTENTIAL(3, "Dead, potentially merchantable"), //
	DEAD_USELESS(4, "Dead, not merchantable"), //
	VETERAN(5, "Veteran"), //
	NO_LONGER_USED(6, "No longer used");

	private final int index;
	private final String description;

	private CfsTreeClass(int index, String description) {
		this.index = index;
		this.description = description;
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

	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static CfsTreeClass forIndex(int index) {

		for (CfsTreeClass e : CfsTreeClass.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return NO_LONGER_USED.index - MISSING.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsTreeClass> {
		public Iterator() {
			super(values(), MISSING, NO_LONGER_USED);
		}
	}
}
