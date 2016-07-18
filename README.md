AnyLine只是间于JS,JSP,Controller,Service,Cache,DB之间的辅助工具，以数据操作与传输显示为核心,目的是为了提高开发效率 统一编码规则与结构 降低开发难度与技术要求.在使用AnyLine之前我们仍然需要熟悉JS,SpringMVC/Strut2,SQL.AnyLine只是帮助我们更充分的发挥现有工具的优势，是辅助而非替代。

我们为什么要开发Anyline

相对于需求，设计，测试，实施，市场，运营....开发往往容易被轻视，

而从实用角度出发，需求，设计都是为了更好的指导开发。开发也是后期实施或运营的直接基础。

在开发环节中，普通的编码工作更容易被轻视。

虽然架构很重要，性能很重要，稳定很重要，扩展很重要。

但这几部分通常都是由核心程序员，甚至是架构师亲自动手来完成的，

要保证这几部分的质量，也不是很困难。(别拿没有架构师和核心程序员的团队来比)

而90%以上的编码工作是由普通的开发人员来实现的。这部分开发人员的不但流动率高，

而且出于成本方面的考虑，这部分开发人员的技术水平也无法保证。

既没有对日本包般的标准流程可依，也没有稳定的技术团队可用。

有的是只会作练习，只会print hello word一样的新手。

如果不能将这90%的编码工作，作到如hello word一样简单，我们如何敢想像开发出来的产品？

架构可以调整，性能可以优化，必要时也可以重构。

但任由这90%的代码野草般的疯长，到了后期要负出多少倍的代价才能收拾得了。

在收拾了几次这种烂摊子之后，我们写了Anyline。

首先我们将普通的编码压缩90%左右。

同时保证编码方式风格简单一致。

在这两个目标之上，我们让普通开发人员的工作如hello word一样简单。


AnyLine支持者来自开发，架构设计，测试，需求，实施等不同岗位，涉及到生产制造,交通,金融等众多行业.

我们追求的第一目标： 简单速度

你将走的开发之路，我们已经踏平;你所需要的方法，我们已重复过千百遍.

1.你可以用AnyLine来实现快速开发。

如果你缺少技术架构师

如果你的团队开发经验不足，甚至多数是实习生

如果你不想用Hibernat又没把握用好JDBC

如果你对正在使用Strut2 Spring之类的框架并不精通


如果你想基于以上团队保证软件质量，开发速度

如果你同时又想节约开发成本与维护成本

AnyLine是你不二的选择

保证一群实习生快速完成合格的ERP模块,正是AnyLine问世的背景。

2.深入源码与我们一起作一个实现第1条的人

欢迎各路小伙伴们一展所长

与我们一起将MVC/IOC/AOP/SOA...发挥到极致

让我们精致的设计思想，为更多的人提供帮助.也一起接受，不同行业，各类岗位的建议和挑剔.


为什么要简单一致

因为简单，所以我们的团队成员有JAVA/HTML/SQL基础就可以

因为千篇一律的一致，所以开发维护与升级都简单

为什么有了Struts Spring MyBatis JSTL 还要有Anyline

首先这些不是给普通开发人员准备的，开发人员只用到其中很少一部分

其次普通的开发人员有没能力合格的驾驭这些框架。

而AnyLine的一个重要使命，正是帮助普通的开发团队将这些框架的优势充分发挥


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