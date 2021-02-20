package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Tests the dispatching of a dead event if an event could not be delivered.
 *
 * @author Kai S. K. Engelbart
 * @since 1.1.0
 */
class DeadTest {

	EventBus	bus		= new EventBus();
	String		event	= "This event has no handler";
	boolean		deadEventHandled;

	/**
	 * Tests dead event delivery.
	 * 
	 * @since 1.1.0
	 */
	@Test
	void testDeadEvent() {
		bus.registerListener(this);
		bus.dispatch(event);
		assertTrue(deadEventHandled);
		bus.removeListener(this);
	}

	/**
	 * Tests how the event bus reacts to an unhandled dead event. This should not lead to an
	 * exception or endless recursion and instead be logged.
	 * 
	 * @since 1.1.0
	 */
	@Test
	void testUnhandledDeadEvent() {
		bus.dispatch(event);
	}

	@Event
	void onDeadEvent(DeadEvent deadEvent) {
		assertEquals(bus, deadEvent.getEventBus());
		assertEquals(event, deadEvent.getEvent());
		deadEventHandled = true;
	}
}
