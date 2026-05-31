# DML操作

## 概述
本文档涵盖通过 Anyline 进行的DQL操作。所有操作通过注入的 `AnylineService` 实例或 `ServiceProxy` 调用。
select:返回DataRow 有可能为null 可以通过ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL = true; 实现查询无结果时返回new DataRow();  
selects:返回DataSet&lt;DataRow&gt; 不会返回null 查无结果时会返回空集合    
map:返回Map  
maps:返回List &lt;Map&gt;  
query:返回Entity  
queries:返回Entity集合

## 1 简单查询
```java
service.selects("HR_USER");

//生成对应SQL:
SELECT * FROM HR_USER
```
## 2 指定列查询
### 2.1 单列
```java
service.selects("HR_USER(NAME)");

//生成对应SQL:
SELECT NAME FROM HR_USER
```
### 2.2  多列逗号分隔
```java
service.selects("HR_USER(NAME, CODE)");
//生成对应SQL:
SELECT NAME , CODE FROM HR_USER
```
### 2.3  列别名 标准 AS 语法
```java
service.selects("HR_USER(NAME AS USER_NAME, CODE AS USER_CODE)");
//生成对应SQL:
SELECT NAME AS USER_NAME, CODE AS USER_CODE FROM HR_USER
```
### 2.4  组合列名
```java
service.selects("HR_USER(NAME AS USER_NAME, PRICE * QTY AS TOTAL)");
//生成对应SQL:
SELECT NAME AS USER_NAME, PRICE * QTY AS TOTAL FROM HR_USER
```
## 3 表别名 
### 3.1 单表别名生成
```java
service.selects("HR_USER(M.NAME AS USER_NAME) AS M")
    
//生成对应SQL:
SELECT M.NAME AS USER_NAME FROM HR_USER AS M 
```
### 3.2 多表别名 
参考TableBuilder与PrepareRun
```java
TableBuilder builder = TableBuilder.init("FI_USER AS FI")
    .left("HR_USER AS HR", "FI.ID = HR.ID")
    .columns("FI.ID AS FI_ID", "HR.ID AS HR_ID").distinct(true);
RunPrepare prepare = builder.build();
ServiceProxy.selects(prepare);
 
TableBuilder与JSON互换
String json = builder.json();
service.selects(TableBuilder.build(json), 其他查询条件...);
```
## 4 去重
### 4.1 单列去重
```java
service.selects("HR_USER(DISTINCT TYPE_ID)")

//生成对应SQL:
SELECT DISTINCT TYPE_ID FROM HR_USER
```
## 4.2 多列去重 
```java
service.select("HR_USER(DISTINCT TYPE_ID, TYPE_NAME)")

//生成对应SQL:
SELECT DISTINCT TYPE_ID, TYPE_NAME FROM HR_USER
```
## 5 查询条件
空值查询条件默认被忽略  
+COLUMN:VALUE 表示如果VALUE为空(null或空String) 则生成 COLUMN IS NULL条件  
++COLUMN:VALUE 表示如果VALUE为空(null或空String) 则阻断SQL执行  
### 5.1 K:V条件
比较简单的场景下可以通过k:v生成带占位符的查询条件,默认K=V
```java
service.selects("HR_USER", "CODE:1", "TYPE_ID:>=100")

//生成对应SQL:
SELECT * FROM HR_USER WHERE CODE = ? AND TYPE_ID >= ?
param0=1(String)
param0=100(String)
```
### 5.2 K:V条件指定数据类型
部分数据库对数据类型要求严格，不能隐式转换，如果类型不匹配会抛出异常
```java
service.selects("HR_USER", "CODE:1::int")

//生成对应SQL:
SELECT * FROM HR_USER WHERE CODE = ?
param0=1(int)
//生成对应SQL:
```
### 5.3 自动识别数据类型
开启元数据检测后，每次执行会检查需要的数据类型再进行类型转换
```java
ConfigTable.IS_AUTO_CHECK_METADATA=true
        service.selects("HR_USER", "ID:1");

//生成对应SQL:
SELECT * FROM HR_USER WHERE CODE = ?
param0=1(int)
```
### 5.4 多查询条件
多个条件默认用AND连接,OR连接方式参考ConfigStore合成条件
```java
service.selects("HR_USER", "TYPE_ID:1", "CLASS_ID:2");
    
//生成对应SQL:
SELECT * FROM HR_USER WHERE TYPE_ID = ? AND CLASS_ID = ?
```
### 5.5 ConfigStore合成查询条件
比较复制的条件需要通过ConfigStore合成
#### 5.5.1 完整参数说明
ConfigStore重载了大量and()方法
```java
/**
 * 构造查询条件
 * @param swt 遇到空值处理方式
 * @param prefix 表别名或XML中查询条件的ID或表名
 * @param var XML自定义SQL条件中指定变量赋值或占位符key或列名 在value值为空的情况下 如果var以+开头会生成var is null 如果以++开头当前SQL不执行 这与swt作用一样,不要与swt混用 注意会有++a.id的形式
 * @param value 值 可以是集合
 * @param compare 匹配方式如等于,大于等
 * @param overCondition 覆盖相同key并且相同运算符的条件,true在现有条件基础上修改(多个相同key的条件只留下第一个),false:添加新条件
 * @param overValue		覆盖相同key并且相同运算符的条件时，是否覆盖条件值,true:删除析来的值 false:原来的值合成新的集合
 * @return ConfigStore
 */
@Override
public ConfigStore and(EMPTY_VALUE_SWITCH swt, Compare compare, String prefix, String var, Object value, boolean overCondition, boolean overValue) {
```
### 5.5.2 常用方式
```java
ConfigStore configs = new DefaultConfigStore();
configs.and("TYPE_ID", 1);
//集合参数值默认生成IN条件
List<Integer> ages = new ArrayList();
ages.add(20); 
ages.add(30);
configs.and("CODE", ages); 
//非 等条件需要指定
configs.gt("JOIN_DATE", "2020-01-01");
//在低代码、自定义报表场景中，查询条件通常是由前端动态生成，可以统一用and生成，根据前端参数解析Compare
configs.and(Compare.GREAT_EQUAL, "SALARY", 10000); 
service.selects("HR_USER", configs);

//生成对应SQL:
SELECT * FROM HR_USER WHERE TYPE_ID = ? 
AND CODE IN(?, ?)
AND JOIN_DATE > ?    
AND SALARY >= ?
```
### 5.5.3 约定JSON格式
在低代码等自定义查询条件的解析过程比较麻烦，可以按ConfigStore约定的格式提交一个JSON实现ConfigStore的自动解析
完整的JSON格式参考  http://doc.anyline.org/aa/73_13975  
多表关联的JSON格式参考  http://doc.anyline.org/aa/a3_14036  
```java
ConfigStore configs = ConfigBuilder.build(json);
ServiceProxy.select("表名", configs);
```

