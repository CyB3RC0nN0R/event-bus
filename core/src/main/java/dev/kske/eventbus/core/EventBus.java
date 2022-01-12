package dev.kske.eventbus.core;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import dev.kske.eventbus.core.handler.*;

/**
 * Event listeners can be registered at an event bus to be notified when an event is dispatched.
 * <p>
 * A singleton instance of this class can be lazily created and acquired using the
 * {@link EventBus#getInstance()} method.
 *
 * @implNote This is a thread-safe implementation.
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
	 * Compares event handlers based on priority, but uses hash codes for equal priorities.
	 *
	 * @implNote As the priority comparator by itself is not consistent with equals (two handlers
	 *           with the same priority are not necessarily equal, but would have a comparison
	 *           result of 0), the hash code is used for the fallback comparison. This way,
	 *           consistency with equals is restored.
	 * @since 1.2.0
	 */
	private static final Comparator<EventHandler> byPriority =
		Comparator.comparingInt(EventHandler::getPriority).reversed()
			.thenComparingInt(EventHandler::hashCode);

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
	 * @throws ExceptionWrapper     if it is thrown by an event handler
	 * @throws NullPointerException if the specified event is {@code null}
	 * @since 0.0.1
	 */
	public void dispatch(Object event) {
		Objects.requireNonNull(event);
		logger.log(Level.INFO, "Dispatching event {0}", event);

		// Look up dispatch state
		var state = dispatchState.get();

		// Increment nesting count (becomes > 1 during nested dispatches)
		++state.nestingCount;

		Iterator<EventHandler> handlers = getHandlersFor(event.getClass()).iterator();
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
						else if (e.getCause() instanceof ExceptionWrapper)

							// Transparently pass exception wrapper to the caller
							throw (ExceptionWrapper) e.getCause();
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
	 * @param eventType the event type to use for the search
	 * @return a navigable set containing the applicable handlers in descending order of priority
	 * @since 1.2.0
	 */
	private NavigableSet<EventHandler> getHandlersFor(Class<?> eventType) {

		// Get handlers defined for the event class
		TreeSet<EventHandler> handlers =
			bindings.getOrDefault(eventType, new TreeSet<>(byPriority));

		// Get polymorphic handlers
		for (var binding : bindings.entrySet())
			if (binding.getKey().isAssignableFrom(eventType))
				for (var handler : binding.getValue())
					if (handler.isPolymorphic())
						handlers.add(handler);

		return handlers;
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
	public void registerListener(Object listener) {
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
		for (var method : getHandlerMethods(listener.getClass())) {
			Event annotation = method.getAnnotation(Event.class);

			// Skip methods without annotations
			if (annotation == null)
				continue;

			// Initialize and bind the handler
			bindHandler(
				new ReflectiveEventHandler(listener, method, annotation, polymorphic, priority));
			handlerBound = true;
		}

		if (!handlerBound)
			logger.log(
				Level.WARNING,
				"No event handlers bound for event listener {0}",
				listener.getClass().getName());
	}

	/**
	 * Searches for event handling methods declared inside the inheritance hierarchy of an event
	 * listener.
	 *
	 * @param listenerClass the class to inspect
	 * @return all event handling methods defined for the given listener
	 * @since 1.3.0
	 */
	private Set<Method> getHandlerMethods(Class<?> listenerClass) {

		// Get methods declared by the listener
		Set<Method> methods = getMethodsAnnotatedWith(listenerClass, Event.class);

		// Recursively add superclass handlers
		Class<?> superClass = listenerClass.getSuperclass();
		if (superClass != null && superClass != Object.class)
			methods.addAll(getHandlerMethods(superClass));

		// Recursively add interface handlers
		for (Class<?> iClass : listenerClass.getInterfaces())
			methods.addAll(getHandlerMethods(iClass));

		return methods;
	}

	/**
	 * Searches for declared methods with a specific annotation inside a class.
	 *
	 * @param enclosingClass  the class to inspect
	 * @param annotationClass the annotation to look for
	 * @return all methods matching the search criteria
	 * @since 1.3.0
	 */
	private Set<Method> getMethodsAnnotatedWith(Class<?> enclosingClass,
		Class<? extends Annotation> annotationClass) {
		var methods = new HashSet<Method>();
		for (var method : enclosingClass.getDeclaredMethods())
			if (method.isAnnotationPresent(annotationClass))
				methods.add(method);

		return methods;
	}

	/**
	 * Registers a callback listener, which is a consumer that is invoked when an event occurs. The
	 * listener is not polymorphic and has the {@link #DEFAULT_PRIORITY}.
	 *
	 * @param <E>           the event type the listener listens for
	 * @param eventType     the event type the listener listens for
	 * @param eventListener the callback that is invoked when an event occurs
	 * @since 1.2.0
	 * @see #registerListener(Class, Consumer, boolean, int)
	 */
	public <E> void registerListener(Class<E> eventType, Consumer<E> eventListener) {
		registerListener(eventType, eventListener, false, DEFAULT_PRIORITY);
	}

	/**
	 * Registers a callback listener, which is a consumer that is invoked when an event occurs. The
	 * listener has the {@link #DEFAULT_PRIORITY}.
	 *
	 * @param <E>           the event type the listener listens for
	 * @param eventType     the event type the listener listens for
	 * @param eventListener the callback that is invoked when an event occurs
	 * @param polymorphic   whether the listener is also invoked for subtypes of the event type
	 * @since 1.2.0
	 * @see #registerListener(Class, Consumer, boolean, int)
	 */
	public <E> void registerListener(Class<E> eventType, Consumer<E> eventListener,
		boolean polymorphic) {
		registerListener(eventType, eventListener, polymorphic, DEFAULT_PRIORITY);
	}

	/**
	 * Registers a callback listener, which is a consumer that is invoked when an event occurs. The
	 * listener is not polymorphic.
	 *
	 * @param <E>           the event type the listener listens for
	 * @param eventType     the event type the listener listens for
	 * @param eventListener the callback that is invoked when an event occurs
	 * @param priority      the priority to assign to the listener
	 * @since 1.2.0
	 * @see #registerListener(Class, Consumer, boolean, int)
	 */
	public <E> void registerListener(Class<E> eventType, Consumer<E> eventListener, int priority) {
		registerListener(eventType, eventListener, false, priority);
	}

	/**
	 * Registers a callback listener, which is a consumer that is invoked when an event occurs.
	 *
	 * @param <E>           the event type the listener listens for
	 * @param eventType     the event type the listener listens for
	 * @param eventListener the callback that is invoked when an event occurs
	 * @param polymorphic   whether the listener is also invoked for subtypes of the event type
	 * @param priority      the priority to assign to the listener
	 * @since 1.2.0
	 */
	public <E> void registerListener(Class<E> eventType, Consumer<E> eventListener,
		boolean polymorphic,
		int priority) {
		Objects.requireNonNull(eventListener);
		if (registeredListeners.contains(eventListener))
			throw new EventBusException(eventListener + " already registered!");
		logger.log(Level.INFO, "Registering callback event listener {0}",
			eventListener.getClass().getName());

		registeredListeners.add(eventListener);
		bindHandler(new CallbackEventHandler(eventType, eventListener, polymorphic, priority));
	}

	/**
	 * Inserts a new handler into the {@link #bindings} map.
	 *
	 * @param handler the handler to bind
	 * @since 1.2.0
	 */
	private void bindHandler(EventHandler handler) {
		bindings.putIfAbsent(handler.getEventType(), new TreeSet<>(byPriority));
		logger.log(Level.DEBUG, "Binding event handler {0}", handler);
		bindings.get(handler.getEventType()).add(handler);
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
	 * Generates a string describing the event handlers that would be executed for a specific event
	 * type, in order and without actually executing them.
	 *
	 * @apiNote Using this method is only recommended for debugging purposes, as the output depends
	 *          on implementation internals which may be subject to change.
	 * @implNote Nested dispatches are not accounted for, as this would require actually executing
	 *           the handlers.
	 * @param eventType the event type to generate the execution order for
	 * @return a human-readable event handler list suitable for debugging purposes
	 * @since 1.2.0
	 */
	public String debugExecutionOrder(Class<?> eventType) {
		var	handlers	= getHandlersFor(eventType);
		var	sj			= new StringJoiner("\n");

		// Output header line
		sj.add(String.format("Event handler execution order for %s (%d handler(s)):", eventType,
			handlers.size()));
		sj.add(
			"==========================================================================================");

		// Individual handlers
		for (var handler : handlers)
			sj.add(handler.toString());

		// Bottom line
		sj.add(
			"==========================================================================================");

		return sj.toString();
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
