8.3.8之前提交历史记录查看[https://github.com/anylineorg/anyline-history.git](https://github.com/anylineorg/anyline-history.git)  

***快速开始请参考示例代码:***  
[https://gitee.com/anyline/anyline-simple.git](https://gitee.com/anyline/anyline-simple.git)  


AnyLine的核心是一个基于spring-jdbc的快捷数据库操作工具,摒弃了各种繁琐呆板的Service/Dao/Bean/*O/Mapper  
简化数据库及针对结果集的操作.   
适合于抽象设计阶段，常用于数据交换、报表输出等大量数据操作的场景  

操作数据不需要从生成xml/dao/service以及各种O开始,默认的service已经提供了大部分的数据库操作功能。
操作过程大致如下:  
```
DataSet set = service.querys("HR_USER(ID,NM)", condition(true,"anyline根据约定自动生成的查询条件"));
```
接收到的DataSet可以继续实现业务逻辑  
DataSet也提供了常用的数据操作函数的如:排序,求和,截取,清除空值,按列去重,最大最小值,交集合集差集,分组,行列转换,类SQL筛选(like,eq,in,less,between...),JSON,XML格式转换等 
