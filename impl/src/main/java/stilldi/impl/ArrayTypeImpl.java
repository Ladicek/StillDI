package stilldi.impl;

import jakarta.enterprise.lang.model.types.ArrayType;
import jakarta.enterprise.lang.model.types.Type;

class ArrayTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedArrayType> implements ArrayType {
    private final int dimensions;
    private final java.lang.reflect.AnnotatedType componentType;

    // TODO what about multi-dimensional arrays where each dimension has its own annotations?
    //  e.g. `@Foo int @Bar [] @Baz [] @Quux [] myArray`

    ArrayTypeImpl(java.lang.reflect.AnnotatedArrayType reflectionType) {
        super(reflectionType);

        int dimensions = 1;
        java.lang.reflect.AnnotatedType componentType = reflectionType.getAnnotatedGenericComponentType();
        while (componentType instanceof java.lang.reflect.AnnotatedArrayType) {
            dimensions++;
            componentType = ((java.lang.reflect.AnnotatedArrayType) componentType).getAnnotatedGenericComponentType();
        }
        this.dimensions = dimensions;
        this.componentType = componentType;
    }

    @Override
    public int dimensions() {
        return dimensions;
    }

    @Override
    public Type componentType() {
        return TypeImpl.fromReflectionType(componentType);
    }
}
