package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解就是标识类为配置类能够获取handlerMapping
public @interface Configuration {
}
