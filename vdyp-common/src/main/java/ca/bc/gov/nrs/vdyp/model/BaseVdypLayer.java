package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class BaseVdypLayer<S extends BaseVdypSpecies, I extends BaseVdypSite> {

	private final String polygonIdentifier;
	private final LayerType layer;
	private LinkedHashMap<String, S> species = new LinkedHashMap<>();
	private LinkedHashMap<String, I> sites = new LinkedHashMap<>();
	private Optional<Integer> inventoryTypeGroup = Optional.empty();

	protected BaseVdypLayer(String polygonIdentifier, LayerType layer, Optional<Integer> inventoryTypeGroup) {
		super();
		this.polygonIdentifier = polygonIdentifier;
		this.layer = layer;

		this.inventoryTypeGroup = inventoryTypeGroup;
	}

	public String getPolygonIdentifier() {
		return polygonIdentifier;
	}

	public LayerType getLayer() {
		return layer;
	}

	public LinkedHashMap<String, S> getSpecies() {
		return species;
	}

	public void setSpecies(Map<String, S> species) {
		this.species.clear();
		this.species.putAll(species);
	}

	public void setSpecies(Collection<S> species) {
		this.species.clear();
		species.forEach(spec -> this.species.put(spec.getGenus(), spec));
	}

	public LinkedHashMap<String, I> getSites() {
		return sites;
	}

	public void setSites(Map<String, I> sites) {
		this.sites.clear();
		this.sites.putAll(sites);
	}

	public void setSites(Collection<I> sites) {
		this.sites.clear();
		sites.forEach(spec -> this.sites.put(spec.getSiteGenus(), spec));
	}

	public Optional<Integer> getInventoryTypeGroup() {
		return inventoryTypeGroup;
	}

	public void setInventoryTypeGroup(Optional<Integer> inventoryTypeGroup) {
		this.inventoryTypeGroup = inventoryTypeGroup;
	}

	public abstract static class Builder<T extends BaseVdypLayer<S, I>, S extends BaseVdypSpecies, I extends BaseVdypSite, SB extends BaseVdypSpecies.Builder<S>, IB extends BaseVdypSite.Builder<I>>
			extends ModelClassBuilder<T> {
		protected Optional<String> polygonIdentifier = Optional.empty();
		protected Optional<LayerType> layer = Optional.empty();

		protected Optional<Integer> inventoryTypeGroup = Optional.empty();

		protected List<S> species = new LinkedList<>();
		protected List<Consumer<SB>> speciesBuilders = new LinkedList<>();
		protected List<Consumer<IB>> siteBuilders = new LinkedList<>();

		public Builder<T, S, I, SB, IB> polygonIdentifier(String polygonIdentifier) {
			this.polygonIdentifier = Optional.of(polygonIdentifier);
			return this;
		}

		public Builder<T, S, I, SB, IB> layerType(LayerType layer) {
			this.layer = Optional.of(layer);
			return this;
		}

		public Optional<LayerType> getLayerType() {
			return layer;
		}

		public Builder<T, S, I, SB, IB> addSpecies(Consumer<SB> config) {
			speciesBuilders.add(config);
			return this;
		}

		public Builder<T, S, I, SB, IB> addSpecies(Collection<S> species) {
			this.species.addAll(species);
			return this;
		}

		public Builder<T, S, I, SB, IB> addSite(Consumer<IB> config) {
			siteBuilders.add(config);
			return this;
		}

		public Builder<T, S, I, SB, IB> inventoryTypeGroup(int inventoryTypeGroup) {
			return this.inventoryTypeGroup(Optional.of(inventoryTypeGroup));
		}

		public Builder<T, S, I, SB, IB> inventoryTypeGroup(Optional<Integer> inventoryTypeGroup) {
			this.inventoryTypeGroup = inventoryTypeGroup;
			return this;
		}

		public Builder<T, S, I, SB, IB> copy(BaseVdypLayer<?, ?> toCopy) {
			polygonIdentifier(toCopy.getPolygonIdentifier());
			layerType(toCopy.getLayer());
			inventoryTypeGroup(toCopy.getInventoryTypeGroup());
			return this;
		}

		@Override
		protected void check(Collection<String> errors) {
			requirePresent(polygonIdentifier, "polygonIdentifier", errors);
			requirePresent(layer, "layer", errors);
		}

		protected abstract S buildSpecies(Consumer<SB> config);

		protected abstract I buildSite(Consumer<IB> config);

		@Override
		protected void postProcess(T result) {
			super.postProcess(result);
			var species = this.species;
			speciesBuilders.stream().map(this::buildSpecies).collect(Collectors.toCollection(() -> species));
			var sites = siteBuilders.stream().map(this::buildSite).toList();
			result.setSpecies(species);
			result.setSites(sites);
		}

	}

}
