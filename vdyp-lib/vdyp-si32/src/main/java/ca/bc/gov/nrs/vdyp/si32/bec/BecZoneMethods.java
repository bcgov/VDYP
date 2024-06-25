package ca.bc.gov.nrs.vdyp.si32.bec;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.bc.gov.nrs.vdyp.si32.vdyp.SP0Name;
import ca.bc.gov.nrs.vdyp.si32.vdyp.VdypMethods;

public class BecZoneMethods {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(BecZoneMethods.class);

	public static final String UNKNOWN_BEC_ZONE_TEXT = "????";

	private static final Map<String, BecZone> becZoneToIndexMap = new HashMap<>();

	static {
		becZoneToIndexMap.put("AT", BecZone.AT);
		becZoneToIndexMap.put("BG", BecZone.BG);
		becZoneToIndexMap.put("BWBS", BecZone.BWBS);
		becZoneToIndexMap.put("CDF", BecZone.CDF);
		becZoneToIndexMap.put("CWH", BecZone.CWH);
		becZoneToIndexMap.put("ESSF", BecZone.ESSF);
		becZoneToIndexMap.put("ICH", BecZone.ICH);
		becZoneToIndexMap.put("IDF", BecZone.IDF);
		becZoneToIndexMap.put("MH", BecZone.MH);
		becZoneToIndexMap.put("MS", BecZone.MS);
		becZoneToIndexMap.put("PP", BecZone.PP);
		becZoneToIndexMap.put("SBSP", BecZone.SBPS);
		becZoneToIndexMap.put("SBS", BecZone.SBS);
		becZoneToIndexMap.put("SWB", BecZone.SWB);
	}

	private static final Map<BecZone, String> enumToBecZoneMap = new HashMap<>();
	static {
		enumToBecZoneMap.put(BecZone.AT, "AT");
		enumToBecZoneMap.put(BecZone.BG, "BG");
		enumToBecZoneMap.put(BecZone.BWBS, "BWBS");
		enumToBecZoneMap.put(BecZone.CDF, "CDF");
		enumToBecZoneMap.put(BecZone.CWH, "CWH");
		enumToBecZoneMap.put(BecZone.ESSF, "ESSF");
		enumToBecZoneMap.put(BecZone.ICH, "ICH");
		enumToBecZoneMap.put(BecZone.IDF, "IDF");
		enumToBecZoneMap.put(BecZone.MH, "MH");
		enumToBecZoneMap.put(BecZone.MS, "MS");
		enumToBecZoneMap.put(BecZone.PP, "PP");
		enumToBecZoneMap.put(BecZone.SBPS, "SBSP");
		enumToBecZoneMap.put(BecZone.SBS, "SBS");
		enumToBecZoneMap.put(BecZone.SWB, "SWB");
	}

	/**
	 * Convert the given BEC Zone label to its enumeration value.
	 *
	 * @param becZone the 2 - 4 character BEC Zone identifier such as "AG". The value need not be in upper-case.
	 * @return the index of the given BEC Zone in the enumeration, or bec_UNKNOWN if it's not recognized.
	 */
	public static BecZone becZoneToIndex(String becZone) {

		if (becZone != null) {
			String becZoneUC = becZone.toUpperCase();
			if (becZoneToIndexMap.containsKey(becZoneUC)) {
				return becZoneToIndexMap.get(becZoneUC);
			}
		}

		return BecZone.UNKNOWN;
	}

	/**
	 * Convert the given BEC Zone enumeration value to its identifier
	 *
	 * @param becZone the value to convert
	 * @return the BEC Zone identifier or ???? if it's not recognized.
	 */
	public static String becZoneToCode(BecZone becZone) {

		if (enumToBecZoneMap.containsKey(becZone)) {
			return enumToBecZoneMap.get(becZone);
		} else {
			return UNKNOWN_BEC_ZONE_TEXT;
		}
	}

