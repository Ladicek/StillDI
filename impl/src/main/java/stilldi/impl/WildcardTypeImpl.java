package stilldi.impl;

import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.WildcardType;

import java.util.Optional;

class WildcardTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedWildcardType> implements WildcardType {
    private final boolean hasUpperBound;

    // note that while java.lang.reflect.AnnotatedWildcardType API returns arrays,
    // the Java language only permits at most one upper or lower bound

    WildcardTypeImpl(java.lang.reflect.AnnotatedWildcardType reflectionType) {
        super(reflectionType);
        this.hasUpperBound = reflectionType.getAnnotatedLowerBounds().length == 0;
    }

    @Override
    public Optional<Type> upperBound() {
        return hasUpperBound
                ? Optional.of(TypeImpl.fromReflectionType(reflectionType.getAnnotatedUpperBounds()[0]))
                : Optional.empty();
    }

    @Override
    public Optional<Type> lowerBound() {
        return hasUpperBound
                ? Optional.empty()
                : Optional.of(TypeImpl.fromReflectionType(reflectionType.getAnnotatedLowerBounds()[0]));
    }
}
