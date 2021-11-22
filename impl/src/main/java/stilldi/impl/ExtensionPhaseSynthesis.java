package stilldi.impl;

import java.util.List;

class ExtensionPhaseSynthesis extends ExtensionPhaseBase {
    private final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    private final List<SyntheticObserverBuilderImpl<?>> syntheticObservers;

    ExtensionPhaseSynthesis(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors, List<SyntheticBeanBuilderImpl<?>> syntheticBeans,
            List<SyntheticObserverBuilderImpl<?>> syntheticObservers) {
        super(ExtensionPhase.SYNTHESIS, beanManager, util, errors);
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        switch (type) {
            case SYNTHETIC_COMPONENTS:
                return new SyntheticComponentsImpl(syntheticBeans, syntheticObservers, method.getDeclaringClass());
            case TYPES:
                return new TypesImpl();

            default:
                return super.argumentForExtensionMethod(type, method);
        }
    }
}
