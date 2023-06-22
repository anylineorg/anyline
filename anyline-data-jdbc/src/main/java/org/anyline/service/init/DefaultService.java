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
 *
 *
 */


package org.anyline.service.init;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.dao.AnylineDao;
import org.anyline.data.cache.CacheUtil;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.init.DefaultSQLStore;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.*;
import org.anyline.entity.data.*;
import org.anyline.exception.AnylineException;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
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

    @Override 
    public AnylineService datasource(String datasource) {
        DataSourceHolder.setDataSource(datasource);
        return this;
    }

    @Override 
    public AnylineService datasource() {
        DataSourceHolder.setDefaultDataSource();
        return this;
    }

    @Override 
    public AnylineService setDataSource(String datasource) {
        DataSourceHolder.setDataSource(datasource);
        return this;
    }

    @Override 
    public AnylineService setDataSource(String datasource, boolean auto) {
        DataSourceHolder.setDataSource(datasource, auto);
        return this;
    }

    @Override 
    public AnylineService setDefaultDataSource() {
        DataSourceHolder.setDefaultDataSource();
        return this;
    }

    // 恢复切换前数据源
    @Override 
    public AnylineService recoverDataSource() {
        DataSourceHolder.recoverDataSource();
        return this;
    }

    @Override 
    public String getDataSource() {
        return DataSourceHolder.curDataSource();
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
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        configs = append(configs, obj);
        return queryFromDao(src, configs, conditions);
    }

    
    @Override 
    public DataSet querys(String src, PageNavi navi, Object obj, String... conditions) {
        ConfigStore configs = new DefaultConfigStore();
        configs.setPageNavi(navi);
        return querys(src, configs, obj, conditions);
    }

    
    @Override 
    public DataSet querys(String src, Object obj, String... conditions) {
        return querys(src, (ConfigStore) null, obj, conditions);
    }

    
    @Override 
    public DataSet querys(String src, int fr, int to, Object obj, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return querys(src, configs, obj, conditions);
    }


    
    @Override 
    public DataSet querys(String src, ConfigStore configs, String... conditions) {
        return querys(src, configs, null, conditions);
    }

    
    @Override 
    public DataSet querys(String src, PageNavi navi, String... conditions) {
        return querys(src, navi, null, conditions);
    }

    
    @Override 
    public DataSet querys(String src, String... conditions) {
        return querys(src, (Object) null, conditions);
    }

    
    @Override 
    public DataSet querys(String src, int fr, int to, String... conditions) {
        return querys(src, fr, to, null, conditions);
    }

    
    @Override 
    public List<String> column2param(String table) {
        List<String> columns = columns(table);
        return EntityAdapterProxy.column2param(columns);
    }

    
    @Override 
    public List<Map<String, Object>> maps(String src, ConfigStore configs, Object obj, String... conditions) {
        List<Map<String, Object>> maps = null;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][src:{}]", src);
        }
        try {
            RunPrepare prepare = createRunPrepare(src);
            configs = append(configs, obj);
            maps = dao.maps(prepare, configs, conditions);
        } catch (Exception e) {
            maps = new ArrayList<Map<String, Object>>();
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
            log.error("QUERY ERROR:" + e);
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return maps;
    }

    
    @Override 
    public List<Map<String, Object>> maps(String src, Object obj, String... conditions) {
        return maps(src, null, obj, conditions);
    }

    
    @Override 
    public List<Map<String, Object>> maps(String src, int fr, int to, Object obj, String... conditions) {
        return maps(src, new DefaultConfigStore(fr, to), obj, conditions);
    }

    
    @Override 
    public List<Map<String, Object>> maps(String src, ConfigStore configs, String... conditions) {
        return maps(src, configs, null, conditions);
    }

    
    @Override 
    public List<Map<String, Object>> maps(String src, String... conditions) {
        return maps(src, null,null, conditions);
    }


    @Override
    public List<Map<String, Object>> maps(String src, int fr, int to, String... conditions) {
        return maps(src, fr, to, null, conditions);
    }
    @Override
    public List<Map<String, Object>> maps(String src, PageNavi navi, String... conditions) {
        return maps(src,new DefaultConfigStore().setPageNavi(navi), null, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, String src, ConfigStore configs, Object obj, String... conditions) {
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
    public DataSet caches(String cache, String src, Object obj, String... conditions) {
        return caches(cache, src, null, obj, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, String src, int fr, int to, Object obj, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return caches(cache, src, configs, obj, conditions);
    }


    
    @Override 
    public DataSet caches(String cache, String src, ConfigStore configs, String... conditions) {
        return caches(cache, src, configs, (Object) null, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, String src, String... conditions) {
        return caches(cache, src, null, null, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, String src, int fr, int to, String... conditions) {
        return caches(cache, src, fr, to, null, conditions);
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
        if (ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            return new DataRow();
        }
        return null;
    }


    
    @Override 
    public DataRow query(String src, Object obj, String... conditions) {
        return query(src, (ConfigStore) null, obj, conditions);
    }

    
    @Override 
    public DataRow query(String src, ConfigStore store, String... conditions) {
        return query(src, store, null, conditions);
    }


    
    @Override 
    public DataRow query(String src, String... conditions) {
        return query(src, (ConfigStore) null, conditions);
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
    public BigDecimal sequence(String name) {
        return sequence(true, name);
    }

    
    @Override 
    public DataRow sequences(boolean next, String... names) {
        return dao.sequence(next, names);
    }

    
    @Override 
    public DataRow sequences(String... names) {
        return sequences(true, names);
    }

    
    @Override 
    public DataRow cache(String cache, String src, ConfigStore configs, Object obj, String... conditions) {
        // 是否启动缓存
        if (null == cache || null == cacheProvider || ThreadConfig.check(DataSourceHolder.curDataSource()).IS_CACHE_DISABLED()) {
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
        if (null == row && ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            row = new DataRow();
        }
        return row;
    }

    
    @Override 
    public DataRow cache(String cache, String src, Object obj, String... conditions) {
        return cache(cache, src, null, obj, conditions);
    }

    
    @Override 
    public DataRow cache(String cache, String src, ConfigStore configs, String... conditions) {
        return cache(cache, src, configs, null, conditions);
    }

    
    @Override 
    public DataRow cache(String cache, String src, String... conditions) {
        return cache(cache, src, null, null, conditions);
    }


    @Deprecated
    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        return selects(clazz, configs, entity, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, T entity, String... conditions) {
        return selects(clazz, navi, entity, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, T entity, String... conditions) {
        return selects(clazz, entity, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, int first, int last, T entity, String... conditions) {
        return selects(clazz, first, last, entity, conditions);
    }

    @Override 
    public <T> T query(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        return select(clazz, configs, entity, conditions);
    }

    @Override 
    public <T> T query(Class<T> clazz, T entity, String... conditions) {
        return select(clazz, entity, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String... conditions) {
        return selects(clazz, configs, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, String... conditions) {
        return selects(clazz, navi, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, String... conditions) {
        return selects(clazz, conditions);
    }

    @Override 
    public <T> EntitySet<T> querys(Class<T> clazz, int first, int last, String... conditions) {
        return selects(clazz, first, last, conditions);
    }

    @Override 
    public <T> T query(Class<T> clazz, ConfigStore configs, String... conditions) {
        return select(clazz, configs, conditions);
    }

    @Override 
    public <T> T query(Class<T> clazz, String... conditions) {
        return select(clazz, conditions);
    }


    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        return queryFromDao(clazz, append(configs, entity), conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, T entity, String... conditions) {
        ConfigStore configs = new DefaultConfigStore();
        configs.setPageNavi(navi);
        return selects(clazz, configs, entity, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, T entity, String... conditions) {
        return selects(clazz, (ConfigStore) null, entity, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, int fr, int to, T entity, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return selects(clazz, configs, entity, conditions);
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
        if (ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            try {
                return (T) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    
    @Override 
    public <T> T select(Class<T> clazz, T entity, String... conditions) {
        return select(clazz, (ConfigStore) null, entity, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, String... conditions) {
        return selects(clazz, configs, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, String... conditions) {
        return selects(clazz, navi, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, String... conditions) {
        return selects(clazz, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(Class<T> clazz, int fr, int to, String... conditions) {
        return selects(clazz, fr, to, (T) null, conditions);
    }

    
    @Override 
    public <T> T select(Class<T> clazz, ConfigStore configs, String... conditions) {
        return select(clazz, configs, (T) null, conditions);
    }

    
    @Override 
    public <T> T select(Class<T> clazz, String... conditions) {
        return select(clazz, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        return queryFromDao(src, clazz, append(configs, entity), conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, T entity, String... conditions) {
        ConfigStore configs = new DefaultConfigStore();
        configs.setPageNavi(navi);
        return selects(src, clazz, configs, entity, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, T entity, String... conditions) {
        return selects(src, clazz, (ConfigStore) null, entity, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, int fr, int to, T entity, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return selects(src, clazz, configs, entity, conditions);
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
        if (ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            try {
                return (T) clazz.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    
    @Override 
    public <T> T select(String src, Class<T> clazz, T entity, String... conditions) {
        return select(src, clazz, (ConfigStore) null, entity, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, String... conditions) {
        return selects(src, clazz, configs, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, String... conditions) {
        return selects(src, clazz, navi, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, String... conditions) {
        return selects(src, clazz, (T) null, conditions);
    }

    
    @Override 
    public <T> EntitySet<T> selects(String src, Class<T> clazz, int fr, int to, String... conditions) {
        return selects(src, clazz, fr, to, (T) null, conditions);
    }

    
    @Override 
    public <T> T select(String src, Class<T> clazz, ConfigStore configs, String... conditions) {
        return select(src, clazz, configs, (T) null, conditions);
    }

    
    @Override 
    public <T> T select(String src, Class<T> clazz, String... conditions) {
        return select(src, clazz, (T) null, conditions);
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
    public EntitySet<E> gets(PageNavi navi, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return selects(clazz, navi, conditions);
    }

    
    @Override 
    public EntitySet<E> gets(String... conditions) {
        Class<E> clazz = parseGenericClass();
        return selects(clazz, conditions);
    }

    
    @Override 
    public EntitySet<E> gets(int fr, int to, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return selects(clazz, fr, to, conditions);
    }

    
    @Override 
    public E get(ConfigStore configs, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return select(clazz, configs, conditions);
    }

    
    @Override 
    public E get(String... conditions) {
        Class<E> clazz = parseGenericClass();
        return select(clazz, conditions);
    }


    /**
     * 按条件查询
     *
     * @param prepare    表｜视图｜函数｜自定义SQL |RunPrepare
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
    public DataSet querys(RunPrepare prepare, ConfigStore configs, String... conditions) {
        return querys(prepare, configs, null, conditions);
    }

    
    @Override 
    public DataSet querys(RunPrepare prepare, Object obj, String... conditions) {
        return querys(prepare, null, obj, conditions);
    }

    
    @Override 
    public DataSet querys(RunPrepare prepare, String... conditions) {
        return querys(prepare, null, null, conditions);
    }


    
    @Override 
    public DataSet querys(RunPrepare prepare, int fr, int to, Object obj, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return querys(prepare, configs, obj, conditions);
    }

    
    @Override 
    public DataSet querys(RunPrepare prepare, int fr, int to, String... conditions) {
        return querys(prepare, fr, to, null, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, RunPrepare table, ConfigStore configs, Object obj, String... conditions) {
        DataSet set = null;
        conditions = BasicUtil.compress(conditions);
        if (null == cache) {
            set = querys(table, configs, obj, conditions);
        } else {
            if (null != cacheProvider) {
                // set = queryFromCache(cache, table, configs, conditions);
            } else {
                set = querys(table, configs, obj, conditions);
            }
        }
        return set;
    }

    
    @Override 
    public DataSet caches(String cache, RunPrepare table, ConfigStore configs, String... conditions) {
        return caches(cache, table, configs, null, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, RunPrepare table, Object obj, String... conditions) {
        return caches(cache, table, null, obj, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, RunPrepare table, String... conditions) {
        return caches(cache, table, null, null, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, RunPrepare table, int fr, int to, Object obj, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return caches(cache, table, configs, obj, conditions);
    }

    
    @Override 
    public DataSet caches(String cache, RunPrepare table, int fr, int to, String... conditions) {
        return caches(cache, table, fr, to, null, conditions);
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
        if (ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            return new DataRow();
        }
        return null;
    }

    
    @Override 
    public DataRow query(RunPrepare table, ConfigStore store, String... conditions) {
        return query(table, store, null, conditions);
    }

    
    @Override 
    public DataRow query(RunPrepare table, Object obj, String... conditions) {
        return query(table, null, obj, conditions);
    }

    
    @Override 
    public DataRow query(RunPrepare table, String... conditions) {
        return query(table, null, null, conditions);
    }

    
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
        if (null == row && ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            row = new DataRow();
        }
        return row;
    }

    
    @Override 
    public DataRow cache(String cache, RunPrepare table, ConfigStore configs, String... conditions) {
        return cache(cache, table, configs, null, conditions);
    }

    
    @Override 
    public DataRow cache(String cache, RunPrepare table, Object obj, String... conditions) {
        return cache(cache, table, null, obj, conditions);
    }

    
    @Override 
    public DataRow cache(String cache, RunPrepare table, String... conditions) {
        return cache(cache, table, null, null, conditions);
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

    
    @Override 
    public boolean removeCache(String channel, String src, String... conditions) {
        return removeCache(channel, src, null, conditions);
    }

    
    @Override 
    public boolean removeCache(String channel, String src, int fr, int to, String... conditions) {
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return removeCache(channel, src, configs, conditions);
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


    /**
     * 检查唯一性
     *
     * @param src        src
     * @param configs    根据http等上下文构造查询条件
     * @param obj        根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return boolean
     */

    
    @Override 
    public boolean exists(String src, ConfigStore configs, Object obj, String... conditions) {
        boolean result = false;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(src);
        result = dao.exists(prepare, append(configs, obj), conditions);
        return result;
    }

    
    @Override 
    public boolean exists(String src, ConfigStore configs, String... conditions) {
        return exists(src, configs, null, conditions);
    }

    
    @Override 
    public boolean exists(String src, Object obj, String... conditions) {
        return exists(src, null, obj, conditions);
    }

    
    @Override 
    public boolean exists(String src, String... conditions) {
        return exists(src, null, null, conditions);
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

    
    @Override 
    public boolean exists(DataRow row) {
        return exists(null, row);
    }

    
    @Override 
    public int count(String src, ConfigStore configs, Object obj, String... conditions) {
        int count = -1;
        try {
            // conditions = parseConditions(conditions);
            src = BasicUtil.compress(src);
            conditions = BasicUtil.compress(conditions);
            RunPrepare prepare = createRunPrepare(src);
            count = dao.count(prepare, append(configs, obj), conditions);
        } catch (Exception e) {
            log.error("COUNT ERROR:" + e);
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            } else {
                e.printStackTrace();
            }
        }
        return count;
    }

    
    @Override 
    public int count(String src, ConfigStore configs, String... conditions) {
        return count(src, configs, null, conditions);
    }

    
    @Override 
    public int count(String src, Object obj, String... conditions) {
        return count(src, null, obj, conditions);
    }

    
    @Override 
    public int count(String src, String... conditions) {
        return count(src, null, null, conditions);
    }


    /**
     * 更新记录
     * 默认情况下以主键为更新条件,需在更新的数据保存在data中
     * 如果提供了dest则更新dest表,如果没有提供则根据data解析出表名
     * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键,则根据TYPE_CODE更新
     * 可以提供了ConfigStore以实现更复杂的更新条件
     * 需要更新的列通过fixs/columns提供
     *
     * @param fixs    需要更新的列
     * @param columns 需要更新的列
     * @param dest    表
     * @param data    更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
     * @param configs 更新条件
     * @return int 影响行数
     */
    
    @Override 
    public int update(boolean async, String dest, Object data, ConfigStore configs, List<String> fixs, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest, dest);
        fixs = BeanUtil.merge(fixs, columns);
        final List<String> cols = BeanUtil.merge(fixs, columns);
        final String _dest = BasicUtil.compress(dest);
        final Object _data = data;
        final ConfigStore _configs = configs;
        if (async) {
            new Thread(new Runnable() {
            @Override 
            public void run() {
                        dao.update(_dest, _data, _configs, cols);
                }
            }).start();
            return 0;
        } else {
            return dao.update(dest, data, configs, cols);
        }
    }

    
    @Override 
    public int update(boolean async, String dest, Object data, List<String> fixs, String... columns) {
        return update(async, dest, data, null, fixs, columns);
    }

    
    @Override 
    public int update(boolean async, String dest, Object data, String[] fixs, String... columns) {
        return update(async, dest, data, null, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(boolean async, String dest, Object data, String... columns) {
        return update(async, dest, data, null, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int update(boolean async, String dest, Object data, ConfigStore configs, String[] fixs, String... columns) {
        return update(async, dest, data, configs, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(boolean async, String dest, Object data, ConfigStore configs, String... columns) {
        return update(async, dest, data, configs, BeanUtil.array2list(columns));
    }


    
    @Override 
    public int update(boolean async, Object data, ConfigStore configs, List<String> fixs, String... columns) {
        return update(async, null, data, configs, fixs, columns);
    }

    
    @Override 
    public int update(boolean async, Object data, List<String> fixs, String... columns) {
        return update(async, null, data, null, fixs, columns);
    }

    
    @Override 
    public int update(boolean async, Object data, String[] fixs, String... columns) {
        return update(async, null, data, null, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(boolean async, Object data, String... columns) {
        return update(async, null, data, null, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int update(boolean async, Object data, ConfigStore configs, String[] fixs, String... columns) {
        return update(async, null, data, configs, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(boolean async, Object data, ConfigStore configs, String... columns) {
        return update(async, null, data, configs, BeanUtil.array2list(columns));
    }


    
    @Override 
    public int update(String dest, Object data, ConfigStore configs, List<String> fixs, String... columns) {
        return update(false, dest, data, configs, fixs, columns);
    }

    
    @Override 
    public int update(String dest, Object data, ConfigStore configs, String[] fixs, String... columns) {
        return update(false, dest, data, configs, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(String dest, Object data, ConfigStore configs, String... columns) {
        return update(false, dest, data, configs, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int update(String dest, Object data, List<String> fixs, String... columns) {
        return update(false, dest, data, null, fixs, columns);
    }

    
    @Override 
    public int update(String dest, Object data, String[] fixs, String... columns) {
        return update(false, dest, data, null, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(String dest, Object data, String... columns) {
        return update(false, dest, data, null, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int update(Object data, ConfigStore configs, List<String> fixs, String... columns) {
        return update(false, null, data, configs, fixs, columns);
    }

    
    @Override 
    public int update(Object data, ConfigStore configs, String[] fixs, String... columns) {
        return update(false, null, data, configs, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(Object data, ConfigStore configs, String... columns) {
        return update(false, null, data, configs, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int update(Object data, List<String> fixs, String... columns) {
        return update(false, null, data, null, fixs, columns);
    }

    
    @Override 
    public int update(Object data, String[] fixs, String... columns) {
        return update(false, null, data, null, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int update(Object data, String... columns) {
        return update(false, null, data, null, BeanUtil.array2list(columns));
    }


    @Override 
    public int save(boolean async, String dest, Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return save(async, dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }

    @Override 
    public int save(boolean async, String dest, Object data, boolean checkPrimary, String... columns) {
        return save(async, dest, data, checkPrimary, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int save(boolean async, String dest, Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        if (async) {

            final String _dest = dest;
            final Object _data = data;
            final boolean _chk = checkPrimary;
            final String[] cols = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
            new Thread(new Runnable() {
                @Override 
                public void run() {
                    save(_dest, _data, _chk, cols);
                }

            }).start();
            return 0;
        } else {
            return save(dest, data, checkPrimary, columns);
        }

    }

    @SuppressWarnings("rawtypes")
    
    @Override 
    public int save(String dest, Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        if (null == data) {
            return 0;
        }
        columns = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
        if (data instanceof Collection) {
            Collection datas = (Collection) data;
            int cnt = 0;
            for (Object obj : datas) {
                cnt += save(dest, obj, checkPrimary, columns);
            }
            return cnt;
        }
        return saveObject(dest, data, checkPrimary, columns);
    }

    
    @Override 
    public int save(String dest, Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return save(dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int save(String dest, Object data, boolean checkPrimary, String... columns) {
        return save(dest, data, checkPrimary, BeanUtil.array2list(columns));
    }

    
    @Override 
    public int save(Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        return save(null, data, checkPrimary, BeanUtil.merge(fixs, columns));
    }

    
    @Override 
    public int save(Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return save(null, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int save(Object data, boolean checkPrimary, String... columns) {
        return save(null, data, checkPrimary, columns);
    }

    
    @Override 
    public int save(boolean async, Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        return save(async, null, data, checkPrimary, fixs, columns);
    }

    
    @Override 
    public int save(boolean async, Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return save(async, null, data, checkPrimary, fixs, columns);
    }

    
    @Override 
    public int save(boolean async, Object data, boolean checkPrimary, String... columns) {
        return save(async, null, data, checkPrimary, columns);
    }


    
    @Override 
    public int save(Object data, List<String> fixs, String... columns) {
        return save(null, data, false, fixs, columns);
    }

    
    @Override 
    public int save(Object data, String[] fixs, String... columns) {
        return save(null, data, false, fixs, columns);
    }

    
    @Override 
    public int save(Object data, String... columns) {
        return save(null, data, false, columns);
    }


    
    @Override 
    public int save(boolean async, Object data, List<String> fixs, String... columns) {
        return save(async, null, data, false, fixs, columns);
    }


    
    @Override 
    public int save(boolean async, Object data, String[] fixs, String... columns) {
        return save(async, null, data, false, fixs, columns);
    }


    
    @Override 
    public int save(boolean async, Object data, String... columns) {
        return save(async, null, data, false, columns);
    }


    
    @Override 
    public int save(String dest, Object data, List<String> fixs, String... columns) {
        return save(dest, data, false, fixs, columns);
    }

    
    @Override 
    public int save(String dest, Object data, String[] fixs, String... columns) {
        return save(dest, data, false, fixs, columns);
    }

    
    @Override 
    public int save(String dest, Object data, String... columns) {
        return save(dest, data, false, columns);
    }

    
    @Override 
    public int save(boolean async, String dest, Object data, List<String> fixs, String... columns) {
        return save(async, dest, data, false, fixs, columns);
    }

    
    @Override 
    public int save(boolean async, String dest, Object data, String[] fixs, String... columns) {
        return save(async, dest, data, false, fixs, columns);
    }

    
    @Override 
    public int save(boolean async, String dest, Object data, String... columns) {
        return save(async, dest, data, false, columns);
    }

    protected int saveObject(String dest, Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        if (BasicUtil.isEmpty(dest)) {
            if (data instanceof DataRow || data instanceof DataSet) {
                dest = DataSourceHolder.parseDataSource(dest, data);
            } else {
                if (EntityAdapterProxy.hasAdapter()) {
                    dest = EntityAdapterProxy.table(data.getClass()).getName();
                }
            }
        }

        return dao.save(dest, data, checkPrimary, BeanUtil.list2array(BeanUtil.merge(fixs, columns)));
    }

    protected int saveObject(String dest, Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return saveObject(dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int insert(String dest, Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest, data);
        columns = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
        return dao.insert(dest, data, checkPrimary, columns);
    }

    
    @Override 
    public int insert(String dest, Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return insert(dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int insert(String dest, Object data, boolean checkPrimary, String... columns) {
        return insert(dest, data, checkPrimary, BeanUtil.array2list(columns));
    }


    
    @Override 
    public int insert(Object data, boolean checkPrimary, List<String> fixs, String... columns) {
        return insert(null, data, checkPrimary, fixs, columns);
    }

    
    @Override 
    public int insert(Object data, boolean checkPrimary, String[] fixs, String... columns) {
        return insert(null, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }

    
    @Override 
    public int insert(Object data, boolean checkPrimary, String... columns) {
        return insert(null, data, checkPrimary, BeanUtil.array2list(columns));
    }


    
    @Override 
    public int insert(Object data, List<String> fixs, String... columns) {
        return insert(null, data, false, fixs, columns);
    }

    
    @Override 
    public int insert(Object data, String[] fixs, String... columns) {
        return insert(null, data, false, fixs, columns);
    }

    
    @Override 
    public int insert(Object data, String... columns) {
        return insert(null, data, false, columns);
    }


    
    @Override 
    public int insert(String dest, Object data, List<String> fixs, String... columns) {
        return insert(dest, data, false, fixs, columns);
    }

    
    @Override 
    public int insert(String dest, Object data, String[] fixs, String... columns) {
        return insert(dest, data, false, fixs, columns);
    }

    
    @Override 
    public int insert(String dest, Object data, String... columns) {
        return insert(dest, data, false, columns);
    }


    
    @Override 
    public boolean executeProcedure(String procedure, String... inputs) {
        Procedure proc = new Procedure();
        proc.setName(procedure);
        for (String input : inputs) {
            proc.addInput(input);
        }
        return execute(proc);
    }

    
    @Override 
    public boolean execute(Procedure procedure, String... inputs) {
        procedure.setName(DataSourceHolder.parseDataSource(procedure.getName(), null));
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
            procedure.setName(DataSourceHolder.parseDataSource(procedure.getName()));
            if (null != inputs) {
                for (String input : inputs) {
                    procedure.addInput(input);
                }
            }
            set = dao.querys(procedure, navi);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            log.error("QUERY ERROR:" + e);
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return set;
    }

    @Override 
    public DataSet querys(Procedure procedure, String... inputs) {
        return querys(procedure, null, inputs);
    }

    @Override 
    public DataSet querys(Procedure procedure, int fr, int to, String... inputs) {
        PageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        return querys(procedure, navi, inputs);
    }

    
    @Override 
    public DataSet querysProcedure(String procedure, PageNavi navi, String... inputs) {
        Procedure proc = new Procedure();
        proc.setName(procedure);
        if (null != inputs) {
            for (String input : inputs) {
                proc.addInput(input);
            }
        }
        return querys(proc, navi);
    }

    @Override 
    public DataSet querysProcedure(String procedure, int fr, int to, String... inputs) {
        PageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        return querysProcedure(procedure, navi, inputs);
    }

    @Override 
    public DataSet querysProcedure(String procedure, String... inputs) {
        return querysProcedure(procedure, null, inputs);
    }

    @Override 
    public DataRow queryProcedure(String procedure, String... inputs) {
        Procedure proc = new Procedure();
        proc.setName(procedure);
        return query(procedure, inputs);
    }

    @Override 
    public DataRow query(Procedure procedure, String... inputs) {
        DataSet set = querys(procedure, 0, 0, inputs);
        if (set.size() > 0) {
            return set.getRow(0);
        }
        if (ThreadConfig.check(DataSourceHolder.curDataSource()).IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL()) {
            return new DataRow();
        }
        return null;
    }

    
    @Override 
    public int execute(String src, ConfigStore store, String... conditions) {
        int result = -1;
        src = BasicUtil.compress(src);
        src = DataSourceHolder.parseDataSource(src);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(src);
        if (null == prepare) {
            return result;
        }
        result = dao.execute(prepare, store, conditions);
        return result;
    }


    
    @Override 
    public int execute(String src, String... conditions) {
        return execute(src, null, conditions);
    }

    @SuppressWarnings("rawtypes")
    
    @Override 
    public int delete(String dest, DataSet set, String... columns) {
        int cnt = 0;
        int size = set.size();
        for (int i = 0; i < size; i++) {
            cnt += delete(dest, set.getRow(i), columns);
        }
        log.info("[delete DataSet][影响行数:{}]", LogUtil.format(cnt, 34));
        return cnt;
    }

    
    @Override 
    public int delete(DataSet set, String... columns) {
        String dest = DataSourceHolder.parseDataSource(null, set);
        return delete(dest, set, columns);
    }

    
    @Override 
    public int delete(String dest, DataRow row, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest, row);
        return dao.delete(dest, row, columns);
    }

    
    @Override 
    public int delete(Object obj, String... columns) {
        if (null == obj) {
            return 0;
        }
        String dest = null;
        if (obj instanceof DataRow) {
            DataRow row = (DataRow) obj;
            dest = DataSourceHolder.parseDataSource(null, row);
            return dao.delete(dest, row, columns);
        } else {
            if (EntityAdapterProxy.hasAdapter()) {
                if (obj instanceof Collection) {
                    dest = EntityAdapterProxy.table(((Collection) obj).iterator().next().getClass()).getName();
                } else {
                    dest = EntityAdapterProxy.table(obj.getClass()).getName();
                }
                return dao.delete(dest, obj, columns);
            }
        }
        return 0;
    }

    
    @Override 
    public int delete(String table, String... kvs) {
        table = DataSourceHolder.parseDataSource(table);
        DataRow row = DataRow.parseArray(kvs);
        row.setPrimaryKey(row.keys());
        return dao.delete(table, row);
    }

    
    @Override 
    public <T> int deletes(String table, String key, Collection<T> values) {
        table = DataSourceHolder.parseDataSource(table);
        return dao.deletes(table, key, values);
    }

    
    @Override 
    public <T> int deletes(String table, String key, T... values) {
        table = DataSourceHolder.parseDataSource(table);
        return dao.deletes(table, key, values);
    }

    
    @Override 
    public int delete(String table, ConfigStore configs, String... conditions) {
        table = DataSourceHolder.parseDataSource(table);
        return dao.delete(table, configs, conditions);
    }

    
    @Override 
    public int truncate(String table) {
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
                int total = PageLazyStore.getTotal(lazyKey, navi.getLazyPeriod());
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
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
            log.error("QUERY ERROR:" + e);
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
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
            log.error("QUERY ERROR:" + e);
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
            list = dao.querys(prepare, clazz, configs, conditions);
        } catch (Exception e) {
            list = new EntitySet<>();
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
            log.error("QUERY ERROR:" + e);
            if (ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw e;
            }
        }
        return list;
    }

    protected <T> EntitySet<T> queryFromDao(Class<T> clazz, ConfigStore configs, String... conditions) {
        EntitySet<T> list = null;
        if (ConfigTable.isSQLDebug()) {
            log.debug("[解析SQL][src:{}]", clazz);
        }
        try {
            setPageLazy(clazz.getName(), configs, conditions);
            list = dao.querys(clazz, configs, conditions);
        } catch (Exception e) {
            list = new EntitySet<>();
            if (log.isWarnEnabled()) {
                e.printStackTrace();
            }
            log.error("QUERY ERROR:" + e);
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

    protected synchronized RunPrepare createRunPrepare(String src) {
        RunPrepare prepare = null;
        src = src.trim();
        List<String> pks = new ArrayList<>();
        // 文本sql
        if (src.startsWith("${") && src.endsWith("}")) {
            if (ConfigTable.isSQLDebug()) {
                log.debug("[解析SQL类型] [类型:{JAVA定义}] [src:{}]", src);
            }
            src = src.substring(2, src.length() - 1);
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            prepare = new DefaultTextPrepare(src);
        } else {
            src = DataSourceHolder.parseDataSource(src);//解析数据源
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
                        if (EntityAdapterProxy.hasAdapter()) {
                            key = EntityAdapterProxy.column(entity.getClass(), field).getName();
                        }
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

    
    @Override 
    public List<String> tables(boolean greedy, String catalog, String schema, String name, String types) {
        LinkedHashMap<String, Table> tables = metadata.tables(greedy, catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (Table table : tables.values()) {
            list.add(table.getName());
        }
        return list;
    }

    @Override 
    public List<String> tables(boolean greedy, String schema, String name, String types) {
        return tables(greedy, null, schema, name, types);
    }

    @Override 
    public List<String> tables(boolean greedy, String name, String types) {
        return tables(greedy, null, null, name, types);
    }

    @Override 
    public List<String> tables(boolean greedy, String types) {
        return tables(greedy, null, null, null, types);
    }

    @Override 
    public List<String> tables(boolean greedy) {
        return tables(greedy, null);
    }

    
    @Override 
    public List<String> tables(String catalog, String schema, String name, String types) {
        return tables(false, catalog, schema, name, types);
    }

    @Override 
    public List<String> tables(String schema, String name, String types) {
        return tables(false, null, schema, name, types);
    }

    @Override 
    public List<String> tables(String name, String types) {
        return tables(false, null, null, name, types);
    }

    @Override 
    public List<String> tables(String types) {
        return tables(false, null, null, null, types);
    }

    @Override 
    public List<String> tables() {
        return tables(false, null);
    }


    
    @Override 
    public List<String> views(boolean greedy, String catalog, String schema, String name, String types) {
        LinkedHashMap<String, View> views = metadata.views(greedy, catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (View view : views.values()) {
            list.add(view.getName());
        }
        return list;
    }

    @Override 
    public List<String> views(boolean greedy, String schema, String name, String types) {
        return views(greedy, null, schema, name, types);
    }

    @Override 
    public List<String> views(boolean greedy, String name, String types) {
        return views(greedy, null, null, name, types);
    }

    @Override 
    public List<String> views(boolean greedy, String types) {
        return views(greedy, null, null, null, types);
    }

    @Override 
    public List<String> views(boolean greedy) {
        return views(greedy, null);
    }

    
    @Override 
    public List<String> views(String catalog, String schema, String name, String types) {
        return views(false, catalog, schema, name, types);
    }

    @Override 
    public List<String> views(String schema, String name, String types) {
        return views(false, null, schema, name, types);
    }

    @Override 
    public List<String> views(String name, String types) {
        return views(false, null, null, name, types);
    }

    @Override 
    public List<String> views(String types) {
        return views(false, null, null, null, types);
    }

    @Override 
    public List<String> views() {
        return views(false, null);
    }

    
    @Override 
    public List<String> mtables(boolean greedy, String catalog, String schema, String name, String types) {
        LinkedHashMap<String, MasterTable> tables = metadata.mtables(greedy, catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for (MasterTable table : tables.values()) {
            list.add(table.getName());
        }
        return list;
    }

    
    @Override 
    public List<String> mtables(boolean greedy, String schema, String name, String types) {
        return mtables(greedy, null, schema, name, types);
    }

    
    @Override 
    public List<String> mtables(boolean greedy, String name, String types) {
        return mtables(greedy, null, null, name, types);
    }

    
    @Override 
    public List<String> mtables(boolean greedy, String types) {
        return mtables(greedy, null, null, null, types);
    }

    
    @Override 
    public List<String> mtables(boolean greedy) {
        return mtables(greedy, "STABLE");
    }


    
    @Override 
    public List<String> mtables(String catalog, String schema, String name, String types) {
        return mtables(false, catalog, schema, name, types);
    }

    
    @Override 
    public List<String> mtables(String schema, String name, String types) {
        return mtables(false, null, schema, name, types);
    }

    
    @Override 
    public List<String> mtables(String name, String types) {
        return mtables(false, null, null, name, types);
    }

    
    @Override 
    public List<String> mtables(String types) {
        return mtables(false, null, null, null, types);
    }

    
    @Override 
    public List<String> mtables() {
        return mtables(false, "STABLE");
    }

    
    @Override 
    public List<String> columns(boolean greedy, Table table) {
        return columns(greedy, table.getCatalog(), table.getSchema(), table.getName());
    }

    @Override 
    public List<String> columns(boolean greedy, String table) {
        return columns(greedy, null, null, table);
    }

    @Override 
    public List<String> columns(boolean greedy, String catalog, String schema, String table) {
        LinkedHashMap<String, Column> columns = metadata.columns(greedy, catalog, schema, table);
        List<String> list = new ArrayList<>();
        for (Column column : columns.values()) {
            list.add(column.getName());
        }
        return list;
    }

    
    @Override 
    public List<String> columns(Table table) {
        return columns(false, table);
    }

    @Override 
    public List<String> columns(String table) {
        return columns(false, null, null, table);
    }

    @Override 
    public List<String> columns(String catalog, String schema, String table) {
        return columns(false, catalog, schema, table);
    }

    
    @Override 
    public List<String> tags(boolean greedy, Table table) {
        return tags(greedy, table.getCatalog(), table.getSchema(), table.getName());
    }

    @Override 
    public List<String> tags(boolean greedy, String table) {
        return tags(greedy, null, null, table);
    }

    @Override 
    public List<String> tags(boolean greedy, String catalog, String schema, String table) {
        LinkedHashMap<String, Tag> tags = metadata.tags(greedy, catalog, schema, table);
        List<String> list = new ArrayList<>();
        for (Tag tag : tags.values()) {
            list.add(tag.getName());
        }
        return list;
    }

    
    @Override 
    public List<String> tags(Table table) {
        return tags(false, table.getCatalog(), table.getSchema(), table.getName());
    }

    @Override 
    public List<String> tags(String table) {
        return tags(false, null, null, table);
    }

    @Override 
    public List<String> tags(String catalog, String schema, String table) {
        return tags(false, catalog, schema, table);
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
        public LinkedHashMap<String, Database> databases() {
            return dao.databases();
        }

        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(Table table)
         * LinkedHashMap<String,Table> tables(String catalog, String schema, String name, String types)
         * LinkedHashMap<String,Table> tables(String schema, String name, String types)
         * LinkedHashMap<String,Table> tables(String name, String types)
         * LinkedHashMap<String,Table> tables(String types)
         * LinkedHashMap<String,Table> tables()
         * Table table(String catalog, String schema, String name)
         * Table table(String schema, String name)
         * Table table(String name)
         ******************************************************************************************************************/
        
        @Override
        public boolean exists(boolean greedy, Table table) {
            if (null != table(greedy, table.getCatalog(), table.getSchema(), table.getName())) {
                return true;
            }
            return false;
        }


        @Override
        public boolean exists(Table table) {
                return exists(false, table);
            }


        @Override
        public <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String schema, String name, String types) {
            return tables(greedy, null, schema, name, types);
        }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(boolean greedy, String name, String types) {
            return tables(greedy, null, null, name, types);
        }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(boolean greedy, String types) {
            return tables(greedy, null, types);
        }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(boolean greedy) {
                return tables(greedy, null);
            }



        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(boolean greedy, String catalog, String schema, String name, String types) {
            return dao.tables(greedy, catalog, schema, name, types);
        }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(String schema, String name, String types) {
            return tables(false, null, schema, name, types);
        }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(String name, String types) {
            return tables(false, null, null, name, types);
        }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(String types) {
                return tables(false, null, types);
            }


        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables() {
                return tables(false, null);
            }



        @Override
        public <T extends Table>  LinkedHashMap<String, T> tables(String catalog, String schema, String name, String types) {
            return dao.tables(false, catalog, schema, name, types);
        }


        @Override
        public Table table(boolean greedy, String catalog, String schema, String name, boolean struct) {
            Table table = null;
            LinkedHashMap<String, Table> tables = tables(greedy, catalog, schema, name, null);
            if (tables.size() > 0) {
                table = tables.values().iterator().next();
                if(struct) {
                    LinkedHashMap<String, Column> columns = columns(table);
                    table.setColumns(columns);
                    table.setTags(tags(table));
                    PrimaryKey pk = primary(table);
                    if(null != pk){
                        for(String col:pk.getColumns().keySet()){
                            Column column = columns.get(col.toUpperCase());
                            if(null != column){
                                column.setPrimaryKey(true);
                            }
                        }
                    }
                    table.setPrimaryKey(pk);
                    table.setIndexs(indexs(table));
                }
            }
            return table;
        }


        @Override
        public Table table(boolean greedy, String catalog, String schema, String name) {
            return table(greedy, catalog, schema, name, true);
        }

        @Override
        public Table table(boolean greedy, String schema, String name, boolean struct) {
            return table(greedy, null, schema, name, struct);
        }


        @Override
        public Table table(boolean greedy, String name, boolean struct) {
            return table(greedy, null, null, name, struct);
        }



        @Override
        public Table table(String catalog, String schema, String name, boolean struct) {
            return table(false, catalog, schema, name, struct);
        }


        @Override
        public Table table(String schema, String name, boolean struct) {
            return table(false, null, schema, name, struct);
        }


        @Override
        public Table table(String name, boolean struct) {
                return table(false, null, null, name, struct);
            }



        @Override
        public Table table(boolean greedy, String schema, String name) {
            return table(greedy, null, schema, name, true);
        }


        @Override
        public Table table(boolean greedy, String name) {
                return table(greedy, null, null, name, false);
            }



        @Override
        public Table table(String catalog, String schema, String name) {
            return table(false, catalog, schema, name, true);
        }


        @Override
        public Table table(String schema, String name) {
                return table(false, null, schema, name, true);
            }


        @Override
        public Table table(String name) {
            return table(false, null, null, name, true);
        }

        /* *****************************************************************************************************************
         * 													view
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(View view)
         * LinkedHashMap<String,View> views(String catalog, String schema, String name, String types)
         * LinkedHashMap<String,View> views(String schema, String name, String types)
         * LinkedHashMap<String,View> views(String name, String types)
         * LinkedHashMap<String,View> views(String types)
         * LinkedHashMap<String,View> views()
         * View view(String catalog, String schema, String name)
         * View view(String schema, String name)
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
        public boolean exists(View view) {
                return exists(false, view);
            }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(boolean greedy, String schema, String name, String types) {
            return views(greedy, null, schema, name, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(boolean greedy, String name, String types) {
            return views(greedy, null, null, name, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(boolean greedy, String types) {
            return views(greedy, null, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(boolean greedy) {
                return views(greedy, null);
            }



        @Override
        public <T extends View> LinkedHashMap<String, T> views(boolean greedy, String catalog, String schema, String name, String types) {
            return dao.views(greedy, catalog, schema, name, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(String schema, String name, String types) {
            return views(false, null, schema, name, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(String name, String types) {
            return views(false, null, null, name, types);
        }


        @Override
        public <T extends View> LinkedHashMap<String, T> views(String types) {
                return views(false, null, types);
            }


        @Override
        public <T extends View> LinkedHashMap<String, T> views() {
                return views(false, null);
            }



        @Override
        public <T extends View> LinkedHashMap<String, T> views(String catalog, String schema, String name, String types) {
            return dao.views(false, catalog, schema, name, types);
        }


        @Override
        public View view(boolean greedy, String catalog, String schema, String name) {
            View view = null;
            LinkedHashMap<String, View> views = views(greedy, catalog, schema, name, null);
            if (views.size() > 0) {
                view = views.values().iterator().next();
                view.setColumns(columns(view));
                //view.setTags(tags(view));
                //view.setIndexs(indexs(view));
            }
            return view;
        }


        @Override
        public View view(boolean greedy, String schema, String name) {
                return view(greedy, null, schema, name);
            }


        @Override
        public View view(boolean greedy, String name) {
                return view(greedy, null, null, name);
            }



        @Override
        public View view(String catalog, String schema, String name) {
                return view(false, catalog, schema, name);
            }


        @Override
        public View view(String schema, String name) {
                return view(false, null, schema, name);
            }


        @Override
        public View view(String name) {
            return view(false, null, null, name);
        }
        /* *****************************************************************************************************************
         * 													master table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(MasterTable table)
         * LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types)
         * LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types)
         * LinkedHashMap<String, MasterTable> mtables(String name, String types)
         * LinkedHashMap<String, MasterTable> mtables(String types)
         * LinkedHashMap<String, MasterTable> mtables()
         * MasterTable mtable(String catalog, String schema, String name)
         * MasterTable mtable(String schema, String name)
         * MasterTable mtable(String name)
         ******************************************************************************************************************/


        @Override
        public boolean exists(boolean greedy, MasterTable table) {
                return false;
            }


        @Override
        public boolean exists(MasterTable table) {
                return false;
            }



        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String catalog, String schema, String name, String types) {
            return dao.mtables(greedy, catalog, schema, name, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String schema, String name, String types) {
            return mtables(greedy, null, schema, name, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String name, String types) {
            return mtables(greedy, null, null, name, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String types) {
            return mtables(greedy, null, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy) {
            return mtables(greedy, "STABLE");
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(String catalog, String schema, String name, String types) {
            return dao.mtables(false, catalog, schema, name, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(String schema, String name, String types) {
            return mtables(false, null, schema, name, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(String name, String types) {
            return mtables(false, null, null, name, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables(String types) {
            return mtables(false, null, types);
        }


        @Override
        public <T extends MasterTable> LinkedHashMap<String, T> mtables() {
                return mtables(false, "STABLE");
            }


        @Override
        public MasterTable mtable(boolean greedy, String catalog, String schema, String name) {
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
        public MasterTable mtable(boolean greedy, String schema, String name) {
            return mtable(greedy, null, schema, name);
        }


        @Override
        public MasterTable mtable(boolean greedy, String name) {
                return mtable(greedy, null, null, name);
            }


        @Override
        public MasterTable mtable(String catalog, String schema, String name) {
            return mtable(false, catalog, schema, name);
        }


        @Override
        public MasterTable mtable(String schema, String name) {
                return mtable(false, null, schema, name);
            }


        @Override
        public MasterTable mtable(String name) {
            return mtable(false, null, null, name);
        }


        /* *****************************************************************************************************************
         * 													partition  table
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(PartitionTable table)
         * LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types)
         * LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types)
         * LinkedHashMap<String, PartitionTable> ptables(String name, String types)
         * LinkedHashMap<String, PartitionTable> ptables(String types)
         * LinkedHashMap<String, PartitionTable> ptables()
         * LinkedHashMap<String, PartitionTable> ptables(MasterTable master)
         * PartitionTable ptable(String catalog, String schema, String name)
         * PartitionTable ptable(String schema, String name)
         * PartitionTable ptable(String name)
         ******************************************************************************************************************/

        
        @Override
        public boolean exists(boolean greedy, PartitionTable table) {
                return false;
            }


        @Override
        public boolean exists(PartitionTable table) {
                return false;
            }



        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String catalog, String schema, String master, String name) {
            return dao.ptables(greedy, catalog, schema, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String schema, String master, String name) {
            return ptables(greedy, null, schema, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master, String name) {
            return ptables(greedy, null, null, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master) {
            return ptables(greedy, null, null, master, null);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master) {
            return dao.ptables(greedy, master);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String, Object> tags) {
            return dao.ptables(greedy, master, tags);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
            return dao.ptables(greedy, master, tags, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String catalog, String schema, String master, String name) {
            return dao.ptables(false, catalog, schema, master, name);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String schema, String master, String name) {
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
            return dao.ptables(false, master, tags);
        }


        @Override
        public <T extends PartitionTable> LinkedHashMap<String, T> ptables(MasterTable master, Map<String, Object> tags, String name) {
            return dao.ptables(false, master, tags, name);
        }


        @Override
        public PartitionTable ptable(boolean greedy, String catalog, String schema, String master, String name) {
            LinkedHashMap<String, PartitionTable> tables = ptables(greedy, catalog, schema, master, name);
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
        public PartitionTable ptable(boolean greedy, String schema, String master, String name) {
            return ptable(greedy, null, schema, master, name);
        }


        @Override
        public PartitionTable ptable(boolean greedy, String master, String name) {
            return ptable(greedy, null, null, master, name);
        }


        @Override
        public PartitionTable ptable(String catalog, String schema, String master, String name) {
            return ptable(false, catalog, schema, master, name);
        }


        @Override
        public PartitionTable ptable(String schema, String master, String name) {
            return ptable(false, null, schema, master, name);
        }


        @Override
        public PartitionTable ptable(String master, String name) {
            return ptable(false, null, null, master, name);
        }

        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
         * boolean exists(Column column);
         * boolean exists(Table table, String name);
         * boolean exists(String table, String name);
         * boolean exists(String catalog, String schema, String table, String name);
         * LinkedHashMap<String,Column> columns(Table table)
         * LinkedHashMap<String,Column> columns(String table)
         * LinkedHashMap<String,Column> columns(String catalog, String schema, String table)
         * LinkedHashMap<String,Column> column(Table table, String name);
         * LinkedHashMap<String,Column> column(String table, String name);
         * LinkedHashMap<String,Column> column(String catalog, String schema, String table, String name);
         ******************************************************************************************************************/

        @Override
        public boolean exists(boolean greedy, Column column) {
            try {
                Table table = table(greedy, column.getCatalog(), column.getSchema(), column.getTableName(true));
                if (null != table) {
                    if (table.getColumns().containsKey(column.getName().toUpperCase())) {
                        return true;
                    }
                }
            } catch (Exception e) {

            }
            return false;
        }


        @Override
        public boolean exists(boolean greedy, Table table, String column) {
            try {
                LinkedHashMap<String, Column> columns = table.getColumns();
                if (null == columns && columns.isEmpty()) {
                    columns = columns(greedy, table);
                }
                if (columns.containsKey(column.toUpperCase())) {
                    return true;
                }
            } catch (Exception e) {

            }
            return false;
        }


        @Override
        public boolean exists(boolean greedy, String table, String column) {
            try {
                LinkedHashMap<String, Column> columns = columns(greedy, table);
                if (columns.containsKey(column.toUpperCase())) {
                    return true;
                }
            } catch (Exception e) {

            }
            return false;
        }

        @Override
        public boolean exists(boolean greedy, String catalog, String schema, String table, String column) {
            try {
                LinkedHashMap<String, Column> columns = columns(greedy, catalog, schema, table);
                if (columns.containsKey(column.toUpperCase())) {
                    return true;
                }
            } catch (Exception e) {

            }
            return false;
        }


        @Override
        public boolean exists(Column column) {
                return exists(false, column);
            }


        @Override
        public boolean exists(Table table, String column) {
                return exists(false, table, column);
            }


        @Override
        public boolean exists(String table, String column) {
                return exists(false, table, column);
            }

        @Override
        public boolean exists(String catalog, String schema, String table, String column) {
            return exists(false, catalog, schema, table, column);
        }



        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String table) {
            return columns(greedy, null, null, table);
        }


        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table) {
            LinkedHashMap<String, T> columns = dao.columns(greedy, table);
            return columns;
        }


        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, String catalog, String schema, String table) {
            return columns(greedy, new Table(catalog, schema, table));
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

        @Override
        public Column column(boolean greedy, String table, String name) {
            Column column = null;
            LinkedHashMap<String, Column> columns = columns(greedy, table);
            column = columns.get(name.toUpperCase());
            return column;

        }

        @Override
        public Column column(boolean greedy, String catalog, String schema, String table, String name) {
            Column column = null;
            LinkedHashMap<String, Column> columns = columns(greedy, catalog, schema, table);
            column = columns.get(name.toUpperCase());
            return column;
        }



        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(String table) {
            return columns(false, null, null, table);
        }


        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(Table table) {
                return columns(false, table);
            }


        @Override
        public <T extends Column> LinkedHashMap<String, T> columns(String catalog, String schema, String table) {
            return columns(false, new Table(catalog, schema, table));
        }

        @Override
        public Column column(Table table, String name) {
                return column(false, table, name);
            }

        @Override
        public Column column(String table, String name) {
            return column(false, table, name);
        }

        @Override
        public Column column(String catalog, String schema, String table, String name) {
            return column(false, catalog, schema, table, name);
        }
        /* *****************************************************************************************************************
         * 													tag
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String,Tag> tags(String catalog, String schema, String table)
         * LinkedHashMap<String,Tag> tags(String table)
         * LinkedHashMap<String,Tag> tags(Table table)
         ******************************************************************************************************************/


        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String catalog, String schema, String table) {
            return tags(greedy, new Table(catalog, schema, table));
        }


        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table) {
            return tags(greedy, null, null, table);
        }


        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table) {
            return dao.tags(greedy, table);
        }


        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(String catalog, String schema, String table) {
            return tags(false, new Table(catalog, schema, table));
        }


        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(String table) {
                return tags(false, null, null, table);
            }


        @Override
        public <T extends Tag> LinkedHashMap<String, T> tags(Table table) {
            return tags(false, table);
        }

        /* *****************************************************************************************************************
         * 													primary
         * -----------------------------------------------------------------------------------------------------------------
         * PrimaryKey primary(Table table)
         * PrimaryKey primary(String table)
         * PrimaryKey primary(String catalog, String schema, String table)
         ******************************************************************************************************************/

        @Override
        public PrimaryKey primary(boolean greedy, Table table) {
                return dao.primary(greedy, table);
            }


        @Override
        public PrimaryKey primary(boolean greedy, String table) {
                return primary(greedy, new Table(table));
            }


        @Override
        public PrimaryKey primary(boolean greedy, String catalog, String schema, String table) {
            return primary(greedy, new Table(catalog, schema, table));
        }


        @Override
        public PrimaryKey primary(Table table) {
                return dao.primary(false, table);
            }


        @Override
        public PrimaryKey primary(String table) {
                return primary(false, new Table(table));
            }


        @Override
        public PrimaryKey primary(String catalog, String schema, String table) {
            return primary(false, new Table(catalog, schema, table));
        }

        /* *****************************************************************************************************************
         * 													foreign
         ******************************************************************************************************************/

        
        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table) {
            return dao.foreigns(greedy, table);
        }


        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, String table) {
            return foreigns(greedy, new Table(table));
        }


        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, String catalog, String schema, String table) {
            return foreigns(greedy, new Table(catalog, schema, table));
        }


        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(Table table) {
                return foreigns(false, table);
            }


        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(String table) {
            return foreigns(false, new Table(table));
        }


        @Override
        public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(String catalog, String schema, String table) {
            return foreigns(false, new Table(catalog, schema, table));
        }


        @Override
        public ForeignKey foreign(boolean greedy, Table table, List<String> columns) {
            if(null == columns || columns.size() ==0){
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


        @Override
        public ForeignKey foreign(boolean greedy, Table table, String ... columns) {
            return foreign(greedy, table, BeanUtil.array2list(columns));
        }

        @Override
        public ForeignKey foreign(boolean greedy, String table, List<String> columns) {
            return foreign(greedy, new Table(table), columns);
        }

        @Override
        public ForeignKey foreign(boolean greedy, String table, String... columns) {
            return foreign(greedy, new Table(table), BeanUtil.array2list(columns));
        }

           /*
        @Override
        public ForeignKey foreign(boolean greedy, String catalog, String schema, String table, String... columns) {
                return foreign(greedy, new Table(catalog, schema, table), BeanUtil.array2list(columns));
            }*/

        @Override
        public ForeignKey foreign(boolean greedy, String catalog, String schema, String table, List<String> columns) {
            return foreign(greedy, new Table(catalog, schema, table), columns);
        }


        @Override
        public ForeignKey foreign(Table table, List<String> columns) {
                return foreign(false, table, columns);
            }

        @Override
        public ForeignKey foreign(Table table, String... columns) {
            return foreign(false, table, BeanUtil.array2list(columns));
        }


        @Override
        public ForeignKey foreign(String table, List<String> columns) {
            return foreign(false, new Table(table), columns);
        }


        @Override
        public ForeignKey foreign(String table, String... columns) {
            return foreign(false, new Table(table), BeanUtil.array2list(columns));
        }

           /*
        @Override
        public ForeignKey foreign(String catalog, String schema, String table, String... columns) {
                return foreign(false, new Table(catalog, schema, table), BeanUtil.array2list(columns));
            }*/

        @Override
        public ForeignKey foreign(String catalog, String schema, String table, List<String> columns) {
            return foreign(false, new Table(catalog, schema, table), columns);
        }

        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String, Index> indexs(Table table)
         * LinkedHashMap<String, Index> indexs(String table)
         * LinkedHashMap<String, Index> indexs(String catalog, String schema, String table)
         * Index index(Table table, String name);
         * Index index(String table, String name);
         * Index index(String name);
         ******************************************************************************************************************/

        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table) {
            return dao.indexs(greedy, table);
        }


        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table) {
            return indexs(greedy, new Table(table));
        }


        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String catalog, String schema, String table) {
            return indexs(greedy, new Table(catalog, schema, table));
        }


        @Override
        public Index index(boolean greedy, Table table, String name) {
            Index index = null;
            LinkedHashMap<String, Index> all = dao.indexs(greedy, table, name);
            if (null != all && null != name) {
                index = all.get(name.toUpperCase());
            }
            if (null == index) {
                //根据名称查询没有实现的先查所有
                all = dao.indexs(greedy, table);
                if (null != all && null != name) {
                    index = all.get(name.toUpperCase());
                }
            }
            return index;
        }


        @Override
        public Index index(boolean greedy, String table, String name) {
                return index(greedy, new Table(table), name);
            }


        @Override
        public Index index(boolean greedy, String name) {
                return index(greedy, (Table) null, name);
            }


        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(Table table) {
                return indexs(false, table);
            }


        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(String table) {
                return indexs(false, table);
            }


        @Override
        public <T extends Index> LinkedHashMap<String, T> indexs(String catalog, String schema, String table) {
            return indexs(false, catalog, schema, table);
        }


        @Override
        public Index index(Table table, String name) {
                return index(false, table, name);
            }

        @Override
        public Index index(String table, String name) {
                return index(false, table, name);
            }


        @Override
        public Index index(String name) {
            return index(false, name);
        }

        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
         * LinkedHashMap<String,Constraint> constraints(Table table)
         * LinkedHashMap<String,Constraint> constraints(String table)
         * LinkedHashMap<String,Constraint> constraints(String catalog, String schema, String table)
         ******************************************************************************************************************/

        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, Table table) {
            return dao.constraints(greedy, table);
        }


        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String table) {
            return constraints(greedy, new Table(table));
        }


        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(boolean greedy, String catalog, String schema, String table) {
            return constraints(greedy, new Table(catalog, schema, table));
        }


        @Override
        public Constraint constraint(boolean greedy, Table table, String name) {
            LinkedHashMap<String, Constraint> constraints = constraints(greedy, table);
            if (null != constraints && null != name) {
                return constraints.get(name.toUpperCase());
            }
            return null;
        }


        @Override
        public Constraint constraint(boolean greedy, String table, String name) {
            return constraint(new Table(table), name);
        }


        @Override
        public Constraint constraint(boolean greedy, String name) {
                return constraint((Table) null, name);
            }


        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(Table table) {
            return constraints(false, table);
        }


        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(String table) {
            return constraints(false, new Table(table));
        }


        @Override
        public <T extends Constraint> LinkedHashMap<String, T> constraints(String catalog, String schema, String table) {
            return constraints(false, new Table(catalog, schema, table));
        }


        @Override
        public Constraint constraint(Table table, String name) {
                return constraint(false, table, name);
            }


        @Override
        public Constraint constraint(String table, String name) {
                return constraint(false, table, name);
            }


        @Override
        public Constraint constraint(String name) {
                return constraint(false, name);
            }



        /* *****************************************************************************************************************
         * 													trigger
         ******************************************************************************************************************/


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return dao.triggers(greedy, table, events);
        }

        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String catalog, String schema, String table, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(greedy, new Table(catalog, schema, table), events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String schema, String table, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(greedy, null, schema, table, events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, String table, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(greedy, null, null, table, events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(greedy, null, null, null, events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy) {
            return triggers(greedy, (List)null);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(String catalog, String schema, String name, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(false, catalog, schema, name, events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(String schema, String name, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(false, null, schema, name, events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(String name, List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(false, null, null, name, events);
        }


        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers(List<org.anyline.entity.data.Trigger.EVENT> events) {
            return triggers(false, null, null, null, events);
        }

        @Override
        public <T extends Trigger> LinkedHashMap<String, T> triggers() {
                return triggers(false, (List)null);
            }



        @Override
        public Trigger trigger(boolean greedy, String catalog, String schema, String name) {
            LinkedHashMap<String, Trigger> triggers = triggers(greedy, catalog, schema, null);
            if(null != triggers){
                return triggers.get(name.toUpperCase());
            }
            return null;
        }


        @Override
        public Trigger trigger(boolean greedy, String schema, String name) {
            return trigger(greedy, null, schema, name);
        }


        @Override
        public Trigger trigger(boolean greedy, String name) {
            return trigger(greedy, null, null, name);
        }


        @Override
        public Trigger trigger(String catalog, String schema, String name) {
            return trigger(false, null, schema, name);
        }


        @Override
        public Trigger trigger(String schema, String name) {
                return trigger(false, null, schema, name);
            }


        @Override
        public Trigger trigger(String name) {
            return trigger(false, null, null, name);
        }

        /* *****************************************************************************************************************
         * 													procedure
         ******************************************************************************************************************/


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String catalog, String schema, String name) {
            return dao.procedures(greedy, catalog, schema, name);
        }


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String schema, String name) {
            return procedures(greedy, null, schema, name);
        }


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String name) {
            return procedures(greedy, null, null, name);
        }

        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy) {
            return procedures(greedy, null, null, null);
        }


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(String catalog, String schema, String name) {
            return procedures(false, null, schema, name);
        }


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(String schema, String name) {
            return procedures(false, null, schema, name);
        }


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures(String name) {
            return procedures(false, null, null, name);
        }


        @Override
        public <T extends Procedure> LinkedHashMap<String, T> procedures() {
            return procedures(false, null, null, null);
        }



        @Override
        public Procedure procedure(boolean greedy, String catalog, String schema, String name) {
                return null;
            }

        @Override
        public Procedure procedure(boolean greedy, String schema, String name) {
            return procedure(greedy, null, schema, name);
        }

        @Override
        public Procedure procedure(boolean greedy, String name) {
                return procedure(greedy, null, null, name);
            }

        @Override
        public Procedure procedure(String catalog, String schema, String name) {
            return procedure(false, null, schema, name);
        }

        @Override
        public Procedure procedure(String schema, String name) {
                return procedure(false, null, schema, name);
            }

        @Override
        public Procedure procedure(String name) {
            return procedure(false, null, null, name);
        }

        /* *****************************************************************************************************************
         * 													function
         ******************************************************************************************************************/


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String catalog, String schema, String name) {
            return dao.functions(greedy, catalog, schema, name);
        }


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String schema, String name) {
            return functions(greedy, null, schema, name);
        }


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String name) {
            return functions(greedy, null, null, name);
        }

        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(boolean greedy) {
            return functions(greedy, null, null, null);
        }


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(String catalog, String schema, String name) {
            return functions(false, null, schema, name);
        }


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(String schema, String name) {
            return functions(false, null, schema, name);
        }


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions(String name) {
            return functions(false, null, null, name);
        }


        @Override
        public <T extends Function> LinkedHashMap<String, T> functions() {
            return functions(false, null, null, null);
        }



        @Override
        public Function function(boolean greedy, String catalog, String schema, String name) {
            return null;
        }

        @Override
        public Function function(boolean greedy, String schema, String name) {
            return function(greedy, null, schema, name);
        }

        @Override
        public Function function(boolean greedy, String name) {
            return function(greedy, null, null, name);
        }

        @Override
        public Function function(String catalog, String schema, String name) {
            return function(false, null, schema, name);
        }

        @Override
        public Function function(String schema, String name) {
            return function(false, null, schema, name);
        }

        @Override
        public Function function(String name) {
            return function(false, null, null, name);
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
                Table update = table.getUpdate();
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
            Table update = table.getUpdate();
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
            boolean result = false;
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
            boolean result = false;
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
            boolean result = false;
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
            PartitionTable otable = metadata.ptable(table.getCatalog(), table.getSchema(), table.getName());
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
            boolean result = false;
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
                columns.put(original.getName(), original);
            }
            column.setTable(table);
            CacheProxy.clear();
            return result;
        }

        @Override
        public boolean rename(Column origin, String name) throws Exception{
            boolean result = false;
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
            boolean result = false;
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
            boolean result = false;
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
            boolean result = false;
            dao.rename(origin, name);
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
            boolean result = false;
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
            boolean result = false;
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
            boolean result = false;
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
            boolean result = false;
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
            boolean result = false;
            return result;
        }

    };
}
