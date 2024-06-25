package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_ACT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_AT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_CWC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_CWI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_DR;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_EA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_EP;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_FDC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_FDI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HWC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HWI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_LA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_LT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_LW;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_MB;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PF;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PLI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PW;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PY;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SB;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SE;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SS;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SW;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_YC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.ClassErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.ForestInventoryZoneException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.SpeciesErrorException;

class SiteClassCode2SiteIndexTest {
	private static final char[] validSiteCl = { 'G', 'M', 'P', 'L' };

	@Test
	void testClassErrorExcepion() throws CommonCalculatorException {
		assertThrows(ClassErrorException.class, () -> SiteClassCode2SiteIndex.classToIndex(SI_SPEC_ACT, 'A', 'A'));
	}

	@Test
	void testSI_SPEC_ActAndMb() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_MB, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 26);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_ACT, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 18);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_ACT, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 9);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_ACT, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 3);
	}

	@Test
	void testSI_SPEC_AtEaAndEp() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_AT, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 27);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_EA, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 20);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_EP, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 12);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_EP, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 4);
	}

	@Test
	void testSI_SPEC_BA() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BA, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 29);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BA, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 23);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BA, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 14);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BA, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 5);
	}

	@Test
	void testSI_SPEC_BL() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BL, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 18);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BL, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 15);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BL, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 11);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_BL, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 5);
	}

	@Test
	void testSI_SPEC_CwcAndYc() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_CWC, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 29);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_YC, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 23);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_YC, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 15);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_YC, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 6);
	}

	@Test
	void testSI_SPEC_Cwi() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_CWI, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 22);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_CWI, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 19);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_CWI, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 13);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_CWI, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 5);
	}

	@Test
	void testSI_SPEC_Dr() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_DR, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 33);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_DR, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 23);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_DR, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 13);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_DR, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 6);
	}

	@Test
	void testSI_SPEC_Fdc() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDC, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 32);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDC, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 27);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDC, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 18);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDC, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 7);
	}

	@Test
	void testSI_SPEC_Fdi() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDI, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 20);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDI, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 17);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDI, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 12);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_FDI, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 5);
	}

	@Test
	void testSI_SPEC_Hwc() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 28);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 22);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 14);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 5);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[0], 'D');
		assertEquals(actualResult, (double) 21);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[1], 'D');
		assertEquals(actualResult, (double) 18);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[2], 'D');
		assertEquals(actualResult, (double) 12);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[3], 'D');
		assertEquals(actualResult, (double) 4);

		assertThrows(
				ForestInventoryZoneException.class,
				() -> SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWC, validSiteCl[3], 'X')
		);
	}

	@Test
	void testSI_SPEC_Hwi() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWI, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 21);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWI, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 18);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWI, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 12);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_HWI, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 4);
	}

	@Test
	void testSI_SPEC_LaLtAndLw() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_LW, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 20);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_LA, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 16);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_LT, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 10);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_LW, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 3);
	}

	@Test
	void testSI_SPEC_PliPaAndPf() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PLI, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 20);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PA, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 16);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PF, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 11);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PF, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 4);
	}

	@Test
	void testSI_SPEC_Py() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PY, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 17);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PY, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 14);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PY, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 10);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PY, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 4);
	}

	@Test
	void testSI_SPEC_Pw() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PW, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 28);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PW, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 22);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PW, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 12);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_PW, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 4);
	}

	@Test
	void testSI_SPEC_Ss() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SS, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 28);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SS, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 21);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SS, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 11);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SS, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 4);
	}

	@Test
	void testSI_SPEC_SbSwAndSe() throws CommonCalculatorException {
		double actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SB, validSiteCl[0], 'A');
		assertEquals(actualResult, (double) 19);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SW, validSiteCl[1], 'A');
		assertEquals(actualResult, (double) 15);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SE, validSiteCl[2], 'A');
		assertEquals(actualResult, (double) 10);

		actualResult = SiteClassCode2SiteIndex.classToIndex(SI_SPEC_SE, validSiteCl[3], 'A');
		assertEquals(actualResult, (double) 5);
	}

	@Test
	void testSpeciesErrorException() throws CommonCalculatorException {
		assertThrows(
				SpeciesErrorException.class, () -> SiteClassCode2SiteIndex.classToIndex(null, validSiteCl[3], 'X')
		);
	}
}
