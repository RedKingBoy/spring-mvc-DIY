package com.wfh.annotation;

import java.lang.annotation.*;

//此注解用于扫描控制器包的标识
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScan {
    //value值表明要扫描的控制器包
    String[] value() default {};
}
