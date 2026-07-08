/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.arango.adapter;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.*;
import org.anyline.adapter.EntityAdapter;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.arango.entity.ArangoRow;
import org.anyline.data.arango.run.ArangoRun;
import org.anyline.data.arango.runtime.ArangoRuntime;
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
import org.anyline.exception.CommandSelectException;
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

import java.lang.reflect.Field;
import java.util.*;

/**
 * ArangoDB 数据适配器<br/>
 * 负责将 Anyline 的条件/实体/DataRow 转换为 ArangoDB 的 AQL 查询与文档操作<br/>
 * <br/>
 * CRUD 策略:<br/>
 * - INSERT: 直接使用 ArangoCollection.insertDocument(s)<br/>
 * - SELECT: 构建 AQL FOR doc IN @@collection FILTER ... SORT ... LIMIT ... RETURN doc<br/>
 * - UPDATE: 构建 AQL UPDATE doc WITH @update IN @@collection<br/>
 * - DELETE: 构建 AQL REMOVE doc IN @@collection<br/>
 * - COUNT:  构建 AQL COLLECT WITH COUNT INTO cnt RETURN cnt<br/>
 * - TRUNCATE: 使用 ArangoCollection.truncate()
 */
@AnylineComponent("anyline.data.adapter.arango")
public class ArangoAdapter extends AbstractDriverAdapter implements DriverAdapter {

    @Override
    public DatabaseType type() {
        return DatabaseType.ArangoDB;
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

    // ========================================================================
    //                                INSERT
    // ========================================================================

    @Override
    public Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs,
                              Object obj, Boolean placeholder, Boolean unicode, String... conditions) {
        return null;
    }

