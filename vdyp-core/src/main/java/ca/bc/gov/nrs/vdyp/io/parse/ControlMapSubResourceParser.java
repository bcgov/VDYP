package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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

		control.put(getControlKey(), result);
	}

}
