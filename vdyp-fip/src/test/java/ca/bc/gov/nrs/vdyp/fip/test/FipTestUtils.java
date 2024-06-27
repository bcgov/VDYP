package ca.bc.gov.nrs.vdyp.fip.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.opentest4j.AssertionFailedError;

import ca.bc.gov.nrs.vdyp.application.VdypApplicationIdentifier;
import ca.bc.gov.nrs.vdyp.fip.FipControlParser;
import ca.bc.gov.nrs.vdyp.io.parse.coe.ModifierParser;
import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.model.Region;
import ca.bc.gov.nrs.vdyp.test.TestUtils;

public class FipTestUtils {

	/**
	 * Apply modifiers to mock test map to simulate control file parser.
	 *
	 * @param controlMap
	 */
	public static void modifyControlMap(HashMap<String, Object> controlMap) {
		VdypApplicationIdentifier jprogram = VdypApplicationIdentifier.FIP_START;
		TestUtils.populateControlMapFromResource(controlMap, new ModifierParser(jprogram), "mod19813.prm");

	}

	/**
	 * Load the control map from resources in the test package using the full control map parser.
	 */
	public static Map<String, Object> loadControlMap() {
		BaseControlParser parser = new FipControlParser();
		try {
			return TestUtils.loadControlMap(parser, TestUtils.class, "FIPSTART.CTR");
		} catch (IOException | ResourceParseException ex) {
			throw new AssertionFailedError(null, ex);
		}

	}
}
