package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
//此注解标识路径参数/user/{username}
public @interface PathVariable {
    String value() default "";
}
