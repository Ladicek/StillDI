package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;
import cdi.lite.extension.model.AnnotationInfo;
import cdi.lite.extension.model.declarations.ClassInfo;
import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.phases.synthesis.SyntheticObserver;
import cdi.lite.extension.phases.synthesis.SyntheticObserverBuilder;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

class SyntheticObserverBuilderImpl implements SyntheticObserverBuilder {
    Class<?> declaringClass;
    java.lang.reflect.Type type;
    Set<Annotation> qualifiers = new HashSet<>();
    int priority = javax.enterprise.inject.spi.ObserverMethod.DEFAULT_PRIORITY;
    boolean isAsync;
    Reception reception = Reception.ALWAYS;
    TransactionPhase transactionPhase = TransactionPhase.IN_PROGRESS;
    Class<? extends SyntheticObserver<?>> implementationClass;

    SyntheticObserverBuilderImpl(Class<?> extensionClass) {
        this.declaringClass = extensionClass;
    }

    @Override
    public SyntheticObserverBuilder declaringClass(Class<?> declaringClass) {
        this.declaringClass = declaringClass;
        return this;
    }

    @Override
    public SyntheticObserverBuilder declaringClass(ClassInfo<?> declaringClass) {
        this.declaringClass = ((ClassInfoImpl) declaringClass).cdiDeclaration.getJavaClass();
        return this;
    }

    @Override
    public SyntheticObserverBuilder type(Class<?> type) {
        this.type = type;
        return this;
    }

    @Override
    public SyntheticObserverBuilder type(ClassInfo<?> type) {
        this.type = ((ClassInfoImpl) type).cdiDeclaration.getJavaClass();
        return this;
    }

    @Override
    public SyntheticObserverBuilder type(Type type) {
        this.type = ((TypeImpl<?>) type).reflectionType.getType();
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(Class<? extends Annotation> qualifierAnnotation, AnnotationAttribute... attributes) {
        this.qualifiers.add(AnnotationProxy.create(qualifierAnnotation, attributes));
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(ClassInfo<?> qualifierAnnotation, AnnotationAttribute... attributes) {
        Class<? extends Annotation> clazz = (Class<? extends Annotation>) ((ClassInfoImpl) qualifierAnnotation).cdiDeclaration.getJavaClass();
        this.qualifiers.add(AnnotationProxy.create(clazz, attributes));
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(AnnotationInfo qualifierAnnotation) {
        this.qualifiers.add(((AnnotationInfoImpl) qualifierAnnotation).annotation);
        return this;
    }

    @Override
    public SyntheticObserverBuilder qualifier(Annotation qualifierAnnotation) {
        this.qualifiers.add(qualifierAnnotation);
        return this;
    }

    @Override
    public SyntheticObserverBuilder priority(int priority) {
        this.priority = priority;
        return this;
    }

    @Override
    public SyntheticObserverBuilder async(boolean isAsync) {
        this.isAsync = isAsync;
        return this;
    }

    @Override
    public SyntheticObserverBuilder reception(Reception reception) {
        this.reception = reception;
        return this;
    }

    @Override
    public SyntheticObserverBuilder transactionPhase(TransactionPhase transactionPhase) {
        this.transactionPhase = transactionPhase;
        return this;
    }

    @Override
    public SyntheticObserverBuilder observeWith(Class<? extends SyntheticObserver<?>> syntheticObserverClass) {
        this.implementationClass = syntheticObserverClass;
        return this;
    }
}
