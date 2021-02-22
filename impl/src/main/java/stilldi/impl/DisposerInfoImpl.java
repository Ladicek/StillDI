package stilldi.impl;

import cdi.lite.extension.beans.DisposerInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.declarations.ParameterInfo;

class DisposerInfoImpl implements DisposerInfo {
    private final javax.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration;

    DisposerInfoImpl(javax.enterprise.inject.spi.AnnotatedParameter<?> cdiDeclaration) {
        this.cdiDeclaration = cdiDeclaration;
    }

    @Override
    public MethodInfo<?> disposerMethod() {
        return new MethodInfoImpl(cdiDeclaration.getDeclaringCallable());
    }

    @Override
    public ParameterInfo disposedParameter() {
        return new ParameterInfoImpl(cdiDeclaration);
    }
}
