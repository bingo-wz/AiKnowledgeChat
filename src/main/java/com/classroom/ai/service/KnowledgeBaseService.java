package com.classroom.ai.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.ai.entity.KbDocument;
import com.classroom.ai.entity.KnowledgeBase;
import com.classroom.ai.mapper.KbDocumentMapper;
import com.classroom.ai.mapper.KbEmbeddingMapper;
import com.classroom.ai.mapper.KnowledgeBaseMapper;
import com.classroom.common.exception.BusinessException;
import com.classroom.common.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeBaseService {

    private final KnowledgeBaseMapper kbMapper;
    private final KbDocumentMapper docMapper;
    private final KbEmbeddingMapper embeddingMapper;
    private final MinioService minioService;
    private final EmbeddingService embeddingService;

    private static final int CHUNK_SIZE = 500; // 每个切片的字符数
    private static final int CHUNK_OVERLAP = 50; // 切片重叠字符数

    /**
     * 创建知识库
     */
    public Long createKnowledgeBase(String name, String description, Boolean isPublic) {
        long userId = StpUtil.getLoginIdAsLong();

        KnowledgeBase kb = new KnowledgeBase();
        kb.setName(name);
        kb.setDescription(description);
        kb.setCreatorId(userId);
        kb.setIsPublic(isPublic != null ? isPublic : false);
        kb.setDocCount(0);

        kbMapper.insert(kb);
        return kb.getId();
    }

    /**
     * 更新知识库
     */
    public void updateKnowledgeBase(Long id, String name, String description, Boolean isPublic) {
        KnowledgeBase kb = kbMapper.selectById(id);
        if (kb == null) {
            throw new BusinessException("知识库不存在");
        }

        checkOwnership(kb);

        kb.setName(name);
        kb.setDescription(description);
        kb.setIsPublic(isPublic);
        kb.setUpdateTime(LocalDateTime.now());

        kbMapper.updateById(kb);
    }

    /**
     * 删除知识库
     */
    @Transactional
    public void deleteKnowledgeBase(Long id) {
        KnowledgeBase kb = kbMapper.selectById(id);
        if (kb == null) {
            throw new BusinessException("知识库不存在");
        }

        checkOwnership(kb);

        // 删除所有向量
        embeddingMapper.deleteByKbId(id);

        // 删除所有文档
        docMapper.delete(new LambdaQueryWrapper<KbDocument>().eq(KbDocument::getKbId, id));

        // 删除知识库
        kbMapper.deleteById(id);
    }

    /**
     * 获取知识库列表
     */
    public Page<KnowledgeBase> getKnowledgeBases(int page, int size, boolean onlyMine) {
        long userId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper<>();
        if (onlyMine) {
            wrapper.eq(KnowledgeBase::getCreatorId, userId);
        } else {
            // 公开的或自己的
            wrapper.and(w -> w.eq(KnowledgeBase::getIsPublic, true)
                    .or().eq(KnowledgeBase::getCreatorId, userId));
        }
        wrapper.orderByDesc(KnowledgeBase::getUpdateTime);

        return kbMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 上传文档到知识库
     */
    @Transactional
    public Long uploadDocument(Long kbId, MultipartFile file) {
        KnowledgeBase kb = kbMapper.selectById(kbId);
        if (kb == null) {
            throw new BusinessException("知识库不存在");
        }

        // 上传文件到MinIO
        String objectName = minioService.uploadFile(file, "kb/" + kbId);

        // 创建文档记录
        KbDocument doc = new KbDocument();
        doc.setKbId(kbId);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileUrl(objectName);
        doc.setFileType(file.getContentType());
        doc.setFileSize(file.getSize());
        doc.setStatus("processing");

        docMapper.insert(doc);

        // 异步处理文档
        processDocumentAsync(doc.getId(), kbId, objectName);

        return doc.getId();
    }

    /**
     * 异步处理文档（解析+向量化）
     */
    @Async
    public void processDocumentAsync(Long docId, Long kbId, String objectName) {
        try {
            // 下载文件
            InputStream is = minioService.downloadFile(objectName);

            // 使用Tika解析文档
            Tika tika = new Tika();
            String content = tika.parseToString(is);
            is.close();

            // 切片
            List<String> chunks = splitText(content, CHUNK_SIZE, CHUNK_OVERLAP);

            // 向量化并存储
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                float[] vector = embeddingService.embed(null, chunk);
                String vectorStr = embeddingService.vectorToString(vector);

                embeddingMapper.insertWithVector(kbId, docId, i, chunk, vectorStr);
            }

            // 更新文档状态
            KbDocument doc = docMapper.selectById(docId);
            doc.setChunkCount(chunks.size());
            doc.setStatus("ready");
            docMapper.updateById(doc);

            // 更新知识库文档数
            KnowledgeBase kb = kbMapper.selectById(kbId);
            kb.setDocCount(kb.getDocCount() + 1);
            kb.setUpdateTime(LocalDateTime.now());
            kbMapper.updateById(kb);

            log.info("文档处理完成: docId={}, chunks={}", docId, chunks.size());

        } catch (Exception e) {
            log.error("文档处理失败: docId={}", docId, e);
            KbDocument doc = docMapper.selectById(docId);
            if (doc != null) {
                doc.setStatus("failed");
                docMapper.updateById(doc);
            }
        }
    }

    /**
     * 删除文档
     */
    @Transactional
    public void deleteDocument(Long docId) {
        KbDocument doc = docMapper.selectById(docId);
        if (doc == null) {
            return;
        }

        // 删除向量
        embeddingMapper.deleteByDocId(docId);

        // 删除文件
        minioService.deleteFile(doc.getFileUrl());

        // 删除记录
        docMapper.deleteById(docId);

        // 更新知识库文档数
        KnowledgeBase kb = kbMapper.selectById(doc.getKbId());
        if (kb != null && kb.getDocCount() > 0) {
            kb.setDocCount(kb.getDocCount() - 1);
            kbMapper.updateById(kb);
        }
    }

    /**
     * 获取知识库文档列表
     */
    public List<KbDocument> getDocuments(Long kbId) {
        return docMapper.selectList(
                new LambdaQueryWrapper<KbDocument>()
                        .eq(KbDocument::getKbId, kbId)
                        .orderByDesc(KbDocument::getCreateTime));
    }

    /**
     * 文本切片
     */
    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return chunks;
        }

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap;
            if (start < 0)
                start = 0;
            if (end >= text.length())
                break;
        }

        return chunks;
    }

    private void checkOwnership(KnowledgeBase kb) {
        long userId = StpUtil.getLoginIdAsLong();
        if (!kb.getCreatorId().equals(userId)) {
            throw new BusinessException("无权操作此知识库");
        }
    }
}
