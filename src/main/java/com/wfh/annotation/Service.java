package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解标识一个业务层类,用在具体的实现类上面,可以创建一个实例
public @interface Service {
    //业务层实现类的名称
    String value() default "";
}
