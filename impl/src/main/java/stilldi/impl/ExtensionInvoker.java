package stilldi.impl;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.SkipIfPortableExtensionPresent;
import jakarta.interceptor.Interceptor;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

class ExtensionInvoker {
    private final Map<String, Class<?>> extensionClasses = new HashMap<>();
    private final Map<Class<?>, Object> extensionClassInstances = new HashMap<>();

    ExtensionInvoker() {
        for (BuildCompatibleExtension extension : ServiceLoader.load(BuildCompatibleExtension.class)) {
            Class<? extends BuildCompatibleExtension> extensionClass = extension.getClass();

            SkipIfPortableExtensionPresent skip = extensionClass.getAnnotation(SkipIfPortableExtensionPresent.class);
            if (skip != null && isClassPresent(skip.value())) {
                continue;
            }

            extensionClasses.put(extensionClass.getName(), extensionClass);
            extensionClassInstances.put(extensionClass, extension);
        }
    }

    private boolean isClassPresent(String className) {
        try {
            Class.forName(className, true, Thread.currentThread().getContextClassLoader());
            // TODO consult BeanManager if extension is present?
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    List<java.lang.reflect.Method> findExtensionMethods(Class<? extends Annotation> annotation) {
        return extensionClasses.values()
                .stream()
                .flatMap(it -> Arrays.stream(it.getDeclaredMethods()))
                .filter(it -> it.getAnnotation(annotation) != null)
                .sorted((m1, m2) -> {
                    if (m1.equals(m2)) {
                        return 0;
                    }

                    int p1 = getExtensionMethodPriority(m1);
                    int p2 = getExtensionMethodPriority(m2);

                    // must _not_ return 0 if priorities are equal, because that isn't consistent
                    // with the `equals` method
                    return p1 < p2 ? -1 : 1;
                })
                .collect(Collectors.toList());
    }

    private int getExtensionMethodPriority(java.lang.reflect.Method method) {
        Priority priority = method.getAnnotation(Priority.class);
        if (priority != null) {
            return priority.value();
        }
        return Interceptor.Priority.APPLICATION + 500;
    }

    void callExtensionMethod(java.lang.reflect.Method method, List<Object> arguments) throws ReflectiveOperationException {
        Class<?>[] parameterTypes = new Class[arguments.size()];

        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = arguments.get(i);
            Class<?> argumentClass = argument.getClass();

            // beware of ordering! subtypes must precede supertypes
            if (jakarta.enterprise.lang.model.declarations.ClassInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.lang.model.declarations.ClassInfo.class;
            } else if (jakarta.enterprise.lang.model.declarations.MethodInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.lang.model.declarations.MethodInfo.class;
            } else if (jakarta.enterprise.lang.model.declarations.FieldInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.lang.model.declarations.FieldInfo.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.ScannedClasses.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.ScannedClasses.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.ClassConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.ClassConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.MethodConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.MethodConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.FieldConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.FieldConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.BeanInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.BeanInfo.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.ObserverInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.ObserverInfo.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.Messages.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.Messages.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.Types.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.Types.class;
            } else {
                // should never happen, internal error (or missing error handling) if it does
                throw new IllegalArgumentException("Unexpected extension method argument: " + argument);
            }
        }

        Class<?> extensionClass = extensionClasses.get(method.getDeclaringClass().getName());
        Object extensionClassInstance = extensionClassInstances.get(extensionClass);

        method.setAccessible(true);
        method.invoke(extensionClassInstance, arguments.toArray());
    }

    void clear() {
        extensionClasses.clear();
        extensionClassInstances.clear();
    }
}
