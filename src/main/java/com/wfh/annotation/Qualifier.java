package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
//此注解用于字段上面与AutoWired注解一起使用,作用:当注入值的同类型对象有多个时,根据实例的名称来取具体实例
public @interface Qualifier {
    //实例的名称
    String value() default "";
}
