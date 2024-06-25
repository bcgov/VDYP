package ca.bc.gov.nrs.vdyp.si32.site;

import java.text.MessageFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEstimationType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsBiomassConversionSupportedGenera;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsBiomassConversionSupportedSpecies;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsDeadConversionParams;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsLiveConversionParams;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsSpeciesMethods;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsTreeSpecies;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SpeciesRegion;
import ca.bc.gov.nrs.vdyp.si32.vdyp.SP64Name;
import ca.bc.gov.nrs.vdyp.si32.vdyp.VdypMethods;
import ca.bc.gov.nrs.vdyp.sindex.Reference;
import ca.bc.gov.nrs.vdyp.sindex.Sindxdll;

public class SiteTool {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(SiteTool.class);

	public static final String UNKNOWN_CURVE_RESULT = "Unknown Curve";

	/**
	 * Converts a MoF sp64 species name (e.g, "AC" from SP64Name.AC) to its equivalent in
	 * {@link CfsBiomassConversionSupportedSpecies}, should one exist. If one doesn't,
	 * CFSBiomassConversionSupportedSpecies.UNKNOWN is returned.
	 *
	 * @param spcsNm the text portion of a SP64Name
	 * @return as described
	 */
	public static CfsBiomassConversionSupportedSpecies lcl_MoFSP64ToCFSSpecies(String spcsNm) {

		SP64Name sp64Name = SP64Name.forText(spcsNm);

		switch (sp64Name) {
		case AC:
			return CfsBiomassConversionSupportedSpecies.AC;
		case ACB:
			return CfsBiomassConversionSupportedSpecies.ACB;
		case AT:
			return CfsBiomassConversionSupportedSpecies.AT;
		case B:
			return CfsBiomassConversionSupportedSpecies.B;
		case BA:
			return CfsBiomassConversionSupportedSpecies.BA;
		case BG:
			return CfsBiomassConversionSupportedSpecies.BG;
		case BL:
			return CfsBiomassConversionSupportedSpecies.BL;
		case CW:
			return CfsBiomassConversionSupportedSpecies.CW;
		case DR:
			return CfsBiomassConversionSupportedSpecies.DR;
		case EA:
			return CfsBiomassConversionSupportedSpecies.EA;
		case EP:
			return CfsBiomassConversionSupportedSpecies.EP;
		case EXP:
			return CfsBiomassConversionSupportedSpecies.EXP;
		case FD:
			return CfsBiomassConversionSupportedSpecies.FD;
		case FDC:
			return CfsBiomassConversionSupportedSpecies.FDC;
		case FDI:
			return CfsBiomassConversionSupportedSpecies.FDI;
		case H:
			return CfsBiomassConversionSupportedSpecies.H;
		case HM:
			return CfsBiomassConversionSupportedSpecies.HM;
		case HW:
			return CfsBiomassConversionSupportedSpecies.HW;
		case L:
			return CfsBiomassConversionSupportedSpecies.L;
		case LA:
			return CfsBiomassConversionSupportedSpecies.LA;
		case LT:
			return CfsBiomassConversionSupportedSpecies.LT;
		case LW:
			return CfsBiomassConversionSupportedSpecies.LW;
		case MB:
			return CfsBiomassConversionSupportedSpecies.MB;
		case PA:
			return CfsBiomassConversionSupportedSpecies.PA;
		case PL:
		case PLI:
			return CfsBiomassConversionSupportedSpecies.PL;
		case PLC:
			return CfsBiomassConversionSupportedSpecies.PLC;
		case PW:
			return CfsBiomassConversionSupportedSpecies.PW;
		case PY:
			return CfsBiomassConversionSupportedSpecies.PY;
		case S:
			return CfsBiomassConversionSupportedSpecies.S;
		case SB:
			return CfsBiomassConversionSupportedSpecies.SB;
		case SE:
			return CfsBiomassConversionSupportedSpecies.SE;
		case SS:
			return CfsBiomassConversionSupportedSpecies.SS;
		case SW:
			return CfsBiomassConversionSupportedSpecies.SW;
		case SX:
			return CfsBiomassConversionSupportedSpecies.SX;
		case W:
			return CfsBiomassConversionSupportedSpecies.W;
		case X:
			return CfsBiomassConversionSupportedSpecies.XC;
		case YC:
			return CfsBiomassConversionSupportedSpecies.YC;
		default:
			return CfsBiomassConversionSupportedSpecies.UNKNOWN;
		}
	}

