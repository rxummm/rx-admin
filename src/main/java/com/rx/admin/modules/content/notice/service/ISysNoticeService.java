package com.rx.admin.modules.content.notice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.PageResult;
import com.rx.admin.modules.content.notice.entity.SysNotice;
import com.rx.admin.modules.content.notice.dto.NoticeCreateDTO;
import com.rx.admin.modules.content.notice.dto.NoticeUpdateDTO;

import java.util.List;

public interface ISysNoticeService extends IService<SysNotice> {

    PageResult<SysNotice> pageQuery(int page, int size, String keyword);

    PageResult<SysNotice> pageQuery(int page, int size, String keyword, String category);

    void addNotice(NoticeCreateDTO dto);

    void updateNotice(NoticeUpdateDTO dto);

    long countByCategory(String category);

    List<Long> getReadIds(Long userId);

    void markRead(Long userId, Long noticeId);

    void markAllRead(Long userId);
}