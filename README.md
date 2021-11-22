# StillDI

This is a proof of concept implementation of a translation layer between proposed Build Compatible Extensions and existing Portable Extensions.
It includes:

- `impl`, the implementation based on Portable Extensions
- `test`, a playground based on JUnit Jupiter and Weld

It depends on:

- Jakarta CDI `4.0.0.Beta1`
- Weld Core `5.0.0-SNAPSHOT`, from current `master` branch

---

# Build Compatible Extensions API proposal

The proposed Build Compatible Extension API has 2 parts: language model and the actual extension API.
We'll describe them here and compare to Portable Extensions and other notable APIs such as Reflection.

## Language model

The `jakarta.enterprise.lang.model` API models the Java language from a high-level perspective.
This is crucial for extension to be able to inspect classes, methods, fields, annotations, etc.

The model is structured as a hierarchy of types.
The root in this hierarchy is `AnnotationTarget`.
That is anything that can be annotated:

- _declarations_, such as classes, methods or fields;
- _type parameters_ and _type usages_, such as reference types, type variables or wildcard types.

Annotations are represented by `AnnotationInfo`.
Annotation attributes are represented by `AnnotationAttribute`, which is basically a pair of `String` name and annotation attribute value.
Annotation attribute values are represented by `AnnotationAttributeValue`.

| Build Compatible Extensions | Portable Extensions | Reflection | Annotation Processing | Jandex |
| --------------------------- | ------------------- | ---------- | --------------------- | ------ |
| `AnnotationTarget` | `jakarta.enterprise.inject.spi.Annotated` | `java.lang.reflect.AnnotatedElement` | `javax.lang.model.AnnotatedConstruct` | `org.jboss.jandex.AnnotationTarget` |
| `AnnotationInfo` | none, uses `java.lang.Annotation` | none<sup>1</sup> | `javax.lang.model.AnnotationMirror` |`org.jboss.jandex.AnnotationInstance` |
| `AnnotationAttribute` | none<sup>1</sup> | none<sup>1</sup> | none | `org.jboss.jandex.AnnotationValue` |
| `AnnotationAttributeValue` | none<sup>1</sup> | none<sup>1</sup> | `javax.lang.model.element.AnnotationValue` | none |
1. Reflective access is possible through `java.lang.Annotation#annotationType`, `java.lang.Class` and `java.lang.reflect.Method`, but usually not necessary.

As mentioned above, there are two kinds of `AnnotationTarget`s: declarations and types.
Therefore, we have `DeclarationInfo` as the top-level type for representing Java declarations, and `Type` as the top-level type for representing Java types.

Declarations are:
- packages;
- classes;
- fields;
- methods, including constructors;
- method parameters.

| Build Compatible Extensions | Portable Extensions | Reflection | Annotation Processing | Jandex |
| --------------------------- | ------------------- | ---------- | --------------------- | ------ |
| `DeclarationInfo` | none, just `jakarta.enterprise.inject.spi.Annotated` | none<sup>3</sup> | `javax.lang.model.element.Element` | none, just `org.jboss.jandex.AnnotationTarget` |
| `PackageInfo` | none<sup>1</sup> | `java.lang.Package` | `javax.lang.model.element.PackageElement` | none<sup>4</sup> |
| `ClassInfo` | `jakarta.enterprise.inject.spi.AnnotatedType`<sup>2</sup> | `java.lang.Class` | `javax.lang.model.element.TypeElement` | `org.jboss.jandex.ClassInfo` |
| `FieldInfo` | `jakarta.enterprise.inject.spi.AnnotatedField`<sup>2</sup> | `java.lang.reflect.Field` | `javax.lang.model.element.VariableElement` | `org.jboss.jandex.FieldInfo` |
| `MethodInfo` | `jakarta.enterprise.inject.spi.AnnotatedMethod`, `jakarta.enterprise.inject.spi.AnnotatedConstructor`<sup>2</sup> | `java.lang.reflect.Method`, `java.lang.reflect.Constructor` | `javax.lang.model.element.ExecutableElement` | `org.jboss.jandex.MethodInfo` |
| `ParameterInfo` | `jakarta.enterprise.inject.spi.AnnotatedParameter`<sup>2</sup> | `java.lang.reflect.Parameter` | `javax.lang.model.element.VariableElement` | `org.jboss.jandex.MethodParameterInfo` | 
1. Relies on Reflection.
2. Provides access to the corresponding Reflection object.
3. There are types such as `java.lang.reflect.GenericDeclaration` or `java.lang.reflect.AccessibleObject`, but nothing directly corresponding,
4. May use `ClassInfo` for `package-info.class`,

