package stilldi.impl;

import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.types.Type;
import stilldi.impl.util.reflection.AnnotatedTypes;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

abstract class TypeImpl<ReflectionType extends java.lang.reflect.AnnotatedType> implements Type {
    final ReflectionType reflectionType;

    TypeImpl(ReflectionType reflectionType) {
        this.reflectionType = reflectionType;
    }

    static Type fromReflectionType(java.lang.reflect.AnnotatedType reflectionType) {
        if (reflectionType instanceof java.lang.reflect.AnnotatedParameterizedType) {
            return new ParameterizedTypeImpl((java.lang.reflect.AnnotatedParameterizedType) reflectionType);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedTypeVariable) {
            return new TypeVariableImpl((java.lang.reflect.AnnotatedTypeVariable) reflectionType);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedArrayType) {
            return new ArrayTypeImpl((java.lang.reflect.AnnotatedArrayType) reflectionType);
        } else if (reflectionType instanceof java.lang.reflect.AnnotatedWildcardType) {
            return new WildcardTypeImpl((java.lang.reflect.AnnotatedWildcardType) reflectionType);
        } else {
            // plain java.lang.reflect.AnnotatedType
            if (reflectionType.getType() instanceof Class) {
                Class<?> clazz = (Class<?>) reflectionType.getType();
                if (clazz.isArray()) {
                    return new ArrayTypeImpl((java.lang.reflect.AnnotatedArrayType) AnnotatedTypes.from(clazz));
                }
                return new ClassTypeImpl((Class<?>) reflectionType.getType());
            } else {
                throw new IllegalArgumentException("Unknown type " + reflectionType);
            }
        }
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> annotationType) {
        return reflectionType.isAnnotationPresent(annotationType);
    }

    @Override
    public boolean hasAnnotation(Predicate<AnnotationInfo> predicate) {
        return Arrays.stream(reflectionType.getAnnotations())
                .anyMatch(it -> predicate.test(new AnnotationInfoImpl(null, reflectionType, it)));
    }

    @Override
    public AnnotationInfo annotation(Class<? extends Annotation> annotationType) {
        return new AnnotationInfoImpl(null, reflectionType,
                reflectionType.getAnnotation(annotationType));
    }

    @Override
    public Collection<AnnotationInfo> repeatableAnnotation(Class<? extends Annotation> annotationType) {
        return Arrays.stream(reflectionType.getAnnotationsByType(annotationType))
                .map(it -> new AnnotationInfoImpl(null, reflectionType, it))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations(Predicate<AnnotationInfo> predicate) {
        return Arrays.stream(reflectionType.getAnnotations())
                .map(it -> new AnnotationInfoImpl(null, reflectionType, it))
                .filter(predicate)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> annotations() {
        return annotations(it -> true);
    }

    @Override
    public String toString() {
        return reflectionType.getType().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TypeImpl)) return false;
        TypeImpl<?> type = (TypeImpl<?>) o;
        return Objects.equals(reflectionType.getType(), type.reflectionType.getType())
                && Objects.deepEquals(reflectionType.getAnnotations(), type.reflectionType.getAnnotations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(reflectionType.getType(), reflectionType.getAnnotations());
    }
}
