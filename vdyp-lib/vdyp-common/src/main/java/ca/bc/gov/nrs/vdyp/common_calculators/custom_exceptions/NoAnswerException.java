package ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions;

/**
 * The NoAnswerException exist to replace the SI_ERR_NO_ANS(-4) error code found in the original C code This exception
 * is generally thrown when some sort of compute value is out bounds or not a valid answer
 */
public class NoAnswerException extends CommonCalculatorException {
	private static final long serialVersionUID = -5193821896984760660L;

	public NoAnswerException() {
		super();
	}

	public NoAnswerException(String message) {
		super(message);
	}

	public NoAnswerException(Throwable cause) {
		super(cause);
	}

	public NoAnswerException(String message, Throwable cause) {
		super(message, cause);
	}
}
