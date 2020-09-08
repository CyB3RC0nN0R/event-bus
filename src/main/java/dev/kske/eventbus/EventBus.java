package dev.kske.eventbus;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Event listeners can be registered at an event bus to be notified when an event is dispatched.
 * <p>
 * A singleton instance of this class can be lazily created and acquired using the
 * {@link EventBus#getInstance()} method.
 * <p>
 * This is a thread-safe implementation.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 * @see Event
 */
public final class EventBus {

	private static EventBus singletonInstance;

	/**
	 * Produces a singleton instance of the event bus. It is lazily initialized on the first call.
	 *
	 * @return a singleton instance of the event bus.
	 * @since 0.0.2
	 */
	public static EventBus getInstance() {
		if (singletonInstance == null)
			singletonInstance = new EventBus();
		return singletonInstance;
	}

	private final Map<Class<? extends IEvent>, Collection<EventHandler>> bindings
		= new ConcurrentHashMap<>();
	private final Set<EventListener> registeredListeners = ConcurrentHashMap.newKeySet();

	/**
	 * Dispatches an event to all event handlers registered for it in descending order of their
	 * priority.
	 *
	 * @param event the event to dispatch
	 * @since 0.0.1
	 */
	public void dispatch(IEvent event) {
		Objects.requireNonNull(event);
		getHandlersFor(event.getClass()).forEach(handler -> handler.execute(event));
	}

	/**
	 * Searches for the event handlers bound to an event class.
	 *
	 * @param eventClass the event class to use for the search
	 * @return all event handlers registered for the event class
	 * @since 0.0.1
	 */
	private List<EventHandler> getHandlersFor(Class<? extends IEvent> eventClass) {
		return bindings.containsKey(eventClass) ? new ArrayList<>(bindings.get(eventClass))
			: new ArrayList<>();
	}

	/**
	 * Registers an event listener at this event bus.
	 *
	 * @param listener the listener to register
	 * @throws EventBusException if the listener is already registered or a declared event handler
	 *                           does not comply with the specification
	 * @since 0.0.1
	 * @see Event
	 */
	public void registerListener(EventListener listener) throws EventBusException {
		Objects.requireNonNull(listener);
		if (registeredListeners.contains(listener))
			throw new EventBusException(listener + " already registered!");

		registeredListeners.add(listener);
		for (var method : listener.getClass().getDeclaredMethods()) {
			Event annotation = method.getAnnotation(Event.class);

			// Skip methods without annotations
			if (annotation == null)
				continue;

			// Initialize and bind the handler
			var handler = new EventHandler(listener, method, annotation);
			if (!bindings.containsKey(handler.getEventType()))
				bindings.put(handler.getEventType(), new TreeSet<>());
			bindings.get(handler.getEventType())
				.add(handler);
		}
	}

	/**
	 * Removes a specific listener from this event bus.
	 *
	 * @param listener the listener to remove
	 * @since 0.0.1
	 */
	public void removeListener(EventListener listener) {
		Objects.requireNonNull(listener);
		for (var binding : bindings.values()) {
			var it = binding.iterator();
			while (it.hasNext())
				if (it.next().getListener() == listener)
					it.remove();
		}
		registeredListeners.remove(listener);
	}

	/**
	 * Removes all event listeners from this event bus.
	 *
	 * @since 0.0.1
	 */
	public void clearListeners() {
		bindings.clear();
		registeredListeners.clear();
	}

	/**
	 * Provides an unmodifiable view of the event listeners registered at this event bus.
	 *
	 * @return all registered event listeners
	 * @since 0.0.1
	 */
	public Set<EventListener> getRegisteredListeners() {
		return Collections.unmodifiableSet(registeredListeners);
	}
}
