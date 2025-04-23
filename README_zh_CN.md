[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)  

<table style="text-align: center;">
  <tr>
  <td>
    <a href="https://qm.qq.com/q/M5ub6mqS0o"><img src="https://cdn.anyline.org/img/user/alq.png" width="150"/></a> 
  </td>
  <td>
   <a href="http://www.anyline.org/ss/9f_17"><img src="https://cdn.anyline.org/img/user/alvg.png" width="150"></a>
  </td>
  <td>
   <a href="http://www.anyline.org/ss/9f_17"><img src="https://cdn.anyline.org/img/user/alv.png" width="150"></a>
  </td>
  </tr>
  <tr>
  <td>QQ群(86020680)</td>
  <td>微信群	</td>
  <td>过期或满员联系管理员
  </td>
  </tr>
  <tr>
    <td colspan="3">有问题请联系以上群(无论BUG、疑问、需求、源码、竞品) </td>
</tr>
</table>


 
***快速测试请参考[【anyline-simple-clear】](https://gitee.com/anyline/anyline-simple-clear)***
***详细语法请参考[【anyline-simple】](https://gitee.com/anyline/anyline-simple)***  
关于多数据源，请先阅读   
[【六种方式注册数据源】](http://doc.anyline.org/aa/a9_3451)
[【三种方式切换数据源】](http://doc.anyline.org/aa/64_3449)
[【多数据源事务控制】](http://doc.anyline.org/ss/23_1189)  
低代码平台、数据中台等场景需要生成SQL/操作元数据参考  
[【JDBCAdapter】](http://doc.anyline.org/ss/01_1193)
[【返回SQL方言、导出SQL】](http://doc.anyline.org/aa/70_3793)
[【表结构差异对比及生成DDL】](http://doc.anyline.org/aa/a2_3936)
[【ConfigStore与JSON互换】](http://doc.anyline.org/aa/73_13975)
[【service.metadata】](http://doc.anyline.org/ss/22_1174)
[【SQL.metadata】](http://doc.anyline.org/aa/c1_3847)

## 简介
AnyLine的核心是一个面向运行时的 元数据动态关系映射 主要用于  
- 动态注册切换数据源
- 读写表结构、索引、函数、存储过程等元数据  
- 对比数据库结构差异  
- 生成动态DDL/DML/DQL,组合动态查询条件  
- 内存计算(结果集二次操作)    

一个天然的低代码、动态表单、动态数据源底层工具  

适配各种关系型与非关系型数据库(及各种国产小众数据库)    
常用于动态结构场景的底层支持，作为SQL解析引擎或适配器出现  
如:数据中台、可视化、低代码、SAAS、自定义表单、异构数据库迁移同步、 物联网车联网数据处理、
条件/数据结构、 爬虫数据解析等。
参考【[适用场景](http://doc.anyline.org/ss/ed_14)】  

## AnyLine与传统ORM的差异：

- 动态 VS 静态  
  AnyLine‌：基于运行时元数据驱动，支持动态数据源注册（如用户运行时提供数据库地址/类型），无需预定义实体类或映射关系。
‌传统ORM‌（如Hibernate）：依赖静态实体类与数据库表的预映射，需提前配置方言和表结构。


- 元数据操作 VS 对象操作  
  AnyLine‌：面向数据库元数据（如表结构、字段类型），适用于低代码平台或未知业务场景。
‌传统ORM‌：通过对象模型（Class/Property）间接操作数据库，需预先定义对象关系


- 多数据库适配  
  AnyLine‌：通过动态元数据引擎和智能方言适配，实现异构数据库的无缝兼容。内置200+种SQL语法转换规则，自动识别数据库类型并生成目标方言SQL，将不同数据库的元数据抽象为标准化对象，通过统一接口操作，实现元数据对象在各个数据库之间无缝兼容。
‌传统ORM‌：需硬编码实体类和方言，无法动态适配异构数据库的元数据差异和自动转换SQL语法，扩展性和兼容性受限。

##### 数据源注册及切换
注意这里的数据源并不是主从关系，而是多个完全不相关的数据源。
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
ConfigStore configs;
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

## 已经有了ORM了，为什么还要用AnyLine

- 一、面向场景不同‌  
  AnyLine：主要面向动态场景，即运行时随时可变的场景。例如，动态数据源可能在不确定的时间由不确定的用户提供，表结构等元数据也可能随着用户或数据源的不同而随时变化。  
  传统ORM‌：更适用于静态或相对稳定的场景，其中数据库表结构、实体类等在开发阶段就已经明确，并且在使用过程中不会发生频繁变化。


- 二、针对产品不同‌  
  AnyLine：通常不会直接用来开发面向终端用户的产品（如ERP、CRM等），而是用来开发中间产品（如低代码平台），让用户通过中间产品来生成最终产品。例如，可以使用AnyLine开发一个自定义查询分析工具，让用户根据业务需求生成动态报表。  
  传统ORM‌：则更常用于直接开发面向终端用户的应用系统，通过ORM框架提供的对象关系映射功能，简化数据库操作，提高开发效率。


- 三、操作对象不同‌  
  AnyLine：主要操作元数据，因为在项目开发之初可能还没有一个明确的产品，也就没有具体的对象及属性可操作。AnyLine通过操作元数据来帮助用户实现对业务数据的设计与操作。‌  
  传统ORM‌：则主要操作具体的实体类及其属性，这些实体类与数据库表结构相对应，通过ORM框架提供的映射关系进行数据库操作。


- 四、面向用户不同‌  
  AnyLine：面向的底层设计人员，特别是那些需要设计动态、灵活的应用系统的用户。AnyLine提供了强大的动态元数据引擎和结果集操作能力，以满足这些用户对系统灵活性和可扩展性的需求。‌  
  传统ORM‌：则更面向广大开发人员，特别是那些需要快速开发、维护关系型数据库应用系统的用户。ORM框架通过提供对象关系映射功能和各种高级特性（如事务管理、缓存等），降低了数据库操作的复杂度，提高了开发效率。


- 五、对用户要求不同‌  
  AnyLine：要求用户（特别是开发设计人员）对系统的动态性和灵活性有较高的认识和理解。由于AnyLine主要操作元数据并支持动态场景，因此用户需要具备一定的元数据操作能力和对业务需求的深入理解。‌  
  传统ORM‌：则相对更易于上手和使用。ORM框架通过提供直观的映射关系和面向对象的数据库操作方式，降低了数据库操作的门槛。即使是没有丰富数据库操作经验的开发人员，也能通过ORM框架快速上手并开发出功能完善的应用系统。

  

## 误解
当然我们并不是要抛弃Entity或ORM，不同的场景确实需要不同的解决方案，而 AnyLine 的设计理念正是为了提供灵活性和扩展性，
同时不排斥传统的 Entity 或 ORM 使用。

AnyLine 与 Entity/ORM 互补而非替代‌：  
Entity/ORM 在 ‌可预知、固定‌ 的场景下具有强类型、编译时检查、代码可读性强等优势，适合业务逻辑稳定、数据结构明确的场景。  
AnyLine 则专注于 ‌动态、运行时‌ 的场景，如数据中台、多数据源、动态查询条件、结果集灵活处理等，解决传统 ORM 在这些场景下的不足。  

## 如何使用
数据操作***不要***再从生成xml/dao/service以及各种配置各种O开始  
默认的service已经提供了大部分的数据库操作功能。  
操作过程大致如下:
```
DataSet set = service.querys("HR_USER(ID, NM)", 
    condition(true, "anyline根据约定自动生成的=, in, like等查询条件"));  
```
这里的查询条件不再需要各种配置, 各种if else foreach标签  
Anyline会自动生成, 生成规则可以参考这里的[【约定规则】](http://doc.anyline.org/s?id=p298pn6e9o1r5gv78acvic1e624c62387f2c45dd13bb112b34176fad5a868fa6a4)  
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
## 应用场景
### 一、低代码后台  
**‌动态数据源与元数据操作**  
支持运行时动态注册和切换数据源，能够自动生成SQL语句（包括DDL、DML、DQL），并读写元数据。这一特性使得开发者在构建低代码平台时，能够轻松实现对不同数据源的访问和操作，无需手动编写复杂的数据库访问代码。同时，还支持对比数据库结构差异，帮助开发者快速识别和解决数据不一致的问题。  

**动态表单与自定义查询**     
在低代码场景中，动态表单和自定义查询是常见的需求。提供了强大的动态SQL合成工具，支持开发者快速构建动态表单，并根据业务需求自定义查询条件。用户可以通过简单的拖拽和配置，即可实现对表单字段的增删改查操作，大大提高了开发效率。  

**可视化数据源与报表输出**      
能够处理动态属性，并适配前端的多维度多结构数据转换，为低代码平台提供可视化数据源支持。开发者可以与前端可视化框架结合使用，快速构建数据可视化应用。同时，还支持自定义报表输出，用户可以根据业务需求自定义报表样式和内容，实现数据的精准展示和分析。  

**简化开发流程与降低技术门槛**  
简化低代码平台的开发流程。开发者无需深入了解底层数据库结构和访问机制，只需通过简单的配置和API调用，即可实现对数据的高效访问和操作。降低了低代码平台的技术门槛，使得更多非专业开发者也能够参与到平台的建设和维护中来。

**提升平台灵活性与可扩展性**  
AnyLine的灵活性和可扩展性为低代码平台提供了强大的支持。随着业务需求的不断变化和升级，开发者可以通过简单的配置和代码调整，即可实现对AnyLine功能的扩展和优化。使低代码平台能够持续适应业务发展的需求。  

### 二、动态报表生成  
在财务、库存等ERP模块中，用户经常需要输出不同格式的报表，并根据不同维度查询统计数据。
‌动态数据源适配‌：动态注册和切换数据源，支持多种关系型与非关系型数据库，使得报表生成可以灵活地基于不同的数据源。  
‌元数据管理‌：提供强大的元数据管理功能，能够动态地获取和更新数据库的表结构、字段属性等信息，为报表生成提供准确的数据模型。  
‌动态SQL生成‌：根据用户的查询条件和报表格式要求，AnyLine能够自动生成相应的SQL语句，从数据库中检索所需数据，大大提高了报表生成的效率和准确性。

### 三、数据中台  
中台项目通常涉及各种动态异构数据源的DDL/DML及元数据管理，特别是一些小众及国产数据库。  
‌跨数据库兼容性‌：原生支持多种数据库，包括主流的关系型数据库和非关系型数据库，以及小众和国产数据库，使得中台项目能够无缝集成各种数据源。  
‌动态表结构管理‌：支持运行时动态注册和切换数据源，能够动态地管理数据库的表结构，适应中台项目中频繁的数据源变更需求。  
‌元数据版本控制‌：提供元数据版本控制功能，能够记录表结构的变更历史，方便中台项目进行数据回溯和恢复。

### 四、可视化数据源  
**处理动态属性**  
在可视化数据源场景中，数据往往具有动态性，即数据的属性、结构和类型可能会随着业务需求的变化而变化。AnyLine通过其动态数据结构适配能力，能够灵活地处理这些动态属性。它可以根据数据源的变化，自动调整数据模型，确保数据在前端展示时的准确性和一致性。适配前端的多维度多结构数据转换  
前端展示通常需要将数据按照特定的维度和结构进行转换和处理。AnyLine提供了丰富的数据转换和映射功能，能够支持前端所需的各种数据转换操作。无论是简单的数据类型转换，还是复杂的数据结构重组，AnyLine都能轻松应对。这使得前端开发人员能够更加专注于界面的设计和交互体验，而无需花费大量时间在数据转换上。  

**生成动态图表**    
AnyLine能够根据用户的自定义统计方式和维度，自动生成相应的DQL（数据查询语言）语句，从数据库中检索所需数据。有助于生成大量动态图表。用户可以根据自己的业务需求，灵活地选择统计方式和维度，AnyLine则会自动处理数据查询和转换，生成符合要求的图表数据格式。    
在提高图表的生成效率的同时确保图表数据的准确性和实时性。    

**DataRow和DataSet**    
**1).内置大量数据计算公式**    
公式涵盖了常见的数学运算、统计分析、日期时间处理等多个方面，能够满足用户在数据处理和分析过程中的各种需求。通过这些公式，用户可以快速实现数据的聚合、行列转换等操作，无需编写复杂的代码或依赖外部工具。  

**2). 快速实现数据聚合**  
数据聚合是数据分析中的一项重要任务，它涉及对大量数据进行分组、求和、平均值计算等操作。DataRow和DataSet通过内置的数据计算公式，能够轻松实现这些聚合操作。用户只需选择相应的公式和数据源，即可快速得到所需的聚合结果。这不仅提高了数据分析的效率，还确保了结果的准确性和一致性。  

**3). 行列转换功能**  
在数据可视化过程中，有时需要将数据的行列进行转换，以满足特定的展示需求。的DataRow和DataSet提供了灵活的行列转换功能。用户可以根据需要，轻松地将数据的行转换为列，或将列转换为行。这一功能在处理复杂数据结构或需要特定展示格式的数据时特别有用。  

**4). 类SQL过滤功能**  
除了内置的数据计算公式外，的DataRow和DataSet还支持类SQL过滤功能。用户可以使用类似SQL的语法来筛选数据，这使得数据处理过程更加直观和易于理解。通过类SQL过滤功能，用户可以快速定位到所需的数据，并进行进一步的分析和处理。  

**5). 内存计算**  
DataRow和DataSet组件在内存中进行计算，可以快速实现数据的聚合、行列转换等操作。  


### 五、物联网车联网数据处理

**动态管理数据库表结构**  
在物联网和车联网数据处理中，数据通常具有时序性，即数据是按照时间顺序产生的。时序数据库（如InfluxDB、Prometheus等）非常适合存储和处理这类数据，因为它们能够高效地处理时间序列数据，并提供快速的查询性能。然而，时序数据库的结构通常较为简单，可能无法满足复杂业务系统的需求。  
AnyLine能够动态地管理数据库表结构，根据数据的变更自动生成和执行DDL语句。这意味着当物联网或车联网中的数据发生变化时，可以自动调整数据库表结构，以适应这些变化。这大大简化了数据库管理过程，并确保了数据的准确性和一致性。  

**高效数据处理能力**  
物联网和车联网产生的数据量通常非常大，且需要实时处理。DataRow/DataSet通过内存计算提供了高效的数据处理能力，能够快速地处理这些大量数据。  
此外，还提供了丰富的数据转换和映射功能，能够轻松实现数据的清洗、聚合、转换等操作。这使得物联网和车联网数据在进入业务系统之前，可以进行必要的预处理和转换，以满足业务系统的需求。  

**实时性和准确性保障**  
在物联网和车联网场景中，数据的实时性和准确性至关重要。AnyLine通过其高效的数据处理能力和动态数据库表结构管理功能，确保了数据的实时性和准确性。它能够实时地接收和处理物联网和车联网产生的数据，并根据业务需求进行必要的转换和聚合。  

**与时序数据库的无缝集成**  
提供了各个时序数据库的原生驱动集成。可以直接访问时序数据库中的数据，进行必要的处理和分析，简化数据处理流程。  

### 六、数据清洗和批量处理  
在数据批处理场景中，通常需要处理大量的数据，并进行复杂的数据转换和加载操作。各种结构的数据、更多的是不符合标准甚至是错误的结构  这种场景下需要一个灵活的数据结构来统一处理各种结构的数据  
**‌高效的数据处理能力‌**  
支持批量获取数据，能够显著提高数据处理的效率。  

**‌复杂的结果集操作‌**
DataSet/DataRow 内置了大量数学计算公式、可以像操作数据库一样操作内存数据。简化加速数据处理过程，如聚合、去重、搜索等

**‌灵活的数据结构‌**  
提供灵活的数据结构来处理各种结构的数据，包括不符合标准甚至是错误的结构，能简化加速数据处理过程。  

### 七、信创改造工程    
在信创改造工程中，通常需要将现有的业务系统迁移到国产数据库上，并确保系统的稳定性和兼容性。  

**‌国产化数据库适配‌**  
原生支持多种国产数据库，能够无缝迁移现有的业务系统到国产数据库上。

**SQL语法统一‌**
支持智能方言转换功能，能够将不同数据库的SQL语法统一起来，确保业务系统的兼容性。\

**‌平滑迁移和自动比对‌**  
支持平滑迁移功能，能够在双跑期间自动比对查询结果，差异数据自动修复，确保迁移过程的准确性和稳定性。

### 八、工作流
AnyLine在工作流中的应用提供灵活的自定义支持，使工作流的管理和自定义变得更加简单和高效  

**‌自定义表单‌**  
在工作流中，经常需要根据不同的业务需求定义各种表单。AnyLine允许用户通过简单的配置和拖拽操作来定义表单的字段。意味着用户可以根据实际需要添加、删除或修改表单字段，而无需编写代码。  

**‌自定义查询条件‌**  
查询条件是工作流中数据筛选和过滤的关键。AnyLine提供了灵活的查询条件定义方式，用户可以通过配置来指定查询条件，如等于、大于、小于、包含等。此外，AnyLine还支持复杂的组合查询条件，使得数据筛选更加精确和高效。  

**‌自定义数据结构‌**  
在工作流中，数据结构可能因业务需求的不同而有所变化。AnyLine允许用户自定义数据结构，以适应不同的数据处理和分析需求。用户可以根据实际需要定义数据的字段、类型、长度等信息，从而确保数据的准确性和一致性。  

**‌简化开发过程‌**  
由于不需要预先定义实体类和相关的配置文件，简化了开发过程。开发人员可以更加专注于业务逻辑的实现，而不是花费大量时间在数据库表结构和查询条件的定义上。  

**‌提高灵活性‌**  
动态ORM特性使得工作流中的表单、查询条件和数据结构可以根据实际需求进行灵活调整。提高了工作流的适应性和可扩展性，使得业务系统能够更好地满足不断变化的业务需求。

### 九、网络爬虫数据解析  
**动态解析不同数据结构**  
网络爬虫需要从不同的网站和页面中抓取数据，而这些网站和页面的数据结构往往各不相同。AnyLine的动态元数据引擎能够自动识别各种异构数据结构，无需预先定义数据结构或编写针对特定网站的解析代码。这使得AnyLine能够灵活地应对不同网站和数据结构的解析需求，大大提高了网络爬虫的数据解析能力。  

**高效灵活的HTML解析**  
提供了强大的HTML解析功能，能够快速地解析HTML页面中的数据。它支持类似正则表达式又比正则简单，使得用户可以方便地提取所需信息。内置了丰富的数学计算公式和统计函数，可以对解析后的数据进行进一步的处理和分析。  

**简化数据存储流程**  
AnyLine支持将解析后的数据直接存储到数据库中，无需用户进行额外的数据转换或处理。提供了多种数据库连接选项，包括关系型数据库（如MySQL、Oracle等）和非关系型数据库（如MongoDB、Redis等），使得用户可以根据自己的需求选择合适的数据库进行数据存储。  

**提高数据解析效率**  
AnyLine的内存计算能力可以提高数据解析和处理速度。可以在内存中完成数据聚合、去重、排序等操作，避免了频繁的数据库IO操作，从而提高了数据解析和处理的效率。  


### 十、异构数据库迁移同步  
异构数据库迁移同步时，主要面临以下挑战：  

**DDL差异‌**  
不同数据库系统的DDL语法可能存在差异，如创建表、索引、视图等语句的写法可能不同。

**DML差异‌**  
DML操作如插入、更新、删除等也可能因数据库系统的不同而有所差异。

**‌数据类型差异‌**  
不同数据库系统支持的数据类型及其表示方式可能不同，需要进行数据类型转换。  

**AnyLine在其中的应用**    
**100+的数据库兼容性‌**   
原生支持多种数据库系统，包括主流的关系型数据库（如MySQL、Oracle、PostgreSQL等）和非关系型数据库，以及国产数据库。    
能够轻松应对异构数据库迁移同步中的数据库差异问题，无需进行额外的定制开发。    

**‌动态数据结构适配‌**    
具备强大的动态数据结构适配能力。    
能够根据源数据库和目标数据库的结构差异，自动生成相应的SQL语句，实现表结构的创建和数据的复制。  
简化了迁移同步的过程，降低了人工干预的需求。  

**数据转换与映射‌**  
在异构数据库迁移同步中，数据转换与映射是一个关键环节。  
AnyLine提供了丰富的数据转换和映射功能，能够自动处理数据类型差异、数据格式转换等问题。  
这确保了数据在迁移过程中的完整性和一致性，避免了数据丢失或错误的情况。
 
### 十一、项目快速交付场景  
在需求不明确或频繁变更的项目中，项目团队通常会面临以下挑战：  
**‌需求不确定性‌**  
项目初期，需求可能尚未完全明确，或者随着项目的进展，需求会发生变更。

**‌开发效率‌**  
需求的不确定性可能导致开发过程中的反复修改和调整，从而影响开发效率。

**‌交付质量‌**  
频繁的需求变更可能增加项目的复杂性和出错的风险，从而影响交付质量。

**数据库类型不确定**  
特别是随着信创进度的推进，许多项目要求国产化替换

**anyline在项目快速交付中的优势**  
**‌动态ORM**   
AnyLine的动态ORM（对象关系映射）特性允许项目团队根据实际需求动态地调整数据结构。  
这意味着，即使需求发生变化，团队也无需重新设计数据库表结构或编写大量的数据转换代码。  
动态ORM能够自动处理数据表与对象之间的映射关系，从而简化开发过程。  

**‌灵活应对需求变更‌**    
由于AnyLine支持动态数据结构适配和高效的数据转换与映射功能，项目团队可以更加灵活地应对需求变更。  
当需求发生变化时，团队可以快速调整业务逻辑和数据查询条件，而无需对整体架构进行大规模修改。  

**100+的数据库适配及兼容**    
AnyLine原生支持多种数据库系统，包括但不限于主流的关系型数据库（如MySQL、Oracle、PostgreSQL等）和非关系型数据库，以及各类国产数据库。能够轻松应对数据库类型的变更，无需担心与新数据库不兼容的问题。

**‌自动化的数据迁移与同步**    
当数据库类型发生变化时，通过自动化的数据迁移与同步功能。能够根据源数据库和目标数据库的结构差异，自动生成相应的迁移脚本，实现数据的无缝迁移。减少了人工迁移的繁琐和错误风险，提高了迁移的效率。  

**‌动态数据结构适配‌**  
动态数据结构适配能力在数据库类型变更时同样发挥着重要作用。能够根据新数据库的类型和结构，自动调整数据表、索引、视图等数据库对象的定义，确保数据在新数据库中的正确存储和高效访问。  

**自动的数据转换与映射‌**    
在数据库类型变更过程中，往往需要进行数据类型转换和数据格式调整。AnyLine提供了丰富的数据转换和映射功能，能够自动处理这些差异，确保数据在迁移后的完整性和一致性。避免因数据转换错误而导致的项目风险。  

##  关于数据库的适配  [【更多查看】](http://doc.anyline.org/dbs)  
[【示例源码】](/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect)
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mysql"><img alt="MYSQL" src="https://gitee.com/anyline/service/raw/master/db/mysql.jpg" width="100"/>MYSQL</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-postgresql"><img alt="PostgreSQL" src="https://gitee.com/anyline/service/raw/master/db/postgres.png" width="100"/>PostgreSQL</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oracle"><img alt="ORACLE" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>ORACLE</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mssql"><img alt="MSSQL" src="https://gitee.com/anyline/service/raw/master/db/mssql.jpg" width="100"/>MSSQL</a>
<a style="display:inline-block;" href="https://www.mongodb.com/"><img alt="MongoDB" src="https://gitee.com/anyline/service/raw/master/db/mongodb.svg" width="100"/>MongoDB</a>
<a style="display:inline-block;" href="https://redis.com/"><img alt="Redis" src="https://gitee.com/anyline/service/raw/master/db/redis.svg" width="100"/>Redis</a>
<a style="display:inline-block;" href="https://www.elastic.co/elasticsearch/"><img alt="ElasticSearch" src="https://gitee.com/anyline/service/raw/master/db/es.svg" width="100"/>ElasticSearch</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-db2"><img alt="DB2" src="https://gitee.com/anyline/service/raw/master/db/db2.webp" width="100"/>DB2</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-dm"><img alt="DM" src="https://gitee.com/anyline/service/raw/master/db/dm.webp" width="100"/>DM(武汉达梦数据库股份有限公司)</a>
<a style="display:inline-block;" href="https://www.gbase.cn/"><img alt="GBase8a" src="https://gitee.com/anyline/service/raw/master/db/gbase.webp" width="100"/>GBase8a(天津南大通用数据技术股份有限公司)</a>
<a style="display:inline-block;" href="https://www.gbase.cn/"><img alt="GBase8c" src="https://gitee.com/anyline/service/raw/master/db/gbase.webp" width="100"/>GBase8c(天津南大通用数据技术股份有限公司)</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-GBase8s"><img alt="GBase8s" src="https://gitee.com/anyline/service/raw/master/db/gbase.webp" width="100"/>GBase8s(天津南大通用数据技术股份有限公司)</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oscar"><img alt="oscar" src="https://gitee.com/anyline/service/raw/master/db/oscar.png" width="100"/>oscar</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-sqlite"><img alt="SQLite" src="https://gitee.com/anyline/service/raw/master/db/sqlite.gif" width="100"/>SQLite</a>
<a style="display:inline-block;" href="https://www.snowflake.com/"><img alt="Snowflake" src="https://gitee.com/anyline/service/raw/master/db/snowflake.svg" width="100"/>Snowflake</a>
<a style="display:inline-block;" href="https://cassandra.apache.org/"><img alt="Cassandra" src="https://gitee.com/anyline/service/raw/master/db/cassandra.svg" width="100"/>Cassandra</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mariadb"><img alt="MariaDB" src="https://gitee.com/anyline/service/raw/master/db/mariadb.png" width="100"/>MariaDB</a>
<a style="display:inline-block;" href="https://www.splunk.com/"><img alt="Splunk" src="https://gitee.com/anyline/service/raw/master/db/splunk.svg" width="100"/>Splunk</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/en-us/products/azure-sql/database/"><img alt="AzureSQL" src="https://gitee.com/anyline/service/raw/master/db/microsoft.png" width="100"/>AzureSQL</a>
<a style="display:inline-block;" href="https://aws.amazon.com/dynamodb/"><img alt="AmazonDynamoDB" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonDynamoDB</a>
<a style="display:inline-block;" href="https://www.databricks.com/"><img alt="Databricks" src="https://gitee.com/anyline/service/raw/master/db/databricks.svg" width="100"/>Databricks</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hive"><img alt="Hive" src="https://gitee.com/anyline/service/raw/master/db/hive.svg" width="100"/>Hive</a>
<a style="display:inline-block;" href="https://www.microsoft.com/en-us/microsoft-365/access"><img alt="Access" src="https://gitee.com/anyline/service/raw/master/db/access.webp" width="100"/>Access</a>
<a style="display:inline-block;" href="https://cloud.google.com/bigquery/"><img alt="GoogleBigQuery" src="https://gitee.com/anyline/service/raw/master/db/googlecloud.webp" width="100"/>GoogleBigQuery</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-highgo"><img alt="HighGo" src="https://gitee.com/anyline/service/raw/master/db/highgo.svg" width="100"/>HighGo(瀚高基础软件股份有限公司)</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-mssql"><img alt="MSSQL2000" src="https://gitee.com/anyline/service/raw/master/db/mssql.jpg" width="100"/>MSSQL2000</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-neo4j"><img alt="Neo4j" src="https://gitee.com/anyline/service/raw/master/db/neo4j.webp" width="100"/>Neo4j</a>
<a style="display:inline-block;" href="https://www.polardbx.com/home"><img alt="PolarDB" src="https://gitee.com/anyline/service/raw/master/db/polardb.webp" width="100"/>PolarDB(阿里云计算有限公司)</a>
<a style="display:inline-block;" href=""><img alt="Sybase" src="https://gitee.com/anyline/service/raw/master/db/sap.svg" width="100"/>Sybase</a>
<a style="display:inline-block;" href="https://www.teradata.com/"><img alt="TeraData" src="https://gitee.com/anyline/service/raw/master/db/teradata.ico" width="100"/>TeraData</a>
<a style="display:inline-block;" href="https://www.claris.com/filemaker/"><img alt="FileMaker" src="https://gitee.com/anyline/service/raw/master/db/filemarker.png" width="100"/>FileMaker</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hana"><img alt="HANA" src="https://gitee.com/anyline/service/raw/master/db/hana.png" width="100"/>HANA</a>
<a style="display:inline-block;" href="https://solr.apache.org/"><img alt="Solr" src="https://gitee.com/anyline/service/raw/master/db/solr.svg" width="100"/>Solr</a>
<a style="display:inline-block;" href="https://www.sap.com/products/sybase-ase.html"><img alt="Adaptive" src="https://gitee.com/anyline/service/raw/master/db/sap.svg" width="100"/>Adaptive</a>
<a style="display:inline-block;" href="https://hbase.apache.org/"><img alt="Hbase" src="https://gitee.com/anyline/service/raw/master/db/hbase.png" width="100"/>Hbase</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/services/cosmos-db"><img alt="AzureCosmos" src="https://gitee.com/anyline/service/raw/master/db/microsoft.png" width="100"/>AzureCosmos</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-influxdb"><img alt="InfluxDB" src="https://gitee.com/anyline/service/raw/master/db/influx.svg" width="100"/>InfluxDB</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-postgis"><img alt="PostGIS" src="https://gitee.com/anyline/service/raw/master/db/postgis.webp" width="100"/>PostGIS</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/services/synapse-analytics/"><img alt="AzureSynapse" src="https://gitee.com/anyline/service/raw/master/db/microsoft.png" width="100"/>AzureSynapse</a>
<a style="display:inline-block;" href="http://www.firebirdsql.org/"><img alt="Firebird" src="https://gitee.com/anyline/service/raw/master/db/firebird.png" width="100"/>Firebird</a>
<a style="display:inline-block;" href="https://www.couchbase.com/"><img alt="Couchbase" src="https://gitee.com/anyline/service/raw/master/db/couchbase.svg" width="100"/>Couchbase</a>
<a style="display:inline-block;" href="https://aws.amazon.com/redshift/"><img alt="AmazonRedshift" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonRedshift</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/informix"><img alt="Informix" src="https://gitee.com/anyline/service/raw/master/db/informix.webp" width="100"/>Informix</a>
<a style="display:inline-block;" href="http://www.memcached.org/"><img alt="Memcached" src="https://gitee.com/anyline/service/raw/master/db/memcached.webp" width="100"/>Memcached</a>
<a style="display:inline-block;" href="https://spark.apache.org/sql/"><img alt="Spark" src="https://gitee.com/anyline/service/raw/master/db/spark.svg" width="100"/>Spark</a>
<a style="display:inline-block;" href="https://www.cloudera.com/products/open-source/apache-hadoop/impala.html"><img alt="Cloudera " src="https://gitee.com/anyline/service/raw/master/db/cloudera.png" width="100"/>Cloudera </a>
<a style="display:inline-block;" href="https://firebase.google.cn/products/realtime-database/"><img alt="Firebase" src="https://gitee.com/anyline/service/raw/master/db/firebase.svg" width="100"/>Firebase</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-clickhouse"><img alt="ClickHouse" src="https://gitee.com/anyline/service/raw/master/db/clickhouse.png" width="100"/>ClickHouse</a>
<a style="display:inline-block;" href="https://prestodb.io/"><img alt="Presto" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Presto</a>
<a style="display:inline-block;" href="https://www.vertica.com/"><img alt="Vertica" src="https://gitee.com/anyline/service/raw/master/db/vertica.png" width="100"/>Vertica</a>
<a style="display:inline-block;" href="http://www.dbase.com/"><img alt="dbase" src="https://gitee.com/anyline/service/raw/master/db/dbase.png" width="100"/>dbase</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/netezza"><img alt="Netezza" src="https://gitee.com/anyline/service/raw/master/db/ibm.svg" width="100"/>Netezza</a>
<a style="display:inline-block;" href="https://github.com/opensearch-project"><img alt="OpenSearch" src="https://gitee.com/anyline/service/raw/master/db/opensearch.ico" width="100"/>OpenSearch</a>
<a style="display:inline-block;" href="https://flink.apache.org/"><img alt="Flink" src="https://gitee.com/anyline/service/raw/master/db/flink.png" width="100"/>Flink</a>
<a style="display:inline-block;" href="https://couchdb.apache.org/"><img alt="CouchDB" src="https://gitee.com/anyline/service/raw/master/db/couchdb.png" width="100"/>CouchDB</a>
<a style="display:inline-block;" href="https://firebase.google.com/products/firestore/"><img alt="GoogleFirestore" src="https://gitee.com/anyline/service/raw/master/db/googlecloud.webp" width="100"/>GoogleFirestore</a>
<a style="display:inline-block;" href="https://greenplum.org/"><img alt="Greenplum" src="https://gitee.com/anyline/service/raw/master/db/greenplum.png" width="100"/>Greenplum</a>
<a style="display:inline-block;" href="https://aws.amazon.com/rds/aurora/"><img alt="AmazonAurora" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonAurora</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-h2"><img alt="H2" src="https://gitee.com/anyline/service/raw/master/db/h2db.png" width="100"/>H2</a>
<a style="display:inline-block;" href="https://kx.com/"><img alt="Kdb" src="https://gitee.com/anyline/service/raw/master/db/kx.svg" width="100"/>Kdb</a>
<a style="display:inline-block;" href="https://etcd.io/"><img alt="etcd" src="https://gitee.com/anyline/service/raw/master/db/etcd.svg" width="100"/>etcd</a>
<a style="display:inline-block;" href="https://realm.io/"><img alt="Realm" src="https://gitee.com/anyline/service/raw/master/db/realm.svg" width="100"/>Realm</a>
<a style="display:inline-block;" href="https://www.marklogic.com/"><img alt="MarkLogic" src="https://gitee.com/anyline/service/raw/master/db/marklogic.png" width="100"/>MarkLogic</a>
<a style="display:inline-block;" href="https://hazelcast.com/"><img alt="Hazelcast" src="https://gitee.com/anyline/service/raw/master/db/hazelcast.svg" width="100"/>Hazelcast</a>
<a style="display:inline-block;" href="https://prometheus.io/"><img alt="Prometheus" src="https://gitee.com/anyline/service/raw/master/db/prometheus.svg" width="100"/>Prometheus</a>
<a style="display:inline-block;" href="https://www.oracle.com/business-analytics/essbase.html"><img alt="OracleEssbase" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>OracleEssbase</a>
<a style="display:inline-block;" href="https://www.datastax.com/products/datastax-enterprise"><img alt="Datastax" src="https://gitee.com/anyline/service/raw/master/db/datastax.svg" width="100"/>Datastax</a>
<a style="display:inline-block;" href="https://aerospike.com/"><img alt="Aerospike" src="https://gitee.com/anyline/service/raw/master/db/aerospike.webp" width="100"/>Aerospike</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/services/data-explorer/"><img alt="AzureDataExplorer" src="https://gitee.com/anyline/service/raw/master/db/microsoft.png" width="100"/>AzureDataExplorer</a>
<a style="display:inline-block;" href="https://www.algolia.com/"><img alt="Algolia" src="https://gitee.com/anyline/service/raw/master/db/algolia-mark-blue.svg" width="100"/>Algolia</a>
<a style="display:inline-block;" href="https://www.ehcache.org/"><img alt="Ehcache" src="https://gitee.com/anyline/service/raw/master/db/ehcache.png" width="100"/>Ehcache</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-derby"><img alt="Derby" src="https://gitee.com/anyline/service/raw/master/db/derby.webp" width="100"/>Derby</a>
<a style="display:inline-block;" href="https://www.cockroachlabs.com/"><img alt="CockroachDB" src="https://gitee.com/anyline/service/raw/master/db/cockroach.avif" width="100"/>CockroachDB</a>
<a style="display:inline-block;" href="https://www.scylladb.com/"><img alt="ScyllaDB" src="https://gitee.com/anyline/service/raw/master/db/scylladb.svg" width="100"/>ScyllaDB</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/en-us/services/search/"><img alt="AzureSearch" src="https://gitee.com/anyline/service/raw/master/db/microsoft.png" width="100"/>AzureSearch</a>
<a style="display:inline-block;" href="https://www.embarcadero.com/products/interbase"><img alt="Interbase" src="https://gitee.com/anyline/service/raw/master/db/embarcadero.webp" width="100"/>Interbase</a>
<a style="display:inline-block;" href="https://azure.microsoft.com/en-us/services/storage/tables/"><img alt="AzureTableStorage" src="https://gitee.com/anyline/service/raw/master/db/microsoft.png" width="100"/>AzureTableStorage</a>
<a style="display:inline-block;" href="http://sphinxsearch.com/"><img alt="Sphinx" src="https://gitee.com/anyline/service/raw/master/db/sphinx.png" width="100"/>Sphinx</a>
<a style="display:inline-block;" href="https://jackrabbit.apache.org/"><img alt="Jackrabbit" src="https://gitee.com/anyline/service/raw/master/db/jackrabbit.gif" width="100"/>Jackrabbit</a>
<a style="display:inline-block;" href="https://trino.io/"><img alt="Trino" src="https://gitee.com/anyline/service/raw/master/db/trino.svg" width="100"/>Trino</a>
<a style="display:inline-block;" href="https://www.singlestore.com/"><img alt="SingleStore" src="https://gitee.com/anyline/service/raw/master/db/singlestore.svg" width="100"/>SingleStore</a>
<a style="display:inline-block;" href="https://www.actian.com/databases/ingres/"><img alt="Ingres" src="https://gitee.com/anyline/service/raw/master/db/ingres.png" width="100"/>Ingres</a>
<a style="display:inline-block;" href="https://virtuoso.openlinksw.com/"><img alt="Virtuoso" src="https://gitee.com/anyline/service/raw/master/db/virtuoso.png" width="100"/>Virtuoso</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-timescale"><img alt="Timescale" src="https://gitee.com/anyline/service/raw/master/db/timescale.svg" width="100"/>Timescale</a>
<a style="display:inline-block;" href="https://cloud.google.com/datastore/"><img alt="GoogleDatastore" src="https://gitee.com/anyline/service/raw/master/db/googlecloud.webp" width="100"/>GoogleDatastore</a>
<a style="display:inline-block;" href="https://github.com/graphite-project/graphite-web"><img alt="Graphite" src="https://gitee.com/anyline/service/raw/master/db/graphiteweb.png" width="100"/>Graphite</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-hsqldb"><img alt="HyperSQL" src="https://gitee.com/anyline/service/raw/master/db/hypersql.png" width="100"/>HyperSQL</a>
<a style="display:inline-block;" href="https://www.softwareag.com/en_corporate/platform/adabas-natural.html"><img alt="Adabas" src="https://gitee.com/anyline/service/raw/master/db/softwareag.svg" width="100"/>Adabas</a>
<a style="display:inline-block;" href=""><img alt="RiakKV" src="https://gitee.com/anyline/service/raw/master/db/riak.png" width="100"/>RiakKV</a>
<a style="display:inline-block;" href="https://www.sap.com/products/technology-platform/sybase-iq-big-data-management.html"><img alt="SAPIQ" src="https://gitee.com/anyline/service/raw/master/db/sap.svg" width="100"/>SAPIQ</a>
<a style="display:inline-block;" href="https://www.arangodb.com/"><img alt="ArangoDB" src="https://gitee.com/anyline/service/raw/master/db/arangodb.png" width="100"/>ArangoDB</a>
<a style="display:inline-block;" href="https://jena.apache.org/documentation/tdb/index.html"><img alt="Jena" src="https://gitee.com/anyline/service/raw/master/db/jena.png" width="100"/>Jena</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-ignite"><img alt="Ignite" src="https://gitee.com/anyline/service/raw/master/db/ignite.svg" width="100"/>Ignite</a>
<a style="display:inline-block;" href="https://cloud.google.com/bigtable/"><img alt="GoogleBigtable" src="https://gitee.com/anyline/service/raw/master/db/googlecloud.webp" width="100"/>GoogleBigtable</a>
<a style="display:inline-block;" href="https://pingcap.com/"><img alt="TiDB" src="https://gitee.com/anyline/service/raw/master/db/tidb.svg" width="100"/>TiDB(PingCAP)</a>
<a style="display:inline-block;" href="https://accumulo.apache.org/"><img alt="Accumulo" src="https://gitee.com/anyline/service/raw/master/db/accumulo.png" width="100"/>Accumulo</a>
<a style="display:inline-block;" href="https://rocksdb.org/"><img alt="RocksDB" src="https://gitee.com/anyline/service/raw/master/db/rocksdb.svg" width="100"/>RocksDB</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/nosql/technologies/nosql/"><img alt="OracleNoSQL" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>OracleNoSQL</a>
<a style="display:inline-block;" href="https://www.progress.com/openedge"><img alt="OpenEdge" src="https://gitee.com/anyline/service/raw/master/db/progress.ico" width="100"/>OpenEdge</a>
<a style="display:inline-block;" href="https://duckdb.org/"><img alt="DuckDB" src="https://gitee.com/anyline/service/raw/master/db/duckdb.png" width="100"/>DuckDB</a>
<a style="display:inline-block;" href="https://www.dolphindb.com/"><img alt="DolphinDB" src="https://gitee.com/anyline/service/raw/master/db/dolphindb.webp" width="100"/>DolphinDB</a>
<a style="display:inline-block;" href="https://www.vmware.com/products/gemfire.html"><img alt="GemFire" src="https://gitee.com/anyline/service/raw/master/db/vmware.webp" width="100"/>GemFire</a>
<a style="display:inline-block;" href="https://orientdb.org/"><img alt="OrientDB" src="https://gitee.com/anyline/service/raw/master/db/orientdb.png" width="100"/>OrientDB</a>
<a style="display:inline-block;" href="https://cloud.google.com/spanner/"><img alt="GoogleSpanner" src="https://gitee.com/anyline/service/raw/master/db/googlecloud.webp" width="100"/>GoogleSpanner</a>
<a style="display:inline-block;" href="https://ravendb.net/"><img alt="RavenDB" src="https://gitee.com/anyline/service/raw/master/db/ravendb.svg" width="100"/>RavenDB</a>
<a style="display:inline-block;" href="https://www.sap.com/products/technology-platform/sql-anywhere.html"><img alt="Anywhere" src="https://gitee.com/anyline/service/raw/master/db/sap.svg" width="100"/>Anywhere</a>
<a style="display:inline-block;" href="https://www.intersystems.com/products/cache/"><img alt="Cache" src="https://gitee.com/anyline/service/raw/master/db/intersystems.svg" width="100"/>Cache</a>
<a style="display:inline-block;" href=""><img alt="ChinaMobileDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>ChinaMobileDB</a>
<a style="display:inline-block;" href=""><img alt="ChinaUnicomDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>ChinaUnicomDB</a>
<a style="display:inline-block;" href="https://www.cloudiip.com/"><img alt="CirroData" src="https://gitee.com/anyline/service/raw/master/db/cloudiip.png" width="100"/>CirroData</a>
<a style="display:inline-block;" href=""><img alt="FusionInsight" src="https://gitee.com/anyline/service/raw/master/db/huawei.svg" width="100"/>FusionInsight</a>
<a style="display:inline-block;" href="https://cloud.baidu.com/doc/DRDS/index.html"><img alt="GaiaDB" src="https://gitee.com/anyline/service/raw/master/db/baiduyun.webp" width="100"/>GaiaDB</a>
<a style="display:inline-block;" href="https://support.huaweicloud.com/gaussdb/index.html"><img alt="GaussDB100" src="https://gitee.com/anyline/service/raw/master/db/huawei.svg" width="100"/>GaussDB100</a>
<a style="display:inline-block;" href="https://support.huaweicloud.com/gaussdb/index.html"><img alt="GaussDB200" src="https://gitee.com/anyline/service/raw/master/db/huawei.svg" width="100"/>GaussDB200</a>
<a style="display:inline-block;" href="https://www.zte.com.cn/china/solutions_latest/goldendb.html"><img alt="GoldenDB" src="https://gitee.com/anyline/service/raw/master/db/zte.webp" width="100"/>GoldenDB</a>
<a style="display:inline-block;" href="https://www.greatdb.com/"><img alt="GreatDB" src="https://gitee.com/anyline/service/raw/master/db/greatdb.png" width="100"/>GreatDB(北京万里开源软件有限公司)</a>
<a style="display:inline-block;" href="https://www.hashdata.xyz/"><img alt="HashData" src="https://gitee.com/anyline/service/raw/master/db/hashdata.png" width="100"/>HashData</a>
<a style="display:inline-block;" href="https://www.hotdb.com/index"><img alt="HotDB" src="https://gitee.com/anyline/service/raw/master/db/hotdb.png" width="100"/>HotDB</a>
<a style="display:inline-block;" href="https://infinispan.org/"><img alt="Infinispan" src="https://gitee.com/anyline/service/raw/master/db/infinispan.png" width="100"/>Infinispan</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-kingbase"><img alt="KingBase" src="https://gitee.com/anyline/service/raw/master/db/kingbase.png" width="100"/>KingBase(北京人大金仓信息技术股份有限公司)</a>
<a style="display:inline-block;" href="https://www.hundsun.com/"><img alt="LightDB" src="https://gitee.com/anyline/service/raw/master/db/hundsun.ico" width="100"/>LightDB</a>
<a style="display:inline-block;" href="https://www.mogdb.io/"><img alt="MogDB" src="https://gitee.com/anyline/service/raw/master/db/mogdb.png" width="100"/>MogDB(云和恩墨)</a>
<a style="display:inline-block;" href="https://www.murongtech.com/"><img alt="MuDB" src="https://gitee.com/anyline/service/raw/master/db/murongtech.png" width="100"/>MuDB(沐融信息科技)</a>
<a style="display:inline-block;" href="https://www.boraydata.cn/"><img alt="RapidsDB" src="https://gitee.com/anyline/service/raw/master/db/boraydata.png" width="100"/>RapidsDB</a>
<a style="display:inline-block;" href="https://www.selectdb.com/"><img alt="SelectDB" src="https://gitee.com/anyline/service/raw/master/db/selectdb.png" width="100"/>SelectDB</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-sinodb"><img alt="SinoDB" src="https://gitee.com/anyline/service/raw/master/db/sinoregal.png" width="100"/>SinoDB</a>
<a style="display:inline-block;" href=""><img alt="StarDB" src="https://gitee.com/anyline/service/raw/master/db/stardb.webp" width="100"/>StarDB</a>
<a style="display:inline-block;" href=""><img alt="UbiSQL" src="https://gitee.com/anyline/service/raw/master/db/pingan.png" width="100"/>UbiSQL</a>
<a style="display:inline-block;" href="https://www.uxsino.com/"><img alt="UXDB" src="https://gitee.com/anyline/service/raw/master/db/uxsino.png" width="100"/>UXDB(北京优炫软件股份有限公司)</a>
<a style="display:inline-block;" href="https://docs.vastdata.com.cn/zh/"><img alt="Vastbase" src="https://gitee.com/anyline/service/raw/master/db/vastdata.png" width="100"/>Vastbase(北京海量数据技术股份有限公司)</a>
<a style="display:inline-block;" href="http://www.vsettan.com.cn/7824.html"><img alt="xigemaDB" src="https://gitee.com/anyline/service/raw/master/db/vsettan.png" width="100"/>xigemaDB</a>
<a style="display:inline-block;" href="https://dt.bestpay.com.cn/"><img alt="YiDB" src="https://gitee.com/anyline/service/raw/master/db/bestpay.png" width="100"/>YiDB</a>
<a style="display:inline-block;" href="https://www.xugudb.com/"><img alt="xugu" src="https://gitee.com/anyline/service/raw/master/db/xugu.png" width="100"/>xugu(成都虚谷伟业科技有限公司)</a>
<a style="display:inline-block;" href="https://maxdb.sap.com/"><img alt="MaxDB" src="https://gitee.com/anyline/service/raw/master/db/sap.svg" width="100"/>MaxDB</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/cloudant"><img alt="Cloudant" src="https://gitee.com/anyline/service/raw/master/db/ibm.svg" width="100"/>Cloudant</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/technologies/related/berkeleydb.html"><img alt="OracleBerkeley" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>OracleBerkeley</a>
<a style="display:inline-block;" href="https://www.yugabyte.com/"><img alt="YugabyteDB" src="https://gitee.com/anyline/service/raw/master/db/yugabyte.svg" width="100"/>YugabyteDB</a>
<a style="display:inline-block;" href="https://github.com/google/leveldb"><img alt="LevelDB" src="https://gitee.com/anyline/service/raw/master/db/leveldb.png" width="100"/>LevelDB</a>
<a style="display:inline-block;" href="https://www.pinecone.io/"><img alt="Pinecone" src="https://gitee.com/anyline/service/raw/master/db/pinecone.ico" width="100"/>Pinecone</a>
<a style="display:inline-block;" href="https://github.com/heavyai/heavydb"><img alt="HEAVYAI" src="https://gitee.com/anyline/service/raw/master/db/heavy.png" width="100"/>HEAVYAI</a>
<a style="display:inline-block;" href="https://memgraph.com/"><img alt="Memgraph" src="https://gitee.com/anyline/service/raw/master/db/memgraph.webp" width="100"/>Memgraph</a>
<a style="display:inline-block;" href="https://developer.apple.com/icloud/cloudkit/"><img alt="CloudKit" src="https://gitee.com/anyline/service/raw/master/db/appledev.svg" width="100"/>CloudKit</a>
<a style="display:inline-block;" href="https://rethinkdb.com/"><img alt="RethinkDB" src="https://gitee.com/anyline/service/raw/master/db/rethinkdb.png" width="100"/>RethinkDB</a>
<a style="display:inline-block;" href="https://www.exasol.com/"><img alt="EXASOL" src="https://gitee.com/anyline/service/raw/master/db/exasol.png" width="100"/>EXASOL</a>
<a style="display:inline-block;" href="https://drill.apache.org/"><img alt="Drill" src="https://gitee.com/anyline/service/raw/master/db/drill.png" width="100"/>Drill</a>
<a style="display:inline-block;" href="https://pouchdb.com/"><img alt="PouchDB" src="https://gitee.com/anyline/service/raw/master/db/pouchdb.svg" width="100"/>PouchDB</a>
<a style="display:inline-block;" href="https://phoenix.apache.org/"><img alt="Phoenix" src="https://gitee.com/anyline/service/raw/master/db/phoenix.ico" width="100"/>Phoenix</a>
<a style="display:inline-block;" href="https://www.enterprisedb.com/"><img alt="EDB" src="https://gitee.com/anyline/service/raw/master/db/edb.svg" width="100"/>EDB</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-tdengine"><img alt="TDengine" src="https://gitee.com/anyline/service/raw/master/db/tdengine.png" width="100"/>TDengine</a>
<a style="display:inline-block;" href="https://www.intersystems.com/products/intersystems-iris/"><img alt="IRIS" src="https://gitee.com/anyline/service/raw/master/db/intersystems.svg" width="100"/>IRIS</a>
<a style="display:inline-block;" href="https://oss.oetiker.ch/rrdtool/"><img alt="RRDtool" src="https://gitee.com/anyline/service/raw/master/db/rrdtool.png" width="100"/>RRDtool</a>
<a style="display:inline-block;" href="https://www.ontotext.com/"><img alt="GraphDB" src="https://gitee.com/anyline/service/raw/master/db/graphdb.png" width="100"/>GraphDB</a>
<a style="display:inline-block;" href="https://www.citusdata.com/"><img alt="Citus" src="https://gitee.com/anyline/service/raw/master/db/cutisdata.svg" width="100"/>Citus</a>
<a style="display:inline-block;" href="https://www.coveo.com"><img alt="Coveo" src="https://gitee.com/anyline/service/raw/master/db/coveo.svg" width="100"/>Coveo</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/ims"><img alt="IMS" src="https://gitee.com/anyline/service/raw/master/db/ibm.svg" width="100"/>IMS</a>
<a style="display:inline-block;" href="https://www.symas.com/symas-embedded-database-lmdb"><img alt="LMDB" src="https://gitee.com/anyline/service/raw/master/db/symas.webp" width="100"/>LMDB</a>
<a style="display:inline-block;" href="https://github.com/vesoft-inc/nebula"><img alt="Nebula" src="https://gitee.com/anyline/service/raw/master/db/nebula.ico" width="100"/>Nebula</a>
<a style="display:inline-block;" href="https://aws.amazon.com/neptune/"><img alt="AmazonNeptune" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonNeptune</a>
<a style="display:inline-block;" href="https://www.oracle.com/java/coherence/"><img alt="OracleCoherence" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>OracleCoherence</a>
<a style="display:inline-block;" href="https://geode.apache.org/"><img alt="Geode" src="https://gitee.com/anyline/service/raw/master/db/geode.png" width="100"/>Geode</a>
<a style="display:inline-block;" href="https://aws.amazon.com/simpledb/"><img alt="AmazonSimpleDB" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonSimpleDB</a>
<a style="display:inline-block;" href="https://www.percona.com/software/mysql-database/percona-server"><img alt="PerconaMySQL" src="https://gitee.com/anyline/service/raw/master/db/percona.svg" width="100"/>PerconaMySQL</a>
<a style="display:inline-block;" href="https://aws.amazon.com/cloudsearch/"><img alt="AmazonCloudSearch" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonCloudSearch</a>
<a style="display:inline-block;" href="https://www.stardog.com/"><img alt="Stardog" src="https://gitee.com/anyline/service/raw/master/db/stardog.webp" width="100"/>Stardog</a>
<a style="display:inline-block;" href="https://www.firebolt.io/"><img alt="Firebolt" src="https://gitee.com/anyline/service/raw/master/db/firebolt.svg" width="100"/>Firebolt</a>
<a style="display:inline-block;" href="https://www.datomic.com/"><img alt="Datomic" src="https://gitee.com/anyline/service/raw/master/db/datomic.png" width="100"/>Datomic</a>
<a style="display:inline-block;" href="https://www.gaia-gis.it/fossil/libspatialite/index"><img alt="SpatiaLite" src="https://gitee.com/anyline/service/raw/master/db/spatialite.png" width="100"/>SpatiaLite</a>
<a style="display:inline-block;" href="https://www.monetdb.org/"><img alt="MonetDB" src="https://gitee.com/anyline/service/raw/master/db/monetdb.png" width="100"/>MonetDB</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-multivalue-application-development-platform/rocket-jbase"><img alt="jBASE" src="https://gitee.com/anyline/service/raw/master/db/rocket.svg" width="100"/>jBASE</a>
<a style="display:inline-block;" href="https://basex.org/"><img alt="BaseX" src="https://gitee.com/anyline/service/raw/master/db/basex.png" width="100"/>BaseX</a>
<a style="display:inline-block;" href="https://www.trychroma.com/"><img alt="Chroma" src="https://gitee.com/anyline/service/raw/master/db/chroma.png" width="100"/>Chroma</a>
<a style="display:inline-block;" href="http://www.empress.com/"><img alt="Empress" src="https://gitee.com/anyline/service/raw/master/db/empress.gif" width="100"/>Empress</a>
<a style="display:inline-block;" href="https://aws.amazon.com/documentdb/"><img alt="AmazonDocumentDB" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonDocumentDB</a>
<a style="display:inline-block;" href="https://janusgraph.org/"><img alt="JanusGraph" src="https://gitee.com/anyline/service/raw/master/db/janusgraph.ico" width="100"/>JanusGraph</a>
<a style="display:inline-block;" href="http://erlang.org/doc/man/mnesia.html"><img alt="Mnesia" src="https://gitee.com/anyline/service/raw/master/db/erlang.png" width="100"/>Mnesia</a>
<a style="display:inline-block;" href="https://www.tmaxsoft.com/products/tibero/"><img alt="Tibero" src="https://gitee.com/anyline/service/raw/master/db/tmaxsoft.png" width="100"/>Tibero</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-questdb"><img alt="QuestDB" src="https://gitee.com/anyline/service/raw/master/db/questdb.svg" width="100"/>QuestDB</a>
<a style="display:inline-block;" href="https://griddb.net/"><img alt="GridDB" src="https://gitee.com/anyline/service/raw/master/db/griddb.png" width="100"/>GridDB</a>
<a style="display:inline-block;" href="https://www.tigergraph.com/"><img alt="TigerGraph" src="https://gitee.com/anyline/service/raw/master/db/tigergraph.svg" width="100"/>TigerGraph</a>
<a style="display:inline-block;" href="http://www.db4o.com/"><img alt="Db4o" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Db4o</a>
<a style="display:inline-block;" href="https://github.com/weaviate/weaviate"><img alt="Weaviate" src="https://gitee.com/anyline/service/raw/master/db/weaviate.svg" width="100"/>Weaviate</a>
<a style="display:inline-block;" href="https://www.tarantool.io/"><img alt="Tarantool" src="https://gitee.com/anyline/service/raw/master/db/tarantool.svg" width="100"/>Tarantool</a>
<a style="display:inline-block;" href="https://www.gridgain.com/"><img alt="GridGain" src="https://gitee.com/anyline/service/raw/master/db/gridgain.svg" width="100"/>GridGain</a>
<a style="display:inline-block;" href="https://dgraph.io/"><img alt="Dgraph" src="https://gitee.com/anyline/service/raw/master/db/dgraph.svg" width="100"/>Dgraph</a>
<a style="display:inline-block;" href="http://www.opentext.com/what-we-do/products/specialty-technologies/opentext-gupta-development-tools-databases/opentext-gupta-sqlbase"><img alt="SQLBase" src="https://gitee.com/anyline/service/raw/master/db/opentext.svg" width="100"/>SQLBase</a>
<a style="display:inline-block;" href="http://opentsdb.net/"><img alt="OpenTSDB" src="https://gitee.com/anyline/service/raw/master/db/opentsdb.png" width="100"/>OpenTSDB</a>
<a style="display:inline-block;" href="https://sourceforge.net/projects/sedna/"><img alt="Sedna" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Sedna</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-oceanbase"><img alt="OceanBase" src="https://gitee.com/anyline/service/raw/master/db/oceanbase.webp" width="100"/>OceanBase</a>
<a style="display:inline-block;" href="https://fauna.com/"><img alt="Fauna" src="https://gitee.com/anyline/service/raw/master/db/fauna.svg" width="100"/>Fauna</a>
<a style="display:inline-block;" href="https://www.datameer.com/"><img alt="Datameer" src="https://gitee.com/anyline/service/raw/master/db/datameer.svg" width="100"/>Datameer</a>
<a style="display:inline-block;" href="https://planetscale.com/"><img alt="PlanetScale" src="https://gitee.com/anyline/service/raw/master/db/planetscale.ico" width="100"/>PlanetScale</a>
<a style="display:inline-block;" href="https://www.actian.com/data-management/nosql-object-database/"><img alt="ActianNoSQL" src="https://gitee.com/anyline/service/raw/master/db/actian.png" width="100"/>ActianNoSQL</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/technologies/related/timesten.html"><img alt="TimesTen" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>TimesTen</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-voltdb"><img alt="VoltDB" src="https://gitee.com/anyline/service/raw/master/db/voltdb.svg" width="100"/>VoltDB</a>
<a style="display:inline-block;" href="https://github.com/apple/foundationdb"><img alt="FoundationDB" src="https://gitee.com/anyline/service/raw/master/db/appledev.svg" width="100"/>FoundationDB</a>
<a style="display:inline-block;" href="https://ignitetech.com/softwarelibrary/infobrightdb"><img alt="Infobright" src="https://gitee.com/anyline/service/raw/master/db/ignitetech.ico" width="100"/>Infobright</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/db2/warehouse"><img alt="Db2Warehouse" src="https://gitee.com/anyline/service/raw/master/db/ibm.svg" width="100"/>Db2Warehouse</a>
<a style="display:inline-block;" href="https://www.hpe.com/us/en/servers/nonstop.html"><img alt="NonStopSQL" src="https://gitee.com/anyline/service/raw/master/db/nonstop.svg" width="100"/>NonStopSQL</a>
<a style="display:inline-block;" href="https://ignitetech.com/objectstore/"><img alt="ObjectStore" src="https://gitee.com/anyline/service/raw/master/db/ignitetech.ico" width="100"/>ObjectStore</a>
<a style="display:inline-block;" href="https://hughestech.com.au/products/msql/"><img alt="mSQL" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>mSQL</a>
<a style="display:inline-block;" href="http://www.litedb.org/"><img alt="LiteDB" src="https://gitee.com/anyline/service/raw/master/db/litedb.svg" width="100"/>LiteDB</a>
<a style="display:inline-block;" href="https://milvus.io/"><img alt="Milvus" src="https://gitee.com/anyline/service/raw/master/db/milvus.svg" width="100"/>Milvus</a>
<a style="display:inline-block;" href="http://www.dataease.com/"><img alt="DataEase" src="https://gitee.com/anyline/service/raw/master/db/dataease.png" width="100"/>DataEase</a>
<a style="display:inline-block;" href="https://cubrid.com"><img alt="Cubrid" src="https://gitee.com/anyline/service/raw/master/db/cubrid.png" width="100"/>Cubrid</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-d3"><img alt="D3" src="https://gitee.com/anyline/service/raw/master/db/rocket.svg" width="100"/>D3</a>
<a style="display:inline-block;" href="https://victoriametrics.com/"><img alt="VictoriaMetrics" src="https://gitee.com/anyline/service/raw/master/db/victoriametrics.png" width="100"/>VictoriaMetrics</a>
<a style="display:inline-block;" href="https://kylin.apache.org/"><img alt="kylin" src="https://gitee.com/anyline/service/raw/master/db/kylin.png" width="100"/>kylin</a>
<a style="display:inline-block;" href="https://giraph.apache.org/"><img alt="Giraph" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Giraph</a>
<a style="display:inline-block;" href="https://sourceforge.net/projects/fis-gtm/"><img alt="GTM" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>GTM</a>
<a style="display:inline-block;" href="https://objectbox.io/"><img alt="ObjectBox" src="https://gitee.com/anyline/service/raw/master/db/objectbox.png" width="100"/>ObjectBox</a>
<a style="display:inline-block;" href="https://windev.com/pcsoft/hfsql.htm"><img alt="HFSQL" src="https://gitee.com/anyline/service/raw/master/db/hfsql.png" width="100"/>HFSQL</a>
<a style="display:inline-block;" href="https://github.com/meilisearch/meilisearch"><img alt="Meilisearch" src="https://gitee.com/anyline/service/raw/master/db/meilisearch.svg" width="100"/>Meilisearch</a>
<a style="display:inline-block;" href="https://www.matrixorigin.io/"><img alt="MatrixOne" src="https://gitee.com/anyline/service/raw/master/db/matrixone.svg" width="100"/>MatrixOne</a>
<a style="display:inline-block;" href="https://www.mcobject.com/perst/"><img alt="Perst" src="https://gitee.com/anyline/service/raw/master/db/mcobject.png" width="100"/>Perst</a>
<a style="display:inline-block;" href="https://www.oracle.com/database/technologies/related/rdb.html"><img alt="OracleRdb" src="https://gitee.com/anyline/service/raw/master/db/oracle.avif" width="100"/>OracleRdb</a>
<a style="display:inline-block;" href="https://www.gigaspaces.com"><img alt="GigaSpaces" src="https://gitee.com/anyline/service/raw/master/db/gigaspaces.png" width="100"/>GigaSpaces</a>
<a style="display:inline-block;" href="https://vitess.io/"><img alt="Vitess" src="https://gitee.com/anyline/service/raw/master/db/vitess.png" width="100"/>Vitess</a>
<a style="display:inline-block;" href="https://reality.necsws.com/"><img alt="Reality" src="https://gitee.com/anyline/service/raw/master/db/necsws.jpg" width="100"/>Reality</a>
<a style="display:inline-block;" href="https://sql.js.org/"><img alt="SQLJS" src="https://gitee.com/anyline/service/raw/master/db/sqljs.png" width="100"/>SQLJS</a>
<a style="display:inline-block;" href="https://www.hpe.com/us/en/software/data-fabric.html"><img alt="Ezmeral" src="https://gitee.com/anyline/service/raw/master/db/nonstop.svg" width="100"/>Ezmeral</a>
<a style="display:inline-block;" href="https://allegrograph.com/"><img alt="AllegroGraph" src="https://gitee.com/anyline/service/raw/master/db/allegrograph.png" width="100"/>AllegroGraph</a>
<a style="display:inline-block;" href="https://m3db.io/"><img alt="M3DB" src="https://gitee.com/anyline/service/raw/master/db/m3db.svg" width="100"/>M3DB</a>
<a style="display:inline-block;" href="http://hawq.apache.org/"><img alt="HAWQ" src="https://gitee.com/anyline/service/raw/master/db/hawq.png" width="100"/>HAWQ</a>
<a style="display:inline-block;" href="https://www.starrocks.io/"><img alt="StarRocks" src="https://gitee.com/anyline/service/raw/master/db/starrocks.webp" width="100"/>StarRocks</a>
<a style="display:inline-block;" href="https://teamblue.unicomsi.com/products/soliddb/"><img alt="solidDB" src="https://gitee.com/anyline/service/raw/master/db/unicomsi.png" width="100"/>solidDB</a>
<a style="display:inline-block;" href="https://www.3ds.com/nuodb-distributed-sql-database/"><img alt="NuoDB" src="https://gitee.com/anyline/service/raw/master/db/nuodb.png" width="100"/>NuoDB</a>
<a style="display:inline-block;" href="https://www.alachisoft.com/ncache/"><img alt="NCache" src="https://gitee.com/anyline/service/raw/master/db/ncache.svg" width="100"/>NCache</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-opengauss"><img alt="OpenGauss" src="https://gitee.com/anyline/service/raw/master/db/opengauss.png" width="100"/>OpenGauss</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-iotdb"><img alt="IoTDB" src="https://gitee.com/anyline/service/raw/master/db/iotdb.webp" width="100"/>IoTDB</a>
<a style="display:inline-block;" href="https://github.com/qdrant/qdrant"><img alt="Qdrant" src="https://gitee.com/anyline/service/raw/master/db/qdrant.svg" width="100"/>Qdrant</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-m204"><img alt="Model204" src="https://gitee.com/anyline/service/raw/master/db/rocket.svg" width="100"/>Model204</a>
<a style="display:inline-block;" href="https://zodb.org/"><img alt="ZODB" src="https://gitee.com/anyline/service/raw/master/db/zodb.png" width="100"/>ZODB</a>
<a style="display:inline-block;" href="https://www.bigchaindb.com/"><img alt="BigchainDB" src="https://gitee.com/anyline/service/raw/master/db/bigchaindb.png" width="100"/>BigchainDB</a>
<a style="display:inline-block;" href="https://surrealdb.com/"><img alt="SurrealDB" src="https://gitee.com/anyline/service/raw/master/db/surrealdb.svg" width="100"/>SurrealDB</a>
<a style="display:inline-block;" href="https://xapian.org/"><img alt="Xapian" src="https://gitee.com/anyline/service/raw/master/db/xapian.png" width="100"/>Xapian</a>
<a style="display:inline-block;" href="https://www.elevatesoft.com/products?category=dbisam"><img alt="DBISAM" src="https://gitee.com/anyline/service/raw/master/db/dbisam.png" width="100"/>DBISAM</a>
<a style="display:inline-block;" href="https://www.actian.com/analytic-database/vector-analytic-database/"><img alt="ActianVector" src="https://gitee.com/anyline/service/raw/master/db/actian.png" width="100"/>ActianVector</a>
<a style="display:inline-block;" href="https://github.com/hibari/hibari"><img alt="Hibari" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Hibari</a>
<a style="display:inline-block;" href="https://github.com/dolthub/dolt"><img alt="Dolt" src="https://gitee.com/anyline/service/raw/master/db/dolt.png" width="100"/>Dolt</a>
<a style="display:inline-block;" href="https://typedb.com/"><img alt="TypeDB" src="https://gitee.com/anyline/service/raw/master/db/typedb.svg" width="100"/>TypeDB</a>
<a style="display:inline-block;" href="http://altibase.com/"><img alt="Altibase" src="https://gitee.com/anyline/service/raw/master/db/altibase.png" width="100"/>Altibase</a>
<a style="display:inline-block;" href="https://aws.amazon.com/timestream/"><img alt="AmazonTimestream" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonTimestream</a>
<a style="display:inline-block;" href="https://www.objectdb.com/"><img alt="ObjectDB" src="https://gitee.com/anyline/service/raw/master/db/objectdb.ico" width="100"/>ObjectDB</a>
<a style="display:inline-block;" href="https://blazegraph.com/"><img alt="Blazegraph" src="https://gitee.com/anyline/service/raw/master/db/blazegraph.png" width="100"/>Blazegraph</a>
<a style="display:inline-block;" href="https://aws.amazon.com/keyspaces/"><img alt="AmazonKeyspaces" src="https://gitee.com/anyline/service/raw/master/db/amazon.webp" width="100"/>AmazonKeyspaces</a>
<a style="display:inline-block;" href="https://www.tencentcloud.com/products/dcdb"><img alt="TDSQL" src="https://gitee.com/anyline/service/raw/master/db/tencentcloud.ico" width="100"/>TDSQL(腾讯云计算（北京）有限责任公司)</a>
<a style="display:inline-block;" href="https://www.ca.com/us/products/ca-idms.html"><img alt="IDMS" src="https://gitee.com/anyline/service/raw/master/db/broadcom.png" width="100"/>IDMS</a>
<a style="display:inline-block;" href="https://rdf4j.org/"><img alt="RDF4J" src="https://gitee.com/anyline/service/raw/master/db/rdf4j.png" width="100"/>RDF4J</a>
<a style="display:inline-block;" href="https://www.geomesa.org/"><img alt="GeoMesa" src="https://gitee.com/anyline/service/raw/master/db/geomesa.png" width="100"/>GeoMesa</a>
<a style="display:inline-block;" href="http://exist-db.org/"><img alt="eXistdb" src="https://gitee.com/anyline/service/raw/master/db/existdb.gif" width="100"/>eXistdb</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/ibm-websphere-extreme-scale"><img alt="eXtremeScale" src="https://gitee.com/anyline/service/raw/master/db/extremescale.svg" width="100"/>eXtremeScale</a>
<a style="display:inline-block;" href="https://rockset.com/"><img alt="Rockset" src="https://gitee.com/anyline/service/raw/master/db/rockset.svg" width="100"/>Rockset</a>
<a style="display:inline-block;" href="https://yellowbrick.com/"><img alt="Yellowbrick" src="https://gitee.com/anyline/service/raw/master/db/yellowbrick.svg" width="100"/>Yellowbrick</a>
<a style="display:inline-block;" href="https://sqream.com/"><img alt="SQream" src="https://gitee.com/anyline/service/raw/master/db/sqream.svg" width="100"/>SQream</a>
<a style="display:inline-block;" href="https://www.broadcom.com/products/mainframe/databases-database-mgmt/datacom"><img alt="DatacomDB" src="https://gitee.com/anyline/service/raw/master/db/broadcom.png" width="100"/>DatacomDB</a>
<a style="display:inline-block;" href="https://typesense.org/"><img alt="Typesense" src="https://gitee.com/anyline/service/raw/master/db/typesense.svg" width="100"/>Typesense</a>
<a style="display:inline-block;" href="https://mapdb.org/"><img alt="MapDB" src="https://gitee.com/anyline/service/raw/master/db/mapdb.png" width="100"/>MapDB</a>
<a style="display:inline-block;" href="https://objectivity.com/products/objectivitydb/"><img alt="ObjectivityDB" src="https://gitee.com/anyline/service/raw/master/db/objectivity.png" width="100"/>ObjectivityDB</a>
<a style="display:inline-block;" href="https://cratedb.com/?utm_campaign=2023-Q1-WS-DB-Engines&utm_source=db-engines.com"><img alt="CrateDB" src="https://gitee.com/anyline/service/raw/master/db/cratedb.svg" width="100"/>CrateDB</a>
<a style="display:inline-block;" href="https://www.mcobject.com"><img alt="eXtreme" src="https://gitee.com/anyline/service/raw/master/db/extremedb.webp" width="100"/>eXtreme</a>
<a style="display:inline-block;" href="https://paradigm4.com/"><img alt="SciDB" src="https://gitee.com/anyline/service/raw/master/db/paradigm4.svg" width="100"/>SciDB</a>
<a style="display:inline-block;" href="http://alasql.org"><img alt="AlaSQL" src="https://gitee.com/anyline/service/raw/master/db/alasql.png" width="100"/>AlaSQL</a>
<a style="display:inline-block;" href="https://github.com/kairosdb/kairosdb"><img alt="KairosDB" src="https://gitee.com/anyline/service/raw/master/db/kairosdb.png" width="100"/>KairosDB</a>
<a style="display:inline-block;" href="https://www.kinetica.com/"><img alt="Kinetica" src="https://gitee.com/anyline/service/raw/master/db/kinetica.png" width="100"/>Kinetica</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/maxcompute"><img alt="MaxCompute" src="https://gitee.com/anyline/service/raw/master/db/aliyun.ico" width="100"/>MaxCompute</a>
<a style="display:inline-block;" href="https://github.com/Snapchat/KeyDB"><img alt="KeyDB" src="https://gitee.com/anyline/service/raw/master/db/keydb.svg" width="100"/>KeyDB</a>
<a style="display:inline-block;" href="https://www.revelation.com/index.php/products/openinsight"><img alt="OpenInsight" src="https://gitee.com/anyline/service/raw/master/db/openinsight.png" width="100"/>OpenInsight</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/analyticdb-for-mysql"><img alt="AnalyticDBMySQL" src="https://gitee.com/anyline/service/raw/master/db/aliyun.ico" width="100"/>AnalyticDBMySQL</a>
<a style="display:inline-block;" href="https://gemtalksystems.com/"><img alt="GemStoneS" src="https://gitee.com/anyline/service/raw/master/db/GemTalkLogo.png" width="100"/>GemStoneS</a>
<a style="display:inline-block;" href="https://vald.vdaas.org/"><img alt="Vald" src="https://gitee.com/anyline/service/raw/master/db/vald.svg" width="100"/>Vald</a>
<a style="display:inline-block;" href="/anyline/anyline-simple/tree/master/anyline-simple-data-jdbc-dialect/anyline-simple-data-jdbc-doris"><img alt="Doris" src="https://gitee.com/anyline/service/raw/master/db/doris.svg" width="100"/>Doris</a>
<a style="display:inline-block;" href="https://www.devgraph.com/scalearc/"><img alt="ScaleArc" src="https://gitee.com/anyline/service/raw/master/db/scalearc.svg" width="100"/>ScaleArc</a>
<a style="display:inline-block;" href="https://www.risingwave.com/database/"><img alt="RisingWave" src="https://gitee.com/anyline/service/raw/master/db/risingwave.svg" width="100"/>RisingWave</a>
<a style="display:inline-block;" href="http://www.frontbase.com/"><img alt="FrontBase" src="https://gitee.com/anyline/service/raw/master/db/frontbase.gif" width="100"/>FrontBase</a>
<a style="display:inline-block;" href="https://www.postgres-xl.org/"><img alt="PostgresXL" src="https://gitee.com/anyline/service/raw/master/db/pgxl.jpg" width="100"/>PostgresXL</a>
<a style="display:inline-block;" href="https://pinot.apache.org/"><img alt="Pinot" src="https://gitee.com/anyline/service/raw/master/db/apachepoint.png" width="100"/>Pinot</a>
<a style="display:inline-block;" href="https://spotify.github.io/heroic/#!/index"><img alt="Heroic" src="https://gitee.com/anyline/service/raw/master/db/heroic.png" width="100"/>Heroic</a>
<a style="display:inline-block;" href="https://vistadb.com/"><img alt="VistaDB" src="https://gitee.com/anyline/service/raw/master/db/vistadb.png" width="100"/>VistaDB</a>
<a style="display:inline-block;" href="http://scalaris.zib.de/"><img alt="Scalaris" src="https://gitee.com/anyline/service/raw/master/db/scalaris.png" width="100"/>Scalaris</a>
<a style="display:inline-block;" href="https://www.nexusdb.com/"><img alt="NexusDB" src="https://gitee.com/anyline/service/raw/master/db/nexusdb.gif" width="100"/>NexusDB</a>
<a style="display:inline-block;" href="https://www.percona.com/mongodb/software/percona-server-for-mongodb"><img alt="PerconaMongoDB" src="https://gitee.com/anyline/service/raw/master/db/percona.svg" width="100"/>PerconaMongoDB</a>
<a style="display:inline-block;" href="https://www.graphengine.io/"><img alt="GraphEngine" src="https://gitee.com/anyline/service/raw/master/db/graphengine.ico" width="100"/>GraphEngine</a>
<a style="display:inline-block;" href="https://github.com/boltdb/bolt"><img alt="BoltDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>BoltDB</a>
<a style="display:inline-block;" href="https://atoti.io/"><img alt="atoti" src="https://gitee.com/anyline/service/raw/master/db/atoti.svg" width="100"/>atoti</a>
<a style="display:inline-block;" href="https://vespa.ai/"><img alt="Vespa" src="https://gitee.com/anyline/service/raw/master/db/vespa.png" width="100"/>Vespa</a>
<a style="display:inline-block;" href="https://github.com/techfort/LokiJS"><img alt="LokiJS" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>LokiJS</a>
<a style="display:inline-block;" href="https://raima.com/"><img alt="Raima" src="https://gitee.com/anyline/service/raw/master/db/raima.png" width="100"/>Raima</a>
<a style="display:inline-block;" href="https://databend.rs"><img alt="Databend" src="https://gitee.com/anyline/service/raw/master/db/databend.svg" width="100"/>Databend</a>
<a style="display:inline-block;" href="https://www.rbase.com/"><img alt="RBASE" src="https://gitee.com/anyline/service/raw/master/db/rbase.png" width="100"/>RBASE</a>
<a style="display:inline-block;" href="https://librdf.org/"><img alt="Redland" src="https://gitee.com/anyline/service/raw/master/db/librdf.ico" width="100"/>Redland</a>
<a style="display:inline-block;" href="https://harperdb.io/"><img alt="HarperDB" src="https://gitee.com/anyline/service/raw/master/db/harper.webp" width="100"/>HarperDB</a>
<a style="display:inline-block;" href="https://splicemachine.com/"><img alt="SpliceMachine" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>SpliceMachine</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/hybriddb-postgresql"><img alt="AnalyticDBPostgreSQL" src="https://gitee.com/anyline/service/raw/master/db/aliyun.ico" width="100"/>AnalyticDBPostgreSQL</a>
<a style="display:inline-block;" href="https://modeshape.jboss.org/"><img alt="ModeShape" src="https://gitee.com/anyline/service/raw/master/db/modeshape.ico" width="100"/>ModeShape</a>
<a style="display:inline-block;" href="https://strabon.di.uoa.gr/"><img alt="Strabon" src="https://gitee.com/anyline/service/raw/master/db/strabon.png" width="100"/>Strabon</a>
<a style="display:inline-block;" href="https://www.jadeworld.com/developer-center"><img alt="Jade" src="https://gitee.com/anyline/service/raw/master/db/jade.svg" width="100"/>Jade</a>
<a style="display:inline-block;" href="http://www.sequoiadb.com/"><img alt="Sequoiadb" src="https://gitee.com/anyline/service/raw/master/db/sequoiadb.png" width="100"/>Sequoiadb</a>
<a style="display:inline-block;" href="https://github.com/cnosdb/cnosdb"><img alt="CnosDB" src="https://gitee.com/anyline/service/raw/master/db/cnosdb.png" width="100"/>CnosDB</a>
<a style="display:inline-block;" href="https://www.ittia.com/"><img alt="ITTIA" src="https://gitee.com/anyline/service/raw/master/db/ittia.png" width="100"/>ITTIA</a>
<a style="display:inline-block;" href="https://github.com/reverbrain/elliptics"><img alt="Elliptics" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Elliptics</a>
<a style="display:inline-block;" href="https://www.elassandra.io/"><img alt="Elassandra" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Elassandra</a>
<a style="display:inline-block;" href="http://www.rasdaman.org/"><img alt="Rasdaman" src="https://gitee.com/anyline/service/raw/master/db/rasdaman.png" width="100"/>Rasdaman</a>
<a style="display:inline-block;" href="https://www.searchblox.com/"><img alt="SearchBlox" src="https://gitee.com/anyline/service/raw/master/db/searchblox.webp" width="100"/>SearchBlox</a>
<a style="display:inline-block;" href="https://objectivity.com/infinitegraph/"><img alt="InfiniteGraph" src="https://gitee.com/anyline/service/raw/master/db/infinitegraph.png" width="100"/>InfiniteGraph</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/polardb"><img alt="ApsaraDBPolarDB" src="https://gitee.com/anyline/service/raw/master/db/aliyun.ico" width="100"/>ApsaraDBPolarDB</a>
<a style="display:inline-block;" href="https://starcounter.com/"><img alt="Starcounter" src="https://gitee.com/anyline/service/raw/master/db/starcounter.svg" width="100"/>Starcounter</a>
<a style="display:inline-block;" href="https://axibase.com/docs/atsd/finance/"><img alt="Axibase" src="https://gitee.com/anyline/service/raw/master/db/axibase.png" width="100"/>Axibase</a>
<a style="display:inline-block;" href="https://kyligence.io/kyligence-enterprise/"><img alt="Kyligence" src="https://gitee.com/anyline/service/raw/master/db/kyligence.png" width="100"/>Kyligence</a>
<a style="display:inline-block;" href="https://www.featurebase.com/"><img alt="FeatureBase" src="https://gitee.com/anyline/service/raw/master/db/featurebase.png" width="100"/>FeatureBase</a>
<a style="display:inline-block;" href="https://google.github.io/lovefield/"><img alt="Lovefield" src="https://gitee.com/anyline/service/raw/master/db/lovefield.png" width="100"/>Lovefield</a>
<a style="display:inline-block;" href="http://www.project-voldemort.com/"><img alt="Voldemort" src="https://gitee.com/anyline/service/raw/master/db/voldemort.png" width="100"/>Voldemort</a>
<a style="display:inline-block;" href="https://brytlyt.io/"><img alt="Brytlyt" src="https://gitee.com/anyline/service/raw/master/db/brytlyt.png" width="100"/>Brytlyt</a>
<a style="display:inline-block;" href="https://www.machbase.com/"><img alt="MachbaseNeo" src="https://gitee.com/anyline/service/raw/master/db/machbase.png" width="100"/>MachbaseNeo</a>
<a style="display:inline-block;" href="https://esd.actian.com/product/Versant_FastObjects/"><img alt="ActianFastObjects" src="https://gitee.com/anyline/service/raw/master/db/actian.png" width="100"/>ActianFastObjects</a>
<a style="display:inline-block;" href="https://www.rocketsoftware.com/products/rocket-multivalue-application-development-platform/rocket-open-qm"><img alt="OpenQM" src="https://gitee.com/anyline/service/raw/master/db/rocket.svg" width="100"/>OpenQM</a>
<a style="display:inline-block;" href="https://www.oxfordsemantic.tech/"><img alt="RDFox" src="https://gitee.com/anyline/service/raw/master/db/rdfox.svg" width="100"/>RDFox</a>
<a style="display:inline-block;" href="https://cambridgesemantics.com/anzograph/"><img alt="AnzoGraph_DB" src="https://gitee.com/anyline/service/raw/master/db/cambridgesemantics.svg" width="100"/>AnzoGraph_DB</a>
<a style="display:inline-block;" href="https://flur.ee/"><img alt="Fluree" src="https://gitee.com/anyline/service/raw/master/db/fluee.png" width="100"/>Fluree</a>
<a style="display:inline-block;" href="https://immudb.io/"><img alt="Immudb" src="https://gitee.com/anyline/service/raw/master/db/immudb.svg" width="100"/>Immudb</a>
<a style="display:inline-block;" href="https://www.mimer.com/"><img alt="Mimer_SQL" src="https://gitee.com/anyline/service/raw/master/db/mimer.png" width="100"/>Mimer_SQL</a>
<a style="display:inline-block;" href="https://github.com/ydb-platform/ydb"><img alt="YDB" src="https://gitee.com/anyline/service/raw/master/db/ydb.svg" width="100"/>YDB</a>
<a style="display:inline-block;" href="https://www.aelius.com/njh/redstore/"><img alt="RedStore" src="https://gitee.com/anyline/service/raw/master/db/redstore.png" width="100"/>RedStore</a>
<a style="display:inline-block;" href="http://www.hypergraphdb.org/"><img alt="HyperGraphDB" src="https://gitee.com/anyline/service/raw/master/db/hypergraphdb.jpg" width="100"/>HyperGraphDB</a>
<a style="display:inline-block;" href="https://github.com/marqo-ai/marqo"><img alt="Marqo" src="https://gitee.com/anyline/service/raw/master/db/marqo.png" width="100"/>Marqo</a>
<a style="display:inline-block;" href="https://github.com/Softmotions/ejdb"><img alt="EJDB" src="https://gitee.com/anyline/service/raw/master/db/ejdb.png" width="100"/>EJDB</a>
<a style="display:inline-block;" href="https://tajo.apache.org/"><img alt="Tajo" src="https://gitee.com/anyline/service/raw/master/db/tajo.png" width="100"/>Tajo</a>
<a style="display:inline-block;" href="https://github.com/activeloopai/deeplake"><img alt="DeepLake" src="https://gitee.com/anyline/service/raw/master/db/deeplake.png" width="100"/>DeepLake</a>
<a style="display:inline-block;" href="https://www.asiainfo.com/en_us/product_aisware_antdb_detail.html"><img alt="AntDB" src="https://gitee.com/anyline/service/raw/master/db/antdb.png" width="100"/>AntDB</a>
<a style="display:inline-block;" href="https://www.leanxcale.com/"><img alt="LeanXcale" src="https://gitee.com/anyline/service/raw/master/db/leanxcale.png" width="100"/>LeanXcale</a>
<a style="display:inline-block;" href="http://www.mulgara.org/"><img alt="Mulgara" src="https://gitee.com/anyline/service/raw/master/db/mulgara.jpg" width="100"/>Mulgara</a>
<a style="display:inline-block;" href="https://fast.fujitsu.com"><img alt="Fujitsu" src="https://gitee.com/anyline/service/raw/master/db/fujitsu.svg" width="100"/>Fujitsu</a>
<a style="display:inline-block;" href="https://github.com/twitter-archive/flockdb"><img alt="FlockDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>FlockDB</a>
<a style="display:inline-block;" href="https://github.com/STSSoft/STSdb4"><img alt="STSdb" src="https://gitee.com/anyline/service/raw/master/db/stsdb.png" width="100"/>STSdb</a>
<a style="display:inline-block;" href="https://www.openpie.com/"><img alt="PieCloudDB" src="https://gitee.com/anyline/service/raw/master/db/openpie.svg" width="100"/>PieCloudDB</a>
<a style="display:inline-block;" href="https://www.transaction.de/en/products/transbase.html"><img alt="Transbase" src="https://gitee.com/anyline/service/raw/master/db/transaction.png" width="100"/>Transbase</a>
<a style="display:inline-block;" href="https://www.elevatesoft.com/products?category=edb"><img alt="ElevateDB" src="https://gitee.com/anyline/service/raw/master/db/elevatedb.png" width="100"/>ElevateDB</a>
<a style="display:inline-block;" href=""><img alt="RiakTS" src="https://gitee.com/anyline/service/raw/master/db/riak.png" width="100"/>RiakTS</a>
<a style="display:inline-block;" href="https://www.faircom.com/products/faircom-db"><img alt="FaircomDB" src="https://gitee.com/anyline/service/raw/master/db/faircom.svg" width="100"/>FaircomDB</a>
<a style="display:inline-block;" href="http://neventstore.org/"><img alt="NEventStore" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>NEventStore</a>
<a style="display:inline-block;" href="https://github.com/bloomberg/comdb2"><img alt="Comdb2" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Comdb2</a>
<a style="display:inline-block;" href="https://yottadb.com/"><img alt="YottaDB" src="https://gitee.com/anyline/service/raw/master/db/yottadb.svg" width="100"/>YottaDB</a>
<a style="display:inline-block;" href="https://quasar.ai/"><img alt="Quasardb" src="https://gitee.com/anyline/service/raw/master/db/quasar.svg" width="100"/>Quasardb</a>
<a style="display:inline-block;" href="https://www.speedb.io/"><img alt="Speedb" src="https://gitee.com/anyline/service/raw/master/db/speedb.svg" width="100"/>Speedb</a>
<a style="display:inline-block;" href="http://www.esgyn.cn/"><img alt="EsgynDB" src="https://gitee.com/anyline/service/raw/master/db/esgyn.jpg" width="100"/>EsgynDB</a>
<a style="display:inline-block;" href="https://community.tibco.com/products/tibco-computedb"><img alt="ComputeDB" src="https://gitee.com/anyline/service/raw/master/db/tibco.ico" width="100"/>ComputeDB</a>
<a style="display:inline-block;" href="https://hugegraph.apache.org/"><img alt="HugeGraph" src="https://gitee.com/anyline/service/raw/master/db/hugegraph.png" width="100"/>HugeGraph</a>
<a style="display:inline-block;" href="https://www.valentina-db.net/"><img alt="Valentina" src="https://gitee.com/anyline/service/raw/master/db/valentina.png" width="100"/>Valentina</a>
<a style="display:inline-block;" href="https://github.com/pipelinedb/pipelinedb"><img alt="PipelineDB" src="https://gitee.com/anyline/service/raw/master/db/pipelinedb.png" width="100"/>PipelineDB</a>
<a style="display:inline-block;" href="https://bangdb.com/"><img alt="Bangdb" src="https://gitee.com/anyline/service/raw/master/db/bangdb.png" width="100"/>Bangdb</a>
<a style="display:inline-block;" href="https://dydra.com/about"><img alt="Dydra" src="https://gitee.com/anyline/service/raw/master/db/dydra.png" width="100"/>Dydra</a>
<a style="display:inline-block;" href="https://tinkerpop.apache.org/docs/current/reference/#tinkergraph-gremlin"><img alt="TinkerGraph" src="https://gitee.com/anyline/service/raw/master/db/apache.ico" width="100"/>TinkerGraph</a>
<a style="display:inline-block;" href="https://www.ibm.com/products/db2-event-store"><img alt="EventStore" src="https://gitee.com/anyline/service/raw/master/db/eventstore.svg" width="100"/>EventStore</a>
<a style="display:inline-block;" href="https://www.ultipa.com/"><img alt="Ultipa" src="https://gitee.com/anyline/service/raw/master/db/ultipa.svg" width="100"/>Ultipa</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/table-store"><img alt="Table_Store" src="https://gitee.com/anyline/service/raw/master/db/aliyun.ico" width="100"/>Table_Store</a>
<a style="display:inline-block;" href="https://www.actian.com/data-management/psql-embedded-database/"><img alt="ActianPSQL" src="https://gitee.com/anyline/service/raw/master/db/actian.png" width="100"/>ActianPSQL</a>
<a style="display:inline-block;" href="https://www.cubicweb.org/"><img alt="CubicWeb" src="https://gitee.com/anyline/service/raw/master/db/cubicweb.svg" width="100"/>CubicWeb</a>
<a style="display:inline-block;" href="https://www.exorbyte.com/"><img alt="Exorbyte" src="https://gitee.com/anyline/service/raw/master/db/exorbyte.png" width="100"/>Exorbyte</a>
<a style="display:inline-block;" href="https://graphbase.ai/"><img alt="GraphBase" src="https://gitee.com/anyline/service/raw/master/db/graphbase.png" width="100"/>GraphBase</a>
<a style="display:inline-block;" href="https://fallabs.com/tokyotyrant/"><img alt="TokyoTyrant" src="https://gitee.com/anyline/service/raw/master/db/fallabs.webp" width="100"/>TokyoTyrant</a>
<a style="display:inline-block;" href="https://github.com/skytable/skytable"><img alt="Skytable" src="https://gitee.com/anyline/service/raw/master/db/skytable.png" width="100"/>Skytable</a>
<a style="display:inline-block;" href="https://terminusdb.com/"><img alt="TerminusDB" src="https://gitee.com/anyline/service/raw/master/db/terminusdb.svg" width="100"/>TerminusDB</a>
<a style="display:inline-block;" href="https://github.com/dgraph-io/badger"><img alt="Badger" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Badger</a>
<a style="display:inline-block;" href="https://greptime.com/"><img alt="GreptimeDB" src="https://gitee.com/anyline/service/raw/master/db/greptime.png" width="100"/>GreptimeDB</a>
<a style="display:inline-block;" href="http://www.translattice.com/"><img alt="TransLattice" src="https://gitee.com/anyline/service/raw/master/db/translattice.png" width="100"/>TransLattice</a>
<a style="display:inline-block;" href="https://arcadedb.com/"><img alt="ArcadeDB" src="https://gitee.com/anyline/service/raw/master/db/arcadedb.png" width="100"/>ArcadeDB</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/product/kundb"><img alt="KunDB" src="https://gitee.com/anyline/service/raw/master/db/kundb.png" width="100"/>KunDB</a>
<a style="display:inline-block;" href="https://www.sparsity-technologies.com/"><img alt="Sparksee" src="https://gitee.com/anyline/service/raw/master/db/sparsity.png" width="100"/>Sparksee</a>
<a style="display:inline-block;" href="https://myscale.com/"><img alt="MyScale" src="https://gitee.com/anyline/service/raw/master/db/myscale.ico" width="100"/>MyScale</a>
<a style="display:inline-block;" href="https://bigobject.io/"><img alt="BigObject" src="https://gitee.com/anyline/service/raw/master/db/bigobject.svg" width="100"/>BigObject</a>
<a style="display:inline-block;" href="https://linter.ru/"><img alt="Linter" src="https://gitee.com/anyline/service/raw/master/db/linter.svg" width="100"/>Linter</a>
<a style="display:inline-block;" href="https://manticoresearch.com"><img alt="ManticoreSearch" src="https://gitee.com/anyline/service/raw/master/db/manticoresearch.svg" width="100"/>ManticoreSearch</a>
<a style="display:inline-block;" href="https://github.com/dragonflydb/dragonfly"><img alt="Dragonfly" src="https://gitee.com/anyline/service/raw/master/db/dragonflydb.svg" width="100"/>Dragonfly</a>
<a style="display:inline-block;" href="https://www.tigrisdata.com/"><img alt="Tigris" src="https://gitee.com/anyline/service/raw/master/db/tigris.svg" width="100"/>Tigris</a>
<a style="display:inline-block;" href="http://www.h2gis.org/"><img alt="H2GIS" src="https://gitee.com/anyline/service/raw/master/db/h2gis.png" width="100"/>H2GIS</a>
<a style="display:inline-block;" href="https://velocitydb.com/"><img alt="VelocityDB" src="https://gitee.com/anyline/service/raw/master/db/velocitydb.png" width="100"/>VelocityDB</a>
<a style="display:inline-block;" href="https://www.nuget.org/packages/EloqueraDB/"><img alt="Eloquera" src="https://gitee.com/anyline/service/raw/master/db/eloquera.png" width="100"/>Eloquera</a>
<a style="display:inline-block;" href="https://github.com/rescrv/HyperLevelDB"><img alt="HyperLevelDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>HyperLevelDB</a>
<a style="display:inline-block;" href="https://github.com/xtdb/xtdb"><img alt="XTDB" src="https://gitee.com/anyline/service/raw/master/db/xtdb.svg" width="100"/>XTDB</a>
<a style="display:inline-block;" href="http://blueflood.io/"><img alt="Blueflood" src="https://gitee.com/anyline/service/raw/master/db/blueflood.png" width="100"/>Blueflood</a>
<a style="display:inline-block;" href="https://senseidb.github.io/sensei/"><img alt="SenseiDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>SenseiDB</a>
<a style="display:inline-block;" href="https://www.alibabacloud.com/product/hitsdb"><img alt="TSDB" src="https://gitee.com/anyline/service/raw/master/db/aliyun.ico" width="100"/>TSDB</a>
<a style="display:inline-block;" href="https://github.com/krareT/trkdb"><img alt="TerarkDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>TerarkDB</a>
<a style="display:inline-block;" href="https://origodb.com/"><img alt="OrigoDB" src="https://gitee.com/anyline/service/raw/master/db/origodb.ico" width="100"/>OrigoDB</a>
<a style="display:inline-block;" href="https://tomp2p.net/"><img alt="TomP2P" src="https://gitee.com/anyline/service/raw/master/db/tomp2p.png" width="100"/>TomP2P</a>
<a style="display:inline-block;" href="https://www.xtremedata.com/"><img alt="XtremeData" src="https://gitee.com/anyline/service/raw/master/db/xtremedata.svg" width="100"/>XtremeData</a>
<a style="display:inline-block;" href="https://www.siaqodb.com/"><img alt="Siaqodb" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Siaqodb</a>
<a style="display:inline-block;" href="https://ytsaurus.tech/"><img alt="YTsaurus" src="https://gitee.com/anyline/service/raw/master/db/ytsaurus.svg" width="100"/>YTsaurus</a>
<a style="display:inline-block;" href="https://www.warp10.io/"><img alt="Warp" src="https://gitee.com/anyline/service/raw/master/db/warp10.svg" width="100"/>Warp</a>
<a style="display:inline-block;" href="http://www.opengemini.org/"><img alt="openGemini" src="https://gitee.com/anyline/service/raw/master/db/opengemini.svg" width="100"/>openGemini</a>
<a style="display:inline-block;" href="https://upscaledb.com/"><img alt="Upscaledb" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Upscaledb</a>
<a style="display:inline-block;" href="https://en.gstore.cn/"><img alt="gStore" src="https://gitee.com/anyline/service/raw/master/db/gstore.png" width="100"/>gStore</a>
<a style="display:inline-block;" href="http://www.oushu.com/product/oushuDB"><img alt="OushuDB" src="https://gitee.com/anyline/service/raw/master/db/oushu.svg" width="100"/>OushuDB</a>
<a style="display:inline-block;" href="https://indica.nl/"><img alt="Indica" src="https://gitee.com/anyline/service/raw/master/db/indica.jpg" width="100"/>Indica</a>
<a style="display:inline-block;" href="https://brightstardb.com/"><img alt="BrightstarDB" src="https://gitee.com/anyline/service/raw/master/db/brightstardb.png" width="100"/>BrightstarDB</a>
<a style="display:inline-block;" href="https://boilerbay.com/"><img alt="InfinityDB" src="https://gitee.com/anyline/service/raw/master/db/boilerbay.jpg" width="100"/>InfinityDB</a>
<a style="display:inline-block;" href="https://www.alachisoft.com/nosdb/"><img alt="NosDB" src="https://gitee.com/anyline/service/raw/master/db/ncache.svg" width="100"/>NosDB</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/subproduct/hippo"><img alt="Hippo" src="https://gitee.com/anyline/service/raw/master/db/transwarp.svg" width="100"/>Hippo</a>
<a style="display:inline-block;" href="https://github.com/appy-one/acebase"><img alt="Acebase" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Acebase</a>
<a style="display:inline-block;" href="https://siridb.com/"><img alt="SiriDB" src="https://gitee.com/anyline/service/raw/master/db/sisridb.svg" width="100"/>SiriDB</a>
<a style="display:inline-block;" href="https://sitewhere.io/"><img alt="SiteWhere" src="https://gitee.com/anyline/service/raw/master/db/sitewhere.svg" width="100"/>SiteWhere</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/product/argodb"><img alt="ArgoDB" src="https://gitee.com/anyline/service/raw/master/db/transwarp.ico" width="100"/>ArgoDB</a>
<a style="display:inline-block;" href="https://nsdb.io/"><img alt="NSDb" src="https://gitee.com/anyline/service/raw/master/db/nsdb.png" width="100"/>NSDb</a>
<a style="display:inline-block;" href="http://www.datajaguar.com"><img alt="JaguarDB" src="https://gitee.com/anyline/service/raw/master/db/jaguardb.png" width="100"/>JaguarDB</a>
<a style="display:inline-block;" href="http://wakanda.github.io/"><img alt="WakandaDB" src="https://gitee.com/anyline/service/raw/master/db/wakanda.png" width="100"/>WakandaDB</a>
<a style="display:inline-block;" href="https://www.transwarp.cn/en/product/stellardb"><img alt="StellarDB" src="https://gitee.com/anyline/service/raw/master/db/transwarp.svg" width="100"/>StellarDB</a>
<a style="display:inline-block;" href="https://galaxybase.com/"><img alt="Galaxybase" src="https://gitee.com/anyline/service/raw/master/db/galaxybase.png" width="100"/>Galaxybase</a>
<a style="display:inline-block;" href="https://newdatabase.com/"><img alt="DataFS" src="https://gitee.com/anyline/service/raw/master/db/datafs.png" width="100"/>DataFS</a>
<a style="display:inline-block;" href="https://www.sadasengine.com/"><img alt="SadasEngine" src="https://gitee.com/anyline/service/raw/master/db/sadasengine.png" width="100"/>SadasEngine</a>
<a style="display:inline-block;" href="https://www.hawkular.org/"><img alt="Hawkular" src="https://gitee.com/anyline/service/raw/master/db/hawkular.svg" width="100"/>Hawkular</a>
<a style="display:inline-block;" href="https://bitnine.net/"><img alt="AgensGraph" src="https://gitee.com/anyline/service/raw/master/db/agens.png" width="100"/>AgensGraph</a>
<a style="display:inline-block;" href="https://www.faircom.com/products/faircomedge-iot-database"><img alt="FaircomEDGE" src="https://gitee.com/anyline/service/raw/master/db/faircom.svg" width="100"/>FaircomEDGE</a>
<a style="display:inline-block;" href="https://cachelot.io/"><img alt="Cachelot" src="https://gitee.com/anyline/service/raw/master/db/cacheiot.png" width="100"/>Cachelot</a>
<a style="display:inline-block;" href="https://www.iboxdb.com/"><img alt="iBoxDB" src="https://gitee.com/anyline/service/raw/master/db/iboxdb.png" width="100"/>iBoxDB</a>
<a style="display:inline-block;" href="https://www.scaleoutsoftware.com/products/stateserver/"><img alt="StateServer" src="https://gitee.com/anyline/service/raw/master/db/scaleout.svg" width="100"/>StateServer</a>
<a style="display:inline-block;" href="https://dbmx.net/tkrzw/"><img alt="Tkrzw" src="https://gitee.com/anyline/service/raw/master/db/tkrzw.png" width="100"/>Tkrzw</a>
<a style="display:inline-block;" href="https://github.com/kashirin-alex/swc-db"><img alt="SWCDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>SWCDB</a>
<a style="display:inline-block;" href="https://ledisdb.io/"><img alt="LedisDB" src="https://gitee.com/anyline/service/raw/master/db/ledisdb.png" width="100"/>LedisDB</a>
<a style="display:inline-block;" href="https://swaydb.simer.au/"><img alt="SwayDB" src="https://gitee.com/anyline/service/raw/master/db/swaydb.png" width="100"/>SwayDB</a>
<a style="display:inline-block;" href="http://opennms.github.io/newts/"><img alt="Newts" src="https://gitee.com/anyline/service/raw/master/db/newts.png" width="100"/>Newts</a>
<a style="display:inline-block;" href="http://www.actordb.com/"><img alt="ActorDB" src="https://gitee.com/anyline/service/raw/master/db/actordb.png" width="100"/>ActorDB</a>
<a style="display:inline-block;" href="https://www.edgeintelligence.com/"><img alt="Intelligence" src="https://gitee.com/anyline/service/raw/master/db/edgeintelligence.svg" width="100"/>Intelligence</a>
<a style="display:inline-block;" href="http://www.smallsql.de/"><img alt="SmallSQL" src="https://gitee.com/anyline/service/raw/master/db/smallsql.png" width="100"/>SmallSQL</a>
<a style="display:inline-block;" href="https://www.mireo.com/spacetime"><img alt="SpaceTime" src="https://gitee.com/anyline/service/raw/master/db/spacetime.svg" width="100"/>SpaceTime</a>
<a style="display:inline-block;" href="http://sparkledb.com/"><img alt="SparkleDB" src="https://gitee.com/anyline/service/raw/master/db/sparkledb.png" width="100"/>SparkleDB</a>
<a style="display:inline-block;" href="https://caucho.com/"><img alt="ResinCache" src="https://gitee.com/anyline/service/raw/master/db/caucho.png" width="100"/>ResinCache</a>
<a style="display:inline-block;" href="https://jethro.io/"><img alt="JethroData" src="https://gitee.com/anyline/service/raw/master/db/jethro.png" width="100"/>JethroData</a>
<a style="display:inline-block;" href="http://bergdb.com/"><img alt="BergDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>BergDB</a>
<a style="display:inline-block;" href="https://www.cortex-ag.com/"><img alt="CortexDB" src="https://gitee.com/anyline/service/raw/master/db/cortex.svg" width="100"/>CortexDB</a>
<a style="display:inline-block;" href="https://covenantsql.io/"><img alt="CovenantSQL" src="https://gitee.com/anyline/service/raw/master/db/covenantsql.svg" width="100"/>CovenantSQL</a>
<a style="display:inline-block;" href="https://www.daggerdb.com/"><img alt="DaggerDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>DaggerDB</a>
<a style="display:inline-block;" href="https://github.com/edgelesssys/edgelessdb"><img alt="EdgelessDB" src="https://gitee.com/anyline/service/raw/master/db/edgelessdb.svg" width="100"/>EdgelessDB</a>
<a style="display:inline-block;" href="http://www.levyx.com/helium"><img alt="Helium" src="https://gitee.com/anyline/service/raw/master/db/levyx.png" width="100"/>Helium</a>
<a style="display:inline-block;" href="https://github.com/rayokota/hgraphdb"><img alt="HGraphDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>HGraphDB</a>
<a style="display:inline-block;" href="http://www.oberasoftware.com/jasdb/"><img alt="JasDB" src="https://gitee.com/anyline/service/raw/master/db/jasdb.png" width="100"/>JasDB</a>
<a style="display:inline-block;" href="https://github.com/mgholam/RaptorDB-Document"><img alt="RaptorDB" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>RaptorDB</a>
<a style="display:inline-block;" href="https://www.rizhiyi.com/"><img alt="Rizhiyi" src="https://gitee.com/anyline/service/raw/master/db/rizhiyi.ico" width="100"/>Rizhiyi</a>
<a style="display:inline-block;" href="http://www.searchxml.net/category/products/"><img alt="searchxml" src="https://gitee.com/anyline/service/raw/master/db/searchxml.png" width="100"/>searchxml</a>
<a style="display:inline-block;" href="https://github.com/dgraph-io/badger"><img alt="BadgerDB" src="https://gitee.com/anyline/service/raw/master/db/badgerdb.webp" width="100"/>BadgerDB</a>
<a style="display:inline-block;" href="https://github.com/cayleygraph/cayley"><img alt="Cayley" src="https://gitee.com/anyline/service/raw/master/db/cayley.webp" width="100"/>Cayley</a>
<a style="display:inline-block;" href="https://crase.sourceforge.net/"><img alt="Crase" src="https://gitee.com/anyline/service/raw/master/db/" width="100"/>Crase</a>
<a style="display:inline-block;" href=""><img alt="CrispI" src="https://gitee.com/anyline/service/raw/master/db/crispI.jpg" width="100"/>CrispI</a>
<a style="display:inline-block;" href="http://graphportal.com/"><img alt="GraphPortal" src="https://gitee.com/anyline/service/raw/master/db/graphportal.webp" width="100"/>GraphPortal</a>
<a style="display:inline-block;" href="http://kwanjeeraw.github.io/grinn/"><img alt="Grinn" src="https://gitee.com/anyline/service/raw/master/db/grinn.jpg" width="100"/>Grinn</a>
<a style="display:inline-block;" href="http://odaba.com/content/start/"><img alt="ODABA" src="https://gitee.com/anyline/service/raw/master/db/odaba.webp" width="100"/>ODABA</a>
<a style="display:inline-block;" href="https://github.com/OWASP/Amass"><img alt="OWASP" src="https://gitee.com/anyline/service/raw/master/db/owasp.webp" width="100"/>OWASP</a>
<a style="display:inline-block;" href="https://reldb.org/"><img alt="reldb" src="https://gitee.com/anyline/service/raw/master/db/reldb.webp" width="100"/>reldb</a>
<a style="display:inline-block;" href="https://www.iri.com/products/voracity/"><img alt="Voracity" src="https://gitee.com/anyline/service/raw/master/db/voracity.png" width="100"/>Voracity</a>
<a style="display:inline-block;" href="https://zeromq.org/"><img alt="ZeroMQ" src="https://gitee.com/anyline/service/raw/master/db/zeromq.gif" width="100"/>ZeroMQ</a>
没有示例的看这个目录下有没有 [【anyline-data-jdbc-dialect】](/anyline/anyline/tree/master/anyline-data-jdbc-dialect)还没有的请联系群管理员
