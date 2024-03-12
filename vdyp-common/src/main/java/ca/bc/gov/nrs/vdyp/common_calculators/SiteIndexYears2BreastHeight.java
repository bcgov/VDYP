package ca.bc.gov.nrs.vdyp.common_calculators;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptTotalException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;

/* @formatter:off */
/**
 * siy2bh.c
 * - computes number of years from seed to breast height.
 * - returns unrounded real number.
 * - assumes "site_index" in metres, based on breast-height age 50.
 * - error codes (returned as y2bh value):
 *     SI_ERR_LT13: site index < 1.3m
 *     SI_ERR_CURVE: unknown curve index
 *     SI_ERR_GI_TOT: cannot use with GI equations
 *     SI_ERR_NO_ANS: site index out of range
 */
/* @formatter:on */
public class SiteIndexYears2BreastHeight {
/* @formatter:off */
/*
 * 1990 may 31
 *      jun 8  - Added Fdi Monserud's equations.
 *               Added At Goudie's equation.
 *          11 - Added Dr Harrington & Curtis' equation.
 *             - Added Bg Cochran's equation.
 *             - Added Bc Cochran's equation.
 *          15 - Changed most variable names, and file name, to include
 *               "si" at start.
 *      aug 29 - Added Pp Hann & Scrivani's equation.
 *               Added Lw Milner's equation.
 *      oct 22 - Changed Pli Goudie's equation from 7.591 to 3.591.
 *               Added some missing "break;" statements.
 *          23 - Changed Pli Goudie's equation to include 2 years from
 *               seed to seedling age.
 *          26 - Copied Pli Goudie's equation to Pp Hann and Lw Milner.
 *      dec 13 - Added Dempster's Sw, Sb, and Pli.
 *          17 - If incoming site index is 0, change it to a bit bigger.
 * 1991 jan 11 - Added Mb, Ea, Pw, Pa, and Yc, using other existing equations.
 *          16 - Added Fdc, Cwi and Fdi, by Hegyi.
 *          17 - Added Hw, Cw, and Ss Barker.
 *          23 - Split Goudie's Pli, Sw, Pw, and Pa into natural and
 *               plantation versions.
 *          24 - Added Hegyi's black cottonwood.
 *      feb 4  - Changed EA to EP.
 *          12 - Added Curtis' Pw.
 *          28 - Added ppow() and llog() macros to check for out-of-range
 *               conditions.
 *               Added a check for zero sites for Hegyi's curves.
 *               Changed FDI y2bh equations to from 1.7+103/site to 4+99/site.
 *      mar 15 - Removed Goudie's Pw.
 *      jun 19 - Split balsam into coast and interior.
 *               Changed y2bh function for LW.
 *          21 - Added Ker & Bowling's Bac, Sb, and Sw.
 *      oct 21 - Added Cieszewski & Bella's Pl.
 *      nov 6  - Added Cieszewski & Bella's Sw, Sb, and At.
 *      dec 2  - Changed to independent Sindex functions.
 *          6  - Changed y2bh for Goudie Pli :
 *               plantation: old: 2 + 3.592 + 42.637 / SI
 *                           new: 2.68 + 65.5 / SI
 *               natural: old: 5 + 3.592 + 42.637 / SI
 *                        new: 2 + 2.68 + 65.5 / SI
 * 1992 jan 10 - Added defines for how function prototypes and definitions are
 *               handled.
 *      feb 11 - Added Milner's Pp, Fdi, and Pli.
 *      apr 1  - Removed 2 years from y2bh functions for Ponderosa Pine.
 *          29 - Changed y2bh for Goudie Pli, back to his original, and
 *               applied it to plantations and natural stands:
 *                   y2bh = 2 + 3.6 + 42.64 / SI
 *               This equation is now used for Pli Milner, Pli Cieszewski,
 *               Pli Goudie, Pli Dempster, Pp Hann, Pp Milner, and Pa Goudie.
 *               Removed difference between plantations and natural stands
 *               for Pli Goudie and Pa Goudie.
 *      jun 10 - Updated all Sb equations, adding in 7 years to account for
 *               years from ground to stump height.
 *      dec 2  - Added Mario Dilucca's coastal balsam fir.
 * 1993 feb 4  - Added Thrower's draft height-age curve for lodgepole pine.
 *      mar 22 - Added Thrower's draft height-age curve for black cottonwood.
 *      sep 16 - Added Thrower's height-age curve for white spruce.
 * 1994 sep 27 - Copied Kurucz' 1982 Ba equation to Bg and Bc.
 * 1995 jun 12 - Added Goudie's Teak.
 *      sep 20 - Added Huang, Titus, & Lakusta equations for Sw, Pli, At,
 *               Sb, Pj, Acb, Bb, Fdi.
 *      oct 13 - Updated Thrower's Pli.
 *      nov 3  - Copied Ss Goudie to Ss Nigh.
 *          20 - Removed white fir.
 *      dec 19 - Added Hm.
 * 1996 jun 10 - Copied Fdc Bruce to Fdc Nigh.
 *             - Copied Hw Wiley to Hw Nigh.
 *             - Copied Pli Goudie to Pli Nigh.
 *             - Copied Ss Goudie to Ss Nigh(GI).
 *             - Copied Sw Goudie pla/nat to Sw Nigh pla/nat.
 *          27 - Added error codes.
 *      aug 2  - Added error code -9 to indicate not available for GI.
 *             - Amalgamated SI_SW_NIGH_PLA and SI_SW_NIGH_NAT into
 *               SI_SW_NIGH.
 *          8  - Changed error codes to defined constants.
 *          9  - Removed global check for y2bh < 0.
 *             - Added individual checks for y2bh < 1.
 *      oct 22 - Changed MB_HARRING to MB_THROWER.
 * 1997 feb 5  - Changed check for top height or site index < 1.3 to be
 *               <= 1.3.
 *      mar 21 - Added Nigh's 1997 Hwi GI.
 *             - Changed define names: FDC_NIGH, HW_NIGH, PLI_NIGH, SW_NIGH
 *               all have "GI" added after them.
 *             - Added Nigh's 1997 Hwi.
 *             - Added Nigh's 1997 Pl GI.
 *             - Added Nigh's 1997 Fdi GI.
 *          24 - Split HW into HWI and HWC.
 *      oct 28 - Added Thrower's Bl GI.
 *      nov 17 - Added Ea as At Goudie.
 *             - Added Lt and La as Lw Milner.
 *             - Added Pf as Pli Goudie.
 *             - Added Se as Sw Goudie.
 * 1998 apr 7  - Added inclusion of sindex2.h.
 *      nov 12 - Added Nigh & Courtin's 1998 Dr.
 *             - Added Nigh & Love's 1998 Pli.
 *          13 - Added hybrid Pli Thrower 1994 with Pli Nigh & Love 1998.
 * 1999 jan 8  - Changed int to short int.
 *      apr 15 - Added functions for Chen's curves.
 *      may 28 - Added Cameron's Ep.
 *      sep 24 - Added Curtis' Bp.
 *      oct 18 - Added Nigh's Hwc GI, SS GI, Sw GI, Lw GI.
 *             - Added Nigh/Love's Sw total age curve.
 * 2000 jan 27 - Added Nigh's Cw GI.
 *      jul 24 - Renamed CW to CWC or CWI.
 *          25 - Added spliced Sw Goudie/Nigh.
 *      oct 10 - Changed check for site <= 1.3 to < 1.3.
 *      nov 3  - Added Hm by Means/Campbell/Johnson.
 *      dec 12 - Renamed BAC_KER to BB_KER.
 * 2001 apr 9  - Added Fdc Nigh total age curve, and spliced with Bruce.
 *      may 3  - Added Lw curve by Brisco, Klinka, Nigh.
 * 2002 feb 12 - Added Sb Nigh.
 *      jun 27 - Major change to force output to be steps of 0.5, 1.5, etc.
 *      aug 21 - Changed numbers for Bruce-Nigh Fdc.
 *          26 - A final change to Bruce-Nigh Fdc, for sites below 6.6661.
 *      oct 9  - Added At Nigh.
 * 2003 jun 13 - Copied several curves and "corrected" the origin from
 *               bhage=0 ht=1.3 to bhage=0.5 ht=1.3.
 *               Added "AC" to the end of the define.
 * 2004 mar 26 - Added SI_SW_GOUDIE_NATAC.
 *      apr 28 - Added Nigh's 2002 Py.
 *             - Added Nigh's 2004 Pl/Sw/Se total age curves.
 *      may 4  - Added SI_SW_GOUDIE_PLAAC.
 * 2005 oct 20 - Added Huang's Pj.
 * 2008 feb 28 - Added 2004 Sw Nigh GI.
 * 2009 aug 28 - Added Nigh's 2009 Ep.
 * 2010 mar 4  - Added 2009 Ba Nigh GI.
 *             - Added 2009 Ba Nigh.
 *      apr 14 - Added 2010 Sw Hu and Garcia.
 *          15 - Changed return of error code to be Goudie's planted stand
 *               y2bh for SW_NIGHTA2004, SW_NIGHTA, SW_HU_GARCIA, SE_NIGHTA.
 * 2014 sep 2  - Added 2014 Se Nigh GI.
 * 2015 may 13 - Added 2015 Se Nigh.
 * 2016 mar 9  - Changed si_y2bh() to NOT round result,
 *               and added si_y2bh05() which DOES round the result
 *               into steps of 0.5, 1.5, 2.5, etc.
 * 2017 feb 2  - Added Nigh's 2016 Cwc.
 * 2023 jul 17 - Translated like for like from C to Java
 *             - Renamed from siy2bh to SiteIndexYears2BreastHeight
 */
/* @formatter:on */

