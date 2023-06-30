package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.io.FileResolver;

/**
 * Modifies the control map based on a resource
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public interface ResourceControlMapModifier extends ControlMapModifier {

	void modify(Map<String, Object> control, InputStream data) throws ResourceParseException, IOException;

	/**
	 * The key for this resource's entry in the control map
	 *
	 * @return
	 */
	String getControlKey();

	/**
	 * Replace the entry in the control map containing the filename for a resource
	 * with the parsed resource
	 *
	 * @param control
	 * @param fileResolver
	 * @throws IOException
	 * @throws ResourceParseException
	 */
	@Override
	default void modify(Map<String, Object> control, FileResolver fileResolver)
			throws IOException, ResourceParseException {
		var filename = (String) control.get(getControlKey());
		try (InputStream data = fileResolver.resolve(filename)) {
			modify(control, data);
		}
	}
}
