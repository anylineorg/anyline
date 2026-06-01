# 数据源操作

## 概述
本文档涵盖通过 Anyline MDM注册、切换、注销、验证数据源相关的操作，注册和注销方法由 DataSourceHolder 统一提供，注册时不需要关心是JDBC数据源还是MongoDB数据源，DataSourceHolder内部会识别
切换由ServiceProxy.service("数据源key")方法提供，实际是返回了一个操作指定数据源的AnylinService实例  

## 1 数据源注册
### 1.1 配置文件
静态的数据源可以在配置文件中提前配置好  
参考连接池配置参数，不同版本的参数会有所区别，准确参数参考连接池的类属性(就是打开项目中依赖的连接池类的源码看看属性，如HikariDataSource、DruidDataSource)
这里用application.properties配置，yml类似
```yaml
#一般先有一个默认数据源(并不是主从关系中的主)
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://192.168.220.100:3306/simple?useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.user-name=root
spring.datasource.password=root

#这里先声明一下都有哪些数据源(用anyline或spring前缀都可以,用anyline在IDEA中会有自动补齐的提示)
anyline.datasource.list=crm,erp
#或
anyline.datasource-list=crm,erp

#为每个数据源设置属性
anyline(或spring).datasource.crm.driver-class-name=com.mysql.cj.jdbc.Driver
anyline(或spring).datasource.crm.url=jdbc:mysql://192.168.220.100:3306/simple_crm?useUnicode=true&characterEncoding=UTF8&useSSL=false
...其他属性

anyline.datasource.erp.driver-class-name=com.mysql.cj.jdbc.Driver
anyline.datasource.erp.url=jdbc:mysql://192.168.220.100:3306/simple_erp?useUnicode=true&characterEncoding=UTF8&useSSL=false
...其他属性
```
### 1.2 在Java中设置连接参数、由spring等环境创建实例  
如在运行过程中由用户动态添加数据源
```java
// 这种方式只在写Hello World时用一下看看效果，因为实际项目中连接池的参数会比较多
String url = "jdbc:mysql://192.168.220.100:3306/simple_crm?useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
DataSourceHolder.reg("crm", "com.zaxxer.hikari.HikariDataSource", "com.mysql.cj.jdbc.Driver", url, "root", "root");

//生产环境一定要这样，把属性放在Map中注册,因为默认的连接数可能不够用以及更多的微调参数需要设置 
Map<String,?> params = new HashMap();
params.put("driver-class","com.mysql.cj.jdbc.Driver"); //有些示例代码中没有设置driver是因为一些常用的数据库可以识别driver,最好还是写上
params.put("userNmae","root"); //各种格式都可以 如:user-name
...其他属性
DataSourceHolder.reg("crm", params); 
```
### 1.3 在Java中创建实例交给spring等环境管理  
如druid的参数比较多与srping默认数据源的属性差别比较大，就不用挨个确认属性名与类型了，直接在Java中调用setter方法设置
```java
DataSource ds = DataSourceUtil.build("com.zaxxer.hikari.HikariDataSource", "com.mysql.cj.jdbc.Driver", url, "root", "root");
//或者直接new一个
DruidDataSource ds2= new DruidDataSource(); //这里不像上面那样设置连接池class,直接创建实例
ds2.setUrl(url);
ds2.setDriverClassName("com.mysql.cj.jdbc.Driver");
ds2.setUsername("root"); ds2.setPassword("root");
DataSourceHolder.reg("ds2", ds2);
ServiceProxy.service("ds2").select("crm_customer");
```
### 1.4 临时数据源
有些场景中只是临时用一次，用完之后很长时间空闲，  
如果按上面的情况注册数据源，用完之后还需要RuntimeHolder.destroy(key)注销数据源及一系列相关的bean  
所以可以临时注册一个用完就不用管了GC会自动回收，参考【临时数据源操作】  
需要注意临时数据源不支持事务,多线程环境引发冲突  
频繁切换的情况下不推荐这种方式，因为临时数据源每个类型同时只存在一个，注册第二个时会把第一个覆盖
```java
//一、根据DataSource创建
String url = "jdbc:mysql://localhost:13306/simple?useUnicode=true&characterEncoding=UTF8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true";
DruidDataSource ds = new DruidDataSource();
ds.setUrl(url);
ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
ds.setUsername("root");
ds.setPassword("root");
AnylineService service = ServiceProxy.temporary(ds);
LinkedHashMap<String, Table> tables = service.metadata().tables();
for(String key:tables.keySet()){
    System.out.println(key);
}
 
DataSource ds1 = DataSourceUtil.build("com.zaxxer.hikari.HikariDataSource", "com.mysql.cj.jdbc.Driver", url, "root", "root");
service = ServiceProxy.temporary(ds1);
tables = service.metadata().tables();
for(String key:tables.keySet()){
   ystem.out.println(key);
}
//二、根据JdbcTemplate创建,对于spring项目一般都有一个JdbcTemplate
service = ServiceProxy.temporary(template);
 
//事务控制，运行时动态创建的数据源不支持注解方式
DataSourceTransactionManager dstm = new DataSourceTransactionManager(ds1);
DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
// 定义事务传播方式 以及 其他参数都在definition中设置
definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
TransactionStatus status = dstm.getTransaction(definition);
//service.insert();
dstm.commit(status);
```
### 1.5 虚拟数据源
有些场景中并没有实际可用的数据源，如需要根据本的MySQL生成PG脚本，但并没有可用的PG环境(几百种数据库不可能都安装一遍)  
这时可以直接根据数据库类型创建一个操作这种数据库的service,用这个service执行相关的命令返回针对这个数据库的SQL  
需要注意虚拟环境下 检测不到表是否已存在，所以在save(Table)时会直接生成create table而不是alter table  
```java
AnylineService service = ServiceProxy.service(DatabaseType.MYSQL);
ConfigStore configs = new DefaultConfigStore();
service.selects("crm_user", configs);
List<Run> runs = configs.runs();
for(Run run:runs){
    System.out.println(run.getFinalQuery());
}
Table table = new Table("crm_user");
table.addColumn("ID", "INT");
service.ddl().create(table);
List<String> ddls = table.ddls();
for (String ddl:ddls){
    System.out.println(ddl);
}
```

