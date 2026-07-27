# 项目启动指南

> 项目路径：`~/Documents/pingpong-saas`（Maven坐标 `pingpong-saas`）

## 一、环境版本确认

| 组件 | 版本要求 | 本地版本 |
|------|----------|----------|
| JDK | ≥ 17 | OpenJDK 25.0.2 (Amazon Corretto) |
| Maven | ≥ 3.6 | 3.9.15 |
| MySQL | 8.0 | 8.0.45 |

确认命令：
```bash
java -version
mvn -version
mysql --version
```

> ⚠️ 本机 Maven 在 `~/bin/mvn`，已加入 PATH，可直接敲 `mvn`。

---

## 二、前置条件：MySQL 数据库已就绪

```bash
mysql -u root -phelloworld -e "SHOW DATABASES;" 2>&1 | grep pingpong
```

预期输出：看到 `pingpong_saas` 数据库名。

如果数据库不存在（之前可能删了），从 `~/Desktop/sql2/` 重新初始化：
```bash
mysql -u root -phelloworld < ~/Desktop/sql2/01_schema.sql
mysql -u root -phelloworld pingpong_saas < ~/Desktop/sql2/02_init_data.sql
```

---

## 三、修改 MySQL 密码（如需）

`application.yml` 里写的是 `password: helloworld`，如果本机 MySQL 密码不是这个，改掉：

```yaml
# src/main/resources/application.yml 第6行
password: 你的密码
```

---

## 四、启动项目

```bash
cd ~/Documents/pingpong-saas
mvn spring-boot:run
```

或者先打包再运行：
```bash
mvn package -DskipTests
java -jar target/pingpong-saas-1.0.0.jar
```

启动成功后看到：
```
Tomcat started on port(s): 8080
Started PingPongApplication in X.XX seconds
```

---

## 五、验证接口是否通

浏览器或 curl 访问：

```bash
# 健康检查（门店列表）
curl http://localhost:8080/api/stores

# 学员列表（分页）
curl http://localhost:8080/api/students

# 课包列表
curl http://localhost:8080/api/course-types
```

预期返回 JSON：
```json
{"code":200,"message":"操作成功","data":{...}}
```

---

## 六、所有接口一览

| 模块 | 方法 | URL | 说明 |
|------|------|-----|------|
| 门店 | GET | `/api/stores` | 门店列表（分页） |
| 门店 | GET | `/api/stores/{id}` | 门店详情 |
| 门店 | POST | `/api/stores` | 新增门店 |
| 门店 | PUT | `/api/stores` | 更新门店 |
| 门店 | DELETE | `/api/stores/{id}` | 删除门店 |
| 员工 | GET | `/api/staff` | 员工列表（分页） |
| 员工 | GET | `/api/staff/{id}` | 员工详情 |
| 员工 | POST | `/api/staff` | 新增员工 |
| 员工 | PUT | `/api/staff` | 更新员工 |
| 员工 | DELETE | `/api/staff/{id}` | 删除员工 |
| 学员 | GET | `/api/students` | 学员列表（分页） |
| 学员 | GET | `/api/students/{id}` | 学员详情 |
| 学员 | POST | `/api/students` | 新增学员 |
| 学员 | PUT | `/api/students` | 更新学员 |
| 学员 | DELETE | `/api/students/{id}` | 删除学员 |
| 课包 | GET | `/api/course-types` | 课包列表 |
| 课包 | GET | `/api/course-types/{id}` | 课包详情 |
| 课包 | POST | `/api/course-types` | 新增课包 |
| 课包 | PUT | `/api/course-types` | 更新课包 |
| 课包 | DELETE | `/api/course-types/{id}` | 删除课包 |
| 订单 | GET | `/api/course-orders` | 订单列表（分页） |
| 订单 | GET | `/api/course-orders/{id}` | 订单详情 |
| 订单 | POST | `/api/course-orders` | 新增订单 |
| 订单 | PUT | `/api/course-orders` | 更新订单 |
| 订单 | DELETE | `/api/course-orders/{id}` | 删除订单 |
| 消课 | GET | `/api/course-consumptions` | 消课记录（分页） |
| 消课 | GET | `/api/course-consumptions/{id}` | 消课详情 |
| 消课 | POST | `/api/course-consumptions` | 新增消课 |
| 消课 | PUT | `/api/course-consumptions` | 更新消课 |
| 消课 | DELETE | `/api/course-consumptions/{id}` | 删除消课 |
| 月目标 | GET | `/api/monthly-targets` | 月度目标列表 |
| 月目标 | POST | `/api/monthly-targets` | 新增月度目标 |
| 退款 | GET | `/api/refund-logs` | 退款记录列表 |
| 退款 | POST | `/api/refund-logs` | 新增退款（只读） |
| 周目标 | GET | `/api/weekly-targets` | 周目标列表 |
| 周目标 | POST | `/api/weekly-targets` | 新增周目标 |
| 周目标 | PUT | `/api/weekly-targets` | 更新周目标 |
| 周目标 | DELETE | `/api/weekly-targets/{id}` | 删除周目标 |

---

## 七、分页参数

所有列表接口支持分页：
```
GET /api/students?current=1&size=20
```
- `current`：当前页（默认 1）
- `size`：每页条数（默认 10）

---

## 八、常见问题

**Q: `com.mysql.cj.jdbc.Driver` 找不到？**
A: 检查 `application.yml` 里 `driver-class-name` 是否写对，MySQL 8 用 `com.mysql.cj.jdbc.Driver`，旧版用 `com.mysql.jdbc.Driver`。

**Q: 启动报数据库连接失败？**
A: 确认 MySQL 在运行：`mysql -u root -phelloworld -e "SELECT 1"`

**Q: 字段映射不上？**
A: `application.yml` 已配置 `map-underscore-to-camel-case: true`，下划线自动转驼峰，无需额外配置。
