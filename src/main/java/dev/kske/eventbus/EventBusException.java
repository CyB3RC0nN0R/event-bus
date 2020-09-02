package dev.kske.eventbus;

/**
 * This runtime exception is thrown when an event bus error occurs. This can either occur while
 * registering event listeners with invalid handlers, or when an event handler throws an exception.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
public class EventBusException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EventBusException(String message, Throwable cause) {
		super(message, cause);
	}

	public EventBusException(String message) {
		super(message);
	}
}
