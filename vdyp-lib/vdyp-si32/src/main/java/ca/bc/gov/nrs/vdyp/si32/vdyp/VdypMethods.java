package ca.bc.gov.nrs.vdyp.si32.vdyp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CodeErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CurveErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.ForestInventoryZoneException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SpeciesRegion;
import ca.bc.gov.nrs.vdyp.si32.vdyp.SpeciesTable.SpeciesTableItem;
import ca.bc.gov.nrs.vdyp.sindex.Sindxdll;

public class VdypMethods {
	private static final Logger logger = LoggerFactory.getLogger(VdypMethods.class);

	private static final int SI_MAX_CURVES = 123;

	public static SpeciesTable speciesTable = new SpeciesTable();

	/**
	 * Returns the total number of defined species. One is subtracted one off of the size of the speciesTable because
	 * there is a blank entry at the head of the array which does not represent a valid species.
	 *
	 * @return as described.
	 */
	public static int getNumDefinedSpecies() {
		return speciesTable.getNSpecies();
	}

	/**
	 * Converts a SP64 code name into an index into the Species table.
	 *
	 * @param sp64CodeName the code name of the SP64 name in question
	 * @return the index into the Species table for the corresponding species, and SpeciesTable.UNKNOWN_ENTRY_INDEX if
	 *         the species was not recognized.
	 */
	public static int speciesIndex(String sp64CodeName) {
		int result;

		if (StringUtils.isNotBlank(sp64CodeName)) {
			try {
				SpeciesTableItem spsc = speciesTable.getByCode(sp64CodeName);
				result = spsc.index();
			} catch (IllegalArgumentException e) {
				result = SpeciesTable.UNKNOWN_ENTRY_INDEX;
			}
		} else {
			result = SpeciesTable.UNKNOWN_ENTRY_INDEX;
		}

		return result;
	}

	/**
	 * Determines if the given species name is valid; that is, if it is either a Commercial or Non-Commercial tree
	 * species as defined by MoF..
	 *
	 * @param spName the species name to test.
	 * @return a boolean indicating the result of the test.
	 */
	public static boolean isValidSpecies(String sp64Name) {
		return speciesIndex(sp64Name) > 0;
	}

	/**
	 * Determines if the tree species is a deciduous species or not.
	 *
	 * @param sp64Name the name of tree species to be tested.
	 * @return a boolean with the result
	 */
	public static boolean isDeciduous(SP64Name sp64Name) {
		if (sp64Name != null) {
			return speciesTable.getByCode(sp64Name.getText()).details().isDeciduous();
		} else {
			return false;
		}
	}

	/**
	 * Determines if the tree species is a commercial species or not.
	 *
	 * @param sp64Name the species in question
	 * @return a boolean with the result
	 */
	public static boolean isCommercial(SP64Name sp64Name) {
		if (sp64Name != null) {
			return speciesTable.getByCode(sp64Name.getText()).details().isCommercial();
		} else {
			return false;
		}
	}

	/**
	 * Returns the MoF abbreviation for a given species.
	 *
	 * @param sp64Name the species in question
	 * @return the short ("code") name of the species.
	 */
	public static String getSpeciesShortName(SP64Name sp64Name) {
		if (sp64Name != null) {
			return sp64Name.getText();
		} else {
			return SP64Name.UNKNOWN.getText();
		}
	}

	/**
	 * Returns the full name of a given species.
	 *
	 * @param sp64Name the species in question
	 * @return the full ("common") name of the species.
	 */
	public static String getSpeciesFullName(SP64Name sp64Name) {
		if (sp64Name != null) {
			return speciesTable.getByCode(sp64Name.getText()).details().fullName();
		} else {
			return SpeciesTable.DefaultEntry.fullName();
		}
	}

	/**
	 * Returns the latin name of a given species.
	 *
	 * @param sp64Name the species
	 * @return the latin name of the species.
	 */
	public static String getSpeciesLatinName(SP64Name sp64Name) {
		if (sp64Name != null) {
			return speciesTable.getByCode(sp64Name.getText()).details().latinName();
		} else {
			return SpeciesTable.DefaultEntry.latinName();
		}
	}

