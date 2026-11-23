package com.zhaoce50.smartdocserver.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kb_chat_message")
public class ChatMessage implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long kbId;             // 对应 kb_id (所属知识库ID)
    private Long userId;           // 对应 user_id (提问用户ID)
    private String question;       // 用户的提问
    private String answer;         // AI 生成的回答
    private String matchedChunks;  // 对应 matched_chunks (引用的原文切片内容)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
