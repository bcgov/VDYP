package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_AT;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BA;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_BL;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_CWC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_FDC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_FDI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HWC;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_HWI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_LW;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_PLI;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SB;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SS;
import static ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies.SI_SPEC_SW;

import ca.bc.gov.nrs.vdyp.common_calculators.enumerations.SiteIndexSpecies;

/**
 * SiteIndexNames - full names of species and curves.
 */
public class SiteIndexNames {

	public static final String[] siSpeciesName = {
			// SI_SPEC_A
			"Aspen",

			// SI_SPEC_ABAL
			"Silver fir",

			// SI_SPEC_ABCO
			"White fir",

			// SI_SPEC_AC
			"Poplar",

			// SI_SPEC_ACB
			"Balsam poplar",

			// SI_SPEC_ACT
			"Black cottonwood",

			// SI_SPEC_AD
			"Southern cottonwood",

			// SI_SPEC_AH
			"Poplar x cottonwood",

			// SI_SPEC_AT
			"Trembling aspen",

			// SI_SPEC_AX
			"Hybrid poplar",

			// SI_SPEC_B
			"Balsam",

			// SI_SPEC_BA
			"Amabilis fir",

			// SI_SPEC_BB
			"Balsam fir",

			// SI_SPEC_BC
			"White fir",

			// SI_SPEC_BG
			"Grand fir",

			// SI_SPEC_BI
			"Birch",

			// SI_SPEC_BL
			"Subalpine fir",

			// SI_SPEC_BM
			"Shasta red fir",

			// SI_SPEC_BP
			"Noble fir",

			// SI_SPEC_BV
			// "Paper birch", Commented out since removed above

			// SI_SPEC_C
			"Cedar",

			// SI_SPEC_CI
			"Incense-cedar",

			// SI_SPEC_CP
			"Port-Orford-cedar",

			// SI_SPEC_CW
			"Western redcedar",

			// SI_SPEC_CWC
			"Western redcedar (coastal)",

			// SI_SPEC_CWI
			"Western redcedar (interior)",

			// SI_SPEC_CY
			"Yellow-cedar",

			// SI_SPEC_D
			"Alder",

			// SI_SPEC_DG
			"Green/Sitka alder",

			// SI_SPEC_DM
			"Mountain alder",

			// SI_SPEC_DR
			"Red alder",

			// SI_SPEC_E
			"Birch",

			// SI_SPEC_EA
			"Alaska paper birch",

			// SI_SPEC_EB
			"Bog birch",

			// SI_SPEC_EE
			"European birch",

			// SI_SPEC_EP
			"Paper birch",

			// SI_SPEC_ES
			"Silver birch",

			// SI_SPEC_EW
			"Water birch",

			// SI_SPEC_EXP
			"Alaska x paper birch",

			// SI_SPEC_FD
			"Douglas-fir",

			// SI_SPEC_FDC
			"Coastal Douglas-fir",

			// SI_SPEC_FDI
			"Interior Douglas-fir",

			// SI_SPEC_G
			"Dogwood",

			// SI_SPEC_GP
			"Pacific dogwood",

			// SI_SPEC_GR
			"Red-osier dogwood",

			// SI_SPEC_H
			"Hemlock",

			// SI_SPEC_HM
			"Mountain hemlock",

			// SI_SPEC_HW
			"Western hemlock",

			// SI_SPEC_HWC
			"Western hemlock (coastal)",

			// SI_SPEC_HWI
			"Western hemlock (interior)",

			// SI_SPEC_HXM
			"Mountain x western hemlock",

			// SI_SPEC_IG
			"Giant sequoia",

			// SI_SPEC_IS
			"Coast redwood",

			// SI_SPEC_J
			"Juniper",

			// SI_SPEC_JR
			"Rocky mountain juniper",

			// SI_SPEC_K
			"Cascara",

			// SI_SPEC_KC
			"Cascara",

			// SI_SPEC_L
			"Larch",

			// SI_SPEC_LA
			"Alpine larch",

			// SI_SPEC_LE
			"Eastern larch",

			// SI_SPEC_LT
			"Tamarack",

			// SI_SPEC_LW
			"Western larch",

			// SI_SPEC_M
			"Maple",

			// SI_SPEC_MB
			"Bigleaf maple",

			// SI_SPEC_ME
			"Boxelder",

			// SI_SPEC_MN
			"Norway maple",

			// SI_SPEC_MR
			"Rocky mountain maple",

			// SI_SPEC_MS
			"Sycamore maple",

			// SI_SPEC_MV
			"Vine maple",

			// SI_SPEC_OA
			"Incense-cedar",

			// SI_SPEC_OB
			"Giant sequoia",

			// SI_SPEC_OC
			"Redwood",

			// SI_SPEC_OD
			"European mountain ash",

			// SI_SPEC_OE
			"Siberian elm",

			// SI_SPEC_OF
			"Common pear",

			// SI_SPEC_OG
			"Oregon ash",

			// SI_SPEC_P
			"Pine",

			// SI_SPEC_PA
			"Whitebark pine",

			// SI_SPEC_PF
			"Limber pine",

			// SI_SPEC_PJ
			"Jack pine",

			// SI_SPEC_PL
			"Lodgepole pine",

			// SI_SPEC_PLC
			"Shore pine",

			// SI_SPEC_PLI
			"Lodgepole pine",

			// SI_SPEC_PM
			"Monterey pine",

			// SI_SPEC_PR
			"Red pine",

			// SI_SPEC_PS
			"Sugar pine",

			// SI_SPEC_PW
			"Western white pine",

			// SI_SPEC_PXJ
			"Lodgepole x jack pine",

			// SI_SPEC_PY
			"Ponderosa pine",

			// SI_SPEC_Q
			"Oak",

			// SI_SPEC_QE
			"English oak",

			// SI_SPEC_QG
			"Garry oak",

			// SI_SPEC_R
			"Arbutus",

			// SI_SPEC_RA
			"Arbutus",

			// SI_SPEC_S
			"Spruce",

			// SI_SPEC_SA
			"Norway spruce",

			// SI_SPEC_SB
			"Black spruce",

			// SI_SPEC_SE
			"Engelmann spruce",

			// SI_SPEC_SI
			"Interior spruce",

			// SI_SPEC_SN
			"Norway spruce",

			// SI_SPEC_SS
			"Sitka spruce",

			// SI_SPEC_SW
			"White spruce",

			// SI_SPEC_SX
			"Spruce hybrid",

			// SI_SPEC_SXB
			"White x black spruce",

			// SI_SPEC_SXE
			"Sitka x Engelmann spruce",

			// SI_SPEC_SXL
			"Sitka x white spruce",

			// SI_SPEC_SXS
			"Sitka x unknown spruce",

			// SI_SPEC_SXW
			"Engelmann x white spruce",

			// SI_SPEC_SXX
			"White spruce hybrid",

			// SI_SPEC_T
			"Yew",

			// SI_SPEC_TW
			"Pacific yew",

			// SI_SPEC_U
			"Apple",

			// SI_SPEC_UA
			"Apple",

			// SI_SPEC_UP
			"Pacific crab apple",

			// SI_SPEC_V
			"Cherry",

			// SI_SPEC_VB
			"Bitter cherry",

			// SI_SPEC_VP
			"Pin cherry",

			// SI_SPEC_VS
			"Sweet cherry",

			// SI_SPEC_VV
			"Choke cherry",

			// SI_SPEC_W
			"Willow",

			// SI_SPEC_WA
			"Peachleaf willow",

			// SI_SPEC_WB
			"Bebb's willow",

			// SI_SPEC_WD
			"Pussy willow",

			// SI_SPEC_WI
			"Willow",

			// SI_SPEC_WP
			"Pacific willow",

			// SI_SPEC_WS
			"Scouler's willow",

			// SI_SPEC_WT
			"Sitka willow",

			// SI_SPEC_X
			"Unknown",

			// SI_SPEC_XC
			"Unknown conifer",

			// SI_SPEC_XH
			"Unknown hardwood",

			// SI_SPEC_Y
			"Yellow-cedar",

			// SI_SPEC_YC
			"Yellow-cedar",

			// SI_SPEC_YP
			"Port-Orford-cedar",

			// SI_SPEC_Z
			"Other",

			// SI_SPEC_ZC
			"Other conifer",

			// SI_SPEC_ZH
			"Other hardwood", };

