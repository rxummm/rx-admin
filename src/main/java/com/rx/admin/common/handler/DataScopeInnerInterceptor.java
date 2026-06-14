package com.rx.admin.common.handler;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.rx.admin.common.annotation.DataScope;
import com.rx.admin.service.DataScopeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 数据权限拦截器（MyBatis Plus InnerInterceptor）
 * 自动为标注了 @DataScope 注解的 Mapper 方法注入数据权限过滤条件。
 *
 * 使用方式：在 Mapper 方法上添加 @DataScope 注解
 * <pre>
 *   @DataScope(deptColumn = "dept_id", userColumn = "create_by")
 *   List&lt;SysUser&gt; selectUserList(SysUser user);
 * </pre>
 */
@Slf4j
public class DataScopeInnerInterceptor implements InnerInterceptor {

    private final DataScopeService dataScopeService;

    public DataScopeInnerInterceptor(DataScopeService dataScopeService) {
        this.dataScopeService = dataScopeService;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 只拦截 SELECT 查询
        if (ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }

        // 检查 Mapper 方法是否有 @DataScope 注解
        DataScope dataScope = getDataScopeAnnotation(ms);
        if (dataScope == null) {
            return;
        }

        // 获取当前用户可见的部门 ID 列表
        List<Long> deptIds;
        try {
            deptIds = dataScopeService.getVisibleDeptIds();
        } catch (Exception e) {
            // 非登录态（如定时任务）跳过数据权限过滤
            log.debug("跳过数据权限过滤: {}", e.getMessage());
            return;
        }

        if (deptIds == null) {
            // DATA_ALL = 1 全部数据，不做限制
            return;
        }

        // 构建数据权限 SQL 条件
        String condition;
        if (deptIds.isEmpty()) {
            // DATA_SELF = 4 仅本人数据
            Long userId = StpUtil.getLoginIdAsLong();
            condition = dataScope.userColumn() + " = " + userId;
        } else {
            // DATA_DEPT / DATA_DEPT_TREE / DATA_CUSTOM
            String ids = deptIds.stream()
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("-1");
            condition = dataScope.deptColumn() + " IN (" + ids + ")";
        }

        // 注入 SQL 条件
        String originalSql = boundSql.getSql();
        String newSql = injectWhereCondition(originalSql, condition);

        try {
            Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, newSql);
        } catch (Exception e) {
            log.error("数据权限 SQL 注入失败", e);
        }
    }

    /**
     * 从 MappedStatement 中反射获取对应 Mapper 方法上的 @DataScope 注解
     */
    private DataScope getDataScopeAnnotation(MappedStatement ms) {
        try {
            String id = ms.getId(); // 格式: com.rx.admin.mapper.SysUserMapper.selectUserList
            String className = id.substring(0, id.lastIndexOf('.'));
            String methodName = id.substring(id.lastIndexOf('.') + 1);
            Class<?> mapperClass = Class.forName(className);

            for (java.lang.reflect.Method method : mapperClass.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return method.getAnnotation(DataScope.class);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    /**
     * 将数据权限条件注入到原始 SQL 中
     * 条件会插入在 WHERE 子句后（AND）或新增 WHERE 子句
     * 插入位置在 GROUP BY / ORDER BY / LIMIT 之前
     */
    static String injectWhereCondition(String sql, String condition) {
        String upperSql = sql.toUpperCase();
        int groupByIdx = upperSql.indexOf("GROUP BY");
        int orderByIdx = upperSql.indexOf("ORDER BY");
        int limitIdx = upperSql.indexOf("LIMIT");

        int insertIdx = sql.length();
        if (groupByIdx >= 0) insertIdx = groupByIdx;
        if (orderByIdx >= 0 && orderByIdx < insertIdx) insertIdx = orderByIdx;
        if (limitIdx >= 0 && limitIdx < insertIdx) insertIdx = limitIdx;

        if (upperSql.contains("WHERE")) {
            return sql.substring(0, insertIdx) + " AND " + condition + " " + sql.substring(insertIdx);
        } else {
            return sql.substring(0, insertIdx) + " WHERE " + condition + " " + sql.substring(insertIdx);
        }
    }
}