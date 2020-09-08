package dev.kske.eventbus;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

/**
 * Tests the of the event bus library.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
class EventBusTest implements EventListener {

	int hits;

	@BeforeEach
	public void registerListener() {
		EventBus.getInstance().registerListener(this);
	}

	@Test
	void testDispatch() {
		EventBus.getInstance().dispatch(new SimpleEvent());
	}

	@Event(priority = 50)
	private void onSimpleEventSecond(SimpleEvent event) {
		++hits;
		assertEquals(2, hits);
	}

	@Event(eventType = SimpleEvent.class, priority = 150)
	private void onSimpleEventFirst() {
		++hits;
		assertEquals(1, hits);
	}
}
