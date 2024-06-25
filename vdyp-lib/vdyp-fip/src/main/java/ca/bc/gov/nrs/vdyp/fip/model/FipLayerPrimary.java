package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Computed;
import ca.bc.gov.nrs.vdyp.model.LayerType;
import ca.bc.gov.nrs.vdyp.model.PolygonIdentifier;

public class FipLayerPrimary extends FipLayer {

	private Optional<Character> stockingClass; // FIPL_1ST/STK_L1

	private Optional<String> primaryGenus; // FIPL_1C/JPRIME

	public FipLayerPrimary(
			PolygonIdentifier polygonIdentifier, Optional<Integer> inventoryTypeGroup, float crownClosure,
			Optional<Character> stockingClass, Optional<String> primaryGenus
	) {
		super(polygonIdentifier, LayerType.PRIMARY, inventoryTypeGroup, crownClosure);
		this.stockingClass = stockingClass;
		this.primaryGenus = primaryGenus;

	}

	public Optional<Character> getStockingClass() {
		return stockingClass;
	}

	public void setStockingClass(Optional<Character> stockingClass) {
		this.stockingClass = stockingClass;
	}

	public Optional<String> getPrimaryGenus() {
		return primaryGenus;
	}

	public void setPrimaryGenus(Optional<String> primaryGenus) {
		this.primaryGenus = primaryGenus;
	}

	@Computed
	public Optional<FipSpecies> getPrimarySpeciesRecord() {
		return primaryGenus.map(this.getSpecies()::get);
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * FipLayerPrimary myLayer = FipLayerPrimary.buildPrimary(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.ageTotal(8f);
			builder.yearsToBreastHeight(7f);
			builder.height(6f);

	@Override
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
	public static FipLayerPrimary buildPrimary(Consumer<PrimaryBuilder> config) {
		var builder = new PrimaryBuilder();
		config.accept(builder);
		return (FipLayerPrimary) builder.build();
	}

	public static FipLayerPrimary buildPrimary(FipPolygon polygon, Consumer<PrimaryBuilder> config) {
		var layer = buildPrimary(builder -> {
			builder.polygonIdentifier(polygon.getPolygonIdentifier());
			config.accept(builder);
		});
		polygon.getLayers().put(layer.getLayerType(), layer);
		return layer;
	}

	public static class PrimaryBuilder extends Builder {
		protected Optional<Character> stockingClass = Optional.empty();

		protected Optional<String> primaryGenus = Optional.empty();

		public Builder stockingClass(Optional<Character> stockingClass) {
			this.stockingClass = stockingClass;
			return this;
		}

		public Builder primaryGenus(Optional<String> primaryGenus) {
			this.primaryGenus = primaryGenus;
			return this;
		}

		public Builder stockingClass(char stockingClass) {
			return stockingClass(Optional.of(stockingClass));
		}

		public Builder primaryGenus(String primaryGenus) {
			return primaryGenus(Optional.of(primaryGenus));
		}

		public PrimaryBuilder() {
			super();
			this.layerType(LayerType.PRIMARY);
		}

		@Override
		protected FipLayerPrimary doBuild() {
			return new FipLayerPrimary(
					polygonIdentifier.get(), //
					inventoryTypeGroup, //
					crownClosure.get(), //
					stockingClass, //
					primaryGenus
			);
		}

	}
}