### 1.6 复制数据源
如果有多个数据库，可以批量复制(帐号需要操作这些数据库的权限)  
```java
//如在同一个数据库实例上有多个数据库(mysql中就是多个schema)
//在已经有一个sso数据源的情况下，可以针对每个数据库复制出一个数据源，数据源的key=数据库名称
List<String> list = DatasourceHolder.copy("sso");
```

### 1.7 集成第三方的数据源
anyline启动时会加载spring上下文中已经注册好的数据源。但是如果数据源是基于DynamicDataSource实现的可切换数据源，会有问题。需要实现一个DataSourceMonitor 来监听数据源的切换，也要不用anyline的方式切换数据源，对anyline来说只有一个数据源  

非DynamicDataSource或者自己另外注册数据源 就不用管这些了  

默认情况下anyline中的一个数据源 只会绑定一个DriverAdapter(用来生成一类数据库方言)  
但有第三方数据源会通过DynamicDataSource实现数据源切换，这样就会造成一个数据源对应多种数据库如(mysql, oracle)  
而adapter只会检测一次，这样就会造成如果第一次操作的是mysql那么这个数据源就会绑定MySQLAdapter ，下一次操作oracle时就不检测了直接使用MySQLAdapter  
为了处理这个问题可以通过ConfigTable.KEEP_ADAPTER来设置adapter检测方式  
KEEP_ADAPTER取值范围:  
1: 全部保持   
0: 全部不保持 每次操作数据库都检测一次  
2: 由DataSourceMonitor接口实现  
如果项目中出现 一个数据源对应多类数据库的情况, 同时出现 一个数据源只对应一类数据库的情况 这时才需要设置为2或0    

