package com.rx.admin.modules.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.modules.system.user.entity.SysUserTag;
import com.rx.admin.modules.system.user.entity.SysUserTagRelation;
import com.rx.admin.modules.system.user.mapper.SysUserTagMapper;
import com.rx.admin.modules.system.user.mapper.SysUserTagRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户标签服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserTagService {

    private final SysUserTagMapper tagMapper;
    private final SysUserTagRelationMapper relationMapper;

    /**
     * 获取所有标签
     */
    public List<SysUserTag> listAll() {
        LambdaQueryWrapper<SysUserTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTag::getStatus, 1)
               .orderByAsc(SysUserTag::getCreateTime);
        return tagMapper.selectList(wrapper);
    }

    /**
     * 创建标签
     */
    public SysUserTag createTag(SysUserTag tag) {
        tag.setStatus(1);
        tag.setCreateTime(LocalDateTime.now());
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.insert(tag);
        log.info("创建用户标签: {}", tag.getTagName());
        return tag;
    }

    /**
     * 更新标签
     */
    public void updateTag(SysUserTag tag) {
        tag.setUpdateTime(LocalDateTime.now());
        tagMapper.updateById(tag);
    }

    /**
     * 删除标签
     */
    public void deleteTag(Long tagId) {
        tagMapper.deleteById(tagId);
        // 同时删除关联关系
        LambdaQueryWrapper<SysUserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTagRelation::getTagId, tagId);
        relationMapper.delete(wrapper);
    }

    /**
     * 给用户打标签
     */
    public void addUserTag(Long userId, Long tagId) {
        // 检查是否已存在
        LambdaQueryWrapper<SysUserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTagRelation::getUserId, userId)
               .eq(SysUserTagRelation::getTagId, tagId);
        Long count = relationMapper.selectCount(wrapper);
        
        if (count == 0) {
            SysUserTagRelation relation = new SysUserTagRelation();
            relation.setUserId(userId);
            relation.setTagId(tagId);
            relation.setCreateTime(LocalDateTime.now());
            relationMapper.insert(relation);
        }
    }

    /**
     * 移除用户标签
     */
    public void removeUserTag(Long userId, Long tagId) {
        LambdaQueryWrapper<SysUserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTagRelation::getUserId, userId)
               .eq(SysUserTagRelation::getTagId, tagId);
        relationMapper.delete(wrapper);
    }

    /**
     * 获取用户的标签列表
     */
    public List<SysUserTag> getUserTags(Long userId) {
        LambdaQueryWrapper<SysUserTagRelation> relationWrapper = new LambdaQueryWrapper<>();
        relationWrapper.eq(SysUserTagRelation::getUserId, userId);
        List<SysUserTagRelation> relations = relationMapper.selectList(relationWrapper);
        
        if (relations.isEmpty()) {
            return List.of();
        }
        
        List<Long> tagIds = relations.stream()
                .map(SysUserTagRelation::getTagId)
                .collect(Collectors.toList());
        
        LambdaQueryWrapper<SysUserTag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.in(SysUserTag::getId, tagIds);
        return tagMapper.selectList(tagWrapper);
    }

    /**
     * 按标签查询用户ID列表
     */
    public List<Long> getUserIdsByTag(Long tagId) {
        LambdaQueryWrapper<SysUserTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserTagRelation::getTagId, tagId);
        return relationMapper.selectList(wrapper).stream()
                .map(SysUserTagRelation::getUserId)
                .collect(Collectors.toList());
    }
}
