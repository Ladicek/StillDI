package stilldi.impl.util.fake;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class AnnotatedPackage implements javax.enterprise.inject.spi.Annotated {
    private final Package pkg;

    public AnnotatedPackage(Package pkg) {
        this.pkg = pkg;
    }

    public Package getJavaPackage() {
        return pkg;
    }

    @Override
    public Type getBaseType() {
        return null;
    }

    @Override
    public Set<Type> getTypeClosure() {
        return Collections.emptySet();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        return pkg.getAnnotation(annotationType);
    }

    @Override
    public <T extends Annotation> Set<T> getAnnotations(Class<T> annotationType) {
        return new HashSet<>(Arrays.asList(pkg.getAnnotationsByType(annotationType)));
    }

    @Override
    public Set<Annotation> getAnnotations() {
        return new HashSet<>(Arrays.asList(pkg.getAnnotations()));
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        return pkg.isAnnotationPresent(annotationType);
    }

    @Override
    public String toString() {
        return pkg.getName();
    }
}
