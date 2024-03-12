package ca.bc.gov.nrs.vdyp.common_calculators;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CodeErrorException;

/* @formatter:off */
/**
 * SpecRMap.java
 * - determines the default species/curve index for a given species code.
 * - initial species code remappings provided by Inventory Branch.
 * - species codes can be 1-3 letters, in upper or lower case.
 */
/* @formatter:on */

public class SpecRMap {
/* @formatter:off */
/*
 * 1994 oct 19 - Moved here from FredTab.
 * 1996 jun 3  - Changed remap of YC to CW.
 *             - Changed remap of EP to AT.
 *             - Changed remap of MB to DR.
 *             - Changed remap of PA to PLI.
 *             - Changed SS_GOUDIE to SS_NIGH as default curve for Ss.
 *          27 - Changed error code of -1 to -8.
 *             - Changed error code of -2 to -7.
 *      aug 9  - Changed error codes to defined constants.
 * 1997 mar 24 - Split HW into HWC and HWI.
 *             - Added Nigh's Hwi.
 *      aug 27 - Added conditional compilation around HWI_NIGH.
 *      nov 17 - Changed mapping of PJ from PLI_GOUDIE_DRY to PJ_HUANG_NAT.
 *          21 - Changed mapping of PJ back to PLI_GOUDIE_DRY.
 *          26 - Changed remapping of MB from DR to ACT.
 * 1998 sep 17 - Added some ifdefs.
 *             - Changed code to allow checking 3-letter codes, and allow
 *               lower case.
 * 1999 jan 8  - Changed int to short int.
 *             - Changed to return species index, not curve index.
 *      may 31 - Split Ac into Acb and Act.
 *      sep 24 - Added Bp.
 * 2000 jul 24 - Split Cw into Cwc and Cwi.
 *      oct 10 - Implemented Cwc/Cwi.
 *      nov 3  - Changed remap of Hm -> Hwc to stay Hm.
 * 2001 jan 4  - If "CW" was entered, a bug made it always return "ACT". Fixed.
 *             - If "C" was entered, "CWC" was always returned. Fixed.
 *          17 - Changed mapping of SE.
 *      mar 14 - Bug fix in 'S'. A 'break;' was missing, causing any
 *               single letter 'S' to fall through to the SB case.
 * 2002 jan 30 - Added vdyp_species_remap().
 *      feb 8  - Added a few more cases to vdyp_species_remap().
 *          21 - Bug fixes and additions to vdyp_species_remap().
 *      mar 7  - Bug fixes and additions to vdyp_species_remap().
 *          25 - Removed vdyp_species_remap().
 *             - Expanded and limited species_remap().
 *      jun 21 - Changed dynamic sc2[] to static array.
 *      nov 29 - Added species_map().
 * 2003 jan 16 - Added Hwc, Hwi, Cwc, Cwi, Pli to species_map().
 *      aug 7  - Added 40 more species.
 *      sep 11 - Added Fd, Pl, Hw, Cw.
 * 2005 oct 20 - Changed PJ mapping from PLI to PJ.
 * 2009 aug 18 - Changed E* remaps from At to Ep.
 * 2015 apr 9  - Removed species code "Bv".
 */
/* @formatter:on */
	// Taken from sindex.h
	/* define species and equation indices */
	private static final int SI_SPEC_A = 0;
	private static final int SI_SPEC_ABAL = 1;
	private static final int SI_SPEC_ABCO = 2;
	private static final int SI_SPEC_AC = 3;
	private static final int SI_SPEC_ACB = 4;
	private static final int SI_SPEC_ACT = 5;
	private static final int SI_SPEC_AD = 6;
	private static final int SI_SPEC_AH = 7;
	private static final int SI_SPEC_AT = 8;
	private static final int SI_SPEC_AX = 9;
	private static final int SI_SPEC_B = 10;
	private static final int SI_SPEC_BA = 11;
	private static final int SI_SPEC_BB = 12;
	private static final int SI_SPEC_BC = 13;
	private static final int SI_SPEC_BG = 14;
	private static final int SI_SPEC_BI = 15;
	private static final int SI_SPEC_BL = 16;
	private static final int SI_SPEC_BM = 17;
	private static final int SI_SPEC_BP = 18;
	private static final int SI_SPEC_C = 19;
	private static final int SI_SPEC_CI = 20;
	private static final int SI_SPEC_CP = 21;
	private static final int SI_SPEC_CW = 22;
	private static final int SI_SPEC_CWC = 23;
	private static final int SI_SPEC_CWI = 24;
	private static final int SI_SPEC_CY = 25;
	private static final int SI_SPEC_D = 26;
	private static final int SI_SPEC_DG = 27;
	private static final int SI_SPEC_DM = 28;
	private static final int SI_SPEC_DR = 29;
	private static final int SI_SPEC_E = 30;
	private static final int SI_SPEC_EA = 31;
	private static final int SI_SPEC_EB = 32;
	private static final int SI_SPEC_EE = 33;
	private static final int SI_SPEC_EP = 34;
	private static final int SI_SPEC_ES = 35;
	private static final int SI_SPEC_EW = 36;
	private static final int SI_SPEC_EXP = 37;
	private static final int SI_SPEC_FD = 38;
	private static final int SI_SPEC_FDC = 39;
	private static final int SI_SPEC_FDI = 40;
	private static final int SI_SPEC_G = 41;
	private static final int SI_SPEC_GP = 42;
	private static final int SI_SPEC_GR = 43;
	private static final int SI_SPEC_H = 44;
	private static final int SI_SPEC_HM = 45;
	private static final int SI_SPEC_HW = 46;
	private static final int SI_SPEC_HWC = 47;
	private static final int SI_SPEC_HWI = 48;
	private static final int SI_SPEC_HXM = 49;
	private static final int SI_SPEC_IG = 50;
	private static final int SI_SPEC_IS = 51;
	private static final int SI_SPEC_J = 52;
	private static final int SI_SPEC_JR = 53;
	private static final int SI_SPEC_K = 54;
	private static final int SI_SPEC_KC = 55;
	private static final int SI_SPEC_L = 56;
	private static final int SI_SPEC_LA = 57;
	private static final int SI_SPEC_LE = 58;
	private static final int SI_SPEC_LT = 59;
	private static final int SI_SPEC_LW = 60;
	private static final int SI_SPEC_M = 61;
	private static final int SI_SPEC_MB = 62;
	private static final int SI_SPEC_ME = 63;
	private static final int SI_SPEC_MN = 64;
	private static final int SI_SPEC_MR = 65;
	private static final int SI_SPEC_MS = 66;
	private static final int SI_SPEC_MV = 67;
	private static final int SI_SPEC_OA = 68;
	private static final int SI_SPEC_OB = 69;
	private static final int SI_SPEC_OC = 70;
	private static final int SI_SPEC_OD = 71;
	private static final int SI_SPEC_OE = 72;
	private static final int SI_SPEC_OF = 73;
	private static final int SI_SPEC_OG = 74;
	private static final int SI_SPEC_P = 75;
	private static final int SI_SPEC_PA = 76;
	private static final int SI_SPEC_PF = 77;
	private static final int SI_SPEC_PJ = 78;
	private static final int SI_SPEC_PL = 79;
	private static final int SI_SPEC_PLC = 80;
	private static final int SI_SPEC_PLI = 81;
	private static final int SI_SPEC_PM = 82;
	private static final int SI_SPEC_PR = 83;
	private static final int SI_SPEC_PS = 84;
	private static final int SI_SPEC_PW = 85;
	private static final int SI_SPEC_PXJ = 86;
	private static final int SI_SPEC_PY = 87;
	private static final int SI_SPEC_Q = 88;
	private static final int SI_SPEC_QE = 89;
	private static final int SI_SPEC_QG = 90;
	private static final int SI_SPEC_R = 91;
	private static final int SI_SPEC_RA = 92;
	private static final int SI_SPEC_S = 93;
	private static final int SI_SPEC_SA = 94;
	private static final int SI_SPEC_SB = 95;
	private static final int SI_SPEC_SE = 96;
	private static final int SI_SPEC_SI = 97;
	private static final int SI_SPEC_SN = 98;
	private static final int SI_SPEC_SS = 99;
	private static final int SI_SPEC_SW = 100;
	private static final int SI_SPEC_SX = 101;
	private static final int SI_SPEC_SXB = 102;
	private static final int SI_SPEC_SXE = 103;
	private static final int SI_SPEC_SXL = 104;
	private static final int SI_SPEC_SXS = 105;
	private static final int SI_SPEC_SXW = 106;
	private static final int SI_SPEC_SXX = 107;
	private static final int SI_SPEC_T = 108;
	private static final int SI_SPEC_TW = 109;
	private static final int SI_SPEC_U = 110;
	private static final int SI_SPEC_UA = 111;
	private static final int SI_SPEC_UP = 112;
	private static final int SI_SPEC_V = 113;
	private static final int SI_SPEC_VB = 114;
	private static final int SI_SPEC_VP = 115;
	private static final int SI_SPEC_VS = 116;
	private static final int SI_SPEC_VV = 117;
	private static final int SI_SPEC_W = 118;
	private static final int SI_SPEC_WA = 119;
	private static final int SI_SPEC_WB = 120;
	private static final int SI_SPEC_WD = 121;
	private static final int SI_SPEC_WI = 122;
	private static final int SI_SPEC_WP = 123;
	private static final int SI_SPEC_WS = 124;
	private static final int SI_SPEC_WT = 125;
	private static final int SI_SPEC_X = 126;
	private static final int SI_SPEC_XC = 127;
	private static final int SI_SPEC_XH = 128;
	private static final int SI_SPEC_Y = 129;
	private static final int SI_SPEC_YC = 130;
	private static final int SI_SPEC_YP = 131;
	private static final int SI_SPEC_Z = 132;
	private static final int SI_SPEC_ZC = 133;
	private static final int SI_SPEC_ZH = 134;

