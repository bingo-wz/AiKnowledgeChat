package com.classroom.ai.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.ai.entity.AiConversation;
import com.classroom.ai.entity.AiMessage;
import com.classroom.ai.entity.KnowledgeBase;
import com.classroom.ai.mapper.AiConversationMapper;
import com.classroom.ai.mapper.AiMessageMapper;
import com.classroom.ai.mapper.KbEmbeddingMapper;
import com.classroom.ai.mapper.KnowledgeBaseMapper;
import com.classroom.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG对话服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagChatService {

    private final LlmService llmService;
    private final EmbeddingService embeddingService;
    private final KbEmbeddingMapper embeddingMapper;
    private final KnowledgeBaseMapper kbMapper;
    private final AiConversationMapper conversationMapper;
    private final AiMessageMapper messageMapper;

    private static final int TOP_K = 5;
    private static final String RAG_SYSTEM_PROMPT = "You are an AI teaching assistant. Please answer questions based on the following references.\n"
            +
            "If no relevant information is available, honestly tell the user.\n\n" +
            "References:\n%s";

    /**
     * 创建对话
     */
    public Long createConversation(Long kbId, String title) {
        long userId = StpUtil.getLoginIdAsLong();

        AiConversation conv = new AiConversation();
        conv.setUserId(userId);
        conv.setKbId(kbId);
        conv.setTitle(title != null ? title : "新对话");

        conversationMapper.insert(conv);
        return conv.getId();
    }

    /**
     * RAG对话（流式）
     */
    public Flux<String> chatStream(Long conversationId, String userInput, String modelKey) {
        AiConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException("对话不存在");
        }

        // 保存用户消息
        saveMessage(conversationId, "user", userInput, modelKey);

        // 构建系统提示
        String systemPrompt = buildSystemPrompt(conv.getKbId(), userInput);

        // 获取历史消息
        List<LlmService.ChatMessage> history = getHistory(conversationId);

        // 收集完整响应
        StringBuilder fullResponse = new StringBuilder();

        return llmService.chatStream(modelKey, systemPrompt, history.subList(0, history.size() - 1), userInput)
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> {
                    // 保存助手回复
                    saveMessage(conversationId, "assistant", fullResponse.toString(), modelKey);

                    // 更新对话时间
                    conv.setUpdateTime(LocalDateTime.now());
                    conversationMapper.updateById(conv);
                });
    }

    /**
     * RAG对话（非流式）
     */
    public String chat(Long conversationId, String userInput, String modelKey) {
        AiConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            throw new BusinessException("对话不存在");
        }

        // 保存用户消息
        saveMessage(conversationId, "user", userInput, modelKey);

        // 构建系统提示
        String systemPrompt = buildSystemPrompt(conv.getKbId(), userInput);

        // 获取历史消息
        List<LlmService.ChatMessage> history = getHistory(conversationId);

        // 调用LLM
        String response = llmService.chat(modelKey, systemPrompt, history.subList(0, history.size() - 1), userInput);

        // 保存助手回复
        saveMessage(conversationId, "assistant", response, modelKey);

        // 更新对话时间
        conv.setUpdateTime(LocalDateTime.now());
        conversationMapper.updateById(conv);

        return response;
    }

    /**
     * 获取用户的对话列表（按月分组）
     */
    public Page<AiConversation> getConversations(int page, int size) {
        long userId = StpUtil.getLoginIdAsLong();

        return conversationMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<AiConversation>()
                        .eq(AiConversation::getUserId, userId)
                        .orderByDesc(AiConversation::getUpdateTime));
    }

    /**
     * 获取对话消息
     */
    public List<AiMessage> getMessages(Long conversationId) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreateTime));
    }

    /**
     * 删除对话
     */
    public void deleteConversation(Long conversationId) {
        AiConversation conv = conversationMapper.selectById(conversationId);
        if (conv == null) {
            return;
        }

        long userId = StpUtil.getLoginIdAsLong();
        if (!conv.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此对话");
        }

        // 删除消息
        messageMapper.delete(new LambdaQueryWrapper<AiMessage>()
                .eq(AiMessage::getConversationId, conversationId));

        // 删除对话
        conversationMapper.deleteById(conversationId);
    }

    /**
     * 构建系统提示（包含RAG检索结果）
     */
    private String buildSystemPrompt(Long kbId, String query) {
        if (kbId == null) {
            return "你是AI智慧教研室的智能助手，请友好地回答用户的问题。";
        }

        // 向量检索
        float[] queryVector = embeddingService.embed(null, query);
        String vectorStr = embeddingService.vectorToString(queryVector);

        List<KbEmbeddingMapper.VectorSearchResult> results = embeddingMapper.searchSimilar(kbId, vectorStr, TOP_K);

        if (results.isEmpty()) {
            return "你是AI智慧教研室的智能助手。当前知识库暂无相关资料，请根据你的通用知识回答问题。";
        }

        // 拼接参考资料
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            context.append(String.format("[%d] %s\n\n", i + 1, results.get(i).getContent()));
        }

        return String.format(RAG_SYSTEM_PROMPT, context.toString());
    }

    /**
     * 获取历史消息
     */
    private List<LlmService.ChatMessage> getHistory(Long conversationId) {
        List<AiMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<AiMessage>()
                        .eq(AiMessage::getConversationId, conversationId)
                        .orderByAsc(AiMessage::getCreateTime)
                        .last("LIMIT 20") // 限制历史长度
        );

        return messages.stream()
                .map(m -> new LlmService.ChatMessage(m.getRole(), m.getContent()))
                .collect(Collectors.toList());
    }

    private void saveMessage(Long conversationId, String role, String content, String model) {
        AiMessage message = new AiMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setModel(model);
        messageMapper.insert(message);
    }
}
