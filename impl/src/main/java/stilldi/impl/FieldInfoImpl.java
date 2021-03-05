package stilldi.impl;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.types.Type;

import java.lang.reflect.Modifier;
import java.util.Objects;

class FieldInfoImpl extends DeclarationInfoImpl<javax.enterprise.inject.spi.AnnotatedField<?>> implements FieldInfo<Object> {
    // only for equals/hashCode
    private final String className;
    private final String name;

    FieldInfoImpl(javax.enterprise.inject.spi.AnnotatedField<?> cdiDeclaration) {
        super(cdiDeclaration);
        this.className = cdiDeclaration.getJavaMember().getDeclaringClass().getName();
        this.name = cdiDeclaration.getJavaMember().getName();
    }

    @Override
    public String name() {
        return cdiDeclaration.getJavaMember().getName();
    }

    @Override
    public Type type() {
        return TypeImpl.fromReflectionType(cdiDeclaration.getJavaMember().getAnnotatedType());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(cdiDeclaration.getJavaMember().getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(cdiDeclaration.getJavaMember().getModifiers());
    }

    @Override
    public int modifiers() {
        return cdiDeclaration.getJavaMember().getModifiers();
    }

    @Override
    public ClassInfo<Object> declaringClass() {
        return new ClassInfoImpl(cdiDeclaration.getDeclaringType());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FieldInfoImpl fieldInfo = (FieldInfoImpl) o;
        return Objects.equals(className, fieldInfo.className)
                && Objects.equals(name, fieldInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name);
    }
}
