package com.classroom.document.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.common.result.R;
import com.classroom.document.dto.DocumentDTO;
import com.classroom.document.service.DocumentService;
import com.classroom.document.vo.DocumentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文档控制器
 */
@RestController
@RequestMapping("/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 创建文档
     */
    @PostMapping
    public R<Long> create(@Valid @RequestBody DocumentDTO dto) {
        return R.ok(documentService.createDocument(dto));
    }

    /**
     * 更新文档
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody DocumentDTO dto) {
        documentService.updateDocument(id, dto);
        return R.ok();
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return R.ok();
    }

    /**
     * 获取文档详情
     */
    @GetMapping("/{id}")
    public R<DocumentVO> detail(@PathVariable Long id) {
        return R.ok(documentService.getDocumentDetail(id));
    }

    /**
     * 获取课堂文档列表
     */
    @GetMapping("/classroom/{classroomId}")
    public R<Page<DocumentVO>> classroomDocs(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(documentService.getClassroomDocuments(classroomId, page, size));
    }

    /**
     * 获取我的文档列表
     */
    @GetMapping("/my")
    public R<Page<DocumentVO>> myDocs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(documentService.getMyDocuments(page, size));
    }

    /**
     * 获取Yjs文档初始状态
     */
    @GetMapping("/{id}/state")
    public R<byte[]> getState(@PathVariable Long id) {
        return R.ok(documentService.getYdocState(id));
    }
}
