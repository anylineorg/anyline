# DML操作

## 概述
本文档涵盖通过 Anyline 进行的DML操作。所有操作通过注入的 `AnylineService` 实例或 `ServiceProxy` 调用。


空值(null以及空字符串)默认不参与插入与更新 
除非显式指定或修改ConfigTable(全局有效)或ConfigStore(当前方法有效)或DataRow(当前对象有效)相关配置:
```java
//ConfigTable配置项:
IS_UPDATE_NULL_COLUMN	= false	; // DataRow是否更新nul值的列(针对DataRow)
IS_UPDATE_EMPTY_COLUMN	= false	; // DataRow是否更新空值的列
IS_INSERT_NULL_COLUMN	= false	; // DataRow是否插入nul值的列
IS_INSERT_EMPTY_COLUMN	= false	; // DataRow是否插入空值的列
IS_UPDATE_NULL_FIELD	= false	; // Entity是否更新nul值的属性(针对Entity)
IS_UPDATE_EMPTY_FIELD	= false	; // Entity是否更新空值的属性
IS_INSERT_NULL_FIELD	= false	; // Entity是否更新nul值的属性
IS_INSERT_EMPTY_FIELD	= false	; // Entity是否更新空值的属性
```
## 1 插入数据
### 1.1 单条插入
```java
// 方式一：DataRow 承载数据
DataRow row = new DataRow();
row.put("name","张三");
row.put("age",28);
service.insert("hr_user",row);

//如果表中有自增主键，执行插入后主键值会被存入DataRow
Long id = row.getLong("id");
// 方式二：实体对象承载数据,user上通常会有表名注解，如果不指定表名则使注解上的表名，如果没有注解会根据类名以驼峰转下划线的规则生成表名
User user = new User();
user.setName("张三");
user.setAge(28);
service.insert(user);
//如果指定表名则以指定的表名为准
service.insert("hr_user",user);
//如果表中有自增主键，执行插入后主键值会被存入user
Long id = user.getId();

//以下方式DataRow与Entity类似
//方式三：插入时指定列名，当DataRow或Entity中有不需要插入的列名时可以在调用方法时指定列表
service.insert("hr_user", row, "NAME","AGE","TYPE_ID");
//也可以把列名放在一个集合中
List<String> columns = new ArrayList();
columns.add("NAME");
columns.add("AGE");
columns.add("TYPE_ID");
service.insert("hr_user", row, columns);
//也可以通过ConfigStore指定插入或更新的列名
ConfigStore configs = new DefaultConfigStore();
configs.columns("NAME","AGE","TYPE_ID");
service.insert("hr_user", row, configs);
```
### 1.2 多条插入
与单条插入类似，只是把DataRow换成DataSet，把Entity换成Collection
```java
service.insert("hr_user", set);
service.insert("hr_user", users);
```

### 1.3 批量执行
插入数量比较大时可以批量执行，在多条插入方法基础上加一个int参数表示一次执行多少行
```java
service.insert(200, "hr_user", set);

//参考完整的插入方法
/**
 * 插入数据
 * @param batch 批量执行每批最多数量,大于1时批量执行
 * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
 * @param data entity或list或DataRow或DataSet
 * @param configs 插入过程中的配置项，如是否插入空值<br/>
 *              IS_UPDATE_NULL_COLUMN:DataRow是否更新nul值的列(针对DataRow)<br/>
 * 				IS_UPDATE_EMPTY_COLUMN:DataRow是否更新空值的列<br/>
 * 				IS_INSERT_NULL_COLUMN:DataRow是否插入nul值的列<br/>
 * 				IS_INSERT_EMPTY_COLUMN:DataRow是否插入空值的列<br/>
 * 				IS_UPDATE_NULL_FIELD:Entity是否更新nul值的属性(针对Entity)<br/>
 * 				IS_UPDATE_EMPTY_FIELD:Entity是否更新空值的属性<br/>
 * 				IS_INSERT_NULL_FIELD:Entity是否更新nul值的属性<br/>
 * 				IS_INSERT_EMPTY_FIELD:Entity是否更新空值的属性<br/>
 * 				IS_CHECK_ALL_INSERT_COLUMN:插入集合时是否检测所有条目的列(默认只检测第一行)<br/>
 * 				IS_CHECK_ALL_UPDATE_COLUMN:更新集合时是否检测所有条目的列(默认只检测第一行)
 * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
 *                列可以加前缀<br/>
 *                +:表示必须插入<br/>
 *                -:表示必须不插入<br/>
 *                ?:根据是否有值<br/>
 *
 *        如果没有提供columns,长度为0也算没有提供<br/>
 *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
 *
 *        如果提供了columns则根据columns获取insert列<br/>
 *
 *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
 *
 *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
 *        则把执行结果与表结构对比,删除表中没有的列<br/>
 * @return 影响行数
 */
long insert(int batch, String dest, Object data, ConfigStore configs, List<String> columns);
```

