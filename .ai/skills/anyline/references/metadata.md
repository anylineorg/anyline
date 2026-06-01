# 元数据读取操作

## 概述
本文档涵盖通过 Anyline MDM执行对表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象信息查询以及结构差异对比
查询对象信息通过service.metadata()或ServiceProxy.metadata()执行
如:service.metadata().tables()
注意有些方法返回Map<String,Metadata>有些方法返回List<Metadata>  
是因为贪婪模式下多个库中会有重名的对象，返回以对象表作为key的Map会被覆盖，所以返回List    
针对每个对象通常提供3组方法:    
1.根据名称查询单个对象  
2.根据条件查询多个对象返回List或Map  
3.根据名称检测对象是否存在  

## 1 数据库对象 
表:class Table<E extends Table> extends Metadata<E>   
视图:class View extends Table<View>  
图库表父类:class GraphTable extends Table<GraphTable>  
图库中的边:class EdgeTable extends GraphTable  
图库中的顶点:class VertexTable extends GraphTable  
虚拟表:class VirtualTable extends Table  
主表:class MasterTable extends Table<MasterTable>  
分区表:class PartitionTable extends Table<PartitionTable>  
Catalog:class Catalog extends Metadata<Catalog>  
Schema:class Schema extends Metadata<Schema>  
Database:class Database extends Metadata<Database>  
存储过程:class Procedure extends Metadata<Procedure>  
函数:class Function extends Metadata<Function>  
序列:class Sequence extends Metadata<Sequence>  

表附属对象:class TableAffiliation<E extends TableAffiliation> extends Metadata<E>  
Namespace:class Namespace extends Schema  
列:class Column extends TableAffiliation<Column>  
索引:class Index<M extends Index> extends TableAffiliation<M>  
主键:class PrimaryKey extends Index<PrimaryKey>  
外键:class ForeignKey extends Constraint<ForeignKey>  
触发器:class Trigger extends TableAffiliation<Trigger>

数据类型元类型:interface TypeMetadata  
数据类型元类型别名:interface TypeMetadataAlias   
标准数据类型:enum StandardTypeMetadata implements TypeMetadata  

角色:org.anyline.entity.authorize.Role
用户:org.anyline.entity.authorize.User
权限:org.anyline.entity.authorize.Privilege
角色组:org.anyline.entity.authorize.RoleGroup
用户组:org.anyline.entity.authorize.UserGroup
权限组:org.anyline.entity.authorize.PrivilegeGroup

多对多关系:org.anyline.metadata.persistence.ManyToMany
一对多关系:org.anyline.metadata.persistence.OneToMany

具体数据库对象：  
ElasticSearch的索引： class ElasticSearchIndex extends Table    
nebula的边:class EdgeType extends EdgeTable   
nebula的顶点:class Tag extends VertexTable    
Influx的表:class InfluxTable extends Table  
Influx的Measurement:class InfluxMeasurement extends InfluxTable  
Milvus的表:class MilvusCollection extends Table<MilvusCollection>

## 2 元数据读取
### 2.1 表结构读取
读取视图等表的子类结构与读取表结构类似
#### 2.1.1 读取多个表 完整参数方法
默认情况下仅读取表自身属性，不读取表、索引等附加对象属性，除非通过 struct 属性指定,struct也支持boolean类型,true表示读取表的所有属性以及附加对象属性
```java
/**
 * tables
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return tables
 */
<T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs);
<T extends Table> LinkedHashMap<String, T> tables(Table query, int types, int struct, ConfigStore configs);
default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    Table query = new Table(catalog, schema, name);
    return tables(greedy, query, types, struct, configs);
}
default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
    return tables(greedy, catalog, schema, name, types, struct, null);
}
default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return tables(greedy, catalog, schema, name, types, structs, configs);
}
default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return tables(greedy, catalog, schema, name, types, struct, null);
}
default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, int struct) {
    return tables(greedy, null, schema, name, types, struct);
}
default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types, boolean struct) {
    return tables(greedy, null, schema, name, types, struct);
}
default <T extends Table> List<T> tables(boolean greedy, String name, int types, boolean struct) {
    return tables(greedy, null, null, name, types, struct);
}
default <T extends Table> List<T> tables(boolean greedy, String name, int types, int struct) {
    return tables(greedy, null, null, name, types, struct);
}
default <T extends Table> List<T> tables(boolean greedy, int types, int struct) {
    return tables(greedy, null, types, struct);
}
default <T extends Table> List<T> tables(boolean greedy, int types, boolean struct) {
    return tables(greedy, null, types, struct);
}
default <T extends Table> List<T> tables(boolean greedy, boolean struct) {
    return tables(greedy, Table.TYPE.NORMAL.value, struct);
}

<T extends Table> LinkedHashMap<String, T> tables(Table query, int types, int struct, ConfigStore configs);

default <T extends Table> LinkedHashMap<String, T> tables(Table query) {
    return tables(query, Table.TYPE.NORMAL.value, 0, new DefaultConfigStore());
}
default <T extends Table> LinkedHashMap<String, T> tables(Table query, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return tables(query, Table.TYPE.NORMAL.value, structs, new DefaultConfigStore());
}
default <T extends Table> LinkedHashMap<String, T> tables(Table query, ConfigStore configs) {
    return tables(query, Table.TYPE.NORMAL.value, 0, configs);
}
default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    Table query = new Table(catalog, schema, name);
    return tables(query, types, struct, configs);
}
default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct) {
    return tables(catalog, schema, name, types, struct, null);
}
default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return tables(catalog, schema, name, types, structs, configs);
}
default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return tables(catalog, schema, name, types, struct, null);
}

default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, int struct) {
    return tables(null, schema, name, types, struct);
}
default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types, boolean struct) {
    return tables(null, schema, name, types, struct);
}
default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, int struct) {
    return tables(null, null, name, types, struct);
}
default <T extends Table> LinkedHashMap<String, T> tables(String name, int types, boolean struct) {
    return tables(null, null, name, types, struct);
}
default <T extends Table> LinkedHashMap<String, T> tables(String name) {
    return tables(null, null, name, Table.TYPE.NORMAL.value, false);
}
default <T extends Table> LinkedHashMap<String, T> tables(int types, int struct) {
    return tables(null, types, struct);
}
default <T extends Table> LinkedHashMap<String, T> tables(int types, boolean struct) {
    return tables(null, types, struct);
}
default <T extends Table> LinkedHashMap<String, T> tables() {
    return tables(Table.TYPE.NORMAL.value, false);
}
default <T extends Table> LinkedHashMap<String, T> tables(Schema schema) {
    return tables(new Catalog(), schema, null, Table.TYPE.NORMAL.value, false);
}

default <T extends Table> LinkedHashMap<String, T> tables(int types, int struct, ConfigStore configs) {
    return tables(null, null, null, types, struct, configs);
}
default <T extends Table> LinkedHashMap<String, T> tables(int types, boolean struct, ConfigStore configs) {
    return tables(null, null, null,  types, struct, configs);
}
default <T extends Table> LinkedHashMap<String, T> tables(ConfigStore configs) {
    return tables(Table.TYPE.NORMAL.value, false, configs);
}

default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
    return tables(greedy, catalog, schema, name, types, false, configs);
}
default <T extends Table> List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
    return tables(greedy, catalog, schema, name, types, false);
}
default <T extends Table> List<T> tables(boolean greedy, Schema schema, String name, int types) {
    return tables(greedy, null, schema, name, types, false);
}
default <T extends Table> List<T> tables(boolean greedy, String name, int types) {
    return tables(greedy, null, null, name, types, false);
}
default <T extends Table> List<T> tables(boolean greedy, String name) {
    return tables(greedy, null, null, name, Table.TYPE.NORMAL.value, false);
}
default <T extends Table> List<T> tables(boolean greedy, int types) {
    return tables(greedy, null, types, false);
}
default <T extends Table> List<T> tables(boolean greedy) {
    return tables(greedy, Table.TYPE.NORMAL.value, false);
}
default <T extends Table> List<T> tables(boolean greedy, int types, ConfigStore configs) {
    return tables(greedy, null, null, null, types, configs);
}
default <T extends Table> List<T> tables(boolean greedy, ConfigStore configs) {
    return tables(greedy, null, null, null, Table.TYPE.NORMAL.value, configs);
}

default <T extends Table> LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types) {
    return tables(catalog, schema, name, types, false);
}

default <T extends Table> LinkedHashMap<String, T> tables(Schema schema, String name, int types) {
    return tables(null, schema, name, types, false);
}
default <T extends Table> LinkedHashMap<String, T> tables(String name, int types) {
    return tables(null, null, name, types, false);
}
default <T extends Table> LinkedHashMap<String, T> tables(int types) {
    return tables(null, types, false);
}
default <T extends Table> LinkedHashMap<String, T> tables(int types, ConfigStore configs) {
    return tables(null,null,null, types, false, configs);
}

```
 
