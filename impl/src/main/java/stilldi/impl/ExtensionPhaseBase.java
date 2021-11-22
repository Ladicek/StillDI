package stilldi.impl;

import java.util.ArrayList;
import java.util.List;

abstract class ExtensionPhaseBase {
    private final ExtensionPhase phase;

    final jakarta.enterprise.inject.spi.BeanManager beanManager;
    final ExtensionInvoker util;
    final SharedErrors errors;

    ExtensionPhaseBase(ExtensionPhase phase, jakarta.enterprise.inject.spi.BeanManager beanManager,
            ExtensionInvoker util, SharedErrors errors) {
        this.phase = phase;

        this.beanManager = beanManager;
        this.util = util;
        this.errors = errors;
    }

    final void run() {
        try {
            List<java.lang.reflect.Method> extensionMethods = util.findExtensionMethods(phase.annotation);

            for (java.lang.reflect.Method method : extensionMethods) {
                runExtensionMethod(method);
            }
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    // complex phases may override, but this is enough for the simple phases
    void runExtensionMethod(java.lang.reflect.Method method) throws ReflectiveOperationException {
        int numParameters = method.getParameterCount();
        List<ExtensionMethodParameterType> parameters = new ArrayList<>(numParameters);
        for (int i = 0; i < numParameters; i++) {
            Class<?> parameterType = method.getParameterTypes()[i];
            ExtensionMethodParameterType parameter = ExtensionMethodParameterType.of(parameterType);
            parameters.add(parameter);

            parameter.verifyAvailable(phase, method);
        }

        List<Object> arguments = new ArrayList<>(numParameters);
        for (ExtensionMethodParameterType parameter : parameters) {
            Object argument = argumentForExtensionMethod(parameter, method);
            arguments.add(argument);
        }

        util.callExtensionMethod(method, arguments);
    }

    // all phases should override and use this as a fallback
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.MESSAGES) {
            return new MessagesImpl(method, errors);
        }

        throw new IllegalArgumentException("internal error, " + type + " parameter declared at "
                + method.getDeclaringClass().getSimpleName() + "." + method.getName());
    }
}
