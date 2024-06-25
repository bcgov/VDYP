package ca.bc.gov.nrs.vdyp.si32.cfs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.si32.vdyp.SP0Name;

public class CfsMethods {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(CfsMethods.class);

	/**
	 * Convert a Canadian Forest Service Tree Class index to a descriptive string.
	 * <p>
	 * Names come from the 'Volume_To_Biomass.doc', Table 2.
	 *
	 * @param cfsTreeClassIndex the CFS Tree Class number to be converted to a string.
	 * @return a string corresponding to the named CFS Tree Class constant, or "Unknown CFS Tree Class" if the given
	 *         integer doesn't identify a known CFS Tree Class.
	 */
	public static String cfsTreeClassToString(int cfsTreeClassIndex) {
		CfsTreeClass cfsTreeCls = CfsTreeClass.forIndex(cfsTreeClassIndex);
		if (cfsTreeCls != null) {
			return cfsTreeCls.getDescription();
		} else {
			return CfsTreeClass.UNKNOWN.getDescription();
		}
	}

	/**
	 * Convert the supplied CFS Genus enumeration value to a corresppnding string value.
	 * <p>
	 * The CFS Tree Genus is defined in 'Appendix 7' in the 'Model_based_volume_to_biomass_CFS' document located in
	 * 'Documents/CFS-Biomass'.
	 * <p>
	 * Elements for this table are automatically generated and copy and pasted from the:
	 * <ol>
	 * <li>'C Enum to String Mapping' column of the
	 * <li>'GenusTable' found on the
	 * <li>'Lookups' tab in the
	 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
	 * <li>'Documents/CFS-Biomass' folder.
	 * </ol>
	 *
	 * @param genus the CFS Tree Genus value to be converted to a string.
	 * @return the string corresponding to the identified CFS Tree Genus. The string for 'UNKNOWN' if the value is not
	 *         recognized.
	 */
	public static String cfsGenusToString(CfsTreeGenus genus) {
		if (genus != null) {
			return genus.getGenusName();
		} else {
			return CfsTreeGenus.UNKNOWN.getGenusName();
		}
	}

	/*-----------------------------------------------------------------------------
	 *
	 * CFS_CFSSP0DensityMin
	 * CFS_CFSSP0DensityMax
	 * CFS_CFSSP0DensityMean
	 * =====================
	 *
	 *    Determine the minimum, maximum and mean density for each of the SP0s.
	 *
	 *
	 * Parameters
	 * ----------
	 *
	 *    sp0Index
	 *       The index of the SP0 for which your want the density value.
	 *
	 *
	 * Return Value
	 * ------------
	 *
	 *    The density value (in kg/m3) for the requested SP0.
	 *    -9.0 if the SP0 index value was not recognized.
	 *
	 *
	 * Remarks
	 * -------
	 *
	 *    Values comes from Tabl6 found in 'Volume_to_Biomass.doc located in
	 *    'Documents/CFS-Biomass'Volume_to_Biomass.doc'
	 */

	public static float cfsSP0DensityMin(SP0Name sp0Index) {
		return CfsSP0Densities.getValue(sp0Index, CfsDensity.MIN_DENSITY_INDEX);
	}

	public static float cfsSP0DensityMax(SP0Name sp0Index) {
		return CfsSP0Densities.getValue(sp0Index, CfsDensity.MAX_DENSITY_INDEX);
	}

	public static float cfsSP0DensityMean(SP0Name sp0Index) {
		return CfsSP0Densities.getValue(sp0Index, CfsDensity.MEAN_DENSITY_INDEX);
	}

	/**
	 * Convert the supplied string into the best fit for the CFS Species. The supplied string must case-insensitively
	 * match one of the strings found in Appendix 7 of 'Model_based_volume_to_biomass_CFS.pdf' found in
	 * 'Documents/CFS-Biomass'.
	 *
	 * @param cfsSpeciesName the cfs name of the species.
	 * @return the CFSTreeSpecies corresponding to the given name. UNKNOWN is returned if the given name doesn't match a
	 *         known name.
	 *
	 */
	public static CfsTreeSpecies stringToCfsSpecies(String cfsSpeciesName) {
		return CfsSpeciesMethods.getSpeciesBySpeciesName(cfsSpeciesName);
	}

	/**
	 * Determine the CFS Genus for a particular CFS Species. The CFS Species to Genus conversions are defined in
	 * Appendix 7 of the document 'Model_based_volume_to_biomass_CFS.pdf' found in the folder 'Documents/CFS-Biomass'.
	 *
	 * @param cfsSpcs the CFS Species whose genus is to be determined.
	 * @return The CFS Genus code corresponding to the supplied CFS Species. UNKNOWN if the 'cfsSpcs' code was not
	 *         recognized.
	 */

	public static CfsTreeGenus cfsSpcsNumToCFSGenus(CfsTreeSpecies cfsSpcs) {
		return CfsSpeciesMethods.getGenusBySpecies(cfsSpcs);
	}

	/*-----------------------------------------------------------------------------
	 *
	 * CFS_LogConfiguration
	 * ====================
	 *
	 *    Log all of the CFS Configuration parameters to the logging system.
	 *
	 *
	 * Parameters
	 * ----------
	 *
	 *    forceLogr
	 *       If desired, force logging through a supplied logger. Otherwise an
	 *       internal logger will be used.
	 *       NULL_LOGGER triggers use of a routine specific logger.
	 *
	 *    msgMrkr
	 *       The message marker to apply to each of the logging statements.
	 *       NULL_MSG_MARKER if no message marker is to be used.
	 *
	 *    logLvl
	 *       The logging level to log each logging statement at.
	 *
	 *    indent
	 *       The number of additional spaces to indent the table from the current logging
	 *       indent level.
	 *       0 indicates no additional indent.
	 *
	 *
	 * Remarks (Optional Heading)
	 * -------
	 *
	 *    Remarks, warnings, special conditions to be aware of, etc.
	 *
	 */

	public static void logConfiguration() {
		// TODO
	}
}