package stilldi.test;

import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.Messages;
import cdi.lite.extension.phases.Discovery;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@UseBuildCompatibleExtension(HelloWorldTest.HelloWorldExtension.class)
public class HelloWorldTest {
    @Test
    public void trigger() {
    }

    public static class HelloWorldExtension implements BuildCompatibleExtension {
        @Discovery
        public void hello(Messages msg) {
            msg.info("Hello, world!");
        }
    }
}
