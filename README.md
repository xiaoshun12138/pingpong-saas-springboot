# 🏓 PingPong SaaS — 乒乓球培训管理系统（后端）

> 面向乒乓球培训连锁机构的多门店 SaaS 管理系统后端，支持老板/店长/教练/销售四种角色权限隔离，涵盖学员管理、课包订单、消课退款、排课日程、业绩目标、数据看板、客户池等核心业务。

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.3.5-green" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.7-blue" alt="MyBatis-Plus">
  <img src="https://img.shields.io/badge/MySQL-8.0-orange" alt="MySQL">
  <img src="https://img.shields.io/badge/JDK-21+-red" alt="JDK">
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License">
</p>

---

## ✨ 功能特性

- **多门店多角色**：boss 全量数据 / shop_owner 本门店隔离 / coach & sales 仅自己相关
- **学员全生命周期**：报名 → 排课 → 消课 → 续费 / 退款 → 停课 / 复课
- **消课事务**：乐观锁保护课时扣减，保证并发安全
- **退款全退**：后端自动计算退款金额，不信任前端传参
- **排课自动消课**：排课保存即自动消 1 课时，取消排课自动归还
- **数据看板**：GROUP BY 聚合 SQL，7 大核心指标 + 排名 + 趋势 + 门店明细
- **目标管理**：月度/周度目标，业绩目标 + 课消目标双维度
- **客户池**：学员缴费/消课排名、建议约课、建议续费提醒
- **JWT 认证**：无状态 Token + 拦截器角色注入
- **逻辑删除**：全表 `@TableLogic`，数据可追溯

## 🛠 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.5 | 基础框架 |
| MyBatis-Plus | 3.5.7 | ORM + 分页 + 乐观锁 + 逻辑删除 |
| MySQL | 8.0+ | 关系型数据库 |
| JWT (jjwt) | 0.12.5 | 身份认证 |
| Lombok | 1.18.46 | 简化实体类 |
| Hutool | 5.8.29 | 工具库 |
| Maven | 3.6+ | 构建工具 |

## 📦 环境要求

- **JDK 21+**（推荐 JDK 25）
- **Maven 3.6+**
- **MySQL 8.0+**

## 🚀 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/xiaoshun12138/pingpong-saas-springboot.git
cd pingpong-saas-springboot
```

### 2. 创建数据库

```sql
CREATE DATABASE pingpong_saas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

> 建表 SQL 和模拟数据脚本见 `sql/` 目录（或联系作者获取）。

### 3. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/pingpong_saas?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: ${DB_PASSWORD:你的密码}

jwt:
  secret: ${JWT_SECRET:你的密钥}
```

> ⚠️ **生产环境**：务必通过环境变量注入 `DB_PASSWORD` 和 `JWT_SECRET`，不要使用默认值。

### 4. 编译运行

```bash
mvn compile
mvn spring-boot:run
```

启动后访问 `http://localhost:8080`，前端构建产物已内嵌在 `static/` 目录。

### 5. 默认账号

| 角色 | 手机号 | 密码 | 权限 |
|------|--------|------|------|
| 老板 (boss) | 13800000001 | 123456 | 全量数据，可按门店筛选 |
| 店长 (shop_owner) | 13800000002 | 123456 | 仅本门店数据 |
| 教练 (coach) | 13800000003 | 123456 | 仅自己相关数据 |
| 销售 (sales) | 13800000004 | 123456 | 仅自己相关数据 |

## 📊 数据库设计

共 10 张表，全部支持逻辑删除（`deleted` 字段）：

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `store` | 门店 | id, name |
| `staff` | 员工 | id, name, phone, role, store_id, @Version |
| `student` | 学员 | id, name, phone, store_id, primary_coach_id, total_remaining_lessons, status, @Version |
| `course_type` | 课包类型 | id, name, total_lessons, price, status |
| `course_order` | 课包订单 | id, order_no, student_id, coach_id, sales_id, store_id, course_type_id, total_lessons, remaining_lessons, consumed_lessons, paid_amount, status, source, @Version |
| `course_consumption` | 消课记录 | id, student_id, coach_id, course_order_id, store_id, lessons, record_date, record_time |
| `refund_log` | 退款记录 | id, course_order_id, student_id, store_id, refund_amount, refund_lessons, reason, operator_id |
| `lesson_schedule` | 排课记录 | id, coach_id, student_id, course_order_id, store_id, schedule_date, start_time, end_time |
| `monthly_target` | 月度目标 | id, store_id, target_month, target_type, target_amount |
| `weekly_target` | 周度目标 | id, store_id, target_week, target_type, target_amount |

> `target_type` 取值：`sales`（销售额）/ `consumption`（消课额）

## 📡 API 接口

共 50+ 个接口，按模块组织：

