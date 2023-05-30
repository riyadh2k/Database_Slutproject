package org.example;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class Model {
    private static MysqlDataSource dataSource;
    static final String url = "localhost";
    static final int port = 3307;
    static final String database = "slutproject";
    static final String username = "root";
    static final String password = "";

    protected Model() throws SQLException {
        initializeDataSource();
    }

    private static void initializeDataSource() throws SQLException {
        dataSource = new MysqlDataSource();
        dataSource.setUser(username);
        dataSource.setPassword(password);
        dataSource.setUrl("jdbc:mysql://" + url + ":" + port + "/" + database + "?serverTimezone=UTC");
        dataSource.setUseSSL(false);
    }

    protected static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initializeDataSource();
        }

        return dataSource.getConnection();
    }

    protected static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // Handle or log the exception
            }
        }
    }
}
