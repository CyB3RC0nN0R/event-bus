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

	private final EventListener listener;
	private final Method method;
	private final Event annotation;
	private final Class<? extends IEvent> eventType;

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
	@SuppressWarnings("unchecked")
	EventHandler(EventListener listener, Method method, Event annotation) throws EventBusException {
		this.listener = listener;
		this.method = method;
		this.annotation = annotation;

		// Check for correct method signature and return type
		if (method.getParameterCount() == 0 && annotation.eventType().equals(USE_PARAMETER.class))
			throw new EventBusException(method + " does not define an event type!");

		if (method.getParameterCount() == 1 && !annotation.eventType().equals(USE_PARAMETER.class))
			throw new EventBusException(method + " defines an ambiguous event type!");

		if (method.getParameterCount() > 1)
			throw new EventBusException(method + " defines more than one parameter!");

		if (!method.getReturnType().equals(void.class))
			throw new EventBusException(method + " does not have a return type of void!");

		// Determine the event type
		Class<? extends IEvent> eventType = annotation.eventType();
		if (eventType.equals(USE_PARAMETER.class)) {
			var param = method.getParameterTypes()[0];
			if (!IEvent.class.isAssignableFrom(param))
				throw new EventBusException(param + " is not of type IEvent!");
			eventType = (Class<? extends IEvent>) param;
		}
		this.eventType = eventType;

		// Allow access if the method is non-public
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

	@Override
	public String toString() {
		return String.format("EventHandler[method=%s, annotation=%s]", method, annotation);
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
			if (annotation.eventType().equals(USE_PARAMETER.class))
				method.invoke(listener, event);
			else
				method.invoke(listener);
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

	/**
	 * @return whether this handler includes subtypes
	 * @since 0.0.4
	 */
	boolean includeSubtypes() { return annotation.includeSubtypes(); }

	/**
	 * @return the event type this handler listens to
	 * @since 0.0.3
	 */
	Class<? extends IEvent> getEventType() { return eventType; }
}
