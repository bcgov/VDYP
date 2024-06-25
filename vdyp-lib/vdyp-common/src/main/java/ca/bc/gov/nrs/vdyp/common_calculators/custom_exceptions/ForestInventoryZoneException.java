package ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions;

/**
 * The ForestInventoryZoneException exist to replace the SI_ERR_FIZ(-7) error code found in the original C code This
 * exception is generally thrown when there is an issue with the FIZ code, such as it being unknown
 */
public class ForestInventoryZoneException extends CommonCalculatorException {
	private static final long serialVersionUID = 7385965541254190635L;

	public ForestInventoryZoneException() {
		super();
	}

	public ForestInventoryZoneException(String message) {
		super(message);
	}

	public ForestInventoryZoneException(Throwable cause) {
		super(cause);
	}

	public ForestInventoryZoneException(String message, Throwable cause) {
		super(message, cause);
	}
}
