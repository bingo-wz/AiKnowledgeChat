package com.classroom.classroom.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.classroom.classroom.entity.ClassroomMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 课堂成员Mapper
 */
@Mapper
public interface ClassroomMemberMapper extends BaseMapper<ClassroomMember> {
}
