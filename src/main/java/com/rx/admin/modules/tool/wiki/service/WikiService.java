package com.rx.admin.modules.tool.wiki.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.modules.tool.wiki.dto.WikiSpaceCreateDTO;
import com.rx.admin.modules.tool.wiki.dto.WikiPageCreateDTO;
import com.rx.admin.modules.tool.wiki.entity.WikiPage;
import com.rx.admin.modules.tool.wiki.entity.WikiSpace;

import java.util.List;

public interface WikiService extends IService<WikiSpace> {
    List<WikiSpace> listSpaces();
    List<WikiPage> listPages(Long spaceId);
    WikiPage getPage(Long pageId);
    void createSpace(WikiSpaceCreateDTO dto, Long ownerId);
    void createPage(WikiPageCreateDTO dto, Long authorId, String authorName);
    void updatePage(Long pageId, String title, String content, Integer isPublished);
}
