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
  <td>QQ Group (86020680)</td>
  <td>WeChat Group</td>
  <td>Contact admin if expired or full
  </td>
  </tr>
</table>



# AnyLine MDM

**Metadata Dynamic Mapping — Runtime-oriented Metadata Dynamic Mapping Library**

![License](https://img.shields.io/badge/License-Apache%202.0-blue)
![Maven Central](https://img.shields.io/badge/Maven%20Central-org.anyline:anyline--core-green)
![JDK](https://img.shields.io/badge/JDK-1.8%2B-orange)

A runtime-oriented metadata dynamic mapping library
Supports 100+ relational/non-relational databases (including various niche domestic databases)
Often used as an SQL parsing engine or adapter to solve unified operation and dynamic data source management problems in highly dynamic heterogeneous database scenarios

> In environments where traditional ORM falls short—in scenarios where table structures and data sources are unknown or constantly changing—  
> AnyLine enables dynamic registration and switching of multiple heterogeneous data sources at runtime, automatically detects and adapts to metadata structure differences across databases,
> and generates DDL/DML/DQL commands compatible with each database dialect,
> providing underlying support for data platforms, low-code platforms, SAAS systems, custom forms, and heterogeneous database migration/synchronization,
> shielding database differences and enabling "write once, run on multiple databases"

---

## Design Philosophy
Metadata-driven dynamic modeling for unknown domains
## Core Objective
Establish a cross-database universal standard and achieve unified operations on heterogeneous databases
## Implementation Mechanism
Synthesize dialect conversion engines and metadata mapping libraries through built-in rules and external plugins
## Design Principles
> Oriented toward dynamics, oriented toward metadata, based on runtime

### Simplicity

Rooted in minimalistic user intuition, achieving simple and natural calling flows through ultimate design, so that operations align with user intuition and can even be "guessed"—delivering "what you think is what you get."

More than intuition, rooted in intuition: not blindly following object-oriented programming conventions, but **redefining conventions** in metadata-driven dynamic scenarios.

### Dynamic

Breaking free from static property constraints to achieve high flexibility and extensibility.

- **Runtime**: That cat can only be determined at runtime, not at compile time

- **Dynamic**: That cat moves, it even changes—sometimes it becomes a dog

- **Metadata**: To navigate numerous uncertainties, use higher-dimensional metadata to transform uncertainty into certainty

### Pluralistic

Spring ≠ Java—learn from it, apply it, but don't be limited by it.

Building a pluralistic symbiotic technology ecosystem centered on metadata dynamic mapping—breaking framework boundaries, not only deeply adapting to Spring, but comprehensively covering various Java runtimes such as pure Java, Vert.x, Quarkus, and Solon.

---

## Core Features

### 1. Dynamic Data Sources

> Supports dynamic registration, switching, and deregistration of data sources (including third-party data sources) at runtime, enabling flexible full-lifecycle management of data sources; supports arbitrary switching of data sources within methods and intelligent transaction state maintenance, ensuring atomicity and consistency of multi-data source operations; seamlessly integrates with mainstream Java frameworks such as Spring, Vert.x, and Solon; naturally adapts to high-frequency dynamic scenarios like low-code platforms, multi-tenant architectures, and data platforms

- 7 registration methods
- 3 switching methods
- Arbitrary switching of data sources within methods with intelligent transaction state maintenance

---

### 2. Oriented Toward Database Structures and Metadata

> Metadata-driven design frees you from traditional ORM's dependency on static entity classes, enabling direct programming toward dynamic database structures and runtime metadata, achieving a transformation from "static binding" to "dynamic awareness". Uses dynamic data structures to carry business data, adapting to complex scenarios like dynamic columns and sparse data, thereby decoupling business logic from physical table structures.

- Metadata-driven design

- Dynamic structure self-adaptation

- Heterogeneous database integration and compatibility

---

### 3. Heterogeneous Database Integration and Compatibility

> Three-layer abstraction achieves unified operations on heterogeneous data sources. The bottom layer uses a plugin-based dialect engine to dynamically convert standard SQL syntax into target database native commands at runtime. The middle layer's runtime metadata parser reads database structure information without requiring pre-defined entity classes for data mapping. The top layer exposes standard DDL/DML methods through unified data operation interfaces, achieving complete decoupling of business logic from underlying data sources.

- Dynamic DDL/DML/DQL compatibility

- Built-in **200+** data type mapping matrix

- Built-in **1200+** system function and parameter mapping matrix

---

### 4. In-Memory Computing (DataSet/DataRow)

> Dynamic in-memory data containers, designed specifically for flexible handling of heterogeneous data at runtime. Does not depend on pre-defined entity classes; directly carries results from any database table or query, and supports dynamic operations and secondary calculations. Provides weakly-typed, dynamically-structured data representation with built-in SQL-like querying, aggregation, filtering, format conversion, and complex mathematical calculations, improving development efficiency and flexibility in dynamic data scenarios.

- Dynamic in-memory data containers without pre-defined entity class dependencies

- Weakly-typed, dynamically-structured data representation that dynamically adapts to structural changes

- Built-in expression engine supporting SQL-like queries, aggregation, filtering, and complex mathematical calculations

- Reduces database round-trips, avoiding frequent queries and network transfers

---

### 5. Dynamic Query Conditions

> Solves problems of low security, difficult maintenance, and poor cross-database compatibility in traditional development with manual SQL string concatenation. Based on metadata-driven approach, allows structured data formats (such as JSON, ConfigStore) to automatically construct secure and efficient database query logic at runtime. Supports JSON/ConfigStore/SQL mutual conversion, especially suitable for heterogeneous data source integration and complex condition filtering.

- Multi-dimensional analysis for data platforms

- Custom forms for low-code platforms

- Dynamic reports/BI

---

### 6. Maximum Database Compatibility

> Relying on the core dialect conversion engine and runtime dynamic metadata mapping library, achieves support for 100+ databases, covering mainstream commercial databases, various domestic Xinchuang (China's domestic IT substitution) databases, and various niche databases. Broadly speaking, databases also include third-party platforms (such as Dify, Coze, RAGflow, etc.). Relational, key-value, time-series, graph, document, column-family, vector, search, spatial, RDF, Event Store, MultiValue, Object, and other types of databases are progressively being integrated

- **100+** integrated
- **300+** in progress
- Where there's a database, there's AnyLine

---

## Core Application Scenarios

| Scenario | Description |
|---|---|
| **Low-Code Backend** | Dynamic data sources and metadata-driven design naturally adapt to the data layer requirements of low-code platforms. Generate table structures and query logic on demand at runtime, completing data access and operations with zero coding. Combined with dynamic form building and custom query functions, supports visual data adaptation and report output, thereby reducing technical barriers and improving platform flexibility, scalability, and development efficiency. |
| **Dynamic Report Generation** | Automatically identifies data structures based on runtime metadata; assembles query conditions and aggregation logic on demand to support dynamic configuration and instant output of report fields and data sources. Achieves cross-database, multi-dimensional, efficient report building through dynamic data source adaptation, metadata management, and intelligent SQL generation. Combined with AnyLine-Office, it can further parse dynamic templates and support automated output of complex formats. |
| **Data Platform** | Unifies operation interfaces for heterogeneous data sources, shields database syntax differences, and achieves unified cross-database querying, aggregation, and governance—providing a solid technical foundation for data platforms. Whether relational databases, NoSQL, or big data platforms, AnyLine provides consistent operational experience, allowing data engineers to focus on business logic rather than underlying adaptation. |
| **Visual Data Sources** | With dynamic data structure adaptation capabilities, flexibly handles dynamically changing business attributes, automatically adjusting data models to ensure frontend display accuracy and consistency. Provides rich data conversion and mapping functions, supporting multi-dimensional, multi-structure data reorganization required by the frontend. Can automatically generate DQL statements based on custom statistical methods and dimensions, efficiently retrieving and converting data to quickly generate dynamic charts. |
| **IoT / Connected Vehicles** | Native time-series database integration with support for dynamic field extension, easily handling massive heterogeneous data reported by devices. Leverages efficient in-memory computing capabilities to quickly process massive time-series data, with built-in rich conversion and mapping functions for data cleaning, aggregation, and conversion. From time-series database data reading to business system adaptation, it ensures real-time and accurate data processing throughout. |
| **Data Cleaning and Batch Processing** | With dynamic data structures and powerful in-memory computing capabilities, handles large volumes of heterogeneous and often non-standard data: DataSet/DataRow's built-in rich mathematical calculation functions can perform complex operations like aggregation, deduplication, and search in memory just like operating a database. Especially adept at handling various non-standard data or even data with structural errors, simplifying and accelerating the entire data processing workflow. |
| **Xinchuang Transformation Projects** | Provides data layer support for Xinchuang transformation projects from adaptation to migration: natively compatible with multiple domestic databases to achieve rapid business system migration; automatic dialect conversion unifies database SQL syntax to ensure system compatibility; during the dual-run period, compares query results and table structure differences to ensure migration accuracy and stability, reducing Xinchuang transformation costs and risks. |
| **Workflow** | AnyLine provides full-chain dynamic support for workflows with a dynamic metadata engine. Through dynamic ORM features, it achieves custom forms and dynamic query conditions without pre-defined entity classes and configuration files, improving workflow adaptability and extensibility when facing changing requirements. |
| **Data Lineage Analysis** | This solution delivers three-layer progressive metadata support for data lineage systems: column-level fine-grained extraction ensures accurate lineage mapping and node identification, heterogeneous type standardization enables cross-source matching free from dialect interference, and automated version fingerprint comparison identifies incremental structural changes to avoid full reconstruction and keep lineage maps synchronized, guaranteeing accuracy, consistency and timeliness respectively. |
| **Heterogeneous Database Migration and Synchronization** | Identifies differences in DDL syntax, DML operations, and data types between different databases; its dynamic data structure adaptation capabilities can automatically identify structural differences between source and target databases, generating corresponding SQL for table structure creation and data replication; rich data conversion and mapping functions handle data type and format conversion. |
| **LLM Data Analysis** | NLP 2 MDM 2 SQL — SQL generated by AI models is often generic or standard-form, which may not execute directly on the target database. AnyLine provides a reliable execution engine for NLP natural language to SQL scenarios. It serves as an intermediate layer in the pipeline, providing infrastructure for understanding databases, operating databases, and validating results. |
| **Rapid Project Delivery** | For project scenarios with unclear or frequently changing requirements and uncertain database types (especially domestic Xinchuang), AnyLine supports on-demand dynamic adjustment of data structures through dynamic features, without redesigning tables or writing extensive conversion code. With flexible data structure adaptation and conversion capabilities, it can quickly respond to requirement changes without large-scale architectural adjustments. |

---


## Target Users

【[AnyLine MDM User Group Deep Analysis](http://doc.anyline.org/aa/6a_15732)】

---


## Technology Selection

- 【[AnyLine VS ORM: Core Differences and Selection Guide](http://doc.anyline.org/aa/f1_15730)】

- 【[AnyLine MDM Selection Report](http://doc.anyline.org/aa/27_15731)】

### Quick Decision Guide

- Need to operate multiple different types of databases simultaneously

- Table structures dynamically change at runtime, cannot pre-define entity classes

- Oriented toward dynamic data scenarios such as low-code/data platform/Xinchuang migration

- Need cross-database metadata collection, structure comparison, and DDL governance

- Query conditions are dynamically generated by frontend or configuration, not hardcoded

---

## Ecosystem and Community

| Resource | Link |
|---|---|
| Official Website | [www.anyline.org](http://anyline.org) |
| Documentation | [doc.anyline.org](http://doc.anyline.org) |
| Quick Start Guide | [anyline.org/start](http://doc.anyline.org/ss/f5_1150) |
| Architecture Diagram | [anyline.org/architecture](hhttps://deepwiki.com/anylineorg/anyline) |
| GitHub | [github.com/anyline](https://github.com/anylineorg/anyline) |
| Gitee | [gitee.com/anyline](https://gitee.com/anyline) |
| Maven Central | [search.maven.org - anyline](https://mvnrepository.com/artifact/org.anyline/anyline-core) |
| Sample Projects | [anyline/anyline-simple](https://gitee.com/anyline/anyline-simple) |
| Test Cases | [6,056](http://doc.anyline.org/cs) |



## Traditional ORM Exists, Why Still Need AnyLine
- 【[AnyLine vs Traditional ORM: Design Philosophy Comparison](http://doc.anyline.org/aa/3b_15735)】
- 【[AnyLine VS ORM: Core Differences and Selection Guide](http://doc.anyline.org/aa/f1_15730)】


## How to Integrate

Only one dependency and one annotation are needed for seamless integration with Spring Boot, Netty, and other framework projects. Refer to 【[Getting Started Series](http://doc.anyline.org/ss/f5_1150)】  
For common examples, refer to 【[Sample Code](https://gitee.com/anyline/anyline-simple)】 or 【[Test Cases](http://doc.anyline.org/cs)】



##### Data Source Registration and Switching
Note that data sources here are not in a primary-secondary relationship, but multiple completely unrelated data sources.
```java
DataSource ds_sso = new DruidDataSource();
ds_sso.setUrl("jdbc:mysql://localhost:3306/sso");
ds_sso.setDriverClassName("com.mysql.cj.jdbc.Driver");
...
DataSourceHolder.reg("ds_sso", ds_sso);
// or
DataSourceHolder.reg("ds_sso", pool, driver, url, user, password);
DataSourceHolder.reg("ds_sso", Map<String, Object> params); // connection pool property key-value pairs

// Query SSO_USER table from ds_sso data source
DataSet<DataRow> set = ServiceProxy.service("ds_sso").selects("SSO_USER");
```
Data source from static configuration file (in Spring environment, use Spring format)
```properties
# default data source
anyline.datasource.type=com.zaxxer.hikari.HikariDataSource
anyline.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.url=jdbc:mysql://localhost:33306/simple
anyline.datasource.user-name=root
... more parameters
# other data sources
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
// In web environment, you can do:
service.queries("SSO_USER", 
   condition(true, "NAME:%name%", "TYPE:[type]", "[CODES]:code"));
// true means pagination is needed, conditions without parameters are ignored by default
// Generated SQL:
SELECT * 
FROM SSO_USER 
WHERE 1=1 
AND NAME LIKE '%?%' 
AND TYPE IN(?,?,?)
AND FIND_IN_SET(?, CODES)	
LIMIT 5,10 // depends on database type

// User-defined query conditions, low-code scenarios usually require more complex query conditions
ConfigStore configs;
service.select("SSO_USER", configs);
ConfigStore provides all SQL operations
// Examples and documentation for multi-table, batch submission, custom SQL, and parsing XML-defined SQL parameters
```

Reading and Writing Metadata
```java
@Autowired("anyline.service")
AnylineService service;

// Query SSO_USER table structure from default data source
Table table = service.metadata().table("SSO_USER");
LinkedHashMap<String, Column> columns = table.getColumns();                 // columns in the table
LinkedHashMap<String, Constraint> constraints = table.getConstraints();     // constraints on the table
List<String> ddls = table.getDdls();                                        // table creation SQL

// Drop table and recreate
service.ddl().drop(table);
table = new Table("SSO_USER");

// Data types here can be arbitrary, no need to worry about int8 or bigint, will be converted to correct type at execution
table.addColumn("ID", "BIGINT").autoIncrement(true).setPrimary(true).setComment("primary key");
table.addColumn("CODE", "VARCHAR(20)").setComment("code");
table.addColumn("NAME", "VARCHAR(20)").setComment("name");
table.addColumn("AGE", "INT").setComment("age");
service.ddl().create(table);

// or service.ddl().save(table); which distinguishes at execution time which columns need add and which need alter
```
Transactions

```java
// Since methods can switch data sources anytime, annotations cannot capture the current data source anymore
// More transaction parameters through TransactionDefine
TransactionState state = TransactionProxy.start("ds_sso"); 
// Perform operations
TransactionProxy.commit(state);
TransactionProxy.rollback(state);
```



## Compatibility
If existing various XOOOs cannot fit into the implementation  
DataSet and Entity can convert to each other  
Or like this:
```
EntitySet<User> = service.queries(User.class, 
    condition(true, "AnyLine automatically generates query conditions based on conventions")); 
// true: pagination is needed
// Why does it return EntitySet instead of List?
// Because in pagination scenarios, EntitySet contains pagination data, but List cannot.
// Returns the same data structure regardless of pagination, so you don't need to implement two interfaces with different return types based on whether pagination is used

// You can also do this (but if you really want to do this, don't use AnyLine, use MyBatis, Hibernate, etc.)
public class UserService extends AnylinseService<User> 
userService.queries(condition(true, "AnyLine automatically generates query conditions based on conventions")); 
```
