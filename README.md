# Event Bus

## Introduction

This library allows passing events between different objects without them having a direct reference to each other.
Any object can serve as an event.

Using an instance of the `EventBus` class, an instance of the event class can be dispatched.
This means that it will be forwarded to all listeners registered for it at the event bus.

In addition, a singleton instance of the event bus is provided by the `EventBus#getInstance()` method.

To listen to events, register event handling methods using the `Event` annotation.
For this to work, the method must declare a single parameter of the desired event type.
Alternatively, a parameter-less event handler can be declared as shown [below](#parameter-less-event-handlers).

## A Simple Example

Lets look at a simple example: we declare the empty class `SimpleEvent` whose objects can be used as events.

```java
public class SimpleEvent {}
```

Next, an event listener for the `SimpleEvent` is declared:

```java
import dev.kske.eventbus.core.*;

public class SimpleEventListener {

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

Note that creating static event handlers like this

```java
@Event
private static void onSimpleEvent(SimpleEvent event) { ... }
```

is technically possible, however you would still have to create an instance of the event listener to register it at an event bus.

## Installation

Event Bus is available in Maven Central.
To include it inside your project, just add the following dependency to your `pom.xml`:

```xml
<dependencies>
    <dependency>
        <groupId>dev.kske</groupId>
        <artifactId>event-bus-core</artifactId>
        <version>1.2.0</version>
    </dependency>
</dependencies>
```

Then, require the Event Bus Core module in your `module-info.java`:

```java
requires dev.kske.eventbus.core;
```

If you intend to use event handlers that are inaccessible to Event Bus by means of Java language access control, make sure to allow reflective access to your package for Event Bus:

```java
opens my.package to dev.kske.eventbus.core;
```

## Polymorphic Event Handlers

On certain occasions it's practical for an event handler to accept both events of the specified type, as well as subclasses of that event.
To include subtypes for an event handler, use the `@Polymorphic` annotation in addition to `@Event`:

```java
@Event
@Polymorphic
private void onSimpleEvent(SimpleEvent event) { ... }
```

## Event Handler Execution Order

Sometimes when using multiple handlers for one event, it might be useful to define in which order they will be executed.
Event Bus assigns a priority to every handler, which is `100` by default, but can be explicitly set using the `@Priority` annotation in addition to `@Event`:

```java
@Event
@Priority(250)
private void onSimpleEvent(SimpleEvent event) { ... }
```

**Important:**
Events are dispatched to handlers in descending order of their priority.
The execution order is undefined for handlers with the same priority.

## Parameter-Less Event Handlers

In some cases an event handler is not interested in the dispatched event instance.
To avoid declaring a useless parameter just to specify the event type of the handler, there is an alternative:

```java
@Event(SimpleEvent.class)
private void onSimpleEvent() {
	System.out.println("SimpleEvent received!");
}
```

Make sure that you **do not** both declare a parameter and specify the event type in the annotation, as this would be ambiguous.

## Callback listeners

While defining event handlers as annotated methods is rather simple and readable, sometimes a more flexible approach is required.
For this reason, there are callback event handlers that allow the registration of an "inline" event listener consisting of just one handler in the form of a consumer:

```java
EventBus.getInstance().registerListener(SimpleEvent.class, e -> System.out.println("Received " + e));
```

The event type has to be defined explicitly, with the priority and polymorphism parameters being optional.
If you intend to remove the listener later, remember to keep a reference to it, as you would have to clear the entire event bus if you didn't.

## Listener-Level Properties

When defining a dedicated event listener that, for example, performs pre- or post-processing, all event handlers will probably have the same non-standard priority.
Instead of defining that priority for each handler, it can be defined at the listener level by annotating the listener itself.

The same applies to polymorphism.

## Event Consumption

In some cases it might be useful to stop the propagation of an event.
Event Bus makes this possible with event consumption:

```java
@Event(SimpleEvent.class)
@Priority(100)
private void onSimpleEvent() {
	EventBus.getInstance().cancel();
}

@Event(SimpleEvent.class)
@Priority(50)
private void onSimpleEvent2() {
	System.out.println("Will not be printed!");
}
```

In this example, the second method will not be executed as it has a lower priority and the event will not be propagated after consumption.
This applies to all event handlers that would have been executed after the one consuming the event.

**Important:**
Avoid cancelling events while using multiple event handlers with the same priority.
As event handlers are ordered by priority, it is not defined which of them will be executed after the event has been consumed.

## System Events

To accommodate for special circumstances in an event distribution, system events have been introduced.
At the moment, there are two system events, which are explained in this section.

### Detecting Unhandled Events

When an event is dispatched but not delivered to any handler, a dead event is dispatched that wraps the original event.
You can declare a dead event handler to respond to this situation:

```java
private void onDeadEvent(DeadEvent deadEvent) { ... }
```

### Detecting Exceptions Thrown by Event Handlers

When an event handler throws an exception, an exception event is dispatched that wraps the original event.
An exception handler is declared as follows:

```java
private void onExceptionEvent(ExceptionEvent ExceptionEvent) { ... }
```
Both system events reference the event bus that caused them and a warning is logged if they are unhandled.

#### Yeeting Exceptions Out of an Event Handler

In some cases, a warning about an `Exception` that was thrown in an event handler is not enough, stays unnoticed, or an exception should be catched explicitly.
Event Bus explicitly dispatches no `ExceptionEvent` when an `ExceptionWrapper` exception is thrown and instead simply rethrows it.
`ExceptionWrapper` is an unchecked exception that (as the name says) simply wraps an exception that caused it.
This means the following is possible and results in a normal program exit:
```java
@Event(String.class)
void onString() {
	throw new ExceptionWrapper(new RuntimeException("I failed!"));
}

void helloStackTrace() {
	EventBus.getInstance().registerListener(this);
	try {
		EventBus.getInstance().dispatch("A string!");
		System.exit(-1);
	} catch(ExceptionWrapper e) {
		e.getCause().printStackTrace();
		System.exit(0);
	}
}
```

### What About Endless Recursion Caused By Dead Events and Exception Events?

As one might imagine, an unhandled dead event would theoretically lead to an endless recursion.
The same applies when an exception event handler throws an exception.

To avoid this, system events never cause system events and instead just issue a warning to the logger.

## Inheritance

When a superclass or an interface of an event listener defines event handlers, they will be detected and registered by Event Bus, even if they are `private`.
If an event handler is overridden by the listener, the `@Event` annotation of the overridden method is automatically considered present on the overriding method.
If the overridden method contains an implementation, it is ignored as expected.

## Debugging

In more complex setups, taking a look at the event handler execution order can be helpful for debugging.
Event Bus offers a method for this purpose which can be used as follows:

```java
System.out.println(EventBus.getInstance().debugExecutionOrder(SimpleEvent.class));
```

Then, the execution order can be inspected in the console.

## Compile-Time Error Checking with Event Bus Proc

To assist you with writing event listeners, the Event Bus Proc (Annotation Processor) module enforces correct usage of the `@Event` annotation during compile time.
This reduces difficult-to-debug bugs that occur during runtime to compile-time errors which can be easily fixed.

The event annotation processor detects invalid event handlers and event type issues with more to come in future versions.

When using Maven, it can be registered using the Maven Compiler Plugin:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.8.1</version>
    <configuration>
        <annotationProcessorPaths>
            <annotationProcessorPath>
                <groupId>dev.kske</groupId>
                <artifactId>event-bus-proc</artifactId>
                <version>1.2.0</version>
            </annotationProcessorPath>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

Alternatively, a JAR file containing the processor is offered with each release for the use within IDEs and environments without Maven support.
