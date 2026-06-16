package com.rx.admin.modules.system.permission.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rx.admin.modules.system.menu.entity.SysMenu;
import com.rx.admin.modules.content.notice.entity.SysNotice;
import com.rx.admin.modules.system.permission.entity.SysPermissionRequest;
import com.rx.admin.modules.system.user.entity.SysUser;
import com.rx.admin.modules.system.menu.mapper.SysMenuMapper;
import com.rx.admin.modules.system.permission.mapper.SysPermissionRequestMapper;
import com.rx.admin.modules.system.user.mapper.SysUserMapper;
import com.rx.admin.modules.content.notice.service.SysNoticeService;
import com.rx.admin.modules.content.message.service.SysMessageService;
import com.rx.admin.modules.system.user.mapper.SysUserMenuMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 权限申请 Service
 *
 * 审批通过后的权限分配策略：
 * 1. 页面菜单（type=2）→ 写入 sys_user_menu 表（个性化权限）
 * 2. 按钮权限（type=3）→ 写入 sys_user_menu 表（个性化权限）
 * 3. 仪表盘、权限申请等公共权限 → 通过 user 角色（role_id=2）统一控制，无需审批
 *
 * 为什么不用独立角色 user_{userId}？
 * - 100个用户=100个角色，角色表膨胀，失去"角色=分类"的意义
 * - sys_user_menu 表语义更清晰：直接表达"用户拥有哪些菜单/按钮"
 * - 查询更简单：用户权限 = 角色权限 ∪ 直接授权权限
 */
@Service
public class SysPermissionRequestService extends ServiceImpl<SysPermissionRequestMapper, SysPermissionRequest> {

    private static final Logger log = LoggerFactory.getLogger(SysPermissionRequestService.class);

    private final SysUserMapper sysUserMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysUserMenuMapper sysUserMenuMapper;
    private final SysNoticeService sysNoticeService;
    private final SysMessageService sysMessageService;
    private final CacheManager cacheManager;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SysPermissionRequestService(SysUserMapper sysUserMapper,
                                       SysMenuMapper sysMenuMapper,
                                       SysUserMenuMapper sysUserMenuMapper,
                                       SysNoticeService sysNoticeService,
                                       SysMessageService sysMessageService,
                                       CacheManager cacheManager) {
        this.sysUserMapper = sysUserMapper;
        this.sysMenuMapper = sysMenuMapper;
        this.sysUserMenuMapper = sysUserMenuMapper;
        this.sysNoticeService = sysNoticeService;
        this.sysMessageService = sysMessageService;
        this.cacheManager = cacheManager;
    }

    /**
     * 提交权限申请（自动生成待办通知）
     */
    public void submitRequest(Long userId, List<Long> menuIds, List<String> menuNames) {
        try {
            SysUser user = sysUserMapper.selectById(userId);
            String username = user != null ? user.getUsername() : ("user_" + userId);

            SysPermissionRequest request = new SysPermissionRequest();
            request.setUserId(userId);
            request.setUsername(username);
            request.setMenuIds(objectMapper.writeValueAsString(menuIds));
            request.setMenuNames(objectMapper.writeValueAsString(menuNames));
            request.setStatus(0);
            save(request);

            // 自动生成待办通知给 admin
            createTodoNotice(request.getId(), username, String.join("、", menuNames));
        } catch (Exception e) {
            throw new RuntimeException("提交权限申请失败", e);
        }
    }

