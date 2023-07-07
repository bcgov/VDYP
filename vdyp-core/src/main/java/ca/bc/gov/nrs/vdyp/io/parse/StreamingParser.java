package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;

public interface StreamingParser<T> extends AutoCloseable {

	/**
	 * Get the next entry in the resource
	 *
	 * @return
	 * @throws IOException
	 * @throws ResourceParseException
	 */
	T next() throws IOException, ResourceParseException;

	/**
	 * Is there a next resource
	 *
	 * @return
	 * @throws IOException
	 * @throws ResourceParseException
	 */
	boolean hasNext() throws IOException, ResourceParseException;

	@Override
	void close() throws IOException;

}
