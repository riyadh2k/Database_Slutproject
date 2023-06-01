package org.example;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.example.PasswordModel.hashPassword;


public class Main {
    static Scanner scanner;
    static MysqlDataSource dataSource;
    static String url = "localhost";
    static int port = 3307;
    static String database = "slutproject";
    static String username = "root";
    static String password = "";
    static String userSocialSecurityNumber;

    public static void main(String[] args) throws SQLException, NoSuchAlgorithmException {
        scanner = new Scanner(System.in);

        InitializeDatabase();
        CreateTable();
        boolean run = true;

        while (run) {
            System.out.println("Choose what you want to do.");
            System.out.println("1. Create user");
            System.out.println("2. Remove user");
            System.out.println("3. Login");
            System.out.println("4. Exit");

            switch (scanner.nextLine().trim()) {
                case "1":
                    createUser();
                    break;

                case "2":
                    removeUser();
                    break;

                case "3":
                    System.out.print("Enter Social Security Number: ");
                    username = scanner.nextLine();
                    System.out.print("Enter password: ");
                    password = scanner.nextLine();

                    if (PasswordModel.authenticateUser(username, password)) {
                        System.out.println("Authentication successful. You are logged in.");
                        userSocialSecurityNumber = username;
                        userMenu();
                    } else {
                        System.out.println("Authentication failed. Please try again.");
                    }
                    break;


                case "4":
                    run = false;
                    break;

                default:
                    break;
            }

        }
    }
    private static void userMenu() throws SQLException, NoSuchAlgorithmException {
        boolean run = true;
        while (run) {
            System.out.println("Choose what you want to do.");
            System.out.println("1. Add Account");
            System.out.println("2. Remove Account");
            System.out.println("3. Update User's Information");
            System.out.println("4. Send Amount");
            System.out.println("5. List Of Account Transaction");
            System.out.println("6. User's Summary");
            System.out.println("7. Logout");

            switch (scanner.nextLine().trim()) {
                case "1":
                    addAccount();
                    break;

                case "2":
                    removeAccount();
                    break;

                case "3":
                    updateUserDetails();
                    break;

                case "4":
                    sendTransaction();
                    break;

                case "5":
                    listAccountTransactions();
                    break;

                case "6":
                    userSummary();
                    break;

                case "7":
                    run = false;
                    System.out.println("Logged out successfully.");
                    break;

                default:
                    break;
            }
        }
    }

    //Configure connections to the database
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

    static void PrintSQLException(SQLException e) {
    }

    //Creates a temporary connection to the database
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
                + "sender_account_id INT , "
                + "receiver_account_id INT , "
                + "amount DOUBLE NOT NULL, "
                + "transaction_type VARCHAR(50), " // Add transaction_type column
                + "transaction_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                + "FOREIGN KEY (sender_account_id) REFERENCES accounts(account_id), "
                + "FOREIGN KEY (receiver_account_id) REFERENCES accounts(account_id)"
                + ")";


        int resultTransactionsTable = statement.executeUpdate(createTransactionsTableQuery);
        System.out.println("Transactions table creation result: " + resultTransactionsTable);