	/**
	 * Returns the genus of a given species.
	 *
	 * @param sp64 the species
	 * @return the latin name of the species.
	 */
	public static String getSpeciesGenus(SP64Name sp64Name) {
		if (sp64Name != null) {
			return speciesTable.getByCode(sp64Name.getText()).details().genusName();
		} else {
			return SpeciesTable.DefaultEntry.genusName();
		}
	}

	/**
	 * Converts a species name to an equivalent SINDEX supported Species code.
	 *
	 * @param spName the name of the species to convert.
	 * @param region indicates which provincial region to set the site curve for,
	 *
	 * @return the equivalent SINDEX species code. "" is returned if the species is null or is not supported by SINDEX
	 *         and no mapping exists.
	 */
	public static String getSINDEXSpecies(String speciesName, SpeciesRegion region) {

		String speciesCode = "";

		if (speciesName != null && region != null) {

			char fiz;

			switch (region) {
			case COAST:
				fiz = 'A';
				break;
			case INTERIOR:
				fiz = 'D';
				break;
			default:
				fiz = ' ';
				break;
			}

			if (fiz != ' ') {
				SiteIndexSpecies s = SiteIndexSpecies.SI_NO_SPECIES;

				try {
					s = Sindxdll.SpecRemap(speciesName, fiz);
				} catch (CodeErrorException | ForestInventoryZoneException e) {
					// fall through
				}

				if (s != SiteIndexSpecies.SI_NO_SPECIES) {
					speciesCode = Sindxdll.SpecCode(s);
				}
			}
		}

		return speciesCode;
	}

	/**
	 * Converts a CFS species name to an equivalent VDYP7 species (genus) name.
	 *
	 * @param sp64CodeName the SP64 code name of the species to convert.
	 * @return the equivalent VDYP7 species code. "" is returned if the species is not supported by VDYP7 and no mapping
	 *         exists.
	 */
	public static String getVDYP7Species(String sp64CodeName) {
		if (sp64CodeName != null) {
			return speciesTable.getByCode(sp64CodeName).details().sp0Name();
		} else {
			return SpeciesTable.DefaultEntry.sp0Name();
		}
	}

	/**
	 * Convert the supplied SP0Name constant into a VDYP7 species name.
	 * <p>
	 * SP0 and VDYP7 species names are one and the same.
	 * <p>
	 * The returned name is a valid VDYP7 class of species names and would also be a valid SP64 species name.
	 *
	 * @param sp0 the species whose name is to be fetched.
	 * @return the name sought, or "" if the SP0 number is not recognized.
	 */
	public static String getSP0Species(SP0Name sp0) {

		if (sp0 != null) {
			return sp0.getText();
		} else {
			return SP0Name.UNKNOWN.getText();
		}
	}

	/**
	 * Determines the currently assigned Site Index curve type to use for the specified species.
	 * <p>
	 * Within this program, there a number of core "Freddie" routines which use this value as an input parameter instead
	 * of a species name.
	 * <p>
	 * Changes
	 * <p>
	 * November 21, 1997 - Added a Coastal/Interior Differentiation for Hemlock.
	 * <p>
	 * The curves assigned to specific species may be reassigned through calls to 'VDYP_SetCurrentSICurve'. If no such
	 * curve assignment has been made, the default curve from SINDEX will be returned.
	 *
	 * @param sp64Name the species short ("code") name such as "ABAL"
	 * @param region   the region under consideration
	 * @return The Site Index curve type to use for this species. -1 if the species or region was not recognized.
	 */
	public static SiteIndexEquation getCurrentSICurve(String sp64Name, SpeciesRegion region) {

		SiteIndexEquation siCurve = SiteIndexEquation.SI_NO_EQUATION;

		if (sp64Name != null && region != null) {
			var entry = speciesTable.getByCode(sp64Name);
			siCurve = entry.details().currentSICurve()[region.ordinal()];

			// If the curve for this species is not set, look it up from SINDEX.

			if (siCurve == SiteIndexEquation.SI_NO_EQUATION) {
				SiteIndexSpecies sindexSpcs;
				try {
					sindexSpcs = Sindxdll.SpecRemap(sp64Name, region == SpeciesRegion.COAST ? 'A' : 'D');

					siCurve = Sindxdll.DefCurve(sindexSpcs);
				} catch (CommonCalculatorException e) {
					siCurve = SiteIndexEquation.SI_NO_EQUATION;
				}
				entry.details().currentSICurve()[region.ordinal()] = siCurve;
			}
		}

		return siCurve;
	}

