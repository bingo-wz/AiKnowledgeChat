package com.classroom.auth.controller;

import com.classroom.auth.dto.LoginDTO;
import com.classroom.auth.dto.RegisterDTO;
import com.classroom.auth.entity.User;
import com.classroom.auth.service.AuthService;
import com.classroom.auth.vo.LoginVO;
import com.classroom.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(authService.login(dto));
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return R.ok();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public R<User> info() {
        User user = authService.getCurrentUser();
        user.setPassword(null); // 不返回密码
        return R.ok(user);
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }
}
