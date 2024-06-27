package ca.bc.gov.nrs.vdyp.forward;

import java.util.Optional;

public record SpeciesRankingDetails(
		int primarySpeciesIndex, Optional<Integer> secondarySpeciesIndex, int inventoryTypeGroup
) {
}