# 构建阶段
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制Maven配置
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# 下载依赖（利用缓存）
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# 复制源码
COPY src src

# 构建
RUN ./mvnw package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 创建非root用户
RUN addgroup -g 1000 app && adduser -u 1000 -G app -D app

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 设置权限
RUN chown -R app:app /app
USER app

# JVM优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
