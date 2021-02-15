package dev.kske.eventbus.ap;

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
			TypeElement	eventListener	= (TypeElement) eventHandler.getEnclosingElement();
			Event		eventAnnotation	= eventHandler.getAnnotation(Event.class);

			// Determine how the event type is defined
			boolean useParameter;
			try {
				eventAnnotation.eventType();
				throw new EventBusException(
					"Could not determine event type of handler " + eventHandler);
			} catch (MirroredTypeException e) {

				// Task failed successfully
				useParameter = processingEnv.getTypeUtils().isSameType(e.getTypeMirror(),
					getTypeMirror(Event.USE_PARAMETER.class));
			}

			// Check for correct method signature and return type
			if (eventHandler.getParameters().size() == 0 && useParameter)
				error(eventHandler, "The method or the annotation must define the event type");

			if (eventHandler.getParameters().size() == 1 && !useParameter)
				error(eventHandler,
					"Either the method or the annotation must define the event type");

			if (eventHandler.getParameters().size() > 1)
				error(eventHandler, "Method must not have more than one parameter");

			if (eventHandler.getReturnType().getKind() != TypeKind.VOID)
				error(eventHandler, "Method must return void");

			// Get first parameter as type and element
			var	paramElement	= eventHandler.getParameters().get(0);
			var	paramType		= paramElement.asType();

			// Check for valid event type
			if (useParameter && !processingEnv.getTypeUtils().isAssignable(paramType,
				getTypeMirror(IEvent.class)))
				error(paramElement, "Parameter must implement IEvent");

			// Check for handlers for abstract types that don't include subtypes
			if (!eventAnnotation.includeSubtypes() && paramType.getKind() == TypeKind.DECLARED) {
				var declaredElement = ((DeclaredType) paramType).asElement();
				if (declaredElement.getKind() == ElementKind.INTERFACE
					|| declaredElement.getModifiers().contains(Modifier.ABSTRACT))
					warning(paramElement,
						"Parameter should be instantiable or handler should include subtypes");
			}

			// Check listener for interface implementation
			if (!eventListener.getInterfaces().contains(getTypeMirror(EventListener.class)))
				warning(eventHandler.getEnclosingElement(),
					"Class should implement EventListener interface");
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
