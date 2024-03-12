package ca.bc.gov.nrs.vdyp.model;

import java.util.Optional;

public enum PolygonMode {

	DONT_PROCESS(-1), START(1), YOUNG(2), BATN(3), BATC(4);

	private final int code;

	PolygonMode(int i) {
		code = i;
	}

	public int getCode() {
		return code;
	}

	public static Optional<PolygonMode> getByCode(int code) {
		switch (code) {
		case -1:
			return Optional.of(DONT_PROCESS);
		case 0:
			return Optional.empty();
		case 1:
			return Optional.of(START);
		case 2:
			return Optional.of(YOUNG);
		case 3:
			return Optional.of(BATN);
		case 4:
			return Optional.of(BATC);
		default:
			return Optional.of(DONT_PROCESS);
		}
	}
}
