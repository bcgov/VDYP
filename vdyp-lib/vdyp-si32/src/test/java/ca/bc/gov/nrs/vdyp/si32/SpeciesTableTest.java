package ca.bc.gov.nrs.vdyp.si32;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.si32.vdyp.SP64Name;
import ca.bc.gov.nrs.vdyp.si32.vdyp.SpeciesDetails;
import ca.bc.gov.nrs.vdyp.si32.vdyp.SpeciesTable;

class SpeciesTableTest {

	private static SpeciesTable speciesTable;

	@BeforeAll
	static void beforeAll() {
		speciesTable = new SpeciesTable();
	}

	@Test
	void testSpeciesTable() {
		assertThat(speciesTable.getByCode(SP64Name.ABAL.name()).details().codeName(), is(SP64Name.ABAL.name()));
		assertThat(speciesTable.getNSpecies(), is(SP64Name.values().length - 1));
	}

	@Test
	void testSpeciesDetails() {
		SpeciesDetails abalDetails = speciesTable.getByCode(SP64Name.ABAL.name()).details();
		SpeciesDetails hwcDetails = speciesTable.getByCode(SP64Name.HWC.name()).details();

		assertTrue(hwcDetails.hashCode() == hwcDetails.hashCode());
		assertThat(abalDetails.toString(), Matchers.notNullValue());
		assertTrue(hwcDetails.equals(hwcDetails));
		assertFalse(hwcDetails.equals(abalDetails));
	}
}
