package com.rx.admin.modules.system.user.convert;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.entity.SysUser;
import com.rx.admin.modules.system.user.dto.UserCreateDTO;
import com.rx.admin.modules.system.user.dto.UserUpdateDTO;
import com.rx.admin.modules.system.user.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 用户对象转换器（MapStruct 编译期生成实现）
 * 统一管理 Entity ↔ DTO ↔ VO 的转换逻辑
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConvert {

    /** DTO → Entity（创建时） */
    SysUser toEntity(UserCreateDTO dto);

    /** DTO → Entity（更新时，忽略 null 值） */
    void updateEntity(@MappingTarget SysUser entity, UserUpdateDTO dto);

    /** Entity → VO（返回给前端，排除 password） */
    @Mapping(target = "deptName", ignore = true)
    @Mapping(target = "roleIds", ignore = true)
    @Mapping(target = "roleNames", ignore = true)
    UserVO toVO(SysUser entity);

    /** MyBatis-Plus Page → PageResult */
    default PageResult<UserVO> toPageResult(Page<SysUser> page) {
        List<UserVO> voList = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }
}