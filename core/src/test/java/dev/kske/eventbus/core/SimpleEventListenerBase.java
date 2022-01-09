package dev.kske.eventbus.core;

/**
 * An abstract class defining a package-private and a private handler for {@link SimpleEvent}.
 *
 * @author Kai S. K. Engelbart
 * @since 1.3.0
 */
abstract class SimpleEventListenerBase {

	@Event
	void onSimpleEventAbstractHandler(SimpleEvent event) {
		event.increment();
	}

	@Event
	private void onSimpleEventPrivate(SimpleEvent event) {
		event.increment();
	}
}