## 2 更新数据
默认根据主键值更新,主键默认ID  
如果主键不是ID，可以通过以下方式修改
全局生效:ConfigTable.DEFAULT_PRIMARY_KEY = "主键列名";  
当前方法生效:new DefaultConfigStore().DEFAULT_PRIMARY_KEY("主键列名");  
针对当前DataRow生效:也可以单独设置DataRow对象的主键row.setPrimaryKey("主键列名");
### 2.1 按默认主键更新
```java
DataRow row = new DataRow();
row.put("ID", 1);
row.put("CODE", "A01");
service.update("hr_user", row);
//生成对应的SQL:
UPDATE hr_user SET CODE = ? WHERE ID = ?
```
### 2.2 按指定列更新
实际主键是ID，但更新时按CODE更新  
可以设置临时(模拟主键)row.setPrimaryKey("CODE")  
仅对UPDATE条件产生影响，不影响表结构  
```java
DataRow row = new DataRow();
row.put("TYPE_ID", "1");
row.put("CODE", "A01");
row.setPrimaryKey("CODE")
service.update("HR_USER", row);

//生成对应的SQL:
UPDATE HR_USER SET TYPE_ID = ? WHERE CODE = ?
```
### 2.3 更新列与条件列重叠
如把CODE=1的行更新成CODE=2   
造成CODE既在更新列中又在更新条件中  
需要通过ConfigStore构造更新条件  
```java
DataRow row = new DataRow();
row.put("TYPE_ID", "1");
row.put("CODE", "A01");
ConfigStore configs = new DefaultConfigStore();
configs.and("CODE", "A02");
service.update("HR_USER", row, configs);

//生成对应SQL:
UPDATE HR_USER SET CODE = ?, TYPE_ID = ? WHERE CODE = ?
param0=1
param1=A01
param2=A02
```
### 2.4 更新值中含${表达式或函数或常量}值
```java
DataRow row = new DataRow();
row.put("ID", 1);
row.put("TOTAL", "${PRICE * QTY}");
row.put("PUB_TIME", "${NOW()}");
service.update("HR_USER", row);

//生成对应SQL:
UPDATE HR_USER SET
    PUB_TIME = now() ,
    TOTAL = PRICE * QTY
WHERE ID = ?
```
### 2.5 多表关联更新 
#### 2.5.1 通过TableBuilder构造RunPrepare
```java
RunPrepare prepare = TableBuilder.init("FI_USER AS FI")
		.left("HR_USER AS HR", "HR.ID = FI.ID")
		.build();
DataRow data = new DataRow();
data.put("CODE", 1);
data.putVar("NAME", "HR.NAME");
ConfigStore configs = new DefaultConfigStore();//过滤条件也可以通过TableBuilder或RunPrepare设置
configs.and("FI.ID > 10");
ServiceProxy.service().update(prepare, data, configs, "HR.TYPE_CODE:100");

//生成对应SQL:
UPDATE FI_USER AS FI
LEFT JOIN HR_USER AS HR ON HR.ID = FI.ID
        SET
FI.CODE = ?, FI.NAME = HR.NAME
WHERE (HR.TYPE_CODE = ? AND FI.ID > 10)

param0=1(java.lang.Integer)
param1=100(java.lang.String)
```
#### 2.5.2 带子查询的更新
```java
RunPrepare inner_hr = TableBuilder.init("HR_USER(ID AS HR_ID, CODE AS HR_CODE, NAME) AS HR").build();

RunPrepare master = TableBuilder.init("FI_USER AS M")   //()内指定的是最外层的查询列名，放在主表名容易误解，可以addColumns()单独指定
		.left("HRS", inner_hr, "HRS.HR_ID = M.ID", "HRS.HR_CODE = M.CODE")    //主表的表名列名要用原名 这里的子查的表名列名注意用 别名 HRS是当前子查询的别名
		.left("MM_USER AS MM", "MM.ID = HRS.HR_ID")
	.build();
DataRow data = new DataRow();
data.put("CODE", 1);
data.putVar("NAME", "HRS.NAME");
ConfigStore configs = new DefaultConfigStore();
configs.and("M.ID > 10");
ServiceProxy.service().update(master, data, configs, "M.TYPE_CODE:100");

//生成对应SQL:(注意区分内外层 别名)
UPDATE FI_USER AS M
LEFT JOIN (
        SELECT ID AS HR_ID, CODE AS HR_CODE, NAME
        FROM HR_USER AS HR
) AS HRS ON (HRS.HR_ID = M.ID AND HRS.HR_CODE = M.CODE)
SET
M.CODE = ?, M.NAME = HRS.NAME
WHERE (M.TYPE_CODE = ? AND M.ID > 10)

param0=1(java.lang.Integer)
param1=100(java.lang.String)

```
#### 2.5.3 TableBuilder构造RunPrepare 通过ConfigSTore设置各种参数及条件
```java
RunPrepare inner_hr = TableBuilder.init("HR_USER(ID AS HR_ID, CODE AS HR_CODE, NAME) AS HR").build();

RunPrepare master = TableBuilder.init("FI_USER AS M")   //()内指定的是最外层的查询列名，放在主表名容易误解，可以addColumns()单独指定
		.left("HRS", inner_hr, "HRS.HR_ID = M.ID", "HRS.HR_CODE = M.CODE")    //主表的表名列名要用原名 这里的子查的表名列名注意用 别名 HRS是当前子查询的别名
		.left("MM_USER AS MM", "MM.ID = HRS.HR_ID")
	.build();
DataRow data = new DataRow();
data.put("CODE", 1);
data.putVar("NAME", "HRS.NAME");
ConfigStore configs = new DefaultConfigStore();
configs.and("M.ID > 10");
ServiceProxy.service().update(master, data, configs, "M.TYPE_CODE:100");

//生成对应SQL:
UPDATE FI_USER AS M
LEFT JOIN (
        SELECT ID AS HR_ID, CODE AS HR_CODE, NAME
        FROM HR_USER AS HR
) AS HRS ON (HRS.HR_ID = M.ID AND HRS.HR_CODE = M.CODE)
SET
M.CODE = ?, M.NAME = HRS.NAME
WHERE (M.TYPE_CODE = ? AND M.ID > 10)

param0=1(java.lang.Integer)
param1=100(java.lang.String)
```
## 3 删除数据
### 3.1 根据主键删除
```java
DataRow row = new DataRow();
row.put("ID", 1);
service.delete("HR_USER", row);

//生成对应SQL:
DELETE FROM HR_USER WHERE ID = ?
```
### 3.2 根据多主键值删除多行
```java
DataSet set = new DataSet();
DataRow row = set.add();
row.put("ID", 1);
row = set.add();
row.put("ID", 2);
service.delete("HR_USER", set);
或
service.deletes("HR_USER", "ID", "1","2");
或
List ids = new ArrayList<>();
//service.deletes("HR_EMPLOYEE", "ID", ids);
ids.add("1");
ids.add("2");

//生成对应SQL:
DELETE FROM HR_USER WHERE ID IN(?,?)
```
### 3.3 根据条件删除 - 多条件 AND
```java
DataRow row = new DataRow();
row.put("CODE", 1);
row.put("CD", 2);
row.setPrimaryKey("CODE", "CD");
service.delete("HR_USER", row);
或
service.delete("HR_USER","CODE","1", "CD:2");

//生成对应SQL:
DELETE FROM HR_USER WHERE CODE = ? AND CD = ?
```
### 3.4 复杂条件删除
复杂每件通过ConfigStore构造
```java
ConfigStore condition = new DefaultConfigStore();
condition.and("ID" , "1");
condition.and(Compare.NOT_IN,"ID",  "100");
condition.and("ID > 100");
List between = new ArrayList<>();
between.add(1);
between.add(200);
condition.and(Compare.BETWEEN, "ID", between);
//在ConfigStore没有条件时 主键条件才会生效
service.delete("HR_USER", condition);

//生成对应SQL:
DELETE FROM HR_USER
WHERE (ID = ? AND  ID != ? AND  ID > 100 AND  ID BETWEEN ? AND ?)
param0=1(java.lang.String)
param1=100(java.lang.String)
param2=1(java.lang.Integer)
param3=200(java.lang.Integer)
```
### 3.5 截断表
```java
service.truncate("HR_USER")

//生成对应SQL:
TRUNCATE TABLE HR_USER
```

