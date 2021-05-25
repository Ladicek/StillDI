package stilldi.test.util;

import jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(UseBuildCompatibleExtensionJunit.class)
public @interface UseBuildCompatibleExtension {
    Class<? extends BuildCompatibleExtension> value();
}
