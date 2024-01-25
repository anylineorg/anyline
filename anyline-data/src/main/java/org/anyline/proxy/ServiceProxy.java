/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.anyline.proxy;

import org.anyline.dao.AnylineDao;
import org.anyline.dao.init.springjdbc.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.handler.EntityHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.DefaultRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.service.AnylineService;
import org.anyline.service.init.DefaultService;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.service.proxy")
public class ServiceProxy {
    private static Logger log = LoggerFactory.getLogger(ServiceProxy.class);

    private static AnylineService service;
    private static AnylineService.DDLService ddl;
    private static AnylineService.MetaDataService metadata;
    public ServiceProxy(){}

    @Autowired(required = false)
    @Qualifier("anyline.service")
    public void init(AnylineService service) {
        ServiceProxy.service = service;
        ServiceProxy.ddl = service.ddl();
        ServiceProxy.metadata = service.metadata();
    }

    public static AnylineService service(){
        return service("default");
    }
    public static AnylineService service(String key){
        if(null == key){
            key = "default";
        }
        AnylineService service = (AnylineService)SpringContextUtil.getBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + key);
        /*if(null == service){
            service = (AnylineService)SpringContextUtil.getBean("anyline.service");
        }*/
        return service;
    }
    public static AnylineService service(DatabaseType type, DriverAdapter adapter){
        if(null == adapter){
            return null;
        }
        DataRuntime runtime = new DefaultRuntime();
        runtime.setAdapter(adapter);
        runtime.setKey("virtual("+type+")");
        AnylineService service = new DefaultService();
        AnylineDao dao = new DefaultDao();
        service.setDao(dao);
        dao.setRuntime(runtime);
        return service;
    }
    public static AnylineService service(DatabaseType type){
        DriverAdapter adapter = DriverAdapterHolder.getAdapter(type);
        if(null == adapter && type.url().contains("jdbc:") && ConfigTable.IS_ENABLE_COMMON_JDBC_ADAPTER){
           adapter = (DriverAdapter)SpringContextUtil.getBean("anyline.data.jdbc.adapter.common");
        }
        return service(type, adapter);
    }

    /**
     * 临时数据源
     * @param datasource 数据源, 如DruidDataSource, MongoClient
     * @param database 数据库, jdbc类型数据源不需要
     * @param adapter 如果确认数据库类型可以提供如 new MySQLAdapter(), 如果不提供则根据ds检测
     * @return service
     * @throws Exception 异常 Exception
     */
    public static AnylineService temporary(Object datasource, String database, DriverAdapter adapter) throws Exception {
        DataRuntime runtime =DatasourceHolderProxy.temporary(datasource, database, adapter);
        AnylineDao dao = new DefaultDao();
        //dao.setDatasource(key);
        dao.setRuntime(runtime);
        AnylineService service = new DefaultService();
        //service.setDataSource(key);
        service.setDao(dao);

        return service;
    }

    public static AnylineService temporary(Object datasource) throws Exception {
        return temporary(datasource, null, null);
    }
    public static AnylineService temporary(Object datasource, String database) throws Exception {
        return temporary(datasource, database, null);
    }


    public static ConfigStore condition(){
        return new DefaultConfigStore();
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
     * METADATA			: 简单格式元数据, 只返回NAME
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     * 													INSERT
     ******************************************************************************************************************/

    public static long insert(int batch, String dest, Object data, List<String>  columns){
        return service.insert(batch, dest, data, columns);
    }
    public static long insert(int batch, String dest, Object data, String ... columns){
        return service.insert(batch, dest, data, columns);
    }
    public static long insert(int batch, Object data, String ... columns){
        return service.insert(batch, data, columns);
    }
    public static long insert(String dest, Object data, List<String> columns){
        return service.insert(dest, data, columns);
    }
    public static long insert(String dest, Object data, String ... columns){
        return service.insert(dest, data, columns);
    }
    public static long insert(Object data, String ... columns){
        return service.insert(data, columns);
    }

    public static long insert(int batch, String dest, Object data, ConfigStore configs, List<String>  columns){
        return service.insert(batch, dest, data, configs, columns);
    }
    public static long insert(int batch, String dest, Object data, ConfigStore configs, String ... columns){
        return service.insert(batch, dest, data, configs, columns);
    }
    public static long insert(int batch, Object data, ConfigStore configs, String ... columns){
        return service.insert(batch, data, configs, columns);
    }
    public static long insert(String dest, Object data, ConfigStore configs, List<String> columns){
        return service.insert(dest, data, configs, columns);
    }
    public static long insert(String dest, Object data, ConfigStore configs, String ... columns){
        return service.insert(dest, data, configs, columns);
    }
    public static long insert(Object data, ConfigStore configs, String ... columns){
        return service.insert(data, configs, columns);
    }

    public static long insert(int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
        return service.insert(batch, dest, data, configs, columns);
    }
    public static long insert(int batch, Table dest, Object data, List<String> columns){
        return insert(batch, dest, data, null, columns);
    }
    public static long insert(int batch, Table dest, Object data, String ... columns){
        return insert(batch, dest, data, BeanUtil.array2list(columns));
    }
    public static long insert(int batch, Table dest, Object data, ConfigStore configs, String ... columns){
        return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    public static long insert(Table dest, Object data, List<String> columns){
        return insert(0, dest, data, columns);
    }
    public static long insert(Table dest, Object data, ConfigStore configs, List<String> columns){
        return insert(0, dest, data, configs, columns);
    }
    public static long insert(Table dest, Object data, String ... columns){
        return insert(dest, data, BeanUtil.array2list(columns));
    }
    public static long insert(Table dest, Object data, ConfigStore configs, String ... columns){
        return insert(dest, data, configs, BeanUtil.array2list(columns));
    }
    /* *****************************************************************************************************************
     * 													UPDATE
     ******************************************************************************************************************/

    /**
     * 更新记录
     * 默认情况下以主键为更新条件, 需在更新的数据保存在data中
     * 如果提供了dest则更新dest表, 如果没有提供则根据data解析出表名
     * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键, 则根据TYPE_CODE更新
     * 可以提供了ConfigStore以实现更复杂的更新条件
     * 需要更新的列通过 columns提供
     * @param columns	需要更新的列
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 		更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
     * @param configs 	更新条件
     * @return int 影响行数
     */

    public static long update(int batch, String dest, Object data, ConfigStore configs, List<String> columns){
        return service.update(batch, dest, data, configs, columns);
    }
    public static long update(int batch, String dest, Object data, String ... columns){
        return service.update(batch, dest, data, columns);
    }
    public static long update(int batch, String dest, Object data, ConfigStore configs, String ... columns){
        return service.update(batch, dest, data, configs, columns);
    }

    public static long update(int batch, Object data, String ... columns){
        return service.update(batch, data, columns);
    }
    public static long update(int batch, Object data, ConfigStore configs, String ... columns){
        return service.update(batch, data, configs, columns);
    }

    public static long update(String dest, Object data, String ... columns){
        return service.update(dest, data, columns);
    }
    public static long update(String dest, Object data, ConfigStore configs, String ... columns){
        return service.update(dest, data, configs, columns);
    }
    public static long update(Object data, String ... columns){
        return service.update(data, columns);
    }
    public static long update(Object data, ConfigStore configs, String ... columns){
        return service.update(data, configs, columns);
    }


    public static long update(int batch, Table dest, Object data, ConfigStore configs, List<String>columns){
        return service.update(batch, dest, data, configs, columns);
    }
    public static long update(int batch, Table dest, Object data, String ... columns){
        return update(batch, dest, data, null, BeanUtil.array2list(columns));
    }
    public static long update(int batch, Table dest, Object data, ConfigStore configs, String ... columns){
        return update(batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    public static long update(Table dest, Object data, ConfigStore configs, List<String>columns){
        return update(0, dest, data, configs, columns);
    }
    public static long update(Table dest, Object data, String ... columns){
        return update(dest, data, null, BeanUtil.array2list(columns));
    }
    public static long update(Table dest, Object data, ConfigStore configs, String ... columns){
        return update(dest, data, configs, BeanUtil.array2list(columns));
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
     * insert 集合中的数据必须保存到相同的表, 结构必须相同
     * insert 将一次性插入多条数据整个过程有可能只操作一次数据库  并 不考虑update情况 对于大批量数据来说 性能是主要优势
     *
     * 保存(insert|update)根据是否有主键值确定insert或update
     * @param data  数据
     * @param columns 指定更新或保存的列
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @return 影响行数
     */
    public static long save(int batch, String dest, Object data, List<String>  columns){
        return service.save(batch, dest, data, columns);
    }
    public static long save(int batch, String dest, Object data, String ... columns){
        return service.save(batch, dest, data, columns);
    }
    public static long save(int batch, Object data, String ... columns){
        return service.save(batch, data, columns);
    }
    public static long save(String dest, Object data, String ... columns){
        return service.save(dest, data, columns);
    }
    public static long save(Object data, String ... columns){
        return service.save(data, columns);
    }
    public static long save(int batch, String dest, Object data, ConfigStore configs, List<String>  columns){
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(int batch, String dest, Object data, ConfigStore configs, String ... columns){
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(int batch, Object data, ConfigStore configs, String ... columns){
        return service.save(batch, data, configs, columns);
    }
    public static long save(int batch, String dest, ConfigStore configs, Object data, String ... columns){
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(String dest, Object data, ConfigStore configs, String ... columns){
        return service.save(dest, data, configs, columns);
    }
    public static long save(Object data, ConfigStore configs, String ... columns){
        return service.save(data, configs, columns);
    }
    public static long save(String dest, ConfigStore configs, Object data, String ... columns){
        return service.save(dest, data, configs, columns);
    }

    public static long save(int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(int batch, Table dest, Object data, List<String> columns){
        return save(batch, dest, data, null, columns);
    }

    public static long save(int batch, Table dest, Object data, String ... columns){
        return save(batch, dest, data, BeanUtil.array2list(columns));
    }
    public static long save(int batch, Table dest, Object data, ConfigStore configs, String ... columns){
        return save(batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    public static long save(Table dest, Object data, List<String> columns){
        return save(0, dest, data, columns);
    }
    public static long save(Table dest, Object data, ConfigStore configs, List<String> columns){
        return save(0, dest, data, configs, columns);
    }
    public static long save(Table dest, Object data, String ... columns){
        return save(dest, data, BeanUtil.array2list(columns));
    }
    public static long save(Table dest, Object data, ConfigStore configs, String ... columns){
        return save(dest, data, configs, BeanUtil.array2list(columns));
    }
    /* *****************************************************************************************************************
     * 													QUERY
     ******************************************************************************************************************/

    /**
     * 按条件查询
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
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
    public static DataSet querys(String dest, ConfigStore configs, Object obj, String ... conditions){
        return service.querys(dest, configs, obj, conditions);
    }
    public static DataSet querys(String dest, Object obj, String ... conditions){
        return service.querys(dest, obj, conditions);
    }
    public static void querys(String dest, StreamHandler handler, Object obj, String ... conditions){
        service.querys(dest, handler, obj, conditions);
    }
    public static DataSet querys(String dest, PageNavi navi, Object obj, String ... conditions){
        return service.querys(dest, navi, obj, conditions);
    }

    /**
     * 按条件查询
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions	固定查询条件
     * @return DataSet
     */
    public static DataSet querys(String dest, long first, long last, Object obj, String ... conditions){
        return service.querys(dest, first, last, obj, conditions);
    }
    public static DataRow query(String dest, ConfigStore configs, Object obj, String ... conditions){
        return service.query(dest, configs, obj, conditions);
    }
    public static DataRow query(String dest, Object obj, String ... conditions){
        return service.query(dest, obj, conditions);
    }

    public static DataSet querys(String dest, ConfigStore configs, String ... conditions){
        return service.querys(dest, configs, conditions);
    }
    public static DataSet querys(String dest, String ... conditions){
        return service.querys(dest, conditions);
    }
    public static void querys(String dest, StreamHandler handler, String ... conditions){
        service.querys(dest, handler, conditions);
    }
    public static DataSet querys(String dest, PageNavi navi, String ... conditions){
        return service.querys(dest, navi, conditions);
    }

    /**
     * 按条件查询
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions	固定查询条件
     * @return DataSet
     */
    public static DataSet querys(String dest, long first, long last, String ... conditions){
        return service.querys(dest, first, last, conditions);
    }

    public static DataSet querys(Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.querys(dest, configs, obj, conditions);
    }
    public static DataSet querys(Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions){
        return service.querys(dest, first, last, configs, obj, conditions);
    }
    public static DataSet querys(Table dest, Object obj, String ... conditions){
        return service.querys(dest, obj, conditions);
    }
    public static void querys(Table dest, StreamHandler handler, Object obj, String ... conditions){
        service.querys(dest, handler, obj, conditions);
    }
    public static DataSet querys(Table dest, PageNavi navi, Object obj, String ... conditions){
        return service.querys(dest, navi, obj, conditions);
    }

    /**
     * 按条件查询
     * @param dest 			数据源(表或自定义SQL或SELECT语句)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions	固定查询条件
     * @return DataSet
     */
    public static DataSet querys(Table dest, long first, long last, Object obj, String ... conditions){
        return service.querys(dest, first, last, obj, conditions);
    }

    public static DataSet querys(Table dest, ConfigStore configs, String ... conditions){
        return service.querys(dest, configs, conditions);
    }
    public static DataSet querys(Table dest, long first, long last, ConfigStore configs, String ... conditions){
        return service.querys(dest, first, last, configs, conditions);
    }
    public static DataSet querys(Table dest, String ... conditions){
        return service.querys(dest, conditions);
    }
    public static void querys(Table dest, StreamHandler handler, String ... conditions){
       service.querys(dest, handler, conditions);
    }
    public static DataSet querys(Table dest, PageNavi navi, String ... conditions){
        return service.querys(dest, navi, conditions);
    }
    public static DataSet querys(Table dest, long first, long last, String ... conditions){
        return service.querys(dest, first, last, conditions);
    }
    public static DataSet querys(Table dest, StreamHandler handler, long first, long last, String ... conditions){
        return service.querys(dest, handler, first, last, conditions);
    }


    public static DataRow query(String dest, ConfigStore configs, String ... conditions){
        return service.query(dest, configs, conditions);
    }
    public static DataRow query(String dest, String ... conditions){
        return service.query(dest, conditions);
    }
    public static DataRow query(Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.query(dest, configs, obj, conditions);
    }
    public static DataRow query(Table dest, Object obj, String ... conditions){
        return service.query(dest, obj, conditions);
    }
    public static DataRow query(Table dest, ConfigStore configs, String ... conditions){
        return service.query(dest, configs, conditions);
    }
    public static DataRow query(Table dest, String ... conditions){
        return service.query(dest, conditions);
    }

    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return service.selects(dest, clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, PageNavi navi, T entity, String ... conditions){
        return service.selects(dest, clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, T entity, String ... conditions){
        return service.selects(dest, clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions){
        return service.selects(dest, clazz, handler, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, long first, long last, T entity, String ... conditions){
        return service.selects(dest, clazz, first, last, entity, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return (T)service.select(dest, clazz, configs, entity, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, T entity, String ... conditions){
        return (T)service.selects(dest, clazz, entity, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions){
        return (T)service.selects(dest, clazz, handler, entity, conditions);
    }

    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, String ... conditions){
        return service.selects(dest, clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, PageNavi navi, String ... conditions){
        return service.selects(dest, clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, String ... conditions){
        return service.selects(dest, clazz, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, EntityHandler<T> handler, String ... conditions){
        return service.selects(dest, clazz, handler, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, long first, long last, String ... conditions){
        return service.selects(dest, clazz, first, last, conditions);
    }

    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return service.selects(dest, clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, PageNavi navi, T entity, String ... conditions){
        return service.selects(dest, clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, T entity, String ... conditions){
        return service.selects(dest, clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions){
        return service.selects(dest, clazz, handler, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, long first, long last, T entity, String ... conditions){
        return service.selects(dest, clazz, first, last, entity, conditions);
    }

    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, String ... conditions){
        return service.selects(dest, clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, PageNavi navi, String ... conditions){
        return service.selects(dest, clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, String ... conditions){
        return service.selects(dest, clazz, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, EntityHandler<T> handler, String ... conditions){
        return service.selects(dest, clazz, handler, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, long first, long last, String ... conditions){
        return service.selects(dest, clazz, first, last, conditions);
    }

    public static <T> T select(String dest, Class<T> clazz, ConfigStore configs, String ... conditions){
        return (T)service.select(dest, clazz, configs, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, String ... conditions){
        return (T)service.select(dest, clazz, conditions);
    }

    public static <T> T select(Table dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return (T)service.select(dest, clazz, configs, entity, conditions);
    }
    public static <T> T select(Table dest, Class<T> clazz, T entity, String ... conditions){
        return (T)service.select(dest, clazz, entity, conditions);
    }
    public static <T> T select(Table dest, Class<T> clazz, ConfigStore configs, String ... conditions){
        return (T)service.select(dest, clazz, configs, conditions);
    }
    public static <T> T select(Table dest, Class<T> clazz, String ... conditions){
        return (T)service.select(dest, clazz, conditions);
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
    public static <T> EntitySet<T> selects(Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions){
        return service.selects(clazz, handler, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, long first, long last, T entity, String ... conditions){
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
    public static <T> EntitySet<T> selects(Class<T> clazz, EntityHandler<T> handler, String ... conditions){
        return service.selects(clazz, handler, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, long first, long last, String ... conditions){
        return service.selects(clazz, first, last, conditions);
    }
    public static <T> T select(Class<T> clazz, ConfigStore configs, String ... conditions){
        return (T)service.select(clazz, configs, conditions);
    }
    public static <T> T select(Class<T> clazz, String ... conditions){
        return (T)service.select(clazz, conditions);
    }




    /**
     * 直接返回Map集合不封装, 不分页
     * @param dest			数据源(表或自定义SQL或SELECT语句)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions	固定查询条件
     * @return List
     */
    public static List<Map<String, Object>> maps(String dest, ConfigStore configs, Object obj, String ... conditions){
        return service.maps(dest, configs, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, Object obj, String ... conditions){
        return service.maps(dest, obj, conditions);
    }
    public static void maps(String dest, StreamHandler handler, Object obj, String ... conditions){
        service.maps(dest, handler, obj, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, long first, long last, Object obj, String ... conditions){
        return service.maps(dest, first, last, obj, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, ConfigStore configs, String ... conditions){
        return service.maps(dest, configs, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, String ... conditions){
        return service.maps(dest, conditions);
    }
    public static void maps(String dest, StreamHandler handler, String ... conditions){
        service.maps(dest, handler, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, long first, long last, String ... conditions){
        return service.maps(dest, first, last, conditions);
    }


    public static List<Map<String, Object>> maps(Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.maps(dest, configs, obj, conditions);
    }
    public static void maps(Table dest, StreamHandler handler, Object obj, String ... conditions){
        service.maps(dest, handler, obj, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, Object obj, String ... conditions){
        return service.maps(dest, obj, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, long first, long last, Object obj, String ... conditions){
        return service.maps(dest, first, last, obj, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, ConfigStore configs, String ... conditions){
        return service.maps(dest, configs, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, String ... conditions){
        return service.maps(dest, conditions);
    }
    public static void maps(Table dest, StreamHandler handler, String ... conditions){
        service.maps(dest, handler, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, PageNavi navi, String ... conditions){
        return service.maps(dest, navi, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, long first, long last, String ... conditions){
        return service.maps(dest, first, last, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, StreamHandler handler, long first, long last, String ... conditions){
        return service.maps(dest, handler, first, last, conditions);
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
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 	固定查询条件
     * @return DataSet
     */
    public static DataSet caches(String cache, String dest, ConfigStore configs, Object obj, String ... conditions){
        return caches(cache, dest, configs, obj, conditions);
    }
    public static DataSet caches(String cache, String dest, Object obj, String ... conditions){
        return service.caches(cache, dest, obj, conditions);
    }
    public static DataSet caches(String cache, String dest, long first, long last, Object obj, String ... conditions){
        return service.caches(cache, dest, first, last, obj, conditions);
    }

    public static DataSet caches(String cache, Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.caches(cache, dest, configs, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions){
        return service.caches(cache, dest, first, last, configs, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, Object obj, String ... conditions){
        return service.caches(cache, dest, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, Object obj, String ... conditions){
        return service.caches(cache, dest, first, last, obj, conditions);
    }

    public static DataRow cache(String cache, String dest, ConfigStore configs, Object obj, String ... conditions){
        return service.cache(cache, dest, configs, obj, conditions);
    }
    public static DataRow cache(String cache, String dest, Object obj, String ... conditions){
        return service.cache(cache, dest, obj, conditions);
    }

    public static DataSet caches(String cache, String dest, ConfigStore configs, String ... conditions){
        return service.caches(cache, dest, configs, conditions);
    }
    public static DataSet caches(String cache, String dest, String ... conditions){
        return service.caches(cache, dest, conditions);
    }
    public static DataSet caches(String cache, String dest, long first, long last, String ... conditions){
        return service.caches(cache, dest, first, last, conditions);
    }
    public static DataRow cache(String cache, String dest, ConfigStore configs, String ... conditions){
        return service.cache(cache, dest, configs, conditions);
    }
    public static DataRow cache(String cache, String dest, String ... conditions){
        return service.cache(cache, dest, conditions);
    }

    public static DataRow cache(String cache, Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.cache(cache, dest, configs, obj, conditions);
    }
    public static DataRow cache(String cache, Table dest, Object obj, String ... conditions){
        return service.cache(cache, dest, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, ConfigStore configs, String ... conditions){
        return service.caches(cache, dest, configs, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, ConfigStore configs, String ... conditions){
        return service.caches(cache, dest, first, last, configs, conditions);
    }
    public static DataSet caches(String cache, Table dest, String ... conditions){
        return service.caches(cache, dest, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, String ... conditions){
        return service.caches(cache, dest, first, last, conditions);
    }
    public static DataRow cache(String cache, Table dest, ConfigStore configs, String ... conditions){
        return service.cache(cache, dest, configs, conditions);
    }
    public static DataRow cache(String cache, Table dest, String ... conditions){
        return service.cache(cache, dest, conditions);
    }

    /*多表查询, 左右连接时使用*/
    public static DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.querys(prepare, configs, obj, conditions);
    }
    public static DataSet querys(RunPrepare prepare, Object obj, String ... conditions){
        return service.querys(prepare, obj, conditions);
    }
    public static void querys(RunPrepare prepare, StreamHandler handler, Object obj, String ... conditions){
        service.querys(prepare, handler, obj, conditions);
    }
    public static DataSet querys(RunPrepare prepare, long first, long last, Object obj, String ... conditions){
        return service.querys(prepare, first, last, obj, conditions);
    }
    public static DataRow query(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.query(prepare, configs, obj, conditions);
    }
    public static DataRow query(RunPrepare prepare, Object obj, String ... conditions){
        return service.query(prepare, obj, conditions);
    }

    public static DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions){
        return service.querys(prepare, configs, conditions);
    }
    public static DataSet querys(RunPrepare prepare, String ... conditions){
        return service.querys(prepare, conditions);
    }
    public static void querys(RunPrepare prepare, StreamHandler handler, String ... conditions){
        service.querys(prepare, handler, conditions);
    }
    public static DataSet querys(RunPrepare prepare, long first, long last, String ... conditions){
        return service.querys(prepare, first, last, conditions);
    }
    public static DataRow query(RunPrepare prepare, ConfigStore configs, String ... conditions){
        return service.query(prepare, configs, conditions);
    }
    public static DataRow query(RunPrepare prepare, String ... conditions){
        return service.query(prepare, conditions);
    }
    public static void query(RunPrepare prepare, StreamHandler handler, String ... conditions){
        service.query(prepare, handler, conditions);
    }


    public static DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.caches(cache, prepare, configs, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, Object obj, String ... conditions){
        return service.caches(cache, prepare, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, long first, long last, Object obj, String ... conditions){
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
    public static DataSet caches(String cache, RunPrepare prepare, long first, long last, String ... conditions){
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
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param configs  		根据http等上下文构造查询条件
     * @param conditions 	固定查询条件
     * @return boolean
     */
    public static boolean removeCache(String channel, String dest, ConfigStore configs, String ... conditions){
        return service.removeCache(channel, dest, configs, conditions);
    }
    public static boolean removeCache(String channel, String dest, String ... conditions){
        return service.removeCache(channel, dest, conditions);
    }

    public static boolean removeCache(String channel, String dest, long first, long last, String ... conditions){
        return service.removeCache(channel, dest, first, last, conditions);
    }
    public static boolean removeCache(String channel, Table dest, ConfigStore configs, String ... conditions){
        return service.removeCache(channel, dest, configs, conditions);
    }
    public static boolean removeCache(String channel, Table dest, String ... conditions){
        return service.removeCache(channel, dest, conditions);
    }
    public static boolean removeCache(String channel, Table dest, long first, long last, String ... conditions){
        return service.removeCache(channel, dest, first, last, conditions);
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
     * @param dest  			数据源(表或自定义SQL或SELECT语句)
     * @param configs  		根据http等上下文构造查询条件
     * @param conditions 	固定查询条件
     * @return boolean
     */
    public static boolean exists(String dest, ConfigStore configs, Object obj, String ... conditions){
        return service.exists(dest, configs, obj, conditions);
    }
    public static boolean exists(String dest, Object obj, String ... conditions){
        return service.exists(dest, obj, conditions);
    }
    public static boolean exists(String dest, ConfigStore configs, String ... conditions){
        return service.exists(dest, configs, conditions);
    }
    public static boolean exists(String dest, String ... conditions){
        return service.exists(dest, conditions);
    }
    public static boolean exists(String dest, DataRow row){
        return service.exists(dest, row);
    }
    public static boolean exists(DataRow row){
        return service.exists(row);
    }
    public static boolean exists(Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.exists(dest, configs, obj, conditions);
    }
    public static boolean exists(Table dest, Object obj, String ... conditions){
        return service.exists(dest, obj, conditions);
    }
    public static boolean exists(Table dest, ConfigStore configs, String ... conditions){
        return service.exists(dest, configs, conditions);
    }
    public static boolean exists(Table dest, String ... conditions){
        return service.exists(dest, conditions);
    }
    public static boolean exists(Table dest, DataRow row){
        return service.exists(dest, row);
    }

    /* *****************************************************************************************************************
     * 													COUNT
     ******************************************************************************************************************/
    public static long count(String dest, ConfigStore configs, Object obj, String ... conditions){
        return service.count(dest, configs, obj, conditions);
    }
    public static long count(String dest, Object obj, String ... conditions){
        return service.count(dest, obj, conditions);
    }
    public static long count(String dest, ConfigStore configs, String ... conditions){
        return service.count(dest, configs, conditions);
    }
    public static long count(String dest, String ... conditions){
        return service.count(dest, conditions);
    }


    public static long count(Table dest, ConfigStore configs, Object obj, String ... conditions){
        return service.count(dest, configs, obj, conditions);
    }
    public static long count(Table dest, Object obj, String ... conditions){
        return service.count(dest, obj, conditions);
    }
    public static long count(Table dest, ConfigStore configs, String ... conditions){
        return service.count(dest, configs, conditions);
    }
    public static long count(Table dest, String ... conditions){
        return service.count(dest, conditions);
    }

    /* *****************************************************************************************************************
     * 													EXECUTE
     ******************************************************************************************************************/

    /**
     * 执行
     * @param dest  查询或操作的目标(表、存储过程、SQL等)
     * @param configs  configs
     * @param conditions  conditions
     * @return int
     */
    public static long execute(String dest, ConfigStore configs, String ... conditions){
        return service.execute(dest, configs, conditions);
    }
    public static long execute(String dest, String ... conditions){
        return service.execute(dest, conditions);
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
    public static DataSet querysProcedure(String procedure, long first, long last, String ... inputs){
        return service.querysProcedure(procedure, first, last, inputs);
    }
    public static DataSet querysProcedure(String procedure, PageNavi navi, String ... inputs){
        return service.querysProcedure(procedure, navi, inputs);
    }
    public static DataSet querysProcedure(String procedure, String ... inputs){
        return service.querysProcedure(procedure, inputs);
    }
    public static DataSet querys(Procedure procedure, long first, long last, String ... inputs){
        return service.querys(procedure, first, last, inputs);
    }
    public static DataSet querys(Procedure procedure, PageNavi navi, String ... inputs){
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
    public static long delete(String table, ConfigStore configs, String ... conditions){
        return service.delete(table, configs, conditions);
    }
    public static long delete(Table dest, ConfigStore configs, String ... conditions){
        return service().delete(dest, configs, conditions);
    }
    /**
     * 删除 根据columns列删除 可设置复合主键
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param set 数据
     * @param columns 生成删除条件的列, 如果不设置则根据主键删除
     * @return 影响行数
     */
    public static long delete(String dest, DataSet set, String ... columns){
        return service.delete(dest, set, columns);
    }
    public static long delete(Table dest, DataSet set, String ... columns){
        return service.delete(dest, set, columns);
    }
    public static long delete(DataSet set, String ... columns){
        return service.delete(set, columns);
    }
    public static long delete(String dest, DataRow row, String ... columns){
        return service.delete(dest, row, columns);
    }
    public static long delete(Table dest, DataRow row, String ... columns){
        return service.delete(dest, row, columns);
    }

    /**
     * 根据columns列删除
     * @param obj obj
     * @param columns 生成删除条件的列, 如果不设置则根据主键删除
     * @return 影响行数
     */
    public static long delete(Object obj, String ... columns){
        return service.delete(obj, columns);
    }

    /**
     * 根据多列条件删除 delete("user","type","1","age:20");
     * @param table 表
     * @param kvs key-value
     * @return 影响行数
     */
    public static long delete(String table, String ... kvs){
        return service.delete(table, kvs);
    }
    public static long delete(Table table, String ... kvs){
        return service.delete(table, kvs);
    }

    /**
     * 根据一列的多个值删除多行
     * @param table 表
     * @param key 名
     * @param values 值集合
     * @return 影响行数
     */
    public static long deletes(String table, String key, Collection<Object> values){
        return service.deletes(table, key, values);
    }
    public static long deletes(Table table, String key, Collection<Object> values){
        return service.deletes(table, key, values);
    }

    public static long deletes(int batch, String table, String key, Collection<Object> values){
        return service.deletes(batch, table, key, values);
    }
    public static long deletes(int batch, Table table, String key, Collection<Object> values){
        return service.deletes(batch, table, key, values);
    }

    /**
     * 根据一列的多个值删除多行
     * @param table 表
     * @param key 名
     * @param values 值集合
     * @return 影响行数
     */
    public static <T> long deletes(String table, String key, T ... values){
        return service.deletes(table, key, values);
    }

    public static <T> long deletes(Table table, String key, T ... values){
        return service.deletes(table, key, values);
    }

    public static <T> long deletes(int batch, String table, String key, T ... values){
        return service.deletes(batch, table, key, values);
    }

    public static <T> long deletes(int batch, Table table, String key, T ... values){
        return service.deletes(batch, table, key, values);
    }


    /* *****************************************************************************************************************
     * 													METADATA
     ******************************************************************************************************************/

    public static List<String> tables(Catalog catalog, Schema schema, String name, String types){
        return service.tables(catalog, schema, name, types);
    }
    public static List<String> tables(Schema schema, String name, String types){
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

    public static List<String> mtables(Catalog catalog, Schema schema, String name, String types){
        return service.mtables(catalog, schema, name, types);
    }
    public static List<String> mtables(Schema schema, String name, String types){
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
    public static List<String> columns(Catalog catalog, Schema schema, String table){
        return service.columns(catalog, schema, table);
    }

    public static List<String> tags(Table table){
        return service.tags(table);
    }
    public static List<String> tags(String table){
        return service.tags(table);
    }
    public static List<String> tags(Catalog catalog, Schema schema, String table){
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
     * primary key      : 主键
     * foreign key		: 外键
     * index			: 索引
     * constraint		: 约束
     * trigger		    : 触发器
     * procedure        : 存储过程
     ******************************************************************************************************************/
    public static interface MetaDataService{


        /* *****************************************************************************************************************
         * 													database
         ******************************************************************************************************************/

        /**
         * 查询全部数据库
         * @return databases
         */
        public static LinkedHashMap<String, Database> databases(){
            return service.metadata().databases();
        }
        public static Database database(String name){
            return service.metadata().database(name);
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
         * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
         * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
         * @param name 一般情况下如果要获取所有的表的话, 可以直接设置为null, 如果设置为特定的表名称, 则返回该表的具体信息。
         * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
         * @return tables
         */
        public static LinkedHashMap<String, Table> tables(Catalog catalog, Schema schema, String name, String types){
            return service.metadata().tables(catalog, schema, name, types);
        }
        public static LinkedHashMap<String, Table> tables(Schema schema, String name, String types){
            return service.metadata().tables(schema, name, types);
        }
        public static LinkedHashMap<String, Table> tables(String name, String types){
            return service.metadata().tables(name, types);
        }
        public static LinkedHashMap<String, Table> tables(String types){
            return service.metadata().tables(types);
        }
        public static LinkedHashMap<String, Table> tables(){
            return service.metadata().tables();
        }


        public static Table table(Catalog catalog, Schema schema, String name){
            return service.metadata().table(catalog, schema, name);
        }
        public static Table table(Schema schema, String name){
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
        public static LinkedHashMap<String, MasterTable> mtables(Catalog catalog, Schema schema, String name, String types){
            return service.metadata().mtables(catalog, schema, name, types);
        }
        public static LinkedHashMap<String, MasterTable> mtables(Schema schema, String name, String types){
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

        public static MasterTable mtable(Catalog catalog, Schema schema, String name){
            return service.metadata().mtable(catalog, schema, name);
        }
        public static MasterTable mtable(Schema schema, String name){
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
        public static LinkedHashMap<String, PartitionTable> ptables(Catalog catalog, Schema schema, String master, String name){
            return service.metadata().ptables(catalog, schema, master, name);
        }
        public static LinkedHashMap<String, PartitionTable> ptables(Schema schema, String master, String name){
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
        public static LinkedHashMap<String, PartitionTable> ptables(MasterTable master, Map<String, Object> tags){
            return service.metadata().ptables(master, tags);
        }

        public static PartitionTable ptable(Catalog catalog, Schema schema, String master, String name){
            return service.metadata().ptable(catalog, schema, master, name);
        }
        public static PartitionTable ptable(Schema schema, String master, String name){
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
        public static boolean exists(Catalog catalog, Schema schema, String table, String name){
            return service.metadata().exists(catalog, schema, table, name);
        }
        /**
         * 查询表中所有的表, 注意这里的map.KEY全部转大写
         * @param table 表
         * @return map
         */
        public static LinkedHashMap<String, Column> columns(Table table){
            return service.metadata().columns(table);
        }
        public static LinkedHashMap<String, Column> columns(String table){
            return service.metadata().columns(table);
        }
        public static LinkedHashMap<String, Column> columns(Catalog catalog, Schema schema, String table){
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
        public static Column column(Catalog catalog, Schema schema, String table, String name){
            return service.metadata().column(catalog, schema, table, name);
        }


        /* *****************************************************************************************************************
         * 													tag
         ******************************************************************************************************************/

        public static LinkedHashMap<String, Tag> tags(Table table){
            return service.metadata().tags(table);
        }
        public static LinkedHashMap<String, Tag> tags(String table){
            return service.metadata().tags(table);
        }
        public static LinkedHashMap<String, Tag> tags(Catalog catalog, Schema schema, String table){
            return service.metadata().tags(catalog, schema, table);
        }


        /* *****************************************************************************************************************
         * 													index
         ******************************************************************************************************************/

        public static LinkedHashMap<String, Index> indexs(Table table){
            return service.metadata().indexs(table);
        }
        public static LinkedHashMap<String, Index> indexs(String table){
            return service.metadata().indexs(table);
        }
        public static LinkedHashMap<String, Index> indexs(Catalog catalog, Schema schema, String table){
            return service.metadata().indexs(catalog, schema, table);
        }


        /* *****************************************************************************************************************
         * 													constraint
         ******************************************************************************************************************/

        public static LinkedHashMap<String, Constraint> constraints(Table table){
            return service.metadata().constraints(table);
        }
        public static LinkedHashMap<String, Constraint> constraints(String table){
            return service.metadata().constraints(table);
        }
        public static LinkedHashMap<String, Constraint> constraints(Catalog catalog, Schema schema, String table){
            return service.metadata().constraints(catalog, schema, table);
        }


    }



    /* *****************************************************************************************************************
     *
     * 													DDL
     *
     * =================================================================================================================
     * database			: 数据库
     * table			: 表
     * master table		: 主表
     * partition table	: 分区表
     * column			: 列
     * tag				: 标签
     * primary key      : 主键
     * foreign key		: 外键
     * index			: 索引
     * constraint		: 约束
     * trigger		    : 触发器
     * procedure        : 存储过程
     ******************************************************************************************************************/

    public static interface DDLService{


        /* *****************************************************************************************************************
         * 													table
         ******************************************************************************************************************/

        public static boolean save(Table table) throws Exception {
            return service.ddl().save(table);
        }
        public static boolean create(Table table) throws Exception {
            return service.ddl().create(table);
        }
        public static boolean alter(Table table) throws Exception {
            return service.ddl().alter(table);
        }
        public static boolean drop(Table table) throws Exception {
            return service.ddl().drop(table);
        }


        /* *****************************************************************************************************************
         * 													master table
         ******************************************************************************************************************/

        public static boolean save(MasterTable table) throws Exception {
            return service.ddl().save(table);
        }
        public static boolean create(MasterTable table) throws Exception {
            return service.ddl().create(table);
        }
        public static boolean alter(MasterTable table) throws Exception {
            return service.ddl().alter(table);
        }
        public static boolean drop(MasterTable table) throws Exception {
            return service.ddl().drop(table);
        }


        /* *****************************************************************************************************************
         * 													partition table
         ******************************************************************************************************************/

        public static boolean save(PartitionTable table) throws Exception {
            return service.ddl().save(table);
        }
        public static boolean create(PartitionTable table) throws Exception {
            return service.ddl().create(table);
        }
        public static boolean alter(PartitionTable table) throws Exception {
            return service.ddl().alter(table);
        }
        public static boolean drop(PartitionTable table) throws Exception {
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
        public static boolean save(Column column) throws Exception {
            return service.ddl().save(column);
        }
        public static boolean add(Column column) throws Exception {
            return service.ddl().add(column);
        }
        public static boolean alter(Column column) throws Exception {
            return service.ddl().alter(column);
        }
        public static boolean drop(Column column) throws Exception {
            return service.ddl().drop(column);
        }


        /* *****************************************************************************************************************
         * 													tag
         ******************************************************************************************************************/

        public static boolean save(Tag tag) throws Exception {
            return service.ddl().save(tag);
        }
        public static boolean add(Tag tag) throws Exception {
            return service.ddl().add(tag);
        }
        public static boolean alter(Tag tag) throws Exception {
            return service.ddl().alter(tag);
        }
        public static boolean drop(Tag tag) throws Exception {
            return service.ddl().drop(tag);
        }


        /* *****************************************************************************************************************
         * 													index
         ******************************************************************************************************************/

        public static boolean add(Index index) throws Exception {
            return service.ddl().add(index);
        }
        public static boolean alter(Index index) throws Exception {
            return service.ddl().alter(index);
        }
        public static boolean drop(Index index) throws Exception {
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
        public static boolean add(Constraint constraint) throws Exception {
            return service.ddl().add(constraint);
        }
        public static boolean alter(Constraint constraint) throws Exception {
            return service.ddl().alter(constraint);
        }
        public static boolean drop(Constraint constraint) throws Exception {
            return service.ddl().drop(constraint);
        }
    }


}
