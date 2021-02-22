package stilldi.impl;

final class BeanManagerAccess {
    private static javax.enterprise.inject.spi.BeanManager beanManager;

    static <T> javax.enterprise.inject.spi.AnnotatedType<T> createAnnotatedType(Class<T> clazz) {
        if (beanManager == null) {
            beanManager = javax.enterprise.inject.spi.CDI.current().getBeanManager();
        }

        return beanManager.createAnnotatedType(clazz);
    }
}
