package ca.bc.gov.nrs.vdyp.common;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Type union of a value and an out of band marker.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 * @param <Value>
 * @param <Marker>
 */
public class ValueOrMarker<Value, Marker> {
	private final boolean isMarker;
	private final Object obj;

	private ValueOrMarker(Object obj, boolean isMarker) {
		Objects.requireNonNull(obj, "The Value or Marker of a ValueOrMarker must not be null");

		this.isMarker = isMarker;
		this.obj = obj;
	}

	/**
	 * Is there a value
	 *
	 * @return
	 */
	public boolean isMarker() {
		return isMarker;
	}

	/**
	 * Is there a marker
	 *
	 * @return
	 */
	public boolean isValue() {
		return !isMarker;
	}

	/**
	 * Get the value if present
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Optional<Value> getValue() {
		if (isValue()) {
			return (Optional<Value>) Optional.of(obj);
		}
		return Optional.empty();
	}

	/**
	 * Get the marker if present
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Optional<Marker> getMarker() {
		if (isMarker()) {
			return (Optional<Marker>) Optional.of(obj);
		}
		return Optional.empty();
	}

	/**
	 * Apply the value handler if a value, otherwise the marker handler, and return
	 * the result
	 */
	@SuppressWarnings("unchecked")
	public <T> T handle(Function<Value, T> valueHandler, Function<Marker, T> markerHandler) {
		if (isValue()) {
			return valueHandler.apply((Value) obj);
		}
		return markerHandler.apply((Marker) obj);
	}

	/**
	 * Create a Builder to create ValueOrMarker instances for particular Value and
	 * Marker types
	 *
	 * @param vClazz Value class
	 * @param mClazz Marker class
	 * @return
	 */
	public static <Value, Marker> Builder<Value, Marker> builder(Class<Value> vClazz, Class<Marker> mClazz) {
		return new Builder<Value, Marker>();
	}

	/**
	 * Builder to create ValueOrMarker instances for particular Value and Marker
	 * types
	 *
	 * @author Kevin Smith, Vivid Solutions
	 *
	 * @param <Value>  Value class
	 * @param <Marker> Marker class
	 */
	public static class Builder<Value, Marker> {

		public Builder() {

		}

		/**
		 * Create a ValueOrMarker with a Marker
		 */
		public ValueOrMarker<Value, Marker> marker(Marker m) {
			return new ValueOrMarker<Value, Marker>(m, true);
		}

		/**
		 * Create a ValueOrMarker with a Value
		 */
		public ValueOrMarker<Value, Marker> value(Value v) {
			return new ValueOrMarker<Value, Marker>(v, false);
		}
	}
}