    /**
     * 审批通过
     * 将申请的菜单/按钮权限直接写入 sys_user_menu 表（个性化权限）
     * 不再创建独立角色，避免角色冗余
     */
    @Transactional
    public void approve(Long requestId) {
        SysPermissionRequest request = getById(requestId);
        if (request == null || request.getStatus() != 0) {
            throw new RuntimeException("申请不存在或已处理");
        }

        long auditUserId = StpUtil.getLoginIdAsLong();
        SysUser auditUser = sysUserMapper.selectById(auditUserId);
        String auditUsername = auditUser != null ? auditUser.getUsername() : ("user_" + auditUserId);

        try {
            List<Long> menuIds = objectMapper.readValue(request.getMenuIds(), new TypeReference<List<Long>>() {});

            // 收集所有需要授权的菜单ID
            Set<Long> allMenuIds = new HashSet<>();
            // 加载所有菜单，用于递归查找子孙
            List<SysMenu> allMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                    .eq(SysMenu::getStatus, 1));

            // 判断用户是否已精确选择了按钮权限（type=3）
            boolean hasButtonPerms = false;
            for (Long mid : menuIds) {
                SysMenu menu = allMenus.stream().filter(m -> m.getId().equals(mid)).findFirst().orElse(null);
                if (menu != null && menu.getMenuType() == 3) {
                    hasButtonPerms = true;
                    break;
                }
            }

            for (Long menuId : menuIds) {
                allMenuIds.add(menuId);
                // 如果用户精确选择了按钮权限，只分配选中的菜单；否则自动追加所有子孙按钮权限
                if (!hasButtonPerms) {
                    collectDescendantButtonIds(allMenus, menuId, allMenuIds);
                }
            }

            // 写入 sys_user_menu 表（个性化权限）
            for (Long menuId : allMenuIds) {
                try {
                    sysUserMenuMapper.insert(request.getUserId(), menuId);
                } catch (Exception e) {
                    log.warn("插入用户菜单权限忽略重复: {}", e.getMessage());
                }
            }
            // 清除申请人菜单缓存，使权限立即生效
            evictUserMenuCache(request.getUserId());
        } catch (Exception e) {
            throw new RuntimeException("审批失败: " + e.getMessage(), e);
        }

        request.setStatus(1);
        request.setAuditUserId(auditUserId);
        request.setAuditUsername(auditUsername);
        updateById(request);

        // 发送系统消息通知申请人
        try {
            String menuNameStr = formatMenuNames(request.getMenuNames());
            sysMessageService.sendNotificationMessage(
                    "权限申请已通过",
                    "您申请的权限（" + menuNameStr + "）已被 " + auditUsername + " 审批通过。权限将在您重新登录后生效，请退出后再次登录。",
                    request.getUserId(),
                    "/permission/my");
        } catch (Exception e) { log.warn("审批通过消息发送失败: {}", e.getMessage()); }

