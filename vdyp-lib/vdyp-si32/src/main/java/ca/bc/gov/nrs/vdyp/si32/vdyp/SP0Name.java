package ca.bc.gov.nrs.vdyp.si32.vdyp;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * An enumeration holding each of the valid SP0 genera known to VDYP7.
 */
public enum SP0Name implements SI32Enum<SP0Name> {
	UNKNOWN(-1),

	AC(0), //
	AT(1), //
	B(2), //
	C(3), //
	D(4), //
	E(5), //
	F(6), //
	H(7), //
	L(8), //
	MB(9), //
	PA(10), //
	PL(11), //
	PW(12), //
	PY(13), //
	S(14), //
	Y(15);

	private final int index;

	private SP0Name(int index) {
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
					MessageFormat.format(
							"Cannot call getOffset on {0} as it's not a standard member of the enumeration", this
					)
			);
		}

		return index;
	}

	@Override
	public String getText() {
		if (this.equals(UNKNOWN)) {
			return "";
		} else {
			return toString();
		}
	}

	/**
	 * Returns the enumeration constant corresponding to the given text value.
	 *
	 * @param text the text value in question. Example: "AC". The value is converted to upper-case before the lookup.
	 * @return the enumeration value, unless <code>text</code> is null or
	 */
	public static SP0Name forText(String text) {
		try {
			return valueOf("" + text.toUpperCase());
		} catch (IllegalArgumentException | NullPointerException e) {
			return UNKNOWN;
		}
	}

	/**
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static SP0Name forIndex(int index) {
		for (SP0Name e : SP0Name.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return Y.index - AC.index + 1;
	}

	public static class Iterator extends EnumIterator<SP0Name> {
		public Iterator() {
			super(values(), AC, Y);
		}
	}
}
