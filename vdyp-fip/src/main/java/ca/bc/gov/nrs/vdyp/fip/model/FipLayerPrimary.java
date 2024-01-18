package ca.bc.gov.nrs.vdyp.fip.model;

import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Computed;
import ca.bc.gov.nrs.vdyp.model.LayerType;

public class FipLayerPrimary extends FipLayer {

	static final String SITE_CURVE_NUMBER = "SITE_CURVE_NUMBER"; // SCN
	static final String STOCKING_CLASS = "STOCKING_CLASS"; // STK

	// TODO Confirm if these should be required instead of optional if we know it's
	// a Primary layer.
	private Optional<Integer> siteCurveNumber = Optional.empty(); // FIPL_1/SCN_L1

	private Optional<Character> stockingClass = Optional.empty(); // FIPL_1ST/STK_L1

	private Optional<String> primaryGenus = Optional.empty(); // FIPL_1C/JPRIME

	private Optional<Integer> inventoryTypeGroup = Optional.empty();

	public FipLayerPrimary(
			String polygonIdentifier, float ageTotal, float yearsToBreastHeight, float height, float siteIndex,
			float crownClosure, String siteGenus, String siteSpecies
	) {
		super(
				polygonIdentifier, LayerType.PRIMARY, ageTotal, yearsToBreastHeight, height, siteIndex, crownClosure,
				siteGenus, siteSpecies
		);
	}

	public Optional<Integer> getSiteCurveNumber() {
		return siteCurveNumber;
	}

	public void setSiteCurveNumber(Optional<Integer> siteCurveNumber) {
		this.siteCurveNumber = siteCurveNumber;
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

	public Optional<Integer> getInventoryTypeGroup() {
		return inventoryTypeGroup;
	}

	public void setInventoryTypeGroup(Optional<Integer> inventoryTypeGroup) {
		this.inventoryTypeGroup = inventoryTypeGroup;
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

			builder.siteIndex(5f);
			builder.crownClosure(0.9f);
			builder.siteGenus("B");
			builder.siteSpecies("B");
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by
	 *                               the configuration function.
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
		polygon.getLayers().put(layer.getLayer(), layer);
		return layer;
	}

	public static class PrimaryBuilder extends Builder {
		private Optional<Integer> siteCurveNumber = Optional.empty();

		private Optional<Character> stockingClass = Optional.empty();

		private Optional<String> primaryGenus = Optional.empty();

		private Optional<Integer> inventoryTypeGroup = Optional.empty();

		public Builder siteCurveNumber(Optional<Integer> siteCurveNumber) {
			this.siteCurveNumber = siteCurveNumber;
			return this;
		}

		public Builder stockingClass(Optional<Character> stockingClass) {
			this.stockingClass = stockingClass;
			return this;
		}

		public Builder primaryGenus(Optional<String> primaryGenus) {
			this.primaryGenus = primaryGenus;
			return this;
		}

		public Builder inventoryTypeGroup(Optional<Integer> inventoryTypeGroup) {
			this.inventoryTypeGroup = inventoryTypeGroup;
			return this;
		}

		public Builder siteCurveNumber(int siteCurveNumber) {
			return siteCurveNumber(Optional.of(siteCurveNumber));
		}

		public Builder stockingClass(char stockingClass) {
			return stockingClass(Optional.of(stockingClass));
		}

		public Builder primaryGenus(String primaryGenus) {
			return primaryGenus(Optional.of(primaryGenus));
		}

		public Builder inventoryTypeGroup(int inventoryTypeGroup) {
			return inventoryTypeGroup(Optional.of(inventoryTypeGroup));
		}

		public PrimaryBuilder() {
			super();
			this.layerType(LayerType.PRIMARY);
		}

		@Override
		protected FipLayerPrimary doBuild() {
			var result = new FipLayerPrimary(
					polygonIdentifier.get(), //
					ageTotal.get(), //
					yearsToBreastHeight.get(), //
					height.get(), //
					siteIndex.get(), //
					crownClosure.get(), //
					siteGenus.get(), //
					siteSpecies.get()
			);
			result.setSiteCurveNumber(this.siteCurveNumber);
			result.setStockingClass(this.stockingClass);
			result.setPrimaryGenus(this.primaryGenus);
			result.setInventoryTypeGroup(this.inventoryTypeGroup);
			return result;
		}

	}
}
