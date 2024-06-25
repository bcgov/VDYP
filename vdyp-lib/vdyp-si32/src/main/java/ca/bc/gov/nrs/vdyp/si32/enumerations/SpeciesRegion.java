package ca.bc.gov.nrs.vdyp.si32.enumerations;

import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;

/**
 * Enumerates each of the potential regions a species can be found in.
 * <ul>
 * <li>Coast
 * <li>Interior
 * </ul>
 */
public enum SpeciesRegion implements SI32Enum<SpeciesRegion> {

	COAST(0), //
	INTERIOR(1);

	private static Map<Integer, SpeciesRegion> index2EnumMap = null;

	private static synchronized Map<Integer, SpeciesRegion> getMappings() {
		if (index2EnumMap == null) {
			index2EnumMap = new HashMap<>();
		}
		return index2EnumMap;
	}

	private final int index;

	private SpeciesRegion(int index) {
		this.index = index;
		getMappings().put(index, this);
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getOffset() {
		return index;
	}

	@Override
	public String getText() {
		return this.toString();
	}

	/**
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static SpeciesRegion forIndex(int index) {
		return index2EnumMap.get(index);
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return INTERIOR.index - COAST.index + 1;
	}

	public static class Iterator extends EnumIterator<SpeciesRegion> {
		public Iterator() {
			super(values(), COAST, INTERIOR);
		}
	}
}
