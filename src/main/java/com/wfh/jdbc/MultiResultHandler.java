package com.wfh.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MultiResultHandler<T> implements ResultSetHandler<List<T>> {
    private Class<T> clazz;

    public MultiResultHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<T> handle(ResultSet resultSet) throws SQLException {
        List<T> list = new ArrayList<>();
        while (resultSet.next()) {
            T t = null;
            try {//如果结果是具体数值的类型需要判断
                if (clazz.isPrimitive() || Number.class.isAssignableFrom(clazz) || clazz == Boolean.class || clazz == Character.class || clazz == String.class) {
                    t = resultSet.getObject(1, clazz);
                }else {
                    t = JdbcUtil.getObject(resultSet, clazz);
                }
                list.add(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
