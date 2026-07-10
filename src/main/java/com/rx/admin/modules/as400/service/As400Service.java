package com.rx.admin.modules.as400.service;

import com.rx.admin.modules.as400.common.LibraryNotFoundException;
import com.rx.admin.modules.as400.vo.As400ObjectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * AS400 (IBM i) 连接与查询服务
 *
 * 使用 QSYS2.OBJECT_STATISTICS Table Function 获取对象完整属性信息
 */
@Slf4j
@Service
public class As400Service {

    @Value("${as400.host:}")
    private String host;

    @Value("${as400.username:}")
    private String username;

    @Value("${as400.password:}")
    private String password;

    /**
     * 列出指定 Library 下的所有 Object 信息
     *
     * 先用 LIBRARY_INFO(库名, 'NO') 验证库是否存在（库不存在会直接报错），
     * 存在再查 OBJECT_STATISTICS(*ALL) 获取所有 objects。
     *
     * @param library 库名，如 A7RXUZZ1, A7RXUZZ2, A7RXUZZB
     * @return Object 列表
     * @throws LibraryNotFoundException 库不存在
     * @throws RuntimeException 其他查询错误
     */
    public List<As400ObjectVO> listObjects(String library) {
        List<As400ObjectVO> result = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // 1. 连接 AS400
            String url = "jdbc:as400://" + host
                    + ";translate binary=true;naming=sql;errors=full";
            Class.forName("com.ibm.as400.access.AS400JDBCDriver");
            conn = DriverManager.getConnection(url, username, password);

            // 2. 用 LIBRARY_INFO 验证库是否存在
            //    库不存在时：抛 SQLException（SQL0443）或返回0行，两种都要兜底
            String checkSql = "SELECT 1 FROM TABLE(QSYS2.LIBRARY_INFO(?, 'NO'))";
            ps = conn.prepareStatement(checkSql);
            ps.setString(1, library);
            boolean exists;
            try {
                rs = ps.executeQuery();
                exists = rs.next();
            } catch (SQLException e) {
                // SQL0443: LIBRARY not found
                log.warn("Library [{}] 不存在: {}", library, e.getMessage());
                throw new LibraryNotFoundException(library, e);
            }
            if (!exists) {
                log.warn("Library [{}] 不存在（返回0行）", library);
                throw new LibraryNotFoundException(library);
            }
            log.info("Library [{}] 存在", library);
            closeQuietly(rs);
            closeQuietly(ps);
            rs = null;
            ps = null;

            // 3. 库存在，查询全部 objects
            String sql = "SELECT "
                    + "OBJLIB, OBJNAME, OBJTYPE, OBJATTRIBUTE, "
                    + "OBJTEXT, OBJOWNER, OBJCREATED, OBJSIZE, LAST_USED_TIMESTAMP "
                    + "FROM TABLE(QSYS2.OBJECT_STATISTICS(?, '*ALL', '*ALL')) "
                    + "ORDER BY OBJNAME";
            ps = conn.prepareStatement(sql);
            ps.setString(1, library);
            rs = ps.executeQuery();

            while (rs.next()) {
                As400ObjectVO vo = As400ObjectVO.builder()
                        .library(rs.getString("OBJLIB"))
                        .objectName(rs.getString("OBJNAME"))
                        .objectType(rs.getString("OBJTYPE"))
                        .attribute(rs.getString("OBJATTRIBUTE"))
                        .text(rs.getString("OBJTEXT"))
                        .owner(rs.getString("OBJOWNER"))
                        .createDate(formatTimestamp(rs.getTimestamp("OBJCREATED")))
                        .build();

                try {
                    long size = rs.getLong("OBJSIZE");
                    if (!rs.wasNull() && size > 0) {
                        vo.setSize(size);
                    }
                } catch (Exception e) {
                    log.warn("获取 OBJSIZE 失败: {}", e.getMessage());
                }

                result.add(vo);
            }

            log.info("从 {} 通过 QSYS2.OBJECT_STATISTICS 获取 {} 个对象", library, result.size());
        } catch (LibraryNotFoundException e) {
            throw e; // 直接透传
        } catch (SQLException e) {
            log.error("连接 AS400 或查询 Library [{}] 失败: {}", library, e.getMessage(), e);
            throw new RuntimeException("AS400 查询失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("查询 Library [{}] 失败: {}", library, e.getMessage(), e);
            throw new RuntimeException("AS400 查询失败: " + e.getMessage(), e);
        } finally {
            closeQuietly(rs);
            closeQuietly(ps);
            closeQuietly(conn);
        }

        return result;
    }

    /**
     * 批量查询多个 Library 下的所有 Object
     */
    public List<As400ObjectVO> listAllObjects(List<String> libraries) {
        List<As400ObjectVO> allObjects = new ArrayList<>();
        for (String lib : libraries) {
            try {
                List<As400ObjectVO> objects = listObjects(lib);
                allObjects.addAll(objects);
            } catch (Exception e) {
                log.error("查询 Library [{}] 失败，跳过: {}", lib, e.getMessage());
            }
        }
        return allObjects;
    }

    private static String formatTimestamp(Timestamp ts) {
        return ts == null ? "" : ts.toString();
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try { closeable.close(); } catch (Exception e) { log.debug("资源关闭失败", e); }
        }
    }
}
