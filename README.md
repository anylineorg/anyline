8.3.8之前提交历史记录查看[https://github.com/anylineorg/anyline-history.git](https://github.com/anylineorg/anyline-history.git)

***快速开始请参考示例代码:***  
[https://gitee.com/anyline/anyline-simple.git](https://gitee.com/anyline/anyline-simple.git)


AnyLine的核心是一个基于spring-jdbc的快捷数据库操作工具,摒弃了各种繁琐呆板的Service/Dao/Bean/*O/Mapper  
简化了数据库以及结果集的操作.   
适合于抽象设计阶段，常用于数据处理、报表输出、低代码后台、可视化数据源、运行时自定义查询条件及数据结构等需要动态结构的场景

操作数据库 就简简单单 操作数据库，***不要***再从生成xml/dao/service以及各种O各种配置开始  
默认的service已经提供了大部分的数据库操作功能。
操作过程大致如下:
```
DataSet set = service.querys("HR_USER(ID,NM)", condition(true,"anyline根据约定自动生成的查询条件"));  
```
这里的查询条件不再需要各种标签配置,各种if else foreach标签  
Anyline会自动生成,生成规则可以参考这里[规则](https://gitee.com/anyline/anyline-simple.git)  
分页不需要另外的插件，更不需要繁琐的计算和配置，指定true或false即可
繁琐机械的工作不要浪费程序员的时间  

返回的DataSet已提供了常用的数据操作函数如:排序,求和,截取,清除空值,按列去重,最大最小值,交集合集差集,分组,
行列转换,类SQL筛选(like,eq,in,less,between...),JSON,XML格式转换等

如果实现放不下那些已存在的各种OOO  
DataSet与Bean之间可以相互转换 
或者这样:  
```
List<User> = service.querys(User.class, condition(true,"anyline根据约定自动生成的查询条件"));  
```
如何集成:  
需要为anyline提供一个org.springframework.jdbc.core.JdbcTemplate;  
如果是springboot项目一般是在pom里添加一个spring-boot-starter-jdbc的依赖  
如果不是springboot项目一般是在配置文件中配置一个JdbcTemplate  
如果你的项目中已经配置过数据源了，那以上就可以忽略了。  

在需要操作数据库的地方注入AnylineService  
```
@Qualifier("anyline.service")
protected AnylineService service;
```
接下来service就可以完成大部分的数据库操作了。常用示例可以参考[示例代码](https://gitee.com/anyline/anyline-simple.git)