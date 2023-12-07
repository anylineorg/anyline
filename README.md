[【***招募开发合伙伴，个人或组织皆可***】](http://doc.anyline.org/ss/23_1213)  
实际过程很简单，任务已经拆解到一条SQL,一个方法。  
就是把每极个别与ISO/IEC 9075标准不一致的SQL补充一下  
我们将协助你熟悉源码 并 对你的项目提供全面的技术支持    
这时有[【400+】](http://doc.anyline.org/dbs)数据库，总有一个是你熟悉的   
  
***详细说明请参考:***  
[http://doc.anyline.org/](http://doc.anyline.org/)  
开发测试来环境请使用[【8.7.1-SNAPSHOT】](http://doc.anyline.org/aa/aa_3702)版本    
发版务必到[【中央库】](https://mvnrepository.com/artifact/org.anyline/anyline-core)找一个正式版本，不要把SNAPSHOT版本发到生产环境  
关于多数据源，请先阅读   
[【三种方式注册数据源】](http://doc.anyline.org/aa/a9_3451)
[【三种方式切换数据源】](http://doc.anyline.org/aa/64_3449)
[【多数据源事务控制】](http://doc.anyline.org/ss/23_1189)  
低代码平台、数据中台等场景需要生成SQL/操作元数据参考  
[【JDBCAdapter】](http://doc.anyline.org/ss/01_1193)
[【SQL及日志】](http://doc.anyline.org/aa/70_3793)
[【service.metadata】](http://doc.anyline.org/ss/22_1174)
[【SQL.metadata】](http://doc.anyline.org/aa/c1_3847)

***快速开始请参考示例源码(各种各样最简单的hello world):***  
[https://gitee.com/anyline/anyline-simple](https://gitee.com/anyline/anyline-simple)


***一个字都不想看，就想直接启动项目的下载这个源码:***  
[https://gitee.com/anyline/anyline-simple-clear](https://gitee.com/anyline/anyline-simple-clear)

有问题请不要自行百度，因为百度收录的内容有可能过期或版本不一致,有问题请联系

[<img src="http://cdn.anyline.org/img/user/alq.png" width="150">](http://shang.qq.com/wpa/qunwpa?idkey=279fe968c371670fa9791a9ff8686f86dbac0b5edba8021a660b313e2dd863ad)
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<img src="http://cdn.anyline.org/img/user/alvg.png" width="150">  
&nbsp;&nbsp;&nbsp;QQ群(86020680)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
或&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
微信群
## 简介
AnyLine的核心是一个基于spring生态的D-ORM(动态对象关系影射),其重点是:
- 以最简单、快速方式操作 ***数据库*** 与 ***结果集***
- 兼容各种数据库，统一生成或执行DML/DDL，读写表结构、索引等元数据
- 一切基于动态、基于运行时(包括数据源、数据结构等)

常用于动态结构的场景中，作为SQL解析引擎或适配器出现 ,
如:数据中台、可视化、低代码、SAAS、自定义表单、异构数据库迁移同步、 物联网车联网数据处理、
数据清洗、运行时自定义报表/查询条件/数据结构、 爬虫数据解析等。
参考【[适用场景](http://doc.anyline.org/ss/ed_14)】  
实际开发过程中主要用来:    
- 运行时动态注册切换数据源   
- 生成动态SQL(重点是复杂的动态查询条件)  
- 操作元数据(如不同数据库间复制表结构)
- 屏蔽不同数据库的语法差异(主要是DDL差异)

### 已经有ORM了 为什么还要有AnyLine,与ORM有什么区别
- ***面向场景不同***  
anyline主要面向动态场景，就是运行时随时可变的场景。  
如我们常用的动态数据源，不是在部署时可以固定在配置文件中，  
而是可能在不确定的时间，由不确定的用户提供的不确定数据源。
- ***针对产品不同***  
anyline一般不会直接用来开发一个面向终端用户的产品(如ERP、CRM等)，  
而是要开发一个中间产品(如低代码平台)，让用户通过中间产品来生成一个最终产品。  
再比如用anyline开发一个自定义查询分析工具，让用户通过这个工具根据业务需求生成动态报表。
- ***操作对象不同***  
anyline主要操作元数据，因为在项目开发之初，可能就没有一个如ERP/CRM之类的明确的产品，  
当然也就没有订单、客户之类的具体对象及属性，所以也没什么具体数据可操作。
- ***面向用户(开发设计人员)不同***  
ORM的用户基本上看看文档复制个模板就可以开工了，
而anyline用户因为其面对的产品的特点也决定了这部分用户不可能以一个固定的思维去完成自己的产品，  
这也直接导致了anyline经常会没有一个工具或方法能满足用户突然而来的灵感与思路。
那就只能随用户灵感而动。也正是用户源源不断的灵感聚沙了现在的anyline。  
所以anyline的用户大部分也是其作者。
- ***所以对用户(开发设计人员)要求不同***  
一个ORM用户用了许多年的连接池，他可以只知道配置哪几个默认参数，也能正常开展工作。  
但anyline的用户不行，他要求这个团队中至少有一个人要明白其所以然。这就说来话长了，有兴趣进群慢慢说吧。
### 实际操作中与ORM最明显的区别是  
- ***摒弃了各种繁琐呆板的实体类以及相关的配置文件***  
  让数据库操作更简单，不要一动就是一整套的service/dao/mapping/VOPODTO有用没用的各种O，生成个简单的SQL也各种判断遍历。  
- ***强强化了结果集的对象概念***  
  面向对象的对象不是只有get/set/注解这么弱  
  需要把数据及对数据的操作封装在一起，作为一个相互依存的整体，并实现高度的抽象  
  要关注元数据，不要关注姓名、年龄等具体属性  
  强化针对结果集的数据二次处理能力  
  如结果集的聚合、过滤、行列转换、格式化及各种数学计算尽量作到一键...一键...  
  而不要像ORM提供的entity,map,list除了提供个get/set/foreach，稍微有点用的又要麻烦程序员各种判断各种遍历  
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
再如空值的处理包括,"",null,"null","\n","\r","\t","   "全角、半角等各种乱七八糟的情况  
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
DataSet set = service.querys("HR_USER(ID,NM)", 
    condition(true,"anyline根据约定自动生成的=,in,like等查询条件"));  
```
这里的查询条件不再需要各种配置,各种if else foreach标签  
Anyline会自动生成,生成规则可以【参考】这里的[【约定规则】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f2c45dd13bb112b34176fad5a868fa6a4)  
分页也不需要另外的插件，更不需要繁琐的计算和配置，指定true或false即可


## 如何集成

只需要一个依赖、一个注解即可实现与springboot,netty等框架项目完美整合，参考【入门系列】
大概的方式就是在需要操作数据库的地方注入AnylineService
接下来service就可以完成大部分的数据库操作了。常用示例可以参考【[示例代码](https://gitee.com/anyline/anyline-simple)】

## 兼容
如果实现放不下那些已存在的各种XOOO  
DataSet与Entity之间可以相互转换  
或者这样:
```
EntitySet<User> = service.querys(User.class, 
    condition(true,"anyline根据约定自动生成的查询条件")); 
//true：表示需要分页
//为什么不用返回的是一个EntitySet而不是List?
//因为分页情况下,EntitySet中包含了分页数据,而List不行。
//无论是否分页都返回相同的数据结构，而不需要根据是否分页实现两个接口返回不同的数据结构



//也可以这样(如果真要这样就不要用anyline了,还是用MyBatis,Hibernate之类吧)
public class UserService extends AnylinseService<User> 
userService.querys(condition(true,"anyline根据约定自动生成的查询条件")); 
```

## 适用场景
- **低代码后台**    
  主要用来处理动态属性、动态数据源、运行时自定义查询条件、元数据管理等。  
  比较容易落地的几个场景如财务、库存等ERP模块用户经常需要输出不同格式的报表，根据不同维度查询统计数据  
  前端可以用百度amis前端低代码框架，后端由anyline解析SQL及查询条件，管理元数据。  
  [【示例】](https://gitee.com/anyline/service)


- **数据中台**    
  动态处理各种异构数据源、强大的结果集批量处理能力，不再需要对呆板的实体类各种遍历各种转换。  
  [【示例】](https://gitee.com/anyline/anyline-simple)
-
- **可视化数据源**  
  主要用来处理动态属性，以及适配前端的多维度多结构的数据转换   
  [【参考】](http://doc.anyline.org/a?id=p298pn6e9o1r5gv78vicac1e624c62387f7bb5cdeaeddf6f93f9eb865d5cc60b9b)


- **物联网车联网数据处理**    
  如车载终端、交通信号灯、数字化工厂传感器、环境检测设备数据等   
  [【示例】](https://gitee.com/anyline/service)


- **数据清洗、数据批量处理**  
  各种结构的数据、更多的是不符合标准甚至是错误的结构  
  这种场景下需要一个灵活的数据结构来统一处理各种结构的数据    
  再想像一下临时有几个数据需要处理一下(如补齐或替换几个字符)  
  这个时候先去创建个Entity,XML,Service,Dao吗  
  [【示例】](https://gitee.com/anyline/service)


- **报表输出，特别是用户自定义报表**  
  类似于可视化环境,样式相对简单一点，但精度要求极高，需要控制到像素、字体等  
  如检验检测报告、资质证书等，当然这需要配合 anyline-office  
  [【office示例】](http://office.anyline.org/v/b6_3797)


- **运行时自定义表单/查询条件/数据结构**  
  各个阶段都要自定义，比低代码要求更高的是:操作用户不懂编程
  [【示例】](https://gitee.com/anyline/service)


- **网络爬虫数据解析**    
  不固定的结构、html解析(当然不是用正则或dom那太费脑子了)  
  [【参考】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f51d08504f16eef5d4dd25719cf7844ce)



- **异构数据库迁移同步**  
  动态的数据结构可以灵活的适配多种不同的表,需不需要反复的get/set    
  兼容多种数据库的DDL也可以方便的在不同类型的数据库中执行  
  [【核心代码示例(Mysql到Apache Ignite)】](http://doc.anyline.org/aa/08_3842)  
  [【基础应用项目】](https://gitee.com/anyline/anyline-database-sync)
  [【完整应用代替datax】](https://gitee.com/czarea/devops)

- **还有一种很实现的场景是 许多项目到了交付的那一天 实体也没有设计完成**  
  别说设计了，需求都有可能还没结束就催交付了,Entity哪里找  
  [【示例】](https://gitee.com/anyline/service)

##  关于数据库的适配  [【更多查看】](http://doc.anyline.org/dbs)  
[【示例源码】](https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect)

<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mysql"> <img alt="MySQL" src="http://cdn.anyline.org/img/logo/mysql.png" width="100" />MySQL </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-postgresql"> <img alt="PostgreSQL" src="http://cdn.anyline.org/img/logo/postgres.png" width="100" />PostgreSQL </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oracle"> <img alt="Oracle 11G" src="http://cdn.anyline.org/img/logo/oracle.png" width="100" />Oracle </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mssql"> <img alt="SQL Server" src="http://cdn.anyline.org/img/logo/mssql.jpg" width="100" />SQL Server </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mariadb"> <img alt="MariaDB" src="http://cdn.anyline.org/img/logo/mariadb.png" width="100" />MariaDB </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-db2">IBM DB2</a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-clickhouse"> <img alt="clickhouse" src="http://cdn.anyline.org/img/logo/clickhouse.jpg" width="100" />clickhouse </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-sqlite"> <img alt="sqlite" src="http://cdn.anyline.org/img/logo/sqlite.jpg" width="100" />sqlite </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-dm"> <img alt="达梦" src="http://cdn.anyline.org/img/logo/dm.webp" width="100" />达梦 </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-tdengine"> <img alt="tdengine" src="http://cdn.anyline.org/img/logo/tdengine.png" width="100" />tdengine </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-derby"> <img alt="derby" src="http://cdn.anyline.org/img/logo/derby.webp" width="100" />derby </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-h2"> <img alt="H2" src="http://cdn.anyline.org/img/logo/h2db.png" width="100" />H2 </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hsqldb"> <img alt="hsqldb" src="http://cdn.anyline.org/img/logo/hsqldb.webp" width="100" />hsqldb </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-kingbase"> <img alt="人大金仓" src="http://cdn.anyline.org/img/logo/kingbase.png" width="100" />人大金仓 </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-opengauss"> <img alt="OpenGauss" src="http://cdn.anyline.org/img/logo/opengauss.png" width="100" />OpenGauss </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-neo4j"> <img alt="Neo4j" src="http://cdn.anyline.org/img/logo/neo4j.webp" width="100" />Neo4j </a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-highgo"> <img alt="瀚高" src="http://cdn.anyline.org/img/logo/hgdb.webp" width="100" />瀚高 </a> 
<a style="display:inline-block;" href=""> <img alt="Hive" src="http://cdn.anyline.org/img/logo/hive.svg" width="100" />Apache Hive</a> 
<a style="display:inline-block;" href=""> <img alt="南大通用" src="http://cdn.anyline.org/img/logo/gbase.jpg" width="100" />南大通用 </a> 
<a style="display:inline-block;" href=""> <img alt="cassandra" src="http://cdn.anyline.org/img/logo/cassandra.png" width="100" />cassandra </a> 
<a style="display:inline-block;" href=""> <img alt="oceanbase" src="http://cdn.anyline.org/img/logo/oceanbase.webp" width="100" />oceanbase </a> 
<a style="display:inline-block;" href=""> <img alt="神舟通用" src="http://cdn.anyline.org/img/logo/oscar.png" width="100" />神舟通用 </a> 
<a style="display:inline-block;" href=""> <img alt="polardb" src="http://cdn.anyline.org/img/logo/polardb.webp" width="100" />polardb </a> 
<a style="display:inline-block;" href=""> <img alt="questdb" src="http://cdn.anyline.org/img/logo/questdb.png" width="100" />questdb </a> 
<a style="display:inline-block;" href=""> <img alt="timescale" src="http://cdn.anyline.org/img/logo/timescale.svg" width="100" />timescale </a> 
<a style="display:inline-block;" href=""> <img alt="海量数据" src="http://cdn.anyline.org/img/logo/vastdata.png" width="100" />Vastbase(海量数据)</a> 
<a style="display:inline-block;" href=""> <img alt="恒生电子" src="http://cdn.anyline.org/img/logo/lightdb.png" width="100" />LightDB(恒生电子)</a> 
<a style="display:inline-block;" href=""> <img alt="万里数据库" src="http://cdn.anyline.org/img/logo/greatdb.png" width="100" />greatdb(万里数据库)</a> 
<a style="display:inline-block;" href=""> <img alt="云和恩墨" src="http://cdn.anyline.org/img/logo/mogdb.png" width="100" />mogdb(云和恩墨)</a> 
<a style="display:inline-block;" href=""> <img alt="中兴GoldenDB" src="http://cdn.anyline.org/img/logo/zte.webp" width="100"/>GoldenDB(中兴)</a> 
<a style="display:inline-block;" href=""> <img alt="GaiaDB-X" src="http://cdn.anyline.org/img/logo/bdy.jpg" />GaiaDB-X(百度云)</a> 
<a style="display:inline-block;" href=""> <img alt="TiDB" src="http://cdn.anyline.org/img/logo/tidb.svg" width="100" />TiDB</a> 
<a style="display:inline-block;" href=""> <img alt="AntDB" src="http://cdn.anyline.org/img/logo/antdb.png" width="100" />AntDB(亚信)</a>
<a style="display:inline-block;" href=""> <img alt="citus" src="http://cdn.anyline.org/img/logo/citus.png" width="100"/>citus</a> 
<a style="display:inline-block;" href=""> <img alt="TDSQL" src="http://cdn.anyline.org/img/logo/tencent.ico" />TDSQL(TBase)(腾讯云)</a> 
<a style="display:inline-block;" href=""> <img alt="磐维数据库" src="http://cdn.anyline.org/img/logo/10086.png" width="100"/>磐维数据库(中国移动)</a> 
<a style="display:inline-block;" href=""> <img alt="中国联通cudb" src="http://cdn.anyline.org/img/logo/chinaunicom.png" width="100"/>CUDB(中国联通)</a> 
<a style="display:inline-block;" href=""> <img alt="沐融信息科技" src="http://cdn.anyline.org/img/logo/murongtech.png" width="100"/>MuDB(沐融)</a> 
<a style="display:inline-block;" href=""> <img alt="北京酷克数据" src="http://cdn.anyline.org/img/logo/hashdata.png" width="100"/>HashData(酷克)</a> 
<a style="display:inline-block;" href=""> <img alt="热璞" src="http://cdn.anyline.org/img/logo/hotdb.png" width="100"/>HotDB(热璞)</a> 
<a style="display:inline-block;" href=""> <img alt="优炫" src="http://cdn.anyline.org/img/logo/uxdb.png" width="100"/>UXDB(优炫)</a> 
<a style="display:inline-block;" href=""> <img alt="星环" src="http://cdn.anyline.org/img/logo/kundb.png" width="100"/>KunDB(星环)</a> 
<a style="display:inline-block;" href=""> <img alt="StarDB" src="http://cdn.anyline.org/img/logo/stardb.png" width="100"/>StarDB(京东)</a> 
<a style="display:inline-block;" href=""> <img alt="YiDB" src="http://cdn.anyline.org/img/logo/yidb.png" width="100"/>YiDB(天翼数智)</a> 
<a style="display:inline-block;" href=""> <img alt="UbiSQL" src="http://cdn.anyline.org/img/logo/ubisql.webp" width="100"/>UbiSQL(平安科技)</a> 
<a style="display:inline-block;" href=""> <img alt="华胜信泰" src="http://cdn.anyline.org/img/logo/xigemadb.jpg" width="100"/>xigemaDB(华胜信泰)</a> 
<a style="display:inline-block;" href=""> <img alt="星瑞格" src="http://cdn.anyline.org/img/logo/sinodb.png" width="100"/>SinoDB(星瑞格)</a> 
<a style="display:inline-block;" href=""> <img alt="CockroachDB" src="http://cdn.anyline.org/img/logo/cockroachdb.png" width="100"/>CockroachDB</a> 
<a style="display:inline-block;" href=""> <img alt="InfluxDB" src="http://cdn.anyline.org/img/logo/influxdata.svg" width="100"/>InfluxDB</a> 
<a style="display:inline-block;" href=""> <img alt="Informix" src="http://cdn.anyline.org/img/logo/informix.webp" width="100"/>Informix</a> 
<a style="display:inline-block;" href=""> <img alt="MongoDB" src="http://cdn.anyline.org/img/logo/mongodb.svg" width="100"/>MongoDB</a> 
<a style="display:inline-block;" href=""> <img alt="MogoDB" src="http://cdn.anyline.org/img/logo/mogdb.png" width="100"/>MogoDB</a> 
<a style="display:inline-block;" href=""> <img alt="RethinkDB" src="http://cdn.anyline.org/img/logo/rethinkdb.png" width="100"/>RethinkDB</a> 
<a style="display:inline-block;" href=""> <img alt="SAP HANA" src="http://cdn.anyline.org/img/logo/hana.png" width="100"/>SAP HANA</a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-voltdb"> <img alt="voltdb" src="http://cdn.anyline.org/img/logo/voltdb.svg" width="100" />voltdb </a>
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-ignite"> <img alt="voltdb" src="http://cdn.anyline.org/img/logo/ignite.svg" width="100" />Apache Ignite</a> 
<a style="display:inline-block;" href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-doris"> <img alt="voltdb" src="http://cdn.anyline.org/img/logo/doris.svg" width="100" />Apache Doris</a> 

没有示例的看这个目录下有没有 [【anyline-data-jdbc-dialect】](https://gitee.com/anyline/anyline/tree/master/anyline-data-jdbc-dialect)还没有的请联系群管理员
