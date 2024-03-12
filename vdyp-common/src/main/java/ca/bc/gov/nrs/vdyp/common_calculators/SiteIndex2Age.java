package ca.bc.gov.nrs.vdyp.common_calculators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptTotalException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;

/* @formatter:off */
/**
 * SiteIndex2Age.java
 * - given site index and site height, computes age.
 *
 * @throws LessThan13Exception site index or height < 1.3m
 * @throws NoAnswerException iteration could not converge (or projected age > 999)
 * @throws CurveEroorException unknown curve index
 * @throws GrowthInterceptTotalException cannot compute growth intercept when using total age
 */
/* @formatter:on */
public class SiteIndex2Age {
/* @formatter:off */
/*
 * 1990 aug 15 - Created.
 * 1991 jan 15 - Added check for no convergence.
 *      jul 23 - Added code to compute age directly for Bruce's Fdc,
 *               Wiley's Hw, and Goudie's Pli, Ss, and Sw.
 *      dec 2  - Changed to independent Sindex functions.
 * 1992 jan 10 - Added defines for how function prototypes and definitions
 *               are handled.
 *      apr 29 - Noticed that Pli Goudie Dry was included, but Wet was not.
 *               Fixed.
 *               Removed difference between plantations and natural stands
 *               for Pli Goudie.
 * 1994 dec 6  - Added another check for getting stuck when iterating.
 * 1995 dec 19 - Added Hm.
 * 1996 jun 27 - Added error code of -4, instead of 999.
 *      jul 30 - Added check for incoming height or site < 1.3m.
 *      aug 1  - Refined iterating loop when growth intercept curves are used.
 *          8  - Changed error codes to defined constants.
 * 1997 jan 23 - Added special function to handle growth intercept.
 *      feb 5  - Changed check for top height or site index < 1.3 to be
 *               <= 1.3.
 *      mar 21 - Added Nigh's 1997 Hwi GI.
 *             - Changed define names: FDC_NIGH, HW_NIGH, PLI_NIGH, SW_NIGH
 *               all have "GI" added after them.
 *             - Added Nigh's 1997 Pl GI.
 *             - Added Nigh's 1997 Fdi GI.
 *          24 - Split HW into HWI and HWC.
 *      jul 8  - Replaced checking height <= 1.3 and returning error code
 *               with checking and returning 0 if age type is breast-height.
 *      sep 16 - Changed a "log(50)" to "log(50.0)" in Goudie formulation.
 *      nov 17 - Added Pf as Pli Goudie.
 *             - Added Se as Sw Goudie.
 * 1998 apr 7  - Added inclusion of sindex2.h.
 *      may 27 - If site height is <= 1.3 and age type is breast height,
 *               return value is SI_ERR_NO_ANS.
 * 1999 jan 8  - Changed int to short int.
 *      aug 20 - Changed iteration to always be to breast height age.
 *          24 - Added error count to ensure iterate loop doesn't run forever.
 *             - Added additional error checks in iterate loop.
 *          26 - Removed y2bh as parameter to gi_iterate().
 *      sep 24 - If an error occurs in iterating, don't convert age type.
 *             - If age is really tiny and total age, return 0.
 *      oct 1  - D'oh!  The aug 20 change to make iterating always be
 *               by breast height makes it impossible to iterate for
 *               heights below breast-height!  Trying total age now...
 * 2000 jan 27 - Added some missing GI cases.
 *      apr 25 - Added call to age_to_age() in iterate() when converting
 *               from total age to breast height age.
 *      jul 24 - Changed CW to CWI.
 *      oct 10 - Changed check for site <= 1.3 to < 1.3.
 * 2009 may 6  - Forced pure y2bh to be computed for Fdc-Bruce.
 *      apr 16 - Added 2010 Sw Hu and Garcia.
 * 2016 mar 9  - Added parameter to index_to_height().
 * 2023 jul 14  - Translated like for like from C to Java
 *              - Renamed from si2age.c to SiteIndex2Age
 *
 */
/* @formatter:on */

