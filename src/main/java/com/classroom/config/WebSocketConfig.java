package com.classroom.config;

import com.classroom.document.service.DocumentService;
import com.classroom.document.websocket.YjsWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final DocumentService documentService;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // 课堂聊天WebSocket
        registry.addHandler(new ChatWebSocketHandler(), "/ws/chat/{classroomId}")
                .setAllowedOrigins("*");

        // Yjs协同编辑WebSocket
        registry.addHandler(new YjsWebSocketHandler(documentService), "/ws/doc/{docId}")
                .setAllowedOrigins("*");
    }
}