	public static String[] siCurveName = {
			// SI_ACB_HUANG
			"Huang, Titus, and Lakusta (1994)",

			// SI_ACT_THROWER
			"Thrower (1992)",

			// SI_AT_HUANG
			"Huang, Titus, and Lakusta (1994)",

			// SI_AT_CIESZEWSKI
			"Cieszewski and Bella (1991)",

			// SI_AT_GOUDIE
			"Alberta Forest Service (1985)",

			// SI_BA_DILUCCA
			"Di Lucca (1992)",

			// SI_BB_KER
			"Ker and Bowling (1991)",

			// SI_BA_KURUCZ86
			"Kurucz (1986)",

			// SI_BA_KURUCZ82
			"Kurucz (1982)",

			// SI_BB_HUANG
			"Huang, Titus, and Lakusta (1994)",

			// SI_BG_KURUCZ82
			"uses Ba Kurucz (1982)",

			// SI_BG_COCHRAN
			"Cochran (1979)",

			// SI_BL_THROWERGI
			"Thrower (1997)",

			// SI_BL_KURUCZ82
			"uses Ba Kurucz (1982)",

			// SI_CWC_KURUCZ
			"Kurucz (1985)",

			// SI_CWC_BARKER
			"Barker (1983)",

			// SI_DR_NIGH
			"Nigh and Courtin (1998)",

			// SI_DR_HARRING
			"Harrington and Curtis (1986)",

			// SI_EA_GOUDIE
			"uses At Alberta Forest Service (1985)",

			// SI_EP_CAMERON
			"Cameron (1999)",

			// SI_EP_CHEN
			"Chen (1999)",

			// SI_EP_GOUDIE
			"uses At Alberta Forest Service (1985)",

			// SI_FDC_NIGHGI
			"Nigh (1997)",

			// SI_FDC_BRUCE
			"Bruce (1981)",

			// SI_FDC_COCHRAN
			"Cochran (1979)",

			// SI_FDC_KING
			"King (1966)",

			// SI_FDI_NIGHGI
			"Nigh (1997)",

			// SI_FDI_HUANG_PLA
			"Huang, Titus, and Lakusta (1994) (pla)",

			// SI_FDI_HUANG_NAT
			"Huang, Titus, and Lakusta (1994) (nat)",

			// SI_FDI_MILNER
			"Milner (1992)",

			// SI_FDI_THROWER
			"Thrower and Goudie (1992)",

			// SI_FDI_VDP_MONT
			"Vander Ploeg and Moore (1989) Montana",

			// SI_FDI_VDP_WASH
			"Vander Ploeg and Moore (1989) Washington",

			// SI_FDI_MONS_DF
			"Monserud (1984) Fd habitat",

			// SI_FDI_MONS_GF
			"Monserud (1984) Bg habitat",

			// SI_FDI_MONS_WRC
			"Monserud (1984) Cw habitat",

			// SI_FDI_MONS_WH
			"Monserud (1984) Hw habitat",

			// SI_FDI_MONS_SAF
			"Monserud (1984) Bl habitat",

			// SI_HM_WILEY
			"uses Hwc Wiley (1978)",

			// SI_HWC_NIGHGI
			"Nigh (1995)",

			// SI_HWC_FARR
			"Farr (1984)",

			// SI_HWC_BARKER
			"Barker (1983)",

			// SI_HWC_WILEY
			"Wiley (1978)",

			// SI_HWC_WILEY_BC
			"Wiley (1978) BC adj.",

			// SI_HWC_WILEY_MB
			"Wiley (1978) BC,MB adj.",

			// SI_HWI_NIGH
			"Nigh (1998)",

			// SI_HWI_NIGHGI
			"Nigh (1998)",

			// SI_LA_MILNER
			"uses Lw Milner (1992)",

			// SI_LT_MILNER
			"uses Lw Milner (1992)",

			// SI_LW_MILNER
			"Milner (1992)",

			// SI_MB_THROWER
			"uses Act Thrower (1992)",

			// SI_PA_GOUDIE_DRY
			"uses Pli Goudie (1984) (dry site)",

			// SI_PA_GOUDIE_WET
			"uses Pli Goudie (1984) (wet site)",

			// SI_PF_GOUDIE_DRY
			"uses Pli Goudie (1984) (dry site)",

			// SI_PF_GOUDIE_WET
			"uses Pli Goudie (1984) (wet site)",

			// SI_PJ_HUANG_PLA
			"Huang, Titus, and Lakusta (1994) (pla)",

			// SI_PJ_HUANG_NAT
			"Huang, Titus, and Lakusta (1994) (nat)",

			// SI_PLI_THROWNIGH
			"Nigh (1999)",

			// SI_PLI_NIGHTA98
			"Nigh and Love (1999)",

			// SI_PLI_NIGHGI97
			"Nigh (1997)",

			// SI_PLI_NIGHGI
			"Nigh (1995)",

			// SI_PLI_HUANG_PLA
			"Huang, Titus, and Lakusta (1994) (pla)",

			// SI_PLI_HUANG_NAT
			"Huang, Titus, and Lakusta (1994) (nat)",

			// SI_PLI_THROWER
			"Thrower (1994)",

			// SI_PLI_MILNER
			"Milner (1992)",

			// SI_PLI_CIESZEWSKI
			"Cieszewski and Bella (1991)",

			// SI_PLI_GOUDIE_DRY
			"Goudie (1984) (dry site)",

			// SI_PLI_GOUDIE_WET
			"Goudie (1984) (wet site)",

			// SI_PLI_DEMPSTER
			"Alberta Forest Service (1985)",

			// SI_PW_CURTIS
			"Curtis, Diaz, and Clendenen (1990)",

			// SI_PY_MILNER
			"Milner (1992)",

			// SI_PY_HANN
			"Hann and Scrivani (1986)",

			// SI_SB_HUANG
			"Huang, Titus, and Lakusta (1994)",

			// SI_SB_CIESZEWSKI
			"Cieszewski and Bella (1991)",

			// SI_SB_KER
			"Ker and Bowling (1991)",

			// SI_SB_DEMPSTER
			"Alberta Forest Service (1985)",

			// SI_SE_GOUDIE_PLA
			"uses Sw Goudie (1984) (plantation)",

			// SI_SE_GOUDIE_NAT
			"uses Sw Goudie (1984) (natural)",

			// SI_SS_NIGHGI
			"Nigh (1996)",

			// SI_SS_NIGH
			"Nigh (1997)",

			// SI_SS_GOUDIE
			"Barker and Goudie (1987)",

			// SI_SS_FARR
			"Farr (1984)",

			// SI_SS_BARKER
			"Barker (1983)",

			// SI_SW_NIGHGI
			"Nigh (1995)",

			// SI_SW_HUANG_PLA
			"Huang, Titus, and Lakusta (1994) (pla)",

			// SI_SW_HUANG_NAT
			"Huang, Titus, and Lakusta (1994) (nat)",

			// SI_SW_THROWER
			"Thrower (1994)",

			// SI_SW_CIESZEWSKI
			"Cieszewski and Bella (1991)",

			// SI_SW_KER_PLA
			"Ker and Bowling (1991) (plantation)",

			// SI_SW_KER_NAT
			"Ker and Bowling (1991) (natural)",

			// SI_SW_GOUDIE_PLA
			"Goudie (1984) (plantation)",

			// SI_SW_GOUDIE_NAT
			"Goudie (1984) (natural)",

			// SI_SW_DEMPSTER
			"Alberta Forest Service (1985)",

			// SI_TE_GOUDIE
			"Goudie (1995)",

			// SI_YC_KURUCZ
			"uses Cw Kurucz (1985)",

			// SI_BL_CHEN
			"Chen and Klinka (2000)",

			// SI_AT_CHEN
			"Chen (1997)",

			// SI_DR_CHEN
			"Chen (1999)",

			// SI_PL_CHEN
			"Chen and Klinka (2000)",

			// SI_CWI_NIGH
			"Nigh (2000)",

			// SI_BP_CURTIS
			"Curtis (1990)",

			// SI_HWC_NIGHGI99
			"Nigh (1999)",

			// SI_SS_NIGHGI99
			"Nigh (1999)",

			// SI_SW_NIGHGI99
			"Nigh (1999)",

			// SI_LW_NIGHGI
			"Nigh (1999)",

			// SI_SW_NIGHTA
			"Nigh and Love (2000)",

			// SI_CWI_NIGHGI
			"Nigh (2000)",

			// SI_SW_GOUDNIGH
			"Nigh/Love (2000) + Goudie (1984) (pla)",

			// SI_HM_MEANS
			"Means, Campbell, Johnson (1988)",

			// SI_SE_CHEN
			"Chen and Klinka (2000)",

			// SI_FDC_NIGHTA
			"Nigh and Mitchell (2002)",

			// SI_FDC_BRUCENIGH
			"Nigh and Mitchell (2002) + Bruce (1981)",

			// SI_LW_NIGH
			"Brisco, Klinka, and Nigh 2002",

			// SI_SB_NIGH
			"Nigh, Krestov, and Klinka 2002",

			// SI_AT_NIGH
			"Nigh, Krestov, and Klinka 2002",

			// SI_BL_CHENAC
			"Chen and Klinka (2000ac)",

			// SI_BP_CURTISAC
			"Curtis (1990ac)",

			// SI_HM_MEANSAC
			"Means, Campbell, Johnson (1988ac)",

			// SI_FDI_THROWERAC
			"Thrower and Goudie (1992ac)",

			// SI_ACB_HUANGAC
			"Huang, Titus, and Lakusta (1994ac)",

			// SI_PW_CURTISAC
			"Curtis, Diaz, and Clendenen (1990ac)",

			// SI_HWC_WILEYAC
			"Wiley (1978ac)",

			// SI_FDC_BRUCEAC
			"Bruce (1981ac)",

			// SI_CWC_KURUCZAC
			"Kurucz (1985ac)",

			// SI_BA_KURUCZ82AC
			"Kurucz (1982ac)",

			// SI_ACT_THROWERAC
			"Thrower (1992ac)",

			// SI_PY_HANNAC
			"Hann and Scrivani (1986ac)",

			// SI_SE_CHENAC
			"Chen and Klinka (2000ac)",

			// SI_SW_GOUDIE_NATAC
			"Goudie (1984ac) (natural)",

			// SI_PY_NIGH
			"Nigh (2002)",

			// SI_PY_NIGHGI
			"Nigh (2002)",

			// SI_PLI_NIGHTA2004
			"Nigh (2004)",

			// SI_SW_NIGHTA2004
			"Nigh (2004)",

			// SI_SE_NIGHTA
			"Nigh (2004)",

			// SI_SW_GOUDIE_PLAAC
			"Goudie (1984ac) (plantation)",

			// SI_PJ_HUANG
			"Huang (1997)",

			// SI_PJ_HUANGAC
			"Huang (1997ac)",

			// SI_SW_NIGHGI2004
			"Nigh (2004)",

			// SI_EP_NIGH
			"Nigh (2009)",

			// SI_BA_NIGHGI
			"Nigh (2009)",

			// SI_BA_NIGH
			"Nigh (2009)",

			// SI_SW_HU_GARCIA
			"Hu and Garcia (2010)",

			// SI_SE_NIGHGI
			"Nigh (2014)",

			// SI_SE_NIGH
			"Nigh (2015)",

			// SI_CWC_NIGH
			"Nigh (2016)", };

