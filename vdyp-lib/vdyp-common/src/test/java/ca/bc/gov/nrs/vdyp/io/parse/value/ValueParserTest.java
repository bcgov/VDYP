package ca.bc.gov.nrs.vdyp.io.parse.value;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.closeTo;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.notPresent;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.model.LayerType;

class ValueParserTest {

	@Nested
	class RangeParser {
		@Test
		void testRangeParser() throws Exception {
			var exclusiveParser = ValueParser.range(ValueParser.INTEGER, 10, false, 20, false, "Test");

			var result = exclusiveParser.parse("11");
			assertThat(result, is(11));

			result = exclusiveParser.parse("19");
			assertThat(result, is(19));

			var ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("10"));
			assertThat(ex, hasProperty("message", is("Test must be greater than 10.")));

			ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("9"));
			assertThat(ex, hasProperty("message", is("Test must be greater than 10.")));

			ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("20"));
			assertThat(ex, hasProperty("message", is("Test must be less than 20.")));

			ex = assertThrows(ValueParseException.class, () -> exclusiveParser.parse("21"));
			assertThat(ex, hasProperty("message", is("Test must be less than 20.")));

			var inclusiveParser = ValueParser.range(ValueParser.INTEGER, 10, true, 20, true, "Test");

			result = inclusiveParser.parse("11");
			assertThat(result, is(11));

			result = inclusiveParser.parse("19");
			assertThat(result, is(19));

			result = inclusiveParser.parse("10");
			assertThat(result, is(10));

			result = inclusiveParser.parse("20");
			assertThat(result, is(20));

			ex = assertThrows(ValueParseException.class, () -> inclusiveParser.parse("9"));
			assertThat(ex, hasProperty("message", is("Test must be greater than or equal to 10.")));

			ex = assertThrows(ValueParseException.class, () -> inclusiveParser.parse("21"));
			assertThat(ex, hasProperty("message", is("Test must be less than or equal to 20.")));

		}

		@Test
		void testRangeSilentLow() throws Exception {

			var parser = ValueParser.rangeSilentLow(ValueParser.FLOAT, 1f, true, 4f, false, "test");

			assertThat(parser.parse("0"), notPresent());
			assertThat(parser.parse("0.99999"), notPresent());
			assertThat(parser.parse("1"), present(closeTo(1f)));
			assertThat(parser.parse("3.99999"), present(closeTo(3.99999f)));

			assertThat(
					assertThrows(ValueParseException.class, () -> parser.parse("4")),
					hasProperty("message", is("test must be less than 4.0."))
			);
			assertThat(
					assertThrows(ValueParseException.class, () -> parser.parse("5")),
					hasProperty("message", is("test must be less than 4.0."))
			);

			var parser2 = ValueParser.rangeSilentLow(ValueParser.FLOAT, 1f, false, 4f, true, "test");

			assertThat(parser2.parse("0"), notPresent());
			assertThat(parser2.parse("0.99999"), notPresent());
			assertThat(parser2.parse("1"), notPresent());
			assertThat(parser2.parse("3.99999"), present(closeTo(3.99999f)));
			assertThat(parser2.parse("4"), present(closeTo(4f)));

			assertThat(
					assertThrows(ValueParseException.class, () -> parser2.parse("4.0001")),
					hasProperty("message", is("test must be less than or equal to 4.0."))
			);
			assertThat(
					assertThrows(ValueParseException.class, () -> parser2.parse("5")),
					hasProperty("message", is("test must be less than or equal to 4.0."))
			);
		}

		@Test
		void testValidateRangeInclusive() throws Exception {
			var unit = ValueParser.validateRangeInclusive(10f, 20f, "Test");

			assertThat(unit.apply(10f), notPresent());
			assertThat(unit.apply(20f), notPresent());
			assertThat(unit.apply(11f), notPresent());
			assertThat(unit.apply(19f), notPresent());
			assertThat(unit.apply(9f), present(is("Test is expected to be between 10.0 and 20.0 but was 9.0")));
			assertThat(unit.apply(21f), present(is("Test is expected to be between 10.0 and 20.0 but was 21.0")));

		}

		@Test
		void testValidateRangeInclusiveBadRange() throws Exception {
			assertThrows(IllegalArgumentException.class, () -> ValueParser.validateRangeInclusive(20f, 10f, "Test"));
		}

	}

	static enum TestEnum {
		VALUE1, VALUE2
	}

	@Test
	void testEnumParser() throws Exception {
		var parser = ValueParser.enumParser(TestEnum.class);

		assertThat(parser.parse("VALUE1"), is(TestEnum.VALUE1));
		assertThat(parser.parse("VALUE2"), is(TestEnum.VALUE2));

		var ex = assertThrows(ValueParseException.class, () -> parser.parse("FAKE"));
		assertThat(ex.getMessage(), is("\"FAKE\" is not a valid TestEnum"));

		ex = assertThrows(ValueParseException.class, () -> parser.parse(""));
		assertThat(ex.getMessage(), is("\"\" is not a valid TestEnum"));

		ex = assertThrows(ValueParseException.class, () -> parser.parse(" "));
		assertThat(ex.getMessage(), is("\"\" is not a valid TestEnum"));
	}

	@Nested
	class TestOptional {

		@Test
		void testPretestOptional() throws Exception {
			var unit = ValueParser.pretestOptional(ValueParser.INTEGER, x -> !x.equals("IGNORE"));

			assertThat(unit.parse("1"), present(is(1)));
			assertThat(unit.parse("2"), present(is(2)));
			assertThat(unit.parse("IGNORE"), notPresent());
			assertThrows(ValueParseException.class, () -> unit.parse("DONT_IGNORE"));
		}

		@Test
		void testPosttestOptional() throws Exception {
			var unit = ValueParser.posttestOptional(ValueParser.INTEGER, x -> !x.equals(1));

			assertThat(unit.parse("0"), present(is(0)));
			assertThat(unit.parse("1"), notPresent());
			assertThat(unit.parse("2"), present(is(2)));
			assertThrows(ValueParseException.class, () -> unit.parse("DONT_IGNORE"));
		}

		@Test
		void testOptionalSingleton() throws Exception {
			var unit = ValueParser.optionalSingleton(x -> x.equals("PASS"), "X");

			assertThat(unit.parse("FAIL"), notPresent());
			assertThat(unit.parse("PASS"), present(is("X")));
		}

	}

	@Test
	void testValueOrMarkerParser() throws Exception {
		var parser = ValueParser.valueOrMarker(ValueParser.INTEGER, (s) -> {
			if ("MARK".equals(s)) {
				return Optional.of("MARK");
			}
			return Optional.empty();
		});

		assertThat(parser.parse("MARK").getMarker(), present(is("MARK")));
		assertThat(parser.parse("MARK").getValue(), notPresent());

		assertThat(parser.parse("1").getValue(), present(is(1)));
		assertThat(parser.parse("1").getMarker(), notPresent());

		var ex = assertThrows(ValueParseException.class, () -> parser.parse("X"));
		assertThat(ex.getMessage(), is("\"X\" is not a valid Integer"));
	}

	@Test
	void testLayer() throws Exception {
		var unit = ValueParser.LAYER;

		assertThat(unit.parse("1"), present(is(LayerType.PRIMARY)));
		assertThat(unit.parse("P"), present(is(LayerType.PRIMARY)));

		assertThat(unit.parse("2"), present(is(LayerType.SECONDARY)));
		assertThat(unit.parse("S"), present(is(LayerType.SECONDARY)));

		assertThat(unit.parse("V"), present(is(LayerType.VETERAN)));

		assertThat(unit.parse(""), notPresent());
		assertThat(unit.parse(" "), notPresent());
		assertThat(unit.parse("X"), notPresent());
	}

	@Nested
	class MapParser {

		@Test
		void testSimple() throws Exception {
			var parser = ValueParser.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), "A", "B", "C");

			var result = parser.parse("  1  2  3");

			assertThat(result, aMapWithSize(3));
			assertThat(result, hasEntry(is("A"), is(1)));
			assertThat(result, hasEntry(is("B"), is(2)));
			assertThat(result, hasEntry(is("C"), is(3)));

		}

		@Test
		void testMissingAValueError() throws Exception {
			var parser = ValueParser.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), "A", "B", "C");

			var ex = assertThrows(ValueParseException.class, () -> parser.parse("  1  2"));
			assertThat(ex, hasProperty("message", is("Expected exactly 3 values but there were 2")));
		}

		@Test
		void testExtraAValueError() throws Exception {
			var parser = ValueParser.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), "A", "B", "C");

			var ex = assertThrows(ValueParseException.class, () -> parser.parse("  1  2  3  4"));
			assertThat(ex, hasProperty("message", is("Expected exactly 3 values but there were 4")));
		}

		@Test
		void testWithDefaults() throws Exception {
			Map<String, Integer> defaults = new HashMap<>();
			defaults.put("C", 5);
			defaults.put("D", 6);

			var parser = ValueParser
					.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), defaults, "A", "B", "C", "D");

			var result = parser.parse("  1  2  3");

			assertThat(result, aMapWithSize(4));
			assertThat(result, hasEntry(is("A"), is(1)));
			assertThat(result, hasEntry(is("B"), is(2)));
			assertThat(result, hasEntry(is("C"), is(3)));// 3 not 5
			assertThat(result, hasEntry(is("D"), is(6)));
		}

		@Test
		void testWithDefaultsToFew() throws Exception {
			Map<String, Integer> defaults = new HashMap<>();
			defaults.put("C", 5);
			defaults.put("D", 6);

			var parser = ValueParser
					.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), defaults, "A", "B", "C", "D");

			var ex = assertThrows(ValueParseException.class, () -> parser.parse("  1"));
			assertThat(ex, hasProperty("message", is("Expected between 2 and 4 values but there were 1")));
		}

		@Test
		void tesWithDefaultsToMany() throws Exception {
			Map<String, Integer> defaults = new HashMap<>();
			defaults.put("C", 5);
			defaults.put("D", 6);

			var parser = ValueParser
					.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), defaults, "A", "B", "C", "D");

			var ex = assertThrows(ValueParseException.class, () -> parser.parse("  1  2  3  4  5"));
			assertThat(ex, hasProperty("message", is("Expected between 2 and 4 values but there were 5")));
		}

		@Test
		void testDefaultsMustComeAfterRequired() throws Exception {
			Map<String, Integer> defaults = new HashMap<>();
			defaults.put("C", 5);
			defaults.put("B", 6);

			var ex = assertThrows(
					IllegalArgumentException.class,
					() -> ValueParser
							.toMap(ValueParser.segmentList(3, ValueParser.INTEGER), defaults, "A", "B", "C", "D")
			);
			assertThat(ex, hasProperty("message", is("Keys with defaults must follow those without")));
		}
	}

}