## 4 执行原生SQL
复杂SQL或出于性能考虑可以直接执行原生SQL  
SQL中可带占位符  
#{key}与:key 在执行时会替换成 ? 占位符  
${key}与::key 在执行会会直接替换成值  
### 4.1 无参数
```java
service.execute("UPDATE HR_USER SET TYPE_ID = 1 WHERE ID < 100");
```
### 4.2 带下标占位符
```java
ConfigStore configs = new DefaultConfigStore();
configs.params("1", "2");
service.execute("DELETE FROM CRM_USER WHERE ID = ? AND CODE = ?", configs);
```

### 4.3 带 $｛key｝ 占位符
```java
service.execute("DELETE FROM CRM_USER WHERE ID = ${ID}", "ID:2");

//生成对应SQL:
DELETE FROM CRM_USER WHERE ID = 2
```
### 4.4 带 #{key} 占位符
```java
service.execute("DELETE FROM CRM_USER WHERE ID = #{ID}", "ID:2");

//生成对应SQL:
DELETE FROM CRM_USER WHERE ID = ?
```
### 4.5 带 :key 占位符
```java
service.execute("DELETE FROM CRM_USER WHERE ID = :ID", "ID:2");

//生成对应SQL:
DELETE FROM CRM_USER WHERE ID = ?
```
### 4.6 带 ::key 占位符
```java
service.execute("DELETE FROM CRM_USER WHERE ID = ::ID", "ID:2");

//生成对应SQL:
DELETE FROM CRM_USER WHERE ID = 2
```
### 4.7 原生SQL中的空值条件
原生SQL中如果需要忽略空值部分，可以用${}划分出边界
```java
service.execute("DELETE FROM CRM_USER WHERE 1=1 ${AND (ID>:MAX OR ID<:MIN)} AND NAME IS NOT NULL ${AND LVL > :LVL} AND LVL < 20", configs);
//${AND (ID>:MAX OR ID<:MIN)} 和 ${AND LVL > :LVL} 部分为动态条件
//如果 MAX 或 MIN 有一个参数为空 最终执行SQL时会把这${AND (ID>:MAX OR ID<:MIN)}部分删除执行
//如果 LVL参数为空 最终执行SQL时会把这${AND LVL > :LVL}部分删除执行
```


