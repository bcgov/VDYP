package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.model.BaseVdypSite;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;

public class FipSite extends BaseVdypSite {

	private final Optional<String> siteSpecies; // FIPL_1A/SITESP64_L1 or FIPL_VA/SITESP64_L1

	public FipSite(
			PolygonIdentifier polygonIdentifier, LayerType layer, String siteGenus, Optional<Integer> siteCurveNumber,
			Optional<Float> siteIndex, Optional<Float> height, Optional<Float> ageTotal,
			Optional<Float> yearsToBreastHeight, Optional<String> siteSpecies
	) {
		super(polygonIdentifier, layer, siteGenus, siteCurveNumber, siteIndex, height, ageTotal, yearsToBreastHeight);
		this.siteSpecies = siteSpecies;
	}

	public Optional<String> getSiteSpecies() {
		return siteSpecies;
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static FipSite build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static class Builder extends BaseVdypSite.Builder<FipSite> {

		protected Optional<String> siteSpecies = Optional.empty();

		public Builder siteSpecies(Optional<String> siteSpecies) {
			this.siteSpecies = siteSpecies;
			return this;
		}

		public Builder siteSpecies(String string) {
			return siteSpecies(Optional.of(string));
		}

		@Override
		protected FipSite doBuild() {
			return new FipSite(
					this.polygonIdentifier.get(), //
					this.layerType.get(), //
					this.siteGenus.get(), //
					this.siteCurveNumber, //
					this.siteIndex, //
					this.height, //
					this.ageTotal, //
					this.yearsToBreastHeight, //
					this.siteSpecies //
			);
		}
	}
}
