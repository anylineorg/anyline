***详细说明请参考:***  
[http://doc.anyline.org/](http://doc.anyline.org/)


***快速开始请参考示例源码:***  
[https://gitee.com/anyline/anyline-simple](https://gitee.com/anyline/anyline-simple)


有问题请联系QQ群:  86020680  
[
<img src="http://alcms.oss-cn-shanghai.aliyuncs.com/anyline/img/ab/rhodilywys.jpg" />
](http://shang.qq.com/wpa/qunwpa?idkey=279fe968c371670fa9791a9ff8686f86dbac0b5edba8021a660b313e2dd863ad) 


AnyLine的核心是一个基于spring-jdbc生态的(No-ORM)数据库操作工具  
其重点是:
- 以最简单、快速、动态的方式操作数据库  
- 针对结果集的 数据二次处理能力    

简单来说主要作了两方面的工作：    
- 1）在运行时根据需求动态生成SQL(包括DDL和DML),特别是针对查询条件的封装  
  查询条件不再需要各种空判断、遍历、类型转换,机械繁琐的工作交给机器  
  这里说的动态是指:  
  不需要针对固定的表结构或具体的Entity,分别提供不同的Service/Dao/Mapper  
  一个默认的Service可以操作一切数据  


**操作数据库很简单，但AnyLine要实现的不仅仅是操作数据库，更不是简单的增删改查**   
**灵活的处理结果集数据、快速的生成符合业务逻辑的数据结构才是重点**      


- 2）为结果集定义了统一的数据结构,主要是DataSet&lt;DataRow&gt;结构类似于List&lt;Map&gt;  
  不要以为DataSet&lt;DataRow&gt;结构比实体类功能类弱   
  他将提供比实体类更全面、更强大的数据处理能力    
  为前端或第三方应用提供数据时 不再需要各种遍历、判断、计算、格式转换    
  一切与业务无关的数学运算,DataSet&lt;DataRow&gt;尽量作到 一键 ... 一键 ...
  [示例](https://gitee.com/anyline/service)

同时摒弃了各种繁琐呆板的Service/Dao/Entity/*O/Mapper 没有mybatis 没有各种配置 各种O  
没有需要自动生成的代码,没有模板文件(自动生成的都是程序员的负担)


## 适用场景
Anyline一的切都是面向动态、面向运行时环境  
适合于抽象设计阶段(实体概念还不明确或者设计工作不局限于某个特别的实体)   
常用于需要大量复杂动态的查询，以及查询的结果集需要经过深度处理的场景 如:

- **可视化数据源**  
    主要用来处理动态属性，以及适配前端的多维度多结构的数据转换   
  [示例](https://gitee.com/anyline/service)


- **低代码后台**    
    主要用来处理动态属性、动态数据源(下拉列表)以及用户自定义的一些属性    
    灵活的DDL也可以快速统一的操作各种表结构(包括各种时序、列式数据库)   
    [示例](https://gitee.com/anyline/service)  


- **物联网车联网数据处理**    
    如车载终端、交通信号灯、数字化工厂传感器、环境检测设备数据等   
  [示例](https://gitee.com/anyline/service)


- **数据清洗、数据批量处理**  
    各种结构的数据、更多的是不符合标准甚至是错误的结构  
  [示例](https://gitee.com/anyline/service)


- **报表输出，特别是用户自定义报表**  
    类似于可视化环境,只是样式简单一点  
  [示例](https://gitee.com/anyline/service)


- **运行时自定义表单/查询条件/数据结构**  
    各个阶段都要自定义，比低代码要求更高的是:操作用户不懂编程
  [示例](https://gitee.com/anyline/service)


- **网络爬虫数据解析**    
    不固定的结构、html解析(当然不是用正则或dom那太费脑子了)  
  [示例](https://gitee.com/anyline/service)



- **异构数据库迁移同步**  
    动态的数据结构可以灵活的适配多种不同的表,需不需要反复的get/set    
    兼容多种数据库的DDL也可以方便的在不同类型的数据库中执行  
  [示例](https://gitee.com/anyline/service)


- **还有一种很实现的场景是 许多项目到了交付的那一天 实体也没有设计完成**  
    别说设计了，需求都有可能还没结束就催交付了,Entity哪里找  
  [示例](https://gitee.com/anyline/service)




### 什么情况下说明你的应该考虑换工具了  
- 非常简单的增删改查,Entity中大部分只用到了get/set方法,很少需要计算  
这一般都是些hello world 或 练习作业  
这样的直接利用默认的service查出数据返回给前端就可以收工了  
不要再生成一堆重复的模板，简单改个属性也要层层修改，从头改个遍。    
  [示例](https://gitee.com/anyline/service)


- 代码中出现了大量的List,Map结构 或者 针对查询结果集需要大量的二次计算  
这种情况应该非常多见  
随着系统的增强完善和高度的抽象,同一份数据源将为各种不同的业务场景提供数据支持  
每个场景需要的数据结构各不雷同  
这时经常是为每类场景订制视图或SQL  
但数据支持部门不可能针对每种场景每个视图、每个SQL 生成不同的Entity  
更也不可能生成一个大而全的Entity以应万变  
  [示例](https://gitee.com/anyline/service)


- 与第三方系统交换数据时  
  只在自己内部系统的小圈子里时,List/Entity还勉强可以  
  遇到各种第三方系统呢，根本不知道会出现什么实体什么结果  
  难道还要像EJB那样相互给对方生成一堆Bean么？  
  [示例](https://gitee.com/anyline/service)



无论是Map还是Entity计算能力都非常有限,通常需要开发人员各种遍历、计算、格式化  
而这种大量的机械的计算不应该占用开发人员太多的时间  
Anyline提供的默认数据结构DataSet/DataRow已经实现了常用的数据二次处理功能,如:   
排序、维度转换、截取、去重、方差、偏差、交集合集差集、分组、忽略大小写对比、行列转换、类SQL过滤筛选(like,eq,in,less,between...)、JSON、XML格式转换等  
[示例](https://gitee.com/anyline/service)



## 不适用场景
对已经非常明确的实体执行增删改查操作  
不要跨过设计人员或者架构师/技术经理等直接拿给业务开发人员用  
[示例](https://gitee.com/anyline/service)

##  关于数据库的适配
直接看示例(代码都是一样的、可以用来测试一下自己的数据库是否被支持)
[https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect](https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect)


<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mysql">
<img alt="MySQL" src="http://cdn.anyline.org/img/logo/mysql.png" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-postgresql">
<img alt="PostgreSQL" src="http://cdn.anyline.org/img/logo/postgres.png" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oracle">
<img alt="Oracle 11G" src="http://cdn.anyline.org/img/logo/oracle.png" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mssql">
<img alt="SQL Server" src="http://cdn.anyline.org/img/logo/mssql.jpg" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mariadb">
<img alt="MariaDB" src="http://cdn.anyline.org/img/logo/mariadb.png" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-db2">
<b>IBM DB2</b>
</a>
<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-clickhouse">
<img alt="clickhouse" src="http://cdn.anyline.org/img/logo/clickhouse.jpg" width="100">
</a>
<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-sqlite">
<img alt="sqlite" src="http://cdn.anyline.org/img/logo/sqlite.jpg" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-dm">
<img alt="达梦" src="http://cdn.anyline.org/img/logo/dm.webp" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-tdengine">
<img alt="tdengine" src="http://cdn.anyline.org/img/logo/tdengine.png" width="100">
</a>


<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-derby">
<img alt="tdengine" src="http://cdn.anyline.org/img/logo/derby.webp" width="100">
</a>


<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-h2">
<img alt="H2" src="http://cdn.anyline.org/img/logo/h2db.png" width="100">
</a>

<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hsqldb">

<img alt="hsqldb" src="http://cdn.anyline.org/img/logo/hsqldb.webp" width="100">
</a>


<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-kingbase">

<img alt="kingbase" src="http://cdn.anyline.org/img/logo/kingbase.png" width="100">
</a>


<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-neo4j">
<img alt="Neo4j" src="http://cdn.anyline.org/img/logo/neo4j.webp" width="100">
</a>


<a href="https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hgdb">
<img alt="hgdb" src="http://cdn.anyline.org/img/logo/hgdb.webp" width="100">
</a>


<a href="">
<img alt="南大通用" src="http://cdn.anyline.org/img/logo/gbasedbt.jpg" width="100">
</a>

<a href="">
<img alt="cassandra" src="http://cdn.anyline.org/img/logo/cassandra.png" width="100">
</a>

<a href="">
<img alt="oceanbase" src="http://cdn.anyline.org/img/logo/oceanbase.webp" width="100">
</a>
<a href="">
<img alt="神舟通用" src="http://cdn.anyline.org/img/logo/oscar.png" width="100">
</a>

<a href="">
<img alt="polardb" src="http://cdn.anyline.org/img/logo/polardb.webp" width="100">
</a>
<a href="">
<img alt="questdb" src="http://cdn.anyline.org/img/logo/questdb.png" width="100">
</a>
<a href="">
<img alt="timescale" src="http://cdn.anyline.org/img/logo/timescale.svg" width="100">
</a>



没有示例的看这个目录下有没有 [【anyline-data-jdbc-dialect】](https://gitee.com/anyline/anyline/tree/master/anyline-data-jdbc-dialect)还没有的请QQ群管理管理员


## 如何使用
数据操作***不要***再从生成xml/dao/service以及各种配置各种O开始  
默认的service已经提供了大部分的数据库操作功能。  
操作过程大致如下:
```
DataSet set = service.querys("HR_USER(ID,NM)", 
    condition(true,"anyline根据约定自动生成的=,in,like等查询条件"));  
```
这里的查询条件不再需要各种配置,各种if else foreach标签  
Anyline会自动生成,生成规则可以参考这里的[【约定规则】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f2c45dd13bb112b34176fad5a868fa6a4)  
分页也不需要另外的插件，更不需要繁琐的计算和配置，指定true或false即可


## 如何集成
只需要一个依赖、一个注解即可实现与springboot,netty等框架项目完美整合  
直接看代码[【anyline-simple-hello】](https://gitee.com/anyline/anyline-simple/tree/master/anyline-simple-hello)  
生产环境可以参考这几个[pom](https://gitee.com/anyline/anyboot/tree/master)    
[【anyboot-start】](https://gitee.com/anyline/anyboot/blob/master/anyboot-start/pom.xml) 没有web环境,如定时任务,爬虫等    
[【anyboot-start-mvc】](https://gitee.com/anyline/anyboot/blob/master/anyboot-start-mv/pom.xml) 基于spring-mvc  
[【anyboot-start-mvc-mysql】](https://gitee.com/anyline/anyboot/blob/master/anyboot-start-mvc-mysql/pom.xml) 基于spring-mvc MySQL数据库    
[【anyboot-start-mvc-jsp-mysql】](https://gitee.com/anyline/anyboot/blob/master/anyboot-start-mvc-jsp-mysql/pom.xml) 基于spring-mvc MySQL数据库 支持JSP    
以下可以略过  

根据数据库类型添加依赖,如
```
<dependency>
    <groupId>org.anyline</groupId>
    <artifactId>anyline-data-jdbc-mysql(oracle|clickhouse...)</artifactId>
    <version>8.6.1-20221010</version>
</dependency>
```

在需要操作数据库的地方注入AnylineService
```
@Qualifier("anyline.service")
protected AnylineService service;
```
接下来service就可以完成大部分的数据库操作了。常用示例可以参考[【示例代码】](https://gitee.com/anyline/anyline-simple)

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


## 实战对比
在理想的HelloWord环境下，任何方式都可以快速实现目标，更能体现优劣的是复杂多变的实战环境。  
### &nbsp;首先要承认银弹是没有的，所以先说 劣势
- 在增、删、改、查4个过程中，增的环境劣势比较明显  
- 操作查询结果时，不能像Entity一样有IDE的提示和自动补齐，减少了IDE的协助确实让许多人寸步难行，  
  大部分人也是在这里被劝退的。  
- 在插入数据时，不能像像Entity一样:userService.save(user)，而是需要指定表名:service.save(HR_USER, row);  

#### &nbsp;&nbsp;以上问题如果平衡的
- AnyLine返回的结果集与Entity之间随时可以相互转换,也可以在查询时直接返回Entity  



### &nbsp;有思想的程序员会想为何要造个轮子 可靠吗，所以再说 疑问
+ AnylineLine并非新造了一个轮子，只是简单的把业务参数传给了底层的spring-jdbc  
  接下来的操作（如事务控制、连接池等）完全交给了spring-jdbc(没有能力作好的事我们不作)
  
 
+ 如果非要说是一个新轮子，那只能说原来的轮子太难用，太消耗程序员体力了。  
  正事还没开始就先生成一堆的mapper,OOO，各种铺垫  
  铺垫完了要操作数据实现业务了，依然啰嗦，各种 劳力 不劳心 的遍历及加减乘除  
  不但操作数据库慢,查询出来的结果集更是一堆废柴,除了能遍历能get/set啥也不是

### &nbsp;所以重点说 优势
####  &nbsp;&nbsp;1. **关于查询条件**  
&nbsp;&nbsp;&nbsp;&nbsp;这是开发人员最繁重的体力劳动<font color="red">之一</font>  
&nbsp;&nbsp;&nbsp;&nbsp;接收参数、验证、格式化、层层封装传递到mapper.xml，再各种判断、遍历就为生成一条SQL    
&nbsp;&nbsp;&nbsp;&nbsp;下面的这些标签许多人可能感觉习以为常了,以下是<font color="red">反例 反例 反例</font>
```
 <if test="code != null and code != '' ">
    AND CODE = #{code}
 </if>

 <if test="name != null and name != '' ">
  AND NAME like concat('%',#{name},'%')
</if>

<if test="types != null and types.size > 0 ">
    AND TYPE IN
    <foreach collection="types" item="type" open="(" close=")" separator=",">
        #{type}
    </foreach>
</if>
```
```
但这并不正常，这期间还有什么是必须程序员参与的，程序员不参与就自动不了，就约定不了的吗？
  
换一种方式处理：  
不要mapper.xml了，也更不要定位SQL的ID的
    
直接在java中这样处理          
condition("CODE:code","NAME:name%", "TYPE:[type]")
其他的交给机器处理，生成SQL     
WHERE CODE = ? AND NAME LIKE '?%' AND TYPE IN(?,?...)
``` 
&nbsp;&nbsp;&nbsp;&nbsp;更多的约定可以参考这里的[【约定规则】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f2c45dd13bb112b34176fad5a868fa6a4)


####  &nbsp;&nbsp;2. **结果集的二次操作**    
&nbsp;&nbsp;&nbsp;&nbsp;这是开发人员最繁重的劳动<font color="red">之二</font>    
&nbsp;&nbsp;&nbsp;&nbsp;从数据库中查询出数据后，根据业务需求还需要对结果集作各种操作，最简单的如加减乘除、交集差集、筛选过滤等  
&nbsp;&nbsp;&nbsp;&nbsp;这些常见的操作DataSet中都已经提供默认实现了，如ngl表达式、聚合函数、类SQL筛选过滤、维度转换等。


#### &nbsp;3. **关于面向动态与运行时环境**  
&nbsp;&nbsp;&nbsp;&nbsp;这里说的动态是指出动态数据源、动态数据结构、动态结果集  
&nbsp;&nbsp;&nbsp;&nbsp;运行时环境是指在系统运行阶段才能确定以上内容，而不是在需求、设计、编码阶段    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
###### &nbsp;&nbsp;&nbsp;&nbsp;动态数据源：
&nbsp;&nbsp;&nbsp;&nbsp;一般是在系统运行时生成  
&nbsp;&nbsp;&nbsp;&nbsp;典型场景如数据中台，用户通过管理端提交第三方数据库的地址帐号，中台汇聚多个数据源的数据  
&nbsp;&nbsp;&nbsp;&nbsp;   
&nbsp;&nbsp;&nbsp;&nbsp;这种情况下显示不是在配置文件中添加多个数据源可以解决的  
&nbsp;&nbsp;&nbsp;&nbsp;而是需要在接收到用户提交数据后，生成动态的数据源  
&nbsp;&nbsp;&nbsp;&nbsp;生成的动态数据源最好交给Spring等容器管理  
&nbsp;&nbsp;&nbsp;&nbsp;以充分利用其生态内的连接池,事务管理,切面等现有工具  
&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;在切换数据源时也不能通过切面来实现    
&nbsp;&nbsp;&nbsp;&nbsp;而是根据组织或租户身份等上下文环境来切换
###### &nbsp;&nbsp;&nbsp;&nbsp;动态数据结构:
&nbsp;&nbsp;&nbsp;&nbsp;一般由非专业开发人员甚至是最终用户来设计表结构  
&nbsp;&nbsp;&nbsp;&nbsp;根据用户设置或不同场景返回不同结构的结果集    
&nbsp;&nbsp;&nbsp;&nbsp;查询条件也由用户动态指定    
&nbsp;&nbsp;&nbsp;&nbsp;结果集与查询条件的选择范围也不能在编码阶段设置限定    
&nbsp;&nbsp;&nbsp;&nbsp;典型场景如物联网平台仪器设备参数、低代码平台、报表工具    


###### &nbsp;&nbsp;&nbsp;&nbsp;常用的数据结构有两种  
&nbsp;&nbsp;&nbsp;&nbsp;1).DataRow类似于一个Map  
&nbsp;&nbsp;&nbsp;&nbsp;2).DataSet是DataRow的集合，并内含了分页信息  

&nbsp;&nbsp;&nbsp;&nbsp;以下场景中将逐步体现出相对于List,Entity的优势  
###### &nbsp;&nbsp;&nbsp;&nbsp;**1). 最常见的如更新或查询部分列**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DataRow row = service.query("HR_USER(ID,CODE)")  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;service.update(row,"CODE") 

###### &nbsp;&nbsp;&nbsp;&nbsp;**2).可视化数据源、报表输出、数据清洗**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;这些场景下都需要的数据结构都是灵活多变的    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;经常是针对不同的业务从多个表中合成不同的结构集  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;甚至是运行时根据用户输入动态结合的结构集  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;输出结果集后又需要大量的对比及聚合操作  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;这种情况下显示不可能为每个结果集生成一个对应Entity，只能是动态的Map结构  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;在对结构集的二次操作上,DataRow/DataSet可以在抽象设计阶段就完成，而Entity却很难
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

###### &nbsp;&nbsp;&nbsp;&nbsp;**3).低代码后台、元数据管理**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;作为一个低代码的后台，首先需要具体灵活可定制的表结构(通常会是一个半静半动的结构)     
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;我们将不再操作具体的业务对象与属性。对大部分业务的操作都只能通过抽象的元数据进行。    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;举例来说一个简单的求和过程，原来在对静态结构时常用的的遍历、Lamda、反射都难堪重任了。  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;我们能接收到的信息通常是这样的:类型(学生)、属性(年龄)、条件(年级=1)、聚合公式(平均值)    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Anyline的实现过程类似这样    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DataSet set = service.querys(学生,年级=1);  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;int 平均年龄 = set.agg(平均值,年龄);   

###### &nbsp;&nbsp;&nbsp;&nbsp;**4).运行时自定义表单、查询条件**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;许多情况下我们的基础版本产品，很难满足用户100%的需求，  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;而这些新需求又大部分是一些简单的表单、查询条件    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;如果是让程序员去开发一个表单，添加几个查询条件，那确实很简单    
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;但用户不是程序员，我们也不可能为每个用户提供全面全天候的技术支持  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;考虑到成本与用户体验的问题通常会给用户提供一个自定义表单与查询条件的功能  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;自定义并不难，难的是对自定义表单的存储、查询、关联，以及对自定义查询条件的支持  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;与上一条说的元数据管理一样，我们在代码实现环节还是不知道会有什么对象什么属性  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;当然也更不会有对应的service, dao, mapper, VO/DTO/BO/DO/PO/POJO  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Anyline的动态查询类似这样实现  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;service.query(类型(属性集合),condition().add('对比方式','属性','值');


###### &nbsp;&nbsp;&nbsp;&nbsp;**5).物联网环境(特别是像Cassandra、ClickHouse等列式数据库 InfluxDB、timescale等时序数据库)**  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;与低代码平台类似都需要一种动态的结构，并且为了数据读取的高效，数据在水平方向上变的更分散。  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;这与最终用户需要显示的格式完全不一样，直接通过数据库查询出来的原始数据通常是类似这样  

| 时间戳           | KEY | VALUE      |
|---------------|-----|------------|
| 1657330073131 | LAT | 39.917055  |
| 1657330073131 | LNG | 116.392191 |
| 1657330073132 | LAT | 39.917055  |
| 1657330073132 | LNG | 116.392191 |
| 1657330073133 | LAT | 39.917055  |
| 1657330073134 | LNG | 116.392191 |

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 而最终展示的界面可能是这样：

| 时间戳           | LNG        | LAT       |
|---------------|------------|-----------|
| 1657330073131 | 116.392191 | 39.917055 |
| 1657330073131 | 116.392191 | 39.917055 |


| 日期(向下合并)  |  时间点1(向右合并)   |              |  时间点2(向右合并)   |           |    时间点...N     |            |
|:---------:|:-------------:|:------------:|:-------------:|:---------:|:--------------:|:----------:|
|           |      LNG      |     LAT      |      LNG      |    LAT    |      LNG       |    LAT     |  
|   01-01   |  116.392191   |  39.917055   |  116.392191   | 39.917055 |   116.392191   | 39.917055  |
|   01-02   |  116.392191   |  39.917055   |  116.392191   | 39.917055 |   116.392191   | 39.917055  |

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;当然实战中会比这更复杂，历经实战的程序员一定体验过什么是千变万化、什么是刁钻苛刻  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;数据库中将不再有一一对应的hello表格，java中也没有对应的Entity  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;可以想像的出来基于一个静态结构或者原始的Map,List结构需要程序员负责多少体力  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;要在这个基础上实现让用户自定义报表，那可能比把用户培养成一个程序员还要困难  

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;而一个有思想的程序员应该会把以上问题抽象成简单的行列转换的问题     
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;并在项目之前甚至没有项目的时候就已经解决之。  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;各种维度的转换可以参考DataSet.pivot()的几个重载 或示例代码 anyline-simple-result  

 
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

###### &nbsp;&nbsp;&nbsp;&nbsp;**6).关于分页查询的数据存储结构**
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;  
###### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;通过默认的方式查询  
- 无论是否分页 都可以通过DataSet结构接收数据  
- 不同的是分页后DataSet.PageNavi中会嵌入详细的分页信息  
###### &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;通过User.class查询数据时  
- 如果没有分页 可以通过List&lt;User&gt;&gt;结构接收数据  
- 如果有分页了 那需要通过Page&lt;List&lt;User&gt;&gt;结构接收数据  
- 简单查询个部门列表，还要根据分不分页写两个接口吗   

###### &nbsp;&nbsp;&nbsp;&nbsp;**7).数据加密**      
&nbsp;&nbsp;&nbsp;&nbsp;对于需要加密的数据经常会遇到数字类型的ID  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;而加密后的数据类型通常是String类型，导致原对象无法存储  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;

  
&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;