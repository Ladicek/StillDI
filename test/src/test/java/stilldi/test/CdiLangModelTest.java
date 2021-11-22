package stilldi.test;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import jakarta.enterprise.inject.build.compatible.spi.Enhancement;
import jakarta.enterprise.lang.model.declarations.ClassInfo;
import org.jboss.cdi.lang.model.tck.LangModelVerifier;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import stilldi.impl.StillDI;
import stilldi.test.util.UseBuildCompatibleExtension;

import java.io.IOException;
import java.lang.annotation.Annotation;

@EnableAutoWeld
@AddExtensions({StillDI.class})
@AddBeanClasses(LangModelVerifier.class)
@UseBuildCompatibleExtension(CdiLangModelTest.LangModelVerifierExtension.class)
public class CdiLangModelTest {
    @Test
    public void test() throws IOException {
    }

    public static class LangModelVerifierExtension implements BuildCompatibleExtension {
        @Enhancement(types = LangModelVerifier.class, withAnnotations = Annotation.class)
        public void run(ClassInfo clazz) {
            LangModelVerifier.verify(clazz);
        }
    }
}
