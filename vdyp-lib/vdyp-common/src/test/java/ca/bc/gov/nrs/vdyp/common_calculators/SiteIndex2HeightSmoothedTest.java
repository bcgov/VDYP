package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_BREAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_TOTAL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDI_HUANG_NAT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PLI_THROWER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;

class SiteIndex2HeightSmoothedTest {
	private static final double ERROR_TOLERANCE = 0.00001;

	@Nested
	class IndexToHeightSmoothedTest {
		@Test
		void testInvalidSiteIndex() throws CommonCalculatorException {
			assertThrows(
					LessThan13Exception.class,
					() -> SiteIndex2HeightSmoothed.indexToHeightSmoothed(null, 0.0, SI_AT_TOTAL, 1.2, 0.0, 0.0, 0.0)
			);
		}

		@Test
		void testInvalidY2BH() throws CommonCalculatorException {
			assertThrows(
					NoAnswerException.class,
					() -> SiteIndex2HeightSmoothed.indexToHeightSmoothed(null, 0.0, SI_AT_TOTAL, 1.31, -1.0, 0.0, 0.0)
			);
		}

		@Test
		void testItageInvalid() throws CommonCalculatorException {
			assertThrows(
					NoAnswerException.class,
					() -> SiteIndex2HeightSmoothed.indexToHeightSmoothed(null, -1.0, SI_AT_BREAST, 1.31, 0.0, 0.0, 0.0)
			);

			double actualResult = SiteIndex2HeightSmoothed
					.indexToHeightSmoothed(null, 0.0, SI_AT_BREAST, 1.31, 0.0, 0.0, 0.0);
			double expectedResult = 0;
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

		@Test
		void testIterationCannotConverge() throws CommonCalculatorException {
			assertThrows(
					NoAnswerException.class,
					() -> SiteIndex2HeightSmoothed
							.indexToHeightSmoothed(SI_PLI_THROWER, 0.0, SI_AT_BREAST, 1.31, 1.0, 0, 0)
			);
		}

		@Test
		void testValidInput() throws CommonCalculatorException {
			double actualResult = SiteIndex2HeightSmoothed
					.indexToHeightSmoothed(SI_FDI_HUANG_NAT, 3.0, SI_AT_TOTAL, 16.0, 4.0, 3.1, 1.3);
			double expectedResult = 1.3 / 3.1 * 3;
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			actualResult = SiteIndex2HeightSmoothed
					.indexToHeightSmoothed(SI_FDI_HUANG_NAT, 3.0, SI_AT_TOTAL, 16.0, 4.0, 0.0, 1.0);

			double k1 = 2.6120353509515746; // based on calculation with traced values
			double k0 = (.3) / Math.pow(4, k1);
			expectedResult = k0 * Math.pow(3, k1);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

			actualResult = SiteIndex2HeightSmoothed
					.indexToHeightSmoothed(SI_FDI_HUANG_NAT, 3.0, SI_AT_TOTAL, 16.0, 4.0, 3.0, 1.0);

			expectedResult = 1; // since k0 * Math.pow(0, k1) should be 0
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

	}

}
