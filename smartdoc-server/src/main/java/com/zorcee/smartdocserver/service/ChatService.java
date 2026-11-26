package com.zorcee.smartdocserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zorcee.smartdocserver.dto.ChatRequestDTO;
import com.zorcee.smartdocserver.entity.ChatMessage;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {

    // 1. 向知识库发起 RAG 智能提问
    ChatMessage askQuestion(ChatRequestDTO dto);

    // 2. 获取当前用户在指定知识库下的历史对话列表
    List<ChatMessage> listHistory(Long kbId);
}