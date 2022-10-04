package com.wfh.adapter;

import com.wfh.model.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//处理器适配器接口
public interface HandlerAdapter {
    //适配器支持的处理器类型
    boolean support(Class<?> clazz);

    //处理请求
    ModelAndView handleRequest(HttpServletRequest req, HttpServletResponse resp,Object handler);
}
