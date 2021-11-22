package stilldi.test;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.ScannedClasses;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Qualifier;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses({ChangeQualifierTest.MyService.class, ChangeQualifierTest.MyServiceConsumer.class})
@UseBuildCompatibleExtension(ChangeQualifierTest.MyExtension.class)
public class ChangeQualifierTest {
    @Test
    public void test() {
        MyServiceConsumer myServiceConsumer = CDI.current().select(MyServiceConsumer.class).get();
        assertTrue(myServiceConsumer.myService instanceof MyBarService);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Discovery
        public void services(ScannedClasses scan, Messages messages) {
            scan.add(MyFooService.class.getName());
            scan.add(MyBarService.class.getName());
            scan.add(MyBazService.class.getName());
            messages.info("discovery complete");
        }

        @Enhancement(types = ChangeQualifierTest.MyFooService.class)
        public void foo(ClassConfig clazz) {
            clazz.removeAnnotation(ann -> ann.name().equals(MyQualifier.class.getName()));
        }

        @Enhancement(types = ChangeQualifierTest.MyBarService.class)
        public void bar(ClassConfig clazz) {
            clazz.addAnnotation(MyQualifier.class);
        }

        @Enhancement(types = ChangeQualifierTest.MyServiceConsumer.class)
        public void service(FieldConfig field) {
            if ("myService".equals(field.info().name())) {
                field.addAnnotation(MyQualifier.class);
            }
        }
    }

    // ---

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyQualifier {
    }

    public interface MyService {
        String hello();
    }

    @Dependent
    @MyQualifier
    public static class MyFooService implements MyService {
        private final String value = "foo";

        @Override
        public String hello() {
            return value;
        }
    }

    @Dependent
    public static class MyBarService implements MyService {
        private static final String VALUE = "bar";

        @Override
        public String hello() {
            return VALUE;
        }
    }

    @Dependent
    public static class MyBazService implements MyService {
        @Override
        public String hello() {
            throw new UnsupportedOperationException();
        }
    }

    @Dependent
    public static class MyServiceConsumer {
        @Inject
        MyService myService;
    }
}
