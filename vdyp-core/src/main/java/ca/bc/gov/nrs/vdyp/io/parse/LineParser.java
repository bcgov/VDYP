package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Parse a file with records consisting of lines with fixed width fields.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class LineParser {

	private List<LineParserSegment> segments = new ArrayList<>();
	public static final Charset charset = StandardCharsets.US_ASCII;
	public static final String LINE_NUMBER_KEY = "_PARSER_LINE_NUMBER";

	static private abstract class LineParserSegment {
		int length;

		public int getLength() {
			return length;
		}

		public LineParserSegment(int length) {
			super();
			this.length = length;
		}

		public abstract void parseIntoMap(String toParse, Map<String, Object> control, Map<String, Object> map)
				throws ValueParseException;
	}

	static private class LineParserNullSegment extends LineParserSegment {

		public LineParserNullSegment(int length) {
			super(length);
		}

		@Override
		public void parseIntoMap(String toParse, Map<String, Object> control, Map<String, Object> map) {
			// do nothing
		}
	}

	static abstract class LineParserValueSegment<T> extends LineParserSegment {
		String name;

		abstract T parse(String value, Map<String, Object> control) throws ValueParseException;

		public String getName() {
			return name;
		}

		@Override
		public void parseIntoMap(String toParse, Map<String, Object> control, Map<String, Object> map)
				throws ValueParseException {
			var value = this.parse(toParse, control);
			map.put(this.getName(), value);
		}

		public LineParserValueSegment(int length, String name) {
			super(length);
			this.name = name;
		}
	}

	/**
	 * ignore a segment of characters
	 *
	 * @param length
	 * @return
	 */
	public LineParser space(int length) {
		segments.add(new LineParserNullSegment(length));
		return this;
	}

	/**
	 * A decimal integer segment
	 */
	public LineParser integer(int length, String name) {
		return this.value(length, name, ValueParser.INTEGER);
	}

	/**
	 * A floating point segment
	 */
	public LineParser floating(int length, String name) {
		return this.value(length, name, ValueParser.FLOAT);
	}

	/**
	 * A string segment
	 */
	public LineParser string(int length, String name) {
		return this.value(length, name, ValueParser.STRING_UNSTRIPPED);
	}

	/**
	 * A string segment with no bounds. No further segments may be added.
	 */
	public LineParser string(String name) {
		return this.value(name, ValueParser.STRING_UNSTRIPPED);
	}

	/**
	 * A string segment stripped of leading and trailing whitespace.
	 */
	public LineParser strippedString(int length, String name) {
		return this.value(length, name, ValueParser.STRING);
	}

	/**
	 * A string segment stripped of leading and trailing whitespace with no bounds.
	 * No further segments may be added.
	 */
	public LineParser strippedString(String name) {
		return this.value(name, ValueParser.STRING);
	}

	/**
	 * Add a multipart segment
	 *
	 * @param <T>    type the segment will be parsed too
	 * @param length length of the segment
	 * @param name   name of the segment
	 * @param parser parser to convert the string
	 */
	public <T> LineParser multiValue(int number, int length, String name, ValueParser<T> parser) {
		if (number < 0)
			throw new IllegalArgumentException("number can not be negative");
		if (length < 0)
			throw new IllegalArgumentException("length can not be negative");

		return doValue(length * number, name, ValueParser.segmentList(length, parser));
	}

	/**
	 * Add a segment
	 *
	 * @param <T>    type the segment will be parsed too
	 * @param length length of the segment
	 * @param name   name of the segment
	 * @param parser parser to convert the string
	 */
	public <T> LineParser value(int length, String name, ControlledValueParser<T> parser) {
		if (length < 0)
			throw new IllegalArgumentException("length can not be negative");
		return doValue(length, name, parser);
	}

	/**
	 * Add an unbounded segment. No further segments may be added.
	 *
	 * @param <T>    type the segment will be parsed too
	 * @param length length of the segment
	 * @param name   name of the segment
	 * @param parser parser to convert the string
	 */
	public <T> LineParser value(String name, ControlledValueParser<T> parser) {
		return doValue(-1, name, parser);
	}

	private <T> LineParser doValue(int length, String name, ControlledValueParser<T> parser) {
		if (segments.size() > 0 && segments.get(segments.size() - 1).length < 0)
			throw new IllegalStateException("Can not add a segment after an unbounded segment");
		segments.add(new LineParserValueSegment<T>(length, name) {

			@Override
			T parse(String value, Map<String, Object> control) throws ValueParseException {
				return parser.parse(value, control);
			}

		});
		return this;
	}

	private List<String> segmentize(String line) {
		List<String> result = new ArrayList<>(segments.size());

		int i = 0;
		for (var segment : segments) {
			if (i >= line.length()) {
				result.add(null);
				continue;
			}
			;
			String segmentString;
			if (segment.getLength() >= 0 && i + segment.getLength() < line.length()) {
				segmentString = line.substring(i, i + segment.length);
			} else {
				segmentString = line.substring(i);
			}
			i += segmentString.length();
			result.add(segmentString);
		}

		return result;
	}

	/**
	 * Parse an individual line
	 *
	 * @param line
	 * @return
	 * @throws ValueParseException
	 */
	public Map<String, Object> parseLine(String line, Map<String, Object> control) throws ValueParseException {
		var segments = segmentize(line);
		return parse(segments, control);
	}

	private Map<String, Object> parse(List<String> segmentStrings, Map<String, Object> control)
			throws ValueParseException {
		if (segmentStrings.size() != segments.size()) {
			throw new IllegalStateException("segment strings and segment handlers must have the same size");
		}

		var result = new HashMap<String, Object>();

		for (int i = 0; i < segments.size(); i++) {
			var segmentHandler = segments.get(i);
			var segmentString = segmentStrings.get(i);
			if (segmentString != null) {
				segmentHandler.parseIntoMap(segmentString, control, result);
			}
		}

		return result;
	}

	/**
	 * Parse an input stream
	 *
	 * @param <T>         Type of the resulting object
	 * @param is          Input stream to parse
	 * @param result      Starting state for the resulting object
	 * @param addToResult Add a record from the file to the result object and return
	 *                    it
	 * @return The result object after parsing
	 * @throws IOException                if an error occurs while reading from the
	 *                                    stream
	 * @throws ResourceParseLineException if the content of the stream could not be
	 *                                    parsed
	 */
	public <T> T parse(
			InputStream is, T result, ParseEntryHandler<Map<String, Object>, T> addToResult, Map<String, Object> control
	) throws IOException, ResourceParseLineException {
		var stream = parseAsStream(is, control); // Assume the caller will close the stream so we don't need to close
													// this.
		while (stream.hasNext()) {
			var entry = stream.next();
			try {
				result = addToResult.addTo(entry, result);
			} catch (ValueParseException ex) {
				stream.handleValueParseException(ex);
			}
		}
		return result;
	}

	/**
	 * Returns a LineStream of parsed entry maps. Closing it will close the provided
	 * stream.
	 *
	 * @param is
	 * @param control
	 * @return
	 */
	LineStream parseAsStream(InputStream is, Map<String, Object> control) {
		var reader = new BufferedReader(new InputStreamReader(is, charset));

		var stream = new LineStream(reader, control);
		return stream;
	}

	public class LineStream implements AutoCloseable {

		int lineNumber = 0;
		BufferedReader reader;
		Map<String, Object> control;

		Optional<Optional<String>> nextLine = Optional.empty();

		public LineStream(BufferedReader reader, Map<String, Object> control) {
			this.reader = reader;
			this.control = control;
		}

		public Map<String, Object> next() throws IOException, ResourceParseLineException {
			if (nextLine.isEmpty()) {
				nextLine = Optional.of(doGetNextLine());
			}
			try {
				var line = nextLine.get()
						.orElseThrow(() -> new IllegalStateException("Tried to get next entry when none exists"));
				var segments = segmentize(line);

				var entry = parse(segments, control);

				entry.put(LINE_NUMBER_KEY, lineNumber);

				return entry;
			} catch (ValueParseException ex) {
				handleValueParseException(ex);
				return null;
			} finally {
				nextLine = Optional.empty();
			}
		}

		public boolean hasNext() throws IOException, ResourceParseLineException {
			if (nextLine.isEmpty()) {
				nextLine = Optional.of(doGetNextLine());
			}
			return nextLine.get().isPresent();
		}

		private Optional<String> doGetNextLine() throws IOException, ResourceParseLineException {
			while (true) {
				lineNumber++;
				var line = reader.readLine();
				if (Objects.isNull(line)) {
					return Optional.empty();
				}
				if (isStopLine(line)) {
					return Optional.empty();
				}
				if (isIgnoredLine(line)) {
					continue;
				}
				var segments = segmentize(line);
				if (isStopSegment(segments)) {
					return Optional.empty();
				}
				if (isIgnoredSegment(segments)) {
					continue;
				}

				return Optional.of(line);

			}
		}

		void handleValueParseException(ValueParseException ex) throws ResourceParseLineException {
			throw new ResourceParseLineException(lineNumber, ex);
		}

		@Override
		public void close() throws IOException {
			this.reader.close();
		}
	}

	/**
	 * Parse an input stream into a list of maps
	 *
	 * @param is Input stream to parse
	 * @return A list of maps, one per line of the stream
	 * @throws IOException                if an error occurs while reading from the
	 *                                    stream
	 * @throws ResourceParseLineException if the content of the stream could not be
	 *                                    parsed
	 */
	public List<Map<String, Object>> parse(InputStream is, Map<String, Object> control)
			throws IOException, ResourceParseLineException {
		var result = new ArrayList<Map<String, Object>>();
		result = this.parse(is, result, (v, r) -> {
			r.add(v);
			return r;
		}, control);
		return result;
	}

	/**
	 * If this returns true for a segmented line, parsing will stop and that line
	 * will not be included in the result.
	 */
	public boolean isStopSegment(List<String> entry) {
		return false;
	}

	/**
	 * If this returns true for an unparsed line, parsing will stop and that line
	 * will not be included in the result.
	 */
	public boolean isStopLine(String line) {
		return false;
	}

	/**
	 * If this returns true for a segmented line, that line will not be included in
	 * the result.
	 */
	public boolean isIgnoredSegment(List<String> entry) {
		return false;
	}

	/**
	 * If this returns true for an unparsed line, that line will not be included in
	 * the result.
	 */
	public boolean isIgnoredLine(String line) {
		return false;
	}
}
