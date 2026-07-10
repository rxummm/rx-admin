package com.rx.admin.modules.tool.schemaDiff.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Tag(name = "Schema对比")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/schema-diff")
@RequiredArgsConstructor
public class SchemaDiffController {

    private final JdbcTemplate jdbcTemplate;

    @SaCheckPermission("tool:schema-diff:query")
    @GetMapping("/tables")
    @Operation(summary = "获取表列表")
    public Result<List<String>> listTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() ORDER BY TABLE_NAME",
                String.class);
        return Result.ok(tables);
    }

    @SaCheckPermission("tool:schema-diff:query")
    @GetMapping("/schema/{tableName}")
    @Operation(summary = "获取表结构")
    public Result<Map<String, Object>> getSchema(@PathVariable String tableName) {
        Map<String, Object> result = new HashMap<>();
        result.put("columns", jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, COLUMN_COMMENT FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY ORDINAL_POSITION",
                tableName));
        result.put("indexes", jdbcTemplate.queryForList(
                "SELECT INDEX_NAME, COLUMN_NAME, NON_UNIQUE, INDEX_TYPE FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? ORDER BY SEQ_IN_INDEX",
                tableName));
        return Result.ok(result);
    }

    @SaCheckPermission("tool:schema-diff:query")
    @GetMapping("/compare")
    @Operation(summary = "对比两个表结构")
    public Result<Map<String, Object>> compare(@RequestParam String table1, @RequestParam String table2) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> cols1 = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?", table1);
        List<Map<String, Object>> cols2 = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?", table2);

        Set<String> names1 = new HashSet<>();
        Set<String> names2 = new HashSet<>();
        cols1.forEach(c -> names1.add((String) c.get("COLUMN_NAME")));
        cols2.forEach(c -> names2.add((String) c.get("COLUMN_NAME")));

        Set<String> onlyIn1 = new TreeSet<>(names1);
        onlyIn1.removeAll(names2);
        Set<String> onlyIn2 = new TreeSet<>(names2);
        onlyIn2.removeAll(names1);

        result.put("table1", table1);
        result.put("table2", table2);
        result.put("onlyInTable1", onlyIn1);
        result.put("onlyInTable2", onlyIn2);
        result.put("commonColumns", new TreeSet<>(names1));
        return Result.ok(result);
    }
}
