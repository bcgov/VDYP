package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_BREAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexAgeType.SI_AT_TOTAL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_ACB_HUANGAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_ACT_THROWERAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_AT_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_BA_KURUCZ82AC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_BA_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_BL_CHENAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_CWI_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_DR_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_EP_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDC_BRUCEAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDC_BRUCENIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDC_NIGHTA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_FDI_THROWERAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_HWI_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_LW_MILNER;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_LW_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PJ_HUANG;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PJ_HUANGAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PLI_NIGHTA2004;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PLI_NIGHTA98;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PLI_THROWNIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PW_CURTISAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PY_HANNAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_PY_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SB_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SE_CHENAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SE_NIGHTA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SS_NIGH;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_GOUDIE_NATAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_GOUDIE_PLAAC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_NIGHTA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation.SI_SW_NIGHTA2004;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.AgeTypeErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;

class AgeToAgeTest {

	private static final double ERROR_TOLERANCE = 0.00001;

	@Test
	void testShouldBranchInvalid() {
		assertThrows(
				AgeTypeErrorException.class,
				() -> AgeToAge.ageToAge(SI_ACB_HUANGAC, 0.0, SI_AT_BREAST, SI_AT_BREAST, 0.0)
		);

		assertThrows(
				AgeTypeErrorException.class,
				() -> AgeToAge.ageToAge(SI_ACT_THROWERAC, 0.0, SI_AT_TOTAL, SI_AT_TOTAL, 0.0)
		);
	}

	@Test
	void testShouldBranchReturnValue() throws AgeTypeErrorException {
		double expectedResult = 3.0; // normal calculated test
		double actualResult = AgeToAge.ageToAge(SI_AT_NIGH, 1.5, SI_AT_BREAST, SI_AT_TOTAL, 2);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

		expectedResult = 0; // returnValue < 0
		actualResult = AgeToAge.ageToAge(SI_BA_KURUCZ82AC, 0, SI_AT_BREAST, SI_AT_TOTAL, 0);

		expectedResult = 1.0; // normal calculated test
		actualResult = AgeToAge.ageToAge(SI_BA_NIGH, 1.5, SI_AT_TOTAL, SI_AT_BREAST, 1);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

		expectedResult = 0; // returnValue < 0
		actualResult = AgeToAge.ageToAge(SI_BL_CHENAC, 0, SI_AT_TOTAL, SI_AT_BREAST, 1);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}

	@Test
	void testShouldNotBranchReturnValue() throws AgeTypeErrorException {
		double expectedResult = 3.0; // normal calculated test
		double actualResult = AgeToAge.ageToAge(SI_LW_MILNER, 1, SI_AT_BREAST, SI_AT_TOTAL, 2);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

		expectedResult = 0; // returnValue < 0
		actualResult = AgeToAge.ageToAge(SI_LW_MILNER, 0, SI_AT_BREAST, SI_AT_TOTAL, -2);

		expectedResult = 1.0; // normal calculated test
		actualResult = AgeToAge.ageToAge(SI_LW_MILNER, 2, SI_AT_TOTAL, SI_AT_BREAST, 1);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));

		expectedResult = 0; // returnValue < 0
		actualResult = AgeToAge.ageToAge(SI_LW_MILNER, 0, SI_AT_TOTAL, SI_AT_BREAST, 1);

		assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
	}

	@Test
	void testShouldBranchNoIfMatch() {
		assertThrows(AgeTypeErrorException.class, () -> AgeToAge.ageToAge(SI_SS_NIGH, 0.0, null, null, 0.0));
	}

	@Test
	void testUncheckedCasesWithShouldBranch() throws AgeTypeErrorException {
		double expectedResult = 3.0; // normal calculated test
		SiteIndexEquation[] cases = { SI_CWI_NIGH, SI_DR_NIGH, SI_EP_NIGH, SI_FDC_BRUCENIGH, SI_FDC_BRUCEAC,
				SI_FDC_NIGHTA, SI_FDI_THROWERAC, SI_HWI_NIGH, SI_LW_NIGH, SI_PJ_HUANG, SI_PJ_HUANGAC, SI_PLI_NIGHTA2004,
				SI_PLI_NIGHTA98, SI_PLI_THROWNIGH, SI_PW_CURTISAC, SI_PY_HANNAC, SI_PY_NIGH, SI_SB_NIGH, SI_SE_CHENAC,
				SI_SE_NIGHTA, SI_SW_GOUDIE_NATAC, SI_SW_GOUDIE_PLAAC, SI_SW_NIGHTA2004, SI_SW_NIGHTA };

		for (SiteIndexEquation caseValue : cases) {
			double actualResult = AgeToAge.ageToAge(caseValue, 1.5, SI_AT_BREAST, SI_AT_TOTAL, 2);
			assertThat(actualResult, closeTo(expectedResult, ERROR_TOLERANCE));
		}

	}
}
