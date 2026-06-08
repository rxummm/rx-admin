package com.rx.admin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AS400 Object 信息 VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class As400ObjectVO {

    /** 对象名称 */
    private String objectName;

    /** 对象类型 (如 *PGM, *FILE, *MSGF 等) */
    private String objectType;

    /** 所属 Library */
    private String library;

    /** 对象属性 (如 CLP, PF, LF 等) */
    private String attribute;

    /** 对象描述文本 */
    private String text;

    /** 对象所有者 */
    private String owner;

    /** 创建日期 */
    private String createDate;

    /** 对象大小（字节） */
    private Long size;
}
