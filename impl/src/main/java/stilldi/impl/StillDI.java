package stilldi.impl;

import cdi.lite.extension.phases.synthesis.SyntheticBeanCreator;
import cdi.lite.extension.phases.synthesis.SyntheticBeanDisposer;
import cdi.lite.extension.phases.synthesis.SyntheticObserver;
import stilldi.impl.util.impl.specific.CurrentInjectionPoint;
import stilldi.impl.util.impl.specific.SyntheticBeanPriority;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.AlterableContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.EventContext;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.ProcessProducerField;
import javax.enterprise.inject.spi.ProcessProducerMethod;
import javax.enterprise.inject.spi.ProcessSyntheticObserverMethod;
import javax.enterprise.inject.spi.configurator.BeanConfigurator;
import javax.enterprise.inject.spi.configurator.ObserverMethodConfigurator;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class StillDI implements Extension {
    private final PhaseUtil util = new PhaseUtil();
    private final SharedErrors errors = new SharedErrors();

    private final List<Class<? extends AlterableContext>> contextsToRegister = new ArrayList<>();

    private final List<EnhancementAction> enhancementActions = new ArrayList<>();

    private final List<Class<?>> allClasses = new ArrayList<>();
    private final List<BeanInfoImpl> allBeans = new ArrayList<>();
    private final List<ObserverInfoImpl> allObservers = new ArrayList<>();

    private final List<javax.enterprise.inject.spi.AnnotatedType<?>> allTypes = new ArrayList<>();

    public void discovery(@Priority(Integer.MAX_VALUE) @Observes BeforeBeanDiscovery bdd) throws ClassNotFoundException {
        PhaseDiscovery processor = new PhaseDiscovery(util, errors);
        processor.run();
        for (String additionalClass : processor.additionalClasses) {
            bdd.addAnnotatedType(Class.forName(additionalClass), additionalClass);
        }
        for (ContextBuilderImpl context : processor.contexts) {
            Class<? extends Annotation> scopeAnnotation = context.scopeAnnotation;
            if (scopeAnnotation == null) {
                try {
                    scopeAnnotation = context.implementationClass.newInstance().getScope();
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            }

            boolean isNormal;
            boolean isPassivating;
            if (context.isNormal != null) {
                isNormal = context.isNormal;
                isPassivating = false; // TODO
            } else {
                NormalScope normalScope = scopeAnnotation.getAnnotation(NormalScope.class);
                if (normalScope != null) {
                    isNormal = true;
                    isPassivating = normalScope.passivating();
                } else {
                    isNormal = false;
                    isPassivating = false;
                }
            }


            bdd.addScope(scopeAnnotation, isNormal, isPassivating);

            Class<? extends AlterableContext> contextClass = context.implementationClass;
            contextsToRegister.add(contextClass);
        }

        enhancementActions.addAll(new PhaseEnhancement(util, errors).run());
    }

    public void enhancement(@Priority(Integer.MAX_VALUE) @Observes ProcessAnnotatedType<?> pat) {
        allClasses.add(pat.getAnnotatedType().getJavaClass());

        for (EnhancementAction enhancementAction : enhancementActions) {
            enhancementAction.run(pat);
        }
    }

    public void collectBeans(@Priority(Integer.MAX_VALUE) @Observes ProcessBean<?> pb) {
        javax.enterprise.inject.spi.Annotated declaration = pb.getAnnotated();
        if (pb instanceof javax.enterprise.inject.spi.ProcessSyntheticBean) {
            declaration = null;
        }

        javax.enterprise.inject.spi.AnnotatedParameter<?> disposer = null;
        if (pb instanceof ProcessProducerField) {
            disposer = ((ProcessProducerField<?, ?>) pb).getAnnotatedDisposedParameter();
        } else if (pb instanceof ProcessProducerMethod) {
            disposer = ((ProcessProducerMethod<?, ?>) pb).getAnnotatedDisposedParameter();
        }
        allBeans.add(new BeanInfoImpl(pb.getBean(), declaration, disposer));
    }

    public void collectObservers(@Priority(Integer.MAX_VALUE) @Observes ProcessObserverMethod<?, ?> pom) {
        javax.enterprise.inject.spi.AnnotatedMethod<?> declaration = pom.getAnnotatedMethod();
        if (pom instanceof ProcessSyntheticObserverMethod) {
            declaration = null;
        }

        allObservers.add(new ObserverInfoImpl(pom.getObserverMethod(), declaration));
    }

    public void synthesis(@Priority(Integer.MAX_VALUE) @Observes AfterBeanDiscovery abd) throws IllegalAccessException, InstantiationException {
        // when synthetic components are created, the corresponding ProcessSynthetic* event is fired and hence
        // the corresponding collect* method is called, which results in modifying allBeans/allObservers
        List<BeanInfoImpl> allBeans = new ArrayList<>(this.allBeans);
        List<ObserverInfoImpl> allObservers = new ArrayList<>(this.allObservers);

        for (Class<? extends AlterableContext> contextClass : contextsToRegister) {
            abd.addContext(contextClass.newInstance());
        }

        allClasses.stream()
                .flatMap(it -> StreamSupport.stream(abd.getAnnotatedTypes(it).spliterator(), false))
                .forEach(allTypes::add);

        PhaseSynthesis phase = new PhaseSynthesis(util, allBeans, allObservers, allTypes, errors);
        phase.run();

        for (SyntheticBeanBuilderImpl<?> syntheticBean : phase.syntheticBeans) {
            BeanConfigurator<Object> configurator = abd.addBean();
            configurator.beanClass(syntheticBean.implementationClass);
            configurator.types(syntheticBean.types);
            configurator.qualifiers(syntheticBean.qualifiers);
            if (syntheticBean.scope != null) {
                configurator.scope(syntheticBean.scope);
            }
            configurator.alternative(syntheticBean.isAlternative);
            SyntheticBeanPriority.set(configurator, syntheticBean.priority);
            configurator.name(syntheticBean.name);
            configurator.stereotypes(syntheticBean.stereotypes);
            // TODO can't really know if the scope is @Dependent, because there may be a stereotype with default scope
            //  but this will have to do for now
            boolean isDependent = syntheticBean.scope == null || Dependent.class.equals(syntheticBean.scope);
            configurator.createWith(creationalContext -> {
                try {
                    SyntheticBeanCreator creator = syntheticBean.creatorClass.newInstance();
                    InjectionPoint injectionPoint = null;
                    if (isDependent) {
                        injectionPoint = CurrentInjectionPoint.get();
                    }
                    return creator.create(creationalContext, injectionPoint, syntheticBean.params);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
            configurator.destroyWith((object, creationalContext) -> {
                try {
                    SyntheticBeanDisposer disposer = syntheticBean.disposerClass.newInstance();
                    disposer.dispose(object, creationalContext, syntheticBean.params);
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        for (SyntheticObserverBuilderImpl syntheticObserver : phase.syntheticObservers) {
            ObserverMethodConfigurator<Object> configurator = abd.addObserverMethod();
            configurator.beanClass(syntheticObserver.declaringClass);
            configurator.observedType(syntheticObserver.type);
            configurator.qualifiers(syntheticObserver.qualifiers);
            configurator.priority(syntheticObserver.priority);
            configurator.async(syntheticObserver.isAsync);
            configurator.reception(syntheticObserver.reception);
            configurator.transactionPhase(syntheticObserver.transactionPhase);
            configurator.notifyWith(eventContext -> {
                SyntheticObserver<?> observer = syntheticObserver.implementationClass.newInstance();
                observer.observe((EventContext) eventContext);
            });
        }
    }

    public void validation(@Priority(Integer.MAX_VALUE) @Observes AfterDeploymentValidation adv) {
        new PhaseValidation(util, allBeans, allObservers, allTypes, errors).run();

        for (Throwable error : errors.list) {
            adv.addDeploymentProblem(error);
        }

        // cleanup
        util.clear();
        errors.list.clear();

        contextsToRegister.clear();
        enhancementActions.clear();
        allClasses.clear();
        allBeans.clear();
        allObservers.clear();
        allTypes.clear();
    }
}
