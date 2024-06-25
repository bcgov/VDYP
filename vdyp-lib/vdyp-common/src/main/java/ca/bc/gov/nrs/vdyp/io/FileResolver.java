package ca.bc.gov.nrs.vdyp.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileResolver {
	InputStream resolveForInput(String filename) throws IOException;

	OutputStream resolveForOutput(String filename) throws IOException;

	String toString(String filename) throws IOException;

	/**
	 * Create a FileResolver that resolves relative to a given path
	 *
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public FileResolver relative(String path) throws IOException;

}