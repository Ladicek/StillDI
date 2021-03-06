package stilldi.test;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.ExactType;
import jakarta.enterprise.inject.spi.CDI;
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

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses({ChangeFieldThroughClassTest.MyService.class, ChangeFieldThroughClassTest.MyFooService.class,
        ChangeFieldThroughClassTest.MyBarService.class, ChangeFieldThroughClassTest.MyServiceConsumer.class})
@UseBuildCompatibleExtension(ChangeFieldThroughClassTest.MyExtension.class)
public class ChangeFieldThroughClassTest {
    @Test
    public void test() {
        MyServiceConsumer myServiceConsumer = CDI.current().select(MyServiceConsumer.class).get();
        assertTrue(myServiceConsumer.myService instanceof MyBarService);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Enhancement
        @ExactType(type = MyServiceConsumer.class, annotatedWith = Singleton.class)
        public void service(ClassConfig<?> clazz) {
            clazz.fields()
                    .stream()
                    .filter(it -> it.name().equals("myService"))
                    .forEach(field -> field.addAnnotation(MyQualifier.class));
        }
    }

    // ---

    @Qualifier
    @Retention(RUNTIME)
    public @interface MyQualifier {
    }

    public interface MyService {
        String hello();
    }

    @Singleton
    public static class MyFooService implements MyService {
        @Override
        public String hello() {
            return "foo";
        }
    }

    @Singleton
    @MyQualifier
    public static class MyBarService implements MyService {
        @Override
        public String hello() {
            return "bar";
        }
    }

    @Singleton
    public static class MyServiceConsumer {
        @Inject
        MyService myService;
    }
}
