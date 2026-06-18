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

### 数据源与连接
- [动态数据源](references/datasource.md)：动态数据源注册、切换、注销、验证、临时数据源、虚拟数据源

### 数据操作
- [DDL](references/ddl.md)：表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象创建、修改、删除
- [DML](references/dml.md)：插入、更新、删除、upsert、批量执行相关操作
- [DQL](references/dql.md)：查询、条件构建、分页、排序、分组、子查询、流式查询
- [condition](references/condition.md)：自动封装HTTP参数合成查询条件

### 数据容器
- [DataRow](references/datarow.md)：DataRow数据行操作、类型转换、JSON解析
- [DataSet](references/dataset.md)：DataSet结果集聚合、分组、过滤、排序、格式化等计算

### 元数据管理
- [元数据](references/metadata.md)：表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象信息查询
- [权限](references/authorize.md)：用户、角色、权限的查询、创建、删除、授权

### 事务与扩展
- [事务管理](references/transaction.md)：编程式事务、传播级别、隔离级别、保存点、跨线程事务
- [拦截器](references/interceptor.md)：SQL执行拦截、参数修改、结果处理
- [监听器](references/listener.md)：DDL/DML事件监听、数据源加载监听

## 扩展数据源类型

AnyLine 支持多种非关系型数据库和特殊数据源，通过统一的 API 进行操作：

### 图数据库
- **Neo4j**：通过 `anyline-data-neo4j` 或 `anyline-data-jdbc-neo4j` 模块支持
- **Nebula Graph**：通过 `anyline-data-nebula` 模块支持
- **TuGraph**：通过 `anyline-data-tugraph` 模块支持
- 操作方式与关系型数据库类似，使用 `service.selects()` 等方法，框架自动生成 Cypher 或 nGQL

### 文档数据库
- **MongoDB**：通过 `anyline-data-mongodb` 模块支持
- 支持 Document 结构的 CRUD 操作

### AI 数据源
- **Dify**：通过 `anyline-data-dify` 模块支持，用于 RAG 文档检索
- **Coze**：通过 `anyline-data-coze` 模块支持

### 其他数据源
- **ElasticSearch**：通过 `anyline-data-jdbc-elasticsearch` 模块支持
- **Cassandra**：通过 `anyline-data-jdbc-cassandra` 模块支持
- **HBase**：通过 `anyline-data-hbase` 模块支持

## 数据源切换方式

### 1. 通过 ServiceProxy 切换
```java
// 获取指定数据源的 Service
AnylineService crmService = ServiceProxy.service("crm");
crmService.select("CRM_USER");

// 获取默认数据源的 Service
AnylineService defService = ServiceProxy.service();
defService.select("HR_USER");
```

### 2. 通过表名前缀临时切换
```java
// 在表名前添加数据源名称，仅当前查询有效
service.select("<crm>CRM_USER");
```

### 3. 临时数据源
```java
// 创建临时数据源，用完自动回收
DataSource ds = DataSourceUtil.build(...);
AnylineService service = ServiceProxy.temporary(ds);
service.select("CRM_USER");
```

### 4. 虚拟数据源（仅生成SQL不执行）
```java
// 根据数据库类型创建虚拟数据源，用于生成SQL脚本
AnylineService service = ServiceProxy.service(DatabaseType.POSTGRESQL);
ConfigStore configs = new DefaultConfigStore();
service.selects("CRM_USER", configs);
List<Run> runs = configs.runs();
// 获取生成的 PostgreSQL SQL
```

## 返回SQL方言(DDL/DML/DQL)以及SQL日志

SQL执行过程中，在控制台或日志文件中会生成带占位符的日志。  
如果需要在执行完成后返回SQL(DQL DDL DML),可以在service调用的方法中添加ConfigStore参数  
在执行完成后从ConfigStore中获取执行的SQL,   
因为执行的SQL可能是多条，所以会返回一个List<Run>集合，  
默认情况下Run中的SQL是带占位符的，与占位值分开存储  

```java
List<Run> runs = configs.runs();
for (Run run:runs){
    System.out.println("无占位符 sql:"+run.getFinalQuery(false));
    System.out.println("有占位符 sql:"+run.getFinalQuery());
    System.out.println("占位values:"+run.getValues());
}
```

如果只需要生成SQL，但不执行，可以调用相应的adapter.buildQueryRun/buildDeleteRun等方法 返回的Run与以上操作相同  
  
因为adapter平时不常用，所以许多人对此不太熟悉，也可以在ConfigStore上调用execute(false)方法  
然后调用service.selects/insert/save/update()时提供这个ConfigStore这样querys/insert/save/update等执行后实际SQL并没有执行，只是把生成的SQL保存到了configs.runs  

```java
ConfigStore configs = new DefaultConfigStore().execute(false);//false表示最后实际操作数据库的一步不执行
DataSet set = service.selects(table, configs);
System.out.println(set);//因为最后一步没有执行，所以这里应该输出空集合
 
List<Run> runs = configs.runs();
for (Run run:runs){
     System.out.println("无占位符 sql:"+run.getFinalQuery(false));
     System.out.println("有占位符 sql:"+run.getFinalQuery());
     System.out.println("占位values:"+run.getValues());
}
```

DDL也类似，因为DDL执行过程中有元数据的对象所以execute(false)在metadata(table/column等)上执行

```java
Table table = service.metadata().table("sso_user"); //获取表结构
table.execute(false);//不执行SQL
service.ddl().create(table);
List<Run> runs = table.runs(); //返回创建表的DDL
String sql = run.getFinalUpdate()
```

## 常用配置项

通过 `ConfigTable` 可以设置全局配置：

```java
// 主键默认列名
ConfigTable.DEFAULT_PRIMARY_KEY = "ID";

// 是否自动检测元数据
ConfigTable.IS_AUTO_CHECK_METADATA = true;

// 元数据缓存时间（秒）
ConfigTable.METADATA_CACHE_SECOND = 3600 * 24;

// 是否返回空实例替代null
ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL = true;

// 列名大小写处理
ConfigTable.IS_UPPER_KEY = true;  // 列名转大写
ConfigTable.IS_LOWER_KEY = false; // 列名转小写

// 空值处理
ConfigTable.IS_INSERT_NULL_COLUMN = false;
ConfigTable.IS_UPDATE_NULL_COLUMN = false;
ConfigTable.IS_INSERT_EMPTY_COLUMN = false;
ConfigTable.IS_UPDATE_EMPTY_COLUMN = false;
```

## 注意事项

1. **数据源切换**：8.7.2版本后取消了 ThreadLocal 方式的数据源切换，改用 ServiceProxy.service(key) 方式
2. **主键处理**：默认主键为 ID，可通过 ConfigTable.DEFAULT_PRIMARY_KEY 修改
3. **空值处理**：默认空值(null/空字符串)不参与插入和更新，可通过配置修改
4. **事务支持**：临时数据源不支持注解方式的事务，需使用编程式事务
5. **批量操作**：大批量插入建议使用 batch 参数，如 `service.insert(200, table, set)`