package ca.bc.gov.nrs.vdyp.application;

public class RuntimeStandProcessingException extends RuntimeProcessingException {

	private static final long serialVersionUID = 3236293487579672502L;

	public RuntimeStandProcessingException(StandProcessingException causedBy) {
		super(causedBy);
	}

	@Override
	public synchronized StandProcessingException getCause() {
		return (StandProcessingException) super.getCause();
	}

}
