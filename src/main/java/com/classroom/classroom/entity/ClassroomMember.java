package com.classroom.classroom.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 课堂成员实体
 */
@Data
@TableName("t_classroom_member")
public class ClassroomMember {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 课堂ID
     */
    private Long classroomId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 角色: teacher/student
     */
    private String role;

    /**
     * 加入时间
     */
    private LocalDateTime joinTime;
}
