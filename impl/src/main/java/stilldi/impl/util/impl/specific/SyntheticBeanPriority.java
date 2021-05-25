package stilldi.impl.util.impl.specific;

import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;

public final class SyntheticBeanPriority {
    public static void set(BeanConfigurator<?> configurator, int priority) {
        if (isWeld()) {
            setWithWeld(configurator, priority);
            return;
        }

        throw new UnsupportedOperationException("Couldn't set synthetic bean priority, not running Weld?");
    }

    private static boolean isWeld() {
        try {
            Class.forName("org.jboss.weld.bootstrap.event.WeldBeanConfigurator");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static void setWithWeld(BeanConfigurator<?> configurator, int priority) {
        try {
            Class<?> weldBeanConfiguratorClass = Class.forName("org.jboss.weld.bootstrap.event.WeldBeanConfigurator");

            Object weldConfigurator = weldBeanConfiguratorClass.cast(configurator);
            weldBeanConfiguratorClass.getMethod("priority", int.class).invoke(weldConfigurator, priority);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
