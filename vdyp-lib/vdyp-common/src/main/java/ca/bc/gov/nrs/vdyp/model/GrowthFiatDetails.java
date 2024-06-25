package ca.bc.gov.nrs.vdyp.model;

import java.util.List;

public class GrowthFiatDetails {
	private final Region region;
	private final Float[] ages;
	private final Float[] coefficients;
	private final Float[] mixedCoefficients;

	/**
	 * Describes a GrowthFiat model.
	 *
	 * @param regionId     either 1 (coast) or 2 (interior)
	 * @param coefficients (11: 0-3 ages, 4-7 coefficients, 8-10 mixed coefficients)
	 */
	public GrowthFiatDetails(int regionId, List<Float> numbers) {

		region = regionId == 1 ? Region.COASTAL : Region.INTERIOR;

		int agesSupplied = 4;
		for (int i = 0; i < 4; i++)
			if (numbers.get(i) == 0.0) {
				agesSupplied = i;
				break;
			}

		ages = new Float[agesSupplied];
		for (int i = 0; i < agesSupplied; i++) {
			ages[i] = numbers.get(i);
		}

		coefficients = new Float[agesSupplied];
		for (int i = 0; i < agesSupplied; i++) {
			coefficients[i] = numbers.get(4 + i);
		}

		mixedCoefficients = new Float[3];
		for (int i = 0; i < 3; i++) {
			mixedCoefficients[i] = numbers.get(8 + i);
		}
	}

	public Region getRegion() {
		return region;
	}

	public int getNAges() {
		return ages.length;
	}

	public Float getAge(int index) {
		return ages[index];
	}

	public Float getCoefficient(int index) {
		return coefficients[index];
	}

	public Float getMixedCoefficient(int index) {
		return mixedCoefficients[index];
	}

	public Float[] getAges() {
		return ages;
	}

	public Float[] getCoefficients() {
		return coefficients;
	}

	public Float[] getMixedCoefficients() {
		return mixedCoefficients;
	}
}
