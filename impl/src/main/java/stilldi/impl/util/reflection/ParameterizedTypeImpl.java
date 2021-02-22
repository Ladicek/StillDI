package stilldi.impl.util.reflection;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

final class ParameterizedTypeImpl implements java.lang.reflect.ParameterizedType {
    private final Class<?> clazz;
    private final java.lang.reflect.Type[] typeArguments;
    private final java.lang.reflect.Type ownerType;

    ParameterizedTypeImpl(Class<?> clazz, java.lang.reflect.Type... typeArguments) {
        this(clazz, typeArguments, null);
    }

    ParameterizedTypeImpl(Class<?> clazz, java.lang.reflect.Type[] typeArguments, java.lang.reflect.Type ownerType) {
        this.clazz = clazz;
        this.typeArguments = typeArguments;
        this.ownerType = ownerType;
    }

    public java.lang.reflect.Type getRawType() {
        return clazz;
    }

    public java.lang.reflect.Type[] getActualTypeArguments() {
        return typeArguments;
    }

    public java.lang.reflect.Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.ParameterizedType that = (java.lang.reflect.ParameterizedType) o;
            return Objects.equals(ownerType, that.getOwnerType())
                    && Objects.equals(clazz, that.getRawType())
                    && Arrays.equals(typeArguments, that.getActualTypeArguments());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(typeArguments) ^ Objects.hashCode(ownerType) ^ Objects.hashCode(clazz);
    }

    @Override
    public String toString() {
        StringJoiner result = new StringJoiner(",", clazz.getName() + "<", ">");
        result.setEmptyValue(clazz.getName());
        for (java.lang.reflect.Type typeArgument : typeArguments) {
            result.add(typeArgument.toString());
        }
        return result.toString();
    }
}
