package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.SiteIndexUtilities.llog;
import static ca.bc.gov.nrs.vdyp.common_calculators.SiteIndexUtilities.ppow;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_BREAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_TOTAL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEstimationType.SI_EST_DIRECT;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptTotalException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;

public class SiteIndex2Age {

	/*
	 * error codes as return values from functions
	 */
	private static final int SI_ERR_GI_MAX = -3;
	private static final int SI_ERR_NO_ANS = -4;

	public static final double MAX_AGE = 999.0;

	/*
	 * Wrapped in directives which check if TEST is defined. The code that defines TEST as 1 is commented out, so I am
	 * assuming this wouldn't run
	 */
	public static final boolean TEST = false;

	/**
	 * Given site index and site height, computes age.
	 *
	 * @param cuIndex
	 * @param siteHeight
	 * @param ageType
	 * @param siteIndex
	 * @param yearsToBreastHeight
	 *
	 * @return
	 *
	 * @throws LessThan13Exception           site index or height < 1.3m
	 * @throws NoAnswerException             iteration could not converge (or projected age > 999)
	 * @throws CurveEroorException           unknown curve index
	 * @throws GrowthInterceptTotalException cannot compute growth intercept when using total age
	 */
	public static double indexToAge(
			SiteIndexEquation cuIndex, double siteHeight, SiteIndexAgeType ageType, double siteIndex,
			double yearsToBreastHeight
	) throws CommonCalculatorException {

		double x1, x2, x3, x4;
		double a, b, c;
		/*
		 * I could not find HOOP, so I am assuming it is not intialized and as such the directives wouldn't trigger
		 */
		boolean HOOP = false;

		// #ifdef HOOP
		double ht5, ht10;
		// This is initialized because otherwise it will cause issues later.
		// #endif
		double age;

		if (siteHeight < 1.3) {
			if (ageType == SI_AT_BREAST) {
				throw new LessThan13Exception("Site index or height < 1.3m, site height: " + siteHeight);
			}

			if (siteHeight <= 0.0001) {
				return 0;
			}
		}

		if (siteIndex < 1.3) {
			throw new LessThan13Exception("Site index or height < 1.3m, site_index: " + siteIndex);
		}

		switch (cuIndex) {
		case SI_FDC_BRUCE:
			// 2009 may 6: force a non-rounded y2bh
			yearsToBreastHeight = 13.25 - siteIndex / 6.096;

			x1 = siteIndex / 30.48;
			x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));
			x3 = ppow(50.0 + yearsToBreastHeight, x2);
			double x4Denominator = ppow(yearsToBreastHeight, x2) - x3;
			if (x4Denominator == 0) {
				throw new ArithmeticException("Attempted Division by zero");
			}
			x4 = llog(1.372 / siteIndex) / x4Denominator;

			x1 = llog(siteHeight / siteIndex) / x4 + x3;

			if (x1 < 0) {
				age = SI_ERR_NO_ANS;
			} else {
				age = ppow(x1, 1 / x2);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
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

			q = huGarciaQ(siteIndex, 50.0);
			age = huGarciaBha(q, siteHeight);
			if (ageType == SI_AT_TOTAL) {
				age += yearsToBreastHeight;
			}
		}
			break;
		case SI_HWC_WILEY:
			if (siteHeight / 0.3048 < 4.5) {
				age = yearsToBreastHeight * ppow(siteHeight / 1.37, 0.5);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = 2500 / (siteIndex / 0.3048 - 4.5);
				x2 = -1.7307 + 0.1394 * x1;
				x3 = -0.0616 + 0.0137 * x1;
				x4 = 0.00192428 + 0.00007024 * x1;

				x1 = (4.5 - siteHeight / 0.3048);
				a = 1 + x1 * x4;
				b = x1 * x3;
				c = x1 * x2;

				x1 = ppow(b * b - 4 * a * c, 0.5);
				if (x1 == 0.0) {
					age = SI_ERR_NO_ANS;
				} else {
					age = (-b + x1) / (2 * a);

					if (ageType == SI_AT_TOTAL) {
						age += yearsToBreastHeight;
					}

					if (age < 0) {
						age = SI_ERR_NO_ANS;
					} else if (age > MAX_AGE) {
						age = SI_ERR_NO_ANS;
					}
				}
			}

