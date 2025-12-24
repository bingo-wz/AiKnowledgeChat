package com.classroom.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库向量实体
 */
@Data
@TableName("t_kb_embedding")
public class KbEmbedding {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID
     */
    private Long kbId;

    /**
     * 文档ID
     */
    private Long docId;

    /**
     * 切片索引
     */
    private Integer chunkIndex;

    /**
     * 切片内容
     */
    private String content;

    /**
     * 向量（PostgreSQL中用float[]存储）
     */
    // 注意：向量字段通过SQL直接操作，不在Java中映射

    private LocalDateTime createTime;
}
