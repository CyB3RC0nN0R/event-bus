package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * Tests whether event handlers correctly work in the context of an inheritance hierarchy.
 *
 * @author Kai S. K. Engelbart
 * @since 1.3.0
 */
class InheritanceTest extends SimpleEventListenerBase implements SimpleEventListenerInterface {

	EventBus bus = new EventBus();

	@Test
	void test() {
		bus.registerListener(this);
		var event = new SimpleEvent();

		bus.dispatch(event);
		assertSame(4, event.getCounter());
	}

	@Override
	void onSimpleEventAbstractHandler(SimpleEvent event) {
		event.increment();
	}

	@Override
	public void onSimpleEventInterfaceHandler(SimpleEvent event) {
		event.increment();
	}

	@Event
	private void onSimpleEventPrivate(SimpleEvent event) {
		event.increment();
	}
}
