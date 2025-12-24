package com.classroom.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建文档请求
 */
@Data
public class DocumentDTO {

    @NotBlank(message = "文档标题不能为空")
    private String title;

    /**
     * 所属课堂ID（可选）
     */
    private Long classroomId;

    /**
     * 文档类型: collab/upload/result
     */
    private String docType = "collab";

    /**
     * 文件URL（上传类型）
     */
    private String contentUrl;
}