| 模块 | 前缀 | 主要功能 |
|------|------|----------|
| 认证 | `POST /api/auth/login` | 手机号 + 密码登录，返回 JWT Token |
| 学员 | `/api/students` | CRUD、停课/复课、课包查询、排序（剩余课时/注册日期/最近上课/姓名） |
| 订单 | `/api/course-orders` | 新报（含自动创建学员）、续费、退款、关键词搜索 |
| 消课 | `/api/course-consumptions` | 消课记录查询、日期范围筛选 |
| 退款 | `/api/refund-logs` | 退款记录查询、退款操作（全退、金额自动计算） |
| 排课 | `/api/schedules` | 排课 CRUD、唯一约束（教练+学员+订单+日期+时段）、取消归还课时 |
| 课包 | `/api/course-types` | 课包类型 CRUD |
| 门店 | `/api/stores` | 门店 CRUD（仅 boss） |
| 员工 | `/api/staff` | 员工 CRUD、角色筛选 |
| 看板 | `/api/dashboard` | 7 大核心指标、每日趋势、门店业绩/课消明细、教练排名 |
| 排名 | `/api/ranking` | 教练课消排名、教练业绩排名、销售业绩排名、门店排名 |
| 目标 | `/api/target-dashboard` | 业绩目标看板、课消目标看板 |
| 目标设定 | `/api/monthly-targets` `/api/weekly-targets` | 月度/周度目标 CRUD（仅 boss） |
| 客户池 | `/api/customer-pool` | 学员汇总列表、建议约课、建议续费 |

> 统一响应格式：`{ code: 200, message: "success", data: ... }`，错误码：200 成功 / 400 参数错误 / 401 未认证 / 403 无权限 / 500 系统错误。

## 🔐 权限模型

```
请求 → AuthInterceptor（JWT 校验）→ 注入 role + storeId 到 request attribute
     → Controller 按 role 过滤数据
```

| 角色 | 数据范围 | 特殊权限 |
|------|----------|----------|
| boss | 全部门店 | 目标设定、门店管理、删除操作 |
| shop_owner | 本门店 | — |
| coach | 自己相关 | — |
| sales | 自己相关 | — |

## 🔄 核心业务流程

### 消课流程（@Transactional + 乐观锁）
```
校验学员状态（停课不可消课）
→ 校验订单 active + 剩余课时 > 0
→ 校验学员与订单同一门店
→ 校验教练属于订单门店
→ 插入消课记录
→ 扣减订单 remaining_lessons（乐观锁）
→ 扣减学员 total_remaining_lessons（乐观锁）
→ 更新学员 last_lesson_at
```

### 退款流程（@Transactional，全退不可改）
```
校验退款课时 == 订单剩余课时（必须全退）
→ 计算退款金额 = paid_amount × (remaining_lessons / total_lessons)
→ 扣减学员 total_remaining_lessons（乐观锁）
→ 订单状态改为 refunded
→ 插入退款记录（操作人从 JWT 获取）
→ 消课记录保留（审计追溯）
```

### 排课流程（@Transactional）
```
校验时间段唯一约束（教练+学员+订单+日期+时段）
→ 保存排课记录
→ 自动消课 1 课时
→ 失败全回滚
```

## 📁 项目结构

```
src/main/java/com/pingpong/
├── PingPongApplication.java          # 启动类
├── common/
│   ├── R.java                        # 统一响应封装
│   ├── GlobalExceptionHandler.java   # 全局异常处理
│   └── JwtUtil.java                  # JWT 工具
├── config/
│   ├── CorsConfig.java               # 跨域配置
│   ├── MyBatisPlusConfig.java        # 分页 + 乐观锁插件
│   └── WebConfig.java                # 拦截器注册 + 静态资源
├── interceptor/
│   └── AuthInterceptor.java          # JWT 认证拦截器
├── runner/
│   └── PasswordFixRunner.java        # 启动时明文密码 → BCrypt
├── entity/                           # 10 个实体类
├── dto/                              # 7 个 DTO/VO
├── mapper/                           # 10 个 Mapper 接口
├── service/                          # Service 接口 + 实现
└── controller/                       # 15 个 Controller
```

## 🔧 配置说明

| 配置项 | 环境变量 | 默认值 | 说明 |
|--------|----------|--------|------|
| 数据库密码 | `DB_PASSWORD` | — | MySQL 密码 |
| JWT 密钥 | `JWT_SECRET` | — | JWT 签名密钥 |
| CORS 白名单 | `CORS_ALLOWED_ORIGINS` | `*` | 允许的前端域名，逗号分隔 |

## 📦 部署

### JAR 部署

```bash
mvn package -DskipTests
java -jar target/pingpong-saas-1.0.0.jar \
  --spring.datasource.password=${DB_PASSWORD} \
  --jwt.secret=${JWT_SECRET}
```

### Docker 部署（示例）

```dockerfile
FROM eclipse-temurin:21-jre
COPY target/pingpong-saas-1.0.0.jar app.jar
ENV DB_PASSWORD="" JWT_SECRET=""
ENTRYPOINT ["java", "-jar", "/app.jar", \
  "--spring.datasource.password=${DB_PASSWORD}", \
  "--jwt.secret=${JWT_SECRET}"]
```

## 🔗 关联项目

| 仓库 | 说明 |
|------|------|
| [pingpong-saas-springboot](https://github.com/xiaoshun12138/pingpong-saas-springboot) | 后端（本仓库） |
| [pingpong-saas-vue](https://github.com/xiaoshun12138/pingpong-saas-vue) | 前端（Vue 3 + Element Plus） |

## 📄 License

MIT License — 仅供学习交流使用，商业使用请联系作者。

## 🤝 贡献

欢迎提 Issue 和 PR。

- 作者：xiaoshun12138
- GitHub：https://github.com/xiaoshun12138
