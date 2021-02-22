package stilldi.impl;

import cdi.lite.extension.beans.InjectionPointInfo;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.DeclarationInfo;
import cdi.lite.extension.model.types.Type;
import stilldi.impl.util.reflection.AnnotatedTypes;

import java.util.Collection;
import java.util.stream.Collectors;

class InjectionPointInfoImpl implements InjectionPointInfo {
    private final javax.enterprise.inject.spi.InjectionPoint cdiInjectionPoint;

    InjectionPointInfoImpl(javax.enterprise.inject.spi.InjectionPoint cdiInjectionPoint) {
        this.cdiInjectionPoint = cdiInjectionPoint;
    }

    @Override
    public Type type() {
        return TypeImpl.fromReflectionType(AnnotatedTypes.from(cdiInjectionPoint.getType()));
    }

    @Override
    public Collection<AnnotationInfo> qualifiers() {
        return cdiInjectionPoint.getQualifiers()
                .stream()
                .map(it -> new AnnotationInfoImpl(cdiInjectionPoint.getAnnotated(), null, it))
                .collect(Collectors.toList());
    }

    @Override
    public DeclarationInfo declaration() {
        return DeclarationInfoImpl.fromCdiDeclaration(cdiInjectionPoint.getAnnotated());
    }
}
