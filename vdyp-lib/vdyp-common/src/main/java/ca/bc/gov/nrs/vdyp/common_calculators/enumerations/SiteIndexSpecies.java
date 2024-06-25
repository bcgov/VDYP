package ca.bc.gov.nrs.vdyp.common_calculators.enumerations;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.CodeErrorException;

public enum SiteIndexSpecies {
	SI_NO_SPECIES(-1, ""), //
	SI_SPEC_A(0, "A"), //
	SI_SPEC_ABAL(1, "Abal"), //
	SI_SPEC_ABCO(2, "Abco"), //
	SI_SPEC_AC(3, "Ac"), //
	SI_SPEC_ACB(4, "Acb"), //
	SI_SPEC_ACT(5, "Act"), //
	SI_SPEC_AD(6, "Ad"), //
	SI_SPEC_AH(7, "Ah"), //
	SI_SPEC_AT(8, "At"), //
	SI_SPEC_AX(9, "Ax"), //
	SI_SPEC_B(10, "B"), //
	SI_SPEC_BA(11, "Ba"), //
	SI_SPEC_BB(12, "Bb"), //
	SI_SPEC_BC(13, "Bc"), //
	SI_SPEC_BG(14, "Bg"), //
	SI_SPEC_BI(15, "Bi"), //
	SI_SPEC_BL(16, "Bl"), //
	SI_SPEC_BM(17, "Bm"), //
	SI_SPEC_BP(18, "Bp"), //
	SI_SPEC_C(19, "C"), //
	SI_SPEC_CI(20, "Ci"), //
	SI_SPEC_CP(21, "Cp"), //
	SI_SPEC_CW(22, "Cw"), //
	SI_SPEC_CWC(23, "Cwc"), //
	SI_SPEC_CWI(24, "Cwi"), //
	SI_SPEC_CY(25, "Cy"), //
	SI_SPEC_D(26, "D"), //
	SI_SPEC_DG(27, "Dg"), //
	SI_SPEC_DM(28, "Dm"), //
	SI_SPEC_DR(29, "Dr"), //
	SI_SPEC_E(30, "E"), //
	SI_SPEC_EA(31, "Ea"), //
	SI_SPEC_EB(32, "Eb"), //
	SI_SPEC_EE(33, "Ee"), //
	SI_SPEC_EP(34, "Ep"), //
	SI_SPEC_ES(35, "Es"), //
	SI_SPEC_EW(36, "Ew"), //
	SI_SPEC_EXP(37, "Exp"), //
	SI_SPEC_FD(38, "Fd"), //
	SI_SPEC_FDC(39, "Fdc"), //
	SI_SPEC_FDI(40, "Fdi"), //
	SI_SPEC_G(41, "G"), //
	SI_SPEC_GP(42, "Gp"), //
	SI_SPEC_GR(43, "Gr"), //
	SI_SPEC_H(44, "H"), //
	SI_SPEC_HM(45, "Hm"), //
	SI_SPEC_HW(46, "Hw"), //
	SI_SPEC_HWC(47, "Hwc"), //
	SI_SPEC_HWI(48, "Hwi"), //
	SI_SPEC_HXM(49, "Hxm"), //
	SI_SPEC_IG(50, "Ig"), //
	SI_SPEC_IS(51, "Is"), //
	SI_SPEC_J(52, "J"), //
	SI_SPEC_JR(53, "Jr"), //
	SI_SPEC_K(54, "K"), //
	SI_SPEC_KC(55, "Kc"), //
	SI_SPEC_L(56, "L"), //
	SI_SPEC_LA(57, "La"), //
	SI_SPEC_LE(58, "Le"), //
	SI_SPEC_LT(59, "Lt"), //
	SI_SPEC_LW(60, "Lw"), //
	SI_SPEC_M(61, "M"), //
	SI_SPEC_MB(62, "Mb"), //
	SI_SPEC_ME(63, "Me"), //
	SI_SPEC_MN(64, "Mn"), //
	SI_SPEC_MR(65, "Mr"), //
	SI_SPEC_MS(66, "Ms"), //
	SI_SPEC_MV(67, "Mv"), //
	SI_SPEC_OA(68, "Oa"), //
	SI_SPEC_OB(69, "Ob"), //
	SI_SPEC_OC(70, "Oc"), //
	SI_SPEC_OD(71, "Od"), //
	SI_SPEC_OE(72, "Oe"), //
	SI_SPEC_OF(73, "Of"), //
	SI_SPEC_OG(74, "Og"), //
	SI_SPEC_P(75, "P"), //
	SI_SPEC_PA(76, "Pa"), //
	SI_SPEC_PF(77, "Pf"), //
	SI_SPEC_PJ(78, "Pj"), //
	SI_SPEC_PL(79, "Pl"), //
	SI_SPEC_PLC(80, "Plc"), //
	SI_SPEC_PLI(81, "Pli"), //
	SI_SPEC_PM(82, "Pm"), //
	SI_SPEC_PR(83, "Pr"), //
	SI_SPEC_PS(84, "Ps"), //
	SI_SPEC_PW(85, "Pw"), //
	SI_SPEC_PXJ(86, "Pxj"), //
	SI_SPEC_PY(87, "Py"), //
	SI_SPEC_Q(88, "Q"), //
	SI_SPEC_QE(89, "Qe"), //
	SI_SPEC_QG(90, "Qg"), //
	SI_SPEC_R(91, "R"), //
	SI_SPEC_RA(92, "Ra"), //
	SI_SPEC_S(93, "S"), //
	SI_SPEC_SA(94, "Sa"), //
	SI_SPEC_SB(95, "Sb"), //
	SI_SPEC_SE(96, "Se"), //
	SI_SPEC_SI(97, "Si"), //
	SI_SPEC_SN(98, "Sn"), //
	SI_SPEC_SS(99, "Ss"), //
	SI_SPEC_SW(100, "Sw"), //
	SI_SPEC_SX(101, "Sx"), //
	SI_SPEC_SXB(102, "Sxb"), //
	SI_SPEC_SXE(103, "Sxe"), //
	SI_SPEC_SXL(104, "Sxl"), //
	SI_SPEC_SXS(105, "Sxs"), //
	SI_SPEC_SXW(106, "Sxw"), //
	SI_SPEC_SXX(107, "Sxx"), //
	SI_SPEC_T(108, "T"), //
	SI_SPEC_TW(109, "Tw"), //
	SI_SPEC_U(110, "U"), //
	SI_SPEC_UA(111, "Ua"), //
	SI_SPEC_UP(112, "Up"), //
	SI_SPEC_V(113, "V"), //
	SI_SPEC_VB(114, "Vb"), //
	SI_SPEC_VP(115, "Vp"), //
	SI_SPEC_VS(116, "Vs"), //
	SI_SPEC_VV(117, "Vv"), //
	SI_SPEC_W(118, "W"), //
	SI_SPEC_WA(119, "Wa"), //
	SI_SPEC_WB(120, "Wb"), //
	SI_SPEC_WD(121, "Wd"), //
	SI_SPEC_WI(122, "Wi"), //
	SI_SPEC_WP(123, "Wp"), //
	SI_SPEC_WS(124, "Ws"), //
	SI_SPEC_WT(125, "Wt"), //
	SI_SPEC_X(126, "X"), //
	SI_SPEC_XC(127, "Xc"), //
	SI_SPEC_XH(128, "Xh"), //
	SI_SPEC_Y(129, "Y"), //
	SI_SPEC_YC(130, "Yc"), //
	SI_SPEC_YP(131, "Yp"), //
	SI_SPEC_Z(132, "Z"), //
	SI_SPEC_ZC(133, "Zc"), //
	SI_SPEC_ZH(134, "Zh");

