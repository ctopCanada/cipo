package ca.ised.sts.integration.exception;

public class EndpointException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EndpointException(String message, Exception ex) {
		super(message, ex);
	}
	
	public EndpointException(String message) {
		super(message);
	}
	
}
