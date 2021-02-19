package dev.kske.eventbus.core;

/**
 * Wraps an event that was dispatched but for which no handler has been bound.
 * <p>
 * Handling dead events is useful as it can identify a poorly configured event distribution.
 *
 * @author Kai S. K. Engelbart
 * @since 1.1.0
 */
public final class DeadEvent {

	private final EventBus	eventBus;
	private final Object	event;

	DeadEvent(EventBus eventBus, Object event) {
		this.eventBus	= eventBus;
		this.event		= event;
	}

	/**
	 * @return the event bus that originated this event
	 * @since 1.1.0
	 */
	public EventBus getEventBus() { return eventBus; }

	/**
	 * @return the event that could not be delivered
	 * @since 1.1.0
	 */
	public Object getEvent() { return event; }
}
