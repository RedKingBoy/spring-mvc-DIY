package com.wfh.proxy;

import com.wfh.annotation.Param;
import com.wfh.jdbc.JdbcUtil;
import com.wfh.jdbc.MultiResultHandler;
import com.wfh.jdbc.SingleResultHandler;
import com.wfh.transaction.Transaction;
import com.wfh.transaction.TransactionHolder;
import com.wfh.utils.XmlConfiguration;
import com.wfh.xml.SqlStatement;
import com.wfh.xml.SqlType;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;

public class MapperInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Transaction transaction = TransactionHolder.getTransaction();
        //通过事务获取连接
        Connection connection = transaction.getConnection();
        //获取sql和执行的参数(根据类名和方法名获取唯一的标识)
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        //类名+方法名:sql唯一标识
        String sqlMark = className + "." + methodName;
        //获取SqlStatement对象
        SqlStatement sqlStatement = XmlConfiguration.getSqlStatement(sqlMark);
        //Bindings表达式需要的变量信息,因为java文件编译后的参数名为系统自定义的无法瞒住#{name}和${name}的参数名要求
        //需要注解去记录参数名
        Bindings bindings = new SimpleBindings();
        Parameter[] parameters = method.getParameters();
        for (int i=0;i<parameters.length;i++) {
            Parameter p = parameters[i];
            Param param = p.getAnnotation(Param.class);
            if (param != null) {
                //参数名
                String paramName = param.value();
                //参数名和参数值放入bindings中
                bindings.put(paramName,args[i]);
            }
        }
        //获取sql
        String sql = sqlStatement.getSql(bindings);
        System.out.println("当前执行的sql:"+sql);
        //获取sql执行的参数
        List<Object> params = sqlStatement.getParams();
        System.out.println("sql执行的参数"+params.toString());
        //获取sql执行后返回的类型
        Class<?> resultType = sqlStatement.getResultType();
        //方法返回值类型
        Class<?> methodReturnType = method.getReturnType();
        //获取sql的操作类型
        SqlType sqlType = sqlStatement.getSqlType();
        if (sqlType == SqlType.SELECT){//查询
            if (Collection.class.isAssignableFrom(methodReturnType)){
                return JdbcUtil.query(new MultiResultHandler<>(resultType),connection,sql,params.toArray());
            }else {
                return JdbcUtil.query(new SingleResultHandler<>(resultType),connection,sql,params.toArray());
            }
        }else {//增删改
            return JdbcUtil.update(connection,sql,params.toArray());
        }
    }
}