        // 清除对应的待办通知
        clearTodoNotice(request.getId());
    }

    /**
     * 审批拒绝
     */
    public void reject(Long requestId, String remark) {
        SysPermissionRequest request = getById(requestId);
        if (request == null || request.getStatus() != 0) {
            throw new RuntimeException("申请不存在或已处理");
        }

        long auditUserId = StpUtil.getLoginIdAsLong();
        SysUser auditUser = sysUserMapper.selectById(auditUserId);
        String auditUsername = auditUser != null ? auditUser.getUsername() : ("user_" + auditUserId);

        request.setStatus(2);
        request.setAuditUserId(auditUserId);
        request.setAuditUsername(auditUsername);
        request.setAuditRemark(remark != null ? remark : "");
        updateById(request);

        // 发送系统消息通知申请人
        try {
            String menuNameStr = formatMenuNames(request.getMenuNames());
            String remarkText = (remark != null && !remark.isEmpty()) ? "原因：" + remark : "";
            sysMessageService.sendNotificationMessage(
                    "权限申请已被拒绝",
                    "您申请的权限（" + menuNameStr + "）已被 " + auditUsername + " 拒绝。" + remarkText,
                    request.getUserId(),
                    "/permission/my");
        } catch (Exception e) { log.warn("审批拒绝消息发送失败: {}", e.getMessage()); }

        // 清除对应的待办通知
        clearTodoNotice(request.getId());
    }

    private void evictUserMenuCache(Long userId) {
        try {
            var cache = cacheManager.getCache("menu");
            if (cache != null) {
                cache.evict("router_" + userId);
                cache.evict("requestable_" + userId);
                log.debug("已清除用户 {} 的菜单缓存", userId);
            }
        } catch (Exception e) {
            log.warn("清除用户菜单缓存失败: {}", e.getMessage());
        }
    }

    /**
     * 递归收集指定菜单ID下的所有子孙按钮权限（type=3）
     */
    private void collectDescendantButtonIds(List<SysMenu> allMenus, Long parentId, Set<Long> result) {
        for (SysMenu m : allMenus) {
            if (m.getParentId().equals(parentId)) {
                if (m.getMenuType() == 3) {
                    result.add(m.getId());
                }
                // 继续递归（中间可能有目录层级）
                collectDescendantButtonIds(allMenus, m.getId(), result);
            }
        }
    }

    /**
     * admin 查待审批列表
     */
    public Page<SysPermissionRequest> getPendingRequests(int page, int size) {
        Page<SysPermissionRequest> pageParam = new Page<>(page, size);
        return page(pageParam, new LambdaQueryWrapper<SysPermissionRequest>()
                .eq(SysPermissionRequest::getStatus, 0)
                .orderByDesc(SysPermissionRequest::getCreateTime));
    }

    /**
     * 当前用户查自己的申请
     */
    public Page<SysPermissionRequest> getMyRequests(int page, int size) {
        long userId = StpUtil.getLoginIdAsLong();
        Page<SysPermissionRequest> pageParam = new Page<>(page, size);
        return page(pageParam, new LambdaQueryWrapper<SysPermissionRequest>()
                .eq(SysPermissionRequest::getUserId, userId)
                .orderByDesc(SysPermissionRequest::getCreateTime));
    }

    /**
     * 创建待办通知（权限审批专用）
     */
    private void createTodoNotice(Long requestId, String username, String menuNames) {
        SysNotice notice = new SysNotice();
        notice.setTitle("权限审批：" + username + " 申请了 " + menuNames);
        notice.setContent("用户 " + username + " 申请了以下菜单权限：" + menuNames + "，请前往用户管理页面进行审批。");
        notice.setNoticeType("1");
        notice.setCategory("todo");
        notice.setLinkPath("/system/user");
        notice.setStatus(1);
        sysNoticeService.save(notice);
    }

    /**
     * 将 JSON 数组字符串（如 ["A","B","C"]）格式化为可读字符串 "A、B、C"
     */
    private String formatMenuNames(String menuNamesJson) {
        try {
            List<String> names = objectMapper.readValue(menuNamesJson, new TypeReference<List<String>>() {});
            return String.join("、", names);
        } catch (Exception e) {
            return menuNamesJson;
        }
    }

    /**
     * 清除指定申请的待办通知（审批通过/拒绝后调用）
     * 通过标题模糊匹配定位对应的待办通知并软删除
     */
    private void clearTodoNotice(Long requestId) {
        SysPermissionRequest request = getById(requestId);
        if (request == null) return;
        String username = request.getUsername();
        List<SysNotice> todoNotices = sysNoticeService.list(new LambdaQueryWrapper<SysNotice>()
                .eq(SysNotice::getCategory, "todo")
                .like(SysNotice::getTitle, username));
        for (SysNotice notice : todoNotices) {
            sysNoticeService.removeById(notice.getId());
        }
    }

    /**
     * 邮件申请权限（申请角色范围外的菜单）
     * 发送消息通知所有admin用户
     */
    public void submitEmailRequest(Long userId, String userName, String menus, String description) {
        String title = "权限邮件申请 - " + userName;
        String content = "用户 " + userName + " 申请以下角色范围外的菜单权限：\n" +
                "申请菜单：" + menus + "\n" +
                "申请理由：" + description + "\n\n" +
                "请管理员在角色管理中为用户分配对应权限。";
        sysMessageService.sendToRoleUsers("admin", title, content);
    }
}