	private static final Map<Integer, SiteIndexSpecies> byIndex = new HashMap<>();
	private static final SiteIndexSpecies first, last;

	static {
		first = SiteIndexSpecies.getByIndex(0);

		int firstMissingIndexCandidate = 0;
		while (true) {
			try {
				getByIndex(firstMissingIndexCandidate);
				firstMissingIndexCandidate += 1;
			} catch (IllegalArgumentException e) {
				break;
			}
		}
		last = SiteIndexSpecies.getByIndex(firstMissingIndexCandidate - 1);
	}

	private final int n;
	private final String code;

	SiteIndexSpecies(int n, String code) {
		this.n = n;
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public int n() {
		return n;
	}

	public static SiteIndexSpecies getFirstSpecies() {
		return first;
	}

	public static SiteIndexSpecies getLastSpecies() {
		return last;
	}

	public static SiteIndexSpecies getByCode(String code) throws CodeErrorException {
		try {
			if (code != null) {
				String enumEntryText = "SI_SPEC_" + code.replace(" ", "").toUpperCase();
				return SiteIndexSpecies.valueOf(enumEntryText);
			}
		} catch (IllegalArgumentException | NullPointerException e) {
			/* fall through */
		}

		throw new CodeErrorException("Unknown species code: " + code);
	}

	public static SiteIndexSpecies getByIndex(int n) {
		SiteIndexSpecies e = null;

		if (n >= 0) {
			e = byIndex.get(n);
			if (e == null) {
				for (SiteIndexSpecies v : values()) {
					if (v.n == n) {
						byIndex.put(n, e = v);
						break;
					}
				}
			}
		}

		if (e == null) {
			throw new IllegalArgumentException(MessageFormat.format("Index {} doesn't exist in SiteIndexSpecies", n));
		} else {
			return e;
		}
	}
}
