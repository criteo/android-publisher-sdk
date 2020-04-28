package com.criteo.publisher.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicate that the annotated element will soon be part of the public API, but the entire feature
 * is not ready yet to be published publicly.
 *
 * This help the rollout of new APIs. Once a feature is finished, one may replace this annotation by
 * {@link android.support.annotation.Keep} or add a matching proguard rule.
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
public @interface Incubating {

  String NATIVE = "native";

  /**
   * Marker to indicate the name of the feature, in which this annotated elements belongs to.
   */
  String value() default "";

}