    @Override
    public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs,
                              Boolean placeholder, Boolean unicode, List<String> columns) {
        return createInsertRun(runtime, dest, obj, configs, placeholder, unicode, columns);
    }

    @Override
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs,
                                  Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = new ArangoRun(runtime, dest);
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        if(null != generator) {
            Object pv = BeanUtil.getFieldValue(obj, "_key", true);
            if(null == pv) {
                List<String> pk = new ArrayList<>();
                pk.add("_key");
                generator.create(obj, DatabaseType.ArangoDB, dest.getName(), pk, null);
            }
        }
        run.setValue(obj);
        return run;
    }

    @Override
    protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list,
                                                ConfigStore configs, Boolean placeholder, Boolean unicode,
                                                List<String> columns) {
        Run run = new ArangoRun(runtime, dest);
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        if(null != generator) {
            List<String> pk = new ArrayList<>();
            pk.add("_key");
            for (Object item : list) {
                Object pv = BeanUtil.getFieldValue(item, "_key", true);
                if(null == pv) {
                    generator.create(item, DatabaseType.ArangoDB, dest.getName(), pk, null);
                }
            }
        }
        run.setValue(list);
        return run;
    }

    /**
     * insert [命令执行]<br/>
     * 使用 ArangoDB document API 插入文档
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks) {
        long cnt = 0;
        Object value = run.getValue();
        String collectionName = run.getTableName();
        if(null == value) {
            if(ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                log.warn("[valid:false][action:insert][collection:{}][不具备执行条件]", collectionName);
            }
            return -1;
        }
        ArangoRuntime rt = (ArangoRuntime) runtime;
        ArangoDatabase database = rt.getDatabase();
        ArangoCollection collection = database.collection(collectionName);
        long fr = System.currentTimeMillis();

        try {
            if(value instanceof List) {
                List<Object> list = (List<Object>) value;
                List<BaseDocument> docs = toBaseDocuments(list);
                MultiDocumentEntity result = collection.insertDocuments(docs);
                cnt = docs.size();
                List entities = result.getDocuments();
                for(int i = 0; i < list.size() && i < entities.size(); i++) {
                    setDocumentKey(list.get(i), (DocumentCreateEntity) entities.get(i));
                }
            } else if(value instanceof DataSet) {
                DataSet<DataRow> set = (DataSet<DataRow>) value;
                List<BaseDocument> docs = new ArrayList<>();
                for(DataRow row : set) {
                    docs.add(toBaseDocument(row));
                }
                MultiDocumentEntity result = collection.insertDocuments(docs);
                cnt = set.size();
                List entities = result.getDocuments();
                int idx = 0;
                for(DataRow row : set) {
                    if(idx < entities.size()) {
                        row.setPrimaryValue(((DocumentCreateEntity) entities.get(idx)).getKey());
                    }
                    idx++;
                }
            } else if(value instanceof EntitySet) {
                List<Object> datas = ((EntitySet)value).getDatas();
                List<BaseDocument> docs = toBaseDocuments(datas);
                MultiDocumentEntity result = collection.insertDocuments(docs);
                cnt = docs.size();
                List entities = result.getDocuments();
                for(int i = 0; i < datas.size() && i < entities.size(); i++) {
                    setDocumentKey(datas.get(i), (DocumentCreateEntity) entities.get(i));
                }
            } else if(value instanceof Collection) {
                Collection items = (Collection) value;
                List<Object> list = new ArrayList<>();
                for(Object item : items) {
                    list.add(item);
                }
                List<BaseDocument> docs = toBaseDocuments(list);
                MultiDocumentEntity result = collection.insertDocuments(docs);
                cnt = docs.size();
                List entities = result.getDocuments();
                int i = 0;
                for(Object item : items) {
                    if(i < entities.size()) {
                        setDocumentKey(item, (DocumentCreateEntity) entities.get(i));
                    }
                    i++;
                }
            } else {
                BaseDocument doc = toBaseDocument(value);
                DocumentCreateEntity result = collection.insertDocument(doc);
                setDocumentKey(value, result);
                cnt = 1;
            }

            long millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[{}][action:insert][collection:{}][执行耗时:{}]", random,
                            LogUtil.format("slow cmd", 33), collectionName, DateUtil.format(millis));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.INSERT, run, null, null, null, true, cnt, millis);
                    }
                }
            }
            if (!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[action:insert][collection:{}][执行耗时:{}][影响行数:{}]", random,
                        collectionName, DateUtil.format(millis), LogUtil.format(cnt, 34));
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("insert 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
                throw new CommandUpdateException("insert异常:" + e.getMessage(), e);
            } else {
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][collection:{}][param:{}]", random,
                            LogUtil.format("插入异常:", 33) + e.toString(),
                            collectionName, BeanUtil.object2json(data));
                }
            }
        }
        return cnt;
    }

    /** 将对象转为 BaseDocument */
    @SuppressWarnings("unchecked")
    private BaseDocument toBaseDocument(Object obj) {
        if(obj instanceof BaseDocument) {
            return (BaseDocument) obj;
        }
        if(obj instanceof DataRow) {
            return toBaseDocument((DataRow) obj);
        }
        if(obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            BaseDocument doc = new BaseDocument();
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                doc.addAttribute(entry.getKey(), entry.getValue());
            }
            return doc;
        }
        BaseDocument doc = new BaseDocument();
        // 使用 EntityAdapter 映射或 BeanUtil 转为 Map
        try {
            LinkedHashMap<String, Column> columns = EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.DDL);
            if(null != columns && !columns.isEmpty()) {
                for(Column col : columns.values()) {
                    Object val = BeanUtil.getFieldValue(obj, col.getName(), true);
                    doc.addAttribute(col.getName(), val);
                }
            } else {
                Map<String, Object> map = BeanUtil.object2map(obj);
                for(Map.Entry<String, Object> entry : map.entrySet()) {
                    doc.addAttribute(entry.getKey(), entry.getValue());
                }
            }
        } catch(Exception e) {
            Map<String, Object> map = BeanUtil.object2map(obj);
            for(Map.Entry<String, Object> entry : map.entrySet()) {
                doc.addAttribute(entry.getKey(), entry.getValue());
            }
        }
        // _key → _key 映射
        if(doc.getProperties().containsKey("_key")) {
            Object idVal = doc.getProperties().get("_key");
            if(idVal instanceof String && ((String)idVal).contains("/")) {
                String str = (String) idVal;
                doc.addAttribute("_key", str.substring(str.indexOf('/') + 1));
            }
        }
        return doc;
    }

    private BaseDocument toBaseDocument(DataRow row) {
        BaseDocument doc = new BaseDocument();
        if(null != row) {
            for(String key : row.keySet()) {
                doc.addAttribute(key, row.get(key));
            }
        }
        return doc;
    }

    @SuppressWarnings("unchecked")
    private List<BaseDocument> toBaseDocuments(List<Object> list) {
        List<BaseDocument> docs = new ArrayList<>();
        for(Object obj : list) {
            docs.add(toBaseDocument(obj));
        }
        return docs;
    }

    private void setDocumentKey(Object obj, DocumentCreateEntity result) {
        if(null != result) {
            String key = result.getKey();
            if(null != key) {
                BeanUtil.setFieldValue(obj, "_key", key);
                BeanUtil.setFieldValue(obj, "_key", key);
            }
        }
    }

    // ========================================================================
    //                                METADATA
    // ========================================================================

    @Override
    public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs,
                                                       LinkedHashMap<String, Column> columns) {
        return columns;
    }

    // ========================================================================
    //                                SELECT
    // ========================================================================

    @Override
    public Run buildSelectRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs,
                              Boolean placeholder, Boolean unicode, String... conditions) {
        ArangoRun run;
        if(prepare instanceof TablePrepare) {
            run = new ArangoRun(runtime, prepare.getTableName());
        } else {
            throw new RuntimeException("不支持查询的类型");
        }
        run.setRuntime(runtime);
        run.setPrepare(prepare);
        run.setConfigStore(configs);
        run.addCondition(conditions);
        if(run.checkValid()) {
            run.init();
            fillSelectContent(runtime, run, placeholder, unicode);
        }
        return run;
    }

    @Override
    public Object createConditionJsonContains(DataRuntime runtime, StringBuilder builder, String column,
                                              Compare compare, Object value, Boolean placeholder, Boolean unicode) {
        return null;
    }

    @Override
    protected Run fillSelectContent(DataRuntime runtime, TableRun run, Boolean placeholder, Boolean unicode) {
        ArangoRun r = (ArangoRun) run;
        ConditionChain chain = r.getConditionChain();

        // 构建 AQL WHERE + bindVars
        Map<String, Object> bindVars = new HashMap<>();
        String whereClause = buildAqlWhere(chain, bindVars);
        r.setBindVars(bindVars);
        r.setCmd(whereClause);

        // 查询列
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

        List<String> queryColumns = r.getSelectColumns();
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
            r.setSelectColumns(queryColumns);
        }
        return r;
    }

    /**
     * 将 ConditionChain 转换为 AQL WHERE 子句(不含 "FILTER" 关键字)与绑定变量
     */
    private String buildAqlWhere(ConditionChain chain, Map<String, Object> bindVars) {
        if(null == chain || chain.getConditions().isEmpty()) {
            return "";
        }
        return buildAqlCondition(chain, bindVars);
    }

    private String buildAqlCondition(Condition condition, Map<String, Object> bindVars) {
        if(condition instanceof ConditionChain) {
            ConditionChain chain = (ConditionChain) condition;
            List<Condition> conditions = chain.getConditions();
            if(null == conditions || conditions.isEmpty()) {
                return "";
            }
            StringBuilder result = new StringBuilder();
            String join = (Condition.JOIN.OR == chain.getJoin()) ? " OR " : " AND ";
            for(int i = 0; i < conditions.size(); i++) {
                String part = buildAqlCondition(conditions.get(i), bindVars);
                if(part.length() > 0) {
                    if(result.length() > 0) {
                        result.append(join);
                    }
                    result.append(part);
                }
            }
            return result.length() > 0 ? "(" + result + ")" : "";
        }

        if(condition instanceof AutoCondition) {
            AutoCondition auto = (AutoCondition) condition;
            String column = auto.getId();
            List<Object> values = auto.getValues();
            if(null == values || values.isEmpty()) {
                return "";
            }
            Compare compare = auto.getCompare();
            int cc = compare.getCode();
            String varName = column.replaceAll("[^a-zA-Z0-9_]", "_") + "_" + bindVars.size();

            // EQUAL = 10
            if(cc == 10) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "doc.`" + column + "` == @" + varName;
            }
            // NOT EQUAL = 110
            if(cc == 110) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "doc.`" + column + "` != @" + varName;
            }
            // GREAT = 20
            if(cc == 20) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "doc.`" + column + "` > @" + varName;
            }
            // GREAT_EQUAL = 21
            if(cc == 21) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "doc.`" + column + "` >= @" + varName;
            }
            // LESS = 30
            if(cc == 30) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "doc.`" + column + "` < @" + varName;
            }
            // LESS_EQUAL = 31
            if(cc == 31) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "doc.`" + column + "` <= @" + varName;
            }
            // IN = 40
            if(cc == 40) {
                List<Object> vals = new ArrayList<>();
                for(Object v : values) {
                    vals.add(convertAqlValue(v));
                }
                bindVars.put(varName, vals);
                return "doc.`" + column + "` IN @" + varName;
            }
            // NOT IN = 140
            if(cc == 140) {
                List<Object> vals = new ArrayList<>();
                for(Object v : values) {
                    vals.add(convertAqlValue(v));
                }
                bindVars.put(varName, vals);
                return "doc.`" + column + "` NOT IN @" + varName;
            }
            // LIKE = 50, REGEX = 99
            if(cc == 50 || cc == 99) {
                Object val = convertAqlValue(values.get(0));
                String regex = escapeRegex(val != null ? val.toString() : "");
                bindVars.put(varName, regex);
                return "REGEX_TEST(doc.`" + column + "`, @" + varName + ", true)";
            }
            // START_WITH = 51
            if(cc == 51) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val);
                return "STARTS_WITH(doc.`" + column + "`, @" + varName + ")";
            }
            // END_WITH = 52
            if(cc == 52) {
                Object val = convertAqlValue(values.get(0));
                bindVars.put(varName, val.toString() + "$");
                return "REGEX_TEST(doc.`" + column + "`, @" + varName + ", true)";
            }
            // BETWEEN = 80
            if(cc == 80) {
                if(values.size() >= 2) {
                    String varName2 = varName + "_2";
                    bindVars.put(varName, convertAqlValue(values.get(0)));
                    bindVars.put(varName2, convertAqlValue(values.get(1)));
                    return "doc.`" + column + "` >= @" + varName + " AND doc.`" + column + "` <= @" + varName2;
                }
            }
            // NULL = 90
            if(cc == 90) {
                return "doc.`" + column + "` == null";
            }
            // NOT_NULL = 190
            if(cc == 190) {
                return "doc.`" + column + "` != null";
            }
        }
        return "";
    }

    private Object convertAqlValue(Object value) {
        if(null == value) {
            return null;
        }
        if(value instanceof String) {
            String str = (String) value;
            if(str.contains("/")) {
                return str.substring(str.indexOf('/') + 1);
            }
        }
        return value;
    }

    private String escapeRegex(String val) {
        if(null == val) return "";
        return val.replace("\\", "\\\\").replace(".", "\\.").replace("*", ".*")
                .replace("?", ".").replace("^", "\\^").replace("$", "\\$");
    }

    // ========================================================================
    //                                QUERY
    // ========================================================================

    @Override
    @SuppressWarnings("unchecked")
    public DataSet<DataRow> query(DataRuntime runtime, String random, boolean system, Table table,
                                  ConfigStore configs, Run run) {
        ArangoRun r = (ArangoRun) run;
        long fr = System.currentTimeMillis();
        if(null == random) {
            random = random(runtime);
        }
        DataSet<DataRow> set = new DataSet<>();
        try {
            ArangoRuntime rt = (ArangoRuntime) runtime;
            ArangoDatabase database = rt.getDatabase();
            String collectionName = run.getTableName();

            String aql = buildSelectAql(r);
            Map<String, Object> bindVars = new HashMap<>();
            if(null != r.getBindVars()) {
                bindVars.putAll(r.getBindVars());
            }

            if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                log.info("{}[cmd:select][aql:{}][bindVars:{}]", random, aql, bindVars);
            }

            String inlined = inlineAql(aql, collectionName, bindVars);
            ArangoCursor<BaseDocument> cursor = database.query(inlined, BaseDocument.class);
            while (cursor.hasNext()) {
                BaseDocument doc = cursor.next();
                ArangoRow row = new ArangoRow();
                for(Map.Entry<String, Object> entry : doc.getProperties().entrySet()) {
                    row.put(entry.getKey(), entry.getValue());
                    if("_key".equals(entry.getKey())) {
                        row.setPrimaryValue(entry.getValue());
                    }
                }
                set.add(row);
            }

            if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[封装耗时:{}][封装行数:{}]", random, DateUtil.format(System.currentTimeMillis() - fr), set.size());
            }
            if((!system || !ConfigStore.IS_LOG_QUERY_RESULT_EXCLUDE_METADATA(configs))
                    && ConfigStore.IS_LOG_QUERY_RESULT(configs) && log.isInfoEnabled()) {
                log.info("{}[查询结果]{}", random, LogUtil.table(set));
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("select 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw new CommandSelectException("query异常:" + e.getMessage(), e);
            } else {
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][cmd:select][collection:{}]", random,
                            LogUtil.format("查询异常:", 33) + e.toString(), run.getTableName());
                }
            }
        }
        return set;
    }

    /**
     * 构建完整 AQL SELECT 语句<br/>
     * FOR doc IN @@collection [FILTER ...] [SORT ...] [LIMIT ...] RETURN doc
     */
    private String buildSelectAql(ArangoRun run) {
        StringBuilder aql = new StringBuilder("FOR doc IN @@collection");

        // FILTER
        String where = run.getCmd();
        if(where != null && !where.isEmpty()) {
            aql.append(" FILTER ").append(where);
        }

        // SORT
        OrderStore orders = run.getOrders();
        if(null != orders && !orders.isEmpty()) {
            List<String> sortParts = new ArrayList<>();
            LinkedHashMap<String, Order> ods = orders.gets();
            for(Order od : ods.values()) {
                String dir = (Order.TYPE.DESC == od.getType()) ? "DESC" : "ASC";
                sortParts.add("doc.`" + od.getColumn() + "` " + dir);
            }
            aql.append(" SORT ").append(String.join(", ", sortParts));
        }

        // LIMIT (分页)
        PageNavi navi = run.getPageNavi();
        if(null != navi) {
            long skip = navi.getFirstRow();
            long limit = navi.getLastRow() - navi.getFirstRow() + 1;
            aql.append(" LIMIT ").append(skip).append(", ").append(limit);
        }

        // RETURN — 带投影
        List<String> queryColumns = run.getSelectColumns();
        List<String> excludeColumns = run.getExcludeColumns();
        if(queryColumns != null && !queryColumns.isEmpty()) {
            aql.append(" RETURN KEEP(doc, ").append(toAqlArray(queryColumns)).append(")");
        } else if(excludeColumns != null && !excludeColumns.isEmpty()) {
            aql.append(" RETURN UNSET(doc, ").append(toAqlArray(excludeColumns)).append(")");
        } else {
            aql.append(" RETURN doc");
        }

        return aql.toString();
    }

    private String toAqlArray(List<String> items) {
        StringBuilder sb = new StringBuilder("[");
        for(int i = 0; i < items.size(); i++) {
            if(i > 0) sb.append(", ");
            sb.append("\"").append(items.get(i)).append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 将 AQL 模板中的 @@collection / @var 替换为字面值, 返回可直接执行的 AQL
     */
    private String inlineAql(String aqlTemplate, String collectionName, Map<String, Object> bindVars) {
        String result = aqlTemplate;
        // 替换 @@collection → 字面集合名
        result = result.replace("@@collection", "`" + collectionName + "`");
        // 替换 @varName → 字面值
        if(null != bindVars) {
            for(Map.Entry<String, Object> entry : bindVars.entrySet()) {
                String key = "@" + entry.getKey();
                String val = toAqlLiteral(entry.getValue());
                result = result.replace(key + " ", val + " ");
                result = result.replace(key + ")", val + ")");
                result = result.replace(key + ",", val + ",");
                result = result.replace(key + "\n", val + "\n");
                result = result.replace(key, val); // fallback
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String toAqlLiteral(Object value) {
        if(null == value) return "null";
        if(value instanceof String) {
            return "\"" + ((String) value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }
        if(value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }
        if(value instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            int i = 0;
            for(Map.Entry<Object, Object> entry : ((Map<Object, Object>) value).entrySet()) {
                if(i++ > 0) sb.append(", ");
                sb.append("\"").append(entry.getKey()).append("\": ").append(toAqlLiteral(entry.getValue()));
            }
            sb.append("}");
            return sb.toString();
        }
        if(value instanceof Collection) {
            StringBuilder sb = new StringBuilder("[");
            int i = 0;
            for(Object item : (Collection<?>) value) {
                if(i++ > 0) sb.append(", ");
                sb.append(toAqlLiteral(item));
            }
            sb.append("]");
            return sb.toString();
        }
        return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    @Override
    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        List<Map<String, Object>> maps = new ArrayList<>();
        if(null == random) {
            random = random(runtime);
        }
        ArangoRun r = (ArangoRun) run;
        long fr = System.currentTimeMillis();

        try {
            ArangoRuntime rt = (ArangoRuntime) runtime;
            ArangoDatabase database = rt.getDatabase();

            String aql = buildSelectAql(r);
            Map<String, Object> bindVars = new HashMap<>();
            if(null != r.getBindVars()) {
                bindVars.putAll(r.getBindVars());
            }

            if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                log.info("{}[cmd:select][aql:{}][bindVars:{}]", random, aql, bindVars);
            }

            String inlined = inlineAql(aql, run.getTableName(), bindVars);
            ArangoCursor<Map> cursor = database.query(inlined, Map.class);
            while (cursor.hasNext()) {
                Map doc = cursor.next();
                Map<String, Object> map = new HashMap<>();
                for(Object e : doc.entrySet()) {
                    Map.Entry entry = (Map.Entry) e;
                    map.put(entry.getKey().toString(), entry.getValue());
                }
                maps.add(map);
            }

            if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[封装耗时:{}][封装行数:{}]", random,
                        DateUtil.format(System.currentTimeMillis() - fr), maps.size());
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("maps 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw new CommandSelectException("query异常:" + e.getMessage(), e);
            } else {
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][cmd:maps][collection:{}]", random,
                            LogUtil.format("查询异常:", 33) + e.toString(), run.getTableName());
                }
            }
        }
        return maps;
    }

    // ========================================================================
    //                                COUNT
    // ========================================================================

    @Override
    public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        Run run = buildSelectRun(runtime, prepare, configs, true, true, conditions);
        return count(runtime, random, run);
    }

    @Override
    public long count(DataRuntime runtime, String random, Run run) {
        ArangoRun r = (ArangoRun) run;
        ArangoRuntime rt = (ArangoRuntime) runtime;
        ArangoDatabase database = rt.getDatabase();
        String collectionName = run.getTableName();

        try {
            StringBuilder aql = new StringBuilder("FOR doc IN @@collection");
            String where = r.getCmd();
            if(where != null && !where.isEmpty()) {
                aql.append(" FILTER ").append(where);
            }
            aql.append(" COLLECT WITH COUNT INTO cnt RETURN cnt");

            Map<String, Object> bindVars = new HashMap<>();
            if(null != r.getBindVars()) {
                bindVars.putAll(r.getBindVars());
            }

            if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                log.info("{}[cmd:count][aql:{}][bindVars:{}]", random, aql, bindVars);
            }

            String inlined = inlineAql(aql.toString(), collectionName, bindVars);
            ArangoCursor<Long> cursor = database.query(inlined, Long.class);
            if(cursor.hasNext()) {
                return cursor.next();
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("count 异常:", e);
            }
        }
        return 0;
    }

    // ========================================================================
    //                                DELETE
    // ========================================================================

    @Override
    public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs,
                            String column, Collection<T> values) {
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String key,
                            Collection<T> values) {
        if(values == null || values.isEmpty()) return 0;
        List<String> keys = new ArrayList<>();
        for(T v : values) {
            if(null != v) keys.add(v.toString());
        }
        ArangoRuntime rt = (ArangoRuntime) runtime;
        ArangoCollection collection = rt.getDatabase().collection(table.getName());
        try {
            MultiDocumentEntity result = collection.deleteDocuments(keys);
            if(null != result && null != result.getDocuments()) {
                return result.getDocuments().size();
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("deletes 异常:", e);
            }
        }
        return 0;
    }

    @Override
    public long truncate(DataRuntime runtime, String random, Table table) {
        long fr = System.currentTimeMillis();
        ArangoRuntime rt = (ArangoRuntime) runtime;
        ArangoDatabase database = rt.getDatabase();
        ArangoCollection collection = database.collection(table.getName());
        try {
            collection.truncate();
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("truncate 异常:", e);
            }
        }
        long millis = System.currentTimeMillis() - fr;
        if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[action:truncate][collection:{}][执行耗时:{}]", random, table, DateUtil.format(millis));
        }
        return 1;
    }

    @Override
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,
                                             Boolean placeholder, Boolean unicode, String key, Object values) {
        if(null == key || null == values) {
            return null;
        }
        if(null == configs) {
            configs = new DefaultConfigStore();
        }
        if(values instanceof Collection) {
            Collection collection = (Collection) values;
            if(collection.isEmpty()) {
                return null;
            }
            configs.in(key, collection);
        } else {
            configs.and(key, values);
        }
        return buildDeleteRun(runtime, table, configs, false, false);
    }

    @Override
    public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table dest, ConfigStore configs,
                                              Object obj, Boolean placeholder, Boolean unicode, String... columns) {
        if(null == configs || configs.isEmptyCondition()) {
            if(null == columns || columns.length == 0) {
                columns = new String[]{"_key"};
            }
            if(null == configs) {
                configs = new DefaultConfigStore();
            }
            for(String column : columns) {
                Object val = BeanUtil.getFieldValue(obj, column, true);
                if(null != val) {
                    configs.and(column, val);
                }
            }
        }
        return buildDeleteRun(runtime, dest, configs, placeholder, unicode);
    }

    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs,
                                    Object obj, Boolean placeholder, Boolean unicode, String... columns) {
        List<Run> runs = new ArrayList<>();
        if(null == obj && (null == configs || configs.isEmptyCondition())) {
            return null;
        }
        if(null == dest) {
            dest = DataSourceUtil.parseDest(null, obj, configs);
        }
        if(null == dest) {
            Object entity = obj;
            if(obj instanceof Collection) {
                entity = ((Collection) obj).iterator().next();
            }
            Table tbl = EntityAdapterProxy.table(entity.getClass());
            if(null != tbl) {
                dest = tbl;
            }
        }
        if(obj instanceof ConfigStore) {
            Run run = new ArangoRun(runtime, dest);
            RunPrepare prepare = new DefaultTablePrepare();
            prepare.setDest(dest);
            run.setPrepare(prepare);
            run.setConfigStore((ConfigStore) obj);
            run.addCondition(columns);
            run.init();
            fillDeleteRunContent(runtime, run, placeholder, unicode);
            runs.add(run);
        } else {
            runs = buildDeleteRunFromEntity(runtime, dest, configs, obj, placeholder, unicode, columns);
        }
        return runs;
    }

    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs,
                                    Boolean placeholder, Boolean unicode, String column, Object values) {
        return null;
    }

    @Override
    public List<Run> buildTruncateRun(DataRuntime runtime, Table table) {
        return null;
    }

    @Override
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs,
                                             Boolean placeholder, Boolean unicode, String column, Object values) {
        return null;
    }

    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs,
                                    Boolean placeholder, Boolean unicode) {
        List<Run> runs = new ArrayList<>();
        TableRun run = new ArangoRun(runtime, table);
        run.setConfigs(configs);
        run.init();
        fillDeleteRunContent(runtime, run);
        runs.add(run);
        return runs;
    }

    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        if(null != run && run instanceof TableRun) {
            fillDeleteRunContent(runtime, (TableRun) run);
        }
    }

    protected void fillDeleteRunContent(DataRuntime runtime, TableRun run) {
        ArangoRun ar = (ArangoRun) run;
        ConditionChain chain = run.getConditionChain();
        Map<String, Object> bindVars = new HashMap<>();
        String where = buildAqlWhere(chain, bindVars);
        ar.setCmd(where);
        ar.setBindVars(bindVars);
    }

    /**
     * 执行删除(通过 AQL REMOVE)
     */
    @Override
    @SuppressWarnings("unchecked")
    public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        ArangoRun mr = (ArangoRun) run;
        long result = -1;
        boolean cmdSuccess = false;
        long fr = System.currentTimeMillis();

        ACTION.SWITCH swt = InterceptorProxy.beforeDelete(runtime, random, run, configs);
        if(swt == ACTION.SWITCH.BREAK) return -1;
        if(null != dmListener) {
            swt = dmListener.beforeDelete(runtime, random, run);
        }
        if(swt == ACTION.SWITCH.BREAK) return -1;

        String collectionName = run.getTableName();
        String where = mr.getCmd();
        Map<String, Object> bindVars = mr.getBindVars();

        try {
            ArangoRuntime rt = (ArangoRuntime) runtime;
            ArangoDatabase database = rt.getDatabase();

            StringBuilder aql = new StringBuilder("FOR doc IN @@collection");
            if(where != null && !where.isEmpty()) {
                aql.append(" FILTER ").append(where);
            }
            aql.append(" REMOVE doc IN @@collection");

            if(null == bindVars) bindVars = new HashMap<>();

            if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                log.info("{}[action:delete][aql:{}][bindVars:{}]", random, aql, bindVars);
            }

            String inlined = inlineAql(aql.toString(), collectionName, bindVars);
            ArangoCursor<BaseDocument> cursor = database.query(inlined, BaseDocument.class);
            result = cursor.getCount();
            cmdSuccess = true;

            long millis = System.currentTimeMillis() - fr;
            if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[action:delete][collection:{}][执行耗时:{}][影响行数:{}]", random,
                        collectionName, DateUtil.format(millis), LogUtil.format(result, 34));
            }
            if(null != dmListener) {
                dmListener.afterDelete(runtime, random, run, cmdSuccess, result, millis);
            }
            InterceptorProxy.afterDelete(runtime, random, run, configs, cmdSuccess, result, millis);

        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("delete 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw new CommandSelectException("delete异常:" + e.getMessage(), e);
            }
        }
        return result;
    }

    // ========================================================================
    //                                UPDATE
    // ========================================================================

    @Override
    public long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) {
        ArangoRun mr = (ArangoRun) run;
        long result = 0;
        long fr = System.currentTimeMillis();
        String collectionName = run.getTableName();
        Map<String, Object> updateData = mr.getUpdateData();
        String where = mr.getCmd();
        Map<String, Object> bindVars = new HashMap<>();

        try {
            ArangoRuntime rt = (ArangoRuntime) runtime;
            ArangoDatabase database = rt.getDatabase();

            StringBuilder aql = new StringBuilder("FOR doc IN @@collection");
            if(where != null && !where.isEmpty()) {
                aql.append(" FILTER ").append(where);
                if(null != mr.getBindVars()) {
                    bindVars.putAll(mr.getBindVars());
                }
            }
            aql.append(" UPDATE doc WITH @update IN @@collection LET updated = NEW RETURN OLD._key");

            bindVars.put("update", updateData != null ? updateData : new HashMap<>());

            if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                log.info("{}[action:update][aql:{}][bindVars:{}]", random, aql, bindVars);
            }

            String inlined = inlineAql(aql.toString(), collectionName, bindVars);
            ArangoCursor<String> cursor = database.query(inlined, String.class);
            while(cursor.hasNext()) {
                cursor.next();
                result++;
            }

            long millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[{}][action:update][collection:{}][执行耗时:{}][影响行数:{}]", random,
                            LogUtil.format("slow cmd", 33), collectionName,
                            DateUtil.format(millis), LogUtil.format(result, 34));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, null, null, null, true, result, millis);
                    }
                }
            }
            if(!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[action:update][collection:{}][执行耗时:{}][影响行数:{}]", random,
                        collectionName, DateUtil.format(millis), LogUtil.format(result, 34));
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("update 异常:", e);
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                throw new CommandSelectException("update异常:" + e.getMessage(), e);
            }
        }
        return result;
    }

    @Override
    public long update(DataRuntime runtime, String random, int batch, Table dest, Object data,
                       ConfigStore configs, List<String> columns) {
        return super.update(runtime, random, batch, dest, data, configs, columns);
    }

    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs,
                                        Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        ArangoRun run = new ArangoRun(runtime, dest);
        run.setOriginType(2);

        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        List<String> primaryKeys = new ArrayList<>();
        if(null != columns && !columns.isEmpty()) {
            cols = columns;
        } else {
            cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.UPDATE));
        }
        if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
            primaryKeys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
        } else {
            primaryKeys.add("_key");
        }

        for(String pk : primaryKeys) {
            if(!columns.containsKey(pk.toUpperCase())) {
                cols.remove(pk.toUpperCase());
            }
        }
        if(!columns.containsKey("_key")) {
            cols.remove("_key");
        }

        boolean isReplaceEmptyNull = ConfigTable.IS_REPLACE_EMPTY_NULL;
        cols = checkMetadata(runtime, dest, configs, cols);

        Map<String, Object> updateData = new HashMap<>();
        if(!cols.isEmpty()) {
            for(Column column : cols.values()) {
                String key = column.getName();
                Object value;
                if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
                    Field field = EntityAdapterProxy.field(obj.getClass(), key);
                    value = BeanUtil.getFieldValue(obj, field);
                } else {
                    value = BeanUtil.getFieldValue(obj, key, true);
                }
                if(BasicUtil.checkEl(String.valueOf(value))) {
                    String str = value.toString();
                    value = str.substring(2, str.length() - 1);
                } else {
                    if("NULL".equals(value)) {
                        value = null;
                    } else if("".equals(value) && isReplaceEmptyNull) {
                        value = null;
                    }
                }
                updateData.put(key, value);
            }
            run.setUpdateData(updateData);

            if(null == configs) {
                configs = new DefaultConfigStore();
                for(String pk : primaryKeys) {
                    if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
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

        Map<String, Object> bindVars = new HashMap<>();
        String where = buildAqlWhere(run.getConditionChain(), bindVars);
        run.setCmd(where);
        run.setBindVars(bindVars);
        return run;
    }

    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs,
                                         Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        ArangoRun run = new ArangoRun(runtime, dest);
        run.setOriginType(1);

        LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, row, configs, Column.names(columns));
        List<String> primaryKeys = row.getPrimaryKeys();
        if(primaryKeys.isEmpty()) {
            throw new CommandUpdateException("[更新更新异常][更新条件为空, update方法不支持更新整表操作]");
        }

        for(String pk : primaryKeys) {
            if(!columns.containsKey(pk.toUpperCase())) {
                cols.remove(pk.toUpperCase());
            }
        }
        if(!columns.containsKey("_key")) {
            cols.remove("_key");
        }

        boolean replaceEmptyNull = row.isReplaceEmptyNull();

        Map<String, Object> updateData = new HashMap<>();
        if(!cols.isEmpty()) {
            for(Column col : cols.values()) {
                String key = col.getName();
                Object value = row.get(key);
                if(BasicUtil.checkEl(String.valueOf(value))) {
                    String str = value.toString();
                    value = str.substring(2, str.length() - 1);
                } else {
                    if("NULL".equals(value)) {
                        value = null;
                    } else if("".equals(value) && replaceEmptyNull) {
                        value = null;
                    }
                }
                updateData.put(key, value);
            }
            run.setUpdateData(updateData);

            if(null == configs) {
                configs = new DefaultConfigStore();
                for(String pk : primaryKeys) {
                    configs.and(pk, row.get(pk));
                }
            }
            run.setConfigStore(configs);
            run.init();
            run.appendCondition(this, true, true, false);

            Map<String, Object> bindVars = new HashMap<>();
            String where = buildAqlWhere(run.getConditionChain(), bindVars);
            run.setCmd(where);
            run.setBindVars(bindVars);
        }
        return run;
    }

    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list,
                                            ConfigStore configs, Boolean placeholder, Boolean unicode,
                                            LinkedHashMap<String, Column> columns) {
        return null;
    }

    // ========================================================================
    //                                TABLES / COLUMNS
    // ========================================================================

    @Override
    public List<Run> buildSelectTablesRun(DataRuntime runtime, boolean greedy, Table query, int types,
                                          ConfigStore configs) throws Exception {
        return new ArrayList<>();
    }

    @Override
    public MetadataFieldRefer initTableFieldRefer() {
        return super.initTableFieldRefer();
    }

    @SuppressWarnings("unchecked")
    public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy,
                                            Table query, int types, int struct, ConfigStore configs) {
        String pattern = query.getName();
        List<T> tables = new ArrayList<>();
        ArangoRuntime rt = (ArangoRuntime) runtime;
        ArangoDatabase database = rt.getDatabase();

        try {
            Collection<CollectionEntity> entities = database.getCollections();
            for (CollectionEntity entity : entities) {
                if(entity.getIsSystem()) {
                    continue;
                }
                String name = entity.getName();
                if(BasicUtil.isNotEmpty(pattern)) {
                    String regex = pattern.replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(name.toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T table = (T) new Table(name);
                tables.add(table);
            }

            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                Column columnQuery = new Column();
                columnQuery.setCatalog(query.getCatalog());
                columnQuery.setSchema(query.getSchema());
                columns(runtime, random, greedy, tables, columnQuery);
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                Index indexQuery = new Index();
                indexQuery.setCatalog(query.getCatalog());
                indexQuery.setSchema(query.getSchema());
                indexes(runtime, random, greedy, tables, indexQuery);
            }
        } catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("tables 异常:", e);
            }
        }
        return tables;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy,
                                              Collection<? extends Table> tables, Column query, ConfigStore configs) {
        List<T> list = new ArrayList<>();
        ArangoRuntime rt = (ArangoRuntime) runtime;
        ArangoDatabase database = rt.getDatabase();

        for(Table table : tables) {
            String cacheKey = CacheProxy.key(runtime, "collection_column", greedy,
                    query.getCatalog(), query.getSchema(), table.getName());
            LinkedHashMap<String, T> cachedColumns = CacheProxy.columns(cacheKey);

            if(null == cachedColumns || cachedColumns.isEmpty()) {
                cachedColumns = new LinkedHashMap<>();
                try {
                    ArangoCollection collection = database.collection(table.getName());
                    if(null != collection) {
                        String aqlTemplate = "FOR doc IN @@collection LIMIT @limit RETURN doc";
                        Map<String, Object> bindVars = new HashMap<>();
                        bindVars.put("limit", Long.valueOf(ConfigTable.CHECK_METADATA_SAMPLE));

                        String inlined = inlineAql(aqlTemplate, table.getName(), bindVars);
                        ArangoCursor<BaseDocument> cursor = database.query(inlined, BaseDocument.class);
                        while (cursor.hasNext()) {
                            BaseDocument doc = cursor.next();
                            for(Map.Entry<String, Object> entry : doc.getProperties().entrySet()) {
                                String field = entry.getKey();
                                String up = field.toUpperCase();
                                if(cachedColumns.containsKey(up)) continue;

                                Object value = entry.getValue();
                                if(null != value) {
                                    String type = value.getClass().getSimpleName();
                                    Column column = new Column(field, type);
                                    cachedColumns.put(up, (T) column);
                                    list.add((T) column);
                                }
                            }
                        }
                        CacheProxy.cache(cacheKey, cachedColumns);
                    }
                } catch(Exception e) {
                    if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                        log.error("columns 异常:", e);
                    }
                }
            } else {
                list.addAll(cachedColumns.values());
            }
        }
        return list;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create,
                                                               LinkedHashMap<String, T> columns, Column query) throws Exception {
        return null;
    }

    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create,
                                                         LinkedHashMap<String, T> tags, Tag query) throws Exception {
        return null;
    }

    @Override
    public List<Run> buildSelectConstraintsRun(DataRuntime runtime, boolean greedy, Constraint query) {
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