package ca.bc.gov.nrs.vdyp.io.parse.control;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.parseAs;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.present;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseLineException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

class ControlFileParserTest {

	@Test
	void testParsesEntriesSimple() throws Exception {
		var parser = makeParser();

		String file = "001 Control";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), contains(controlEntry(equalTo(1), equalTo(" "), equalTo("Control"))));
		}
	}

	@Test
	void testParsesEntriesSpacePadding() throws Exception {
		var parser = makeParser();

		String file = "  1 Control";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), contains(controlEntry(equalTo(1), equalTo(" "), equalTo("Control"))));
		}
	}

	@Test
	void testParsesEntriesExtended() throws Exception {
		var parser = makeParser();

		String file = "001XControl that is longer than 50 characters. Blah Blah Blah Blah.";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(
					result.entrySet(),
					contains(
							controlEntry(
									equalTo(1), equalTo("X"),
									equalTo("Control that is longer than 50 characters. Blah Blah Blah Blah.")
							)
					)
			);
		}
	}

	@Test
	void testParsesEntriesExtendedAlternate() throws Exception {
		var parser = makeParser();

		String file = "001>Control that is longer than 50 characters. Blah Blah Blah Blah.";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(
					result.entrySet(),
					contains(
							controlEntry(
									equalTo(1), equalTo(">"),
									equalTo("Control that is longer than 50 characters. Blah Blah Blah Blah.")
							)
					)
			);
		}
	}

	@Test
	void testParsesEntriesWithDistantComment() throws Exception {
		var parser = makeParser();

		String file = "001 Control                                           Comment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), contains(controlEntry(equalTo(1), equalTo(" "), equalTo("Control"))));
		}
	}

	@Test
	void testParsesEntriesExtendedWithDistantComment() throws Exception {
		var parser = makeParser();

		String file = "001XControl                                                                                                                 Comment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), contains(controlEntry(equalTo(1), equalTo("X"), equalTo("Control"))));
		}
	}

	@Test
	void testParsesEntriesWithMarkedComment() throws Exception {
		var parser = makeParser();

		String file = "001 Control!Comment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), contains(controlEntry(equalTo(1), equalTo(" "), equalTo("Control"))));
		}
	}

	@Test
	void testParsesEntriesExtendedWithMarkedComment() throws Exception {
		var parser = makeParser();

		String file = "001XControl!Comment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), contains(controlEntry(equalTo(1), equalTo("X"), equalTo("Control"))));
		}
	}

	@Test
	void testParsesEntriesIgnoreCommentLinesByExtendedMarker() throws Exception {
		var parser = makeParser();

		String file = "001CComment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), empty());
		}
	}

	@Test
	void testParsesEntriesIgnoreCommentLinesByZeroIndex() throws Exception {
		var parser = makeParser();

		String file = "000 Comment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), empty());
		}
	}

	@Test
	void testParsesEntriesIgnoreCommentLinesByNullIndex() throws Exception {
		var parser = makeParser();

		String file = "    Comment";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), empty());
		}
	}

	@Test
	void testParsesEntriesIgnoreEmptyLines() throws Exception {
		var parser = makeParser();

		String file = "\n \n  \n   \n    ";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result.entrySet(), empty());
		}
	}

	@Test
	void testParsesMultipleEntries() throws Exception {
		var parser = makeParser();

		String file = "001 Control 1\n002 Control 2";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(
					result.entrySet(),
					contains(
							controlEntry(equalTo(1), equalTo(" "), equalTo("Control 1")),
							controlEntry(equalTo(2), equalTo(" "), equalTo("Control 2"))
					)
			);
		}
	}

	@Test
	void testException() throws Exception {
		var parser = makeParser();

		String file = "00X Control 1\n002 Control 2";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			ResourceParseLineException ex1 = Assertions
					.assertThrows(ResourceParseLineException.class, () -> parser.parse(is));

			assertThat(ex1, hasProperty("line", is(1)));
			assertThat(ex1, hasProperty("cause", instanceOf(ValueParseException.class)));
			assertThat(ex1, hasProperty("cause", hasProperty("cause", instanceOf(NumberFormatException.class))));
			assertThat(ex1, hasProperty("cause", hasProperty("value", is("00X"))));
			assertThat(ex1, hasProperty("message", stringContainsInOrder("line 1", "00X")));
		}
	}

	@Test
	void testExceptionInControlValueParser() throws Exception {
		var parser = makeParser();
		parser.record(2, (s, c) -> {
			throw new ValueParseException(s, "Test Exception");
		});

		String file = "001 Control 1\n002 Control 2";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			ResourceParseLineException ex1 = Assertions
					.assertThrows(ResourceParseLineException.class, () -> parser.parse(is));

			assertThat(ex1, hasProperty("line", is(2)));
			assertThat(ex1, hasProperty("cause", instanceOf(ValueParseException.class)));
			assertThat(ex1, hasProperty("cause", hasProperty("value", is("Control 2"))));
			assertThat(ex1, hasProperty("message", stringContainsInOrder("line 2", "Test Exception")));
		}
	}

	@Test
	void testParseToMap() throws Exception {
		var parser = makeParser();
		String file = "097 coe\\vetdq2.dat                                    DQ for Vet layer           RD_YDQV\n"
				+ "098 coe\\REGBAV01.coe                                  VET BA, IPSJF168.doc       RD_E098\n" + "\n"
				+ "197    5.0   0.0   2.0                                Minimum Height, Minimum BA, Min BA fully stocked.\n"
				+ "\n"
				+ "198 coe\\MOD19813.prm                                  Modifier file (IPSJF155, XII) RD_E198\n"
				+ "199  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 Debug switches (0 by default) See IPSJF155  App IX                              Debug switches (25) 0=default See IPSJF155, App IX\n"
				+ "                                                      1st:  1: Do NOT apply BA limits from SEQ043\n"
				+ "                                                      2nd:  1: Do NOT apply DQ limits from SEQ043\n";
		try (var is = new ByteArrayInputStream(file.getBytes());) {
			var result = parser.parse(is);

			assertThat(result, hasEntry(equalTo("097"), equalTo("coe\\vetdq2.dat")));

			assertThat(result, hasEntry(equalTo("098"), equalTo("coe\\REGBAV01.coe")));
			assertThat(result, hasEntry(equalTo("197"), equalTo("5.0   0.0   2.0")));
			assertThat(result, hasEntry(equalTo("198"), equalTo("coe\\MOD19813.prm")));
			assertThat(result, hasEntry(equalTo("199"), equalTo("0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0")));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testParseToMapWithConfiguration() throws Exception {
		var parser = makeConfiguredParser();
		String file = "097 coe\\vetdq2.dat                                    DQ for Vet layer           RD_YDQV\n"
				+ "098 coe\\REGBAV01.coe                                  VET BA, IPSJF168.doc       RD_E098\n" + "\n"
				+ "197    5.0   0.0   2.0                                Minimum Height, Minimum BA, Min BA fully stocked.\n"
				+ "\n"
				+ "198 coe\\MOD19813.prm                                  Modifier file (IPSJF155, XII) RD_E198\n"
				+ "199  0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 1 0 0 Debug switches (0 by default) See IPSJF155  App IX                              Debug switches (25) 0=default See IPSJF155, App IX\n"
				+ "                                                      1st:  1: Do NOT apply BA limits from SEQ043\n"
				+ "                                                      2nd:  1: Do NOT apply DQ limits from SEQ043\n";
		try (var is = new ByteArrayInputStream(file.getBytes());) {
			var result = parser.parse(is);

			assertThat(result, hasEntry(equalTo("097"), equalTo("coe\\vetdq2.dat")));

			assertThat(result, hasEntry(equalTo("098"), equalTo("coe\\REGBAV01.coe")));
			assertThat(result, hasEntry(equalTo("MINIMA"), (Matcher) contains(5.0f, 0.0f, 2.0f)));
			assertThat(result, hasEntry(equalTo("MODIFIER_FILE"), equalTo("coe\\MOD19813.prm")));
			assertThat(
					result,
					hasEntry(
							equalTo("DEBUG_SWITCHES"),
							(Matcher) contains(
									0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0
							)
					)
			);
		}
	}

	@Test
	void testParseToMapLastOfDuplicates() throws Exception {
		var parser = makeParser();
		String file = "097 value1\n" + "097 value2\n";
		try (var is = new ByteArrayInputStream(file.getBytes());) {
			var result = parser.parse(is);

			assertThat(result, hasEntry(equalTo("097"), equalTo("value2")));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	void testParsesOptional() throws Exception {
		var parser = makeParser();

		parser.optional(ControlKey.MAX_NUM_POLY, ValueParser.STRING);

		String file = "001 Control";
		try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
			var result = parser.parse(is);

			assertThat(result, hasEntry(equalTo("MAX_NUM_POLY"), (Matcher) present(equalTo("Control"))));
		}
	}

	private static Matcher<Map.Entry<String, Object>>
			controlEntry(Matcher<Integer> index, Matcher<String> extend, Matcher<String> control) {

		return allOf(hasProperty("key", parseAs(index, Integer::valueOf)), hasProperty("value", control));
	}

	private ControlFileParser makeParser() {
		return new ControlFileParser();
	}

	private ControlFileParser makeConfiguredParser() {
		var parser = makeParser();

		parser //
				.record(ControlKey.MINIMA, ValueParser.list(ValueParser.FLOAT))//
				.record(ControlKey.MODIFIER_FILE)//
				.record(ControlKey.DEBUG_SWITCHES, ValueParser.list(ValueParser.INTEGER));

		return parser;
	}

}
