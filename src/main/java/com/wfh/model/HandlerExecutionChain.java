package com.wfh.model;

import com.wfh.interceptor.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

//处理器执行链
public class HandlerExecutionChain {
    //处理器HandlerMethod
    private Object handler;
    //拦截器链
    private List<HandlerInterceptor> interceptors = new ArrayList<>();
    //已经执行的拦截器下标(因为拦截器不通过也会执行后置拦截,需要记录对应不通过的下标)
    private int executedIndex = 0;

    public HandlerExecutionChain(Object handler) {
        this.handler = handler;
    }

    public Object getHandler() {
        return handler;
    }

    public void setHandler(Object handler) {
        this.handler = handler;
    }

    //添加拦截器
    public void addInterceptor(HandlerInterceptor interceptor) {
        interceptors.add(interceptor);
    }

    public boolean applyPreHandle(HttpServletRequest req, HttpServletResponse resp) {
        for (int i = 0; i < interceptors.size(); i++) {
            //获取拦截器
            HandlerInterceptor interceptor = interceptors.get(i);
            //调用前置拦截
            if (!interceptor.preHandle(req, resp, handler)) {
                //不通过就记录下标,返回false
                executedIndex = i;
                return false;
            }
        }
        return true;
    }

    public void applyPostHandle(HttpServletRequest req, HttpServletResponse resp) {
        //后置拦截是在请求处理后获得视图时执行
        for (int i = interceptors.size()-1; i >= 0; i--) {
            //获取拦截器
            HandlerInterceptor interceptor = interceptors.get(i);
            //调用后置拦截
            interceptor.postHandle(req,resp,handler);
        }
    }
    public void applyAfterComplete(HttpServletRequest req,HttpServletResponse resp,Exception ex){
        //最终拦截是无论何时都会执行,所以当拦截器没通过时该拦截器的最终拦截也会执行
        for (int i = executedIndex; i >= 0; i--) {
            //获取拦截器
            HandlerInterceptor interceptor = interceptors.get(i);
            //调用最终拦截
            interceptor.afterComplete(req,resp,handler,ex);
        }
    }
}
