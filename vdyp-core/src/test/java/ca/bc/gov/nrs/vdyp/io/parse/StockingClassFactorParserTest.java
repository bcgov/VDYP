package ca.bc.gov.nrs.vdyp.io.parse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class StockingClassFactorParserTest {

	@Test
	public void testEmpty() throws Exception {
		var parser = new StockingClassFactorParser();
		var is = TestUtils.makeStream("");

		var result = parser.parse(is, Collections.emptyMap());

		assertThat(result, anEmptyMap());
	}

	@Test
	public void testSimple() throws Exception {
		var parser = new StockingClassFactorParser();
		var is = TestUtils.makeStream("R I P  0  1.00  100", "Z Z P  0  1.00  100");

		var result = parser.parse(is, Collections.emptyMap());

		assertThat(
				result,
				hasEntry(
						is('R'),
						hasEntry(
								is(Region.INTERIOR),
								allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))
						)
				)
		);
	}

	@Test
	public void testMultiple() throws Exception {
		var parser = new StockingClassFactorParser();
		try (var is = StockingClassFactorParserTest.class.getResourceAsStream("coe/FIPSTKR.PRM")) {

			var result = parser.parse(is, Collections.emptyMap());

			assertThat(
					result,
					hasEntry(
							is('R'),
							hasEntry(
									is(Region.INTERIOR),
									allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))
							)
					)
			);
			assertThat(
					result,
					hasEntry(
							is('R'),
							hasEntry(
									is(Region.COASTAL),
									allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))
							)
					)
			);
			assertThat(
					result,
					hasEntry(
							is('4'),
							hasEntry(
									is(Region.COASTAL),
									allOf(hasProperty("factor", is(1.0f)), allOf(hasProperty("npctArea", is(100))))
							)
					)
			);
		}
	}
}
