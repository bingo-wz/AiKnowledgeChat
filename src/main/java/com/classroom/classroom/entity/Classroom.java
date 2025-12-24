package com.classroom.classroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂实体
 */
@Data
@TableName("t_classroom")
public class Classroom {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课堂名称
     */
    private String name;

    /**
     * 课堂描述
     */
    private String description;

    /**
     * 创建教师ID
     */
    private Long teacherId;

    /**
     * 封面图URL
     */
    private String coverUrl;

    /**
     * 状态: active/closed
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
