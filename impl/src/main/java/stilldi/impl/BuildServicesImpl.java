package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.AnnotationBuilderFactory;
import jakarta.enterprise.inject.build.compatible.spi.BuildServices;

public class BuildServicesImpl implements BuildServices {
    @Override
    public AnnotationBuilderFactory annotationBuilderFactory() {
        return new AnnotationBuilderFactoryImpl();
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
