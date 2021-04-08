package stilldi.impl;

import java.lang.annotation.Annotation;
import java.util.Set;
import java.util.function.Consumer;

class EnhancementAction {
    private final Set<Class<?>> exactClasses;
    private final Set<Class<?>> superclasses;
    private final Set<Class<? extends Annotation>> requiredAnnotations;
    private final Consumer<javax.enterprise.inject.spi.ProcessAnnotatedType<?>> acceptor;

    EnhancementAction(Set<Class<?>> exactClasses, Set<Class<?>> superclasses,
            Set<Class<? extends Annotation>> requiredAnnotations,
            Consumer<javax.enterprise.inject.spi.ProcessAnnotatedType<?>> acceptor) {
        this.exactClasses = exactClasses;
        this.superclasses = superclasses;
        this.requiredAnnotations = requiredAnnotations;
        this.acceptor = acceptor;
    }

    void run(javax.enterprise.inject.spi.ProcessAnnotatedType<?> pat) {
        if (!satisfies(pat.getAnnotatedType())) {
            return;
        }

        acceptor.accept(pat);
    }

    private boolean satisfies(javax.enterprise.inject.spi.AnnotatedType<?> annotatedType) {
        Class<?> inspectedClass = annotatedType.getJavaClass();
        if (exactClasses.contains(inspectedClass)) {
            return satisfiesAnnotationConstraints(annotatedType);
        } else {
            for (Class<?> superclass : superclasses) {
                if (Subtyping.isSubtype(superclass, inspectedClass)) {
                    return satisfiesAnnotationConstraints(annotatedType);
                }
            }
        }
        return false;
    }

    private boolean satisfiesAnnotationConstraints(javax.enterprise.inject.spi.AnnotatedType<?> annotatedType) {
        for (Class<? extends Annotation> requiredAnnotation : requiredAnnotations) {
            if (Annotation.class.equals(requiredAnnotation)) {
                return true;
            }

            if (AnnotationPresence.isAnnotationPresentAnywhere(annotatedType, requiredAnnotation)) {
                return true;
            }
        }

        return false;
    }
}
