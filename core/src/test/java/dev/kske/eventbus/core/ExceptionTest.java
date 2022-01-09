package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests the dispatching of an exception event if an event handler threw an exception.
 *
 * @author Kai S. K. Engelbart
 * @since 1.1.0
 */
class ExceptionTest {

	EventBus			bus			= new EventBus();
	String				event		= "This event will cause an exception";
	RuntimeException	exception	= new RuntimeException("I failed");
	boolean				exceptionEventHandled;

	/**
	 * Tests exception event delivery.
	 *
	 * @since 1.1.0
	 */
	@Test
	void testExceptionEvent() {
		bus.registerListener(this);
		bus.registerListener(new ExceptionListener());
		bus.dispatch(event);
		assertTrue(exceptionEventHandled);
		bus.clearListeners();
	}

	/**
	 * Tests how the event bus reacts to an unhandled exception event. This should not lead to an
	 * exception or an endless recursion and should be logged instead.
	 *
	 * @since 1.1.0
	 */
	@Test
	void testUnhandledExceptionEvent() {
		bus.registerListener(this);
		bus.dispatch(event);
		bus.removeListener(this);
	}

	@Event(String.class)
	void onString() {
		throw exception;
	}

	class ExceptionListener {

		@Event
		void onExceptionEvent(ExceptionEvent exceptionEvent) {
			assertEquals(bus, exceptionEvent.getEventBus());
			assertEquals(event, exceptionEvent.getEvent());
			assertEquals(exception, exceptionEvent.getCause());
			exceptionEventHandled = true;
		}
	}
}
