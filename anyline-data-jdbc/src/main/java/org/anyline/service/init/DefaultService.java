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
import org.anyline.data.cache.CacheUtil;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.dao.AnylineDao;
import org.anyline.data.entity.*;
import org.anyline.data.entity.Column;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.init.DefaultProcedure;
import org.anyline.data.prepare.init.DefaultSQLStore;
import org.anyline.proxy.CacheProxy;
import org.anyline.service.AnylineService;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.data.prepare.Procedure;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

@Service("anyline.service")
public class DefaultService<E> implements AnylineService<E> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    @Qualifier("anyline.dao")
    protected AnylineDao dao;

    @Autowired(required = false)
    @Qualifier("anyline.cache.provider")
    protected CacheProvider cacheProvider;

    public AnylineService datasource(String datasource){
        DataSourceHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService datasource(){
        DataSourceHolder.setDefaultDataSource();
        return this;
    }
    public AnylineService setDataSource(String datasource){
        DataSourceHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService setDataSource(String datasource, boolean auto){
        DataSourceHolder.setDataSource(datasource, auto);
        return this;
    }
    public AnylineService setDefaultDataSource(){
        DataSourceHolder.setDefaultDataSource();
        return this;
    }
    // 恢复切换前数据源
    public AnylineService recoverDataSource(){
        DataSourceHolder.recoverDataSource();
        return this;
    }
    public String getDataSource(){
        return DataSourceHolder.getDataSource();
    }
    /**
     * 按条件查询
     * @param src 			数据源(表｜视图｜函数｜自定义SQL | SELECT语句)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions    固定查询条件
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
        return querys(src, configs, obj,conditions);
    }

    @Override
    public DataSet querys(String src, Object obj, String... conditions) {
        return querys(src, (ConfigStore)null, obj, conditions);
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
        return querys(src,(Object)null, conditions);
    }

    @Override
    public DataSet querys(String src, int fr, int to, String... conditions) {
        return querys(src,fr, to,null, conditions);
    }

    @Override
    public List<String> column2param(String table){
        List<String> columns = columns(table);
        return AdapterProxy.column2param(columns);
    }
    @Override
    public List<Map<String,Object>> maps(String src, ConfigStore configs, Object obj, String... conditions) {
        List<Map<String,Object>> maps = null;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", src);
        }
        try {
            RunPrepare prepare = createRunPrepare(src);
            configs = append(configs, obj);
            maps = dao.maps(prepare, configs, conditions);
        } catch (Exception e) {
            maps = new ArrayList<Map<String,Object>>();
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return maps;
    }

    @Override
    public List<Map<String,Object>> maps(String src, Object obj, String... conditions) {
        return maps(src, null, obj, conditions);
    }
    @Override
    public List<Map<String,Object>> maps(String src, int fr, int to, Object obj, String... conditions) {
        return maps(src, new DefaultConfigStore(fr, to), obj, conditions);
    }

    @Override
    public List<Map<String,Object>> maps(String src, ConfigStore configs, String... conditions) {
       return maps(src, configs, null, conditions);
    }

    @Override
    public List<Map<String,Object>> maps(String src, String... conditions) {
        return maps(src, null, conditions);
    }
    @Override
    public List<Map<String,Object>> maps(String src, int fr, int to, String... conditions) {
        return maps(src,fr, to, null, conditions);
    }

    @Override
    public DataSet caches(String cache, String src, ConfigStore configs, Object obj, String ... conditions){
        DataSet set = null;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        if(null == cache || "true".equalsIgnoreCase(ConfigTable.getString("CACHE_DISABLED"))){
            set = querys(src, append(configs, obj), conditions);
        }else{
            if(null != cacheProvider){
                set = queryFromCache(cache, src, configs, conditions);
            }else{
                set = querys(src, configs, conditions);
            }
        }
        return set;
    }
    @Override
    public DataSet caches(String cache, String src, Object obj, String ... conditions){
        return caches(cache, src, null, obj, conditions);
    }
    @Override
    public DataSet caches(String cache, String src, int fr, int to, Object obj, String ... conditions){
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return caches(cache, src, configs, obj, conditions);
    }


    @Override
    public DataSet caches(String cache, String src, ConfigStore configs, String ... conditions){
        return caches(cache, src, configs, (Object)null, conditions);
    }
    @Override
    public DataSet caches(String cache, String src, String ... conditions){
        return caches(cache, src, null, null, conditions);
    }
    @Override
    public DataSet caches(String cache, String src, int fr, int to, String ... conditions){
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
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
            return new DataRow();
        }
        return null;
    }


    @Override
    public DataRow query(String src, Object obj, String... conditions) {
        return query(src, (ConfigStore)null, obj, conditions);
    }

    @Override
    public DataRow query(String src, ConfigStore store, String... conditions) {
        return query(src, store, null, conditions);
    }


    @Override
    public DataRow query(String src, String... conditions) {
        return query(src, (ConfigStore)null, conditions);
    }

    @Override
    public DataRow cache(String cache, String src, ConfigStore configs, Object obj, String ... conditions){
        // 是否启动缓存
        if(null == cache){
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

        if(cache.contains(":")){
            String ks[] = BeanUtil.parseKeyValue(cache);
            cache = ks[0];
            key += ks[1]+":";
        }
        key +=  CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
        if(null != cacheProvider) {
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
        if(null != row && null != cacheProvider){
            cacheProvider.put(cache, key, row);
        }
        if(null == row && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
            row = new DataRow();
        }
        return row;
    }
    @Override
    public DataRow cache(String cache, String src, Object obj, String ... conditions){
        return cache(cache, src, null, obj, conditions);
    }

    @Override
    public DataRow cache(String cache, String src, ConfigStore configs, String ... conditions){
        return cache(cache, src, configs, null, conditions);
    }
    @Override
    public DataRow cache(String cache, String src, String ... conditions){
        return cache(cache, src, null, null, conditions);
    }


    @Deprecated
    public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return selects(clazz, configs, entity, conditions);
    }
    public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, T entity, String ... conditions){
        return selects(clazz, navi, entity, conditions);
    }
    public <T> EntitySet<T> querys(Class<T> clazz, T entity, String ... conditions){
        return selects(clazz, entity, conditions);
    }
    public <T> EntitySet<T> querys(Class<T> clazz, int first, int last, T entity, String ... conditions){
        return selects(clazz, first, last, entity, conditions);
    }
    public <T> T query(Class<T> clazz, ConfigStore configs, T entity, String ... conditions){
        return select(clazz, configs, entity, conditions);
    }

    public <T> T query(Class<T> clazz, T entity, String ... conditions){
        return select(clazz, entity, conditions);
    }

    public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String ... conditions){
        return selects(clazz, configs, conditions);
    }
    public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, String ... conditions){
        return selects(clazz, navi, conditions);
    }
    public <T> EntitySet<T> querys(Class<T> clazz, String ... conditions){
        return selects(clazz, conditions);
    }
    public <T> EntitySet<T> querys(Class<T> clazz, int first, int last, String ... conditions){
        return selects(clazz, first, last, conditions);
    }
    public <T> T query(Class<T> clazz, ConfigStore configs, String ... conditions){
        return select(clazz, configs, conditions);
    }
    public <T> T query(Class<T> clazz, String ... conditions){
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
        return selects(clazz, (ConfigStore)null, entity, conditions);
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
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
            try {
                return (T) clazz.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public <T> T select(Class<T> clazz, T entity, String... conditions) {
        return select(clazz, (ConfigStore)null, entity, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(Class<T> clazz, ConfigStore configs, String... conditions) {
        return selects(clazz, configs, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(Class<T> clazz, PageNavi navi, String... conditions) {
        return selects(clazz, navi, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(Class<T> clazz, String... conditions) {
        return selects(clazz, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(Class<T> clazz, int fr, int to, String... conditions) {
        return selects(clazz, fr, to, (T)null, conditions);
    }

    @Override
    public <T> T select(Class<T> clazz, ConfigStore configs, String... conditions) {
        return select(clazz, configs, (T)null, conditions);
    }

    @Override
    public <T> T select(Class<T> clazz, String... conditions) {
        return select(clazz, (T)null, conditions);
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
        return selects(src, clazz, (ConfigStore)null, entity, conditions);
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
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
            try {
                return (T) clazz.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public <T> T select(String src, Class<T> clazz, T entity, String... conditions) {
        return select(src, clazz, (ConfigStore)null, entity, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(String src, Class<T> clazz, ConfigStore configs, String... conditions) {
        return selects(src, clazz, configs, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(String src, Class<T> clazz, PageNavi navi, String... conditions) {
        return selects(src, clazz, navi, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(String src, Class<T> clazz, String... conditions) {
        return selects(src, clazz, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> selects(String src, Class<T> clazz, int fr, int to, String... conditions) {
        return selects(src, clazz, fr, to, (T)null, conditions);
    }

    @Override
    public <T> T select(String src, Class<T> clazz, ConfigStore configs, String... conditions) {
        return select(src, clazz, configs, (T)null, conditions);
    }

    @Override
    public <T> T select(String src, Class<T> clazz, String... conditions) {
        return select(src, clazz, (T)null, conditions);
    }
    /**
     * 解析泛型class
     * @return class
     */
    protected Class<E> parseGenericClass(){
        Type type = null;
        Class<E> clazz = null;
        Type superClass = getClass().getGenericSuperclass();
        if(superClass instanceof ParameterizedType) {
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
     * @param prepare       表｜视图｜函数｜自定义SQL |RunPrepare
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions    固定查询条件
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
        ConfigStore configs = new DefaultConfigStore(fr,to);
        return querys(prepare, configs, obj, conditions);
    }
    @Override
    public DataSet querys(RunPrepare prepare, int fr, int to,  String... conditions) {
        return querys(prepare, fr, to, null, conditions);
    }
    @Override
    public DataSet caches(String cache, RunPrepare table, ConfigStore configs, Object obj, String ... conditions){
        DataSet set = null;
        conditions = BasicUtil.compress(conditions);
        if(null == cache){
            set = querys(table, configs, obj, conditions);
        }else{
            if(null != cacheProvider){
               // set = queryFromCache(cache, table, configs, conditions);
            }else{
                set = querys(table, configs, obj, conditions);
            }
        }
        return set;
    }
    @Override
    public DataSet caches(String cache, RunPrepare table, ConfigStore configs, String ... conditions){
        return caches(cache, table, configs, null, conditions);
    }
    @Override
    public DataSet caches(String cache, RunPrepare table, Object obj, String ... conditions){
        return caches(cache, table, null, obj, conditions);
    }
    @Override
    public DataSet caches(String cache, RunPrepare table, String ... conditions){
        return caches(cache, table, null, null, conditions);
    }
    @Override
    public DataSet caches(String cache, RunPrepare table, int fr, int to, Object obj, String ... conditions){
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return caches(cache, table, configs, obj, conditions);
    }
    @Override
    public DataSet caches(String cache, RunPrepare table, int fr, int to, String ... conditions){
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
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
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
    public DataRow cache(String cache, RunPrepare table, ConfigStore configs, Object obj, String ... conditions){
        // 是否启动缓存
        if(null == cache){
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

        if(cache.contains(":")){
            String ks[] = BeanUtil.parseKeyValue(cache);
            cache = ks[0];
            key += ks[1]+":";
        }
        key +=  CacheUtil.createCacheElementKey(true, true, table.getTable(), configs, conditions);
        if(null != cacheProvider) {
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
        if(null != row && null != cacheProvider){
            cacheProvider.put(cache, key, row);
        }
        if(null == row && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
            row = new DataRow();
        }
        return row;
    }

    @Override
    public DataRow cache(String cache, RunPrepare table, ConfigStore configs, String ... conditions){
        return cache(cache, table, configs, null, conditions);
    }
    @Override
    public DataRow cache(String cache, RunPrepare table, Object obj, String ... conditions){
        return cache(cache, table, null, obj, conditions);
    }
    @Override
    public DataRow cache(String cache, RunPrepare table,  String ... conditions){
        return cache(cache, table, null, null, conditions);
    }

    /**
     * 删除缓存 参数保持与查询参数完全一致
     * @param channel  channel
     * @param src  src
     * @param configs  configs
     * @param conditions  conditions
     * @return boolean
     */
    @Override
    public boolean removeCache(String channel, String src, ConfigStore configs, String ... conditions){
        if(null != cacheProvider) {
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
    public boolean removeCache(String channel, String src, String ... conditions){
        return removeCache(channel, src, null, conditions);
    }
    @Override
    public boolean removeCache(String channel, String src, int fr, int to, String ... conditions){
        ConfigStore configs = new DefaultConfigStore(fr, to);
        return removeCache(channel, src, configs, conditions);
    }
    /**
     * 清空缓存
     * @param channel  channel
     * @return boolean
     */
    @Override
    public boolean clearCache(String channel){
        if(null != cacheProvider) {
            return cacheProvider.clear(channel);
        }else{
            return false;
        }
    }


    /**
     * 检查唯一性
     * @param src  src
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return boolean
     */

    @Override
    public boolean exists(String src, ConfigStore configs, Object obj, String ... conditions){
        boolean result = false;
        src = BasicUtil.compress(src);
        conditions = BasicUtil.compress(conditions);
        RunPrepare prepare = createRunPrepare(src);
        result = dao.exists(prepare, append(configs, obj), conditions);
        return result;
    }
    @Override
    public boolean exists(String src, ConfigStore configs, String ... conditions){
        return exists(src, configs, null, conditions);
    }
    @Override
    public boolean exists(String src, Object obj, String ... conditions){
        return exists(src, null, obj, conditions);
    }
    @Override
    public boolean exists(String src, String ... conditions){
        return exists(src, null, null, conditions);
    }
    /**
     * 只根据主键判断
     */
    @Override
    public boolean exists(String src, DataRow row){
        if(null != row){
            List<String> keys = row.getPrimaryKeys();
            if(null != keys){
                String[] conditions = new String[keys.size()];
                int idx = 0;
                for(String key: keys){
                    conditions[idx++] = key + ":" + row.getString(key);
                }
                return exists(src, null, conditions);
            }
            return false;
        }else{
            return false;
        }
    }
    @Override
    public boolean exists(DataRow row){
        return exists(null, row);
    }

    @Override
    public int count(String src, ConfigStore configs, Object obj, String ... conditions){
        int count = -1;
        try {
            // conditions = parseConditions(conditions);
            src = BasicUtil.compress(src);
            conditions = BasicUtil.compress(conditions);
            RunPrepare prepare = createRunPrepare(src);
            count = dao.count(prepare, append(configs, obj), conditions);
        } catch (Exception e) {
            if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("COUNT ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return count;
    }

    @Override
    public int count(String src, ConfigStore configs, String ... conditions){
        return count(src, configs, null, conditions);
    }
    @Override
    public int count(String src, Object obj, String ... conditions){
        return count(src, null, obj, conditions);
    }
    @Override
    public int count(String src, String ... conditions){
        return count(src, null, null, conditions);
    }


    /**
     * 更新记录
     * 默认情况下以主键为更新条件,需在更新的数据保存在data中
     * 如果提供了dest则更新dest表,如果没有提供则根据data解析出表名
     * DataRow/DataSet可以临时设置主键 如设置TYPE_CODE为主键,则根据TYPE_CODE更新
     * 可以提供了ConfigStore以实现更复杂的更新条件
     * 需要更新的列通过fixs/columns提供
     * @param fixs	  	需要更新的列
     * @param columns	需要更新的列
     * @param dest	   	表
     * @param data 		更新的数据及更新条件(如果有ConfigStore则以ConfigStore为准)
     * @param configs 	更新条件
     * @return int 影响行数
     */
    @Override
    public int update(boolean async, String dest, Object data, ConfigStore configs, List<String> fixs, String ... columns){
        dest = DataSourceHolder.parseDataSource(dest,dest);
        fixs = BeanUtil.merge(fixs, columns);
        final List<String> cols = BeanUtil.merge(fixs, columns);
        final String _dest = BasicUtil.compress(dest);
        final Object _data = data;
        final ConfigStore _configs = configs;
        if(async){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    dao.update(_dest, _data, _configs,  cols);
                }
            }).start();
            return 0;
        }else{
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
    public int update(boolean async, Object data, ConfigStore configs, List<String> fixs, String ... columns){
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
    public int update(boolean async,  Object data, ConfigStore configs, String[] fixs, String... columns) {
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
        return update(false, dest,  data, configs, BeanUtil.array2list(fixs, columns));
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
        return update(false, null,  data, configs, BeanUtil.array2list(fixs, columns));
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




    public int save(boolean async, String dest, Object data, boolean checkPrimary, String[] fixs,  String... columns) {
        return save(async, dest, data, checkPrimary, BeanUtil.array2list(fixs, columns));
    }
    public int save(boolean async, String dest, Object data, boolean checkPrimary,  String... columns) {
        return save(async, dest, data, checkPrimary, BeanUtil.array2list(columns));
    }

    @Override
    public int save(boolean async, String dest, Object data, boolean checkPrimary, List<String> fixs,  String... columns) {
        if(async){

            final String _dest = dest;
            final Object _data = data;
            final boolean _chk = checkPrimary;
            final String[] cols = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
            new Thread(new Runnable(){
                @Override
                public void run() {
                    save(_dest, _data, _chk, cols);
                }

            }).start();
            return 0;
        }else{
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
    public int save(Object data, boolean checkPrimary, List<String>fixs, String... columns) {
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
    public int save(boolean async, Object data, boolean checkPrimary,  String... columns) {
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
    public int save(Object data,  String... columns) {
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
        if(BasicUtil.isEmpty(dest)) {
            if (data instanceof DataRow || data instanceof DataSet) {
                dest = DataSourceHolder.parseDataSource(dest, data);
            }else{
                if(AdapterProxy.hasAdapter()){
                    dest = AdapterProxy.table(data.getClass());
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
        dest = DataSourceHolder.parseDataSource(dest,data);
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
    public int insert(Object data, boolean checkPrimary,  String... columns) {
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
        return insert(null, data, false,columns);
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
        Procedure proc = new DefaultProcedure();
        proc.setName(procedure);
        for (String input : inputs) {
            proc.addInput(input);
        }
        return execute(proc);
    }

    @Override
    public boolean execute(Procedure procedure, String ... inputs) {
        procedure.setName(DataSourceHolder.parseDataSource(procedure.getName(),null));
        if(null != inputs){
            for(String input:inputs){
                procedure.addInput(input);
            }
        }
        return dao.execute(procedure);
    }


    /**
     * 根据存储过程查询
     *
     * @param procedure  procedure
     * @return DataSet
     */
    @Override
    public DataSet querys(Procedure procedure, PageNavi navi, String ... inputs) {
        DataSet set = null;
        try {
            procedure.setName(DataSourceHolder.parseDataSource(procedure.getName()));
            if(null != inputs){
                for(String input:inputs){
                    procedure.addInput(input);
                }
            }
            set = dao.querys(procedure, navi);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            log.error("QUERY ERROR:"+e);
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return set;
    }

    public DataSet querys(Procedure procedure,  String ... inputs) {
        return querys(procedure, null, inputs);
    }
    public DataSet querys(Procedure procedure, int fr, int to, String ... inputs) {
        PageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        return querys(procedure, navi, inputs);
    }

    @Override
    public DataSet querysProcedure(String procedure, PageNavi navi, String... inputs) {
        Procedure proc = new DefaultProcedure();
        proc.setName(procedure);
        if(null != inputs) {
            for (String input : inputs) {
                proc.addInput(input);
            }
        }
        return querys(proc, navi);
    }
    public DataSet querysProcedure(String procedure, int fr, int to, String... inputs) {
        PageNavi navi = new DefaultPageNavi();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        return querysProcedure(procedure, navi, inputs);
    }
    public DataSet querysProcedure(String procedure, String... inputs) {
        return querysProcedure(procedure, null, inputs);
    }
    public DataRow queryProcedure(String procedure, String... inputs) {
        Procedure proc = new DefaultProcedure();
        proc.setName(procedure);
        return query(procedure, inputs);
    }

    public DataRow query(Procedure procedure,  String ... inputs) {
        DataSet set = querys(procedure, 0,0, inputs);
        if(set.size()>0){
            return set.getRow(0);
        }
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL){
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
    public int delete(String dest, DataSet set, String ... columns) {
        int cnt = 0;
        int size = set.size();
        for (int i = 0; i < size; i++) {
            cnt += delete(dest, set.getRow(i), columns);
        }
        log.warn("[delete DataSet][影响行数:{}]", LogUtil.format(cnt, 34));
        return cnt;
    }

    @Override
    public int delete(DataSet set, String ... columns) {
        String dest = DataSourceHolder.parseDataSource(null,set);
        return delete(dest, set, columns);
    }
    @Override
    public int delete(String dest, DataRow row, String ... columns) {
        dest = DataSourceHolder.parseDataSource(dest, row);
        return dao.delete(dest, row, columns);
    }
    @Override
    public int delete(Object obj, String ... columns) {
        if(null == obj){
            return 0;
        }
        String dest = null;
        if(obj instanceof DataRow) {
            DataRow row = (DataRow)obj;
            dest = DataSourceHolder.parseDataSource(null, row);
            return dao.delete(dest, row, columns);
        }else{
            if(AdapterProxy.hasAdapter()){
                if(obj instanceof Collection){
                    dest = AdapterProxy.table(((Collection)obj).iterator().next().getClass());
                }else{
                    dest = AdapterProxy.table(obj.getClass());
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
    public int deletes(String table, String key, Collection<Object> values){
        table = DataSourceHolder.parseDataSource(table);
        return dao.deletes(table, key, values);
    }

    @Override
    public int deletes(String table, String key, String ... values){
        table = DataSourceHolder.parseDataSource(table);
        return dao.deletes(table, key, values);
    }

    @Override
    public int delete(String table, ConfigStore configs, String ... conditions){
        table = DataSourceHolder.parseDataSource(table);
        return dao.delete(table, configs, conditions);
    }
    protected PageNavi setPageLazy(String src, ConfigStore configs, String ... conditions){
        PageNavi navi =  null;
        String lazyKey = null;
        if(null != configs){
            navi = configs.getPageNavi();
            if(null != navi && navi.isLazy()){
                lazyKey = CacheUtil.createCacheElementKey(false, false, src, configs, conditions);
                navi.setLazyKey(lazyKey);
                int total = PageLazyStore.getTotal(lazyKey, navi.getLazyPeriod());
                navi.setTotalRow(total);
            }
        }
        return navi;
    }
    protected DataSet queryFromDao(RunPrepare prepare, ConfigStore configs, String... conditions){
        DataSet set = null;
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", prepare.getText());
        }
        try {
            setPageLazy(prepare.getText(), configs, conditions);
            set = dao.querys(prepare, configs, conditions);
         } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return set;
    }
    protected DataSet queryFromDao(String src, ConfigStore configs, String... conditions){
        DataSet set = null;
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", src);
        }
        try {
            setPageLazy(src, configs, conditions);
            RunPrepare prepare = createRunPrepare(src);
            set = dao.querys(prepare, configs, conditions);

         } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return set;
    }
    protected <T> EntitySet<T> queryFromDao(String src, Class<T> clazz, ConfigStore configs, String... conditions){
        EntitySet<T> list = null;
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", clazz);
        }
        try {
            setPageLazy(src, configs, conditions);
            RunPrepare prepare = createRunPrepare(src);
            list = dao.querys(prepare, clazz, configs, conditions);
        } catch (Exception e) {
            list = new EntitySet<>();
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return list;
    }
    protected <T> EntitySet<T> queryFromDao(Class<T> clazz, ConfigStore configs, String... conditions){
        EntitySet<T> list = null;
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", clazz);
        }
        try {
            setPageLazy(clazz.getName(), configs, conditions);
            list = dao.querys(clazz, configs, conditions);
        } catch (Exception e) {
            list = new EntitySet<>();
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                throw e;
            }
        }
        return list;
    }
    /**
     * 解析SQL中指定的主键table(col1,col2)&lt;pk1,pk2&gt;
     * @param src  src
     * @param pks  pks
     * @return String
     */
    protected String parsePrimaryKey(String src, List<String> pks){
        if(src.endsWith(">")){
            int fr = src.lastIndexOf("<");
            int to = src.lastIndexOf(">");
            if(fr != -1){
                String pkstr = src.substring(fr+1,to);
                src = src.substring(0, fr);
                String[] tmps = pkstr.split(",");
                for(String tmp:tmps){
                    pks.add(tmp);
                    if(ConfigTable.isSQLDebug()){
                        log.warn("[解析SQL主键] [KEY:{}]",tmp);
                    }
                }
            }
        }
        return src;
    }

    protected synchronized RunPrepare createRunPrepare(String src){
        RunPrepare prepare = null;
        src = src.trim();
        List<String> pks = new ArrayList<>();
        // 文本sql
        if (src.startsWith("${") && src.endsWith("}")) {
            if(ConfigTable.isSQLDebug()){
                log.warn("[解析SQL类型] [类型:{JAVA定义}] [src:{}]",src);
            }
            src = src.substring(2,src.length()-1);
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            prepare = new DefaultTextPrepare(src);
        } else {
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            if(src.replace("\n","").replace("\r","").trim().matches("^[a-zA-Z]+\\s+.+")){
                if(ConfigTable.isSQLDebug()){
                    log.warn("[解析SQL类型] [类型:JAVA定义] [src:{}]", src);
                }
                prepare = new DefaultTextPrepare(src);
            }else if (RegularUtil.match(src, RunPrepare.XML_SQL_ID_STYLE)) {
                /* XML定义 */
                if(ConfigTable.isSQLDebug()){
                    log.warn("[解析SQL类型] [类型:XML定义] [src:{}]", src);
                }
                prepare = DefaultSQLStore.parseSQL(src);
                if(null == prepare){
                    log.error("[解析SQL类型][XML解析失败][src:{}]",src);
                }
            } else {
                /* 自动生成 */
                if(ConfigTable.isSQLDebug()){
                    log.warn("[解析SQL类型] [类型:auto] [src:{}]", src);
                }
                prepare = new DefaultTablePrepare();
                prepare.setDataSource(src);
            }
        }
        if(null != prepare && pks.size()>0){
            prepare.setPrimaryKey(pks);
        }
        return prepare;
    }
    protected DataSet queryFromCache(String cache, String src, ConfigStore configs, String ... conditions){
        if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
            log.warn("[cache from][cache:{}][src:{}]", cache, src);
        }
        DataSet set = null;
        String key = "SET:";
        if(cache.contains(">")){
            String tmp[] = cache.split(">");
            cache = tmp[0];
        }
        if(cache.contains(":")){
            String ks[] = BeanUtil.parseKeyValue(cache);
            cache = ks[0];
            key += ks[1]+":";
        }
        key += CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
        RunPrepare prepare = createRunPrepare(src);
        if(null != cacheProvider) {
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
                        log.warn("[缓存即将到期提前刷新][src:{}] [生存:{}/{}]", src, age, _max);
                    }
                    final String _key = key;
                    final String _cache = cache;
                    final RunPrepare _sql = prepare;
                    final ConfigStore _configs = configs;
                    final String[] _conditions = conditions;
                    new Thread(new Runnable() {
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

    private ConfigStore append(ConfigStore configs, Object entity){
        if(null == configs){
            configs = new DefaultConfigStore();
        }
        if(null != entity) {
            if(entity instanceof Map){
                Map map = (Map)entity;
                for(Object key:map.keySet()){
                    Object value = map.get(key);
                    if (value instanceof Collection) {
                        configs.ands(key.toString(), value);
                    } else {
                        configs.and(key.toString(), value);
                    }
                }
            }else {
                List<Field> fields = ClassUtil.getFields(entity.getClass());
                for (Field field : fields) {
                    Object value = BeanUtil.getFieldValue(entity, field);
                    if (BasicUtil.isNotEmpty(true, value)) {
                        String key = field.getName();
                        if (AdapterProxy.hasAdapter()) {
                            key = AdapterProxy.column(entity.getClass(), field);
                        }
                        if(BasicUtil.isEmpty(key)){
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

    public List<String> tables(String catalog, String schema, String name, String types){
        LinkedHashMap<String, Table> tables = metadata.tables(catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for(Table table:tables.values()){
            list.add(table.getName());
        }
        return list;
    }
    public List<String> tables(String schema, String name, String types){
        return tables(null, schema, name, types);
    }
    public List<String> tables(String name, String types){
        return tables(null, null, name, types);
    }
    public List<String> tables(String types){
        return tables(null, null, null, types);
    }
    public List<String> tables(){
        return tables(null);
    }

    @Override
    public List<String> mtables(String catalog, String schema, String name, String types) {
        LinkedHashMap<String, MasterTable> tables = metadata.mtables(catalog, schema, name, types);
        List<String> list = new ArrayList<>();
        for(MasterTable table:tables.values()){
            list.add(table.getName());
        }
        return list;
    }

    @Override
    public List<String> mtables(String schema, String name, String types) {
        return mtables(null, schema, name, types);
    }

    @Override
    public List<String> mtables(String name, String types) {
        return mtables(null, null, name, types);
    }

    @Override
    public List<String> mtables(String types) {
        return mtables(null, null, null, types);
    }

    @Override
    public List<String> mtables() {
        return mtables("STABLE");
    }

    public LinkedHashMap<String, Column> columns(String table, boolean map){
        return metadata.columns(table);
    }


    @Override
    public List<String> columns(Table table){
        return columns(table.getCatalog(), table.getSchema(), table.getName());
    }
    public List<String> columns(String table){
        return columns(null, null, table);
    }
    public List<String> columns(String catalog, String schema, String table){
        LinkedHashMap<String,Column> columns = metadata.columns(catalog, schema, table);
        List<String> list = new ArrayList<>();
        for(Column column:columns.values()){
            list.add(column.getName());
        }
        return list;
    }


    @Override
    public List<String> tags(Table table){
        return tags(table.getCatalog(), table.getSchema(), table.getName());
    }
    public List<String> tags(String table){
        return tags(null, null, table);
    }
    public List<String> tags(String catalog, String schema, String table){
        LinkedHashMap<String, Tag> tags = metadata.tags(catalog, schema, table);
        List<String> list = new ArrayList<>();
        for(Tag tag:tags.values()){
            list.add(tag.getName());
        }
        return list;
    }
    /**
     * 修改表结构
     * @param table 表
     * @throws Exception 异常 SQL异常
     */
    public boolean save(Table table) throws Exception{
        return ddl.save(table);
    }
    /**
     * 修改列  名称 数据类型 位置 默认值
     * 执行save前先调用column.update()设置修改后的属性
     * column.update().setName().setDefaultValue().setAfter()....
     * @param column 列
     * @throws Exception 异常 SQL异常
     */
    public boolean save(Column column) throws Exception{
        return ddl.save(column);
    }
    public boolean drop(Table table) throws Exception{
        return ddl.drop(table);
    }
    public boolean drop(Column column) throws Exception{
        return ddl.drop(column);
    }



    public MetaDataService metadata(){
        return metadata;
    }
    public DDLService ddl(){
        return ddl;
    }

    public void clearColumnCache(String catalog, String schema, String table){
        CacheProxy.clearColumnCache(catalog, schema, table);
    }
    public void clearTagCache(String catalog, String schema, String table){
        CacheProxy.clearTagCache(catalog, schema, table);

    }
    /* *****************************************************************************************************************
     *
     * 													metadata
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
    public MetaDataService metadata = new MetaDataService() {

        /* *****************************************************************************************************************
         * 													database
         * -----------------------------------------------------------------------------------------------------------------
         * public LinkedHashMap<String,Database> databases();
         ******************************************************************************************************************/
        @Override
        public LinkedHashMap<String,Database> databases(){
            return dao.databases();
        }

        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
         * public boolean exists(Table table)
         * public LinkedHashMap<String,Table> tables(String catalog, String schema, String name, String types)
         * public LinkedHashMap<String,Table> tables(String schema, String name, String types)
         * public LinkedHashMap<String,Table> tables(String name, String types)
         * public LinkedHashMap<String,Table> tables(String types)
         * public LinkedHashMap<String,Table> tables()
         * public Table table(String catalog, String schema, String name)
         * public Table table(String schema, String name)
         * public Table table(String name)
         ******************************************************************************************************************/
        @Override
        public boolean exists(Table table) {
            if(null != table(table.getCatalog(), table.getSchema(), table.getName())){
                return true;
            }
            return false;
        }

        @Override
        public LinkedHashMap<String, Table> tables(String schema, String name, String types) {
            return tables(null, schema, name, types);
        }

        @Override
        public LinkedHashMap<String, Table> tables(String name, String types) {
            return tables(null, null, name, types);
        }

        @Override
        public LinkedHashMap<String, Table> tables(String types) {
            return tables(null, types);
        }

        @Override
        public LinkedHashMap<String, Table> tables() {
            return tables(null);
        }


        @Override
        public LinkedHashMap<String, Table> tables(String catalog, String schema, String name, String types) {
            return dao.tables(catalog, schema, name, types);
        }

        @Override
        public Table table(String catalog, String schema,String name) {
            Table table = null;
            LinkedHashMap<String, Table> tables = tables(catalog, schema, name,null);
            if(tables.size()>0){
                table = tables.values().iterator().next();
                table.setColumns(columns(table));
                table.setTags(tags(table));
                table.setIndexs(indexs(table));
            }
            return table;
        }
        @Override
        public Table table(String schema,String name) {
            return table(null, schema, name);
        }
        @Override
        public Table table(String name) {
            return table(null, null, name);
        }


        /* *****************************************************************************************************************
         * 													master table
         * -----------------------------------------------------------------------------------------------------------------
         * public boolean exists(MasterTable table)
         * public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types)
         * public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types)
         * public LinkedHashMap<String, MasterTable> mtables(String name, String types)
         * public LinkedHashMap<String, MasterTable> mtables(String types)
         * public LinkedHashMap<String, MasterTable> mtables()
         * public MasterTable mtable(String catalog, String schema, String name)
         * public MasterTable mtable(String schema, String name)
         * public MasterTable mtable(String name)
         ******************************************************************************************************************/

        @Override
        public boolean exists(MasterTable table) {
            return false;
        }


        @Override
        public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types) {
            return dao.mtables(catalog, schema, name, types);
        }

        @Override
        public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types) {
            return mtables(null, schema, name, types);
        }

        @Override
        public LinkedHashMap<String, MasterTable> mtables(String name, String types) {
            return mtables(null, null, name, types);
        }

        @Override
        public LinkedHashMap<String, MasterTable> mtables(String types) {
            return mtables(null, types);
        }

        @Override
        public LinkedHashMap<String, MasterTable> mtables() {
            return mtables("STABLE");
        }

        @Override
        public MasterTable mtable(String catalog, String schema, String name) {
            LinkedHashMap<String, MasterTable> tables = mtables(catalog, schema, name, "STABLE");
            if(tables.size() == 0){
                return null;
            }
            MasterTable table = tables.values().iterator().next();
            table.setColumns(columns(table));
            table.setTags(tags(table));
            table.setIndexs(indexs(table));
            return table;
        }

        @Override
        public MasterTable mtable(String schema, String name) {
            return mtable(null, schema, name);
        }

        @Override
        public MasterTable mtable(String name) {
            return mtable(null, null, name);
        }

        /* *****************************************************************************************************************
         * 													partition  table
         * -----------------------------------------------------------------------------------------------------------------
         * public boolean exists(PartitionTable table)
         * public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types)
         * public LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types)
         * public LinkedHashMap<String, PartitionTable> ptables(String name, String types)
         * public LinkedHashMap<String, PartitionTable> ptables(String types)
         * public LinkedHashMap<String, PartitionTable> ptables()
         * public LinkedHashMap<String, PartitionTable> ptables(MasterTable master)
         * public PartitionTable ptable(String catalog, String schema, String name)
         * public PartitionTable ptable(String schema, String name)
         * public PartitionTable ptable(String name)
         ******************************************************************************************************************/

        @Override
        public boolean exists(PartitionTable table) {
            return false;
        }


        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String master, String name) {
            return dao.ptables(catalog, schema, master, name);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String schema, String master, String name) {
            return ptables(null, schema, master, name);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String master, String name) {
            return ptables(null, null, master, name);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String master) {
            return ptables(null, null, master, null);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(MasterTable master) {
            return dao.ptables(master);
        }
        @Override
        public LinkedHashMap<String, PartitionTable> ptables(MasterTable master, Map<String, Object> tags) {
            return dao.ptables(master, tags);
        }
        @Override
        public LinkedHashMap<String, PartitionTable> ptables(MasterTable master, Map<String, Object> tags, String name) {
            return dao.ptables(master, tags, name);
        }

        @Override
        public PartitionTable ptable(String catalog, String schema, String master, String name) {
            LinkedHashMap<String, PartitionTable> tables = ptables(catalog, schema, master, name);
            if(tables.size() == 0){
                return null;
            }
            PartitionTable table = tables.values().iterator().next();
            table.setColumns(columns(table));
            table.setTags(tags(table));
            table.setIndexs(indexs(table));
            return table;
        }

        @Override
        public PartitionTable ptable(String schema, String master, String name) {
            return ptable(null, schema, master, name);
        }

        @Override
        public PartitionTable ptable(String master, String name) {
            return ptable(null, null, master, name);
        }

        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean exists(Column column);
		 * public boolean exists(Table table, String name);
		 * public boolean exists(String table, String name);
         * public boolean exists(String catalog, String schema, String table, String name);
         * public LinkedHashMap<String,Column> columns(Table table)
         * public LinkedHashMap<String,Column> columns(String table)
         * public LinkedHashMap<String,Column> columns(String catalog, String schema, String table)
		 * public LinkedHashMap<String,Column> column(Table table, String name);
		 * public LinkedHashMap<String,Column> column(String table, String name);
         * public LinkedHashMap<String,Column> column(String catalog, String schema, String table, String name);
         ******************************************************************************************************************/
        @Override
        public boolean exists(Column column) {
            try {
                Table table = table(column.getCatalog(), column.getSchema(), column.getTableName());
                if(null != table){
                    if(table.getColumns().containsKey(column.getName().toUpperCase())){
                        return true;
                    }
                }
            }catch (Exception e){

            }
            return false;
        }
        @Override
        public boolean exists(Table table, String name) {
            try {
                LinkedHashMap<String,Column> columns = table.getColumns();
                if(null == columns && columns.isEmpty()){
                    columns = columns(table);
                }
                if(columns.containsKey(name.toUpperCase())){
                    return true;
                }
            }catch (Exception e){

            }
            return false;
        }
        @Override
        public boolean exists(String table, String name) {
            try {
                LinkedHashMap<String,Column> columns = columns(table);
                if(columns.containsKey(name.toUpperCase())){
                    return true;
                }
            }catch (Exception e){

            }
            return false;
        }
        public boolean exists(String catalog, String schema, String table, String name){
            try {
                LinkedHashMap<String,Column> columns = columns(catalog, schema, table);
                if(columns.containsKey(name.toUpperCase())){
                    return true;
                }
            }catch (Exception e){

            }
            return false;
        }


        @Override
        public LinkedHashMap<String,Column> columns(String table){
            return columns(null, null, table);
        }
        @Override
        public LinkedHashMap<String,Column> columns(Table table){
            LinkedHashMap<String,Column> columns = CacheProxy.columns(table.getName());
            if(null == columns){
                columns = dao.columns(table);
                CacheProxy.columns(table.getName(), columns);
            }
            return columns;

        }
        @Override
        public LinkedHashMap<String,Column> columns(String catalog, String schema, String table){
            return columns(new Table(catalog, schema, table));
        }

        public Column column(Table table, String name){
            Column column = null;
            LinkedHashMap<String,Column> columns = table.getColumns();
            if(null == columns && columns.isEmpty()){
                columns = columns(table);
            }
            column = columns.get(name.toUpperCase());
            return column;
        }
        public Column column(String table, String name){
            Column column = null;
            LinkedHashMap<String,Column> columns =  columns(table);
            column = columns.get(name.toUpperCase());
            return column;

        }
        public Column column(String catalog, String schema, String table, String name){
            Column column = null;
            LinkedHashMap<String,Column> columns =  columns(catalog, schema, table);
            column = columns.get(name.toUpperCase());
            return column;
        }


        /* *****************************************************************************************************************
         * 													tag
         * -----------------------------------------------------------------------------------------------------------------
         * public LinkedHashMap<String,Tag> tags(String catalog, String schema, String table)
         * public LinkedHashMap<String,Tag> tags(String table)
         * public LinkedHashMap<String,Tag> tags(Table table)
         ******************************************************************************************************************/

        @Override
        public LinkedHashMap<String,Tag> tags(String catalog, String schema, String table){
            return tags(new Table(catalog, schema, table));
        }
        @Override
        public LinkedHashMap<String,Tag> tags(String table){
            return tags(null, null, table);
        }
        @Override
        public LinkedHashMap<String,Tag> tags(Table table){
            LinkedHashMap<String,Tag> tags = CacheProxy.tags(table.getName());
            if(null == tags){
                tags = dao.tags(table);
                CacheProxy.tags(table.getName(), tags);
            }
            return tags;
        }
        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
         * public LinkedHashMap<String, Index> indexs(Table table)
         * public LinkedHashMap<String, Index> indexs(String table)
         * public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table)
         ******************************************************************************************************************/
        @Override
        public LinkedHashMap<String, Index> indexs(Table table) {
            return dao.indexs(table);
        }

        @Override
        public LinkedHashMap<String, Index> indexs(String table) {
            return indexs(new Table(table));
        }

        @Override
        public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table) {
            return indexs(new Table(catalog, schema, table));
        }

        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
		 * public LinkedHashMap<String,Constraint> constraints(Table table)
		 * public LinkedHashMap<String,Constraint> constraints(String table)
		 * public LinkedHashMap<String,Constraint> constraints(String catalog, String schema, String table)
         ******************************************************************************************************************/
        @Override
        public LinkedHashMap<String, Constraint> constraints(Table table) {
            return null;
        }

        @Override
        public LinkedHashMap<String, Constraint> constraints(String table) {
            return null;
        }

        @Override
        public LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table) {
            return null;
        }

    };


    /* *****************************************************************************************************************
     *
     * 													DDL
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

    public DDLService ddl = new DDLService() {
        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(Table table) throws Exception
		 * public boolean create(Table table) throws Exception
		 * public boolean alter(Table table) throws Exception
         * public boolean drop(Table table) throws Exception
         ******************************************************************************************************************/

        @Override
        public boolean save(Table table) throws Exception{
            boolean result = false;
            Table otable = metadata.table(table.getCatalog(), table.getSchema(), table.getName());
            if(null != otable){
                otable.setUpdate(table);
                result = alter(otable);
            }else{
                result =  create(table);
            }

            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }
        @Override
        public boolean create(Table table) throws Exception{
            table.setService(DefaultService.this);
            boolean result =  dao.create(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }
        @Override
        public boolean alter(Table table) throws Exception{
            table.setService(DefaultService.this);
            boolean result = dao.alter(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean drop(Table table) throws Exception{
            table.setService(DefaultService.this);
            boolean result = dao.drop(table);

            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        /* *****************************************************************************************************************
         * 													master table
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(MasterTable master) throws Exception
		 * public boolean create(MasterTable master) throws Exception
		 * public boolean alter(MasterTable master) throws Exception
         * public boolean drop(MasterTable master) throws Exception
         ******************************************************************************************************************/


        @Override
        public boolean save(MasterTable table) throws Exception {
            boolean result = false;
            MasterTable otable = metadata.mtable(table.getCatalog(), table.getSchema(), table.getName());
            if(null != otable){
                otable.setUpdate(table);
                result = alter(otable);
            }else{
                result =  create(table);
            }

            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }
        @Override
        public boolean create(MasterTable table) throws Exception {
            table.setService(DefaultService.this);
            boolean result =  dao.create(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean alter(MasterTable table) throws Exception {
            table.setService(DefaultService.this);
            boolean result = dao.alter(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean drop(MasterTable table) throws Exception {
            table.setService(DefaultService.this);
            boolean result = dao.drop(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        /* *****************************************************************************************************************
         * 													partition table
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(PartitionTable table) throws Exception
		 * public boolean create(PartitionTable table) throws Exception
		 * public boolean alter(PartitionTable table) throws Exception
         * public boolean drop(PartitionTable table) throws Exception
         ******************************************************************************************************************/
        @Override
        public boolean save(PartitionTable table) throws Exception {
            boolean result = false;
            PartitionTable otable = metadata.ptable(table.getCatalog(), table.getSchema(), table.getName());
            if(null != otable){
                otable.setUpdate(table);
                result = alter(otable);
            }else{
                result =  create(table);
            }

            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean create(PartitionTable table) throws Exception {
            table.setService(DefaultService.this);
            boolean result =  dao.create(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean alter(PartitionTable table) throws Exception {
            table.setService(DefaultService.this);
            boolean result = dao.alter(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean drop(PartitionTable table) throws Exception {
            table.setService(DefaultService.this);
            boolean result = dao.drop(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(Column column) throws Exception
		 * public boolean add(Column column) throws Exception
		 * public boolean alter(Column column) throws Exception
         * public boolean drop(Column column) throws Exception
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
            Table table = metadata.table(column.getCatalog(), column.getSchema(), column.getTableName());
            if(null == table){
                throw new AnylineException("表不存在:"+column.getTableName());
            }
            LinkedHashMap<String, Column> columns = table.getColumns();
            Column original = columns.get(column.getName().toUpperCase());
            if(null == original){
                result = add(columns, column);
            }else {
                result = alter(table, column);
            }
            clearColumnCache(column.getCatalog(), column.getSchema(), column.getTableName());
            return result;
        }
        @Override
        public boolean add(Column column) throws Exception{
            LinkedHashMap<String, Column> columns = metadata.columns(column.getCatalog(), column.getSchema(), column.getTableName());
            boolean result = add(columns, column);

            clearColumnCache(column.getCatalog(), column.getSchema(), column.getTableName());
            return result;
        }
        @Override
        public boolean alter(Column column) throws Exception{
            Table table = metadata.table(column.getCatalog(), column.getSchema(), column.getTableName());
            boolean result = alter(table, column);
            clearColumnCache(column.getCatalog(), column.getSchema(), column.getTableName());
            return result;
        }

        @Override
        public boolean drop(Column column) throws Exception{
            column.setService(DefaultService.this);
            boolean result = dao.drop(column);
            clearColumnCache(column.getCatalog(), column.getSchema(), column.getTableName());
            return result;
        }

        private boolean add(LinkedHashMap<String, Column> columns, Column column) throws Exception{
            column.setService(DefaultService.this);
            boolean result =  dao.add(column);
            if(result) {
                columns.put(column.getName(), column);
            }
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
            LinkedHashMap<String, Column> columns = table.getColumns();
            Column original = columns.get(column.getName().toUpperCase());

            Column update = column.getUpdate();
            if(null == update){
                update = (Column) column.clone();
                String newName = column.getNewName();
                if(BasicUtil.isNotEmpty(newName)){
                    update.setName(newName);
                }
            }
            original.setUpdate(update);
            original.setService(DefaultService.this);
            String name = original.getName();
            try {
                result = dao.alter(table, original);
            }finally {
                original.setName(name);
            }
            if(result) {
                columns.remove(original.getName());

                BeanUtil.copyFieldValueWithoutNull(original, update);
                original.setUpdate(update);
                BeanUtil.copyFieldValue(column, original);
                columns.put(original.getName(), original);
            }
            return result;
        }


        /* *****************************************************************************************************************
         * 													tag
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(Tag tag) throws Exception
		 * public boolean add(Tag tag) throws Exception
		 * public boolean alter(Tag tag) throws Exception
         * public boolean drop(Tag tag) throws Exception
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
            Table table = metadata.table(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            if(null == table){
                throw new AnylineException("表不存在:"+tag.getTableName());
            }
            LinkedHashMap<String, Tag> tags = table.getTags();
            Tag original = tags.get(tag.getName().toUpperCase());
            if(null == original){
                result = add(tags, tag);
            }else {
                result = alter(table, tag);
            }
            clearTagCache(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            return result;
        }


        @Override
        public boolean add(Tag tag) throws Exception{
            LinkedHashMap<String, Tag> tags = metadata.tags(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            boolean result = add(tags, tag);
            clearTagCache(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            return result;
        }

        @Override
        public boolean alter(Tag tag) throws Exception{
            Table table = metadata.table(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            boolean result = alter(table, tag);
            clearTagCache(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            return result;
        }

        @Override
        public boolean drop(Tag tag) throws Exception{
            tag.setService(DefaultService.this);
            boolean result = dao.drop(tag);
            clearTagCache(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            return result;
        }
        private boolean add(LinkedHashMap<String, Tag> tags, Tag tag) throws Exception{
            tag.setService(DefaultService.this);
            boolean result =  dao.add(tag);
            if(result) {
                tags.put(tag.getName(), tag);
            }
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
            LinkedHashMap<String, Tag> tags = table.getTags();
            Tag original = tags.get(tag.getName().toUpperCase());

            Tag update = tag.getUpdate();
            if(null == update){
                update = (Tag) tag.clone();
                String newName = tag.getNewName();
                if(BasicUtil.isNotEmpty(newName)){
                    update.setName(newName);
                }
            }
            original.setUpdate(update);
            original.setService(DefaultService.this);
            result = dao.alter(table, original);
            if(result) {
                tags.remove(original.getName());

                BeanUtil.copyFieldValueWithoutNull(original, update);
                original.setUpdate(update);
                BeanUtil.copyFieldValue(tag, original);
                tags.put(original.getName(), original);
            }
            return result;
        }

        /* *****************************************************************************************************************
         * 													index
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean add(Index index) throws Exception
		 * public boolean alter(Index index) throws Exception
         * public boolean drop(Index index) throws Exception
         ******************************************************************************************************************/

        @Override
        public boolean add(Index index) throws Exception{
            index.setService(DefaultService.this);
            return dao.add(index);
        }

        @Override
        public boolean alter(Index index) throws Exception {
            return false;
        }

        public boolean drop(Index index) throws Exception{
            index.setService(DefaultService.this);
            return dao.drop(index);
        }
        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean add(Constraint constraint) throws Exception
		 * public boolean alter(Constraint constraint) throws Exception
         * public boolean drop(Constraint constraint) throws Exception
         ******************************************************************************************************************/
        @Override
        public boolean add(Constraint constraint) throws Exception {
            constraint.setService(DefaultService.this);
            return dao.add(constraint);
        }

        @Override
        public boolean alter(Constraint constraint) throws Exception {
            return false;
        }

        @Override
        public boolean drop(Constraint constraint) throws Exception {
            constraint.setService(DefaultService.this);
            return dao.drop(constraint);
        }
    };
}
