package com.zhaoce50.smartdocserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhaoce50.smartdocserver.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {
}