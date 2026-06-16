package com.rx.admin.modules.system.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class SysUserServiceTest {

    private final SysUserService sysUserService = new SysUserService(null, null, null);

    @Test
    void validatePassword_shouldAcceptValidPassword() {
        assertDoesNotThrow(() -> sysUserService.validatePassword("Pass1234!", "testuser", "Test"));
    }

    @Test
    void validatePassword_shouldRejectTooShort() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sysUserService.validatePassword("Ab1!", "testuser", "Test"));
        assertTrue(ex.getMessage().contains("密码"));
    }

    @Test
    void validatePassword_shouldRejectNoUppercase() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sysUserService.validatePassword("pass1234!", "testuser", "Test"));
        assertTrue(ex.getMessage().contains("密码"));
    }

    @Test
    void validatePassword_shouldRejectNoLowercase() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sysUserService.validatePassword("PASS1234!", "testuser", "Test"));
        assertTrue(ex.getMessage().contains("密码"));
    }

    @Test
    void validatePassword_shouldRejectNoDigit() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sysUserService.validatePassword("Password!", "testuser", "Test"));
        assertTrue(ex.getMessage().contains("密码"));
    }

    @Test
    void validatePassword_shouldRejectNoSpecialChar() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> sysUserService.validatePassword("Pass1234", "testuser", "Test"));
        assertTrue(ex.getMessage().contains("密码"));
    }

    @Test
    void validatePassword_shouldRejectEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> sysUserService.validatePassword("", "testuser", "Nick"));
    }
}
