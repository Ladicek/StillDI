package stilldi.impl;

import cdi.lite.extension.AppArchive;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

class AppArchiveImpl implements AppArchive {
    private final Collection<javax.enterprise.inject.spi.AnnotatedType<?>> types;

    AppArchiveImpl(Collection<javax.enterprise.inject.spi.AnnotatedType<?>> types) {
        this.types = types;
    }

    @Override
    public ClassQuery classes() {
        return new ClassQueryImpl();
    }

    @Override
    public MethodQuery constructors() {
        return new MethodQueryImpl(true);
    }

    @Override
    public MethodQuery methods() {
        return new MethodQueryImpl(false);
    }

    @Override
    public FieldQuery fields() {
        return null;
    }

    class ClassQueryImpl implements ClassQuery {
        private Set<Class<?>> requiredExactClasses;
        private Set<Class<?>> requiredSuperclasses;
        private Set<Class<? extends Annotation>> requiredAnnotations;

        @Override
        public ClassQuery exactly(Class<?> clazz) {
            if (requiredExactClasses == null) {
                requiredExactClasses = new HashSet<>();
            }

            requiredExactClasses.add(clazz);
            return this;
        }

        @Override
        public ClassQuery subtypeOf(Class<?> clazz) {
            if (requiredSuperclasses == null) {
                requiredSuperclasses = new HashSet<>();
            }

            requiredSuperclasses.add(clazz);
            return this;
        }

        @Override
        public ClassQuery annotatedWith(Class<? extends Annotation> annotationType) {
            if (requiredAnnotations == null) {
                requiredAnnotations = new HashSet<>();
            }

            requiredAnnotations.add(annotationType);
            return this;
        }

        @Override
        public void forEach(Consumer<ClassInfo<?>> consumer) {
            stream().map(ClassInfoImpl::new).forEach(consumer);
        }

        Stream<javax.enterprise.inject.spi.AnnotatedType<?>> stream() {
            Stream<javax.enterprise.inject.spi.AnnotatedType<?>> result = types.stream();

            if (requiredExactClasses != null || requiredSuperclasses != null) {
                Set<Class<?>> exactClasses = requiredExactClasses != null ? requiredExactClasses : Collections.emptySet();
                Set<Class<?>> superclasses = requiredSuperclasses != null ? requiredSuperclasses : Collections.emptySet();
                result = result.filter(it -> exactClasses.contains(it.getJavaClass())
                        || superclasses.stream().anyMatch(clazz -> clazz.isAssignableFrom(it.getJavaClass())));
            }

            if (requiredAnnotations != null) {
                result = result.filter(it -> requiredAnnotations.stream().anyMatch(it::isAnnotationPresent));
            }

            return result;
        }
    }

    class MethodQueryImpl implements MethodQuery {
        private final boolean constructors;
        private Stream<javax.enterprise.inject.spi.AnnotatedType<?>> requiredDeclarationSites;
        private Set<java.lang.reflect.Type> requiredReturnTypes;
        private Set<Class<? extends Annotation>> requiredAnnotations;

        MethodQueryImpl(boolean constructors) {
            this.constructors = constructors;
        }

        @Override
        public MethodQuery declaredOn(ClassQuery classes) {
            if (requiredDeclarationSites == null) {
                requiredDeclarationSites = ((ClassQueryImpl) classes).stream();
            } else {
                requiredDeclarationSites = Stream.concat(requiredDeclarationSites, ((ClassQueryImpl) classes).stream());
            }
            return this;
        }

        @Override
        public MethodQuery withReturnType(Class<?> type) {
            if (requiredReturnTypes == null) {
                requiredReturnTypes = new HashSet<>();
            }

            requiredReturnTypes.add(type);
            return this;
        }

        @Override
        public MethodQuery withReturnType(Type type) {
            if (requiredReturnTypes == null) {
                requiredReturnTypes = new HashSet<>();
            }

            requiredReturnTypes.add(((TypeImpl<?>) type).reflectionType.getType());
            return this;
        }

