package cdi.lite.extension.model.declarations;

import cdi.lite.extension.model.types.Type;
import cdi.lite.extension.model.types.TypeVariable;
import java.util.Collection;
import java.util.List;

/**
 * @param <T> the inspected class
 */
public interface ClassInfo<T> extends DeclarationInfo {
    String name();

    String simpleName();

    PackageInfo packageInfo();

    // empty if class is not parameterized
    List<TypeVariable> typeParameters();

    // null if this class doesn't have a superclass (e.g. is Object or an interface)
    Type superClass();

    // null if this class doesn't have a superclass (e.g. is Object or an interface)
    ClassInfo<?> superClassDeclaration();

    // empty if the class has no superinterfaces
    List<Type> superInterfaces();

    // empty if the class has no superinterfaces
    List<ClassInfo<?>> superInterfacesDeclarations();

    boolean isPlainClass();

    boolean isInterface();

    boolean isEnum();

    boolean isAnnotation();

    boolean isAbstract();

    boolean isFinal();

    int modifiers();

    // only constructors declared by this class, not inherited ones
    // no static initializers
    Collection<? extends MethodInfo<T>> constructors();

    // only methods declared by this class, not inherited ones
    // no constructors nor static initializers
    Collection<? extends MethodInfo<T>> methods();

    // only fields declared by this class, not inherited ones
    Collection<? extends FieldInfo<T>> fields();

    // ---

    @Override
    default Kind kind() {
        return Kind.CLASS;
    }

    @Override
    default ClassInfo<?> asClass() {
        return this;
    }
}
