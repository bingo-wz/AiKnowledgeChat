# AI智慧教研室

在线课堂 + AI教研助手 + 知识库管理系统

## 功能特性

- 🎓 **在线课堂**: 实时聊天 + 成员管理
- 📝 **协同文档**: Yjs实时多人编辑
- 🤖 **AI助手**: 多模型RAG对话 (通义千问/智谱/DeepSeek)
- 📚 **知识库**: 文档上传 + 向量检索

## 快速开始

### 开发环境

```bash
# 1. 启动基础设施
chmod +x start-infra.sh && ./start-infra.sh

# 2. 配置AI模型 (选择一个)
export QWEN_API_KEY=your-key

# 3. 启动应用
./mvnw spring-boot:run
```

### 生产部署

```bash
# 配置API Key
export QWEN_API_KEY=your-key

# 一键部署
chmod +x deploy.sh && ./deploy.sh
```

## 服务地址

| 服务 | 地址 |
|------|------|
| 后端API | http://localhost:8080 |
| MinIO控制台 | http://localhost:9001 |

**默认账号**: admin / admin123

## API文档

| 模块 | 接口 |
|------|------|
| 认证 | `POST /auth/login` `POST /auth/register` |
| 课堂 | `POST /classroom` `GET /classroom/my` |
| 文档 | `POST /document` `WS /ws/doc/{id}` |
| 知识库 | `POST /kb` `POST /kb/{id}/document` |
| AI对话 | `POST /ai/chat/stream` `GET /ai/models` |

## 技术栈

- Spring Boot 3.3.6 + Spring AI
- PostgreSQL 16 + pgvector
- Redis 7 + MinIO
- Vue 3 + TypeScript (前端)

## 资源占用

| 服务 | 内存 |
|------|------|
| PostgreSQL | 1G |
| Redis | 256M |
| MinIO | 256M |
| 应用 | 1-1.5G |
| **合计** | ~3G |
