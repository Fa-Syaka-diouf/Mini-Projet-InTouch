package com.elfstack.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MenuWithLayout {
    String title() default "";
    String icon() default "";
    double order() default Double.MIN_VALUE;
    String layout();
}
