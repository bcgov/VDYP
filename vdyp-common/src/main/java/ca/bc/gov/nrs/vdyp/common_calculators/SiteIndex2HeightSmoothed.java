package ca.bc.gov.nrs.vdyp.common_calculators;

import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.LessThan13Exception;
import ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions.NoAnswerException;

/**
 * SiteIndex2HeightSmoothed.java
 *
 * @throws NoAnswerException   if iteration could not converge (projected height > 999)
 * @throws LessThan13Exception if site index < 1.3m
 */
public class SiteIndex2HeightSmoothed {
/* @formatter:off */
/*
 * 2016 mar 9  - Adjusted default equations for Pli, Sw, Fdc, Hwc
               to incorporate height smoothing near 1.3m.
 * 2023 jul 17 - Translated like for like from C to Java
 *             - Renamed from si2hts to SiteIndex2HeightSmoothed
*/
/* @formatter:on */

//Taken from sindex.h
	/*
	 * age types
	 */
	private static final short SI_AT_TOTAL = 0;
	private static final short SI_AT_BREAST = 1;

	/* define species and equation indices */
	private static final int SI_FDC_BRUCEAC = 100;
	private static final int SI_HWC_WILEYAC = 99;
	private static final int SI_PLI_THROWER = 45;
	private static final int SI_SW_GOUDIE_NATAC = 106;
	private static final int SI_SW_GOUDIE_PLAAC = 112;

//This doesn't seem to be used? It seems like this was copied over from SiteIndex2Height.java. Maybe be removable
	public static double ppow(double x, double y) {
		return (x <= 0) ? 0.0 : Math.pow(x, y);
	}

	public static double llog(double x) {
		return ( (x) <= 0.0) ? Math.log(.00001) : Math.log(x);
	}

	public static double index_to_height_smoothed(
			short cu_index, double iage, short age_type, double site_index, double y2bh, double seedling_age,
			double seedling_ht
	) {
		double height; // return value
		double k0, k1;
		double itage; // user's total age
		double tage; // total age
		double bhage; // breast-height age
		double pi; // proportion of height growth between breast height
					// ages 0 and 1 that occurs below breast height

		if (site_index < 1.3) {
			throw new LessThan13Exception("Site index < 1.3m: " + site_index);
		}

		if (y2bh < 0) {
			throw new NoAnswerException("Iteration could not converge (projected height > 999), y2bh: " + y2bh);
		}

		itage = iage;
		if (age_type == SI_AT_BREAST) {
			itage = iage + y2bh;
		}
		if (itage < 0.0) {
			throw new NoAnswerException("Iteration could not converge (projected height > 999), itage: " + itage);
		}
		if (itage < 0.00001) {
			return 0.0;
		}

		if (cu_index == SI_PLI_THROWER || cu_index == SI_SW_GOUDIE_PLAAC || cu_index == SI_SW_GOUDIE_NATAC
				|| cu_index == SI_FDC_BRUCEAC || cu_index == SI_HWC_WILEYAC) {
			pi = y2bh - (int) y2bh;
		} else {
			pi = 0.5;
		}

		bhage = 2;
		do {
			height = SiteIndex2Height.index_to_height(cu_index, bhage, SI_AT_BREAST, site_index, y2bh, pi);
			if (height < 0) {
				return height;
			}
			tage = bhage + (int) y2bh;
			k1 = Math.log( (1.3 - seedling_ht) / (height - seedling_ht))
					/ Math.log( (y2bh - seedling_age) / (tage - seedling_age));
			// printf ("%f %f k1\n", tage, height, k1);
			if (k1 >= 1) {
				k0 = (1.3 - seedling_ht) / Math.pow(y2bh - seedling_age, k1);
				break;
			}
			bhage++;
			if (bhage >= 25) {
				throw new NoAnswerException(
						"Iteration could not converge (projected height > 999), bhage >= 25: " + bhage
				);
			}
		} while (true);

		if (seedling_age == 0) {
			if (itage <= tage) {
				height = k0 * Math.pow(itage, k1);
			} else {
				height = SiteIndex2Height.index_to_height(cu_index, itage, SI_AT_TOTAL, site_index, y2bh, pi);
			}
		} else {
			if (itage < seedling_age) {
				height = seedling_ht / seedling_age * itage;
			} else if (itage < tage) {
				height = seedling_ht + k0 * Math.pow(itage - seedling_age, k1);
			} else {
				height = SiteIndex2Height.index_to_height(cu_index, itage, SI_AT_TOTAL, site_index, y2bh, pi);
			}
		}

		return height;
	}

}
