package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

public class VdypSpecies extends BaseVdypSpecies implements VdypUtilizationHolder {

	private Coefficients baseAreaByUtilization = new Coefficients(Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1); // LVCOM/BA
	private Coefficients loreyHeightByUtilization = new Coefficients(Arrays.asList(0f, 0f), -1); // LVCOM/HL
	private Coefficients quadraticMeanDiameterByUtilization = new Coefficients(
			Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1
	); // LVCOM/DQ
	private Coefficients treesPerHectareByUtilization = new Coefficients(Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1); // LVCOM/TPH

	private Coefficients wholeStemVolumeByUtilization = new Coefficients(Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1); // LVCOM/VOLWS
	private Coefficients closeUtilizationVolumeByUtilization = new Coefficients(
			Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1
	); // LVCOM/VOLCU
	private Coefficients closeUtilizationNetVolumeOfDecayByUtilization = new Coefficients(
			Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1
	); // LVCOM/VOL_D
	private Coefficients closeUtilizationVolumeNetOfDecayAndWasteByUtilization = new Coefficients(
			Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1
	); // LVCOM/VOL_DW
	private Coefficients closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization = new Coefficients(
			Arrays.asList(0f, 0f, 0f, 0f, 0f, 0f), -1
	); // LVCOM/VOL_DWB

	int volumeGroup;
	int decayGroup;
	int breakageGroup;

	public VdypSpecies(
			PolygonIdentifier polygonIdentifier, LayerType layer, String genus, float percentGenus, //
			int volumeGroup, int decayGroup, int breakageGroup
	) {
		super(polygonIdentifier, layer, genus, percentGenus);
		this.volumeGroup = volumeGroup;
		this.decayGroup = decayGroup;
		this.breakageGroup = breakageGroup;

	}

	public VdypSpecies(BaseVdypSpecies toCopy) {
		super(toCopy);
	}

	/**
	 * Base area for utilization index -1 through 4
	 */
	@Override
	public Coefficients getBaseAreaByUtilization() {
		return baseAreaByUtilization;
	}

	/**
	 * Base area for utilization index -1 through 4
	 */
	@Override
	public void setBaseAreaByUtilization(Coefficients baseAreaByUtilization) {
		this.baseAreaByUtilization = baseAreaByUtilization;
	}

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	@Override
	public Coefficients getLoreyHeightByUtilization() {
		return loreyHeightByUtilization;
	}

	/**
	 * Lorey height for utilization index -1 through 0
	 */
	@Override
	public void setLoreyHeightByUtilization(Coefficients loreyHeightByUtilization) {
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
	public Coefficients getQuadraticMeanDiameterByUtilization() {
		return quadraticMeanDiameterByUtilization;
	}

	@Override
	public void setQuadraticMeanDiameterByUtilization(Coefficients quadraticMeanDiameterByUtilization) {
		this.quadraticMeanDiameterByUtilization = quadraticMeanDiameterByUtilization;
	}

	@Override
	public Coefficients getTreesPerHectareByUtilization() {
		return treesPerHectareByUtilization;
	}

	@Override
	public void setTreesPerHectareByUtilization(Coefficients treesPerHectareByUtilization) {
		this.treesPerHectareByUtilization = treesPerHectareByUtilization;
	}

	@Override
	public Coefficients getWholeStemVolumeByUtilization() {
		return wholeStemVolumeByUtilization;
	}

	@Override
	public void setWholeStemVolumeByUtilization(Coefficients wholeStemVolumeByUtilization) {
		this.wholeStemVolumeByUtilization = wholeStemVolumeByUtilization;
	}

	@Override
	public Coefficients getCloseUtilizationVolumeByUtilization() {
		return closeUtilizationVolumeByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeByUtilization(Coefficients closeUtilizationVolumeByUtilization) {
		this.closeUtilizationVolumeByUtilization = closeUtilizationVolumeByUtilization;
	}

	@Override
	public Coefficients getCloseUtilizationVolumeNetOfDecayByUtilization() {
		return closeUtilizationNetVolumeOfDecayByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayByUtilization(
			Coefficients closeUtilizationNetVolumeOfDecayByUtilization
	) {
		this.closeUtilizationNetVolumeOfDecayByUtilization = closeUtilizationNetVolumeOfDecayByUtilization;
	}

	@Override
	public Coefficients getCloseUtilizationVolumeNetOfDecayAndWasteByUtilization() {
		return closeUtilizationVolumeNetOfDecayAndWasteByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayAndWasteByUtilization(
			Coefficients closeUtilizationVolumeNetOfDecayAndWasteByUtilization
	) {
		this.closeUtilizationVolumeNetOfDecayAndWasteByUtilization = closeUtilizationVolumeNetOfDecayAndWasteByUtilization;
	}

	@Override
	public Coefficients getCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization() {
		return closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization;
	}

	@Override
	public void setCloseUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization(
			Coefficients closeUtilizationVolumeNetOfDecayWasteAndBreakageByUtilization
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

	public static class Builder extends BaseVdypSpecies.Builder<VdypSpecies> {
		protected Optional<Integer> volumeGroup = Optional.empty();
		protected Optional<Integer> decayGroup = Optional.empty();
		protected Optional<Integer> breakageGroup = Optional.empty();

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
		protected VdypSpecies doBuild() {
			return new VdypSpecies(
					polygonIdentifier.get(), //
					layerType.get(), //
					genus.get(), //
					percentGenus.get(), //
					volumeGroup.get(), //
					decayGroup.get(), //
					breakageGroup.get() //
			);
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
