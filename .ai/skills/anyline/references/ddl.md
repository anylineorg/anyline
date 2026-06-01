# DDL相关操作

## 概述
本文档涵盖通过 Anyline MDM执行对表、列、视图、索引、约束、主外键、存储过程、函数、触发器等数据库对象执行创建、修改、删除操作  
## 1 数据库对象
参考 metadata.md中的 数据库对象 章节
## 2 对象操作
### 2.1 表及附加对象
#### 2.1.1 创建表
```java
String name = "crm_user";
Table table = service.metadata().table(name);
//如果存在则删除
if(null != table){
    service.ddl().drop(tab);
}
table = new Table(name).setCharset("utf8mb4").setCollate("utf8mb4_general_ci");
// 根据不同数据库长度精度有可能忽略
table.addColumn("CODE", "varchar(20)").setComment("编号");
table.addColumn("ID", "bigint", 12, 11).primary(true).setComment("主键");
table.addColumn("DEFAULT_NAME", "varchar(50)").setComment("名称").setDefaultValue("A");
table.addColumn("NAME", "varchar(50)").setComment("名称");
table.addColumn("O_NAME", "varchar(50)").setComment("原列表");
table.addColumn("SALARY", "decimal(10,2)").setComment("精度").setNullable(false);
table.addColumn("SALARY_12", "decimal(10,2)").setComment("精度").setNullable(false);
table.addColumn("DEL_COL", "varchar(50)").setComment("删除");
table.addColumn("CREATE_BY", "bigint").setComment("创建人");
table.addColumn("CREATE_TIME", "datetime")
        .setComment("创建时间")
        .setDefaultValue(JDBCAdapter.SQL_BUILD_IN_VALUE.CURRENT_DATETIME);
table.addColumn("UPDATE_BY", "bigint").setComment("更新人");
table.addColumn("UPDATE_TIME", "datetime").setComment("更新时间");
service.ddl().create(tab);
```
#### 2.1.2 修改表
修改表通常是先读取表结构，在读取结果的基础上修改列、索引等属性。  
也可以在new Table()在基础上修改属性
修改完成后调用 alter(Table)或save(Table)方法  方法内部会再次读取数据库中原表结构与传入的Table对比，根据对比结构生成SQL
```java
/*
 * 1.修改表名
 * 修改名称比较特殊，因为需要同时保留新旧名称，否则就不知道要修改哪个表或列了
 * 同时要注意改名不会检测新名称是否存在 所以改名前要确保 新名称 没有被占用
 */
table.update().setName(updateName).setComment(updateComment);
/*
 * 2.修改属性，注释，非空 如果不存在则创建
 */
Column col = table.getColumn("NAME");
if(null == col){
    col = new Column("NAME");
}
col.setType("int").setDefaultValue("123").setComment("新类型").setNullable(false);

Column def = table.getColumn("DEFAULT_NAME");
def.setDefaultValue("ABC");
/*
 * 3.删除列
 */
col = table.getColumn("DEL_COL");
col.drop();
/*
 * 4.修改列名
 */
col = table.getColumn("O_NAME");
col.update().setName("N_NAME").setComment("新列名");
/*
 * 5.修改精度
 */
col = table.getColumn("SALARY_12");
col.setPrecision(18);
col.setScale(9);
col.setNullable(true);
/*
 * 6.换主键
 */
table.addColumn("PK_CODE", "INT");
table.setPrimaryKey("PK_CODE");
service.ddl().save(table);
/*
 * 7.删除主键之外的索引
 */
LinkedHashMap<String,Index> indexes = table.getIndexes();
for(Index index:indexes.values()){
    if(!index.isPrimary()){
        service.ddl().drop(index);
    }
}
/*
 * 8.添加新索引
 */
Index index = new Index();
index.addColumn("ID");
index.addColumn("SALARY");
index.setName("IDX_ID_SALARY");
index.setUnique(true);
index.setTable(table);
service.ddl().add(index);
```

