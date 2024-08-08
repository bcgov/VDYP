package ca.bc.gov.nrs.vdyp.model;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Computed;

public abstract class BaseVdypSpecies<I extends BaseVdypSite> {
	private final PolygonIdentifier polygonIdentifier; // FIP_P/POLYDESC
	private final LayerType layerType; // This is also represents the distinction between data stored in
	// FIPL_1(A) and FIP_V(A). Where VDYP7 stores both and looks at certain values
	// to determine if a layer is "present". VDYP8 stores them in a map keyed by
	// this value

	private final String genus; // FIPSA/SP0V

	private float percentGenus; // FIPS/PCTVOLV L1COM1/PCTL1

	// This is computed from percentGenus, but VDYP7 computes it in a way that might
	// lead to a slight difference so it's stored separately and can be modified.
	@Computed
	private float fractionGenus; // FRBASP0/FR

	private Map<String, Float> speciesPercent; // Map from
	private Optional<I> site;

	protected BaseVdypSpecies(
			PolygonIdentifier polygonIdentifier, LayerType layerType, String genus, float percentGenus, Optional<I> site
	) {
		this.polygonIdentifier = polygonIdentifier;
		this.layerType = layerType;
		this.genus = genus;
		this.setPercentGenus(percentGenus);
		this.site = site;
	}

	public PolygonIdentifier getPolygonIdentifier() {
		return polygonIdentifier;
	}

	public LayerType getLayerType() {
		return layerType;
	}

	public float getPercentGenus() {
		return percentGenus;
	}

	@Computed
	public float getFractionGenus() {
		return fractionGenus;
	}

	public void setPercentGenus(float percentGenus) {
		this.percentGenus = percentGenus;
		this.fractionGenus = percentGenus / 100f;
	}

	@Computed
	public void setFractionGenus(float fractionGenus) {
		this.fractionGenus = fractionGenus;
	}

	public Map<String, Float> getSpeciesPercent() {
		return speciesPercent;
	}

	public void setSpeciesPercent(Map<String, Float> speciesPercent) {
		this.speciesPercent = speciesPercent;
	}

	public String getGenus() {
		return genus;
	}

	public Optional<I> getSite() {
		return site;
	}

	public abstract static class Builder<T extends BaseVdypSpecies<I>, I extends BaseVdypSite, IB extends BaseVdypSite.Builder<I>>
			extends ModelClassBuilder<T> {
		protected Optional<PolygonIdentifier> polygonIdentifier = Optional.empty();
		protected Optional<LayerType> layerType = Optional.empty();
		protected Optional<String> genus = Optional.empty();
		protected Optional<Float> percentGenus = Optional.empty();
		protected Optional<Float> fractionGenus = Optional.empty();
		protected Map<String, Float> speciesPercent = new LinkedHashMap<>();
		protected Optional<Consumer<IB>> siteBuilder = Optional.empty();
		protected Optional<I> site = Optional.empty();

		public Builder<T, I, IB> polygonIdentifier(PolygonIdentifier polygonIdentifier) {
			this.polygonIdentifier = Optional.of(polygonIdentifier);
			return this;
		}

		public Builder<T, I, IB> polygonIdentifier(String polygonIdentifier) {
			this.polygonIdentifier = Optional.of(PolygonIdentifier.split(polygonIdentifier));
			return this;
		}

		public Builder<T, I, IB> polygonIdentifier(String base, int year) {
			this.polygonIdentifier = Optional.of(new PolygonIdentifier(base, year));
			return this;
		}

		public Builder<T, I, IB> layerType(LayerType layer) {
			this.layerType = Optional.of(layer);
			return this;
		}

		public Builder<T, I, IB> genus(String genus) {
			this.genus = Optional.of(genus);
			return this;
		}

		public Builder<T, I, IB> percentGenus(float percentGenus) {
			this.percentGenus = Optional.of(percentGenus);
			return this;
		}

		protected Builder<T, I, IB> fractionGenus(float fractionGenus) {
			this.fractionGenus = Optional.of(fractionGenus);
			return this;
		}

		public Builder<T, I, IB> addSpecies(String id, float percent) {
			this.speciesPercent.put(id, percent);
			return this;
		}

		public Builder<T, I, IB> addSpecies(Map<String, Float> toAdd) {
			this.speciesPercent.putAll(toAdd);
			return this;
		}

		public Builder<T, I, IB> copy(T toCopy) {
			return this.adapt(toCopy);
		}

		public Builder<T, I, IB> addSite(Consumer<IB> config) {
			this.siteBuilder = Optional.of(config);
			this.site = Optional.empty();
			return this;
		}

		public Builder<T, I, IB> addSite(I site) {
			addSite(Optional.of(site));
			return this;
		}

		public Builder<T, I, IB> addSite(Optional<I> site) {
			this.site = site;
			this.siteBuilder = Optional.empty();
			return this;
		}

		@Override
		protected void check(Collection<String> errors) {
			requirePresent(polygonIdentifier, "polygonIdentifier", errors);
			requirePresent(layerType, "layerType", errors);
			requirePresent(genus, "genus", errors);
			requirePresent(percentGenus, "percentGenus", errors);
		}

		@Override
		protected void postProcess(T result) {
			super.postProcess(result);
			result.setSpeciesPercent(speciesPercent);
			this.fractionGenus.ifPresent(result::setFractionGenus);
		}

		@Override
		protected void preProcess() {
			super.preProcess();
			site = siteBuilder.map(this::buildSite).or(() -> site);
		}

		@Override
		protected String getBuilderId() {
			return MessageFormat.format(
					"Species {0} {1} {2}", //
					polygonIdentifier.map(Object::toString).orElse("N/A"), //
					layerType.map(Object::toString).orElse("N/A"), //
					genus.map(Object::toString).orElse("N/A")//
			);
		}

		public Builder<T, I, IB> adapt(BaseVdypSpecies<?> toCopy) {
			polygonIdentifier(toCopy.getPolygonIdentifier());
			layerType(toCopy.getLayerType());
			genus(toCopy.getGenus());
			percentGenus(toCopy.getPercentGenus());

			fractionGenus(toCopy.getFractionGenus());

			for (var entry : toCopy.getSpeciesPercent().entrySet()) {
				addSpecies(entry.getKey(), entry.getValue());
			}

			return this;
		}

		public <I2 extends BaseVdypSite> Builder<T, I, IB> adaptSite(I2 toCopy, BiConsumer<IB, I2> config) {
			this.addSite(builder -> {
				builder.adapt(toCopy);
				builder.polygonIdentifier = Optional.empty();
				builder.layerType = Optional.empty();
				config.accept(builder, toCopy);
			});
			return this;
		}

		public <S2 extends BaseVdypSpecies<I2>, I2 extends BaseVdypSite> Builder<T, I, IB>
				adaptSiteFrom(S2 specToCopy, BiConsumer<IB, I2> config) {
			specToCopy.getSite().ifPresent(toCopy -> this.adaptSite(toCopy, config));
			return this;
		}

		public Builder<T, I, IB> copySite(I toCopy, BiConsumer<IB, I> config) {
			this.addSite(builder -> {
				builder.copy(toCopy);
				builder.siteGenus = Optional.empty();
				builder.polygonIdentifier = Optional.empty();
				builder.layerType = Optional.empty();
				config.accept(builder, toCopy);
			});
			return this;
		}

		public Builder<T, I, IB> copySiteFrom(T specToCopy, BiConsumer<IB, I> config) {
			specToCopy.getSite().ifPresent(toCopy -> this.copySite(toCopy, config));
			return this;
		}

		protected abstract I buildSite(Consumer<IB> config);
	}

}
