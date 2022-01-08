package dev.kske.eventbus.core;

/**
 * This unchecked exception is specific to the event bus and can be thrown under the following
 * circumstances:
 * <ul>
 * <li>An event handler throws an exception (which is stored as the cause)</li>
 * <li>An event listener with an invalid event handler is registered</li>
 * <li>{@link EventBus#cancel()} is invoked from outside an active dispatch thread</li>
 * </ul>
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
public final class EventBusException extends RuntimeException {

	private static final long serialVersionUID = 7254445250300604449L;

	/**
	 * Creates a new event bus exception.
	 *
	 * @param message the message to display
	 * @param cause   the cause of this exception
	 * @since 0.0.1
	 */
	public EventBusException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new event bus exception.
	 *
	 * @param message the message to display
	 * @since 0.0.1
	 */
	public EventBusException(String message) {
		super(message);
	}
}