	/**
	 * Determines the BC default Site Index curve type to use for the given species and region.
	 * <p>
	 * Within this program, there a number of core "Freddie" routines which use this value as an input parameter instead
	 * of a species name.
	 *
	 * @param sp64Name the species short ("code") name such as "ABAL"
	 * @param region   the region under consideration
	 *
	 * @return the BC default Site Index curve type to use for this species. Returns -2 for non-commercial species.
	 */
	public static SiteIndexEquation getDefaultSICurve(String sp64Name, SpeciesRegion region) {

		SiteIndexEquation siCurve = SiteIndexEquation.SI_NO_EQUATION;

		if (sp64Name != null && region != null
				&& speciesTable.getByCode(sp64Name).details() != SpeciesTable.DefaultEntry) {

			try {
				SiteIndexSpecies sindxSpcs = Sindxdll.SpecRemap(sp64Name, (region == SpeciesRegion.COAST) ? 'A' : 'D');

				siCurve = Sindxdll.DefCurve(sindxSpcs);
			} catch (CommonCalculatorException e) {
				// fall through
			}
		}

		return siCurve;
	}

	/**
	 * Sets the Site Index curve to use for a particular species.
	 *
	 * @param sp64CodeName the species short ("code") name such as "ABAL"
	 * @param region       the region under consideration
	 * @param siCurve      the site index curve to use for the specified species. -1.0f resets the curve to the default.
	 *
	 * @return the previous value.
	 */
	public static SiteIndexEquation
			setCurrentSICurve(String sp64CodeName, SpeciesRegion region, SiteIndexEquation siCurve) {

		SiteIndexEquation oldCurve = getCurrentSICurve(sp64CodeName, region);
		var speciesEntry = speciesTable.getByCode(sp64CodeName);
		if (region != null && speciesEntry.details() != SpeciesTable.DefaultEntry) {
			speciesEntry.details().currentSICurve()[region.ordinal()] = siCurve;
		}

		return oldCurve;
	}

