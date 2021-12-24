package dev.kske.eventbus.core;

/**
 * Wraps an event that was dispatched but caused an exception in one of its handlers.
 * <p>
 * Handling exception events is useful as it allows the creation of a centralized exception handling
 * mechanism for unexpected exceptions.
 * 
 * @author Kai S. K. Engelbart
 * @since 1.1.0
 */
public final class ExceptionEvent {

	private final EventBus	eventBus;
	private final Object	event;
	private final Throwable	cause;

	ExceptionEvent(EventBus eventBus, Object event, Throwable cause) {
		this.eventBus	= eventBus;
		this.event		= event;
		this.cause		= cause;
	}

	@Override
	public String toString() {
		return String.format("ExceptionEvent[eventBus=%s, event=%s, cause=%s]", eventBus, event,
			cause);
	}

	/**
	 * @return the event bus that dispatched this event
	 * @since 1.1.0
	 */
	public EventBus getEventBus() { return eventBus; }

	/**
	 * @return the event that could not be handled because of an exception
	 * @since 1.1.0
	 */
	public Object getEvent() { return event; }

	/**
	 * @return the exception that was thrown while handling the event
	 * @since 1.1.0
	 */
	public Throwable getCause() { return cause; }
}
