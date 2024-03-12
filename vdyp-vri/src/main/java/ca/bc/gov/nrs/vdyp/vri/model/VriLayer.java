package ca.bc.gov.nrs.vdyp.vri.model;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.model.BaseVdypLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;

public class VriLayer extends BaseVdypLayer<VriSpecies, VriSite> {

	private final float crownClosure; // VRIL/CCL
	private final Optional<Float> baseArea; // VRIL/BAL
	private final Optional<Float> treesPerHectare; // VRIL/TPHL
	private final float utilization; // VRIL/UTLL

	public VriLayer(
			String polygonIdentifier, LayerType layer, float crownClosure, Optional<Float> baseArea,
			Optional<Float> treesPerHectare, float utilization
	) {
		super(polygonIdentifier, layer, Optional.empty());
		this.crownClosure = crownClosure;
		this.baseArea = baseArea;
		this.treesPerHectare = treesPerHectare;
		this.utilization = utilization;
	}

	public float getCrownClosure() {
		return crownClosure;
	}

	public Optional<Float> getBaseArea() {
		return baseArea;
	}

	public Optional<Float> getTreesPerHectare() {
		return treesPerHectare;
	}

	public float getUtilization() {
		return utilization;
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */

	public static VriLayer build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	public static VriLayer build(VriPolygon polygon, Consumer<Builder> config) {
		var layer = build(builder -> {
			builder.polygonIdentifier(polygon.getPolygonIdentifier());
			config.accept(builder);
		});
		polygon.getLayers().put(layer.getLayer(), layer);
		return layer;
	}

	public static class Builder
			extends BaseVdypLayer.Builder<VriLayer, VriSpecies, VriSite, VriSpecies.Builder, VriSite.Builder> {
		protected Optional<Float> crownClosure = Optional.empty();
		protected Optional<Float> baseArea = Optional.empty();
		protected Optional<Float> treesPerHectare = Optional.empty();
		protected Optional<Float> utilization = Optional.empty();
		protected Optional<Float> percentAvailable = Optional.empty();

		public Builder crownClosure(float crownClosure) {
			this.crownClosure = Optional.of(crownClosure);
			return this;
		}

		public Builder baseArea(Optional<Float> baseArea) {
			this.baseArea = baseArea;
			return this;
		}

		public Builder treesPerHectare(Optional<Float> treesPerHectare) {
			this.treesPerHectare = treesPerHectare;
			return this;
		}

		public Builder baseArea(float baseArea) {
			return baseArea(Optional.of(baseArea));
		}

		public Builder treesPerHectare(float treesPerHectare) {
			return treesPerHectare(Optional.of(treesPerHectare));
		}

		public Builder utilization(float utilization) {
			this.utilization = Optional.of(utilization);
			return this;
		}

		public Builder percentAvailable(Optional<Float> percentAvailable) {
			this.percentAvailable = percentAvailable;
			return this;
		}

		public Builder percentAvailable(float percentAvailable) {
			return percentAvailable(Optional.of(percentAvailable));
		}

		@Override
		protected void check(Collection<String> errors) {
			super.check(errors);
			requirePresent(crownClosure, "crownClosure", errors);
			requirePresent(utilization, "utilization", errors);
		}

		@Override
		protected VriLayer doBuild() {
			float multiplier = percentAvailable.orElse(100f) / 100f;
			return (new VriLayer(
					polygonIdentifier.get(), //
					layer.get(), //
					crownClosure.get(), //
					baseArea.map(x -> x * multiplier), //
					treesPerHectare.map(x -> x * multiplier), //
					Math.max(utilization.get(), 7.5f)
			));
		}

		@Override
		protected VriSpecies buildSpecies(Consumer<VriSpecies.Builder> config) {
			return VriSpecies.build(builder -> {
				builder.polygonIdentifier(polygonIdentifier.get());
				builder.layerType(layer.get());
			});
		}

		@Override
		protected VriSite buildSite(Consumer<VriSite.Builder> config) {
			return VriSite.build(builder -> {
				builder.polygonIdentifier(polygonIdentifier.get());
				builder.layerType(layer.get());
			});
		}

	}

}
