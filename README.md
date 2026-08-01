# 乒乓球培训管理系统（后端）

> Spring Boot 3.x + MyBatis-Plus + MySQL 8.0 + Redis + JWT

## 项目简介

面向乒乓球培训机构的 SaaS 管理系统，支持多门店、多角色权限管理。涵盖学员管理、课包订单、消课退款、排课日程、业绩目标、数据看板、客户池等核心业务模块。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.5 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | - | 缓存（微信 access_token） |
| JWT | - | 身份认证 |
| Lombok | 1.18.46 | 简化实体类 |
| Hutool | - | 工具库 |

## 环境要求

- JDK 17+（推荐 JDK 25）
- Maven 3.6+
- MySQL 8.0+
- Redis（可选，用于微信 access_token 缓存）

## 快速开始

### 1. 克隆仓库

```bash
git clone git@github.com:xiaoshun12138/pingpong-saas-springboot.git
cd pingpong-saas-springboot
```

### 2. 创建数据库

```sql
CREATE DATABASE pingpong_saas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 3. 修改配置

编辑 `src/main/resources/application.yml`，修改数据库连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pingpong_saas?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: 你的密码
```

### 4. 编译运行

```bash
mvn compile
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`，前端构建产物已内嵌在 `static/` 目录下，直接访问即可。

### 5. 默认账号

| 角色 | 手机号 | 密码 |
|------|--------|------|
| 老板 (boss) | 13800000001 | 123456 |
| 店长 (shop_owner) | 13800000002 | 123456 |
| 教练 (coach) | 13800000003 | 123456 |
| 销售 (sales) | 13800000004 | 123456 |

## 数据库表结构

共 9 张表：

| 表名 | 说明 |
|------|------|
| store | 门店 |
| staff | 员工（含角色：boss/shop_owner/coach/sales） |
| student | 学员 |
| course_type | 课包类型 |
| course_order | 课包订单（新报/续费） |
| course_consumption | 消课记录 |
| refund_log | 退款记录 |
| monthly_target | 月度目标 |
| weekly_target | 周度目标 |
| lesson_schedule | 排课记录 |

## API 接口

共 40+ 个接口，主要模块：

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证 | `/api/auth` | 登录、JWT 签发 |
| 学员 | `/api/students` | 学员 CRUD、停课/复课、课包查询 |
| 订单 | `/api/course-orders` | 新报、续费、退款 |
| 消课 | `/api/course-consumptions` | 消课记录、自动扣课时 |
| 退款 | `/api/refund-logs` | 退款记录 |
| 排课 | `/api/schedules` | 排课 CRUD、排课自动消课 |
| 课包 | `/api/course-types` | 课包类型管理 |
| 门店 | `/api/stores` | 门店管理 |
| 员工 | `/api/staff` | 员工管理 |
| 看板 | `/api/dashboard` | 数据统计、排名、趋势 |
| 目标 | `/api/target-dashboard` | 业绩目标、课消目标 |
| 客户池 | `/api/customer-pool` | 学员缴费排名、消课排名、需约课提醒 |

## 权限模型

- **boss（老板）**：全量数据，可按门店筛选
- **shop_owner（店长）**：仅本门店数据
- **coach（教练）**：仅查看自己相关的数据
- **sales（销售）**：仅查看自己相关的数据

数据隔离通过 `AuthInterceptor` 在请求上下文中注入 `role` 和 `storeId`，Controller 层自动过滤。

## 核心业务流程

### 消课流程
1. 教练选择学员 + 课包订单 → 发起消课
2. 校验学员状态（停课不可消课）
3. 校验订单剩余课时（不足抛异常）
4. 扣减订单 `remaining_lessons`、累加 `consumed_lessons`
5. 扣减学员 `total_remaining_lessons`（乐观锁）
6. 更新学员 `last_lesson_at`
7. 插入 `course_consumption` 记录

### 退款流程
1. 选择学员 + 课包订单 → 发起退款
2. 按剩余课时比例计算退款金额：`paidAmount × (remainingLessons / totalLessons)`
3. 扣减学员 `total_remaining_lessons`
4. 订单状态改为 `refunded`
5. 插入 `refund_log` 记录
6. 消课记录保留

### 排课流程
1. 选择教练 + 时间段 → 新建排课
2. 关联学员和课包订单
3. 保存后自动消课 1 课时
4. 同段满 6 人阻止添加

## 项目结构

```
src/main/java/com/pingpong/
├── PingPongApplication.java       # 启动类
├── common/                         # 公共类（R 统一响应、异常处理）
├── config/                         # 配置类（WebConfig、拦截器注册）
├── controller/                     # 控制器
├── dto/                            # 数据传输对象
├── entity/                         # 实体类
├── interceptor/                    # JWT 拦截器
├── mapper/                         # MyBatis-Plus Mapper
├── service/                        # Service 接口 + 实现
└── vo/                             # 视图对象
```

## GitHub

- 后端仓库：https://github.com/xiaoshun12138/pingpong-saas-springboot
- 前端仓库：https://github.com/xiaoshun12138/pingpong-saas-vue
