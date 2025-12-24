package com.classroom.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 聊天WebSocket处理器
 */
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // 课堂ID -> 该课堂的所有会话
    private static final Map<String, Set<WebSocketSession>> CLASSROOM_SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String classroomId = extractClassroomId(session);
        CLASSROOM_SESSIONS.computeIfAbsent(classroomId, k -> new CopyOnWriteArraySet<>()).add(session);
        log.info("用户加入课堂 {}, 当前人数: {}", classroomId, CLASSROOM_SESSIONS.get(classroomId).size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String classroomId = extractClassroomId(session);
        String payload = message.getPayload();

        // 广播给课堂内所有人
        Set<WebSocketSession> sessions = CLASSROOM_SESSIONS.get(classroomId);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    try {
                        s.sendMessage(new TextMessage(payload));
                    } catch (IOException e) {
                        log.error("发送消息失败", e);
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String classroomId = extractClassroomId(session);
        Set<WebSocketSession> sessions = CLASSROOM_SESSIONS.get(classroomId);
        if (sessions != null) {
            sessions.remove(session);
            log.info("用户离开课堂 {}, 当前人数: {}", classroomId, sessions.size());
        }
    }

    private String extractClassroomId(WebSocketSession session) {
        String path = session.getUri().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
