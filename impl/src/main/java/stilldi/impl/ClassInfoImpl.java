package stilldi.impl;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.PackageInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import stilldi.impl.util.fake.AnnotatedPackage;
import stilldi.impl.util.reflection.AnnotatedTypes;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class ClassInfoImpl extends DeclarationInfoImpl<javax.enterprise.inject.spi.AnnotatedType<?>> implements ClassInfo<Object> {
    // only for equals/hashCode
    private final String name;

    ClassInfoImpl(javax.enterprise.inject.spi.AnnotatedType<?> cdiDeclaration) {
        super(cdiDeclaration);
        this.name = cdiDeclaration.getJavaClass().getName();
    }

    @Override
    public String name() {
        return cdiDeclaration.getJavaClass().getName();
    }

    @Override
    public String simpleName() {
        return cdiDeclaration.getJavaClass().getSimpleName();
    }

    @Override
    public PackageInfo packageInfo() {
        return new PackageInfoImpl(new AnnotatedPackage(cdiDeclaration.getJavaClass().getPackage()));
    }

    @Override
    public List<TypeVariable> typeParameters() {
        return Arrays.stream(cdiDeclaration.getJavaClass().getTypeParameters())
                .map(AnnotatedTypes::from)
                .map(TypeImpl::fromReflectionType)
                .filter(Type::isTypeVariable) // not necessary, just as a precaution
                .map(Type::asTypeVariable) // not necessary, just as a precaution
                .collect(Collectors.toList());
    }

    @Override
    public Type superClass() {
        return TypeImpl.fromReflectionType(cdiDeclaration.getJavaClass().getAnnotatedSuperclass());
    }

    @Override
    public ClassInfo<?> superClassDeclaration() {
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(cdiDeclaration.getJavaClass().getSuperclass()));
    }

    @Override
    public List<Type> superInterfaces() {
        java.lang.reflect.AnnotatedType[] interfaces = cdiDeclaration.getJavaClass().getAnnotatedInterfaces();
        return Arrays.stream(interfaces)
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }

    @Override
    public List<ClassInfo<?>> superInterfacesDeclarations() {
        return Arrays.stream(cdiDeclaration.getJavaClass().getInterfaces())
                .map(it -> new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(it)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPlainClass() {
        // TODO there must be a better way
        return !isInterface() && !isEnum() && !isAnnotation();
    }

    @Override
    public boolean isInterface() {
        return cdiDeclaration.getJavaClass().isInterface();
    }

    @Override
    public boolean isEnum() {
        return cdiDeclaration.getJavaClass().isEnum();
    }

    @Override
    public boolean isAnnotation() {
        return cdiDeclaration.getJavaClass().isAnnotation();
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(cdiDeclaration.getJavaClass().getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(cdiDeclaration.getJavaClass().getModifiers());
    }

    @Override
    public int modifiers() {
        return cdiDeclaration.getJavaClass().getModifiers();
    }

    @Override
    public Collection<MethodInfo<Object>> constructors() {
        return cdiDeclaration.getConstructors()
                .stream()
                .map(MethodInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<MethodInfo<Object>> methods() {
        return cdiDeclaration.getMethods()
                .stream()
                .map(MethodInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<FieldInfo<Object>> fields() {
        return cdiDeclaration.getFields()
                .stream()
                .map(FieldInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassInfoImpl classInfo = (ClassInfoImpl) o;
        return Objects.equals(name, classInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
