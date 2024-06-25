package ca.bc.gov.nrs.vdyp.common_calculators.custom_exceptions;

/**
 * The GrowthInterceptMaximumException exist to replace the SI_ERR_GI_MAX(-3) error code found in the original C code
 * This exception is generally thrown when the growth intercept(GI) is above a threshold, this could manifest in
 * something like breast height age is greater than GI range
 */
public class GrowthInterceptMaximumException extends CommonCalculatorException {
	private static final long serialVersionUID = 7947200735123548686L;

	public GrowthInterceptMaximumException() {
		super();
	}

	public GrowthInterceptMaximumException(String message) {
		super(message);
	}

	public GrowthInterceptMaximumException(Throwable cause) {
		super(cause);
	}

	public GrowthInterceptMaximumException(String message, Throwable cause) {
		super(message, cause);
	}
}