	/*
	 * codes returned by fiz_check()
	 */
	private static final int FIZ_COAST = 1;
	private static final int FIZ_INTERIOR = 2;

	public static short species_map(String sc) {
		// This can be done more elegantly with Java
		String sc2 = sc.replaceAll(" ", "").toUpperCase();

		// Just incase here is a like for like recreation
		/*
		 * short i, i2; char[] sc2 = new char[10];
		 *
		 * i2 = 0; for (i = 0; i < sc.length && i < 10; i++) { if (sc[i] != ' ') { sc2[i2] =
		 * Character.toUpperCase(sc[i]); i2++; } } sc2[i2] = '\0';
		 */

		// This could be improved with a switch statement or even an if/else. But I have
		// left it since it's like for like
		if (sc2.equals("A")) {
			return SI_SPEC_A;
		}
		if (sc2.equals("ABAL")) {
			return SI_SPEC_ABAL;
		}
		if (sc2.equals("ABCO")) {
			return SI_SPEC_ABCO;
		}
		if (sc2.equals("AC")) {
			return SI_SPEC_AC;
		}
		if (sc2.equals("ACB")) {
			return SI_SPEC_ACB;
		}
		if (sc2.equals("ACT")) {
			return SI_SPEC_ACT;
		}
		if (sc2.equals("AD")) {
			return SI_SPEC_AD;
		}
		if (sc2.equals("AH")) {
			return SI_SPEC_AH;
		}
		if (sc2.equals("AT")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("AX")) {
			return SI_SPEC_AX;
		}
		if (sc2.equals("B")) {
			return SI_SPEC_B;
		}
		if (sc2.equals("BA")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("BB")) {
			return SI_SPEC_BB;
		}
		if (sc2.equals("BC")) {
			return SI_SPEC_BC;
		}
		if (sc2.equals("BG")) {
			return SI_SPEC_BG;
		}
		if (sc2.equals("BI")) {
			return SI_SPEC_BI;
		}
		if (sc2.equals("BL")) {
			return SI_SPEC_BL;
		}
		if (sc2.equals("BM")) {
			return SI_SPEC_BM;
		}
		if (sc2.equals("BP")) {
			return SI_SPEC_BP;
		}
		// if (sc2.equals("BV")){ return SI_SPEC_BV;}
		if (sc2.equals("C")) {
			return SI_SPEC_C;
		}
		if (sc2.equals("CI")) {
			return SI_SPEC_CI;
		}
		if (sc2.equals("CP")) {
			return SI_SPEC_CP;
		}
		if (sc2.equals("CW")) {
			return SI_SPEC_CW;
		}
		if (sc2.equals("CWC")) {
			return SI_SPEC_CWC;
		}
		if (sc2.equals("CWI")) {
			return SI_SPEC_CWI;
		}
		if (sc2.equals("CY")) {
			return SI_SPEC_CY;
		}
		if (sc2.equals("D")) {
			return SI_SPEC_D;
		}
		if (sc2.equals("DG")) {
			return SI_SPEC_DG;
		}
		if (sc2.equals("DM")) {
			return SI_SPEC_DM;
		}
		if (sc2.equals("DR")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("E")) {
			return SI_SPEC_E;
		}
		if (sc2.equals("EA")) {
			return SI_SPEC_EA;
		}
		if (sc2.equals("EB")) {
			return SI_SPEC_EB;
		}
		if (sc2.equals("EE")) {
			return SI_SPEC_EE;
		}
		if (sc2.equals("EP")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("ES")) {
			return SI_SPEC_ES;
		}
		if (sc2.equals("EW")) {
			return SI_SPEC_EW;
		}
		if (sc2.equals("EXP")) {
			return SI_SPEC_EXP;
		}
		if (sc2.equals("FD")) {
			return SI_SPEC_FD;
		}
		if (sc2.equals("FDC")) {
			return SI_SPEC_FDC;
		}
		if (sc2.equals("FDI")) {
			return SI_SPEC_FDI;
		}
		if (sc2.equals("G")) {
			return SI_SPEC_G;
		}
		if (sc2.equals("GP")) {
			return SI_SPEC_GP;
		}
		if (sc2.equals("GR")) {
			return SI_SPEC_GR;
		}
		if (sc2.equals("H")) {
			return SI_SPEC_H;
		}
		if (sc2.equals("HM")) {
			return SI_SPEC_HM;
		}
		if (sc2.equals("HW")) {
			return SI_SPEC_HW;
		}
		if (sc2.equals("HWC")) {
			return SI_SPEC_HWC;
		}
		if (sc2.equals("HWI")) {
			return SI_SPEC_HWI;
		}
		if (sc2.equals("HXM")) {
			return SI_SPEC_HXM;
		}
		if (sc2.equals("IG")) {
			return SI_SPEC_IG;
		}
		if (sc2.equals("IS")) {
			return SI_SPEC_IS;
		}
		if (sc2.equals("J")) {
			return SI_SPEC_J;
		}
		if (sc2.equals("JR")) {
			return SI_SPEC_JR;
		}
		if (sc2.equals("K")) {
			return SI_SPEC_K;
		}
		if (sc2.equals("KC")) {
			return SI_SPEC_KC;
		}
		if (sc2.equals("L")) {
			return SI_SPEC_L;
		}
		if (sc2.equals("LA")) {
			return SI_SPEC_LA;
		}
		if (sc2.equals("LE")) {
			return SI_SPEC_LE;
		}
		if (sc2.equals("LT")) {
			return SI_SPEC_LT;
		}
		if (sc2.equals("LW")) {
			return SI_SPEC_LW;
		}
		if (sc2.equals("M")) {
			return SI_SPEC_M;
		}
		if (sc2.equals("MB")) {
			return SI_SPEC_MB;
		}
		if (sc2.equals("ME")) {
			return SI_SPEC_ME;
		}
		if (sc2.equals("MN")) {
			return SI_SPEC_MN;
		}
		if (sc2.equals("MR")) {
			return SI_SPEC_MR;
		}
		if (sc2.equals("MS")) {
			return SI_SPEC_MS;
		}
		if (sc2.equals("MV")) {
			return SI_SPEC_MV;
		}
		if (sc2.equals("OA")) {
			return SI_SPEC_OA;
		}
		if (sc2.equals("OB")) {
			return SI_SPEC_OB;
		}
		if (sc2.equals("OC")) {
			return SI_SPEC_OC;
		}
		if (sc2.equals("OD")) {
			return SI_SPEC_OD;
		}
		if (sc2.equals("OE")) {
			return SI_SPEC_OE;
		}
		if (sc2.equals("OF")) {
			return SI_SPEC_OF;
		}
		if (sc2.equals("OG")) {
			return SI_SPEC_OG;
		}
		if (sc2.equals("P")) {
			return SI_SPEC_P;
		}
		if (sc2.equals("PA")) {
			return SI_SPEC_PA;
		}
		if (sc2.equals("PF")) {
			return SI_SPEC_PF;
		}
		if (sc2.equals("PJ")) {
			return SI_SPEC_PJ;
		}
		if (sc2.equals("PL")) {
			return SI_SPEC_PL;
		}
		if (sc2.equals("PLC")) {
			return SI_SPEC_PLC;
		}
		if (sc2.equals("PLI")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PM")) {
			return SI_SPEC_PM;
		}
		if (sc2.equals("PR")) {
			return SI_SPEC_PR;
		}
		if (sc2.equals("PS")) {
			return SI_SPEC_PS;
		}
		if (sc2.equals("PW")) {
			return SI_SPEC_PW;
		}
		if (sc2.equals("PXJ")) {
			return SI_SPEC_PXJ;
		}
		if (sc2.equals("PY")) {
			return SI_SPEC_PY;
		}
		if (sc2.equals("Q")) {
			return SI_SPEC_Q;
		}
		if (sc2.equals("QE")) {
			return SI_SPEC_QE;
		}
		if (sc2.equals("QG")) {
			return SI_SPEC_QG;
		}
		if (sc2.equals("R")) {
			return SI_SPEC_R;
		}
		if (sc2.equals("RA")) {
			return SI_SPEC_RA;
		}
		if (sc2.equals("S")) {
			return SI_SPEC_S;
		}
		if (sc2.equals("SA")) {
			return SI_SPEC_SA;
		}
		if (sc2.equals("SB")) {
			return SI_SPEC_SB;
		}
		if (sc2.equals("SE")) {
			return SI_SPEC_SE;
		}
		if (sc2.equals("SI")) {
			return SI_SPEC_SI;
		}
		if (sc2.equals("SN")) {
			return SI_SPEC_SN;
		}
		if (sc2.equals("SS")) {
			return SI_SPEC_SS;
		}
		if (sc2.equals("SW")) {
			return SI_SPEC_SW;
		}
		if (sc2.equals("SX")) {
			return SI_SPEC_SX;
		}
		if (sc2.equals("SXB")) {
			return SI_SPEC_SXB;
		}
		if (sc2.equals("SXE")) {
			return SI_SPEC_SXE;
		}
		if (sc2.equals("SXL")) {
			return SI_SPEC_SXL;
		}
		if (sc2.equals("SXS")) {
			return SI_SPEC_SXS;
		}
		if (sc2.equals("SXW")) {
			return SI_SPEC_SXW;
		}
		if (sc2.equals("SXX")) {
			return SI_SPEC_SXX;
		}
		if (sc2.equals("T")) {
			return SI_SPEC_T;
		}
		if (sc2.equals("TW")) {
			return SI_SPEC_TW;
		}
		if (sc2.equals("U")) {
			return SI_SPEC_U;
		}
		if (sc2.equals("UA")) {
			return SI_SPEC_UA;
		}
		if (sc2.equals("UP")) {
			return SI_SPEC_UP;
		}
		if (sc2.equals("V")) {
			return SI_SPEC_V;
		}
		if (sc2.equals("VB")) {
			return SI_SPEC_VB;
		}
		if (sc2.equals("VP")) {
			return SI_SPEC_VP;
		}
		if (sc2.equals("VS")) {
			return SI_SPEC_VS;
		}
		if (sc2.equals("VV")) {
			return SI_SPEC_VV;
		}
		if (sc2.equals("W")) {
			return SI_SPEC_W;
		}
		if (sc2.equals("WA")) {
			return SI_SPEC_WA;
		}
		if (sc2.equals("WB")) {
			return SI_SPEC_WB;
		}
		if (sc2.equals("WD")) {
			return SI_SPEC_WD;
		}
		if (sc2.equals("WI")) {
			return SI_SPEC_WI;
		}
		if (sc2.equals("WP")) {
			return SI_SPEC_WP;
		}
		if (sc2.equals("WS")) {
			return SI_SPEC_WS;
		}
		if (sc2.equals("WT")) {
			return SI_SPEC_WT;
		}
		if (sc2.equals("X")) {
			return SI_SPEC_X;
		}
		if (sc2.equals("XC")) {
			return SI_SPEC_XC;
		}
		if (sc2.equals("XH")) {
			return SI_SPEC_XH;
		}
		if (sc2.equals("Y")) {
			return SI_SPEC_Y;
		}
		if (sc2.equals("YC")) {
			return SI_SPEC_YC;
		}
		if (sc2.equals("YP")) {
			return SI_SPEC_YP;
		}
		if (sc2.equals("Z")) {
			return SI_SPEC_Z;
		}
		if (sc2.equals("ZC")) {
			return SI_SPEC_ZC;
		}
		if (sc2.equals("ZH")) {
			return SI_SPEC_ZH;
		}

		throw new CodeErrorException("Unknown species code: " + sc2);
	}

