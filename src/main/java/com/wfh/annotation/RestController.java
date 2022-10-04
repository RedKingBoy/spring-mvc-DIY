package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此类标识类为控制器类且返回值类型是数据
public @interface RestController {
    String value() default "";
}
