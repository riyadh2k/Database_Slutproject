package org.example;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    static String url = "localhost";
    static int port = 3307;
    static String database = "slutproject";
    static String username = "root";
    static String password = "";
    private static MysqlDataSource dataSource;

    static {
        dataSource = new MysqlDataSource();
        dataSource.setServerName(url);
        dataSource.setPort(port);
        dataSource.setDatabaseName(database);
        dataSource.setUser(username);
        dataSource.setPassword(password);
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection connection = dataSource.getConnection();
            return connection;
        } catch (SQLException e) {
            // Handle or propagate the exception
            throw e;
        }
    }

    public static void initializeDatabase() throws SQLException {
        try {
            dataSource.setUrl("jdbc:mysql://" + url + ":" + port + "/" + database +
                    "?serverTimezone=UTC");
            dataSource.setUseSSL(false);
        } catch (SQLException e) {
            // Handle the exception
            throw e;
        }
    }

    public static void createTables() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Create users table
            String createUsersTableQuery = "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "social_security_number VARCHAR(11) NOT NULL, "
                    + "password VARCHAR(100) NOT NULL, "
                    + "name VARCHAR(100) NOT NULL, "
                    + "email VARCHAR(100) NOT NULL, "
                    + "address VARCHAR(200) NOT NULL, "
                    + "phone_number VARCHAR(20) NOT NULL, "
                    + "`created` TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")";
            int resultUsersTable = statement.executeUpdate(createUsersTableQuery);
            System.out.println("Users table creation result: " + resultUsersTable);

            // Create accounts table
            String createAccountsTableQuery = "CREATE TABLE IF NOT EXISTS accounts ("
                    + "account_id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "user_id INT , "
                    + "account_number INT , "
                    + "balance DOUBLE , "
                    + "created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (user_id) REFERENCES users(user_id)"
                    + ")";
            int resultAccountsTable = statement.executeUpdate(createAccountsTableQuery);
            System.out.println("Accounts table creation result: " + resultAccountsTable);

            // Create transactions table
            String createTransactionsTableQuery = "CREATE TABLE IF NOT EXISTS transactions ("
                    + "transaction_id INT PRIMARY KEY AUTO_INCREMENT, "
                    + "sender_account_id INT , "
                    + "receiver_account_id INT , "
                    + "amount DOUBLE NOT NULL, "
                    + "transaction_type VARCHAR(50), "
                    + "transaction_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "FOREIGN KEY (sender_account_id) REFERENCES accounts(account_id), "
                    + "FOREIGN KEY (receiver_account_id) REFERENCES accounts(account_id)"
                    + ")";

            int resultTransactionsTable = statement.executeUpdate(createTransactionsTableQuery);
            System.out.println("Transactions table creation result: " + resultTransactionsTable);
        } catch (SQLException e) {
            // Handle the exception
            throw e;
        }
    }

}
