### 简单快速,整洁精致的javaWeb基础框架


### 我们追求的第一目标： 简单速度

### 1.你可以用AnyLine来实现快速开发。

```

如果你缺少技术架构师
如果你的团队开发经验不足，甚至多数是实习生
如果你不想用Hibernat又没把握用好JDBC

如果你想基于以上团队保证软件质量，开发速度
如果你同时又想节约开发成本与维护成本

AnyLine是你不二的选择
保证一群实习生快速完成合格的ERP模块,正是AnyLine问世的背景。
```

### 2.深入源码与我们一起作一个实现第1条的人

```

欢迎各路小伙伴们一展所长
与我们一起将MVC/IOC/AOP/SOA...发挥到极致
```

```

为什么要简单一致?
因为简单，所以我们的团队成员有JAVA/HTML/SQL基础就可以
因为千篇一律的一致，所以开发维护与升级都简单

选择了AnyLine之后，你需要作的是集中精力来把业务分解和抽象到简单一致
```


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

这是jar包的源码，demo请下载https://git.oschina.net/anyline/anyline_demo

分别基于springmv和struts2实现的 增删改查