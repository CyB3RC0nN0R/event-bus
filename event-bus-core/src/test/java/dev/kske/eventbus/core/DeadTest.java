package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the dispatching of a dead event if an event could not be delivered.
 *
 * @author Kai S. K. Engelbart
 * @since 1.1.0
 */
class DeadTest {

	EventBus	bus;
	String		event	= "This event has no handler";
	boolean		deadEventHandled;

	@BeforeEach
	void registerListener() {
		bus = new EventBus();
		bus.registerListener(this);
	}

	@Test
	void testDeadEvent() {
		bus.dispatch(event);
		assertTrue(deadEventHandled);
	}

	@Event
	void onDeadEvent(DeadEvent deadEvent) {
		assertEquals(bus, deadEvent.getEventBus());
		assertEquals(event, deadEvent.getEvent());
		deadEventHandled = true;
	}
}
