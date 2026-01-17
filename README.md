# 🎓 学生管理系统后端

一个基于Spring Boot的现代化学生管理系统后端，集成了JPA、MyBatis、Redis、Kafka和JWT等主流技术栈。

## ✨ 核心特性

### 🔒 安全认证
- **JWT无状态认证**：基于Token的身份验证机制
- **RBAC权限控制**：ADMIN、TEACHER、STUDENT三级角色体系
- **密码加密**：BCrypt强哈希加密存储
- **Token管理**：Redis存储，支持自动过期和黑名单

### 📊 性能优化
- **多级缓存策略**：Redis + Spring Cache注解缓存
- **接口监控**：AOP切面自动统计接口性能
- **滑动窗口限流**：基于Redis ZSet实现的IP级限流
- **热点数据缓存**：学生详情、在线用户等高频数据缓存

### 🔄 数据持久化
- **双ORM支持**：JPA简化CRUD + MyBatis处理复杂查询
- **动态SQL**：XML配置支持灵活的条件查询
- **API调用日志**：完整记录所有接口调用详情

### 📡 消息队列
- **事件驱动架构**：关键操作异步发送Kafka消息
- **事件类型**：学生创建/删除、用户创建/删除等
- **可扩展性**：易于集成其他微服务

## 🏗️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.x | 应用框架 |
| Spring Security | 2.x | 安全框架 |
| Spring Data JPA | 2.x | 数据访问 |
| MyBatis | 3.x | SQL映射 |
| Redis | 6.x | 缓存/会话/统计 |
| Kafka | 2.x | 消息队列 |
| MySQL | 8.x | 数据库 |
| JWT | 0.11.x | 令牌认证 |

## 📁 项目结构

