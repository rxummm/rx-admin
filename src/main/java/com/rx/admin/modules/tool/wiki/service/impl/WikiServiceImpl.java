package com.rx.admin.modules.tool.wiki.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rx.admin.modules.tool.wiki.dto.WikiSpaceCreateDTO;
import com.rx.admin.modules.tool.wiki.dto.WikiPageCreateDTO;
import com.rx.admin.modules.tool.wiki.entity.WikiPage;
import com.rx.admin.modules.tool.wiki.entity.WikiSpace;
import com.rx.admin.modules.tool.wiki.mapper.WikiPageMapper;
import com.rx.admin.modules.tool.wiki.mapper.WikiSpaceMapper;
import com.rx.admin.modules.tool.wiki.service.WikiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WikiServiceImpl extends ServiceImpl<WikiSpaceMapper, WikiSpace> implements WikiService {

    private final WikiPageMapper pageMapper;

    @Override
    public List<WikiSpace> listSpaces() {
        return list();
    }

    @Override
    public List<WikiPage> listPages(Long spaceId) {
        LambdaQueryWrapper<WikiPage> w = new LambdaQueryWrapper<>();
        w.eq(WikiPage::getSpaceId, spaceId).orderByAsc(WikiPage::getSortOrder);
        return pageMapper.selectList(w);
    }

    @Override
    public WikiPage getPage(Long pageId) {
        WikiPage page = pageMapper.selectById(pageId);
        if (page != null) {
            page.setViewCount(page.getViewCount() + 1);
            pageMapper.updateById(page);
        }
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSpace(WikiSpaceCreateDTO dto, Long ownerId) {
        WikiSpace space = new WikiSpace();
        space.setName(dto.getName());
        space.setDescription(dto.getDescription());
        space.setIcon(dto.getIcon());
        space.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : "PUBLIC");
        space.setOwnerId(ownerId);
        save(space);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPage(WikiPageCreateDTO dto, Long authorId, String authorName) {
        WikiPage page = new WikiPage();
        page.setSpaceId(dto.getSpaceId());
        page.setParentId(dto.getParentId());
        page.setTitle(dto.getTitle());
        page.setContent(dto.getContent());
        page.setSlug(dto.getSlug());
        page.setIsPublished(dto.getIsPublished() != null ? dto.getIsPublished() : 0);
        page.setAuthorId(authorId);
        page.setAuthorName(authorName);
        page.setViewCount(0);
        page.setSortOrder(0);
        pageMapper.insert(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePage(Long pageId, String title, String content, Integer isPublished) {
        WikiPage page = pageMapper.selectById(pageId);
        if (page != null) {
            page.setTitle(title);
            page.setContent(content);
            page.setIsPublished(isPublished);
            pageMapper.updateById(page);
        }
    }
}
