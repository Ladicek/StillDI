package stilldi.impl;

import cdi.lite.extension.model.declarations.PackageInfo;
import stilldi.impl.util.fake.AnnotatedPackage;

import java.util.Objects;

class PackageInfoImpl extends DeclarationInfoImpl<AnnotatedPackage> implements PackageInfo {
    // only for equals/hashCode
    private final String name;

    PackageInfoImpl(AnnotatedPackage cdiDeclaration) {
        super(cdiDeclaration);
        this.name = cdiDeclaration.getJavaPackage().getName();
    }

    @Override
    public String name() {
        return cdiDeclaration.getJavaPackage().getName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PackageInfoImpl that = (PackageInfoImpl) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
