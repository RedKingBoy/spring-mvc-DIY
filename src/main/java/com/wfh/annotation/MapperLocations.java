package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解用于配置类上面,做mapper的xml文件路径的标识
public @interface MapperLocations {
    //mapper的xml文件路径
    String value() default "";
}
