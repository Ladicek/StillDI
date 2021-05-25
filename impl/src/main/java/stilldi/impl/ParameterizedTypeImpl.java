package stilldi.impl;

import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ParameterizedType;
import jakarta.enterprise.lang.model.types.Type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ParameterizedTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedParameterizedType> implements ParameterizedType {
    ParameterizedTypeImpl(java.lang.reflect.AnnotatedParameterizedType reflectionType) {
        super(reflectionType);
    }

    @Override
    public ClassInfo<?> declaration() {
        java.lang.reflect.ParameterizedType type = (java.lang.reflect.ParameterizedType) reflectionType.getType();
        jakarta.enterprise.inject.spi.AnnotatedType<?> declaration = BeanManagerAccess.createAnnotatedType((Class<?>) type.getRawType());
        return new ClassInfoImpl(declaration);
    }

    @Override
    public List<Type> typeArguments() {
        return Arrays.stream(reflectionType.getAnnotatedActualTypeArguments())
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }
}
