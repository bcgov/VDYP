package ca.bc.gov.nrs.vdyp.io.parse;

public interface KeyedControlMapModifier extends ControlMapModifier {

	/**
	 * The key for this resource's entry in the control map
	 *
	 * @return
	 */
	String getControlKey();

}