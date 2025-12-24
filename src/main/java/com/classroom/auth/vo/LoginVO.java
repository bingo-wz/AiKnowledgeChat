package com.classroom.auth.vo;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private String role;
    private String token;
}
