package org.example;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.example.Main.GetConnection;

public class PasswordModel {
    public static String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : encodedHash) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }

        return hexString.toString();
    }

    public static boolean authenticateUser(String username, String password) {
        try (Connection connection = GetConnection();
             PreparedStatement verifyUserStatement = connection.prepareStatement("SELECT social_security_number, password FROM users WHERE social_security_number = ?")) {

            verifyUserStatement.setString(1, username);
            try (ResultSet resultSet = verifyUserStatement.executeQuery()) {
                if (resultSet.next()) {
                    String storedSocialSecurityNumber = resultSet.getString("social_security_number");
                    String storedHashedPassword = resultSet.getString("password");

                    // Hash the provided password
                    String hashedPassword = hashPassword(password);

                    // Compare the hashed passwords
                    return storedSocialSecurityNumber.equals(username) && storedHashedPassword.equals(hashedPassword);
                }
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return false;
    }
}
