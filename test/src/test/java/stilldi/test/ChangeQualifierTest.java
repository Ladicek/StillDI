package stilldi.test;

import jakarta.enterprise.inject.build.compatible.spi.AppArchive;
import jakarta.enterprise.inject.build.compatible.spi.AppArchiveBuilder;
import jakarta.enterprise.inject.build.compatible.spi.AppDeployment;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ClassConfig;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.ExactType;
import jakarta.enterprise.inject.build.compatible.spi.FieldConfig;
import jakarta.enterprise.inject.build.compatible.spi.Messages;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
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
        public void services(AppArchiveBuilder app, Messages messages) {
            // TODO can't implement addSubtypesOf on top of Portable Extensions?
            app.add(MyFooService.class.getName());
            app.add(MyBarService.class.getName());
            app.add(MyBazService.class.getName());
            messages.info("discovery complete");
        }

        @Enhancement
        @ExactType(type = MyFooService.class, annotatedWith = Singleton.class)
        public void foo(ClassConfig clazz) {
            clazz.removeAnnotation(ann -> ann.name().equals(MyQualifier.class.getName()));
        }

        @Enhancement
        @ExactType(type = MyBarService.class, annotatedWith = Singleton.class)
        public void bar(ClassConfig clazz) {
            clazz.addAnnotation(MyQualifier.class);
        }

        @Enhancement
        @ExactType(type = MyServiceConsumer.class, annotatedWith = Inject.class)
        public void service(FieldConfig field) {
            if ("myService".equals(field.name())) {
                field.addAnnotation(MyQualifier.class);
            }
        }

        @Validation
        public void validate(AppArchive archive, AppDeployment deployment, Messages messages) {
            archive.classes().subtypeOf(MyService.class).forEach(clazz -> {
                messages.info("class has annotations " + clazz.annotations(), clazz);
            });

            deployment.beans().type(MyService.class).forEach(bean -> {
                messages.info("got bean", bean);
            });
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
    @MyQualifier
    public static class MyFooService implements MyService {
        private final String value = "foo";

        @Override
        public String hello() {
            return value;
        }
    }

    @Singleton
    public static class MyBarService implements MyService {
        private static final String VALUE = "bar";

        @Override
        public String hello() {
            return VALUE;
        }
    }

    @Singleton
    public static class MyBazService implements MyService {
        @Override
        public String hello() {
            throw new UnsupportedOperationException();
        }
    }

    @Singleton
    public static class MyServiceConsumer {
        @Inject
        MyService myService;
    }
}
