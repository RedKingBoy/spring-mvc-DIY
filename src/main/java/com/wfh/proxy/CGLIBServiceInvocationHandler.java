package com.wfh.proxy;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

public class CGLIBServiceInvocationHandler implements MethodInterceptor {
    private Object targetObj;

    public CGLIBServiceInvocationHandler(Object targetObj) {
        this.targetObj = targetObj;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        return null;
    }
}
