package ca.bc.gov.nrs.vdyp.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * The difference between a set of values and what they are expaected to be
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <T>
 */
public class ExpectationDifference<T> {
	final Set<T> missing;
	final Set<T> unexpected;

	public ExpectationDifference(Set<T> missing, Set<T> unexpected) {
		super();
		this.missing = missing;
		this.unexpected = unexpected;
	}

	/**
	 * Find the missing and expected values in a collections of values.
	 *
	 * @param <U>
	 * @param values
	 * @param expected
	 * @return
	 */
	public static <U> ExpectationDifference<U> difference(Collection<U> values, Collection<U> expected) {
		var missing = new HashSet<U>(expected);
		missing.removeAll(values);
		var unexpected = new HashSet<U>(values);
		unexpected.removeAll(expected);
		return new ExpectationDifference<U>(missing, unexpected);
	}

	/**
	 * Which values are missing
	 *
	 * @return
	 */
	public Set<T> getMissing() {
		return missing;
	}

	/**
	 * Which values were unexpected
	 *
	 * @return
	 */
	public Set<T> getUnexpected() {
		return unexpected;
	}

	/**
	 * Were the values as expected
	 *
	 * @return
	 */
	public boolean isSame() {
		return getMissing().isEmpty() && getUnexpected().isEmpty();
	}
}
