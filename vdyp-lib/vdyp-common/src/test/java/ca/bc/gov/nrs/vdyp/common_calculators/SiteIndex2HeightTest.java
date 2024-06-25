package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_BREAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_TOTAL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_ACT_THROWER;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_BA_NIGHGI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_BP_CURTIS;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_BP_CURTISAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_CWC_KURUCZAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDC_COCHRAN;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDC_KING;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HM_MEANS;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HM_MEANSAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_BARKER;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_FARR;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_WILEY;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_WILEYAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_WILEY_BC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWC_WILEY_MB;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_GOUDNIGH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptMaximumException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptMinimumException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;

class SiteIndex2HeightTest {

	private static final double ERROR_TOLERANCE = 0.00001;

	@Test
	void testPpowPositive() throws CommonCalculatorException {
		assertThat(8.0, closeTo(SiteIndexUtilities.ppow(2.0, 3.0), ERROR_TOLERANCE));
		assertThat(1.0, closeTo(SiteIndexUtilities.ppow(5.0, 0.0), ERROR_TOLERANCE));
	}

	@Test
	void testPpowZero() throws CommonCalculatorException {
		assertThat(0.0, closeTo(SiteIndexUtilities.ppow(0.0, 3.0), ERROR_TOLERANCE));
	}

	@Test
	void testLlogPositive() throws CommonCalculatorException {
		assertThat(1.60943, closeTo(SiteIndexUtilities.llog(5.0), ERROR_TOLERANCE));
		assertThat(11.51293, closeTo(SiteIndexUtilities.llog(100000.0), ERROR_TOLERANCE));
	}

	@Test
	void testLlogZero() throws CommonCalculatorException {
		assertThat(-11.51293, closeTo(SiteIndexUtilities.llog(0.0), ERROR_TOLERANCE));
	}

	@Nested
	class Index_to_heightTest {

		@Test
		void testInvalidSiteIndex() throws CommonCalculatorException {
			assertThrows(
					LessThan13Exception.class,
					() -> SiteIndex2Height.indexToHeight(null, 0.0, SI_AT_TOTAL, 1.2, 0.0, 0.0)
			);
		}

		@Test
		void testInvalidTage() throws CommonCalculatorException {
			double expectedResult = 0;
			double actualResult = SiteIndex2Height.indexToHeight(SI_BP_CURTISAC, 0.0, SI_AT_TOTAL, 1.31, 0.0, 0.0);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			assertThrows(
					NoAnswerException.class,
					() -> SiteIndex2Height.indexToHeight(SI_CWC_KURUCZAC, (short) -1, SI_AT_TOTAL, 1.31, 0.0, 0.0)
			);
		}

		@Test
		void runAllCalls() {

			var equationIterator = SiteIndexEquation.getIterator();
			while (equationIterator.hasNext()) {
				var eq = equationIterator.next();
				for (SiteIndexAgeType t : SiteIndexAgeType.values()) {
					try {
						SiteIndex2Height.indexToHeight(eq, 0.0, t, 1.31, 1.0, 0.0);
					} catch (GrowthInterceptMaximumException | GrowthInterceptMinimumException | NoAnswerException e) {
						// ok - continue
					} catch (CommonCalculatorException e) {
						Assertions.fail(e);
					}
				}
			}
		}

