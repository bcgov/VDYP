package ca.bc.gov.nrs.vdyp.test;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import ca.bc.gov.nrs.vdyp.io.FileSystemFileResolver;

/**
 * A mock file resolver for testing which returns specified
 */
public class MockFileResolver extends FileSystemFileResolver {

	Map<String, InputStream> isMap = new HashMap<>();
	Map<String, OutputStream> osMap = new HashMap<>();
	Map<String, FileSystemFileResolver> subResolverMap = new HashMap<>();

	Map<String, Supplier<? extends Throwable>> errorMap = new HashMap<>();

	String name;

	public MockFileResolver(String name) {
		this.name = name;
	}

	private <V> V get(Map<String, V> map, String filename, String method) throws IOException {
		var errorGenerator = errorMap.get(filename);
		if (errorGenerator != null) {
			try {
				throw errorGenerator.get();
			} catch (Throwable e) {
				if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				}
				if (e instanceof Error) {
					throw (Error) e;
				}
				if (e instanceof IOException) {
					throw (IOException) e;
				}
				fail("Unexpected throwable type specified for MockFileResolver");
			}
		}
		var result = map.get(filename);
		if (result == null) {
			fail("Should not call FileResolver::" + method + "(\"" + filename + "\") on resolver " + name);
			return null;
		}

		return result;
	}

	@Override
	public InputStream resolveForInput(String filename) throws IOException {

		return get(isMap, filename, "resolveForInput");
	}

	@Override
	public OutputStream resolveForOutput(String filename) throws IOException {
		return get(osMap, filename, "resolveForOutput");
	}

	@Override
	public String toString(String filename) throws IOException {
		return "Mock(" + name + "):" + filename;
	}

	@Override
	public FileSystemFileResolver relative(String path) throws IOException {
		return (FileSystemFileResolver) get(subResolverMap, path, "relative");
	}

	public void addStream(String filename, InputStream is) {
		this.isMap.put(filename, is);
	}

	public void addStream(String filename, OutputStream os) {
		this.osMap.put(filename, os);
	}

	public void addChild(String filename, FileSystemFileResolver child) {
		this.subResolverMap.put(filename, child);
	}

	public void addError(String filename, Supplier<? extends Throwable> errorGenerator) {
		this.errorMap.put(filename, errorGenerator);
	}
}
