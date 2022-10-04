package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
//此注解应用于mapper的参数上,作为参数名的记录标记
public @interface Param {
    //参数名称
    String value() default "";
}
