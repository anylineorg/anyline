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

package org.anyline.data.adapter.init;

import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.adapter.EntityAdapter;
import org.anyline.adapter.KeyAdapter;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverAdapter.SQL_BUILD_IN_VALUE;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.entity.Join;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.metadata.type.TypeMetadataAlias;
import org.anyline.data.param.Config;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.auto.init.VirtualTablePrepare;
import org.anyline.data.prepare.text.TextPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.CommandParser;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.entity.generator.GeneratorConfig;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.AnylineException;
import org.anyline.exception.CommandException;
import org.anyline.exception.CommandQueryException;
import org.anyline.exception.CommandUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.TypeMetadataHolder;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.ConvertProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class AbstractDriverAdapter implements DriverAdapter {

    protected DMListener dmListener;
    protected DDListener ddListener;
    protected PrimaryGenerator primaryGenerator;
    protected DriverActuator actuator;

    protected DMListener getListener() {
        return dmListener;
    }

    public void setListener(DMListener listener) {
        this.dmListener = listener;
    }
    public DMListener getDMListener() {
        return this.dmListener;
    }
    public void setListener(DDListener listener) {
        this.ddListener = listener;
    }
    public DDListener getDDListener() {
        return this.ddListener;
    }
    public void setGenerator(PrimaryGenerator generator) {
        this.primaryGenerator = generator;
    }
    public PrimaryGenerator getPrimaryGenerator() {
        return primaryGenerator;
    }
    protected String delimiterFr = "";
    protected String delimiterTo = "";
    //拼写兼容 下划线空格兼容
    protected static Map<String, String> spells = new HashMap<>();

    protected Map<Class<?>, MetadataFieldRefer> refers = new HashMap<>();
    //根据名称定位数据类型
    //protected LinkedHashMap<String, TypeMetadata> alias = new LinkedHashMap();

    static {
        for(StandardTypeMetadata type: StandardTypeMetadata.values()) {
            String name = type.name().toUpperCase();//变量名
            String standard = type.getName().toUpperCase(); //标准SQL类型名
            spells.put(name, standard);
            if(name.contains(" ")) {
                spells.put(name.replace(" ", "_"), standard);
            }
            if(name.contains("_")) {
                spells.put(name.replace("_", " "), standard);
            }
            if(standard.contains(" ")) {
                spells.put(standard.replace(" ", "_"), standard);
            }
            if(standard.contains("_")) {
                spells.put(standard.replace("_", " "), standard);
            }
        }
    }

    @Override
    public void setActuator(DriverActuator actuator) {
        this.actuator = actuator;
    }

    @Override
    public DriverActuator getActuator() {
        return actuator;
    }

    @Override
    public String getDelimiterFr() {
        return this.delimiterFr;
    }

    @Override
    public String getDelimiterTo() {
        return this.delimiterTo;
    }

    @Override
    public void setDelimiter(String delimiter) {
        if(BasicUtil.isNotEmpty(delimiter)) {
            delimiter = delimiter.replaceAll("\\s","");
            if(delimiter.length() == 1) {
                this.delimiterFr = delimiter;
                this.delimiterTo = delimiter;
            }else{
                this.delimiterFr = delimiter.substring(0,1);
                this.delimiterTo = delimiter.substring(1,2);
            }
        }
    }

    /**
     * 对应的兼容模式，有些数据库会兼容oracle或pg,需要分别提供两个JDBCAdapter或者直接依赖oracle/pg的adapter
     * 参考DefaultJDBCAdapterUtil定位adapter的方法
     * @return DatabaseType
     */
    @Override
    public DatabaseType compatible() {
        return null;
    }
    public AbstractDriverAdapter() {
        //当前数据库支持的数据类型,子类根据情况覆盖
        for(StandardTypeMetadata type: StandardTypeMetadata.values()) {
            reg(type, type.refer());
            List<DatabaseType> dbs = type.databaseTypes();
            for(DatabaseType db:dbs) {
                if(db == this.type()) {
                    //column type支持当前db
                    alias(type.getName(), type);
                    alias(type.name(), type);
                    break;
                }
            }
        }
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Refer(0, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Refer( 1,1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BYTES, new TypeMetadata.Refer(0, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Refer(1,1,1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.INT, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Refer(1, 0, 0));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.DATE, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TIME, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.DATETIME, new TypeMetadata.Refer( 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.COLLECTION, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.GEOMETRY, new TypeMetadata.Refer(1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Refer( 1, 1, 1));

        reg(initDatabaseFieldRefer());
        reg(initCatalogFieldRefer());
        reg(initSchemaFieldRefer());
        reg(initTableFieldRefer());
        reg(initTableCommentFieldRefer());
        reg(initMasterTableFieldRefer());
        reg(initTablePartitionFieldRefer());
        reg(initTablePartitionSliceFieldRefer());
        reg(initPartitionTableFieldRefer());
        reg(initEdgeFieldRefer());
        reg(initVertexFieldRefer());
        reg(initColumnFieldRefer());
        reg(initTagFieldRefer());
        reg(initViewFieldRefer());
        reg(initPrimaryKeyFieldRefer());
        reg(initForeignKeyFieldRefer());
        reg(initConstraintFieldRefer());
        reg(initIndexFieldRefer());
        reg(initTriggerFieldRefer());
        reg(initProcedureFieldRefer());
        reg(initFunctionFieldRefer());
        reg(initSequenceFieldRefer());
        reg(initUserFieldRefer());
        reg(initRoleFieldRefer());
        reg(initPrivilegeFieldRefer());
    }

    @Override
    public MetadataFieldRefer refer(DataRuntime runtime, Class<?> type) {
        MetadataFieldRefer refer = refers.get(type);
        if(null == refer) {
            refer = new MetadataFieldRefer(type);
        }
        return refer;
    }

    @Override
    public void reg(MetadataFieldRefer refer) {
        Class<?> metadata = refer.metadata();
        MetadataFieldRefer cur = refers.get(metadata);
        if(null != cur) {
            cur.copy(refer);
        }else {
            refers.put(metadata, refer);
        }
    }

    @Override
    public LinkedHashMap<String, TypeMetadata> alias() {
        return TypeMetadataHolder.gets(type());
    }

    /**
     * 注册数据类型别名(包含对应的标准类型、length/precision/scale等配置)
     * @param alias 数据类型别名
     * @return Config
     */
    @Override
    public TypeMetadata.Refer reg(TypeMetadataAlias alias) {
        TypeMetadata standard = alias.standard();
        if(standard == StandardTypeMetadata.NONE) {
            return null;
        }
        alias(alias.input(), standard);                                        //根据别名
        alias(standard.getName(), standard);                                        //根据实现SQL数据类型名称
        TypeMetadata.Refer refer = alias.refer();
        reg(alias.input(), refer);
        reg(alias.standard(), refer);
        return refer;
    }
    protected void alias(String key, TypeMetadata value) {
        if(null != key && null != value && TypeMetadata.NONE != value) {
            //this.alias.put(key, value);
            TypeMetadataHolder.reg(type(), key, value);

            key = key.replace("_", " ");
            //this.alias.put(key, value);
            TypeMetadataHolder.reg(type(), key, value);

            key = key.replace(" ", "_");
            //this.alias.put(key, value);
            TypeMetadataHolder.reg(type(), key, value);
        }
    }

    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param type 数据类型
     * @param refer 配置项 主要包括数据类型规则以及是否忽略长度、精度
     * @return Config
     */
    @Override
    public TypeMetadata.Refer reg(TypeMetadata type, TypeMetadata.Refer refer) {
        return MetadataReferHolder.reg(type(), type, refer);
    }

    /**
     * 注册数据类型配置
     * 要从配置项中取出每个属性检测合并,不要整个覆盖
     * @param type 类型名称或别名
     * @param config 配置项
     * @return TypeMetadata.Config
     */
    @Override
    public TypeMetadata.Refer reg(String type, TypeMetadata.Refer refer) {
        return MetadataReferHolder.reg(type(), type, refer);
    }

    /* *****************************************************************************************************************
     *
     *                                                     DML
     *
     * =================================================================================================================
     * INSERT            : 插入
     * UPDATE            : 更新
     * SAVE                : 根据情况插入或更新
     * QUERY            : 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
     * EXISTS            : 是否存在
     * COUNT            : 统计
     * EXECUTE            : 执行(原生SQL及存储过程)
     * DELETE            : 删除
     *
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     *                                                     INSERT
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns)
     * [命令合成]
     * public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
     * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
     * public String batchInsertSeparator()
     * public boolean supportInsertPlaceholder()
     * protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
     * protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns)
     * public String generatedKey()
     * [命令执行]
     * long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);
     ******************************************************************************************************************/

    /**
     * insert [调用入口]<br/>
     * 执行前根据主键生成器补充主键值,执行完成后会补齐自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 需要插入入的数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须插入<br/>
     *                -:表示必须不插入<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return 影响行数
     */
    @Override
    public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        if(null == random) {
            random = random(runtime);
        }
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        boolean cmd_success = false;
        swt = InterceptorProxy.prepareInsert(runtime, random, batch, dest, data, configs, columns);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.prepareInsert(runtime, random, batch, dest, data, columns);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(data instanceof DataSet) {
            DataSet set = (DataSet)data;
            Map<String, Object> tags = set.getTags();
            if(null != tags && !tags.isEmpty()) {
                LinkedHashMap<String, PartitionTable> partitions = partitions(runtime, random, false, new MasterTable(dest), tags, null);
                if(partitions.size() != 1) {
                    String msg = "分区表定位异常,主表:" + dest + ",标签:" + BeanUtil.map2json(tags) + ",分区表:" + BeanUtil.object2json(partitions.keySet());
                    if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                        throw new CommandUpdateException(msg);
                    }else{
                        log.error(msg);
                        return -1;
                    }
                }
                dest = partitions.values().iterator().next();
            }
        }
        Run run = buildInsertRun(runtime, batch, dest, data, configs, true, true, columns);
        //提前设置好columns,到了adapter中需要手动检测缓存
        if(ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
            dest.setColumns(columns(runtime, random, false, dest, false));
        }
        if(null == run) {
            return 0;
        }

        long cnt = 0;
        long fr = System.currentTimeMillis();
        long millis = -1;

        swt = InterceptorProxy.beforeInsert(runtime, random, run, dest, data, configs, columns);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.beforeInsert(runtime, random, run, dest, data, columns);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        cnt = insert(runtime, random, data, configs, run, null);
        if (null != dmListener) {
            dmListener.afterInsert(runtime, random, run, cnt, dest, data, columns, cmd_success, cnt, millis);
        }
        InterceptorProxy.afterInsert(runtime, random, run, dest, data, configs, columns, cmd_success, cnt, System.currentTimeMillis() - fr);
        return cnt;
    }

    /**
     * insert into table select * from table
     * 与query参数一致
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 插入表
     * @param prepare 查询表
     * @param configs 查询条件及相关配置
     * @param obj 查询条件
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    @Override
    public long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions)", 37));
        }
        return 0;
    }

    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 需要插入的数据
     * @param configs configs
     * @param placeholder 占位符
     * @param unicode 编码
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = null;
        if(null == obj) {
            return null;
        }
        if(null == dest) {
            dest = DataSourceUtil.parseDest(null, obj, configs);
        }

        if(obj instanceof Collection) {
            Collection list = (Collection) obj;
            if(null != list && !list.isEmpty()) {
                run = createInsertRunFromCollection(runtime, batch, dest, list, configs, placeholder, unicode, columns);
            }
        }else {
            run = createInsertRun(runtime, dest, obj, configs, placeholder, unicode, columns);
        }
        convert(runtime, configs, run);
        return run;
    }

    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param prepare 查询
     * @param configs 过滤条件及相关配置
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String... conditions) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String... conditions)", 37));
        }
        return null;
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param set 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)", 37));
        }
    }
    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param configs configs
     * @param placeholder 占位符
     * @param unicode 编码
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)", 37));
        }
    }

    /**
     * 插入子表前 检测并创建子表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表
     * @param configs ConfigStore
     */
    @Override
    public void fillInsertCreateTemplate(DataRuntime runtime, Run run, PartitionTable dest, ConfigStore configs) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillInsertCreateTemplate(DataRuntime runtime, Run run, PartitionTable dest, ConfigStore configs)", 37));
        }
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 确认需要插入的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj  Entity或DataRow
     * @param batch  是否批量，批量时不检测值是否为空
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须插入<br/>
     *                -:表示必须不插入<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch) {
        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();/*确定需要插入的列*/
        if(null == obj) {
            return new LinkedHashMap<>();
        }
        if(obj instanceof Map && !(obj instanceof DataRow)) {
            obj = new DataRow(KeyAdapter.KEY_CASE.SRC, (Map)obj);
        }
        LinkedHashMap<String, Column> mastKeys = new LinkedHashMap<>();        // 必须插入列
        List<String> ignores = new ArrayList<>();        // 必须不插入列
        List<String> factKeys = new ArrayList<>();        // 根据是否空值

        boolean each = true;//是否需要从row中查找列
        if(null != columns && !columns.isEmpty()) {
            each = false;
            cols = new LinkedHashMap<>();
            for(String column:columns) {
                if(BasicUtil.isEmpty(column)) {
                    continue;
                }
                if(column.startsWith("+")) {
                    column = column.substring(1);
                    mastKeys.put(column.toUpperCase(), new Column(column));
                    each = true;
                }else if(column.startsWith("-")) {
                    column = column.substring(1);
                    ignores.add(column);
                    each = true;
                }else if(column.startsWith("?")) {
                    column = column.substring(1);
                    factKeys.add(column);
                    each = true;
                }
                cols.put(column.toUpperCase(), new Column(column));
            }
        }
        if(each) {
            // 是否插入null及""列
            boolean isInsertNullColumn =  false;
            boolean isInsertEmptyColumn = false;
            DataRow row = null;
            if(obj instanceof DataRow) {
                row = (DataRow)obj;
                mastKeys.putAll(row.getUpdateColumns(true));

                ignores.addAll(row.getIgnoreUpdateColumns());
                cols = row.getColumns();

                isInsertNullColumn = row.isInsertNullColumn();
                isInsertEmptyColumn = row.isInsertEmptyColumn();

            }else{
                isInsertNullColumn = ConfigStore.IS_INSERT_NULL_FIELD(configs);
                isInsertEmptyColumn = ConfigStore.IS_INSERT_EMPTY_FIELD(configs);
                if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
                    cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.INSERT));
                }else {
                    cols = new LinkedHashMap<>();
                    List<Field> fields = ClassUtil.getFields(obj.getClass(), false, false);
                    for (Field field : fields) {
                        Class clazz = field.getType();
                        if (clazz == String.class || clazz == Date.class || ClassUtil.isPrimitiveClass(clazz)) {
                            cols.put(field.getName().toUpperCase(), new Column(field.getName()));
                        }
                    }
                }
            }
            if(batch) {
                isInsertNullColumn = true;
                isInsertEmptyColumn = true;
            }

            if(log.isDebugEnabled()) {
                log.debug("[confirm insert columns][columns:{}]", cols);
            }
            BeanUtil.removeAll(ignores, columns);
            for(String ignore:ignores) {
                cols.remove(ignore.toUpperCase());
            }
            if(log.isDebugEnabled()) {
                log.debug("[confirm insert columns][ignores:{}]", ignores);
            }
            List<String> keys = Column.names(cols);
            for(String key:keys) {
                if(mastKeys.containsKey(key.toUpperCase())) {
                    // 必须插入
                    continue;
                }
                Object value = null;
                if(!(obj instanceof Map) && EntityAdapterProxy.hasAdapter(obj.getClass())) {
                    value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
                }else{
                    value = BeanUtil.getFieldValue(obj, key, true);
                }

                if(null == value) {
                    if(factKeys.contains(key)) {
                        cols.remove(key);
                        continue;
                    }
                    if(!isInsertNullColumn) {
                        cols.remove(key);
                        continue;
                    }
                }else if(BasicUtil.isEmpty(true, value)) {
                    if(factKeys.contains(key)) {
                        cols.remove(key);
                        continue;
                    }
                    if(!isInsertEmptyColumn) {
                        cols.remove(key);
                        continue;
                    }
                }

            }
        }
        if(log.isDebugEnabled()) {
            log.debug("[confirm insert columns][result:{}]", cols);
        }
        cols = checkMetadata(runtime, dest, configs, cols);
         return cols;
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 批量插入数据时,多行数据之间分隔符
     * @return String
     */
    @Override
    public String batchInsertSeparator() {
        return ",";
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 插入数据时是否支持占位符
     * @return boolean
     */
    @Override
    public boolean supportInsertPlaceholder() {
        return true;
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 设置主键值
     * @param obj obj
     * @param value value
     */
    protected void setPrimaryValue(Object obj, Object value) {
        if(null == obj) {
            return;
        }
        if(obj instanceof DataRow) {
            DataRow row = (DataRow)obj;
            row.setPrimaryValue(value);
        }else{
            Column key = EntityAdapterProxy.primaryKey(obj.getClass());
            Field field = EntityAdapterProxy.field(obj.getClass(), key);
            BeanUtil.setFieldValue(obj, field, value);
        }
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 根据entity创建 INSERT RunPrepare由buildInsertRun调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Run createInsertRun(DataRuntime runtime, Table dest, Object obj, List<String> columns)", 37));
        }
        return null;
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 根据collection创建 INSERT RunPrepare由buildInsertRun调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 对象集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = new TableRun(runtime, dest);
        run.setBatch(batch);
        if(null == list || list.isEmpty()) {
            throw new CommandException("空数据");
        }
        Object first = list.iterator().next();

        if(BasicUtil.isEmpty(dest)) {
            throw new CommandException("未指定表");
        }
        /*确定需要插入的列*/
        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        for(Object item:list) {
            cols.putAll(confirmInsertColumns(runtime, dest, item, configs, columns, true));
            if(!ConfigStore.IS_CHECK_ALL_INSERT_COLUMN(configs)) {
                break;
            }
        }
        if(null == cols || cols.isEmpty()) {
            throw new CommandException("未指定列(DataRow或Entity中没有需要插入的属性值)["+first.getClass().getName()+":"+BeanUtil.object2json(first)+"]");
        }
        run.setInsertColumns(cols);
        run.setVol(cols.size());
        fillInsertContent(runtime, run, dest, list, configs, placeholder, unicode, cols);

        return run;
    }

    /**
     * insert [after]<br/>
     * 执行insert后返回自增主键的key
     * @return String
     */
    @Override
    public String generatedKey() {
        return null;
    }

    /**
     * insert [命令执行]
     * <br/>
     * 执行完成后会补齐自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param data data
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param pks 需要返回的主键
     * @return 影响行数
     */
    @Override
    public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks) {
        long cnt = 0;
        int batch = run.getBatch();
        String action = "insert";
        if(batch > 1) {
            action = "batch insert";
        }
        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][action:{}][table:{}][不具备执行条件]", action, run.getTableName());
            }
            return -1;
        }
        String cmd = run.getFinalInsert();
        if(run.isEmpty()) {
            log.warn("[不具备执行条件][action:{}][table:{}]", action, run.getTable());
            return -1;
        }
        if(null != configs) {
            configs.add(run);
        }
        List<Object> values = run.getValues();
        long fr = System.currentTimeMillis();
        /*执行SQL*/
        if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
            if(batch > 1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
                log.info("{}[action:{}][table:{}][cmd:\n{}\n]\n[param size:{}]", random, action, run.getTable(), cmd, values.size());
            }else {
                log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
        }
        long millis = -1;

        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return -1;
        }
        try {
            cnt = actuator.insert(this, runtime, random, data, configs, run, generatedKey(), pks);
            millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}]{}", random, action, run.getTable(), DateUtil.format(millis), run.log(ACTION.DML.INSERT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.INSERT, run, cmd, values, null, true, cnt, millis);
                    }
                }
            }
            if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
                String qty = LogUtil.format(cnt, 34);
                if(batch > 1) {
                    qty = LogUtil.format("约"+cnt, 34);
                }
                log.info("{}[action:{}][table:{}][执行耗时:{}][影响行数:{}]", random, action, run.getTable(), DateUtil.format(millis), qty);
            }
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("insert 异常:", e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:{}][table:{}]{}", random, LogUtil.format("插入异常:", 33)+e, action, run.getTable(), run.log(ACTION.DML.INSERT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                CommandUpdateException ex = new CommandUpdateException("insert异常:"+e.toString(), e);
                ex.setCmd(cmd);
                ex.setValues(values);
                throw ex;
            }

        }
        return cnt;
    }

    /**
     * 是否支持返回自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs configs中也可能禁用返回
     * @return boolean
     */
    @Override
    public boolean supportKeyHolder(DataRuntime runtime, ConfigStore configs) {
        if(null != configs && !configs.supportKeyHolder()) {
            return false;
        }
        return true;
    }

    /**
     * 自增主键值keys
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs configs中也可能禁用返回
     * @return keys
     */
    @Override
    public List<String> keyHolders(DataRuntime runtime, ConfigStore configs) {
        if(null != configs) {
            return configs.keyHolders();
        }
        return new ArrayList<>();
    }
    /* *****************************************************************************************************************
     *                                                     UPDATE
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns)
     * [命令合成]
     * Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
     * Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns)
     * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
     * [命令执行]
     * long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run)
     ******************************************************************************************************************/
    /**
     * UPDATE [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param configs 条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return 影响行数
     */
    @Override
    public long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        boolean cmd_success = false;
        if(null == random) {
            random = random(runtime);
        }
        swt = InterceptorProxy.prepareUpdate(runtime, random, batch, dest, data, configs, columns);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.prepareUpdate(runtime, random, batch, dest, data, configs, columns);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null == data) {
            if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                throw new CommandUpdateException("更新空数据");
            }else{
                log.error("更新空数据");
            }
        }
        long result = 0;
        if(data instanceof Collection) {
            if(batch <= 1) {
                Collection list = (Collection) data;
                for (Object item : list) {
                    ConfigStore cfg = new DefaultConfigStore();
                    cfg.copyProperty(configs);
                    cfg.and(configs);
                    result += update(runtime, random, 0, dest, item, cfg, columns);
                }
                return result;
            }
        }

        Run run = buildUpdateRun(runtime, batch, dest, data, configs, true, true, columns);

        if(run.isEmptyCondition()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][没有更新条件][dest:"+dest+"]");
            }
            return -1;
        }

        //提前设置好columns,到了adapter中需要手动检测缓存
        if(ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
            dest.setColumns(columns(runtime, null, false, dest, false));
        }
        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
            }
            return -1;
        }
        //String sql = run.getFinalUpdate();
        /*if(BasicUtil.isEmpty(sql)) {
            log.warn("[不具备更新条件][dest:{}]", dest);
            return -1;
        }
        List<Object> values = run.getValues();*/
        long fr = System.currentTimeMillis();
        /*执行SQL*/
        /*if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
            log.info("{}[cmd:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
        }*/
        long millis = -1;
        swt = InterceptorProxy.beforeUpdate(runtime, random, run, dest, data, configs, columns);
        if (swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if (null != dmListener) {
            swt = dmListener.beforeUpdate(runtime, random, run, dest, data, columns);
        }
        if (swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        result = update(runtime, random, dest, data, configs, run);
        cmd_success = true;
        millis = System.currentTimeMillis() - fr;
        if (null != dmListener) {
            dmListener.afterUpdate(runtime, random, run, result, dest, data, columns, cmd_success, result, millis);
        }
        InterceptorProxy.afterUpdate(runtime, random, run, dest, data, configs, columns, cmd_success, result, System.currentTimeMillis() - fr);
        return result;
    }

    /**
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 一般通过TableBuilder生成
     * @param data K-VariableValue 更新值key:需要更新的列 value:通常是关联表的列用VariableValue表示，也可以是常量
     * @return 影响行数
     */
    @Override
    public long update(DataRuntime runtime, String random, RunPrepare prepare, DataRow data, ConfigStore configs, String ... conditions) {
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        boolean cmd_success = false;
        if(null == random) {
            random = random(runtime);
        }
        swt = InterceptorProxy.prepareUpdate(runtime, random, prepare, data, configs);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.prepareUpdate(runtime, random, prepare, data, configs);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null == data || data.isEmpty()) {
            throw new CommandUpdateException("更新空数据");
        }
        long result = -1; 

        Run run = buildUpdateRun(runtime, prepare, data, configs, true, true, conditions);

        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件]");
            }
            return -1;
        }
        long fr = System.currentTimeMillis();
        long millis = -1;
        swt = InterceptorProxy.beforeUpdate(runtime, random, run, prepare, data, configs);
        if (swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if (null != dmListener) {
            swt = dmListener.beforeUpdate(runtime, random, run, prepare, data, configs);
        }
        if (swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        result = update(runtime, random, "", data, configs, run);
        cmd_success = true;
        millis = System.currentTimeMillis() - fr;
        if (null != dmListener) {
            dmListener.afterUpdate(runtime, random, run, result, prepare, data, configs, cmd_success, result, millis);
        }
        InterceptorProxy.afterUpdate(runtime, random, run, prepare, data, configs, cmd_success, result, System.currentTimeMillis() - fr);
        return result;
    }

    /**
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 一般通过TableBuilder生成
     * @param data K-VariableValue 更新值key:需要更新的列 value:通常是关联表的列用VariableValue表示，也可以是常量
     * @return 影响行数
     */
    @Override
    public Run buildUpdateRun(DataRuntime runtime, RunPrepare prepare, DataRow data, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        Run run = initQueryRun(runtime, prepare);
        init(runtime, run, configs, conditions);
        if(run.checkValid()) {
            fillUpdateContent(runtime, (TableRun) run, data, configs, placeholder, unicode);
        }
        return run;
    }
    @Override
    public void fillUpdateContent(DataRuntime runtime, TableRun run, StringBuilder builder, DataRow data, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillUpdateContent(DataRuntime runtime, TableRun run, StringBuilder builder, DataRow data, ConfigStore configs)", 37));
        }
    }

    /**
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj Entity或DtaRow
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = null;
        if(null == obj) {
            return null;
        }
        if(null == dest) {
            dest = DataSourceUtil.parseDest(null, obj, configs);
        }
        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        if(null != columns) {
            for(String column:columns) {
                cols.put(column.toUpperCase(), new Column(column));
            }
        }
        if(obj instanceof DataRow) {
        }else if(obj instanceof Map) {
            obj = new DataRow((Map)obj);
        }
        if(obj instanceof Collection) {
            run = buildUpdateRunFromCollection(runtime, batch, dest, (Collection)obj, configs, placeholder, unicode, cols);
        }else if(obj instanceof DataRow) {
            run = buildUpdateRunFromDataRow(runtime, dest, (DataRow)obj, configs, placeholder, unicode, cols);
        }else{
            run = buildUpdateRunFromEntity(runtime, dest, obj, configs, placeholder, unicode, cols);
        }
        convert(runtime, configs, run);
        buildUpdateRunLimit(runtime, run);
        return run;
    }

    /**
     *
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return Run
     */
    @Override
    public Run buildUpdateRunLimit(DataRuntime runtime, Run run) {
        return run;
    }

    /**
     *
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj Entity或DtaRow
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run
     */
    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        TableRun run = new TableRun(runtime, dest);
        run.setOriginType(2);
        StringBuilder builder = run.getBuilder();
        // List<Object> values = new ArrayList<Object>();
        List<String> list = new ArrayList<>();
        for(Column column:columns.values()) {
            list.add(column.getName());
        }
        LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, obj, configs, list);

        if(null == configs) {
            configs = new DefaultConfigStore();
        }
        List<String> primaryKeys = configs.keys();
        if(primaryKeys.isEmpty()) {
            if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
                primaryKeys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
            } else {
                primaryKeys = new ArrayList<>();
                primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
            }
        }
        /*if(primaryKeys.isEmpty()) {
            throw new SQLUpdateException("[更新异常][更新条件为空, update方法不支持更新整表操作]");
        }*/
        // 不更新主键 除非显示指定
        LinkedHashMap<String, Column> configColumns = configs.getColumns();
        for(String pk:primaryKeys) {
            pk = pk.toUpperCase();
            if(!columns.containsKey(pk) && !columns.containsKey("+"+pk) && !configColumns.containsKey(pk)) {
                cols.remove(pk.toUpperCase());
            }
        }
        //不更新默认主键  除非显示指定
        String defaultPk = DataRow.DEFAULT_PRIMARY_KEY.toUpperCase();
        if(!columns.containsKey(defaultPk) && !columns.containsKey("+"+defaultPk) && !configColumns.containsKey(defaultPk)) {
            cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
        }
        boolean isReplaceEmptyNull = ConfigStore.IS_REPLACE_EMPTY_NULL(configs);
        cols = checkMetadata(runtime, dest, configs, cols);

        List<String> updateColumns = new ArrayList<>();
        /*构造SQL*/
        if(!cols.isEmpty()) {
            builder.append("UPDATE ");
            name(runtime, builder, dest);
            builder.append(" SET").append(BR_TAB);
            boolean first = true;
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

                    if(!first) {
                        builder.append(", ");
                    }
                    delimiter(builder, key).append(" = ").append(value).append(BR_TAB);
                    first = false;
                }else{
                    if("NULL".equals(value)) {
                        value = null;
                    }else if("".equals(value) && isReplaceEmptyNull) {
                        value = null;
                    }
                    boolean chk = true;
                   /* if(null == value) {
                        if(!IS_UPDATE_NULL_FIELD(configs)) {
                            chk = false;
                        }
                    }else */if("".equals(value)) {
                        if(!ConfigStore.IS_UPDATE_EMPTY_FIELD(configs)) {
                            chk = false;
                        }
                    }
                    if(chk) {
                        if(!first) {
                            builder.append(", ");
                        }
                        first = false;
                        delimiter(builder, key).append(" = ?").append(BR_TAB);
                        updateColumns.add(key);
                        Compare compare = Compare.EQUAL;
                        if(isMultipleValue(runtime, run, key)) {
                            compare = Compare.IN;
                        }
                        addRunValue(runtime, run, compare, column, value);
                    }
                }
            }
            builder.append(BR);
            if(configs.isEmptyCondition()) {
                for (String pk : primaryKeys) {
                    if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
                        Field field = EntityAdapterProxy.field(obj.getClass(), pk);
                        configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, pk, BeanUtil.getFieldValue(obj, field));
                    } else {
                        configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, pk, BeanUtil.getFieldValue(obj, pk, true));
                    }
                }
            }

            //builder.append("\nWHERE 1=1").append(BR_TAB);
            run.setConfigStore(configs);
            run.init();
            run.appendCondition(this, true, placeholder, unicode);
        }
        run.setUpdateColumns(updateColumns);

        return run;
    }

    /**
     *
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param row DtaRow
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run
     */
    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        //注意columns中可能含 +-号
        TableRun run = new TableRun(runtime, dest);
        run.setOriginType(1);
        StringBuilder builder = run.getBuilder();

        // List<Object> values = new ArrayList<Object>();
        /*确定需要更新的列*/
        LinkedHashMap<String, Column> cols = confirmUpdateColumns(runtime, dest, row, configs, Column.names(columns));

        if(null == configs) {
            configs = new DefaultConfigStore();
        }
        List<String> primaryKeys = configs.keys();
        if(primaryKeys.isEmpty()) {
            primaryKeys.addAll(row.getPrimaryKeys());
        }
        if(primaryKeys.isEmpty()) {
            throw new CommandUpdateException("[更新异常][更新条件为空, update方法不支持更新整表操作]");
        }
        if(configs.isEmptyCondition()) {
            //没有其他条件时添加 主键作条件
            for (String pk : primaryKeys) {
                Object pv = row.get(pk);
                pv = convert(runtime, cols.get(pk.toUpperCase()), pv); //统一调用
                if (null != pv) {
                    configs.and(Compare.EMPTY_VALUE_SWITCH.SRC, pk, pv);
                }
                /*builder.append(" AND ");
                delimiter(builder, pk).append(" = ?");
                updateColumns.add(pk);
                addRunValue(runtime, run, Compare.EQUAL, new Column(pk), row.get(pk));*/
            }
        }

        // 不更新主键 除非显示指定
        LinkedHashMap<String, Column> configColumns = configs.getColumns();
        for(String pk:primaryKeys) {
            pk = pk.toUpperCase();
            if(!columns.containsKey(pk) && !columns.containsKey("+"+pk) && !configColumns.containsKey(pk)) {
                cols.remove(pk.toUpperCase());
            }
        }
        //primaryKeys.addAll(row.getPrimaryKeys()); 时已经检测过一次了
        //不更新默认主键  除非显示指定
       /* String defaultPk = DataRow.DEFAULT_PRIMARY_KEY.toUpperCase();
        if(!columns.containsKey(defaultPk) && !columns.containsKey("+"+defaultPk) && !configColumns.containsKey(defaultPk)) {
            cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
        }*/

        boolean replaceEmptyNull = row.isReplaceEmptyNull();

        List<String> updateColumns = new ArrayList<>();
        /*构造SQL*/

        if(!cols.isEmpty()) {
            builder.append("UPDATE ");
            name(runtime, builder, dest);
            builder.append(" SET").append(BR_TAB);
            boolean first = true;
            for(Column col:cols.values()) {
                String key = col.getName();
                Object value = row.get(key);
                if(!first) {
                    builder.append(", ");
                }
                first = false;
                //if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ) {
                if(BasicUtil.checkEl(value+"")) {
                    String str = value.toString();
                    value = str.substring(2, str.length()-1);
                    delimiter(builder, key).append(" = ").append(value).append(BR_TAB);
                }else{
                    delimiter(builder, key).append(" = ");
                    convert(runtime, builder, value, col, placeholder, unicode, configs);
                    builder.append(BR_TAB);
                    if("NULL".equals(value)) {
                        value = null;
                    }else if("".equals(value) && replaceEmptyNull) {
                        value = null;
                    }
                    updateColumns.add(key);
                    Compare compare = Compare.EQUAL;
                    addRunValue(runtime, run, compare, col, value);
                }
            }
            builder.append(BR);
            //builder.append("\nWHERE 1=1").append(BR_TAB);
            run.setConfigStore(configs);
             run.init();
            run.appendCondition(this, true, placeholder, unicode);
        }
        run.setUpdateColumns(updateColumns);
        return run;
    }

    /**
     *
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list Collection
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run
     */
    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode,  LinkedHashMap<String, Column> columns) {
        TableRun run = new TableRun(runtime, dest);
        run.setOriginType(1);
        if (null == list || list.isEmpty()) {
            return run;
        }
        if(null == configs) {
            configs = new DefaultConfigStore();
        }

        LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
        List<String> primaryKeys = new ArrayList<>();

        boolean replaceEmptyNull = false;
        for(Object item:list) {
            if(item instanceof DataRow) {
                DataRow row = (DataRow)item;
                primaryKeys = row.getPrimaryKeys();
                cols.putAll(confirmUpdateColumns(runtime, dest, row, configs, Column.names(columns)));
                replaceEmptyNull = row.isReplaceEmptyNull();
            }else{
                List<String> ll = new ArrayList<>();
                for(Column column:columns.values()) {
                    ll.add(column.getName());
                }
                cols.putAll(confirmUpdateColumns(runtime, dest, item, configs, ll));
                if(EntityAdapterProxy.hasAdapter(item.getClass())) {
                    primaryKeys.addAll(EntityAdapterProxy.primaryKeys(item.getClass()).keySet());
                }else{
                    primaryKeys = new ArrayList<>();
                    primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
                }
                replaceEmptyNull = ConfigStore.IS_REPLACE_EMPTY_NULL(configs);
            }
            if(!ConfigStore.IS_CHECK_ALL_UPDATE_COLUMN(configs)) {
                break;
            }
        }
        cols = checkMetadata(runtime, dest, configs, cols);
        StringBuilder builder = run.getBuilder();
        List<String> keys = configs.keys();
        if(!keys.isEmpty()) {
            primaryKeys = keys;
        }
        if(primaryKeys.isEmpty()) {
            throw new CommandUpdateException("[更新异常][更新条件为空, update方法不支持更新整表操作]");
        }
        LinkedHashMap<String, Column> configColumns = configs.getColumns();
        // 不更新主键 除非显示指定
        for(String pk:primaryKeys) {
            pk = pk.toUpperCase();
            if(!columns.containsKey(pk) && !columns.containsKey("+"+pk) && !configColumns.containsKey(pk)) {
                cols.remove(pk.toUpperCase());
            }
        }
        //不更新默认主键  除非显示指定
        String defaultPk = DataRow.DEFAULT_PRIMARY_KEY.toUpperCase();
        if(!columns.containsKey(defaultPk) && !columns.containsKey("+"+defaultPk)&& !configColumns.containsKey(defaultPk)) {
            cols.remove(DataRow.DEFAULT_PRIMARY_KEY.toUpperCase());
        }
        List<String> updateColumns = new ArrayList<>();
        /*构造SQL*/

        if(!cols.isEmpty()) {
            builder.append("UPDATE ");
            name(runtime, builder, dest);
            builder.append(" SET ");
            boolean start = true;
            for(Column col:cols.values()) {
                String key = col.getName();
                if(!start) {
                    builder.append(", ");
                }
                start = false;
                builder.append(key);
                builder.append(" = ");
                convert(runtime, builder, null, col, placeholder, unicode, configs);
            }
            start = true;
            for (String pk : primaryKeys) {
                if(start) {
                    builder.append(" WHERE ");
                }else {
                    builder.append(" AND ");
                }
                delimiter(builder, pk).append(" = ?");
                start = false;
            }
        }
        run.setUpdateColumns(updateColumns);
        List<RunValue> values = new ArrayList<>();
        for(Object item:list) {
            for(Column col:cols.values()) {
                String key = col.getName();
                Object value = BeanUtil.getFieldValue(item, key, true);
                //if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ) {
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
                values.add(new RunValue(key, value));
            }

            for (String pk : primaryKeys) {
                values.add(new RunValue(pk, BeanUtil.getFieldValue(item, pk, true)));
            }
        }
        run.setBatch(batch);
        run.setVol(cols.size() + primaryKeys.size());
        run.setRunValues(values);
        return run;
    }

    /**
     * 确认需要更新的列
     * @param dest 表
     * @param row DataRow
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns) {
        LinkedHashMap<String, Column> cols = null;/*确定需要更新的列*/
        if(null == row) {
            return new LinkedHashMap<>();
        }
        boolean each = true;//是否需要从row中查找列
        List<String> conditions = new ArrayList<>()                            ; // 更新条件
        if(null == columns || columns.isEmpty()) {
            if(null != configs) {
                columns = configs.columns();
            }
        }
        LinkedHashMap<String, Column> masters = row.getUpdateColumns(true)        ; // 必须更新列
        List<String> ignores = BeanUtil.copy(row.getIgnoreUpdateColumns())    ; // 必须不更新列
        List<String> factKeys = new ArrayList<>()                            ; // 根据是否空值
        BeanUtil.removeAll(ignores, columns);
        if(null != configs) {
            BeanUtil.removeAll(configs.excludes(), columns);
        }
        if(null != columns && !columns.isEmpty()) {
            each = false;
            cols = new LinkedHashMap<>();
            for(String column:columns) {
                if(BasicUtil.isEmpty(column)) {
                    continue;
                }
                if(column.startsWith("+")) {
                    column = column.substring(1);
                    masters.put(column.toUpperCase(), new Column(column));
                    each = true;
                }else if(column.startsWith("-")) {
                    column = column.substring(1);
                    ignores.add(column);
                    each = true;
                }else if(column.startsWith("?")) {
                    column = column.substring(1);
                    factKeys.add(column);
                    each = true;
                }
                cols.put(column.toUpperCase(), new Column(column));
            }
        }else if(null != masters && !masters.isEmpty()) {
            each = false;
            cols = masters;
        }
        if(each) {
            cols = row.getUpdateColumns(true);
            cols.putAll(masters);
            // 是否更新null及""列
            boolean isUpdateNullColumn = row.isUpdateNullColumn();
            boolean isUpdateEmptyColumn = row.isUpdateEmptyColumn();
            List<String> keys = Column.names(cols);
            for(String key:keys) {
                if(masters.containsKey(key)) {
                    // 必须更新
                    continue;
                }

                Object value = row.get(key);
                if(null == value) {
                    if(factKeys.contains(key)) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                    if(!isUpdateNullColumn) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                }else if("".equals(value.toString().trim())) {
                    if(factKeys.contains(key)) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                    if(!isUpdateEmptyColumn) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                }

            }
        }
        if(null != ignores) {
            for(String ignore:ignores) {
                cols.remove(ignore.toUpperCase());
            }
        }
        cols = checkMetadata(runtime, dest, configs, cols);
        return cols;
    }

    /**
     * 确认需要更新的列
     * @param dest 表
     * @param obj Object
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        LinkedHashMap<String, Column> cols = null;/*确定需要更新的列*/
        if(null == obj) {
            return new LinkedHashMap<>();
        }

        if(obj instanceof Map && !(obj instanceof DataRow)) {
            obj = new DataRow(KeyAdapter.KEY_CASE.SRC, (Map)obj);
        }
        boolean each = true;//是否需要从row中查找列
        List<String> conditions = new ArrayList<>()                            ; // 更新条件
        LinkedHashMap<String, Column> masters = new LinkedHashMap<>()        ; // 必须更新列
        List<String> ignores = new ArrayList<>()    ; // 必须不更新列
        List<String> factKeys = new ArrayList<>()                            ; // 根据是否空值
        BeanUtil.removeAll(ignores, columns);

        if(null != columns && !columns.isEmpty()) {
            each = false;
            cols = new LinkedHashMap<>();
            for(String column:columns) {
                if(BasicUtil.isEmpty(column)) {
                    continue;
                }
                if(column.startsWith("+")) {
                    column = column.substring(1);
                    masters.put(column.toUpperCase(), new Column(column));
                    each = true;
                }else if(column.startsWith("-")) {
                    column = column.substring(1);
                    ignores.add(column);
                    each = true;
                }else if(column.startsWith("?")) {
                    column = column.substring(1);
                    factKeys.add(column);
                    each = true;
                }
                cols.put(column.toUpperCase(), new Column(column));
            }
        }else if(null != masters && !masters.isEmpty()) {
            each = false;
            cols = masters;
        }
        if(each) {
            // 是否更新null及""列
            boolean isUpdateNullColumn = ConfigStore.IS_UPDATE_NULL_FIELD(configs);
            boolean isUpdateEmptyColumn = ConfigStore.IS_UPDATE_EMPTY_FIELD(configs);
            cols = new LinkedHashMap<>();
            if(obj instanceof DataRow) {
                DataRow row = (DataRow)obj;
                masters.putAll(row.getUpdateColumns(true));
                ignores.addAll(row.getIgnoreUpdateColumns());
                cols = row.getColumns();
                isUpdateNullColumn = row.isUpdateNullColumn();
                isUpdateEmptyColumn = row.isUpdateEmptyColumn();
            } else {
                cols.putAll(EntityAdapterProxy.columns(obj.getClass(), EntityAdapter.MODE.UPDATE)); ;
            }
            cols.putAll(masters);
            List<String> keys = Column.names(cols);
            for(String key:keys) {
                if(masters.containsKey(key)) {
                    // 必须更新
                    continue;
                }
                Object value = BeanUtil.getFieldValue(obj, key, true);
                if(null == value) {
                    if(factKeys.contains(key)) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                    if(!isUpdateNullColumn) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                }else if("".equals(value.toString().trim())) {
                    if(factKeys.contains(key)) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                    if(!isUpdateEmptyColumn) {
                        cols.remove(key.toUpperCase());
                        continue;
                    }
                }

            }
        }
        if(null != ignores) {
            for(String ignore:ignores) {
                cols.remove(ignore.toUpperCase());
            }
        }
        cols = checkMetadata(runtime, dest, configs, cols);
        return cols;
    }

    /**
     * update [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    @Override
    public long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) {
        long result = 0;
        if(!run.isValid()) {
            if(log.isWarnEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
            }
            return -1;
        }
        if(run.isEmpty()) {
            log.warn("[不具备执行条件][dest:{}]", dest);
            return -1;
        }
        if(null != configs) {
            configs.add(run);
        }
        List<Object> values = run.getValues();
        int batch = run.getBatch();
        String action = null;
        if(null != run.action()) {
            action = run.action().toString();
        }
        if(batch > 1) {
            action = "batch " + action;
        }
        long fr = System.currentTimeMillis();
        /*执行SQL*/
        if (log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
            log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.UPDATE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
        }

        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return -1;
        }
        long millis = -1;
        try{
            result = actuator.update(this, runtime, random, dest, data, configs, run);
            millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[slow cmd][action:{}][{}][执行耗时:{}]{}", random, action, run.metadata(), DateUtil.format(millis), run.log(ACTION.DML.UPDATE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, run.getFinalUpdate(), values, null, true, result, millis);
                    }
                }
            }
            if (!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
                String qty = result+"";
                if(batch>1) {
                    qty = "约"+result;
                }
                log.info("{}[action:{}][{}][执行耗时:{}][影响行数:{}]", random, action, run.metadata(), DateUtil.format(millis), LogUtil.format(qty, 34));
            }

        }catch(Exception e) {
            if (ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("update 异常:", e);
            }
            if (ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                CommandUpdateException ex = new CommandUpdateException("update异常:" + e.toString(), e);
                ex.setValues(values);
                throw ex;
            }
            if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:][{}]{}", random, action, run.getTable(), LogUtil.format("更新异常:", 33) + e.toString(), run.log(ACTION.DML.UPDATE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }

        }
        return result;
    }

    /**
     * save [调用入口]<br/>
     * <br/>
     * 根据是否有主键值确认insert | update<br/>
     * 执行完成后会补齐自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns,长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列,则解析完columns后,继续解析obj<br/>
     *
     *        以上执行完后,如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比,删除表中没有的列<br/>
     * @return 影响行数
     */
    @Override
    public long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
        if(null == random) {
            random = random(runtime);
        }
        if(null == data) {
            if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                throw new CommandUpdateException("save空数据");
            }else {
                log.error("save空数据");
                return -1;
            }
        }
        if(data instanceof Collection) {
            Collection<?> items = (Collection<?>)data;
            long cnt = 0;
            for (Object item : items) {
                cnt += save(runtime, random, dest, item, configs, columns);
            }
            return cnt;
        }
        return saveObject(runtime, random, dest, data, configs, columns);
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表
     * @param data 保存的数据
     * @param configs ConfigStore
     * @param columns 指定列
     * @return long 影响行数
     */
    protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns) {
        long cnt = 0;
        //List<Run> runs = buildInsertRun(runtime, random, batch, dest, data, columns);
        return cnt;
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表
     * @param data 保存的数据
     * @param configs ConfigStore
     * @param columns 指定列
     * @return long 影响行数
     */
    protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
        if(null == data) {
            return 0;
        }
        boolean isNew = BeanUtil.checkIsNew(data);
        if(isNew) {
            return insert(runtime, random, 0, dest, data, configs, columns);
        }else{
            //是否覆盖(null:不检测直接执行update有可能影响行数=0)
            Boolean override = checkOverride(data, configs);
            Boolean overrideSync = checkOverrideSync(data, configs);
            ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
            if(null != override) {
                RunPrepare prepare = new DefaultTablePrepare(dest);
                Map<String, Object> pvs = checkPv(data);
                ConfigStore stores = new DefaultConfigStore();
                for(String k:pvs.keySet()) {
                    stores.and(k, pvs.get(k));
                }
                DataSet exists = querys(runtime, random, prepare, stores);
                if(!exists.isEmpty()) {
                    if(override) {
                        long result = update(runtime, random, dest, data, configs, columns);
                        if(null != overrideSync && overrideSync) {
                            if(data instanceof DataRow) {
                                DataRow row = (DataRow) data;
                                row.copyIfEmpty(exists.getRow(0));
                            }else{
                                BeanUtil.copyFieldValueEvl(data, exists.getRow(0));
                            }
                        }
                        return result;
                    }else{
                        log.warn("[跳过更新][数据已存在:{}({})]", dest, BeanUtil.map2json(pvs));
                    }
                }else{
                    return insert(runtime, random, 0, dest, data, configs, columns);
                }
            }else{
                return update(runtime, random, dest, data, configs, columns);
            }
        }
        return 0;
    }

    /**
     * 有主键值的情况下 检测覆盖
     * @param obj Object
     * @return boolean
     * null:正常执行update<br/>
     * true或false:检测数据库中是否存在<br/>
     * 如果数据库中存在匹配的数据<br/>
     * true:执行更新<br/>
     * false:跳过更新<br/>
     */
    protected Boolean checkOverride(Object obj, ConfigStore configs) {
        Boolean result = null;
        if(obj instanceof DataRow) {
            result = ((DataRow)obj).getOverride();
        }
        if(null == result && null != configs) {
            result = configs.override();
        }
        return result;
    }
    protected Boolean checkOverrideSync(Object obj, ConfigStore configs) {
        Boolean result = null;
        if(obj instanceof DataRow) {
            result = ((DataRow)obj).getOverrideSync();
        }
        if(null == result && null != configs) {
            result = configs.override();
        }
        return result;
    }

    /**
     * 检测主键值
     * @param obj Object
     * @return Map
     */
    protected Map<String, Object> checkPv(Object obj) {
        Map<String, Object> pvs = new HashMap<>();
        if(null != obj && obj instanceof DataRow) {
            DataRow row = (DataRow) obj;
            List<String> ks = row.getPrimaryKeys();
            for(String k:ks) {
                pvs.put(k, row.get(k));
            }
        }
        return pvs;
    }

    /**
     * 是否是可以接收数组类型的值
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param key key
     * @return boolean
     */
    protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key) {
        Table table = run.getTable();
        if (null != table) {
            LinkedHashMap<String, Column> columns = columns(runtime, null, false, table, false);
            if(null != columns) {
                Column column = columns.get(key.toUpperCase());
                return isMultipleValue(column);
            }
        }
        return false;
    }

    /**
     * 是否支持集合值
     * @param column Column 根据column的数据类型
     * @return boolean
     */
    protected boolean isMultipleValue(Column column) {
        if(null != column) {
            String type = column.getTypeName().toUpperCase();
            if(type.contains("POINT") || type.contains("GEOMETRY") || type.contains("POLYGON")) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤掉表结构中不存在的列
     * @param table 表
     * @param columns columns
     * @return List
     */
    public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        if(!ConfigStore.IS_AUTO_CHECK_METADATA(configs)) {
            return columns;
        }
        if(null == table || table.isEmpty()) {
            return columns;
        }
        LinkedHashMap<String, Column> result = new LinkedHashMap<>();
        LinkedHashMap<String, Column> metadatas = columns(runtime, null, false, table, false);
        try {
            LinkedHashMap<String, Tag> tags = tags(runtime, null,false, table);
            metadatas.putAll(tags);
        }catch (Exception e) {}
        //tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)
        if(!metadatas.isEmpty()) {
            for (String key:columns.keySet()) {
                if (metadatas.containsKey(key)) {
                    result.put(key, metadatas.get(key));
                } else {
                    if(ConfigStore.IS_LOG_SQL_WARN(configs)) {
                        log.warn("[{}][column:{}.{}][insert/update忽略当前列名]", LogUtil.format("列名检测不存在", 33), table, key);
                    }
                }
            }
        }else{
            if(ConfigStore.IS_LOG_SQL_WARN(configs)) {
                log.warn("[{}][table:{}][忽略列名检测]", LogUtil.format("表结构检测失败(检查表名是否存在)", 33), table);
            }
        }
        if(ConfigStore.IS_LOG_SQL_WARN(configs)) {
            log.info("[check column metadata][origin:{}][result:{}]", columns.size(), result.size());
        }
        return result;
    }

    /* *****************************************************************************************************************
     *                                                     QUERY
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)
     * <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions)
     * List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * [命令合成]
     * Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)
     * Run fillQueryContent(DataRuntime runtime, Run run)
     * String mergeFinalQuery(DataRuntime runtime, Run run)
     * RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode)
     * Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode)
     * List<RunValue> createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode)
     * [命令执行]
     * DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)
     * List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run)
     * Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run)
     * DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)
     * List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list)
     ******************************************************************************************************************/

    /**
     * query [调用入口]<br/>
     * <br/>
     * 返回DataSet中包含元数据信息，如果性能有要求换成maps
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    @Override
    public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        Table table = null;
        DataSet set = null;
        Long fr = 0L;
        boolean cmd_success = false;
        Run run = null;
        PageNavi navi = null;
        if(null == prepare.getJoins() || prepare.getJoins().isEmpty()) {
            //多个表的情况 不考虑表结构(不查元数据)
            table = prepare.getTable();
        }
        if(null == random) {
            random = random(runtime);
        }
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if (null != dmListener) {
            swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return new DataSet().setTable(table);
        }
        //query拦截
        swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
        if(swt == ACTION.SWITCH.BREAK) {
            return new DataSet().setTable(table);
        }

        run = buildQueryRun(runtime, prepare, configs, true, true, conditions);

        if (log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs) && !run.isValid()) {
            String tmp = "[valid:false][不具备执行条件]";
            String src = "";
            if (prepare instanceof TablePrepare) {
                src = prepare.getTableName();
            } else {
                src = prepare.getText();
            }
            tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
            log.warn(tmp);
        }
        navi = run.getPageNavi();
        if(null == navi && null != configs) {
            navi = configs.getPageNavi();
        }
        long total = 0;
        Boolean autoCount = false;
        if (run.isValid()) {
            if (null != navi) {
                autoCount = navi.autoCount();//未设置或true时查询总数
                if(null == autoCount) {
                    autoCount = true;
                }
                if(autoCount) {
                    if (null != dmListener) {
                        dmListener.beforeTotal(runtime, random, run);
                    }
                    fr = System.currentTimeMillis();
                    if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
                        // 第一条 query中设置的标识(只查一行)
                        total = 1;
                    } else {
                        // 未计数(总数 )
                        if (navi.getTotalRow() == 0) {
                            total = count(runtime, random, run);
                            navi.setTotalRow(total);
                        } else {
                            total = navi.getTotalRow();
                        }
                    }
                    if (null != dmListener) {
                        dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
                    }
                    if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                        log.info("[查询记录总数][行数:{}]", total);
                    }
                }
            }
        }
        fr = System.currentTimeMillis();
        if (run.isValid()) {
            if(null == navi || total > 0 || !autoCount) {
                if(null != dmListener) {
                    dmListener.beforeQuery(runtime, random, run, total);
                }
                swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
                if(swt == ACTION.SWITCH.BREAK) {
                    return new DataSet().setTable(table);
                }
                set = select(runtime, random, false, table, configs, run);
                cmd_success = true;
            }else{
                if(null != configs) {
                    configs.add(run);
                }
                set = new DataSet().setTable(table);
                if(ConfigStore.IS_CHECK_EMPTY_SET_METADATA(configs)) {
                    set.setMetadata(metadata(runtime, prepare, false));
                }
            }
        } else {
            set = new DataSet().setTable(table);
        }

        set.setDest(prepare.getDest());
        set.setNavi(navi);
        if (null != navi && navi.isLazy()) {
            navi.setDataSize(set.size());
            PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
        }

        if(null != dmListener) {
            dmListener.afterQuery(runtime, random, run, cmd_success, set, System.currentTimeMillis() - fr);
        }
        InterceptorProxy.afterQuery(runtime, random, run, cmd_success, set, navi, System.currentTimeMillis() - fr);
        return set;
    }

    /**
     * query procedure [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param procedure 存储过程
     * @param navi 分页
     * @return DataSet
     */
    @Override
    public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)", 37));
        }
        return new DataSet();
    }

    /**
     * query [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param clazz 类
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return EntitySet
     * @param <T> Entity
     */
    @Override
    public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions) {
        if(null == prepare) {
            prepare = new DefaultTablePrepare();
        }
        EntitySet<T> list = null;
        Long fr = System.currentTimeMillis();
        Run run = null;
        boolean cmd_success = false;
        PageNavi navi = null;

        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if (null != dmListener) {
            swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return new EntitySet();
        }
        swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
        if(swt == ACTION.SWITCH.BREAK) {
            return new EntitySet();
        }

        if(BasicUtil.isEmpty(prepare.getDest())) {
            //text xml格式的 不检测表名，避免一下步根据表名检测表结构
            if(prepare instanceof org.anyline.data.prepare.auto.TextPrepare || prepare instanceof TextPrepare) {
            }else {
                prepare.setDest(EntityAdapterProxy.table(clazz, true));
            }
        }

        run = buildQueryRun(runtime, prepare, configs, true, true, conditions);
        if (log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs) && !run.isValid()) {
            String tmp = "[valid:false][不具备执行条件]";
            tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
            log.warn(tmp);
        }
        navi = run.getPageNavi();
        long total = 0;
        Boolean autoCount = false;
        if (run.isValid()) {
            if (null != navi) {
                autoCount = navi.autoCount();//未设置或true时查询总数
                if(null == autoCount) {
                    autoCount = true;
                }
                if(autoCount) {
                    if (null != dmListener) {
                        dmListener.beforeTotal(runtime, random, run);
                    }
                    fr = System.currentTimeMillis();
                    if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
                        // 第一条 query中设置的标识(只查一行)
                        total = 1;
                    }  else {
                        // 未计数(总数 )
                        if (navi.getTotalRow() == 0) {
                            total = count(runtime, random, run);
                            navi.setTotalRow(total);
                        } else {
                            total = navi.getTotalRow();
                        }
                    }
                    if (null != dmListener) {
                        dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
                    }
                }
            }
            if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.info("[查询记录总数][行数:{}]", total);
            }
        }
        fr = System.currentTimeMillis();
        if (run.isValid()) {
            if(null == navi || !autoCount || total > 0) {
                swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
                if(swt == ACTION.SWITCH.BREAK) {
                    return new EntitySet();
                }
                if (null != dmListener) {
                    dmListener.beforeQuery(runtime, random, run, total);
                }
                fr = System.currentTimeMillis();
                list = select(runtime, random, clazz, run.getTable(), configs, run);
                cmd_success = false;
            }else{
                list = new EntitySet<>();
            }
        } else {
            list = new EntitySet<>();
        }
        list.setNavi(navi);
        if (null != navi && navi.isLazy()) {
            PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
        }

        if (null != dmListener) {
            dmListener.afterQuery(runtime, random, run, cmd_success, list, System.currentTimeMillis() - fr);
        }
        InterceptorProxy.afterQuery(runtime, random, run, cmd_success, list, navi, System.currentTimeMillis() - fr);
        return list;
    }

    /**
     * select [命令执行-子流程]<br/>
     * DataRow转换成Entity
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param clazz entity class
     * @param table table
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return EntitySet
     * @param <T> entity.class
     *
     */
    protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, Table table, ConfigStore configs, Run run) {
        EntitySet<T> set = new EntitySet<>();
        if(null == random) {
            random = random(runtime);
        }
        if(null != configs) {
            configs.entityClass(clazz);
        }
        DataSet rows = select(runtime, random, false, table, configs, run);
        for(DataRow row:rows) {
            T entity = null;
            if(EntityAdapterProxy.hasAdapter(clazz)) {
                //jdbc adapter需要参与 或者metadata里添加colun type
                entity = EntityAdapterProxy.entity(clazz, row, null);
            }else{
                entity = row.entity(clazz);
            }
            set.add(entity);
        }

        return set;
    }

    /**
     * query [调用入口]<br/>
     * <br/>
     * 对性能有要求的场景调用，返回java原生map集合,结果中不包含元数据信息
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return maps 返回map集合
     */
    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        List<Map<String, Object>> maps = null;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        boolean cmd_success = false;
        Run run = null;
        if(null == random) {
            random = random(runtime);
        }
        //query拦截
        swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
        if(swt == ACTION.SWITCH.BREAK) {
            return new ArrayList<>();
        }

        if (null != dmListener) {
            swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return new ArrayList<>();
        }
        run = buildQueryRun(runtime, prepare, configs, true, true, conditions);
        Long fr = System.currentTimeMillis();
        if (log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs) && !run.isValid()) {
            String tmp = "[valid:false][不具备执行条件]";
            String src = "";
            if (prepare instanceof TablePrepare) {
                src = prepare.getTableName();
            } else {
                src = prepare.getText();
            }
            tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
            log.warn(tmp);
        }
        if (run.isValid()) {
            swt = InterceptorProxy.beforeQuery(runtime, random, run, null);
            if(swt == ACTION.SWITCH.BREAK) {
                return new ArrayList<>();
            }
            if (null != dmListener) {
                dmListener.beforeQuery(runtime, random, run, -1);
            }
            if(null != configs) {
                PageNavi navi = configs.getPageNavi();
                if(null != navi) {
                    Boolean autoCount = navi.autoCount();
                    if (null != autoCount && autoCount) {
                        long total = count(runtime, random, run);
                        navi.setTotalRow(total);
                    }
                }

            }

            maps = maps(runtime, random, configs, run);
            cmd_success = true;
        } else {
            maps = new ArrayList<>();
        }

        if (null != dmListener) {
            dmListener.afterQuery(runtime, random, run, cmd_success, maps, System.currentTimeMillis() - fr);
        }
        InterceptorProxy.afterQuery(runtime, random, run, cmd_success, maps, null, System.currentTimeMillis() - fr);
        return maps;
    }

    /**
     * select[命令合成]<br/> 最终可执行命令<br/>
     * 创建查询SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        Run run = initQueryRun(runtime, prepare);
        init(runtime, run, configs, conditions);
        /*List<RunPrepare> joins = prepare.getJoins();
        if(null != joins) {
            for(RunPrepare join:joins) {
                buildQueryRun(runtime, join, new DefaultConfigStore());
            }
        }*/
        List<Run> unions = run.getUnions();
        if(null != unions) {
            for(Run union:unions) {
                init(runtime, union, configs, conditions);
            }
        }
        if(run.checkValid()) {
            //构造最终的查询SQL
            run = fillQueryContent(runtime, run, placeholder, unicode);
        }

        return run;
    }

    /**
     * query run初始化,检测占位符、忽略不存在的列等
     * select[命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     */
    public void init(DataRuntime runtime, Run run, ConfigStore configs, String ... conditions) {
        if(null != run) {
            RunPrepare prepare = run.getPrepare();
            if(prepare instanceof TablePrepare) {
                likes(runtime, prepare.getTable(), configs);
            }
            run.addConfigStore(configs);
            //如果是text类型 将解析文本并抽取出变量
            parsePlaceholder(runtime, run);
            configs = run.getConfigs();
            //先把configs中的占位值取出
            if(null != configs) {
                List<Object> statics = configs.getStaticValues();
                for (Object item : statics) {
                    run.addValue(new RunValue("none", item));
                }
            }

            run.addCondition(conditions);

            if(run.checkValid()) {
                //为变量赋值 run.condition赋值
                run.init();
                //检测不存在的列
                if(ConfigStore.IS_AUTO_CHECK_METADATA(configs) && null != prepare) {
                    List<RunPrepare> joins = prepare.getJoins();
                    Table table = run.getTable();
                    if(null != table && (null == joins || joins.isEmpty())) {//TODO 单表时再检测
                        LinkedHashMap<String, Column> metadatas = columns(runtime, null, false, table, false);
                        //检测不存在的列
                        OrderStore orders = run.getOrders();
                        if (null != orders) {
                            orders.filter(metadatas);
                        }
                        prepare.filter(metadatas);
                        ConditionChain chain = run.getConditionChain();
                        if(null != chain) {
                            chain.filter(metadatas);
                        }
                        if(null != configs) {
                            configs.filter(metadatas);
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析文本中的占位符
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void parsePlaceholder(DataRuntime runtime, Run run) {
        CommandParser.parseText(runtime, run);
    }


    private void likes(DataRuntime runtime, Table table, ConfigStore configs) {
        if(null == table || null == configs) {
            return;
        }
        List<Config> list = configs.getConfigChain().getConfigs();
        for(Config config:list) {
            if(config.getCompare() == Compare.LIKES) {
                LinkedHashMap<String, Column> colums = columns(runtime, null, false, table, false);
                list.remove(config);
                ConfigStore ors = new DefaultConfigStore();
                List<Object> values = config.getValues();
                Object value = null;
                if(null != values && !values.isEmpty()) {
                    value = values.get(0);
                }
                for(Column column:colums.values()) {
                    TypeMetadata tm = column.getTypeMetadata();
                    if(null != tm && tm.getCategoryGroup() == TypeMetadata.CATEGORY_GROUP.STRING) {
                        ors.or(Compare.LIKE, column.getName(), value);
                    }
                }
                configs.and(ors);
                break;
            }
        }
    }

    /**
     * 查询序列cur 或 next value
     * @param next  是否生成返回下一个序列 false:cur true:next
     * @param names 序列名
     * @return String
     */
    @Override
    public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)", 37));
        }
        return new ArrayList<>();
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体, 中间过程有可能转换类型
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public Run fillQueryContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        return fillQueryContent(runtime, run.getBuilder(), run, placeholder, unicode);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public Run fillQueryContent(DataRuntime runtime, StringBuilder builder, Run run, Boolean placeholder, Boolean unicode) {
        if(null != run) {
            if(run instanceof TableRun) {
                run = fillQueryContent(runtime, builder, (TableRun) run, placeholder, unicode);
            }else if(run instanceof XMLRun) {
                run = fillQueryContent(runtime, builder, (XMLRun) run, placeholder, unicode);
            }else if(run instanceof TextRun) {
                run = fillQueryContent(runtime, builder, (TextRun) run, placeholder, unicode);
            }
            convert(runtime, run.getConfigs(), run);
        }
        return run;
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    protected Run fillQueryContent(DataRuntime runtime, XMLRun run, Boolean placeholder, Boolean unicode) {
        return fillQueryContent(runtime, run.getBuilder(), run, placeholder, unicode);
    }


    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run TextRun
     */
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, XMLRun run, Boolean placeholder, Boolean unicode) {
        String text = CommandParser.replaceVariable(runtime, run, run.getVariableBlocks(), run.getVariables(), run.getText());
        run.getBuilder().append(text);
        run.appendCondition(true);
        run.appendGroup();
        run.checkValid();
        return run;
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run TextRun
     */
    protected Run fillQueryContent(DataRuntime runtime, TextRun run, Boolean placeholder, Boolean unicode) {
        return fillQueryContent(runtime, run.getBuilder(), run, placeholder, unicode);
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run TextRun
     */
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TextRun run, Boolean placeholder, Boolean unicode) {
        String text = CommandParser.replaceVariable(runtime, run, run.getVariableBlocks(), run.getVariables(), run.getText());
        run.getBuilder().append(text);
        run.appendCondition(placeholder, unicode);
        run.appendGroup(runtime, placeholder, unicode);
        // appendOrderStore();
        run.checkValid();
        return run;
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run TextRun
     */
    protected Run fillQueryContent(DataRuntime runtime, TableRun run, Boolean placeholder, Boolean unicode) {
        return fillQueryContent(runtime, run.getBuilder(), run, placeholder, unicode);
    }

    /**
     * 有些非JDBC环境也需要用到SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TableRun run, Boolean placeholder, Boolean unicode) {
        TablePrepare prepare = (TablePrepare)run.getPrepare();
        builder.append("SELECT ");
        Boolean distinct = run.distinct();
        if(null != distinct && distinct){
            builder.append("distinct");
        } else if(null != prepare.getDistinct()) {
            builder.append(prepare.getDistinct());
        }
        builder.append(BR_TAB);
        ConfigStore configs = run.getConfigs();

        LinkedHashMap<String,Column> columns = prepare.getColumns();
        List<AggregationConfig> aggregations = prepare.aggregations();
        if(null == columns || columns.isEmpty()) {
            if(null != configs) {
                List<String> cols = configs.columns();
                columns = new LinkedHashMap<>();
                for(String col:cols) {
                    columns.put(col.toUpperCase(), new Column(col));
                }
            }
        }
        boolean first = true;
        if(null != columns && !columns.isEmpty()) {
            // 指定查询列
            for(Column column:columns.values()) {
                if(BasicUtil.isEmpty(column) || BasicUtil.isEmpty(column.getName())) {
                    continue;
                }
                if(!first) {
                    builder.append(", ");
                }
                first = false;
                String name = column.getName();
                //if (column.startsWith("${") && column.endsWith("}")) {
                if (BasicUtil.checkEl(name)) {
                    name = name.substring(2, name.length()-1);
                    builder.append(name);
                }else{
                    if(name.contains("(") || name.contains(",")) {
                        builder.append(name);
                    }else if(name.toUpperCase().contains(" AS ")) {
                        int split = name.toUpperCase().indexOf(" AS ");
                        String tmp = name.substring(0, split).trim();
                        delimiter(builder, tmp);
                        builder.append(columnAliasGuidd());
                        tmp = name.substring(split+4).trim();
                        delimiter(builder, tmp);
                    }else if("*".equals(name)) {
                        builder.append("*");
                    }else{
                        delimiter(builder, name);
                    }
                }
            }
        }
        if(null != aggregations){
            for(AggregationConfig agg:aggregations){
                if(!first) {
                    builder.append(", ");
                }
                first = false;
                builder.append(agg.getAggregation().formula()).append("(").append(agg.getField()).append(") ");
                String alias = agg.getAlias();
                if(BasicUtil.isEmpty(alias)){
                    alias = agg.getField() +"_" + agg.getAggregation().code();
                }
                builder.append(alias);
            }
        }
        if((null == columns || columns.isEmpty()) && (null == aggregations || aggregations.isEmpty())){
            // 全部查询
            builder.append("*");
        }
        builder.append(BR);
        builder.append("FROM ");
        fillMasterTableContent(runtime, builder, run, prepare);
        builder.append(BR);
        List<RunPrepare> joins = prepare.getJoins();
        if(null != joins) {
            for (RunPrepare join:joins) {
                fillJoinTableContent(runtime, builder, run, join);
            }
        }

        //builder.append("\nWHERE 1=1\n\t");
        /*添加查询条件*/
        // appendConfigStore();
        run.appendCondition(builder, this, true, placeholder, unicode);
        fillQueryContentGroup(runtime, builder, run, placeholder, unicode);
        return run;
    }
    protected Run fillQueryContentGroup(DataRuntime runtime, StringBuilder builder, TableRun run, Boolean placeholder, Boolean unicode) {
        RunPrepare prepare = run.getPrepare();
        ConfigStore configs = run.getConfigs();
        GroupStore groups = run.getGroups();
        if(null == groups || groups.isEmpty()){
            if(null != prepare){
                groups = prepare.groups();
            }
        }
        if(null == groups || groups.isEmpty()){
            if(null != configs){
                groups = configs.groups();
            }
        }
        ConfigStore having = run.having();
        if(null == having || having.isEmpty()){
            if(null != configs) {
                having = configs.having();
            }
        }
        if(null == having || having.isEmpty()){
            having = prepare.having();
        }
        if(null != groups) {
            builder.append("\n").append(groups.getRunText(delimiterFr+delimiterTo));
        }

        if(null != having) {
            String txt = SQLUtil.trim(having.getRunText(runtime, false, unicode));
            if(BasicUtil.isNotEmpty(txt)) {
                builder.append("\nHAVING ").append(txt);
            }
        }
        //run.appendGroup(builder);
        return run;
    }

    /**
     * 主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param prepare TablePrepare
     * @return Run
     */
    public Run fillMasterTableContent(DataRuntime runtime, StringBuilder builder, TableRun run, RunPrepare prepare) {
        if(prepare instanceof VirtualTablePrepare) {
            ConfigStore configs = run.getConfigs();
            if(null == configs){
                configs = new DefaultConfigStore();
            }
            Run fromRun = buildQueryRun(runtime, ((VirtualTablePrepare) prepare).getPrepare(), configs, true, true);
            run.getRunValues().addAll(fromRun.getRunValues());
            String inner = fromRun.getFinalQuery(true);
            inner = BasicUtil.tab(inner);
            builder.append("(\n").append(inner).append("\n)");
        } else {
            Table table = prepare.getTable();
            name(runtime, builder, table);
        }
        String alias = prepare.getAlias();
        if(BasicUtil.isNotEmpty(alias)) {
            builder.append(tableAliasGuidd());
            delimiter(builder, alias);
        }
        return run;
    }

    /**
     * 关联表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param prepare TablePrepare
     * @return Run
     */
    public Run fillJoinTableContent(DataRuntime runtime, StringBuilder builder, TableRun run, RunPrepare prepare) {
        builder.append(BR);
        Join join = prepare.getJoin();
        if(prepare instanceof VirtualTablePrepare) {
            join = ((VirtualTablePrepare) prepare).getPrepare().getJoin();
            builder.append(join.getType().getCode()).append(" ");
            Run joinRun = buildQueryRun(runtime, ((VirtualTablePrepare) prepare).getPrepare(), new DefaultConfigStore(), true, true);
            run.getRunValues().addAll(joinRun.getRunValues());
            String inner = joinRun.getFinalQuery(true);
            inner = BasicUtil.tab(inner);
            builder.append("(").append(BR).append(inner).append(BR).append(")");
        }else {
            builder.append(join.getType().getCode()).append(" ");
            name(runtime, builder, prepare.getTable());
        }
        String alias = prepare.getAlias();
        if(BasicUtil.isNotEmpty(alias)) {
            builder.append(tableAliasGuidd());
            delimiter(builder, alias);
        }
        String on = join.getConditions().getRunText(runtime, false);
        on = SQLUtil.trim(on);
        builder.append(" ON ").append(on).append(BR);
        return run;
    }
    /**
     * select[命令合成-子流程] <br/>
     * 合成最终 select 命令 包含分页 排序
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run) {
        return run.getBaseQuery();
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造 LIKE 查询条件
     * 如果不需要占位符 返回null  否则原样返回value
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param value value
     * @return value 有占位符时返回占位值，没有占位符返回null
     */
    @Override
    public RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode)", 37));
        }
        return null;
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造(NOT) IN 查询条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param value value
     * @return builder
     */
    @Override
    public List<RunValue> createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<RunValue> createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode)", 37));
        }
        return null;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param system 系统表不检测列属性
     * @param table 表
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return DataSet
     */
    @Override
    public DataSet select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)", 37));
        }
        return new DataSet().setTable(table);
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return maps
     */
    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        List<Map<String, Object>> maps = null;
        if(null == random) {
            random = random(runtime);
        }
        if(null != configs) {
            configs.add(run);
        }
        String sql = run.getFinalQuery();
        List<Object> values = run.getValues();
        if(BasicUtil.isEmpty(sql)) {
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                throw new CommandQueryException("未指定命令");
            }else{
                log.error("未指定命令");
                return new ArrayList<>();
            }
        }
        if(log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
            log.info("{}[action:select]{}", random, run.log(ACTION.DML.SELECT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
        }
        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return new ArrayList<>();
        }
        try{
            maps = actuator.maps(this, runtime, random, configs, run);
            maps = process(runtime, maps);
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("maps 异常:", e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e.toString(), run.log(ACTION.DML.SELECT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                CommandQueryException ex = new CommandQueryException("query异常:"+e.toString(), e);
                ex.setCmd(sql);
                ex.setValues(values);
                throw ex;
            }

        }
        return maps;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return map
     */
    @Override
    public Map<String, Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        Map<String, Object> map = null;
        String sql = run.getFinalExists();
        List<Object> values = run.getValues();
        if(null != configs) {
            configs.add(run);
        }
        long fr = System.currentTimeMillis();
        if (log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
            log.info("{}[action:select]{}", random, run.log(ACTION.DML.EXISTS,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
        }
        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return new HashMap<>();
        }
        try {
            map = actuator.map(this, runtime, random, configs, run);
        }catch (Exception e) {
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                throw new CommandQueryException("查询异常", e);
            }
            if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                    e.printStackTrace();
                }
                log.error("{}[{}][action:select][cmd:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e, sql, LogUtil.param(values));
            }
        }
        //}
        Long millis = System.currentTimeMillis() - fr;
        boolean slow = false;
        long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
        if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
            if(millis > SLOW_SQL_MILLIS) {
                slow = true;
                log.warn("{}[slow cmd][action:exists][执行耗时:{}][cmd:\n{}\n]\n[param:{}]", random, DateUtil.format(millis), sql, LogUtil.param(values));
                if(null != dmListener) {
                    dmListener.slow(runtime, random, ACTION.DML.EXISTS, run, sql, values, null, true, map, millis);
                }
            }
        }
        if (!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
            log.info("{}[action:select][执行耗时:{}][封装行数:{}]", random, DateUtil.format(millis), LogUtil.format(map == null ?0:1, 34));
        }
        return map;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param next 是否查下一个序列值
     * @param names 存储过程名称s
     * @return DataRow 保存序列查询结果 以存储过程name作为key
     */
    @Override
    public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)", 37));
        }
        return null;
    }

    /**
     * select [结果集封装-子流程]<br/>
     * JDBC执行完成后的结果处理
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param list JDBC执行返回的结果集
     * @return  maps
     */
    @Override
    public List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list) {
        return list;
    }

    /* *****************************************************************************************************************
     *                                                     COUNT
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * [命令合成]
     * String mergeFinalTotal(DataRuntime runtime, Run run)
     * [命令执行]
     * long count(DataRuntime runtime, String random, Run run)
     ******************************************************************************************************************/
    /**
     * count [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return long
     */
    @Override
    public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        long count = -1;
        Long fr = System.currentTimeMillis();
        Run run = null;
        if(null == random) {
            random = random(runtime);
        }

        boolean cmd_success = false;

        ACTION.SWITCH swt = InterceptorProxy.prepareCount(runtime, random, prepare, configs, conditions);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if (null != dmListener) {
            swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        run = buildQueryRun(runtime, prepare, configs,true, true, conditions);
        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTableName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
            }
            return -1;
        }
        if (null != dmListener) {
            dmListener.beforeCount(runtime, random, run);
        }
        swt = InterceptorProxy.beforeCount(runtime, random, run, configs);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        fr = System.currentTimeMillis();
        count = count(runtime, random, run);
        cmd_success = true;

        if(null != dmListener) {
            dmListener.afterCount(runtime, random, run, cmd_success, count, System.currentTimeMillis() - fr);
        }
        InterceptorProxy.afterCount(runtime, random, run, configs, cmd_success, count, System.currentTimeMillis() - fr);
        return count;
    }

    /**
     * count [命令合成]<br/>
     * 合成最终 select count 命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    @Override
    public String mergeFinalTotal(DataRuntime runtime, Run run) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String mergeFinalTotal(DataRuntime runtime, Run run)", 37));
        }
        return null;
    }

    /**
     * count [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return long
     */
    @Override
    public long count(DataRuntime runtime, String random, Run run) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 long count(DataRuntime runtime, String random, Run run)", 37));
        }
        return -1;
    }


    /**
     * 计算字符串在当前数据库中占用字节长度
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param cn 字符串
     * @param configs 过滤条件及相关配置
     * @return int
     */
    @Override
    public int length(DataRuntime runtime, String random, String cn, ConfigStore configs) {
        int count = -1;
        Long fr = System.currentTimeMillis();
        Run run = null;
        if(null == random) {
            random = random(runtime);
        }

        run = buildQueryLengthRun(runtime, cn, configs);

        DataSet set = select(runtime, random, true, new Table(), configs, run);
        if(!set.isEmpty()){
            count = set.getRow(0).getInt( "CNT", -1);
        }
        return count;
    }
    /**
     * 计算字符串在当前数据库中占用字节长度
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param cn 字符串
     * @param configs 过滤条件及相关配置
     * @return Run
     */
    @Override
    public Run buildQueryLengthRun(DataRuntime runtime, String cn, ConfigStore configs) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现  Run buildQueryLengthRun(DataRuntime runtime, String cn, ConfigStore configs)", 37));
        }
        return null;
    }
    /* *****************************************************************************************************************
     *                                                     EXISTS
     * -----------------------------------------------------------------------------------------------------------------
     * boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * String mergeFinalExists(DataRuntime runtime, Run run)
     ******************************************************************************************************************/

    /**
     * exists [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return boolean
     */
    @Override
    public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现  exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)", 37));
        }
        return false;
    }

    /**
     * exists [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    @Override
    public String mergeFinalExists(DataRuntime runtime, Run run) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String mergeFinalExists(DataRuntime runtime, Run run)", 37));
        }
        return null;
    }

    /* *****************************************************************************************************************
     *                                                     EXECUTE
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values)
     * boolean execute(DataRuntime runtime, String random, Procedure procedure)
     * [命令合成]
     * Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * void fillExecuteContent(DataRuntime runtime, Run run)
     * [命令执行]
     * long execute(DataRuntime runtime, String random, ConfigStore configs, Run run)
     ******************************************************************************************************************/

    /**
     * execute [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        long result = -1;
        boolean cmd_success = false;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if(null == random) {
            random = random(runtime);
        }
        swt = InterceptorProxy.prepareExecute(runtime, random, prepare, configs, conditions);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }

        Run run = buildExecuteRun(runtime,  prepare, configs, true, true, conditions);
        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTableName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
            }
            return -1;
        }
        long fr = System.currentTimeMillis();

        long millis = -1;
        swt = InterceptorProxy.beforeExecute(runtime, random, run, configs);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.beforeExecute(runtime, random, run);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        result = execute(runtime, random, configs, run);
        cmd_success = true;
        if (null != dmListener) {
            dmListener.afterExecute(runtime, random, run, cmd_success, result, millis);
        }
        InterceptorProxy.afterExecute(runtime, random, run, configs, cmd_success, result, System.currentTimeMillis()-fr);
        return result;
    }

    @Override
    public long execute(DataRuntime runtime, String random, List<RunPrepare> prepares, ConfigStore configs) {
        long result = 0;
        boolean cmd_success = false;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if(null == random) {
            random = random(runtime);
        }
        swt = InterceptorProxy.prepareExecute(runtime, random, prepares, configs);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }

        List<Run> runs = new ArrayList<>();
        for(RunPrepare prepare:prepares) {
            Run run = buildExecuteRun(runtime, prepare, configs, true, true);
            if(!run.isValid()) {
                if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                    log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, run.getTableName(), configs) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
                }
                return -1;
            }
            runs.add(run);
        }
        long fr = System.currentTimeMillis();
        long millis = -1;
        swt = InterceptorProxy.beforeExecute(runtime, random, runs, configs);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.beforeExecute(runtime, random, runs);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        result += execute(runtime, random, configs, runs);
        cmd_success = true;
        if (null != dmListener) {
            dmListener.afterExecute(runtime, random, runs, cmd_success, result, millis);
        }
        InterceptorProxy.afterExecute(runtime, random, runs, configs, cmd_success, result, System.currentTimeMillis()-fr);
        
        return result;
    }
    /**
     * execute [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param batch 批量执行每批最多数量
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param values  values
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values) {
        if(null == random) {
            random = random(runtime);
        }
        prepare.setBatch(batch);
        Run run = buildExecuteRun(runtime, prepare, configs, true, true);
        if(null != values && !values.isEmpty()) {
            Object first = values.iterator().next();
            if (first instanceof Collection) {
                //?下标占位
                List<Object> list = new ArrayList<>();
                int vol = 0;
                for (Object item : values) {
                    Collection col = (Collection) item;
                    list.addAll(col);
                    vol = col.size();
                }
                run.setValues(null, list);
                run.setVol(vol);
            } else if(first.getClass().isArray()) {
                //?下标占位
                List<Object> list = new ArrayList<>();
                int vol = 0;
                int len = Array.getLength(first);
                for (Object item : values) {
                    for (int i = 0; i < len; i++) {
                        Object value = Array.get(item, i);
                        list.add(value);
                    }
                }
                run.setValues(null, list);
                run.setVol(len);
            }else {
                //${} #{}占位
                List<Object> list = new ArrayList<>();
                List<Variable> vars = run.getVariables();
                List<String> keys = new ArrayList<>();
                run.setVol(vars.size());
                for (Variable var : vars) {
                    keys.add(var.getKey());
                }
                for (Object item : values) {
                    for (String key : keys) {
                        Object value = BeanUtil.getFieldValue(item, key, true);
                        list.add(value);
                    }
                }
                run.setValues(null, list);
            }
        }
        return execute(runtime, random, configs, run);
    }

    /**
     * execute [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param batch 批量执行每批最多数量
     * @param vol 批量执行每行参数数量
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param values  values
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, int batch, int vol, ConfigStore configs, RunPrepare prepare, Collection<Object> values) {
        if(null == random) {
            random = random(runtime);
        }
        prepare.setBatch(batch);
        Run run = buildExecuteRun(runtime, prepare, configs, true, true);
        run.setVol(vol);
        run.setValues(null, values);
        return execute(runtime, random, configs, run);
    }

    /**
     * procedure [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @param random  random
     * @return 影响行数
     */
    @Override
    public boolean execute(DataRuntime runtime, String random, Procedure procedure) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, Procedure procedure)", 37));
        }
        return false;
    }

    /**
     * execute [命令合成]<br/>
     * 创建执行SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        Run run = null;
        if(prepare instanceof TextPrepare) {
            run = new XMLRun();
        }else if(prepare instanceof org.anyline.data.prepare.auto.TextPrepare) {
            run = prepare.build(runtime);
        }
        if(null != run) {
            run.setConfigStore(configs);
            run.setBatch(prepare.getBatch());
            run.setRuntime(runtime);
            run.setPrepare(prepare);
            parsePlaceholder(runtime, run);
            run.addCondition(conditions);
            run.init(); //
            //构造最终的执行SQL
            //fillQueryContent(runtime, run);
            fillExecuteContent(runtime, run, placeholder, unicode);
        }
        return run;
    }

    /**
     * execute [命令合成]<br/>
     * 填充execute命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    protected void fillExecuteContent(DataRuntime runtime, XMLRun run) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, XMLRun run)", 37));
        }
    }

    /**
     * execute [命令合成]<br/>
     * 填充execute命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    protected void fillExecuteContent(DataRuntime runtime, TextRun run, Boolean placeholder, Boolean unicode) {
        String text = CommandParser.replaceVariable(runtime, run, run.getVariableBlocks(), run.getVariables(), run.getText());
        run.getBuilder().append(text);
        run.appendCondition(placeholder, unicode);
        run.appendGroup(runtime, placeholder, unicode);
        run.checkValid();
    }

    /**
     * execute [命令合成]<br/>
     * 填充execute命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    protected void fillExecuteContent(DataRuntime runtime, TableRun run, Boolean placeholder, Boolean unicode) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 fillExecuteContent(DataRuntime runtime, TextRun run)", 37));
        }
    }

    /**
     * execute [命令合成-子流程]<br/>
     * 填充 execute 命令内容
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void fillExecuteContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        if(null != run) {
            if(run instanceof TableRun) {
                TableRun r = (TableRun) run;
                fillExecuteContent(runtime, r, placeholder, unicode);
            }else if(run instanceof XMLRun) {
                XMLRun r = (XMLRun) run;
                fillExecuteContent(runtime, r, placeholder, unicode);
            }else if(run instanceof TextRun) {
                TextRun r = (TextRun) run;
                fillExecuteContent(runtime, r, placeholder, unicode);
            }
        }
    }

    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) {
        long result = -1;
        if(null == random) {
            random = random(runtime);
        }
        String sql = run.getFinalExecute();
        List<Object> values = run.getValues();
        long fr = System.currentTimeMillis();
        int batch = run.getBatch();
        String action = "execute";
        if(batch > 1) {
            action = "batch execute";
        }
        if(log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL(configs)) {
            if(batch >1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
                log.info("{}[action:{}][cmd:\n{}\n]\n[param size:{}]", random, action, sql, values.size());
            }else {
                log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.EXECUTE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
        }
        if(null != configs) {
            configs.add(run);
        }
        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return -1;
        }
        long millis = -1;
        try{
            result = actuator.execute(this, runtime, random, configs, run);
            millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[slow cmd][action:{}][执行耗时:{}][cmd:\n{}\n]\n[param:{}]", random, action, DateUtil.format(millis), sql, LogUtil.param(values));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.EXECUTE, run, sql, values, null, true, result, millis);
                    }
                }
            }
            if (!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
                String qty = ""+result;
                if(batch>1) {
                    qty = "约"+result;
                }
                log.info("{}[action:{}][执行耗时:{}][影响行数:{}]", random, action, DateUtil.format(millis), LogUtil.format(qty, 34));
            }
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("execute exception:",e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:{}]{}", random, LogUtil.format("命令执行异常:", 33)+e, action, run.log(ACTION.DML.EXECUTE,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                throw new CommandUpdateException("命令执行异常", e);
            }

        }
        return result;
    }

    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param runs 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, ConfigStore configs, List<Run> runs) {
        long result = -1;
        if(null == random) {
            random = random(runtime);
        }

        long fr = System.currentTimeMillis();
        String action = "executes";
        if(null != configs) {
            configs.runs(runs);
        }
        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return -1;
        }
        long millis = -1;
        try{
            result = actuator.execute(this, runtime, random, configs, runs);
            millis = System.currentTimeMillis() - fr;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 &&ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    log.warn("{}[slow cmd][action:{}][执行耗时:{}]", random, action, DateUtil.format(millis));
                }
            }
            if (log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
                String qty = ""+result;
                log.info("{}[action:{}][执行耗时:{}][影响行数:{}]", random, action, DateUtil.format(millis), LogUtil.format(qty, 34));
            }
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("execute exception:",e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:{}]", random, LogUtil.format("命令执行异常:", 33)+e, action);
            }
            if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                throw new CommandUpdateException("命令执行异常", e);
            }

        }
        return result;
    }
    /* *****************************************************************************************************************
     *                                                     DELETE
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, Collection<T> values)
     * long delete(DataRuntime runtime, String random, String table, ConfigStore configs, Object obj, String... columns)
     * long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions)
     * long truncate(DataRuntime runtime, String random, String table)
     * [命令合成]
     * List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns)
     * List<Run> buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String column, Object values)
     * List<Run> buildTruncateRun(DataRuntime runtime, String table)
     * List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,String column, Object values)
     * List<Run> buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns)
     * void fillDeleteRunContent(DataRuntime runtime, Run run)
     * [命令执行]
     * long delete(DataRuntime runtime, String random, ConfigStore configs, Run run)
     ******************************************************************************************************************/
    /**
     * delete [调用入口]<br/>
     * <br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param values 列对应的值
     * @return 影响行数
     * @param <T> T
     */
    @Override
    public <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String key, Collection<T> values) {
        long result = -1;
        if(null == random) {
            random = random(runtime);
        }
        ACTION.SWITCH swt = InterceptorProxy.prepareDelete(runtime, random, batch, table, configs, key, values);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.prepareDelete(runtime, random, batch, table, key, values);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        List<Run> runs = buildDeleteRun(runtime, batch, table, configs, true, true, key, values);
        for(Run run:runs) {

            if(!run.isValid()) {
                if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                    log.warn("[valid:false][不具备执行条件][table:" +table+ "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
                }
                continue;
            }
            if(result == -1) {
                result = 1;
            }
            result += delete(runtime, random, configs, run);
        }
        return result;
    }

    /**
     * delete [调用入口]<br/>
     * <br/>
     * 合成 where k1 = v1 and k2 = v2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
     * @return 影响行数
     */
    @Override
    public long delete(DataRuntime runtime, String random, Table dest, ConfigStore configs, Object obj, String... columns) {
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        long size = -1;
        if(null != obj) {
            swt = InterceptorProxy.prepareDelete(runtime, random, 0, dest, obj, configs, columns);
            if(swt == ACTION.SWITCH.BREAK) {
                return -1;
            }
            if(null != dmListener) {
                swt = dmListener.prepareDelete(runtime, random, 0, dest, obj, columns);
            }
            if(swt == ACTION.SWITCH.BREAK) {
                return -1;
            }
            List<Run> runs = buildDeleteRun(runtime, dest, configs, obj, true, true, columns);
            for(Run run:runs) {
                if(!run.isValid()) {
                    if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                        log.warn("[valid:false][不具备执行条件][dest:" + dest + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
                    }
                    continue;
                }
                if(size == -1) {
                    size = 0;
                }
                size += delete(runtime, random, configs, run);
            }
        }
        return size;
    }

    /**
     * delete [调用入口]<br/>
     * <br/>
     * 根据configs和conditions过滤条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @param configs 查询条件及相关设置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    @Override
    public long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions) {
        long result = -1;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        swt = InterceptorProxy.prepareDelete(runtime, random, 0, table, configs, conditions);
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        if(null != dmListener) {
            swt = dmListener.prepareDelete(runtime, random, 0, table, configs, conditions);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return -1;
        }
        List<Run> runs = buildDeleteRun(runtime, table, configs, null, true, true, conditions);
        for(Run run:runs) {
            if(!run.isValid()) {
                if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                    log.warn("[valid:false][不具备执行条件][table:" + table + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
                }
        		continue;
			}
			if(result == -1) {
				result = 0;
			}
			result += delete(  runtime, random, configs, run);
		}
		return result;
	}

	/**
	 * truncate [调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return 1表示成功执行
	 */
	@Override
	public long truncate(DataRuntime runtime, String random, Table table) {
		List<Run> runs = buildTruncateRun(runtime, table);
		if(null != runs && !runs.isEmpty()) {
			RunPrepare prepare = new DefaultTextPrepare(runs.get(0).getFinalUpdate());
			return (int)execute(runtime, random, prepare, null);
		}
		return -1;
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
		List<Run> runs = new ArrayList<>();
		if(null == obj && (null == configs || configs.isEmptyCondition())) {
			return null;
		}
		if(obj instanceof Collection) {
			Collection list = (Collection) obj;
			for(Object item:list) {
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
		if(null == dest || BasicUtil.isEmpty(dest.getName())) {
			runs = buildDeleteRunFromConfig(runtime, configs, placeholder, unicode);
		}else if(obj instanceof ConfigStore) {
			Run run = new TableRun(runtime, dest);
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
		convert(runtime, new DefaultConfigStore(), runs);

		return runs;
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param key 列
	 * @param values values
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String key, Object values) {
		List<Run> runs = buildDeleteRunFromTable(runtime, batch, table, configs, placeholder, unicode, key, values);
		convert(runtime, new DefaultConfigStore(), runs);
		return runs;
	}

	/**
	 * delete[命令合成]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildTruncateRun(DataRuntime runtime, Table table) {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("TRUNCATE TABLE ");
		name(runtime, builder, table);
		return runs;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
	 * @param column 列
	 * @param values values
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,String column, Object values)", 37));
		}
		return null;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源 如果为空 可以根据obj解析
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
	 */
	@Override
	public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String... columns) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String... columns)", 37));
		}
		return null;
	}

	/**
	 * delete[命令合成-子流程]<br/>
	 * 构造查询主体 拼接where group等(不含分页 ORDER)
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 */
	@Override
	public void fillDeleteRunContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 void fillDeleteRunContent(DataRuntime runtime, Run run)", 37));
		}
	}

	/**
	 * delete[命令执行]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param configs 查询条件及相关设置
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run) {
		long result = -1;
		boolean cmd_success = false;
		if(null == random) {
			random = random(runtime);
		}
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
		long millis = -1;

		result = execute(runtime, random, configs, run);
		cmd_success = true;
		millis = System.currentTimeMillis() - fr;

		if(null != dmListener) {
			dmListener.afterDelete(runtime, random, run, cmd_success, result, millis);
		}
		InterceptorProxy.afterDelete(runtime, random, run, configs, cmd_success, result, millis);
		return result;
	}

	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * database			: 数据库(catalog, schema)
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

	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * String version(DataRuntime runtime, String random)
	 * String product(DataRuntime runtime, String random)
	 * Database database(DataRuntime runtime, String random)
	 * <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, String name)
	 * <T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, String name)
	 * Database database(DataRuntime runtime, String random, String name)
	 * Database database(DataRuntime runtime, String random)
	 * String String product(DataRuntime runtime, String random);
	 * String String version(DataRuntime runtime, String random);
	 * [命令合成]
	 * List<Run> buildQueryProductRun(DataRuntime runtime)
	 * List<Run> buildQueryVersionRun(DataRuntime runtime)
	 * List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryProductRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryVersionRun(DataRuntime runtime, boolean greedy, String name)
	 * List<Run> buildQueryDatabaseRun(DataRuntime runtime)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set)
	 * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet set)
	 * Database database(DataRuntime runtime, boolean create, Database dataase, DataSet set)
	 * Database database(DataRuntime runtime, boolean create, Database dataase)
	 * String product(DataRuntime runtime, boolean create, Database product, DataSet set)
	 * String product(DataRuntime runtime, boolean create, String product)
	 * String version(DataRuntime runtime, int index, boolean create, String version, DataSet set)
	 * String version(DataRuntime runtime, boolean create, String version)
	 * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog)
	 * Schema schema(DataRuntime runtime, boolean create, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, boolean create, Schema schema)
	 * Database database(DataRuntime runtime, boolean create, Database dataase)
	 * String product(DataRuntime runtime, int index, boolean create, String product, DataSet set)
	 * String product(DataRuntime runtime, boolean create, String product)
	 ******************************************************************************************************************/
	/**
	 * database[调用入口]<br/>
	 * 当前数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Database
	 */
	@Override
	public Database database(DataRuntime runtime, String random) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, String random)", 37));
		}
		return null;
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库描述(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	@Override
	public String product(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		String product = null;
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryProductRun(runtime);
				if (null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						product = product(runtime, idx++, true, product, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[product][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if(null == product) {
				product = product(runtime, false, product);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[product][result:{}][执行耗时:{}]", random, product, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[product][result:fail][msg:{}]", e.toString());
			}
		}
		return product;
	}

	/**
	 * database[调用入口]<br/>
	 * 当前数据源 数据库类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return String
	 */
	@Override
	public String version(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		String version = null;
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryProductRun(runtime);
				if (null != runs) {
					int idx = 0;
					for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
						version = version(runtime, idx++, true, version, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[version][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if(null == version) {
				version = version(runtime, false, version);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[version][result:{}][执行耗时:{}]", random, version, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[version][result:fail][msg:{}]", e.toString());
			}
		}
		return version;
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
	 * @param query 查询条件 根据metadata属性
	 * @return LinkedHashMap
	 */
	@Override
	public <T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, Database query) {
		String name = query.getName();
		if(null == random) {
			random = random(runtime);
		}
		List<T> databases = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
				List<Run> runs = buildQueryDatabasesRun(runtime, greedy, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						databases = databases(runtime, idx++, true, databases, query, set);
					}
				}
				if(databases.isEmpty()) {
					databases = getActuator().databases(this, runtime, query);
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}]", random, databases.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
	}

	/**
	 * database[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return LinkedHashMap
	 */
	@Override
	public <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, Database query) {
		String name = query.getName();
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, T> databases = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabasesRun(runtime, false, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						databases = databases(runtime, idx++, true, databases, query, set);
					}
				}
				if(databases.isEmpty()) {
					List<T> list = getActuator().databases(this, runtime, query);
					for(T item:list) {
						databases.put(item.getName().toUpperCase(), item);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}]", random, databases.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库产品说明(产品名称+版本号)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryProductRun(DataRuntime runtime) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryProductRun(DataRuntime runtime)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * database[命令合成]<br/>
	 * 查询当前数据源 数据库版本 版本号比较复杂 不是全数字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryVersionRun(DataRuntime runtime) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryVersionRun(DataRuntime runtime)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * database[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, Database query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, Database query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * database[结果集封装]<br/>
     * database 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initDatabaseFieldRefer() {
        return new MetadataFieldRefer(Database.class);
    }

	/**
	 * database[结果集封装]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	@Override
	public <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Database query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
    }

    /**
     * database[结果集封装]<br/>
     *
     * @param runtime  运行环境主要包含驱动适配器 数据源或客户端
     * @param index    第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create   上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param set      查询结果集
     * @return List
     * @throws Exception 异常
     */
    @Override
    public <T extends Database> List<T> databases(DataRuntime runtime, int index, boolean create, List<T> previous, Database query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
    }

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param meta 上一步查询结果
	 * @param set 查询结果集
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, int index, boolean create, Database meta, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, int index, boolean create, Database meta, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param meta 上一步查询结果
	 * @return database
	 * @throws Exception 异常
	 */
	@Override
	public Database database(DataRuntime runtime, boolean create, Database meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Database database(DataRuntime runtime, boolean create, Database meta)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @param set 查询结果集
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, int index, boolean create, String product, DataSet set) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String product(DataRuntime runtime, int index, boolean create, String product, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, boolean create, String product) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String product(DataRuntime runtime, boolean create, String product)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @param set 查询结果集
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, int index, boolean create, String version, DataSet set) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String version(DataRuntime runtime, int index, boolean create, String version, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, boolean create, String version) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 String version(DataRuntime runtime, boolean create, String version)", 37));
		}
		return null;
	}

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果封装 schema 对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Database
     */
    @Override
    public <T extends Database> T init(DataRuntime runtime, int index, T meta, Database query, DataRow row) {
        if(null == meta) {
            meta = (T)new Database();
        }
        MetadataFieldRefer refer = refer(runtime, Database.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, Database.FIELD_NAME));
        return meta;
    }

    /**
     * database[结果集封装]<br/>
     * 根据查询结果封装 database 对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    @Override
    public <T extends Database> T detail(DataRuntime runtime, int index, T meta, Database query, DataRow row) {
        MetadataFieldRefer refer = refer(runtime, Database.class);
        meta.setUser(getString(row, refer, Database.FIELD_USER));
        meta.setEngine(getString(row, refer, Database.FIELD_ENGINE));
        return meta;
    }
	/* *****************************************************************************************************************
	 * 													catalog
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, String name)
	 * <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
	 * [命令合成]
	 * List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
	 * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
	 * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs)
	 * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs)
	 *
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set)
	 * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog)
	 ******************************************************************************************************************/

	/**
	 * catalog[调用入口]<br/>
	 * 当前Catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Catalog
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		Catalog catalog = null;
		try{
			long fr = System.currentTimeMillis();
			//根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogRun(runtime, random);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						catalog = catalog(runtime, idx++, true, catalog, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalog][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			//根据JDBC接口补充
			try{
				catalog = catalog(runtime, true, catalog);
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalog][{}][msg:{}]", random, LogUtil.format("根据JDBC接口补充失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalog][result:{}][执行耗时:{}]", random, catalog, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalog][result:fail][msg:{}]", e.toString());
			}
		}
		return catalog;
	}

	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return LinkedHashMap
	 */
	@Override
	public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, Catalog query) {
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, T> catalogs = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryCatalogsRun(runtime, false, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						catalogs = catalogs(runtime, idx++, true, catalogs, query, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalogs][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalogs][result:{}][执行耗时:{}]", random, catalogs.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalogs][result:fail][msg:{}]", e.toString());
			}
		}
		return catalogs;
	}

	/**
	 * catalog[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return LinkedHashMap
	 */
	@Override
	public <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, Catalog query) {
		if(null == random) {
			random = random(runtime);
		}
		List<T> catalogs = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
				List<Run> runs = buildQueryCatalogsRun(runtime, greedy, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						catalogs = catalogs(runtime, idx++, true, catalogs, query, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[catalogs][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[catalogs][result:{}][执行耗时:{}]", random, catalogs.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[catalogs][result:fail][msg:{}]", e.toString());
			}
		}
		return catalogs;
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询当前catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogRun(DataRuntime runtime, String random) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryCatalogRun(DataRuntime runtime, String random)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, Catalog query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, Catalog query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * Catalog[结果集封装]<br/>
     * Catalog 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initCatalogFieldRefer() {
        return new MetadataFieldRefer(Catalog.class);
    }
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog query, DataSet set) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果集构造 Database
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Catalog> List<T> catalogs(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous)", 37));
		}
		return new LinkedHashMap<>();
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据驱动内置接口补充 catalog
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return catalogs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Catalog> List<T> catalogs(DataRuntime runtime, boolean create, List<T> previous) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Catalog> List<T> catalogs(DataRuntime runtime, boolean create, List<T> previous)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param meta 上一步查询结果
	 * @param set 查询结果集
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog meta, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog meta, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 当前catalog 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param meta 上一步查询结果
	 * @return Catalog
	 * @throws Exception 异常
	 */
	@Override
	public Catalog catalog(DataRuntime runtime, boolean create, Catalog meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Catalog catalog(DataRuntime runtime, boolean create, Catalog meta)", 37));
		}
		return null;
	}

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果封装 catalog 对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Catalog
     */
    @Override
    public <T extends Catalog> T init(DataRuntime runtime, int index, T meta, Catalog query, DataRow row) {
        if(null == meta) {
            meta = (T)new Catalog();
        }
        MetadataFieldRefer refer = refer(runtime, Catalog.class);
        String name = row.getString(refer.maps(Catalog.FIELD_NAME));
        meta.setMetadata(row);
        meta.setName(name);
        return meta;
    }

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果封装 catalog 对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    @Override
    public <T extends Catalog> T detail(DataRuntime runtime, int index, T meta, Catalog query, DataRow row) {
        return meta;
    }
	/* *****************************************************************************************************************
	 * 													schema
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Catalog catalog, String name)
	 * <T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name)
	 * [命令合成]
	 * List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name)
	 * [结果集封装]<br/>
	 * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet set)
	 * List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set)
	 * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema)
	 ******************************************************************************************************************/

	/**
	 * schema[调用入口]<br/>
	 * 当前Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @return Schema
	 */
	@Override
	public Schema schema(DataRuntime runtime, String random) {
		if(null == random) {
			random = random(runtime);
		}
		Schema schema = null;
		try{
			long fr = System.currentTimeMillis();
			//根据系统表查询
			try{
				List<Run> runs = buildQuerySchemaRun(runtime, random);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						schema = schema(runtime, idx++, true, schema, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schema][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			//根据JDBC接口补充
			try{
				schema = schema(runtime, true, schema);
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schema][{}][msg:{}]", random, LogUtil.format("根据JDBC接口补充失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schema][result:{}][执行耗时:{}]", random, schema, DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schema][result:fail][msg:{}]", e.toString());
			}
		}
		return schema;
	}
    public DataSet selectMetadata(DataRuntime runtime, String random, Run run) {
        ConfigStore configs = run.getConfigs();
        if(null == configs) {
            configs = new DefaultConfigStore();
            run.setConfigStore(configs);
        }
        configs.keyCase(KeyAdapter.KEY_CASE.PUT_UPPER);
        configs.IS_ENABLE_PLACEHOLDER_REGEX_EXT(false);
        if(run instanceof SimpleRun) {
            String text = run.getBuilder().toString();
            RunPrepare prepare = new DefaultTextPrepare(text);
            run = buildQueryRun(runtime, prepare, configs, true, true);
        }
        DataSet set = select(runtime, random, true, (Table)null, configs, run);
        return set;
    }
	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return LinkedHashMap
	 */
	@Override
	public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Schema query) {
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, T> schemas = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySchemasRun(runtime, false, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						schemas = schemas(runtime, idx++, true, schemas, query, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schemas][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schemas][result:{}][执行耗时:{}]", random, schemas.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schemas][result:fail][msg:{}]", e.toString());
			}
		}
		return schemas;
	}

	/**
	 * schema[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return LinkedHashMap
	 */
	@Override
	public <T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, Schema query) {
		if(null == random) {
			random = random(runtime);
		}
		List<T> schemas = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
				List<Run> runs = buildQuerySchemasRun(runtime, greedy, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						schemas = schemas(runtime, idx++, true, schemas, query, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[schemas][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[schemas][result:{}][执行耗时:{}]", random, schemas.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[schemas][result:fail][msg:{}]", e.toString());
			}
		}
		return schemas;
	}

	/**
	 * schema[命令合成]<br/>
	 * 查询当前schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQuerySchemaRun(DataRuntime runtime, String random) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySchemaRun(DataRuntime runtime, String random)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * catalog[命令合成]<br/>
	 * 查询全部数据库
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
	 * @return runs
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Schema query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Schema query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * Schema[结果集封装]<br/>
     * Schema 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initSchemaFieldRefer() {
        return new MetadataFieldRefer(Schema.class);
    }
	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Schema query, DataSet set) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}
	/**
	 * schema[结果集封装]<br/>
	 * 根据查询结果集构造 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Schema> List<T> schemas(DataRuntime runtime, int index, boolean create, List<T> previous, Schema query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Schema query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Schema query)", 37));
		}
		return previous;
	}

	/**
	 * schema[结果集封装]<br/>
	 * 根据驱动内置接口补充 Schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return databases
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Schema> List<T> schemas(DataRuntime runtime, boolean create, List<T> previous, Schema query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Schema> List<T> schemas(DataRuntime runtime, boolean create, List<T> previous, Schema query)", 37));
		}
        return previous;
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQuerySchemaRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param meta 上一步查询结果
	 * @param set 查询结果集
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, int index, boolean create, Schema meta, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Schema schema(DataRuntime runtime, int index, boolean create, Schema meta, DataSet set)", 37));
		}
		return null;
	}

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据驱动内置接口补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param meta 上一步查询结果
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, boolean create, Schema meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 Schema schema(DataRuntime runtime, boolean create, Schema meta)", 37));
		}
		return null;
	}

    /**
     * 根据结果集对象获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param comment 是否需要查询列注释
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String,Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String,Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment)", 37));
        }
        return null;
    }

	/**
	 * 检测name,name中可能包含catalog.schema.name<br/>
	 * 如果有一项或三项，在父类中解析<br/>
	 * 如果只有两项，需要根据不同数据库区分出最前一项是catalog还是schema，如果区分不出来的抛出异常
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表,视图等
	 * @return T
	 * @throws Exception 如果区分不出来的抛出异常
	 */
	@Override
	public <T extends Metadata> T checkName(DataRuntime runtime, String random, T meta) throws RuntimeException {
		if(null == meta) {
			return null;
		}
		String name = meta.getName();
		if(null != name && name.contains(".")) {
			String[] ks = name.split("\\.");
			if(ks.length == 3) {
				meta.setCatalog(ks[0]);
				meta.setSchema(ks[1]);
				meta.setName(ks[2]);
			}else{
				throw new RuntimeException("无法实别schema或catalog(子类" + this.getClass().getSimpleName() + "未实现)");
			}
		}
		return meta;
	}

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果封装 schema 对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Schema
     */
    @Override
    public  <T extends Schema> T init(DataRuntime runtime, int index, T meta, Schema query, DataRow row) {
        Catalog catalog = query.getCatalog();
        if(null == meta) {
            meta = (T)new Schema();
        }
        MetadataFieldRefer refer = refer(runtime, Schema.class);
        String _catalog = row.getString(refer.maps(Schema.FIELD_CATALOG));
        if(null == _catalog && null != catalog) {
            _catalog = catalog.getName();
        }
        String name = row.getString(refer.maps(Schema.FIELD_NAME));
        if(null != _catalog) {
            _catalog = _catalog.trim();
        }
        meta.setUser(row.getString(refer.maps(Schema.FIELD_USER)));
        meta.setMetadata(row);
        meta.setCatalog(_catalog);
        meta.setName(name);
        return meta;
    }

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果封装 schema 对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    @Override
    public <T extends Schema> T detail(DataRuntime runtime, int index, T meta, Schema query, DataRow row) {
        return meta;
    }
	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Table table, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, Table table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
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
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Table query, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
            if(!greedy) {
                checkSchema(runtime, query);
            }
            Catalog catalog = query.getCatalog();
            Schema schema = query.getSchema();
            String pattern = query.getName();
			String caches_key = CacheProxy.key(runtime, "tables", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.tables(caches_key);
			if(null == list || list.isEmpty()) {
                String cache_key = CacheProxy.key(runtime, "table_name_map", greedy, catalog, schema, pattern);
                String origin = CacheProxy.name(cache_key);
                if (null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
                    //先查出所有key并以大写缓存 用来实现忽略大小写
                    tableMap(runtime, random, greedy, query, new DefaultConfigStore());
                    origin = CacheProxy.name(cache_key);
                }
                if (null != origin) {
                    query.setName(origin);
                } else {
                    origin = query.getName();
                }
                PageNavi navi = null;
                if (null == configs) {
                    configs = new DefaultConfigStore();
                }
                navi = configs.getPageNavi();
                // 根据系统表查询
                try {
                    List<Run> runs = buildQueryTablesRun(runtime, greedy, query, types, configs);
                    if (null != runs) {
                        int idx = 0;
                        for (Run run : runs) {
                            if (null != navi) {
                                run.setPageNavi(navi);
                                mergeFinalQuery(runtime, run);
                            }
                            DataSet set = selectMetadata(runtime, random, run);
                            list = tables(runtime, idx++, true, list, catalog, schema, set);
                            if (null != navi) {
                                //分页只查一次
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                        e.printStackTrace();
                    } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                        log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                    }
                }

                // 根据系统表查询失败后根据驱动内置接口补充
                if(null == list || list.isEmpty()) {
                    try {
                        list = tables(runtime, true, list, catalog, schema, origin, types);
                        //删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
                        if (!greedy) {
                            int size = list.size();
                            for (int i = size - 1; i >= 0; i--) {
                                Table item = list.get(i);
                                if (!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
                                    list.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else {
                            log.warn("{}[tables][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                boolean comment = false;
                for (Table table : list) {
                    if (BasicUtil.isNotEmpty(table.getComment())) {
                        comment = true;
                        break;
                    }
                }
                //表备注
                if (!comment) {
                    try {
                        List<Run> runs = buildQueryTablesCommentRun(runtime, catalog, schema, origin, types);
                        if (null != runs) {
                            int idx = 0;
                            for (Run run : runs) {
                                if (null != navi) {
                                    run.setPageNavi(navi);
                                    //mergeFinalQuery(runtime, run);
                                }
                                DataSet set = selectMetadata(runtime, random, run);
                                list = comments(runtime, idx++, true, list, catalog, schema, set);
                                if (null != navi) {
                                    break;
                                }
                                //merge(list, maps);
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                            log.info("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                    log.info("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
                }
                if (BasicUtil.isNotEmpty(origin)) {
                    origin = origin.replace("%", ".*");
                    //有表名的，根据表名过滤出符合条件的
                    List<T> tmp = new ArrayList<>();
                    for (T item : list) {
                        String name = item.getName(greedy) + "";
                        if (RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            if (
                                (BasicUtil.isEmpty(catalog) || equals(catalog, item.getCatalog())) //catalog 无要求 或 相等
                                && (BasicUtil.isEmpty(schema) || equals(schema, item.getSchema()))
                            ) {
                                tmp.add(item);
                            }
                        }
                    }
                    list = tmp;
                }
                CacheProxy.tables(caches_key, list);
            }

            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                //查询全部表结构 columns()内部已经给table.columns赋值
                for(Table item:list) {
                    if(null == item.getColumns() || item.getColumns().isEmpty()) {
                        Column column_query = new Column();
                        column_query.setCatalog(catalog);
                        column_query.setSchema(schema);
                        columns(runtime, random, greedy, list, column_query);
                        break;
                    }
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                //查询全部表结构 indexes()内部已经给table.indexes赋值
                for(Table item:list) {
                    if(null == item.getIndexes() || item.getIndexes().isEmpty()) {
                        Index index_qeury = new Index();
                        index_qeury.setCatalog(catalog);
                        index_qeury.setSchema(schema);
                        indexes(runtime, random, greedy, list, index_qeury);
                        break;
                    }
                }
            }
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[tables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 */
	protected void tableMap(DataRuntime runtime, String random, boolean greedy, Table query, ConfigStore configs) {
		Catalog catalog = query.getCatalog();
		Schema schema = query.getSchema();
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
			if(null == random) {
				random = random(runtime);
			}
			DriverAdapter adapter = runtime.getAdapter();
			List<Table> tables = null;
			boolean sys = false; //根据系统表查询
			if(greedy) {
				catalog = null;
				schema = null;
			}
			try {
				//缓存 不需要configs条件及分页
				List<Run> runs = buildQueryTablesRun(runtime, greedy, catalog, schema, null, Table.TYPE.NORMAL.value, new DefaultConfigStore());
				if (null != runs && !runs.isEmpty()) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = selectMetadata(runtime, random, run);
						tables = tables(runtime, idx++, true, tables, catalog, schema, set);
						for(Table table:tables) {
							String cache_key = CacheProxy.key(runtime, "table_name_map", greedy, catalog, schema, table.getName());
							CacheProxy.name(cache_key, table.getName());
						}
						sys = true;
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			if(!sys) {
				try {
					tables = tables(runtime, true, tables, catalog, schema, null, Table.TYPE.NORMAL.value);
					for(Table table:tables) {
						String cache_key = CacheProxy.key(runtime, "table_name_map", greedy, catalog, schema, table.getName());
						CacheProxy.name(cache_key, table.getName());
					}
				}catch (Exception e) {
					e.printStackTrace();
				}
			}
		//}

	}

	/**
	 * table[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Table query, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		List<T> list = tables(runtime, random, false, query, types, struct, configs);
		for(T table:list) {
			tables.put(table.getName().toUpperCase(), table);
		}
		return tables;
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Table query, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTablesRun(DataRuntime runtime, Table query, String pattern, int types)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * Table[结果集封装]<br/>
     * Table 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTableFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Table.class);
        refer.map(Table.FIELD_NAME, "TABLE_NAME,NAME,TABNAME");
        refer.map(Table.FIELD_CATALOG, "TABLE_CATALOG");
        refer.map(Table.FIELD_SCHEMA, "TABLE_SCHEMA,TABSCHEMA,SCHEMA_NAME");
        refer.map(Table.FIELD_COMMENT, "TABLE_COMMENT,COMMENTS,COMMENT");
        return refer;
    }

    /**
     * Table[结果集封装]<br/>
     * TableComment 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTableCommentFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(TableComment.class);
        refer.map(TableComment.FIELD_VALUE, "TABLE_COMMENT");
        refer.map(TableComment.FIELD_TABLE,  "TABLE_NAME");
        refer.map(TableComment.FIELD_CATALOG, "TABLE_CATALOG");
        refer.map(TableComment.FIELD_SCHEMA, "TABLE_SCHEMA");
        return refer;
    }
	/**
	 * table[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Table query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Table query, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, catalog, schema, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, catalog, schema, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet set) throws Exception {
		if(null == previous) {
			previous = new ArrayList<>();
		}
		for(DataRow row:set) {
			T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
			if(null == search(previous, meta.getCatalog(), meta.getSchema(), meta.getName())) {
				previous.add(meta);
			}
			detail(runtime, index, meta, query, row);
		}
		return previous;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Table query, int types)", 37));
		}
		if(null == previous) {
            previous = new LinkedHashMap<>();
		}
		return previous;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return tables
	 * @throws Exception 异常
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> previous, Table query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Table query, int types)", 37));
		}
		if(null == previous) {
            previous = new ArrayList<>();
		}
		return previous;
	}

	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Table
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> T init(DataRuntime runtime, int index, T meta, Table query, DataRow row) {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        MetadataFieldRefer refer = refer(runtime, Table.class);
        String _catalog = getString(row, refer, Table.FIELD_CATALOG);
        String _schema = getString(row, refer, Table.FIELD_SCHEMA);
        if(null == _catalog && null != catalog) {
            _catalog = catalog.getName();
        }
        if(null == _schema && null != schema) {
            _schema = schema.getName();
        }
        String name = getString(row, refer, Table.FIELD_NAME);

        if(null == meta) {
            if("VIEW".equals(getString(row, refer, Table.FIELD_TYPE))) {
                meta = (T)new View();
            }else {
                String[] chks = refer.maps(Table.FIELD_MASTER_CHECK);
                String[] vals = refer.maps(Table.FIELD_MASTER_CHECK_VALUE);
                Boolean bol = matchBoolean(row, chks, vals);
                if(null != bol && bol) {
                    meta = (T) new MasterTable();
                }else {
                    meta = (T) new Table();
                }
            }
        }
        if(null != _catalog) {
            _catalog = _catalog.trim();
        }
        if(null != _schema) {
            _schema = _schema.trim();
        }
        meta.setMetadata(row);
        meta.setCatalog(_catalog);
        meta.setSchema(_schema);
        meta.setName(name);
        return meta;
	}
	/**
	 * table[结果集封装]<br/>
	 * 根据查询结果封装Table对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Table
	 * @param <T> Table
	 */
	@Override
	public <T extends Table> T detail(DataRuntime runtime, int index, T meta, Table query, DataRow row) {
        MetadataFieldRefer refer = refer(runtime, Table.class);
        meta.setObjectId(getLong(row, refer, Table.FIELD_OBJECT_ID));
        meta.setComment(getString(row, refer, Table.FIELD_COMMENT));
        meta.setDataRows(getLong(row, refer, Table.FIELD_DATA_ROWS));
        meta.setCollate(getString(row, refer, Table.FIELD_COLLATE));
        meta.setDataLength(getLong(row, refer, Table.FIELD_DATA_LENGTH));
        meta.setDataFree(getLong(row, refer, Table.FIELD_DATA_FREE));
        meta.setIncrement(getLong(row, refer, Table.FIELD_INCREMENT));
        meta.setIndexLength(getLong(row, refer, Table.FIELD_INDEX_LENGTH));
        meta.setCreateTime(getDate(row, refer, Table.FIELD_CREATE_TIME));
        meta.setUpdateTime(getDate(row, refer, Table.FIELD_UPDATE_TIME));
        meta.setType(getString(row, refer, Table.FIELD_TYPE));
        meta.setEngine(getString(row, refer, Table.FIELD_ENGINE));
        meta.setTemporary(getBoolean(row, refer, Table.FIELD_TEMPORARY, false));
        return meta;
	}

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        MetadataFieldRefer refer = new MetadataFieldRefer(TableComment.class);
        for(DataRow row:set) {
            String tab = getString(row, refer, TableComment.FIELD_TABLE);
            if(null == tab) {
                continue;
            }
            String comment = getString(row, refer, TableComment.FIELD_VALUE);
            if(null != tab && null != comment) {
                Table table = previous.get(tab.toUpperCase());
                if(null != table) {
                    table.setComment(comment);
                }
            }
        }
        return previous;
	}

	/**
	 * table[结果集封装]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet set) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == previous) {
            previous = new ArrayList<>();
        }
        MetadataFieldRefer refer = new MetadataFieldRefer(TableComment.class);
        for(DataRow row:set) {
            String tab = getString(row, refer, TableComment.FIELD_TABLE);
            if(null == tab) {
                continue;
            }
            String comment = getString(row, refer, TableComment.FIELD_VALUE);
            String catlog_ = getString(row, refer, TableComment.FIELD_CATALOG);
            String schema_ = getString(row, refer, TableComment.FIELD_SCHEMA);
            if(null == catalog && BasicUtil.isNotEmpty(catlog_)) {
                catalog = new Catalog(catlog_);
            }
            if(null == schema && BasicUtil.isNotEmpty(schema_)) {
                schema = new Schema(schema_);
            }

            boolean contains = true;
            T table = search(previous, catalog, schema, tab);
            if (null == table) {
                if (create) {
                    table = (T) new Table(catalog, schema, tab);
                    contains = false;
                } else {
                    continue;
                }
            }
            table.setComment(comment);
            if (!contains) {
                previous.add(table);
            }
        }
        return previous;
	}

	/**
	 *
	 * table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, table);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, table, list, set);
				}
				table.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = table.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, table, true);
					table.setColumns(columns);
					table.setTags(tags(runtime, random, false, table));
				}
				PrimaryKey pk = table.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, table);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				table.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = table.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					table.setIndexes(indexes(runtime, random, table, null));
				}
				runs = buildCreateRun(runtime, table);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					table.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[table ddl][table:{}][result:{}][执行耗时:{}]", random, table.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * table[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, Table table) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, Table table)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param table 表
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/* *****************************************************************************************************************
	 * 													vertex
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryVertexsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
	 * List<Run> buildQueryVertexsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
	 * [结果集封装]<br/>
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexs, Catalog catalog, Schema schema, DataSet set)
	 * <T extends VertexTable> List<T> vertexs(DataRuntime runtime, int index, boolean create, List<T> vertexs, Catalog catalog, Schema schema, DataSet set)
	 * <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexs, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends VertexTable> List<T> vertexs(DataRuntime runtime, boolean create, List<T> vertexs, Catalog catalog, Schema schema, String pattern, int types)
	 * <T extends VertexTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexs, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, VertexTable vertex, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, VertexTable vertex)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, VertexTable vertex, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * vertex[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
	 * @return List
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, VertexTable query, int types, int struct, ConfigStore configs) {
        List<T> list = new ArrayList<>();
        if(null == random) {
            random = random(runtime);
        }
        try{
            long fr = System.currentTimeMillis();
            if(!greedy) {
                checkSchema(runtime, query);
            }
            Catalog catalog = query.getCatalog();
            Schema schema = query.getSchema();
            String pattern = query.getName();
            String caches_key = CacheProxy.key(runtime, "vertexs", greedy, catalog, schema, pattern, types, configs);
            list = CacheProxy.vertexs(caches_key);
            if(null == list || list.isEmpty()) {
                String cache_key = CacheProxy.key(runtime, "vertex_name_map", greedy, catalog, schema, pattern);
                String origin = CacheProxy.name(cache_key);
                if (null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
                    //先查出所有key并以大写缓存 用来实现忽略大小写
                    vertexMap(runtime, random, greedy, query, new DefaultConfigStore());
                    origin = CacheProxy.name(cache_key);
                }
                if (null != origin) {
                    query.setName(origin);
                } else {
                    origin = query.getName();
                }
                PageNavi navi = null;
                if (null == configs) {
                    configs = new DefaultConfigStore();
                }
                navi = configs.getPageNavi();
                // 根据系统表查询
                try {
                    List<Run> runs = buildQueryTablesRun(runtime, greedy, query, types, configs);
                    if (null != runs) {
                        int idx = 0;
                        for (Run run : runs) {
                            if (null != navi) {
                                run.setPageNavi(navi);
                                mergeFinalQuery(runtime, run);
                            }
                            DataSet set = selectMetadata(runtime, random, run);
                            list = vertexs(runtime, idx++, true, list, catalog, schema, set);
                            if (null != navi) {
                                //分页只查一次
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                        e.printStackTrace();
                    } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                        log.warn("{}[vertexs][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                    }
                }

                // 根据系统表查询失败后根据驱动内置接口补充
                if(null == list || list.isEmpty()) {
                    try {
                        list = vertexs(runtime, true, list, catalog, schema, origin, types);
                        //删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
                        if (!greedy) {
                            int size = list.size();
                            for (int i = size - 1; i >= 0; i--) {
                                Table item = list.get(i);
                                if (!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
                                    list.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else {
                            log.warn("{}[vertexs][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                boolean comment = false;
                for (Table vertex : list) {
                    if (BasicUtil.isNotEmpty(vertex.getComment())) {
                        comment = true;
                        break;
                    }
                }
                //表备注
                if (!comment) {
                    try {
                        List<Run> runs = buildQueryTablesCommentRun(runtime, catalog, schema, origin, types);
                        if (null != runs) {
                            int idx = 0;
                            for (Run run : runs) {
                                if (null != navi) {
                                    run.setPageNavi(navi);
                                    //mergeFinalQuery(runtime, run);
                                }
                                DataSet set = selectMetadata(runtime, random, run);
                                list = comments(runtime, idx++, true, list, catalog, schema, set);
                                if (null != navi) {
                                    break;
                                }
                                //merge(list, maps);
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                            log.info("{}[vertexs][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                    log.info("{}[vertexs][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
                }
                if (BasicUtil.isNotEmpty(origin)) {
                    origin = origin.replace("%", ".*");
                    //有表名的，根据表名过滤出符合条件的
                    List<T> tmp = new ArrayList<>();
                    for (T item : list) {
                        String name = item.getName(greedy) + "";
                        if (RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            if (equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
                                tmp.add(item);
                            }
                        }
                    }
                    list = tmp;
                }
            }

            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                //查询全部表结构 columns()内部已经给VertexTable.columns赋值
                for(VertexTable item:list) {
                    if(null == item.getColumns() || item.getColumns().isEmpty()) {
                        Column column_query = new Column();
                        column_query.setCatalog(catalog);
                        column_query.setSchema(schema);
                        columns(runtime, random, greedy, list, column_query);
                        break;
                    }
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                //查询全部表结构 indexes()内部已经给VertexTable.indexes赋值
                for(VertexTable item:list) {
                    if(null == item.getIndexes() || item.getIndexes().isEmpty()) {
                        Index index_qeury = new Index();
                        index_qeury.setCatalog(catalog);
                        index_qeury.setSchema(schema);
                        indexes(runtime, random, greedy, list, index_qeury);
                        break;
                    }
                }
            }
            CacheProxy.vertexs(caches_key, list);
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[vertexs][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 * vertex[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	protected void vertexMap(DataRuntime runtime, String random, boolean greedy, VertexTable query,  ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<VertexTable> vertexs = null;
		boolean sys = false; //根据系统表查询
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryVertexsRun(runtime, greedy, catalog, schema, null, VertexTable.TYPE.NORMAL.value, new DefaultConfigStore());
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = selectMetadata(runtime, random, run);
					vertexs = vertexs(runtime, idx++, true, vertexs, catalog, schema, set);
					for(VertexTable vertex:vertexs) {
						String cache_key = CacheProxy.key(runtime, "vertex", greedy, catalog, schema, vertex.getName());
						CacheProxy.name(cache_key, vertex.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				vertexs = vertexs(runtime, true, vertexs, catalog, schema, null, VertexTable.TYPE.NORMAL.value);
				for(VertexTable vertex:vertexs) {
					String cache_key = CacheProxy.key(runtime, "vertex", greedy, catalog, schema, vertex.getName());
					CacheProxy.name(cache_key, vertex.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * vertex[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, VertexTable query, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> vertexs = new LinkedHashMap<>();
		List<T> list = vertexs(runtime, random, false, query, types, struct, configs);
		for(T vertex:list) {
			vertexs.put(vertex.getName().toUpperCase(), vertex);
		}
		return vertexs;
	}

	/**
	 * vertex[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryVertexsRun(DataRuntime runtime, boolean greedy, VertexTable query, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryVertexsRun(DataRuntime runtime, VertexTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * vertex[结果集封装]<br/>
     * VertexTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initVertexFieldRefer() {
        return new MetadataFieldRefer(VertexTable.class);
    }

	/**
	 * vertex[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryVertexsCommentRun(DataRuntime runtime, VertexTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryVertexsCommentRun(DataRuntime runtime, VertexTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * vertex[结果集封装]<br/>
	 *  根据查询结果集构造VertexTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryVertexsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return vertexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, VertexTable query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * vertex[结果集封装]<br/>
	 *  根据查询结果集构造VertexTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryVertexsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return vertexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends VertexTable> List<T> vertexs(DataRuntime runtime, int index, boolean create, List<T> previous, VertexTable query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * vertex[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return vertexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, VertexTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexs, VertexTable query, int types)", 37));
		}
		if(null == previous) {
            previous = new LinkedHashMap<>();
		}
		return previous;
	}

	/**
	 * vertex[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return vertexs
	 * @throws Exception 异常
	 * @param <T> VertexTable
	 */
	@Override
	public <T extends VertexTable> List<T> vertexs(DataRuntime runtime, boolean create, List<T> previous, VertexTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> List<T> vertexs(DataRuntime runtime, boolean create, List<T> vertexs, VertexTable query, int types)", 37));
		}
		if(null == previous) {
            previous = new ArrayList<>();
		}
		return previous;
	}

	/**
	 *
	 * vertex[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param vertex 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, VertexTable vertex, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, vertex);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
                    DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, vertex, list, set);
				}
				vertex.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = vertex.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, vertex, true);
					vertex.setColumns(columns);
					vertex.setTags(tags(runtime, random, false, vertex));
				}
				PrimaryKey pk = vertex.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, vertex);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				vertex.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = vertex.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					vertex.setIndexes(indexes(runtime, random, vertex, null));
				}
				runs = buildCreateRun(runtime, vertex);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					vertex.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[vertex ddl][vertex:{}][result:{}][执行耗时:{}]", random, vertex.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[vertex ddl][{}][vertex:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), vertex.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * vertex[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param vertex 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, VertexTable vertex) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, VertexTable vertex)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * vertex[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param vertex 表
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, VertexTable vertex, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, VertexTable vertex, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

    /**
     * vertex[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return VertexTable
     * @param <T> VertexTable
     */
    @Override
    public <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, VertexTable query, DataRow row) {
        if(null == meta) {
            meta = (T)new VertexTable();
        }
        MetadataFieldRefer refer = refer(runtime, VertexTable.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, VertexTable.FIELD_NAME));

        return meta;
    }

    /**
     * vertex[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return VertexTable
     * @param <T> VertexTable
     */
    @Override
    public <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, VertexTable query, DataRow row) {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, VertexTable query, DataRow row)", 37));
        }
        return meta;
    }

    /* *****************************************************************************************************************
	 * 													EdgeTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, EdgeTable query, int types, boolean struct)
	 * <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, EdgeTable query, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryEdgesRun(DataRuntime runtime, boolean greedy, EdgeTable query, int types, ConfigStore configs)
	 * List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, EdgeTable query, int types)
	 * [结果集封装]<br/>
	 * <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edges, Catalog catalog, Schema schema, DataSet set)
	 * <T extends EdgeTable> List<T> edges(DataRuntime runtime, int index, boolean create, List<T> edges, Catalog catalog, Schema schema, DataSet set)
	 * <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edges, EdgeTable query, int types)
	 * <T extends EdgeTable> List<T> edges(DataRuntime runtime, boolean create, List<T> edges, EdgeTable query, int types)
	 * <T extends EdgeTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edges, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, EdgeTable meta)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * edge[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
	 * @return List
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, EdgeTable query, int types, int struct, ConfigStore configs) {

        List<T> list = new ArrayList<>();
        if(null == random) {
            random = random(runtime);
        }
        try{
            long fr = System.currentTimeMillis();
            if(!greedy) {
                checkSchema(runtime, query);
            }
            Catalog catalog = query.getCatalog();
            Schema schema = query.getSchema();
            String pattern = query.getName();
            String caches_key = CacheProxy.key(runtime, "edges", greedy, catalog, schema, pattern, types, configs);
            list = CacheProxy.edges(caches_key);
            if(null == list || list.isEmpty()) {
                String cache_key = CacheProxy.key(runtime, "edge_name_map", greedy, catalog, schema, pattern);
                String origin = CacheProxy.name(cache_key);
                if (null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
                    //先查出所有key并以大写缓存 用来实现忽略大小写
                    edgeMap(runtime, random, greedy, query, new DefaultConfigStore());
                    origin = CacheProxy.name(cache_key);
                }
                if (null != origin) {
                    query.setName(origin);
                } else {
                    origin = query.getName();
                }
                PageNavi navi = null;
                if (null == configs) {
                    configs = new DefaultConfigStore();
                }
                navi = configs.getPageNavi();
                // 根据系统表查询
                try {
                    List<Run> runs = buildQueryTablesRun(runtime, greedy, query, types, configs);
                    if (null != runs) {
                        int idx = 0;
                        for (Run run : runs) {
                            if (null != navi) {
                                run.setPageNavi(navi);
                                mergeFinalQuery(runtime, run);
                            }
                            DataSet set = selectMetadata(runtime, random, run);
                            list = edges(runtime, idx++, true, list, catalog, schema, set);
                            if (null != navi) {
                                //分页只查一次
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                        e.printStackTrace();
                    } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                        log.warn("{}[edges][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                    }
                }

                // 根据系统表查询失败后根据驱动内置接口补充
                if(null == list || list.isEmpty()) {
                    try {
                        list = edges(runtime, true, list, catalog, schema, origin, types);
                        //删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
                        if (!greedy) {
                            int size = list.size();
                            for (int i = size - 1; i >= 0; i--) {
                                Table item = list.get(i);
                                if (!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
                                    list.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else {
                            log.warn("{}[edges][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                boolean comment = false;
                for (Table edge : list) {
                    if (BasicUtil.isNotEmpty(edge.getComment())) {
                        comment = true;
                        break;
                    }
                }
                //表备注
                if (!comment) {
                    try {
                        List<Run> runs = buildQueryTablesCommentRun(runtime, catalog, schema, origin, types);
                        if (null != runs) {
                            int idx = 0;
                            for (Run run : runs) {
                                if (null != navi) {
                                    run.setPageNavi(navi);
                                    //mergeFinalQuery(runtime, run);
                                }
                                DataSet set = selectMetadata(runtime, random, run);
                                list = comments(runtime, idx++, true, list, catalog, schema, set);
                                if (null != navi) {
                                    break;
                                }
                                //merge(list, maps);
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                            log.info("{}[edges][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                    log.info("{}[edges][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
                }
                if (BasicUtil.isNotEmpty(origin)) {
                    origin = origin.replace("%", ".*");
                    //有表名的，根据表名过滤出符合条件的
                    List<T> tmp = new ArrayList<>();
                    for (T item : list) {
                        String name = item.getName(greedy) + "";
                        if (RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            if (equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
                                tmp.add(item);
                            }
                        }
                    }
                    list = tmp;
                }
                CacheProxy.edges(caches_key, list);
            }

            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                //查询全部表结构 columns()内部已经给EdgeTable.columns赋值
                for(EdgeTable item:list) {
                    if(null == item.getColumns() || item.getColumns().isEmpty()) {
                        Column column_query = new Column();
                        column_query.setCatalog(catalog);
                        column_query.setSchema(schema);
                        columns(runtime, random, greedy, list, column_query);
                        break;
                    }
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                //查询全部表结构 indexes()内部已经给EdgeTable.indexes赋值
                for(EdgeTable item:list) {
                    if(null == item.getIndexes() || item.getIndexes().isEmpty()) {
                        Index index_qeury = new Index();
                        index_qeury.setCatalog(catalog);
                        index_qeury.setSchema(schema);
                        indexes(runtime, random, greedy, list, index_qeury);
                        break;
                    }
                }
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[edges][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 * edge[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	protected void edgeMap(DataRuntime runtime, String random, boolean greedy, EdgeTable query, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<EdgeTable> edges = null;
		boolean sys = false; //根据系统表查询
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryEdgesRun(runtime, greedy, catalog, schema, null, EdgeTable.TYPE.NORMAL.value, new DefaultConfigStore());
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = selectMetadata(runtime, random, run);
					edges = edges(runtime, idx++, true, edges, catalog, schema, set);
					for(EdgeTable item:edges) {
						String cache_key = CacheProxy.key(runtime, "edge", greedy, catalog, schema, item.getName());
						CacheProxy.name(cache_key, item.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				edges = edges(runtime, true, edges, catalog, schema, null, EdgeTable.TYPE.NORMAL.value);
				for(EdgeTable item:edges) {
					String cache_key = CacheProxy.key(runtime, "edge", greedy, catalog, schema, item.getName());
					CacheProxy.name(cache_key, item.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * edge[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, EdgeTable query, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> edges = new LinkedHashMap<>();
		List<T> list = edges(runtime, random, false, query, types, struct, configs);
		for(T edge:list) {
			edges.put(edge.getName().toUpperCase(), edge);
		}
		return edges;
	}

	/**
	 * edge[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryEdgesRun(DataRuntime runtime, boolean greedy, EdgeTable query, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryEdgesRun(DataRuntime runtime, EdgeTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * edge[结果集封装]<br/>
     * EdgeTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initEdgeFieldRefer() {
        return new MetadataFieldRefer(EdgeTable.class);
    }

	/**
	 * edge[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, EdgeTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, EdgeTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * edge[结果集封装]<br/>
	 *  根据查询结果集构造EdgeTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryEdgesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return edges
	 * @throws Exception 异常
	 */
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, EdgeTable query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * edge[结果集封装]<br/>
	 *  根据查询结果集构造EdgeTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryEdgesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return edges
	 * @throws Exception 异常
	 */
	@Override
	public <T extends EdgeTable> List<T> edges(DataRuntime runtime, int index, boolean create, List<T> previous, EdgeTable query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * edge[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return edges
	 * @throws Exception 异常
	 */
	@Override
	public <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, EdgeTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edges, EdgeTable query, int types)", 37));
		}
		if(null == previous) {
            previous = new LinkedHashMap<>();
		}
		return previous;
	}

	/**
	 * edge[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return edges
	 * @throws Exception 异常
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> List<T> edges(DataRuntime runtime, boolean create, List<T> previous, EdgeTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> List<T> edges(DataRuntime runtime, boolean create, List<T> edges, EdgeTable query, int types)", 37));
		}
		if(null == previous) {
            previous = new ArrayList<>();
		}
		return previous;
	}

	/**
	 * edge[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return EdgeTable
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, EdgeTable query, DataRow row) {
        if(null == meta) {
            meta = (T)new EdgeTable();
        }
        MetadataFieldRefer refer = refer(runtime, EdgeTable.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, EdgeTable.FIELD_NAME));
        return meta;
	}
	/**
	 * edge[结果集封装]<br/>
	 * 根据查询结果封装EdgeTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return EdgeTable
	 * @param <T> EdgeTable
	 */
	@Override
	public <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, EdgeTable query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, EdgeTable query, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 *
	 * edge[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, meta);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
                    DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, meta, list, set);
				}
				meta.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = meta.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, meta, true);
					meta.setColumns(columns);
					meta.setTags(tags(runtime, random, false, meta));
				}
				PrimaryKey pk = meta.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, meta);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				meta.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = meta.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					meta.setIndexes(indexes(runtime, random, meta, null));
				}
				runs = buildCreateRun(runtime, meta);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					meta.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[edge ddl][edge:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[edge ddl][{}][edge:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * edge[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, EdgeTable meta) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, EdgeTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * edge[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param meta 表
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, View query, int types, boolean struct)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, View query, int types, ConfigStore configs)
	 * List<Run> buildQueryViewsCommentRun(DataRuntime runtime, View query, int types)
	 * [结果集封装]<br/>
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
	 * <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, View query, int types)
	 * <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, View query, int types)
	 * <T extends View> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, View view, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, View view)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
	 * @return List
	 * @param <T> View
	 */
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, View query, int types, int struct, ConfigStore configs) {
        List<T> list = new ArrayList<>();
        if(null == random) {
            random = random(runtime);
        }
        try{
            long fr = System.currentTimeMillis();
            if(!greedy) {
                checkSchema(runtime, query);
            }
            Catalog catalog = query.getCatalog();
            Schema schema = query.getSchema();
            String pattern = query.getName();
            String caches_key = CacheProxy.key(runtime, "views", greedy, catalog, schema, pattern, types, configs);
            list = CacheProxy.views(caches_key);
            if(null == list || list.isEmpty()) {
                String cache_key = CacheProxy.key(runtime, "view_name_map", greedy, catalog, schema, pattern);
                String origin = CacheProxy.name(cache_key);
                if (null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
                    //先查出所有key并以大写缓存 用来实现忽略大小写
                    viewMap(runtime, random, greedy, query, new DefaultConfigStore());
                    origin = CacheProxy.name(cache_key);
                }
                if (null != origin) {
                    query.setName(origin);
                } else {
                    origin = query.getName();
                }
                PageNavi navi = null;
                if (null == configs) {
                    configs = new DefaultConfigStore();
                }
                navi = configs.getPageNavi();
                // 根据系统表查询
                try {
                    List<Run> runs = buildQueryTablesRun(runtime, greedy, query, types, configs);
                    if (null != runs) {
                        int idx = 0;
                        for (Run run : runs) {
                            if (null != navi) {
                                run.setPageNavi(navi);
                                mergeFinalQuery(runtime, run);
                            }
                            DataSet set = selectMetadata(runtime, random, run);
                            list = views(runtime, idx++, true, list, catalog, schema, set);
                            if (null != navi) {
                                //分页只查一次
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                        e.printStackTrace();
                    } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                        log.warn("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                    }
                }

                // 根据系统表查询失败后根据驱动内置接口补充
                if(null == list || list.isEmpty()) {
                    try {
                        list = views(runtime, true, list, catalog, schema, origin, types);
                        //删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
                        if (!greedy) {
                            int size = list.size();
                            for (int i = size - 1; i >= 0; i--) {
                                Table item = list.get(i);
                                if (!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
                                    list.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else {
                            log.warn("{}[views][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                boolean comment = false;
                for (Table view : list) {
                    if (BasicUtil.isNotEmpty(view.getComment())) {
                        comment = true;
                        break;
                    }
                }
                //表备注
                if (!comment) {
                    try {
                        List<Run> runs = buildQueryTablesCommentRun(runtime, catalog, schema, origin, types);
                        if (null != runs) {
                            int idx = 0;
                            for (Run run : runs) {
                                if (null != navi) {
                                    run.setPageNavi(navi);
                                    //mergeFinalQuery(runtime, run);
                                }
                                DataSet set = selectMetadata(runtime, random, run);
                                list = comments(runtime, idx++, true, list, catalog, schema, set);
                                if (null != navi) {
                                    break;
                                }
                                //merge(list, maps);
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                            log.info("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                    log.info("{}[views][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
                }
                if (BasicUtil.isNotEmpty(origin)) {
                    origin = origin.replace("%", ".*");
                    //有表名的，根据表名过滤出符合条件的
                    List<T> tmp = new ArrayList<>();
                    for (T item : list) {
                        String name = item.getName(greedy) + "";
                        if (RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            if (equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
                                tmp.add(item);
                            }
                        }
                    }
                    list = tmp;
                }
                CacheProxy.views(caches_key, list);
            }
            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                //查询全部表结构 columns()内部已经给view.columns赋值
                for(View item:list) {
                    if(null == item.getColumns() || item.getColumns().isEmpty()) {
                        Column column_query = new Column();
                        column_query.setCatalog(catalog);
                        column_query.setSchema(schema);
                        columns(runtime, random, greedy, list, column_query);
                        break;
                    }
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                //查询全部表结构 indexes()内部已经给view.indexes赋值
                for(View item:list) {
                    if(null == item.getIndexes() || item.getIndexes().isEmpty()) {
                        Index index_qeury = new Index();
                        index_qeury.setCatalog(catalog);
                        index_qeury.setSchema(schema);
                        indexes(runtime, random, greedy, list, index_qeury);
                        break;
                    }
                }
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[views][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 * view[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	protected void viewMap(DataRuntime runtime, String random, boolean greedy, View query, ConfigStore configs) {
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<View> views = null;
		boolean sys = false; //根据系统视图查询
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryViewsRun(runtime, greedy, catalog, schema, null, View.TYPE.NORMAL.value, new DefaultConfigStore());
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = selectMetadata(runtime, random, run);
					views = views(runtime, idx++, true, views, catalog, schema, set);
					for(View view:views) {
						String cache_key = CacheProxy.key(runtime, "view_name_map", greedy, catalog, schema, view.getName());
						CacheProxy.name(cache_key, view.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				views = views(runtime, true, views, catalog, schema, null, View.TYPE.NORMAL.value);
				for(View view:views) {
					String cache_key = CacheProxy.key(runtime, "view_name_map", greedy, catalog, schema, view.getName());
					CacheProxy.name(cache_key, view.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * view[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> views = new LinkedHashMap<>();
		List<T> list = views(runtime, random, false, query, types, struct, configs);
		for(T view:list) {
            if(view instanceof View) {
                String name = view.getName();
                if(null != name) {
                    views.put(name.toUpperCase(), view);
                }
            }
		}
		return views;
	}

	/**
	 * view[命令合成]<br/>
	 * 查询视图,不是查视图中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, View query, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryViewsRun(DataRuntime runtime, View query, int types)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * View[结果集封装]<br/>
     * View 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initViewFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(View.class);
        refer.map(View.FIELD_NAME, "VIEW_NAME,TABLE_NAME,NAME,TABNAME");
        refer.map(View.FIELD_CATALOG, "VIEW_CATALOG,TABLE_CATALOG");
        refer.map(View.FIELD_SCHEMA, "VIEW_SCHEMA,TABLE_SCHEMA,TABSCHEMA,SCHEMA_NAME");
        return refer;
    }
	/**
	 * view[命令合成]<br/>
	 * 查询视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryViewsCommentRun(DataRuntime runtime, View query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryViewsCommentRun(DataRuntime runtime, View query, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[结果集封装]<br/>
	 *  根据查询结果集构造View
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, View query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * view[结果集封装]<br/>
	 *  根据查询结果集构造View
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> previous, View query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, View query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, View query, int types)", 37));
		}
		if(null == previous) {
            previous = new LinkedHashMap<>();
		}
		return previous;
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return views
	 * @throws Exception 异常
	 * @param <T> View
	 */
	@Override
	public <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> previous, View query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, View query, int types)", 37));
		}
		if(null == previous) {
            previous = new ArrayList<>();
		}
		return previous;
	}

	/**
	 * view[结果集封装]<br/>
	 * 根据查询结果封装View对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return View
	 * @param <T> View
	 */
	@Override
	public <T extends View> T init(DataRuntime runtime, int index, T meta, View query, DataRow row) {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == meta) {
            meta = (T)new View();
        }
        MetadataFieldRefer refer = refer(runtime, View.class);
        String _catalog = getString(row, refer, View.FIELD_CATALOG);
        String _schema =getString(row, refer, View.FIELD_SCHEMA);
        if(null == _catalog && null != catalog) {
            _catalog = catalog.getName();
        }
        if(null == _schema && null != schema) {
            _schema = schema.getName();
        }
        String name = getString(row, refer, View.FIELD_NAME);
        if(null == meta) {
            meta = (T)new View();
        }
        if(null != _catalog) {
            _catalog = _catalog.trim();
        }
        if(null != _schema) {
            _schema = _schema.trim();
        }
        meta.setMetadata(row);
        meta.setCatalog(_catalog);
        meta.setSchema(_schema);
        meta.setName(name);
        meta.setDefinition(getString(row, refer, View.FIELD_DEFINITION));
        return meta;
	}
	/**
	 * view[结果集封装]<br/>
	 * 根据查询结果封装View对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return View
	 * @param <T> View
	 */
	@Override
	public <T extends View> T detail(DataRuntime runtime, int index, T meta, View query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends View> T detail(DataRuntime runtime, int index, T meta, View query, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 *
	 * view[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param view 视图
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, View view, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, view);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
                    DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, view, list, set);
				}
				view.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = view.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, view, true);
					view.setColumns(columns);
					view.setTags(tags(runtime, random, false, view));
				}
				PrimaryKey pk = view.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, view);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				view.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = view.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					view.setIndexes(indexes(runtime, random, view, null));
				}
				runs = buildCreateRun(runtime, view);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					view.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[view ddl][view:{}][result:{}][执行耗时:{}]", random, view.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[view ddl][{}][view:{}][msg:{}]", random, LogUtil.format("查询视图的创建DDL失败", 33), view.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * view[命令合成]<br/>
	 * 查询视图DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, View view) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, View view)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[结果集封装]<br/>
	 * 查询视图DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param view 视图
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/* *****************************************************************************************************************
	 * 													master
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, MasterTable query, int types, boolean struct)
	 * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, String types, boolean struct)
	 * [命令合成]
	 * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, MasterTable query, int types, ConfigStore configs)
	 * List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, MasterTable query, int types)
	 * [结果集封装]<br/>
	 * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masters, Catalog catalog, Schema schema, DataSet set)
	 * <T extends MasterTable> List<T> masters(DataRuntime runtime, int index, boolean create, List<T> masters, Catalog catalog, Schema schema, DataSet set)
	 * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masters, MasterTable query, int types)
	 * <T extends MasterTable> List<T> masters(DataRuntime runtime, boolean create, List<T> masters, MasterTable query, int types)
	 * <T extends MasterTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masters, Catalog catalog, Schema schema, DataSet set)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, MasterTable master, boolean init)
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable master)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, MasterTable master, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * master[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, MasterTable query, int types, int struct, ConfigStore configs) {
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}

		Catalog catalog = query.getCatalog();
		Schema schema = query.getSchema();
		String pattern = query.getName();
		try{
			long fr = System.currentTimeMillis();
			MasterTable search = new MasterTable();
			if((supportCatalog() && empty(catalog))    //支持catalog 但catalog为空
				|| (supportSchema() && empty(schema))	//支持schema 但schema为空
			) {
				MasterTable tmp = new MasterTable();
				if(!greedy) { //非贪婪模式下 检测当前catalog schema
					checkSchema(runtime, tmp);
				}
				if(supportCatalog() && empty(catalog)) {
					catalog = tmp.getCatalog();
				}
				if(supportSchema() && empty(schema)) {
					schema = tmp.getSchema();
				}
			}
			String caches_key = CacheProxy.key(runtime, "masters", greedy, catalog, schema, pattern, types, configs);
			list = CacheProxy.masters(caches_key);
			if(null == list || list.isEmpty()) {
                String cache_key = CacheProxy.key(runtime, "master", greedy, catalog, schema, pattern);
                String origin = CacheProxy.name(cache_key);
                if (null == origin && ConfigTable.IS_METADATA_IGNORE_CASE) {
                    //先查出所有key并以大写缓存 用来实现忽略大小写
                    masterMap(runtime, random, greedy, query, configs);
                    origin = CacheProxy.name(cache_key);
                }
                if (null != origin) {
                    query.setName(origin);
                } else {
                    origin = query.getName();
                }
                search.setName(origin);
                search.setCatalog(catalog);
                search.setSchema(schema);
                PageNavi navi = null;
                if (null == configs) {
                    configs = new DefaultConfigStore();
                }
                navi = configs.getPageNavi();
                // 根据系统表查询
                try {
                    List<Run> runs = buildQueryMasterTablesRun(runtime, greedy, catalog, schema, origin, types, configs);
                    if (null != runs) {
                        int idx = 0;
                        for (Run run : runs) {
                            if (null != navi) {
                                run.setPageNavi(navi);
                                mergeFinalQuery(runtime, run);
                            }
                            DataSet set = selectMetadata(runtime, random, run);
                            list = masters(runtime, idx++, true, list, catalog, schema, set);
                            if (null != navi) {
                                //分页只查一次
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                        e.printStackTrace();
                    } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                        log.warn("{}[masters][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                    }
                }

                // 根据系统表查询失败后根据驱动内置接口补充
                if(null == list || list.isEmpty()) {
                    try {
                        list = masters(runtime, true, list, catalog, schema, origin, types);
                        //删除跨库表，JDBC驱动内置接口补充可能会返回跨库表
                        if (!greedy) {
                            int size = list.size();
                            for (int i = size - 1; i >= 0; i--) {
                                MasterTable item = list.get(i);
                                if (!equals(catalog, item.getCatalog()) || !equals(schema, item.getSchema())) {
                                    list.remove(i);
                                }
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else {
                            log.warn("{}[masters][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                boolean comment = false;
                for (MasterTable master : list) {
                    if (BasicUtil.isNotEmpty(master.getComment())) {
                        comment = true;
                        break;
                    }
                }
                //表备注
                if (!comment) {
                    try {
                        List<Run> runs = buildQueryMasterTablesCommentRun(runtime, catalog, schema, origin, types);
                        if (null != runs) {
                            int idx = 0;
                            for (Run run : runs) {
                                if (null != navi) {
                                    run.setPageNavi(navi);
                                    //mergeFinalQuery(runtime, run);
                                }
                                DataSet set = selectMetadata(runtime, random, run);
                                list = comments(runtime, idx++, true, list, catalog, schema, set);
                                if (null != navi) {
                                    break;
                                }
                                //merge(list, maps);
                            }
                        }
                    } catch (Exception e) {
                        if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                            e.printStackTrace();
                        } else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                            log.info("{}[masters][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
                        }
                    }
                }
                if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                    log.info("{}[masters][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}]", random, catalog, schema, origin, types, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
                }
                if (BasicUtil.isNotEmpty(origin)) {
                    origin = origin.replace("%", ".*");
                    //有表名的，根据表名过滤出符合条件的
                    List<T> tmp = new ArrayList<>();
                    for (T item : list) {
                        String name = item.getName(greedy) + "";
                        if (RegularUtil.match(name.toUpperCase(), origin.toUpperCase(), Regular.MATCH_MODE.MATCH)) {
                            if (equals(catalog, item.getCatalog()) && equals(schema, item.getSchema())) {
                                tmp.add(item);
                            }
                        }
                    }
                    list = tmp;
                }
                CacheProxy.masters(caches_key, list);
            }


            if(Metadata.check(struct, Metadata.TYPE.COLUMN)) {
                //查询全部表结构 columns()内部已经给MasterTable.columns赋值
                for(MasterTable item:list) {
                    if(null == item.getColumns() || item.getColumns().isEmpty()) {
                        Column column_query = new Column();
                        column_query.setCatalog(catalog);
                        column_query.setSchema(schema);
                        columns(runtime, random, greedy, list, column_query);
                        break;
                    }
                }
            }
            if(Metadata.check(struct, Metadata.TYPE.INDEX)) {
                //查询全部表结构 indexes()内部已经给MasterTable.indexes赋值
                for(MasterTable item:list) {
                    if(null == item.getIndexes() || item.getIndexes().isEmpty()) {
                        Index index_qeury = new Index();
                        index_qeury.setCatalog(catalog);
                        index_qeury.setSchema(schema);
                        indexes(runtime, random, greedy, list, index_qeury);
                        break;
                    }
                }
            }
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[masters][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * master[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	protected void masterMap(DataRuntime runtime, String random, boolean greedy, MasterTable query, ConfigStore configs) {
		Catalog catalog = query.getCatalog();
		Schema schema = query.getSchema();
		//Map<String, String> names = CacheProxy.names(this, catalog, schema);
		//if(null == names || names.isEmpty()) {
		if(null == random) {
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		List<MasterTable> masters = null;
		boolean sys = false; //根据系统表查询
		if(greedy) {
			catalog = null;
			schema = null;
		}
		try {
			//缓存 不需要configs条件及分页
			List<Run> runs =buildQueryMasterTablesRun(runtime, greedy, catalog, schema, null, MasterTable.TYPE.NORMAL.value, new DefaultConfigStore());
			if (null != runs && !runs.isEmpty()) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = selectMetadata(runtime, random, run);
					masters = masters(runtime, idx++, true, masters, catalog, schema, set);
					for(MasterTable master:masters) {
						String cache_key = CacheProxy.key(runtime, "master", greedy, catalog, schema, master.getName());
						CacheProxy.name(cache_key, master.getName());
					}
					sys = true;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		if(!sys) {
			try {
				masters = masters(runtime, true, masters, catalog, schema, null, MasterTable.TYPE.NORMAL.value);
				for(MasterTable master:masters) {
					String cache_key = CacheProxy.key(runtime, "master", greedy, catalog, schema, master.getName());
					CacheProxy.name(cache_key, master.getName());
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		//}

	}

	/**
	 * master[结果集封装-子流程]<br/>
	 * 查出所有key并以大写缓存 用来实现忽略大小写
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, int types, int struct, ConfigStore configs) {
		LinkedHashMap<String, T> masters = new LinkedHashMap<>();
		List<T> list = masters(runtime, random, false, query, types, struct, configs);
		for(T master:list) {
			masters.put(master.getName().toUpperCase(), master);
		}
		return masters;
	}

	/**
	 * master[命令合成]<br/>
	 * 查询表,不是查表中的数据
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, MasterTable query, int types, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryMasterTablesRun(DataRuntime runtime, MasterTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * master[结果集封装]<br/>
     * MasterTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initMasterTableFieldRefer() {
        return new MetadataFieldRefer(MasterTable.class);
    }
	/**
	 * master[命令合成]<br/>
	 * 查询表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 * @throws Exception Exception
	 */
	@Override
	public List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, MasterTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, MasterTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master[结果集封装]<br/>
	 *  根据查询结果集构造MasterTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return masters
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, MasterTable query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * master[结果集封装]<br/>
	 *  根据查询结果集构造MasterTable
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return masters
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> List<T> masters(DataRuntime runtime, int index, boolean create, List<T> previous, MasterTable query, DataSet set) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == previous) {
            previous = new ArrayList<>();
        }
        for(DataRow row:set) {
            T master = null;
            master = init(runtime, index, master, catalog, schema, row);
            if(null == search(previous, master.getCatalog(), master.getSchema(), master.getName())) {
                previous.add(master);
            }
            detail(runtime, index, master, catalog, schema, row);
        }
        return previous;
	}

	/**
	 * master[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return masters
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, MasterTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masters, MasterTable query, int types)", 37));
		}
		if(null == previous) {
            previous = new LinkedHashMap<>();
		}
		return previous;
	}

	/**
	 * master[结果集封装]<br/>
	 * 根据驱动内置方法补充
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return masters
	 * @throws Exception 异常
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> List<T> masters(DataRuntime runtime, boolean create, List<T> previous, MasterTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> List<T> masters(DataRuntime runtime, boolean create, List<T> masters, MasterTable query, int types)", 37));
		}
		if(null == previous) {
            previous = new ArrayList<>();
		}
		return previous;
	}

	/**
	 * master[结果集封装]<br/>
	 * 根据查询结果封装MasterTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return MasterTable
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> T init(DataRuntime runtime, int index, T meta, MasterTable query, DataRow row) {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == meta) {
            meta = (T)new MasterTable();
        }
        MetadataFieldRefer refer = refer(runtime, MasterTable.class);
        String _catalog = getString(row, refer, MasterTable.FIELD_CATALOG);
        String _schema = getString(row, refer, MasterTable.FIELD_SCHEMA);
        if(null == _catalog && null != catalog) {
            _catalog = catalog.getName();
        }
        if(null == _schema && null != schema) {
            _schema = schema.getName();
        }
        String name = getString(row, refer, MasterTable.FIELD_NAME);

        if(null == meta) {
            meta = (T)new MasterTable();
        }
        if(null != _catalog) {
            _catalog = _catalog.trim();
        }
        if(null != _schema) {
            _schema = _schema.trim();
        }
        meta.setMetadata(row);
        meta.setCatalog(_catalog);
        meta.setSchema(_schema);
        meta.setName(name);
        return meta;
	}
	/**
	 * master[结果集封装]<br/>
	 * 根据查询结果封装MasterTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return MasterTable
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta,  MasterTable query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta,  MasterTable query, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 *
	 * master[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta 表
	 * @param init 是否还原初始状态 如自增状态
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, MasterTable meta, boolean init) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, meta);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传master,这里的master用来查询表结构
					DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, meta, list, set);
				}
				meta.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = meta.getColumns();
				if(null == columns || columns.isEmpty()) {
					columns = columns(runtime, random, false, meta, true);
					meta.setColumns(columns);
					meta.setTags(tags(runtime, random, false, meta));
				}
				PrimaryKey pk = meta.getPrimaryKey();
				if(null == pk) {
					pk = primary(runtime, random, false, meta);
				}
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.primary(true);
						}
					}
				}
				meta.setPrimaryKey(pk);
				LinkedHashMap<String, Index> indexes = meta.getIndexes();
				if(null == indexes || indexes.isEmpty()) {
					meta.setIndexes(indexes(runtime, random, meta, null));
				}
				runs = buildCreateRun(runtime, meta);
				for(Run run:runs) {
					list.add(run.getFinalUpdate());
					meta.setDdls(list);
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[master ddl][master:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[master ddl][{}][master:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * master[命令合成]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param master 表
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable master) throws Exception {
		//有支持直接查询DDL的在子类中实现
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable master)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master[结果集封装]<br/>
	 * 查询表DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param master 表
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, MasterTable master, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, MasterTable master, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  PartitionTable query, int types)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master, Map<String, Tag> tags, String pattern)
	 * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master, Map<String, Tag> tags)
	 * [结果集封装]<br/>
	 * <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
	 * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, PartitionTable table)
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, PartitionTable table)
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
    /**
     * 表分区方式及分片
     * @param table 主表
     * @return Partition
     */
    @Override
    public Table.Partition partition(DataRuntime runtime, String random, Table table) {
        if(null == random) {
            random = random(runtime);
        }
        Table.Partition partition = null;
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                List<Run> runs = buildQueryTablePartitionRun(runtime, table);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        partition = partition(runtime, idx++, true, partition, table, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    log.error("[partition][result:fail]", e);
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[partition][{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), table.getName(), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[partition][table:{}][result:true][执行耗时:{}]", random, table.getName(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("[partition][result:fail]", e);
            }else{
                log.error("[partition][result:fail][msg:{}]", e.toString());
            }
        }
        return partition;
    }

    /**
     * partition table[命令合成]<br/>
     * 查询表分区方式及分片
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return String
     */
    @Override
    public List<Run> buildQueryTablePartitionRun(DataRuntime runtime, Table table) {
        return new ArrayList<>();
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param meta 上一步查询结果
     * @param table 表
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public Table.Partition partition(DataRuntime runtime, int index, boolean create, Table.Partition meta, Table table, DataSet set) throws Exception {
        for(DataRow row:set) {
            if(null == meta) {
               meta = init(runtime, index, create, meta, table, row);
            }
            if(null == meta) {
                continue;
            }
            meta = detail(runtime, index, create, meta, table, row);
        }
        return meta;
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果集构造Table.Partition
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param meta 上一步查询结果
     * @param table 表
     * @param row 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public Table.Partition init(DataRuntime runtime, int index, boolean create, Table.Partition meta, Table table, DataRow row) throws Exception {
        MetadataFieldRefer refer = refer(runtime, Table.Partition.class);
        try{
            String type = getString(row, refer, Table.Partition.FIELD_NAME);
            if(BasicUtil.isNotEmpty(type)) {
                meta.setType(Table.Partition.TYPE.valueOf(type));
                meta.setMetadata(row);
                String columns = getString(row, refer, Table.Partition.FIELD_COLUMN);
                if(null != columns) {
                    String[] cols = columns.split(",");
                    for(String col:cols) {
                        col = col.replace(getDelimiterFr(), "");
                        col = col.replace(getDelimiterTo(), "");
                        meta.addColumn(col);
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return meta;
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果集构造Table.Partition
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param meta 上一步查询结果
     * @param table 表
     * @param row 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public Table.Partition detail(DataRuntime runtime, int index, boolean create, Table.Partition meta, Table table, DataRow row) throws Exception {
        MetadataFieldRefer refer = refer(runtime, Table.Partition.Slice.class);
        Table.Partition.Slice slice = new Table.Partition.Slice();
        slice.setName(getString(row, refer, Table.Partition.Slice.FIELD_NAME));
        meta.addSlice(slice);
        return meta;
    }


    /**
     * partition table[结果集封装]<br/>
     * Table.Partition 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTablePartitionFieldRefer() {
        return new MetadataFieldRefer(Table.Partition.class);
    }

    /**
     * partition table[结果集封装]<br/>
     * Table.Partition.Slice 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTablePartitionSliceFieldRefer() {
        return new MetadataFieldRefer(Table.Partition.Slice.class);
    }

	/**
	 * partition table[调用入口]<br/>
	 * 查询主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 * @param <T> MasterTable
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, PartitionTable query) {
        if(!greedy) {
            checkSchema(runtime, query);
        }
        MasterTable master = (MasterTable) query.getMaster();
		Map<String, Tag> tags = query.getTags();
		String pattern = query.getName();
		LinkedHashMap<String,T> tables = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryPartitionTablesRun(runtime, greedy, query);
				if(null != runs) {
					int idx = 0;
					int total = runs.size();
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
						tables = partitions(runtime, total, idx++, true, master, tables, master.getCatalog(), master.getSchema(), set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][stable:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), master.getName(), e.toString());
				}
			}

			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[tables][stable:{}][result:{}][执行耗时:{}]", random, master.getName(), tables.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[partitions][result:fail][msg:{}]", e.toString());
			}
		}
		return tables;
	}

	/**
	 * partition table[命令合成]<br/>
	 * 查询分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
	 * @return String
	 */
	@Override
	public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  PartitionTable query, int types) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  PartitionTable query, int types)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * partition table[结果集封装]<br/>
     * PartitionTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initPartitionTableFieldRefer() {
        return new MetadataFieldRefer(PartitionTable.class);
    }

	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果集构造Table
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, LinkedHashMap<String, T> previous, PartitionTable query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 根据根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, PartitionTable query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 LinkedHashMap<String, PartitionTable> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, PartitionTable query)", 37));
		}
		if(null == previous) {
			previous = new LinkedHashMap<>();
		}
		return previous;
	}

	/**
	 * partition table[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, table, list, set);
				}
				table.setDdls(list);
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[partition table ddl][table:{}][result:{}][执行耗时:{}]", random, table.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[partition table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询子表创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * partition table[命令合成]<br/>
	 * 查询 PartitionTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table PartitionTable
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, PartitionTable table) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, PartitionTable table)", 37));
		}
		return runs;
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 查询 MasterTable DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param table MasterTable
	 * @param set sql执行的结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果封装PartitionTable对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return PartitionTable
	 */
	@Override
	public <T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, PartitionTable query, DataRow row) {
        if(null == meta) {
            meta = (T)new PartitionTable();
        }
        MetadataFieldRefer refer = refer(runtime, PartitionTable.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, PartitionTable.FIELD_NAME));
        meta.setCatalog(getString(row, refer, PartitionTable.FIELD_CATALOG));
        meta.setSchema(getString(row, refer, PartitionTable.FIELD_SCHEMA));
        meta.setType(getString(row, refer, PartitionTable.FIELD_TYPE));
        meta.setComment(getString(row, refer, PartitionTable.FIELD_COMMENT));
        meta.setMaster(getString(row, refer, PartitionTable.FIELD_MASTER));
        return meta;
	}
	/**
	 * partition table[结果集封装]<br/>
	 * 根据查询结果封装PartitionTable对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return PartitionTable
	 */
	@Override
	public <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, PartitionTable query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, PartitionTable query, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);
	 * <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String table);
	 * [命令合成]
	 * List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
	 * [结果集封装]<br/>
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception;
	 * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * column[调用入口]<br/>
	 * 查询表结构(多方法合成)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, Column query, boolean primary, ConfigStore configs) {
        if(null != table) {
            query.setTable(table);
        }
        if(null == configs) {
            configs = new DefaultConfigStore();
        }
        if(!greedy) {
            checkSchema(runtime, query);
        }
		Catalog catalog = query.getCatalog();
		Schema schema = query.getSchema();
		String key = CacheProxy.key(runtime, "table_columns", greedy, table);
		LinkedHashMap<String,T> columns = CacheProxy.columns(key);
		if(null != columns && !columns.isEmpty()) {
			return columns;
		}
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		try {

			int qty_total = 0;
			int qty_dialect = 0; //优先根据系统表查询
			int qty_metadata = 0; //再根据metadata解析
			int qty_jdbc = 0; //根据驱动内置接口补充

			// 1.优先根据系统表查询
			try {
				List<Run> runs = buildQueryColumnsRun(runtime,  false, query, configs);
				if (null != runs) {
					int idx = 0;
					for (Run run: runs) {
						DataSet set = selectMetadata(runtime, random, run);
						columns = columns(runtime, idx, true, columns, table, query, set);
						idx++;
					}
				}
				if(null != columns) {
					qty_dialect = columns.size();
					qty_total=columns.size();
				}
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
				if(primary) {
					e.printStackTrace();
				} if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			// 2.根据驱动内置接口补充
			// 再根据metadata解析 SELECT * FROM T WHERE 1=0
			if (null == columns || columns.isEmpty()) {
				try {
					List<Run> runs = buildQueryColumnsRun(runtime, true, query, configs);
					if (null != runs) {
						for (Run run  : runs) {
							String sql = run.getFinalQuery();
							if(BasicUtil.isNotEmpty(sql)) {
								columns = actuator.columns(this, runtime, true, columns, table, sql);
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						log.error("columns exception:", e);
					}else {
						if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
							log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.toString());
						}
					}
				}
				if(null != columns) {
					qty_metadata = columns.size() - qty_dialect;
					qty_total = columns.size();
				}
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据驱动内置接口补充:{}][执行耗时:{}]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, DateUtil.format(System.currentTimeMillis() - fr));
			}

			// 方法(3)根据根据驱动内置接口补充
			if (null == columns || columns.isEmpty()) {
				columns = actuator.metadata(this, runtime, true, columns, query);

				if(null != columns) {
					qty_total = columns.size();
					qty_jdbc = columns.size() - qty_metadata - qty_dialect;
				}
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据根据驱动内置接口补充:{}][执行耗时:{}]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, DateUtil.format(System.currentTimeMillis() - fr));
			}
			//检测主键
			if(ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY) {
				if (null != columns || !columns.isEmpty()) {
					boolean exists = false;
					for(Column column:columns.values()) {
						if(column.getPrimaryKey() != null) {
							exists = true;
							break;
						}
					}
					if(!exists) {
						PrimaryKey pk = primary(runtime, random, false, table);
						if(null != pk) {
							LinkedHashMap<String,Column> pks = pk.getColumns();
							if(null != pks) {
								for(String k:pks.keySet()) {
									Column column = columns.get(k);
									if(null != column) {
										column.primary(true);
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				log.error("columns exception:", e);
			}else{
				log.error("{}[columns][result:fail][table:{}][msg:{}]", random, table, e.toString());
			}
		}
		if(null != columns) {
			CacheProxy.cache(key, columns);
		}else{
			columns = new LinkedHashMap<>();
		}
		int index = 0;
		for(Column column:columns.values()) {
			if(null == column.getPosition() || -1 == column.getPosition()) {
				column.setPosition(index++);
			}
			if(!column.isAutoIncrement()) {
				column.autoIncrement(false);
			}
			if(!column.isPrimaryKey()) {
				column.setPrimary(false);
			}
			if(null == column.getTable() && !greedy) {
				column.setTable(table);
			}
		}
		return columns;
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
        if(!greedy) {
            checkSchema(runtime, query);
        }
		Catalog catalog = query.getCatalog();
		Schema schema = query.getSchema();
		List<T> columns = new ArrayList<>();
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		Table tab = null;
		if(null != tables && !tables.isEmpty()) {
			tab = tables.iterator().next();
		}

		if(null!= tab) {
			tab.setCatalog(catalog);
			tab.setSchema(schema);
			if (BasicUtil.isEmpty(catalog) && BasicUtil.isEmpty(schema) && !greedy) {
				checkSchema(runtime, tab);
			}
		}
		//根据系统表查询
		try {
			List<Run> runs = buildQueryColumnsRun(runtime, false, tables, query, configs);
			if (null != runs) {
				int idx = 0;
				for (Run run: runs) {
					DataSet set = selectMetadata(runtime, random, run);
					columns = columns(runtime, idx, true, columns, query, set);
					idx++;
				}
			}

			for(Table table:tables) {
				Long tObjectId = table.getObjectId();
				LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
				table.setColumns(cols);
				for(Column column:columns) {
					if(table.equals(column.getTable())) {
						Catalog cCatalog = column.getCatalog();
						Schema cSchema = column.getSchema();
						Long cObjectId = column.getObjectId();
						if(null != tObjectId && null != cObjectId && tObjectId == cObjectId) {
							cols.put(column.getName().toUpperCase(), column);
						}else{
							if(equals(cCatalog, column.getCatalog())
									&& equals(cSchema, column.getSchema())
									&& BasicUtil.equals(table.getName(), column.getTableName(), true)
							) {
								cols.put(column.getName().toUpperCase(), column);
							}
						}
					}
				}
				table.setColumns(cols);
				columns.removeAll(cols.values());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return columns;
	}
	

	/**
	 * column[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryColumnsRun(DataRuntime runtime,  boolean metadata, Column query, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成]<br/>(方法1)<br/>
	 * 查询多个表的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @param tables 表
	 * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryColumnsRun(DataRuntime runtime, boolean metadata, Collection<? extends Table> tables, Column query, ConfigStore configs) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryColumnsRun(DataRuntime runtime, Catalog catalog, Schema schema, Collection<? extends Table> tables, boolean metadata)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * Column[结果集封装]<br/>
     * Column 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initColumnFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Column.class);
        refer.map(Column.FIELD_NAME, "COLUMN_NAME,COLNAME");
        refer.map(Column.FIELD_CATALOG,"TABLE_CATALOG");
        refer.map(Column.FIELD_SCHEMA,"TABLE_SCHEMA,TABSCHEMA,SCHEMA_NAME,OWNER");
        refer.map(Column.FIELD_TABLE,"TABLE_NAME,TABNAME");
        refer.map(Column.FIELD_NULLABLE,"IS_NULLABLE,NULLABLE,NULLS");
        refer.map(Column.FIELD_CHARSET, "CHARACTER_SET_NAME");
        refer.map(Column.FIELD_COLLATE, "COLLATION_NAME");
        refer.map(Column.FIELD_TYPE, "FULL_TYPE,DATA_TYPE,TYPE_NAME,TYPENAME,DATA_TYPE_NAME,UDT_NAME,DATA_TYPE,TYPENAME,DATA_TYPE_NAME");
        refer.map(Column.FIELD_POSITION, "ORDINAL_POSITION,COLNO,POSITION");
        refer.map(Column.FIELD_COMMENT ,"COLUMN_COMMENT,COMMENTS,REMARKS");
        refer.map(Column.FIELD_DEFAULT_VALUE, "COLUMN_DEFAULT,DATA_DEFAULT,DEFAULT,DEFAULT_VALUE,DEFAULT_DEFINITION");
        return refer;
    }
	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 *  根据查询结果集构造Column
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T column = null;
            query.setTable(table);
            column = init(runtime, index, column, query, row);
            if(null != column) {
                column = detail(runtime, index, column, query, row);
                previous.put(column.getName().toUpperCase(), column);
            }
        }
        return previous;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 *  根据查询结果集构造Column
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception {
        Table table = query.getTable();
        if(null == previous) {
            previous = new ArrayList<>();
        }
        for(DataRow row:set) {
            T column = null;
            column = init(runtime, index, column, query, row);
            if(null == Metadata.match(column, previous)) {
                previous.add(column);
            }
            detail(runtime, index, column, query, row);
        }
        return previous;
	}

    /**
     * column[结果集封装]<br/>
     * 解析JDBC get columns结果
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @return previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @throws Exception 异常
     */
   public  <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Column query) throws Exception {
       return previous;
   }

    /**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 根据系统表查询SQL获取表结构
	 * 根据查询结果集构造Column,并分配到各自的表中
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 表
	 * @param previous 上一步查询结果
	 * @param set 系统表查询SQL结果集
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create,  List<T> previous, Collection<? extends Table> tables, Column query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        Map<String,Table> tbls = new HashMap<>();
        for(Table table:tables) {
            tbls.put(table.getName().toUpperCase(), table);
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == Metadata.match(meta, previous)) {
                previous.add(meta);
            }
            detail(runtime, index, meta, query,  row);
            String tableName = meta.getTableName();
            if(null != tableName) {
                Table table = tbls.get(tableName.toUpperCase());
                if(null != table) {
                    table.addColumn(meta);
                }
            }
        }
        return previous;
	}
	/**
	 * column[结果集封装]<br/>
	 * (方法1)
	 * <br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 系统表查询SQL结果集
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> T init(DataRuntime runtime, int index, T meta, Column query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Column
	 * @param <T> Column
	 */
	@Override
	public <T extends Column> T detail(DataRuntime runtime, int index, T meta, Column query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Column> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据数字有效位数列<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public String columnFieldLengthRefer(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return null;
		}
		String result = null;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Refer refer = MetadataReferHolder.get(type(), meta);
		if(null != refer) {
			result = refer.getLengthRefer();
		}

		//2.配置类-数据类型名称
		if(null == result) {
			//根据数据类型名称
            refer = MetadataReferHolder.get(type(), meta.getName());
			if(null != refer) {
				result = refer.getLengthRefer();
			}
		}
		//3.数据类型自带(length/precision/scale)

		//4.配置类-数据类型大类
		if(null == result) {
            refer = MetadataReferHolder.get(type(), meta.getCategory());
			if(null != refer) {
				result = refer.getLengthRefer();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(null == result) {
            //config = dataTypeMetadataRefer(runtime, meta);
            if(null != refer) {
                result = refer.getLengthRefer();
            }

		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据长度列<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public String columnFieldPrecisionRefer(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return null;
		}
		String result = null;

		//1.配置类 数据类型
		TypeMetadata.Refer refer = MetadataReferHolder.get(type(), meta);
		if(null != refer) {
			result = refer.getPrecisionRefer();
		}

		//2.配置类-数据类型名称
		if(null == result) {
			//根据数据类型名称
            refer = MetadataReferHolder.get(type(), meta.getName());
			if(null != refer) {
				result = refer.getPrecisionRefer();
			}
		}
		//3.数据类型自带(length/precision/scale)

		//4.配置类-数据类型大类
		if(null == result) {
            refer = MetadataReferHolder.get(type(), meta.getCategory());
			if(null != refer) {
				result = refer.getPrecisionRefer();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(null == result) {
            //config = dataTypeMetadataRefer(runtime, meta);
            if(null != refer) {
                result = refer.getPrecisionRefer();
            }
		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 元数据数字有效位数列<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public String columnFieldScaleRefer(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return null;
		}
		String result = null;
		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Refer refer = MetadataReferHolder.get(type(), meta);
		if(null != refer) {
			result = refer.getScaleRefer();
		}

		//2.配置类-数据类型名称
		if(null == result) {
			//根据数据类型名称
            refer = MetadataReferHolder.get(type(), meta.getName());
			if(null != refer) {
				result = refer.getScaleRefer();
			}
		}
		//3.数据类型自带(length/precision/scale)

		//4.配置类-数据类型大类
		if(null == result) {
            refer = MetadataReferHolder.get(type(), meta.getCategory());
			if(null != refer) {
				result = refer.getScaleRefer();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(null == result) {
           // config = dataTypeMetadataRefer(runtime, meta);
            if(null != refer) {
                result = refer.getScaleRefer();
            }
		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略长度<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public int columnMetadataIgnoreLength(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return -1;
		}
		int result = -1;

		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Refer refer = MetadataReferHolder.get(type(), meta);
		if(null != refer) {
			result = refer.ignoreLength();
		}

		//2.配置类-数据类型名称
		if(-1 == result) {
			//根据数据类型名称
            refer = MetadataReferHolder.get(type(), meta.getName());
			if(null != refer) {
				result = refer.ignoreLength();
			}
		}
		//3.数据类型自带(length/precision/scale)
		if(-1 == result) {
			result = meta.ignoreLength();
		}
		//4.配置类-数据类型大类
		if(-1 == result) {
            refer = MetadataReferHolder.get(type(), meta.getCategory());
			if(null != refer) {
				result = refer.ignoreLength();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(-1 == result) {
            //config = dataTypeMetadataRefer(runtime, meta);
            if(null != refer) {
                result = refer.ignoreLength();
            }

		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public int columnMetadataIgnorePrecision(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return -1;
		}
		int result = -1;

		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Refer refer = MetadataReferHolder.get(type(), meta);
		if(null != refer) {
			result = refer.ignorePrecision();
		}

		//2.配置类-数据类型名称
		if(-1 == result) {
			//根据数据类型名称
            refer = MetadataReferHolder.get(type(), meta.getName());
			if(null != refer) {
				result = refer.ignorePrecision();
			}
		}
		//3.数据类型自带(length/precision/scale)
		if(-1 == result) {
			result = meta.ignorePrecision();
		}
		//4.配置类-数据类型大类
		if(-1 == result) {
            refer = MetadataReferHolder.get(type(), meta.getCategory());
			if(null != refer) {
				result = refer.ignorePrecision();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(-1 == result) {
            //config = dataTypeMetadataRefer(runtime, meta);
            if(null != refer) {
                result = refer.ignorePrecision();
            }

		}
		return result;
	}

	/**
	 * column[结果集封装]<br/>(方法1)<br/>
	 * 是否忽略小数位<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta TypeMetadata
	 * @return String
	 */
	@Override
	public int columnMetadataIgnoreScale(DataRuntime runtime, TypeMetadata meta) {
		if(null == meta) {
			return -1;
		}
		int result = -1;

		/*
		1.配置类-数据类型
		2.配置类-数据类型名称
		3.数据类型自带(length/precision/scale)
		4.配置类-数据类型大类
		5.具体数据库实现的MetadataAdapter
		 */

		//1.配置类 数据类型
		TypeMetadata.Refer refer = MetadataReferHolder.get(type(), meta);
		if(null != refer) {
			result = refer.ignoreScale();
		}

		//2.配置类-数据类型名称
		if(-1 == result) {
			//根据数据类型名称
            refer = MetadataReferHolder.get(type(), meta.getName());
			if(null != refer) {
				result = refer.ignoreScale();
			}
		}
		//3.数据类型自带(length/precision/scale)
		if(-1 == result) {
			result = meta.ignoreScale();
		}
		//4.配置类-数据类型大类
		if(-1 == result) {
            refer = MetadataReferHolder.get(type(), meta.getCategory());
			if(null != refer) {
				result = refer.ignoreScale();
			}
		}
		//5.具体数据库实现的MetadataAdapter
		if(-1 == result) {
            //config = dataTypeMetadataRefer(runtime, meta);
            if(null != refer) {
                result = refer.ignoreScale();
            }

		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query)
	 * [命令合成]
	 * List<Run> buildQueryTagsRun(DataRuntime runtime, boolean greedy, Tag query)
	 * [结果集封装]<br/>
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Tag query, DataSet set)
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @return Tag
	 * @param <T>  Tag
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query)", 37));
		}
        return new LinkedHashMap<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 查询表上的列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryTagsRun(DataRuntime runtime, boolean greedy, Tag query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryTagsRun(DataRuntime runtime, boolean greedy, Tag query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * tag[结果集封装]<br/>
     * tag 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTagFieldRefer() {
        return new MetadataFieldRefer(Tag.class);
    }

	/**
	 * tag[结果集封装]<br/>
	 *  根据查询结果集构造Tag
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryTagsRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Tag query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 *
	 * tag[结果集封装]<br/>
	 * 解析JDBC get columns结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Tag query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)", 37));
		}
		if(null == tags) {
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

	/**
	 * tag[结果集封装]<br/>
	 * 列基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param table 表
	 * @param row 系统表查询SQL结果集
	 * @return Tag
	 * @param <T> Tag
	 */
	@Override
	public <T extends Tag> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) {
        if(null == meta) {
            meta = (T)new Tag();
        }
        MetadataFieldRefer refer = refer(runtime, Tag.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, Table.FIELD_NAME));
        return meta;
	}

	/**
	 * tag[结果集封装]<br/>
	 * 列详细属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 系统表查询SQL结果集
	 * @return Tag
	 * @param <T> Tag
	 */
	@Override
	public <T extends Tag> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Tag> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)", 37));
		}
		return meta;
	}
	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
	 * [命令合成]
	 * List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception
	 * [结构集封装]
	 * <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set)
	 * PrimaryKey primary(DataRuntime runtime, Table table)
	 ******************************************************************************************************************/
	/**
	 * primary[调用入口]<br/>
	 * 查询主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @return PrimaryKey
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, PrimaryKey query) {
		PrimaryKey primary = null;
		if(null == random) {
			random = random(runtime);
		}
		try{
            if(!greedy) {
                checkSchema(runtime, query);
            }
			List<Run> runs = buildQueryPrimaryRun(runtime, greedy, query);
			if(null != runs) {
				int idx = 0;
				for(Run run:runs) {
					DataSet set = selectMetadata(runtime, random, run);
					primary = init(runtime, idx, primary, query, set);
					primary = detail(runtime, idx, primary, query, set);
					idx ++;
				}
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.warn("{}[primary][{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), query.getTable(), e.toString());
			}
		}
		return primary;
	}

	/**
	 * primary[命令合成]<br/>
	 * 查询表上的主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  PrimaryKey query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  Table table)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * primary[结果集封装]<br/>
     * PrimaryKey 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initPrimaryKeyFieldRefer() {
        return new MetadataFieldRefer(PrimaryKey.class);
    }
	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, PrimaryKey query, DataSet set) throws Exception {
        Table table = query.getTable();
        MetadataFieldRefer refer = refer(runtime, PrimaryKey.class);
        for(DataRow row:set) {
            if(null == meta) {
                meta = (T)new PrimaryKey();
                meta.setName(getString(row, refer, PrimaryKey.FIELD_NAME));
                if(null == table) {
                    table = new Table(getString(row, refer, PrimaryKey.FIELD_CATALOG)
                            ,getString(row, refer, PrimaryKey.FIELD_SCHEMA)
                            ,getString(row, refer, PrimaryKey.FIELD_TABLE));
                }
                meta.setTable(table);
                meta.setMetadata(row);
            }
            String col = getString(row, refer, PrimaryKey.FIELD_COLUMN);
            if(BasicUtil.isEmpty(col)) {
                throw new Exception("主键相关列名异常,请检查buildQueryPrimaryRun与primaryMetadataColumn");
            }
            Column column = meta.getColumn(col);
            if(null == column) {
                column = new Column(col);
            }
            column.setTable(table);
            String position = getString(row, refer, PrimaryKey.FIELD_POSITION);
            meta.setPosition(column, BasicUtil.parseInt(position, 0));
            String order = getString(row, refer, PrimaryKey.FIELD_ORDER);
            if(BasicUtil.isNotEmpty(order)) {
                column.setOrder(order);
            }
            meta.addColumn(column);
        }
        return meta;
	}

	/**
	 * primary[结构集封装]<br/>
	 * 根据查询结果集构造PrimaryKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, PrimaryKey query, DataSet set) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, PrimaryKey query, DataSet set)", 37));
		}
		return meta;
	}

	/**
	 * primary[结构集封装]<br/>
	 *  根据驱动内置接口补充PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @throws Exception 异常
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, PrimaryKey query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 PrimaryKey primary(DataRuntime runtime, PrimaryKey query)", 37));
		}
		return null;
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
	 * [命令合成]
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception;
	 * [结构集封装]
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 查询外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @return PrimaryKey
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, ForeignKey query) {
		Table table = query.getTable();
		LinkedHashMap<String, T> foreigns = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
            if(!greedy) {
                checkSchema(runtime, query);
            }
			List<Run> runs = buildQueryForeignsRun(runtime, greedy, query);
			if(null != runs) {
				int idx = 0;
				for(Run run:runs) {
					DataSet set = selectMetadata(runtime, random, run);
					foreigns = foreigns(runtime, idx, table, foreigns, set);
					idx++;
				}
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
		}
		return foreigns;
	}

	/**
	 * foreign[命令合成]<br/>
	 * 查询表上的外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  ForeignKey query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  ForeignKey query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * foreign[结果集封装]<br/>
     * ForeignKey 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initForeignKeyFieldRefer() {
        return new MetadataFieldRefer(ForeignKey.class);
    }

    /**
	 * foreign[结构集封装]<br/>
	 *  根据查询结果集构造PrimaryKey
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, LinkedHashMap<String, T> previous, ForeignKey query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * foreign[结构集封装]<br/>
	 * 根据查询结果集构造ForeignKey基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, ForeignKey query, DataRow row) throws Exception {
        if(null == meta) {
            meta = (T)new ForeignKey();
        }
        MetadataFieldRefer refer = refer(runtime, ForeignKey.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, ForeignKey.FIELD_NAME));
        String name = getString(row, refer, ForeignKey.FIELD_NAME);

        meta.setName(name);
        meta.setTable(getString(row, refer, ForeignKey.FIELD_TABLE));
        meta.setReference(getString(row, refer, ForeignKey.FIELD_REFERENCE_TABLE));

        Table refTable = new Table(getString(row, refer, ForeignKey.FIELD_REFERENCE_CATALOG),
            getString(row, refer, ForeignKey.FIELD_REFERENCE_SCHEMA),
            getString(row, refer, ForeignKey.FIELD_REFERENCE_TABLE));
        Column reference = new Column(getString(row, refer, ForeignKey.FIELD_REFERENCE_COLUMN));
        reference.setTable(refTable);
        meta.addColumn(new Column(getString(row, refer, ForeignKey.FIELD_COLUMN))
            .setReference(reference)
            .setPosition(getInt(row, refer, ForeignKey.FIELD_POSITION, 0))
            );

        return meta;
	}

	/**
	 * foreign[结构集封装]<br/>
	 * 根据查询结果集构造ForeignKey更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, ForeignKey query, DataRow row) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, ForeignKey query, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern)
	 * [命令合成]
	 * List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Table table, String name)
	 * [结果集封装]<br/>
	 * <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set)
	 * <T extends Index> List< T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate)
	 * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate)
	 ******************************************************************************************************************/
	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param tables 表
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Collection<? extends Table> tables, Index query) {
		List<T> list = null;
		if(null == random) {
			random = random(runtime);
		}
		//根据系统表查询
		try {
			List<Run> runs = buildQueryIndexesRun(runtime, greedy, tables);
			if (null != runs) {
				int idx = 0;
				for (Run run: runs) {
					DataSet set = selectMetadata(runtime, random, run);
                    list = indexes(runtime, idx, true, tables, list, new Index(), set);
					idx++;
				}
			}
			if(null != list) {
				for (Table table : tables) {
					Long tObjectId = table.getObjectId();
					LinkedHashMap<String, Index> idxs = new LinkedHashMap<>();
					table.setIndexes(idxs);
					for (Index index : list) {
						if (table.equals(index.getTable())) {
							Catalog cCatalog = index.getCatalog();
							Schema cSchema = index.getSchema();
							Long cObjectId = index.getObjectId();
							if (null != tObjectId && null != cObjectId && tObjectId == cObjectId) {
								idxs.put(index.getName().toUpperCase(), index);
							} else {
								if (equals(cCatalog, index.getCatalog())
										&& equals(cSchema, index.getSchema())
										&& BasicUtil.equals(table.getName(), index.getTableName(), true)
								) {
									idxs.put(index.getName().toUpperCase(), index);
								}
							}
						}
					}
                    list.removeAll(idxs.values());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(null == list) {
            list = new ArrayList<>();
		}
		return list;
	}
	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Index query) {
		Table table = query.getTable();
		List<T> list = null;
		if(null == table) {
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}
        if(!greedy) {
            checkSchema(runtime, query);
        }
		List<Run> runs = buildQueryIndexesRun(runtime, greedy, query);
		if(null != runs) {
			int idx = 0;
			for(Run run:runs) {
				DataSet set = selectMetadata(runtime, random, run);
				try {
                    list = indexes(runtime, idx, true, table, list, set);
				}catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		if(null == list || list.isEmpty()) {
			if(null != table.getName()) {
				try {
					LinkedHashMap<String,T> maps = indexes(runtime, true, new LinkedHashMap<>(), table, false, false);
					table.setIndexes(maps);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
			}
		}
		if(null == list) {
            list = new ArrayList<>();
		}
		return list;
	}

	/**
	 *
	 * index[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Index query) {
		Table table = query.getTable();
		String pattern = query.getName();
		LinkedHashMap<String,T> map = null;
		if(null == table) {
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}

		checkSchema(runtime, query);

		List<Run> runs = buildQueryIndexesRun(runtime, false, query);

		if(null != runs) {
			int idx = 0;
			for(Run run:runs) {
				DataSet set = selectMetadata(runtime, random, run);
				try {
                    map = indexes(runtime, idx, true, table, map, set);
				}catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		if(null == map || map.isEmpty()) {
			if(null != table.getName()) {
				try {
                    map = indexes(runtime, true, map, table, false, false);
					table.setIndexes(map);
				} catch (Exception e) {
					log.info("{}[{}][table:{}][msg:{}]", random, LogUtil.format("JDBC方式获取索引失败", 33), table, e.toString());
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
                    map = new LinkedHashMap<>();
				}
				if(BasicUtil.isNotEmpty(pattern)) {
					T index = map.get(pattern.toUpperCase());
                    map = new LinkedHashMap<>();
                    map.put(pattern.toUpperCase(), index);
				}
			}
		}
		Index pk = null;
		if(null != map) {
			for (Index index : map.values()) {
				if (index.isPrimary()) {
					pk = index;
					break;
				}
			}
		}
		if(null == pk) {
			//识别主键索引
			pk = table.getPrimaryKey();
			if (null == pk) {
				pk = primary(runtime, random, false, table);
			}
			if (null != pk && !pk.isEmpty()) {
				Index index = map.get(pk.getName().toUpperCase());
				if (null != index) {
					index.setPrimary(true);
				} else {
                    map.put(pk.getName().toUpperCase(), (T) pk);
				}
			}
		}
		if(null == map) {
            map = new LinkedHashMap<>();
		}
		return map;
	}

	/**
	 * index[命令合成]<br/>
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy, Index query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Index query)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * index[命令合成]<br/>
	 * 查询表上的索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tables 表
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Collection<? extends Table> tables) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Collection<? extends Table> tables)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * Index[结果集封装]<br/>
     * Index 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initIndexFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Index.class);
        refer.map(Index.FIELD_NAME,"INDEX_NAME");
        refer.map(Index.FIELD_TABLE,"TABLE_NAME");
        refer.map(Index.FIELD_SCHEMA, "TABLE_SCHEMA");
        refer.map(Index.FIELD_COLUMN, "COLUMN_NAME");
        refer.map(Index.FIELD_ORDER, "COLLATION");
        refer.map(Index.FIELD_POSITION, "SEQ_IN_INDEX");
        return refer;
    }
	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Index query, DataSet set) throws Exception {
		Table table = query.getTable();
		if(null == previous) {
            previous = new LinkedHashMap<>();
		}
		MetadataFieldRefer refer = refer(runtime, Index.class);
		for(DataRow row:set) {
			String name = getString(row, refer, Index.FIELD_NAME);
			if(null == name) {
				continue;
			}
			T meta = previous.get(name.toUpperCase());
            if(null == meta) {
                meta = init(runtime, index, meta, query, row);
            }
            if(null == meta || meta.isEmpty()) {
                continue;
            }
			if(null != table) {
				if (!table.getName().equalsIgnoreCase(meta.getTableName())) {
					continue;
				}
			}
			meta = detail(runtime, index, meta, query, row);
			if(null != meta && !meta.isEmpty()) {
                previous.put(meta.getName().toUpperCase(), meta);
			}
		}
		return previous;
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, List<T> previous, Index query, DataSet set) throws Exception {
		Table table = query.getTable();
		if(null == previous) {
            previous = new ArrayList<>();
		}
        MetadataFieldRefer refer = refer(runtime, Index.class);
		for(DataRow row:set) {
			String name = getString(row, refer, Index.FIELD_NAME);
			if(null == name) {
				continue;
			}
			T meta = search(previous, name);
            if(null == meta) {
                meta = init(runtime, index, meta, table, row);
            }
            if(null == meta || meta.isEmpty()) {
                continue;
            }
			if(null != table) {
				if (!table.getName().equalsIgnoreCase(meta.getTableName())) {
					continue;
				}
			}
			meta = detail(runtime, index, meta, table, row);
			if(null != meta && !meta.isEmpty()) {
                previous.add(meta);
			}
		}
		return previous;
	}

	/**
	 * index[结果集封装]<br/>
	 *  根据查询结果集构造Index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> previous, Index query, DataSet set) throws Exception {
		if(null == previous) {
			previous = new ArrayList<>();
		}
        MetadataFieldRefer refer = refer(runtime, Index.class);
		Map<String,Table> tbls = new HashMap<>();
		for(Table table:tables) {
			tbls.put(table.getName().toUpperCase(), table);
		}
		for(DataRow row:set) {
            String name = getString(row, refer, Index.FIELD_NAME);
            if(null == name) {
                continue;
            }
			T meta = search(previous, name);
            if(null == meta) {
                meta = init(runtime, index, meta, query, row);
            }
            if(null == meta || meta.isEmpty()) {
                continue;
            }
			if(null == Metadata.match(meta, previous)) {
				previous.add(meta);
			}
			detail(runtime, index, meta, query, row);
			String tableName = meta.getTableName();
			if(null != tableName) {
				Table table = tbls.get(tableName.toUpperCase());
				if(null != table) {
					table.add(meta);
				}
			}
		}
		return previous;
	}
	/**
	 * index[结果集封装]<br/>
	 * 根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Index query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Index query)", 37));
		}
		if(null == indexes) {
			indexes = new ArrayList<>();
		}
		return indexes;
	}

	/**
	 * index[结果集封装]<br/>
	 * 根据驱动内置接口
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @return indexes indexes
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Index query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Index query)", 37));
		}
		if(null == indexes) {
			indexes = new LinkedHashMap<>();
		}
		return indexes;
	}

	/**
	 * index[结构集封装]<br/>
	 * 根据查询结果集构造index基础属性(name,table,schema,catalog)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> T init(DataRuntime runtime, int index, T meta, Index query, DataRow row) throws Exception {
		Table table = query.getTable();
		MetadataFieldRefer refer = refer(runtime, Index.class);
		String name = getString(row, refer, Index.FIELD_NAME);
		if(null == meta) {
			meta = (T)new Index();
			meta.setName(name);
			Catalog catalog = null;
			Schema schema = null;
			String catalogName = getString(row, refer, Index.FIELD_CATALOG);
			if(BasicUtil.isNotEmpty(catalogName)) {
				catalog = new Catalog(catalogName);
			}else{
				if(null != table) {
					catalog = table.getCatalog();
				}
			}
			String schemaName = getString(row, refer, Index.FIELD_SCHEMA);
			if(BasicUtil.isNotEmpty(schemaName)) {
				schema = new Schema(schemaName);
			}else{
				if(null != table) {
					schema = table.getSchema();
				}
			}

			if(null == table) {
				String tableName = getString(row, refer, Index.FIELD_TABLE);
				table = new Table(catalog, schema, tableName);
			}
			meta.setCatalog(catalog);
			meta.setSchema(schema);
			meta.setTable(table);
			meta.setMetadata(row);

			//是否主键
			String[] chks = refer.maps(Index.FIELD_PRIMARY_CHECK);
			String[] vals = refer.maps(Index.FIELD_PRIMARY_CHECK_VALUE);
			Boolean bol = matchBoolean(row, chks, vals);
			if(null != bol) {
				meta.setPrimary(bol);
			}
			//是否唯一
			chks = refer.maps(Index.FIELD_UNIQUE_CHECK);
			vals = refer.maps(Index.FIELD_UNIQUE_CHECK_VALUE);
			bol = matchBoolean(row, chks, vals);
			if(null != bol) {
				meta.setUnique(bol);
			}

            //注释
            String comment = getString(row, refer, Index.FIELD_COMMENT);
            meta.setComment(comment);
            //类型
            String type = getString(row, refer, Index.FIELD_TYPE);
            meta.setType(type);
		}
		return meta;
	}

	/**
	 * index[结构集封装]<br/>
	 * 根据查询结果集构造index更多属性(column,order, position)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> T detail(DataRuntime runtime, int index, T meta, Index query, DataRow row) throws Exception {
        MetadataFieldRefer refer = refer(runtime, Index.class);
		String columnName = row.getStringWithoutEmpty(refer.maps(Index.FIELD_COLUMN));
		if(null == columnName) {
			return meta;
		}
		columnName = columnName.replace("\"", "");
		Column column = meta.getColumn(columnName.toUpperCase());
		if(null == column) {
			column = new Column();
		}
		column.setName(columnName);
		meta.addColumn(column);
		Integer position = getInt(row, refer, Index.FIELD_POSITION);
		if(null == position) {
			position = 0;
		}
		column.setPosition(position);
		meta.setPosition(column, position);
		String order = getString(row, refer, Index.FIELD_ORDER);
		if(null != order) {
			order = order.toUpperCase();
			Order.TYPE type = Order.TYPE.ASC;
			if(order.contains("DESC")) {
				type = Order.TYPE.DESC;
			}
			meta.setOrder(column, type);
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern);
	 * [命令合成]
	 * List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Table table, Column column, String pattern) ;
	 * [结果集封装]<br/>
	 * <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 *
	 * constraint[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Constraint query) {
        if(null == random) {
            random = random(runtime);
        }
        List<T> list = new ArrayList<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
                List<Run> runs = buildQueryConstraintsRun(runtime, greedy, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        list = constraints(runtime, idx++, true, list, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[constraints][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[constraints][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[constraints][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 *
	 * constraint[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Constraint query) {
        if(null == random) {
            random = random(runtime);
        }
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                List<Run> runs = buildQueryConstraintsRun(runtime, false, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        map = constraints(runtime, idx++, true, map, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[constraints][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[constraints][result:{}][执行耗时:{}]", random, map.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[constraints][result:fail][msg:{}]", e.toString());
            }
        }
        return map;
	}

	/**
	 * constraint[命令合成]<br/>
	 * 查询表上的约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Constraint query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Constraint query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * constraint[结果集封装]<br/>
     * Constraint 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initConstraintFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Constraint.class);
        refer.map(Constraint.FIELD_NAME, "CONSTRAINT_NAME");
        refer.map(Constraint.FIELD_SCHEMA, "CONSTRAINT_CATALOG");
        refer.map(Constraint.FIELD_TABLE, "TABLE_NAME");
        refer.map(Constraint.FIELD_TYPE, "CONSTRAINT_TYPE");
        return refer;
    }

    /**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, List<T> previous, Constraint query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new ArrayList<>();
        }
        MetadataFieldRefer refer = refer(runtime, Constraint.class);
        Table table = query.getTable();
        for(DataRow row:set) {
            String name = getString(row, refer, Constraint.FIELD_NAME);
            if(null == name) {
                continue;
            }
            T meta = search(previous, name);
            if(null == meta) {
                meta = init(runtime, index, meta, query, row);
            }
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            if(null != table) {
                if (!table.getName().equalsIgnoreCase(meta.getTableName())) {
                    continue;
                }
            }
            if(null == Metadata.match(meta, previous)) {
                previous.add(meta);
            }
            detail(runtime, index, meta, query, row);
        }
        return previous;
	}

	/**
	 * constraint[结果集封装]<br/>
	 * 根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create,  LinkedHashMap<String, T> previous,  Constraint query, DataSet set) throws Exception {
        Table table = query.getTable();
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        MetadataFieldRefer refer = refer(runtime, Constraint.class);
        for(DataRow row:set) {
            String name = getString(row, refer, Constraint.FIELD_NAME);
            if(null == name) {
                continue;
            }
            T meta = previous.get(name.toUpperCase());
            if(null == meta) {
                meta = init(runtime, index, meta, query, row);
            }
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            if(null != table) {
                if (!table.getName().equalsIgnoreCase(meta.getTableName())) {
                    continue;
                }
            }
            meta = detail(runtime, index, meta, query, row);
            if(null != meta && !meta.isEmpty()) {
                previous.put(meta.getName().toUpperCase(), meta);
            }
        }
        return previous;
	}

	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Constraint
	 */
	@Override
	public <T extends Constraint> T init(DataRuntime runtime, int index, T meta, Constraint query, DataRow row) {
        if(null == meta) {
            meta = (T)new Constraint<>();
        }
        MetadataFieldRefer refer = refer(runtime, Constraint.class);
        String name = getString(row, refer, Constraint.FIELD_NAME);
        if(null == name) {
            return meta;
        }
        String catalog =  getString(row, refer, Constraint.FIELD_CATALOG);
        String schema =  getString(row, refer, Constraint.FIELD_SCHEMA);
        meta.setCatalog(catalog);
        meta.setSchema(schema);
        Table table = query.getTable();
        if(null == table) {
            table = new Table(catalog, schema, getString(row, refer, Constraint.FIELD_TABLE));
        }
        meta.setTable(table);
        meta.setName(name);
        meta.setType(getString(row, refer, Constraint.FIELD_TYPE));

		return meta;
	}
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Constraint
	 */
	@Override
	public <T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Constraint query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Constraint query, DataRow row)", 37));
		}
		return meta;
	}
	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)
	 * [命令合成]
	 * List<Run> buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Table table, List<Trigger.EVENT> events)
	 * [结果集封装]<br/>
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * trigger[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Trigger query) {

        if(null == random) {
            random = random(runtime);
        }
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
                List<Run> runs = buildQueryTriggersRun(runtime, greedy, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        map = triggers(runtime, idx++, true, map, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[triggers][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[triggers][result:{}][执行耗时:{}]", random, map.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[triggers][result:fail][msg:{}]", e.toString());
            }
        }
        return map;
	}

	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Trigger query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Trigger query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * trigger[结果集封装]<br/>
     * trigger 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initTriggerFieldRefer() {
        return new MetadataFieldRefer(Trigger.class);
    }

	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Trigger query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Trigger
	 */
	@Override
	public <T extends Trigger> T init(DataRuntime runtime, int index, T meta, Trigger query, DataRow row) {
        if(null == meta) {
            meta = (T)new Trigger();
        }
        MetadataFieldRefer refer = refer(runtime, Trigger.class);
        meta.setName(getString(row, refer, Trigger.FIELD_NAME));
        Table table = null;
        String tableName = getString(row, refer, Trigger.FIELD_TABLE);
        if(BasicUtil.isNotEmpty(tableName)) {
            table = new Table(
                getString(row, refer, Trigger.FIELD_CATALOG),
                getString(row, refer, Trigger.FIELD_SCHEMA),
                tableName);
        }else{
            table = query.getTable();
        }
        meta.setTable(table);
        meta.setMetadata(row);
        meta.setDefinition(getString(row, refer, Trigger.FIELD_DEFINITION));
        return meta;
	}
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Trigger
	 */
	@Override
	public <T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Trigger query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Trigger query, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Procedure query);
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Procedure query);
	 * [命令合成]
	 * List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Procedure query) ;
	 * [结果集封装]<br/>
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
	 * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures)
	 * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception;
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set);
	 ******************************************************************************************************************/
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Procedure query) {
        if(null == random) {
            random = random(runtime);
        }
        List<T> list = new ArrayList<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
                List<Run> runs = buildQueryProceduresRun(runtime, greedy, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        list = procedures(runtime, idx++, true, list, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[procedures][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[procedures][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[procedures][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Procedure query) {
        if(null == random) {
            random = random(runtime);
        }
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                List<Run> runs = buildQueryProceduresRun(runtime, false, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        map = procedures(runtime, idx++, true, map, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[procedures][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[procedures][result:{}][执行耗时:{}]", random, map.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[procedures][result:fail][msg:{}]", e.toString());
            }
        }
        return map;
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	public List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Procedure query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Procedure query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * procedure[结果集封装]<br/>
     * Procedure 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initProcedureFieldRefer() {
        return new MetadataFieldRefer(Procedure.class);
    }

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> previous, Procedure query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Procedure query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> previous, Procedure query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set)", 37));
		}
        return previous;
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Procedure query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set)", 37));
		}
        return previous;
	}

	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, procedure);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, procedure, list, set);
				}
				if(null != list && !list.isEmpty()) {
					procedure.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装

			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[procedure ddl][procedure:{}][result:{}][执行耗时:{}]", random, procedure.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[procedure ddl][{}][procedure:{}][msg:{}]", random, LogUtil.format("查询存储过程的创建DDL失败", 33), procedure.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * procedure[命令合成]<br/>
	 * 查询存储DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, Procedure procedure) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, Procedure procedure)", 37));
		}
		return runs;
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param procedure Procedure
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Procedure
	 */
	@Override
	public <T extends Procedure> T init(DataRuntime runtime, int index, T meta, Procedure query, DataRow row) {
        if(null == meta) {
            meta = (T)new Procedure();
        }
        MetadataFieldRefer refer = refer(runtime, Procedure.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, Procedure.FIELD_NAME));
        meta.setCatalog(getString(row, refer, Procedure.FIELD_CATALOG));
        meta.setSchema(getString(row, refer, Procedure.FIELD_SCHEMA));
        meta.setDefinition(getString(row, refer, Procedure.FIELD_DEFINITION));
        return meta;
	}
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Procedure
	 */
	@Override
	public <T extends Procedure> T detail(DataRuntime runtime, int index, T meta, Procedure query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 detail(DataRuntime runtime, int index, T meta, Procedure query, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Function query);
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Function query);
	 * [命令合成]
	 * List<Run> buildQueryFunctionsRun(DataRuntime runtime, boolean greedy, Function query) ;
	 * [结果集封装]<br/>
	 * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
	 * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions)
	 * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Function function);
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, Function function) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Function query) {
        if(null == random) {
            random = random(runtime);
        }
        List<T> list = new ArrayList<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
                List<Run> runs = buildQueryFunctionsRun(runtime, greedy, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        list = functions(runtime, idx++, true, list, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[functions][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[functions][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[functions][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Function query) {
        if(null == random) {
            random = random(runtime);
        }
        LinkedHashMap<String, T> map = new LinkedHashMap<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                List<Run> runs = buildQueryFunctionsRun(runtime, false, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        map = functions(runtime, idx++, true, map, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[functions][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[functions][result:{}][执行耗时:{}]", random, map.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[functions][result:fail][msg:{}]", e.toString());
            }
        }
        return map;
	}

	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQueryFunctionsRun(DataRuntime runtime, boolean greedy, Function query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryFunctionsRun(DataRuntime runtime, boolean greedy, Function query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * Function[结果集封装]<br/>
     * Function 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initFunctionFieldRefer() {
        return new MetadataFieldRefer(Function.class);
    }

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> previous, Function query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果集构造 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Function query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> previous, Function query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema previous)", 37));
		}
        return previous;
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Function query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)", 37));
		}
        return previous;
	}

	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Function
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Function meta) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, meta);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, meta, list, set);
				}
				if(null != list && !list.isEmpty()) {
					meta.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[function ddl][function:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[function ddl][{}][function:{}][msg:{}]", random, LogUtil.format("查询函数的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * function[命令合成]<br/>
	 * 查询函数DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, Function meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, Function meta)", 37));
		}
		return runs;
	}

	/**
	 * function[结果集封装]<br/>
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param function Function
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Function
	 */
	@Override
	public <T extends Function> T init(DataRuntime runtime, int index, T meta, Function query, DataRow row) {
        MetadataFieldRefer refer = refer(runtime, Function.class);
		if(null == meta) {
			meta = (T)new Function();
		}
		if(null != refer) {
			meta.setName(getString(row, refer, Function.FIELD_NAME));
            meta.setCatalog(getString(row, refer, Function.FIELD_CATALOG));
            meta.setSchema(getString(row, refer, Function.FIELD_SCHEMA));
			meta.setComment(getString(row, refer, Function.FIELD_COMMENT));
			meta.setDefinition(getString(row, refer, Function.FIELD_DEFINITION));
		}
		return meta;
	}

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Function
	 */
	@Override
	public <T extends Function> T detail(DataRuntime runtime, int index, T meta, Function query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Function> T detail(DataRuntime runtime, int index, T meta, Function query, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Sequence query);
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Sequence query);
	 * [命令合成]
	 * List<Run> buildQuerySequencesRun(DataRuntime runtime, boolean greedy, Sequence query) ;
	 * [结果集封装]<br/>
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences)
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences)
	 * [调用入口]
	 * List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
	 * [命令合成]
	 * List<Run> buildQueryDdlRun(DataRuntime runtime, Sequence sequence) throws Exception;
	 * [结果集封装]<br/>
	 * List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set)
	 ******************************************************************************************************************/
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Sequence query) {
		if(null == random) {
			random = random(runtime);
		}
		List<T> list = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
				List<Run> runs = buildQuerySequencesRun(runtime, greedy, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
                        list = sequences(runtime, idx++, true, list, query, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[sequences][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[sequences][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[sequences][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param query 查询条件 根据metadata属性
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Sequence query) {
		if(null == random) {
			random = random(runtime);
		}
		LinkedHashMap<String, T> maps = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQuerySequencesRun(runtime, false, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
                        maps = sequences(runtime, idx++, true, maps, new Sequence(), set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[sequences][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[sequences][result:{}][执行耗时:{}]", random, maps.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[sequences][result:fail][msg:{}]", e.toString());
			}
		}
		return maps;
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	@Override
	public List<Run> buildQuerySequencesRun(DataRuntime runtime, boolean greedy, Sequence query) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQuerySequencesRun(DataRuntime runtime, boolean greedy, Sequence query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * sequence[结果集封装]<br/>
     * Sequence 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initSequenceFieldRefer() {
        return new MetadataFieldRefer(Sequence.class);
    }
	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> previous, Sequence query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果集构造 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Sequence query, DataSet set) throws Exception {
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.put(meta.getName().toUpperCase(), meta);
        }
        return previous;
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> previous, Sequence query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> previous, Sequence query)", 37));
		}
        return previous;
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Sequence query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Sequence query)", 37));
		}
        return previous;
	}

	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Sequence
	 * @return ddl
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, String random, Sequence meta) {
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDdlRun(runtime, meta);
			if (null != runs && !runs.isEmpty()) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = selectMetadata(runtime, random, run);
					list = ddl(runtime, idx++, meta, list, set);
				}
				if(null != list && !list.isEmpty()) {
					meta.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装
			}
			if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
				log.info("{}[sequence ddl][sequence:{}][result:{}][执行耗时:{}]", random, meta.getName(), list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
				log.info("{}[sequence ddl][{}][sequence:{}][msg:{}]", random, LogUtil.format("查询序列的创建DDL失败", 33), meta.getName(), e.toString());
			}
		}
		return list;
	}

	/**
	 * sequence[命令合成]<br/>
	 * 查询序列DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	@Override
	public List<Run> buildQueryDdlRun(DataRuntime runtime, Sequence meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryDdlRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return runs;
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 查询 Sequence DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param sequence Sequence
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set) {
		if(null == ddls) {
			ddls = new ArrayList<>();
		}
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set)", 37));
		}
		return ddls;
	}

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Sequence
	 */
	@Override
	public <T extends Sequence> T init(DataRuntime runtime, int index, T meta, Sequence query, DataRow row) {
        if(null == meta) {
            meta = (T)new Sequence();
        }
        MetadataFieldRefer refer = refer(runtime, Database.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, Sequence.FIELD_NAME));
        meta.setCatalog(getString(row, refer, Sequence.FIELD_CATALOG));
        meta.setSchema(getString(row, refer, Sequence.FIELD_SCHEMA));
        meta.setLast(getLong(row, refer, Sequence.FIELD_LAST, null));
        meta.setMin(getLong(row, refer, Sequence.FIELD_MIN, null));
        meta.setMax(getLong(row, refer, Sequence.FIELD_MAX, null));
        meta.setIncrement(getInt(row, refer, Sequence.FIELD_INCREMENT, 1));
        meta.setCache(getInt(row, refer, Sequence.FIELD_CACHE, null));
        meta.setCycle(getBoolean(row, refer, Sequence.FIELD_CYCLE_CHECK, null));
        return meta;
	}
	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Sequence
	 */
	@Override
	public <T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Sequence query, DataRow row) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 <T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Sequence query, DataRow row)", 37));
		}
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													common
	 * ----------------------------------------------------------------------------------------------------------------
	 */
	/**
	 *
	 * 根据 catalog, name检测schemas集合中是否存在
	 * @param schemas schemas
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param name name
	 * @return 如果存在则返回 Schema 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name) {
		if(null != schemas) {
			for(T schema:schemas) {
				if(BasicUtil.equalsIgnoreCase(catalog, schema.getCatalog())
						&& schema.getName().equalsIgnoreCase(name)
				) {
					return schema;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * 根据 name检测catalogs集合中是否存在
	 * @param catalogs catalogs
	 * @param name name
	 * @return 如果存在则返回 Catalog 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Catalog> T catalog(List<T> catalogs, String name) {
		if(null != catalogs) {
			for(T catalog:catalogs) {
				if(catalog.getName().equalsIgnoreCase(name)) {
					return catalog;
				}
			}
		}
		return null;
	}

	/**
	 *
	 * 根据 name检测databases集合中是否存在
	 * @param databases databases
	 * @param name name
	 * @return 如果存在则返回 Database 不存在则返回null
	 * @param <T> Table
	 */
	public <T extends Database> T database(List<T> databases, String name) {
		if(null != databases) {
			for(T database:databases) {
				if(database.getName().equalsIgnoreCase(name)) {
					return database;
				}
			}
		}
		return null;
	}
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

	/**
	 * ddl [执行命令]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return boolean
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run)", 37));
		}
		return false;
	}
	/**
	 * 执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param runs 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return boolean
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, List<Run> runs) {
		boolean result = true;
		int idx = 0;
		long frs = System.currentTimeMillis();
		ACTION.SWITCH swt = meta.swt();
		if(swt == ACTION.SWITCH.CONTINUE) {//上一步执行状态保存在meta中
			swt = InterceptorProxy.before(runtime, random, action, meta, runs);
			if (swt == ACTION.SWITCH.CONTINUE) {
				for (Run run : runs) {
					swt = InterceptorProxy.before(runtime, random, action, meta, run, runs);
					long fr = System.currentTimeMillis();
					if (swt == ACTION.SWITCH.CONTINUE) {
						result = execute(runtime, random + "[index:" + idx++ + "]", meta, action, run) && result;
					} else if (swt == ACTION.SWITCH.SKIP) {//跳过after
						continue;
					} else if (swt == ACTION.SWITCH.BREAK) {//中断整组命令
						break;
					}
					swt = InterceptorProxy.after(runtime, random, action, meta, run, runs, result, System.currentTimeMillis() - fr);
					if (swt == ACTION.SWITCH.BREAK) {
						break;
					}
				}
				long millis = System.currentTimeMillis() - frs;
				if(runs.size()>1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
					//如果有多个命令执行，再统计一次 合计时间
					log.info("{}[action:{}][meta:{}][cmds:{}][result:{}][执行耗时:{}]", random, action, meta.getName(), runs.size(), result, DateUtil.format(millis));
				}
				swt = InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

    /**
     * 解析DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param type Metadata类型
     * @param ddl ddl
     * @param configs 其他配置
     * @return T
     * @param <T> T
     */
    @Override
    public <T extends Metadata> T parse(DataRuntime runtime, Class<T> type, String ddl, ConfigStore configs) {
        return null;
    }
    /* *****************************************************************************************************************
     * 													catalog
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(Catalog catalog) throws Exception
     * boolean alter(Catalog catalog) throws Exception
     * boolean drop(Catalog catalog) throws Exception
     * boolean rename(Catalog origin, String name) throws Exception
     ******************************************************************************************************************/

    /**
     * catalog[调用入口]<br/>
     * 创建Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean create(DataRuntime runtime, Catalog meta) throws Exception {
        ACTION.DDL action = ACTION.DDL.CATALOG_CREATE;
        String random = random(runtime);
        ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        List<Run> runs = buildCreateRun(runtime, meta);
        return execute(runtime, random, meta, action, runs);
    }

    /**
     * catalog[调用入口]<br/>
     * 修改Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean alter(DataRuntime runtime, Catalog meta) throws Exception {
        return false;
    }
    /**
     * catalog[调用入口]<br/>
     * 删除Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean drop(DataRuntime runtime, Catalog meta) throws Exception {
        ACTION.DDL action = ACTION.DDL.CATALOG_DROP;
        String random = random(runtime);
        ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        checkSchema(runtime, meta);
        List<Run> runs = buildDropRun(runtime, meta);
        return execute(runtime, random, meta, action, runs);
    }

    /**
     * catalog[调用入口]<br/>
     * 重命名Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param origin 原Catalog
     * @param name 新名称
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean rename(DataRuntime runtime, Catalog origin, String name) throws Exception {
        return false;
    }

    /**
     * catalog[命令合成]<br/>
     * 创建Catalog<br/>
     * 其中1.x三选一 不要重复
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildCreateRun(DataRuntime runtime, Catalog meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * catalog[命令合成]<br/>
     * 修改Catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAlterRun(DataRuntime runtime, Catalog meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * catalog[命令合成]<br/>
     * 重命名
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildRenameRun(DataRuntime runtime, Catalog meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * catalog[命令合成]<br/>
     * 删除Catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Catalog meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 创建Catalog完成后追加Catalog备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Catalog meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendCommentRun(DataRuntime runtime, Catalog meta) throws Exception {
        return new ArrayList<>();
    }
    /**
     * catalog[命令合成-子流程]<br/>
     * 创建Catalog完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Catalog meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 修改备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Catalog catalog) throws Exception {
        return new ArrayList<>();
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 添加备注(部分数据库需要区分添加还是修改)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog Catalog
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public  List<Run> buildAddCommentRun(DataRuntime runtime, Catalog catalog) throws Exception {
        return buildChangeCommentRun(runtime, catalog);
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 创建或删除Catalog之前  检测Catalog是否存在
     * IF NOT EXISTS
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param exists exists
     * @return StringBuilder
     */
    @Override
    public StringBuilder checkCatalogExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
        return builder;
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 创建Catalog engine
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    @Override
    public StringBuilder engine(DataRuntime runtime, StringBuilder builder, Catalog meta) {
        return builder;
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 编码
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    @Override
    public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Catalog meta) {
        return builder;
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * Catalog备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    @Override
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Catalog meta) {
        return builder;
    }

    /**
     * catalog[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    @Override
    public StringBuilder property(DataRuntime runtime, StringBuilder builder, Catalog meta) {
        return builder;
    }


    /* *****************************************************************************************************************
     * 													schema
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(Schema schema) throws Exception
     * boolean alter(Schema schema) throws Exception
     * boolean drop(Schema schema) throws Exception
     * boolean rename(Schema origin, String name) throws Exception
     ******************************************************************************************************************/

    /**
     * schema[调用入口]<br/>
     * 创建Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean create(DataRuntime runtime, Schema meta) throws Exception {
        ACTION.DDL action = ACTION.DDL.SCHEMA_CREATE;
        String random = random(runtime);
        ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        List<Run> runs = buildCreateRun(runtime, meta);
        return execute(runtime, random, meta, action, runs);
    }

    /**
     * schema[调用入口]<br/>
     * 修改Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean alter(DataRuntime runtime, Schema meta) throws Exception {
        ACTION.DDL action = ACTION.DDL.SCHEMA_DROP;
        String random = random(runtime);
        ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        checkSchema(runtime, meta);
        List<Run> runs = buildDropRun(runtime, meta);
        return execute(runtime, random, meta, action, runs);
    }
    /**
     * schema[调用入口]<br/>
     * 删除Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean drop(DataRuntime runtime, Schema meta) throws Exception {
        return false;
    }

    /**
     * schema[调用入口]<br/>
     * 重命名Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param origin 原Schema
     * @param name 新名称
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean rename(DataRuntime runtime, Schema origin, String name) throws Exception {
        return false;
    }

    /**
     * schema[命令合成]<br/>
     * 创建Schema<br/>
     * 其中1.x三选一 不要重复
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildCreateRun(DataRuntime runtime, Schema meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * schema[命令合成]<br/>
     * 修改Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAlterRun(DataRuntime runtime, Schema meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * schema[命令合成]<br/>
     * 重命名
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildRenameRun(DataRuntime runtime, Schema meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * schema[命令合成]<br/>
     * 删除Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Schema meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 创建Schema完成后追加Schema备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Schema meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendCommentRun(DataRuntime runtime, Schema meta) throws Exception {
        return new ArrayList<>();
    }
    /**
     * schema[命令合成-子流程]<br/>
     * 创建Schema完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Schema meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 修改备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param schema Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Schema schema) throws Exception {
        return new ArrayList<>();
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 添加备注(部分数据库需要区分添加还是修改)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param schema Schema
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public  List<Run> buildAddCommentRun(DataRuntime runtime, Schema schema) throws Exception {
        return buildChangeCommentRun(runtime, schema);
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 创建或删除Schema之前  检测Schema是否存在
     * IF NOT EXISTS
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param exists exists
     * @return StringBuilder
     */
    @Override
    public StringBuilder checkSchemaExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
        return builder;
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 创建Schema engine
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    @Override
    public StringBuilder engine(DataRuntime runtime, StringBuilder builder, Schema meta) {
        return builder;
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 编码
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    @Override
    public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Schema meta) {
        return builder;
    }

    /**
     * schema[命令合成-子流程]<br/>
     * Schema备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    @Override
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Schema meta) {
        return builder;
    }

    /**
     * schema[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    @Override
    public StringBuilder property(DataRuntime runtime, StringBuilder builder, Schema meta) {
        return builder;
    }


    /* *****************************************************************************************************************
     * 													database
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(DataRuntime runtime, Database meta)) throws Exception
     * boolean alter(DataRuntime runtime, Database meta) throws Exception
     * boolean drop(DataRuntime runtime, Database meta) throws Exception
     * boolean rename(DataRuntime runtime, Database origin, String name) throws Exception
     ******************************************************************************************************************/

    /**
     * database[调用入口]<br/>
     * 创建Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean create(DataRuntime runtime, Database meta) throws Exception {
        ACTION.DDL action = ACTION.DDL.DATABASE_CREATE;
        String random = random(runtime);
        ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        List<Run> runs = buildCreateRun(runtime, meta);
        return execute(runtime, random, meta, action, runs);
    }

    /**
     * database[调用入口]<br/>
     * 修改Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean alter(DataRuntime runtime, Database meta) throws Exception {
        return false;
    }
    /**
     * database[调用入口]<br/>
     * 删除Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean drop(DataRuntime runtime, Database meta) throws Exception {
        ACTION.DDL action = ACTION.DDL.DATABASE_DROP;
        String random = random(runtime);
        ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        checkSchema(runtime, meta);
        List<Run> runs = buildDropRun(runtime, meta);
        return execute(runtime, random, meta, action, runs);
    }

    /**
     * database[调用入口]<br/>
     * 重命名Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param origin 原Database
     * @param name 新名称
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override
    public boolean rename(DataRuntime runtime, Database origin, String name) throws Exception {
        return false;
    }

    /**
     * database[命令合成]<br/>
     * 创建Database<br/>
     * 其中1.x三选一 不要重复
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildCreateRun(DataRuntime runtime, Database meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * database[命令合成]<br/>
     * 修改Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAlterRun(DataRuntime runtime, Database meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * database[命令合成]<br/>
     * 重命名
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildRenameRun(DataRuntime runtime, Database meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * database[命令合成]<br/>
     * 删除Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Database meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * database[命令合成-子流程]<br/>
     * 创建Database完成后追加Database备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Database meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendCommentRun(DataRuntime runtime, Database meta) throws Exception {
        return new ArrayList<>();
    }
    /**
     * database[命令合成-子流程]<br/>
     * 创建Database完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Database meta) throws Exception {
        return new ArrayList<>();
    }

    /**
     * database[命令合成-子流程]<br/>
     * 修改备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param database Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Database database) throws Exception {
        return new ArrayList<>();
    }

    /**
     * database[命令合成-子流程]<br/>
     * 添加备注(部分数据库需要区分添加还是修改)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param database Database
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public  List<Run> buildAddCommentRun(DataRuntime runtime, Database database) throws Exception {
        return buildChangeCommentRun(runtime, database);
    }

    /**
     * database[命令合成-子流程]<br/>
     * 创建或删除Database之前  检测Database是否存在
     * IF NOT EXISTS
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param exists exists
     * @return StringBuilder
     */
    @Override
    public StringBuilder checkDatabaseExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
        return builder;
    }

    /**
     * database[命令合成-子流程]<br/>
     * 创建Database engine
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    @Override
    public StringBuilder engine(DataRuntime runtime, StringBuilder builder, Database meta) {
        return builder;
    }

    /**
     * database[命令合成-子流程]<br/>
     * 编码
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    @Override
    public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Database meta) {
        return builder;
    }

    /**
     * database[命令合成-子流程]<br/>
     * Database备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    @Override
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Database meta) {
        return builder;
    }

    /**
     * database[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    @Override
    public StringBuilder property(DataRuntime runtime, StringBuilder builder, Database meta) {
        return builder;
    }

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Table meta)
	 * boolean alter(DataRuntime runtime, Table meta)
	 * boolean drop(DataRuntime runtime, Table meta)
	 * boolean rename(DataRuntime runtime, Table origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns)
	 * List<Run> buildRenameRun(DataRuntime runtime, Table meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Table meta)
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table table)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table)
	 * time runtime, StringBuilder builder, Table table)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table table)
	 ******************************************************************************************************************/
	/**
	 * table[调用入口]<br/>
	 * 创建表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Table meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		//检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
		checkPrimary(runtime, meta);
        checkTypeMetadata(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}
    protected void checkTypeMetadata(DataRuntime runtime, Table meta){
        LinkedHashMap<String, Column> columns = meta.getColumns();
        LinkedHashMap<String, TypeMetadata> alias = TypeMetadataHolder.gets(type());
        for(Column column:columns.values()){
            TypeMetadata tm = column.getTypeMetadata();
            if(null == tm || tm == TypeMetadata.NONE){
                TypeMetadata.parse(type(), column, alias, null);
            }
        }
    }


	/**
	 * 检测列的执行命令,all drop alter等
	 * @param meta 表
	 * @return cols
	 */
	protected LinkedHashMap<String, Column> checkColumnAction(DataRuntime runtime, Table meta) {
		Table update = (Table)meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		for(Column col:columns.values()) {
			typeMetadata(runtime, col);
		}
		for(Column col:ucolumns.values()) {
			typeMetadata(runtime, col);
		}
		LinkedHashMap<String, Column> cols = new LinkedHashMap<>();
		// 更新列
		for (Column ucolumn : ucolumns.values()) {
			//先根据原列名 找到数据库中定义的列
			Column column = columns.get(ucolumn.getName().toUpperCase());
			//再检测update(如果name不一样需要rename)
			if(null != ucolumn.getUpdate()) {
				ucolumn = ucolumn.getUpdate();
			}
			if (null != column) {
				// 修改列
				if (!column.equals(ucolumn)) {
					column.setTable(update);
					column.setUpdate(ucolumn, false, false);
					column.setAction(ACTION.DDL.COLUMN_ALTER);
					ucolumn.setAction(ACTION.DDL.COLUMN_ALTER);
					cols.put(column.getName().toUpperCase(), column);
				}
			} else {
				// 添加列
				ucolumn.setTable(update);
				ucolumn.setAction(ACTION.DDL.COLUMN_ADD);
				cols.put(ucolumn.getName().toUpperCase(), ucolumn);
			}
		}
		List<String> deletes = new ArrayList<>();
		// 删除列(根据删除标记)
		for (Column column : ucolumns.values()) {
			if (column.isDrop()) {
				/*drop(column);*/
				deletes.add(column.getName().toUpperCase());
				column.setAction(ACTION.DDL.COLUMN_DROP);
				cols.put(column.getName().toUpperCase(), column);
			}
		}
		// 删除列(根据新旧对比)
		if (meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if (column instanceof Tag) {
					continue;
				}
				if (column.isDrop() || deletes.contains(column.getName().toUpperCase()) || ACTION.DDL.COLUMN_DROP == column.getAction()) {
					//上一步已删除
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					column.setAction(ACTION.DDL.COLUMN_DROP);
					cols.put(column.getName().toUpperCase(), column);
				}
			}
		}
		//忽略 删除不存的的列(原表中本来就没有的 还执行删除)
		for(Column column:cols.values()) {
			if(!columns.containsKey(column.getName().toUpperCase())) {
				if(column.getAction() == ACTION.DDL.COLUMN_DROP) {
					column.setAction(ACTION.DDL.IGNORE);
				}
			}
		}
		return cols;
	}

	/**
	 * 修改主键前先 根据主键检测自增 如果数据库要求自增必须在主键上时才需要执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return boolean
	 * @throws Exception 异常
	 */
	protected List<Run> checkAutoIncrement(DataRuntime runtime, String random, Table table, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Table update = (Table)table.getUpdate();
		if(!table.primaryEquals(update)) {
			LinkedHashMap<String, Column> pks = table.getPrimaryKeyColumns();
			LinkedHashMap<String, Column> npks = update.getPrimaryKeyColumns();
			LinkedHashMap<String, Column> columns = table.getColumns();
			if (null != pks) {
				for (String k : pks.keySet()) {
					Column auto = columns.get(k.toUpperCase());
					if (null != auto && auto.isAutoIncrement()) {//原来是自增
						if (null != npks && !npks.containsKey(auto.getName().toUpperCase())) { //当前不是主键
							auto.primary(false);
							//取消自增
							runs = buildDropAutoIncrement(runtime, auto, slice);
						}
					}
				}
			}
		}
		return runs;
	}

	/**
	 * 合关DDL片段
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param slices slices
	 * @return list
	 */
	@Override
	public List<Run> merge(DataRuntime runtime, Table meta, List<Run> slices) {
		List<Run> runs = new ArrayList<>();
		Run merge = null;
		if(null != slices && !slices.isEmpty()) {
			StringBuilder builder = null;
			boolean first = true;
			for(Run item:slices) {
				if(BasicUtil.isNotEmpty(item)) {
					String line = item.getFinalUpdate().trim();
					if(BasicUtil.isEmpty(line)) {
						continue;
					}
					if(!item.slice()) {
						//不支持合并的(不是片段的)
						runs.add(item);
						continue;
					}
					if(null == merge) {
                        //先加入队列 保证ADD COLUMN在前
						merge = new SimpleRun(runtime);
						builder = merge.getBuilder();
						builder.append("ALTER ").append(keyword(meta)).append(" ");
						name(runtime, builder, meta);
                        runs.add(merge);
					}
					builder.append("\n");
					if(!first) {
						builder.append(", ");
					}
					first = false;
					builder.append(line);
				}
			}
		}
		return runs;
	}

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table meta) throws Exception {
		boolean result = true;
		Table update = (Table)meta.getUpdate();
		//检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
		checkPrimary(runtime, update);
        checkSchema(runtime, meta);
		String name = meta.getName();
		String uname = update.getName();
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, ACTION.DDL.TABLE_ALTER, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		checkSchema(runtime, update);

		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)) {
			//先修改表名，后续在新表名基础上执行
			result = rename(runtime, meta, uname);
			meta.setName(uname);
		}
		if(!result) {
			return result;
		}

        List<Run> runs = buildAlterRun(runtime, meta);;
        if(!runs.isEmpty()){
            result = execute(runtime, random, meta, ACTION.DDL.TABLE_PROPERTY, runs) && result;
            if (meta.swt() == ACTION.SWITCH.BREAK) {
                return result;
            }
        }
		boolean slice  = slice();
		List<Run> slices = new ArrayList<>();
		//List<Run> merges = new ArrayList<>();

		LinkedHashMap<String, Column> cols = checkColumnAction(runtime, meta);
		//主键
		PrimaryKey src_primary = primary(runtime, random, false, meta);
		PrimaryKey cur_primary = update.getPrimaryKey();
		boolean change_pk = !meta.primaryEquals(update);
		if(change_pk) {
			meta.setChangePrimary(1);
		}
		//主动标记删除状态
		if(null != cur_primary && cur_primary.isDrop()) {
			cur_primary.execute(meta.execute());
			if(slice) {
				slices.addAll(buildDropRun(runtime, cur_primary, slice));
			}else {
				drop(runtime, cur_primary);
			}
			cur_primary = null;
			change_pk = false;
		}
		//如果主键有更新 先删除主键 避免alters中把原主键列的非空取消时与主键约束冲突
		try {
			List<Run> autos = checkAutoIncrement(runtime, null, meta, slice);
			if(slice) {
				slices.addAll(autos);
			}else{
				result = execute(runtime, random, meta, ACTION.DDL.TABLE_ALTER, autos) && result;
				if(meta.swt() == ACTION.SWITCH.BREAK) {
					return result;
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		if(change_pk && null != src_primary) {
			src_primary.execute(meta.execute());
			if(slice) {
				slices.addAll(buildDropRun(runtime, src_primary, slice));
			}else {
				drop(runtime, src_primary);
			}
			src_primary = null;
		}
        List<Index> index_alters = new ArrayList<>();
        List<Index> index_adds = new ArrayList<>();
		/*
		索引
		删除先执行 因为有些索引相关的列要删除
		添加和修改 放到column后执行 因为有可能用到添加的列
		在索引上标记删除的才删除,没有明确标记删除的不删除(因为许多情况会生成索引，比如唯一约束也会生成个索引，但并不在uindexes中)
		*/
        LinkedHashMap<String, Index> oindexes = indexes(runtime, random, meta, null);		//原索引
        LinkedHashMap<String, Index> indexes = update.getIndexes();		//新索引
        for(Index index:indexes.values()) {
            if(index.isPrimary()) {
                continue;
            }
            index.execute(meta.execute());
            if(index.isDrop()) {
                //项目中调用drop()明确要删除的
                drop(runtime, index);
            }else{
                if(null != index.getUpdate()) {
                    //改名或设置过update的
                    index_alters.add(index);
                }else {
                    Index oindex = oindexes.get(index.getName().toUpperCase());
                    if (null == oindex) {
                        //名称不存在的
                        index_adds.add(index);
                    }else{
                        if(!index.equals(oindex)) {
                            if(oindex.isPrimary()) {
                                continue;
                            }
                            oindex.execute(meta.execute());
                            oindex.setUpdate(index, false, false);
                            index_alters.add(oindex);
                        }
                    }
                }
            }
        }
		//更新列
		List<Run> alters = buildAlterRun(runtime, meta, cols.values(), slice);
		if(slice) {
			slices.addAll(alters);
		}else{
			if(null != alters && !alters.isEmpty()) {
				result = execute(runtime, random, meta, ACTION.DDL.COLUMN_ALTER, alters) && result;
				if(meta.swt() == ACTION.SWITCH.BREAK) {
					return result;
				}
			}
		}

		//在alters执行完成后 添加主键 避免主键中存在alerts新添加的列
		if(null != cur_primary) {//复合主键的单独添加
			if(change_pk) {
				if(slice) {
					slices.addAll(buildAddRun(runtime, cur_primary, slice));
				}else {
					add(runtime, cur_primary);
				}
			}
		}
		List<Run> merges = merge(runtime, meta, slices);
		result = execute(runtime, random, meta, ACTION.DDL.TABLE_ALTER, merges) && result;
		if(meta.swt() == ACTION.SWITCH.BREAK) {
			return result;
		}
        //修改 添加索引
        for(Index index:index_adds){
            add(runtime, index);
        }
        for(Index index:index_alters){
            alter(runtime, index);
        }
		return result;
	}

	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Table meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * table[调用入口]<br/>
	 * 重命名表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.TABLE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * table[命令合成]<br/>
	 * 创建表<br/>
	 * 关于创建主键的几个环节<br/>
	 * 1.1.定义列时 标识 primary(DataRuntime runtime, StringBuilder builder, Column column)<br/>
	 * 1.2.定义表时 标识 primary(DataRuntime runtime, StringBuilder builder, Table table)<br/>
	 * 1.3.定义完表DDL后，单独创建 primary(DataRuntime runtime, PrimaryKey primary)根据三选一情况调用buildCreateRun<br/>
	 * 2.单独创建 buildCreateRun(DataRuntime runtime, PrimaryKey primary)<br/>
	 * 其中1.x三选一 不要重复
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return runs
	 * @throws Exception
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成]<br/>
	 * 修改表 只生成修改表本身属性 不生成关于列及索引的
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Table update = (Table)meta.getUpdate();
        //修改表备注
        String ucomment = update.getComment();
        String comment = meta.getComment();
        if(BasicUtil.isEmpty(ucomment) && BasicUtil.isEmpty(comment)) {
            //都为空时不更新
        }else {
            if (!BasicUtil.equals(comment, ucomment)) {
                /*swt = InterceptorProxy.prepare(runtime, random, ACTION.DDL.TABLE_COMMENT, meta);
                if (swt == ACTION.SWITCH.BREAK) {
                    return false;
                }*/
                if (BasicUtil.isNotEmpty(meta.getComment())) {
                    runs.addAll(buildChangeCommentRun(runtime, update));
                } else {
                    runs.addAll(buildAddCommentRun(runtime, update));
                }
            }
        }
		return runs;
	}

	/**
	 * table[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param columns 列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		for(Column column:columns) {
			ACTION.DDL action = column.getAction();
			CMD cmd = null;
			if(null != action) {
				cmd = action.getCmd();
			}
			if(CMD.IGNORE == cmd) {
				continue;
			}
			if(CMD.CREATE == cmd) {
				runs.addAll(buildAddRun(runtime, column, slice));
			}else if(CMD.ALTER == cmd) {
				runs.addAll(buildAlterRun(runtime, column, slice));
			}else if(CMD.DROP == cmd) {
				runs.addAll(buildDropRun(runtime, column, slice));
			}
		}
		return runs;
	}

	/**
	 * table[命令合成]<br/>
	 * 重命名
	 * 子类实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成]<br/>
	 * 删除表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 */
	@Override
	public void checkPrimary(DataRuntime runtime, Table table) {
		if(null != table) {
			table.checkColumnPrimary();
		}
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 定义表的主键标识,在创建表的DDL结尾部分(注意不要跟列定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 engine
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 body部分包含column index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 columns部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table table) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 索引部分，与buildAppendIndexRun二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder indexes(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder indexes(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 备注 创建表的完整DDL拼接COMMENT部分，与buildAppendCommentRun二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 数据模型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 分桶方式
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 物化视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 扩展属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

    @Override
    public StringBuilder option(DataRuntime runtime, StringBuilder builder, Table meta) {
        return builder;
    }
	/**
	 * table[命令合成-子流程]<br/>
	 * 主表设置分区依据(根据哪几列分区)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(相关主表)<br/>
	 * 如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(分区依据值)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/**
	 * table[命令合成-子流程]<br/>
	 * 继承自table.getInherit
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, View view) throws Exception;
	 * boolean alter(DataRuntime runtime, View view) throws Exception;
	 * boolean drop(DataRuntime runtime, View view) throws Exception;
	 * boolean rename(DataRuntime runtime, View origin, String name) throws Exception;
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception;
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception;
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception;
	 * StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view);
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 创建视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, View meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, View meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, View meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * view[调用入口]<br/>
	 * 重命名视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 视图
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, View origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.VIEW_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * view[命令合成]<br/>
	 * 创建视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return Run
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图头部
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图选项
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	@Override
	public StringBuilder buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
	}

	/**
	 * view[命令合成]<br/>
	 * 修改视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成]<br/>
	 * 重命名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成]<br/>
	 * 删除视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, View meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, View meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}

	/**
	 * view[命令合成-子流程]<br/>
	 * 视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													MasterTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, MasterTable meta)
	 * boolean alter(DataRuntime runtime, MasterTable meta)
	 * boolean drop(DataRuntime runtime, MasterTable meta)
	 * boolean rename(DataRuntime runtime, MasterTable origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildDropRun(DataRuntime runtime, MasterTable table)
	 * [命令合成-子流程]
	 * List<Run> buildAlterRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildRenameRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table)
	 ******************************************************************************************************************/

	/**
	 * master table[调用入口]<br/>
	 * 创建主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, MasterTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, MasterTable meta) throws Exception {
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		String random = random(runtime);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		boolean result = true;
		checkSchema(runtime, meta);
		Table update = meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		LinkedHashMap<String, Tag> tags = meta.getTags();
		LinkedHashMap<String, Tag> utags = update.getTags();
		String name = meta.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();

		if(!name.equalsIgnoreCase(uname)) {
			result = rename(runtime, meta, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()) {
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column) {
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(runtime, column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(runtime, ucolumn);
				result = true;
			}
		}
		// 删除列
		if(meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag) {
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(runtime, column);
					result = true;
				}
			}
		}
		// 更新标签
		for(Tag utag : utags.values()) {
			Tag tag = tags.get(utag.getName().toUpperCase());
			if(null != tag) {
				// 修改列
				tag.setTable(update);
				tag.setUpdate(utag, false, false);
				alter(runtime, tag);
				result = true;
			}else{
				// 添加列
				utag.setTable(update);
				add(runtime, utag);
				result = true;
			}
		}
		// 删除标签
		if(meta.isAutoDropColumn()) {
			for (Tag tag : tags.values()) {
				Tag utag = utags.get(tag.getName().toUpperCase());
				if (null == utag) {
					tag.setTable(update);
					drop(runtime, tag);
					result = true;
				}
			}
		}
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[alter master table][table:{}][result:{}][执行耗时:{}]", random, meta.getName(), result, DateUtil.format(System.currentTimeMillis() - fr));
		}
		return result;
	}

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, MasterTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * master table[调用入口]<br/>
	 * 重命名主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.MASTER_TABLE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);

		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * master table[命令合成]<br/>
	 * 创建主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master table[命令合成]<br/>
	 * 删除主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 主表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception;
	 * boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception;
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 * [命令合成-子流程]
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;
	 *
	 ******************************************************************************************************************/

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean create(DataRuntime runtime, PartitionTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception {
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		String random = random(runtime);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		boolean result = true;

		checkSchema(runtime, meta);
		Table update = meta.getUpdate();
		LinkedHashMap<String, Column> columns = meta.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = meta.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)) {
			result = rename(runtime, meta, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()) {
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column) {
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
				alter(runtime, column);
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(runtime, ucolumn);
				result = true;
			}
		}
		// 删除列
		if(meta.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag) {
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(runtime, column);
					result = true;
				}
			}
		}
		if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
			log.info("{}[alter partition table][table:{}][result:{}][执行耗时:{}]", random, meta.getName(), result, DateUtil.format(System.currentTimeMillis() - fr));
		}
		return result;
	}

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return  execute(runtime, random, meta, action, runs);
	}

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.PARTITION_TABLE_RENAME;
		origin.setNewName(name);
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * partition table[命令合成]<br/>
	 * 创建分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成]<br/>
	 * 修改分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成-]<br/>
	 * 删除分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * partition table[命令合成-子流程]<br/>
	 * 修改分区表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable meta)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Column meta)
	 * boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger)
	 * boolean alter(DataRuntime runtime, Column meta)
	 * boolean drop(DataRuntime runtime, Column meta)
	 * boolean rename(DataRuntime runtime, Column origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column)
	 * List<Run> buildRenameRun(DataRuntime runtime, Column column)
	 * [命令合成-子流程]
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Column column)
	 * String alterColumnKeyword(DataRuntime runtime)
	 * StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column column)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column)
	 * StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column, String type, int ignorePrecision, boolean ignoreScale)
	 * int ignorePrecision(DataRuntime runtime, Column column)
	 * int ignoreScale(DataRuntime runtime, Column column)
	 * Boolean checkIgnorePrecision(DataRuntime runtime, String datatype)
	 * int checkIgnoreScale(DataRuntime runtime, String datatype)
	 * StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action)
	 * StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * column[调用入口]<br/>
	 * 添加列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Column meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.COLUMN_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param trigger 修改异常时，是否触发监听器
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta, false);

		try{
			result = execute(runtime, random, meta, action, runs);
			if(meta.swt() == ACTION.SWITCH.BREAK) {
				return result;
			}
		}catch (Exception e) {
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改Column执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				//DDL修改列异常后 -1:抛出异常 0:中断修改 1:删除列 n:总行数小于多少时更新值否则触发另一个监听
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION == -1) {
					throw e;
				}else if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改Column执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == ACTION.SWITCH.CONTINUE) {
					result = alter(runtime, table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}
		return result;
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Column meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			LinkedHashMap<String, Table> tables = tables(runtime, null, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(runtime, table, meta, true);
	}

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Column meta) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.COLUMN_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * column[调用入口]<br/>
	 * 重命名列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 列
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Column origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.COLUMN_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin, false);
		return  execute(runtime, random, origin, action, runs);
	}

	/**
	 * column[命令合成]<br/>
	 * 添加列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * column[命令合成]<br/>
	 * 删除列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	@Override
	public String alterColumnKeyword(DataRuntime runtime) {
		return "";
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列引导<br/>
	 * alter table sso_user [add column] type_code int
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param meta 列
	 * @return String
	 */
	@Override
	public StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 删除列引导<br/>
	 * alter table sso_user [drop column] type_code
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param meta 列
	 * @return String
	 */
	@Override
	public StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列，依次拼接下面几个属性注意不同数据库可能顺序不一样
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action) {
		String define = meta.getDefinition();
		if(BasicUtil.isNotEmpty(define)) {
			builder.append(" ").append(define);
			return builder;
		}
		// 数据类型
		type(runtime, builder, meta);
		//聚合
		aggregation(runtime, builder, meta);
		// 编码
		charset(runtime, builder, meta);
		// 默认值
		defaultValue(runtime, builder, meta);
		// 非空
		nullable(runtime, builder, meta, action);
		//主键
		primary(runtime, builder, meta);
		//唯一索引
		unique(runtime, builder, meta);

		// 递增(注意有些数据库不需要是主键)
		increment(runtime, builder, meta);
		// 更新行事件
		onupdate(runtime, builder, meta);
		// 备注
		comment(runtime, builder, meta);
		// 位置
		position(runtime, builder, meta);

		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:聚合类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param type 数据类型(已经过转换)
	 * @param ignoreLength 是否忽略长度
	 * @param ignorePrecision 是否忽略有效位数
	 * @param ignoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale) {
		if(null == builder) {
			builder = new StringBuilder();
		}
		String finalType = meta.getFinalType();
		if(BasicUtil.isNotEmpty(finalType)) {
			builder.append(finalType);
			return builder;
		}
		meta.ignoreLength(ignoreLength);
		meta.ignorePrecision(ignorePrecision);
		meta.ignoreScale(ignoreScale);
		meta.parseType(2, type());
		builder.append(meta.getFullType(type()));
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有长度<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type 数据类型
	 * @return boolean
	 */
	@Override
	public int ignoreLength(DataRuntime runtime, TypeMetadata type) {
		return MetadataReferHolder.ignoreLength(type(), type);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
	@Override
	public int ignorePrecision(DataRuntime runtime, TypeMetadata type) {
		return MetadataReferHolder.ignorePrecision(type(), type);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:定义列:是否忽略小数位<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
    @Override
    public int ignoreScale(DataRuntime runtime, TypeMetadata type) {
		return MetadataReferHolder.ignoreScale(type(), type);
    }

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:非空
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action) {
		if(meta.isPrimaryKey()) {
			builder.append(" NOT NULL");
			return builder;
		}
		if(null == meta.getDefaultValue()) {
			Boolean nullable = meta.getNullable();
			if(nullable != null) {
				if (!nullable) {
					builder.append(" NOT");
				}
				builder.append(" NULL");
			}
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:默认值
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column meta) {
		Object def = null;
		boolean defaultCurrentDateTime = false;
		Column update = meta.getUpdate();
		if(null != update) {
			//自增序列不要默认值nextval('crm_user_id_seq'::regclass)
			if(update.isAutoIncrement()) {
				return builder;
			}
			def = update.getDefaultValue();
			defaultCurrentDateTime = update.isDefaultCurrentDateTime();
		}else {
			if(meta.isAutoIncrement()) {
				return builder;
			}
			def = meta.getDefaultValue();
			defaultCurrentDateTime = meta.isDefaultCurrentDateTime();
		}
		if(null == def && defaultCurrentDateTime) {
			String type = meta.getFullType(type()).toLowerCase();
			if (type.contains("timestamp")) {
				def = SQL_BUILD_IN_VALUE.CURRENT_TIMESTAMP;
			}else{
				def = SQL_BUILD_IN_VALUE.CURRENT_DATETIME;
			}
		}
		if(null != def) {
			String str = def.toString().trim();
			builder.append(" DEFAULT ");
			//boolean isCharColumn = isCharColumn(runtime, column);
			SQL_BUILD_IN_VALUE val = checkDefaultBuildInValue(runtime, def);
			if(null != val) {
				def = val;
			}
			if(def instanceof SQL_BUILD_IN_VALUE) {
				String value = value(runtime, meta, (SQL_BUILD_IN_VALUE)def);
				if(null != value) {
					builder.append(value);
				}else{
					throw new RuntimeException("当前adapter没有解析"+def+",可以用${原生SQL}代替,如column.setDefaultValue(\"${now()}\");");
				}
			}else if(str.startsWith("${") && str.endsWith("}")) {
				builder.append(str.substring(2, str.length()-1));
			}else {
				//nextval('crm_user_id_seq'::regclass)
				//DEFAULT NULL::timestamp with time zone,
				if(null != def && def.toString().contains("::")) {
					def = def.toString().split("::")[0];
				}
				def = write(runtime, meta, def, false, false);
				if(null == def) {
					def = meta.getDefaultValue();
				}
				//format(builder, def);
				builder.append(def);
			}
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:定义列的主键标识(注意不要跟表定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}
	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:唯一索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder unique(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder unique(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:递增列,需要通过serial实现递增的在type(DataRuntime runtime, StringBuilder builder, Column meta)中实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:更新行事件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:位置
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Tag meta)
	 * boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger)
	 * boolean alter(DataRuntime runtime, Tag meta)
	 * boolean drop(DataRuntime runtime, Tag meta)
	 * boolean rename(DataRuntime runtime, Tag origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildDropRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag)
	 * StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Tag meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TAG_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);

		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @param trigger 修改异常时，是否触发监听器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TAG_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta, false);
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, meta, action, runs);
		}catch (Exception e) {
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改TAG执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改TAG执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == ACTION.SWITCH.CONTINUE) {
					result = alter(runtime, table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}
		return result;
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Tag meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), 1);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else {
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta, true);
	}

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Tag meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TAG_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * tag[调用入口]<br/>
	 * 重命名标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原标签
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.TAG_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin, slice());
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * tag[命令合成]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Tag meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Tag meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * tag[命令合成]<br/>
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, PrimaryKey meta)
	 * boolean alter(DataRuntime runtime, PrimaryKey meta)
	 * boolean alter(DataRuntime runtime, Table table, PrimaryKey meta)
	 * boolean drop(DataRuntime runtime, PrimaryKey meta)
	 * boolean rename(DataRuntime runtime, PrimaryKey origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary)
	 * List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary)
	 ******************************************************************************************************************/

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta, false);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables( runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 修改Table最后修改主键,注意不要与列上的主键标识重复,如果列上支持标识主键，这里不需要实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, origin, meta);
		return execute(runtime, random, table, action, runs);
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 主键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * primary[命令合成]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * primary[命令合成]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta, boolean slice) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, PrimaryKey meta, boolean slice)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, PrimaryKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, ForeignKey meta)
	 * boolean alter(DataRuntime runtime, ForeignKey meta)
	 * boolean alter(DataRuntime runtime, Table table, ForeignKey meta)
	 * boolean drop(DataRuntime runtime, ForeignKey meta)
	 * boolean rename(DataRuntime runtime, ForeignKey origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta)
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, ForeignKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.FOREIGN_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * foreign[调用入口]
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * foreign[调用入口]
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.FOREIGN_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * foreign[调用入口]
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.FOREIGN_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * foreign[命令合成]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) ", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) ", 37));
		}
		return new ArrayList<>();
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Index meta)
	 * boolean alter(DataRuntime runtime, Index meta)
	 * boolean alter(DataRuntime runtime, Table table, Index meta)
	 * boolean drop(DataRuntime runtime, Index meta)
	 * boolean rename(DataRuntime runtime, Index origin, String name)
	 * [命令合成]
	 * List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Index meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Index meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Index meta)
	 * [命令合成-子流程]
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta)
	 ******************************************************************************************************************/

	/**
	 * index[调用入口]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Index meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_ADD;
		String random = random(runtime);
		Index update = (Index)meta.getUpdate();
		if(null != update) {
			//如果是修改 调用过来的  添加update
			meta = update;
		}
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Index meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Index meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * index[调用入口]<br/>
	 * 重命名索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 索引
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Index origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.INDEX_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * index[命令合成]<br/>
	 * 创建表过程添加索引,表创建完成后添加索引,于表内索引index(DataRuntime, StringBuilder, Table)二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	@Override
	public List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(null != meta && !meta.isEmpty()) {
			LinkedHashMap<String, Index> indexes = meta.getIndexes();
			if(null != indexes) {
				for(Index index:indexes.values()) {
					if(index.isPrimary()) {
						continue;
					}
					runs.addAll(buildAddRun(runtime, index));
				}
			}
		}
		return runs;
	}

	/**
	 * index[命令合成]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Index meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * index[命令合成]<br/>
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Index meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * index[命令合成]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Index meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * index[命令合成]<br/>
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Index meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Index meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta)", 37));
		}
		return builder;
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder property(DataRuntime runtime, StringBuilder builder, Index meta) {
		return builder;
	}

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta)", 37));
		}
		return builder;
	}
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists)", 37));
		}
		return builder;
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean add(DataRuntime runtime, Constraint meta)
	 * boolean alter(DataRuntime runtime, Constraint meta)
	 * boolean alter(DataRuntime runtime, Table table, Constraint meta)
	 * boolean drop(DataRuntime runtime, Constraint meta)
	 * boolean rename(DataRuntime runtime, Constraint origin, String name)
	 * [命令合成]
	 * List<Run> buildAddRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Constraint meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Constraint meta)
	 ******************************************************************************************************************/

	/**
	 * constraint[调用入口]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Constraint meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAddRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Constraint meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table) {
			List<Table> tables = tables(runtime, null, false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), Table.TYPE.NORMAL.value);
			if(tables.isEmpty()) {
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.get(0);
			}
		}
		return alter(runtime, table, meta);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Constraint meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * constraint[调用入口]<br/>
	 * 重命名约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 约束
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.CONSTRAINT_RENAME;
		String random = random(runtime);
		origin.setNewName(name);

		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * constraint[命令合成]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Constraint meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAddRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Constraint meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Constraint meta)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * trigger[调用入口]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean add(DataRuntime runtime, Trigger meta) throws Exception {
		boolean result = false;
		ACTION.DDL action = ACTION.DDL.TRIGGER_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Trigger meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Trigger meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * trigger[调用入口]<br/>
	 * 重命名触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 触发器
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.TRIGGER_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * trigger[命令合成]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Trigger meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Trigger meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * trigger[命令合成]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Trigger meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Trigger meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Trigger meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Procedure meta)
	 * boolean alter(DataRuntime runtime, Procedure meta)
	 * boolean drop(DataRuntime runtime, Procedure meta)
	 * boolean rename(DataRuntime runtime, Procedure origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildDropRun(DataRuntime runtime, Procedure meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, Procedure meta)
	 * [命令合成-子流程]
	 * StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter)
	 ******************************************************************************************************************/

	/**
	 * procedure[调用入口]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Procedure meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PRIMARY_ADD;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Procedure meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PROCEDURE_ALTER;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Procedure meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.PROCEDURE_DROP;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * procedure[调用入口]<br/>
	 * 重命名存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 存储过程
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.PROCEDURE_RENAME;
		origin.setNewName(name);
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);
		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Procedure meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	@Override
	public StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter)", 37));
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Function meta)
	 * boolean alter(DataRuntime runtime, Function meta)
	 * boolean drop(DataRuntime runtime, Function meta)
	 * boolean rename(DataRuntime runtime, Function origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Function function)
	 * List<Run> buildAlterRun(DataRuntime runtime, Function function)
	 * List<Run> buildDropRun(DataRuntime runtime, Function function)
	 * List<Run> buildRenameRun(DataRuntime runtime, Function function)
	 ******************************************************************************************************************/

	/**
	 * function[调用入口]
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Function meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.FUNCTION_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * function[调用入口]
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Function meta) throws Exception {
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.FUNCTION_ALTER;
		ACTION.SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * function[调用入口]
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Function meta) throws Exception {
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.FUNCTION_DROP;
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);

		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * function[调用入口]
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Function origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.FUNCTION_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);

		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * function[命令合成]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * function[命令合成]<br/>
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Function meta)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, Sequence meta)
	 * boolean alter(DataRuntime runtime, Sequence meta)
	 * boolean drop(DataRuntime runtime, Sequence meta)
	 * boolean rename(DataRuntime runtime, Sequence origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildAlterRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildDropRun(DataRuntime runtime, Sequence sequence)
	 * List<Run> buildRenameRun(DataRuntime runtime, Sequence sequence)
	 ******************************************************************************************************************/

	/**
	 * sequence[调用入口]
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean create(DataRuntime runtime, Sequence meta) throws Exception {
		ACTION.DDL action = ACTION.DDL.SEQUENCE_CREATE;
		String random = random(runtime);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildCreateRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * sequence[调用入口]
	 * 修改序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean alter(DataRuntime runtime, Sequence meta) throws Exception {
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.SEQUENCE_ALTER;
		ACTION.SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);
		List<Run> runs = buildAlterRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * sequence[调用入口]
	 * 删除序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean drop(DataRuntime runtime, Sequence meta) throws Exception {
		boolean result = false;
		String random = random(runtime);
		ACTION.DDL action = ACTION.DDL.SEQUENCE_DROP;
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, meta);

		List<Run> runs = buildDropRun(runtime, meta);
		return execute(runtime, random, meta, action, runs);
	}

	/**
	 * sequence[调用入口]
	 * 重命名序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 序列
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	@Override
	public boolean rename(DataRuntime runtime, Sequence origin, String name) throws Exception {
		ACTION.DDL action = ACTION.DDL.SEQUENCE_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		ACTION.SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == ACTION.SWITCH.BREAK) {
			return false;
		}
		checkSchema(runtime, origin);

		List<Run> runs = buildRenameRun(runtime, origin);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * sequence[命令合成]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildAlterRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[命令合成]<br/>
	 * 删除序列
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Sequence meta) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Sequence meta)", 37));
		}
		return new ArrayList<>();
	}

	/* *****************************************************************************************************************
	 *
	 * 													Authorize
	 *
	 * =================================================================================================================
	 * role			: 角色
	 * user			: 用户
	 * grant		: 授权
	 * privilege	: 权限
	 ******************************************************************************************************************/

	/**
	 * 执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return boolean
	 */
	@Override
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.Authorize action, Run run) {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.Authorize action, Run run)", 37));
		}
		return false;
	}
	@Override
	public boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.Authorize action, List<Run> runs) {
		boolean result = true;
		int idx = 0;
		long frs = System.currentTimeMillis();
		for (Run run : runs) {
			result = execute(runtime, random + "[index:" + idx++ + "]", meta, action, run) && result;
		}
		long millis = System.currentTimeMillis() - frs;
		if(runs.size()>1 && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
			//如果有多个命令执行，再统计一次 合计时间
			log.info("{}[action:{}][meta:{}][cmds:{}][result:{}][执行耗时:{}]", random, action, meta.getName(), runs.size(), result, DateUtil.format(millis));
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													role
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Role role) throws Exception
	 * boolean rename(DataRuntime runtime, Role origin, Role update) throws Exception;
	 * boolean delete(DataRuntime runtime, Role role) throws Exception
	 * <T extends Role> List<T> roles(Catalog catalog, Schema schema, String pattern) throws Exception
	 * List<Run> buildQueryRolesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) throws Exception
	 * <T extends Role> List<T> roles(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> roles, DataSet set) throws Exception
	 * <T extends Role> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)
	 * <T extends Role> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)
	 ******************************************************************************************************************/

	/**
	 * role[调用入口]<br/>
	 * 创建角色
	 * @param role 角色
	 * @return boolean
	 */
	@Override
	public boolean create(DataRuntime runtime, Role role) throws Exception {
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.ROLE_CREATE;
		List<Run> runs = buildCreateRun(runtime, role);
		return execute(runtime, random, role, action, runs);
	}

	/**
	 * role[调用入口]<br/>
	 * 角色重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	@Override
	public boolean rename(DataRuntime runtime, Role origin, Role update) throws Exception {
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.ROLE_RENAME;
		List<Run> runs = buildRenameRun(runtime, origin, update);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * role[调用入口]<br/>
	 * 删除角色
	 * @param role 角色
	 * @return boolean
	 */
	@Override
	public boolean drop(DataRuntime runtime, Role role) throws Exception {
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.ROLE_DROP;
		List<Run> runs = buildDropRun(runtime, role);
		return execute(runtime, random, role, action, runs);
	}

	/**
	 * role[调用入口]<br/>
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, Role query) throws Exception {
        if(null == random) {
            random = random(runtime);
        }
        List<T> list = new ArrayList<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
                List<Run> runs = buildQueryRolesRun(runtime, greedy, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        list = roles(runtime, idx++, true, list, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[roles][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[roles][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[roles][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 * role[命令合成]<br/>
	 * 创建角色
	 * @param role 角色
	 * @return List
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Role role) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildCreateRun(DataRuntime runtime, Role role)", 37));
        }
		return new ArrayList<>();
	}
	/**
	 * role[命令合成]<br/>
	 * 角色重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return List
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Role origin, Role update) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, Role origin, Role update)", 37));
        }
		return new ArrayList<>();
	}

	/**
	 * role[命令合成]<br/>
	 * 删除角色
	 * @param role 角色
	 * @return List
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Role role) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, Role role)", 37));
        }
		return new ArrayList<>();
	}

	/**
	 * role[命令合成]<br/>
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public List<Run> buildQueryRolesRun(DataRuntime runtime, boolean greedy, Role query) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryRolesRun(DataRuntime runtime, boolean greedy, Role query)", 37));
        }
		return new ArrayList<>();
	}

    /**
     * role[结果集封装]<br/>
     * Role 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initRoleFieldRefer() {
        return new MetadataFieldRefer(Role.class);
    }

    /**
	 * role[结果集封装]<br/>
	 * 根据查询结果集构造 role
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryRolessRun 返回顺序
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Role> List<T> roles(DataRuntime runtime, int index, boolean create, List<T> previous, Role query, DataSet set) throws Exception {
        if (null == previous) {
            previous = new ArrayList<>();
        }
        for (DataRow row : set) {
            T meta = null;
            meta = init(runtime, index, meta, query, row);
            if(null == meta || meta.isEmpty()) {
                continue;
            }
            meta = detail(runtime, index, meta, query, row);
            previous.add(meta);
        }
        return previous;
	}

	/**
	 * role[结果集封装]<br/>
	 * 根据查询结果封装 role 对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Role
	 */
	@Override
	public <T extends Role> T init(DataRuntime runtime, int index, T meta, Role query, DataRow row) {
        if(null == meta) {
            meta = (T)new Role();
        }
        MetadataFieldRefer refer = refer(runtime, Role.class);
        meta.setMetadata(row);
        meta.setName(getString(row, refer, org.anyline.entity.authorize.Role.FIELD_NAME));
        return meta;
	}

	/**
	 * role[结果集封装]<br/>
	 * 根据查询结果封装 role 对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Role
	 */
	@Override
	public <T extends Role> T detail(DataRuntime runtime, int index, T meta, Role query, DataRow row) {
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													user
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, User user) throws Exception
	 * boolean rename(DataRuntime runtime, User origin, User update) throws Exception;
	 * boolean drop(DataRuntime runtime, User user) throws Exception
	 * List<User> users(Catalog catalog, Schema schema, String pattern) throws Exception
	 * List<Run> buildQueryUsersRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) throws Exception
	 * <T extends User> List<T> users(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> users, DataSet set) throws Exception
	 * <T extends User> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)
	 * <T extends User> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)
	 ******************************************************************************************************************/

	/**
	 * user[调用入口]<br/>
	 * 创建用户
	 * @param user 用户
	 * @return boolean
	 */
	@Override
	public boolean create(DataRuntime runtime, User user) throws Exception {
		boolean result = false;
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.USER_CREATE;
		List<Run> runs = buildCreateRun(runtime, user);
		return execute(runtime, random, user, action, runs);
	}

	/**
	 * user[调用入口]<br/>
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	@Override
	public boolean rename(DataRuntime runtime, User origin, User update) throws Exception {
		boolean result = false;
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.USER_RENAME;
		List<Run> runs = buildRenameRun(runtime, origin, update);
		return execute(runtime, random, origin, action, runs);
	}

	/**
	 * user[调用入口]<br/>
	 * 删除用户
	 * @param user 用户
	 * @return boolean
	 */
	@Override
	public boolean drop(DataRuntime runtime, User user) throws Exception {
		boolean result = false;
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.USER_DROP;
		List<Run> runs = buildDropRun(runtime, user);
		return execute(runtime, random, user, action, runs);
	}

	/**
	 * user[调用入口]<br/>
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public <T extends User> List<T> users(DataRuntime runtime, String random, boolean greedy, User query) throws Exception {
        if(null == random) {
            random = random(runtime);
        }
        List<T> list = new ArrayList<>();
        try{
            long fr = System.currentTimeMillis();
            // 根据系统表查询
            try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
                List<Run> runs = buildQueryUsersRun(runtime, greedy, query);
                if(null != runs) {
                    int idx = 0;
                    for(Run run:runs) {
                        DataSet set = selectMetadata(runtime, random, run);
                        list = users(runtime, idx++, true, list, query, set);
                    }
                }
            }catch (Exception e) {
                if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                    e.printStackTrace();
                }else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("{}[users][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
                }
            }
            if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[users][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
            }
        }catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }else{
                log.error("[users][result:fail][msg:{}]", e.toString());
            }
        }
        return list;
	}

	/**
	 * user[命令合成]<br/>
	 * 创建用户
	 * @param user 用户
	 * @return List
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, User user) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildCreateRun(DataRuntime runtime, User user)", 37));
		}
		return new ArrayList<>();
	}
	/**
	 * user[命令合成]<br/>
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return List
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, User origin, User update) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildRenameRun(DataRuntime runtime, User origin, User update)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * user[命令合成]<br/>
	 * 删除用户
	 * @param user 用户
	 * @return List
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, User user) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildDropRun(DataRuntime runtime, User user)", 37));
		}
		return new ArrayList<>();
	}

	/**
	 * user[命令合成]<br/>
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public List<Run> buildQueryUsersRun(DataRuntime runtime, boolean greedy, User query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryUsersRun(DataRuntime runtime, User query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * User[结果集封装]<br/>
     * User 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initUserFieldRefer() {
        return new MetadataFieldRefer(User.class);
    }
	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果集构造 user
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryUserssRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends User> List<T> users(DataRuntime runtime, int index, boolean create, List<T> previous, User query, DataSet set) throws Exception {
		if(null == previous) {
			previous = new ArrayList<>();
		}
		for(DataRow row:set) {
			T meta = null;
			meta = init(runtime, index, meta, query, row);
			meta = detail(runtime, index, meta, query, row);
			previous.add(meta);
		}
		return previous;
	}

	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果封装 user 对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return User
	 */
	@Override
	public <T extends User> T init(DataRuntime runtime, int index, T meta, User query, DataRow row) {
        if(null == meta) {
            meta = (T)new User();
        }
        MetadataFieldRefer refer = refer(runtime, Procedure.class);
        meta.setMetadata(row);
        meta.setName(row.getString(refer.maps(org.anyline.entity.authorize.User.FIELD_NAME)));
        meta.setHost(row.getString(refer.maps(org.anyline.entity.authorize.User.FIELD_HOST)));
        return meta;
	}

	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果封装 user 对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return User
	 */
	@Override
	public <T extends User> T detail(DataRuntime runtime, int index, T meta, User query, DataRow row) {
		return meta;
	}

	/* *****************************************************************************************************************
	 * 													privilege
	 * -----------------------------------------------------------------------------------------------------------------
	 * <T extends Privilege> List<T> privileges(DataRuntime runtime, User user)
	 * List<Run> buildQueryPrivilegesRun(DataRuntime runtime, User user) throws Exception
	 * <T extends Privilege> List<T> privileges(DataRuntime runtime, int index, boolean create, User user, List<T> privileges, DataSet set) throws Exception
	 * <T extends Privilege> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, User user, DataRow row)
	 * <T extends Privilege> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row)
	 * Privilege.MetadataAdapter privilegeMetadataAdapter(DataRuntime runtime)
	 ******************************************************************************************************************/

	/**
	 * privilege[调用入口]<br/>
	 * 查询用户权限
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public <T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, Privilege query) throws Exception {
		User user = query.getUser();
        if(null == random) {
            random = random(runtime);
        }
		List<T> list = new ArrayList<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
                if(!greedy) {
                    checkSchema(runtime, query);
                }
				List<Run> runs = buildQueryPrivilegesRun(runtime, greedy, query);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = selectMetadata(runtime, random, run);
                        list = privileges(runtime, idx++, true, list, query, set);
					}
				}
			}catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
					log.warn("{}[privileges][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), e.toString());
				}
			}
			if (ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
				log.info("{}[privileges][result:{}][执行耗时:{}]", random, list.size(), DateUtil.format(System.currentTimeMillis() - fr));
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[privileges][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}

	/**
	 * privilege[命令合成]<br/>
	 * 查询用户权限
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	@Override
	public List<Run> buildQueryPrivilegesRun(DataRuntime runtime, boolean regreedy, Privilege query) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 List<Run> buildQueryPrivilegesRun(DataRuntime runtime, Privilege query)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * privilege[结果集封装]<br/>
     * Privilege 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initPrivilegeFieldRefer() {
        return new MetadataFieldRefer(Privilege.class);
    }

    /**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Privilege> List<T> privileges(DataRuntime runtime, int index, boolean create,  List<T> previous, Privilege query, DataSet set) throws Exception {
		if(null == previous) {
			previous = new ArrayList<>();
		}
		for(DataRow row:set) {
			T meta = null;
			meta = init(runtime, index, meta, query, row);
			meta = detail(runtime, index, meta, query, row);
			previous.add(meta);
		}
		return previous;
	}

	/**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果封装Privilege对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Privilege
	 */
	@Override
	public <T extends Privilege> T init(DataRuntime runtime, int index, T meta, Privilege query, DataRow row) {
        if(null == meta) {
            meta = (T)new Privilege();
        }
        MetadataFieldRefer refer = refer(runtime, Procedure.class);
        meta.setMetadata(row);
        meta.setName(row.getString(refer.maps(Procedure.FIELD_NAME)));
        return meta;
	}

	/**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果封装Privilege对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Privilege
	 */
	@Override
	public <T extends Privilege> T detail(DataRuntime runtime, int index, T meta, Privilege query, DataRow row) {
		return meta;
	}

    /* *****************************************************************************************************************
     * 													grant
     * -----------------------------------------------------------------------------------------------------------------
     * boolean grant(DataRuntime runtime, User user, Privilege ... privileges) throws Exception
     * boolean grant(DataRuntime runtime, User user, Role ... roles) throws Exception
     * boolean grant(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     * List<Run> buildGrantRun(DataRuntime runtime, User user, Privilege ... privileges) throws Exception
     * List<Run> buildGrantRun(DataRuntime runtime, User user, Role ... roles) throws Exception
     * List<Run> buildGrantRun(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     ******************************************************************************************************************/

	/**
	 * grant[调用入口]<br/>
	 * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	@Override
	public boolean grant(DataRuntime runtime, User user, Privilege ... privileges)  throws Exception {
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.GRANT;
		List<Run> runs = buildGrantRun(runtime, user, privileges);
		return execute(runtime, random, user, action, runs);
	}

    /**
     * grant[调用入口]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */
    @Override
    public boolean grant(DataRuntime runtime, User user, Role ... roles)  throws Exception {
        String random = random(runtime);
        ACTION.Authorize action = ACTION.Authorize.GRANT;
        List<Run> runs = buildGrantRun(runtime, user, roles);
        return execute(runtime, random, user, action, runs);
    }

    /**
     * grant[调用入口]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */
    @Override
    public boolean grant(DataRuntime runtime, Role role, Privilege ... privileges)  throws Exception {
        String random = random(runtime);
        ACTION.Authorize action = ACTION.Authorize.GRANT;
        List<Run> runs = buildGrantRun(runtime, role, privileges);
        return execute(runtime, random, role, action, runs);
    }

	/**
	 * grant[命令合成]<br/>
	 * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param user 用户
	 * @param privileges 权限
	 * @return List
	 */
	@Override
	public List<Run> buildGrantRun(DataRuntime runtime, User user, Privilege ... privileges) throws Exception {
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildGrantRun(DataRuntime runtime, User user, Privilege ... privileges)", 37));
		}
		return new ArrayList<>();
	}

    /**
     * grant[命令合成]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return List
     */
    @Override
    public List<Run> buildGrantRun(DataRuntime runtime, User user, Role ... roles) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildGrantRun(DataRuntime runtime, User user, Role ... roles)", 37));
        }
        return new ArrayList<>();
    }

    /**
     * grant[命令合成]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return List
     */
    @Override
    public List<Run> buildGrantRun(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildGrantRun(DataRuntime runtime, User user, Privilege ... privileges)", 37));
        }
        return new ArrayList<>();
    }

    /* *****************************************************************************************************************
     * 													revoke
     * -----------------------------------------------------------------------------------------------------------------
     * boolean revoke(DataRuntime runtime, User user, Privilege ... privileges) throws Exception
     * boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception
     * boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     * List<Run> buildRevokeRun(DataRuntime runtime, User user, Privilege ... privileges) throws Exception
     * List<Run> buildRevokeRun(DataRuntime runtime, User user, Role ... roles) throws Exception
     * List<Run> buildRevokeRun(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception
     ******************************************************************************************************************/
	/**
	 * grant[调用入口]<br/>
	 * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	@Override
	public boolean revoke(DataRuntime runtime, User user, Privilege ... privileges) throws Exception {
		boolean result = false;
		String random = random(runtime);
		ACTION.Authorize action = ACTION.Authorize.REVOKE;
		List<Run> runs = buildRevokeRun(runtime, user, privileges);
		return execute(runtime, random, user, action, runs);
	}

    /**
     * grant[调用入口]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */
    @Override
    public boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception {
        boolean result = false;
        String random = random(runtime);
        ACTION.Authorize action = ACTION.Authorize.REVOKE;
        List<Run> runs = buildRevokeRun(runtime, user, roles);
        return execute(runtime, random, user, action, runs);
    }

    /**
     * grant[调用入口]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */
    @Override
    public boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception {
        boolean result = false;
        String random = random(runtime);
        ACTION.Authorize action = ACTION.Authorize.REVOKE;
        List<Run> runs = buildRevokeRun(runtime, role, privileges);
        return execute(runtime, random, role, action, runs);
    }

    /**
     * grant[命令合成]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param privileges 权限
     * @return List
     */
    @Override
    public List<Run> buildRevokeRun(DataRuntime runtime, User user, Privilege ... privileges) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildRevokeRun(DataRuntime runtime, User user, Privilege ... privileges)", 37));
        }
        return new ArrayList<>();
    }

    /**
     * grant[命令合成]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return List
     */
    @Override
    public List<Run> buildRevokeRun(DataRuntime runtime, User user, Role ... roles) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildRevokeRun(DataRuntime runtime, User user, Role ... roles)", 37));
        }
        return new ArrayList<>();
    }

    /**
     * grant[命令合成]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return List
     */
    @Override
    public List<Run> buildRevokeRun(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception {
        if(log.isDebugEnabled()) {
            log.debug(LogUtil.format("子类(" + this.getClass().getSimpleName() + ")未实现 buildRevokeRun(DataRuntime runtime, Role role, Privilege ... privileges)", 37));
        }
        return new ArrayList<>();
    }

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(DataRuntime runtime, Column column)
	 * boolean isNumberColumn(DataRuntime runtime, Column column)
	 * boolean isCharColumn(DataRuntime runtime, Column column)
	 * String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value)
	 * String type(String type)
	 * String type2class(String type)
	 *
	 * protected String string(List<String> keys, String key, ResultSet set, String def) throws Exception
	 * protected String string(List<String> keys, String key, ResultSet set) throws Exception
	 * protected Integer integer(List<String> keys, String key, ResultSet set, Integer def) throws Exception
	 * protected Boolean bool(List<String> keys, String key, ResultSet set, Boolean def) throws Exception
	 * protected Boolean bool(List<String> keys, String key, ResultSet set, int def) throws Exception
	 * protected Object value(List<String> keys, String key, ResultSet set, Object def) throws Exception
	 * protected Object value(List<String> keys, String key, ResultSet set) throws Exception
	 ******************************************************************************************************************/

    protected String getString(DataRow row, MetadataFieldRefer refer, String key, String def) {
        String result = null;
        String[] keys = refer.maps(key);
        if(null != keys && keys.length > 0) {
            result = row.getString(keys);
        }
        if(null == result) {
            result = def;
        }
        return result;
    }

    protected String getString(DataRow row, MetadataFieldRefer refer, String key) {
        return getString(row, refer, key, null);
    }
    protected Boolean getBoolean(DataRow row, MetadataFieldRefer refer, String key, Boolean def) {
        Boolean result = null;
        try{
            String[] keys = refer.maps(key);
            if(null != keys && keys.length > 0) {
                result = row.getBoolean(keys);
            }
        }catch (Exception ignore) {}
        if(null == result) {
            result = def;
        }
        return result;
    }
    protected Date getDate(DataRow row, MetadataFieldRefer refer, String key, Date def) {
        Date result = null;
        try{
            String[] keys = refer.maps(key);
            if(null != keys && keys.length > 0) {
                result = row.getDate(keys);
            }
        }catch (Exception ignore) {}
        if(null == result) {
            result = def;
        }
        return result;
    }

    protected Date getDate(DataRow row, MetadataFieldRefer refer, String key) {
        return getDate(row, refer, key, null);
    }
    protected Long getLong(DataRow row, MetadataFieldRefer refer, String key, Long def) {
        Long result = null;
        try{
            String[] keys = refer.maps(key);
            if(null != keys && keys.length > 0) {
                result = row.getLong(keys);
            }
        }catch (Exception ignore) {}
        if(null == result) {
            result = def;
        }
        return result;
    }

    protected Long getLong(DataRow row, MetadataFieldRefer refer, String key) {
        return getLong(row, refer, key, null);
    }
    protected Integer getInt(DataRow row, MetadataFieldRefer refer, String key, Integer def) {
        Integer result = null;
        try{
            String[] keys = refer.maps(key);
            if(null != keys && keys.length > 0) {
                result = row.getInt(keys);
            }
        }catch (Exception ignore) {}
        if(null == result) {
            result = def;
        }
        return result;
    }

    protected Integer getInt(DataRow row, MetadataFieldRefer refer, String key) {
        return getInt(row, refer, key, null);
    }
    protected Boolean matchBoolean(DataRow row, MetadataFieldRefer refer, String key, String value) {
        String[] cols = refer.maps(key);
        String[] vals = refer.maps(value);
        return matchBoolean(row, cols, vals);
    }

    /**
     * parse boolean
     * @param row 结果集
     * @param cols 检测的列
     * @param vals 匹配true的值S(只要一项匹配就返回true)
     * @return boolean
     */
    protected Boolean matchBoolean(DataRow row, String[] cols, String[] vals) {
        Boolean bol = null;
        if(null != cols) {
            for(String col:cols) {
                Object value = row.get(col);
                if(null == value) {
                    continue;
                }
                if(value instanceof Boolean) {
                    bol = (Boolean)value;
                }else if(null != vals) {
                    String str = value.toString();
                    for (String val : vals) {
                        if (str.matches(val)) {
                            bol = true;
                            break;
                        }
                    }
                }
                if(null != bol) {
                    break;
                }
            }
            if(null == bol) {
                bol = false;
            }
        }
        return bol;
    }

    /**
	 * 转换成相应数据库类型<br/>
	 * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型<br/>
	 * 同时解析长度、有效位数、精度<br/>
	 * 如有些数据库中用bigint有些数据库中有long
	 * @param meta 列
	 * @return 具体数据库中对应的数据类型
	 */
	@Override
	public TypeMetadata typeMetadata(DataRuntime runtime, Column meta) {
		TypeMetadata typeMetadata = meta.getTypeMetadata();
		if(null == typeMetadata || TypeMetadata.NONE == typeMetadata || meta.getParseLvl() < 2 || type() != meta.getDatabaseType()) {
            LinkedHashMap<String, TypeMetadata> alias = TypeMetadataHolder.gets(type());
			typeMetadata = TypeMetadata.parse(type(), meta, alias, spells);
			meta.setDatabaseType(type());
			meta.setParseLvl(2);
		}
		return typeMetadata;
	}
	/**
	 * 转换成相应数据库类型<br/>
	 * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型，如有些数据库中用bigint有些数据库中有long
	 * @param type 编码时输入的类型
	 * @return 具体数据库中对应的数据类型
	 */
	@Override
	public TypeMetadata typeMetadata(DataRuntime runtime, String type) {
		if(null == type) {
			return null;
		}
		Column tmp = new Column();
		tmp.setTypeName(type, false);
		return typeMetadata(runtime, tmp);
	}

	/**
	 * 检测针对表的主键生成器
	 * @param type 数据库类型
	 * @param table 表
	 * @return PrimaryGenerator
	 */
	protected PrimaryGenerator checkPrimaryGenerator(DatabaseType type, String table) {
		table = table.replace(getDelimiterFr(), "").replace(getDelimiterTo(), "");
		//针对当前表的生成器
		PrimaryGenerator generator = GeneratorConfig.get(table);
		if(null != generator) {
			if(generator != PrimaryGenerator.GENERATOR.AUTO) {
				return generator;
			}
		}
		//全局配置
		if(null == primaryGenerator) {
			if(null == primaryGenerator) {
				primaryGenerator = GeneratorConfig.get();
			}
			if(null == primaryGenerator) {
				//全局配置
				if (ConfigTable.PRIMARY_GENERATOR_SNOWFLAKE_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.SNOWFLAKE;
				} else if (ConfigTable.PRIMARY_GENERATOR_RANDOM_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.RANDOM;
				} else if (ConfigTable.PRIMARY_GENERATOR_UUID_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.UUID;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIME_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.TIME;
				} else if (ConfigTable.PRIMARY_GENERATOR_TIMESTAMP_ACTIVE) {
					primaryGenerator = PrimaryGenerator.GENERATOR.TIMESTAMP;
				}
			}
		}
		if(null != primaryGenerator) {
			return primaryGenerator;
		}else{
			return null;
		}
	}

	/**
	 * 数据类型拼写兼容
	 * @param name name
	 * @return spell
	 */
	public TypeMetadata spell(String name) {
		TypeMetadata typeMetadata = TypeMetadataHolder.get(type(), name.toUpperCase());
		if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {//拼写兼容  下划线空格兼容
			typeMetadata = TypeMetadataHolder.get(type(), spells.get(name.toUpperCase()));
		}
		return typeMetadata;
	}
	/**
	 * 合成完整名称
	 * @param meta 合成完整名称
	 * @return String
	 */
	public String name(Metadata meta) {
		StringBuilder builder = new StringBuilder();
		String catalog = meta.getCatalogName();
		String schema = meta.getSchemaName();
		String name = meta.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			builder.append(catalog).append(".");
		}
		if(!empty(schema)) {
			builder.append(schema).append(".");
		}
		builder.append(name);
		return builder.toString();
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param meta Metadata
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, Metadata meta) {
		checkName(runtime, null, meta);
		String catalog = meta.getCatalogName();
		String schema = meta.getSchemaName();
		String name = meta.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			delimiter(builder, catalog).append(".");
		}
		if(!empty(schema)) {
			delimiter(builder, schema).append(".");
		}
		delimiter(builder, name);
		return builder;
	}
	/**
	 * 拼接完整列名
	 * @param builder builder
	 * @param meta Column
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, Column meta) {
		if(null != meta && !meta.isEmpty()) {
			delimiter(builder, meta.getName());
		}
		return builder;
	}

    /**
     * 生成insert update 命令时 类型转换 如 ?::json
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder StringBuilder
     * @param value 值
     * @param column 数据类型
     * @param configs ConfigStore
     * @param placeholder 占位符
     * @param unicode 编码
     * @return Object
     */
    @Override
    public Object convert(DataRuntime runtime, StringBuilder builder, Object value, Column column, Boolean placeholder, Boolean unicode, ConfigStore configs) {
        if(placeholder){
            builder.append("?");
        }else{
            Object write = write(runtime, column, value, false, unicode);
            builder.append(write);
        }
        return value;
    }
	/**
	 * 拼接界定符
	 * @param builder StringBuilder
	 * @param src 原文
	 * @return StringBuilder
	 */
    public StringBuilder delimiter(StringBuilder builder, String src, boolean check) {
        return SQLUtil.delimiter(builder, src, getDelimiterFr(), getDelimiterTo(), check);
    }
    public StringBuilder delimiter(StringBuilder builder, String src) {
        return SQLUtil.delimiter(builder, src, getDelimiterFr(), getDelimiterTo());
    }
	/**
	 * 拼接界定符
	 * @param builder StringBuilder
	 * @param list 原文
	 * @return StringBuilder
	 */
	public StringBuilder delimiter(StringBuilder builder, List<String> list) {
		String fr = getDelimiterFr();
		String to = getDelimiterTo();
		boolean first = true;
		for(String item:list) {
			if(!first) {
				builder.append(", ");
			}
			first = false;
			SQLUtil.delimiter(builder, item, fr, to);
		}
		return builder;
	}

	/**
	 * 是否是boolean列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isBooleanColumn(DataRuntime runtime, Column column) {
		String clazz = column.getClassName();
		if(null != clazz) {
			clazz = clazz.toLowerCase();
			if(clazz.contains("boolean")) {
				return true;
			}
		}else{
			// 如果没有同步法数据库,直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type) {
				type = type.toLowerCase();
				if(type.equals("bit") || type.equals("bool")) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isNumberColumn(DataRuntime runtime, Column column) {
		String clazz = column.getClassName();
		if(null != clazz) {
			clazz = clazz.toLowerCase();
			if(
					clazz.startsWith("int")
							|| clazz.contains("integer")
							|| clazz.contains("long")
							|| clazz.contains("decimal")
							|| clazz.contains("float")
							|| clazz.contains("double")
							|| clazz.contains("timestamp")
							// || clazz.contains("bit")
							|| clazz.contains("short")
			) {
				return true;
			}
		}else{
			// 如果没有同步法数据库,直接生成column可能只设置了type Name
			String type = column.getTypeName();
			if(null != type) {
				type = type.toLowerCase();
				if(type.startsWith("int")
						||type.contains("float")
						||type.contains("double")
						||type.contains("short")
						||type.contains("long")
						||type.contains("decimal")
						||type.contains("numeric")
						||type.contains("timestamp")
				) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isCharColumn(DataRuntime runtime, Column column) {
		return !isNumberColumn(runtime, column) && !isBooleanColumn(runtime, column);
	}

	/**
	 * 内置函数
	 * @param column 列属性
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
		return null;
	}

	/**
	 * 写入数据库前类型转换<br/>
	 *
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	@Override
	public Object write(DataRuntime runtime, Column metadata, Object value, Boolean placeholder, Boolean unicode) {
		if(null == value || "NULL".equals(value)) {
			return null;
		}
        if(value instanceof VariableValue){
            return ((VariableValue)value).value();
        }
        if(value instanceof String){
            String str = value.toString();
            if(BasicUtil.checkEl(str)) {
                return str.substring(2, str.length() - 1);
            }
        }
		Object result = null;
		TypeMetadata columnType = null;
		DataWriter writer = null;
		boolean isArray = false;
		//根据元数据类型
		if(null != metadata) {
			isArray = metadata.isArray();
			//根据列类型
			columnType = metadata.getTypeMetadata();
			if(null != columnType) {
				writer = writer(columnType);
			}
			if(null == writer) {
				//根据列类型名称
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					writer = writer(typeName);
					if(null != columnType) {
						//类型名称 转 成标准类型
						writer = writer(typeMetadata(runtime, metadata));
					}
				}
			}
		}
		if(null == columnType || TypeMetadata.NONE == columnType) {
			//根据值的Java class
			columnType = typeMetadata(runtime, value.getClass().getSimpleName());
		}
		if(null != columnType && TypeMetadata.NONE != columnType) {
			Class writeClass = columnType.compatible();
			if(null != writeClass) {
				value = ConvertProxy.convert(value, writeClass, isArray);
			}
		}

		if(null != columnType && TypeMetadata.NONE != columnType) {//根据列类型定位writer
			writer = writer(columnType);
			if(null == writer) {
				writer = writer(columnType.getCategory());
			}
		}
		if(null == writer && null != value) {//根据值类型定位writer
			writer = writer(value.getClass());
		}
		if(null != writer) {
			result = writer.write(value, placeholder, unicode, columnType);
		}
		if(null != result) {
            return write(runtime, result, placeholder, unicode);
		}
		if(null != columnType && TypeMetadata.NONE != columnType) {
			result = columnType.write(value, null, false);
		}
		if(null != result) {
            return write(runtime, result, placeholder, unicode);
		}
		return write(runtime, value, placeholder, unicode);
	}
    private Object write(DataRuntime runtime, Object value, Boolean placeholder, Boolean unicode) {
        if(null == value) {
            return null;
        }
        Object result = null;
        if(!placeholder) {
            boolean isNumber = BasicUtil.isNumber(value);
            boolean isBoolean = BasicUtil.isBoolean(value);
            boolean isNull = null == value || "NULL".equals(value);
            boolean isFun = false;
            String str = value +"";
            if(!isNumber) {
                if(str.contains("(") || str.contains("'")) {
                    isFun = true;
                }
            }
            boolean isOrigin = isNumber || isBoolean || isNull || isFun || value instanceof VariableValue;
            if(value instanceof VariableValue){
                value = ((VariableValue)value).value();
            }
            if (isOrigin) {
                result = value;
            } else {
                result = "'" + value + "'";
            }
            if(unicode && result instanceof String && result.toString().startsWith("'")) {
                result = unicodeGuide(runtime) + result;
            }
        }else{
            if(value instanceof VariableValue) {
                result = ((VariableValue)value).value();
            }
        }
        return result;
    }

	/**
	 * 从数据库中读取数据<br/>
	 * 先由子类根据metadata.typeName(CHAR,INT)定位到具体的数据库类型ColumnType<br/>
	 * 如果定位成功由CoumnType根据class转换(class可不提供)<br/>
	 * 如果没有定位到ColumnType再根据className(String,BigDecimal)定位到JavaType<br/>
	 * 如果定位失败或转换失败(返回null)再由父类转换<br/>
	 * 如果没有提供metadata和class则根据value.class<br/>
	 * 常用类型jdbc可以自动转换直接返回就可以(一般子类DataType返回null父类原样返回)<br/>
	 * 不常用的如json/point/polygon/blob等转换成anyline对应的类型<br/>
	 *
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时应该指定属性class, DataRow赋值时可以通过JDBChandler指定class)
	 * @return Object
	 */
	@Override
	public Object read(DataRuntime runtime, Column metadata, Object value, Class clazz) {
		Object result = value;
		if(null == value) {
			return null;
		}
		DataReader reader = null;
		TypeMetadata ctype = null;
		if (null != metadata) {
			ctype = metadata.getTypeMetadata();
			if(null != ctype) {
				reader = reader(ctype);
			}
			if(null == reader) {
				String typeName = metadata.getTypeName();
				if (null != typeName) {
					reader = reader(typeName);
					if(null == reader) {
						reader = reader(typeMetadata(runtime, metadata));
					}
				}
			}
		}
		if(null == reader) {
			reader = reader(value.getClass());
		}
		if(null != reader) {
			result = reader.read(value);
		}
		if(null == reader || null == result) {
			if(null != ctype) {
				result = ctype.read(value, null, clazz);
			}
		}
		return result;
	}
	/**
	 * 在不检测数据库结构时才生效,否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param obj Object
	 * @param key 列名
	 */
	@Override
	public void value(DataRuntime runtime, StringBuilder builder, Object obj, String key) {
		Object value = null;
		if(obj instanceof DataRow) {
			value = ((DataRow)obj).get(key);
		}else {
			if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
				Field field = EntityAdapterProxy.field(obj.getClass(), key);
				value = BeanUtil.getFieldValue(obj, field);
			} else {
				value = BeanUtil.getFieldValue(obj, key, true);
			}
		}
		if(null != value) {
			if(value instanceof SQL_BUILD_IN_VALUE) {
				builder.append(value(runtime, null, (SQL_BUILD_IN_VALUE)value));
			}else {
				TypeMetadata type = typeMetadata(runtime, value.getClass().getName());
				if (null != type) {
					value = type.write(value, null, false);
				}
				builder.append(value);

			}
		}else{
			builder.append("null");
		}
	}
	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param table 表
	 * @param value  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue value) {
		boolean result = false;
		if(ConfigTable.IS_AUTO_CHECK_METADATA) {
			LinkedHashMap<String, Column> columns = columns(runtime,null, false, new Table(catalog, schema, table), false);
			result = convert(runtime, columns, value);
		}else{
			result = convert(runtime,(Column)null, value);
		}
		return result;
	}

	/**
	 * 设置参数值,主要根据数据类型格执行式化，如对象,list,map等插入json列
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @param compare 比较方式 默认 equal 多个值默认 in
	 * @param column 列
	 * @param value value
	 */
	@Override
	public void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value) {
		boolean split = ConfigTable.IS_AUTO_SPLIT_ARRAY;
		if(ConfigTable.IS_AUTO_CHECK_METADATA) {
			String type = null;
			if(null != column) {
				type = column.getTypeName();
				if(null == type && BasicUtil.isNotEmpty(run.getTable())) {
					LinkedHashMap<String,Column> columns = columns(runtime,null, false, run.getTable(), false);
					column = columns.get(column.getName().toUpperCase());
					if(null != column) {
						type = column.getTypeName();
					}
				}
			}
		}
		run.addValues(compare, column, value, split);
		if(null != column) {
			//value = convert(runtime, column, rv); //统一调用
		}
	}

	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param configs ConfigStore
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, ConfigStore configs, Run run) {
		boolean result = false;
		if(null != run) {
			result = convert(runtime, run.getTable(), run);
		}
		return result;
	}

	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table Table
	 * @param run  run
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, Table table, Run run) {
		boolean result = false;
		if(null != table) {
			LinkedHashMap<String, Column> columns = table.getColumns();

			if (ConfigTable.IS_AUTO_CHECK_METADATA) {
				//if (null == columns || columns.isEmpty()) {
				//有可能是通过class解析解析的columns以数据库为准
					columns = columns(runtime, null, false, table, false);
				//}
			}
			List<RunValue> values = run.getRunValues();
			if (null != values) {
				for (RunValue value : values) {
					if (ConfigTable.IS_AUTO_CHECK_METADATA) {
						result = convert(runtime, columns, value);
					} else {
						result = convert(runtime, (Column) null, value);
					}
				}
			}
		}
		return result;
	}

	/**
	 * 数据类型转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param columns 列
	 * @param value 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	@Override
	public boolean convert(DataRuntime runtime, Map<String,Column> columns, RunValue value) {
		boolean result = false;
		if(null != columns && null != value) {
			String key = value.getKey();
			if(null != key) {
				Column meta = columns.get(key.toUpperCase());
				result = convert(runtime, meta, value);
			}
		}
		return result;
	}

	/**
	 * 根据数据库列属性 类型转换(一般是在更新数据库时调用)
	 * 子类先解析(有些同名的类型以子类为准)、失败后再到这里解析
	 * @param metadata 列
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)Value
	 * @return boolean 是否完成类型转换,决定下一步是否继续
	 */
	@Override
	public boolean convert(DataRuntime runtime, Column metadata, RunValue run) {
		if(null == run) {
			return true;
		}
		Object value = run.getValue();
		if(null == value) {
			return true;
		}
		try {
			if(null != metadata) {
				//根据列属性转换(最终也是根据java类型转换)
				value = convert(runtime, metadata, value);
			}else {
                String datatype = run.datatype();
                TypeMetadata tm = null;
                if (null != datatype) {
                    LinkedHashMap<String, TypeMetadata> alias = TypeMetadataHolder.gets(type());
                    tm = alias.get(datatype.toUpperCase());
                    if (null == tm) {
                        log.warn("[类型检测失败][datatype:{}]", datatype);
                    }
                }
                if (null != tm) {
                    value = convert(runtime, tm, value);
                } else {
                    DataWriter writer = writer(value.getClass());
                    if (null != writer) {
                        value = writer.write(value, true, false,null);
                    }
                }
            }
			run.setValue(value);

		}catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata 列
	 * @param value 值
	 * @return Object
	 */
	@Override
	public Object convert(DataRuntime runtime, Column metadata, Object value) {
		if(null == value) {
			return value;
		}
		try {
			if(null != metadata) {
				TypeMetadata columnType = metadata.getTypeMetadata();
				if(null == columnType) {
					columnType = typeMetadata(runtime, metadata);
					if(null != columnType) {
						columnType.setArray(metadata.isArray());
						metadata.setTypeMetadata(columnType);
					}
				}
				value = convert(runtime, columnType, value);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 数据类型
	 * @param value 值
	 * @return Object
	 */
	@Override
	public Object convert(DataRuntime runtime, TypeMetadata meta, Object value) {
		if(null == meta) {
			return value;
		}
		String typeName = meta.getName();

		boolean parseJson = false;
		if(null != typeName && !(value instanceof String)) {
			if(typeName.contains("JSON")) {
				//对象转换成json string
				value = BeanUtil.object2json(value);
				parseJson = true;
			}else if(typeName.contains("XML")) {
				value = BeanUtil.object2xml(value);
				parseJson = true;
			}
		}
		if(!parseJson) {
			if (null != meta) {
				DataWriter writer = writer(meta);
				if(null == writer) {
					writer = writer(meta.getCategory());
				}
				if(null != writer) {
					value = writer.write(value, true, false, meta);
				}else {
					Class transfer = meta.transfer();
					Class compatible = meta.compatible();
					if (null != transfer) {
						value = ConvertProxy.convert(value, transfer, meta.isArray());
					}
					if (null != compatible) {
						value = ConvertProxy.convert(value, compatible, meta.isArray());
					}
				}
			}
		}
		return value;
	}

	/**
	 * 对象名称格式化(大小写转换)，在查询系统表时需要
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name name
	 * @return String
	 */
	@Override
	public String objectName(DataRuntime runtime, String name) {
		KeyAdapter.KEY_CASE keyCase = type().nameCase();
		if(null != keyCase) {
			return keyCase.convert(name);
		}
		return name;
	}

	/**
	 *
	 * 根据 catalog, schema, name检测tables集合中是否存在
	 * @param list metas
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param name name
	 * @return 如果存在则返回Table 不存在则返回null
	 * @param <T> Table
	 */
	@Override
	public <T extends Metadata> T search(List<T> list, Catalog catalog, Schema schema, String name) {
		if(null != list) {
			for(T meta:list) {
				if(equals(catalog, meta.getCatalog())
						&& equals(schema, meta.getSchema())
						&& BasicUtil.equalsIgnoreCase(meta.getName(),name)
				) {
					return meta;
				}
			}
		}
		return Metadata.match(list, catalog, schema, name);
	}

	public <T extends Metadata> T search(List<T> list, String catalog, String schema, String name) {
		return Metadata.match(list, catalog, schema, name);
	}

	public <T extends Metadata> T search(List<T> list, String catalog, String name) {
		return Metadata.match(list, catalog, name);
	}

	public <T extends Metadata> T search(List<T> list, String name) {
		return Metadata.match(list, name);
	}

	//A.ID,A.COOE,A.NAME
	protected String concat(String prefix, String split, List<String> columns) {
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(prefix)) {
			prefix = "";
		}else{
			if(!prefix.endsWith(".")) {
				prefix += ".";
			}
		}

		boolean first = true;
		for(String column:columns) {
			if(!first) {
				builder.append(split);
			}
			first = false;
			builder.append(prefix);
			//.append(column);
			delimiter(builder, column);
		}
		return builder.toString();
	}
	//master.column = data.column
	protected String concatEqual(String master, String data, String split, List<String> columns) {
		StringBuilder builder = new StringBuilder();
		if(BasicUtil.isEmpty(master)) {
			master = "";
		}else{
			if(!master.endsWith(".")) {
				master += ".";
			}
		}
		if(BasicUtil.isEmpty(data)) {
			data = "";
		}else{
			if(!data.endsWith(".")) {
				data += ".";
			}
		}

		boolean first = true;
		for(String column:columns) {
			if(!first) {
				builder.append(split);
			}
			first = false;
			builder.append(master);
            delimiter(builder, column);
            builder.append(" = ");
            builder.append(data);
            delimiter(builder, column);
		}
		return builder.toString();
	}

	protected String random(DataRuntime runtime) {
		StringBuilder builder = new StringBuilder();
		builder.append("[cmd:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
			.append("][thread:")
			.append(Thread.currentThread().getId()).append("][ds:").append(runtime.datasource()).append("]");
		return builder.toString();
	}

}