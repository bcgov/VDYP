package ca.bc.gov.nrs.vdyp.common_calculators;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;

public class SiteIndex2AgeTest {
	// Taken from sindex.h
	/*
	 * age types
	 */
	private static final short SI_AT_TOTAL = 0;
	private static final short SI_AT_BREAST = 1;

	/*
	 * site index estimation (from height and age) types
	 */
	private static final short SI_EST_DIRECT = 1;

	/*
	 * error codes as return values from functions
	 */
	private static final short SI_ERR_GI_MAX = -3;
	private static final short SI_ERR_NO_ANS = -4;

	/* define species and equation indices */
	private static final short SI_BL_THROWERGI = 9;
	private static final short SI_CWI_NIGHGI = 84;
	private static final short SI_FDC_BRUCE = 16;
	private static final short SI_FDC_NIGHGI = 15;
	private static final short SI_FDI_NIGHGI = 19;
	private static final short SI_HWC_NIGHGI = 31;
	private static final short SI_HWC_NIGHGI99 = 79;
	private static final short SI_HWC_WILEY = 34;
	private static final short SI_HWI_NIGHGI = 38;
	private static final short SI_LW_NIGHGI = 82;
	private static final short SI_PLI_GOUDIE_DRY = 48;
	private static final short SI_PLI_GOUDIE_WET = 49;
	private static final short SI_PLI_NIGHGI97 = 42;
	private static final short SI_SS_GOUDIE = 60;
	private static final short SI_SS_NIGHGI = 58;
	private static final short SI_SS_NIGHGI99 = 80;
	private static final short SI_SW_GOUDIE_NAT = 71;
	private static final short SI_SW_GOUDIE_PLA = 70;
	private static final short SI_SW_HU_GARCIA = 119;
	private static final short SI_SW_NIGHGI = 63;
	private static final short SI_SW_NIGHGI99 = 81;

	private static final double ERROR_TOLERANCE = 0.00001;

	@Test
	void testPpowPositive() {
		assertThat(8.0, closeTo(SiteIndex2Age.ppow(2.0, 3.0), ERROR_TOLERANCE));
		assertThat(1.0, closeTo(SiteIndex2Age.ppow(5.0, 0.0), ERROR_TOLERANCE));
	}

	@Test
	void testPpowZero() {
		assertThat(0.0, closeTo(SiteIndex2Age.ppow(0.0, 3.0), ERROR_TOLERANCE));
	}

	@Test
	void testLlogPositive() {
		assertThat(1.60943, closeTo(SiteIndex2Age.llog(5.0), ERROR_TOLERANCE));
		assertThat(11.51293, closeTo(SiteIndex2Age.llog(100000.0), ERROR_TOLERANCE));
	}

	@Test
	void testLlogZero() {
		assertThat(-11.51293, closeTo(SiteIndex2Age.llog(0.0), ERROR_TOLERANCE));
	}

