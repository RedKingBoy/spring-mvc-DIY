package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
//此注解用在业务层的方法上,标识开启事务
public @interface Transactional {
    //rollBackFor标识产生回滚的异常类型
    Class<? extends Exception> rollBackFor() default RuntimeException.class;
}
