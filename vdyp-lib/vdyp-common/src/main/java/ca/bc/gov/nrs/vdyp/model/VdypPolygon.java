package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class VdypPolygon extends BaseVdypPolygon<VdypLayer, Float, VdypSpecies, VdypSite> {

	public VdypPolygon(
			PolygonIdentifier polygonIdentifier, Float percentAvailable, String fiz, String becIdentifier,
			Optional<PolygonMode> modeFip
	) {
		super(polygonIdentifier, percentAvailable, fiz, becIdentifier, modeFip);
	}

	/**
	 * Copy constructs from the simple attributes of another polygon, but does not copy layers.
	 *
	 * @param <O>                     Type of the polygon to copy
	 * @param <U>                     Type of percent available in the other polygon
	 * @param toCopy                  The polygon to copy
	 * @param convertPercentAvailable Function to convert
	 */
	public <O extends BaseVdypPolygon<?, U, ?, ?>, U> VdypPolygon(
			O toCopy, Function<U, Float> convertPercentAvailable
	) {
		super(toCopy, convertPercentAvailable);
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * VdypPolygon myPolygon = VdypPolygon.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.percentAvailable(percentAvailable);
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static VdypPolygon build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static class Builder extends
			BaseVdypPolygon.Builder<VdypPolygon, VdypLayer, Float, VdypSpecies, VdypSite, VdypLayer.Builder, VdypSpecies.Builder, VdypSite.Builder> {

		@Override
		protected VdypPolygon doBuild() {
			return new VdypPolygon(
					polygonIdentifier.get(), percentAvailable.get(), forestInventoryZone.get(),
					biogeoclimaticZone.get(), mode
			);
		}

		@Override
		protected VdypLayer.Builder getLayerBuilder() {
			return new VdypLayer.Builder();
		}

	}
}
