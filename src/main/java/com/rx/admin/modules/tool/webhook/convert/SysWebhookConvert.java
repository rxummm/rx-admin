package com.rx.admin.modules.tool.webhook.convert;

import com.rx.admin.modules.tool.webhook.dto.SysWebhookCreateDTO;
import com.rx.admin.modules.tool.webhook.entity.SysWebhook;
import com.rx.admin.modules.tool.webhook.vo.SysWebhookVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SysWebhookConvert {
    SysWebhook toEntity(SysWebhookCreateDTO dto);
    SysWebhookVO toVO(SysWebhook entity);
    List<SysWebhookVO> toVOList(List<SysWebhook> list);
}
