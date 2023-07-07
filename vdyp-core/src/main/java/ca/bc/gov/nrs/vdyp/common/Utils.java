package ca.bc.gov.nrs.vdyp.common;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

public class Utils {

	/**
	 * Returns a singleton set containing the value if it's not null, otherwise an
	 * empty set
	 *
	 * @param <T>
	 * @param value
	 * @return
	 */
	public static <T> Set<T> singletonOrEmpty(@Nullable T value) {
		if (Objects.isNull(value)) {
			return Collections.emptySet();
		}
		return Collections.singleton(value);
	}

}