#### 2.1.2 读取单个表结构
默认读取表的所有属性以及附加对象属性
```java

Table table(boolean greedy, Table query, int struct);

default Table table(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
    Table query = new Table(catalog, schema, name);
    return table(greedy, query, struct);
}
default Table table(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return table(greedy, catalog, schema, name, structs);
}
default Table table(boolean greedy, Schema schema, String name, int struct) {
    return table(greedy, null, schema, name, struct);
}
default Table table(boolean greedy, Schema schema, String name, boolean struct) {
    return table(greedy, null, schema, name, struct);
}
default Table table(boolean greedy, String name, int struct) {
    return table(greedy, null, null, name, struct);
}
default Table table(boolean greedy, String name, boolean struct) {
    return table(greedy, null, null, name, struct);
}

Table table(Table query, int struct);
default Table table(Catalog catalog, Schema schema, String name, int struct) {
    Table query = new Table(catalog, schema, name);
    return table(query, struct);
}
default Table table(Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return table(catalog, schema, name, structs);
}
default Table table(Schema schema, String name, int struct) {
    return table(false, null, schema, name, struct);
}
default Table table(Schema schema, String name, boolean struct) {
    return table(false, null, schema, name, struct);
}
default Table table(String name, int struct) {
    return table(false, null, null, name, struct);
}
default Table table(String name, boolean struct) {
    return table(false, null, null, name, struct);
}
default Table table(boolean greedy, Catalog catalog, Schema schema, String name) {
    return table(greedy, catalog, schema, name, true);
}
default Table table(boolean greedy, Schema schema, String name) {
    return table(greedy, null, schema, name, true);
}
default Table table(boolean greedy, String name) {
    return table(greedy, null, null, name, true);
}

default Table table(Catalog catalog, Schema schema, String name) {
    return table( catalog, schema, name, true);
}
default Table table(Schema schema, String name) {
    return table(null, schema, name, true);
}
default Table table(String name) {
    return table(null, null, name, true);
}
```

