package stilldi.test;

import cdi.lite.extension.BuildCompatibleExtension;
import cdi.lite.extension.SkipIfPortableExtensionPresent;
import cdi.lite.extension.phases.Discovery;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnableAutoWeld
@AddExtensions({StillDI.class, SkipTest.MyPortableExtension.class})
@UseBuildCompatibleExtension(SkipTest.MyBuildCompatibleExtension.class)
public class SkipTest {
    @Test
    public void trigger() {
        assertTrue(MyPortableExtension.invoked);
        assertFalse(MyBuildCompatibleExtension.invoked);
    }

    public static class MyPortableExtension implements Extension {
        static boolean invoked = false;

        public void hello(@Observes BeforeBeanDiscovery bbd) {
            invoked = true;
        }

    }

    @SkipIfPortableExtensionPresent("stilldi.test.SkipTest$MyPortableExtension")
    public static class MyBuildCompatibleExtension implements BuildCompatibleExtension {
        static boolean invoked = false;

        @Discovery
        public void hello() {
            invoked = true;
        }
    }
}
