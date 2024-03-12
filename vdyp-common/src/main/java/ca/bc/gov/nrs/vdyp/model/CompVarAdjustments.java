package ca.bc.gov.nrs.vdyp.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CompVarAdjustments {
	public static final int MIN_INDEX = 1;
	public static final int MAX_INDEX = 98;

	public static final int SMALL_BA = 1;
	public static final int SMALL_DQ = 2;
	public static final int SMALL_HL = 3;
	public static final int SMALL_VOL = 4;

	public static final int LOREY_HEIGHT_PRIMARY_PARAM = 51;
	public static final int LOREY_HEIGHT_OTHER_PARAM = 52;

	private static final Set<Integer> validNamedParams = Set
			.of(SMALL_BA, SMALL_DQ, SMALL_HL, SMALL_VOL, LOREY_HEIGHT_PRIMARY_PARAM, LOREY_HEIGHT_OTHER_PARAM);

	private static final int BA_ADJ_UC_START_INDEX = 5;

	private static final int DQ_ADJ_UC_START_INDEX = 15;

	private static final int UC_VOL_START_INDEX = 11;

	private static final int WHOLE_STEM_VOL_OFFSET = 0;
	private static final int CLOSE_UTIL_VOL_OFFSET = 1;
	private static final int CLOSE_UTIL_VOL_LESS_DECAY_OFFSET = 2;
	private static final int CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTE_OFFSET = 3;

	private Map<Integer, Float> values;

	/**
	 * Constructs a default instance, one in which all index values are 1.0f. See rd_e028.for.
	 */
	public CompVarAdjustments() {
		values = new HashMap<>();
		for (int i = MIN_INDEX; i <= MAX_INDEX; i++)
			values.put(i, 1.0f);
	}

	/**
	 * Constructs an instance from a data set.
	 */
	public CompVarAdjustments(Map<Integer, Float> values) {
		this.values = new HashMap<>(values);
	}

	public Float getParam(int paramNumber) {
		if (!validNamedParams.contains(paramNumber))
			throw new IllegalArgumentException("paramNumber (" + paramNumber + ") is not a valid named parameter");

		return values.get(paramNumber);
	}

	public Float getBaUcAdjustment(UtilizationClass uc) {
		validateUc(uc);
		return values.get(BA_ADJ_UC_START_INDEX + uc.index - 1);
	}

	public Float getDqUcAdjustment(UtilizationClass uc) {
		validateUc(uc);
		return values.get(DQ_ADJ_UC_START_INDEX + uc.index - 1);
	}

	public Float getWholeStemVolumeAdjustment(UtilizationClass uc) {
		validateUc(uc);
		return values.get(UC_VOL_START_INDEX + (10 * (uc.index - 1)) + WHOLE_STEM_VOL_OFFSET);
	}

	public Float getCloseUtilVolumeAdjustment(UtilizationClass uc) {
		validateUc(uc);
		return values.get(UC_VOL_START_INDEX + (10 * (uc.index - 1)) + CLOSE_UTIL_VOL_OFFSET);
	}

	public Float getCloseUtilLessDecayVolumeAdjustment(UtilizationClass uc) {
		validateUc(uc);
		return values.get(UC_VOL_START_INDEX + (10 * (uc.index - 1)) + CLOSE_UTIL_VOL_LESS_DECAY_OFFSET);
	}

	public Float getCloseUtilLessDecayLessWasteVolumeAdjustment(UtilizationClass uc) {
		validateUc(uc);
		return values.get(UC_VOL_START_INDEX + (10 * (uc.index - 1)) + CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTE_OFFSET);
	}

	private void validateUc(UtilizationClass uc) {
		if (uc == UtilizationClass.SMALL || uc == UtilizationClass.ALL)
			throw new IllegalArgumentException("UC (" + uc + ") is not a specific UC other than SMALL");

	}
}
