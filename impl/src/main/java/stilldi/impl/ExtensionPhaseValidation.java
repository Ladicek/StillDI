package stilldi.impl;

class ExtensionPhaseValidation extends ExtensionPhaseBase {
    ExtensionPhaseValidation(jakarta.enterprise.inject.spi.BeanManager beanManager, ExtensionInvoker util,
            SharedErrors errors) {
        super(ExtensionPhase.VALIDATION, beanManager, util, errors);
    }

    @Override
    Object argumentForExtensionMethod(ExtensionMethodParameterType type, java.lang.reflect.Method method) {
        if (type == ExtensionMethodParameterType.TYPES) {
            return new TypesImpl();
        }

        return super.argumentForExtensionMethod(type, method);
    }
}
