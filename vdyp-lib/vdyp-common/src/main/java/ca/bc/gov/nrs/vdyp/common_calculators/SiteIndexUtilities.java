package ca.bc.gov.nrs.vdyp.common_calculators;

public interface SiteIndexUtilities {

	public static double ppow(double x, double y) {
		return (x <= 0) ? 0.0 : Math.pow(x, y);
	}

	public static double llog(double x) {
		return (x <= 0.0) ? Math.log(.00001) : Math.log(x);
	}
}
