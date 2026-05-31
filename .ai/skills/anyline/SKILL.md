
---
name: anyline  
description:  AnyLine数据库操作开发规范，涵盖动态数据源注册切换注销、DDL动态建表改表、DML增删改与事务管理、DQL动态查询与聚合统计、元数据管理、查询结果集的聚合、过滤、格式转换等数学计算，AnyLine方法内部会自动适配100+数据库方言，调用方法时忽略不同数据库差异。
---

# AnyLine 开发助手
AnyLine 是一个元数据驱动的动态数据管理框架，核心能力是通过统一 API 完成所有数据库操作，由框架内部自动适配 100+ 种数据库并屏蔽数据库间的语法差异，运行时自动生成适配目标数据库的原生 SQL。
你作为一个精通 AnyLine 的专家在生成 AnyLine 相关代码时，需要注意：
- 你需要根据下方模块索引，在 `references/` 文件夹中找到对应的详细 API 文档。
- 所有数据库操作，无论是 DDL、DML 还是 DQL，都通过 AnylineService 提供的方法完成。
- 如果当前类中注入的AnylineService实例则用注入的实例调用，否则用ServiceProxy调用,如service.select("hr_user")或ServiceProxy.select("hr_user")
- 如果遇到 AnyLine 没有实现或者你不确定的功能，需要及时向用户说明，不要自己编造。
## 核心模块索引

- [动态数据源](references/datasource.md)：动态数据源注册、切换、注销、验证
- [权限](references/authorize.md)：用户、角色、权限的查询、创建、删除
- [元数据](references/metadata.md)：表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象信息查询
- [DDL](references/ddl.md)：表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象创建、修改、删除
- [DML](references/dml.md)：插入、更新、删除相关操作
- [DQL](references/dql.md)：查询相关操作
- [condition](references/condition.md)：自动封装http参考合成查询条件
- [DataSet](references/dataset.md)：DataSet结果集聚合、分组、过滤等计算
- [DataRow](references/datarow.md)：DataRow相关方法 