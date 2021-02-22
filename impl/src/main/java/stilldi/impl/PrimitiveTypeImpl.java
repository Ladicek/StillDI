package stilldi.impl;

import cdi.lite.extension.model.types.PrimitiveType;
import stilldi.impl.util.reflection.AnnotatedTypes;

class PrimitiveTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements PrimitiveType {
    private final Class<?> clazz;

    public PrimitiveTypeImpl(Class<?> primitiveType) {
        super(AnnotatedTypes.from(primitiveType));
        this.clazz = primitiveType;
    }

    @Override
    public String name() {
        return reflectionType.getType().getTypeName();
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