	/**
	 * Determines the number of available SI curves for a particular species.
	 *
	 * @param respectSpeciesBoundariesInd
	 *                                    <ul>
	 *                                    <li><b>true</b> all remaining parameters are used to filter the particular set
	 *                                    of curves to count.
	 *                                    <li><b>false</b> the count of all curves is returned, regardless of species
	 *                                    and provincial location.
	 *                                    </ul>
	 * @param sp64Name                    the short ("code") name of the species.
	 * @param mixInteriorCoastalInd
	 *                                    <ul>
	 *                                    <li><b>true</b> curves from the interior and the coast are counted in the
	 *                                    total count. In this case, the <code>region</code> parameter is ignored.
	 *                                    <li><b>false</b> only the curves from the particular region of the province
	 *                                    specified by the <code>region</code> parameter are counted.
	 *                                    </ul>
	 * @param region                      indicates which provincial region to get the number for. If
	 *                                    <code>mixInteriorCoastalInd</code> is <code>true</code>, this parameter is
	 *                                    ignored.
	 * @return a count of all of the Site Index curves for a particular species. 0 is returned if the species is
	 *         non-commercial or the species is not recognized. This count is always positive for commercial species.
	 *         Always 0 for non-commercial species. When mixing interior and coastal curves, the count may be the same
	 *         as when treating these regions separately.
	 */
	public static int getNumSICurves(
			boolean respectSpeciesBoundariesInd, String sp64Name, boolean mixInteriorCoastalInd, SpeciesRegion region
	) {

		int numCurves = 0;

		// Check if we are going to return a count of all curves regardless of region.
		if (!respectSpeciesBoundariesInd) {
			return SI_MAX_CURVES;
		}

		if (sp64Name != null && (mixInteriorCoastalInd || region != null)) {

			int specNum = speciesIndex(sp64Name);

			if (specNum == SpeciesTable.UNKNOWN_ENTRY_INDEX) {
				return 0;
			}

			boolean[] countedCurve = new boolean[SI_MAX_CURVES];
			Arrays.fill(countedCurve, 0, SI_MAX_CURVES, false);

			List<Character> forestInventoryZones = new ArrayList<>();
			if (mixInteriorCoastalInd || SpeciesRegion.COAST.equals(region)) {
				// Add in the number of available coastal curves.
				forestInventoryZones.add('A');
			}
			if (mixInteriorCoastalInd || SpeciesRegion.INTERIOR.equals(region)) {
				// Add in the number of available interior curves.
				forestInventoryZones.add('D');
			}

			// Count the available coastal curves.
			for (Character fiz : forestInventoryZones) {
				try {
					SiteIndexSpecies sindxSpeciesIndex = Sindxdll.SpecRemap(sp64Name, fiz);

					// Count each of the curves in the range of curves for the species.

					SiteIndexEquation curve = Sindxdll.FirstCurve(sindxSpeciesIndex);

					while (true) {

						if (!countedCurve[curve.n()]) {
							numCurves++;
							countedCurve[curve.n()] = true;
						}

						try {
							curve = Sindxdll.NextCurve(sindxSpeciesIndex, curve);
						} catch (NoAnswerException e) {
							break;
						}
					}

					// Make sure we track the default curve just in case it lies outside of the range of curves
					// enumerated above.

					curve = Sindxdll.DefCurve(sindxSpeciesIndex);

					if (!SiteIndexEquation.SI_NO_EQUATION.equals(curve) && !countedCurve[curve.n()]) {
						numCurves++;
						countedCurve[curve.n()] = true;
					}
				} catch (CommonCalculatorException e) {
					logger.warn(
							MessageFormat.format(
									"CommonCalculatorException during evaluation of species {0}, fiz {2}", sp64Name, fiz
							)
					);
				}
			}
		}

		return numCurves;
	}

	/**
	 * Obtains the SINDEX species index most closely associated with a particular site curve.
	 *
	 * @param siCurve the Site Index curve to convert into a species name.
	 * @return species index, for use in other Sindex functions. SI_NO_SPECIES is returned if the curve was not
	 *         recognized.
	 */
	public static SiteIndexSpecies getSICurveSpeciesIndex(SiteIndexEquation siCurve) {
		try {
			return Sindxdll.CurveToSpecies(siCurve);
		} catch (CurveErrorException e) {
			return SiteIndexSpecies.SI_NO_SPECIES;
		}
	}

	/**
	 * For a specific species, returns the default Crown Closure associated with that species within a particular region
	 * of the province.
	 *
	 * @param sp64CodeName the species name to be looked up.
	 * @param region       indicates which provincial region to get the default CC for.
	 *
	 * @return the default CC associated with the species in that particular region and -1.0 if the species was not
	 *         recognized or no default CC has been assigned to that species and region.
	 */
	public static float getDefaultCrownClosure(String sp64CodeName, SpeciesRegion region) {

		// Note that if spName is invalid, the default entry is returned, which in
		// turn contains the right default value of -1.0f.

		if (sp64CodeName != null && region != null) {
			return speciesTable.getByCode(sp64CodeName).details().defaultCrownClosure()[region.ordinal()];
		} else {
			return -1.0f;
		}
	}

	/**
	 * Convert a VDYP7 SP0 Species Name into an enumSP0Name.
	 *
	 * @param sp0Name the VDYP7 Species Name to be converted into a species index.
	 *
	 * @return The enumSP0Name corresponding to the supplied species, and UNKNOWN if the species name was not supplied
	 *         or was not recognized. Note that the returned index is a zero-based index.
	 */
	public static SP0Name getVDYP7SpeciesIndex(String sp0Name) {
		SP0Name sp0Index = SP0Name.UNKNOWN;

		if (sp0Name != null) {
			try {
				sp0Index = SP0Name.forText(sp0Name);
			} catch (IllegalArgumentException e) {
				sp0Index = SP0Name.UNKNOWN;
			}
		}

		return sp0Index;
	}
}