package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.Annotations;
import jakarta.enterprise.lang.model.AnnotationAttribute;
import jakarta.enterprise.lang.model.AnnotationAttributeValue;
import jakarta.enterprise.lang.model.AnnotationInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

class AnnotationsImpl implements Annotations {
    @Override
    public AnnotationAttributeValue value(boolean value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(byte value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(short value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(int value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(long value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(float value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(double value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(char value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(String value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue value(Enum<?> enumValue) {
        return attribute(null, enumValue).value();
    }

    @Override
    public AnnotationAttributeValue value(Class<? extends Enum<?>> enumType, String enumValue) {
        return attribute(null, enumType, enumValue).value();
    }

    @Override
    public AnnotationAttributeValue value(ClassInfo<?> enumType, String enumValue) {
        return attribute(null, enumType, enumValue).value();
    }

    @Override
    public AnnotationAttributeValue value(Class<?> value) {
        return attribute(null, value).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(Class<? extends Annotation> annotationType,
            AnnotationAttribute... attributes) {
        return annotationAttribute(null, annotationType, attributes).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(ClassInfo<?> annotationType, AnnotationAttribute... attributes) {
        return annotationAttribute(null, annotationType, attributes).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(AnnotationInfo annotation) {
        return annotationAttribute(null, annotation).value();
    }

    @Override
    public AnnotationAttributeValue annotationValue(Annotation annotation) {
        return annotationAttribute(null, annotation).value();
    }

    @Override
    public AnnotationAttribute attribute(String name, boolean value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, byte value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, short value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, int value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, long value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, float value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, double value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, char value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, String value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, Enum<?> enumValue) {
        return new AnnotationAttributeImpl(null, null, name, enumValue);
    }

    @Override
    public AnnotationAttribute attribute(String name, Class<? extends Enum<?>> enumType, String enumValue) {
        Enum<?> value = Enum.valueOf((Class) enumType, enumValue);
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute attribute(String name, ClassInfo<?> enumType, String enumValue) {
        Class<?> clazz = ((ClassInfoImpl) enumType).cdiDeclaration.getJavaClass();
        return attribute(name, (Class<? extends Enum<?>>) clazz, enumValue);
    }

    @Override
    public AnnotationAttribute attribute(String name, Class<?> value) {
        return new AnnotationAttributeImpl(null, null, name, value);
    }

    @Override
    public AnnotationAttribute arrayAttribute(String name, AnnotationAttributeValue... values) {
        Object[] array = Arrays.stream(values)
                .map(it -> ((AnnotationAttributeValueImpl) it).value)
                .toArray(Object[]::new);
        return new AnnotationAttributeImpl(null, null, name, array);
    }

    @Override
    public AnnotationAttribute arrayAttribute(String name, List<AnnotationAttributeValue> values) {
        Object[] array = values.stream()
                .map(it -> ((AnnotationAttributeValueImpl) it).value)
                .toArray(Object[]::new);
        return new AnnotationAttributeImpl(null, null, name, array);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, Class<? extends Annotation> annotationType, AnnotationAttribute... attributes) {
        Annotation annotation = AnnotationProxy.create(annotationType, attributes);
        return new AnnotationAttributeImpl(null, null, name, annotation);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, ClassInfo<?> annotationType, AnnotationAttribute... attributes) {
        Class<?> clazz = ((ClassInfoImpl) annotationType).cdiDeclaration.getJavaClass();
        Annotation annotation = AnnotationProxy.create((Class<? extends Annotation>) clazz, attributes);
        return new AnnotationAttributeImpl(null, null, name, annotation);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, AnnotationInfo annotation) {
        return new AnnotationAttributeImpl(null, null, name, ((AnnotationInfoImpl) annotation).annotation);
    }

    @Override
    public AnnotationAttribute annotationAttribute(String name, Annotation annotation) {
        return new AnnotationAttributeImpl(null, null, name, annotation);
    }
}
