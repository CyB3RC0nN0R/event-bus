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

	public EventBus eventBus = new EventBus();
	int hits;

	@BeforeEach
	public void registerListener() {
		eventBus.registerListener(this);
	}

	@Test
	void testDispatch() {
		eventBus.dispatch(new SimpleEvent());
	}

	@Event(priority = 50)
	private void onSimpleEventSecond(SimpleEvent event) {
		++hits;
		assertEquals(2, hits);
	}

	@Event(priority = 150)
	private void onSimpleEventFirst(SimpleEvent event) {
		++hits;
		assertEquals(1, hits);
	}
}
