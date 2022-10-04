package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解标识类为控制器类
public @interface Controller {
    String value() default "";
}
