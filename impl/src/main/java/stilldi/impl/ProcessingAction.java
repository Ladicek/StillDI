package stilldi.impl;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

class ProcessingAction {
    private final Set<Class<?>> exactClasses;
    private final Set<Class<?>> superclasses;
    private final Consumer<javax.enterprise.inject.spi.ProcessBean<?>> beanAcceptor;
    private final Consumer<javax.enterprise.inject.spi.ProcessObserverMethod<?, ?>> observerAcceptor;

    ProcessingAction(Set<Class<?>> exactClasses, Set<Class<?>> superclasses,
            Consumer<javax.enterprise.inject.spi.ProcessBean<?>> beanAcceptor,
            Consumer<javax.enterprise.inject.spi.ProcessObserverMethod<?, ?>> observerAcceptor) {
        this.exactClasses = exactClasses;
        this.superclasses = superclasses;
        this.beanAcceptor = beanAcceptor;
        this.observerAcceptor = observerAcceptor;
    }

    void run(javax.enterprise.inject.spi.ProcessBean<?> pb) {
        if (beanAcceptor == null) {
            return;
        }

        Set<Type> beanTypes = pb.getBean().getTypes();
        if (!satisfies(beanTypes)) {
            return;
        }

        beanAcceptor.accept(pb);
    }

    void run(javax.enterprise.inject.spi.ProcessObserverMethod<?, ?> pom) {
        if (observerAcceptor == null) {
            return;
        }

        Type observedType = pom.getObserverMethod().getObservedType();
        if (!satisfies(Collections.singleton(observedType))) {
            return;
        }

        observerAcceptor.accept(pom);
    }

    private boolean satisfies(Set<Type> types) {
        for (Type type : types) {
            Class<?> rawType = getRawType(type);
            if (rawType == null) {
                continue;
            }

            if (exactClasses.contains(rawType)) {
                return true;
            }

            for (Class<?> superclass : superclasses) {
                if (superclass.isAssignableFrom(rawType)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static Class<?> getRawType(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return (Class<?>) ((ParameterizedType) type).getRawType();
        } else if (type instanceof TypeVariable) {
            return getRawType(((TypeVariable<?>) type).getBounds()[0]);
        } else if (type instanceof WildcardType) {
            return getRawType(((WildcardType) type).getUpperBounds()[0]);
        } else if (type instanceof GenericArrayType) {
            Class<?> rawType = getRawType(((GenericArrayType) type).getGenericComponentType());
            if (rawType != null) {
                return Array.newInstance(rawType, 0).getClass();
            }
        }
        return null;
    }
}
