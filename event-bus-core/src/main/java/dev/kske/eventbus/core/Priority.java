package dev.kske.eventbus.core;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Defines the priority of an event handler. Handlers are executed in descending order of their
 * priority.
 * <p>
 * Handlers without this annotation have the default priority of 100.
 * <p>
 * The execution order of handlers with the same priority is undefined.
 *
 * @author Kai S. K. Engelbart
 * @since 1.0.0
 * @see Event
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Priority {

	/**
	 * @return the priority of the event handler
	 * @since 1.0.0
	 */
	int value();
}
