package ca.bc.gov.nrs.vdyp.io.parse;

import java.util.Map;

/**
 * Replace a control map entry referencing a file with a parsed version of that
 * file if it was specified, otherwise initialize that entry with a default.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public interface OptionalControlMapSubResourceParser<T>
		extends ControlMapSubResourceParser<T>, OptionalResourceControlMapModifier {

	default void defaultModify(Map<String, Object> control) {
		control.put(getControlKey(), defaultResult());
	}

	/**
	 * The default value
	 *
	 * @return
	 */
	T defaultResult();

}
