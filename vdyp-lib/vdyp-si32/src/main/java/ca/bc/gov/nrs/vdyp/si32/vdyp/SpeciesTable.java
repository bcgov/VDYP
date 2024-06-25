package ca.bc.gov.nrs.vdyp.si32.vdyp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexEquation;
import ca.bc.gov.nrs.vdyp.si32.cfs.CfsTreeSpecies;

/**
 * Maintains all information for VDYP7 species.
 * <p>
 * These entries MUST be listed alphabetically by sCodeName. It is essential that this array's contents match
 * {@code enumSP64Enum} exactly.
 * <p>
 * iCrntCurve is left uninitialized until the first request of a site curve is made or assigned at which time defaults
 * will be populated from SINDEX.
 */
public class SpeciesTable {

	private static final String ARBUTUS = "Arbutus";
	private static final String DOUGLAS_FIR = "Douglas Fir";
	private static final String YELLOW_CEDAR = "Yellow Cedar";
	private static final String UNKNOWN_NAME = "<<unknown>>";

	public record SpeciesTableItem(/** The index at which this item resides in the table */
	int index, SpeciesDetails details) {
	}

	public static final int UNKNOWN_ENTRY_INDEX = 0;
	public static final String UNKNOWN_ENTRY_CODE_NAME = "??";

	public static final String UNKNOWN_ENTRY_FULL_NAME = "Invalid Species";
	public static final String UNKNOWN_ENTRY_LATIN_NAME = "Invalid Species";
	public static final String UNKNOWN_ENTRY_GENUS_NAME = "";
	public static final String UNKNOWN_ENTRY_SP0_NAME_VALUE = "";
	public static final boolean UNKNOWN_ENTRY_IS_COMMERCIAL_VALUE = false;
	public static final boolean UNKNOWN_ENTRY_IS_DECIDUOUS_VALUE = false;
	public static final boolean UNKNOWN_ENTRY_IS_SOFTWOOD_VALUE = false;
	public static final float UNKNOWN_ENTRY_CROWN_CLOSURE_VALUE = -1.0f;
	public static final int UNKNOWN_ENTRY_CURRENT_SI_CURVE_VALUE = -1;

	public static final SpeciesDetails DefaultEntry = new SpeciesDetails(
			UNKNOWN_ENTRY_CODE_NAME, UNKNOWN_ENTRY_FULL_NAME, UNKNOWN_ENTRY_LATIN_NAME, UNKNOWN_ENTRY_GENUS_NAME,
			UNKNOWN_ENTRY_SP0_NAME_VALUE, CfsTreeSpecies.UNKNOWN, UNKNOWN_ENTRY_IS_COMMERCIAL_VALUE,
			UNKNOWN_ENTRY_IS_DECIDUOUS_VALUE, UNKNOWN_ENTRY_IS_SOFTWOOD_VALUE,
			new float[] { UNKNOWN_ENTRY_CROWN_CLOSURE_VALUE, UNKNOWN_ENTRY_CROWN_CLOSURE_VALUE },
			getDefaultSiteIndexCurves()
	);

	public static final SpeciesTableItem DefaultTableItem = new SpeciesTableItem(0, DefaultEntry);

	private final List<SpeciesTableItem> speciesTable = new ArrayList<>();
	private final Map<String, SpeciesTableItem> speciesByTextMap = new HashMap<>();

	private void addSpeciesToTable(SpeciesDetails item) {

		int index = speciesTable.size();
		if (index != SP64Name.forText(item.codeName()).ordinal()) {
			throw new IllegalStateException(
					MessageFormat.format(
							"enumSP64Name's ordinal value for {0}"
									+ " does not match the index the species will be placed in SpeciesTable {1}",
							item.codeName(), index
					)
			);
		}

		SpeciesTableItem tableItem = new SpeciesTableItem(index, item);

		speciesTable.add(tableItem);
		speciesByTextMap.put(item.codeName(), tableItem);
	}

