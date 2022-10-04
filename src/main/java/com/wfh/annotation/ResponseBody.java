package com.wfh.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
//此注解标识处理器返回数据而不是视图
public @interface ResponseBody {
}
