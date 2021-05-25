package stilldi.impl;

import java.lang.annotation.Annotation;

final class AnnotationPresence {
    static boolean isAnnotationPresentAnywhere(jakarta.enterprise.inject.spi.AnnotatedType<?> cdiClassDeclaration,
            Class<? extends Annotation> annotationType) {
        if (cdiClassDeclaration.isAnnotationPresent(annotationType)) {
            return true;
        }
        if (cdiClassDeclaration.getFields()
                .stream()
                .anyMatch(it -> it.isAnnotationPresent(annotationType))) {
            return true;
        }
        if (cdiClassDeclaration.getMethods()
                .stream()
                .anyMatch(it -> it.isAnnotationPresent(annotationType))) {
            return true;
        }
        if (cdiClassDeclaration.getMethods()
                .stream()
                .flatMap(it -> it.getParameters().stream())
                .anyMatch(it -> it.isAnnotationPresent(annotationType))) {
            return true;
        }
        if (cdiClassDeclaration.getConstructors()
                .stream()
                .anyMatch(it -> it.isAnnotationPresent(annotationType))) {
            return true;
        }
        if (cdiClassDeclaration.getConstructors()
                .stream()
                .flatMap(it -> it.getParameters().stream())
                .anyMatch(it -> it.isAnnotationPresent(annotationType))) {
            return true;
        }
        return false;
    }
}