##### 2.1.2.1 修改列 
```java
table = new Table();
table.setName("c_test");
table.setComment("表备注");
table.addColumn("ID", "int").primary(true).autoIncrement(true).setComment("主键说明");

table.addColumn("NAME","varchar(50)").setComment("名称");
table.addColumn("A_CHAR","varchar(50)");
table.addColumn("DEL_COL","varchar(50)");
LinkedHashMap<String, Column> columns = table.getColumns();
columns.put("NEW_CHAR", new Column().setName("NEW_CHAR").setType("int"));
service.ddl().save(table);

table = service.metadata().table("c_test");

//添加列
String tmp = "NEW_"+BasicUtil.getRandomNumberString(3);
table.addColumn(tmp, "int");
service.ddl().save(table);
//删除列
Column dcol = table.getColumn(tmp);
dcol.delete();
service.ddl().save(table);

//修改列属性
Column column = table.getColumn("NAME");
column.setTypeName("int");	//没有数据的情况下修改数据类型
column.setPrecision(0);
column.setScale(0);
column.setDefaultValue("1");
column.setNullable(false);
boolean result = service.save(column);

column = new Column();
column.setTable("c_test");
column.setName("A_CHAR");
column.setTypeName("int");	//没有数据的情况下修改数据类型
column.setPrecision(0);
column.setScale(0);
column.setDefaultValue("1");
column.setNullable(false);

service.ddl().save(column);
column.setTable("c_test");
column.setName("A_CHAR");
//修改列
//没有值的属性 默认同步原有的数据库结构
//如果不修改列名，直接修改column属性
column.setTypeName("varchar(10)");
column.setComment("测试备注1"+ DateUtil.format());
column.setDefaultValue("2");
log.warn("修改列");
service.ddl().save(column);

//修改列名2种方式
//注意:修改列名时，不要直接设置name属性,修改数据类型时，不要直接设置typeName属性,因为需要原属性
// 1.可以设置newName属性(注意setNewName返回的是update)
column.setNewName("B_TEST").setTypeName("varchar(20)");
log.warn("修改列名");
service.ddl().save(column);



// 2.可以在update基础上修改
//如果设置了update, 后续所有更新应该在update上执行
column.update().setName("C_TEST").setPosition(0).setTypeName("VARCHAR(20)");
log.warn("修改列名");
service.ddl().save(column);

column = new Column();
column.setName("c_test").setNewName("d_test");
column.setTypeName("varchar(1)");
column.setTable("c_test");
service.ddl().save(column);
/*
column = new Column("id");
column.setTable("t");
column.setCatalog("c");
column.setSchema("s");

log.warn("删除列");
service.ddl().drop(column);*/

Table tab = new Table("c_test");
tab.addColumn("ID", "int");
service.ddl().save(tab);
```
##### 2.1.2.2 修改列排序
```java
table = new Table(name);
table.addColumn("C2","INT").setPosition(2);
table.addColumn("C1","INT").setPosition(1);
table.addColumn("C3", "int").setPosition(3);
table.addColumn("C", "int"); //没有设置排最后
// C1 C2 C3 C
service.ddl().create(table);

table = new Table(name);
//改成C1 C3 C C2
//排序主要取决于位置最小的那一列，其他列一次排序
//position只用来排序已知的列， 只有未设置after,before时0才表示首位
table.addColumn("C3", "int").setPosition(0).setAfter("C1");
table.addColumn("C", "int").setPosition(1); //没有设置排最后
table.sort();
service.ddl().save(table);
table = service.metadata().table(name);
LinkedHashMap<String, Column> columns = table.getColumns();
for(Column c:columns.values()){
    System.out.println(c.getName());
}
```
##### 2.1.2.3 修改数据类型
```java
table = new Table(name);
table.addColumn("ID", "int");
table.addColumn("CODE", "int8");
ServiceProxy.ddl().create(table);
table = ServiceProxy.metadata().table(name);
table.getColumn("CODE").setType("varchar(50)");
ServiceProxy.ddl().save(table);
```
##### 2.1.2.4 删除列
默认情况下如果传入的table对象中的列比数据库中的列少，不会直接执行删除，除非在列上调用了delete()或drop()方法  
主要是为了在new Table()基础上修改属性时，table对象中只包含了需要修改的几个列时，避免删除其他不需要修改的列
```java
Table table = service.metadata().table(name);
Map<String, Column> columns = table.getColumns();
columns.get("NAME").delete(); //删除列
columns.get("O_NAME").delete();
service.ddl().alter(table);
```
##### 2.1.2.5 修改主键
```java
table = new Table(name);
table.addColumn("ID", "INT").setAutoIncrement(true).setPrimary(true, "BTREE");
table.addColumn("CODE", "int");
service.ddl().create(table);
table = service.metadata().table(name);

PrimaryKey pk = new PrimaryKey();
pk.setName("pk_code");
pk.addColumn("CODE");
pk.setType("BTREE");
table.setPrimaryKey(pk);
service.ddl().save(table);
```

