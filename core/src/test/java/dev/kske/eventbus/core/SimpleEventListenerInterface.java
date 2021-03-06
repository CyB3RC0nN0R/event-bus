package dev.kske.eventbus.core;

/**
 * An interface defining a single handler for {@link SimpleEvent}.
 *
 * @author Kai S. K. Engelbart
 * @since 1.3.0
 */
interface SimpleEventListenerInterface {

	@Priority(120)
	@Event
	void onSimpleEventInterfaceHandler(SimpleEvent event);
}
