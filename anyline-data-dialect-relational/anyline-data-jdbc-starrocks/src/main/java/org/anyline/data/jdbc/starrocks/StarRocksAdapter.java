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

package org.anyline.data.jdbc.starrocks;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.MySQLGenusAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.*;
import org.anyline.metadata.adapter.*;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component("anyline.data.jdbc.adapter.starrocks")
public class StarRocksAdapter extends MySQLGenusAdapter implements JDBCAdapter {
    public DatabaseType type() {
        return DatabaseType.StarRocks;
    }

    public StarRocksAdapter() {
        super();
        delimiterFr = "`";
        delimiterTo = "`";
        for (StarRocksTypeMetadataAlias alias: StarRocksTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }
    }

    /* *****************************************************************************************************************
     *
     * 													DML
     *
     * =================================================================================================================
     * INSERT			: 插入
     * UPDATE			: 更新
     * SAVE				: 根据情况插入或更新
     * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
     * EXISTS			: 是否存在
     * COUNT			: 统计
     * EXECUTE			: 执行(原生SQL及存储过程)
     * DELETE			: 删除
     *
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     * 													INSERT
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
        return super.insert(runtime, random, batch, dest, data, configs, columns);
    }

    /**
     * insert into table select * from table
     * 与query参数一致
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 插入表
     * @param prepare 查询表
     * @param configs 查询条件及相关配置
     * @param obj 查询条件
     * @param conditions 查询条件
     * @return 影响行数
     */
    @Override
    public long insert(DataRuntime runtime, String random, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String ... conditions) {
        return super.insert(runtime, random, dest, prepare, configs, obj, conditions);
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
    @Override
    public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return super.buildInsertRun(runtime, batch, dest, obj, configs, columns);
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
    public Run buildInsertRun(DataRuntime runtime, Table dest, RunPrepare prepare, ConfigStore configs, Object obj, String... conditions) {
        return super.buildInsertRun(runtime, dest, prepare, configs, obj, conditions);
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
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        super.fillInsertContent(runtime, run, dest, set, configs, columns);
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
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        super.fillInsertContent(runtime, run, dest, list, configs, columns);
    }

    /**
     * 插入子表前 检测并创建子表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表
     * @param configs ConfigStore
     */
    @Override
    public void fillInsertCreateTemplate(DataRuntime runtime, Run run, PartitionTable dest, ConfigStore configs){
        super.fillInsertCreateTemplate(runtime, run, dest, configs);
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
        return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 批量插入数据时,多行数据之间分隔符
     * @return String
     */
    @Override
    public String batchInsertSeparator() {
        return super.batchInsertSeparator();
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 插入数据时是否支持占位符
     * @return boolean
     */
    @Override
    public boolean supportInsertPlaceholder() {
        return super.supportInsertPlaceholder();
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 设置主键值
     * @param obj obj
     * @param value value
     */
    @Override
    protected void setPrimaryValue(Object obj, Object value) {
        super.setPrimaryValue(obj, value);
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
    @Override
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return super.createInsertRun(runtime, dest, obj, configs, columns);
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
    @Override
    protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns) {
        return super.createInsertRunFromCollection(runtime, batch, dest, list, configs, columns);
    }

    /**
     * insert [after]<br/>
     * 执行insert后返回自增主键的key
     * @return String
     */
    @Override
    public String generatedKey() {
        return super.generatedKey();
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
        return super.insert(runtime, random, data, configs, run, pks);
    }

    /**
     * 是否支持返回自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs configs中也可能禁用返回
     * @return boolean
     */
    @Override
    public boolean supportKeyHolder(DataRuntime runtime, ConfigStore configs) {
        return super.supportKeyHolder(runtime, configs);
    }

    /**
     * 自增主键值keys
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs configs中也可能禁用返回
     * @return keys
     */
    @Override
    public List<String> keyHolders(DataRuntime runtime, ConfigStore configs) {
        return super.keyHolders(runtime, configs);
    }

    /* *****************************************************************************************************************
     * 													UPDATE
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
        return super.update(runtime, random, batch, dest, data, configs, columns);
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
    public Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return super.buildUpdateRun(runtime, batch, dest, obj, configs, columns);
    }

    /**
     *
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return Run
     */
    @Override
    public Run buildUpdateRunLimit(DataRuntime runtime, Run run){
        return super.buildUpdateRunLimit(runtime, run);
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
    public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, columns);
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
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, columns);
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
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, columns);
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
        return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
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
        return super.confirmUpdateColumns(runtime, dest, obj, configs, columns);
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
        return super.update(runtime, random, dest, data, configs, run);
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
        return super.save(runtime, random, dest, data, configs, columns);
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
    @Override
    protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns) {
        return super.saveCollection(runtime, random, dest, data, configs, columns);
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
    @Override
    protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return super.saveObject(runtime, random, dest, data, configs, columns);
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
    @Override
    protected Boolean checkOverride(Object obj) {
        return super.checkOverride(obj);
    }

    /**
     * 检测主键值
     * @param obj Object
     * @return Map
     */
    @Override
    protected Map<String, Object> checkPv(Object obj) {
        return super.checkPv(obj);
    }

    /**
     * 是否是可以接收数组类型的值
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param key key
     * @return boolean
     */
    @Override
    protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key) {
        return super.isMultipleValue(runtime, run, key);
    }

    /**
     * 是否支持集合值
     * @param column Column 根据column的数据类型
     * @return boolean
     */
    @Override
    protected boolean isMultipleValue(Column column) {
        return super.isMultipleValue(column);
    }

    /**
     * 过滤掉表结构中不存在的列
     * @param table 表
     * @param columns columns
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.checkMetadata(runtime, table, configs, columns);
    }

    /* *****************************************************************************************************************
     * 													QUERY
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
     * RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)
     * Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder)
     * StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder)
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
     * @param conditions  简单过滤条件
     * @return DataSet
     */
    @Override
    public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.querys(runtime, random, prepare, configs, conditions);
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
        return super.querys(runtime, random, procedure, navi);
    }

