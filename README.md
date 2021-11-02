 8.3.8之后代码已迁至[https://github.com/anylineorg/anyline](http://https://github.com/anylineorg/anyline)  
 8.3.8之前提交记录查看[https://github.com/anylineorg/anyline-history.git](https://github.com/anylineorg/anyline-history.git)  
 
 demo:[https://gitee.com/anyline/anyline-simple.git](https://gitee.com/anyline/anyline-simple.git)  
 
 demo:[https://github.com/anylineorg/anyline-simple.git](https://github.com/anylineorg/anyline-simple.git)  
 
|     |参数>|code=0|code=|<font size=7 >code=0&amp;code=1<br/>&amp;cd=2&amp;user=5</font>|cd=2&amp;cd=3|code=0(密)|cd=2(密)<br/>&amp;cd=3(密)|
| --- | --- | ---  | --- | ---                                 | ---         | ---       | ---                   | 
1|CODE:code|CODE = 0|忽略|CODE = 0|忽略|忽略|忽略|
2|CODE:%code%|CODE LIKE '%0%'|忽略|CODE LIKE '%0%'|忽略|忽略|忽略|
3|CODE:%code|CODE LIKE '%0'|忽略|CODE LIKE '%0'|忽略|忽略|忽略|
4|CODE:code%|CODE LIKE '0%'|忽略|CODE LIKE '0%'|忽略|忽略|忽略|
5|CODE:%code: cd% |CODE LIKE '%0%'|忽略|CODE LIKE '%0%'|CODE LIKE '%2%'|忽略|忽略|
6|CODE:%code: cd:{9}% |CODE LIKE '%0%'|CODE LIKE '%9%'|CODE LIKE '%0%'|CODE LIKE '%2%'|忽略|忽略|
7|CODE:%code: cd|CODE LIKE '%0'|忽略|CODE LIKE '%0'|CODE LIKE '%2'|忽略|忽略|
8|CODE:%code: cd:{9}|CODE LIKE '%0'|CODE LIKE '%9'|CODE LIKE '%0'|CODE LIKE '%2'|忽略|忽略|
9|CODE:[code]  |CODE = 0|忽略|CODE IN(0,1)|忽略|忽略|忽略|
10|CODE:[code: cd]|CODE = 0|忽略|CODE IN(0,1)|CODE IN(2,3)|忽略|忽略|
11|CODE:[cd+]|忽略|忽略|CODE = 2|CODE IN(2,3)|忽略|CODE IN(2,3)|
12|CODE:[code: cd:{[6,7,8]}]  |CODE = 0|CODE IN(6,7,8)|CODE IN(0,1)|CODE IN(2,3)|忽略|忽略|
13|CODE:[code: cd:{6,7,8}]|CODE = 0|CODE IN(6,7,8)|CODE IN(0,1)|CODE IN(2,3)|忽略|忽略|
14|+CODE:code  |CODE = 0|CODE IS NULL|CODE = 0|CODE IS NULL|忽略|忽略|
15|++CODE:code  |CODE = 0|不执行|CODE = 0|不执行|忽略|忽略|
16|CODE:&gt;code|CODE &gt; 0|忽略|CODE &gt; 0|忽略|忽略|忽略|
17|CODE:&gt;code: cd|CODE &gt; 0|忽略|CODE &gt; 0|CODE &gt; 2|忽略|忽略|
18|CODE:&gt;code:{9}|CODE &gt; 0|CODE &gt; 9|CODE &gt;0|CODE &gt; 9|CODE &gt; 9|CODE &gt; 9|
19|CODE:code: cd|CODE = 0|忽略|CODE = 2|CODE = 2|忽略|忽略|
20|CODE:code: cd:{9}|CODE = 0|CODE = 9|CODE = 0|CODE = 2|忽略|忽略|
21|CODE:code\|cd |CODE = 0|忽略|CODE =0 OR CODE = 2|忽略 |忽略|忽略|
22|CODE:code\|{NULL}|CODE = 0 OR CODE IS NULL|忽略|CODE = 0 OR CODE IS NULL|忽略|忽略|忽略|
23|CODE:code\|CODE: cd  |CODE = 0|忽略|CODE = 0 OR CODE = 1|CODE = 2 |忽略|忽略|
24|CODE:code\|CD: cd  |CODE = 0|忽略|CODE = 0 OR CD = 2|CD = 2|忽略|忽略|
25|CODE:code: cd\|user|CODE = 0|忽略|CODE = 0 OR CODE = 5|CODE = 2|忽略|忽略|
26|CODE:code: cd\|{9}|CODE = 0|忽略|CODE = 0 OR CODE = 9|CODE = 2 OR CODE = 9|CODE = 9|CODE = 9|
27|CODE:code+:{9} |CODE = 9|CODE = 9|CODE = 9|CODE = 9|CODE = 0|CODE = 9|
28|CODE:code+: cd:{9}|CODE = 9|CODE = 9|CODE = 2|CODE = 2|CODE = 0|CODE = 9|
29|CODE:code+: cd+|忽略|忽略|忽略|忽略|CODE = 0|CODE = 2|
30|CODE:code\|CODE: cd\|CD: cd\|CD:code|CODE = 0 OR CD = 0|忽略|CODE =0 OR CODE = 2 OR ID =0 OR ID = 2|CODE =2 OR CD =2|忽略|忽略|
31|CODE:code:{9}\|CD: cd:{9}|CODE = 0 OR CD = 9|CODE = 9 OR CD = 9|CODE = 0 OR CD = 2|CODE = 9 OR CD = 2|CODE = 9 OR CD = 9|CODE = 9 OR CD = 9| 

详细参考:[http://doc.anyline.org/s?id=1059](http://doc.anyline.org/s?id=1059)

 **- 常规数据库操作** 

AnyLine提供了一对基础的service/dao。通过接收上一步约定好格式的参数，足以完成绝大部分的数据库操作。  
在实际开发过程中，通常是用BaseController继承tAnylineController(适用于springmvc框架，如果是Struts2框架则继承AnylineAction)  
AnylineController中已经注入AnylineService serive，并重载了大量condition函数。  

或者在Controller中注入AnylineService,AnylineService已通过@Service("anyline.service")的形式注册到Spring上下文中。  
对于更复杂的业务逻辑需要自己实现Serive，同样可以继承AnylineService,AnylineService中也已经注入AnylineDao dao，并重载了大量数据库操作函数。  

Controller层操作过程大致如下:  
```
DataSet set = service.querys("HR_USER(ID,NM)"
    , condition(true,"TYPE_CODE:[type]","NM:nm%","+ZIP_CODE:zip:","++DEPT_CODE:dept","POST_CODE:post:pt:{101}")
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
以SQL根据http参数传递情况动态生成，如果http中没有相关的参数则相应的查询条件不生成。  
IS_PUB值不是来自http  
TYPE_CODE IN(?,?,?)中的占位符数量根据http中type参数数量生成  
如果http中没有提供zip则生成ZIP_CODE IS NULL的查询条件  
如果http中没有提供dept则整个SQL将不执行，当前querys函数返回长度为0的DataSet  
如果http中没有提供post，则取根据pt取值，如果没有提供pt，则使用默认值101,其中的{}表示常量，而不是http中的参数key  
注意IS_PUB的值是一个固定值1，也不是通过http参数中取值，所以不需要放在condition函数中转换  

 **- 标准数据结构与计算函数** 

AnyLine默认提供DataRow (类似于Map)与DataSet(类似于Collection&lt;datarow&gt;）两种数据结构用来实现MVC各层之间的数据传输，  
DataRow,DataSet一般是通过http参数直接构造，或AnylineSevice.query()函数返回，或通过json,xml等数据格式构造，也可以new实例后执行put,add等赋值操作。  
AnyLine支持但不推荐实体Bean的形式  
因为DataRow/DataSet上可以统一附加开发中常用的计算函数，但实体Bean不方便，如：  
排序，求和，截取，清除空值，按列去重，最大最小值，交集合集差集，分组，行列转换，类SQL筛选(like,eq,in,less,between...)，JSON,XML格式转换  

 **- 附加功能** 

针对view层提供了大量的JSP自定义标签用来简化前端操作。  
如&lt;al:navi/&gt;用来配合后台的condition函数快速实现ajax分页查询  
&lt;al:select/&gt;&lt;al:checkbox/&gt;用来根据DataSet数据源生成&lt;select&gt;/&lt;input type="checkbox"&gt;标签，并根据条件设置默认选中值。  
其他如 nvl,evl,date,escape,text,set,if,else等  
以及对支付工具，阿里云，微信，QQ，高德，极光，环信等第三方平台或插件的集成。  
同时提供了常用的图片，HTTP，FTP，加密解密，签名验签，压缩，File，Date，正则，反射等常用工具  


```
anyline-core           : 基础包
anyline-web            : javaEE基础
anyline-net            : 多任务下载，下载进度，断点续传，网速计算，耗时预算，下载回调	
anyline-struts         : strtus2支持
anyline-springmvc      : springmvc支持
anyline-jdbc		: jdbc基础
anyline-jdbc-mysql     : mysql支持
anyline-jdbc-mssql     : sql server支持
anyline-jdbc-db2       : db2支持
anyline-jdbc-oracle    : oracle支持
anyline-jdbc-postgresql: PostgreSQL支持
anyline-jdbc-hgdb      :瀚高数据库支持
anyline-alipay         : 支付宝支付集成工具
anyline-aliyun         : 阿里云平台相关工具
anyline-weixin         : 微信支付,微信开放平台，公众平台集成工具
anyline-qq             : QQ支付，QQ开放平台，公众平台集成工具
anyline-jpush          : 极光推送集成工具
anyline-easemob        : 环信集成工具
anyline-amap           : 高德云图集成工具
anyline-sms            : 基于阿里云的短信发送工具
anyline-mail           : 邮件接收发送
anyline-video          : 视频处理
anyline-ext            : 水印,动画等扩展功能
anyline-redis          : redis
anyline-nc	       : netcdf解析
anyline-poi	       : 基于poi的msoffice文件操作
```
