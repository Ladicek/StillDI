package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.AppArchiveBuilder;

import java.util.Set;

class AppArchiveBuilderImpl implements AppArchiveBuilder {
    private final Set<String> classes;

    AppArchiveBuilderImpl(Set<String> classes) {
        this.classes = classes;
    }

    @Override
    public void add(String fullyQualifiedClassName) {
        classes.add(fullyQualifiedClassName);
    }

    @Override
    public void addSubtypesOf(String fullyQualifiedClassName) {
        throw new UnsupportedOperationException("AppArchiveBuilderImpl.addSubtypesOf(" + fullyQualifiedClassName + ")");
    }
}
