package stilldi.impl;

import cdi.lite.extension.model.AnnotationAttribute;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

final class AnnotationProxy {
    static <T extends Annotation> T create(Class<T> clazz, AnnotationAttribute... attributes) {
        return create(clazz, Arrays.asList(attributes));
    }

    static <T extends Annotation> T create(Class<T> clazz, Collection<AnnotationAttribute> attributes) {
        Class<?>[] interfaces = new Class[]{clazz};
        Map<String, Object> values = new HashMap<>();
        for (AnnotationAttribute attribute : attributes) {
            values.put(attribute.name(), ((AnnotationAttributeImpl) attribute).value.value);
        }
        return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces,
                new AnnotationInvocationHandler(clazz, values));
    }

    private static final class AnnotationInvocationHandler implements InvocationHandler {
        private final Class<? extends Annotation> clazz;
        private final Map<String, Object> attributes;

        AnnotationInvocationHandler(Class<? extends Annotation> clazz, Map<String, Object> attributes) {
            this.clazz = clazz;
            this.attributes = attributes;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Exception {
            if ("annotationType".equals(method.getName())) {
                return clazz;
            } else if ("toString".equals(method.getName())) {
                StringJoiner joiner = new StringJoiner(", ", "(", ")");
                for (Map.Entry<String, Object> attribute : attributes.entrySet()) {
                    joiner.add(attribute.getKey() + "=" + attribute.getValue());
                }
                return "@" + clazz.getName() + joiner.toString();
            } else if ("equals".equals(method.getName())) {
                Object other = args[0];
                if (other instanceof Annotation) {
                    Annotation that = (Annotation) other;
                    if (clazz.equals(that.annotationType())) {
                        for (Method member : clazz.getDeclaredMethods()) {
                            Object thisValue = attributes.get(member.getName());
                            Object thatValue = method.invoke(that);
                            if (!Objects.deepEquals(thisValue, thatValue)) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
                return false;
            } else if ("hashCode".equals(method.getName())) {
                Object[] components = new Object[attributes.size() + 1];
                components[0] = clazz;
                int i = 1;
                for (Object attributeValue : attributes.values()) {
                    components[i++] = attributeValue;
                }
                return Objects.hash(components);
            } else {
                return attributes.get(method.getName());
            }
        }
    }
}
