package stilldi.impl;

import jakarta.enterprise.lang.model.types.VoidType;
import stilldi.impl.util.reflection.AnnotatedTypes;

class VoidTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements VoidType {
    final Class<?> clazz;

    VoidTypeImpl() {
        super(AnnotatedTypes.from(void.class), null);
        this.clazz = void.class;
    }

    @Override
    public String name() {
        return reflection.getType().getTypeName();
    }
}
