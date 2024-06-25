package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.SiteIndexUtilities.llog;
import static ca.bc.gov.nrs.vdyp.common_calculators.SiteIndexUtilities.ppow;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_BREAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_TOTAL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEstimationType.SI_EST_DIRECT;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptMinimumException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;

/**
 * SiteIndex2Height.java - given site index and age, computes site height.
 */
@SuppressWarnings("java:S1479")
public class SiteIndex2Height {

	/**
	 * Given site index and age, computes site height.
	 * <ul>
	 * <li><code>age</code> can be given as total age or breast height age
	 * <li>if total age is given, years2BreastHeight must be the number of years to breast height
	 * <li>all heights input/output are in metres.
	 * <li>site index must be based on breast height age 50
	 * <li>where breast height age is less than 0, a quadratic function is used
	 * </ul>
	 *
	 * @param cuIndex            the index of the site curve
	 * @param age                the current age, of type <code>ageType</code>
	 * @param ageType            one of SI_AT_TOTAL or SI_AT_BREAST
	 * @param siteIndex          the site index
	 * @param years2BreastHeight if <code>ageType</code> is SI_AT_TOTAL, this value must be supplied and indicates years
	 *                           to breast height
	 * @param pi                 proportion of height growth between breast height ages 0 and 1 that occurs below breast
	 *                           height
	 * @returns as described
	 * @throws LessThan13Exception site index < 1.3m
	 * @throws CurveErrorException when cuIndex does not identify a known curve. error codes (returned as height value):
	 */
	public static double indexToHeight(
			SiteIndexEquation cuIndex, double age, SiteIndexAgeType ageType, double siteIndex,
			double years2BreastHeight, double pi
	) throws CommonCalculatorException {
		double height; // return value
		double x1, x2, x3, x4, x5; // equation coefficients
		double totalAge; // total age
		double breastHeightAge; // breast-height age

		if (siteIndex < 1.3) {
			throw new LessThan13Exception("Site index < 1.3m: " + siteIndex);
		}

		// should this line be removed?
		years2BreastHeight = ((int) years2BreastHeight) + 0.5;

		if (ageType == SI_AT_TOTAL) {
			totalAge = age;
			breastHeightAge = AgeToAge.ageToAge(cuIndex, totalAge, SI_AT_TOTAL, SI_AT_BREAST, years2BreastHeight);
		} else {
			breastHeightAge = age;
			totalAge = AgeToAge.ageToAge(cuIndex, breastHeightAge, SI_AT_BREAST, SI_AT_TOTAL, years2BreastHeight);
		}
		if (totalAge < 0.0) {
			throw new NoAnswerException("Iteration could not converge (projected height > 999), age: " + totalAge);
		}
		if (totalAge < 0.00001) {
			return 0.0;
		}

		if (cuIndex == null) {
			throw new CurveErrorException("Unknown curve index");
		}

		switch (cuIndex) {
		case SI_FDC_COCHRAN:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = Math.log(breastHeightAge);
				x1 = Math.exp(-0.37496 + 1.36164 * x1 - 0.00243434 * ppow(x1, 4));
				x2 = -0.2828 + 1.87947 * ppow(1 - Math.exp(-0.022399 * breastHeightAge), 0.966998);

				height = 4.5 + x1 - x2 * (79.97 - (siteIndex - 4.5));

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_FDC_KING:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 2500 / (siteIndex - 4.5);

				x2 = -0.954038 + 0.109757 * x1;
				x3 = 0.0558178 + 0.00792236 * x1;
				x4 = -0.000733819 + 0.000197693 * x1;

				height = 4.5 + breastHeightAge * breastHeightAge
						/ (x2 + x3 * breastHeightAge + x4 * breastHeightAge * breastHeightAge);

				if (breastHeightAge < 5) {
					height += (0.22 * breastHeightAge);
				}

				if (breastHeightAge >= 5 && breastHeightAge < 10) {
					height += (2.2 - 0.22 * breastHeightAge);
				}

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_HWC_FARR:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = Math.log(breastHeightAge);

				x2 = 0.3621734 + 1.149181 * x1 - 0.005617852 * ppow(x1, 3.0) - 7.267547E-6 * ppow(x1, 7.0)
						+ 1.708195E-16 * ppow(x1, 22.0) - 2.482794E-22 * ppow(x1, 30.0);

				x3 = -2.146617 - 0.109007 * x1 + 0.0994030 * ppow(x1, 3.0) - 0.003853396 * ppow(x1, 5.0)
						+ 1.193933E-8 * ppow(x1, 12.0) - 9.486544E-20 * ppow(x1, 27.0) + 1.431925E-26 * ppow(x1, 36.0);

				height = 4.5 + Math.exp(x2) - Math.exp(x3) * (83.20 - (siteIndex - 4.5));

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_HWC_BARKER: {
			double si50t;

			/*
			 * convert from SI 50b to SI 50t
			 */
			si50t = -10.45 + 1.30049 * siteIndex - 0.0022 * siteIndex * siteIndex;

			height = Math.exp(4.35753) * ppow(si50t / Math.exp(4.35753), ppow(50.0 / totalAge, 0.756313));
		}
			break;
		case SI_HM_MEANS:
			if (breastHeightAge > 0.0) {
				/* convert to base 100 */
				siteIndex = -1.73 + 3.149 * ppow(siteIndex, 0.8279);

				height = 1.37 + (22.87 + 0.9502 * (siteIndex - 1.37)) * ppow(
						1 - Math.exp(-0.0020647 * ppow(siteIndex - 1.37, 0.5) * breastHeightAge),
						1.3656 + 2.046 / (siteIndex - 1.37)
				);
			} else
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			break;
		case SI_HM_MEANSAC:
			if (breastHeightAge > 0.5) {
				/* convert to base 100 */
				siteIndex = -1.73 + 3.149 * ppow(siteIndex, 0.8279);

				height = 1.37 + (22.87 + 0.9502 * (siteIndex - 1.37)) * ppow(
						1 - Math.exp(-0.0020647 * ppow(siteIndex - 1.37, 0.5) * (breastHeightAge - 0.5)),
						1.3656 + 2.046 / (siteIndex - 1.37)
				);
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't find the constant
		/*
		 * case SI_HM_WILEY: if (bhage > 0.0){ if (site_index > 60 + 1.667 * bhage){ // function starts going nuts at
		 * high sites and low ages // evaluate at a safe age, and interpolate x1 = (site_index - 60) / 1.667 + 0.1; x2 =
		 * index_to_height (cu_index, x1, SI_AT_BREAST, site_index, y2bh, pi); height = 1.37 + (x2-1.37) * bhage / x1;
		 * break; }
		 *
		 * // convert to imperial site_index /= 0.3048;
		 *
		 * x1 = 2500 / (site_index - 4.5);
		 *
		 * x2 = -1.7307 + 0.1394 * x1; x3 = -0.0616 + 0.0137 * x1; x4 = 0.00192428 + 0.00007024 * x1;
		 *
		 * height = 4.5 + bhage * bhage / (x2 + x3 * bhage + x4 * bhage * bhage);
		 *
		 * if (bhage < 5){ height += (0.3 * bhage); } else if (bhage < 10){ height += (3.0 - 0.3 * bhage); }
		 *
		 * // convert back to metric height *= 0.3048;
		 *
		 * }else{ height = tage * tage * 1.37 / y2bh / y2bh; } break;
		 */

		case SI_HWC_WILEY:
			if (breastHeightAge > 0.0) {
				if (siteIndex > 60 + 1.667 * breastHeightAge) {
					// function starts going nuts at high sites and low ages
					// evaluate at a safe age, and interpolate
					x1 = (siteIndex - 60) / 1.667 + 0.1;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.37 + (x2 - 1.37) * breastHeightAge / x1;
					break;
				}

				// convert to imperial
				siteIndex /= 0.3048;

				x1 = 2500 / (siteIndex - 4.5);

				x2 = -1.7307 + 0.1394 * x1;
				x3 = -0.0616 + 0.0137 * x1;
				x4 = 0.00192428 + 0.00007024 * x1;

				height = 4.5 + breastHeightAge * breastHeightAge
						/ (x2 + x3 * breastHeightAge + x4 * breastHeightAge * breastHeightAge);

				if (breastHeightAge < 5) {
					height += (0.3 * breastHeightAge);
				} else if (breastHeightAge < 10) {
					height += (3.0 - 0.3 * breastHeightAge);
				}

				// convert back to metric
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_HWC_WILEY_BC:
			if (breastHeightAge > 0.0) {
				if (siteIndex > 60 + 1.667 * breastHeightAge) {
					// function starts going nuts at high sites and low ages
					// evaluate at a safe age, and interpolate
					x1 = (siteIndex - 60) / 1.667 + 0.1;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.37 + (x2 - 1.37) * breastHeightAge / x1;
					break;
				}

				// convert to imperial
				siteIndex /= 0.3048;

				x1 = 2500 / (siteIndex - 4.5);

				x2 = -1.7307 + 0.1394 * x1;
				x3 = -0.0616 + 0.0137 * x1;
				x4 = 0.00192428 + 0.00007024 * x1;

				height = 4.5 + breastHeightAge * breastHeightAge
						/ (x2 + x3 * breastHeightAge + x4 * breastHeightAge * breastHeightAge);

				if (breastHeightAge < 5) {
					height += (0.3 * breastHeightAge);
				} else if (breastHeightAge < 10) {
					height += (3.0 - 0.3 * breastHeightAge);
				}

				// convert back to metric
				height *= 0.3048;

				if (cuIndex == SiteIndexEquation.SI_HWC_WILEY_BC) {
					x1 = -1.34105 + 0.0009 * breastHeightAge * height;
					if (x1 > 0.0) {
						height -= x1;
					}
				}
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_HWC_WILEY_MB:
			if (breastHeightAge > 0.0) {
				if (siteIndex > 60 + 1.667 * breastHeightAge) {
					// function starts going nuts at high sites and low ages
					// evaluate at a safe age, and interpolate
					x1 = (siteIndex - 60) / 1.667 + 0.1;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.37 + (x2 - 1.37) * breastHeightAge / x1;
					break;
				}

				// convert to imperial
				siteIndex /= 0.3048;

				x1 = 2500 / (siteIndex - 4.5);

				x2 = -1.7307 + 0.1394 * x1;
				x3 = -0.0616 + 0.0137 * x1;
				x4 = 0.00192428 + 0.00007024 * x1;

				height = 4.5 + breastHeightAge * breastHeightAge
						/ (x2 + x3 * breastHeightAge + x4 * breastHeightAge * breastHeightAge);

				if (breastHeightAge < 5) {
					height += (0.3 * breastHeightAge);
				} else if (breastHeightAge < 10) {
					height += (3.0 - 0.3 * breastHeightAge);
				}

				// convert back to metric
				height *= 0.3048;

				if (cuIndex == SiteIndexEquation.SI_HWC_WILEY_MB) {
					x1 = 0.0972129 + 0.000419315 * breastHeightAge * height;
					height -= x1;
				}
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_HWC_WILEYAC:
			if (breastHeightAge >= pi) {
				if (siteIndex > 60 + 1.667 * (breastHeightAge - pi)) {
					/* function starts going nuts at high sites and low ages */
					/* evaluate at a safe age, and interpolate */
					x1 = (siteIndex - 60) / 1.667 + 0.1 + pi;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.37 + (x2 - 1.37) * (breastHeightAge - pi) / x1;
					break;
				}

				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = Math.pow(49 + (1 - pi), 2.0) / (siteIndex - 4.5);

				x2 = -1.7307 + 0.1394 * x1;
				x3 = -0.0616 + 0.0137 * x1;
				x4 = 0.00195078 + 0.00007446 * x1;
				x5 = breastHeightAge - pi;
				height = 4.5 + x5 * x5 / (x2 + x3 * x5 + x4 * x5 * x5);

				if (x5 < 5) {
					height += (0.3 * x5);
				} else if (x5 < 10) {
					height += (3.0 - 0.3 * x5);
				}

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}

			break;
		case SI_BP_CURTIS:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = Math.log(siteIndex - 4.5) + 1.649871 * (Math.log(breastHeightAge) - Math.log(50))
						+ 0.147245 * Math.pow(Math.log(breastHeightAge) - Math.log(50), 2.0);
				x2 = 1.0 + 0.164927 * (Math.log(breastHeightAge) - Math.log(50))
						+ 0.052467 * Math.pow(Math.log(breastHeightAge) - Math.log(50), 2.0);
				height = 4.5 + Math.exp(x1 / x2);

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_BP_CURTISAC:
			if (breastHeightAge > 0.5) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = Math.log(siteIndex - 4.5) + 1.649871 * (Math.log(breastHeightAge - 0.5) - Math.log(49.5))
						+ 0.147245 * Math.pow(Math.log(breastHeightAge - 0.5) - Math.log(49.5), 2.0);
				x2 = 1.0 + 0.164927 * (Math.log(breastHeightAge - 0.5) - Math.log(49.5))
						+ 0.052467 * Math.pow(Math.log(breastHeightAge - 0.5) - Math.log(49.5), 2.0);
				height = 4.5 + Math.exp(x1 / x2);

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_GOUDNIGH:
			if (siteIndex < 19.5) {
				if (breastHeightAge > 0.5) {
					/* Goudie */
					x1 = (1.0 + Math.exp(9.7936 - 1.2866 * llog(siteIndex - 1.3) - 1.4661 * Math.log(49.5)))
							/ (1.0 + Math.exp(
									9.7936 - 1.2866 * llog(siteIndex - 1.3) - 1.4661 * Math.log(breastHeightAge - 0.5)
							));

					height = 1.3 + (siteIndex - 1.3) * x1;
				} else {
					height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
				}
			} else {
				if (totalAge < years2BreastHeight - 0.5) {
					/* use Nigh's total age curve */
					height = (-0.01666 + 0.001722 * siteIndex) * ppow(totalAge, 1.858) * ppow(0.9982, totalAge);
				} else if (totalAge > years2BreastHeight + 2 - 0.5) {
					/* use Goudie's breast-height age curve */
					x1 = (1.0 + Math.exp(9.7936 - 1.2866 * llog(siteIndex - 1.3) - 1.4661 * Math.log(49.5)))
							/ (1.0 + Math.exp(
									9.7936 - 1.2866 * llog(siteIndex - 1.3) - 1.4661 * Math.log(breastHeightAge - 0.5)
							));

					height = 1.3 + (siteIndex - 1.3) * x1;
				} else {
					/* use Nigh's total age curve */
					x4 = (-0.01666 + 0.001722 * siteIndex) * ppow(years2BreastHeight - 0.5, 1.858)
							* ppow(0.9982, years2BreastHeight - 0.5);

					/* use Goudie's breast-height age curve */
					x1 = (1.0 + Math.exp(9.7936 - 1.2866 * llog(siteIndex - 1.3) - 1.4661 * Math.log(49.5)))
							/ (1.0 + Math.exp(9.7936 - 1.2866 * llog(siteIndex - 1.3) - 1.4661 * Math.log(2 - 0.5)));

					x5 = 1.3 + (siteIndex - 1.3) * x1;

					height = x4 + (x5 - x4) * breastHeightAge / 2.0;
				}
			}
			break;
		case SI_PLI_THROWNIGH:
			if (siteIndex < 18.5) {
				if (breastHeightAge > 0.5) {
					x1 = (1.0 + Math.exp(7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(49.5)))
							/ (1.0 + Math.exp(
									7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(breastHeightAge - 0.5)
							));

					height = 1.3 + (siteIndex - 1.3) * x1;
				} else {
					height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.8);
				}
			} else {
				if (totalAge < years2BreastHeight - 0.5) {
					/* use Nigh's total age curve */
					height = (-0.03993 + 0.004828 * siteIndex) * ppow(totalAge, 1.902) * ppow(0.9645, totalAge);
				} else if (totalAge > years2BreastHeight + 2 - 0.5) {
					/* use Thrower's breast-height age curve */
					x1 = (1.0 + Math.exp(7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(49.5)))
							/ (1.0 + Math.exp(
									7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(breastHeightAge - 0.5)
							));

					height = 1.3 + (siteIndex - 1.3) * x1;
				} else {
					/* use Nigh's total age curve */
					x4 = (-0.03993 + 0.004828 * siteIndex) * ppow(years2BreastHeight - 0.5, 1.902)
							* ppow(0.9645, years2BreastHeight - 0.5);

					/* use Thrower's breast-height age curve */
					x1 = (1.0 + Math.exp(7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(49.5)))
							/ (1.0 + Math.exp(7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(2 - 0.5)));

					x5 = 1.3 + (siteIndex - 1.3) * x1;

					height = x4 + (x5 - x4) * breastHeightAge / 2.0;
				}
			}
			break;
		case SI_PLI_THROWER:
			if (breastHeightAge > pi) {
				x1 = (1.0 + Math.exp(7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(50 - pi))) / (1.0
						+ Math.exp(7.6298 - 0.8940 * llog(siteIndex - 1.3) - 1.3563 * Math.log(breastHeightAge - pi)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				/*
				 * height = tage * tage * 1.3 / y2bh / y2bh;
				 */
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.77 - 0.1028 * years2BreastHeight)
						* Math.pow(1.179, totalAge - years2BreastHeight);
			}
			break;
		case SI_PLI_NIGHTA2004:
			if (totalAge <= 15) {
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.77 - 0.1028 * years2BreastHeight)
						* Math.pow(1.179, totalAge - years2BreastHeight);
			} else {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			break;
		case SI_PLI_NIGHTA98:
			if (totalAge <= 15) {
				height = (-0.03993 + 0.004828 * siteIndex) * ppow(totalAge, 1.902) * ppow(0.9645, totalAge);
			} else {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			break;
		case SI_SW_NIGHTA2004:
			if (totalAge <= 20) {
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.628 - 0.05991 * years2BreastHeight)
						* Math.pow(1.127, totalAge - years2BreastHeight);
			} else {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			break;
		case SI_SW_NIGHTA:
			if (totalAge <= 20 && siteIndex >= 14.2) {
				height = (-0.01666 + 0.001722 * siteIndex) * ppow(totalAge, 1.858) * ppow(0.9982, totalAge);
			} else {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			break;
		case SI_FDC_NIGHTA:
			if (totalAge <= 25) {
				height = (-0.002355 + 0.0003156 * siteIndex) * ppow(totalAge, 2.861) * ppow(0.9337, totalAge);
			} else {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			break;
		case SI_SE_NIGH:
			if (breastHeightAge > 0.5) {
				// -1.71635 = 1.758 * log (1 - exp (-0.00955 * 49.5))
				// 45.3824 = -4 * 11.6209 * log (1 - exp (-0.00955 * 49.5))
				x1 = 0.5 * ( (Math.log(siteIndex - 1.3) - 1.71635)
						+ Math.sqrt(Math.pow(Math.log(siteIndex - 1.3) - 1.71635, 2.0) + 45.3824));
				height = 1.3 + Math.exp(x1)
						* Math.pow(1 - Math.exp(-0.00955 * (breastHeightAge - 0.5)), -1.758 + 11.6209 / x1);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SE_NIGHTA:
			if (totalAge <= 20) {
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.628 - 0.05991 * years2BreastHeight)
						* Math.pow(1.127, totalAge - years2BreastHeight);
			} else {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			break;
		case SI_FDC_BRUCE:
			// 2009 may 6: force a non-rounded y2bh
			years2BreastHeight = 13.25 - siteIndex / 6.096;

			x1 = siteIndex / 30.48;

			x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));

			x3 = ppow(50.0 + years2BreastHeight, x2);

			x4 = Math.log(1.372 / siteIndex) / (ppow(years2BreastHeight, x2) - x3);

			if (ageType == SI_AT_TOTAL) {
				height = siteIndex * Math.exp(x4 * (ppow(totalAge, x2) - x3));
			} else {
				height = siteIndex * Math.exp(x4 * (ppow(breastHeightAge + years2BreastHeight, x2) - x3));
			}
			break;
		case SI_FDC_BRUCEAC:
			// 2009 may 6: force a non-rounded y2bh
			years2BreastHeight = 13.25 - siteIndex / 6.096;

			x1 = siteIndex / 30.48;

			x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));

			x3 = ppow(49 + (1 - pi) + years2BreastHeight, x2);

			x4 = Math.log(1.372 / siteIndex) / (ppow(years2BreastHeight, x2) - x3);

			if (ageType == SI_AT_TOTAL) {
				height = siteIndex * Math.exp(x4 * (ppow(totalAge, x2) - x3));
			} else {
				height = siteIndex * Math.exp(x4 * (ppow(breastHeightAge + years2BreastHeight - pi, x2) - x3));
			}
			break;
		case SI_FDC_BRUCENIGH:
			// 2009 may 6: force a non-rounded y2bh
			years2BreastHeight = 13.25 - siteIndex / 6.096;

			if (totalAge < 50) {
				/* compute Bruce at age 50 */
				x1 = siteIndex / 30.48;
				x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));
				x3 = ppow(50.0 + years2BreastHeight - 0.5, x2);
				x4 = Math.log(1.372 / siteIndex) / (ppow(years2BreastHeight - 0.5, x2) - x3);
				height = siteIndex * Math.exp(x4 * (ppow(50, x2) - x3));

				/* now smooth it into the Nigh curve */
				x4 = ppow(height * ppow(50, -2.037) / (-0.0123 + 0.00158 * siteIndex), 1.0 / 50);

				height = (-0.0123 + 0.00158 * siteIndex) * ppow(totalAge, 2.037) * ppow(x4, totalAge);
			} else {
				/* compute Bruce */
				x1 = siteIndex / 30.48;
				x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));
				x3 = ppow(50.0 + years2BreastHeight - 0.5, x2);
				x4 = Math.log(1.372 / siteIndex) / (ppow(years2BreastHeight - 0.5, x2) - x3);
				height = siteIndex * Math.exp(x4 * (ppow(totalAge, x2) - x3));
			}
			break;
		case SI_PLI_MILNER:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 96.93 * ppow(1 - Math.exp(-0.01955 * breastHeightAge), 1.216);
				x2 = 1.41 * ppow(1 - Math.exp(-0.02656 * breastHeightAge), 1.297);
				height = 4.5 + x1 + x2 * (siteIndex - 59.6);

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PLI_CIESZEWSKI:
			if (breastHeightAge > 0.0) {
				x1 = 0.20372424;
				x2 = 97.37473618;
				x3 = 20 * x2 / (ppow(50.0, 1 + x1));
				x4 = siteIndex - 1.3
						+ Math.sqrt(
								(siteIndex - 1.3 - x3) * (siteIndex - 1.3 - x3)
										+ 80 * x2 * (siteIndex - 1.3) * ppow(50.0, - (1 + x1))
						);

				height = 1.3 + (x4 + x3) / (2 + 80 * x2 * ppow(breastHeightAge, - (1 + x1)) / (x4 - x3));
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_CIESZEWSKI:
			if (breastHeightAge > 0.0) {
				x1 = 0.3235139;
				x2 = 260.9162652;
				x3 = 20 * x2 / (ppow(50.0, 1 + x1));
				x4 = siteIndex - 1.3
						+ Math.sqrt(
								(siteIndex - 1.3 - x3) * (siteIndex - 1.3 - x3)
										+ 80 * x2 * (siteIndex - 1.3) * ppow(50.0, - (1 + x1))
						);

				height = 1.3 + (x4 + x3) / (2 + 80 * x2 * ppow(breastHeightAge, - (1 + x1)) / (x4 - x3));
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SB_CIESZEWSKI:
			if (breastHeightAge > 0.0) {
				x1 = 0.1992266;
				x2 = 114.8730018;
				x3 = 20 * x2 / (ppow(50.0, 1 + x1));
				x4 = siteIndex - 1.3
						+ Math.sqrt(
								(siteIndex - 1.3 - x3) * (siteIndex - 1.3 - x3)
										+ 80 * x2 * (siteIndex - 1.3) * ppow(50.0, - (1 + x1))
						);

				height = 1.3 + (x4 + x3) / (2 + 80 * x2 * ppow(breastHeightAge, - (1 + x1)) / (x4 - x3));
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_AT_CIESZEWSKI:
			if (breastHeightAge > 0.0) {
				x1 = 0.2644606;
				x2 = 117.3695371;
				x3 = 20 * x2 / (ppow(50.0, 1 + x1));
				x4 = siteIndex - 1.3
						+ Math.sqrt(
								(siteIndex - 1.3 - x3) * (siteIndex - 1.3 - x3)
										+ 80 * x2 * (siteIndex - 1.3) * ppow(50.0, - (1 + x1))
						);

				height = 1.3 + (x4 + x3) / (2 + 80 * x2 * ppow(breastHeightAge, - (1 + x1)) / (x4 - x3));
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_PF_GOUDIE_WET: if (bhage > 0.0){ x1 = -0.935; x2 = 7.81498; x3 = -1.28517;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		// Couldn't find constant
		/*
		 * case SI_PF_GOUDIE_DRY: if (bhage > 0.0){ x1 = -1.00726; x2 = 7.81498; x3 = -1.28517;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		case SI_PLI_GOUDIE_WET:
			if (breastHeightAge > 0.0) {
				x1 = -0.935;
				x2 = 7.81498;
				x3 = -1.28517;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PLI_GOUDIE_DRY:
			if (breastHeightAge > 0.0) {
				x1 = -1.00726;
				x2 = 7.81498;
				x3 = -1.28517;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_PA_GOUDIE_WET: if (bhage > 0.0){ x1 = -0.935; x2 = 7.81498; x3 = -1.28517;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		// Couldn't find constant
		/*
		 * case SI_PA_GOUDIE_DRY: if (bhage > 0.0){ x1 = -1.00726; x2 = 7.81498; x3 = -1.28517;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		case SI_PLI_DEMPSTER:
			if (breastHeightAge > 0.0) {
				x1 = -0.9576;
				x2 = 7.4871;
				x3 = -1.2036;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_SE_GOUDIE_PLA: if (bhage > 0.0){ x1 = -1.2866; x2 = 9.7936; x3 = -1.4661;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		// Couldn't find constant
		/*
		 * case SI_SE_GOUDIE_NAT: if (bhage > 0.0){ x1 = -1.2866; x2 = 9.7936; x3 = -1.4661;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		case SI_SW_GOUDIE_PLA:
			if (breastHeightAge > 0.0) {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_GOUDIE_NAT:
			if (breastHeightAge > 0.0) {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_DEMPSTER:
			if (breastHeightAge > 0.0) {
				x1 = -1.2240;
				x2 = 9.6183;
				x3 = -1.4627;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SB_DEMPSTER:
			if (breastHeightAge > 0.0) {
				x1 = -1.3154;
				x2 = 8.5594;
				x3 = -1.1484;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SS_GOUDIE:
			if (breastHeightAge > 0.0) {
				x1 = -1.5282;
				x2 = 11.0605;
				x3 = -1.5108;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_FDI_THROWER:
			if (breastHeightAge > 0.0) {
				x1 = -0.237724692;
				x2 = 5.780089777;
				x3 = -1.150039266;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_AT_GOUDIE:
			if (breastHeightAge > 0.0) {
				x1 = -0.618;
				x2 = 6.879;
				x3 = -1.32;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't find constant
		/*
		 * case SI_EP_GOUDIE: if (bhage > 0.0){ x1 = -0.618; x2 = 6.879; x3 = -1.32;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		// Couldn't find constant
		/*
		 * case SI_EA_GOUDIE: if (bhage > 0.0){ x1 = -0.618; x2 = 6.879; x3 = -1.32;
		 *
		 * x1 = (1.0 + Math.exp (x2 + x1 * llog(site_index - 1.3) + x3 * Math.log (50.0))) / (1.0 + Math.exp (x2 + x1 *
		 * llog(site_index - 1.3) + x3 * Math.log (bhage)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x1; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		case SI_SW_GOUDIE_NATAC:
			if (breastHeightAge > pi) {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50 - pi)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge - pi)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.628 - 0.05991 * years2BreastHeight)
						* Math.pow(1.127, totalAge - years2BreastHeight);
			}
			break;

		case SI_SW_GOUDIE_PLAAC:
			if (breastHeightAge > pi) {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50 - pi)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge - pi)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.628 - 0.05991 * years2BreastHeight)
						* Math.pow(1.127, totalAge - years2BreastHeight);
			}
			break;
		case SI_FDI_THROWERAC:
			if (breastHeightAge > 0.5) {
				x1 = -0.237724692;
				x2 = 5.780089777;
				x3 = -1.150039266;
				x1 = (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(49.5)))
						/ (1.0 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(breastHeightAge - 0.5)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SS_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 8.947;
				x2 = -1.357;
				x3 = -1.013;

				x1 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));
				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_BA_NIGH:
			if (breastHeightAge > 0.5) {
				x5 = Math.pow(siteIndex - 1.3, 3.0) / 49.5;
				x4 = x5 + Math.pow(x5 * x5 + 16692000.0 * Math.pow(siteIndex - 1.3, 3.0) / 299891.0, 0.5);
				x2 = (8346000.0 + x4 * 6058.412) * Math.pow(breastHeightAge - 0.5, 3.232);
				x3 = (8346000.0 + x4 * Math.pow(breastHeightAge - 0.5, 2.232)) * 299891.0;
				height = 1.3 + (siteIndex - 1.3) * Math.pow(x2 / x3, 1 / 3.0);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_EP_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 9.604;
				x2 = -1.113;
				x3 = -1.849;

				x1 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));
				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_CWI_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 9.474;
				x2 = -1.340;
				x3 = -1.244;

				x1 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));
				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_HWI_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 8.998;
				x2 = -1.434;
				x3 = -1.051;

				x1 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));
				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PY_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 8.519;
				x2 = -1.385;
				x3 = -0.8498;

				x1 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));
				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = (1.3 * Math.pow(totalAge, 1.137) * Math.pow(1.016, totalAge))
						/ (Math.pow(years2BreastHeight, 1.137) * Math.pow(1.016, years2BreastHeight));
			}
			break;
		case SI_ACT_THROWER:
			// case SI_MB_THROWER: Cannot find constant
			if (breastHeightAge > 0.0) {
				x1 = -1.3481;
				x2 = 10.3861;
				x3 = -1.6555;

				x1 = (1.0 + Math.exp(x2 + x3 * llog(siteIndex - 1.3) + x1 * Math.log(50.0)))
						/ (1.0 + Math.exp(x2 + x3 * llog(siteIndex - 1.3) + x1 * Math.log(breastHeightAge)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_ACT_THROWERAC:
			if (breastHeightAge > 0.5) {
				x1 = -1.3481;
				x2 = 10.3861;
				x3 = -1.6555;

				x1 = (1.0 + Math.exp(x2 + x3 * llog(siteIndex - 1.3) + x1 * Math.log(49.5)))
						/ (1.0 + Math.exp(x2 + x3 * Math.log(siteIndex - 1.3) + x1 * Math.log(breastHeightAge - 0.5)));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			break;
		case SI_SB_KER:
			if (breastHeightAge > 0.0) {
				x2 = 0.01741;
				x3 = 8.7428;
				x4 = -0.7346;
				x1 = ppow(1 - Math.exp(-x2 * breastHeightAge), x3 * ppow(siteIndex, x4));
				x2 = ppow(1 - Math.exp(-x2 * 50), x3 * ppow(siteIndex, x4));
				height = 1.3 + (siteIndex - 1.3) * x1 / x2;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_KER_PLA, SI_SW_KER_NAT:
			if (breastHeightAge > 0.0) {
				x2 = 0.02081;
				x3 = 11.1515;
				x4 = -0.7518;
				x1 = ppow(1 - Math.exp(-x2 * breastHeightAge), x3 * ppow(siteIndex, x4));
				x2 = ppow(1 - Math.exp(-x2 * 50), x3 * ppow(siteIndex, x4));
				height = 1.3 + (siteIndex - 1.3) * x1 / x2;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_THROWER:
			if (breastHeightAge > 0.5) {
				x1 = (1.0 + Math.exp(10.1654 - 1.4002 * llog(siteIndex - 1.3) - 1.4482 * Math.log(50.0 - 0.5)))
						/ (1.0 + Math.exp(
								10.1654 - 1.4002 * llog(siteIndex - 1.3) - 1.4482 * Math.log(breastHeightAge - 0.5)
						));

				height = 1.3 + (siteIndex - 1.3) * x1;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SW_HU_GARCIA:
			if (breastHeightAge > 0.5) {
				double q;

				q = huGarciaQ(siteIndex, 50.0);
				height = huGarciaH(q, breastHeightAge);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SS_FARR:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x3 = llog(breastHeightAge);

				x1 = -0.20505 + 1.449615 * x3 - 0.01780992 * ppow(x3, 3.0) + 6.519748E-5 * ppow(x3, 5.0)
						- 1.095593E-23 * ppow(x3, 30.0);

				x2 = -5.61188 + 2.418604 * x3 - 0.259311 * ppow(x3, 2.0) + 1.351445E-4 * ppow(x3, 5.0)
						- 1.701139E-12 * ppow(x3, 16.0) + 7.964197E-27 * ppow(x3, 36.0);

				height = 4.5 + Math.exp(x1) - Math.exp(x2) * (86.43 - (siteIndex - 4.5));

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PW_CURTIS:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 1.0 - Math.exp(
						-Math.exp(
								-9.975053 + (1.747353 - 0.38583) * Math.log(breastHeightAge)
										+ 1.119438 * Math.log(siteIndex)
						)
				);

				x2 = 1.0 - Math.exp(
						-Math.exp(
								-9.975053 + 1.747353 * Math.log(50.0) - 0.38583 * Math.log(breastHeightAge)
										+ 1.119438 * Math.log(siteIndex)
						)
				);

				height = 4.5 + (siteIndex - 4.5) * x1 / x2;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PW_CURTISAC:
			if (breastHeightAge > 0.5) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 1.0 - Math.exp(
						-Math.exp(
								-9.975053 + (1.747353 - 0.38583) * Math.log(breastHeightAge - 0.5)
										+ 1.119438 * Math.log(siteIndex)
						)
				);

				x2 = 1.0 - Math.exp(
						-Math.exp(
								-9.975053 + 1.747353 * Math.log(49.5) - 0.38583 * Math.log(breastHeightAge - 0.5)
										+ 1.119438 * Math.log(siteIndex)
						)
				);

				height = 4.5 + (siteIndex - 4.5) * x1 / x2;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SS_BARKER: {
			double si50t;

			/*
			 * convert from SI 50b to SI 50t
			 */
			si50t = -10.59 + 1.24 * siteIndex - 0.001 * siteIndex * siteIndex;

			height = Math.exp(4.39751) * ppow(si50t / Math.exp(4.39751), ppow(50.0 / totalAge, 0.792329));
		}
			break;
		case SI_CWC_BARKER: {
			double si50t;

			/*
			 * convert from SI 50b to SI 50t
			 */
			si50t = -5.85 + 1.12 * siteIndex;

			height = Math.exp(4.56128) * ppow(si50t / Math.exp(4.56128), ppow(50.0 / totalAge, 0.584627));
		}
			break;
		case SI_CWC_KURUCZ:
			// case SI_YC_KURUCZ: Cannot find constant
			if (breastHeightAge > 0.0) {
				if (siteIndex > 43 + 1.667 * breastHeightAge) {
					/* function starts going nuts at high sites and low ages */
					/* evaluate at a safe age, and interpolate */
					x1 = (siteIndex - 43) / 1.667 + 0.1;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.3 + (x2 - 1.3) * breastHeightAge / x1;
					break;
				}

				if (siteIndex <= 1.3) {
					x1 = 99999.0;
				} else {
					x1 = 2500.0 / (siteIndex - 1.3);
				}

				x2 = -3.11785 + 0.05027 * x1;
				x3 = -0.02465 + 0.01411 * x1;
				x4 = 0.00174 + 0.000097667 * x1;

				height = 1.3 + breastHeightAge * breastHeightAge
						/ (x2 + x3 * breastHeightAge + x4 * breastHeightAge * breastHeightAge);

				if (breastHeightAge > 50.0) {
					if (breastHeightAge > 200) {
						/*
						 * The "standard" correction applied above 50 years would overpower the uncorrected curve at
						 * around 400 years. So, after consultation with Robert Macdonald and Ian Cameron, it was
						 * decided to use a correction beyond 200 years with the same ratio as at age 200.
						 */
						breastHeightAge = 200;
					}
					height = height - (-0.02379545 * height + 0.000475909 * breastHeightAge * height);
				}
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_CWC_KURUCZAC:
			if (breastHeightAge >= 0.5) {
				if (siteIndex > 43 + 1.667 * (breastHeightAge - 0.5)) {
					/* function starts going nuts at high sites and low ages */
					/* evaluate at a safe age, and interpolate */
					x1 = (siteIndex - 43) / 1.667 + 0.1 + 0.5;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.3 + (x2 - 1.3) * (breastHeightAge - 0.5) / x1;
					break;
				}

				if (siteIndex <= 1.3) {
					x1 = 99999.0;
				} else {
					x1 = 2450.25 / (siteIndex - 1.3);
				}

				x2 = -3.11785 + 0.05027 * x1;
				x3 = -0.02465 + 0.01411 * x1;
				x4 = 0.00177044 + 0.000102554 * x1;
				x5 = breastHeightAge - 0.5;
				height = 1.3 + x5 * x5 / (x2 + x3 * x5 + x4 * x5 * x5);

				if (breastHeightAge > 50.0) {
					if (breastHeightAge > 200) {
						/*
						 * The "standard" correction applied above 50 years would overpower the uncorrected curve at
						 * around 400 years. So, after consultation with Robert Macdonald and Ian Cameron, it was
						 * decided to use a correction beyond 200 years with the same ratio as at age 200.
						 */
						breastHeightAge = 200;
					}
					height = height - (-0.02379545 * height + 0.000475909 * breastHeightAge * height);
				}
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_CWC_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = -3.004284755 + 2.5332489439 * siteIndex - 0.019027688 * siteIndex * siteIndex
						+ 0.0000992968 * Math.pow(siteIndex, 3.0);
				height = 1.3 + x1 * Math.pow(1 - Math.exp(-0.01449 * (breastHeightAge - 0.5)), 1.4026 - 0.005781 * x1);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_BA_DILUCCA:
			if (breastHeightAge > 0.0) {
				x1 = 1 + Math.exp(8.377148582 - 1.27351813 * Math.log(50.0) - 0.975226632 * Math.log(siteIndex));
				x2 = 1 + Math
						.exp(8.377148582 - 1.27351813 * Math.log(breastHeightAge) - 0.975226632 * Math.log(siteIndex));
				height = 1.3 + (siteIndex - 1.3) * x1 / x2;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_BB_KER:
			if (breastHeightAge > 0.0) {
				x2 = 0.01373;
				x3 = 6.1299;
				x4 = -0.6157;
				x1 = ppow(1 - Math.exp(-x2 * breastHeightAge), x3 * ppow(siteIndex, x4));
				x2 = ppow(1 - Math.exp(-x2 * 50), x3 * ppow(siteIndex, x4));
				height = 1.3 + (siteIndex - 1.3) * x1 / x2;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_BA_KURUCZ86:
			if (breastHeightAge > 0.0) {
				x1 = (siteIndex - 1.3) * ppow(1.0 - Math.exp(-0.01303 * breastHeightAge), 1.024971);

				height = 1.3 + x1 / 0.470011;

				if (breastHeightAge <= 50.0) {
					height -= (4 * 0.4 * breastHeightAge * (50 - breastHeightAge) / 2500);
				}
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_BA_KURUCZ82, SI_BL_KURUCZ82:
			// case SI_BG_KURUCZ82: Cannot find constants
			if (breastHeightAge > 0.0) {
				if (siteIndex > 60 + 1.667 * breastHeightAge) {
					/* function starts going nuts at high sites and low ages */
					/* evaluate at a safe age, and interpolate */
					x1 = (siteIndex - 60) / 1.667 + 0.1;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.3 + (x2 - 1.3) * breastHeightAge / x1;
					break;
				}

				if (siteIndex <= 1.3) {
					x1 = 99999.0;
				} else {
					x1 = 2500.0 / (siteIndex - 1.3);
				}

				x2 = -2.34655 + 0.0565 * x1;
				x3 = -0.42007 + 0.01687 * x1;
				x4 = 0.00934 + 0.00004 * x1;

				height = 1.3 + breastHeightAge * breastHeightAge
						/ (x2 + x3 * breastHeightAge + x4 * breastHeightAge * breastHeightAge);

				if (breastHeightAge < 50.0 && breastHeightAge * height < 1695.3) {
					x1 = 0.45773 - 0.00027 * breastHeightAge * height;
					if (x1 > 0.0) {
						height -= x1;
					}
				}
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;

				x1 = 0.45773 - 0.00027 * totalAge * height; /* flaw? total vs bh-age */
				if (x1 > 0.0)
					height -= x1;
			}
			break;

		case SI_BA_KURUCZ82AC:
			if (breastHeightAge >= 0.5) {
				if (siteIndex > 60 + 1.667 * (breastHeightAge - 0.5)) {
					/* function starts going nuts at high sites and low ages */
					/* evaluate at a safe age, and interpolate */
					x1 = (siteIndex - 60) / 1.667 + 0.1 + 0.5;
					x2 = indexToHeight(cuIndex, x1, SI_AT_BREAST, siteIndex, years2BreastHeight, pi);
					height = 1.3 + (x2 - 1.3) * (breastHeightAge - 0.5) / x1;
					break;
				}

				if (siteIndex <= 1.3) {
					x1 = 99999.0;
				} else {
					x1 = 2450.25 / (siteIndex - 1.3);
				}

				x2 = -2.09187 + 0.066925 * x1;
				x3 = -0.42007 + 0.01687 * x1;
				x4 = 0.00934 + 0.00004 * x1;
				x5 = breastHeightAge - 0.5;
				height = 1.3 + x5 * x5 / (x2 + x3 * x5 + x4 * x5 * x5);

				if (breastHeightAge < 50.0 && breastHeightAge * height < 1695.3) {
					x1 = 0.45773 - 0.00027 * breastHeightAge * height;
					if (x1 > 0.0) {
						height -= x1;
					}
				}
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;

				x1 = 0.45773 - 0.00027 * totalAge * height; /* flaw? total vs bh-age */
				if (x1 > 0.0) {
					height -= x1;
				}
			}
			break;
		case SI_FDI_MILNER:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 114.6 * ppow(1 - Math.exp(-0.01462 * breastHeightAge), 1.179);
				x2 = 1.703 * ppow(1 - Math.exp(-0.02214 * breastHeightAge), 1.321);
				height = 4.5 + x1 + x2 * (siteIndex - 57.3);

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_FDI_VDP_MONT:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				height = 4.5
						+ (1.9965 * (siteIndex - 4.5) / (1 + Math.exp(5.479 - 1.4016 * Math.log(breastHeightAge))));

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_FDI_VDP_WASH:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				height = 4.5
						+ (1.79897 * (siteIndex - 4.5) / (1 + Math.exp(6.0678 - 1.6085 * Math.log(breastHeightAge))));

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_FDI_MONS_DF:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;
				x1 = 0.3197;
				x2 = 1.0232;

				x3 = 1.0 + Math.exp(9.7278 - 1.2934 * Math.log(breastHeightAge) - x2 * llog(siteIndex - 4.5));

				height = 4.5 + 42.397 * ppow(siteIndex - 4.5, x1) / x3;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_FDI_MONS_GF:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;
				x1 = 0.3488;
				x2 = 0.9779;

				x3 = 1.0 + Math.exp(9.7278 - 1.2934 * Math.log(breastHeightAge) - x2 * llog(siteIndex - 4.5));

				height = 4.5 + 42.397 * ppow(siteIndex - 4.5, x1) / x3;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_FDI_MONS_WRC:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;
				x1 = 0.3488;
				x2 = 0.9779;

				x3 = 1.0 + Math.exp(9.7278 - 1.2934 * Math.log(breastHeightAge) - x2 * llog(siteIndex - 4.5));

				height = 4.5 + 42.397 * ppow(siteIndex - 4.5, x1) / x3;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_FDI_MONS_WH:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;
				x1 = 0.3656;
				x2 = 0.9527;

				x3 = 1.0 + Math.exp(9.7278 - 1.2934 * Math.log(breastHeightAge) - x2 * llog(siteIndex - 4.5));

				height = 4.5 + 42.397 * ppow(siteIndex - 4.5, x1) / x3;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_FDI_MONS_SAF:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;
				x1 = 0.3656;
				x2 = 0.9527;

				x3 = 1.0 + Math.exp(9.7278 - 1.2934 * Math.log(breastHeightAge) - x2 * llog(siteIndex - 4.5));

				height = 4.5 + 42.397 * ppow(siteIndex - 4.5, x1) / x3;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_DR_HARRING:
			if (siteIndex > 45 + 2.5 * totalAge) {
				/* function starts going nuts at high sites and low ages */
				/* evaluate at a safe age, and interpolate */
				x1 = (siteIndex - 45) / 2.5 + 0.1;
				x2 = indexToHeight(cuIndex, x1, SI_AT_TOTAL, siteIndex, years2BreastHeight, pi);
				height = x2 * totalAge / x1;
			} else {
				double si20;

				si20 = ppow(siteIndex, 1.5) / 8.0;
				x1 = 18.1622 + 0.7953 * si20;
				x2 = 0.00194 - 0.002441 * si20;
				x3 = si20 + x1 * ppow(1.0 - Math.exp(x2 * totalAge), 0.9198);
				height = x3 - x1 * ppow(1.0 - Math.exp(x2 * 20), 0.9198);
			}
			break;
		case SI_DR_NIGH:
			if (breastHeightAge > 0.5) {
				double si25;

				si25 = 0.3094 + 0.7616 * siteIndex;
				height = 1.3 + (1.693 * (si25 - 1.3)) / (1 + Math.exp(3.6 - 1.24 * Math.log(breastHeightAge - 0.5)));
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Cannot Find Constant
		/*
		 * case SI_BG_COCHRAN: if (bhage > 0.0){ // convert to imperial site_index /= 0.3048;
		 *
		 * x1 = Math.log (bhage); x2 = -0.30935 + 1.2383 * x1 + 0.001762 * ppow(x1, 4.0) - 5.4e-6 * ppow(x1, 9.0) +
		 * 2.046e-7 * ppow(x1, 11.0) - 4.04e-13 * ppow(x1, 18.0); x3 = -6.2056 + 2.097 * x1 - 0.09411 * ppow(x1, 2) -
		 * 4.382e-5 * ppow(x1, 7) + 2.007e-11 * ppow(x1, 16) - 2.054e-17 * ppow(x1, 24); height = 4.5 + Math.exp (x2) -
		 * 84.93 * Math.exp (x3) + (site_index - 4.5) * Math.exp (x3);
		 *
		 * // convert back to metric height *= 0.3048; } else{ height = tage * tage * 1.37 / y2bh / y2bh; } break;
		 */
		case SI_PY_MILNER:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 121.4 * ppow(1 - Math.exp(-0.01756 * breastHeightAge), 1.483);
				x2 = 1.189 * ppow(1 - Math.exp(-0.05799 * breastHeightAge), 2.63);
				height = 4.5 + x1 + x2 * (siteIndex - 59.6);

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PY_HANN:
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 1 - Math.exp(
						-Math.exp(-6.54707 + 0.288169 * llog(siteIndex - 4.5) + 1.21297 * Math.log(breastHeightAge))
				);
				x2 = 1 - Math.exp(-Math.exp(-6.54707 + 0.288169 * llog(siteIndex - 4.5) + 1.21297 * Math.log(50.0)));
				height = 4.5 + (siteIndex - 4.5) * x1 / x2;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PY_HANNAC:
			if (breastHeightAge > 0.5) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 1 - Math
						.exp(
								-Math.exp(
										-6.54707 + 0.288169 * llog(siteIndex - 4.5)
												+ 1.21297 * Math.log(breastHeightAge - 0.5)
								)
						);
				x2 = 1 - Math.exp(-Math.exp(-6.54707 + 0.288169 * llog(siteIndex - 4.5) + 1.21297 * Math.log(49.5)));
				height = 4.5 + (siteIndex - 4.5) * x1 / x2;

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;

		// case SI_LT_MILNER: Couldn't find constant
		case SI_LW_MILNER:
			// case SI_LA_MILNER: Couldn't find constant
			if (breastHeightAge > 0.0) {
				/* convert to imperial */
				siteIndex /= 0.3048;

				x1 = 127.8 * ppow(1 - Math.exp(-0.01655 * breastHeightAge), 1.196);
				x2 = 1.289 * ppow(1 - Math.exp(-0.03211 * breastHeightAge), 1.047);
				height = 4.5 + x1 + x2 * (siteIndex - 69.0);

				/* convert back to metric */
				height *= 0.3048;
			} else {
				height = totalAge * totalAge * 1.37 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_LW_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = Math.log(Math.pow(siteIndex - 1.3, 1 - 0.8566) / 3.027) / Math.log(1 - Math.exp(-0.01588 * 49.5));
				height = 1.3 + 3.027 * Math.pow(siteIndex - 1.3, 0.8566)
						* Math.pow(1 - Math.exp(-0.01588 * (breastHeightAge - 0.5)), x1);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_SB_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 1 + Math.exp(9.086 - 1.052 * Math.log(49.5) - 1.55 * Math.log(siteIndex - 1.3));
				x2 = 1 + Math.exp(9.086 - 1.052 * Math.log(breastHeightAge - 0.5) - 1.55 * Math.log(siteIndex - 1.3));
				height = 1.3 + (siteIndex - 1.3) * x1 / x2;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_AT_NIGH:
			if (breastHeightAge > 0.5) {
				x1 = 1 + Math.exp(7.423 - 1.15 * Math.log(49.5) - 0.9614 * Math.log(siteIndex - 1.3));
				x2 = 1 + Math.exp(7.423 - 1.15 * Math.log(breastHeightAge - 0.5) - 0.9614 * Math.log(siteIndex - 1.3));
				height = 1.3 + (siteIndex - 1.3) * x1 / x2;
			} else {
				/*
				 * was height = tage * tage * 1.3 / y2bh / y2bh;
				 */
				height = Math.pow(totalAge / years2BreastHeight, 1.5) * 1.3;
			}
			break;
		// Cannot find constant
		/*
		 * case SI_TE_GOUDIE: if (bhage > 0.0){ x1 = (1-Math.exp(-0.0227 * bhage)) / (1-Math.exp(-0.0227 * 50)); x2 =
		 * 6.525 * ppow(site_index, -0.7606); height = site_index * ppow(x1, x2); } else{ height = tage * tage * 1.3 /
		 * y2bh / y2bh; } break;
		 */
		case SI_SW_HUANG_PLA:
			double x0;
			double age_huang; /* used in HUANG's equations */

			if (breastHeightAge > 0.0) {
				x0 = 0.010168;
				x1 = 0.004801;
				x2 = 4.997735;
				x3 = 0.802776;
				x4 = -0.243297;
				x5 = 0.325438;
				age_huang = 50.0;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_SW_HUANG_NAT:
			if (breastHeightAge > 0.0) {
				x0 = 0.010168;
				x1 = 0.004801;
				x2 = 4.997735;
				x3 = 0.802776;
				x4 = -0.243297;
				x5 = 0.325438;
				age_huang = 50.0;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PLI_HUANG_PLA:
			if (breastHeightAge > 0.0) {
				x0 = 0.026714;
				x1 = -0.314562;
				x2 = 1.033165;
				x3 = 0.799658;
				x4 = -0.439270;
				x5 = 0.401374;
				age_huang = 1;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_PLI_HUANG_NAT:
			if (breastHeightAge > 0.0) {
				x0 = 0.026714;
				x1 = -0.314562;
				x2 = 1.033165;
				x3 = 0.799658;
				x4 = -0.439270;
				x5 = 0.401374;
				age_huang = 1;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Cannot Find Constant
		/*
		 * case SI_PJ_HUANG_PLA: double x0; double age_huang; // used in HUANG's equations
		 *
		 * if (bhage > 0.0){ x0 = 0.023405; x1 = -0.371557; x2 = 1.048011; x3 = 0.715449; x4 = -0.503105; x5 = 0.444505;
		 * age_huang = 1;
		 *
		 * x0 = -x0 * ppow(site_index - 1.3, x1) * Math.pow (x2, (site_index - 1.3) / age_huang); x0 = (1.0 - Math.exp
		 * (x0 * bhage)) / (1 - Math.exp (x0 * 50.0)); x1 = ppow(site_index - 1.3, x4); x2 = Math.pow (50.0, x5);
		 *
		 * height = 1.3 + (site_index - 1.3) * ppow(x0, x3 * x1 * x2); } else{ height = tage * tage * 1.3 / y2bh / y2bh;
		 * } break;
		 */
		// Cannot Find Constant
		/*
		 * case SI_PJ_HUANG_NAT: double x0; double age_huang; // used in HUANG's equations
		 *
		 * if (bhage > 0.0){ x0 = 0.023405; x1 = -0.371557; x2 = 1.048011; x3 = 0.715449; x4 = -0.503105; x5 = 0.444505;
		 * age_huang = 1;
		 *
		 * x0 = -x0 * ppow(site_index - 1.3, x1) * Math.pow (x2, (site_index - 1.3) / age_huang); x0 = (1.0 - Math.exp
		 * (x0 * bhage)) / (1 - Math.exp (x0 * 50.0)); x1 = ppow(site_index - 1.3, x4); x2 = Math.pow (50.0, x5);
		 *
		 * height = 1.3 + (site_index - 1.3) * ppow(x0, x3 * x1 * x2); } else{ height = tage * tage * 1.3 / y2bh / y2bh;
		 * } break;
		 */

		case SI_FDI_HUANG_PLA:
			if (breastHeightAge > 0.0) {
				x0 = 0.007932;
				x1 = 0.011994;
				x2 = 7.053999;
				x3 = 0.617157;
				x4 = -0.365916;
				x5 = 0.405321;
				age_huang = 50.0;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_FDI_HUANG_NAT:
			if (breastHeightAge > 0.0) {
				x0 = 0.007932;
				x1 = 0.011994;
				x2 = 7.053999;
				x3 = 0.617157;
				x4 = -0.365916;
				x5 = 0.405321;
				age_huang = 50.0;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_AT_HUANG:
			if (breastHeightAge > 0.0) {
				x0 = 0.035930;
				x1 = -0.486239;
				x2 = 1.041916;
				x3 = 0.818283;
				x4 = -0.594641;
				x5 = 0.522558;
				age_huang = 1;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_SB_HUANG:
			if (breastHeightAge > 0.0) {
				x0 = 0.011117;
				x1 = 0.030221;
				x2 = 1.010399;
				x3 = 0.573793;
				x4 = -0.328092;
				x5 = 0.387445;
				age_huang = 1;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_ACB_HUANG:
			if (breastHeightAge > 0.0) {
				x0 = 0.041208;
				x1 = -0.559626;
				x2 = 1.038923;
				x3 = 0.832609;
				x4 = -0.627227;
				x5 = 0.526901;
				age_huang = 1;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * breastHeightAge)) / (1 - Math.exp(x0 * 50.0));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(50.0, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;

			}
			break;

		// Cannot Find Constant
		/*
		 * case SI_BB_HUANG: double x0; double age_huang; // used in HUANG's equations
		 *
		 * if (bhage > 0.0){ x0 = 0.010190; x1 = 0.013957; x2 = 3.876735; x3 = 0.647527; x4 = -0.274343; x5 = 0.378078;
		 * age_huang = 50.0;
		 *
		 * x0 = -x0 * ppow(site_index - 1.3, x1) * Math.pow (x2, (site_index - 1.3) / age_huang); x0 = (1.0 - Math.exp
		 * (x0 * bhage)) / (1 - Math.exp (x0 * 50.0)); x1 = ppow(site_index - 1.3, x4); x2 = Math.pow (50.0, x5);
		 *
		 * height = 1.3 + (site_index - 1.3) * ppow(x0, x3 * x1 * x2); } else{ height = tage * tage * 1.3 / y2bh / y2bh;
		 * } break;
		 */

		case SI_ACB_HUANGAC: {
			if (breastHeightAge > 0.5) {
				x0 = 0.041208;
				x1 = -0.559626;
				x2 = 1.038923;
				x3 = 0.832609;
				x4 = -0.627227;
				x5 = 0.526901;
				age_huang = 1;

				x0 = -x0 * ppow(siteIndex - 1.3, x1) * Math.pow(x2, (siteIndex - 1.3) / age_huang);
				x0 = (1.0 - Math.exp(x0 * (breastHeightAge - 0.5))) / (1 - Math.exp(x0 * 49.5));
				x1 = ppow(siteIndex - 1.3, x4);
				x2 = Math.pow(49.5, x5);

				height = 1.3 + (siteIndex - 1.3) * ppow(x0, x3 * x1 * x2);
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
		}
			break;
		case SI_BL_CHEN:
			if (breastHeightAge > 0.0) {
				x1 = 9.523;
				x2 = -1.4945;
				x3 = -1.2159;

				x4 = (1.0 + Math.exp(x1 + x2 * Math.log(50.0) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge) + x3 * llog(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x4;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_SE_CHEN:
			if (breastHeightAge > 0.0) {
				x1 = 8.6126;
				x2 = -1.5269;
				x3 = -0.7805;

				x4 = (1.0 + Math.exp(x1 + x2 * Math.log(50.0) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge) + x3 * llog(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x4;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_PL_CHEN:
			if (breastHeightAge > 0.0) {
				x1 = 6.9603;
				x2 = -1.2875;
				x3 = -0.5904;

				x4 = (1.0 + Math.exp(x1 + x2 * Math.log(50.0) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge) + x3 * llog(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x4;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't Find Constant
		/*
		 * case SI_EP_CHEN: if (bhage > 0.0){ x1 = 9.9045; x2 = -1.1736; x3 = -1.8361;
		 *
		 * x4 = (1.0 + Math.exp (x1 + x2 * Math.log (50.0) + x3 * llog(site_index - 1.3))) / (1.0 + Math.exp (x1 + x2 *
		 * Math.log (bhage)+ x3 * llog(site_index - 1.3)));
		 *
		 * height = 1.3 + (site_index - 1.3) * x4; } else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */
		case SI_DR_CHEN:
			if (breastHeightAge > 0.0) {
				x1 = 6.6133;
				x2 = -1.0807;
				x3 = -1.0176;

				x4 = (1.0 + Math.exp(x1 + x2 * Math.log(50.0) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge) + x3 * llog(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x4;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_BL_CHENAC:
			if (breastHeightAge > 0.5) {
				x1 = 9.523;
				x2 = -1.4945;
				x3 = -1.2159;

				x4 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x4;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_SE_CHENAC:
			if (breastHeightAge > 0.5) {
				x1 = 8.6126;
				x2 = -1.5269;
				x3 = -0.7805;

				x4 = (1.0 + Math.exp(x1 + x2 * Math.log(49.5) + x3 * llog(siteIndex - 1.3)))
						/ (1.0 + Math.exp(x1 + x2 * Math.log(breastHeightAge - 0.5) + x3 * llog(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x4;
			} else {
				/*
				 * height = tage * tage * 1.3 / y2bh / y2bh;
				 */
				height = 1.3 * Math.pow(totalAge / years2BreastHeight, 1.628 - 0.05991 * years2BreastHeight)
						* Math.pow(1.127, totalAge - years2BreastHeight);
			}
			break;

		case SI_AT_CHEN:
			if (breastHeightAge > 0.0) {
				x1 = llog(ppow(siteIndex - 1.3, -0.076) / 1.418) / llog(1 - Math.exp(-0.017 * 50));
				height = 1.3
						+ 1.418 * (ppow(siteIndex - 1.3, 1.076) * ppow(1 - Math.exp(-0.017 * breastHeightAge), x1));
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		case SI_PJ_HUANG:
			if (breastHeightAge > 0) {
				x1 = 0.073456;
				x2 = 8.770517;
				x3 = -1.334706;
				x4 = 1.719841;

				x5 = (1.0 + x1 * (siteIndex - 1.3) + Math.exp(x2 + x3 * Math.log(50 + x4) - Math.log(siteIndex - 1.3)))
						/ (1.0 + x1 * (siteIndex - 1.3)
								+ Math.exp(x2 + x3 * Math.log(breastHeightAge + x4) - Math.log(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x5;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;

		case SI_PJ_HUANGAC:
			if (breastHeightAge > 0.5) {
				x1 = 0.073456;
				x2 = 8.770517;
				x3 = -1.334706;
				x4 = 1.719841;

				x5 = (1.0 + x1 * (siteIndex - 1.3)
						+ Math.exp(x2 + x3 * Math.log(49.5 + x4) - Math.log(siteIndex - 1.3)))
						/ (1.0 + x1 * (siteIndex - 1.3)
								+ Math.exp(x2 + x3 * Math.log(breastHeightAge - 0.5 + x4) - Math.log(siteIndex - 1.3)));

				height = 1.3 + (siteIndex - 1.3) * x5;
			} else {
				height = totalAge * totalAge * 1.3 / years2BreastHeight / years2BreastHeight;
			}
			break;
		// Couldn't Find Constant
		/*
		 * case SI_EP_CAMERON: if (bhage > 0.0) height = 1.76928 * (site_index - 1.3) * ppow(1 - Math.exp (-0.01558 *
		 * bhage), 0.92908); else{ height = tage * tage * 1.3 / y2bh / y2bh; } break;
		 */

		case SI_BA_NIGHGI, SI_BL_THROWERGI, SI_PY_NIGHGI, SI_CWI_NIGHGI, SI_FDC_NIGHGI, SI_FDI_NIGHGI, SI_HWC_NIGHGI,
				SI_HWC_NIGHGI99, SI_HWI_NIGHGI, SI_LW_NIGHGI,
				// case SI_PLI_NIGHGI: Couldnt Find constant
				SI_PLI_NIGHGI97, SI_SE_NIGHGI, SI_SS_NIGHGI, SI_SS_NIGHGI99, SI_SW_NIGHGI, SI_SW_NIGHGI99,
				SI_SW_NIGHGI2004:

			height = giSi2Ht(cuIndex, breastHeightAge, siteIndex);
			break;

		default:
			throw new CurveErrorException("Unknown curve index");
		}

		return height;
	}

	public static double giSi2Ht(SiteIndexEquation cuIndex, double age, double siteIndex)
			throws CommonCalculatorException {
		double si2ht;
		double step;
		double test_site;

		/* breast height age must be at least 1/2 a year */
		if (age < 0.5) {
			throw new GrowthInterceptMinimumException(
					"Variable height growth intercept formulation; bhage < 0.5 years. Age: " + age
			);
		}

		/* initial guess */
		si2ht = siteIndex;
		if (si2ht < 1.3) {
			si2ht = 1.3;
		}
		step = si2ht / 2;

		/* loop until real close */
		do {
			test_site = Height2SiteIndex.heightToIndex(cuIndex, age, SI_AT_BREAST, si2ht, SI_EST_DIRECT);
			/*
			 * printf ("age=%3.0f, site=%5.2f, test_site=%5.2f, si2ht=%5.2f, step=%9.7f\n", age, site_index, test_site,
			 * si2ht, step);
			 */

			// This code could probably be removed
			if (test_site < 0) /* error */
			{
				si2ht = test_site;
				break;
			}

			if ( (test_site - siteIndex > 0.01) || (test_site - siteIndex < -0.01)) {
				/* not close enough */
				if (test_site > siteIndex) {
					if (step > 0) {
						step = -step / 2.0;
					}
				} else {
					if (step < 0) {
						step = -step / 2.0;
					}
				}
				si2ht += step;
			} else {
				/* done */
				break;
			}

			/* check for lack of convergence, so we're not here forever */
			if (step < 0.00001 && step > -0.00001) {
				/* we have a value, but perhaps not too accurate */
				break;
			}
			if (si2ht > 999.0) {
				throw new NoAnswerException("Iteration could not converge (projected height > 999)");
			}
			/* site index must be at least 1.3 */
			if (si2ht < 1.3) {
				if (step > 0) {
					si2ht += step;
				} else {
					si2ht -= step;
				}
				step = step / 2.0;
			}
		} while (true);

		return si2ht;
	}

	public static double huGarciaQ(double siteIndex, double bhage) {
		double h, q, step, diff, lastdiff;

		q = 0.02;
		step = 0.01;
		lastdiff = 0;
		diff = 0;

		do {
			h = huGarciaH(q, bhage);
			lastdiff = diff;
			diff = siteIndex - h;
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

	public static double huGarciaH(double q, double bhage) {
		double a, height;

		a = 283.9 * Math.pow(q, 0.5137);
		height = a * Math.pow(1 - (1 - Math.pow(1.3 / a, 0.5829)) * Math.exp(-q * (bhage - 0.5)), 1.71556);
		return height;
	}
}
