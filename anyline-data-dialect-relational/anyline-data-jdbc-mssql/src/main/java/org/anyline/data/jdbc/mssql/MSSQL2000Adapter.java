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

package org.anyline.data.jdbc.mssql;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.metadata.refer.MetadataFieldRefer;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 2000(8.0)及以下版本
 */
@Component("anyline.data.jdbc.adapter.mssql2000")
public class MSSQL2000Adapter extends MSSQLAdapter implements JDBCAdapter {

    public String version() {return "2000";}

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
        boolean chk = super.match(runtime, feature, adapterKey, compensate);
        if(chk) {
            String version = runtime.getVersion();
            if (null != version && version.contains(".")) {
                version = version.split("\\.")[0];
                double v = BasicUtil.parseDouble(version, 0d);
                if(ConfigTable.IS_LOG_ADAPTER_MATCH) {
                    log.debug("[adapter match][SQL Server版本检测][result:{}][runtime version:{}][adapter:{}]", false, version, this.getClass());
                }
                if (v < 9.0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查询SQL
     * Run 反转调用
     * @param run  run
     * @return String
     */
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run) {
        StringBuilder builder = new StringBuilder();
        String cols = run.getQueryColumn();
        PageNavi navi = run.getPageNavi();
        String sql = run.getBaseQuery();
        OrderStore orders = run.getOrderStore();
        long first = 0;
        long last = 0;
        String order = "";
        if(null != orders) {
            order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
        }
        if(null != navi) {
            first = navi.getFirstRow();
            last = navi.getLastRow();
        }
        if(first == 0 && null != navi) {
            // top
            builder.append("SELECT TOP ").append(last+1).append(" "+cols+" FROM(\n");
            builder.append(sql).append("\n) AS _TAB_O \n");
            builder.append(order);
            return builder.toString();
        }
        if(null == navi) {
            builder.append(sql).append("\n").append(order);
        }else{
            // 分页
            long rows = navi.getPageRows();
            if(rows * navi.getCurPage() > navi.getTotalRow()) {
                // 最后一页不足10条
                rows = navi.getTotalRow() % navi.getPageRows();
            }
            String asc = order;
            String desc = order.replace("ASC","<A_ORDER>");
            desc = desc.replace("DESC","ASC");
            desc = desc.replace("<A_ORDER>","DESC");
            builder.append("SELECT "+cols+" FROM (\n ");
            builder.append("SELECT TOP ").append(rows).append(" * FROM (\n");
            builder.append("SELECT TOP ").append(navi.getPageRows()*navi.getCurPage()).append(" * ");
            builder.append(" FROM (" + sql + ") AS T0 ").append(asc).append("\n");
            builder.append(") AS T1 ").append(desc).append("\n");
            builder.append(") AS T2").append(asc);
        }
        return builder.toString();
    }

    /**
     * 根据DataSet创建批量INSERT RunPrepare
     * 2000版本单独处理  insert into tab(nm) select 1 union all select 2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param set 集合
     * @param columns 需插入的列
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns) {
        //2000及以下
        StringBuilder builder = run.getBuilder();
        if(null == builder) {
            builder = new StringBuilder();
            run.setBuilder(builder);
        }

        LinkedHashMap<String, Column> pks = null;
        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        if(null != generator) {
            pks = set.getRow(0).getPrimaryColumns();
            columns.putAll(pks);
        }

        builder.append("INSERT INTO ");
        name(runtime, builder, dest);
        builder.append("(");

        boolean start = true;
        for(Column column:columns.values()) {
            if(!start) {
                builder.append(",");
            }
            start = false;
            String key = column.getName();
            delimiter(builder, key);
        }
        builder.append(")");
        boolean el = ConfigStore.IS_AUTO_CHECK_EL_VALUE(configs);
        int dataSize = set.size();
        for(int i=0; i<dataSize; i++) {
            DataRow row = set.getRow(i);
            if(null == row) {
                continue;
            }
            if(row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())) {
                if(null != generator) {
                    generator.create(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
                }
                //createPrimaryValue(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
            }
            builder.append("\n SELECT ");
            builder.append(insertValue(runtime, run, row, i==0, true, true, false, false, el, columns));
            if(i<dataSize-1) {
                //多行数据之间的分隔符
                builder.append("\n UNION ALL ");
            }
        }
    }

    /**
     * 根据Collection创建批量INSERT RunPrepare
     * 2000版本单独处理  insert into tab(nm) select 1 union all select 2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 集合
     * @param columns 需插入的列
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, LinkedHashMap<String, Column> columns) {
        StringBuilder builder = run.getBuilder();
        if(null == builder) {
            builder = new StringBuilder();
            run.setBuilder(builder);
        }
        if(list instanceof DataSet) {
            DataSet set = (DataSet) list;
            this.fillInsertContent(runtime, run, dest, set, columns);
            return;
        }

        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.getName());
        LinkedHashMap<String, Column> pks = null;
        if(null != generator) {
            Object entity = list.iterator().next();
            pks = EntityAdapterProxy.primaryKeys(entity.getClass());
            columns.putAll(pks);
        }

        builder.append("INSERT INTO ");
        name(runtime, builder, dest);
        builder.append("(");
        boolean start = true;
        for(Column column:columns.values()) {
            if(!start) {
                builder.append(",");
            }
            start = false;
            String key = column.getName();
            delimiter(builder, key);
        }
        builder.append(")\n ");
        int dataSize = list.size();
        int idx = 0;
        boolean el = ConfigTable.IS_AUTO_CHECK_EL_VALUE;
        for(Object obj:list) {
            builder.append("\n SELECT ");
           /* if(obj instanceof DataRow) {
                DataRow row = (DataRow)obj;
                if (row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())) {
                    createPrimaryValue(row, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
                }
                insertValue(template, run, row, true, false, false, keys);
            }else{*/
                boolean create = EntityAdapterProxy.createPrimaryValue(obj, pks);
                if(!create && null != generator) {
                    generator.create(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
                    //createPrimaryValue(obj, type(), dest.getName().replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
                }
            builder.append(insertValue(runtime, run, obj, idx==0,true, true, false, false, el, columns));
           // }
            if(idx<dataSize-1) {
                //多行数据之间的分隔符
                builder.append("\n UNION ALL ");

            }
            idx ++;
        }
    }

    /**
     * 查询表
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考Metadata.TYPE 多个类型相加算出总和
     * @return String
     */
    @Override
    public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Table query, int types, ConfigStore configs) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime, configs);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT M.*, SCHEMA_NAME(M.SCHEMA_ID) AS TABLE_SCHEMA, F.VALUE AS TABLE_COMMENT FROM SYS.TABLES AS M \n");
        builder.append("LEFT JOIN SYS.EXTENDED_PROPERTIES AS F ON M.OBJECT_ID = F.MAJOR_ID AND F.MINOR_ID=0 \n");
        configs.and("SCHEMA_NAME(M.SCHEMA_ID)", query.getSchemaName());
        configs.like("M.NAM", query.getName());
        //SYS.TABLES 中没有视图不需要过滤视图
        return runs;
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
     * 创建表完成后追加表备注, 创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
     * @param table 表
     * @return sql
     * @throws Exception 异常
     */
    public List<Run> buildAppendCommentRun(DataRuntime runtime, Table table) throws Exception {
        List<Run> runs = new ArrayList<>();
        String comment = table.getComment();
        if(BasicUtil.isNotEmpty(comment)) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            builder.append("EXEC sp_addextendedproperty ");
            builder.append("'MS_Description', ");
            builder.append("N'").append(comment).append("', ");
            builder.append("'USER', ");
            builder.append("'").append(table.getSchemaName()).append("', ");
            builder.append("'TABLE', ");
            builder.append("'").append(table.getName()).append("'");
        }

        return runs;
    }

    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception {
        List<Run> runs = new ArrayList<>();
        String comment = table.getComment();
        if(BasicUtil.isNotEmpty(comment)) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            builder.append("EXEC sp_updateextendedproperty ");
            builder.append("'MS_Description', ");
            builder.append("N'").append(comment).append("', ");
            builder.append("'USER', ");
            builder.append("'").append(table.getSchema()).append("', ");
            builder.append("'TABLE', ");
            builder.append("'").append(table.getName()).append("'");
        }

        return runs;
    }

    /**
     * 创建表完成后追加表备注, 创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
     * @param column 列
     * @return sql
     * @throws Exception 异常
     */
    public List<Run> buildAppendCommentRun(DataRuntime runtime, Column column) throws Exception {
        List<Run> runs = new ArrayList<>();
        String comment = column.getComment();
        if(BasicUtil.isNotEmpty(comment)) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            Schema schema = column.getSchema();
            if(BasicUtil.isEmpty(schema)) {
                schema = column.getTable(true).getSchema();
            }
            builder.append("EXEC sp_addextendedproperty ");
            builder.append("'MS_Description', ");
            builder.append("N'").append(comment).append("', ");
            builder.append("'USER', ");
            builder.append("'").append(schema.getName()).append("', ");
            builder.append("'TABLE', ");
            builder.append("'").append(column.getTableName(true)).append("', ");
            builder.append("'COLUMN', ");
            builder.append("'").append(column.getName()).append("'");
        }

        return runs;
    }

    /**
     * 修改备注
     *  -- 字段加注释
     * EXEC sys.sp_addextendedproperty @name=N'MS_Description'
     *, @value=N'注释内容'
     *, @level0type=N'SCHEMA'
     *, @level0name=N'dbo'
     *, @level1type=N'TABLE'
     *, @level1name=N'表名'
     *, @level2type=N'COLUMN'
     *, @level2name=N'字段名'
     *
     * @param column 列
     * @return String
     */
    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Column column, boolean slice) throws Exception {
        List<Run> runs = new ArrayList<>();
        String comment = null;
        if(null != column.getUpdate()) {
            comment = column.getUpdate().getComment();
        }else {
            comment = column.getComment();
        }
        if(BasicUtil.isNotEmpty(comment)) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            Schema schema = column.getSchema();
            if(BasicUtil.isEmpty(schema)) {
                schema = column.getTable(true).getSchema();
            }
            builder.append("EXEC sp_updateextendedproperty ");
            builder.append("'MS_Description', ");
            builder.append("N'").append(comment).append("', ");
            builder.append("'USER', ");
            builder.append("'").append(schema.getName()).append("', ");
            builder.append("'TABLE', ");
            builder.append("'").append(column.getTableName(true)).append("', ");
            builder.append("'COLUMN', ");
            builder.append("'").append(column.getName()).append("'");
        }

        return runs;
    }

}