## 5 upsert 插入前先判断记录是否已存在，不存在则插入，存在则覆盖或忽略  
不同的数据库判断机制不同   
对于MySQL,PostgreSQL,Oracle等AnyLine都是一样的操作，但不同的数据库有不同的要求，    
如MySQL根据唯一索引，PG可以根据指定列，但必须是唯一索引中的列，  
而Oracle虽然不友好但对各种情况的支持却更强一些语法上可以随意指定列  
当然无论怎么随便一定要考虑性能，更不能违反唯一约束  
具体参考 http://doc.anyline.org/aa/5d_3836

通过configs.override方法来设置数据重复时的操作  
ConfigStore configs = new DefaultConfigStore():  
configs.override(false, "CODE","TYPE");     //根据CODE,TYPE列判断数据是否已存在，如果数据存在则忽略  
configs.override(false, constraint);             //根据唯一约束判断数据是否已存在  
configs.override(true, "CODE","TYPE");      //如果存在则覆盖，不存在则正常inseart  
configs.override(true, constraint);              //如果存在则覆盖，不存在则正常insert  
false:表示重复时不覆盖直接跳过  
CODE,TYPE:表示根据这两列判断是否重复  
constraint:表示根据这个约束判断是否重复

### 5.1 存在则更新 根据指定列判断
```java
DataRow row = new DataRow();
row.put("code", "a");
row.put("name","z");
ConfigStore configs = new DefaultConfigStore();
DataSet set = new DataSet();
set.add(row);
configs.override(true, "code");
service.insert("crm_user", set, configs);

//生成对应SQL:
//ORACLE:
MERGE INTO crm_user M
USING (
        SELECT I.code AS code, I.name AS name
                FROM(
                SELECT ? AS code, ? AS name FROM DUAL ) I ) D ON(D.code = M.code)
WHEN NOT MATCHED THEN
INSERT(M.code,M.name)VALUES(D.code,D.name)
WHEN MATCHED THEN
UPDATE SET M.name = D.name

//MySQL:
INSERT INTO crm_user(code, name) VALUES (?, ?) ON DUPLICATE KEY UPDATE code = values(code), name = values(name)

//PG:
INSERT INTO CRM_USER(CODE, NAME) VALUES (?, ?),(?, ?),(?, ?) ON CONFLICT(CODE) DO UPDATE SET CODE = EXCLUDED.CODE, NAME = EXCLUDED.NAME
```
### 5.2 存在则忽略 根据指定列判断
```java
DataRow row = new DataRow();
row.put("code", "a");
row.put("name","z");
ConfigStore configs = new DefaultConfigStore();
DataSet set = new DataSet();
set.add(row);
configs.override(false, "code");
service.insert("crm_user", set, configs);


//生成对应SQL:
//MySQL:
INSERT IGNORE INTO crm_user(name, code) VALUES (?, ?)
//PG:
INSERT INTO CRM_USER(CODE, NAME, id) VALUES (?, ?, ?),(?, ?, ?),(?, ?, ?) ON CONFLICT(CODE,CODE) DO NOTHING
```
### 5.3 根据唯一约束判断数据是否已存在-存在则覆盖
```java
Table table = service.metadata().table("CRM_USER");
if(null != table){
	service.ddl().drop(table);
}
table = new Table("CRM_USER");
table.addColumn("ID", "bigint").primary(true).autoIncrement(true);
table.addColumn("CODE", "VARCHAR(10)");
table.addColumn("NAME","VARCHAR(10)");
service.ddl().create(table);
//唯一索引
Constraint u_code = new Constraint<>(table, "u_code").setType(Constraint.TYPE.UNIQUE).addColumn("CODE");
service.ddl().add(u_code);

//根据唯一索引判断 重复则覆盖
configs.override(true, u_code);
ServiceProxy.insert("CRM_USER", set, configs););

//生成对应SQL:
//PG
INSERT INTO CRM_USER(CODE, NAME) VALUES (?, ?),(?, ?),(?, ?) ON CONFLICT ON CONSTRAINT u_code DO UPDATE SET CODE = EXCLUDED.CODE, NAME = EXCLUDED.NAME
```
### 5.4 根据唯一约束判断数据是否已存在-存在则忽略
```java
Table table = service.metadata().table("CRM_USER");
if(null != table){
	service.ddl().drop(table);
}
table = new Table("CRM_USER");
table.addColumn("ID", "bigint").primary(true).autoIncrement(true);
table.addColumn("CODE", "VARCHAR(10)");
table.addColumn("NAME","VARCHAR(10)");
service.ddl().create(table);
//唯一索引
Constraint u_code = new Constraint<>(table, "u_code").setType(Constraint.TYPE.UNIQUE).addColumn("CODE");
service.ddl().add(u_code);

//根据唯一索引判断 重复则忽略
configs.override(false, u_code);
ServiceProxy.insert("CRM_USER", set, configs););

//生成对应SQL:
//PG
INSERT INTO CRM_USER(CODE, NAME) VALUES (?, ?),(?, ?),(?, ?) ON CONFLICT ON CONSTRAINT u_code DO NOTHING
```
### 5.5 根据DataRow.override执行-存在则覆盖
如果数据库不支持重复检测 或 数据不满足数据库要求(比如无法创建索引)，但还需要实现类似的功能  
可以通过DataSet/DataRow提供的setOverride(true|false)实现  
但这要会比较耗时，因为需要去数据库中挨行查询一次确认数据是否存在。  
只有通过configs.override不能实现的情况下才用这种处理方式

