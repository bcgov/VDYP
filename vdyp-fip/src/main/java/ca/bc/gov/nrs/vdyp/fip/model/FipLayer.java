package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Computed;
import ca.bc.gov.nrs.vdyp.model.BaseVdypLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;

public class FipLayer extends BaseVdypLayer<FipSpecies> {

	private float crownClosure; // FIPL_1/CC_L1 or FIP:_V/CC_V1
	private String siteSpecies; // FIPL_1A/SITESP64_L1 or FIPL_VA/SITESP64_L1

	public FipLayer(
			String polygonIdentifier, LayerType layer, Optional<Float> ageTotal, Optional<Float> height,
			Optional<Float> yearsToBreastHeight, Optional<Float> siteIndex, Optional<Integer> siteCurveNumber,
			Optional<Integer> inventoryTypeGroup, Optional<String> siteGenus, float crownClosure, String siteSpecies
	) {
		super(
				polygonIdentifier, layer, ageTotal, height, yearsToBreastHeight, siteIndex, siteCurveNumber,
				inventoryTypeGroup, siteGenus
		);
		this.crownClosure = crownClosure;
		this.siteSpecies = siteSpecies;
	}

	public float getCrownClosure() {
		return crownClosure;
	}

	public String getSiteSpecies() {
		return siteSpecies;
	}

	public void setCrownClosure(float crownClosure) {
		this.crownClosure = crownClosure;
	}

	public void setSiteSpecies(String siteSp64) {
		this.siteSpecies = siteSp64;
	}

	@Computed
	public float getAgeTotalSafe() {
		return super.getAgeTotal().orElseThrow(() -> new IllegalStateException());
	}

	@Computed
	public float getHeightSafe() {
		return super.getHeight().orElseThrow(() -> new IllegalStateException());
	}

	@Computed
	public float getYearsToBreastHeightSafe() {
		return super.getYearsToBreastHeight().orElseThrow(() -> new IllegalStateException());
	}

	@Computed
	public void setAgeTotalSafe(float ageTotal) {
		super.setAgeTotal(Optional.of(ageTotal));
	}

	@Computed
	public void setHeightSafe(float height) {
		super.setHeight(Optional.of(height));
	}

	@Computed
	public void setYearsToBreastHeightSafe(float yearsToBreastHeight) {
		super.setYearsToBreastHeight(Optional.of(yearsToBreastHeight));
	}

	@Override
	public void setAgeTotal(Optional<Float> ageTotal) {
		if (ageTotal.isEmpty()) {
			throw new IllegalArgumentException("ageTotal must not be empty");
		}
		super.setAgeTotal(ageTotal);
	}

	@Override
	public void setHeight(Optional<Float> height) {
		if (height.isEmpty()) {
			throw new IllegalArgumentException("height must not be empty");
		}
		super.setHeight(height);
	}

	@Override
	public void setYearsToBreastHeight(Optional<Float> yearsToBreastHeight) {
		if (yearsToBreastHeight.isEmpty()) {
			throw new IllegalArgumentException("yearsToBreastHeight must not be empty");
		}
		super.setYearsToBreastHeight(yearsToBreastHeight);
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * FipLayer myLayer = FipLayer.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.VETERAN);
			builder.ageTotal(8f);
			builder.yearsToBreastHeight(7f);
			builder.height(6f);

			builder.siteIndex(5f);
			builder.crownClosure(0.9f);
			builder.siteGenus("B");
			builder.siteSpecies("B");
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static FipLayer build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static FipLayer build(FipPolygon polygon, Consumer<Builder> config) {
		var layer = build(builder -> {
			builder.polygonIdentifier(polygon.getPolygonIdentifier());
			config.accept(builder);
		});
		polygon.getLayers().put(layer.getLayer(), layer);
		return layer;
	}

	public static class Builder extends BaseVdypLayer.Builder<FipLayer, FipSpecies> {
		protected Optional<Float> crownClosure = Optional.empty();
		protected Optional<String> siteSpecies = Optional.empty();

		public Builder crownClosure(float crownClosure) {
			this.crownClosure = Optional.of(crownClosure);
			return this;
		}

		public Builder siteSpecies(String siteSpecies) {
			this.siteSpecies = Optional.of(siteSpecies);
			return this;
		}

		@Override
		protected void check(Collection<String> errors) {
			super.check(errors);
			requirePresent(crownClosure, "crownClosure", errors);
			requirePresent(siteSpecies, "siteSpecies", errors);
		}

		@Override
		protected FipLayer doBuild() {
			/*
			 * public FipLayer( String polygonIdentifier, LayerType layer, Optional<Float> ageTotal, Optional<Float>
			 * height, Optional<Float> yearsToBreastHeight, Optional<Float> siteIndex, Optional<Integer>
			 * siteCurveNumber, Optional<Integer> inventoryTypeGroup, Optional<String> siteGenus, float crownClosure,
			 * String siteSpecies
			 */
			return (new FipLayer(
					polygonIdentifier.get(), //
					layer.get(), //
					ageTotal, //
					height, //
					yearsToBreastHeight, //
					siteIndex, //
					siteCurveNumber, //
					inventoryTypeGroup, //
					siteGenus, //
					crownClosure.get(), //
					siteSpecies.get()
			));
		}

	}
}