	/**
	 * Site index conversion between species. Here's how to use the following array: The four elements are: reference
	 * species, target species, coeff_a, coeff_b.
	 * <p>
	 * Target_SI = coeff_a + coeff_b * Reference_SI
	 */

	public record SpeciesConversionParametersDetails(
			SiteIndexSpecies sourceSpecies, SiteIndexSpecies targetSpecies, double param1, double param2
	) {
	}

	public static final SpeciesConversionParametersDetails[] siSpeciesConversionParameters = {
			new SpeciesConversionParametersDetails(SI_SPEC_AT, SI_SPEC_SW, 3.804, 0.7978), //
			new SpeciesConversionParametersDetails(SI_SPEC_BA, SI_SPEC_HWC, 2.005, 1.014), //
			new SpeciesConversionParametersDetails(SI_SPEC_CWC, SI_SPEC_HWC, 1.256, 1.048), //
			new SpeciesConversionParametersDetails(SI_SPEC_FDC, SI_SPEC_HWC, -0.432, 0.899), //
			new SpeciesConversionParametersDetails(SI_SPEC_HWC, SI_SPEC_BA, -1.97731755, 0.98619329), //
			new SpeciesConversionParametersDetails(SI_SPEC_HWC, SI_SPEC_CWC, -1.19847328, 0.95419847), //
			new SpeciesConversionParametersDetails(SI_SPEC_HWC, SI_SPEC_FDC, 0.48053393, 1.11234705), //
			new SpeciesConversionParametersDetails(SI_SPEC_HWC, SI_SPEC_SS, -4.94382022, 1.24843945), //
			new SpeciesConversionParametersDetails(SI_SPEC_HWI, SI_SPEC_FDI, 4.56, 0.887), //
			new SpeciesConversionParametersDetails(SI_SPEC_SS, SI_SPEC_HWC, 3.96, 0.801), //
			new SpeciesConversionParametersDetails(SI_SPEC_PLI, SI_SPEC_SW, -2.14130435, 1.08695652), //
			new SpeciesConversionParametersDetails(SI_SPEC_PLI, SI_SPEC_FDI, 0.70841121, 0.93457944), //
			new SpeciesConversionParametersDetails(SI_SPEC_PLI, SI_SPEC_BL, 0.47431193, 0.91743119), //
			new SpeciesConversionParametersDetails(SI_SPEC_PLI, SI_SPEC_LW, 1.92307692, 0.96153846), //
			new SpeciesConversionParametersDetails(SI_SPEC_PLI, SI_SPEC_SB, 2.76436782, 0.6385696), //
			new SpeciesConversionParametersDetails(SI_SPEC_SB, SI_SPEC_PLI, -4.329, 1.566), //
			new SpeciesConversionParametersDetails(SI_SPEC_SW, SI_SPEC_AT, -4.768112309, 1.253446979), //
			new SpeciesConversionParametersDetails(SI_SPEC_SW, SI_SPEC_PLI, 1.97, 0.92), //
			new SpeciesConversionParametersDetails(SI_SPEC_SW, SI_SPEC_FDI, 4.75, 0.737), //
			new SpeciesConversionParametersDetails(SI_SPEC_SW, SI_SPEC_BL, 1.68, 0.86), //
			new SpeciesConversionParametersDetails(SI_SPEC_FDI, SI_SPEC_PLI, -0.758, 1.07), //
			new SpeciesConversionParametersDetails(SI_SPEC_FDI, SI_SPEC_SW, -6.44504749, 1.3568521), //
			new SpeciesConversionParametersDetails(SI_SPEC_FDI, SI_SPEC_HWI, -5.14092446, 1.12739572), //
			new SpeciesConversionParametersDetails(SI_SPEC_FDI, SI_SPEC_LW, 0.70193286, 1.017294), //
			new SpeciesConversionParametersDetails(SI_SPEC_BL, SI_SPEC_PLI, -0.517, 1.09), //
			new SpeciesConversionParametersDetails(SI_SPEC_BL, SI_SPEC_SW, -1.95348837, 1.1627907), //
			new SpeciesConversionParametersDetails(SI_SPEC_LW, SI_SPEC_PLI, -2, 1.04), //
			new SpeciesConversionParametersDetails(SI_SPEC_LW, SI_SPEC_FDI, -0.69, 0.983) };

