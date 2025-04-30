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

package org.anyline.data.adapter;

import org.anyline.adapter.DataReader;
import org.anyline.adapter.DataWriter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.authorize.Privilege;
import org.anyline.entity.authorize.Role;
import org.anyline.entity.authorize.User;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.NotSupportException;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.*;
import org.anyline.metadata.differ.*;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.metadata.type.DatabaseOrigin;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.*;

/**
 * DriverAdapter主要用来构造和执行不同数据库的命令,一般会分成3步,以insert为例<br/>
 * 1.insert[调用入口]<br/>提供为上一步调用的方法,方法内部再调用[命令合成]<br/>生成具体命令，最后调用[命令执行]执行命令<br/>
 * 2.insert[命令合成]<br/>根据不同的数据库生成具体的insert命令<br/>
 * 3.insert[命令执行]执行[命令合成]<br/>生成的命令<br/>
 * 其中[调用入口]<br/>,[命令执行]大部分通用，重点是[命令合成]<br/>需要由每个数据库的适配器各自生成<br/>
 * [命令执行]过程注意数据库是否支持占位符，是否支持返回自增值，是否支持批量量插入<br/>
 * 以上3步在子类中要全部实现，如果不实现，需要输出日志或调用super方法(用于异常堆栈输出)<br/>
 */
public interface DriverAdapter {
    Log log = LogProxy.get(DriverAdapter.class);

    // 内置VALUE
     enum SQL_BUILD_IN_VALUE{
        CURRENT_DATETIME("CURRENT_DATETIME","当前日期时间"),
        CURRENT_DATE("CURRENT_DATE","当前日期"),
        CURRENT_TIME("CURRENT_TIME","当前时间"),
        CURRENT_TIMESTAMP("CURRENT_TIMESTAMP","当前时间戳");
        private final String code;
        private final String name;
        SQL_BUILD_IN_VALUE(String code, String name) {
            this.code = code;
            this.name = name;
        }
        String getCode() {
            return code;
        }
        String getName() {
            return name;
        }
    }

    /**
     * 数据库类型
     * @return DatabaseType
     */
    DatabaseType type();
    default String version() {
        return null;
    }
    default LinkedHashMap<String, TypeMetadata> types() {
        LinkedHashMap<String, TypeMetadata> types = new LinkedHashMap<>();
        for(TypeMetadata type:alias().values()) {
            types.put(type.getName().toUpperCase(), type);
        }
        return types;
    }
    default DatabaseOrigin origin() {
        return type().origin();
    }

    /**
     * 数据类型别名
     * @return LinkedHashMap
     */
    LinkedHashMap<String, TypeMetadata> alias();
    void setActuator(DriverActuator actuator);
    DriverActuator getActuator();
    boolean supportCatalog();
    boolean supportSchema();
    default boolean supportPlaceholder() {
        return true;
    }
    default String columnAliasGuidd() {
        return " ";
    }
    default String tableAliasGuidd() {
        return " ";
    }
    void setListener(DDListener listener);
    DDListener getDDListener();
    void setListener(DMListener listener);
    DMListener getDMListener();
    void setGenerator(PrimaryGenerator generator);
    void setDelimiter(String delimiter);

    /**
     * 根据catalog+schema+name 比较,过程中需要检测是否支持catalog,schema不支持的不判断
     * @param m1 Metadata
     * @param m2 Metadata
     * @return boolean
     */
    default boolean equals(Metadata m1, Metadata m2) {
        String c1 = null;
        String c2 = null;
        String s1 = null;
        String s2 = null;
        String n1 = null;
        String n2 = null;
        if(null != m1) {
            if(null == m2) {
                return false;
            }
            c1 = m1.getCatalogName();
            s1 = m1.getSchemaName();
            n1 = m1.getName();
        }
        if(null != m2) {
            if(null == m1) {
                return false;
            }
            c2 = m2.getCatalogName();
            s2 = m2.getSchemaName();
            n2 = m2.getName();
        }
        if(supportCatalog()) {
            if(!BasicUtil.equals(c1, c2, true)) {
                return false;
            }
        }
        if(supportSchema()) {
            if(!BasicUtil.equals(s1, s2, true)) {
                return false;
            }
        }
        return BasicUtil.equals(n1, n2, true);
    }
    default boolean empty(Metadata meta) {
        if(null == meta) {
            return true;
        }
        if(BasicUtil.isEmpty(meta.getName())) {
            return true;
        }
        return false;
    }
    default boolean empty(String meta) {
        return BasicUtil.isEmpty(meta);
    }
    default boolean equals(Catalog c1, Catalog c2) {
        if(!supportCatalog()) {
            //如果数据库不支持直接返回true
            return true;
        }
        String n1 = null;
        String n2 = null;
        if(null != c1) {
            if(null == c2) {
                return false;
            }
            n1 = c1.getName();
        }
        if(null != c2) {
            if(null == c1) {
                return false;
            }
            n2 = c2.getName();
        }
        return BasicUtil.equals(n1, n2, true);
    }
    default boolean equals(Schema s1, Schema s2) {
        //如果数据库不支持直接返回true
        if(!supportCatalog()) {
            return true;
        }
        String n1 = null;
        String n2 = null;
        if(null != s1) {
            if(null == s2) {
                return false;
            }
            n1 = s1.getName();
        }
        if(null != s2) {
            if(null == s1) {
                return false;
            }
            n2 = s2.getName();
        }
        return BasicUtil.equals(n1, n2, true);
    }

    /**
     * 注册数据类型别名(包含对应的标准类型、length/precision/scale等配置)
     * @param alias 数据类型别名
     * @return Config
     */
    TypeMetadata.Refer reg(TypeMetadataAlias alias);
    /**
     * 注册数据类型配置<br/>
     * 要从配置项中取出每个属性检测合并,不要整个覆盖<br/>
     * 数据类型 与 数据类型名称 的区别:如ORACLE_FLOAT,FLOAT 这两个对象的name都是float所以会相互覆盖
     * @param type 数据类型名称
     * @param refer 配置项
     * @return Config
     */
    TypeMetadata.Refer reg(String type, TypeMetadata.Refer refer);
    /**
     * 注册数据类型配置<br/>
     * 要从配置项中取出每个属性检测合并,不要整个覆盖<br/>
     * 数据类型 与 数据类型名称 的区别:如ORACLE_FLOAT,FLOAT 这两个对象的name都是float所以会相互覆盖
     * @param type 数据类型
     * @param refer 配置项
     * @return refer
     */
    TypeMetadata.Refer reg(TypeMetadata type, TypeMetadata.Refer refer);
    /**
     * 子类不要覆盖这个方法 用来实现子类跨多层父类直接调用当前方法
     * 验证运行环境与当前适配器是否匹配<br/>
     * 默认不连接只根据连接参数<br/>
     * 只有同一个库区分不同版本(如mssql2000/mssql2005)或不同模式(如KingBase的oracle/pg模式)时才需要单独实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
     * @return boolean
     */
    default boolean exeMatch(DataRuntime runtime, String feature, String adapterKey, boolean compensate) {
        if(BasicUtil.isNotEmpty(adapterKey)) {
            return matchByAdapter(adapterKey);
        }
        if(null == feature) {
            feature = runtime.getFeature(true);
        }
        //获取特征时会重新解析 adapter参数,因为有些数据源是通过DataSource对象注册的，这时需要打开连接后才能拿到url
        if(BasicUtil.isNotEmpty(runtime.getAdapterKey())) {
            return matchByAdapter(runtime);
        }

        List<String> keywords = type().keywords(); //关键字+jdbc-url前缀+驱动类
        return match(feature, keywords, compensate);
    }