	/**
	 * Convert the supplied Internal Species Index into its corresponding string.
	 *
	 * @param cfsSpecies the species to be converted into a string.
	 * @return the string corresponding to the supplied species. If {@code cfsSpecies} is null or has the value
	 *         "UNKNOWN", "??" is returned.
	 */
	public static String lcl_InternalSpeciesIndexToString(CfsBiomassConversionSupportedSpecies cfsSpecies) {
		if (cfsSpecies == null || CfsBiomassConversionSupportedSpecies.UNKNOWN.equals(cfsSpecies)) {
			return "??";
		} else {
			return cfsSpecies.getText();
		}
	}

	/**
	 * Convert the supplied Internal Genus Index into its corresponding string.
	 *
	 * @param genera the internal genus index to be converted into a string.
	 * @return the string corresponding to the supplied internal genus index. If {@code genera} is null or has the value
	 *         "INVALID", "genusInt_INVALID" is returned.
	 */
	public static String lcl_InternalGenusIndexToString(CfsBiomassConversionSupportedGenera genera) {
		if (genera == null || genera.equals(CfsBiomassConversionSupportedGenera.INVALID))
			return "genusInt_INVALID";
		else
			return genera.getText();
	}

	/**
	 * Convert the supplied Live Conversion Parameter into an identifying string.
	 *
	 * @param liveParam  the conversion parameter to be converted into a string.
	 * @param nameFormat indicates in what format the enumeration constant is to be converted.
	 * @return a string representation of the parameter. If {@code liveParam} has the value null or <code>UNKNOWN</code>
	 *         then if {@code nameFormat} is null or not CAT_ONLY, NAME_ONLY or CAT_NAME, "cfsLiveParam_UNKNOWN" is
	 *         returned. Otherwise, "??" is returned.
	 *         <p>
	 *         On the other hand ({@code liveParam} is not null and not <code>UNKNOWN</code>), the result is a function
	 *         of the value of {@code nameFormat}, unless that is null or not CAT_ONLY, NAME_ONLY or CAT_NAME, in which
	 *         case the text of {@code liveParam} is returned.
	 */
	public static String lcl_LiveConversionParamToString(CfsLiveConversionParams liveParam, NameFormat nameFormat) {
		if (liveParam == null || liveParam.equals(CfsLiveConversionParams.UNKNOWN)) {
			if (nameFormat == null || nameFormat.equals(NameFormat.ENUM_STR)) {
				return "cfsLiveParam_UNKNOWN";
			} else {
				return "??";
			}
		} else if (nameFormat == null) {
			return liveParam.getText();
		} else {
			switch (nameFormat) {
			case CAT_ONLY:
				return liveParam.getCategory();
			case NAME_ONLY:
				return liveParam.getShortName();
			case CAT_NAME:
				return MessageFormat.format("{0} {1}", liveParam.getCategory(), liveParam.getShortName());
			default:
				return liveParam.getText();
			}
		}
	}

