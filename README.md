# Anyline

**Anyline**是基于Spring对SpringMVC,Struts2等web框架的扩展<br/>
以及对阿里云,微信,QQ,高德,极光,环信等第三方平台或插件的集成<br/>
主要功能集中在对数据及缓存的操作上<br/>
并提供了常用的图片,http,ftp,加密解密,File,Date,正则,反射等常用工具和大量的jsp标签<br/>
<br/>
##### 针对人群:<br/>
●架构师,技术经理<br/>
●没有构架师和技术经理提供技术支持的项目经理<br/>
●没有构架师和技术经理提供技术支持的主程<br/>
●对底层框架有兴趣的程序员<br/>
<br/>
anyline不涉及具体业务逻辑,只是将前后端主流技术的融合与扩展,<br/>
并在此基础上统一和简化了前后端操作.<br/>
让开发人员将主要精力集中在业务逻辑的实现上.<br/>
<br/>
##### 注: 
任何框架都是把多仞的剑,在保证软件质量,性能,稳定,扩展的同时,也把程序员往码工的方向更推进了一步.<br/>
所以,如果选择了技术方向,不要仅限于框架和工具的应用.<br/>
参与进去,一起开发.anyline提供的不仅仅是一个工具,更重要的是编程思想.
```
anyline_core        : 基础包
anyline_web         : javaEE基础
anyline_struts      : strtus2支持
anyline_springmvc   : springmvc支持
anyline_mysql       : mysql支持
anyline_mssql       : sql server支持
anyline_alipay      : 支付宝支付集成工具
anyline_weixin      : 微信支付集成工具
anyline_jpush       : 极光推送集成工具
anyline_easemob     : 环信集成工具
anyline_amap        : 高德云图集成工具
anyline_sms         : 基于阿里云的短信发送工具
```
一行代码自我介绍
```
HelloWord:
DataSet set = service.query("member", parseConfig(true,"AGE:age","NAME:name%","DEPT:[dept]"));


方法说明:以分页方式 查询 年龄=20 并且 姓名以'张'开头的用户
对应的URL参数: http://localhost/test?age=20&name=张&dept=1&dept=2
最终执行的SQL:
SELEC 
    * 
FROM MEMBER 
WHERE 
    AGE=20 
    AND NAME LIKE '张%' 
    AND DEPT IN(1,2)
limit 0, 10
MEMBER:需要查询的表
parseConfig:收集http传入的参数
true:是否需要分页(默认false)
AGE:age
    AGE:对应表中的列 
    age:对应url参数名 
    默认当(null != age值)时,最终会拼成SQL查询条件 WHERE AGE= ?
```
更多方法请看API或源码或QQ群
<br/>
<a target="_blank" href="//shang.qq.com/wpa/qunwpa?idkey=279fe968c371670fa9791a9ff8686f86dbac0b5edba8021a660b313e2dd863ad"><img border="0" src="//pub.idqqimg.com/wpa/images/group.png" alt="AnyLine" title="AnyLine"></a>