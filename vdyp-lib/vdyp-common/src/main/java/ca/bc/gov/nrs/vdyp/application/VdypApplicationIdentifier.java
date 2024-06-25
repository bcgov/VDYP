package ca.bc.gov.nrs.vdyp.application;

/**
 * Identifies the VDYP applications. Matches the values used for the FORTRAN implementation's JPROGRAM COMMON variable.
 *
 * @author Michael Junkin, Vivid Solutions
 */
public enum VdypApplicationIdentifier {

	FIP_START(true, 1), //
	VRI_START(true, 3), //
	VDYP_FORWARD(false, 6), //
	VDYP_BACK(false, 7), //
	VRI_ADJUST(false, 8);

	private final int jProgramNumber;
	private final boolean isStart;

	VdypApplicationIdentifier(boolean isStart, int jProgramNumber) {
		this.jProgramNumber = jProgramNumber;
		this.isStart = isStart;
	}

	public int getJProgramNumber() {
		return jProgramNumber;
	}

	public boolean isStart() {
		return isStart;
	}
}
