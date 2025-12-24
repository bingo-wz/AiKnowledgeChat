package com.classroom.document.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文档响应
 */
@Data
public class DocumentVO {
    private Long id;
    private Long classroomId;
    private String classroomName;
    private Long creatorId;
    private String creatorName;
    private String title;
    private String docType;
    private String contentUrl;
    private Integer version;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
