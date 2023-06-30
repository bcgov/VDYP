package ca.bc.gov.nrs.vdyp.io.parse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import ca.bc.gov.nrs.vdyp.model.Region;

/**
 * Parses a string to a value
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
@FunctionalInterface
public interface ValueParser<T> extends ControlledValueParser<T> {
	/**
	 * Parse a string to a value
	 *
	 * @param string
	 * @return The parsed value
	 * @throws ValueParseException if the string could not be parsed
	 */
	T parse(String string) throws ValueParseException;

	@Override
	default T parse(String string, Map<String, Object> control) throws ValueParseException {
		// Ignore the control map
		return this.parse(string);
	}

	public static interface JavaNumberParser<U extends Number> {
		U parse(String s) throws NumberFormatException;
	}

	/**
	 * Adapts a parse method that throws NumerFormatException
	 *
	 * @param parser method that parses a string into the required type and throws
	 *               NumerFormatException
	 * @param klazz  the type to parse into
	 */
	public static <U extends Number> ValueParser<U> numberParser(JavaNumberParser<U> parser, Class<U> klazz) {
		return s -> {
			String stripped = s.strip();
			try {
				return parser.parse(stripped);
			} catch (NumberFormatException ex) {
				throw new ValueParseException(
						stripped, String.format("\"%s\" is not a valid %s", stripped, klazz.getSimpleName()), ex
				);
			}
		};
	}

	/**
	 * Adapts an parsing an Enum
	 *
	 * @param klazz the type to parse into
	 */
	public static <U extends Enum<U>> ValueParser<U> enumParser(Class<U> klazz) {
		return s -> {
			String stripped = s.strip();
			try {
				return Enum.valueOf(klazz, s);
			} catch (IllegalArgumentException ex) {
				throw new ValueParseException(
						stripped, String.format("\"%s\" is not a valid %s", stripped, klazz.getSimpleName()), ex
				);
			}
		};
	}

	/**
	 * Parse as index into a sequence of values
	 */
	// If an Enum is overkill
	public static ValueParser<Integer> indexParser(String sequenceName, int indexFrom, String... values) {
		return s -> {
			String stripped = s.strip();
			int index = Arrays.asList(values).indexOf(stripped);
			if (index < 0) {
				throw new ValueParseException(
						stripped, String.format("\"%s\" is not a valid %s", stripped, sequenceName)
				);
			}
			return indexFrom + index;
		};
	}

	/**
	 * Parser for long integers
	 */
	public static final ValueParser<Long> LONG = numberParser(Long::parseLong, Long.class);

	/**
	 * Parser for integers
	 */
	public static final ValueParser<Integer> INTEGER = numberParser(Integer::parseInt, Integer.class);

	/**
	 * Parser for short integers
	 */
	public static final ValueParser<Short> SHORT = numberParser(Short::parseShort, Short.class);

	/**
	 * Parser for double precision floats
	 */
	public static final ValueParser<Double> DOUBLE = numberParser(Double::parseDouble, Double.class);

	/**
	 * Parser for single precision floats
	 */
	public static final ValueParser<Float> FLOAT = numberParser(Float::parseFloat, Float.class);

	/**
	 * Parser for integers as booleans
	 */
	public static final ValueParser<Boolean> LOGICAL = s -> INTEGER.parse(s) != 0;

	/**
	 * Parser for Characters
	 */
	public static final ValueParser<Character> CHARACTER = s -> {
		if (s.isBlank()) {
			throw new ValueParseException("Character is blank");
		}
		return s.charAt(0);
	};

	/**
	 * Parser for strings that does not strip whitespace
	 */
	public static final ValueParser<String> STRING_UNSTRIPPED = s -> s;

	/**
	 * Parser for strings
	 */
	public static final ValueParser<String> STRING = String::strip;

	/**
	 * Parser for a region identifier
	 */
	public static final ValueParser<Region> REGION = (s) -> Region.fromAlias(Character.toUpperCase(s.charAt(0)))
			.orElseThrow(() -> new ValueParseException(s, s + " is not a valid region identifier"));

	public static <U> ValueParser<List<U>> list(ValueParser<U> delegate) {
		return s -> {
			var elementStrings = s.strip().split("\s+");
			List<U> result = new ArrayList<>();
			for (String elementString : elementStrings) {
				result.add(delegate.parse(elementString));
			}
			return Collections.unmodifiableList(result);
		};
	}

	/**
	 * Wrap a parser with an additional validation step
	 *
	 * @param <U>
	 * @param delegate
	 * @param validator Function that returns an error string if the parsed value is
	 *                  invalid
	 * @return
	 */
	public static <U> ValueParser<U> validate(ValueParser<U> delegate, Function<U, Optional<String>> validator) {
		return uncontrolled(ControlledValueParser.validate(delegate, (v, c) -> validator.apply(v)));
	}

	/**
	 * Makes a parser that parses if the string is not blank, and returns an empty
	 * Optional otherwise.
	 *
	 * @param delegate Parser to use if the string is not blank
	 */
	public static <U> ValueParser<Optional<U>> optional(ValueParser<U> delegate) {
		return uncontrolled(ControlledValueParser.optional(delegate));
	}

	/**
	 * Parse a string as a set of fixed length chucks.
	 *
	 * @param <U>
	 * @param length   length of a chunk
	 * @param delegate parser for the individual chunks
	 * @return
	 */
	public static <U> ValueParser<List<U>> segmentList(int length, ValueParser<U> delegate) {
		return s -> {
			var result = new ArrayList<U>( (s.length() + length - 1) / length);
			for (int i = 0; i < s.length(); i += length) {
				var j = Math.min(i + length, s.length());
				result.add(delegate.parse(s.substring(i, j)));
			}
			return Collections.unmodifiableList(result);
		};
	}

	static <U> ValueParser<U> uncontrolled(ControlledValueParser<U> delegate) {
		return s -> delegate.parse(s, Collections.emptyMap());
	}
}
