package ca.bc.gov.nrs.vdyp.model;

import java.util.EnumMap;
import java.util.Map;

public class SiteCurveAgeMaximum {
	private final Map<Region, Float> ageMaximums;
	private final float t1;
	private final float t2;

	public SiteCurveAgeMaximum(float ageCoast, float ageInt, float t1, float t2) {
		this(maximums(ageCoast, ageInt), t1, t2);
	}

	private static Map<Region, Float> maximums(float ageCoast, float ageInt) {
		var result = new EnumMap<Region, Float>(Region.class);
		result.put(Region.COASTAL, ageCoast);
		result.put(Region.INTERIOR, ageInt);
		return result;
	}

	public SiteCurveAgeMaximum(Map<Region, Float> ageMaximums, float t1, float t2) {
		super();
		this.ageMaximums = new EnumMap<>(ageMaximums);
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
