package ca.bc.gov.nrs.vdyp.si32.cfs;

import java.text.MessageFormat;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;
import ca.bc.gov.nrs.vdyp.si32.enumerations.SI32Enum;

/**
 * Enumeration of the Tree Species as defined by the Canadian Forest Service.
 * <ul>
 * <li>UNKNOWN
 * <p>
 * Indicates an error condition or an uninitialized state. Should not be used as a place holder for an actual tree
 * species.
 * <li>others
 * <p>
 * </ul>
 * Species names are defined in Appendix 7 of the document 'Model_based_volume_to_biomass.pdf' found in
 * 'Documents/CFS-Biomass'.
 * <p>
 * The list of enumeration constants is automatically generated and copy and pasted into this enum definition from the:
 * <ol>
 * <li>'Conversion Param Enum Defn' column of the
 * <li>'DeadConversionFactorsTable' found on the
 * <li>'Derived C Species Table' tab in the
 * <li>'BC_Inventory_updates_by_CBMv2bs.xlsx' located in the
 * <li>'Documents/CFS-Biomass' folder.
 * </ol>
 */
public enum CfsTreeSpecies implements SI32Enum<CfsTreeSpecies> {
	UNKNOWN(-1, -1, "Unknown Species", CfsTreeGenus.UNKNOWN),

