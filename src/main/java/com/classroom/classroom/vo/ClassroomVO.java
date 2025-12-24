package com.classroom.classroom.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂详情响应
 */
@Data
public class ClassroomVO {
    private Long id;
    private String name;
    private String description;
    private String coverUrl;
    private String status;

    /**
     * 教师信息
     */
    private Long teacherId;
    private String teacherName;
    private String teacherAvatar;

    /**
     * 成员数量
     */
    private Integer memberCount;

    /**
     * 当前用户是否是成员
     */
    private Boolean isMember;

    private LocalDateTime createTime;
}
