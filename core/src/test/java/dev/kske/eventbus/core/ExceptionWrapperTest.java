package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests the behavior of the event bus when an {@link ExceptionWrapper} is thrown.
 *
 * @author Kai S. K. Engelbart
 * @since 1.2.1
 */
class ExceptionWrapperTest {

	EventBus	bus		= new EventBus();
	String		event	= "This event will cause an exception";

	/**
	 * Tests transparent rethrowing of an exception wrapper by {@link EventBus#dispatch(Object)}.
	 *
	 * @since 1.2.1
	 */
	@Test
	void testExceptionWrapper() {
		bus.registerListener(this);
		assertThrows(ExceptionWrapper.class, () -> bus.dispatch(event));
	}

	@Event(String.class)
	void onString() {
		throw new ExceptionWrapper(new RuntimeException("I failed!"));
	}
}
