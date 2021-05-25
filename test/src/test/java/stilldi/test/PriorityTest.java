package stilldi.test;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Discovery;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.inject.build.compatible.spi.ExtensionPriority;
import jakarta.enterprise.inject.build.compatible.spi.Validation;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        private static final List<String> invocations = new ArrayList<>();

        @Discovery
        @ExtensionPriority(10)
        public void first() {
            invocations.add("1");
        }

        @Discovery
        @ExtensionPriority(20)
        public void second() {
            invocations.add("2");
        }

        @Enhancement
        @ExtensionPriority(15)
        public void third() {
            invocations.add("3");
        }

        @Validation
        public void fourth() {
            invocations.add("4");
        }

        @Validation
        @ExtensionPriority(100_000)
        public void fifth() {
            invocations.add("5");
        }
    }
}
