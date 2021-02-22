package stilldi.impl;

import cdi.lite.extension.beans.BeanInfo;
import cdi.lite.extension.beans.DisposerInfo;
import cdi.lite.extension.beans.InjectionPointInfo;
import cdi.lite.extension.beans.ScopeInfo;
import cdi.lite.extension.beans.StereotypeInfo;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;
import stilldi.impl.util.reflection.AnnotatedTypes;

import javax.annotation.Priority;
import java.util.Collection;
import java.util.stream.Collectors;

class BeanInfoImpl implements BeanInfo<Object> {
    final javax.enterprise.inject.spi.Bean<?> cdiBean;
    final javax.enterprise.inject.spi.Annotated cdiDeclaration;
    final javax.enterprise.inject.spi.AnnotatedParameter<?> cdiDisposerDeclaration;

    BeanInfoImpl(javax.enterprise.inject.spi.Bean<?> cdiBean, javax.enterprise.inject.spi.Annotated cdiDeclaration,
            javax.enterprise.inject.spi.AnnotatedParameter<?> cdiDisposerDeclaration) {
        this.cdiBean = cdiBean;
        this.cdiDeclaration = cdiDeclaration;
        this.cdiDisposerDeclaration = cdiDisposerDeclaration;
    }

    @Override
    public ScopeInfo scope() {
        javax.enterprise.inject.spi.AnnotatedType<?> scopeType = BeanManagerAccess.createAnnotatedType(cdiBean.getScope());
        boolean isNormal = scopeType.isAnnotationPresent(javax.enterprise.context.NormalScope.class);
        return new ScopeInfoImpl(new ClassInfoImpl(scopeType), isNormal);
    }

    @Override
    public Collection<Type> types() {
        return cdiBean.getTypes()
                .stream()
                .map(it -> TypeImpl.fromReflectionType(AnnotatedTypes.from(it)))
                .collect(Collectors.toList());
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return cdiBean.getQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(cdiDeclaration, null, it))
                .collect(Collectors.toList());
    }

    @Override
    public ClassInfo<?> declaringClass() {
        javax.enterprise.inject.spi.AnnotatedType<?> beanClass = BeanManagerAccess.createAnnotatedType(cdiBean.getBeanClass());
        return new ClassInfoImpl(beanClass);
    }

    @Override
    public boolean isClassBean() {
        return cdiDeclaration instanceof javax.enterprise.inject.spi.AnnotatedType;
    }

    @Override
    public boolean isProducerMethod() {
        return cdiDeclaration instanceof javax.enterprise.inject.spi.AnnotatedMethod;
    }

    @Override
    public boolean isProducerField() {
        return cdiDeclaration instanceof javax.enterprise.inject.spi.AnnotatedField;
    }

    @Override
    public boolean isSynthetic() {
        return cdiDeclaration == null;
    }

    @Override
    public MethodInfo<?> producerMethod() {
        return null;
    }

    @Override
    public FieldInfo<?> producerField() {
        return null;
    }

    @Override
    public boolean isAlternative() {
        return cdiBean.isAlternative();
    }

    @Override
    public int priority() {
        // TODO not exactly sure what's the proper way
        //  see https://github.com/weld/core/blob/master/impl/src/main/java/org/jboss/weld/bean/builtin/PriorityComparator.java
        if (cdiDeclaration instanceof javax.enterprise.inject.spi.AnnotatedType
                && cdiDeclaration.isAnnotationPresent(Priority.class)) {
            return cdiDeclaration.getAnnotation(Priority.class).value();
        }
        if (cdiBean instanceof javax.enterprise.inject.spi.Prioritized) {
            return ((javax.enterprise.inject.spi.Prioritized) cdiBean).getPriority();
        }

        // TODO default value?
        return 0;
    }

    @Override
    public String getName() {
        return cdiBean.getName();
    }

    @Override
    public DisposerInfo disposer() {
        if (cdiDisposerDeclaration != null) {
            return new DisposerInfoImpl(cdiDisposerDeclaration);
        }
        return null;
    }

    @Override
    public Collection<StereotypeInfo> stereotypes() {
        return cdiBean.getStereotypes()
                .stream()
                .map(StereotypeInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<InjectionPointInfo> injectionPoints() {
        return cdiBean.getInjectionPoints()
                .stream()
                .map(InjectionPointInfoImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "@" + cdiBean.getScope().getSimpleName() + " bean [types=" + cdiBean.getTypes()
                + ", qualifiers=" + cdiBean.getQualifiers() + "]"
                + (cdiDeclaration != null ? " declared at " + cdiDeclaration : "");
    }
}
