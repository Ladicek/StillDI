package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanBuilder;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserverBuilder;

import java.util.List;

class SyntheticComponentsImpl implements SyntheticComponents {
    final List<SyntheticBeanBuilderImpl<?>> syntheticBeans;
    final List<SyntheticObserverBuilderImpl> syntheticObservers;
    final Class<?> extensionClass;

    SyntheticComponentsImpl(List<SyntheticBeanBuilderImpl<?>> syntheticBeans,
            List<SyntheticObserverBuilderImpl> syntheticObservers, Class<?> extensionClass) {
        this.syntheticBeans = syntheticBeans;
        this.syntheticObservers = syntheticObservers;
        this.extensionClass = extensionClass;
    }

    @Override
    public <T> SyntheticBeanBuilder<T> addBean(Class<T> implementationClass) {
        SyntheticBeanBuilderImpl<T> builder = new SyntheticBeanBuilderImpl<>(implementationClass);
        syntheticBeans.add(builder);
        return builder;
    }

    @Override
    public SyntheticObserverBuilder addObserver() {
        SyntheticObserverBuilderImpl builder = new SyntheticObserverBuilderImpl(extensionClass);
        syntheticObservers.add(builder);
        return builder;
    }
}
