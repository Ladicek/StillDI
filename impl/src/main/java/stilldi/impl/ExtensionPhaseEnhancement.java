package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.Enhancement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

class ExtensionPhaseEnhancement extends ExtensionPhaseBase {
    private final List<ExtensionPhaseEnhancementAction> actions;

    ExtensionPhaseEnhancement(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, List<ExtensionPhaseEnhancementAction> actions) {
        super(ExtensionPhase.ENHANCEMENT, beanManager, util, errors);
        this.actions = actions;
    }

    @Override
    void runExtensionMethod(java.lang.reflect.Method method) {
        int numParameters = method.getParameterCount();
        int numQueryParameters = 0;
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            ExtensionMethodParameterType parameter = ExtensionMethodParameterType.of(parameterType);
            parameters.add(parameter);

            if (parameter.isQuery()) {
                numQueryParameters++;
            }

            parameter.verifyAvailable(ExtensionPhase.ENHANCEMENT, method);
        }

        if (numQueryParameters == 0) {
            throw new IllegalArgumentException("No parameter of type ClassInfo, MethodInfo, FieldInfo, "
                    + "ClassConfig, MethodConfig, or FieldConfig for method " + method + " @ " + method.getDeclaringClass());
        }

        if (numQueryParameters > 1) {
            throw new IllegalArgumentException("More than 1 parameter of type ClassInfo, MethodInfo, FieldInfo, "
                    + "ClassConfig, MethodConfig, or FieldConfig for method " + method + " @ " + method.getDeclaringClass());
        }

        ExtensionMethodParameterType query = parameters.stream()
                .filter(ExtensionMethodParameterType::isQuery)
                .findAny()
                .get(); // guaranteed to be there

        Consumer<jakarta.enterprise.inject.spi.ProcessAnnotatedType<?>> patAcceptor = pat -> {
            List<List<Object>> argumentsForAllInvocations = new ArrayList<>();
            if (query == ExtensionMethodParameterType.CLASS_INFO) {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter == ExtensionMethodParameterType.CLASS_INFO) {
                        argument = new ClassInfoImpl(pat.getAnnotatedType());
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                argumentsForAllInvocations.add(arguments);
            } else if (query == ExtensionMethodParameterType.CLASS_CONFIG) {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter == ExtensionMethodParameterType.CLASS_CONFIG) {
                        argument = new ClassConfigImpl(pat.configureAnnotatedType());
                    } else {
                        argument = argumentForExtensionMethod(parameter, method);
                    }
                    arguments.add(argument);
                }

                argumentsForAllInvocations.add(arguments);
            } else if (query == ExtensionMethodParameterType.METHOD_INFO) {
                for (jakarta.enterprise.inject.spi.AnnotatedMethod<?> xmethod : pat.getAnnotatedType().getMethods()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_INFO) {
                            argument = new MethodInfoImpl(xmethod);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
                for (jakarta.enterprise.inject.spi.AnnotatedConstructor<?> xconstructor : pat.getAnnotatedType().getConstructors()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_INFO) {
                            argument = new MethodInfoImpl(xconstructor);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else if (query == ExtensionMethodParameterType.METHOD_CONFIG) {
                for (jakarta.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> methodConfigurator : pat.configureAnnotatedType().methods()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_CONFIG) {
                            argument = new MethodConfigImpl(methodConfigurator);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
                for (jakarta.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator<?> constructorConfigurator : pat.configureAnnotatedType().constructors()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.METHOD_CONFIG) {
                            argument = new MethodConstructorConfigImpl(constructorConfigurator);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else if (query == ExtensionMethodParameterType.FIELD_INFO) {
                for (jakarta.enterprise.inject.spi.AnnotatedField<?> xfield : pat.getAnnotatedType().getFields()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.FIELD_INFO) {
                            argument = new FieldInfoImpl(xfield);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
                        }
                        arguments.add(argument);
                    }
                    argumentsForAllInvocations.add(arguments);
                }
            } else if (query == ExtensionMethodParameterType.FIELD_CONFIG) {
                for (jakarta.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> fieldConfigurator : pat.configureAnnotatedType().fields()) {
                    List<Object> arguments = new ArrayList<>(numParameters);
                    for (ExtensionMethodParameterType parameter : parameters) {
                        Object argument;
                        if (parameter == ExtensionMethodParameterType.FIELD_CONFIG) {
                            argument = new FieldConfigImpl(fieldConfigurator);
                        } else {
                            argument = argumentForExtensionMethod(parameter, method);
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

        Enhancement enhancement = method.getAnnotation(Enhancement.class);
        actions.add(new ExtensionPhaseEnhancementAction(new HashSet<>(Arrays.asList(enhancement.types())), enhancement.withSubtypes(),
                new HashSet<>(Arrays.asList(enhancement.withAnnotations())), patAcceptor));
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl();
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
