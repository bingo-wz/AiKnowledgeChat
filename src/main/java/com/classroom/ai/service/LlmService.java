package com.classroom.ai.service;

import com.classroom.ai.config.AiModelProperties;
import com.classroom.ai.config.AiModelProperties.ModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多模型LLM服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final AiModelProperties aiModelProperties;

    // 模型实例缓存
    private final Map<String, OpenAiChatModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 获取可用模型列表
     */
    public List<String> getAvailableModels() {
        Map<String, ModelConfig> models = aiModelProperties.getModels();
        return models != null ? new ArrayList<>(models.keySet()) : List.of();
    }

    /**
     * 对话（流式）
     */
    public Flux<String> chatStream(String modelKey, String systemPrompt, List<ChatMessage> history, String userInput) {
        OpenAiChatModel chatModel = getChatModel(modelKey);

        List<Message> messages = new ArrayList<>();

        // 系统提示
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        // 历史消息
        if (history != null) {
            for (ChatMessage msg : history) {
                if ("user".equals(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }

        // 当前用户输入
        messages.add(new UserMessage(userInput));

        Prompt prompt = new Prompt(messages);

        return chatModel.stream(prompt)
                .map(response -> {
                    if (response.getResult() != null &&
                            response.getResult().getOutput() != null &&
                            response.getResult().getOutput().getContent() != null) {
                        return response.getResult().getOutput().getContent();
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty());
    }

    /**
     * 对话（非流式）
     */
    public String chat(String modelKey, String systemPrompt, List<ChatMessage> history, String userInput) {
        OpenAiChatModel chatModel = getChatModel(modelKey);

        List<Message> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(new SystemMessage(systemPrompt));
        }

        if (history != null) {
            for (ChatMessage msg : history) {
                if ("user".equals(msg.getRole())) {
                    messages.add(new UserMessage(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    messages.add(new AssistantMessage(msg.getContent()));
                }
            }
        }

        messages.add(new UserMessage(userInput));

        Prompt prompt = new Prompt(messages);
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getContent();
    }

    private OpenAiChatModel getChatModel(String modelKey) {
        String key = modelKey != null ? modelKey : aiModelProperties.getDefaultModel();

        return modelCache.computeIfAbsent(key, k -> {
            ModelConfig config = aiModelProperties.getModels().get(k);
            if (config == null) {
                throw new IllegalArgumentException("未配置的模型: " + k);
            }

            OpenAiApi api = new OpenAiApi(config.getBaseUrl(), config.getApiKey());
            OpenAiChatOptions options = OpenAiChatOptions.builder()
                    .withModel(config.getModel())
                    .build();

            return new OpenAiChatModel(api, options);
        });
    }

    /**
     * 聊天消息
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
