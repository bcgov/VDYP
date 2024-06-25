package ca.bc.gov.nrs.vdyp.si32.cfs;

import ca.bc.gov.nrs.vdyp.si32.vdyp.SP0Name;

/**
 * This is a two dimensional array indexed first by {@link SP0Name} and then {@link CfsDensity}, yielding the density
 * value for the given VDYP7 species and density.
 *
 * The density values come from Table 6 of 'Volume_to_Biomass.doc' found in the folder 'Documents/CFS-Biomass'.
 */
public class CfsSP0Densities {

	public static float DEFAULT_VALUE = -9.0f;

	public static float getValue(SP0Name sp0Name, CfsDensity cfsDensity) {

		if (sp0Name == null || cfsDensity == null) {
			return DEFAULT_VALUE;
		} else {
			return array[sp0Name.getOffset()][cfsDensity.getOffset()];
		}
	}

	private final static float[][] array = { //
			{ 295.00F, 229.00F, 564.00F }, //
			{ 416.00F, 304.00F, 519.00F }, //
			{ 379.25F, 204.00F, 541.00F }, //
			{ 391.00F, 238.00F, 475.00F }, //
			{ 373.00F, 333.00F, 603.00F }, //
			{ 607.00F, 512.00F, 693.00F }, //
			{ 445.00F, 323.00F, 615.00F }, //
			{ 476.00F, 249.00F, 661.00F }, //
			{ 524.25F, 323.00F, 616.00F }, //
			{ 466.00F, 466.00F, 530.00F }, //
			{ 420.00F, 204.00F, 693.00F }, //
			{ 423.00F, 256.00F, 518.00F }, //
			{ 373.00F, 237.00F, 496.00F }, //
			{ 420.00F, 204.00F, 693.00F }, //
			{ 387.00F, 257.00F, 568.00F }, //
			{ 453.00F, 239.00F, 544.00F } };

	static {
		if (array.length != SP0Name.size()) {
			throw new IllegalStateException("CfsSP0Densities.array does not have exactly one row per VDYP7 species");
		}
		for (int i = 0; i < array.length; i++) {
			if (array[i].length != CfsDensity.size()) {
				throw new IllegalStateException(
						"CfsSP0Densities.array[" + i + "] does not have exactly one element per CFSDensity value"
				);
			}
		}
	}
}
