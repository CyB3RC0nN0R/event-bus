package dev.kske.eventbus.core.handler;

import java.lang.reflect.*;

import dev.kske.eventbus.core.*;

/**
 * An event handler wrapping a method annotated with {@link Event} and executing it using
 * reflection.
 *
 * @author Kai S. K. Engelbart
 * @since 1.2.0
 */
public final class ReflectiveEventHandler implements EventHandler {

	private final Object	listener;
	private final Method	method;
	private final Class<?>	eventType;
	private final boolean	useParameter;
	private final boolean	polymorphic;
	private final int		priority;

	/**
	 * Constructs a reflective event handler.
	 *
	 * @param listener        the listener containing the handler
	 * @param method          the handler method
	 * @param annotation      the event annotation
	 * @param defPolymorphism the predefined polymorphism (default or listener-level)
	 * @param defPriority     the predefined priority (default or listener-level)
	 * @throws EventBusException if the method or the annotation do not comply with the
	 *                           specification
	 * @since 1.2.0
	 */
	public ReflectiveEventHandler(Object listener, Method method, Event annotation,
		boolean defPolymorphism, int defPriority) throws EventBusException {
		this.listener	= listener;
		this.method		= method;
		useParameter	= annotation.value() == void.class;

		// Check handler signature
		if (method.getParameterCount() == 0 && useParameter)
			throw new EventBusException(method + " does not define an event type!");

		if (method.getParameterCount() == 1 && !useParameter)
			throw new EventBusException(method + " defines an ambiguous event type!");

		if (method.getParameterCount() > 1)
			throw new EventBusException(method + " defines more than one parameter!");

		// Determine handler properties
		eventType	= useParameter ? method.getParameterTypes()[0] : annotation.value();
		polymorphic	= method.isAnnotationPresent(Polymorphic.class)
			? method.getAnnotation(Polymorphic.class).value()
			: defPolymorphism;
		priority	= method.isAnnotationPresent(Priority.class)
			? method.getAnnotation(Priority.class).value()
			: defPriority;

		// Try to allow access if the method is not accessible
		if (!method.canAccess(Modifier.isStatic(method.getModifiers()) ? null : listener))
			method.setAccessible(true);
	}

	@Override
	public void execute(Object event) throws EventBusException, InvocationTargetException {
		try {
			if (useParameter)
				method.invoke(getListener(), event);
			else
				method.invoke(getListener());
		} catch (IllegalArgumentException e) {
			throw new EventBusException("Event handler rejected target / argument!", e);
		} catch (IllegalAccessException e) {
			throw new EventBusException("Event handler is not accessible!", e);
		}
	}

	@Override
	public String toString() {
		return String.format(
			"ReflectiveEventHandler[eventType=%s, polymorphic=%b, priority=%d, method=%s, useParameter=%b]",
			eventType, polymorphic, priority, method, useParameter);
	}

	@Override
	public Object getListener() {
		return listener;
	}

	@Override
	public Class<?> getEventType() {
		return eventType;
	}

	@Override
	public int getPriority() {
		return priority;
	}

	@Override
	public boolean isPolymorphic() {
		return polymorphic;
	}
}