#### 2.1.3 读取创建表的DDL
```java
/**
 * 表的创建SQL
 * @param table table
 * @param init 是否还原初始状态 默认false
 * @return ddl
 */
List<String> ddl(Table table, boolean init);
default List<String> ddl(String table, boolean init) {
    return ddl(new Table(table), init);
}
default List<String> ddl(Table table) {
    return ddl(table, false);
}
default List<String> ddl(String table) {
    return ddl(new Table(table));
}
```
#### 2.1.4 判断表是否存在
```java
/**
 * 表是否存在
 * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @return boolean
 */
boolean exists(boolean greedy, Table table);
default boolean exists(Table table) {
    return exists(false, table);
}
```
### 2.2 读取表上的列
```java
/**
 * 列是否存在
 * @param column 列
 * @return boolean
 */
boolean exists(boolean greedy, Table table, Column column);
default boolean exists(boolean greedy, Column column) {
    return exists(greedy, null, column);
}
default boolean exists(boolean greedy, String table, String column) {
    return exists(greedy, new Table(table), new Column(column));
}
default boolean exists(boolean greedy, Catalog catalog, Schema schema, String table, String column) {
    return exists(greedy, new Table(catalog, schema, table), new Column(column));
}
default boolean exists(Column column) {
    return exists(false, null, column);
}
default boolean exists(String table, String column) {
    return exists(false, new Table(table), new Column(column));
}
default boolean exists(Table table, String column) {
    return exists(false, table, new Column(column));
}
default boolean exists(Catalog catalog, Schema schema, String table, String column) {
    return exists(false, new Table(catalog, schema, table), new Column(column));
}
/**
 * 查询表中所有的表,注意这里的map.KEY全部转大写
 * @param table 表
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @return map
 */
<T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table, ConfigStore configs);
default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table) {
    return columns(greedy, table, null);
}
default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table, ConfigStore configs) {
    return columns(greedy, new Table(table), configs);
}
default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table) {
    return columns(greedy, new Table(table), null);
}
default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table, ConfigStore configs) {
    return columns(greedy, new Table(catalog, schema, table), configs);
}
default <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Catalog catalog, Schema schema, String table) {
    return columns(greedy, new Table(catalog, schema, table), new DefaultConfigStore());
}
default <T extends Column> LinkedHashMap<String, T> columns(Table table, ConfigStore configs) {
    return columns(false, table, configs);
}
default <T extends Column> LinkedHashMap<String, T> columns(Table table) {
    return columns(false, table, new DefaultConfigStore());
}
default <T extends Column> LinkedHashMap<String, T> columns(String table, ConfigStore configs) {
    return columns(false, new Table(table), configs);
}
default <T extends Column> LinkedHashMap<String, T> columns(String table) {
    return columns(false, new Table(table));
}
default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table, ConfigStore configs) {
    return columns(false, new Table(catalog, schema, table), configs);
}
default <T extends Column> LinkedHashMap<String, T> columns(Catalog catalog, Schema schema, String table) {
    return columns(false, new Table(catalog, schema, table), new DefaultConfigStore());
}

/**
 * 查询列
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @return List
 */
<T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema, ConfigStore configs);
default <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema) {
    return columns(greedy, catalog, schema, new DefaultConfigStore());
}
default <T extends Column> List<T> columns(Catalog catalog, Schema schema, ConfigStore configs) {
    return columns(false, catalog, schema, configs);
}
default <T extends Column> List<T> columns(Catalog catalog, Schema schema) {
    return columns(false, catalog, schema, new DefaultConfigStore());
}
default <T extends Column> List<T> columns(boolean greedy, ConfigStore configs) {
    return columns(greedy, (Catalog) null, (Schema) null, configs);
}
default <T extends Column> List<T> columns(boolean greedy) {
    return columns(greedy, (Catalog) null, (Schema) null, new DefaultConfigStore());
}
default <T extends Column> List<T> columns(ConfigStore configs) {
    return columns(false, (Catalog) null, (Schema)null, configs);
}
default <T extends Column> List<T> columns() {
    return columns(false, (Catalog) null, (Schema)null, new DefaultConfigStore());
}

/**
 * 查询table中的column列
 * @param table 表
 * @param name 列名(不区分大小写)
 * @return Column
 */
Column column(boolean greedy, Table table, String name);
default Column column(boolean greedy, String table, String name) {
    return column(greedy, new Table(table), name);
}
default Column column(boolean greedy, Catalog catalog, Schema schema, String table, String name) {
    return column(greedy, new Table(catalog, schema, table), name);
}
default Column column(Table table, String name) {
    return column(false, table, name);
}
default Column column(String table, String name) {
    return column(false, new Table(table), name);
}
default Column column(Catalog catalog, Schema schema, String table, String name) {
    return column(false, new Table(catalog, schema, table), name);
}

```
### 2.3 读取主表结构
```java
/**
 * masters
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param query 查询条件 根据metadata属性
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return masters
 */
<T extends MasterTable> List<T> masters(boolean greedy, MasterTable query , int types, int struct, ConfigStore configs);

/**
 * masters
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return masters
 */
default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    MasterTable query = new MasterTable(catalog, schema, name);
    return masters(greedy, query, types, struct, configs);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
    return masters(greedy, catalog, schema, name, types, struct, null);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return masters(greedy, catalog, schema, name, types, structs, configs);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return masters(greedy, catalog, schema, name, types, struct, null);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Schema schema, String name, int types, int struct) {
    return masters(greedy, null, schema, name, types, struct);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Schema schema, String name, int types, boolean struct) {
    return masters(greedy, null, schema, name, types, struct);
}
default <T extends MasterTable> List<T> masters(boolean greedy, String name, int types, boolean struct) {
    return masters(greedy, null, null, name, types, struct);
}
default <T extends MasterTable> List<T> masters(boolean greedy, String name, int types, int struct) {
    return masters(greedy, null, null, name, types, struct);
}
default <T extends MasterTable> List<T> masters(boolean greedy, int types, int struct) {
    return masters(greedy, null, types, struct);
}
default <T extends MasterTable> List<T> masters(boolean greedy, int types, boolean struct) {
    return masters(greedy, null, types, struct);
}
default <T extends MasterTable> List<T> masters(boolean greedy, boolean struct) {
    return masters(greedy, MasterTable.TYPE.NORMAL.value, struct);
}

<T extends MasterTable> LinkedHashMap<String, T> masters(MasterTable query, int types, int struct, ConfigStore configs);
default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    MasterTable query = new MasterTable(catalog, schema, name);
    return masters(query, types, struct, configs);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, int struct) {
    return masters(catalog, schema, name, types, struct, null);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return masters(catalog, schema, name, types, structs, configs);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return masters(catalog, schema, name, types, struct, null);
}

default <T extends MasterTable> LinkedHashMap<String, T> masters(Schema schema, String name, int types, int struct) {
    return masters(null, schema, name, types, struct);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(Schema schema, String name, int types, boolean struct) {
    return masters(null, schema, name, types, struct);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(String name, int types, int struct) {
    return masters(null, null, name, types, struct);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(String name, int types, boolean struct) {
    return masters(null, null, name, types, struct);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, int struct) {
    return masters(null, types, struct);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, boolean struct) {
    return masters(null, types, struct);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters() {
    return masters( MasterTable.TYPE.NORMAL.value, false);
}

default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, int struct, ConfigStore configs) {
    return masters(null, null, null, types, struct, configs);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, boolean struct, ConfigStore configs) {
    return masters(null, null, null,  types, struct, configs);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(ConfigStore configs) {
    return masters(MasterTable.TYPE.NORMAL.value, false, configs);
}

default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
    return masters(greedy, catalog, schema, name, types, false, configs);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
    return masters(greedy, catalog, schema, name, types, false);
}
default <T extends MasterTable> List<T> masters(boolean greedy, Schema schema, String name, int types) {
    return masters(greedy, null, schema, name, types, false);
}
default <T extends MasterTable> List<T> masters(boolean greedy, String name, int types) {
    return masters(greedy, null, null, name, types, false);
}
default <T extends MasterTable> List<T> masters(boolean greedy, int types) {
    return masters(greedy, null, types, false);
}
default <T extends MasterTable> List<T> masters(boolean greedy) {
    return masters(greedy, MasterTable.TYPE.NORMAL.value, false);
}
default <T extends MasterTable> List<T> masters(boolean greedy, int types, ConfigStore configs) {
    return masters(greedy, null, null, null, types, configs);
}
default <T extends MasterTable> List<T> masters(boolean greedy, ConfigStore configs) {
    return masters(greedy, null, null, null, MasterTable.TYPE.NORMAL.value, configs);
}

default <T extends MasterTable> LinkedHashMap<String, T> masters(Catalog catalog, Schema schema, String name, int types) {
    return masters(catalog, schema, name, types, false);
}

default <T extends MasterTable> LinkedHashMap<String, T> masters(Schema schema, String name, int types) {
    return masters(null, schema, name, types, false);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(String name, int types) {
    return masters(null, null, name, types, false);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(int types) {
    return masters(null, types, false);
}
default <T extends MasterTable> LinkedHashMap<String, T> masters(int types, ConfigStore configs) {
    return masters(null,null,null, types, false, configs);
}

/**
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param query 查询条件 根据metadata属性
 * @param struct 需要查询的表结构(参考Metadata.TYPE) true:表示查询全部 多个结构提供一个最终合计值
 * @return MasterTable
 */
MasterTable master(boolean greedy, MasterTable query, int struct);
/**
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param struct 需要查询的表结构(参考Metadata.TYPE) true:表示查询全部 多个结构提供一个最终合计值
 * @return MasterTable
 */
default MasterTable master(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
    MasterTable query = new MasterTable(catalog, schema, name);
    return master(greedy, query, struct);
}
default MasterTable master(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return master(greedy, catalog, schema, name, structs);
}
default MasterTable master(boolean greedy, Schema schema, String name, int struct) {
    return master(greedy, null, schema, name, struct);
}
default MasterTable master(boolean greedy, Schema schema, String name, boolean struct) {
    return master(greedy, null, schema, name, struct);
}
default MasterTable master(boolean greedy, String name, int struct) {
    return master(greedy, null, null, name, struct);
}
default MasterTable master(boolean greedy, String name, boolean struct) {
    return master(greedy, null, null, name, struct);
}

MasterTable master(MasterTable query, int struct);
default MasterTable master(Catalog catalog, Schema schema, String name, int struct) {
    MasterTable query = new MasterTable(catalog, schema, name);
    return master(query, struct);
}
default MasterTable master(Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return master(catalog, schema, name, structs);
}
default MasterTable master(Schema schema, String name, int struct) {
    return master(false, null, schema, name, struct);
}
default MasterTable master(Schema schema, String name, boolean struct) {
    return master(false, null, schema, name, struct);
}
default MasterTable master(String name, int struct) {
    return master(false, null, null, name, struct);
}
default MasterTable master(String name, boolean struct) {
    return master(false, null, null, name, struct);
}
default MasterTable master(boolean greedy, Catalog catalog, Schema schema, String name) {
    return master(greedy, catalog, schema, name, true);
}
default MasterTable master(boolean greedy, Schema schema, String name) {
    return master(greedy, null, schema, name, true);
}
default MasterTable master(boolean greedy, String name) {
    return master(greedy, null, null, name, true);
}

default MasterTable master(Catalog catalog, Schema schema, String name) {
    return master( catalog, schema, name, true);
}
default MasterTable master(Schema schema, String name) {
    return master(null, schema, name, true);
}
default MasterTable master(String name) {
    return master(null, null, name, true);
}

/**
 * 表的创建SQL
 * @param master master
 * @param init 是否还原初始状态 默认false
 * @return ddl
 */
List<String> ddl(MasterTable master, boolean init);
default List<String> ddl(MasterTable master) {
    return ddl(master, false);
}
```
### 2.4 分区表
```java
/**
 * 表分区方式及分片
 * @param table 主表
 * @return Partition
 */
Table.Partition partition(Table table);
/**
 * 子表
 * @param table 表
 * @return LinkedHashMap
 */
boolean exists(boolean greedy, PartitionTable table);
default boolean exists(PartitionTable table) {
    return exists(false, table);
}
/**
 * 根据主表与标签值查询分区表(子表)
 * @param query 查询条件 根据metadata属性
 * @return PartitionTables
 */
<T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, PartitionTable query);
/**
 * 根据主表与标签值查询分区表(子表)
 * @param master 主表
 * @param tags 标签值
 * @param name 子表名
 * @return PartitionTables
 */
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
    PartitionTable query = new PartitionTable();
    query.setMaster(master);
    if(null != tags) {
        for(String key:tags.keySet()) {
            Tag tag = null;
            Object value = tags.get(key);
            if(value instanceof Tag) {
                tag = (Tag)value;
            }else{
                tag = new Tag(key, value);
            }
            query.addTag(tag);
        }
    }
    query.setName(name);
    return partitions(greedy, query);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(MasterTable master, Map<String, Object> tags, String name) {
    return partitions(false, master, tags, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master, Map<String,Object> tags) {
    return partitions(greedy, master, tags, null);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(MasterTable master, Map<String,Object> tags) {
    return partitions(false, master, tags, null);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, Catalog catalog, Schema schema, String master, String name) {
    return partitions(greedy, new MasterTable(catalog, schema, master), null, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, Schema schema, String master, String name) {
    return partitions(greedy, new MasterTable(schema, master), null, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, String master, String name) {
    return partitions(greedy, new MasterTable(master), null, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, String master) {
    return partitions(greedy, new MasterTable(master), null, null);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master) {
    return partitions(greedy, master, null, null);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(boolean greedy, MasterTable master, String name) {
    return partitions(greedy, master, null, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(Catalog catalog, Schema schema, String master, String name) {
    return partitions(false, catalog, schema, master, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(Schema schema, String master, String name) {
    return partitions(false, schema, master, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(String master, String name) {
    return partitions(false, master, name);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(String master) {
    return partitions(false, master);
}
default <T extends PartitionTable> LinkedHashMap<String, T> partitions(MasterTable master) {
    return partitions(false, master);
}

PartitionTable partition(boolean greedy, PartitionTable query);
default PartitionTable partition(boolean greedy, MasterTable master, String name) {
    PartitionTable query = new PartitionTable(name);
    query.setMaster(master);
    return partition(greedy, query);
}
default PartitionTable partition(boolean greedy, Catalog catalog, Schema schema, String master, String name) {
    return partition(greedy, new MasterTable(catalog, schema, master), name);
}
default PartitionTable partition(boolean greedy, Schema schema, String master, String name) {
    return partition(greedy, new MasterTable(schema, master), name);
}
default PartitionTable partition(boolean greedy, String master, String name) {
    return partition(greedy, new MasterTable(master), name);
}
default PartitionTable partition(Catalog catalog, Schema schema, String master, String name) {
    return partition(false, catalog, schema, master, name);
}
default PartitionTable partition(Schema schema, String master, String name) {
    return partition(false, new MasterTable(schema, master), name);
}
default PartitionTable partition(String master, String name) {
    return partition(false, new MasterTable(master), name);
}

List<String> ddl(PartitionTable table);
```
### 2.5 顶点
```java
/**
 * 表是否存在
 * @param vertex 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @return boolean
 */
boolean exists(boolean greedy, VertexTable vertex);
default boolean exists(VertexTable vertex) {
    return exists(false, vertex);
}
/**
 * vertexs
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param query 查询条件 根据metadata属性
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return vertexs
 */
<T extends VertexTable> List<T> vertexs(boolean greedy, VertexTable query, int types, int struct, ConfigStore configs);
/**
 * vertexs
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return vertexs
 */
default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    VertexTable query = new VertexTable(catalog, schema, name);
    return vertexs(greedy, query, types, struct, configs);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
    return vertexs(greedy, catalog, schema, name, types, struct, null);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return vertexs(greedy, catalog, schema, name, types, structs, configs);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return vertexs(greedy, catalog, schema, name, types, struct, null);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Schema schema, String name, int types, int struct) {
    return vertexs(greedy, null, schema, name, types, struct);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Schema schema, String name, int types, boolean struct) {
    return vertexs(greedy, null, schema, name, types, struct);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, String name, int types, boolean struct) {
    return vertexs(greedy, null, null, name, types, struct);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, String name, int types, int struct) {
    return vertexs(greedy, null, null, name, types, struct);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, int types, int struct) {
    return vertexs(greedy, null, types, struct);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, int types, boolean struct) {
    return vertexs(greedy, null, types, struct);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, boolean struct) {
    return vertexs(greedy, VertexTable.TYPE.NORMAL.value, struct);
}

<T extends VertexTable> LinkedHashMap<String, T> vertexs(VertexTable query, int types, int struct, ConfigStore configs);
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    VertexTable query = new VertexTable(catalog, schema, name);
    return vertexs(query, types, struct, configs);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, int struct) {
    return vertexs(catalog, schema, name, types, struct, null);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return vertexs(catalog, schema, name, types, structs, configs);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return vertexs(catalog, schema, name, types, struct, null);
}

default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Schema schema, String name, int types, int struct) {
    return vertexs(null, schema, name, types, struct);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Schema schema, String name, int types, boolean struct) {
    return vertexs(null, schema, name, types, struct);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(String name, int types, int struct) {
    return vertexs(null, null, name, types, struct);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(String name, int types, boolean struct) {
    return vertexs(null, null, name, types, struct);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, int struct) {
    return vertexs(null, types, struct);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, boolean struct) {
    return vertexs(null, types, struct);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs() {
    return vertexs( VertexTable.TYPE.NORMAL.value, false);
}

default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, int struct, ConfigStore configs) {
    return vertexs(null, null, null, types, struct, configs);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, boolean struct, ConfigStore configs) {
    return vertexs(null, null, null,  types, struct, configs);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(ConfigStore configs) {
    return vertexs(VertexTable.TYPE.NORMAL.value, false, configs);
}

default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
    return vertexs(greedy, catalog, schema, name, types, false, configs);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
    return vertexs(greedy, catalog, schema, name, types, false);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, Schema schema, String name, int types) {
    return vertexs(greedy, null, schema, name, types, false);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, String name, int types) {
    return vertexs(greedy, null, null, name, types, false);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, int types) {
    return vertexs(greedy, null, types, false);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy) {
    return vertexs(greedy, VertexTable.TYPE.NORMAL.value, false);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, int types, ConfigStore configs) {
    return vertexs(greedy, null, null, null, types, configs);
}
default <T extends VertexTable> List<T> vertexs(boolean greedy, ConfigStore configs) {
    return vertexs(greedy, null, null, null, VertexTable.TYPE.NORMAL.value, configs);
}

default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Catalog catalog, Schema schema, String name, int types) {
    return vertexs(catalog, schema, name, types, false);
}

default <T extends VertexTable> LinkedHashMap<String, T> vertexs(Schema schema, String name, int types) {
    return vertexs(null, schema, name, types, false);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(String name, int types) {
    return vertexs(null, null, name, types, false);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types) {
    return vertexs(null, types, false);
}
default <T extends VertexTable> LinkedHashMap<String, T> vertexs(int types, ConfigStore configs) {
    return vertexs(null,null,null, types, false, configs);
}

/**
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param query 查询条件 根据metadata属性
 * @param struct 需要查询的表结构(参考Metadata.TYPE) true:表示查询全部 多个结构提供一个最终合计值
 * @return VertexTable
 */
VertexTable vertex(boolean greedy, VertexTable query, int struct);

/**
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param struct 需要查询的表结构(参考Metadata.TYPE) true:表示查询全部 多个结构提供一个最终合计值
 * @return VertexTable
 */
default VertexTable vertex(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
    VertexTable query = new VertexTable(catalog, schema, name);
    return vertex(greedy, query, struct);
}
default VertexTable vertex(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return vertex(greedy, catalog, schema, name, structs);
}
default VertexTable vertex(boolean greedy, Schema schema, String name, int struct) {
    return vertex(greedy, null, schema, name, struct);
}
default VertexTable vertex(boolean greedy, Schema schema, String name, boolean struct) {
    return vertex(greedy, null, schema, name, struct);
}
default VertexTable vertex(boolean greedy, String name, int struct) {
    return vertex(greedy, null, null, name, struct);
}
default VertexTable vertex(boolean greedy, String name, boolean struct) {
    return vertex(greedy, null, null, name, struct);
}

VertexTable vertex(VertexTable query, int struct);

default VertexTable vertex(Catalog catalog, Schema schema, String name, int struct) {
    VertexTable query = new VertexTable(catalog, schema, name);
    return vertex(query, struct);
}
default VertexTable vertex(Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return vertex(catalog, schema, name, structs);
}
default VertexTable vertex(Schema schema, String name, int struct) {
    return vertex(false, null, schema, name, struct);
}
default VertexTable vertex(Schema schema, String name, boolean struct) {
    return vertex(false, null, schema, name, struct);
}
default VertexTable vertex(String name, int struct) {
    return vertex(false, null, null, name, struct);
}
default VertexTable vertex(String name, boolean struct) {
    return vertex(false, null, null, name, struct);
}
default VertexTable vertex(boolean greedy, Catalog catalog, Schema schema, String name) {
    return vertex(greedy, catalog, schema, name, true);
}
default VertexTable vertex(boolean greedy, Schema schema, String name) {
    return vertex(greedy, null, schema, name, true);
}
default VertexTable vertex(boolean greedy, String name) {
    return vertex(greedy, null, null, name, true);
}

default VertexTable vertex(Catalog catalog, Schema schema, String name) {
    return vertex( catalog, schema, name, true);
}
default VertexTable vertex(Schema schema, String name) {
    return vertex(null, schema, name, true);
}
default VertexTable vertex(String name) {
    return vertex(null, null, name, true);
}

/**
 * 表的创建SQL
 * @param vertex vertex
 * @param init 是否还原初始状态 默认false
 * @return ddl
 */
List<String> ddl(VertexTable vertex, boolean init);
default List<String> ddl(VertexTable vertex) {
    return ddl(vertex, false);
}

```
### 2.6 边
```java
/**
 * 表是否存在
 * @param edge 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @return boolean
 */
boolean exists(boolean greedy, EdgeTable edge);
default boolean exists(EdgeTable edge) {
    return exists(false, edge);
}
/**
 * edges
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param query 查询条件 根据metadata属性
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return edges
 */
<T extends EdgeTable> List<T> edges(boolean greedy, EdgeTable query, int types, int struct, ConfigStore configs);
/**
 * edges
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和
 * @return edges
 */
default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    EdgeTable query = new EdgeTable(catalog, schema, name);
    return edges(greedy, query, types, struct, configs);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
    return edges(greedy, catalog, schema, name, types, struct, null);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return edges(greedy, catalog, schema, name, types, structs, configs);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return edges(greedy, catalog, schema, name, types, struct, null);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Schema schema, String name, int types, int struct) {
    return edges(greedy, null, schema, name, types, struct);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Schema schema, String name, int types, boolean struct) {
    return edges(greedy, null, schema, name, types, struct);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, String name, int types, boolean struct) {
    return edges(greedy, null, null, name, types, struct);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, String name, int types, int struct) {
    return edges(greedy, null, null, name, types, struct);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, int types, int struct) {
    return edges(greedy, null, types, struct);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, int types, boolean struct) {
    return edges(greedy, null, types, struct);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, boolean struct) {
    return edges(greedy, EdgeTable.TYPE.NORMAL.value, struct);
}

<T extends EdgeTable> LinkedHashMap<String, T> edges(EdgeTable query, int types, int struct, ConfigStore configs);
default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
    EdgeTable query = new EdgeTable();
    return edges(query, types, struct, configs);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, int struct) {
    return edges(catalog, schema, name, types, struct, null);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, boolean struct, ConfigStore configs) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return edges(catalog, schema, name, types, structs, configs);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types, boolean struct) {
    return edges(catalog, schema, name, types, struct, null);
}

default <T extends EdgeTable> LinkedHashMap<String, T> edges(Schema schema, String name, int types, int struct) {
    return edges(null, schema, name, types, struct);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(Schema schema, String name, int types, boolean struct) {
    return edges(null, schema, name, types, struct);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(String name, int types, int struct) {
    return edges(null, null, name, types, struct);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(String name, int types, boolean struct) {
    return edges(null, null, name, types, struct);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, int struct) {
    return edges(null, types, struct);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, boolean struct) {
    return edges(null, types, struct);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges() {
    return edges( EdgeTable.TYPE.NORMAL.value, false);
}

default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, int struct, ConfigStore configs) {
    return edges(null, null, null, types, struct, configs);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, boolean struct, ConfigStore configs) {
    return edges(null, null, null,  types, struct, configs);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(ConfigStore configs) {
    return edges(EdgeTable.TYPE.NORMAL.value, false, configs);
}

default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types, ConfigStore configs) {
    return edges(greedy, catalog, schema, name, types, false, configs);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
    return edges(greedy, catalog, schema, name, types, false);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, Schema schema, String name, int types) {
    return edges(greedy, null, schema, name, types, false);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, String name, int types) {
    return edges(greedy, null, null, name, types, false);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, int types) {
    return edges(greedy, null, types, false);
}
default <T extends EdgeTable> List<T> edges(boolean greedy) {
    return edges(greedy, EdgeTable.TYPE.NORMAL.value, false);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, int types, ConfigStore configs) {
    return edges(greedy, null, null, null, types, configs);
}
default <T extends EdgeTable> List<T> edges(boolean greedy, ConfigStore configs) {
    return edges(greedy, null, null, null, EdgeTable.TYPE.NORMAL.value, configs);
}

default <T extends EdgeTable> LinkedHashMap<String, T> edges(Catalog catalog, Schema schema, String name, int types) {
    return edges(catalog, schema, name, types, false);
}

default <T extends EdgeTable> LinkedHashMap<String, T> edges(Schema schema, String name, int types) {
    return edges(null, schema, name, types, false);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(String name, int types) {
    return edges(null, null, name, types, false);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types) {
    return edges(null, types, false);
}
default <T extends EdgeTable> LinkedHashMap<String, T> edges(int types, ConfigStore configs) {
    return edges(null,null,null, types, false, configs);
}

/**
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param query 查询条件 根据metadata属性
 * @param struct 需要查询的表结构(参考Metadata.TYPE) true:表示查询全部 多个结构提供一个最终合计值
 * @return EdgeTable
 */
EdgeTable edge(boolean greedy, EdgeTable query, int struct);

/**
 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
 * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
 * @param struct 需要查询的表结构(参考Metadata.TYPE) true:表示查询全部 多个结构提供一个最终合计值
 * @return EdgeTable
 */
default EdgeTable edge(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
    EdgeTable query = new EdgeTable(catalog, schema, name);
    return edge(greedy, query, struct);
}
default EdgeTable edge(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return edge(greedy, catalog, schema, name, structs);
}
default EdgeTable edge(boolean greedy, Schema schema, String name, int struct) {
    return edge(greedy, null, schema, name, struct);
}
default EdgeTable edge(boolean greedy, Schema schema, String name, boolean struct) {
    return edge(greedy, null, schema, name, struct);
}
default EdgeTable edge(boolean greedy, String name, int struct) {
    return edge(greedy, null, null, name, struct);
}
default EdgeTable edge(boolean greedy, String name, boolean struct) {
    return edge(greedy, null, null, name, struct);
}

EdgeTable edge(EdgeTable query, int struct);
default EdgeTable edge(Catalog catalog, Schema schema, String name, int struct) {
    EdgeTable query = new EdgeTable(catalog, schema, name);
    return edge(query, struct);
}
default EdgeTable edge(Catalog catalog, Schema schema, String name, boolean struct) {
    int structs = 0;
    if(struct) {
        structs = Metadata.TYPE.ALL.value();
    }
    return edge(catalog, schema, name, structs);
}
default EdgeTable edge(Schema schema, String name, int struct) {
    return edge(false, null, schema, name, struct);
}
default EdgeTable edge(Schema schema, String name, boolean struct) {
    return edge(false, null, schema, name, struct);
}
default EdgeTable edge(String name, int struct) {
    return edge(false, null, null, name, struct);
}
default EdgeTable edge(String name, boolean struct) {
    return edge(false, null, null, name, struct);
}
default EdgeTable edge(boolean greedy, Catalog catalog, Schema schema, String name) {
    return edge(greedy, catalog, schema, name, true);
}
default EdgeTable edge(boolean greedy, Schema schema, String name) {
    return edge(greedy, null, schema, name, true);
}
default EdgeTable edge(boolean greedy, String name) {
    return edge(greedy, null, null, name, true);
}

default EdgeTable edge(Catalog catalog, Schema schema, String name) {
    return edge( catalog, schema, name, true);
}
default EdgeTable edge(Schema schema, String name) {
    return edge(null, schema, name, true);
}
default EdgeTable edge(String name) {
    return edge(null, null, name, true);
}

/**
 * 表的创建SQL
 * @param edge edge
 * @param init 是否还原初始状态 默认false
 * @return ddl
 */
List<String> ddl(EdgeTable edge, boolean init);
default List<String> ddl(EdgeTable edge) {
    return ddl(edge, false);
}
```
### 2.7 外键
```java
<T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table);
default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, String table) {
    return foreigns(greedy, new Table(table));
}
default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Catalog catalog, Schema schema, String table) {
    return foreigns(greedy, new Table(catalog, schema, table));
}
default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(Table table) {
    return foreigns(false, table);
}
default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(String table) {
    return foreigns(false, new Table(table));
}
default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(Catalog catalog, Schema schema, String table) {
    return foreigns(false, new Table(catalog, schema, table));
}
ForeignKey foreign(boolean greedy, Table table, List<String> columns);
default ForeignKey foreign(boolean greedy, Table table, String ... columns) {
    return foreign(greedy, table, BeanUtil.array2list(columns));
}
default ForeignKey foreign(boolean greedy, String table, String ... columns) {
    return foreign(greedy, new Table(table), BeanUtil.array2list(columns));
}
default ForeignKey foreign(boolean greedy, String table, List<String> columns) {
    return foreign(greedy, new Table(table), columns);
}
//与上面的foreign(boolean greedy, String table, String ... columns)冲突
//ForeignKey foreign(boolean greedy, Catalog catalog, Schema schema, String table, String ... columns);
default ForeignKey foreign(boolean greedy, Catalog catalog, Schema schema, String table, List<String> columns) {
    return foreign(greedy, new Table(catalog, schema, table), columns);
}
default ForeignKey foreign(Table table, String ... columns) {
    return foreign(false, table, BeanUtil.array2list(columns));
}
default ForeignKey foreign(Table table, List<String> columns) {
    return foreign(false, table, columns);
}
default ForeignKey foreign(String table, String ... columns) {
    return foreign(false, new Table(table), BeanUtil.array2list(columns));
}
default ForeignKey foreign(String table, List<String> columns) {
    return foreign(false, new Table(table), columns);
}
//与上面的foreign(String table, String ... columns)冲突
//ForeignKey foreign(Catalog catalog, Schema schema, String table, String ... columns);
default ForeignKey foreign(Catalog catalog, Schema schema, String table, List<String> columns) {
    return foreign(false, new Table(catalog, schema, table), columns);
}
```
### 2.8 索引
```java
<T extends Index> List<T> indexes(boolean greedy, Table table);
default <T extends Index> List<T> indexes(boolean greedy, String table) {
    return indexes(greedy, new Table(table));
}
default <T extends Index> List<T> indexes(boolean greedy) {
    return indexes(greedy, (Table)null);
}
default <T extends Index> List<T> indexes(boolean greedy, Catalog catalog, Schema schema, String table) {
    return indexes(greedy, new Table(catalog, schema, table));
}

<T extends Index> LinkedHashMap<String, T> indexes(Table table);
default <T extends Index> LinkedHashMap<String, T> indexes(String table) {
    return indexes(new Table(table));
}
default <T extends Index> LinkedHashMap<String, T> indexes() {
    return indexes((Table)null);
}
default <T extends Index> LinkedHashMap<String, T> indexes(Catalog catalog, Schema schema, String table) {
    return indexes(new Table(catalog, schema, table));
}

Index index(boolean greedy, Table table, String name);
default Index index(boolean greedy, String table, String name) {
    return index(greedy, new Table(table), name);
}
default Index index(boolean greedy, String name) {
    return index(greedy, (Table) null, name);
}
default Index index(Table table, String name) {
    return index(false, table, name);
}
default Index index(String table, String name) {
    return index(false, new Table(table), name);
}
default Index index(String name) {
    return index(false, name);
}

```
### 2.9 约束
```java
<T extends Constraint> List<T> constraints(boolean greedy, Table table, String name);
default <T extends Constraint> List<T> constraints(boolean greedy, Table table) {
    return constraints(greedy, table, null);
}
default <T extends Constraint> List<T> constraints(boolean greedy, String table) {
    return constraints(greedy, new Table(table));
}
default <T extends Constraint> List<T> constraints(boolean greedy, Catalog catalog, Schema schema, String table) {
    return constraints(greedy, new Table(catalog, schema, table));
}

<T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name);
default <T extends Constraint> LinkedHashMap<String, T> constraints(Table table) {
    return constraints(table, null);
}
default <T extends Constraint> LinkedHashMap<String, T> constraints(String table) {
    return constraints(new Table(table));
}
default <T extends Constraint> LinkedHashMap<String, T> constraints(Catalog catalog, Schema schema, String table) {
    return constraints( new Table(catalog, schema, table));
}

<T extends Constraint> LinkedHashMap<String, T> constraints(Column column, String name);
default <T extends Constraint> LinkedHashMap<String, T> constraints(Column column) {
    return constraints(column, null);
}

Constraint constraint(boolean greedy, Table table, String name);
default Constraint constraint(boolean greedy, String table, String name) {
    return constraint(greedy, new Table(table), name);
}
default Constraint constraint(boolean greedy, String name) {
    return constraint(greedy, (Table)null, name);
}
default Constraint constraint(Table table, String name) {
    return constraint(false, table, name);
}
default Constraint constraint(String table, String name) {
    return constraint(false, table, name);
}
default Constraint constraint(String name) {
    return constraint(false, name);
}

```
### 2.10 触发器
```java
<T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events);
default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Catalog catalog, Schema schema, String table, List<Trigger.EVENT> events) {
    return triggers(greedy, new Table(catalog, schema, table), events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Schema schema, String table, List<Trigger.EVENT> events) {
    return triggers(greedy, new Table(schema, table), events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String table, List<Trigger.EVENT> events) {
    return triggers(greedy, new Table(table), events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, List<Trigger.EVENT> events) {
    return triggers(greedy, (Table) null, events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy) {
    return triggers(greedy,(Table) null, null);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(Catalog catalog, Schema schema, String table, List<Trigger.EVENT> events) {
    return triggers(false, new Table(catalog, schema, table), events);
}

default <T extends Trigger> LinkedHashMap<String, T> triggers(Schema schema, String table, List<Trigger.EVENT> events) {
    return triggers(false, schema, table, events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(String table, List<Trigger.EVENT> events) {
    return triggers(false, table, events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers(List<Trigger.EVENT> events) {
    return triggers(false, events);
}
default <T extends Trigger> LinkedHashMap<String, T> triggers() {
    return triggers(false);
}

Trigger trigger(boolean greedy, Catalog catalog, Schema schema, String name);
default Trigger trigger(boolean greedy, Schema schema, String name) {
    return trigger(greedy, null, schema, name);
}
default Trigger trigger(boolean greedy, String name) {
    return trigger(greedy, null, null, name);
}

default Trigger trigger(Catalog catalog, Schema schema, String name) {
    return trigger(false, catalog, schema, name);
}
default Trigger trigger(Schema schema, String name) {
    return trigger(false, null, schema, name);
}
default Trigger trigger(String name) {
    return trigger(false, name);
}
```
### 2.11 存储过程
参考以下接口声明
```java
<T extends Procedure> List<T> procedures(boolean greedy, Catalog catalog, Schema schema, String name);
default <T extends Procedure> List<T> procedures(boolean greedy, Schema schema, String name) {
    return procedures(greedy, null, schema, name);
}
default <T extends Procedure> List<T> procedures(boolean greedy, String name) {
    return procedures(greedy, null, null, name);
}
default <T extends Procedure> List<T> procedures(boolean greedy) {
    return procedures(greedy, null, null, null);
}

<T extends Procedure> LinkedHashMap<String, T> procedures(Catalog catalog, Schema schema, String name);
default <T extends Procedure> LinkedHashMap<String, T> procedures(Schema schema, String name) {
    return procedures(null, schema, name);
}
default <T extends Procedure> LinkedHashMap<String, T> procedures(String name) {
    return procedures(null, null, name);
}
default <T extends Procedure> LinkedHashMap<String, T> procedures() {
    return procedures(null, null, null);
}

Procedure procedure(boolean greedy, Catalog catalog, Schema schema, String name) throws Exception;
default Procedure procedure(boolean greedy, Schema schema, String name) throws Exception {
    return procedure(greedy, null, schema, name);
}
default Procedure procedure(boolean greedy, String name) throws Exception {
    return procedure(greedy, null, null, name);
}
default Procedure procedure(Catalog catalog, Schema schema, String name) throws Exception {
    return procedure(false, catalog, schema, name);
}
default Procedure procedure(Schema schema, String name) throws Exception {
    return procedure(false, schema, name);
}
default Procedure procedure(String name) throws Exception {
    return procedure(false, name);
}
/**
 * 存储过程的创建DDL
 * @param procedure 存储过程
 * @return ddl
 */
List<String> ddl(Procedure procedure);
```
### 2.12 函数
参考以下接口声明
```java
<T extends Function> List<T> functions(boolean greedy, Catalog catalog, Schema schema, String name);
default <T extends Function> List<T> functions(boolean greedy, Schema schema, String name) {
    return functions(greedy, null, schema, name);
}
default <T extends Function> List<T> functions(boolean greedy, String name) {
    return functions(greedy, null, null, name);
}
default <T extends Function> List<T> functions(boolean greedy) {
    return functions(greedy, null, null, null);
}
<T extends Function> LinkedHashMap<String, T> functions(Catalog catalog, Schema schema, String name);
default <T extends Function> LinkedHashMap<String, T> functions(Schema schema, String name) {
    return functions(null, schema, name);
}
default <T extends Function> LinkedHashMap<String, T> functions(String name) {
    return functions(null, name);
}
default <T extends Function> LinkedHashMap<String, T> functions() {
    return functions(null);
}

Function function(boolean greedy, Catalog catalog, Schema schema, String name);
default Function function(boolean greedy, Schema schema, String name) {
    return function(greedy, null, schema, name);
}
default Function function(boolean greedy, String name) {
    return function(greedy, null, null, name);
}
default Function function(Catalog catalog, Schema schema, String name) {
    return function(false, catalog, schema, name);
}
default Function function(Schema schema, String name) {
    return function(false, schema, name);
}
default Function function(String name) {
    return function(false, name);
}

/**
 * 函数的创建DDL
 * @param function 函数
 * @return ddl
 */
List<String> ddl(Function function);
```
### 2.13 序列
参考以下接口声明
```java
<T extends Sequence> List<T> sequences(boolean greedy, Catalog catalog, Schema schema, String name);
default <T extends Sequence> List<T> sequences(boolean greedy, Schema schema, String name) {
    return sequences(greedy, null, schema, name);
}
default <T extends Sequence> List<T> sequences(boolean greedy, String name) {
    return sequences(greedy, null, null, name);
}
default <T extends Sequence> List<T> sequences(boolean greedy) {
    return sequences(greedy, null, null, null);
}
<T extends Sequence> LinkedHashMap<String, T> sequences(Catalog catalog, Schema schema, String name);
default <T extends Sequence> LinkedHashMap<String, T> sequences(Schema schema, String name) {
    return sequences(null, schema, name);
}
default <T extends Sequence> LinkedHashMap<String, T> sequences(String name) {
    return sequences(null, name);
}
default <T extends Sequence> LinkedHashMap<String, T> sequences() {
    return sequences(null);
}

Sequence sequence(boolean greedy, Catalog catalog, Schema schema, String name);
default Sequence sequence(boolean greedy, Schema schema, String name) {
    return sequence(greedy, null, schema, name);
}
default Sequence sequence(boolean greedy, String name) {
    return sequence(greedy, null, null, name);
}
default Sequence sequence(Catalog catalog, Schema schema, String name) {
    return sequence(false, catalog, schema, name);
}
default Sequence sequence(Schema schema, String name) {
    return sequence(false, schema, name);
}
default Sequence sequence(String name) {
    return sequence(false, name);
}
List<String> ddl(Sequence sequence);
```
### 2.14 catalog
```java
Catalog catalog();
default Catalog catalog(String name) {
    List<Catalog> catalogs = catalogs(false, name);
    if(!catalogs.isEmpty()){
        return catalogs.get(0);
    }
    return null;
}
<T extends Catalog> LinkedHashMap<String, T> catalogs(String name);
default LinkedHashMap<String, Catalog> catalogs() {
    return catalogs(null);
}
<T extends Catalog> List<T> catalogs(boolean greedy, String name);
default List<Catalog> catalogs(boolean greedy) {
    return catalogs(greedy, null);
}
```
### 2.15 schema
```java
Schema schema();
default Schema schema(String name) {
    List<Schema> schemas = schemas(false, name);
    if(!schemas.isEmpty()){
        return schemas.get(0);
    }
    return null;
}
<T extends Schema> LinkedHashMap<String, T> schemas(Catalog catalog, String name);
default LinkedHashMap<String, Schema> schemas(Catalog catalog) {
    return schemas(catalog, null);
}
default LinkedHashMap<String, Schema> schemas() {
    return schemas(null, null);
}
default LinkedHashMap<String, Schema> schemas(String name) {
    return schemas(null, name);
}
<T extends Schema> List<T> schemas(boolean greedy, Catalog catalog, String name);
default List<Schema> schemas(boolean greedy) {
    return schemas(greedy, null, null);
}
default List<Schema> schemas(boolean greedy, Catalog catalog) {
    return schemas(greedy, catalog, null);
}
default List<Schema> schemas(boolean greedy, String name) {
    return schemas(greedy, null, name);
}
```
### 2.16 根据SQL读取元数据属性(主要是列的数据类型)  
注意这个方法是由 AnylineService 直接提供，而不是 AnylineService.metadata()提供
```java
/**
 * 根据结果集对象获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
 * @param sql sql
 * @param comment 是否需要列注释
 * @param condition 是否需要拼接查询条件, 如果需要会拼接where 1=0 条件(默认不添加，通常情况下SQL自带查询条件，给参数赋值NULL达到相同的效果)
 * @return LinkedHashMap
 */
LinkedHashMap<String, Column> metadata(String sql, boolean comment, boolean condition) throws Exception;
default LinkedHashMap<String, Column> metadata(String sql) throws Exception {
    return metadata(sql, false, false);
}
```

