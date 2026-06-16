package com.rx.admin.modules.auth.service;

import com.rx.admin.common.metrics.CustomMetricsService;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import com.rx.admin.modules.system.user.mapper.SysUserMenuMapper;
import com.rx.admin.modules.system.user.service.ISysUserService;
import com.rx.admin.modules.system.menu.service.SysMenuService;
import com.rx.admin.modules.monitor.online.service.OnlineUserService;
import com.rx.admin.modules.content.message.service.SysMessageService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private ISysUserService userService;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysMenuService menuService;
    @Mock
    private SysUserMenuMapper sysUserMenuMapper;
    @Mock
    private OnlineUserService onlineUserService;
    @Mock
    private SysMessageService messageService;

    private AuthService authService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final CustomMetricsService metricsService = new CustomMetricsService(new SimpleMeterRegistry());

    @BeforeEach
    void setUp() {
        authService = new AuthService(userService, userMapper, menuService,
                sysUserMenuMapper, onlineUserService, messageService, metricsService);
    }

    @Test
    void login_shouldThrowWhenUserNotFound() {
        when(userService.getByUsername(anyString())).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> authService.login("unknown", "pass"));
    }

    @Test
    void login_shouldThrowWhenUserDisabled() {
        SysUser user = new SysUser();
        user.setStatus(0);
        user.setPassword(encoder.encode("pass"));
        when(userService.getByUsername(anyString())).thenReturn(user);
        assertThrows(IllegalArgumentException.class,
                () -> authService.login("disabled", "pass"));
    }

    @Test
    void login_shouldThrowWhenWrongPassword() {
        SysUser user = new SysUser();
        user.setStatus(1);
        user.setPassword(encoder.encode("correctPass1!"));
        when(userService.getByUsername(anyString())).thenReturn(user);
        assertThrows(IllegalArgumentException.class,
                () -> authService.login("user", "wrongPass1!"));
    }

    @Test
    void register_shouldThrowWhenUsernameExists() {
        when(userService.getByUsername(anyString())).thenReturn(new SysUser());
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("existing", "Pass1234!", "nick"));
    }

    @Test
    void register_shouldThrowWhenPasswordInvalid() {
        when(userService.getByUsername(anyString())).thenReturn(null);
        doThrow(new IllegalArgumentException("密码需以字母开头，包含数字，至少6位"))
                .when(userService).validatePassword(anyString(), anyString(), anyString());
        assertThrows(IllegalArgumentException.class,
                () -> authService.register("newuser", "weak", "nick"));
    }
}
