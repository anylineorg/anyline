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

package org.anyline.data.jdbc.sinodb;

import org.anyline.annotation.AnylineComponent;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.InformixGenusAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.exception.NotSupportException;
import org.anyline.metadata.*;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@AnylineComponent("anyline.data.jdbc.adapter.sinodb")
public class SinoDBAdapter extends InformixGenusAdapter implements JDBCAdapter {
    
    public DatabaseType type() {
        return DatabaseType.SinoDB;
    }
    
    private String delimiter;

    public SinoDBAdapter() {
        super();
        delimiterFr = "\"";
        delimiterTo = "\"";
        for (SinoDBTypeMetadataAlias alias : SinoDBTypeMetadataAlias.values()) {
            clear(alias);
        }
        for (SinoDBTypeMetadataAlias alias : SinoDBTypeMetadataAlias.values()) {
            reg(alias);
            alias(alias.name(), alias.standard());
        }

        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Refer("DATA_LENGTH", null, null, 0, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Refer("DATA_LENGTH", null, null, 0, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1,1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BYTES, new TypeMetadata.Refer("DATA_LENGTH", null, null, 0, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1,1,1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.INT, new TypeMetadata.Refer("DATA_LENGTH", "DATA_PRECISION", null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Refer("DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE", 1, 0, 0));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.DATE, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TIME, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.DATETIME, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.COLLECTION, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.GEOMETRY, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
        MetadataReferHolder.reg(type(), TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Refer("DATA_LENGTH", null, null, 1, 1, 1));
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
     * long insert(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns)
     * [命令合成]
     * public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns)
     * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet<DataRow> set, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
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
        if(data instanceof Collection) {
            long qty = 0;
            Collection list = (Collection) data;
            for(Object item:list) {
                qty += super.insert(runtime, random, batch, dest, item, configs, columns);
            }
            return qty;
        }
        return super.insert(runtime, random, batch, dest, data, configs, columns);
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
        return super.buildInsertRun(runtime, batch, dest, obj, configs, placeholder, unicode, columns);
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
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet<DataRow> set, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        super.fillInsertContent(runtime, run, dest, set, configs, placeholder, unicode, columns);
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
        super.fillInsertContent(runtime, run, dest, list, configs, placeholder, unicode, columns);
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
    public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch) {
        return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
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
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return super.createInsertRun(runtime, dest, obj, configs, placeholder, unicode, columns);
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
    protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return super.createInsertRunFromCollection(runtime, batch, dest, list, configs, placeholder, unicode, columns);
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
        if(data instanceof Collection) {
            long qty = 0;
            Collection list = (Collection) data;
            for(Object item:list) {
                qty += super.insert(runtime, random, item, configs, run, pks);
            }
            return qty;
        }
        return super.insert(runtime, random, data, configs, run, pks);
    }

    /* *****************************************************************************************************************
     * 													UPDATE
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns)
     * [命令合成]
     * Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns)
     * Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String,Column> columns)
     * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String,Column> columns)
     * LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns)
     * LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns)
     * [命令执行]
     * long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run)
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
    public long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns) {
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
    public Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return super.buildUpdateRun(runtime, batch, dest, obj, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns) {
        return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String,Column> columns) {
        return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, placeholder, unicode, columns);
    }

    /**
     * update [命令合成-子流程]<br/>
     * 确认需要更新的列
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
    public LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns) {
        return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
    }

    @Override
    public LinkedHashMap<String,Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns) {
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
    public long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run) {
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
    public long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
        return super.save(runtime, random, dest, data, configs, columns);
    }

    @Override
    protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns) {
        return super.saveCollection(runtime, random, dest, data, configs, columns);
    }

    @Override
    protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return super.saveObject(runtime, random, dest, data, configs, columns);
    }

    @Override
    protected Boolean checkOverride(Object obj, ConfigStore configs) {
        return super.checkOverride(obj, configs);
    }

    @Override
    protected Boolean checkOverrideSync(Object obj, ConfigStore configs) {
        return super.checkOverrideSync(obj, configs);
    }

    @Override
    protected Map<String,Object> checkPv(Object obj) {
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
     * DataSet<DataRow> querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * DataSet<DataRow> querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi)
     * <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions)
     * List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * [命令合成]
     * Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions)
     * List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names)
     * Run fillQueryContent(DataRuntime runtime, Run run)
     * String mergeFinalQuery(DataRuntime runtime, Run run)
     * RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode)
     * Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode)
     * List<RunValue> createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode)
     * [命令执行]
     * DataSet<DataRow> select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run)
     * List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run)
     * Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) 
     * DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names)
     * List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list)
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
    public DataSet<DataRow> querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
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
    public DataSet<DataRow> querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
        return super.querys(runtime, random, procedure, navi);
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return maps 返回map集合
     */
    @Override
    public List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.maps(runtime, random, prepare, configs, conditions);
    }

    /**
     * select[命令合成]<br/> 最终可执行命令<br/>
     * 创建查询SQL
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        return super.buildQueryRun(runtime, prepare, configs, placeholder, unicode, conditions);
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
     * 构造查询主体
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public Run fillQueryContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        return super.fillQueryContent(runtime, run, placeholder, unicode);
    }

    @Override
    protected Run fillQueryContent(DataRuntime runtime, XMLRun run, Boolean placeholder, Boolean unicode) {
        return super.fillQueryContent(runtime, run, placeholder, unicode);
    }

    @Override
    protected Run fillQueryContent(DataRuntime runtime, TextRun run, Boolean placeholder, Boolean unicode) {
        return super.fillQueryContent(runtime, run, placeholder, unicode);
    }

    @Override
    protected Run fillQueryContent(DataRuntime runtime, TableRun run, Boolean placeholder, Boolean unicode) {
        return super.fillQueryContent(runtime, run, placeholder, unicode);
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
    public RunValue createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value, Boolean placeholder, Boolean unicode) {
        return super.createConditionLike(runtime, builder, compare, value, placeholder, unicode);
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
    @Override
    public Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, Boolean placeholder, Boolean unicode) throws NotSupportException {
        return super.createConditionFindInSet(runtime, builder, column, compare, value, placeholder, unicode);
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
        return super.createConditionIn(runtime, builder, compare, value, placeholder, unicode);
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
    public DataSet<DataRow> select(DataRuntime runtime, String random, boolean system, Table table, ConfigStore configs, Run run) {
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
    public List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run) {
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
    public Map<String,Object> map(DataRuntime runtime, String random, ConfigStore configs, Run run) {
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
    public List<Map<String,Object>> process(DataRuntime runtime, List<Map<String,Object>> list) {
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return boolean
     */
    @Override
    public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
        return super.exists(runtime, random, prepare, configs, conditions);
    }

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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    @Override
    public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)  {
        return super.execute(runtime, random, prepare, configs, conditions);
    }

    @Override
    public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values) {
        return super.execute(runtime, random, batch, configs, prepare, values);
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, Boolean placeholder, Boolean unicode, String ... conditions) {
        return super.buildExecuteRun(runtime, prepare, configs, placeholder, unicode, conditions);
    }

    @Override
    protected void fillExecuteContent(DataRuntime runtime, XMLRun run) {
        super.fillExecuteContent(runtime, run);
    }

    @Override
    protected void fillExecuteContent(DataRuntime runtime, TextRun run, Boolean placeholder, Boolean unicode) {
        super.fillExecuteContent(runtime, run, placeholder, unicode);
    }

    @Override
    protected void fillExecuteContent(DataRuntime runtime, TableRun run, Boolean placeholder, Boolean unicode) {
        super.fillExecuteContent(runtime, run, placeholder, unicode);
    }

    /**
     * execute [命令合成-子流程]<br/>
     * 填充 execute 命令内容
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void fillExecuteContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        super.fillExecuteContent(runtime, run, placeholder, unicode);
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
    public <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String key, Collection<T> values) {
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
    public long delete(DataRuntime runtime, String random, String dest, ConfigStore configs, Object obj, String... columns) {
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return 影响行数
     */
    @Override
    public long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions) {
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
    public List<Run> buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, Boolean placeholder, Boolean unicode, String ... columns) {
        return super.buildDeleteRun(runtime, dest, configs, obj, placeholder, unicode, columns);
    }

    /**
     * delete[命令合成]<br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析,表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param key 根据属性解析出列
     * @param values values
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public List<Run> buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, Boolean placeholder, Boolean unicode, String key, Object values) {
        return super.buildDeleteRun(runtime, batch, table, configs, placeholder, unicode, key, values);
    }

    @Override
    public List<Run> buildTruncateRun(DataRuntime runtime, String table) {
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
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String column, Object values) {
        return super.buildDeleteRunFromTable(runtime, batch, table, configs, placeholder, unicode, column, values);
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
        return super.buildDeleteRunFromEntity(runtime, table, configs, obj, placeholder, unicode, columns);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run, Boolean placeholder, Boolean unicode) {
        super.fillDeleteRunContent(runtime, run, placeholder, unicode);
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
     * <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, String random, String name)
     * <T extends Database> List<T> databases(DataRuntime runtime, String random, boolean greedy, String name)
     * Database database(DataRuntime runtime, String random, String name)
     * Database database(DataRuntime runtime, String random)
     * String String product(DataRuntime runtime, String random);
     * String String version(DataRuntime runtime, String random);
     * [命令合成]
     * List<Run> buildQueryDatabasesRun(DataRuntime runtime, boolean greedy, String name)
     * List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy, String name)
     * List<Run> buildQueryProductRun(DataRuntime runtime, boolean greedy, String name)
     * List<Run> buildQueryVersionRun(DataRuntime runtime, boolean greedy, String name)
     * [结果集封装]<br/>
     * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * Database database(DataRuntime runtime, boolean create, Database dataase, DataSet<DataRow> set)
     * Database database(DataRuntime runtime, boolean create, Database dataase)
     * String product(DataRuntime runtime, boolean create, Database product, DataSet<DataRow> set)
     * String product(DataRuntime runtime, boolean create, String product)
     * String version(DataRuntime runtime, int index, boolean create, String version, DataSet<DataRow> set)
     * String version(DataRuntime runtime, boolean create, String version)
     * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog, DataSet<DataRow> set)
     * Catalog catalog(DataRuntime runtime, boolean create, Catalog catalog)
     * Schema schema(DataRuntime runtime, boolean create, Schema schema, DataSet<DataRow> set)
     * Schema schema(DataRuntime runtime, boolean create, Schema schema)
     * Database database(DataRuntime runtime, boolean create, Database dataase)
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
    public String version(DataRuntime runtime, String random) {
        return super.version(runtime, random);
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
        return super.databases(runtime, random, greedy, query);
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
        return super.databases(runtime, random, query);
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
        return super.buildQueryProductRun(runtime);
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
        return super.buildQueryVersionRun(runtime);
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
        return super.buildQueryDatabasesRun(runtime, greedy, query);
    }

    /**
     * database[结果集封装]<br/>
     * database 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initDatabaseFieldRefer() {
        return super.initDatabaseFieldRefer();
    }

    /**
     * database[结果集封装]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception Exception
     */
    @Override
    public <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Database query, DataSet<DataRow> set) throws Exception {
        return super.databases(runtime, index, create, previous, query, set);
    }

    @Override
    public <T extends Database> List<T> databases(DataRuntime runtime, int index, boolean create, List<T> previous, Database query, DataSet<DataRow> set) throws Exception {
        return super.databases(runtime, index, create, previous, query, set);
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
    public Database database(DataRuntime runtime, int index, boolean create, Database meta, DataSet<DataRow> set) throws Exception {
        return super.database(runtime, index, create, meta, set);
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
        return super.database(runtime, create, meta);
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
    public String product(DataRuntime runtime, int index, boolean create, String product, DataSet<DataRow> set) {
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
    public String version(DataRuntime runtime, int index, boolean create, String version, DataSet<DataRow> set) {
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
     * <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, String name)
     * <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
     * [命令合成]
     * List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name)
     * [结果集封装]<br/>
     * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet<DataRow> set)
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet<DataRow> set)
     * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs, DataSet<DataRow> set)
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet<DataRow> set)
     * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet<DataRow> set)
     * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog)
     ******************************************************************************************************************/
    /**
     * catalog[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @return LinkedHashMap
     */
    @Override
    public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, Catalog query) {
        return super.catalogs(runtime, random, query);
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
        return super.catalogs(runtime, random, greedy, query);
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
    public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, Catalog query) throws Exception {
        return super.buildQueryCatalogsRun(runtime, greedy, query);
    }

    /**
     * Catalog[结果集封装]<br/>
     * Catalog 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initCatalogFieldRefer() {
        return super.initCatalogFieldRefer();
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
    public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Catalog query, DataSet<DataRow> set) throws Exception {
        return super.catalogs(runtime, index, create, previous, query, set);
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
    public <T extends Catalog> List<T> catalogs(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog query, DataSet<DataRow> set) throws Exception {
        return super.catalogs(runtime, index, create, previous, query, set);
    }
	/**
     * catalog[结果集封装]<br/>
     * 根据驱动内置接口补充 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous) throws Exception {
        return super.catalogs(runtime, create, previous);
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
        return super.catalogs(runtime, create, previous);
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
    public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog meta, DataSet<DataRow> set) throws Exception {
        return super.catalog(runtime, index, create, meta, set);
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
        return super.catalog(runtime, create, meta);
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
        return super.init(runtime, index, meta, query, row);
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
        return super.detail(runtime, index, meta, query, row);
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
     * LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema, DataSet<DataRow> set)
     * Schema schema(DataRuntime runtime, int index, boolean create, Schema schema)
     ******************************************************************************************************************/
    /**
     * schema[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @return LinkedHashMap
     */
    @Override
    public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, String random, Schema query) {
        return super.schemas(runtime, random, query);
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
        return super.schemas(runtime, random, greedy, query);
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
        return super.buildQuerySchemasRun(runtime, greedy, query);
    }

    /**
     * Schema[结果集封装]<br/>
     * Schema 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initSchemaFieldRefer() {
        return super.initSchemaFieldRefer();
    }

    /**
     * schema[结果集封装]<br/>
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
    public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Schema query, DataSet<DataRow> set) throws Exception {
        return super.schemas(runtime, index, create, previous, query, set);
    }

    @Override
    public <T extends Schema> List<T> schemas(DataRuntime runtime, int index, boolean create, List<T> previous, Schema query, DataSet<DataRow> set) throws Exception {
        return super.schemas(runtime, index, create, previous, query, set);
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
    public Schema schema(DataRuntime runtime, int index, boolean create, Schema meta, DataSet<DataRow> set) throws Exception {
        return super.schema(runtime, index, create, meta, set);
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
        return super.schema(runtime, create, meta);
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
        return super.init(runtime, index, meta, query, row);
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
        return super.detail(runtime, index, meta, query, row);
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
     * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> tables, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Table table, boolean init)
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, Table table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet<DataRow> set)
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
        return super.tables(runtime, random, greedy, query, types, struct, configs);
    }

    /**
     * table[结果集封装-子流程]<br/>
     * 查出所有key并以大写缓存 用来实现忽略大小写
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     */
    @Override
    protected void tableMap(DataRuntime runtime, String random, boolean greedy, Table query, ConfigStore configs) {
        super.tableMap(runtime, random, greedy, query, configs);
    }

    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Table query, int types, int struct, ConfigStore configs) {
        return super.tables(runtime, random, query, types, struct, configs);
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
        return super.buildQueryTablesRun(runtime, greedy, query, types, configs);
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
        return super.buildQueryTablesCommentRun(runtime, query, types);
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
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet<DataRow> set) throws Exception {
        return super.tables(runtime, index, create, previous, query, set);
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
    public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet<DataRow> set) throws Exception {
        return super.tables(runtime, index, create, previous, query, set);
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
        return super.tables(runtime, create, previous, query, types);
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
    public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> previous, Table query, int types) throws Exception {
        return super.tables(runtime, create, previous, query, types);
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
    public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet<DataRow> set) throws Exception {
        return super.comments(runtime, index, create, previous, query, set);
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
    public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, List<T> previous, Table query, DataSet<DataRow> set) throws Exception {
        return super.comments(runtime, index, create, previous, query, set);
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
    public List<Run> buildQueryDdlRun(DataRuntime runtime, Table table) throws Exception {
        return super.buildQueryDdlRun(runtime, table);
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
    public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, table, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													view
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
     * [命令合成]
     * List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, View view)
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, View view)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet<DataRow> set)
     ******************************************************************************************************************/

    /**
     * view[调用入口]<br/>
     * 查询视图
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return List
     * @param <T> View
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, View query, int types, int struct, ConfigStore configs) {
		return super.views(runtime, random, query, types, struct, configs);
	}

    /**
     * view[命令合成]<br/>
     * 查询视图
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return List
     */
    @Override
    public List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, View query, int types, ConfigStore configs) throws Exception {
		return super.buildQueryViewsRun(runtime, greedy, query, types, configs);
	}

    /**
     * View[结果集封装]<br/>
     * View 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initViewFieldRefer() {
        return super.initViewFieldRefer();
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
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, View query, DataSet<DataRow> set) throws Exception {
        return super.views(runtime, index, create, previous, query, set);
    }

    /**
     * view[结果集封装]<br/>
     * 根据根据驱动内置接口补充
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
        return super.views(runtime, create, previous, query, types);
    }

    /**
     * view[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param view 视图
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, View view, boolean init) {
		return super.ddl(runtime, random, view, init);
	}

    /**
     * view[命令合成]<br/>
     * 查询view DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param view view
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlRun(DataRuntime runtime, View view) throws Exception {
        return super.buildQueryDdlRun(runtime, view);
    }

    /**
     * view[结果集封装]<br/>
     * 查询 view DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param view view
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, view, ddls, set);
    }
    /* *****************************************************************************************************************
     * 													master table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs)
     * [命令合成]
     * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables,Catalog catalog, Schema schema, String pattern, int types)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, MasterTable table)
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet<DataRow> set)
     ******************************************************************************************************************/

    /**
     * master table[调用入口]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据 false:只查当前catalog/schema/database范围内数据
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return List
     * @param <T> MasterTable
     */
    @Override
	public <T extends MasterTable> List<T> masters(DataRuntime runtime, String random, boolean greedy, MasterTable query, int types, int struct, ConfigStore configs) {
		return super.masters(runtime, random, greedy, query, types, struct, configs);
	}

    /**
     * master table[命令合成]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return String
     */
    @Override
    public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, boolean greedy, MasterTable query, int types, ConfigStore configs) throws Exception {
		return super.buildQueryMasterTablesRun(runtime, greedy, query, types, configs);
	}

    /**
     * master[结果集封装]<br/>
     * MasterTable 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initMasterTableFieldRefer() {
        return super.initMasterTableFieldRefer();
    }

    /**
     * master table[结果集封装]<br/>
     * 根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, MasterTable query, DataSet<DataRow> set) throws Exception {
        return super.masters(runtime, index, create, previous, query, set);
    }

    /**
     * master table[结果集封装]<br/>
     * 根据根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param previous 上一步查询结果
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, MasterTable query, int types) throws Exception {
        return super.masters(runtime, create, previous, query, types);
    }

    /**
     * master table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta MasterTable
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, MasterTable meta, boolean init) {
		return super.ddl(runtime, random, meta, init);
	}

    /**
     * master table[命令合成]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta MasterTable
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable meta) throws Exception {
        return super.buildQueryDdlRun(runtime, meta);
    }

    /**
     * master table[结果集封装]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param table MasterTable
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, table, ddls, set);
    }
    /* *****************************************************************************************************************
     * 													partition table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
     * [命令合成]
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Catalog catalog, Schema schema, String pattern, int types)
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master, Map<String, Tag> tags, String pattern)
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Table master, Map<String, Tag> tags)
     * [结果集封装]<br/>
     * <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, MasterTable master, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet<DataRow> set)
     * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, MasterTable master)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, PartitionTable table)
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, PartitionTable table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet<DataRow> set)
     ******************************************************************************************************************/
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
        return super.partitions(runtime, random, greedy, query);
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
        return super.buildQueryPartitionTablesRun(runtime, greedy, query, types);
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
    public <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, LinkedHashMap<String, T> previous, PartitionTable query, DataSet<DataRow> set) throws Exception {
        return super.partitions(runtime, total, index, create, previous, query, set);
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
        return super.partitions(runtime, create, previous, query);
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
    public List<Run> buildQueryDdlRun(DataRuntime runtime, PartitionTable table) throws Exception {
        return super.buildQueryDdlRun(runtime, table);
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
    public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, table, ddls, set);
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
     * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet<DataRow> set) throws Exception;
     * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet<DataRow> set) throws Exception;
     * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception;
     ******************************************************************************************************************/
    /**
     * column[调用入口]<br/>
     * 查询表结构
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
        return super.columns(runtime, random, greedy, table, query, primary, configs);
    }

    /**
     * column[调用入口]<br/>
     * 查询列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
     * @return List
     * @param <T> Column
     */
    @Override
     public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Column query, ConfigStore configs) {
        return super.columns(runtime, random, greedy, query, configs);
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
        return super.buildQueryColumnsRun(runtime, metadata, query, configs);
    }

    /**
     * Column[结果集封装]<br/>
     * Column 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initColumnFieldRefer() {
        return super.initColumnFieldRefer();
    }
    
    /**
     * Column[结果集封装]<br/>
     * 数据类型 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initDataTypeFieldRefer() {
        return super.initDataTypeFieldRefer();
    }
    
    /**
     * column[结果集封装]<br/>
     *  根据查询结果集构造Tag
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return tags tags
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet<DataRow> set) throws Exception {
        return super.columns(runtime, index, create, previous, table, query, set);
    }

    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet<DataRow> set) throws Exception {
        return super.columns(runtime, index, create, previous, query, set);
    }

    /**
     * column[结果集封装]<br/>
     * 解析JDBC get columns结果
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param previous 上一步查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Column query) throws Exception {
        return super.columns(runtime, create, previous, query);
    }

    /* *****************************************************************************************************************
     * 													tag
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query)
     * [命令合成]
     * List<Run> buildQueryTagsRun(DataRuntime runtime, boolean greedy, Tag query)
     * [结果集封装]<br/>
     * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Tag query, DataSet<DataRow> set)
     * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern)
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
    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table, Tag query) {
        return super.tags(runtime, random, greedy, table, query);
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
        return super.buildQueryTagsRun(runtime, greedy, query);
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
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Tag query, DataSet<DataRow> set) throws Exception {
        return super.tags(runtime, index, create, previous, query, set);
    }

    /**
     *
     * tag[结果集封装]<br/>
     * 解析JDBC get columns结果
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param previous 上一步查询结果
     * @return tags
     * @throws Exception 异常
     */
    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Tag query) throws Exception {
        return super.tags(runtime, create, previous, query);
    }

    /* *****************************************************************************************************************
     * 													primary
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
     * [命令合成]
     * List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception
     * [结构集封装]
     * <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet<DataRow> set)
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
        return super.primary(runtime, random, greedy, query);
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
        return super.buildQueryPrimaryRun(runtime, greedy, query);
    }

    /**
     * primary[结果集封装]<br/>
     * PrimaryKey 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initPrimaryKeyFieldRefer() {
        return super.initPrimaryKeyFieldRefer();
    }

    /**
     * primary[结构集封装]<br/>
     *  根据查询结果集构造PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param query 查询条件 根据metadata属性
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, PrimaryKey query, DataSet<DataRow> set) throws Exception {
        return super.init(runtime, index, primary, query, set);
    }

    /* *****************************************************************************************************************
     * 													foreign
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table);
     * [命令合成]
     * List<Run> buildQueryForeignsRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception;
     * [结构集封装]
     * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet<DataRow> set) throws Exception;
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
        return super.foreigns(runtime, random, greedy, query);
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
		return super.buildQueryForeignsRun(runtime, greedy, query);
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
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, LinkedHashMap<String, T> previous, ForeignKey query, DataSet<DataRow> set) throws Exception {
		return super.foreigns(runtime, index, previous, query, set);
	}

    /* *****************************************************************************************************************
     * 													index
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
     * <T extends Index> LinkedHashMap<T, Index> indexes(DataRuntime runtime, String random, Table table, String pattern)
     * [命令合成]
     * List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Table table, String name)
     * [结果集封装]<br/>
     * <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet<DataRow> set)
     * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet<DataRow> set)
     * <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> indexes, Table table, boolean unique, boolean approximate)
     * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Table table, boolean unique, boolean approximate)
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
    @Override
    public <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Index query) {
        return super.indexes(runtime, random, greedy, query);
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
        return super.indexes(runtime, random, query);
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
        return super.buildQueryIndexesRun(runtime, greedy, query);
    }

    /**
     * Index[结果集封装]<br/>
     * Index 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initIndexFieldRefer() {
        return super.initIndexFieldRefer();
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
    public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Index query, DataSet<DataRow> set) throws Exception {
		return super.indexes(runtime, index, create, previous, query, set);
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
    public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, List<T> previous, Index query, DataSet<DataRow> set) throws Exception {
		return super.indexes(runtime, index, create, previous, query, set);
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
    public <T extends Index> List<T> indexes(DataRuntime runtime, boolean create, List<T> previous, Index query) throws Exception {
		return super.indexes(runtime, create, previous, query);
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
    public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Index query) throws Exception {
		return super.indexes(runtime, create, previous, query);
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
     * <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, Table table, List<T> constraints, DataSet<DataRow> set) throws Exception;
     * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, Table table, Column column, LinkedHashMap<String, T> constraints, DataSet<DataRow> set) throws Exception;
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
		return super.constraints(runtime, random, greedy, query);
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
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    @Override
    public List<Run> buildQueryConstraintsRun(DataRuntime runtime, boolean greedy, Constraint query) {
		return super.buildQueryConstraintsRun(runtime, greedy, query);
	}

    /**
     * constraint[结果集封装]<br/>
     * Constraint 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initConstraintFieldRefer() {
        return super.initConstraintFieldRefer();
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
    public <T extends Constraint> List<T> constraints(DataRuntime runtime, int index, boolean create, List<T> previous, Constraint query, DataSet<DataRow> set) throws Exception {
		return super.constraints(runtime, index, create, previous, query, set);
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
    public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Constraint query, DataSet<DataRow> set) throws Exception {
        return super.constraints(runtime, index, create, previous, query, set);
    }

    /* *****************************************************************************************************************
     * 													trigger
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events)
     * [命令合成]
     * List<Run> buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Table table, List<Trigger.EVENT> events)
     * [结果集封装]<br/>
     * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet<DataRow> set)
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
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Trigger query) {
		return super.triggers(runtime, random, greedy, query);
	}

    /**
     * trigger[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    public List<Run> buildQueryTriggersRun(DataRuntime runtime, boolean greedy, Trigger query) {
		return super.buildQueryTriggersRun(runtime, greedy, query);
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
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Trigger query, DataSet<DataRow> set) throws Exception {
		return super.triggers(runtime, index, create, previous, query, set);
	}

    /* *****************************************************************************************************************
     * 													procedure
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
     * [命令合成]
     * List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) ;
     * [结果集封装]<br/>
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet<DataRow> set) throws Exception;
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet<DataRow> set) throws Exception;
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures, DataSet<DataRow> set) throws Exception;
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures, DataSet<DataRow> set) throws Exception;
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Procedure procedure);
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, Procedure procedure) throws Exception;
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet<DataRow> set);
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
        return super.procedures(runtime, random, greedy, query);
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
		return super.procedures(runtime, random, query);
	}

    /**
     * procedure[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    @Override
    public List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Procedure query) {
		return super.buildQueryProceduresRun(runtime, greedy, query);
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
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Procedure query, DataSet<DataRow> set) throws Exception {
        return super.procedures(runtime, index, create, previous, query, set);
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
        return super.procedures(runtime, create, previous, query);
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
        return super.procedures(runtime, create, previous, query);
    }
    
    /**
     *
     * procedure[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
     * @param query 查询条件 根据metadata属性
     * @return  LinkedHashMap
     * @param <T> Procedure
     */
    @Override
    public <T extends Procedure> T procedure(DataRuntime runtime, String random, boolean greedy, Procedure query) throws Exception {
        return super.procedure(runtime, random, greedy, query);
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
    public List<Run> buildQueryDdlRun(DataRuntime runtime, Procedure procedure) throws Exception {
        return super.buildQueryDdlRun(runtime, procedure);
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
    public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, procedure, ddls, set);
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
     * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet<DataRow> set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet<DataRow> set) throws Exception;
     * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions, DataSet<DataRow> set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions, DataSet<DataRow> set) throws Exception;
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Function function);
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, Function function) throws Exception;
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet<DataRow> set)
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
		return super.functions(runtime, random, greedy, query);
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
		return super.functions(runtime, random, query);
	}

    /**
     * function[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    @Override
    public List<Run> buildQueryFunctionsRun(DataRuntime runtime, boolean greedy, Function query) {
        return super.buildQueryFunctionsRun(runtime, greedy, query);
    }

    /**
     * Function[结果集封装]<br/>
     * Function 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initFunctionFieldRefer() {
        return super.initFunctionFieldRefer();
    }

    /**
     * function[结果集封装]<br/>
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
    public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> previous, Function query, DataSet<DataRow> set) throws Exception {
        return super.functions(runtime, index, create, previous, query, set);
    }

    /**
     * function[结果集封装]<br/>
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
    public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Function query, DataSet<DataRow> set) throws Exception {
        return super.functions(runtime, index, create, previous, query, set);
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
        return super.functions(runtime, create, previous, query);
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
    public List<Run> buildQueryDdlRun(DataRuntime runtime, Function meta) throws Exception {
        return super.buildQueryDdlRun(runtime, meta);
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
    public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, function, ddls, set);
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
     * <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet<DataRow> set) throws Exception;
     * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet<DataRow> set) throws Exception;
     * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences, DataSet<DataRow> set) throws Exception;
     * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences, DataSet<DataRow> set) throws Exception;
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, Sequence sequence);
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, Sequence sequence) throws Exception;
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet<DataRow> set)
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
        return super.sequences(runtime, random, greedy, query);
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
        return super.sequences(runtime, random, query);
    }

    /**
     * sequence[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param query 查询条件 根据metadata属性
     * @return runs
     */
    @Override
    public List<Run> buildQuerySequencesRun(DataRuntime runtime, boolean greedy, Sequence query) {
		return super.buildQuerySequencesRun(runtime, greedy, query);
	}

    /**
     * sequence[结果集封装]<br/>
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
    public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> previous, Sequence query, DataSet<DataRow> set) throws Exception {
        return super.sequences(runtime, index, create, previous, query, set);
    }

    /**
     * sequence[结果集封装]<br/>
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
    public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Sequence query, DataSet<DataRow> set) throws Exception {
        return super.sequences(runtime, index, create, previous, query, set);
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
        return super.sequences(runtime, create, previous, query);
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
    public List<Run> buildQueryDdlRun(DataRuntime runtime, Sequence meta) throws Exception {
        return super.buildQueryDdlRun(runtime, meta);
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
    public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet<DataRow> set) {
        return super.ddl(runtime, index, sequence, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													common
     * ----------------------------------------------------------------------------------------------------------------
     */
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
    @Override
    public <T extends Metadata> T search(List<T> metas, Catalog catalog, Schema schema, String name) {
        return super.search(metas, catalog, schema, name);
    }

    /**
     *
     * 根据 catalog, name检测schemas集合中是否存在
     * @param schemas schemas
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildAppendColumnCommentRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, catalog);
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
        return super.buildAddCommentRun(runtime, catalog);
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
        return super.checkCatalogExists(runtime, builder, exists);
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
        return super.engine(runtime, builder, meta);
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
        return super.charset(runtime, builder, meta);
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
        return super.comment(runtime, builder, meta);
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
        return super.property(runtime, builder, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildAppendColumnCommentRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, schema);
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
        return super.buildAddCommentRun(runtime, schema);
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
        return super.checkSchemaExists(runtime, builder, exists);
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
        return super.engine(runtime, builder, meta);
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
        return super.charset(runtime, builder, meta);
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
        return super.comment(runtime, builder, meta);
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
        return super.property(runtime, builder, meta);
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
        return super.create(runtime, meta);
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
        return super.alter(runtime, meta);
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
        return super.drop(runtime, meta);
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
        return super.rename(runtime, origin, name);
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
        return super.buildCreateRun(runtime, meta);
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
        return super.buildAlterRun(runtime, meta);
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
        return super.buildRenameRun(runtime, meta);
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
        return super.buildDropRun(runtime, meta);
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
        return super.buildAppendCommentRun(runtime, meta);
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
        return super.buildAppendColumnCommentRun(runtime, meta);
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
        return super.buildChangeCommentRun(runtime, database);
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
        return super.buildAddCommentRun(runtime, database);
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
        return super.checkDatabaseExists(runtime, builder, exists);
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
        return super.engine(runtime, builder, meta);
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
        return super.charset(runtime, builder, meta);
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
        return super.comment(runtime, builder, meta);
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
        return super.property(runtime, builder, meta);
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
     * @throws Exception Exception
     */
    @Override
    public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
        return super.buildCreateRun(runtime, meta);
    }

    
	/**
	 * table[命令合成]<br/>
	 * 修改表 只生成修改表本身属性 不生成关于列及索引的
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 原表
	 * @param update 新表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table meta, Table update) throws Exception {
		return super.buildAlterRun(runtime, meta, update);
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
    public List<Run> buildAlterRun(DataRuntime runtime, Table meta, Collection<Column> columns) throws Exception {
        return super.buildAlterRun(runtime, meta, columns);
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
     * 定义表的主键标识,在创建表的DDL结尾部分(注意不要跟列定义中的主键重复) primary(DataRuntime runtime, StringBuilder builder, Column meta) 
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
     * @param meta 表
     * @return StringBuilder
     */
    @Override
    public StringBuilder columns(DataRuntime runtime, StringBuilder builder, Table meta) {
        return super.columns(runtime, builder, meta);
    }

    /**
     * table[命令合成-子流程]<br/>
     * 创建表 索引部分
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
     * 如CREATE TABLE hr_usr_fi PARTITION OF hr_usr FOR VALUES IN ('FI')
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
     * 子表执行分区依据(分区依据值)如CREATE TABLE hr_usr_fi PARTITION OF hr_usr FOR VALUES IN ('FI')
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
     * @param meta 分区表
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
     * 添加列备注(表创建完成后调用,创建过程能添加备注的不需要实现)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 列
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
     * 列定义:数据类型定义
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta 列
     * @param type 数据类型(已经过转换)
     * @param ignorePrecision 是否忽略长度
     * @param ignoreScale 是否忽略小数
     * @return StringBuilder
     */
    @Override
    public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
        return super.type(runtime, builder, meta, type, ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale);
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
     * 列定义:虚拟列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param meta 列
     * @return StringBuilder
     */
    @Override
    public StringBuilder virtual(DataRuntime runtime, StringBuilder builder, Column meta) {
        return super.virtual(runtime, builder, meta);
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
     * 列定义:定义列的主键标识(注意不要跟表定义中的主键重复) primary(DataRuntime runtime, StringBuilder builder, Table meta)
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
     * List<Run> buildAddRun(DataRuntime runtime, Tag meta)
     * List<Run> buildAlterRun(DataRuntime runtime, Tag meta)
     * List<Run> buildDropRun(DataRuntime runtime, Tag meta, boolean slice)
     * List<Run> buildRenameRun(DataRuntime runtime, Tag meta, boolean slice)
     * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta)
     * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta)
     * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta)
     * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta)
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
     * 修改主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
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
     * 修改主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 主键
     * @return 是否执行成功
     * @throws Exception 异常
     */
    @Override
    public boolean alter(DataRuntime runtime, Table table, PrimaryKey meta) throws Exception {
        return super.alter(runtime, table, meta);
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
     * foreign[调用入口]<br/>
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
     * foreign[调用入口]<br/>
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
     * foreign[调用入口]<br/>
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
     * foreign[调用入口]<br/>
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
     * foreign[调用入口]<br/>
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
     * @param meta 外键
     * @return List
     */

    /**
     * 添加外键
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
     * function[调用入口]<br/>
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
     * function[调用入口]<br/>
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
     * function[调用入口]<br/>
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
     * function[调用入口]<br/>
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
     * sequence[调用入口]<br/>
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
     * sequence[调用入口]<br/>
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
     * sequence[调用入口]<br/>
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
     * sequence[调用入口]<br/>
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
     * 														JDBC
     *
     *  ***************************************************************************************************************/

    @Override
    public <T extends Metadata> void checkSchema(DataRuntime runtime, DataSource datasource, T meta) {
        super.checkSchema(runtime, datasource,meta);
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
     * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
     * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
     * @param meta Metadata
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param overrideMeta 如果meta中有值，是否覆盖
     * @param overrideRuntime 如果runtime中有值，是否覆盖，注意结果集中可能跨多个schema，所以一般不要覆盖runtime,从con获取的可以覆盖ResultSet中获取的不要覆盖
     * @param <T> Metadata
     */
    @Override
    public <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta) {
        super.correctSchemaFromJDBC(runtime, meta, catalog, schema, overrideRuntime, overrideMeta);
    }

    @Override
    public <T extends Metadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema) {
        super.correctSchemaFromJDBC(runtime, meta, catalog, schema);
    }

    /**
     * 在调用jdbc接口前处理业务中的catalog,schema,部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @return String[]
     */
    @Override
    public String[] correctSchemaFromJDBC(String catalog, String schema) {
        return super.correctSchemaFromJDBC(catalog, schema);
    }
   

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据长度列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnFieldLengthRefer(DataRuntime runtime, TypeMetadata meta) {
        return super.columnFieldLengthRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字有效位数列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnFieldPrecisionRefer(DataRuntime runtime, TypeMetadata meta) {
        return super.columnFieldPrecisionRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字小数位数列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnFieldScaleRefer(DataRuntime runtime, TypeMetadata meta) {
        return super.columnFieldScaleRefer(runtime, meta);
    }
    public String insertHead(ConfigStore configs) {
        return super.insertHead(configs);
    }
    public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return super.insertFoot(configs, columns);
    }
   

    /**
     * 内置函数 多种数据库兼容时需要
     * @param value SQL_BUILD_IN_VALUE
     * @return String
     */
    @Override
    public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
        return super.value(runtime, column, value);
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
