#### anyline-solon-plugin
```xml
<dependency>
    <groupId>org.anyline</groupId>
    <artifactId>anyline-environment-solon-plugin</artifactId>
    <version>最新版本</version>
</dependency>
<!--https://oss.sonatype.org/content/repositories/snapshots/org/anyline/anyline-environment-solon-plugin/-->
```

#### 1、描述

solon-data数据扩展插件,提供基于AnyLine[【官网】](http://www.anyline.org)[【Git源码仓库】](https://gitee.com/anyline/anyline/tree/master/anyline-environment/anyline-environment-solon-plugin)的面向运行时D-ORM(动态对象关系映射)  
主要用来读写元数据、动态注册切换数据源、对比数据库结构差异、生成动态SQL、复杂的结果集操作  
适配各种关系型与非关系型数据库(及各种国产小众数据库)  
常用于动态结构场景的底层支持，作为SQL解析引擎或适配器出现       
如:数据中台、可视化、低代码、自定义表单、异构数据库迁移同步、运行时自定义报表/查询条件/数据结构等。  

#### 2、一切面向动态、面向运行时

* ##### 动态结构  
  动态场景中没有固定的、预先可知的对象  
  所以也不会有实体类等各种O、没有mapper.xml、repository等    
  只有一组service来处理一切数据库问题 

  
* ##### 动态数据源  
  数据源通常不会出现在配置文件和代码中，而是由用户提交    
  编码时甚至不知道数据源是什么名、是什么类型的数据库  
  所以遇到动态数据源时传统的注释切换数据源、注解事务全部失效

* ##### 结果集处理  
  虽然没有了VO/PO/DTO等各种实体类  
  但anyline提供了更灵活的动态结构用来实现结果集的二次操作

#### 3、示例
##### 数据源注册及切换
注意这里的数据源并不是主从关系，而是多个完全无关的数据源。
```java
DataSource ds_sso = new DruidDataSource();
ds_sso.setUrl("jdbc:mysql://localhost:3306/sso");
ds_sso.setDriverClassName("com.mysql.cj.jdbc.Driver");
...
DataSourceHolder.reg("ds_sso", ds_sso);
或
DataSourceHolder.reg("ds_sso", pool, driver, url, user, password);
DataSourceHolder.reg("ds_sso", Map<String, Object> params); //对应连接池的属性k-v

//查询ds_sso数据源的SSO_USER表
DataSet<DataRow> set = ServiceProxy.service("ds_sso").querys("SSO_USER");
```
来自静态配置文件数据源(或者自定义一个前缀)
```yaml
#默认数据源
anyline:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:33306/simple
    user-name: root
    password: root
  datasource-list: crm,erp,sso
    crm:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:33306/simple_crm
      username: root
      password: root
    erp:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:33306/simple_erp
      username: root
      password: root
    sso:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://localhost:33306/simple_sso
      username: root
      password: root
```

DML
```java
如果是web环境可以
service.querys("SSO_USER", 
  condition(true, "NAME:%name%", "TYPE:[type]", "[CODES]:code"));
//true表示需要分页,没有传参籹值的条件默认忽略
//生成SQL:
SELECT * 
FROM SSO_USER 
WHERE 1=1 
AND NAME LIKE '%?%' 
AND TYPE IN(?,?,?)
AND FIND_IN_SET(?, CODES)	
LIMIT 5,10 //根据具体数据库类型

//用户自定义查询条件,低代码等场景一般需要更复杂的查询条件
ConfigStore configs;
service.query("SSO_USER", configs);
ConfigStore提供了所有的SQL操作  
//多表、批量提交、自定义SQL以及解析XML定义的SQL参数示例代码和说明
```

读写元数据
```java
@Inject("anyline.service")
AnylineService service;

//查询默认数据源的SSO_USER表结构
Table table = serivce.metadata().table("SSO_USER");
LinkedHashMap<String, Column> columns = table.getColumns();                 //表中的列
LinkedHashMap<String, Constraint> constraints = table.getConstraints();     //表中上约束
List<String> ddls = table.getDdls();                                        //表的创建SQL

//删除表 重新创建
service.ddl().drop(table);
table = new Table("SSO_USER");

//这里的数据类型随便写，不用管是int8还是bigint,执行时会转换成正确的类型
table.addColumn("ID", "BIGINT").autoIncrement(true).setPrimary(true).setComment("主键");
table.addColumn("CODE", "VARCHAR(20)").setComment("编号");
table.addColumn("NAME", "VARCHAR(20)").setComment("姓名");
table.addColumn("AGE", "INT").setComment("年龄");
service.ddl().create(table);

或者service.ddl().save(table); 执行时会区分出来哪些是列需要add哪些列需要alter
```
事务

```java
//因为方法可以有随时切换多次数据源,所以注解已经捕捉不到当前数据源了
//更多事务参数通过TransactionDefine参数
TransactionState state = TransactionProxy.start("ds_sso"); 
//操作数据
TransactionProxy.commit(state);
TransactionProxy.rollback(state);
```

#### 数据结构
数据结构主要有DataSet,DataRow两种  
分别对应数据库的表与行如果是来自数据库查询，则会附带SQL或表的元数据  

##### DataRow
相当于Map 通常来自数据库或实体类、XML、JSON等格式的转换  
提供了常用的格式化、ognl表达式、对比、复制、深层取值、批量操作等方法

##### DataSet
是DataRow的集合 相当于List
结果集的过滤、求和、多级树、平均值、行列转换、分组、方差等各种聚合操作等可以通过DataSet自带的方法实现  
有些情况下从数据库中查出结果集后还需要经过多次过滤，用来避免多次查询给数据库造成不必要的压力  
DataSet类似sql的查询  
DataSet<DataRow> result = set.select.equals("AGE","20")的方式调用

**具体可参考：**  
[源码](https://gitee.com/anyline/anyline)  
[使用说明](http://doc.anyline.org/ss/03_12)  
[示例代码](https://gitee.com/anyline/anyline-simple)  