    default boolean match(DataRuntime runtime, String feature, String adapterKey, boolean compensate) {
       return exeMatch(runtime, feature, adapterKey, compensate);
    }
    default boolean matchByAdapter(DataRuntime runtime) {
        String config_adapter_key = runtime.getAdapterKey();
        return matchByAdapter(config_adapter_key);
    }
    default boolean matchByAdapter(String key) {
        if(BasicUtil.isNotEmpty(key)) {
            String type = type().name();
            //如果明确指定了adapter 不考虑其他特征
            boolean result = false;
            if(key.equalsIgnoreCase(type)) {
                result = true;
            }
            if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
                log.info("[adapter match][result:{}][config adapter:{}][match adapter:{}]", result, key, type);
            }
            return result;
        }
        return false;
    }

    /**
     *
     * @param feature 当前运行环境特征
     * @param keywords 关键字+jdbc-url前缀+驱动类
     * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
     * @return 数据源特征中包含上以任何一项都可以通过
     */
    default boolean match(String feature, List<String> keywords, boolean compensate) {
        if(null == feature) {
            return false;
        }
        feature = feature.toLowerCase();
        if(null != keywords) {
            for (String k:keywords) {
                if(BasicUtil.isEmpty(k)) {
                    if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
                        log.info("[adapter match][result:{}][feature:{}][key:{}][match adapter:{}]", false, feature, k, this.getClass());
                    }
                    continue;
                }
                if(feature.contains(k)) {
                    if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
                        log.info("[adapter match][result:{}][feature:{}][key:{}][match adapter:{}]", true, feature, k, this.getClass());
                    }
                    return true;
                }
            }
        }
        return false;
    }

    String TAB         = "\t"        ;
    String BR         = "\n"        ;
    String BR_TAB     = "\n\t"    ;

    /**
     * 界定符(分隔符)
     * @return String
     */
    String getDelimiterFr();
    String getDelimiterTo();

    /**
     * 对应的兼容模式，有些数据库会兼容oracle或pg,需要分别提供两个JDBCAdapter或者直接依赖oracle/pg的adapter
     * 参考DefaultJDBCAdapterUtil定位adapter的方法
     * @return DatabaseType
     */
    DatabaseType compatible();

    /**
     * 转换成相应数据库类型<br/>
     * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型<br/>
     * 同时解析长度、有效位数、精度<br/>
     * 如有些数据库中用bigint有些数据库中有long
     * @param meta 列
     * @return 具体数据库中对应的数据类型
     */
    TypeMetadata typeMetadata(DataRuntime runtime, Column meta);

    /**
     * 转换成相应数据库类型<br/>
     * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型，如有些数据库中用bigint有些数据库中有long
     * @param type 编码时输入的类型(通常是java类)
     * @return 具体数据库中对应的数据类型
     */
    TypeMetadata typeMetadata(DataRuntime runtime, String type);
    /**
     * 写入数据库前 类型转换
     * @param supports 写入的原始类型 class ColumnType StringColumnType
     * @param writer DataWriter
     */
    default void reg(Object[] supports, DataWriter writer) {
        SystemDataWriterFactory.reg(type(), supports, writer);
    }

    /**
     * 写入数据库时 类型转换 写入的原始类型需要writer中实现supports
     * @param writer DataWriter
     */
    default void reg(DataWriter writer) {
        SystemDataWriterFactory.reg(type(), null, writer);
    }

    /**
     * 读取数据库入 类型转换
     * @param supports 读取的原始类型 class ColumnType StringColumnType
     * @param reader DataReader
     */
    default void reg(Object[] supports, DataReader reader) {
        SystemDataReaderFactory.reg(type(), supports, reader);
    }

    /**
     * 读取数据库入 类型转换 读取的原始类型需要reader中实现supports
     * @param reader DataReader
     */
    default void reg(DataReader reader) {
        SystemDataReaderFactory.reg(type(), null, reader);
    }

    /**
     * 根据读出的数据类型 定位DataReader
     * @param type class ColumnType StringColumnType
     * @return DataReader
     */
    default DataReader reader(Object type) {
        DataReader reader = DataReaderFactory.reader(type(), type);
        if(null == reader) {
            reader = SystemDataReaderFactory.reader(type(), type);
        }
        if(null == reader) {
            reader = DataReaderFactory.reader(DatabaseType.NONE, type);
        }
        if(null == reader) {
            reader = SystemDataReaderFactory.reader(DatabaseType.NONE, type);
        }
        return reader;
    }

    /**
     * 根据写入的数据类型 定位DataWriter,只根据输入类型，输出类型在writer中判断
     * @param type class(String.class) TypeMetadata,TypeMetadata.CATEGORY, StringColumnType("VARCHAR2")
     * @return DataWriter
     */
    default DataWriter writer(Object type) {
        DataWriter writer = DataWriterFactory.writer(type(), type);
        if(null == writer) {
            writer = SystemDataWriterFactory.writer(type(), type);
        }
        if(null == writer) {
            writer = DataWriterFactory.writer(DatabaseType.NONE, type);
        }
        if(null == writer) {
            writer = SystemDataWriterFactory.writer(DatabaseType.NONE, type);
        }
        return writer;
    }
    default String name(Type type) {
        Map<Type, String> types = new HashMap<>();
        types.put(Table.TYPE.NORMAL, "BASE TABLE");
        types.put(Table.TYPE.VIEW, "VIEW");
        types.put(View.TYPE.NORMAL, "VIEW");
        types.put(Metadata.TYPE.TABLE, "BASE TABLE");
        types.put(Metadata.TYPE.VIEW, "VIEW");
        return types.get(type);
    }
    default List<String> names(List<Type> types) {
        List<String> list = new ArrayList<>();
        for(Type type:types) {
            String name = name(type);
            if(null != name) {
                list.add(name);
            }
        }
        return list;
    }

    default void in(DataRuntime runtime, StringBuilder builder, String column, List<String> list) {
        if(!list.isEmpty()) {
            builder.append(" AND ").append(column);
            if(list.size() == 1) {
                builder.append(" = '").append(objectName(runtime, list.get(0))).append("'");
            }else{
                boolean first = true;
                builder.append(" IN(");
                for(String item:list) {
                    if(!first) {
                        builder.append(", ");
                    }
                    builder.append("'").append(objectName(runtime, item)).append("'");
                    first = false;
                }
                builder.append(")");
            }
        }
    }

    /**
     * 合关DDL片段
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @param slices slices
     * @return list
     */
    List<Run> merge(DataRuntime runtime, Table meta, List<Run> slices);
    default List<Run> ddl(DataRuntime runtime, String random, MetadataDiffer differ) {
        return ddl(runtime, random, differ, true);
    }

    /**
     * 根据差异生成SQL
     * @param differ differ 需要保证表中有列信息
     * @return runs
     */
    default List<Run> ddl(DataRuntime runtime, String random, MetadataDiffer differ, boolean merge) {
        List<Run> list = new ArrayList<>();
        if(differ instanceof TablesDiffer) {
            TablesDiffer df = (TablesDiffer) differ;
            LinkedHashMap<String, Table> adds = df.getAdds();
            LinkedHashMap<String, Table> drops = df.getDrops();
            LinkedHashMap<String, Table> updates = df.getAlters();//只统计哪些表需要修改
            LinkedHashMap<String, TableDiffer> diffs = df.getDiffers();//标记具体需要修改的内容
            //添加表
            for(Table add:adds.values()) {
                try {
                    list.addAll(buildCreateRun(runtime, add));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            //修改表
            for(TableDiffer dif:diffs.values()) {
                list.addAll(ddl(runtime, random, dif, merge));
            }
            //删除表
            for(Table drop:drops.values()) {
                try {
                    list.addAll(buildDropRun(runtime, drop));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
        }else if(differ instanceof ViewsDiffer) {
            ViewsDiffer df = (ViewsDiffer) differ;
            LinkedHashMap<String, View> adds = df.getAdds();
            LinkedHashMap<String, View> drops = df.getDrops();
            LinkedHashMap<String, View> updates = df.getAlters();
            for(View add:adds.values()) {
                try {
                    list.addAll(buildCreateRun(runtime, add));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(View update:updates.values()) {
                try {
                    list.addAll(buildAlterRun(runtime, update));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(View drop:drops.values()) {
                try {
                    list.addAll(buildDropRun(runtime, drop));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
        }else if(differ instanceof TableDiffer) {
            try {
                TableDiffer dif = (TableDiffer) differ;
                List<Run> slices = new ArrayList<>();
                Table direct = dif.getDirect();
                Table dest = dif.getDest();
                Table origin = dif.getOrigin();
                Table update = origin.clone();
                if(null != update) {
                    update.setUpdate(dest, false, false);
                }
                ColumnsDiffer columns_dif = dif.getColumnsDiffer();
                slices.addAll(ddl(runtime, random, columns_dif, false));

                PrimaryKeyDiffer primary_dif = dif.getPrimaryKeyDiffer();
                slices.addAll(ddl(runtime, random, primary_dif, false));

                IndexesDiffer index_dif = dif.getIndexesDiffer();
                slices.addAll(ddl(runtime, random, index_dif, false));

                if(merge) {
                    //a.compare(b) > alter b
                    List<Run> merges = merge(runtime, direct, slices);
                    list.addAll(merges);
                }else{
                    list.addAll(slices);
                }
            }catch (Exception e) {
                log.error("build ddl exception:", e);
            }
        }else if(differ instanceof PrimaryKeyDiffer) {
            PrimaryKeyDiffer df = (PrimaryKeyDiffer) differ;
            Table table = null;
            PrimaryKey add = df.getAdd();
            PrimaryKey drop = df.getDrop();
            PrimaryKey alter = df.getAlter();
            boolean slice = slice();
            List<Run> slices = new ArrayList<>();
            try{
                if(null != drop) {
                    if(null == table) {
                        table = drop.getTable();
                    }
                    slices.addAll(buildDropRun(runtime, drop, slice));
                }
                if(null != add) {
                    if(null == table) {
                        table = add.getTable();
                    }
                    slices.addAll(buildAddRun(runtime, add, slice));
                }
                if(null != alter) {
                    if(null == table) {
                        table = alter.getTable();
                    }
                    slices.addAll(buildAlterRun(runtime, alter, alter.getUpdate(), slice));
                }
                if(merge) {
                    list.addAll(merge(runtime, table, slices));
                }else{
                    list.addAll(slices);
                }
            }catch (Exception e) {
                log.error("build ddl exception:", e);
            }

        }else if(differ instanceof ColumnsDiffer) {
            boolean slice = slice();
            ColumnsDiffer df = (ColumnsDiffer) differ;
            LinkedHashMap<String, Column> adds = df.getAdds();
            LinkedHashMap<String, Column> drops = df.getDrops();
            LinkedHashMap<String, Column> updates = df.getAlters();
            Table direct = differ.getDirect();
            List<Run> slices = new ArrayList<>();
            for(Column add:adds.values()) {
                try {
                    if(null == direct) {
                        direct = add.getTable();
                    }
                    slices.addAll(buildAddRun(runtime, add, slice));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Column update:updates.values()) {
                try {
                    if(null == direct) {
                        direct = update.getTable();
                    }
                    slices.addAll(buildAlterRun(runtime, update, slice));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Column drop:drops.values()) {
                try {
                    if(null == direct) {
                        direct = drop.getTable();
                    }
                    slices.addAll(buildDropRun(runtime, drop, slice));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            if(merge) {
                list.addAll(merge(runtime, direct, slices));
            }else{
                list.addAll(slices);
            }
        }else if(differ instanceof IndexesDiffer) {
            IndexesDiffer df = (IndexesDiffer) differ;
            LinkedHashMap<String, Index> adds = df.getAdds();
            LinkedHashMap<String, Index> drops = df.getDrops();
            LinkedHashMap<String, Index> updates = df.getAlters();
            for(Index add:adds.values()) {
                try {
                    list.addAll(buildAddRun(runtime, add));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Index update:updates.values()) {
                try {
                    list.addAll(buildAlterRun(runtime, update));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Index drop:drops.values()) {
                try {
                    list.addAll(buildDropRun(runtime, drop));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
        }else if(differ instanceof FunctionsDiffer) {
            FunctionsDiffer df = (FunctionsDiffer) differ;
            List<Function> adds = df.getAdds();
            List<Function> drops = df.getDrops();
            List<Function> updates = df.getAlters();
            for(Function add:adds) {
                try {
                    list.addAll(buildCreateRun(runtime, add));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Function update:updates) {
                try {
                    list.addAll(buildAlterRun(runtime, update));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Function drop:drops) {
                try {
                    list.addAll(buildDropRun(runtime, drop));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
        }else if(differ instanceof ProceduresDiffer) {
            ProceduresDiffer df = (ProceduresDiffer) differ;
            LinkedHashMap<String, Procedure> adds = df.getAdds();
            LinkedHashMap<String, Procedure> drops = df.getDrops();
            LinkedHashMap<String, Procedure> updates = df.getUpdates();
            for(Procedure add:adds.values()) {
                try {
                    list.addAll(buildCreateRun(runtime, add));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Procedure update:updates.values()) {
                try {
                    list.addAll(buildAlterRun(runtime, update));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
            for(Procedure drop:drops.values()) {
                try {
                    list.addAll(buildDropRun(runtime, drop));
                }catch (Exception e) {
                    log.error("build ddl exception:", e);
                }
            }
        }
        return list;
    }

    /**
     * 根据差异生成SQL
     * @param differs differs
     * @return runs
     */
    default List<Run> ddl(DataRuntime runtime, String random, List<MetadataDiffer> differs) {
        List<Run> list = new ArrayList<>();
        for(MetadataDiffer differ:differs) {
            list.addAll(ddl(runtime, random, differ));
        }
        return list;
    }
    /* *****************************************************************************************************************
     *
     *                                                     DML
     *
     * =================================================================================================================
     * INSERT            : 插入
     * UPDATE            : 更新
     * SAVE                : 插入或更新
     * QUERY            : 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
     * EXISTS            : 是否存在
     * COUNT            : 统计
     * EXECUTE            : 执行(原生SQL及存储过程)
     * DELETE            : 删除
     * COMMON            ：其他通用
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     *                                                     INSERT
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
    long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
    default long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns) {
        return insert(runtime, random, batch, dest, data, null, columns);
    }
    default long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, String ... columns) {
        return insert(runtime, random, batch, dest, data, BeanUtil.array2list(columns));
    }
    default long insert(DataRuntime runtime, String random, int batch, Object data, String ... columns) {
        return insert(runtime, random, batch, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2list(columns));
    }
    default long insert(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
        return insert(runtime, random, 0, dest, data, columns);
    }
    default long insert(DataRuntime runtime, String random, Table dest, Object data, String ... columns) {
        return insert(runtime, random, dest, data, BeanUtil.array2list(columns));
    }
    default long insert(DataRuntime runtime, String random, Object data, String ... columns) {
        return insert(runtime, random, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2list(columns));
    }

    default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        return insert(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
    }
    default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, List<String> columns) {
        return insert(runtime, random, batch, dest, data, null, columns);
    }
    default long insert(DataRuntime runtime, String random, int batch, String dest, Object data, String ... columns) {
        return insert(runtime, random, batch, dest, data, BeanUtil.array2list(columns));
    }
    default long insert(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
        return insert(runtime, random, 0, dest, data, columns);
    }
    default long insert(DataRuntime runtime, String random, String dest, Object data, String ... columns) {
        return insert(runtime, random, dest, data, BeanUtil.array2list(columns));
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
    long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions);

    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param prepare 查询
     * @param configs 过滤条件及相关配置
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... conditions);
    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 需要插入的数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns);
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildInsertRun(runtime, batch, dest, obj, null, placeholder, unicode, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore confgis, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, confgis, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, null), obj, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, configs), obj, configs, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, configs), obj, configs, placeholder, unicode, BeanUtil.array2list(columns));
    }

    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, placeholder, unicode, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildInsertRun(runtime, batch, dest, obj, null, placeholder, unicode, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore confgis, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, confgis, placeholder, unicode, BeanUtil.array2list(columns));
    }
    
    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns);
    default void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, list, null, placeholder, unicode, columns);
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
    void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns);
    default void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, set, null, placeholder, unicode, columns);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, DataSourceUtil.parseDest(dest, list, configs), list, configs, placeholder, unicode, columns);
    }
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, list, null, placeholder, unicode, columns);
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
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, DataSourceUtil.parseDest(dest, set, configs), set, configs, placeholder, unicode, columns);
    }
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, set, null, placeholder, unicode, columns);
    }




    default Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        return buildInsertRun(runtime, dest, prepare, configs, obj, true, true, conditions);
    }
    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 需要插入的数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return buildInsertRun(runtime, batch, dest, obj, configs, true, true, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, List<String> columns) {
        return buildInsertRun(runtime, batch, dest, obj, null, true, true, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, true, true, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore confgis, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, confgis, true, true, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Object obj, String ... columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, null), obj, true, true, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, Object obj, ConfigStore configs, String ... columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, configs), obj, configs, true, true, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, ConfigStore configs, Object obj, String ... columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(null, obj, configs), obj, configs, true, true, BeanUtil.array2list(columns));
    }

    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns) {
        return buildInsertRun(runtime, batch, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, true, true, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, List<String> columns) {
        return buildInsertRun(runtime, batch, dest, obj, null, true, true, columns);
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, true, true, BeanUtil.array2list(columns));
    }
    default Run buildInsertRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore confgis, String ... columns) {
        return buildInsertRun(runtime, batch, dest, obj, confgis, true, true, BeanUtil.array2list(columns));
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    default void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, list, configs, true, true, columns);
    }
    default void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, list, null, true, true, columns);
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
    default void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, set, configs, true, true, columns);
    }
    default void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, set, null, true, true, columns);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, DataSourceUtil.parseDest(dest, list, configs), list, configs, true, true, columns);
    }
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, list, null, true, true, columns);
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
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, DataSourceUtil.parseDest(dest, set, configs), set, configs, true, true, columns);
    }
    default void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, LinkedHashMap<String, Column> columns) {
        fillInsertContent(runtime, run, dest, set, null, true, true, columns);
    }

    /**
     * 插入子表前 检测并创建子表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表
     * @param configs ConfigStore
     */
    void fillInsertCreateTemplate(DataRuntime runtime, Run run, PartitionTable dest, ConfigStore configs);

    /**
     * insert [命令合成-子流程]<br/>
     * 确认需要插入的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data  Entity或DataRow
     * @param batch  是否批量
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
    LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object data, ConfigStore configs, List<String> columns, boolean batch);
    default LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object data, ConfigStore configs, List<String> columns, boolean batch) {
        return confirmInsertColumns(runtime, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns, batch);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 批量插入数据时,多行数据之间分隔符
     * @return String
     */
    String batchInsertSeparator();
    /**
     * insert [命令合成-子流程]<br/>
     * 插入数据时是否支持占位符
     * @return boolean
     */
    boolean supportInsertPlaceholder();
    /**
     * insert [命令合成-子流程]<br/>
     * 自增主键返回标识
     * @return String
     */
    String generatedKey();

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
    long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);

    /**
     * 是否支持返回自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs configs中也可能禁用返回
     * @return boolean
     */
    boolean supportKeyHolder(DataRuntime runtime, ConfigStore configs);

    /**
     * 自增主键值keys
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs configs中也可能禁用返回
     * @return keys
     */
    List<String> keyHolders(DataRuntime runtime, ConfigStore configs);
    /* *****************************************************************************************************************
     *                                                     UPDATE
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
    long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns);
    default long update(DataRuntime runtime, String random, int batch, Object data, ConfigStore configs, List<String> columns) {
        return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, configs), data, configs, columns);
    }
    default long update(DataRuntime runtime, String random, int batch, Table dest, Object data, List<String> columns) {
        return update(runtime, random, batch, dest, data, null, columns);
    }
    default long update(DataRuntime runtime, String random, int batch, Object data, List<String> columns) {
        return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, null), data, null, columns);
    }
    default long update(DataRuntime runtime, String random, int batch, Object data, ConfigStore configs) {
        return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, configs), data, configs);
    }
    default long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, String ... columns) {
        return update(runtime, random, batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    default long update(DataRuntime runtime, String random, int batch, Object data, ConfigStore configs, String ... columns) {
        return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, configs), data, configs, BeanUtil.array2list(columns));
    }
    default long update(DataRuntime runtime, String random, int batch, Table dest, Object data, String ... columns) {
        return update(runtime, random, batch, dest, data, BeanUtil.array2string(columns));
    }
    default long update(DataRuntime runtime, String random, int batch, Object data, String ... columns) {
        return update(runtime, random, batch, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2string(columns));
    }
    default long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return update(runtime, random, 0, dest, data, configs, columns);
    }
    default long update(DataRuntime runtime, String random, Object data, ConfigStore configs, List<String> columns) {
        return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, configs), data, configs, columns);
    }
    default long update(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
        return update(runtime, random, 0, dest, data, null, columns);
    }
    default long update(DataRuntime runtime, String random, Object data, List<String> columns) {
        return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, null), data, null, columns);
    }
    default long update(DataRuntime runtime, String random, Object data, ConfigStore configs) {
        return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, configs), data, configs);
    }
    default long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, String ... columns) {
        return update(runtime, random, 0, dest, data, configs, BeanUtil.array2list(columns));
    }
    default long update(DataRuntime runtime, String random, Object data, ConfigStore configs, String ... columns) {
        return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, configs), data, configs, BeanUtil.array2list(columns));
    }
    default long update(DataRuntime runtime, String random, Table dest, Object data, String ... columns) {
        return update(runtime, random, 0, dest, data, BeanUtil.array2string(columns));
    }
    default long update(DataRuntime runtime, String random, Object data, String ... columns) {
        return update(runtime, random, 0, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2string(columns));
    }

    default long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
        return update(runtime, random, batch, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
    }

    default long update(DataRuntime runtime, String random, int batch, String dest, Object data, List<String> columns) {
        return update(runtime, random, batch, dest, data, null, columns);
    }
    default long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, String ... columns) {
        return update(runtime, random, batch, dest, data, configs, BeanUtil.array2list(columns));
    }
    default long update(DataRuntime runtime, String random, int batch, String dest, Object data, String ... columns) {
        return update(runtime, random, batch, dest, data, BeanUtil.array2string(columns));
    }
    default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
        return update(runtime, random, 0, dest, data, configs, columns);
    }
    default long update(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
        return update(runtime, random, 0, dest, data, null, columns);
    }
    default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, String ... columns) {
        return update(runtime, random, 0, dest, data, configs, BeanUtil.array2list(columns));
    }
    default long update(DataRuntime runtime, String random, String dest, Object data, String ... columns) {
        return update(runtime, random, 0, dest, data, BeanUtil.array2string(columns));
    }

    /**
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 一般通过TableBuilder生成
     * @param data K-VariableValue 更新值key:需要更新的列 value:通常是关联表的列用VariableValue表示，也可以是常量
     * @return 影响行数
     */
    long update(DataRuntime runtime, String random, RunPrepare prepare, DataRow data, ConfigStore configs, String ... conditions);

    /**
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 一般通过TableBuilder生成
     * @param data K-VariableValue 更新值key:需要更新的列 value:通常是关联表的列用VariableValue表示，也可以是常量
     * @return 影响行数
     */
    Run buildUpdateRun(DataRuntime runtime, RunPrepare prepare, DataRow data, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions);

    /**
     *
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    void fillUpdateContent(DataRuntime runtime, TableRun run, StringBuilder builder, DataRow data, ConfigStore configs, Boolean placeholder, Boolean unicode);
    /**
     *
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    default void fillUpdateContent(DataRuntime runtime, TableRun run, DataRow data, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        fillUpdateContent(runtime, run, run.getBuilder(), data, configs, placeholder, unicode);
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
    Run buildUpdateRun(DataRuntime runtime, int btch, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns);

    /**
     *
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return Run
     */
    Run buildUpdateRunLimit(DataRuntime runtime, Run run);
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, 0, dest, obj, configs, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, configs), obj, configs, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, dest, obj, null, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, null), obj, null, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode,  String ... columns) {
        return buildUpdateRun(runtime, dest, obj, configs, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, configs), obj, configs, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildUpdateRun(runtime, dest, obj, null, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, null), obj, null, placeholder, unicode, BeanUtil.array2list(columns));
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
    Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns);

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
    Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns);

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
    Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns);

    default Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, batch, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, 0, dest, obj, configs, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, Boolean placeholder, Boolean unicode, List<String> columns) {
        return buildUpdateRun(runtime, dest, obj, null, placeholder, unicode, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildUpdateRun(runtime, dest, obj, configs, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildUpdateRun(runtime, dest, obj, null, placeholder, unicode, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return buildUpdateRunFromEntity(runtime, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, placeholder, unicode, columns);
    }

    default Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns) {
        return buildUpdateRunFromDataRow(runtime, DataSourceUtil.parseDest(dest, row, configs), row, configs, placeholder, unicode, columns);
    }

    default Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns) {
        return buildUpdateRunFromCollection(runtime, batch, DataSourceUtil.parseDest(dest, list, configs), list, configs, placeholder, unicode, columns);
    }

    /**
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 一般通过TableBuilder生成
     * @param data K-VariableValue 更新值key:需要更新的列 value:通常是关联表的列用VariableValue表示，也可以是常量
     * @return 影响行数
     */
    default Run buildUpdateRun(DataRuntime runtime, RunPrepare prepare, DataRow data, ConfigStore configs, String ... conditions){
        return buildUpdateRun(runtime, prepare, data, configs, true, true, conditions);
    }

    /**
     *
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    default void fillUpdateContent(DataRuntime runtime, TableRun run, StringBuilder builder, DataRow data, ConfigStore configs) {
        fillUpdateContent(runtime, run, builder, data, configs, true, true);
    }
    /**
     *
     * 多表关联更新
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    default void fillUpdateContent(DataRuntime runtime, TableRun run, DataRow data, ConfigStore configs) {
        fillUpdateContent(runtime, run, run.getBuilder(), data, configs, true, true);
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
    default Run buildUpdateRun(DataRuntime runtime, int btch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return buildUpdateRun(runtime, btch, dest, obj, configs, true, true, columns);
    }

    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return buildUpdateRun(runtime, 0, dest, obj, configs, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, List<String> columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, configs), obj, configs, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, List<String> columns) {
        return buildUpdateRun(runtime, dest, obj, null, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, List<String> columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, null), obj, null, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs,  String ... columns) {
        return buildUpdateRun(runtime, dest, obj, configs, true, true, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, ConfigStore configs, String ... columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, configs), obj, configs, true, true, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, Table dest, Object obj, String ... columns) {
        return buildUpdateRun(runtime, dest, obj, null, true, true, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, Object obj, String ... columns) {
        return buildUpdateRun(runtime, DataSourceUtil.parseDest(null, obj, null), obj, null, true, true, BeanUtil.array2list(columns));
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
    default Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return buildUpdateRunFromEntity(runtime, dest, obj, configs, true, true, columns);
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
    default Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns) {
        return buildUpdateRunFromDataRow(runtime, dest, row, configs, true, true, columns);
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
    default Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns) {
        return buildUpdateRunFromCollection(runtime, batch, dest, list, configs, true, true, columns);
    }

    default Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns) {
        return buildUpdateRun(runtime, batch, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns) {
        return buildUpdateRun(runtime, 0, dest, obj, configs, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, List<String> columns) {
        return buildUpdateRun(runtime, dest, obj, null, true, true, columns);
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, ConfigStore configs, String ... columns) {
        return buildUpdateRun(runtime, dest, obj, configs, true, true, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRun(DataRuntime runtime, String dest, Object obj, String ... columns) {
        return buildUpdateRun(runtime, dest, obj, null, true, true, BeanUtil.array2list(columns));
    }
    default Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return buildUpdateRunFromEntity(runtime, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, true, true, columns);
    }

    default Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns) {
        return buildUpdateRunFromDataRow(runtime, DataSourceUtil.parseDest(dest, row, configs), row, configs, true, true, columns);
    }

    default Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns) {
        return buildUpdateRunFromCollection(runtime, batch, DataSourceUtil.parseDest(dest, list, configs), list, configs, true, true, columns);
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
    LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns);

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
    LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns);
    default LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns) {
        return confirmUpdateColumns(runtime, DataSourceUtil.parseDest(dest, row, configs), row, configs, columns);
    }
    default LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns) {
        return confirmUpdateColumns(runtime, DataSourceUtil.parseDest(dest, obj, configs), obj, configs, columns);
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
    long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run);
    default long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run) {
        return update(runtime, random, DataSourceUtil.parseDest(dest, data, configs), data, configs, run);
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
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return 影响行数
     */
    long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns);
    default long save(DataRuntime runtime, String random, Table dest, Object data, List<String> columns) {
        return save(runtime, random, dest, data, null, columns);
    }

    default long save(DataRuntime runtime, String random, Object data, List<String> columns) {
        return save(runtime, random, DataSourceUtil.parseDest(null, data, null), data, columns);
    }
    default long save(DataRuntime runtime, String random, Table dest, Object data, String ... columns) {
        return save(runtime, random, dest, data, BeanUtil.array2list(columns));
    }
    default long save(DataRuntime runtime, String random, Object data, String ... columns) {
        return save(runtime, random, DataSourceUtil.parseDest(null, data, null), data, BeanUtil.array2list(columns));
    }

    default long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
        return save(runtime, random, DataSourceUtil.parseDest(dest, data, configs), data, configs, columns);
    }
    default long save(DataRuntime runtime, String random, String dest, Object data, List<String> columns) {
        return save(runtime, random, dest, data, null, columns);
    }
    default long save(DataRuntime runtime, String random, String dest, Object data, String ... columns) {
        return save(runtime, random, dest, data, BeanUtil.array2list(columns));
    }
    /* *****************************************************************************************************************
     *                                                     QUERY
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
    DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
    /**
     * query procedure [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param procedure 存储过程
     * @param navi 分页
     * @return DataSet
     */
    DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi);

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
    <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) ;
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
    List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
    /**
     * select[命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions);
    default Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return buildQueryRun(runtime, prepare, configs, true, true, conditions);
    }
    default Run initQueryRun(DataRuntime runtime, RunPrepare prepare) {
        Run run = prepare.build(runtime);
        if(null != run && null == run.action()) {
            run.action(ACTION.DML.SELECT);
        }
        return run;
    }
    default RunPrepare buildRunPrepare(DataRuntime runtime, String text) {
        return null;
    }

    /**
     * 解析文本中的占位符(包含主体和查询条件)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    void parsePlaceholder(DataRuntime runtime, Run run);

    /**
     * 是否支持SQL变量占位符扩展格式 :VAR,图数据库不要支持会与表冲突
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return boolean
     */
    default boolean supportSqlVarPlaceholderRegexExt(DataRuntime runtime) {
        return true;
    }

    default String compareFormula(DataRuntime runtime, Compare compare) {
        return compare.formula();
    }
    /**
     * select[命令合成]<br/>
     * 创建 select sequence 最终可执行命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param next  是否生成返回下一个序列 false:cur true:next
     * @param names 存储过程名称s
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names);
    /**
     * select[命令合成-子流程] <br/>
     * 中间过程有可能转换类型 如从TableRun转到TextRun
     * 填充 select 命令内容
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    Run fillQueryContent(DataRuntime runtime, StringBuilder builder,  Run run, Boolean placeholder, Boolean unicode);
    default Run fillQueryContent(DataRuntime runtime, StringBuilder builder,  Run run){
        return fillQueryContent(runtime, builder, run, true, true);
    }

    Run fillQueryContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode);
    default Run fillQueryContent(DataRuntime runtime, Run run) {
        return fillQueryContent(runtime, run, true, true);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 合成最终 select 命令 包含分页 排序
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    String mergeFinalQuery(DataRuntime runtime, Run run);
    default String orderNullSet(OrderStore orders) {
        return "";
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
    RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode);
    default RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value) {
        return createConditionLike(runtime, builder, compare, value, true, true);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造 [not] exists 查询条件
     * 如果不需要占位符 返回null  否则原样返回value
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param prepare RunPrepare
     * @return value 有占位符时返回占位值，没有占位符返回null
     */
    default List<RunValue> createConditionExists(DataRuntime runtime, StringBuilder builder, Compare compare, RunPrepare prepare, Boolean placeholder, Boolean unicode) {
        return null;
    }
    default List<RunValue> createConditionExists(DataRuntime runtime, StringBuilder builder, Compare compare, RunPrepare prepare) {
        return createConditionExists(runtime, builder, compare, prepare, true, true);
    }
    /**
     * select[命令合成-子流程] <br/>
     * 构造 FIND_IN_SET 查询条件
     * 如果不需要占位符 返回null  否则原样返回value
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param column 列
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param value value
     * @return value
     */
    default Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode) throws NotSupportException {
        throw new NotSupportException("不支持");
    }
    default Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value) throws NotSupportException {
        return createConditionFindInSet(runtime, builder, column, compare, value, true, true);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造 JSON_CONTAINS 查询条件
     * 如果不需要占位符 返回null  否则原样返回value
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param column 列
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param value value
     * @return value
     */
    default Object createConditionJsonContains(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode) throws NotSupportException {
        throw new NotSupportException("不支持");
    }
    default Object createConditionJsonContains(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value) throws NotSupportException {
        return createConditionJsonContains(runtime, builder, column, compare, value, true, true);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造 JSON_CONTAINS_PATH 查询条件
     * 如果不需要占位符 返回null  否则原样返回value
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param column 列
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param value value
     * @return value
     */
    default Object createConditionJsonContainsPath(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode) throws NotSupportException {
        throw new NotSupportException("不支持");
    }

    default Object createConditionJsonContainsPath(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value) throws NotSupportException {
        return createConditionJsonContainsPath(runtime, builder, column, compare, value, true, true);
    }
    /**
     * select[命令合成-子流程] <br/>
     * 构造 JSON_SEARCH 查询条件(默认 IS NOT NULL)
     * 如果不需要占位符 返回null  否则原样返回value
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param column 列
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param value value
     * @return value
     */
    default Object createConditionJsonSearch(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode) throws NotSupportException {
        throw new NotSupportException("不支持");
    }

    default Object createConditionJsonSearch(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value) throws NotSupportException {
        return createConditionJsonSearch(runtime, builder, column, compare, value, true, true);
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

    default List<RunValue> createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode) {
        return null;
    }
    default List<RunValue> createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value) {
        return createConditionIn(runtime, builder, compare, value, true, true);
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param system 系统表不检测列属性
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return DataSet
     */
    DataSet select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run);
    default DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run) {
        return select(runtime, random, system, new Table(table), configs, run);
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return maps
     */
    List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run);
    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return map
     */
    Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) ;

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param next 是否查下一个序列值
     * @param names 存储过程名称s
     * @return DataRow 保存序列查询结果 以存储过程name作为key
     */
    DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names);

    /**
     * select [命令执行-子流程]<br/>
     * JDBC执行完成后的结果处理
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param list JDBC执行返回的结果集
     * @return  maps
     */
    List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list);
    /* *****************************************************************************************************************
     *                                                     COUNT
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
    long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);
    /**
     * count [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return long
     */
    long count(DataRuntime runtime, String random, Run run);

 
    /**
     * 合成最终 select count 命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    String mergeFinalTotal(DataRuntime runtime, Run run);


    /**
     * 计算字符串在当前数据库中占用字节长度
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param cn 字符串
     * @param configs 过滤条件及相关配置
     * @return int
     */
    int length(DataRuntime runtime, String random, String cn, ConfigStore configs);

    /**
     * 计算字符串在当前数据库中占用字节长度
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param cn 字符串
     * @param configs 过滤条件及相关配置
     * @return Run
     */
    Run buildQueryLengthRun(DataRuntime runtime, String cn, ConfigStore configs);
    /* *****************************************************************************************************************
     *                                                     EXISTS
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
    boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions);

    /**
     * exists [命令合成]<br/>
     * 合成最终 exists 命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    String mergeFinalExists(DataRuntime runtime, Run run);

    /* *****************************************************************************************************************
     *                                                     EXECUTE
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
    long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) ;
    long execute(DataRuntime runtime, String random, List<RunPrepare> prepares, ConfigStore configs) ;

    long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values) ;
    long execute(DataRuntime runtime, String random, int batch, int vol, ConfigStore configs, RunPrepare prepare, Collection<Object> values) ;
    /**
     * procedure [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @param random  random
     * @return 影响行数
     */
    boolean execute(DataRuntime runtime, String random, Procedure procedure) ;
    /**
     * 创建执行SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions);
    default Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return buildExecuteRun(runtime, prepare, configs, true, true, conditions);
    }

    /**
     * 填充 execute 命令内容
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    void fillExecuteContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode);
    default void fillExecuteContent(DataRuntime runtime, Run run) {
        fillExecuteContent(runtime, run, true, true);
    }

    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    long execute(DataRuntime runtime, String random, ConfigStore configs, Run run) ;
    long execute(DataRuntime runtime, String random, ConfigStore configs, List<Run> runs) ;
    /* *****************************************************************************************************************
     *                                                     DELETE
     ******************************************************************************************************************/

    /**
     * delete [调用入口]<br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param column 列
     * @param values 列对应的值
     * @return 影响行数
     * @param <T> T
     */
    <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, Collection<T> values);
    default <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String column, Collection<T> values) {
        return deletes(runtime, random, batch, table, null, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, Table table, String column, Collection<T> values) {
        return deletes(runtime, random, 0, table, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, Table table, ConfigStore configs, String column, Collection<T> values) {
        return deletes(runtime, random, 0, table, configs, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, int batch, Table table, String column, T ... values) {
        return deletes(runtime, random, batch, table, column, BeanUtil.array2list(values));
    }
    default <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, T ... values) {
        return deletes(runtime, random, batch, table, configs, column, BeanUtil.array2list(values));
    }
    default <T> long deletes(DataRuntime runtime, String random, Table table, ConfigStore configs, String column, T ... values) {
        return deletes(runtime, random, 0, table, configs, column, BeanUtil.array2list(values));
    }
    default <T> long deletes(DataRuntime runtime, String random, Table table, String column, T ... values) {
        return deletes(runtime, random, 0, table, column, BeanUtil.array2list(values));
    }

    default <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, Collection<T> values) {
        return deletes(runtime, random, batch, new Table(table), configs, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, int batch, String table, String column, Collection<T> values) {
        return deletes(runtime, random, batch, table, null, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, String table, String column, Collection<T> values) {
        return deletes(runtime, random, 0, table, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, String table, ConfigStore configs, String column, Collection<T> values) {
        return deletes(runtime, random, 0, table, configs, column, values);
    }
    default <T> long deletes(DataRuntime runtime, String random, int batch, String table, String column, T ... values) {
        return deletes(runtime, random, batch, table, column, BeanUtil.array2list(values));
    }
    default <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String column, T ... values) {
        return deletes(runtime, random, batch, table, configs, column, BeanUtil.array2list(values));
    }
    default <T> long deletes(DataRuntime runtime, String random, String table, ConfigStore configs, String column, T ... values) {
        return deletes(runtime, random, 0, table, configs, column, BeanUtil.array2list(values));
    }
    default <T> long deletes(DataRuntime runtime, String random, String table, String column, T ... values) {
        return deletes(runtime, random, 0, table, column, BeanUtil.array2list(values));
    }

    /**
     * delete [调用入口]<br/>
     * <br/>
     * 合成 where k1 = v1 and k2 = v2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
     * @return 影响行数
     */
    long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, Object obj, String... columns);
    default long delete(DataRuntime runtime, String random, String table, ConfigStore configs, Object obj, String... columns) {
        return delete(runtime, random, new Table(table), configs, obj, columns);
    }

    /**
     * delete [调用入口]<br/>
     * <br/>
     * 根据configs和conditions过滤条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param configs 查询条件及相关设置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions);
    default long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions) {
        return delete(runtime, random, new Table(table), configs, conditions);
    }

    /**
     * truncate [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @return 1表示成功执行
     */
    long truncate(DataRuntime runtime, String random, Table table);
    default long truncate(DataRuntime runtime, String random, String table) {
        return truncate(runtime, random, new Table(table));
    }

    /**
     * delete[命令合成]<br/>
     * 合成 where k1 = v1 and k2 = v2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode,  String ... columns);
    default List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildDeleteRun(runtime, new Table(table), configs, obj, placeholder, unicode, columns);
    }

    default List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        return buildDeleteRun(runtime,  table, configs, null, placeholder, unicode,  null);
    }
    default List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        return buildDeleteRun(runtime,  new Table(table), configs, null, placeholder, unicode,  null);
    }

    /**
     * delete[命令合成]<br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param column 列
     * @param values values
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values);
    default List<Run> buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
        return buildDeleteRun(runtime, batch, new Table(table), configs, placeholder, unicode, column, values);
    }

    /**
     * truncate[命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    List<Run> buildTruncateRun(DataRuntime runtime, Table table);
    default List<Run> buildTruncateRun(DataRuntime runtime, String table) {
        return buildTruncateRun(runtime, new Table(table));
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
    List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values);
    default List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
        return buildDeleteRunFromTable(runtime, batch, new Table(table), configs, placeholder, unicode, column, values);
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
    List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns);
    default List<Run> buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return buildDeleteRunFromEntity(runtime, new Table(table), configs, obj, placeholder, unicode, columns);
    }

    default List<Run> buildDeleteRunFromConfig(DataRuntime runtime, ConfigStore configs, Boolean placeholder, Boolean unicode) {
        Table table = configs.table();
        if(null!= table && BasicUtil.isNotEmpty(table.getName())) {
            return buildDeleteRunFromTable(runtime, 1, table, configs, placeholder, unicode, null, null);
        }
        return null;
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    void fillDeleteRunContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode);


    /**
     * delete[命令合成]<br/>
     * 合成 where k1 = v1 and k2 = v2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    default List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Object obj,  String ... columns) {
        return buildDeleteRun(runtime, table, configs, obj, true, true, columns);
    }
    default List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns) {
        return buildDeleteRun(runtime, new Table(table), configs, obj, true, true, columns);
    }

    default List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs) {
        return buildDeleteRun(runtime,  table, configs, null, true, true,  null);
    }
    default List<Run> buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs) {
        return buildDeleteRun(runtime,  new Table(table), configs, null, true, true,  null);
    }

    /**
     * delete[命令合成]<br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param column 列
     * @param values values
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    default List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values) {
        return buildDeleteRun(runtime, batch, table, configs, true, true, column, values);
    }
    default List<Run> buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String column, Object values) {
        return buildDeleteRun(runtime, batch, new Table(table), configs, true, true, column, values);
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
    default List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values) {
        return buildDeleteRunFromTable(runtime, batch, table, configs, true, true, column, values);
    }
    default List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs, String column, Object values) {
        return buildDeleteRunFromTable(runtime, batch, new Table(table), configs, true, true, column, values);
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
    default List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns) {
        return buildDeleteRunFromEntity(runtime, table, configs, obj, true, true, columns);
    }
    default List<Run> buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns) {
        return buildDeleteRunFromEntity(runtime, new Table(table), configs, obj, true, true, columns);
    }

    default List<Run> buildDeleteRunFromConfig(DataRuntime runtime, ConfigStore configs) {
        return buildDeleteRunFromConfig(runtime, configs, true, true);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    default void fillDeleteRunContent(DataRuntime runtime, Run run) {
        fillDeleteRunContent(runtime, run, true, true);
    }
    /**
     *
     * delete[命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param configs 查询条件及相关设置
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    long delete(DataRuntime runtime, String random, ConfigStore configs, Run run);
    /* *****************************************************************************************************************
     *
     *                                                     metadata
     *
     * =================================================================================================================
     * database            : 数据库(catalog, schema)
     * table            : 表
     * master table        : 主表
     * partition table    : 分区表
     * column            : 列
     * tag                : 标签
     * primary key      : 主键
     * foreign key        : 外键
     * index            : 索引
     * constraint        : 约束
     * trigger            : 触发器
     * procedure        : 存储过程
     * function         : 函数
     ******************************************************************************************************************/

    /**
     * 元数据[结构集封装-依据]<br/>
     * 读取元数据结果集的依据(元数据属性与数据库列的对应关系)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param type Table Column等元数据类
     * @return MetadataRefer
     */
    MetadataFieldRefer refer(DataRuntime runtime, Class<?> type);
    void reg(MetadataFieldRefer refer);

    /**
     * 根据运行环境识别 catalog与schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Metadata
     * @param <T> Metadata
     */
    <T extends Metadata> void checkSchema(DataRuntime runtime, T meta);

    /**
     * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
     * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
     * @param meta Metadata
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param overrideMeta 如果meta中有值，是否覆盖
     * @param overrideRuntime 如果runtime中有值，是否覆盖，注意结果集中可能跨多个schema，所以一般不要覆盖runtime,从con获取的可以覆盖ResultSet中获取的不要覆盖
     * @param <T> Metadata
     */
    default <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta) {
        if(supportCatalog()) {
            if (overrideMeta || empty(meta.getCatalog())) {
                meta.setCatalog(catalog);
            }
            if (overrideRuntime || BasicUtil.isEmpty(runtime.getCatalog())) {
                if(ConfigTable.KEEP_ADAPTER == 1) {
                    runtime.setCatalog(catalog);
                }
            }
        }else{
            meta.setCatalog((Catalog) null);
            runtime.setCatalog(null);
        }
        if(supportSchema()) {
            if (overrideMeta || empty(meta.getSchema())) {
                meta.setSchema(schema);
            }
            if (overrideRuntime || BasicUtil.isEmpty(runtime.getSchema())) {
                if(ConfigTable.KEEP_ADAPTER == 1) {
                    runtime.setSchema(schema);
                }
            }
        }else{
            meta.setSchema((Schema) null);
            runtime.setSchema(null);
        }
    }

    /**
     * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
     * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
     * @param meta Metadata
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param <T> Metadata
     */
    default <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema) {
        correctSchemaFromJDBC(runtime, meta, catalog, schema, false, true);
    }

    /**
     * 在调用jdbc接口前处理业务中的catalog,schema,部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @return String[]
     */
    default String[] correctSchemaFromJDBC(String catalog, String schema) {
        return new String[]{catalog, schema};
    }

    /**
     * 根据结果集对象获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param comment 是否需要查询列注释
     * @return LinkedHashMap
     */
    LinkedHashMap<String,Column> metadata(DataRuntime runtime, RunPrepare prepare, boolean comment);

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
    <T extends Metadata> T checkName(DataRuntime runtime, String random, T meta) throws Exception;
    /* *****************************************************************************************************************
     *                                                     database
     ******************************************************************************************************************/

    /**
     * database[调用入口]<br/>
     * 当前数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return Database
     */
    Database database(DataRuntime runtime, String random);
    /**
     * database[调用入口]<br/>
     * 当前数据源 数据库描述(产品名称+版本号)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return String
     */
    String product(DataRuntime runtime, String random);

    /**
     * database[调用入口]<br/>
     * 当前数据源 数据库版本 版本号比较复杂 不是全数字
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return String
     */
    String version(DataRuntime runtime, String random);

    /**
     * database[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @return LinkedHashMap
     */
    <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, Database query);
    default <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, String pattern) {
        return databases(runtime, random, new Database(pattern));
    }
    <T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, Database query);
    default <T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, String pattern) {
        return databases(runtime, random, greedy, new Database(pattern));
    }
    default Database database(DataRuntime runtime, String random, String pattern) {
        List<Database> databases = databases(runtime, random, false, pattern);
        if(!databases.isEmpty()) {
            return databases.get(0);
        }
        return null;
    }

    /**
     * database[命令合成]<br/>
     * 查询当前数据源 数据库产品说明(产品名称+版本号)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQueryProductRun(DataRuntime runtime) throws Exception;
    /**
     * database[命令合成]<br/>
     * 查询当前数据源 数据库版本 版本号比较复杂 不是全数字
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQueryVersionRun(DataRuntime runtime) throws Exception;
    /**
     * database[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, Database query) throws Exception;
    default List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String pattern) throws Exception {
        return buildQueryDatabasesRun(runtime, greedy, new Database(pattern));
    }
    default List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy) throws Exception {
        return buildQueryDatabasesRun(runtime, false, new Database());
    }
    default List<Run> buildQueryDatabaseRun(DataRuntime runtime, String pattern) throws Exception {
        return buildQueryDatabasesRun(runtime, false, pattern);
    }
    default List<Run> buildQueryDatabaseRun(DataRuntime runtime) throws Exception {
        return buildQueryDatabasesRun(runtime, false, new Database());
    }

    /**
     * database[结果集封装]<br/>
     * database 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initDatabaseFieldRefer();
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
    String product(DataRuntime runtime, int index, boolean create, String product, DataSet set);
    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 product
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param product 上一步查询结果
     * @return product
     * @throws Exception 异常
     */
    String product(DataRuntime runtime, boolean create, String product);
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
    String version(DataRuntime runtime, int index, boolean create, String version, DataSet set);
    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 version
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param version 上一步查询结果
     * @return version
     * @throws Exception 异常
     */
    String version(DataRuntime runtime, boolean create, String version);
    /**
     * database[结果集封装]<br/>
     * 根据查询结果集构造 Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Database query, DataSet set) throws Exception;
    <T extends Database> List<T> databases(DataRuntime runtime, int index, boolean create, List<T> previous, Database query, DataSet set) throws Exception;

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
    Database database(DataRuntime runtime, int index, boolean create, Database meta, DataSet set) throws Exception;
    /**
     * database[结果集封装]<br/>
     * 当前database 根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param meta 上一步查询结果
     * @return database
     * @throws Exception 异常
     */
    Database database(DataRuntime runtime, boolean create, Database meta) throws Exception;
    /**
     * schema[结果集封装]<br/>
     * 根据查询结果封装 schema 对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Database
     */
    <T extends Database> T init(DataRuntime runtime, int index, T meta, Database query, DataRow row);

    /**
     * database[结果集封装]<br/>
     * 根据查询结果封装 database 对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    <T extends Database> T detail(DataRuntime runtime, int index, T meta, Database query, DataRow row);

    /* *****************************************************************************************************************
     *                                                     catalog
     ******************************************************************************************************************/
    /**
     * catalog[调用入口]<br/>
     * 当前catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return Catalog
     */
    Catalog catalog(DataRuntime runtime, String random);
    /**
     * catalog[调用入口]<br/>
     * 全部catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @return LinkedHashMap
     */
    <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, Catalog query);
    default <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, String name) {
        return catalogs(runtime, random, new Catalog(name));
    }
    <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, Catalog query);
    default <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, String name) {
        return catalogs(runtime, random, greedy, new Catalog(name));
    }
    default Catalog catalog(DataRuntime runtime, String random, String name) {
        List<Catalog> catalogs = catalogs(runtime, random, false, name);
        if(!catalogs.isEmpty()) {
            return catalogs.get(0);
        }
        return null;
    }

    /**
     * catalog[命令合成]<br/>
     * 查询当前catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQueryCatalogRun(DataRuntime runtime, String random) throws Exception;
    /**
     * catalog[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, Catalog query) throws Exception;
    /**
     * catalog[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param name 名称统配符或正则
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @return runs
     * @throws Exception 异常
     */
    default List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
        return buildQueryCatalogsRun(runtime, greedy, new Catalog(name));
    }
    default List<Run> buildQueryCatalogsRun(DataRuntime runtime) throws Exception {
        return buildQueryCatalogsRun(runtime, false, new Catalog());
    }

    /**
     * Catalog[结果集封装]<br/>
     * Catalog 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initCatalogFieldRefer();
    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果集构造 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return catalogs
     * @throws Exception 异常
     */
    <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog query, DataSet set) throws Exception;
    <T extends Catalog> List<T> catalogs(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog query, DataSet set) throws Exception;

    /**
     * catalog[结果集封装]<br/>
     * 根据驱动内置接口补充 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @return catalogs
     * @throws Exception 异常
     */
    <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous) throws Exception;

    /**
     * catalog[结果集封装]<br/>
     * 根据驱动内置接口补充 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @return catalogs
     * @throws Exception 异常
     */
    <T extends Catalog> List<T> catalogs(DataRuntime runtime, boolean create, List<T> previous) throws Exception;

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
    Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog meta, DataSet set) throws Exception;
    /**
     * catalog[结果集封装]<br/>
     * 当前catalog 根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param meta 上一步查询结果
     * @return Catalog
     * @throws Exception 异常
     */
    Catalog catalog(DataRuntime runtime, boolean create, Catalog meta) throws Exception;

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果封装 catalog 对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Catalog
     */
    <T extends Catalog> T init(DataRuntime runtime, int index, T meta, Catalog query, DataRow row);

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果封装 catalog 对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    <T extends Catalog> T detail(DataRuntime runtime, int index, T meta, Catalog query, DataRow row);

    /* *****************************************************************************************************************
     *                                                     schema
     ******************************************************************************************************************/
    /**
     * schema[调用入口]<br/>
     * 当前schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return LinkedHashMap
     */
    Schema schema(DataRuntime runtime, String random);
    /**
     * schema[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @return LinkedHashMap
     */
    <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Schema query);
    /**
     * schema[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    default <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Catalog catalog, String name) {
        Schema query = new Schema(name);
        query.setCatalog(catalog);
        return schemas(runtime, random, query);
    }

    default <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, String name) {
        return schemas(runtime, random, null, name);
    }
    <T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, Schema query);
    default <T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name) {
        Schema query = new Schema(name);
        query.setCatalog(catalog);
        return schemas(runtime, random, greedy, query);
    }
    default <T extends Schema> List<T> schemas(DataRuntime runtime, String random, boolean greedy, String name) {
        return schemas(runtime, random, greedy, null, name);
    }
    default Schema schema(DataRuntime runtime, String random, Catalog catalog, String name) {
        List<Schema> schemas = schemas(runtime, random, false, catalog, name);
        if(!schemas.isEmpty()) {
            return schemas.get(0);
        }
        return null;
    }

    /**
     * schema[命令合成]<br/>
     * 查询当前schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQuerySchemaRun(DataRuntime runtime, String random) throws Exception;
    /**
     * schema[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @return runs
     * @throws Exception 异常
     */
    List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Schema query) throws Exception;
    /**
     * schema[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param name 名称统配符或正则
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @return runs
     * @throws Exception 异常
     */
    default List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception {
        Schema query = new Schema(name);
        query.setCatalog(catalog);
        return buildQuerySchemasRun(runtime, greedy, query);
    }
    default List<Run> buildQuerySchemasRun(DataRuntime runtime, String name) throws Exception {
        return buildQuerySchemasRun(runtime, false, null, name);
    }
    default List<Run> buildQuerySchemasRun(DataRuntime runtime, Catalog catalog) throws Exception {
        return buildQuerySchemasRun(runtime, false, catalog,null);
    }

    /**
     * Schema[结果集封装]<br/>
     * Schema 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initSchemaFieldRefer();
    /**
     * schema[结果集封装]<br/>
     * 根据查询结果集构造 schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return schemas
     * @throws Exception 异常
     */
    <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Schema query, DataSet set) throws Exception;

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果集构造 schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return schemas
     * @throws Exception 异常
     */
    default <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, DataSet set) throws Exception {
        Schema query = new Schema();
        query.setCatalog(catalog);
        return schemas(runtime, index, create, previous, query, set);
    }
    <T extends Schema> List<T> schemas(DataRuntime runtime, int index, boolean create, List<T> previous, Schema query, DataSet set) throws Exception;
    default <T extends Schema> List<T> schemas(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, DataSet set) throws Exception {
        Schema query = new Schema();
        query.setCatalog(catalog);
        return schemas(runtime, index, create, previous, query, set);
    }

    /**
     * schema[结果集封装]<br/>
     * 根据驱动内置接口补充 schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @return schemas
     * @throws Exception 异常
     */
    <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Schema query) throws Exception;
    /**
     * schema[结果集封装]<br/>
     * 根据驱动内置接口补充 schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @return schemas
     * @throws Exception 异常
     */
    <T extends Schema> List<T> schemas(DataRuntime runtime, boolean create, List<T> previous, Schema query) throws Exception;

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
    Schema schema(DataRuntime runtime, int index, boolean create, Schema meta, DataSet set) throws Exception;
    /**
     * schema[结果集封装]<br/>
     * 当前schema 根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @return schema
     * @throws Exception 异常
     */
    Schema schema(DataRuntime runtime, boolean create, Schema schema) throws Exception;

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果封装 schema 对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Schema
     */
    <T extends Schema> T init(DataRuntime runtime, int index, T meta, Schema query, DataRow row);

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果封装 schema 对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    <T extends Schema> T detail(DataRuntime runtime, int index, T meta, Schema query, DataRow row);
    /* *****************************************************************************************************************
     *                                                     table
     ******************************************************************************************************************/
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
    <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Table query, int types, int struct, ConfigStore configs);
    /**
     * table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
     * @return List
     * @param <T> Table
     */
    default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        Table query = new Table(catalog, schema, pattern);
        return tables(runtime, random, greedy, query, types, struct, configs);
    }
    default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return tables(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return tables(runtime, random, greedy, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return tables(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
        return tables(runtime, random, greedy, catalog, schema, pattern, types, false);
    }
    <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Table query, int types, int struct, ConfigStore configs);
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        Table query = new Table(catalog, schema, pattern);
        return tables(runtime, random, query, types, struct, configs);
    }
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return tables(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return tables(runtime, random, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return tables(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
        return tables(runtime, random, catalog, schema, pattern, types, false);
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
    List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Table query, int types, ConfigStore configs) throws Exception;

    /**
     * table[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        Table query = new Table(catalog, schema, pattern);
        return buildQueryTablesRun(runtime, greedy, query, types, configs);
    }

    /**
     * Table[结果集封装]<br/>
     * Table 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initTableFieldRefer();

    /**
     * Table[结果集封装]<br/>
     * TableComment 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initTableCommentFieldRefer();
    /**
     * table[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Table query, int types) throws Exception;
    /**
     * table[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        Table query = new Table(catalog, schema, pattern);
        return buildQueryTablesCommentRun(runtime, query, types);
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
    <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception;
    /**
     * table[结果集封装]<br/>
     * 根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        Table query = new Table();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return tables(runtime, index, create, previous, query, set);
    }
    <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet set) throws Exception;
    default <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        Table query = new Table();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return tables(runtime, index, create, previous, query, set);
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
    <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table query, int types) throws Exception;
    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        Table query = new Table(catalog, schema, pattern);
        return tables(runtime, create, previous, query, types);
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
    <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> previous, Table query, int types) throws Exception;
    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        Table query = new Table(catalog, schema, pattern);
        return tables(runtime, create, previous, query, types);
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
    <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception;

    /**
     * table[结果集封装]<br/>
     * 表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    default <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        Table query = new Table();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return comments(runtime, index, create, previous, query, set);
    }
    <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet set) throws Exception;
    default <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        Table query = new Table();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return comments(runtime, index, create, previous, query, set);
    }

    /**
     * 查询表创建SQL
     * table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    List<String> ddl(DataRuntime runtime, String random, Table table, boolean init);

    /**
     * table[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return List
     */
    List<Run> buildQueryDdlRun(DataRuntime runtime, Table table) throws Exception;

    /**
     * table[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param table 表
     * @param set sql执行的结果集
     * @return List
     */
    List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set);

    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Table
     */
    <T extends Table> T init(DataRuntime runtime, int index, T meta, Table query, DataRow row);
    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param row 查询结果集
     * @return Table
     */
    default <T extends Table> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        Table query = new Table();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return init(runtime, index, meta, query, row);
    }

    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    <T extends Table> T detail(DataRuntime runtime, int index, T meta, Table query, DataRow row);
    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    default <T extends Table> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        Table query = new Table();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return detail(runtime, index, meta, query, row);
    }

    /* *****************************************************************************************************************
     *                                                     vertex
     ******************************************************************************************************************/
    /**
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
    <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, VertexTable query, int types, int struct, ConfigStore configs);
    /**
     * vertex[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
     * @return List
     * @param <T> VertexTable
     */
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        VertexTable query = new VertexTable(catalog, schema, pattern);
        return vertexs(runtime, random, greedy, query, types, struct, configs);
    }
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return vertexs(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return vertexs(runtime, random, greedy, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return vertexs(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
        return vertexs(runtime, random, greedy, catalog, schema, pattern, types, false);
    }
    <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, VertexTable query, int types, int struct, ConfigStore configs);
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        VertexTable query = new VertexTable(catalog, schema, pattern);
        return vertexs(runtime, random, query, types, struct, configs);
    }
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return vertexs(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return vertexs(runtime, random, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return vertexs(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
        return vertexs(runtime, random, catalog, schema, pattern, types, false);
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
    List<Run> buildQueryVertexsRun(DataRuntime runtime, boolean greedy, VertexTable query, int types, ConfigStore configs) throws Exception;
    /**
     * vertex[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryVertexsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        VertexTable query = new VertexTable(catalog, schema, pattern);
        return buildQueryVertexsRun(runtime, greedy, query, types, configs);
    }

    /**
     * vertex[结果集封装]<br/>
     * vertex 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initVertexFieldRefer();

    /**
     * vertex[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    List<Run> buildQueryVertexsCommentRun(DataRuntime runtime, VertexTable query, int types) throws Exception;
    /**
     * vertex[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryVertexsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        VertexTable query = new VertexTable(catalog, schema, pattern);
        return buildQueryVertexsCommentRun(runtime, query, types);
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
    <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, VertexTable query, DataSet set) throws Exception;
    /**
     * vertex[结果集封装]<br/>
     *  根据查询结果集构造VertexTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryVertexsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return vertexs
     * @throws Exception 异常
     */
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        VertexTable query = new VertexTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return vertexs(runtime, index, create, previous, query, set);
    }
    <T extends VertexTable> List<T> vertexs(DataRuntime runtime, int index, boolean create, List<T> previous, VertexTable query, DataSet set) throws Exception;
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        VertexTable query = new VertexTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return vertexs(runtime, index, create, previous, query, set);
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
    <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, VertexTable query, int types) throws Exception;

    /**
     * vertex[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return vertexs
     * @throws Exception 异常
     */
    default <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        VertexTable query = new VertexTable(catalog, schema, pattern);
        return vertexs(runtime, create, previous, query, types);
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
    <T extends VertexTable> List<T> vertexs(DataRuntime runtime, boolean create, List<T> previous, VertexTable query, int types) throws Exception;
    /**
     * vertex[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return vertexs
     * @throws Exception 异常
     */
    default <T extends VertexTable> List<T> vertexs(DataRuntime runtime, boolean create, List<T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        VertexTable query = new VertexTable(catalog, schema, pattern);
        return vertexs(runtime, create, previous, query, types);
    }

    /**
     * 查询表创建SQL
     * vertex[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param vertex 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    List<String> ddl(DataRuntime runtime, String random, VertexTable vertex, boolean init);

    /**
     * vertex[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param vertex 表
     * @return List
     */
    List<Run> buildQueryDdlRun(DataRuntime runtime, VertexTable vertex) throws Exception;

    /**
     * vertex[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param vertex 表
     * @param set sql执行的结果集
     * @return List
     */
    List<String> ddl(DataRuntime runtime, int index, VertexTable vertex, List<String> ddls, DataSet set);

    /**
     * vertex[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return VertexTable
     */
    <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, VertexTable query, DataRow row);
    /**
     * vertex[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param row 查询结果集
     * @return VertexTable
     */
    default <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        VertexTable query = new VertexTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return init(runtime, index, meta, query, row);
    }

    /**
     * vertex[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return VertexTable
     */
    <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, VertexTable query, DataRow row);
    /**
     * vertex[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return VertexTable
     */
    default <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        VertexTable query = new VertexTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return detail(runtime, index, meta, query, row);
    }

    /* *****************************************************************************************************************
     *                                                     edge
     ******************************************************************************************************************/

    /**
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
    <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, EdgeTable query, int types, int struct, ConfigStore configs);
    /**
     * edge[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
     * @return List
     * @param <T> EdgeTable
     */
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        EdgeTable query = new EdgeTable(catalog, schema, pattern);
        return edges(runtime, random, greedy, query, types, struct, configs);
    }
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return edges(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return edges(runtime, random, greedy, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return edges(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
        return edges(runtime, random, greedy, catalog, schema, pattern, types, false);
    }
    <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, EdgeTable query, int types, int struct, ConfigStore configs);
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        EdgeTable query = new EdgeTable(catalog, schema, pattern);
        return edges(runtime, random, query, types, struct, configs);
    }
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return edges(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return edges(runtime, random, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return edges(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
        return edges(runtime, random, catalog, schema, pattern, types, false);
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
    List<Run> buildQueryEdgesRun(DataRuntime runtime, boolean greedy, EdgeTable query, int types, ConfigStore configs) throws Exception;
    /**
     * edge[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryEdgesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        EdgeTable query = new EdgeTable(catalog, schema, pattern);
        return buildQueryEdgesRun(runtime, greedy, query, types, configs);
    }

    /**
     * master[结果集封装]<br/>
     * MasterTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initEdgeFieldRefer();

    /**
     * edge[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, EdgeTable query, int types) throws Exception;
    /**
     * edge[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        EdgeTable query = new EdgeTable(catalog, schema, pattern);
        return buildQueryEdgesCommentRun(runtime, query, types);
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
    <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, EdgeTable query, DataSet set) throws Exception;
    /**
     * edge[结果集封装]<br/>
     *  根据查询结果集构造EdgeTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryEdgesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return edges
     * @throws Exception 异常
     */
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        EdgeTable query = new EdgeTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return edges(runtime, index, create, previous, query, set);
    }
    <T extends EdgeTable> List<T> edges(DataRuntime runtime, int index, boolean create, List<T> previous, EdgeTable query, DataSet set) throws Exception;
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        EdgeTable query = new EdgeTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return edges(runtime, index, create, previous, query, set);
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
    <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, EdgeTable query, int types) throws Exception;

    /**
     * edge[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return edges
     * @throws Exception 异常
     */
    default <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        EdgeTable query = new EdgeTable(catalog, schema, pattern);
        return edges(runtime, create, previous, query, types);
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
    <T extends EdgeTable> List<T> edges(DataRuntime runtime, boolean create, List<T> previous, EdgeTable query, int types) throws Exception;
    /**
     * edge[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return edges
     * @throws Exception 异常
     */
    default <T extends EdgeTable> List<T> edges(DataRuntime runtime, boolean create, List<T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        EdgeTable query = new EdgeTable(catalog, schema, pattern);
        return edges(runtime, create, previous, query, types);
    }

    /**
     * 查询表创建SQL
     * edge[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param edge 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    List<String> ddl(DataRuntime runtime, String random, EdgeTable edge, boolean init);

    /**
     * edge[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param edge 表
     * @return List
     */
    List<Run> buildQueryDdlRun(DataRuntime runtime, EdgeTable edge) throws Exception;

    /**
     * edge[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param edge 表
     * @param set sql执行的结果集
     * @return List
     */
    List<String> ddl(DataRuntime runtime, int index, EdgeTable edge, List<String> ddls, DataSet set);

    /**
     * edge[结果集封装]<br/>
     * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return EdgeTable
     */
    <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, EdgeTable query, DataRow row);

    /**
     * edge[结果集封装]<br/>
     * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param row 查询结果集
     * @return EdgeTable
     */
    default <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        EdgeTable query = new EdgeTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return init(runtime, index, meta, query, row);
    }

    /**
     * edge[结果集封装]<br/>
     * 根据查询结果封装EdgeTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return EdgeTable
     */
    <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, EdgeTable query, DataRow row);
    /**
     * edge[结果集封装]<br/>
     * 根据查询结果封装EdgeTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return EdgeTable
     */
    default <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        EdgeTable query = new EdgeTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return detail(runtime, index, meta, query, row);
    }

    /* *****************************************************************************************************************
     *                                                     view
     ******************************************************************************************************************/

    /**
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
    <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, View query, int types, int struct, ConfigStore configs);
    /**
     * view[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
     * @return List
     * @param <T> View
     */
    default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        View query = new View(catalog, schema, pattern);
        return views(runtime, random, greedy, catalog, schema, pattern, types, struct, configs);
    }
    default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return views(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return views(runtime, random, greedy, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return views(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
        return views(runtime, random, greedy, catalog, schema, pattern, types, false);
    }
    <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, int types, int struct, ConfigStore configs);
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        View query = new View(catalog, schema, pattern);
        return views(runtime, random, query, types, struct, configs);
    }
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return views(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return views(runtime, random, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return views(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
        return views(runtime, random, catalog, schema, pattern, types, false);
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
    List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, View query, int types, ConfigStore configs) throws Exception;

    /**
     * view[命令合成]<br/>
     * 查询视图,不是查视图中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        View query = new View(catalog, schema, pattern);
        return buildQueryViewsRun(runtime, greedy, query, types, configs);
    }

    /**
     * View[结果集封装]<br/>
     * View 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initViewFieldRefer();
    /**
     * view[命令合成]<br/>
     * 查询视图备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    List<Run> buildQueryViewsCommentRun(DataRuntime runtime, View query, int types) throws Exception;
    /**
     * view[命令合成]<br/>
     * 查询视图备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryViewsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        View query = new View(catalog, schema, pattern);
        return buildQueryViewsCommentRun(runtime, query, types);
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
    <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, View query, DataSet set) throws Exception;
    /**
     * view[结果集封装]<br/>
     *  根据查询结果集构造View
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return views
     * @throws Exception 异常
     */
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        View query = new View();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return views(runtime, index, create, previous, query ,set);
    }
    <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> previous, View query, DataSet set) throws Exception;
    default <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        View query = new View();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return views(runtime, index, create, previous, query ,set);
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
    <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, View query, int types) throws Exception;
    /**
     * view[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return views
     * @throws Exception 异常
     */
    default <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        View query = new View(catalog, schema, pattern);
        return views(runtime, create, previous, query, types);
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
    <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> previous, View query, int types) throws Exception;
    /**
     * view[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return views
     * @throws Exception 异常
     */
    default <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        View query = new View(catalog, schema, pattern);
        return views(runtime, create, previous, query, types);
    }

    /**
     * 查询视图创建SQL
     * view[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param view 视图
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    List<String> ddl(DataRuntime runtime, String random, View view, boolean init);

    /**
     * view[命令合成]<br/>
     * 查询视图DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param view 视图
     * @return List
     */
    List<Run> buildQueryDdlRun(DataRuntime runtime, View view) throws Exception;

    /**
     * view[结果集封装]<br/>
     * 查询视图DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param view 视图
     * @param set sql执行的结果集
     * @return List
     */
    List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set);

    /**
     * view[结果集封装]<br/>
     * 根据查询结果封装View对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return View
     */
    <T extends View> T init(DataRuntime runtime, int index, T meta, View query, DataRow row);
    /**
     * view[结果集封装]<br/>
     * 根据查询结果封装View对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param row 查询结果集
     * @return View
     */
    default <T extends View> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        View query = new View();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return init(runtime, index, meta, query, row);
    }

    /**
     * view[结果集封装]<br/>
     * 根据查询结果封装View对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return View
     */
    <T extends View> T detail(DataRuntime runtime, int index, T meta, View query, DataRow row);
    /**
     * view[结果集封装]<br/>
     * 根据查询结果封装View对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return View
     */
    default <T extends View> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        View query = new View();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return detail(runtime, index, meta, query, row);
    }

    /* *****************************************************************************************************************
     *                                                     master table
     ******************************************************************************************************************/

    /**
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
    <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, MasterTable query, int types, int struct, ConfigStore configs);
    /**
     * master[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @param struct 查询的属性 参考Metadata.TYPE 多个属性相加算出总和 true:表示查询全部
     * @return List
     * @param <T> MasterTable
     */
    default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        MasterTable query = new MasterTable(catalog, schema, pattern);
        return masters(runtime, random, greedy, query, types, struct, configs);
    }

    default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return masters(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return masters(runtime, random, greedy, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return masters(runtime, random, greedy, catalog, schema, pattern, types, struct, null);
    }
    default <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) {
        return masters(runtime, random, greedy, catalog, schema, pattern, types, false);
    }
    <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, int types, int struct, ConfigStore configs);
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        MasterTable query = new MasterTable(catalog, schema, pattern);
        return masters(runtime, random, query, types, struct, configs);
    }
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct) {
        return masters(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct, ConfigStore configs) {
        int structs = 0;
        if(struct) {
            structs = Metadata.TYPE.ALL.value();
        }
        return masters(runtime, random, catalog, schema, pattern, types, structs, configs);
    }
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, boolean struct) {
        return masters(runtime, random, catalog, schema, pattern, types, struct, null);
    }
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types) {
        return masters(runtime, random, catalog, schema, pattern, types, false);
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
    List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, MasterTable query, int types, ConfigStore configs) throws Exception;
    /**
     * master[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        MasterTable query = new MasterTable(catalog, schema, pattern);
        return buildQueryMasterTablesRun(runtime, greedy, query, types, configs);
    }

    /**
     * master[结果集封装]<br/>
     * MasterTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initMasterTableFieldRefer();
    /**
     * master[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, MasterTable query, int types) throws Exception;
    /**
     * master[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     * @throws Exception Exception
     */
    default List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        MasterTable query = new MasterTable(catalog, schema, pattern);
        return buildQueryMasterTablesCommentRun(runtime, query, types);
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
    <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, MasterTable query, DataSet set) throws Exception;
    /**
     * master[结果集封装]<br/>
     *  根据查询结果集构造MasterTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return masters
     * @throws Exception 异常
     */
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        MasterTable query = new MasterTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return masters(runtime, index, create, previous, query, set);
    }
    <T extends MasterTable> List<T> masters(DataRuntime runtime, int index, boolean create, List<T> previous, MasterTable query, DataSet set) throws Exception;
    default <T extends MasterTable> List<T> masters(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
        MasterTable query = new MasterTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return masters(runtime, index, create, previous, query, set);
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
    <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, MasterTable query, int types) throws Exception;

    /**
     * master[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return masters
     * @throws Exception 异常
     */
    default <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        MasterTable query = new MasterTable(catalog, schema, pattern);
        return masters(runtime, create, previous, query, types);
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
    <T extends MasterTable> List<T> masters(DataRuntime runtime, boolean create, List<T> previous, MasterTable query, int types) throws Exception;
    /**
     * master[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return masters
     * @throws Exception 异常
     */
    default <T extends MasterTable> List<T> masters(DataRuntime runtime, boolean create, List<T> previous, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        MasterTable query = new MasterTable(catalog, schema, pattern);
        return masters(runtime, create, previous, query, types);
    }

    /**
     * 查询表创建SQL
     * master[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param master 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    List<String> ddl(DataRuntime runtime, String random, MasterTable master, boolean init);

    /**
     * master[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param master 表
     * @return List
     */
    List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable master) throws Exception;

    /**
     * master[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param master 表
     * @param set sql执行的结果集
     * @return List
     */
    List<String> ddl(DataRuntime runtime, int index, MasterTable master, List<String> ddls, DataSet set);

    /**
     * master[结果集封装]<br/>
     * 根据查询结果封装MasterTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return MasterTable
     */
    <T extends MasterTable> T init(DataRuntime runtime, int index, T meta, MasterTable query, DataRow row);
    /**
     * master[结果集封装]<br/>
     * 根据查询结果封装MasterTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param row 查询结果集
     * @return MasterTable
     */
    default <T extends MasterTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        MasterTable query = new MasterTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return init(runtime, index, meta, query, row);
    }

    /**
     * master[结果集封装]<br/>
     * 根据查询结果封装MasterTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return MasterTable
     */
    <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta, MasterTable query, DataRow row);
    /**
     * master[结果集封装]<br/>
     * 根据查询结果封装MasterTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return MasterTable
     */
    default <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        MasterTable query = new MasterTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return detail(runtime, index, meta, query, row);
    }

    /* *****************************************************************************************************************
     *                                                     partition table
     ******************************************************************************************************************/
    /**
     * 表分区方式及分片
     * @param table 主表
     * @return Partition
     */
    Table.Partition partition(DataRuntime runtime, String random, Table table);

    /**
     * partition table[命令合成]<br/>
     * 查询表分区方式及分片
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return String
     */
    List<Run> buildQueryTablePartitionRun(DataRuntime runtime, Table table);

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
    Table.Partition partition(DataRuntime runtime, int index, boolean create, Table.Partition meta, Table table, DataSet set) throws Exception;

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
    Table.Partition init(DataRuntime runtime, int index, boolean create, Table.Partition meta, Table table, DataRow row) throws Exception;

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
    Table.Partition detail(DataRuntime runtime, int index, boolean create, Table.Partition meta, Table table, DataRow row) throws Exception;
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
    <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, PartitionTable query);
    /**
     * partition table[调用入口]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param master 主表
     * @param pattern 名称统配符或正则
     * @return List
     * @param <T> MasterTable
     */
    default <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern) {
        PartitionTable query = new PartitionTable();
        query.setMaster(master);
        if(null != tags) {
            for(String key:tags.keySet()) {
                Tag tag = null;
                Object value = tags.get(key);
                if(value instanceof Tag) {
                    tag = (Tag)value;
                }else{
                    tag = new Tag(key, value);
                }
                query.addTag(tag);
            }
        }
        query.setName(pattern);
        return partitions(runtime, random, greedy, query);
    }

    /**
     * partition table[命令合成]<br/>
     * 查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     */
    List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  PartitionTable query, int types) throws Exception;
    /**
     * partition table[命令合成]<br/>
     * 查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param pattern 名称统配符或正则
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     */
    default List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        PartitionTable query = new PartitionTable(catalog, schema, pattern);
        return buildQueryPartitionTablesRun(runtime, greedy, query, types);
    }

    /**
     * partition table[命令合成]<br/>
     * 根据主表查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  PartitionTable query) throws Exception {
        return buildQueryPartitionTablesRun(runtime, greedy, query, 1);
    }

    /**
     * partition table[命令合成]<br/>
     * 根据主表查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param master 主表
     * @param tags 标签名+标签值
     * @param pattern 名称统配符或正则
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master, Map<String, Tag> tags, String pattern) throws Exception {
        PartitionTable query = new PartitionTable();
        query.setMaster(master);
        if(null != tags) {
            for(String key:tags.keySet()) {
                Tag tag = null;
                Object value = tags.get(key);
                if(value instanceof Tag) {
                    tag = (Tag)value;
                }else{
                    tag = new Tag(key, value);
                }
                query.addTag(tag);
            }
        }
        query.setName(pattern);
        return buildQueryPartitionTablesRun(runtime, greedy, query);
    }

    /**
     * partition table[命令合成]<br/>
     * 根据主表查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param master 主表
     * @param tags 标签名+标签值
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master, Map<String, Tag> tags) throws Exception {
        PartitionTable query = new PartitionTable();
        query.setMaster(master);
        if(null != tags) {
            for(String key:tags.keySet()) {
                Tag tag = null;
                Object value = tags.get(key);
                if(value instanceof Tag) {
                    tag = (Tag)value;
                }else{
                    tag = new Tag(key, value);
                }
                query.addTag(tag);
            }
        };
        return buildQueryPartitionTablesRun(runtime, greedy, query);
    }

    /**
     * partition table[命令合成]<br/>
     * 根据主表查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param master 主表
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master) throws Exception {
        PartitionTable query = new PartitionTable();
        query.setMaster(master);
        return buildQueryPartitionTablesRun(runtime, greedy, query);
    }

    /**
     * partition table[结果集封装]<br/>
     * PartitionTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initPartitionTableFieldRefer();

    /**
     * partition table[结果集封装]<br/>
     * Table.Partition 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initTablePartitionFieldRefer();

    /**
     * partition table[结果集封装]<br/>
     * Table.Partition.Slice 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initTablePartitionSliceFieldRefer();

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param total 合计SQL数量
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, LinkedHashMap<String, T> tables, PartitionTable query, DataSet set) throws Exception;

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param total 合计SQL数量
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param master 主表
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    default <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        PartitionTable query = new PartitionTable();
        query.setMaster(master);
        query.setCatalog(catalog);
        query.setSchema(schema);
        return partitions(runtime, total, index, create, tables, query, set);
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @return tables
     * @throws Exception 异常
     */
    <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, PartitionTable query) throws Exception;
    /**
     * partition table[结果集封装]<br/>
     * 根据根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param master 主表
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @return tables
     * @throws Exception 异常
     */
    default <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master) throws Exception {
        PartitionTable query = new PartitionTable();
        query.setMaster(master);
        query.setCatalog(catalog);
        query.setSchema(schema);
        return partitions(runtime, create, tables, query);
    }

    /**
     * partition table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table PartitionTable
     * @return List
     */
    List<String> ddl(DataRuntime runtime, String random, PartitionTable table);

    /**
     * partition table[命令合成]<br/>
     * 查询 PartitionTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table PartitionTable
     * @return List
     */
    List<Run> buildQueryDdlRun(DataRuntime runtime, PartitionTable table) throws Exception;

    /**
     * partition table[结果集封装]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param table MasterTable
     * @param set sql执行的结果集
     * @return List
     */
    List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set);

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果封装PartitionTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return PartitionTable
     */
    <T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, PartitionTable query, DataRow row);
    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果封装PartitionTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param row 查询结果集
     * @return PartitionTable
     */
    default <T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        PartitionTable query = new PartitionTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return init(runtime, index, meta, query, row);
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果封装PartitionTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return PartitionTable
     */
    <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, PartitionTable query, DataRow row);
    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果封装PartitionTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return PartitionTable
     */
    default <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        PartitionTable query = new PartitionTable();
        query.setCatalog(catalog);
        query.setSchema(schema);
        return detail(runtime, index, meta, query, row);
    }
    /* *****************************************************************************************************************
     *                                                     column
     * 获取表的几种方法和场景
     * (1)查询系统表
     * (2)JDBC结果集自带的ResultSet
     * (3)JDBC.DatabaseMetaData查询指定表的列
     * (4)SPRING.queryForRowSet.SqlRowSetMetaData(与2类似)
     ******************************************************************************************************************/
    /**
     * column[调用入口]<br/>(多方法合成)<br/>
     * 查询表结构
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
     * @param primary 是否检测主键
     * @return Column
     * @param <T>  Column
     */
    <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, Column query, boolean primary, ConfigStore configs);
    default <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary, ConfigStore configs) {
        Column query = new Column();
        query.setTable(table);
        return columns(runtime, random, greedy, table, query, primary, configs);
    }
    default <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary) {
        Column query = new Column();
        query.setTable(table);
        return columns(runtime, random, greedy, table, query, primary, new DefaultConfigStore());
    }

    /**
     * column[调用入口]<br/>(方法1)<br/>
     * 查询列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
     * @return List
     * @param <T> Column
     */
    default <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Column query, ConfigStore configs) {
        return columns(runtime, random, greedy, new ArrayList<>(), query, configs);
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
    <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Collection<? extends Table> tables, Column query, ConfigStore configs);
    default <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Collection<? extends Table> tables, Column query) {
        return columns(runtime, random, greedy, tables, query, new DefaultConfigStore());
    }

    /**
     * column[命令合成]<br/>(方法1)<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
     * @return runs
     */
    List<Run> buildQueryColumnsRun(DataRuntime runtime, boolean metadata, Column query, ConfigStore configs) throws Exception;

    /**
     * column[命令合成]<br/>(方法1)<br/>
     * 查询多个表的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param tables 表
     * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
     * @return runs
     */
    List<Run> buildQueryColumnsRun(DataRuntime runtime, boolean metadata, Collection<? extends Table> tables, Column query, ConfigStore configs) throws Exception;

    /**
     * Column[结果集封装]<br/>
     * Column 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initColumnFieldRefer();
    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 根据系统表查询SQL获取表结构
     *  根据查询结果集构造Column
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param set 系统表查询SQL结果集
     * @return columns
     * @throws Exception 异常
     */
    <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception;

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 根据系统表查询SQL获取表结构
     * 根据查询结果集构造Column
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param set 系统表查询SQL结果集
     * @return columns
     * @throws Exception 异常
     */
    <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception;

    /**
     * column[结果集封装]<br/>
     * 解析JDBC get columns结果
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @return previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @throws Exception 异常
     */
    <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Column query) throws Exception;
    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 根据系统表查询SQL获取表结构
     * 根据查询结果集构造Column,并分配到各自的表中
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 表
     * @param set 系统表查询SQL结果集
     * @return columns
     * @throws Exception 异常
     */
    <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Collection<? extends Table> tables, Column query, DataSet set) throws Exception;

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 列基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 系统表查询SQL结果集
     * @param <T> Column
     */
    <T extends Column> T init(DataRuntime runtime, int index, T meta, Column query, DataRow row);

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 列详细属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 系统表查询SQL结果集
     * @return Column
     * @param <T> Column
     */
    <T extends Column> T detail(DataRuntime runtime, int index, T meta, Column query, DataRow row);

    /**
     * column[结构集封装-依据]<br/>
     * 读取column元数据结果集的依据，主要在dataTypeMetadataRefer(DataRuntime runtime)基础上补充length/precision/sacle相关
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 具体数据类型,length/precisoin/scale三个属性需要根据数据类型覆盖通用配置
     * @return ColumnMetadataAdapter
     */
    default TypeMetadata.Refer dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta) {
        TypeMetadata.Refer refer = new TypeMetadata.Refer();
        //长度列
        String columnFieldLengthRefer = columnFieldLengthRefer(runtime, meta);
        if(null != columnFieldLengthRefer) {
            refer.setLengthRefer(columnFieldLengthRefer);
        }
        //有效位数列
        String columnFieldPrecisionRefer = columnFieldPrecisionRefer(runtime, meta);
        if(null != columnFieldPrecisionRefer) {
            refer.setPrecisionRefer(columnFieldPrecisionRefer);
        }
        //小数位数列
        String columnFieldScaleRefer = columnFieldScaleRefer(runtime, meta);
        if(null != columnFieldScaleRefer) {
            refer.setScaleRefer(columnFieldScaleRefer);
        }
        int ignoreLength = ignoreLength(runtime, meta);
        if(-1 != ignoreLength) {
            refer.setIgnoreLength(ignoreLength);
        }
        int ignorePrecision = ignorePrecision(runtime, meta);
        if(-1 != ignorePrecision) {
            refer.setIgnorePrecision(ignorePrecision);
        }
        int ignoreScale = ignoreScale(runtime, meta);
        if(-1 != ignoreScale) {
            refer.setIgnoreScale(ignoreScale);
        }
        return refer;
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据长度列<br/>
     * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    String columnFieldLengthRefer(DataRuntime runtime, TypeMetadata meta);

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字有效位数列<br/>
     * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    String columnFieldPrecisionRefer(DataRuntime runtime, TypeMetadata meta);

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字小数位数列<br/>
     * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    String columnFieldScaleRefer(DataRuntime runtime, TypeMetadata meta);

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 是否忽略长度<br/>
     * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    int columnMetadataIgnoreLength(DataRuntime runtime, TypeMetadata meta);

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 是否忽略有效位数<br/>
     * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    int columnMetadataIgnorePrecision(DataRuntime runtime, TypeMetadata meta);

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 是否忽略小数位数<br/>
     * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    int columnMetadataIgnoreScale(DataRuntime runtime, TypeMetadata meta);

    /* *****************************************************************************************************************
     *                                                     tag
     ******************************************************************************************************************/

    /**
     * tag[调用入口]<br/>
     * 查询表结构
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
     * @return Tag
     * @param <T>  Tag
     */
    <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query);
    default <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table) {
        Tag query = new Tag();
        query.setTable(table);
        return tags(runtime, random, greedy, table, query);
    }

    /**
     * tag[命令合成]<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据mdtadata属性
     * @return runs
     */
    List<Run> buildQueryTagsRun(DataRuntime runtime, boolean greedy, Tag query) throws Exception;

    /**
     * tag[结果集封装]<br/>
     * tag 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initTagFieldRefer();

    /**
     * tag[结果集封装]<br/>
     *  根据查询结果集构造Tag
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryTagsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param set 查询结果集
     * @return tags
     * @throws Exception 异常
     */
    <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Tag query, DataSet set) throws Exception;

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
    <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Tag query) throws Exception;

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
    <T extends Tag> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row);

    /**
     * tag[结果集封装]<br/>
     * 列详细属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 系统表查询SQL结果集
     * @return Tag
     * @param <T> Tag
     */
    <T extends Tag> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row);
    /* *****************************************************************************************************************
     *                                                     primary
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
    PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, PrimaryKey query);
    /**
     * primary[调用入口]<br/>
     * 查询主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param table 表
     * @return PrimaryKey
     */
    default PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table) {
        PrimaryKey query = new PrimaryKey();
        query.setTable(table);
        return primary(runtime, random, greedy, query);
    }

    /**
     * primary[命令合成]<br/>
     * 查询表上的主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  PrimaryKey query) throws Exception;
    /**
     * primary[命令合成]<br/>
     * 查询表上的主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return runs
     */
    default List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception {
        PrimaryKey query = new PrimaryKey();
        query.setTable(table);
        return buildQueryPrimaryRun(runtime, greedy, query);
    }

    /**
     * primary[结果集封装]<br/>
     * PrimaryKey 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initPrimaryKeyFieldRefer();
    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param set sql查询结果
     * @throws Exception 异常
     */
    <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, PrimaryKey query, DataSet set) throws Exception;
    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param table 表
     * @param set sql查询结果
     * @throws Exception 异常
     */
    default <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception {
        PrimaryKey query = new PrimaryKey();
        query.setTable(table);
        return init(runtime, index, meta, query, set);
    }

    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param set sql查询结果
     * @throws Exception 异常
     */
    <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, PrimaryKey query, DataSet set) throws Exception;

    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param table 表
     * @param set sql查询结果
     * @throws Exception 异常
     */
    default <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception {
        PrimaryKey query = new PrimaryKey();
        query.setTable(table);
        return detail(runtime, index, meta, query, set);
    }

    /**
     * primary[结构集封装]<br/>
     *  根据驱动内置接口补充PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @throws Exception 异常
     */
    PrimaryKey primary(DataRuntime runtime, PrimaryKey query) throws Exception;
    /**
     * primary[结构集封装]<br/>
     *  根据驱动内置接口补充PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @throws Exception 异常
     */
    default PrimaryKey primary(DataRuntime runtime, Table table) throws Exception {
        PrimaryKey query = new PrimaryKey();
        query.setTable(table);
        return primary(runtime, query);
    }

    /* *****************************************************************************************************************
     *                                                     foreign
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
    <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, ForeignKey query);
    /**
     * foreign[调用入口]<br/>
     * 查询外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param table 表
     * @return PrimaryKey
     */
    default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table) {
        ForeignKey query = new ForeignKey();
        query.setTable(table);
        return foreigns(runtime, random, greedy, query);
    }

    /**
     * foreign[命令合成]<br/>
     * 查询表上的外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  ForeignKey query) throws Exception;
    /**
     * foreign[命令合成]<br/>
     * 查询表上的外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return runs
     */
    default List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception {
        ForeignKey query = new ForeignKey();
        query.setTable(table);
        return buildQueryForeignsRun(runtime, greedy, query);
    }

    /**
     * foreign[结果集封装]<br/>
     * ForeignKey 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initForeignKeyFieldRefer();

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
    <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, LinkedHashMap<String, T> previous, ForeignKey query, DataSet set) throws Exception;
    /**
     * foreign[结构集封装]<br/>
     *  根据查询结果集构造PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
     * @param table 表
     * @param previous 上一步查询结果
     * @param set sql查询结果
     * @throws Exception 异常
     */
    default <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> previous, DataSet set) throws Exception {
        ForeignKey query = new ForeignKey();
        query.setTable(table);
        return foreigns(runtime, index, previous, query, set);
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
    <T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, ForeignKey query, DataRow row) throws Exception;

    /**
     * foreign[结构集封装]<br/>
     * 根据查询结果集构造ForeignKey基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param table 表
     * @param row sql查询结果
     * @throws Exception 异常
     */
    default <T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
        ForeignKey query = new ForeignKey();
        query.setTable(table);
        return init(runtime, index, meta, query, row);
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
    <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, ForeignKey query, DataRow row) throws Exception;
    /**
     * foreign[结构集封装]<br/>
     * 根据查询结果集构造ForeignKey更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param table 表
     * @param row sql查询结果
     * @throws Exception 异常
     */
    default <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
        ForeignKey query = new ForeignKey();
        query.setTable(table);
        return detail(runtime, index, meta, query, row);
    }
    /* *****************************************************************************************************************
     *                                                     index
     ******************************************************************************************************************/

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
    <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Index query);
    /**
     *
     * index[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param table 表
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    default <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern) {
        Index query = new Index();
        query.setTable(table);
        query.setName(pattern);
        return indexes(runtime, random, greedy,  query);
    }

    <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy,  Collection<? extends Table> tables, Index query);
    /**
     *
     * index[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @return  LinkedHashMap
     * @param <T> Index
     */
    <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Index query);
    /**
     *
     * index[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    default <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern) {
        Index query = new Index();
        query.setTable(table);
        query.setName(pattern);
        return indexes(runtime, random, query);
    }

    /**
     * index[命令合成]<br/>
     * 查询表上的索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Index query);
    /**
     * index[命令合成]<br/>
     * 查询表上的索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param pattern 名称
     * @return runs
     */
    default List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Table table, String pattern) {
        Index query = new Index();
        query.setTable(table);
        query.setName(pattern);
        return buildQueryIndexesRun(runtime, greedy, query);
    }
    List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Collection<? extends Table> tables);

    /**
     * Index[结果集封装]<br/>
     * Index 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initIndexFieldRefer();
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
    <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Index query, DataSet set) throws Exception;
    /**
     * index[结果集封装]<br/>
     *  根据查询结果集构造Index
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return indexes indexes
     * @throws Exception 异常
     */
    default <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> previous, DataSet set) throws Exception {
        Index query = new Index();
        query.setTable(table);
        return indexes(runtime, index, create, previous, query, set);
    }
    <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, List<T> previous, Index query, DataSet set) throws Exception;
    default <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> previous, DataSet set) throws Exception {
        Index query = new Index();
        query.setTable(table);
        return indexes(runtime, index, create, previous, query, set);
    }
    <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> previous, Index query, DataSet set) throws Exception;

    /**
     * index[结果集封装]<br/>
     * 根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @return indexes indexes
     * @throws Exception 异常
     */
    <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Index query) throws Exception;
    /**
     * index[结果集封装]<br/>
     * 根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param unique 是否唯一
     * @param approximate 索引允许结果反映近似值
     * @return indexes indexes
     * @throws Exception 异常
     */
    default <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate) throws Exception {
        Index query = new Index();
        query.setTable(table);
        query.setUnique(unique);
        return indexes(runtime, create, indexes, query);
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
    <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Index query) throws Exception;
    /**
     * index[结果集封装]<br/>
     * 根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param unique 是否唯一
     * @param approximate 索引允许结果反映近似值
     * @return indexes indexes
     * @throws Exception 异常
     */
    default <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate) throws Exception {
        Index query = new Index();
        query.setTable(table);
        query.setUnique(unique);
        return indexes(runtime, create, indexes, query);
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
    <T extends Index> T init(DataRuntime runtime, int index, T meta, Index query, DataRow row) throws Exception;
    /**
     * index[结构集封装]<br/>
     * 根据查询结果集构造index基础属性(name,table,schema,catalog)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param table 表
     * @param row sql查询结果
     * @throws Exception 异常
     */
    default <T extends Index> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
        Index query = new Index();
        query.setTable(table);
        return init(runtime, index, meta, query, row);
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
    <T extends Index> T detail(DataRuntime runtime, int index, T meta, Index query, DataRow row) throws Exception;

    /**
     * index[结构集封装]<br/>
     * 根据查询结果集构造index更多属性(column,order, position)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param meta 上一步封装结果
     * @param table 表
     * @param row sql查询结果
     * @throws Exception 异常
     */
    default <T extends Index> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
        Index query = new Index();
        query.setTable(table);
        return detail(runtime, index, meta, query, row);
    }
    /* *****************************************************************************************************************
     *                                                     constraint
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
    <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Constraint query);
    /**
     *
     * constraint[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param table 表
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    default <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern) {
        Constraint query = new Constraint();
        query.setTable(table);
        query.setName(pattern);
        return constraints(runtime, random, greedy, query);
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
    <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Constraint query);
    /**
     *
     * constraint[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @param column 列
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    default <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern) {
        Constraint query = new Constraint();
        query.setTable(table);
        query.setName(pattern);
        if(null != column) {
            query.addColumn(column);
        }
        return constraints(runtime, random, query);
    }

    /**
     * constraint[命令合成]<br/>
     * 查询表上的约束
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Constraint query);
    /**
     * constraint[命令合成]<br/>
     * 查询表上的约束
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param pattern 名称通配符或正则
     * @return runs
     */
    default List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Table table, Column column, String pattern) {
        Constraint query = new Constraint();
        query.setTable(table);
        query.setName(pattern);
        if(null != column) {
            query.addColumn(column);
        }
        return buildQueryConstraintsRun(runtime, greedy, query);
    }

    /**
     * constraint[结果集封装]<br/>
     * Constraint 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initConstraintFieldRefer();

    /**
     * constraint[结果集封装]<br/>
     *  根据查询结果集构造Constraint
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return constraints constraints
     * @throws Exception 异常
     */
    <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, List<T> previous, Constraint query, DataSet set) throws Exception;
    /**
     * constraint[结果集封装]<br/>
     *  根据查询结果集构造Constraint
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	default <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> previous, DataSet set) throws Exception {
		Constraint query = new Constraint();
		query.setTable(table);
		return constraints(runtime, index, create, previous, query, set);
	}

	/**
	 * constraint[结果集封装]<br/>
	 *  根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param query 查询条件 根据metadata属性
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	<T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Constraint query, DataSet set) throws Exception;
	/**
	 * constraint[结果集封装]<br/>
	 *  根据查询结果集构造Constraint
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	default <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, Column> columns, LinkedHashMap<String, T> previous, DataSet set) throws Exception {
		Constraint query = new Constraint();
		query.setTable(table);
		query.setColumns(columns);
		return constraints(runtime, index, create, previous, query, set);
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
	<T extends Constraint> T init(DataRuntime runtime, int index, T meta, Constraint query, DataRow row);
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return Constraint
	 */
	default <T extends Constraint> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		Constraint query = new Constraint();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return init(runtime, index, meta, query, row);
	}
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Constraint
	 */
	<T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Constraint query, DataRow row);
	/**
	 * catalog[结果集封装]<br/>
	 * 根据查询结果封装Constraint对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Constraint
	 */
	default <T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		Constraint query = new Constraint();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return detail(runtime, index, meta, query, row);
	}

	/* *****************************************************************************************************************
	 * 													trigger
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
	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Trigger query);
	/**
	 *
	 * trigger[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events) {
		Trigger query = new Trigger();
		query.setTable(table);
		query.setEvent(events);
		return triggers(runtime, random, greedy, query);
	}
	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	List<Run> buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Trigger query) ;
	/**
	 * trigger[命令合成]<br/>
	 * 查询表上的 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param events 事件 INSERT|UPDATE|DELETE
	 * @return runs
	 */
	default List<Run> buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Table table, List<Trigger.EVENT> events) {
		Trigger query = new Trigger();
		query.setTable(table);
		query.setEvent(events);
		return buildQueryTriggersRun(runtime, greedy, query);
	}

    /**
     * trigger[结果集封装]<br/>
     * Trigger 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initTriggerFieldRefer();

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
	<T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Trigger query, DataSet set) throws Exception;
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	default <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> previous, DataSet set) throws Exception {
		Trigger query = new Trigger();
		query.setTable(table);
		return triggers(runtime, index, create, previous, query, set);
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
	<T extends Trigger> T init(DataRuntime runtime, int index, T meta, Trigger query, DataRow row);

	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return Trigger
	 */
	default <T extends Trigger> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		Trigger query = new Trigger();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return init(runtime, index, meta, query, row);
	}
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Trigger
	 */
	<T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Trigger query, DataRow row);
	/**
	 * trigger[结果集封装]<br/>
	 * 根据查询结果封装trigger对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Trigger
	 */
	default <T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		Trigger query = new Trigger();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return detail(runtime, index, meta, query, row);
	}

	/* *****************************************************************************************************************
	 * 													procedure
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
	<T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Procedure query);
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		Procedure query = new Procedure();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return procedures(runtime, random, greedy, query);
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
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Procedure query);
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		Procedure query = new Procedure();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return procedures(runtime, random, query);
	}
	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Procedure query);
	/**
	 * procedure[命令合成]<br/>
	 * 查询表上的 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return runs
	 */
	default List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		Procedure query = new Procedure();
		query.setCatalog(catalog);
		query.setSchema(schema);
		return buildQueryProceduresRun(runtime, greedy, query);
	}

    /**
     * procedure[结果集封装]<br/>
     * Procedure 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initProcedureFieldRefer();

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> previous, Procedure query, DataSet set) throws Exception;
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果集构造 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Procedure query, DataSet set) throws Exception;

	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> previous, Procedure query) throws Exception;
	/**
	 * procedure[结果集封装]<br/>
	 * 根据驱动内置接口补充 Procedure
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Procedure query) throws Exception;
	/**
	 *
	 * procedure[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure Procedure
	 * @return ddl
	 */
	List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
	/**
	 * procedure[命令合成]<br/>
	 * 查询存储DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @return List
	 */
	List<Run> buildQueryDdlRun(DataRuntime runtime, Procedure procedure) throws Exception;
	/**
	 * procedure[结果集封装]<br/>
	 * 查询 Procedure DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param meta Procedure
	 * @param set 查询结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Procedure meta, List<String> ddls, DataSet set);

	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Procedure
	 */
	<T extends Procedure> T init(DataRuntime runtime, int index, T meta, Procedure query, DataRow row);
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return Procedure
	 */
	default <T extends Procedure> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		meta.setCatalog(catalog);
		meta.setSchema(schema);
		return init(runtime, index, meta, catalog, schema, row);
	}
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Procedure
	 */
	<T extends Procedure> T detail(DataRuntime runtime, int index, T meta, Procedure query, DataRow row);
	/**
	 * procedure[结果集封装]<br/>
	 * 根据查询结果封装procedure对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Procedure
	 */
	default <T extends Procedure> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		meta.setCatalog(catalog);
		meta.setSchema(schema);
		return detail(runtime, index, meta, catalog, schema, row);
	}
	/* *****************************************************************************************************************
	 * 													function
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
	<T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Function query);
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		Function query = new Function(catalog, schema, pattern);
		return functions(runtime, random, greedy, query);
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
	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Function query);
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		Function query = new Function(catalog, schema, pattern);
		return functions(runtime, random, query);
	}
	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	List<Run> buildQueryFunctionsRun(DataRuntime runtime, boolean greedy, Function query) ;
	/**
	 * function[命令合成]<br/>
	 * 查询表上的 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return runs
	 */
	default List<Run> buildQueryFunctionsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		Function query = new Function(catalog, schema, pattern);
		return buildQueryFunctionsRun(runtime, greedy, query);
	}

    /**
     * Function[结果集封装]<br/>
     * Function 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initFunctionFieldRefer();
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
	<T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> previous, Function query, DataSet set) throws Exception;
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
	default <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
		Function query = new Function(catalog, schema, null);
		return functions(runtime, index, create, previous, query, set);
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
	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Function query, DataSet set) throws Exception;

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
	default <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog catalog, Schema schema, DataSet set) throws Exception {
		Function query = new Function(catalog, schema, null);
		return functions(runtime, index, create, previous, query, set);
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
	<T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> previous, Function query) throws Exception;

	/**
	 * function[结果集封装]<br/>
	 * 根据驱动内置接口补充 Function
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Function query) throws Exception;
	/**
	 *
	 * function[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param function Function
	 * @return ddl
	 */
	List<String> ddl(DataRuntime runtime, String random, Function function);
	/**
	 * function[命令合成]<br/>
	 * 查询函数DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	List<Run> buildQueryDdlRun(DataRuntime runtime, Function meta) throws Exception;
	/**
	 * function[结果集封装]<br/>
	 * 查询 Function DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param meta Function
	 * @param set 查询结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Function meta, List<String> ddls, DataSet set);

	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Function
	 */
	<T extends Function> T init(DataRuntime runtime, int index, T meta, Function query, DataRow row);
	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return Function
	 */
	default <T extends Function> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return init(runtime, index, meta, new Function(catalog, schema, null), row);
	}
	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Function
	 */
	<T extends Function> T detail(DataRuntime runtime, int index, T meta, Function query, DataRow row);
	/**
	 * function[结果集封装]<br/>
	 * 根据查询结果封装function对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Function
	 */
	default <T extends Function> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return detail(runtime, index, meta, new Function(catalog, schema, null), row);
	}

	/* *****************************************************************************************************************
	 * 													sequence
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
	<T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Sequence query);
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		return sequences(runtime, random, greedy, new Sequence(catalog, schema, pattern));
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
	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Sequence query);
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return  LinkedHashMap
	 * @param <T> Index
	 */
	default <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
		return sequences(runtime, random, new Sequence(catalog, schema, pattern));
	}
	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param query 查询条件 根据metadata属性
	 * @return runs
	 */
	List<Run> buildQuerySequencesRun(DataRuntime runtime, boolean greedy, Sequence query) ;
	/**
	 * sequence[命令合成]<br/>
	 * 查询表上的 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 名称统配符或正则
	 * @return runs
	 */
	default List<Run> buildQuerySequencesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) {
		return buildQuerySequencesRun(runtime, greedy, new Sequence(catalog, schema, pattern));
	}

    /**
     * sequence[结果集封装]<br/>
     * Sequence 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initSequenceFieldRefer();

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
	<T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> previous, Sequence query, DataSet set) throws Exception;

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
	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Sequence query, DataSet set) throws Exception;

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> previous, Sequence query) throws Exception;

	/**
	 * sequence[结果集封装]<br/>
	 * 根据驱动内置接口补充 Sequence
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param previous 上一步查询结果
	 * @return LinkedHashMap
	 * @throws Exception 异常
	 */
	<T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Sequence query) throws Exception;
	/**
	 *
	 * sequence[调用入口]<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Sequence
	 * @return ddl
	 */
	List<String> ddl(DataRuntime runtime, String random, Sequence meta);
	/**
	 * sequence[命令合成]<br/>
	 * 查询序列DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	List<Run> buildQueryDdlRun(DataRuntime runtime, Sequence meta) throws Exception;
	/**
	 * sequence[结果集封装]<br/>
	 * 查询 Sequence DDL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
	 * @param meta Sequence
	 * @param set 查询结果集
	 * @return List
	 */
	List<String> ddl(DataRuntime runtime, int index, Sequence meta, List<String> ddls, DataSet set);

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param query 查询条件 根据metadata属性
	 * @param row 查询结果集
	 * @return Sequence
	 */
	<T extends Sequence> T init(DataRuntime runtime, int index, T meta, Sequence query, DataRow row);

	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return Sequence
	 */
	default <T extends Sequence> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return init(runtime, index, meta, new Sequence(catalog, schema, null), row);
	}
	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Sequence
	 */
	<T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Sequence query, DataRow row);
	/**
	 * sequence[结果集封装]<br/>
	 * 根据查询结果封装sequence对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Sequence
	 */
	default <T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return detail(runtime, index, meta, new Sequence(catalog, schema, null), row);
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
	 * 是否支持DDL合并
	 * @return boolean
	 */
	default boolean slice() {
		return false;
	}
	default boolean slice(boolean slice) {
		return slice && slice();
	}

	/**
	 *
	 * 根据 catalog, schema, name检测tables集合中是否存在
	 * @param metas metas
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param name name
	 * @return 如果存在则返回Table 不存在则返回null
	 * @param <T> Table
	 */
	<T extends Metadata> T search(List<T> metas, Catalog catalog, Schema schema, String name);
	/**
	 * 执行命令
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param meta Metadata(表,列等)
	 * @param action 执行命令
	 * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
	 * @return boolean
	 */
	boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, Run run);
	boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.DDL action, List<Run> runs);


    /**
     * 解析DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param type Metadata类型
     * @param ddl ddl
     * @param configs 其他配置
     * @return T
     * @param <T> T
     */
    <T extends Metadata> T parse(DataRuntime runtime, Class<T> type, String ddl, ConfigStore configs);
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
    boolean create(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[调用入口]<br/>
     * 修改Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean alter(DataRuntime runtime, Catalog meta) throws Exception;
    /**
     * catalog[调用入口]<br/>
     * 删除Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean drop(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[调用入口]<br/>
     * 重命名Catalog,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param origin 原Catalog
     * @param name 新名称
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean rename(DataRuntime runtime, Catalog origin, String name) throws Exception;

    /**
     * catalog[命令合成]<br/>
     * 创建Catalog<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildCreateRun(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[命令合成]<br/>
     * 修改Catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAlterRun(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[命令合成]<br/>
     * 重命名
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildRenameRun(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[命令合成]<br/>
     * 删除Catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildDropRun(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[命令合成-子流程]<br/>
     * 创建Catalog完成后追加Catalog备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Catalog meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAppendCommentRun(DataRuntime runtime, Catalog meta) throws Exception;
    /**
     * catalog[命令合成-子流程]<br/>
     * 创建Catalog完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Catalog meta) throws Exception;

    /**
     * catalog[命令合成-子流程]<br/>
     * 修改备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog Catalog
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildChangeCommentRun(DataRuntime runtime, Catalog catalog) throws Exception;

    /**
     * catalog[命令合成-子流程]<br/>
     * 添加备注(部分数据库需要区分添加还是修改)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog Catalog
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildAddCommentRun(DataRuntime runtime, Catalog catalog) throws Exception {
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
    StringBuilder checkCatalogExists(DataRuntime runtime, StringBuilder builder, boolean exists);

    /**
     * catalog[命令合成-子流程]<br/>
     * 创建Catalog engine
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    StringBuilder engine(DataRuntime runtime, StringBuilder builder, Catalog meta);

    /**
     * catalog[命令合成-子流程]<br/>
     * 编码
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    StringBuilder charset(DataRuntime runtime, StringBuilder builder, Catalog meta);

    /**
     * catalog[命令合成-子流程]<br/>
     * Catalog备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    StringBuilder comment(DataRuntime runtime, StringBuilder builder, Catalog meta);

    /**
     * catalog[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Catalog
     * @return StringBuilder
     */
    StringBuilder property(DataRuntime runtime, StringBuilder builder, Catalog meta);


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
    boolean create(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[调用入口]<br/>
     * 修改Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean alter(DataRuntime runtime, Schema meta) throws Exception;
    /**
     * schema[调用入口]<br/>
     * 删除Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean drop(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[调用入口]<br/>
     * 重命名Schema,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param origin 原Schema
     * @param name 新名称
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean rename(DataRuntime runtime, Schema origin, String name) throws Exception;

    /**
     * schema[命令合成]<br/>
     * 创建Schema<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildCreateRun(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[命令合成]<br/>
     * 修改Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAlterRun(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[命令合成]<br/>
     * 重命名
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildRenameRun(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[命令合成]<br/>
     * 删除Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildDropRun(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[命令合成-子流程]<br/>
     * 创建Schema完成后追加Schema备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Schema meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAppendCommentRun(DataRuntime runtime, Schema meta) throws Exception;
    /**
     * schema[命令合成-子流程]<br/>
     * 创建Schema完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Schema meta) throws Exception;

    /**
     * schema[命令合成-子流程]<br/>
     * 修改备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param schema Schema
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildChangeCommentRun(DataRuntime runtime, Schema schema) throws Exception;

    /**
     * schema[命令合成-子流程]<br/>
     * 添加备注(部分数据库需要区分添加还是修改)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param schema Schema
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildAddCommentRun(DataRuntime runtime, Schema schema) throws Exception {
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
    StringBuilder checkSchemaExists(DataRuntime runtime, StringBuilder builder, boolean exists);

    /**
     * schema[命令合成-子流程]<br/>
     * 创建Schema engine
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    StringBuilder engine(DataRuntime runtime, StringBuilder builder, Schema meta);

    /**
     * schema[命令合成-子流程]<br/>
     * 编码
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    StringBuilder charset(DataRuntime runtime, StringBuilder builder, Schema meta);

    /**
     * schema[命令合成-子流程]<br/>
     * Schema备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    StringBuilder comment(DataRuntime runtime, StringBuilder builder, Schema meta);

    /**
     * schema[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Schema
     * @return StringBuilder
     */
    StringBuilder property(DataRuntime runtime, StringBuilder builder, Schema meta);

    /* *****************************************************************************************************************
     * 													database
     * -----------------------------------------------------------------------------------------------------------------
     * boolean create(Database database) throws Exception
     * boolean alter(Database database) throws Exception
     * boolean drop(Database database) throws Exception
     * boolean rename(Database origin, String name) throws Exception
     ******************************************************************************************************************/

    /**
     * database[调用入口]<br/>
     * 创建Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean create(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[调用入口]<br/>
     * 修改Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean alter(DataRuntime runtime, Database meta) throws Exception;
    /**
     * database[调用入口]<br/>
     * 删除Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean drop(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[调用入口]<br/>
     * 重命名Database,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param origin 原Database
     * @param name 新名称
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    boolean rename(DataRuntime runtime, Database origin, String name) throws Exception;

    /**
     * database[命令合成]<br/>
     * 创建Database<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildCreateRun(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[命令合成]<br/>
     * 修改Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAlterRun(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[命令合成]<br/>
     * 重命名
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildRenameRun(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[命令合成]<br/>
     * 删除Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildDropRun(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[命令合成-子流程]<br/>
     * 创建Database完成后追加Database备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Database meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAppendCommentRun(DataRuntime runtime, Database meta) throws Exception;
    /**
     * database[命令合成-子流程]<br/>
     * 创建Database完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Database meta) throws Exception;

    /**
     * database[命令合成-子流程]<br/>
     * 修改备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param database Database
     * @return sql
     * @throws Exception 异常
     */
    List<Run> buildChangeCommentRun(DataRuntime runtime, Database database) throws Exception;

    /**
     * database[命令合成-子流程]<br/>
     * 添加备注(部分数据库需要区分添加还是修改)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param database Database
     * @return sql
     * @throws Exception 异常
     */
    default List<Run> buildAddCommentRun(DataRuntime runtime, Database database) throws Exception {
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
    StringBuilder checkDatabaseExists(DataRuntime runtime, StringBuilder builder, boolean exists);

    /**
     * database[命令合成-子流程]<br/>
     * 创建Database engine
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    StringBuilder engine(DataRuntime runtime, StringBuilder builder, Database meta);

    /**
     * database[命令合成-子流程]<br/>
     * 编码
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    StringBuilder charset(DataRuntime runtime, StringBuilder builder, Database meta);

    /**
     * database[命令合成-子流程]<br/>
     * Database备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    StringBuilder comment(DataRuntime runtime, StringBuilder builder, Database meta);

    /**
     * database[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta Database
     * @return StringBuilder
     */
    StringBuilder property(DataRuntime runtime, StringBuilder builder, Database meta);

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Table table) throws Exception
	 * boolean alter(Table table) throws Exception
	 * boolean drop(Table table) throws Exception
	 * boolean rename(Table origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * table[调用入口]<br/>
	 * 创建表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[调用入口]<br/>
	 * 修改表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, Table meta) throws Exception;
	/**
	 * table[调用入口]<br/>
	 * 删除表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[调用入口]<br/>
	 * 重命名表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, Table origin, String name) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
	 * @param meta 表
	 * @return String
	 */
	default String keyword(Metadata meta) {
		return meta.getKeyword();
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
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 修改表 只生成修改表本身属性 不生成关于列及索引的
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @param columns 列
	 * @param slice 是否只生成片段(true:不含alter table部分，用于DDL合并)
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns, boolean slice) throws Exception;
	default List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns) throws Exception {
		return buildAlterRun(runtime, meta, columns, false);
	}

	/**
	 * table[命令合成]<br/>
	 * 重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成]<br/>
	 * 删除表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta) throws Exception;
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 添加备注(部分数据库需要区分添加还是修改)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	default List<Run> buildAddCommentRun(DataRuntime runtime, Table table) throws Exception {
		return buildChangeCommentRun(runtime, table);
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
	StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * table[命令合成-子流程]<br/>
	 * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 */
	void checkPrimary(DataRuntime runtime, Table table);

	/**
	 * table[命令合成-子流程]<br/>
	 * 定义表的主键标识,在创建表的DDL结尾部分(注意不要跟列定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 engine
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder engine(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 body部分包含column index
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder body(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 columns部分
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 创建表 索引部分，与buildAppendIndexRun二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder indexes(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 数据模型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder keys(DataRuntime runtime, StringBuilder builder, Table meta);
	/**
	 * table[命令合成-子流程]<br/>
	 * 分桶方式
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder distribution(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 物化视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 */
	StringBuilder materialize(DataRuntime runtime, StringBuilder builder, Table meta);
    /**
     * table[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta 表
     * @return StringBuilder
     */
    StringBuilder property(DataRuntime runtime, StringBuilder builder, Table meta);
    /**
     * table[命令合成-子流程]<br/>
     * 扩展属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta 表
     * @return StringBuilder
     */
    StringBuilder option(DataRuntime runtime, StringBuilder builder, Table meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 主表设置分区依据(分区依据列)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(相关主表)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;

	/**
	 * table[命令合成-子流程]<br/>
	 * 子表执行分区依据(分区依据值)如CREATE TABLE hr_user_fi PARTITION OF hr_user FOR VALUES IN ('FI')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder partitionFor(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;
	/**
	 * table[命令合成-子流程]<br/>
	 * 继承自table.getInherit
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 表
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder inherit(DataRuntime runtime, StringBuilder builder, Table meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(View view) throws Exception
	 * boolean alter(View view) throws Exception
	 * boolean drop(View view) throws Exception
	 * boolean rename(View origin, String name) throws Exception
	 ******************************************************************************************************************/
	/**
	 * view[调用入口]<br/>
	 * 创建视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, View view) throws Exception;
	/**
	 * view[调用入口]<br/>
	 * 修改视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[调用入口]<br/>
	 * 删除视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[调用入口]<br/>
	 * 重命名视图,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 视图
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, View origin, String name) throws Exception;

	/**
	 * view[命令合成]<br/>
	 * 创建视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, View view) throws Exception;
	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图头部
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder buildCreateRunHead(DataRuntime runtime, StringBuilder builder, View meta) throws Exception;
	/**
	 * view[命令合成-子流程]<br/>
	 * 创建视图选项
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 视图
	 * @return StringBuilder
	 * @throws Exception 异常
	 */
	StringBuilder buildCreateRunOption(DataRuntime runtime, StringBuilder builder, View meta) throws Exception;
	/**
	 * view[命令合成]<br/>
	 * 修改视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成]<br/>
	 * 重命名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成]<br/>
	 * 删除视图
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成-子流程]<br/>
	 * 添加视图备注(视图创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成-子流程]<br/>
	 * 修改备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param view 视图
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, View view) throws Exception;

	/**
	 * view[命令合成-子流程]<br/>
	 * 创建或删除视图之前  检测视图是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * view[命令合成-子流程]<br/>
	 * 视图备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param view 视图
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, View view);

	/* *****************************************************************************************************************
	 * 													MasterTable
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(MasterTable meta) throws Exception
	 * boolean alter(MasterTable meta) throws Exception
	 * boolean drop(MasterTable meta) throws Exception
	 * boolean rename(MasterTable origin, String name) throws Exception
	 ******************************************************************************************************************/
	/**
	 * master table[调用入口]<br/>
	 * 创建主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, MasterTable meta) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * 修改主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, MasterTable meta) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * 删除主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, MasterTable meta) throws Exception;

	/**
	 * master table[调用入口]<br/>
	 * 重命名主表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception;

	/**
	 * master table[命令合成]<br/>
	 * 创建主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, MasterTable table) throws Exception;

	/**
	 * master table[命令合成]<br/>
	 * 删除主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, MasterTable table) throws Exception;
	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, MasterTable table) throws Exception;
	/**
	 * master table[命令合成-子流程]<br/>
	 * 主表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, MasterTable table) throws Exception;

	/**
	 * master table[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table) throws Exception;

	/**
	 * master table[命令合成-子流程]<br/>
	 * 修改主表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table) throws Exception;

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, PartitionTable meta) throws Exception
	 * boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception
	 * boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception
	 * boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean create(DataRuntime runtime, PartitionTable meta) throws Exception;

	/**
	 * partition table[调用入口]<br/>
	 * 修改分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, PartitionTable meta) throws Exception;

	/**
	 * partition table[调用入口]<br/>
	 * 删除分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception;

	/**
	 * partition table[调用入口]<br/>
	 * 创建分区表,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, PartitionTable origin, String name) throws Exception;

	/**
	 * partition table[命令合成]<br/>
	 * 创建分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成]<br/>
	 * 修改分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成-]<br/>
	 * 删除分区表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception;
	/**
	 * partition table[命令合成]<br/>
	 * 分区表重命名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/**
	 * partition table[命令合成-子流程]<br/>
	 * 修改分区表备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception;

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Column meta) throws Exception
	 * boolean alter(DataRuntime runtime, Table table, Column meta) throws Exception
	 * boolean alter(DataRuntime runtime, Column meta) throws Exception
	 * boolean drop(DataRuntime runtime, Column meta) throws Exception
	 *
	 * private boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	/**
	 * column[调用入口]<br/>
	 * 添加列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean add(DataRuntime runtime, Column meta) throws Exception;

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param trigger 修改异常时，是否触发监听器
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, Table table, Column meta, boolean trigger) throws Exception;
	default boolean alter(DataRuntime runtime, Table table, Column meta) throws Exception {
		return alter(runtime, table, meta, true);
	}

	/**
	 * column[调用入口]<br/>
	 * 修改列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean alter(DataRuntime runtime, Column meta) throws Exception;

	/**
	 * column[调用入口]<br/>
	 * 删除列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean drop(DataRuntime runtime, Column meta) throws Exception;

	/**
	 * column[调用入口]<br/>
	 * 重命名列,执行的命令通过meta.ddls()返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 列
	 * @param name 新名称
	 * @return boolean 是否执行成功
	 * @throws Exception DDL异常
	 */
	boolean rename(DataRuntime runtime, Column origin, String name) throws Exception;

	/**
	 * column[命令合成]<br/>
	 * 添加列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成]<br/>
	 * 修改列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成]<br/>
	 * 删除列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成]<br/>
	 * 修改列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeTypeRun(DataRuntime runtime, Column column, boolean slice) throws Exception;
	default List<Run> buildChangeTypeRun(DataRuntime runtime, Column column) throws Exception {
		return buildChangeTypeRun(runtime, column, false);
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改表的关键字
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return String
	 */
	String alterColumnKeyword(DataRuntime runtime);

	/**
	 * column[命令合成-子流程]<br/>
	 * 添加列引导<br/>
	 * alter table sso_user [add column] type_code int
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列
	 * @return String
	 */
    StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column column);
    default StringBuilder addColumnClose(DataRuntime runtime, StringBuilder builder, Column column) {
        return builder;
    }

	/**
	 * column[命令合成-子流程]<br/>
	 * 删除列引导<br/>
	 * alter table sso_user [drop column] type_code
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列
	 * @return String
	 */
	StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeNullableRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildAppendCommentRun(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 取消自增
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column, boolean slice) throws Exception;

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列，依次拼接下面几个属性注意不同数据库可能顺序不一样
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param action 区分 创建与修改过程有区别  如有些数据库修改时不支持NULL NOT NULL(如clickhouse)
	 * @return StringBuilder
	 */
	StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:创建或删除列之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:数据类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有长度<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type 数据类型
	 * @return boolean
	 */
	int ignoreLength(DataRuntime runtime, TypeMetadata type);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:是否忽略有效位数<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
	int ignorePrecision(DataRuntime runtime, TypeMetadata type);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:定义列:是否忽略小数位<br/>
	 * 不直接调用 用来覆盖dataTypeMetadataRefer(DataRuntime runtime, TypeMetadata meta)<br/>
	 * 注意父类中会根据具体数据库和数据类型(alias,category) 确定refer 如果父类没有设置 再用子类中统一的默认值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type TypeMetadata
	 * @return boolean
	 */
	int ignoreScale(DataRuntime runtime, TypeMetadata type);
	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:聚合类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder aggregation(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:列数据类型定义
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @param type 数据类型(已经过转换)
	 * @param ignoreLength 是否忽略长度
	 * @param ignorePrecision 是否忽略有效位数
	 * @param ignoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:非空
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:编码
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:默认值
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列的主键标识(注意不要跟表定义中的主键重复)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 列定义:唯一索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param meta 列
	 * @return StringBuilder
	 */
	StringBuilder unique(DataRuntime runtime, StringBuilder builder, Column meta);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:递增列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:更新行事件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:位置
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column);

	/**
	 * column[命令合成-子流程]<br/>
	 * 定义列:备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param column 列
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column);

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Tag meta) throws Exception
	 * boolean alter(DataRuntime runtime, Tag table, Column meta) throws Exception
	 * boolean alter(DataRuntime runtime, Tag meta) throws Exception
	 * boolean drop(DataRuntime runtime, Tag meta) throws Exception
	 *
	 * private boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception
	 ******************************************************************************************************************/

	/**
	 * tag[调用入口]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Tag meta) throws Exception;

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @param trigger 修改异常时，是否触发监听器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, Tag meta, boolean trigger) throws Exception;
	default boolean alter(DataRuntime runtime, Table table, Tag meta) throws Exception {
		return alter(runtime, table, meta, true);
	}

	/**
	 * tag[调用入口]<br/>
	 * 修改标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Tag meta) throws Exception;

	/**
	 * tag[调用入口]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 标签
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Tag meta) throws Exception;

	/**
	 * tag[调用入口]<br/>
	 * 重命名标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 原标签
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Tag origin, String name) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 添加标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;
	/**
	 * tag[命令合成]<br/>
	 * 修改标签
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 删除标签
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;
	/**
	 * tag[命令合成]<br/>
	 * 修改标签名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;
	/**
	 * tag[命令合成]<br/>
	 * 修改默认值
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param tag 标签
	 * @return String
	 */
	List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag, boolean slice) throws Exception;

	/**
	 * tag[命令合成]<br/>
	 * 创建或删除标签之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists);

	/**
	 * //TODO 放在下一级 metadata引用
	 * ddl过程 默认值 检测适配 内置函数 如mysql.CURRENT_TIMESTAMP 转换成 oracle.sysdate
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param def 默认值
	 * @return SQL_BUILD_IN_VALUE
	 */
	default SQL_BUILD_IN_VALUE checkDefaultBuildInValue(DataRuntime runtime, Object def) {
		SQL_BUILD_IN_VALUE result = null;
		if(null != def) {
			String chk = def.toString().toUpperCase().trim();
			if("CURRENT_TIMESTAMP".equals(chk)
				|| "CURRENT TIMESTAMP".equals(chk)
				|| "SYSDATE".equals(chk)
				|| "NOW()".equals(chk)
				|| "NOW".equals(chk)
				|| "SYSTIMESTAMP".equals(chk)
				|| "GETDATE()".equals(chk)
				|| chk.contains("DATETIME(")
				) {
				result = SQL_BUILD_IN_VALUE.CURRENT_DATETIME;
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception
	 * boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception
	 * boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception
	 * boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception
	 * boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param table 表
	 * @param origin 原主键
	 * @param meta 新主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, PrimaryKey origin, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 修改主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	default boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception {
		return alter(runtime, table, table.getPrimaryKey(), meta);
	}

	/**
	 * primary[调用入口]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 主键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, PrimaryKey meta) throws Exception;

	/**
	 * primary[调用入口]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 主键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, PrimaryKey origin, String name) throws Exception;
	/**
	 * primary[命令合成]<br/>
	 * 添加主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary, boolean slice) throws Exception;
	/**
	 * primary[命令合成]<br/>
	 * 创建完表后，添加主键，与列主键标识，表主键标识三选一<br/>
	 * 大部分情况调用buildAddRun<br/>
	 * 默认不调用，大部分数据库在创建列或表时可以直接标识出主键<br/>
	 * 只有在创建表过程中不支持创建主键的才需要实现这个方法
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	default List<Run> buildAppendPrimaryRun(DataRuntime runtime, Table meta) throws Exception {
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
	List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta, boolean slice) throws Exception;
	default List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta) throws Exception {
		return buildAlterRun(runtime, origin, meta, false);
	}
	default List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey meta) throws Exception {
		return buildAlterRun(runtime, null, meta);
	}

	/**
	 * primary[命令合成]<br/>
	 * 删除主键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @param slice 是否只生成片段(true:不含alter table部分，用于DDL合并)
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary, boolean slice) throws Exception;
	default List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception {
		return buildDropRun(runtime, primary, false);
	}

	/**
	 * primary[命令合成]<br/>
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param primary 主键
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception;

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add((DataRuntime runtime, ForeignKey meta) throws Exception
	 * boolean alter((DataRuntime runtime, ForeignKey meta) throws Exception
	 * boolean alter(Table table, ForeignKey meta) throws Exception;
	 * boolean drop((DataRuntime runtime, ForeignKey meta) throws Exception
	 * boolean rename((DataRuntime runtime, ForeignKey origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * foreign[调用入口]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 修改外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[调用入口]<br/>
	 * 重命名外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 外键
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, ForeignKey origin, String name) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 添加外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键
	 * @param meta 外键
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 删除外键
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/**
	 * foreign[命令合成]<br/>
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 外键
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, ForeignKey meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Index index) throws Exception
	 * boolean alter(DataRuntime runtime, Index index) throws Exception
	 * boolean drop(DataRuntime runtime, Index index) throws Exception
	 * boolean rename(DataRuntime runtime, Index origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * index[调用入口]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 修改索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[调用入口]<br/>
	 * 重命名索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 索引
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Index origin, String name) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 创建表过程添加索引,表创建完成后添加索引,于表内索引index(DataRuntime, StringBuilder, Table)二选一
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 表
	 * @return String
	 */
	List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 添加索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Index meta) throws Exception;
	/**
	 * index[命令合成]<br/>
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 删除索引
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[命令合成]<br/>
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Index meta) throws Exception;

	/**
	 * index[命令合成-子流程]<br/>
	 * 索引类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta);
	/**
	 * index[命令合成-子流程]<br/>
	 * 索引属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder property(DataRuntime runtime, StringBuilder builder, Index meta);
	/**
	 * index[命令合成-子流程]<br/>
	 * 索引备注
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 索引
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta);

	/**
	 * table[命令合成-子流程]<br/>
	 * 创建或删除表之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	StringBuilder checkIndexExists(DataRuntime runtime, StringBuilder builder, boolean exists);
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(DataRuntime runtime, Constraint meta) throws Exception
	 * boolean alter(DataRuntime runtime, Constraint meta) throws Exception
	 * boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception;
	 * boolean drop(DataRuntime runtime, Constraint meta) throws Exception
	 * boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * constraint[调用入口]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 修改约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Table table, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[调用入口]<br/>
	 * 重命名约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 约束
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Constraint origin, String name) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 添加约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	List<Run> buildAddRun(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 删除约束
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Constraint meta) throws Exception;

	/**
	 * constraint[命令合成]<br/>
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 约束
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Constraint meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Trigger meta) throws Exception
	 * boolean alter(DataRuntime runtime, Trigger meta) throws Exception
	 * boolean drop(DataRuntime runtime, Trigger meta) throws Exception
	 * boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * trigger[调用入口]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean add(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[调用入口]<br/>
	 * 修改触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[调用入口]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[调用入口]<br/>
	 * 重命名触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 触发器
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Trigger origin, String name) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 添加触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 删除触发器
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildDropRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成]<br/>
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @return List
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Trigger meta) throws Exception;

	/**
	 * trigger[命令合成-子流程]<br/>
	 * 触发级别(行或整个命令)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 触发器
	 * @param builder builder
	 * @return StringBuilder
	 */
	StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta);

	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Procedure meta) throws Exception
	 * boolean alter(DataRuntime runtime, Procedure meta) throws Exception
	 * boolean drop(DataRuntime runtime, Procedure meta) throws Exception
	 * boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception
	 ******************************************************************************************************************/

	/**
	 * procedure[调用入口]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean create(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[调用入口]<br/>
	 * 修改存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[调用入口]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[调用入口]<br/>
	 * 重命名存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 存储过程
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Procedure origin, String name) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 添加存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 删除存储过程
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成]<br/>
	 * 修改存储过程名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 存储过程
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Procedure meta) throws Exception;

	/**
	 * procedure[命令合成-子流程]<br/>
	 * 生在输入输出参数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param parameter parameter
	 */
	StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter);
	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Function meta) throws Exception
	 * boolean alter(DataRuntime runtime, Function meta) throws Exception
	 * boolean drop(DataRuntime runtime, Function meta) throws Exception
	 * boolean rename(DataRuntime runtime, Function origin, String name)  throws Exception
	 ******************************************************************************************************************/

	/**
	 * function[调用入口]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean create(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[调用入口]<br/>
	 * 修改函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[调用入口]<br/>
	 * 删除函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[调用入口]<br/>
	 * 重命名函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 函数
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Function origin, String name) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 添加函数
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 删除函数
	 * @param meta 函数
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception;

	/**
	 * function[命令合成]<br/>
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 函数
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Function meta) throws Exception;

	/* *****************************************************************************************************************
	 * 													sequence
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(DataRuntime runtime, Sequence meta) throws Exception
	 * boolean alter(DataRuntime runtime, Sequence meta) throws Exception
	 * boolean drop(DataRuntime runtime, Sequence meta) throws Exception
	 * boolean rename(DataRuntime runtime, Sequence origin, String name)  throws Exception
	 ******************************************************************************************************************/

	/**
	 * sequence[调用入口]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean create(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[调用入口]<br/>
	 * 修改序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean alter(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[调用入口]<br/>
	 * 删除序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean drop(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[调用入口]<br/>
	 * 重命名序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param origin 序列
	 * @param name 新名称
	 * @return 是否执行成功
	 * @throws Exception 异常
	 */
	boolean rename(DataRuntime runtime, Sequence origin, String name) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 添加序列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return List
	 */
	List<Run> buildAlterRun(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 删除序列
	 * @param meta 序列
	 * @return String
	 */
	List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception;

	/**
	 * sequence[命令合成]<br/>
	 * 修改序列名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 序列
	 * @return String
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Sequence meta) throws Exception;

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
	boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.Authorize action, Run run);
	boolean execute(DataRuntime runtime, String random, Metadata meta, ACTION.Authorize action, List<Run> runs);

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
	boolean create(DataRuntime runtime, Role role) throws Exception;

	/**
	 * role[调用入口]<br/>
	 * 角色重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	boolean rename(DataRuntime runtime, Role origin, Role update) throws Exception;

	/**
	 * role[调用入口]<br/>
	 * 删除角色
	 * @param role 角色
	 * @return boolean
	 */
	boolean drop(DataRuntime runtime, Role role) throws Exception;

	/**
	 * role[调用入口]<br/>
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
    <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, Role query) throws Exception;
	/**
	 * role[调用入口]<br/>
	 * 查询角色
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 角色名
	 * @return List
	 */
	default <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) throws Exception {
		Role query = new Role();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(pattern);
		return roles(runtime, random, greedy, query);
	}

	/**
	 * role[调用入口]<br/>
	 * 查询角色
	 * @return List
	 */
	default <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy) throws Exception {
		return roles(runtime, random, greedy, null, null, null);
	}
	/**
	 * role[调用入口]<br/>
	 * 查询角色
	 * @param pattern 角色名
	 * @return List
	 */
	default <T extends Role> List<T> roles(DataRuntime runtime, String random, boolean greedy, String pattern) throws Exception {
		return roles(runtime, random, greedy,null, null, pattern);
	}

	/**
	 * role[命令合成]<br/>
	 * 创建角色
	 * @param role 角色
	 * @return List
	 */
	List<Run> buildCreateRun(DataRuntime runtime, Role role) throws Exception;
	/**
	 * role[命令合成]<br/>
	 * 角色重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return List
	 */
	List<Run> buildRenameRun(DataRuntime runtime, Role origin, Role update) throws Exception;

	/**
	 * role[命令合成]<br/>
	 * 删除角色
	 * @param role 角色
	 * @return List
	 */
	List<Run> buildDropRun(DataRuntime runtime, Role role) throws Exception;

	/**
	 * role[命令合成]<br/>
	 * 查询角色
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	List<Run> buildQueryRolesRun(DataRuntime runtime, boolean greedy, Role query) throws Exception;
	/**
	 * role[命令合成]<br/>
	 * 查询角色
	 * @param pattern 角色名
	 * @return List
	 */
	default List<Run> buildQueryRolesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) throws Exception {
		return buildQueryRolesRun(runtime, greedy, new Role(catalog, schema, pattern));
	}

    /**
     * role[结果集封装]<br/>
     * Role 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initRoleFieldRefer();

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
	<T extends Role> List<T> roles(DataRuntime runtime, int index, boolean create, List<T> previous, Role query, DataSet set) throws Exception;
	/**
	 * role[结果集封装]<br/>
	 * 根据查询结果集构造 role
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryRolessRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception 异常
	 */
	default <T extends Role> List<T> roles(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> previous, DataSet set) throws Exception {
		return roles(runtime, index, create, previous, new Role(catalog, schema, null), set);
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
	<T extends Role> T init(DataRuntime runtime, int index, T meta, Role query, DataRow row);
	/**
	 * role[结果集封装]<br/>
	 * 根据查询结果封装 role 对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return Role
	 */
	default <T extends Role> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return init(runtime, index, meta, new Role(catalog, schema, null), row);
	}
	/**
	 * role[结果集封装]<br/>
	 * 根据查询结果封装 role 对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Role
	 */
	<T extends Role> T detail(DataRuntime runtime, int index, T meta, Role query, DataRow row);
	/**
	 * role[结果集封装]<br/>
	 * 根据查询结果封装 role 对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Role
	 */
	default <T extends Role> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return detail(runtime, index, meta, new Role(catalog, schema, null), row);
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
	boolean create(DataRuntime runtime, User user) throws Exception;

	/**
	 * user[调用入口]<br/>
	 * 创建用户
	 * @param name 用户名
	 * @param password 密码
	 * @return boolean
	 */
	default boolean create(DataRuntime runtime, String name, String password) throws Exception {
		return create(runtime, new User(name, password));
	}

	/**
	 * user[调用入口]<br/>
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	boolean rename(DataRuntime runtime, User origin, User update) throws Exception;

	/**
	 * user[调用入口]<br/>
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return boolean
	 */
	default boolean rename(DataRuntime runtime, String origin, String update)  throws Exception {
		return rename(runtime, new User(origin), new User(update));
	}

	/**
	 * user[调用入口]<br/>
	 * 删除用户
	 * @param user 用户
	 * @return boolean
	 */
	boolean drop(DataRuntime runtime, User user) throws Exception;
	/**
	 * user[调用入口]<br/>
	 * 删除用户
	 * @param user 用户名
	 * @return boolean
	 */
	default boolean drop(DataRuntime runtime, String user)  throws Exception {
		return drop(runtime, new User(user));
	}

	/**
	 * user[调用入口]<br/>
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	<T extends User> List<T> users(DataRuntime runtime, String random, boolean greedy, User query) throws Exception;
	/**
	 * user[调用入口]<br/>
	 * 查询用户
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param pattern 用户名
	 * @return List
	 */
	default <T extends User> List<T> users(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) throws Exception {
		User query = new User();
		query.setCatalog(catalog);
		query.setSchema(schema);
		query.setName(pattern);
		return users(runtime, random, greedy, query);
	}

	/**
	 * user[调用入口]<br/>
	 * 查询用户
	 * @return List
	 */
	default List<User> users(DataRuntime runtime, String random, boolean greedy) throws Exception {
		return users(runtime, random, greedy, null, null, null);
	}
	/**
	 * user[调用入口]<br/>
	 * 查询用户
	 * @param pattern 用户名
	 * @return List
	 */
	default <T extends User> List<T> users(DataRuntime runtime, String random, boolean greedy, String pattern) throws Exception {
		return users(runtime, random, greedy,null, null, pattern);
	}

	/**
	 * user[命令合成]<br/>
	 * 创建用户
	 * @param user 用户
	 * @return List
	 */
	List<Run> buildCreateRun(DataRuntime runtime, User user) throws Exception;
	/**
	 * user[命令合成]<br/>
	 * 用户重命名
	 * @param origin 原名
	 * @param update 新名
	 * @return List
	 */
	List<Run> buildRenameRun(DataRuntime runtime, User origin, User update) throws Exception;

	/**
	 * user[命令合成]<br/>
	 * 删除用户
	 * @param user 用户
	 * @return List
	 */
	List<Run> buildDropRun(DataRuntime runtime, User user) throws Exception;

	/**
	 * user[命令合成]<br/>
	 * 查询用户
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	List<Run> buildQueryUsersRun(DataRuntime runtime, boolean greedy, User query) throws Exception;
	/**
	 * user[命令合成]<br/>
	 * 查询用户
	 * @param pattern 用户名
	 * @return List
	 */
	default List<Run> buildQueryUsersRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) throws Exception {
		return buildQueryUsersRun(runtime, greedy, new User(catalog, schema, pattern));
	}

    /**
     * user[结果集封装]<br/>
     * User 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initUserFieldRefer();

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
	<T extends User> List<T> users(DataRuntime runtime, int index, boolean create, List<T> previous, User query, DataSet set) throws Exception;
	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果集构造 user
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryUserssRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception 异常
	 */
	default <T extends User> List<T> users(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> previous, DataSet set) throws Exception {
		return users(runtime, index, create, previous,  new User(catalog, schema, null), set);
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
	<T extends User> T init(DataRuntime runtime, int index, T meta, User query, DataRow row);
	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果封装 user 对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param row 查询结果集
	 * @return User
	 */
	default <T extends User> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return init(runtime, index, meta, new User(catalog, schema, null), row);
	}
	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果封装 user 对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return User
	 */
	<T extends User> T detail(DataRuntime runtime, int index, T meta, User query, DataRow row);
	/**
	 * user[结果集封装]<br/>
	 * 根据查询结果封装 user 对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return User
	 */
	default <T extends User> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
		return detail(runtime, index, meta, new User(catalog, schema, null), row);
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
	<T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, Privilege query) throws Exception;
	/**
	 * privilege[调用入口]<br/>
	 * 查询用户权限
	 * @param user 用户
	 * @return List
	 */
	default <T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, User user) throws Exception {
		Privilege query = new Privilege();
		query.setUser(user);
		return privileges(runtime, random, greedy, query);
	}

	/**
	 * privilege[调用入口]<br/>
	 * 查询用户权限
	 * @param user 用户
	 * @return List
	 */
	default <T extends Privilege> List<T> privileges(DataRuntime runtime, String random, boolean greedy, String user) throws Exception {
		return privileges(runtime, random, greedy, new User(user));
	}

	/**
	 * privilege[命令合成]<br/>
	 * 查询用户权限
	 * @param query 查询条件 根据metadata属性
	 * @return List
	 */
	List<Run> buildQueryPrivilegesRun(DataRuntime runtime, boolean greedy, Privilege query) throws Exception;

	/**
	 * privilege[命令合成]<br/>
	 * 查询用户权限
	 * @param user 用户
	 * @return List
	 */
	default List<Run> buildQueryPrivilegesRun(DataRuntime runtime, boolean greedy, User user) throws Exception {
		return buildQueryPrivilegesRun(runtime, greedy, new Privilege(user));
	}

    /**
     * privilege[结果集封装]<br/>
     * Privilege 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    MetadataFieldRefer initPrivilegeFieldRefer();

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
	<T extends Privilege> List<T> privileges(DataRuntime runtime, int index, boolean create, List<T> previous, Privilege query, DataSet set) throws Exception;

	/**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果集构造 Trigger
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param user 用户
	 * @param previous 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 * @throws Exception 异常
	 */
	default <T extends Privilege> List<T> privileges(DataRuntime runtime, int index, boolean create, List<T> previous, User user, DataSet set) throws Exception {
		return privileges(runtime, index, create, previous, new Privilege(user), set);
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
	<T extends Privilege> T init(DataRuntime runtime, int index, T meta, Privilege query, DataRow row);

	/**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果封装Privilege对象,只封装catalog,schema,name等基础属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param user 用户
	 * @param row 查询结果集
	 * @return Privilege
	 */
	default <T extends Privilege> T init(DataRuntime runtime, int index, T meta, User user, DataRow row) {
		return init(runtime, index, meta, new Privilege(user), row);
	}
	/**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果封装Privilege对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Privilege
	 */
	<T extends Privilege> T detail(DataRuntime runtime, int index, T meta, Privilege query, DataRow row);
	/**
	 * privilege[结果集封装]<br/>
	 * 根据查询结果封装Privilege对象,更多属性
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 上一步封装结果
	 * @param row 查询结果集
	 * @return Privilege
	 */
	default <T extends Privilege> T detail(DataRuntime runtime, int index, T meta, User user, DataRow row) {
		return detail(runtime, index, meta, new Privilege(user), row);
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
    boolean grant(DataRuntime runtime, User user, Privilege ... privileges)  throws Exception;
    /**
     * grant[调用入口]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */
    boolean grant(DataRuntime runtime, Role role, Privilege ... privileges)  throws Exception;
    /**
     * grant[调用入口]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */
    boolean grant(DataRuntime runtime, User user, Role ... roles)  throws Exception;
	/**
	 * grant[调用入口]<br/>
	 * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	default boolean grant(DataRuntime runtime, String user, Privilege ... privileges) throws Exception {
		return grant(runtime, new User(user), privileges);
	}

    /**
     * grant[命令合成]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param privileges 权限
     * @return List
     */
    List<Run> buildGrantRun(DataRuntime runtime, User user, Privilege ... privileges) throws Exception;

    /**
     * grant[命令合成]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return List
     */
    List<Run> buildGrantRun(DataRuntime runtime, User user, Role ... roles) throws Exception;

    /**
     * grant[命令合成]<br/>
     * 授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return List
     */
    List<Run> buildGrantRun(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception;

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
     * privilege[调用入口]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param privileges 权限
     * @return boolean
     */
    boolean revoke(DataRuntime runtime, User user, Privilege ... privileges) throws Exception;
    /**
     * privilege[调用入口]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return boolean
     */
    boolean revoke(DataRuntime runtime, User user, Role ... roles) throws Exception;
    /**
     * privilege[调用入口]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return boolean
     */
    boolean revoke(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception;

	/**
	 * privilege[调用入口]<br/>
	 * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param user 用户
	 * @param privileges 权限
	 * @return boolean
	 */
	default boolean revoke(DataRuntime runtime, String user, Privilege ... privileges) throws Exception {
		return revoke(runtime, new User(user), privileges);
	}

	/**
	 * privilege[命令合成]<br/>
	 * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param user 用户
	 * @param privileges 权限
	 * @return List
	 */
	List<Run> buildRevokeRun(DataRuntime runtime, User user, Privilege ... privileges) throws Exception;

    /**
     * privilege[命令合成]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param user 用户
     * @param roles 角色
     * @return List
     */
    List<Run> buildRevokeRun(DataRuntime runtime, User user, Role ... roles) throws Exception;

    /**
     * privilege[命令合成]<br/>
     * 撤销授权
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param role 角色
     * @param privileges 权限
     * @return List
     */
    List<Run> buildRevokeRun(DataRuntime runtime, Role role, Privilege ... privileges) throws Exception;

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 ******************************************************************************************************************/
	StringBuilder name(DataRuntime runtime, StringBuilder builder, Metadata meta);
	StringBuilder name(DataRuntime runtime, StringBuilder builder, Column meta);

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
    public Object convert(DataRuntime runtime, StringBuilder builder,  Object value, Column column, Boolean placeholder, Boolean unicode, ConfigStore configs);
	/**
	 * 参数值 数据类型转换
	 * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
	 * @param table 表
	 * @param run  值
	 * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue run);
	boolean convert(DataRuntime runtime, Table table, Run run);
	boolean convert(DataRuntime runtime, ConfigStore configs, Run run);
	default boolean convert(DataRuntime runtime, ConfigStore configs, List<Run> runs) {
		for(Run run:runs) {
			convert(runtime, configs, run);
		}
		return true;
	}

	/**
	 * 数据类型转换
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param columns 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Map<String, Column> columns, RunValue run);

	/**
	 * 数据类型转换,没有提供column的根据value类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @param run 值
	 * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
	 */
	boolean convert(DataRuntime runtime, Column column, RunValue run);
	Object convert(DataRuntime runtime, Column column, Object value);
	Object convert(DataRuntime runtime, TypeMetadata columnType, Object value);
	/**
	 * 在不检测数据库结构时才生效,否则会被convert代替
	 * 生成value格式 主要确定是否需要单引号  或  类型转换
	 * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
	 * 如 TO_DATE('')
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param row DataRow 或 Entity
	 * @param key 列名
	 */
	void value(DataRuntime runtime, StringBuilder builder, Object row, String key);

	/**
	 * 根据数据类型生成SQL(如是否需要'',是否需要格式转换函数)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder builder
	 * @param value value
	 */
	//void format(StringBuilder builder, Object value);

	/**
	 * 从数据库中读取数据,常用的基本类型可以自动转换,不常用的如json/point/polygon/blob等转换成anyline对应的类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata Column 用来定位数据类型
	 * @param value value
	 * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
	 * @return Object
	 */
	Object read(DataRuntime runtime, Column metadata, Object value, Class clazz);

	/**
	 * 通过占位符写入数据库前转换成数据库可接受的Java数据类型<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param metadata Column 用来定位数据类型
	 * @param placeholder 是否占位符
	 * @param value value
	 * @return Object
	 */
	Object write(DataRuntime runtime, Column metadata, Object value, Boolean placeholder, Boolean unicode);
    default String unicodeGuide(DataRuntime runtime) {
        return "";
    }
 	/**
	 * 拼接字符串
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param args args
	 * @return String
	 */
	String concat(DataRuntime runtime, String ... args);

	/**
	 * 是否是数字列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isNumberColumn(DataRuntime runtime, Column column);

	/**
	 * 是否是boolean列
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isBooleanColumn(DataRuntime runtime, Column column);

	/**
	 * 是否是字符类型
	 * 决定值是否需要加单引号
	 * number boolean 返回false
	 * 其他返回true
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return boolean
	 */
	boolean isCharColumn(DataRuntime runtime, Column column);

	/**
	 * 内置函数
	 * 如果需要引号,方法应该一块返回
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列属性,不同的数据类型解析出来的值可能不一样
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value);
	default String defaultValue(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
		return value(runtime, column, value);
	}
	void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value);
	/**
	 * 转换成相应数据库的数据类型包含精度
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param column 列
	 * @return String
	 */
	//String type(Column column);

	/**
	 * 数据库类型转换成java类型
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param type type
	 * @return String
	 */
	//String type2class(String type);

	/**
	 * 对象名称格式化(大小写转换)，在查询系统表时需要
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param name name
	 * @return String
	 */
	String objectName(DataRuntime runtime, String name);

	default String compressCondition(DataRuntime runtime, String cmd) {
		String head = conditionHead();
		cmd = cmd.replaceAll(head + "\\s*1=1\\s*AND", head);
		return cmd;
	}
	default String conditionHead() {
		return "WHERE";
	}
	/**
	 * 比较运算符在不同数据库的区别
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param builder StringBuilder
	 * @param column 列名
	 * @param compare Compare
	 * @param metadata 数据类型
	 * @param value 值
	 * @param placeholder 是否启用占位符
	 */
	default void formula(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Column metadata, Object value, Boolean placeholder, Boolean unicode) {
		if(!placeholder) {
			//不用占位 需要引号的在这里加上
			value = write(runtime, metadata, value, placeholder, unicode);
		}
		builder.append(compare.formula(column, value, placeholder, unicode));
	}

}
