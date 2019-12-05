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
![输入图片说明](https://images.gitee.com/uploads/images/2019/1205/131120_f326a5f3_580913.png "fmt.png")

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

