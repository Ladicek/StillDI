package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationAttributeValue;

import java.util.Objects;

class AnnotationAttributeImpl implements AnnotationAttribute {
    final String name;
    final AnnotationAttributeValueImpl value;

    AnnotationAttributeImpl(javax.enterprise.inject.spi.Annotated cdiDeclaration,
            java.lang.reflect.AnnotatedType reflectionType, String name, Object value) {
        this.name = name;
        this.value = new AnnotationAttributeValueImpl(cdiDeclaration, reflectionType, value);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public AnnotationAttributeValue value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationAttributeImpl that = (AnnotationAttributeImpl) o;
        return Objects.equals(name, that.name)
                && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}
