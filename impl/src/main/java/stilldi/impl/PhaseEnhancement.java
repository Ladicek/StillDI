package stilldi.impl;

import cdi.lite.extension.phases.Enhancement;
import cdi.lite.extension.phases.enhancement.ExactType;
import cdi.lite.extension.phases.enhancement.SubtypesOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

class PhaseEnhancement {
    private final PhaseUtil util;
    private final SharedErrors errors;

    PhaseEnhancement(PhaseUtil util, SharedErrors errors) {
        this.util = util;
        this.errors = errors;
    }

    PhaseEnhancementResult run() {
        try {
            return doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private PhaseEnhancementResult doRun() throws ReflectiveOperationException {
        List<Method> extensionMethods = util.findExtensionMethods(Enhancement.class);

        List<EnhancementAction> actions = new ArrayList<>();
        for (Method method : extensionMethods) {
            processExtensionMethod(method, actions::add);
        }

        return new PhaseEnhancementResult(actions);
    }

    private void processExtensionMethod(Method method, Consumer<EnhancementAction> acceptor) throws ReflectiveOperationException {
        List<Annotation> constraintAnnotations = new ArrayList<>();
        constraintAnnotations.addAll(Arrays.asList(method.getAnnotationsByType(ExactType.class)));
        constraintAnnotations.addAll(Arrays.asList(method.getAnnotationsByType(SubtypesOf.class)));

        int numParameters = method.getParameterCount();
        int numQueryParameters = 0;
        boolean appArchiveConfigPresent = false;
        List<PhaseUtil.ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            PhaseUtil.ExtensionMethodParameterType kind = PhaseUtil.ExtensionMethodParameterType.of(parameterType);
            parameters.add(kind);

            if (kind.isQuery()) {
                numQueryParameters++;
            }

            if (kind == PhaseUtil.ExtensionMethodParameterType.APP_ARCHIVE_CONFIG) {
                appArchiveConfigPresent = true;
            }

            if (!kind.isAvailableIn(PhaseUtil.Phase.ENHANCEMENT)) {
                throw new IllegalArgumentException("@Enhancement methods can't declare a parameter of type "
                        + parameterType + ", found at " + method + " @ " + method.getDeclaringClass());
            }
        }

        if (numQueryParameters > 1) {
            throw new IllegalArgumentException("More than 1 parameter of type ClassConfig, MethodConfig or FieldConfig"
                    + " for method " + method + " @ " + method.getDeclaringClass());
        }

        if (numQueryParameters > 0 && appArchiveConfigPresent) {
            throw new IllegalArgumentException("Parameter of type AppArchiveConfig present together with a parameter"
                    + " of type ClassConfig, MethodConfig or FieldConfig for method " + method
                    + " @ " + method.getDeclaringClass());
        }

        if (numQueryParameters > 0 && constraintAnnotations.isEmpty()) {
            throw new IllegalArgumentException("Missing constraint annotation (@ExactType, @SubtypesOf) for method "
                    + method + " @ " + method.getDeclaringClass());
        }

        if (numQueryParameters == 0) {
            List<Object> arguments = new ArrayList<>(numParameters);
            for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                Object argument;
                if (parameter == PhaseUtil.ExtensionMethodParameterType.APP_ARCHIVE_CONFIG) {
                    argument = new AppArchiveConfigImpl(acceptor);
                } else {
                    argument = createArgumentForExtensionMethodParameter(method, parameter);
                }

                arguments.add(argument);
            }

            util.callExtensionMethod(method, arguments);
        } else {
            PhaseUtil.ExtensionMethodParameterType query = parameters.stream()
                    .filter(PhaseUtil.ExtensionMethodParameterType::isQuery)
                    .findAny()
                    .get(); // guaranteed to be there

            for (Annotation constraintAnnotation : constraintAnnotations) {
                Set<Class<?>> exactType = Collections.emptySet();
                Set<Class<?>> subtypesOf = Collections.emptySet();
                Set<Class<? extends Annotation>> requiredAnnotations = Collections.singleton(Annotation.class);

                if (constraintAnnotation instanceof ExactType) {
                    exactType = Collections.singleton(((ExactType) constraintAnnotation).type());
                    requiredAnnotations = new HashSet<>(Arrays.asList(((ExactType) constraintAnnotation).annotatedWith()));
                } else if (constraintAnnotation instanceof SubtypesOf) {
                    subtypesOf = Collections.singleton(((SubtypesOf) constraintAnnotation).type());
                    requiredAnnotations = new HashSet<>(Arrays.asList(((SubtypesOf) constraintAnnotation).annotatedWith()));
                }

                Consumer<javax.enterprise.inject.spi.ProcessAnnotatedType<?>> configurator = pat -> {
                    List<List<Object>> argumentsForAllInvocations = new ArrayList<>();
                    if (query == PhaseUtil.ExtensionMethodParameterType.CLASS_CONFIG) {
                        List<Object> arguments = new ArrayList<>(numParameters);
                        for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                            Object argument;
                            if (parameter == PhaseUtil.ExtensionMethodParameterType.CLASS_CONFIG) {
                                argument = new ClassConfigImpl(pat.configureAnnotatedType());
                            } else {
                                argument = createArgumentForExtensionMethodParameter(method, parameter);
                            }
                            arguments.add(argument);
                        }

                        argumentsForAllInvocations.add(arguments);
                    } else if (query == PhaseUtil.ExtensionMethodParameterType.METHOD_CONFIG) {
                        for (javax.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> methodConfigurator : pat.configureAnnotatedType().methods()) {
                            List<Object> arguments = new ArrayList<>(numParameters);
                            for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                                Object argument;
                                if (parameter == PhaseUtil.ExtensionMethodParameterType.METHOD_CONFIG) {
                                    argument = new MethodConfigImpl(methodConfigurator);
                                } else {
                                    argument = createArgumentForExtensionMethodParameter(method, parameter);
                                }
                                arguments.add(argument);
                            }
                            argumentsForAllInvocations.add(arguments);
                        }
                        for (javax.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator<?> constructorConfigurator : pat.configureAnnotatedType().constructors()) {
                            List<Object> arguments = new ArrayList<>(numParameters);
                            for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                                Object argument;
                                if (parameter == PhaseUtil.ExtensionMethodParameterType.METHOD_CONFIG) {
                                    argument = new MethodConstructorConfigImpl(constructorConfigurator);
                                } else {
                                    argument = createArgumentForExtensionMethodParameter(method, parameter);
                                }
                                arguments.add(argument);
                            }
                            argumentsForAllInvocations.add(arguments);
                        }
                    } else if (query == PhaseUtil.ExtensionMethodParameterType.FIELD_CONFIG) {
                        for (javax.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> fieldConfigurator : pat.configureAnnotatedType().fields()) {
                            List<Object> arguments = new ArrayList<>(numParameters);
                            for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                                Object argument;
                                if (parameter == PhaseUtil.ExtensionMethodParameterType.FIELD_CONFIG) {
                                    argument = new FieldConfigImpl(fieldConfigurator);
                                } else {
                                    argument = createArgumentForExtensionMethodParameter(method, parameter);
                                }
                                arguments.add(argument);
                            }
                            argumentsForAllInvocations.add(arguments);
                        }
                    } else {
                        throw new IllegalStateException("Unknown query parameter " + query);
                    }

                    for (List<Object> arguments : argumentsForAllInvocations) {
                        try {
                            util.callExtensionMethod(method, arguments);
                        } catch (ReflectiveOperationException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };

                acceptor.accept(new EnhancementAction(exactType, subtypesOf, requiredAnnotations, configurator));
            }
        }
    }

    private Object createArgumentForExtensionMethodParameter(java.lang.reflect.Method method, PhaseUtil.ExtensionMethodParameterType kind) {
        switch (kind) {
            case ANNOTATIONS:
                return new AnnotationsImpl();
            case APP_ARCHIVE:
                // TODO
                throw new UnsupportedOperationException("AppArchive probably can't be supported in @Enhancement on top of Portable Extensions");
            case TYPES:
                return new TypesImpl();
            case MESSAGES:
                return new MessagesImpl(method, errors);

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Enhancement method " + method);
        }
    }
}
