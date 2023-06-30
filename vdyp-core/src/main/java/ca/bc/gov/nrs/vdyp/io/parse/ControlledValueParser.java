package ca.bc.gov.nrs.vdyp.io.parse;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

@FunctionalInterface
public interface ControlledValueParser<T> {

	/**
	 * Parse a string to a value using a control map for context. Should not attempt
	 * to modify the map.
	 *
	 * @param s
	 * @param control
	 * @return
	 * @throws ValueParseException
	 */
	T parse(String s, Map<String, Object> control) throws ValueParseException;

	/**
	 * Wrap a parser with an additional validation step
	 *
	 * @param <U>
	 * @param delegate
	 * @param validator Function that returns an error string if the parsed value is
	 *                  invalid
	 * @return
	 */
	public static <U> ControlledValueParser<U> validate(
			ControlledValueParser<U> delegate, BiFunction<U, Map<String, Object>, Optional<String>> validator
	) {
		return (s, c) -> {
			var value = delegate.parse(s, c);
			var error = validator.apply(value, c);
			if (error.isPresent()) {
				throw new ValueParseException(s, error.get());
			}
			return value;
		};
	}

	/**
	 * Makes a parser that parses if the string is not blank, and returns an empty
	 * Optional otherwise.
	 *
	 * @param delegate Parser to use if the string is not blank
	 */
	public static <U> ControlledValueParser<Optional<U>> optional(ControlledValueParser<U> delegate) {
		return (s, c) -> {
			if (!s.isBlank()) {
				return Optional.of(delegate.parse(s, c));
			}
			return Optional.empty();
		};
	}

	/**
	 * Parser that strips whitespace and validates that the string is a Species ID
	 */
	static final ControlledValueParser<String> SPECIES = (string, control) -> {
		var result = string.strip();
		SP0DefinitionParser.checkSpecies(control, result);
		return result;
	};

	/**
	 * Parser that strips whitespace and validates that the string is a BEC ID
	 */
	static final ControlledValueParser<String> BEC = (string, control) -> {
		var result = string.strip();
		if (!BecDefinitionParser.getBecAliases(control).contains(string)) {
			throw new ValueParseException(string, string + " is not a valid BEC");
		}
		return result;
	};

}
