package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.io.FileResolver;

/**
 * Modify a control map based on a resource referenced within it if it is
 * specified, otherwise perform a default operation.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public interface OptionalResourceControlMapModifier extends ResourceControlMapModifier {

	@Override
	default void modify(Map<String, Object> control, FileResolver fileResolver)
			throws IOException, ResourceParseException {
		@SuppressWarnings("unchecked")
		var filename = (Optional<String>) control.get(getControlKey());
		if (filename.isPresent()) {
			try (InputStream data = fileResolver.resolve(filename.get())) {
				modify(control, data);
			}
		} else {
			defaultModify(control);
		}

	}

	/**
	 * Perform this operation on the control map if no resource is specified.
	 *
	 * @param control
	 */
	void defaultModify(Map<String, Object> control);

}
