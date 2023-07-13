package ca.bc.gov.nrs.vdyp.io.parse;

/**
 * Handler to apply the result of parsing to an under construction object
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <Entry>
 * @param <Result>
 */
@FunctionalInterface
public interface ParseEntryHandler<Entry, Result> {
	/**
	 * Apply the result of parsing to an under construction object
	 *
	 * @param entry  parsed record
	 * @param result object under construction
	 * @return the object under construction
	 * @throws ValueParseException if there was a problem with the parsed value
	 */
	public Result addTo(Entry entry, Result result) throws ValueParseException;
}
