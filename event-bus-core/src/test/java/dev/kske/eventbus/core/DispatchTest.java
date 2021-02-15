package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the dispatching mechanism of the event bus.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
class DispatchTest implements EventListener {

	EventBus	bus;
	static int	hits;

	/**
	 * Constructs an event bus and registers this test instance as an event listener.
	 *
	 * @since 0.0.1
	 */
	@BeforeEach
	void registerListener() {
		bus = new EventBus();
		bus.registerListener(this);
	}

	/**
	 * Tests {@link EventBus#dispatch(IEvent)} with multiple handler priorities, a subtype handler
	 * and a static handler.
	 *
	 * @since 0.0.1
	 */
	@Test
	void testDispatch() {
		bus.dispatch(new SimpleEventSub());
		bus.dispatch(new SimpleEvent());
	}

	@Event(eventType = SimpleEvent.class)
	@Priority(200)
	@Polymorphic
	void onSimpleEventFirst() {
		++hits;
		assertTrue(hits == 1 || hits == 2);
	}

	@Event(eventType = SimpleEvent.class)
	@Priority(150)
	static void onSimpleEventSecond() {
		++hits;
		assertEquals(3, hits);
	}

	@Event
	void onSimpleEventThird(SimpleEvent event) {
		++hits;
		assertEquals(4, hits);
	}
}
