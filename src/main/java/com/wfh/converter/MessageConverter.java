package com.wfh.converter;

import javax.servlet.http.HttpServletRequest;

public interface MessageConverter {
    //消息转换器支持的参数类型
    boolean support(Class<?> clazz);
    //通过请求的数据转换为为对应的类型(类型由用户自己定义clazz)
    <T> T convert(HttpServletRequest req, Class<T> clazz,Class<?> genericClass);
}
