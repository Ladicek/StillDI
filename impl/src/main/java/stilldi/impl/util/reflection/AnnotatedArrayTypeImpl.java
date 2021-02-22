package stilldi.impl.util.reflection;

final class AnnotatedArrayTypeImpl extends AbstractEmptyAnnotatedType implements java.lang.reflect.AnnotatedArrayType {
    private final java.lang.reflect.GenericArrayType arrayType;

    AnnotatedArrayTypeImpl(java.lang.reflect.GenericArrayType arrayType) {
        this.arrayType = arrayType;
    }

    @Override
    public java.lang.reflect.AnnotatedType getAnnotatedGenericComponentType() {
        return AnnotatedTypes.from(arrayType.getGenericComponentType());
    }

    @Override
    public java.lang.reflect.Type getType() {
        return arrayType;
    }

    @Override
    public String toString() {
        return arrayType.toString();
    }
}
