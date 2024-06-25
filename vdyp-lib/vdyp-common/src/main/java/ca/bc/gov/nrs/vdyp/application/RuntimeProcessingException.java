package ca.bc.gov.nrs.vdyp.application;

public class RuntimeProcessingException extends RuntimeException {

	private static final long serialVersionUID = 3236293487579672502L;

	public RuntimeProcessingException(ProcessingException causedBy) {
		super(causedBy);
	}

	@Override
	public synchronized ProcessingException getCause() {
		return (ProcessingException) super.getCause();
	}

}
