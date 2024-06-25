package ca.bc.gov.nrs.vdyp.forward.model;

import java.util.Optional;

public enum FipMode {

	DONT_PROCESS(-1), FIPSTART(1), FIPYOUNG(2);

	private final int code;

	FipMode(int i) {
		code = i;
	}

	public int getCode() {
		return code;
	}

	public static Optional<FipMode> getByCode(int code) {
		switch (code) {
		case -1:
			return Optional.of(DONT_PROCESS);
		case 0:
			return Optional.empty();
		case 1:
			return Optional.of(FIPSTART);
		case 2:
			return Optional.of(FIPYOUNG);
		default:
			return Optional.of(DONT_PROCESS);
		}
	}
}
