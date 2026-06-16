package com.rx.admin.modules.as400.techblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.as400.techblog.entity.TechBlogArticle;

import java.util.List;
import java.util.Map;

public interface ITechBlogArticleService extends IService<TechBlogArticle> {

    PageResult<TechBlogArticle> pageQuery(int pageNum, int pageSize, String keyword, String category, String source);

    TechBlogArticle getDetail(Long id);

    List<String> getAllCategories(String source);

    List<TechBlogArticle> getRecent(int limit, String source);

    int getFetchProgress(String source);

    List<String> getFetchLogs(String source);

    Map<String, Integer> getAllProgress();

    void startFetch(String source);
}