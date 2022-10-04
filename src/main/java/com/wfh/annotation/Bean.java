package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//此注解用来替代spring-xml的bean标签,表明创建一个bean对象
public @interface Bean {
    String value() default "";
}
