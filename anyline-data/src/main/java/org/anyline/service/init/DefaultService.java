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

import org.anyline.annotation.Autowired;
import org.anyline.annotation.Component;
import org.anyline.cache.CacheElement;
import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.cache.CacheUtil;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.auto.init.SimplePrepare;
import org.anyline.data.prepare.init.DefaultSQLStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.MetadataDiffer;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.ServiceProxy;
import org.anyline.service.AnylineService;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

@Component(value = "anyline.service", index = 10)
public class DefaultService<E> implements AnylineService<E> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected AnylineDao dao;

    public String datasource() {
        return dao.runtime().datasource();
    }

    @Override
    public DriverAdapter adapter() {
        return dao.adapter();
    }

    @Override
    public DataRuntime runtime() {
        return dao.runtime();
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
     * @param dest        查询或操作的目标(表｜视图｜函数｜自定义SQL | SELECT语句)
     * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return DataSet
     */
    @Override
    public DataSet querys(String dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).querys(ps[1], configs, obj, conditions);
        }
        dest = BasicUtil.compress(dest);
        conditions = BasicUtil.compress(conditions);
        if(obj instanceof PageNavi){
            if(null == configs){
                configs = new DefaultConfigStore();
                configs.setPageNavi((PageNavi) obj);
            }
        }else {
            configs = append(configs, obj);
        }
        return queryFromDao(dest, configs, conditions);
    }

    @Override
    public DataSet querys(Table dest, ConfigStore configs, Object obj, String... conditions) {
        return queryFromDao(new DefaultTablePrepare(dest), configs, conditions);
    }
    @Override
    public List<String> column2param(String table) {
        List<String> columns = columns(table);
        return EntityAdapterProxy.column2param(columns);
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
    public List<Map<String, Object>> maps(RunPrepare prepare, ConfigStore configs, Object obj, String... conditions) {
        List<Map<String, Object>> maps = null;
        conditions = BasicUtil.compress(conditions);
        try {
            configs = append(configs, obj);
            if(null != prepare.getRuntime()) {
                maps = ServiceProxy.service(prepare.getRuntime()).getDao().maps(prepare, configs, conditions);
            }else {
                maps = dao.maps(prepare, configs, conditions);
            }
        } catch (Exception e) {
            maps = new ArrayList<Map<String, Object>>();
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return maps;
    }
    @Override
    public List<Map<String, Object>> maps(String dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).maps(ps[1], configs, obj, conditions);
        }
        List<Map<String, Object>> maps = null;
        dest = BasicUtil.compress(dest);
        conditions = BasicUtil.compress(conditions);
        try {
            RunPrepare prepare = createRunPrepare(dest);
            configs = append(configs, obj);
            if(null != prepare.getRuntime()) {
                maps = ServiceProxy.service(prepare.getRuntime()).getDao().maps(prepare, configs, conditions);
            }else {
                maps = dao.maps(prepare, configs, conditions);
            }
        } catch (Exception e) {
            maps = new ArrayList<Map<String, Object>>();
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return maps;
    }

    @Override
    public List<Map<String, Object>> maps(Table dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).maps(ps[1], configs, obj, conditions);
        }
        List<Map<String, Object>> maps = null;
        conditions = BasicUtil.compress(conditions);
        try {
            RunPrepare prepare = createRunPrepare(dest);
            configs = append(configs, obj);
            if(null != prepare.getRuntime()) {
                maps = ServiceProxy.service(prepare.getRuntime()).getDao().maps(prepare, configs, conditions);
            }else {
                maps = dao.maps(prepare, configs, conditions);
            }
        } catch (Exception e) {
            maps = new ArrayList<Map<String, Object>>();
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return maps;
    }

    @Override
    public DataSet caches(String cache, String dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).caches(cache, ps[1], configs, obj, conditions);
        }
        DataSet set = null;
        dest = BasicUtil.compress(dest);
        conditions = BasicUtil.compress(conditions);
        if (null == cache || ConfigTable.IS_CACHE_DISABLED) {
            set = querys(dest, append(configs, obj), conditions);
        } else {
            if (null != CacheProxy.provider) {
                set = queryFromCache(cache, dest, configs, conditions);
            } else {
                set = querys(dest, configs, conditions);
            }
        }
        return set;
    }

    @Override
    public DataSet caches(String cache, Table dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).caches(cache, ps[1], configs, obj, conditions);
        }
        DataSet set = null;
        conditions = BasicUtil.compress(conditions);
        if (null == cache || ConfigTable.IS_CACHE_DISABLED) {
            set = querys(dest, append(configs, obj), conditions);
        } else {
            if (null != CacheProxy.provider) {
                set = queryFromCache(cache, dest, configs, conditions);
            } else {
                set = querys(dest, configs, conditions);
            }
        }
        return set;
    }

    @Override
    public DataRow query(String dest, ConfigStore store, Object obj, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new DefaultConfigStore();
        }
        store.setPageNavi(navi);
        DataSet set = querys(dest, store, obj, conditions);
        if (null != set && !set.isEmpty()) {
            DataRow row = set.getRow(0);
            return row;
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }

    @Override
    public DataRow query(Table dest, ConfigStore store, Object obj, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new DefaultConfigStore();
        }
        store.setPageNavi(navi);
        DataSet set = querys(dest, store, obj, conditions);
        if (null != set && !set.isEmpty()) {
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
    public DataRow cache(String cache, String dest, ConfigStore configs, Object obj, String... conditions) {
        // 是否启动缓存
        if (null == cache || null == CacheProxy.provider || ConfigTable.IS_CACHE_DISABLED) {
            return query(dest, configs, obj, conditions);
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
        key += CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
        if (null != CacheProxy.provider) {
            CacheElement cacheElement = CacheProxy.provider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataRow) {
                    row = (DataRow) cacheValue;
                    row.setIsFromCache(true);
                    return row;
                } else {
                    log.error("[缓存设置错误, 检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
            }
        }
        // 调用实际 的方法
        row = query(dest, configs, obj, conditions);
        if (null != row && null != CacheProxy.provider) {
            CacheProxy.provider.put(cache, key, row);
        }
        if (null == row && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            row = new DataRow();
        }
        return row;
    }

    @Override
    public DataRow cache(String cache, Table dest, ConfigStore configs, Object obj, String... conditions) {
        // 是否启动缓存
        if (null == cache || null == CacheProxy.provider || ConfigTable.IS_CACHE_DISABLED) {
            return query(dest, configs, obj, conditions);
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
        key += CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
        if (null != CacheProxy.provider) {
            CacheElement cacheElement = CacheProxy.provider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataRow) {
                    row = (DataRow) cacheValue;
                    row.setIsFromCache(true);
                    return row;
                } else {
                    log.error("[缓存设置错误, 检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
            }
        }
        // 调用实际 的方法
        row = query(dest, configs, obj, conditions);
        if (null != row && null != CacheProxy.provider) {
            CacheProxy.provider.put(cache, key, row);
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
        if (null != list && !list.isEmpty()) {
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
    public <T> EntitySet<T> selects(String dest, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).selects(ps[1], clazz, configs, entity, conditions);
        }
        return queryFromDao(dest, clazz, append(configs, entity), conditions);
    }

    @Override
    public <T> EntitySet<T> selects(Table dest, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).selects(ps[1], clazz, configs, entity, conditions);
        }
        return queryFromDao(dest, clazz, append(configs, entity), conditions);
    }


    @Override
    public <T> T select(String dest, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        configs.setPageNavi(navi);
        EntitySet<T> list = selects(dest, clazz, configs, entity, conditions);
        if (null != list && !list.isEmpty()) {
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
    public <T> T select(Table dest, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        DefaultPageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new DefaultConfigStore();
        }
        configs.setPageNavi(navi);
        EntitySet<T> list = selects(dest, clazz, configs, entity, conditions);
        if (null != list && !list.isEmpty()) {
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
            if (null != CacheProxy.provider) {
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
        if (null != set && !set.isEmpty()) {
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
        if (null != CacheProxy.provider) {
            CacheElement cacheElement = CacheProxy.provider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataRow) {
                    row = (DataRow) cacheValue;
                    row.setIsFromCache(true);
                    return row;
                } else {
                    log.error("[缓存设置错误, 检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
            }
        }
        // 调用实际 的方法
        row = query(table, configs, conditions);
        if (null != row && null != CacheProxy.provider) {
            CacheProxy.provider.put(cache, key, row);
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
     * @param dest        查询或操作的目标(表、存储过程、SQL等)
     * @param configs    configs
     * @param conditions conditions
     * @return boolean
     */

    @Override
    public boolean removeCache(String channel, String dest, ConfigStore configs, String... conditions) {
        if (null != CacheProxy.provider) {
            dest = BasicUtil.compress(dest);
            conditions = BasicUtil.compress(conditions);
            String key = CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
            CacheProxy.provider.remove(channel, "SET:" + key);
            CacheProxy.provider.remove(channel, "ROW:" + key);

            DefaultPageNavi navi = new DefaultPageNavi();
            navi.setFirstRow(0);
            navi.setLastRow(0);
            navi.setCalType(1);
            if (null == configs) {
                configs = new DefaultConfigStore();
            }
            configs.setPageNavi(navi);
            key = CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
            CacheProxy.provider.remove(channel, "ROW:" + key);
        }
        return true;
    }

    @Override
    public boolean removeCache(String channel, Table dest, ConfigStore configs, String... conditions) {
        if (null != CacheProxy.provider) {
            conditions = BasicUtil.compress(conditions);
            String key = CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
            CacheProxy.provider.remove(channel, "SET:" + key);
            CacheProxy.provider.remove(channel, "ROW:" + key);

            DefaultPageNavi navi = new DefaultPageNavi();
            navi.setFirstRow(0);
            navi.setLastRow(0);
            navi.setCalType(1);
            if (null == configs) {
                configs = new DefaultConfigStore();
            }
            configs.setPageNavi(navi);
            key = CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
            CacheProxy.provider.remove(channel, "ROW:" + key);
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
        if (null != CacheProxy.provider) {
            return CacheProxy.provider.clear(channel);
        } else {
            return false;
        }
    }
    @Override
    public boolean clearCaches() {
        if (null != CacheProxy.provider) {
            return CacheProxy.provider.clears();
        } else {
            return false;
        }
    }

    /**
     * 是否存在
     *
     * @param dest        查询或操作的目标(表、存储过程、SQL等)
     * @param configs    根据http等上下文构造查询条件
     * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return boolean
     */
    @Override
    public boolean exists(String dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).exists(ps[1], configs, obj, conditions);
        }
        boolean result = false;
        dest = BasicUtil.compress(dest);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(dest);
        if(null != prepare.getRuntime()) {
            result = ServiceProxy.service(prepare.getRuntime()).getDao().exists(prepare, append(configs, obj), conditions);
        }else {
            result = dao.exists(prepare, append(configs, obj), conditions);
        }
        return result;
    }

    @Override
    public boolean exists(Table dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).exists(ps[1], configs, obj, conditions);
        }
        boolean result = false;
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(dest);
        if(null != prepare.getRuntime()) {
            result = ServiceProxy.service(prepare.getRuntime()).getDao().exists(prepare, append(configs, obj), conditions);
        }else {
            result = dao.exists(prepare, append(configs, obj), conditions);
        }
        return result;
    }
    /**
     * 只根据主键判断
     * @param dest 查询或操作的目标(表、存储过程、SQL等)
     * @param row DataRow
     * @return boolean
     */
    @Override
    public boolean exists(String dest, DataRow row) {
        if (null != row) {
            List<String> keys = row.getPrimaryKeys();
            if (null != keys) {
                String[] conditions = new String[keys.size()];
                int idx = 0;
                for (String key : keys) {
                    conditions[idx++] = key + ":" + row.getString(key);
                }
                return exists(dest, null, conditions);
            }
            return false;
        } else {
            return false;
        }
    }
    @Override
    public boolean exists(Table dest, DataRow row) {
        if (null != row) {
            List<String> keys = row.getPrimaryKeys();
            if (null != keys) {
                String[] conditions = new String[keys.size()];
                int idx = 0;
                for (String key : keys) {
                    conditions[idx++] = key + ":" + row.getString(key);
                }
                return exists(dest, null, conditions);
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
     * @param dest 表或视图或自定义SQL
     * @param configs 过滤条件
     * @param obj 根据obj生成的过滤条件
     * @param conditions 简单过滤条件
     * @return long
     */
    @Override
    public long count(String dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).count(ps[1], configs, obj, conditions);
        }
        long count = -1;
        try {
            // conditions = parseConditions(conditions);
            dest = BasicUtil.compress(dest);
            conditions = BasicUtil.compress(conditions);
            RunPrepare prepare = createRunPrepare(dest);
            if(null != prepare.getRuntime()) {
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
    @Override
    public long count(Table dest, ConfigStore configs, Object obj, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).count(ps[1], configs, obj, conditions);
        }
        long count = -1;
        try {
            conditions = BasicUtil.compress(conditions);
            RunPrepare prepare = createRunPrepare(dest);
            if(null != prepare.getRuntime()) {
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
    @Override
    public
    long count(RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions){
        long count = -1;
        try {
            conditions = BasicUtil.compress(conditions);
            if(null != prepare.getRuntime()) {
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
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data entity或list或DataRow或DataSet重复
     * @param columns 需要插入哪些列
     * @return 影响行数
     */
    @Override
    public long insert(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).insert(batch, ps[1], data, configs, columns);
        }
        return dao.insert(batch, dest, data, configs, columns);
    }

    @Override
    public long insert(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).insert(batch, dest, data, configs, columns);
        }
        return dao.insert(batch, dest, data, configs, columns);
    }

    @Override
    public long insert(Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).insert(dest, prepare, configs, conditions);
        }
        return dao.insert(dest, prepare, configs, obj, conditions);
    }

    @Override
    public long insert(Table dest, Table origin, ConfigStore configs, Object obj, String ... conditions){
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).insert(dest, origin, configs, obj, conditions);
        }
        RunPrepare prepare = createRunPrepare(origin);
        return dao.insert(dest, prepare, configs, obj, conditions);
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
     * 需要更新的列通过fixs/columns提供
     * @param columns 需要更新的列
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data    更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
     * @param configs 更新条件
     * @return int 影响行数
     */
    @Override
    public long update(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        if(!checkCondition(data, configs)) {
            return -1;
        }
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).update(batch, ps[1], data, configs, columns);
        }
        return dao.update(batch, dest, data, configs, columns);
    }

    @Override
    public long update(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        if(!checkCondition(data, configs)) {
            return -1;
        }
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).update(batch, dest, data, configs, columns);
        }
        return dao.update(batch, dest, data, configs, columns);
    }

    /**
     * update/delete 前检测是否有过滤条件
     * @param data Entity | DataRow
     * @param configs ConfigStore
     * @return boolean 返回false表示没有过滤条件 应该中断执行
     */
    protected boolean checkCondition(Object data, ConfigStore configs, String ... conditions) {
        if(null != configs) {
            if(!configs.isEmptyCondition()) {
                return true;
            }
        }
        if(null != conditions && conditions.length > 0) {
            return true;
        }
        if(null != data) {
            if (data instanceof DataRow) {
                DataRow row = (DataRow) data;
                if (BasicUtil.isNotEmpty(row.getPrimaryValue())) {
                    return true;
                }
            }else if(data instanceof Collection) {
                return true;
            }else{
                if(!EntityAdapterProxy.primaryValue(data).isEmpty()) {
                    return true;
                }
            }
        }
        log.warn("[没有update或delete过滤条件]");
        return false;
    }

    protected boolean checkCondition(Object data) {
        return checkCondition(data, null);
    }
    protected boolean checkCondition(ConfigStore configs, String ... conditions) {
        return checkCondition(null, configs, conditions);
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
     * @param batch 批量执行每批最多数量
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data  数据
     * @param columns 指定更新或保存的列
     * @return 影响行数
     */
    @Override
    public long save(int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        return save(batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
    }

    @Override
    public long save(int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        if (null == data) {
            return 0;
        }
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).save(batch, dest, data, configs, columns);
        }
        if(data instanceof DataSet) {
            DataSet set = (DataSet) data;
            long cnt = 0;
            DataSet inserts = new DataSet();
            DataSet updates = new DataSet();
            for(DataRow row:set) {
                Boolean override = row.getOverride();
                if(null != override) {
                    //如果设置了override需要到数据库中实际检测
                    boolean exists = exists(dest, row);
                    if(exists) {
                        if(!override) {//忽略

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
            if(!inserts.isEmpty()) {
                cnt += insert(batch, dest, inserts, configs, columns);
            }
            if(!updates.isEmpty()) {
                cnt += update(batch, dest, updates, configs, columns);
            }
            return cnt;
        }else if (data instanceof Collection) {
            Collection objs = (Collection) data;
            long cnt = 0;
            List<Object> inserts = new ArrayList<>();
            List<Object> updates = new ArrayList<>();
            for (Object obj : objs) {
                if(BeanUtil.checkIsNew(obj)) {
                    inserts.add(obj);
                }else{
                    updates.add(obj);
                }
            }
            if(!inserts.isEmpty()) {
                cnt += insert(batch, dest, inserts, configs, columns);
            }
            if(!updates.isEmpty()) {
                cnt += update(batch, dest, updates, configs, columns);
            }
            return cnt;
        }
        return saveObject(dest, data, configs, columns);
    }

    protected long saveObject(String dest, Object data, ConfigStore configs, List<String> columns) {
        return saveObject(DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
    }

    protected long saveObject(Table dest, Object data, ConfigStore configs, List<String> columns) {
        return dao.save(dest, data, configs, columns);
    }
    protected long saveObject(String dest, Object data, ConfigStore configs, String... columns) {
        return saveObject(dest, data, configs, BeanUtil.array2list(columns));
    }

    protected long saveObject(Table dest, Object data, ConfigStore configs, String... columns) {
        return saveObject(dest, data, configs, BeanUtil.array2list(columns));
    }

    @Override
    public boolean execute(Procedure procedure, String... inputs) {
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
            if (null != inputs) {
                for (String input : inputs) {
                    procedure.addInput(input);
                }
            }
            set = dao.querys(procedure, navi);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return set;
    }

    @Override
    public DataRow query(Procedure procedure, String... inputs) {
        DataSet set = querys(procedure, 0, 0, inputs);
        if (!set.isEmpty()) {
            return set.getRow(0);
        }
        if (ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }

    public long execute(int batch, String sql, Collection<Object> values) {
        RunPrepare prepare = createRunPrepare(sql);
        if (null == prepare) {
            return -1;
        }
        return dao.execute(batch, prepare, values);
    }
    public long execute(int batch, int vol, String sql, Collection<Object> values) {
        RunPrepare prepare = createRunPrepare(sql);
        if (null == prepare) {
            return -1;
        }
        return dao.execute(batch, vol, prepare, values);
    }
    @Override
    public long execute(String src, ConfigStore configs, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(src);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).execute(ps[1], configs, conditions);
        }
        long result = -1;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(src);
        if (null == prepare) {
            return result;
        }
        result = dao.execute(prepare, configs, conditions);
        return result;
    }

    @SuppressWarnings("rawtypes")

    @Override
    public long delete(String dest, DataSet set, String... columns) {
        return delete(DataSourceUtil.parseDest(dest, set, null), set, columns);
    }

    @Override
    public long delete(Table dest, DataSet set, String... columns) {
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
        return delete(DataSourceUtil.parseDest(dest, row, null), row, columns);
    }

    @Override
    public long delete(Table dest, DataRow row, String... columns) {
        if(!checkCondition(row)) {
            return -1;
        }
        String[] ps = DataSourceUtil.parseRuntime(dest);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], row, columns);
        }
        return dao.delete(dest, row, columns);
    }

    @Override
    public long delete(Object obj, String... columns) {
        if (null == obj) {
            return 0;
        }
        if(obj instanceof ConfigStore){
            return delete((ConfigStore) obj, columns);
        }
        if(!checkCondition(obj)) {
            return -1;
        }
        Table dest = null;
        if (obj instanceof DataRow) {
            DataRow row = (DataRow) obj;
            return dao.delete(dest, row, columns);
        } else {
            if (obj instanceof Collection) {
                Collection list =((Collection) obj);
                if(!list.isEmpty()) {
                    Class clazz = list.iterator().next().getClass();
                    dest = EntityAdapterProxy.table(clazz);
                }
            } else {
                    dest = EntityAdapterProxy.table(obj.getClass());
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
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], kvs);
        }
        DataRow row = DataRow.parseArray(kvs);
        row.setPrimaryKey(row.keys());
        if(!checkCondition(row)) {
            return -1;
        }
        return dao.delete(table, row);
    }

    @Override
    public long delete(Table table, String... kvs) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], kvs);
        }
        DataRow row = DataRow.parseArray(kvs);
        row.setPrimaryKey(row.keys());
        if(!checkCondition(row)) {
            return -1;
        }
        return dao.delete(table, row);
    }

    @Override
    public long delete(Table table, Collection values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], values);
        }
        return dao.delete(table, values);
    }

    @Override
    public long delete(String table, Collection values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], values);
        }
        return dao.delete(table, values);
    }

    @Override
    public <T> long deletes(int batch, String table, String key, Collection<T> values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).deletes(batch, ps[1], key, values);
        }
        if(batch >1) {
            long qty = 0;
            List<T> list = new ArrayList<>();
            int vol = 0;
            for(T value:values) {
                list.add(value);
                vol ++;
                if(vol >= batch) {
                    qty += dao.deletes(0, table, key, values);
                    list.clear();
                }
            }
            if(!list.isEmpty()) {
                qty += dao.deletes(0, table, key, values);
            }
            return qty;
        }else {
            return dao.deletes(batch, table, key, values);
        }
    }

    @Override
    public <T> long deletes(int batch, Table table, String key, Collection<T> values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).deletes(batch, ps[1], key, values);
        }
        if(batch >1) {
            long qty = 0;
            List<T> list = new ArrayList<>();
            int vol = 0;
            for(T value:values) {
                list.add(value);
                vol ++;
                if(vol >= batch) {
                    qty += dao.deletes(0, table, key, values);
                    list.clear();
                }
            }
            if(!list.isEmpty()) {
                qty += dao.deletes(0, table, key, values);
            }
            return qty;
        }else {
            return dao.deletes(batch, table, key, values);
        }
    }


    @Override
    public <T> long deletes(int batch, String table, String key, T... values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).deletes(batch, ps[1], key, values);
        }
        return dao.deletes(batch, table, key, values);
    }

    @Override
    public <T> long deletes(int batch, Table table, String key, T... values) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).deletes(batch, ps[1], key, values);
        }
        return dao.deletes(batch, table, key, values);
    }


    @Override
    public long delete(String table, ConfigStore configs, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], configs, conditions);
        }
        if(!checkCondition(configs, conditions)) {
            return -1;
        }
        return dao.delete(table, configs, conditions);
    }

    @Override
    public long delete(Table table, ConfigStore configs, String... conditions) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).delete(ps[1], configs, conditions);
        }
        if(!checkCondition(configs, conditions)) {
            return -1;
        }
        return dao.delete(table, configs, conditions);
    }


    @Override
    public long truncate(String table) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
            return ServiceProxy.service(ps[0]).truncate(ps[1]);
        }
        return dao.truncate(table);
    }

    @Override
    public long truncate(Table table) {
        String[] ps = DataSourceUtil.parseRuntime(table);
        if(null != ps[0]) {
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

    protected PageNavi setPageLazy(Table src, ConfigStore configs, String... conditions) {
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
            log.debug("[解析SQL][dest:{}]", prepare.getText());
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

    protected DataSet queryFromDao(String dest, ConfigStore configs, String... conditions) {
       return queryFromDao(DataSourceUtil.parseDest(dest, null, configs), configs, conditions);
    }
    protected DataSet queryFromDao(Table dest, ConfigStore configs, String... conditions) {
        DataSet set = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][dest:{}]", dest);
        }
        try {
            setPageLazy(dest, configs, conditions);
            RunPrepare prepare = createRunPrepare(dest);

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

    protected <T> EntitySet<T> queryFromDao(String dest, Class<T> clazz, ConfigStore configs, String... conditions) {
        return queryFromDao(DataSourceUtil.parseDest(dest, clazz, configs), clazz, configs, conditions);
    }

    protected <T> EntitySet<T> queryFromDao(Table dest, Class<T> clazz, ConfigStore configs, String... conditions) {
        EntitySet<T> list = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][dest:{}]", clazz);
        }
        try {
            setPageLazy(dest, configs, conditions);
            RunPrepare prepare = createRunPrepare(dest);
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
            log.debug("[解析SQL][dest:{}]", clazz);
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
     * 根据差异生成SQL
     * @param differ differ
     * @return sqls
     */
    @Override
    public List<Run> ddl(MetadataDiffer differ) {
        return dao.ddl(differ);
    }
    /**
     * 根据差异生成SQL
     * @param differs differs
     * @return sqls
     */
    @Override
    public List<Run> ddl(List<MetadataDiffer> differs) {
        return dao.ddl(differs);
    }
    /**
     * 解析SQL中指定的主键table(col1, col2)&lt;pk1, pk2&gt;
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

    protected RunPrepare createRunPrepare(Table table) {
        if(null == table){
            return new SimplePrepare();
        }
        return createRunPrepare(table.getFullName());
    }
    protected RunPrepare createRunPrepare(String src) {
        RunPrepare prepare = null;
        if(BasicUtil.isEmpty(src)){
            //有些数据库不根据表查询可以为空
            return new DefaultTablePrepare();
        }
        src = src.trim();
        List<String> pks = new ArrayList<>();
        // 文本sql
        //if (src.startsWith("${") && src.endsWith("}")) {
        if(BasicUtil.checkEl(src)) {
            if (ConfigTable.isSQLDebug()) {
                log.debug("[解析SQL类型] [类型:{JAVA定义}] [src:{}]", src);
            }
            src = src.substring(2, src.length() - 1);
            src = parsePrimaryKey(src, pks);//解析主键
            prepare = new DefaultTextPrepare(src);
        } else {
            Table table = DataSourceUtil.parseDest(src, null, null);
            String id = table.getId();
            pks = Column.names(table.primarys());
            DriverAdapter adapter = adapter();
            src = table.getText();
            if(null != adapter){
                prepare = adapter.buildRunPrepare(runtime(), src);
            }
            if(null == prepare){
                if (null != id && RegularUtil.match(id, RunPrepare.XML_SQL_ID_STYLE)) {
                    /* XML定义 */
                    if (ConfigTable.isSQLDebug()) {
                        log.debug("[解析SQL类型] [类型:XML定义] [src:{}]", src);
                    }
                    prepare = DefaultSQLStore.parseSQL(id);
                    if (null == prepare) {
                        log.error("[解析SQL类型][XML解析失败][src:{}]", src);
                    }
                }
            }
            if(null == prepare){
                String chk = src;
                if(null != chk){

                    chk = chk.replace("\n","").replace("\r","").trim().toLowerCase()
                        .replaceAll("\\s+\\(","("); //user (id, name) > user(id, name)
                }
                if(BasicUtil.isEmpty(chk) || chk.matches("^[a-z]+$") || chk.matches("^[a-z]+\\(.*\\)$")){
                    //USER
                    //USER(ID,CODE)
                    //USER(ID AS CODE, IFNULL(CODE, ID ) AS CODE)
                    prepare = new DefaultTablePrepare(table);
                    /* 自动生成 */
                    if (ConfigTable.isSQLDebug()) {
                        log.debug("[解析SQL类型] [类型:auto] [src:{}]", src);
                    }
                }else{
                    //(null != chk && (chk.matches("^[a-z]+\\s+.+") || chk.startsWith("from(")))
                    //SELECT * FROM SSO_USER
                    //其他格式在adapter先解析出来 不要等到这一步 会跟SQL混淆
                    //MATCH (e:CRM_USER:HR_USER) RETURN e
                    //from(bucket: "test") |> range(start: 0) |> filter(fn: (r) => r._measurement == "device_test")
                    prepare = new DefaultTextPrepare(src);
                    if (ConfigTable.isSQLDebug()) {
                        log.debug("[解析SQL类型] [类型:JAVA定义] [src:{}]", src);
                    }
                }

            }
        }
        if (null != prepare && null != pks && !pks.isEmpty()) {
            prepare.setPrimaryKey(pks);
        }
        return prepare;
    }

    protected DataSet queryFromCache(String cache, String dest, ConfigStore configs, String... conditions) {
        return queryFromCache(cache, DataSourceUtil.parseDest(dest,configs), configs, conditions);
    }

    protected DataSet queryFromCache(String cache, Table dest, ConfigStore configs, String... conditions) {
        if (ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
            log.debug("[cache from][cache:{}][dest:{}]", cache, dest);
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
        key += CacheUtil.createCacheElementKey(true, true, dest, configs, conditions);
        RunPrepare prepare = createRunPrepare(dest);
        if (null != CacheProxy.provider) {
            CacheElement cacheElement = CacheProxy.provider.get(cache, key);
            if (null != cacheElement && null != cacheElement.getValue()) {
                Object cacheValue = cacheElement.getValue();
                if (cacheValue instanceof DataSet) {
                    set = (DataSet) cacheValue;
                    set.setIsFromCache(true);
                } else {
                    log.error("[缓存设置错误, 检查配置文件是否有重复cache.name 或Java代码调用中cache.name混淆][channel:{}]", cache);
                }
//       	// 开启新线程提前更新缓存(90%时间)
                long age = (System.currentTimeMillis() - cacheElement.getCreateTime()) / 1000;
                final int _max = cacheElement.getExpires();
                if (age > _max * 0.9) {
                    if (ConfigTable.IS_DEBUG && log.isWarnEnabled()) {
                        log.debug("[缓存即将到期提前刷新][dest:{}] [生存:{}/{}]", dest, age, _max);
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
                            CacheProxy.provider.put(_cache, _key, newSet);
                            CacheUtil.stop(_key, _max / 10);
                        }
                    }).start();
                }

            } else {
                setPageLazy(dest, configs, conditions);
                set = dao.querys(prepare, configs, conditions);
                CacheProxy.provider.put(cache, key, set);
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
     * 根据结果集对象获取列结构, 如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param sql sql
     * @param comment 是否需要列注释
     * @param condition 是否需要拼接查询条件, 如果需要会拼接where 1=0 条件
     * @return LinkedHashMap
     */
    public LinkedHashMap<String, Column> metadata(String sql, boolean comment, boolean condition) {
        if(condition) {
            String up = sql.toUpperCase().replace("\n"," ").replace("\t","");
            String key = " WHERE ";
            boolean split = false;
            if(up.contains(key)) {
                int idx = sql.lastIndexOf(key);
                sql = sql.substring(0, idx) + " WHERE 1=0 AND " + sql.substring(idx + key.length());
                split = true;
            }else{
                key = " GROUP ";
                if(up.contains(key)) {
                    int idx = sql.lastIndexOf(key);
                    sql = sql.substring(0, idx) + " WHERE 1=0 GROUP " + sql.substring(idx + key.length());
                    split = true;
                }else{
                    key = " ORDER ";
                    if(up.contains(key)) {
                        int idx = sql.lastIndexOf(key);
                        sql = sql.substring(0, idx) + " WHERE 1=0 ORDER " + sql.substring(idx + key.length());
                        split = true;
                    }
                }
            }
            if(!split) {
                sql = sql + " WHERE 1=0";
            }
        }
        RunPrepare prepare = createRunPrepare(sql);
        LinkedHashMap<String, Column> metadata = dao.metadata(prepare, comment);
        return metadata;
    }
    @Override
    public List<String> tables(Catalog catalog, Schema schema, String name, int types) {
        LinkedHashMap<String, Table> tables = metadata.tables(catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (Table table : tables.values()) {
            list.add(table.getName());
        }
        return list;
    }

    @Override
    public List<String> views(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
        LinkedHashMap<String, View> tables = metadata.views(catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (Table table : tables.values()) {
            list.add(table.getName());
        }
        return list;
    }


    @Override
    public List<String> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types) {
        LinkedHashMap<String, MasterTable> tables = metadata.masterTables(catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (Table table : tables.values()) {
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
         * LinkedHashMap<String, Database> databases();
         ******************************************************************************************************************/

        /**
         * 当前数据源 数据库类型
         * @return DatabaseType
         */
        @Override
        public DatabaseType type() {
            return dao.type();
        }

        /**
         * 当前数据源 数据库版本 版本号比较复杂 不是全数字
         * @return String
         */
        @Override
        public String version() {
            return dao.version();
        }

        /**
         * 当前数据源 数据库描述(产品名称+版本号)
         * @return String
         */
        @Override
        public String product() {
            return dao.product();
        }
        @Override
        public Database database() {
            return dao.database();
        }
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
         * LinkedHashMap<String, Database> databases();
         ******************************************************************************************************************/
        @Override
        public Catalog catalog() {
            return dao.catalog();
        }
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
        public Schema schema() {
            return dao.schema();
        }
        @Override
        public List<Schema> schemas(boolean greedy, Catalog catalog, String name) {
            return dao.schemas(greedy, catalog, name);
        }

        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(Table table)
         * LinkedHashMap<String, Table> tables(Catalog catalog, Schema schema, String name, int types)
         * LinkedHashMap<String, Table> tables(Schema schema, String name, int types)
         * LinkedHashMap<String, Table> tables(String name, int types)
         * LinkedHashMap<String, Table> tables(int types)
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
        public <T extends Table>  List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().tables(greedy, catalog, schema, ps[1], types, struct, configs);
            }
            return dao.tables(greedy, catalog, schema, name, types, struct, configs);
        }
        @Override
        public <T extends Table>  List<T> tables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
            return tables(greedy, catalog, schema, name, types, struct, null);
        }
        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().tables(catalog, schema, ps[1], types, struct, configs);
            }
            return dao.tables(catalog, schema, name, types, struct, configs);
        }

        private void struct(Table table, int struct) {
            //是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
            LinkedHashMap<String, Column> columns = table.getColumns();
            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                if(null == columns || columns.isEmpty()) {//上一步ddl是否加载过以下内容
                    columns = columns(table);
                    table.setColumns(columns);
                    table.setTags(tags(table));
                }
            }

            if(Metadata.check(struct, Metadata.TYPE.PRIMARY)) {
                PrimaryKey pk = table.getPrimaryKey();
                if(null == pk) {
                    pk = primary(table);
                    if (null != pk) {
                        for (Column col : pk.getColumns().values()) {
                            Column column = columns.get(col.getName().toUpperCase());
                            if (null != column) {
                                column.primary(true);
                                BeanUtil.copyFieldValue(col, column);
                            }
                        }
                    }
                    table.setPrimaryKey(pk);
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                LinkedHashMap<String, Index> indexes = table.getIndexes();
                if(null == indexes || indexes.isEmpty()) {
                    table.setIndexes(indexes(table));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.CONSTRAINT)) {
                LinkedHashMap<String, Constraint> constraints = table.getConstraints();
                if(null == constraints || constraints.isEmpty()) {
                    table.setConstraints(constraints(table));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.DDL)) {
                if (null == table.ddl()) {
                    ddl(table);
                }
            }

        }
        @Override
        public Table table(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
            Table table = null;
            List<Table> tables = tables(greedy, catalog, schema, name, Table.TYPE.NORMAL.value);
            if (!tables.isEmpty()) {
                table = tables.get(0);
                if(null != table && struct>0) {
                    struct(table, struct);
                }
            }
            return table;
        }
        @Override
        public Table table(Catalog catalog, Schema schema, String name, int struct) {
            Table table = null;
            LinkedHashMap<String, Table> tables = tables(catalog, schema, name, Table.TYPE.NORMAL.value);
            if (!tables.isEmpty()) {
                table = tables.values().iterator().next();
                if(null != table && struct > 0) {
                   struct(table, struct);
                }
            }
            return table;
        }

        @Override
        public List<String> ddl(Table table, boolean init) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(table, init);
            }
            return dao.ddl(table, init);
        }


        /* *****************************************************************************************************************
         * 													vertexTable
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(VertexTable vertexTable)
         * LinkedHashMap<String, VertexTable> vertexTables(Catalog catalog, Schema schema, String name, int types)
         * LinkedHashMap<String, VertexTable> vertexTables(Schema schema, String name, int types)
         * LinkedHashMap<String, VertexTable> vertexTables(String name, int types)
         * LinkedHashMap<String, VertexTable> vertexTables(int types)
         * LinkedHashMap<String, VertexTable> vertexTables()
         * VertexTable vertexTable(Catalog catalog, Schema schema, String name)
         * VertexTable vertexTable(Schema schema, String name)
         * VertexTable vertexTable(String name)
         ******************************************************************************************************************/

        @Override
        public boolean exists(boolean greedy, VertexTable vertexTable) {
            if (null != vertexTable(greedy, vertexTable.getCatalog(), vertexTable.getSchema(), vertexTable.getName(), false)) {
                return true;
            }
            return false;
        }

        @Override
        public <T extends VertexTable>  List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().vertexTables(greedy, catalog, schema, ps[1], types, struct, configs);
            }
            return dao.vertexTables(greedy, catalog, schema, name, types, struct, configs);
        }
        @Override
        public <T extends VertexTable>  List<T> vertexTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
            return vertexTables(greedy, catalog, schema, name, types, struct, null);
        }
        @Override
        public <T extends VertexTable>  LinkedHashMap<String, T> vertexTables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().vertexTables(catalog, schema, ps[1], types, struct, configs);
            }
            return dao.vertexTables(catalog, schema, name, types, struct, configs);
        }

        private void struct(VertexTable vertexTable, int struct) {
            //是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
            LinkedHashMap<String, Column> columns = vertexTable.getColumns();
            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                if(null == columns || columns.isEmpty()) {//上一步ddl是否加载过以下内容
                    columns = columns(vertexTable);
                    vertexTable.setColumns(columns);
                    vertexTable.setTags(tags(vertexTable));
                }
            }

            if(Metadata.check(struct, Metadata.TYPE.PRIMARY)) {
                PrimaryKey pk = vertexTable.getPrimaryKey();
                if(null == pk) {
                    pk = primary(vertexTable);
                    if (null != pk) {
                        for (Column col : pk.getColumns().values()) {
                            Column column = columns.get(col.getName().toUpperCase());
                            if (null != column) {
                                column.primary(true);
                                BeanUtil.copyFieldValue(col, column);
                            }
                        }
                    }
                    vertexTable.setPrimaryKey(pk);
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                LinkedHashMap<String, Index> indexes = vertexTable.getIndexes();
                if(null == indexes || indexes.isEmpty()) {
                    vertexTable.setIndexes(indexes(vertexTable));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.CONSTRAINT)) {
                LinkedHashMap<String, Constraint> constraints = vertexTable.getConstraints();
                if(null == constraints || constraints.isEmpty()) {
                    vertexTable.setConstraints(constraints(vertexTable));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.DDL)) {
                if (null == vertexTable.ddl()) {
                    ddl(vertexTable);
                }
            }

        }
        @Override
        public VertexTable vertexTable(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
            VertexTable vertexTable = null;
            List<VertexTable> vertexTables = vertexTables(greedy, catalog, schema, name, VertexTable.TYPE.NORMAL.value);
            if (!vertexTables.isEmpty()) {
                vertexTable = vertexTables.get(0);
                if(null != vertexTable && struct>0) {
                    struct(vertexTable, struct);
                }
            }
            return vertexTable;
        }
        @Override
        public VertexTable vertexTable(Catalog catalog, Schema schema, String name, int struct) {
            VertexTable vertexTable = null;
            LinkedHashMap<String, VertexTable> vertexTables = vertexTables(catalog, schema, name, VertexTable.TYPE.NORMAL.value);
            if (!vertexTables.isEmpty()) {
                vertexTable = vertexTables.values().iterator().next();
                if(null != vertexTable && struct > 0) {
                    struct(vertexTable, struct);
                }
            }
            return vertexTable;
        }

        @Override
        public List<String> ddl(VertexTable vertexTable, boolean init) {
            String[] ps = DataSourceUtil.parseRuntime(vertexTable);
            if(null != ps[0]) {
                vertexTable.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(vertexTable, init);
            }
            return dao.ddl(vertexTable, init);
        }

        /* *****************************************************************************************************************
         * 													edgeTable
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(EdgeTable edgeTable)
         * LinkedHashMap<String, EdgeTable> edgeTables(Catalog catalog, Schema schema, String name, int types)
         * LinkedHashMap<String, EdgeTable> edgeTables(Schema schema, String name, int types)
         * LinkedHashMap<String, EdgeTable> edgeTables(String name, int types)
         * LinkedHashMap<String, EdgeTable> edgeTables(int types)
         * LinkedHashMap<String, EdgeTable> edgeTables()
         * EdgeTable edgeTable(Catalog catalog, Schema schema, String name)
         * EdgeTable edgeTable(Schema schema, String name)
         * EdgeTable edgeTable(String name)
         ******************************************************************************************************************/

        @Override
        public boolean exists(boolean greedy, EdgeTable edgeTable) {
            if (null != edgeTable(greedy, edgeTable.getCatalog(), edgeTable.getSchema(), edgeTable.getName(), false)) {
                return true;
            }
            return false;
        }

        @Override
        public <T extends EdgeTable>  List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().edgeTables(greedy, catalog, schema, ps[1], types, struct, configs);
            }
            return dao.edgeTables(greedy, catalog, schema, name, types, struct, configs);
        }
        @Override
        public <T extends EdgeTable>  List<T> edgeTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
            return edgeTables(greedy, catalog, schema, name, types, struct, null);
        }
        @Override
        public <T extends EdgeTable>  LinkedHashMap<String, T> edgeTables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().edgeTables(catalog, schema, ps[1], types, struct, configs);
            }
            return dao.edgeTables(catalog, schema, name, types, struct, configs);
        }

        private void struct(EdgeTable edgeTable, int struct) {
            //是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
            LinkedHashMap<String, Column> columns = edgeTable.getColumns();
            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                if(null == columns || columns.isEmpty()) {//上一步ddl是否加载过以下内容
                    columns = columns(edgeTable);
                    edgeTable.setColumns(columns);
                    edgeTable.setTags(tags(edgeTable));
                }
            }

            if(Metadata.check(struct, Metadata.TYPE.PRIMARY)) {
                PrimaryKey pk = edgeTable.getPrimaryKey();
                if(null == pk) {
                    pk = primary(edgeTable);
                    if (null != pk) {
                        for (Column col : pk.getColumns().values()) {
                            Column column = columns.get(col.getName().toUpperCase());
                            if (null != column) {
                                column.primary(true);
                                BeanUtil.copyFieldValue(col, column);
                            }
                        }
                    }
                    edgeTable.setPrimaryKey(pk);
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                LinkedHashMap<String, Index> indexes = edgeTable.getIndexes();
                if(null == indexes || indexes.isEmpty()) {
                    edgeTable.setIndexes(indexes(edgeTable));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.CONSTRAINT)) {
                LinkedHashMap<String, Constraint> constraints = edgeTable.getConstraints();
                if(null == constraints || constraints.isEmpty()) {
                    edgeTable.setConstraints(constraints(edgeTable));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.DDL)) {
                if (null == edgeTable.ddl()) {
                    ddl(edgeTable);
                }
            }

        }
        @Override
        public EdgeTable edgeTable(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
            EdgeTable edgeTable = null;
            List<EdgeTable> edgeTables = edgeTables(greedy, catalog, schema, name, EdgeTable.TYPE.NORMAL.value);
            if (!edgeTables.isEmpty()) {
                edgeTable = edgeTables.get(0);
                if(null != edgeTable && struct>0) {
                    struct(edgeTable, struct);
                }
            }
            return edgeTable;
        }
        @Override
        public EdgeTable edgeTable(Catalog catalog, Schema schema, String name, int struct) {
            EdgeTable edgeTable = null;
            LinkedHashMap<String, EdgeTable> edgeTables = edgeTables(catalog, schema, name, EdgeTable.TYPE.NORMAL.value);
            if (!edgeTables.isEmpty()) {
                edgeTable = edgeTables.values().iterator().next();
                if(null != edgeTable && struct > 0) {
                    struct(edgeTable, struct);
                }
            }
            return edgeTable;
        }

        @Override
        public List<String> ddl(EdgeTable edgeTable, boolean init) {
            String[] ps = DataSourceUtil.parseRuntime(edgeTable);
            if(null != ps[0]) {
                edgeTable.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(edgeTable, init);
            }
            return dao.ddl(edgeTable, init);
        }

        /* *****************************************************************************************************************
         * 													view
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(View view)
         * LinkedHashMap<String, View> views(Catalog catalog, Schema schema, String name, int types)
         * LinkedHashMap<String, View> views(Schema schema, String name, int types)
         * LinkedHashMap<String, View> views(String name, int types)
         * LinkedHashMap<String, View> views(int types)
         * LinkedHashMap<String, View> views()
         * View view(Catalog catalog, Schema schema, String name)
         * View view(Schema schema, String name)
         * View view(String name)
         ******************************************************************************************************************/

        @Override
        public boolean exists(boolean greedy, View view) {
            if (null != view(greedy, view.getCatalog(), view.getSchema(), view.getName(), false)) {
                return true;
            }
            return false;
        }

        @Override
        public <T extends View>  List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().views(greedy, catalog, schema, ps[1], types, struct, configs);
            }
            return dao.views(greedy, catalog, schema, name, types, struct, configs);
        }
        @Override
        public <T extends View>  List<T> views(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
            return views(greedy, catalog, schema, name, types, struct, null);
        }
        @Override
        public <T extends View>  LinkedHashMap<String, T> views(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().views(catalog, schema, ps[1], types, struct, configs);
            }
            return dao.views(catalog, schema, name, types, struct, configs);
        }

        private void struct(View view, int struct) {
            //是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
            LinkedHashMap<String, Column> columns = view.getColumns();
            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                if(null == columns || columns.isEmpty()) {//上一步ddl是否加载过以下内容
                    columns = columns(view);
                    view.setColumns(columns);
                    view.setTags(tags(view));
                }
            }

            if(Metadata.check(struct, Metadata.TYPE.PRIMARY)) {
                PrimaryKey pk = view.getPrimaryKey();
                if(null == pk) {
                    pk = primary(view);
                    if (null != pk) {
                        for (Column col : pk.getColumns().values()) {
                            Column column = columns.get(col.getName().toUpperCase());
                            if (null != column) {
                                column.primary(true);
                                BeanUtil.copyFieldValue(col, column);
                            }
                        }
                    }
                    view.setPrimaryKey(pk);
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                LinkedHashMap<String, Index> indexes = view.getIndexes();
                if(null == indexes || indexes.isEmpty()) {
                    view.setIndexes(indexes(view));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.CONSTRAINT)) {
                LinkedHashMap<String, Constraint> constraints = view.getConstraints();
                if(null == constraints || constraints.isEmpty()) {
                    view.setConstraints(constraints(view));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.DDL)) {
                if (null == view.ddl()) {
                    ddl(view);
                }
            }

        }
        @Override
        public View view(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
            View view = null;
            List<View> views = views(greedy, catalog, schema, name, View.TYPE.NORMAL.value);
            if (!views.isEmpty()) {
                view = views.get(0);
                if(null != view && struct>0) {
                    struct(view, struct);
                }
            }
            return view;
        }
        @Override
        public View view(Catalog catalog, Schema schema, String name, int struct) {
            View view = null;
            LinkedHashMap<String, View> views = views(catalog, schema, name, View.TYPE.NORMAL.value);
            if (!views.isEmpty()) {
                view = views.values().iterator().next();
                if(null != view && struct > 0) {
                    struct(view, struct);
                }
            }
            return view;
        }

        @Override
        public List<String> ddl(View view, boolean init) {
            String[] ps = DataSourceUtil.parseRuntime(view);
            if(null != ps[0]) {
                view.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(view, init);
            }
            return dao.ddl(view, init);
        }

        /* *****************************************************************************************************************
         * 													masterTable
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(MasterTable masterTable)
         * LinkedHashMap<String, MasterTable> masterTables(Catalog catalog, Schema schema, String name, int types)
         * LinkedHashMap<String, MasterTable> masterTables(Schema schema, String name, int types)
         * LinkedHashMap<String, MasterTable> masterTables(String name, int types)
         * LinkedHashMap<String, MasterTable> masterTables(int types)
         * LinkedHashMap<String, MasterTable> masterTables()
         * MasterTable masterTable(Catalog catalog, Schema schema, String name)
         * MasterTable masterTable(Schema schema, String name)
         * MasterTable masterTable(String name)
         ******************************************************************************************************************/

        @Override
        public boolean exists(boolean greedy, MasterTable masterTable) {
            if (null != masterTable(greedy, masterTable.getCatalog(), masterTable.getSchema(), masterTable.getName(), false)) {
                return true;
            }
            return false;
        }

        @Override
        public <T extends MasterTable>  List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().masterTables(greedy, catalog, schema, ps[1], types, struct, configs);
            }
            return dao.masterTables(greedy, catalog, schema, name, types, struct, configs);
        }
        @Override
        public <T extends MasterTable>  List<T> masterTables(boolean greedy, Catalog catalog, Schema schema, String name, int types, int struct) {
            return masterTables(greedy, catalog, schema, name, types, struct, null);
        }
        @Override
        public <T extends MasterTable>  LinkedHashMap<String, T> masterTables(Catalog catalog, Schema schema, String name, int types, int struct, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().masterTables(catalog, schema, ps[1], types, struct, configs);
            }
            return dao.masterTables(catalog, schema, name, types, struct, configs);
        }

        private void struct(MasterTable masterTable, int struct) {
            //是否查询详细结构(1列、2主键、4索引、8外键、16约束、128DDL等)
            LinkedHashMap<String, Column> columns = masterTable.getColumns();
            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                if(null == columns || columns.isEmpty()) {//上一步ddl是否加载过以下内容
                    columns = columns(masterTable);
                    masterTable.setColumns(columns);
                    masterTable.setTags(tags(masterTable));
                }
            }

            if(Metadata.check(struct, Metadata.TYPE.PRIMARY)) {
                PrimaryKey pk = masterTable.getPrimaryKey();
                if(null == pk) {
                    pk = primary(masterTable);
                    if (null != pk) {
                        for (Column col : pk.getColumns().values()) {
                            Column column = columns.get(col.getName().toUpperCase());
                            if (null != column) {
                                column.primary(true);
                                BeanUtil.copyFieldValue(col, column);
                            }
                        }
                    }
                    masterTable.setPrimaryKey(pk);
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                LinkedHashMap<String, Index> indexes = masterTable.getIndexes();
                if(null == indexes || indexes.isEmpty()) {
                    masterTable.setIndexes(indexes(masterTable));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.CONSTRAINT)) {
                LinkedHashMap<String, Constraint> constraints = masterTable.getConstraints();
                if(null == constraints || constraints.isEmpty()) {
                    masterTable.setConstraints(constraints(masterTable));
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.DDL)) {
                if (null == masterTable.ddl()) {
                    ddl(masterTable);
                }
            }

        }
        @Override
        public MasterTable masterTable(boolean greedy, Catalog catalog, Schema schema, String name, int struct) {
            MasterTable masterTable = null;
            List<MasterTable> masterTables = masterTables(greedy, catalog, schema, name, MasterTable.TYPE.NORMAL.value);
            if (!masterTables.isEmpty()) {
                masterTable = masterTables.get(0);
                if(null != masterTable && struct>0) {
                    struct(masterTable, struct);
                }
            }
            return masterTable;
        }
        @Override
        public MasterTable masterTable(Catalog catalog, Schema schema, String name, int struct) {
            MasterTable masterTable = null;
            LinkedHashMap<String, MasterTable> masterTables = masterTables(catalog, schema, name, MasterTable.TYPE.NORMAL.value);
            if (!masterTables.isEmpty()) {
                masterTable = masterTables.values().iterator().next();
                if(null != masterTable && struct > 0) {
                    struct(masterTable, struct);
                }
            }
            return masterTable;
        }

        @Override
        public List<String> ddl(MasterTable masterTable, boolean init) {
            String[] ps = DataSourceUtil.parseRuntime(masterTable);
            if(null != ps[0]) {
                masterTable.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(masterTable, init);
            }
            return dao.ddl(masterTable, init);
        }

        /* *****************************************************************************************************************
         * 													partition  table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(PartitionTable table)
         * LinkedHashMap<String, PartitionTable> partitionTables(Catalog catalog, Schema schema, String name, int types)
         * LinkedHashMap<String, PartitionTable> partitionTables(Schema schema, String name, int types)
         * LinkedHashMap<String, PartitionTable> partitionTables(String name, int types)
         * LinkedHashMap<String, PartitionTable> partitionTables(int types)
         * LinkedHashMap<String, PartitionTable> partitionTables()
         * LinkedHashMap<String, PartitionTable> partitionTables(MasterTable master)
         * PartitionTable partitionTable(Catalog catalog, Schema schema, String name)
         * PartitionTable partitionTable(Schema schema, String name)
         * PartitionTable partitionTable(String name)
         ******************************************************************************************************************/


        @Override
        public boolean exists(boolean greedy, PartitionTable table) {
            PartitionTable tab = partitionTable(greedy, table.getCatalog(), table.getSchema(), table.getMasterName(), table.getName());
            return null != tab;
        }

        @Override
        public boolean exists(PartitionTable table) {
            return exists(false, table);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, Map<String, Object> tags) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]) {
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().partitionTables(greedy, master, tags);
            }
            return dao.partitionTables(greedy, master, tags);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]) {
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().partitionTables(greedy, master, tags, name);
            }
            return dao.partitionTables(greedy, master, tags, name);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(Catalog catalog, Schema schema, String master, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().partitionTables(catalog, schema, master, name);
            }
            return dao.partitionTables(false, catalog, schema, master, name);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(Schema schema, String master, String name) {
            return partitionTables(false, null, schema, master, name);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(String master, String name) {
            return partitionTables(false, null, null, master, name);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(String master) {
            return partitionTables(false, null, null, master, null);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master) {
            return dao.partitionTables(false, master);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master, Map<String, Object> tags) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]) {
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().partitionTables(master, tags);
            }
            return dao.partitionTables(false, master, tags);
        }

        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(MasterTable master, Map<String, Object> tags, String name) {
            String[] ps = DataSourceUtil.parseRuntime(master.getName());
            if(null != ps[0]) {
                master.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().partitionTables(master, tags, name);
            }
            return dao.partitionTables(false, master, tags, name);
        }

        @Override
        public PartitionTable partitionTable(boolean greedy, MasterTable master, String name) {
            LinkedHashMap<String, PartitionTable> tables = partitionTables(greedy, master, name);
            if (tables.isEmpty()) {
                return null;
            }
            PartitionTable table = tables.values().iterator().next();
            table.setColumns(columns(table));
            table.setTags(tags(table));
            table.setIndexes(indexes(table));
            return table;
        }

        @Override
        public List<String> ddl(PartitionTable table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
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
         * LinkedHashMap<String, Column> columns(Table table)
         * LinkedHashMap<String, Column> columns(String table)
         * LinkedHashMap<String, Column> columns(Catalog catalog, Schema schema, String table)
         * LinkedHashMap<String, Column> column(Table table, String name);
         * LinkedHashMap<String, Column> column(String table, String name);
         * LinkedHashMap<String, Column> column(Catalog catalog, Schema schema, String table, String name);
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
                    if(null == table) {
                        String tableName = column.getTableName(true);
                        if(BasicUtil.isNotEmpty(tableName)) {
                            table = new Table(column.getCatalog(), column.getSchema(), tableName);
                        }
                    }
                }
                if (null == columns || columns.isEmpty()) {
                    if(null != table) {
                        columns = columns(greedy, table);
                    }
                }
                if (null != columns && columns.containsKey(name)) {
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public <T extends Column> List<T> columns(boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
           return dao.columns(greedy, catalog, schema, configs);
        }
        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table, ConfigStore configs) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().columns(greedy, table, configs);
            }
            LinkedHashMap<String, T> columns = dao.columns(greedy, table, configs);
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
         * LinkedHashMap<String, Tag> tags(Catalog catalog, Schema schema, String table)
         * LinkedHashMap<String, Tag> tags(String table)
         * LinkedHashMap<String, Tag> tags(Table table)
         ******************************************************************************************************************/

        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
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
            if(null != ps[0]) {
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
            if(null != ps[0]) {
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
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().foreigns( greedy, table);
            }
            return dao.foreigns(greedy, table);
        }
        @Override
        public ForeignKey foreign(boolean greedy, Table table, List<String> columns) {
            if(null == columns || columns.isEmpty()) {
                return null;
            }
            LinkedHashMap<String, ForeignKey> foreigns = foreigns(greedy, table);
             Collections.sort(columns);
            String id = BeanUtil.concat(columns).toUpperCase();
            for(ForeignKey foreign:foreigns.values()) {
                List<String> fcols = Column.names(foreign.getColumns());
                Collections.sort(fcols);
                if(id.equals(BeanUtil.concat(fcols).toUpperCase())) {
                    return foreign;
                }
            }
            return null;
        }

        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String, Index> indexes(Table table)
         * LinkedHashMap<String, Index> indexes(String table)
         * LinkedHashMap<String, Index> indexes(Catalog catalog, Schema schema, String table)
         * Index index(Table table, String name);
         * Index index(String table, String name);
         * Index index(String name);
         ******************************************************************************************************************/

        @Override
        public <T extends Index> List<T> indexes(boolean greedy, Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().indexes(greedy, table);
            }
            return dao.indexes(greedy, table);
        }

        @Override
        public <T extends Index> LinkedHashMap<String, T> indexes(Table table) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().indexes(table);
            }
            return dao.indexes(table);
        }

        @Override
        public Index index(boolean greedy, Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().index(greedy, table, name);
            }
            Index index = null;
            List<Index> all = dao.indexes(greedy, table, name);
            if (null != all && !all.isEmpty()) {
                index = all.get(0);
            }
            return index;
        }

        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String, Constraint> constraints(Table table)
         * LinkedHashMap<String, Constraint> constraints(String table)
         * LinkedHashMap<String, Constraint> constraints(Catalog catalog, Schema schema, String table)
         ******************************************************************************************************************/

        @Override
        public <T extends Constraint> List<T> constraints(boolean greedy, Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().constraints(greedy, table, name);
            }
            return dao.constraints(greedy, table);
        }

        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name) {
            String[] ps = DataSourceUtil.parseRuntime(table);
            if(null != ps[0]) {
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
            if(null != ps[0]) {
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
            if(null != ps[0]) {
                table.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().triggers(greedy, table, events);
            }
            return dao.triggers(greedy, table, events);
        }

        @Override
        public Trigger trigger(boolean greedy, Catalog catalog, Schema schema, String name) {
            LinkedHashMap<String, Trigger> triggers = triggers(greedy, new Table(catalog, schema, null), null);
            if(null != triggers) {
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
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().procedures(greedy, catalog, schema, ps[0]);
            }
            return dao.procedures(greedy, catalog, schema, name);
        }
        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().procedures(catalog, schema, ps[0]);
            }
            return dao.procedures(catalog, schema, name);
        }
        @Override
        public Procedure procedure(boolean greedy, Catalog catalog, Schema schema, String name) {
            List<Procedure> procedures = procedures(greedy, catalog, schema, name);
            if(null != procedures && !procedures.isEmpty()) {
                return procedures.get(0);
            }
            return null;
        }

        @Override
        public List<String> ddl(Procedure procedure) {
            String[] ps = DataSourceUtil.parseRuntime(procedure.getName());
            if(null != ps[0]) {
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
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().functions(greedy, catalog, schema, ps[0]);
            }
            return dao.functions(greedy, catalog, schema, name);
        }
        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().functions(catalog, schema, ps[0]);
            }
            return dao.functions(catalog, schema, name);
        }
        @Override
        public Function function(boolean greedy, Catalog catalog, Schema schema, String name) {
            List<Function> functions = functions(greedy, catalog, schema, name);
            if(null != functions && !functions.isEmpty()) {
                return functions.get(0);
            }
            return null;
        }
        @Override
        public List<String> ddl(Function function) {
            String[] ps = DataSourceUtil.parseRuntime(function.getName());
            if(null != ps[0]) {
                function.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(function);
            }
            return dao.ddl(function);
        }

        /* *****************************************************************************************************************
         * 													sequence
         ******************************************************************************************************************/
        @Override
        public <T extends Sequence> List<T> sequences(boolean greedy, Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().sequences(greedy, catalog, schema, ps[0]);
            }
            return dao.sequences(greedy, catalog, schema, name);
        }
        @Override
        public <T extends Sequence> LinkedHashMap<String, T> sequences(Catalog catalog, Schema schema, String name) {
            String[] ps = DataSourceUtil.parseRuntime(name);
            if(null != ps[0]) {
                return ServiceProxy.service(ps[0]).metadata().sequences(catalog, schema, ps[0]);
            }
            return dao.sequences(catalog, schema, name);
        }
        @Override
        public Sequence sequence(boolean greedy, Catalog catalog, Schema schema, String name) {
            List<Sequence> sequences = sequences(greedy, catalog, schema, name);
            if(null != sequences && !sequences.isEmpty()) {
                return sequences.get(0);
            }
            return null;
        }
        @Override
        public List<String> ddl(Sequence sequence) {
            String[] ps = DataSourceUtil.parseRuntime(sequence.getName());
            if(null != ps[0]) {
                sequence.setName(ps[1]);
                return ServiceProxy.service(ps[0]).metadata().ddl(sequence);
            }
            return dao.ddl(sequence);
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
        public boolean save(Table table) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            try {
                Table otable = metadata.table(table.getCatalog(), table.getSchema(), table.getName());
                if (null != otable) {
                    otable.setAutoDropColumn(table.isAutoDropColumn());
                    otable.execute(table.execute());
                    Table update = (Table) table.getUpdate();
                    if (null == update) {
                        update = table;
                    }
                    otable.setUpdate(update, false, false);
                    sort(table);
                    result = dao.alter(otable);
                    table.setDdls(otable.ddls());
                    table.setRuns(otable.runs());
                } else {
                    sort(table);
                    result = dao.create(table);
                }
            }finally {
                CacheProxy.clear();
            }
            return result;
        }

        @Override
        public boolean create(Table table) throws Exception {
            sort(table);
            return dao.create(table);
        }
        protected void sort(Table table) {
            LinkedHashMap<String, Column> columns = table.getColumns();
            boolean sort = false;
            for(Column column:columns.values()) {
                //只要有一个带 位置属性的列就排序
                if(null != column.getPosition()) {
                    sort = true;
                    break;
                }
            }
            if(sort) {
                table.sort();
            }
        }
        @Override
        public boolean alter(Table table) throws Exception {
            CacheProxy.clear();
            try {
                Table update = (Table) table.getUpdate();
                if (null == update) {
                    update = table;
                }
                table = metadata().table(table.getCatalog(), table.getSchema(), table.getName());
                table.setUpdate(update, false, false);
                boolean result = dao.alter(table);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean drop(Table table) throws Exception {
            boolean result = dao.drop(table);
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean rename(Table origin, String name) throws Exception {
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
        public boolean save(View view) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            try {
                View oview = metadata.view(view.getCatalog(), view.getSchema(), view.getName());
                if (null != oview) {
                    oview.setAutoDropColumn(view.isAutoDropColumn());
                    View update = (View) view.getUpdate();
                    if (null == update) {
                        update = view;
                    }
                    oview.setUpdate(update, false, false);
                    result = alter(oview);
                } else {
                    result = create(view);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean create(View view) throws Exception {
            boolean result =  dao.create(view);
            return result;
        }

        @Override
        public boolean alter(View view) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.alter(view);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean drop(View view) throws Exception {
            try {
                boolean result = dao.drop(view);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(View origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
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
            try {
                MasterTable otable = metadata.masterTable(table.getCatalog(), table.getSchema(), table.getName());
                if (null != otable) {
                    otable.setUpdate(table, false, false);
                    result = alter(otable);
                } else {
                    result = create(table);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }

        }

        @Override
        public boolean create(MasterTable table) throws Exception {
            boolean result =  dao.create(table);
            return result;
        }


        @Override
        public boolean alter(MasterTable table) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.alter(table);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean drop(MasterTable table) throws Exception {
            try {
                boolean result = dao.drop(table);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(MasterTable origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }

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
            try {
                PartitionTable otable = metadata.partitionTable(table.getCatalog(), table.getSchema(), table.getMasterName(), table.getName());
                if (null != otable) {
                    otable.setUpdate(table, false, false);
                    result = alter(otable);
                } else {
                    result = create(table);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean create(PartitionTable table) throws Exception {
            boolean result =  dao.create(table);
            return result;
        }


        @Override
        public boolean alter(PartitionTable table) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.alter(table);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean drop(PartitionTable table) throws Exception {
            try {
                boolean result = dao.drop(table);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(PartitionTable origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
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
        public boolean save(Column column) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            try {
                Table table = metadata.table(column.getCatalog(), column.getSchema(), column.getTableName(true));
                if (null == table) {
                    throw new AnylineException("表不存在:" + column.getTableName(true));
                }
                LinkedHashMap<String, Column> columns = table.getColumns();
                Column origin = columns.get(column.getName().toUpperCase());
                if (null == origin) {
                    result = add(columns, column);
                } else {
                    result = alter(table, column);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean add(Column column) throws Exception {
            CacheProxy.clear();
            try {
                LinkedHashMap<String, Column> columns = metadata.columns(column.getCatalog(), column.getSchema(), column.getTableName(true));
                boolean result = add(columns, column);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean alter(Column column) throws Exception {
            CacheProxy.clear();
            try {
                Table table = metadata.table(column.getCatalog(), column.getSchema(), column.getTableName(true));
                boolean result = alter(table, column);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean drop(Column column) throws Exception {
            try {
                boolean result = dao.drop(column);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        private boolean add(LinkedHashMap<String, Column> columns, Column column) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.add(column);
                if (result) {
                    columns.put(column.getName().toUpperCase(), column);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        /**
         * 修改列
         * @param table 表
         * @param column 修改目标
         * @return boolean
         * @throws Exception 异常 sql异常
         */
        private boolean alter(Table table, Column column) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            try {
                LinkedHashMap<String, Column> columns = table.getColumns();
                Column origin = columns.get(column.getName().toUpperCase());

                Column update = column.getUpdate();
                if (null == update) {
                    update = column.clone();
                }
                origin.setUpdate(update, false, false);
                String name = origin.getName();
                try {
                    result = dao.alter(table, origin);
                } finally {
                    origin.setName(name);
                }
                if (result) {
                    columns.remove(origin.getName());

                    BeanUtil.copyFieldValueWithoutNull(origin, update);
                    origin.setUpdate(update, false, false);
                    BeanUtil.copyFieldValue(column, origin);
                    column.setUpdate(update, false, false);
                    columns.put(origin.getName().toUpperCase(), origin);
                }
                column.setTable(table);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean rename(Column origin, String name) throws Exception {
            try {
                origin.setNewName(name);
                boolean result = alter(origin);//dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
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
        public boolean save(Tag tag) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            try {
                Table table = metadata.table(tag.getCatalog(), tag.getSchema(), tag.getTableName(true));
                if (null == table) {
                    throw new AnylineException("表不存在:" + tag.getTableName(true));
                }
                LinkedHashMap<String, Tag> tags = table.getTags();
                Tag original = tags.get(tag.getName().toUpperCase());
                if (null == original) {
                    result = add(tags, tag);
                } else {
                    result = alter(table, tag);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean add(Tag tag) throws Exception {
            CacheProxy.clear();
            try {
                LinkedHashMap<String, Tag> tags = metadata.tags(tag.getCatalog(), tag.getSchema(), tag.getTableName(true));
                boolean result = add(tags, tag);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean alter(Tag tag) throws Exception {
            CacheProxy.clear();
            try {
                Table table = metadata.table(tag.getCatalog(), tag.getSchema(), tag.getTableName(true));
                boolean result = alter(table, tag);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean drop(Tag tag) throws Exception {
            try {
                boolean result = dao.drop(tag);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        private boolean add(LinkedHashMap<String, Tag> tags, Tag tag) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.add(tag);
                if (result) {
                    tags.put(tag.getName(), tag);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        /**
         * 修改标签
         * @param table 表
         * @param tag 修改目标
         * @return boolean
         * @throws Exception 异常 sql异常
         */
        private boolean alter(Table table, Tag tag) throws Exception {
            boolean result = false;
            CacheProxy.clear();
            try {
                LinkedHashMap<String, Tag> tags = table.getTags();
                Tag origin = tags.get(tag.getName().toUpperCase());

                Tag update = tag.getUpdate();
                if (null == update) {
                    update = tag.clone();
                }
                origin.setUpdate(update, false, false);
                result = dao.alter(table, origin);
                if (result) {
                    tags.remove(origin.getName());

                    BeanUtil.copyFieldValueWithoutNull(origin, update);
                    origin.setUpdate(update, false, false);
                    BeanUtil.copyFieldValue(tag, origin);
                    tags.put(origin.getName(), origin);
                }
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean rename(Tag origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        /* *****************************************************************************************************************
         * 													primary
         * -----------------------------------------------------------------------------------------------------------------
         * boolean add(PrimaryKey primary) throws Exception
         * boolean alter(PrimaryKey primary) throws Exception
         * boolean drop(PrimaryKey primary) throws Exception
         ******************************************************************************************************************/


        @Override
        public boolean add(PrimaryKey primary) throws Exception {
            CacheProxy.clear();
            try {
                return dao.add(primary);
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean alter(PrimaryKey primary) throws Exception {
            CacheProxy.clear();
            return false;
        }

        @Override
        public boolean drop(PrimaryKey primary) throws Exception {
            try {
                boolean result = dao.drop(primary);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(PrimaryKey origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        /* *****************************************************************************************************************
         * 													foreign
         ******************************************************************************************************************/

        @Override
        public boolean add(ForeignKey foreign) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.add(foreign);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean alter(ForeignKey foreign) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.alter(foreign);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean drop(ForeignKey foreign) throws Exception {
            try {
                if (BasicUtil.isEmpty(foreign.getName())) {
                    List<String> names = Column.names(foreign.getColumns());
                    foreign = metadata.foreign(foreign.getTable(true), names);
                }
                boolean result = dao.drop(foreign);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }

        @Override
        public boolean rename(ForeignKey origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
         * boolean add(Index index) throws Exception
         * boolean alter(Index index) throws Exception
         * boolean drop(Index index) throws Exception
         ******************************************************************************************************************/


        @Override
        public boolean add(Index index) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.add(index);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean alter(Index index) throws Exception {
            CacheProxy.clear();
            return dao.alter(index);
        }

        @Override
        public boolean drop(Index index) throws Exception {
            try {
                boolean result = dao.drop(index);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(Index origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
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
            try {
                boolean result = dao.add(constraint);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }


        @Override
        public boolean alter(Constraint constraint) throws Exception {
            CacheProxy.clear();
            return false;
        }


        @Override
        public boolean drop(Constraint constraint) throws Exception {
            try {
                boolean result = dao.drop(constraint);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(Constraint origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
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
        public boolean create(Trigger trigger) throws Exception {
            boolean result = dao.add(trigger);
            return result;
        }
        @Override
        public boolean alter(Trigger trigger) throws Exception {
            CacheProxy.clear();
            try {
                boolean result = dao.alter(trigger);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean drop(Trigger trigger) throws Exception {
            try {
                boolean result = dao.drop(trigger);
                return result;
            }finally {
                CacheProxy.clear();
            }
        }
        @Override
        public boolean rename(Trigger origin, String name) throws Exception {
            try {
                boolean result = dao.rename(origin, name);
                return result;
            }finally {
                CacheProxy.clear();
            }
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
        public boolean create(Procedure procedure) throws Exception {
            return dao.create(procedure);
        }
        @Override
        public boolean alter(Procedure procedure) throws Exception {
            boolean result = dao.alter(procedure);
            return result;
        }
        @Override
        public boolean drop(Procedure procedure) throws Exception {
            boolean result = dao.drop(procedure);
            return result;
        }
        @Override
        public boolean rename(Procedure origin, String name) throws Exception {
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
        public boolean create(Function function) throws Exception {
            boolean result = dao.create(function);
            return result;
        }
        @Override
        public boolean alter(Function function) throws Exception {
            boolean result = dao.alter(function);
            return result;
        }
        @Override
        public boolean drop(Function function) throws Exception {
            boolean result = dao.drop(function);
            return result;
        }
        @Override
        public boolean rename(Function origin, String name) throws Exception {
            boolean result = dao.rename(origin, name);
            return result;
        }

        /* *****************************************************************************************************************
         * 													sequence
         ******************************************************************************************************************/
        /**
         * 函数
         * @param sequence 序列
         * @return boolean
         * @throws Exception 异常 Exception
         */
        @Override
        public boolean create(Sequence sequence) throws Exception {
            boolean result = dao.create(sequence);
            return result;
        }
        @Override
        public boolean alter(Sequence sequence) throws Exception {
            boolean result = dao.alter(sequence);
            return result;
        }
        @Override
        public boolean drop(Sequence sequence) throws Exception {
            boolean result = dao.drop(sequence);
            return result;
        }
        @Override
        public boolean rename(Sequence origin, String name) throws Exception {
            boolean result = dao.rename(origin, name);
            return result;
        }
    };
}
