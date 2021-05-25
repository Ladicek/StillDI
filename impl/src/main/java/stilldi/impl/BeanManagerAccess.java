package stilldi.impl;

// TODO this is mostly a hack
final class BeanManagerAccess {
    private static jakarta.enterprise.inject.spi.BeanManager beanManager;

    static void set(jakarta.enterprise.inject.spi.BeanManager beanManager) {
        BeanManagerAccess.beanManager = beanManager;
    }

    static void remove() {
        BeanManagerAccess.beanManager = null;
    }

    static <T> jakarta.enterprise.inject.spi.AnnotatedType<T> createAnnotatedType(Class<T> clazz) {
        if (beanManager == null) {
            throw new IllegalStateException("BeanManagerAccess.createAnnotatedType can only be called within an extension method");
        }

        return beanManager.createAnnotatedType(clazz);
    }
}
