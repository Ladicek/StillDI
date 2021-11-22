package stilldi.impl;

import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.ClassType;
import stilldi.impl.util.AnnotationOverrides;
import stilldi.impl.util.reflection.AnnotatedTypes;

class ClassTypeImpl extends TypeImpl<java.lang.reflect.AnnotatedType> implements ClassType {
    final Class<?> clazz;

    ClassTypeImpl(java.lang.reflect.AnnotatedType clazz) {
        this(clazz, null);
    }

    ClassTypeImpl(java.lang.reflect.AnnotatedType clazz, AnnotationOverrides overrides) {
        super(clazz, overrides);
        this.clazz = (Class<?>) clazz.getType();
    }

    ClassTypeImpl(Class<?> clazz) {
        this(clazz, null);
    }

    ClassTypeImpl(Class<?> clazz, AnnotationOverrides overrides) {
        super(AnnotatedTypes.from(clazz), overrides);
        this.clazz = clazz;
    }

    @Override
    public ClassInfo declaration() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(clazz));
    }
}