同时ConfigStore也可以逆向生成JSON格式用来保存或传输    
json格式参数ConfigStore.json()方法输出的格式  
JSON格式比较麻烦，需要什么条件可以先用ConfigStore逆向生成一下看看
```java
ConfigStore configs = new DefaultConfigStore();
configs.and("ID", 1);
configs.like("NAME", "ZH");
configs.in("TYPE_CODE", "1,2,3".split(","));
ConfigStore ors = new DefaultConfigStore();
ors.and("A", 1);
ors.or("B", 2);
configs.or(ors);
configs.group("TYPE_CODE").group("CLASS_CODE");
configs.having("MIN(PRICE)>100").having("COUNT(*)>1"); 
//如果having条件比较复杂可以通过ConfigStore构造
ConfigStore having = new DefaultConfigStore();
having.and("MAX(ID)>0").or("MIN(ID)>0").and(Compare.GREAT, "COUNT(*)", 1);
configs.having(having);
//这里可以生成JSON格式
String json = configs.json(true); 
```


### 5.5.4 OR 条件
当OR前面有多个条件时需要注意边界  
ConfigStore提供了两个方法:     
or()表示与其前一个条件形成or关系，configs.and(A).and(B).or(C)生成 A AND B OR C   
ors()表示与其前面所有条件形成or关系, configs.and(A).and(B).ors(C)生成 (A AND B) OR C  



