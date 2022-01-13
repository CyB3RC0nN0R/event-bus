package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An abstract class defining a package-private and a private handler for {@link SimpleEvent}.
 *
 * @author Kai S. K. Engelbart
 * @since 1.3.0
 */
@Priority(200)
abstract class SimpleEventListenerBase {

	@Event
	void onSimpleEventAbstractHandler(SimpleEvent event) {
		fail("This handler should not be invoked");
	}

	@Event
	@Priority(150)
	private void onSimpleEventPrivate(SimpleEvent event) {
		assertSame(1, event.getCounter());
		event.increment();
	}
}
