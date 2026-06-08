package com.rx.admin.common;

import com.rx.admin.service.SysSlowQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.springframework.stereotype.Component;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@Intercepts({
    @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
    @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, java.sql.ResultSet.class})
})
@RequiredArgsConstructor
public class SlowQueryInterceptor implements Interceptor {

    private final SysSlowQueryService slowQueryService;
    private static final Pattern SQL_TYPE_PATTERN = Pattern.compile("^\\s*(SELECT|INSERT|UPDATE|DELETE)", Pattern.CASE_INSENSITIVE);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= slowQueryService.SLOW_THRESHOLD_MS) {
                try {
                    StatementHandler handler = (StatementHandler) invocation.getTarget();
                    BoundSql boundSql = handler.getBoundSql();
                    String sql = boundSql.getSql();
                    String params = boundSql.getParameterObject() != null
                            ? boundSql.getParameterObject().toString() : null;
                    String queryType = "OTHER";
                    Matcher m = SQL_TYPE_PATTERN.matcher(sql);
                    if (m.find()) queryType = m.group(1).toUpperCase();
                    String mapperMethod = extractMapperMethod(handler);
                    slowQueryService.addSlowQuery(sql, params, cost, queryType, mapperMethod);
                    if (log.isWarnEnabled()) {
                        log.warn("Slow query: {}ms | {} | {}", cost, queryType, mapperMethod);
                    }
                } catch (Exception e) {
                    log.error("Failed to record slow query", e);
                }
            }
        }
    }

    private String extractMapperMethod(StatementHandler handler) {
        try {
            MetaObject meta = SystemMetaObject.forObject(handler);
            Object ms = meta.getValue("delegate.mappedStatement");
            if (ms instanceof MappedStatement) {
                return ((MappedStatement) ms).getId();
            }
        } catch (Exception ignored) {}
        return "";
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
