package ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions;

/**
 * The AgeTypeErrorException exist to replace the SI_ERR_AGE_TYPE(-11) error code found in the original C code This
 * exception is generally thrown when the age type is unknown, not valid or a similar issue
 */
public class AgeTypeErrorException extends CommonCalculatorException {
	private static final long serialVersionUID = 5707778478154161902L;

	public AgeTypeErrorException() {
	}

	public AgeTypeErrorException(String message) {
		super(message);
	}

	public AgeTypeErrorException(Throwable cause) {
		super(cause);
	}

	public AgeTypeErrorException(String message, Throwable cause) {
		super(message, cause);
	}
}
