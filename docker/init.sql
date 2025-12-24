-- 启用向量扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 用户表
CREATE TABLE t_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'student',
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 课堂表
CREATE TABLE t_classroom (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    teacher_id BIGINT NOT NULL,
    cover_url VARCHAR(255),
    status VARCHAR(20) DEFAULT 'active',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 课堂成员表
CREATE TABLE t_classroom_member (
    id BIGSERIAL PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'student',
    join_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(classroom_id, user_id)
);

-- 聊天消息表
CREATE TABLE t_chat_message (
    id BIGSERIAL PRIMARY KEY,
    classroom_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT,
    msg_type VARCHAR(20) DEFAULT 'text',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_chat_classroom_time ON t_chat_message(classroom_id, create_time);

-- 协同文档表
CREATE TABLE t_document (
    id BIGSERIAL PRIMARY KEY,
    classroom_id BIGINT,
    creator_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    doc_type VARCHAR(20) NOT NULL,
    content_url VARCHAR(500),
    ydoc_state BYTEA,
    version INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库表
CREATE TABLE t_knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    creator_id BIGINT NOT NULL,
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    doc_count INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库文档表
CREATE TABLE t_kb_document (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    file_url VARCHAR(500),
    file_type VARCHAR(50),
    file_size BIGINT,
    chunk_count INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'pending',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 知识库向量表
CREATE TABLE t_kb_embedding (
    id BIGSERIAL PRIMARY KEY,
    kb_id BIGINT NOT NULL,
    doc_id BIGINT NOT NULL,
    chunk_index INT NOT NULL,
    content TEXT NOT NULL,
    embedding vector(1536),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_kb_embedding ON t_kb_embedding USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- AI对话会话表
CREATE TABLE t_ai_conversation (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    kb_id BIGINT,
    title VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_conv_user_time ON t_ai_conversation(user_id, create_time);

-- AI对话消息表
CREATE TABLE t_ai_message (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content TEXT,
    model VARCHAR(50),
    tokens INT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初始管理员账号 (密码: admin123)
INSERT INTO t_user (username, password, nickname, role)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iZC4e/oKSXb5rR.uHtFj.bX3ej3C', '管理员', 'admin');