	// Taken from sindex.h
	/*
	 * age types
	 */
	private static final short SI_AT_TOTAL = 0;
	private static final short SI_AT_BREAST = 1;

	/*
	 * site index estimation (from height and age) types
	 */
	private static final int SI_EST_DIRECT = 1;

	/*
	 * error codes as return values from functions
	 */
	private static final int SI_ERR_GI_MAX = -3;
	private static final int SI_ERR_NO_ANS = -4;

	/* define species and equation indices */
	private static final int SI_BL_THROWERGI = 9;
	private static final int SI_CWI_NIGHGI = 84;
	private static final int SI_FDC_BRUCE = 16;
	private static final int SI_FDC_NIGHGI = 15;
	private static final int SI_FDI_NIGHGI = 19;
	private static final int SI_HWC_NIGHGI = 31;
	private static final int SI_HWC_NIGHGI99 = 79;
	private static final int SI_HWC_WILEY = 34;
	private static final int SI_HWI_NIGHGI = 38;
	private static final int SI_LW_NIGHGI = 82;
	private static final int SI_PLI_GOUDIE_DRY = 48;
	private static final int SI_PLI_GOUDIE_WET = 49;
	private static final int SI_PLI_NIGHGI97 = 42;
	private static final int SI_SS_GOUDIE = 60;
	private static final int SI_SS_NIGHGI = 58;
	private static final int SI_SS_NIGHGI99 = 80;
	private static final int SI_SW_GOUDIE_NAT = 71;
	private static final int SI_SW_GOUDIE_PLA = 70;
	private static final int SI_SW_HU_GARCIA = 119;
	private static final int SI_SW_NIGHGI = 63;
	private static final int SI_SW_NIGHGI99 = 81;

	/*
	 * #define TEST 1
	 */
	public static double ppow(double x, double y) {
		return (x <= 0) ? 0.0 : Math.pow(x, y);
	}

	public static double llog(double x) {
		return ( (x) <= 0.0) ? Math.log(.00001) : Math.log(x);
	}

	public static final double MAX_AGE = 999.0;

	/*
	 * Wrapped in directives which check if TEST is defined. The code that defines TEST as 1 is commented out, so I am
	 * assuming this wouldn't run
	 */
	public static final boolean TEST = false;

	/*
	 * #ifdef TEST File* testFile; they test before opening the file so removing the directives should be fine I have
	 * moved the file stuff into the functions where it happens #endif
	 */

	public static double
			index_to_age(short cu_index, double site_height, short age_type, double site_index, double y2bh) {
		double x1, x2, x3, x4;
		double a, b, c;
		/*
		 * I could not find HOOP, so I am assuming it is not intialized and as such the directives wouldn't trigger
		 */
		boolean HOOP = false;

		// #ifdef HOOP
		double ht5, ht10;
		// This is intialzied because otherwise it will cause issues later.
		// #endif
		double age;

		if (site_height < 1.3) {
			if (age_type == SI_AT_BREAST) {
				throw new LessThan13Exception("Site index or height < 1.3m, site height: " + site_height);
			}

			if (site_height <= 0.0001) {
				return 0;
			}
		}

		if (site_index < 1.3) {
			throw new LessThan13Exception("Site index or height < 1.3m, site_index: " + site_index);
		}

		switch (cu_index) {
		case SI_FDC_BRUCE:
			// 2009 may 6: force a non-rounded y2bh
			y2bh = 13.25 - site_index / 6.096;

			x1 = site_index / 30.48;
			x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));
			x3 = ppow(50.0 + y2bh, x2);
			double x4Denominator = ppow(y2bh, x2) - x3;
			if (x4Denominator == 0) {
				throw new ArithmeticException("Attempted Division by zero");
			}
			x4 = llog(1.372 / site_index) / x4Denominator;

			x1 = llog(site_height / site_index) / x4 + x3;

