package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Lists the indices for CFS Genus for looking up CFS Conversion Params at the Genus level.
 * <p>
 * These values are used as an index into the {@link CfsBiomassConversionCoefficientsForGenus} array.
 */
public enum CfsBiomassConversionSupportedGenera implements SI32Enum<CfsBiomassConversionSupportedGenera> {
	INVALID(-1),

	/* 0 */
	AC(0),
	/* 1 */
	B(1),
	/* 2 */
	C(2),
	/* 3 */
	D(3),
	/* 4 */
	E(4),
	/* 5 */
	F(5),
	/* 6 */
	G(6),
	/* 7 */
	H(7),
	/* 8 */
	L(8),
	/* 9 */
	M(9),
	/* 10 */
	PL(10),
	/* 11 */
	Q(11),
	/* 12 */
	R(12),
	/* 13 */
	S(13),
	/* 14 */
	U(14),
	/* 15 */
	V(15),
	/* 16 */
	W(16),
	/* 17 */
	XH(17),
	/* 18 */
	ZC(18),
	/* 19 */
	ZH(19);

	private final int index;

	private CfsBiomassConversionSupportedGenera(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getOffset() {
		if (this.equals(INVALID)) {
			throw new UnsupportedOperationException(
					MessageFormat
							.format("Cannot call getIndex on {} as it's not a standard member of the enumeration", this)
			);
		}

		return index;
	}

	@Override
	public String getText() {
		if (this.equals(INVALID)) {
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
	public static CfsBiomassConversionSupportedGenera forIndex(int index) {
		for (CfsBiomassConversionSupportedGenera e : CfsBiomassConversionSupportedGenera.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return ZH.index - AC.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsBiomassConversionSupportedGenera> {
		public Iterator() {
			super(values(), AC, ZH);
		}
	}
}
