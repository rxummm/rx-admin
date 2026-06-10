package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.result.Result;
import com.rx.admin.entity.As400ObjectVO;
import com.rx.admin.service.As400Service;
import com.rx.admin.service.LibraryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * AS400 管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/as400")
@RequiredArgsConstructor
public class As400Controller {

    private final As400Service as400Service;

    /**
     * 查询指定 Library 下的所有 Object
     */
    @GetMapping("/objects/{library}")
    @SaCheckPermission("as400:objects:query")
    public Result<List<As400ObjectVO>> listObjects(@PathVariable String library) {
        try {
            List<As400ObjectVO> objects = as400Service.listObjects(library);
            return Result.ok(objects);
        } catch (LibraryNotFoundException e) {
            log.warn("库不存在: {}", library);
            return Result.fail(404, e.getMessage());
        }
    }

    /**
     * 查询所有配置的 Library 下的 Object（默认 A7RXUZZ1, A7RXUZZ2, A7RXUZZB）
     */
    @GetMapping("/objects")
    @SaCheckPermission("as400:objects:query")
    public Result<List<As400ObjectVO>> listAllObjects(
            @RequestParam(defaultValue = "A7RXUZZ1,A7RXUZZ2,A7RXUZZB") String libraries) {
        List<String> libList = Arrays.asList(libraries.split(","));
        List<As400ObjectVO> objects = as400Service.listAllObjects(libList);
        return Result.ok(objects);
    }
}
