package ca.bc.gov.nrs.vdyp.si32.vdyp;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsTreeSpecies;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SpeciesRegion;

/**
 * Records all of the information regarding a particular CSF species.
 */
public record SpeciesDetails(

		/** UNIQUE KEY. The short standard code name for the species */
		String codeName,

		/** The full common name for the species. */
		String fullName,

		/** The Latin name (if known) for the species. */
		String latinName,

		/** The species code name associated with the species as a genus. */
		String genusName,

		/** The VDYP7 SP0 code name to use with the species. */
		String sp0Name,

		/**
		 * The corresponding CFS Species for the MoF Species. The source for this mapping is found in the document
		 * <b>Documents/CFS-Biomass/BCSpcsToCFSSpcs-SAS.txt</b>
		 */
		CfsTreeSpecies cfsSpecies,

		/**
		 * <ul>
		 * <li>true: indicates the species is a commercial species.
		 * <li>false: indicates the species is not a commercial species.
		 * </ul>
		 */
		boolean isCommercial,

		/**
		 * <ul>
		 * <li>true: indicates the species is a deciduous species.
		 * <li>false: indicates the species is a coniferous species.
		 * </ul>
		 */
		boolean isDeciduous,

		/**
		 * <ul>
		 * <li>true: indicates the species is a softwood species.
		 * <li>false: indicates the species is a hardwood species.
		 * </ul>
		 */
		boolean isSoftwood,

		/**
		 * Contains the default Crown Closure for a species in each of the {@link SpeciesRegion} regions of the
		 * province, given by SpeciesRegion.ordinal().
		 */
		float[] defaultCrownClosure,

		/**
		 * For each {@link SpeciesRegion}, identifies the SINDEX curve number currently assigned to the species. In
		 * certain applications, the standard curve number for a species may be modified to other experimental or
		 * alternative curves rather than the standard curve.
		 */
		SiteIndexEquation[] currentSICurve
) {
	@Override
	public String toString() {
		return MessageFormat.format("code name: {0}; full name: {1}, sp0 name: {2}", codeName, fullName, sp0Name);
	}

	@Override
	public int hashCode() {
		// codeName is the unique key for this class
		return codeName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof SpeciesDetails that) {
			// codeName is the unique key for this class
			return this.codeName.equals(that.codeName);
		} else {
			return false;
		}
	}
}
