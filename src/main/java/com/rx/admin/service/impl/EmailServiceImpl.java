package com.rx.admin.service.impl;

import com.rx.admin.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String defaultFrom;

    public EmailServiceImpl(
            @Value("${app.mail.host:}") String host,
            @Value("${app.mail.port:25}") int port,
            @Value("${app.mail.username:}") String username,
            @Value("${app.mail.password:}") String password,
            @Value("${app.mail.properties.mail.smtp.auth:false}") boolean auth,
            @Value("${app.mail.properties.mail.smtp.starttls.enable:false}") boolean starttls,
            @Value("${app.mail.properties.mail.smtp.ssl.enable:false}") boolean ssl) {

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);
        sender.setDefaultEncoding("UTF-8");

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", String.valueOf(auth));
        props.put("mail.smtp.starttls.enable", String.valueOf(starttls));
        props.put("mail.smtp.ssl.enable", String.valueOf(ssl));
        props.put("mail.smtp.connectiontimeout", String.valueOf(10000));
        props.put("mail.smtp.timeout", String.valueOf(30000));
        props.put("mail.smtp.writetimeout", String.valueOf(30000));

        this.mailSender = sender;
    }

    @Override
    public void send(String from, List<String> to, List<String> cc, List<String> bcc,
                     String subject, String content, boolean isHtml,
                     List<File> attachments) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // 发件人统一使用 MAIL_FROM（SMTP 服务器限制：免费邮箱不允许伪造 from）
            // 用户的邮箱通过 Reply-To 体现，收件人回复时自动回复给对应用户
            String replyTo = (from != null && !from.isBlank()) ? from.trim() : defaultFrom;
            if (defaultFrom.isBlank()) {
                throw new RuntimeException("发件人地址未配置，请在 application-local.yml 中设置 MAIL_FROM");
            }
            helper.setFrom(defaultFrom);
            helper.setReplyTo(replyTo);
            helper.setTo(to.toArray(new String[0]));
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0]));
            }
            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(new String[0]));
            }
            helper.setSubject(subject);
            helper.setText(content, isHtml);

            // 添加附件
            if (attachments != null) {
                for (File file : attachments) {
                    if (file.exists() && file.isFile()) {
                        FileSystemResource fsr = new FileSystemResource(file);
                        helper.addAttachment(file.getName(), fsr);
                    }
                }
            }

            mailSender.send(message);
            log.info("邮件发送成功 -> to: {}, subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("邮件发送失败 -> to: {}, subject: {}, error: {}", to, subject, e.getMessage(), e);
            throw new RuntimeException("邮件发送失败: " + e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<Void> sendAsync(String from, List<String> to, List<String> cc, List<String> bcc,
                                              String subject, String content, boolean isHtml,
                                              List<File> attachments) {
        return CompletableFuture.runAsync(() ->
                send(from, to, cc, bcc, subject, content, isHtml, attachments));
    }

    @Override
    public Map<String, String> getMailConfig(String userEmail) {
        Map<String, String> config = new LinkedHashMap<>();
        // 统一发件人（SMTP 限制：不能随意伪造 from）
        config.put("from", defaultFrom);
        // 回复地址（收件人点击"回复"时自动使用此地址）
        String replyTo = (userEmail != null && !userEmail.isBlank()) ? userEmail.trim() : defaultFrom;
        config.put("replyTo", replyTo);
        config.put("displayName", "邮件发送工具");
        return config;
    }
}
