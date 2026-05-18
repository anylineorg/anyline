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
</table>




# AnyLine MDM

**Metadata Dynamic Mapping — 元数据动态映射库**

![License](https://img.shields.io/badge/License-Apache%202.0-blue)
![Maven Central](https://img.shields.io/badge/Maven%20Central-org.anyline:anyline--core-green)
![JDK](https://img.shields.io/badge/JDK-1.8%2B-orange)

核心是一个面向运行时的 元数据动态映射库    
适配100+关系/非关系型数据库(及各种国产小众数据库)  
经常作为SQL解析引擎或适配器，用于解决高度动态化场景下异构数据库的统一操作与动态数据源管理问题

> 在传统 ORM 难以胜任的、表结构和数据源随时可能变化的环境中  
AnyLine 能够运行时动态注册、切换多种异构数据源，自动感知并适配不同数据库的元数据结构差异，
生成兼容各数据库方言的DDL/DML/DQL指令，从而为数据中台、低代码平台、SAAS 系统、自定义表单及异构数据库迁移同步等场景提供底层支撑，
屏蔽数据库差异、实现一次编写多库运行

---

## 设计思想
面向未知领域，由元数据驱动的动态建模
## 实现目标
建立跨数据库通用标准，实现异构数据库统一操作
## 实现机制
通过内置规则与外部插件合成方言转换引擎与元数据映射库
## 设计原则
> 面向动态，面向元数据，基于运行时

### 极简

源于极简的用户直觉，通过极致设计实现调用流程的简单自然，让操作体验符合用户直觉甚至能被"猜中"，达到所(猜)想即所得。

源于直觉不止于直觉：不盲从面向对象编程习惯，在元数据驱动的动态场景中**重新定义习惯**。

### 动态

摆脱静态属性束缚，实现高度灵活性与可扩展性。

- **运行时**：那只猫在运行时才能确定，编程时不确定

- **动态**：那只猫是会动的，甚至会变，有时会变成狗

- **元数据**：为应对诸多的不确定性，用更多维度的元数据将不确定性转化为确定性

### 多元

Spring ≠ Java，借鉴之应用之不要局限于。

以元数据动态映射为核心构建多元共生技术生态——打破框架边界，不止深度适配 Spring，更全面覆盖纯 Java、Vert.x、Quarkus、Solon 等 Java 领域各类运行环境。

---

## 核心功能与特征

### 1.动态数据源

> 支持运行时动态注册、切换与注销数据源(包括第三方数据源)， 实现数据源全生命周期灵活管控； 支持方法内任意切换数据源并智能维护事务状态，保障多数据源操作的原子性与一致性； 无缝集成Spring、Vert.x、Solon等主流Java框架； 天然适配低代码平台、多租户架构、数据中台等高频动态场景

- 7种注册方式
- 3种切换方式
- 方法内任意切换数据源并智能维护事务状态

---

### 2.面向数据库结构和元数据

> 以元数据驱动设计，摆脱传统 ORM 对静态实体类的依赖， 直接面向动态数据库结构与运行时元数据编程，实现从“静态绑定”到“动态感知”的转变。 采用动态数据结构承载业务数据，适配动态列、稀疏数据等复杂场景， 从而解耦业务逻辑与物理表结构。

- 元数据驱动设计

- 动态结构自适应

- 异构数据库整合兼容

---

### 3.异构数据库整合兼容

> 三层抽象实现对异构数据源的统一操作。底层采用插件化方言引擎，运行时将标准SQL语法动态转换为目标数据库原生指令。 中间层的运行时元数据解析器负责读取数据库结构信息，无需预定义实体类实现数据映射。上层则通过统一数据操作接口暴露标准DDL/DML方法，实现业务逻辑与底层数据源的完全解耦。

- 动态 DDL/DML/DQL 兼容

- 内置 **200+** 数据类型映射矩阵

- 内置 **1200+** 系统函数及参数映射矩阵

---

### 4.内存计算（DataSet/DataRow）

> 动态内存型数据容器，专为运行时灵活处理异构数据而设计‌。不依赖预定义的实体类，直接承载来自任意数据库表或查询的结果，并支持动态操作与二次计算。 提供了‌弱类型、动态结构的数据表示方式‌， 内置类SQL查询、聚合、过滤、格式转换及复杂数学计算，提升动态数据场景的开发效率与灵活性。

- 动态内存型数据容器，不依赖预定义的实体类

- 弱类型、动态结构的数据表示方式，动态适应结构变更

- 内置表达式引擎，支持类 SQL 查询、聚合、过滤及复杂数学计算

- 减少数据库往返，避免频繁查询与网络传输

---

### 5.动态查询条件

> 解决传统开发中手动拼接 SQL 字符串带来的安全性低、维护难、跨数据库兼容性差等问题。基于元数据驱动，允许在运行时通过结构化的数据格式（如 JSON、ConfigStore）自动构建安全、高效的数据库查询逻辑。 支持JSON/ConfigStore /SQL相互转换，特别适合处理异构数据源整合和复杂条件筛选。

- 数据中台的多维分析

- 低代码平台的自定义表单

- 动态报表/BI

---

### 6.适配最多的数据库

> 依托于核心的‌方言转换引擎‌与‌运行时动态元数据映射库，实现100+数据库支持, 涵盖从主流商业数据库到各类国产信创数据库及各种小众数据库 广义的数据库 也包含第三方平台(如dify、coze、ragflow等)。 关系型、键值、时序、图谱、文档、列簇、向量、搜索、空间、RDF、Event Store、MultiValue、Object 等类型数据库陆续集成中

- **100+** 已集成
- **300+** 在路上
- 有数据库的地方 就有AnyLine

---

## 核心应用场景

| 场景           | 说明                                                                                                                                    |
|--------------|---------------------------------------------------------------------------------------------------------------------------------------|
| **低代码后台**    | 动态数据源与元数据驱动天然适配低代码平台的数据层需求，运行时按需生成表结构与查询逻辑，零编码完成数据接入与操作。 结合动态表单构建与自定义查询功能，支持可视化数据适配和报表输出，从而降低技术门槛，提升平台的灵活性、可扩展性及开发效率。                 |
| **动态报表生成**   | 基于运行时元数据自动识别数据结构，按需组装查询条件与聚合逻辑，支撑报表字段与数据源的动态配置与即时输出。通过动态数据源适配、元数据管理与智能SQL生成，实现跨库、多维、高效率的报表构建‌。结合AnyLine-Office可进一步解析动态模板，支持复杂格式的自动化输出。 |
| **数据中台**     | 统一异构数据源的操作接口，屏蔽各数据库语法差异，实现跨库数据的统一查询、聚合和治理，为数据中台提供坚实的技术底座。无论是关系型数据库、NoSQL还是大数据平台，AnyLine 都能提供一致的操作体验，让数据工程师专注于业务逻辑而非底层适配。              |
| **可视化数据源**   | 凭借动态数据结构适配能力，灵活处理随业务变化的动态属性，自动调整数据模型以保障前端展示的准确性与一致性。 并提供丰富的数据转换和映射功能，支持前端所需的多维度、多结构数据重组。 可根据自定义统计方式和维度自动生成DQL语句，高效检索并转换数据，从而快速生成动态图表。 |
| **物联网 / 车联网** | 时序库原生集成，支持动态字段扩展，轻松应对设备上报的海量异构数据，凭借高效内存计算能力快速处理海量时序数据，内置丰富转换与映射功能完成数据清洗、聚合、转换；从时序库数据读室到业务系统适配，全程保障数据处理的实时性与准确。                        |
| **数据清洗和批量处理** | 以动态数据结构与强大的内存计算能力，应对大量异构且常不规范的数据：DataSet/DataRow内置丰富数学计算函数，能像操作数据库一样在内存完成聚合、去重、搜索等复杂操作，尤其擅长处理各类不符合标准甚至存在结构错误的数据，全程简化并加速数据处理流程。 |
| **信创改造工程**   | 为信创改造工程提供从适配到迁移的数据层支持：原生兼容多种国产数据库，实现业务系统快速迁移；自动方言转换统一各数据库SQL语法，确保系统兼容性；在双跑期间比对查询结果、表结构差异，保障迁移的准确性与稳定性，降低信创改造成本与风险。 |
| **工作流**      | AnyLine以动态元数据引擎为工作流提供全链路动态化支持，通过动态ORM特性实现无预定义实体类和配置文件的自定义表单与动态查询条件，提升工作流应对需求变化的适应性和可扩展性。 |
| **网络（）数据解析** |  |
| **异构数据库迁移同步** | 识别不同数据库间DDL语法、DML操作及数据类型的差异；其动态数据结构适配能力可自动识别源库与目标库的结构差异，生成相应SQL实现表结构创建与数据复制；丰富的数据转换与映射功能，处理数据类型与格式转换。 |
| **大模型数据分析**  | NLP 2 MDM 2 SQL——AI模型生成的SQL往往是通用或标准形式的，未必能直接在目标数据库上执行。AnyLine为NLP自然语言转SQL场景提供可靠的执行引擎。在链路中承担中间层角色提供理解数据库、操作数据库、验证结果的基础设施 |
| **项目快速交付**   | 动针对需求不明确或频繁变更、数据库类型不确定（尤其信创国产化）的项目场景，AnyLine通过动态特性支持按需动态调整数据结构， 无需重新设计表或编写大量转换代码；凭借灵活的数据结构适配与转换能力，可快速响应需求变更而无需大规模架构调整； |

---

## 面向用户

【[AnyLine MDM 用户群体深度分析](http://doc.anyline.org/aa/6a_15732)】

---

## 技术选型

- [AnyLine VS ORM：核心差异与选型指南](http://doc.anyline.org/aa/f1_15730)

- [AnyLine MDM 选型报告](http://doc.anyline.org/aa/27_15731)

### 快速决策指引

- 需要同时操作多种不同类型的数据库

- 表结构在运行时动态变化，无法预先定义实体类

- 面向低代码/数据中台/信创迁移等动态数据场景

- 需要跨库元数据采集、结构比对与 DDL 治理

- 查询条件由前端或配置动态生成，而非硬编码

---

## 生态与社区

| 资源            | 链接                                                                                        |
|---------------|-------------------------------------------------------------------------------------------|
| 官网            | [www.anyline.org](http://anyline.org)                                                     |
| 文档            | [doc.anyline.org](http://doc.anyline.org)                                                 |
| 快速入门指南        | [anyline.org/start](http://doc.anyline.org/ss/f5_1150)                                    |
| 架构图           | [anyline.org/architecture](hhttps://deepwiki.com/anylineorg/anyline)                               |
| GitHub        | [github.com/anyline](https://github.com/anylineorg/anyline)                               |
| Gitee         | [gitee.com/anyline](https://gitee.com/anyline)                                            |
| Maven Central | [search.maven.org - anyline](https://mvnrepository.com/artifact/org.anyline/anyline-core) |
| 示例项目          | [anyline/anyline-simple](https://gitee.com/anyline/anyline-simple)                        |
| 测试用例          | [6,056](http://doc.anyline.org/cs)                                                        |




## 已经有了ORM了，为什么还要用AnyLine，两者有什么不同：
其中最显著的不同是：AnyLine主要是用来操作数据库结构(如自动合成、执行DDL)以及读写元数据(比如读出来的数据可以带数据类型、精度、约束、默认值等)
同时适配100+关系及非关系型数据库
- **一、面向场景不同‌**  
  **AnyLine**：专为高度动态化的运行时场景提供底层适配，其核心优势在于对运行时不确定性的原生支持。
  系统需要处理在任意时刻由不同用户发起的动态数据源接入请求，这些请求往往伴随着完全异构的数据结构和访问协议。
  更关键的是，数据源的表结构、字段定义等元数据信息会随着业务需求或数据提供方的变化而实时调整。
  通过动态元数据管理机制和自适应映射机制，能够在运行时感知变化，完成数据模型的动态重构和查询适配，实现"变化即常态"的动态支持能力。    
  **传统ORM**‌：更适用于静态或相对稳定的业务场景，其典型特征包括：在系统开发阶段即可明确定义数据库表结构、实体类关系等核心数据模型，且这些基础架构元素在后续运行维护过程中保持相对稳定，不会出现频繁的结构性变更。
  能够充分发挥预定义架构的优势，通过前期完善的建模和设计，确保系统在稳定的环境中运行。


- **二、针对产品不同‌**  
  **AnyLine**：主要定位于中间层开发平台，其典型应用场景是构建低代码平台、动态查询引擎等中间产品，而非直接开发面向终端用户的业务系统（如ERP、CRM等）。
  通过动态建模和灵活配置能力，可以快速搭建业务工具平台，使最终用户能够自主创建满足个性化需求的业务应用，例如基于可视化配置的自定义查询分析工具，让业务人员无需编码即可按需生成动态报表和数据分析视图。
  这种"平台赋能用户"的模式下，更能充分发挥AnyLine在动态适配和快速迭代方面的技术优势。    
  **传统ORM**‌：主要应用于终端业务系统的直接开发，如ERP、CRM、OA等企业管理软件。它通过对象关系映射技术，将数据库表结构映射为编程语言中的对象模型，使开发者能够以面向对象的方式操作数据库。特别适合业务模型相对固定的应用场景。


- **三、操作对象不同‌**  
  **AnyLine**：采用元数据驱动的模式，其核心在于对数据结构和业务逻辑的抽象化处理。在项目初期阶段，当具体业务对象和属性尚未明确定义时，AnyLine通过元数据管理机制，允许开发者以动态配置的方式定义数据模型和业务规则。
  使系统能够灵活适应业务需求的变化，支持用户在缺乏完整对象模型的情况下，通过元数据操作完成业务数据的设计、建模和交互。这种元数据优先的架构特别适合需求不确定或快速迭代的项目场景，为业务系统的渐进式开发提供了有力支撑。   
  **传统ORM**‌：核心在于操作与数据库表结构直接映射的实体类及其属性。通过建立对象模型与关系型数据库之间的映射关系，使开发者能够以面向对象的方式操作数据。
  将数据库表映射为编程语言中的类，表中的字段对应类的属性，表间关系则通过对象间的关联关系来体现。特别适合业务模型稳定、数据结构明确的系统开发场景。


- **四、面向用户不同‌**  
  **AnyLine**：主要面向系统架构师和底层框架开发者，特别适合需要构建高度灵活、可扩展应用系统的技术团队。它通过创新的动态元数据引擎和强大的结果集处理能力，为开发者提供了在运行时动态定义数据结构、业务规则和数据处理流程的能力。
  有效解决传统开发中因需求变更导致的系统重构问题，使开发团队能够快速响应业务变化，特别适用于需要支持多租户、可配置业务模型的SaaS平台和低代码开发场景    
  **传统ORM**：主要服务于广大应用开发人员，特别适合需要快速构建和维护基于关系型数据库的业务系统的开发团队。通过实现对象与关系型数据库之间的自动化映射，使开发者能够完全以面向对象的方式进行数据库操作。
  不仅大幅降低数据库访问层的开发难度，还显著提升系统的整体性能和开发效率。特别适合业务需求明确、数据结构相对固定的企业应用开发场景。


- **五、对用户要求不同‌**    
  **AnyLine**：对用户(特别是设计人员)提出了更高的技术要求，主要面向具备系统架构思维的技术团队。需要用户深入理解元数据驱动开发理念，掌握动态数据模型的设计方法，并能够将业务需求转化为可配置的元数据规则。
  这要求开发团队不仅要熟悉底层数据结构和业务逻辑，还需要具备动态系统设计经验，能够预见并处理运行时可能出现的各种业务场景变化。    
  **传统ORM**‌：则相对更易于上手和使用。ORM框架通过提供直观的映射关系和面向对象的数据库操作方式，降低了数据库操作的门槛。即使是没有丰富数据库操作经验的开发人员，也能通过ORM框架快速上手并开发出功能完善的应用系统。


- **六、设计理念与实现方式不同**
  >**动态 VS 静态**    
  **AnyLine**：基于运行时元数据驱动，支持动态数据源注册（如用户运行时提供数据库地址/类型），无需预定义实体类或映射关系。  
  **传统ORM**：依赖静态实体类与数据库表的预映射，需提前配置方言和表结构。

  >**元数据操作 VS 对象操作**  
  **AnyLine**：面向数据库元数据（如表结构、字段类型），适用于低代码平台或未知业务场景。  
  **传统ORM**：通过对象模型（Class/Property）间接操作数据库，需预先定义对象关系

  >**多数据库适配**  
  **AnyLine**：通过动态元数据引擎和智能方言适配，实现异构数据库的无缝兼容。内置200+种SQL语法转换规则，自动识别数据库类型并生成目标方言SQL，将不同数据库的元数据抽象为标准化对象，通过统一接口操作，实现元数据对象在各个数据库之间无缝兼容。    
  **‌传统ORM**：需硬编码实体类和方言，无法动态适配异构数据库的元数据差异和自动转换SQL语法，扩展性和兼容性受限。




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
DataSet<DataRow> set = ServiceProxy.service("ds_sso").queries("SSO_USER");
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
service.queries("SSO_USER", 
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


## 误解
当然我们并不是要抛弃Entity或ORM，不同的场景确实需要不同的解决方案，而 AnyLine 的设计理念正是为了提供灵活性和扩展性，
同时不排斥传统的 Entity 或 ORM 使用。

**AnyLine 与 Entity/ORM 互补而非替代‌**    
Entity/ORM 在 ‌可预知、固定‌ 的场景下具有强类型、编译时检查、代码可读性强等优势，适合业务逻辑稳定、数据结构明确的场景。  
AnyLine 则专注于 ‌动态、运行时‌ 的场景，如数据中台、多数据源、动态查询条件、结果集灵活处理等，解决传统 ORM 在这些场景下的不足。

## 如何使用
数据操作***不要***再从生成xml/dao/service以及各种配置各种O开始  
默认的service已经提供了大部分的数据库操作功能。  
操作过程大致如下:
```
DataSet<DataRow> set = service.queries("hr_usr(ID, NM)", 
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
EntitySet<User> = service.queries(User.class, 
    condition(true, "anyline根据约定自动生成的查询条件")); 
//true：表示需要分页
//为什么不用返回的是一个EntitySet而不是List?
//因为分页情况下, EntitySet中包含了分页数据, 而List不行。
//无论是否分页都返回相同的数据结构，而不需要根据是否分页实现两个接口返回不同的数据结构

//也可以这样(如果真要这样就不要用anyline了, 还是用MyBatis, Hibernate之类吧)
public class UserService extends AnylinseService<User> 
userService.queries(condition(true, "anyline根据约定自动生成的查询条件")); 
```
