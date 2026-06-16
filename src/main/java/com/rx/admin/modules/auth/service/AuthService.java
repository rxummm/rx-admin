package com.rx.admin.modules.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.modules.auth.vo.LoginResponseVO;
import com.rx.admin.modules.auth.vo.UserInfoVO;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import com.rx.admin.modules.system.user.mapper.SysUserMenuMapper;
import com.rx.admin.modules.system.user.service.ISysUserService;
import com.rx.admin.modules.system.menu.service.SysMenuService;
import com.rx.admin.modules.monitor.online.service.OnlineUserService;
import com.rx.admin.modules.content.message.service.SysMessageService;
import com.rx.admin.common.metrics.CustomMetricsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService implements IAuthService {

    private final ISysUserService userService;
    private final SysUserMapper userMapper;
    private final SysMenuService menuService;
    private final SysUserMenuMapper sysUserMenuMapper;
    private final OnlineUserService onlineUserService;
    private final SysMessageService messageService;
    private final CustomMetricsService metricsService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(ISysUserService userService, SysUserMapper userMapper, SysMenuService menuService,
                       SysUserMenuMapper sysUserMenuMapper, OnlineUserService onlineUserService,
                       SysMessageService messageService, CustomMetricsService metricsService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.menuService = menuService;
        this.sysUserMenuMapper = sysUserMenuMapper;
        this.onlineUserService = onlineUserService;
        this.messageService = messageService;
        this.metricsService = metricsService;
    }

    @Override
    public LoginResponseVO login(String username, String password) {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            metricsService.recordLoginFailure();
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            metricsService.recordLoginFailure();
            throw new IllegalArgumentException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            metricsService.recordLoginFailure();
            throw new IllegalArgumentException("用户名或密码错误");
        }
        metricsService.recordLoginSuccess();

        StpUtil.login(user.getId());
        onlineUserService.userLoggedIn(StpUtil.getTokenValue(), user.getId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        LoginResponseVO response = new LoginResponseVO();
        response.setToken(tokenInfo.getTokenValue());
        response.setTokenName(tokenInfo.getTokenName());

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setEmail(user.getEmail());

        response.setUserInfo(userInfo);
        return response;
    }

    @Override
    @Transactional
    public void register(String username, String password, String nickname) {
        SysUser existUser = userService.getByUsername(username);
        if (existUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(username);
        userService.validatePassword(password, username, nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus(1);
        userService.save(user);
        userService.assignRole(user.getId(), 2L);
    }

    @Override
    public void logout() {
        onlineUserService.userLoggedOut(StpUtil.getTokenValue());
        StpUtil.logout();
    }

    @Override
    public UserInfoVO getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setGender(user.getGender());
        userInfo.setCreateTime(user.getCreateTime());

        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        userInfo.setRoles(roles);

        List<String> rolePerms = userMapper.selectPermsByUserId(userId);
        List<String> directPerms = sysUserMenuMapper.selectPermsByUserId(userId);
        Set<String> allPerms = new LinkedHashSet<>();
        if (rolePerms != null) allPerms.addAll(rolePerms);
        if (directPerms != null) allPerms.addAll(directPerms);
        userInfo.setPerms(new ArrayList<>(allPerms));

        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String nickname, String email, String phone, Integer gender,
                              String newPassword, String oldPassword) {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        String sanitizedEmail = (email != null && email.isBlank()) ? null : email;
        String sanitizedPhone = (phone != null && phone.isBlank()) ? null : phone;

        if (sanitizedEmail != null && !sanitizedEmail.equals(user.getEmail())) {
            userService.validateEmail(sanitizedEmail);
        }
        if (sanitizedPhone != null && !sanitizedPhone.equals(user.getPhone())) {
            userService.validatePhone(sanitizedPhone);
        }
        boolean passwordChanged = newPassword != null && !newPassword.isBlank();
        if (passwordChanged) {
            if (oldPassword == null || oldPassword.isBlank()) {
                throw new IllegalArgumentException("修改密码必须提供旧密码");
            }
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("旧密码错误");
            }
            userService.validatePassword(newPassword, user.getUsername(), nickname);
        }

        SysUser update = new SysUser();
        update.setId(userId);
        update.setNickname(nickname);
        update.setEmail(sanitizedEmail);
        update.setPhone(sanitizedPhone);
        update.setGender(gender);
        if (passwordChanged) {
            update.setPassword(passwordEncoder.encode(newPassword));
        }
        userService.updateById(update);

        List<String> changes = new ArrayList<>();
        if (nickname != null && !nickname.equals(user.getNickname())) changes.add("昵称");
        if (!Objects.equals(sanitizedEmail, user.getEmail())) changes.add("邮箱");
        if (!Objects.equals(sanitizedPhone, user.getPhone())) changes.add("手机号");
        if (gender != null && !Objects.equals(gender, user.getGender())) changes.add("性别");
        if (passwordChanged) changes.add("密码");
        if (!changes.isEmpty()) {
            String changeItems = String.join("、", changes);
            String timeStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            messageService.sendInfoMessage("个人信息已更新",
                    "您于 " + timeStr + " 更新了：" + changeItems, userId);
            String userLabel = user.getNickname() + "(" + user.getUsername() + ")";
            messageService.sendToRoleUsers("admin", "个人信息已更新",
                    "用户 " + userLabel + " 于 " + timeStr + " 更新了：" + changeItems, userId);
        }
    }

    @Override
    public Map<String, Object> getRouters() {
        Map<String, Object> result = new HashMap<>();
        result.put("menus", menuService.getRouterMenus());
        return result;
    }
}
