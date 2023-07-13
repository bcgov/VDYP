package ca.bc.gov.nrs.vdyp.io.parse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Wraps a StreamingParser and groups its entries.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 * @param <U>
 */
public abstract class GroupingStreamingParser<T, U> implements StreamingParser<T> {

	final StreamingParser<U> delegate;

	Optional<List<U>> next = Optional.empty();

	public GroupingStreamingParser(StreamingParser<U> delegate) {
		super();
		this.delegate = delegate;
	}

	/**
	 * Returns true if a child should be skipped
	 *
	 * @param nextChild
	 * @return
	 */
	protected abstract boolean skip(U nextChild);

	/**
	 * Returns true if a schild is an end of group marker
	 *
	 * @param nextChild
	 * @return
	 */
	protected abstract boolean stop(U nextChild);

	/**
	 * Convert a list of children to a result.
	 *
	 * @param children
	 * @return
	 */
	protected abstract T convert(List<U> children);

	@Override
	public T next() throws IOException, ResourceParseException {

		doGetNext();
		var children = next.orElseThrow(() -> new NoSuchElementException("Requested next group when there is none"));
		next = Optional.empty();
		return convert(children);
	}

	protected void doGetNext() throws IOException, ResourceParseException {

		if (next.isEmpty()) {
			var nextResult = new ArrayList<U>();

			var nextChild = safeNextChild();
			while (nextChild.map(x -> !stop(x)).orElse(false)) {
				nextResult.add(nextChild.get());
				nextChild = safeNextChild();
			}
			if (nextChild.isEmpty()) {
				return;
			}
			next = Optional.of(nextResult);

		}
	}

	protected Optional<U> safeNextChild() throws IOException, ResourceParseException {
		Optional<U> result = Optional.empty();
		while (result.isEmpty() && delegate.hasNext()) {
			if (delegate.hasNext())
				result = Optional.of(delegate.next()).filter(x -> !skip(x));
		}
		return result;
	}

	@Override
	public boolean hasNext() throws IOException, ResourceParseException {
		doGetNext();

		return next.isPresent();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

}
