package stilldi.impl;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.NormalScope;
import jakarta.enterprise.context.spi.AlterableContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserver;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AfterTypeDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessObserverMethod;
import jakarta.enterprise.inject.spi.ProcessProducerField;
import jakarta.enterprise.inject.spi.ProcessProducerMethod;
import jakarta.enterprise.inject.spi.ProcessSyntheticObserverMethod;
import jakarta.enterprise.inject.spi.configurator.BeanConfigurator;
import jakarta.enterprise.inject.spi.configurator.ObserverMethodConfigurator;
import stilldi.impl.util.impl.specific.CurrentInjectionPoint;
import stilldi.impl.util.impl.specific.SyntheticBeanPriority;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class StillDI implements Extension {
    private final PhaseUtil util = new PhaseUtil();
    private final SharedErrors errors = new SharedErrors();

    private final List<Class<? extends AlterableContext>> contextsToRegister = new ArrayList<>();

    private final List<EnhancementAction> enhancementActions = new ArrayList<>();
    private final List<ProcessingAction> processingActions = new ArrayList<>();

    private final List<Class<?>> allClasses = new ArrayList<>();
    private final List<BeanInfoImpl> allBeans = new ArrayList<>();
    private final List<ObserverInfoImpl> allObservers = new ArrayList<>();

    private final List<jakarta.enterprise.inject.spi.AnnotatedType<?>> allTypes = new ArrayList<>();

    public void afterStartupSupport(@Observes BeforeBeanDiscovery bbd) {
        bbd.addAnnotatedType(AfterStartupSupport.class, AfterStartupSupport.class.getName());
    }

    public void discovery(@Priority(Integer.MAX_VALUE) @Observes BeforeBeanDiscovery bbd, BeanManager bm) throws ClassNotFoundException {
        BeanManagerAccess.set(bm);

        MetaAnnotationsHelper helper = new MetaAnnotationsHelper(bbd);

        PhaseDiscoveryResult discoveryResult = new PhaseDiscovery(util, errors, helper).run();

        for (String additionalClass : discoveryResult.additionalClasses) {
            bbd.addAnnotatedType(Class.forName(additionalClass), additionalClass);
        }

        // qualifiers and interceptor bindings are handled automatically through BBD
        for (MetaAnnotationsHelper.StereotypeConfigurator<?> stereotype : discoveryResult.stereotypes) {
            bbd.addStereotype(stereotype.annotation, stereotype.annotations.toArray(new Annotation[0]));
        }

        for (ContextBuilderImpl context : discoveryResult.contexts) {
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

            bbd.addScope(scopeAnnotation, isNormal, isPassivating);

            Class<? extends AlterableContext> contextClass = context.implementationClass;
            contextsToRegister.add(contextClass);
        }

        PhaseEnhancementResult enhancementResult = new PhaseEnhancement(util, errors).run();
        enhancementActions.addAll(enhancementResult.actions);

        BeanManagerAccess.remove();
    }

    public void enhancement(@Priority(Integer.MAX_VALUE) @Observes ProcessAnnotatedType<?> pat, BeanManager bm) {
        BeanManagerAccess.set(bm);

        allClasses.add(pat.getAnnotatedType().getJavaClass());

        for (EnhancementAction enhancementAction : enhancementActions) {
            enhancementAction.run(pat);
        }

        BeanManagerAccess.remove();
    }

    public void processing(@Priority(Integer.MAX_VALUE) @Observes AfterTypeDiscovery atd, BeanManager bm) {
        BeanManagerAccess.set(bm);

        PhaseProcessingResult processingResult = new PhaseProcessing(util, errors).run();
        processingActions.addAll(processingResult.actions);

        BeanManagerAccess.remove();
    }

    public void collectBeans(@Priority(Integer.MAX_VALUE) @Observes ProcessBean<?> pb, BeanManager bm) {
        BeanManagerAccess.set(bm);

        jakarta.enterprise.inject.spi.Annotated declaration = pb.getAnnotated();
        if (pb instanceof jakarta.enterprise.inject.spi.ProcessSyntheticBean) {
            declaration = null;
        } else {
            for (ProcessingAction processingAction : processingActions) {
                processingAction.run(pb);
            }
        }

        jakarta.enterprise.inject.spi.AnnotatedParameter<?> disposer = null;
        if (pb instanceof ProcessProducerField) {
            disposer = ((ProcessProducerField<?, ?>) pb).getAnnotatedDisposedParameter();
        } else if (pb instanceof ProcessProducerMethod) {
            disposer = ((ProcessProducerMethod<?, ?>) pb).getAnnotatedDisposedParameter();
        }
        allBeans.add(new BeanInfoImpl(pb.getBean(), declaration, disposer));

        BeanManagerAccess.remove();
    }

    public void collectObservers(@Priority(Integer.MAX_VALUE) @Observes ProcessObserverMethod<?, ?> pom, BeanManager bm) {
        BeanManagerAccess.set(bm);

        jakarta.enterprise.inject.spi.AnnotatedMethod<?> declaration = pom.getAnnotatedMethod();
        if (pom instanceof ProcessSyntheticObserverMethod) {
            declaration = null;
        } else {
            for (ProcessingAction processingAction : processingActions) {
                processingAction.run(pom);
            }
        }

        allObservers.add(new ObserverInfoImpl(pom.getObserverMethod(), declaration));

        BeanManagerAccess.remove();
    }

    public void synthesis(@Priority(Integer.MAX_VALUE) @Observes AfterBeanDiscovery abd, BeanManager bm) throws IllegalAccessException, InstantiationException {
        BeanManagerAccess.set(bm);

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

        PhaseSynthesisResult synthesisResult = new PhaseSynthesis(util, allBeans, allObservers, allTypes, errors).run();

        for (SyntheticBeanBuilderImpl<?> syntheticBean : synthesisResult.syntheticBeans) {
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
            if (syntheticBean.disposerClass != null) {
                configurator.destroyWith((object, creationalContext) -> {
                    try {
                        SyntheticBeanDisposer disposer = syntheticBean.disposerClass.newInstance();
                        disposer.dispose(object, creationalContext, syntheticBean.params);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        for (SyntheticObserverBuilderImpl syntheticObserver : synthesisResult.syntheticObservers) {
            ObserverMethodConfigurator<Object> configurator = abd.addObserverMethod();
            configurator.beanClass(syntheticObserver.declaringClass);
            configurator.observedType(syntheticObserver.type);
            configurator.qualifiers(syntheticObserver.qualifiers);
            configurator.priority(syntheticObserver.priority);
            configurator.async(syntheticObserver.isAsync);
            configurator.reception(syntheticObserver.reception);
            configurator.transactionPhase(syntheticObserver.transactionPhase);
            configurator.notifyWith(eventContext -> {
                SyntheticObserver observer = syntheticObserver.implementationClass.newInstance();
                observer.observe(eventContext);
            });
        }

        BeanManagerAccess.remove();
    }

    public void validation(@Priority(Integer.MAX_VALUE) @Observes AfterDeploymentValidation adv, BeanManager bm) {
        BeanManagerAccess.set(bm);

        new PhaseValidation(util, allBeans, allObservers, allTypes, errors).run();

        for (Throwable error : errors.list) {
            adv.addDeploymentProblem(error);
        }

        // cleanup
        util.clear();
        errors.list.clear();

        contextsToRegister.clear();
        enhancementActions.clear();
        processingActions.clear();
        allClasses.clear();
        allBeans.clear();
        allObservers.clear();
        allTypes.clear();

        BeanManagerAccess.remove();
    }
}