	// Taken from sindex.h
	/* define species and equation indices */
	private static final int SI_ACB_HUANGAC = 97;
	private static final int SI_ACB_HUANG = 0;
	private static final int SI_ACT_THROWERAC = 103;
	private static final int SI_ACT_THROWER = 1;
	private static final int SI_AT_CHEN = 74;
	private static final int SI_AT_CIESZEWSKI = 3;
	private static final int SI_AT_GOUDIE = 4;
	private static final int SI_AT_HUANG = 2;
	private static final int SI_AT_NIGH = 92;
	private static final int SI_BA_DILUCCA = 5;
	private static final int SI_BA_KURUCZ82AC = 102;
	private static final int SI_BA_KURUCZ82 = 8;
	private static final int SI_BA_KURUCZ86 = 7;
	private static final int SI_BA_NIGHGI = 117;
	private static final int SI_BA_NIGH = 118;
	private static final int SI_BL_CHENAC = 93;
	private static final int SI_BL_CHEN = 73;
	private static final int SI_BL_KURUCZ82 = 10;
	private static final int SI_BL_THROWERGI = 9;
	private static final int SI_BP_CURTISAC = 94;
	private static final int SI_BP_CURTIS = 78;
	private static final int SI_CWC_BARKER = 12;
	private static final int SI_CWC_KURUCZAC = 101;
	private static final int SI_CWC_KURUCZ = 11;
	private static final int SI_CWC_NIGH = 122;
	private static final int SI_CWI_NIGH = 77;
	private static final int SI_CWI_NIGHGI = 84;
	private static final int SI_DR_HARRING = 14;
	private static final int SI_DR_NIGH = 13;
	private static final int SI_EP_NIGH = 116;
	private static final int SI_FDC_BRUCEAC = 100;
	private static final int SI_FDC_BRUCE = 16;
	private static final int SI_FDC_BRUCENIGH = 89;
	private static final int SI_FDC_COCHRAN = 17;
	private static final int SI_FDC_KING = 18;
	private static final int SI_FDC_NIGHGI = 15;
	private static final int SI_FDC_NIGHTA = 88;
	private static final int SI_FDI_HUANG_NAT = 21;
	private static final int SI_FDI_HUANG_PLA = 20;
	private static final int SI_FDI_MILNER = 22;
	private static final int SI_FDI_MONS_DF = 26;
	private static final int SI_FDI_MONS_GF = 27;
	private static final int SI_FDI_MONS_SAF = 30;
	private static final int SI_FDI_MONS_WH = 29;
	private static final int SI_FDI_MONS_WRC = 28;
	private static final int SI_FDI_NIGHGI = 19;
	private static final int SI_FDI_THROWERAC = 96;
	private static final int SI_FDI_THROWER = 23;
	private static final int SI_FDI_VDP_MONT = 24;
	private static final int SI_FDI_VDP_WASH = 25;
	private static final int SI_HM_MEANSAC = 95;
	private static final int SI_HM_MEANS = 86;
	private static final int SI_HWC_BARKER = 33;
	private static final int SI_HWC_FARR = 32;
	private static final int SI_HWC_NIGHGI = 31;
	private static final int SI_HWC_NIGHGI99 = 79;
	private static final int SI_HWC_WILEYAC = 99;
	private static final int SI_HWC_WILEY = 34;
	private static final int SI_HWC_WILEY_BC = 35;
	private static final int SI_HWC_WILEY_MB = 36;
	private static final int SI_HWI_NIGH = 37;
	private static final int SI_HWI_NIGHGI = 38;
	private static final int SI_LW_MILNER = 39;
	private static final int SI_LW_NIGH = 90;
	private static final int SI_LW_NIGHGI = 82;
	private static final int SI_PJ_HUANG = 113;
	private static final int SI_PJ_HUANGAC = 114;
	private static final int SI_PLI_CIESZEWSKI = 47;
	private static final int SI_PLI_DEMPSTER = 50;
	private static final int SI_PLI_GOUDIE_DRY = 48;
	private static final int SI_PLI_GOUDIE_WET = 49;
	private static final int SI_PLI_HUANG_NAT = 44;
	private static final int SI_PLI_HUANG_PLA = 43;
	private static final int SI_PLI_MILNER = 46;
	private static final int SI_PLI_NIGHGI97 = 42;
	private static final int SI_PLI_NIGHTA98 = 41;
	private static final int SI_PLI_THROWER = 45;
	private static final int SI_PLI_THROWNIGH = 40;
	private static final int SI_PL_CHEN = 76;
	private static final int SI_PW_CURTISAC = 98;
	private static final int SI_PW_CURTIS = 51;
	private static final int SI_PY_HANNAC = 104;
	private static final int SI_PY_HANN = 53;
	private static final int SI_PY_MILNER = 52;
	private static final int SI_PY_NIGH = 107;
	private static final int SI_PY_NIGHGI = 108;
	private static final int SI_SB_CIESZEWSKI = 55;
	private static final int SI_SB_DEMPSTER = 57;
	private static final int SI_SB_HUANG = 54;
	private static final int SI_SB_KER = 56;
	private static final int SI_SB_NIGH = 91;
	private static final int SI_SE_CHENAC = 105;
	private static final int SI_SE_CHEN = 87;
	private static final int SI_SE_NIGHGI = 120;
	private static final int SI_SE_NIGH = 121;
	private static final int SI_SS_BARKER = 62;
	private static final int SI_SS_FARR = 61;
	private static final int SI_SS_GOUDIE = 60;
	private static final int SI_SS_NIGH = 59;
	private static final int SI_SS_NIGHGI = 58;
	private static final int SI_SS_NIGHGI99 = 80;
	private static final int SI_SW_CIESZEWSKI = 67;
	private static final int SI_SW_DEMPSTER = 72;
	private static final int SI_SW_GOUDIE_NAT = 71;
	private static final int SI_SW_GOUDIE_NATAC = 106;
	private static final int SI_SW_GOUDIE_PLA = 70;
	private static final int SI_SW_GOUDIE_PLAAC = 112;
	private static final int SI_SW_GOUDNIGH = 85;
	private static final int SI_SW_HU_GARCIA = 119;
	private static final int SI_SW_HUANG_NAT = 65;
	private static final int SI_SW_HUANG_PLA = 64;
	private static final int SI_SW_KER_NAT = 69;
	private static final int SI_SW_KER_PLA = 68;
	private static final int SI_SW_NIGHGI = 63;
	private static final int SI_SW_NIGHGI99 = 81;
	private static final int SI_SW_NIGHGI2004 = 115;
	private static final int SI_SW_NIGHTA = 83;
	private static final int SI_SW_THROWER = 66;

