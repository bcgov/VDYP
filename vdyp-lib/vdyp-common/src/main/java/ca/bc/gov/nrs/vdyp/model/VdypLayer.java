package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Computed;
import ca.bc.gov.nrs.vdyp.common.Utils;

public class VdypLayer extends SingleSiteLayer<VdypSpecies, VdypSite> implements VdypUtilizationHolder {

	private UtilizationVector baseAreaByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/BA species 0
	private UtilizationVector loreyHeightByUtilization = //
			VdypUtilizationHolder.emptyLoreyHeightUtilization(); // LVCOM/HL species 0
	private UtilizationVector quadraticMeanDiameterByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/DQ species 0
	private UtilizationVector treesPerHectareByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/TPH species 0

	private UtilizationVector wholeStemVolumeByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/VOLWS species 0
	private UtilizationVector closeUtilizationVolumeByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/VOLCU species/ 0
	private UtilizationVector closeUtilizationVolumeNetOfDecayByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/VOL_D species 0
	private UtilizationVector closeUtilizationVolumeNetOfDecayAndWasteByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/VOL_DW species 0
	private UtilizationVector closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization = //
			VdypUtilizationHolder.emptyUtilization(); // LVCOM/VOL_DWB species 0

	private Optional<Integer> empericalRelationshipParameterIndex = Optional.empty(); // INXL1/GRPBA1

	public VdypLayer(
			PolygonIdentifier polygonIdentifier, LayerType layer, Optional<Integer> inventoryTypeGroup,
			Optional<Integer> empericalRelationshipParameterIndex
	) {
		super(polygonIdentifier, layer, inventoryTypeGroup);
		this.empericalRelationshipParameterIndex = empericalRelationshipParameterIndex;
	}

	@Computed
	public Optional<Float> getBreastHeightAge() {
		return this.getAgeTotal().flatMap(at -> this.getYearsToBreastHeight().map(bha -> at - bha));
	}

	@Override
	public UtilizationVector getBaseAreaByUtilization() {
		return baseAreaByUtilization;
	}

	@Override
	public void setBaseAreaByUtilization(UtilizationVector baseAreaByUtilization) {
		this.baseAreaByUtilization = baseAreaByUtilization;
	}

	@Override
	public UtilizationVector getLoreyHeightByUtilization() {
		return loreyHeightByUtilization;
	}

	@Override
	public void setLoreyHeightByUtilization(UtilizationVector loreyHeightByUtilization) {
		this.loreyHeightByUtilization = loreyHeightByUtilization;
	}

	@Override
	public UtilizationVector getQuadraticMeanDiameterByUtilization() {
		return quadraticMeanDiameterByUtilization;
	}

	@Override
	public void setQuadraticMeanDiameterByUtilization(UtilizationVector quadraticMeanDiameterByUtilization) {
		this.quadraticMeanDiameterByUtilization = quadraticMeanDiameterByUtilization;
	}

	@Override
	public UtilizationVector getTreesPerHectareByUtilization() {
		return treesPerHectareByUtilization;
	}

	@Override
	public void setTreesPerHectareByUtilization(UtilizationVector treesPerHectareByUtilization) {
		this.treesPerHectareByUtilization = treesPerHectareByUtilization;
	}

	@Override
	public UtilizationVector getWholeStemVolumeByUtilization() {
		return wholeStemVolumeByUtilization;
	}

	@Override
	public void setWholeStemVolumeByUtilization(UtilizationVector wholeStemVolumeByUtilization) {
		this.wholeStemVolumeByUtilization = wholeStemVolumeByUtilization;
	}

