package com.rx.admin.common.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * API 版本条件
 * <p>
 * 匹配请求路径中的版本号
 * </p>
 *
 * @author RX
 * @version 1.0
 */
@SuppressWarnings("null")
public class ApiVersionCondition implements RequestCondition<ApiVersionCondition> {

    /**
     * 默认 API 前缀
     */
    private static final String DEFAULT_PREFIX = "/api";

    /**
     * 版本号，如 v1, v2
     */
    private final String version;

    /**
     * 版本号对应的路径模式，如 /api/v1, /api/v2
     */
    private final String versionPath;

    /**
     * 版本号的正则表达式
     */
    private final Pattern versionPattern;

    public ApiVersionCondition(int versionNumber) {
        this(versionNumber, DEFAULT_PREFIX);
    }

    public ApiVersionCondition(int versionNumber, String prefix) {
        this.version = "v" + versionNumber;
        this.versionPath = prefix + "/" + this.version;
        this.versionPattern = Pattern.compile(versionPath.replace("/", "\\/") + ".*");
    }

    /**
     * 匹配请求路径
     */
    @Override
    public ApiVersionCondition combine(ApiVersionCondition other) {
        // 方法级别的版本覆盖类级别的版本
        return other;
    }

    /**
     * 获取当前请求的版本路径
     */
    @Override
    public ApiVersionCondition getMatchingCondition(HttpServletRequest request) {
        Matcher m = getVersionPattern().matcher(request.getRequestURI());
        if (m.matches()) {
            return this;
        }
        return null;
    }

    /**
     * 比较优先级，用于版本冲突时决定使用哪个条件
     * 版本号越大优先级越高
     */
    @Override
    public int compareTo(ApiVersionCondition other, HttpServletRequest request) {
        // 从版本字符串中提取版本号进行比较
        int thisVersion = extractVersionNumber(this.version);
        int otherVersion = extractVersionNumber(other.version);
        return Integer.compare(otherVersion, thisVersion); // 降序，更高版本优先
    }

    private int extractVersionNumber(String version) {
        try {
            return Integer.parseInt(version.replace("v", ""));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public String getVersion() {
        return version;
    }

    public String getVersionPath() {
        return versionPath;
    }

    public Pattern getVersionPattern() {
        return versionPattern;
    }

    @Override
    public String toString() {
        return "ApiVersionCondition{" +
                "version='" + version + '\'' +
                ", versionPath='" + versionPath + '\'' +
                '}';
    }
}
