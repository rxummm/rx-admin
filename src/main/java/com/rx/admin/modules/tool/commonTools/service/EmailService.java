package com.rx.admin.modules.tool.commonTools.service;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 通用邮件发送服务
 * <p>
 * 可用于：
 * 1. 前端邮件发送页面（办公工具 > 邮件发送）
 * 2. 定时任务自动发送（操作日志、异常报告等）
 * 3. 其他业务模块的程序化邮件通知
 * </p>
 */
public interface EmailService {

    /**
     * 发送邮件（同步）
     * <p>
     * 注意：由于免费 SMTP 不允许伪造发件人，实际 From 始终为 MAIL_FROM 配置值。
     * replyTo 参数仅设置 Reply-To 头，收件人回复时会自动回给对应地址。
     * </p>
     *
     * @param replyTo 回复地址（为 null 则使用 MAIL_FROM），收件人点击"回复"时的目标地址
     * @param to      收件人列表
     * @param cc      抄送列表（可为 null）
     * @param bcc     密送列表（可为 null）
     * @param subject 邮件主题
     * @param content 邮件正文
     * @param isHtml  是否为 HTML 格式
     * @param attachments 附件文件路径列表（可为 null）
     */
    void send(String replyTo, List<String> to, List<String> cc, List<String> bcc,
              String subject, String content, boolean isHtml,
              List<File> attachments);

    /**
     * 发送邮件（异步），返回 CompletableFuture 便于链式处理
     */
    CompletableFuture<Void> sendAsync(String replyTo, List<String> to, List<String> cc, List<String> bcc,
                                       String subject, String content, boolean isHtml,
                                       List<File> attachments);

    /**
     * 发送简单文本邮件（无附件、无抄送）—— 为自动通知场景提供便捷方法
     * from 为 null 时使用配置的 MAIL_FROM
     */
    default void sendSimple(String to, String subject, String content) {
        send(null, List.of(to), null, null, subject, content, false, null);
    }

    /**
     * 发送简单文本邮件（异步）
     */
    default CompletableFuture<Void> sendSimpleAsync(String to, String subject, String content) {
        return sendAsync(null, List.of(to), null, null, subject, content, false, null);
    }

    /**
     * 发送HTML邮件（无附件、无抄送）
     */
    default void sendHtml(String to, String subject, String htmlContent) {
        send(null, List.of(to), null, null, subject, htmlContent, true, null);
    }

    /**
     * 发送HTML邮件（异步）
     */
    default CompletableFuture<Void> sendHtmlAsync(String to, String subject, String htmlContent) {
        return sendAsync(null, List.of(to), null, null, subject, htmlContent, true, null);
    }

    /**
     * 获取邮件配置信息（用于前端展示）
     * <p>
     * 返回 Map 包含：
     * - from: 统一发件人地址（MAIL_FROM 配置值，所有用户相同）
     * - replyTo: 回复地址（当前用户的邮箱，收件人点击回复时自动使用）
     * </p>
     * @param userEmail 当前登录用户的邮箱
     */
    Map<String, String> getMailConfig(String userEmail);
}
