package dev.kske.eventbus.core;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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

	/**
	 * Holds the state of the dispatching process on one thread.
	 *
	 * @since 0.1.0
	 */
	private static final class DispatchState {

		boolean isDispatching, isCancelled;
	}

	private static volatile EventBus singletonInstance;

	private static final Logger logger = System.getLogger(EventBus.class.getName());

	/**
	 * Produces a singleton instance of the event bus. It is lazily initialized on the first call.
	 *
	 * @return a singleton instance of the event bus.
	 * @since 0.0.2
	 */
	public static EventBus getInstance() {
		EventBus instance = singletonInstance;
		if (instance == null)
			synchronized (EventBus.class) {
				if ((instance = singletonInstance) == null) {
					logger.log(Level.DEBUG, "Initializing singleton event bus instance");
					instance = singletonInstance = new EventBus();
				}
			}
		return instance;
	}

	private final Map<Class<? extends IEvent>, TreeSet<EventHandler>>	bindings			=
		new ConcurrentHashMap<>();
	private final Set<EventListener>									registeredListeners	=
		ConcurrentHashMap.newKeySet();
	private final ThreadLocal<DispatchState>							dispatchState		=
		ThreadLocal.withInitial(DispatchState::new);

	/**
	 * Dispatches an event to all event handlers registered for it in descending order of their
	 * priority.
	 *
	 * @param event the event to dispatch
	 * @since 0.0.1
	 */
	public void dispatch(IEvent event) {
		Objects.requireNonNull(event);
		logger.log(Level.INFO, "Dispatching event {0}", event);

		// Set dispatch state
		var state = dispatchState.get();
		state.isDispatching = true;

		for (var handler : getHandlersFor(event.getClass()))
			if (state.isCancelled) {
				logger.log(Level.INFO, "Cancelled dispatching event {0}", event);
				state.isCancelled = false;
				break;
			} else {
				handler.execute(event);
			}

		// Reset dispatch state
		state.isDispatching = false;

		logger.log(Level.DEBUG, "Finished dispatching event {0}", event);
	}

	/**
	 * Searches for the event handlers bound to an event class.
	 *
	 * @param eventClass the event class to use for the search
	 * @return all event handlers registered for the event class
	 * @since 0.0.1
	 */
	private List<EventHandler> getHandlersFor(Class<? extends IEvent> eventClass) {

		// Get handlers defined for the event class
		Set<EventHandler> handlers = bindings.getOrDefault(eventClass, new TreeSet<>());

		// Get subtype handlers
		for (var binding : bindings.entrySet())
			if (binding.getKey().isAssignableFrom(eventClass))
				for (var handler : binding.getValue())
					if (handler.includeSubtypes())
						handlers.add(handler);

		return new ArrayList<>(handlers);
	}

	/**
	 * Cancels an event that is currently dispatched from inside an event handler.
	 *
	 * @throws EventBusException if the calling thread is not an active dispatching thread
	 * @since 0.1.0
	 */
	public void cancel() {
		var state = dispatchState.get();
		if (state.isDispatching && !state.isCancelled)
			state.isCancelled = true;
		else
			throw new EventBusException("Calling thread not an active dispatching thread!");
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
		logger.log(Level.INFO, "Registering event listener {0}", listener.getClass().getName());
		boolean handlerBound = false;

		registeredListeners.add(listener);
		for (var method : listener.getClass().getDeclaredMethods()) {
			Event annotation = method.getAnnotation(Event.class);

			// Skip methods without annotations
			if (annotation == null)
				continue;

			// Initialize and bind the handler
			var handler = new EventHandler(listener, method, annotation);
			bindings.putIfAbsent(handler.getEventType(), new TreeSet<>());
			logger.log(Level.DEBUG, "Binding event handler {0}", handler);
			bindings.get(handler.getEventType())
				.add(handler);
			handlerBound = true;
		}

		if (!handlerBound)
			logger.log(
				Level.WARNING,
				"No event handlers bound for event listener {0}",
				listener.getClass().getName());
	}

	/**
	 * Removes a specific listener from this event bus.
	 *
	 * @param listener the listener to remove
	 * @since 0.0.1
	 */
	public void removeListener(EventListener listener) {
		Objects.requireNonNull(listener);
		logger.log(Level.INFO, "Removing event listener {0}", listener.getClass().getName());

		for (var binding : bindings.values()) {
			var it = binding.iterator();
			while (it.hasNext()) {
				var handler = it.next();
				if (handler.getListener() == listener) {
					logger.log(Level.DEBUG, "Unbinding event handler {0}", handler);
					it.remove();
				}
			}
		}
		registeredListeners.remove(listener);
	}

	/**
	 * Removes all event listeners from this event bus.
	 *
	 * @since 0.0.1
	 */
	public void clearListeners() {
		logger.log(Level.INFO, "Clearing event listeners");
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
