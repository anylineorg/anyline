package org.anyline.proxy;

import org.anyline.data.entity.*;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Procedure;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.service.AnylineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.service.proxy")
public class ServiceProxy {
    public static AnylineService service;
    public static AnylineService.DDLService ddl;
    public static AnylineService.MetaDataService metadata;
    public ServiceProxy(){}
    @Autowired(required = true)
    @Qualifier("anyline.service")
    public void init(AnylineService service) {
        ServiceProxy.service = service;
        ServiceProxy.ddl = service.ddl();
        ServiceProxy.metadata = service.metadata();
    }

    /**
     * 切换数据源
     * @param datasource 数据源
     */
    public static AnylineService datasource(String datasource){
        DataSourceHolder.setDataSource(datasource);
        return service;
    }
    public static AnylineService setDataSource(String datasource){
        DataSourceHolder.setDataSource(datasource);
        return service;
    }
    public static AnylineService setDataSource(String datasource, boolean auto){
        DataSourceHolder.setDataSource(datasource, auto);
        return service;
    }
    public static AnylineService setDefaultDataSource(){
        DataSourceHolder.setDefaultDataSource();
        return service;
    }
    // 恢复切换前数据源
    public static AnylineService recoverDataSource(){
        DataSourceHolder.recoverDataSource();
        return service;
    }
    public static String getDataSource(){
        return DataSourceHolder.getDataSource();
    }


    /* *****************************************************************************************************************
     *
     * 													DML
     *
     * =================================================================================================================
     * INSERT			: 插入
     * BATCH INSERT		: 批量插入
     * UPDATE			: 更新
     * SAVE				: 根据情况插入或更新
     * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
     * EXISTS			: 是否存在
     * COUNT			: 统计
     * EXECUTE			: 执行(原生SQL及存储过程)
     * DELETE			: 删除
     * CACHE			: 缓存
     * METADATA			: 简单格式元数据，只返回NAME
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     * 													INSERT
     ******************************************************************************************************************/

    public static int insert(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.insert(dest, data, checkPriamry, fixs, columns);
    }
    public static int insert(Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.insert(data, checkPriamry, fixs, columns);
    }
    public static int insert(Object data, List<String> fixs, String ... columns){
        return service.insert(data, fixs, columns);
    }
    public static int insert(String dest, Object data, List<String> fixs, String ... columns){
        return service.insert(dest, data, fixs, columns);
    }

    public static int insert(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.insert(dest, data, checkPriamry, fixs, columns);
    }
    public static int insert(Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.insert(data, checkPriamry, fixs, columns);
    }
    public static int insert(Object data, String[] fixs, String ... columns){
        return service.insert(data, fixs, columns);
    }
    public static int insert(String dest, Object data, String[] fixs, String ... columns){
        return service.insert(dest, data, fixs, columns);
    }

    public static int insert(String dest, Object data, boolean checkPriamry, String ... columns){
        return service.insert(dest, data, checkPriamry, columns);
    }
    public static int insert(Object data, boolean checkPriamry, String ... columns){
        return service.insert(data, checkPriamry, columns);
    }
    public static int insert(Object data, String ... columns){
        return service.insert(data, columns);
    }
    public static int insert(String dest, Object data, String ... columns){
        return service.insert(dest, data, columns);
    }

    /* *****************************************************************************************************************
     * 													BATCH INSERT
     ******************************************************************************************************************/
    /**
     * 异步插入
     * @param dest dest
     * @param data data
     * @param checkPriamry checkPriamry
     * @param fixs 指定更新或保存的列
     * @param columns columns
     * @return int
     */
    public static int batchInsert(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.batchInsert(dest, data, checkPriamry, fixs, columns);
    }
    public static int batchInsert(Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.batchInsert(data, checkPriamry, fixs, columns);
    }
    public static int batchInsert(Object data, List<String> fixs, String ... columns){
        return service.batchInsert(data, fixs, columns);
    }
    public static int batchInsert(String dest, Object data, List<String> fixs, String ... columns){
        return service.batchInsert(dest, data, fixs, columns);
    }

    public static int batchInsert(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.batchInsert(dest, data, checkPriamry, fixs, columns);
    }
    public static int batchInsert(Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.batchInsert(data, checkPriamry, fixs, columns);
    }
    public static int batchInsert(Object data, String[] fixs, String ... columns){
        return service.batchInsert(data, fixs, columns);
    }
    public static int batchInsert(String dest, Object data, String[] fixs, String ... columns){
        return service.batchInsert(dest, data, fixs, columns);
    }

    public static int batchInsert(String dest, Object data, boolean checkPriamry, String ... columns){
        return service.batchInsert(dest, data, checkPriamry, columns);
    }
    public static int batchInsert(Object data, boolean checkPriamry, String ... columns){
        return service.batchInsert(data, checkPriamry, columns);
    }
    public static int batchInsert(Object data, String ... columns){
        return service.batchInsert(data, columns);
    }
    public static int batchInsert(String dest, Object data, String ... columns){
        return service.batchInsert(dest, data, columns);
    }

    /* *****************************************************************************************************************
     * 													UPDATE
     ******************************************************************************************************************/

