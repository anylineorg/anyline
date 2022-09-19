/*
 * Copyright 2006-2022 www.anyline.org
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


package org.anyline.service.impl;

import org.anyline.cache.CacheElement;
import org.anyline.cache.CacheProvider;
import org.anyline.cache.CacheUtil;
import org.anyline.cache.PageLazyStore;
import org.anyline.dao.AnylineDao;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.impl.ProcedureImpl;
import org.anyline.jdbc.config.db.impl.SQLStoreImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.TextSQLImpl;
import org.anyline.jdbc.config.impl.ConfigStoreImpl;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.jdbc.entity.*;
import org.anyline.service.AnylineService;
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
public class AnylineServiceImpl<E> implements AnylineService<E> {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    @Qualifier("anyline.dao")
    protected AnylineDao dao;

    @Autowired(required = false)
    @Qualifier("anyline.cache.provider")
    protected CacheProvider cacheProvider;

    /**
     * 按条件查询
     * @param src 			数据源(表｜视图｜函数｜自定义SQL | SELECT语句)
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return DataSet
     */
    @Override
    public DataSet querys(String src, ConfigStore configs, Object obj, String... conditions) {
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        configs = append(configs, obj);
        return queryFromDao(src, configs, conditions);
    }
    @Override
    public DataSet querys(String src, PageNavi navi, Object obj, String... conditions) {
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return querys(src, configs, obj,conditions);
    }

    @Override
    public DataSet querys(String src, Object obj, String... conditions) {
        return querys(src, (ConfigStore)null, obj, conditions);
    }

    @Override
    public DataSet querys(String src, int fr, int to, Object obj, String... conditions) {
        ConfigStore configs = new ConfigStoreImpl(fr, to);
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
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", src);
        }
        try {
            SQL sql = createSQL(src);
            configs = append(configs, obj);
            maps = dao.maps(sql, configs, conditions);
        } catch (Exception e) {
            maps = new ArrayList<Map<String,Object>>();
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
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
        return maps(src, new ConfigStoreImpl(fr, to), obj, conditions);
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
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
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
        ConfigStore configs = new ConfigStoreImpl(fr, to);
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
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new ConfigStoreImpl();
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
        //是否启动缓存
        if(null == cache){
            return query(src, configs, obj, conditions);
        }
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new ConfigStoreImpl();
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

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        return queryFromDao(clazz, append(configs, entity), conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, T entity, String... conditions) {
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return querys(clazz, configs, entity, conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, T entity, String... conditions) {
        return querys(clazz, (ConfigStore)null, entity, conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, int fr, int to, T entity, String... conditions) {
        ConfigStore configs = new ConfigStoreImpl(fr, to);
        return querys(clazz, configs, entity, conditions);
    }

    @Override
    public <T> T query(Class<T> clazz, ConfigStore configs, T entity, String... conditions) {
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new ConfigStoreImpl();
        }
        configs.setPageNavi(navi);
        EntitySet<T> list = querys(clazz, configs, entity, conditions);
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
    public <T> T query(Class<T> clazz, T entity, String... conditions) {
        return query(clazz, (ConfigStore)null, entity, conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String... conditions) {
        return querys(clazz, configs, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, PageNavi navi, String... conditions) {
        return querys(clazz, navi, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, String... conditions) {
        return querys(clazz, (T)null, conditions);
    }

    @Override
    public <T> EntitySet<T> querys(Class<T> clazz, int fr, int to, String... conditions) {
        return querys(clazz, fr, to, (T)null, conditions);
    }

    @Override
    public <T> T query(Class<T> clazz, ConfigStore configs, String... conditions) {
        return query(clazz, configs, (T)null, conditions);
    }

    @Override
    public <T> T query(Class<T> clazz, String... conditions) {
        return query(clazz, (T)null, conditions);
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
        return querys(clazz, configs, conditions);
    }
    @Override
    public EntitySet<E> gets(PageNavi navi, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return querys(clazz, navi, conditions);
    }

    @Override
    public EntitySet<E> gets(String... conditions) {
        Class<E> clazz = parseGenericClass();
        return querys(clazz, conditions);
    }

    @Override
    public EntitySet<E> gets(int fr, int to, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return querys(clazz, fr, to, conditions);
    }

    @Override
    public E get(ConfigStore configs, String... conditions) {
        Class<E> clazz = parseGenericClass();
        return query(clazz, configs, conditions);
    }

    @Override
    public E get(String... conditions) {
        Class<E> clazz = parseGenericClass();
        return query(clazz, conditions);
    }



    /**
     * 按条件查询
     * @param sql 表｜视图｜函数｜自定义SQL |SQL
     * @param configs		根据http等上下文构造查询条件
     * @param obj			根据obj的field/value构造查询条件(支侍Map和Object)(查询条件只支持 =和in)
     * @param conditions 固定查询条件
     * @return DataSet
     */
    @Override
    public DataSet querys(SQL sql, ConfigStore configs, Object obj, String... conditions) {
        conditions = BasicUtil.compressionSpace(conditions);
        DataSet set = queryFromDao(sql, append(configs, obj), conditions);
        return set;

    }
    @Override
    public DataSet querys(SQL sql, ConfigStore configs, String... conditions) {
        return querys(sql, configs, null, conditions);
    }

    @Override
    public DataSet querys(SQL sql, Object obj, String... conditions) {
        return querys(sql, null, obj, conditions);
    }
    @Override
    public DataSet querys(SQL sql, String... conditions) {
        return querys(sql, null, null, conditions);
    }


    @Override
    public DataSet querys(SQL sql, int fr, int to, Object obj, String... conditions) {
        ConfigStore configs = new ConfigStoreImpl(fr,to);
        return querys(sql, configs, obj, conditions);
    }
    @Override
    public DataSet querys(SQL sql, int fr, int to,  String... conditions) {
        return querys(sql, fr, to, null, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, ConfigStore configs, Object obj, String ... conditions){
        DataSet set = null;
        conditions = BasicUtil.compressionSpace(conditions);
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
    public DataSet caches(String cache, SQL table, ConfigStore configs, String ... conditions){
        return caches(cache, table, configs, null, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, Object obj, String ... conditions){
        return caches(cache, table, null, obj, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, String ... conditions){
        return caches(cache, table, null, null, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, int fr, int to, Object obj, String ... conditions){
        ConfigStore configs = new ConfigStoreImpl(fr, to);
        return caches(cache, table, configs, obj, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, int fr, int to, String ... conditions){
        return caches(cache, table, fr, to, null, conditions);
    }

    @Override
    public DataRow query(SQL table, ConfigStore store, Object obj, String... conditions) {
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new ConfigStoreImpl();
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
    public DataRow query(SQL table, ConfigStore store, String... conditions) {
        return query(table, store, null, conditions);
    }

    @Override
    public DataRow query(SQL table, Object obj, String... conditions) {
        return query(table, null, obj, conditions);
    }

    @Override
    public DataRow query(SQL table, String... conditions) {
        return query(table, null, null, conditions);
    }

    @Override
    public DataRow cache(String cache, SQL table, ConfigStore configs, Object obj, String ... conditions){
        //是否启动缓存
        if(null == cache){
            return query(table, configs, obj, conditions);
        }
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new ConfigStoreImpl();
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
    public DataRow cache(String cache, SQL table, ConfigStore configs, String ... conditions){
        return cache(cache, table, configs, null, conditions);
    }
    @Override
    public DataRow cache(String cache, SQL table, Object obj, String ... conditions){
        return cache(cache, table, null, obj, conditions);
    }
    @Override
    public DataRow cache(String cache, SQL table,  String ... conditions){
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
            src = BasicUtil.compressionSpace(src);
            conditions = BasicUtil.compressionSpace(conditions);
            String key = CacheUtil.createCacheElementKey(true, true, src, configs, conditions);
            cacheProvider.remove(channel, "SET:" + key);
            cacheProvider.remove(channel, "ROW:" + key);

            PageNaviImpl navi = new PageNaviImpl();
            navi.setFirstRow(0);
            navi.setLastRow(0);
            navi.setCalType(1);
            if (null == configs) {
                configs = new ConfigStoreImpl();
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
        ConfigStore configs = new ConfigStoreImpl(fr, to);
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
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        SQL sql = createSQL(src);
        result = dao.exists(sql, append(configs, obj), conditions);
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
            //conditions = parseConditions(conditions);
            src = BasicUtil.compressionSpace(src);
            conditions = BasicUtil.compressionSpace(conditions);
            SQL sql = createSQL(src);
            count = dao.count(sql, append(configs, obj), conditions);
        } catch (Exception e) {
            if(ConfigTable.isDebug() && log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("COUNT ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
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
     *
     * @param async  是否异步
     * @param dest  dest
     * @param data  需要更新的数据
     * @param fixs 需要更新的列
     * @param columns 需要更新的列
     * @return int
     */
    @Override
    public int update(boolean async, String dest, Object data, List<String> fixs, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,dest);
        fixs = BeanUtil.merge(fixs, columns);
        final String cols[] = BasicUtil.compressionSpace(BeanUtil.list2array(fixs));
        final String _dest = BasicUtil.compressionSpace(dest);
        final Object _data = data;
        if(async){
            new Thread(new Runnable(){
                @Override
                public void run() {
                    dao.update(_dest, _data, cols);
                }
            }).start();
            return 0;
        }else{
            return dao.update(dest, data, cols);
        }
    }

    public int update(boolean async, String dest, Object data, String[] fixs, String... columns) {
        return update(async, dest, data, BeanUtil.array2list(fixs, columns));
    }
    public int update(boolean async, String dest, Object data, String... columns) {
        return update(async, dest, data, BeanUtil.array2list(columns));
    }

    @Override
    public int update(String dest, ConfigStore configs, List<String> fixs, String... conditions) {
        //TODO
        return 0;
    }
    public int update(String dest, ConfigStore configs, String[] fixs, String... conditions) {
        //TODO
        return 0;
    }
    public int update(String dest, ConfigStore configs, String... conditions) {
        //TODO
        return 0;
    }
    @Override
    public int update(String dest, Object data, List<String> fixs, String... columns) {
        fixs = BeanUtil.merge(fixs, columns);
        dest = BasicUtil.compressionSpace(dest);
        dest = DataSourceHolder.parseDataSource(dest,data);
        columns = BasicUtil.compressionSpace(BeanUtil.list2array(fixs));
        return dao.update(dest, data, columns);
    }

    @Override
    public int update(String dest, Object data, String[] fixs, String... columns) {
        return update(dest, data, BeanUtil.array2list(fixs, columns));
    }

    @Override
    public int update(String dest, Object data, String... columns) {
        return update(dest, data, BeanUtil.array2list(columns));
    }


    @Override
    public int update(Object data, List<String> fixs, String... columns) {
        return update(null, data, fixs, columns);
    }
    @Override
    public int update(Object data, String[] fixs, String... columns) {
        return update(null, data, fixs, columns);
    }
    @Override
    public int update(Object data,  String... columns) {
        return update(null, data,  columns);
    }

    @Override
    public int update(boolean async, Object data, List<String> fixs ,String... columns) {
        return update(async, null, data, BeanUtil.merge(fixs, columns));
    }
    @Override
    public int update(boolean async, Object data, String[] fixs ,String... columns) {
        return update(async, null, data, BeanUtil.array2list(fixs, columns));
    }
    @Override
    public int update(boolean async, Object data,String... columns) {
        return update(async, null, data, BeanUtil.array2list(columns));
    }
    @Override
    public int save(boolean async, String dest, Object data, boolean checkParimary, List<String> fixs,  String... columns) {
        if(async){
            final String _dest = dest;
            final Object _data = data;
            final boolean _chk = checkParimary;
            final String[] cols = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
            new Thread(new Runnable(){
                @Override
                public void run() {
                    save(_dest, _data, _chk, cols);
                }

            }).start();
            return 0;
        }else{
            return save(dest, data, checkParimary, columns);
        }

    }

    public int save(boolean async, String dest, Object data, boolean checkParimary, String[] fixs,  String... columns) {
        return save(async, dest, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }
    public int save(boolean async, String dest, Object data, boolean checkParimary,  String... columns) {
        return save(async, dest, data, checkParimary, BeanUtil.array2list(columns));
    }
    @SuppressWarnings("rawtypes")
    @Override
    public int save(String dest, Object data, boolean checkParimary, List<String> fixs, String... columns) {
        if (null == data) {
            return 0;
        }
        columns = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
        if (data instanceof Collection) {
            Collection datas = (Collection) data;
            int cnt = 0;
            for (Object obj : datas) {
                cnt += save(dest, obj, checkParimary, columns);
            }
            return cnt;
        }
        return saveObject(dest, data, checkParimary, columns);
    }

    @Override
    public int save(String dest, Object data, boolean checkParimary, String[] fixs, String... columns) {
        return save(dest, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }
    @Override
    public int save(String dest, Object data, boolean checkParimary, String... columns) {
        return save(dest, data, checkParimary, BeanUtil.array2list(columns));
    }

    @Override
    public int save(Object data, boolean checkParimary, List<String>fixs, String... columns) {
        return save(null, data, checkParimary, BeanUtil.merge(fixs, columns));
    }
    @Override
    public int save(Object data, boolean checkParimary, String[] fixs, String... columns) {
        return save(null, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }
    @Override
    public int save(Object data, boolean checkParimary, String... columns) {
        return save(null, data, checkParimary, columns);
    }

    @Override
    public int save(boolean async, Object data, boolean checkParimary, List<String> fixs, String... columns) {
        return save(async, null, data, checkParimary, fixs, columns);
    }

    @Override
    public int save(boolean async, Object data, boolean checkParimary, String[] fixs, String... columns) {
        return save(async, null, data, checkParimary, fixs, columns);
    }

    @Override
    public int save(boolean async, Object data, boolean checkParimary,  String... columns) {
        return save(async, null, data, checkParimary, columns);
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

    protected int saveObject(String dest, Object data, boolean checkParimary, List<String> fixs, String... columns) {
        if(BasicUtil.isEmpty(dest)) {
            if (data instanceof DataRow || data instanceof DataSet) {
                dest = DataSourceHolder.parseDataSource(dest, data);
            }else{
                if(AdapterProxy.hasAdapter()){
                    dest = AdapterProxy.table(data.getClass());
                }
            }
        }

        return dao.save(dest, data, checkParimary, BeanUtil.list2array(BeanUtil.merge(fixs, columns)));
    }

    protected int saveObject(String dest, Object data, boolean checkParimary, String[] fixs, String... columns) {
        return saveObject(dest, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }
    @Override
    public int insert(String dest, Object data, boolean checkParimary, List<String> fixs, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,data);
        columns = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
        return dao.insert(dest, data, checkParimary, columns);
    }
    @Override
    public int insert(String dest, Object data, boolean checkParimary, String[] fixs, String... columns) {
        return insert(dest, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }
    @Override
    public int insert(String dest, Object data, boolean checkParimary, String... columns) {
        return insert(dest, data, checkParimary, BeanUtil.array2list(columns));
    }


    @Override
    public int insert(Object data, boolean checkParimary, List<String> fixs, String... columns) {
        return insert(null, data, checkParimary, fixs, columns);
    }

    @Override
    public int insert(Object data, boolean checkParimary, String[] fixs, String... columns) {
        return insert(null, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }

    @Override
    public int insert(Object data, boolean checkParimary,  String... columns) {
        return insert(null, data, checkParimary, BeanUtil.array2list(columns));
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
    public int batchInsert(String dest, Object data, boolean checkParimary, List<String> fixs,  String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,data);
        columns = BeanUtil.list2array(BeanUtil.merge(fixs, columns));
        return dao.batchInsert(dest, data, checkParimary, columns);
    }

    @Override
    public int batchInsert(String dest, Object data, boolean checkParimary, String[] fixs,  String... columns) {
        return batchInsert(dest, data, checkParimary, BeanUtil.array2list(fixs, columns));
    }
    @Override
    public int batchInsert(String dest, Object data, boolean checkParimary, String... columns) {
        return batchInsert(dest, data, checkParimary, BeanUtil.array2list(columns));
    }


    @Override
    public int batchInsert(Object data, boolean checkParimary, List<String> fixs, String... columns) {
        return batchInsert(null, data, checkParimary, fixs, columns);
    }
    @Override
    public int batchInsert(Object data, boolean checkParimary, String[] fixs, String... columns) {
        return batchInsert(null, data, checkParimary, fixs, columns);
    }
    @Override
    public int batchInsert(Object data, boolean checkParimary, String... columns) {
        return batchInsert(null, data, checkParimary, columns);
    }

    @Override
    public int batchInsert(Object data, List<String> fixs, String... columns) {
        return batchInsert(null, data, false, fixs, columns);
    }


    @Override
    public int batchInsert(Object data, String[] fixs, String... columns) {
        return batchInsert(null, data, false, fixs, columns);
    }


    @Override
    public int batchInsert(Object data, String... columns) {
        return batchInsert(null, data, false, columns);
    }


    @Override
    public int batchInsert(String dest, Object data, List<String> fixs, String... columns) {
        return batchInsert(dest, data, false, fixs, columns);
    }
    @Override
    public int batchInsert(String dest, Object data, String[] fixs, String... columns) {
        return batchInsert(dest, data, false, fixs, columns);
    }
    @Override
    public int batchInsert(String dest, Object data, String... columns) {
        return batchInsert(dest, data, false, columns);
    }
    @Override
    public boolean executeProcedure(String procedure, String... inputs) {
        Procedure proc = new ProcedureImpl();
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
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
                throw e;
            }
        }
        return set;
    }

    public DataSet querys(Procedure procedure,  String ... inputs) {
        return querys(procedure, null, inputs);
    }
    public DataSet querys(Procedure procedure, int fr, int to, String ... inputs) {
        PageNavi navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        return querys(procedure, navi, inputs);
    }

    @Override
    public DataSet querysProcedure(String procedure, PageNavi navi, String... inputs) {
        Procedure proc = new ProcedureImpl();
        proc.setName(procedure);
        if(null != inputs) {
            for (String input : inputs) {
                proc.addInput(input);
            }
        }
        return querys(proc, navi);
    }
    public DataSet querysProcedure(String procedure, int fr, int to, String... inputs) {
        PageNavi navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        return querysProcedure(procedure, navi, inputs);
    }
    public DataSet querysProcedure(String procedure, String... inputs) {
        return querysProcedure(procedure, null, inputs);
    }
    public DataRow queryProcedure(String procedure, String... inputs) {
        Procedure proc = new ProcedureImpl();
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
        src = BasicUtil.compressionSpace(src);
        src = DataSourceHolder.parseDataSource(src);
        conditions = BasicUtil.compressionSpace(conditions);
        SQL sql = createSQL(src);
        if (null == sql) {
            return result;
        }
        result = dao.execute(sql, store, conditions);
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
    protected DataSet queryFromDao(SQL sql, ConfigStore configs, String... conditions){
        DataSet set = null;
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", sql.getText());
        }
        try {
            setPageLazy(sql.getText(), configs, conditions);
            set = dao.querys(sql, configs, conditions);
         } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
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
            SQL sql = createSQL(src);
            set = dao.querys(sql, configs, conditions);
         } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
                throw e;
            }
        }
        return set;
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
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
                throw e;
            }
        }
        return list;
    }
    /**
     * 解析SQL中指定的主键table(col1,col2)[pk1,pk2]
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

    protected synchronized SQL createSQL(String src){
        SQL sql = null;
        src = src.trim();
        List<String> pks = new ArrayList<>();
        //文本sql
        if (src.startsWith("${") && src.endsWith("}")) {
            if(ConfigTable.isSQLDebug()){
                log.warn("[解析SQL类型] [类型:{JAVA定义}] [src:{}]",src);
            }
            src = src.substring(2,src.length()-1);
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            sql = new TextSQLImpl(src);
        } else {
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            String chk = src.toUpperCase().trim().replace("\t"," ");
            if (chk.startsWith("SELECT ")
                    || chk.startsWith("DELETE ")
                    || chk.startsWith("INSERT ")
                    || chk.startsWith("UPDATE ")
                    || chk.startsWith("TRUNCATE ")
                    || chk.startsWith("CREATE ")
                    || chk.startsWith("ALTER ")
                    || chk.startsWith("DROP ")
                    || chk.startsWith("IF ")
                    || chk.startsWith("CALL ")) {
                if(ConfigTable.isSQLDebug()){
                    log.warn("[解析SQL类型] [类型:JAVA定义] [src:{}]", src);
                }
                sql = new TextSQLImpl(src);
            }else if (RegularUtil.match(src, SQL.XML_SQL_ID_STYLE)) {
                /* XML定义 */
                if(ConfigTable.isSQLDebug()){
                    log.warn("[解析SQL类型] [类型:XML定义] [src:{}]", src);
                }
                sql = SQLStoreImpl.parseSQL(src);
                if(null == sql){
                    log.error("[解析SQL类型][XML解析失败][src:{}]",src);
                }
            } else {
                /* 自动生成 */
                if(ConfigTable.isSQLDebug()){
                    log.warn("[解析SQL类型] [类型:auto] [src:{}]", src);
                }
                sql = new TableSQLImpl();
                sql.setDataSource(src);
            }
        }
        if(null != sql && pks.size()>0){
            sql.setPrimaryKey(pks);
        }
        return sql;
    }
    protected DataSet queryFromCache(String cache, String src, ConfigStore configs, String ... conditions){
        if(ConfigTable.isDebug() && log.isWarnEnabled()){
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
        SQL sql = createSQL(src);
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
//        	//开启新线程提前更新缓存(90%时间)
                long age = (System.currentTimeMillis() - cacheElement.getCreateTime()) / 1000;
                final int _max = cacheElement.getExpires();
                if (age > _max * 0.9) {
                    if (ConfigTable.isDebug() && log.isWarnEnabled()) {
                        log.warn("[缓存即将到期提前刷新][src:{}] [生存:{}/{}]", src, age, _max);
                    }
                    final String _key = key;
                    final String _cache = cache;
                    final SQL _sql = sql;
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
                set = dao.querys(sql, configs, conditions);
                cacheProvider.put(cache, key, set);
            }
        }
        return set;
    }

    private ConfigStore append(ConfigStore configs, Object entity){
        if(null == configs){
            configs = new ConfigStoreImpl();
        }
        if(null != entity) {
            if(entity instanceof Map){
                Map map = (Map)entity;
                for(Object key:map.keySet()){
                    Object value = map.get(key);
                    if (value instanceof Collection) {
                        configs.addConditions(key.toString(), value);
                    } else {
                        configs.addCondition(key.toString(), value);
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
                        if (value instanceof Collection) {
                            configs.addConditions(key, value);
                        } else {
                            configs.addCondition(key, value);
                        }
                    }
                }
            }
        }
        return configs;
    }

    public List<String> tables(String catalog, String schema, String name, String types){
        LinkedHashMap<String,Table> tables = metadata.tables(catalog, schema, name, types);
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
        return tables("TABLE");
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

    public LinkedHashMap<String,Column> columns(String table, boolean map){
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
        LinkedHashMap<String,Tag> tags = metadata.tags(catalog, schema, table);
        List<String> list = new ArrayList<>();
        for(Tag tag:tags.values()){
            list.add(tag.getName());
        }
        return list;
    }
    /**
     * 修改表结构
     * @param table table
     * @throws Exception SQL异常
     */
    public boolean save(Table table) throws Exception{
        return ddl.save(table);
    }
    /**
     * 修改列  名称 数据类型 位置 默认值
     * 执行save前先调用column.update()设置修改后的属性
     * column.update().setName().setDefaultValue().setAfter()....
     * @param column column
     * @throws Exception SQL异常
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



    private static Map<String,DataRow> cache_metadata = new HashMap<>();
    private static Map<String,DataRow> cache_metadatas = new HashMap<>();




    public MetaDataService metadata(){
        return metadata;
    }
    public DDLService ddl(){
        return ddl;
    }

    public void clearColumnCache(String catalog, String schema, String table){
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.getDataSource()+"_COLUMNS_" + table.toUpperCase();
        if(null != cacheProvider && BasicUtil.isNotEmpty(cache) && !"true".equalsIgnoreCase(ConfigTable.getString("CACHE_DISABLED"))) {
            cacheProvider.remove(cache, key);
        }else{
            cache_metadatas.remove(key);
        }
    }
    public void clearTagCache(String catalog, String schema, String table){
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
        String key = DataSourceHolder.getDataSource()+"_TAGS_" + table.toUpperCase();
        if(null != cacheProvider && BasicUtil.isNotEmpty(cache) && !"true".equalsIgnoreCase(ConfigTable.getString("CACHE_DISABLED"))) {
            cacheProvider.remove(cache, key);
        }else{
            cache_metadatas.remove(key);
        }
    }
    /* *****************************************************************************************************************
     *
     * 													metadata
     * =================================================================================================================
     * table			: 表
     * master table		: 主表
     * partition table	: 分区有
     * column			: 列
     * tag				: 标签
     * index			: 索引
     * constraint		: 约束
     *
     ******************************************************************************************************************/
    public MetaDataService metadata = new MetaDataService() {

        /* *****************************************************************************************************************
         * 													table
         * -----------------------------------------------------------------------------------------------------------------
         * public boolean exists(Table table);
         * public LinkedHashMap<String,Table> tables(String catalog, String schema, String name, String types);
         * public LinkedHashMap<String,Table> tables(String schema, String name, String types);
         * public LinkedHashMap<String,Table> tables(String name, String types);
         * public LinkedHashMap<String,Table> tables(String types);
         * public LinkedHashMap<String,Table> tables();
         * public Table table(String catalog, String schema, String name);
         * public Table table(String schema, String name);
         * public Table table(String name);
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
            return tables("TABLE");
        }


        @Override
        public LinkedHashMap<String, Table> tables(String catalog, String schema, String name, String types) {
            return dao.tables(catalog, schema, name, types);
        }

        @Override
        public Table table(String catalog, String schema,String name) {
            Table table = null;
            LinkedHashMap<String, Table> tables = tables(catalog, schema, name,"TABLE");
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
         * public boolean exists(MasterTable table);
         * public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types);
         * public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types);
         * public LinkedHashMap<String, MasterTable> mtables(String name, String types);
         * public LinkedHashMap<String, MasterTable> mtables(String types);
         * public LinkedHashMap<String, MasterTable> mtables();
         * public MasterTable mtable(String catalog, String schema, String name);
         * public MasterTable mtable(String schema, String name);
         * public MasterTable mtable(String name);
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
         * public boolean exists(PartitionTable table);
         * public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types);
         * public LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types);
         * public LinkedHashMap<String, PartitionTable> ptables(String name, String types);
         * public LinkedHashMap<String, PartitionTable> ptables(String types);
         * public LinkedHashMap<String, PartitionTable> ptables();
         * public LinkedHashMap<String, PartitionTable> ptables(MasterTable table);
         * public PartitionTable ptable(String catalog, String schema, String name);
         * public PartitionTable ptable(String schema, String name);
         * public PartitionTable ptable(String name);
         ******************************************************************************************************************/

        @Override
        public boolean exists(PartitionTable table) {
            return false;
        }


        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types) {
            return dao.ptables(catalog, schema, name, types);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types) {
            return ptables(null, schema, name, types);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String name, String types) {
            return ptables(null, null, name, types);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(String types) {
            return ptables(null, types);
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables() {
            return ptables("STABLE");
        }

        @Override
        public LinkedHashMap<String, PartitionTable> ptables(MasterTable table) {
            return dao.ptables(table);
        }

        @Override
        public PartitionTable ptable(String catalog, String schema, String name) {
            LinkedHashMap<String, PartitionTable> tables = ptables(catalog, schema, name, "STABLE");
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
        public PartitionTable ptable(String schema, String name) {
            return ptable(null, schema, name);
        }

        @Override
        public PartitionTable ptable(String name) {
            return ptable(null, null, name);
        }

        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
         * public boolean exists(Column column);
         * public LinkedHashMap<String,Column> columns(Table table);
         * public LinkedHashMap<String,Column> columns(String table);
         * public LinkedHashMap<String,Column> columns(String catalog, String schema, String table);
         ******************************************************************************************************************/
        @Override
        public boolean exists(Column column) {
            try {
                Table table = table(column.getCatalog(), column.getSchema(), column.getTableName());
                if(null != table){
                    if(table.getColumns().containsKey(column.getName())){
                        return true;
                    }
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
            LinkedHashMap<String,Column> columns = null;
            String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
            String key = DataSourceHolder.getDataSource()+"_COLUMNS_" + table.getName().toUpperCase();

            if(null != cacheProvider && BasicUtil.isNotEmpty(cache) && !"true".equalsIgnoreCase(ConfigTable.getString("CACHE_DISABLED"))){
                CacheElement cacheElement = cacheProvider.get(cache, key);
                if(null != cacheElement){
                    columns = (LinkedHashMap<String,Column>) cacheElement.getValue();
                }
                if(null == columns){
                    columns = dao.columns(table);
                    cacheProvider.put(cache, key, columns);
                }
            }else{
                //通过静态变量缓存
                DataRow static_cache = cache_metadatas.get(key);
                if(null != static_cache && (ConfigTable.TABLE_METADATA_CACHE_SECOND <0 || !static_cache.isExpire(ConfigTable.TABLE_METADATA_CACHE_SECOND*1000))) {
                    columns = (LinkedHashMap<String,Column>) static_cache.get("keys");
                }
                if(null == columns){
                    columns = dao.columns(table);
                    static_cache = new DataRow();
                    static_cache.put("keys", columns);
                    cache_metadatas.put(key, static_cache);
                }
            }
            return columns;

        }
        @Override
        public LinkedHashMap<String,Column> columns(String catalog, String schema, String table){
            return columns(new Table(catalog, schema, table));
        }


        /* *****************************************************************************************************************
         * 													tag
         * -----------------------------------------------------------------------------------------------------------------
         * public LinkedHashMap<String,Tag> tags(String catalog, String schema, String table);
         * public LinkedHashMap<String,Tag> tags(String table);
         * public LinkedHashMap<String,Tag> tags(Table table);
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
            String name = table.getName();
            LinkedHashMap<String,Tag> tags = null;
            String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");
            String key = DataSourceHolder.getDataSource()+"_TAGS_" + name.toUpperCase();

            if(null != cacheProvider && BasicUtil.isNotEmpty(cache) && !"true".equalsIgnoreCase(ConfigTable.getString("CACHE_DISABLED"))){
                CacheElement cacheElement = cacheProvider.get(cache, key);
                if(null != cacheElement){
                    tags = (LinkedHashMap<String,Tag>) cacheElement.getValue();
                }
                if(null == tags){
                    tags = dao.tags(table);
                    cacheProvider.put(cache, key, tags);
                }
            }else{
                //通过静态变量缓存
                DataRow static_cache = cache_metadatas.get(key);
                if(null != static_cache && (ConfigTable.TABLE_METADATA_CACHE_SECOND <0 || !static_cache.isExpire(ConfigTable.TABLE_METADATA_CACHE_SECOND*1000))) {
                    tags = (LinkedHashMap<String,Tag>) static_cache.get("keys");
                }
                if(null == tags){
                    tags = dao.tags(table);
                    static_cache = new DataRow();
                    static_cache.put("keys", tags);
                    cache_metadatas.put(key, static_cache);
                }
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
		 * public LinkedHashMap<String,Constraint> constraints(Table table);
		 * public LinkedHashMap<String,Constraint> constraints(String table);
		 * public LinkedHashMap<String,Constraint> constraints(String catalog, String schema, String table);
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
     * partition table	: 分区有
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
		 * public boolean save(Table table) throws Exception;
		 * public boolean create(Table table) throws Exception;
		 * public boolean alter(Table table) throws Exception;
         * public boolean drop(Table table) throws Exception;
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
            table.setService(AnylineServiceImpl.this);
            boolean result =  dao.create(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }
        @Override
        public boolean alter(Table table) throws Exception{
            table.setService(AnylineServiceImpl.this);
            boolean result = dao.alter(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean drop(Table table) throws Exception{
            table.setService(AnylineServiceImpl.this);
            boolean result = dao.drop(table);

            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        /* *****************************************************************************************************************
         * 													master table
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(MasterTable table) throws Exception;
		 * public boolean create(MasterTable table) throws Exception;
		 * public boolean alter(MasterTable table) throws Exception;
         * public boolean drop(MasterTable table) throws Exception;
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
            table.setService(AnylineServiceImpl.this);
            boolean result =  dao.create(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean alter(MasterTable table) throws Exception {
            table.setService(AnylineServiceImpl.this);
            boolean result = dao.alter(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        @Override
        public boolean drop(MasterTable table) throws Exception {
            table.setService(AnylineServiceImpl.this);
            boolean result = dao.drop(table);
            clearColumnCache(table.getCatalog(), table.getSchema(), table.getName());
            return result;
        }

        /* *****************************************************************************************************************
         * 													partition table
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(PartitionTable table) throws Exception;
		 * public boolean create(PartitionTable table) throws Exception;
		 * public boolean alter(PartitionTable table) throws Exception;
         * public boolean drop(PartitionTable table) throws Exception;
         ******************************************************************************************************************/
        @Override
        public boolean save(PartitionTable table) throws Exception {
            return false;
        }

        @Override
        public boolean create(PartitionTable table) throws Exception {
            return false;
        }

        @Override
        public boolean alter(PartitionTable table) throws Exception {
            return false;
        }

        @Override
        public boolean drop(PartitionTable table) throws Exception {
            return false;
        }

        /* *****************************************************************************************************************
         * 													column
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean save(Column column) throws Exception;
		 * public boolean add(Column column) throws Exception;
		 * public boolean alter(Column column) throws Exception;
         * public boolean drop(Column column) throws Exception;
         *
         * private boolean add(LinkedHashMap<String, Column> columns, Column column) throws Exception
         * private boolean alter(Table table, Column column) throws Exception
         ******************************************************************************************************************/

        /**
         * 修改列  名称 数据类型 位置 默认值
         * 执行save前先调用column.update()设置修改后的属性
         * column.update().setName().setDefaultValue().setAfter()....
         * @param column column
         * @throws Exception SQL异常
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
            column.setService(AnylineServiceImpl.this);
            boolean result = dao.drop(column);
            clearColumnCache(column.getCatalog(), column.getSchema(), column.getTableName());
            return result;
        }

        private boolean add(LinkedHashMap<String, Column> columns, Column column) throws Exception{
            column.setService(AnylineServiceImpl.this);
            boolean result =  dao.add(column);
            if(result) {
                columns.put(column.getName(), column);
            }
            return result;
        }
        /**
         * 修改列
         * @param table table
         * @param column 修改目标
         * @return boolean
         * @throws Exception sql异常
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
            original.setService(AnylineServiceImpl.this);
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
		 * public boolean save(Tag tag) throws Exception;
		 * public boolean add(Tag tag) throws Exception;
		 * public boolean alter(Tag tag) throws Exception;
         * public boolean drop(Tag tag) throws Exception;
         *
         * private boolean add(LinkedHashMap<String, Tag> tags, Tag tag) throws Exception
         * private boolean alter(Table table, Tag tag) throws Exception
         ******************************************************************************************************************/

        /**
         * 修改列  名称 数据类型 位置 默认值
         * 执行save前先调用tag.update()设置修改后的属性
         * tag.update().setName()
         * @param tag tag
         * @throws Exception SQL异常
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
            tag.setService(AnylineServiceImpl.this);
            boolean result = dao.drop(tag);
            clearTagCache(tag.getCatalog(), tag.getSchema(), tag.getTableName());
            return result;
        }

        private boolean add(LinkedHashMap<String, Tag> tags, Tag tag) throws Exception{
            tag.setService(AnylineServiceImpl.this);
            boolean result =  dao.add(tag);
            if(result) {
                tags.put(tag.getName(), tag);
            }
            return result;
        }
        /**
         * 修改标签
         * @param table table
         * @param tag 修改目标
         * @return boolean
         * @throws Exception sql异常
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
            original.setService(AnylineServiceImpl.this);
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
		 * public boolean add(Index index) throws Exception;
		 * public boolean alter(Index index) throws Exception;
         * public boolean drop(Index index) throws Exception;
         ******************************************************************************************************************/

        @Override
        public boolean add(Index index) throws Exception{
            index.setService(AnylineServiceImpl.this);
            return false;
        }

        @Override
        public boolean alter(Index index) throws Exception {
            return false;
        }

        public boolean drop(Index index) throws Exception{
            return false;
        }
        /* *****************************************************************************************************************
         * 													constraint
         * -----------------------------------------------------------------------------------------------------------------
		 * public boolean add(Constraint constraint) throws Exception;
		 * public boolean alter(Constraint constraint) throws Exception;
         * public boolean drop(Constraint constraint) throws Exception;
         ******************************************************************************************************************/
        @Override
        public boolean add(Constraint constraint) throws Exception {
            return false;
        }

        @Override
        public boolean alter(Constraint constraint) throws Exception {
            return false;
        }

        @Override
        public boolean drop(Constraint constraint) throws Exception {
            return false;
        }


    };





}
