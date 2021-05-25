package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.ExactType;
import jakarta.enterprise.inject.build.compatible.spi.Processing;
import jakarta.enterprise.inject.build.compatible.spi.SubtypesOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

class PhaseProcessing {
    private final PhaseUtil util;
    private final SharedErrors errors;

    PhaseProcessing(PhaseUtil util, SharedErrors errors) {
        this.util = util;
        this.errors = errors;
    }

    PhaseProcessingResult run() {
        try {
            return doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private PhaseProcessingResult doRun() {
        List<Method> extensionMethods = util.findExtensionMethods(Processing.class);

        List<ProcessingAction> actions = new ArrayList<>();
        for (Method method : extensionMethods) {
            processExtensionMethod(method, actions::add);
        }

        return new PhaseProcessingResult(actions);
    }

    private void processExtensionMethod(Method method, Consumer<ProcessingAction> acceptor) {
        List<Annotation> constraintAnnotations = new ArrayList<>();
        constraintAnnotations.addAll(Arrays.asList(method.getAnnotationsByType(ExactType.class)));
        constraintAnnotations.addAll(Arrays.asList(method.getAnnotationsByType(SubtypesOf.class)));

        int numParameters = method.getParameterCount();
        int numQueryParameters = 0;
        List<PhaseUtil.ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            PhaseUtil.ExtensionMethodParameterType kind = PhaseUtil.ExtensionMethodParameterType.of(parameterType);
            parameters.add(kind);

            if (kind.isQuery()) {
                numQueryParameters++;
            }

            if (!kind.isAvailableIn(PhaseUtil.Phase.PROCESSING)) {
                throw new IllegalArgumentException("@Processing methods can't declare a parameter of type "
                        + parameterType + ", found at " + method + " @ " + method.getDeclaringClass());
            }
        }

        if (numQueryParameters == 0) {
            throw new IllegalArgumentException("No parameter of type BeanInfo or ObserverInfo"
                    + " for method " + method + " @ " + method.getDeclaringClass());
        }

        if (numQueryParameters > 1) {
            throw new IllegalArgumentException("More than 1 parameter of type BeanInfo or ObserverInfo"
                    + " for method " + method + " @ " + method.getDeclaringClass());
        }

        if (constraintAnnotations.isEmpty()) {
            throw new IllegalArgumentException("Missing constraint annotation (@ExactType, @SubtypesOf) for method "
                    + method + " @ " + method.getDeclaringClass());
        }

        PhaseUtil.ExtensionMethodParameterType query = parameters.stream()
                .filter(PhaseUtil.ExtensionMethodParameterType::isQuery)
                .findAny()
                .get(); // guaranteed to be there

        Set<Class<?>> exactType = new HashSet<>();
        Set<Class<?>> subtypesOf = new HashSet<>();
        for (Annotation constraintAnnotation : constraintAnnotations) {
            if (constraintAnnotation instanceof ExactType) {
                exactType.add(((ExactType) constraintAnnotation).type());
            } else if (constraintAnnotation instanceof SubtypesOf) {
                subtypesOf.add(((SubtypesOf) constraintAnnotation).type());
            }
        }

        if (query == PhaseUtil.ExtensionMethodParameterType.BEAN_INFO) {
            Consumer<jakarta.enterprise.inject.spi.ProcessBean<?>> beanAcceptor = pb -> {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter.isQuery()) {
                        jakarta.enterprise.inject.spi.AnnotatedParameter<?> disposer = null;
                        if (pb instanceof jakarta.enterprise.inject.spi.ProcessProducerField) {
                            disposer = ((jakarta.enterprise.inject.spi.ProcessProducerField<?, ?>) pb).getAnnotatedDisposedParameter();
                        } else if (pb instanceof jakarta.enterprise.inject.spi.ProcessProducerMethod) {
                            disposer = ((jakarta.enterprise.inject.spi.ProcessProducerMethod<?, ?>) pb).getAnnotatedDisposedParameter();
                        }

                        argument = new BeanInfoImpl(pb.getBean(), pb.getAnnotated(), disposer);
                    } else {
                        argument = createArgumentForExtensionMethodParameter(method, parameter);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            };

            acceptor.accept(new ProcessingAction(exactType, subtypesOf, beanAcceptor, null));
        } else if (query == PhaseUtil.ExtensionMethodParameterType.OBSERVER_INFO) {
            Consumer<jakarta.enterprise.inject.spi.ProcessObserverMethod<?, ?>> observerAcceptor = pom -> {
                List<Object> arguments = new ArrayList<>(numParameters);
                for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
                    Object argument;
                    if (parameter.isQuery()) {
                        argument = new ObserverInfoImpl(pom.getObserverMethod(), pom.getAnnotatedMethod());
                    } else {
                        argument = createArgumentForExtensionMethodParameter(method, parameter);
                    }
                    arguments.add(argument);
                }

                try {
                    util.callExtensionMethod(method, arguments);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            };

            acceptor.accept(new ProcessingAction(exactType, subtypesOf, null, observerAcceptor));
        } else {
            throw new IllegalStateException("Unknown query parameter " + query);
        }
    }

    private Object createArgumentForExtensionMethodParameter(Method method, PhaseUtil.ExtensionMethodParameterType kind) {
        switch (kind) {
            case TYPES:
                return new TypesImpl();
            case MESSAGES:
                return new MessagesImpl(method, errors);

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Processing method " + method);
        }
    }
}
