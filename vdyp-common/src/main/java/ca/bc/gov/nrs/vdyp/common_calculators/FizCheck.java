package ca.bc.gov.nrs.vdyp.common_calculators;

/**
 * FizCheck.java - determines whether a given FIZ code represents the coast or interior.
 */
public class FizCheck {
/* @formatter:off */
 /*
 * 1994 oct 19 - Moved here from FredTab.
 * 1999 jan 8  - Changed int to short int.
 * 2023 jul 7  - Translated like for like from C to Java
 */
/* @formatter:on */

	// From sindex.h
	public static final short FIZ_UNKNOWN = 0;
	public static final short FIZ_COAST = 1;
	public static final short FIZ_INTERIOR = 2;

	public static short fiz_check(char fiz) {
		switch (fiz) {
		case 'A', 'B', 'C':
			return FIZ_COAST;
		case 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L':
			return FIZ_INTERIOR;
		default:
			return FIZ_UNKNOWN;
		}
	}
}
