package ca.bc.gov.nrs.vdyp.io.parse.control;

import java.io.IOException;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common.ControlKey;
import ca.bc.gov.nrs.vdyp.io.FileResolver;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

public interface ControlMapModifier {

	void modify(Map<String, Object> control, FileResolver fileResolver) throws ResourceParseException, IOException;

	/**
	 * The name of the key for this resource's entry in the control map
	 *
	 * @return
	 */
	default String getControlKeyName() {
		return getControlKey().name();
	}

	/**
	 * The key for this resource's entry in the control map
	 *
	 * @return
	 */
	ControlKey getControlKey();

	/**
	 * Value parser for the control value before modification
	 *
	 * @return
	 */
	ValueParser<Object> getValueParser();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ValueParser<Object> FILENAME = (ValueParser) ValueParser.FILENAME;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ValueParser<Object> OPTIONAL_FILENAME = (ValueParser) ValueParser.optional(FILENAME);
}
