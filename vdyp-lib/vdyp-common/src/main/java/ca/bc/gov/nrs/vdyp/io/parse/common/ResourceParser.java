package ca.bc.gov.nrs.vdyp.io.parse.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * A parser for a multi-line resource
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
@FunctionalInterface
public interface ResourceParser<T> {
	/**
	 * Parse an InputStream
	 *
	 * @param is
	 * @param control Control parameter map
	 * @return The parsed resource
	 * @throws IOException                if there is an error communicating with the input stream
	 * @throws ResourceParseLineException if there is a problem with the content of the resource
	 */
	T parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException;

	/**
	 * Parse a resource from the classpath
	 *
	 * @param klazz
	 * @param resourcePath
	 * @param control      Control parameter map
	 * @return The parsed resource
	 * @throws IOException                if there is an error communicating with the input stream
	 * @throws ResourceParseLineException if there is a problem with the content of the resource
	 */
	default T parse(Class<?> klazz, String resourcePath, Map<String, Object> control)
			throws IOException, ResourceParseException {
		try (var is = klazz.getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IllegalStateException(
						String.format("Could not find %s in %s", resourcePath, klazz.getPackage())
				);
			}
			return parse(is, control);
		}
	}

	/**
	 * Parse a resource from a file
	 *
	 * @param resourcePath
	 * @param control      Control parameter map
	 * @return The parsed resource
	 * @throws IOException                if there is an error communicating with the input stream
	 * @throws ResourceParseLineException if there is a problem with the content of the resource
	 */
	default T parse(Path resourcePath, Map<String, Object> control) throws IOException, ResourceParseException {
		try (InputStream is = Files.newInputStream(resourcePath)) {
			return parse(is, control);
		}
	}
}
