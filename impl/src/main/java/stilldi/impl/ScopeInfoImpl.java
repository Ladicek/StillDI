package stilldi.impl;

import cdi.lite.extension.beans.ScopeInfo;
import cdi.lite.extension.model.declarations.ClassInfo;

class ScopeInfoImpl implements ScopeInfo {
    private final ClassInfo<?> annotation;
    private final boolean isNormal;

    ScopeInfoImpl(ClassInfo<?> annotation, boolean isNormal) {
        this.annotation = annotation;
        this.isNormal = isNormal;
    }

    @Override
    public ClassInfo<?> annotation() {
        return annotation;
    }

    @Override
    public boolean isNormal() {
        return isNormal;
    }
}
