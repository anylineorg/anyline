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

import org.anyline.adapter.EnvironmentWorker;
import org.anyline.dao.AnylineDao;
import org.anyline.dao.init.DefaultDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapterHolder;
import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.handler.DataHandler;
import org.anyline.data.handler.EntityHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.init.AbstractRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.MetadataDiffer;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.service.AnylineService;
import org.anyline.service.init.DefaultService;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ServiceProxy {
    private static Logger log = LoggerFactory.getLogger(ServiceProxy.class);
    private static AnylineService service;
    private static AnylineService.DDLService ddl;
    private static AnylineService.MetaDataService metadata;
    public ServiceProxy() {}

    public static void init(AnylineService service) {
        ServiceProxy.service = service;
        ServiceProxy.ddl = service.ddl();
        ServiceProxy.metadata = service.metadata();
    }

    public static AnylineService service() {
        AnylineService service = service("default");
        return service;
    }
    public static AnylineService service(String key) {
        if(null == key) {
            key = "default";
        }
        AnylineService service = null;
        try{
            EnvironmentWorker worker = ConfigTable.environment();
            if(null != worker) {
                if (worker.containsBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + key)) {
                    service = (AnylineService) worker.getBean(DataRuntime.ANYLINE_SERVICE_BEAN_PREFIX + key);
                }
            } else {
                log.error("请在上下文环境初始化完成后调用");
            }
        }catch (Exception e) {
            log.error("get service exception:", e);
        }
        return service;
    }
    public static AnylineService service(DatabaseType type, DriverAdapter adapter) {
        if(null == adapter) {
            return null;
        }
        DataRuntime runtime = new AbstractRuntime();
        runtime.setAdapter(adapter);
        runtime.setKey("virtual("+type+")");
        AnylineService service = new DefaultService();
        AnylineDao dao = new DefaultDao();
        service.setDao(dao);
        dao.setRuntime(runtime);
        return service;
    }
    public static AnylineService service(DatabaseType type) {
        DriverAdapter adapter = DriverAdapterHolder.getAdapter(type);
        if(null == adapter && type.url().contains("jdbc:") && ConfigTable.IS_ENABLE_COMMON_JDBC_ADAPTER) {
            try {
                adapter = (DriverAdapter) ConfigTable.environment().getBean("anyline.data.jdbc.adapter.common");
            }catch (Exception e) {
                e.printStackTrace();
            }
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
    public static AnylineService temporary(Object datasource, String database, DatabaseType type, DriverAdapter adapter) throws Exception {
        DataRuntime runtime = DataSourceHolder.reg("temporary", datasource, database, type, adapter, true);
        AnylineDao dao = new DefaultDao();
        //dao.setDataSource(key);
        dao.setRuntime(runtime);
        AnylineService service = new DefaultService();
        //service.setDataSource(key);
        service.setDao(dao);

        return service;
    }

    public static AnylineService temporary(Object datasource, DatabaseType type) throws Exception {
        return temporary(datasource, null, type, null);
    }
    public static AnylineService temporary(Object datasource, String database, DatabaseType type) throws Exception {
        return temporary(datasource, database, type, null);
    }
    public static AnylineService temporary(Object datasource) throws Exception {
        return temporary(datasource, null, null);
    }
    public static AnylineService temporary(Object datasource, String database) throws Exception {
        return temporary(datasource, database, null);
    }

    public static ConfigStore condition() {
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
    public static long insert(int batch, String dest, Object data, List<String>  columns) {
        return service.insert(batch, dest, data, columns);
    }
    public static long insert(int batch, String dest, Object data, String ... columns) {
        return service.insert(batch, dest, data, columns);
    }
    public static long insert(int batch, Object data, String ... columns) {
        return service.insert(batch, data, columns);
    }
    public static long insert(String dest, Object data, List<String> columns) {
        return service.insert(dest, data, columns);
    }
    public static long insert(String dest, Object data, String ... columns) {
        return service.insert(dest, data, columns);
    }
    public static long insert(Object data, String ... columns) {
        return service.insert(data, columns);
    }

    public static long insert(int batch, String dest, Object data, ConfigStore configs, List<String>  columns) {
        return service.insert(batch, dest, data, configs, columns);
    }
    public static long insert(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
        return service.insert(batch, dest, data, configs, columns);
    }
    public static long insert(int batch, Object data, ConfigStore configs, String ... columns) {
        return service.insert(batch, data, configs, columns);
    }
    public static long insert(String dest, Object data, ConfigStore configs, List<String> columns) {
        return service.insert(dest, data, configs, columns);
    }
    public static long insert(String dest, Object data, ConfigStore configs, String ... columns) {
        return service.insert(dest, data, configs, columns);
    }
    public static long insert(Object data, ConfigStore configs, String ... columns) {
        return service.insert(data, configs, columns);
    }

    public static long insert(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return service.insert(batch, dest, data, configs, columns);
    }
    public static long insert(int batch, Table dest, Object data, List<String> columns) {
        return insert(batch, dest, data, null, columns);
    }
    public static long insert(int batch, Table dest, Object data, String ... columns) {
        return insert(batch, dest, data, BeanUtil.array2list(columns));
    }
    public static long insert(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
        return insert(batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    public static long insert(Table dest, Object data, List<String> columns) {
        return insert(0, dest, data, columns);
    }
    public static long insert(Table dest, Object data, ConfigStore configs, List<String> columns) {
        return insert(0, dest, data, configs, columns);
    }
    public static long insert(Table dest, Object data, String ... columns) {
        return insert(dest, data, BeanUtil.array2list(columns));
    }
    public static long insert(Table dest, Object data, ConfigStore configs, String ... columns) {
        return insert(dest, data, configs, BeanUtil.array2list(columns));
    }

    /* *****************************************************************************************************************
     * 													INSERT SELECT
     ******************************************************************************************************************/
    /**
     * insert into table select * from table
     * 与query参数一致
     * @param dest 插入表
     * @param origin 查询表
     * @param configs 查询条件及相关配置
     * @param obj 查询条件
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    public static long insert(Table dest, Table origin, ConfigStore configs, Object obj, String ... conditions){
        return service.insert(dest, origin, configs, obj, conditions);
    }
    public static long insert(Table dest, Table origin, Object obj, String ... conditions) {
        return service.insert(dest, origin, obj, conditions);
    }
    public static long insert(Table dest, Table origin, long first, long last, Object obj, String ... conditions) {
        return service.insert(dest, origin, first, last, obj, conditions);
    }
    public static long insert(Table dest, Table origin, ConfigStore configs, String ... conditions) {
        return service.insert(dest, origin, configs, conditions);
    }
    public static long insert(Table dest, Table origin, String ... conditions) {
        return service.insert(dest, origin, conditions);
    }
    public static long insert(Table dest, Table origin, PageNavi navi, String ... conditions) {
        return service.insert(dest, origin, navi,  conditions);
    }
    public static long insert(Table dest, Table origin, long first, long last, String ... conditions) {
        return service.insert(dest, origin, first, last,  conditions);
    }

    public static long insert(String dest, String origin, ConfigStore configs, Object obj, String ... conditions){
        return service.insert(dest, origin, configs, obj, conditions);
    }
    public static long insert(String dest, String origin, Object obj, String ... conditions) {
        return service.insert(dest, origin, obj, conditions);
    }
    public static long insert(String dest, String origin, long first, long last, Object obj, String ... conditions) {
        return service.insert(dest, origin, first, last, obj, conditions);
    }
    public static long insert(String dest, String origin, ConfigStore configs, String ... conditions) {
        return service.insert(dest, origin, configs,  conditions);
    }
    public static long insert(String dest, String origin, String ... conditions) {
        return service.insert(dest, origin, conditions);
    }
    public static long insert(String dest, String origin, PageNavi navi, String ... conditions) {
        return service.insert(dest, origin, navi, conditions);
    }
    public static long insert(String dest, String origin, long first, long last, String ... conditions) {
        return service.insert(dest, origin, first, last, conditions);
    }

    /**
     * insert into table select * from table
     * 复杂的查询先通过TableBuilder构造查询
     * @param dest 表 table(c1,c2,c3)需要插入的列可以通过addColumn添加到dest中
     * @param prepare 一般通过TableBuilder生成查询
     * @return 影响行数
     */
    public static long insert(Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.insert(dest, prepare, configs, obj, conditions);
    }
    public static long insert(Table dest, RunPrepare prepare, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return service.insert(dest, prepare, first, last, configs, obj, conditions);
    }
    public static long insert(Table dest, RunPrepare prepare, Object obj, String ... conditions) {
        return service.insert(dest, prepare,  obj, conditions);
    }
    public static long insert(Table dest, RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
        return service.insert(dest, prepare, first, last, obj, conditions);
    }

    public static long insert(Table dest, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return service.insert(dest, prepare, configs,  conditions);
    }
    public static long insert(Table dest, RunPrepare prepare, String ... conditions) {
        return service.insert(dest, prepare, conditions);
    }
    public static long insert(Table dest, RunPrepare prepare, long first, long last, String ... conditions) {
        return service.insert(dest, prepare, first, last,  conditions);
    }

    public static long insert(String dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.insert(dest, prepare, configs, obj, conditions);
    }
    public static long insert(String dest, RunPrepare prepare, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return service.insert(dest, prepare, first, last, configs, obj, conditions);
    }
    public static long insert(String dest, RunPrepare prepare, Object obj, String ... conditions) {
        return service.insert(dest, prepare, obj, conditions);
    }
    public static long insert(String dest, RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
        return insert(dest, prepare, first, last, obj, conditions);
    }

    public static long insert(String dest, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return service.insert(dest, prepare, configs, conditions);
    }
    public static long insert(String dest, RunPrepare prepare, String ... conditions) {
        return service.insert(dest, prepare,  conditions);
    }
    public static long insert(String dest, RunPrepare prepare, long first, long last, String ... conditions) {
        return service.insert(dest, prepare, first, last, conditions);
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
    public static long update(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        return service.update(batch, dest, data, configs, columns);
    }
    public static long update(int batch, String dest, Object data, String ... columns) {
        return service.update(batch, dest, data, columns);
    }
    public static long update(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
        return service.update(batch, dest, data, configs, columns);
    }

    public static long update(int batch, Object data, String ... columns) {
        return service.update(batch, data, columns);
    }
    public static long update(int batch, Object data, ConfigStore configs, String ... columns) {
        return service.update(batch, data, configs, columns);
    }

    public static long update(String dest, Object data, String ... columns) {
        return service.update(dest, data, columns);
    }
    public static long update(String dest, Object data, ConfigStore configs, String ... columns) {
        return service.update(dest, data, configs, columns);
    }
    public static long update(Object data, String ... columns) {
        return service.update(data, columns);
    }
    public static long update(Object data, ConfigStore configs, String ... columns) {
        return service.update(data, configs, columns);
    }

    public static long update(int batch, Table dest, Object data, ConfigStore configs, List<String>columns) {
        return service.update(batch, dest, data, configs, columns);
    }
    public static long update(int batch, Table dest, Object data, String ... columns) {
        return update(batch, dest, data, null, BeanUtil.array2list(columns));
    }
    public static long update(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
        return update(batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    public static long update(Table dest, Object data, ConfigStore configs, List<String>columns) {
        return update(0, dest, data, configs, columns);
    }
    public static long update(Table dest, Object data, String ... columns) {
        return update(dest, data, null, BeanUtil.array2list(columns));
    }
    public static long update(Table dest, Object data, ConfigStore configs, String ... columns) {
        return update(dest, data, configs, BeanUtil.array2list(columns));
    }

    /**
     * 多表关联更新
     * @param prepare 一般通过TableBuilder生成
     * @param data K-DataRow.VariableValue 更新值key:需要更新的列 value:通常是关联表的列用DataRow.VariableValue表示，也可以是常量
     * @param configs 附加过滤条件 通常是在prepare最外层设置
     * @param conditions 附加查询条件 支持k:v k:v::type 以及原生sql形式 默认忽略空值条件 <br/>
     * @return 影响行数
     */
    public static long update(RunPrepare prepare, DataRow data, ConfigStore configs, String ... conditions) {
        return service.update(prepare, data, configs, conditions);
    }
    public static long update(RunPrepare prepare, DataRow data, String ... conditions) {
        return update(prepare, data, new DefaultConfigStore(), conditions);
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
    public static long save(int batch, String dest, Object data, List<String>  columns) {
        return service.save(batch, dest, data, columns);
    }
    public static long save(int batch, String dest, Object data, String ... columns) {
        return service.save(batch, dest, data, columns);
    }
    public static long save(int batch, Object data, String ... columns) {
        return service.save(batch, data, columns);
    }
    public static long save(String dest, Object data, String ... columns) {
        return service.save(dest, data, columns);
    }
    public static long save(Object data, String ... columns) {
        return service.save(data, columns);
    }
    public static long save(int batch, String dest, Object data, ConfigStore configs, List<String>  columns) {
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(int batch, String dest, Object data, ConfigStore configs, String ... columns) {
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(int batch, Object data, ConfigStore configs, String ... columns) {
        return service.save(batch, data, configs, columns);
    }
    public static long save(int batch, String dest, ConfigStore configs, Object data, String ... columns) {
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(String dest, Object data, ConfigStore configs, String ... columns) {
        return service.save(dest, data, configs, columns);
    }
    public static long save(Object data, ConfigStore configs, String ... columns) {
        return service.save(data, configs, columns);
    }
    public static long save(String dest, ConfigStore configs, Object data, String ... columns) {
        return service.save(dest, data, configs, columns);
    }

    public static long save(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return service.save(batch, dest, data, configs, columns);
    }
    public static long save(int batch, Table dest, Object data, List<String> columns) {
        return save(batch, dest, data, null, columns);
    }

    public static long save(int batch, Table dest, Object data, String ... columns) {
        return save(batch, dest, data, BeanUtil.array2list(columns));
    }
    public static long save(int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
        return save(batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    public static long save(Table dest, Object data, List<String> columns) {
        return save(0, dest, data, columns);
    }
    public static long save(Table dest, Object data, ConfigStore configs, List<String> columns) {
        return save(0, dest, data, configs, columns);
    }
    public static long save(Table dest, Object data, String ... columns) {
        return save(dest, data, BeanUtil.array2list(columns));
    }
    public static long save(Table dest, Object data, ConfigStore configs, String ... columns) {
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * 			原生SQL(AND GROUP ORDER)
     * 			{原生}
     * 			[+]CD:1
     * 			[+]CD:
     * 			[+]CD:null
     * 			[+]CD:NULL
     *
     * @return DataSet
     */
    public static DataSet querys(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.querys(dest, configs, obj, conditions);
    }
    public static DataSet querys(ConfigStore configs, Object obj, String ... conditions) {
        return service.querys(configs, obj, conditions);
    }
    public static DataSet querys(ConfigStore configs, String ... conditions) {
        return service.querys(configs, conditions);
    }
    public static DataSet querys(DataSet set, ConfigStore configs) {
        return service.querys(set, configs);
    }
    public static DataSet querys(String dest, Object obj, String ... conditions) {
        return service.querys(dest, obj, conditions);
    }
    public static void querys(String dest, DataHandler handler, Object obj, String ... conditions) {
        service.querys(dest, handler, obj, conditions);
    }
    public static DataSet querys(String dest, PageNavi navi, Object obj, String ... conditions) {
        return service.querys(dest, navi, obj, conditions);
    }

    /**
     * 按条件查询
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    public static DataSet querys(String dest, long first, long last, Object obj, String ... conditions) {
        return service.querys(dest, first, last, obj, conditions);
    }
    public static DataRow query(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.query(dest, configs, obj, conditions);
    }
    public static DataRow query(ConfigStore configs, Object obj, String ... conditions) {
        return service.query(configs, obj, conditions);
    }
    public static DataRow query(ConfigStore configs, String ... conditions) {
        return service.query(configs, conditions);
    }
    public static DataRow query(String dest, Object obj, String ... conditions) {
        return service.query(dest, obj, conditions);
    }

    public static DataSet querys(String dest, ConfigStore configs, String ... conditions) {
        return service.querys(dest, configs, conditions);
    }
    public static DataSet querys(String dest, String ... conditions) {
        return service.querys(dest, conditions);
    }
    public static void querys(String dest, DataHandler handler, String ... conditions) {
        service.querys(dest, handler, conditions);
    }
    public static DataSet querys(String dest, PageNavi navi, String ... conditions) {
        return service.querys(dest, navi, conditions);
    }

    /**
     * 按条件查询
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    public static DataSet querys(String dest, long first, long last, String ... conditions) {
        return service.querys(dest, first, last, conditions);
    }

    public static DataSet querys(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.querys(dest, configs, obj, conditions);
    }
    public static DataSet querys(Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return service.querys(dest, first, last, configs, obj, conditions);
    }
    public static DataSet querys(Table dest, Object obj, String ... conditions) {
        return service.querys(dest, obj, conditions);
    }
    public static void querys(Table dest, DataHandler handler, Object obj, String ... conditions) {
        service.querys(dest, handler, obj, conditions);
    }
    public static DataSet querys(Table dest, PageNavi navi, Object obj, String ... conditions) {
        return service.querys(dest, navi, obj, conditions);
    }

    /**
     * 按条件查询
     * @param dest 			数据源(表或自定义SQL或SELECT语句)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param first 起 下标从0开始
     * @param last 止
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    public static DataSet querys(Table dest, long first, long last, Object obj, String ... conditions) {
        return service.querys(dest, first, last, obj, conditions);
    }

    public static DataSet querys(Table dest, ConfigStore configs, String ... conditions) {
        return service.querys(dest, configs, conditions);
    }
    public static DataSet querys(Table dest, long first, long last, ConfigStore configs, String ... conditions) {
        return service.querys(dest, first, last, configs, conditions);
    }
    public static DataSet querys(Table dest, String ... conditions) {
        return service.querys(dest, conditions);
    }
    public static void querys(Table dest, DataHandler handler, String ... conditions) {
       service.querys(dest, handler, conditions);
    }
    public static DataSet querys(Table dest, PageNavi navi, String ... conditions) {
        return service.querys(dest, navi, conditions);
    }
    public static DataSet querys(Table dest, long first, long last, String ... conditions) {
        return service.querys(dest, first, last, conditions);
    }
    public static DataSet querys(Table dest, DataHandler handler, long first, long last, String ... conditions) {
        return service.querys(dest, handler, first, last, conditions);
    }

    public static DataRow query(String dest, ConfigStore configs, String ... conditions) {
        return service.query(dest, configs, conditions);
    }
    public static DataRow query(String dest, String ... conditions) {
        return service.query(dest, conditions);
    }
    public static DataRow query(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.query(dest, configs, obj, conditions);
    }
    public static DataRow query(Table dest, Object obj, String ... conditions) {
        return service.query(dest, obj, conditions);
    }
    public static DataRow query(Table dest, ConfigStore configs, String ... conditions) {
        return service.query(dest, configs, conditions);
    }
    public static DataRow query(Table dest, String ... conditions) {
        return service.query(dest, conditions);
    }

    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions) {
        return service.selects(dest, clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, PageNavi navi, T entity, String ... conditions) {
        return service.selects(dest, clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, T entity, String ... conditions) {
        return service.selects(dest, clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
        return service.selects(dest, clazz, handler, entity, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, long first, long last, T entity, String ... conditions) {
        return service.selects(dest, clazz, first, last, entity, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions) {
        return (T)service.select(dest, clazz, configs, entity, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, T entity, String ... conditions) {
        return (T)service.selects(dest, clazz, entity, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
        return (T)service.selects(dest, clazz, handler, entity, conditions);
    }

    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
        return service.selects(dest, clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, PageNavi navi, String ... conditions) {
        return service.selects(dest, clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, String ... conditions) {
        return service.selects(dest, clazz, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, EntityHandler<T> handler, String ... conditions) {
        return service.selects(dest, clazz, handler, conditions);
    }
    public static <T> EntitySet<T> selects(String dest, Class<T> clazz, long first, long last, String ... conditions) {
        return service.selects(dest, clazz, first, last, conditions);
    }

    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions) {
        return service.selects(dest, clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, PageNavi navi, T entity, String ... conditions) {
        return service.selects(dest, clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, T entity, String ... conditions) {
        return service.selects(dest, clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
        return service.selects(dest, clazz, handler, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, long first, long last, T entity, String ... conditions) {
        return service.selects(dest, clazz, first, last, entity, conditions);
    }

    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
        return service.selects(dest, clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, PageNavi navi, String ... conditions) {
        return service.selects(dest, clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, String ... conditions) {
        return service.selects(dest, clazz, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, EntityHandler<T> handler, String ... conditions) {
        return service.selects(dest, clazz, handler, conditions);
    }
    public static <T> EntitySet<T> selects(Table dest, Class<T> clazz, long first, long last, String ... conditions) {
        return service.selects(dest, clazz, first, last, conditions);
    }

    public static <T> T select(String dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
        return (T)service.select(dest, clazz, configs, conditions);
    }
    public static <T> T select(String dest, Class<T> clazz, String ... conditions) {
        return (T)service.select(dest, clazz, conditions);
    }

    public static <T> T select(Table dest, Class<T> clazz, ConfigStore configs, T entity, String ... conditions) {
        return (T)service.select(dest, clazz, configs, entity, conditions);
    }
    public static <T> T select(Table dest, Class<T> clazz, T entity, String ... conditions) {
        return (T)service.select(dest, clazz, entity, conditions);
    }
    public static <T> T select(Table dest, Class<T> clazz, ConfigStore configs, String ... conditions) {
        return (T)service.select(dest, clazz, configs, conditions);
    }
    public static <T> T select(Table dest, Class<T> clazz, String ... conditions) {
        return (T)service.select(dest, clazz, conditions);
    }

    /**
     *
     * @param clazz 返回类型
     * @param configs 根据http等上下文构造查询条件
     * @param entity 根据entity的field/value构造简单的查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return EntitySet
     * @param <T> T
     */
    public static <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, T entity, String ... conditions) {
        return service.selects(clazz, configs, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, T entity, String ... conditions) {
        return service.selects(clazz, navi, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, T entity, String ... conditions) {
        return service.selects(clazz, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, EntityHandler<T> handler, T entity, String ... conditions) {
        return service.selects(clazz, handler, entity, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, long first, long last, T entity, String ... conditions) {
        return service.selects(clazz, first, last, entity, conditions);
    }
    public static <T> T select(Class<T> clazz, ConfigStore configs, T entity, String ... conditions) {
        return (T)service.select(clazz, configs, entity, conditions);
    }
    public static <T> T select(Class<T> clazz, T entity, String ... conditions) {
        return (T)service.select(clazz, entity, conditions);
    }

    public static <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, String ... conditions) {
        return service.selects(clazz, configs, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, String ... conditions) {
        return service.selects(clazz, navi, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, String ... conditions) {
        return service.selects(clazz, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, EntityHandler<T> handler, String ... conditions) {
        return service.selects(clazz, handler, conditions);
    }
    public static <T> EntitySet<T> selects(Class<T> clazz, long first, long last, String ... conditions) {
        return service.selects(clazz, first, last, conditions);
    }
    public static <T> T select(Class<T> clazz, ConfigStore configs, String ... conditions) {
        return (T)service.select(clazz, configs, conditions);
    }
    public static <T> T select(Class<T> clazz, String ... conditions) {
        return (T)service.select(clazz, conditions);
    }

    /**
     * 直接返回Map集合不封装, 不分页
     * @param dest			数据源(表或自定义SQL或SELECT语句)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return List
     */
    public static List<Map<String, Object>> maps(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.maps(dest, configs, conditions);
    }
    public static List<Map<String, Object>> maps(ConfigStore configs, Object obj, String ... conditions) {
        return service.maps(configs, obj, conditions);
    }
    public static List<Map<String, Object>> maps(ConfigStore configs, String ... conditions) {
        return service.maps(configs, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, Object obj, String ... conditions) {
        return service.maps(dest, obj, conditions);
    }
    public static void maps(String dest, DataHandler handler, Object obj, String ... conditions) {
        service.maps(dest, handler, obj, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, long first, long last, Object obj, String ... conditions) {
        return service.maps(dest, first, last, obj, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, ConfigStore configs, String ... conditions) {
        return service.maps(dest, configs, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, String ... conditions) {
        return service.maps(dest, conditions);
    }
    public static void maps(String dest, DataHandler handler, String ... conditions) {
        service.maps(dest, handler, conditions);
    }
    public static List<Map<String, Object>> maps(String dest, long first, long last, String ... conditions) {
        return service.maps(dest, first, last, conditions);
    }

    public static List<Map<String, Object>> maps(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.maps(dest, configs, obj, conditions);
    }
    public static void maps(Table dest, DataHandler handler, Object obj, String ... conditions) {
        service.maps(dest, handler, obj, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, Object obj, String ... conditions) {
        return service.maps(dest, obj, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, long first, long last, Object obj, String ... conditions) {
        return service.maps(dest, first, last, obj, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, ConfigStore configs, String ... conditions) {
        return service.maps(dest, configs, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, String ... conditions) {
        return service.maps(dest, conditions);
    }
    public static void maps(Table dest, DataHandler handler, String ... conditions) {
        service.maps(dest, handler, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, PageNavi navi, String ... conditions) {
        return service.maps(dest, navi, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, long first, long last, String ... conditions) {
        return service.maps(dest, first, last, conditions);
    }
    public static List<Map<String, Object>> maps(Table dest, DataHandler handler, long first, long last, String ... conditions) {
        return service.maps(dest, handler, first, last, conditions);
    }

    /**
     * 列名转找成参数名 可以给condition()提供参数用来接收前端参数
     * @param table 表
     * @return List
     */
    public static List<String> column2param(String table) {
        return service.column2param(table);
    }

    /**
     * 如果二级缓存开启 会从二级缓存中提取数据
     * @param cache			对应ehcache缓存配置文件 中的cache.name
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    public static DataSet caches(String cache, String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.caches(cache, dest, configs, obj, conditions);
    }
    public static DataSet caches(String cache, String dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return service.caches(cache, dest, first, last, configs, obj, conditions);
    }
    public static DataSet caches(String cache, String dest, Object obj, String ... conditions) {
        return service.caches(cache, dest, obj, conditions);
    }
    public static DataSet caches(String cache, String dest, long first, long last, Object obj, String ... conditions) {
        return service.caches(cache, dest, first, last, obj, conditions);
    }

    public static DataSet caches(String cache, Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.caches(cache, dest, configs, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return service.caches(cache, dest, first, last, configs, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, Object obj, String ... conditions) {
        return service.caches(cache, dest, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, Object obj, String ... conditions) {
        return service.caches(cache, dest, first, last, obj, conditions);
    }

    public static DataRow cache(String cache, String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.cache(cache, dest, configs, obj, conditions);
    }
    public static DataRow cache(String cache, String dest, Object obj, String ... conditions) {
        return service.cache(cache, dest, obj, conditions);
    }

    public static DataSet caches(String cache, String dest, ConfigStore configs, String ... conditions) {
        return service.caches(cache, dest, configs, conditions);
    }
    public static DataSet caches(String cache, String dest, String ... conditions) {
        return service.caches(cache, dest, conditions);
    }
    public static DataSet caches(String cache, String dest, long first, long last, String ... conditions) {
        return service.caches(cache, dest, first, last, conditions);
    }
    public static DataRow cache(String cache, String dest, ConfigStore configs, String ... conditions) {
        return service.cache(cache, dest, configs, conditions);
    }
    public static DataRow cache(String cache, String dest, String ... conditions) {
        return service.cache(cache, dest, conditions);
    }

    public static DataRow cache(String cache, Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.cache(cache, dest, configs, obj, conditions);
    }
    public static DataRow cache(String cache, Table dest, Object obj, String ... conditions) {
        return service.cache(cache, dest, obj, conditions);
    }
    public static DataSet caches(String cache, Table dest, ConfigStore configs, String ... conditions) {
        return service.caches(cache, dest, configs, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, ConfigStore configs, String ... conditions) {
        return service.caches(cache, dest, first, last, configs, conditions);
    }
    public static DataSet caches(String cache, Table dest, String ... conditions) {
        return service.caches(cache, dest, conditions);
    }
    public static DataSet caches(String cache, Table dest, long first, long last, String ... conditions) {
        return service.caches(cache, dest, first, last, conditions);
    }
    public static DataRow cache(String cache, Table dest, ConfigStore configs, String ... conditions) {
        return service.cache(cache, dest, configs, conditions);
    }
    public static DataRow cache(String cache, Table dest, String ... conditions) {
        return service.cache(cache, dest, conditions);
    }

    /*多表查询, 左右连接时使用*/
    public static DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        return service.querys(prepare, configs, obj, conditions);
    }
    public static DataSet querys(RunPrepare prepare, Object obj, String ... conditions) {
        return service.querys(prepare, obj, conditions);
    }
    public static void querys(RunPrepare prepare, StreamHandler handler, Object obj, String ... conditions) {
        service.querys(prepare, handler, obj, conditions);
    }
    public static DataSet querys(RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
        return service.querys(prepare, first, last, obj, conditions);
    }
    public static DataRow query(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        return service.query(prepare, configs, obj, conditions);
    }
    public static DataRow query(RunPrepare prepare, Object obj, String ... conditions) {
        return service.query(prepare, obj, conditions);
    }

    public static DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return service.querys(prepare, configs, conditions);
    }
    public static DataSet querys(RunPrepare prepare, String ... conditions) {
        return service.querys(prepare, conditions);
    }
    public static void querys(RunPrepare prepare, StreamHandler handler, String ... conditions) {
        service.querys(prepare, handler, conditions);
    }
    public static DataSet querys(RunPrepare prepare, long first, long last, String ... conditions) {
        return service.querys(prepare, first, last, conditions);
    }
    public static DataRow query(RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return service.query(prepare, configs, conditions);
    }
    public static DataRow query(RunPrepare prepare, String ... conditions) {
        return service.query(prepare, conditions);
    }
    public static void query(RunPrepare prepare, StreamHandler handler, String ... conditions) {
        service.query(prepare, handler, conditions);
    }

    public static DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        return service.caches(cache, prepare, configs, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, Object obj, String ... conditions) {
        return service.caches(cache, prepare, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, long first, long last, Object obj, String ... conditions) {
        return service.caches(cache, prepare, first, last, obj, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        return service.cache(cache, prepare, configs, obj, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, Object obj, String ... conditions) {
        return service.cache(cache, prepare, obj, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return service.caches(cache, prepare, configs, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, String ... conditions) {
        return service.caches(cache, prepare, conditions);
    }
    public static DataSet caches(String cache, RunPrepare prepare, long first, long last, String ... conditions) {
        return service.caches(cache, prepare, first, last, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return service.cache(cache, prepare, configs, conditions);
    }
    public static DataRow cache(String cache, RunPrepare prepare, String ... conditions) {
        return service.cache(cache, prepare, conditions);
    }

    public static DataSet caches(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return caches(null, dest, configs, obj, conditions);
    }
    public static DataSet caches(String dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return caches(null, dest, first, last, configs, obj, conditions);
    }
    public static DataSet caches(String dest, Object obj, String ... conditions) {
        return caches(null, dest, null, obj, conditions);
    }
    public static DataSet caches(String dest, long first, long last, Object obj, String ... conditions) {
        return caches(null, dest, first, last,obj, conditions);
    }

    public static DataSet caches(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return caches(null, dest, configs, obj, conditions);
    }
    public static DataSet caches(Table dest, long first, long last, ConfigStore configs, Object obj, String ... conditions) {
        return caches(null, dest, first, last, configs, obj, conditions);
    }
    public static DataSet caches(Table dest, Object obj, String ... conditions) {
        return caches(null, dest, null, obj, conditions);
    }
    public static DataSet caches(Table dest, long first, long last, Object obj, String ... conditions) {
        ConfigStore configs = new DefaultConfigStore(first, last);
        return caches(null, dest, configs, obj, conditions);
    }

    /**
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    public static DataRow cache(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return cache(null, dest, configs, obj, conditions);
    }
    public static DataRow cache(String dest, Object obj, String ... conditions) {
        return cache(null, dest, null, obj, conditions);
    }
    public static DataSet caches(String dest, ConfigStore configs, String ... conditions) {
        return caches(null, dest, configs, (Object) null, conditions);
    }
    public static DataSet caches(String dest, long first, long last, ConfigStore configs, String ... conditions) {
        return caches(null, dest, first, last, configs, conditions);
    }
    public static DataSet caches(String dest, String ... conditions) {
        return caches(null, dest, null, null, conditions);
    }
    public static DataSet caches(String dest, long first, long last, String ... conditions) {
        return caches(null, dest, first, last, null, conditions);
    }
    public static DataRow cache(String dest, ConfigStore configs, String ... conditions) {
        return cache(null, dest, configs, null, conditions);
    }
    public static DataRow cache(String dest, String ... conditions) {
        return cache(null, dest, null, null, conditions);
    }

    public static DataRow cache(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return cache(null, dest, configs, obj, conditions);
    }
    public static DataRow cache(Table dest, Object obj, String ... conditions) {
        return cache(null, dest, null, obj, conditions);
    }
    public static DataSet caches(Table dest, ConfigStore configs, String ... conditions) {
        return caches(null, dest, configs, (Object) null, conditions);
    }
    public static DataSet caches(Table dest, long first, long last, ConfigStore configs, String ... conditions) {
        return caches(null, dest, first, last, configs, conditions);
    }
    public static DataSet caches(Table dest, String ... conditions) {
        return caches(null, dest, null, null, conditions);
    }
    public static DataSet caches(Table dest, long first, long last, String ... conditions) {
        return caches(null, dest, first, last, null, conditions);
    }
    public static DataRow cache(Table dest, ConfigStore configs, String ... conditions) {
        return cache(null, dest, configs, null, conditions);
    }
    public static DataRow cache(Table dest, String ... conditions) {
        return cache(null, dest, null, null, conditions);
    }
    /**
     * 删除缓存 参数保持与查询参数完全一致
     * @param channel 		channel
     * @param dest 			查询或操作的目标(表、存储过程、SQL等)
     * @param configs  		根据http等上下文构造查询条件
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return boolean
     */
    public static boolean removeCache(String channel, String dest, ConfigStore configs, String ... conditions) {
        return service.removeCache(channel, dest, configs, conditions);
    }
    public static boolean removeCache(String channel, String dest, String ... conditions) {
        return service.removeCache(channel, dest, conditions);
    }

    public static boolean removeCache(String channel, String dest, long first, long last, String ... conditions) {
        return service.removeCache(channel, dest, first, last, conditions);
    }
    public static boolean removeCache(String channel, Table dest, ConfigStore configs, String ... conditions) {
        return service.removeCache(channel, dest, configs, conditions);
    }
    public static boolean removeCache(String channel, Table dest, String ... conditions) {
        return service.removeCache(channel, dest, conditions);
    }
    public static boolean removeCache(String channel, Table dest, long first, long last, String ... conditions) {
        return service.removeCache(channel, dest, first, last, conditions);
    }

    /**
     * 清空缓存
     * @param channel channel
     * @return boolean
     */
    public static boolean clearCache(String channel) {
        return service.clearCache(channel);
    }

    /* *****************************************************************************************************************
     * 													EXISTS
     ******************************************************************************************************************/

    /**
     * 是否存在
     * @param dest  			数据源(表或自定义SQL或SELECT语句)
     * @param configs  		根据http等上下文构造查询条件
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return boolean
     */
    public static boolean exists(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.exists(dest, configs, obj, conditions);
    }
    public static boolean exists(String dest, Object obj, String ... conditions) {
        return service.exists(dest, obj, conditions);
    }
    public static boolean exists(String dest, ConfigStore configs, String ... conditions) {
        return service.exists(dest, configs, conditions);
    }
    public static boolean exists(String dest, String ... conditions) {
        return service.exists(dest, conditions);
    }
    public static boolean exists(String dest, DataRow row) {
        return service.exists(dest, row);
    }
    public static boolean exists(DataRow row) {
        return service.exists(row);
    }
    public static boolean exists(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.exists(dest, configs, obj, conditions);
    }
    public static boolean exists(Table dest, Object obj, String ... conditions) {
        return service.exists(dest, obj, conditions);
    }
    public static boolean exists(Table dest, ConfigStore configs, String ... conditions) {
        return service.exists(dest, configs, conditions);
    }
    public static boolean exists(Table dest, String ... conditions) {
        return service.exists(dest, conditions);
    }
    public static boolean exists(Table dest, DataRow row) {
        return service.exists(dest, row);
    }

    /* *****************************************************************************************************************
     * 													COUNT
     ******************************************************************************************************************/
    public static long count(String dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.count(dest, configs, obj, conditions);
    }
    public static long count(String dest, Object obj, String ... conditions) {
        return service.count(dest, obj, conditions);
    }
    public static long count(String dest, ConfigStore configs, String ... conditions) {
        return service.count(dest, configs, conditions);
    }
    public static long count(String dest, String ... conditions) {
        return service.count(dest, conditions);
    }

    public static long count(Table dest, ConfigStore configs, Object obj, String ... conditions) {
        return service.count(dest, configs, obj, conditions);
    }
    public static long count(Table dest, Object obj, String ... conditions) {
        return service.count(dest, obj, conditions);
    }
    public static long count(Table dest, ConfigStore configs, String ... conditions) {
        return service.count(dest, configs, conditions);
    }
    public static long count(Table dest, String ... conditions) {
        return service.count(dest, conditions);
    }

    public static long count(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        return service.count(prepare, configs, obj, conditions);
    }
    public static long count(RunPrepare prepare, Object obj, String ... conditions) {
        return count(prepare, null, obj, conditions);
    }
    public static long count(RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return count(prepare, configs, null, conditions);
    }
    public static long count(RunPrepare prepare, String ... conditions) {
        return count(prepare, null, null, conditions);
    }

    /* *****************************************************************************************************************
     * 													EXECUTE
     ******************************************************************************************************************/

    /**
     * 执行SQL文件
     * @param file 文件内容
     * @param charset 文件编码
     * @param brk 失败后是否中断
     * @return 成功数量,失败数量
     */
    public static int[] execute(File file, Charset charset, boolean brk) {
        return service.execute(file, charset, brk);
    }

    public static int[] execute(File file, boolean brk) {
        return execute(file, StandardCharsets.UTF_8, brk);
    }

    public static int[] execute(String txt, boolean brk) {
        return service.execute(txt, brk);
    }

    /**
     * 执行
     * @param dest  查询或操作的目标(表、存储过程、SQL等)
     * @param configs  configs
     * @param conditions  conditions
     * @return int
     */
    public static long execute(String dest, ConfigStore configs, String ... conditions) {
        return service.execute(dest, configs, conditions);
    }
    public static long execute(String dest, String ... conditions) {
        return service.execute(dest, conditions);
    }

    /**
     * 执行存储过程
     * @param procedure  procedure
     * @param inputs  inputs
     * @return boolean
     */
    public static boolean executeProcedure(String procedure, String... inputs) {
        return service.executeProcedure(procedure, inputs);
    }
    public static boolean execute(Procedure procedure, String... inputs) {
        return service.execute(procedure, inputs);
    }

    /**
     * 执行命令<br/>
     * execute(10, sql, List&lt;List&lt;Object&gt;&gt;);<br/>
     * 批量执行要求SQL一样
     * @param batch 大于1时表示批量执行
     * @param sql sql
     * @param values 如果是批量执行，需要提供一个二维数组
     * @return 影响行数批量执行时不准确
     */
    public static long execute(int batch, String sql, Collection<Object> values){
        return service.execute(batch, sql, values);
    }

    /**
     * 执行命令<br/>
     * execute(10, sql, List&lt;List&lt;Object&gt;&gt;);<br/>
     * 批量执行要求SQL一样
     * @param batch 大于1时表示批量执行
     * @param vol 每行多少个占位符
     * @param sql sql
     * @param values 如果是批量执行，需要提供一个二维数组
     * @return 影响行数批量执行时不准确
     */
    public static long execute(int batch, int vol, String sql, Collection<Object> values){
        return service.execute(batch, vol, sql, values);
    }

    /**
     * 根据存储过程查询
     * @param procedure  procedure
     * @param first  first
     * @param last  last
     * @param inputs  inputs
     * @return DataSet
     */
    public static DataSet querysProcedure(String procedure, long first, long last, String ... inputs) {
        return service.querysProcedure(procedure, first, last, inputs);
    }
    public static DataSet querysProcedure(String procedure, PageNavi navi, String ... inputs) {
        return service.querysProcedure(procedure, navi, inputs);
    }
    public static DataSet querysProcedure(String procedure, String ... inputs) {
        return service.querysProcedure(procedure, inputs);
    }
    public static DataSet querys(Procedure procedure, long first, long last, String ... inputs) {
        return service.querys(procedure, first, last, inputs);
    }
    public static DataSet querys(Procedure procedure, PageNavi navi, String ... inputs) {
        return service.querys(procedure, navi, inputs);
    }

    public static DataRow queryProcedure(String procedure, String ... inputs) {
        return service.queryProcedure(procedure, inputs);
    }

    public static DataRow query(Procedure procedure, String ... inputs) {
        return service.query(procedure, inputs);
    }

    /* *****************************************************************************************************************
     * 													DELETE
     ******************************************************************************************************************/
    public static long truncate(String table) {
        return service.truncate(table);
    }
    public static long truncate(Table table) {
        return service.truncate(table);
    }
    public static long delete(String table, ConfigStore configs, String ... conditions) {
        return service.delete(table, configs, conditions);
    }
    public static long delete(ConfigStore configs, String ... conditions) {
        return service.delete(configs, conditions);
    }
    public static long delete(ConfigStore configs) {
        return service.delete(configs);
    }
    public static long delete(Table dest, ConfigStore configs, String ... conditions) {
        return service().delete(dest, configs, conditions);
    }

    /**
     * 删除 根据columns列删除 可设置复合主键
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param set 数据
     * @param columns 生成删除条件的列, 如果不设置则根据主键删除
     * @return 影响行数
     */
    public static long delete(String dest, DataSet set, String ... columns) {
        return service.delete(dest, set, columns);
    }
    public static long delete(Table dest, DataSet set, String ... columns) {
        return service.delete(dest, set, columns);
    }
    public static long delete(DataSet set, String ... columns) {
        return service.delete(set, columns);
    }
    public static long delete(String dest, DataRow row, String ... columns) {
        return service.delete(dest, row, columns);
    }
    public static long delete(Table dest, DataRow row, String ... columns) {
        return service.delete(dest, row, columns);
    }

    /**
     * 根据columns列删除
     * @param obj obj
     * @param columns 生成删除条件的列, 如果不设置则根据主键删除
     * @return 影响行数
     */
    public static long delete(Object obj, String ... columns) {
        return service.delete(obj, columns);
    }

    /**
     * 根据多列条件删除 delete("user","type","1","age:20");
     * @param table 表
     * @param kvs key-value
     * @return 影响行数
     */
    public static long delete(String table, String ... kvs) {
        return service.delete(table, kvs);
    }
    public static long delete(Table table, String ... kvs) {
        return service.delete(table, kvs);
    }

    public static long delete(Table table, Collection values){
        return service.delete(table, values);
    }
    public static long delete(String table, Collection values){
        return service.delete(table, values);
    }

    /**
     * 根据一列的多个值删除多行
     * @param table 表
     * @param key 名
     * @param values 值集合
     * @return 影响行数
     */
    public static long deletes(String table, String key, Collection<Object> values) {
        return service.deletes(table, key, values);
    }
    public static long deletes(Table table, String key, Collection<Object> values) {
        return service.deletes(table, key, values);
    }

    public static long deletes(int batch, String table, String key, Collection<Object> values) {
        return service.deletes(batch, table, key, values);
    }
    public static long deletes(int batch, Table table, String key, Collection<Object> values) {
        return service.deletes(batch, table, key, values);
    }

    /**
     * 根据一列的多个值删除多行
     * @param table 表
     * @param key 名
     * @param values 值集合
     * @return 影响行数
     */
    public static <T> long deletes(String table, String key, T ... values) {
        return service.deletes(table, key, values);
    }

    public static <T> long deletes(Table table, String key, T ... values) {
        return service.deletes(table, key, values);
    }

    public static <T> long deletes(int batch, String table, String key, T ... values) {
        return service.deletes(batch, table, key, values);
    }

    public static <T> long deletes(int batch, Table table, String key, T ... values) {
        return service.deletes(batch, table, key, values);
    }

    /* *****************************************************************************************************************
     * 													METADATA
     ******************************************************************************************************************/
    public static List<String> tables(Catalog catalog, Schema schema, String name, int types) {
        return service.tables(catalog, schema, name, types);
    }
    public static List<String> tables(Schema schema, String name, int types) {
        return service.tables(schema, name, types);
    }
    public static List<String> tables(String name, int types) {
        return service.tables(name, types);
    }
    public static List<String> tables(String name) {
        return service.tables(name);
    }
    public static List<String> tables(int types) {
        return service.tables(types);
    }
    public static List<String> tables() {
        return service.tables();
    }

    public static List<String> masters(Catalog catalog, Schema schema, String name, int types) {
        return service.masters(catalog, schema, name, types);
    }
    public static List<String> masters(Schema schema, String name, int types) {
        return service.masters(schema, name, types);
    }
    public static List<String> masters(String name, int types) {
        return service.masters(name, types);
    }
    public static List<String> masters(int types) {
        return service.masters(types);
    }
    public static List<String> masters() {
        return service.masters();
    }

    public static List<String> columns(Table table) {
        return service.columns(table);
    }
    public static List<String> columns(String table) {
        return service.columns(table);
    }
    public static List<String> columns(Catalog catalog, Schema schema, String table) {
        return service.columns(catalog, schema, table);
    }

    public static List<String> tags(Table table) {
        return service.tags(table);
    }
    public static List<String> tags(String table) {
        return service.tags(table);
    }
    public static List<String> tags(Catalog catalog, Schema schema, String table) {
        return service.tags(catalog, schema, table);
    }

    /**
     * 根据差异生成SQL
     * @param differ differ
     * @return runs
     */
    public static List<Run> ddl(MetadataDiffer differ) {
        return service.ddl(differ);
    }

    /**
     * 根据差异生成SQL
     * @param differs differs
     * @return runs
     */
    public static List<Run> ddl(List<MetadataDiffer> differs) {
        return service.ddl(differs);
    }

    /**
     * 根据结果集对象获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param sql sql
     * @param comment 是否需要列注释
     * @param condition 是否需要拼接查询条件, 如果需要会拼接where 1=0 条件(默认不添加，通常情况下SQL自带查询条件，给参数赋值NULL达到相同的效果)
     * @return LinkedHashMap
     */
    public static LinkedHashMap<String, Column> metadata(String sql, boolean comment, boolean condition){
        return service.metadata(sql, comment,condition);
    }
    public static  LinkedHashMap<String, Column> metadata(String sql) {
        return metadata(sql, false, false);
    }

    public static AnylineService.DDLService ddl() {
        return service.ddl();
    }
    public static AnylineService.MetaDataService metadata() {
        return service.metadata();
    }
}