	@Nested
	class index_to_ageTest {
		@Test
		void testSiteHeightLessTooSmall() {
			// Test where SiteHeight < 1.3, AgeType = 1 (SI_AT_BREAST)
			assertThrows(
					LessThan13Exception.class,
					() -> SiteIndex2Age.index_to_age(SI_BL_THROWERGI, 0.0, SI_AT_BREAST, SI_AT_BREAST, 0.0)
			);

			// Test where SiteHeight <= 0.0001
			double expectedResult = 0;
			double actualResult = SiteIndex2Age.index_to_age(SI_AT_TOTAL, 0.0001, SI_AT_TOTAL, SI_AT_BREAST, 0.0);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSiteIndexLessTooSmall() {
			// Test where SiteHeight < 1.3, AgeType = 1 (SI_AT_BREAST)
			assertThrows(
					LessThan13Exception.class,
					() -> SiteIndex2Age.index_to_age(SI_BL_THROWERGI, 1.4, SI_AT_BREAST, SI_AT_BREAST, 0.0)
			);
		}

		@Test
		void testSI_FDC_BRUCEValid() {
			double site_height = 1.5;
			double site_index = 25.0;

			double y2bh = 13.25 - site_index / 6.096;
			double x1 = site_index / 30.48;
			double x2 = -0.477762 + x1 * (-0.894427 + x1 * (0.793548 - x1 * 0.171666));
			double x3 = SiteIndex2Age.ppow(50.0 + y2bh, x2);
			double x4 = SiteIndex2Age.llog(1.372 / site_index) / (SiteIndex2Age.ppow(y2bh, x2) - x3);
			x1 = SiteIndex2Age.llog(site_height / site_index) / x4 + x3;

			double actualResult = SiteIndex2Age.index_to_age(SI_FDC_BRUCE, site_height, SI_AT_BREAST, site_index, 12.0);
			double expectedResult = (SiteIndex2Age.ppow(x1, 1 / x2)) - y2bh;

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_FDC_BRUCEInvalidDenominator() {
			double site_index = 900;
			double site_height = 0.00011;

			assertThrows(
					ArithmeticException.class,
					() -> SiteIndex2Age.index_to_age(SI_FDC_BRUCE, site_height, SI_FDC_BRUCE, site_index, 12)
			);
		}

		@Test
		void testSI_FDC_BRUCEX1TooSmall() {
			double site_index = 100.0;
			double site_height = 1.31;

			assertThrows(
					NoAnswerException.class,
					() -> SiteIndex2Age.index_to_age(SI_FDC_BRUCE, site_height, SI_AT_BREAST, site_index, 12.0)
			);
		}

		// TODO figure out if this is possible (dont think so)
		/*
		 * @Test void testSI_FDC_BRUCEAgeTooSmall() { double site_index = 1.38; double
		 * site_height = 0.0001000009;
		 *
		 * double expectedResult = 0; double actualResult =
		 * SiteIndex2Age.index_to_age(SI_FDC_BRUCE, site_height,
		 * SI_FDC_BRUCE,site_index, 12);
		 *
		 * assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE)); }
		 */

		@Test
		void testSI_FDC_BRUCEAgeTooBig() {
			double site_index = 1.5;
			double site_height = 1.6;

			assertThrows(
					NoAnswerException.class,
					() -> SiteIndex2Age.index_to_age(SI_FDC_BRUCE, site_height, SI_AT_BREAST, site_index, 12.0)
			);
		}

		@Test
		void testSI_SW_HU_GARCIAValid() {
			double site_index = 2;
			double site_height = 1.6;

			double expectedResult = SiteIndex2Age.hu_garcia_bha(0.002242150170898438, 1.6);
			double actualResult = SiteIndex2Age
					.index_to_age(SI_SW_HU_GARCIA, site_height, SI_AT_BREAST, site_index, 12.0);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testSI_SW_HU_GARCIASI_AT_TOTAL() {
			double site_index = 2;
			double site_height = 1.6;

			double expectedResult = 12 + SiteIndex2Age.hu_garcia_bha(0.002242150170898438, 1.6);
			double actualResult = SiteIndex2Age
					.index_to_age(SI_SW_HU_GARCIA, site_height, SI_AT_TOTAL, site_index, 12.0);

			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}
	}

	@Test
	void testHuGarciaQ() {
		double site_index = 20.0;
		double bhage = 15.0;
		double expectedResult = 0.0452838897705078; // Expected result based on input values

		double actualResult = SiteIndex2Age.hu_garcia_q(site_index, bhage);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}

	@Test
	void testHuGarciaH() {
		double q = 2;
		double bhage = 15.0;
		double expectedResult = 405.32603588881113; // Expected result based on input values

		double actualResult = SiteIndex2Age.hu_garcia_h(q, bhage);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}

	@Test
	void testHuGarciaBha() {
		double q = 2;
		double bhage = 15.0;
		double expectedResult = 0.5612209407277136; // Expected result based on input values

		double actualResult = SiteIndex2Age.hu_garcia_bha(q, bhage);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}
}