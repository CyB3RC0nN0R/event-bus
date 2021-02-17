package dev.kske.eventbus.proc;

import java.util.Set;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.Diagnostic.Kind;

import dev.kske.eventbus.core.*;

/**
 * This annotation processor checks event handlers for common mistakes which can only be detected
 * during runtime otherwise.
 *
 * @author Kai S. K. Engelbart
 * @since 1.0.0
 */
@SupportedAnnotationTypes("dev.kske.eventbus.core.Event")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class EventProcessor extends AbstractProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!roundEnv.errorRaised() && !roundEnv.processingOver())
			processRound(
				(Set<ExecutableElement>) roundEnv.getElementsAnnotatedWith(Event.class));

		// Do not claim the processed annotations
		return false;
	}

	private void processRound(Set<ExecutableElement> eventHandlers) {
		for (ExecutableElement eventHandler : eventHandlers) {
			Event		eventAnnotation	= eventHandler.getAnnotation(Event.class);
			TypeMirror	eventType;

			// Determine the event type and how it is defined
			boolean useParameter;
			try {
				eventAnnotation.value();
				throw new EventBusException(
					"Could not determine event type of handler " + eventHandler);
			} catch (MirroredTypeException e) {

				// Task failed successfully
				eventType		= e.getTypeMirror();
				useParameter	= processingEnv.getTypeUtils().isSameType(eventType,
					getTypeMirror(Event.USE_PARAMETER.class));
			}

			// Check handler signature
			boolean pass = false;
			if (useParameter && eventHandler.getParameters().size() == 0)
				error(eventHandler, "The method or the annotation must define the event type");
			else if (!useParameter && eventHandler.getParameters().size() == 1)
				error(eventHandler,
					"Either the method or the annotation must define the event type");
			else if (eventHandler.getParameters().size() > 1)
				error(eventHandler, "Method must not have more than one parameter");
			else if (eventHandler.getReturnType().getKind() != TypeKind.VOID)
				error(eventHandler, "Method must return void");
			else
				pass = true;

			// Abort checking if the handler signature is incorrect
			if (!pass)
				continue;

			// Additional checks if parameter is used
			if (useParameter) {
				VariableElement paramElement = eventHandler.getParameters().get(0);
				eventType = paramElement.asType();

				// Check if parameter is object
				// Abort checking otherwise
				if (eventType.getKind() != TypeKind.DECLARED) {
					error(paramElement, "Event must be an object");
					continue;
				}
			}

			// Check for handlers for abstract types that aren't polymorphic
			Element eventElement = ((DeclaredType) eventType).asElement();
			if (eventHandler.getAnnotation(Polymorphic.class) == null
				&& (eventElement.getKind() == ElementKind.INTERFACE
					|| eventElement.getModifiers().contains(Modifier.ABSTRACT))) {
				warning(eventHandler,
					"Parameter should be instantiable or handler should use @Polymorphic");
			}
		}
	}

	private TypeMirror getTypeMirror(Class<?> clazz) {
		return getTypeElement(clazz).asType();
	}

	private TypeElement getTypeElement(Class<?> clazz) {
		return processingEnv.getElementUtils().getTypeElement(clazz.getCanonicalName());
	}

	private void warning(Element e, String msg, Object... args) {
		processingEnv.getMessager().printMessage(Kind.WARNING, String.format(msg, args), e);
	}

	private void error(Element e, String msg, Object... args) {
		processingEnv.getMessager().printMessage(Kind.ERROR, String.format(msg, args), e);
	}
}
