package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.example.Main.GetConnection;

public class Transaction {
    static Scanner scanner;
    static DatabaseManager databaseManager;
    private int transactionId;
    private int senderAccountId;
    private int receiverAccountId;
    private double amount;
    private String transactionType;
    private LocalDateTime transactionDateTime;

    // Constructor
    public Transaction(int transactionId, int senderAccountId, int receiverAccountId, double amount, String transactionType, LocalDateTime transactionDateTime) {
        this.transactionId = transactionId;
        this.senderAccountId = senderAccountId;
        this.receiverAccountId = receiverAccountId;
        this.amount = amount;
        this.transactionType = transactionType;
        this.transactionDateTime = transactionDateTime;
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
            getAccountTransactionsStatement.setString(4, startDate + " 00:00:00");
            getAccountTransactionsStatement.setString(5, endDate + " 23:59:59");
            ResultSet accountTransactionsResultSet = getAccountTransactionsStatement.executeQuery();

            System.out.println("Account Transactions:");
            while (accountTransactionsResultSet.next()) {
                int transactionId = accountTransactionsResultSet.getInt("transaction_id");
                int senderAccountId = accountTransactionsResultSet.getInt("sender_account_id");
                int receiverAccountId = accountTransactionsResultSet.getInt("receiver_account_id");
                double amount = accountTransactionsResultSet.getDouble("amount");
                String transactionType = accountTransactionsResultSet.getString("transaction_type");
                LocalDateTime transactionDateTime = accountTransactionsResultSet.getTimestamp("transaction_date_time").toLocalDateTime();

                System.out.println("Transaction ID: " + transactionId + ", Sender Account ID: " + senderAccountId + ", Receiver Account ID: " + receiverAccountId + ", Amount: " + amount + ", Transaction Type: " + transactionType + ", Transaction Date Time: " + transactionDateTime);
            }
        } else {
            System.out.println("Invalid account number.");
        }

        connection.close();
    }

    public static void listAllTransactions() throws SQLException {
        Connection connection = GetConnection();

        String getAllTransactionsQuery = "SELECT * FROM transactions ORDER BY transaction_date_time";
        PreparedStatement getAllTransactionsStatement = connection.prepareStatement(getAllTransactionsQuery);
        ResultSet allTransactionsResultSet = getAllTransactionsStatement.executeQuery();

        System.out.println("All Transactions:");
        while (allTransactionsResultSet.next()) {
            int transactionId = allTransactionsResultSet.getInt("transaction_id");
            int senderAccountId = allTransactionsResultSet.getInt("sender_account_id");
            int receiverAccountId = allTransactionsResultSet.getInt("receiver_account_id");
            double amount = allTransactionsResultSet.getDouble("amount");
            String transactionType = allTransactionsResultSet.getString("transaction_type");
            LocalDateTime transactionDateTime = allTransactionsResultSet.getTimestamp("transaction_date_time").toLocalDateTime();

            System.out.println("Transaction ID: " + transactionId + ", Sender Account ID: " + senderAccountId + ", Receiver Account ID: " + receiverAccountId + ", Amount: " + amount + ", Transaction Type: " + transactionType + ", Transaction Date Time: " + transactionDateTime);
        }

        connection.close();
    }
    // Getters
    public int getTransactionId() {
        return transactionId;
    }

    public int getSenderAccountId() {
        return senderAccountId;
    }

    public int getReceiverAccountId() {
        return receiverAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public LocalDateTime getTransactionDateTime() {
        return transactionDateTime;
    }

    // Setters
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setSenderAccountId(int senderAccountId) {
        this.senderAccountId = senderAccountId;
    }

    public void setReceiverAccountId(int receiverAccountId) {
        this.receiverAccountId = receiverAccountId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public void setTransactionDateTime(LocalDateTime transactionDateTime) {
        this.transactionDateTime = transactionDateTime;
    }
}
