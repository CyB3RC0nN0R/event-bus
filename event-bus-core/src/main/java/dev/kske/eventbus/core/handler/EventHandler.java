package dev.kske.eventbus.core.handler;

import java.lang.reflect.InvocationTargetException;

import dev.kske.eventbus.core.*;

/**
 * Internal representation of an event handling method.
 *
 * @author Kai S. K. Engelbart
 * @since 1.2.0
 * @see EventBus
 */
public interface EventHandler {

	/**
	 * Executes the event handler.
	 *
	 * @param event the event used as the method parameter
	 * @throws EventBusException         if the event handler isn't accessible or has an invalid
	 *                                   signature
	 * @throws InvocationTargetException if the handler throws an exception
	 * @throws EventBusException         if the handler has the wrong signature or is inaccessible
	 * @since 1.2.0
	 */
	void execute(Object event) throws EventBusException, InvocationTargetException;

	/**
	 * @return the listener containing this handler
	 * @since 1.2.0
	 */
	Object getListener();

	/**
	 * @return the event type this handler listens for
	 * @since 1.2.0
	 */
	Class<?> getEventType();

	/**
	 * @return the priority of this handler
	 * @since 1.2.0
	 * @see Priority
	 */
	int getPriority();

	/**
	 * @return whether this handler is polymorphic
	 * @since 1.2.0
	 * @see Polymorphic
	 */
	boolean isPolymorphic();
}
