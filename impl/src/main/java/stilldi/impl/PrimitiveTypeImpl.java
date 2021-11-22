package stilldi.impl;

import jakarta.enterprise.lang.model.types.PrimitiveType;
import stilldi.impl.util.AnnotationOverrides;
import stilldi.impl.util.reflection.AnnotatedTypes;

import java.lang.reflect.AnnotatedType;

class PrimitiveTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements PrimitiveType {
    final Class<?> clazz;

    PrimitiveTypeImpl(AnnotatedType primitiveType) {
        this(primitiveType, null);
    }

    PrimitiveTypeImpl(AnnotatedType primitiveType, AnnotationOverrides overrides) {
        super(primitiveType, overrides);
        this.clazz = (Class<?>) primitiveType.getType();
    }

    PrimitiveTypeImpl(Class<?> primitiveType) {
        this(primitiveType, null);
    }

    PrimitiveTypeImpl(Class<?> primitiveType, AnnotationOverrides overrides) {
        super(AnnotatedTypes.from(primitiveType), overrides);
        this.clazz = primitiveType;
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }

    @Override
    public PrimitiveKind primitiveKind() {
        if (clazz == boolean.class) {
            return PrimitiveKind.BOOLEAN;
        } else if (clazz == byte.class) {
            return PrimitiveKind.BYTE;
        } else if (clazz == short.class) {
            return PrimitiveKind.SHORT;
        } else if (clazz == int.class) {
            return PrimitiveKind.INT;
        } else if (clazz == long.class) {
            return PrimitiveKind.LONG;
        } else if (clazz == float.class) {
            return PrimitiveKind.FLOAT;
        } else if (clazz == double.class) {
            return PrimitiveKind.DOUBLE;
        } else if (clazz == char.class) {
            return PrimitiveKind.CHAR;
        } else {
            throw new IllegalStateException("Unknown primitive type " + clazz);
        }
    }
}
