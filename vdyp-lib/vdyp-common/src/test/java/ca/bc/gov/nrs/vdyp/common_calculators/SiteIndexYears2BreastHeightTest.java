package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.GrowthInterceptTotalException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;

class SiteIndexYears2BreastHeightTest {

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
	class si_y2bhTest {
		@Test
		void testSiteIndexInvalid() throws CommonCalculatorException {
			assertThrows(LessThan13Exception.class, () -> SiteIndexYears2BreastHeight.y2bh(null, 1));
		}

		@Test
		void testSI_FDC_NIGHGI() throws CommonCalculatorException {
			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_FDC_NIGHGI, 2));
		}

		@Test
		void testSI_FDC_BRUCE() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDC_BRUCE;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 13.25 * 6.096; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDC_BRUCEAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDC_BRUCEAC;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 13.25 * 6.096; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDC_NIGHTA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDC_NIGHTA;
			double siteIndex = 10;// check normal case

			double expectedResult = 24.44 * Math.pow(siteIndex - 9.051, -0.394);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			// check site index <= 9.051 case
			assertThrows(NoAnswerException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2));

		}

		@Test
		void testSI_FDC_BRUCENIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDC_BRUCENIGH;
			double siteIndex = 15;// check normal case

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 16; // check site index <= 15 case
			expectedResult = 36.5818 * Math.pow(siteIndex - 6.6661, -0.5526);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDC_COCHRAN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDC_COCHRAN;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 13.25 * 6.096; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDC_KING() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDC_KING;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 13.25 * 6.096; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_FARR() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWC_FARR;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 13.25 * 6.096; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_BARKER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWC_BARKER;
			double siteIndex = 1.3;// check normal case

			double expectedResult = -5.2 + 410.00 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HM_MEANS() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HM_MEANS;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 9.43 - siteIndex / 7.088;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HM_MEANSAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HM_MEANSAC;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 9.43 - siteIndex / 7.088;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWI_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWI_NIGH;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 446.6 * SiteIndexUtilities.ppow(siteIndex, -1.432);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWI_NIGHGI() throws CommonCalculatorException {
			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_HWI_NIGHGI, 2));
		}

		@Test
		void testSI_HWC_NIGHGI() throws CommonCalculatorException {
			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_HWC_NIGHGI, 2));
		}

		@Test
		void testSI_HWC_NIGHGI99() throws CommonCalculatorException {
			assertThrows(
					GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_HWC_NIGHGI99, 2)
			);
		}

		@Test
		void testSI_SS_NIGHGI99() throws CommonCalculatorException {
			assertThrows(
					GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_SS_NIGHGI99, 2)
			);
		}

		@Test
		void testSI_SW_NIGHGI99() throws CommonCalculatorException {
			assertThrows(
					GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_SW_NIGHGI99, 2)
			);
		}

		@Test
		void testSI_SW_NIGHGI2004() throws CommonCalculatorException {
			assertThrows(
					GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_SW_NIGHGI2004, 2)
			);
		}

		@Test
		void testSI_LW_NIGHGI() throws CommonCalculatorException {
			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(SI_LW_NIGHGI, 2));
		}

		@Test
		void testSI_HWC_WILEY() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWC_WILEY;
			double siteIndex = 1.3;// check normal case

			double expectedResult = 9.43 - siteIndex / 7.088;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410; // check y2bh < 1 case
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEYAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWC_WILEYAC;
			double siteIndex = 1.3;

			double expectedResult = 9.43 - siteIndex / 7.088;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEY_BC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWC_WILEY_BC;
			double siteIndex = 1.3;

			double expectedResult = 9.43 - siteIndex / 7.088;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_HWC_WILEY_MB() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_HWC_WILEY_MB;
			double siteIndex = 1.3;

			double expectedResult = 9.43 - siteIndex / 7.088;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 410;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PJ_HUANG() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PJ_HUANG;
			double siteIndex = 2.5;

			double expectedResult = 5 + 1.872138 + 49.555513 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PJ_HUANGAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PJ_HUANGAC;
			double siteIndex = 2.5;

			double expectedResult = 5 + 1.872138 + 49.555513 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_NIGHGI97() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_NIGHGI97;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_PLI_HUANG_PLA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_HUANG_PLA;
			double siteIndex = 2.5;

			double expectedResult = 3.5 + 1.740006 + 58.83891 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_HUANG_NAT() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_HUANG_NAT;
			double siteIndex = 2.5;

			double expectedResult = 5 + 1.740006 + 58.83891 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_NIGHTA2004() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_NIGHTA2004;

			assertThrows(NoAnswerException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 8.0));

			double siteIndex = 12.0;
			double expectedResult = 21.6623 * SiteIndexUtilities.ppow(siteIndex - 9.05671, -0.550762);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_NIGHTA98() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_NIGHTA98;

			assertThrows(NoAnswerException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 8.0));

			double siteIndex = 12.0;
			double expectedResult = 21.6623 * SiteIndexUtilities.ppow(siteIndex - 9.05671, -0.550762);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_GOUDNIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_GOUDNIGH;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 19.4;
			expectedResult = 10.45;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 20;
			expectedResult = 35.87 * SiteIndexUtilities.ppow(siteIndex - 9.726, -0.5409);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_NIGHTA2004() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_NIGHTA2004;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 20;
			expectedResult = 35.87 * SiteIndexUtilities.ppow(siteIndex - 9.726, -0.5409);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_HU_GARCIA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_HU_GARCIA;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 20;
			expectedResult = 35.87 * SiteIndexUtilities.ppow(siteIndex - 9.726, -0.5409);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_NIGHTA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_NIGHTA;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 20;
			expectedResult = 35.87 * SiteIndexUtilities.ppow(siteIndex - 9.726, -0.5409);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SE_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SE_NIGH;
			double siteIndex = 2;

			double expectedResult = 6.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SE_NIGHTA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SE_NIGHTA;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 20;
			expectedResult = 35.87 * SiteIndexUtilities.ppow(siteIndex - 9.726, -0.5409);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SE_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SE_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_PLI_THROWNIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_THROWNIGH;
			double siteIndex = 2;

			double expectedResult = 2 + 0.55 + 69.4 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 20;
			expectedResult = 21.6623 * SiteIndexUtilities.ppow(siteIndex - 9.05671, -0.550762);
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_THROWER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_THROWER;
			double siteIndex = 2;

			double expectedResult = 2 + 0.55 + 69.4 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_MILNER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_MILNER;
			double siteIndex = 2;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_CIESZEWSKI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_CIESZEWSKI;
			double siteIndex = 2;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_GOUDIE_DRY() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_GOUDIE_DRY;
			double siteIndex = 2;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_GOUDIE_WET() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_GOUDIE_WET;
			double siteIndex = 2;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PLI_DEMPSTER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PLI_DEMPSTER;
			double siteIndex = 2;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PL_CHEN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PL_CHEN;
			double siteIndex = 2;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SE_CHEN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SE_CHEN;
			double siteIndex = 2;

			double expectedResult = 6.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SE_CHENAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SE_CHENAC;
			double siteIndex = 2;

			double expectedResult = 6.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_SW_HUANG_PLA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_HUANG_PLA;
			double siteIndex = 2;

			double expectedResult = 4.5 + 4.3473 + 59.908359 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_HUANG_NAT() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_HUANG_NAT;
			double siteIndex = 2;

			double expectedResult = 8 + 4.3473 + 59.908359 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_THROWER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_THROWER;
			double siteIndex = 2;

			double expectedResult = 4 + 0.38 + 117.34 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_KER_PLA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_KER_PLA;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_KER_NAT() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_KER_NAT;
			double siteIndex = 2;

			double expectedResult = 6.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_GOUDIE_PLA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_GOUDIE_PLA;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_GOUDIE_NAT() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_GOUDIE_NAT;
			double siteIndex = 2;

			double expectedResult = 6.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_GOUDIE_PLAAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_GOUDIE_PLAAC;
			double siteIndex = 3.0;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_GOUDIE_NATAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_GOUDIE_NATAC;
			double siteIndex = 4.0;

			double expectedResult = 6.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_DEMPSTER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_DEMPSTER;
			double siteIndex = 5.0;

			double expectedResult = 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_CIESZEWSKI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SW_CIESZEWSKI;
			double siteIndex = 6.0;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SB_HUANG() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SB_HUANG;
			double siteIndex = 7.0;

			double expectedResult = 8 + 2.288325 + 80.774008 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SB_KER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SB_KER;
			double siteIndex = 8.0;

			double expectedResult = 7.0 + 4.0427 + 61.08 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SB_DEMPSTER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SB_DEMPSTER;
			double siteIndex = 9.0;

			double expectedResult = 7.0 + 4.0427 + 61.08 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SB_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SB_NIGH;
			double siteIndex = 2;

			double expectedResult = 7.0 + 4.0427 + 61.08 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SB_CIESZEWSKI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SB_CIESZEWSKI;
			double siteIndex = 2;

			double expectedResult = 7.0 + 4.0427 + 61.08 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SS_GOUDIE() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SS_GOUDIE;
			double siteIndex = 2;

			double expectedResult = 11.7 - siteIndex / 5.4054;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 100;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SS_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SS_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_SS_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SS_NIGH;
			double siteIndex = 2;

			double expectedResult = 11.7 - siteIndex / 5.4054;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 100;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SS_FARR() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SS_FARR;
			double siteIndex = 2;

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 100;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SS_BARKER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_SS_BARKER;
			double siteIndex = 2;

			double expectedResult = -5.13 + 450.00 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_CWI_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_CWI_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_CWI_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_CWI_NIGH;
			double siteIndex = 2;

			double expectedResult = 18.18 - 0.5526 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_CWC_KURUCZ() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_CWC_KURUCZ;
			double siteIndex = 2;

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_CWC_KURUCZAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_CWC_KURUCZAC;
			double siteIndex = 2;

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_CWC_BARKER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_CWC_BARKER;
			double siteIndex = 2;

			double expectedResult = -3.46 + 285.00 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_CWC_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_CWC_NIGH;
			double siteIndex = 2;

			double expectedResult = 13.25 - siteIndex / 6.096;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BA_DILUCCA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BA_DILUCCA;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BB_KER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BB_KER;
			double siteIndex = 2;

			double expectedResult = 18.47373 - siteIndex / 2.447;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BP_CURTIS() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BP_CURTIS;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BP_CURTISAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BP_CURTISAC;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BA_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BA_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_BA_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BA_NIGH;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BA_KURUCZ86() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BA_KURUCZ86;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BA_KURUCZ82() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BA_KURUCZ82;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BA_KURUCZ82AC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BA_KURUCZ82AC;
			double siteIndex = 2;

			double expectedResult = 18.47373 - 0.4086 * siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BL_CHEN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BL_CHEN;
			double siteIndex = 2;

			double expectedResult = 42.25 - 10.66 * SiteIndexUtilities.llog(siteIndex);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BL_CHENAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BL_CHENAC;
			double siteIndex = 2;

			double expectedResult = 42.25 - 10.66 * SiteIndexUtilities.llog(siteIndex);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_BL_THROWERGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BL_THROWERGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_BL_KURUCZ82() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_BL_KURUCZ82;
			double siteIndex = 2;

			double expectedResult = 42.25 - 10.66 * SiteIndexUtilities.llog(siteIndex);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 5;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_FDI_HUANG_PLA() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_HUANG_PLA;
			double siteIndex = 2;

			double expectedResult = 6.5 + 5.276585 + 38.968242 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_HUANG_NAT() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_HUANG_NAT;
			double siteIndex = 2;

			double expectedResult = 8.0 + 5.276585 + 38.968242 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_MILNER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_MILNER;
			double siteIndex = 2;

			double expectedResult = 4.0 + 99.0 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_THROWER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_THROWER;
			double siteIndex = 2;

			double expectedResult = 4.0 + 99.0 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_THROWERAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_THROWERAC;
			double siteIndex = 2;

			double expectedResult = 4.0 + 99.0 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_VDP_MONT() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_VDP_MONT;
			double siteIndex = 2;

			double expectedResult = 4.0 + 99.0 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_VDP_WASH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_VDP_WASH;
			double siteIndex = 2;

			double expectedResult = 4.0 + 99.0 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_MONS_DF() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_MONS_DF;
			double siteIndex = 2;

			double expectedResult = 16.0 - siteIndex / 3.0;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 8;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_MONS_GF() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_MONS_GF;
			double siteIndex = 2;

			double expectedResult = 16.0 - siteIndex / 3.0;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 8;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_MONS_WRC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_MONS_WRC;
			double siteIndex = 2;

			double expectedResult = 16.0 - siteIndex / 3.0;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 8;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_MONS_WH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_MONS_WH;
			double siteIndex = 2;

			double expectedResult = 16.0 - siteIndex / 3.0;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 8;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDI_MONS_SAF() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_FDI_MONS_SAF;
			double siteIndex = 2;

			double expectedResult = 16.0 - siteIndex / 3.0;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 8;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_AT_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_AT_NIGH;
			double siteIndex = 3.5;

			double expectedResult = 1.331 + 38.56 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_AT_CHEN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_AT_CHEN;
			double siteIndex = 3.5;

			double expectedResult = 1.331 + 38.56 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_AT_HUANG() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_AT_HUANG;
			double siteIndex = 5.0;

			double expectedResult = 1 + 2.184066 + 50.788746 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_AT_GOUDIE() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_AT_GOUDIE;
			double siteIndex = 4.0;

			double expectedResult = 1.331 + 38.56 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_ACB_HUANGAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_ACB_HUANGAC;
			double siteIndex = 2;

			double expectedResult = 1 - 1.196472 + 104.124205 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_ACB_HUANG() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_ACB_HUANG;
			double siteIndex = 2;

			double expectedResult = 1 - 1.196472 + 104.124205 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_ACT_THROWER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_ACT_THROWER;
			double siteIndex = 4.0;

			double expectedResult = 2;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_ACT_THROWERAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_ACT_THROWERAC;
			double siteIndex = 4.0;

			double expectedResult = 2;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_AT_CIESZEWSKI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_AT_CIESZEWSKI;
			double siteIndex = 4.0;

			double expectedResult = 1.331 + 38.56 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_DR_HARRING() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_DR_HARRING;
			double siteIndex = 2;

			double expectedResult = 2;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_DR_CHEN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_DR_CHEN;
			double siteIndex = 2;

			double expectedResult = 2;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_DR_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_DR_NIGH;
			double siteIndex = 2;

			double expectedResult = 5.494 - 0.1789 * (0.3094 + 0.7616 * siteIndex);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			siteIndex = 600;
			expectedResult = 1;
			actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PY_NIGHGI() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PY_NIGHGI;

			assertThrows(GrowthInterceptTotalException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}

		@Test
		void testSI_PY_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PY_NIGH;
			double siteIndex = 3.5;

			double expectedResult = 36.35 * Math.pow(0.9318, siteIndex);
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PY_HANN() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PY_HANN;
			double siteIndex = 3.5;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PY_HANNAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PY_HANNAC;
			double siteIndex = 3.5;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PY_MILNER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PY_MILNER;
			double siteIndex = 3.5;

			double expectedResult = 2 + 3.6 + 42.64 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_LW_MILNER() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_LW_MILNER;
			double siteIndex = 3.5;

			double expectedResult = 3.36 + 87.18 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_LW_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_LW_NIGH;
			double siteIndex = 3.5;

			double expectedResult = 3.36 + 87.18 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_EP_NIGH() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_EP_NIGH;
			double siteIndex = 3.5;

			double expectedResult = 1.331 + 38.56 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PW_CURTIS() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PW_CURTIS;
			double siteIndex = 3.5;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_PW_CURTISAC() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = SI_PW_CURTISAC;
			double siteIndex = 2;

			double expectedResult = 2.0 + 2.1578 + 110.76 / siteIndex;
			double actualResult = SiteIndexYears2BreastHeight.y2bh(cuIndex, siteIndex);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSwitchDefault() throws CommonCalculatorException {
			SiteIndexEquation cuIndex = null;
			assertThrows(CurveErrorException.class, () -> SiteIndexYears2BreastHeight.y2bh(cuIndex, 2.0));
		}
	}

	@Test
	void testSi_y2bh05() throws CommonCalculatorException {
		SiteIndexEquation cuIndex = SI_PW_CURTIS;
		double siteIndex = 3.5;

		double expectedResult = ((int) (2.0 + 2.1578 + 110.76 / siteIndex)) + 0.5;
		double actualResult = SiteIndexYears2BreastHeight.y2bh05(cuIndex, siteIndex);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}
}