    /**
     * 更新记录
     * 默认情况下以主键为更新条件，需在更新的数据保存在data中
     * 如果提供了dest则更新dest表，如果没有提供则根据data解析出表名
     * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键，则根据TYPE_CODE更新
     * 可以提供了ConfigStore以实现更复杂的更新条件
     * 需要更新的列通过fixs/columns提供
     * @param fixs	  	需要更新的列
     * @param columns	需要更新的列
     * @param dest	   	表
     * @param data 		更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
     * @param configs 	更新条件
     * @return int 影响行数
     */
    public static int update(String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns){
        return service.update(dest, data, configs, fixs, columns);
    }
    public static int update(String dest, Object data, List<String> fixs, String ... columns){
        return service.update(dest, data, fixs, columns);
    }
    public static int update(String dest, Object data, String[] fixs, String ... columns){
        return service.update(dest, data, fixs, columns);
    }
    public static int update(String dest, Object data, ConfigStore configs, String[] fixs, String ... columns){
        return service.update(dest, data, configs, fixs, columns);
    }
    public static int update(String dest, Object data, String ... columns){
        return service.update(dest, data, columns);
    }
    public static int update(String dest, Object data, ConfigStore configs, String ... columns){
        return service.update(dest, data, configs, columns);
    }

    public static int update(Object data, ConfigStore configs, List<String> fixs, String ... columns){
        return service.update(data, configs, fixs, columns);
    }
    public static int update(Object data, List<String> fixs, String ... columns){
        return service.update(data, fixs, columns);
    }
    public static int update(Object data, String[] fixs, String ... columns){
        return update(data, fixs, columns);
    }
    public static int update(Object data, ConfigStore configs, String[] fixs, String ... columns){
        return service.update(data, configs, fixs, columns);
    }
    public static int update(Object data, String ... columns){
        return service.update(data, columns);
    }
    public static int update(Object data, ConfigStore configs, String ... columns){
        return service.update(data, configs, columns);
    }



    public static int update(boolean async, String dest, Object data, List<String> fixs, String ... columns){
        return service.update(async, dest, data, fixs, columns);
    }
    public static int update(boolean async, String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns){
        return service.update(async, dest, data, configs, fixs, columns);
    }
    public static int update(boolean async, String dest, Object data, String[] fixs, String ... columns){
        return service.update(async, dest, data, fixs, columns);
    }
    public static int update(boolean async, String dest, Object data, ConfigStore configs, String[] fixs, String ... columns){
        return service.update(async, dest, data, configs, fixs, columns);
    }
    public static int update(boolean async, String dest, Object data, String ... columns){
        return service.update(async, dest, data, columns);
    }

    public static int update(boolean async, String dest, Object data, ConfigStore configs, String ... columns){
        return service.update(async, dest, data, configs, columns);
    }

    public static int update(boolean async, Object data, List<String> fixs, String ... columns){
        return service.update(async, data, fixs, columns);
    }
    public static int update(boolean async, Object data, ConfigStore configs, List<String> fixs, String ... columns){
        return service.update(async, data, configs, fixs, columns);
    }
    public static int update(boolean async, Object data, String[] fixs, String ... columns){
        return service.update(async, data, fixs, columns);
    }
    public static int update(boolean async, Object data, ConfigStore configs, String[] fixs, String ... columns){
        return service.update(async, data, configs, fixs, columns);
    }
    public static int update(boolean async, Object data, String ... columns){
        return service.update(async, data, columns);
    }
    public static int update(boolean async, Object data, ConfigStore configs, String ... columns){
        return service.update(async, data, configs, columns);
    }


    /* *****************************************************************************************************************
     * 													SAVE
     ******************************************************************************************************************/


    /**
     * save insert区别
     * 操作单个对象时没有区别
     * 在操作集合时区别:
     * save会循环操作数据库每次都会判断insert|update
     * save 集合中的数据可以是不同的表不同的结构
     * insert 集合中的数据必须保存到相同的表,结构必须相同
     * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
     *
     * 保存(insert|update)根据是否有主键值确定insert或update
     * @param data  数据
     * @param checkPriamry 是否检测主键
     * @param fixs 指定更新或保存的列 一般与columns配合使用,fixs通过常量指定常用的列,columns在调用时临时指定经常是从上一步接收
     * @param columns 指定更新或保存的列
     * @param dest 表
     * @return 影响行数
     */
    public static int save(String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.save(dest, data, checkPriamry, fixs, columns);
    }
    public static int save(Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.save(data, checkPriamry, fixs, columns);
    }
    public static int save(Object data, List<String> fixs, String ... columns){
        return service.save(data, fixs, columns);
    }
    public static int save(String dest, Object data, List<String> fixs, String ... columns){
        return service.save(dest, data, fixs, columns);
    }

    public static int save(String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.save(dest, data, checkPriamry, fixs, columns);
    }
    public static int save(Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.save(data, checkPriamry, fixs, columns);
    }
    public static int save(Object data, String[] fixs, String ... columns){
        return service.save(data, fixs, columns);
    }
    public static int save(String dest, Object data, String[] fixs, String ... columns){
        return service.save(dest, data, fixs, columns);
    }

