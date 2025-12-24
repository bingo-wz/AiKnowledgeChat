package com.classroom.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 多模型配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiModelProperties {

    /**
     * 默认模型
     */
    private String defaultModel = "qwen-turbo";

    /**
     * 模型配置映射
     */
    private Map<String, ModelConfig> models;

    @Data
    public static class ModelConfig {
        /**
         * API地址
         */
        private String baseUrl;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 模型名称
         */
        private String model;

        /**
         * Embedding模型（可选）
         */
        private String embeddingModel;
    }
}
