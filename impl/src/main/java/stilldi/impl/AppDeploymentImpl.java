package stilldi.impl;

import jakarta.enterprise.inject.build.compatible.spi.AppDeployment;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import jakarta.enterprise.lang.model.types.Type;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

class AppDeploymentImpl implements AppDeployment {
    private final Collection<BeanInfoImpl> beans;
    private final Collection<ObserverInfoImpl> observers;

    AppDeploymentImpl(Collection<BeanInfoImpl> beans, Collection<ObserverInfoImpl> observers) {
        this.beans = beans;
        this.observers = observers;
    }

    @Override
    public BeanQuery beans() {
        return new BeanQueryImpl();
    }

    @Override
    public ObserverQuery observers() {
        return new ObserverQueryImpl();
    }

    class BeanQueryImpl implements BeanQuery {
        private Set<Class<? extends Annotation>> requiredScopeAnnotations;
        private Set<java.lang.reflect.Type> requiredBeanTypes;
        private Set<Class<? extends Annotation>> requiredQualifiers;
        private Set<Class<?>> requiredDeclaringClasses;

        @Override
        public BeanQuery scope(Class<? extends Annotation> scopeAnnotation) {
            if (requiredScopeAnnotations == null) {
                requiredScopeAnnotations = new HashSet<>();
            }

            requiredScopeAnnotations.add(scopeAnnotation);
            return this;
        }

        @Override
        public BeanQuery scope(ClassInfo<?> scopeAnnotation) {
            if (requiredScopeAnnotations == null) {
                requiredScopeAnnotations = new HashSet<>();
            }

            requiredScopeAnnotations.add((Class<? extends Annotation>) ((ClassInfoImpl) scopeAnnotation).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public BeanQuery type(Class<?> beanType) {
            if (requiredBeanTypes == null) {
                requiredBeanTypes = new HashSet<>();
            }

            requiredBeanTypes.add(beanType);
            return this;
        }

        @Override
        public BeanQuery type(ClassInfo<?> beanType) {
            if (requiredBeanTypes == null) {
                requiredBeanTypes = new HashSet<>();
            }

            requiredBeanTypes.add(((ClassInfoImpl) beanType).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public BeanQuery type(Type beanType) {
            if (requiredBeanTypes == null) {
                requiredBeanTypes = new HashSet<>();
            }

            requiredBeanTypes.add(((TypeImpl<?>) beanType).reflectionType.getType());
            return this;
        }

        @Override
        public BeanQuery qualifier(Class<? extends Annotation> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add(qualifierAnnotation);
            return this;
        }

        @Override
        public BeanQuery qualifier(ClassInfo<?> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add((Class<? extends Annotation>) ((ClassInfoImpl) qualifierAnnotation).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public BeanQuery declaringClass(Class<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(declaringClass);
            return this;
        }

        @Override
        public BeanQuery declaringClass(ClassInfo<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(((ClassInfoImpl) declaringClass).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public void forEach(Consumer<BeanInfo<?>> consumer) {
            stream().forEach(consumer);
        }

        @Override
        public void ifNone(Runnable runnable) {
            if (stream().count() == 0) {
                runnable.run();
            }
        }

        Stream<BeanInfo<?>> stream() {
            Stream<BeanInfoImpl> result = beans.stream();

            if (requiredScopeAnnotations != null) {
                result = result.filter(it -> requiredScopeAnnotations.contains(it.cdiBean.getScope()));
            }

            if (requiredBeanTypes != null) {
                result = result.filter(it -> it.cdiBean.getTypes()
                        .stream()
                        .anyMatch(type -> requiredBeanTypes.contains(type)));
            }

            if (requiredQualifiers != null) {
                result = result.filter(it -> it.cdiBean.getQualifiers()
                        .stream()
                        .map(Annotation::annotationType)
                        .anyMatch(qualifierType -> requiredQualifiers.contains(qualifierType)));
            }

            if (requiredDeclaringClasses != null) {
                result = result.filter(it -> requiredDeclaringClasses.contains(it.cdiBean.getBeanClass()));
            }

            return result.map(Function.identity());
        }
    }

    class ObserverQueryImpl implements ObserverQuery {
        private Set<java.lang.reflect.Type> requiredObservedTypes;
        private Set<Class<? extends Annotation>> requiredQualifiers;
        private Set<Class<?>> requiredDeclaringClasses;

        @Override
        public ObserverQuery observedType(Class<?> observedType) {
            if (requiredObservedTypes == null) {
                requiredObservedTypes = new HashSet<>();
            }

            requiredObservedTypes.add(observedType);
            return this;
        }

        @Override
        public ObserverQuery observedType(ClassInfo<?> observedType) {
            if (requiredObservedTypes == null) {
                requiredObservedTypes = new HashSet<>();
            }

            requiredObservedTypes.add(((ClassInfoImpl) observedType).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public ObserverQuery observedType(Type observedType) {
            if (requiredObservedTypes == null) {
                requiredObservedTypes = new HashSet<>();
            }

            requiredObservedTypes.add(((TypeImpl<?>) observedType).reflectionType.getType());
            return this;
        }

        @Override
        public ObserverQuery qualifier(Class<? extends Annotation> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add(qualifierAnnotation);
            return this;
        }

        @Override
        public ObserverQuery qualifier(ClassInfo<?> qualifierAnnotation) {
            if (requiredQualifiers == null) {
                requiredQualifiers = new HashSet<>();
            }

            requiredQualifiers.add((Class<? extends Annotation>) ((ClassInfoImpl) qualifierAnnotation).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public ObserverQuery declaringClass(Class<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(declaringClass);
            return this;
        }

        @Override
        public ObserverQuery declaringClass(ClassInfo<?> declaringClass) {
            if (requiredDeclaringClasses == null) {
                requiredDeclaringClasses = new HashSet<>();
            }

            requiredDeclaringClasses.add(((ClassInfoImpl) declaringClass).cdiDeclaration.getJavaClass());
            return this;
        }

        @Override
        public void forEach(Consumer<ObserverInfo<?>> consumer) {
            stream().forEach(consumer);
        }

        @Override
        public void ifNone(Runnable runnable) {
            if (stream().count() == 0) {
                runnable.run();
            }
        }

        Stream<ObserverInfo<?>> stream() {
            Stream<ObserverInfoImpl> result = observers.stream();

            if (requiredObservedTypes != null) {
                result = result.filter(it -> requiredObservedTypes.contains(it.cdiObserver.getObservedType()));
            }

            if (requiredQualifiers != null) {
                result = result.filter(it -> it.cdiObserver.getObservedQualifiers()
                        .stream()
                        .map(Annotation::annotationType)
                        .anyMatch(qualifierType -> requiredQualifiers.contains(qualifierType)));
            }

            if (requiredDeclaringClasses != null) {
                result = result.filter(it -> requiredDeclaringClasses.contains(it.cdiObserver.getBeanClass()));
            }

            return result.map(Function.identity());
        }
    }
}
