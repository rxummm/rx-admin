package com.rx.admin.modules.tool.dbConsole.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.annotation.ApiVersion;
import com.rx.admin.common.exception.ErrorCode;
import com.rx.admin.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.datasource.DataSourceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Tag(name = "数据库工具")
@RestController
@ApiVersion(1)
@RequestMapping("/tool/database")
@SaCheckRole("admin")
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DatabaseToolController {

    private final DataSource primaryDataSource;

    @Operation(summary = "执行SQL查询(只读)")
    @PostMapping("/execute")
    @SaCheckPermission("tool:db-console:execute")
    public Result<?> executeSql(@RequestBody Map<String, Object> body) {
        String sql = (String) body.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return Result.fail(ErrorCode.BAD_REQUEST, "SQL不能为空");
        }

        // ⚠️ 安全要求：白名单验证（仅允许只读语句），不再使用黑名单 contains 检查。
        // 黑名单可被分号、注释（SELECT/*x*/DROP）、大小写等方式绕开，攻击者可通过
        // `SELECT 1; DROP TABLE users` 等方式执行破坏性操作。
        // 白名单方案：从 SQL 开头（去前导空白与注释）匹配只读关键字。
        String sanitized = stripLeadingCommentsAndWhitespace(sql);
        String upperSql = sanitized.toUpperCase(Locale.ROOT);
        if (!upperSql.startsWith("SELECT") && !upperSql.startsWith("SHOW")
                && !upperSql.startsWith("DESCRIBE") && !upperSql.startsWith("DESC")
                && !upperSql.startsWith("EXPLAIN") && !upperSql.startsWith("WITH")) {
            return Result.fail(ErrorCode.FORBIDDEN, "仅支持 SELECT/SHOW/DESCRIBE/EXPLAIN/WITH 等只读语句");
        }

        try (Connection conn = DataSourceUtils.getConnection(primaryDataSource);
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(10); // 10秒超时
            // ⚠️ 强制 setMaxRows，避免查询返回过多数据导致 OOM
            stmt.setMaxRows(1000);
            long startTime = System.currentTimeMillis();
            boolean isResultSet = stmt.execute(sanitized);
            long elapsed = System.currentTimeMillis() - startTime;

            if (isResultSet) {
                try (ResultSet rs = stmt.getResultSet()) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int colCount = meta.getColumnCount();

                    List<String> columns = new ArrayList<>();
                    for (int i = 1; i <= colCount; i++) {
                        columns.add(meta.getColumnName(i));
                    }

                    List<Map<String, Object>> rows = new ArrayList<>();
                    int rowCount = 0;
                    while (rs.next() && rowCount < 1000) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        for (int i = 0; i < colCount; i++) {
                            row.put(columns.get(i), rs.getObject(i + 1));
                        }
                        rows.add(row);
                        rowCount++;
                    }

                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("type", "query");
                    result.put("columns", columns);
                    result.put("rows", rows);
                    result.put("rowCount", rowCount);
                    result.put("elapsed", elapsed);
                    result.put("truncated", rowCount >= 1000);
                    return Result.ok(result);
                }
            } else {
                int affected = stmt.getUpdateCount();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("type", "update");
                result.put("affected", affected);
                result.put("elapsed", elapsed);
                return Result.ok(result);
            }
        } catch (SQLException e) {
            return Result.fail(ErrorCode.INTERNAL_ERROR, "SQL执行错误: " + e.getMessage());
        }
    }

    /**
     * 去掉 SQL 开头的空白和注释（行注释 -- 与块注释 /* * /），得到第一个有效 token。
     * 用于白名单匹配时排除 `   \n  -- xxx \n SELECT ...` 这类干扰。
     * 注意：块注释内的 `/*` 终止符用占位符临时替换，避免被外层 replaceAll 误删。
     */
    private String stripLeadingCommentsAndWhitespace(String sql) {
        String s = sql;
        while (true) {
            // 去掉前导空白
            s = s.replaceAll("^\\s+", "");
            if (s.isEmpty()) return s;
            // 去掉行注释
            if (s.startsWith("--")) {
                int nl = s.indexOf('\n');
                s = (nl >= 0) ? s.substring(nl + 1) : "";
                continue;
            }
            // 去掉块注释（仅去开头的连续块注释，不处理 SQL 中部的注释以免破坏语义）
            if (s.startsWith("/*")) {
                int end = s.indexOf("*/");
                if (end < 0) return s; // 未闭合，按原样返回让后续 startsWith 失败
                s = s.substring(end + 2);
                continue;
            }
            // 去掉 # 注释（MySQL 风格）
            if (s.startsWith("#")) {
                int nl = s.indexOf('\n');
                s = (nl >= 0) ? s.substring(nl + 1) : "";
                continue;
            }
            break;
        }
        return s;
    }

    @Operation(summary = "获取数据库表列表")
    @GetMapping("/tables")
    @SaCheckPermission("tool:db-console:query")
    public Result<?> getTables() {
        try (Connection conn = DataSourceUtils.getConnection(primaryDataSource)) {
            DatabaseMetaData meta = conn.getMetaData();
            List<Map<String, Object>> tables = new ArrayList<>();
            try (ResultSet rs = meta.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    Map<String, Object> table = new LinkedHashMap<>();
                    table.put("name", rs.getString("TABLE_NAME"));
                    table.put("comment", rs.getString("REMARKS"));
                    table.put("type", rs.getString("TABLE_TYPE"));
                    tables.add(table);
                }
            }
            return Result.ok(tables);
        } catch (SQLException e) {
            return Result.fail(ErrorCode.INTERNAL_ERROR, "获取表列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取表结构详情")
    @GetMapping("/tables/{tableName}/columns")
    @SaCheckPermission("tool:db-console:query")
    public Result<?> getTableColumns(@PathVariable String tableName) {
        try (Connection conn = DataSourceUtils.getConnection(primaryDataSource)) {
            DatabaseMetaData meta = conn.getMetaData();
            List<Map<String, Object>> columns = new ArrayList<>();

            // 获取列信息
            try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, null)) {
                while (rs.next()) {
                    Map<String, Object> col = new LinkedHashMap<>();
                    col.put("name", rs.getString("COLUMN_NAME"));
                    col.put("type", rs.getString("TYPE_NAME"));
                    col.put("size", rs.getInt("COLUMN_SIZE"));
                    col.put("nullable", rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                    col.put("comment", rs.getString("REMARKS"));
                    col.put("defaultValue", rs.getString("COLUMN_DEF"));
                    columns.add(col);
                }
            }

            // 获取主键
            List<String> primaryKeys = new ArrayList<>();
            try (ResultSet rs = meta.getPrimaryKeys(conn.getCatalog(), null, tableName)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("tableName", tableName);
            result.put("columns", columns);
            result.put("primaryKeys", primaryKeys);
            return Result.ok(result);
        } catch (SQLException e) {
            return Result.fail(ErrorCode.INTERNAL_ERROR, "获取表结构失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取连接池状态")
    @GetMapping("/pool-status")
    @SaCheckPermission("tool:db-console:query")
    public Result<?> getPoolStatus() {
        try {
            if (primaryDataSource instanceof com.zaxxer.hikari.HikariDataSource hikari) {
                Map<String, Object> status = new LinkedHashMap<>();
                status.put("activeConnections", hikari.getHikariPoolMXBean().getActiveConnections());
                status.put("idleConnections", hikari.getHikariPoolMXBean().getIdleConnections());
                status.put("totalConnections", hikari.getHikariPoolMXBean().getTotalConnections());
                status.put("threadsAwaitingConnection", hikari.getHikariPoolMXBean().getThreadsAwaitingConnection());
                status.put("maximumPoolSize", hikari.getMaximumPoolSize());
                return Result.ok(status);
            }
            return Result.ok(Map.of("message", "非HikariCP数据源，无法获取详细状态"));
        } catch (Exception e) {
            return Result.fail(ErrorCode.INTERNAL_ERROR, "获取连接池状态失败: " + e.getMessage());
        }
    }
}