Types are:
- void;
- primitive types, such as `int`;
- class types, such as `String`;
- array types, such as `int[]` or `String[][]`;
- parameterized types, such as `List<String>`;
- type variables, such as `T` when used in a class or method that declares a type parameter `T`;
- wildcard types, such as `? extends Number`.
                   
| Build Compatible Extensions | Portable Extensions | Reflection | Annotation Processing | Jandex |
| --------------------------- | ------------------- | ---------- | --------------------- | ------ |
| `Type` |  none<sup>1</sup> | `java.lang.reflect.Type`<sup>2</sup> | `javax.lang.model.type.TypeMirror` | `org.jboss.jandex.Type` |
| `VoidType` | none<sup>1</sup> | `java.lang.Class`<sup>2</sup> | `javax.lang.model.type.NoType` | `org.jboss.jandex.VoidType` |
| `PrimitiveType` | none<sup>1</sup> | `java.lang.Class`<sup>2</sup> | `javax.lang.model.type.PrimitiveType` | `org.jboss.jandex.PrimitiveType` |
| `ClassType` | none<sup>1</sup> | `java.lang.Class`<sup>2</sup> | `javax.lang.model.type.DeclaredType` | `org.jboss.jandex.ClassType` |
| `ArrayType` | none<sup>1</sup> | `java.lang.Class`, `java.lang.reflect.GenericArrayType`<sup>2</sup> | `javax.lang.model.type.ArrayType` | `org.jboss.jandex.ArrayType` |
| `ParameterizedType` | none<sup>1</sup> | `java.lang.reflect.ParameterizedType`<sup>2</sup> | `javax.lang.model.type.DeclaredType` | `org.jboss.jandex.ParameterizedType` | 
| `TypeVariable` | none<sup>1</sup> | `java.lang.reflect.TypeVariable`<sup>2</sup> | `javax.lang.model.type.TypeVariable` | `org.jboss.jandex.TypeVariable`, `org.jboss.jandex.UnresolvedTypeVariable` | 
| `WildcardType` | none<sup>1</sup> | `java.lang.reflect.WildcardType`<sup>2</sup> | `javax.lang.model.type.WildcardType` | `org.jboss.jandex.WildcardType` | 
1. Relies on Reflection.
2. There's also an alternative hierarchy rooted at `java.lang.reflect.AnnotatedType`.

## Extension API
                     
Build Compatible Extensions, similarly to Portable Extensions, are service providers for the `BuildCompatibleExtension` interface.
They can declare arbitrary methods annotated with one of the processing phases annotations.
These methods can declare arbitrary parameters out of a particular set of types supported for given processing phase.

The extension API proposes 5 processing phases, roughly corresponding to Portable Extensions processing phases.

- `@Discovery`, that allows adding types to be scanned during bean discovery, and allows registering custom meta-annotations;
- `@Enhancement`, that allows modifying annotations;
- `@Processing`, that allows looking at registered beans and observers;
- `@Synthesis`, that allows registering synthetic beans and observers;
- `@Validation`, that allows custom validation.

| Build Compatible Extensions | Portable Extensions |
| --------------------------- | ------------------- |
| `@Discovery` | `jakarta.enterprise.inject.spi.BeforeBeanDiscovery` |
| `@Enhancement` | `jakarta.enterprise.inject.spi.ProcessAnnotatedType` |
| `@Processing` | `jakarta.enterprise.inject.spi.ProcessBean`, `jakarta.enterprise.inject.spi.ProcessObserverMethod` |
| `@Synthesis` | `jakarta.enterprise.inject.spi.AfterBeanDiscovery` |
| `@Validation` | `jakarta.enterprise.inject.spi.AfterDeploymentValidation` |

A simple _Hello, world!_ extension would look like this:

```java
// the corresponding META-INF/services/jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension file must also exist
public class MyExtension implements BuildCompatibleExtension {
    @Discovery
    public void hello(Messages msg) {
        msg.info("Hello, world!");
    }
}
```

`Messages` is one of the types that extension methods can use as a parameter.
Let's take a look at what Build Compatible Extensions can do in all the phases.

### `@Discovery`

