package ca.bc.gov.nrs.vdyp.application;

/**
 * Identifies the VDYP applications. Matches the values used for the FORTRAN
 * implementation's JPROGRAM COMMON variable.
 *
 * @author Michael Junkin, Vivid Solutions
 */
public enum VdypApplicationIdentifier {

	FIP_START(1), VRI_START(3), VDYP_FORWARD(6), VDYP_BACK(7), VRI_ADJUST(8);

	private final int jProgramNumber;

	VdypApplicationIdentifier(int jProgramNumber) {
		this.jProgramNumber = jProgramNumber;
	}

	public int getJProgramNumber() {
		return jProgramNumber;
	}
}
