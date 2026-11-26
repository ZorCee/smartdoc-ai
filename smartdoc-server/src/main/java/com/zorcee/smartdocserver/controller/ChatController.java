package com.zorcee.smartdocserver.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.zorcee.smartdocserver.common.Result;
import com.zorcee.smartdocserver.dto.ChatRequestDTO;
import com.zorcee.smartdocserver.entity.ChatMessage;
import com.zorcee.smartdocserver.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@SaCheckLogin
public class ChatController {

    @Autowired
    private ChatService chatService;

    // 1. 发起 RAG 知识库智能问答
    @PostMapping("/ask")
    public Result<ChatMessage> ask(@Valid @RequestBody ChatRequestDTO dto) {
        return Result.success(chatService.askQuestion(dto));
    }

    // 2. 查询指定知识库的历史对话记录
    @GetMapping("/history/{kbId}")
    public Result<List<ChatMessage>> history(@PathVariable Long kbId) {
        return Result.success(chatService.listHistory(kbId));
    }
}