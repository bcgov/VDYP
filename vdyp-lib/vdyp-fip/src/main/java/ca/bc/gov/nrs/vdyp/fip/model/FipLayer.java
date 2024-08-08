package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Computed;
import ca.bc.gov.nrs.vdyp.model.BaseVdypLayer;
import ca.bc.gov.nrs.vdyp.model.InputLayer;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;
import ca.bc.gov.nrs.vdyp.model.SingleSiteLayer;

public class FipLayer extends SingleSiteLayer<FipSpecies, FipSite> implements InputLayer {

	private float crownClosure; // FIPL_1/CC_L1 or FIP:_V/CC_V1

	private Optional<FipSite> siteWithoutSpecies = Optional.empty();

	public FipLayer(
			PolygonIdentifier polygonIdentifier, LayerType layer, Optional<Integer> inventoryTypeGroup,
			float crownClosure, Optional<FipSite> siteWithoutSpecies
	) {
		super(polygonIdentifier, layer, inventoryTypeGroup);
		this.crownClosure = crownClosure;
		this.siteWithoutSpecies = siteWithoutSpecies;
	}

	@Override
	public float getCrownClosure() {
		return crownClosure;
	}

	public void setCrownClosure(float crownClosure) {
		this.crownClosure = crownClosure;
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

	public Optional<String> getSiteSpecies() {
		return getSite().flatMap(FipSite::getSiteSpecies);
	}

	@Override
	public Optional<FipSite> getSite() {
		return siteWithoutSpecies.or(() -> super.getSite());
	}

	// This is a bit of a hack. The Layer holds on to a site until populated with Species at which point the site is
	// attached to the correct Species.
	private void applySiteWithoutSpecies() {
		siteWithoutSpecies.ifPresent(site -> {

			var spec = this.getSpecies().get(site.getSiteGenus());
			if (spec != null) {
				this.getSpecies().put(site.getSiteGenus(), FipSpecies.build(sb -> {
					sb.copy(spec);
					sb.addSite(siteWithoutSpecies);
				}));
				siteWithoutSpecies = Optional.empty();
			}

		});

	}

	@Override
	public void setSpecies(Map<String, FipSpecies> species) {
		super.setSpecies(species);
		applySiteWithoutSpecies();
	}

	@Override
	public void setSpecies(Collection<FipSpecies> species) {
		super.setSpecies(species);
		applySiteWithoutSpecies();
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
		polygon.getLayers().put(layer.getLayerType(), layer);
		return layer;
	}

	public static class Builder
			extends BaseVdypLayer.Builder<FipLayer, FipSpecies, FipSite, FipSpecies.Builder, FipSite.Builder> {
		protected Optional<Float> crownClosure = Optional.empty();

		public Builder crownClosure(float crownClosure) {
			this.crownClosure = Optional.of(crownClosure);
			return this;
		}

		protected Optional<FipSite> siteWithoutSpecies = Optional.empty();
		protected Optional<Consumer<FipSite.Builder>> siteWithoutSpeciesBuilder = Optional.empty();

		public Builder addSiteWithoutSpecies(Optional<FipSite> siteWithoutSpecies) {
			this.siteWithoutSpecies = siteWithoutSpecies;
			return this;
		}

		public Builder addSiteWithoutSpecies(FipSite siteWithoutSpecies) {
			return this.addSiteWithoutSpecies(Optional.of(siteWithoutSpecies));
		}

		public Builder addSiteWithoutSpecies(Consumer<FipSite.Builder> config) {
			this.siteWithoutSpeciesBuilder = Optional.of(config);
			return this;
		}

		@Override
		protected void check(Collection<String> errors) {
			super.check(errors);
			requirePresent(crownClosure, "crownClosure", errors);
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
					layerType.get(), //
					inventoryTypeGroup, //
					crownClosure.get(), //
					siteWithoutSpecies
			));
		}

		@Override
		protected FipSpecies buildSpecies(Consumer<FipSpecies.Builder> config) {
			return FipSpecies.build(builder -> {
				config.accept(builder);
				builder.polygonIdentifier(this.polygonIdentifier.get());
				builder.layerType(layerType.get());
			});
		}

		@Override
		protected void preProcess() {
			super.preProcess();
			this.siteWithoutSpecies = siteWithoutSpecies
					.or(() -> siteWithoutSpeciesBuilder.map(config -> FipSite.build(builder -> {
						config.accept(builder);
						builder.polygonIdentifier(this.polygonIdentifier.get());
						builder.layerType(layerType.get());
					})));
		}

	}
}
