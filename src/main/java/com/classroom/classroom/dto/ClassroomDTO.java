package com.classroom.classroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新课堂请求
 */
@Data
public class ClassroomDTO {

    @NotBlank(message = "课堂名称不能为空")
    private String name;

    private String description;

    private String coverUrl;
}
