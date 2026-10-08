package controller;

import dao.UserDAO;
import model.User;

import java.sql.SQLException;

public class LoginController {

    private final UserDAO userDAO = new UserDAO();

    public User login(String email, String password) throws SQLException {
        return userDAO.login(email, password);
    }

    public User getUserById(int userId) throws SQLException {
        return userDAO.getUserById(userId);
    }

    public void register(String name, String email, String password) throws SQLException {
        userDAO.register(name, email, password);
    }

    public void updateProfile(int userId, String name, String email) throws SQLException {
        userDAO.updateProfile(userId, name, email);
    }

    public void changePassword(int userId, String newPassword) throws SQLException {
        userDAO.changePassword(userId, newPassword);
    }
}