## 3 差异对比
对比数据库(表、列)之间的差异及成生DDL
### 3.1 注意事项:
#### 3.1.1 生成的SQL并不能真实还原数据库修改过程，最大的障碍在于不能捕获名称的修改过程以及先后，会导致:  
1) 实际操作的是alter,但生成的是drop+add  
2) 有依赖关系的操作如索引与名，自境与主键等，执行顺序不对会造成冲突  
#### 3.1.2 默认不比较catalog与schema  
### 3.2 对比结果
对比结果以Differ结构返回
#### 3.2.1 TablesDiffer
两个数据库表列表之间的差别，就是用一个A库所有的表与B库所有的表对比  
先分别查出A B两个库中的所有表  
LinkedHashMap<String, Table> as= serviceA.metadata().tables();  
LinkedHashMap<String, Table> bs= serviceB.metadata().tables();  
然后调用TablesDiffer静态方法  
public static TablesDiffer compare(LinkedHashMap<String, Table> as, LinkedHashMap<String, Table> bs)  
返回的结果中同B库相对于A库的表删除了哪几个、添加了哪几个、更新了哪几个  
#### 3.2.2 TableDiffer
两个表之间的差别  
表之间对比会有好几分部内容对应了几个属性，如  
1）ColumnsDiffer:两个表列之间的差别  
2）IndexsDiffer:两个表之间索引的差别  
先查出每个表的元数据，直接调用Table.compare对比  
Table a = service.metadata().table("a")  
Table b = service.metadata().table("b")  
TableDiffer differ = a.compare(b);  
或者  
TableDiffer differ = TableDiffer.compare(a, b);  

