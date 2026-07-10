package com.rx.admin.common.result;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public long getCurrent() {
        return this.page;
    }

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.total = page.getTotal();
        result.page = page.getCurrent();
        result.size = page.getSize();
        result.records = page.getRecords();
        return result;
    }

    /**
     * 转换分页结果中的记录类型，保留分页元数据
     */
    public <R> PageResult<R> map(Function<T, R> mapper) {
        PageResult<R> result = new PageResult<>();
        result.total = this.total;
        result.page = this.page;
        result.size = this.size;
        result.records = this.records.stream().map(mapper).collect(Collectors.toList());
        return result;
    }
}