package com.hong.py.config.annotation;


import java.lang.annotation.*;

/**
 * @since 2.6.5
 *  *
 *  * 2018/9/29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@Inherited
public @interface Method {
    String name();

    int timeout() default -1;

    int retries() default -1;

    String loadbalance() default "";

    boolean async() default false;

    boolean sent() default true;

    int actives() default 0;

    int executes() default 0;

    boolean deprecated() default false;

    boolean sticky() default false;

    boolean isReturn() default true;

    String oninvoke() default "";

    String onreturn() default "";

    String onthrow() default "";

    String cache() default "";

    String validation() default "";

    Argument[] arguments() default {};
}