	/**
	 * Convert the supplied Dead Conversion Parameter into an identifying string.
	 *
	 * @param deadParam  the conversion parameter to be converted into a string.
	 * @param nameFormat indicates in what format the enumeration constant is to be converted.
	 * @return a string representation of the parameter. If {@code deadParam} has the value null or <code>UNKNOWN</code>
	 *         then if {@code nameFormat} is null or not CAT_ONLY, NAME_ONLY or CAT_NAME, "cfsDeadParam_UNKNOWN" is
	 *         returned. Otherwise, "??" is returned.
	 *         <p>
	 *         On the other hand ({@code deadParam} is not null and not <code>UNKNOWN</code>), the result is a function
	 *         of the value of {@code nameFormat}, unless that is null or not CAT_ONLY, NAME_ONLY or CAT_NAME, in which
	 *         case the text of {@code deadParam} is returned.
	 */
	public static String lcl_DeadConversionParamToString(CfsDeadConversionParams deadParam, NameFormat nameFormat) {
		if (deadParam == null || deadParam.equals(CfsDeadConversionParams.UNKNOWN)) {
			if (nameFormat == null || nameFormat.equals(NameFormat.ENUM_STR)) {
				return "cfsDeadParam_UNKNOWN";
			} else {
				return "??";
			}
		} else if (nameFormat == null) {
			return deadParam.getText();
		} else {
			switch (nameFormat) {
			case CAT_ONLY:
				return deadParam.getCategory();
			case NAME_ONLY:
				return deadParam.getShortName();
			case CAT_NAME:
				return MessageFormat.format("{0} {1}", deadParam.getCategory(), deadParam.getShortName());
			default:
				return deadParam.getText();
			}
		}
	}

	/**
	 * Return the species number for the given CFS Tree Species.
	 * <p>
	 * CFS Species are defined in Appendix 7 of the document 'Model_based_volume_to_biomass_CFS.pdf' found in
	 * 'Documents/CFS-Biomass'.
	 *
	 * @param cfsSpcs the CFS tree species whose species number is to be returned.
	 *
	 * @return as described.
	 */
	public static int cfsSpcsToCfsSpcsNum(CfsTreeSpecies cfsSpcs) {
		return CfsSpeciesMethods.getSpeciesIndexBySpecies(cfsSpcs);
	}

	/**
	 * Determines if the supplied species is a deciduous or coniferous species.
	 *
	 * @param sp64Index the SP64Name's -index- of species in question.
	 * @return as described
	 */
	public static boolean getIsDeciduous(int sp64Index) {

		return VdypMethods.isDeciduous(SP64Name.forIndex(sp64Index));
	}

	/**
	 * Determines if the supplied species is a softwood species.
	 *
	 * @param sp64CodeName the species short ("code") name.
	 * @return as described
	 */
	public static boolean getIsSoftwood(String sp64CodeName) {

		// Note that if spName is not a recognized species name, the correct default value is returned.
		return VdypMethods.speciesTable.getByCode(sp64CodeName).details().isSoftwood();
	}

	/**
	 * Determines if the supplied species corresponds to a Pine species or not.
	 *
	 * @param sp64CodeName the species short ("code") name.
	 * @return {@code true} when the supplied species is a Pine related species and false if not, or the supplied
	 *         species was not recognized.
	 */
	public static boolean getIsPine(String sp64CodeName) {

		String sp0Name = VdypMethods.getVDYP7Species(sp64CodeName);
		if (sp0Name != null) {
			switch (sp0Name) {
			case "PA", "PL", "PW", "PY":
				return true;
			default:
				return false;
			}
		}

		return false;
	}

	/**
	 * Converts a species name to its corresponding CFS defined species.
	 * <p>
	 * The list of species mappings is defined in the file 'BCSpcsToCFSSpcs-SAS.txt' found in 'Documents/CFS-Biomass'.
	 *
	 * @param sp64CodeName the species short ("code") name.
	 * @return the mapping to the equivalent CFS defined tree species (if a mapping exists). {@code UNKNOWN} is returned
	 *         if the species was not recognized or a mapping does not exist.
	 */
	public static CfsTreeSpecies getSpeciesCFSSpcs(String sp64CodeName) {

		// Note that if spName is not a recognized species name, the correct default value is returned.
		return VdypMethods.speciesTable.getByCode(sp64CodeName).details().cfsSpecies();
	}

