package dev.kske.eventbus.core;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;
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

		/**
		 * Indicates that the last event handler invoked has called {@link EventBus#cancel}. In that
		 * case, the event is not dispatched further.
		 *
		 * @since 0.1.0
		 */
		boolean isCancelled;

		/**
		 * Is incremented when {@link EventBus#dispatch(Object)} is invoked and decremented when it
		 * finishes. This allows keeping track of nested dispatches.
		 *
		 * @since 1.2.0
		 */
		int nestingCount;
	}

	/**
	 * The priority assigned to every event handler without an explicitly defined priority.
	 *
	 * @since 1.1.0
	 * @see Priority
	 */
	public static final int DEFAULT_PRIORITY = 100;

	private static final EventBus singletonInstance = new EventBus();

	private static final Logger logger = System.getLogger(EventBus.class.getName());

	/**
	 * Returns the default event bus, which is a statically initialized singleton instance.
	 *
	 * @return the default event bus
	 * @since 0.0.2
	 */
	public static EventBus getInstance() {
		return singletonInstance;
	}

	/**
	 * Event handler bindings (target class to handlers registered for that class), does not contain
	 * other (polymorphic) handlers.
	 *
	 * @since 0.0.1
	 */
	private final Map<Class<?>, TreeSet<EventHandler>> bindings = new ConcurrentHashMap<>();

	/**
	 * Stores all registered event listeners (which declare event handlers) and prevents them from
	 * being garbage collected.
	 *
	 * @since 0.0.1
	 */
	private final Set<Object> registeredListeners = ConcurrentHashMap.newKeySet();

	/**
	 * The current event dispatching state, local to each thread.
	 *
	 * @since 0.1.0
	 */
	private final ThreadLocal<DispatchState> dispatchState =
		ThreadLocal.withInitial(DispatchState::new);

	/**
	 * Dispatches an event to all event handlers registered for it in descending order of their
	 * priority.
	 *
	 * @param event the event to dispatch
	 * @throws EventBusException    if an event handler isn't accessible or has an invalid signature
	 * @throws NullPointerException if the specified event is {@code null}
	 * @since 0.0.1
	 */
	public void dispatch(Object event) throws EventBusException {
		Objects.requireNonNull(event);
		logger.log(Level.INFO, "Dispatching event {0}", event);

		// Look up dispatch state
		var state = dispatchState.get();

		// Increment nesting count (becomes > 1 during nested dispatches)
		++state.nestingCount;

		Iterator<EventHandler> handlers = getHandlersFor(event.getClass());
		if (handlers.hasNext()) {
			while (handlers.hasNext())
				if (state.isCancelled) {
					logger.log(Level.INFO, "Cancelled dispatching event {0}", event);
					state.isCancelled = false;
					break;
				} else {
					try {
						handlers.next().execute(event);
					} catch (InvocationTargetException e) {
						if (e.getCause() instanceof Error)

							// Transparently pass error to the caller
							throw (Error) e.getCause();
						else if (event instanceof DeadEvent || event instanceof ExceptionEvent)

							// Warn about system event not being handled
							logger.log(Level.WARNING, event + " not handled due to exception", e);
						else

							// Dispatch exception event
							dispatch(new ExceptionEvent(this, event, e.getCause()));
					}
				}
		} else if (event instanceof DeadEvent || event instanceof ExceptionEvent) {

			// Warn about the dead event not being handled
			logger.log(Level.WARNING, "{0} not handled", event);
		} else {

			// Dispatch dead event
			dispatch(new DeadEvent(this, event));
		}

		// Decrement nesting count (becomes 0 when all dispatches on the thread are finished)
		--state.nestingCount;

		logger.log(Level.DEBUG, "Finished dispatching event {0}", event);
	}

	/**
	 * Searches for the event handlers bound to an event class. This includes polymorphic handlers
	 * that are bound to a supertype of the event class.
	 *
	 * @param eventClass the event class to use for the search
	 * @return an iterator over the applicable handlers in descending order of priority
	 * @since 0.0.1
	 */
	private Iterator<EventHandler> getHandlersFor(Class<?> eventClass) {

		// Get handlers defined for the event class
		TreeSet<EventHandler> handlers = bindings.getOrDefault(eventClass, new TreeSet<>());

		// Get polymorphic handlers
		for (var binding : bindings.entrySet())
			if (binding.getKey().isAssignableFrom(eventClass))
				for (var handler : binding.getValue())
					if (handler.isPolymorphic())
						handlers.add(handler);

		return handlers.iterator();
	}

	/**
	 * Cancels an event that is currently dispatched from inside an event handler.
	 *
	 * @throws EventBusException if the calling thread is not an active dispatching thread
	 * @since 0.1.0
	 */
	public void cancel() {
		var state = dispatchState.get();
		if (state.nestingCount > 0 && !state.isCancelled)
			state.isCancelled = true;
		else
			throw new EventBusException("Calling thread not an active dispatching thread!");
	}

	/**
	 * Registers an event listener at this event bus.
	 *
	 * @param listener the listener to register
	 * @throws EventBusException    if the listener is already registered or a declared event
	 *                              handler does not comply with the specification
	 * @throws NullPointerException if the specified listener is {@code null}
	 * @since 0.0.1
	 * @see Event
	 */
	public void registerListener(Object listener) throws EventBusException {
		Objects.requireNonNull(listener);
		if (registeredListeners.contains(listener))
			throw new EventBusException(listener + " already registered!");
		logger.log(Level.INFO, "Registering event listener {0}", listener.getClass().getName());
		boolean handlerBound = false;

		// Predefined handler polymorphism
		boolean polymorphic = false;
		if (listener.getClass().isAnnotationPresent(Polymorphic.class))
			polymorphic = listener.getClass().getAnnotation(Polymorphic.class).value();

		// Predefined handler priority
		int priority = DEFAULT_PRIORITY;
		if (listener.getClass().isAnnotationPresent(Priority.class))
			priority = listener.getClass().getAnnotation(Priority.class).value();

		registeredListeners.add(listener);
		for (var method : listener.getClass().getDeclaredMethods()) {
			Event annotation = method.getAnnotation(Event.class);

			// Skip methods without annotations
			if (annotation == null)
				continue;

			// Initialize and bind the handler
			var handler = new EventHandler(listener, method, annotation, polymorphic, priority);
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
	public void removeListener(Object listener) {
		Objects.requireNonNull(listener);
		logger.log(Level.INFO, "Removing event listener {0}", listener.getClass().getName());

		// Remove bindings from binding map
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

		// Remove the listener itself
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
	public Set<Object> getRegisteredListeners() {
		return Collections.unmodifiableSet(registeredListeners);
	}
}
