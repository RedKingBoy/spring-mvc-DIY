package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解标识为一个组件,应用在类上面,有此注解表示可以创建一个bean对象
public @interface Component {
    //bean对象name
    String value() default "";
}
