package ca.bc.gov.nrs.vdyp.common_calculators;

import static ca.bc.gov.nrs.vdyp.math.FloatMath.sqrt;

/**
 * Converts between trees per hectare and quad mean diameter in a given base area
 */
public class BaseAreaTreeDensityDiameter {

	private BaseAreaTreeDensityDiameter() {
	}

	/**
	 * π/10⁴
	 */
	public static final float PI_40K = (float) (Math.PI / 40_000);

	// FT_BD
	/**
	 * Return an estimate of the number of trees per hectare based on a given base area (a) and quadratic mean diameter
	 * (q) according to the formula
	 * <p>
	 * a / π/10⁴ / q^2
	 * <p>
	 *
	 * @param baseArea              the base area
	 * @param quadraticMeanDiameter the quadratic mean diameter
	 * @return as described. If baseArea or quadraticMeanDiameter is 0 (or less), 0 is returned.
	 */
	public static float treesPerHectare(float baseArea, float quadraticMeanDiameter) {
		if (baseArea > 0 && quadraticMeanDiameter > 0.0f) {
			return baseArea / PI_40K / (quadraticMeanDiameter * quadraticMeanDiameter);
		}

		return 0f;
	}

	// FD_BT
	/**
	 * Return an estimate of the quadratic mean diameter based on a given number of trees per hectare (t) and base area
	 * (a) according to the formula
	 * <p>
	 * (b / t / π/10⁴)^1/2
	 * <p>
	 *
	 * @param baseArea        the base area value
	 * @param treesPerHectare the trees per hectare value
	 * @return as described. If baseArea or treesPerHectare is 0 (or less) or more than 1,000,000, 0.0 is returned.
	 */
	public static float quadMeanDiameter(float baseArea, float treesPerHectare) {
		if (baseArea > 1e6f || treesPerHectare > 1e6f || Float.isNaN(baseArea) || Float.isNaN(treesPerHectare)) {
			return 0f;
		} else if (baseArea > 0f && treesPerHectare > 0f) {
			return sqrt(baseArea / treesPerHectare / PI_40K);
		}
		return 0f;

	}
}
