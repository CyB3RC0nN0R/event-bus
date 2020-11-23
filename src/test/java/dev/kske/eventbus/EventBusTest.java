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

	int			hits;
	static int	canceledHits;

	@BeforeEach
	public void registerListener() {
		EventBus.getInstance().registerListener(this);
	}

	@Test
	void testDispatch() {
		EventBus.getInstance().dispatch(new SimpleEventSub());
		EventBus.getInstance().dispatch(new SimpleEvent());
	}

	@Test
	void testCancellation() {
		var test2 = new EventBusTest();
		test2.registerListener();
		EventBus.getInstance().dispatch(new SimpleCancelEvent());
		assertTrue(canceledHits == 1);
	}

	@Event(eventType = SimpleEvent.class, includeSubtypes = true, priority = 200)
	private void onSimpleEventFirst() {
		++hits;
		assertTrue(hits == 1 || hits == 2);
	}

	@Event(eventType = SimpleEvent.class, priority = 150)
	private void onSimpleEventSecond() {
		++hits;
		assertEquals(3, hits);
	}

	@Event(priority = 50)
	private void onSimpleEventThird(SimpleEvent event) {
		++hits;
		assertEquals(4, hits);
	}

	@Event(eventType = SimpleCancelEvent.class, priority = 500)
	private void onSimpleCancelFirst() {
		++canceledHits;
		assertTrue(canceledHits == 1);
		EventBus.getInstance().cancel();
	}

	@Event(eventType = SimpleCancelEvent.class, priority = 200)
	private void onSimpleCancelSecond() {
		fail();
	}
}