	/**
	 * Returns the Canadian Forest Service Species Number corresponding to the MoF Species Number.
	 * <p>
	 * The mapping from MoF Species is defined in 'BCSpcsToCFSSpcs-SAS.txt' found in 'Documents/CFS-Biomass'.
	 *
	 * @param sp64CodeName the species short ("code") name.
	 * @return the CFS Species Number corresponding to the MoF Species index, and -1 if the species index is not in
	 *         range or there is no mapping from the MoF Species to the CFS Species.
	 */
	public static int getSpeciesCFSSpcsNum(String sp64CodeName) {

		CfsTreeSpecies cfsSpcs = getSpeciesCFSSpcs(sp64CodeName);

		if (cfsSpcs != CfsTreeSpecies.UNKNOWN) {
			return cfsSpcsToCfsSpcsNum(cfsSpcs);
		} else {
			return -1;
		}
	}

	/**
	 * Converts a Height and Age to a Site Index for a particular Site Index Curve.
	 *
	 * @param curve   the particular site index curve to project the height and age along. This curve must be one of the
	 *                active curves defined in "sindex.h"
	 * @param age     the age of the trees indicated by the curve selection. The interpretation of this age is modified
	 *                by the 'ageType' parameter.
	 * @param ageType must be one of:
	 *                <ul>
	 *                <li>Height2SiteIndex.SI_AT_TOTAL the age is the total age of the stand in years since planting.
	 *                <li>Height2SiteIndex.SI_AT_BREAST the age indicates the number of years since the stand reached
	 *                breast height.
	 *                </ul>
	 * @param height  the height of the species in meters.
	 * @param estType must be one of:
	 *                <ul>
	 *                <li>Height2SiteIndex.SI_EST_DIRECT compute the site index based on direct equations if available.
	 *                If the equations are not available, then automatically fall to the SI_EST_ITERATE method.
	 *                <li>Height2SiteIndex.SI_EST_ITERATE compute the site index based on an iterative method which
	 *                converges on the true site index.
	 *                </ul>
	 * @return the site index of the pure species stand given the height and age.
	 */
	public static double heightAndAgeToSiteIndex(
			SiteIndexEquation curve, double age, SiteIndexAgeType ageType, double height,
			SiteIndexEstimationType estType
	) throws CommonCalculatorException {

		Reference<Double> siRef = new Reference<>();

		// This method always returns 0; in the event of an error, an exception is thrown.
		Sindxdll.HtAgeToSI(curve, age, ageType, height, estType, siRef);

		double siteIndex = siRef.get();

		// Round SI off to two decimals.
		siteIndex = Math.round(siteIndex * 100.0) / 100.0;

		return siteIndex;
	}

	/**
	 * Converts a Height and Site Index to an Age for a particular Site Index Curve.
	 *
	 * @param curve              the particular site index curve to project the height and age along. This curve must be
	 *                           one of the active curves defined in "sindex.h"
	 * @param height             the height of the species in meters.
	 * @param ageType            must be one of:
	 *                           <ul>
	 *                           <li>Height2SiteIndex.SI_AT_TOTAL the age is the total age of the stand in years since
	 *                           planting.
	 *                           <li>Height2SiteIndex.SI_AT_BREAST the age indicates the number of years since the stand
	 *                           reached breast height.
	 *                           </ul>
	 * @param siteIndex          the site index value of the stand.
	 * @param years2BreastHeight the number of years it takes the stand to reach breast height.
	 *
	 * @return the age of the stand (given the ageType) at which point it has reached the height specified.
	 */
	public static double heightAndSiteIndexToAge(
			SiteIndexEquation curve, double height, SiteIndexAgeType ageType, double siteIndex,
			double years2BreastHeight
	) throws CommonCalculatorException {

		Reference<Double> tempRef_rtrn = new Reference<>();

		// This call always returns 0; in the event of an error, an exception is thrown.
		Sindxdll.HtSIToAge(curve, height, ageType, siteIndex, years2BreastHeight, tempRef_rtrn);

		return tempRef_rtrn.get();
	}

