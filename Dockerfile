# 简化版 - 只保留必要的
FROM --platform=linux/amd64 eclipse-temurin:11-jre

# 设置工作目录
WORKDIR /app

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建日志目录
RUN mkdir -p /app/logs

# 复制 jar 文件
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用 - 简化 JVM 参数
CMD ["java", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]