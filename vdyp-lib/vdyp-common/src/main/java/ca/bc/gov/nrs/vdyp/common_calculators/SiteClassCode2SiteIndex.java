package ca.bc.gov.nrs.vdyp.common_calculators;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.ClassErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CommonCalculatorException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.ForestInventoryZoneException;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.SpeciesErrorException;
import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies;

/**
 * SiteClassCode2SiteIndex.java - translates site class code to site index (height in metres) for a given species, site
 * class, and FIZ.
 * <p>
 * The translation is intended to be used where total age is small (under 30 years) and where site index based on height
 * may not be reliable.
 * <p>
 * Primarily used by VDYP and FredTab.
 * <p>
 * The origin of the values used here is Inventory Branch.
 */
public class SiteClassCode2SiteIndex {
	/**
	 * Convert a species, site class and forest inventory zone to a site index (in m).
	 *
	 * @param spIndex             the species' index.
	 * @param siteClass           (one of G, M, P or L)
	 * @param forestInventoryZone (A through L, with A, B and C coastal and the others interior)
	 * @return the site index, in metres
	 * @throws CommonCalculatorException
	 */
	public static double classToIndex(SiteIndexSpecies spIndex, char siteClass, char forestInventoryZone)
			throws CommonCalculatorException {

		if (siteClass != 'G' && siteClass != 'M' && siteClass != 'P' && siteClass != 'L') {
			throw new ClassErrorException("Unknown site class code: " + siteClass);
		}

		if (spIndex != null) {

			switch (spIndex) {
			case SI_SPEC_ACT, SI_SPEC_MB:
				switch (siteClass) {
				case 'G':
					return 26;
				case 'M':
					return 18;
				case 'P':
					return 9;
				case 'L':
					return 3;
				default:
					break;
				}
				break;
			case SI_SPEC_AT, SI_SPEC_EA, SI_SPEC_EP:
				switch (siteClass) {
				case 'G':
					return 27;
				case 'M':
					return 20;
				case 'P':
					return 12;
				case 'L':
					return 4;
				default:
					break;
				}
				break;
			case SI_SPEC_BA:
				switch (siteClass) {
				case 'G':
					return 29;
				case 'M':
					return 23;
				case 'P':
					return 14;
				case 'L':
					return 5;
				default:
					break;
				}
				break;
			case SI_SPEC_BL:
				switch (siteClass) {
				case 'G':
					return 18;
				case 'M':
					return 15;
				case 'P':
					return 11;
				case 'L':
					return 5;
				default:
					break;
				}
				break;
			case SI_SPEC_CWC, SI_SPEC_YC:
				switch (siteClass) {
				case 'G':
					return 29;
				case 'M':
					return 23;
				case 'P':
					return 15;
				case 'L':
					return 6;
				default:
					break;
				}
				break;
			case SI_SPEC_CWI:
				switch (siteClass) {
				case 'G':
					return 22;
				case 'M':
					return 19;
				case 'P':
					return 13;
				case 'L':
					return 5;
				default:
					break;
				}
				break;
			case SI_SPEC_DR:
				switch (siteClass) {
				case 'G':
					return 33;
				case 'M':
					return 23;
				case 'P':
					return 13;
				case 'L':
					return 6;
				default:
					break;
				}
				break;
			case SI_SPEC_FDC:
				switch (siteClass) {
				case 'G':
					return 32;
				case 'M':
					return 27;
				case 'P':
					return 18;
				case 'L':
					return 7;
				default:
					break;
				}
				break;
			case SI_SPEC_FDI:
				switch (siteClass) {
				case 'G':
					return 20;
				case 'M':
					return 17;
				case 'P':
					return 12;
				case 'L':
					return 5;
				default:
					break;
				}
				break;
			case SI_SPEC_HWC:
				switch (ForestInventoryZone.toRegion(forestInventoryZone)) {
				case FIZ_COAST:
					switch (siteClass) {
					case 'G':
						return 28;
					case 'M':
						return 22;
					case 'P':
						return 14;
					case 'L':
						return 5;
					default:
						break;
					}
					break;

				case FIZ_INTERIOR:
					switch (siteClass) {
					case 'G':
						return 21;
					case 'M':
						return 18;
					case 'P':
						return 12;
					case 'L':
						return 4;
					default:
						break;
					}
					break;

				default:
					throw new ForestInventoryZoneException("Unknown FIZ code: " + forestInventoryZone);
				}

			case SI_SPEC_HWI:
				switch (siteClass) {
				case 'G':
					return 21;
				case 'M':
					return 18;
				case 'P':
					return 12;
				case 'L':
					return 4;
				default:
					break;
				}
				break;
			case SI_SPEC_LA, SI_SPEC_LT, SI_SPEC_LW:
				switch (siteClass) {
				case 'G':
					return 20;
				case 'M':
					return 16;
				case 'P':
					return 10;
				case 'L':
					return 3;
				default:
					break;
				}
				break;
			case SI_SPEC_PLI, SI_SPEC_PA, SI_SPEC_PF:
				switch (siteClass) {
				case 'G':
					return 20;
				case 'M':
					return 16;
				case 'P':
					return 11;
				case 'L':
					return 4;
				default:
					break;
				}
				break;
			case SI_SPEC_PY:
				switch (siteClass) {
				case 'G':
					return 17;
				case 'M':
					return 14;
				case 'P':
					return 10;
				case 'L':
					return 4;
				default:
					break;
				}
				break;
			case SI_SPEC_PW:
				switch (siteClass) {
				case 'G':
					return 28;
				case 'M':
					return 22;
				case 'P':
					return 12;
				case 'L':
					return 4;
				default:
					break;
				}
				break;
			case SI_SPEC_SS:
				switch (siteClass) {
				case 'G':
					return 28;
				case 'M':
					return 21;
				case 'P':
					return 11;
				case 'L':
					return 4;
				default:
					break;
				}
				break;

			case SI_SPEC_SB, SI_SPEC_SW, SI_SPEC_SE:
				switch (siteClass) {
				case 'G':
					return 19;
				case 'M':
					return 15;
				case 'P':
					return 10;
				case 'L':
					return 5;
				default:
					break;
				}
				break;

			default:
				break /* fall through */;
			}
		}

		throw new SpeciesErrorException(
				MessageFormat.format(
						"classToIndex: not found: spIndex {0}, sitecl {1}, fiz {2}", spIndex, siteClass,
						forestInventoryZone
				)
		);
	}
}