	public static short species_remap(String sc, char fiz) {
		// This can be done more elegantly with Java
		String sc2 = sc.replaceAll(" ", "").toUpperCase();

		// Just incase here is a like for like recreation
		/*
		 * short i, i2; char[] sc2 = new char[10];
		 *
		 * i2 = 0; for (i = 0; i < sc.length && i < 10; i++) { if (sc[i] != ' ') { sc2[i2] =
		 * Character.toUpperCase(sc[i]); i2++; } } sc2[i2] = '\0';
		 */

		if (sc2.equals("A")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("ABAL")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("ABCO")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("AC")) {
			return SI_SPEC_ACB;
		}
		if (sc2.equals("ACB")) {
			return SI_SPEC_ACB;
		}
		if (sc2.equals("ACT")) {
			return SI_SPEC_ACT;
		}
		if (sc2.equals("AD")) {
			return SI_SPEC_ACT;
		}
		if (sc2.equals("AH")) {
			return SI_SPEC_ACT;
		}
		if (sc2.equals("AT")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("AX")) {
			return SI_SPEC_ACB;
		}
		if (sc2.equals("B")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_BA;
			case FIZ_INTERIOR:
				return SI_SPEC_BL;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("BA")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("BAC")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("BAI")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("BB")) {
			return SI_SPEC_BL;
		}
		if (sc2.equals("BC")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_BA;
			case FIZ_INTERIOR:
				return SI_SPEC_BL;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("BG")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("BI")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("BL")) {
			return SI_SPEC_BL;
		}
		if (sc2.equals("BM")) {
			return SI_SPEC_BA;
		}
		if (sc2.equals("BN")) {
			return SI_SPEC_BP;
		}
		if (sc2.equals("BP")) {
			return SI_SPEC_BP;
		}
//  if (sc2.equals("BV")){ return SI_SPEC_AT;}
		if (sc2.equals("C")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("CI")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("COT")) {
			return SI_SPEC_ACT;
		}
		if (sc2.equals("CP")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("CT")) {
			return SI_SPEC_ACT;
		}
		if (sc2.equals("CW")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("CWC")) {
			return SI_SPEC_CWC;
		}
		if (sc2.equals("CWI")) {
			return SI_SPEC_CWI;
		}
		if (sc2.equals("CY")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("D")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("DF")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("DG")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("DM")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("DR")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("E")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("EA")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("EB")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("EE")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("EP")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("ES")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("EW")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("EXP")) {
			return SI_SPEC_EP;
		}
		if (sc2.equals("F")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("FD")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("FDC")) {
			return SI_SPEC_FDC;
		}
		if (sc2.equals("FDI")) {
			return SI_SPEC_FDI;
		}
		if (sc2.equals("G")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("GP")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("GR")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("H")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_HWC;
			case FIZ_INTERIOR:
				return SI_SPEC_HWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("HM")) {
			return SI_SPEC_HM;
		}
		if (sc2.equals("HW")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_HWC;
			case FIZ_INTERIOR:
				return SI_SPEC_HWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("HWC")) {
			return SI_SPEC_HWC;
		}
		if (sc2.equals("HWI")) {
			return SI_SPEC_HWI;
		}
		if (sc2.equals("HXM")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_HWC;
			case FIZ_INTERIOR:
				return SI_SPEC_HWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("IG")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("IS")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("J")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("JR")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("K")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("KC")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("L")) {
			return SI_SPEC_LW;
		}
		if (sc2.equals("LA")) {
			return SI_SPEC_LW;
		}
		if (sc2.equals("LE")) {
			return SI_SPEC_LW;
		}
		if (sc2.equals("LT")) {
			return SI_SPEC_LW;
		}
		if (sc2.equals("LW")) {
			return SI_SPEC_LW;
		}
		if (sc2.equals("M")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("MB")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("ME")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("MN")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("MR")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("MS")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("MV")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("OA")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("OB")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("OC")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("OD")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("OE")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("OF")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("OG")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("P")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PA")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PF")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PJ")) {
			return SI_SPEC_PJ;
		}
		if (sc2.equals("PL")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PLC")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PLI")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PM")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PR")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PS")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PV")) {
			return SI_SPEC_PY;
		}
		if (sc2.equals("PW")) {
			return SI_SPEC_PW;
		}
		if (sc2.equals("PXJ")) {
			return SI_SPEC_PLI;
		}
		if (sc2.equals("PY")) {
			return SI_SPEC_PY;
		}
		if (sc2.equals("Q")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("QE")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("QG")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("R")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("RA")) {
			return SI_SPEC_DR;
		}
		if (sc2.equals("S")) { // Duplicate case? Unreachable
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_SS;
			case FIZ_INTERIOR:
				return SI_SPEC_SW;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("SA")) { // Duplicate case? Unreachable
			return SI_SPEC_SW;
		}
		if (sc2.equals("SB")) {
			return SI_SPEC_SB;
		}
		if (sc2.equals("SE")) {
			return SI_SPEC_SE;
		}
		if (sc2.equals("SI")) {
			return SI_SPEC_SW;
		}
		if (sc2.equals("SN")) {
			return SI_SPEC_SW;
		}
		if (sc2.equals("SS")) {
			return SI_SPEC_SS;
		}
		if (sc2.equals("SW")) {
			return SI_SPEC_SW;
		}
		if (sc2.equals("SX")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_SS;
			case FIZ_INTERIOR:
				return SI_SPEC_SW;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("SXB")) {
			return SI_SPEC_SW;
		}
		if (sc2.equals("SXE")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_SS;
			case FIZ_INTERIOR:
				return SI_SPEC_SE;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("SXL")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_SS;
			case FIZ_INTERIOR:
				return SI_SPEC_SW;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("SXS")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_SS;
			case FIZ_INTERIOR:
				return SI_SPEC_SW;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("SXW")) {
			return SI_SPEC_SW;
		}
		if (sc2.equals("SXX")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_SS;
			case FIZ_INTERIOR:
				return SI_SPEC_SW;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("T")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_HWC;
			case FIZ_INTERIOR:
				return SI_SPEC_HWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("TW")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_HWC;
			case FIZ_INTERIOR:
				return SI_SPEC_HWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("U")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("UA")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("UP")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("V")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("VB")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("VP")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("VS")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("VV")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("W")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WA")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WB")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WD")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WI")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WP")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WS")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("WT")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("X")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("XC")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("XH")) {
			return SI_SPEC_AT;
		}
		if (sc2.equals("Y")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("YC")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("YP")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_CWC;
			case FIZ_INTERIOR:
				return SI_SPEC_CWI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("Z")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("ZC")) {
			switch (FizCheck.fiz_check(fiz)) {
			case FIZ_COAST:
				return SI_SPEC_FDC;
			case FIZ_INTERIOR:
				return SI_SPEC_FDI;
			default:
				throw new CodeErrorException("Unknown species code: " + sc2);
			}
		}
		if (sc2.equals("ZH")) {
			return SI_SPEC_AT;
		}

		throw new CodeErrorException("Unknown species code: " + sc2);
	}

}
