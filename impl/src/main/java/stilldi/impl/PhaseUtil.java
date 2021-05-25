package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.Annotations;
import jakarta.enterprise.inject.build.compatible.spi.AppArchive;
import jakarta.enterprise.inject.build.compatible.spi.AppArchiveBuilder;
import jakarta.enterprise.inject.build.compatible.spi.AppArchiveConfig;
import jakarta.enterprise.inject.build.compatible.spi.AppDeployment;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.ExtensionPriority;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations;
import jakarta.enterprise.inject.build.compatible.spi.MethodConfig;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.inject.build.compatible.spi.SkipIfPortableExtensionPresent;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.Types;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

class PhaseUtil {
    private final Map<String, Class<?>> extensionClasses = new HashMap<>();
    private final Map<Class<?>, Object> extensionClassInstances = new HashMap<>();

    PhaseUtil() {
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
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    List<Method> findExtensionMethods(Class<? extends Annotation> annotation) {
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

    private int getExtensionMethodPriority(Method method) {
        ExtensionPriority priority = method.getAnnotation(ExtensionPriority.class);
        if (priority != null) {
            return priority.value();
        }
        return 10_000;
    }

    // ---

    enum Phase {
        DISCOVERY,
        ENHANCEMENT,
        PROCESSING,
        SYNTHESIS,
        VALIDATION
    }

    enum ExtensionMethodParameterType {
        CLASS_CONFIG(Phase.ENHANCEMENT),
        METHOD_CONFIG(Phase.ENHANCEMENT),
        FIELD_CONFIG(Phase.ENHANCEMENT),

        BEAN_INFO(Phase.PROCESSING),
        OBSERVER_INFO(Phase.PROCESSING),

        ANNOTATIONS(Phase.ENHANCEMENT),
        APP_ARCHIVE(Phase.ENHANCEMENT, Phase.SYNTHESIS, Phase.VALIDATION), // TODO remove @Enhancement?
        APP_ARCHIVE_BUILDER(Phase.DISCOVERY),
        APP_ARCHIVE_CONFIG(Phase.ENHANCEMENT),
        APP_DEPLOYMENT(Phase.SYNTHESIS, Phase.VALIDATION),
        MESSAGES(Phase.DISCOVERY, Phase.ENHANCEMENT, Phase.PROCESSING, Phase.SYNTHESIS, Phase.VALIDATION),
        META_ANNOTATIONS(Phase.DISCOVERY),
        SYNTHETIC_COMPONENTS(Phase.SYNTHESIS),
        TYPES(Phase.ENHANCEMENT, Phase.PROCESSING, Phase.SYNTHESIS, Phase.VALIDATION),

        UNKNOWN,
        ;

        private final Set<Phase> validPhases;

        ExtensionMethodParameterType(Phase... validPhases) {
            if (validPhases == null || validPhases.length == 0) {
                this.validPhases = EnumSet.noneOf(Phase.class);
            } else {
                this.validPhases = EnumSet.copyOf(Arrays.asList(validPhases));
            }
        }

        boolean isQuery() {
            return this == CLASS_CONFIG
                    || this == METHOD_CONFIG
                    || this == FIELD_CONFIG
                    || this == BEAN_INFO
                    || this == OBSERVER_INFO
                    ;
        }

        boolean isAvailableIn(Phase phase) {
            return validPhases.contains(phase);
        }

        static ExtensionMethodParameterType of(Class<?> type) {
            if (ClassConfig.class.equals(type)) {
                return CLASS_CONFIG;
            } else if (MethodConfig.class.equals(type)) {
                return METHOD_CONFIG;
            } else if (FieldConfig.class.equals(type)) {
                return FIELD_CONFIG;
            } else if (BeanInfo.class.equals(type)) {
                return BEAN_INFO;
            } else if (ObserverInfo.class.equals(type)) {
                return OBSERVER_INFO;
            } else if (Annotations.class.equals(type)) {
                return ANNOTATIONS;
            } else if (AppArchive.class.equals(type)) {
                return APP_ARCHIVE;
            } else if (AppArchiveBuilder.class.equals(type)) {
                return APP_ARCHIVE_BUILDER;
            } else if (AppArchiveConfig.class.equals(type)) {
                return APP_ARCHIVE_CONFIG;
            } else if (AppDeployment.class.equals(type)) {
                return APP_DEPLOYMENT;
            } else if (Messages.class.equals(type)) {
                return MESSAGES;
            } else if (MetaAnnotations.class.equals(type)) {
                return META_ANNOTATIONS;
            } else if (SyntheticComponents.class.equals(type)) {
                return SYNTHETIC_COMPONENTS;
            } else if (Types.class.equals(type)) {
                return TYPES;
            } else {
                return UNKNOWN;
            }
        }
    }

    // ---

    void callExtensionMethod(Method method, List<Object> arguments) throws ReflectiveOperationException {
        Class<?>[] parameterTypes = new Class[arguments.size()];

        for (int i = 0; i < parameterTypes.length; i++) {
            Object argument = arguments.get(i);
            Class<?> argumentClass = argument.getClass();

            // beware of ordering! subtypes must precede supertypes
            if (jakarta.enterprise.inject.build.compatible.spi.AppArchiveBuilder.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.AppArchiveBuilder.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.MetaAnnotations.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.ClassConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.ClassConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.MethodConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.MethodConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.FieldConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.FieldConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.Annotations.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.Annotations.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.AppArchiveConfig.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.AppArchiveConfig.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.BeanInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.BeanInfo.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.ObserverInfo.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.ObserverInfo.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.AppArchive.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.AppArchive.class;
            } else if (jakarta.enterprise.inject.build.compatible.spi.AppDeployment.class.isAssignableFrom(argumentClass)) {
                parameterTypes[i] = jakarta.enterprise.inject.build.compatible.spi.AppDeployment.class;
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

    // ---

    void clear() {
        extensionClasses.clear();
        extensionClassInstances.clear();
    }
}
