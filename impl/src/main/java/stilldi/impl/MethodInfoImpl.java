package stilldi.impl;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import stilldi.impl.util.reflection.AnnotatedTypes;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

class MethodInfoImpl extends DeclarationInfoImpl<javax.enterprise.inject.spi.AnnotatedCallable<?>> implements MethodInfo<Object> {
    // only for equals/hashCode
    private final String className;
    private final String name;
    private final java.lang.reflect.Type[] parameterTypes;

    MethodInfoImpl(javax.enterprise.inject.spi.AnnotatedCallable<?> cdiDeclaration) {
        super(cdiDeclaration);
        this.className = cdiDeclaration.getJavaMember().getDeclaringClass().getName();
        this.name = cdiDeclaration.getJavaMember().getName();
        this.parameterTypes = getJavaExecutable().getGenericParameterTypes();
    }

    private java.lang.reflect.Executable getJavaExecutable() {
        return (java.lang.reflect.Executable) cdiDeclaration.getJavaMember();
    }

    @Override
    public String name() {
        return cdiDeclaration.getJavaMember().getName();
    }

    @Override
    public List<ParameterInfo> parameters() {
        return cdiDeclaration.getParameters()
                .stream()
                .map(ParameterInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Type returnType() {
        return TypeImpl.fromReflectionType(getJavaExecutable().getAnnotatedReturnType());
    }

    @Override
    public Optional<Type> receiverType() {
        return Optional.of(TypeImpl.fromReflectionType(getJavaExecutable().getAnnotatedReceiverType()));
    }

    @Override
    public List<Type> throwsTypes() {
        return Arrays.stream(getJavaExecutable().getAnnotatedExceptionTypes())
                .map(TypeImpl::fromReflectionType)
                .collect(Collectors.toList());
    }

    @Override
    public List<TypeVariable> typeParameters() {
        return Arrays.stream(getJavaExecutable().getTypeParameters())
                .map(AnnotatedTypes::from)
                .map(TypeImpl::fromReflectionType)
                .filter(Type::isTypeVariable) // not necessary, just as a precaution
                .map(Type::asTypeVariable) // not necessary, just as a precaution
                .collect(Collectors.toList());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(cdiDeclaration.getJavaMember().getModifiers());
    }

    @Override
    public boolean isAbstract() {
        return Modifier.isAbstract(cdiDeclaration.getJavaMember().getModifiers());
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
        MethodInfoImpl that = (MethodInfoImpl) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(name, that.name) &&
                Arrays.equals(parameterTypes, that.parameterTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, name, parameterTypes);
    }

}
