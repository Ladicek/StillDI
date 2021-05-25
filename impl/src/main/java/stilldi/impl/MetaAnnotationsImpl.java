package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.ContextBuilder;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Consumer;

class MetaAnnotationsImpl implements MetaAnnotations {
    private final MetaAnnotationsHelper meta;
    private final List<ContextBuilderImpl> contexts;

    MetaAnnotationsImpl(MetaAnnotationsHelper meta, List<ContextBuilderImpl> contexts) {
        this.meta = meta;
        this.contexts = contexts;
    }

    @Override
    public void addQualifier(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config) {
        AnnotatedTypeConfigurator<? extends Annotation> cfg = meta.newQualifier(annotation);
        config.accept(new ClassConfigImpl(cfg));
    }

    @Override
    public void addInterceptorBinding(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config) {
        AnnotatedTypeConfigurator<? extends Annotation> cfg = meta.newInterceptorBinding(annotation);
        config.accept(new ClassConfigImpl(cfg));
    }

    @Override
    public void addStereotype(Class<? extends Annotation> annotation, Consumer<ClassConfig<?>> config) {
        AnnotatedTypeConfigurator<? extends Annotation> cfg = meta.newStereotype(annotation);
        config.accept(new ClassConfigImpl(cfg));
    }

    @Override
    public ContextBuilder addContext() {
        ContextBuilderImpl builder = new ContextBuilderImpl();
        contexts.add(builder);
        return builder;
    }
}
