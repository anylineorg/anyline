# DQL查询条件

## 概述
本文档涵盖通过 condition()方法解析http参数生成DQL查询条件，condition()方法返回ConfigStore对象    
http参数支持get/post/raw-json格式  
其中get参数格式支持:code=0&code=1或code=0,1  
condition(true)表示需要分页 condition(10)表示分页并显式指定一页10行
AnylineController中提供了condition()



## 约定符号说明
"[http参数]"中的[]表示数组   
如CODE:[cd]表示request参数中cd是数组形式查询需要IN函数 CODE IN(1,2,3)    
"[数据库列表]:http参数"中的[]表示数据库中CODES是数组形式如1,2,3 查询时需要FIND_IN_SET函数 FIND_IN_SET('1', CODES)      
"+"开头表示必须条件，如果没有值传则生成CODE IS NULL的条件(仅"="时有效，其他IN,>时，当前条件忽略)    
“++”开头时，如果没有传值则整个SQL不执行，返回长度为零的DataSet  
"+"结尾表示参数值加过密码   
“++”结尾表示参数值和参数key都加过密码  
~表示约、忽略大小写，对PG系有效，其他数据库与LIKE等效, 如"CODE:~%code%"  

具体效果参考 http://doc.anyline.org/cft

## 约定格式
condition方法的参数格式一般是以 数据库列表:http参数名 的格式提供
### 1 常用方式
```java
service.selects("CRM_USER", condition("TYPE_ID:type"))

//生成对应SQL:
SELECT * FROM CRM_USER WHERE TYPE_ID = ?
```
### 2 指定数据类型
```java
service.selects("CRM_USER", condition("TYPE_ID:type::int"))

//生成对应SQL:
SELECT * FROM CRM_USER WHERE TYPE_ID = ?
```
### 3 模糊查询
```java
service.selects("CRM_USER", condition("CODE:%code%"))
//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE LIKE '%?%'
        
service.selects("CRM_USER", condition("CODE:%code"))
//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE LIKE '%?'

service.selects("CRM_USER", condition("CODE:code%"))
//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE LIKE '?%'
```


### 4 忽略大小写模糊查询
~表示约、忽略大小写，对PG系有效，其他数据库与LIKE等效
```java
service.selects("CRM_USER", condition("CODE:~%code%"))

//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE ILIKE '%?%'
```

### 5 多参数解析
依次通过code,cd取值
```java
service.selects("CRM_USER", condition("CODE:%code:cd%"))
```
### 6 多参数解析
依次通过code,cd取值,如果都失败，则指定常量值
```java
//${}表示常量
service.selects("CRM_USER", condition("CODE:%code:cd:${9}%"))
//{[]}表示常量值是一个数组
service.selects("CRM_USER", condition("CODE:[code:cd:${[6,7,8]}]"))

```
### 7 in条件
[]表示数组
```java
service.selects("CRM_USER", condition("CODE:[code]"))
service.selects("CRM_USER", condition("CODE:[code]::int"))

//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE IN(?,?,?)
```
### 8 非等于条件
```java
service.selects("CRM_USER", condition("CODE:>=code")) 

//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE >= ?
```

### 9 OR条件
#### 9.1
只有code取值成功,当前条件才生效
```java
service.selects("CRM_USER", condition("CODE:code|cd"))
//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE = ?(code值) OR CODE = ?(cd值)

service.selects("CRM_USER", condition("CODE:code|{NULL}"))
//生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE = ?(code值) OR CODE IS NULL

```
#### 9.2
code与cd不相干,哪个有值取哪个
```java
service.selects("CRM_USER", condition("CODE:code|CD:cd"))
//如果code没有值生成对应SQL:
SELECT * FROM CRM_USER WHERE CD = ?(cd值)
//如果code和cd都有值有生成对应SQL:
SELECT * FROM CRM_USER WHERE CODE = ?(code值) OR CD = ?(cd值) 
```
### 10 FIND_IN_SET
```java
service.selects("CRM_USER", condition("[CODES]:code"))
//生成对应SQL:
SELECT * FROM CRM_USER WHERE FIND_IN_SET(?, CODES)
    
service.selects("CRM_USER", condition("[CODES]:[codes]"))
//如果code是一个数组值，生成对应SQL:
SELECT * FROM CRM_USER WHERE FIND_IN_SET(?, CODES) OR FIND_IN_SET(?, CODES)
```

### 11 自定义方法处理参数值(不推荐)
#### 11.1 内置类
调用默认类org.anyline.jdbc.config.DefaultPrepare的split预处理参数值
```java
service.selects("CRM_USER", condition("CODE:[split(code)]"))
```
#### 11.2 自定义类
指定完整的包名.类名.方法名
```java
service.selects("CRM_USER", condition("CODE:[org.ClassA.split(code)]"))
```
 
### 12 空值条件处理
"+"开头表示必须条件，如果没有值传则生成CODE IS NULL的条件(仅"="时有效，其他IN,>时，当前条件忽略)  
“++”开头时，如果没有传值则整个SQL不执行，返回长度为零的DataSet  
```java
service.selects("CRM_USER", condition("+CODE:code"))
service.selects("CRM_USER", condition("++CODE:code"))
```
### 13 加密参数
"+"结尾表示参数值加过密码  
“++”结尾表示参数值和参数key都加过密码
```java
service.selects("CRM_USER", condition("CODE:code+"))
service.selects("CRM_USER", condition("CODE:code++"))
//code值加密 cd值未加密
service.selects("CRM_USER", condition("CODE:code+:cd"))
```
