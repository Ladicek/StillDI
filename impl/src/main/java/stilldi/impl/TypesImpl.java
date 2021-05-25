package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.PrimitiveType;
import jakarta.enterprise.lang.model.types.Type;
import stilldi.impl.util.reflection.AnnotatedTypes;

class TypesImpl implements Types {
    @Override
    public Type of(Class<?> clazz) {
        if (clazz.isArray()) {
            int dimensions = 1;
            Class<?> componentType = clazz.getComponentType();
            while (componentType.isArray()) {
                dimensions++;
                componentType = componentType.getComponentType();
            }
            return ofArray(of(componentType), dimensions);
        }

        if (clazz.isPrimitive()) {
            if (clazz == void.class) {
                return ofVoid();
            } else if (clazz == boolean.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.BOOLEAN);
            } else if (clazz == byte.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.BYTE);
            } else if (clazz == short.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.SHORT);
            } else if (clazz == int.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.INT);
            } else if (clazz == long.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.LONG);
            } else if (clazz == float.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.FLOAT);
            } else if (clazz == double.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.DOUBLE);
            } else if (clazz == char.class) {
                return ofPrimitive(PrimitiveType.PrimitiveKind.CHAR);
            } else {
                throw new IllegalArgumentException("Unknown primitive type " + clazz);
            }
        }

        return new ClassTypeImpl(clazz);
    }

    @Override
    public Type ofVoid() {
        return new VoidTypeImpl();
    }

    @Override
    public Type ofPrimitive(PrimitiveType.PrimitiveKind kind) {
        switch (kind) {
            case BOOLEAN:
                return new PrimitiveTypeImpl(Boolean.TYPE);
            case BYTE:
                return new PrimitiveTypeImpl(Byte.TYPE);
            case SHORT:
                return new PrimitiveTypeImpl(Short.TYPE);
            case INT:
                return new PrimitiveTypeImpl(Integer.TYPE);
            case LONG:
                return new PrimitiveTypeImpl(Long.TYPE);
            case FLOAT:
                return new PrimitiveTypeImpl(Float.TYPE);
            case DOUBLE:
                return new PrimitiveTypeImpl(Double.TYPE);
            case CHAR:
                return new PrimitiveTypeImpl(Character.TYPE);
            default:
                throw new IllegalArgumentException("Unknown primitive type " + kind);
        }
    }

    @Override
    public Type ofClass(ClassInfo<?> clazz) {
        return of(((ClassInfoImpl) clazz).cdiDeclaration.getJavaClass());
    }

    @Override
    public Type ofArray(Type componentType, int dimensions) {
        return new ArrayTypeImpl(AnnotatedTypes.array(((TypeImpl<?>) componentType).reflectionType.getType(), dimensions));
    }

    @Override
    public Type parameterized(Class<?> parameterizedType, Class<?>... typeArguments) {
        return new ParameterizedTypeImpl(AnnotatedTypes.parameterized(parameterizedType, typeArguments));
    }

    @Override
    public Type parameterized(Class<?> parameterizedType, Type... typeArguments) {
        java.lang.reflect.Type[] underlyingTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {
            underlyingTypeArguments[i] = ((TypeImpl<?>) typeArguments[i]).reflectionType.getType();
        }
        return new ParameterizedTypeImpl(AnnotatedTypes.parameterized(parameterizedType, underlyingTypeArguments));
    }

    @Override
    public Type parameterized(Type parameterizedType, Type... typeArguments) {
        Class<?> clazz = (Class<?>) ((TypeImpl<?>) parameterizedType).reflectionType.getType();
        java.lang.reflect.Type[] underlyingTypeArguments = new java.lang.reflect.Type[typeArguments.length];
        for (int i = 0; i < typeArguments.length; i++) {
            underlyingTypeArguments[i] = ((TypeImpl<?>) typeArguments[i]).reflectionType.getType();
        }
        return new ParameterizedTypeImpl(AnnotatedTypes.parameterized(clazz, underlyingTypeArguments));
    }

    @Override
    public Type wildcardWithUpperBound(Type upperBound) {
        return new WildcardTypeImpl(AnnotatedTypes.wildcardWithUpperBound(((TypeImpl<?>) upperBound).reflectionType.getType()));
    }

    @Override
    public Type wildcardWithLowerBound(Type lowerBound) {
        return new WildcardTypeImpl(AnnotatedTypes.wildcardWithLowerBound(((TypeImpl<?>) lowerBound).reflectionType.getType()));
    }

    @Override
    public Type wildcardUnbounded() {
        return new WildcardTypeImpl(AnnotatedTypes.unboundedWildcardType());
    }
}
