package ca.bc.gov.nrs.vdyp.io.parse.value;

/**
 * An error while parsing a simple value from a string.
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class ValueParseException extends Exception {

	private static final long serialVersionUID = 4181384333196602044L;
	private final String value;

	public String getValue() {
		return value;
	}

	public ValueParseException(String value) {
		super();
		this.value = value;
	}

	public ValueParseException(
			String value, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace
	) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.value = value;
	}

	public ValueParseException(String value, String message, Throwable cause) {
		super(message, cause);
		this.value = value;
	}

	public ValueParseException(String value, String message) {
		super(message);
		this.value = value;
	}

	public ValueParseException(String value, Throwable cause) {
		super(cause);
		this.value = value;
	}

}
