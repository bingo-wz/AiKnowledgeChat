package com.classroom.document.websocket;

import com.classroom.document.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.BinaryWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Yjs协同编辑WebSocket处理器
 * 
 * 协议说明：
 * - 客户端发送Yjs更新消息（二进制）
 * - 服务端广播给同一文档的所有其他用户
 * - 服务端定期保存文档状态到数据库
 */
@Slf4j
public class YjsWebSocketHandler extends BinaryWebSocketHandler {

    // 文档ID -> 该文档的所有会话
    private static final Map<Long, Set<WebSocketSession>> DOC_SESSIONS = new ConcurrentHashMap<>();

    // 会话 -> 文档ID
    private static final Map<String, Long> SESSION_DOC_MAP = new ConcurrentHashMap<>();

    // 文档ID -> 最新状态（内存缓存，定期持久化）
    private static final Map<Long, byte[]> DOC_STATE_CACHE = new ConcurrentHashMap<>();

    private final DocumentService documentService;

    public YjsWebSocketHandler(DocumentService documentService) {
        this.documentService = documentService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long docId = extractDocId(session);
        if (docId == null) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭会话失败", e);
            }
            return;
        }

        DOC_SESSIONS.computeIfAbsent(docId, k -> new CopyOnWriteArraySet<>()).add(session);
        SESSION_DOC_MAP.put(session.getId(), docId);

        log.info("用户加入文档 {}, 当前编辑人数: {}", docId, DOC_SESSIONS.get(docId).size());

        // 发送当前文档状态
        sendCurrentState(session, docId);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        Long docId = SESSION_DOC_MAP.get(session.getId());
        if (docId == null) {
            return;
        }

        byte[] payload = message.getPayload().array();

        // 解析消息类型（第一个字节）
        if (payload.length > 0) {
            int msgType = payload[0] & 0xFF;

            switch (msgType) {
                case 0: // sync step 1 - 请求同步
                    handleSyncStep1(session, docId, payload);
                    break;
                case 1: // sync step 2 - 同步响应
                    handleSyncStep2(session, docId, payload);
                    break;
                case 2: // update - Yjs更新
                    handleUpdate(session, docId, payload);
                    break;
                default:
                    // 未知消息类型，广播给其他用户
                    broadcastToOthers(docId, session, payload);
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long docId = SESSION_DOC_MAP.remove(session.getId());
        if (docId != null) {
            Set<WebSocketSession> sessions = DOC_SESSIONS.get(docId);
            if (sessions != null) {
                sessions.remove(session);
                log.info("用户离开文档 {}, 当前编辑人数: {}", docId, sessions.size());

                // 如果没有用户了，保存状态并清理缓存
                if (sessions.isEmpty()) {
                    saveDocState(docId);
                    DOC_SESSIONS.remove(docId);
                    DOC_STATE_CACHE.remove(docId);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket传输错误: {}", exception.getMessage());
        afterConnectionClosed(session, CloseStatus.SERVER_ERROR);
    }

    /**
     * 发送当前文档状态给新加入的用户
     */
    private void sendCurrentState(WebSocketSession session, Long docId) {
        try {
            // 先从缓存获取，没有则从数据库加载
            byte[] state = DOC_STATE_CACHE.get(docId);
            if (state == null) {
                state = documentService.getYdocState(docId);
                if (state != null) {
                    DOC_STATE_CACHE.put(docId, state);
                }
            }

            if (state != null && state.length > 0) {
                // 发送sync step 2消息（包含文档状态）
                byte[] syncMsg = new byte[state.length + 1];
                syncMsg[0] = 1; // sync step 2
                System.arraycopy(state, 0, syncMsg, 1, state.length);
                session.sendMessage(new BinaryMessage(ByteBuffer.wrap(syncMsg)));
            }
        } catch (IOException e) {
            log.error("发送文档状态失败", e);
        }
    }

    /**
     * 处理sync step 1（请求同步）
     */
    private void handleSyncStep1(WebSocketSession session, Long docId, byte[] payload) {
        // 客户端请求同步，发送当前状态
        sendCurrentState(session, docId);
    }

    /**
     * 处理sync step 2（同步响应）
     */
    private void handleSyncStep2(WebSocketSession session, Long docId, byte[] payload) {
        // 广播给其他用户
        broadcastToOthers(docId, session, payload);
    }

    /**
     * 处理Yjs更新消息
     */
    private void handleUpdate(WebSocketSession session, Long docId, byte[] payload) {
        // 更新缓存（简化处理：直接存储最新的完整状态）
        // 实际生产中应该使用Yjs的mergeUpdates合并增量更新
        if (payload.length > 1) {
            byte[] update = new byte[payload.length - 1];
            System.arraycopy(payload, 1, update, 0, update.length);

            // 这里简化处理，实际应合并更新
            // 定期保存时会保存最新状态
        }

        // 广播给其他用户
        broadcastToOthers(docId, session, payload);
    }

    /**
     * 广播消息给同一文档的其他用户
     */
    private void broadcastToOthers(Long docId, WebSocketSession sender, byte[] payload) {
        Set<WebSocketSession> sessions = DOC_SESSIONS.get(docId);
        if (sessions != null) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen() && !s.getId().equals(sender.getId())) {
                    try {
                        s.sendMessage(new BinaryMessage(ByteBuffer.wrap(payload)));
                    } catch (IOException e) {
                        log.error("广播消息失败", e);
                    }
                }
            }
        }
    }

    /**
     * 保存文档状态到数据库
     */
    private void saveDocState(Long docId) {
        byte[] state = DOC_STATE_CACHE.get(docId);
        if (state != null) {
            documentService.saveYdocState(docId, state);
            log.info("文档 {} 状态已保存", docId);
        }
    }

    /**
     * 更新文档状态缓存（供外部调用保存最新状态）
     */
    public static void updateStateCache(Long docId, byte[] state) {
        DOC_STATE_CACHE.put(docId, state);
    }

    private Long extractDocId(WebSocketSession session) {
        try {
            String path = session.getUri().getPath();
            return Long.parseLong(path.substring(path.lastIndexOf('/') + 1));
        } catch (Exception e) {
            log.error("解析文档ID失败", e);
            return null;
        }
    }
}