    /**
     * query [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param clazz 类
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions  简单过滤条件
     * @return EntitySet
     * @param <T> Entity
     */
    @Override
    public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions) {
        return super.selects(runtime, random, prepare, clazz, configs, conditions);
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
    @Override
    protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, Table table, ConfigStore configs, Run run) {
        return super.select(runtime, random, clazz, table, configs, run);
    }

    /**
     * query [调用入口]<br/>
     * <br/>
     * 对性能有要求的场景调用，返回java原生map集合,结果中不包含元数据信息
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions  简单过滤条件
     * @return maps 返回map集合
     */
    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.maps(runtime, random, prepare, configs, conditions);
    }

    /**
     * select[命令合成]<br/> 最终可执行命令<br/>
     * 创建查询SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions  简单过滤条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.buildQueryRun(runtime, prepare, configs, conditions);
    }

    /**
     * query run初始化,检测占位符、忽略不存在的列等
     * select[命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param configs 过滤条件及相关配置
     * @param conditions  简单过滤条件
     */
    @Override
    public void init(DataRuntime runtime, Run run, ConfigStore configs, String ... conditions) {
        super.init(runtime, run, configs, conditions);
    }

    /**
     * 解析文本中的占位符
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void parseText(DataRuntime runtime, TextRun run) {
        super.parseText(runtime, run);
    }

    /**
     * 查询序列cur 或 next value
     * @param next  是否生成返回下一个序列 false:cur true:next
     * @param names 序列名
     * @return String
     */
    @Override
    public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names) {
        return super.buildQuerySequence(runtime, next, names);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体, 中间过程有可能转换类型
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public Run fillQueryContent(DataRuntime runtime, Run run) {
        return super.fillQueryContent(runtime, run);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public Run fillQueryContent(DataRuntime runtime, StringBuilder builder, Run run) {
        return super.fillQueryContent(runtime, builder, run);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    @Override
    protected Run fillQueryContent(DataRuntime runtime, XMLRun run) {
        return super.fillQueryContent(runtime, run);
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, XMLRun run) {
        return super.fillQueryContent(runtime, builder, run);
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run TextRun
     */
    @Override
    protected Run fillQueryContent(DataRuntime runtime, TextRun run) {
        return super.fillQueryContent(runtime, run);
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run TextRun
     */
    @Override
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TextRun run) {
        return super.fillQueryContent(runtime, builder, run);
    }

    /**
     *
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run TextRun
     */
    @Override
    protected Run fillQueryContent(DataRuntime runtime, TableRun run) {
        return super.fillQueryContent(runtime, run);
    }

    /**
     * 有些非JDBC环境也需要用到SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder 有可能合个run合成一个 所以提供一个共用builder
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TableRun run) {
        return super.fillQueryContent(runtime, builder, run);
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
        return super.mergeFinalQuery(runtime, run);
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
    public RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder) {
        return super.createConditionLike(runtime, builder, compare, value, placeholder);
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
    public StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, boolean placeholder) {
        return super.createConditionIn(runtime, builder, compare, value, placeholder);
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
        return super.select(runtime, random, system, table, configs, run);
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
        return super.maps(runtime, random, configs, run);
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
        return super.map(runtime, random, configs, run);
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
        return super.sequence(runtime, random, next, names);
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
        return super.process(runtime, list);
    }

    /* *****************************************************************************************************************
     * 													COUNT
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
     * @param conditions  简单过滤条件
     * @return long
     */
    @Override
    public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.count(runtime, random, prepare, configs, conditions);
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
        return super.mergeFinalTotal(runtime, run);
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
        return super.count(runtime, random, run);
    }

    /* *****************************************************************************************************************
     * 													EXISTS
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
     * @param conditions  简单过滤条件
     * @return boolean
     */
    @Override
    public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.exists(runtime, random, prepare, configs, conditions);
    }

    /**
     * exists [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return String
     */
    @Override
    public String mergeFinalExists(DataRuntime runtime, Run run) {
        return super.mergeFinalExists(runtime, run);
    }

    /* *****************************************************************************************************************
     * 													EXECUTE
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
     * @param conditions  简单过滤条件
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.execute(runtime, random, prepare, configs, conditions);
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
        return super.execute(runtime, random, batch, configs, prepare, values);
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
        return super.execute(runtime, random, batch, vol, configs, prepare, values);
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
        return super.execute(runtime, random, procedure);
    }

    /**
     * execute [命令合成]<br/>
     * 创建执行SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param conditions  简单过滤条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.buildExecuteRun(runtime, prepare, configs, conditions);
    }

    /**
     * execute [命令合成]<br/>
     * 填充execute命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    @Override
    protected void fillExecuteContent(DataRuntime runtime, XMLRun run) {
        super.fillExecuteContent(runtime, run);
    }

    /**
     * execute [命令合成]<br/>
     * 填充execute命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    @Override
    protected void fillExecuteContent(DataRuntime runtime, TextRun run) {
        super.fillExecuteContent(runtime, run);
    }

    /**
     * execute [命令合成]<br/>
     * 填充execute命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run XMLRun
     */
    @Override
    protected void fillExecuteContent(DataRuntime runtime, TableRun run) {
        super.fillExecuteContent(runtime, run);
    }

    /**
     * execute [命令合成-子流程]<br/>
     * 填充 execute 命令内容
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void fillExecuteContent(DataRuntime runtime, Run run) {
        super.fillExecuteContent(runtime, run);
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
        return super.execute(runtime, random, configs, run);
    }

    /**
     * query [命令合成]<br/>
     * 替换占位符
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    @Override
    protected void replaceVariable(DataRuntime runtime, TextRun run) {
        super.replaceVariable(runtime, run);
    }

    /* *****************************************************************************************************************
     * 													DELETE
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
        return super.deletes(runtime, random, batch, table, configs, key, values);
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
        return super.delete(runtime, random, dest, configs, obj, columns);
    }

    /**
     * delete [调用入口]<br/>
     * <br/>
     * 根据configs和conditions过滤条件
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @param configs 查询条件及相关设置
     * @param conditions  简单过滤条件
     * @return 影响行数
     */
    @Override
    public long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions) {
        return super.delete(runtime, random, table, configs, conditions);
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
        return super.truncate(runtime, random, table);
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
    public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, String ... columns) {
        return super.buildDeleteRun(runtime, dest, configs, obj, columns);
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
    public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String key, Object values) {
        return super.buildDeleteRun(runtime, batch, table, configs, key, values);
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
        return super.buildTruncateRun(runtime, table);
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
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values) {
        return super.buildDeleteRunFromTable(runtime, batch, table, configs, column, values);
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
    public List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String... columns) {
        return super.buildDeleteRunFromEntity(runtime, table, configs, obj, columns);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run) {
        super.fillDeleteRunContent(runtime, run);
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
        return super.delete(runtime, random, configs, run);
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
     * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
     * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
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
        return super.database(runtime, random);
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
        return super.product(runtime, random);
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
        return super.version(runtime, random);
    }

    /**
     * database[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name) {
        return super.databases(runtime, random, greedy, name);
    }

    /**
     * database[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name) {
        return super.databases(runtime, random, name);
    }

    /**
     * database[命令合成]<br/>
     * 查询当前数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryDatabaseRun(DataRuntime runtime) throws Exception {
        return super.buildQueryDatabaseRun(runtime);
    }

    /**
     * database[命令合成]<br/>
     * 查询当前数据源 数据库产品说明(产品名称+版本号)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryProductRun(DataRuntime runtime) throws Exception {
        return super.buildQueryProductRun(runtime);
    }

    /**
     * database[命令合成]<br/>
     * 查询当前数据源 数据库版本 版本号比较复杂 不是全数字
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryVersionRun(DataRuntime runtime) throws Exception {
        return super.buildQueryVersionRun(runtime);
    }

    /**
     * database[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param name 名称统配符或正则
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
        return super.buildQueryDatabasesRun(runtime, greedy, name);
    }

    /**
     * database[结果集封装]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param databases 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception
     */
    @Override
    public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.databases(runtime, index, create, databases, catalog, schema, set);
    }

    /**
     * database[结果集封装]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param databases 上一步查询结果
     * @param set 查询结果集
     * @return List
     * @throws Exception
     */
    @Override
    public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.databases(runtime, index, create, databases, catalog, schema, set);
    }

    /**
     * database[结果集封装]<br/>
     * 当前database 根据查询结果集
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param database 上一步查询结果
     * @param set 查询结果集
     * @return database
     * @throws Exception 异常
     */
    @Override
    public Database database(DataRuntime runtime, int index, boolean create, Database database, DataSet set) throws Exception {
        return super.database(runtime, index, create, database, set);
    }

    /**
     * database[结果集封装]<br/>
     * 当前database 根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param database 上一步查询结果
     * @return database
     * @throws Exception 异常
     */
    @Override
    public Database database(DataRuntime runtime, boolean create, Database database) throws Exception {
        return super.database(runtime, create, database);
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
        return super.product(runtime, index, create, product, set);
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
        return super.product(runtime, create, product);
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
        return super.version(runtime, index, create, version, set);
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
        return super.version(runtime, create, version);
    }

    /* *****************************************************************************************************************
     * 													catalog
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name)
     * List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
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
        return super.catalog(runtime, random);
    }

    /**
     * catalog[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name) {
        return super.catalogs(runtime, random, name);
    }

    /**
     * catalog[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name) {
        return super.catalogs(runtime, random, greedy, name);
    }

    /**
     * catalog[命令合成]<br/>
     * 查询当前catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryCatalogRun(DataRuntime runtime, String random) throws Exception {
        return super.buildQueryCatalogRun(runtime, random);
    }

    /**
     * catalog[命令合成]<br/>
     * 查询全部catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param name 名称统配符或正则
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
        return super.buildQueryCatalogsRun(runtime, greedy, name);
    }

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果集构造 Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.catalogs(runtime, index, create, catalogs, catalog, schema, set);
    }

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果集构造 Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.catalogs(runtime, index, create, catalogs, catalog, schema, set);
    }

    /**
     * catalog[结果集封装]<br/>
     * 根据驱动内置接口补充 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @return catalogs
     * @throws Exception 异常
     */
    @Override
    public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs) throws Exception {
        return super.catalogs(runtime, create, catalogs);
    }

    /**
     * catalog[结果集封装]<br/>
     * 根据驱动内置接口补充 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @return catalogs
     * @throws Exception 异常
     */
    @Override
    public List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs) throws Exception {
        return super.catalogs(runtime, create, catalogs);
    }

    /**
     * catalog[结果集封装]<br/>
     * 当前catalog 根据查询结果集
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 上一步查询结果
     * @param set 查询结果集
     * @return Catalog
     * @throws Exception 异常
     */
    @Override
    public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set) throws Exception {
        return super.catalog(runtime, index, create, catalog, set);
    }

    /**
     * catalog[结果集封装]<br/>
     * 当前catalog 根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog 上一步查询结果
     * @return Catalog
     * @throws Exception 异常
     */
    @Override
    public Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog) throws Exception {
        return super.catalog(runtime, create, catalog);
    }

    /* *****************************************************************************************************************
     * 													schema
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name)
     * List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name)
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
        return super.schema(runtime, random);
    }

    /**
     * schema[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name) {
        return super.schemas(runtime, random, catalog, name);
    }

    /**
     * schema[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name) {
        return super.schemas(runtime, random, greedy, catalog, name);
    }

    /**
     * schema[命令合成]<br/>
     * 查询当前schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQuerySchemaRun(DataRuntime runtime, String random) throws Exception {
        return super.buildQuerySchemaRun(runtime, random);
    }

    /**
     * catalog[命令合成]<br/>
     * 查询全部数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param name 名称统配符或正则
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @return sqls
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQuerySchemasRun(DataRuntime runtime, boolean greedy, Catalog catalog, String name) throws Exception {
        return super.buildQuerySchemasRun(runtime, greedy, catalog, name);
    }

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果集构造 Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param schemas 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.schemas(runtime, index, create, schemas, catalog, schema, set);
    }

    /**
     * schema[结果集封装]<br/>
     * 根据查询结果集构造 Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param schemas 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.schemas(runtime, index, create, schemas, catalog, schema, set);
    }

    /**
     * schema[结果集封装]<br/>
     * 根据驱动内置接口补充 Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param schemas 上一步查询结果
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, boolean create, LinkedHashMap<String, Schema> schemas) throws Exception {
        return super.schemas(runtime, create, schemas);
    }

    /**
     * schema[结果集封装]<br/>
     * 根据驱动内置接口补充 Schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param schemas 上一步查询结果
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public List<Schema> schemas(DataRuntime runtime, boolean create, List<Schema> schemas) throws Exception {
        return super.schemas(runtime, create, schemas);
    }

    /**
     * schema[结果集封装]<br/>
     * 当前schema 根据查询结果集
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQuerySchemaRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param schema 上一步查询结果
     * @param set 查询结果集
     * @return schema
     * @throws Exception 异常
     */
    @Override
    public Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet set) throws Exception {
        return super.schema(runtime, index, create, schema, set);
    }

    /**
     * schema[结果集封装]<br/>
     * 当前schema 根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param schema 上一步查询结果
     * @return schema
     * @throws Exception 异常
     */
    @Override
    public Schema schema(DataRuntime runtime, boolean create, Schema schema) throws Exception {
        return super.schema(runtime, create, schema);
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
    public <T extends Metadata> T checkName(DataRuntime runtime, String random, T meta) throws RuntimeException{
        return super.checkName(runtime, random, meta);
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
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @param struct 是否查询表结构
     * @return List
     * @param <T> Table
     */
    @Override
    public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.tables(runtime, random, greedy, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * table[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    protected void tableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
        super.tableMap(runtime, random, greedy, catalog, schema, configs);
    }

    /**
     * table[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.tables(runtime, random, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * table[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        return super.buildQueryTablesRun(runtime, greedy, catalog, schema, pattern, types, configs);
    }

    /**
     * table[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryTablesCommentRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * table[结果集封装]<br/>
     *  根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.tables(runtime, index, create, tables, catalog, schema, set);
    }

    /**
     * table[结果集封装]<br/>
     *  根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.tables(runtime, index, create, tables, catalog, schema, set);
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.tables(runtime, create, tables, catalog, schema, pattern, types);
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return tables
     * @throws Exception 异常
     * @param <T> Table
     */
    @Override
    public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.tables(runtime, create, tables, catalog, schema, pattern, types);
    }

    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return Table
     * @param <T> Table
     */
    @Override
    public <T extends Table> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends Table> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * table[结构集封装-依据]<br/>
     * 读取table元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return TableMetadataAdapter
     */
    @Override
    public TableMetadataAdapter tableMetadataAdapter(DataRuntime runtime) {
        return super.tableMetadataAdapter(runtime);
    }

    /**
     * table[结果集封装]<br/>
     * 表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.comments(runtime, index, create, tables, catalog, schema, set);
    }

    /**
     * table[结果集封装]<br/>
     * 表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.comments(runtime, index, create, tables, catalog, schema, set);
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
        return super.ddl(runtime, random, table, init);
    }

    /**
     * table[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, Table table) throws Exception {
        return super.buildQueryDdlsRun(runtime, table);
    }

    /**
     * table[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param table 表
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, table, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													vertexTable
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
     * List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set)
     * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, DataSet set)
     * <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends VertexTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, VertexTable vertexTable, boolean init)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * vertexTable[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @param struct 是否查询表结构
     * @return List
     * @param <T> VertexTable
     */
    @Override
    public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.vertexTables(runtime, random, greedy, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * vertexTable[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    protected void vertexTableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
        super.vertexTableMap(runtime, random, greedy, catalog, schema, configs);
    }

    /**
     * vertexTable[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.vertexTables(runtime, random, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * vertexTable[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryVertexTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        return super.buildQueryVertexTablesRun(runtime, greedy, catalog, schema, pattern, types, configs);
    }

    /**
     * vertexTable[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryVertexTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryVertexTablesCommentRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * vertexTable[结果集封装]<br/>
     *  根据查询结果集构造VertexTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryVertexTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param vertexTables 上一步查询结果
     * @param set 查询结果集
     * @return vertexTables
     * @throws Exception 异常
     */
    @Override
    public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.vertexTables(runtime, index, create, vertexTables, catalog, schema, set);
    }

    /**
     * vertexTable[结果集封装]<br/>
     *  根据查询结果集构造VertexTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryVertexTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param vertexTables 上一步查询结果
     * @param set 查询结果集
     * @return vertexTables
     * @throws Exception 异常
     */
    @Override
    public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, int index, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.vertexTables(runtime, index, create, vertexTables, catalog, schema, set);
    }

    /**
     * vertexTable[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param vertexTables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return vertexTables
     * @throws Exception 异常
     */
    @Override
    public <T extends VertexTable> LinkedHashMap<String, T> vertexTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> vertexTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.vertexTables(runtime, create, vertexTables, catalog, schema, pattern, types);
    }

    /**
     * vertexTable[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param vertexTables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return vertexTables
     * @throws Exception 异常
     * @param <T> VertexTable
     */
    @Override
    public <T extends VertexTable> List<T> vertexTables(DataRuntime runtime, boolean create, List<T> vertexTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.vertexTables(runtime, create, vertexTables, catalog, schema, pattern, types);
    }

    /**
     * vertexTable[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return VertexTable
     * @param <T> VertexTable
     */
    @Override
    public <T extends VertexTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
    }

    /**
     * vertexTable[结果集封装]<br/>
     * 根据查询结果封装VertexTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return VertexTable
     * @param <T> VertexTable
     */
    @Override
    public <T extends VertexTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * vertexTable[结构集封装-依据]<br/>
     * 读取vertexTable元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return VertexTableMetadataAdapter
     */
    @Override
    public VertexTableMetadataAdapter vertexTableMetadataAdapter(DataRuntime runtime) {
        return super.vertexTableMetadataAdapter(runtime);
    }

    /**
     *
     * vertexTable[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param vertexTable 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, VertexTable vertexTable, boolean init) {
        return super.ddl(runtime, random, vertexTable, init);
    }

    /**
     * vertexTable[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param vertexTable 表
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, VertexTable vertexTable) throws Exception {
        return super.buildQueryDdlsRun(runtime, vertexTable);
    }

    /**
     * vertexTable[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param vertexTable 表
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, VertexTable vertexTable, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, vertexTable, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													EdgeTable
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
     * List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set)
     * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, DataSet set)
     * <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends EdgeTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable meta)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * edgeTable[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @param struct 是否查询表结构
     * @return List
     * @param <T> EdgeTable
     */
    @Override
    public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.edgeTables(runtime, random, greedy, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * edgeTable[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    protected void edgeTableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
        super.edgeTableMap(runtime, random, greedy, catalog, schema, configs);
    }

    /**
     * edgeTable[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.edgeTables(runtime, random, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * edgeTable[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryEdgeTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        return super.buildQueryEdgeTablesRun(runtime, greedy, catalog, schema, pattern, types, configs);
    }

    /**
     * edgeTable[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryEdgeTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryEdgeTablesCommentRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * edgeTable[结果集封装]<br/>
     *  根据查询结果集构造EdgeTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryEdgeTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param edgeTables 上一步查询结果
     * @param set 查询结果集
     * @return edgeTables
     * @throws Exception 异常
     */
    @Override
    public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.edgeTables(runtime, index, create, edgeTables, catalog, schema, set);
    }

    /**
     * edgeTable[结果集封装]<br/>
     *  根据查询结果集构造EdgeTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryEdgeTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param edgeTables 上一步查询结果
     * @param set 查询结果集
     * @return edgeTables
     * @throws Exception 异常
     */
    @Override
    public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, int index, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.edgeTables(runtime, index, create, edgeTables, catalog, schema, set);
    }

    /**
     * edgeTable[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param edgeTables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return edgeTables
     * @throws Exception 异常
     */
    @Override
    public <T extends EdgeTable> LinkedHashMap<String, T> edgeTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edgeTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.edgeTables(runtime, create, edgeTables, catalog, schema, pattern, types);
    }

    /**
     * edgeTable[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param edgeTables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return edgeTables
     * @throws Exception 异常
     * @param <T> EdgeTable
     */
    @Override
    public <T extends EdgeTable> List<T> edgeTables(DataRuntime runtime, boolean create, List<T> edgeTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.edgeTables(runtime, create, edgeTables, catalog, schema, pattern, types);
    }

    /**
     * edgeTable[结果集封装]<br/>
     * 根据查询结果封装EdgeTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return EdgeTable
     * @param <T> EdgeTable
     */
    @Override
    public <T extends EdgeTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
    }

    /**
     * edgeTable[结果集封装]<br/>
     * 根据查询结果封装EdgeTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return EdgeTable
     * @param <T> EdgeTable
     */
    @Override
    public <T extends EdgeTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * edgeTable[结构集封装-依据]<br/>
     * 读取edgeTable元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return EdgeTableMetadataAdapter
     */
    @Override
    public EdgeTableMetadataAdapter edgeTableMetadataAdapter(DataRuntime runtime) {
        return super.edgeTableMetadataAdapter(runtime);
    }

    /**
     *
     * edgeTable[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, EdgeTable meta, boolean init) {
        return super.ddl(runtime, random, meta, init);
    }

    /**
     * edgeTable[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, EdgeTable meta) throws Exception {
        return super.buildQueryDdlsRun(runtime, meta);
    }

    /**
     * edgeTable[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param meta 表
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, EdgeTable meta, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, meta, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													view
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
     * List<Run> buildQueryViewsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
     * <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set)
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends View> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, View view, boolean init)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, View view)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * view[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @param struct 是否查询视图结构
     * @return List
     * @param <T> View
     */
    @Override
    public <T extends View> List<T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.views(runtime, random, greedy, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * view[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    protected void viewMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
        super.viewMap(runtime, random, greedy, catalog, schema, configs);
    }

    /**
     * view[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.views(runtime, random, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * view[命令合成]<br/>
     * 查询视图,不是查视图中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        return super.buildQueryViewsRun(runtime, greedy, catalog, schema, pattern, types, configs);
    }

    /**
     * view[命令合成]<br/>
     * 查询视图备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryViewsCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryViewsCommentRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * view[结果集封装]<br/>
     *  根据查询结果集构造View
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param views 上一步查询结果
     * @param set 查询结果集
     * @return views
     * @throws Exception 异常
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.views(runtime, index, create, views, catalog, schema, set);
    }

    /**
     * view[结果集封装]<br/>
     *  根据查询结果集构造View
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryViewsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param views 上一步查询结果
     * @param set 查询结果集
     * @return views
     * @throws Exception 异常
     */
    @Override
    public <T extends View> List<T> views(DataRuntime runtime, int index, boolean create, List<T> views, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.views(runtime, index, create, views, catalog, schema, set);
    }

    /**
     * view[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param views 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return views
     * @throws Exception 异常
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.views(runtime, create, views, catalog, schema, pattern, types);
    }

    /**
     * view[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param views 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return views
     * @throws Exception 异常
     * @param <T> View
     */
    @Override
    public <T extends View> List<T> views(DataRuntime runtime, boolean create, List<T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.views(runtime, create, views, catalog, schema, pattern, types);
    }

    /**
     * view[结果集封装]<br/>
     * 根据查询结果封装View对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return View
     * @param <T> View
     */
    @Override
    public <T extends View> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends View> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * view[结构集封装-依据]<br/>
     * 读取view元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return ViewMetadataAdapter
     */
    @Override
    public ViewMetadataAdapter viewMetadataAdapter(DataRuntime runtime) {
        return super.viewMetadataAdapter(runtime);
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
        return super.ddl(runtime, random, view, init);
    }

    /**
     * view[命令合成]<br/>
     * 查询视图DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param view 视图
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, View view) throws Exception {
        return super.buildQueryDdlsRun(runtime, view);
    }

    /**
     * view[结果集封装]<br/>
     * 查询视图DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param view 视图
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, view, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													masterTable
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends MasterTable> List<T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
     * List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set)
     * <T extends MasterTable> List<T> masterTables(DataRuntime runtime, int index, boolean create, List<T> masterTables, Catalog catalog, Schema schema, DataSet set)
     * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends MasterTable> List<T> masterTables(DataRuntime runtime, boolean create, List<T> masterTables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends MasterTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, MasterTable masterTable, boolean init)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable masterTable)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, MasterTable masterTable, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * masterTable[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @param struct 是否查询表结构
     * @return List
     * @param <T> MasterTable
     */
    @Override
    public <T extends MasterTable> List<T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.masterTables(runtime, random, greedy, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * masterTable[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    protected void masterTableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, ConfigStore configs) {
        super.masterTableMap(runtime, random, greedy, catalog, schema, configs);
    }

    /**
     * masterTable[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs) {
        return super.masterTables(runtime, random, catalog, schema, pattern, types, struct, configs);
    }

    /**
     * masterTable[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs) throws Exception {
        return super.buildQueryMasterTablesRun(runtime, greedy, catalog, schema, pattern, types, configs);
    }

    /**
     * masterTable[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return String
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildQueryMasterTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryMasterTablesCommentRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * masterTable[结果集封装]<br/>
     *  根据查询结果集构造MasterTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param masterTables 上一步查询结果
     * @param set 查询结果集
     * @return masterTables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.masterTables(runtime, index, create, masterTables, catalog, schema, set);
    }

    /**
     * masterTable[结果集封装]<br/>
     *  根据查询结果集构造MasterTable
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param masterTables 上一步查询结果
     * @param set 查询结果集
     * @return masterTables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> List<T> masterTables(DataRuntime runtime, int index, boolean create, List<T> masterTables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.masterTables(runtime, index, create, masterTables, catalog, schema, set);
    }

    /**
     * masterTable[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param masterTables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return masterTables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> masterTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.masterTables(runtime, create, masterTables, catalog, schema, pattern, types);
    }

    /**
     * masterTable[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param masterTables 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types Metadata.TYPE.
     * @return masterTables
     * @throws Exception 异常
     * @param <T> MasterTable
     */
    @Override
    public <T extends MasterTable> List<T> masterTables(DataRuntime runtime, boolean create, List<T> masterTables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.masterTables(runtime, create, masterTables, catalog, schema, pattern, types);
    }

    /**
     * masterTable[结果集封装]<br/>
     * 根据查询结果封装MasterTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return MasterTable
     * @param <T> MasterTable
     */
    @Override
    public <T extends MasterTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
    }

    /**
     * masterTable[结果集封装]<br/>
     * 根据查询结果封装MasterTable对象,更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return MasterTable
     * @param <T> MasterTable
     */
    @Override
    public <T extends MasterTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * masterTable[结构集封装-依据]<br/>
     * 读取masterTable元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return MasterTableMetadataAdapter
     */
    @Override
    public MasterTableMetadataAdapter masterTableMetadataAdapter(DataRuntime runtime) {
        return super.masterTableMetadataAdapter(runtime);
    }

    /**
     *
     * masterTable[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, MasterTable meta, boolean init) {
        return super.ddl(runtime, random, meta, init);
    }

    /**
     * masterTable[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param masterTable 表
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable masterTable) throws Exception {
        return super.buildQueryDdlsRun(runtime, masterTable);
    }

    /**
     * masterTable[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param masterTable 表
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, MasterTable masterTable, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, masterTable, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													partition table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
     * [命令合成]
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String pattern)
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags)
     * [结果集封装]<br/>
     * <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
     * <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, PartitionTable table)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     * partition table[调用入口]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param master 主表
     * @param pattern 名称统配符或正则
     * @return List
     * @param <T> MasterTable
     */
    @Override
    public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern) {
        return super.partitionTables(runtime, random, greedy, master, tags, pattern);
    }

    /**
     * partition table[命令合成]<br/>
     * 查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types
     * @return String
     */
    @Override
    public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryPartitionTablesRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * partition table[命令合成]<br/>
     * 根据主表查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param master 主表
     * @param tags 标签名+标签值
     * @param name 名称统配符或正则
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags, String name) throws Exception {
        return super.buildQueryPartitionTablesRun(runtime, master, tags, name);
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
    @Override
    public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master, Map<String,Object> tags) throws Exception {
        return super.buildQueryPartitionTablesRun(runtime, master, tags);
    }

    /**
     * partition table[命令合成]<br/>
     * 根据主表查询分区表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param master 主表
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, Table master) throws Exception {
        return super.buildQueryPartitionTablesRun(runtime, master);
    }

    /**
     * partition table[结果集封装]<br/>
     *  根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param total 合计SQL数量
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param master 主表
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.partitionTables(runtime, total, index, create, master, tables, catalog, schema, set);
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param master 主表
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master) throws Exception {
        return super.partitionTables(runtime, create, tables, catalog, schema, master);
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
        return super.ddl(runtime, random, table);
    }

    /**
     * partition table[命令合成]<br/>
     * 查询 PartitionTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table PartitionTable
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, PartitionTable table) throws Exception {
        return super.buildQueryDdlsRun(runtime, table);
    }

    /**
     * partition table[结果集封装]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param table MasterTable
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, table, ddls, set);
    }

    /**
     * partition table[结果集封装]<br/>
     * 根据查询结果封装PartitionTable对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return PartitionTable
     */
    @Override
    public <T extends PartitionTable> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends PartitionTable> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
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
     * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
     * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception;
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
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary, ConfigStore configs) {
        return super.columns(runtime, random, greedy, table, primary, configs);
    }

    /**
     * column[调用入口]<br/>
     * 查询列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param catalog catalog
     * @param schema schema
     * @param table 查询全部表时 输入null
     * @return List
     * @param <T> Column
     */
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table, ConfigStore configs) {
        return super.columns(runtime, random, greedy, catalog, schema, table, configs);
    }

    /**
     * column[调用入口]<br/>(方法1)<br/>
     * 查询多个表列，并分配到每个表中，需要保持所有表的catalog,schema相同
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param catalog catalog
     * @param schema schema
     * @param tables 表
     * @return List
     * @param <T> Column
     */
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Collection<? extends Table> tables, ConfigStore configs) {
        return super.columns(runtime, random, greedy, catalog, schema, tables, configs);
    }

    /**
     * column[调用入口]<br/>
     * DatabaseMetaData(方法3)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @return columns 上一步查询结果
     * @return pattern 列名称通配符
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern, ConfigStore configs) throws Exception {
        return super.columns(runtime, create, columns, table, pattern, configs);
    }

    /**
     * column[命令合成]<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
     * @return runs
     */
    @Override
    public List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata, ConfigStore configs) throws Exception {
        return super.buildQueryColumnsRun(runtime, table, metadata, configs);
    }

    /**
     * column[命令合成]<br/>(方法1)<br/>
     * 查询多个表的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param tables 表
     * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
     * @return runs
     */
    @Override
    public List<Run> buildQueryColumnsRun(DataRuntime runtime, Catalog catalog, Schema schema, Collection<? extends Table> tables, boolean metadata, ConfigStore configs) throws Exception {
        return super.buildQueryColumnsRun(runtime, catalog, schema, tables, metadata, configs);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 根据系统表查询SQL获取表结构
     *  根据查询结果集构造Column
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param columns 上一步查询结果
     * @param set 系统表查询SQL结果集
     * @return columns
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception {
        return super.columns(runtime, index, create, table, columns, set);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 根据系统表查询SQL获取表结构
     *  根据查询结果集构造Column
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param columns 上一步查询结果
     * @param set 系统表查询SQL结果集
     * @return columns
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception {
        return super.columns(runtime, index, create, table, columns, set);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 根据系统表查询SQL获取表结构
     * 根据查询结果集构造Column,并分配到各自的表中
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 表
     * @param columns 上一步查询结果
     * @param set 系统表查询SQL结果集
     * @return columns
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> columns, DataSet set) throws Exception {
        return super.columns(runtime, index, create, tables, columns, set);
    }

    /**
     * column[结果集封装]<br/>
     * (方法1)
     * <br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param table 表
     * @param row 系统表查询SQL结果集
     * @param <T> Column
     */
    @Override
    public <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) {
        return super.init(runtime, index, meta, table, row);
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
    public <T extends Column> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * column[结构集封装-依据]<br/>
     * 读取column元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return ColumnMetadataAdapter
     */
    @Override
    public ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime) {
        return super.columnMetadataAdapter(runtime);
    }

    /**
     * column[结构集封装-依据]<br/>
     * 读取column元数据结果集的依据(需要区分数据类型)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 具体数据类型,length/precisoin/scale三个属性需要根据数据类型覆盖通用配置
     * @return ColumnMetadataAdapter
     */
    @Override
    public ColumnMetadataAdapter columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataAdapter(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字有效位数列<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataLengthRefer(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataLengthRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据长度列<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataPrecisionRefer(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataPrecisionRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字有效位数列<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataScaleRefer(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataScaleRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 是否忽略长度<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public int columnMetadataIgnoreLength(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataIgnoreLength(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 是否忽略有效位数<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public int columnMetadataIgnorePrecision(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataIgnorePrecision(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 是否忽略小数位<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public int columnMetadataIgnoreScale(DataRuntime runtime, TypeMetadata meta) {
        return super.columnMetadataIgnoreScale(runtime, meta);
    }

    /* *****************************************************************************************************************
     * 													tag
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table)
     * [命令合成]
     * List<Run> buildQueryTagsRun(DataRuntime runtime, Table table, boolean metadata)
     * [结果集封装]<br/>
     * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set)
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
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table) {
        return super.tags(runtime, random, greedy, table);
    }

    /**
     * tag[命令合成]<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param metadata 是否需要根据metadata
     * @return runs
     */
    @Override
    public List<Run> buildQueryTagsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception {
        return super.buildQueryTagsRun(runtime, table, metadata);
    }

    /**
     * tag[结果集封装]<br/>
     *  根据查询结果集构造Tag
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryTagsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param tags 上一步查询结果
     * @param set 查询结果集
     * @return tags
     * @throws Exception 异常
     */
    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception {
        return super.tags(runtime, index, create, table, tags, set);
    }

    /**
     *
     * tag[结果集封装]<br/>
     * 解析JDBC get columns结果
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param tags 上一步查询结果
     * @param pattern 名称统配符或正则
     * @return tags
     * @throws Exception 异常
     */
    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception {
        return super.tags(runtime, create, tags, table, pattern);
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
        return super.init(runtime, index, meta, table, row);
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
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /* *****************************************************************************************************************
     * 													primary
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
     * [命令合成]
     * List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception
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
     * @param table 表
     * @return PrimaryKey
     */
    @Override
    public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table) {
        return super.primary(runtime, random, greedy, table);
    }

    /**
     * primary[命令合成]<br/>
     * 查询表上的主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return runs
     */
    @Override
    public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception {
        return super.buildQueryPrimaryRun(runtime, table);
    }

    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception {
        return super.init(runtime, index, meta, table, set);
    }

    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataSet set) throws Exception {
        return super.detail(runtime, index, meta, table, set);
    }

    /**
     * primary[结构集封装-依据]<br/>
     * primary元数据名称依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return column name
     */
    @Override
    public PrimaryMetadataAdapter primaryMetadataAdapter(DataRuntime runtime) {
        return super.primaryMetadataAdapter(runtime);
    }

    /**
     * primary[结构集封装]<br/>
     *  根据驱动内置接口补充PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @throws Exception 异常
     */
    @Override
    public PrimaryKey primary(DataRuntime runtime, Table table) throws Exception {
        return super.primary(runtime, table);
    }

    /* *****************************************************************************************************************
     * 													foreign
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
     * [命令合成]
     * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception;
     * [结构集封装]
     * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception;
     ******************************************************************************************************************/

    /**
     * foreign[调用入口]<br/>
     * 查询外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param table 表
     * @return PrimaryKey
     */
    @Override
    public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table) {
        return super.foreigns(runtime, random, greedy, table);
    }

    /**
     * foreign[命令合成]<br/>
     * 查询表上的外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return runs
     */
    @Override
    public List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception {
        return super.buildQueryForeignsRun(runtime, table);
    }

    /**
     * foreign[结构集封装]<br/>
     *  根据查询结果集构造PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
     * @param table 表
     * @param foreigns 上一步查询结果
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception {
        return super.foreigns(runtime, index, table, foreigns, set);
    }

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
    @Override
    public <T extends ForeignKey> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
        return super.init(runtime, index, meta, table, row);
    }

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
    @Override
    public <T extends ForeignKey> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception {
        return super.detail(runtime, index, meta, table, row);
    }

    /* *****************************************************************************************************************
     * 													index
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
     * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern)
     * [命令合成]
     * List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name)
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
    public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Collection<? extends Table> tables) {
        return super.indexes(runtime, random, greedy, tables);
    }

    /**
     *
     * index[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param table 表
     * @param pattern 索引名称
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern) {
        return super.indexes(runtime, random, greedy, table, pattern);
    }

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
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, String random, Table table, String pattern) {
        return super.indexes(runtime, random, table, pattern);
    }

    /**
     * index[命令合成]<br/>
     * 查询表上的索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param name 名称
     * @return runs
     */
    @Override
    public List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name) {
        return super.buildQueryIndexesRun(runtime, table, name);
    }

    /**
     * index[命令合成]<br/>
     * 查询表上的索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param tables 表
     * @return runs
     */
    @Override
    public List<Run> buildQueryIndexesRun(DataRuntime runtime, Collection<? extends Table> tables) {
        return super.buildQueryIndexesRun(runtime, tables);
    }

    /**
     * index[结果集封装]<br/>
     *  根据查询结果集构造Index
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param indexes 上一步查询结果
     * @param set 查询结果集
     * @return indexes indexes
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set) throws Exception {
        return super.indexes(runtime, index, create, table, indexes, set);
    }

    /**
     * index[结果集封装]<br/>
     *  根据查询结果集构造Index
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param indexes 上一步查询结果
     * @param set 查询结果集
     * @return indexes indexes
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set) throws Exception {
        return super.indexes(runtime, index, create, table, indexes, set);
    }

    /**
     * index[结果集封装]<br/>
     *  根据查询结果集构造Index
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param tables 表
     * @param indexes 上一步查询结果
     * @param set 查询结果集
     * @return indexes indexes
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Collection<? extends Table> tables, List<T> indexes, DataSet set) throws Exception {
        return super.indexes(runtime, index, create, tables, indexes, set);
    }

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
    @Override
    public <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate) throws Exception {
        return super.indexes(runtime, create, indexes, table, unique, approximate);
    }

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
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate) throws Exception {
        return super.indexes(runtime, create, indexes, table, unique, approximate);
    }

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
    @Override
    public <T extends Index> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception{
        return super.init(runtime, index, meta, table, row);
    }

    /**
     * parse boolean
     * @param row 结果集
     * @param cols 检测的我
     * @param vals 匹配true的值S(只要一项匹配就返回true)
     * @return boolean
     */
    @Override
    protected Boolean parseBoolean(DataRow row, String[] cols, String[] vals){
        return super.parseBoolean(row, cols, vals);
    }

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
    @Override
    public <T extends Index> T detail(DataRuntime runtime, int index, T meta, Table table, DataRow row) throws Exception{
        return super.detail(runtime, index, meta, table, row);
    }

    /**
     * index[结构集封装-依据]<br/>
     * 读取index元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return IndexMetadataAdapter
     */
    @Override
    public IndexMetadataAdapter indexMetadataAdapter(DataRuntime runtime) {
        return super.indexMetadataAdapter(runtime);
    }

    /* *****************************************************************************************************************
     * 													constraint
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern);
     * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern);
     * [命令合成]
     * List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern) ;
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
     * @param table 表
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern) {
        return super.constraints(runtime, random, greedy, table, pattern);
    }

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
    @Override
    public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern) {
        return super.constraints(runtime, random, table, column, pattern);
    }

    /**
     * constraint[命令合成]<br/>
     * 查询表上的约束
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param pattern 名称通配符或正则
     * @return runs
     */
    @Override
    public List<Run> buildQueryConstraintsRun(DataRuntime runtime, Table table, Column column, String pattern) {
        return super.buildQueryConstraintsRun(runtime, table, column, pattern);
    }

    /**
     * constraint[结果集封装]<br/>
     * 根据查询结果集构造Constraint
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param constraints 上一步查询结果
     * @param set DataSet
     * @return constraints constraints
     * @throws Exception 异常
     */
    @Override
    public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet set) throws Exception {
        return super.constraints(runtime, index, create, table, constraints, set);
    }

    /**
     * constraint[结果集封装]<br/>
     * 根据查询结果集构造Constraint
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param column 列
     * @param constraints 上一步查询结果
     * @param set DataSet
     * @return constraints constraints
     * @throws Exception 异常
     */
    @Override
    public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet set) throws Exception {
        return super.constraints(runtime, index, create, table, column, constraints, set);
    }

    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果封装Constraint对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return Constraint
     */
    @Override
    public <T extends Constraint> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends Constraint> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * catalog[结构集封装-依据]<br/>
     * 读取catalog元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return ConstraintMetadataAdapter
     */
    @Override
    public ConstraintMetadataAdapter constraintMetadataAdapter(DataRuntime runtime) {
        return super.constraintMetadataAdapter(runtime);
    }

    /* *****************************************************************************************************************
     * 													trigger
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)
     * [命令合成]
     * List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)
     * [结果集封装]<br/>
     * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
     ******************************************************************************************************************/
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
    @Override
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events) {
        return super.triggers(runtime, random, greedy, table, events);
    }

    /**
     * trigger[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param events 事件 INSERT|UPDATE|DELETE
     * @return runs
     */
    @Override
    public List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) {
        return super.buildQueryTriggersRun(runtime, table, events);
    }

    /**
     * trigger[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param triggers 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception {
        return super.triggers(runtime, index, create, table, triggers, set);
    }

    /**
     * trigger[结果集封装]<br/>
     * 根据查询结果封装trigger对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return Trigger
     */
    @Override
    public <T extends Trigger> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends Trigger> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * trigger[结构集封装-依据]<br/>
     * 读取 trigger 元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return IndexMetadataAdapter
     */
    @Override
    public TriggerMetadataAdapter triggerMetadataAdapter(DataRuntime runtime) {
        return super.triggerMetadataAdapter(runtime);
    }

    /* *****************************************************************************************************************
     * 													procedure
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
     * [命令合成]
     * List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
     * [结果集封装]<br/>
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures)
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception;
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception;
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set);
     ******************************************************************************************************************/
    /**
     *
     * procedure[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
        return super.procedures(runtime, random, greedy, catalog, schema, pattern);
    }

    /**
     *
     * procedure[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
        return super.procedures(runtime, random, catalog, schema, pattern);
    }

    /**
     * procedure[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return sqls
     */
    @Override

    public List<Run> buildQueryProceduresRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
        return super.buildQueryProceduresRun(runtime, catalog, schema, pattern);
    }

    /**
     * procedure[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param procedures 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception {
        return super.procedures(runtime, index, create, procedures, set);
    }

    /**
     * procedure[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param procedures 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception {
        return super.procedures(runtime, index, create, procedures, set);
    }

    /**
     * procedure[结果集封装]<br/>
     * 根据驱动内置接口补充 Procedure
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param procedures 上一步查询结果
     * @return List
     * @throws Exception 异常
     */
    @Override
    public <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures) throws Exception {
        return super.procedures(runtime, create, procedures);
    }

    /**
     * procedure[结果集封装]<br/>
     * 根据驱动内置接口补充 Procedure
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param procedures 上一步查询结果
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures) throws Exception {
        return super.procedures(runtime, create, procedures);
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
        return super.ddl(runtime, random, procedure);
    }

    /**
     * procedure[命令合成]<br/>
     * 查询存储DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, Procedure procedure) throws Exception {
        return super.buildQueryDdlsRun(runtime, procedure);
    }

    /**
     * procedure[结果集封装]<br/>
     * 查询 Procedure DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param procedure Procedure
     * @param ddls 上一步查询结果
     * @param set 查询结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, procedure, ddls, set);
    }

    /**
     * procedure[结果集封装]<br/>
     * 根据查询结果封装procedure对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return Procedure
     */
    @Override
    public <T extends Procedure> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends Procedure> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * procedure[结构集封装-依据]<br/>
     * 读取 procedure 元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return ProcedureMetadataAdapter
     */
    @Override
    public ProcedureMetadataAdapter procedureMetadataAdapter(DataRuntime runtime) {
        return super.procedureMetadataAdapter(runtime);
    }

    /* *****************************************************************************************************************
     * 													function
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
     * [命令合成]
     * List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
     * [结果集封装]<br/>
     * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
     * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions)
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Function function);
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, Function function) throws Exception;
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * function[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
        return super.functions(runtime, random, greedy, catalog, schema, pattern);
    }

    /**
     *
     * function[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
        return super.functions(runtime, random, catalog, schema, pattern);
    }

    /**
     * function[命令合成]<br/>
     * 查询表上的 Function
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return sqls
     */
    @Override
    public List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
        return super.buildQueryFunctionsRun(runtime, catalog, schema, pattern);
    }

    /**
     * function[结果集封装]<br/>
     * 根据查询结果集构造 Function
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param functions 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.functions(runtime, index, create, functions, catalog, schema, set);
    }

    /**
     * function[结果集封装]<br/>
     * 根据查询结果集构造 Function
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param functions 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return super.functions(runtime, index, create, functions, catalog, schema, set);
    }

    /**
     * function[结果集封装]<br/>
     * 根据驱动内置接口补充 Function
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param functions 上一步查询结果
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions) throws Exception {
        return super.functions(runtime, create, functions);
    }

    /**
     * function[结果集封装]<br/>
     * 根据驱动内置接口补充 Function
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param functions 上一步查询结果
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions) throws Exception {
        return super.functions(runtime, create, functions);
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
        return super.ddl(runtime, random, meta);
    }

    /**
     * function[命令合成]<br/>
     * 查询函数DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 函数
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, Function meta) throws Exception {
        return super.buildQueryDdlsRun(runtime, meta);
    }

    /**
     * function[结果集封装]<br/>
     * 查询 Function DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param function Function
     * @param ddls 上一步查询结果
     * @param set 查询结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, function, ddls, set);
    }

    /**
     * function[结果集封装]<br/>
     * 根据查询结果封装function对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return Function
     */
    @Override
    public <T extends Function> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends Function> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * function[结构集封装-依据]<br/>
     * 读取 function 元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return FunctionMetadataAdapter
     */
    @Override
    public FunctionMetadataAdapter functionMetadataAdapter(DataRuntime runtime) {
        return super.functionMetadataAdapter(runtime);
    }

    /* *****************************************************************************************************************
     * 													sequence
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
     * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
     * [命令合成]
     * List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
     * [结果集封装]<br/>
     * <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception;
     * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
     * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences)
     * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence sequence) throws Exception;
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set)
     ******************************************************************************************************************/
    /**
     *
     * sequence[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Sequence> List<T> sequences(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern) {
        return super.sequences(runtime, random, greedy, catalog, schema, pattern);
    }

    /**
     *
     * sequence[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return  LinkedHashMap
     * @param <T> Index
     */
    @Override
    public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern) {
        return super.sequences(runtime, random, catalog, schema, pattern);
    }

    /**
     * sequence[命令合成]<br/>
     * 查询表上的 Sequence
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @return sqls
     */
    @Override
    public List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) {
        return super.buildQuerySequencesRun(runtime, catalog, schema, pattern);
    }

    /**
     * sequence[结果集封装]<br/>
     * 根据查询结果集构造 Sequence
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param sequences 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception {
        return super.sequences(runtime, index, create, sequences, set);
    }

    /**
     * sequence[结果集封装]<br/>
     * 根据查询结果集构造 Sequence
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param sequences 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception {
        return super.sequences(runtime, index, create, sequences, set);
    }

    /**
     * sequence[结果集封装]<br/>
     * 根据驱动内置接口补充 Sequence
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param sequences 上一步查询结果
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences) throws Exception {
        return super.sequences(runtime, create, sequences);
    }

    /**
     * sequence[结果集封装]<br/>
     * 根据驱动内置接口补充 Sequence
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param sequences 上一步查询结果
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences) throws Exception {
        return super.sequences(runtime, create, sequences);
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
        return super.ddl(runtime, random, meta);
    }

    /**
     * sequence[命令合成]<br/>
     * 查询序列DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 序列
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, Sequence meta) throws Exception {
        return super.buildQueryDdlsRun(runtime, meta);
    }

    /**
     * sequence[结果集封装]<br/>
     * 查询 Sequence DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param sequence Sequence
     * @param ddls 上一步查询结果
     * @param set 查询结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, sequence, ddls, set);
    }

    /**
     * sequence[结果集封装]<br/>
     * 根据查询结果封装sequence对象,只封装catalog,schema,name等基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param catalog catalog
     * @param schema schema
     * @param row 查询结果集
     * @return Sequence
     */
    @Override
    public <T extends Sequence> T init(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.init(runtime, index, meta, catalog, schema, row);
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
    public <T extends Sequence> T detail(DataRuntime runtime, int index, T meta, Catalog catalog, Schema schema, DataRow row) {
        return super.detail(runtime, index, meta, catalog, schema, row);
    }

    /**
     * sequence[结构集封装-依据]<br/>
     * 读取 sequence 元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return SequenceMetadataAdapter
     */
    @Override
    public SequenceMetadataAdapter sequenceMetadataAdapter(DataRuntime runtime) {
        return super.sequenceMetadataAdapter(runtime);
    }

    /**
     *
     * 根据 catalog, name检测schemas集合中是否存在
     * @param schemas schemas
     * @param catalog catalog
     * @param name name
     * @return 如果存在则返回 Schema 不存在则返回null
     * @param <T> Table
     */
    @Override
    public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name) {
        return super.schema(schemas, catalog, name);
    }

    /**
     *
     * 根据 name检测catalogs集合中是否存在
     * @param catalogs catalogs
     * @param name name
     * @return 如果存在则返回 Catalog 不存在则返回null
     * @param <T> Table
     */
    @Override
    public <T extends Catalog> T catalog(List<T> catalogs, String name) {
        return super.catalog(catalogs, name);
    }

    /**
     *
     * 根据 name检测databases集合中是否存在
     * @param databases databases
     * @param name name
     * @return 如果存在则返回 Database 不存在则返回null
     * @param <T> Table
     */
    @Override
    public <T extends Database> T database(List<T> databases, String name) {
        return super.database(databases, name);
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
        return super.execute(runtime, random, meta, action, run);
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
        return super.execute(runtime, random, meta, action, runs);
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
        return super.create(runtime, meta);
    }

    /**
     * 检测列的执行命令,all drop alter等
     * @param meta 表
     * @return cols
     */
    @Override
    protected LinkedHashMap<String, Column> checkColumnAction(DataRuntime runtime, Table meta) {
        return super.checkColumnAction(runtime, meta);
    }

    /**
     * 修改主键前先 根据主键检测自增 如果数据库要求自增必须在主键上时才需要执行
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table 表
     * @return boolean
     * @throws Exception 异常
     */
    @Override
    protected List<Run> checkAutoIncrement(DataRuntime runtime, String random, Table table, boolean slice) throws Exception {
        return super.checkAutoIncrement(runtime, random, table, slice);
    }

    /**
     * 合关DDL片段
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @param slices slices
     * @return list
     */
    @Override
    public List<Run> merge(DataRuntime runtime, Table meta, List<Run> slices){
        return super.merge(runtime, meta, slices);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
    @Override

    public boolean rename(DataRuntime runtime, Table origin, String name) throws Exception {
        return super.rename(runtime, origin, name);
    }

    /**
     * table[命令合成-子流程]<br/>
     * 部分数据库在创建主表时用主表关键字(默认)，部分数据库普通表主表子表都用table，部分数据库用collection、timeseries等
     * @param meta 表
     * @return String
     */
    @Override
    public String keyword(Metadata meta) {
        return super.keyword(meta);
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
        return super.buildCreateRun(runtime, meta);
    }

    /**
     * table[命令合成]<br/>
     * 修改表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAlterRun(DataRuntime runtime, Table meta) throws Exception {
        return super.buildAlterRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta, columns, slice);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildAppendColumnCommentRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, meta);
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
        return super.checkTableExists(runtime, builder, exists);
    }

    /**
     * table[命令合成-子流程]<br/>
     * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     */
    @Override
    public void checkPrimary(DataRuntime runtime, Table table) {
        super.checkPrimary(runtime, table);
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
        return super.primary(runtime, builder, meta);
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
        return super.engine(runtime, builder, meta);
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
        return super.body(runtime, builder, meta);
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
        return super.columns(runtime, builder, table);
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
        return super.indexes(runtime, builder, meta);
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
        return super.charset(runtime, builder, meta);
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
        return super.comment(runtime, builder, meta);
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
        return super.keys(runtime, builder, meta);
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
        return super.distribution(runtime, builder, meta);
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
        return super.materialize(runtime, builder, meta);
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
        return super.property(runtime, builder, meta);
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
        return super.partitionBy(runtime, builder, meta);
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
        return super.partitionOf(runtime, builder, meta);
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
        return super.partitionFor(runtime, builder, meta);
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
        return super.inherit(runtime, builder, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildCreateRunHead(runtime, builder, meta);
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
        return super.buildCreateRunOption(runtime, builder, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, meta);
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
        return super.checkViewExists(runtime, builder, exists);
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
        return super.comment(runtime, builder, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
    }

    /**
     * partition table[调用入口]<br/>
     * 删除分区表,执行的命令通过meta.ddls()返回
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @return boolean 是否执行成功
     * @throws Exception DDL异常
     */
    @Override

    public boolean drop(DataRuntime runtime, PartitionTable meta) throws Exception {
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, meta);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, table, meta, trigger);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildAddRun(runtime, meta, slice);
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
        return super.buildAlterRun(runtime, meta, slice);
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
        return super.buildDropRun(runtime, meta, slice);
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
        return super.buildRenameRun(runtime, meta, slice);
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
        return super.buildChangeTypeRun(runtime, meta, slice);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 修改表的关键字
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return String
     */
    @Override
    public String alterColumnKeyword(DataRuntime runtime) {
        return super.alterColumnKeyword(runtime);
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
        return super.addColumnGuide(runtime, builder, meta);
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
        return super.dropColumnGuide(runtime, builder, meta);
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
        return super.buildChangeDefaultRun(runtime, meta, slice);
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
        return super.buildChangeNullableRun(runtime, meta, slice);
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
        return super.buildChangeCommentRun(runtime, meta, slice);
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
        return super.buildAppendCommentRun(runtime, meta, slice);
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
        return super.buildDropAutoIncrement(runtime, meta, slice);
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
        return super.define(runtime, builder, meta, action);
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
        return super.checkColumnExists(runtime, builder, exists);
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
        return super.type(runtime, builder, meta);
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
        return super.aggregation(runtime, builder, meta);
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
        return super.type(runtime, builder, meta, type, ignoreLength, ignorePrecision, ignoreScale);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 定义列:是否忽略有长度<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param type 数据类型
     * @return boolean
     */
    @Override
    public int ignoreLength(DataRuntime runtime, TypeMetadata type) {
        return super.ignoreLength(runtime, type);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 定义列:是否忽略有效位数<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param type TypeMetadata
     * @return boolean
     */
    @Override
    public int ignorePrecision(DataRuntime runtime, TypeMetadata type) {
        return super.ignorePrecision(runtime, type);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 定义列:定义列:是否忽略小数位<br/>
     * 不直接调用 用来覆盖columnMetadataAdapter(DataRuntime runtime, TypeMetadata meta)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param type TypeMetadata
     * @return boolean
     */
    @Override
    public int ignoreScale(DataRuntime runtime, TypeMetadata type) {
        return super.ignoreScale(runtime, type);
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
        return super.nullable(runtime, builder, meta, action);
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
        return super.charset(runtime, builder, meta);
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
        return super.defaultValue(runtime, builder, meta);
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
        return super.primary(runtime, builder, meta);
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
        return super.unique(runtime, builder, meta);
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
        return super.increment(runtime, builder, meta);
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
        return super.onupdate(runtime, builder, meta);
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
        return super.position(runtime, builder, meta);
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
        return super.comment(runtime, builder, meta);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, table, meta, trigger);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildAddRun(runtime, meta, slice);
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
        return super.buildAlterRun(runtime, meta, slice);
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
        return super.buildDropRun(runtime, meta, slice);
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
        return super.buildRenameRun(runtime, meta, slice);
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
        return super.buildChangeDefaultRun(runtime, meta, slice);
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
        return super.buildChangeNullableRun(runtime, meta, slice);
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
        return super.buildChangeCommentRun(runtime, meta, slice);
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
        return super.buildChangeTypeRun(runtime, meta, slice);
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
        return super.checkTagExists(runtime, builder, exists);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.alter(runtime, table, origin, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildAddRun(runtime, meta, slice);
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
        return super.buildAlterRun(runtime, origin, meta, slice);
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
        return super.buildDropRun(runtime, meta, slice);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.alter(runtime, table, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildAddRun(runtime, meta);
    }

    /**
     * foreign[命令合成]<br/>
     * 修改外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 外键
     * @return List
     */
    @Override

    public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey meta) throws Exception {
        return super.buildAlterRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.alter(runtime, table, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildAppendIndexRun(runtime, meta);
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
        return super.buildAddRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.type(runtime, builder, meta);
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
        return super.property(runtime, builder, meta);
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
        return super.comment(runtime, builder, meta);
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
        return super.checkIndexExists(runtime, builder, exists);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.alter(runtime, table, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildAddRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.add(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.each(runtime, builder, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.parameter(runtime, builder, parameter);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
    }

    /**
     * function[命令合成]<br/>
     * 删除函数
     * @param meta 函数
     * @return String
     */
    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Function meta) throws Exception {
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
    }

    /**
     * sequence[命令合成]<br/>
     * 删除序列
     * @param meta 序列
     * @return String
     */
    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Sequence meta) throws Exception {
        return super.buildDropRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.typeMetadata(runtime, meta);
    }

    /**
     * 转换成相应数据库类型<br/>
     * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型，如有些数据库中用bigint有些数据库中有long
     * @param type 编码时输入的类型
     * @return 具体数据库中对应的数据类型
     */
    @Override
    public TypeMetadata typeMetadata(DataRuntime runtime, String type) {
        return super.typeMetadata(runtime, type);
    }

    /**
     * 检测针对表的主键生成器
     * @param type 数据库类型
     * @param table 表
     * @return PrimaryGenerator
     */
    @Override
    protected PrimaryGenerator checkPrimaryGenerator(DatabaseType type, String table) {
        return super.checkPrimaryGenerator(type, table);
    }

    /**
     * 数据类型拼写兼容
     * @param name name
     * @return spell
     */
    @Override
    public TypeMetadata spell(String name) {
        return super.spell(name);
    }

    /**
     * 合成完整名称
     * @param meta 合成完整名称
     * @return String
     */
    @Override
    public String name(Metadata meta) {
        return super.name(meta);
    }

    /**
     * 构造完整表名
     * @param builder builder
     * @param meta Metadata
     * @return StringBuilder
     */
    @Override
    public StringBuilder name(DataRuntime runtime, StringBuilder builder, Metadata meta) {
        return super.name(runtime, builder, meta);
    }

    /**
     * 拼接完整列名
     * @param builder builder
     * @param meta Column
     * @return StringBuilder
     */
    @Override
    public StringBuilder name(DataRuntime runtime, StringBuilder builder, Column meta) {
        return super.name(runtime, builder, meta);
    }

    /**
     * 拼接界定符
     * @param builder StringBuilder
     * @param src 原文
     * @return StringBuilder
     */
    @Override
    public StringBuilder delimiter(StringBuilder builder, String src) {
        return super.delimiter(builder, src);
    }

    /**
     * 拼接界定符
     * @param builder StringBuilder
     * @param list 原文
     * @return StringBuilder
     */
    @Override
    public StringBuilder delimiter(StringBuilder builder, List<String> list) {
        return super.delimiter(builder, list);
    }

    /**
     * 是否是boolean列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column 列
     * @return boolean
     */
    @Override
    public boolean isBooleanColumn(DataRuntime runtime, Column column) {
        return super.isBooleanColumn(runtime, column);
    }

    /**
     * 是否同数字
     * @param column 列
     * @return boolean
     */
    @Override
    public boolean isNumberColumn(DataRuntime runtime, Column column) {
        return super.isNumberColumn(runtime, column);
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
        return super.isCharColumn(runtime, column);
    }

    /**
     * 内置函数
     * @param column 列属性
     * @param value SQL_BUILD_IN_VALUE
     * @return String
     */
    @Override
    public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
        return super.value(runtime, column, value);
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
    public Object write(DataRuntime runtime, Column metadata, Object value, boolean placeholder) {
        return super.write(runtime, metadata, value, placeholder);
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
        return super.read(runtime, metadata, value, clazz);
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
        super.value(runtime, builder, obj, key);
    }

    /**
     * 参数值 数据类型转换
     * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param table 表
     * @param value  值
     * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
     */
    @Override
    public boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue value) {
        return super.convert(runtime, catalog, schema, table, value);
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
        super.addRunValue(runtime, run, compare, column, value);
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
        return super.convert(runtime, configs, run);
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
        return super.convert(runtime, table, run);
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
        return super.convert(runtime, columns, value);
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
        return super.convert(runtime, metadata, run);
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
        return super.convert(runtime, metadata, value);
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
        return super.convert(runtime, meta, value);
    }

    /**
     * 对象名称格式化(大小写转换)，在查询系统表时需要
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param name name
     * @return String
     */
    @Override
    public String objectName(DataRuntime runtime, String name) {
        return super.objectName(runtime, name);
    }

    /**
     *
     * 根据 catalog, schema, name检测tables集合中是否存在
     * @param list metas
     * @param catalog catalog
     * @param schema schema
     * @param name name
     * @return 如果存在则返回Table 不存在则返回null
     * @param <T> Table
     */
    @Override
    public <T extends Metadata> T search(List<T> list, Catalog catalog, Schema schema, String name) {
        return super.search(list, catalog, schema, name);
    }

    /* *****************************************************************************************************************
     *
     * 														JDBC
     *
     *  ***************************************************************************************************************/

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, DataSource datasource, T meta) {
        super.checkSchema(runtime, datasource, meta);
    }

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, Connection con, T meta) {
        super.checkSchema(runtime, con, meta);
    }
    /**
     * 根据运行环境识别 catalog与schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta Metadata
     * @param <T> Metadata
     */
    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, T meta) {
        super.checkSchema(runtime, meta);
    }

    /**
     * 识别根据jdbc返回的catalog与schema, 部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
     * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
     * @param meta Metadata
     * @param catalog catalog
     * @param schema schema
     * @param overrideMeta 如果meta中有值，是否覆盖
     * @param overrideRuntime 如果runtime中有值，是否覆盖，注意结果集中可能跨多个schema，所以一般不要覆盖runtime,从con获取的可以覆盖ResultSet中获取的不要覆盖
     * @param <T> Metadata
     */
    @Override
    public <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta) {
        super.correctSchemaFromJDBC(runtime, meta, catalog, schema, overrideRuntime, overrideMeta);
    }

    /**
     * 在调用jdbc接口前处理业务中的catalog, schema, 部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
     * @param catalog catalog
     * @param schema schema
     * @return String[]
     */
    @Override
    public String[] correctSchemaFromJDBC(String catalog, String schema) {
        return super.correctSchemaFromJDBC(catalog, schema);
    }

    public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.insertFoot(configs, columns);
    }

    /**
     * 拼接字符串
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param args args
     * @return String
     */
    @Override
    public String concat(DataRuntime runtime, String... args) {
        return super.concat(runtime, args);
    }

    /**
     * 伪表
     * @return String
     */
    protected String dummy() {
        return super.dummy();
    }
    /* *****************************************************************************************************************
     *
     * 														具体数据库
     *
     *  ***************************************************************************************************************/
}
