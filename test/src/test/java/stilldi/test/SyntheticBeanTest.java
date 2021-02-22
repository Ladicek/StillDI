package stilldi.test;

import cdi.lite.extension.AppDeployment;
import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.Messages;
import cdi.lite.extension.phases.Synthesis;
import cdi.lite.extension.phases.Validation;
import cdi.lite.extension.phases.synthesis.SyntheticBeanCreator;
import cdi.lite.extension.phases.synthesis.SyntheticBeanDisposer;
import cdi.lite.extension.phases.synthesis.SyntheticComponents;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;
import java.lang.annotation.Retention;
import java.util.Map;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses({SyntheticBeanTest.MyService.class})
@UseBuildCompatibleExtension(SyntheticBeanTest.MyExtension.class)
public class SyntheticBeanTest {
    @Test
    public void test() {
        MyService myService = CDI.current().select(MyService.class).get();
        assertEquals("Hello World", myService.unqualified.data);
        assertEquals("Hello @MyQualifier SynBean", myService.qualified.data);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Synthesis
        public void synthesise(SyntheticComponents syn) {
            syn.addBean(MyPojo.class)
                    .type(MyPojo.class)
                    .scope(Singleton.class)
                    .withParam("name", "World")
                    .createWith(MyPojoCreator.class)
                    .disposeWith(MyPojoDisposer.class);

            syn.addBean(MyPojo.class)
                    .type(MyPojo.class)
                    .scope(Singleton.class)
                    .qualifier(MyQualifier.class)
                    .withParam("name", "SynBean")
                    .createWith(MyPojoCreator.class)
                    .disposeWith(MyPojoDisposer.class);
        }

        @Validation
        public void validate(AppDeployment deployment, Messages messages) {
            deployment.beans().type(MyPojo.class).forEach(bean -> {
                messages.info("got bean", bean);
            });
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    public @interface MyQualifier {
    }

    @Singleton
    public static class MyService {
        @Inject
        MyPojo unqualified;

        @Inject
        @MyQualifier
        MyPojo qualified;
    }

    public static class MyPojo {
        final String data;

        MyPojo(String data) {
            this.data = data;
        }
    }

    public static class MyPojoCreator implements SyntheticBeanCreator<MyPojo> {
        @Override
        public MyPojo create(CreationalContext<MyPojo> creationalContext, InjectionPoint injectionPoint,
                Map<String, Object> params) {
            String name = (String) params.get("name");

            if (injectionPoint.getQualifiers().stream().anyMatch(it -> it.annotationType().equals(MyQualifier.class))) {
                return new MyPojo("Hello @MyQualifier " + name);
            }

            return new MyPojo("Hello " + name);
        }
    }

    public static class MyPojoDisposer implements SyntheticBeanDisposer<MyPojo> {
        @Override
        public void dispose(MyPojo instance, CreationalContext<MyPojo> creationalContext, Map<String, Object> params) {
            System.out.println("disposing " + instance.data);
        }
    }
}
