package ca.bc.gov.nrs.vdyp.test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class TestUtils {

	public static InputStream makeStream(String... lines) {
		return new ByteArrayInputStream(String.join("\r\n", lines).getBytes());
	}
}
