package com.rx.admin.modules.as400.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.result.Result;
import com.rx.admin.modules.as400.vo.As400ObjectVO;
import com.rx.admin.modules.as400.service.As400Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import com.rx.admin.modules.as400.common.LibraryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * AS400 管理接口
 */
@Tag(name = "AS400对象管理")
@Slf4j
@RestController
@RequestMapping("/api/as400")
@RequiredArgsConstructor
public class As400Controller {
    private final As400Service as400Service;

    @Operation(summary = "查询指定Library下的所有Object")
    @GetMapping("/objects/{library}")
    @SaCheckPermission(PermissionConstants.As400.OBJECTS_QUERY)
    public Result<List<As400ObjectVO>> listObjects(@PathVariable String library) {
        try {
            List<As400ObjectVO> objects = as400Service.listObjects(library);
            return Result.ok(objects);
        } catch (LibraryNotFoundException e) {
            log.warn("库不存在: {}", library);
            return Result.fail(ErrorCode.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "查询所有Library下的Object")
    @GetMapping("/objects")
    @SaCheckPermission(PermissionConstants.As400.OBJECTS_QUERY)
    public Result<List<As400ObjectVO>> listAllObjects(
            @RequestParam(defaultValue = "A7RXUZZ1,A7RXUZZ2,A7RXUZZB") String libraries) {
        List<String> libList = Arrays.asList(libraries.split(","));
        List<As400ObjectVO> objects = as400Service.listAllObjects(libList);
        return Result.ok(objects);
    }
}