	/**
	 * Indicates what equations are available (these are additive):
	 * <ul>
	 * <li>1: ht = fn (si, age)
	 * <li>2: si = fn (ht, age)
	 * <li>4: y2bh = fn (si)
	 * <li>8: si = fn (ht, age) (growth intercept)
	 * </ul>
	 */
	public static char[] siCurveAvailableTypes = {
			// SI_ACB_HUANG
			5,

			// SI_ACT_THROWER
			5,

			// SI_AT_HUANG
			5,

			// SI_AT_CIESZEWSKI
			5,

			// SI_AT_GOUDIE
			7,

			// SI_BA_DILUCCA
			3,

			// SI_BB_KER
			5,

			// SI_BA_KURUCZ86
			5,

			// SI_BA_KURUCZ82
			5,

			// SI_BB_HUANG
			5,

			// SI_BG_KURUCZ82
			5,

			// SI_BG_COCHRAN
			5,

			// SI_BL_THROWERGI
			8,

			// SI_BL_KURUCZ82
			5,

			// SI_CWC_KURUCZ
			5,

			// SI_CWC_BARKER
			5,

			// SI_DR_NIGH
			7,

			// SI_DR_HARRING
			5,

			// SI_EA_GOUDIE
			3,

			// SI_EP_CAMERON
			5,

			// SI_EP_CHEN
			5,

			// SI_EP_GOUDIE
			3,

			// SI_FDC_NIGHGI
			8,

			// SI_FDC_BRUCE
			5,

			// SI_FDC_COCHRAN
			5,

			// SI_FDC_KING
			5,

			// SI_FDI_NIGHGI
			8,

			// SI_FDI_HUANG_PLA
			5,

			// SI_FDI_HUANG_NAT
			5,

			// SI_FDI_MILNER
			3,

			// SI_FDI_THROWER
			7,

			// SI_FDI_VDP_MONT
			7,

			// SI_FDI_VDP_WASH
			7,

			// SI_FDI_MONS_DF
			7,

			// SI_FDI_MONS_GF
			7,

			// SI_FDI_MONS_WRC
			7,

			// SI_FDI_MONS_WH
			7,

			// SI_FDI_MONS_SAF
			7,

			// SI_HM_WILEY
			5,

			// SI_HWC_NIGHGI
			8,

			// SI_HWC_FARR
			5,

			// SI_HWC_BARKER
			5,

			// SI_HWC_WILEY
			5,

			// SI_HWC_WILEY_BC
			5,

			// SI_HWC_WILEY_MB
			5,

			// SI_HWI_NIGH
			5,

			// SI_HWI_NIGHGI
			8,

			// SI_LA_MILNER
			7,

			// SI_LT_MILNER
			7,

			// SI_LW_MILNER
			7,

			// SI_MB_THROWER
			5,

			// SI_PA_GOUDIE_DRY
			5,

			// SI_PA_GOUDIE_WET
			5,

			// SI_PF_GOUDIE_DRY
			5,

			// SI_PF_GOUDIE_WET
			5,

			// SI_PJ_HUANG_PLA
			5,

			// SI_PJ_HUANG_NAT
			5,

			// SI_PLI_THROWNIGH
			5,

			// SI_PLI_NIGHTA98
			5,

			// SI_PLI_NIGHGI97
			8,

			// SI_PLI_NIGHGI
			8,

			// SI_PLI_HUANG_PLA
			5,

			// SI_PLI_HUANG_NAT
			5,

			// SI_PLI_THROWER
			7,

			// SI_PLI_MILNER
			3,

			// SI_PLI_CIESZEWSKI
			5,

			// SI_PLI_GOUDIE_DRY
			5,

			// SI_PLI_GOUDIE_WET
			5,

			// SI_PLI_DEMPSTER
			7,

			// SI_PW_CURTIS
			3,

			// SI_PY_MILNER
			3,

			// SI_PY_HANN
			5,

			// SI_SB_HUANG
			5,

			// SI_SB_CIESZEWSKI
			5,

			// SI_SB_KER
			5,

			// SI_SB_DEMPSTER
			7,

			// SI_SE_GOUDIE_PLA
			5,

			// SI_SE_GOUDIE_NAT
			5,

			// SI_SS_NIGHGI
			8,

			// SI_SS_NIGH
			5,

			// SI_SS_GOUDIE
			5,

			// SI_SS_FARR
			5,

			// SI_SS_BARKER
			5,

			// SI_SW_NIGHGI
			8,

			// SI_SW_HUANG_PLA
			5,

			// SI_SW_HUANG_NAT
			5,

			// SI_SW_THROWER
			5,

			// SI_SW_CIESZEWSKI
			5,

			// SI_SW_KER_PLA
			5,

			// SI_SW_KER_NAT
			5,

			// SI_SW_GOUDIE_PLA
			5,

			// SI_SW_GOUDIE_NAT
			5,

			// SI_SW_DEMPSTER
			7,

			// SI_TE_GOUDIE
			5,

			// SI_YC_KURUCZ
			5,

			// SI_BL_CHEN
			5,

			// SI_AT_CHEN
			5,

			// SI_DR_CHEN
			5,

			// SI_PL_CHEN
			5,

			// SI_CWI_NIGH
			5,

			// SI_BP_CURTIS
			5,

			// SI_HWC_NIGHGI99
			8,

			// SI_SS_NIGHGI99
			8,

			// SI_SW_NIGHGI99
			8,

			// SI_LW_NIGHGI
			8,

			// SI_SW_NIGHTA
			5,

			// SI_CWI_NIGHGI
			8,

			// SI_SW_GOUDNIGH
			5,

			// SI_HM_MEANS
			7,

			// SI_SE_CHEN
			5,

			// SI_FDC_NIGHTA
			5,

			// SI_FDC_BRUCENIGH
			5,

			// SI_LW_NIGH
			5,

			// SI_SB_NIGH
			5,

			// SI_AT_NIGH
			5,

			// SI_BL_CHENAC
			5,

			// SI_BP_CURTISAC
			5,

			// SI_HM_MEANSAC
			7,

			// SI_FDI_THROWERAC
			7,

			// SI_ACB_HUANGAC
			5,

			// SI_PW_CURTISAC
			3,

			// SI_HWC_WILEYAC
			5,

			// SI_FDC_BRUCEAC
			5,

			// SI_CWC_KURUCZAC
			5,

			// SI_BA_KURUCZ82AC
			5,

			// SI_ACT_THROWERAC
			5,

			// SI_PY_HANNAC
			5,

			// SI_SE_CHENAC
			5,

			// SI_SW_GOUDIE_NATAC
			5,

			// SI_PY_NIGH
			5,

			// SI_PY_NIGHGI
			8,

			// SI_PLI_NIGHTA2004
			5,

			// SI_SW_NIGHTA2004
			5,

			// SI_SE_NIGHTA
			5,

			// SI_SW_GOUDIE_PLAAC
			5,

			// SI_PJ_HUANG
			5,

			// SI_PJ_HUANGAC
			5,

			// SI_SW_NIGHGI2004
			8,

			// SI_EP_NIGH
			5,

			// SI_BA_NIGHGI
			8,

			// SI_BA_NIGH
			5,

			// SI_SW_HU_GARCIA
			7,

			// SI_SE_NIGHGI
			8,

			// SI_SE_NIGH
			5,

			// SI_CWC_NIGH
			5,

	};

