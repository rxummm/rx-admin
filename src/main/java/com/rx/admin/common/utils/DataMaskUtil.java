package com.rx.admin.common.utils;

/**
 * 数据脱敏工具类
 * 用于返回前端时对敏感字段进行脱敏处理
 */
public class DataMaskUtil {

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
        return name.charAt(0) + "**";
    }
}