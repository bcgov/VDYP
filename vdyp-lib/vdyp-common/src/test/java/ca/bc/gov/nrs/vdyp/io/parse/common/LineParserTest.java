package ca.bc.gov.nrs.vdyp.io.parse.common;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

class LineParserTest {

	@Test
	void testBasic() throws Exception {
		var parser = new LineParser();
		parser.string(3, "part1").space(1).string(4, "part2");

		var result1 = parser.parseLine("042 Blah", Collections.emptyMap());

		assertThat(result1, hasEntry("part1", "042"));
		assertThat(result1, hasEntry("part2", "Blah"));

	}

	@Test
	void testNumbers() throws Exception {
		var parser = new LineParser();
		parser.integer(4, "part1").space(1).floating(5, "part2");

		var result1 = parser.parseLine(" 4   0.5  ", Collections.emptyMap());

		assertThat(result1, hasEntry("part1", 4));
		assertThat(result1, hasEntry("part2", 0.5f));

	}

	@Test
	void testIncomplete() throws Exception {
		var parser = new LineParser();
		parser.integer(4, "part1").space(1).floating(5, "part2");

		var result1 = parser.parseLine(" 4  ", Collections.emptyMap());

		assertThat(result1, hasEntry("part1", 4));
		assertThat(result1, not(hasKey("part2")));

	}

	@Test
	void testIncompleteSegment() throws Exception {
		var parser = new LineParser();
		parser.integer(4, "part1").space(1).floating(5, "part2");

		var result1 = parser.parseLine(" 4   5.0", Collections.emptyMap());

		assertThat(result1, hasEntry("part1", 4));
		assertThat(result1, hasEntry("part2", 5.0f));

	}

	@Test
	void testNumberParseErrors() throws Exception {
		var parser = new LineParser();
		parser.integer(4, "part1").space(1).floating(5, "part2");

		var ex1 = assertThrows(ValueParseException.class, () -> parser.parseLine(" X   0.5  ", Collections.emptyMap()));

		assertThat(ex1, hasProperty("value", is("X")));
		assertThat(ex1, hasProperty("cause", isA(NumberFormatException.class)));

		var ex2 = assertThrows(ValueParseException.class, () -> parser.parseLine(" 4   0.x  ", Collections.emptyMap()));

		assertThat(ex2, hasProperty("value", is("0.x")));
		assertThat(ex2, hasProperty("cause", isA(NumberFormatException.class)));

	}

	@Test
	void testValueParser() throws Exception {
		var parser = new LineParser();
		parser.value(4, "part1", (s, c) -> Integer.valueOf(s.strip()) + 1).space(1)
				.value("part2", (s, c) -> Float.valueOf(s.strip()) + 1);

		var result1 = parser.parseLine(" 4   0.5  ", Collections.emptyMap());

		assertThat(result1, hasEntry("part1", 5));
		assertThat(result1, hasEntry("part2", 1.5f));

	}

	@Test
	void testValueParserError() throws Exception {
		var parser = new LineParser();
		parser.value(4, "part1", (s, c) -> {
			throw new ValueParseException(s, "Testing");
		}).space(1).value(4, "part2", (s, c) -> Float.valueOf(s.strip()) + 1);

		var ex1 = assertThrows(ValueParseException.class, () -> parser.parseLine(" X   0.5  ", Collections.emptyMap()));
		assertThat(ex1, hasProperty("value", is(" X  ")));
		assertThat(ex1, hasProperty("message", is("Testing")));

	}

	@Test
	void testUnbounded() throws Exception {
		var parser = new LineParser();
		parser.string(4, "part1").string("part2");

		var result1 = parser.parseLine(
				"123  67890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 ",
				Collections.emptyMap()
		);

		assertThat(result1, hasEntry("part1", "123 "));
		assertThat(
				result1,
				hasEntry(
						"part2",
						" 67890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 "
				)
		);

	}

