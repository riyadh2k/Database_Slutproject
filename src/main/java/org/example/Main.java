package org.example;
import com.mysql.cj.jdbc.MysqlDataSource;
import java.sql.*;

import java.util.Scanner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Main {
    static Scanner scanner;
    static MysqlDataSource dataSource;
    static String url = "localhost";
    static int port = 3307;
    static String database = "slutproject";
    static String username = "root";
    static String password = "";

    public static void main(String[] args) throws SQLException {
        scanner = new Scanner(System.in);

        InitializeDatabase();
        CreateTable();
        boolean run = true;

        while (run) {
            System.out.println("Välj vad du vill göra.");
            System.out.println("1. Create user");
            System.out.println("2. Delete User");
            System.out.println("3. Add User's Account");
            System.out.println("4. Remove User's Account ");
            System.out.println("5. Update User's Information");
            System.out.println("6. Send Amount  ");
            System.out.println("7. List Of Account Transaction ");
            System.out.println("8. User's Summary");
            System.out.println("9. Avsluta");

            switch (scanner.nextLine().trim()) {
                case "1":
                    createUser();
                    break;

                case "2":
                    removeUser();
                    break;

                case "3":
                    addAccount();
                    break;
                case "4":
                    removeAccount();
                    break;

                case "5":
                    updateUserDetails();
                    break;

                case "6":
                    sendTransaction();
                    break;

                case "7":
                    listAccountTransactions();
                    break;

                case "8":
                    userSummary();
                    break;

                case "9":
                    run = false;
                    break;

                default:
                    break;
            }
        }
    }


    //Konfigurerar kopplingar mot databasen
    public static void InitializeDatabase() {
        try {
            System.out.print("Configuring data source...");
            dataSource = new MysqlDataSource();
            dataSource.setUser(username);
            dataSource.setPassword(password);
            dataSource.setUrl("jdbc:mysql://" + url + ":" + port + "/" + database +
                    "?serverTimezone=UTC");
            dataSource.setUseSSL(false);
            System.out.print("done!\n");
        } catch (SQLException e) {
            System.out.print("failed!\n");
            PrintSQLException(e);
            System.exit(0);
        }
    }

    private static void PrintSQLException(SQLException e) {
    }

    //Skapar en tillfällig koppling till databasen
    public static Connection GetConnection() {
        try {
//System.out.printf("Fetching connection to database...");
            Connection connection = dataSource.getConnection();
//System.out.printf("done!\n");
            return connection;
        } catch (SQLException e) {
//System.out.printf("failed!\n");
            PrintSQLException(e);
            System.exit(0);
            return null;
        }
    }

    public static void CreateTable() throws SQLException {
        Connection connection = GetConnection();
        Statement statement = connection.createStatement();

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
                + "account_id INT , "
                + "sender_account_id INT , "
                + "receiver_account_id INT , "
                + "amount DOUBLE NOT NULL, "
                + "transaction_type VARCHAR(50), " // Add transaction_type column
                + "transaction_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (account_id) REFERENCES accounts(account_id), "
                + "FOREIGN KEY (sender_account_id) REFERENCES accounts(account_id), "
                + "FOREIGN KEY (receiver_account_id) REFERENCES accounts(account_id)"
                + ")";


        int resultTransactionsTable = statement.executeUpdate(createTransactionsTableQuery);
        System.out.println("Transactions table creation result: " + resultTransactionsTable);

        connection.close();
    }
    public static void createUser() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter social security number: ");
        String socialSecurityNumber = scanner.nextLine();

        // Check if social security number already exists
        String checkUserQuery = "SELECT COUNT(*) FROM users WHERE social_security_number = ?";
        PreparedStatement checkUserStatement = connection.prepareStatement(checkUserQuery);
        checkUserStatement.setString(1, socialSecurityNumber);
        ResultSet resultSet = checkUserStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        if (count > 0) {
            System.out.println("User with the provided social security number already exists.");
            connection.close();
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter address: ");
        String address = scanner.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();

        String insertUserQuery = "INSERT INTO users (social_security_number, password, name, email, address, phone_number) VALUES (?, ?, ?, ?, ?,?)";

        PreparedStatement insertUserStatement = connection.prepareStatement(insertUserQuery);
        insertUserStatement.setString(1, socialSecurityNumber);
        insertUserStatement.setString(2, password);
        insertUserStatement.setString(3, name);
        insertUserStatement.setString(4, email);
        insertUserStatement.setString(5, address);
        insertUserStatement.setString(6, phoneNumber);

        int result = insertUserStatement.executeUpdate();

        if (result > 0) {
            System.out.println("User created successfully.");
        } else {
            System.out.println("Failed to create user.");
        }

        connection.close();
    }
    public static void removeUser() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter social security number: ");
        String socialSecurityNumber = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Verify user credentials
        String verifyUserQuery = "SELECT COUNT(*) FROM users WHERE social_security_number = ? AND password = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, socialSecurityNumber);
        verifyUserStatement.setString(2, password);
        ResultSet resultSet = verifyUserStatement.executeQuery();
        resultSet.next();
        int count = resultSet.getInt(1);
        if (count == 0) {
            System.out.println("Invalid social security number or password.");
            connection.close();
            return;
        }

        // Delete associated transactions
        String deleteTransactionsQuery = "DELETE FROM transactions WHERE sender_account_id IN (SELECT account_id FROM accounts WHERE user_id = (SELECT user_id FROM users WHERE social_security_number = ?))";
        PreparedStatement deleteTransactionsStatement = connection.prepareStatement(deleteTransactionsQuery);
        deleteTransactionsStatement.setString(1, socialSecurityNumber);
        deleteTransactionsStatement.executeUpdate();

        // Delete associated accounts
        String deleteAccountsQuery = "DELETE FROM accounts WHERE user_id = (SELECT user_id FROM users WHERE social_security_number = ?)";
        PreparedStatement deleteAccountsStatement = connection.prepareStatement(deleteAccountsQuery);
        deleteAccountsStatement.setString(1, socialSecurityNumber);
        deleteAccountsStatement.executeUpdate();

        // Delete user
        String deleteUserQuery = "DELETE FROM users WHERE social_security_number = ?";
        PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery);
        deleteUserStatement.setString(1, socialSecurityNumber);
        int result = deleteUserStatement.executeUpdate();

        if (result > 0) {
            System.out.println("User and associated records removed successfully.");
        } else {
            System.out.println("Failed to remove user.");
        }

        connection.close();
    }

    public static void addAccount() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter social security number: ");
        String socialSecurityNumber = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Verify user credentials
        String verifyUserQuery = "SELECT * FROM users WHERE social_security_number = ? AND password = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, socialSecurityNumber);
        verifyUserStatement.setString(2, password);
        ResultSet resultSet = verifyUserStatement.executeQuery();

        if (resultSet.next()) {
            // User logged in successfully
            int userId = resultSet.getInt("user_id");

            System.out.print("Enter account number: ");
            String accountNumber = scanner.nextLine();

            System.out.print("Enter amount: ");
            double amount = scanner.nextDouble();

            // Check if account already exists for the user
            String checkAccountQuery = "SELECT * FROM accounts WHERE user_id = ? AND account_number = ?";
            PreparedStatement checkAccountStatement = connection.prepareStatement(checkAccountQuery);
            checkAccountStatement.setInt(1, userId);
            checkAccountStatement.setString(2, accountNumber);
            ResultSet accountResultSet = checkAccountStatement.executeQuery();

            if (accountResultSet.next()) {
                // Account already exists, update the balance
                int accountId = accountResultSet.getInt("account_id");
                double currentBalance = accountResultSet.getDouble("balance");
                double newBalance = currentBalance + amount;

                // Update the balance of the existing account
                String updateAccountQuery = "UPDATE accounts SET balance = ? WHERE account_id = ?";
                PreparedStatement updateAccountStatement = connection.prepareStatement(updateAccountQuery);
                updateAccountStatement.setDouble(1, newBalance);
                updateAccountStatement.setInt(2, accountId);
                int result = updateAccountStatement.executeUpdate();

                if (result > 0) {
                    System.out.println("Account balance updated successfully.");

                    // Insert transaction record
                    String insertTransactionQuery = "INSERT INTO transactions (account_id, amount) VALUES (?, ?)";
                    PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery);
                    insertTransactionStatement.setInt(1, accountId);
                    //insertTransactionStatement.setInt(2, -1); // Placeholder for receiver account ID
                    insertTransactionStatement.setDouble(2, amount);
                    int transactionResult = insertTransactionStatement.executeUpdate();

                    if (transactionResult > 0) {
                        System.out.println("Transaction recorded successfully.");
                    } else {
                        System.out.println("Failed to record transaction.");
                    }
                } else {
                    System.out.println("Failed to update account balance.");
                }
            } else {
                // Account doesn't exist, create a new account
                String insertAccountQuery = "INSERT INTO accounts (user_id, account_number, balance) VALUES (?, ?, ?)";
                PreparedStatement insertAccountStatement = connection.prepareStatement(insertAccountQuery, Statement.RETURN_GENERATED_KEYS);

                insertAccountStatement.setInt(1, userId);
                insertAccountStatement.setString(2, accountNumber);
                insertAccountStatement.setDouble(3, amount);
                int result = insertAccountStatement.executeUpdate();

                if (result > 0) {
                    System.out.println("Account added successfully.");

                    // Get the last inserted account ID
                    ResultSet generatedKeys = insertAccountStatement.getGeneratedKeys();
                    int accountId = -1;
                    if (generatedKeys.next()) {
                        accountId = generatedKeys.getInt(1);
                    }

                    // Insert transaction record
                    String insertTransactionQuery = "INSERT INTO transactions (account_id, amount) VALUES (?, ?)";
                    PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery);
                    insertTransactionStatement.setInt(1, accountId);
                    insertTransactionStatement.setInt(2, -1); // Placeholder for receiver account ID
                    insertTransactionStatement.setDouble(2, amount);
                    int transactionResult = insertTransactionStatement.executeUpdate();

                    if (transactionResult > 0) {
                        System.out.println("Transaction recorded successfully.");
                    } else {
                        System.out.println("Failed to record transaction.");
                    }
                } else {
                    System.out.println("Failed to add account.");
                }
            }
        } else {
            // Invalid social security number or password
            System.out.println("Invalid social security number or password.");
        }

        connection.close();
    }

    public static void removeAccount() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter social security number: ");
        String socialSecurityNumber = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Verify user credentials
        String verifyUserQuery = "SELECT * FROM users WHERE social_security_number = ? AND password = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, socialSecurityNumber);
        verifyUserStatement.setString(2, password);
        ResultSet resultSet = verifyUserStatement.executeQuery();

        if (resultSet.next()) {
            // User logged in successfully
            int userId = resultSet.getInt("user_id");

            System.out.print("Enter account number to remove: ");
            String accountNumber = scanner.nextLine();

            // Check if the account exists for the user
            String checkAccountQuery = "SELECT * FROM accounts WHERE user_id = ? AND account_number = ?";
            PreparedStatement checkAccountStatement = connection.prepareStatement(checkAccountQuery);
            checkAccountStatement.setInt(1, userId);
            checkAccountStatement.setString(2, accountNumber);
            ResultSet accountResultSet = checkAccountStatement.executeQuery();

            if (accountResultSet.next()) {
                // Account exists, delete the account and associated transactions
                int accountId = accountResultSet.getInt("account_id");

                // Delete the account from the accounts table
                String deleteAccountQuery = "DELETE FROM accounts WHERE account_id = ?";
                PreparedStatement deleteAccountStatement = connection.prepareStatement(deleteAccountQuery);
                deleteAccountStatement.setInt(1, accountId);
                int accountResult = deleteAccountStatement.executeUpdate();

                // Delete the transactions associated with the account from the transactions table
                String deleteTransactionsQuery = "DELETE FROM transactions WHERE account_id = ? ";
                PreparedStatement deleteTransactionsStatement = connection.prepareStatement(deleteTransactionsQuery);
                deleteTransactionsStatement.setInt(1, accountId);
                int transactionsResult = deleteTransactionsStatement.executeUpdate();

                if (accountResult > 0 && transactionsResult > 0) {
                    System.out.println("Account and associated transactions have been removed successfully.");
                } else {
                    System.out.println("Failed to remove account and associated transactions.");
                }
            } else {
                System.out.println("Account not found for the user.");
            }
        } else {
            // Invalid social security number or password
            System.out.println("Invalid social security number or password.");
        }

        connection.close();
    }

    public static void updateUserDetails() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter social security number: ");
        String socialSecurityNumber = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Verify user credentials
        String verifyUserQuery = "SELECT * FROM users WHERE social_security_number = ? AND password = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, socialSecurityNumber);
        verifyUserStatement.setString(2, password);
        ResultSet resultSet = verifyUserStatement.executeQuery();

        if (resultSet.next()) {
            // User logged in successfully
            int userId = resultSet.getInt("user_id");

            // Retrieve current user details
            String getUserQuery = "SELECT * FROM users WHERE user_id = ?";
            PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            getUserStatement.setInt(1, userId);
            ResultSet userResultSet = getUserStatement.executeQuery();

            if (userResultSet.next()) {
                // Display current user details
                System.out.println("Current user details:");
                System.out.println("Social Security Number: " + userResultSet.getString("social_security_number"));
                System.out.println("Name: " + userResultSet.getString("name"));
                System.out.println("Password: " + userResultSet.getString("password"));
                System.out.println("Email: " + userResultSet.getString("email"));
                System.out.println("Phone: " + userResultSet.getString("phone_number"));
                System.out.println("Address: " + userResultSet.getString("address"));

                // Prompt for updated user details
                System.out.println("Enter new user details (leave blank to keep current value):");
                System.out.print("Name: ");
                String newName = scanner.nextLine();

                System.out.print("Password: ");
                String newPassword = scanner.nextLine();

                System.out.print("Email: ");
                String newEmail = scanner.nextLine();

                System.out.print("Phone: ");
                String newPhone = scanner.nextLine();

                System.out.print("Address: ");
                String newAddress = scanner.nextLine();

                // Update user details if new values are provided
                if (!newName.isEmpty()) {
                    userResultSet.updateString("name", newName);
                }

                if (!newPassword.isEmpty()) {
                    userResultSet.updateString("password", newPassword);
                }

                if (!newEmail.isEmpty()) {
                    userResultSet.updateString("email", newEmail);
                }

                if (!newPhone.isEmpty()) {
                    userResultSet.updateString("phone_number", newPhone);
                }

                if (!newAddress.isEmpty()) {
                    userResultSet.updateString("address", newAddress);
                }

                // Commit the updates
                userResultSet.updateRow();

                System.out.println("User details updated successfully.");
            } else {
                System.out.println("User not found.");
            }
        } else {
            // Invalid social security number or password
            System.out.println("Invalid social security number or password.");
        }

        connection.close();
    }


    public static void sendTransaction() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter sender's social security number: ");
        String senderSocialSecurityNumber = scanner.nextLine();

        System.out.print("Enter sender's password: ");
        String senderPassword = scanner.nextLine();

        // Verify sender's credentials
        String verifySenderQuery = "SELECT * FROM users WHERE social_security_number = ? AND password = ?";
        PreparedStatement verifySenderStatement = connection.prepareStatement(verifySenderQuery);
        verifySenderStatement.setString(1, senderSocialSecurityNumber);
        verifySenderStatement.setString(2, senderPassword);
        ResultSet senderResultSet = verifySenderStatement.executeQuery();

        if (senderResultSet.next()) {
            // Sender logged in successfully
            int senderUserId = senderResultSet.getInt("user_id");

            // Retrieve sender's accounts
            String getSenderAccountsQuery = "SELECT * FROM accounts WHERE user_id = ?";
            PreparedStatement getSenderAccountsStatement = connection.prepareStatement(getSenderAccountsQuery);
            getSenderAccountsStatement.setInt(1, senderUserId);
            ResultSet senderAccountsResultSet = getSenderAccountsStatement.executeQuery();

            System.out.println("Sender's Accounts:");
            while (senderAccountsResultSet.next()) {
                int senderAccountId = senderAccountsResultSet.getInt("account_id");
                String accountNumber = senderAccountsResultSet.getString("account_number");
                double balance = senderAccountsResultSet.getDouble("balance");

                System.out.println("Account ID: " + senderAccountId + ", Account Number: " + accountNumber + ", Balance: " + balance);
            }

            // Prompt for the sender's account ID
            System.out.print("Enter the Account ID of the sender: ");
            int senderAccountId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            System.out.print("Enter receiver's social security number: ");
            String receiverSocialSecurityNumber = scanner.nextLine();

            // Verify receiver's existence
            String verifyReceiverQuery = "SELECT * FROM users WHERE social_security_number = ?";
            PreparedStatement verifyReceiverStatement = connection.prepareStatement(verifyReceiverQuery);
            verifyReceiverStatement.setString(1, receiverSocialSecurityNumber);
            ResultSet receiverResultSet = verifyReceiverStatement.executeQuery();

            if (receiverResultSet.next()) {
                // Receiver exists
                int receiverUserId = receiverResultSet.getInt("user_id");

                // Retrieve receiver's accounts
                String getReceiverAccountsQuery = "SELECT * FROM accounts WHERE user_id = ?";
                PreparedStatement getReceiverAccountsStatement = connection.prepareStatement(getReceiverAccountsQuery);
                getReceiverAccountsStatement.setInt(1, receiverUserId);
                ResultSet receiverAccountsResultSet = getReceiverAccountsStatement.executeQuery();

                System.out.println("Receiver's Accounts:");
                while (receiverAccountsResultSet.next()) {
                    int receiverAccountId = receiverAccountsResultSet.getInt("account_id");
                    String accountNumber = receiverAccountsResultSet.getString("account_number");
                    double balance = receiverAccountsResultSet.getDouble("balance");

                    System.out.println("Account ID: " + receiverAccountId + ", Account Number: " + accountNumber + ", Balance: " + balance);
                }

                // Prompt for the receiver's account ID
                System.out.print("Enter the Account ID of the receiver: ");
                int receiverAccountId = scanner.nextInt();

                // Prompt for the transaction amount
                System.out.print("Enter the amount to send: ");
                double amount = scanner.nextDouble();

                // Verify sender's account balance
                String verifyBalanceQuery = "SELECT balance FROM accounts WHERE account_id = ? AND user_id = ?";
                PreparedStatement verifyBalanceStatement = connection.prepareStatement(verifyBalanceQuery);
                verifyBalanceStatement.setInt(1, senderAccountId);
                verifyBalanceStatement.setInt(2, senderUserId);
                ResultSet balanceResultSet = verifyBalanceStatement.executeQuery();

                if (balanceResultSet.next()) {
                    double senderBalance = balanceResultSet.getDouble("balance");

                    if (senderBalance >= amount) {
                        // Update sender's balance
                        double newSenderBalance = senderBalance - amount;
                        String updateSenderBalanceQuery = "UPDATE accounts SET balance = ? WHERE account_id = ?";
                        PreparedStatement updateSenderBalanceStatement = connection.prepareStatement(updateSenderBalanceQuery);
                        updateSenderBalanceStatement.setDouble(1, newSenderBalance);
                        updateSenderBalanceStatement.setInt(2, senderAccountId);
                        updateSenderBalanceStatement.executeUpdate();

                        // Update receiver's balance
                        String updateReceiverBalanceQuery = "UPDATE accounts SET balance = balance + ? WHERE account_id = ?";
                        PreparedStatement updateReceiverBalanceStatement = connection.prepareStatement(updateReceiverBalanceQuery);
                        updateReceiverBalanceStatement.setDouble(1, amount);
                        updateReceiverBalanceStatement.setInt(2, receiverAccountId);
                        updateReceiverBalanceStatement.executeUpdate();

                        // Create transaction record
                        String createTransactionQuery = "INSERT INTO transactions (sender_account_id, receiver_account_id, amount) VALUES (?, ?, ?)";
                        PreparedStatement createTransactionStatement = connection.prepareStatement(createTransactionQuery);
                        createTransactionStatement.setInt(1, senderAccountId);
                        createTransactionStatement.setInt(2, receiverAccountId);
                        createTransactionStatement.setDouble(3, amount);
                        createTransactionStatement.executeUpdate();

                        System.out.println("Transaction sent successfully.");
                    } else {
                        System.out.println("Insufficient balance in the sender's account.");
                    }
                } else {
                    System.out.println("Invalid sender account.");
                }
            } else {
                System.out.println("Receiver not found.");
            }
        } else {
            // Invalid social security number or password
            System.out.println("Invalid social security number or password.");
        }

        connection.close();
    }



    public static void listAccountTransactions() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        System.out.print("Enter start date (yyyy-MM-dd): ");
        String startDate = scanner.nextLine();

        System.out.print("Enter end date (yyyy-MM-dd): ");
        String endDate = scanner.nextLine();

        // Retrieve account ID based on account number
        String getAccountIdQuery = "SELECT account_id FROM accounts WHERE account_number = ?";
        PreparedStatement getAccountIdStatement = connection.prepareStatement(getAccountIdQuery);
        getAccountIdStatement.setString(1, accountNumber);
        ResultSet accountIdResultSet = getAccountIdStatement.executeQuery();

        if (accountIdResultSet.next()) {
            int accountId = accountIdResultSet.getInt("account_id");

            // Retrieve transactions for the specified account and date range
            String getAccountTransactionsQuery = "SELECT * FROM transactions WHERE (account_id=? OR sender_account_id = ? OR receiver_account_id = ?) AND transaction_date_time BETWEEN ? AND ? ORDER BY transaction_date_time";
            PreparedStatement getAccountTransactionsStatement = connection.prepareStatement(getAccountTransactionsQuery);
            getAccountTransactionsStatement.setInt(1, accountId);
            getAccountTransactionsStatement.setInt(2, accountId);
            getAccountTransactionsStatement.setInt(3, accountId);
            getAccountTransactionsStatement.setString(4, startDate);
            getAccountTransactionsStatement.setString(5, endDate);
            ResultSet transactionsResultSet = getAccountTransactionsStatement.executeQuery();

            System.out.println("Account Transactions:");
            while (transactionsResultSet.next()) {
                int transactionId = transactionsResultSet.getInt("transaction_id");
                int accountsId = transactionsResultSet.getInt("account_id");
                int senderAccountId = transactionsResultSet.getInt("sender_account_id");
                int receiverAccountId = transactionsResultSet.getInt("receiver_account_id");
                double amount = transactionsResultSet.getDouble("amount");
                String transactionDateTime = transactionsResultSet.getString("transaction_date_time");

                System.out.println("Transaction ID: " + transactionId);
                System.out.println("Accounts ID: " + accountsId);
                System.out.println("Sender Account ID: " + senderAccountId);
                System.out.println("Receiver Account ID: " + receiverAccountId);
                System.out.println("Amount: " + amount);
                System.out.println("Transaction Date Time: " + transactionDateTime);
                System.out.println("--------------------");
            }
        } else {
            System.out.println("Account not found.");
        }

        connection.close();
    }

    public static void userSummary() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter user's social security number: ");
        String socialSecurityNumber = scanner.nextLine();

        // Retrieve user details
        String getUserQuery = "SELECT * FROM users WHERE social_security_number = ?";
        PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery);
        getUserStatement.setString(1, socialSecurityNumber);
        ResultSet userResultSet = getUserStatement.executeQuery();

        if (userResultSet.next()) {
            int userId = userResultSet.getInt("user_id");
            String name = userResultSet.getString("name");
            String email = userResultSet.getString("email");
            String phoneNumber = userResultSet.getString("phone_number");
            String address = userResultSet.getString("address");

            System.out.println("User Details:");
            System.out.println("User ID: " + userId);
            System.out.println("Name: " + name);
            System.out.println("Email: " + email);
            System.out.println("Phone Number: " + phoneNumber);
            System.out.println("Address: " + address);

            // Retrieve user's associated accounts and amounts
            String getAccountsQuery = "SELECT * FROM accounts WHERE user_id = ?";
            PreparedStatement getAccountsStatement = connection.prepareStatement(getAccountsQuery);
            getAccountsStatement.setInt(1, userId);
            ResultSet accountsResultSet = getAccountsStatement.executeQuery();

            System.out.println("Associated Accounts:");
            while (accountsResultSet.next()) {
                int accountId = accountsResultSet.getInt("account_id");
                String accountNumber = accountsResultSet.getString("account_number");
                double balance = accountsResultSet.getDouble("balance");

                System.out.println("Account ID: " + accountId);
                System.out.println("Account Number: " + accountNumber);
                System.out.println("Balance: " + balance);
                System.out.println("--------------------");
            }
        } else {
            System.out.println("User not found.");
        }

        connection.close();
    }








}



