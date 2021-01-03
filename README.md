# Event Bus

## Introduction

This library allows passing events between different objects without them having a direct reference to each other.
Any class can be made an event by implementing the `IEvent` interface.

Using an instance of the `EventBus` class, an instant of the event class can be dispatched.
This means that it will be forwarded to all listeners registered for it at the event bus.

In addition, a singleton instance of the event bus is provided by the `EventBus#getInstance()` method.

To listen to events, register event handling methods using the `Event` annotation.
For this to work, the method must have a return type of `void` and declare a single parameter of the desired event type.
Alternatively, a parameter-less event handler can be declared as shown [below](#parameter-less-event-handlers).
Additionally, the class containing the method must implement the `EventListener` interface.

## A Simple Example

Lets look at a simple example: we declare the empty class `SimpleEvent` that implements `IEvent` and can thus be used as an event.

```java
import dev.kske.eventbus.IEvent;

public class SimpleEvent implements IEvent {}
```

Next, an event listener for the `SimpleEvent` is declared:

```java
import dev.kske.eventbus.*;

public class SimpleEventListener implements EventListener {

    public SimpleEventListener() {

        // Register this listener at the event bus
        EventBus.getInstance().register(this);

        // Dispatch a SimpleEvent
        EventBus.getInstance().dispatch(new SimpleEvent());
    }

    @Event
    private void onSimpleEvent(SimpleEvent event) {
        System.out.println("SimpleEvent received!");
    }
}
```

In this case, an event bus is created and used locally.
In a more sophisticated example the class would acquire an external event bus that is used by multiple classes.

Note that creating static event handlers like this

```java
    @Event
    private static void onSimpleEvent(SimpleEvent event) ...
```

is technically possible, however you would still have to create an instance of the event listener to register it at an event bus.

## Event handlers for subtypes

On certain occasions its practical for an event handler to accept both events of the specified type, as well as subclasses of that event.
To include subtypes for an event handler, use the `includeSubtypes` parameter as follows:

```java
@Event(includeSubtypes = true)
```

## Event handler execution order

Sometimes when using multiple handlers for one event, it might be useful to know in which order they will be executed.
Event Bus provides a mechanism to ensure the correct propagation of events: the `priority`.

Priority can be set on the `@Event` annotation like that:
```java
@Event(priority=100)
```

The default priority for events is `100`.

**Important:**
Events are dispatched top-down, meaning the event handler with the highest priority will be executed first.

If no priority is set or multiple handlers have the same priority, the order of execution is undefined.

## Parameter-less event handlers

In some cases an event handler is not interested in the dispatched event instance.
To avoid declaring a useless parameter just to specify the event type of the handler, there is an alternative:

```java
@Event(eventType = SimpleEvent.class)
private void onSimpleEvent() {
	System.out.println("SimpleEvent received!");
}
```

Make sure that you **do not** declare both a parameter and the `eventType` value of the annotation, as this would be ambiguous.

## Event consumption

In some cases it might be useful to stop the propagation of an event.
Event Bus makes this possible with event consumption:

```java
@Event(eventType = SimpleEvent.class, priority=100)
private void onSimpleEvent() {
	EventBus.getInstance().cancel();
}

@Event(eventType = SimpleEvent.class, priority=50)
private void onSimpleEvent2() {
	System.out.println("Will not be printed!");
}
```

In this example, the second method will not be executed as it has a lower priority and the event will not be propagated after consumption.
This applies to all event handlers that would have been executed after the one consuming the event.

**Important:**
Avoid cancelling events while using multiple event handlers with the same priority.
As event handlers are ordered by priority, it is not defined which of them will be executed after the event has been consumed.

## Installation

Event Bus is currently hosted at [kske.dev](https://kske.dev).
To include it inside your project, just add the Maven repository and the dependency to your `pom.xml`:

```xml
<repositories>
	<repository>
		<id>kske-repo</id>
		<url>https://kske.dev/maven-repo</url>
	</repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>dev.kske</groupId>
        <artifactId>event-bus</artifactId>
        <version>0.1.0</version>
    </dependency>
</dependencies>
```
