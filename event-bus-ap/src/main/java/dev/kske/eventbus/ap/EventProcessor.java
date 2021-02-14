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

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(Event.class)) {
			ExecutableElement	method			= (ExecutableElement) annotatedElement;
			Event				eventAnnotation	= method.getAnnotation(Event.class);
			boolean				useParameter	= false;

			// Determine how the event type is defined
			try {
				eventAnnotation.eventType();
			} catch (MirroredTypeException e) {

				// Task failed successfully
				useParameter = processingEnv.getTypeUtils().isSameType(e.getTypeMirror(),
					processingEnv.getElementUtils()
						.getTypeElement(Event.USE_PARAMETER.class.getCanonicalName()).asType());
			}

			// Check for correct method signature and return type
			if (method.getParameters().size() == 0 && useParameter)
				error(method, "The method or the annotation should define the event type");

			if (method.getParameters().size() == 1 && !useParameter)
				error(method, "Either the method or the annotation should define the event type");

			if (method.getParameters().size() > 1)
				error(method, "Method should not have more than one parameter");

			if (!method.getReturnType().getKind().equals(TypeKind.VOID))
				error(method, "Method needs a return type of void");

			// Check event type
			var paramType = ((ExecutableType) method.asType()).getParameterTypes().get(0);
			if (useParameter && !processingEnv.getTypeUtils().isAssignable(paramType,
				processingEnv.getElementUtils()
					.getTypeElement(IEvent.class.getCanonicalName()).asType()))
				error(method.getParameters().get(0), "Parameter should implement IEvent");

			// Check listener for interface implementation
			if (!((TypeElement) method.getEnclosingElement()).getInterfaces().contains(processingEnv
				.getElementUtils().getTypeElement(EventListener.class.getCanonicalName()).asType()))
				warning(method.getEnclosingElement(),
					"Class should implement EventListener interface");
		}
		return true;
	}

	private void warning(Element e, String msg, Object... args) {
		processingEnv.getMessager().printMessage(Kind.WARNING, String.format(msg, args), e);
	}

	private void error(Element e, String msg, Object... args) {
		processingEnv.getMessager().printMessage(Kind.ERROR, String.format(msg, args), e);
	}
}
