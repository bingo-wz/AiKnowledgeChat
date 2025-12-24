# AI智慧教研室

在线课堂 + AI教研助手 + 知识库管理系统

## 快速开始

### 1. 启动基础设施

```bash
chmod +x start-infra.sh
./start-infra.sh
```

### 2. 配置环境变量

创建 `.env` 文件或设置环境变量：

```bash
# AI配置（通义千问）
export AI_API_KEY=your-api-key
export AI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1
export AI_MODEL=qwen-turbo

# OSS配置
export OSS_ENDPOINT=oss-cn-hangzhou.aliyuncs.com
export OSS_ACCESS_KEY_ID=your-access-key
export OSS_ACCESS_KEY_SECRET=your-access-secret
export OSS_BUCKET_NAME=classroom-files
```

### 3. 启动应用

```bash
./mvnw spring-boot:run
```

访问: http://localhost:8080

## 默认账号

- 管理员: admin / admin123

## 技术栈

- **后端**: Spring Boot 3.3.6 + Spring AI + MyBatis-Plus + Sa-Token
- **前端**: Vue 3 + TypeScript + Element Plus
- **数据库**: PostgreSQL 16 + pgvector
- **缓存**: Redis 7
- **存储**: 阿里云OSS

## 项目结构

```
src/main/java/com/classroom/
├── ClassroomApplication.java   # 主入口
├── config/                     # 配置类
├── common/                     # 公共模块
├── auth/                       # 认证模块
├── classroom/                  # 课堂模块
├── document/                   # 文档模块
├── ai/                         # AI对话模块
└── knowledge/                  # 知识库模块
```
