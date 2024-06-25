package ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions;

/**
 * The ClassErrorException exist to replace the SI_ERR_CLASS(-6) error code found in the original C code This exception
 * is generally thrown when the site class is unknown, not valid or a similar issue
 */
public class ClassErrorException extends CommonCalculatorException {
	private static final long serialVersionUID = 2202772153292611121L;

	public ClassErrorException() {
	}

	public ClassErrorException(String message) {
		super(message);
	}

	public ClassErrorException(Throwable cause) {
		super(cause);
	}

	public ClassErrorException(String message, Throwable cause) {
		super(message, cause);
	}
}
