package com.rx.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_dict_data")
public class SysDictData {
    @TableId(type = IdType.AUTO)
    private Long id;
    @NotNull(message = "字典类型不能为空")
    private Long typeId;
    @NotBlank(message = "字典标签不能为空")
    private String dictLabel;
    @NotBlank(message = "字典值不能为空")
    private String dictValue;
    private String cssClass;
    private String listClass;
    private Integer sort;
    private Integer status;
    private String remark;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
