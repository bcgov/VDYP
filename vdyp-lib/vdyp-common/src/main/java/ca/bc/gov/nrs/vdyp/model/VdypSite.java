package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;
import java.util.function.Consumer;

public class VdypSite extends BaseVdypSite {

	public VdypSite(
			PolygonIdentifier polygonIdentifier, LayerType layer, String siteGenus, Optional<Integer> siteCurveNumber,
			Optional<Float> siteIndex, Optional<Float> height, Optional<Float> ageTotal,
			Optional<Float> yearsToBreastHeight
	) {
		super(polygonIdentifier, layer, siteGenus, siteCurveNumber, siteIndex, height, ageTotal, yearsToBreastHeight);
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * FipSpecies myLayer = FipSpecies.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.VETERAN);
			builder.genus("B");
			builder.percentGenus(6f);
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static VdypSite build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	/**
	 * Builds a species and adds it to the layer.
	 *
	 * @param layer  Layer to create the species for.
	 * @param config Configuration function for the builder.
	 * @return the new species.
	 */
	public static VdypSite build(VdypLayer layer, Consumer<Builder> config) {

		var result = build(builder -> {
			builder.polygonIdentifier(layer.getPolygonIdentifier());
			builder.layerType(layer.getLayerType());
			config.accept(builder);
		});
		layer.getSites().put(result.getSiteGenus(), result);
		return result;
	}

	public static class Builder extends BaseVdypSite.Builder<VdypSite> {

		@Override
		protected VdypSite doBuild() {
			return new VdypSite(
					polygonIdentifier.get(), layerType.get(), siteGenus.get(), siteCurveNumber, siteIndex, height,
					ageTotal, yearsToBreastHeight
			);
		}
	}
}
