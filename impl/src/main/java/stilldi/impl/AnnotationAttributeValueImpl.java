package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttributeValue;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import stilldi.impl.util.reflection.AnnotatedTypes;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class AnnotationAttributeValueImpl implements AnnotationAttributeValue {
    // null if the annotation doesn't target a declaration
    final javax.enterprise.inject.spi.Annotated cdiDeclaration;
    // null if the annotation doesn't target a type
    final java.lang.reflect.AnnotatedType reflectionType;

    final Kind kind;
    final Object value;

    AnnotationAttributeValueImpl(javax.enterprise.inject.spi.Annotated cdiDeclaration,
            java.lang.reflect.AnnotatedType reflectionType, Object value) {
        this.cdiDeclaration = cdiDeclaration;
        this.reflectionType = reflectionType;
        this.kind = determineKind(value);
        this.value = value;
    }

    private static Kind determineKind(Object value) {
        if (value instanceof Boolean) {
            return Kind.BOOLEAN;
        } else if (value instanceof Byte) {
            return Kind.BYTE;
        } else if (value instanceof Short) {
            return Kind.SHORT;
        } else if (value instanceof Integer) {
            return Kind.INT;
        } else if (value instanceof Long) {
            return Kind.LONG;
        } else if (value instanceof Float) {
            return Kind.FLOAT;
        } else if (value instanceof Double) {
            return Kind.DOUBLE;
        } else if (value instanceof Character) {
            return Kind.CHAR;
        } else if (value instanceof String) {
            return Kind.STRING;
        } else if (value instanceof Enum) {
            return Kind.ENUM;
        } else if (value instanceof Class) {
            return Kind.CLASS;
        } else if (value instanceof Object[]) {
            return Kind.ARRAY;
        } else if (value instanceof Annotation) {
            return Kind.NESTED_ANNOTATION;
        } else {
            throw new IllegalArgumentException("Unknown annotation attribute value " + value);
        }
    }

    @Override
    public Kind kind() {
        return kind;
    }

    @Override
    public boolean asBoolean() {
        return (Boolean) value;
    }

    @Override
    public byte asByte() {
        return (Byte) value;
    }

    @Override
    public short asShort() {
        return (Short) value;
    }

    @Override
    public int asInt() {
        return (Integer) value;
    }

    @Override
    public long asLong() {
        return (Long) value;
    }

    @Override
    public float asFloat() {
        return (Float) value;
    }

    @Override
    public double asDouble() {
        return (Double) value;
    }

    @Override
    public char asChar() {
        return (Character) value;
    }

    @Override
    public String asString() {
        return (String) value;
    }

    @Override
    public <T extends Enum<T>> T asEnum() {
        return (T) value;
    }

    @Override
    public String asEnumValue() {
        return ((Enum<?>) value).name();
    }

    @Override
    public ClassInfo<?> asEnumClass() {
        Class<?> enumType = ((Enum<?>) value).getDeclaringClass();
        return new ClassInfoImpl(BeanManagerAccess.createAnnotatedType(enumType));
    }

    @Override
    public Type asClass() {
        Class<?> clazz = (Class<?>) value;
        return TypeImpl.fromReflectionType(AnnotatedTypes.from(clazz));
    }

    @Override
    public List<AnnotationAttributeValue> asArray() {
        Object[] array = (Object[]) value;
        return Arrays.stream(array)
                .map(it -> new AnnotationAttributeValueImpl(cdiDeclaration, reflectionType, it))
                .collect(Collectors.toList());
    }

    @Override
    public AnnotationInfo asNestedAnnotation() {
        return new AnnotationInfoImpl(cdiDeclaration, reflectionType, (Annotation) value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnnotationAttributeValueImpl that = (AnnotationAttributeValueImpl) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "" + value;
    }
}
