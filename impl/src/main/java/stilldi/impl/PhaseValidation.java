package stilldi.impl;

import cdi.lite.extension.phases.Validation;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PhaseValidation {
    private final PhaseUtil util;
    private final Collection<BeanInfoImpl> allBeans;
    private final Collection<ObserverInfoImpl> allObservers;
    private final Collection<javax.enterprise.inject.spi.AnnotatedType<?>> allTypes;
    private final SharedErrors errors;

    PhaseValidation(PhaseUtil util, Collection<BeanInfoImpl> allBeans, Collection<ObserverInfoImpl> allObservers,
            Collection<javax.enterprise.inject.spi.AnnotatedType<?>> allTypes, SharedErrors errors) {
        this.util = util;
        this.allBeans = allBeans;
        this.allObservers = allObservers;
        this.allTypes = allTypes;
        this.errors = errors;
    }

    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private void doRun() throws ReflectiveOperationException {
        List<Method> extensionMethods = util.findExtensionMethods(Validation.class);

        for (Method method : extensionMethods) {
            processExtensionMethod(method);
        }
    }

    private void processExtensionMethod(Method method) throws ReflectiveOperationException {
        int numParameters = method.getParameterCount();
        List<PhaseUtil.ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            PhaseUtil.ExtensionMethodParameterType kind = PhaseUtil.ExtensionMethodParameterType.of(parameterType);
            parameters.add(kind);

            if (!kind.isAvailableIn(PhaseUtil.Phase.VALIDATION)) {
                throw new IllegalArgumentException("@Validation methods can't declare a parameter of type "
                        + parameterType + ", found at " + method + " @ " + method.getDeclaringClass());
            }
        }

        List<Object> arguments = new ArrayList<>(numParameters);
        for (PhaseUtil.ExtensionMethodParameterType parameter : parameters) {
            Object argument = createArgumentForExtensionMethodParameter(method, parameter);
            arguments.add(argument);
        }

        util.callExtensionMethod(method, arguments);
    }

    private Object createArgumentForExtensionMethodParameter(java.lang.reflect.Method method,
            PhaseUtil.ExtensionMethodParameterType kind) {
        switch (kind) {
            case APP_ARCHIVE:
                return new AppArchiveImpl(allTypes);
            case APP_DEPLOYMENT:
                return new AppDeploymentImpl(allBeans, allObservers);
            case MESSAGES:
                return new MessagesImpl(method, errors);
            case TYPES:
                return new TypesImpl();

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Validation method " + method);
        }
    }
}
