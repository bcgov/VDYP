package ca.bc.gov.nrs.vdyp.model;

import java.util.HashMap;
import java.util.Map;

public class SiteCurveAgeMaximum {
	final Map<Region, Float> ageMaximums;
	final float t1;
	final float t2;

	public SiteCurveAgeMaximum(float ageCoast, float ageInt, float t1, float t2) {
		this(maximums(ageCoast, ageInt), t1, t2);
	}

	private static Map<Region, Float> maximums(float ageCoast, float ageInt) {
		var result = new HashMap<Region, Float>();
		result.put(Region.COASTAL, ageCoast);
		result.put(Region.INTERIOR, ageInt);
		return result;
	}

	public SiteCurveAgeMaximum(Map<Region, Float> ageMaximums, float t1, float t2) {
		super();
		this.ageMaximums = new HashMap<>(ageMaximums);
		this.t1 = t1;
		this.t2 = t2;
	}

	public float getAgeMaximum(Region region) {
		return ageMaximums.get(region);
	}

	public float getT1() {
		return t1;
	}

	public float getT2() {
		return t2;
	}

}
