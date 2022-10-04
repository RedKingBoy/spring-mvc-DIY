package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
//此注解标识请求的地址和方式
public @interface RequestMapping {
    //请求地址
    String value() default "";
    //请求方式
    String method() default "";
}
