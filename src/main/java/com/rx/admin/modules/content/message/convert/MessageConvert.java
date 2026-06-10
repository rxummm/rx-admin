package com.rx.admin.modules.content.message.convert;

import com.rx.admin.modules.content.message.dto.MessageCreateDTO;
import com.rx.admin.modules.content.message.vo.MessageVO;
import com.rx.admin.entity.SysMessage;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/** 消息对象转换器 */
@Mapper(componentModel = "spring")
public interface MessageConvert {
    MessageConvert INSTANCE = Mappers.getMapper(MessageConvert.class);

    SysMessage toEntity(MessageCreateDTO dto);
    MessageVO toVO(SysMessage entity);
    List<MessageVO> toVOList(List<SysMessage> list);
}
