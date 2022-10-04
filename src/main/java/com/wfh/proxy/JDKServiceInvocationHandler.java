package com.wfh.proxy;

import com.wfh.annotation.Transactional;
import com.wfh.transaction.Transaction;
import com.wfh.transaction.TransactionHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JDKServiceInvocationHandler implements InvocationHandler {
    private Object targetObj;
    //因为是业务层的动态代理要引进目标实例

    public JDKServiceInvocationHandler(Object targetObj) {
        this.targetObj = targetObj;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //因为是jdk动态代理:对接口做的代理,方法也是接口方法,没有注解!
        //转换为实现类的方法
        Method ImplMethod = targetObj.getClass().getMethod(method.getName(), method.getParameterTypes());
        //获取业务层的方法上面的Transactional注解,标识开启事务
        Transactional transactional = ImplMethod.getAnnotation(Transactional.class);
        if (transactional == null) {//没有事务直接调用方法
            return method.invoke(targetObj, args);
        } else {//有事务
            //获取事务
            Transaction transaction = TransactionHolder.getTransaction();
            //事务初始化connection
            transaction.init();
            //触发事务回滚的异常
            Class<? extends Exception> ex = transactional.rollBackFor();
            try {
                Object invoke = method.invoke(targetObj, args);
                //事务提交
                transaction.commit();
                return invoke;
            } catch (Exception e) {
                //调用方法产生的异常必须为设定异常的子类
                if (ex.isAssignableFrom(e.getClass())){
                    //事务回滚
                    transaction.rollBack();
                }
                throw e;
            }finally {
                //方法调用完毕,关闭连接
                transaction.close();
            }
        }
    }
}
