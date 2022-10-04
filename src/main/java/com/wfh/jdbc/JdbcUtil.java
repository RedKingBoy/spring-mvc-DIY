package com.wfh.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.*;

public class JdbcUtil {

    public static <T> T query(ResultSetHandler<T> resultSetHandler,
                              Connection connection,
                              String sql,
                              Object... params) {
        T result = null;
        try {
            //获取预编译对象
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    //编译sql参数
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            //执行sql获取结果集
            ResultSet resultSet = preparedStatement.executeQuery();
            result = resultSetHandler.handle(resultSet);
            //关闭资源
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int update(Connection connection,
                             String sql,
                             Object... params) {
        //受影响的行数
        int result = 0;
        try {
            //获取预编译对象
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            if (params != null && params.length > 0) {
                for (int i = 0; i < params.length; i++) {
                    //编译sql参数
                    preparedStatement.setObject(i + 1, params[i]);
                }
            }
            //执行sql获取结果集
            result = preparedStatement.executeUpdate();
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static <T> T getObject(ResultSet resultSet, Class<T> clazz) throws Exception {
        T t = clazz.newInstance();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            String columnLabel = metaData.getColumnLabel(i + 1);
            Field declaredField = clazz.getDeclaredField(columnLabel);
            String name = declaredField.getName();
            String methodName = "set"+name.substring(0,1).toUpperCase()+name.substring(1);
            Class<?> type = declaredField.getType();
            Method method = clazz.getMethod(methodName, type);
            method.invoke(t,resultSet.getObject(columnLabel, type));
        }
        return t;
    }
}
