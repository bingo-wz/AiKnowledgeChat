#!/bin/bash

# AI智慧教研室 - 生产环境部署脚本

set -e

echo "🚀 部署AI智慧教研室..."
echo ""

# 检查环境变量
if [ -z "$QWEN_API_KEY" ] && [ -z "$ZHIPU_API_KEY" ] && [ -z "$DEEPSEEK_API_KEY" ]; then
    echo "⚠️  警告: 未配置任何AI模型API Key"
    echo "   请设置环境变量: QWEN_API_KEY / ZHIPU_API_KEY / DEEPSEEK_API_KEY"
    echo ""
fi

# 构建镜像
echo "📦 构建Docker镜像..."
docker-compose -f docker-compose.prod.yml build

# 启动服务
echo "🚀 启动所有服务..."
docker-compose -f docker-compose.prod.yml up -d

# 等待应用启动
echo "⏳ 等待应用启动..."
sleep 30

# 检查状态
echo ""
echo "📊 服务状态:"
docker-compose -f docker-compose.prod.yml ps

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ 部署完成!"
echo ""
echo "应用地址:    http://localhost:8080"
echo "MinIO控制台: http://localhost:9001"
echo ""
echo "默认账号: admin / admin123"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
