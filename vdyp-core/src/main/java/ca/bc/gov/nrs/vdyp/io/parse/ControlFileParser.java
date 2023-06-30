package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Parser for control files
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class ControlFileParser implements ResourceParser<Map<String, Object>> {

	public static final int INDEX_LENGTH = 3;
	public static final int EXTEND_LENGTH = 1;
	public static final int CONTROL_LENGTH_EXTENDED = 120;
	public static final int CONTROL_LENGTH = 50;

	public static final List<String> EXTEND_FLAGS = Arrays.asList("X", ">");
	public static final List<String> COMMENT_FLAGS = Arrays.asList("C");
	public static final String COMMENT_MARKER = "!";

	private Map<Integer, String> identifiers;
	private Map<Integer, ControlledValueParser<?>> valueParsers;
	private ValueParser<?> defaultValueParser;
	private Map<String, Supplier<?>> defaultValueGenerators;

	LineParser lineParser = new LineParser() {

		@Override
		public boolean isIgnoredLine(String line) {
			return line.isBlank();
		}

		@Override
		public boolean isIgnoredSegment(List<String> segments) {
			return segments.get(0).isBlank();
		}

		@Override
		public boolean isIgnoredEntry(Map<String, Object> segments) {
			return 0 == (Integer) segments.get("index") || COMMENT_FLAGS.contains(segments.get("extend"));
		}

	}.integer(3, "index").string(1, "extend").string("restOfLine");

	/**
	 * Create a control file parser.
	 */
	public ControlFileParser() {
		this.identifiers = new HashMap<>();
		this.valueParsers = new HashMap<>();
		this.defaultValueParser = String::strip;
		this.defaultValueGenerators = new LinkedHashMap<>();
	}

	@Override
	public Map<String, Object> parse(InputStream input, Map<String, Object> control)
			throws IOException, ResourceParseException {
		Map<String, Object> result = new HashMap<>();
		result = lineParser.parse(input, result, (map, r) -> {
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
	public ControlFileParser record(int index, String name, ValueParser<?> parser) {
		record(index, name);
		record(index, parser);
		return this;
	}

	/**
	 * Remap a record index with a meaningful name and parse its value as an
	 * optional
	 *
	 * @param index
	 * @param name
	 * @param parser
	 * @return
	 */
	public ControlFileParser optional(int index, String name, ControlledValueParser<?> parser) {
		record(index, name);
		optional(index, parser);
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
	public ControlFileParser record(int index, String name) {
		this.identifiers.put(index, name);
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
		return this.parse(is, Collections.emptyMap());
	}

}
