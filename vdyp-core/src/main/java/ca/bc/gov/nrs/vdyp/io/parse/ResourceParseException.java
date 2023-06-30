package ca.bc.gov.nrs.vdyp.io.parse;

public class ResourceParseException extends Exception {

	private static final long serialVersionUID = -5647186835165496893L;

	public ResourceParseException() {
		super();
	}

	public ResourceParseException(
			String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
	) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResourceParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceParseException(String message) {
		super(message);
	}

	public ResourceParseException(Throwable cause) {
		super(cause);
	}

}
