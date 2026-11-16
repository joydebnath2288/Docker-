package com.studentmanagement.service;

import com.studentmanagement.beans.Student;
import com.studentmanagement.beans.User;

public interface UserService {

    User authenticate(String username, String password);

    String getLastAuthError();

    User findByUsername(String username);

    boolean usernameExists(String username);

    boolean createUser(User user);

    boolean deleteUser(String username);

    String validateUserCredentials(String username, String password, String confirmPassword);

    String registerStudent(Student student, String confirmPassword);

    boolean isAdmin(User user);

    boolean isStudent(User user);

    boolean isStaff(User user);

    boolean isActive(User user);

    String getDashboardRoute(User user);
}
