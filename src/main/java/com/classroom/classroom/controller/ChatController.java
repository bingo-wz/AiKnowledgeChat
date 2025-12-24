package com.classroom.classroom.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.classroom.service.ChatService;
import com.classroom.classroom.vo.ChatMessageVO;
import com.classroom.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取课堂历史消息
     */
    @GetMapping("/{classroomId}/messages")
    public R<Page<ChatMessageVO>> getMessages(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        return R.ok(chatService.getMessages(classroomId, page, size));
    }
}
