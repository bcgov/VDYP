package ca.bc.gov.nrs.vdyp.vri.model;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.model.BaseVdypPolygon;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.PolygonMode;

public class VriPolygon extends BaseVdypPolygon<VriLayer, Optional<Float>, VriSpecies, VriSite> {

	private Optional<String> nonproductiveDescription; // FIP_P3/NPDESC
	private float yieldFactor; // FIP_P4/YLDFACT
	public static final String FIZ = " ";

	public VriPolygon(
			PolygonIdentifier polygonIdentifier, String fiz, String becIdentifier, Optional<Float> percentAvailable,
			Optional<PolygonMode> modeFip, Optional<String> nonproductiveDescription, float yieldFactor
	) {
		super(polygonIdentifier, percentAvailable, fiz, becIdentifier, modeFip);
		this.nonproductiveDescription = nonproductiveDescription;
		this.yieldFactor = yieldFactor;
	}

	public Optional<String> getNonproductiveDescription() {
		return nonproductiveDescription;
	}

	public float getYieldFactor() {
		return yieldFactor;
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * VdypPolygon myPolygon = VdypPolygon.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.percentAvailable(percentAvailable);
			builder.forestInventoryZone
			builder.biogeoclimaticZone
			builder.
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static VriPolygon build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static class Builder extends
			BaseVdypPolygon.Builder<VriPolygon, VriLayer, Optional<Float>, VriSpecies, VriSite, VriLayer.Builder, VriSpecies.Builder, VriSite.Builder> {
		protected Optional<String> nonproductiveDescription = Optional.empty();
		protected Optional<Float> yieldFactor = Optional.empty();

		public Builder() {
			this.forestInventoryZone(FIZ); // VRI FIZ is always " "
			this.percentAvailable = Optional.of(Optional.empty());
		}

		public Builder percentAvailable(float percentAvailable) {
			return (Builder) percentAvailable(Optional.of(percentAvailable));
		}

		public Builder nonproductiveDescription(Optional<String> nonproductiveDescription) {
			this.nonproductiveDescription = nonproductiveDescription;
			return this;
		}

		public Builder nonproductiveDescription(String nonproductiveDescription) {
			nonproductiveDescription(Optional.of(nonproductiveDescription));
			return this;
		}

		public Builder yieldFactor(Float yieldFactor) {
			this.yieldFactor = Optional.of(yieldFactor);
			return this;
		}

		@Override
		protected void check(Collection<String> errors) {
			super.check(errors);
			requirePresent(biogeoclimaticZone, "biogeoclimaticZone", errors);
			requirePresent(yieldFactor, "yieldFactor", errors);
		}

		@Override
		public Builder copy(VriPolygon toCopy) {
			super.copy(toCopy);
			yieldFactor(toCopy.getYieldFactor());
			nonproductiveDescription(toCopy.getNonproductiveDescription());
			return this;
		}

		@Override
		protected VriPolygon doBuild() {
			return (new VriPolygon(
					polygonIdentifier.get(), //
					FIZ, // FIZ is always " "
					biogeoclimaticZone.get(), //
					percentAvailable.get(), //
					mode, //
					nonproductiveDescription, //
					yieldFactor.get() //
			));
		}

		@Override
		protected VriLayer.Builder getLayerBuilder() {
			return new VriLayer.Builder();
		}

	}

}
