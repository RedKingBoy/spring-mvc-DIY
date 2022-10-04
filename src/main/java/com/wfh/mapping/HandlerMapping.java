package com.wfh.mapping;

import com.wfh.model.HandlerExecutionChain;

import javax.servlet.http.HttpServletRequest;

//处理器映射器接口
public interface HandlerMapping {
    //根据请求来获取一个处理器执行链(根据什么方式来找执行链:实现这接口自己定义找到执行链的方法)
    HandlerExecutionChain mapping(HttpServletRequest req);
}
