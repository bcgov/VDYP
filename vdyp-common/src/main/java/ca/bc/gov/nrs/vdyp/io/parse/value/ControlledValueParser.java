package ca.bc.gov.nrs.vdyp.io.parse.value;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import ca.bc.gov.nrs.vdyp.io.parse.coe.BecDefinitionParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.GenusDefinitionParser;
import ca.bc.gov.nrs.vdyp.model.UtilizationClass;

@FunctionalInterface
public interface ControlledValueParser<T> {

	public static final String DELEGATE_MUST_NOT_BE_NULL = "delegate must not be null";

	/**
	 * Parse a string to a value using a control map for context. Should not attempt to modify the map.
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
	 * @param validator Function that returns an error string if the parsed value is invalid
	 * @return
	 */
	public static <U> ControlledValueParser<U> validate(
			ControlledValueParser<U> delegate, BiFunction<U, Map<String, Object>, Optional<String>> validator
	) {
		Objects.requireNonNull(delegate, DELEGATE_MUST_NOT_BE_NULL);
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
	 * Makes a parser that parses if the string is not blank, and returns an empty Optional otherwise.
	 *
	 * @param delegate Parser to use if the string is not blank
	 */
	public static <U> ControlledValueParser<Optional<U>> optional(ControlledValueParser<U> delegate) {
		Objects.requireNonNull(delegate, DELEGATE_MUST_NOT_BE_NULL);
		return pretestOptional(delegate, s -> s != null && !s.isBlank());
	}

	/**
	 * Makes a parser that parses if the string passes the test, and returns an empty Optional otherwise.
	 *
	 * @param delegate Parser to use if the string is not blank
	 * @param test     Test to apply to the string
	 */
	public static <U> ControlledValueParser<Optional<U>>
			pretestOptional(ControlledValueParser<U> delegate, Predicate<String> test) {
		Objects.requireNonNull(delegate, DELEGATE_MUST_NOT_BE_NULL);
		return (s, c) -> {
			if (test.test(s)) {
				return Optional.of(delegate.parse(s, c));
			}
			return Optional.empty();
		};
	}

	/**
	 * Makes a parser that parses the string, then tests it, and returns empty if it fails.
	 *
	 * @param delegate Parser to use
	 * @param test     Test to apply to the parsed result
	 */
	public static <U> ControlledValueParser<Optional<U>>
			posttestOptional(ControlledValueParser<U> delegate, Predicate<U> test) {
		Objects.requireNonNull(delegate, DELEGATE_MUST_NOT_BE_NULL);
		return (s, c) -> {
			var result = delegate.parse(s, c);
			if (test.test(result)) {
				return Optional.of(result);
			}
			return Optional.empty();
		};
	}

	/**
	 * Parser that strips whitespace and validates that the string is a Genus (SP0) ID
	 */
	static final ControlledValueParser<String> GENUS = (string, control) -> {
		var result = string.strip();
		GenusDefinitionParser.checkSpecies(control, result);
		return result;
	};

	/**
	 * Parser that strips whitespace of a Species (SP64) id
	 */
	// Currently just parses as a string but marking it explicitly makes it clearer
	// and could allow for validation later.
	public static final ControlledValueParser<String> SPECIES = (s, c) -> s.strip();

	/**
	 * Parser that strips whitespace and validates that the string is a BEC ID
	 */
	static final ControlledValueParser<String> BEC = (string, control) -> {
		var result = string.strip();
		if (!BecDefinitionParser.getBecs(control).getBecAliases().contains(result)) {
			throw new ValueParseException(string, string + " is not a valid BEC");
		}
		return result;
	};

	/**
	 * Parser that strips whitespace and validates that the string is a Utilization Class name
	 */
	static final ControlledValueParser<UtilizationClass> UTILIZATION_CLASS = (string, control) -> {
		try {
			return UtilizationClass.getByIndex(string.strip());
		} catch (IllegalArgumentException e) {
			throw new ValueParseException(string, string + " is not a valid Utilization Class");
		}
	};
}