        connection.close();
    }
    public static void createUser() throws SQLException, NoSuchAlgorithmException {
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

        // Hash the password
        String hashedPassword = hashPassword(password);

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter address: ");
        String address = scanner.nextLine();

        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();

        String insertUserQuery = "INSERT INTO users (social_security_number, password, name, email, address, phone_number) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement insertUserStatement = connection.prepareStatement(insertUserQuery);
        insertUserStatement.setString(1, socialSecurityNumber);
        insertUserStatement.setString(2, hashedPassword);
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

        System.out.print("Enter Social Security Number: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        // Verify user credentials
        if (PasswordModel.authenticateUser(username, password)) {
            // User authentication successful

            // Get the user ID
            String getUserIdQuery = "SELECT user_id FROM users WHERE social_security_number = ?";
            PreparedStatement getUserIdStatement = connection.prepareStatement(getUserIdQuery);
            getUserIdStatement.setString(1, username);
            ResultSet userIdResultSet = getUserIdStatement.executeQuery();
            userIdResultSet.next();
            int userId = userIdResultSet.getInt("user_id");

            // Delete associated transactions
            String deleteTransactionsQuery = "DELETE FROM transactions WHERE sender_account_id IN (SELECT account_id FROM accounts WHERE user_id = ?) OR receiver_account_id IN (SELECT account_id FROM accounts WHERE user_id = ?)";
            PreparedStatement deleteTransactionsStatement = connection.prepareStatement(deleteTransactionsQuery);
            deleteTransactionsStatement.setInt(1, userId);
            deleteTransactionsStatement.setInt(2, userId);
            deleteTransactionsStatement.executeUpdate();

            // Delete associated accounts
            String deleteAccountsQuery = "DELETE FROM accounts WHERE user_id = ?";
            PreparedStatement deleteAccountsStatement = connection.prepareStatement(deleteAccountsQuery);
            deleteAccountsStatement.setInt(1, userId);
            deleteAccountsStatement.executeUpdate();

            // Delete user
            String deleteUserQuery = "DELETE FROM users WHERE social_security_number = ?";
            PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery);
            deleteUserStatement.setString(1, username);
            int result = deleteUserStatement.executeUpdate();

            if (result > 0) {
                System.out.println("User and associated records removed successfully.");
            } else {
                System.out.println("Failed to remove user.");
            }
        } else {
            System.out.println("Invalid username or password.");
        }

        connection.close();
    }

    public static void addAccount() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        System.out.print("Enter amount: ");
        double amount = scanner.nextDouble();

        // Verify user credentials
        String verifyUserQuery = "SELECT * FROM users WHERE social_security_number = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, userSocialSecurityNumber);
        ResultSet resultSet = verifyUserStatement.executeQuery();

        if (resultSet.next()) {
            // User logged in successfully
            int userId = resultSet.getInt("user_id");

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
                    String insertTransactionQuery = "INSERT INTO transactions (sender_account_id, receiver_account_id, amount, transaction_type) VALUES (?, ?, ?, 'Deposit')";
                    PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery);
                    insertTransactionStatement.setInt(1, accountId);
                    insertTransactionStatement.setInt(2, accountId); // Sender and receiver are the same account
                    insertTransactionStatement.setDouble(3, amount);
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
                    String insertTransactionQuery = "INSERT INTO transactions (sender_account_id, receiver_account_id, amount, transaction_type) VALUES (?, ?, ?, 'Deposit')";
                    PreparedStatement insertTransactionStatement = connection.prepareStatement(insertTransactionQuery);
                    insertTransactionStatement.setInt(1, accountId);
                    insertTransactionStatement.setInt(2, accountId); // Sender and receiver are the same account
                    insertTransactionStatement.setDouble(3, amount);
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
            // Invalid social security number
            System.out.println("Invalid social security number.");
        }

        connection.close();
    }

    public static void removeAccount() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter account number to remove: ");
        String accountNumber = scanner.nextLine();

        // Verify user credentials
        String verifyUserQuery = "SELECT * FROM users u JOIN accounts a ON u.user_id = a.user_id WHERE u.social_security_number = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, userSocialSecurityNumber);
        ResultSet resultSet = verifyUserStatement.executeQuery();

        if (resultSet.next()) {
            // User logged in successfully
            int accountId = resultSet.getInt("account_id");

            // Check if the account exists for the user
            String checkAccountQuery = "SELECT * FROM accounts WHERE user_id = ? AND account_number = ?";
            PreparedStatement checkAccountStatement = connection.prepareStatement(checkAccountQuery);
            checkAccountStatement.setInt(1, accountId);
            checkAccountStatement.setString(2, accountNumber);
            ResultSet accountResultSet = checkAccountStatement.executeQuery();

            if (accountResultSet.next()) {
                // Account exists, delete the account and associated transactions
                accountId = accountResultSet.getInt("account_id");

                // Delete associated transactions
                String deleteTransactionsQuery = "DELETE FROM transactions WHERE sender_account_id = ? OR receiver_account_id = ?";
                PreparedStatement deleteTransactionsStatement = connection.prepareStatement(deleteTransactionsQuery);
                deleteTransactionsStatement.setInt(1, accountId);
                deleteTransactionsStatement.setInt(2, accountId);
                int transactionsResult = deleteTransactionsStatement.executeUpdate();

                // Delete the account
                String deleteAccountQuery = "DELETE FROM accounts WHERE account_id = ?";
                PreparedStatement deleteAccountStatement = connection.prepareStatement(deleteAccountQuery);
                deleteAccountStatement.setInt(1, accountId);
                int accountResult = deleteAccountStatement.executeUpdate();

                if (accountResult > 0 && transactionsResult > 0) {
                    System.out.println("Account and associated transactions have been removed successfully.");
                } else {
                    System.out.println("Failed to remove account and associated transactions.");
                }
            } else {
                System.out.println("Account not found for the user.");
            }
        } else {
            // Invalid social security number
            System.out.println("Account not found for the user.");
        }

        connection.close();
    }

    public static void updateUserDetails() throws SQLException, NoSuchAlgorithmException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        // Retrieve current user details
        String getUserQuery = "SELECT * FROM users WHERE social_security_number = ?";
        PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        getUserStatement.setString(1, userSocialSecurityNumber);
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

            if (!newEmail.isEmpty()) {
                userResultSet.updateString("email", newEmail);
            }

            if (!newPhone.isEmpty()) {
                userResultSet.updateString("phone_number", newPhone);
            }

            if (!newAddress.isEmpty()) {
                userResultSet.updateString("address", newAddress);
            }

            // Update the password if a new password is provided
            System.out.print("New password: ");
            String newPassword = scanner.nextLine();
            if (!newPassword.isEmpty()) {
                // Hash the new password
                String hashedPassword = PasswordModel.hashPassword(newPassword);
                userResultSet.updateString("password", hashedPassword);
            }

            // Commit the updates
            userResultSet.updateRow();

            System.out.println("User details updated successfully.");
        } else {
            // User not found
            System.out.println("User not found.");
        }

        connection.close();
    }


    public static void sendTransaction() throws SQLException {
        Connection connection = GetConnection();

        Scanner scanner = new Scanner(System.in);

        // Assuming the sender is already logged in and their social security number is available

        // Retrieve sender's details using the social security number
        String getSenderQuery = "SELECT * FROM users WHERE social_security_number = ?";
        PreparedStatement getSenderStatement = connection.prepareStatement(getSenderQuery);
        getSenderStatement.setString(1, userSocialSecurityNumber); // Use the available socialSecurityNumber
        ResultSet senderResultSet = getSenderStatement.executeQuery();

        if (senderResultSet.next()) {
            // Sender exists and is logged in successfully
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

                boolean receiverHasAccounts = false; // Flag to track if receiver has accounts

                System.out.println("Receiver's Accounts:");
                while (receiverAccountsResultSet.next()) {
                    int receiverAccountId = receiverAccountsResultSet.getInt("account_id");
                    String accountNumber = receiverAccountsResultSet.getString("account_number");

                    System.out.println("Account ID: " + receiverAccountId + ", Account Number: " + accountNumber);

                    receiverHasAccounts = true; // Set the flag to true if receiver has accounts
                }

                if (receiverHasAccounts) {
                    // Prompt for the receiver's account ID
                    System.out.print("Enter the Account ID of the receiver: ");
                    int receiverAccountId = scanner.nextInt();

                    // Verify receiver's account existence
                    String verifyReceiverAccountQuery = "SELECT * FROM accounts WHERE account_id = ?";
                    PreparedStatement verifyReceiverAccountStatement = connection.prepareStatement(verifyReceiverAccountQuery);
                    verifyReceiverAccountStatement.setInt(1, receiverAccountId);
                    ResultSet receiverAccountResultSet = verifyReceiverAccountStatement.executeQuery();

                    if (receiverAccountResultSet.next()) {
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
                                String createTransactionQuery = "INSERT INTO transactions (sender_account_id, receiver_account_id, amount, transaction_type) VALUES (?, ?, ?, 'Transfer')";
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
                        System.out.println("Invalid receiver account.");
                    }
                } else {
                    System.out.println("No accounts associated with the receiver.");
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



        // Verify user credentials
        String verifyUserQuery = "SELECT user_id FROM users WHERE social_security_number = ?";
        PreparedStatement verifyUserStatement = connection.prepareStatement(verifyUserQuery);
        verifyUserStatement.setString(1, userSocialSecurityNumber);
        ResultSet userResultSet = verifyUserStatement.executeQuery();

        if (userResultSet.next()) {
            int loggedInUserId = userResultSet.getInt("user_id");

            // Retrieve account ID based on account number and user ID
            String getAccountIdQuery = "SELECT account_id FROM accounts WHERE account_number = ? AND user_id = ?";
            PreparedStatement getAccountIdStatement = connection.prepareStatement(getAccountIdQuery);
            getAccountIdStatement.setString(1, accountNumber);
            getAccountIdStatement.setInt(2, loggedInUserId);
            ResultSet accountIdResultSet = getAccountIdStatement.executeQuery();

            if (accountIdResultSet.next()) {
                int accountId = accountIdResultSet.getInt("account_id");

                System.out.print("Enter start date (yyyy-MM-dd): ");
                String startDate = scanner.nextLine();

                System.out.print("Enter end date (yyyy-MM-dd): ");
                String endDate = scanner.nextLine();

                // Retrieve transactions for the specified account, user, and date range
                String getAccountTransactionsQuery = "SELECT t.*, s.account_number AS sender_account_number, r.account_number AS receiver_account_number FROM transactions t " +
                        "JOIN accounts s ON t.sender_account_id = s.account_id " +
                        "JOIN accounts r ON t.receiver_account_id = r.account_id " +
                        "WHERE (s.account_id = ? OR r.account_id = ?) AND s.user_id = ? AND t.transaction_date_time BETWEEN ? AND ? " +
                        "ORDER BY t.transaction_date_time";
                PreparedStatement getAccountTransactionsStatement = connection.prepareStatement(getAccountTransactionsQuery);
                getAccountTransactionsStatement.setInt(1, accountId);
                getAccountTransactionsStatement.setInt(2, accountId);
                getAccountTransactionsStatement.setInt(3, loggedInUserId);
                getAccountTransactionsStatement.setString(4, startDate + " 00:00:00");
                getAccountTransactionsStatement.setString(5, endDate + " 23:59:59");
                ResultSet accountTransactionsResultSet = getAccountTransactionsStatement.executeQuery();

                System.out.println("Account Transactions:");
                while (accountTransactionsResultSet.next()) {
                    int transactionId = accountTransactionsResultSet.getInt("transaction_id");
                    String senderAccountNumber = accountTransactionsResultSet.getString("sender_account_number");
                    String receiverAccountNumber = accountTransactionsResultSet.getString("receiver_account_number");
                    double amount = accountTransactionsResultSet.getDouble("amount");
                    String transactionType = accountTransactionsResultSet.getString("transaction_type");
                    LocalDateTime transactionDateTime = accountTransactionsResultSet.getTimestamp("transaction_date_time").toLocalDateTime();

                    System.out.println("_________________________");
                    System.out.println("Transaction ID: " + transactionId);
                    System.out.println("Sender Account Number: " + senderAccountNumber);
                    System.out.println("Receiver Account Number: " + receiverAccountNumber);
                    System.out.println("Amount: " + amount);
                    System.out.println("Transaction Type: " + transactionType);
                    System.out.println("Transaction Date Time: " + transactionDateTime);
                    System.out.println("_________________________");
                }
            } else {
                System.out.println("Account not found.");
            }
        } else {
            System.out.println("Invalid social security number.");
        }

        connection.close();
    }


    public static void userSummary() throws SQLException {
        Connection connection = GetConnection();

        // Retrieve user details
        String getUserQuery = "SELECT * FROM users WHERE social_security_number = ?";
        PreparedStatement getUserStatement = connection.prepareStatement(getUserQuery);
        getUserStatement.setString(1, userSocialSecurityNumber);
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