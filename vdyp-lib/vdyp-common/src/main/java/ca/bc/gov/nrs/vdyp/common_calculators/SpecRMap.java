package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_ACB;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_ACT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_AT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BP;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_CWC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_CWI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_DR;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_EP;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_FDC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_FDI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HM;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HWC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HWI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_LW;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PJ;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PLI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PW;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PY;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SB;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SE;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SS;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SW;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CodeErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies;

/**
 * SpecRMap.java - determines the default species/curve index for a given species code. - initial species code
 * remappings provided by Inventory Branch. - species codes can be 1-3 letters, in upper or lower case.
 */
public class SpecRMap {

	public static SiteIndexSpecies species_remap(String sc, char fiz) throws CodeErrorException {

		if (sc != null) {

			String sc2 = sc.replaceAll(" ", "").toUpperCase();

			if (sc2.equals("A")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("ABAL")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("ABCO")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("AC")) {
				return SI_SPEC_ACB;
			}
			if (sc2.equals("ACB")) {
				return SI_SPEC_ACB;
			}
			if (sc2.equals("ACT")) {
				return SI_SPEC_ACT;
			}
			if (sc2.equals("AD")) {
				return SI_SPEC_ACT;
			}
			if (sc2.equals("AH")) {
				return SI_SPEC_ACT;
			}
			if (sc2.equals("AT")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("AX")) {
				return SI_SPEC_ACB;
			}
			if (sc2.equals("B")) {
				return speciesByFizCategory(fiz, SI_SPEC_BA, SI_SPEC_BL);
			}
			if (sc2.equals("BA")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("BAC")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("BAI")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("BB")) {
				return SI_SPEC_BL;
			}
			if (sc2.equals("BC")) {
				return speciesByFizCategory(fiz, SI_SPEC_BA, SI_SPEC_BL);
			}
			if (sc2.equals("BG")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("BI")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("BL")) {
				return SI_SPEC_BL;
			}
			if (sc2.equals("BM")) {
				return SI_SPEC_BA;
			}
			if (sc2.equals("BN")) {
				return SI_SPEC_BP;
			}
			if (sc2.equals("BP")) {
				return SI_SPEC_BP;
			}
			// if (sc2.equals("BV")) {
			// return SI_SPEC_AT;
			// }
			if (sc2.equals("C")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("CI")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("COT")) {
				return SI_SPEC_ACT;
			}
			if (sc2.equals("CP")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("CT")) {
				return SI_SPEC_ACT;
			}
			if (sc2.equals("CW")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("CWC")) {
				return SI_SPEC_CWC;
			}
			if (sc2.equals("CWI")) {
				return SI_SPEC_CWI;
			}
			if (sc2.equals("CY")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("D")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("DF")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("DG")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("DM")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("DR")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("E")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("EA")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("EB")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("EE")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("EP")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("ES")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("EW")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("EXP")) {
				return SI_SPEC_EP;
			}
			if (sc2.equals("F")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("FD")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("FDC")) {
				return SI_SPEC_FDC;
			}
			if (sc2.equals("FDI")) {
				return SI_SPEC_FDI;
			}
			if (sc2.equals("G")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("GP")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("GR")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("H")) {
				return speciesByFizCategory(fiz, SI_SPEC_HWC, SI_SPEC_HWI);
			}
			if (sc2.equals("HM")) {
				return SI_SPEC_HM;
			}
			if (sc2.equals("HW")) {
				return speciesByFizCategory(fiz, SI_SPEC_HWC, SI_SPEC_HWI);
			}
			if (sc2.equals("HWC")) {
				return SI_SPEC_HWC;
			}
			if (sc2.equals("HWI")) {
				return SI_SPEC_HWI;
			}
			if (sc2.equals("HXM")) {
				return speciesByFizCategory(fiz, SI_SPEC_HWC, SI_SPEC_HWI);
			}
			if (sc2.equals("IG")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("IS")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("J")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("JR")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("K")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("KC")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("L")) {
				return SI_SPEC_LW;
			}
			if (sc2.equals("LA")) {
				return SI_SPEC_LW;
			}
			if (sc2.equals("LE")) {
				return SI_SPEC_LW;
			}
			if (sc2.equals("LT")) {
				return SI_SPEC_LW;
			}
			if (sc2.equals("LW")) {
				return SI_SPEC_LW;
			}
			if (sc2.equals("M")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("MB")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("ME")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("MN")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("MR")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("MS")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("MV")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("OA")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("OB")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("OC")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("OD")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("OE")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("OF")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("OG")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("P")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PA")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PF")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PJ")) {
				return SI_SPEC_PJ;
			}
			if (sc2.equals("PL")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PLC")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PLI")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PM")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PR")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PS")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PV")) {
				return SI_SPEC_PY;
			}
			if (sc2.equals("PW")) {
				return SI_SPEC_PW;
			}
			if (sc2.equals("PXJ")) {
				return SI_SPEC_PLI;
			}
			if (sc2.equals("PY")) {
				return SI_SPEC_PY;
			}
			if (sc2.equals("Q")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("QE")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("QG")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("R")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("RA")) {
				return SI_SPEC_DR;
			}
			if (sc2.equals("S")) { // Duplicate case? Unreachable
				return speciesByFizCategory(fiz, SI_SPEC_SS, SI_SPEC_SW);
			}
			if (sc2.equals("SA")) { // Duplicate case? Unreachable
				return SI_SPEC_SW;
			}
			if (sc2.equals("SB")) {
				return SI_SPEC_SB;
			}
			if (sc2.equals("SE")) {
				return SI_SPEC_SE;
			}
			if (sc2.equals("SI")) {
				return SI_SPEC_SW;
			}
			if (sc2.equals("SN")) {
				return SI_SPEC_SW;
			}
			if (sc2.equals("SS")) {
				return SI_SPEC_SS;
			}
			if (sc2.equals("SW")) {
				return SI_SPEC_SW;
			}
			if (sc2.equals("SX")) {
				return speciesByFizCategory(fiz, SI_SPEC_SS, SI_SPEC_SW);
			}
			if (sc2.equals("SXB")) {
				return SI_SPEC_SW;
			}
			if (sc2.equals("SXE")) {
				return speciesByFizCategory(fiz, SI_SPEC_SS, SI_SPEC_SE);
			}
			if (sc2.equals("SXL")) {
				return speciesByFizCategory(fiz, SI_SPEC_SS, SI_SPEC_SW);
			}
			if (sc2.equals("SXS")) {
				return speciesByFizCategory(fiz, SI_SPEC_SS, SI_SPEC_SW);
			}
			if (sc2.equals("SXW")) {
				return SI_SPEC_SW;
			}
			if (sc2.equals("SXX")) {
				return speciesByFizCategory(fiz, SI_SPEC_SS, SI_SPEC_SW);
			}
			if (sc2.equals("T")) {
				return speciesByFizCategory(fiz, SI_SPEC_HWC, SI_SPEC_HWI);
			}
			if (sc2.equals("TW")) {
				return speciesByFizCategory(fiz, SI_SPEC_HWC, SI_SPEC_HWI);
			}
			if (sc2.equals("U")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("UA")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("UP")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("V")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("VB")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("VP")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("VS")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("VV")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("W")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WA")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WB")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WD")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WI")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WP")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WS")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("WT")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("X")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("XC")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("XH")) {
				return SI_SPEC_AT;
			}
			if (sc2.equals("Y")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("YC")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("YP")) {
				return speciesByFizCategory(fiz, SI_SPEC_CWC, SI_SPEC_CWI);
			}
			if (sc2.equals("Z")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("ZC")) {
				return speciesByFizCategory(fiz, SI_SPEC_FDC, SI_SPEC_FDI);
			}
			if (sc2.equals("ZH")) {
				return SI_SPEC_AT;
			}
		}

		throw new CodeErrorException("Unknown species code: " + sc);
	}

	private static SiteIndexSpecies
			speciesByFizCategory(char fiz, SiteIndexSpecies coastalSpecies, SiteIndexSpecies interiorSpecies)
					throws CodeErrorException {
		switch (ForestInventoryZone.toRegion(fiz)) {
		case FIZ_COAST:
			return coastalSpecies;
		case FIZ_INTERIOR:
			return interiorSpecies;
		default:
			throw new CodeErrorException("Unknown forest inventory code: " + fiz);
		}
	}
}