### 3.3 示例
```java
LinkedHashMap<String, Table> as = ServiceProxy.metadata().tables(1, true);
LinkedHashMap<String, Table> bs = ServiceProxy.service("pg").metadata().tables(1, true);
//对比过程 默认忽略catalog, schema
TablesDiffer differ = TablesDiffer.compare(as, bs);
 
System.out.println("===================================== DDL ================================================");
//设置生成的SQL在源库还是目标库上执行
differ.setDirect(MetadataDiffer.DIRECT.ORIGIN);
List<Run> runs = ServiceProxy.ddl(differ);
for(Run run:runs){
    System.out.println(run.getFinalExecute()+";\n");
}
```
以下在实际应用中不需要，只是为了说明详细过程
```java
LinkedHashMap<String, Table> adds =  differ.getAdds();
System.out.println("原表"+as);
System.out.println("表表"+bs);
//由a > b
System.out.println("++++++++++++++++++++++++++++++++++++++++++添加表++++++++++++++++++++++++++++++++++++++");
for(Table item:adds.values()){
    System.out.println(item);
}
LinkedHashMap<String, Table> alters = differ.getAlters();
System.out.println("///////////////////////////////////////////修改表/////////////////////////////////////");
for(Table item:alters.values()){
    System.out.println(item);
}
 
LinkedHashMap<String, TableDiffer> differs = differ.getDiffers();
for(TableDiffer dif:differs.values()){
    System.out.println("修改表:"+dif.getOrigin() +" > "+dif.getDest());
    ColumnsDiffer columnsDiffer = dif.getColumnsDiffer();
    for(Column column:columnsDiffer.getAdds().values()){
        System.out.println("+添加列:"+column);
    }
    for(Column column:columnsDiffer.getAlters().values()){
        System.out.println("/修改列:"+column+" > "+column.getUpdate());
    }
    for(Column column:columnsDiffer.getDrops().values()){
        System.out.println("-删除列:"+column);
    }
 
}
LinkedHashMap<String, Table> drops = differ.getDrops();
System.out.println("---------------------------------------------删除表----------------------------------------");
for(Table item:drops.values()){
    System.out.println(""+item);
}
```
生成的日志大概如下
```
原表{A=TABLE:simple.a, A2=TABLE:simple.a2, B=TABLE:simple.b, C=TABLE:simple.c}
表表{A=TABLE:simple.public.a, B=TABLE:simple.public.b, D=TABLE:simple.public.d}
++++++++++++++++++++++++++++++++++++++++++添加表++++++++++++++++++++++++++++++++++++++
TABLE:simple.public.d
///////////////////////////////////////////修改表/////////////////////////////////////
TABLE:simple.a
TABLE:simple.b
修改表:TABLE:simple.a > TABLE:simple.public.a
+添加列:id INT8 default nextval('bs_array_id_seq'::regclass)
+添加列:array_int INT8[]
+添加列:array_ints INT4[]
+添加列:array_char VARCHAR[]
/修改列:cc DECIMAL(10) > cc VARCHAR[]
-删除列:CODE DOUBLE(100,2)
-删除列:d DECIMAL(10)
修改表:TABLE:simple.b > TABLE:simple.public.b
+添加列:name VARCHAR(20)
/修改列:ID INT > id INT4
/修改列:CODE INT > code VARCHAR(20)
---------------------------------------------删除表----------------------------------------
TABLE:simple.a2
TABLE:simple.c
===================================== DDL ================================================
CREATE TABLE IF NOT EXISTS simple.public.d(
    id INT AUTO_INCREMENT COMMENT '主键'
    ,code VARCHAR(10) NULL COMMENT '编号'
    ,price NUMERIC(22,1) NULL
    ,salary NUMERIC NULL
    ,salary1 NUMERIC(10) NULL COMMENT '工资1'
    ,salary2 NUMERIC(10,2) NULL COMMENT '工资2'
    ,types VARCHAR(100) NULL);
 
ALTER TABLE simple.a
ADD COLUMN id BIGINT COMMENT '主键'
,ADD COLUMN array_int BIGINT[] NULL
,ADD COLUMN array_ints INT[] NULL
,ADD COLUMN array_char VARCHAR[] NULL
,MODIFY cc VARCHAR[] NULL
,DROP COLUMN CODE
,DROP COLUMN d;
 
ALTER TABLE simple.b
ADD COLUMN name VARCHAR(20) NOT NULL
,MODIFY ID INT NOT NULL
,MODIFY CODE VARCHAR(20) NOT NULL;
 
DROP TABLE IF EXISTS simple.a2;
 
DROP TABLE IF EXISTS simple.c;
```