	/* not used, but must be defined for array positioning */
	private static final int SI_BB_KER = 6;
	private static final int SI_DR_CHEN = 75;
	private static final int SI_PLI_NIGHTA2004 = 109;
	private static final int SI_SE_NIGHTA = 110;
	private static final int SI_SW_NIGHTA2004 = 111;

	public static double ppow(double x, double y) {
		return (x <= 0) ? 0.0 : Math.pow(x, y);
	}

	public static double llog(double x) {
		return ( (x) <= 0.0) ? Math.log(.00001) : Math.log(x);
	}

	public static double si_y2bh(short cu_index, double site_index) {
		double y2bh;
		double si20;

		if (site_index < 1.3) {
			throw new LessThan13Exception("Site index < 1.3m: " + site_index);
		}

		switch (cu_index) {
		case SI_FDC_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_FDC_NIGHGI" + cu_index);

		case SI_FDC_BRUCE:
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_FDC_BRUCEAC:
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_FDC_NIGHTA:
			if (site_index <= 9.051) {
				throw new NoAnswerException("Site index out of range, site index <= 9.051: " + site_index);
			} else {
				y2bh = 24.44 * Math.pow(site_index - 9.051, -0.394);
			}
			break;

		case SI_FDC_BRUCENIGH:
			/* changed 2002 AUG 20 */
			if (site_index <= 15) {
				y2bh = 13.25 - site_index / 6.096;
				if (y2bh < 1) { // TODO impossible to reach???
					y2bh = 1;
				}
			} else {
				y2bh = 36.5818 * Math.pow(site_index - 6.6661, -0.5526);
			}
			break;

		case SI_FDC_COCHRAN:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_FDC_KING:
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HWC_FARR:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HWC_BARKER:
			y2bh = -5.2 + 410.00 / site_index;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HM_MEANS:
			/* copied from Hw Wiley 1978 */
			/* seed (root collar) */
			y2bh = 9.43 - site_index / 7.088;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HM_MEANSAC:
			/* copied from Hw Wiley 1978 */
			/* seed (root collar) */
			y2bh = 9.43 - site_index / 7.088;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		// Couldn't find constant
		/*
		 * case SI_HM_WILEY: // copied from Hw Wiley 1978 // seed (root collar) y2bh = 9.43 - site_index / 7.088; if
		 * (y2bh < 1){ y2bh = 1; } break;
		 */

		case SI_HWI_NIGH:
			/* from seed */
			y2bh = 446.6 * ppow(site_index, -1.432);
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HWI_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_HWI_NIGHGI: " + cu_index);

		case SI_HWC_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_HWC_NIGHGI: " + cu_index);

		case SI_HWC_NIGHGI99:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_HWC_NIGHGI99: " + cu_index);

		case SI_SS_NIGHGI99:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SS_NIGHGI99: " + cu_index);

		case SI_SW_NIGHGI99:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SW_NIGHGI99: " + cu_index);

		case SI_SW_NIGHGI2004:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SW_NIGHGI2004: " + cu_index);

		case SI_LW_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_LW_NIGHGI: " + cu_index);

		case SI_HWC_WILEY:
			/* seed (root collar) */
			y2bh = 9.43 - site_index / 7.088;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HWC_WILEYAC:
			/* seed (root collar) */
			y2bh = 9.43 - site_index / 7.088;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HWC_WILEY_BC:
			/* seed (root collar) */
			y2bh = 9.43 - site_index / 7.088;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_HWC_WILEY_MB:
			/* seed (root collar) */
			y2bh = 9.43 - site_index / 7.088;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		// Couldn't find constant
		/*
		 * case SI_PF_GOUDIE_DRY: // copied from Pli Goudie // from seed y2bh = 2 + 3.6 + 42.64 / site_index; break;
		 */
		// Couldn't find constant
		/*
		 * case SI_PF_GOUDIE_WET: // copied from Pli Goudie // from seed y2bh = 2 + 3.6 + 42.64 / site_index; break;
		 */
		// Couldn't find constant
		/*
		 * case SI_PJ_HUANG_PLA: // from seed y2bh = 3.5 + 1.872138 + 49.555513 / site_index; break;
		 */
		// Couldn't find constant
		/*
		 * case SI_PJ_HUANG_NAT: // from seed y2bh = 5 + 1.872138 + 49.555513 / site_index; break;
		 */

		case SI_PJ_HUANG:
			/* from seed */
			y2bh = 5 + 1.872138 + 49.555513 / site_index;
			break;

		case SI_PJ_HUANGAC:
			/* from seed */
			y2bh = 5 + 1.872138 + 49.555513 / site_index;
			break;
		// Couldn't find constant
		/*
		 * case SI_PLI_NIGHGI: return SI_ERR_GI_TOT; break;
		 */

		case SI_PLI_NIGHGI97:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_PLI_NIGHGI97: " + cu_index);

		case SI_PLI_HUANG_PLA:
			/* from seed */
			y2bh = 3.5 + 1.740006 + 58.83891 / site_index;
			break;

		case SI_PLI_HUANG_NAT:
			/* from seed */
			y2bh = 5 + 1.740006 + 58.83891 / site_index;
			break;

		case SI_PLI_NIGHTA2004:
			/* temporarily copied from PLI_NIGHTA98 */
			if (site_index < 9.5) {
				throw new NoAnswerException("Site index out of range, site index < 9.5: " + site_index);
			} else {
				y2bh = 21.6623 * ppow(site_index - 9.05671, -0.550762);
			}
			break;

		case SI_PLI_NIGHTA98:
			if (site_index < 9.5) {
				throw new NoAnswerException("Site index out of range, site index < 9.5: " + site_index);
			} else {
				y2bh = 21.6623 * ppow(site_index - 9.05671, -0.550762);
			}
			break;

		case SI_SW_GOUDNIGH:
			if (site_index < 19.5) {
				/* Goudie plantation */
				y2bh = 2.0 + 2.1578 + 110.76 / site_index;
				/* smooth transition to Nigh curve */
				if (y2bh < 10.45) {
					y2bh = 10.45;
				}
			} else {
				/* Nigh */
				y2bh = 35.87 * ppow(site_index - 9.726, -0.5409);
			}
			break;

		case SI_SW_NIGHTA2004:
			/* temporarily copied from SW_NIGHTA */
			if (site_index < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			} else {
				y2bh = 35.87 * ppow(site_index - 9.726, -0.5409);
			}
			break;

		case SI_SW_HU_GARCIA:
			/* temporarily copied from SW_NIGHTA */
			if (site_index < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			} else {
				y2bh = 35.87 * ppow(site_index - 9.726, -0.5409);
			}
			break;

		case SI_SW_NIGHTA:
			if (site_index < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			} else {
				y2bh = 35.87 * ppow(site_index - 9.726, -0.5409);
			}
			break;

		case SI_SE_NIGH:
			/* copied from SW_GOUDIE_NATAC */
			y2bh = 6.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SE_NIGHTA:
			/* copied from SW_NIGHTA */
			if (site_index < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			} else {
				y2bh = 35.87 * ppow(site_index - 9.726, -0.5409);
			}
			break;

		case SI_SE_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SE_NIGHGI: " + cu_index);

		case SI_PLI_THROWNIGH:
			if (site_index < 18.5) {
				/* Thrower Pli */
				y2bh = 2 + 0.55 + 69.4 / site_index;
			} else {
				/* Nigh Pli */
				y2bh = 21.6623 * ppow(site_index - 9.05671, -0.550762);
			}
			/*
			 * slightly older version y2bh = 22.41028 * ppow (site_index - 8.90585, -0.5614);
			 */
			break;

		case SI_PLI_THROWER:
			/* from seed */
			y2bh = 2 + 0.55 + 69.4 / site_index;
			break;

		case SI_PLI_MILNER:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PLI_CIESZEWSKI:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PLI_GOUDIE_DRY:
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PLI_GOUDIE_WET:
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PLI_DEMPSTER:
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PL_CHEN:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_SE_CHEN:
			/* copied from Sw Goudie (natural) */
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SE_CHENAC:
			/* copied from Sw Goudie (natural) */
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / site_index;
			break;

		// Couldn't find constant\
		/*
		 * case SI_SE_GOUDIE_PLA: // copied from Sw Goudie // from seed y2bh = 2.0 + 2.1578 + 110.76 / site_index;
		 * break;
		 */

		// Couldn't find constant
		/*
		 * case SI_SE_GOUDIE_NAT: // copied from Sw Goudie // from seed y2bh = 6.0 + 2.1578 + 110.76 / site_index;
		 * break;
		 */

		case SI_SW_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SW_NIGHGI: " + cu_index);

		case SI_SW_HUANG_PLA:
			/* from seed */
			y2bh = 4.5 + 4.3473 + 59.908359 / site_index;
			break;

		case SI_SW_HUANG_NAT:
			/* from seed */
			y2bh = 8 + 4.3473 + 59.908359 / site_index;
			break;

		case SI_SW_THROWER:
			/* from seed */
			y2bh = 4 + 0.38 + 117.34 / site_index;
			break;

		case SI_SW_KER_PLA:
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_KER_NAT:
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_GOUDIE_PLA:
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_GOUDIE_NAT:
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_GOUDIE_PLAAC:
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_GOUDIE_NATAC:
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_DEMPSTER:
			y2bh = 2.1578 + 110.76 / site_index;
			break;

		case SI_SW_CIESZEWSKI:
			/*
			 * borrowed from Sw Goudie, plantation
			 */
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_SB_HUANG:
			/* from seed */
			y2bh = 8 + 2.288325 + 80.774008 / site_index;
			break;

		case SI_SB_KER:
			/*
			 * borrowed from Sb Dempster, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / site_index;
			break;

		case SI_SB_DEMPSTER:
			/*
			 * estimate of 7 years from ground to stump, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / site_index;
			break;

		case SI_SB_NIGH:
			/*
			 * estimate of 7 years from ground to stump, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / site_index;
			break;

		case SI_SB_CIESZEWSKI:
			/*
			 * borrowed from Sb Dempster, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / site_index;
			break;

		case SI_SS_GOUDIE:
			/* from seed */
			y2bh = 11.7 - site_index / 5.4054;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_SS_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SS_NIGHGI: " + cu_index);

		case SI_SS_NIGH:
			/* copied from Ss Goudie */
			/* from seed */
			y2bh = 11.7 - site_index / 5.4054;
			if (y2bh < 1)
				y2bh = 1;
			break;

		case SI_SS_FARR:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1)
				y2bh = 1;
			break;

		case SI_SS_BARKER:
			y2bh = -5.13 + 450.00 / site_index;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_CWI_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_CWI_NIGHGI: " + cu_index);

		case SI_CWI_NIGH:
			/* from seed */
			y2bh = 18.18 - 0.5526 * site_index;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_CWC_KURUCZ:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1)
				y2bh = 1;
			break;

