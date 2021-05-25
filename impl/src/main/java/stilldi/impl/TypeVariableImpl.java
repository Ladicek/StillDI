package stilldi.impl;

import jakarta.enterprise.lang.model.types.Type;
import jakarta.enterprise.lang.model.types.TypeVariable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class TypeVariableImpl extends TypeImpl<java.lang.reflect.AnnotatedTypeVariable> implements TypeVariable {
    TypeVariableImpl(java.lang.reflect.AnnotatedTypeVariable reflectionType) {
        super(reflectionType);
    }

    @Override
    public String name() {
        return reflectionType.getType().getTypeName();
    }

    @Override
    public List<Type> bounds() {
        return Arrays.stream(reflectionType.getAnnotatedBounds())
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }
}
