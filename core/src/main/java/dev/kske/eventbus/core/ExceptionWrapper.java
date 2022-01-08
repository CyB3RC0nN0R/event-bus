package dev.kske.eventbus.core;

/**
 * This unchecked exception acts as a wrapper for an arbitrary exception to prevent an
 * {@link ExceptionEvent} from being dispatched. Instead, the wrapped exception is rethrown by
 * {@link EventBus#dispatch(Object)}.
 *
 * @author Kai S. K. Engelbart
 * @since 1.2.1
 */
public final class ExceptionWrapper extends RuntimeException {

	private static final long serialVersionUID = -2016681140617308788L;

	/**
	 * Creates a new exception wrapper.
	 *
	 * @param cause the exception to wrap
	 * @since 1.2.1
	 */
	public ExceptionWrapper(Exception cause) {
		super(cause);
	}
}