| 路径 | 文件 | 描述 |
|------|------|------|
| **根目录** | `pom.xml` | Maven配置 |
|  | `Dockerfile` | Docker构建文件 |
| **src/main/java/** | `StudentManageBackendApplication.java` | 启动类 |
| **config/** | `SecurityConfig.java` | Spring Security配置 |
|  | `RedisConfig.java` | Redis配置 |
|  | `KafkaConfig.java` | Kafka配置 |
|  | `MyBatisConfig.java` | MyBatis配置 |
|  | `GlobalExceptionHandler.java` | 全局异常处理 |
| **controller/** | `AuthController.java` | 认证控制器 |
|  | `StudentController.java` | 学生控制器 |
|  | `UserController.java` | 用户控制器 |
|  | `MonitorController.java` | 监控控制器 |
| **service/** | `AuthService.java` | 认证服务 |
|  | `StudentService.java` | 学生服务 |
|  | `UserService.java` | 用户服务 |
|  | `RedisService.java` | Redis服务 |
|  | `KafkaService.java` | Kafka服务 |
| **repository/** | `UserRepository.java` | 用户仓储 |
|  | `StudentRepository.java` | 学生仓储 |
|  | `ApiLogRepository.java` | API日志仓储 |
| **mapper/** | `StudentMapper.java` | 学生Mapper接口 |
|  | `StudentMapper.xml` | 学生Mapper XML |
| **entity/** | `User.java` | 用户实体 |
|  | `Student.java` | 学生实体 |
|  | `ApiLog.java` | API日志实体 |
| **dto/** | `LoginDTO.java` | 登录DTO |
|  | `UserDTO.java` | 用户DTO |
|  | `StudentDTO.java` | 学生DTO |
|  | `ApiLogDTO.java` | API日志DTO |
| **security/** | `JwtUtil.java` | JWT工具类 |
|  | `JwtFilter.java` | JWT过滤器 |
|  | `UserDetailServiceImpl.java` | 用户详情服务 |
| **aop/** | `ApiLogAspect.java` | API日志切面 |
|  | `RateLimitAspect.java` | 限流切面 |
| **kafka/** | `MessageProducer.java` | 消息生产者 |
|  | `StudentEvent.java` | 学生事件 |
| **vo/** | `StudentVO.java` | 学生视图对象 |
| **src/main/resources/** | `application.yml` | 应用配置 |
|  | `mybatis/StudentMapper.xml` | MyBatis映射文件 |

---

## 📚 API 接口文档

### 🔐 认证模块 `/api/auth`

| 方法 | 路径 | 描述 | 权限 | 限流 |
|------|------|------|------|------|
| POST | `/register` | 用户注册 | 所有人 | 10次/分钟/IP |
| POST | `/login` | 用户登录 | 所有人 | 10次/分钟/IP |
| POST | `/logout` | 用户登出 | 已认证用户 | 100次/分钟/IP |
| GET | `/profile` | 获取当前用户信息 | 已认证用户 | 100次/分钟/IP |

### 👥 用户管理模块 `/api/users` (需ADMIN权限)

| 方法 | 路径 | 描述 | 请求参数 | 限流 |
|------|------|------|----------|------|
| GET | `/` | 获取用户列表(分页) | `page=1&size=10` | 100次/分钟 |
| GET | `/{id}` | 获取用户详情 | - | 100次/分钟 |
| POST | `/` | 创建用户 | UserDTO JSON | 50次/分钟 |
| PUT | `/{id}` | 更新用户 | UserDTO JSON | 100次/分钟 |
| DELETE | `/{id}` | 删除用户 | - | 50次/分钟 |

### 🎓 学生管理模块 `/api/students`

| 方法 | 路径 | 描述 | 权限 | 请求参数 | 限流 |
|------|------|------|------|----------|------|
| GET | `/` | 获取学生列表(分页) | ALL | `keyword=&page=1&size=10` | 200次/分钟 |
| GET | `/{id}` | 获取学生详情 | ALL | - | 300次/分钟 |
| POST | `/` | 创建学生 | ADMIN | StudentDTO JSON | 100次/分钟 |
| PUT | `/{id}` | 更新学生 | ADMIN | StudentDTO JSON | 100次/分钟 |
| DELETE | `/{id}` | 删除学生 | ADMIN | - | 50次/分钟 |
| GET | `/search` | 高级搜索 | ADMIN, TEACHER | `name=张&classId=1&gender=男` | 200次/分钟 |

### 📊 系统监控模块 `/api/monitor` (需ADMIN权限)

| 方法 | 路径 | 描述 | 限流 |
|------|------|------|------|
| GET | `/api-stats` | 获取API接口统计 | 50次/分钟 |
| GET | `/redis-info` | 获取Redis信息 | 50次/分钟 |

### 🎯 AOP切面端点

所有被以下注解标记的接口都会被自动监控和统计：

| 注解 | 描述 |
|------|------|
| `@GetMapping` | HTTP GET请求 |
| `@PostMapping` | HTTP POST请求 |
| `@PutMapping` | HTTP PUT请求 |
| `@DeleteMapping` | HTTP DELETE请求 |
| `@RateLimit` | 自定义限流配置 |

---

## 🗄️ 数据库设计

### `users` 表（用户）

| 字段 | 类型 | 描述 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY |
| username | VARCHAR(50) | 用户名 | UNIQUE, NOT NULL |
| password | VARCHAR(255) | 密码(BCrypt加密) | NOT NULL |
| role | VARCHAR(20) | 角色: ADMIN/TEACHER/STUDENT | NOT NULL |
| email | VARCHAR(100) | 邮箱 | - |
| phone | VARCHAR(20) | 手机号 | - |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

### `students` 表（学生）

| 字段 | 类型 | 描述 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY |
| student_no | VARCHAR(50) | 学号 | UNIQUE, NOT NULL |
| name | VARCHAR(100) | 姓名 | NOT NULL |
| age | INT | 年龄 | - |
| gender | VARCHAR(10) | 性别 | - |
| email | VARCHAR(100) | 邮箱 | - |
| phone | VARCHAR(20) | 手机号 | - |
| address | VARCHAR(255) | 地址 | - |
| class_id | BIGINT | 班级ID | INDEX |
| created_at | DATETIME | 创建时间 | DEFAULT CURRENT_TIMESTAMP |

### `api_logs` 表（API日志）

| 字段 | 类型 | 描述 | 约束 |
|------|------|------|------|
| id | BIGINT | 主键 | PRIMARY KEY |
| api_name | VARCHAR(200) | API名称 | INDEX |
| method_name | VARCHAR(100) | 方法名 | - |
| user_id | BIGINT | 用户ID | INDEX |
| username | VARCHAR(50) | 用户名 | INDEX |
| ip | VARCHAR(50) | IP地址 | - |
| request_time | DATETIME | 请求时间 | INDEX |
| response_time | BIGINT | 响应时间(ms) | - |
| success | BOOLEAN | 是否成功 | - |
| error_msg | TEXT | 错误信息 | - |

---

## ⚙️ 配置说明

### 限流配置

| 接口类型 | 默认限流 | 自定义示例 |
|----------|----------|------------|
| 通用接口 | 100次/分钟/IP | `@RateLimit(value = 50, time = 60)` |
| 登录接口 | 10次/分钟/IP | `@RateLimit(value = 5, time = 60)` |
| 管理接口 | 50次/分钟 | `@RateLimit(value = 30, time = 60)` |

### 缓存配置

| 缓存类型 | Key格式 | 过期时间 | 描述 |
|----------|---------|----------|------|
| 学生详情 | `student:{id}` | 1小时 | 学生对象缓存 |
| 在线用户 | `online:users` | 2小时 | 在线用户集合 |
| API统计 | `api:stats:{date}:{api}` | 30天 | 接口调用统计 |
| Token | `token:{username}` | 2小时 | 用户登录Token |

### 监控指标

| 指标 | 存储位置 | 说明 |
|------|----------|------|
| 调用次数 | Redis Hash | 按天统计各接口调用次数 |
| 响应时间 | Redis Hash | 累计响应时间，计算平均值 |
| 成功率 | Redis Hash | 成功/失败次数统计 |
| 接口热度 | Redis ZSet | 按调用频率排名 |

---

## 🚀 快速开始

### 1. 环境要求

| 软件 | 版本 | 备注 |
|------|------|------|
| JDK | 1.8+ | 推荐JDK 11 |
| Maven | 3.6+ | 构建工具 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存服务 |
| Kafka | 2.8+ | 消息队列(可选) |

1.2 Kafka安装配置

 1.2.1 下载Kafka
 
 访问 https://kafka.apache.org/downloads 下载最新版。
 
 推荐版本：
 
 kafka_2.12-3.6.0.tgz（Scala 2.12）
 
 kafka_2.13-3.6.0.tgz（Scala 2.13）

 1.2.2 解压安装
 
 1.2.3 启动
 
 Windows：
 
  打开两个命令提示符窗口
  
  窗口1 - 启动Zookeeper：
  
 cd C:\你的路径\kafka_2.12-3.6.0
 
 bin\windows\zookeeper-server-start.bat config\zookeeper.properties
 
  窗口2 - 启动Kafka：
  
 cd C:\你的路径\kafka_2.12-3.6.0
 
 bin\windows\kafka-server-start.bat config\server.properties

1.3 Redis下载

下载Redis for Windows：

访问：https://github.com/microsoftarchive/redis/releases 下载Redis-x64-3.2.100.msi或最新版本，安装后，Redis会自动作为服务启动

### 2. 部署步骤

| 步骤 | 命令 | 说明 |
|------|------|------|
| 克隆项目 | `git clone <repo-url>` | 克隆代码库 |
| 数据库创建 | `CREATE DATABASE student_manage;` | 创建数据库 |
| 配置文件 | 修改`application.yml` | 配置数据库连接 |
| 构建项目 | `mvn clean package -DskipTests` | 打包项目 |
| 运行应用 | `java -jar target/*.jar` | 启动应用 |

### 3. Docker部署

```bash
# 构建镜像
docker build -t student-manage-backend:1.0.0 .

# 运行容器
docker run -d \
  --name student-manage \
  -p 8080:8080 \
  -e MYSQL_HOST=host.docker.internal \
  -e MYSQL_PORT=3306 \
  -e MYSQL_DATABASE=student_manage \
  -e MYSQL_USERNAME=root \
  -e MYSQL_PASSWORD=root \
  -e REDIS_HOST=redis \
  -e REDIS_PORT=6379 \
  student-manage-backend:1.0.0
```

---

## 🧪 使用示例

### 1. 用户登录

| 项目 | 内容 |
|------|------|
| 请求方法 | POST |
| 请求地址 | `http://localhost:8080/api/auth/login` |
| 请求头 | `Content-Type: application/json` |
| 请求体 | `{"username":"admin","password":"admin123"}` |

响应示例：
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "role": "ADMIN",
  "userId": 1
}
```

### 2. 创建学生

| 项目 | 内容 |
|------|------|
| 请求方法 | POST |
| 请求地址 | `http://localhost:8080/api/students` |
| 请求头 | `Authorization: Bearer {token}` |
| 请求体 | StudentDTO JSON |

---

## 🔐 JWT 认证说明

| 声明字段 | 描述 | 示例值 |
|----------|------|--------|
| sub | 用户名 | "admin" |
| userId | 用户ID | 1 |
| role | 用户角色 | "ADMIN" |
| exp | 过期时间 | 1747440000 |
| iat | 签发时间 | 1747353600 |

请求头格式：
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---
