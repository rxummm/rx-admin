package com.rx.admin.common.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

/**
 * MyBatis-Plus TypeHandler：AES 加密/解密字段
 * 用于手机号、邮箱等敏感字段的自动加解密
 * 
 * 使用方式：@TableField(typeHandler = AesTypeHandler.class)
 */
@SuppressWarnings("null")
public class AesTypeHandler extends BaseTypeHandler<String> {

    /** 16 位密钥（生产环境应从配置中心/环境变量读取） */
    private static final byte[] AES_KEY = "RxAdmin!@#2026!!".getBytes(StandardCharsets.UTF_8);
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    private static final SecretKeySpec KEY_SPEC = new SecretKeySpec(AES_KEY, "AES");

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, encrypt(parameter));
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return decrypt(value);
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return decrypt(value);
    }

    private String encrypt(String plainText) {
        if (plainText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, KEY_SPEC);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES encrypt failed", e);
        }
    }

    private String decrypt(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, KEY_SPEC);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // 如果解密失败，返回原文（兼容未加密的旧数据）
            return encryptedText;
        }
    }
}