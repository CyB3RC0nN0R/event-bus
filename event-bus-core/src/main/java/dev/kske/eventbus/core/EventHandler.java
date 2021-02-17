package dev.kske.eventbus.core;

import java.lang.reflect.*;

import dev.kske.eventbus.core.Event.USE_PARAMETER;

/**
 * Internal representation of an event handling method.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 * @see EventBus
 */
final class EventHandler implements Comparable<EventHandler> {

	/**
	 * The priority assigned to every event handler without an explicitly defined priority.
	 *
	 * @since 1.0.0
	 * @see Priority
	 */
	public static final int DEFAULT_PRIORITY = 100;

	private final Object	listener;
	private final Method	method;
	private final Class<?>	eventType;
	private final boolean	useParameter;
	private final boolean	polymorphic;
	private final int		priority;

	/**
	 * Constructs an event handler.
	 *
	 * @param listener   the listener containing the handler
	 * @param method     the handler method
	 * @param annotation the event annotation
	 * @throws EventBusException if the method or the annotation do not comply with the
	 *                           specification
	 * @since 0.0.1
	 */
	EventHandler(Object listener, Method method, Event annotation) throws EventBusException {
		this.listener	= listener;
		this.method		= method;
		useParameter	= annotation.value() == USE_PARAMETER.class;

		// Check handler signature
		if (method.getParameterCount() == 0 && useParameter)
			throw new EventBusException(method + " does not define an event type!");

		if (method.getParameterCount() == 1 && !useParameter)
			throw new EventBusException(method + " defines an ambiguous event type!");

		if (method.getParameterCount() > 1)
			throw new EventBusException(method + " defines more than one parameter!");

		// Determine handler properties
		eventType	= useParameter ? method.getParameterTypes()[0] : annotation.value();
		polymorphic	= method.isAnnotationPresent(Polymorphic.class);
		priority	= method.isAnnotationPresent(Priority.class)
			? method.getAnnotation(Priority.class).value()
			: DEFAULT_PRIORITY;

		// Allow access if the method is non-public
		method.setAccessible(true);
	}

	/**
	 * Compares this to another event handler based on priority. In case of equal priority a
	 * non-zero value based on hash codes is returned.
	 * <p>
	 * This is used to retrieve event handlers in order of descending priority from a tree set.
	 *
	 * @since 0.0.1
	 */
	@Override
	public int compareTo(EventHandler other) {
		int priority = other.priority - this.priority;
		if (priority == 0)
			priority = listener.hashCode() - other.listener.hashCode();
		return priority == 0 ? hashCode() - other.hashCode() : priority;
	}

	@Override
	public String toString() {
		return String.format(
			"EventHandler[method=%s, eventType=%s, useParameter=%b, polymorphic=%b, priority=%d]",
			method, eventType, useParameter, polymorphic, priority);
	}

	/**
	 * Executes the event handler.
	 *
	 * @param event the event used as the method parameter
	 * @throws EventBusException if the handler throws an exception
	 * @since 0.0.1
	 */
	void execute(Object event) throws EventBusException {
		try {
			if (useParameter)
				method.invoke(listener, event);
			else
				method.invoke(listener);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new EventBusException("Failed to invoke event handler!", e);
		}
	}

	/**
	 * @return the listener containing this handler
	 * @since 0.0.1
	 */
	Object getListener() { return listener; }

	/**
	 * @return the event type this handler listens for
	 * @since 0.0.3
	 */
	Class<?> getEventType() { return eventType; }

	/**
	 * @return the priority of this handler
	 * @since 0.0.1
	 * @see Priority
	 */
	int getPriority() { return priority; }

	/**
	 * @return whether this handler is polymorphic
	 * @since 1.0.0
	 * @see Polymorphic
	 */
	boolean isPolymorphic() { return polymorphic; }
}
