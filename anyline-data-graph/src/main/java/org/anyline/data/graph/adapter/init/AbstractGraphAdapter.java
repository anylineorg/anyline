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

package org.anyline.data.graph.adapter.init;

import org.anyline.data.adapter.init.AbstractDriverAdapter;
import org.anyline.data.entity.Join;
import org.anyline.data.handler.DataHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.AutoPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.CommandException;
import org.anyline.exception.CommandQueryException;
import org.anyline.exception.CommandUpdateException;
import org.anyline.exception.NotSupportException;
import org.anyline.metadata.*;
import org.anyline.metadata.graph.EdgeTable;
import org.anyline.metadata.graph.VertexTable;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.*;

public abstract class AbstractGraphAdapter extends AbstractDriverAdapter {

    public AbstractGraphAdapter() {
        super();
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.COMMON;
    }

    @Override
    public boolean supportCatalog() {
        return true;
    }

    @Override
    public boolean supportSchema() {
        return true;
    }

    private static Map<Type, String> types = new HashMap<>();
    static {
        types.put(Table.TYPE.NORMAL, "BASE TABLE");
        types.put(Table.TYPE.VIEW, "VIEW");
        types.put(View.TYPE.NORMAL, "VIEW");
        types.put(Metadata.TYPE.TABLE, "BASE TABLE");
        types.put(Metadata.TYPE.VIEW, "VIEW");
    }

    @Override
    public String name(Type type) {
        return types.get(type);
    }

    /**
     * 验证运行环境与当前适配器是否匹配<br/>
     * 默认不连接只根据连接参数<br/>
     * 只有同一个种区分不同版本(如mmsql2000/mssql2005)或不同模式(如kingbase的oracle/pg模式)时才需要单独实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
     * @return boolean
     */
    @Override
    public boolean match(DataRuntime runtime, String feature, String adapterKey, boolean compensate) {
        return super.match(runtime, feature, adapterKey, compensate);
    }

    @Override
    public boolean match(String feature, List<String> keywords, boolean compensate) {
        return super.match(feature, keywords, compensate);
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
     * 执行前根据主键生成器补充主键值, 执行完成后会补齐自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 需要插入入的数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须插入<br/>
     *                -:表示必须不插入<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns, 长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
     *
     *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比, 删除表中没有的列<br/>
     * @return 影响行数
     */
    @Override
    public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return super.insert(runtime, random, batch, dest, data, configs, columns);
    }

    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 需要插入的数据
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
            if(!list.isEmpty()) {
                run = createInsertRunFromCollection(runtime, batch, dest, list, configs, placeholder, unicode, columns);
            }
            run.setRows(list.size());
        }else {
            run = createInsertRun(runtime, dest, obj, configs, placeholder, unicode, columns);
            run.setRows(1);
        }
        convert(runtime, configs, run);
        return run;
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param set 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        super.fillInsertContent(runtime, run, dest, set, configs, placeholder, unicode, columns);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        StringBuilder builder = run.getBuilder();
        int batch = run.getBatch();
        if(null == builder) {
            builder = new StringBuilder();
            run.setBuilder(builder);
        }
        checkName(runtime, null, dest);
        if(list instanceof DataSet) {
            DataSet set = (DataSet) list;
            this.fillInsertContent(runtime, run, dest, set, configs, placeholder, unicode, columns);
            return;
        }
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        Object entity = list.iterator().next();
        List<String> pks = null;
        if(null != generator) {
            columns.putAll(EntityAdapterProxy.primaryKeys(entity.getClass()));
        }
        String head = insertHead(configs);
        builder.append(head);//
        name(runtime, builder, dest);// .append(parseTable(dest));
        builder.append("(");
        delimiter(builder, Column.names(columns));
        builder.append(") VALUES ");
        int dataSize = list.size();
        int idx = 0;
        if(batch > 1) {
            //批量执行
            builder.append("(");
            int size = columns.size();
            run.setVol(size);
            for(int i=0; i<size; i++) {
                if(i>0) {
                    builder.append(", ");
                }
                builder.append("?");
            }
            builder.append(")");
        }
        for(Object obj:list) {
            /*if(obj instanceof DataRow) {
                DataRow row = (DataRow)obj;
                if (row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())) {
                    createPrimaryValue(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
                }
                insertValue(template, run, row, true, false, true, keys);
            }else{*/
            boolean create = EntityAdapterProxy.createPrimaryValue(obj, Column.names(columns));
            if(!create && null != generator) {
                generator.create(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
                //createPrimaryValue(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
            }
            builder.append(insertValue(runtime, run, obj, true, true, false, true, columns));
            //}
            if(idx<dataSize-1 && batch <= 1) {
                //多行数据之间的分隔符
                builder.append(batchInsertSeparator());
            }
            idx ++;
        }
        builder.append(insertFoot(configs, columns));
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 确认需要插入的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj  Entity或DataRow
     * @param batch  是否批量，批量时不检测值是否为空
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须插入<br/>
     *                -:表示必须不插入<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns, 长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
     *
     *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比, 删除表中没有的列<br/>
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns, boolean batch) {
        return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 批量插入数据时, 多行数据之间分隔符
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
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        Run run = new TableRun(runtime, dest);
        // List<Object> values = new ArrayList<Object>();
        StringBuilder builder = new StringBuilder();
        if(BasicUtil.isEmpty(dest)) {
            throw new CommandException("未指定表");
        }
        if(null == configs) {
            configs = new DefaultConfigStore();
        }
        if(null == placeholder) {
            placeholder = configs.getPlaceholder();
        }
        if(null == placeholder) {
            placeholder = true;
        }

        checkName(runtime, null, dest);
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());

        int type = 1;
        StringBuilder valuesBuilder = new StringBuilder();
        DataRow row = null;
        if(obj instanceof Map) {
            if(!(obj instanceof DataRow)) {
                obj = new DataRow((Map) obj);
            }
        }
        if(obj instanceof DataRow) {
            row = (DataRow)obj;
            if(null == unicode) {
                unicode = row.getUnicode();
            }
            if(row.hasPrimaryKeys() && null != generator) {
                generator.create(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
                //createPrimaryValue(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
            }
        }else{
            type = 2;
            boolean create = EntityAdapterProxy.createPrimaryValue(obj, columns);
            LinkedHashMap<String, Column> pks = EntityAdapterProxy.primaryKeys(obj.getClass());
            if(!create && null != generator) {
                generator.create(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
                //createPrimaryValue(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
            }
        }
        run.setOriginType(type);
        /*确定需要插入的列*/
        LinkedHashMap<String, Column> cols = confirmInsertColumns(runtime, dest, obj, configs, columns, false);
        if(null == cols || cols.isEmpty()) {
            throw new CommandException("未指定列(DataRow或Entity中没有需要插入的属性值)["+obj.getClass().getName()+":"+BeanUtil.object2json(obj)+"]");
        }
        boolean replaceEmptyNull = false;
        if(obj instanceof DataRow) {
            row = (DataRow)obj;
            replaceEmptyNull = row.isReplaceEmptyNull();
        }else{
            replaceEmptyNull = ConfigStore.IS_REPLACE_EMPTY_NULL(configs);
        }
        if(null == unicode) {
            unicode = configs.getUnicode();
        }
        if(null == unicode) {
            unicode = false;
        }
        String head = insertHead(configs);
        builder.append(head);//.append(parseTable(dest));
        name(runtime, builder, dest);
        builder.append("(");
        valuesBuilder.append(") VALUES (");
        List<String> insertColumns = new ArrayList<>();
        boolean first = true;
        for(Column column:cols.values()) {
            if(!first) {
                builder.append(", ");
                valuesBuilder.append(", ");
            }
            first = false;
            String key = column.getName();
            Object value = null;
            if(!(obj instanceof Map) && EntityAdapterProxy.hasAdapter(obj.getClass())) {
                value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
            }else{
                value = BeanUtil.getFieldValue(obj, key, true);
            }

            String str = null;
            if(value instanceof String) {
                str = (String)value;
            }
            delimiter(builder, key);

            //if (str.startsWith("${") && str.endsWith("}")) {
            if (BasicUtil.checkEl(str)) {
                value = str.substring(2, str.length()-1);
                valuesBuilder.append(value);
            }else if(value instanceof SQL_BUILD_IN_VALUE) {
                //内置函数值
                value = value(runtime, null, (SQL_BUILD_IN_VALUE)value);
                valuesBuilder.append(value);
            }else{
                insertColumns.add(key);
                if(supportInsertPlaceholder() && placeholder) {
                    valuesBuilder.append("?");
                    if ("NULL".equals(value)) {
                        value = null;
                    }else if("".equals(value) && replaceEmptyNull) {
                        value = null;
                    }
                    addRunValue(runtime, run, Compare.EQUAL, column, value);
                }else{
                    //format(valuesBuilder, value);
                    valuesBuilder.append(write(runtime, null, value, false, unicode));
                }
            }
        }
        valuesBuilder.append(")");
        builder.append(valuesBuilder);
        builder.append(insertFoot(configs, cols));
        run.setBuilder(builder);
        run.setInsertColumns(insertColumns);
        return run;
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 根据collection创建 INSERT RunPrepare由buildInsertRun调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 对象集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
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
        LinkedHashMap<String, Column> cols = confirmInsertColumns(runtime, dest, first, configs, columns, true);
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

    public String insertHead(ConfigStore configs) {
        return "INSERT INTO ";
    }
    public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns) {
        return "";
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
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param configs 条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns, 长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
     *
     *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比, 删除表中没有的列<br/>
     * @return 影响行数
     */
    @Override
    public long update(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns) {
        return super.update(runtime, random, batch, dest, data, configs, columns);
    }

    /**
     * update [命令合成]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj Entity或DtaRow
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns, 长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
     *
     *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比, 删除表中没有的列<br/>
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public Run buildUpdateRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, List<String> columns) {
        return super.buildUpdateRun(runtime, batch, dest, obj, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, placeholder, unicode, columns);
    }

    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return null;
    }

    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return null;
    }

    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, Boolean placeholder, Boolean unicode, LinkedHashMap<String, Column> columns) {
        return null;
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
     *        如果没有提供columns, 长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
     *
     *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比, 删除表中没有的列<br/>
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, DataRow row, ConfigStore configs, List<String> columns) {
        return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
    }

    @Override
    public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns) {
        return super.confirmUpdateColumns(runtime, dest, obj, configs, columns);
    }

    /**
     * update [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    @Override
    public long update(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) {
        long result = 0;
        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
            }
            return -1;
        }
        String sql = null;
        sql = run.getFinalUpdate();
        if(BasicUtil.isEmpty(sql)) {
            log.warn("[不具备更新条件][dest:{}]", dest);
            return -1;
        }
        if(null != configs) {
            configs.add(run);
        }
        List<Object> values = run.getValues();
        int batch = run.getBatch();
        String action = "update";
        if(batch > 1) {
            action = "batch update";
        }
        long fr = System.currentTimeMillis();

        /*执行SQL*/
        if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
            if(batch > 1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
                log.info("{}[action:{}][table:{}]{}", random, action, run.getTable(), run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }else {
                log.info("{}[action:update][table:{}]{}", random, run.getTable(), run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
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

            if(batch > 1) {
            }else {
            }
            millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[slow cmd][action:{}][table:{}][执行耗时:{}]{}", random, action, run.getTable(), DateUtil.format(millis), run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, sql, values, null, true, result, millis);
                    }
                }
            }
            if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
                String qty = result+"";
                if(batch>1) {
                    qty = "约"+result;
                }
                log.info("{}[action:{}][table:{}][执行耗时:{}][影响行数:{}]", random, action, run.getTable(), DateUtil.format(millis), LogUtil.format(qty, 34));
            }

        }catch(Exception e) {
            if (ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                e.printStackTrace();
            }
            if (ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                CommandUpdateException ex = new CommandUpdateException("update异常:" + e, e);
                ex.setCmd(sql);
                ex.setValues(values);
                throw ex;
            }
            if (ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:update][table:{}]{}", random, run.getTable(), LogUtil.format("更新异常:", 33) + e, run.log(ACTION.DML.UPDATE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
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
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param configs 更新条件
     * @param columns 需要插入或更新的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     *                列可以加前缀<br/>
     *                +:表示必须更新<br/>
     *                -:表示必须不更新<br/>
     *                ?:根据是否有值<br/>
     *
     *        如果没有提供columns, 长度为0也算没有提供<br/>
     *        则解析obj(遍历所有的属性工Key)获取insert列<br/>
     *
     *        如果提供了columns则根据columns获取insert列<br/>
     *
     *        但是columns中出现了添加前缀列, 则解析完columns后, 继续解析obj<br/>
     *
     *        以上执行完后, 如果开启了ConfigTable.IS_AUTO_CHECK_METADATA=true<br/>
     *        则把执行结果与表结构对比, 删除表中没有的列<br/>
     * @return 影响行数
     */
    @Override
    public long save(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns) {
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
        DataSet set = null;
        final List<Parameter> inputs = procedure.getInputs();
        final List<Parameter> outputs = procedure.getOutputs();
        if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
            log.info("{}[action:procedure][cmd:\n{}\n][input param:{}]\n[output param:{}]", random, procedure.getName(), LogUtil.param(inputs), LogUtil.param(outputs));
        }
        final String rdm = random;
        long millis = -1;
        try{

            ACTION.SWITCH swt = InterceptorProxy.prepareQuery(runtime, random, procedure, navi);
            if(swt == ACTION.SWITCH.BREAK) {
                return new DataSet();
            }
            swt = InterceptorProxy.beforeQuery(runtime, random, procedure, navi);
            if(swt == ACTION.SWITCH.BREAK) {
                return new DataSet();
            }
            if(null != dmListener) {
                dmListener.beforeQuery(runtime, random, procedure);
            }
            final DataRuntime rt = runtime;
            long fr = System.currentTimeMillis();
            millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigTable.SLOW_SQL_MILLIS;
            if(SLOW_SQL_MILLIS > 0 && ConfigTable.IS_LOG_SLOW_SQL) {
                if(millis > SLOW_SQL_MILLIS) {
                    log.warn("{}[slow cmd][action:procedure][执行耗时:{}][cmd:\n{}\n][input param:{}]\n[output param:{}]"
                            , random
                            , DateUtil.format(millis)
                            , procedure.getName()
                            , LogUtil.param(inputs)
                            , LogUtil.param(outputs));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.PROCEDURE, null, procedure.getName(), inputs, outputs, true, set, millis);
                    }
                }
            }
