package stilldi.impl;

import jakarta.enterprise.lang.model.types.ClassType;
import jakarta.enterprise.lang.model.types.ParameterizedType;
import jakarta.enterprise.lang.model.types.Type;
import stilldi.impl.util.AnnotationOverrides;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParameterizedTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedParameterizedType> implements ParameterizedType {
    ParameterizedTypeImpl(java.lang.reflect.AnnotatedParameterizedType reflectionType) {
        this(reflectionType, null);
    }

    ParameterizedTypeImpl(java.lang.reflect.AnnotatedParameterizedType reflectionType, AnnotationOverrides overrides) {
        super(reflectionType, overrides);
    }

    @Override
    public ClassType genericClass() {
        java.lang.reflect.ParameterizedType type = (java.lang.reflect.ParameterizedType) reflection.getType();
        return new ClassTypeImpl((Class<?>) type.getRawType(), null);
    }

    @Override
    public List<Type> typeArguments() {
        return Arrays.stream(reflection.getAnnotatedActualTypeArguments())
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }
}
