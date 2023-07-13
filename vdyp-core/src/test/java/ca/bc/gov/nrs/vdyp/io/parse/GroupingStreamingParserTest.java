package ca.bc.gov.nrs.vdyp.io.parse;

import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertEmpty;
import static ca.bc.gov.nrs.vdyp.test.VdypMatchers.assertNext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMock;
import org.junit.jupiter.api.Test;

public class GroupingStreamingParserTest {

	@Test
	public void testEmpty() throws Exception {
		var control = EasyMock.createControl();

		StreamingParser<Integer> delegate = control.createMock("delegate", StreamingParser.class);

		EasyMock.expect(delegate.hasNext()).andStubReturn(false);

		control.replay();

		var unit = new GroupingStreamingParser<List<Integer>, Integer>(delegate) {

			@Override
			protected boolean skip(Integer nextChild) {
				return nextChild % 3 == 0;
			}

			@Override
			protected boolean stop(Integer nextChild) {
				return nextChild % 5 == 0;
			}

			@Override
			protected List<Integer> convert(List<Integer> children) {
				return children;
			}

		};

		assertEmpty(unit);

		control.verify();
	}

	@Test
	public void testOneEntry() throws Exception {
		var control = EasyMock.createControl();

		var mock = Arrays.asList(1, 5).iterator();

		StreamingParser<Integer> delegate = control.createMock("delegate", StreamingParser.class);

		EasyMock.expect(delegate.hasNext()).andStubAnswer(mock::hasNext);
		EasyMock.expect(delegate.next()).andStubAnswer(mock::next);

		control.replay();

		var unit = new GroupingStreamingParser<List<Integer>, Integer>(delegate) {

			@Override
			protected boolean skip(Integer nextChild) {
				return nextChild % 3 == 0;
			}

			@Override
			protected boolean stop(Integer nextChild) {
				return nextChild % 5 == 0;
			}

			@Override
			protected List<Integer> convert(List<Integer> children) {
				return children;
			}

		};

		var group = assertNext(unit);
		assertThat(group, contains(1));

		assertEmpty(unit);

		control.verify();
	}

	@Test
	public void testMultipleEntries() throws Exception {
		var control = EasyMock.createControl();

		var mock = Arrays.asList(1, 2, 5).iterator();

		StreamingParser<Integer> delegate = control.createMock("delegate", StreamingParser.class);

		EasyMock.expect(delegate.hasNext()).andStubAnswer(mock::hasNext);
		EasyMock.expect(delegate.next()).andStubAnswer(mock::next);

		control.replay();

		var unit = new GroupingStreamingParser<List<Integer>, Integer>(delegate) {

			@Override
			protected boolean skip(Integer nextChild) {
				return nextChild % 3 == 0;
			}

			@Override
			protected boolean stop(Integer nextChild) {
				return nextChild % 5 == 0;
			}

			@Override
			protected List<Integer> convert(List<Integer> children) {
				return children;
			}

		};

		var group = assertNext(unit);
		assertThat(group, contains(1, 2));

		assertEmpty(unit);

		control.verify();

	}

	@Test
	public void testMultipleGroups() throws Exception {
		var control = EasyMock.createControl();

		var mock = Arrays.asList(1, 5, 2, 5).iterator();

		StreamingParser<Integer> delegate = control.createMock("delegate", StreamingParser.class);

		EasyMock.expect(delegate.hasNext()).andStubAnswer(mock::hasNext);
		EasyMock.expect(delegate.next()).andStubAnswer(mock::next);

		control.replay();

		var unit = new GroupingStreamingParser<List<Integer>, Integer>(delegate) {

			@Override
			protected boolean skip(Integer nextChild) {
				return nextChild % 3 == 0;
			}

			@Override
			protected boolean stop(Integer nextChild) {
				return nextChild % 5 == 0;
			}

			@Override
			protected List<Integer> convert(List<Integer> children) {
				return children;
			}

		};

		var group = assertNext(unit);
		assertThat(group, contains(1));

		group = assertNext(unit);
		assertThat(group, contains(2));

		assertEmpty(unit);

		control.verify();

	}

	@Test
	public void testSkip() throws Exception {
		var control = EasyMock.createControl();

		var mock = Arrays.asList(3, 1, 5).iterator();

		StreamingParser<Integer> delegate = control.createMock("delegate", StreamingParser.class);

		EasyMock.expect(delegate.hasNext()).andStubAnswer(mock::hasNext);
		EasyMock.expect(delegate.next()).andStubAnswer(mock::next);

		control.replay();

		var unit = new GroupingStreamingParser<List<Integer>, Integer>(delegate) {

			@Override
			protected boolean skip(Integer nextChild) {
				return nextChild % 3 == 0;
			}

			@Override
			protected boolean stop(Integer nextChild) {
				return nextChild % 5 == 0;
			}

			@Override
			protected List<Integer> convert(List<Integer> children) {
				return children;
			}

		};

		var group = assertNext(unit);
		assertThat(group, contains(1));

		assertEmpty(unit);

		control.verify();
	}

}
