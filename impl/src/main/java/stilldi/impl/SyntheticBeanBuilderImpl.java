package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.phases.synthesis.SyntheticBeanBuilder;
import cdi.lite.extension.phases.synthesis.SyntheticBeanCreator;
import cdi.lite.extension.phases.synthesis.SyntheticBeanDisposer;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class SyntheticBeanBuilderImpl<T> implements SyntheticBeanBuilder<T> {
    Class<?> implementationClass;
    Set<java.lang.reflect.Type> types = new HashSet<>();
    Set<Annotation> qualifiers = new HashSet<>();
    Class<? extends Annotation> scope;
    boolean isAlternative;
    int priority;
    String name;
    Set<Class<? extends Annotation>> stereotypes = new HashSet<>();
    Map<String, Object> params = new HashMap<>();
    Class<? extends SyntheticBeanCreator<T>> creatorClass;
    Class<? extends SyntheticBeanDisposer<T>> disposerClass;

    SyntheticBeanBuilderImpl(Class<?> implementationClass) {
        this.implementationClass = implementationClass;
    }

    @Override
    public SyntheticBeanBuilder<T> type(Class<?> type) {
        this.types.add(type);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> type(ClassInfo<?> type) {
        this.types.add(((ClassInfoImpl) type).cdiDeclaration.getJavaClass());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> type(Type type) {
        this.types.add(((TypeImpl<?>) type).reflectionType.getType());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(Class<? extends Annotation> qualifierAnnotation,
            AnnotationAttribute... attributes) {
        this.qualifiers.add(AnnotationProxy.create(qualifierAnnotation, attributes));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes) {
        Class<? extends Annotation> clazz = (Class<? extends Annotation>) ((ClassInfoImpl) qualifierAnnotation).cdiDeclaration.getJavaClass();
        this.qualifiers.add(AnnotationProxy.create(clazz, attributes));
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(AnnotationInfo qualifierAnnotation) {
        this.qualifiers.add(((AnnotationInfoImpl) qualifierAnnotation).annotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> qualifier(Annotation qualifierAnnotation) {
        this.qualifiers.add(qualifierAnnotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> scope(Class<? extends Annotation> scopeAnnotation) {
        this.scope = scopeAnnotation;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> alternative(boolean isAlternative) {
        this.isAlternative = isAlternative;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> stereotype(Class<? extends Annotation> stereotypeAnnotation) {
        this.stereotypes.add(stereotypeAnnotation);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> stereotype(ClassInfo<?> stereotypeAnnotation) {
        this.stereotypes.add((Class<? extends Annotation>) ((ClassInfoImpl) stereotypeAnnotation).cdiDeclaration.getJavaClass());
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, boolean value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, int value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, long value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, double value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, String value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> withParam(String key, Class<?> value) {
        this.params.put(key, value);
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> createWith(Class<? extends SyntheticBeanCreator<T>> creatorClass) {
        this.creatorClass = creatorClass;
        return this;
    }

    @Override
    public SyntheticBeanBuilder<T> disposeWith(Class<? extends SyntheticBeanDisposer<T>> disposerClass) {
        this.disposerClass = disposerClass;
        return this;
    }
}
