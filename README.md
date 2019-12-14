 代码已迁至[https://github.com/anylineorg/anyline](http://https://github.com/anylineorg/anyline)
 
 **AnyLine**  是一个针对web环境的，基于spring-jdbc的，快捷数据库操作工具。

其核心是  
   1、提供了一个约定格式的函数，用来实现http参数到jdbc参数的转换。  
   2、提供了一对基础的service和dao，用来实现常规的数据库操作。  
   3、提供了一对标准的数据结构(DataSet/DataRow)，用来实现MVC各层之间的数据传输，  
    并在其上附加了开发常用的计算函数(如求和、分组、交集、筛选，行转列等)。  

 **- http参数到jdbc参数的转换** 

在实际开发中，业务开发人员经常需要大量的时间，不厌其烦的从http中提取参数，判断验证，生成jdbc要求格式的参数，  
再把参数依次传到service、dao，最后返回一个实现bean。这整个过程中经常有各种小细节容易忽略而导致异常，如空值处理，IN条件生成等。  
而在整个项目中这些过程又是大量重复或类似的。这不但影响开发速度与代码质量，更影响心情。  
所以AnyLine提供了一个统一约定格式来实现这些繁琐的操作,格式大致如下 

序号|约定格式|最终SQL(实际SQL根据数据库类型略有不同)|备注
| --- | --- | --- | --- |
1|CODE:cd|CODE = ?(cd没有值，则不生成)|如果http中有cd或cd值为空，则生成以上SQL,否则不生成SQL,其他格式同样适用(+开头除外)
2|CODE:%id|CODE LIKE '%?'|
3|CODE:%id%|CODE LIKE '%?%'|
4|CODE:[id]|CODE IN(?,?,?,?,?,?,?,?)|占位符数量根据http参数数量生成
5|CODE: cd: code|CODE = ?|如果http参数中有cd或cd值不为空,则根据cd取值，否则根据code取值,如果都没有值,则不生成SQL
6|CODE: cd: id: code:{1}|CODE = ?|依次根据cd,id,code从http中取值，如果都没有值，则取默认值1，{}表示其中是value，而不是key
7|CODE:cd\|CODE:code|(CODE = ? OR CODE = ?)|如果cd,code都有值则生成OR条件，如果仅一个有值则只生成CODE=?，如果都没有值则不生成SQL
8|CODE:cd+|CODE = ?|cd+后加号表示http中cd值已加密，在生成SQL时需要先解密
9|+CODE:cd|CODE = ? 或 CODE IS NULL|如果http中没有cd或cd值为空,则生成CODE IS NULL
10|++CODE:cd|CODE = ? 或 整个SQL不执行|如果http中没有cd或cd值为空,整个SQL不执行，如果是查询SQL，则返回长度为0的集合


以上格式仅提供给condition(String)函数用来指定从http中取值的方式，其他形式(如直接提供值，而不是从http中取值)参考AnylineService或AnylineDao  

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
