package com.rx.admin.modules.tool.wiki.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rx.admin.common.result.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rx.admin.modules.tool.wiki.entity.SysCollaborativeDoc;
import com.rx.admin.modules.tool.wiki.entity.SysCollaborativeDocVersion;
import com.rx.admin.modules.tool.wiki.mapper.SysCollaborativeDocMapper;
import com.rx.admin.modules.tool.wiki.mapper.SysCollaborativeDocVersionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 协作文档服务
 * 支持多人实时编辑、版本历史、编辑锁
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborativeDocService {

    private final SysCollaborativeDocMapper docMapper;
    private final SysCollaborativeDocVersionMapper versionMapper;

    /**
     * 分页查询文档
     */
    public PageResult<SysCollaborativeDoc> pageQuery(Long spaceId, int page, int size) {
        LambdaQueryWrapper<SysCollaborativeDoc> wrapper = new LambdaQueryWrapper<>();
        if (spaceId != null) {
            wrapper.eq(SysCollaborativeDoc::getSpaceId, spaceId);
        }
        wrapper.eq(SysCollaborativeDoc::getStatus, "published")
               .orderByDesc(SysCollaborativeDoc::getLastEditTime);
        
        Page<SysCollaborativeDoc> pageResult = docMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult);
    }

    /**
     * 获取文档详情
     */
    public SysCollaborativeDoc getDoc(Long docId) {
        SysCollaborativeDoc doc = docMapper.selectById(docId);
        if (doc != null) {
            // 增加浏览次数
            doc.setViewCount(doc.getViewCount() + 1);
            docMapper.updateById(doc);
        }
        return doc;
    }

    /**
     * 创建文档
     */
    @Transactional(rollbackFor = Exception.class)
    public SysCollaborativeDoc createDoc(SysCollaborativeDoc doc) {
        doc.setStatus("draft");
        doc.setViewCount(0);
        doc.setCreateTime(LocalDateTime.now());
        doc.setLastEditTime(LocalDateTime.now());
        docMapper.insert(doc);
        
        // 创建初始版本
        saveVersion(doc.getId(), 1, doc.getTitle(), doc.getContent(), 
                   doc.getCreatorId(), doc.getCreatorName(), "初始版本");
        
        log.info("创建协作文档: docId={}, title={}", doc.getId(), doc.getTitle());
        return doc;
    }

    /**
     * 更新文档
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDoc(Long docId, String title, String content, Long editorId, String editorName) {
        SysCollaborativeDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            throw new IllegalArgumentException("文档不存在");
        }
        
        // 检查编辑锁
        if (doc.getEditLock() != null && !editorId.toString().equals(doc.getEditLock())) {
            if (doc.getLockTime() != null && doc.getLockTime().plusMinutes(30).isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("文档正在被 " + doc.getLastEditorName() + " 编辑");
            }
        }
        
        // 获取最新版本号
        LambdaQueryWrapper<SysCollaborativeDocVersion> versionWrapper = new LambdaQueryWrapper<>();
        versionWrapper.eq(SysCollaborativeDocVersion::getDocId, docId)
                     .orderByDesc(SysCollaborativeDocVersion::getVersionNumber)
                     .last("LIMIT 1");
        SysCollaborativeDocVersion latestVersion = versionMapper.selectOne(versionWrapper);
        int newVersionNumber = latestVersion != null ? latestVersion.getVersionNumber() + 1 : 1;
        
        // 更新文档
        doc.setTitle(title);
        doc.setContent(content);
        doc.setLastEditorId(editorId);
        doc.setLastEditorName(editorName);
        doc.setLastEditTime(LocalDateTime.now());
        docMapper.updateById(doc);
        
        // 保存新版本
        saveVersion(docId, newVersionNumber, title, content, editorId, editorName, "自动保存");
        
        log.info("更新协作文档: docId={}, version={}", docId, newVersionNumber);
    }

    /**
     * 获取文档版本历史
     */
    public List<SysCollaborativeDocVersion> getVersionHistory(Long docId) {
        LambdaQueryWrapper<SysCollaborativeDocVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysCollaborativeDocVersion::getDocId, docId)
               .orderByDesc(SysCollaborativeDocVersion::getVersionNumber);
        return versionMapper.selectList(wrapper);
    }

    /**
     * 获取指定版本
     */
    public SysCollaborativeDocVersion getVersion(Long docId, Integer versionNumber) {
        LambdaQueryWrapper<SysCollaborativeDocVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysCollaborativeDocVersion::getDocId, docId)
               .eq(SysCollaborativeDocVersion::getVersionNumber, versionNumber);
        return versionMapper.selectOne(wrapper);
    }

    /**
     * 锁定文档（防止并发编辑）
     */
    public boolean lockDoc(Long docId, Long userId) {
        SysCollaborativeDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            return false;
        }
        
        // 如果锁已过期或没有锁，可以获取锁
        if (doc.getEditLock() == null || 
            doc.getLockTime() == null || 
            doc.getLockTime().plusMinutes(30).isBefore(LocalDateTime.now())) {
            doc.setEditLock(userId.toString());
            doc.setLockTime(LocalDateTime.now());
            docMapper.updateById(doc);
            return true;
        }
        
        // 如果是同一个人，续锁
        if (userId.toString().equals(doc.getEditLock())) {
            doc.setLockTime(LocalDateTime.now());
            docMapper.updateById(doc);
            return true;
        }
        
        return false;
    }

    /**
     * 解锁文档
     */
    public void unlockDoc(Long docId, Long userId) {
        SysCollaborativeDoc doc = docMapper.selectById(docId);
        if (doc != null && userId.toString().equals(doc.getEditLock())) {
            doc.setEditLock(null);
            doc.setLockTime(null);
            docMapper.updateById(doc);
        }
    }

    /**
     * 保存版本
     */
    private void saveVersion(Long docId, int versionNumber, String title, String content, 
                            Long editorId, String editorName, String changeNote) {
        SysCollaborativeDocVersion version = new SysCollaborativeDocVersion();
        version.setDocId(docId);
        version.setVersionNumber(versionNumber);
        version.setTitle(title);
        version.setContent(content);
        version.setEditorId(editorId);
        version.setEditorName(editorName);
        version.setChangeNote(changeNote);
        version.setCreateTime(LocalDateTime.now());
        versionMapper.insert(version);
    }
}