	/**
	 * Performs a case-insensitive search for the species with the given sp64 code name.
	 *
	 * @param sp64Name the code name of the species to be found.
	 * @return the {@code structSpeciesTableItem} of that name, or {@code DefaultEntry} if not found.
	 */
	public SpeciesTableItem getByCode(String sp64Name) {

		if (sp64Name != null) {
			String sp64NameUC = sp64Name.toUpperCase();
			if (speciesByTextMap.containsKey(sp64NameUC)) {
				return speciesByTextMap.get(sp64NameUC);
			}
		}

		return DefaultTableItem;
	}

	public int getNSpecies() {
		return speciesTable.size() - 1;
	};

	public SpeciesTable() {

		addSpeciesToTable(DefaultEntry);

		addSpeciesToTable(
				new SpeciesDetails(
						"A", "Aspen/Cottonwood/Poplar", "Populus", "A", "AC", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ABAL", "Silver Fir", "Abies alba", "B", "B", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ABCO", "White Fir", "Abies concolor", "B", "B", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"AC", "Poplar", "Populus balsamifera", "CT", "AC", CfsTreeSpecies.BALSAM_POPLAR, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ACB", "Balsam Poplar", "Populus balsamifera ssp. balsamifera", "CT", "AC",
						CfsTreeSpecies.BALSAM_POPLAR, true, true, false, getDefaultCrownClosure(61.0f, 61.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ACT", "Black Cottonwood", "Populus balsamifera spp. trichocarpa", "CT", "AC",
						CfsTreeSpecies.UNKNOWN, true, true, false, getDefaultCrownClosure(61.0f, 61.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"AD", "Cottonwood (exotic)", UNKNOWN_NAME, "CT", "AC", CfsTreeSpecies.UNKNOWN, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"AH", "Poplar Cottonwood hybrid", UNKNOWN_NAME, "??", "AC", CfsTreeSpecies.ASPEN_TREMBLING,
						true, true, false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"AT", "Trembling Aspen", "Populus tremuloides", "A", "AT", CfsTreeSpecies.ASPEN_TREMBLING, true,
						true, false, getDefaultCrownClosure(52.0f, 52.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"AX", "Hybrid Poplars", "Populus ssp.", "CT", "AC", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"B", "Balsam", "Abies", "B", "B", CfsTreeSpecies.FIR, true, false, true,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BA", "Amabilis/Pacific Silver Fir", "Abies amabilis", "B", "B", CfsTreeSpecies.FIR_AMABILIS,
						true, false, true, getDefaultCrownClosure(57.0f, 57.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BAC", "Amabilis fir (coast)", UNKNOWN_NAME, "B", "B", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BAI", "Amabilis fir (interior)", UNKNOWN_NAME, "B", "B", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BB", "Balsam Fir", "Abies balsamea", "B", "B", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BC", "White Fir", "Abies concolor", "B", "B", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BG", "Grand Fir", "Abies grandis", "B", "B", CfsTreeSpecies.FIR_GRAND, true, false, true,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BI", "Birch", "Betula", "BI", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BL", "Alpine Fir", "Abies lasiocarpa", "B", "B", CfsTreeSpecies.FIR_SUBALPINE_OR_ALPINE, true,
						false, true, getDefaultCrownClosure(42.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BM", "Shasta Red Fir", "Abies magnifica var. shastensis", "B", "B", CfsTreeSpecies.UNKNOWN,
						true, false, true, getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BN", "Noble fir", UNKNOWN_NAME, "B", "B", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BP", "Noble Fir", "Abies procera", "B", "B", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(57.0f, 42.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"BV", "Silver/Paper Birch", UNKNOWN_NAME, "BI", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"C", "Cedar", "Thuja", "C", "C", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"CI", "Incense Cedar", "Calocedrus decurrens", "??", "C", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"COT", "Cottonwood", UNKNOWN_NAME, "CT", "AC", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"CP", "Port Orford Cedar", UNKNOWN_NAME, "??", "C", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"CT", "Cottonwood", UNKNOWN_NAME, "CT", "AC", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"CW", "Western Red Cedar", "Thuja plicata", "C", "C", CfsTreeSpecies.CEDAR_WESTERN_RED, true,
						false, true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"CY", YELLOW_CEDAR, UNKNOWN_NAME, "C", "Y", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"D", "Alder", "Alnus", "D", "D", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(72.0f, 72.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"DF", DOUGLAS_FIR, "Pseudotsuga menziesii", "F", "F", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(61.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"DG", "Sitka (green) Alder", UNKNOWN_NAME, "D", "D", CfsTreeSpecies.ALDER_SITKA, true, true,
						false, getDefaultCrownClosure(72.0f, 72.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"DM", "Mountain Alder", UNKNOWN_NAME, "D", "D", CfsTreeSpecies.ALDER_SITKA, true, true, false,
						getDefaultCrownClosure(72.0f, 72.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"DR", "Red Alder", "Alnus rubra", "D", "D", CfsTreeSpecies.ALDER_RED, true, true, false,
						getDefaultCrownClosure(72.0f, 72.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"E", "Birch", "Betula", "BI", "E", CfsTreeSpecies.BIRCH_WHITE, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"EA", "Common Paper Birch", "Betula neoalaskana", "BI", "E", CfsTreeSpecies.BIRCH_ALASKA_PAPER,
						true, true, false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"EB", "Bog Birch", UNKNOWN_NAME, "BI", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"EE", "European Birch", "Betula pendula", "BI", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"EP", "Silver Paper Birch", "Betula papyrifera", "BI", "E", CfsTreeSpecies.BIRCH_WHITE, true,
						true, false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ES", "Silver Birch (exotic)", "Betula pubescens", "BI", "E", CfsTreeSpecies.UNKNOWN, true,
						true, false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"EW", "Water Birch", "Betula occidentalis", "BI", "E", CfsTreeSpecies.BIRCH_WHITE, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"EXP", "Alaska x Paper Birch Hybrid", "Betula x. winteri", "BI", "E",
						CfsTreeSpecies.BIRCH_ALASKA_PAPER_AND_WHITE, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"F", DOUGLAS_FIR, "Pseudotsuga", "F", "F", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(61.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"FD", DOUGLAS_FIR, "Pseudotsuga menziesii", "F", "F",
						CfsTreeSpecies.FIR_DOUGLAS_AND_ROCKY_MOUNTAIN, true, false, true,
						getDefaultCrownClosure(61.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"FDC", "Douglas Fir (Coastal)", "Pseudotsuga mensiesii var. menziesii", "F", "F",
						CfsTreeSpecies.FIR_DOUGLAS_AND_ROCKY_MOUNTAIN, true, false, true,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"FDI", "Douglas Fir (Interior)", "Pseudotsuga menziesii var. glauca", "F", "F",
						CfsTreeSpecies.FIR_DOUGLAS_AND_ROCKY_MOUNTAIN, true, false, true,
						getDefaultCrownClosure(48.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"G", "Dogwood", "Cornus", "G", "MB", CfsTreeSpecies.UNKNOWN, true, false, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"GP", "Pacific Dogwood", "Cornus nuttallii", "G", "MB", CfsTreeSpecies.DOGWOOD_WESTERNFLOWERING,
						true, false, false, getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"GR", "Red-Osier Dogwood", UNKNOWN_NAME, "G", "MB", CfsTreeSpecies.UNKNOWN, true, false, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"H", "Hemlock", "Tsuga", "H", "H", CfsTreeSpecies.HEMLOCK, true, false, true,
						getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"HM", "Mountain Hemlock", "Tsuga mertensiana", "H", "H", CfsTreeSpecies.HEMLOCK_MOUNTAIN, true,
						false, true, getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"HW", "Western Hemlock", "Tsuga heterophylla", "H", "H", CfsTreeSpecies.HEMLOCK_WESTERN, true,
						false, true, getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"HWC", "Western hemlock (coast)", UNKNOWN_NAME, "H", "H", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"HWI", "Western hemlock (interior)", UNKNOWN_NAME, "H", "H", CfsTreeSpecies.UNKNOWN, true,
						false, true, getDefaultCrownClosure(51.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"HXM", "Western/Mountain Hemlock cross", UNKNOWN_NAME, "H", "H", CfsTreeSpecies.UNKNOWN, true,
						false, true, getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"IG", "Giant Sequoia", "Sequoiadendron giganteum", "C", "C", CfsTreeSpecies.UNKNOWN, true,
						false, true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"IS", "Coast Redwood", "Sequoia sempervirens", "C", "C", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"J", "Juniper", "Juniperus", "J", "C", CfsTreeSpecies.JUNIPER_ROCKY_MOUNTAIN, true, false, true,
						getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"JR", "Rocky Mountain Juniper", "Juniperus scopulorum", "J", "C",
						CfsTreeSpecies.JUNIPER_ROCKY_MOUNTAIN, true, false, true, getDefaultCrownClosure(60.0f, 51.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"K", "Cascara", "Rhamnus", "K", "E", CfsTreeSpecies.UNKNOWN, true, false, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"KC", "Casara", "Rhamnus Purshiana", "K", "E", CfsTreeSpecies.SPRUCE_WHITE, true, false, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"L", "Larch", "Larix", "L", "L", CfsTreeSpecies.TAMARACK_LARCH, true, true, false,
						getDefaultCrownClosure(54.0f, 54.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"LA", "Alpine Larch", "Larix lyallii", "L", "L", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(54.0f, 54.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"LE", "Eastern Larch", UNKNOWN_NAME, "??", "L", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(54.0f, 54.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"LT", "Tamarack", "Larix laricina", "L", "L", CfsTreeSpecies.TAMARACK, true, true, false,
						getDefaultCrownClosure(54.0f, 54.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"LW", "Western Larch", "Larix occidentalis", "L", "L", CfsTreeSpecies.LARCH_WESTERN, true, true,
						false, getDefaultCrownClosure(54.0f, 54.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"M", "Maple", "Acer", "M", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"MB", "Broadleaf Maple", "Acer macrophyllum", "M", "MB", CfsTreeSpecies.MAPLE_BIGLEAF, true,
						true, false, getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ME", "Box elder", "Acer negundo", "??", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"MN", "Norway Maple", "Acer platanoides", "M", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"MR", "Rocky Mountain Maple", UNKNOWN_NAME, "M", "MB", CfsTreeSpecies.MAPLE, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"MS", "Sycamore Maple", UNKNOWN_NAME, "M", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"MV", "Vine Maple", "Acer macrophyllum", "M", "MB", CfsTreeSpecies.MAPLE, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OA", "Incense cedar", "Calocedrus decurrens", "??", "C", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OB", "Giant sequoia", "Seqoiadendron giganteum", "??", "C", CfsTreeSpecies.UNKNOWN, true,
						false, true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OC", "Coast redwood", "Sequoia sempervirens", "??", "C", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OD", "European mountain Ash", "Sorbus aucuparia", "??", "MB", CfsTreeSpecies.UNKNOWN, true,
						false, false, getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OE", "Siberian elm", "Ulmus pumila", "??", "MB", CfsTreeSpecies.UNKNOWN, true, false, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OF", "Common pear", "Pyrus communis", "??", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"OG", "Oregon ash", "Fraxinus latifolia", "??", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"P", "Pine", "Pinus", "PL", "PL", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(50.0f, 50.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PA", "Whitebark Pine", "Pinus albicaulis", "PL", "PA", CfsTreeSpecies.PINE_WHITEBARK, true,
						false, true, getDefaultCrownClosure(50.0f, 50.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PF", "Limber Pine", "Pinus Flexilis", "PL", "PA", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(50.0f, 50.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PJ", "Jack Pine", "Pinus banksiana", "PL", "PL", CfsTreeSpecies.PINE_JACK, true, false, true,
						getDefaultCrownClosure(50.0f, 50.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PL", "Lodgepole Pine", "Pinus contorta", "PL", "PL", CfsTreeSpecies.PINE_LODGEPOLE, true,
						false, true, getDefaultCrownClosure(50.0f, 50.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PLC", "Lodgepole Pine (Coastal)", "Pinus contorta var. contorta", "PL", "PL",
						CfsTreeSpecies.PINE_SHORE, true, false, true, getDefaultCrownClosure(50.0f, 50.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PLI", "Lodgepole Pine (Interior)", "Pinus contorta var. latifolia", "PL", "PL",
						CfsTreeSpecies.PINE_LODGEPOLE, true, false, true, getDefaultCrownClosure(50.0f, 50.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PM", "Monterray Pine", "Pinus radiata", "PL", "PW", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(55.0f, 55.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PR", "Red Pine", "Pinus Resinosa", "PL", "PW", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(55.0f, 55.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PS", "Sugar Pine", "Pinus lambertiana", "PL", "PW", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(55.0f, 55.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PV", "Ponderosa pine", "Pinus ponderosa", "PY", "PY", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(30.0f, 30.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PW", "Western White Pine", "Pinus monticola", "PW", "PW", CfsTreeSpecies.PINE_WESTERN_WHITE,
						true, false, true, getDefaultCrownClosure(55.0f, 55.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PXJ", "Lodgepole/Jack Pine Hybrid", "Pinus x. murraybanksiana", "PL", "PL",
						CfsTreeSpecies.UNKNOWN, true, false, true, getDefaultCrownClosure(50.0f, 50.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"PY", "Yellow Pine", "Pinus ponderosa", "PY", "PY", CfsTreeSpecies.PINE_PONDEROSA, true, false,
						true, getDefaultCrownClosure(30.0f, 30.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"Q", "Oak", "Quercus", "Q", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"QE", "English Oak", "Quercus robur", "Q", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"QG", "Garry Oak", "Quercus Garryana", "Q", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"R", ARBUTUS, ARBUTUS, "R", "MB", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"RA", ARBUTUS, "Arbutus menziesii", "R", "MB", CfsTreeSpecies.ARBUTUS, true, true, false,
						getDefaultCrownClosure(71.0f, 71.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"S", "Spruce", "Picea", "S", "S", CfsTreeSpecies.SPRUCE, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SA", "Norway Spruce", UNKNOWN_NAME, "??", "S", CfsTreeSpecies.SPRUCE_WHITE, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SB", "Black Spruce", "Picea mariana", "S", "S", CfsTreeSpecies.SPRUCE_BLACK, true, false, true,
						getDefaultCrownClosure(46.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SE", "Engelmann Spruce", "Picea engelmannii", "S", "S", CfsTreeSpecies.SPRUCE_ENGLEMANN, true,
						false, true, getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SI", "Interior Spruce", UNKNOWN_NAME, "S", "S", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(46.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SN", "Norway Spruce", UNKNOWN_NAME, "S", "S", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SS", "Sitka Spruce", "Picea sitchensis", "S", "S", CfsTreeSpecies.SPRUCE_SITKA, true, false,
						true, getDefaultCrownClosure(50.0f, 50.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SW", "White Spruce", "Picea glauca", "S", "S", CfsTreeSpecies.SPRUCE_WHITE, true, false, true,
						getDefaultCrownClosure(46.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SX", "Spruce Hybrid", "Picea x", "S", "S", CfsTreeSpecies.SPRUCE, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SXB", "SXxSB hybrid", UNKNOWN_NAME, "S", "S", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SXE", "SSxSE hybrid", UNKNOWN_NAME, "S", "S", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SXL", "Sitka/White Spruce Hybrid", "Picea sitchensis x. lutzii", "S", "S",
						CfsTreeSpecies.UNKNOWN, true, false, true, getDefaultCrownClosure(50.0f, 46.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SXS", "Sitka Spruce Hybrid", "Picea sitchensis x", "S", "S", CfsTreeSpecies.UNKNOWN, true,
						false, true, getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SXW", "Engelmann/White Spruce Hybrid", "Picea engelmannii x. glauca", "S", "S",
						CfsTreeSpecies.UNKNOWN, true, false, true, getDefaultCrownClosure(50.0f, 46.0f),
						getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"SXX", "SW hybrid", UNKNOWN_NAME, "S", "S", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(50.0f, 46.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"T", "Yew", "Taxus", "T", "H", CfsTreeSpecies.UNKNOWN, true, true, true,
						getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"TW", "Pacific (western) yew", "Taxus brevifolia", "T", "H", CfsTreeSpecies.YEW_WESTERN, true,
						true, true, getDefaultCrownClosure(61.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"U", "Apple", "Malus", "U", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"UA", "Apple", "Malus pumila", "U", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"UP", "Crab apple", "Malus fusca", "U", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"V", "Cherry", "Prunus", "V", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"VB", "Bitter Cherry", "Prunus emarginata", "V", "E", CfsTreeSpecies.CHERRY_BITTER, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"VP", "Pin Cherry", "Prunus pensylvanica", "V", "E", CfsTreeSpecies.CHERRY_PIN, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"VS", "Sweet Cherry", "Prunus avium", "V", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"VV", "Choke Cherry", "Prunus virginiana", "V", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"W", "Willow", "Salix", "W", "E", CfsTreeSpecies.WILLOW, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WA", "Peachleaf Willow", "Salix amygdaloides", "W", "E", CfsTreeSpecies.UNKNOWN, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WB", "Bebb's Willow", "Salix bebbiana", "W", "E", CfsTreeSpecies.WILLOW, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WD", "Pussy Willow", "Salix discolor", "W", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WI", "Willow", UNKNOWN_NAME, "W", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WP", "Pacific Willow", "Salix lucida", "W", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WS", "Scouler's Willow", "Salix scouleriana", "W", "E", CfsTreeSpecies.WILLOW, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"WT", "Sitka Willow", "Salix sitchensis", "W", "E", CfsTreeSpecies.UNKNOWN, true, true, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"X", "Unknown", UNKNOWN_NAME, "X", "F", CfsTreeSpecies.UNSPECIFIED_SOFTWOOD, true, false, true,
						getDefaultCrownClosure(61.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"XC", "Unknown conifer", UNKNOWN_NAME, "X", "F", CfsTreeSpecies.SPRUCE_WHITE, true, false, true,
						getDefaultCrownClosure(61.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"XH", "Unknown hardwood", UNKNOWN_NAME, "X", "E", CfsTreeSpecies.SPRUCE_WHITE, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"Y", YELLOW_CEDAR, "Chamaecyparis", "C", "Y", CfsTreeSpecies.UNKNOWN, true, false, true,
						getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"YC", YELLOW_CEDAR, "Chamaecyparis nootkatensis", "C", "Y", CfsTreeSpecies.CYPRESS_YELLOW, true,
						false, true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"YP", "Port Orford", "Chamaecyparis lawsoniana", "C", "Y", CfsTreeSpecies.UNKNOWN, true, false,
						true, getDefaultCrownClosure(60.0f, 51.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"Z", "Other Tree", UNKNOWN_NAME, "Z", "E", CfsTreeSpecies.UNKNOWN, true, false, false,
						getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ZC", "Other tree (conifer)", UNKNOWN_NAME, "Z", "F", CfsTreeSpecies.SPRUCE_WHITE, true, false,
						true, getDefaultCrownClosure(61.0f, 48.0f), getDefaultSiteIndexCurves()
				)
		);
		addSpeciesToTable(
				new SpeciesDetails(
						"ZH", "Other tree (hardwood)", UNKNOWN_NAME, "Z", "E", CfsTreeSpecies.SPRUCE_WHITE, true, true,
						false, getDefaultCrownClosure(61.0f, 61.0f), getDefaultSiteIndexCurves()
				)
		);
	}

	private static float[] getDefaultCrownClosure(float coastal, float interior) {
		return new float[] { coastal, interior };
	}

	private static SiteIndexEquation[] getDefaultSiteIndexCurves() {
		return new SiteIndexEquation[] { SiteIndexEquation.SI_NO_EQUATION, SiteIndexEquation.SI_NO_EQUATION };
	}
}