    public static int save(String dest, Object data, boolean checkPriamry, String ... columns){
        return service.save(dest, data, checkPriamry, columns);
    }
    public static int save(Object data, boolean checkPriamry, String ... columns){
        return service.save(data, checkPriamry, columns);
    }
    public static int save(Object data, String ... columns){
        return service.save(data, columns);
    }
    public static int save(String dest, Object data, String ... columns){
        return service.save(dest, data, columns);
    }


    /**
     * 保存(insert|update)根据是否有主键值确定insert或update
     * @param async 是否异步执行
     * @param data  数据
     * @param checkPriamry 是否检测主键
     * @param fixs 指定更新或保存的列 一般与columns配合使用,fixs通过常量指定常用的列,columns在调用时临时指定经常是从上一步接收
     * @param columns 指定更新或保存的列
     * @param dest 表
     * @return 影响行数
     */
    public static int save(boolean async, String dest, Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.save(async, dest, data, checkPriamry, fixs, columns);
    }
    public static int save(boolean async, Object data, boolean checkPriamry, List<String> fixs, String ... columns){
        return service.save(async, data, checkPriamry, fixs, columns);
    }
    public static int save(boolean async, Object data, List<String> fixs, String ... columns){
        return service.save(async, data, fixs, columns);
    }
    public static int save(boolean async, String dest, Object data, List<String> fixs, String ... columns){
        return service.save(async, dest, data, fixs, columns);
    }

    public static int save(boolean async, String dest, Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.save(async, dest, data, checkPriamry, fixs, columns);
    }
    public static int save(boolean async, Object data, boolean checkPriamry, String[] fixs, String ... columns){
        return service.save(async, data, checkPriamry, fixs, columns);
    }
    public static int save(boolean async, Object data, String[] fixs, String ... columns){
        return service.save(async, data, fixs, columns);
    }
    public static int save(boolean async, String dest, Object data, String[] fixs, String ... columns){
        return service.save(async, dest, data, fixs, columns);
    }

    public static int save(boolean async, String dest, Object data, boolean checkPriamry, String ... columns){
        return service.save(async, dest, data, checkPriamry, columns);
    }
    public static int save(boolean async, Object data, boolean checkPriamry, String ... columns){
        return service.save(async, data, checkPriamry, columns);
    }
    public static int save(boolean async, Object data, String ... columns){
        return service.save(async, data, columns);
    }
    public static int save(boolean async, String dest, Object data, String ... columns){
        return service.save(async, dest, data, columns);
    }


    /* *****************************************************************************************************************
     * 													QUERY
     ******************************************************************************************************************/

    /**
     * 按条件查询
     * @param src 			数据源(表或自定义SQL或SELECT语句)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件
     * @param conditions	固定查询条件
     * 			原生SQL(AND GROUP ORDER)
     * 			{原生}
     * 			[+]CD:1
     * 			[+]CD:
     * 			[+]CD:null
     * 			[+]CD:NULL
     *
     * @return DataSet
     */
    public static DataSet querys(String src, ConfigStore configs, Object obj, String ... conditions){
        return service.querys(src, configs, obj, conditions);
    }
    public static DataSet querys(String src, Object obj, String ... conditions){
        return service.querys(src, obj, conditions);
    }
    public static DataSet querys(String src, PageNavi navi, Object obj, String ... conditions){
        return service.querys(src, navi, obj, conditions);
    }

    /**
     * 按条件查询
     * @param src 			数据源(表或自定义SQL或SELECT语句)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions	固定查询条件
     * @return DataSet
     */
    public static DataSet querys(String src, int first, int last, Object obj, String ... conditions){
        return service.querys(src, first, last, obj, conditions);
    }
    public static DataRow query(String src, ConfigStore configs, Object obj, String ... conditions){
        return service.query(src, configs, obj, conditions);
    }
    public static DataRow query(String src, Object obj, String ... conditions){
        return service.query(src, obj, conditions);
    }

    public static DataSet querys(String src, ConfigStore configs, String ... conditions){
        return service.querys(src, configs, conditions);
    }
    public static DataSet querys(String src,  String ... conditions){
        return service.querys(src, conditions);
    }
    public static DataSet querys(String src, PageNavi navi,  String ... conditions){
        return service.querys(src, navi, conditions);
    }

    /**
     * 按条件查询
     * @param src 			数据源(表或自定义SQL或SELECT语句)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions	固定查询条件
     * @return DataSet
     */
    public static DataSet querys(String src, int first, int last,  String ... conditions){
        return service.querys(src, first, last, conditions);
    }
    public static DataRow query(String src, ConfigStore configs,  String ... conditions){
        return service.query(src, configs, conditions);
    }
    public static DataRow query(String src, String ... conditions){
        return service.query(src, conditions);
    }


