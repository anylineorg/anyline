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


package org.anyline.data.pinecone.adapter;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import io.pinecone.clients.Index;
import io.pinecone.clients.Pinecone;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.pinecone.run.PineconeRun;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.Compare;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.BeanUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.anyline.util.LogUtil;

import java.util.*;

@AnylineComponent("anyline.data.adapter.pinecone")
public class PineconeAdapter extends AbstractDriverAdapter {

    public PineconeAdapter() {
        super();
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.Pinecone;
    }

    @Override
    public boolean supportCatalog() {
        return false;
    }

    @Override
    public boolean supportSchema() {
        return false;
    }

    public PineconeActuator actuator() {
        return (PineconeActuator) actuator;
    }

    @Override
    public Run buildSelectRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        PineconeRun run = new PineconeRun(runtime, prepare.getTableName());
        run.setRuntime(runtime);
        run.setConfigStore(configs);
        run.setPrepare(prepare);
        run.addCondition(conditions);
        if(run.checkValid()) {
            run.init();
            fillSelectContent(runtime, run, placeholder, unicode);
        }
        return run;
    }

    @Override
    public long count(DataRuntime runtime, String random, Run run) {
        try {
            return actuator().count(this, runtime, random, null, run);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * select [命令执行]<br/>
     * Pinecone非关系型数据库，不使用SQL文本命令，直接通过SDK调用
     */
    @Override
    public DataSet<DataRow> selects(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
        long fr = System.currentTimeMillis();
        if(null == random) {
            random = random(runtime);
        }
        DataSet set = new DataSet();
        try{
            if(run instanceof PineconeRun) {
                PineconeRun r = (PineconeRun) run;
                if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                    log.info("{}[cmd:select][index:{}][filter:{}]", random, r.getTableName(), r.getFilterStr());
                }
            } else {
                if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
                    log.info("{}[cmd:select][index:{}]", random, run.getTableName());
                }
            }
            set = actuator().selects(this, runtime, random, system, ACTION.DML.SELECT, table, configs, run, null, null, null);
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
                throw new org.anyline.exception.CommandSelectException("query异常:" + e, e);
            } else {
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][index:{}]", random, LogUtil.format("查询异常:", 33) + e, run.getTableName());
                }
            }
        }
        return set;
    }

    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        if(null == random) {
            random = random(runtime);
        }
        if(null != configs) {
            configs.add(run);
        }
        if(log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
            log.info("{}[action:select]{}", random, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
        }
        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return new ArrayList<>();
        }
        try{
            return actuator().maps(this, runtime, random, configs, run);
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("maps 异常:", e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                throw new org.anyline.exception.CommandSelectException("query异常:" + e, e);
            }
        }
        return new ArrayList<>();
    }

    @Override
    public Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        List<Map<String, Object>> maps = maps(runtime, random, configs, run);
        if(maps != null && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }

    @Override
    public void init(DataRuntime runtime, Run run, ConfigStore configs, String ... conditions) {
        super.init(runtime, run, configs, conditions);
    }

    @Override
    public String name(Type type) {
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
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Column query) throws Exception {
        return null;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }

    /* *****************************************************************************************************************
     *                                                     DELETE
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * long truncate(DataRuntime runtime, String random, String table)
     * [命令合成]
     * List<Run> buildDeleteRun(...)
     * List<Run> buildDeleteRunFromEntity(...)
     * void fillDeleteRunContent(...)
     * [命令执行]
     * long delete(DataRuntime runtime, String random, ConfigStore configs, Run run)
     ******************************************************************************************************************/

    /**
     * truncate [调用入口]<br/>
     * 参考MongoDB/Milvus实现:直接调用Pinecone client删除全部数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @return 影响行数
     */
    @Override
    public long truncate(DataRuntime runtime, String random, Table table) {
        long result = -1;
        long fr = System.currentTimeMillis();
        try {
            Pinecone client = client(runtime);
            Index index = client.getIndexConnection(table.getName());
            index.deleteAll(null);
            result = 1;
        } catch (Exception e) {
            log.error("truncate exception:", e);
        }
        long millis = System.currentTimeMillis() - fr;
        if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[action:truncate][index:{}][执行耗时:{}]", random, table.getName(), DateUtil.format(millis));
        }
        return result;
    }

    /**
     * delete[命令合成]<br/>
     * 参考MongoDB/Milvus模式:创建PineconeRun,填充条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性
     * @return Run
     */
    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        List<Run> runs = new ArrayList<>();
        if(null == obj && (null == configs || configs.isEmptyCondition())) {
            return null;
        }
        if(obj instanceof Collection) {
            Collection list = (Collection) obj;
            for(Object item : list) {
                runs.addAll(buildDeleteRun(runtime, dest, configs, item, placeholder, unicode, columns));
            }
            return runs;
        }
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
            PineconeRun run = new PineconeRun(runtime, dest);
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

    /**
     * delete[命令合成]<br/>
     * Pinecone批量删除由delete(Run)处理,build阶段暂不实现
     */
    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String key, Object values) {
        return null;
    }

    /**
     * delete[命令合成]<br/>
     * truncate已在truncate()中直接实现,不需要build run
     */
    @Override
    public List<Run> buildTruncateRun(DataRuntime runtime, Table table) {
        return null;
    }

    /**
     * delete[命令合成]<br/>
     * 创建PineconeRun并从ConfigStore构建删除条件
     */
    public List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        List<Run> runs = new ArrayList<>();
        PineconeRun run = new PineconeRun(runtime, table);
        run.setConfigs(configs);
        run.init();
        fillDeleteRunContent(runtime, run, placeholder, unicode);
        runs.add(run);
        return runs;
    }

    /**
     * delete[命令合成-子流程]<br/>
     * Pinecone中删除由delete(Run)处理,build阶段暂不实现
     */
    @Override
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
        return null;
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 从entity属性解析删除条件,最终转换为PineconeRun供delete(Run)执行
     * 参考MongoDB/Milvus模式
     */
    @Override
    public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String... columns) {
        if(null == configs || configs.isEmptyCondition()) {
            if(null == columns || columns.length == 0) {
                columns = new String[]{"id"};
            }
            if(null == configs) {
                configs = new DefaultConfigStore();
            }
            for(String column : columns) {
                configs.and(column, BeanUtil.getFieldValue(obj, column, true));
            }
        }
        return buildDeleteRun(runtime, table, configs, placeholder, unicode);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 将ConditionChain解析并存储到PineconeRun中,后续由delete(Run)使用
     * 参考MongoDB parseCondition/Milvus buildExpression模式
     */
    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        if(run instanceof PineconeRun) {
            PineconeRun pr = (PineconeRun) run;
            ConditionChain chain = pr.getConditionChain();
            List<Object> ids = new ArrayList<>();
            Map<String, Object> filterMap = new LinkedHashMap<>();
            extractConditions(chain, ids, filterMap);
            if(!ids.isEmpty()) {
                List<RunValue> runValues = new ArrayList<>();
                for(Object id : ids) {
                    runValues.add(new RunValue("id", id));
                }
                pr.setRunValues(runValues);
            }
            if(!filterMap.isEmpty()) {
                pr.setFilter(filterMap);
            }
        }
    }

    /**
     * delete[命令执行]<br/>
     * 参考MongoDB/Milvus模式:在Adapter层直接操作Pinecone Client执行删除
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param configs 查询条件及相关设置
     * @param run 最终待执行的命令和参数
     * @return 影响行数
     */
    @Override
    public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run) {
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
        try {
            PineconeRun pr = (PineconeRun) run;
            String tableName = pr.getTableName();
            List<Object> values = pr.getValues();
            Map<String, Object> filterMap = pr.getFilter();

            Pinecone client = client(runtime);
            Index index = client.getIndexConnection(tableName);

            // 优先使用ID列表删除
            if(BasicUtil.isNotEmpty(true, values)) {
                List<String> ids = new ArrayList<>();
                for(Object value : values) {
                    ids.add(String.valueOf(value));
                }
                log.info("{}[action:delete][index:{}][ids:{}]", random, tableName, ids);
                index.deleteByIds(ids);
                result = ids.size();
            }else if(BasicUtil.isNotEmpty(true, filterMap)) {
                // 使用metadata filter删除
                Struct filterStruct = buildFilterStruct(filterMap);
                log.info("{}[action:delete][index:{}][filter:{}]", random, tableName, filterMap);
                index.deleteByFilter(filterStruct);
                result = 1;
            }else {
                log.warn("{}[action:delete][index:{}][无条件,跳过]", random, tableName);
                return -1;
            }

            cmd_success = true;
        } catch (Exception e) {
            log.error("delete exception:", e);
        }
        long millis = System.currentTimeMillis() - fr;
        if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[action:delete][index:{}][执行耗时:{}][影响行数:{}]", random, run.getTableName(), DateUtil.format(millis), LogUtil.format(result, 34));
        }
        if(null != dmListener) {
            dmListener.afterDelete(runtime, random, run, cmd_success, result, millis);
        }
        InterceptorProxy.afterDelete(runtime, random, run, configs, cmd_success, result, millis);
        return result;
    }

    /**
     * 从ConditionChain中提取ID列表和metadata filter
     */
    private void extractConditions(Condition condition, List<Object> ids, Map<String, Object> filterMap) {
        if(condition instanceof ConditionChain) {
            ConditionChain chain = (ConditionChain) condition;
            for(Condition con : chain.getConditions()) {
                extractConditions(con, ids, filterMap);
            }
        }else if(condition instanceof AutoCondition) {
            AutoCondition auto = (AutoCondition) condition;
            String column = auto.getId();
            List<Object> values = auto.getValues();
            Compare compare = auto.getCompare();
            if(null == compare) {
                compare = Compare.EQUAL;
            }
            if(null == column || null == values || values.isEmpty()) {
                return;
            }
            // "id"列作为ID删除
            if("id".equalsIgnoreCase(column)) {
                ids.addAll(values);
            }else {
                // 其他列作为metadata filter
                if(compare == Compare.IN || compare == Compare.NOT_IN) {
                    Map<String, Object> opFilter = new LinkedHashMap<>();
                    opFilter.put(compare == Compare.IN ? "$in" : "$nin", values);
                    filterMap.put(column, opFilter);
                }else {
                    filterMap.put(column, values.get(0));
                }
            }
        }
    }

    /**
     * 将Map转换为Pinecone protobuf Struct的metadata filter
     */
    private Struct buildFilterStruct(Map<String, Object> filterMap) {
        Struct.Builder structBuilder = Struct.newBuilder();
        for(Map.Entry<String, Object> entry : filterMap.entrySet()) {
            Value value = toProtobufValue(entry.getValue());
            if(value != null) {
                structBuilder.putFields(entry.getKey(), value);
            }
        }
        return structBuilder.build();
    }

    @SuppressWarnings("unchecked")
    private Value toProtobufValue(Object value) {
        Value.Builder builder = Value.newBuilder();
        if(value == null) {
            return null;
        }else if(value instanceof String) {
            builder.setStringValue((String) value);
        }else if(value instanceof Number) {
            builder.setNumberValue(((Number) value).doubleValue());
        }else if(value instanceof Boolean) {
            builder.setBoolValue((Boolean) value);
        }else if(value instanceof List) {
            com.google.protobuf.ListValue.Builder listBuilder = com.google.protobuf.ListValue.newBuilder();
            for(Object item : (List<?>) value) {
                Value itemValue = toProtobufValue(item);
                if(itemValue != null) {
                    listBuilder.addValues(itemValue);
                }
            }
            return Value.newBuilder().setListValue(listBuilder.build()).build();
        }else if(value instanceof Map) {
            Struct.Builder structBuilder = Struct.newBuilder();
            for(Map.Entry<String, Object> entry : ((Map<String, Object>) value).entrySet()) {
                Value v = toProtobufValue(entry.getValue());
                if(v != null) {
                    structBuilder.putFields(entry.getKey(), v);
                }
            }
            builder.setStructValue(structBuilder.build());
        }else {
            builder.setStringValue(String.valueOf(value));
        }
        return builder.build();
    }

    private Pinecone client(DataRuntime runtime) {
        return (Pinecone) runtime.getProcessor();
    }
}