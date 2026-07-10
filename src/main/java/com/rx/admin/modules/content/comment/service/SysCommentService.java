package com.rx.admin.modules.content.comment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.comment.entity.SysComment;
import com.rx.admin.modules.content.comment.mapper.SysCommentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysCommentService {

    private final SysCommentMapper commentMapper;

    /**
     * 分页查询评论
     */
    public PageResult<SysComment> pageQuery(String targetType, Long targetId, int page, int size) {
        LambdaQueryWrapper<SysComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysComment::getTargetType, targetType)
               .eq(SysComment::getTargetId, targetId)
               .eq(SysComment::getStatus, 1)
               .orderByAsc(SysComment::getCreateTime);
        
        Page<SysComment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult);
    }

    /**
     * 添加评论
     */
    public SysComment addComment(String targetType, Long targetId, String content, Long userId, String username, Long parentId) {
        SysComment comment = new SysComment();
        comment.setTargetType(targetType);
        comment.setTargetId(targetId);
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setUsername(username);
        comment.setParentId(parentId);
        comment.setStatus(1);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        
        commentMapper.insert(comment);
        log.info("添加评论: targetType={}, targetId={}, userId={}", targetType, targetId, userId);
        return comment;
    }

    /**
     * 删除评论（逻辑删除）
     */
    public void deleteComment(Long id, Long userId) {
        SysComment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new IllegalArgumentException("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此评论");
        }
        
        comment.setStatus(2);
        comment.setUpdateTime(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    /**
     * 获取评论数量
     */
    public long countComments(String targetType, Long targetId) {
        LambdaQueryWrapper<SysComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysComment::getTargetType, targetType)
               .eq(SysComment::getTargetId, targetId)
               .eq(SysComment::getStatus, 1);
        return commentMapper.selectCount(wrapper);
    }
}
