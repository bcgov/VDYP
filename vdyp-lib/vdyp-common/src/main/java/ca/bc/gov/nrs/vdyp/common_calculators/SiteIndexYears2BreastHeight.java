package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.SiteIndexUtilities.llog;
import static ca.bc.gov.nrs.vdyp.common_calculators.SiteIndexUtilities.ppow;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptTotalException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;

/**
 * SiteIndexYears2BreastHeight - computes number of years from seed to breast height.
 */
public class SiteIndexYears2BreastHeight {

	/**
	 * Calculate years-to-breast-height from a curve and site.
	 *
	 * @param cuIndex   the index of the site curve
	 * @param siteIndex the index of the site, in metres, based on a breast height age 50.
	 * @return as described. The result has not been rounded.
	 * @throws LessThan13Exception           when the site index is < 1.3.
	 * @throws GrowthInterceptTotalException when cuIndex identifies a Growth Intercept curve.
	 * @throws NoAnswerException             when the calculation will not converge.
	 */
	public static double y2bh(SiteIndexEquation cuIndex, double siteIndex) throws CommonCalculatorException {
		double y2bh;
		double si20;

		if (siteIndex < 1.3) {
			throw new LessThan13Exception("Site index < 1.3m: " + siteIndex);
		}

		if (cuIndex == null) {
			throw new CurveErrorException("cuIndex is null");
		}

		switch (cuIndex) {
		case SI_FDC_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_FDC_NIGHGI: " + cuIndex);

		case SI_FDC_BRUCE:
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_FDC_BRUCEAC:
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_FDC_NIGHTA:
			if (siteIndex <= 9.051) {
				throw new NoAnswerException("Site index out of range, site index <= 9.051: " + siteIndex);
			} else {
				y2bh = 24.44 * Math.pow(siteIndex - 9.051, -0.394);
			}
			break;

		case SI_FDC_BRUCENIGH:
			/* changed 2002 AUG 20 */
			if (siteIndex <= 15) {
				min1(y2bh = 13.25 - siteIndex / 6.096);
			} else {
				y2bh = 36.5818 * Math.pow(siteIndex - 6.6661, -0.5526);
			}
			break;

		case SI_FDC_COCHRAN:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_FDC_KING:
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_HWC_FARR:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_HWC_BARKER:
			y2bh = min1(-5.2 + 410.00 / siteIndex);
			break;

		case SI_HM_MEANS:
			/* copied from Hw Wiley 1978 */
			/* seed (root collar) */
			y2bh = min1(9.43 - siteIndex / 7.088);
			break;

		case SI_HM_MEANSAC:
			/* copied from Hw Wiley 1978 */
			/* seed (root collar) */
			y2bh = min1(9.43 - siteIndex / 7.088);
			break;

		// Couldn't find constant
		/*
		 * case SI_HM_WILEY: // copied from Hw Wiley 1978 // seed (root collar) y2bh = 9.43 - site_index / 7.088; if
		 * (y2bh < 1){ y2bh = 1; } break;
		 */

		case SI_HWI_NIGH:
			/* from seed */
			y2bh = min1(446.6 * ppow(siteIndex, -1.432));
			break;

		case SI_HWI_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_HWI_NIGHGI: " + cuIndex);

		case SI_HWC_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_HWC_NIGHGI: " + cuIndex);

		case SI_HWC_NIGHGI99:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_HWC_NIGHGI99: " + cuIndex);

		case SI_SS_NIGHGI99:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SS_NIGHGI99: " + cuIndex);

		case SI_SW_NIGHGI99:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SW_NIGHGI99: " + cuIndex);

		case SI_SW_NIGHGI2004:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SW_NIGHGI2004: " + cuIndex);

		case SI_LW_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_LW_NIGHGI: " + cuIndex);

		case SI_HWC_WILEY:
			/* seed (root collar) */
			y2bh = min1(9.43 - siteIndex / 7.088);
			break;

		case SI_HWC_WILEYAC:
			/* seed (root collar) */
			y2bh = min1(9.43 - siteIndex / 7.088);
			break;

		case SI_HWC_WILEY_BC:
			/* seed (root collar) */
			y2bh = min1(9.43 - siteIndex / 7.088);
			break;

		case SI_HWC_WILEY_MB:
			/* seed (root collar) */
			y2bh = min1(9.43 - siteIndex / 7.088);
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
			y2bh = 5 + 1.872138 + 49.555513 / siteIndex;
			break;

		case SI_PJ_HUANGAC:
			/* from seed */
			y2bh = 5 + 1.872138 + 49.555513 / siteIndex;
			break;
		// Couldn't find constant
		/*
		 * case SI_PLI_NIGHGI: return SI_ERR_GI_TOT; break;
		 */

		case SI_PLI_NIGHGI97:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_PLI_NIGHGI97: " + cuIndex);

		case SI_PLI_HUANG_PLA:
			/* from seed */
			y2bh = 3.5 + 1.740006 + 58.83891 / siteIndex;
			break;

		case SI_PLI_HUANG_NAT:
			/* from seed */
			y2bh = 5 + 1.740006 + 58.83891 / siteIndex;
			break;

		case SI_PLI_NIGHTA2004:
			/* temporarily copied from PLI_NIGHTA98 */
			if (siteIndex < 9.5) {
				throw new NoAnswerException("Site index out of range, site index < 9.5: " + siteIndex);
			} else {
				y2bh = 21.6623 * ppow(siteIndex - 9.05671, -0.550762);
			}
			break;

		case SI_PLI_NIGHTA98:
			if (siteIndex < 9.5) {
				throw new NoAnswerException("Site index out of range, site index < 9.5: " + siteIndex);
			} else {
				y2bh = 21.6623 * ppow(siteIndex - 9.05671, -0.550762);
			}
			break;

		case SI_SW_GOUDNIGH:
			if (siteIndex < 19.5) {
				/* Goudie plantation */
				y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
				/* smooth transition to Nigh curve */
				if (y2bh < 10.45) {
					y2bh = 10.45;
				}
			} else {
				/* Nigh */
				y2bh = 35.87 * ppow(siteIndex - 9.726, -0.5409);
			}
			break;

		case SI_SW_NIGHTA2004:
			/* temporarily copied from SW_NIGHTA */
			if (siteIndex < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			} else {
				y2bh = 35.87 * ppow(siteIndex - 9.726, -0.5409);
			}
			break;

		case SI_SW_HU_GARCIA:
			/* temporarily copied from SW_NIGHTA */
			if (siteIndex < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			} else {
				y2bh = 35.87 * ppow(siteIndex - 9.726, -0.5409);
			}
			break;

		case SI_SW_NIGHTA:
			if (siteIndex < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			} else {
				y2bh = 35.87 * ppow(siteIndex - 9.726, -0.5409);
			}
			break;

		case SI_SE_NIGH:
			/* copied from SW_GOUDIE_NATAC */
			y2bh = 6.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SE_NIGHTA:
			/* copied from SW_NIGHTA */
			if (siteIndex < 14.2) {
				/* from Goudie Sw managed stands */
				y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			} else {
				y2bh = 35.87 * ppow(siteIndex - 9.726, -0.5409);
			}
			break;

		case SI_SE_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SE_NIGHGI: " + cuIndex);

		case SI_PLI_THROWNIGH:
			if (siteIndex < 18.5) {
				/* Thrower Pli */
				y2bh = 2 + 0.55 + 69.4 / siteIndex;
			} else {
				/* Nigh Pli */
				y2bh = 21.6623 * ppow(siteIndex - 9.05671, -0.550762);
			}
			/*
			 * slightly older version y2bh = 22.41028 * ppow (site_index - 8.90585, -0.5614);
			 */
			break;

		case SI_PLI_THROWER:
			/* from seed */
			y2bh = 2 + 0.55 + 69.4 / siteIndex;
			break;

		case SI_PLI_MILNER:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PLI_CIESZEWSKI:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PLI_GOUDIE_DRY:
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PLI_GOUDIE_WET:
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PLI_DEMPSTER:
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PL_CHEN:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_SE_CHEN:
			/* copied from Sw Goudie (natural) */
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SE_CHENAC:
			/* copied from Sw Goudie (natural) */
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / siteIndex;
			break;

		// Couldn't find constant
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
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SW_NIGHGI: " + cuIndex);

		case SI_SW_HUANG_PLA:
			/* from seed */
			y2bh = 4.5 + 4.3473 + 59.908359 / siteIndex;
			break;

		case SI_SW_HUANG_NAT:
			/* from seed */
			y2bh = 8 + 4.3473 + 59.908359 / siteIndex;
			break;

		case SI_SW_THROWER:
			/* from seed */
			y2bh = 4 + 0.38 + 117.34 / siteIndex;
			break;

		case SI_SW_KER_PLA:
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_KER_NAT:
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_GOUDIE_PLA:
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_GOUDIE_NAT:
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_GOUDIE_PLAAC:
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_GOUDIE_NATAC:
			/* from seed */
			y2bh = 6.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_DEMPSTER:
			y2bh = 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SW_CIESZEWSKI:
			/*
			 * borrowed from Sw Goudie, plantation
			 */
			/* from seed */
			y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_SB_HUANG:
			/* from seed */
			y2bh = 8 + 2.288325 + 80.774008 / siteIndex;
			break;

		case SI_SB_KER:
			/*
			 * borrowed from Sb Dempster, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / siteIndex;
			break;

		case SI_SB_DEMPSTER:
			/*
			 * estimate of 7 years from ground to stump, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / siteIndex;
			break;

		case SI_SB_NIGH:
			/*
			 * estimate of 7 years from ground to stump, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / siteIndex;
			break;

		case SI_SB_CIESZEWSKI:
			/*
			 * borrowed from Sb Dempster, natural stand
			 */
			/* from seed */
			y2bh = 7.0 + 4.0427 + 61.08 / siteIndex;
			break;

		case SI_SS_GOUDIE:
			/* from seed */
			y2bh = min1(11.7 - siteIndex / 5.4054);
			break;

		case SI_SS_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_SS_NIGHGI: " + cuIndex);

		case SI_SS_NIGH:
			/* copied from Ss Goudie */
			/* from seed */
			y2bh = min1(11.7 - siteIndex / 5.4054);
			break;

		case SI_SS_FARR:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_SS_BARKER:
			y2bh = min1(-5.13 + 450.00 / siteIndex);
			break;

		case SI_CWI_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_CWI_NIGHGI: " + cuIndex);

		case SI_CWI_NIGH:
			/* from seed */
			y2bh = min1(18.18 - 0.5526 * siteIndex);
			break;

		case SI_CWC_KURUCZ:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_CWC_KURUCZAC:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_CWC_BARKER:
			y2bh = min1(-3.46 + 285.00 / siteIndex);
			break;

		case SI_CWC_NIGH:
			/*
			 * approximate function, borrowed from Fdc Bruce 1981
			 */
			/* from seed */
			y2bh = min1(13.25 - siteIndex / 6.096);
			break;

		case SI_BA_DILUCCA:
			/*
			 * copied from BAC_KURUCZ86
			 */
			/* from seed */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BB_KER:
			/* from seed */
			y2bh = min5(18.47373 - siteIndex / 2.447);
			break;

		case SI_BP_CURTIS:
			/* from seed */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BP_CURTISAC:
			/* copied from BP_CURTIS */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BA_NIGHGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_BA_NIGHGI: " + cuIndex);

		case SI_BA_NIGH:
			/*
			 * copied from BAC_KURUCZ86
			 */
			/* from seed */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BA_KURUCZ86:
			/* from seed */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BA_KURUCZ82:
			/* from seed */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BA_KURUCZ82AC:
			/* from seed */
			y2bh = min5(18.47373 - 0.4086 * siteIndex);
			break;

		case SI_BL_CHEN:
			/*
			 * Copied from Bl Kurucz82 (Thrower)
			 */
			/* from seed (root collar) */
			y2bh = min5(42.25 - 10.66 * llog(siteIndex));
			break;

		case SI_BL_CHENAC:
			/*
			 * Copied from Bl Kurucz82 (Thrower)
			 */
			/* from seed (root collar) */
			y2bh = min5(42.25 - 10.66 * llog(siteIndex));
			break;

		case SI_BL_THROWERGI:
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_BL_THROWERGI: " + cuIndex);

		case SI_BL_KURUCZ82:
			/*
			 * From Jim Thrower, 1991 Jun 19
			 */
			/* from seed (root collar) */
			y2bh = min5(42.25 - 10.66 * llog(siteIndex));
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
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_FDI_NIGHGI: " + cuIndex);

		case SI_FDI_HUANG_PLA:
			/* from seed */
			y2bh = 6.5 + 5.276585 + 38.968242 / siteIndex;
			break;

		case SI_FDI_HUANG_NAT:
			/* from seed */
			y2bh = 8 + 5.276585 + 38.968242 / siteIndex;
			break;

		case SI_FDI_MILNER:
			/*
			 * copied from Fdi Thrower
			 */
			/* from seed */
			y2bh = 4.0 + 99.0 / siteIndex;
			break;

		case SI_FDI_THROWER:
			/* from seed */
			y2bh = 4.0 + 99.0 / siteIndex;
			break;

		case SI_FDI_THROWERAC:
			/* copied from FDI_THROWER */
			/* from seed */
			y2bh = 4.0 + 99.0 / siteIndex;
			break;

		case SI_FDI_VDP_MONT:
			/* from seed */
			y2bh = 4.0 + 99.0 / siteIndex;
			break;

		case SI_FDI_VDP_WASH:
			/* from seed */
			y2bh = 4.0 + 99.0 / siteIndex;
			break;

		case SI_FDI_MONS_DF:
			y2bh = min8(16.0 - siteIndex / 3.0);
			break;

		case SI_FDI_MONS_GF:
			y2bh = min8(16.0 - siteIndex / 3.0);
			break;

		case SI_FDI_MONS_WRC:
			y2bh = min8(16.0 - siteIndex / 3.0);
			break;

		case SI_FDI_MONS_WH:
			y2bh = min8(16.0 - siteIndex / 3.0);
			break;

		case SI_FDI_MONS_SAF:
			y2bh = min8(16.0 - siteIndex / 3.0);
			break;

		case SI_AT_NIGH:
			/*
			 * equation copied from At Goudie
			 */
			y2bh = 1.331 + 38.56 / siteIndex;
			break;

		case SI_AT_CHEN:
			/*
			 * equation copied from At Goudie
			 */
			y2bh = 1.331 + 38.56 / siteIndex;
			break;

		case SI_AT_HUANG:
			/* from seed */
			y2bh = 1 + 2.184066 + 50.788746 / siteIndex;
			break;

		case SI_AT_GOUDIE:
			y2bh = 1.331 + 38.56 / siteIndex;
			break;

		case SI_ACB_HUANG:
			/* from seed */
			y2bh = min1(1 - 1.196472 + 104.124205 / siteIndex);
			break;

		case SI_ACB_HUANGAC:
			/* copied from ACB_HUANG */
			/* from seed */
			y2bh = min1(1 - 1.196472 + 104.124205 / siteIndex);
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
			y2bh = 1.331 + 38.56 / siteIndex;
			break;

		case SI_DR_HARRING:
			si20 = ppow(siteIndex, 1.5) / 8.0;
			if (si20 >= 15) {
				y2bh = 1.0;
			} else {
				y2bh = 2.0;
			}
			break;

		case SI_DR_CHEN:
			/* copied from Dr Harrington */
			si20 = ppow(siteIndex, 1.5) / 8.0;
			if (si20 >= 15) {
				y2bh = 1.0;
			} else
				y2bh = 2.0;
			break;

		case SI_DR_NIGH: {
			double si25;

			si25 = 0.3094 + 0.7616 * siteIndex;
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
			throw new GrowthInterceptTotalException("Cannot use with GI equations, case SI_PY_NIGHGI: " + cuIndex);

		case SI_PY_NIGH:
			y2bh = 36.35 * Math.pow(0.9318, siteIndex);
			break;

		case SI_PY_HANN:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PY_HANNAC:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
			break;

		case SI_PY_MILNER:
			/*
			 * copied from Pli Goudie
			 */
			/* from seed */
			y2bh = 2 + 3.6 + 42.64 / siteIndex;
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
			y2bh = 3.36 + 87.18 / siteIndex;
			break;

		case SI_LW_NIGH:
			/*
			 * Copied from Lw Milner.
			 */
			/* from seed (root collar) */
			y2bh = 3.36 + 87.18 / siteIndex;
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
			y2bh = 1.331 + 38.56 / siteIndex;
			break;

		case SI_PW_CURTIS:
			/*
			 * equation copied from Sw, plantation
			 */
			y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
			break;

		case SI_PW_CURTISAC:
			/*
			 * equation copied from Sw, plantation
			 */
			y2bh = 2.0 + 2.1578 + 110.76 / siteIndex;
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

	private static double min1(double d) {
		return d < 1 ? 1.0 : d;
	}

	private static double min5(double d) {
		return d < 5 ? 5.0 : d;
	}

	private static double min8(double d) {
		return d < 8 ? 8.0 : d;
	}

	/**
	 * Calculates years-to-breast-height and then converts all results r, x <= r < (x + 1) to x + 0.5.
	 *
	 * @param cuIndex   site curve index
	 * @param siteIndex site index
	 * @return as described
	 * @throws CommonCalculatorException
	 */
	public static double y2bh05(SiteIndexEquation cuIndex, double siteIndex) throws CommonCalculatorException {

		double y2bh = y2bh(cuIndex, siteIndex);

		/* force answer to be in steps 0.5, 1.5, 2.5, etc. */
		return ((int) y2bh) + 0.5;
	}
}
