package ca.bc.gov.nrs.vdyp.io.parse.control;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParser;
import ca.bc.gov.nrs.vdyp.io.parse.value.ValueParser;

/**
 * A resource that can be loaded as a sub-resource of a control file.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public interface ControlMapSubResourceParser<T> extends ResourceControlMapModifier, ResourceParser<T> {

	@Override
	default void modify(Map<String, Object> control, InputStream data) throws IOException, ResourceParseException {
		var result = this.parse(data, control);

		control.put(getControlKeyName(), result);
	}

	@Override
	default ValueParser<Object> getValueParser() {
		return FILENAME;
	}

}