	/**
	 * Converts an Age and Site Index to a Height for a particular Site Index Curve.
	 *
	 * @param curve              the particular site index curve to project the height and age along. This curve must be
	 *                           one of the active curves defined in "sindex.h"
	 *
	 * @param age                the age of the trees indicated by the curve selection. The interpretation of this age
	 *                           is modified by the 'ageType' parameter.
	 *
	 * @param ageType            must be one of:
	 *                           <ul>
	 *                           <li>Height2SiteIndex.SI_AT_TOTAL the age is the total age of the stand in years since
	 *                           planting.
	 *                           <li>Height2SiteIndex.SI_AT_BREAST the age indicates the number of years since the stand
	 *                           reached breast height.
	 *                           </ul>
	 * @param siteIndex          the site index value of the stand.
	 * @param years2BreastHeight the number of years it takes the stand to reach breast height.
	 *
	 * @return the height of the stand given the height and site index.
	 *
	 * @throws CommonCalculatorException
	 */
	public static double ageAndSiteIndexToHeight(
			SiteIndexEquation curve, double age, SiteIndexAgeType ageType, double siteIndex, double years2BreastHeight
	) throws CommonCalculatorException {

		Reference<Double> tempRef_rtrn = new Reference<>();

		// This call always returns 0; if an error occurs, an exception is thrown.
		Sindxdll.AgeSIToHt(curve, age, ageType, siteIndex, years2BreastHeight, tempRef_rtrn);

		return tempRef_rtrn.get();
	}

	/**
	 * Calculates the number of years a stand takes to grow from seed to breast height.
	 *
	 * @param curve     the particular site index curve to project the height and age along. This curve must be one of
	 *                  the active curves defined in "sindex.h"
	 * @param siteIndex the site index value of the stand.
	 * @return the number of years to grow from seed to breast height.
	 * @throws CommonCalculatorException in the event of an error
	 */
	public static double yearsToBreastHeight(SiteIndexEquation curve, double siteIndex)
			throws CommonCalculatorException {
		double rtrn = 0.0;

		Reference<Double> tempRef_rtrn = new Reference<>(rtrn);

		// This call always returns 0; if an error occurs, an exception is thrown.
		Sindxdll.Y2BH(curve, siteIndex, tempRef_rtrn);

		rtrn = tempRef_rtrn.get();

		// Round off to 1 decimal.
		rtrn = Math.round(rtrn * 10.0) / 10.0;

		return rtrn;
	}

	/**
	 * Returns the name of a particular curve.
	 *
	 * @param siCurve the site index curve to get the name of.
	 * @return string corresponding the name of the supplied curve number. "Unknown Curve" is returned for unrecognized
	 *         curves.
	 */
	public static String getSICurveName(SiteIndexEquation siCurve) {
		String retStr;

		try {
			retStr = Sindxdll.CurveName(siCurve);
		} catch (CurveErrorException e) {
			retStr = UNKNOWN_CURVE_RESULT;
		}

		return retStr;
	}

	public static int getNumSpecies() {
		return VdypMethods.getNumDefinedSpecies();
	}

	public static String getSpeciesShortName(int sp64Index) {
		return VdypMethods.getSpeciesShortName(SP64Name.forIndex(sp64Index));
	}

	public static int getSpeciesIndex(String spcsCodeName) {
		return VdypMethods.speciesIndex(spcsCodeName);
	}

	public static String getSpeciesFullName(String spcsCodeName) {
		return VdypMethods.getSpeciesFullName(SP64Name.forText(spcsCodeName));
	}

	public static String getSpeciesLatinName(String spcsCodeName) {
		return VdypMethods.getSpeciesLatinName(SP64Name.forText(spcsCodeName));
	}

