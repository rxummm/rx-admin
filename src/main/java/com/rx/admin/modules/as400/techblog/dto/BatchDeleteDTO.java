package com.rx.admin.modules.as400.techblog.dto;

import lombok.Data;
import java.util.List;

@Data
public class BatchDeleteDTO {
    private List<Integer> ids;
}