			if (x1 < 0) {
				age = SI_ERR_NO_ANS;
			} else {
				age = ppow(x1, 1 / x2);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		case SI_SW_HU_GARCIA: {
			double q;

			q = hu_garcia_q(site_index, 50.0);
			age = hu_garcia_bha(q, site_height);
			if (age_type == SI_AT_TOTAL) {
				age += y2bh;
			}
		}
			break;
		case SI_HWC_WILEY:
			if (site_height / 0.3048 < 4.5) {
				age = y2bh * ppow(site_height / 1.37, 0.5);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = 2500 / (site_index / 0.3048 - 4.5);
				x2 = -1.7307 + 0.1394 * x1;
				x3 = -0.0616 + 0.0137 * x1;
				x4 = 0.00192428 + 0.00007024 * x1;

				x1 = (4.5 - site_height / 0.3048);
				a = 1 + x1 * x4;
				b = x1 * x3;
				c = x1 * x2;

				x1 = ppow(b * b - 4 * a * c, 0.5);
				if (x1 == 0.0) {
					age = SI_ERR_NO_ANS;
				} else {
					age = (-b + x1) / (2 * a);

					if (age_type == SI_AT_TOTAL) {
						age += y2bh;
					}

					if (age < 0) {
						age = SI_ERR_NO_ANS;
					} else if (age > MAX_AGE) {
						age = SI_ERR_NO_ANS;
					}
				}
			}

			if (age < 10 && age > 0) {
				age = iterate(cu_index, site_height, age_type, site_index, y2bh);
				if (HOOP) {
					ht5 = SiteIndex2Height.index_to_height(cu_index, 5.0, SI_AT_BREAST, site_index, y2bh, 0.5); // 0.5
																												// may
																												// have
																												// to
																												// change

					if (site_height <= ht5) {
						site_height -= (1 - ( (ht5 - site_height) / ht5)) * 1.5;
					} else {
						ht10 = SiteIndex2Height.index_to_height(cu_index, 10.0, SI_AT_BREAST, site_index, y2bh, 0.5); // 0.5
																														// may
																														// have
																														// to
																														// change
						site_height -= ( ( (ht10 - site_height) / (ht10 - ht5))) * 1.5;
					}
				}
			}
			break;
		// Cannot find constant
		/*
		 * case SI_HM_WILEY: if (site_height / 0.3048 < 4.5){ age = y2bh * ppow (site_height / 1.37, 0.5);
		 *
		 * if (age_type == SI_AT_BREAST){ age -= y2bh; }
		 *
		 * if (age < 0.0){ age = 0.0; } } else{ x1 = 2500 / (site_index / 0.3048 - 4.5); x2 = -1.7307 + 0.1394 * x1; x3
		 * = -0.0616 + 0.0137 * x1; x4 = 0.00192428 + 0.00007024 * x1;
		 *
		 * x1 = (4.5 - site_height / 0.3048); a = 1 + x1 * x4; b = x1 * x3; c = x1 * x2;
		 *
		 * x1 = ppow (b * b - 4 * a * c, 0.5); if (x1 == 0.0){ age = SI_ERR_NO_ANS; } else{ age = (-b + x1) / (2 * a);
		 *
		 * if (age_type == SI_AT_TOTAL){ age += y2bh; }
		 *
		 * if (age < 0){ age = SI_ERR_NO_ANS; } else if (age > MAX_AGE){ age = SI_ERR_NO_ANS; } } }
		 *
		 * if (age < 10 && age > 0){ age = iterate (cu_index, site_height, age_type, site_index, y2bh); if(HOOP){ ht5 =
		 * index_to_height (cu_index, 5.0, SI_AT_BREAST, site_index, y2bh, 0.5); // 0.5 may have to change
		 *
		 * if (site_height <= ht5){ site_height -= (1 - ((ht5 - site_height) / ht5)) * 1.5; } else{ ht10 =
		 * index_to_height (cu_index, 10.0, SI_AT_BREAST, site_index, y2bh, 0.5); // 0.5 may have to change site_height
		 * -= (((ht10 - site_height) / (ht10 - ht5))) * 1.5; } } } break;
		 */
		case SI_PLI_GOUDIE_DRY:
			if (site_height < 1.3) {
				age = y2bh * ppow(site_height / 1.3, 0.5);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.00726;
				x2 = 7.81498;
				x3 = -1.28517;

				a = (site_index - 1.3) * (1 + Math.exp(x2 + x1 * llog(site_index - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(site_index - 1.3);

				age = Math.exp( (llog(a / (site_height - 1.3) - 1) - b) / x3);

				if (age_type == SI_AT_TOTAL) {
					age += y2bh;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		case SI_PLI_GOUDIE_WET:
			if (site_height < 1.3) {
				age = y2bh * ppow(site_height / 1.3, 0.5);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -0.935;
				x2 = 7.81498;
				x3 = -1.28517;

				a = (site_index - 1.3) * (1 + Math.exp(x2 + x1 * llog(site_index - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(site_index - 1.3);

				age = Math.exp( (llog(a / (site_height - 1.3) - 1) - b) / x3);

				if (age_type == SI_AT_TOTAL) {
					age += y2bh;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_PF_GOUDIE_DRY: if (site_height < 1.3){ age = y2bh * ppow (site_height / 1.3, 0.5);
		 *
		 * if (age_type == SI_AT_BREAST){ age -= y2bh; }
		 *
		 * if (age < 0.0){ age = 0.0; } } else{ x1 = -1.00726; x2 = 7.81498; x3 = -1.28517;
		 *
		 * a = (site_index - 1.3) * (1 + Math.exp (x2 + x1 * llog (site_index - 1.3) + x3 * Math.log(50.0))); b = x2 +
		 * x1 * llog (site_index - 1.3);
		 *
		 * age = Math.exp ((llog (a / (site_height - 1.3) - 1) - b) / x3);
		 *
		 * if (age_type == SI_AT_TOTAL){ age += y2bh; }
		 *
		 * if (age < 0){ age = 0; } else if (age > MAX_AGE){ age = SI_ERR_NO_ANS; } }
		 */
		// Couldn't find constant
		/*
		 * case SI_PF_GOUDIE_WET: if (site_height < 1.3){ age = y2bh * ppow (site_height / 1.3, 0.5);
		 *
		 * if (age_type == SI_AT_BREAST){ age -= y2bh; }
		 *
		 * if (age < 0.0){ age = 0.0; } } else{ x1 = -0.935; x2 = 7.81498; x3 = -1.28517;
		 *
		 * a = (site_index - 1.3) * (1 + Math.exp (x2 + x1 * llog (site_index - 1.3) + x3 * Math.log(50.0))); b = x2 +
		 * x1 * llog (site_index - 1.3);
		 *
		 * age = Math.exp ((llog (a / (site_height - 1.3) - 1) - b) / x3);
		 *
		 * if (age_type == SI_AT_TOTAL){ age += y2bh; }
		 *
		 * if (age < 0){ age = 0; } else if (age > MAX_AGE){ age = SI_ERR_NO_ANS; } }
		 */
		case SI_SS_GOUDIE:
			if (site_height < 1.3) {
				age = y2bh * ppow(site_height / 1.3, 0.5);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.5282;
				x2 = 11.0605;
				x3 = -1.5108;

				a = (site_index - 1.3) * (1 + Math.exp(x2 + x1 * llog(site_index - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(site_index - 1.3);

				age = Math.exp( (llog(a / (site_height - 1.3) - 1) - b) / x3);

				if (age_type == SI_AT_TOTAL) {
					age += y2bh;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_SE_GOUDIE_PLA: if (site_height < 1.3){ age = y2bh * ppow (site_height / 1.3, 0.5);
		 *
		 * if (age_type == SI_AT_BREAST){ age -= y2bh; }
		 *
		 * if (age < 0.0){ age = 0.0; } } else{ x1 = -1.2866; x2 = 9.7936; x3 = -1.4661;
		 *
		 * a = (site_index - 1.3) * (1 + Math.exp (x2 + x1 * llog (site_index - 1.3) + x3 * Math.log(50.0))); b = x2 +
		 * x1 * llog (site_index - 1.3);
		 *
		 * age = Math.exp ((llog (a / (site_height - 1.3) - 1) - b) / x3);
		 *
		 * if (age_type == SI_AT_TOTAL){ age += y2bh; }
		 *
		 * if (age < 0){ age = 0; } else if (age > MAX_AGE){ age = SI_ERR_NO_ANS; } }
		 */
		// Couldn't find constant
		/*
		 * case SI_SE_GOUDIE_NAT: if (site_height < 1.3){ age = y2bh * ppow (site_height / 1.3, 0.5);
		 *
		 * if (age_type == SI_AT_BREAST){ age -= y2bh; }
		 *
		 * if (age < 0.0){ age = 0.0; } } else{ x1 = -1.2866; x2 = 9.7936; x3 = -1.4661;
		 *
		 * a = (site_index - 1.3) * (1 + Math.exp (x2 + x1 * llog (site_index - 1.3) + x3 * Math.log(50.0))); b = x2 +
		 * x1 * llog (site_index - 1.3);
		 *
		 * age = Math.exp ((llog (a / (site_height - 1.3) - 1) - b) / x3);
		 *
		 * if (age_type == SI_AT_TOTAL){ age += y2bh; }
		 *
		 * if (age < 0){ age = 0; } else if (age > MAX_AGE){ age = SI_ERR_NO_ANS; } }
		 */
		case SI_SW_GOUDIE_PLA:
			if (site_height < 1.3) {
				age = y2bh * ppow(site_height / 1.3, 0.5);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				a = (site_index - 1.3) * (1 + Math.exp(x2 + x1 * llog(site_index - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(site_index - 1.3);

				age = Math.exp( (llog(a / (site_height - 1.3) - 1) - b) / x3);

				if (age_type == SI_AT_TOTAL) {
					age += y2bh;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;

		case SI_SW_GOUDIE_NAT:
			if (site_height < 1.3) {
				age = y2bh * ppow(site_height / 1.3, 0.5);

				if (age_type == SI_AT_BREAST) {
					age -= y2bh;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				a = (site_index - 1.3) * (1 + Math.exp(x2 + x1 * llog(site_index - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(site_index - 1.3);

				age = Math.exp( (llog(a / (site_height - 1.3) - 1) - b) / x3);

				if (age_type == SI_AT_TOTAL) {
					age += y2bh;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		case SI_BL_THROWERGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_CWI_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_FDC_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_FDI_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_HWC_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_HWC_NIGHGI99:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_HWI_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_LW_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		// Couldn't find constant
		/*
		 * case SI_PLI_NIGHGI: age = gi_iterate (cu_index, site_height, age_type, site_index); break;
		 */
		case SI_PLI_NIGHGI97:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_SS_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_SS_NIGHGI99:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_SW_NIGHGI:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;
		case SI_SW_NIGHGI99:
			age = gi_iterate(cu_index, site_height, age_type, site_index);
			break;

		default:
			if (TEST) {
				try {
					// Open the file for writing
					File testfile = new File("si2age.tst");

					try (FileWriter fileWriter = new FileWriter(testfile)) {

						// Write to the file
						fileWriter.write("before iterate()\n");

						// Close the file
						fileWriter.close();
					}
				} catch (IOException e) {
					throw new RuntimeException("An error occurred while writing to the file.", e);
				}
			}
			age = iterate(cu_index, site_height, age_type, site_index, y2bh);
			break;
		}

		if (TEST) {
			try {
				File testfile = new File("si2age.tst");

				// Open the file for writing
				try (FileWriter fileWriter = new FileWriter(testfile, true)) {

					// Write the final age to the file
					fileWriter.write("final age: " + age);

					// Close the file
					fileWriter.close();
				}
			} catch (IOException e) {
				throw new RuntimeException("An error occurred while writing to the file.", e);
			}
		}
		if (age == SI_ERR_NO_ANS) {
			throw new NoAnswerException("Iteration could not converge (or projected age > 999), age: " + age);
		}
		return (age);
	}

	public static double iterate(short cu_index, double site_height, short age_type, double site_index, double y2bh) {
		double si2age;
		double step;
		double test_ht;
		short err_count;

		/* initial guess */
		si2age = 25;
		step = si2age / 2;
		err_count = 0;

		/* do a preliminary test to catch some obvious errors */
		test_ht = SiteIndex2Height.index_to_height(cu_index, si2age, SI_AT_TOTAL, site_index, y2bh, 0.5); // 0.5 may
																											// have to
																											// change
		// This would throw an illegal argument exception and move up the stack

		/* loop until real close, or other end condition */
		do {
			if (TEST) {
				try {
					// Open the file for writing
					File testfile = new File("si2age.tst");

					try (FileWriter fileWriter = new FileWriter(testfile, true)) {

						// Write to the file
						fileWriter.write(
								String.format(
										"before index_to_height(age=%.2f, age_type=%d, site_index=%.2f, y2bh=%.2f)%n",
										si2age, age_type, site_index, y2bh
								)
						);

						// Close the file
						fileWriter.close();
					}
				} catch (IOException e) {
					throw new RuntimeException("An error occurred while writing to the file.", e);
				}
			}

			try {
				test_ht = SiteIndex2Height.index_to_height(cu_index, si2age, SI_AT_TOTAL, site_index, y2bh, 0.5); // 0.5
																													// may
																													// have
																													// to
																													// change

				if (TEST) {
					try {
						// Open the file for writing
						File testfile = new File("si2age.tst");
						try (FileWriter fileWriter = new FileWriter(testfile, true)) {

							// Write to the file
							fileWriter.write(String.format("index_to_height()=%.2f%n", test_ht));

							// Close the file
							fileWriter.close();
						}
					} catch (IOException e) {
						throw new RuntimeException("An error occurred while writing to the file.", e);
					}
				}
			} catch (NoAnswerException e) { /* height > 999 */
				/*
				 * printf ("si2age.c: site_height=%f, test_ht=%f, si2age=%f\n", site_height, test_ht, si2age);
				 */
				test_ht = 1000; /* should eventualy force an error code */
				err_count++;
				if (err_count == 100) {
					si2age = SI_ERR_NO_ANS;
					break;
				}
			}

			/* see if we're close enough */
			if ( (test_ht - site_height > 0.005) || (test_ht - site_height < -0.005)) {
				/* not close enough */
				if (test_ht > site_height) {
					if (step > 0) {
						step = -step / 2.0;
					}
				} else {
					if (step < 0) {
						step = -step / 2.0;
					}
				}
				si2age += step;
			} else {
				/* done */
				break;
			}

			/* check for lack of convergence, so we're not here forever */
			if (step < 0.00001 && step > -0.00001) {
				/* we have a value, but perhaps not too accurate */
				break;
			}
			if (si2age > 999.0) {
				si2age = SI_ERR_NO_ANS;
				if (TEST) {
					try {
						// Open the file for writing
						File testfile = new File("si2age.tst");
						try (FileWriter fileWriter = new FileWriter(testfile, true)) {

							// Write to the file
							fileWriter.write(String.format("Failed due to age too high (> 999).\n"));

							// Close the file
							fileWriter.close();
						}
					} catch (IOException e) {
						throw new RuntimeException("An error occurred while writing to the file.", e);
					}
				}
				break;
			}
		} while (true);

		if (si2age >= 0) {
			if (age_type == SI_AT_BREAST) {
				/*
				 * was si2age -= y2bh;
				 */
				si2age = Age2Age.age_to_age(cu_index, si2age, SI_AT_TOTAL, SI_AT_BREAST, y2bh);
			}
		}
		if (si2age == SI_ERR_NO_ANS) {
			throw new NoAnswerException(
					"Iteration could not converge (or projected age > 999)," + "site index 2 age variable: " + si2age
			);
		}
		return (si2age);
	}

	public static double gi_iterate(short cu_index, double site_height, short age_type, double site_index) {
		double age;
		double si2age;
		double test_site;
		double diff;
		double mindiff;

		if (age_type == SI_AT_TOTAL) {
			throw new GrowthInterceptTotalException(
					"cannot compute growth intercept when using total age, age type: " + age_type
			);
		}

		diff = 0;
		mindiff = 999;
		si2age = 1;

		for (age = 1; age < 100; age += 1) {
			if (TEST) {
				try {
					// Open the file for writing
					File testfile = new File("si2age.tst");
					try (FileWriter fileWriter = new FileWriter(testfile, true)) {

						// Write to the file
						fileWriter.write(
								String.format("before height_to_index(age=%f, site_height=%f)\n", age, site_height)
						);

						// Close the file
						fileWriter.close();
					}
				} catch (IOException e) {
					throw new RuntimeException("An error occurred while writing to the file.", e);
				}

			}
			test_site = Height2SiteIndex
					.height_to_index(cu_index, age, SI_AT_BREAST, site_height, (short) SI_EST_DIRECT);

			if (TEST) {
				try {
					// Open the file for writing
					File testfile = new File("si2age.tst");
					try (FileWriter fileWriter = new FileWriter(testfile, true)) {

						// Write to the file
						fileWriter.write(String.format("height_to_index()=%f\n", test_site));

						// Close the file
						fileWriter.close();
					}
				} catch (IOException e) {
					throw new RuntimeException("An error occurred while writing to the file.", e);
				}
			}

			if (test_site == SI_ERR_GI_MAX) {
				break;
			}

			if (test_site > site_index) {
				diff = test_site - site_index;
			} else {
				diff = site_index - test_site;
			}

			if (diff < mindiff) {
				mindiff = diff;
				si2age = age;
			}
		}

		if (si2age == 1) {
			/* right answer, or not low enough */
			if (diff > 1) {
				/* outside tolerance of 1m */
				throw new NoAnswerException(
						"Iiteration could not converge (or projected age > 999),"
								+ "difference outside of 1m tolerance: " + diff
				);
			}
		}

		else if (si2age == age - 1) {
			/* right answer, or not high enough */
			if (diff > 1) {
				/* outside tolerance of 1m */
				throw new NoAnswerException(
						"Iiteration could not converge (or projected age > 999),"
								+ "difference outside of 1m tolerance: " + diff
				);
			}
		}

		return si2age;
	}

	public static double hu_garcia_q(double site_index, double bhage) {
		double h, q, step, diff, lastdiff;

		q = 0.02;
		step = 0.01;
		lastdiff = 0;
		diff = 0;

		do {
			h = hu_garcia_h(q, bhage);
			lastdiff = diff;
			diff = site_index - h;
			if (diff > 0.0000001) {
				if (lastdiff < 0) {
					step = step / 2.0;
				}
				q += step;
			} else if (diff < -0.0000001) {
				if (lastdiff > 0) {
					step = step / 2.0;
				}
				q -= step;
				if (q <= 0) {
					q = 0.0000001;
				}
			} else {
				break;
			}
			if (step < 0.0000001) {
				break;
			}
		} while (true);

		return q;
	}

	public static double hu_garcia_h(double q, double bhage) {
		double a, height;

		a = 283.9 * Math.pow(q, 0.5137);
		height = a * Math.pow(1 - (1 - Math.pow(1.3 / a, 0.5829)) * Math.exp(-q * (bhage - 0.5)), 1.71556);
		return height;
	}

	public static double hu_garcia_bha(double q, double height) {
		double a, bhage;

		a = 283.9 * Math.pow(q, 0.5137);
		bhage = 0.5 - 1 / q * Math.log( (1 - Math.pow(height / a, 0.5829)) / (1 - Math.pow(1.3 / a, 0.5829)));
		return bhage;
	}

}
