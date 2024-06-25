package ca.bc.gov.nrs.vdyp.io.parse.common;

public class RuntimeResourceParseException extends RuntimeException {

	private static final long serialVersionUID = 6222533539704333084L;

	public RuntimeResourceParseException(ResourceParseException causedBy) {
		super(causedBy);
	}

	@Override
	public synchronized ResourceParseException getCause() {
		return (ResourceParseException) super.getCause();
	}

}
