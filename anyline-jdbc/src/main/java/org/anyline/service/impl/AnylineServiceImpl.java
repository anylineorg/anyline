/*
 * Copyright 2006-2020 www.anyline.org
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
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.MetaData;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Order;
import org.anyline.jdbc.config.db.OrderStore;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.SQL.ORDER_TYPE;
import org.anyline.jdbc.config.db.impl.PageNaviImpl;
import org.anyline.jdbc.config.db.impl.ProcedureImpl;
import org.anyline.jdbc.config.db.impl.SQLStoreImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.jdbc.config.db.sql.auto.impl.TextSQLImpl;
import org.anyline.jdbc.config.impl.ConfigStoreImpl;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service("anyline.service")
public class AnylineServiceImpl implements AnylineService {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired(required = false)
    @Qualifier("anyline.dao")
    protected AnylineDao dao;

    @Autowired(required = false)
    @Qualifier("anyline.cache.provider")
    protected CacheProvider cacheProvider;


    @Override
    public DataSet selects(String src, ConfigStore configs, String... conditions) {
        return querys(src, configs, conditions);
    }
    @Override
    public DataSet selects(String src, String... conditions) {
        return querys(src, conditions);
    }
    @Override
    public DataSet selects(String src, int fr, int to, String... conditions) {
        return querys(src, fr, to, conditions);
    }
    @Override
    public DataRow select(String src, ConfigStore configs, String... conditions) {
        return query(src, configs, conditions);
    }
    @Override
    public DataRow select(String src, String... conditions) {
        return query(src, conditions);
    }
    @Override
    public DataSet selectProcedure(String procedure, String... inputs) {
        return queryProcedure(procedure, inputs);
    }
    @Override
    public DataSet select(Procedure procedure, String ... inputs) {
        return query(procedure, inputs);
    }
    /**
     * 按条件查询
     * @param src 表｜视图｜函数｜自定义SQL
     * @param configs http参数封装
     * @param conditions 固定查询条件
     * @return return
     */
    @Override
    public DataSet querys(String src, ConfigStore configs, String... conditions) {
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        DataSet set = queryFromDao(src, configs, conditions);
        return set;
    }

    @Override
    public DataSet querys(String src, String... conditions) {
        return querys(src, null, conditions);
    }


    @Override
    public DataSet querys(String src, int fr, int to, String... conditions) {
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        navi.setCalType(1);
        navi.setTotalRow(to-fr+1);
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return querys(src, configs, conditions);
    }

    public List<String> metadata(String table){
        List<String> list = null;
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");

        if(null != cacheProvider && BasicUtil.isNotEmpty(cache)){
            String key = "METADATA_" + table;
            CacheElement cacheElement = cacheProvider.get(cache, key);
            if(null != cacheElement){
                list = (List<String>) cacheElement.getValue();
            }
            if(null == list){
                list = dao.metadata(table);
                cacheProvider.put(cache, key, list);
            }
        }else{
            list = dao.metadata(table);
        }
        return list;
    }
    public List<MetaData> metadatas(String table){
        List<MetaData> list = null;
        String cache = ConfigTable.getString("TABLE_METADATA_CACHE_KEY");

        if(null != cacheProvider && BasicUtil.isNotEmpty(cache)){
            String key = "METADATAS_" + table;
            CacheElement cacheElement = cacheProvider.get(cache, key);
            if(null != cacheElement){
                list = (List<MetaData>) cacheElement.getValue();
            }
            if(null == list){
                list = dao.metadatas(table);
                cacheProvider.put(cache, key, list);
            }
        }else{
            list = dao.metadatas(table);
        }
        return list;
    }
    @Override
    public List<Map<String,Object>> maps(String src, String... conditions) {
        return maps(src, null, conditions);
    }

    @Override
    public List<Map<String,Object>> maps(String src, ConfigStore configs, String... conditions) {
        List<Map<String,Object>> maps = null;
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        if(ConfigTable.isSQLDebug()){
            log.warn("[解析SQL][src:{}]", src);
        }
        try {
            SQL sql = createSQL(src);
            maps = dao.maps(sql, configs, conditions);
        } catch (Exception e) {
            maps = new ArrayList<Map<String,Object>>();
            if(ConfigTable.isDebug() && log.isWarnEnabled()){
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
    public DataSet caches(String cache, String src, ConfigStore configs, String ... conditions){
        DataSet set = null;
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        if(null == cache || "true".equalsIgnoreCase(ConfigTable.getString("CACHE_DISABLED"))){
            set = querys(src, configs, conditions);
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
    public DataSet caches(String cache, String src, String ... conditions){
        return caches(cache, src, null, conditions);
    }
    @Override
    public DataSet caches(String cache, String src, int fr, int to, String ... conditions){
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        navi.setCalType(1);
        navi.setTotalRow(to-fr+1);
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return caches(cache, src, configs, conditions);
    }

    @Override
    public DataRow query(String src, ConfigStore store, String... conditions) {
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new ConfigStoreImpl();
        }
        store.setPageNavi(navi);
        DataSet set = querys(src, store, conditions);
        if (null != set && set.size() > 0) {
            DataRow row = set.getRow(0);
            return row;
        }
        return null;
    }


    @Override
    public DataRow query(String src, String... conditions) {
        return query(src, null, conditions);
    }

    @Override
    public DataRow cache(String cache, String src, ConfigStore configs, String ... conditions){
        //是否启动缓存
        if(null == cache){
            return query(src, configs, conditions);
        }
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new ConfigStoreImpl();
        }
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
        row = query(src, configs, conditions);
        if(null != row && null != cacheProvider){
            cacheProvider.put(cache, key, row);
        }
        return row;
    }
    @Override
    public DataRow cache(String cache, String src, String ... conditions){
        return cache(cache, src, null, conditions);
    }

    /*多表查询，左右连接*/

    @Override
    public DataSet selects(SQL table, ConfigStore configs, String... conditions) {
        return querys(table, configs, conditions);
    }
    @Override
    public DataSet selects(SQL table, String... conditions) {
        return querys(table, conditions);
    }
    @Override
    public DataSet selects(SQL table, int fr, int to, String... conditions) {
        return querys(table, fr, to, conditions);
    }
    @Override
    public DataRow select(SQL table, ConfigStore configs, String... conditions) {
        return query(table, configs, conditions);
    }
    @Override
    public DataRow select(SQL table, String... conditions) {
        return query(table, conditions);
    }

    /**
     * 按条件查询
     * @param sql 表｜视图｜函数｜自定义SQL |SQL
     * @param configs http参数封装
     * @param conditions 固定查询条件
     * @return return
     */
    @Override
    public DataSet querys(SQL sql, ConfigStore configs, String... conditions) {
        conditions = BasicUtil.compressionSpace(conditions);
        DataSet set = queryFromDao(sql, configs, conditions);
        return set;

    }

    @Override
    public DataSet querys(SQL sql, String... conditions) {
        return querys(sql, null, conditions);
    }


    @Override
    public DataSet querys(SQL sql, int fr, int to, String... conditions) {
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        navi.setCalType(1);
        navi.setTotalRow(to-fr+1);
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return querys(sql, configs, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, ConfigStore configs, String ... conditions){
        DataSet set = null;
        conditions = BasicUtil.compressionSpace(conditions);
        if(null == cache){
            set = querys(table, configs, conditions);
        }else{
            if(null != cacheProvider){
               // set = queryFromCache(cache, table, configs, conditions);
            }else{
                set = querys(table, configs, conditions);
            }
        }
        return set;
    }
    @Override
    public DataSet caches(String cache, SQL table, String ... conditions){
        return caches(cache, table, null, conditions);
    }
    @Override
    public DataSet caches(String cache, SQL table, int fr, int to, String ... conditions){
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        navi.setCalType(1);
        navi.setTotalRow(to-fr+1);
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return caches(cache, table, configs, conditions);
    }

    @Override
    public DataRow query(SQL table, ConfigStore store, String... conditions) {
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == store) {
            store = new ConfigStoreImpl();
        }
        store.setPageNavi(navi);
        DataSet set = querys(table, store, conditions);
        if (null != set && set.size() > 0) {
            DataRow row = set.getRow(0);
            return row;
        }
        return null;
    }


    @Override
    public DataRow query(SQL table, String... conditions) {
        return query(table, null, conditions);
    }

    @Override
    public DataRow cache(String cache, SQL table, ConfigStore configs, String ... conditions){
        //是否启动缓存
        if(null == cache){
            return query(table, configs, conditions);
        }
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(0);
        navi.setLastRow(0);
        navi.setCalType(1);
        if (null == configs) {
            configs = new ConfigStoreImpl();
        }
        configs.setPageNavi(navi);

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
        return row;
    }
    @Override
    public DataRow cache(String cache, SQL table, String ... conditions){
        return cache(cache, table, null, conditions);
    }

    @Override
    public DataRow next(DataRow row, String column, SQL.ORDER_TYPE order, ConfigStore configs, String ... conditions) {
        //查询条件
        if(null == configs){
            configs = (ConfigStore)row.getQueryParam("query_config");
        }
        if(null == configs){
            configs = new ConfigStoreImpl();
        }
        if(null == conditions){
            conditions = (String[])row.getQueryParam("query_condition");
        }
        //排序列
        if(BasicUtil.isEmpty(column)){
            OrderStore orderStore = (OrderStore)row.getQueryParam("query_order");
            if(null != orderStore){
                List<Order> orders = orderStore.getOrders();
                if(null != orders && orders.size()>0){
                    Order od = orders.get(0);
                    if(null == order){
                        order = od.getType();
                    }
                    column = od.getColumn();
                }
            }
        }
        if(BasicUtil.isEmpty(column)){
            column = row.getPrimaryKey();
        }
        if(null == order){
            order = SQL.ORDER_TYPE.DESC;
        }
        String src = (String)row.getQueryParam("query_src");

        SQL.ORDER_TYPE queryOrder = null;
        String pk = row.getPrimaryKey();
        Object pv = row.getPrimaryValue();
        SQL.COMPARE_TYPE compare = null;
        configs.removeConfig(pk);
        if(BasicUtil.isEmpty(pk) || BasicUtil.isEmpty(pv) || column.equalsIgnoreCase(pk)){
            if(order == SQL.ORDER_TYPE.DESC){
                compare = SQL.COMPARE_TYPE.LESS;
                queryOrder = SQL.ORDER_TYPE.DESC;
            }else{
                compare = SQL.COMPARE_TYPE.GREAT;
                queryOrder = SQL.ORDER_TYPE.ASC;
            }
        }else{
            configs.addCondition(SQL.COMPARE_TYPE.NOT_EQUAL, pk, pv, true, true);
            if(order == SQL.ORDER_TYPE.DESC){
                compare = SQL.COMPARE_TYPE.LESS_EQUAL;
                queryOrder = SQL.ORDER_TYPE.DESC;
            }else{
                compare = SQL.COMPARE_TYPE.GREAT_EQUAL;
                queryOrder = SQL.ORDER_TYPE.ASC;
            }
        }
        configs.order(column, queryOrder.getCode());
        configs.removeConfig(column);
        configs.addCondition(compare, column, row.get(column), true, true);
        return query(src, configs, conditions);
    }

    @Override
    public DataRow next(DataRow row, String column, ORDER_TYPE order, String... conditions) {
        return next(row, column, order, null, conditions);
    }
    @Override
    public DataRow next(DataRow row, ORDER_TYPE order, String... conditions) {
        return next(row, null, order, null, conditions);
    }
    @Override
    public DataRow next(DataRow row,  String... conditions) {
        return next(row, null, null, null, conditions);
    }

    @Override
    public DataRow next(DataRow row, ConfigStore configs, String... conditions) {
        return next(row, null, null, configs, conditions);
    }
    @Override
    public DataRow prev(DataRow row, String column, SQL.ORDER_TYPE order, ConfigStore configs, String ... conditions) {
        //查询条件
        if(null == configs){
            configs = (ConfigStore)row.getQueryParam("query_config");
        }
        if(null == configs){
            configs = new ConfigStoreImpl();
        }
        if(null == conditions){
            conditions = (String[])row.getQueryParam("query_condition");
        }
        //排序列
        if(BasicUtil.isEmpty(column)){
            OrderStore orderStore = (OrderStore)row.getQueryParam("query_order");
            if(null != orderStore){
                List<Order> orders = orderStore.getOrders();
                if(null != orders && orders.size()>0){
                    Order od = orders.get(0);
                    if(null == order){
                        order = od.getType();
                    }
                    column = od.getColumn();
                }
            }
        }
        if(BasicUtil.isEmpty(column)){
            column = row.getPrimaryKey();
        }
        if(null == order){
            order = SQL.ORDER_TYPE.DESC;
        }
        String src = (String)row.getQueryParam("query_src");

        SQL.ORDER_TYPE queryOrder = null;
        String pk = row.getPrimaryKey();
        Object pv = row.getPrimaryValue();
        SQL.COMPARE_TYPE compare = null;
        configs.removeConfig(pk);
        if(BasicUtil.isEmpty(pk) || BasicUtil.isEmpty(pv) || column.equalsIgnoreCase(pk)){
            if(order == SQL.ORDER_TYPE.ASC){
                compare = SQL.COMPARE_TYPE.LESS;
                queryOrder = SQL.ORDER_TYPE.DESC;
            }else{
                compare = SQL.COMPARE_TYPE.GREAT;
                queryOrder = SQL.ORDER_TYPE.ASC;
            }
        }else{
            configs.addCondition(SQL.COMPARE_TYPE.NOT_EQUAL, pk, pv, true, true);
            if(order == SQL.ORDER_TYPE.ASC){
                compare = SQL.COMPARE_TYPE.LESS_EQUAL;
                queryOrder = SQL.ORDER_TYPE.DESC;
            }else{
                compare = SQL.COMPARE_TYPE.GREAT_EQUAL;
                queryOrder = SQL.ORDER_TYPE.ASC;
            }
        }
        configs.order(column, queryOrder.getCode());
        configs.removeConfig(column);
        configs.addCondition(compare, column, row.get(column), true, true);
        return query(src, configs, conditions);
    }

    @Override
    public DataRow prev(DataRow row, String column, ORDER_TYPE order, String... conditions) {
        return prev(row, column, order, null, conditions);
    }
    @Override
    public DataRow prev(DataRow row, ORDER_TYPE order, String... conditions) {
        return prev(row, null, order, null, conditions);
    }
    @Override
    public DataRow prev(DataRow row, String... conditions) {
        return prev(row, null, null, null, conditions);
    }
    @Override
    public DataRow prev(DataRow row, ConfigStore configs, String... conditions) {
        return prev(row, null, null, configs, conditions);
    }


    /**
     * 删除缓存 参数保持与查询参数完全一致
     * @param channel  channel
     * @param src  src
     * @param configs  configs
     * @param conditions  conditions
     * @return return
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
        PageNaviImpl navi = new PageNaviImpl();
        navi.setFirstRow(fr);
        navi.setLastRow(to);
        navi.setCalType(1);
        navi.setTotalRow(to-fr+1);
        ConfigStore configs = new ConfigStoreImpl();
        configs.setPageNavi(navi);
        return removeCache(channel, src, configs, conditions);
    }
    /**
     * 清空缓存
     * @param channel  channel
     * @return return
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
     * @param configs  configs
     * @param conditions  conditions
     * @return return
     */

    @Override
    public boolean exists(String src, ConfigStore configs, String ... conditions){
        boolean result = false;
        src = BasicUtil.compressionSpace(src);
        conditions = BasicUtil.compressionSpace(conditions);
        SQL sql = createSQL(src);
        result = dao.exists(sql, configs, conditions);
        return result;
    }
    @Override
    public boolean exists(String src, String ... conditions){
        return exists(src, null, conditions);
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
    public int count(String src, ConfigStore configs, String ... conditions){
        int count = -1;
        try {
            //conditions = parseConditions(conditions);
            src = BasicUtil.compressionSpace(src);
            conditions = BasicUtil.compressionSpace(conditions);
            SQL sql = createSQL(src);
            count = dao.count(sql, configs, conditions);
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
    public int count(String src, String ... conditions){
        return count(src, null, conditions);
    }


    /**
     * 更新记录
     *
     * @param sync  是否异步
     * @param dest  dest
     * @param data  需要更新的数据
     * @param columns 需要更新的列
     * @return return
     */
    @Override
    public int update(boolean sync, String dest, Object data, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,dest);
        final String cols[] = BasicUtil.compressionSpace(columns);
        final String _dest = BasicUtil.compressionSpace(dest);
        final Object _data = data;
        if(sync){
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

    @Override
    public int update(String dest, ConfigStore configs, String... conditions) {
        return 0;
    }
    @Override
    public int update(String dest, Object data, String... columns) {
        dest = BasicUtil.compressionSpace(dest);
        dest = DataSourceHolder.parseDataSource(dest,data);
        columns = BasicUtil.compressionSpace(columns);
        return dao.update(dest, data, columns);
    }


    @Override
    public int update(Object data, String... columns) {
        return update(null, data, columns);
    }
    @Override
    public int update(boolean sync, Object data, String... columns) {
        return update(sync, null, data, columns);
    }
    @Override
    public int save(boolean sync, String dest, Object data, boolean checkParimary, String... columns) {
        if(sync){
            final String _dest = dest;
            final Object _data = data;
            final boolean _chk = checkParimary;
            final String[] cols = columns;
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
    @SuppressWarnings("rawtypes")
    @Override
    public int save(String dest, Object data, boolean checkParimary, String... columns) {
        if (null == data) {
            return 0;
        }
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
    public int save(Object data, boolean checkParimary, String... columns) {
        return save(null, data, checkParimary, columns);
    }
    @Override
    public int save(boolean sync, Object data, boolean checkParimary, String... columns) {
        return save(sync, null, data, checkParimary, columns);
    }


    @Override
    public int save(Object data, String... columns) {
        return save(null, data, false, columns);
    }

    @Override
    public int save(boolean sync, Object data, String... columns) {
        return save(sync, null, data, false, columns);
    }

    @Override
    public int save(String dest, Object data, String... columns) {
        return save(dest, data, false, columns);
    }

    @Override
    public int save(boolean sync, String dest, Object data, String... columns) {
        return save(sync, dest, data, false, columns);
    }

    protected int saveObject(String dest, Object data, boolean checkParimary, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,data);
        return dao.save(dest, data, checkParimary, columns);
    }

    @Override
    public int insert(String dest, Object data, boolean checkParimary, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,data);
        return dao.insert(dest, data, checkParimary, columns);
    }


    @Override
    public int insert(Object data, boolean checkParimary, String... columns) {
        return insert(null, data, checkParimary, columns);
    }


    @Override
    public int insert(Object data, String... columns) {
        return insert(null, data, false, columns);
    }



    @Override
    public int insert(String dest, Object data, String... columns) {
        return insert(dest, data, false, columns);
    }

    @Override
    public int batchInsert(String dest, Object data, boolean checkParimary, String... columns) {
        dest = DataSourceHolder.parseDataSource(dest,data);
        return dao.batchInsert(dest, data, checkParimary, columns);
    }


    @Override
    public int batchInsert(Object data, boolean checkParimary, String... columns) {
        return batchInsert(null, data, checkParimary, columns);
    }

    @Override
    public int batchInsert(Object data, String... columns) {
        return batchInsert(null, data, false, columns);
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
     * @return return
     */
    @Override
    public DataSet query(Procedure procedure, String ... inputs) {
        DataSet set = null;
        try {
            procedure.setName(DataSourceHolder.parseDataSource(procedure.getName()));
            if(null != inputs){
                for(String input:inputs){
                    procedure.addInput(input);
                }
            }

            set = dao.query(procedure);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.isDebug() && log.isWarnEnabled()){
                e.printStackTrace();
            }
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
                throw e;
            }
        }
        return set;
    }


    @Override
    public DataSet queryProcedure(String procedure, String... inputs) {
        Procedure proc = new ProcedureImpl();
        proc.setName(procedure);
        if(null != inputs) {
            for (String input : inputs) {
                proc.addInput(input);
            }
        }
        return query(proc);
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
    public int delete(DataRow row, String ... columns) {
        String dest = DataSourceHolder.parseDataSource(null,row);
        return dao.delete(dest, row, columns);
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
        //conditions = parseConditions(conditions);
        try {
            setPageLazy(sql.getText(), configs, conditions);
            set = dao.querys(sql, configs, conditions);
            set.addQueryParam("query_src", sql.getText());
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(ConfigTable.isDebug() && log.isWarnEnabled()){
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
        //conditions = parseConditions(conditions);
        try {
            setPageLazy(src, configs, conditions);
            SQL sql = createSQL(src);
            set = dao.querys(sql, configs, conditions);
            set.addQueryParam("query_src", src);
        } catch (Exception e) {
            set = new DataSet();
            set.setException(e);
            if(ConfigTable.isDebug() && log.isWarnEnabled()){
                e.printStackTrace();
            }
            log.error("QUERY ERROR:"+e);
            if(ConfigTable.IS_THROW_SQL_EXCEPTION){
                throw e;
            }
        }
        return set;
    }
    /**
     * 解析SQL中指定的主键table(col1,col2)[pk1,pk2]
     * @param src  src
     * @param pks  pks
     * @return return
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
        List<String> pks = new ArrayList<String>();
        //文本sql
        if (src.startsWith("{") && src.endsWith("}")) {
            if(ConfigTable.isSQLDebug()){
                log.warn("[解析SQL类型] [类型:{JAVA定义}] [src:{}]",src);
            }
            src = src.substring(1,src.length()-1);
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            sql = new TextSQLImpl(src);
        } else {
            src = DataSourceHolder.parseDataSource(src);//解析数据源
            src = parsePrimaryKey(src, pks);//解析主键
            if (src.toUpperCase().trim().startsWith("SELECT")
                    || src.toUpperCase().trim().startsWith("DELETE")
                    || src.toUpperCase().trim().startsWith("INSERT")
                    || src.toUpperCase().trim().startsWith("UPDATE")) {
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
}
