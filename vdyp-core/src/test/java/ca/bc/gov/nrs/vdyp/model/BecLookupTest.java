package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.BecLookup.Substitution;

public class BecLookupTest {

	@Test
	public void testSimpleGet() throws Exception {
		var lookup = new BecLookup(Arrays.asList(new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)));
		var result = lookup.get("ESSF", Substitution.PARTIAL_FILL_OK);

		assertThat(result, present(hasProperty("alias", is("ESSF"))));
	}

	@Test
	public void testGetMissing() throws Exception {
		var lookup = new BecLookup(Arrays.asList(new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)));
		var result = lookup.get("XX", Substitution.PARTIAL_FILL_OK);

		assertThat(result, notPresent());
	}

	@Test
	public void testWithSubstitution() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);
		var result = lookup.get("BG", Substitution.SUBSTITUTE);

		assertThat(
				result,
				present(
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(1))
						)
				)
		);
	}

	@Test
	public void testWithSubstitutionButNoneNeeded() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BWBS", Region.INTERIOR, "BWBS Test", 1, 2, 3),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);
		var result = lookup.get("BWBS", Substitution.SUBSTITUTE);

		assertThat(
				result,
				present(
						allOf(
								hasProperty("alias", is("BWBS")), hasProperty("growthIndex", is(1)),
								hasProperty("volumeIndex", is(2)), hasProperty("decayIndex", is(3))
						)
				)
		);
	}

	@Test
	public void testWithPartialAllowed() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);
		var result = lookup.get("BG", Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				present(
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(0)),
								hasProperty("volumeIndex", is(0)), hasProperty("decayIndex", is(1))
						)
				)
		);
	}

	@Test
	public void testWithNoSubstitution() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);
		var ex = assertThrows(IllegalArgumentException.class, () -> lookup.get("BG", Substitution.NONE));

		assertThat(ex, hasProperty("message", stringContainsInOrder("Substitution needed", "BEC BG")));
	}

	@Test
	public void testWithSubstitutionButDefaultIsMissing() throws Exception {
		var lookup = new BecLookup(Arrays.asList(new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1)));
		var ex = assertThrows(IllegalStateException.class, () -> lookup.get("BG", Substitution.SUBSTITUTE));

		assertThat(ex, hasProperty("message", stringContainsInOrder("Could not find default BEC", "ESSF")));

	}

	@Test
	public void testWithSubstitutionButDefaultIsIncomplete() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 0, 5, 6)
				)
		);
		var ex = assertThrows(IllegalStateException.class, () -> lookup.get("BG", Substitution.SUBSTITUTE));

		assertThat(ex, hasProperty("message", stringContainsInOrder("substitute", "BG", "default", "ESSF")));

	}

	@Test
	public void testGetBecsWithSubstitution() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecs(Substitution.SUBSTITUTE);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(1))
						),
						allOf(
								hasProperty("alias", is("ESSF")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(6))
						)
				)
		);
	}

	@Test
	public void testGetBecsAllowingPartial() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecs(Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(0)),
								hasProperty("volumeIndex", is(0)), hasProperty("decayIndex", is(1))
						),
						allOf(
								hasProperty("alias", is("ESSF")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(6))
						)
				)
		);
	}

	@Test
	public void testGetGrowthBecs() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("AT", Region.INTERIOR, "AT Test", 0, 1, 1),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getGrowthBecs(Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("ESSF")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(6))
						)
				)
		);
	}

	@Test
	public void testGetCoastalBecs() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("CDF", Region.COASTAL, "CDF Test", 1, 2, 3),
						new BecDefinition("CWH", Region.COASTAL, "CWH Test", 2, 3, 4),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecsForRegion(Region.COASTAL, Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("CDF")), hasProperty("growthIndex", is(1)),
								hasProperty("volumeIndex", is(2)), hasProperty("decayIndex", is(3))
						),
						allOf(
								hasProperty("alias", is("CWH")), hasProperty("growthIndex", is(2)),
								hasProperty("volumeIndex", is(3)), hasProperty("decayIndex", is(4))
						)
				)
		);
	}

	@Test
	public void testGetInteriorBecs() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("CDF", Region.COASTAL, "CDF Test", 1, 2, 3),
						new BecDefinition("CWH", Region.COASTAL, "CWH Test", 2, 3, 4),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecsForRegion(Region.INTERIOR, Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(0)),
								hasProperty("volumeIndex", is(0)), hasProperty("decayIndex", is(1))
						),
						allOf(
								hasProperty("alias", is("ESSF")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(6))
						)
				)
		);
	}

	@Test
	public void testGetByBlankScope() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("CDF", Region.COASTAL, "CDF Test", 1, 2, 3),
						new BecDefinition("CWH", Region.COASTAL, "CWH Test", 2, 3, 4),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecsForScope("", Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("CDF")), hasProperty("growthIndex", is(1)),
								hasProperty("volumeIndex", is(2)), hasProperty("decayIndex", is(3))
						),
						allOf(
								hasProperty("alias", is("CWH")), hasProperty("growthIndex", is(2)),
								hasProperty("volumeIndex", is(3)), hasProperty("decayIndex", is(4))
						),
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(0)),
								hasProperty("volumeIndex", is(0)), hasProperty("decayIndex", is(1))
						),
						allOf(
								hasProperty("alias", is("ESSF")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(6))
						)
				)
		);
	}

	@Test
	public void testGetByRegionScope() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("CDF", Region.COASTAL, "CDF Test", 1, 2, 3),
						new BecDefinition("CWH", Region.COASTAL, "CWH Test", 2, 3, 4),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecsForScope("I", Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("BG")), hasProperty("growthIndex", is(0)),
								hasProperty("volumeIndex", is(0)), hasProperty("decayIndex", is(1))
						),
						allOf(
								hasProperty("alias", is("ESSF")), hasProperty("growthIndex", is(4)),
								hasProperty("volumeIndex", is(5)), hasProperty("decayIndex", is(6))
						)
				)
		);
	}

	@Test
	public void testGetByBecScope() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("CDF", Region.COASTAL, "CDF Test", 1, 2, 3),
						new BecDefinition("CWH", Region.COASTAL, "CWH Test", 2, 3, 4),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecsForScope("CDF", Substitution.PARTIAL_FILL_OK);

		assertThat(
				result,
				containsInAnyOrder(
						allOf(
								hasProperty("alias", is("CDF")), hasProperty("growthIndex", is(1)),
								hasProperty("volumeIndex", is(2)), hasProperty("decayIndex", is(3))
						)
				)
		);
	}

	@Test
	public void testGetByMissingScope() throws Exception {
		var lookup = new BecLookup(
				Arrays.asList(
						new BecDefinition("CDF", Region.COASTAL, "CDF Test", 1, 2, 3),
						new BecDefinition("CWH", Region.COASTAL, "CWH Test", 2, 3, 4),
						new BecDefinition("BG", Region.INTERIOR, "BG Test", 0, 0, 1),
						new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test", 4, 5, 6)
				)
		);

		var result = lookup.getBecsForScope("X", Substitution.PARTIAL_FILL_OK);

		assertThat(result, empty());
	}

}
