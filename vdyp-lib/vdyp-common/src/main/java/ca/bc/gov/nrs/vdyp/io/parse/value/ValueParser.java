package ca.bc.gov.nrs.vdyp.io.parse.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import ca.bc.gov.nrs.vdyp.common.ValueOrMarker;
import ca.bc.gov.nrs.vdyp.model.LayerType;
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

	public static final String S_IS_NOT_A_VALID_S = "\"%s\" is not a valid %s";

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
	 * @param parser method that parses a string into the required type and throws NumerFormatException
	 * @param klazz  the type to parse into
	 */
	public static <U extends Number> ValueParser<U> numberParser(JavaNumberParser<U> parser, Class<U> klazz) {
		return s -> {
			String stripped = s.strip();
			try {
				return parser.parse(stripped);
			} catch (NumberFormatException ex) {
				throw new ValueParseException(
						stripped, String.format(S_IS_NOT_A_VALID_S, stripped, klazz.getSimpleName()), ex
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
						stripped, String.format(S_IS_NOT_A_VALID_S, stripped, klazz.getSimpleName()), ex
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
				throw new ValueParseException(stripped, String.format(S_IS_NOT_A_VALID_S, stripped, sequenceName));
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
	 * Parser for single precision floats >0
	 */
	public static final ValueParser<Optional<Float>> SAFE_POSITIVE_FLOAT = rangeSilentLow(
			FLOAT, 0f, false, Float.MAX_VALUE, true, "positive float"
	);

	/**
	 * Parser for single precision floats >=0
	 */
	public static final ValueParser<Optional<Float>> SAFE_NONNEGATIVE_FLOAT = rangeSilentLow(
			FLOAT, 0f, true, Float.MAX_VALUE, true, "non-negative float"
	);

	/**
	 * Parser for percentages
	 */
	public static final ValueParser<Float> PERCENTAGE = ValueParser
			.range(FLOAT, 0.0f, true, 100.0f, true, "Percentage");

	static final String RANGE_ERROR_TEMPLATE = "%s must be %s %s.";

	/**
	 * Validate that a parsed value is within a range
	 *
	 * @param parser     underlying parser
	 * @param min        the lower bound
	 * @param includeMin is the lower bound inclusive
	 * @param max        the upper bound
	 * @param includeMax is the upper bound inclusive
	 * @param name       Name for the value to use in the parse error if it is out of the range.
	 */
	public static <T extends Comparable<T>> ValueParser<T>
			range(ValueParser<T> parser, T min, boolean includeMin, T max, boolean includeMax, String name) {
		return validate(parser, x -> {
			if (x.compareTo(min) < (includeMin ? 0 : 1)) {
				return Optional.of(
						String.format(
								RANGE_ERROR_TEMPLATE, name, includeMin ? "greater than or equal to" : "greater than",
								min
						)
				);
			}
			if (x.compareTo(max) > (includeMax ? 0 : -1)) {
				return Optional.of(
						String.format(
								RANGE_ERROR_TEMPLATE, name, includeMax ? "less than or equal to" : "less than", max
						)
				);
			}
			return Optional.empty();
		});
	}

	/**
	 * Validate that a parsed value is within a range. Returns empty if out of bounds low and throws an exception if out
	 * of bounds high.
	 *
	 * @param parser     underlying parser
	 * @param min        the lower bound
	 * @param includeMin is the lower bound inclusive
	 * @param max        the upper bound
	 * @param includeMax is the upper bound inclusive
	 * @param name       Name for the value to use in the parse error if it is out of the range.
	 */
	public static <T extends Comparable<T>> ValueParser<Optional<T>>
			rangeSilentLow(ValueParser<T> parser, T min, boolean includeMin, T max, boolean includeMax, String name) {
		return validate(s -> {
			var result = parser.parse(s);
			if (result.compareTo(min) < (includeMin ? 0 : 1)) {
				return Optional.empty();
			}
			return Optional.of(result);
		}, result -> {
			return result.filter(x -> x.compareTo(max) > (includeMax ? 0 : -1)).map(
					x -> String
							.format(RANGE_ERROR_TEMPLATE, name, includeMax ? "less than or equal to" : "less than", max)
			);
		});
	}

	/**
	 * Parser for integers as booleans
	 */
	public static final ValueParser<Boolean> LOGICAL = s -> INTEGER.parse(s) != 0;

	/**
	 * Parser for integers as booleans restricted to the values 1 (true) and 0 (false)
	 */
	public static final ValueParser<Boolean> LOGICAL_0_1 = s -> {
		int v = INTEGER.parse(s);
		if (v == 0)
			return false;
		else if (v == 1)
			return true;
		else
			throw new ValueParseException("Logical value is not 0 or 1");
	};

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
	 * Parser for filenames
	 */
	public static final ValueParser<String> FILENAME = String::strip;

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
	 * @param validator Function that returns an error string if the parsed value is invalid
	 * @return
	 */
	public static <U> ValueParser<U> validate(ValueParser<U> delegate, Function<U, Optional<String>> validator) {
		return uncontrolled(ControlledValueParser.validate(delegate, (v, c) -> validator.apply(v)));
	}

	/**
	 * Creates a validator function for an inclusive range. See {@link validate}.
	 *
	 * @param <U>  A comparable type
	 * @param min  Minimum value allowed
	 * @param max  Maximum value allowed
	 * @param name Name of the field being validated to use in errors
	 */
	public static <U extends Comparable<U>> Function<U, Optional<String>>
			validateRangeInclusive(U min, U max, String name) {
		if (min.compareTo(max) > 0) {
			throw new IllegalArgumentException("min " + min + " is greater than max " + max);
		}
		return value -> {
			if (min.compareTo(value) > 0 || max.compareTo(value) < 0) {
				return Optional.of(name + " is expected to be between " + min + " and " + max + " but was " + value);
			}
			return Optional.empty();
		};
	}

	/**
	 * Makes a parser that parses if the string is not blank, and returns an empty Optional otherwise.
	 *
	 * @param delegate Parser to use if the string is not blank
	 */
	public static <U> ValueParser<Optional<U>> optional(ValueParser<U> delegate) {
		return uncontrolled(ControlledValueParser.optional(delegate));
	}

	/**
	 * Makes a parser that parses if the string passes the test, and returns an empty Optional otherwise.
	 *
	 * @param delegate Parser to use if the string is not blank
	 * @param test     Test to apply to the string
	 */
	public static <U> ValueParser<Optional<U>> pretestOptional(ValueParser<U> delegate, Predicate<String> test) {
		return uncontrolled(ControlledValueParser.pretestOptional(delegate, test));
	}

	/**
	 * Makes a parser that parses the string, then tests it, and returns empty if it fails.
	 *
	 * @param delegate Parser to use
	 * @param test     Test to apply to the parsed result
	 */
	public static <U> ValueParser<Optional<U>> posttestOptional(ValueParser<U> delegate, Predicate<U> test) {
		return uncontrolled(ControlledValueParser.posttestOptional(delegate, test));
	}

	/**
	 * Parse a string as a set of fixed length chunks.
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

	private static <U> ValueParser<U> uncontrolled(ControlledValueParser<U> delegate) {
		return s -> delegate.parse(s, Collections.emptyMap());
	}

	/**
	 * Attempt to parse as a marker using markerParser. If it returns empty, parse as a value with valueParser.
	 *
	 * @param valueParser  Parser from String to Value.
	 * @param markerParser Parser from String to Optional<Marker>. This should return empty if the string is not a
	 *                     marker.
	 * @return a ValueOrMarker
	 */
	public static <V, M> ValueParser<ValueOrMarker<V, M>>
			valueOrMarker(ValueParser<V> valueParser, ValueParser<Optional<M>> markerParser) {
		return s -> {
			var builder = new ValueOrMarker.Builder<V, M>();
			var marker = markerParser.parse(s).map(builder::marker);
			if (marker.isPresent()) {
				return marker.get();
			}

			return builder.value(valueParser.parse(s));
		};
	}

	/**
	 * Return the given value if the test passes, otherwise empty.
	 *
	 * @param <T>
	 * @param test
	 * @param value
	 * @return
	 */
	public static <T> ValueParser<Optional<T>> optionalSingleton(Predicate<String> test, T value) {
		return s -> test.test(s) ? Optional.of(value) : Optional.empty();
	}

	/**
	 * Parser for a layer identifier
	 */
	public static ValueParser<Optional<LayerType>> LAYER = s -> {
		switch (s.toUpperCase()) {
		case "1", "P":
			return Optional.of(LayerType.PRIMARY);
		case "2", "S":
			return Optional.of(LayerType.SECONDARY);
		case "V":
			return Optional.of(LayerType.VETERAN);
		default:
			return Optional.empty(); // Unknown
		}
	};

	/**
	 * Make a list parser return a map
	 *
	 * @param parser Parser for a list of values
	 * @param keys   Keys for each position in the list.
	 */
	@SafeVarargs
	public static <K, V> ValueParser<Map<K, V>> toMap(ValueParser<List<V>> parser, K... keys) {
		return toMap(parser, Collections.emptyMap(), keys);
	}

	/**
	 * Make a list parser return a map
	 *
	 * @param parser        Parser for a list of values
	 * @param defaultValues Map of default values. Keys with defaults must follow those without.
	 * @param keys          Keys for each position in the list.
	 */
	@SafeVarargs
	public static <K, V> ValueParser<Map<K, V>> toMap(ValueParser<List<V>> parser, Map<K, V> defaultValues, K... keys) {

		boolean defaultSeen = false;
		int required = 0;

		for (var key : keys) {
			var isDefault = defaultValues.containsKey(key);
			defaultSeen |= isDefault;
			required += isDefault ? 0 : 1;
			if (defaultSeen != isDefault) {
				throw new IllegalArgumentException("Keys with defaults must follow those without");
			}
		}
		final int requiredFinal = required;
		return s -> {
			var list = parser.parse(s);

			Map<K, V> result = new LinkedHashMap<>();
			if (defaultValues.isEmpty() && list.size() != keys.length) {
				throw new ValueParseException(
						s, "Expected exactly " + requiredFinal + " values but there were " + list.size()
				);
			}
			if (list.size() < requiredFinal || list.size() > keys.length) {
				throw new ValueParseException(
						s,
						"Expected between " + requiredFinal + " and " + keys.length + " values but there were "
								+ list.size()
				);
			}
			var it = list.iterator();
			for (int i = 0; i < keys.length; i++) {
				K key = keys[i];
				if (it.hasNext()) {
					result.put(key, it.next());
				} else {
					assert defaultValues.containsKey(key);
					result.put(key, defaultValues.get(key)); // should never be null due to preceding checks
				}
			}
			return result;
		};
	}

	/**
	 * Call the provided callback after parsing. This is meant for logging. It should not be used for business logic.
	 *
	 * @param delegate
	 * @param callback
	 * @return
	 */
	public static <T> ValueParser<T> callback(ValueParser<T> delegate, Consumer<T> callback) {
		return s -> {
			var result = delegate.parse(s);
			callback.accept(result);
			return result;
		};
	}

}
