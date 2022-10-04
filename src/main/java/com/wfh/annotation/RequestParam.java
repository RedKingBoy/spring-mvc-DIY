package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
//用在参数上面,标注请求url地址后的参数
public @interface RequestParam {
    String value() default "";
}
