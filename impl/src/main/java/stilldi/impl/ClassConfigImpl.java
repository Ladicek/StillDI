package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.MethodConfig;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class ClassConfigImpl extends ClassInfoImpl implements ClassConfig<Object> {
    private final javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<?> configurator;

    ClassConfigImpl(javax.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator<?> configurator) {
        super(configurator.getAnnotated());
        this.configurator = configurator;
    }

    @Override
    public Collection<? extends MethodConfig<Object>> constructors() {
        Set<Constructor<?>> declared = new HashSet<>(Arrays.asList(
                cdiDeclaration.getJavaClass().getDeclaredConstructors()));

        return configurator.constructors()
                .stream()
                .filter(it -> declared.contains(it.getAnnotated().getJavaMember()))
                .map(MethodConstructorConfigImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends MethodConfig<Object>> methods() {
        Set<java.lang.reflect.Method> declared = new HashSet<>(Arrays.asList(
                cdiDeclaration.getJavaClass().getDeclaredMethods()));

        return configurator.methods()
                .stream()
                .filter(it -> declared.contains(it.getAnnotated().getJavaMember()))
                .map(MethodConfigImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends FieldConfig<Object>> fields() {
        Set<java.lang.reflect.Field> declared = new HashSet<>(Arrays.asList(
                cdiDeclaration.getJavaClass().getDeclaredFields()));

        return configurator.fields()
                .stream()
                .filter(it -> declared.contains(it.getAnnotated().getJavaMember()))
                .map(FieldConfigImpl::new)
                .collect(Collectors.toList());
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
            AnnotationInfo info = new AnnotationInfoImpl(ClassConfigImpl.this.cdiDeclaration, null, annotation);
            return predicate.test(info);
        });
    }

    @Override
    public void removeAllAnnotations() {
        configurator.removeAll();
    }
}