        @Override
        public MethodQuery annotatedWith(Class<? extends Annotation> annotationType) {
            if (requiredAnnotations == null) {
                requiredAnnotations = new HashSet<>();
            }

            requiredAnnotations.add(annotationType);
            return this;
        }

        @Override
        public void forEach(Consumer<MethodInfo<?>> consumer) {
            stream().forEach(consumer);
        }

        Stream<MethodInfo<?>> stream() {
            Stream<javax.enterprise.inject.spi.AnnotatedType<?>> declarationSites = requiredDeclarationSites != null
                    ? requiredDeclarationSites.distinct() : types.stream();

            Stream<javax.enterprise.inject.spi.AnnotatedCallable<?>> result = declarationSites
                    .flatMap(it -> constructors ? it.getConstructors().stream() : it.getMethods().stream());

            if (requiredReturnTypes != null) {
                result = result.filter(it -> {
                    if (it instanceof javax.enterprise.inject.spi.AnnotatedMethod) {
                        java.lang.reflect.Type returnType = ((javax.enterprise.inject.spi.AnnotatedMethod<?>) it).getJavaMember().getReturnType();
                        return requiredReturnTypes.contains(returnType);
                    }
                    return false;
                });
            }

            if (requiredAnnotations != null) {
                result = result.filter(it -> requiredAnnotations.stream().anyMatch(it::isAnnotationPresent));
            }

            return result.map(MethodInfoImpl::new);
        }
    }

    class FieldQueryImpl implements FieldQuery {
        private Stream<javax.enterprise.inject.spi.AnnotatedType<?>> requiredDeclarationSites;
        private Set<java.lang.reflect.Type> requiredTypes;
        private Set<Class<? extends Annotation>> requiredAnnotations;

        @Override
        public FieldQuery declaredOn(ClassQuery classes) {
            if (requiredDeclarationSites == null) {
                requiredDeclarationSites = ((ClassQueryImpl) classes).stream();
            } else {
                requiredDeclarationSites = Stream.concat(requiredDeclarationSites, ((ClassQueryImpl) classes).stream());
            }
            return this;
        }

        @Override
        public FieldQuery ofType(Class<?> type) {
            if (requiredTypes == null) {
                requiredTypes = new HashSet<>();
            }

            requiredTypes.add(type);
            return this;
        }

        @Override
        public FieldQuery ofType(Type type) {
            if (requiredTypes == null) {
                requiredTypes = new HashSet<>();
            }

            requiredTypes.add(((TypeImpl<?>) type).reflectionType.getType());
            return this;
        }

        @Override
        public FieldQuery annotatedWith(Class<? extends Annotation> annotationType) {
            if (requiredAnnotations == null) {
                requiredAnnotations = new HashSet<>();
            }

            requiredAnnotations.add(annotationType);
            return this;
        }

        @Override
        public void forEach(Consumer<FieldInfo<?>> consumer) {
            stream().forEach(consumer);
        }

        Stream<FieldInfo<?>> stream() {
            Stream<javax.enterprise.inject.spi.AnnotatedType<?>> declarationSites = requiredDeclarationSites != null
                    ? requiredDeclarationSites.distinct() : types.stream();

            Stream<javax.enterprise.inject.spi.AnnotatedField<?>> result = declarationSites
                    .flatMap(it -> it.getFields().stream());

            if (requiredTypes != null) {
                result = result.filter(it -> {
                    java.lang.reflect.Type type = it.getJavaMember().getType();
                    return requiredTypes.contains(type);
                });
            }

            if (requiredAnnotations != null) {
                result = result.filter(it -> requiredAnnotations.stream().anyMatch(it::isAnnotationPresent));
            }

            return result.map(FieldInfoImpl::new);
        }
    }
}
