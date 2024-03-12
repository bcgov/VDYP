package ca.bc.gov.nrs.vdyp.application;

/**
 * Base class of all VDYP applications.
 *
 * <p>
 * Expects <tt>application.properties</tt> to be on the class path.
 *
 * @author Michael Junkin, Vivid Solutions
 */
public abstract class VdypApplication extends VdypComponent {
	public abstract VdypApplicationIdentifier getId();

	/**
	 * @returns the ordinal of the application's identifier. It will agree with the JPROGRAM values from the FORTRAN
	 *          implementation.
	 */
	public int getJProgramNumber() {
		return getId().getJProgramNumber();
	}
}
