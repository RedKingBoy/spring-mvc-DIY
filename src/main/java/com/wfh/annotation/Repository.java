package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解用于数据访问层(dao),表示数据的仓库,应用于dao接口的实现类,应该被扫描并创建bean对象
public @interface Repository {
    //bean对象的name
    String value() default "";
}
