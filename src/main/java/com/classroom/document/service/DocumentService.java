package com.classroom.document.service;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.classroom.auth.entity.User;
import com.classroom.auth.mapper.UserMapper;
import com.classroom.classroom.entity.Classroom;
import com.classroom.classroom.mapper.ClassroomMapper;
import com.classroom.common.exception.BusinessException;
import com.classroom.document.dto.DocumentDTO;
import com.classroom.document.entity.Document;
import com.classroom.document.mapper.DocumentMapper;
import com.classroom.document.vo.DocumentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文档服务
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentMapper documentMapper;
    private final ClassroomMapper classroomMapper;
    private final UserMapper userMapper;

    /**
     * 创建文档
     */
    public Long createDocument(DocumentDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();

        Document doc = new Document();
        doc.setTitle(dto.getTitle());
        doc.setClassroomId(dto.getClassroomId());
        doc.setCreatorId(userId);
        doc.setDocType(dto.getDocType());
        doc.setContentUrl(dto.getContentUrl());
        doc.setVersion(1);

        documentMapper.insert(doc);
        return doc.getId();
    }

    /**
     * 更新文档标题
     */
    public void updateDocument(Long id, DocumentDTO dto) {
        Document doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }

        long userId = StpUtil.getLoginIdAsLong();
        if (!doc.getCreatorId().equals(userId)) {
            throw new BusinessException("无权修改此文档");
        }

        doc.setTitle(dto.getTitle());
        doc.setUpdateTime(LocalDateTime.now());
        documentMapper.updateById(doc);
    }

    /**
     * 删除文档
     */
    public void deleteDocument(Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }

        long userId = StpUtil.getLoginIdAsLong();
        if (!doc.getCreatorId().equals(userId)) {
            throw new BusinessException("无权删除此文档");
        }

        documentMapper.deleteById(id);
    }

    /**
     * 获取文档详情
     */
    public DocumentVO getDocumentDetail(Long id) {
        Document doc = documentMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException("文档不存在");
        }
        return toVO(doc);
    }

    /**
     * 获取课堂文档列表
     */
    public Page<DocumentVO> getClassroomDocuments(Long classroomId, int page, int size) {
        Page<Document> pageResult = documentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getClassroomId, classroomId)
                        .orderByDesc(Document::getUpdateTime));

        return convertPage(pageResult, page, size);
    }

    /**
     * 获取我的文档列表
     */
    public Page<DocumentVO> getMyDocuments(int page, int size) {
        long userId = StpUtil.getLoginIdAsLong();

        Page<Document> pageResult = documentMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Document>()
                        .eq(Document::getCreatorId, userId)
                        .orderByDesc(Document::getUpdateTime));

        return convertPage(pageResult, page, size);
    }

    /**
     * 保存Yjs文档状态
     */
    public void saveYdocState(Long docId, byte[] state) {
        Document doc = documentMapper.selectById(docId);
        if (doc == null) {
            return;
        }

        doc.setYdocState(state);
        doc.setVersion(doc.getVersion() + 1);
        doc.setUpdateTime(LocalDateTime.now());
        documentMapper.updateById(doc);
    }

    /**
     * 获取Yjs文档状态
     */
    public byte[] getYdocState(Long docId) {
        Document doc = documentMapper.selectById(docId);
        return doc != null ? doc.getYdocState() : null;
    }

    private Page<DocumentVO> convertPage(Page<Document> pageResult, int page, int size) {
        List<Long> creatorIds = pageResult.getRecords().stream()
                .map(Document::getCreatorId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, User> userMap = creatorIds.isEmpty() ? Map.of()
                : userMapper.selectBatchIds(creatorIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));

        List<Long> classroomIds = pageResult.getRecords().stream()
                .map(Document::getClassroomId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Classroom> classroomMap = classroomIds.isEmpty() ? Map.of()
                : classroomMapper.selectBatchIds(classroomIds).stream()
                        .collect(Collectors.toMap(Classroom::getId, c -> c));

        Page<DocumentVO> voPage = new Page<>(page, size, pageResult.getTotal());
        voPage.setRecords(pageResult.getRecords().stream()
                .map(d -> {
                    DocumentVO vo = toVO(d);
                    User user = userMap.get(d.getCreatorId());
                    if (user != null) {
                        vo.setCreatorName(user.getNickname());
                    }
                    if (d.getClassroomId() != null) {
                        Classroom classroom = classroomMap.get(d.getClassroomId());
                        if (classroom != null) {
                            vo.setClassroomName(classroom.getName());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList()));

        return voPage;
    }

    private DocumentVO toVO(Document doc) {
        DocumentVO vo = new DocumentVO();
        vo.setId(doc.getId());
        vo.setClassroomId(doc.getClassroomId());
        vo.setCreatorId(doc.getCreatorId());
        vo.setTitle(doc.getTitle());
        vo.setDocType(doc.getDocType());
        vo.setContentUrl(doc.getContentUrl());
        vo.setVersion(doc.getVersion());
        vo.setCreateTime(doc.getCreateTime());
        vo.setUpdateTime(doc.getUpdateTime());
        return vo;
    }
}
