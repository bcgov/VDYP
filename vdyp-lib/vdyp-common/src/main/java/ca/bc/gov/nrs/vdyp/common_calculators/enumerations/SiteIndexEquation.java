package ca.bc.gov.nrs.vdyp.common_calculators.enumerations;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import ca.bc.gov.nrs.vdyp.model.EnumIterator;

public enum SiteIndexEquation {
	SI_NO_EQUATION(-4), //
	SI_ACB_HUANG(0), //
	SI_ACT_THROWER(1), //
	SI_AT_HUANG(2), //
	SI_AT_CIESZEWSKI(3), //
	SI_AT_GOUDIE(4), //
	SI_BA_DILUCCA(5), //
	SI_BB_KER(6), //
	SI_BA_KURUCZ86(7), //
	SI_BA_KURUCZ82(8), //
	SI_BL_THROWERGI(9), //
	SI_BL_KURUCZ82(10), //
	SI_CWC_KURUCZ(11), //
	SI_CWC_BARKER(12), //
	SI_DR_NIGH(13), //
	SI_DR_HARRING(14), //
	SI_FDC_NIGHGI(15), //
	SI_FDC_BRUCE(16), //
	SI_FDC_COCHRAN(17), //
	SI_FDC_KING(18), //
	SI_FDI_NIGHGI(19), //
	SI_FDI_HUANG_PLA(20), //
	SI_FDI_HUANG_NAT(21), //
	SI_FDI_MILNER(22), //
	SI_FDI_THROWER(23), //
	SI_FDI_VDP_MONT(24), //
	SI_FDI_VDP_WASH(25), //
	SI_FDI_MONS_DF(26), //
	SI_FDI_MONS_GF(27), //
	SI_FDI_MONS_WRC(28), //
	SI_FDI_MONS_WH(29), //
	SI_FDI_MONS_SAF(30), //
	SI_HWC_NIGHGI(31), //
	SI_HWC_FARR(32), //
	SI_HWC_BARKER(33), //
	SI_HWC_WILEY(34), //
	SI_HWC_WILEY_BC(35), //
	SI_HWC_WILEY_MB(36), //
	SI_HWI_NIGH(37), //
	SI_HWI_NIGHGI(38), //
	SI_LW_MILNER(39), //
	SI_PLI_THROWNIGH(40), //
	SI_PLI_NIGHTA98(41), //
	SI_PLI_NIGHGI97(42), //
	SI_PLI_HUANG_PLA(43), //
	SI_PLI_HUANG_NAT(44), //
	SI_PLI_THROWER(45), //
	SI_PLI_MILNER(46), //
	SI_PLI_CIESZEWSKI(47), //
	SI_PLI_GOUDIE_DRY(48), //
	SI_PLI_GOUDIE_WET(49), //
	SI_PLI_DEMPSTER(50), //
	SI_PW_CURTIS(51), //
	SI_PY_MILNER(52), //
	SI_PY_HANN(53), //
	SI_SB_HUANG(54), //
	SI_SB_CIESZEWSKI(55), //
	SI_SB_KER(56), //
	SI_SB_DEMPSTER(57), //
	SI_SS_NIGHGI(58), //
	SI_SS_NIGH(59), //
	SI_SS_GOUDIE(60), //
	SI_SS_FARR(61), //
	SI_SS_BARKER(62), //
	SI_SW_NIGHGI(63), //
	SI_SW_HUANG_PLA(64), //
	SI_SW_HUANG_NAT(65), //
	SI_SW_THROWER(66), //
	SI_SW_CIESZEWSKI(67), //
	SI_SW_KER_PLA(68), //
	SI_SW_KER_NAT(69), //
	SI_SW_GOUDIE_PLA(70), //
	SI_SW_GOUDIE_NAT(71), //
	SI_SW_DEMPSTER(72), //
	SI_BL_CHEN(73), //
	SI_AT_CHEN(74), //
	SI_DR_CHEN(75), //
	SI_PL_CHEN(76), //
	SI_CWI_NIGH(77), //
	SI_BP_CURTIS(78), //
	SI_HWC_NIGHGI99(79), //
	SI_SS_NIGHGI99(80), //
	SI_SW_NIGHGI99(81), //
	SI_LW_NIGHGI(82), //
	SI_SW_NIGHTA(83), //
	SI_CWI_NIGHGI(84), //
	SI_SW_GOUDNIGH(85), //
	SI_HM_MEANS(86), //
	SI_SE_CHEN(87), //
	SI_FDC_NIGHTA(88), //
	SI_FDC_BRUCENIGH(89), //
	SI_LW_NIGH(90), //
	SI_SB_NIGH(91), //
	SI_AT_NIGH(92), //
	SI_BL_CHENAC(93), //
	SI_BP_CURTISAC(94), //
	SI_HM_MEANSAC(95), //
	SI_FDI_THROWERAC(96), //
	SI_ACB_HUANGAC(97), //
	SI_PW_CURTISAC(98), //
	SI_HWC_WILEYAC(99), //
	SI_FDC_BRUCEAC(100), //
	SI_CWC_KURUCZAC(101), //
	SI_BA_KURUCZ82AC(102), //
	SI_ACT_THROWERAC(103), //
	SI_PY_HANNAC(104), //
	SI_SE_CHENAC(105), //
	SI_SW_GOUDIE_NATAC(106), //
	SI_PY_NIGH(107), //
	SI_PY_NIGHGI(108), //
	SI_PLI_NIGHTA2004(109), //
	SI_SE_NIGHTA(110), //
	SI_SW_NIGHTA2004(111), //
	SI_SW_GOUDIE_PLAAC(112), //
	SI_PJ_HUANG(113), //
	SI_PJ_HUANGAC(114), //
	SI_SW_NIGHGI2004(115), //
	SI_EP_NIGH(116), //
	SI_BA_NIGHGI(117), //
	SI_BA_NIGH(118), //
	SI_SW_HU_GARCIA(119), //
	SI_SE_NIGHGI(120), //
	SI_SE_NIGH(121), //
	SI_CWC_NIGH(122);

	private static Map<Integer, SiteIndexEquation> byIndex = new HashMap<>();

	private final int n;

	SiteIndexEquation(int n) {
		this.n = n;
	}

	public static EnumIterator<SiteIndexEquation> getIterator() {
		return new EnumIterator<>(values(), SI_ACB_HUANG, SI_CWC_NIGH);
	}

	public static SiteIndexEquation getByIndex(int n) {
		var e = byIndex.get(n);

		if (e == null) {
			for (SiteIndexEquation v : values()) {
				if (v.n == n) {
					byIndex.put(n, e = v);
					break;
				}
			}
		}

		if (e == null) {
			throw new IllegalArgumentException(MessageFormat.format("Index {} doesn't exist in SiteIndexEquation", n));
		} else {
			return e;
		}
	}

	public int n() {
		return n;
	}
}
