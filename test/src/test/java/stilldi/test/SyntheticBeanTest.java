package stilldi.test;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanCreator;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticBeanDisposer;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
                    .withParam("name", "World")
                    .createWith(MyPojoCreator.class)
                    .disposeWith(MyPojoDisposer.class);

            syn.addBean(MyPojo.class)
                    .type(MyPojo.class)
                    .qualifier(MyQualifier.class)
                    .withParam("name", "SynBean")
                    .createWith(MyPojoCreator.class)
                    .disposeWith(MyPojoDisposer.class);
        }
    }

    // ---

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
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
        public MyPojo create(Instance<Object> lookup, Parameters params) {
            String name = params.get("name", String.class);

            InjectionPoint injectionPoint = lookup.select(InjectionPoint.class).get();
            if (injectionPoint.getQualifiers().stream().anyMatch(it -> it.annotationType().equals(MyQualifier.class))) {
                return new MyPojo("Hello @MyQualifier " + name);
            }

            return new MyPojo("Hello " + name);
        }
    }

    public static class MyPojoDisposer implements SyntheticBeanDisposer<MyPojo> {
        @Override
        public void dispose(MyPojo instance, Instance<Object> lookup, Parameters params) {
            System.out.println("disposing " + instance.data);
        }
    }
}
