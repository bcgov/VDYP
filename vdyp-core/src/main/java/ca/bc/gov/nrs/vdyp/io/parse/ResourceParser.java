package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * A parser for a multi-line recource
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
	 * @throws IOException                if there is an error communicating with
	 *                                    the input stream
	 * @throws ResourceParseLineException if there is a problem with the content of
	 *                                    the resource
	 */
	T parse(InputStream is, Map<String, Object> control) throws IOException, ResourceParseException;

	/**
	 * Parse a resource from the classpath
	 *
	 * @param klazz
	 * @param resourcePath
	 * @param control      Control parameter map
	 * @return The parsed resource
	 * @throws IOException                if there is an error communicating with
	 *                                    the input stream
	 * @throws ResourceParseLineException if there is a problem with the content of
	 *                                    the resource
	 */
	default T parse(Class<?> klazz, String resourcePath, Map<String, Object> control)
			throws IOException, ResourceParseException {
		try (var is = klazz.getResourceAsStream(resourcePath)) {
			return parse(is, control);
		}
	}

	/**
	 * Parse a resource from a file
	 *
	 * @param resourcePath
	 * @param control      Control parameter map
	 * @return The parsed resource
	 * @throws IOException                if there is an error communicating with
	 *                                    the input stream
	 * @throws ResourceParseLineException if there is a problem with the content of
	 *                                    the resource
	 */
	default T parse(Path resourcePath, Map<String, Object> control) throws IOException, ResourceParseException {
		try (InputStream is = Files.newInputStream(resourcePath)) {
			return parse(is, control);
		}
	}

	@SuppressWarnings("unchecked")
	static <U> U expectParsedControl(Map<String, Object> control, String key, Class<? super U> clazz) {
		var value = control.get(key);
		if (value == null) {
			throw new IllegalStateException("Expected control map to have " + key);
		}
		if (clazz != String.class && value instanceof String) {
			throw new IllegalStateException(
					"Expected control map entry " + key + " to be parsed but was still a String " + value
			);
		}
		if (!clazz.isInstance(value)) {
			throw new IllegalStateException(
					"Expected control map entry " + key + " to be a " + clazz.getSimpleName() + " but was a "
							+ value.getClass()
			);
		}
		return (U) value;
	}
}
