package ca.bc.gov.nrs.vdyp.io.parse.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.parse.common.LineParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ControlledValueParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

/**
 * Parser for control files.
 *
 * A control file is primarily a list of files of specific types. Lines of this type have contain:
 * <ol>
 * <li>(cols 0-2) - int - Index (up to three digits)</li>
 * <li>(col 3) an optional Extension indicator (1 character) ('C', 'X' or '>' -> file name length of 120)</li>
 * <li>(cols 4-54 (124)) a file name (50 or 120 characters)</li>
 * <li>(cols 55 (125) onwards) an optional comment (unlimited)</li>
 * </ol>
 * Control files can contain other configuration values, parsed out of the characters following the extension indicator
 * (column 5 and onward).
 * <p>
 * Lines with an empty Index, or Index 0, are considered blank (or comments) and are skipped, as are lines with a 'C'
 * Extension indicator.
 * <p>
 * Each "index" value indicates the type of the file (or configuration value) on the remainder of the line.
 * <p>
 * FIP Control index: n/a
 * <p>
 * Example file: FIPSTART.CTR
 *
 * @author Kevin Smith, Vivid Solutions
 * @see ResourceParser
 */
public class ControlFileParser implements ResourceParser<Map<String, Object>> {

	public static final int INDEX_LENGTH = 3;
	public static final int EXTEND_LENGTH = 1;
	public static final int CONTROL_LENGTH_EXTENDED = 120;
	public static final int CONTROL_LENGTH = 50;

	public static final List<String> EXTEND_FLAGS = List.of("X", ">");
	public static final List<String> COMMENT_FLAGS = List.of("C");
	public static final String COMMENT_MARKER = "!";

	private final Map<Integer, String> identifiers = new HashMap<>();
	private final Map<Integer, ControlledValueParser<?>> valueParsers = new HashMap<>();
	private final Map<String, Supplier<?>> defaultValueGenerators = new LinkedHashMap<>();

	private ValueParser<?> defaultValueParser = String::strip;

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isIgnoredLine(String line) {
			return line.isBlank();
		}

		@Override
		public boolean isIgnoredSegment(List<String> segments) {
			try {
				var sequenceComment = ValueParser.optional(ValueParser.INTEGER).parse(segments.get(0))
						.map(x -> x.equals(0)).orElse(true);
				var extendComment = ValueParser.optional(ValueParser.STRING).parse(segments.get(1))
						.map(COMMENT_FLAGS::contains).orElse(false);
				return sequenceComment || extendComment;
			} catch (ValueParseException e) {
				return false; // Ignore it for now, throw the exception for real when parsing instead.
			}
		}

	}.integer(3, "index").string(1, "extend").string("restOfLine");

	/**
	 * Create a control file parser.
	 */
	public ControlFileParser() {
		this.defaultValueParser = String::strip;
	}

	@Override
	public Map<String, Object> parse(InputStream input, Map<String, Object> control)
			throws IOException, ResourceParseException {
		Map<String, Object> result = control;
		result = lineParser.parse(input, result, (map, r, line) -> {
			Integer index = (Integer) map.get("index");
			String extend = (String) map.get("extend");
			String restOfLine = (String) map.get("restOfLine");

			// How long is the control value
			int controlLength = Math
					.min(EXTEND_FLAGS.contains(extend) ? CONTROL_LENGTH_EXTENDED : CONTROL_LENGTH, restOfLine.length());

			// Trim inline comments
			int commentMarkerPosition = restOfLine.indexOf(COMMENT_MARKER);
			if (commentMarkerPosition >= 0) {
				controlLength = Math.min(controlLength, commentMarkerPosition);
			}

			// Find the string representation of the value of the control
			String controlString = restOfLine.substring(0, controlLength);

			String key = getKeyForIndex(index);
			Object value = valueParsers.getOrDefault(index, defaultValueParser).parse(controlString, control);

			r.put(key, value);

			return r;
		}, Collections.emptyMap());
		for (var e : defaultValueGenerators.entrySet()) {
			result.putIfAbsent(e.getKey(), e.getValue().get());
		}
		return result;
	}

	private String getKeyForIndex(Integer index) {
		return identifiers.getOrDefault(index, String.format("%03d", index));
	}

	/**
	 * Set a default value parser to use when one can't be found in the parser map
	 */
	public void setDefaultValueParser(ValueParser<?> defaultParser) {
		this.defaultValueParser = defaultParser;
	}

	/**
	 * Remap a record index with a meaningful name and parse its value
	 *
	 * @param index
	 * @param name
	 * @param parser
	 * @return
	 */
	public ControlFileParser record(ControlKey key, ValueParser<?> parser) {
		record(key);
		record(key.sequence.orElseThrow(), parser);
		return this;
	}

	/**
	 * Remap a record index with a meaningful name and parse its value as an optional
	 *
	 * @param index
	 * @param name
	 * @param parser
	 * @return
	 */
	public ControlFileParser optional(ControlKey key, ControlledValueParser<?> parser) {
		record(key);
		optional(key.sequence.orElseThrow(), parser);
		return this;
	}

	/**
	 * Remap a record index with a meaningful name
	 *
	 * @param index
	 * @param name
	 * @param parser
	 * @return
	 */
	public ControlFileParser record(ControlKey key) {
		this.identifiers.put(key.sequence.orElseThrow(), key.name());
		return this;
	}

	/**
	 * Parse the value of records with the given index
	 *
	 * @param index
	 * @param name
	 * @param parser
	 * @return
	 */
	public ControlFileParser record(int index, ControlledValueParser<?> parser) {
		this.valueParsers.put(index, parser);
		return this;
	}

	/**
	 * Parse the value of records with the given index as an optional
	 *
	 * @param index
	 * @param name
	 * @param parser
	 * @return
	 */
	public ControlFileParser optional(int index, ControlledValueParser<?> parser) {
		record(index, ControlledValueParser.optional(parser));
		this.defaultValueGenerators.put(getKeyForIndex(index), Optional::empty);
		return this;
	}

	public Map<String, Object> parse(InputStream is) throws IOException, ResourceParseException {
		return this.parse(is, new HashMap<>());
	}

}