		case SI_CWC_KURUCZAC:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_CWC_BARKER:
			y2bh = -3.46 + 285.00 / site_index;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_CWC_NIGH:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = 13.25 - site_index / 6.096;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_BA_DILUCCA:
			/*
			 * copied from BAC_KURUCZ86
			 */
			/* from seed */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BB_KER:
			/* from seed */
			y2bh = 18.47373 - site_index / 2.447;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BP_CURTIS:
			/* from seed */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BP_CURTISAC:
			/* copied from BP_CURTIS */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BA_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_BA_NIGHGI: " + cu_index);

		case SI_BA_NIGH:
			/*
			 * copied from BAC_KURUCZ86
			 */
			/* from seed */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BA_KURUCZ86:
			/* from seed */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BA_KURUCZ82:
			/* from seed */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BA_KURUCZ82AC:
			/* from seed */
			y2bh = 18.47373 - 0.4086 * site_index;
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BL_CHEN:
			/*
			 * Copied from Bl Kurucz82 (Thrower)
			 */
			/* from seed (root collar) */
			y2bh = 42.25 - 10.66 * llog(site_index);
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BL_CHENAC:
			/*
			 * Copied from Bl Kurucz82 (Thrower)
			 */
			/* from seed (root collar) */
			y2bh = 42.25 - 10.66 * llog(site_index);
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;

