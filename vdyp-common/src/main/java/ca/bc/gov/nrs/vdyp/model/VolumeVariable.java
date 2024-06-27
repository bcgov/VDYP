package ca.bc.gov.nrs.vdyp.model;

import java.util.Collections;
import java.util.List;

public enum VolumeVariable {
	WHOLE_STEM_VOL, CLOSE_UTIL_VOL, CLOSE_UTIL_VOL_LESS_DECAY, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE;

	public static final List<VolumeVariable> ALL = Collections.unmodifiableList(
			List.of(WHOLE_STEM_VOL, CLOSE_UTIL_VOL, CLOSE_UTIL_VOL_LESS_DECAY, CLOSE_UTIL_VOL_LESS_DECAY_LESS_WASTAGE)
	);
}
