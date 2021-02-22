package stilldi.impl.util.reflection;

import java.lang.annotation.Annotation;

final class AnnotatedTypeImpl implements java.lang.reflect.AnnotatedType {
    private final Class<?> clazz;

    AnnotatedTypeImpl(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public java.lang.reflect.Type getType() {
        return clazz;
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return clazz.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return clazz.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return clazz.getDeclaredAnnotations();
    }

    @Override
    public String toString() {
        return clazz.getName();
    }
}
