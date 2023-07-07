package ca.bc.gov.nrs.vdyp.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import ca.bc.gov.nrs.vdyp.io.FileResolver;

public class TestUtils {

	public static InputStream makeStream(String... lines) {
		return new ByteArrayInputStream(String.join("\r\n", lines).getBytes());
	}

	public static FileResolver fileResolver(String expectedFilename, InputStream is) {
		return new FileResolver() {

			@Override
			public InputStream resolve(String filename) throws IOException {
				if (filename.equals(expectedFilename)) {
					return is;
				} else {
					fail("Attempted to resolve unexpected filename " + filename);
					return null;
				}
			}

			@Override
			public String toString(String filename) throws IOException {
				return "TEST:" + filename;
			}

		};
	}
}
