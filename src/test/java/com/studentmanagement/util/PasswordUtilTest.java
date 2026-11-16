package com.studentmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PasswordUtil Tests")
class PasswordUtilTest {

    @Test
    @DisplayName("Should hash and verify password successfully with BCrypt")
    void testHashAndCheckPassword() {
        String raw = "admin";
        String hashed = PasswordUtil.hashPassword(raw);

        assertNotNull(hashed);
        assertNotEquals(raw, hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$"));
        assertTrue(PasswordUtil.checkPassword(raw, hashed));
        assertFalse(PasswordUtil.checkPassword("wrong", hashed));
    }

    @Test
    @DisplayName("Should support legacy plaintext password fallback")
    void testPlaintextFallback() {
        assertTrue(PasswordUtil.checkPassword("admin", "admin"));
        assertFalse(PasswordUtil.checkPassword("admin", "wrong"));
        assertFalse(PasswordUtil.checkPassword(null, "admin"));
        assertFalse(PasswordUtil.checkPassword("admin", null));
    }
}
