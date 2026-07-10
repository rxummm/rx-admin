package com.rx.admin.modules.content.notice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rx.admin.modules.content.notice.entity.SysNoticeRead;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysNoticeReadMapper extends BaseMapper<SysNoticeRead> {

    @Insert("<script>INSERT INTO sys_notice_read (notice_id, user_id, read_time) VALUES <foreach item='noticeId' collection='noticeIds' separator=','>(#{noticeId}, #{userId}, NOW())</foreach></script>")
    int insertBatch(@Param("userId") Long userId, @Param("noticeIds") List<Long> noticeIds);
}