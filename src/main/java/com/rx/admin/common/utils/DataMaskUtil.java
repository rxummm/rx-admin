package com.rx.admin.common.utils;

/**
 * 数据脱敏工具类
 * 用于返回前端时对敏感字段进行脱敏处理
 */
public class DataMaskUtil {

    private DataMaskUtil() {
    }

    /** 手机号：138****1234 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 邮箱：tes***@example.com */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int atIndex = email.indexOf("@");
        if (atIndex <= 1) return email;
        return email.substring(0, 3) + "***" + email.substring(atIndex);
    }

    /** 身份证：110***********1234 */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) return idCard;
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /** 姓名：张* */
    public static String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /** 银行卡：6222****1234 */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 8) return bankCard;
        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }

    /** 地址：北京市朝阳区**** */
    public static String maskAddress(String address) {
        if (address == null || address.length() < 6) return address;
        return address.substring(0, 6) + "****";
    }

    /** 密码：****** */
    public static String maskPassword(String password) {
        if (password == null) return null;
        return "******";
    }

    /**
     * 通用脱敏方法
     * @param str 原始字符串
     * @param prefixLen 前缀保留长度
     * @param suffixLen 后缀保留长度
     * @param maskChar 脱敏字符
     */
    public static String mask(String str, int prefixLen, int suffixLen, char maskChar) {
        if (str == null) return null;
        int len = str.length();
        if (len <= prefixLen + suffixLen) return str;
        StringBuilder sb = new StringBuilder();
        sb.append(str.substring(0, prefixLen));
        for (int i = 0; i < len - prefixLen - suffixLen; i++) {
            sb.append(maskChar);
        }
        sb.append(str.substring(len - suffixLen));
        return sb.toString();
    }

    /**
     * 判断是否为敏感字段
     */
    public static boolean isSensitiveField(String fieldName) {
        if (fieldName == null) return false;
        String lowerName = fieldName.toLowerCase();
        return lowerName.contains("password")
                || lowerName.contains("phone")
                || lowerName.contains("email")
                || lowerName.contains("idcard")
                || lowerName.contains("id_card")
                || lowerName.contains("bankcard")
                || lowerName.contains("bank_card")
                || lowerName.contains("address");
    }
}