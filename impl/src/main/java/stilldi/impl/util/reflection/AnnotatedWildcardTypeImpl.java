package stilldi.impl.util.reflection;

import java.util.Arrays;

final class AnnotatedWildcardTypeImpl extends AbstractEmptyAnnotatedType implements java.lang.reflect.AnnotatedWildcardType {
    private final java.lang.reflect.WildcardType wildcardType;

    AnnotatedWildcardTypeImpl(java.lang.reflect.WildcardType wildcardType) {
        this.wildcardType = wildcardType;
    }

    @Override
    public java.lang.reflect.AnnotatedType[] getAnnotatedLowerBounds() {
        return Arrays.stream(wildcardType.getLowerBounds())
                .map(AnnotatedTypes::from)
                .toArray(java.lang.reflect.AnnotatedType[]::new);
    }

    @Override
    public java.lang.reflect.AnnotatedType[] getAnnotatedUpperBounds() {
        return Arrays.stream(wildcardType.getUpperBounds())
                .map(AnnotatedTypes::from)
                .toArray(java.lang.reflect.AnnotatedType[]::new);
    }

    @Override
    public java.lang.reflect.Type getType() {
        return wildcardType;
    }

    @Override
    public String toString() {
        return wildcardType.toString();
    }
}