```java
DataRow row = new DataRow();
row.put("K", "V")
row.setPrimaryKey("name", "code")//如果不是默认主键可以在这里临时设置逻辑主键
row.setOverride(true)
service.save(row);

```
### 5.6 根据DataRow.override执行-存在则忽略
```java
DataRow row = new DataRow();
row.put("K", "V")
row.setPrimaryKey("name", "code")//如果不是默认主键可以在这里临时设置逻辑主键
row.setOverride(false)
service.save(row);
```
### 5.7 insert 与 update行混合save
根据是否有主键值分两组分别执行insert与update
```java
service.save(set);
```

## 6 开启元数据检测执行类型转换 
插入、更新、查询、删除前调用有效
开启后，在每次执行命令前会检测元数据信息，并根据元数据转换参数值的数据类型
数据类型只检测一次，检测成功后会缓存，只有缓存过期或调用CacheProxy.clear() 或修改了表结构才会清空缓存。

```java
ConfigTable.IS_AUTO_CHECK_METADATA = true;

//通过ConfigTable属性设置元数据缓存
int METADATA_CACHE_SCOPE	= 9				;   // 0:不缓存 1:当前线程 9:整个应用
String METADATA_CACHE_KEY	= ""			;	// 表结构缓存key
int METADATA_CACHE_SECOND	= 3600*24		;	// 表结构缓存时间(-1:表示永不失效)
int CHECK_METADATA_SAMPLE	= 1000			;   // 检测元数据的样本数量(-1:全部 0:不检测)(mongo当前版本中没有提供元数据查询方法)

```
