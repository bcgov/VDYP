package ca.bc.gov.nrs.vdyp.si32.vdyp;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * An enumeration holding each of the valid SP0 Species known to VDYP7. The index value <b>must</b> match the index of
 * this species in {@code SpeciesTable}.
 * <p>
 * <b>UNKNOWN</b> represents an error condition or an uninitialized state. This value should not be used as a species
 * constant.
 */
public enum SP64Name implements SI32Enum<SP64Name> {
	UNKNOWN(0),

	A(1), //
	ABAL(2), //
	ABCO(3), //
	AC(4), //
	ACB(5), //
	ACT(6), //
	AD(7), //
	AH(8), //
	AT(9), //
	AX(10), //
	B(11), //
	BA(12), //
	BAC(13), //
	BAI(14), //
	BB(15), //
	BC(16), //
	BG(17), //
	BI(18), //
	BL(19), //
	BM(20), BN(21), //
	BP(22), //
	BV(23), //
	C(24), //
	CI(25), //
	COT(26), //
	CP(27), //
	CT(28), //
	CW(29), //
	CY(30), //
	D(31), //
	DF(32), //
	DG(33), //
	DM(34), //
	DR(35), //
	E(36), //
	EA(37), //
	EB(38), //
	EE(39), //
	EP(40), //
	ES(41), //
	EW(42), //
	EXP(43), //
	F(44), //
	FD(45), //
	FDC(46), //
	FDI(47), //
	G(48), //
	GP(49), //
	GR(50), //
	H(51), //
	HM(52), //
	HW(53), //
	HWC(54), //
	HWI(55), //
	HXM(56), //
	IG(57), //
	IS(58), //
	J(59), //
	JR(60), //
	K(61), //
	KC(62), //
	L(63), //
	LA(64), //
	LE(65), //
	LT(66), //
	LW(67), //
	M(68), //
	MB(69), //
	ME(70), //
	MN(71), //
	MR(72), //
	MS(73), //
	MV(74), //
	OA(75), //
	OB(76), //
	OC(77), //
	OD(78), //
	OE(79), //
	OF(80), //
	OG(81), //
	P(82), //
	PA(83), //
	PF(84), //
	PJ(85), //
	PL(86), //
	PLC(87), //
	PLI(88), //
	PM(89), //
	PR(90), //
	PS(91), //
	PV(92), //
	PW(93), //
	PXJ(94), //
	PY(95), //
	Q(96), //
	QE(97), //
	QG(98), //
	R(99), //
	RA(100), //
	S(101), //
	SA(102), //
	SB(103), //
	SE(104), //
	SI(105), //
	SN(106), //
	SS(107), //
	SW(108), //
	SX(109), //
	SXB(110), //
	SXE(111), //
	SXL(112), //
	SXS(113), //
	SXW(114), //
	SXX(115), //
	T(116), //
	TW(117), //
	U(118), //
	UA(119), //
	UP(120), //
	V(121), //
	VB(122), //
	VP(123), //
	VS(124), //
	VV(125), //
	W(126), //
	WA(127), //
	WB(128), //
	WD(129), //
	WI(130), //
	WP(131), //
	WS(132), //
	WT(133), //
	X(134), //
	XC(135), //
	XH(136), //
	Y(137), //
	YC(138), //
	YP(139), //
	Z(140), //
	ZC(141), //
	ZH(142);

	private final int index;

	private SP64Name(int index) {
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
							"Cannot call getIndex on {0} as it's not a standard member of the enumeration", this
					)
			);
		}

		return index - 1;
	}

	@Override
	public String getText() {
		if (this.equals(UNKNOWN)) {
			return "";
		}

		return this.toString();
	}

	/**
	 * Return the enumeration value corresponding to the given text. If none exists, {@code UNKNOWN} is returned.
	 *
	 * @param text the text value == the enumeration text minus the "" prefix.
	 * @return as described
	 */
	public static SP64Name forText(String text) {

		if (text != null) {
			try {
				return valueOf(SP64Name.class, text.toUpperCase());
			} catch (IllegalArgumentException e) {
				return UNKNOWN;
			}
		} else {
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
	public static SP64Name forIndex(int index) {
		for (SP64Name e : SP64Name.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return ZH.index - A.index + 1;
	}

	public static class Iterator extends EnumIterator<SP64Name> {
		public Iterator() {
			super(values(), A, ZH);
		}
	}
}
