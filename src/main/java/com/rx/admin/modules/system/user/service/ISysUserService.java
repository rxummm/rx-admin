package com.rx.admin.modules.system.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.system.user.entity.SysUser;
import java.util.List;
import com.rx.admin.modules.system.user.dto.UserCreateDTO;
import com.rx.admin.modules.system.user.dto.UserUpdateDTO;

public interface ISysUserService extends IService<SysUser> {

    PageResult<SysUser> pageQuery(int page, int size, String keyword);

    void addUser(UserCreateDTO dto);

    void updateUser(UserUpdateDTO dto);

    void deleteUser(Long id);

    void deleteUserBatch(List<Long> ids);

    SysUser getByUsername(String username);

    boolean isEmailDuplicate(String email, Long excludeUserId);

    void assignRole(Long userId, Long roleId);

    void validatePassword(String password, String username, String nickname);

    void validateEmail(String email);

    void validatePhone(String phone);
}