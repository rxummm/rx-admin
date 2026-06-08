package com.rx.admin.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;

/**
 * XSS 防护 Jackson 配置
 * 全局反序列化时自动转义所有 String 字段中的 HTML 特殊字符
 */
@Configuration
public class XssJacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer xssCustomizer() {
        return builder -> builder.deserializerByType(String.class, new XssStringDeserializer());
    }

    public static class XssStringDeserializer extends JsonDeserializer<String> {
        @Override
        public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null) return null;
            // Spring HtmlUtils.htmlEscape 转义 < > " ' & 等字符
            return HtmlUtils.htmlEscape(value);
        }
    }
}