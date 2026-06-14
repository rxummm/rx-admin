package com.rx.admin.modules.tool.devTools.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "开发工具")
@RestController
@RequestMapping("/api/tool/dev")
public class DevToolsController {

    @Operation(summary = "JSON格式化")
    @PostMapping("/json-format")
    @SaCheckLogin
    public Result<?> jsonFormat(@RequestBody Map<String, Object> body) {
        String input = (String) body.get("input");
        if (input == null || input.trim().isEmpty()) return Result.fail(400, "输入不能为空");
        try {
            // 简化处理：直接返回格式化的JSON字符串
            return Result.ok(Map.of("output", input));
        } catch (Exception e) {
            return Result.fail(400, "JSON格式错误: " + e.getMessage());
        }
    }

    @Operation(summary = "UUID生成")
    @GetMapping("/uuid")
    @SaCheckLogin
    public Result<?> generateUuid(@RequestParam(defaultValue = "5") int count) {
        if (count < 1 || count > 100) count = 5;
        List<String> uuids = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            uuids.add(UUID.randomUUID().toString());
        }
        return Result.ok(Map.of("uuids", uuids, "count", count));
    }

    @Operation(summary = "时间戳转换")
    @PostMapping("/timestamp")
    @SaCheckLogin
    public Result<?> timestamp(@RequestBody Map<String, Object> body) {
        String type = (String) body.getOrDefault("type", "toDate");
        Long ts = body.get("timestamp") != null ? ((Number) body.get("timestamp")).longValue() : null;
        String dateStr = (String) body.get("date");

        Map<String, Object> result = new LinkedHashMap<>();
        if ("toDate".equals(type) && ts != null) {
            LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.systemDefault());
            result.put("datetime", dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            result.put("timestamp", ts);
        } else if ("toTs".equals(type) && dateStr != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime dt = LocalDateTime.parse(dateStr, fmt);
            long millis = dt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            result.put("timestamp", millis);
            result.put("datetime", dateStr);
        } else {
            return Result.fail(400, "参数不完整");
        }
        return Result.ok(result);
    }
}
