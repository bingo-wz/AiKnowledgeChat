package com.classroom.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("t_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    /**
     * 角色: admin/teacher/student
     */
    private String role;

    /**
     * 状态: 1-正常 0-禁用
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