## 5.6 原生SQL
复杂SQL或出于性能考虑可以直接执行原生SQL  
SQL中可带占位符  
#{key}与:key 在执行时会替换成 ? 占位符  
${key}与::key 在执行会会直接替换成值
### 5.6.1
```java
service.selects("SELECT * FROM HR_USER WHERE ID > 10");
```

### 5.6.2 带下标占位符
```java
ConfigStore configs = new DefaultConfigStore();
configs.params("1", "2");
service.execute("SELECT * FROM CRM_USER WHERE ID = ? AND CODE = ?", configs);
```

### 5.6.3 带 $｛key｝ 占位符
```java
service.execute("SELECT * FROM CRM_USER WHERE ID = ${ID}", "ID:2");

//生成对应SQL:
SELECT * FROM CRM_USER WHERE ID = 2
```
### 5.6.4 带 #{key} 占位符
```java
service.execute("SELECT * FROM CRM_USER WHERE ID = #{ID}", "ID:2");

//生成对应SQL:
SELECT * FROM CRM_USER WHERE ID = ?
```
### 5.6.5 带 :key 占位符
```java
service.execute("SELECT * FROM CRM_USER WHERE ID = :ID", "ID:2");

//生成对应SQL:
SELECT * FROM CRM_USER WHERE ID = ?
```
### 5.6.6 带 ::key 占位符
```java
service.execute("SELECT * FROM CRM_USER WHERE ID = ::ID", "ID:2");

//生成对应SQL:
SELECT * FROM CRM_USER WHERE ID = 2
```

### 5.7 空值条件
默认情况下空值条件会忽略,用+和++前缀可以处理大部分情况，更复杂的情况参考ConfigStore合成查询条件
#### 5.7.1 默认忽略
适合于动态查询条件，有值则过滤，无值则忽略
```java
String code = null; //这里一般是从前端传入
service.selects("HR_USER", "CODE:"+code);
service.selects("HR_USER", "CODE:null");
service.selects("HR_USER", "CODE:");

//生成对应SQL:
SELECT * FROM HR_USER
```
#### 5.7.2 强制条件
有极少场景中，需要强制添null过滤条件
用+前缀表示强制条件，无论是null还是空String都当成NULL处理
```java
String code = null; //这里一般是从前端传入
service.selects("HR_USER", "+CODE:"+code);
service.selects("HR_USER", "+CODE:null");
service.selects("HR_USER", "+CODE:");

//生成对应SQL:
SELECT * FROM HR_USER WHERE CODE IS NULL
```
#### 5.7.3 阻断执行
有些场景中，如果参数没有值需要阻断整个SQL执行，如根据id查询
```java
String id = null; //这里一般是从前端传入
service.select("HR_USER", "++ID:"+id);
service.select("HR_USER", "++ID:null");
service.select("HR_USER", "++ID:");

//生成对应SQL:
整个SQL不会执行,返回null
```
#### 5.7.4 复杂场景
更复杂的场景需要通过ConfigStore方法合成查询条件  
通过Compare.EMPTY_VALUE_SWITCH 的枚举值设置遇到空值时的处理方式  
IGNORE   //忽略当前条件  其他条件继续执行  
BREAK	   //中断执行 整个命令不执行   
NULL	   //生成 WHERE ID IS NULL  
SRC	   //原样处理 会生成 WHERE ID = NULL  
EXCEPTION //抛出异常  
NONE	   //根据条件判断 ++或+  
```java
String code = ""; //这里一般是从前端传入
ConfigStore configs = new DefaultConfigStore();
configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, "CODE", code);
service.select("HR_USER", configs);

//生成对应SQL:
SELECT * FROM HR_USER WHERE CODE = ?
param0=""(String)
```
#### 5.7.5 原生SQL中的空值条件
原生SQL中如果需要忽略空值部分，可以用${}划分出边界
```java
service.selects("SELECT * FROM CRM_USER WHERE 1=1 ${AND (ID>:MAX OR ID<:MIN)} AND NAME IS NOT NULL ${AND LVL > :LVL} AND LVL < 20", configs);
//${AND (ID>:MAX OR ID<:MIN)} 和 ${AND LVL > :LVL} 部分为动态条件
//如果 MAX 或 MIN 有一个参数为空 最终执行SQL时会把这${AND (ID>:MAX OR ID<:MIN)}部分删除执行
//如果 LVL参数为空 最终执行SQL时会把这${AND LVL > :LVL}部分删除执行
```
## 5.8 order group having
可与查询条件参数混在一起，方法内部可以识别
```java
service.select("HR_USER(TYPE_ID, MAX(AGE) AS MAX_AGE, MIN(AGE) AS MIN_AGE)", "GROUP BY TYPE_ID", "ORDER BY ID", "HAVING COUNT(*) > 1");
```

