package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
//此注解用于数据访问层(dao),表示数据的仓库,应该被扫描并创建代理实例
public @interface Mapper {
    String value() default "";
}
