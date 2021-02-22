package stilldi.impl;

import cdi.lite.extension.model.types.VoidType;
import stilldi.impl.util.reflection.AnnotatedTypes;

class VoidTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements VoidType {
    VoidTypeImpl() {
        super(AnnotatedTypes.from(void.class));
    }

    @Override
    public String name() {
        return reflectionType.getType().getTypeName();
    }
}
