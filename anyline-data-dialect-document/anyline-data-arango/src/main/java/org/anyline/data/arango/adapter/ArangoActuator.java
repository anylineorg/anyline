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

import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.entity.CollectionType;
import org.anyline.annotation.AnylineComponent;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.arango.runtime.ArangoRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * ArangoDB 驱动执行器<br/>
 * 负责元数据查询(table/column/index/database 列表)以及通过 AQL 执行的数据操作<br/>
 * <br/>
 * 注意: insert/update/delete 的实际执行在 {@link ArangoAdapter} 中完成, Actuator 主要负责元数据与 AQL 查询
 */
@AnylineComponent("anyline.environment.data.driver.actuator.arango")
public class ArangoActuator implements DriverActuator {

    /**
     * 获取当前执行器支持的适配器类型
     * @return ArangoAdapter 类对象
     */
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return ArangoAdapter.class;
    }

    /**
     * 获取 ArangoDB 数据库连接对象
     * @param runtime 运行时环境，包含 ArangoDB 客户端信息
     * @return ArangoDatabase 数据库对象
     */
    private ArangoDatabase database(DataRuntime runtime) {
        ArangoRuntime rt = (ArangoRuntime) runtime;
        return rt.getDatabase();
    }

    /**
     * 获取执行器优先级，数值越高优先级越高
     * @return 优先级数值，默认为 0
     */
    public int priority() {
        return 0;
    }

    /**
     * 获取数据源（ArangoDB 不使用 JDBC DataSource，返回 null）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @return null
     */
    public DataSource getDataSource(DriverAdapter adapter, DataRuntime runtime) {
        return null;
    }

    /**
     * 获取数据库连接（ArangoDB 不使用 JDBC Connection，返回 null）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param datasource 数据源
     * @return null
     */
    public Connection getConnection(DriverAdapter adapter, DataRuntime runtime, DataSource datasource) {
        return null;
    }

    /**
     * 释放数据库连接（ArangoDB 不使用 JDBC Connection，空实现）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param connection 数据库连接
     * @param datasource 数据源
     */
    public void releaseConnection(DriverAdapter adapter, DataRuntime runtime, Connection connection, DataSource datasource) {
    }

    /**
     * 检查 Schema（ArangoDB 无需 Schema 检查，空实现）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param datasource 数据源
     * @param meta 元数据对象
     * @param <T> 元数据类型
     */
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, DataSource datasource, T meta) {
    }

    /**
     * 检查 Schema（ArangoDB 无需 Schema 检查，空实现）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param meta 元数据对象
     * @param <T> 元数据类型
     */
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, T meta) {
    }

    /**
     * 检查 Schema（ArangoDB 无需 Schema 检查，空实现）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param con 数据库连接
     * @param meta 元数据对象
     * @param <T> 元数据类型
     */
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, Connection con, T meta) {
    }

    // ===== database metadata =====

    /**
     * 获取数据库产品名称
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param product 上一步查询结果
     * @return 数据库产品名称 "ArangoDB"
     */
    public String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product) {
        return "ArangoDB";
    }

    /**
     * 获取数据库版本号
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param version 上一步查询结果
     * @return 数据库版本号
     */
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        if(BasicUtil.isEmpty(version) && null != runtime) {
            try {
                ArangoDatabase db = database(runtime);
                version = db.getVersion().getVersion();
            } catch (Exception ignore) {}
        }
        return version;
    }

    /**
     * 获取数据库列表
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param query 查询条件，支持模糊匹配（% 和 _ 通配符）
     * @param <T> Database 类型
     * @return 数据库列表
     */
    @SuppressWarnings("unchecked")
    public <T extends Database> List<T> databases(DriverAdapter adapter, DataRuntime runtime, Database query) {
        List<T> list = new ArrayList<>();
        try {
            ArangoDatabase db = database(runtime);
            for (String name : db.arango().getDatabases()) {
                if(BasicUtil.isNotEmpty(query.getName())) {
                    String regex = query.getName().replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(name.toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T d = (T) new Database(name);
                list.add(d);
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("databases 异常:", e);
            }
        }
        return list;
    }

    /**
     * 获取 Catalog 列表（ArangoDB 不支持 Catalog，返回空列表）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @return 空列表
     */
    public List<Catalog> catalogs(DriverAdapter adapter, DataRuntime runtime) {
        List<Catalog> list = new ArrayList<>();
        return list;
    }

    /**
     * 获取 Schema 列表（在 ArangoDB 中，Schema 等同于 Database）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @return Schema 列表
     */
    @SuppressWarnings("unchecked")
    public List<Schema> schemas(DriverAdapter adapter, DataRuntime runtime) {
        List<Schema> list = new ArrayList<>();
        try {
            ArangoDatabase db = database(runtime);
            for (String name : db.arango().getDatabases()) {
                list.add(new Schema(name));
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("schemas 异常:", e);
            }
        }
        return list;
    }

    // ===== AQL 查询执行 =====

    /**
     * 执行 AQL 查询，返回数据集
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param system 是否系统表
     * @param action DML 操作类型
     * @param table 表对象
     * @param configs 配置存储
     * @param run 运行对象
     * @param cmd AQL 命令
     * @param values 参数值列表
     * @param columns 查询列
     * @return 数据集
     * @throws Exception 异常
     */
    public DataSet<DataRow> query(DriverAdapter adapter, DataRuntime runtime, String random,
                                  boolean system, ACTION.DML action, Table table,
                                  ConfigStore configs, Run run, String cmd,
                                  List<Object> values, LinkedHashMap<String, Column> columns) throws Exception {
        return adapter.query(runtime, random, system, table, configs, run);
    }

    /**
     * 执行存储过程查询（ArangoDB 不支持存储过程，返回空数据集）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param procedure 存储过程对象
     * @param navi 分页导航
     * @return 空数据集
     * @throws Exception 异常
     */
    public DataSet<DataRow> selects(DriverAdapter adapter, DataRuntime runtime, String random,
                                    Procedure procedure, PageNavi navi) throws Exception {
        return new DataSet<>();
    }

    /**
     * 执行查询，返回 Map 列表
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param configs 配置存储
     * @param run 运行对象
     * @return Map 列表
     * @throws Exception 异常
     */
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random,
                                          ConfigStore configs, Run run) throws Exception {
        return adapter.maps(runtime, random, configs, run);
    }

    /**
     * 执行查询，返回单个 Map（取第一条结果）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param configs 配置存储
     * @param run 运行对象
     * @return 单个 Map，无结果时返回空 Map
     * @throws Exception 异常
     */
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random,
                                   ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = maps(adapter, runtime, random, configs, run);
        if(null != maps && !maps.isEmpty()) {
            return maps.get(0);
        }
        return new HashMap<>();
    }

    /**
     * 执行插入操作
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param data 待插入数据
     * @param configs 配置存储
     * @param run 运行对象
     * @param generatedKey 自增主键 key
     * @param pks 主键列表
     * @return 影响行数
     * @throws Exception 异常
     */
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random,
                       Object data, ConfigStore configs, Run run,
                       String generatedKey, String[] pks) throws Exception {
        return adapter.insert(runtime, random, data, configs, run, pks);
    }

    /**
     * 执行更新操作
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param dest 目标表
     * @param data 更新数据
     * @param configs 配置存储
     * @param run 运行对象
     * @return 影响行数
     * @throws Exception 异常
     */
    public long update(DriverAdapter adapter, DataRuntime runtime, String random,
                       Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        return adapter.update(runtime, random, dest, data, configs, run);
    }

    /**
     * 执行存储过程（ArangoDB 不支持存储过程，返回空列表）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param procedure 存储过程对象
     * @param sql SQL 语句
     * @param inputs 输入参数
     * @param outputs 输出参数
     * @return 空列表
     * @throws Exception 异常
     */
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random,
                                Procedure procedure, String sql, List<Parameter> inputs,
                                List<Parameter> outputs) throws Exception {
        return new ArrayList<>();
    }

    /**
     * 执行通用命令（ArangoDB 中由 Adapter 处理，返回 0）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param configs 配置存储
     * @param run 运行对象
     * @return 0
     * @throws Exception 异常
     */
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random,
                        ConfigStore configs, Run run) throws Exception {
        return 0;
    }

    /**
     * 执行批量命令（ArangoDB 中由 Adapter 处理，返回 0）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param configs 配置存储
     * @param run 运行对象列表
     * @return 0
     * @throws Exception 异常
     */
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random,
                        ConfigStore configs, List<Run> run) throws Exception {
        return 0;
    }

    // ===== 列元数据(从文档采样推断) =====

    /**
     * 根据运行对象获取列元数据（ArangoDB 返回空 Map）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param random 命令组标记
     * @param run 运行对象
     * @param comment 是否需要注释
     * @return 空 Map
     */
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime,
                                                   String random, Run run, boolean comment) {
        return new LinkedHashMap<>();
    }

    /**
     * 通过采样文档推断列元数据<br/>
     * 从集合中采样指定数量的文档，提取字段名和类型信息
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param query 查询条件，需指定表名
     * @param <T> Column 类型
     * @return 列元数据 Map
     * @throws Exception 异常
     */
    @SuppressWarnings("unchecked")
    public <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime,
                                                                boolean create, LinkedHashMap<String, T> previous,
                                                                Column query) throws Exception {
        if(null == previous){
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            com.arangodb.ArangoCollection collection = db.collection(query.getTableName());
            if(null != collection) {
                String aqlTemplate = "FOR doc IN @@collection LIMIT @limit RETURN doc";
                Map<String, Object> bindVars = new HashMap<>();
                bindVars.put("limit", Long.valueOf(ConfigTable.CHECK_METADATA_SAMPLE));

                String inlined = inlineAql(aqlTemplate, query.getTableName(), bindVars);
                ArangoCursor<BaseDocument> cursor = db.query(inlined, BaseDocument.class);
                while (cursor.hasNext()) {
                    BaseDocument doc = cursor.next();
                    Set<String> fields = doc.getProperties().keySet();
                    for (String field : fields) {
                        String up = field.toUpperCase();
                        if (previous.containsKey(up)) {
                            continue;
                        }
                        Object value = doc.getProperties().get(field);
                        if (null != value) {
                            String type = value.getClass().getSimpleName();
                            Column column = new Column(field, type);
                            previous.put(up, (T) column);
                        }
                    }
                }
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("metadata 异常:", e);
            }
        }
        return previous;
    }

    // ===== tables / views =====

    /**
     * 获取表列表（ArangoDB 中表等同于 Collection）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param query 查询条件，支持模糊匹配
     * @param types 表类型过滤
     * @param <T> Table 类型
     * @return 表元数据 Map
     * @throws Exception 异常
     */
    @SuppressWarnings("unchecked")
    public <T extends Table> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime,
                                                             boolean create, LinkedHashMap<String, T> previous,
                                                             Table query, int types) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            Collection<CollectionEntity> entities = db.getCollections();

            for (CollectionEntity entity : entities) {
                if(entity.getIsSystem()) {
                    continue;
                }
                CollectionType ct = entity.getType();
                if(CollectionType.DOCUMENT != ct && CollectionType.EDGES != ct) {
                    continue;
                }

                if(BasicUtil.isNotEmpty(query.getName())) {
                    String regex = query.getName().replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(entity.getName().toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T table = (T) new Table(entity.getName());
                previous.put(entity.getName().toUpperCase(), table);
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("tables 异常:", e);
            }
        }
        return previous;
    }

    /**
     * 获取表列表（返回 List 形式）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param query 查询条件，支持模糊匹配
     * @param types 表类型过滤
     * @param <T> Table 类型
     * @return 表元数据列表
     * @throws Exception 异常
     */
    public <T extends Table> List<T> tables(DriverAdapter adapter, DataRuntime runtime,
                                            boolean create, List<T> previous,
                                            Table query, int types) throws Exception {
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        if(null != previous) {
            for(T t : previous) {
                map.put(t.getName().toUpperCase(), t);
            }
        }
        LinkedHashMap<String, T> result = tables(adapter, runtime, create, map, query, types);
        return new ArrayList<>(result.values());
    }

    /**
     * 获取视图列表
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param query 查询条件，支持模糊匹配
     * @param types 视图类型过滤
     * @param <T> View 类型
     * @return 视图元数据 Map
     * @throws Exception 异常
     */
    @SuppressWarnings("unchecked")
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime,
                                                           boolean create, LinkedHashMap<String, T> previous,
                                                           View query, int types) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            Collection<com.arangodb.entity.ViewEntity> dbViews = db.getViews();
            for (com.arangodb.entity.ViewEntity entity : dbViews) {
                if(BasicUtil.isNotEmpty(query.getName())) {
                    String regex = query.getName().replace("%", ".*").replace("_", ".");
                    if(!RegularUtil.match(entity.getName().toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                        continue;
                    }
                }
                T view = (T) new View(entity.getName());
                previous.put(entity.getName().toUpperCase(), view);
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("views 异常:", e);
            }
        }
        return previous;
    }

    /**
     * 获取视图列表（返回 List 形式）
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param query 查询条件，支持模糊匹配
     * @param types 视图类型过滤
     * @param <T> View 类型
     * @return 视图元数据列表
     * @throws Exception 异常
     */
    public <T extends View> List<T> views(DriverAdapter adapter, DataRuntime runtime,
                                          boolean create, List<T> previous,
                                          View query, int types) throws Exception {
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        if(null != previous) {
            for(T t : previous) {
                map.put(t.getName().toUpperCase(), t);
            }
        }
        LinkedHashMap<String, T> result = views(adapter, runtime, create, map, query, types);
        return new ArrayList<>(result.values());
    }

    // ===== columns (from table list) =====

    /**
     * 获取表的列元数据
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param table 表对象
     * @param cmd SQL 命令（未使用）
     * @param <T> Column 类型
     * @return 列元数据 Map
     * @throws Exception 异常
     */
    public <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime,
                                                               boolean create, LinkedHashMap<String, T> previous,
                                                               Table table, String cmd) throws Exception {
        if(BasicUtil.isNotEmpty(table.getName())) {
            Column q = new Column();
            q.setTable(table.getName());
            return metadata(adapter, runtime, create, previous, q);
        }
        return previous != null ? previous : new LinkedHashMap<>();
    }

    // ===== AQL inline helper =====

    /**
     * 将 AQL 模板中的绑定变量内联为字面量值
     * @param aqlTemplate AQL 模板，包含 @@collection 和 @var 形式的绑定变量
     * @param collectionName 集合名称
     * @param bindVars 绑定变量 Map
     * @return 内联后的完整 AQL 语句
     */
    private String inlineAql(String aqlTemplate, String collectionName, Map<String, Object> bindVars) {
        String result = aqlTemplate;
        result = result.replace("@@collection", "`" + collectionName + "`");
        if(null != bindVars) {
            for(Map.Entry<String, Object> entry : bindVars.entrySet()) {
                String key = "@" + entry.getKey();
                String val = toAqlLiteral(entry.getValue());
                result = result.replace(key + " ", val + " ");
                result = result.replace(key + ")", val + ")");
                result = result.replace(key + "\n", val + "\n");
                result = result.replace(key, val);
            }
        }
        return result;
    }

    /**
     * 将 Java 对象转换为 AQL 字面量表达式
     * @param value Java 对象
     * @return AQL 字面量字符串
     */
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

    // ===== indexes =====

    /**
     * 获取索引列表
     * @param adapter 驱动适配器
     * @param runtime 运行时环境
     * @param create 是否需要创建
     * @param previous 上一步查询结果
     * @param query 查询条件，需指定表名，支持模糊匹配
     * @param <T> Index 类型
     * @return 索引元数据 Map
     * @throws Exception 异常
     */
    @SuppressWarnings("unchecked")
    public <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime,
                                                              boolean create, LinkedHashMap<String, T> previous,
                                                              Index query) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        try {
            ArangoDatabase db = database(runtime);
            com.arangodb.ArangoCollection collection = db.collection(query.getTableName());
            if(null != collection) {
                Collection<com.arangodb.entity.IndexEntity> idxes = collection.getIndexes();
                for (com.arangodb.entity.IndexEntity entity : idxes) {
                    String name = entity.getName();
                    if(BasicUtil.isEmpty(name)) {
                        name = entity.getId();
                    }
                    if(BasicUtil.isNotEmpty(query.getName())) {
                        String regex = query.getName().replace("%", ".*").replace("_", ".");
                        if(!RegularUtil.match(name.toUpperCase(), regex.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            continue;
                        }
                    }
                    Index idx = new Index(name);
                    idx.setTable(query.getTableName());
                    idx.setType(entity.getType().name());
                    previous.put(name.toUpperCase(), (T) idx);
                }
            }
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("indexes 异常:", e);
            }
        }
        return previous;
    }

}