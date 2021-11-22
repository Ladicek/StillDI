package stilldi.test;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserver;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.EventContext;
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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses({SyntheticObserverTest.MyService.class})
@UseBuildCompatibleExtension(SyntheticObserverTest.MyExtension.class)
public class SyntheticObserverTest {
    @Test
    public void test() {
        MyService myService = CDI.current().select(MyService.class).get();
        myService.fireEvent();

        // expects 3 items:
        // - Hello World: unqualified event observed by unqualified observer
        // - Hello @MyQualifier SynObserver: qualified event observed by qualified observer
        // - Hello @MyQualifier SynObserver: qualified event observed by unqualified observer
        assertEquals(3, MyObserver.observed.size());
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Synthesis
        public void synthesise(SyntheticComponents syn) {
            syn.addObserver(MyEvent.class)
                    .observeWith(MyObserver.class);

            syn.addObserver(MyEvent.class)
                    .qualifier(MyQualifier.class)
                    .observeWith(MyObserver.class);
        }
    }

    // ---

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MyQualifier {
    }

    public static class MyEvent {
        final String payload;

        MyEvent(String payload) {
            this.payload = payload;
        }
    }

    @Dependent
    public static class MyService {
        @Inject
        Event<MyEvent> unqualifiedEvent;

        @Inject
        @MyQualifier
        Event<MyEvent> qualifiedEvent;

        void fireEvent() {
            unqualifiedEvent.fire(new MyEvent("Hello World"));
            qualifiedEvent.fire(new MyEvent("Hello @MyQualifier SynObserver"));
        }
    }

    public static class MyObserver implements SyntheticObserver<MyEvent> {
        static final List<String> observed = new ArrayList<>();

        @Override
        public void observe(EventContext<MyEvent> event, Parameters params) {
            String payload = event.getEvent().payload;

            System.out.println("observed " + payload);
            observed.add(payload);
        }
    }
}
