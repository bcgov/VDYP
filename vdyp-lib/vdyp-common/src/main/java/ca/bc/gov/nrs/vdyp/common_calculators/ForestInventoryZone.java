package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone.FIZ_COAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone.FIZ_INTERIOR;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone.FIZ_UNKNOWN;

import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone;

/**
 * Determines whether a given FIZ code represents the coast or interior.
 */
public class ForestInventoryZone {

	/**
	 * @param fiz a character identifying the forest inventory zone (A - L)
	 * @return the region containing the zone
	 */
	public static SiteIndexForestInventoryZone toRegion(char fiz) {
		switch (fiz) {
		case 'A', 'B', 'C':
			return FIZ_COAST;
		case 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L':
			return FIZ_INTERIOR;
		default:
			return FIZ_UNKNOWN;
		}
	}
}