	@Test
	void testStripped() throws Exception {
		var parser = new LineParser();
		parser.strippedString(4, "part1").strippedString("part2");

		var result1 = parser.parseLine(
				"123  67890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 ",
				Collections.emptyMap()
		);

		assertThat(result1, hasEntry("part1", "123"));
		assertThat(
				result1,
				hasEntry(
						"part2",
						"67890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"
				)
		);

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testMultiValue() throws Exception {
		var parser = new LineParser();
		parser.multiValue(4, 3, "test", ValueParser.INTEGER);

		var result1 = parser.parseLine(" 02 04 06 08", Collections.emptyMap());

		assertThat(result1, hasEntry(is("test"), (Matcher) contains(2, 4, 6, 8)));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testMultiLine() throws Exception {
		var parser = new LineParser();
		parser.integer(4, "part1").space(1).string("part2");

		List<Map<String, Object>> result = new ArrayList<>();
		try (var is = new ByteArrayInputStream("0042 Value1\r\n0043 Value2".getBytes());) {
			result = parser.parse(is, Collections.emptyMap());
		}

		assertThat(
				result,
				contains(
						allOf(
								(Matcher) hasEntry("part1", 42), (Matcher) hasEntry("part2", "Value1"),
								(Matcher) hasEntry(LineParser.LINE_NUMBER_KEY, 1)
						),
						allOf(
								(Matcher) hasEntry("part1", 43), (Matcher) hasEntry("part2", "Value2"),
								(Matcher) hasEntry(LineParser.LINE_NUMBER_KEY, 2)
						)
				)
		);
	}

	@Test
	void testMultiLineException() throws Exception {
		var parser = new LineParser();
		parser.integer(4, "part1").space(1).string("part2");

		try (var is = new ByteArrayInputStream("0042 Value1\r\n004x Value2".getBytes());) {

			var ex1 = assertThrows(ResourceParseLineException.class, () -> parser.parse(is, Collections.emptyMap()));

			assertThat(ex1, hasProperty("line", is(2))); // Line numbers indexed from 1 so the error is line 2
			assertThat(ex1, hasProperty("cause", isA(ValueParseException.class)));
			assertThat(ex1, hasProperty("cause", hasProperty("value", is("004x"))));
			assertThat(ex1, hasProperty("cause", hasProperty("cause", isA(NumberFormatException.class))));

		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testMultiLineWithStopLine() throws Exception {
		var parser = new LineParser() {

			@Override
			public boolean isStopLine(String line) {
				return line.length() > 4 && 'X' == Character.toUpperCase(line.charAt(4));
			}

		};
		parser.integer(4, "part1").space(1).string("part2");

		List<Map<String, Object>> result = new ArrayList<>();
		try (var is = new ByteArrayInputStream("0042 Value1\r\n0000X\r\n0043 Value2".getBytes());) {
			result = parser.parse(is, Collections.emptyMap());
		}

		assertThat(result, contains(allOf((Matcher) hasEntry("part1", 42), (Matcher) hasEntry("part2", "Value1"))));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testMultiLineWithStopSegment() throws Exception {
		var parser = new LineParser() {

			@Override
			public boolean isStopSegment(List<String> segments) {
				return 'X' == Character.toUpperCase(segments.get(1).charAt(0));
			}

		};
		parser.integer(4, "part1").space(1).string("part2");

		List<Map<String, Object>> result = new ArrayList<>();
		try (var is = new ByteArrayInputStream("0042 Value1\r\n0000X\r\n0043 Value2".getBytes());) {
			result = parser.parse(is, Collections.emptyMap());
		}

		assertThat(result, contains(allOf((Matcher) hasEntry("part1", 42), (Matcher) hasEntry("part2", "Value1"))));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testMultiLineWithIgnoredLine() throws Exception {
		var parser = new LineParser() {

			@Override
			public boolean isIgnoredLine(String line) {
				return line.length() > 4 && 'X' == Character.toUpperCase(line.charAt(4));
			}

		};
		parser.integer(4, "part1").space(1).string("part2");

		List<Map<String, Object>> result = new ArrayList<>();
		try (var is = new ByteArrayInputStream("0042 Value1\r\n0000X\r\n0043 Value2".getBytes());) {
			result = parser.parse(is, Collections.emptyMap());
		}

		assertThat(
				result,
				contains(
						allOf((Matcher) hasEntry("part1", 42), (Matcher) hasEntry("part2", "Value1")),
						allOf((Matcher) hasEntry("part1", 43), (Matcher) hasEntry("part2", "Value2"))
				)
		);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testMultiLineWithIgnoredSegment() throws Exception {
		var parser = new LineParser() {

			@Override
			public boolean isIgnoredSegment(List<String> segments) {
				return 'X' == Character.toUpperCase(segments.get(1).charAt(0));
			}

		};
		parser.integer(4, "part1").space(1).string("part2");

		List<Map<String, Object>> result = new ArrayList<>();
		try (var is = new ByteArrayInputStream("0042 Value1\r\n0000X\r\n0043 Value2".getBytes());) {
			result = parser.parse(is, Collections.emptyMap());
		}

		assertThat(
				result,
				contains(
						allOf((Matcher) hasEntry("part1", 42), (Matcher) hasEntry("part2", "Value1")),
						allOf((Matcher) hasEntry("part1", 43), (Matcher) hasEntry("part2", "Value2"))
				)
		);
	}

}
