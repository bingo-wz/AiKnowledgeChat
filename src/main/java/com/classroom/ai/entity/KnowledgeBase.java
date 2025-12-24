package com.classroom.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库实体
 */
@Data
@TableName("t_knowledge_base")
public class KnowledgeBase {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 文档数量
     */
    private Integer docCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
