package com.classroom.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.ai.entity.AiConversation;
import com.classroom.ai.entity.AiMessage;
import com.classroom.ai.service.LlmService;
import com.classroom.ai.service.RagChatService;
import com.classroom.common.result.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI对话控制器
 */
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final RagChatService ragChatService;
    private final LlmService llmService;

    /**
     * 获取可用模型列表
     */
    @GetMapping("/models")
    public R<List<String>> getModels() {
        return R.ok(llmService.getAvailableModels());
    }

    /**
     * 创建对话
     */
    @PostMapping("/conversation")
    public R<Long> createConversation(@RequestBody CreateConvDTO dto) {
        return R.ok(ragChatService.createConversation(dto.getKbId(), dto.getTitle()));
    }

    /**
     * 对话列表
     */
    @GetMapping("/conversations")
    public R<Page<AiConversation>> conversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(ragChatService.getConversations(page, size));
    }

    /**
     * 获取对话消息
     */
    @GetMapping("/conversation/{id}/messages")
    public R<List<AiMessage>> messages(@PathVariable Long id) {
        return R.ok(ragChatService.getMessages(id));
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/conversation/{id}")
    public R<Void> deleteConversation(@PathVariable Long id) {
        ragChatService.deleteConversation(id);
        return R.ok();
    }

    /**
     * 发送消息（非流式）
     */
    @PostMapping("/chat")
    public R<String> chat(@RequestBody ChatDTO dto) {
        return R.ok(ragChatService.chat(dto.getConversationId(), dto.getMessage(), dto.getModel()));
    }

    /**
     * 发送消息（流式SSE）
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestBody ChatDTO dto) {
        return ragChatService.chatStream(dto.getConversationId(), dto.getMessage(), dto.getModel())
                .map(content -> "data: " + content.replace("\n", "\\n") + "\n\n");
    }

    @Data
    public static class CreateConvDTO {
        private Long kbId;
        private String title;
    }

    @Data
    public static class ChatDTO {
        private Long conversationId;
        private String message;
        private String model;
    }
}
