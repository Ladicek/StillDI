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
    private final MetaAnnotationsHelper helper;

    private final Set<String> additionalClasses = new HashSet<>();
    private final List<ContextBuilderImpl> contexts = new ArrayList<>();

    PhaseDiscovery(PhaseUtil util, SharedErrors errors, MetaAnnotationsHelper helper) {
        this.util = util;
        this.errors = errors;
        this.helper = helper;
    }

    PhaseDiscoveryResult run() {
        try {
            return doRun();
        } catch (Exception e) {
            // TODO proper diagnostics system
            throw new RuntimeException(e);
        }
    }

    PhaseDiscoveryResult doRun() throws ReflectiveOperationException {
        List<Method> extensionMethods = util.findExtensionMethods(Discovery.class);

        for (Method method : extensionMethods) {
            processExtensionMethod(method);
        }

        return new PhaseDiscoveryResult(additionalClasses, contexts, helper.newStereotypes);
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
            case META_ANNOTATIONS:
                return new MetaAnnotationsImpl(helper, contexts);
            case MESSAGES:
                return new MessagesImpl(method, errors);

            default:
                throw new IllegalArgumentException(kind + " parameter declared for @Discovery method " + method);
        }
    }
}
