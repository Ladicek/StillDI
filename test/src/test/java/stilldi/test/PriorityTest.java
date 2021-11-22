package stilldi.test;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@UseBuildCompatibleExtension(PriorityTest.MyExtension.class)
public class PriorityTest {
    @Test
    public void trigger() {
        assertIterableEquals(Arrays.asList("1", "2", "3", "4", "5"), MyExtension.invocations);
    }

    public static class MyExtension implements BuildCompatibleExtension {
        private static final Set<String> invocations = new HashSet<>();

        @Discovery
        @Priority(10)
        public void first() {
            invocations.add("1");
        }

        @Discovery
        @Priority(20)
        public void second() {
            invocations.add("2");
        }

        @Enhancement(types = Object.class, withSubtypes = true)
        @Priority(15)
        public void third(ClassInfo ignored) {
            invocations.add("3");
        }

        @Validation
        public void fourth() {
            invocations.add("4");
        }

        @Validation
        @Priority(100_000)
        public void fifth() {
            invocations.add("5");
        }
    }
}
