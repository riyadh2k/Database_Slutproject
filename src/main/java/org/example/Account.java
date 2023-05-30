package org.example;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.example.Main.GetConnection;

public class Account {
    static Scanner scanner;
    static DatabaseManager databaseManager;
    private int accountId;
    private int userId;
    private int accountNumber;
    private double balance;
    private LocalDateTime created;

    // Constructor
    public Account(int accountId, int userId, int accountNumber, double balance, LocalDateTime created) {
        this.accountId = accountId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.created = created;
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

    // Getters
    public int getAccountId() {
        return accountId;
    }

    public int getUserId() {
        return userId;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    // Setters
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}
