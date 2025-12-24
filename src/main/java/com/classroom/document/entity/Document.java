package com.classroom.document.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 协同文档实体
 */
@Data
@TableName("t_document")
public class Document {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属课堂ID（可为空，表示个人文档）
     */
    private Long classroomId;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档类型: collab(协同编辑)/upload(上传)/result(成果)
     */
    private String docType;

    /**
     * 文件URL（上传类型）
     */
    private String contentUrl;

    /**
     * Yjs文档状态（协同编辑用）
     */
    private byte[] ydocState;

    /**
     * 版本号
     */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
