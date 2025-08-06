/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.mongodb.adapter;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.anyline.adapter.EntityAdapter;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.mongodb.entity.MongoRow;
import org.anyline.data.mongodb.run.MongoRun;
import org.anyline.data.mongodb.runtime.MongoRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.CommandQueryException;
import org.anyline.exception.CommandUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.lang.reflect.Field;
import java.util.*;

@AnylineComponent("anyline.data.adapter.mongo")
public class MongoAdapter extends AbstractDriverAdapter implements DriverAdapter {

    @Override
    public DatabaseType type() {
        return DatabaseType.MongoDB;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return false;
    }

    private static Map<Type, String> types = new HashMap<>();
    static {
        types.put(Table.TYPE.NORMAL, "collection");
        types.put(Metadata.TYPE.TABLE, "collection");
    }

    @Override
    public String name(Type type) {
        return types.get(type);
    }

    @Override
    public Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj,  Boolean placeholder, Boolean unicode, String... conditions) {
        return null;
    }

    @Override
    public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return createInsertRun(runtime, dest, obj, configs, placeholder, unicode, columns);
    }

	/**
     * 根据entity创建 INSERT RunPrepare
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 数据
     * @param columns 需要插入的列
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */

    @Override
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = new MongoRun(runtime, dest);
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        if(null != generator) {
            Object pv = BeanUtil.getFieldValue(obj, "_id", true);
            if(null == pv) {
                List<String> pk = new ArrayList<>();
                pk.add("_id");
                generator.create(obj, DatabaseType.MongoDB, dest.getName(), pk, null);
            }
        }
        run.setValue(obj);
        return run;
    }

	/**
     * 根据collection创建 INSERT RunPrepare
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 对象集合
     * @param columns 需要插入的列, 如果不指定则全部插入
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */

    @Override
    protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = new MongoRun(runtime, dest);
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        if(null != generator) {
            List<String> pk = new ArrayList<>();
            pk.add("_id");
            for (Object item : list) {
                Object pv = BeanUtil.getFieldValue(item, "_id", true);
                if(null != pv) {
                    break;
                }
                generator.create(item, DatabaseType.MongoDB, dest.getName(), pk, null);
            }
        }
        run.setValue(list);
        return run;
    }

    /**
     * insert [命令执行]
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param data entity|DataRow|DataSet
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param pks pks
     * @return int 影响行数
     */

    @Override
    public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks) {
        long cnt = 0;
        Object value = run.getValue();
        String collection = run.getTableName();
        if(null == value) {
            if(ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                log.warn("[valid:false][action:insert][collection:{}][不具备执行条件]", run.getTableName());
            }
            return -1;
        }
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        long fr = System.currentTimeMillis();
        try {
            MongoCollection cons = null;
            if(value instanceof List) {
                List list = (List) value;
                cons = database.getCollection(run.getTableName(), list.get(0).getClass());
                cnt = list.size();
                InsertManyResult result =  cons.insertMany(list);
                Map<Integer, BsonValue> ids = result.getInsertedIds();
                int idx = 0;
                for(Object item:list){
                    BeanUtil.setFieldValue(item, "_id", value(ids.get(idx++)));
                }
            }else if(value instanceof DataSet) {
                DataSet set = (DataSet)value;
                cons = database.getCollection(run.getTableName(), ConfigTable.DEFAULT_MONGO_ENTITY_CLASS);
                InsertManyResult result =  cons.insertMany(set.getRows());
                cnt = set.size();
                Map<Integer, BsonValue> ids = result.getInsertedIds();
                int idx = 0;
                for(DataRow row:set){
                    row.setPrimaryValue(value(ids.get(idx++)));
                }
            }else if(value instanceof EntitySet) {
                List<Object> datas = ((EntitySet)value).getDatas();
                cons = database.getCollection(run.getTableName(), datas.get(0).getClass());
                InsertManyResult result =  cons.insertMany(datas);
                cnt = datas.size();
                Map<Integer, BsonValue> ids = result.getInsertedIds();
                int idx = 0;
                for(Object item:datas){
                    BeanUtil.setFieldValue(item, "_id", value(ids.get(idx++)));
                }
            }else if(value instanceof Collection) {
                Collection items = (Collection) value;
                List<Object> list = new ArrayList<>();
                for(Object item:items) {
                    list.add(item);
                    cnt ++;
                }
                cons = database.getCollection(run.getTableName(), list.get(0).getClass());
                InsertManyResult result =  cons.insertMany(list);
                Map<Integer, BsonValue> ids = result.getInsertedIds();
                int idx = 0;
                for(Object item:items){
                    BeanUtil.setFieldValue(item, "_id", value(ids.get(idx++)));
                }
            }else{
                cons = database.getCollection(run.getTableName(), value.getClass());
                InsertOneResult result = cons.insertOne(value);
                BeanUtil.setFieldValue(value, "_id", value(result.getInsertedId()));
                cnt = 1;
            }

            long millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[slow cmd][action:insert][collection:{}][执行耗时:{}][collection:{}]", random, run.getTableName(), DateUtil.format(millis), collection);
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.INSERT, run, null, null, null, true, cnt, millis);
                    }
                }
            }
            if (!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[action:insert][collection:{}][执行耗时:{}][影响行数:{}]", random, run.getTableName(), DateUtil.format(millis), LogUtil.format(cnt, 34));
            }
        }catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("insert 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
                CommandUpdateException ex = new CommandUpdateException("insert异常:" + e, e);
                throw ex;
            }else{
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][collection:{}][param:{}]", random, LogUtil.format("插入异常:", 33)+e, run.getTableName(), BeanUtil.object2json(data));
                }
            }
        }
        return cnt;
    }
    private Object value(BsonValue value){
        if(null != value){
            if(value.isObjectId()){
                return value.asObjectId().getValue();
            }else if(value.isString()){
                return value.asString().getValue();
            }else if(value.isInt64()){
                return value.asInt64().getValue();
            }else if(value.isInt32()){
                return value.asInt32().getValue();
            }else if(value.isDecimal128()){
                return value.asDecimal128().getValue();
            }else if(value.isNumber()){
                return value.asNumber().longValue();
            }else if(value.isNull()){
                return null;
            }
        }
        return value;
    }
    /**
     * 过滤掉表结构中不存在的列
     * MONGO不检测
     * @param table 表
     * @param columns columns
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return columns;
    }

    /**
     * 创建查询SQL
     * @param prepare  构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */

    @Override
    public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        MongoRun run = null;
        if(prepare instanceof TablePrepare) {
            run = new MongoRun(runtime, prepare.getTableName());
        }else{
            throw new RuntimeException("不支持查询的类型");
        }
        run.setRuntime(runtime);
        //如果是text类型 将解析文本并抽取出变量
        run.setPrepare(prepare);
        run.setConfigStore(configs);
        run.addCondition(conditions);
        if(run.checkValid()) {
            //为变量赋值
            run.init();
            //构造最终的查询SQL
            fillQueryContent(runtime, run, placeholder, unicode);
        }
        return run;
    }

    @Override
    public Object createConditionJsonContains(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode) {
        return null;
    }

    @Override
    protected Run fillQueryContent(DataRuntime runtime, TableRun run, Boolean placeholder, Boolean unicode) {
        MongoRun r = (MongoRun)run;
        Bson bson = null;
        ConditionChain chain = r.getConditionChain();
        bson = parseCondition(bson, chain);
        r.setFilter(bson);

        List<String> excludeColumns = r.getExcludeColumns();
        if(null == excludeColumns || excludeColumns.isEmpty()) {
            ConfigStore configs = r.getConfigs();
            if(null != configs) {
                excludeColumns = configs.excludes();
            }
        }
        if(null != excludeColumns && !excludeColumns.isEmpty()) {
            r.setExcludeColumns(excludeColumns);
        }

        List<String> queryColumns = r.getQueryColumns();
        if(null == queryColumns || queryColumns.isEmpty()) {
            ConfigStore configs = r.getConfigs();
            if(null != configs) {
                queryColumns = configs.columns();
            }
        }
        if(null == queryColumns || queryColumns.isEmpty()) {
            RunPrepare prepare = run.getPrepare();
            if(null != prepare) {
                LinkedHashMap<String, Column> columns = prepare.getColumns();
                queryColumns = Column.names(columns);
            }
        }

        if(null != queryColumns && !queryColumns.isEmpty()) {
            r.setQueryColumns(queryColumns);
        }
        return r;
    }
    private Bson parseCondition(Bson bson, Condition condition) {
        if(condition instanceof ConditionChain) {
            ConditionChain chain = (ConditionChain)condition;
            bson = parseCondition(bson, chain);
        }else{
            if(condition instanceof AutoCondition) {
                AutoCondition auto = (AutoCondition)condition;
                //List<RunValue> values = condition.getRunValues();
                List<Object> values = auto.getValues();
                String column = condition.getId();
                Condition.JOIN join = condition.getJoin();
                Bson create = bson(auto.getCompare(), column, values);
                if(null != create) {
                    if (Condition.JOIN.OR == join) {
                        bson = Filters.or(create);
                    } else {
                        bson = Filters.and(create);
                    }
                }
            }
        }
        return bson;
    }

    private Bson parseCondition(Bson bson, ConditionChain chain) {
        Condition.JOIN join = chain.getJoin();
        Bson child = null;
        List<Condition> conditions = chain.getConditions();
        for(Condition con:conditions) {
            child = parseCondition(null, con);
            if(null == bson) {
                bson = child;
            }else {
                if(null != child) {
                    if (Condition.JOIN.OR == join) {
                        bson = Filters.or(bson, child);
                    } else {
                        bson = Filters.and(bson, child);
                    }
                }
            }
        }
        return bson;
    }
    private Object id(Object value){
        if(value instanceof Collection){
            Collection list = (Collection)value;
            List<Object> values = new ArrayList<>();
            for(Object item:list){
                values.add(id(item));
            }
            return values;
        }else {
            if (value instanceof String) {
                String str = (String)value;
                if(str.length() == 24){
                    try {
                        return new ObjectId(str);
                    }catch (Exception ignored){}
                }
                if (BasicUtil.isNumber(str)) {
                    try {
                        return BasicUtil.parseLong(str);
                    }catch (Exception ignored){}
                }
            } else if (value instanceof Date) {
                return new ObjectId((Date) value);
            } else if (value instanceof byte[]) {
                return new ObjectId((byte[]) value);
            }
        }
        return value;
    }
    private Bson bson(Compare compare, String column, List<Object> values) {
        Bson bson = null;
        if(null != values && !values.isEmpty()) {
            Object value = null;
            if (compare.valueCount() >1) {
                List<Object> list = new ArrayList<>();

                /*  for (RunValue rv : values) {
                    list.add(rv.getValue());
                } */

                list.addAll(values);
                value = list;
            } else {
                value = values.get(0);
            }

            if("_id".equals(column)){
                value = id(value);
            }
            int cc = compare.getCode();
            if(cc == 10) {                                           //  EQUAL
                bson = Filters.eq(column, value);
            }else if(cc == 50 || cc == 999) {                        //  LIKE Compare.REGEX
                bson = Filters.regex(column, value.toString());
            }else if(cc == 51) {                                     //  START_WITH
                bson = Filters.regex(column, "^"+value);
            }else if(cc == 52) {                                     //  END_WITH
                bson = Filters.regex(column, value+"$");
            }else if(cc == 20) {                                     //  GREAT
                bson = Filters.gt(column, value);
            }else if(cc == 21) {                                     //  GREAT_EQUAL
                bson = Filters.gte(column, value);
            }else if(cc == 30) {                                     //  LESS
                bson = Filters.lt(column, value);
            }else if(cc == 31) {                                     //  LESS_EQUAL
                bson = Filters.lte(column, value);
            }else if(cc == 40) {                                     //  IN
                bson = Filters.in(column, BeanUtil.list2array(values));
            }else if(cc == 80) {                                     //  BETWEEN
                if(values.size() > 1) {
                    bson = Filters.and(Filters.gte(column, values.get(0)), Filters.lte(column, values.get(1)));
                }
            }else if(cc == 110) {                                    //  NOT EQUAL
                bson = Filters.ne(column, value);
            }else if(cc == 140) {                                    //  NOT IN
                bson = Filters.nin(column, BeanUtil.list2array(values));
            }else if(cc == 150) {                                    //  NOT LIKE
            }else if(cc == 151) {                                     //  NOT START_WITH
            }else if(cc == 152) {                                     //  NOT END_WITH
            }

        }
        return bson;
    }

    @Override
    public DataSet select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
        MongoRun r = (MongoRun) run;
        long fr = System.currentTimeMillis();
        if(null == random) {
            random = random(runtime);
        }
        DataSet set = new DataSet();
        try{
            MongoRuntime rt = (MongoRuntime) runtime;
            MongoDatabase database = rt.getDatabase();
            Bson bson = r.getFilter();
            if(null == bson) {
                bson = Filters.empty();
            }
            if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                log.info("{}[cmd:select][collection:{}][filter:{}]", random, run.getTableName(), bson);
            }
            FindIterable<MongoRow> rows = database.getCollection(run.getTableName(), MongoRow.class).find(bson);
            List<Bson> fields = new ArrayList<>();
            List<String> queryColumns = run.getQueryColumns();
            //查询的列
            if(null != queryColumns && !queryColumns.isEmpty()) {
                fields.add(Projections.include(queryColumns));
            }
            //不查询的列
            List<String> excludeColumn = run.getExcludeColumns();
            if(null != excludeColumn && !excludeColumn.isEmpty()) {
                fields.add(Projections.exclude(excludeColumn));
            }
            if(!fields.isEmpty()) {
                rows.projection(Projections.fields(fields));
            }
            PageNavi navi = run.getPageNavi();
            if(null != navi) {
                long limit = navi.getLastRow() - navi.getFirstRow() + 1;
                rows.skip((int)navi.getFirstRow()).limit((int)limit);
            }
            for(MongoRow row:rows) {
                set.add(row);
            }
            if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[封装耗时:{}][封装行数:{}]", random, DateUtil.format(System.currentTimeMillis() - fr), set.size());
            }
            if((!system || !ConfigStore.IS_LOG_QUERY_RESULT_EXCLUDE_METADATA(configs)) && ConfigStore.IS_LOG_QUERY_RESULT(configs) && log.isInfoEnabled()) {
                log.info("{}[查询结果]{}", random, LogUtil.table(set));
            }
        }catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("select 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                CommandQueryException ex = new CommandQueryException("query异常:" + e, e);
                throw ex;
            }else{
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][cmd:select][collection:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), run.getTableName());
                }
            }
        }
        return set;
    }

    @Override
    public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        Run run = buildQueryRun(runtime, prepare, configs, true, true, conditions);
        return count(runtime, random, run);
    }

    @Override
    public long count(DataRuntime runtime, String random, Run run) {
        MongoRun r = (MongoRun)run;
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        Bson bson = r.getFilter();
        if(null == bson) {
            bson = Filters.empty();
        }
        if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
            log.info("{}[cmd:select][collection:{}][filter:{}]", random, run.getTableName(), bson);
        }
        return database.getCollection(run.getTableName()).countDocuments(bson);
    }

    @Override
    public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, Collection<T> values) {
        return 0;
    }

    @Override
    public long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) {
        MongoRun mr = (MongoRun)run;
        long result = -1;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        long fr = System.currentTimeMillis();
        log.info("{}[action:update][collection:{}][update:{}][filter:{}]", random, run.getTableName(), mr.getUpdate(), mr.getFilter());
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        MongoCollection cons = database.getCollection(run.getTableName());
        UpdateResult dr = cons.updateMany(mr.getFilter(), mr.getUpdate());
        result = dr.getMatchedCount();
        long millis = System.currentTimeMillis() - fr;
        boolean slow = false;
        long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
        if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
            if(millis > SLOW_SQL_MILLIS) {
                slow = true;
                log.warn("{}[slow cmd][action:update][collection:{}][执行耗时:{}][影响行数:{}]", random, run.getTableName(), DateUtil.format(millis), LogUtil.format(result, 34));
                if(null != dmListener) {
                    dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, null, null, null, true, result, millis);
                }
            }
        }
        if (!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[action:update][collection:{}][执行耗时:{}][影响行数:{}]", random, run.getTableName(), DateUtil.format(millis), LogUtil.format(result, 34));
        }
        return result;
    }

    @Override
    public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String key, Collection<T> values) {
        ConfigStore configs = new DefaultConfigStore();
        configs.and(key, values);
        return delete(runtime, random, table, configs);
    }

    @Override
    public long truncate(DataRuntime runtime, String random, Table table) {
        long result = -1;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        long fr = System.currentTimeMillis();
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        MongoCollection cons = database.getCollection(table.getName());
        DeleteResult dr = cons.deleteMany(Filters.empty());
        result = dr.getDeletedCount();
        long millis = System.currentTimeMillis() - fr;
        if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[action:truncate][collection:][执行耗时:{}]", random, table, DateUtil.format(millis));
        }
        return result;
    }

    /**
     * UPDATE [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param configs 条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return 影响行数
     */

    @Override
    public long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return super.update(runtime, random, batch, dest, data, configs, columns);
    }

    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        MongoRun run = new MongoRun(runtime, dest);
        run.setOriginType(2);
        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        List<String> primaryKeys = new ArrayList<>();
        if(null != columns && !columns.isEmpty()) {
            cols = columns;
        }else{
            cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.UPDATE)); ;
        }
        if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
            primaryKeys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
        }else{
            primaryKeys = new ArrayList<>();
            primaryKeys.add("_id");
        }

        // 不更新主键 除非显示指定
        for(String pk:primaryKeys) {
            if(!columns.containsKey(pk.toUpperCase())) {
                cols.remove(pk.toUpperCase());
            }
        }
        //不更新默认主键  除非显示指定
        if(!columns.containsKey("_ID")) {
            cols.remove("_ID");
        }
        boolean isReplaceEmptyNull = ConfigTable.IS_REPLACE_EMPTY_NULL;
        cols = checkMetadata(runtime, dest, configs, cols);
        List<Bson> updates = new ArrayList<>();

        /*构造SQL*/

        if(!cols.isEmpty()) {
            for(Column column:cols.values()) {
                String key = column.getName();
                Object value = null;
                if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
                    Field field = EntityAdapterProxy.field(obj.getClass(), key);
                    value = BeanUtil.getFieldValue(obj, field);
                }else {
                    value = BeanUtil.getFieldValue(obj, key, true);
                }
                //if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}")) {
                if(BasicUtil.checkEl(value+"")) {
                    String str = value.toString();
                    value = str.substring(2, str.length()-1);
                }else{
                    if("NULL".equals(value)) {
                        value = null;
                    }else if("".equals(value) && isReplaceEmptyNull) {
                        value = null;
                    }
                    boolean chk = true;
                    if(null == value) {
                        if(!ConfigTable.IS_UPDATE_NULL_FIELD) {
                            chk = false;
                        }
                    }else if("".equals(value)) {
                        if(!ConfigTable.IS_UPDATE_EMPTY_FIELD) {
                            chk = false;
                        }
                    }
                    if(chk) {
                        updates.add(Updates.set(key, value));
                    }
                }
            }
            run.setUpdate(Updates.combine(updates));

            if(null == configs) {
                configs = new DefaultConfigStore();
                for (String pk : primaryKeys) {
                    if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
                        Field field = EntityAdapterProxy.field(obj.getClass(), pk);
                        configs.and(pk, BeanUtil.getFieldValue(obj, field));
                    } else {
                        configs.and(pk, BeanUtil.getFieldValue(obj, pk, true));
                    }
                }
            }
            run.setConfigStore(configs);
            run.init();
            run.appendCondition(this, true, true, false);
        }
        run.setFilter(parseCondition(null, run.getConditionChain()));
        return run;
    }

    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns) {
        MongoRun run = new MongoRun(runtime, dest);
        run.setOriginType(1);

        /*确定需要更新的列*/
        LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, row, configs, Column.names(columns));
        List<String> primaryKeys = row.getPrimaryKeys();
        if(primaryKeys.isEmpty()) {
            throw new CommandUpdateException("[更新更新异常][更新条件为空, update方法不支持更新整表操作]");
        }

        // 不更新主键 除非显示指定
        for(String pk:primaryKeys) {
            if(!columns.containsKey(pk.toUpperCase())) {
                cols.remove(pk.toUpperCase());
            }
        }
        //不更新默认主键  除非显示指定
        if(!columns.containsKey("_ID")) {
            cols.remove("_ID");
        }

        boolean replaceEmptyNull = row.isReplaceEmptyNull();

        List<Bson> updates = new ArrayList<>();
        if(!cols.isEmpty()) {
            for(Column col:cols.values()) {
                String key = col.getName();
                Object value = row.get(key);
                //if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}")) {
                if(BasicUtil.checkEl(value+"")) {
                    String str = value.toString();
                    value = str.substring(2, str.length()-1);
                 }else{
                     if("NULL".equals(value)) {
                        value = null;
                    }else if("".equals(value) && replaceEmptyNull) {
                        value = null;
                    }
                }
                updates.add(Updates.set(key, value));
            }
            run.setUpdate(Updates.combine(updates));
            if(null == configs) {
                configs = new DefaultConfigStore();
                for (String pk : primaryKeys) {
                    configs.and(pk, row.get(pk));
                }
            }
            run.setConfigStore(configs);
            run.init();
            run.appendCondition(this, true, true, false);
            run.setFilter(parseCondition(null, run.getConditionChain()));
        }

        return run;
    }

    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return null;
    }

    @Override
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs, Boolean placeholder, Boolean unicode, String key, Object values) {
        if(null == key || null == values) {
            return null;
        }
        if(null == configs) {
            configs = new DefaultConfigStore();
        }
        if(values instanceof Collection) {
            Collection collection = (Collection)values;
            if(collection.isEmpty()) {
                return null;
            }
            configs.in(key, collection);
        }else{
            configs.and(key, values);
        }
        return buildDeleteRun(runtime, table, configs, false, false);
    }

    @Override
    public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String... columns) {
        //没有configs条件的 才根据主键删除
        if(null == configs || configs.isEmptyCondition()) {
            if(null == columns || columns.length == 0) {
                columns = new String[]{"_id"};
            }
            if(null == configs) {
                configs = new DefaultConfigStore();
            }
            for(String column:columns) {
                configs.and(column, BeanUtil.getFieldValue(obj, column, true));
            }
        }
        return buildDeleteRun(runtime, dest, configs, placeholder, unicode);
    }

