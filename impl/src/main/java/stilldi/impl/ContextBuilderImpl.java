package stilldi.impl;

import cdi.lite.extension.phases.discovery.ContextBuilder;

import java.lang.annotation.Annotation;

class ContextBuilderImpl implements ContextBuilder {
    Class<? extends javax.enterprise.context.spi.AlterableContext> implementationClass;
    Class<? extends Annotation> scopeAnnotation;
    Boolean isNormal; // null if not set, in which case it's derived from the scope annotation

    @Override
    public ContextBuilder scope(Class<? extends Annotation> scopeAnnotation) {
        return null;
    }

    @Override
    public ContextBuilder normal(boolean isNormal) {
        return null;
    }

    @Override
    public ContextBuilder implementation(Class<? extends javax.enterprise.context.spi.AlterableContext> implementationClass) {
        return null;
    }
}