		@Test
		void testSI_FDC_COCHRAN() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_FDC_COCHRAN, 0.0, SI_AT_BREAST, 1.31, 1.0, 0.0);
			double expectedResult = 1.37;
			double site_index = 1.31;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			actualResult = SiteIndex2Height.indexToHeight(SI_FDC_COCHRAN, 1.0, SI_AT_BREAST, site_index, 1.0, 0.0);
			site_index /= 0.3048;
			double x1 = Math
					.exp(-0.37496 + 1.36164 * Math.log(1) - 0.00243434 * SiteIndexUtilities.ppow(Math.log(1), 4));
			double x2 = -0.2828 + 1.87947 * SiteIndexUtilities.ppow(1 - Math.exp(-0.022399 * 1), 0.966998);
			expectedResult = 4.5 + x1 - x2 * (79.97 - (site_index - 4.5));
			expectedResult *= 0.3048;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDC_KINGBhageLessThanZero() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_FDC_KING, 0.0, SI_AT_BREAST, 1.31, 1.0, 0.0);
			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE)); // bhage <= 0
		}

		@Test
		void testSI_FDC_KINGBhageLessThanFive() throws CommonCalculatorException {
			double site_index = 1.31;
			double actualResult = SiteIndex2Height.indexToHeight(SI_FDC_KING, 1.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index /= 0.3048;
			double x1 = 2500 / (site_index - 4.5);
			double x2 = -0.954038 + 0.109757 * x1;
			double x3 = 0.0558178 + 0.00792236 * x1;
			double x4 = -0.000733819 + 0.000197693 * x1;

			double expectedResult = 4.5 + 1 / (x2 + x3 + x4);
			expectedResult += 0.22;
			expectedResult *= 0.3048;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE)); // bhage < 5
		}

		@Test
		void testSI_FDC_KINGBhageGreaterThanFive() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height.indexToHeight(SI_FDC_KING, 5.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index /= 0.3048;
			double x1 = 2500 / (site_index - 4.5);
			double x2 = -0.954038 + 0.109757 * x1;
			double x3 = 0.0558178 + 0.00792236 * x1;
			double x4 = -0.000733819 + 0.000197693 * x1;

			double expectedResult = 4.5 + 25 / (x2 + x3 * 5 + x4 * 25);
			expectedResult += (2.2 - 0.22 * 5);
			expectedResult *= 0.3048;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE)); // bhage >= 5
		}

		@Test
		void testSI_HWC_FARRBhageGreaterThanZero() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height.indexToHeight(SI_HWC_FARR, 5.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index /= 0.3048;
			double x2 = 0.3621734 + 1.149181 * Math.log(5) - 0.005617852 * SiteIndexUtilities.ppow(Math.log(5), 3.0)
					- 7.267547E-6 * SiteIndexUtilities.ppow(Math.log(5), 7.0)
					+ 1.708195E-16 * SiteIndexUtilities.ppow(Math.log(5), 22.0)
					- 2.482794E-22 * SiteIndexUtilities.ppow(Math.log(5), 30.0);

			double x3 = -2.146617 - 0.109007 * Math.log(5) + 0.0994030 * SiteIndexUtilities.ppow(Math.log(5), 3.0)
					- 0.003853396 * SiteIndexUtilities.ppow(Math.log(5), 5.0)
					+ 1.193933E-8 * SiteIndexUtilities.ppow(Math.log(5), 12.0)
					- 9.486544E-20 * SiteIndexUtilities.ppow(Math.log(5), 27.0)
					+ 1.431925E-26 * SiteIndexUtilities.ppow(Math.log(5), 36.0);

			double expectedResult = 4.5 + Math.exp(x2) - Math.exp(x3) * (83.20 - (site_index - 4.5));
			expectedResult *= 0.3048;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_FARRBhageLessThanZero() throws CommonCalculatorException { // LessThanOrEqual is meant but
																					// omitted
			double actualResult = SiteIndex2Height.indexToHeight(SI_HWC_FARR, 0.0, SI_AT_BREAST, 1.31, 2.0, 0.0);
			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE)); // bhage <= 0
		}

		@Test
		void testSI_HWC_BARKER() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_HWC_BARKER, 0.0, SI_AT_BREAST, 1.31, 1.0, 0.0);
			double expectedResult = Math.exp(4.35753) * SiteIndexUtilities.ppow(
					(-10.45 + 1.30049 * 1.31 - 0.0022 * 1.7161) / Math.exp(4.35753),
					SiteIndexUtilities.ppow(50.0, 0.756313)
			);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HM_MEANSBhageGreaterThanZero() throws CommonCalculatorException {
			double site_index = 1.31;
			double actualResult = SiteIndex2Height.indexToHeight(SI_HM_MEANS, 5.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index = -1.73 + 3.149 * SiteIndexUtilities.ppow(site_index, 0.8279);
			double expectedResult = 1.37 + (22.87 + 0.9502 * (site_index - 1.37)) * SiteIndexUtilities.ppow(
					1 - Math.exp(-0.0020647 * SiteIndexUtilities.ppow(site_index - 1.37, 0.5) * 5),
					1.3656 + 2.046 / (site_index - 1.37)
			);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HM_MEANSBhageLessThanZero() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_HM_MEANS, 0.0, SI_AT_BREAST, 1.31, 1.0, 0.0);
			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HM_MEANSACBhageGreaterThanHalf() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HM_MEANSAC, 5.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index = -1.73 + 3.149 * SiteIndexUtilities.ppow(site_index, 0.8279);
			double expectedResult = 1.37 + (22.87 + 0.9502 * (site_index - 1.37)) * SiteIndexUtilities.ppow(
					1 - Math.exp(-0.0020647 * SiteIndexUtilities.ppow(site_index - 1.37, 0.5) * 5),
					1.3656 + 2.046 / (site_index - 1.37)
			);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HM_MEANSACBhageLessThanHalf() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_HM_MEANSAC, 0.0, SI_AT_BREAST, 1.31, 1.5, 0.0);
			double expectedResult = 1.37 / 1.5 / 1.5;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEYBHAGEZero() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height.indexToHeight(SI_HWC_WILEY, 0.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEYBHAGELargeSite() throws CommonCalculatorException {
			double site_index = 80;

			double actualResult = SiteIndex2Height.indexToHeight(SI_HWC_WILEY, 1.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double x1 = 20 / 1.667 + 0.1;
			double expectedFunctionResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY, x1, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37 + (expectedFunctionResult - 1.37) / x1;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEY_BCBHAGEZero() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY_BC, 0.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEY_BCBHAGELargeSite() throws CommonCalculatorException {
			double site_index = 80;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY_BC, 1.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double x1 = 20 / 1.667 + 0.1;
			double expectedFunctionResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY_BC, x1, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37 + (expectedFunctionResult - 1.37) / x1;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEY_MBBHAGEZero() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY_MB, 0.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEY_MBBHAGELargeSite() throws CommonCalculatorException {
			double site_index = 80;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY_MB, 1.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double x1 = 20 / 1.667 + 0.1;
			double expectedFunctionResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEY_MB, x1, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37 + (expectedFunctionResult - 1.37) / x1;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEYACBHAGELessThanPI() throws CommonCalculatorException {
			double site_index = 1.31;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEYAC, 0.5, SI_AT_BREAST, site_index, 1.0, 0.6);

			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEYACBHAGELargeSite() throws CommonCalculatorException {
			double site_index = 80;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEYAC, 1.0, SI_AT_BREAST, site_index, 1.0, 0.0);

			double x1 = 20 / 1.667 + 0.1;
			double expectedFunctionResult = SiteIndex2Height
					.indexToHeight(SI_HWC_WILEYAC, x1, SI_AT_BREAST, site_index, 1.0, 0.0);

			double expectedResult = 1.37 + (expectedFunctionResult - 1.37) / x1;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BP_CURTISBhageLessThanZero() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_BP_CURTIS, 0.0, SI_AT_BREAST, 1.31, 1.0, 0.0);
			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BP_CURTISBhageGreaterThanHalf() throws CommonCalculatorException {
			double site_index = 6;
			double bhage = 5;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_BP_CURTIS, bhage, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index /= 0.3048;
			double x1 = Math.log(site_index - 4.5) + 1.649871 * (Math.log(bhage) - Math.log(50))
					+ 0.147245 * Math.pow(Math.log(bhage) - Math.log(50), 2.0);

			double x2 = 1.0 + 0.164927 * (Math.log(bhage) - Math.log(50))
					+ 0.052467 * Math.pow(Math.log(bhage) - Math.log(50), 2.0);

			double expectedResult = (4.5 + Math.exp(x1 / x2));
			expectedResult *= 0.3048;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BP_CURTISACBhageLessThanZero() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.indexToHeight(SI_BP_CURTISAC, 0.5, SI_AT_BREAST, 1.31, 1.0, 0.6);
			double expectedResult = 1.37;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BP_CURTISACBhageGreaterThanHalf() throws CommonCalculatorException {
			double site_index = 6;
			double bhage = 5;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_BP_CURTISAC, bhage, SI_AT_BREAST, site_index, 1.0, 0.0);

			site_index /= 0.3048;
			double x1 = Math.log(site_index - 4.5) + 1.649871 * (Math.log(bhage - 0.5) - Math.log(49.5))
					+ 0.147245 * Math.pow(Math.log(bhage - 0.5) - Math.log(49.5), 2.0);

			double x2 = 1.0 + 0.164927 * (Math.log(bhage - 0.5) - Math.log(49.5))
					+ 0.052467 * Math.pow(Math.log(bhage - 0.5) - Math.log(49.5), 2.0);

			double expectedResult = (4.5 + Math.exp(x1 / x2));
			expectedResult *= 0.3048;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_GOUDNIGHBhageGreaterThanHalf() throws CommonCalculatorException {
			double site_index = 18.0;
			double bhage = 1.0;
			double y2bh = 1.5;

			double expectedResult = (1.0
					+ Math.exp(9.7936 - 1.2866 * SiteIndexUtilities.llog(site_index - 1.3) - 1.4661 * Math.log(49.5)))
					/ (1.0 + Math.exp(
							9.7936 - 1.2866 * SiteIndexUtilities.llog(site_index - 1.3) - 1.4661 * Math.log(bhage - 0.5)
					));
			expectedResult = 1.3 + (site_index - 1.3) * expectedResult;

			double actualResult = SiteIndex2Height
					.indexToHeight(SI_SW_GOUDNIGH, bhage, SI_AT_BREAST, site_index, y2bh, 0.0);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BA_NIGHGI() throws CommonCalculatorException {
			double site_index = 1.31;

			double expectedResult = SiteIndex2Height.giSi2Ht(SI_BA_NIGHGI, 5, site_index);

			double actualResult = SiteIndex2Height.indexToHeight(SI_BA_NIGHGI, 5, SI_AT_BREAST, site_index, 1.5, 0.0);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testDefaultSwitchStatement() throws CommonCalculatorException {
			assertThrows(
					CurveErrorException.class,
					() -> SiteIndex2Height.indexToHeight(null, 5, SI_AT_BREAST, 1.31, 1.5, 0.0)
			);
		}

	}

	@Nested
	class testGiSi2Ht {
		@Test
		void testBhageLessThanHalf() throws CommonCalculatorException {
			assertThrows(GrowthInterceptMinimumException.class, () -> SiteIndex2Height.giSi2Ht(null, 0.0, 0.0));
		}

		@Test
		void testValidInput() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.giSi2Ht(SI_FDC_COCHRAN, 1, 1.31);

			double expectedResult = 1.965;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testConvergenceError() throws CommonCalculatorException {
			assertThrows(NoAnswerException.class, () -> {
				SiteIndex2Height.giSi2Ht(SI_FDC_COCHRAN, 5.0, 2000.0);
			});
		}

		@Test
		void testNegativeSiteIndex() throws CommonCalculatorException {
			double actualResult = SiteIndex2Height.giSi2Ht(SI_ACT_THROWER, 3.0, -5.0);
			assertThat(actualResult, closeTo(1.3, ERROR_TOLERANCE));
		}

	}

	@Test
	void testHuGarciaQ() throws CommonCalculatorException {
		// the way I've done these tests is to validate them with the original C code and
		// compare them with the output of the java code

		// Test case 1
		double siteIndex1 = 20.0;
		double bhAge1 = 30.0;
		double expectedQ1 = 0.028228; // from running the C code
		double resultQ1 = SiteIndex2Height.huGarciaQ(siteIndex1, bhAge1);

		assertThat(expectedQ1, closeTo(resultQ1, ERROR_TOLERANCE));

		// Test case 2
		double siteIndex2 = 25.0;
		double bhAge2 = 40.0;
		double expectedQ2 = 0.027830; // from running the C code
		double resultQ2 = SiteIndex2Height.huGarciaQ(siteIndex2, bhAge2);

		assertThat(expectedQ2, closeTo(resultQ2, ERROR_TOLERANCE));
	}

	@Test
	void testHuGarciaH() throws CommonCalculatorException {
		double q = 05;
		double bhage = 10.0;

		double expectedResult = (283.9 * Math.pow(q, 0.5137)) * Math.pow(
				1 - (1 - Math.pow(1.3 / (283.9 * Math.pow(q, 0.5137)), 0.5829)) * Math.exp(-q * (bhage - 0.5)), 1.71556
		);

		double actualResult = SiteIndex2Height.huGarciaH(q, bhage);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}
}
