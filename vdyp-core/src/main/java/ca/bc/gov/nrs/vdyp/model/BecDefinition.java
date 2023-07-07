package ca.bc.gov.nrs.vdyp.model;

public class BecDefinition extends AbstractSpeciesDefinition {

	final Region region;

	final int growthIndex;
	final int volumeIndex;
	final int decayIndex;

	public BecDefinition(String alias, Region region, String name, int growthIndex, int volumeIndex, int decayIndex) {
		super(alias, name);
		this.region = region;
		this.growthIndex = growthIndex;
		this.volumeIndex = volumeIndex;
		this.decayIndex = decayIndex;
	}

	public Region getRegion() {
		return region;
	}

	public int getGrowthIndex() {
		return growthIndex;
	}

	public int getVolumeIndex() {
		return volumeIndex;
	}

	public int getDecayIndex() {
		return decayIndex;
	}

}
