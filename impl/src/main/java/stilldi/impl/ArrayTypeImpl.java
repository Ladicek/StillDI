package stilldi.impl;

import jakarta.enterprise.lang.model.types.ArrayType;
import jakarta.enterprise.lang.model.types.Type;
import stilldi.impl.util.AnnotationOverrides;

import java.lang.annotation.Annotation;

class ArrayTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedArrayType> implements ArrayType {
    private final java.lang.reflect.AnnotatedType componentType;

    ArrayTypeImpl(java.lang.reflect.AnnotatedArrayType reflectionType) {
        this(reflectionType, null);
    }

    ArrayTypeImpl(java.lang.reflect.AnnotatedArrayType reflectionType, AnnotationOverrides overrides) {
        super(reflectionType, overrides);

        this.componentType = reflectionType.getAnnotatedGenericComponentType();
    }

    @Override
    public Type componentType() {
        return TypeImpl.fromReflectionType(componentType);
    }
}
