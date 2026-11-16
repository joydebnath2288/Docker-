package com.studentmanagement.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static boolean checkPassword(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            return false;
        }
        try {
            if (BCrypt.checkpw(password, hashedPassword)) {
                return true;
            }
        } catch (IllegalArgumentException ignored) {
            // Not a BCrypt hash, fallback to exact string comparison
        }
        return password.equals(hashedPassword);
    }

    public static void main(String[] args) {
        String pwd = (args.length > 0) ? args[0] : "admin";
        System.out.println(hashPassword(pwd));
    }
}