	@Override
	public UtilizationVector getCloseUtilizationVolumeByUtilization() {
		return closeUtilizationVolumeByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeByUtilization(UtilizationVector closeUtilizationVolumeByUtilization) {
		this.closeUtilizationVolumeByUtilization = closeUtilizationVolumeByUtilization;
	}

	@Override
	public UtilizationVector getCloseUtilizationVolumeNetOfDecayByUtilization() {
		return closeUtilizationVolumeNetOfDecayByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayByUtilization(
			UtilizationVector closeUtilizationNetVolumeOfDecayByUtilization
	) {
		this.closeUtilizationVolumeNetOfDecayByUtilization = closeUtilizationNetVolumeOfDecayByUtilization;
	}

	@Override
	public UtilizationVector getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization() {
		return closeUtilizationVolumeNetOfDecayAndWasteByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
			UtilizationVector closeUtilizationVolumeNetOfDecayAndWasteByUtilization
	) {
		this.closeUtilizationVolumeNetOfDecayAndWasteByUtilization = closeUtilizationVolumeNetOfDecayAndWasteByUtilization;
	}

	@Override
	public UtilizationVector getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization() {
		return closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
			UtilizationVector closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization
	) {
		this.closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization = closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization;
	}

	public Optional<Integer> getEmpiricalRelationshipParameterIndex() {
		return empericalRelationshipParameterIndex;
	}

	public void setEmpericalRelationshipParameterIndex(Optional<Integer> empericalRelationshipParameterIndex) {
		this.empericalRelationshipParameterIndex = empericalRelationshipParameterIndex;
	}

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * VdypLayer myLayer = VdypLayer.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.VETERAN);

			builder.crownClosure(0.9f);
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static VdypLayer build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	/**
	 * Builds a layer and adds it to the polygon.
	 *
	 * @param layerType Layer to create the species for.
	 * @param config    Configuration function for the builder.
	 * @return the new species.
	 */
	public static VdypLayer build(VdypPolygon polygon, Consumer<Builder> config) {
		var result = build(builder -> {
			builder.polygonIdentifier(polygon.getPolygonIdentifier());

			config.accept(builder);
		});
		polygon.getLayers().put(result.getLayerType(), result);
		return result;
	}

	public static class Builder
			extends BaseVdypLayer.Builder<VdypLayer, VdypSpecies, VdypSite, VdypSpecies.Builder, VdypSite.Builder> {

		Optional<Integer> empericalRelationshipParameterIndex = Optional.empty();

		public void empiricalRelationshipParameterIndex(Optional<Integer> empiricalRelationshipParameterIndex) {
			this.empericalRelationshipParameterIndex = empiricalRelationshipParameterIndex;
		}

		public void empiricalRelationshipParameterIndex(int empiricalRelationshipParameterIndex) {
			this.empiricalRelationshipParameterIndex(Optional.of(empiricalRelationshipParameterIndex));
		}

		UtilizationVector loreyHeight = VdypUtilizationHolder.emptyLoreyHeightUtilization();

		public void loreyHeight(float height) {
			this.loreyHeight = Utils.heightVector(0, height);
		}

		public void loreyHeight(float small, float height) {
			this.loreyHeight = Utils.heightVector(small, height);
		}

		protected UtilizationVector baseArea = VdypUtilizationHolder.emptyUtilization();

		public void baseArea(float small, float u1, float u2, float u3, float u4) {
			this.baseArea = Utils.utilizationVector(small, u1, u2, u3, u4);
		}

		public void baseArea(float height) {
			this.baseArea = Utils.utilizationVector(height);
		}

		protected UtilizationVector treesPerHectare = VdypUtilizationHolder.emptyUtilization();

		public void treesPerHectare(float small, float u1, float u2, float u3, float u4) {
			this.treesPerHectare = Utils.utilizationVector(small, u1, u2, u3, u4);
		}

		public void treesPerHectare(float height) {
			this.treesPerHectare = Utils.utilizationVector(height);
		}

		protected UtilizationVector quadMeanDiameter = VdypUtilizationHolder.emptyUtilization();

		public void quadMeanDiameter(float small, float u1, float u2, float u3, float u4) {
			this.quadMeanDiameter = Utils.utilizationVector(small, u1, u2, u3, u4);
		}

		public void quadMeanDiameter(float height) {
			this.quadMeanDiameter = Utils.utilizationVector(height);
		}

		protected UtilizationVector wholeStemVolume = VdypUtilizationHolder.emptyUtilization();

		public void wholeStemVolume(float small, float u1, float u2, float u3, float u4) {
			this.wholeStemVolume = Utils.utilizationVector(small, u1, u2, u3, u4);
		}

		public void wholeStemVolume(float volume) {
			this.wholeStemVolume = Utils.utilizationVector(volume);
		}

		@Override
		protected void postProcess(VdypLayer layer) {
			super.postProcess(layer);
			layer.setLoreyHeightByUtilization(loreyHeight);
			layer.setBaseAreaByUtilization(baseArea);
			layer.setTreesPerHectareByUtilization(treesPerHectare);
			layer.setQuadraticMeanDiameterByUtilization(quadMeanDiameter);
			layer.setWholeStemVolumeByUtilization(wholeStemVolume);
		}

		@Override
		protected VdypLayer doBuild() {
			return new VdypLayer(
					polygonIdentifier.get(), //
					layerType.get(), //
					inventoryTypeGroup, //
					empericalRelationshipParameterIndex
			);
		}

		@Override
		protected VdypSpecies buildSpecies(Consumer<VdypSpecies.Builder> config) {
			return VdypSpecies.build(builder -> {
				config.accept(builder);
				builder.polygonIdentifier(this.polygonIdentifier.get());
				builder.layerType(this.layerType.get());
			});
		}

		public void baseAreaByUtilization(UtilizationVector utilizationVector) {
			// TODO Auto-generated method stub

		}

	}
}
