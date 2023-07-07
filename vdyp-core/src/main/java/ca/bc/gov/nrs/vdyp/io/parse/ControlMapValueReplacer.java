package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.io.FileResolver;

public interface ControlMapValueReplacer<Result, Raw> extends KeyedControlMapModifier {

	/**
	 * Remap a raw control value
	 *
	 * @param rawValue
	 * @return
	 * @throws ResourceParseException
	 * @throws IOException
	 */
	public Result map(Raw rawValue, FileResolver fileResolver, Map<String, Object> control)
			throws ResourceParseException, IOException;

	@SuppressWarnings("unchecked")
	@Override
	default void modify(Map<String, Object> control, FileResolver fileResolver)
			throws ResourceParseException, IOException {

		control.put(getControlKey(), this.map((Raw) control.get(this.getControlKey()), fileResolver, control));

	}

}
