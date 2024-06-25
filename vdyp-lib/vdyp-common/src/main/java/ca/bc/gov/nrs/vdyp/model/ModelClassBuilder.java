package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Base for support classes to make classes with complex constructors nicer to work with.
 */

public abstract class ModelClassBuilder<T> {

	/**
	 * Add an error message to the collection if the value is not present.
	 *
	 * @param holder
	 * @param name
	 * @param errors
	 */
	protected static void requirePresent(Optional<?> holder, String name, Collection<String> errors) {
		if (!holder.isPresent()) {
			errors.add(String.format("%s was not set", name));
		}
	}

	/**
	 * Check that the required properties have been set. Adds a message to the collection for each missing property.
	 * These will be joined together as the message for an exception if any are missing.
	 *
	 * @param errors Collection to add errors to if there are missing properties
	 */
	protected abstract void check(Collection<String> errors);

	/**
	 * Try to build the object
	 *
	 * @throws IllegalStateExcpetion if any required properties have not been set.
	 * @return The constructed model object
	 */
	public T build() {
		Collection<String> errors = new LinkedList<>();
		check(errors);
		if (!errors.isEmpty()) {
			throw new IllegalStateException(String.join(", ", errors));
		}
		var result = doBuild();
		postProcess(result);
		return result;
	}

	/**
	 * Run the constructor, assuming all required properties are present
	 */
	protected abstract T doBuild();

	/**
	 * Additional steps after building.
	 */
	protected void postProcess(T result) {
		// Do nothing
	};
}