Extension methods annotated `@Discovery` can declare parameters of these types:

- `AppArchiveBuilder`: add types to be scanned during bean discovery
- `MetaAnnotations`: register custom meta-annotations (qualifiers, interceptor bindings, stereotypes, and scopes)
- `Messages`: logging and validation

| Build Compatible Extensions | Portable Extensions |
| --------------------------- | ------------------- |
| `AppArchiveBuilder` | `jakarta.enterprise.inject.spi.BeforeBeanDiscovery#addAnnotatedType` |
| `MetaAnnotations` | `jakarta.enterprise.inject.spi.BeforeBeanDiscovery#configureQualifier`, `jakarta.enterprise.inject.spi.BeforeBeanDiscovery#configureInterceptorBinding`, `jakarta.enterprise.inject.spi.BeforeBeanDiscovery#addStereotype`, `jakarta.enterprise.inject.spi.BeforeBeanDiscovery#addScope` + `jakarta.enterprise.inject.spi.AfterBeanDiscovery#addContext` |
| `Messages#error` | `jakarta.enterprise.inject.spi.AfterDeploymentValidation#addDeploymentProblem` |
                   
### `@Enhancement`

Extension methods annotated `@Enhancement` can declare parameters of these types:

- `ClassConfig`: transform annotations on classes that satisfy declaratively expressed criteria (see below)
- `MethodConfig`: transform annotations on methods that satisfy declaratively expressed criteria (see below)
- `FieldConfig`: transform annotations on fields that satisfy declaratively expressed criteria (see below)
- `AppArchiveConfig`: transform annotations on classes/methods/fields that satisfy programmatically expressed criteria (see below)
- `Types`: utility to create instances of `Type`
- `Annotations`: utility to create instances of `AnnotationAttribute` and `AnnotationAttributeValue`
- `Messages`: logging and validation
                    
Each `@Enhancement` method must declare exactly 1 parameter of type `ClassConfig`, `MethodConfig`, `FieldConfig` or `AppArchiveConfig`.
If the method declares a parameter of type `ClassConfig`, `MethodConfig` or `FieldConfig`, it also has to have at least 1 annotation of type `@ExactType` or `@SubtypesOf`.
This is to constrain the set of types for which the method is used as an annotation transformation callback.
If the method declares a parameter of type `AppArchiveConfig`, it registers annotation transformation callbacks explicitly.
See Javadoc for more.

As an example, let's move a qualifier from one class to another:

```java
// 1. a qualifier annotation
@Qualifier
@Retention(RUNTIME)
public @interface MyQualifier {
}

// 2. a service interface
public interface MyService {
    String hello();
}

// 3. two implementations, one with qualifier and the other unqualified
@Singleton
@MyQualifier
public class MyFooService implements MyService {
    @Override
    public String hello() {
        return "foo";
    }
}

@Singleton
public class MyBarService implements MyService {
    @Override
    public String hello() {
        return "bar";
    }
}

// 4. a class that uses the service
@Singleton
public class MyServiceUser {
    @Inject
    @MyQualifier
    MyService myService;
}

// 5. the extension
public class MyExtension implements BuildCompatibleExtension {
    @Enhancement
    @ExactType(type = MyFooService.class)
    public void foo(ClassConfig clazz) {
        clazz.removeAnnotation(it -> it.name().equals(MyQualifier.class.getName()));
    }

    @Enhancement
    @ExactType(type = MyBarService.class)
    public void bar(ClassConfig clazz) {
        clazz.addAnnotation(MyQualifier.class);
    }
}
```

| Build Compatible Extensions | Portable Extensions |
| --------------------------- | ------------------- |
| `ClassConfig` | `jakarta.enterprise.inject.spi.ProcessAnnotatedType#configureAnnotatedType` |
| `MethodConfig` | `jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator#methods`, `jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator#constructors` |
| `FieldConfig` | `jakarta.enterprise.inject.spi.configurator.AnnotatedTypeConfigurator#fields` |
| `AppArchiveConfig` | `jakarta.enterprise.inject.spi.ProcessAnnotatedType` |
| `Types` | none, relies on Reflection |
| `Annotations` | none, relies on `java.lang.Annotation` and `jakarta.enterprise.util.AnnotationLiteral` |
| `Messages#error` | `jakarta.enterprise.inject.spi.AfterDeploymentValidation#addDeploymentProblem` |

### `@Processing`

