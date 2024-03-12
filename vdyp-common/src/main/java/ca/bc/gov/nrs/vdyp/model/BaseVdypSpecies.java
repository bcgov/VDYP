package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.Computed;

public abstract class BaseVdypSpecies {
	private final String polygonIdentifier; // FIP_P/POLYDESC
	private final LayerType layer; // This is also represents the distinction between data stored in
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

	protected BaseVdypSpecies(String polygonIdentifier, LayerType layer, String genus, float percentGenus) {
		this.polygonIdentifier = polygonIdentifier;
		this.layer = layer;
		this.genus = genus;
		this.setPercentGenus(percentGenus);
	}

	protected BaseVdypSpecies(BaseVdypSpecies toCopy) {
		this(
				toCopy.getPolygonIdentifier(), //
				toCopy.getLayer(), //
				toCopy.getGenus(), //
				toCopy.getPercentGenus()
		);
		setSpeciesPercent(toCopy.getSpeciesPercent());
	}

	public String getPolygonIdentifier() {
		return polygonIdentifier;
	}

	public LayerType getLayer() {
		return layer;
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

	public abstract static class Builder<T extends BaseVdypSpecies> extends ModelClassBuilder<T> {
		protected Optional<String> polygonIdentifier = Optional.empty();
		protected Optional<LayerType> layer = Optional.empty();
		protected Optional<String> genus = Optional.empty();
		protected Optional<Float> percentGenus = Optional.empty();
		protected Map<String, Float> speciesPercent = new LinkedHashMap<>();

		public Builder<T> polygonIdentifier(String polygonIdentifier) {
			this.polygonIdentifier = Optional.of(polygonIdentifier);
			return this;
		}

		public Builder<T> layerType(LayerType layer) {
			this.layer = Optional.of(layer);
			return this;
		}

		public Builder<T> genus(String genus) {
			this.genus = Optional.of(genus);
			return this;
		}

		public Builder<T> percentGenus(float percentGenus) {
			this.percentGenus = Optional.of(percentGenus);
			return this;
		}

		public Builder<T> addSpecies(String id, float percent) {
			this.speciesPercent.put(id, percent);
			return this;
		}

		public Builder<T> addSpecies(Map<String, Float> toAdd) {
			this.speciesPercent.putAll(toAdd);
			return this;
		}

		public Builder<T> copy(BaseVdypSpecies toCopy) {
			polygonIdentifier(toCopy.getPolygonIdentifier());
			layerType(toCopy.getLayer());
			genus(toCopy.getGenus());
			percentGenus(toCopy.getPercentGenus());
			return this;
		}

		@Override
		protected void check(Collection<String> errors) {
			requirePresent(polygonIdentifier, "polygonIdentifier", errors);
			requirePresent(layer, "layer", errors);
			requirePresent(genus, "genus", errors);
			requirePresent(percentGenus, "percentGenus", errors);
		}

		@Override
		protected void postProcess(T result) {
			super.postProcess(result);
			result.setSpeciesPercent(speciesPercent);
		}

	}

}