通常设置成0就可以了，每次都检测，这个过程很快，1毫秒内就可以完成  
也可以设置成2 通过DataSourceMonitor接口实现缓存  
```java
package org.anyline.simple.datasource;
 
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.datasource.DataSourceMonitor;
import org.anyline.data.runtime.DataRuntime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Component;
 
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
 
@Component
public class MyBatisDataSourceMonitor implements DataSourceMonitor {
    private Map<String, String> features = new HashMap<>();
    private Map<String, DriverAdapter> adapters = new Hashtable<>();
 
    /**
     * 数据源特征 用来定准 adapter 包含数据库或JDBC协议关键字<br/>
     * 一般会通过 产品名_url 合成 如果返回null 上层方法会通过driver_产品名_url合成
     * @param datasource 数据源
     * @return String 返回null由上层自动提取
     */
    @Override
    public String feature(Object datasource) {
        String feature = null;
        if(datasource instanceof JdbcTemplate){
            JdbcTemplate jdbc = (JdbcTemplate)datasource;
            DataSource ds = jdbc.getDataSource();
            if(ds instanceof DynamicRoutingDataSource){
                String key = DynamicDataSourceContextHolder.peek();
                feature = features.get(key);
                if(null == feature){
                    Connection con = null;
                    try {
                        con = DataSourceUtils.getConnection(ds);
                        DatabaseMetaData meta = con.getMetaData();
                        String url = meta.getURL();
                        feature = meta.getDatabaseProductName().toLowerCase().replace(" ","") + "_" + url;
                        features.put(key, feature);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (null != con && !DataSourceUtils.isConnectionTransactional(con, ds)) {
                            DataSourceUtils.releaseConnection(con, ds);
                        }
                    }
                }
            }
        }
        return feature;
    }
 
    /**
     * ConfigTable.KEEP_ADAPTER=2 : 根据当前接口判断是否保持同一个数据源绑定同一个adapter<br/>
     * DynamicRoutingDataSource类型的返回false,因为同一个DynamicRoutingDataSource可能对应多类数据库, 如果项目中只有一种数据库 应该直接返回true
     * @param datasource 数据源
     * @return boolean
     */
    @Override
    public boolean keepAdapter(Object datasource) {
        if(datasource instanceof JdbcTemplate){
            JdbcTemplate jdbc = (JdbcTemplate)datasource;
            DataSource ds = jdbc.getDataSource();
            if(ds instanceof DynamicRoutingDataSource){
                return false;
            }
        }
        return true;
    }
/* ********************************************************************************************************************
*
*
*                   下面是为了缓存adapter 实际测试发现 定位一次基本会在1毫秒内完成  缓存没什么太大意义 可以不实现
*
*
 ******************************************************************************************************************** */
 
    /**
     * 如果有根据feature识别不了的情况，可以在这里实现，如果这一步返回了adapter则以这一步为准
     * @param datasource 数据源
     * @return DriverAdapter
     */
    @Override
    public DriverAdapter adapter(Object datasource) {
        DriverAdapter adapter = null;
        if(datasource instanceof JdbcTemplate){
            JdbcTemplate jdbc = (JdbcTemplate)datasource;
            DataSource ds = jdbc.getDataSource();
            if(ds instanceof DynamicRoutingDataSource){
                String key = DynamicDataSourceContextHolder.peek();
                adapter = adapters.get(key);
            }
        }
        return adapter;
    }
 
    /**
     * 上层方法完成adapter定位后调用,可以在这里缓存,下一次定位提供给adapter(Object datasource)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param datasource 数据源
     * @param adapter DriverAdapter
     * @return 如果没有问题原样返回，如果有问题可以修正或返回null, 如果返回null上层方法会抛出adapter定位失败的异常
     */
    @Override
    public DriverAdapter after(DataRuntime runtime, Object datasource, DriverAdapter adapter) {
        if(datasource instanceof JdbcTemplate){
            JdbcTemplate jdbc = (JdbcTemplate)datasource;
            DataSource ds = jdbc.getDataSource();
            if(ds instanceof DynamicRoutingDataSource){
                String key = DynamicDataSourceContextHolder.peek();
                adapters.put(key, adapter);
            }
        }
        return adapter;
    }
}
```

