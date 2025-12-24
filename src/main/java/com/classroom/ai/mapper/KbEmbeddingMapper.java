package com.classroom.ai.mapper;

import com.classroom.ai.entity.KbEmbedding;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识库向量Mapper（使用原生SQL操作pgvector）
 */
@Mapper
public interface KbEmbeddingMapper {

    /**
     * 插入向量
     */
    @Insert("INSERT INTO t_kb_embedding (kb_id, doc_id, chunk_index, content, embedding) " +
            "VALUES (#{kbId}, #{docId}, #{chunkIndex}, #{content}, #{embedding}::vector)")
    void insertWithVector(@Param("kbId") Long kbId,
            @Param("docId") Long docId,
            @Param("chunkIndex") Integer chunkIndex,
            @Param("content") String content,
            @Param("embedding") String embedding);

    /**
     * 向量相似度搜索
     */
    @Select("SELECT id, kb_id, doc_id, chunk_index, content, " +
            "1 - (embedding <=> #{queryVector}::vector) as similarity " +
            "FROM t_kb_embedding " +
            "WHERE kb_id = #{kbId} " +
            "ORDER BY embedding <=> #{queryVector}::vector " +
            "LIMIT #{limit}")
    List<VectorSearchResult> searchSimilar(@Param("kbId") Long kbId,
            @Param("queryVector") String queryVector,
            @Param("limit") int limit);

    /**
     * 删除文档的所有向量
     */
    @Delete("DELETE FROM t_kb_embedding WHERE doc_id = #{docId}")
    void deleteByDocId(@Param("docId") Long docId);

    /**
     * 删除知识库的所有向量
     */
    @Delete("DELETE FROM t_kb_embedding WHERE kb_id = #{kbId}")
    void deleteByKbId(@Param("kbId") Long kbId);

    /**
     * 向量搜索结果
     */
    @lombok.Data
    class VectorSearchResult {
        private Long id;
        private Long kbId;
        private Long docId;
        private Integer chunkIndex;
        private String content;
        private Double similarity;
    }
}
