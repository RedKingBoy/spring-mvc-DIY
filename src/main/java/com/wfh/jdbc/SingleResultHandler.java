package com.wfh.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SingleResultHandler<T> implements ResultSetHandler<T> {
    private Class<T> clazz;

    public SingleResultHandler(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public T handle(ResultSet resultSet) throws SQLException {
        T t = null;
        while (resultSet.next()){
            if (t==null){//父类.class.isAssignableFrom(子类.class)
                if(clazz.isPrimitive() ||Number.class.isAssignableFrom(clazz) ||clazz==Boolean.class||clazz==Character.class||clazz==String.class){
                    t = resultSet.getObject(1,clazz);
                    continue;
                }
                try {
                    t = JdbcUtil.getObject(resultSet, clazz);
                    return t;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return t;
    }
}
