package ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions;

/**
 * The CurveErrorException exist to replace the SI_ERR_CURVE(-5) error code found in the original C code This exception
 * is generally thrown when the curve index is unknown, not valid or a similar issue
 */
public class CurveErrorException extends CommonCalculatorException {
	private static final long serialVersionUID = 3447030886146679927L;

	public CurveErrorException() {
		super();
	}

	public CurveErrorException(String message) {
		super(message);
	}

	public CurveErrorException(Throwable cause) {
		super(cause);
	}

	public CurveErrorException(String message, Throwable cause) {
		super(message, cause);
	}
}
