
<div align="center">

# AnyLine MDM

**Metadata Dynamic Mapping — 元数据动态映射**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-org.anyline:anyline--core-green)](https://mvnrepository.com/artifact/org.anyline/anyline-core)
[![JDK](https://img.shields.io/badge/JDK-8~25-orange)](https://openjdk.org/)
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)


</div>

---

💡核心是一个面向运行时的 元数据动态映射库  
适配100+关系/非关系型数据库(及各种国产小众数据库)  
经常作为SQL解析引擎或适配器，用于解决高度动态化场景下异构数据库的统一操作与动态数据源管理问题
> 在传统 ORM 难以胜任的、表结构和数据源未知甚至随时变化的环境中
AnyLine 支持运行时动态注册、切换多种异构数据源，自动感知并适配不同数据库的元数据结构差异， 生成兼容各数据库方言的DDL/DML/DQL指令，从而为数据中台、低代码平台、SAAS 系统、自定义表单及异构数据库迁移同步等场景提供底层支撑， 屏蔽数据库差异、实现一次编写多库运行

<br>
<table style="text-align: center;">
  <tr>
  <td><a href="https://qm.qq.com/q/M5ub6mqS0o"><img src="https://cdn.anyline.org/img/user/alq.png" width="130"/></a></td>
  <td><a href="http://www.anyline.org/ss/9f_17"><img src="https://cdn.anyline.org/img/user/alvg.png" width="130"></a></td>
  <td><a href="http://www.anyline.org/ss/9f_17"><img src="https://cdn.anyline.org/img/user/alv.png" width="130"></a></td>
  </tr>
  <tr>
  <td><b>QQ 群</b><br/>86020680</td>
  <td><b>微信群</b></td>
  <td><b>过期/满员</b><br/>联系管理员</td>
  </tr>
</table>

预置 AI 智能体 SKILL 定义位于 <code>./.ai/skills/anyline/SKILL.md</code>

##  设计初衷

**应对未知领域的不确定性**

在设计/开发阶段无法预知未来的数据结构、接入的数据源类型以及业务数据需求变动的背景下，通过元数据驱动、动态适配等方式赋予系统应对未知的能力——让系统不必在开发阶段预设一切，也能在运行时适应各类不确定性变化。

<br>

##  设计思想
面向动态，面向运行时，基于元数据
<table>
<tr>
<td width="33%" valign="top">

### 运行时
那只猫在运行时才能确定，编程时不确定

</td>
<td width="33%" valign="top">

### 动态
那只猫是会动的，甚至会变，有时会变成狗

</td>
<td width="34%" valign="top">

### 元数据
为应对诸多的不确定性，用更多维度的元数据将不确定性转化为确定性

</td>
</tr>
</table>

## ▎ 实现原则

<table>
<tr>
<td width="50%" valign="top">

### 极简
源于极简的用户直觉，操作体验高度契合开发者编程习惯，甚至能被"猜中"，达到所想即所得。

不止于直觉——不盲从面向对象习惯，在元数据驱动的动态场景中，重新定义操作未知数据的习惯。

</td>
<td width="50%" valign="top">

###  多元
构建基于 Java 的多元技术生态。

**Spring ≠ Java**——借鉴之、应用之，不要局限于。

全面覆盖 Vert.x、Quarkus、Solon 等各类非 Spring 运行环境。

</td>
</tr>
</table>

<br>

##  实现机制

通过内置规则与外部插件合成**方言转换引擎**与**元数据映射库**，建立跨数据库的通用标准，实现异构数据库的统一操作。


<br>

##  核心功能

<table>
<tr>
<td width="50%" valign="top">

### 动态数据源
支持运行时动态注册、切换与注销数据源(包括第三方数据源)， 实现数据源全生命周期灵活管控； 支持方法内任意切换数据源并智能维护事务状态，保障多数据源操作的原子性与一致性； 无缝集成Spring、Vert.x、Solon等主流Java框架； 天然适配低代码平台、多租户架构、数据中台等高频动态场景

- 7 种注册方式
- 3 种切换方式
- 方法内任意切换数据源并智能维护事务状态

</td>
<td width="50%" valign="top">

### 面向数据库结构与元数据
以元数据驱动设计，摆脱传统 ORM 对静态实体类的依赖， 直接面向动态数据库结构与运行时元数据编程，实现从“静态绑定”到“动态感知”的转变。 采用动态数据结构承载业务数据，适配动态列、稀疏数据等复杂场景， 从而解耦业务逻辑与物理表结构。

- 元数据驱动设计
- 动态结构自适应
- 异构数据库整合兼容

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 异构数据库整合兼容
三层抽象实现对异构数据源的统一操作。底层采用插件化方言引擎，运行时将标准SQL语法动态转换为目标数据库原生指令。 中间层的运行时元数据解析器负责读取数据库结构信息，无需预定义实体类实现数据映射。上层则通过统一数据操作接口暴露标准DDL/DML方法，实现业务逻辑与底层数据源的完全解耦。

- 动态 DDL/DML/DQL 兼容
- 内置 **200+** 数据类型映射
- 内置 **1,200+** 系统函数映射

</td>
<td width="50%" valign="top">

### 内存计算（DataSet/DataRow）
动态内存型数据容器，专为运行时灵活处理异构数据而设计‌。不依赖预定义的实体类，直接承载来自任意数据库表或查询的结果，并支持动态操作与二次计算。 提供了‌弱类型、动态结构的数据表示方式‌， 内置类SQL查询、聚合、过滤、格式转换及复杂数学计算，提升动态数据场景的开发效率与灵活性。
- 动态内存型数据容器，不依赖预定义的实体类
- 弱类型、动态结构的数据表示方式，动态适应结构变更
- 内置表达式引擎，支持类 SQL 查询、聚合、过滤及复杂数学计算
- 减少数据库往返，避免频繁查询与网络传输

</td>
</tr>
<tr>
<td width="50%" valign="top">

### 动态查询条件
解决传统开发中手动拼接 SQL 字符串带来的安全性低、维护难、跨数据库兼容性差等问题。基于元数据驱动，允许在运行时通过结构化的数据格式（如 JSON、ConfigStore）自动构建安全、高效的数据库查询逻辑。 支持JSON/ConfigStore /SQL相互转换，特别适合处理异构数据源整合和复杂条件筛选。

- 数据中台的多维分析
- 低代码平台的自定义表单
- 动态报表 / BI

</td>
<td width="50%" valign="top">

### 适配最多的数据库
依托于核心的‌方言转换引擎‌与‌运行时动态元数据映射库，实现100+数据库支持, 涵盖从主流商业数据库到各类国产信创数据库及各种小众数据库 广义的数据库 也包含第三方平台(如dify、coze、ragflow等)。 关系型、键值、时序、图谱、文档、列簇、向量、搜索、空间、RDF、Event Store、MultiValue、Object 等类型数据库陆续集成中

- **100+** 已集成
- **300+** 在路上
- 有数据库的地方就有 AnyLine

</td>
</tr>
</table>

<br>

## ▎ 核心应用场景

| 场景              | 说明 |
|:----------------|:-----|
| **低代码后台**       | 动态数据源与元数据驱动天然适配低代码平台的数据层需求，运行时按需生成表结构与查询逻辑，零编码完成数据接入与操作。 结合动态表单构建与自定义查询功能，支持可视化数据适配和报表输出，从而降低技术门槛，提升平台的灵活性、可扩展性及开发效率。 |
| **动态报表生成**      | 基于运行时元数据自动识别数据结构，按需组装查询条件与聚合逻辑，支撑报表字段与数据源的动态配置与即时输出。通过动态数据源适配、元数据管理与智能SQL生成，实现跨库、多维、高效率的报表构建‌。结合AnyLine-Office可进一步解析动态模板，支持复杂格式的自动化输出。 |
| **数据中台**        | 统一异构数据源的操作接口，屏蔽各数据库语法差异，实现跨库数据的统一查询、聚合和治理，为数据中台提供坚实的技术底座。无论是关系型数据库、NoSQL还是大数据平台，AnyLine 都能提供一致的操作体验，让数据工程师专注于业务逻辑而非底层适配。 |
| **可视化数据源**      | 凭借动态数据结构适配能力，灵活处理随业务变化的动态属性，自动调整数据模型以保障前端展示的准确性与一致性。 并提供丰富的数据转换和映射功能，支持前端所需的多维度、多结构数据重组。 可根据自定义统计方式和维度自动生成DQL语句，高效检索并转换数据，从而快速生成动态图表。 |
| **物联网 / 车联网**   | 时序库原生集成，支持动态字段扩展，轻松应对设备上报的海量异构数据，凭借高效内存计算能力快速处理海量时序数据，内置丰富转换与映射功能完成数据清洗、聚合、转换；从时序库数据读室到业务系统适配，全程保障数据处理的实时性与准确。 |
| **数据清洗与批量处理**   | 以动态数据结构与强大的内存计算能力，应对大量异构且常不规范的数据：DataSet/DataRow内置丰富数学计算函数，能像操作数据库一样在内存完成聚合、去重、搜索等复杂操作，尤其擅长处理各类不符合标准甚至存在结构错误的数据，全程简化并加速数据处理流程。 |
| **信创改造工程**      | 为信创改造工程提供从适配到迁移的数据层支持：原生兼容多种国产数据库，实现业务系统快速迁移；自动方言转换统一各数据库SQL语法，确保系统兼容性；在双跑期间比对查询结果、表结构差异，保障迁移的准确性与稳定性，降低信创改造成本与风险。 |
| **工作流**         | AnyLine以动态元数据引擎为工作流提供全链路动态化支持，通过动态ORM特性实现无预定义实体类和配置文件的自定义表单与动态查询条件，提升工作流应对需求变化的适应性和可扩展性。 |
| **数据血缘分析**      | 为数据血缘系统提供三层递进的元数据支撑： 首先在抽取层深入到列属性级别，完整获取细粒度结构信息，支撑字段级血缘映射与链路节点精确识别； 同时内置异构类型标准化能力，将不同方言的原生字段类型通过别名链统一归约为标准类型体系，使血缘节点跨源匹配不再受方言干扰，异构兼容在 MDM 层即已消化； 在此基础上，通过指纹对表结构关键属性计算版本标识，定期或触发式重新抽取后对比新旧指纹， 自动识别字段增删、类型变更、约束变化等列级差异，推送增量到血缘系统，避免全量重建，使血缘地图始终与真实数据结构保持同步。 |
| **异构数据库迁移同步**   | 识别不同数据库间DDL语法、DML操作及数据类型的差异；其动态数据结构适配能力可自动识别源库与目标库的结构差异，生成相应SQL实现表结构创建与数据复制；丰富的数据转换与映射功能，处理数据类型与格式转换。 |
| **大模型数据分析**     | NLP → MDM → SQL：AI模型生成的SQL往往是通用或标准形式的，未必能直接在目标数据库上执行。AnyLine为NLP自然语言转SQL场景提供可靠的执行引擎。在链路中承担中间层角色：通过动态数据源适配能力识别目标数据库的类型与版本，支撑跨库可执行性。 同时将数据库连接、元数据采集、SQL生成与方言适配等底层复杂能力封装为统一接口，让开发者只需专注于AI模型的训练与调优，降低NL2SQL技术的落地门槛。 作为AI提供理解数据库、操作数据库、验证结果的基础设施，使AI专注于语义理解与意图识别，两者协同构成从自然语言到可信查询结果的准确性。 |
| **项目快速交付**     | 动针对需求不明确或频繁变更、数据库类型不确定（尤其信创国产化）的项目场景，AnyLine通过动态特性支持按需动态调整数据结构， 无需重新设计表或编写大量转换代码；凭借灵活的数据结构适配与转换能力，可快速响应需求变更而无需大规模架构调整； |

<br>

## ▎技术选型

<details>
<summary>快速决策指引</summary>

- ✅ 需要同时操作多种不同类型的数据库
- ✅ 表结构在运行时动态变化，无法预先定义实体类
- ✅ 面向低代码 / 数据中台 / 信创迁移等动态数据场景
- ✅ 需要跨库元数据采集、结构比对与 DDL 治理
- ✅ 查询条件由前端或配置动态生成，而非硬编码

</details>

**深度对比**
- [AnyLine VS ORM：核心差异与选型指南](http://doc.anyline.org/aa/f1_15730)
- [AnyLine MDM 选型报告](http://doc.anyline.org/aa/27_15731)
- [AnyLine 与传统 ORM 的设计思想对比](http://doc.anyline.org/aa/3b_15735)
- [AnyLine MDM 用户群体深度分析](http://doc.anyline.org/aa/6a_15732)

<br>

## ▎如何集成

只需要**一个依赖 + 一个注解**即可实现与 Spring Boot、Netty 等框架项目完美整合

📖 [入门系列](http://doc.anyline.org/ss/f5_1150) · 💻 [示例代码](https://gitee.com/anyline/anyline-simple) · 🧪 [测试用例 6,056+](http://doc.anyline.org/cs)

<details>
<summary> 数据源注册与切换</summary>

> 注意：这里的数据源并不是主从关系，而是多个完全不相关的数据源

```java
DataSource ds_sso = new DruidDataSource();
ds_sso.setUrl("jdbc:mysql://localhost:3306/sso");
ds_sso.setDriverClassName("com.mysql.cj.jdbc.Driver");
...
        DataSourceHolder.reg("ds_sso", ds_sso);
// 或
DataSourceHolder.reg("ds_sso", pool, driver, url, user, password);
DataSourceHolder.reg("ds_sso", Map<String, Object> params); // 对应连接池的属性 k-v

// 查询 ds_sso 数据源的 SSO_USER 表
DataSet<DataRow> set = ServiceProxy.service("ds_sso").selects("SSO_USER");
```

静态配置文件（Spring 环境可按 Spring 格式）：

```properties
# 默认数据源
anyline.datasource.type=com.zaxxer.hikari.HikariDataSource
anyline.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.url=jdbc:mysql://localhost:33306/simple
anyline.datasource.user-name=root

# 其他数据源
anyline.datasource-list=crm,erp,sso,mg

anyline.datasource.crm.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.crm.url=jdbc:mysql://localhost:33306/simple_crm
anyline.datasource.crm.username=root

anyline.datasource.erp.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.erp.url=jdbc:mysql://localhost:33306/simple_erp
anyline.datasource.erp.username=root
```

</details>

<details>
<summary> DML 动态查询</summary>

```java
// Web 环境下
service.queries("SSO_USER",
                condition(true, "NAME:%name%", "TYPE:[type]", "[CODES]:code"));
// true 表示需要分页，没有传参值的条件默认忽略
// 生成 SQL:
// SELECT * FROM SSO_USER WHERE 1=1
// AND NAME LIKE '%?%'
// AND TYPE IN(?,?,?)
// AND FIND_IN_SET(?, CODES)
// LIMIT 5,10  -- 根据具体数据库类型

// 用户自定义查询条件（低代码等场景一般需要更复杂的查询条件）
ConfigStore configs;
service.select("SSO_USER", configs);
// ConfigStore 提供了所有的 SQL 操作
```

</details>

<details>
<summary> 读写元数据</summary>

```java
@Autowired("anyline.service")
AnylineService service;

// 查询默认数据源的 SSO_USER 表结构
Table table = service.metadata().table("SSO_USER");
LinkedHashMap<String, Column> columns = table.getColumns();             // 表中的列
LinkedHashMap<String, Constraint> constraints = table.getConstraints(); // 表中的约束
List<String> ddls = table.getDdls();                                    // 表的创建 SQL

// 删除表并重新创建
service.ddl().drop(table);
table = new Table("SSO_USER");

// 数据类型随便写，不用管是 int8 还是 bigint，执行时会转换成正确的类型
table.addColumn("ID", "BIGINT").autoIncrement(true).setPrimary(true).setComment("主键");
table.addColumn("CODE", "VARCHAR(20)").setComment("编号");
table.addColumn("NAME", "VARCHAR(20)").setComment("姓名");
table.addColumn("AGE", "INT").setComment("年龄");
service.ddl().create(table);

// 或者 service.ddl().save(table); 执行时会区分哪些列需要 ADD，哪些需要 ALTER
```

</details>

<details>
<summary>事务管理</summary>

```java
// 因为方法可以随时切换多次数据源，所以注解已经捕捉不到当前数据源了
// 更多事务参数通过 TransactionDefine 获取
TransactionState state = TransactionProxy.start("ds_sso");
// 操作数据
TransactionProxy.commit(state);
TransactionProxy.rollback(state);
```

</details>

<details>
<summary>兼容传统 ORM</summary>

如果实在放不下那些已存在的各种 XOOO，DataSet 与 Entity 之间可以相互转换：

```java
EntitySet<User> = service.queries(User.class,
                                  condition(true, "anyline 根据约定自动生成的查询条件"));
// true：表示需要分页
// 为什么返回 EntitySet 而不是 List？
// 因为分页情况下 EntitySet 中包含了分页数据，而 List 不行
// 无论是否分页都返回相同的数据结构

// 也可以这样（如果真要这样就不要用 AnyLine 了，还是用 MyBatis/Hibernate 之类吧）
public class UserService extends AnylineService<User> 
userService.queries(condition(true, "anyline 根据约定自动生成的查询条件")); 
```

</details>

<br>

## ▎生态与社区

| 资源          |  链接 |
|:--------------|:-------|
| 官网            | [www.anyline.org](http://anyline.org) |
| 文档            | [doc.anyline.org](http://doc.anyline.org) |
| 快速入门          | [anyline.org/start](http://doc.anyline.org/ss/f5_1150) |
| 架构图           | [deepwiki.com/anylineorg/anyline](https://deepwiki.com/anylineorg/anyline) |
| GitHub        | [github.com/anylineorg/anyline](https://github.com/anylineorg/anyline) |
| Gitee         | [gitee.com/anyline](https://gitee.com/anyline) |
| Maven Central | [mvnrepository.com](https://mvnrepository.com/artifact/org.anyline/anyline-core) |
| 示例项目          | [anyline-simple](https://gitee.com/anyline/anyline-simple) |
| 测试用例          | [6,056+](http://doc.anyline.org/cs) |
  