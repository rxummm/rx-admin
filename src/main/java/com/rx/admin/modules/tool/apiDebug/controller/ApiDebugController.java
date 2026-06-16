package com.rx.admin.modules.tool.apiDebug.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.constant.PermissionConstants;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

@Tag(name = "API调试面板")
@RestController
@RequestMapping("/api/tool/api-debug")
public class ApiDebugController {

    private final RequestMappingHandlerMapping handlerMapping;

    public ApiDebugController(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Operation(summary = "查询所有API端点")
    @GetMapping("/endpoints")
    @SaCheckPermission(PermissionConstants.Tool.API_DEBUG_LIST)
    public Result<List<Map<String, Object>>> endpoints() {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod method = entry.getValue();
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("methods", info.getMethodsCondition().getMethods().stream().map(Enum::name).toList());
            item.put("paths", new ArrayList<>(info.getPatternValues()));
            item.put("controller", method.getBeanType().getSimpleName());
            item.put("handler", method.getMethod().getName());
            list.add(item);
        }
        return Result.ok(list);
    }
}