##### 2.1.2.6 自增列
```java
table = new Table(name);
table.addColumn("ID", "INT").setPrimary(true);
table.addColumn("CODE", "int").setAutoIncrement(true).setUnique(true);
service.ddl().alter(table);
```
##### 2.1.2.7  创建,删除索引
```java
Table tab = service.metadata().table("tab_index");
if(null != tab){
    service.ddl().drop(tab);
}
tab = new Table("tab_index");
tab.setComment("表结构修改测试");
tab.addColumn("columnA", "varchar(10)");
tab.addColumn("columnB", "varchar(10)").setComment("备注B");
Index index = new Index();
index.setName("tab_index_index");
index.addColumn(tab.getColumn("columnA"));
index.addColumn(tab.getColumn("columnB"));
index.setUnique(true);
tab.add(index);
service.ddl().create(tab);

Table nTab = service.metadata().table("tab_index");
Collection<Index> values = nTab.getIndexes().values();
for (Index idx : values) {
    System.out.println(idx.getName() + ":" + idx.isUnique());
    idx.delete();
}
service.ddl().save(tab);

//操作Index添加索引
Index idx = tab.getIndex("IDX_ID_CODE_NAME");
if(null == idx){
    idx = new Index("IDX_ID_CODE_NAME");
    idx.addColumn("ID", "ASC");
    idx.addColumn("CODE", "DESC");
    idx.addColumn("NAME", "DESC");
    idx.setTable(tab);
    service.ddl().add(idx);
}
```
##### 2.1.2.8  表及列的编码格式
```java
 table = new Table("table_char");
table.setComment("表备注");
table.addColumn("ID", "BIGINT").setAutoIncrement(true).setPrimary(true).setComment("主键");
table.addColumn("CODE","varchar(10)").setCharset("utf8mb4").setCollate("utf8mb4_0900_ai_ci").setComment("编号");
table.addColumn("NAME", "varchar(10,2)");
table.setCharset("utf8mb4");
table.setCollate("utf8mb4_0900_ai_ci");
service.ddl().save(table);
```
##### 2.1.2.9  外键
```java
 //创建组合主键
ta = new Table("TAB_A");
ta.addColumn("ID", "int").setNullable(false).primary(true);
ta.addColumn("CODE", "varchar(10)").setNullable(false).primary(true);
ta.addColumn("NAME", "varchar(10)");
service.ddl().create(ta);


tb = new Table("TAB_B");
tb.addColumn("ID", "int").primary(true).autoIncrement(true);
tb.addColumn("AID", "int");
tb.addColumn("ACODE", "varchar(10)");
service.ddl().create(tb);
//创建组合外键
ForeignKey foreign = new ForeignKey("fkb_id_code");
foreign.setTable("TAB_B");
foreign.setReference("TAB_A");
foreign.addColumn("AID","ID");
foreign.addColumn("ACODE","CODE");
service.ddl().add(foreign);

//查询组合外键
LinkedHashMap<String, ForeignKey> foreigns = service.metadata().foreigns("TAB_B");
for(ForeignKey item:foreigns.values()){
    System.out.println("外键:"+item.getName());
    System.out.println("表:"+item.getTableName(true));
    System.out.println("依赖表:"+item.getReference().getName());
    LinkedHashMap<String, Column> columns = item.getColumns();
    for(Column column:columns.values()){
        System.out.println("列:"+column.getName()+"("+column.getReference()+")");
    }
}
//根据列查询外键
foreign = service.metadata().foreign("TAB_B", "AID","ACODE");
if(null != foreign) {
    System.out.println("外键:" + foreign.getName());
    System.out.println("表:" + foreign.getTableName(true));
    System.out.println("依赖表:" + foreign.getReference().getName());
    LinkedHashMap<String, Column> columns = foreign.getColumns();
    for (Column column : columns.values()) {
        System.out.println("列:" + column.getName() + "(" + column.getReference() + ")");
    }
}
```
##### 2.1.2.10 触发器
```java
Table tb = service.metadata().table("TAB_USER", false);
if(null != tb){
    service.ddl().drop(tb);
}
tb = new Table("TAB_USER");
tb.addColumn("ID","INT").autoIncrement(true).primary(true);
tb.addColumn("CODE", "varchar(10)");
service.ddl().create(tb);

Trigger trigger = new Trigger();
trigger.setName("TR_USER");
trigger.setTime(org.anyline.metadata.Trigger.TIME.AFTER);
trigger.addEvent(org.anyline.metadata.Trigger.EVENT.INSERT);
trigger.setTable("TAB_USER");
trigger.setDefinition("UPDATE aa SET code = 1 WHERE id = NEW.id;");
service.ddl().create(trigger);

trigger = service.metadata().trigger("TR_USER");
if(null != trigger){
    System.out.println("TRIGGER TABLE:"+trigger.getTableName(true));
    System.out.println("TRIGGER NAME:"+trigger.getName());
    System.out.println("TRIGGER TIME:"+trigger.getTime());
    System.out.println("TRIGGER EVENT:"+trigger.getEvents());
    System.out.println("TRIGGER define:"+trigger.getDefinition());
    service.ddl().drop(trigger);
}
```

