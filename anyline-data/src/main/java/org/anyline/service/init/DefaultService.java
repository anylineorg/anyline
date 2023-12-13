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


package org.anyline.service.init;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.dao.AnylineDao;
import org.anyline.data.cache.CacheUtil;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.init.DefaultSQLStore;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.metadata.*;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;
@Primary
@Service("anyline.service")
public class DefaultService<E> implements AnylineService<E> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    @Qualifier("anyline.dao")
    protected AnylineDao dao;

    public String datasource() {
        return dao.runtime().datasource();
    }
    protected static CacheProvider cacheProvider;

    public CacheProvider getCacheProvider() {
        return cacheProvider;
    }

    @Autowired(required = false)
    public void setCacheProvider(CacheProvider cacheProvider) {
        DefaultService.cacheProvider = cacheProvider;
    }

    public AnylineDao getDao() {
        return dao;
    }

    public AnylineService setDao(AnylineDao dao) {
        this.dao = dao;
        return this;
    }


    /**
     * 按条件查询
     *
     * @param src        数据源(表｜视图｜函数｜自定义SQL | SELECT语句)
     * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return DataSet
     */
    
    @Override 
    public DataSet querys(String src, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).querys(ps[1], configs, obj, conditions);
        }
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        configs = append(configs, obj);
        return queryFromDao(src, configs, conditions);
    }

    @Override 
    public List<String> column2param(String table) {
        List<String> columns = columns(table);
        return EntityAdapterProxy.column2param(columns);
    }

    
    @Override 
    public List<Map<String, Object>> maps(String src, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).maps(ps[1], configs, obj, conditions);
        }
        List<Map<String, Object>> maps = null;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        try {
            RunPrepare prepare = createRunPrepare(src);
            configs = append(configs, obj);
            if(null != prepare.getRuntime()){
                maps = ServiceProxy.service(prepare.getRuntime()).getDao().maps(prepare, configs, conditions);
            }else {
                maps = dao.maps(prepare, configs, conditions);
            }
        } catch (Exception e) {
            maps = new ArrayList<Map<String, Object>>();
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return maps;
    }
    
    @Override 
    public DataSet caches(String cache, String src, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).caches(cache, ps[1], configs, obj, conditions);
        }
        DataSet set = null;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        if (null == cache || ConfigTable.IS_CACHE_DISABLED) {
            set = querys(src, append(configs, obj), conditions);
        } else {
            if (null != cacheProvider) {
                set = queryFromCache(cache, src, configs, conditions);
            } else {
                set = querys(src, configs, conditions);
            }
        }
        return set;
    }


    @Override 
    public DataRow query(String src, ConfigStore store, Object obj, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new DefaultConfigStore();
        }
        store.setPageNavi(navi);
        DataSet set = querys(src, store, obj, conditions);
        if (null != set && set.size() > 0) {
            DataRow row = set.getRow(0);
            return row;
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }

    @Override 
    public BigDecimal sequence(boolean next, String name) {
        DataRow row = sequences(next, name);
        if (null != row) {
            return row.getDecimal(name, (BigDecimal) null);
        }
        return null;
    }

    
    @Override 
    public DataRow sequences(boolean next, String... names) {
        return dao.sequence(next, names);
    }
 
    
    @Override 
    public DataRow cache(String cache, String src, ConfigStore configs, Object obj, String... conditions) {
        // 是否启动缓存
        if (null == cache || null == cacheProvider || ConfigTable.IS_CACHE_DISABLED) {
            return query(src, configs, obj, conditions);
        }
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        configs = append(configs, obj);
        configs.setPageNavi(navi);

        DataRow row = null;
        String key = "ROW:";

        if (cache.contains(":")) {
            String ks[] = BeanUtil.parseKeyValue(cache);
            cache = ks[0];
            key += ks[1] + ":";
        }
        key += CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
        if (null != cacheProvider) {
            CacheElement cacheElement = cacheProvider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataRow) {
                    row = (DataRow) cacheValue;
                    row.setIsFromCache(true);
                    return row;
                } else {
                    log.error("[缓存设置错误,检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
            }
        }
        // 调用实际 的方法
        row = query(src, configs, obj, conditions);
        if (null != row && null != cacheProvider) {
            cacheProvider.put(cache, key, row);
        }
        if (null == row && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            row = new DataRow();
        }
        return row;
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        return selectFromDao(clazz, append(configs, entity), conditions);
    }
    @Override 
    public <T> T select(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        configs.setPageNavi(navi);
        EntitySet<T> list = selects(clazz, configs, entity, conditions);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            try {
                return (T) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).selects(ps[1], clazz, configs, entity, conditions);
        }
        return queryFromDao(src, clazz, append(configs, entity), conditions);
    }

    
    @Override 
    public <T> T select(String src, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        configs.setPageNavi(navi);
        EntitySet<T> list = selects(src, clazz, configs, entity, conditions);
        if (null != list && list.size() > 0) {
            return list.get(0);
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            try {
                return (T) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    /**
     * 解析泛型class
     *
     * @return class
     */
    protected Class<E> parseGenericClass() {
        Type type = null;
        Class<E> clazz = null;
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof ParameterizedType) {
            type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
        if (type instanceof ParameterizedType) {
            clazz = (Class<E>) ((ParameterizedType) type).getRawType();
        } else {
            clazz = (Class<E>) type;
        }
        return clazz;
    }

    
    @Override 
    public EntitySet<E> gets(ConfigStore configs, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return selects(clazz, configs, conditions);
    }

    
    @Override 
    public E get(ConfigStore configs, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return select(clazz, configs, conditions);
    }


    /**
     * 按条件查询
     *
     * @param prepare    构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs    根据http等上下文构造查询条件
     * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return DataSet
     */
    
    @Override 
    public DataSet querys(RunPrepare prepare, ConfigStore configs, Object obj, String... conditions) {
        conditions = BasicUtil.compress(conditions);
        DataSet set = queryFromDao(prepare, append(configs, obj), conditions);
        return set;

    }
    
    @Override 
    public DataSet caches(String cache, RunPrepare table, ConfigStore configs, Object obj, String... conditions) {
        DataSet set = null;
        conditions = BasicUtil.compress(conditions);
        if (null == cache) {
            set = querys(table, configs, obj, conditions);
        } else {
            if (null != cacheProvider) {
                //TODO
                //set = queryFromCache(cache, table, configs, conditions);
            } else {
                set = querys(table, configs, obj, conditions);
            }
        }
        return set;
    }

    
    @Override 
    public DataRow query(RunPrepare table, ConfigStore store, Object obj, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new DefaultConfigStore();
        }
        store.setPageNavi(navi);
        DataSet set = querys(table, store, obj, conditions);
        if (null != set && set.size() > 0) {
            DataRow row = set.getRow(0);
            return row;
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }


    /**
     *
     * @param cache 缓存 channel
     * @param table
     * @param configs 过滤条件及相关配置
     * @param obj 根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions  简单过滤条件
     * @return DataRow
     */
    @Override 
    public DataRow cache(String cache, RunPrepare table, ConfigStore configs, Object obj, String... conditions) {
        // 是否启动缓存
        if (null == cache) {
            return query(table, configs, obj, conditions);
        }
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        configs.setPageNavi(navi);
        configs = append(configs, obj);
        DataRow row = null;
        String key = "ROW:";

        if (cache.contains(":")) {
            String ks[] = BeanUtil.parseKeyValue(cache);
            cache = ks[0];
            key += ks[1] + ":";
        }
        key += CacheUtil.createCacheElementKey(true, true, table.getTable(), configs, conditions);
        if (null != cacheProvider) {
            CacheElement cacheElement = cacheProvider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataRow) {
                    row = (DataRow) cacheValue;
                    row.setIsFromCache(true);
                    return row;
                } else {
                    log.error("[缓存设置错误,检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
            }
        }
        // 调用实际 的方法
        row = query(table, configs, conditions);
        if (null != row && null != cacheProvider) {
            cacheProvider.put(cache, key, row);
        }
        if (null == row && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            row = new DataRow();
        }
        return row;
    }


    /**
     * 删除缓存 参数保持与查询参数完全一致
     *
     * @param channel    channel
     * @param src        src
     * @param configs    configs
     * @param conditions conditions
     * @return boolean
     */
    
    @Override 
    public boolean removeCache(String channel, String src, ConfigStore configs, String... conditions) {
        if (null != cacheProvider) {
            src = BasicUtil.compress(src);
            conditions = BasicUtil.compress(conditions);
            String key = CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
            cacheProvider.remove(channel, "SET:" + key);
            cacheProvider.remove(channel, "ROW:" + key);

            DefaultPageNavi navi = new DefaultPageNavi();
            navi.setFirstRow(0);
            navi.setLastRow(0);
            navi.setCalType(1);
            if (null == configs) {
                configs = new DefaultConfigStore();
            }
            configs.setPageNavi(navi);
            key = CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
            cacheProvider.remove(channel, "ROW:" + key);
        }
        return true;
    }


    /**
     * 清空缓存
     *
     * @param channel channel
     * @return boolean
     */

    @Override
    public boolean clearCache(String channel) {
        if (null != cacheProvider) {
            return cacheProvider.clear(channel);
        } else {
            return false;
        }
    }
    @Override
    public boolean clearCaches() {
        if (null != cacheProvider) {
            return cacheProvider.clears();
        } else {
            return false;
        }
    }


    /**
     * 是否存在
     *
     * @param src        src
     * @param configs    根据http等上下文构造查询条件
     * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return boolean
     */

    @Override 
    public boolean exists(String src, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).exists(ps[1], configs, obj, conditions);
        }
        boolean result = false;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(src);
        if(null != prepare.getRuntime()) {
            result = ServiceProxy.service(prepare.getRuntime()).getDao().exists(prepare, append(configs, obj), conditions);
        }else {
            result = dao.exists(prepare, append(configs, obj), conditions);
        }
        return result;
    }

    /**
     * 只根据主键判断
     */
    
    @Override 
    public boolean exists(String src, DataRow row) {
        if (null != row) {
            List<String> keys = row.getPrimaryKeys();
            if (null != keys) {
                String[] conditions = new String[keys.size()];
                int idx = 0;
                for (String key : keys) {
                    conditions[idx++] = key + ":" + row.getString(key);
                }
                return exists(src, null, conditions);
            }
            return false;
        } else {
            return false;
        }
    }


    /* *****************************************************************************************************************
     * 													COUNT
     ******************************************************************************************************************/
    /**
     * count
     * @param src 表或视图或自定义SQL
     * @param configs 过滤条件
     * @param obj 根据obj生成的过滤条件
     * @param conditions 简单过滤条件
     * @return long
     */
    @Override 
    public long count(String src, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).count(ps[1], configs, obj, conditions);
        }
        long count = -1;
        try {
            // conditions = parseConditions(conditions);
            src = BasicUtil.compress(src);
            conditions = BasicUtil.compress(conditions);
            RunPrepare prepare = createRunPrepare(src);
            if(null != prepare.getRuntime()){
                count = ServiceProxy.service(prepare.getRuntime()).getDao().count(prepare, append(configs, obj), conditions);
            }else {
                count = dao.count(prepare, append(configs, obj), conditions);
            }
        } catch (Exception e) {
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return count;
    }

    /* *****************************************************************************************************************
     * 													INSERT
     ******************************************************************************************************************/
    /**
     * 插入数据
     * @param batch 批量执行每批最多数量
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data entity或list或DataRow或DataSet重复
     * @param columns 需要插入哪些列
     * @return 影响行数
     */
    @Override
    public long insert(int batch, String dest, Object data,  ConfigStore configs, List<String> columns) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).insert(batch, ps[1], data, configs, columns);
        }
        return dao.insert(batch, dest, data, configs, columns);
    }

    /* *****************************************************************************************************************
     * 													UPDATE
     ******************************************************************************************************************/
    /**
     * 更新记录
     * 默认情况下以主键为更新条件,需在更新的数据保存在data中
     * 如果提供了dest则更新dest表,如果没有提供则根据data解析出表名
     * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键,则根据TYPE_CODE更新
     * 可以提供了ConfigStore以实现更复杂的更新条件
     * 需要更新的列通过fixs/columns提供
     * @param columns 需要更新的列
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data    更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
     * @param configs 更新条件
     * @return int 影响行数
     */
    @Override
    public long update(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).update(batch, ps[1], data, configs, columns);
        }
        dest = DataSourceUtil.parseDataSource(dest, data);
        return dao.update(batch, dest, data, configs, columns);
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
     * @param batch 批量执行每批最多数量
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data  数据
     * @param columns 指定更新或保存的列
     * @return 影响行数
     */
    @Override 
    public long save(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        if (null == data) {
            return 0;
        }
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).save(ps[1], data, configs, columns);
        }
        if(data instanceof DataSet){
            DataSet set = (DataSet) data;
            long cnt = 0;
            DataSet inserts = new DataSet();
            DataSet updates = new DataSet();
            for(DataRow row:set){
                Boolean override = row.getOverride();
                if(null != override) {
                    //如果设置了override需要到数据库中实际检测
                    boolean exists = exists(dest, row);
                    if(exists){
                        if(!override){//忽略

                        }else{//覆盖(更新)
                            updates.add(row);
                        }
                    }else{
                        inserts.add(row);
                    }
                }else {
                    if (row.isNew()) {
                        inserts.add(row);
                    } else {
                        updates.add(row);
                    }
                }
            }
            if(!inserts.isEmpty()){
                cnt += insert(batch, dest, inserts, configs, columns);
            }
            if(!updates.isEmpty()){
                cnt += update(batch, dest, updates, configs, columns);
            }
            return cnt;
        }else if (data instanceof Collection) {
            Collection objs = (Collection) data;
            long cnt = 0;
            List<Object> inserts = new ArrayList<>();
            List<Object> updates = new ArrayList<>();
            for (Object obj : objs) {
                if(BeanUtil.checkIsNew(obj)){
                    inserts.add(obj);
                }else{
                    updates.add(obj);
                }
            }
            if(!inserts.isEmpty()){
                cnt += insert(batch, dest, inserts, configs, columns);
            }
            if(!updates.isEmpty()){
                cnt += update(batch, dest, updates, configs, columns);
            }
            return cnt;
        }
        return saveObject(dest, data, configs, columns);
    }


    protected long saveObject(String dest, Object data, ConfigStore configs, List<String> columns) {
        if (BasicUtil.isEmpty(dest)) {
            if (data instanceof DataRow || data instanceof DataSet) {
                dest = DataSourceUtil.parseDataSource(dest, data);
            } else {
                dest = EntityAdapterProxy.table(data.getClass(), true);
            }
        }
        return dao.save(dest, data, configs, columns);
    }

    protected long saveObject(String dest, Object data, ConfigStore configs, String... columns) {
        return saveObject(dest, data, configs, BeanUtil.array2list(columns));
    }

    
    @Override 
    public boolean execute(Procedure procedure, String... inputs) {
        procedure.setName(DataSourceUtil.parseDataSource(procedure.getName(), null));
        if (null != inputs) {
            for (String input : inputs) {
                procedure.addInput(input);
            }
        }
        return dao.execute(procedure);
    }


    /**
     * 根据存储过程查询
     *
     * @param procedure procedure
     * @return DataSet
     */
    
    @Override 
    public DataSet querys(Procedure procedure, PageNavi navi, String... inputs) {
        DataSet set = null;
        try {
            procedure.setName(DataSourceUtil.parseDataSource(procedure.getName()));
            if (null != inputs) {
                for (String input : inputs) {
                    procedure.addInput(input);
                }
            }
            set = dao.querys(procedure, navi);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return set;
    }

    @Override 
    public DataRow query(Procedure procedure, String... inputs) {
        DataSet set = querys(procedure, 0, 0, inputs);
        if (set.size() > 0) {
            return set.getRow(0);
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }

    public long execute(int batch, String sql, List<Object> values){
        dao.execute(batch, sql, values);
        return 0;
    }
    @Override 
    public long execute(String src, ConfigStore store, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).execute(ps[1], store, conditions);
        }
        long result = -1;
        src = BasicUtil.compress(src);
        src = DataSourceUtil.parseDataSource(src);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(src);
        if (null == prepare) {
            return result;
        }
        result = dao.execute(prepare, store, conditions);
        return result;
    }


    @SuppressWarnings("rawtypes")
    
    @Override 
    public long delete(String dest, DataSet set, String... columns) {
        long cnt = 0;
        int size = set.size();
        for (int i = 0; i < size; i++) {
            cnt += delete(dest, set.getRow(i), columns);
        }
        log.info("[delete DataSet][影响行数:{}]", LogUtil.format(cnt, 34));
        return cnt;
    }

    
    @Override 
    public long delete(String dest, DataRow row, String... columns) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).delete(ps[1], row, columns);
        }
        return dao.delete(dest, row, columns);
    }

    
    @Override 
    public long delete(Object obj, String... columns) {
        if (null == obj) {
            return 0;
        }
        String dest = null;
        if (obj instanceof DataRow) {
            DataRow row = (DataRow) obj;
            dest = DataSourceUtil.parseDataSource(null, row);
            return dao.delete(dest, row, columns);
        } else {
            if (obj instanceof Collection) {
                Collection list =((Collection) obj);
                if(!list.isEmpty()) {
                    Class clazz = list.iterator().next().getClass();
                    dest = EntityAdapterProxy.table(clazz, true);
                }
            } else {
                    dest = EntityAdapterProxy.table(obj.getClass(), true);
            }
            if(null != dest) {
                return dao.delete(dest, obj, columns);
            }

        }
        return 0;
    }

    
    @Override 
    public long delete(String table, String... kvs) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).delete(ps[1], kvs);
        }
        DataRow row = DataRow.parseArray(kvs);
        row.setPrimaryKey(row.keys());
        return dao.delete(table, row);
    }

    
    @Override 
    public <T> long deletes(int batch, String table, String key, Collection<T> values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).deletes(batch, ps[1], key, values);
        }
        return dao.deletes(batch, table, key, values);
    }

    
    @Override 
    public <T> long deletes(int batch, String table, String key, T... values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).deletes(batch, ps[1], key, values);
        }
        return dao.deletes(batch , table, key, values);
    }

    
    @Override 
    public long delete(String table, ConfigStore configs, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).delete(ps[1], configs, conditions);
        }
        return dao.delete(table, configs, conditions);
    }

    
    @Override 
    public long truncate(String table) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]){
            return ServiceProxy.service(ps[0]).truncate(ps[1]);
        }
        return dao.truncate(table);
    }

    protected PageNavi setPageLazy(String src, ConfigStore configs, String... conditions) {
        PageNavi navi = null;
        String lazyKey = null;
        if (null != configs) {
            navi = configs.getPageNavi();
            if (null != navi && navi.isLazy()) {
                lazyKey = CacheUtil.createCacheElementKey(false, false, src, configs, conditions);
                navi.setLazyKey(lazyKey);
                long total = PageLazyStore.getTotal(lazyKey, navi.getLazyPeriod());
                navi.setTotalRow(total);
            }
        }
        return navi;
    }

    protected DataSet queryFromDao(RunPrepare prepare, ConfigStore configs, String... conditions) {
        DataSet set = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][src:{}]", prepare.getText());
        }
        try {
            setPageLazy(prepare.getText(), configs, conditions);
            set = dao.querys(prepare, configs, conditions);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return set;
    }

    protected DataSet queryFromDao(String src, ConfigStore configs, String... conditions) {
        DataSet set = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][src:{}]", src);
        }
        try {
            setPageLazy(src, configs, conditions);
            RunPrepare prepare = createRunPrepare(src);

            set = dao.querys(prepare, configs, conditions);

        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return set;
    }

    protected <T> EntitySet<T> queryFromDao(String src, Class<T> clazz, ConfigStore configs, String... conditions) {
        EntitySet<T> list = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][src:{}]", clazz);
        }
        try {
            setPageLazy(src, configs, conditions);
            RunPrepare prepare = createRunPrepare(src);
            list = dao.selects(prepare, clazz, configs, conditions);
        } catch (Exception e) {
            list = new EntitySet<>();
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return list;
    }

    protected <T> EntitySet<T> selectFromDao(Class<T> clazz, ConfigStore configs, String... conditions) {
        EntitySet<T> list = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][src:{}]", clazz);
        }
        try {
            setPageLazy(clazz.getName(), configs, conditions);
            list = dao.selects(null, clazz, configs, conditions);
        } catch (Exception e) {
            list = new EntitySet<>();
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return list;
    }

    @Override 
    public ConfigStore condition() {
        return new DefaultConfigStore();
    }

    /**
     * 解析SQL中指定的主键table(col1,col2)&lt;pk1,pk2&gt;
     *
     * @param src src
     * @param pks pks
     * @return String
     */
    protected String parsePrimaryKey(String src, List<String> pks) {
        if (src.endsWith(">")) {
            int fr = src.lastIndexOf("<");
            int to = src.lastIndexOf(">");
            if (fr != -1) {
                String pkstr = src.substring(fr + 1, to);
                src = src.substring(0, fr);
                String[] tmps = pkstr.split(",");
                for (String tmp : tmps) {
                    pks.add(tmp);
                    if (ConfigTable.isSQLDebug()) {
                        log.debug("[解析SQL主键] [KEY:{}]", tmp);
                    }
                }
            }
        }
        return src;
    }

    protected RunPrepare createRunPrepare(String src) {
        RunPrepare prepare = null;
        src = src.trim();
        List<String> pks = new ArrayList<>();
        // 文本sql
        //if (src.startsWith("${") && src.endsWith("}")) {
        if(BasicUtil.checkEl(src)){
            if (ConfigTable.isSQLDebug()) {
                log.debug("[解析SQL类型] [类型:{JAVA定义}] [src:{}]", src);
            }
            src = src.substring(2, src.length() - 1);
            src = DataSourceUtil.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            prepare = new DefaultTextPrepare(src);
        } else {
            src = DataSourceUtil.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            if (src.replace("\n", "").replace("\r", "").trim().matches("^[a-zA-Z]+\\s+.+")) {
                if (ConfigTable.isSQLDebug()) {
                    log.debug("[解析SQL类型] [类型:JAVA定义] [src:{}]", src);
                }
                prepare = new DefaultTextPrepare(src);
            } else if (RegularUtil.match(src, RunPrepare.XML_SQL_ID_STYLE)) {
                /* XML定义 */
                if (ConfigTable.isSQLDebug()) {
                    log.debug("[解析SQL类型] [类型:XML定义] [src:{}]", src);
                }
                prepare = DefaultSQLStore.parseSQL(src);
                if (null == prepare) {
                    log.error("[解析SQL类型][XML解析失败][src:{}]", src);
                }
            } else {
                /* 自动生成 */
                if (ConfigTable.isSQLDebug()) {
                    log.debug("[解析SQL类型] [类型:auto] [src:{}]", src);
                }
                prepare = new DefaultTablePrepare();
                prepare.setDataSource(src);
            }
        }
        if (null != prepare && pks.size() > 0) {
            prepare.setPrimaryKey(pks);
        }
        return prepare;
    }

    protected DataSet queryFromCache(String cache, String src, ConfigStore configs, String... conditions) {
        if (ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
            log.debug("[cache from][cache:{}][src:{}]", cache, src);
        }
        DataSet set = null;
        String key = "SET:";
        if (cache.contains(">")) {
            String tmp[] = cache.split(">");
            cache = tmp[0];
        }
        if (cache.contains(":")) {
            String ks[] = BeanUtil.parseKeyValue(cache);
            cache = ks[0];
            key += ks[1] + ":";
        }
        key += CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
        RunPrepare prepare = createRunPrepare(src);
        if (null != cacheProvider) {
            CacheElement cacheElement = cacheProvider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataSet) {
                    set = (DataSet) cacheValue;
                    set.setIsFromCache(true);
                } else {
                    log.error("[缓存设置错误,检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
//       	// 开启新线程提前更新缓存(90%时间)
                long age = (System.currentTimeMillis() - cacheElement.getCreateTime()) / 1000;
                final int _max = cacheElement.getExpires();
                if (age > _max * 0.9) {
                    if (ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
                        log.debug("[缓存即将到期提前刷新][src:{}] [生存:{}/{}]", src, age, _max);
                    }
                    final String _key = key;
                    final String _cache = cache;
                    final RunPrepare _sql = prepare;
                    final ConfigStore _configs = configs;
                    final String[] _conditions = conditions;
                    new Thread(new Runnable() {
                        @Override  
                        public void run() {
                            CacheUtil.start(_key, _max / 10);
                            DataSet newSet = dao.querys(_sql, _configs, _conditions);
                            cacheProvider.put(_cache, _key, newSet);
                            CacheUtil.stop(_key, _max / 10);
                        }
                    }).start();
                }

            } else {
                setPageLazy(src, configs, conditions);
                set = dao.querys(prepare, configs, conditions);
                cacheProvider.put(cache, key, set);
            }
        }
        return set;
    }

    private ConfigStore append(ConfigStore configs, Object entity) {
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        if (null != entity) {
            if (entity instanceof Map) {
                Map map = (Map) entity;
                for (Object key : map.keySet()) {
                    Object value = map.get(key);
                    if (value instanceof Collection) {
                        configs.ands(key.toString(), value);
                    } else {
                        configs.and(key.toString(), value);
                    }
                }
            } else {
                List<Field> fields = ClassUtil.getFields(entity.getClass(), false, false);
                for (Field field : fields) {
                    Object value = BeanUtil.getFieldValue(entity, field);
                    if (BasicUtil.isNotEmpty(true, value)) {
                        String key = field.getName();
                        key = EntityAdapterProxy.column(entity.getClass(), field, true);
                        if (BasicUtil.isEmpty(key)) {
                            continue;
                        }
                        if (value instanceof Collection) {
                            configs.ands(key, value);
                        } else {
                            configs.and(key, value);
                        }
                    }
                }
            }
        }
        return configs;
    }

    /**
     * 根据sql获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param sql sql
     * @param comment 是否需要列注释
     * @param condition 是否需要拼接查询条件,如果需要会拼接where 1=0 条件
     * @return LinkedHashMap
     */
    public LinkedHashMap<String,Column> metadata(String sql, boolean comment, boolean condition){
        if(condition){
            String up = sql.toUpperCase().replace("\n"," ").replace("\t","");
            String key = " WHERE ";
            boolean split = false;
            if(up.contains(key)){
                int idx = sql.lastIndexOf(key);
                sql = sql.substring(0, idx) + " WHERE 1=0 AND " + sql.substring(idx + key.length());
                split = true;
            }else{
                key = " GROUP ";
                if(up.contains(key)){
                    int idx = sql.lastIndexOf(key);
                    sql = sql.substring(0, idx) + " WHERE 1=0 GROUP " + sql.substring(idx + key.length());
                    split = true;
                }else{
                    key = " ORDER ";
                    if(up.contains(key)){
                        int idx = sql.lastIndexOf(key);
                        sql = sql.substring(0, idx) + " WHERE 1=0 ORDER " + sql.substring(idx + key.length());
                        split = true;
                    }
                }
            }
            if(!split){
                sql = sql + " WHERE 1=0";
            }
        }
        RunPrepare prepare = createRunPrepare(sql);
        LinkedHashMap<String,Column> metadata = dao.metadata(prepare, comment);
        return metadata;
    }
    @Override 
    public List<String> tables(Catalog catalog, Schema schema, String name, String types) {
        LinkedHashMap<String, Table> tables = metadata.tables(catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (Table table : tables.values()) {
            list.add(table.getName());
        }
        return list;
    }


    
    @Override 
    public List<String> views(boolean greedy, Catalog catalog, Schema schema, String name, String types) {
        LinkedHashMap<String, View> views = metadata.views(greedy, catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (View view : views.values()) {
            list.add(view.getName());
        }
        return list;
    }

    
    @Override 
    public List<String> mtables(boolean greedy, Catalog catalog, Schema schema, String name, String types) {
        LinkedHashMap<String, MasterTable> tables = metadata.mtables(greedy, catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (MasterTable table : tables.values()) {
            list.add(table.getName());
        }
        return list;
    }


    @Override
    public List<String> columns(boolean greedy, Table table) {
        LinkedHashMap<String, Column> columns = metadata.columns(greedy, table);
        List<String> list = new ArrayList<>();
        for (Column column : columns.values()) {
            list.add(column.getName());
        }
        return list;
    }

    
    @Override 
    public List<String> tags(boolean greedy, Table table) {
        LinkedHashMap<String, Tag> tags = metadata.tags(greedy, table);
        List<String> list = new ArrayList<>();
        for (Tag tag : tags.values()) {
            list.add(tag.getName());
        }
        return list;
    }

    /**
     * 修改表结构
     *
     * @param table 表
     * @throws Exception 异常 SQL异常
     */
    @Override
    public boolean save(Table table) throws Exception {
        return ddl.save(table);
    }

    /**
     * 修改列  名称 数据类型 位置 默认值
     * 执行save前先调用column.update()设置修改后的属性
     * column.update().setName().setDefaultValue().setAfter()....
     *
     * @param column 列
     * @throws Exception 异常 SQL异常
     */
    @Override 
    public boolean save(Column column) throws Exception {
        return ddl.save(column);
    }

    @Override 
    public boolean drop(Table table) throws Exception {
        return ddl.drop(table);
    }

    @Override 
    public boolean drop(Column column) throws Exception {
        return ddl.drop(column);
    }


    @Override 
    public MetaDataService metadata() {
        return metadata;
    }

    @Override 
    public DDLService ddl() {
        return ddl;
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

    public MetaDataService metadata = new MetaDataService() {

        /* *****************************************************************************************************************
         * 													database
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String,Database> databases();
         ******************************************************************************************************************/

        @Override
        public LinkedHashMap<String, Database> databases(String name) {
            return dao.databases(name);
        }
        @Override
        public List<Database> databases(boolean greedy, String name) {
            return dao.databases(greedy, name);
        }
        @Override
        public Database database(String name) {
            return dao.database(name);
        }
        /* *****************************************************************************************************************
         * 													catalog
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String,Database> databases();
         ******************************************************************************************************************/
        @Override
        public LinkedHashMap<String, Catalog> catalogs(String name) {
            return dao.catalogs(name);
        }
        @Override
        public List<Catalog> catalogs(boolean greedy, String name) {
            return dao.catalogs(greedy, name);
        }
        /* *****************************************************************************************************************
         * 													schema
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String, Schema> schemas(Catalog catalog, String name)
         ******************************************************************************************************************/
        @Override
        public LinkedHashMap<String, Schema> schemas(Catalog catalog, String name) {
            return dao.schemas(catalog, name);
        }
        @Override
        public List<Schema> schemas(boolean greedy, Catalog catalog, String name) {
            return dao.schemas(greedy, catalog, name);
        }

        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(Table table)
         * LinkedHashMap<String, Table> tables(Catalog catalog, Schema schema, String name, String types)
         * LinkedHashMap<String, Table> tables(Schema schema, String name, String types)
         * LinkedHashMap<String, Table> tables(String name, String types)
         * LinkedHashMap<String, Table> tables(String types)
         * LinkedHashMap<String, Table> tables()
         * Table table(Catalog catalog, Schema schema, String name)
         * Table table(Schema schema, String name)
         * Table table(String name)
         ******************************************************************************************************************/
        
        @Override
        public boolean exists(boolean greedy, Table table) {
            if (null != table(greedy, table.getCatalog(), table.getSchema(), table.getName(), false)) {
                return true;
            }
            return false;
        }

        @Override
        public <T extends Table>  List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, String types, boolean strut) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().tables(greedy, catalog, schema, ps[1], types, strut);
            }
            if(null == types){
                types = "TABLE";
            }
            return dao.tables(greedy, catalog, schema, name, types, strut);
        }
        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, String types, boolean strut) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().tables(catalog, schema, ps[1], types, strut);
            }
            if(null == types){
                types = "TABLE";
            }
            return dao.tables(catalog, schema, name, types, strut);
        }


        private void struct(Table table){
            ddl(table);
            LinkedHashMap<String, Column> columns = table.getColumns();
            if(null == columns || columns.size() == 0) {//上一步ddl是否加载过以下内容
                columns = columns(table);
                table.setColumns(columns);
                table.setTags(tags(table));
                PrimaryKey pk = primary(table);
                if (null != pk) {
                    for (String col : pk.getColumns().keySet()) {
                        Column column = columns.get(col.toUpperCase());
                        if (null != column) {
                            column.primary(true);
                        }
                    }
                }
                table.setPrimaryKey(pk);
                table.setIndexs(indexs(table));
            }
        }
        @Override
        public Table table(boolean greedy, Catalog catalog, Schema schema, String name, boolean struct) {
            Table table = null;
            List<Table> tables = tables(greedy, catalog, schema, name, null);
            if (tables.size() > 0) {
                table = tables.get(0);
                if(null != table && struct) {
                    ddl(table);
                    LinkedHashMap<String, Column> columns = table.getColumns();
                    if(null == columns || columns.size() == 0) {//上一步ddl是否加载过以下内容
                        struct(table);
                    }
                }
            }
            return table;
        }
        @Override
        public Table table(Catalog catalog, Schema schema, String name, boolean struct) {
            Table table = null;
            LinkedHashMap<String, Table> tables = tables(catalog, schema, name, null);
            if (tables.size() > 0) {
                table = tables.values().iterator().next();
                if(null != table && struct) {
                   struct(table);
                }
            }
            return table;
        }

        @Override
        public List<String> ddl(Table table, boolean init) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(table, init);
            }
            return dao.ddl(table, init);
        }

        /* *****************************************************************************************************************
         * 													view
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(View view)
         * LinkedHashMap<String,View> views(Catalog catalog, Schema schema, String name, String types)
         * LinkedHashMap<String,View> views(Schema schema, String name, String types)
         * LinkedHashMap<String,View> views(String name, String types)
         * LinkedHashMap<String,View> views(String types)
         * LinkedHashMap<String,View> views()
         * View view(Catalog catalog, Schema schema, String name)
         * View view(Schema schema, String name)
         * View view(String name)
         ******************************************************************************************************************/

        
        @Override
        public boolean exists(boolean greedy, View view) {
            if (null != view(greedy, view.getCatalog(), view.getSchema(), view.getName())) {
                return true;
            }
            return false;
        }

        @Override
        public <T extends View> LinkedHashMap<String, T> views(boolean greedy, Catalog catalog, Schema schema, String name, String types) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().views(greedy, catalog, schema, ps[1],types);
            }
            return dao.views(greedy, catalog, schema, name, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, String types) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().views(catalog, schema, ps[1],types);
            }
            return dao.views(false, catalog, schema, name, types);
        }


        @Override
        public View view(boolean greedy, Catalog catalog, Schema schema, String name) {
            View view = null;
            LinkedHashMap<String, View> views = views(greedy, catalog, schema, name, null);
            if (views.size() > 0) {
                view = views.values().iterator().next();
                view.setColumns(columns(view));
                ddl(view);
                //view.setTags(tags(view));
                //view.setIndexs(indexs(view));
            }
            return view;
        }

        @Override
        public List<String> ddl(View view) {
            String[] ps = DataSourceUtil.parseRuntime(view.getName());
            if(null != ps[0]){
                view.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(view);
            }
            return dao.ddl(view);
        }
        /* *****************************************************************************************************************
         * 													master table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(MasterTable table)
         * LinkedHashMap<String, MasterTable> mtables(Catalog catalog, Schema schema, String name, String types)
         * LinkedHashMap<String, MasterTable> mtables(Schema schema, String name, String types)
         * LinkedHashMap<String, MasterTable> mtables(String name, String types)
         * LinkedHashMap<String, MasterTable> mtables(String types)
         * LinkedHashMap<String, MasterTable> mtables()
         * MasterTable mtable(Catalog catalog, Schema schema, String name)
         * MasterTable mtable(Schema schema, String name)
         * MasterTable mtable(String name)
         ******************************************************************************************************************/


        @Override
        public boolean exists(boolean greedy, MasterTable table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().exists(greedy, table);
            }
            MasterTable tab = mtable(greedy, table.getCatalog(), table.getSchema(), table.getName(), false);
            return null != tab;
        }



        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, Catalog catalog, Schema schema, String name, String types) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().mtables(greedy, catalog, schema, ps[1], types);
            }
            return dao.mtables(greedy, catalog, schema, name, types);
        }

        @Override
        public MasterTable mtable(boolean greedy, Catalog catalog, Schema schema, String name, boolean strut) {
            LinkedHashMap<String, MasterTable> tables = mtables(greedy, catalog, schema, name, "STABLE");
            if (tables.size() == 0) {
                return null;
            }
            MasterTable table = tables.values().iterator().next();
            table.setColumns(columns(table));
            table.setTags(tags(table));
            table.setIndexs(indexs(table));
            return table;
        }


        @Override
        public List<String> ddl(MasterTable table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(table);
            }
            return dao.ddl(table);
        }
        /* *****************************************************************************************************************
         * 													partition  table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(PartitionTable table)
         * LinkedHashMap<String, PartitionTable> ptables(Catalog catalog, Schema schema, String name, String types)
         * LinkedHashMap<String, PartitionTable> ptables(Schema schema, String name, String types)
         * LinkedHashMap<String, PartitionTable> ptables(String name, String types)
         * LinkedHashMap<String, PartitionTable> ptables(String types)
         * LinkedHashMap<String, PartitionTable> ptables()
         * LinkedHashMap<String, PartitionTable> ptables(MasterTable master)
         * PartitionTable ptable(Catalog catalog, Schema schema, String name)
         * PartitionTable ptable(Schema schema, String name)
         * PartitionTable ptable(String name)
         ******************************************************************************************************************/

        
        @Override
        public boolean exists(boolean greedy, PartitionTable table) {
            PartitionTable tab = ptable(greedy, table.getCatalog(), table.getSchema(), table.getMasterName(), table.getName());
            return null != tab;
        }


        @Override
        public boolean exists(PartitionTable table) {
            return exists(false, table);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String, Object> tags) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]){
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ptables(greedy, master, tags);
            }
            return dao.ptables(greedy, master, tags);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]){
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ptables(greedy, master, tags, name);
            }
            return dao.ptables(greedy, master, tags, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(Catalog catalog, Schema schema, String master, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().ptables(catalog, schema, master, name);
            }
            return dao.ptables(false, catalog, schema, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(Schema schema, String master, String name) {
            return ptables(false, null, schema, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master, String name) {
            return ptables(false, null, null, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master) {
            return ptables(false, null, null, master, null);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master) {
            return dao.ptables(false, master);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String, Object> tags) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]){
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ptables(master, tags);
            }
            return dao.ptables(false, master, tags);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String, Object> tags, String name) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]){
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ptables(master, tags, name);
            }
            return dao.ptables(false, master, tags, name);
        }


        @Override
        public PartitionTable ptable(boolean greedy, MasterTable master, String name) {
            LinkedHashMap<String, PartitionTable> tables = ptables(greedy, master, name);
            if (tables.size() == 0) {
                return null;
            }
            PartitionTable table = tables.values().iterator().next();
            table.setColumns(columns(table));
            table.setTags(tags(table));
            table.setIndexs(indexs(table));
            return table;
        }


        @Override
        public List<String> ddl(PartitionTable table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(table);
            }
            return dao.ddl(table);
        }
        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(Column column);
         * boolean exists(Table table, String name);
         * boolean exists(String table, String name);
         * boolean exists(Catalog catalog, Schema schema, String table, String name);
         * LinkedHashMap<String,Column> columns(Table table)
         * LinkedHashMap<String,Column> columns(String table)
         * LinkedHashMap<String,Column> columns(Catalog catalog, Schema schema, String table)
         * LinkedHashMap<String,Column> column(Table table, String name);
         * LinkedHashMap<String,Column> column(String table, String name);
         * LinkedHashMap<String,Column> column(Catalog catalog, Schema schema, String table, String name);
         ******************************************************************************************************************/
        @Override
        public boolean exists(boolean greedy, Table table, Column column) {
            try {
                String name = column.getName().toUpperCase();
                LinkedHashMap<String, Column> columns = null;
                if(null != table) {
                  columns = table.getColumns();
                }else{
                    table = column.getTable(true);
                    if(null == table){
                        String tableName = column.getTableName(true);
                        if(BasicUtil.isNotEmpty(tableName)){
                            table = new Table(column.getCatalog(), column.getSchema(), tableName);
                        }
                    }
                }
                if (null == columns || columns.isEmpty()) {
                    if(null != table) {
                        columns = columns(greedy, table);
                    }
                }
                if (null != columns && columns.containsKey(name)){
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }


        @Override
        public <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema){
           return dao.columns(greedy, catalog, schema);
        }
        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().columns(greedy, table);
            }
            LinkedHashMap<String, T> columns = dao.columns(greedy, table);
            return columns;
        }


        @Override
        public Column column(boolean greedy, Table table, String name) {
            Column column = null;
            LinkedHashMap<String, Column> columns = table.getColumns();
            if (null == columns && columns.isEmpty()) {
                columns = columns(greedy, table);
            }
            column = columns.get(name.toUpperCase());
            return column;
        }

        /* *****************************************************************************************************************
         * 													tag
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String,Tag> tags(Catalog catalog, Schema schema, String table)
         * LinkedHashMap<String,Tag> tags(String table)
         * LinkedHashMap<String,Tag> tags(Table table)
         ******************************************************************************************************************/

        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().tags(greedy, table);
            }
            return dao.tags(greedy, table);
        }


        /* *****************************************************************************************************************
         * 													primary
         * -----------------------------------------------------------------------------------------------------------------
         * PrimaryKey primary(Table table)
         * PrimaryKey primary(String table)
         * PrimaryKey primary(Catalog catalog, Schema schema, String table)
         ******************************************************************************************************************/

        @Override
        public PrimaryKey primary(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().primary(table);
            }
            return dao.primary(greedy, table);
        }


        @Override
        public PrimaryKey primary(boolean greedy, String table) {
                return primary(greedy, new Table(table));
            }


        @Override
        public PrimaryKey primary(boolean greedy, Catalog catalog, Schema schema, String table) {
            return primary(greedy, new Table(catalog, schema, table));
        }


        @Override
        public PrimaryKey primary(Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().primary(table);
            }
            return dao.primary(false, table);
        }


        @Override
        public PrimaryKey primary(String table) {
                return primary(false, new Table(table));
            }


        @Override
        public PrimaryKey primary(Catalog catalog, Schema schema, String table) {
            return primary(false, new Table(catalog, schema, table));
        }

        /* *****************************************************************************************************************
         * 													foreign
         ******************************************************************************************************************/
        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().foreigns( greedy, table);
            }
            return dao.foreigns(greedy, table);
        }
        @Override
        public ForeignKey foreign(boolean greedy, Table table, List<String> columns) {
            if(null == columns || columns.isEmpty()){
                return null;
            }
            LinkedHashMap<String, ForeignKey> foreigns = foreigns(greedy, table);
             Collections.sort(columns);
            String id = BeanUtil.concat(columns).toUpperCase();
            for(ForeignKey foreign:foreigns.values()){
                List<String> fcols = BeanUtil.getMapKeys(foreign.getColumns());
                Collections.sort(fcols);
                if(id.equals(BeanUtil.concat(fcols).toUpperCase())){
                    return foreign;
                }
            }
            return null;
        }



        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String, Index> indexs(Table table)
         * LinkedHashMap<String, Index> indexs(String table)
         * LinkedHashMap<String, Index> indexs(Catalog catalog, Schema schema, String table)
         * Index index(Table table, String name);
         * Index index(String table, String name);
         * Index index(String name);
         ******************************************************************************************************************/

        @Override
        public <T extends Index> List<T> indexs(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().indexs(greedy, table);
            }
            return dao.indexs(greedy, table);
        }

        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().indexs(table);
            }
            return dao.indexs(table);
        }

        @Override
        public Index index(boolean greedy, Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().index(greedy, table, name);
            }
            Index index = null;
            List<Index> all = dao.indexs(greedy, table, name);
            if (null != all && !all.isEmpty()) {
                index = all.get(0);
            }
            return index;
        }

        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String,Constraint> constraints(Table table)
         * LinkedHashMap<String,Constraint> constraints(String table)
         * LinkedHashMap<String,Constraint> constraints(Catalog catalog, Schema schema, String table)
         ******************************************************************************************************************/

        @Override
        public <T extends Constraint> List<T> constraints(boolean greedy, Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().constraints(greedy, table, name);
            }
            return dao.constraints(greedy, table);
        }

        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().constraints(table, name);
            }
            return dao.constraints(table);
        }



        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(Column column, String name) {
            return dao.constraints(column, name);
        }


        @Override
        public Constraint constraint(boolean greedy, Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().constraint(greedy, table, name);
            }
            List<Constraint> constraints = constraints(greedy, table, name);
            if (null != constraints && !constraints.isEmpty()) {
                return constraints.get(0);
            }
            return null;
        }



        /* *****************************************************************************************************************
         * 													trigger
         ******************************************************************************************************************/


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]){
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().triggers(greedy, table, events);
            }
            return dao.triggers(greedy, table, events);
        }


        @Override
        public Trigger trigger(boolean greedy, Catalog catalog, Schema schema, String name) {
            LinkedHashMap<String, Trigger> triggers = triggers(greedy, new Table(catalog, schema, null), null);
            if(null != triggers){
                return triggers.get(name.toUpperCase());
            }
            return null;
        }


        /* *****************************************************************************************************************
         * 													procedure
         ******************************************************************************************************************/
        @Override
        public <T extends Procedure> List<T> procedures(boolean greedy, Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().procedures(greedy, catalog, schema, ps[0]);
            }
            return dao.procedures(greedy, catalog, schema, name);
        }
        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().procedures(catalog, schema, ps[0]);
            }
            return dao.procedures(catalog, schema, name);
        }
        @Override
        public Procedure procedure(boolean greedy, Catalog catalog, Schema schema, String name) {
            List<Procedure> procedures = procedures(greedy, catalog, schema, name);
            if(null != procedures && !procedures.isEmpty()){
                return procedures.get(0);
            }
            return null;
        }

        @Override
        public List<String> ddl(Procedure procedure) {
            String[] ps = DataSourceUtil.parseRuntime(procedure.getName());
            if(null != ps[0]){
                procedure.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(procedure);
            }
            return dao.ddl(procedure);
        }

        /* *****************************************************************************************************************
         * 													function
         ******************************************************************************************************************/
        @Override
        public <T extends Function> List<T> functions(boolean greedy, Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().functions(greedy, catalog, schema, ps[0]);
            }
            return dao.functions(greedy, catalog, schema, name);
        }
        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]){
                return ServiceProxy.service(ps[0]).metadata().functions(catalog, schema, ps[0]);
            }
            return dao.functions(catalog, schema, name);
        }
        @Override
        public Function function(boolean greedy, Catalog catalog, Schema schema, String name) {
            List<Function> functions = functions(greedy, catalog, schema, name);
            if(null != functions && !functions.isEmpty()){
                return functions.get(0);
            }
            return null;
        }
        @Override
        public List<String> ddl(Function function) {
            String[] ps = DataSourceUtil.parseRuntime(function.getName());
            if(null != ps[0]){
                function.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(function);
            }
            return dao.ddl(function);
        }
    };
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
	 * function         : 函数
	 ******************************************************************************************************************/

    public DDLService ddl = new DDLService() {
        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
		 * boolean save(Table table) throws Exception
		 * boolean create(Table table) throws Exception
		 * boolean alter(Table table) throws Exception
         * boolean drop(Table table) throws Exception
         ******************************************************************************************************************/

        
        @Override
        public boolean save(Table table) throws Exception{
            boolean result = false;
            CacheProxy.clear();
            Table otable = metadata.table(table.getCatalog(), table.getSchema(), table.getName());
            if(null != otable){
                otable.setAutoDropColumn(table.isAutoDropColumn());
                Table update = (Table)table.getUpdate();
                if(null == update){
                    update = table;
                }
                otable.setUpdate(update, false, false);
                result = alter(otable);
            }else{
                result =  create(table);
            }
            CacheProxy.clear();
            return result;
        }
        
        @Override
        public boolean create(Table table) throws Exception{
            boolean result =  dao.create(table);
            return result;
        }
        
        @Override
        public boolean alter(Table table) throws Exception{
            CacheProxy.clear();
            Table update = (Table) table.getUpdate();
            if(null == update){
                update = table;
                table = metadata().table(table.getCatalog(), table.getSchema(), table.getName());
                table.setUpdate(update, false, false);
            } 
            boolean result = dao.alter(table);
            CacheProxy.clear();
            return result;
        }


        @Override
        public boolean drop(Table table) throws Exception{
            boolean result = dao.drop(table);
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean rename(Table origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }

        /* *****************************************************************************************************************
         * 													view
         * -----------------------------------------------------------------------------------------------------------------
         * boolean save(View view) throws Exception
         * boolean create(View view) throws Exception
         * boolean alter(View view) throws Exception
         * boolean drop(View view) throws Exception
         ******************************************************************************************************************/

        
        @Override
        public boolean save(View view) throws Exception{
            boolean result = false;
            CacheProxy.clear();
            View oview = metadata.view(view.getCatalog(), view.getSchema(), view.getName());
            if(null != oview){
                oview.setAutoDropColumn(view.isAutoDropColumn());
                View update = (View)view.getUpdate();
                if(null == update){
                    update = view;
                }
                oview.setUpdate(update, false, false);
                result = alter(oview);
            }else{
                result =  create(view);
            }

            CacheProxy.clear();
            return result;
        }
        
        @Override
        public boolean create(View view) throws Exception{
            boolean result =  dao.create(view);
            return result;
        }
        
        @Override
        public boolean alter(View view) throws Exception{
            CacheProxy.clear();
            boolean result = dao.alter(view);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean drop(View view) throws Exception{
            boolean result = dao.drop(view);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(View origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }
        /* *****************************************************************************************************************
         * 													master table
         * -----------------------------------------------------------------------------------------------------------------
		 * boolean save(MasterTable master) throws Exception
		 * boolean create(MasterTable master) throws Exception
		 * boolean alter(MasterTable master) throws Exception
         * boolean drop(MasterTable master) throws Exception
         ******************************************************************************************************************/

        @Override
        public boolean save(MasterTable table) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            MasterTable otable = metadata.mtable(table.getCatalog(), table.getSchema(), table.getName());
            if(null != otable){
                otable.setUpdate(table, false, false);
                result = alter(otable);
            }else{
                result =  create(table);
            }

            CacheProxy.clear();
            return result;
        }
        
        @Override
        public boolean create(MasterTable table) throws Exception {
            boolean result =  dao.create(table);
            return result;
        }

        
        @Override
        public boolean alter(MasterTable table) throws Exception {
            CacheProxy.clear();
            boolean result = dao.alter(table);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean drop(MasterTable table) throws Exception {
            boolean result = dao.drop(table);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(MasterTable origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }

        /* *****************************************************************************************************************
         * 													partition table
         * -----------------------------------------------------------------------------------------------------------------
		 * boolean save(PartitionTable table) throws Exception
		 * boolean create(PartitionTable table) throws Exception
		 * boolean alter(PartitionTable table) throws Exception
         * boolean drop(PartitionTable table) throws Exception
         ******************************************************************************************************************/

        @Override
        public boolean save(PartitionTable table) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            PartitionTable otable = metadata.ptable(table.getCatalog(), table.getSchema(), table.getMasterName(), table.getName());
            if(null != otable){
                otable.setUpdate(table,false, false);
                result = alter(otable);
            }else{
                result =  create(table);
            }

            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean create(PartitionTable table) throws Exception {
            boolean result =  dao.create(table);
            return result;
        }

        
        @Override
        public boolean alter(PartitionTable table) throws Exception {
            CacheProxy.clear();
            boolean result = dao.alter(table);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean drop(PartitionTable table) throws Exception {
            boolean result = dao.drop(table);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(PartitionTable origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }


        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
		 * boolean save(Column column) throws Exception
		 * boolean add(Column column) throws Exception
		 * boolean alter(Column column) throws Exception
         * boolean drop(Column column) throws Exception
         *
         * private boolean add(LinkedHashMap<String, Column> columns, Column column) throws Exception
         * private boolean alter(Table table, Column column) throws Exception
         ******************************************************************************************************************/

        /**
         * 修改列  名称 数据类型 位置 默认值
         * 执行save前先调用column.update()设置修改后的属性
         * column.update().setName().setDefaultValue().setAfter()....
         * @param column 列
         * @throws Exception 异常 SQL异常
         */

        
        @Override
        public boolean save(Column column) throws Exception{
            boolean result = false;
            CacheProxy.clear();
            Table table = metadata.table(column.getCatalog(), column.getSchema(), column.getTableName(true));
            if(null == table){
                throw new AnylineException("表不存在:"+column.getTableName(true));
            }
            LinkedHashMap<String, Column> columns = table.getColumns();
            Column original = columns.get(column.getName().toUpperCase());
            if(null == original){
                result = add(columns, column);
            }else {
                result = alter(table, column);
            }
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean add(Column column) throws Exception{
            CacheProxy.clear();
            LinkedHashMap<String, Column> columns = metadata.columns(column.getCatalog(), column.getSchema(), column.getTableName(true));
            boolean result = add(columns, column);
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean alter(Column column) throws Exception{
            CacheProxy.clear();
            Table table = metadata.table(column.getCatalog(), column.getSchema(), column.getTableName(true));
            boolean result = alter(table, column);
            CacheProxy.clear();
            return result;
        }


        @Override
        public boolean drop(Column column) throws Exception{
            boolean result = dao.drop(column);
            CacheProxy.clear();
            return result;
        }

        private boolean add(LinkedHashMap<String, Column> columns, Column column) throws Exception{
            CacheProxy.clear();
            boolean result =  dao.add(column);
            if(result) {
                columns.put(column.getName(), column);
            }
            CacheProxy.clear();
            return result;
        }
        /**
         * 修改列
         * @param table 表
         * @param column 修改目标
         * @return boolean
         * @throws Exception 异常 sql异常
         */
        private boolean alter(Table table, Column column) throws Exception{
            boolean result = false;
            CacheProxy.clear();
            LinkedHashMap<String, Column> columns = table.getColumns();
            Column original = columns.get(column.getName().toUpperCase());

            Column update = column.getUpdate();
            if(null == update){
                update = column.clone();
            }
            original.setUpdate(update, false, false);
            String name = original.getName();
            try {
                result = dao.alter(table, original);
            }finally {
                original.setName(name);
            }
            if(result) {
                columns.remove(original.getName());

                BeanUtil.copyFieldValueWithoutNull(original, update);
                original.setUpdate(update, false, false);
                BeanUtil.copyFieldValue(column, original);
                column.setUpdate(update, false, false);
                columns.put(original.getName(), original);
            }
            column.setTable(table);
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean rename(Column origin, String name) throws Exception{
            origin.setNewName(name);
            boolean result = alter(origin);//dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }

        /* *****************************************************************************************************************
         * 													tag
         * -----------------------------------------------------------------------------------------------------------------
		 * boolean save(Tag tag) throws Exception
		 * boolean add(Tag tag) throws Exception
		 * boolean alter(Tag tag) throws Exception
         * boolean drop(Tag tag) throws Exception
         *
         * private boolean add(LinkedHashMap<String, Tag> tags, Tag tag) throws Exception
         * private boolean alter(Table table, Tag tag) throws Exception
         ******************************************************************************************************************/

        /**
         * 修改列  名称 数据类型 位置 默认值
         * 执行save前先调用tag.update()设置修改后的属性
         * tag.update().setName()
         * @param tag 标签
         * @throws Exception 异常 SQL异常
         */


        @Override
        public boolean save(Tag tag) throws Exception{
            boolean result = false;
            CacheProxy.clear();
            Table table = metadata.table(tag.getCatalog(), tag.getSchema(), tag.getTableName(true));
            if(null == table){
                throw new AnylineException("表不存在:"+tag.getTableName(true));
            }
            LinkedHashMap<String, Tag> tags = table.getTags();
            Tag original = tags.get(tag.getName().toUpperCase());
            if(null == original){
                result = add(tags, tag);
            }else {
                result = alter(table, tag);
            }
            CacheProxy.clear();
            return result;
        }


        
        @Override
        public boolean add(Tag tag) throws Exception{
            CacheProxy.clear();
            LinkedHashMap<String, Tag> tags = metadata.tags(tag.getCatalog(), tag.getSchema(), tag.getTableName(true));
            boolean result = add(tags, tag);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean alter(Tag tag) throws Exception{
            CacheProxy.clear();
            Table table = metadata.table(tag.getCatalog(), tag.getSchema(), tag.getTableName(true));
            boolean result = alter(table, tag);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean drop(Tag tag) throws Exception{
            boolean result = dao.drop(tag);
            CacheProxy.clear();
            return result;
        }
        private boolean add(LinkedHashMap<String, Tag> tags, Tag tag) throws Exception{
            CacheProxy.clear();
            boolean result =  dao.add(tag);
            if(result) {
                tags.put(tag.getName(), tag);
            }
            CacheProxy.clear();
            return result;
        }
        /**
         * 修改标签
         * @param table 表
         * @param tag 修改目标
         * @return boolean
         * @throws Exception 异常 sql异常
         */
        private boolean alter(Table table, Tag tag) throws Exception{
            boolean result = false;
            CacheProxy.clear();
            LinkedHashMap<String, Tag> tags = table.getTags();
            Tag original = tags.get(tag.getName().toUpperCase());

            Tag update = tag.getUpdate();
            if(null == update){
                update = tag.clone();
            }
            original.setUpdate(update, false, false);
            result = dao.alter(table, original);
            if(result) {
                tags.remove(original.getName());

                BeanUtil.copyFieldValueWithoutNull(original, update);
                original.setUpdate(update,false,false);
                BeanUtil.copyFieldValue(tag, original);
                tags.put(original.getName(), original);
            }
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean rename(Tag origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }
        /* *****************************************************************************************************************
         * 													primary
         * -----------------------------------------------------------------------------------------------------------------
         * boolean add(PrimaryKey primary) throws Exception
         * boolean alter(PrimaryKey primary) throws Exception
         * boolean drop(PrimaryKey primary) throws Exception
         ******************************************************************************************************************/

        
        @Override
        public boolean add(PrimaryKey primary) throws Exception{
            CacheProxy.clear();
            return dao.add(primary);
        }

        
        @Override
        public boolean alter(PrimaryKey primary) throws Exception {
            CacheProxy.clear();
            return false;
        }

        @Override
        public boolean drop(PrimaryKey primary) throws Exception{
            boolean result = dao.drop(primary);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(PrimaryKey origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }
        /* *****************************************************************************************************************
         * 													foreign
         ******************************************************************************************************************/

        @Override
        public boolean add(ForeignKey foreign) throws Exception{
            CacheProxy.clear();
            boolean result = dao.add(foreign);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean alter(ForeignKey foreign) throws Exception{
            CacheProxy.clear();
            boolean result = dao.alter(foreign);

            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean drop(ForeignKey foreign) throws Exception{
            if(BasicUtil.isEmpty(foreign.getName())){
                List<String> names = Column.names(foreign.getColumns());
                foreign = metadata.foreign(foreign.getTable(true), names);
            }
            boolean result = dao.drop(foreign);
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean rename(ForeignKey origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }
        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
         * boolean add(Index index) throws Exception
         * boolean alter(Index index) throws Exception
         * boolean drop(Index index) throws Exception
         ******************************************************************************************************************/

        
        @Override
        public boolean add(Index index) throws Exception{
            CacheProxy.clear();
            boolean result = dao.add(index);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean alter(Index index) throws Exception {
            CacheProxy.clear();
            return false;
        }

        @Override
        public boolean drop(Index index) throws Exception{
            boolean result = dao.drop(index);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(Index origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }
        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
		 * boolean add(Constraint constraint) throws Exception
		 * boolean alter(Constraint constraint) throws Exception
         * boolean drop(Constraint constraint) throws Exception
         ******************************************************************************************************************/
        
        @Override
        public boolean add(Constraint constraint) throws Exception {
            CacheProxy.clear();
            boolean result = dao.add(constraint);
            CacheProxy.clear();
            return result;
        }

        
        @Override
        public boolean alter(Constraint constraint) throws Exception {
            CacheProxy.clear();
            return false;
        }

        
        @Override
        public boolean drop(Constraint constraint) throws Exception {
            boolean result = dao.drop(constraint);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(Constraint origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }

        /* *****************************************************************************************************************
         * 													trigger
         ******************************************************************************************************************/
        /**
         * 触发器
         * @param trigger 触发器
         * @return trigger
         * @throws Exception 异常 Exception
         */
        @Override
        public boolean create(Trigger trigger) throws Exception{
            boolean result = dao.add(trigger);
            return result;
        }
        @Override
        public boolean alter(Trigger trigger) throws Exception{
            CacheProxy.clear();
            boolean result = dao.alter(trigger);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean drop(Trigger trigger) throws Exception{
            boolean result = dao.drop(trigger);
            CacheProxy.clear();
            return result;
        }
        @Override
        public boolean rename(Trigger origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            CacheProxy.clear();
            return result;
        }
        /* *****************************************************************************************************************
         * 													procedure
         ******************************************************************************************************************/
        /**
         * 存储过程
         * @param procedure 存储过程
         * @return boolean
         * @throws Exception 异常 Exception
         */
        @Override
        public boolean create(Procedure procedure) throws Exception{
            return dao.create(procedure);
        }
        @Override
        public boolean alter(Procedure procedure) throws Exception{
            boolean result = dao.alter(procedure);
            return result;
        }
        @Override
        public boolean drop(Procedure procedure) throws Exception{
            boolean result = dao.drop(procedure);
            return result;
        }
        @Override
        public boolean rename(Procedure origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            return result;
        }

        /* *****************************************************************************************************************
         * 													function
         ******************************************************************************************************************/
        /**
         * 函数
         * @param function 函数
         * @return boolean
         * @throws Exception 异常 Exception
         */
        @Override
        public boolean create(Function function) throws Exception{
            boolean result = dao.create(function);
            return result;
        }
        @Override
        public boolean alter(Function function) throws Exception{
            boolean result = dao.alter(function);
            return result;
        }
        @Override
        public boolean drop(Function function) throws Exception{
            boolean result = dao.drop(function);
            return result;
        }
        @Override
        public boolean rename(Function origin, String name) throws Exception{
            boolean result = dao.rename(origin, name);
            return result;
        }

    };
}
