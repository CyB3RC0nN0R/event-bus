package dev.kske.eventbus.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Allows an event handler to receive events that are subtypes of the declared event type.
 * <p>
 * This is useful when defining an event handler for an interface or an abstract class.
 *
 * @author Kai S. K. Engelbart
 * @since 1.0.0
 * @see Event
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Polymorphic {}
