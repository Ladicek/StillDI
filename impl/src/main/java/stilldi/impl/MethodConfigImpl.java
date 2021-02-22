package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.phases.enhancement.MethodConfig;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

class MethodConfigImpl extends MethodInfoImpl implements MethodConfig<Object> {
    private final javax.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> configurator;

    MethodConfigImpl(javax.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> configurator) {
        super(configurator.getAnnotated());
        this.configurator = configurator;
    }

    @Override
    public void addAnnotation(Class<? extends Annotation> annotationType, AnnotationAttribute... attributes) {
        configurator.add(AnnotationProxy.create(annotationType, attributes));
    }

    @Override
    public void addAnnotation(ClassInfo<?> annotationType, AnnotationAttribute... attributes) {
        Class<? extends Annotation> clazz = (Class<? extends Annotation>) ((ClassInfoImpl) annotationType).cdiDeclaration.getJavaClass();
        configurator.add(AnnotationProxy.create(clazz, attributes));
    }

    @Override
    public void addAnnotation(AnnotationInfo annotation) {
        Class<? extends Annotation> clazz = ((AnnotationInfoImpl) annotation).annotation.annotationType();
        configurator.add(AnnotationProxy.create(clazz, annotation.attributes()));
    }

    @Override
    public void addAnnotation(Annotation annotation) {
        configurator.add(annotation);
    }

    @Override
    public void removeAnnotation(Predicate<AnnotationInfo> predicate) {
        configurator.remove(annotation -> {
            AnnotationInfo info = new AnnotationInfoImpl(MethodConfigImpl.this.cdiDeclaration, null, annotation);
            return predicate.test(info);
        });
    }

    @Override
    public void removeAllAnnotations() {
        configurator.removeAll();
    }
}
