package stilldi.impl;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.ObserverInfo;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;
import cdi.lite.extension.model.types.Type;
import stilldi.impl.util.reflection.AnnotatedTypes;

import javax.enterprise.event.Observes;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import java.util.Collection;
import java.util.stream.Collectors;

class ObserverInfoImpl implements ObserverInfo<Object> {
    final javax.enterprise.inject.spi.ObserverMethod<?> cdiObserver;
    final javax.enterprise.inject.spi.AnnotatedMethod<?> cdiDeclaration;

    ObserverInfoImpl(javax.enterprise.inject.spi.ObserverMethod<?> cdiObserver,
            javax.enterprise.inject.spi.AnnotatedMethod<?> cdiDeclaration) {
        this.cdiObserver = cdiObserver;
        this.cdiDeclaration = cdiDeclaration;
    }

    @Override
    public String id() {
        throw new UnsupportedOperationException("We really should get rid of ObserverInfo.id()");
    }

    @Override
    public Type observedType() {
        java.lang.reflect.Type observedType = cdiObserver.getObservedType();
        return TypeImpl.fromReflectionType(AnnotatedTypes.from(observedType));
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return cdiObserver.getObservedQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(cdiDeclaration, null, it))
                .collect(Collectors.toList());
    }

    @Override
    public ClassInfo<?> declaringClass() {
        javax.enterprise.inject.spi.AnnotatedType<?> beanClass = BeanManagerAccess.createAnnotatedType(cdiObserver.getBeanClass());
        return new ClassInfoImpl(beanClass);
    }

    @Override
    public MethodInfo<?> observerMethod() {
        if (cdiDeclaration == null) {
            return null;
        }

        return new MethodInfoImpl(cdiDeclaration);
    }

    @Override
    public ParameterInfo eventParameter() {
        if (cdiDeclaration == null) {
            return null;
        }

        for (javax.enterprise.inject.spi.AnnotatedParameter<?> parameter : cdiDeclaration.getParameters()) {
            if (parameter.isAnnotationPresent(Observes.class)) {
                return new ParameterInfoImpl(parameter);
            }
        }
        throw new IllegalStateException("Observer method without an @Observes parameter: " + cdiDeclaration);
    }

    @Override
    public BeanInfo<?> bean() {
        throw new UnsupportedOperationException("Probably get rid of ObserverInfo.bean()");
    }

    @Override
    public boolean isSynthetic() {
        return cdiDeclaration == null;
    }

    @Override
    public int priority() {
        return cdiObserver.getPriority();
    }

    @Override
    public boolean isAsync() {
        return cdiObserver.isAsync();
    }

    @Override
    public Reception reception() {
        return cdiObserver.getReception();
    }

    @Override
    public TransactionPhase transactionPhase() {
        return cdiObserver.getTransactionPhase();
    }

    @Override
    public String toString() {
        return "observer [type=" + cdiObserver.getObservedType()
                + ", qualifiers=" + cdiObserver.getObservedQualifiers() + "]"
                + (cdiDeclaration != null ? " declared at " + cdiDeclaration : "");
    }
}
