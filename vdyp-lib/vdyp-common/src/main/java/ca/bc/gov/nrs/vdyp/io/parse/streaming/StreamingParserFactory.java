package ca.bc.gov.nrs.vdyp.io.parse.streaming;

import java.io.IOException;

public interface StreamingParserFactory<T> {

	/**
	 * Open the resource and get a streaming parser for it. This must be closed.
	 */
	StreamingParser<T> get() throws IOException;

}
