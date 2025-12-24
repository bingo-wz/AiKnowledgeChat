package com.classroom.classroom.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.auth.entity.User;
import com.classroom.classroom.dto.ClassroomDTO;
import com.classroom.classroom.service.ClassroomService;
import com.classroom.classroom.vo.ClassroomVO;
import com.classroom.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课堂控制器
 */
@RestController
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    /**
     * 创建课堂
     */
    @PostMapping
    public R<Long> create(@Valid @RequestBody ClassroomDTO dto) {
        return R.ok(classroomService.createClassroom(dto));
    }

    /**
     * 更新课堂
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ClassroomDTO dto) {
        classroomService.updateClassroom(id, dto);
        return R.ok();
    }

    /**
     * 删除课堂
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return R.ok();
    }

    /**
     * 获取课堂详情
     */
    @GetMapping("/{id}")
    public R<ClassroomVO> detail(@PathVariable Long id) {
        return R.ok(classroomService.getClassroomDetail(id));
    }

    /**
     * 获取我的课堂列表
     */
    @GetMapping("/my")
    public R<Page<ClassroomVO>> myClassrooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return R.ok(classroomService.getMyClassrooms(page, size));
    }

    /**
     * 加入课堂
     */
    @PostMapping("/{id}/join")
    public R<Void> join(@PathVariable Long id) {
        classroomService.joinClassroom(id);
        return R.ok();
    }

    /**
     * 退出课堂
     */
    @PostMapping("/{id}/leave")
    public R<Void> leave(@PathVariable Long id) {
        classroomService.leaveClassroom(id);
        return R.ok();
    }

    /**
     * 获取课堂成员
     */
    @GetMapping("/{id}/members")
    public R<List<User>> members(@PathVariable Long id) {
        return R.ok(classroomService.getClassroomMembers(id));
    }
}