	public static String getSpeciesGenusCode(String spcsCodeName) {
		return VdypMethods.getSpeciesGenus(SP64Name.forText(spcsCodeName));
	}

	public static String getSpeciesSINDEXCode(String spcsCode, boolean isCoastal) {
		return VdypMethods.getSINDEXSpecies(spcsCode, isCoastal ? SpeciesRegion.COAST : SpeciesRegion.INTERIOR);
	}

	public static String getSpeciesVDYP7Code(String sp64CodeName) {
		return VdypMethods.getVDYP7Species(sp64CodeName);
	}

	/**
	 * Sets the Site Index curve to use for a particular species.
	 *
	 * @param sp64CodeName the short ("code") name of the species.
	 * @param coastalInd   if <code>true</code>, the Coastal region is used and otherwise Interior is used.
	 * @param siCurve      the site index curve to use for the specified species. -1 resets the curve to the default.
	 *
	 * @return the previous value.
	 */
	public static SiteIndexEquation setSICurve(String sp64CodeName, boolean coastalInd, SiteIndexEquation siCurve) {

		SpeciesRegion region = (coastalInd ? SpeciesRegion.COAST : SpeciesRegion.INTERIOR);
		return VdypMethods.setCurrentSICurve(sp64CodeName, region, siCurve);
	}

	/**
	 * Maps a Species code name to a specific SI Curve.
	 *
	 * @param sp64CodeName the species short ("code") name.
	 * @param isCoastal    <code>true</code> if coastal, <code>false</code> if interior.
	 * @return the SI Curve number for the species, or -1 if the species was not recognized.
	 */
	public static SiteIndexEquation getSICurve(String sp64CodeName, boolean isCoastal) {

		return VdypMethods.getCurrentSICurve(sp64CodeName, isCoastal ? SpeciesRegion.COAST : SpeciesRegion.INTERIOR);
	}

	/**
	 * Converts a SI Curve number to a Species code name, or "" if the SI Curve number is not recognized.
	 *
	 * @param siCurve the SI Curve number for the species
	 * @return the short ("code") name of the species, in SIndex33 format (leading character in upper case; following
	 *         characters in lower case.)
	 */
	public static String getSiteCurveSINDEXSpecies(SiteIndexEquation siCurve) {

		String speciesName;

		SiteIndexSpecies species = VdypMethods.getSICurveSpeciesIndex(siCurve);
		if (species != SiteIndexSpecies.SI_NO_SPECIES) {
			speciesName = Sindxdll.SpecCode(species);
		} else {
			speciesName = "";
		}

		return speciesName;
	}

	/**
	 * For a specific species, returns the default Crown Closure associated with that species within a particular region
	 * of the province.
	 *
	 * @param spName    the short ("code") name to be looked up.
	 * @param isCoastal if <code>true</code>, region is Coastal. Otherwise, the region is Interior.
	 *
	 * @return the default CC associated with the species in that particular region and -1.0 if the species was not
	 *         recognized or no default CC has been assigned to that species and region.
	 */
	public static float getSpeciesDefaultCrownClosure(String sp64CodeName, boolean isCoastal) {
		return VdypMethods
				.getDefaultCrownClosure(sp64CodeName, (isCoastal ? SpeciesRegion.COAST : SpeciesRegion.INTERIOR));
	}

