package com.wfh.transaction;

import com.wfh.ioc.IocContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class Transaction {
    //事务的连接,应该一个对象对应一个连接,不能为static
    private Connection connection;

    public Connection getConnection() {
        return connection;
    }

    public void init() throws SQLException {
        if (connection == null) {
            List<DataSource> dataSources = IocContainer.getBeans(DataSource.class);
            if (dataSources.isEmpty())
                throw new RuntimeException("未配置数据源");
            else if (dataSources.size() > 1)
                throw new RuntimeException("存在多个数据源,请指定获取");
            else {
                DataSource dataSource = dataSources.get(0);
                Connection connection = dataSource.getConnection();
                //设置事务不自动提交
                connection.setAutoCommit(false);
                this.connection = connection;
            }
        }
    }

    //事务提交
    public void commit() throws SQLException {
        if (connection != null) {
            connection.commit();
        }
    }

    //事务回滚
    public void rollBack() throws SQLException {
        if (connection != null) {
            connection.rollback();
        }
    }

    //连接关闭
    public void close() throws SQLException {
        if (connection != null) {
            connection.close();
            //连接关闭,操作完成,释放资源
            TransactionHolder.removeTransaction();
        }
    }
}
