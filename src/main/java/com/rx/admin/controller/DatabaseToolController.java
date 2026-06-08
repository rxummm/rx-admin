package com.rx.admin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.rx.admin.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Tag(name = "数据库工具")
@RestController
@RequestMapping("/api/tool/database")
@SaCheckRole("admin")
public class DatabaseToolController {

    private final DataSource primaryDataSource;

    public DatabaseToolController(DataSource primaryDataSource) {
        this.primaryDataSource = primaryDataSource;
    }

    @Operation(summary = "执行SQL查询(只读)")
    @PostMapping("/execute")
    public Result<?> executeSql(@RequestBody Map<String, Object> body) {
        String sql = (String) body.get("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return Result.fail(400, "SQL不能为空");
        }

        String upperSql = sql.trim().toUpperCase();
        // 安全检查：只允许读取操作
        String[] forbiddenKeywords = {"DROP", "DELETE", "INSERT", "UPDATE", "ALTER", "TRUNCATE",
                "CREATE", "GRANT", "REVOKE", "EXEC", "EXECUTE", "CALL", "LOAD", "IMPORT", "RENAME", "REPLACE"};
        for (String keyword : forbiddenKeywords) {
            if (upperSql.contains(keyword)) {
                return Result.fail(403, "不允许执行 " + keyword + " 操作，仅支持 SELECT/SHOW/DESCRIBE/EXPLAIN 语句");
            }
        }

        try (Connection conn = DataSourceUtils.getConnection(primaryDataSource);
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(10); // 10秒超时
            long startTime = System.currentTimeMillis();
            boolean isResultSet = stmt.execute(sql);
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
            return Result.fail(500, "SQL执行错误: " + e.getMessage());
        }
    }

    @Operation(summary = "获取数据库表列表")
    @GetMapping("/tables")
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
            return Result.fail(500, "获取表列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取表结构详情")
    @GetMapping("/tables/{tableName}/columns")
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
            return Result.fail(500, "获取表结构失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取连接池状态")
    @GetMapping("/pool-status")
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
            return Result.fail(500, "获取连接池状态失败: " + e.getMessage());
        }
    }
}
