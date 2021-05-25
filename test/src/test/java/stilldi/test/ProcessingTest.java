package stilldi.test;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.build.compatible.spi.BeanInfo;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.ExactType;
import jakarta.enterprise.inject.build.compatible.spi.ObserverInfo;
import jakarta.enterprise.inject.build.compatible.spi.Processing;
import jakarta.enterprise.inject.build.compatible.spi.SubtypesOf;
import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.lang.annotation.Retention;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses({ProcessingTest.MyService.class, ProcessingTest.MyFooService.class, ProcessingTest.MyBarServiceProducer.class})
@UseBuildCompatibleExtension(ProcessingTest.MyExtension.class)
public class ProcessingTest {
    @Test
    public void test() {
        assertEquals(2, MyExtension.beanCounter.get());
        assertEquals(1, MyExtension.beanMyQualifierCounter.get());
        assertEquals(1, MyExtension.observerQualifierCounter.get());
    }

    public static class MyExtension implements BuildCompatibleExtension {
        static final AtomicInteger beanCounter = new AtomicInteger();
        static final AtomicInteger beanMyQualifierCounter = new AtomicInteger();
        static final AtomicInteger observerQualifierCounter = new AtomicInteger();

        @Processing
        @SubtypesOf(type = MyService.class)
        public void processBean(BeanInfo<?> bean) {
            beanCounter.incrementAndGet();

            if (bean.qualifiers().stream().anyMatch(it -> it.name().equals(MyQualifier.class.getName()))) {
                beanMyQualifierCounter.incrementAndGet();
            }
        }

        @Processing
        @ExactType(type = Object.class)
        public void processObserver(ObserverInfo<?> observer, Types types) {
            if (observer.declaringClass().superInterfaces().contains(types.of(MyService.class))) {
                observerQualifierCounter.addAndGet(observer.qualifiers().size());
            }
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

        void init(@Observes @Initialized(ApplicationScoped.class) Object event) {
        }
    }

    // intentionally not a bean, to test that producer-based bean is processed
    public static class MyBarService implements MyService {
        @Override
        public String hello() {
            return "bar";
        }
    }

    @Singleton
    public static class MyBarServiceProducer {
        @Produces
        @Singleton
        @MyQualifier
        // must _not_ return `MyService`, because `@SubtypesOf` wouldn't catch that
        // (we currently pretend that subtyping isn't reflexive)
        public MyBarService produce() {
            return new MyBarService();
        }
    }
}
