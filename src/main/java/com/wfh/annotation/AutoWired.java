package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
//此注解用于类字段上面,根据类型自动注入bean对象
public @interface AutoWired {
}
