#!/bin/bash

# 启动基础设施
echo "🚀 启动 PostgreSQL 和 Redis..."
docker-compose up -d

# 等待服务就绪
echo "⏳ 等待服务就绪..."
sleep 5

# 检查服务状态
echo "📊 检查服务状态..."
docker-compose ps

echo "✅ 基础设施启动完成!"
echo ""
echo "PostgreSQL: localhost:5432 (classroom/classroom123)"
echo "Redis: localhost:6379"
