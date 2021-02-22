package stilldi.impl;

import cdi.lite.extension.phases.Discovery;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class PhaseDiscovery {
    private final PhaseUtil util;
    private final SharedErrors errors;

    final Set<String> additionalClasses = new HashSet<>();
    final List<ContextBuilderImpl> contexts = new ArrayList<>();

    PhaseDiscovery(PhaseUtil util, SharedErrors errors) {
        this.util = util;
        this.errors = errors;
    }

    void run() {
        try {
            doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    private void doRun() throws ReflectiveOperationException {
        List<Method> extensionMethods = util.findExtensionMethods(Discovery.class);

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

            if (!kind.isAvailableIn(PhaseUtil.Phase.DISCOVERY)) {
                throw new IllegalArgumentException("@Discovery methods can't declare a parameter of type "
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

    private Object createArgumentForExtensionMethodParameter(java.lang.reflect.Method method, PhaseUtil.ExtensionMethodParameterType kind) {
        switch (kind) {
            case APP_ARCHIVE_BUILDER:
                return new AppArchiveBuilderImpl(additionalClasses);
            case CONTEXTS:
                return new ContextsImpl(contexts);
            case MESSAGES:
                return new MessagesImpl(method, errors);

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Discovery method " + method);
        }
    }
}
