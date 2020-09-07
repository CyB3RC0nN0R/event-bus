package dev.kske.eventbus;

import java.lang.reflect.*;

/**
 * Internal representation of an event handling method.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 * @see EventBus
 */
final class EventHandler implements Comparable<EventHandler> {

	private final EventListener listener;
	private final Method method;
	private final Event annotation;

	/**
	 * Constructs an event handler.
	 *
	 * @param listener   the listener containing the handler
	 * @param method     the handler method
	 * @param annotation the event annotation
	 * @since 0.0.1
	 */
	EventHandler(EventListener listener, Method method, Event annotation) {
		this.listener = listener;
		this.method = method;
		this.annotation = annotation;
		method.setAccessible(true);
	}

	/**
	 * Compares this to another event handler based on {@link Event#priority()}. In case of equal
	 * priority a non-zero value based on hash codes is returned.
	 * <p>
	 * This is used to retrieve event handlers in the correct order from a tree set.
	 *
	 * @since 0.0.1
	 */
	@Override
	public int compareTo(EventHandler other) {
		int priority = other.annotation.priority() - annotation.priority();
		if (priority == 0)
			priority = listener.hashCode() - other.listener.hashCode();
		return priority == 0 ? hashCode() - other.hashCode() : priority;
	}

	/**
	 * Executes the event handler.
	 *
	 * @param event the event used as the method parameter
	 * @throws EventBusException if the handler throws an exception
	 * @since 0.0.1
	 */
	void execute(IEvent event) throws EventBusException {
		try {
			method.invoke(listener, event);
		} catch (
			IllegalAccessException
			| IllegalArgumentException
			| InvocationTargetException e
		) {
			throw new EventBusException("Failed to invoke event handler!", e);
		}
	}

	/**
	 * @return the listener containing this handler
	 * @since 0.0.1
	 */
	EventListener getListener() { return listener; }

	/**
	 * @return the event annotation
	 * @since 0.0.1
	 */
	Event getAnnotation() { return annotation; }

	/**
	 * @return the priority of the event annotation
	 * @since 0.0.1
	 */
	int getPriority() { return annotation.priority(); }
}
