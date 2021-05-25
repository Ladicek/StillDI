package stilldi.impl;

import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

final class MetaAnnotationsHelper {
    private final BeforeBeanDiscovery bdd;

    final List<StereotypeConfigurator<?>> newStereotypes = new ArrayList<>();

    public MetaAnnotationsHelper(BeforeBeanDiscovery bdd) {
        this.bdd = bdd;
    }

    <T extends Annotation> jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<T> newQualifier(Class<T> annotation) {
        return bdd.configureQualifier(annotation);
    }

    <T extends Annotation> jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<T> newInterceptorBinding(Class<T> annotation) {
        return bdd.configureInterceptorBinding(annotation);
    }

    <T extends Annotation> jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<T> newStereotype(Class<T> annotation) {
        StereotypeConfigurator<T> result = new StereotypeConfigurator<T>(annotation);
        newStereotypes.add(result);
        return result;
    }

    static final class StereotypeConfigurator<T extends Annotation> implements jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<T> {
        final Class<T> annotation;
        final Set<Annotation> annotations = new HashSet<>();

        StereotypeConfigurator(Class<T> annotation) {
            this.annotation = annotation;
        }

        @Override
        public jakarta.enterprise.inject.spi.AnnotatedType<T> getAnnotated() {
            return BeanManagerAccess.createAnnotatedType(annotation);
        }

        @Override
        public jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<T> add(Annotation annotation) {
            annotations.add(annotation);
            return this;
        }

        @Override
        public jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<T> remove(Predicate<Annotation> predicate) {
            annotations.removeIf(predicate);
            return this;
        }

        @Override
        public Set<jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<? super T>> methods() {
            // TODO
            return Collections.emptySet();
        }

        @Override
        public Set<jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<? super T>> fields() {
            // TODO
            return Collections.emptySet();
        }

        @Override
        public Set<jakarta.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator<T>> constructors() {
            // TODO
            return Collections.emptySet();
        }
    }
}
