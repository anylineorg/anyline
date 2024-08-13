Dynamically register and switch data sources during runtime, automatically generate SQL (DDL/DML/DQL), read and write metadata, and compare database structure differences. Adapt to over 100 relational/non relational databases. Commonly used for low-level support in dynamic scenarios, such as data middleware, visualization, low code backend, workflow, custom forms, heterogeneous database migration and synchronization, IoT vehicle data processing, data cleaning, runtime custom reports/query conditions/data structures, crawling insect data parsing, etc


## Introduction
The core of AnyLine is a runtime oriented metadata dynamic relationship mapping primarily used for  
- Dynamically register and switch data sources  
- Read and write metadata  
- Compare database structure differences  
- Generate dynamic SQL and combine dynamic query conditions  
- Complex result set operations  
Adapt to various relational and non relational databases (as well as various domestic niche databases)  
Commonly used for low-level support in dynamic structural scenarios, appearing as an SQL parsing engine or adapter  
Such as: data center, visualization, low code, SAAS, custom forms, heterogeneous database migration and synchronization, IoT vehicle networking data processing  
Conditions/data structures, crawler data parsing, etc.  
Reference [[Applicable Scenarios](http://doc.anyline.org/ss/ed_14)】  

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
DataSet set = ServiceProxy.service("ds_sso").querys("SSO_USER");
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
ConfigStore confis;
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