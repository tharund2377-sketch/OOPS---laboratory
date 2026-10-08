package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.*;

public class UserDAO {

    public User login(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = makeUser(rs);
                    if (user.isBlocked()) {
                        throw new IllegalStateException("Your account has been blocked by an Administrator.");
                    }
                    return user;
                }
            }
        }
        return null;
    }

    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return makeUser(rs);
                }
            }
        }
        return null;
    }

    public void register(String name, String email, String password) throws SQLException {
        String sql = "INSERT INTO users(name, email, password, role, is_blocked) VALUES(?, ?, ?, 'CUSTOMER', 0)";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.executeUpdate();
        }
    }

    public void updateProfile(int userId, String name, String email) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
        }
    }

    public void changePassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public void setBlockedStatus(int userId, boolean blocked) throws SQLException {
        String sql = "UPDATE users SET is_blocked = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.open();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, blocked ? 1 : 0);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        }
    }

    public List<User> findAllUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id";
        try (Connection conn = DatabaseConnection.open();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(makeUser(rs));
            }
        }
        return list;
    }

    private User makeUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String password = rs.getString("password");
        String role = rs.getString("role");
        
        boolean blocked = false;
        try {
            blocked = rs.getInt("is_blocked") == 1;
        } catch (SQLException ignored) { }

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, name, email, password, blocked);
        } else {
            return new Customer(id, name, email, password, blocked);
        }
    }
}