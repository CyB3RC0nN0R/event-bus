package dev.kske.eventbus.core;

/**
 * A simple event for testing purposes. The event contains a counter that is supposed to be
 * incremented when the event is processed by a handler. That way it is possible to test whether all
 * handlers that were supposed to be invoked were in fact invoked.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
class SimpleEvent {

	private int counter;

	@Override
	public String toString() {
		return String.format("SimpleEvent[%d]", counter);
	}

	void increment() {
		++counter;
	}

	int getCounter() {
		return counter;
	}

	void reset() {
		counter = 0;
	}
}
