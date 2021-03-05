package stilldi.impl;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ParameterInfoImpl extends DeclarationInfoImpl<javax.enterprise.inject.spi.AnnotatedParameter<?>> implements ParameterInfo {
    // only for equals/hashCode
    private final MethodInfoImpl method;
    private final int position;

    ParameterInfoImpl(javax.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration) {
        super(cdiDeclaration);
        this.method = new MethodInfoImpl(cdiDeclaration.getDeclaringCallable());
        this.position = cdiDeclaration.getPosition();
    }

    @Override
    public String name() {
        return cdiDeclaration.getJavaParameter().getName();
    }

    @Override
    public Type type() {
        return TypeImpl.fromReflectionType(cdiDeclaration.getJavaParameter().getAnnotatedType());
    }

    @Override
    public MethodInfo<?> declaringMethod() {
        return new MethodInfoImpl(cdiDeclaration.getDeclaringCallable());
    }

    @Override
    public String toString() {
        String name = name();
        return "parameter " + (name != null ? name : position) + " of method "
                + cdiDeclaration.getDeclaringCallable().getJavaMember().toString();
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return cdiDeclaration.isAnnotationPresent(annotationType);
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
        return cdiDeclaration.getAnnotations()
                .stream()
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(cdiDeclaration, null, it)));
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return new AnnotationInfoImpl(cdiDeclaration, null, cdiDeclaration.getAnnotation(annotationType));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return cdiDeclaration.getAnnotations(annotationType)
                .stream()
                .map(it -> new AnnotationInfoImpl(cdiDeclaration, null, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        return cdiDeclaration.getAnnotations()
                .stream()
                .map(it -> new AnnotationInfoImpl(cdiDeclaration, null, it))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return annotations(it -> true);
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
