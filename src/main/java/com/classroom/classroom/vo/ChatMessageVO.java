package com.classroom.classroom.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 聊天消息响应
 */
@Data
public class ChatMessageVO {
    private Long id;
    private Long classroomId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private String content;
    private String msgType;
    private LocalDateTime createTime;
}
