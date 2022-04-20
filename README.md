 8.3.8之后代码已迁至[https://github.com/anylineorg/anyline](http://https://github.com/anylineorg/anyline)  
 8.3.8之前提交历史记录查看[https://github.com/anylineorg/anyline-history.git](https://github.com/anylineorg/anyline-history.git)  

***快速开始请参考示例代码:***  
 [https://gitee.com/anyline/anyline-simple.git](https://gitee.com/anyline/anyline-simple.git)  
[https://github.com/anylineorg/anyline-simple.git](https://github.com/anylineorg/anyline-simple.git)  
 

AnyLine的核心是一个基于spring-jdbc的,脱离了各种O的ORM,用来简化数据库以及针对结果集的操作。  
提供了一对基础的service/dao。通过接收约定好格式的参数(详细参考[参数格式]( http://doc.anyline.org/s?id=1059 )),来完成绝大部分的数据库操作。  
大多数情况下不再需要Service/Dao/Bean/VO/DTO/PO/BO/Mapper  
默认提供DataRow/DataSet两种数据结构用来取代实体Bean实现MVC各层之间的数据传输  

***为什么大多数场景中不需要各种O了***  
在接收前端参数时、在从数据库中查询数据时，数据结构千变万化  
如果针对每个请求和数据库查询都生成一个对应的O就太啰嗦了  
实际开发中有多少的O只用到了get/set

接收前端参数通常为了生成SQL  
可以交给Anyline的参数解析器来完成，不需要各种属性get/set,各种判断,遍历,生成SQL

从数据库中查询的最终通过是为了显示给前端,有可能需要经过一定的计算  
DataRow/DataSet自带了常用的排序，求和，截取，清除空值，按列去重，最大最小值，交集合集差集，分组，行列转换，类SQL筛选(like,eq,in,less,between...)，JSON,XML格式转换等函数  

例如实现一个求和，在哪里实现呢？写一个BeanUtil通过反射实现么?  
再如把行转成列后，用什么结构来接收转换的结果呢?

操作过程大致如下:  
```
DataSet set = service.querys("HR_USER(ID,NM)"
    , condition(true
    ,"TYPE_CODE:[type]"
    ,"NM:nm%"
    ,"+ZIP_CODE:zip:"
    ,"++DEPT_CODE:dept"
    ,"POST_CODE:post:pt:{101}")
    , "IS_PUB:1");
```
实现功能:
    根据TYPE_CODE,NM,IS_PUB列查询HR_USER表,并根据http参数自动分页
生成的SQL(以mysql为例):

```
SELECT
    ID,NM
FROM HR_USER  
WHERE
    TYPE_CODE IN (?,?,?)  
    AND NM LIKE CONCAT(?,'%')
    AND ZIP_CODE = ?
    AND DEPT_CODE = ?
    AND POST_CODE = ?
    AND IS_PUB = ?
LIMIT 0,10
```
以上SQL根据http参数传递情况动态生成，如果http中没有相关的参数则忽略相应的查询条件。  
IS_PUB值从后台固定设置而不是来自http   
TYPE_CODE IN(?,?,?)中的占位符数量根据http中type参数数量生成  
如果http中没有提供zip则生成ZIP_CODE IS NULL的查询条件  
如果http中没有提供dept则整个SQL将不执行，当前querys函数返回长度为0的DataSet  
如果http中没有提供post，则取根据pt取值，如果没有提供pt，则使用默认值101,其中的{}表示常量，而不是http中的参数key  
注意IS_PUB的值是一个固定值1，也不是通过http参数中取值，所以不需要放在condition函数中转换  


```
anyline-core                : 基础包
anyline-web                 : javaEE基础
anyline-net                 : 多任务下载，下载进度，断点续传，网速计算，耗时预算，下载回调	
anyline-struts              : strtus2支持
anyline-mvc                 : springmvc支持
anyline-jdbc                : jdbc基础
anyline-jdbc-mysql          : mysql支持
anyline-jdbc-mssql          : sql server支持
anyline-jdbc-db2            : db2支持
anyline-jdbc-dm             : 达梦支持
anyline-jdbc-oracle         : oracle支持
anyline-jdbc-postgresql     : PostgreSQL支持
anyline-jdbc-hgdb           : 瀚高数据库支持
anyline-jdbc-clickhouse     : clickhouse
anyline-ldap                : ldap
anyline-nacos               : nacos
anyline-alipay              : 支付宝支付集成工具
anyline-aliyun              : 阿里云平台相关工具
anyline-wechat              : 微信支付,微信开放平台，公众平台集成工具
anyline-tencent             : 腾讯云
anyline-qq                  : QQ支付，QQ开放平台，公众平台集成工具
anyline-jpush               : 极光推送集成工具
anyline-easemob             : 环信集成工具
anyline-amap                : 高德云图集成工具
anyline-sms                 : 基于阿里云的短信发送工具
anyline-mail                : 邮件接收发送
anyline-comm                : 串、并口通信
anyline-video               : 视频处理
anyline-ext                 : 水印,动画等扩展功能
anyline-cache-ecache        : ecache
anyline-cache-redis         : redis
anyline-print               : 小票打印机
anyline-nc                  : netcdf解析
anyline-office              : 基于open xml的word/excel操作
anyline-poi                 : 基于poi的msoffice文件操作
```