	/**
	 * The array of MoF BioMass Coefficients by SP0 and then BEC Zone.
	 * <p>
	 * Remarks
	 * <p>
	 * Constants are derived from a spreadsheet supplied by Sam Otukol via e-mail on June 8, 2010. Based on subsequent
	 * telephone discussions, missing BEC/SP0 combinations in that spreadsheet will be populated by the average value of
	 * populated cells for that species across all BEC Zones.
	 * <p>
	 * See the coefficient source file: Documents\VolumeToBiomassRatios.xls
	 * <p>
	 * 2013/03/28: Set the new Volume to MoF Biomass Ratios. Refer to spreadsheet 'VolumeToBiomassRatios.xls' in
	 * 'Documents' folder.
	 */
	public static float[][] mofBiomassCoeffs = {
			{ 0.75226f, 0.67773f, 0.56659f, 0.46211f, 0.68033f, 0.76075f, 0.74426f, 0.65445f, 0.70505f, 0.6811f,
					0.65392f, 0.67541f, 0.74621f, 0.72804f },
			{ 0.65f, 0.59378f, 0.529f, 0.62275f, 0.61644f, 0.66022f, 0.6267f, 0.64858f, 0.62275f, 0.62402f, 0.66216f,
					0.64787f, 0.64741f, 0.5668f },
			{ 0.64299f, 0.59104f, 0.5248f, 0.45428f, 0.57775f, 0.62298f, 0.6189f, 0.59604f, 0.59467f, 0.64494f,
					0.57311f, 0.64939f, 0.62226f, 0.56149f },
			{ 0.33381f, 0.45245f, 0.45245f, 0.38617f, 0.46044f, 0.44231f, 0.46749f, 0.42159f, 0.4479f, 0.39357f,
					0.80881f, 0.40746f, 0.40734f, 0.45245f },
			{ 0.98045f, 0.98045f, 1.31835f, 0.61461f, 0.64046f, 1.34721f, 0.72235f, 0.61955f, 0.68683f, 1.29939f,
					0.98045f, 1.26228f, 1.29339f, 0.98045f },
			{ 0.88456f, 0.81035f, 0.74582f, 0.85618f, 0.77419f, 0.86303f, 0.77514f, 0.77808f, 0.8116f, 0.80313f,
					0.73593f, 0.78081f, 0.81568f, 0.91042f },
			{ 0.59477f, 0.65612f, 0.87535f, 0.62121f, 0.64416f, 0.60205f, 0.57064f, 0.58845f, 0.6732f, 0.60828f,
					0.65134f, 0.61338f, 0.58031f, 0.63687f },
			{ 0.64593f, 0.58617f, 0.58617f, 0.60175f, 0.53275f, 0.57514f, 0.58106f, 0.55665f, 0.63182f, 0.58237f,
					0.58617f, 0.58617f, 0.5681f, 0.58617f },
			{ 0.53048f, 0.58113f, 0.58173f, 0.58113f, 0.63967f, 0.55446f, 0.52352f, 0.5581f, 0.58113f, 0.56171f,
					0.59131f, 0.58113f, 0.57607f, 0.69426f },
			{ 0.67523f, 0.67523f, 0.67523f, 0.60428f, 0.69939f, 0.59213f, 0.80674f, 0.73477f, 0.67523f, 0.69131f,
					0.67523f, 0.67523f, 0.59798f, 0.67523f },
			{ 0.46176f, 0.45991f, 0.45991f, 0.45991f, 0.4697f, 0.44976f, 0.4771f, 0.45006f, 0.4972f, 0.41802f, 0.45991f,
					0.45991f, 0.45568f, 0.45991f },
			{ 0.64937f, 0.56168f, 0.50798f, 0.55649f, 0.59781f, 0.57596f, 0.562f, 0.54768f, 0.60541f, 0.53029f,
					0.55597f, 0.51805f, 0.5793f, 0.5155f },
			{ 0.54482f, 0.54087f, 0.54087f, 0.50384f, 0.55002f, 0.54033f, 0.52083f, 0.50976f, 0.58195f, 0.53198f,
					0.57916f, 0.54087f, 0.54595f, 0.54087f },
			{ 0.70414f, 0.80409f, 0.70414f, 0.70414f, 0.52667f, 0.59504f, 0.62338f, 0.85633f, 0.70414f, 0.77297f,
					0.75048f, 0.70414f, 0.70414f, 0.70414f },
			{ 0.53945f, 0.55009f, 0.47422f, 0.58567f, 0.60615f, 0.52521f, 0.53344f, 0.53973f, 0.6291f, 0.51323f,
					0.50677f, 0.58114f, 0.53923f, 0.57781f },
			{ 0.76271f, 0.68096f, 0.68096f, 0.59783f, 0.69708f, 0.64411f, 0.68096f, 0.66552f, 0.71844f, 0.68096f,
					0.68096f, 0.68096f, 0.68096f, 0.68096f } };

	static {
		if (mofBiomassCoeffs.length != SP0Name.size()) {
			throw new IllegalStateException(
					MessageFormat.format(
							"mofBiomassCoeffs does not contain one row for each of the {0} BEC Zones", BecZone.size()
					)
			);
		}

		for (int i = 0; i < mofBiomassCoeffs.length; i++) {
			if (mofBiomassCoeffs[i].length != BecZone.size()) {
				throw new IllegalStateException(
						MessageFormat.format(
								"mofBiomassCoeffs {} does not contain one entry for each of the {0} SP0 Zones", i,
								SP0Name.size()
						)
				);
			}
		}
	}

	/**
	 * Converts a BEC Zone and a CFS Species code name into an MoF Biomass Coefficient for multiplying corresponding
	 * Projected Volumes into an MoF Biomass value.
	 *
	 * @param becZoneName  the name of the BEC Zone.
	 * @param sp64CodeName the code name of the CFS species.
	 * @return as described. If either parameter doesn't identify an entity of the expected type, -1.0 is returned.
	 */
	public static float mofBiomassCoefficient(String becZoneName, String sp64CodeName) {

		float result = -1.0f;

		if (becZoneName != null && sp64CodeName != null) {
			BecZone becZone = BecZoneMethods.becZoneToIndex(becZoneName);

			if (becZone != BecZone.UNKNOWN && VdypMethods.isValidSpecies(sp64CodeName)) {
				String sp0Name = VdypMethods.getVDYP7Species(sp64CodeName);
				int sp0Index = VdypMethods.getVDYP7SpeciesIndex(sp0Name).getIndex();

				if (sp0Index != SP0Name.UNKNOWN.getIndex()) {
					result = mofBiomassCoeffs[sp0Index][becZone.getOffset()];
				}
			}
		}

		return result;
	}
}