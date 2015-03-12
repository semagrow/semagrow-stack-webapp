/**
 * 
 */
package eu.semagrow.stack.webapp.controllers.exceptions;

/**
 * @author Giannis Mouchakis
 *
 */
public class SemaGrowException extends Exception {


	/**
	 * 
	 */
	private static final long serialVersionUID = 4767798547450971324L;

	/**
	 * 
	 */
	public SemaGrowException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SemaGrowException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SemaGrowException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public SemaGrowException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public SemaGrowException(Throwable cause) {
		super(cause);
	}
	
	

}
