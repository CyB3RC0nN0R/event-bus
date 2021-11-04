package dev.kske.eventbus.core.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;

/**
 * An event handler wrapping a callback method.
 *
 * @author Kai S. K. Engelbart
 * @since 1.2.0
 */
public final class CallbackEventHandler implements EventHandler {

	private final Class<?>			eventType;
	private final Consumer<Object>	callback;
	private final boolean			polymorphic;
	private final int				priority;

	/**
	 * Constructs a callback event handler.
	 *
	 * @param <E>         the event type of the handler
	 * @param eventType   the event type of the handler
	 * @param callback    the callback method to execute when the handler is invoked
	 * @param polymorphic whether the handler is polymorphic
	 * @param priority    the priority of the handler
	 * @since 1.2.0
	 */
	@SuppressWarnings("unchecked")
	public <E> CallbackEventHandler(Class<E> eventType, Consumer<E> callback, boolean polymorphic,
		int priority) {
		this.eventType		= eventType;
		this.callback		= (Consumer<Object>) callback;
		this.polymorphic	= polymorphic;
		this.priority		= priority;
	}

	@Override
	public void execute(Object event) throws InvocationTargetException {
		try {
			callback.accept(event);
		} catch (RuntimeException e) {
			throw new InvocationTargetException(e, "Callback event handler failed!");
		}
	}

	@Override
	public String toString() {
		return String.format(
			"CallbackEventHandler[eventType=%s, polymorphic=%b, priority=%d]",
			eventType, polymorphic, priority);
	}

	@Override
	public Consumer<?> getListener() {
		return callback;
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
