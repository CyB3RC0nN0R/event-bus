package dev.kske.eventbus.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Indicates that a method is an event handler. To be successfully used as such, the method has to
 * comply with the following specifications:
 * <ul>
 * <li>Declared inside a class that implements {@link EventListener}</li>
 * <li>Specifying an event type by either
 * <ul>
 * <li>Declaring one parameter of a type that implements {@link IEvent}</li>
 * <li>Defining the class of the event using the annotation value</li>
 * </ul>
 * </li>
 * <li>Return type of {@code void}</li>
 * </ul>
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 * @see Polymorphic
 * @see Priority
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Event {

	/**
	 * Defines the event type the handler listens to. If this value is set, the handler is not
	 * allowed to declare parameters.
	 * <p>
	 * This is useful when the event handler does not utilize the event instance.
	 *
	 * @return the event type accepted by the handler
	 * @since 1.0.0
	 */
	Class<? extends IEvent> value() default USE_PARAMETER.class;

	/**
	 * Signifies that the event type the handler listens to is determined by the type of its only
	 * parameter.
	 *
	 * @since 0.0.3
	 */
	static final class USE_PARAMETER implements IEvent {}
}
