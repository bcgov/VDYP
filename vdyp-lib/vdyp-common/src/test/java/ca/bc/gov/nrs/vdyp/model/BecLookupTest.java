package ca.bc.gov.nrs.vdyp.model;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class BecLookupTest {

	@Test
	void testSimpleGet() throws Exception {
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(essf));
		var result = lookup.get("ESSF");

		assertThat(result, present(hasProperty("alias", is("ESSF"))));
	}

	@Test
	void testGetMissing() throws Exception {
		List<BecDefinition> essf = Arrays.asList(new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test"));
		var lookup = new BecLookup(essf);
		var result = lookup.get("XX");

		assertThat(result, notPresent());
	}

	@Test
	void testGetBecs() throws Exception {
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(bg, essf));

		var result = lookup.getBecs();

		assertThat(
				result,
				containsInAnyOrder(allOf(hasProperty("alias", is("BG"))), allOf(hasProperty("alias", is("ESSF"))))
		);
	}

	@Test
	void testGetGrowthBecs() throws Exception {
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		BecDefinition at = new BecDefinition(
				"AT", Region.INTERIOR, "AT Test", Optional.of(essf), Optional.empty(), Optional.empty()
		);
		BecDefinition bg = new BecDefinition(
				"BG", Region.INTERIOR, "BG Test", Optional.of(essf), Optional.of(essf), Optional.empty()
		);
		var lookup = new BecLookup(Arrays.asList(at, bg, essf));

		var result = lookup.getGrowthBecs();

		assertThat(result, containsInAnyOrder(allOf(hasProperty("alias", is("ESSF")))));
	}

	@Test
	void testGetCoastalBecs() throws Exception {
		BecDefinition cdf = new BecDefinition("CDF", Region.COASTAL, "CDF Test");
		BecDefinition cwh = new BecDefinition("CWH", Region.COASTAL, "CWH Test");
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(cdf, cwh, bg, essf));

		var result = lookup.getBecsForRegion(Region.COASTAL);

		assertThat(
				result,
				containsInAnyOrder(allOf(hasProperty("alias", is("CDF"))), allOf(hasProperty("alias", is("CWH"))))
		);
	}

	@Test
	void testGetInteriorBecs() throws Exception {
		BecDefinition cdf = new BecDefinition("CDF", Region.COASTAL, "CDF Test");
		BecDefinition cwh = new BecDefinition("CWH", Region.COASTAL, "CWH Test");
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(cdf, cwh, bg, essf));

		var result = lookup.getBecsForRegion(Region.INTERIOR);

		assertThat(
				result,
				containsInAnyOrder(allOf(hasProperty("alias", is("BG"))), allOf(hasProperty("alias", is("ESSF"))))
		);
	}

	@Test
	void testGetByBlankScope() throws Exception {
		BecDefinition cdf = new BecDefinition("CDF", Region.COASTAL, "CDF Test");
		BecDefinition cwh = new BecDefinition("CWH", Region.COASTAL, "CWH Test");
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(cdf, cwh, bg, essf));

		var result = lookup.getBecsForScope("");

		assertThat(
				result,
				containsInAnyOrder(
						allOf(hasProperty("alias", is("CDF"))), allOf(hasProperty("alias", is("CWH"))),
						allOf(hasProperty("alias", is("BG"))), allOf(hasProperty("alias", is("ESSF")))
				)
		);
	}

	@Test
	void testGetByRegionScope() throws Exception {
		BecDefinition cdf = new BecDefinition("CDF", Region.COASTAL, "CDF Test");
		BecDefinition cwh = new BecDefinition("CWH", Region.COASTAL, "CWH Test");
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(cdf, cwh, bg, essf));

		var result = lookup.getBecsForScope("I");

		assertThat(
				result,
				containsInAnyOrder(allOf(hasProperty("alias", is("BG"))), allOf(hasProperty("alias", is("ESSF"))))
		);
	}

	@Test
	void testGetByBecScope() throws Exception {
		BecDefinition cdf = new BecDefinition("CDF", Region.COASTAL, "CDF Test");
		BecDefinition cwh = new BecDefinition("CWH", Region.COASTAL, "CWH Test");
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(cdf, cwh, bg, essf));

		var result = lookup.getBecsForScope("CDF");

		assertThat(result, containsInAnyOrder(allOf(hasProperty("alias", is("CDF")))));
	}

	@Test
	void testGetByMissingScope() throws Exception {
		BecDefinition cdf = new BecDefinition("CDF", Region.COASTAL, "CDF Test");
		BecDefinition cwh = new BecDefinition("CWH", Region.COASTAL, "CWH Test");
		BecDefinition bg = new BecDefinition("BG", Region.INTERIOR, "BG Test");
		BecDefinition essf = new BecDefinition("ESSF", Region.INTERIOR, "ESSF Test");
		var lookup = new BecLookup(Arrays.asList(cdf, cwh, bg, essf));

		var result = lookup.getBecsForScope("X");

		assertThat(result, empty());
	}

}
