package com.zorcee.smartdocserver.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zorcee.smartdocserver.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {
}