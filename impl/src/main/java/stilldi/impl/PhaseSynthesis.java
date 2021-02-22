package stilldi.impl;

import cdi.lite.extension.phases.Synthesis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class PhaseSynthesis {
    private final PhaseUtil util;
    private final Collection<BeanInfoImpl> allBeans;
    private final Collection<ObserverInfoImpl> allObservers;
    private final Collection<javax.enterprise.inject.spi.AnnotatedType<?>> allTypes;
    private final SharedErrors errors;

    final List<SyntheticBeanBuilderImpl<?>> syntheticBeans = new ArrayList<>();
    final List<SyntheticObserverBuilderImpl> syntheticObservers = new ArrayList<>();

    PhaseSynthesis(PhaseUtil util, Collection<BeanInfoImpl> allBeans, Collection<ObserverInfoImpl> allObservers,
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
        List<Method> extensionMethods = util.findExtensionMethods(Synthesis.class);

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

            if (!kind.isAvailableIn(PhaseUtil.Phase.SYNTHESIS)) {
                throw new IllegalArgumentException("@Synthesis methods can't declare a parameter of type "
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

    private Object createArgumentForExtensionMethodParameter(Method method, PhaseUtil.ExtensionMethodParameterType kind) {
        switch (kind) {
            case APP_ARCHIVE:
                return new AppArchiveImpl(allTypes);
            case APP_DEPLOYMENT:
                return new AppDeploymentImpl(allBeans, allObservers);
            case MESSAGES:
                return new MessagesImpl(method, errors);
            case SYNTHETIC_COMPONENTS:
                return new SyntheticComponentsImpl(syntheticBeans, syntheticObservers, method.getDeclaringClass());
            case TYPES:
                return new TypesImpl();

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Synthesis method " + method);
        }
    }
}
