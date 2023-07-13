package ca.bc.gov.nrs.vdyp.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileSystemFileResolver implements FileResolver {
	@Override
	public InputStream resolve(String filename) throws IOException {
		return new FileInputStream(filename);
	}

	@Override
	public String toString(String filename) throws IOException {
		return String.format("file:%s", filename);
	}
}