/*            if(null != queryInterceptor) {
                queryInterceptor.after(procedure, set, millis);
            }*/
            if(!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[action:procedure][执行耗时:{}]", random, DateUtil.format(millis));
            }
        }catch(Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
                CommandQueryException ex = new CommandQueryException("query异常:" + e, e);
                throw ex;
            }else{
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][action:procedure][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]"
                            , random
                            , LogUtil.format("存储过程查询异常:", 33)+e.toString()
                            , procedure.getName()
                            , LogUtil.param(inputs)
                            , LogUtil.param(outputs));
                }
            }
        }
        return set;
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
     * 对性能有要求的场景调用，返回java原生map集合, 结果中不包含元数据信息
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return maps 返回map集合
     */
    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions) {
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
    protected Run fillQueryContent(DataRuntime runtime, TableRun run,  Boolean placeholder, Boolean unicode) {
        StringBuilder builder = run.getBuilder();
        fillQueryContent(runtime, builder, run);
        //UNION
        List<Run> unions = run.getUnions();
        if(null != unions) {
            for(Run union:unions) {
                builder.append("\n UNION ");
                if(union.isUnionAll()) {
                    builder.append(" ALL ");
                }
                builder.append("\n");
                fillQueryContent(runtime, builder, union, placeholder, unicode);
            }
        }
        run.appendOrderStore();
        run.checkValid();
        return run;
    }
    protected Run fillQueryContent(DataRuntime runtime, StringBuilder builder, TableRun run) {
        TablePrepare sql = (TablePrepare)run.getPrepare();
        builder.append("SELECT ");
        if(null != sql.getDistinct()) {
            builder.append(sql.getDistinct());
        }
        builder.append(BR_TAB);
        LinkedHashMap<String,Column> columns = sql.getColumns();
        if(null == columns || columns.isEmpty()) {
            ConfigStore configs = run.getConfigs();
            if(null != configs) {
                List<String> cols = configs.columns();
                columns = new LinkedHashMap<>();
                for(String col:cols) {
                    columns.put(col.toUpperCase(), new Column(col));
                }
            }
        }
        if(null != columns && !columns.isEmpty()) {
            // 指定查询列
            boolean first = true;
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
                    builder.append(column);
                }else{
                    if(name.contains("(") || name.contains(",")) {
                        builder.append(name);
                    }else if(name.toUpperCase().contains(" AS ")) {
                        int split = name.toUpperCase().indexOf(" AS ");
                        String tmp = name.substring(0, split).trim();
                        delimiter(builder, tmp);
                        builder.append(" ");
                        tmp = name.substring(split+4).trim();
                        delimiter(builder, tmp);
                    }else if("*".equals(name)) {
                        builder.append("*");
                    }else{
                        delimiter(builder, name);
                    }
                }
            }
            builder.append(BR);
        }else{
            // 全部查询
            builder.append("*");
            builder.append(BR);
        }
        Table table = run.getTable();
        builder.append("FROM").append(BR_TAB);
        name(runtime, builder, table);
        String alias = table.getAlias();
        if(BasicUtil.isNotEmpty(alias)) {
            builder.append(" ");
            delimiter(builder, alias);
        }
        builder.append(BR);
        List<RunPrepare> joins = sql.getJoins();
        if(null != joins) {
            for (RunPrepare join:joins) {
                Join jn = join.getJoin();
                builder.append(BR_TAB).append(jn.getType().getCode()).append(" ");
                Table joinTable = join.getTable();
                String jionTableAlias = joinTable.getAlias();
                name(runtime, builder, joinTable);
                if(BasicUtil.isNotEmpty(jionTableAlias)) {
                    builder.append("  ").append(jionTableAlias);
                }
                builder.append(" ON ").append(jn.getConditions().getRunText(runtime, false));
            }
        }

        //builder.append("\nWHERE 1=1\n\t");
        /*添加查询条件*/
        // appendConfigStore();
        run.appendCondition(this, true,false, true);
        run.appendGroup(runtime, false, true);
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
        int code = compare.getCode();
        if(code > 100) {
            builder.append(" NOT");
            code = code - 100;
        }
        // %A% 50
        // A%  51
        // %A  52
        // NOT %A% 150
        // NOT A%  151
        // NOT %A  152
        if(compare == Compare.LIKE_SIMPLE) {
            builder.append(" LIKE ?");
        }else if(compare == Compare.LIKE_SIMPLE_IGNORE_CASE) {
            builder.append(" LIKE ?");
        }else if(code == 50) {
            builder.append(" LIKE ").append(concat(runtime, "'%'","?","'%'"));
        }else if(code == 51) {
            builder.append(" LIKE ").append(concat(runtime, "?","'%'"));
        }else if(code == 52) {
            builder.append(" LIKE ").append(concat(runtime, "'%'","?"));
        }
        RunValue run = new RunValue();
        run.setValue(value);
        return run;
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
        if(compare == Compare.NOT_IN) {
            builder.append(" NOT");
        }
        builder.append(" IN (");
        if(value instanceof Collection) {
            Collection<Object> coll = (Collection)value;
            int size = coll.size();
            for(int i=0; i<size; i++) {
                builder.append("?");
                if(i < size-1) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        }else{
            builder.append("= ?");
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
        if(run instanceof ProcedureRun) {
            ProcedureRun pr = (ProcedureRun)run;
            return querys(runtime, random, pr.getProcedure(), configs.getPageNavi());
        }
        String cmd = run.getFinalQuery();
        if(BasicUtil.isEmpty(cmd)) {
            return new DataSet().setTable(table);
        }
        List<Object> values = run.getValues();
        return select(runtime, random, system, ACTION.DML.SELECT, table, configs, run, cmd, values);
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
        long fr = System.currentTimeMillis();
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
            StreamHandler _handler = null;
            if(null != configs) {
                DataHandler handler = configs.handler();
                if(handler instanceof StreamHandler) {
                    _handler = (StreamHandler) handler;
                }
            }
            long[] count = new long[]{0};
            final boolean[] process = {false};
            final StreamHandler handler = _handler;
            final long[] mid = {System.currentTimeMillis()};
            if(null != handler) {
                boolean keep = handler.keep();
                try {

                }finally {

                }

                maps = new ArrayList<>();
                //end stream handler
            }else {

            }
            boolean slow = false;
            if(ConfigStore.SLOW_SQL_MILLIS(configs) > 0) {
                if(mid[0]-fr > ConfigStore.SLOW_SQL_MILLIS(configs)) {
                    slow = true;
                    log.warn("{}[slow cmd][action:select][执行耗时:{}]{}", random, DateUtil.format(mid[0]-fr), run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, maps, mid[0]-fr);
                    }
                }
            }
            if(!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
                log.info("{}[action:select][执行耗时:{}]", random, DateUtil.format(mid[0] - fr));
            }
            maps = process(runtime, maps);
            if(!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
                log.info("{}[action:select][封装耗时:{}][封装行数:{}]", random, DateUtil.format(System.currentTimeMillis() - mid[0]), count[0]);
            }
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                e.printStackTrace();
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:select]{}", random, LogUtil.format("查询异常:", 33) + e, run.log(ACTION.DML.SELECT, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                CommandQueryException ex = new CommandQueryException("query异常:" + e, e);
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
        if (log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
            log.info("{}[action:select]{}", random, run.log(ACTION.DML.EXISTS, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
        }
        /*if(null != values && values.size()>0 && BasicUtil.isEmpty(true, values)) {
            //>0:有占位 isEmpty:值为空
        }else{*/
        boolean exe = true;
        if(null != configs) {
            exe = configs.execute();
        }
        if(!exe) {
            return new HashMap<>();
        }
        try {

        }catch (Exception e) {
            if(ConfigStore.IS_THROW_SQL_QUERY_EXCEPTION(configs)) {
                throw e;
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
        if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
            if(millis > SLOW_SQL_MILLIS) {
                slow = true;
                log.warn("{}[slow cmd][action:exists][执行耗时:{}][cmd:\n{}\n]\n[param:{}]", random, DateUtil.format(millis), sql, LogUtil.param(values));
                if(null != dmListener) {
                    dmListener.slow(runtime, random, ACTION.DML.EXISTS, run, sql, values, null, true, map, millis);
                }
            }
        }
        if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
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
        List<Run> runs = buildQuerySequence(runtime, next, names);
        if (null != runs && !runs.isEmpty()) {
            Run run = runs.get(0);
            if(!run.isValid()) {
                if(ConfigTable.IS_LOG_SQL && log.isWarnEnabled()) {
                    log.warn("[valid:false][不具备执行条件][sequence:"+names);
                }
                return new DataRow();
            }
            DataSet set = select(runtime, random, true, (Table)null, null, run);
            if (!set.isEmpty()) {
                return set.getRow(0);
            }
        }
        return new DataRow();
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
        //select * from user
        //select (select id from a) as a, id as b from (select * from suer) where a in (select a from b)
        String base = run.getBuilder().toString();
        StringBuilder builder = new StringBuilder();
        boolean simple= false;
        String upper = base.toUpperCase();
        if(upper.split("FROM").length == 2) {
            //只有一个表
            //没有聚合 去重
            if(!upper.contains("DISTINCT") && !upper.contains("GROUP")) {
                simple = true;
            }
        }
        if(simple) {
            int idx = base.toUpperCase().indexOf("FROM");
            builder.append("SELECT COUNT(*) AS CNT FROM ").append(base.substring(idx+5));
        }else{
            builder.append("SELECT COUNT(*) AS CNT FROM (\n").append(base).append("\n) F");
        }
        String sql = builder.toString();
        sql = sql.replaceAll("WHERE\\s*1=1\\s*AND","WHERE ");
        return sql;
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
        long total = 0;
        DataSet set = select(runtime, random, false, ACTION.DML.COUNT, null, null, run, run.getTotalQuery(), run.getValues());
        if(!set.isEmpty()) {
            total = set.getRow(0).toUpperKey().getLong("CNT", 0L);
        }
        return total;
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
    public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs,  String ... conditions) {
        boolean result = false;
        if(null == random) {
            random = random(runtime);
        }
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if (null != dmListener) {
            swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
        }
        if(swt == ACTION.SWITCH.BREAK) {
            return false;
        }
        Run run = buildQueryRun(runtime, prepare, configs, true, true, conditions);
        if(!run.isValid()) {
            if(log.isWarnEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
                log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTableName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
            }
            return false;
        }
        if(null != dmListener) {
            dmListener.beforeExists(runtime, random, run);
        }
        long fr = System.currentTimeMillis();
        Map<String, Object> map = map(runtime, random, configs, run);
        if (null == map) {
            result = false;
        } else {
            result = BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
        }
        Long millis = System.currentTimeMillis() - fr;
        if(null != dmListener) {
            dmListener.afterExists(runtime, random, run, true, result, millis);
        }
        return result;
    }

    @Override
    public String mergeFinalExists(DataRuntime runtime, Run run) {
        String sql = "SELECT EXISTS(\n" + run.getBuilder().toString() +"\n)  IS_EXISTS";
        sql = sql.replaceAll("WHERE\\s*1=1\\s*AND","WHERE ");
        return sql;
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
        boolean result = false;
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        boolean cmd_success = false;
        List<Object> list = new ArrayList<Object>();
        final List<Parameter> inputs = procedure.getInputs();
        final List<Parameter> outputs = procedure.getOutputs();
        long fr = System.currentTimeMillis();
        String sql = " {";

        // 带有返回值
        int returnIndex = 0;
        if(procedure.hasReturn()) {
            sql += "? = ";
            returnIndex = 1;
        }
        sql += "call " +procedure.getName()+"(";
        final int sizeIn = inputs.size();
        final int sizeOut = outputs.size();
        final int size = sizeIn + sizeOut;
        for(int i=0; i<size; i++) {
            sql += "?";
            if(i < size-1) {
                sql += ",";
            }
        }
        sql += ")}";

        if(ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
            log.info("{}[action:procedure][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]", random, sql, LogUtil.param(inputs), LogUtil.param(outputs));
        }
        long millis= -1;
        try{

            cmd_success = true;
            procedure.setResult(list);
            result = true;
            millis = System.currentTimeMillis() - fr;

            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigTable.SLOW_SQL_MILLIS;
            if(SLOW_SQL_MILLIS > 0 && ConfigTable.IS_LOG_SLOW_SQL) {
                if(millis > SLOW_SQL_MILLIS) {
                    log.warn("{}[slow cmd][action:procedure][执行耗时:{}][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]", random, DateUtil.format(millis), sql, LogUtil.param(inputs), LogUtil.param(list));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.PROCEDURE, null, sql, inputs, list, true, result, millis);
                    }
                }
            }
            if (null != dmListener) {
                dmListener.afterExecute(runtime, random, procedure, result, millis);
            }
            if (!slow && ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
                log.info("{}[action:procedure][执行耗时:{}]\n[output param:{}]", random, DateUtil.format(millis), list);
            }

        }catch(Exception e) {
            result = false;
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }
            if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
                CommandUpdateException ex = new CommandUpdateException("execute异常:" + e, e);
                ex.setCmd(sql);
                throw ex;
            }else{
                if(ConfigTable.IS_LOG_SQL_WHEN_ERROR) {
                    log.error("{}[{}][action:procedure][cmd:\n{}\n]\n[input param:{}]\n[output param:{}]", random, LogUtil.format("存储过程执行异常:", 33)+e.toString(), sql, LogUtil.param(inputs), LogUtil.param(outputs));
                }
            }
        }
        return result;
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
        if(log.isInfoEnabled() && ConfigStore.IS_LOG_SQL(configs)) {
            if(batch >1 && !ConfigStore.IS_LOG_BATCH_SQL_PARAM(configs)) {
                log.info("{}[action:{}][cmd:\n{}\n]\n[param size:{}]", random, action, sql, values.size());
            }else {
                log.info("{}[action:{}]{}", random, action, run.log(ACTION.DML.EXECUTE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
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

            if(batch>1) {
            }else {
            }
            millis = System.currentTimeMillis() - fr;
            boolean slow = false;
            long SLOW_SQL_MILLIS = ConfigStore.SLOW_SQL_MILLIS(configs);
            if(SLOW_SQL_MILLIS > 0 && ConfigStore.IS_LOG_SLOW_SQL(configs)) {
                if(millis > SLOW_SQL_MILLIS) {
                    slow = true;
                    log.warn("{}[slow cmd][action:{}][执行耗时:{}][cmd:\n{}\n]\n[param:{}]", random, action, DateUtil.format(millis), sql, LogUtil.param(values));
                    if(null != dmListener) {
                        dmListener.slow(runtime, random, ACTION.DML.EXECUTE, run, sql, values, null, true, result, millis);
                    }
                }
            }
            if (!slow && log.isInfoEnabled() && ConfigStore.IS_LOG_SQL_TIME(configs)) {
                String qty = "" + result;
                if(batch>1) {
                    qty = "约" + result;
                }
                log.info("{}[action:{}][执行耗时:{}][影响行数:{}]", random, action, DateUtil.format(millis), LogUtil.format(qty, 34));
            }
        }catch(Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                e.printStackTrace();
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.error("{}[{}][action:{}]{}", random, LogUtil.format("命令执行异常:", 33)+e, action, run.log(ACTION.DML.EXECUTE, ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
            }
            if(ConfigStore.IS_THROW_SQL_UPDATE_EXCEPTION(configs)) {
                throw e;
            }

        }
        return result;
    }

    /* *****************************************************************************************************************
     *                                                     DELETE
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T> long deletes(DataRuntime runtime, String random, int batch, Table table, ConfigStore configs, String column, Collection<T> values)
     * long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, Object obj, String... columns)
     * long delete(DataRuntime runtime, String random, Table table, ConfigStore configs, String... conditions)
     * long truncate(DataRuntime runtime, String random, Table table)
     * [命令合成]
     * List<Run> buildDeleteRun(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns)
     * List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values)
     * List<Run> buildTruncateRun(DataRuntime runtime, Table table)
     * List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values)
     * List<Run> buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String ... columns)
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
     * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
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
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
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
    public List<Run> buildDeleteRun(DataRuntime runtime, int batch, Table table, ConfigStore configs, Boolean placeholder, Boolean unicode, String key, Object values) {
        return super.buildDeleteRun(runtime, batch, table, configs, placeholder, unicode, key, values);
    }

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
     * @param key 列
     * @param values values
     * @return Run 最终执行命令 如JDBC环境中的 SQL 与 参数值
     */
    @Override
    public List<Run> buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs,  Boolean placeholder, Boolean unicode, String key, Object values) {
        List<Run> runs = new ArrayList<>();
        if(null == table || null == key || null == values) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        TableRun run = new TableRun(runtime, table);
        builder.append("DELETE FROM ");
        name(runtime, builder, table);
        builder.append(" WHERE ");

        if(values instanceof Collection) {
            Collection cons = (Collection)values;
            delimiter(builder, key);
            if(batch >1) {
                builder.append(" = ?");
                List<Object> list = null;
                if(values instanceof List) {
                    list = (List<Object>) values;
                }else{
                    list = new ArrayList<>();
                    for(Object item:cons) {
                        list.add(item);
                    }
                }
                run.setValues(key, list);
                run.setVol(1);
                run.setBatch(batch);
            }else {
                if (cons.size() > 1) {
                    builder.append(" IN(");
                    int idx = 0;
                    for (Object obj : cons) {
                        if (idx > 0) {
                            builder.append(", ");
                        }
                        // builder.append("'").append(obj).append("'");
                        builder.append("?");
                        idx++;
                    }
                    builder.append(")");
                } else if (cons.size() == 1) {
                    for (Object obj : cons) {
                        builder.append("=?");
                    }
                } else {
                    throw new CommandUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
                }
                addRunValue(runtime, run, Compare.IN, new Column(key), values);
            }
        }else{
            delimiter(builder, key);
            builder.append("=?");
            addRunValue(runtime, run, Compare.EQUAL, new Column(key), values);
        }

        run.setBuilder(builder);
        runs.add(run);
        return runs;
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
        List<Run> runs = new ArrayList<>();
        TableRun run = new TableRun(runtime, table);
        run.setOriginType(2);
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ");
        name(runtime, builder, table);
        builder.append(" WHERE ");
        List<String> keys = new ArrayList<>();
        if(null != columns && columns.length>0) {
            for(String col:columns) {
                keys.add(col);
            }
        }else{
            if(obj instanceof DataRow) {
                keys = ((DataRow)obj).getPrimaryKeys();
            }else{
                keys.addAll(EntityAdapterProxy.primaryKeys(obj.getClass()).keySet());
            }
        }
        int size = keys.size();
        if(size >0) {
            for(int i=0; i<size; i++) {
                if(i > 0) {
                    builder.append("\nAND ");
                }
                String key = keys.get(i);

                delimiter(builder, key).append(" = ? ");
                Object value = null;
                if(obj instanceof DataRow) {
                    value = ((DataRow)obj).get(key);
                }else{
                    if(EntityAdapterProxy.hasAdapter(obj.getClass())) {
                        value = BeanUtil.getFieldValue(obj,EntityAdapterProxy.field(obj.getClass(), key));
                    }else{
                        value = BeanUtil.getFieldValue(obj, key, true);
                    }
                }
                addRunValue(runtime, run, Compare.EQUAL, new Column(key),value);
            }
        }else{
            throw new CommandUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
        }
        run.setBuilder(builder);
        runs.add(run);
        return runs;
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run,  Boolean placeholder, Boolean unicode) {
        if(null != run) {
            if(run instanceof TableRun) {
                TableRun r = (TableRun) run;
                fillDeleteRunContent(runtime, r);
            }
        }
    }

    /**
     * delete[命令合成-子流程]<br/>
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     */
    protected void fillDeleteRunContent(DataRuntime runtime, TableRun run) {
        AutoPrepare prepare =  (AutoPrepare)run.getPrepare();
        StringBuilder builder = run.getBuilder();
        builder.append("DELETE FROM ");
        name(runtime, builder, run.getTable());
        builder.append(BR);
        if(BasicUtil.isNotEmpty(prepare.getAlias())) {
            // builder.append(" AS ").append(sql.getAlias());
            builder.append("  ").append(prepare.getAlias());
        }
        List<RunPrepare> joins = prepare.getJoins();
        if(null != joins) {
            for (RunPrepare join:joins) {
                Join jn = join.getJoin();
                builder.append(BR_TAB).append(jn.getType().getCode()).append(" ");
                Table joinTable = join.getTable();
                String jionTableAlias = joinTable.getAlias();
                name(runtime, builder, joinTable);
                if(BasicUtil.isNotEmpty(jionTableAlias)) {
                    builder.append("  ").append(jionTableAlias);
                }
                builder.append(" ON ").append(jn.getConditions().getRunText(runtime, false));
            }
        }

        //builder.append("\nWHERE 1=1\n\t");

        /*添加查询条件*/
        // appendConfigStore();
        run.appendCondition(this, true, false, true);
        run.appendGroup(runtime, false, true);
        run.appendOrderStore();
        run.checkValid();
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
     *                                                     database
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
        Catalog catalog = catalog(runtime, random);
        if(null != catalog) {
            return new Database(catalog.getName());
        }
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
     * @throws Exception 异常
     */
    @Override
    public <T extends Database> LinkedHashMap<String, T> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Database query, DataSet set) throws Exception {
        return super.databases(runtime, index, create, previous, query, set);
    }

    @Override
    public <T extends Database> List<T> databases(DataRuntime runtime, int index, boolean create, List<T> previous, Database query, DataSet set) throws Exception {
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
    public Database database(DataRuntime runtime, int index, boolean create, Database meta, DataSet set) throws Exception {
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
        return product;
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
        return version;
    }
    /* *****************************************************************************************************************
     *                                                     catalog
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Catalog> LinkedHashMap<String, T> catalogs(DataRuntime runtime, String random, String name)
     * <T extends Catalog> List<T> catalogs(DataRuntime runtime, String random, boolean greedy, String name)
     * [命令合成]
     * List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name)
     * [结果集封装]<br/>
     * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
     * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs, DataSet set)
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
     * Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog catalog, DataSet set)
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
    public <T extends Catalog> List<T> catalogs(DataRuntime runtime, int index, boolean create, List<T> previous, Catalog query, DataSet set) throws Exception {
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
    public Catalog catalog(DataRuntime runtime, int index, boolean create, Catalog meta, DataSet set) throws Exception {
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
        if(null == meta) {
            Table table = new Table();
            checkSchema(runtime, table);
            meta = table.getCatalog();
        }
        return meta;
    }

    /* *****************************************************************************************************************
     *                                                     schema
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
    public <T extends Schema> LinkedHashMap<String, T> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Schema query, DataSet set) throws Exception {
        return super.schemas(runtime, index, create, previous, query, set);
    }

    @Override
    public <T extends Schema> List<T> schemas(DataRuntime runtime, int index, boolean create, List<T> previous, Schema query, DataSet set) throws Exception {
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
    public Schema schema(DataRuntime runtime, int index, boolean create, Schema meta, DataSet set) throws Exception {
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
        if(null == meta) {
            Table table = new Table();
            checkSchema(runtime, table);
            meta = table.getSchema();
        }
        return meta;
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
            }else if(ks.length == 2) {
                meta.setSchema(ks[0]);
                meta.setName(ks[1]);
            }else{
                throw new RuntimeException("无法实别schema或catalog(子类" + this.getClass().getSimpleName() + "未实现)");
            }
        }
        return meta;
    }
    /* *****************************************************************************************************************
     *                                                     table
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
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table query, DataSet set) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        for(DataRow row:set) {
            T table = null;
            table = init(runtime, index, table, catalog, schema, row);
            table = detail(runtime, index, table, catalog, schema, row);
            previous.put(table.getName().toUpperCase(), table);
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
            T table = null;
            table = init(runtime, index, table, query, row);
            if(null == search(previous, table.getCatalog(), table.getSchema(), table.getName())) {
                previous.add(table);
            }
            detail(runtime, index, table, query, row);
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
    public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, table, ddls, set);
    }

    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index index
     * @param meta 上一步封装结果
     * @param query 查询条件 根据metadata属性
     * @param row 查询结果集
     * @return Table
     * @param <T> Table
     */
    public <T extends Table> T init(DataRuntime runtime, int index, T meta, Table query, DataRow row) {
        return meta;
    }

    /**
     * table[结果集封装]<br/>
     * 根据查询结果封装Table更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 上一步封装结果
     * @param row 查询结果集
     * @return Table
     */
    public <T extends Table> T detail(DataRuntime runtime, int index, T meta, Table query, DataRow row) {
        return meta;
    }
    protected void init(Table table, ResultSet set, Map<String,Integer> keys) {
    }
    protected void init(View view, ResultSet set, Map<String, Integer> keys) {
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
    public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, view, ddls, set);
    }

    /* *****************************************************************************************************************
     *                                                     VertexTable
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends VertexTable> List<T> vertexs(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryVertexsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
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
        return super.vertexs(runtime, random, greedy, query, types, struct, configs);
    }

    public <T extends VertexTable> LinkedHashMap<String, T> vertexs(DataRuntime runtime, String random, VertexTable query, int types, int struct, ConfigStore configs) {
        return super.vertexs(runtime, random, query, types, struct, configs);
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
        return super.buildQueryVertexsRun(runtime, greedy, query, types, configs);
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
    public List<Run> buildQueryVertexsCommentRun(DataRuntime runtime, VertexTable query, int types) throws Exception {
        return super.buildQueryVertexsCommentRun(runtime, query, types);
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
        return super.vertexs(runtime, index, create, previous, query, set);
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
        return super.vertexs(runtime, index, create, previous, query, set);
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
        return super.vertexs(runtime, create, previous, query, types);
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
        return super.vertexs(runtime, create, previous, query, types);
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
        return init(runtime, index, meta, query, row);
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
        return detail(runtime, index, meta, query, row);
    }

    /**
     *
     * vertex[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta 表
     * @param init 是否还原初始状态 如自增状态
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, VertexTable meta, boolean init) {
        return super.ddl(runtime, random, meta, init);
    }

    /**
     * vertex[命令合成]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlRun(DataRuntime runtime, VertexTable meta) throws Exception {
        return super.buildQueryDdlRun(runtime, meta);
    }

    /**
     * vertex[结果集封装]<br/>
     * 查询表DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlRun 返回顺序
     * @param meta 表
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, VertexTable meta, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, meta, ddls, set);
    }

    /* *****************************************************************************************************************
     *                                                     EdgeTable
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends EdgeTable> List<T> edges(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryEdgesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
     * List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edges, Catalog catalog, Schema schema, DataSet set)
     * <T extends EdgeTable> List<T> edges(DataRuntime runtime, int index, boolean create, List<T> edges, Catalog catalog, Schema schema, DataSet set)
     * <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, boolean create, LinkedHashMap<String, T> edges, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends EdgeTable> List<T> edges(DataRuntime runtime, boolean create, List<T> edges, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends EdgeTable> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> edges, Catalog catalog, Schema schema, DataSet set)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, EdgeTable edge, boolean init)
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, EdgeTable edge)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, EdgeTable edge, List<String> ddls, DataSet set)
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
        return super.edges(runtime, random, greedy, query, types, struct, configs);
    }

    public <T extends EdgeTable> LinkedHashMap<String, T> edges(DataRuntime runtime, String random, EdgeTable query, int types, int struct, ConfigStore configs) {
        return super.edges(runtime, random, query, types, struct, configs);
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
        return super.buildQueryEdgesRun(runtime, greedy, query, types, configs);
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
    public List<Run> buildQueryEdgesCommentRun(DataRuntime runtime, EdgeTable query, int types) throws Exception {
        return super.buildQueryEdgesCommentRun(runtime, query, types);
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
        return super.edges(runtime, index, create, previous, query, set);
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
        return super.edges(runtime, index, create, previous, query, set);
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
        return super.edges(runtime, create, previous, query, types);
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
        return super.edges(runtime, create, previous, query, types);
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
        return super.init(runtime, index, meta, query, row);
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
        return super.detail(runtime, index, meta, query, row);
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
        return super.ddl(runtime, random, meta, init);
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
        return super.buildQueryDdlRun(runtime, meta);
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
        return super.ddl(runtime, index, meta, ddls, set);
    }

    /* *****************************************************************************************************************
     *                                                     master table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct, ConfigStore configs)
     * [命令合成]
     * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types, ConfigStore configs)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, DataSet set)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables,Catalog catalog, Schema schema, String pattern, int types)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, MasterTable table)
     * [命令合成]
     * List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set)
     ******************************************************************************************************************/

    /**
     * master table[调用入口]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return List
     * @param <T> MasterTable
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, String random, MasterTable query, int types, int struct, ConfigStore configs) {
        return super.masters(runtime, random, query, types, struct, configs);
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
        return super.buildQueryMasterTablesRun(runtime, greedy, query, types,  configs);
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
    public <T extends MasterTable> LinkedHashMap<String, T> masters(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, MasterTable query, DataSet set) throws Exception {
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
     * @param table MasterTable
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, MasterTable table, boolean init) {
        return super.ddl(runtime, random, table, init);
    }

    /**
     * master table[命令合成]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table MasterTable
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlRun(DataRuntime runtime, MasterTable table) throws Exception {
        return super.buildQueryDdlRun(runtime, table);
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
    public List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, table, ddls, set);
    }
    /* *****************************************************************************************************************
     *                                                     partition table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends PartitionTable> LinkedHashMap<String,T> partitions(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern)
     * [命令合成]
     * List<Run> buildQueryPartitionTablesRun(DataRuntime runtime, boolean greedy,  Catalog catalog, Schema schema, String pattern, int types)
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
    public <T extends PartitionTable> LinkedHashMap<String, T> partitions(DataRuntime runtime, int total, int index, boolean create, LinkedHashMap<String, T> previous, PartitionTable query, DataSet set) throws Exception {
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
    public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, table, ddls, set);
    }
    /* *****************************************************************************************************************
     *                                                     column
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary);
     * <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table);
     * [命令合成]
     * List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception;
     * [结果集封装]<br/>
     * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception;
     * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception;
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
        return super.buildQueryColumnsRun(runtime, metadata, tables, query, configs);
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
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Table table, Column query, DataSet set) throws Exception {
        return super.columns(runtime, index, create, previous, table, query, set);
    }

    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, List<T> previous, Column query, DataSet set) throws Exception {
        return super.columns(runtime, index, create, previous, query, set);
    }

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
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create,  List<T> previous, Collection<? extends Table> tables, Column query, DataSet set) throws Exception {
        return super.columns(runtime, index, create, previous, query, set);
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
        return super.columns(runtime, random, greedy, tables, query, configs);
    }

    /**
     * column [结果集封装-子流程](方法1)<br/>
     * 方法(1)内部遍历
     * @param meta 上一步封装结果
     * @param table 表
     * @param row 查询结果集
     */
    public <T extends Column> T init(DataRuntime runtime, int index, T meta, Table table, DataRow row) {
        if(null == meta) {
            meta = (T)new Column();
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
    public <T extends Column> T detail(DataRuntime runtime, int index, T meta, Column query, DataRow row) {

        return meta;
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
        return super.tags(runtime, index, create, previous, query, set);
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
        return super.tags(runtime, create, tags, query);
    }

    /* *****************************************************************************************************************
     *                                                     primary
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
     * [命令合成]
     * List<Run> buildQueryPrimaryRun(DataRuntime runtime, boolean greedy,  Table table) throws Exception
     * [结构集封装]
     * <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set)
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
     * 根据查询结果集构造PrimaryKey基础属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param query 查询条件 根据metadata属性
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T meta, PrimaryKey query, DataSet set) throws Exception {
        return super.init(runtime, index, meta, query, set);
    }

    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param query 查询条件 根据metadata属性
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T primary, PrimaryKey query, DataSet set) throws Exception {
        return super.detail(runtime, index, primary, query, set);
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
        return super.primary(runtime, query);
    }
    /* *****************************************************************************************************************
     *                                                     foreign
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
    public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, LinkedHashMap<String, T> previous, ForeignKey query, DataSet set) throws Exception {
        return super.foreigns(runtime, index, previous, query, set);
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
        return super.init(runtime, index, meta, query, row);
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
        return super.detail(runtime, index, meta, query, row);
    }
    /* *****************************************************************************************************************
     *                                                     index
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Index> List<T> indexes(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
     * <T extends Index> LinkedHashMap<T, Index> indexes(DataRuntime runtime, String random, Table table, String pattern)
     * [命令合成]
     * List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Table table, String name)
     * [结果集封装]<br/>
     * <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, Table table, List<T> indexes, DataSet set)
     * <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexes, DataSet set)
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

    @Override
    public List<Run> buildQueryIndexesRun(DataRuntime runtime, boolean greedy,  Collection<? extends Table> tables) {
        return super.buildQueryIndexesRun(runtime, greedy, tables);
    }

    /**
     * Index[结果集封装]<br/>
     * Index 属性与结果集对应关系
     * @return MetadataFieldRefer
     */
    @Override
    public MetadataFieldRefer initIndexFieldRefer() {
        MetadataFieldRefer refer = new MetadataFieldRefer(Index.class);
        refer.map(Index.FIELD_NAME, "Index Name");
        refer.map(Index.FIELD_TABLE, "By Tag,By Edge");
        refer.map(Index.FIELD_COLUMN, "Columns");
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
    public <T extends Index> List<T> indexes(DataRuntime runtime, int index, boolean create, List<T> previous, Index query, DataSet set) throws Exception {
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
     * @return indexes
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Index query) throws Exception {
        DataSource datasource = null;
        Connection con = null;
        if(null == previous) {
            previous = new LinkedHashMap<>();
        }
        return previous;
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
        return super.init(runtime, index, meta, query, row);
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
        return super.detail(runtime, index, meta, query, row);
    }

    /* *****************************************************************************************************************
     *                                                     constraint
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
        return super.constraints(runtime, random, greedy, query);
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
        return super.constraints(runtime, random, query);
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
    public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Constraint query, DataSet set) throws Exception {
        return super.constraints(runtime, index, create, previous, query, set);
    }

    /* *****************************************************************************************************************
     *                                                     trigger
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
     * @param table 表
     * @param previous 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> previous, Trigger query, DataSet set) throws Exception {
        return previous;
    }

    /* *****************************************************************************************************************
     *                                                     procedure
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
     * [命令合成]
     * List<Run> buildQueryProceduresRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern) ;
     * [结果集封装]<br/>
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, int index, boolean create, List<T> procedures, DataSet set) throws Exception;
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures, DataSet set) throws Exception;
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
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
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Procedure query, DataSet set) throws Exception {
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
    public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set) {
        return super.ddl(runtime, index, procedure, ddls, set);
    }

    /* *****************************************************************************************************************
     *                                                     function
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Function> List<T> functions(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern);
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern);
     * [命令合成]
     * List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern) ;
     * [结果集封装]<br/>
     * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, Catalog catalog, Schema schema, DataSet set) throws Exception;
     * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions, DataSet set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
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
	public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> previous, Function query, DataSet set) throws Exception {
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
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Function query, DataSet set) throws Exception {
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
	public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set) {
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
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> List<T> sequences(DataRuntime runtime, boolean create, List<T> sequences, DataSet set) throws Exception;
	 * <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, boolean create, LinkedHashMap<String, T> sequences, DataSet set) throws Exception;
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
	public <T extends Sequence> List<T> sequences(DataRuntime runtime, int index, boolean create, List<T> previous, Sequence query, DataSet set) throws Exception {
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
	public <T extends Sequence> LinkedHashMap<String, T> sequences(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> previous, Sequence query, DataSet set) throws Exception {
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
	 * @param meta 上一步查询结果
	 * @param set 查询结果集
	 * @return List
	 */
	@Override
	public List<String> ddl(DataRuntime runtime, int index, Sequence meta, List<String> ddls, DataSet set) {
		return super.ddl(runtime, index, meta, ddls, set);
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
		if(null == run) {
			return false;
		}
		boolean result = false;
		String sql = run.getFinalUpdate();
        run.metadata(meta);
		meta.addDdl(sql);
		if(BasicUtil.isNotEmpty(sql)) {
			if(meta.execute()) {
				try {
					update(runtime, random, (Table) null, null, null, run);
				}finally {
					CacheProxy.clear();
				}
			}
			result = true;
		}
		return result;
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
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table meta)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table meta)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta)
	 * time runtime, StringBuilder builder, Table meta)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta)
	 * StringBuilder partitionBy(DataRuntime runtime, StringBuilder builder, Table meta)
	 * StringBuilder partitionOf(DataRuntime runtime, StringBuilder builder, Table meta)
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
	 * @throws Exception
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table meta) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE ").append(keyword(meta)).append(" ");
		checkTableExists(runtime, builder, false);
		name(runtime, builder, meta);
		//分区表
		partitionOf(runtime, builder, meta);
		partitionFor(runtime, builder, meta);
		body(runtime, builder, meta);
		//分区依据列(主表执行) PARTITION BY RANGE (code);
		partitionBy(runtime, builder, meta);
		//继承表CREATE TABLE simple.public.tab_1c1() INHERITS(simple.public.tab_parent)
		inherit(runtime, builder, meta);
		//引擎
		engine(runtime, builder, meta);
		//编码方式
		charset(runtime, builder, meta);
		//keys type
		keys(runtime, builder, meta);
		//分桶方式
		distribution(runtime, builder, meta);
		//物化视图
		materialize(runtime, builder, meta);
		//扩展属性
		property(runtime, builder, meta);
		//备注
		comment(runtime, builder, meta);

		//创建表后追加
		runs.addAll(buildAppendCommentRun(runtime, meta));
		runs.addAll(buildAppendColumnCommentRun(runtime, meta));
		runs.addAll(buildAppendPrimaryRun(runtime, meta));
		runs.addAll(buildAppendIndexRun(runtime, meta));
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP ").append(keyword(meta)).append(" ");
		checkTableExists(runtime, builder, true);
		name(runtime, builder, meta);
		return runs;
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
		builder.append(" IF ");
		if(!exists) {
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
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
		PrimaryKey primary = meta.getPrimaryKey();
		LinkedHashMap<String, Column> pks = null;
		if(null != primary) {
			pks = primary.getColumns();
		}else{
			pks = meta.primarys();
		}
		if(!pks.isEmpty() && pks.size() >1) {//单列主键时在列名上设置
			builder.append(",PRIMARY KEY (");
			boolean first = true;
			Column.sort(primary.getPositions(), pks);
			for(Column pk:pks.values()) {
				if(!first) {
					builder.append(", ");
				}
				delimiter(builder, pk.getName());
				String order = pk.getOrder();
				if(BasicUtil.isNotEmpty(order)) {
					builder.append(" ").append(order);
				}
				first = false;
			}
			builder.append(")");
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
		String engine = meta.getEngine();
		if(BasicUtil.isNotEmpty(engine)) {
			builder.append("\nENGINE = ").append(engine);
		}
		String params = meta.getEngineParameters();
		if(BasicUtil.isNotEmpty(params)) {
			builder.append(" ").append(params);
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
		LinkedHashMap<String, Column> columns = meta.getColumns();
		if(null == columns || columns.isEmpty()) {
			if(BasicUtil.isEmpty(meta.getInherit())) {
				//继承表没有列也需要() CREATE TABLE IF NOT EXISTS simple.public.tab_c2() INHERITS(simple.public.tab_parent)
				//分区表不需要 CREATE TABLE IF NOT EXISTS simple.public.LOG2 PARTITION OF simple.public.log_master FOR VALUES FROM (100) TO (199)
				return builder;
			}
		}
		builder.append("(");
		columns(runtime, builder, meta);
		indexes(runtime, builder, meta);
		builder.append(")");
		return builder;
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
		LinkedHashMap columMap = meta.getColumns();
		Collection<Column> columns = null;
		PrimaryKey primary = meta.getPrimaryKey();
		LinkedHashMap<String, Column> pks = null;
		if(null != primary) {
			pks = primary.getColumns();
		}else{
			pks = meta.primarys();
			primary = new PrimaryKey();
			primary.setTable(meta);
			for (Column col:pks.values()) {
				primary.addColumn(col);
			}
			meta.setPrimaryKey(primary);
		}
		if(null == pks) {
			pks = new LinkedHashMap<>();
		}
		if(null != columMap) {
			columns = columMap.values();
			if(null != columns && !columns.isEmpty()) {
				//builder.append("(");
				int idx = 0;
				for(Column column:columns) {
					TypeMetadata metadata = column.getTypeMetadata();
					if(null == metadata) {
						metadata = typeMetadata(runtime, column);
						column.setTypeMetadata(metadata);
					}
					if(pks.containsKey(column.getName().toUpperCase())) {
						column.setNullable(false);
					}
					builder.append("\n\t");
					if(idx > 0) {
						builder.append(", ");
					}
					column.setAction(ACTION.DDL.COLUMN_ADD);
					delimiter(builder, column.getName(), false).append(" ");
					define(runtime, builder, column, ACTION.DDL.TABLE_CREATE);
					idx ++;
				}
				if(!pks.isEmpty()) {
					builder.append("\n\t");
					primary(runtime, builder, meta);
				}
				//builder.append("\n)");
			}
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
		if(null != meta.getProperty()) {
			builder.append(" ").append(meta.getProperty());
		}
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
		// PARTITION BY RANGE (code); #根据code值分区
		Table.Partition partition = meta.getPartition();
		if(null == partition) {
			return builder;
		}
		//只有主表需要执行
		if(null != meta.getMaster()) {
			return builder;
		}
		Table.Partition.TYPE  type = partition.getType();
		if(null == type) {
			return builder;
		}
		builder.append(" PARTITION BY ").append(type.name()).append("(");
		LinkedHashMap<String, Column> columns = partition.getColumns();
		if(null != columns) {
			delimiter(builder, Column.names(columns));
		}
		builder.append(")");
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
		//CREATE TABLE partition_name2 PARTITION OF main_table_name FOR VALUES FROM (100) TO (199);
		//CREATE TABLE emp_0 PARTITION OF emp FOR VALUES WITH (MODULUS 3,REMAINDER 0);
		//CREATE TABLE hr_user_1 PARTITION OF hr_user FOR VALUES IN ('HR');
		Table master = meta.getMaster();
		if(null == master) {
			return builder;
		}
		builder.append(" PARTITION OF ");
		if(null == master) {
			throw new CommandException("未提供 Master Table");
		}
		name(runtime, builder, master);
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
		Table master = meta.getMaster();
		if(null == master) {
			//只有子表才需要执行
			return builder;
		}
		Table.Partition partition = meta.getPartition();
		Table.Partition.TYPE type = null;
		if(null != partition) {
			type = partition.getType();
		}
		if(null == type && null != master.getPartition()) {
			type = master.getPartition().getType();
		}
		if(null == type) {
			return builder;
		}
		builder.append(" FOR VALUES");
		if(type == Table.Partition.TYPE.LIST) {
			List<Object> list = partition.getValues();
			if(null == list) {
				throw new CommandException("未提供分区表枚举值(Partition.list)");
			}
			builder.append(" IN(");
			boolean first = true;
			for(Object item:list) {
				if(!first) {
					builder.append(", ");
				}
				first = false;
				if(item instanceof Number) {
					builder.append(item);
				}else{
					builder.append("'").append(item).append("'");
				}
			}
			builder.append(")");
		}else if(type == Table.Partition.TYPE.RANGE) {
			Object from = partition.getMin();
			Object to = partition.getMax();
			if(BasicUtil.isEmpty(from) || BasicUtil.isEmpty(to)) {
				throw new CommandException("未提供分区表范围值(Partition.from/to)");
			}
			builder.append(" FROM (");
			if(from instanceof Number) {
				builder.append(from);
			}else{
				builder.append("'").append(from).append("'");
			}
			builder.append(")");

			builder.append(" TO (");
			if(to instanceof Number) {
				builder.append(to);
			}else{
				builder.append("'").append(to).append("'");
			}
			builder.append(")");
		}else if(type == Table.Partition.TYPE.HASH) {
			int modulus = partition.getModulus();
			if(modulus == 0) {
				throw new CommandException("未提供分区表MODULUS");
			}
			builder.append(" WITH(MODULUS ").append(modulus).append(",REMAINDER ").append(partition.getRemainder()).append(")");
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
		//继承表CREATE TABLE simple.public.tab_1c1() INHERITS(simple.public.tab_parent)
		if(BasicUtil.isNotEmpty(meta.getInherit())) {
			LinkedHashMap<String, Column> columns = meta.getColumns();
			if(null == columns || columns.isEmpty()) {
				// TODO body中已实现
				//继承关系中 子表如果没有新添加的列 需要空()
				//builder.append("()");
			}
			builder.append(" INHERITS(");
			name(runtime, builder, meta.getInherit());
			builder.append(")");
		}
		return builder;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * [调用入口]
	 * boolean create(DataRuntime runtime, View meta)
	 * boolean alter(DataRuntime runtime, View meta)
	 * boolean drop(DataRuntime runtime, View meta)
	 * boolean rename(DataRuntime runtime, View origin, String name)
	 * [命令合成]
	 * List<Run> buildCreateRun(DataRuntime runtime, View meta)
	 * List<Run> buildAlterRun(DataRuntime runtime, View meta)
	 * List<Run> buildRenameRun(DataRuntime runtime, View meta)
	 * List<Run> buildDropRun(DataRuntime runtime, View meta)
	 * [命令合成-子流程]
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, View meta)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, View meta)
	 * StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta)
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		buildCreateRunHead(runtime, builder, meta);
		buildCreateRunOption(runtime, builder, meta);
		builder.append(" AS \n").append(meta.getDefinition());
		runs.addAll(buildAppendCommentRun(runtime, meta));
		return runs;
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
		if (null == builder) {
			builder = new StringBuilder();
		}
		builder.append("CREATE VIEW ");
		name(runtime, builder, meta);
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER VIEW ");
		name(runtime,builder, meta);
		builder.append(" AS \n").append(meta.getDefinition());
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP ").append(meta.keyword()).append(" ");
		checkViewExists(runtime,builder, true);
		name(runtime,builder, meta);
		return runs;
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
		builder.append(" IF ");
		if(!exists) {
			builder.append("NOT ");
		}
		builder.append("EXISTS ");
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
	 * 添加列<br/>
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(!slice(slice)) {
			Table table = meta.getTable(true);
			builder.append("ALTER ").append(keyword(table)).append(" ");
			name(runtime, builder, table);
		}else{
			run.slice(slice);
		}
		// Column update = column.getUpdate();
		// if(null == update) {
		// 添加列
		//builder.append(" ADD ").append(column.getKeyword()).append(" ");
		addColumnGuide(runtime, builder, meta);
		delimiter(builder, meta.getName()).append(" ");
		define(runtime, builder, meta, ACTION.DDL.COLUMN_ADD);
		// }
		runs.addAll(buildAppendCommentRun(runtime, meta, slice));
		return runs;
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列<br/>
	 * 有可能生成多条SQL
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Column update = meta.getUpdate();
		if(null != update) {
			if(null == update.getTable(false)) {
				update.setTable(meta.getTable(false));
			}
			// 修改列名
			String name = meta.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith(ConfigTable.ALTER_COLUMN_TYPE_SUFFIX)) {
				runs.addAll(buildRenameRun(runtime, meta, slice));
			}
			// 修改数据类型
			String type = type(runtime, null, meta).toString();
			String utype = type(runtime, null, update).toString();
			boolean exe = false;
			if(!BasicUtil.equalsIgnoreCase(type, utype)) {
				List<Run> list = buildChangeTypeRun(runtime, meta, slice);
				if(null != list) {
					runs.addAll(list);
					exe = true;
				}
			}else{
				//数据类型没变但长度变了
				if(meta.getPrecision() != update.getPrecision() || meta.getScale() != update.getScale()) {
					List<Run> list = buildChangeTypeRun(runtime, meta, slice);
					if(null != list) {
						runs.addAll(list);
						exe = true;
					}
				}
			}
			// 修改默认值
			Object def = meta.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)) {
				List<Run> defs = buildChangeDefaultRun(runtime, meta, slice);
				if(null != defs) {
					runs.addAll(defs);
				}
			}
			// 修改非空限制
			Boolean nullable = meta.getNullable();
            Boolean unullable = update.getNullable();
			if(nullable != unullable) {
				List<Run> nulls = buildChangeNullableRun(runtime, meta, slice);
				if(null != nulls) {
					runs.addAll(nulls);
				}
			}
			// 修改备注
			String comment = meta.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)) {
				List<Run> cmts = buildChangeCommentRun(runtime, meta, slice);
				if(null != cmts) {
					runs.addAll(cmts);
				}
			}
		}
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(meta instanceof Tag) {
			Tag tag = (Tag)meta;
			return buildDropRun(runtime, tag, slice);
		}
		if(!slice(slice)) {
			Table table = meta.getTable(true);
			builder.append("ALTER ").append(keyword(table)).append(" ");
			name(runtime, builder, table);
		}else{
			run.slice(slice);
		}
		dropColumnGuide(runtime, builder, meta);
		delimiter(builder, meta.getName());
		return runs;
	}

	/**
	 * column[命令合成]<br/>
	 * 修改列名<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column meta, boolean slice) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		builder.append(" RENAME ").append(meta.keyword()).append(" ");
		delimiter(builder, meta.getName());
		builder.append(" ");
		name(runtime, builder, meta.getUpdate());
		meta.setName(meta.getUpdate().getName());
		return runs;
	}

	/**
	 * column[命令合成-子流程]<br/>
	 * 修改数据类型<br/>
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param meta 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta, boolean  slice) throws Exception {
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
		builder.append(" ADD ").append(meta.keyword()).append(" ");
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
		builder.append(" DROP ").append(meta.keyword()).append(" ");
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
		if(null == builder) {
			builder = new StringBuilder();
		}
		int ignoreLength = -1;
		int ignorePrecision = -1;
		int ignoreScale = -1;
        int maxLenght = -1;
        int maxPrecision = -1;
        int maxScale = -1;
		String typeName = meta.getTypeName();
		TypeMetadata type = typeMetadata(runtime, meta);
		if(null != type) {
			if(!type.support()) {
				throw new RuntimeException("数据类型不支持:"+meta.getName() + " " + typeName);
			}
			typeName = type.getName();
		}

		TypeMetadata.Refer refer = dataTypeMetadataRefer(runtime, type);
		ignoreLength = refer.ignoreLength();
		ignorePrecision = refer.ignorePrecision();
		ignoreScale = refer.ignoreScale();
        maxLenght = refer.maxLength();
        maxPrecision = refer.maxPrecision();
        maxScale = refer.maxScale();
		return type(runtime, builder, meta, typeName, ignoreLength, ignorePrecision, ignoreScale, maxLenght, maxPrecision, maxScale);
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
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
		return super.type(runtime, builder, meta, type, ignoreLength, ignorePrecision, ignoreScale, maxLength, maxPrecision, maxScale);
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		// Tag update = tag.getUpdate();
		// if(null == update) {
		// 添加标签
		builder.append(" ADD TAG ");
		delimiter(builder, meta.getName()).append(" ");
		define(runtime, builder, meta, ACTION.DDL.COLUMN_ADD);
		// }
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Tag update = meta.getUpdate();
		if(null != update) {
			// 修改标签名
			String name = meta.getName();
			String uname = update.getName();
			if(!BasicUtil.equalsIgnoreCase(name, uname) && !uname.endsWith(ConfigTable.ALTER_COLUMN_TYPE_SUFFIX)) {
				runs.addAll(buildRenameRun(runtime, meta, slice));
			}
			meta.setName(uname);
			// 修改数据类型
			String type = type(runtime, null, meta).toString();
			String utype = type(runtime, null, update).toString();
			if(!BasicUtil.equalsIgnoreCase(type, utype)) {
				List<Run> list = buildChangeTypeRun(runtime, meta);
				if(null != list) {
					runs.addAll(list);
				}
			}else{
				//数据类型没变但长度变了
				if(meta.getPrecision() != update.getPrecision() || meta.getScale() != update.getScale()) {
					List<Run> list = buildChangeTypeRun(runtime, meta);
					if(null != list) {
						runs.addAll(list);
					}
				}
			}
			// 修改默认值
			Object def = meta.getDefaultValue();
			Object udef = update.getDefaultValue();
			if(!BasicUtil.equalsIgnoreCase(def, udef)) {
				runs.addAll(buildChangeDefaultRun(runtime, meta, slice));
			}
			// 修改非空限制
			Boolean nullable = meta.getNullable();
            Boolean unullable = update.getNullable();
			if(nullable != unullable) {
				runs.addAll(buildChangeNullableRun(runtime, meta, slice));
			}
			// 修改备注
			String comment = meta.getComment();
			String ucomment = update.getComment();
			if(!BasicUtil.equalsIgnoreCase(comment, ucomment)) {
				runs.addAll(buildChangeCommentRun(runtime, meta, slice));
			}
		}
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		builder.append(" DROP ").append(meta.keyword()).append(" ");
		delimiter(builder, meta.getName());
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		builder.append("ALTER ").append(keyword(table)).append(" ");
		name(runtime, builder, table);
		builder.append(" RENAME ").append(meta.keyword()).append(" ");
		delimiter(builder, meta.getName());
		builder.append(" ");
		name(runtime, builder, meta.getUpdate());
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		LinkedHashMap<String,Column> columns = meta.getColumns();
		if(null != columns && !columns.isEmpty()) {
			builder.append("ALTER TABLE ");
			name(runtime, builder, meta.getTable(true));
			builder.append(" ADD");
			if(BasicUtil.isNotEmpty(meta.getName())) {
				builder.append(" CONSTRAINT ").append(meta.getName());
			}
			builder.append(" FOREIGN KEY (");
			delimiter(builder, Column.names(columns));
			builder.append(")");
			builder.append(" REFERENCES ").append(meta.getReference().getName()).append("(");
			boolean first = true;
			for(Column column:columns.values()) {
				if(!first) {
					builder.append(", ");
				}
				name(runtime, builder, column.getReference());
				first = false;
			}
			builder.append(")");

		}
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, meta.getTable(true));
		builder.append(" DROP FOREIGN KEY ").append(meta.getName());
		return runs;
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
		String name = meta.getName();
		if(BasicUtil.isEmpty(name)) {
			name = "index_"+BasicUtil.getRandomString(10);
		}
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE");
		if(meta.isUnique()) {
			builder.append(" UNIQUE");
		}else if(meta.isFulltext()) {
			builder.append(" FULLTEXT");
		}else if(meta.isSpatial()) {
			builder.append(" SPATIAL");
		}
		builder.append(" ").append(keyword(meta)).append(" ");
		checkIndexExists(runtime, builder, false);
		name(runtime, builder, meta);
		builder.append(" ON ");
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append("(");
		int qty = 0;
		LinkedHashMap<String, Column> columns = meta.getColumns();
		//排序
		Column.sort(meta.getPositions(), columns);
		for(Column column:columns.values()) {
			if(qty>0) {
				builder.append(", ");
			}
			delimiter(builder, column.getName(), false);
			Order.TYPE order = meta.getOrder(column.getName());
			if(BasicUtil.isNotEmpty(order)) {
				builder.append(" ").append(order);
			}
			qty ++;
		}
		builder.append(")");
		type(runtime, builder, meta);
		property(runtime, builder, meta);
		comment(runtime, builder, meta);
		return runs;
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
		List<Run> runs = buildDropRun(runtime, meta);
		Index update = (Index)meta.getUpdate();
		if(null != update) {
			runs.addAll(buildAddRun(runtime, update));
		}else {
			runs.addAll(buildAddRun(runtime, meta));
		}
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		if(meta.isPrimary()) {
			builder.append("ALTER TABLE ");
			name(runtime, builder, table);
			builder.append(" DROP CONSTRAINT ");
            name(runtime, builder, meta);
		}else {
			builder.append("DROP INDEX ");
            name(runtime, builder, meta);
			if (BasicUtil.isNotEmpty(table)) {
				builder.append(" ON ");
				name(runtime, builder, table);
			}
		}
		return runs;
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
	 * index[命令合成-子流程]<br/>
	 * 创建或删除表索引前  检测表是否存在
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
		String name = meta.getName();
		if(BasicUtil.isEmpty(name)) {
			name = "constraint_"+BasicUtil.getRandomString(10);
		}
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append(" ADD CONSTRAINT ").append(name);
		if(meta.isUnique()) {
			builder.append(" UNIQUE");
		}
		builder.append("(");
		boolean first = true;
		Collection<Column> cols = meta.getColumns().values();
		for(Column column:cols) {
			if(!first) {
				builder.append(", ");
			}
			first = false;
			delimiter(builder, column.getName(), false);
			String order = column.getOrder();
			if(BasicUtil.isNotEmpty(order)) {
				builder.append(" ").append(order);
			}
		}
		builder.append(")");
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		Table table = meta.getTable(true);
		name(runtime, builder, table);
		builder.append(" DROP CONSTRAINT ").append(meta.getName());
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER CONSTRAINT ");
		name(runtime, builder, meta);
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());

		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("CREATE TRIGGER ").append(meta.getName());
		builder.append(" ").append(meta.getTime().sql()).append(" ");
		List<Trigger.EVENT> events = meta.getEvents();
		boolean first = true;
		for(Trigger.EVENT event:events) {
			if(!first) {
				builder.append(" OR ");
			}
			builder.append(event);
			first = false;
		}
		builder.append(" ON ");
		name(runtime, builder, meta.getTable(true));
		each(runtime, builder, meta);

		builder.append("\n").append(meta.getDefinition());

		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP TRIGGER ");
		name(runtime, builder, meta);
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = meta.getTable(true);
		Catalog catalog = table.getCatalog();
		Schema schema = table.getSchema();
		builder.append("ALTER TRIGGER ");
		if(!empty(catalog)) {
			name(runtime, builder, catalog).append(".");
		}
		if(!empty(schema)) {
			name(runtime, builder, schema).append(".");
		}
		delimiter(builder, meta.getName());
		builder.append(" RENAME TO ");
		name(runtime, builder, meta.getUpdate());

		return runs;
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
		if(meta.isEach()) {
			builder.append(" FOR EACH ROW ");
		}else{
			builder.append(" FOR EACH STATEMENT ");
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP FUNCTION ");
		name(runtime, builder, meta);
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER FUNCTION ");
		name(runtime, builder, meta);
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("DROP SEQUENCE ");
		name(runtime, builder, meta);
		return runs;
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
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun(runtime);
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER SEQUENCE ");
		name(runtime, builder, meta);
		builder.append(" RENAME TO ");
		delimiter(builder, meta.getUpdate().getName());
		return runs;
	}

	protected DataSet select(DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String sql, List<Object> values) {

		return new DataSet();
	}

	/**
	 * 生成insert sql的value部分,每个Entity(每行数据)调用一次
	 * (1,2,3)
	 * (?,?,?)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run           run
	 * @param obj           Entity或DataRow
	 * @param placeholder   是否使用占位符(批量操作时不要超出数量)
	 * @param scope         是否带(), 拼接在select后时不需要
	 * @param alias         是否添加别名
	 * @param columns          需要插入的列
	 * @param child          是否在子查询中，子查询中不要用序列
	 */
	protected String insertValue(DataRuntime runtime, Run run, Object obj, boolean child, Boolean placeholder, boolean alias, boolean scope, LinkedHashMap<String,Column> columns) {
		int batch = run.getBatch();
		StringBuilder builder = new StringBuilder();
		if(scope && batch<=1) {
			builder.append("(");
		}
		int type = 1;
		if(obj instanceof DataRow) {
            type = 1;
		}
		run.setOriginType(type);
		boolean first = true;
		for(Column column:columns.values()) {
			boolean place = placeholder;
			boolean src = false; //直接拼接 如${now()} ${序列}
			String key = column.getName();
			if (!first && batch<=1) {
				builder.append(", ");
			}
			first = false;
			Object value = null;
			if(obj instanceof DataRow) {
				value = BeanUtil.getFieldValue(obj, key, true);
			}else if(obj instanceof Map) {
				value = ((Map)obj).get(key);
			}else{
				value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
			}
			if(value != null) {
				if(value instanceof SQL_BUILD_IN_VALUE) {
					place = false;
				}else if(value instanceof String) {
					String str = (String)value;
					//if(str.startsWith("${") && str.endsWith("}")) {
					if(BasicUtil.checkEl(str)) {
						src = true;
						place = false;
						value = str.substring(2, str.length()-1);
						if (child && str.toUpperCase().contains(".NEXTVAL")) {
							value = null;
						}
					}else if("NULL".equals(str)) {
						value = null;
					}
				}
			}
			if(src) {
				builder.append(value);
			}else {
				if (batch <= 1) {
					if (place) {
						builder.append("?");
						addRunValue(runtime, run, Compare.EQUAL, column, value);
					} else {
						//value(runtime, builder, obj, key);
						builder.append(write(runtime, column, value, false, false));
					}
				} else {
					addRunValue(runtime, run, Compare.EQUAL, column, value);
				}
			}

			if(alias && batch<=1) {
				builder.append(" AS ");
				delimiter(builder, key);
			}
		}
		if(scope && batch<=1) {
			builder.append(")");
		}
		return builder.toString();
	}

	/**
	 * 拼接字符串
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param args args
	 * @return String
	 */
	@Override
	public String concat(DataRuntime runtime, String... args) {
		return null;
	}

}