## 6 TableBuilder RunPrepare
多表关联时可以通过 TableBuilder 合成 RunPrepare

### 6.1 指定查询列名
```java
//表名(列,列)
RunPrepare prepare = TableBuilder.init("FI_USER(ID AS USER_ID, CODE)").build();
ServiceProxy.selects(prepare);

//生成对应SQL:
SELECT  ID AS USER_ID, CODE FROM FI_USER
```

### 6.2 ConfigStore指定查询列名 附带带查询条件
```java
RunPrepare prepare = TableBuilder.init("FI_USER").build();
service.selects(prepare, new DefaultConfigStore().columns("id,code") , "ID > 0");

//生成对应SQL:
SELECT  id, code FROM FI_USER WHERE ID > 0
```

### 6.3 表别名
```java
//表名(列,列) AS 表别名
RunPrepare prepare = TableBuilder.init("FI_USER(ID AS USER_ID, CODE) AS M").build();
ServiceProxy.selects(prepare);

//生成对应SQL:
SELECT  ID AS USER_ID, CODE FROM FI_USER AS M
```

### 6.4 多表关联
```java
RunPrepare prepare = TableBuilder.init("FI_USER AS FI").left("HR_USER AS HR", "FI.ID = HR.ID").build();
ServiceProxy.selects(prepare);

//生成对应SQL:
SELECT * 
FROM FI_USER AS FI
LEFT JOIN HR_USER AS HR ON FI.ID = HR.ID
```
### 6.5 多表关联指定查询列
可以在表名后指定
```java
RunPrepare prepare = TableBuilder.init("FI_USER(FI.ID AS FI_ID, HR.ID AS HR_ID) AS FI")
    .left("HR_USER AS HR", "FI.ID = HR.ID")
    .build();
ServiceProxy.selects(prepare);

//生成对应SQL:
SELECT
FI.ID AS FI_ID, HR.ID AS HR_ID
FROM FI_USER AS FI
LEFT JOIN HR_USER AS HR ON FI.ID = HR.ID
```
### 6.6 多表关联带查询条件
```java
RunPrepare prepare = TableBuilder.init("FI_USER(FI.ID AS FI_ID, HR.ID AS HR_ID) AS FI")
    .left("HR_USER AS HR", "FI.ID = HR.ID")
    .build();
ServiceProxy.selects(prepare, "FI.ID:1::bigint");

//生成对应SQL:
SELECT
    FI.ID AS FI_ID, HR.ID AS HR_ID
FROM FI_USER AS FI
LEFT JOIN HR_USER AS HR ON FI.ID = HR.ID
WHERE FI.ID = ?

```
### 6.7 columns()方法单独指定列
```java
RunPrepare prepare = TableBuilder.init("FI_USER AS FI")
        .left("HR_USER AS HR", "FI.ID = HR.ID")
        .columns("FI.ID AS FI_ID", "HR.ID AS HR_ID")
        .build();
ServiceProxy.selects(prepare);

//生成对应SQL:
SELECT
    FI.ID AS FI_ID, HR.ID AS HR_ID
FROM FI_USER AS FI
LEFT JOIN HR_USER AS HR ON FI.ID = HR.ID
```

