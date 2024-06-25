package ca.bc.gov.nrs.vdyp.si32.bec;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Identifies each of the BEC Zones known to the system.
 * <ul>
 * <li>UNKNOWN: Represents an unknown BEC Zone or an error condition.
 * <li>others: Individual BEC Zones recognized.
 */
public enum BecZone implements SI32Enum<BecZone> {
	UNKNOWN(-1),

	AT(0), BG(1), BWBS(2), CDF(3), CWH(4), ESSF(5), ICH(6), IDF(7), MH(8), MS(9), PP(10), SBPS(11), SBS(12), SWB(13);

	private static Map<Integer, BecZone> index2EnumMap = null;

	private static synchronized Map<Integer, BecZone> getIndex2EnumMap() {
		if (index2EnumMap == null) {
			index2EnumMap = new HashMap<>();
		}
		return index2EnumMap;
	}

	private final int index;

	private BecZone(int index) {
		this.index = index;
		getIndex2EnumMap().put(index, this);
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

		return ordinal() - 1;
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
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static BecZone forIndex(int index) {
		return getIndex2EnumMap().get(index);
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return SWB.index - AT.index + 1;
	}

	public static class Iterator extends EnumIterator<BecZone> {
		public Iterator() {
			super(values(), AT, SWB);
		}
	}
}
