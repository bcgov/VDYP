package ca.bc.gov.nrs.vdyp.application;

/*
 * An exception preventing a particular stand from being processed, but which should not affect other stands
 */
public class StandProcessingException extends ProcessingException {

	private static final long serialVersionUID = -3844954593240011442L;

	public StandProcessingException() {
		super();
	}

	public StandProcessingException(
			String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
	) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StandProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public StandProcessingException(String message) {
		super(message);
	}

	public StandProcessingException(Throwable cause) {
		super(cause);
	}

}
