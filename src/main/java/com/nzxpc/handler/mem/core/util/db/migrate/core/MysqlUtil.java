package com.nzxpc.handler.mem.core.util.db.migrate.core;

import com.nzxpc.handler.mem.core.util.LogUtil;
import com.nzxpc.handler.mem.core.util.db.migrate.function.ResultSetHandler;
import org.apache.http.client.ResponseHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MysqlUtil implements AutoCloseable {
    private String url;
    private String userName;
    private String passWord;
    private Connection connection;

    @Override
    public void close() throws Exception {

    }

    /**
     * 获取连接
     *
     * @return
     */
    private Connection getConnection() throws SQLException {
        if (this.connection == null) {
            connection = DriverManager.getConnection(this.url, this.userName, this.passWord);
        }
        return this.connection;
    }

    private PreparedStatement getStatement(String sql, Object[] args) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement(sql);
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }
        }
        return statement;
    }

    /**
     * 查询单个
     *
     * @param sql     sql语句
     * @param handler resultset处理
     * @param args    sql参数
     * @param <R>     返回类型
     * @return
     * @throws SQLException handler调用方法，传入参数，参数在另一方进行处理
     */
    public <R> R queryForObject(String sql, ResultSetHandler<R> handler, Object... args) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = getStatement(sql, args);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return handler.accept(resultSet);
            }
            return null;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    /**
     * 查询列表
     *
     * @param sql
     * @param handler
     * @param args
     * @param <R>
     * @return
     */
    public <R> ArrayList<R> queryForList(String sql, ResultSetHandler<R> handler, Object... args) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = getStatement(sql, args);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<R> arrayList = new ArrayList<>();
            while (resultSet.next()) {
                arrayList.add(handler.accept(resultSet));//调用方法体执行并返回参数
            }
            return arrayList;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    public static MysqlUtil createUtil(String url, String userName, String passWord, boolean auto) {
        MysqlUtil util = new MysqlUtil();
        util.url = url;
        util.userName = userName;
        util.passWord = passWord;
        if (auto) {
            util.createDatabase();
        }
        return util;
    }

    private void createDatabase() {
        Matcher matcher = Pattern.compile("jdbc:mysql://[^/]+/([^?]+).*").matcher(this.url);
        String dbName = "";
        if (matcher.find()) {
            dbName = matcher.group(1);
        }
        String url = this.url.replace("/" + dbName, "");
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        try {
            connection = DriverManager.getConnection(url, this.userName, this.passWord);
            preparedStatement = connection.prepareStatement(String.format("CREATE DATABASE IF NOT EXISTS %s DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci", dbName));
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LogUtil.err(MysqlUtil.class, e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (preparedStatement != null) {
                    preparedStatement.close();
                }
            } catch (SQLException e) {
                LogUtil.err(MysqlUtil.class, e);
            }
        }
    }

}
