package dev.kske.eventbus.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Indicates that a method is an event handler.
 * <p>
 * To be successfully used as such, the method has to specify the event type by either declaring one
 * parameter of that type or setting the annotation value to the corresponding class.
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
	 * Defines the event type the handler listens for. If this value is set, the handler is not
	 * allowed to declare parameters.
	 * <p>
	 * This is useful when the event handler does not utilize the event instance.
	 *
	 * @return the event type accepted by the handler
	 * @since 1.0.0
	 */
	Class<?> value() default USE_PARAMETER.class;

	/**
	 * Signifies that the event type the handler listens to is determined by the type of its only
	 * parameter.
	 *
	 * @since 0.0.3
	 */
	static final class USE_PARAMETER {}
}