Extension methods annotated `@Processing` can declare parameters of these types:

- `BeanInfo`: look at registered beans that satisfy declaratively expressed criteria (see below)
- `ObserverInfo`: look at registered observers that satisfy declaratively expressed criteria (see below)
- `Types`: utility to create instances of `Type`
- `Messages`: logging and validation

Each `@Processing` method must declare exactly 1 parameter of type `BeanInfo` or `ObserverInfo`.
It also has to have at least 1 annotation of type `@ExactType` or `@SubtypesOf`.
This is to constrain the set of types of beans/observers for which the method is called.
The `annotatedWith` attribute of `@ExactType` and `@SubtypesOf` is ignored here; it's only relevant in `@Enhancement`.

| Build Compatible Extensions | Portable Extensions |
| --------------------------- | ------------------- |
| `BeanInfo` | read-only view on `jakarta.enterprise.inject.spi.ProcessBean` |
| `ObserverInfo` | read-only view on `jakarta.enterprise.inject.spi.ProcessObserverMethod` |
| `Types` | none, relies on Reflection |
| `Messages#error` | `jakarta.enterprise.inject.spi.ProcessBean#addDefinitionError`, `jakarta.enterprise.inject.spi.ProcessObserverMethod#addDefinitionError` |

### `@Synthesis`

Extension methods annotated `@Synthesis` can declare parameters of these types:

- `AppArchive`: inspect classes in the application
- `AppDeployment`: inspect beans and observers in the application
- `SyntheticComponents`: register synthetic beans and observers
- `Types`: utility to create instances of `Type`
- `Messages`: logging and validation

As an example, let's create a synthetic bean.
Synthetic observers are very similar.

```java
// 1. a bean class
public class MyPojo {
    public final String data;

    public MyPojo(String data) {
        this.data = data;
    }
}

// 2. bean creation function
public class MyPojoCreator implements SyntheticBeanCreator<MyPojo> {
    @Override
    public MyPojo create(CreationalContext<MyPojo> creationalContext, InjectionPoint injectionPoint, Map<String, Object> params) {
        String name = (String) params.get("name");
        return new MyPojo("Hello " + name);
    }
}

// 3. bean disposal function
public static class MyPojoDisposer implements SyntheticBeanDisposer<MyPojo> {
    @Override
    public void dispose(MyPojo instance, CreationalContext<MyPojo> creationalContext, Map<String, Object> params) {
        System.out.println("disposing " + instance.data);
    }
}

// 4. the extension
public static class MyExtension implements BuildCompatibleExtension {
    @Synthesis
    public void synthesise(SyntheticComponents syn) {
        syn.addBean(MyPojo.class)
                .type(MyPojo.class)
                .withParam("name", "World")
                .createWith(MyPojoCreator.class)
                .disposeWith(MyPojoDisposer.class);
    }
}
```

| Build Compatible Extensions | Portable Extensions |
| --------------------------- | ------------------- |
| `AppArchive` | read-only view on `jakarta.enterprise.inject.spi.ProcessAnnotatedType` |
| `AppDeployment` | read-only view on `jakarta.enterprise.inject.spi.ProcessBean` and `jakarta.enterprise.inject.spi.ProcessObserverMethod` |
| `SyntheticComponents` | `jakarta.enterprise.inject.spi.AfterBeanDiscovery#addBean` + `jakarta.enterprise.inject.spi.AfterBeanDiscovery#addObserverMethod` |
| `Types` | none, relies on Reflection |
| `Messages#error` | `jakarta.enterprise.inject.spi.AfterDeploymentValidation#addDeploymentProblem` |

### `@Validation`

Extension methods annotated `@Validation` can declare parameters of these types:

- `AppArchive`: inspect classes in the application
- `AppDeployment`: inspect beans and observers in the application
- `Types`: utility to create instances of `Type`
- `Messages`: logging and validation

| Build Compatible Extensions | Portable Extensions |
| --------------------------- | ------------------- |
| `AppArchive` | read-only view on `jakarta.enterprise.inject.spi.ProcessAnnotatedType` |
| `AppDeployment` | read-only view on `jakarta.enterprise.inject.spi.ProcessBean` and `jakarta.enterprise.inject.spi.ProcessObserverMethod`  |
| `Types` | none, relies on Reflection |
| `Messages#error` | `jakarta.enterprise.inject.spi.AfterDeploymentValidation#addDeploymentProblem` |
