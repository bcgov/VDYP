package ca.bc.gov.nrs.vdyp.io.parse;

/**
 * An error parsing resource that is not associated with a particular line
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class ResourceParseValidException extends ResourceParseException {

	private static final long serialVersionUID = 5188546056230073563L;

	public ResourceParseValidException() {
		super();
	}

	public ResourceParseValidException(
			String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
	) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ResourceParseValidException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceParseValidException(String message) {
		super(message);
	}

	public ResourceParseValidException(Throwable cause) {
		super(cause);
	}

}
