package com.rx.admin.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private long page;
    private long size;
    private List<T> records;

    /**
     * 创建分页结果（补全 page 和 size 信息）
     */
    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.total = total;
        result.page = page;
        result.size = size;
        result.records = records;
        return result;
    }

    /**
     * @deprecated 请使用 {@link #of(long, long, long, List)} 补全分页信息
     */
    @Deprecated
    public static <T> PageResult<T> of(long total, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.total = total;
        result.records = records;
        return result;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.total = page.getTotal();
        result.page = page.getCurrent();
        result.size = page.getSize();
        result.records = page.getRecords();
        return result;
    }
}