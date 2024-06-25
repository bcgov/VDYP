package ca.bc.gov.nrs.vdyp.io.parse.common;

import java.util.Optional;

import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParseException;

/**
 * Handler to apply the result of parsing to an under construction object
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <Entry>
 * @param <Result>
 */
@FunctionalInterface
@SuppressWarnings("java:S119")
public interface ParseEntryHandler<Entry, Result> {

	/**
	 * Apply the result of parsing to an under construction object
	 *
	 * @param entry  parsed record
	 * @param result object under construction
	 * @return the object under construction
	 * @throws ValueParseException if there was a problem with the parsed value
	 */
	default Result addTo(Entry entry, Result result) throws ValueParseException {
		return addTo(entry, result, Optional.empty());
	}

	/**
	 * Apply the result of parsing to an under construction object
	 *
	 * @param entry  parsed record
	 * @param result object under construction
	 * @return the object under construction
	 * @throws ValueParseException if there was a problem with the parsed value
	 */
	default Result addTo(Entry entry, Result result, int line) throws ValueParseException {
		return addTo(entry, result, Optional.of(line));
	}

	/**
	 * Apply the result of parsing to an under construction object
	 *
	 * @param entry  parsed record
	 * @param result object under construction
	 * @return the object under construction
	 * @throws ValueParseException if there was a problem with the parsed value
	 */
	Result addTo(Entry entry, Result result, Optional<Integer> line) throws ValueParseException;
}
