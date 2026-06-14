package com.rx.admin.common.base;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rx.admin.common.result.Result;

import java.util.List;

/**
 * 通用 CRUD 控制器基类
 * 封装常见的增删改查操作，子控制器继承后自动获得 CRUD 能力
 *
 * @param <S> Service 类型（需实现 IService<T>）
 * @param <T> 实体类型
 */
public abstract class BaseCrudController<S extends IService<T>, T> {

    protected final S baseService;

    public BaseCrudController(S baseService) {
        this.baseService = baseService;
    }

    protected Result<T> getById(Long id) {
        return Result.ok(baseService.getById(id));
    }

    protected Result<List<T>> list() {
        return Result.ok(baseService.list());
    }

    protected Result<Void> add(T entity) {
        baseService.save(entity);
        return Result.ok();
    }

    protected Result<Void> update(T entity) {
        baseService.updateById(entity);
        return Result.ok();
    }

    protected Result<Void> delete(Long id) {
        baseService.removeById(id);
        return Result.ok();
    }
}