package dev.kske.eventbus.core;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Allows an event handler to receive events that are subtypes of the declared event type.
 * <p>
 * When used on a type, the value applies to all event handlers declared within that type that don't
 * define a value on their own.
 * <p>
 * This is useful when defining an event handler for an interface or an abstract class.
 *
 * @author Kai S. K. Engelbart
 * @since 1.0.0
 * @see Event
 */
@Documented
@Retention(RUNTIME)
@Target({ METHOD, TYPE })
public @interface Polymorphic {

	/**
	 * @return whether the event handler is polymorphic
	 * @since 1.1.0
	 */
	boolean value() default true;
}
