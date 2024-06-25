package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class BaseVdypLayer<S extends BaseVdypSpecies, I extends BaseVdypSite> {

	private final PolygonIdentifier polygonIdentifier;
	private final LayerType layerType;
	private LinkedHashMap<String, S> species = new LinkedHashMap<>();
	private LinkedHashMap<String, I> sites = new LinkedHashMap<>();
	private Optional<Integer> inventoryTypeGroup = Optional.empty();

	protected BaseVdypLayer(
			PolygonIdentifier polygonIdentifier, LayerType layerType, Optional<Integer> inventoryTypeGroup
	) {
		super();
		this.polygonIdentifier = polygonIdentifier;
		this.layerType = layerType;

		this.inventoryTypeGroup = inventoryTypeGroup;
	}

	public PolygonIdentifier getPolygonIdentifier() {
		return polygonIdentifier;
	}

	public LayerType getLayerType() {
		return layerType;
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
		protected Optional<PolygonIdentifier> polygonIdentifier = Optional.empty();
		protected Optional<LayerType> layerType = Optional.empty();

		protected Optional<Integer> inventoryTypeGroup = Optional.empty();

		protected List<S> species = new LinkedList<>();
		protected List<I> sites = new LinkedList<>();
		protected List<Consumer<SB>> speciesBuilders = new LinkedList<>();
		protected List<Consumer<IB>> siteBuilders = new LinkedList<>();

		public Builder<T, S, I, SB, IB> polygonIdentifier(PolygonIdentifier polygonIdentifier) {
			this.polygonIdentifier = Optional.of(polygonIdentifier);
			return this;
		}

		public Builder<T, S, I, SB, IB> polygonIdentifier(String polygonIdentifier) {
			this.polygonIdentifier = Optional.of(PolygonIdentifier.split(polygonIdentifier));
			return this;
		}

		public Builder<T, S, I, SB, IB> polygonIdentifier(String base, int year) {
			this.polygonIdentifier = Optional.of(new PolygonIdentifier(base, year));
			return this;
		}

		public Builder<T, S, I, SB, IB> layerType(LayerType layer) {
			this.layerType = Optional.of(layer);
			return this;
		}

		public Optional<LayerType> getLayerType() {
			return layerType;
		}

		public Builder<T, S, I, SB, IB> addSpecies(Consumer<SB> config) {
			speciesBuilders.add(config);
			return this;
		}

		public Builder<T, S, I, SB, IB> addSpecies(Collection<S> species) {
			this.species.addAll(species);
			return this;
		}

		public Builder<T, S, I, SB, IB> addSpecies(S species) {
			this.species.add(species);
			return this;
		}

		public Builder<T, S, I, SB, IB> addSite(Consumer<IB> config) {
			siteBuilders.add(config);
			return this;
		}

		public Builder<T, S, I, SB, IB> addSite(I site) {
			this.sites.add(site);
			return this;
		}

		public Builder<T, S, I, SB, IB> inventoryTypeGroup(int inventoryTypeGroup) {
			return this.inventoryTypeGroup(Optional.of(inventoryTypeGroup));
		}

		public Builder<T, S, I, SB, IB> inventoryTypeGroup(Optional<Integer> inventoryTypeGroup) {
			this.inventoryTypeGroup = inventoryTypeGroup;
			return this;
		}

		public Builder<T, S, I, SB, IB> adapt(BaseVdypLayer<?, ?> toCopy) {
			polygonIdentifier(toCopy.getPolygonIdentifier());
			layerType(toCopy.getLayerType());
			inventoryTypeGroup(toCopy.getInventoryTypeGroup());
			return this;
		}

		public Builder<T, S, I, SB, IB> copy(T toCopy) {
			adapt(toCopy);
			return this;
		}

		public <I2 extends BaseVdypSite> Builder<T, S, I, SB, IB>
				adaptSites(BaseVdypLayer<?, I2> toCopy, BiConsumer<IB, I2> config) {
			toCopy.getSites().values().forEach(siteToCopy -> {
				this.addSite(builder -> {
					builder.adapt(siteToCopy);
					builder.polygonIdentifier = Optional.empty();
					builder.layerType = Optional.empty();
					config.accept(builder, siteToCopy);
				});
			});
			return this;
		}

		public Builder<T, S, I, SB, IB> copySites(T toCopy, BiConsumer<IB, I> config) {
			toCopy.getSites().values().forEach(siteToCopy -> {
				this.addSite(builder -> {
					builder.copy(siteToCopy);
					builder.polygonIdentifier = Optional.empty();
					builder.layerType = Optional.empty();
					config.accept(builder, siteToCopy);
				});
			});
			return this;
		}

		public <S2 extends BaseVdypSpecies> Builder<T, S, I, SB, IB>
				adaptSpecies(BaseVdypLayer<S2, ?> toCopy, BiConsumer<SB, S2> config) {
			toCopy.getSpecies().values().forEach(speciesToCopy -> {
				this.addSpecies(builder -> {
					builder.adapt(speciesToCopy);
					builder.polygonIdentifier = Optional.empty();
					builder.layerType = Optional.empty();
					config.accept(builder, speciesToCopy);
				});
			});
			return this;
		}

		public Builder<T, S, I, SB, IB> copySpecies(T toCopy, BiConsumer<SB, S> config) {
			toCopy.getSpecies().values().forEach(speciesToCopy -> {
				this.addSpecies(builder -> {
					builder.copy(speciesToCopy);
					builder.polygonIdentifier = Optional.empty();
					builder.layerType = Optional.empty();
					config.accept(builder, speciesToCopy);
				});
			});
			return this;
		}

		public List<S> getSpecies() {
			if (!speciesBuilders.isEmpty()) {
				throw new IllegalStateException("Tried to get species when there are unbuilt species builders");
			}
			return Collections.unmodifiableList(species);
		}

		public List<I> getSites() {
			if (!siteBuilders.isEmpty()) {
				throw new IllegalStateException("Tried to get sites when there are unbuilt site builders");
			}
			return Collections.unmodifiableList(sites);
		}

		@Override
		protected void check(Collection<String> errors) {
			requirePresent(polygonIdentifier, "polygonIdentifier", errors);
			requirePresent(layerType, "layerType", errors);
		}

		protected abstract S buildSpecies(Consumer<SB> config);

		protected abstract I buildSite(Consumer<IB> config);

		/**
		 * Build any builders for child objects and store the results. This will clear the stored child builders.
		 */
		public void buildChildren() {
			speciesBuilders.stream().map(this::buildSpecies).collect(Collectors.toCollection(() -> species));
			speciesBuilders.clear();
			siteBuilders.stream().map(this::buildSite).collect(Collectors.toCollection(() -> sites));
			siteBuilders.clear();
		}

		@Override
		protected void postProcess(T result) {
			super.postProcess(result);
			buildChildren();
			result.setSpecies(species);
			result.setSites(sites);
		}

	}

}