	/**
	 * Compute the third of three age values given two of the others. Exactly one of these parameters must be -9.0. The
	 * other two must be set to valid values. If this does not hold, then nothing is computed.
	 * <p>
	 * This routine implements the equation:
	 * <p>
	 * Total Age = Breast Height Age + YTBH - 0.5
	 * <p>
	 * As this equation recently changed, it was thought to place it in a single routine to automatically keep all the
	 * individualcalculations up to date. Previously, all places that needed to calculate the third value, did this
	 * "in-place" and now all have to be seartched for and modified.
	 * <p>
	 * The following note from Gordon Nigh explains the rationale behind the 0.5 half year age correction. It is dated
	 * June 11, 2003 and was received from Cam embedded in his e-mail dated January 21, 2004:
	 * <p>
	 * Further comments: there has been some confusion about the issues surrounding age in Sindex. I would like to
	 * clarify them here, and make a proposition.
	 * <ol>
	 * <li>The newer ministry recommended height-age curves have the 0.5 age correction. These curves have been
	 * developed internally. The older curves do not have the age correction. This correction (documented in Research
	 * Report 03) was implemented to make the models consistent with the definition of breast height age, which is the
	 * number of annual growth rings at breast height. Since the innermost ring represents a half years growth (on
	 * average), the height-age models acutally go through a height of 1.3 at breast height age 0.5, not age 0 (it also
	 * means that site index is the height of the tree at 49.5 growing seasons after the tree reaches breast height).
	 * Therefore, the age adjustment explicitiy incorporates this assumption (i.e., tree reaches breast height midway
	 * through the growing season) into the height-age model.
	 * <li>Logic has it that if we make this assumption for the height-age curves, then we should make the same
	 * assumption for the years-to-breast-height functions as well. That is, if the years-to-breast-height functions
	 * give us the number of years to reach breast height, then this number should end in 0.5. To do this, we had KenP
	 * truncate the decimal part of the years to breast height estimate, and then add 0.5.
	 * <li>Total age is just the number of years the tree has been growing. If we add breast height age to
	 * years-to-breast-height, we get xx.5 (see item 2 above) but we should be getting a whole number. This 0.5 year
	 * "error" results because of the definition of breast height age. At breast height age nn the tree has only been
	 * growing nn-0.5 years since it reached breast height (e.g. it has only been growing 0.5 years since it reached
	 * breast height at breast height age 1). Therefore,
	 *
	 * Total Age = Breast Height Age + Years-to-Breast-Height - 0.5.
	 * <li>It seems to me that if we accept item 1 above (and I think it is generally accepted now since it is logically
	 * correct), then items 2 and 3 should follow automatically to make the systems internally consistent with respect
	 * to the assumptions.
	 * </ol>
	 *
	 * @param rTotalAge        total age of the stand, or -9.0 if unknown
	 * @param rBreastHeightAge breast height age of the stand, or -9.0 if unknown
	 * @param rYTBH            years to breast height of the stand, or -9.0 if unknown
	 *
	 * @return always returns zero. In the future, this routine will return an error code, but none has been defined for
	 *         this library yet.
	 */
	public static int
			fillInAgeTriplet(Reference<Double> rTotalAge, Reference<Double> rBreastHeightAge, Reference<Double> rYTBH) {

		int rtrnCode = 0;

		// Ensure the parameters have values - one of them must be set to "unknown".
		if (rTotalAge.isPresent() && rBreastHeightAge.isPresent() && rYTBH.isPresent()) {
			// All the parameters are supplied, perform the calculation based on which of the two values are supplied.

			// Note that because BHAge can be negative, we must use the less restrictive test of not being equal to

			if (rTotalAge.get() >= 0.0 && rBreastHeightAge.get() != -9.0 && rYTBH.get() < 0.0 /* unknown */) {
				rYTBH.set(rTotalAge.get() - rBreastHeightAge.get() + 0.5);
			} else if (rTotalAge.get() >= 0.0 && rBreastHeightAge.get() == -9.0 /* unknown */ && rYTBH.get() >= 0.0) {
				rBreastHeightAge.set(rTotalAge.get() - rYTBH.get() + 0.5);
			} else if (rTotalAge.get() < 0.0 /* unknown */ && rBreastHeightAge.get() != -9.0 && rYTBH.get() >= 0.0) {
				rTotalAge.set(rBreastHeightAge.get() + rYTBH.get() - 0.5);
			}
		}

		return rtrnCode;
	}
}