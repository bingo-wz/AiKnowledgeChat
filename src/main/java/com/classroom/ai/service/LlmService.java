package com.classroom.ai.service;

import com.classroom.ai.config.AiModelProperties;
import com.classroom.ai.config.AiModelProperties.ModelConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 多模型LLM服务 - 直接HTTP调用实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmService {

    private final AiModelProperties aiModelProperties;
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, WebClient> webClientCache = new ConcurrentHashMap<>();

    /**
     * 获取可用模型列表
     */
    public List<String> getAvailableModels() {
        Map<String, ModelConfig> models = aiModelProperties.getModels();
        return models != null ? new ArrayList<>(models.keySet()) : List.of();
    }

    /**
     * 对话（流式）- 暂时使用非流式实现
     */
    public Flux<String> chatStream(String modelKey, String systemPrompt, List<ChatMessage> history, String userInput) {
        String response = chat(modelKey, systemPrompt, history, userInput);
        return Flux.just(response);
    }

    /**
     * 对话（非流式）
     */
    public String chat(String modelKey, String systemPrompt, List<ChatMessage> history, String userInput) {
        String key = modelKey != null ? modelKey : aiModelProperties.getDefaultModel();
        ModelConfig config = aiModelProperties.getModels().get(key);

        if (config == null) {
            throw new IllegalArgumentException("Model not configured: " + key);
        }

        log.info("Calling model: {} at {}", key, config.getBaseUrl());

        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();

        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }

        if (history != null) {
            for (ChatMessage msg : history) {
                messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
            }
        }

        messages.add(Map.of("role", "user", "content", userInput));

        // 构建请求体
        Map<String, Object> requestBody = Map.of(
                "model", config.getModel(),
                "messages", messages,
                "temperature", 0.7);

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + config.getApiKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String url = config.getBaseUrl() + "/chat/completions";
            ResponseEntity<ChatCompletionResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    ChatCompletionResponse.class);

            if (response.getBody() != null &&
                    response.getBody().getChoices() != null &&
                    !response.getBody().getChoices().isEmpty()) {
                return response.getBody().getChoices().get(0).getMessage().getContent();
            }

            return "No response from AI";
        } catch (Exception e) {
            log.error("Error calling AI API: {}", e.getMessage(), e);
            throw new RuntimeException("AI call failed: " + e.getMessage());
        }
    }

    /**
     * 聊天消息
     */
    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ChatMessage {
        private String role;
        private String content;
    }

    /**
     * API响应结构
     */
    @Data
    public static class ChatCompletionResponse {
        private String id;
        private String model;
        private List<Choice> choices;
        private Usage usage;

        @Data
        public static class Choice {
            private int index;
            private Message message;
            @JsonProperty("finish_reason")
            private String finishReason;
        }

        @Data
        public static class Message {
            private String role;
            private String content;
        }

        @Data
        public static class Usage {
            @JsonProperty("prompt_tokens")
            private int promptTokens;
            @JsonProperty("completion_tokens")
            private int completionTokens;
            @JsonProperty("total_tokens")
            private int totalTokens;
        }
    }
}