## 2 数据源切换
8.7.2之后就没有切换数据源了，实际上是切换的整个运行时环境，service,adapter,jdbc等一整套的全切换  
### 2.1 切换service
通过ServiceProxy返回数据源对应的service
```java
AnylineService crmService = ServiceProxy.service("crm"); //返回crm数据源对应的service
AnylineService defService = ServiceProxy.service() //返回默认默认数据源对应的service
再通过返回的crmService操作crm数据库
通过defService操作默认的simple数据库
```
### 2.2 临时切换
有些场景中，只需要从另一个数据源临时读取一部分数据，大部分操作还在主数据库中
```java
service.select("<crm>USER"); //在表名前添加数据源名称
//这种方式切换只针对当前查询有效, 查询完成后会切换回原来的数据源(不一定是默认数据源)
```
### 2.3 DataSourceHolder 切换
DataSourceHolder.setDataSource("crm")切换数据源,再调用service.select等方法操作相应的数据库
切换完成后,数据源会一直操持在crm
如果线程共享的话，不要用这种方式，（如springmvc会默认开启线程池）
注意：这个方式在8.7.2及之后的版本中取消了，原因参考【[为什么取消了DynamicDataSource/ThreadLocal切换数据源的方式](http://doc.anyline.org/aa/d5_3883)】
## 3 数据源验证
数据源注册完成后，并没有执行连接，可以通过以下方式确认连接可用性
### 3.1 validity
返回数据源是否可用，即使不可用也不会抛出异常
```java
boolean result = DataSourceHolder.validity("数据源key");
```
### 3.2 hit
返回数据源是否可用，如果数据源不可用，会抛出异常
```java
boolean result = DataSourceHolder.hit("数据源key");
```
### 3.3 service.validity
service本身也提供了验证当前数据源是否可用的方式
```java
boolean result = service.validity("数据源key");
```

## 4 数据源注销
```java
DataSourceHolder.destroy("数据源key");
```

## 5 异构数据源数据迁移
异构数据源迁移，大概过程就3步，其他细节就看具体情况了  
1.在目标库创建表结构  
2.从源库中查出数据  
3.插入到目标库  
以下是从Mysql到Apache Ignite的示例  
```java
//复制mysql表结构到ignite
public static void init(String table) throws Exception{
    //检测一下ignite中有没有这个表
    Table tab = ServiceProxy.service("ignite").metadata().table(table, false);
    if(null != tab){
        ServiceProxy.service("ignite").ddl().drop(tab);
    }
    //获取mysql表结构
    tab = ServiceProxy.service("data").metadata().table(table, true);
    //根据情况修改一下库名模式名，以及一些默认值约束等,能满足业务需求即可，再讲究一点的话把不需要的列都删除(修改完后注册查询删除时需要明确指定一下列名)
    tab.setSchema("PUBLIC");
    tab.getColumn("REG_TIME").setDefaultValue(null);
    tab.getColumn("UPT_TIME").setDefaultValue(null);
    //创建ignite表
    ServiceProxy.service("ignite").ddl().create(tab);
}
//复制mysql数据到ignite
public static void load(String table){
    Long max = 0L;
    //检测最后一条数据
    DataRow last = ServiceProxy.service("ignite").select(table, "ORDER BY ID DESC");
    if(null != last){
        max = last.getLong("ID", 0);
    }
    while (true){
        //从mysql中查出数据 这里不要用DataSet 太慢
        List<Map> list = ServiceProxy.service("data").maps(table,0,999, "ID>"+max);
        if(list.isEmpty()){
            break;
        }
        //插入到ignite 一般需要修改一下默认日志(不显示日志)要不然日志太多
        //override表示重复数据不覆盖(忽略|跳过)
        ConfigStore configs = new DefaultConfigStore().IS_LOG_SQL(false).IS_LOG_SQL_PARAM(false).override(false);
        ServiceProxy.service("ignite").insert(table, list, configs );
        max = BasicUtil.parseLong(list.get(list.size()-1).get("ID"), 0L);
    }
}
```