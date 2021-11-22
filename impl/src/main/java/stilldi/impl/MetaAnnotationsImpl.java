package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

class MetaAnnotationsImpl implements MetaAnnotations {
    private final jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd;

    private final List<StereotypeConfigurator<?>> stereotypes;
    private final List<ContextData> contexts;

    MetaAnnotationsImpl(jakarta.enterprise.inject.spi.BeforeBeanDiscovery bbd, List<StereotypeConfigurator<?>> stereotypes, List<ContextData> contexts) {
        this.bbd = bbd;

        this.stereotypes = stereotypes;
        this.contexts = contexts;
    }

    @Override
    public ClassConfig addQualifier(Class<? extends Annotation> annotation) {
        jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<? extends Annotation> cfg = bbd.configureQualifier(annotation);
        return new ClassConfigImpl(cfg);
    }

    @Override
    public ClassConfig addInterceptorBinding(Class<? extends Annotation> annotation) {
        jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<? extends Annotation> cfg = bbd.configureInterceptorBinding(annotation);
        return new ClassConfigImpl(cfg);
    }

    @Override
    public ClassConfig addStereotype(Class<? extends Annotation> annotation) {
        StereotypeConfigurator<? extends Annotation> cfg = new StereotypeConfigurator<>(annotation);
        stereotypes.add(cfg);
        return new ClassConfigImpl(cfg);
    }

    @Override
    public void addContext(Class<? extends Annotation> scopeAnnotation,
            Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass) {
        contexts.add(new ContextData(scopeAnnotation, null, contextClass));
    }

    @Override
    public void addContext(Class<? extends Annotation> scopeAnnotation, boolean isNormal,
            Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass) {
        contexts.add(new ContextData(scopeAnnotation, isNormal, contextClass));
    }

    static final class ContextData {
        final Class<? extends Annotation> scopeAnnotation;
        final Boolean isNormal; // null if not set, in which case it's derived from the scope annotation

        final Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass;

        ContextData(Class<? extends Annotation> scopeAnnotation, Boolean isNormal,
                Class<? extends jakarta.enterprise.context.spi.AlterableContext> contextClass) {
            this.scopeAnnotation = scopeAnnotation;
            this.isNormal = isNormal;

            this.contextClass = contextClass;
        }
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
