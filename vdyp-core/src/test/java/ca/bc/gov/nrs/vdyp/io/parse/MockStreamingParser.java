package ca.bc.gov.nrs.vdyp.io.parse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MockStreamingParser<T> implements StreamingParser<T> {

	boolean closed = false;
	String name;

	Queue<State> remaining = new LinkedList<>();
	State current = null;

	private class State {

		public final boolean hasNext;
		public final boolean isException;
		public final T next;
		public final Throwable toThrow;

		public State(T next) {
			this.next = next;
			this.hasNext = true;
			this.isException = false;
			this.toThrow = null;
		}

		public State(Throwable ex, boolean hasNext) {
			this.next = null;
			this.hasNext = hasNext;
			this.isException = true;
			this.toThrow = ex;
		}

		public T get() throws IOException, ResourceParseException {
			if (isException) {
				if (toThrow instanceof IOException) {
					throw (IOException) toThrow;
				} else if (toThrow instanceof ResourceParseException) {
					throw (ResourceParseException) toThrow;
				} else if (toThrow instanceof RuntimeException) {
					throw (RuntimeException) toThrow;
				} else if (toThrow instanceof Error) {
					throw (Error) toThrow;
				} else {
					fail("MockStreamingParser can not throw " + toThrow);
				}
			}
			return next;
		}

	}

	@Override
	public T next() throws IOException, ResourceParseException {
		var current = remaining.remove();
		return current.get();
	}

	@Override
	public boolean hasNext() throws IOException, ResourceParseException {
		var current = remaining.peek();
		return current != null && current.hasNext;
	}

	@Override
	public void close() throws IOException {
		closed = true;
	}

	public void expectClosed() {
		assertTrue(closed, name + " was not closed");
	};

	public void expectAllRead() {
		assertTrue(remaining.isEmpty(), name + " has " + remaining.size() + " states remaining");
	};

	public void addValue(T value) {
		remaining.add(new State(value));
	}

	public void addValues(@SuppressWarnings("unchecked") T... values) {
		for (var value : values)
			remaining.add(new State(value));
	}

	public void addValues(List<T> values) {
		for (var value : values)
			remaining.add(new State(value));
	}

	public void addThrow(Throwable ex, boolean hasNext) {
		remaining.add(new State(ex, hasNext));
	}
}
