
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