	SPRUCE(0, 100, "Spruce", CfsTreeGenus.SPRUCE), SPRUCE_BLACK(1, 101, "Black Spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_RED(2, 102, "Red Spruce", CfsTreeGenus.SPRUCE), SPRUCE_NORWAY(3, 103, "Norway spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_ENGLEMANN(4, 104, "Englemann spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_WHITE(5, 105, "White spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_SITKA(6, 106, "Sitka spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_BLACK_AND_RED(7, 107, "Black and red spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_RED_AND_WHITE(8, 108, "Red and white spruce", CfsTreeGenus.SPRUCE),
	SPRUCE_OTHER(9, 109, "Other spruce", CfsTreeGenus.SPRUCE),
	/* 10 */
	SPRUCE_AND_BALSAM_FIR(10, 110, "Spruce and balsam fir", CfsTreeGenus.SPRUCE),
	PINE(11, 200, "Pine", CfsTreeGenus.PINE), PINE_WESTERN_WHITE(12, 201, "Western white pine", CfsTreeGenus.PINE),
	PINE_EASTERN_WHITE(13, 202, "Eastern white pine", CfsTreeGenus.PINE),
	PINE_JACK(14, 203, "Jack pine", CfsTreeGenus.PINE), PINE_LODGEPOLE(15, 204, "Lodgepole pine", CfsTreeGenus.PINE),
	PINE_SHORE(16, 205, "Shore pine", CfsTreeGenus.PINE), PINE_WHITEBARK(17, 206, "Whitebark pine", CfsTreeGenus.PINE),
	PINE_AUSTRIAN(18, 207, "Austrian pine", CfsTreeGenus.PINE),
	PINE_PONDEROSA(19, 208, "Ponderosa pine", CfsTreeGenus.PINE),
	/* 20 */
	PINE_RED(20, 209, "Red pine", CfsTreeGenus.PINE), PINE_PITCH(21, 210, "Pitch pine", CfsTreeGenus.PINE),
	PINE_SCOTS(22, 211, "Scots pine", CfsTreeGenus.PINE), PINE_MUGHO(23, 212, "Mugho pine", CfsTreeGenus.PINE),
	PINE_LIMBER(24, 213, "Limber pine", CfsTreeGenus.PINE),
	PINE_JACK_LODGEPOLE_AND_SHORE(25, 214, "Jack, lodgepole, and shore pine", CfsTreeGenus.PINE),
	PINE_OTHER(26, 215, "Other pine", CfsTreeGenus.PINE),
	PINE_HYBRID_JACK_LODGEPOLE(27, 216, "Hybrid jack and lodgepole pine", CfsTreeGenus.PINE),
	PINE_WHITEBARK_AND_LIMBER(28, 217, "Whitebark and limber pine", CfsTreeGenus.PINE),
	FIR(29, 300, "Fir", CfsTreeGenus.FIR),
	/* 30 */
	FIR_AMABILIS(30, 301, "Amabilis fir", CfsTreeGenus.FIR), FIR_BALSAM(31, 302, "Balsam fir", CfsTreeGenus.FIR),
	FIR_GRAND(32, 303, "Grand fir", CfsTreeGenus.FIR),
	FIR_SUBALPINE_OR_ALPINE(33, 304, "Subalpinefir (or alpine fir)", CfsTreeGenus.FIR),
	FIR_BALSAM_AND_ALPINE(34, 305, "Balsam and alpine fir", CfsTreeGenus.FIR),
	FIR_AMABILIS_AND_GRAND(35, 306, "Alpine, amabilis, and grand fir", CfsTreeGenus.FIR),
	FIR_JAPANESE(36, 307, "Japanese fir", CfsTreeGenus.FIR),
	FIR_SPRUCE_AND_BALSAM(37, 320, "Spruce and balsam fir", CfsTreeGenus.FIR),
	FIR_BALSAM_AND_SPRUCE(38, 321, "Balsam fir and spruce", CfsTreeGenus.FIR),
	HEMLOCK(39, 400, "Hemlock", CfsTreeGenus.HEMLOCK),
	/* 40 */
	HEMLOCK_EASTERN(40, 401, "Eastern hemlock", CfsTreeGenus.HEMLOCK),
	HEMLOCK_WESTERN(41, 402, "Western hemlock", CfsTreeGenus.HEMLOCK),
	HEMLOCK_MOUNTAIN(42, 403, "Mountain hemlock", CfsTreeGenus.HEMLOCK),
	HEMLOCK_WESTERN_AND_MOUNTAIN(43, 404, "Western and mountain hemlock", CfsTreeGenus.HEMLOCK),
	FIR_DOUGLAS_AND_ROCKY_MOUNTAIN(44, 500, "Douglas-fir and Rocky Mountain Douglas-fir", CfsTreeGenus.DOUGLAS_FIR),
	TAMARACK_LARCH(45, 600, "Tamarack/Larch", CfsTreeGenus.LARCH),
	LARCH_EUROPEAN(46, 601, "European larch", CfsTreeGenus.LARCH), TAMARACK(47, 602, "Tamarack", CfsTreeGenus.LARCH),
	LARCH_WESTERN(48, 603, "Western larch", CfsTreeGenus.LARCH),
	LARCH_SUBALPINE(49, 604, "Subalpine larch", CfsTreeGenus.LARCH),
	/* 50 */
	LARCH_JAPANESE(50, 605, "Japanese larch", CfsTreeGenus.LARCH),
	CEDAR(51, 700, "Cedar", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	CEDAR_EASTERN_WHITE(52, 701, "Eastern white-cedar", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	CEDAR_WESTERN_RED(53, 702, "Western red cedar", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	CEDAR_AND_OTHER_CONIFERS(54, 703, "Cedar and other conifers", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	JUNIPER(55, 800, "Juniper", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	CEDAR_EASTERN_RED(56, 801, "Eastern red cedar", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	JUNIPER_ROCKY_MOUNTAIN(57, 802, "Rocky Mountain juniper", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	YEW(58, 900, "Yew", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	YEW_WESTERN(59, 901, "Western yew", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	/* 60 */
	CYPRESS(60, 1000, "Cypress", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	CYPRESS_YELLOW(61, 1001, "Yellow cypress", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	OTHER_SOFTWOODS_CONIFERS(62, 1100, "Other softwoods/other conifers", CfsTreeGenus.UNSPECIFIED_CONIFERS),
	TAMARACK_AND_CEDAR(63, 1110, "Tamarack and cedar", CfsTreeGenus.CEDAR_AND_OTHER_CONIFERS),
	UNSPECIFIED_SOFTWOOD(64, 1150, "Unspecified softwood species", CfsTreeGenus.UNSPECIFIED_CONIFERS),
	POPLAR_ASPEN(65, 1200, "Poplar/aspen", CfsTreeGenus.POPLAR),
	ASPEN_TREMBLING(66, 1201, "Trembling aspen", CfsTreeGenus.POPLAR),
	POPLAR_EUROPEAN_WHITE(67, 1202, "European white poplar", CfsTreeGenus.POPLAR),
	BALSAM_POPLAR(68, 1203, "Balsam poplar", CfsTreeGenus.POPLAR),
	COTTONWOOD_BLACK(69, 1204, "Black cottonwood", CfsTreeGenus.POPLAR),
	/* 70 */
	COTTONWOOD_EASTERN(70, 1205, "Eastern cottonwood", CfsTreeGenus.POPLAR),
	ASPEN_LARGETOOTH(71, 1206, "Largetooth aspen", CfsTreeGenus.POPLAR),
	POPLAR_CAROLINA(72, 1207, "Carolina poplar", CfsTreeGenus.POPLAR),
	POPLAR_LOMBARDY(73, 1208, "Lombardy poplar", CfsTreeGenus.POPLAR),
	POPLAR_HYBRID(74, 1209, "Hybrid poplar", CfsTreeGenus.POPLAR),
	POPLAR_OTHER(75, 1210, "Other poplar", CfsTreeGenus.POPLAR),
	POPLAR_BALSAM_LARGETOOTH_EASTERN(
			76, 1211, "Balsam poplar, largetooth aspen and eastern cottonwood", CfsTreeGenus.POPLAR
	), POPLAR_BALSAM_BLACKCOTTONWOOD(77, 1212, "Balsam poplar and black cottonwood", CfsTreeGenus.POPLAR),
	BIRCH(78, 1300, "Birch", CfsTreeGenus.BIRCH), BIRCH_YELLOW(79, 1301, "Yellow birch", CfsTreeGenus.BIRCH),
	/* 80 */
	BIRCH_CHERRY(80, 1302, "Cherry birch", CfsTreeGenus.BIRCH),
	BIRCH_WHITE(81, 1303, "White birch", CfsTreeGenus.BIRCH), BIRCH_GRAY(82, 1304, "Gray birch", CfsTreeGenus.BIRCH),
	BIRCH_ALASKA_PAPER(83, 1305, "Alaska paper birch", CfsTreeGenus.BIRCH),
	BIRCH_MOUNTAIN_PAPER(84, 1306, "Mountain paper birch", CfsTreeGenus.BIRCH),
	BIRCH_OTHER(85, 1307, "Other birch", CfsTreeGenus.BIRCH),
	BIRCH_ALASKA_PAPER_AND_WHITE(86, 1308, "Alaska paper and white birch", CfsTreeGenus.BIRCH),
	BIRCH_EUROPEAN(87, 1309, "European birch", CfsTreeGenus.BIRCH),
	BIRCH_WHITE_AND_GRAY(88, 1310, "White and gray birch", CfsTreeGenus.BIRCH),
	MAPLE(89, 1400, "Maple", CfsTreeGenus.MAPLE),
	/* 90 */
	MAPLE_SUGAR(90, 1401, "Sugar maple", CfsTreeGenus.MAPLE), MAPLE_BLACK(91, 1402, "Black maple", CfsTreeGenus.MAPLE),
	MAPLE_BIGLEAF(92, 1403, "Bigleaf maple", CfsTreeGenus.MAPLE),
	MAPLE_MANITOBA(93, 1404, "Manitoba maple", CfsTreeGenus.MAPLE),
	MAPLE_RED(94, 1405, "Red maple", CfsTreeGenus.MAPLE), MAPLE_SILVER(95, 1406, "Silver maple", CfsTreeGenus.MAPLE),
	MAPLE_NORWAY(96, 1407, "Norway maple", CfsTreeGenus.MAPLE),
	MAPLE_SUGAR_AND_BLACK(97, 1408, "Sugar and black maple", CfsTreeGenus.MAPLE),
	MAPLE_OTHER(98, 1409, "Other maple", CfsTreeGenus.MAPLE),
	MAPLE_STRIPED(99, 1410, "Striped maple", CfsTreeGenus.MAPLE),
	/* 100 */
	MAPLE_MOUNTAIN(100, 1411, "Mountain maple", CfsTreeGenus.MAPLE),
	MAPLE_SILVER_AND_RED(101, 1412, "Silver and red maple", CfsTreeGenus.MAPLE),
	HARDWOOD_OTHER_BROADLEAFOTHER(
			102, 1500, "Other hardwoods/other broad-leaved species", CfsTreeGenus.OTHER_BROAD_LEAVES
	), HARDWOOD_UNSPECIFIED(103, 1550, "Unspecified hardwood species", CfsTreeGenus.UNSPECIFIED_BROAD_LEAVES),
	HICKORY(104, 1600, "Hickory", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HICKORY_BITTERNUT(105, 1601, "Bitternut hickory", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HICKORY_RED(106, 1602, "Red hickory (Pignut hickory)", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HICKORY_SHAGBARK(107, 1603, "Shagbark hickory", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HICKORY_SHELLBARK(108, 1604, "Shellbark hickory", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WALNUT(109, 1700, "Walnut", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 110 */
	BUTTERNUT(110, 1701, "Butternut", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WALNUT_BLACK(111, 1702, "Black walnut", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ALDER(112, 1800, "Alder", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ALDER_SITKA(113, 1801, "Sitka alder", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ALDER_RED(114, 1802, "Red alder", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ALDER_GREEN(115, 1803, "Green alder", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ALDER_MOUNTAIN(116, 1804, "Mountain alder", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ALDER_SPECKLED(117, 1805, "Speckled alder", CfsTreeGenus.OTHER_BROAD_LEAVES),
	IRONWOOD_HOP_HORNBEAN(118, 1900, "Ironwood (hop-hornbeam)", CfsTreeGenus.OTHER_BROAD_LEAVES),
	BLUE_BEECH(119, 1950, "Blue-beech (American hornbeam)", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 120 */
	BEECH(120, 2000, "Beech", CfsTreeGenus.OTHER_BROAD_LEAVES), OAK(121, 2100, "Oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_WHITE(122, 2101, "White oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_SWAMPWHITE(123, 2102, "Swamp white oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_GARRY(124, 2103, "Garry oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_BUR(125, 2104, "Bur oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_PIN(126, 2105, "Pin oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_CHINQUAPIN(127, 2106, "Chinpaquin oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_CHESTNUT(128, 2107, "Chestnut oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_RED(129, 2108, "Red oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 130 */
	OAK_BLACK(130, 2109, "Black oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_NORTHERN_PIN(131, 2110, "Northern pin oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	OAK_SHUMARD(132, 2111, "Shumard oak", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ELM(133, 2200, "Elm", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ELM_WHITE(134, 2201, "White elm", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ELM_SLIPPERY(135, 2202, "Slippery elm", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ELM_ROCK(136, 2203, "Rock elm", CfsTreeGenus.OTHER_BROAD_LEAVES),
	RED_MULBERRY(137, 2300, "Red mulberry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	TULIPTREE(138, 2400, "Tulip-tree", CfsTreeGenus.OTHER_BROAD_LEAVES),
	CUCUMBERTREE(139, 2500, "Cucumber-tree", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 140 */
	SASSAFRAS(140, 2600, "Sassafras", CfsTreeGenus.OTHER_BROAD_LEAVES),
	SYCAMORE(141, 2700, "Sycamore", CfsTreeGenus.OTHER_BROAD_LEAVES),
	CHERRY(142, 2800, "Cherry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	CHERRY_BLACK(143, 2801, "Black cherry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	CHERRY_PIN(144, 2802, "Pin cherry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	CHERRY_BITTER(145, 2803, "Bitter cherry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	CHERRY_CHOKE(146, 2804, "Choke cherry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HONEYLOCUST(147, 2900, "Honey-locust", CfsTreeGenus.OTHER_BROAD_LEAVES),
	BLACKLOCUST(148, 2901, "Black locust", CfsTreeGenus.OTHER_BROAD_LEAVES),
	BASSWOOD(149, 3000, "Basswood", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 150 */
	BLACKGUM(150, 3100, "Black-gum", CfsTreeGenus.OTHER_BROAD_LEAVES),
	DOGWOOD_FLOWERING(151, 3200, "Flowering dogwood", CfsTreeGenus.OTHER_BROAD_LEAVES),
	DOGWOOD_EASTERNFLOWERING(152, 3201, "Eastern flowering dogwood", CfsTreeGenus.OTHER_BROAD_LEAVES),
	DOGWOOD_WESTERNFLOWERING(153, 3202, "Western flowering dogwood", CfsTreeGenus.OTHER_BROAD_LEAVES),
	DOGWOOD_ALTERNATELEAF(154, 3203, "Alternate-leaf dogwood", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ARBUTUS(155, 3300, "Arbutus", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH(156, 3400, "Ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_WHITE(157, 3401, "White ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_BLACK(158, 3402, "Black ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_RED(159, 3403, "Red ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 160 */
	ASH_NORTHERNRED(160, 3404, "Northern red ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_GREEN(161, 3405, "Green ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_BLUE(162, 3406, "Blue ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_OREGON(163, 3407, "Oregon ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	ASH_PUMPKIN(164, 3408, "Pumpkin ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WILLOW(165, 3500, "Willow", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WILLOW_BLACK(166, 3501, "Black willow", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WILLOW_PEACHLEAF(167, 3502, "Peachleaf willow", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WILLOW_PACIFIC(168, 3503, "Pacific willow", CfsTreeGenus.OTHER_BROAD_LEAVES),
	WILLOW_CRACK(169, 3504, "Crack willow", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 170 */
	WILLOW_SHINING(170, 3505, "Shining willow", CfsTreeGenus.OTHER_BROAD_LEAVES),
	KENTUCKY_COFFEE_TREE(171, 3600, "Kentucky coffee tree", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HACKBERRY(172, 3700, "Hackberry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	SERVICEBERRY(173, 3800, "Serviceberry", CfsTreeGenus.OTHER_BROAD_LEAVES),
	BEAKED_HAZEL(174, 3900, "Beaked hazel", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HAWTHORN(175, 3910, "Hawthorn", CfsTreeGenus.OTHER_BROAD_LEAVES),
	COMMON_WINTERBERRY(176, 3920, "Common winterberry (black-alder)", CfsTreeGenus.OTHER_BROAD_LEAVES),
	APPLE(177, 3930, "Apple", CfsTreeGenus.OTHER_BROAD_LEAVES),
	MOUNTAIN_HOLLY(178, 3940, "Mountain-holly", CfsTreeGenus.OTHER_BROAD_LEAVES),
	SUMAC_STAGHORN(179, 3950, "Staghorn sumac", CfsTreeGenus.OTHER_BROAD_LEAVES),
	/* 180 */
	ASH_MOUNTAIN(180, 3960, "Mountain ash", CfsTreeGenus.OTHER_BROAD_LEAVES),
	HARDWOOD_TOLERANT(181, 4000, "Tolerant hardwoods", CfsTreeGenus.UNSPECIFIED_BROAD_LEAVES),
	HARDWOOD_INTOLERANT(182, 5000, "Intolerant hardwoods", CfsTreeGenus.UNSPECIFIED_BROAD_LEAVES);

	private final int index;
	private final int cfsSpeciesNumber;
	private final String cfsSpeciesName;
	private final CfsTreeGenus cfsTreeGenus;

	private CfsTreeSpecies(int index, int cfsSpeciesNumber, String cfsSpeciesName, CfsTreeGenus cfsTreeGenus) {
		this.index = index;
		this.cfsSpeciesNumber = cfsSpeciesNumber;
		this.cfsSpeciesName = cfsSpeciesName;
		this.cfsTreeGenus = cfsTreeGenus;
	}

	@Override
	public int getIndex() {
		return index;
	}

	public int getNumber() {
		return cfsSpeciesNumber;
	}

	public String getName() {
		return cfsSpeciesName;
	}

	public CfsTreeGenus getCfsTreeGenus() {
		return cfsTreeGenus;
	}

	@Override
	public int getOffset() {
		if (this.equals(UNKNOWN)) {
			throw new UnsupportedOperationException(
					MessageFormat.format(
							"Cannot call getIndex on {0} as it's not a standard member of the enumeration", this
					)
			);
		}

		return index;
	}

	@Override
	public String getText() {
		if (this.equals(UNKNOWN)) {
			return "";
		}

		return this.toString();
	}

	/**
	 * Returns the enumeration constant with the given index.
	 *
	 * @param index the value in question
	 * @return the enumeration value, unless no enumeration constant has the given <code>index</code> in which case
	 *         <code>null</code> is returned.
	 */
	public static CfsTreeSpecies forIndex(int index) {
		for (CfsTreeSpecies e : CfsTreeSpecies.values()) {
			if (index == e.index)
				return e;
		}

		return null;
	}

	/**
	 * @return the number of non-housekeeping entries in the enumeration
	 */
	public static int size() {
		return HARDWOOD_INTOLERANT.index - SPRUCE.index + 1;
	}

	public static class Iterator extends EnumIterator<CfsTreeSpecies> {
		public Iterator() {
			super(values(), SPRUCE, HARDWOOD_INTOLERANT);
		}
	}
}
