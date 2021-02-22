package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.AnnotationTarget;
import cdi.lite.extension.model.declarations.ClassInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class AnnotationInfoImpl implements AnnotationInfo {
    // null if the annotation doesn't target a declaration
    final javax.enterprise.inject.spi.Annotated cdiDeclaration;
    // null if the annotation doesn't target a type
    final java.lang.reflect.AnnotatedType reflectionType;

    final Annotation annotation;

    AnnotationInfoImpl(javax.enterprise.inject.spi.Annotated cdiDeclaration,
            java.lang.reflect.AnnotatedType reflectionType, Annotation annotation) {
        this.cdiDeclaration = cdiDeclaration;
        this.reflectionType = reflectionType;
        this.annotation = annotation;
    }

    @Override
    public AnnotationTarget target() {
        if (cdiDeclaration != null) {
            return DeclarationInfoImpl.fromCdiDeclaration(cdiDeclaration);
        } else if (reflectionType != null) {
            return TypeImpl.fromReflectionType(reflectionType);
        } else {
            throw new IllegalStateException("Unknown annotation target");
        }
    }

    @Override
    public ClassInfo<?> declaration() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(annotation.annotationType()));
    }

    @Override
    public boolean hasAttribute(String name) {
        try {
            annotation.annotationType().getMethod(name);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @Override
    public AnnotationAttributeValue attribute(String name) {
        try {
            Method attribute = annotation.annotationType().getMethod(name);
            Object value = attribute.invoke(annotation);
            return new AnnotationAttributeValueImpl(cdiDeclaration, reflectionType, value);
        } catch (NoSuchMethodException e) {
            return null;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<AnnotationAttribute> attributes() {
        try {
            Method[] attributes = annotation.annotationType().getDeclaredMethods();
            List<AnnotationAttribute> result = new ArrayList<>();
            for (Method attribute : attributes) {
                String name = attribute.getName();
                Object value = attribute.invoke(annotation);
                result.add(new AnnotationAttributeImpl(cdiDeclaration, reflectionType, name, value));
            }
            return result;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationInfoImpl that = (AnnotationInfoImpl) o;
        return Objects.equals(cdiDeclaration, that.cdiDeclaration)
                && Objects.equals(reflectionType, that.reflectionType)
                && Objects.equals(annotation, that.annotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cdiDeclaration, reflectionType, annotation);
    }

    @Override
    public String toString() {
        return annotation.toString();
    }
}