/* *****************************************************************************************************************
     *                                                     DELETE
     * -----------------------------------------------------------------------------------------------------------------
     * List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, String key, Object values)
     * List<Run> buildDeleteRun(DataRuntime runtime, Table dest, Object obj, String ... columns)
     * Run fillDeleteRunContent(DataRuntime runtime, Run run)
     *
     * protected Run buildDeleteRunFromTable(String table, String key, Object values)
     * protected Run buildDeleteRunFromEntity(Table dest, Object obj, String ... columns)
     ******************************************************************************************************************/

    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        List<Run> runs = new ArrayList<>();
        if(null == obj && (null == configs || configs.isEmptyCondition())) {
            return null;
        }
        Run run = null;
        if(null == dest) {
            dest = DataSourceUtil.parseDest(null, obj, configs);
        }
        if(null == dest) {
            Object entity = obj;
            if(obj instanceof Collection) {
                entity = ((Collection)obj).iterator().next();
            }
            Table table = EntityAdapterProxy.table(entity.getClass());
            if(null != table) {
                dest = table;
            }
        }
        if(obj instanceof ConfigStore) {
            run = new MongoRun(runtime, dest);
            RunPrepare prepare = new DefaultTablePrepare();
            prepare.setDest(dest);
            run.setPrepare(prepare);
            run.setConfigStore((ConfigStore)obj);
            run.addCondition(columns);
            run.init();
            fillDeleteRunContent(runtime, run, placeholder, unicode);
            runs.add(run);
        }else{
            runs = buildDeleteRunFromEntity(runtime, dest, configs, obj, placeholder, unicode, columns);
        }
        return runs;
    }

    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
        return null;
    }

    @Override
    public List<Run> buildTruncateRun(DataRuntime runtime, Table table) {
        return null;
    }

    @Override
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
        return null;
    }

    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        List<Run> runs = new ArrayList<>();
        TableRun run = new MongoRun(runtime, table);
        run.setConfigs(configs);
        run.init();
        fillDeleteRunContent(runtime, run);
        runs.add(run);
        return runs;
    }

    /**
     * 构造删除主体
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * */

    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        if(null != run) {
            if(run instanceof TableRun) {
                TableRun r = (TableRun) run;
                fillDeleteRunContent(runtime, r, placeholder, unicode);
            }
        }
    }

    protected void fillDeleteRunContent(DataRuntime runtime, TableRun run) {
        MongoRun mr = (MongoRun)run;
        Bson bson = null;
        ConditionChain chain = run.getConditionChain();
        bson = parseCondition(bson, chain);
        mr.setFilter(bson);
    }

	/**
     * 执行删除
     * @param runtime DataRuntime
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return int
     */
    public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        MongoRun mr = (MongoRun) run;
        long result = -1;
        boolean cmd_success = false;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        long fr = System.currentTimeMillis();
        swt = InterceptorProxy.beforeDelete(runtime, random, run, configs);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.beforeDelete(runtime, random, run);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        log.info("{}[action:delete][collection:{}][filter:{}]", random, run.getTableName(), mr.getFilter());
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        MongoCollection cons = database.getCollection(run.getTableName());
        DeleteResult dr = cons.deleteMany(mr.getFilter());
        result = dr.getDeletedCount();
        cmd_success = true;
        long millis = System.currentTimeMillis() - fr;
        if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[action:delete][collection:{}][执行耗时:{}][影响行数:{}]", random, run.getTableName(), DateUtil.format(millis), LogUtil.format(result, 34));
        }
        if(null != dmListener) {
            dmListener.afterDelete(runtime, random, run, cmd_success, result, millis);
        }
        InterceptorProxy.afterDelete(runtime, random, run, configs, cmd_success, result, millis);
        return result;
    }

    /**
     * table[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param configs ConfigStore
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Table query, int types, ConfigStore configs) throws Exception {
        return new ArrayList<>();
    }

    /**
     * Table[结果集封装]<br/>
     * Table 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTableFieldRefer() {
        return super.initTableFieldRefer();
    }

    /**
     * table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
     * @return List
     * @param <T> Table
     */
    public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Table query, int types, int struct, ConfigStore configs) {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        String pattern = query.getName();
        List<T> tables = new ArrayList<>();
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        ListCollectionNamesIterable names = database.listCollectionNames();
        for(String name:names) {
            T table = (T) new Table(name);
            if(BasicUtil.isNotEmpty(pattern)) {
                String regex = pattern.replace("%", ".*").replace("_", ".");
                if (RegularUtil.match(name.toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                    tables.add(table);
                }
            }else {
                tables.add(table);
            }
        }
        if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
            //查询全部表结构 columns()内部已经给table.columns赋值
            Column column_query = new Column();
            column_query.setCatalog(catalog);
            column_query.setSchema(schema);
            columns(runtime, random, greedy, tables, column_query);
        }
        if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
            //查询全部表结构
            Index index_query = new Index();
            index_query.setCatalog(catalog);
            index_query.setSchema(schema);
            indexes(runtime, random, greedy, tables, index_query);
        }
        return tables;
    }

    /**
     * column[调用入口]<br/>(方法1)<br/>
     * 查询多个表列，并分配到每个表中，需要保持所有表的catalog,schema相同
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
     * @param tables 表
     * @return List
     * @param <T> Column
     */
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Collection<? extends Table> tables, Column query, ConfigStore configs) {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        List<T> list = new ArrayList<>();
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        for(Table table:tables) {
            String cache_key = CacheProxy.key(runtime, "collection_column", greedy, catalog, schema, table.getName());
            LinkedHashMap<String, T> columns = CacheProxy.columns(cache_key);
            if(null == columns || columns.isEmpty()) {
                MongoCollection collection = database.getCollection(table.getName());
                if(null != collection) {
                    if(ConfigTable.CHECK_METADATA_SAMPLE > 0) {
                        MongoCursor<Document> cursor = collection.find().limit(ConfigTable.CHECK_METADATA_SAMPLE).iterator();
                        while (cursor.hasNext()) {
                            Document doc = cursor.next();
                            Set<String> fields = doc.keySet();
                            for(String field:fields) {
                                String up = field.toUpperCase();
                                if(columns.containsKey(up)) {
                                    continue;
                                }
                                Object value = doc.get(field);
                                if(null != value) {
                                    String type = value.getClass().getSimpleName();
                                    Column column = new Column(field, type);
                                    columns.put(up, (T)column);
                                    list.add((T)column);
                                }
                            }
                        }
                        CacheProxy.cache(cache_key, columns);
                    }
                }
            }else{
                list.addAll(columns.values());
            }
        }
        return list;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Column query) throws Exception {
        return null;
    }

    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Tag query) throws Exception {
        return null;
    }

    @Override
    public List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Constraint query) {
        return null;
    }


    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {

    }

    @Override
    public LinkedHashMap<String, Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment) {
        return null;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }

}

