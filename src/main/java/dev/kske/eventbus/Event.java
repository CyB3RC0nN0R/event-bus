package dev.kske.eventbus;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.*;

/**
 * Indicates that a method is an event handler. To be successfully used as such, the method has to
 * comply with the following specifications:
 * <ul>
 * <li>Declared inside a class that implements {@link EventListener}</li>
 * <li>One parameter of a type that implements {@link IEvent}</li>
 * <li>Return type of {@code void}</li>
 * </ul>
 *
 * @author Kai S. K. Engelbart
 * @since 0.0.1
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface Event {

	/**
	 * Defines the priority of the event handler. Handlers are executed in descending order of their
	 * priority.
	 * <p>
	 * The execution order of handlers with the same priority is undefined.
	 *
	 * @since 0.0.1
	 */
	int priority() default 100;
}
