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
  <td>微信群</td>
  <td>过期请联系管理员
  </td>
  </tr>
  <tr>
    <td colspan="3">regardless of bugs, doubts, requirements, source code, competitors<br/>any questions, please contact skype: server@anyline.org  <br/>


</td>
</tr>
</table>

The core of AnyLine is a runtime-oriented dynamic metadata mapping system that is compatible with over 100 relational and non-relational databases.   
It is often used as the underlying support for dynamic structure scenarios, appearing as an SQL parsing engine or adapter.  
【[Document](http://doc.anyline.org)】
【[Quick Start](http://doc.anyline.org/ss/f5_1150)】
【[Applicable Scenarios](http://doc.anyline.org/ss/ed_14)】
【[Architecture Diagram](https://deepwiki.com/anylineorg/anyline)】


## Vision  
Relying on a dialect conversion engine and metadata mapping library synthesized by built-in rules and external plugins,  
we aim to establish a universal standard across databases on this foundation, enabling unified operations on heterogeneous databases.  


## Core Concept
AnyLine MDM focuses on &zwnj;**runtime metadata dynamic mapping**&zwnj;, primarily used for operating database structures, reading/writing metadata, and providing underlying support for dynamic scenarios. It often acts as an &zwnj;**SQL synthesis engine**&zwnj; or &zwnj;**adapter**&zwnj; in contexts such as:
- Data middle platforms
- Visualized data sources
- Low-code platforms
- SaaS applications
- Custom forms
- Heterogeneous database migration and synchronization
- IoT/vehicle network data processing
- Dynamic forms and query conditions
- Web crawler data parsing

---

## Core Features

### 1. &zwnj;**Dynamic Data Source Management**&zwnj;
- Supports runtime dynamic registration, switching, and deregistration of various data sources.
- Provides &zwnj;**7 data source registration methods**&zwnj; and &zwnj;**3 switching mechanisms**&zwnj;.

### 2. &zwnj;**Database Structure and Metadata Management**&zwnj;
- Supports dynamic management of database structures (e.g., automatic table creation, field extension).
- Standardizes metadata collection (data types, comments, constraint rules) for unified governance of data structures and metadata.
- Enables &zwnj;**table structure difference comparison**&zwnj;, &zwnj;**heterogeneous database structure replication**&zwnj;, and &zwnj;**data synchronization**&zwnj;.

### 3. &zwnj;**Dynamic DDL**&zwnj;
- Generates cross-database dynamic DDL by comparing metadata, analyzing table structure differences.
- Supports field type mapping, constraint conversion, indexing, etc., commonly used in database migration and version synchronization.

### 4. &zwnj;**Dynamic Query Conditions**&zwnj;
- Provides metadata-driven dynamic query solutions for flexible data filtering, sorting, and pagination.
- Supports multi-layer complex condition combinations and cross-database compatibility.
- Automatically generates query conditions in formats like JSON, String, or ConfigStore, ideal for low-code platforms to avoid cumbersome judgments, traversals, and format conversions while maintaining high performance and maintainability.

### 5. &zwnj;**Database Compatibility and Adaptation**&zwnj;
- Unifies database dialects for seamless metadata object compatibility across databases (relational, key-value, time-series, graph, document, columnar, vector, search, spatial, RDF, Event Store, Multivalue, Object).
- Specifically supports domestic databases.

### 6. &zwnj;**In-Memory Computation for Dynamic Data Structures (DataSet<DataRow>)**&zwnj;
- Based on a dynamic expression engine and SQL-like filtering, with built-in mathematical formulas, enables one-click aggregation, filtering, pivoting, and other operations on result sets, avoiding cumbersome ORM traversal.

### 7. &zwnj;**Multi-Data Source Transaction Management**&zwnj;
- Supports arbitrary data source switching while maintaining multiple transaction states and cross-thread transactions.

### 8. &zwnj;**Permission Management**&zwnj;
- Manages roles, users, and permissions.

---

## Why Use AnyLine Instead of Traditional ORM?

### 1. &zwnj;**Target Scenarios**&zwnj;
- &zwnj;**AnyLine**&zwnj;: Designed for highly dynamic runtime scenarios, natively supporting runtime uncertainties.
    - Handles dynamic data source access requests with heterogeneous structures and protocols.
    - Adapts to real-time changes in metadata (e.g., table structures, field definitions).
    - Provides dynamic model reconstruction and query adaptation through metadata management and adaptive mapping.

- &zwnj;**Traditional ORM**&zwnj;: Suited for static or relatively stable business scenarios.
    - Relies on predefined database structures and entity relationships during development.
    - Ensures system stability with upfront modeling and design.

### 2. &zwnj;**Product Positioning**&zwnj;
- &zwnj;**AnyLine**&zwnj;: Targets middleware development platforms for building low-code platforms, dynamic query engines, etc.
    - Empowers end-users to create customized business applications via visual configuration (e.g., dynamic reports, data analysis views).

- &zwnj;**Traditional ORM**&zwnj;: Used for developing end-user business systems (e.g., ERP, CRM, OA).
    - Maps database tables to object models for object-oriented database operations.

### 3. &zwnj;**Operation Targets**&zwnj;
- &zwnj;**AnyLine**&zwnj;: Metadata-driven, abstracting data structures and business logic.
    - Allows dynamic configuration of data models and business rules during early project stages.
    - Adapts to changing business requirements without a complete object model.

- &zwnj;**Traditional ORM**&zwnj;: Operates on entity classes directly mapped to database tables.
    - Maps tables to programming language classes, with fields and relationships reflected as class properties and associations.

### 4. &zwnj;**Target Users**&zwnj;
- &zwnj;**AnyLine**&zwnj;: For system architects and framework developers building highly flexible, extensible systems.
    - Provides runtime data structure and business rule definition capabilities.
    - Addresses system reconfiguration challenges due to requirement changes.

- &zwnj;**Traditional ORM**&zwnj;: For application developers building relational database-backed systems.
    - Simplifies database access and improves development efficiency.

### 5. &zwnj;**User Requirements**&zwnj;
- &zwnj;**AnyLine**&zwnj;: Requires deeper technical expertise, especially in metadata-driven development and dynamic system design.
    - Users must understand dynamic data model design and translate business requirements into configurable metadata rules.

- &zwnj;**Traditional ORM**&zwnj;: Easier to learn and use, lowering the database operation threshold for developers.

### 6. &zwnj;**Design Philosophy and Implementation**&zwnj;
| Aspect                | AnyLine                                                                 | Traditional ORM (e.g., Hibernate)                          |
|-----------------------|-------------------------------------------------------------------------|-----------------------------------------------------------|
| &zwnj;**Dynamic vs Static**&zwnj; | Runtime metadata-driven, supports dynamic data source registration.      | Relies on static entity class and database table pre-mapping. |
| &zwnj;**Metadata vs Object**&zwnj;| Operates on database structures (tables, views, columns) and metadata.    | Operates indirectly via object models (classes/properties). |
| &zwnj;**Multi-Database**&zwnj;    | Adapts dynamically via metadata engine and SQL dialect conversion.       | Requires hardcoded entity classes and dialect configuration. |

#### &zwnj;**DataSet/DataRow vs Traditional ORM Entity Class**&zwnj;
| Dimension               | AnyLine (DataSet/DataRow)                                  | Traditional ORM (Entity Class)                          |
|-------------------------|------------------------------------------------------------|---------------------------------------------------------|
| &zwnj;**Data Representation**&zwnj; | Dynamic structure (DataRow = row, DataSet<DataRow> = table).         | Static strongly-typed classes (e.g., `User.java`).      |
| &zwnj;**Flexibility**&zwnj;         | Adapts to table structure changes dynamically.              | Requires code changes for table structure modifications.|
| &zwnj;**Query Result Handling**&zwnj;| Directly operates on dynamic result sets.                  | Requires DTOs or projection interfaces.                 |
| &zwnj;**Low-Code Support**&zwnj;    | Suitable for dynamic forms and ad-hoc queries.              | Requires predefined entity classes.                     |
| &zwnj;**Performance Overhead**&zwnj;| Lightweight, no reflection/proxy generation.                | May incur overhead due to reflection/bytecode enhancement. |
| &zwnj;**Complex Mapping**&zwnj;     | Manual handling by default (e.g., multi-table JOIN results).| Automatically manages associations (e.g., `@OneToMany`).|
| &zwnj;**Use Cases**&zwnj;           | Dynamic business scenarios, heterogeneous databases.        | Fixed business models (e.g., ERP, CRM).                 |

---

## How AnyLine Works

### Key Components
1. &zwnj;**Parsing Layer**&zwnj;: Automatically converts standard SQL syntax into database-specific dialects.
2. &zwnj;**Metadata Abstraction Layer**&zwnj;: Builds a unified data view to shield structural differences.
3. &zwnj;**Multi-Protocol Adaptation Layer**&zwnj;: Supports mixed protocols (JDBC/ODBC/REST) for seamless heterogeneous data source access.

### Core Classes
- &zwnj;**DataSourceHolder**&zwnj;: Manages dynamic data sources.
- &zwnj;**DataRuntime**&zwnj;: Context environment associating data sources, adapters, connection pools, and services.
- &zwnj;**DriverAdapter**&zwnj;: Generates commands,屏蔽ing database command differences and data type compatibility.
- &zwnj;**DriverActuator**&zwnj;: Executes commands.
- &zwnj;**ServiceProxy**&zwnj;: Manages service-level data source switching.
- &zwnj;**DataSet/DataRow**&zwnj;: Encapsulates data, performs in-memory computation, and format conversion.

---

## What AnyLine Provides

### Features
- &zwnj;**Runtime Dynamic Data Source and Structure Support**&zwnj;: Dynamically register/switch data sources and generate SQL for complex queries based on metadata.
- &zwnj;**Simplified Database Operations**&zwnj;: Provides concise APIs for pagination, CRUD operations, and dynamic query conditions.
- &zwnj;**Flexible Result Set Processing**&zwnj;: Processes result sets as `Map` types with rich data processing functions (null handling, string manipulation, math calculations).
- &zwnj;**Multi-Data Source Support**&zwnj;: Manages multiple data sources via simple APIs, enabling focus on business logic.
- &zwnj;**Reduced Repetitive Work**&zwnj;: Supports user-defined query conditions via configuration dictionaries or low-code platforms.
- &zwnj;**Multi-Dialect Support**&zwnj;: Supports DML/DDL operations across databases (including domestic and niche databases), reducing syntax familiarity requirements.

### Value
- &zwnj;**Improved Development Efficiency**&zwnj;: Simplifies database operations and auto-generates SQL, reducing manual effort and errors.
- &zwnj;**Enhanced Flexibility**&zwnj;: Supports dynamic data sources, structures, and result set processing for diverse needs.
- &zwnj;**Lower Development Difficulty**&zwnj;: Provides simple APIs and rich functions, eliminating the need to master hundreds of database syntaxes.

### Advantages
- &zwnj;**Flexibility**&zwnj;: Adapts to runtime requirements with dynamic data sources and structures.
- &zwnj;**Efficiency**&zwnj;: Simplifies workflows and auto-generates SQL for faster development.
- &zwnj;**Ease of Use**&zwnj;: Offers concise APIs and rich functions, lowering the learning curve.

# AnyLine and Entity/ORM: Complementary Tools for Different Scenarios

Of course, we are not abandoning Entity or ORM. Different scenarios indeed require different solutions, and the design philosophy of AnyLine is precisely to provide flexibility and extensibility without excluding the use of traditional Entity or ORM.

## AnyLine Complements Rather Than Replaces Entity/ORM

- &zwnj;**Entity/ORM**&zwnj; has advantages such as strong typing, compile-time checking, and high code readability in predictable and fixed scenarios. It is suitable for scenarios with stable business logic and well-defined data structures.
- &zwnj;**AnyLine**&zwnj; focuses on dynamic and runtime scenarios, such as data middle platforms, multiple data sources, dynamic query conditions, and flexible result set processing, addressing the shortcomings of traditional ORM in these scenarios.

## Scenario Adaptation

- If the business logic is clear and the data structure is fixed (e.g., order systems, user management), using &zwnj;**Entity/ORM**&zwnj; is a more suitable choice.
- If the business logic is complex and the data sources change dynamically (e.g., data middle platforms, reporting systems, multi-tenant systems), the dynamic capabilities of &zwnj;**AnyLine**&zwnj; can significantly improve development efficiency.

## Entity Usage in AnyLine

- The source code of &zwnj;**AnyLine**&zwnj; indeed uses multiple &zwnj;**Entities**&zwnj; (such as geometric shapes), which shows that &zwnj;**AnyLine**&zwnj; itself does not exclude &zwnj;**Entities**&zwnj; but chooses the most appropriate tool based on the scenario.
- In &zwnj;**AnyLine**&zwnj;, &zwnj;**Entities**&zwnj; can serve as carriers of fixed data structures, combined with dynamic data sources and query conditions to achieve more flexible business logic.

## How to Choose the Appropriate Tool

### Scenario Differentiation

- Programmers need to have the ability to differentiate scenarios and choose appropriate technical solutions based on business requirements.
- For fixed scenarios, prioritize the use of &zwnj;**Entity/ORM**&zwnj;; for dynamic scenarios, prioritize the use of &zwnj;**AnyLine**&zwnj;.

### Combined Usage

- In actual projects, &zwnj;**Entity/ORM**&zwnj; and &zwnj;**AnyLine**&zwnj; can be used in combination to leverage their respective advantages.
- For example, use &zwnj;**Entity/ORM**&zwnj; to handle core business logic and &zwnj;**AnyLine**&zwnj; to handle dynamic queries, multiple data source switching, and other requirements.

### Avoid Over-Design

- Do not force-fit scenarios just to use a certain technology; choose the simplest solution based on actual requirements.
- The goal of &zwnj;**AnyLine**&zwnj; is to reduce development complexity, not increase it.


##### Data source registration and switching
Note that the data source here is not a master-slave relationship, but multiple completely unrelated data sources.
```java
DataSource ds_sso = new DruidDataSource();
ds_sso.setUrl("jdbc:mysql://localhost:3306/sso");
ds_sso.setDriverClassName("com.mysql.cj.jdbc.Driver");
...
DataSourceHolder.reg("ds_sso", ds_sso);
OR  
DataSourceHolder.reg("ds_sso", pool, driver, url, user, password);
DataSourceHolder.reg("ds_sso", Map<String, Object> params); //Corresponding properties of the connection pool k-v

//Query the SSO_USER table of the ds_Sso data source
DataSet<DataRow> set = ServiceProxy.service("ds_sso").querys("SSO_USER");
```
From static configuration file data source (if it is a Spring environment, it can be in Spring format)
```properties
#Default Data Source
anyline.datasource.type=com.zaxxer.hikari.HikariDataSource
anyline.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.url=jdbc:mysql://localhost:33306/simple
anyline.datasource.user-name=root
... more parameters
#Other data sources
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
// If it is a web environment
service.querys("SSO_USER", 
   condition(true, "NAME:%name%", "TYPE:[type]", "[CODES]:code"));
//true Indicates the need for pagination, and conditions without parameter values are ignored by default
//SQL:
SELECT * 
FROM SSO_USER 
WHERE 1=1 
AND NAME LIKE '%?%' 
AND TYPE IN(?,?,?)
AND FIND_IN_SET(?, CODES)	
LIMIT 5,10 //Generate different SQL statements based on specific database types

//User defined query conditions, low code scenarios generally require more complex query conditions
ConfigStore configs;
service.query("SSO_USER", configs);
//ConfigStore provides all SQL operations
//Sample code and instructions for SQL parameters for multi table, batch submission, custom SQL, and parsing XML definitions
```
Read and write metadata
```java
@Autowired("anyline.service")
AnylineService service;

//Query the SSO_USER table structure of the default data source
Table table = serivce.metadata().table("SSO_USER");
LinkedHashMap<String, Column> columns = table.getColumns();                 //columns of Table
LinkedHashMap<String, Constraint> constraints = table.getConstraints();     //constraints of table
List<String> ddls = table.getDdls();                                        //ddl for create table

//drop table and recreate
service.ddl().drop(table);
table = new Table("SSO_USER");

//The data type here is arbitrary, regardless of whether it is int8 or bigint, it will be converted to the correct type during execution
table.addColumn("ID", "BIGINT").autoIncrement(true).setPrimary(true).setComment("primary key");
table.addColumn("CODE", "VARCHAR(20)").setComment("code of user");
table.addColumn("NAME", "VARCHAR(20)").setComment("full name");
table.addColumn("AGE", "INT").setComment("age of user");
service.ddl().create(table);

//or service.ddl().save(table);  //During execution, it will distinguish which columns need to be added and which columns need to be altered
```
Database transactions
```java
//Because the method can switch data sources multiple times at any time, the annotation can no longer capture the current data source
//More transaction parameters can be obtained through the TransactionDefine parameter
TransactionState state = TransactionProxy.start("ds_sso"); 
//Operational data(insert update delete)
TransactionProxy.commit(state);
TransactionProxy.rollback(state);
```



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
