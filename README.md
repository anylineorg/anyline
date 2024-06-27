8.7.2后分离了多个环境(Spring/Solon/Vertx)比之前的版本多一个依赖  
***具体参考***[【8.7.2更新】](http://doc.anyline.org/aa/2f_3951)  ***详细说明请参考:*** [【官网】](http://doc.anyline.org/)  
开发测试环境请使用[【8.7.2-20240423】](http://doc.anyline.org/aa/aa_3702)版本 语法测试请参考[【各数据库模拟环境】](http://run.anyline.org/)       
发版务必到[【中央库】](https://mvnrepository.com/artifact/org.anyline/anyline-core)找一个正式版本，不要把SNAPSHOT版本发到生产环境  
关于多数据源，请先阅读   
[【三种方式注册数据源】](http://doc.anyline.org/aa/a9_3451)
[【三种方式切换数据源】](http://doc.anyline.org/aa/64_3449)
[【多数据源事务控制】](http://doc.anyline.org/ss/23_1189)  
低代码平台、数据中台等场景需要生成SQL/操作元数据参考  
[【JDBCAdapter】](http://doc.anyline.org/ss/01_1193)
[【返回SQL方言、导出SQL】](http://doc.anyline.org/aa/70_3793)
[【表结构差异对比及生成DDL】](http://doc.anyline.org/aa/a2_3936)
[【ConfigStore与JSON互换】](http://doc.anyline.org/aa/73_13975)
[【service.metadata】](http://doc.anyline.org/ss/22_1174)
[【SQL.metadata】](http://doc.anyline.org/aa/c1_3847)

***快速开始请参考示例源码(各种各样最简单的hello world):***  
[https://gitee.com/anyline/anyline-simple](https://gitee.com/anyline/anyline-simple)

***一个字都不想看，就想直接启动项目的下载这个源码:***  
[https://gitee.com/anyline/anyline-simple-clear](https://gitee.com/anyline/anyline-simple-clear)

有问题请不要自行百度，因为百度收录的内容有可能过期或版本不一致, 有问题请联系


|   [<img src="https://alcdn.oss-cn-shanghai.aliyuncs.com/img/user/alq.png" width="150">](http://shang.qq.com/wpa/qunwpa?idkey=279fe968c371670fa9791a9ff8686f86dbac0b5edba8021a660b313e2dd863ad)   | [<img src="https://alcdn.oss-cn-shanghai.aliyuncs.com/img/user/alvg.png?v=1" width="150">](http://www.anyline.org/ss/9f_17) | [<img src="https://alcdn.oss-cn-shanghai.aliyuncs.com/img/user/alv.png" width="150">](http://www.anyline.org/ss/9f_17) |
|:-------:|:-------------------------------------------------------------------------------------------------------:|:-------------------------------------------------------------------:|
|   QQ群(86020680)   |                                                   微信群                                                   |                             过期或满员联系管理员                              |

## 简介
AnyLine的核心是一个面向运行时的 元数据动态关系映射 主要用于  
- 动态注册切换数据源
- 读写元数据  
- 对比数据库结构差异  
- 生成动态SQL,组合动态查询条件  
- 复杂的结果集操作    

适配各种关系型与非关系型数据库(及各种国产小众数据库)    
常用于动态结构场景的底层支持，作为SQL解析引擎或适配器出现  
如:数据中台、可视化、低代码、SAAS、自定义表单、异构数据库迁移同步、 物联网车联网数据处理、
条件/数据结构、 爬虫数据解析等。
参考【[适用场景](http://doc.anyline.org/ss/ed_14)】  

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
DataSet set = ServiceProxy.service("ds_sso").querys("SSO_USER");
```
来自静态配置文件数据源(如果是spring环境可以按spring格式)
```properties
#默认数据源
anyline.datasource.type=com.zaxxer.hikari.HikariDataSource
anyline.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.url=jdbc:mysql://localhost:33306/simple
anyline.datasource.user-name=root
...更多参数
#其他数据源
anyline.datasource-list=crm,erp,sso,mg

anyline.datasource.crm.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.crm.url=jdbc:mysql://localhost:33306/simple_crm
anyline.datasource.crm.username=root

anyline.datasource.erp.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.erp.url=jdbc:mysql://localhost:33306/simple_erp
anyline.datasource.erp.username=root
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
ConfigStore confis;
service.query("SSO_USER", configs);
ConfigStore提供了所有的SQL操作  
//多表、批量提交、自定义SQL以及解析XML定义的SQL参数示例代码和说明
```

读写元数据
```java
@Autowired("anyline.service")
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

### 已经有ORM了 为什么还要有AnyLine, 与ORM有什么区别
- ***面向场景不同***  
anyline主要面向动态场景，就是运行时随时可变的场景。  
如我们常用的动态数据源，不是在部署时可以固定在配置文件中，  
而是可能在不确定的时间，由不确定的用户提供的不确定数据源。  
表结构等元数据也可能随着着用户或数据源的不同而随时变化。  

- ***针对产品不同***  
anyline一般不会直接用来开发一个面向终端用户的产品(如ERP、CRM等)，  
而是要开发一个中间产品(如低代码平台)，让用户通过中间产品来生成一个最终产品。  
再比如用anyline开发一个自定义查询分析工具，让用户通过这个工具根据业务需求生成动态报表。  
anyline不是要提供一个可二次开发的半成品船，而是可以用来造船的动态船坞。
  

- ***操作对象不同***
anyline主要操作元数据，因为在项目开发之初，可能就没有一个如ERP/CRM之类的明确的产品，  
当然也就没有订单、客户之类的具体对象及属性，所以也没什么具体数据可操作。

- ***面向用户(开发设计人员)不同***  
anyline要面向的不是开船的人，而是造船的人，而不是使用工具的人，而是设计工具的人。
anyline的大部分代码与灵感也是来自这部分用户的日常实践。   
  

- ***所以对用户(开发设计人员)要求不同***  
一个ORM用户用了许多年的连接池，他可以只知道配置哪几个默认参数，也能正常开展工作。  
但anyline的用户不行，他要求这个团队中至少有一个人要明白其所以然。

### 实际操作中与ORM最明显的区别是  
- ***摒弃了各种繁琐呆板的实体类以及相关的配置文件***  
  让数据库操作更简单，不要一动就是一整套的service/dao/mapping/VOPODTO有用没用的各种O，生成个简单的SQL也各种判断遍历。  
- ***强强化了结果集的对象概念***  
  面向对象的对象不是只有get/set/注解这么弱  
  需要把数据及对数据的操作封装在一起，作为一个相互依存的整体，并实现高度的抽象  
  要关注元数据，不要关注姓名、年龄等具体属性  
  强化针对结果集的数据二次处理能力  
  如结果集的聚合、过滤、行列转换、格式化及各种数学计算尽量作到一键...一键...  
  而不要像ORM提供的entity, map, list除了提供个get/set/foreach，稍微有点用的又要麻烦程序员各种判断各种遍历  
  参考【[疑问与优劣对比](http://doc.anyline.org/ss/ae_1196)】
### 与如何实现
数据操作的两个阶段，1.针对数据库中数据 2.针对数据库查询的结果集(内存中的数据)
- ***提供一个通用的AnylineService实现对数据库的一切操作***
- ***提供一对DataSet/DataRow实现对内存数据的一切数学计算***  
 DataSet/DataRow不是对List/Map的简单封装 他将是提高我们开发速度的重要工具，各种想到想不到的数学计算，只要不是与业务相关的都应该能实现

 
## AnyLine解决或提供了什么

### 动态、运行时
即运行时才能最终确定 动态的数据源、数据结构、展现形式  
如我们需要开发一个数据中台或者一个数据清洗插件，编码阶段我们还不知道数据来源、什么类型的数据库甚至不是数据库、会有什么数据结构对应什么样的实体类，  
如果需要前端展示的话，更不会知道不同的终端需要什么各种五花八门的数据组合  
那只能定义一个高度抽象的实体了，想来想去也只有Collection<Map>可以胜任了。  

### 简单快速的操作数据库
最常见的操作：根据条件分页查询一个表的几列  
这一动就要倾巢出动一整套的service/dao/vo dto 各种O/mapper，生成个查询条件各种封装、为了拼接个SQL又是各种if else forearch  
如果查询条件是由前端的最终用户动态提供的，那Java里if完了还不算完，xml中if也少不了  
一旦分了页，又要搞出另一套数据结构，另一组接口，另一组参数(当然这种拙劣的设计只是极个别，不能代表ORM)  

### 简单快速的操作结果集
数据库负责的是存储，其结构肯定是与业务需要不一样的。所以结果集需要处理。当我们需要用Map处理数据或数学计算时，  
如最常见的数据格式化、筛选、分组、平均值、合计、方差等聚合计算    
再如空值的处理包括, "", null, "null","\n","\r","\t","   "全角、半角等各种乱七八糟的情况  
这时就会发现Map太抽象了，除了get/set/forearch好像也没别的可施展了。  
要处理的细节太多了，if都不够用了。  

### 动态数据源
再比如多数据源的情况下，要切换个数据源又是IOC又是AOP一堆设计模式全出场。经常是在方法配置个拦截器。  
在同一个方法里还能切换个数据源了？  
数据中台里有可能有几百几千个数据源，还得配上几千个方法？  
数据源是用户动态提交的呢怎么拦截呢？  
这不是DB Util的本职工作么，还要借助其他？  
哪个项目少了AOP依赖还切换不了数据源了？  

### 重复工作
如果只是写个helloworld，以上都不是问题，没什么解决不了的。但实际工作中是需要考虑工作量和开发速度的。  
比如一个订单可能有几十上百列的数据，每个分析师需要根据不同的列查询。有那么几十列上同时需要<>=!=IN FIND_IN_SET多种查询方式算正常吧  
不能让开发人员挨个写一遍吧，写一遍是没问题，但修改起来可就不是一遍两遍的事了    
所以需要提供一个字典让用户自己去配置，低代码开发平台、自定义报表、动态查询条件应该经常有这个需求。    
当用户提交上来一个列名、一个运算算、一组值，怎么执行SQL呢，不能在代码中各种判断吧，如果=怎么合成SQL，如果IN怎么合成SQL    

### 多方言
DML方面hibernate还可以处理，DDL呢？国产库呢？

当然这种问题很难有定论，只能在实际应用过程中根据情况取舍。  
可以参考【[适用场景](http://doc.anyline.org/ss/ed_14)】和【[实战对比](http://doc.anyline.org/ss/c9_1153)】中的示例  
造型之前，当然要搞明白优势劣势，参考【[优势劣势](http://doc.anyline.org/aa/24_3712)】

## 误解
当然我们并不是要抛弃Entity或ORM，相反的 AnyLine源码中也使用了多达几十个Entity   
在一些 **可预知的 固定的** 场景下，Entity的优势还是不可替代的  
程序员应该有分辨场景的能力  
AnyLine希望程序员手中多一个数据库操作的利器，而不是被各种模式各种hello world限制

## 如何使用
数据操作***不要***再从生成xml/dao/service以及各种配置各种O开始  
默认的service已经提供了大部分的数据库操作功能。  
操作过程大致如下:
```
DataSet set = service.querys("HR_USER(ID, NM)", 
    condition(true, "anyline根据约定自动生成的=, in, like等查询条件"));  
```
这里的查询条件不再需要各种配置, 各种if else foreach标签  
Anyline会自动生成, 生成规则可以【参考】这里的[【约定规则】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f2c45dd13bb112b34176fad5a868fa6a4)  
分页也不需要另外的插件，更不需要繁琐的计算和配置，指定true或false即可

## 如何集成

只需要一个依赖、一个注解即可实现与springboot, netty等框架项目完美整合，参考【入门系列】
大概的方式就是在需要操作数据库的地方注入AnylineService
接下来service就可以完成大部分的数据库操作了。常用示例可以参考【[示例代码](https://gitee.com/anyline/anyline-simple)】

## 兼容
如果实现放不下那些已存在的各种XOOO  
DataSet与Entity之间可以相互转换  
或者这样:
```
EntitySet<User> = service.querys(User.class, 
    condition(true, "anyline根据约定自动生成的查询条件")); 
//true：表示需要分页
//为什么不用返回的是一个EntitySet而不是List?
//因为分页情况下, EntitySet中包含了分页数据, 而List不行。
//无论是否分页都返回相同的数据结构，而不需要根据是否分页实现两个接口返回不同的数据结构

//也可以这样(如果真要这样就不要用anyline了, 还是用MyBatis, Hibernate之类吧)
public class UserService extends AnylinseService<User> 
userService.querys(condition(true, "anyline根据约定自动生成的查询条件")); 
```

## 适用场景
- **低代码后台**    
  主要用来处理动态属性、动态数据源、运行时自定义查询条件、元数据管理等。  
  比较容易落地的几个场景如财务、库存等ERP模块用户经常需要输出不同格式的报表，根据不同维度查询统计数据  
  前端可以用百度amis前端低代码框架，后端由anyline解析SQL及查询条件，管理元数据。  
  [【示例】](https://gitee.com/anyline/service)

- **数据中台**    
  动态处理各种异构数据源、强大的结果集批量处理能力，不再需要对呆板的实体类各种遍历各种转换。  
  通常需要在运行时频繁的注册、切换、注销数据源  
  [【示例】](https://gitee.com/anyline/anyline-simple)

- **可视化数据源**  
  主要用来处理动态属性，以及适配前端的多维度多结构的数据转换   
  可视化中最主要的工作就是生成图表了，如果是通过map可实体类那又免不了各种判断、遍历、计算如果是有限的几个固定的图表还可以处理，
  如果是大量的动态图表呢(比如由前端用户提供统计方式与维度)  
  [【参考】](http://doc.anyline.org/a?id=p298pn6e9o1r5gv78vicac1e624c62387f7bb5cdeaeddf6f93f9eb865d5cc60b9b)

- **物联网车联网数据处理**    
  如车载终端、交通信号灯、数字化工厂传感器、环境检测设备数据等   
  这种场景通常会涉及到时序数据库    
  时序库虽然快，但是结构简单，数据需要经过各种组合后给业务系统       
  时序库通常需要在运行时操作大量的DDL   
  [【示例】](https://gitee.com/anyline/service)

- **数据清洗、数据批量处理**  
  各种结构的数据、更多的是不符合标准甚至是错误的结构  
  这种场景下需要一个灵活的数据结构来统一处理各种结构的数据    
  再想像一下临时有几个数据需要处理一下(如补齐或替换几个字符)  
  这个时候先去创建个Entity, XML, Service, Dao吗  
  [【示例】](https://gitee.com/anyline/service)

- **报表输出，特别是用户自定义报表**  
  类似于可视化环境, 样式相对简单一点，但精度要求极高，需要控制到像素、字体等  
  如检验检测报告、资质证书等，当然这需要配合 anyline-office  
  [【office示例】](http://office.anyline.org/v/b6_3797)

- **工作流(运行时自定义表单/查询条件/数据结构)**  
  各个阶段都要自定义，比低代码要求更高的是:操作用户不懂编程
  [【示例】](https://gitee.com/anyline/service)

- **网络爬虫数据解析**    
  不固定的结构、html解析(当然不是用正则或dom那太费脑子了)  
  [【参考】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f51d08504f16eef5d4dd25719cf7844ce)

- **异构数据库迁移同步**  
  动态的数据结构可以灵活的适配多种不同的表, 需不需要反复的get/set    
  兼容多种数据库的DDL也可以方便的在不同类型的数据库中执行  
  [【核心代码示例(Mysql到Apache Ignite)】](http://doc.anyline.org/aa/08_3842)  
  [【基础应用项目】](https://gitee.com/anyline/anyline-database-sync)
  [【完整应用代替datax】](https://gitee.com/czarea/devops)

- **还有一种很实现的场景是 许多项目到了交付的那一天 实体也没有设计完成**  
  别说设计了，需求都有可能还没结束就催交付了, Entity哪里找  
  [【示例】](https://gitee.com/anyline/service)

##  关于数据库的适配  [【更多查看】](http://doc.anyline.org/dbs)  
[【示例源码】](https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect)
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mysql"><img alt="MYSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mysql.jpg" width="100"/>MYSQL</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-postgresql"><img alt="PostgreSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/postgres.png" width="100"/>PostgreSQL</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oracle"><img alt="ORACLE" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>ORACLE</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mssql"><img alt="MSSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mssql.jpg" width="100"/>MSSQL</a>
<a style="display:inline-block;" href="https://www.mongodb.com/"><img alt="MongoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mongodb.svg" width="100"/>MongoDB</a>
<a style="display:inline-block;" href="https://redis.com/"><img alt="Redis" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/redis.svg" width="100"/>Redis</a>
<a style="display:inline-block;" href="https://www.elastic.co/elasticsearch/"><img alt="ElasticSearch" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/es.svg" width="100"/>ElasticSearch</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-db2"><img alt="DB2" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/db2.webp" width="100"/>DB2</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-dm"><img alt="DM" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dm.webp" width="100"/>DM(武汉达梦数据库股份有限公司)</a>
<a style="display:inline-block;" href="https://www.gbase.cn/"><img alt="GBase8a" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/gbase.webp" width="100"/>GBase8a(天津南大通用数据技术股份有限公司)</a>
<a style="display:inline-block;" href="https://www.gbase.cn/"><img alt="GBase8c" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/gbase.webp" width="100"/>GBase8c(天津南大通用数据技术股份有限公司)</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-gbase8s"><img alt="GBase8s" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/gbase.webp" width="100"/>GBase8s(天津南大通用数据技术股份有限公司)</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oscar"><img alt="oscar" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oscar.png" width="100"/>oscar</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-sqlite"><img alt="SQLite" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sqlite.gif" width="100"/>SQLite</a>
<a style="display:inline-block;" href="https://www.snowflake.com/"><img alt="Snowflake" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/snowflake.svg" width="100"/>Snowflake</a>
<a style="display:inline-block;" href="https://cassandra.apache.org/"><img alt="Cassandra" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cassandra.svg" width="100"/>Cassandra</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mariadb"><img alt="MariaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mariadb.png" width="100"/>MariaDB</a>
<a style="display:inline-block;" href="https://www.splunk.com/"><img alt="Splunk" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/splunk.svg" width="100"/>Splunk</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/en-us/products/azure-sql/database/"><img alt="AzureSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/microsoft.png" width="100"/>AzureSQL</a>
<a style="display:inline-block;" href="https://aws.amazon.com/dynamodb/"><img alt="AmazonDynamoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonDynamoDB</a>
<a style="display:inline-block;" href="https://www.databricks.com/"><img alt="Databricks" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/databricks.svg" width="100"/>Databricks</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hive"><img alt="Hive" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hive.svg" width="100"/>Hive</a>
<a style="display:inline-block;" href="https://www.microsoft.com/en-us/microsoft-365/access"><img alt="Access" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/access.webp" width="100"/>Access</a>
<a style="display:inline-block;" href="https://cloud.google.com/bigquery/"><img alt="GoogleBigQuery" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/googlecloud.webp" width="100"/>GoogleBigQuery</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-highgo"><img alt="HighGo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/highgo.svg" width="100"/>HighGo(瀚高基础软件股份有限公司)</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mssql"><img alt="MSSQL2000" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mssql.jpg" width="100"/>MSSQL2000</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-neo4j"><img alt="Neo4j" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/neo4j.webp" width="100"/>Neo4j</a>
<a style="display:inline-block;" href="https://www.polardbx.com/home"><img alt="PolarDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/polardb.webp" width="100"/>PolarDB(阿里云计算有限公司)</a>
<a style="display:inline-block;" href=""><img alt="Sybase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sap.svg" width="100"/>Sybase</a>
<a style="display:inline-block;" href="https://www.teradata.com/"><img alt="TeraData" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/teradata.ico" width="100"/>TeraData</a>
<a style="display:inline-block;" href="https://www.claris.com/filemaker/"><img alt="FileMaker" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/filemarker.png" width="100"/>FileMaker</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hana"><img alt="HANA" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hana.png" width="100"/>HANA</a>
<a style="display:inline-block;" href="https://solr.apache.org/"><img alt="Solr" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/solr.svg" width="100"/>Solr</a>
<a style="display:inline-block;" href="https://www.sap.com/products/sybase-ase.html"><img alt="Adaptive" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sap.svg" width="100"/>Adaptive</a>
<a style="display:inline-block;" href="https://hbase.apache.org/"><img alt="Hbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hbase.png" width="100"/>Hbase</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/services/cosmos-db"><img alt="AzureCosmos" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/microsoft.png" width="100"/>AzureCosmos</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-influxdb"><img alt="InfluxDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/influx.svg" width="100"/>InfluxDB</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-postgis"><img alt="PostGIS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/postgis.webp" width="100"/>PostGIS</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/services/synapse-analytics/"><img alt="AzureSynapse" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/microsoft.png" width="100"/>AzureSynapse</a>
<a style="display:inline-block;" href="http://www.firebirdsql.org/"><img alt="Firebird" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/firebird.png" width="100"/>Firebird</a>
<a style="display:inline-block;" href="https://www.couchbase.com/"><img alt="Couchbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/couchbase.svg" width="100"/>Couchbase</a>
<a style="display:inline-block;" href="https://aws.amazon.com/redshift/"><img alt="AmazonRedshift" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonRedshift</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/informix"><img alt="Informix" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/informix.webp" width="100"/>Informix</a>
<a style="display:inline-block;" href="http://www.memcached.org/"><img alt="Memcached" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/memcached.webp" width="100"/>Memcached</a>
<a style="display:inline-block;" href="https://spark.apache.org/sql/"><img alt="Spark" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/spark.svg" width="100"/>Spark</a>
<a style="display:inline-block;" href="https://www.cloudera.com/products/open-source/apache-hadoop/impala.html"><img alt="Cloudera " src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cloudera.png" width="100"/>Cloudera </a>
<a style="display:inline-block;" href="https://firebase.google.cn/products/realtime-database/"><img alt="Firebase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/firebase.svg" width="100"/>Firebase</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-clickhouse"><img alt="ClickHouse" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/clickhouse.png" width="100"/>ClickHouse</a>
<a style="display:inline-block;" href="https://prestodb.io/"><img alt="Presto" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Presto</a>
<a style="display:inline-block;" href="https://www.vertica.com/"><img alt="Vertica" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vertica.png" width="100"/>Vertica</a>
<a style="display:inline-block;" href="http://www.dbase.com/"><img alt="dbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dbase.png" width="100"/>dbase</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/netezza"><img alt="Netezza" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ibm.svg" width="100"/>Netezza</a>
<a style="display:inline-block;" href="https://github.com/opensearch-project"><img alt="OpenSearch" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/opensearch.ico" width="100"/>OpenSearch</a>
<a style="display:inline-block;" href="https://flink.apache.org/"><img alt="Flink" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/flink.png" width="100"/>Flink</a>
<a style="display:inline-block;" href="https://couchdb.apache.org/"><img alt="CouchDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/couchdb.png" width="100"/>CouchDB</a>
<a style="display:inline-block;" href="https://firebase.google.com/products/firestore/"><img alt="GoogleFirestore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/googlecloud.webp" width="100"/>GoogleFirestore</a>
<a style="display:inline-block;" href="https://greenplum.org/"><img alt="Greenplum" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/greenplum.png" width="100"/>Greenplum</a>
<a style="display:inline-block;" href="https://aws.amazon.com/rds/aurora/"><img alt="AmazonAurora" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonAurora</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-h2"><img alt="H2" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/h2db.png" width="100"/>H2</a>
<a style="display:inline-block;" href="https://kx.com/"><img alt="Kdb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kx.svg" width="100"/>Kdb</a>
<a style="display:inline-block;" href="https://etcd.io/"><img alt="etcd" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/etcd.svg" width="100"/>etcd</a>
<a style="display:inline-block;" href="https://realm.io/"><img alt="Realm" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/realm.svg" width="100"/>Realm</a>
<a style="display:inline-block;" href="https://www.marklogic.com/"><img alt="MarkLogic" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/marklogic.png" width="100"/>MarkLogic</a>
<a style="display:inline-block;" href="https://hazelcast.com/"><img alt="Hazelcast" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hazelcast.svg" width="100"/>Hazelcast</a>
<a style="display:inline-block;" href="https://prometheus.io/"><img alt="Prometheus" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/prometheus.svg" width="100"/>Prometheus</a>
<a style="display:inline-block;" href="https://www.oracle.com/business-analytics/essbase.html"><img alt="OracleEssbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>OracleEssbase</a>
<a style="display:inline-block;" href="https://www.datastax.com/products/datastax-enterprise"><img alt="Datastax" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/datastax.svg" width="100"/>Datastax</a>
<a style="display:inline-block;" href="https://aerospike.com/"><img alt="Aerospike" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aerospike.webp" width="100"/>Aerospike</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/services/data-explorer/"><img alt="AzureDataExplorer" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/microsoft.png" width="100"/>AzureDataExplorer</a>
<a style="display:inline-block;" href="https://www.algolia.com/"><img alt="Algolia" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/algolia-mark-blue.svg" width="100"/>Algolia</a>
<a style="display:inline-block;" href="https://www.ehcache.org/"><img alt="Ehcache" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ehcache.png" width="100"/>Ehcache</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-derby"><img alt="Derby" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/derby.webp" width="100"/>Derby</a>
<a style="display:inline-block;" href="https://www.cockroachlabs.com/"><img alt="CockroachDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cockroach.avif" width="100"/>CockroachDB</a>
<a style="display:inline-block;" href="https://www.scylladb.com/"><img alt="ScyllaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/scylladb.svg" width="100"/>ScyllaDB</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/en-us/services/search/"><img alt="AzureSearch" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/microsoft.png" width="100"/>AzureSearch</a>
<a style="display:inline-block;" href="https://www.embarcadero.com/products/interbase"><img alt="Interbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/embarcadero.webp" width="100"/>Interbase</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/en-us/services/storage/tables/"><img alt="AzureTableStorage" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/microsoft.png" width="100"/>AzureTableStorage</a>
<a style="display:inline-block;" href="http://sphinxsearch.com/"><img alt="Sphinx" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sphinx.png" width="100"/>Sphinx</a>
<a style="display:inline-block;" href="https://jackrabbit.apache.org/"><img alt="Jackrabbit" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/jackrabbit.gif" width="100"/>Jackrabbit</a>
<a style="display:inline-block;" href="https://trino.io/"><img alt="Trino" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/trino.svg" width="100"/>Trino</a>
<a style="display:inline-block;" href="https://www.singlestore.com/"><img alt="SingleStore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/singlestore.svg" width="100"/>SingleStore</a>
<a style="display:inline-block;" href="https://www.actian.com/databases/ingres/"><img alt="Ingres" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ingres.png" width="100"/>Ingres</a>
<a style="display:inline-block;" href="https://virtuoso.openlinksw.com/"><img alt="Virtuoso" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/virtuoso.png" width="100"/>Virtuoso</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-timescale"><img alt="Timescale" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/timescale.svg" width="100"/>Timescale</a>
<a style="display:inline-block;" href="https://cloud.google.com/datastore/"><img alt="GoogleDatastore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/googlecloud.webp" width="100"/>GoogleDatastore</a>
<a style="display:inline-block;" href="https://github.com/graphite-project/graphite-web"><img alt="Graphite" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/graphiteweb.png" width="100"/>Graphite</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hsqldb"><img alt="HyperSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hypersql.png" width="100"/>HyperSQL</a>
<a style="display:inline-block;" href="https://www.softwareag.com/en_corporate/platform/adabas-natural.html"><img alt="Adabas" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/softwareag.svg" width="100"/>Adabas</a>
<a style="display:inline-block;" href=""><img alt="RiakKV" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/riak.png" width="100"/>RiakKV</a>
<a style="display:inline-block;" href="https://www.sap.com/products/technology-platform/sybase-iq-big-data-management.html"><img alt="SAPIQ" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sap.svg" width="100"/>SAPIQ</a>
<a style="display:inline-block;" href="https://www.arangodb.com/"><img alt="ArangoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/arangodb.png" width="100"/>ArangoDB</a>
<a style="display:inline-block;" href="https://jena.apache.org/documentation/tdb/index.html"><img alt="Jena" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/jena.png" width="100"/>Jena</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-ignite"><img alt="Ignite" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ignite.svg" width="100"/>Ignite</a>
<a style="display:inline-block;" href="https://cloud.google.com/bigtable/"><img alt="GoogleBigtable" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/googlecloud.webp" width="100"/>GoogleBigtable</a>
<a style="display:inline-block;" href="https://pingcap.com/"><img alt="TiDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tidb.svg" width="100"/>TiDB(PingCAP)</a>
<a style="display:inline-block;" href="https://accumulo.apache.org/"><img alt="Accumulo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/accumulo.png" width="100"/>Accumulo</a>
<a style="display:inline-block;" href="https://rocksdb.org/"><img alt="RocksDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rocksdb.svg" width="100"/>RocksDB</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/nosql/technologies/nosql/"><img alt="OracleNoSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>OracleNoSQL</a>
<a style="display:inline-block;" href="https://www.progress.com/openedge"><img alt="OpenEdge" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/progress.ico" width="100"/>OpenEdge</a>
<a style="display:inline-block;" href="https://duckdb.org/"><img alt="DuckDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/duckdb.png" width="100"/>DuckDB</a>
<a style="display:inline-block;" href="https://www.dolphindb.com/"><img alt="DolphinDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dolphindb.webp" width="100"/>DolphinDB</a>
<a style="display:inline-block;" href="https://www.vmware.com/products/gemfire.html"><img alt="GemFire" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vmware.webp" width="100"/>GemFire</a>
<a style="display:inline-block;" href="https://orientdb.org/"><img alt="OrientDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/orientdb.png" width="100"/>OrientDB</a>
<a style="display:inline-block;" href="https://cloud.google.com/spanner/"><img alt="GoogleSpanner" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/googlecloud.webp" width="100"/>GoogleSpanner</a>
<a style="display:inline-block;" href="https://ravendb.net/"><img alt="RavenDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ravendb.svg" width="100"/>RavenDB</a>
<a style="display:inline-block;" href="https://www.sap.com/products/technology-platform/sql-anywhere.html"><img alt="Anywhere" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sap.svg" width="100"/>Anywhere</a>
<a style="display:inline-block;" href="https://www.intersystems.com/products/cache/"><img alt="Cache" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/intersystems.svg" width="100"/>Cache</a>
<a style="display:inline-block;" href=""><img alt="ChinaMobileDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>ChinaMobileDB</a>
<a style="display:inline-block;" href=""><img alt="ChinaUnicomDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>ChinaUnicomDB</a>
<a style="display:inline-block;" href="https://www.cloudiip.com/"><img alt="CirroData" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cloudiip.png" width="100"/>CirroData</a>
<a style="display:inline-block;" href=""><img alt="FusionInsight" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/huawei.svg" width="100"/>FusionInsight</a>
<a style="display:inline-block;" href="https://cloud.baidu.com/doc/DRDS/index.html"><img alt="GaiaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/baiduyun.webp" width="100"/>GaiaDB</a>
<a style="display:inline-block;" href="https://support.huaweicloud.com/gaussdb/index.html"><img alt="GaussDB100" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/huawei.svg" width="100"/>GaussDB100</a>
<a style="display:inline-block;" href="https://support.huaweicloud.com/gaussdb/index.html"><img alt="GaussDB200" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/huawei.svg" width="100"/>GaussDB200</a>
<a style="display:inline-block;" href="https://www.zte.com.cn/china/solutions_latest/goldendb.html"><img alt="GoldenDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/zte.webp" width="100"/>GoldenDB</a>
<a style="display:inline-block;" href="https://www.greatdb.com/"><img alt="GreatDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/greatdb.png" width="100"/>GreatDB(北京万里开源软件有限公司)</a>
<a style="display:inline-block;" href="https://www.hashdata.xyz/"><img alt="HashData" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hashdata.png" width="100"/>HashData</a>
<a style="display:inline-block;" href="https://www.hotdb.com/index"><img alt="HotDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hotdb.png" width="100"/>HotDB</a>
<a style="display:inline-block;" href="https://infinispan.org/"><img alt="Infinispan" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/infinispan.png" width="100"/>Infinispan</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-kingbase"><img alt="KingBase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kingbase.png" width="100"/>KingBase(北京人大金仓信息技术股份有限公司)</a>
<a style="display:inline-block;" href="https://www.hundsun.com/"><img alt="LightDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hundsun.ico" width="100"/>LightDB</a>
<a style="display:inline-block;" href="https://www.mogdb.io/"><img alt="MogDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mogdb.png" width="100"/>MogDB(云和恩墨)</a>
<a style="display:inline-block;" href="https://www.murongtech.com/"><img alt="MuDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/murongtech.png" width="100"/>MuDB(沐融信息科技)</a>
<a style="display:inline-block;" href="https://www.boraydata.cn/"><img alt="RapidsDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/boraydata.png" width="100"/>RapidsDB</a>
<a style="display:inline-block;" href="https://www.selectdb.com/"><img alt="SelectDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/selectdb.png" width="100"/>SelectDB</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-sinodb"><img alt="SinoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sinoregal.png" width="100"/>SinoDB</a>
<a style="display:inline-block;" href=""><img alt="StarDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/stardb.webp" width="100"/>StarDB</a>
<a style="display:inline-block;" href=""><img alt="UbiSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/pingan.png" width="100"/>UbiSQL</a>
<a style="display:inline-block;" href="https://www.uxsino.com/"><img alt="UXDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/uxsino.png" width="100"/>UXDB(北京优炫软件股份有限公司)</a>
<a style="display:inline-block;" href="https://docs.vastdata.com.cn/zh/"><img alt="Vastbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vastdata.png" width="100"/>Vastbase(北京海量数据技术股份有限公司)</a>
<a style="display:inline-block;" href="http://www.vsettan.com.cn/7824.html"><img alt="xigemaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vsettan.png" width="100"/>xigemaDB</a>
<a style="display:inline-block;" href="https://dt.bestpay.com.cn/"><img alt="YiDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/bestpay.png" width="100"/>YiDB</a>
<a style="display:inline-block;" href="https://www.xugudb.com/"><img alt="xugu" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/xugu.png" width="100"/>xugu(成都虚谷伟业科技有限公司)</a>
<a style="display:inline-block;" href="https://maxdb.sap.com/"><img alt="MaxDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sap.svg" width="100"/>MaxDB</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/cloudant"><img alt="Cloudant" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ibm.svg" width="100"/>Cloudant</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/technologies/related/berkeleydb.html"><img alt="OracleBerkeley" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>OracleBerkeley</a>
<a style="display:inline-block;" href="https://www.yugabyte.com/"><img alt="YugabyteDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/yugabyte.svg" width="100"/>YugabyteDB</a>
<a style="display:inline-block;" href="https://github.com/google/leveldb"><img alt="LevelDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/leveldb.png" width="100"/>LevelDB</a>
<a style="display:inline-block;" href="https://www.pinecone.io/"><img alt="Pinecone" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/pinecone.ico" width="100"/>Pinecone</a>
<a style="display:inline-block;" href="https://github.com/heavyai/heavydb"><img alt="HEAVYAI" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/heavy.png" width="100"/>HEAVYAI</a>
<a style="display:inline-block;" href="https://memgraph.com/"><img alt="Memgraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/memgraph.webp" width="100"/>Memgraph</a>
<a style="display:inline-block;" href="https://developer.apple.com/icloud/cloudkit/"><img alt="CloudKit" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/appledev.svg" width="100"/>CloudKit</a>
<a style="display:inline-block;" href="https://rethinkdb.com/"><img alt="RethinkDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rethinkdb.png" width="100"/>RethinkDB</a>
<a style="display:inline-block;" href="https://www.exasol.com/"><img alt="EXASOL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/exasol.png" width="100"/>EXASOL</a>
<a style="display:inline-block;" href="https://drill.apache.org/"><img alt="Drill" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/drill.png" width="100"/>Drill</a>
<a style="display:inline-block;" href="https://pouchdb.com/"><img alt="PouchDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/pouchdb.svg" width="100"/>PouchDB</a>
<a style="display:inline-block;" href="https://phoenix.apache.org/"><img alt="Phoenix" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/phoenix.ico" width="100"/>Phoenix</a>
<a style="display:inline-block;" href="https://www.enterprisedb.com/"><img alt="EDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/edb.svg" width="100"/>EDB</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-tdengine"><img alt="TDengine" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tdengine.png" width="100"/>TDengine</a>
<a style="display:inline-block;" href="https://www.intersystems.com/products/intersystems-iris/"><img alt="IRIS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/intersystems.svg" width="100"/>IRIS</a>
<a style="display:inline-block;" href="https://oss.oetiker.ch/rrdtool/"><img alt="RRDtool" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rrdtool.png" width="100"/>RRDtool</a>
<a style="display:inline-block;" href="https://www.ontotext.com/"><img alt="GraphDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/graphdb.png" width="100"/>GraphDB</a>
<a style="display:inline-block;" href="https://www.citusdata.com/"><img alt="Citus" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cutisdata.svg" width="100"/>Citus</a>
<a style="display:inline-block;" href="https://www.coveo.com"><img alt="Coveo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/coveo.svg" width="100"/>Coveo</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/ims"><img alt="IMS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ibm.svg" width="100"/>IMS</a>
<a style="display:inline-block;" href="https://www.symas.com/symas-embedded-database-lmdb"><img alt="LMDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/symas.webp" width="100"/>LMDB</a>
<a style="display:inline-block;" href="https://github.com/vesoft-inc/nebula"><img alt="Nebula" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/nebula.ico" width="100"/>Nebula</a>
<a style="display:inline-block;" href="https://aws.amazon.com/neptune/"><img alt="AmazonNeptune" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonNeptune</a>
<a style="display:inline-block;" href="https://www.oracle.com/java/coherence/"><img alt="OracleCoherence" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>OracleCoherence</a>
<a style="display:inline-block;" href="https://geode.apache.org/"><img alt="Geode" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/geode.png" width="100"/>Geode</a>
<a style="display:inline-block;" href="https://aws.amazon.com/simpledb/"><img alt="AmazonSimpleDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonSimpleDB</a>
<a style="display:inline-block;" href="https://www.percona.com/software/mysql-database/percona-server"><img alt="PerconaMySQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/percona.svg" width="100"/>PerconaMySQL</a>
<a style="display:inline-block;" href="https://aws.amazon.com/cloudsearch/"><img alt="AmazonCloudSearch" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonCloudSearch</a>
<a style="display:inline-block;" href="https://www.stardog.com/"><img alt="Stardog" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/stardog.webp" width="100"/>Stardog</a>
<a style="display:inline-block;" href="https://www.firebolt.io/"><img alt="Firebolt" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/firebolt.svg" width="100"/>Firebolt</a>
<a style="display:inline-block;" href="https://www.datomic.com/"><img alt="Datomic" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/datomic.png" width="100"/>Datomic</a>
<a style="display:inline-block;" href="https://www.gaia-gis.it/fossil/libspatialite/index"><img alt="SpatiaLite" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/spatialite.png" width="100"/>SpatiaLite</a>
<a style="display:inline-block;" href="https://www.monetdb.org/"><img alt="MonetDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/monetdb.png" width="100"/>MonetDB</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-multivalue-application-development-platform/rocket-jbase"><img alt="jBASE" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rocket.svg" width="100"/>jBASE</a>
<a style="display:inline-block;" href="https://basex.org/"><img alt="BaseX" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/basex.png" width="100"/>BaseX</a>
<a style="display:inline-block;" href="https://www.trychroma.com/"><img alt="Chroma" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/chroma.png" width="100"/>Chroma</a>
<a style="display:inline-block;" href="http://www.empress.com/"><img alt="Empress" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/empress.gif" width="100"/>Empress</a>
<a style="display:inline-block;" href="https://aws.amazon.com/documentdb/"><img alt="AmazonDocumentDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonDocumentDB</a>
<a style="display:inline-block;" href="https://janusgraph.org/"><img alt="JanusGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/janusgraph.ico" width="100"/>JanusGraph</a>
<a style="display:inline-block;" href="http://erlang.org/doc/man/mnesia.html"><img alt="Mnesia" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/erlang.png" width="100"/>Mnesia</a>
<a style="display:inline-block;" href="https://www.tmaxsoft.com/products/tibero/"><img alt="Tibero" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tmaxsoft.png" width="100"/>Tibero</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-questdb"><img alt="QuestDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/questdb.svg" width="100"/>QuestDB</a>
<a style="display:inline-block;" href="https://griddb.net/"><img alt="GridDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/griddb.png" width="100"/>GridDB</a>
<a style="display:inline-block;" href="https://www.tigergraph.com/"><img alt="TigerGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tigergraph.svg" width="100"/>TigerGraph</a>
<a style="display:inline-block;" href="http://www.db4o.com/"><img alt="Db4o" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Db4o</a>
<a style="display:inline-block;" href="https://github.com/weaviate/weaviate"><img alt="Weaviate" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/weaviate.svg" width="100"/>Weaviate</a>
<a style="display:inline-block;" href="https://www.tarantool.io/"><img alt="Tarantool" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tarantool.svg" width="100"/>Tarantool</a>
<a style="display:inline-block;" href="https://www.gridgain.com/"><img alt="GridGain" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/gridgain.svg" width="100"/>GridGain</a>
<a style="display:inline-block;" href="https://dgraph.io/"><img alt="Dgraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dgraph.svg" width="100"/>Dgraph</a>
<a style="display:inline-block;" href="http://www.opentext.com/what-we-do/products/specialty-technologies/opentext-gupta-development-tools-databases/opentext-gupta-sqlbase"><img alt="SQLBase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/opentext.svg" width="100"/>SQLBase</a>
<a style="display:inline-block;" href="http://opentsdb.net/"><img alt="OpenTSDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/opentsdb.png" width="100"/>OpenTSDB</a>
<a style="display:inline-block;" href="https://sourceforge.net/projects/sedna/"><img alt="Sedna" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Sedna</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oceanbase"><img alt="OceanBase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oceanbase.webp" width="100"/>OceanBase</a>
<a style="display:inline-block;" href="https://fauna.com/"><img alt="Fauna" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/fauna.svg" width="100"/>Fauna</a>
<a style="display:inline-block;" href="https://www.datameer.com/"><img alt="Datameer" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/datameer.svg" width="100"/>Datameer</a>
<a style="display:inline-block;" href="https://planetscale.com/"><img alt="PlanetScale" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/planetscale.ico" width="100"/>PlanetScale</a>
<a style="display:inline-block;" href="https://www.actian.com/data-management/nosql-object-database/"><img alt="ActianNoSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/actian.png" width="100"/>ActianNoSQL</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/technologies/related/timesten.html"><img alt="TimesTen" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>TimesTen</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-voltdb"><img alt="VoltDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/voltdb.svg" width="100"/>VoltDB</a>
<a style="display:inline-block;" href="https://github.com/apple/foundationdb"><img alt="FoundationDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/appledev.svg" width="100"/>FoundationDB</a>
<a style="display:inline-block;" href="https://ignitetech.com/softwarelibrary/infobrightdb"><img alt="Infobright" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ignitetech.ico" width="100"/>Infobright</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/db2/warehouse"><img alt="Db2Warehouse" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ibm.svg" width="100"/>Db2Warehouse</a>
<a style="display:inline-block;" href="https://www.hpe.com/us/en/servers/nonstop.html"><img alt="NonStopSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/nonstop.svg" width="100"/>NonStopSQL</a>
<a style="display:inline-block;" href="https://ignitetech.com/objectstore/"><img alt="ObjectStore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ignitetech.ico" width="100"/>ObjectStore</a>
<a style="display:inline-block;" href="https://hughestech.com.au/products/msql/"><img alt="mSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>mSQL</a>
<a style="display:inline-block;" href="http://www.litedb.org/"><img alt="LiteDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/litedb.svg" width="100"/>LiteDB</a>
<a style="display:inline-block;" href="https://milvus.io/"><img alt="Milvus" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/milvus.svg" width="100"/>Milvus</a>
<a style="display:inline-block;" href="http://www.dataease.com/"><img alt="DataEase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dataease.png" width="100"/>DataEase</a>
<a style="display:inline-block;" href="https://cubrid.com"><img alt="Cubrid" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cubrid.png" width="100"/>Cubrid</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-d3"><img alt="D3" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rocket.svg" width="100"/>D3</a>
<a style="display:inline-block;" href="https://victoriametrics.com/"><img alt="VictoriaMetrics" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/victoriametrics.png" width="100"/>VictoriaMetrics</a>
<a style="display:inline-block;" href="https://kylin.apache.org/"><img alt="kylin" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kylin.png" width="100"/>kylin</a>
<a style="display:inline-block;" href="https://giraph.apache.org/"><img alt="Giraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Giraph</a>
<a style="display:inline-block;" href="https://sourceforge.net/projects/fis-gtm/"><img alt="GTM" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>GTM</a>
<a style="display:inline-block;" href="https://objectbox.io/"><img alt="ObjectBox" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/objectbox.png" width="100"/>ObjectBox</a>
<a style="display:inline-block;" href="https://windev.com/pcsoft/hfsql.htm"><img alt="HFSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hfsql.png" width="100"/>HFSQL</a>
<a style="display:inline-block;" href="https://github.com/meilisearch/meilisearch"><img alt="Meilisearch" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/meilisearch.svg" width="100"/>Meilisearch</a>
<a style="display:inline-block;" href="https://www.matrixorigin.io/"><img alt="MatrixOne" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/matrixone.svg" width="100"/>MatrixOne</a>
<a style="display:inline-block;" href="https://www.mcobject.com/perst/"><img alt="Perst" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mcobject.png" width="100"/>Perst</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/technologies/related/rdb.html"><img alt="OracleRdb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oracle.avif" width="100"/>OracleRdb</a>
<a style="display:inline-block;" href="https://www.gigaspaces.com"><img alt="GigaSpaces" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/gigaspaces.png" width="100"/>GigaSpaces</a>
<a style="display:inline-block;" href="https://vitess.io/"><img alt="Vitess" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vitess.png" width="100"/>Vitess</a>
<a style="display:inline-block;" href="https://reality.necsws.com/"><img alt="Reality" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/necsws.jpg" width="100"/>Reality</a>
<a style="display:inline-block;" href="https://sql.js.org/"><img alt="SQLJS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sqljs.png" width="100"/>SQLJS</a>
<a style="display:inline-block;" href="https://www.hpe.com/us/en/software/data-fabric.html"><img alt="Ezmeral" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/nonstop.svg" width="100"/>Ezmeral</a>
<a style="display:inline-block;" href="https://allegrograph.com/"><img alt="AllegroGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/allegrograph.png" width="100"/>AllegroGraph</a>
<a style="display:inline-block;" href="https://m3db.io/"><img alt="M3DB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/m3db.svg" width="100"/>M3DB</a>
<a style="display:inline-block;" href="http://hawq.apache.org/"><img alt="HAWQ" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hawq.png" width="100"/>HAWQ</a>
<a style="display:inline-block;" href="https://www.starrocks.io/"><img alt="StarRocks" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/starrocks.webp" width="100"/>StarRocks</a>
<a style="display:inline-block;" href="https://teamblue.unicomsi.com/products/soliddb/"><img alt="solidDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/unicomsi.png" width="100"/>solidDB</a>
<a style="display:inline-block;" href="https://www.3ds.com/nuodb-distributed-sql-database/"><img alt="NuoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/nuodb.png" width="100"/>NuoDB</a>
<a style="display:inline-block;" href="https://www.alachisoft.com/ncache/"><img alt="NCache" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ncache.svg" width="100"/>NCache</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-opengauss"><img alt="OpenGauss" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/opengauss.png" width="100"/>OpenGauss</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-iotdb"><img alt="IoTDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/iotdb.webp" width="100"/>IoTDB</a>
<a style="display:inline-block;" href="https://github.com/qdrant/qdrant"><img alt="Qdrant" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/qdrant.svg" width="100"/>Qdrant</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-m204"><img alt="Model204" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rocket.svg" width="100"/>Model204</a>
<a style="display:inline-block;" href="https://zodb.org/"><img alt="ZODB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/zodb.png" width="100"/>ZODB</a>
<a style="display:inline-block;" href="https://www.bigchaindb.com/"><img alt="BigchainDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/bigchaindb.png" width="100"/>BigchainDB</a>
<a style="display:inline-block;" href="https://surrealdb.com/"><img alt="SurrealDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/surrealdb.svg" width="100"/>SurrealDB</a>
<a style="display:inline-block;" href="https://xapian.org/"><img alt="Xapian" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/xapian.png" width="100"/>Xapian</a>
<a style="display:inline-block;" href="https://www.elevatesoft.com/products?category=dbisam"><img alt="DBISAM" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dbisam.png" width="100"/>DBISAM</a>
<a style="display:inline-block;" href="https://www.actian.com/analytic-database/vector-analytic-database/"><img alt="ActianVector" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/actian.png" width="100"/>ActianVector</a>
<a style="display:inline-block;" href="https://github.com/hibari/hibari"><img alt="Hibari" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Hibari</a>
<a style="display:inline-block;" href="https://github.com/dolthub/dolt"><img alt="Dolt" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dolt.png" width="100"/>Dolt</a>
<a style="display:inline-block;" href="https://typedb.com/"><img alt="TypeDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/typedb.svg" width="100"/>TypeDB</a>
<a style="display:inline-block;" href="http://altibase.com/"><img alt="Altibase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/altibase.png" width="100"/>Altibase</a>
<a style="display:inline-block;" href="https://aws.amazon.com/timestream/"><img alt="AmazonTimestream" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonTimestream</a>
<a style="display:inline-block;" href="https://www.objectdb.com/"><img alt="ObjectDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/objectdb.ico" width="100"/>ObjectDB</a>
<a style="display:inline-block;" href="https://blazegraph.com/"><img alt="Blazegraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/blazegraph.png" width="100"/>Blazegraph</a>
<a style="display:inline-block;" href="https://aws.amazon.com/keyspaces/"><img alt="AmazonKeyspaces" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/amazon.webp" width="100"/>AmazonKeyspaces</a>
<a style="display:inline-block;" href="https://www.tencentcloud.com/products/dcdb"><img alt="TDSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tencentcloud.ico" width="100"/>TDSQL(腾讯云计算（北京）有限责任公司)</a>
<a style="display:inline-block;" href="https://www.ca.com/us/products/ca-idms.html"><img alt="IDMS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/broadcom.png" width="100"/>IDMS</a>
<a style="display:inline-block;" href="https://rdf4j.org/"><img alt="RDF4J" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rdf4j.png" width="100"/>RDF4J</a>
<a style="display:inline-block;" href="https://www.geomesa.org/"><img alt="GeoMesa" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/geomesa.png" width="100"/>GeoMesa</a>
<a style="display:inline-block;" href="http://exist-db.org/"><img alt="eXistdb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/existdb.gif" width="100"/>eXistdb</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/ibm-websphere-extreme-scale"><img alt="eXtremeScale" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/extremescale.svg" width="100"/>eXtremeScale</a>
<a style="display:inline-block;" href="https://rockset.com/"><img alt="Rockset" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rockset.svg" width="100"/>Rockset</a>
<a style="display:inline-block;" href="https://yellowbrick.com/"><img alt="Yellowbrick" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/yellowbrick.svg" width="100"/>Yellowbrick</a>
<a style="display:inline-block;" href="https://sqream.com/"><img alt="SQream" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sqream.svg" width="100"/>SQream</a>
<a style="display:inline-block;" href="https://www.broadcom.com/products/mainframe/databases-database-mgmt/datacom"><img alt="DatacomDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/broadcom.png" width="100"/>DatacomDB</a>
<a style="display:inline-block;" href="https://typesense.org/"><img alt="Typesense" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/typesense.svg" width="100"/>Typesense</a>
<a style="display:inline-block;" href="https://mapdb.org/"><img alt="MapDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mapdb.png" width="100"/>MapDB</a>
<a style="display:inline-block;" href="https://objectivity.com/products/objectivitydb/"><img alt="ObjectivityDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/objectivity.png" width="100"/>ObjectivityDB</a>
<a style="display:inline-block;" href="https://cratedb.com/?utm_campaign=2023-Q1-WS-DB-Engines&utm_source=db-engines.com"><img alt="CrateDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cratedb.svg" width="100"/>CrateDB</a>
<a style="display:inline-block;" href="https://www.mcobject.com"><img alt="eXtreme" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/extremedb.webp" width="100"/>eXtreme</a>
<a style="display:inline-block;" href="https://paradigm4.com/"><img alt="SciDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/paradigm4.svg" width="100"/>SciDB</a>
<a style="display:inline-block;" href="http://alasql.org"><img alt="AlaSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/alasql.png" width="100"/>AlaSQL</a>
<a style="display:inline-block;" href="https://github.com/kairosdb/kairosdb"><img alt="KairosDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kairosdb.png" width="100"/>KairosDB</a>
<a style="display:inline-block;" href="https://www.kinetica.com/"><img alt="Kinetica" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kinetica.png" width="100"/>Kinetica</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/maxcompute"><img alt="MaxCompute" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aliyun.ico" width="100"/>MaxCompute</a>
<a style="display:inline-block;" href="https://github.com/Snapchat/KeyDB"><img alt="KeyDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/keydb.svg" width="100"/>KeyDB</a>
<a style="display:inline-block;" href="https://www.revelation.com/index.php/products/openinsight"><img alt="OpenInsight" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/openinsight.png" width="100"/>OpenInsight</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/analyticdb-for-mysql"><img alt="AnalyticDBMySQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aliyun.ico" width="100"/>AnalyticDBMySQL</a>
<a style="display:inline-block;" href="https://gemtalksystems.com/"><img alt="GemStoneS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/GemTalkLogo.png" width="100"/>GemStoneS</a>
<a style="display:inline-block;" href="https://vald.vdaas.org/"><img alt="Vald" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vald.svg" width="100"/>Vald</a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-doris"><img alt="Doris" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/doris.svg" width="100"/>Doris</a>
<a style="display:inline-block;" href="https://www.devgraph.com/scalearc/"><img alt="ScaleArc" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/scalearc.svg" width="100"/>ScaleArc</a>
<a style="display:inline-block;" href="https://www.risingwave.com/database/"><img alt="RisingWave" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/risingwave.svg" width="100"/>RisingWave</a>
<a style="display:inline-block;" href="http://www.frontbase.com/"><img alt="FrontBase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/frontbase.gif" width="100"/>FrontBase</a>
<a style="display:inline-block;" href="https://www.postgres-xl.org/"><img alt="PostgresXL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/pgxl.jpg" width="100"/>PostgresXL</a>
<a style="display:inline-block;" href="https://pinot.apache.org/"><img alt="Pinot" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/apachepoint.png" width="100"/>Pinot</a>
<a style="display:inline-block;" href="https://spotify.github.io/heroic/#!/index"><img alt="Heroic" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/heroic.png" width="100"/>Heroic</a>
<a style="display:inline-block;" href="https://vistadb.com/"><img alt="VistaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vistadb.png" width="100"/>VistaDB</a>
<a style="display:inline-block;" href="http://scalaris.zib.de/"><img alt="Scalaris" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/scalaris.png" width="100"/>Scalaris</a>
<a style="display:inline-block;" href="https://www.nexusdb.com/"><img alt="NexusDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/nexusdb.gif" width="100"/>NexusDB</a>
<a style="display:inline-block;" href="https://www.percona.com/mongodb/software/percona-server-for-mongodb"><img alt="PerconaMongoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/percona.svg" width="100"/>PerconaMongoDB</a>
<a style="display:inline-block;" href="https://www.graphengine.io/"><img alt="GraphEngine" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/graphengine.ico" width="100"/>GraphEngine</a>
<a style="display:inline-block;" href="https://github.com/boltdb/bolt"><img alt="BoltDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>BoltDB</a>
<a style="display:inline-block;" href="https://atoti.io/"><img alt="atoti" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/atoti.svg" width="100"/>atoti</a>
<a style="display:inline-block;" href="https://vespa.ai/"><img alt="Vespa" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/vespa.png" width="100"/>Vespa</a>
<a style="display:inline-block;" href="https://github.com/techfort/LokiJS"><img alt="LokiJS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>LokiJS</a>
<a style="display:inline-block;" href="https://raima.com/"><img alt="Raima" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/raima.png" width="100"/>Raima</a>
<a style="display:inline-block;" href="https://databend.rs"><img alt="Databend" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/databend.svg" width="100"/>Databend</a>
<a style="display:inline-block;" href="https://www.rbase.com/"><img alt="RBASE" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rbase.png" width="100"/>RBASE</a>
<a style="display:inline-block;" href="https://librdf.org/"><img alt="Redland" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/librdf.ico" width="100"/>Redland</a>
<a style="display:inline-block;" href="https://harperdb.io/"><img alt="HarperDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/harper.webp" width="100"/>HarperDB</a>
<a style="display:inline-block;" href="https://splicemachine.com/"><img alt="SpliceMachine" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>SpliceMachine</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/hybriddb-postgresql"><img alt="AnalyticDBPostgreSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aliyun.ico" width="100"/>AnalyticDBPostgreSQL</a>
<a style="display:inline-block;" href="https://modeshape.jboss.org/"><img alt="ModeShape" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/modeshape.ico" width="100"/>ModeShape</a>
<a style="display:inline-block;" href="https://strabon.di.uoa.gr/"><img alt="Strabon" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/strabon.png" width="100"/>Strabon</a>
<a style="display:inline-block;" href="https://www.jadeworld.com/developer-center"><img alt="Jade" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/jade.svg" width="100"/>Jade</a>
<a style="display:inline-block;" href="http://www.sequoiadb.com/"><img alt="Sequoiadb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sequoiadb.png" width="100"/>Sequoiadb</a>
<a style="display:inline-block;" href="https://github.com/cnosdb/cnosdb"><img alt="CnosDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cnosdb.png" width="100"/>CnosDB</a>
<a style="display:inline-block;" href="https://www.ittia.com/"><img alt="ITTIA" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ittia.png" width="100"/>ITTIA</a>
<a style="display:inline-block;" href="https://github.com/reverbrain/elliptics"><img alt="Elliptics" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Elliptics</a>
<a style="display:inline-block;" href="https://www.elassandra.io/"><img alt="Elassandra" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Elassandra</a>
<a style="display:inline-block;" href="http://www.rasdaman.org/"><img alt="Rasdaman" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rasdaman.png" width="100"/>Rasdaman</a>
<a style="display:inline-block;" href="https://www.searchblox.com/"><img alt="SearchBlox" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/searchblox.webp" width="100"/>SearchBlox</a>
<a style="display:inline-block;" href="https://objectivity.com/infinitegraph/"><img alt="InfiniteGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/infinitegraph.png" width="100"/>InfiniteGraph</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/polardb"><img alt="ApsaraDBPolarDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aliyun.ico" width="100"/>ApsaraDBPolarDB</a>
<a style="display:inline-block;" href="https://starcounter.com/"><img alt="Starcounter" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/starcounter.svg" width="100"/>Starcounter</a>
<a style="display:inline-block;" href="https://axibase.com/docs/atsd/finance/"><img alt="Axibase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/axibase.png" width="100"/>Axibase</a>
<a style="display:inline-block;" href="https://kyligence.io/kyligence-enterprise/"><img alt="Kyligence" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kyligence.png" width="100"/>Kyligence</a>
<a style="display:inline-block;" href="https://www.featurebase.com/"><img alt="FeatureBase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/featurebase.png" width="100"/>FeatureBase</a>
<a style="display:inline-block;" href="https://google.github.io/lovefield/"><img alt="Lovefield" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/lovefield.png" width="100"/>Lovefield</a>
<a style="display:inline-block;" href="http://www.project-voldemort.com/"><img alt="Voldemort" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/voldemort.png" width="100"/>Voldemort</a>
<a style="display:inline-block;" href="https://brytlyt.io/"><img alt="Brytlyt" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/brytlyt.png" width="100"/>Brytlyt</a>
<a style="display:inline-block;" href="https://www.machbase.com/"><img alt="MachbaseNeo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/machbase.png" width="100"/>MachbaseNeo</a>
<a style="display:inline-block;" href="https://esd.actian.com/product/Versant_FastObjects/"><img alt="ActianFastObjects" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/actian.png" width="100"/>ActianFastObjects</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-multivalue-application-development-platform/rocket-open-qm"><img alt="OpenQM" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rocket.svg" width="100"/>OpenQM</a>
<a style="display:inline-block;" href="https://www.oxfordsemantic.tech/"><img alt="RDFox" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rdfox.svg" width="100"/>RDFox</a>
<a style="display:inline-block;" href="https://cambridgesemantics.com/anzograph/"><img alt="AnzoGraph_DB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cambridgesemantics.svg" width="100"/>AnzoGraph_DB</a>
<a style="display:inline-block;" href="https://flur.ee/"><img alt="Fluree" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/fluee.png" width="100"/>Fluree</a>
<a style="display:inline-block;" href="https://immudb.io/"><img alt="Immudb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/immudb.svg" width="100"/>Immudb</a>
<a style="display:inline-block;" href="https://www.mimer.com/"><img alt="Mimer_SQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mimer.png" width="100"/>Mimer_SQL</a>
<a style="display:inline-block;" href="https://github.com/ydb-platform/ydb"><img alt="YDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ydb.svg" width="100"/>YDB</a>
<a style="display:inline-block;" href="https://www.aelius.com/njh/redstore/"><img alt="RedStore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/redstore.png" width="100"/>RedStore</a>
<a style="display:inline-block;" href="http://www.hypergraphdb.org/"><img alt="HyperGraphDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hypergraphdb.jpg" width="100"/>HyperGraphDB</a>
<a style="display:inline-block;" href="https://github.com/marqo-ai/marqo"><img alt="Marqo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/marqo.png" width="100"/>Marqo</a>
<a style="display:inline-block;" href="https://github.com/Softmotions/ejdb"><img alt="EJDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ejdb.png" width="100"/>EJDB</a>
<a style="display:inline-block;" href="https://tajo.apache.org/"><img alt="Tajo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tajo.png" width="100"/>Tajo</a>
<a style="display:inline-block;" href="https://github.com/activeloopai/deeplake"><img alt="DeepLake" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/deeplake.png" width="100"/>DeepLake</a>
<a style="display:inline-block;" href="https://www.asiainfo.com/en_us/product_aisware_antdb_detail.html"><img alt="AntDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/antdb.png" width="100"/>AntDB</a>
<a style="display:inline-block;" href="https://www.leanxcale.com/"><img alt="LeanXcale" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/leanxcale.png" width="100"/>LeanXcale</a>
<a style="display:inline-block;" href="http://www.mulgara.org/"><img alt="Mulgara" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/mulgara.jpg" width="100"/>Mulgara</a>
<a style="display:inline-block;" href="https://fast.fujitsu.com"><img alt="Fujitsu" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/fujitsu.svg" width="100"/>Fujitsu</a>
<a style="display:inline-block;" href="https://github.com/twitter-archive/flockdb"><img alt="FlockDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>FlockDB</a>
<a style="display:inline-block;" href="https://github.com/STSSoft/STSdb4"><img alt="STSdb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/stsdb.png" width="100"/>STSdb</a>
<a style="display:inline-block;" href="https://www.openpie.com/"><img alt="PieCloudDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/openpie.svg" width="100"/>PieCloudDB</a>
<a style="display:inline-block;" href="https://www.transaction.de/en/products/transbase.html"><img alt="Transbase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/transaction.png" width="100"/>Transbase</a>
<a style="display:inline-block;" href="https://www.elevatesoft.com/products?category=edb"><img alt="ElevateDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/elevatedb.png" width="100"/>ElevateDB</a>
<a style="display:inline-block;" href=""><img alt="RiakTS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/riak.png" width="100"/>RiakTS</a>
<a style="display:inline-block;" href="https://www.faircom.com/products/faircom-db"><img alt="FaircomDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/faircom.svg" width="100"/>FaircomDB</a>
<a style="display:inline-block;" href="http://neventstore.org/"><img alt="NEventStore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>NEventStore</a>
<a style="display:inline-block;" href="https://github.com/bloomberg/comdb2"><img alt="Comdb2" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Comdb2</a>
<a style="display:inline-block;" href="https://yottadb.com/"><img alt="YottaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/yottadb.svg" width="100"/>YottaDB</a>
<a style="display:inline-block;" href="https://quasar.ai/"><img alt="Quasardb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/quasar.svg" width="100"/>Quasardb</a>
<a style="display:inline-block;" href="https://www.speedb.io/"><img alt="Speedb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/speedb.svg" width="100"/>Speedb</a>
<a style="display:inline-block;" href="http://www.esgyn.cn/"><img alt="EsgynDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/esgyn.jpg" width="100"/>EsgynDB</a>
<a style="display:inline-block;" href="https://community.tibco.com/products/tibco-computedb"><img alt="ComputeDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tibco.ico" width="100"/>ComputeDB</a>
<a style="display:inline-block;" href="https://hugegraph.apache.org/"><img alt="HugeGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hugegraph.png" width="100"/>HugeGraph</a>
<a style="display:inline-block;" href="https://www.valentina-db.net/"><img alt="Valentina" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/valentina.png" width="100"/>Valentina</a>
<a style="display:inline-block;" href="https://github.com/pipelinedb/pipelinedb"><img alt="PipelineDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/pipelinedb.png" width="100"/>PipelineDB</a>
<a style="display:inline-block;" href="https://bangdb.com/"><img alt="Bangdb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/bangdb.png" width="100"/>Bangdb</a>
<a style="display:inline-block;" href="https://dydra.com/about"><img alt="Dydra" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dydra.png" width="100"/>Dydra</a>
<a style="display:inline-block;" href="https://tinkerpop.apache.org/docs/current/reference/#tinkergraph-gremlin"><img alt="TinkerGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/apache.ico" width="100"/>TinkerGraph</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/db2-event-store"><img alt="EventStore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/eventstore.svg" width="100"/>EventStore</a>
<a style="display:inline-block;" href="https://www.ultipa.com/"><img alt="Ultipa" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ultipa.svg" width="100"/>Ultipa</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/table-store"><img alt="Table_Store" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aliyun.ico" width="100"/>Table_Store</a>
<a style="display:inline-block;" href="https://www.actian.com/data-management/psql-embedded-database/"><img alt="ActianPSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/actian.png" width="100"/>ActianPSQL</a>
<a style="display:inline-block;" href="https://www.cubicweb.org/"><img alt="CubicWeb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cubicweb.svg" width="100"/>CubicWeb</a>
<a style="display:inline-block;" href="https://www.exorbyte.com/"><img alt="Exorbyte" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/exorbyte.png" width="100"/>Exorbyte</a>
<a style="display:inline-block;" href="https://graphbase.ai/"><img alt="GraphBase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/graphbase.png" width="100"/>GraphBase</a>
<a style="display:inline-block;" href="https://fallabs.com/tokyotyrant/"><img alt="TokyoTyrant" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/fallabs.webp" width="100"/>TokyoTyrant</a>
<a style="display:inline-block;" href="https://github.com/skytable/skytable"><img alt="Skytable" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/skytable.png" width="100"/>Skytable</a>
<a style="display:inline-block;" href="https://terminusdb.com/"><img alt="TerminusDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/terminusdb.svg" width="100"/>TerminusDB</a>
<a style="display:inline-block;" href="https://github.com/dgraph-io/badger"><img alt="Badger" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Badger</a>
<a style="display:inline-block;" href="https://greptime.com/"><img alt="GreptimeDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/greptime.png" width="100"/>GreptimeDB</a>
<a style="display:inline-block;" href="http://www.translattice.com/"><img alt="TransLattice" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/translattice.png" width="100"/>TransLattice</a>
<a style="display:inline-block;" href="https://arcadedb.com/"><img alt="ArcadeDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/arcadedb.png" width="100"/>ArcadeDB</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/product/kundb"><img alt="KunDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/kundb.png" width="100"/>KunDB</a>
<a style="display:inline-block;" href="https://www.sparsity-technologies.com/"><img alt="Sparksee" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sparsity.png" width="100"/>Sparksee</a>
<a style="display:inline-block;" href="https://myscale.com/"><img alt="MyScale" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/myscale.ico" width="100"/>MyScale</a>
<a style="display:inline-block;" href="https://bigobject.io/"><img alt="BigObject" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/bigobject.svg" width="100"/>BigObject</a>
<a style="display:inline-block;" href="https://linter.ru/"><img alt="Linter" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/linter.svg" width="100"/>Linter</a>
<a style="display:inline-block;" href="https://manticoresearch.com"><img alt="ManticoreSearch" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/manticoresearch.svg" width="100"/>ManticoreSearch</a>
<a style="display:inline-block;" href="https://github.com/dragonflydb/dragonfly"><img alt="Dragonfly" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/dragonflydb.svg" width="100"/>Dragonfly</a>
<a style="display:inline-block;" href="https://www.tigrisdata.com/"><img alt="Tigris" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tigris.svg" width="100"/>Tigris</a>
<a style="display:inline-block;" href="http://www.h2gis.org/"><img alt="H2GIS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/h2gis.png" width="100"/>H2GIS</a>
<a style="display:inline-block;" href="https://velocitydb.com/"><img alt="VelocityDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/velocitydb.png" width="100"/>VelocityDB</a>
<a style="display:inline-block;" href="https://www.nuget.org/packages/EloqueraDB/"><img alt="Eloquera" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/eloquera.png" width="100"/>Eloquera</a>
<a style="display:inline-block;" href="https://github.com/rescrv/HyperLevelDB"><img alt="HyperLevelDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>HyperLevelDB</a>
<a style="display:inline-block;" href="https://github.com/xtdb/xtdb"><img alt="XTDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/xtdb.svg" width="100"/>XTDB</a>
<a style="display:inline-block;" href="http://blueflood.io/"><img alt="Blueflood" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/blueflood.png" width="100"/>Blueflood</a>
<a style="display:inline-block;" href="https://senseidb.github.io/sensei/"><img alt="SenseiDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>SenseiDB</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/hitsdb"><img alt="TSDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/aliyun.ico" width="100"/>TSDB</a>
<a style="display:inline-block;" href="https://github.com/krareT/trkdb"><img alt="TerarkDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>TerarkDB</a>
<a style="display:inline-block;" href="https://origodb.com/"><img alt="OrigoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/origodb.ico" width="100"/>OrigoDB</a>
<a style="display:inline-block;" href="https://tomp2p.net/"><img alt="TomP2P" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tomp2p.png" width="100"/>TomP2P</a>
<a style="display:inline-block;" href="https://www.xtremedata.com/"><img alt="XtremeData" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/xtremedata.svg" width="100"/>XtremeData</a>
<a style="display:inline-block;" href="https://www.siaqodb.com/"><img alt="Siaqodb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Siaqodb</a>
<a style="display:inline-block;" href="https://ytsaurus.tech/"><img alt="YTsaurus" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ytsaurus.svg" width="100"/>YTsaurus</a>
<a style="display:inline-block;" href="https://www.warp10.io/"><img alt="Warp" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/warp10.svg" width="100"/>Warp</a>
<a style="display:inline-block;" href="http://www.opengemini.org/"><img alt="openGemini" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/opengemini.svg" width="100"/>openGemini</a>
<a style="display:inline-block;" href="https://upscaledb.com/"><img alt="Upscaledb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Upscaledb</a>
<a style="display:inline-block;" href="https://en.gstore.cn/"><img alt="gStore" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/gstore.png" width="100"/>gStore</a>
<a style="display:inline-block;" href="http://www.oushu.com/product/oushuDB"><img alt="OushuDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/oushu.svg" width="100"/>OushuDB</a>
<a style="display:inline-block;" href="https://indica.nl/"><img alt="Indica" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/indica.jpg" width="100"/>Indica</a>
<a style="display:inline-block;" href="https://brightstardb.com/"><img alt="BrightstarDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/brightstardb.png" width="100"/>BrightstarDB</a>
<a style="display:inline-block;" href="https://boilerbay.com/"><img alt="InfinityDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/boilerbay.jpg" width="100"/>InfinityDB</a>
<a style="display:inline-block;" href="https://www.alachisoft.com/nosdb/"><img alt="NosDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ncache.svg" width="100"/>NosDB</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/subproduct/hippo"><img alt="Hippo" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/transwarp.svg" width="100"/>Hippo</a>
<a style="display:inline-block;" href="https://github.com/appy-one/acebase"><img alt="Acebase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Acebase</a>
<a style="display:inline-block;" href="https://siridb.com/"><img alt="SiriDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sisridb.svg" width="100"/>SiriDB</a>
<a style="display:inline-block;" href="https://sitewhere.io/"><img alt="SiteWhere" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sitewhere.svg" width="100"/>SiteWhere</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/product/argodb"><img alt="ArgoDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/transwarp.ico" width="100"/>ArgoDB</a>
<a style="display:inline-block;" href="https://nsdb.io/"><img alt="NSDb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/nsdb.png" width="100"/>NSDb</a>
<a style="display:inline-block;" href="http://www.datajaguar.com"><img alt="JaguarDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/jaguardb.png" width="100"/>JaguarDB</a>
<a style="display:inline-block;" href="http://wakanda.github.io/"><img alt="WakandaDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/wakanda.png" width="100"/>WakandaDB</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/product/stellardb"><img alt="StellarDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/transwarp.svg" width="100"/>StellarDB</a>
<a style="display:inline-block;" href="https://galaxybase.com/"><img alt="Galaxybase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/galaxybase.png" width="100"/>Galaxybase</a>
<a style="display:inline-block;" href="https://newdatabase.com/"><img alt="DataFS" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/datafs.png" width="100"/>DataFS</a>
<a style="display:inline-block;" href="https://www.sadasengine.com/"><img alt="SadasEngine" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sadasengine.png" width="100"/>SadasEngine</a>
<a style="display:inline-block;" href="https://www.hawkular.org/"><img alt="Hawkular" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/hawkular.svg" width="100"/>Hawkular</a>
<a style="display:inline-block;" href="https://bitnine.net/"><img alt="AgensGraph" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/agens.png" width="100"/>AgensGraph</a>
<a style="display:inline-block;" href="https://www.faircom.com/products/faircomedge-iot-database"><img alt="FaircomEDGE" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/faircom.svg" width="100"/>FaircomEDGE</a>
<a style="display:inline-block;" href="https://cachelot.io/"><img alt="Cachelot" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cacheiot.png" width="100"/>Cachelot</a>
<a style="display:inline-block;" href="https://www.iboxdb.com/"><img alt="iBoxDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/iboxdb.png" width="100"/>iBoxDB</a>
<a style="display:inline-block;" href="https://www.scaleoutsoftware.com/products/stateserver/"><img alt="StateServer" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/scaleout.svg" width="100"/>StateServer</a>
<a style="display:inline-block;" href="https://dbmx.net/tkrzw/"><img alt="Tkrzw" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/tkrzw.png" width="100"/>Tkrzw</a>
<a style="display:inline-block;" href="https://github.com/kashirin-alex/swc-db"><img alt="SWCDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>SWCDB</a>
<a style="display:inline-block;" href="https://ledisdb.io/"><img alt="LedisDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/ledisdb.png" width="100"/>LedisDB</a>
<a style="display:inline-block;" href="https://swaydb.simer.au/"><img alt="SwayDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/swaydb.png" width="100"/>SwayDB</a>
<a style="display:inline-block;" href="http://opennms.github.io/newts/"><img alt="Newts" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/newts.png" width="100"/>Newts</a>
<a style="display:inline-block;" href="http://www.actordb.com/"><img alt="ActorDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/actordb.png" width="100"/>ActorDB</a>
<a style="display:inline-block;" href="https://www.edgeintelligence.com/"><img alt="Intelligence" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/edgeintelligence.svg" width="100"/>Intelligence</a>
<a style="display:inline-block;" href="http://www.smallsql.de/"><img alt="SmallSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/smallsql.png" width="100"/>SmallSQL</a>
<a style="display:inline-block;" href="https://www.mireo.com/spacetime"><img alt="SpaceTime" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/spacetime.svg" width="100"/>SpaceTime</a>
<a style="display:inline-block;" href="http://sparkledb.com/"><img alt="SparkleDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/sparkledb.png" width="100"/>SparkleDB</a>
<a style="display:inline-block;" href="https://caucho.com/"><img alt="ResinCache" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/caucho.png" width="100"/>ResinCache</a>
<a style="display:inline-block;" href="https://jethro.io/"><img alt="JethroData" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/jethro.png" width="100"/>JethroData</a>
<a style="display:inline-block;" href="http://bergdb.com/"><img alt="BergDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>BergDB</a>
<a style="display:inline-block;" href="https://www.cortex-ag.com/"><img alt="CortexDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cortex.svg" width="100"/>CortexDB</a>
<a style="display:inline-block;" href="https://covenantsql.io/"><img alt="CovenantSQL" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/covenantsql.svg" width="100"/>CovenantSQL</a>
<a style="display:inline-block;" href="https://www.daggerdb.com/"><img alt="DaggerDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>DaggerDB</a>
<a style="display:inline-block;" href="https://github.com/edgelesssys/edgelessdb"><img alt="EdgelessDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/edgelessdb.svg" width="100"/>EdgelessDB</a>
<a style="display:inline-block;" href="http://www.levyx.com/helium"><img alt="Helium" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/levyx.png" width="100"/>Helium</a>
<a style="display:inline-block;" href="https://github.com/rayokota/hgraphdb"><img alt="HGraphDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>HGraphDB</a>
<a style="display:inline-block;" href="http://www.oberasoftware.com/jasdb/"><img alt="JasDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/jasdb.png" width="100"/>JasDB</a>
<a style="display:inline-block;" href="https://github.com/mgholam/RaptorDB-Document"><img alt="RaptorDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>RaptorDB</a>
<a style="display:inline-block;" href="https://www.rizhiyi.com/"><img alt="Rizhiyi" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/rizhiyi.ico" width="100"/>Rizhiyi</a>
<a style="display:inline-block;" href="http://www.searchxml.net/category/products/"><img alt="searchxml" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/searchxml.png" width="100"/>searchxml</a>
<a style="display:inline-block;" href="https://github.com/dgraph-io/badger"><img alt="BadgerDB" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/badgerdb.webp" width="100"/>BadgerDB</a>
<a style="display:inline-block;" href="https://github.com/cayleygraph/cayley"><img alt="Cayley" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/cayley.webp" width="100"/>Cayley</a>
<a style="display:inline-block;" href="https://crase.sourceforge.net/"><img alt="Crase" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/" width="100"/>Crase</a>
<a style="display:inline-block;" href=""><img alt="CrispI" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/crispI.jpg" width="100"/>CrispI</a>
<a style="display:inline-block;" href="http://graphportal.com/"><img alt="GraphPortal" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/graphportal.webp" width="100"/>GraphPortal</a>
<a style="display:inline-block;" href="http://kwanjeeraw.github.io/grinn/"><img alt="Grinn" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/grinn.jpg" width="100"/>Grinn</a>
<a style="display:inline-block;" href="http://odaba.com/content/start/"><img alt="ODABA" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/odaba.webp" width="100"/>ODABA</a>
<a style="display:inline-block;" href="https://github.com/OWASP/Amass"><img alt="OWASP" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/owasp.webp" width="100"/>OWASP</a>
<a style="display:inline-block;" href="https://reldb.org/"><img alt="reldb" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/reldb.webp" width="100"/>reldb</a>
<a style="display:inline-block;" href="https://www.iri.com/products/voracity/"><img alt="Voracity" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/voracity.png" width="100"/>Voracity</a>
<a style="display:inline-block;" href="https://zeromq.org/"><img alt="ZeroMQ" src="
https://alcdn.oss-cn-shanghai.aliyuncs.com/img/logo/zeromq.gif" width="100"/>ZeroMQ</a>
没有示例的看这个目录下有没有 [【anyline-data-jdbc-dialect】](https://gitee.com/anyline/anyline/tree/master/anyline-data-jdbc-dialect)还没有的请联系群管理员
