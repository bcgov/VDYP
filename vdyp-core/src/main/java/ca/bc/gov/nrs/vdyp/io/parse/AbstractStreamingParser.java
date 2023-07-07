package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.io.parse.LineParser.LineStream;

public abstract class AbstractStreamingParser<T> implements StreamingParser<T> {

	private LineStream lineStream;

	/**
	 * Create a new streaming parser
	 *
	 * @param is         Input stream to read from
	 * @param lineParser
	 * @param control
	 */
	public AbstractStreamingParser(InputStream is, LineParser lineParser, Map<String, Object> control) {

		this.lineStream = lineParser.parseAsStream(is, control);
	}

	@Override
	public T next() throws IOException, ResourceParseException {
		return this.convert(lineStream.next());
	}

	/**
	 * Set up the line parser to read the resource
	 *
	 * @return
	 */
	protected abstract T convert(Map<String, Object> entry) throws ResourceParseException;

	@Override
	public boolean hasNext() throws IOException, ResourceParseException {
		return lineStream.hasNext();
	}

	@Override
	public void close() throws IOException {
		lineStream.close();
	}

}
