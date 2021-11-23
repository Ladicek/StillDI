package stilldi.test;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.event.Event;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Parameters;
import jakarta.enterprise.inject.build.compatible.spi.Synthesis;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticComponents;
import jakarta.enterprise.inject.build.compatible.spi.SyntheticObserver;
import jakarta.enterprise.inject.build.compatible.spi.Types;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.EventContext;
import jakarta.inject.Inject;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses({SyntheticObserverOfParameterizedTypeTest.MyService.class})
@UseBuildCompatibleExtension(SyntheticObserverOfParameterizedTypeTest.MyExtension.class)
public class SyntheticObserverOfParameterizedTypeTest {
    @Test
    public void test() {
        MyService myService = CDI.current().select(MyService.class).get();
        myService.fireEvent();

        assertEquals(Arrays.asList("Hello World", "Hello again"), MyObserver.observed);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        @Synthesis
        public void synthesise(SyntheticComponents syn, Types types) {
            syn.<List<MyData>>addObserver(types.parameterized(List.class, MyData.class))
                    .observeWith(MyObserver.class);
        }
    }

    // ---

    public static class MyData {
        final String payload;

        MyData(String payload) {
            this.payload = payload;
        }
    }

    @Dependent
    public static class MyService {
        @Inject
        Event<List<MyData>> event;

        void fireEvent() {
            event.fire(Arrays.asList(new MyData("Hello"), new MyData("World")));
            event.fire(Arrays.asList(new MyData("Hello"), new MyData("again")));
        }
    }

    public static class MyObserver implements SyntheticObserver<List<MyData>> {
        static final List<String> observed = new ArrayList<>();

        @Override
        public void observe(EventContext<List<MyData>> event, Parameters params) {
            observed.add(event.getEvent()
                    .stream()
                    .map(it -> it.payload)
                    .collect(Collectors.joining(" ")));
        }
    }
}
