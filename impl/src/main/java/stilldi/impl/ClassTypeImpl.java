package stilldi.impl;

import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ClassType;
import stilldi.impl.util.reflection.AnnotatedTypes;

class ClassTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements ClassType {
    private final Class<?> clazz;

    ClassTypeImpl(Class<?> clazz) {
        super(AnnotatedTypes.from(clazz));
        this.clazz = clazz;
    }

    @Override
    public ClassInfo<?> declaration() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(clazz));
    }
}
