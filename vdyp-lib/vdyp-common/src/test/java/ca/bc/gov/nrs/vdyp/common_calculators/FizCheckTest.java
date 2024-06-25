package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone.FIZ_COAST;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone.FIZ_INTERIOR;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone.FIZ_UNKNOWN;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexForestInventoryZone;

class FizCheckTest {

	@Test
	void testCoastalFiz() {
		for (char fiz = 'A'; fiz <= 'C'; fiz++) {
			SiteIndexForestInventoryZone result = ForestInventoryZone.toRegion(fiz);
			assertEquals(FIZ_COAST, result);
		}
	}

	@Test
	void testInteriorFiz() {
		for (char fiz = 'D'; fiz <= 'L'; fiz++) {
			SiteIndexForestInventoryZone result = ForestInventoryZone.toRegion(fiz);
			assertEquals(FIZ_INTERIOR, result);
		}
	}

	@Test
	void testUnknownFiz() {
		char fiz = 'X'; // Replace with any unknown fiz value
		SiteIndexForestInventoryZone result = ForestInventoryZone.toRegion(fiz);
		assertEquals(FIZ_UNKNOWN, result);
	}
}