    public static <T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return service.selects(src, clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, T entity, String ... conditions){
        return service.selects(src, clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String src, Class<T> clazz, T entity, String ... conditions){
        return service.selects(src, clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String src, Class<T> clazz, int first, int last, T entity, String ... conditions){
        return service.selects(src, clazz, first, last, entity, conditions);
    }
    public static <T> T select(String src, Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return (T)service.select(src, clazz, configs, entity, conditions);
    }
    public static <T> T select(String src, Class<T> clazz, T entity, String ... conditions){
        return (T)service.selects(src, clazz, entity, conditions);
    }

    public static <T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, String ... conditions){
        return service.selects(src, clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, String ... conditions){
        return service.selects(src, clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(String src, Class<T> clazz, String ... conditions){
        return service.selects(src, clazz, conditions);
    }
    public static <T> EntitySet<T> selects(String src, Class<T> clazz, int first, int last, String ... conditions){
        return service.selects(src, clazz, first, last, conditions);
    }
    public static <T> T select(String src, Class<T> clazz, ConfigStore configs, String ... conditions){
        return (T)service.select(src, clazz, configs, conditions);
    }
    public static <T> T select(String src, Class<T> clazz, String ... conditions){
        return (T)service.select(src, clazz, conditions);
    }

    /**
     *
     * @param clazz 返回类型
     * @param configs 根据http等上下文构造查询条件
     * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return EntitySet
     * @param <T> T
     */
    public static <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return service.selects(clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, T entity, String ... conditions){
        return service.selects(clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, T entity, String ... conditions){
        return service.selects(clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, int first, int last, T entity, String ... conditions){
        return service.selects(clazz, first, last, entity, conditions);
    }
    public static <T> T select(Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return (T)service.select(clazz, configs, entity, conditions);
    }
    public static <T> T select(Class<T> clazz, T entity, String ... conditions){
        return (T)service.select(clazz, entity, conditions);
    }

    public static <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, String ... conditions){
        return service.selects(clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, String ... conditions){
        return service.selects(clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, String ... conditions){
        return service.selects(clazz, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, int first, int last, String ... conditions){
        return service.selects(clazz, first, last, conditions);
    }
    public static <T> T select(Class<T> clazz, ConfigStore configs, String ... conditions){
        return (T)service.select(clazz, configs, conditions);
    }
    public static <T> T select(Class<T> clazz, String ... conditions){
        return (T)service.select(clazz, conditions);
    }




    /**
     * 直接返回Map集合不封装,不分页
     * @param src			数据源(表或自定义SQL或SELECT语句)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions	固定查询条件
     * @return List
     */
    public static List<Map<String,Object>> maps(String src, ConfigStore configs, Object obj, String ... conditions){
        return service.maps(src, configs, conditions);
    }
    public static List<Map<String,Object>> maps(String src, Object obj, String ... conditions){
        return service.maps(src, obj, conditions);
    }
    public static List<Map<String,Object>> maps(String src, int first, int last, Object obj, String ... conditions){
        return service.maps(src, first, last, obj, conditions);
    }
    public static List<Map<String,Object>> maps(String src, ConfigStore configs, String ... conditions){
        return service.maps(src, configs, conditions);
    }
    public static List<Map<String,Object>> maps(String src, String ... conditions){
        return service.maps(src, conditions);
    }
    public static List<Map<String,Object>> maps(String src, int first, int last, String ... conditions){
        return service.maps(src, first, last, conditions);
    }



    /**
     * 列名转找成参数名 可以给condition()提供参数用来接收前端参数
     * @param table 表
     * @return List
     */
    public static List<String> column2param(String table){
        return service.column2param(table);
    }



    /**
     * 如果二级缓存开启 会从二级缓存中提取数据
     * @param cache			对应ehcache缓存配置文件 中的cache.name
     * @param src 			数据源(表或自定义SQL或SELECT语句)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 	固定查询条件
     * @return DataSet
     */
    public static DataSet caches(String cache, String src, ConfigStore configs, Object obj, String ... conditions){
        return caches(cache, src, configs, obj, conditions);
    }
    public static DataSet caches(String cache, String src, Object obj, String ... conditions){
        return service.caches(cache, src, obj, conditions);
    }
    public static DataSet caches(String cache, String src, int first, int last, Object obj, String ... conditions){
        return service.caches(cache, src, first, last, obj, conditions);
    }
    public static DataRow cache(String cache, String src, ConfigStore configs, Object obj, String ... conditions){
        return service.cache(cache, src, configs, obj, conditions);
    }
    public static DataRow cache(String cache, String src, Object obj, String ... conditions){
        return service.cache(cache, src, obj, conditions);
    }

    public static DataSet caches(String cache, String src, ConfigStore configs,  String ... conditions){
        return service.caches(cache, src, configs, conditions);
    }
    public static DataSet caches(String cache, String src, String ... conditions){
        return service.caches(cache, src, conditions);
    }
    public static DataSet caches(String cache, String src, int first, int last, String ... conditions){
        return service.caches(cache, src, first, last, conditions);
    }
    public static DataRow cache(String cache, String src, ConfigStore configs, String ... conditions){
        return service.cache(cache, src, configs, conditions);
    }
    public static DataRow cache(String cache, String src, String ... conditions){
        return service.cache(cache, src, conditions);
    }

    /*多表查询,左右连接时使用*/
    public static DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.querys(prepare, configs, obj, conditions);
    }
    public static DataSet querys(RunPrepare prepare, Object obj, String ... conditions){
        return service.querys(prepare, obj, conditions);
    }
    public static DataSet querys(RunPrepare prepare, int first, int last, Object obj, String ... conditions){
        return service.querys(prepare, first, last, obj, conditions);
    }
    public static DataRow query(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.query(prepare, configs, obj, conditions);
    }
    public static DataRow query(RunPrepare prepare, Object obj, String ... conditions){
        return service.query(prepare, obj, conditions);
    }

    public static DataSet querys(RunPrepare prepare, ConfigStore configs,  String ... conditions){
        return service.querys(prepare, configs, conditions);
    }
    public static DataSet querys(RunPrepare prepare,  String ... conditions){
        return service.querys(prepare, conditions);
    }
    public static DataSet querys(RunPrepare prepare, int first, int last,  String ... conditions){
        return service.querys(prepare, first, last, conditions);
    }
    public static DataRow query(RunPrepare prepare, ConfigStore configs,  String ... conditions){
        return service.query(prepare, configs, conditions);
    }
    public static DataRow query(RunPrepare prepare, String ... conditions){
        return service.query(prepare, conditions);
    }


    public static DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.caches(cache, prepare, configs, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, Object obj, String ... conditions){
        return service.caches(cache, prepare, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, int first, int last, Object obj, String ... conditions){
        return service.caches(cache, prepare, first, last, obj, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.cache(cache, prepare, configs, obj, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, Object obj, String ... conditions){
        return service.cache(cache, prepare, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return service.caches(cache, prepare, configs, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, String ... conditions){
        return service.caches(cache, prepare, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, int first, int last, String ... conditions){
        return service.caches(cache, prepare, first, last, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return service.cache(cache, prepare, configs, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, String ... conditions){
        return service.cache(cache, prepare, conditions);
    }

    /**
     * 删除缓存 参数保持与查询参数完全一致
     * @param channel 		channel
     * @param src 			数据源(表或自定义SQL或SELECT语句)
     * @param configs  		根据http等上下文构造查询条件
     * @param conditions 	固定查询条件
     * @return boolean
     */
    public static boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions){
        return service.removeCache(channel, src, configs, conditions);
    }
    public static boolean removeCache(String channel, String src, String ... conditions){
        return service.removeCache(channel, src, conditions);
    }

    public static boolean removeCache(String channel, String src, int first, int last, String ... conditions){
        return service.removeCache(channel, src, first, last, conditions);
    }
    /**
     * 清空缓存
     * @param channel channel
     * @return boolean
     */
    public static boolean clearCache(String channel){
        return service.clearCache(channel);
    }

    /* *****************************************************************************************************************
     * 													EXISTS
     ******************************************************************************************************************/

    /**
     * 是否存在
     * @param src  			数据源(表或自定义SQL或SELECT语句)
     * @param configs  		根据http等上下文构造查询条件
     * @param conditions 	固定查询条件
     * @return boolean
     */
    public static boolean exists(String src, ConfigStore configs, Object obj, String ... conditions){
        return service.exists(src, configs, obj, conditions);
    }
    public static boolean exists(String src, Object obj, String ... conditions){
        return service.exists(src, obj, conditions);
    }
    public static boolean exists(String src, ConfigStore configs, String ... conditions){
        return service.exists(src, configs, conditions);
    }
    public static boolean exists(String src, String ... conditions){
        return service.exists(src, conditions);
    }
    public static boolean exists(String src, DataRow row){
        return service.exists(src, row);
    }
    public static boolean exists(DataRow row){
        return service.exists(row);
    }

    /* *****************************************************************************************************************
     * 													COUNT
     ******************************************************************************************************************/
    public static int count(String src, ConfigStore configs, Object obj, String ... conditions){
        return service.count(src, configs, obj, conditions);
    }
    public static int count(String src, Object obj, String ... conditions){
        return service.count(src, obj, conditions);
    }
    public static int count(String src, ConfigStore configs, String ... conditions){
        return service.count(src, configs, conditions);
    }
    public static int count(String src, String ... conditions){
        return service.count(src, conditions);
    }



    /* *****************************************************************************************************************
     * 													EXECUTE
     ******************************************************************************************************************/

    /**
     * 执行
     * @param src  src
     * @param configs  configs
     * @param conditions  conditions
     * @return int
     */
    public static int execute(String src, ConfigStore configs, String ... conditions){
        return service.execute(src, configs, conditions);
    }
    public static int execute(String src, String ... conditions){
        return service.execute(src, conditions);
    }
    /**
     * 执行存储过程
     * @param procedure  procedure
     * @param inputs  inputs
     * @return boolean
     */
    public static boolean executeProcedure(String procedure, String... inputs){
        return service.executeProcedure(procedure, inputs);
    }
    public static boolean execute(Procedure procedure, String... inputs){
        return service.execute(procedure, inputs);
    }
    /**
     * 根据存储过程查询
     * @param procedure  procedure
     * @param first  first
     * @param last  last
     * @param inputs  inputs
     * @return DataSet
     */
    public static DataSet querysProcedure(String procedure, int first, int last , String ... inputs){
        return service.querysProcedure(procedure, first, last, inputs);
    }
    public static DataSet querysProcedure(String procedure, PageNavi navi , String ... inputs){
        return service.querysProcedure(procedure, navi, inputs);
    }
    public static DataSet querysProcedure(String procedure, String ... inputs){
        return service.querysProcedure(procedure, inputs);
    }
    public static DataSet querys(Procedure procedure, int first, int last,  String ... inputs){
        return service.querys(procedure, first, last, inputs);
    }
    public static DataSet querys(Procedure procedure, PageNavi navi ,  String ... inputs){
        return service.querys(procedure, navi, inputs);
    }

    public static DataRow queryProcedure(String procedure, String ... inputs){
        return service.queryProcedure(procedure, inputs);
    }

    public static DataRow query(Procedure procedure, String ... inputs){
        return service.query(procedure, inputs);
    }

    /* *****************************************************************************************************************
     * 													DELETE
     ******************************************************************************************************************/
    public static int delete(String table, ConfigStore configs, String ... conditions){
        return service.delete(table, configs, conditions);
    }
    /**
     * 删除 根据columns列删除 可设置复合主键
     * @param dest 表
     * @param set 数据
     * @param columns 生成删除条件的列，如果不设置则根据主键删除
     * @return 影响行数
     */
    public static int delete(String dest, DataSet set, String ... columns){
        return service.delete(dest, set, columns);
    }
    public static int delete(DataSet set, String ... columns){
        return service.delete(set, columns);
    }
    public static int delete(String dest, DataRow row, String ... columns){
        return service.delete(dest, row, columns);
    }

    /**
     * 根据columns列删除
     * @param obj obj
     * @param columns 生成删除条件的列，如果不设置则根据主键删除
     * @return 影响行数
     */
    public static int delete(Object obj, String ... columns){
        return service.delete(obj, columns);
    }

    /**
     * 根据多列条件删除 delete("user","type","1", "age:20");
     * @param table 表
     * @param kvs key-value
     * @return 影响行数
     */
    public static int delete(String table, String ... kvs){
        return service.delete(table, kvs);
    }

    /**
     * 根据一列的多个值删除多行
     * @param table 表
     * @param key 名
     * @param values 值集合
     * @return 影响行数
     */
    public static int deletes(String table, String key, Collection<Object> values){
        return service.deletes(table, key, values);
    }

    /**
     * 根据一列的多个值删除多行
     * @param table 表
     * @param key 名
     * @param values 值集合
     * @return 影响行数
     */
    public static int deletes(String table, String key, String ... values){
        return service.deletes(table, key, values);
    }


    /* *****************************************************************************************************************
     * 													METADATA
     ******************************************************************************************************************/

    public static List<String> tables(String catalog, String schema, String name, String types){
        return service.tables(catalog, schema, name, types);
    }
    public static List<String> tables(String schema, String name, String types){
        return service.tables(schema, name, types);
    }
    public static List<String> tables(String name, String types){
        return service.tables(name, types);
    }
    public static List<String> tables(String types){
        return service.tables(types);
    }
    public static List<String> tables(){
        return service.tables();
    }

    public static List<String> mtables(String catalog, String schema, String name, String types){
        return service.mtables(catalog, schema, name, types);
    }
    public static List<String> mtables(String schema, String name, String types){
        return service.mtables(schema, name, types);
    }
    public static List<String> mtables(String name, String types){
        return service.mtables(name, types);
    }
    public static List<String> mtables(String types){
        return service.mtables(types);
    }
    public static List<String> mtables(){
        return service.mtables();
    }


    public static List<String> columns(Table table){
        return service.columns(table);
    }
    public static List<String> columns(String table){
        return service.columns(table);
    }
    public static List<String> columns(String catalog, String schema, String table){
        return service.columns(catalog, schema, table);
    }

    public static List<String> tags(Table table){
        return service.tags(table);
    }
    public static List<String> tags(String table){
        return service.tags(table);
    }
    public static List<String> tags(String catalog, String schema, String table){
        return service.tags(catalog, schema, table);
    }



    public static AnylineService.DDLService ddl(){
        return service.ddl();
    }
    public static AnylineService.MetaDataService metadata(){
        return service.metadata();
    }


    /* *****************************************************************************************************************
     *
     * 													metadata
     *
     * =================================================================================================================
     * database			: 数据库
     * table			: 表
     * master table		: 主表
     * partition table	: 分区表
     * column			: 列
     * tag				: 标签
     * index			: 索引
     * constraint		: 约束
     *
     ******************************************************************************************************************/
    public static interface MetaDataService{


        /* *****************************************************************************************************************
         * 													database
         ******************************************************************************************************************/

        /**
         * 查询所有数据库
         * @return databases
         */
        public static LinkedHashMap<String, Database> databases(){
            return service.metadata().databases();
        }


        /* *****************************************************************************************************************
         * 													table
         ******************************************************************************************************************/

        /**
         * 表是否存在
         * @param table 表
         * @return boolean
         */
        public static boolean exists(Table table){
            return service.metadata().exists(table);
        }
        /**
         * tables
         * @param catalog 对于MySQL，则对应相应的数据库，对于Oracle来说，则是对应相应的数据库实例，可以不填，也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
         * @param schema 可以理解为数据库的登录名，而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意，其登陆名必须是大写，不然的话是无法获取到相应的数据，而MySQL则不做强制要求。
         * @param name 一般情况下如果要获取所有的表的话，可以直接设置为null，如果设置为特定的表名称，则返回该表的具体信息。
         * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
         * @return tables
         */
        public static LinkedHashMap<String,Table> tables(String catalog, String schema, String name, String types){
            return service.metadata().tables(catalog, schema, name, types);
        }
        public static LinkedHashMap<String,Table> tables(String schema, String name, String types){
            return service.metadata().tables(schema, name, types);
        }
        public static LinkedHashMap<String,Table> tables(String name, String types){
            return service.metadata().tables(name, types);
        }
        public static LinkedHashMap<String,Table> tables(String types){
            return service.metadata().tables(types);
        }
        public static LinkedHashMap<String,Table> tables(){
            return service.metadata().tables();
        }


        public static Table table(String catalog, String schema, String name){
            return service.metadata().table(catalog, schema, name);
        }
        public static Table table(String schema, String name){
            return service.metadata().table(schema, name);
        }
        public static Table table(String name){
            return service.metadata().table(name);
        }


        /* *****************************************************************************************************************
         * 													master table
         ******************************************************************************************************************/

        /**
         * 主表是否存在
         * @param table 表
         * @return boolean
         */
        public static boolean exists(MasterTable table){
            return service.metadata().exists(table);
        }
        public static LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types){
            return service.metadata().mtables(catalog, schema, name, types);
        }
        public static LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types){
            return service.metadata().mtables(schema, name, types);
        }
        public static LinkedHashMap<String, MasterTable> mtables(String name, String types){
            return service.metadata().mtables(name, types);
        }
        public static LinkedHashMap<String, MasterTable> mtables(String types){
            return service.metadata().mtables(types);
        }
        public static LinkedHashMap<String, MasterTable> mtables(){
            return service.metadata().mtables();
        }

        public static MasterTable mtable(String catalog, String schema, String name){
            return service.metadata().mtable(catalog, schema, name);
        }
        public static MasterTable mtable(String schema, String name){
            return service.metadata().mtable(schema, name);
        }
        public static MasterTable mtable(String name){
            return service.metadata().mtable(name);
        }


        /* *****************************************************************************************************************
         * 													partition table
         ******************************************************************************************************************/

        /**
         * 主表是否存在
         * @param table 表
         * @return boolean
         */
        public static boolean exists(PartitionTable table){
            return service.metadata().exists(table);
        }
        public static LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String master, String name){
            return service.metadata().ptables(catalog, schema, master, name);
        }
        public static LinkedHashMap<String, PartitionTable> ptables(String schema, String master, String name){
            return service.metadata().ptables(schema, master, name);
        }
        public static LinkedHashMap<String, PartitionTable> ptables(String master, String name){
            return service.metadata().ptables(master, name);
        }
        public static LinkedHashMap<String, PartitionTable> ptables(String master){
            return service.metadata().ptables(master);
        }
        public static LinkedHashMap<String, PartitionTable> ptables(MasterTable master){
            return service.metadata().ptables(master);
        }

        /**
         * 根据主表与标签值查询分区表(子表)
         * @param master 主表
         * @param tags 标签值
         * @return PartitionTables
         */
        public static LinkedHashMap<String, PartitionTable> ptables(MasterTable master, Map<String,Object> tags){
            return service.metadata().ptables(master, tags);
        }

        public static PartitionTable ptable(String catalog, String schema, String master, String name){
            return service.metadata().ptable(catalog, schema, master, name);
        }
        public static PartitionTable ptable(String schema, String master, String name){
            return service.metadata().ptable(schema, master, name);
        }
        public static PartitionTable ptable(String master, String name){
            return service.metadata().ptable(master, name);
        }


        /* *****************************************************************************************************************
         * 													column
         ******************************************************************************************************************/

        /**
         * 列是否存在
         * @param column 列
         * @return boolean
         */
        public static boolean exists(Column column){
            return service.metadata().exists(column);
        }
        public static boolean exists(Table table, String name){
            return service.metadata().exists(table, name);
        }
        public static boolean exists(String table, String name){
            return service.metadata().exists(table, name);
        }
        public static boolean exists(String catalog, String schema, String table, String name){
            return service.metadata().exists(catalog, schema, table, name);
        }
        /**
         * 查询表中所有的表，注意这里的map.KEY全部转大写
         * @param table 表
         * @return map
         */
        public static LinkedHashMap<String,Column> columns(Table table){
            return service.metadata().columns(table);
        }
        public static LinkedHashMap<String,Column> columns(String table){
            return service.metadata().columns(table);
        }
        public static LinkedHashMap<String,Column> columns(String catalog, String schema, String table){
            return service.metadata().columns(catalog, schema, table);
        }

        /**
         * 查询table中的column列
         * @param table 表
         * @param name 列名(不区分大小写)
         * @return Column
         */
        public static Column column(Table table, String name){
            return service.metadata().column(table, name);
        }
        public static Column column(String table, String name){
            return service.metadata().column(table, name);
        }
        public static Column column(String catalog, String schema, String table, String name){
            return service.metadata().column(catalog, schema, table, name);
        }


        /* *****************************************************************************************************************
         * 													tag
         ******************************************************************************************************************/

        public static LinkedHashMap<String, Tag> tags(Table table){
            return service.metadata().tags(table);
        }
        public static LinkedHashMap<String,Tag> tags(String table){
            return service.metadata().tags(table);
        }
        public static LinkedHashMap<String,Tag> tags(String catalog, String schema, String table){
            return service.metadata().tags(catalog, schema, table);
        }


        /* *****************************************************************************************************************
         * 													index
         ******************************************************************************************************************/

        public static LinkedHashMap<String, Index> indexs(Table table){
            return service.metadata().indexs(table);
        }
        public static LinkedHashMap<String,Index> indexs(String table){
            return service.metadata().indexs(table);
        }
        public static LinkedHashMap<String,Index> indexs(String catalog, String schema, String table){
            return service.metadata().indexs(catalog, schema, table);
        }


        /* *****************************************************************************************************************
         * 													constraint
         ******************************************************************************************************************/

        public static LinkedHashMap<String, Constraint> constraints(Table table){
            return service.metadata().constraints(table);
        }
        public static LinkedHashMap<String,Constraint> constraints(String table){
            return service.metadata().constraints(table);
        }
        public static LinkedHashMap<String,Constraint> constraints(String catalog, String schema, String table){
            return service.metadata().constraints(catalog, schema, table);
        }


    }


    /* *****************************************************************************************************************
     *
     * 													DDL
     *
     * =================================================================================================================
     * table			: 表
     * master table		: 主表
     * partition table	: 分区表
     * column			: 列
     * tag				: 标签
     * index			: 索引
     * constraint		: 约束
     *
     ******************************************************************************************************************/
    public static interface DDLService{


        /* *****************************************************************************************************************
         * 													table
         ******************************************************************************************************************/

        public static boolean save(Table table) throws Exception{
            return service.ddl().save(table);
        }
        public static boolean create(Table table) throws Exception{
            return service.ddl().create(table);
        }
        public static boolean alter(Table table) throws Exception{
            return service.ddl().alter(table);
        }
        public static boolean drop(Table table) throws Exception{
            return service.ddl().drop(table);
        }


        /* *****************************************************************************************************************
         * 													master table
         ******************************************************************************************************************/

        public static boolean save(MasterTable table) throws Exception{
            return service.ddl().save(table);
        }
        public static boolean create(MasterTable table) throws Exception{
            return service.ddl().create(table);
        }
        public static boolean alter(MasterTable table) throws Exception{
            return service.ddl().alter(table);
        }
        public static boolean drop(MasterTable table) throws Exception{
            return service.ddl().drop(table);
        }


        /* *****************************************************************************************************************
         * 													partition table
         ******************************************************************************************************************/

        public static boolean save(PartitionTable table) throws Exception{
            return service.ddl().save(table);
        }
        public static boolean create(PartitionTable table) throws Exception{
            return service.ddl().create(table);
        }
        public static boolean alter(PartitionTable table) throws Exception{
            return service.ddl().alter(table);
        }
        public static boolean drop(PartitionTable table) throws Exception{
            return service.ddl().drop(table);
        }


        /* *****************************************************************************************************************
         * 													column
         ******************************************************************************************************************/
        /**
         * 修改列  名称 数据类型 位置 默认值
         * 执行save前先调用column.update()设置修改后的属性
         * column.update().setName().setDefaultValue().setAfter()....
         * @param column 列
         * @throws Exception 异常 SQL异常
         */
        public static boolean save(Column column) throws Exception{
            return service.ddl().save(column);
        }
        public static boolean add(Column column) throws Exception{
            return service.ddl().add(column);
        }
        public static boolean alter(Column column) throws Exception{
            return service.ddl().alter(column);
        }
        public static boolean drop(Column column) throws Exception{
            return service.ddl().drop(column);
        }


        /* *****************************************************************************************************************
         * 													tag
         ******************************************************************************************************************/

        public static boolean save(Tag tag) throws Exception{
            return service.ddl().save(tag);
        }
        public static boolean add(Tag tag) throws Exception{
            return service.ddl().add(tag);
        }
        public static boolean alter(Tag tag) throws Exception{
            return service.ddl().alter(tag);
        }
        public static boolean drop(Tag tag) throws Exception{
            return service.ddl().drop(tag);
        }


        /* *****************************************************************************************************************
         * 													index
         ******************************************************************************************************************/

        public static boolean add(Index index) throws Exception{
            return service.ddl().add(index);
        }
        public static boolean alter(Index index) throws Exception{
            return service.ddl().alter(index);
        }
        public static boolean drop(Index index) throws Exception{
            return service.ddl().drop(index);
        }


        /* *****************************************************************************************************************
         * 													constraint
         ******************************************************************************************************************/
        /**
         * 修改约束
         * @param constraint 约束
         * @return boolean
         * @throws Exception 异常 Exception
         */
        public static boolean add(Constraint constraint) throws Exception{
            return service.ddl().add(constraint);
        }
        public static boolean alter(Constraint constraint) throws Exception{
            return service.ddl().alter(constraint);
        }
        public static boolean drop(Constraint constraint) throws Exception{
            return service.ddl().drop(constraint);
        }
    }
}