### 6.8 子查询
```java
RunPrepare inner_hr = TableBuilder.init("HR_USER(ID AS HR_ID, CODE AS HR_CODE) AS HR").build();
RunPrepare master = TableBuilder.init("FI_USER(M.ID AS FI_ID, HRS.HR_CODE) AS M")   //()内指定的是最外层的查询列名，放在主表名容易误解，可以addColumns()单独指定
    .left("HRS", inner_hr, "HRS.HR_ID = M.ID", "HRS.HR_CODE = M.CODE")    //主表的表名列名要用原名 这里的子查的表名列名注意用 别名 HRS是当前子查询的别名
    //.columns("M.ID AS ID1", "M.ID AS ID2", "HR.HR_ID AS ID3")                            //设置查询列名，注意是追加不会覆盖  覆盖用setColumns()
    .build();
ServiceProxy.selects(master);
//生成对应SQL(注意区分内外层 别名):
SELECT
    M.ID AS FI_ID, HRS.HR_CODE
FROM FI_USER AS M
LEFT JOIN (
    SELECT
        ID AS HR_ID, CODE AS HR_CODE
    FROM HR_USER AS HR
) AS HRS ON (HRS.HR_ID = M.ID AND HRS.HR_CODE = M.CODE)

```

### 6.8 子查询过滤条件
```java
 //子查询
ConfigStore configs = new DefaultConfigStore();
        configs.and("ID", ""); //空条件忽略
        configs.and("CODE='1'");
        configs.and("LVL", 2);
RunPrepare inner_fi = TableBuilder.init("FI_USER(ID AS FI_ID, CODE AS FI_CODE, 'FI' AS BIZ_TYPE_CODE) AS FI").condition(configs).build();

configs = new DefaultConfigStore();
        configs.and("ID", "");//空条件忽略
        configs.and("CODE","10");
        configs.and("LVL", 20);
RunPrepare inner_hr = TableBuilder.init("HR_USER(ID AS HR_ID, CODE AS HR_CODE) AS HR").condition(configs).build();

RunPrepare group_mm = TableBuilder.init("HR_USER(TYPE_CODE, LVL, MAX(ID) AS MAX_ID) AS MM").build().group("TYPE_CODE", "LVL").having("MAX(ID) > 10");

//主表
RunPrepare master = TableBuilder.init("FIS", inner_fi)   //主表也用一个子查询
        .left("HRS", inner_hr, "HRS.HR_ID = FIS.FI_ID", "HRS.HR_CODE = FIS.FI_CODE")                  //主表的表名列名要用原名 这里的子查的表名列名注意用 别名
        .left("MMS", group_mm, "MMS.MAX_ID = FIS.FI_ID")
        .setColumns("FIS.FI_ID AS FI_IDS","1 AS STATIC_VALUE", "FIS.BIZ_TYPE_CODE") //注意里这里要用外层别名
        .build();
service.selects(master, "HRS.HR_ID > 3", "HRS.HR_CODE:30::int");


//生成对应SQL:
SELECT
FIS.FI_ID AS FI_IDS, 1 AS STATIC_VALUE, FIS.BIZ_TYPE_CODE
FROM (
                SELECT
                        ID AS FI_ID, CODE AS FI_CODE, 'FI' AS BIZ_TYPE_CODE
                FROM FI_USER AS FI
                        WHERE (CODE=1 AND FI.LVL = ?)
            ) AS FIS
LEFT JOIN (
        SELECT
                ID AS HR_ID, CODE AS HR_CODE
                FROM HR_USER AS HR
                WHERE (CODE=10 AND HR.LVL = ?)
            ) AS HRS ON (HRS.HR_ID = FIS.FI_ID AND HRS.HR_CODE = FIS.FI_CODE)
LEFT JOIN (
        SELECT
                TYPE_CODE, LVL, MAX(ID) AS MAX_ID
FROM HR_USER AS MM
GROUP BY TYPE_CODE, LVL HAVING MAX(ID) > 10
        ) AS MMS ON MMS.MAX_ID = FIS.FI_ID
WHERE (HRS.HR_ID > 3 AND HRS.HR_CODE = ?)

```