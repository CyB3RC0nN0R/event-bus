/**
 * Contains an annotation processor for checking for errors related to the
 * {@link dev.kske.eventbus.core.Event} annotation from Event Bus.
 *
 * @author Kai S. K. Engelbart
 * @since 1.0.0
 */
module dev.kske.eventbus.proc {

	requires java.compiler;
	requires dev.kske.eventbus.core;
}
