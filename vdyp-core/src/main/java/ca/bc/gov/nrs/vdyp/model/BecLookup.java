package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Lookup table for BEC definitions
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class BecLookup {

	/**
	 * Create a bec lookup
	 *
	 * @param becMap
	 */
	public BecLookup(Collection<BecDefinition> becs) {
		this.becMap = new HashMap<>();
		for (var bec : becs) {
			becMap.put(bec.getAlias(), bec);
		}
	}

	/**
	 * How to handle substitution of empty indices
	 *
	 * @author Kevin Smith, Vivid Solutions
	 *
	 */
	public static enum Substitution {
		/**
		 * Raise an error if a requested BEC has an empty index.
		 */
		NONE,
		/**
		 * Substitute empty values for those of the default BEC.
		 */
		SUBSTITUTE,
		/**
		 * Return BECs with empty indices.
		 */
		PARTIAL_FILL_OK
	};

	private final Map<String, BecDefinition> becMap;

	/**
	 * Alias of the default BEC
	 */
	public static final String DEFAULT_BEC = "ESSF";

	/**
	 * Empty index value
	 */
	public static final int EMPTY_INDEX = 0;

	/**
	 * Get a BEC definition using its alias.
	 *
	 * @param alias Alias to look up
	 * @param sub
	 * @return
	 */
	public Optional<BecDefinition> get(String alias, Substitution sub) {
		var bec = becMap.get(alias);
		if (Objects.isNull(bec)) {
			return Optional.empty();
		}
		return Optional.of(doSubstitute(bec, sub));

	}

	static private int
			defaultIndex(BecDefinition bec, BecDefinition defaultBec, Function<BecDefinition, Integer> accessor) {
		var index = accessor.apply(bec);
		var defaultIndex = accessor.apply(defaultBec);
		return index == EMPTY_INDEX ? defaultIndex : index;
	}

	private boolean isPartialFill(BecDefinition bec) {
		return bec.getGrowthIndex() == EMPTY_INDEX || bec.getVolumeIndex() == EMPTY_INDEX
				|| bec.getDecayIndex() == EMPTY_INDEX;
	}

	private BecDefinition doSubstitute(BecDefinition bec, Substitution sub) {
		if (sub == Substitution.PARTIAL_FILL_OK) {
			return bec;
		}
		if (isPartialFill(bec)) {
			if (sub == Substitution.NONE) {
				throw new IllegalArgumentException("Substitution needed but not requested for BEC " + bec.getAlias());
			}
			var defaultBec = get(DEFAULT_BEC, Substitution.PARTIAL_FILL_OK)
					.orElseThrow(() -> new IllegalStateException("Could not find default BEC: " + DEFAULT_BEC));

			bec = new BecDefinition(
					bec.getAlias(), bec.getRegion(), bec.getName(),
					defaultIndex(bec, defaultBec, BecDefinition::getGrowthIndex),
					defaultIndex(bec, defaultBec, BecDefinition::getVolumeIndex),
					defaultIndex(bec, defaultBec, BecDefinition::getDecayIndex)
			);

			if (isPartialFill(bec)) {
				throw new IllegalStateException(
						"Could not substitute indices for BEC " + bec.getAlias()
								+ ". This is probably because the default BEC, " + DEFAULT_BEC + ", is incomplete."
				);
			}
		}
		return bec;
	}

	private boolean isGrowthBec(BecDefinition bec) {
		return bec.getGrowthIndex() != EMPTY_INDEX;
	}

	/**
	 * Get all BECs
	 *
	 * @param sub Substitution mode to apply. Should not be NONE.
	 * @return
	 */
	public Collection<BecDefinition> getBecs(Substitution sub) {
		return this.becMap.values().stream().map(bec -> this.doSubstitute(bec, sub)).toList();
	}

	/**
	 * Get all growth BECs
	 *
	 * @param sub Substitution mode to apply. Should not be NONE.
	 * @return
	 */
	public Collection<BecDefinition> getGrowthBecs(Substitution sub) {
		return this.becMap.values().stream().filter(this::isGrowthBec).map(bec -> this.doSubstitute(bec, sub)).toList();
	}

	/**
	 * Get all becs for a Region
	 *
	 * @param region Region to search for
	 * @param sub    Substitution mode to apply. Should not be NONE.
	 * @return
	 */
	public Collection<BecDefinition> getBecsForRegion(Region region, Substitution sub) {
		return this.becMap.values().stream().filter(bec -> bec.getRegion() == region)
				.map(bec -> this.doSubstitute(bec, sub)).toList();
	}

	/**
	 * Get all becs for a scope
	 *
	 * @param scope Scope to search for
	 * @param sub   Substitution mode to apply. Should not be NONE.
	 * @return
	 */
	public Collection<BecDefinition> getBecsForScope(String scope, Substitution sub) {
		if (scope.isBlank()) {
			return this.getBecs(sub);
		}
		return Region.fromAlias(scope).map(region -> this.getBecsForRegion(region, sub)).orElseGet(
				() -> this.get(scope, sub).map(Collections::singletonList).orElseGet(Collections::emptyList)
		);
	}
}
