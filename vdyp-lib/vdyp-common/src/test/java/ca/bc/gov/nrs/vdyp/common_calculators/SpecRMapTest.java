package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CodeErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies;

class SpecRMapTest {

	@Nested
	class SpeciesMapTest {
		@Test
		void testAllSpeciesUppercase() throws CommonCalculatorException { // loop through each case
			for (SiteIndexSpecies s : SiteIndexSpecies.values()) { // test all normal cases
				if (s != SiteIndexSpecies.SI_NO_SPECIES) {
					SiteIndexSpecies expectedResult = s;
					SiteIndexSpecies actualResult = SiteIndexSpecies.getByCode(s.getCode().toUpperCase());
					assertEquals(expectedResult, actualResult);
				}
			}

			assertThrows(CodeErrorException.class, () -> SiteIndexSpecies.getByCode(null));
			assertThrows(CodeErrorException.class, () -> SiteIndexSpecies.getByCode("zzz"));
		}

		@Test
		void testSpeciesLowercase() throws CommonCalculatorException {
			SiteIndexSpecies actualResult = SiteIndexSpecies.getByCode("abal");
			SiteIndexSpecies expectedResult = SI_SPEC_ABAL;

			assertEquals(expectedResult, actualResult);
		}

		@Test
		void testSpeciesMixedcase() throws CommonCalculatorException {
			SiteIndexSpecies actualResult = SiteIndexSpecies.getByCode("AbCo");
			SiteIndexSpecies expectedResult = SI_SPEC_ABCO;

			assertEquals(expectedResult, actualResult);
		}

		@Test
		void testSpeciesSpaces() throws CommonCalculatorException {
			SiteIndexSpecies actualResult = SiteIndexSpecies.getByCode("    c    W   I   ");
			SiteIndexSpecies expectedResult = SI_SPEC_CWI;

			assertEquals(expectedResult, actualResult);
		}
	}

	@Nested
	class SpeciesRemapTest {
		// Spaces and lowe/uppercase is tested throughout but mainly in testReturnERROR
		@Test
		void testReturnSI_SPEC_BA() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_BA;
			String[] cases = { "ABCO", "bA", "BaC", "BAi", "BG", "BM", "B", "BC" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_ACB() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_ACB;
			String[] cases = { "AC", "ACB", "AX" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'X');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_ACT() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_ACT;
			String[] cases = { "ACT", "AD", "AH", "COT", "CT" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'X');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_AT() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_AT;
			String[] cases = { "AT", "BI", "K", "KC", "OD", "OE", "OF", "OG", "Q", "QE", "QG", "U", "UA", "UP", "V",
					"VB", "VP", "VS", "VV", "W", "WA", "WB", "WD", "WI", "WP", "WS", "WT", "XH", "ZH" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'X');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_BL() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_BL;
			String[] cases = { "BB", "BL", "B", "BC" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_BP() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_BP;
			String[] cases = { "BN", "BP" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'X');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_CWC() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_CWC;
			String[] cases = { "C", "CI", "CP", "CW", "CWC", "CY", "IG", "IS", "J", "JR", "OA", "OB", "OC", "Y", "YC",
					"YP" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_CWI() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_CWI;
			String[] cases = { "C", "CI", "CP", "CW", "CWI", "CY", "IG", "IS", "J", "JR", "OA", "OB", "OC", "Y", "YC",
					"YP" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_DR() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_DR;
			String[] cases = { "D", "DG", "DM", "DR", "G", "GP", "GR", "M", "MB", "ME", "MN", "MR", "MS", "MV", "R",
					"RA" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_FDC() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_FDC;
			String[] cases = { "DF", "F", "FD", "FDC", "X", "XC", "Z", "ZC", };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_FDI() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_FDI;
			String[] cases = { "DF", "F", "FD", "FDI", "X", "XC", "Z", "ZC", };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_EP() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_EP;
			String[] cases = { "E", "EA", "EB", "EE", "EP", "ES", "EW", "EXP" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_HWC() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_HWC;
			String[] cases = { "H", "HW", "HWC", "HXM", "T", "TW", };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_HWI() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_HWI;
			String[] cases = { "H", "HW", "HWI", "HXM", "T", "TW", };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_HM() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_HM;
			SiteIndexSpecies actualResult = SpecRMap.species_remap("HM", 'A');
			assertEquals(expectedResult, actualResult);
		}

		@Test
		void testReturnSI_SPEC_LW() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_LW;
			String[] cases = { "L", "LA", "LE", "LT", "LW" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_PLI() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_PLI;
			String[] cases = { "P", "PA", "PF", "PL", "PLC", "PLI", "PM", "PR", "PS", "PXJ" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_PJ() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_PJ;
			SiteIndexSpecies actualResult = SpecRMap.species_remap("PJ", 'A');
			assertEquals(expectedResult, actualResult);
		}

		@Test
		void testReturnSI_SPEC_PY() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_PY;
			String[] cases = { "PV", "PY" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_PW() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_PW;
			SiteIndexSpecies actualResult = SpecRMap.species_remap("PW", 'A');
			assertEquals(expectedResult, actualResult);
		}

		@Test
		void testReturnSI_SPEC_SS() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_SS;
			String[] cases = { "S", "SS", "SX", "SXE", "SXL", "SXS", "SXX" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'A');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_SB() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_SB;
			SiteIndexSpecies actualResult = SpecRMap.species_remap("SB", 'A');
			assertEquals(expectedResult, actualResult);
		}

		@Test
		void testReturnSI_SPEC_SE() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_SE;
			String[] cases = { "SXE", "SE" };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnSI_SPEC_SW() throws CommonCalculatorException {
			SiteIndexSpecies expectedResult = SI_SPEC_SW;
			String[] cases = { "SW", "S", "SA", "SI", "SN", "SX", "SXL", "SXS", "SXX", "SXB", "SXW", };

			for (int i = 0; i < cases.length; i++) {
				SiteIndexSpecies actualResult = SpecRMap.species_remap(cases[i], 'E');
				assertEquals(actualResult, expectedResult, "Error with this " + cases[i] + " case");
			}
		}

		@Test
		void testReturnERROR() {
			String[] cases = { "B", "BC", "C", "CI", "cp", "cw", "cy", "Df", "f", "f  d", "h", "hw", "hxm", "ig", "is",
					"j", "jr", "oa", "ob", "oc", "s", "sx", "sxe", "sxl", "sxs", "sxx", "t", "tw", "x", " X c", "y",
					"YC", "yp", "Z", "ZC", "ERIC" };

			for (String caseValue : cases) {
				assertThrows(CodeErrorException.class, () -> SpecRMap.species_remap(caseValue, 'X'));
			}
		}

	}

}
