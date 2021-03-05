package stilldi.impl;

import cdi.lite.extension.phases.discovery.ContextBuilder;
import cdi.lite.extension.phases.discovery.MetaAnnotations;
import cdi.lite.extension.phases.enhancement.ClassConfig;

import javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator;
import java.lang.annotation.Annotation;
import java.util.List;

class MetaAnnotationsImpl implements MetaAnnotations {
    private final MetaAnnotationsHelper meta;
    private final List<ContextBuilderImpl> contexts;

    MetaAnnotationsImpl(MetaAnnotationsHelper meta, List<ContextBuilderImpl> contexts) {
        this.meta = meta;
        this.contexts = contexts;
    }

    @Override
    public ClassConfig addQualifier(Class<? extends Annotation> annotation) {
        AnnotatedTypeConfigurator<? extends Annotation> cfg = meta.newQualifier(annotation);
        return new ClassConfigImpl(cfg);
    }

    @Override
    public ClassConfig addInterceptorBinding(Class<? extends Annotation> annotation) {
        AnnotatedTypeConfigurator<? extends Annotation> cfg = meta.newInterceptorBinding(annotation);
        return new ClassConfigImpl(cfg);
    }

    @Override
    public ClassConfig addStereotype(Class<? extends Annotation> annotation) {
        AnnotatedTypeConfigurator<? extends Annotation> cfg = meta.newStereotype(annotation);
        return new ClassConfigImpl(cfg);
    }

    @Override
    public ContextBuilder addContext() {
        ContextBuilderImpl builder = new ContextBuilderImpl();
        contexts.add(builder);
        return builder;
    }
}
