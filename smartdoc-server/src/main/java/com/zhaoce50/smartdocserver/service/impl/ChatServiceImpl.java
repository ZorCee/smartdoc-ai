package com.zhaoce50.smartdocserver.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhaoce50.smartdocserver.dto.ChatRequestDTO;
import com.zhaoce50.smartdocserver.entity.ChatMessage;
import com.zhaoce50.smartdocserver.entity.DocumentChunk;
import com.zhaoce50.smartdocserver.entity.KnowledgeBase;
import com.zhaoce50.smartdocserver.mapper.ChatMessageMapper;
import com.zhaoce50.smartdocserver.mapper.DocumentChunkMapper;
import com.zhaoce50.smartdocserver.service.ChatService;
import com.zhaoce50.smartdocserver.service.KnowledgeBaseService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    @Autowired
    private KnowledgeBaseService knowledgeBaseService;

    @Autowired
    private DocumentChunkMapper documentChunkMapper;

    @Value("${llm.api-url:https://api.deepseek.com/chat/completions}")
    private String llmApiUrl;

    @Value("${llm.api-key:}")
    private String llmApiKey;

    @Value("${llm.model:deepseek-chat}")
    private String llmModel;

    // 1. 获取当前用户在指定知识库下的历史对话列表
    @Override
    public List<ChatMessage> listHistory(Long kbId) {
        Long userId = StpUtil.getLoginIdAsLong();
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getKbId, kbId)
                .eq(ChatMessage::getUserId, userId)
                .orderByAsc(ChatMessage::getCreateTime);
        return list(wrapper);
    }

    // 私有检索引擎：从知识库中智能匹配与问题最相关的切片
    private List<DocumentChunk> retrieveChunks(Long kbId, String question) {
        String cleanQ = question.replaceAll("[请问一下公司的什么是怎么规定有几天吗？?！!，,。.]", "").trim();
        LambdaQueryWrapper<DocumentChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentChunk::getKbId, kbId);
        List<DocumentChunk> allChunks = documentChunkMapper.selectList(wrapper);

        List<DocumentChunk> matched = new ArrayList<>();
        for (DocumentChunk chunk : allChunks) {
            if (!cleanQ.isEmpty() && chunk.getContent().contains(cleanQ)) {
                matched.add(chunk);
            } else {
                // 二级双字滑动窗口匹配（如“年假”、“考勤”、“远程”）
                for (int i = 0; i < cleanQ.length() - 1; i++) {
                    String word = cleanQ.substring(i, i + 2);
                    if (chunk.getContent().contains(word)) {
                        matched.add(chunk);
                        break;
                    }
                }
            }
            if (matched.size() >= 3) break;
        }
        return matched;
    }
    // 2. 向知识库发起 RAG 智能提问
    @Override
    public ChatMessage askQuestion(ChatRequestDTO dto) {
        Long userId = StpUtil.getLoginIdAsLong();
        KnowledgeBase kb = knowledgeBaseService.getById(dto.getKbId());
        if (kb == null || !kb.getOwnerId().equals(userId)) {
            throw new RuntimeException("知识库不存在或无权限访问！");
        }

        // 1. 检索匹配的原文知识切片
        List<DocumentChunk> chunks = retrieveChunks(dto.getKbId(), dto.getQuestion());

        // 2. 组装引用上下文与生成 AI 回答
        StringBuilder context = new StringBuilder();
        for (DocumentChunk c : chunks) {
            context.append("【切片#").append(c.getChunkIndex()).append("】").append(c.getContent()).append("\n");
        }

        String answer;
        if (chunks.isEmpty()) {
            answer = "抱歉，在当前知识库《" + kb.getName() + "》中未检索到与“" + dto.getQuestion() + "”相关的规定或资料。";
        } else if (llmApiKey != null && !llmApiKey.isBlank()) {
            // 如果配置了 DeepSeek API Key，直接调用真实大模型生成回答
            answer = callDeepSeek(kb.getName(), context.toString(), dto.getQuestion());
        } else {
            answer = "根据知识库《" + kb.getName() + "》检索到的权威资料，为您解答如下：\n\n"
                    + context.toString().trim()
                    + "\n\n（以上回答严格依据企业知识库原文生成，共计命中 " + chunks.size() + " 处知识切片）";
        }

        // 3. 将本轮问答与引用出处持久化入库
        ChatMessage msg = new ChatMessage();
        msg.setKbId(dto.getKbId());
        msg.setUserId(userId);
        msg.setQuestion(dto.getQuestion());
        msg.setAnswer(answer);
        msg.setMatchedChunks(context.toString().trim());
        save(msg);
        return msg;
    }

    // 调用 DeepSeek / OpenAI 兼容大模型接口生成 RAG 回答
    private String callDeepSeek(String kbName, String context, String question) {
        try {
            String systemPrompt = "你是企业知识库《" + kbName + "》的智能助手。请严格根据以下【参考切片】回答用户问题，并在末尾注明引用的切片编号。若切片中无相关内容，请如实说明。\n\n【参考切片】：\n" + context;
            JSONObject body = new JSONObject();
            body.put("model", llmModel);
            body.put("temperature", 0.2);
            body.put("messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", question)
            ));

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(llmApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + llmApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body.toJSONString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONArray choices = JSON.parseObject(response.body()).getJSONArray("choices");
            return choices.getJSONObject(0).getJSONObject("message").getString("content");
        } catch (Exception e) {
            return "大模型调用异常（已自动降级展示原文切片）：\n\n" + context;
        }
    }
}