### 2.2 分区表
```java
/**
 * 分区
 * PARTITION BY RANGE(ID)(
 * 	PARTITION s1 VALUES LESS THAN (100)
 * 	, PARTITION s2 VALUES LESS THAN (200)
 * 	, PARTITION s3 VALUES LESS THAN (300)
 * )
 * @throws Exception 异常
 */
@Test
public void partition_less() throws Exception{
    Table table = head();
    Table.Partition partition = new Table.Partition();
    partition.addColumn("ID");//.addColumn("QTY");
    partition.setType(Table.Partition.TYPE.RANGE);
    partition.addSlice(new Table.Partition.Slice("s1").setLess("ID",100));//.setLess("QTY",100));
    partition.addSlice(new Table.Partition.Slice("s2").setLess("ID",200));//.setLess("QTY",200));
    partition.addSlice(new Table.Partition.Slice("s3").setLess("ID",300));//.setLess("QTY",1000));
    table.setPartition(partition);
    service.ddl().create(table);
    table = service.metadata().table(name);
    partition = table.getPartition();
    Table.Partition.TYPE type = partition.getType();
    System.out.println(type);
}


/**
 * 分区
 * PARTITION BY LIST(ID)
 * ( PARTITION s1 VALUES IN(1,2)
 * , PARTITION s2 VALUES IN(11,12)
 * )
 * @throws Exception 异常
 */
@Test
public void partition_list() throws Exception{
    Table table = head();
    Table.Partition partition = new Table.Partition();
    partition.addColumn("ID");
    partition.setType(Table.Partition.TYPE.LIST);
    partition.addSlice(new Table.Partition.Slice("s1").addValues(0).addValues(99));
    partition.addSlice(new Table.Partition.Slice("s2").addValues(100).addValues(999));
    partition.addSlice(new Table.Partition.Slice("s3").addValues(1000).addValues(9999));
    table.setPartition(partition);
    service.ddl().create(table);
    table = service.metadata().table(name);
    partition = table.getPartition();
    Table.Partition.TYPE type = partition.getType();
    System.out.println(type);
}
/**
 * PARTITION BY HASH(ID) PARTITIONS 100
 * @throws Exception 异常
 */
@Test
public void partition_hash() throws Exception{
    //
    Table table = head();
    Table.Partition partition = new Table.Partition();
    partition.addColumn("ID");
    partition.setType(Table.Partition.TYPE.HASH).setModulus(100);
    table.setPartition(partition);
    service.ddl().create(table);
    table = service.metadata().table(name);
    partition = table.getPartition();
    Table.Partition.TYPE type = partition.getType();
    System.out.println(type);
}

public Table head() throws Exception{

    ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION = true; //遇到SQL异常直接抛出
    //检测表结构
    Table table = service.metadata().table(catalog, schema, name);
    //如果存在则删除
    if(null != table){
        service.ddl().drop(table);
    }
    //也可以直接删除(需要数据库支持 IF EXISTS)
    service.ddl().drop(new Table(catalog, schema, name));

    //再查询一次
    table = service.metadata().table(catalog, schema, name);
    Assertions.assertNull(table);

    //定义表结构
    table = new Table(name);
    table.setComment("表备注");
    //设置分桶方式 DISTRIBUTED BY HASH('ID'） BUCKETS 2
    table.setDistribution(Table.Distribution.TYPE.HASH, 3, "ID");

    table.addColumn("ID", "INT", false, null).setComment("主键");//.autoIncrement(true).primary(true);
    table.addColumn("QTY", "INT").setComment("数量");
    table.addColumn("CODE", "VARCHAR(10)").setComment("编号");
    table.addColumn("CODE2", "double(10)").setComment("编号");
    table.addColumn("NAME", "VARCHAR(20)").setComment("名称");
    table.addColumn("REG_TIME", "date").setComment("注册时间");
    table.addColumn("REG_TIME1", "datetime").setComment("注册时间");
    table.addColumn("REG_TIME2", "timestamp(6)").setComment("注册时间");
    table.addColumn("DATA_VERSION", "double").setComment("数据版本");
    return table;
}
```
### 2.3 视图
### 2.3.1 创建视图
```java
View view = new View("V_CRM_USER");
view.setDefinition("SELECT * FROM CRM_USER WHERE ID > 0");
service.ddl().create(view);
```
### 2.3.2 读取视图定义
```java
View view = service.metadata().view("v_hr_department");
System.out.println(view.getDefinition());
```
### 2.3.3 物化视图
```java
View view1 = new View("v1");
view1.addColumn(new Column("ID"));
view1.addColumn(new Column("NAME"));
view1.addColumn(new Column("CODE"));
View view2 = new View("v2");
view2.addColumn(new Column("ID"));
view2.addColumn(new Column("DATA_VERSION"));
view2.addColumn(new Column("NAME"));
view2.addColumn(new Column("CODE"));
table.addMaterializes(view1).addMaterializes(view2);
service.ddl().create(table);
```