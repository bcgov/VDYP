package ca.bc.gov.nrs.vdyp.application;

import java.text.MessageFormat;

/**
 * A problem occurred while VDYP was processing data
 *
 * @author Kevin Smith, Vivid Solutions
 *
 */
public class ProcessingException extends Exception {

	private static final long serialVersionUID = 1L;

	public ProcessingException() {
		super();
	}

	public ProcessingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public ProcessingException(String message, int errorNumber, Throwable cause) {
		super(MessageFormat.format("{0} ({1})", message, errorNumber), cause);
	}

	public ProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessingException(String message) {
		super(message);
	}

	public ProcessingException(Throwable cause) {
		super(cause);
	}

}
