package stilldi.impl;

final class Subtyping {
    /**
     * @return whether {@code inspectedClass} is a subtype of {@code superclass}
     * (currently returns {@code false} if they are equal, which we might want to change)
     */
    // we centralize the decision whether inspectedClass is a subtype of superclass here,
    // so that we can easily change it when we decide on the final semantics of @SubtypesOf
    static boolean isSubtype(Class<?> superclass, Class<?> inspectedClass) {
        return !superclass.equals(inspectedClass) && superclass.isAssignableFrom(inspectedClass);
    }
}
