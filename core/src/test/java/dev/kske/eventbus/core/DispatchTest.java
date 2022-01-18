package dev.kske.eventbus.core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests the dispatching mechanism of the event bus.
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
@Polymorphic
@Priority(150)
class DispatchTest {

	EventBus bus;

	/**
	 * Constructs an event bus and registers this test instance as an event listener.
	 *
	 * @since 0.0.1
	 */
	@BeforeEach
	void registerListener() {
		bus = new EventBus();
		bus.registerListener(this);
		bus.registerListener(SimpleEvent.class, e -> {
			e.increment();
			assertEquals(3, e.getCounter());
		});
	}

	/**
	 * Tests {@link EventBus#dispatch(Object)} with multiple handler priorities, a polymorphic
	 * handler and a static handler.
	 *
	 * @since 0.0.1
	 */
	@Test
	void testDispatch() {
		bus.dispatch(new SimpleEventSub());
		bus.dispatch(new SimpleEvent());
	}

	/**
	 * Tests {@link EventBus#debugExecutionOrder(Class)} based on the currently registered handlers.
	 *
	 * @since 1.2.0
	 */
	@Test
	void testDebugExecutionOrder() {
		String executionOrder = bus.debugExecutionOrder(SimpleEvent.class);
		System.out.println(executionOrder);
		assertEquals(
			"Event handler execution order for class dev.kske.eventbus.core.SimpleEvent (3 handler(s)):\n"
				+ "==========================================================================================\n"
				+ "ReflectiveEventHandler[eventType=class dev.kske.eventbus.core.SimpleEvent, polymorphic=true, priority=200, method=void dev.kske.eventbus.core.DispatchTest.onSimpleEventFirst(dev.kske.eventbus.core.SimpleEvent), useParameter=true]\n"
				+ "ReflectiveEventHandler[eventType=class dev.kske.eventbus.core.SimpleEvent, polymorphic=false, priority=150, method=static void dev.kske.eventbus.core.DispatchTest.onSimpleEventSecond(dev.kske.eventbus.core.SimpleEvent), useParameter=true]\n"
				+ "CallbackEventHandler[eventType=class dev.kske.eventbus.core.SimpleEvent, polymorphic=false, priority=100]\n"
				+ "==========================================================================================",
			executionOrder);
	}

	/**
	 * Tests whether the handlers bound to an event type are correct when retrieved from the binding
	 * cache. On the second call of {@link EventBus#debugExecutionOrder(Class)} the cache is used.
	 *
	 * @since 1.3.0
	 */
	@Test
	void testBindingCache() {
		String executionOrder = bus.debugExecutionOrder(SimpleEventSub.class);
		System.out.println(executionOrder);
		assertEquals(executionOrder, bus.debugExecutionOrder(SimpleEventSub.class));
	}

	@Event
	@Priority(200)
	void onSimpleEventFirst(SimpleEvent event) {
		event.increment();
		assertTrue(event.getCounter() == 1 || event.getCounter() == 2);
	}

	@Event
	@Polymorphic(false)
	static void onSimpleEventSecond(SimpleEvent event) {
		event.increment();
		assertEquals(2, event.getCounter());
	}
}
