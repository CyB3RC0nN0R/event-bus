package dev.kske.eventbus;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the of the event bus library.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
class EventBusTest implements EventListener {

	static int hits;

	@BeforeEach
	public void registerListener() {
		EventBus.getInstance().registerListener(this);
	}

	@Test
	void testDispatch() {
		EventBus.getInstance().dispatch(new SimpleEventSub());
		EventBus.getInstance().dispatch(new SimpleEvent());
	}

	@Event(
		eventType = SimpleEvent.class,
		includeSubtypes = true,
		priority = 200
	)
	private void onSimpleEventFirst() {
		++hits;
		assertTrue(hits == 1 || hits == 2);
	}

	@Event(eventType = SimpleEvent.class, priority = 150)
	private static void onSimpleEventSecond() {
		++hits;
		assertEquals(3, hits);
	}

	@Event(priority = 50)
	private void onSimpleEventThird(SimpleEvent event) {
		++hits;
		assertEquals(4, hits);
	}
}
