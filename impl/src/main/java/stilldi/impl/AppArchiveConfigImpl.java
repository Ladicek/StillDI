package stilldi.impl;

import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.declarations.FieldInfo;
import cdi.lite.extension.model.declarations.MethodInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.phases.enhancement.AppArchiveConfig;
import cdi.lite.extension.phases.enhancement.ClassConfig;
import cdi.lite.extension.phases.enhancement.FieldConfig;
import cdi.lite.extension.phases.enhancement.MethodConfig;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

class AppArchiveConfigImpl implements AppArchiveConfig {
    private final Consumer<EnhancementAction> acceptor;

    AppArchiveConfigImpl(Consumer<EnhancementAction> acceptor) {
        this.acceptor = acceptor;
    }

    @Override
    public ClassConfigQuery classes() {
        return new ClassConfigQueryImpl();
    }

    @Override
    public MethodConfigQuery constructors() {
        return new MethodConfigQueryImpl(true);
    }

    @Override
    public MethodConfigQuery methods() {
        return new MethodConfigQueryImpl(false);
    }

    @Override
    public FieldConfigQuery fields() {
        return null;
    }

    private class ClassConfigQueryImpl implements ClassConfigQuery {
        private final Set<Class<?>> requiredExactClasses = new HashSet<>();
        private final Set<Class<?>> requiredSuperclasses = new HashSet<>();
        private final Set<Class<? extends Annotation>> requiredAnnotations = new HashSet<>();

        @Override
        public ClassConfigQuery exactly(Class<?> clazz) {
            requiredExactClasses.add(clazz);
            return this;
        }

        @Override
        public ClassConfigQuery subtypeOf(Class<?> clazz) {
            requiredSuperclasses.add(clazz);
            return this;
        }

        @Override
        public ClassConfigQuery annotatedWith(Class<? extends Annotation> annotationType) {
            requiredAnnotations.add(annotationType);
            return this;
        }

        @Override
        public void forEach(Consumer<ClassInfo<?>> consumer) {
            throw new UnsupportedOperationException("Perhaps somehow remove *ConfigQuery.forEach?");
        }

        @Override
        public void configure(Consumer<ClassConfig<?>> consumer) {
            Consumer<javax.enterprise.inject.spi.ProcessAnnotatedType<?>> configurator = pat -> {
                ClassConfig<?> config = new ClassConfigImpl(pat.configureAnnotatedType());
                consumer.accept(config);
            };
            acceptor.accept(new EnhancementAction(requiredExactClasses, requiredSuperclasses, requiredAnnotations, configurator));
        }
    }

    private class MethodConfigQueryImpl implements MethodConfigQuery {
        private final boolean constructors;
        private final List<ClassConfigQueryImpl> classQueries = new ArrayList<>();
        private final Set<java.lang.reflect.Type> requiredReturnTypes = new HashSet<>();
        private final Set<Class<? extends Annotation>> requiredAnnotations = new HashSet<>();

        MethodConfigQueryImpl(boolean constructors) {
            this.constructors = constructors;
        }

        @Override
        public MethodConfigQuery declaredOn(ClassQuery classes) {
            classQueries.add((ClassConfigQueryImpl) classes);
            return this;
        }

        @Override
        public MethodConfigQuery withReturnType(Class<?> type) {
            requiredReturnTypes.add(type);
            return this;
        }

        @Override
        public MethodConfigQuery withReturnType(Type type) {
            requiredReturnTypes.add(((TypeImpl<?>) type).reflectionType.getType());
            return this;
        }

        @Override
        public MethodConfigQuery annotatedWith(Class<? extends Annotation> annotationType) {
            requiredAnnotations.add(annotationType);
            return this;
        }

        @Override
        public void forEach(Consumer<MethodInfo<?>> consumer) {
            throw new UnsupportedOperationException("Perhaps somehow remove *ConfigQuery.forEach?");
        }

        @Override
        public void configure(Consumer<MethodConfig<?>> consumer) {
            Set<Class<?>> requiredExactClasses = new HashSet<>();
            Set<Class<?>> requiredSuperclasses = new HashSet<>();
            Set<Class<? extends Annotation>> requiredAnnotations = new HashSet<>();

            for (ClassConfigQueryImpl classQuery : classQueries) {
                requiredExactClasses.addAll(classQuery.requiredExactClasses);
                requiredSuperclasses.addAll(classQuery.requiredSuperclasses);
                requiredAnnotations.addAll(classQuery.requiredAnnotations);
            }

            Consumer<javax.enterprise.inject.spi.ProcessAnnotatedType<?>> configurator = pat -> {
                if (constructors) {
                    for (javax.enterprise.inject.spi.configurator.AnnotatedConstructorConfigurator<?> constructorConfigurator : pat.configureAnnotatedType().constructors()) {
                        consumer.accept(new MethodConstructorConfigImpl(constructorConfigurator));
                    }
                } else {
                    for (javax.enterprise.inject.spi.configurator.AnnotatedMethodConfigurator<?> methodConfigurator : pat.configureAnnotatedType().methods()) {
                        consumer.accept(new MethodConfigImpl(methodConfigurator));
                    }
                }
            };
            acceptor.accept(new EnhancementAction(requiredExactClasses, requiredSuperclasses, requiredAnnotations, configurator));
        }
    }

    private class FieldConfigQueryImpl implements FieldConfigQuery {
        private final List<ClassConfigQueryImpl> classQueries = new ArrayList<>();
        private final Set<java.lang.reflect.Type> requiredTypes = new HashSet<>();
        private final Set<Class<? extends Annotation>> requiredAnnotations = new HashSet<>();

        @Override
        public FieldConfigQuery declaredOn(ClassQuery classes) {
            classQueries.add((ClassConfigQueryImpl) classes);
            return this;
        }

        @Override
        public FieldConfigQuery ofType(Class<?> type) {
            requiredTypes.add(type);
            return this;
        }

        @Override
        public FieldConfigQuery ofType(Type type) {
            requiredTypes.add(((TypeImpl<?>) type).reflectionType.getType());
            return this;
        }

        @Override
        public FieldConfigQuery annotatedWith(Class<? extends Annotation> annotationType) {
            requiredAnnotations.add(annotationType);
            return this;
        }

        @Override
        public void forEach(Consumer<FieldInfo<?>> consumer) {
            throw new UnsupportedOperationException("Perhaps somehow remove *ConfigQuery.forEach?");
        }

        @Override
        public void configure(Consumer<FieldConfig<?>> consumer) {
            Set<Class<?>> requiredExactClasses = new HashSet<>();
            Set<Class<?>> requiredSuperclasses = new HashSet<>();
            Set<Class<? extends Annotation>> requiredAnnotations = new HashSet<>();

            for (ClassConfigQueryImpl classQuery : classQueries) {
                requiredExactClasses.addAll(classQuery.requiredExactClasses);
                requiredSuperclasses.addAll(classQuery.requiredSuperclasses);
                requiredAnnotations.addAll(classQuery.requiredAnnotations);
            }

            Consumer<javax.enterprise.inject.spi.ProcessAnnotatedType<?>> configurator = pat -> {
                for (javax.enterprise.inject.spi.configurator.AnnotatedFieldConfigurator<?> fieldConfigurator : pat.configureAnnotatedType().fields()) {
                    consumer.accept(new FieldConfigImpl(fieldConfigurator));
                }
            };
            acceptor.accept(new EnhancementAction(requiredExactClasses, requiredSuperclasses, requiredAnnotations, configurator));
        }
    }
}
