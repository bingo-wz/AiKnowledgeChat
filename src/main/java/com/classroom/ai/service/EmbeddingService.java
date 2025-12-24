package com.classroom.ai.service;

import com.classroom.ai.config.AiModelProperties;
import com.classroom.ai.config.AiModelProperties.ModelConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Embedding向量服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final AiModelProperties aiModelProperties;

    // Embedding模型缓存
    private final Map<String, EmbeddingModel> embeddingCache = new ConcurrentHashMap<>();

    /**
     * 获取文本向量
     */
    public float[] embed(String modelKey, String text) {
        EmbeddingModel model = getEmbeddingModel(modelKey);
        EmbeddingResponse response = model.embedForResponse(List.of(text));
        return response.getResult().getOutput();
    }

    /**
     * 批量获取文本向量
     */
    public List<float[]> embedBatch(String modelKey, List<String> texts) {
        EmbeddingModel model = getEmbeddingModel(modelKey);
        EmbeddingResponse response = model.embedForResponse(texts);
        return response.getResults().stream()
                .map(r -> r.getOutput())
                .collect(Collectors.toList());
    }

    /**
     * 向量转字符串（用于数据库存储）
     */
    public String vectorToString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private EmbeddingModel getEmbeddingModel(String modelKey) {
        String key = modelKey != null ? modelKey : aiModelProperties.getDefaultModel();

        return embeddingCache.computeIfAbsent(key, k -> {
            ModelConfig config = aiModelProperties.getModels().get(k);
            if (config == null) {
                throw new IllegalArgumentException("Model not configured: " + k);
            }

            OpenAiApi api = new OpenAiApi(config.getBaseUrl(), config.getApiKey());

            String embeddingModelName = config.getEmbeddingModel();
            if (embeddingModelName == null) {
                embeddingModelName = "text-embedding-v3";
            }

            OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
                    .withModel(embeddingModelName)
                    .build();

            // Spring AI 1.0.0-M4 构造函数
            return new OpenAiEmbeddingModel(api, org.springframework.ai.document.MetadataMode.EMBED, options);
        });
    }
}
