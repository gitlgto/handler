package com.nzxpc.handler.mem.core.util.db.migrate.core;

import com.nzxpc.handler.mem.core.util.LogUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
