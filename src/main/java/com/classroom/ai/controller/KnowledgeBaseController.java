package com.classroom.ai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.ai.entity.KbDocument;
import com.classroom.ai.entity.KnowledgeBase;
import com.classroom.ai.service.KnowledgeBaseService;
import com.classroom.common.result.R;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/kb")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseService kbService;

    /**
     * 创建知识库
     */
    @PostMapping
    public R<Long> create(@RequestBody KbCreateDTO dto) {
        return R.ok(kbService.createKnowledgeBase(dto.getName(), dto.getDescription(), dto.getIsPublic()));
    }

    /**
     * 更新知识库
     */
    @PutMapping("/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody KbCreateDTO dto) {
        kbService.updateKnowledgeBase(id, dto.getName(), dto.getDescription(), dto.getIsPublic());
        return R.ok();
    }

    /**
     * 删除知识库
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        kbService.deleteKnowledgeBase(id);
        return R.ok();
    }

    /**
     * 知识库列表
     */
    @GetMapping
    public R<Page<KnowledgeBase>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean onlyMine) {
        return R.ok(kbService.getKnowledgeBases(page, size, onlyMine));
    }

    /**
     * 上传文档
     */
    @PostMapping("/{id}/document")
    public R<Long> uploadDocument(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        return R.ok(kbService.uploadDocument(id, file));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/document/{docId}")
    public R<Void> deleteDocument(@PathVariable Long docId) {
        kbService.deleteDocument(docId);
        return R.ok();
    }

    /**
     * 知识库文档列表
     */
    @GetMapping("/{id}/documents")
    public R<List<KbDocument>> documents(@PathVariable Long id) {
        return R.ok(kbService.getDocuments(id));
    }

    @Data
    public static class KbCreateDTO {
        private String name;
        private String description;
        private Boolean isPublic;
    }
}
