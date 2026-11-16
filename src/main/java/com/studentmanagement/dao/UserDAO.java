package com.studentmanagement.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.studentmanagement.beans.User;
import com.studentmanagement.config.DBConnection;

public class UserDAO {

    private void handleException(String method, SQLException e) {
        System.err.println("Error in UserDAO." + method + ": " + e.getMessage());
        e.printStackTrace();
    }

    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT id, username, password, role, status FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRoleId(rs.getInt("role"));
                    user.setStatus(rs.getInt("status"));
                    return user;
                }
            }
        } catch (SQLException e) {
            handleException("findByUsername", e);
        }
        return null;
    }

    public User findById(int id) {
        if (id <= 0) {
            return null;
        }

        String sql = "SELECT id, username, password, role, status FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setUsername(rs.getString("username"));
                    user.setPassword(rs.getString("password"));
                    user.setRoleId(rs.getInt("role"));
                    user.setStatus(rs.getInt("status"));
                    return user;
                }
            }
        } catch (SQLException e) {
            handleException("findById", e);
        }
        return null;
    }

    public boolean usernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "SELECT 1 FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            handleException("usernameExists", e);
        }
        return false;
    }

    public boolean createUser(User user) {
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return false;
        }

        String sql = "INSERT INTO users (username, password, role, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getRoleId() > 0 ? user.getRoleId() : 2);
            ps.setInt(4, user.getStatus() >= 0 ? user.getStatus() : 1);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setId(keys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            handleException("createUser", e);
        }
        return false;
    }

    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) {
            return false;
        }

        String sql = "UPDATE users SET username = ?, password = ?, role = ?, status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPassword());
            ps.setInt(3, user.getRoleId() > 0 ? user.getRoleId() : 2);
            ps.setInt(4, user.getStatus() >= 0 ? user.getStatus() : 1);
            ps.setInt(5, user.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            handleException("updateUser", e);
        }
        return false;
    }

    public boolean deleteUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }

        String sql = "DELETE FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username.trim());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            handleException("deleteUser", e);
        }
        return false;
    }

    public boolean deleteUserById(int id) {
        if (id <= 0) {
            return false;
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            handleException(" UserById", e);
        }
        return false;
    }
}
