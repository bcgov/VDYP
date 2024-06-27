package ca.bc.gov.nrs.vdyp.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.bc.gov.nrs.vdyp.common.Utils;

public class CommonData {

	private CommonData() {
	}

	public static final Map<String, Integer> ITG_PURE = Utils.constMap(map -> {
		map.put("AC", 36);
		map.put("AT", 42);
		map.put("B", 18);
		map.put("C", 9);
		map.put("D", 38);
		map.put("E", 40);
		map.put("F", 1);
		map.put("H", 12);
		map.put("L", 34);
		map.put("MB", 39);
		map.put("PA", 28);
		map.put("PL", 28);
		map.put("PW", 27);
		map.put("PY", 32);
		map.put("S", 21);
		map.put("Y", 9);
	});

	public static final Set<String> HARDWOODS = Collections.unmodifiableSet(Set.of("AC", "AT", "D", "E", "MB"));

	/**
	 * When finding primary species these genera should be combined
	 */
	public static final Collection<List<String>> PRIMARY_SPECIES_TO_COMBINE = Collections.unmodifiableCollection(
			Arrays.asList(
					Collections.unmodifiableList(Arrays.asList("PL", "PA")), //
					Collections.unmodifiableList(Arrays.asList("C", "Y"))
			)
	);
}
