package com.rx.admin.modules.system.permission.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionSubmitDTO {
    private List<Long> menuIds;
    private List<String> menuNames;
}
