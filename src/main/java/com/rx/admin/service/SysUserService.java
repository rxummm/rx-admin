package com.rx.admin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.common.PageResult;
import com.rx.admin.entity.SysUser;
import com.rx.admin.entity.SysRole;
import com.rx.admin.mapper.SysRoleMapper;
import com.rx.admin.mapper.SysUserMapper;
import com.rx.admin.mapper.SysUserRoleMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SysUserService extends ServiceImpl<SysUserMapper, SysUser> {

    private final SysUserRoleMapper userRoleMapper;
    private final SysMessageService sysMessageService;
    private final SysRoleMapper sysRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysUserService(SysUserRoleMapper userRoleMapper,
                          SysMessageService sysMessageService,
                          SysRoleMapper sysRoleMapper) {
        this.userRoleMapper = userRoleMapper;
        this.sysMessageService = sysMessageService;
        this.sysRoleMapper = sysRoleMapper;
    }

    public PageResult<SysUser> pageQuery(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getPhone, keyword));
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        IPage<SysUser> iPage = page(new Page<>(page, size), wrapper);
        // 清除密码字段
        iPage.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(iPage.getTotal(), iPage.getCurrent(), iPage.getSize(), iPage.getRecords());
    }

    @Transactional
    public void addUser(SysUser user, List<Long> roleIds) {
        // 检查用户名是否存在
        long count = count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        // 检查邮箱是否已被使用（测试阶段暂时注释）
        // if (StringUtils.hasText(user.getEmail())) {
        //     if (isEmailDuplicate(user.getEmail(), null)) {
        //         throw new IllegalArgumentException("该邮箱已被其他用户使用");
        //     }
        // }
        if (!StringUtils.hasText(user.getEmail())) {
            user.setEmail(null);
        }
        // 空白手机号转为 null，避免加密后长度超限
        if (!StringUtils.hasText(user.getPhone())) {
            user.setPhone(null);
        }
        // 密码策略校验
        validatePassword(user.getPassword(), user.getUsername(), user.getNickname());
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        save(user);
        // 分配角色
        if (roleIds != null && !roleIds.isEmpty()) {
            roleIds.forEach(roleId -> userRoleMapper.insert(user.getId(), roleId));
        }
        // 发送欢迎消息
        try {
            String roleNamesStr = getRoleNames(roleIds);
            String content = "您的账号已创建成功，用户名：" + user.getUsername();
            if (!roleNamesStr.isEmpty()) {
                content += "，角色：" + roleNamesStr;
            }
            sysMessageService.sendSystemMessage("欢迎加入系统", content, user.getId());
        } catch (Exception e) {
            log.warn("发送欢迎消息失败: userId={}, error={}", user.getId(), e.getMessage());
        }
    }

    @Transactional
    public void updateUser(SysUser user, List<Long> roleIds) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        SysUser existing = getById(user.getId());
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 检测变更
        boolean passwordChanged = StringUtils.hasText(user.getPassword());
        boolean statusChanged = user.getStatus() != null && !user.getStatus().equals(existing.getStatus());
        boolean rolesChanged = roleIds != null && !roleIds.isEmpty();
        List<Long> oldRoleIds = rolesChanged ? userRoleMapper.selectRoleIdsByUserId(user.getId()) : null;
        // 只保留有值的字段进行更新
        SysUser update = new SysUser();
        update.setId(user.getId());
        // 如果修改了密码
        if (StringUtils.hasText(user.getPassword())) {
            validatePassword(user.getPassword(), existing.getUsername(), existing.getNickname());
            update.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        // 只更新非空字段（避免将未传字段误置为 null）
        if (StringUtils.hasText(user.getNickname())) update.setNickname(user.getNickname());
        if (StringUtils.hasText(user.getEmail())) {
            // 检查邮箱是否被其他用户使用（测试阶段暂时注释）
            // if (!user.getEmail().equals(existing.getEmail()) && isEmailDuplicate(user.getEmail(), user.getId())) {
            //     throw new IllegalArgumentException("该邮箱已被其他用户使用");
            // }
            update.setEmail(user.getEmail());
        } else if (user.getEmail() != null) {
            // 空白字符串转为 null
            update.setEmail(null);
        }
        if (StringUtils.hasText(user.getPhone())) {
            update.setPhone(user.getPhone());
        } else if (user.getPhone() != null) {
            // 空白字符串转为 null，避免加密后长度超限
            update.setPhone(null);
        }
        if (StringUtils.hasText(user.getAvatar())) update.setAvatar(user.getAvatar());
        if (user.getGender() != null) update.setGender(user.getGender());
        if (user.getStatus() != null) update.setStatus(user.getStatus());
        if (StringUtils.hasText(user.getRemark())) update.setRemark(user.getRemark());
        updateById(update);
        // 更新角色
        if (roleIds != null && !roleIds.isEmpty()) {
            userRoleMapper.deleteByUserId(user.getId());
            roleIds.forEach(roleId -> userRoleMapper.insert(user.getId(), roleId));
        }
        // 如果修改了密码，踢出该用户的所有旧会话（不影响当前操作者）
        try {
            if (passwordChanged) {
                StpUtil.kickout(user.getId());
            }
        } catch (Exception e) {
            System.err.println("kickout failed: userId=" + user.getId() + " " + e.getMessage());
        }
        // 发送消息通知（在 kickout 之后执行，不影响消息落库）
        try {
            if (passwordChanged) {
                sysMessageService.sendSystemMessage(
                        "密码修改通知",
                        "您的账号密码已被管理员修改，如非本人操作请立即联系管理员。",
                        user.getId());
                log.info("密码修改通知已发送: userId={}", user.getId());
            }
            if (statusChanged) {
                if (user.getStatus() == 1) {
                    sysMessageService.sendSystemMessage(
                            "账号已启用",
                            "您的账号已被启用，现在可以正常登录系统。",
                            user.getId());
                } else {
                    sysMessageService.sendSystemMessage(
                            "账号已禁用",
                            "您的账号已被禁用，如有疑问请联系管理员。",
                            user.getId());
                }
                log.info("状态变更通知已发送: userId={}, newStatus={}", user.getId(), user.getStatus());
            }
            if (rolesChanged) {
                String msg = buildRoleChangeMessage(oldRoleIds, roleIds);
                sysMessageService.sendSystemMessage("角色变更通知", msg, user.getId());
                log.info("角色变更通知已发送: userId={}", user.getId());
            }
        } catch (Exception e) {
            log.warn("发送消息通知失败: userId={}, error={}", user.getId(), e.getMessage());
        }
    }

    @Transactional
    public void deleteUser(Long id) {
        // 不能删除自己
        long currentUserId = StpUtil.getLoginIdAsLong();
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不能删除自己");
        }
        userRoleMapper.deleteByUserId(id);
        removeById(id);
    }

    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    /**
     * 检查邮箱是否已被其他用户使用
     * @param email 待检查的邮箱（明文）
     * @param excludeUserId 排除的用户ID（更新时排除自身），传 null 表示不排除
     * @return true=邮箱已存在
     */
    public boolean isEmailDuplicate(String email, Long excludeUserId) {
        if (!StringUtils.hasText(email)) {
            return false;
        }
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, email);
        if (excludeUserId != null) {
            wrapper.ne(SysUser::getId, excludeUserId);
        }
        return count(wrapper) > 0;
    }

    public void assignRole(Long userId, Long roleId) {
        userRoleMapper.insert(userId, roleId);
    }

    /** 密码策略：至少6位，包含字母和数字 */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[A-Za-z](?=.*\\d).{5,}$");

    /** 邮箱格式 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /** 手机号格式（中国大陆） */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    public static void validatePassword(String password, String username, String nickname) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException("密码需以字母开头，包含数字，至少6位");
        }
        if (password.equalsIgnoreCase(username)) {
            throw new IllegalArgumentException("密码不能与用户名相同");
        }
        if (nickname != null && password.equalsIgnoreCase(nickname)) {
            throw new IllegalArgumentException("密码不能与昵称相同");
        }
    }

    /** 邮箱格式校验 */
    public static void validateEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("邮箱长度不能超过100个字符");
        }
    }

    /** 手机号格式校验 */
    public static void validatePhone(String phone) {
        if (!StringUtils.hasText(phone)) {
            throw new IllegalArgumentException("手机号不能为空");
        }
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确，请输入11位有效手机号");
        }
    }

    /**
     * 构建角色变更消息：对比新旧角色ID，列出新增和移除的角色
     */
    private String buildRoleChangeMessage(List<Long> oldRoleIds, List<Long> newRoleIds) {
        Set<Long> oldSet = new HashSet<>(oldRoleIds != null ? oldRoleIds : List.of());
        Set<Long> newSet = new HashSet<>(newRoleIds != null ? newRoleIds : List.of());

        Set<Long> added = new HashSet<>(newSet);
        added.removeAll(oldSet);
        Set<Long> removed = new HashSet<>(oldSet);
        removed.removeAll(newSet);

        StringBuilder sb = new StringBuilder("您的角色已被管理员更新.");
        if (!added.isEmpty()) {
            sb.append(" 新增：").append(getRoleNames(new ArrayList<>(added)));
        }
        if (!removed.isEmpty()) {
            sb.append(" 移除：").append(getRoleNames(new ArrayList<>(removed)));
        }
        String newNames = getRoleNames(newRoleIds);
        sb.append(" 当前角色：").append(newNames.isEmpty() ? "无" : newNames);
        return sb.toString();
    }

    /**
     * 根据角色ID列表获取角色名称字符串（用顿号分隔）
     */
    private String getRoleNames(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return "";
        List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
        return roles.stream().map(SysRole::getRoleName).collect(Collectors.joining("、"));
    }
}