	/**
	 * Height(m) of breast height (typically 1.3, 1.37, 1.3716).
	 */
	public static double[] siCurveBreastHeight = {
			// SI_ACB_HUANG
			1.3,

			// SI_ACT_THROWER
			1.3,

			// SI_AT_HUANG
			1.3,

			// SI_AT_CIESZEWSKI
			1.3,

			// SI_AT_GOUDIE
			1.3,

			// SI_BA_DILUCCA
			1.3,

			// SI_BB_KER
			1.3,

			// SI_BA_KURUCZ86
			1.3,

			// SI_BA_KURUCZ82
			1.3,

			// SI_BB_HUANG
			1.3,

			// SI_BG_KURUCZ82
			1.3,

			// SI_BG_COCHRAN
			1.37,

			// SI_BL_THROWERGI
			1.3,

			// SI_BL_KURUCZ82
			1.3,

			// SI_CWC_KURUCZ
			1.3,

			// SI_CWC_BARKER
			1.3,

			// SI_DR_NIGH
			1.3,

			// SI_DR_HARRING
			1.3,

			// SI_EA_GOUDIE
			1.3,

			// SI_EP_CAMERON
			1.3,

			// SI_EP_CHEN
			1.3,

			// SI_EP_GOUDIE
			1.3,

			// SI_FDC_NIGHGI
			1.3,

			// SI_FDC_BRUCE
			1.37,

			// SI_FDC_COCHRAN
			1.37,

			// SI_FDC_KING
			1.37,

			// SI_FDI_NIGHGI
			1.3,

			// SI_FDI_HUANG_PLA
			1.3,

			// SI_FDI_HUANG_NAT
			1.3,

			// SI_FDI_MILNER
			1.37,

			// SI_FDI_THROWER
			1.3,

			// SI_FDI_VDP_MONT
			1.37,

			// SI_FDI_VDP_WASH
			1.37,

			// SI_FDI_MONS_DF
			1.37,

			// SI_FDI_MONS_GF
			1.37,

			// SI_FDI_MONS_WRC
			1.37,

			// SI_FDI_MONS_WH
			1.37,

			// SI_FDI_MONS_SAF
			1.37,

			// SI_HM_WILEY
			1.37,

			// SI_HWC_NIGHGI
			1.3,

			// SI_HWC_FARR
			1.37,

			// SI_HWC_BARKER
			1.3,

			// SI_HWC_WILEY
			1.37,

			// SI_HWC_WILEY_BC
			1.37,

			// SI_HWC_WILEY_MB
			1.37,

			// SI_HWI_NIGH
			1.3,

			// SI_HWI_NIGHGI
			1.3,

			// SI_LA_MILNER
			1.37,

			// SI_LT_MILNER
			1.37,

			// SI_LW_MILNER
			1.37,

			// SI_MB_THROWER
			1.3,

			// SI_PA_GOUDIE_DRY
			1.3,

			// SI_PA_GOUDIE_WET
			1.3,

			// SI_PF_GOUDIE_DRY
			1.3,

			// SI_PF_GOUDIE_WET
			1.3,

			// SI_PJ_HUANG_PLA
			1.3,

			// SI_PJ_HUANG_NAT
			1.3,

			// SI_PLI_THROWNIGH
			1.3,

			// SI_PLI_NIGHTA98
			1.3,

			// SI_PLI_NIGHGI97
			1.3,

			// SI_PLI_NIGHGI
			1.3,

			// SI_PLI_HUANG_PLA
			1.3,

			// SI_PLI_HUANG_NAT
			1.3,

			// SI_PLI_THROWER
			1.3,

			// SI_PLI_MILNER
			1.37,

			// SI_PLI_CIESZEWSKI
			1.3,

			// SI_PLI_GOUDIE_DRY
			1.3,

			// SI_PLI_GOUDIE_WET
			1.3,

			// SI_PLI_DEMPSTER
			1.3,

			// SI_PW_CURTIS
			1.37,

			// SI_PY_MILNER
			1.37,

			// SI_PY_HANN
			1.37,

			// SI_SB_HUANG
			1.3,

			// SI_SB_CIESZEWSKI
			1.3,

			// SI_SB_KER
			1.3,

			// SI_SB_DEMPSTER
			1.3,

			// SI_SE_GOUDIE_PLA
			1.3,

			// SI_SE_GOUDIE_NAT
			1.3,

			// SI_SS_NIGHGI
			1.3,

			// SI_SS_NIGH
			1.3,

			// SI_SS_GOUDIE
			1.3,

			// SI_SS_FARR
			1.37,

			// SI_SS_BARKER
			1.3,

			// SI_SW_NIGHGI
			1.3,

			// SI_SW_HUANG_PLA
			1.3,

			// SI_SW_HUANG_NAT
			1.3,

			// SI_SW_THROWER
			1.3,

			// SI_SW_CIESZEWSKI
			1.3,

			// SI_SW_KER_PLA
			1.3,

			// SI_SW_KER_NAT
			1.3,

			// SI_SW_GOUDIE_PLA
			1.3,

			// SI_SW_GOUDIE_NAT
			1.3,

			// SI_SW_DEMPSTER
			1.3,

			// SI_TE_GOUDIE
			1.3,

			// SI_YC_KURUCZ
			1.3,

			// SI_BL_CHEN
			1.3,

			// SI_AT_CHEN
			1.3,

			// SI_DR_CHEN
			1.3,

			// SI_PL_CHEN
			1.3,

			// SI_CWI_NIGH
			1.3,

			// SI_BP_CURTIS
			1.37,

			// SI_HWC_NIGHGI99
			1.3,

			// SI_SS_NIGHGI99
			1.3,

			// SI_SW_NIGHGI99
			1.3,

			// SI_LW_NIGHGI
			1.3,

			// SI_SW_NIGHTA
			1.3,

			// SI_CWI_NIGHGI
			1.3,

			// SI_SW_GOUDNIGH
			1.3,

			// SI_HM_MEANS
			1.37,

			// SI_SE_CHEN
			1.3,

			// SI_FDC_NIGHTA
			1.3,

			// SI_FDC_BRUCENIGH
			1.37,

			// SI_LW_NIGH
			1.3,

			// SI_SB_NIGH
			1.3,

			// SI_AT_NIGH
			1.3,

			// SI_BL_CHENAC
			1.3,

			// SI_BP_CURTISAC
			1.37,

			// SI_HM_MEANSAC
			1.37,

			// SI_FDI_THROWERAC
			1.3,

			// SI_ACB_HUANGAC
			1.3,

			// SI_PW_CURTISAC
			1.37,

			// SI_HWC_WILEYAC
			1.37,

			// SI_FDC_BRUCEAC
			1.37,

			// SI_CWC_KURUCZAC
			1.3,

			// SI_BA_KURUCZ82AC
			1.3,

			// SI_ACT_THROWERAC
			1.3,

			// SI_PY_HANNAC
			1.37,

			// SI_SE_CHENAC
			1.3,

			// SI_SW_GOUDIE_NATAC
			1.3,

			// SI_PY_NIGH
			1.3,

			// SI_PY_NIGHGI
			1.3,

			// SI_PLI_NIGHTA2004
			1.3,

			// SI_SW_NIGHTA2004
			1.3,

			// SI_SE_NIGHTA
			1.3,

			// SI_SW_GOUDIE_PLAAC
			1.3,

			// SI_PJ_HUANG
			1.3,

			// SI_PJ_HUANGAC
			1.3,

			// SI_SW_NIGHGI2004
			1.3,

			// SI_EP_NIGH
			1.3,

			// SI_BA_NIGHGI
			1.3,

			// SI_BA_NIGH
			1.3,

			// SI_SW_HU_GARCIA
			1.3,

			// SI_SE_NIGHGI
			1.3,

			// SI_SE_NIGH
			1.3,

			// SI_CWC_NIGH
			1.3 };
}
