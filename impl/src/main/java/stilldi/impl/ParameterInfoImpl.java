package stilldi.impl;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.MethodInfo;
import jakarta.enterprise.lang.model.declarations.ParameterInfo;
import jakarta.enterprise.lang.model.types.Type;
import stilldi.impl.util.AnnotationOverrides;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ParameterInfoImpl extends DeclarationInfoImpl<java.lang.reflect.Parameter, jakarta.enterprise.inject.spi.AnnotatedParameter<?>> implements ParameterInfo {
    // only for equals/hashCode and going back to the method
    private final MethodInfoImpl method;
    private final int position;

    ParameterInfoImpl(jakarta.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration) {
        super(cdiDeclaration.getJavaParameter(), cdiDeclaration);
        this.method = new MethodInfoImpl(cdiDeclaration.getDeclaringCallable());
        this.position = cdiDeclaration.getPosition();
    }

    ParameterInfoImpl(Parameter reflectionDeclaration, MethodInfoImpl backReference, int position) {
        super(reflectionDeclaration, null);
        this.method = backReference;
        this.position = position;
    }

    @Override
    public String name() {
        return reflection.getName();
    }

    @Override
    public Type type() {
        if (canSuperHandleAnnotations()) {
            return TypeImpl.fromReflectionType(reflection.getAnnotatedType());
        }

        AnnotationOverrides overrides = new AnnotationOverrides(reflection.getDeclaringExecutable()
                .getAnnotatedParameterTypes()[position].getAnnotations());
        return TypeImpl.fromReflectionType(reflection.getAnnotatedType(), overrides);
    }

    @Override
    public MethodInfo declaringMethod() {
        return method;
    }

    private boolean canSuperHandleAnnotations() {
        if (cdiDeclaration != null) {
            return true;
        }

        java.lang.reflect.Executable method = this.method.reflection;
        if (method.getParameterTypes().length == method.getParameterAnnotations().length) {
            return true;
        }

        return false;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        if (canSuperHandleAnnotations()) {
            return super.hasAnnotation(annotationType);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        return Arrays.stream(annotations)
                .anyMatch(it -> annotationType.isAssignableFrom(it.annotationType()));
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
        if (canSuperHandleAnnotations()) {
            return super.hasAnnotation(predicate);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        return Arrays.stream(annotations)
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(it)));
    }

    @Override
    public <T extends Annotation> AnnotationInfo annotation(Class<T> annotationType) {
        if (canSuperHandleAnnotations()) {
            return super.annotation(annotationType);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        T annotation = new AnnotationOverrides(annotations).getAnnotation(annotationType);
        return annotation != null ? new AnnotationInfoImpl(annotation) : null;
    }

    @Override
    public <T extends Annotation> Collection<AnnotationInfo> repeatableAnnotation(Class<T> annotationType) {
        if (canSuperHandleAnnotations()) {
            return super.repeatableAnnotation(annotationType);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        return Arrays.stream(new AnnotationOverrides(annotations).getAnnotationsByType(annotationType))
                .map(AnnotationInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        if (canSuperHandleAnnotations()) {
            return super.annotations(predicate);
        }

        Annotation[] annotations = method.reflection.getParameterAnnotations()[position];
        return Arrays.stream(annotations)
                .map(AnnotationInfoImpl::new)
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        String name = name();
        return "parameter " + (name != null ? name : position) + " of method "
                + cdiDeclaration.getDeclaringCallable().getJavaMember().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterInfoImpl that = (ParameterInfoImpl) o;
        return position == that.position
                && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, position);
    }
}
