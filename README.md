
**Anyline**
是基于Spring对SpringMVC，Struts2等web框架的扩展，<br>
以及对支付工具，阿里云，微信，QQ，高德，极光，环信等第三方平台或插件的集成。<br/>
旨在简化各种框架，插件或工具的操作复杂度，提升整体应用的稳定性，可用性，扩展性。<br>
从而实现在保证代码质量的同时，提高开发速度。<br/>
同时提供了常用的图片，HTTP，FTP，加密解密，签名验签，压缩，File，Date，正则，反射等常用工具和大量的JSP标签<br/>
框架核心在于service对数据库的操作(springjdbc)，以及针对service返回的DataSet，DataRow的一系列配套工具<br>
包括从后台到前台从sql>java>jsp>html/js/ss的完整链路中的数据查询，封装，传递，计算，显示。<br/>
<br/>
主要特性<br/>
1.简化<br/>
尽可能的屏蔽一切与业务无关的技术细节，让开发人员可以专注于业务逻辑。<br/>
如下载或上传文件时，应该只需要指定远程地址及本地地址，而不需要关心输入输出流、断点续传、分片、MD5验证等细节。<br/>
集成一个插件或第三方平台时只需要添加相应的配置文件并像本地方法一样调用第三方API，而不需要关心如果加载更新配置文件、如何签名/验签、加密/解密以及网络协议等细节。<br/>
<br/>
2.增强<br/>
在原框架基础上的扩展和优化，以实现更复杂的功能<br/>
如针对springmvc的视图解析器，模板工具。<br/>
针对springjdbc返回数据增加多级缓存，分页，类SQL的分组，过滤等功能。<br/>
<br/>
简单来说就是把spring之类掌握的更透彻深入，将其优势发挥的更充分，把代码写的更优雅；而不仅仅是只会写一个HelloWord<br/>
AnyLine提供的不仅仅是一个工具，更重要的是编程思想.<br/>
<br/>
为什么不能另起一个炉灶<br/>
首先要认清有没有这个能力。<br/>
写好一个框架一个系统性工程，需要投入海量的精力，优化各种细节，应对千变万化的场景，并不是找到或实现一两个亮点，就能完成一个框架。<br/>
更困难的是需要长期的维护升级，不是一个人或单个团队所能胜任。<br/>
一个框架的生存与发展更离不开一个完整活跃的生态，强如struts，webwork，struts2，jsf也已经被淘汰。<br/>
spring系统也是从支持struts,hibernate等框架的集成开始，在聚集了大量用户，形成相对完善的生态系统后，才慢慢推出了其他框架的替代产品。<br/>
虽然我们不支持重造轮子，但我们可以强化轮子，作为一个程序员一定要深入轮子内部，了解其底层设计与实现过程。<br/>
##### 针对人群:<br/>
●架构师，技术经理<br/>
●没有构架师和技术经理提供技术支持的项目经理<br/>
●没有构架师和技术经理提供技术支持的主程<br/>
●对底层框架有兴趣的程序员<br/>
<br/>
**[AnyBoot](https://gitee.com/anyline/anyboot)**
继承于AnyLine并针对SpringBoot/SpringCloud环境作了进一步优化。<br/>
SpringBoot/SpringCloud将各种优秀框架整合在一起，AnyBoot在此基础上提供了一系列的增强和简化的封装。<br/>

<br/>
```
anyline-core        : 基础包
anyline-web         : javaEE基础
anyline-struts      : strtus2支持
anyline-springmvc   : springmvc支持
anyline-mysql       : mysql支持
anyline-mssql       : sql server支持
anyline-alipay      : 支付宝支付集成工具
anyline-aliyun      : 阿里云平台相关工具
anyline-weixin      : 微信支付,微信开放平台，公众平台集成工具
anyline-qq          : QQ支付，QQ开放平台，公众平台集成工具
anyline-jpush       : 极光推送集成工具
anyline-easemob     : 环信集成工具
anyline-amap        : 高德云图集成工具
anyline-sms         : 基于阿里云的短信发送工具
anyline-mail        : 邮件
anyline-redis       : redis
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
    AND DEPT IN(1，2)
limit 0， 10
MEMBER:需要查询的表
parseConfig:收集http传入的参数
true:是否需要分页(默认false)
AGE:age
    AGE:对应表中的列 
    age:对应url参数名 
    默认当(null != age值)时，最终会拼成SQL查询条件 WHERE AGE= ?
```
更多方法请看**[API文档](http://doc.anyline.org)**或源码或QQ群
<br/>
<a target="_blank" href="//shang.qq.com/wpa/qunwpa?idkey=279fe968c371670fa9791a9ff8686f86dbac0b5edba8021a660b313e2dd863ad"><img border="0" src="//pub.idqqimg.com/wpa/images/group.png" alt="AnyLine" title="AnyLine"></a>