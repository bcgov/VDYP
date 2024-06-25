package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;

import ca.bc.gov.nrs.vdyp.common.Computed;

public class BecDefinition extends AliasedEntity {

	private final Region region;

	private final Optional<BecDefinition> growthBec;
	private final Optional<BecDefinition> volumeBec;
	private final Optional<BecDefinition> decayBec;

	public BecDefinition(String alias, Region region, String name) {
		this(alias, region, name, Optional.empty(), Optional.empty(), Optional.empty());
	}

	public BecDefinition(
			String alias, Region region, String name, Optional<BecDefinition> growthBec,
			Optional<BecDefinition> volumeBec, Optional<BecDefinition> decayBec
	) {
		super(alias, name);
		this.region = region;
		this.growthBec = growthBec;
		this.volumeBec = volumeBec;
		this.decayBec = decayBec;
	}

	public BecDefinition(
			BecDefinition baseBec, BecDefinition defaultBec, boolean isGrowth, boolean isVolume, boolean isDecay
	) {
		this(
				baseBec.getAlias(), baseBec.region, baseBec.getName(),
				isGrowth ? baseBec.growthBec : Optional.of(defaultBec),
				isVolume ? baseBec.volumeBec : Optional.of(defaultBec),
				isDecay ? baseBec.decayBec : Optional.of(defaultBec)
		);
	}

	public Region getRegion() {
		return region;
	}

	/**
	 * Get the BEC to use in a Growth context. Will return this BEC if it is a growth BEC otherwise one that is.
	 */
	public BecDefinition getGrowthBec() {
		var result = growthBec.orElse(this);
		assert result.isGrowth();
		assert this.isGrowth() ^ (this != result);
		return result;
	}

	/**
	 * Get the BEC to use in a Volume context. Will return this BEC if it is a volume BEC otherwise one that is.
	 */
	public BecDefinition getVolumeBec() {
		var result = volumeBec.orElse(this);
		assert result.isVolume();
		assert this.isVolume() ^ (this != result);
		return result;
	}

	/**
	 * Get the BEC to use in a Decay context. Will return this BEC if it is a decay BEC otherwise one that is.
	 */
	public BecDefinition getDecayBec() {
		var result = decayBec.orElse(this);
		assert result.isDecay();
		assert this.isDecay() ^ (this != result);
		return result;
	}

	/**
	 * Is this BEC suitable for use in a growth context.
	 */
	@Computed
	public boolean isGrowth() {
		return growthBec.isEmpty();
	}

	/**
	 * Is this BEC suitable for use in a volume context.
	 */
	@Computed
	public boolean isVolume() {
		return volumeBec.isEmpty();
	}

	/**
	 * Is this BEC suitable for use in a decay context.
	 */
	@Computed
	public boolean isDecay() {
		return decayBec.isEmpty();
	}
}
