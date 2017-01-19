AnyLine只是间于JS,JSP,Controller,Service,Cache,DB之间的辅助工具，
以数据操作与传输显示为核心,目的是为了提高开发效率 统一编码规则与结构 降低开发难度与技术要求.
在使用AnyLine之前我们仍然需要熟悉JS,SpringMVC/Strut2,SQL.
AnyLine只是帮助我们更充分的发挥现有工具的优势，是辅助而非替代。

anyline_core:基础包
anyline_web:javaEE基础
anyline_struts:strtus2支持
anyline_springmvc:springmvc支持
anyline_mysql:mysql支持
anyline_struts:sql server支持
anyline_alipay:支付宝支付集成工具
anyline_weixin:微信支付集成工具
anyline_jpush:极光推送集成工具
anyline_easemob:环信集成工具
anyline_amap:高德云图集成工具

一行代码自我介绍
```


HelloWord:
DataSet set = service.query("member", parseConfig(true,"AGE:age","NAME:name%"));


方法说明:以分页方式 查询 年龄=20 并且 姓名以'张'开头的用户
对应的URL参数: http://localhost/test?age=20&name=张 
最终执行的SQL:SELEC * FROM MEMBER WHERE AGE=20 AND NAME LIKE '张%' limit 0, 10
MEMBER:需要查询的表
parseConfig:收集http传入的参数
true:是否需要分页(默认false)
AGE:age
	AGE:对应表中的列 
	age:对应url参数名 
	默认当(null != age值)时,最终会拼成SQL查询条件 WHERE AGE= ?
类似的查询:
/**
 * 按条件查询
 * @param src           数据源(表或自定义SQL或SELECT语句)
 * @param configs       封装来自于http的查询条件
 * @param conditions    固定查询条件
 * @return
 */
public DataSet query(DataSource ds, String src, ConfigStore configs, String ... conditions);
public DataSet query(String src, int fr, int to, String ... conditions);
publicListquery(Class clazz, ConfigStore configs, String ... conditions);


service.query("hr.MEMBER(count(0) AS CNT)");	//直接查表或视图
service.query("SELECT * FROM hr.MEMBER");	//原生SQL
service.query("oc.web.hr:ALL_LEAVE_MEMBER");	//自定义SQL 像mybatis那种方式
```
