package stilldi.impl.util.impl.specific;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;

public final class CurrentInjectionPoint {
    public static InjectionPoint get() {
        if (isWeld()) {
            return getFromWeld();
        }

        throw new UnsupportedOperationException("Couldn't obtain current injection point, not running Weld?");
    }

    private static boolean isWeld() {
        try {
            Class.forName("org.jboss.weld.manager.api.WeldManager");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static InjectionPoint getFromWeld() {
        try {
            Class<?> weldManagerClass = Class.forName("org.jboss.weld.manager.api.WeldManager");
            Class<?> serviceRegistryClass = Class.forName("org.jboss.weld.bootstrap.api.ServiceRegistry");
            Class<?> currentInjectionPointClass = Class.forName("org.jboss.weld.injection.CurrentInjectionPoint");

            BeanManager beanManager = CDI.current().getBeanManager();
            Object weldManager = weldManagerClass.cast(beanManager);
            Object serviceRegistry = weldManagerClass.getMethod("getServices").invoke(weldManager);
            Object currentInjectionPoint = serviceRegistryClass.getMethod("get", Class.class)
                    .invoke(serviceRegistry, currentInjectionPointClass);
            Object result = currentInjectionPointClass.getMethod("peek").invoke(currentInjectionPoint);
            return (InjectionPoint) result;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
