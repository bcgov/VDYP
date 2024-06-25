package ca.bc.gov.nrs.vdyp.io.parse.coe;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.mmHasEntry;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

class StockingClassFactorParserTest {

	@Test
	void testEmpty() throws Exception {
		var parser = new StockingClassFactorParser();
		var is = TestUtils.makeInputStream("");

		var result = parser.parse(is, Collections.emptyMap());

		assertThat(result, mmEmpty());
	}

	@Test
	void testSimple() throws Exception {
		var parser = new StockingClassFactorParser();
		var is = TestUtils.makeInputStream("R I P  0  1.00  100", "Z Z P  0  1.00  100");

		var result = parser.parse(is, Collections.emptyMap());

		assertThat(
				result,
				mmHasEntry(
						present(allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))), 'R',
						Region.INTERIOR
				)
		);
	}

	@Test
	void testMultiple() throws Exception {
		var parser = new StockingClassFactorParser();
		try (var is = TestUtils.class.getResourceAsStream("coe/FIPSTKR.PRM")) {

			var result = parser.parse(is, Collections.emptyMap());

			assertThat(
					result,
					mmHasEntry(
							present(allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))),
							'R', Region.INTERIOR

					)
			);
			assertThat(
					result, mmHasEntry(

							present(allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))),
							'R', Region.COASTAL
					)
			);
			assertThat(
					result, mmHasEntry(

							present(allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))),
							'4', Region.COASTAL
					)
			);
		}
	}
}
