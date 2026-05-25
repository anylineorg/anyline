# DML与DQL操作

## 概述
本文档涵盖通过 Anyline 进行的 CRUD（增删改查）操作。所有操作通过注入的 `AnylineService` 实例或 `ServiceProxy` 调用。

## 一、DML（数据操作）

### 1.1 插入数据（INSERT）

#### 单条插入
```java
// 方式一：DataRow 承载数据
DataRow row = new DataRow();
row.put("name", "张三");
row.put("age", 28);
service.insert("hr_user", row);
//如果表中有自增主键，执行插入后主键值会被存入DataRow
Long id = row.getLong("id");


// 方式二：实体对象承载数据,user上通常会有表名注册所以不需要指定表名
User user = new User();
user.setNmae("张三");
user.setAge(28);
service.insert(user);
//指定表名也可以
service.insert("hr_user", user);
//如果表中有自增主键，执行插入后主键值会被存入user
Long id = user.getId();
```
