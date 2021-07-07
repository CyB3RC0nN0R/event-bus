package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests nested event dispatches.
 *
 * @author Kai S. K. Engelbart
 * @since 1.2.0
 */
class NestedTest {

	EventBus	bus;
	boolean		nestedHit;

	/**
	 * Constructs an event bus and registers this test instance as an event listener.
	 *
	 * @since 1.2.0
	 */
	@BeforeEach
	void registerListener() {
		bus = new EventBus();
		bus.registerListener(this);
	}

	/**
	 * Dispatches a simple event, which should in turn cause a string to be dispatched as a nested
	 * event. If the corresponding handler sets {@link #nestedHit} to {@code true}, the test is
	 * successful.
	 *
	 * @since 1.2.0
	 */
	@Test
	void testNestedDispatch() {
		bus.dispatch(new SimpleEvent());
		assertTrue(nestedHit);
	}

	/**
	 * Dispatches a string as a nested event and cancels the current dispatch afterwards.
	 *
	 * @since 1.2.0
	 */
	@Event(SimpleEvent.class)
	void onSimpleEvent() {
		bus.dispatch("Nested event");
		bus.cancel();
	}

	/**
	 * Sets {@link #nestedHit} to {@code true} indicating that nested dispatches work.
	 *
	 * @since 1.2.0
	 */
	@Event(String.class)
	void onString() {
		nestedHit = true;
	}

	/**
	 * Fails the test if an exception is caused during the dispatch.
	 *
	 * @param e the event containing the exception
	 * @since 1.2.0
	 */
	@Event
	void onException(ExceptionEvent e) {
		fail("Exception during dispatch", e.getCause());
	}
}
