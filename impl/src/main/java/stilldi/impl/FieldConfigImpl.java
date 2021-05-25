package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.lang.model.AnnotationAttribute;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;

class FieldConfigImpl extends FieldInfoImpl implements FieldConfig<Object> {
    private final jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> configurator;

    FieldConfigImpl(jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> configurator) {
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
            AnnotationInfo info = new AnnotationInfoImpl(FieldConfigImpl.this.cdiDeclaration, null, annotation);
            return predicate.test(info);
        });
    }

    @Override
    public void removeAllAnnotations() {
        configurator.removeAll();
    }
}
