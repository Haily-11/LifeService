
# LifeService

> 基于 SpringBoot + Redis 的生活服务点评平台 

LifeService 是一个模拟大众点评/美团类本地生活服务的综合实战项目，核心围绕 Redis 在**高并发场景下的多样化应用**展开。项目实现了完整的用户体系、商户浏览、优惠券秒杀、达人探店、社交关注、LBS 附近搜索、签到统计等核心业务模块。

---

## 📖 项目背景

本项目原为 ，是一个专为学习 Redis 实战而设计的教学项目。在保留原有功能的基础上进行了代码整理和优化，适合用于：

- 学习 Redis 在真实业务场景中的应用
- 理解高并发场景下的缓存设计与分布式锁
- 掌握 SpringBoot + MyBatis-Plus + Redis 的整合开发

---

## ✨ 核心功能

### 👤 用户模块
| 功能 | 技术要点 |
|------|---------|
| 短信验证码登录 | Redis 共享 Session 实现分布式会话管理 |
| 用户信息管理 | 个人信息查询与修改 |

### 🏪 商户模块
| 功能 | 技术要点 |
|------|---------|
| 商户类型分类展示 | 首页分类查询 |
| 商户详情查询 | 缓存穿透/击穿/雪崩解决方案 |
| 附近商户搜索 | Redis GEO 地理位置检索 + 距离排序 |

### 🎫 优惠券秒杀
| 功能 | 技术要点 |
|------|---------|
| 秒杀优惠券抢购 | 分布式锁（Redisson）防超卖 |
| 一人一单限制 | Redis 计数器 + Lua 脚本原子操作 |
| 异步下单 | 消息队列削峰填谷 |

### 📝 达人探店
| 功能 | 技术要点 |
|------|---------|
| 发布探店笔记 | 图文上传与存储 |
| 点赞/取消点赞 | Redis Set 保证唯一性 |
| 点赞排行榜 | Redis SortedSet 按时间排序 TOP N |

### 👥 社交关注
| 功能 | 技术要点 |
|------|---------|
| 关注/取关 | 数据库关系表管理 |
| 共同关注 | Redis Set 交集运算 |
| 关注动态推送 | Feed 流推模式 + 滚动分页 |

### 📊 数据统计
| 功能 | 技术要点 |
|------|---------|
| 用户签到 | Redis BitMap 位图存储 |
| UV 统计 | Redis HyperLogLog 基数统计 |

---

## 🛠️ 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.x | 后端框架 |
| MyBatis-Plus | 3.x | ORM 框架 |
| Redis | 6.x+ | 缓存、分布式锁、GEO、BitMap、HyperLogLog |
| MySQL | 8.0 | 数据持久化 |
| Maven | 3.6+ | 项目构建 |
| Lombok | - | 简化代码 |
| Hutool | - | 工具类库 |
| Redisson | - | 分布式锁实现 |

---

## 📁 项目结构

```
LifeService/
├── src/main/java/com/hmdp/
│   ├── controller/      # 控制层（RESTful API）
│   ├── service/         # 业务逻辑层
│   ├── mapper/          # 数据访问层（MyBatis-Plus）
│   ├── entity/          # 实体类
│   ├── dto/             # 数据传输对象
│   ├── config/          # 配置类（Redis、MVC、拦截器）
│   ├── utils/           # 工具类（Redis操作、JWT、验证码等）
│   └── interceptor/     # 拦截器（登录校验）
├── src/main/resources/
│   ├── application.yml  # 配置文件
│   └── db/              # 数据库 SQL 脚本
└── pom.xml              # Maven 依赖配置
```

---

## 🚀 快速开始

### 前置条件

- JDK 8 或更高版本
- Maven 3.6+
- MySQL 8.0
- Redis 6.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/你的用户名/LifeService.git
   cd LifeService
   ```

2. **导入数据库**
   - 创建数据库 `hmdp`
   - 执行 `src/main/resources/db/hmdp.sql` 脚本

3. **修改配置文件** `application.yml`
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/hmdp
       username: 你的数据库用户名
       password: 你的数据库密码
     redis:
       host: 127.0.0.1
       port: 6379
       password: 你的Redis密码（若无则留空）
   ```

4. **启动 Redis 服务**
   ```bash
   redis-server
   ```

5. **运行项目**
   - 在 IDEA 中运行 `HmdpApplication.java`
   - 后端服务启动在 `http://localhost:8081`

### 前端启动（可选）

项目前端使用 Nginx 部署静态资源：
1. 将前端文件放入 Nginx 的 `html` 目录
2. 配置 Nginx 反向代理到后端 `8081` 端口
3. 启动 Nginx，访问 `http://localhost`

---

## 🔑 核心知识点

### Redis 数据结构应用

| 数据结构 | 应用场景 |
|---------|---------|
| String | 短信验证码、Token、缓存对象 |
| List | Feed 流时间线推送 |
| Set | 点赞去重、共同关注 |
| ZSet | 点赞排行榜、分页查询 |
| GEO | 附近商户搜索 |
| BitMap | 用户签到统计 |
| HyperLogLog | UV 统计 |

### 高并发解决方案

- **超卖问题**：乐观锁（CAS）+ 分布式锁（Redisson）
- **缓存穿透**：布隆过滤器 + 空值缓存
- **缓存击穿**：互斥锁（Mutex Key）
- **缓存雪崩**：随机过期时间 + 多级缓存

---

## 📝 原项目信息

本项目基于 **黑马点评** 重构：
- 原课程视频：[Bilibili - Redis 实战篇](https://www.bilibili.com/video/BV1cr4y1671t)
- 原作者：黑马程序员（虎哥）

---

## 📄 License

本项目仅供学习交流使用。