			if (age < 10 && age > 0) {
				age = iterate(cuIndex, siteHeight, ageType, siteIndex, yearsToBreastHeight);
				if (HOOP) {
					ht5 = SiteIndex2Height
							.indexToHeight(cuIndex, 5.0, SI_AT_BREAST, siteIndex, yearsToBreastHeight, 0.5);
					// 0.5 may have to change

					if (siteHeight <= ht5) {
						siteHeight -= (1 - ( (ht5 - siteHeight) / ht5)) * 1.5;
					} else {
						// 0.5 may have to change
						ht10 = SiteIndex2Height
								.indexToHeight(cuIndex, 10.0, SI_AT_BREAST, siteIndex, yearsToBreastHeight, 0.5);
						siteHeight -= ( ( (ht10 - siteHeight) / (ht10 - ht5))) * 1.5;
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
			if (siteHeight < 1.3) {
				age = yearsToBreastHeight * ppow(siteHeight / 1.3, 0.5);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.00726;
				x2 = 7.81498;
				x3 = -1.28517;

				a = (siteIndex - 1.3) * (1 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(siteIndex - 1.3);

				age = Math.exp( (llog(a / (siteHeight - 1.3) - 1) - b) / x3);

				if (ageType == SI_AT_TOTAL) {
					age += yearsToBreastHeight;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		case SI_PLI_GOUDIE_WET:
			if (siteHeight < 1.3) {
				age = yearsToBreastHeight * ppow(siteHeight / 1.3, 0.5);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -0.935;
				x2 = 7.81498;
				x3 = -1.28517;

				a = (siteIndex - 1.3) * (1 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(siteIndex - 1.3);

				age = Math.exp( (llog(a / (siteHeight - 1.3) - 1) - b) / x3);

				if (ageType == SI_AT_TOTAL) {
					age += yearsToBreastHeight;
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
			if (siteHeight < 1.3) {
				age = yearsToBreastHeight * ppow(siteHeight / 1.3, 0.5);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.5282;
				x2 = 11.0605;
				x3 = -1.5108;

				a = (siteIndex - 1.3) * (1 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(siteIndex - 1.3);

				age = Math.exp( (llog(a / (siteHeight - 1.3) - 1) - b) / x3);

				if (ageType == SI_AT_TOTAL) {
					age += yearsToBreastHeight;
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
			if (siteHeight < 1.3) {
				age = yearsToBreastHeight * ppow(siteHeight / 1.3, 0.5);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				a = (siteIndex - 1.3) * (1 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(siteIndex - 1.3);

				age = Math.exp( (llog(a / (siteHeight - 1.3) - 1) - b) / x3);

				if (ageType == SI_AT_TOTAL) {
					age += yearsToBreastHeight;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;

		case SI_SW_GOUDIE_NAT:
			if (siteHeight < 1.3) {
				age = yearsToBreastHeight * ppow(siteHeight / 1.3, 0.5);

				if (ageType == SI_AT_BREAST) {
					age -= yearsToBreastHeight;
				}

				if (age < 0.0) {
					age = 0.0;
				}
			} else {
				x1 = -1.2866;
				x2 = 9.7936;
				x3 = -1.4661;

				a = (siteIndex - 1.3) * (1 + Math.exp(x2 + x1 * llog(siteIndex - 1.3) + x3 * Math.log(50.0)));
				b = x2 + x1 * llog(siteIndex - 1.3);

				age = Math.exp( (llog(a / (siteHeight - 1.3) - 1) - b) / x3);

				if (ageType == SI_AT_TOTAL) {
					age += yearsToBreastHeight;
				}

				if (age < 0) {
					age = 0;
				} else if (age > MAX_AGE) {
					age = SI_ERR_NO_ANS;
				}
			}
			break;
		case SI_BL_THROWERGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_CWI_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_FDC_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_FDI_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_HWC_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_HWC_NIGHGI99:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_HWI_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_LW_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		// Couldn't find constant
		/*
		 * case SI_PLI_NIGHGI: age = gi_iterate (cu_index, site_height, age_type, site_index); break;
		 */
		case SI_PLI_NIGHGI97:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_SS_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_SS_NIGHGI99:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_SW_NIGHGI:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
			break;
		case SI_SW_NIGHGI99:
			age = giIterate(cuIndex, siteHeight, ageType, siteIndex);
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
			age = iterate(cuIndex, siteHeight, ageType, siteIndex, yearsToBreastHeight);
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

	public static double iterate(
			SiteIndexEquation cuIndex, double siteHeight, SiteIndexAgeType ageType, double siteIndex,
			double yearsToBreastHeight
	) throws CommonCalculatorException {

		double si2age;
		double step;
		double test_ht;
		int err_count;

		/* initial guess */
		si2age = 25;
		step = si2age / 2;
		err_count = 0;

		/* do a preliminary test to catch some obvious errors */
		test_ht = SiteIndex2Height.indexToHeight(
				cuIndex, si2age, SI_AT_TOTAL, siteIndex, yearsToBreastHeight, 0.5 /* may have to change */
		);
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
										"before index_to_height(age=%.2f, age_type=%s, site_index=%.2f, y2bh=%.2f)%n",
										si2age, ageType.toString(), siteIndex, yearsToBreastHeight
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
				// 0.5 may have to change
				test_ht = SiteIndex2Height
						.indexToHeight(cuIndex, si2age, SI_AT_TOTAL, siteIndex, yearsToBreastHeight, 0.5);

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
			if ( (test_ht - siteHeight > 0.005) || (test_ht - siteHeight < -0.005)) {
				/* not close enough */
				if (test_ht > siteHeight) {
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
			if (ageType == SI_AT_BREAST) {
				/*
				 * was si2age -= y2bh;
				 */
				si2age = AgeToAge.ageToAge(cuIndex, si2age, SI_AT_TOTAL, SI_AT_BREAST, yearsToBreastHeight);
			}
		}
		if (si2age == SI_ERR_NO_ANS) {
			throw new NoAnswerException(
					"Iteration could not converge (or projected age > 999)," + "site index 2 age variable: " + si2age
			);
		}
		return (si2age);
	}

	public static double
			giIterate(SiteIndexEquation cuIndex, double siteHeight, SiteIndexAgeType ageType, double siteIndex)
					throws CommonCalculatorException {
		double age;
		double si2age;
		double testSite;
		double diff;
		double mindiff;

		if (ageType == SI_AT_TOTAL) {
			throw new GrowthInterceptTotalException(
					"cannot compute growth intercept when using total age, age type: " + ageType
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
								String.format("before height_to_index(age=%f, site_height=%f)\n", age, siteHeight)
						);

						// Close the file
						fileWriter.close();
					}
				} catch (IOException e) {
					throw new RuntimeException("An error occurred while writing to the file.", e);
				}

			}
			testSite = Height2SiteIndex.heightToIndex(cuIndex, age, SI_AT_BREAST, siteHeight, SI_EST_DIRECT);

			if (TEST) {
				try {
					// Open the file for writing
					File testfile = new File("si2age.tst");
					try (FileWriter fileWriter = new FileWriter(testfile, true)) {

						// Write to the file
						fileWriter.write(String.format("height_to_index()=%f\n", testSite));

						// Close the file
						fileWriter.close();
					}
				} catch (IOException e) {
					throw new RuntimeException("An error occurred while writing to the file.", e);
				}
			}

			if (testSite == SI_ERR_GI_MAX) {
				break;
			}

			if (testSite > siteIndex) {
				diff = testSite - siteIndex;
			} else {
				diff = siteIndex - testSite;
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

	public static double huGarciaQ(double siteIndex, double breastHeightAge) {
		double h, q, step, diff, lastdiff;

		q = 0.02;
		step = 0.01;
		lastdiff = 0;
		diff = 0;

		do {
			h = huGarciaH(q, breastHeightAge);
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

	public static double huGarciaH(double q, double breastHeightAge) {
		double a, height;

		a = 283.9 * Math.pow(q, 0.5137);
		height = a * Math.pow(1 - (1 - Math.pow(1.3 / a, 0.5829)) * Math.exp(-q * (breastHeightAge - 0.5)), 1.71556);
		return height;
	}

	public static double huGarciaBha(double q, double height) {
		double a, bhage;

		a = 283.9 * Math.pow(q, 0.5137);
		bhage = 0.5 - 1 / q * Math.log( (1 - Math.pow(height / a, 0.5829)) / (1 - Math.pow(1.3 / a, 0.5829)));
		return bhage;
	}

}
