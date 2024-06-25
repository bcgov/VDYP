package ca.bc.gov.nrs.vdyp.vri.test;

import java.io.IOException;
import java.util.Map;

import org.opentest4j.AssertionFailedError;

import ca.bc.gov.nrs.vdyp.io.parse.common.ResourceParseException;
import ca.bc.gov.nrs.vdyp.io.parse.control.BaseControlParser;
import ca.bc.gov.nrs.vdyp.test.TestUtils;
import ca.bc.gov.nrs.vdyp.vri.VriControlParser;

public class VriTestUtils {

	/**
	 * Load the control map from resources in the test package using the full control map parser.
	 */
	public static Map<String, Object> loadControlMap() {
		BaseControlParser parser = new VriControlParser();
		try {
			return TestUtils.loadControlMap(parser, TestUtils.class, "VRISTART.CTR");
		} catch (IOException | ResourceParseException ex) {
			throw new AssertionFailedError(null, ex);
		}

	}
}
