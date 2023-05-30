package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Scanner;

import static org.example.Main.GetConnection;

public class User {
    static Scanner scanner;
    static DatabaseManager databaseManager;
    private int userId;
    private String socialSecurityNumber;
    private String password;
    private String name;
    private String email;
    private String address;
    private String phoneNumber;
    private LocalDateTime created;

    // Constructor
    public User(int userId, String socialSecurityNumber, String password, String name, String email, String address, String phoneNumber, LocalDateTime created) {
        this.userId = userId;
        this.socialSecurityNumber = socialSecurityNumber;
        this.password = password;
        this.name = name;
        this.email = email;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.created = created;
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
    // Getters
    public int getUserId() {
        return userId;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    // Setters
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }
}

