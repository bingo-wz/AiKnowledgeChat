package com.classroom.classroom.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.auth.entity.User;
import com.classroom.auth.mapper.UserMapper;
import com.classroom.classroom.dto.ClassroomDTO;
import com.classroom.classroom.entity.Classroom;
import com.classroom.classroom.entity.ClassroomMember;
import com.classroom.classroom.mapper.ClassroomMapper;
import com.classroom.classroom.mapper.ClassroomMemberMapper;
import com.classroom.classroom.vo.ClassroomVO;
import com.classroom.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 课堂服务
 */
@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomMapper classroomMapper;
    private final ClassroomMemberMapper memberMapper;
    private final UserMapper userMapper;

    /**
     * 创建课堂
     */
    @Transactional
    public Long createClassroom(ClassroomDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();

        Classroom classroom = new Classroom();
        classroom.setName(dto.getName());
        classroom.setDescription(dto.getDescription());
        classroom.setCoverUrl(dto.getCoverUrl());
        classroom.setTeacherId(userId);
        classroom.setStatus("active");

        classroomMapper.insert(classroom);

        // 创建者自动成为成员（教师角色）
        ClassroomMember member = new ClassroomMember();
        member.setClassroomId(classroom.getId());
        member.setUserId(userId);
        member.setRole("teacher");
        memberMapper.insert(member);

        return classroom.getId();
    }

    /**
     * 更新课堂
     */
    public void updateClassroom(Long id, ClassroomDTO dto) {
        Classroom classroom = classroomMapper.selectById(id);
        if (classroom == null) {
            throw new BusinessException("课堂不存在");
        }

        // 只有创建者可以修改
        long userId = StpUtil.getLoginIdAsLong();
        if (!classroom.getTeacherId().equals(userId)) {
            throw new BusinessException("无权修改此课堂");
        }

        classroom.setName(dto.getName());
        classroom.setDescription(dto.getDescription());
        classroom.setCoverUrl(dto.getCoverUrl());
        classroom.setUpdateTime(LocalDateTime.now());

        classroomMapper.updateById(classroom);
    }

    /**
     * 删除课堂
     */
    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = classroomMapper.selectById(id);
        if (classroom == null) {
            throw new BusinessException("课堂不存在");
        }

        long userId = StpUtil.getLoginIdAsLong();
        if (!classroom.getTeacherId().equals(userId)) {
            throw new BusinessException("无权删除此课堂");
        }

        // 删除成员关系
        memberMapper.delete(new LambdaQueryWrapper<ClassroomMember>()
                .eq(ClassroomMember::getClassroomId, id));

        // 删除课堂
        classroomMapper.deleteById(id);
    }

    /**
     * 获取课堂详情
     */
    public ClassroomVO getClassroomDetail(Long id) {
        Classroom classroom = classroomMapper.selectById(id);
        if (classroom == null) {
            throw new BusinessException("课堂不存在");
        }
        return toVO(classroom);
    }

    /**
     * 获取我的课堂列表
     */
    public Page<ClassroomVO> getMyClassrooms(int page, int size) {
        long userId = StpUtil.getLoginIdAsLong();

        // 获取我加入的课堂ID列表
        List<Long> classroomIds = memberMapper.selectList(
                new LambdaQueryWrapper<ClassroomMember>()
                        .eq(ClassroomMember::getUserId, userId)
                        .select(ClassroomMember::getClassroomId))
                .stream().map(ClassroomMember::getClassroomId).collect(Collectors.toList());

        if (classroomIds.isEmpty()) {
            return new Page<>(page, size);
        }

        Page<Classroom> pageResult = classroomMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Classroom>()
                        .in(Classroom::getId, classroomIds)
                        .orderByDesc(Classroom::getCreateTime));

        Page<ClassroomVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream()
                .map(this::toVO)
                .collect(Collectors.toList()));

        return voPage;
    }

    /**
     * 加入课堂
     */
    public void joinClassroom(Long classroomId) {
        Classroom classroom = classroomMapper.selectById(classroomId);
        if (classroom == null) {
            throw new BusinessException("课堂不存在");
        }

        long userId = StpUtil.getLoginIdAsLong();

        // 检查是否已加入
        Long count = memberMapper.selectCount(
                new LambdaQueryWrapper<ClassroomMember>()
                        .eq(ClassroomMember::getClassroomId, classroomId)
                        .eq(ClassroomMember::getUserId, userId));
        if (count > 0) {
            throw new BusinessException("已经是课堂成员");
        }

        ClassroomMember member = new ClassroomMember();
        member.setClassroomId(classroomId);
        member.setUserId(userId);
        member.setRole("student");
        memberMapper.insert(member);
    }

    /**
     * 退出课堂
     */
    public void leaveClassroom(Long classroomId) {
        long userId = StpUtil.getLoginIdAsLong();

        Classroom classroom = classroomMapper.selectById(classroomId);
        if (classroom != null && classroom.getTeacherId().equals(userId)) {
            throw new BusinessException("课堂创建者不能退出");
        }

        memberMapper.delete(new LambdaQueryWrapper<ClassroomMember>()
                .eq(ClassroomMember::getClassroomId, classroomId)
                .eq(ClassroomMember::getUserId, userId));
    }

    /**
     * 获取课堂成员列表
     */
    public List<User> getClassroomMembers(Long classroomId) {
        List<Long> userIds = memberMapper.selectList(
                new LambdaQueryWrapper<ClassroomMember>()
                        .eq(ClassroomMember::getClassroomId, classroomId))
                .stream().map(ClassroomMember::getUserId).collect(Collectors.toList());

        if (userIds.isEmpty()) {
            return List.of();
        }

        List<User> users = userMapper.selectBatchIds(userIds);
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    private ClassroomVO toVO(Classroom classroom) {
        ClassroomVO vo = new ClassroomVO();
        vo.setId(classroom.getId());
        vo.setName(classroom.getName());
        vo.setDescription(classroom.getDescription());
        vo.setCoverUrl(classroom.getCoverUrl());
        vo.setStatus(classroom.getStatus());
        vo.setTeacherId(classroom.getTeacherId());
        vo.setCreateTime(classroom.getCreateTime());

        // 获取教师信息
        User teacher = userMapper.selectById(classroom.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getNickname());
            vo.setTeacherAvatar(teacher.getAvatar());
        }

        // 获取成员数量
        Long memberCount = memberMapper.selectCount(
                new LambdaQueryWrapper<ClassroomMember>()
                        .eq(ClassroomMember::getClassroomId, classroom.getId()));
        vo.setMemberCount(memberCount.intValue());

        // 当前用户是否是成员
        try {
            long userId = StpUtil.getLoginIdAsLong();
            Long count = memberMapper.selectCount(
                    new LambdaQueryWrapper<ClassroomMember>()
                            .eq(ClassroomMember::getClassroomId, classroom.getId())
                            .eq(ClassroomMember::getUserId, userId));
            vo.setIsMember(count > 0);
        } catch (Exception e) {
            vo.setIsMember(false);
        }

        return vo;
    }
}
