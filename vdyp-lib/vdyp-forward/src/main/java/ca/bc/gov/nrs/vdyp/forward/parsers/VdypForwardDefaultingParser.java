package ca.bc.gov.nrs.vdyp.forward.parsers;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ca.bc.gov.nrs.vdyp.forward.model.VdypEntity;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

public interface VdypForwardDefaultingParser extends ValueParser<Float> {

	/**
	 * Parser for non-negative single precision floats with default -9.0. -9.0 results in an
	 * VdypEntity.MISSING_FLOAT_VALUE being returned. All other negative values, and those greater than Float.MAX_VALUE,
	 * result in an error.
	 */
	public static final ValueParser<Float> FLOAT_WITH_DEFAULT = rangeSilentWithDefaulting(
			FLOAT, 0.0f, true, Float.MAX_VALUE, true, -9.0f, VdypEntity.MISSING_FLOAT_VALUE, "non-negative float"
	);

	/**
	 * Parser for non-negative integers with default -9. -9 results in VdypEntity.MISSING_INTEGER_VALUE being returned.
	 * All other negative values, and those > Float.MAX_VALUE, result in an error.
	 */
	public static final ValueParser<Integer> INTEGER_WITH_DEFAULT = rangeSilentWithDefaulting(
			INTEGER, 0, true, Integer.MAX_VALUE, true, -9, VdypEntity.MISSING_INTEGER_VALUE, "non-negative integer"
	);

	/**
	 * Validate that a parsed value is greater than 0 and less than (or, if includeMax is true, equal to) max.
	 * Additionally, if the value is -9.0, it is considered "not present" and Float.NaN is returns.
	 *
	 * @param parser     underlying parser
	 * @param max        the upper bound
	 * @param includeMax is the upper bound inclusive
	 * @param name       Name for the value to use in the parse error if it is out of the range.
	 */
	public static <T extends Comparable<T>> ValueParser<T> rangeSilentWithDefaulting(
			ValueParser<T> parser, T min, boolean includeMin, T max, boolean includeMax, T missingIndicator,
			T defaultValue, String name
	) {
		return ValueParser.validate(s -> {
			var result = defaultValue;
			if (!StringUtils.isEmpty(s)) {
				result = parser.parse(s);
				if (missingIndicator.equals(result)) {
					result = defaultValue;
				}
			}
			return result;
		}, result -> {
			if (!defaultValue.equals(result) && (result.compareTo(max) > (includeMax ? 0 : -1)
					|| result.compareTo(min) < (includeMin ? 0 : 1))) {
				return Optional.of(
						String.format(
								"{} must be between {} ({}) and {} ({})", name, min,
								includeMin ? "inclusive" : "exclusive", max, includeMax ? "inclusive" : "exclusive"
						)
				);
			}
			return Optional.empty();
		});
	}
}
