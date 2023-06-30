package ca.bc.gov.nrs.vdyp.test;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import ca.bc.gov.nrs.vdyp.io.parse.ValueParseException;
import ca.bc.gov.nrs.vdyp.io.parse.ValueParser;
import ca.bc.gov.nrs.vdyp.model.BecDefinition;
import ca.bc.gov.nrs.vdyp.model.BecLookup;
import ca.bc.gov.nrs.vdyp.model.BecLookup.Substitution;
import ca.bc.gov.nrs.vdyp.model.MatrixMap;

/**
 * Custom Hamcrest Matchers
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class VdypMatchers {

	/**
	 * Matches a string if when parsed by the parser method it matches the given
	 * matcher
	 *
	 * @param parsedMatcher matcher for the parsed value
	 * @param parser        parser
	 * @return
	 */
	public static <T> Matcher<String> parseAs(Matcher<? super T> parsedMatcher, ValueParser<T> parser) {
		return new BaseMatcher<String>() {

			@Override
			public boolean matches(Object actual) {
				try {
					return parsedMatcher.matches(parser.parse(actual.toString()));
				} catch (ValueParseException e) {
					return false;
				}
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("parses as ");
				parsedMatcher.describeTo(description);
			}

		};
	}

	/**
	 * Matcher for the cause of an exception
	 *
	 * @param causeMatcher
	 * @return
	 */
	public static Matcher<Throwable> causedBy(Matcher<? extends Throwable> causeMatcher) {

		return new BaseMatcher<Throwable>() {

			@Override
			public boolean matches(Object actual) {
				return causeMatcher.matches( ((Throwable) actual).getCause());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("was caused by exception that ").appendDescriptionOf(causeMatcher);
			}

		};
	}

	/**
	 * Matches an Optional if it is present and its value matches the given matcher
	 *
	 * @param delegate matcher for the optional's value
	 * @return
	 */
	public static <T> Matcher<Optional<T>> present(Matcher<? super T> delegate) {
		return new BaseMatcher<Optional<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean matches(Object actual) {
				if (! (actual instanceof Optional)) {
					return false;
				}
				if (! ((Optional<?>) actual).isPresent()) {
					return false;
				}
				return delegate.matches( ((Optional<T>) actual).get());
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Optional that ");
				delegate.describeTo(description);
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				if (! (item instanceof Optional)) {
					description.appendText("Not an Optional");
					return;
				}
				if (! ((Optional<?>) item).isPresent()) {
					description.appendText("Not present");
					return;
				}
				delegate.describeMismatch(item, description);
			}

		};
	}

	/**
	 * Matches an Optional if it is not present
	 *
	 * @param <T>
	 *
	 * @return
	 */
	public static <T> Matcher<Optional<T>> notPresent() {
		return new BaseMatcher<Optional<T>>() {

			@Override
			public boolean matches(Object actual) {
				if (! (actual instanceof Optional)) {
					return false;
				}
				return ! ((Optional<?>) actual).isPresent();
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Optional that is empty");
			}

			@Override
			public void describeMismatch(Object item, Description description) {
				if (! (item instanceof Optional)) {
					description.appendText("was not an Optional");
					return;
				}
				if (! ((Optional<?>) item).isPresent()) {
					description.appendText("was not present");
					return;
				}
			}

		};
	}

	public static <T> Matcher<MatrixMap<T>> mmHasEntry(Matcher<Optional<T>> valueMatcher, Object... keys) {
		return new BaseMatcher<MatrixMap<T>>() {

			@Override
			public boolean matches(Object actual) {
				if (! (actual instanceof MatrixMap)) {
					return false;
				}
				return valueMatcher.matches( ((MatrixMap<?>) actual).getM(keys));
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("Matrix map with entry ").appendValueList("[", ", ", "]", keys)
						.appendText(" that ");
				valueMatcher.describeTo(description);

			}

			@Override
			public void describeMismatch(Object item, Description description) {
				if (! (item instanceof MatrixMap)) {
					description.appendText("was not a MatrixMap");
					return;
				}
				// TODO give better feedback if keys don't match the map
				var value = ((MatrixMap<?>) item).getM(keys);

				description.appendText("entry ").appendValueList("[", ", ", "]", keys).appendText(" ");
				valueMatcher.describeMismatch(value, description);
			}

		};

	}

	/**
	 * Match a MatrixMap if all of its values match the given matcher
	 *
	 * @param <T>
	 * @param valueMatcher
	 * @return
	 */
	public static <T> Matcher<MatrixMap<T>> mmAll(Matcher<T> valueMatcher) {
		return new TypeSafeDiagnosingMatcher<MatrixMap<T>>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("MatrixMap with all values ").appendDescriptionOf(valueMatcher);
			}

			@Override
			protected boolean matchesSafely(MatrixMap<T> item, Description mismatchDescription) {
				if (item.all(valueMatcher::matches)) {
					return true;
				}
				// TODO This could stand to be more specific
				mismatchDescription.appendText("Not all values were ").appendDescriptionOf(valueMatcher);
				return false;
			}

		};
	}

	/**
	 * Match a MatrixMap if its dimensions match the given matchers
	 *
	 * @param <T>
	 * @param <T>
	 * @param valueMatcher
	 * @return
	 */
	@SafeVarargs
	public static <T> Matcher<MatrixMap<T>> mmDimensions(Matcher<? super Set<?>>... dimensionMatchers) {
		return new TypeSafeDiagnosingMatcher<MatrixMap<T>>() {

			@Override
			public void describeTo(Description description) {
				description.appendList("MatrixMap with dimensions that ", ", ", "", Arrays.asList(dimensionMatchers));
			}

			@Override
			protected boolean matchesSafely(MatrixMap<T> item, Description mismatchDescription) {
				var dimensions = item.getDimensions();
				if (dimensionMatchers.length != dimensions.size()) {
					mismatchDescription.appendText("Expected ").appendValue(dimensionMatchers.length)
							.appendText(" dimensions but had ").appendValue(dimensions.size());
					return false;
				}
				var result = true;
				for (int i = 0; i < dimensionMatchers.length; i++) {
					if (!dimensionMatchers[i].matches(dimensions.get(i))) {
						if (!result) {
							mismatchDescription.appendText(", ");
						}
						result = false;
						mismatchDescription.appendText("dimension ").appendValue(i).appendText(" ");
						dimensionMatchers[i].describeMismatch(dimensions.get(i), mismatchDescription);
					}
				}

				return result;
			}

		};
	}

	/**
	 * Equivalent to {@link Matchers.hasEntry} with a simple equality check on the
	 * key. Does not show the full map contents on a mismatch, just the requested
	 * entry if it's present.
	 */
	public static <K, V> Matcher<Map<K, V>> hasSpecificEntry(K key, Matcher<V> valueMatcher) {
		return new TypeSafeDiagnosingMatcher<Map<K, V>>() {

			@Override
			protected boolean matchesSafely(Map<K, V> map, Description mismatchDescription) {
				V result = map.get(key);
				if (Objects.isNull(result)) {
					mismatchDescription.appendText("entry for ").appendValue(key).appendText(" was not present");
					return false;
				}
				if (!valueMatcher.matches(result)) {
					mismatchDescription.appendText("entry for ").appendValue(key).appendText(" was present but ");
					valueMatcher.describeMismatch(result, mismatchDescription);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("A map with an entry for ").appendValue(key).appendText(" that ")
						.appendDescriptionOf(valueMatcher);
			}

		};

	}

	/**
	 * Matches a BecLookup that contains a bec with the specified alias that matches
	 * the given matcher.
	 */
	public static Matcher<BecLookup>
			hasBec(String alias, Matcher<Optional<BecDefinition>> valueMatcher, Substitution sub) {
		return new TypeSafeDiagnosingMatcher<BecLookup>() {

			@Override
			protected boolean matchesSafely(BecLookup map, Description mismatchDescription) {
				var result = map.get(alias, sub);
				if (Objects.isNull(result)) {
					mismatchDescription.appendText("entry for ").appendValue(alias).appendText(" was not present");
					return false;
				}
				if (!valueMatcher.matches(result)) {
					mismatchDescription.appendText("entry for ").appendValue(alias).appendText(" was present but ");
					valueMatcher.describeMismatch(result, mismatchDescription);
					return false;
				}

				return true;
			}

			@Override
			public void describeTo(Description description) {
				description.appendText("A BEC Lookup with an entry for ").appendValue(alias).appendText(" that ")
						.appendDescriptionOf(valueMatcher);
			}

		};

	}

	/**
	 * Matches a BecLookup that contains a bec with the specified alias that matches
	 * the given matcher.
	 */
	public static Matcher<BecLookup> hasBec(String alias, Matcher<Optional<BecDefinition>> valueMatcher) {
		return hasBec(alias, valueMatcher, Substitution.PARTIAL_FILL_OK);
	}

}
