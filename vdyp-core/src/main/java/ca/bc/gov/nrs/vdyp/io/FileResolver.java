package ca.bc.gov.nrs.vdyp.io;

import java.io.IOException;
import java.io.InputStream;

public interface FileResolver {
	InputStream resolve(String filename) throws IOException;

	String toString(String filename) throws IOException;
}