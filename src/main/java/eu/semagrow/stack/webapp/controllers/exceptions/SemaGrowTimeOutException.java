/**
 * 
 */
package eu.semagrow.stack.webapp.controllers.exceptions;

/**
 * @author Giannis Mouchakis
 *
 */
public class SemaGrowTimeOutException extends SemaGrowException {

	/**
	 * 
	 */
	public SemaGrowTimeOutException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public SemaGrowTimeOutException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public SemaGrowTimeOutException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public SemaGrowTimeOutException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public SemaGrowTimeOutException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
