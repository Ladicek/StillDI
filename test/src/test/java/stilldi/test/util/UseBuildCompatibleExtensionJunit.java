package stilldi.test.util;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;

final class UseBuildCompatibleExtensionJunit implements BeforeAllCallback, AfterAllCallback {
    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        String className = context.getElement()
                .map(it -> it.getAnnotation(UseBuildCompatibleExtension.class))
                .map(it -> it.value().getName())
                .orElseThrow(() -> new RuntimeException("Missing UseBuildCompatibleExtension"));

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        URLClassLoader newClassloader = new URLClassLoader(new URL[]{
                new URL("memory", null, 0, "/", new InMemoryStreamHandler(className))
        }, tccl);
        Thread.currentThread().setContextClassLoader(newClassloader);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(tccl.getParent());
    }

    private static final class InMemoryStreamHandler extends URLStreamHandler {
        private final byte[] content;

        InMemoryStreamHandler(String content) {
            this.content = content.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        protected URLConnection openConnection(URL url) {
            if (!url.getFile().equals("/META-INF/services/cdi.lite.extension.BuildCompatibleExtension")) {
                return null;
            }

            return new URLConnection(url) {
                @Override
                public void connect() {
                }

                @Override
                public InputStream getInputStream() {
                    return new ByteArrayInputStream(content);
                }
            };
        }
    }
}
