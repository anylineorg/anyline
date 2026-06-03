
<div align="center">

# AnyLine MDM

**Metadata Dynamic Mapping**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-org.anyline:anyline--core-green)](https://mvnrepository.com/artifact/org.anyline/anyline-core)
[![JDK](https://img.shields.io/badge/JDK-8~25-orange)](https://openjdk.org/)
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)


</div>

---

💡 At its core, a runtime-oriented metadata dynamic mapping library  
Supports 100+ relational and non-relational databases (including various domestic Xinchuang and niche databases)  
Commonly used as an SQL parsing engine or adapter to solve the problem of unified operations across heterogeneous databases and dynamic data source management in highly dynamic scenarios

> In environments where traditional ORM falls short — where table structures and data sources are unknown or even constantly changing —  
AnyLine supports runtime dynamic registration and switching of multiple heterogeneous data sources, automatically senses and adapts to metadata structure differences across databases,  
and generates DDL/DML/DQL instructions compatible with each database dialect — shielding database differences and enabling write-once-run-anywhere across databases

<br>
<table style="text-align: center;">
  <tr>
  <td><a href="https://qm.qq.com/q/M5ub6mqS0o"><img src="https://cdn.anyline.org/img/user/alq.png" width="130"/></a></td>
  <td><a href="http://www.anyline.org/ss/9f_17"><img src="https://cdn.anyline.org/img/user/alvg.png" width="130"></a></td>
  <td><a href="http://www.anyline.org/ss/9f_17"><img src="https://cdn.anyline.org/img/user/alv.png" width="130"></a></td>
  </tr>
  <tr>
  <td><b>QQ Group</b><br/>86020680</td>
  <td><b>WeChat Group</b></td>
  <td><b>Expired / Full</b><br/>Contact admin</td>
  </tr>
</table>

The preset AI agent SKILL definition is located at <code>./.ai/skills/anyline/SKILL.md</code>

## Design Philosophy

**Addressing the uncertainty of unknown domains**

When it is impossible to predict future data structures, data source types to be connected, and changes in business data requirements during the design/development phase, AnyLine empowers systems with the ability to handle the unknown through metadata-driven dynamic adaptation — enabling systems to adapt to various uncertain changes at runtime without having to predefine everything during development.

<br>

## Design Principles
Oriented toward the dynamic, oriented toward the runtime, based on metadata
<table>
<tr>
<td width="33%" valign="top">

### Runtime
The cat in the box can only be determined at runtime, not at compile time

</td>
<td width="33%" valign="top">

### Dynamic
The cat in the box moves — and sometimes turns into a dog

</td>
<td width="34%" valign="top">

### Metadata
To handle numerous uncertainties, use higher-dimensional metadata to collapse the superposition into certainty

</td>
</tr>
</table>

## Implementation Guidelines

<table>
<tr>
<td width="50%" valign="top">

### Minimalist
Derived from minimalist user intuition, the operation experience closely aligns with developer programming habits and can even be "guessed" — achieving think-and-get.

Beyond intuition — not blindly following object-oriented conventions; redefining the habits of operating unknown data in metadata-driven dynamic scenarios.

</td>
<td width="50%" valign="top">

### Diverse
Build a diverse Java-based technology ecosystem.

**Spring ≠ Java** — learn from it, apply it, but do not be limited by it.

Fully cover non-Spring runtime environments such as Vert.x, Quarkus, and Solon.

</td>
</tr>
</table>

<br>

## Implementation Mechanism

Synthesizes a **dialect conversion engine** and **metadata mapping library** through built-in rules and external plugins, establishing cross-database universal standards and enabling unified operations across heterogeneous databases.


<br>

## Core Features

<table>
<tr>
<td width="50%" valign="top">

### Dynamic Data Sources
Supports runtime dynamic registration, switching, and deregistration of data sources (including third-party data sources), enabling flexible lifecycle management of data sources. Supports arbitrary data source switching within a method with intelligent transaction state maintenance, ensuring atomicity and consistency of multi-data-source operations. Seamlessly integrates with mainstream Java frameworks such as Spring, Vert.x, and Solon. Naturally adapts to high-frequency dynamic scenarios such as low-code platforms, multi-tenant architectures, and data middle platforms.

- 7 registration methods
- 3 switching methods
- Arbitrary data source switching within a method with intelligent transaction state maintenance

</td>
<td width="50%" valign="top">

### Oriented Toward Database Structure and Metadata
Driven by metadata design, it breaks free from the dependency of traditional ORM on static entity classes, directly programming against dynamic database structures and runtime metadata, achieving the transition from "static binding" to "dynamic perception." It uses dynamic data structures to carry business data, adapting to complex scenarios such as dynamic columns and sparse data, thereby decoupling business logic from physical table structures.

- Metadata-driven design
- Dynamic structure self-adaptation
- Heterogeneous database integration and compatibility

</td>
</tr>
<tr>
<td width="50%" valign="top">

### Heterogeneous Database Integration and Compatibility
Three-layer abstraction enables unified operations across heterogeneous data sources. The bottom layer uses a pluggable dialect engine that dynamically converts standard SQL syntax into target database native instructions at runtime. The middle layer's runtime metadata parser reads database structure information, achieving data mapping without predefined entity classes. The top layer exposes standard DDL/DML methods through a unified data operation interface, achieving complete decoupling of business logic from underlying data sources.

- Dynamic DDL/DML/DQL compatibility
- Built-in **200+** data type mappings
- Built-in **1,200+** system function mappings

</td>
<td width="50%" valign="top">

### In-Memory Computation (DataSet/DataRow)
A dynamic in-memory data container designed for flexible processing of heterogeneous data at runtime. It does not depend on predefined entity classes; it directly carries results from any database table or query and supports dynamic operations and secondary computation. It provides a weakly-typed, dynamic-structure data representation with built-in SQL-like queries, aggregation, filtering, format conversion, and complex mathematical calculations.

- Dynamic in-memory data container, independent of predefined entity classes
- Weakly-typed, dynamic-structure data representation that dynamically adapts to structural changes
- Built-in expression engine supporting SQL-like queries, aggregation, filtering, and complex mathematical calculations
- Reduces database round trips, avoiding frequent queries and network transmission

</td>
</tr>
<tr>
<td width="50%" valign="top">

### Dynamic Query Conditions
Solves the problems of low security, difficult maintenance, and poor cross-database compatibility caused by manually concatenating SQL strings in traditional development. Based on metadata-driven design, it allows automatic construction of secure and efficient database query logic at runtime through structured data formats (such as JSON, ConfigStore). Supports mutual conversion between JSON/ConfigStore/SQL, particularly suitable for handling heterogeneous data source integration and complex condition filtering.

- Multi-dimensional analysis in data middle platforms
- Custom forms in low-code platforms
- Dynamic reports / BI

</td>
<td width="50%" valign="top">

### Broadest Database Support
Leveraging the core dialect conversion engine and runtime dynamic metadata mapping library, it supports 100+ databases, covering mainstream commercial databases, various domestic Xinchuang databases, and niche databases. In a broader sense, databases also include third-party platforms (such as dify, coze, ragflow, etc.). Relational, key-value, time-series, graph, document, column-family, vector, search, spatial, RDF, Event Store, MultiValue, Object and other database types are being progressively integrated.

- **100+** integrated
- **300+** on the way
- Where there is a database, there is AnyLine

</td>
</tr>
</table>

<br>

## Core Application Scenarios

| Scenario | Description |
|:---------|:------------|
| **Low-Code Backends** | Dynamic data sources and metadata-driven design naturally adapt to the data layer needs of low-code platforms, generating table structures and query logic on demand at runtime, enabling zero-code data access and operations. Combined with dynamic form construction and custom query features, it supports visual data adaptation and report output, lowering technical barriers and improving platform flexibility, scalability, and development efficiency. |
| **Dynamic Report Generation** | Automatically identifies data structures based on runtime metadata, assembles query conditions and aggregation logic on demand, and supports dynamic configuration and instant output of report fields and data sources. Through dynamic data source adaptation, metadata management, and intelligent SQL generation, it achieves cross-database, multi-dimensional, and efficient report construction. Combined with AnyLine-Office, it can further parse dynamic templates and support automated output in complex formats. |
| **Data Middle Platform** | Provides a unified operation interface for heterogeneous data sources, shields database syntax differences, and enables unified cross-database querying, aggregation, and governance, providing a solid technical foundation for data middle platforms. Whether relational databases, NoSQL, or big data platforms, AnyLine delivers a consistent operation experience, allowing data engineers to focus on business logic rather than low-level adaptation. |
| **Visual Data Sources** | With dynamic data structure adaptation capabilities, it flexibly handles dynamic attributes that change with business needs, automatically adjusting data models to ensure accuracy and consistency in frontend display. It also provides rich data transformation and mapping functions, supporting multi-dimensional, multi-structure data reorganization required by the frontend. It can automatically generate DQL statements based on custom statistical methods and dimensions, efficiently retrieving and transforming data to quickly produce dynamic charts. |
| **IoT / IoV** | Native integration with time-series databases, support for dynamic field extension, and easy handling of massive heterogeneous data reported by devices. Leveraging efficient in-memory computation capabilities to rapidly process massive time-series data, with built-in rich transformation and mapping functions for data cleansing, aggregation, and conversion. From time-series database data reads to business system adaptation, the entire process ensures real-time and accurate data processing. |
| **Data Cleansing and Batch Processing** | With dynamic data structures and powerful in-memory computation capabilities, it handles large volumes of heterogeneous and often non-standard data. DataSet/DataRow comes with rich mathematical calculation functions, enabling in-memory aggregation, deduplication, search, and other complex operations just like operating a database. It excels particularly at handling data that does not conform to standards or even has structural errors, simplifying and accelerating the entire data processing workflow. |
| **Xinchuang Migration Projects** | Provides data layer support from adaptation to migration for Xinchuang migration projects: native compatibility with multiple domestic databases, enabling rapid migration of business systems; automatic dialect conversion unifies SQL syntax across databases, ensuring system compatibility; during dual-run periods, it compares query results and table structure differences, ensuring migration accuracy and stability, reducing Xinchuang migration costs and risks. |
| **Workflows** | AnyLine provides full-chain dynamic support for workflows through its dynamic metadata engine, implementing custom forms and dynamic query conditions without predefined entity classes and configuration files through dynamic ORM features, improving workflow adaptability and scalability in response to requirement changes. |
| **Data Lineage Analysis** | Provides three-tier progressive metadata support for data lineage systems: First, at the extraction layer, it goes deep into column attribute levels, fully obtaining fine-grained structural information to support field-level lineage mapping and precise link node identification. Meanwhile, built-in heterogeneous type standardization capability unifies native field types of different dialects into a standard type system through alias chains, so lineage node cross-source matching is no longer affected by dialect differences, and heterogeneous compatibility is already resolved at the MDM layer. On this basis, it calculates version identifiers for key table structure attributes through fingerprints, and after periodic or triggered re-extraction, compares new and old fingerprints to automatically identify column-level differences such as field additions/deletions, type changes, and constraint changes, pushing increments to the lineage system to avoid full reconstruction, keeping the lineage map always in sync with the actual data structure. |
| **Heterogeneous Database Migration and Synchronization** | Identifies differences in DDL syntax, DML operations, and data types between different databases. Its dynamic data structure adaptation capability can automatically identify structural differences between source and target databases, generating corresponding SQL for table structure creation and data replication. Rich data transformation and mapping functions handle data type and format conversions. |
| **LLM Data Analysis** | NLP → MDM → SQL: SQL generated by AI models is often in generic or standard form and may not execute directly on the target database. AnyLine provides a reliable execution engine for NLP natural-language-to-SQL scenarios. It serves as a middleware in the pipeline, using dynamic data source adaptation to identify the target database type and version for cross-database executability. It also encapsulates database connections, metadata collection, SQL generation, and dialect adaptation into a unified interface, allowing developers to focus on AI model training and tuning, lowering the barrier to NL2SQL adoption. As infrastructure for AI to understand databases, operate databases, and validate results, it enables AI to focus on semantic understanding and intent recognition — the two working together to deliver accurate, trustworthy query results from natural language. |
| **Rapid Project Delivery** | For project scenarios with unclear or frequently changing requirements and uncertain database types (especially Xinchuang domestic databases), AnyLine supports on-demand dynamic adjustment of data structures through its dynamic features, without redesigning tables or writing extensive conversion code. With its flexible data structure adaptation and transformation capabilities, it can quickly respond to requirement changes without large-scale architectural adjustments. |

<br>

## Technology Selection

<details>
<summary>Quick Decision Guide</summary>

- ✅ Need to operate multiple different types of databases simultaneously
- ✅ Table structures change dynamically at runtime, making it impossible to predefine entity classes
- ✅ Facing dynamic data scenarios such as low-code / data middle platforms / Xinchuang migration
- ✅ Need cross-database metadata collection, structure comparison, and DDL governance
- ✅ Query conditions are dynamically generated by the frontend or configuration rather than hardcoded

</details>

**In-Depth Comparisons**
- [AnyLine VS ORM: Core Differences and Selection Guide](http://doc.anyline.org/aa/f1_15730)
- [AnyLine MDM Selection Report](http://doc.anyline.org/aa/27_15731)
- [Comparison of Design Philosophy: AnyLine vs Traditional ORM](http://doc.anyline.org/aa/3b_15735)
- [In-Depth Analysis of AnyLine MDM User Groups](http://doc.anyline.org/aa/6a_15732)

<br>

## How to Integrate

Only **one dependency + one annotation** are needed to seamlessly integrate with Spring Boot, Netty, and other framework projects

📖 [Getting Started Series](http://doc.anyline.org/ss/f5_1150) · 💻 [Example Code](https://gitee.com/anyline/anyline-simple) · 🧪 [Test Cases 6,056+](http://doc.anyline.org/cs)

<details>
<summary>Data Source Registration and Switching</summary>

> Note: The data sources here are not master-slave relationships, but multiple completely independent data sources.

```java
DataSource ds_sso = new DruidDataSource();
ds_sso.setUrl("jdbc:mysql://localhost:3306/sso");
ds_sso.setDriverClassName("com.mysql.cj.jdbc.Driver");
...
DataSourceHolder.reg("ds_sso", ds_sso);
// or
DataSourceHolder.reg("ds_sso", pool, driver, url, user, password);
DataSourceHolder.reg("ds_sso", Map<String, Object> params); // key-value pairs for connection pool properties

// Query the SSO_USER table from the ds_sso data source
DataSet<DataRow> set = ServiceProxy.service("ds_sso").selects("SSO_USER");
```

Static configuration files (use Spring format in a Spring environment):

```properties
# Default data source
anyline.datasource.type=com.zaxxer.hikari.HikariDataSource
anyline.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.url=jdbc:mysql://localhost:33306/simple
anyline.datasource.user-name=root

# Other data sources
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
<summary>DML Dynamic Queries</summary>

```java
// In a web environment
service.queries("SSO_USER",
                condition(true, "NAME:%name%", "TYPE:[type]", "[CODES]:code"));
// true means pagination is needed; conditions without parameter values are ignored by default
// Generated SQL:
// SELECT * FROM SSO_USER WHERE 1=1
// AND NAME LIKE '%?%'
// AND TYPE IN(?,?,?)
// AND FIND_IN_SET(?, CODES)
// LIMIT 5,10  -- varies by database type

// User-defined query conditions (low-code and similar scenarios typically require more complex conditions)
ConfigStore configs;
service.select("SSO_USER", configs);
// ConfigStore provides all SQL operations
```

</details>

<details>
<summary>Read and Write Metadata</summary>

```java
@Autowired("anyline.service")
AnylineService service;

// Query the structure of the SSO_USER table in the default data source
Table table = service.metadata().table("SSO_USER");
LinkedHashMap<String, Column> columns = table.getColumns();             // columns in the table
LinkedHashMap<String, Constraint> constraints = table.getConstraints(); // constraints on the table
List<String> ddls = table.getDdls();                                    // table creation SQL

// Drop the table and recreate it
service.ddl().drop(table);
table = new Table("SSO_USER");

// Data types can be written casually here — no need to worry about int8 vs bigint;
// they will be converted to the correct type at execution time
table.addColumn("ID", "BIGINT").autoIncrement(true).setPrimary(true).setComment("Primary Key");
table.addColumn("CODE", "VARCHAR(20)").setComment("Code");
table.addColumn("NAME", "VARCHAR(20)").setComment("Name");
table.addColumn("AGE", "INT").setComment("Age");
service.ddl().create(table);

// Or use service.ddl().save(table); which distinguishes which columns need ADD and which need ALTER at execution time
```

</details>

<details>
<summary>Transaction Management</summary>

```java
// Since a method can switch data sources multiple times at any point,
// annotations can no longer capture the current data source
// More transaction parameters are available through TransactionDefine
TransactionState state = TransactionProxy.start("ds_sso");
// Perform data operations
TransactionProxy.commit(state);
TransactionProxy.rollback(state);
```

</details>

<details>
<summary>Compatibility with Traditional ORM</summary>

If you really can't let go of those existing XOOO patterns, DataSet and Entity can be converted between each other:

```java
EntitySet<User> = service.queries(User.class,
                                  condition(true, "query conditions auto-generated by AnyLine based on conventions"));
// true: indicates pagination is needed
// Why return EntitySet instead of List?
// Because in paginated scenarios, EntitySet contains pagination data, while List does not.
// Whether paginated or not, the same data structure is returned.

// Alternatively (if you really want to do this, don't use AnyLine — stick with MyBatis/Hibernate or similar)
public class UserService extends AnylineService<User>
userService.queries(condition(true, "query conditions auto-generated by AnyLine based on conventions"));
```

</details>

<br>

## Ecosystem and Community

| Resource | Link |
|:---------|:-----|
| Official Website | [www.anyline.org](http://anyline.org) |
| Documentation | [doc.anyline.org](http://doc.anyline.org) |
| Quick Start | [anyline.org/start](http://doc.anyline.org/ss/f5_1150) |
| Architecture Diagram | [deepwiki.com/anylineorg/anyline](https://deepwiki.com/anylineorg/anyline) |
| GitHub | [github.com/anylineorg/anyline](https://github.com/anylineorg/anyline) |
| Gitee | [gitee.com/anyline](https://gitee.com/anyline) |
| Maven Central | [mvnrepository.com](https://mvnrepository.com/artifact/org.anyline/anyline-core) |
| Example Projects | [anyline-simple](https://gitee.com/anyline/anyline-simple) |
| Test Cases | [6,056+](http://doc.anyline.org/cs) |

---

> 本内容由 Coze AI 生成，请遵循相关法律法规及《人工智能生成合成内容标识办法》使用与传播。
