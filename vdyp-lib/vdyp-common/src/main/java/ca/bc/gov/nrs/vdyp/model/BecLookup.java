package ca.bc.gov.nrs.vdyp.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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

	private final Map<String, BecDefinition> becMap;

	/**
	 * Empty index value
	 */
	public static final int EMPTY_INDEX = 0;

	/**
	 * Get a BEC definition using its alias.
	 *
	 * @param alias Alias to look up
	 * @return
	 */
	public Optional<BecDefinition> get(String alias) {
		var bec = becMap.get(alias);
		if (Objects.isNull(bec)) {
			return Optional.empty();
		}
		return Optional.of(bec);

	}

	/**
	 * Get all BECs
	 *
	 * @return
	 */
	public Collection<BecDefinition> getBecs() {
		return this.becMap.values();
	}

	/**
	 * Get all growth BECs
	 *
	 * @return
	 */
	public Collection<BecDefinition> getGrowthBecs() {
		return this.becMap.values().stream().filter(BecDefinition::isGrowth).toList();
	}

	/**
	 * Get all becs for a Region
	 *
	 * @param region Region to search for
	 * @return
	 */
	public Collection<BecDefinition> getBecsForRegion(Region region) {
		return this.becMap.values().stream().filter(bec -> bec.getRegion() == region).toList();
	}

	/**
	 * Find a set of BECs for the given scope. If the scope is blank, that's all BECs, if it's a Region alias, it's all
	 * BECs for that region, otherwise its treated as a BEC alias and the BEC matching it is returned.
	 *
	 * @param scope The scope to match
	 * @return A collection of matching BECs, or the empty set if none match.
	 *
	 */
	public Collection<BecDefinition> getBecsForScope(String scope) {
		if (scope.isBlank()) {
			return this.getBecs();
		}
		return Region.fromAlias(scope).map(region -> this.getBecsForRegion(region))
				.orElseGet(() -> this.get(scope).map(Collections::singletonList).orElseGet(Collections::emptyList));
	}

	/**
	 * Get all the aliases for defined BECs
	 *
	 * @param control Control map containing the parsed BEC definitions.
	 * @return
	 */
	public Collection<String> getBecAliases() {
		return getBecs().stream().map(BecDefinition::getAlias).toList();
	}

}
