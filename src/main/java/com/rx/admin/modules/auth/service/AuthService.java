package com.rx.admin.modules.auth.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import com.rx.admin.modules.system.user.mapper.SysUserMenuMapper;
import com.rx.admin.modules.system.user.service.SysUserService;
import com.rx.admin.modules.system.menu.service.SysMenuService;
import com.rx.admin.modules.monitor.online.service.OnlineUserService;
import com.rx.admin.modules.content.message.service.SysMessageService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AuthService {

    private final SysUserService userService;
    private final SysUserMapper userMapper;
    private final SysMenuService menuService;
    private final SysUserMenuMapper sysUserMenuMapper;
    private final OnlineUserService onlineUserService;
    private final SysMessageService messageService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(SysUserService userService, SysUserMapper userMapper, SysMenuService menuService,
                       SysUserMenuMapper sysUserMenuMapper, OnlineUserService onlineUserService,
                       SysMessageService messageService) {
        this.userService = userService;
        this.userMapper = userMapper;
        this.menuService = menuService;
        this.sysUserMenuMapper = sysUserMenuMapper;
        this.onlineUserService = onlineUserService;
        this.messageService = messageService;
    }

    public Map<String, Object> login(String username, String password) {
        SysUser user = userService.getByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }

        // 登录
        StpUtil.login(user.getId());
        // 记录在线用户
        onlineUserService.userLoggedIn(StpUtil.getTokenValue(), user.getId());
        // 获取 token
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        Map<String, Object> result = new HashMap<>();
        result.put("token", tokenInfo.getTokenValue());
        result.put("tokenName", tokenInfo.getTokenName());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("email", user.getEmail());
        result.put("userInfo", userInfo);

        return result;
    }

    public void register(String username, String password, String nickname) {
        // 检查用户名是否已存在
        SysUser existUser = userService.getByUsername(username);
        if (existUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        // 创建用户，默认为普通用户角色(role_code=user, id=2)
        SysUser user = new SysUser();
        user.setUsername(username);
        SysUserService.validatePassword(password, username, nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname);
        user.setStatus(1);
        userService.save(user);
        // 分配普通用户角色 (role_id=2)
        userService.assignRole(user.getId(), 2L);
    }

    public void logout() {
        onlineUserService.userLoggedOut(StpUtil.getTokenValue());
        StpUtil.logout();
    }

    public Map<String, Object> getUserInfo() {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("email", user.getEmail());
        result.put("phone", user.getPhone());
        result.put("gender", user.getGender());
        result.put("createTime", user.getCreateTime());

        // 获取角色编码列表
        List<String> roles = userMapper.selectRoleCodesByUserId(userId);
        result.put("roles", roles);

        // 获取权限标识列表（角色权限 + 直接授权权限，与 StpInterfaceImpl 保持一致）
        List<String> rolePerms = userMapper.selectPermsByUserId(userId);
        List<String> directPerms = sysUserMenuMapper.selectPermsByUserId(userId);
        Set<String> allPerms = new LinkedHashSet<>();
        if (rolePerms != null) allPerms.addAll(rolePerms);
        if (directPerms != null) allPerms.addAll(directPerms);
        result.put("perms", new ArrayList<>(allPerms));

        return result;
    }

    /**
     * 更新当前用户的个人信息（仅限本人操作），并将变更记录到消息中心。
     * <p>校验规则：
     * <ul>
     *   <li>邮箱：填写则校验格式 + 唯一性</li>
     *   <li>手机号：填写则校验11位手机号格式</li>
     *   <li>密码：填写则 <b>必须先通过旧密码校验</b>，再校验强度（字母开头+含数字+至少6位）</li>
     * </ul>
     *
     * @param newPassword 新密码（可选，null/空表示不修改）
     * @param oldPassword 旧密码（修改密码时必填，验证通过才允许改）
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(String nickname, String email, String phone, Integer gender,
                              String newPassword, String oldPassword) {
        long userId = StpUtil.getLoginIdAsLong();
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // --- 数据清洗：空白统一转为 null ---
        String sanitizedEmail = (email != null && email.isBlank()) ? null : email;
        String sanitizedPhone = (phone != null && phone.isBlank()) ? null : phone;

        // --- 校验：填写了则必须通过格式验证 ---
        if (sanitizedEmail != null && !sanitizedEmail.equals(user.getEmail())) {
            SysUserService.validateEmail(sanitizedEmail);
            // 唯一性校验（测试阶段暂时注释）
            // if (userService.isEmailDuplicate(sanitizedEmail, userId)) {
            //     throw new IllegalArgumentException("该邮箱已被其他用户使用");
            // }
        }
        if (sanitizedPhone != null && !sanitizedPhone.equals(user.getPhone())) {
            SysUserService.validatePhone(sanitizedPhone);
        }
        boolean passwordChanged = newPassword != null && !newPassword.isBlank();
        if (passwordChanged) {
            // ⚠️ 安全要求：改密码必须先验证旧密码。
            // 防止 token 泄露后被攻击者直接改密码永久接管账号。
            if (oldPassword == null || oldPassword.isBlank()) {
                throw new IllegalArgumentException("修改密码必须提供旧密码");
            }
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new IllegalArgumentException("旧密码错误");
            }
            SysUserService.validatePassword(newPassword, user.getUsername(), nickname);
        }

        // --- 组装更新对象 ---
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

        // --- 记录变更到消息中心 ---
        List<String> changes = new ArrayList<>();
        if (nickname != null && !nickname.equals(user.getNickname())) changes.add("昵称");
        if (!Objects.equals(sanitizedEmail, user.getEmail())) changes.add("邮箱");
        if (!Objects.equals(sanitizedPhone, user.getPhone())) changes.add("手机号");
        if (gender != null && !Objects.equals(gender, user.getGender())) changes.add("性别");
        if (passwordChanged) changes.add("密码");
        if (!changes.isEmpty()) {
            String changeItems = String.join("、", changes);
            String timeStr = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // 用户本人看到的消息：使用"您"
            messageService.sendInfoMessage("个人信息已更新",
                    "您于 " + timeStr + " 更新了：" + changeItems, userId);
            // 管理员看到的消息：显示具体用户（排除操作者自身，避免管理员自己操作时重复收到）
            String userLabel = user.getNickname() + "(" + user.getUsername() + ")";
            messageService.sendToRoleUsers("admin", "个人信息已更新",
                    "用户 " + userLabel + " 于 " + timeStr + " 更新了：" + changeItems, userId);
        }
    }

    public Map<String, Object> getRouters() {
        Map<String, Object> result = new HashMap<>();
        result.put("menus", menuService.getRouterMenus());
        return result;
    }
}
