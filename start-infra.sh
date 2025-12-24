#!/bin/bash

# AI智慧教研室 - 一键启动脚本

set -e

echo "🚀 AI智慧教研室启动中..."
echo ""

# 检查Docker
if ! command -v docker &> /dev/null; then
    echo "❌ 请先安装 Docker"
    exit 1
fi

# 启动基础设施
echo "📦 启动基础设施 (PostgreSQL + Redis + MinIO)..."
docker-compose up -d postgres redis minio

# 等待服务就绪
echo "⏳ 等待服务就绪..."
sleep 8

# 检查服务状态
echo "📊 服务状态:"
docker-compose ps

echo ""
echo "✅ 基础设施启动完成!"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "PostgreSQL: localhost:5432"
echo "Redis:      localhost:6379"
echo "MinIO:      localhost:9000 (控制台: 9001)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "💡 启动应用:"
echo "   ./mvnw spring-boot:run"
echo ""
echo "🔑 配置AI模型 (选择一个):"
echo "   export QWEN_API_KEY=your-key"
echo "   export ZHIPU_API_KEY=your-key"
echo "   export DEEPSEEK_API_KEY=your-key"
