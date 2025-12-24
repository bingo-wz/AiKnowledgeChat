package com.classroom.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.classroom.auth.dto.LoginDTO;
import com.classroom.auth.dto.RegisterDTO;
import com.classroom.auth.entity.User;
import com.classroom.auth.mapper.UserMapper;
import com.classroom.auth.vo.LoginVO;
import com.classroom.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证服务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;

    /**
     * 用户登录
     */
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername()));

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 登录
        StpUtil.login(user.getId());

        // 返回用户信息
        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setRole(user.getRole());
        vo.setToken(StpUtil.getTokenValue());

        return vo;
    }

    /**
     * 用户注册
     */
    public void register(RegisterDTO dto) {
        // 检查用户名是否存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername()));

        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setRole(dto.getRole());
        user.setStatus(1);

        userMapper.insert(user);
    }

    /**
     * 获取当前登录用户
     */
    public User getCurrentUser() {
        long userId = StpUtil.getLoginIdAsLong();
        return userMapper.selectById(userId);
    }

    /**
     * 退出登录
     */
    public void logout() {
        StpUtil.logout();
    }
}
