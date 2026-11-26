package com.zorcee.smartdocserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zorcee.smartdocserver.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}