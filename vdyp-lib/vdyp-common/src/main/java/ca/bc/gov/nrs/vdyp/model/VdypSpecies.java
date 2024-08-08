package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

import ca.bc.gov.nrs.vdyp.common.Utils;

public class VdypSpecies extends BaseVdypSpecies<VdypSite> implements VdypUtilizationHolder {

	private UtilizationVector baseAreaByUtilization = Utils.utilizationVector(); // LVCOM/BA
	private UtilizationVector loreyHeightByUtilization = Utils.heightVector(); // LVCOM/HL
	private UtilizationVector quadraticMeanDiameterByUtilization = Utils.utilizationVector(); // LVCOM/DQ
	private UtilizationVector treesPerHectareByUtilization = Utils.utilizationVector(); // LVCOM/TPH

	private UtilizationVector wholeStemVolumeByUtilization = Utils.utilizationVector(); // LVCOM/VOLWS
	private UtilizationVector closeUtilizationVolumeByUtilization = Utils.utilizationVector(); // LVCOM/VOLCU
	private UtilizationVector closeUtilizationNetVolumeOfDecayByUtilization = Utils.utilizationVector(); // LVCOM/VOL_D
	private UtilizationVector closeUtilizationVolumeNetOfDecayAndWasteByUtilization = Utils.utilizationVector(); // LVCOM/VOL_DW
	private UtilizationVector closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization = Utils.utilizationVector(); // LVCOM/VOL_DWB

	int volumeGroup;
	int decayGroup;
	int breakageGroup;

	public VdypSpecies(
			PolygonIdentifier polygonIdentifier, LayerType layer, String genus, float percentGenus,
			Optional<VdypSite> site, int volumeGroup, int decayGroup, int breakageGroup
	) {
		super(polygonIdentifier, layer, genus, percentGenus, site);
		this.volumeGroup = volumeGroup;
		this.decayGroup = decayGroup;
		this.breakageGroup = breakageGroup;

	}

	/**
	 * Base area for utilization index -1 through 4
	 */
	@Override
	public UtilizationVector getBaseAreaByUtilization() {
		return baseAreaByUtilization;
	}

	/**
	 * Base area for utilization index -1 through 4
	 */
	@Override
	public void setBaseAreaByUtilization(UtilizationVector baseAreaByUtilization) {
		this.baseAreaByUtilization = baseAreaByUtilization;
	}

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	@Override
	public UtilizationVector getLoreyHeightByUtilization() {
		return loreyHeightByUtilization;
	}

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	@Override
	public void setLoreyHeightByUtilization(UtilizationVector loreyHeightByUtilization) {
		this.loreyHeightByUtilization = loreyHeightByUtilization;
	}

	public int getVolumeGroup() {
		return volumeGroup;
	}

	public void setVolumeGroup(int volumeGroup) {
		this.volumeGroup = volumeGroup;
	}

	public int getDecayGroup() {
		return decayGroup;
	}

	public void setDecayGroup(int decayGroup) {
		this.decayGroup = decayGroup;
	}

	public int getBreakageGroup() {
		return breakageGroup;
	}

	public void setBreakageGroup(int breakageGroup) {
		this.breakageGroup = breakageGroup;
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
		return closeUtilizationNetVolumeOfDecayByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayByUtilization(
			UtilizationVector closeUtilizationNetVolumeOfDecayByUtilization
	) {
		this.closeUtilizationNetVolumeOfDecayByUtilization = closeUtilizationNetVolumeOfDecayByUtilization;
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

	/**
	 * Accepts a configuration function that accepts a builder to configure.
	 *
	 * <pre>
	 * FipSpecies myLayer = FipSpecies.build(builder-&gt; {
			builder.polygonIdentifier(polygonId);
			builder.layerType(LayerType.VETERAN);
			builder.genus("B");
			builder.percentGenus(6f);
	 * })
	 * </pre>
	 *
	 * @param config The configuration function
	 * @return The object built by the configured builder.
	 * @throws IllegalStateException if any required properties have not been set by the configuration function.
	 */
	public static VdypSpecies build(Consumer<Builder> config) {
		var builder = new Builder();
		config.accept(builder);
		return builder.build();
	}

	/**
	 * Builds a species and adds it to the layer.
	 *
	 * @param layer  Layer to create the species for.
	 * @param config Configuration function for the builder.
	 * @return the new species.
	 */
	public static VdypSpecies build(VdypLayer layer, Consumer<Builder> config) {
		var result = build(builder -> {
			builder.polygonIdentifier(layer.getPolygonIdentifier());
			builder.layerType(layer.getLayerType());

			config.accept(builder);
		});
		layer.getSpecies().put(result.getGenus(), result);
		return result;
	}

	public static class Builder extends BaseVdypSpecies.Builder<VdypSpecies, VdypSite, VdypSite.Builder> {
		protected Optional<Integer> volumeGroup = Optional.empty();
		protected Optional<Integer> decayGroup = Optional.empty();
		protected Optional<Integer> breakageGroup = Optional.empty();

		protected UtilizationVector loreyHeight = VdypUtilizationHolder.emptyLoreyHeightUtilization();

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
		protected void check(Collection<String> errors) {
			super.check(errors);
			requirePresent(volumeGroup, "volumeGroup", errors);
			requirePresent(decayGroup, "decayGroup", errors);
			requirePresent(breakageGroup, "breakageGroup", errors);
		}

		@Override
		public Builder copy(VdypSpecies toCopy) {
			super.copy(toCopy);
			volumeGroup(toCopy.getVolumeGroup());
			decayGroup(toCopy.getDecayGroup());
			breakageGroup(toCopy.getBreakageGroup());
			return this;
		}

		@Override
		protected void postProcess(VdypSpecies spec) {
			super.postProcess(spec);
			spec.setLoreyHeightByUtilization(loreyHeight);
			spec.setBaseAreaByUtilization(baseArea);
			spec.setTreesPerHectareByUtilization(treesPerHectare);
			spec.setQuadraticMeanDiameterByUtilization(quadMeanDiameter);
			spec.setWholeStemVolumeByUtilization(wholeStemVolume);
		}

		@Override
		protected VdypSpecies doBuild() {

			return new VdypSpecies(
					polygonIdentifier.get(), //
					layerType.get(), //
					genus.get(), //
					percentGenus.get(), //
					site, volumeGroup.get(), //
					decayGroup.get(), //
					breakageGroup.get() //
			);
		}

		@Override
		protected VdypSite buildSite(Consumer<VdypSite.Builder> config) {
			return VdypSite.build(builder -> {
				config.accept(builder);
				builder.polygonIdentifier(polygonIdentifier.get());
				builder.layerType(layerType.get());
				builder.siteGenus(genus);
			});
		}

		public Builder volumeGroup(int i) {
			this.volumeGroup = Optional.of(i);
			return this;
		}

		public Builder decayGroup(int i) {
			this.decayGroup = Optional.of(i);
			return this;
		}

		public Builder breakageGroup(int i) {
			this.breakageGroup = Optional.of(i);
			return this;
		}
	}
}
