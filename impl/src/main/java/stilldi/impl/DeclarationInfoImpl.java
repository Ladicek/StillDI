package stilldi.impl;

import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.DeclarationInfo;
import stilldi.impl.util.fake.AnnotatedPackage;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// TODO all *Info subclasses have equals/hashCode, but *Config do not, and that's probably correct?
abstract class DeclarationInfoImpl<CdiDeclaration extends jakarta.enterprise.inject.spi.Annotated> implements DeclarationInfo {
    final CdiDeclaration cdiDeclaration;

    DeclarationInfoImpl(CdiDeclaration cdiDeclaration) {
        this.cdiDeclaration = cdiDeclaration;
    }

    static DeclarationInfo fromCdiDeclaration(jakarta.enterprise.inject.spi.Annotated cdiDeclaration) {
        if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedType) {
            return new ClassInfoImpl((jakarta.enterprise.inject.spi.AnnotatedType<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedCallable) { // method or constructor
            return new MethodInfoImpl((jakarta.enterprise.inject.spi.AnnotatedCallable<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedParameter) {
            return new ParameterInfoImpl((jakarta.enterprise.inject.spi.AnnotatedParameter<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof jakarta.enterprise.inject.spi.AnnotatedField) {
            return new FieldInfoImpl((jakarta.enterprise.inject.spi.AnnotatedField<?>) cdiDeclaration);
        } else if (cdiDeclaration instanceof AnnotatedPackage) {
            return new PackageInfoImpl((AnnotatedPackage) cdiDeclaration);
        } else {
            throw new IllegalArgumentException("Unknown declaration " + cdiDeclaration);
        }
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
        return new AnnotationInfoImpl(cdiDeclaration, null,
                cdiDeclaration.getAnnotation(annotationType));
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
    public String toString() {
        return cdiDeclaration.toString();
    }
}
