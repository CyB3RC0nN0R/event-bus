package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

/**
 * Tests the event cancellation mechanism of the event bus.
 *
 * @author Kai S. K. Engelbart
 * @author Leon Hofmeister
 * @since 0.1.0
 */
class CancelTest implements EventListener {

	EventBus	bus;
	int			hits;

	/**
	 * Constructs an event bus and registers this test instance as an event listener.
	 *
	 * @since 0.1.0
	 */
	@BeforeEach
	void registerListener() {
		bus = new EventBus();
		bus.registerListener(this);
	}

	/**
	 * Tests {@link EventBus#cancel()} with two event handlers, of which the first cancels the
	 * event.
	 *
	 * @since 0.1.0
	 */
	@Test
	void testCancellation() {
		bus.dispatch(new SimpleEvent());
		assertEquals(1, hits);
	}

	@Event(eventType = SimpleEvent.class)
	@Priority(100)
	void onSimpleFirst() {
		++hits;
		bus.cancel();
	}

	@Event(eventType = SimpleEvent.class)
	@Priority(50)
	void onSimpleSecond() {
		++hits;
	}
}
