package com.wfh.interceptor;

import com.wfh.model.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//创建拦截器接口
public interface HandlerInterceptor {
    //前置拦截
    default boolean preHandle(HttpServletRequest req,
                              HttpServletResponse resp,
                              Object handler){
        return true;
    }
    //后置拦截,在处理请求之后,获取模型与视图
    default void postHandle(HttpServletRequest req,
                            HttpServletResponse resp,
                            Object handler){

    }
    //最终拦截:视图得到之后渲染视图后做事
    default void afterComplete(HttpServletRequest req,
                               HttpServletResponse resp,
                               Object handler,
                               Exception ex){

    }
}
