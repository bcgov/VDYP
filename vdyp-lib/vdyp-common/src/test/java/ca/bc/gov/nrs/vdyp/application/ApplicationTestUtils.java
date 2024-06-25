package ca.bc.gov.nrs.vdyp.application;

import java.util.Map;

/**
 * Test related utilities that need to be in the ca.bc.gov.nrs.vdyp.application package for visibility
 */
public class ApplicationTestUtils {
	/**
	 * Allows tests to set the control map of a VdypStartApplication
	 *
	 * @param app
	 * @param controlMap
	 */
	public static void setControlMap(VdypStartApplication<?, ?, ?, ?> app, Map<String, Object> controlMap) {
		app.setControlMap(controlMap);
	}
}
