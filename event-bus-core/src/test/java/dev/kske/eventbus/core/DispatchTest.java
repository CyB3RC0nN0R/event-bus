package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the dispatching mechanism of the event bus.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
@Polymorphic
@Priority(150)
class DispatchTest {

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
	 * Tests {@link EventBus#dispatch(Object)} with multiple handler priorities, a polymorphic
	 * handler and a static handler.
	 *
	 * @since 0.0.1
	 */
	@Test
	void testDispatch() {
		bus.dispatch(new SimpleEventSub());
		bus.dispatch(new SimpleEvent());
	}

	@Event(SimpleEvent.class)
	@Priority(200)
	void onSimpleEventFirst() {
		++hits;
		assertTrue(hits == 1 || hits == 2);
	}

	@Event(SimpleEvent.class)
	@Polymorphic(false)
	static void onSimpleEventSecond() {
		++hits;
		assertEquals(3, hits);
	}

	@Event
	@Polymorphic(false)
	@Priority(100)
	void onSimpleEventThird(SimpleEvent event) {
		++hits;
		assertEquals(4, hits);
	}
}