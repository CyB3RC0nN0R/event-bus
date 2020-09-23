package dev.kske.eventbus;

/**
 * This runtime exception is thrown when an event bus error occurs. This can
 * either occur while registering event listeners with invalid handlers, or when
 * an event handler throws an exception.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
public class EventBusException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new event bus exception.
	 *
	 * @param message the message to display
	 * @param cause   the cause of this exception
	 */
	public EventBusException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new event bus exception.
	 *
	 * @param message the message to display
	 */
	public EventBusException(String message) {
		super(message);
	}
}
