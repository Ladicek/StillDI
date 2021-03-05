package cdi.lite.extension.phases.discovery;

import cdi.lite.extension.phases.enhancement.ClassConfig;

import java.lang.annotation.Annotation;

public interface MetaAnnotations {
    ClassConfig addQualifier(Class<? extends Annotation> annotation);

    ClassConfig addInterceptorBinding(Class<? extends Annotation> annotation);

    ClassConfig addStereotype(Class<? extends Annotation> annotation);

    // includes defining the scope annotation
    ContextBuilder addContext();
}