		case SI_BL_THROWERGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_BL_THROWERGI: " + cu_index);

		case SI_BL_KURUCZ82:
			/*
			 * From Jim Thrower, 1991 Jun 19
			 */
			/* from seed (root collar) */
			y2bh = 42.25 - 10.66 * llog(site_index);
			if (y2bh < 5.0) {
				y2bh = 5.0;
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_BC_KURUCZ82: //Copied from Bl Kurucz82 (Thrower) // from seed (root collar) y2bh = 42.25 - 10.66 *
		 * llog (site_index); if (y2bh < 5.0){ y2bh = 5.0; } break;
		 */
		// Couldn't find constant
		/*
		 * case SI_BG_KURUCZ82: //Copied from Bl Kurucz82 (Thrower) // from seed (root collar) y2bh = 42.25 - 10.66 *
		 * llog (site_index); if (y2bh < 5.0){ y2bh = 5.0; } break;
		 */

		case SI_FDI_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_FDI_NIGHGI: " + cu_index);

		case SI_FDI_HUANG_PLA:
			/* from seed */
			y2bh = 6.5 + 5.276585 + 38.968242 / site_index;
			break;

		case SI_FDI_HUANG_NAT:
			/* from seed */
			y2bh = 8 + 5.276585 + 38.968242 / site_index;
			break;

		case SI_FDI_MILNER:
			/*
			 * copied from Fdi Thrower
			 */
			/* from seed */
			y2bh = 4.0 + 99.0 / site_index;
			break;

		case SI_FDI_THROWER:
			/* from seed */
			y2bh = 4.0 + 99.0 / site_index;
			break;

		case SI_FDI_THROWERAC:
			/* copied from FDI_THROWER */
			/* from seed */
			y2bh = 4.0 + 99.0 / site_index;
			break;

		case SI_FDI_VDP_MONT:
			/* from seed */
			y2bh = 4.0 + 99.0 / site_index;
			break;

		case SI_FDI_VDP_WASH:
			/* from seed */
			y2bh = 4.0 + 99.0 / site_index;
			break;

		case SI_FDI_MONS_DF:
			y2bh = 16.0 - site_index / 3.0;
			if (y2bh < 8.0) {
				y2bh = 8.0;
			}
			break;

		case SI_FDI_MONS_GF:
			y2bh = 16.0 - site_index / 3.0;
			if (y2bh < 8.0) {
				y2bh = 8.0;
			}
			break;

		case SI_FDI_MONS_WRC:
			y2bh = 16.0 - site_index / 3.0;
			if (y2bh < 8.0) {
				y2bh = 8.0;
			}
			break;

		case SI_FDI_MONS_WH:
			y2bh = 16.0 - site_index / 3.0;
			if (y2bh < 8.0) {
				y2bh = 8.0;
			}
			break;

		case SI_FDI_MONS_SAF:
			y2bh = 16.0 - site_index / 3.0;
			if (y2bh < 8.0) {
				y2bh = 8.0;
			}
			break;

		case SI_AT_NIGH:
			/*
			 * equation copied from At Goudie
			 */
			y2bh = 1.331 + 38.56 / site_index;
			break;

		case SI_AT_CHEN:
			/*
			 * equation copied from At Goudie
			 */
			y2bh = 1.331 + 38.56 / site_index;
			break;

		case SI_AT_HUANG:
			/* from seed */
			y2bh = 1 + 2.184066 + 50.788746 / site_index;
			break;

		case SI_AT_GOUDIE:
			y2bh = 1.331 + 38.56 / site_index;
			break;

		case SI_ACB_HUANG:
			/* from seed */
			y2bh = 1 - 1.196472 + 104.124205 / site_index;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_ACB_HUANGAC:
			/* copied from ACB_HUANG */
			/* from seed */
			y2bh = 1 - 1.196472 + 104.124205 / site_index;
			if (y2bh < 1) {
				y2bh = 1;
			}
			break;

		case SI_ACT_THROWER:
			y2bh = 2;
			break;

		case SI_ACT_THROWERAC:
			y2bh = 2;
			break;
		// Couldn't find constant
		/*
		 * case SI_MB_THROWER: // copied from ACT_THROWER y2bh = 2; break;
		 */

		case SI_AT_CIESZEWSKI:
			/*
			 * borrowed from At Goudie
			 */
			y2bh = 1.331 + 38.56 / site_index;
			break;

		case SI_DR_HARRING:
			si20 = ppow(site_index, 1.5) / 8.0;
			if (si20 >= 15) {
				y2bh = 1.0;
			} else {
				y2bh = 2.0;
			}
			break;

		case SI_DR_CHEN:
			/* copied from Dr Harrington */
			si20 = ppow(site_index, 1.5) / 8.0;
			if (si20 >= 15) {
				y2bh = 1.0;
			} else
				y2bh = 2.0;
			break;

		case SI_DR_NIGH: {
			double si25;

			si25 = 0.3094 + 0.7616 * site_index;
			if (si25 <= 25) {
				y2bh = 5.494 - 0.1789 * si25;
			} else {
				y2bh = 1.0;
			}
		}
			break;

		// Couldn't find constant
		/*
		 * case SI_BB_HUANG: // from seed y2bh = 8 + 8.299433 + 59.302950 / site_index; break;
		 */

		// Couldn't find constant
		/*
		 * case SI_BG_COCHRAN: y2bh = 1.331 + 11.75 / site_index; break;
		 */

		case SI_PY_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_PY_NIGHGI: " + cu_index);

		case SI_PY_NIGH:
			y2bh = 36.35 * Math.pow(0.9318, site_index);
			break;

		case SI_PY_HANN:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PY_HANNAC:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		case SI_PY_MILNER:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / site_index;
			break;

		// Couldn't find constant
		/*
		 * case SI_LA_MILNER: // Copied from Lw Milner. //from seed (root collar) y2bh = 3.36 + 87.18 / site_index;
		 * break;
		 */

		// Couldn't find constant
		/*
		 * case SI_LT_MILNER: // Copied from Lw Milner. //from seed (root collar) y2bh = 3.36 + 87.18 / site_index;
		 * break;
		 */

		case SI_LW_MILNER:
			/*
			 * From Jim Thrower, 1991 Jun 19
			 */
			/* from seed (root collar) */
			y2bh = 3.36 + 87.18 / site_index;
			break;

		case SI_LW_NIGH:
			/*
			 * Copied from Lw Milner.
			 */
			/* from seed (root collar) */
			y2bh = 3.36 + 87.18 / site_index;
			break;

		// Couldn't find constant
		/*
		 * case SI_EA_GOUDIE: // equation copied from At Goudie
		 *
		 * y2bh = 1.331 + 38.56 / site_index; break;
		 */

		// Couldn't find constant
		/*
		 * case SI_EP_CAMERON: y2bh = 4; break;
		 */

		// Couldn't find constant
		/*
		 * case SI_EP_CHEN: // equation copied from At Goudie y2bh = 1.331 + 38.56 / site_index; break;
		 */

		// Couldn't find constant
		/*
		 * case SI_EP_GOUDIE: //equation copied from At Goudie y2bh = 1.331 + 38.56 / site_index; break;
		 */

		case SI_EP_NIGH:
			/*
			 * equation copied from At Goudie
			 */
			y2bh = 1.331 + 38.56 / site_index;
			break;

		case SI_PW_CURTIS:
			/*
			 * equation copied from Sw, plantation
			 */
			y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			break;

		case SI_PW_CURTISAC:
			/*
			 * equation copied from Sw, plantation
			 */
			y2bh = 2.0 + 2.1578 + 110.76 / site_index;
			break;

		// Couldn't find constant
		/*
		 * case SI_PA_GOUDIE_DRY: //copied from Pli Goudie // from seed y2bh = 2 + 3.6 + 42.64 / site_index; break;
		 */

		// Couldn't find constant
		/*
		 * case SI_PA_GOUDIE_WET: //copied from Pli Goudie // from seed y2bh = 2 + 3.6 + 42.64 / site_index; break;
		 */

		// Couldn't find constant
		/*
		 * case SI_YC_KURUCZ: // equation copied from Cw //approximate function, borrowed from Fdc Bruce 1981 // from
		 * seed y2bh = 13.25 - site_index / 6.096; if (y2bh < 1){ y2bh = 1; } break;
		 */

		// Couldn't find constant
		/*
		 * case SI_TE_GOUDIE: y2bh = 5.063 - 0.1797 * site_index; if (y2bh < 1){ y2bh = 1; } break;
		 */

		default:
			throw new CurveErrorException("Unknown curve index");
		}

		return y2bh;
	}

	public static double si_y2bh05(short cu_index, double site_index) {
		double y2bh;

		y2bh = si_y2bh(cu_index, site_index);

		/* force answer to be in steps 0.5, 1.5, 2.5, etc. */
		return ((int) y2bh) + 0.5;
	}

}
