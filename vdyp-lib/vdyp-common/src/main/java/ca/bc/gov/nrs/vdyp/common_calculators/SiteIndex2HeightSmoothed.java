package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDC_BRUCEAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_WILEYAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PLI_THROWER;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_GOUDIE_NATAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_GOUDIE_PLAAC;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.*;

/**
 * SiteIndex2HeightSmoothed
 *
 * Defines {@code index_to_height_smoothed}
 */
public class SiteIndex2HeightSmoothed {

	/**
	 * @param cuIndex
	 * @param age
	 * @param ageType
	 * @param siteIndex
	 * @param yearsToBreastHeight
	 * @param seedlingAge
	 * @param seedlingHeight
	 * @return
	 * @throws LessThan13Exception when {@code site_index} is less than 1.3
	 * @throws NoAnswerException   when the iteration will not converge
	 */
	public static double indexToHeightSmoothed(
			SiteIndexEquation cuIndex, double age, SiteIndexAgeType ageType, double siteIndex,
			double yearsToBreastHeight, double seedlingAge, double seedlingHeight
	) throws CommonCalculatorException {

		double result; // return value

		double tage; // total age
		double bhage; // breast-height age
		double pi; // proportion of height growth between breast height
					// ages 0 and 1 that occurs below breast height

		if (siteIndex < 1.3) {
			throw new LessThan13Exception("Site index < 1.3m: " + siteIndex);
		}

		if (yearsToBreastHeight < 0) {
			throw new NoAnswerException(
					"Iteration could not converge (projected height > 999), y2bh: " + yearsToBreastHeight
			);
		}

		double totalAge = age;
		if (ageType == SI_AT_BREAST) {
			totalAge = age + yearsToBreastHeight;
		}
		if (totalAge < 0.0) {
			throw new NoAnswerException("Iteration could not converge (projected height > 999), itage: " + totalAge);
		}
		if (totalAge < 0.00001) {
			return 0.0;
		}

		if (cuIndex == null) {
			throw new NoAnswerException("cuIndex is null");
		}

		if (cuIndex == SI_PLI_THROWER || cuIndex == SI_SW_GOUDIE_PLAAC || cuIndex == SI_SW_GOUDIE_NATAC
				|| cuIndex == SI_FDC_BRUCEAC || cuIndex == SI_HWC_WILEYAC) {
			pi = yearsToBreastHeight - (int) yearsToBreastHeight;
		} else {
			pi = 0.5;
		}

		double k0;
		double k1;

		bhage = 2;
		do {
			result = SiteIndex2Height.indexToHeight(cuIndex, bhage, SI_AT_BREAST, siteIndex, yearsToBreastHeight, pi);
			if (result < 0) {
				return result;
			}
			tage = bhage + (int) yearsToBreastHeight;
			k1 = Math.log( (1.3 - seedlingHeight) / (result - seedlingHeight))
					/ Math.log( (yearsToBreastHeight - seedlingAge) / (tage - seedlingAge));
			if (k1 >= 1) {
				k0 = (1.3 - seedlingHeight) / Math.pow(yearsToBreastHeight - seedlingAge, k1);
				break;
			}
			bhage++;
			if (bhage >= 25) {
				throw new NoAnswerException(
						"Iteration could not converge (projected height > 999), bhage >= 25: " + bhage
				);
			}
		} while (true);

		if (seedlingAge == 0) {
			if (totalAge <= tage) {
				result = k0 * Math.pow(totalAge, k1);
			} else {
				result = SiteIndex2Height
						.indexToHeight(cuIndex, totalAge, SI_AT_TOTAL, siteIndex, yearsToBreastHeight, pi);
			}
		} else {
			if (totalAge < seedlingAge) {
				result = seedlingHeight / seedlingAge * totalAge;
			} else if (totalAge < tage) {
				result = seedlingHeight + k0 * Math.pow(totalAge - seedlingAge, k1);
			} else {
				result = SiteIndex2Height
						.indexToHeight(cuIndex, totalAge, SI_AT_TOTAL, siteIndex, yearsToBreastHeight, pi);
			}
		}

		return result;
	}
}
