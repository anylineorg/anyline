
提高开发速度 保证产品质量 保持代码一致性 最终控制开发与维护成本
以数据库操作为核心 以WEB开发为背景 追求极致的开发效率

站在实战架构师的角度，告诉你WEB系统应该怎么架构，如何作到质量与效率并重。

Spring Struts MyBatis 等以及IOC MVC AOP等在实现开发中应该怎么用

我们期望的理想状态是:

通过统一的标准，完善的底层来保证开发人员的代码如同出自一人之手．如流水作业般的整齐．

开发人员不再关心技术问题，将80%精力集中在与需求，实施部门的沟通上.
基于anyline可以写出如架构师相同水平的代码.让开发工作如Hello Word一样简单.
让开发人员只作参数对应的工作，其余的一切交给强大的架构师团队事先处理。
这里虽然也有MVC　IOC　AOP　S OA，但是开发人员感觉不到．

anyline的优势在于SQL处理特别是查询条件的处理

以及返回数据与前端(JSP,ajax)的无缝对接

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