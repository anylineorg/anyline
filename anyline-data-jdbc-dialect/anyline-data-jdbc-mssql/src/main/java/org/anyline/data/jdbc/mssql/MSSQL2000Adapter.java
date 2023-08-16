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

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 2000(8.0)及以下版本
 */
@Repository("anyline.data.jdbc.adapter.mssql.2000")
public class MSSQL2000Adapter extends MSSQLAdapter implements JDBCAdapter, InitializingBean {

    public String version(){return "2000";}
    /**
     * 查询SQL
     * Run 反转调用
     * @param run  run
     * @return String
     */
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run){
        StringBuilder builder = new StringBuilder();
        String cols = run.getQueryColumns();
        PageNavi navi = run.getPageNavi();
        String sql = run.getBaseQuery();
        OrderStore orders = run.getOrderStore();
        long first = 0;
        long last = 0;
        String order = "";
        if(null != orders){
            order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
        }
        if(null != navi){
            first = navi.getFirstRow();
            last = navi.getLastRow();
        }
        if(first == 0 && null != navi){
            // top
            builder.append("SELECT TOP ").append(last+1).append(" "+cols+" FROM(\n");
            builder.append(sql).append("\n) AS _TAB_O \n");
            builder.append(order);
            return builder.toString();
        }
        if(null == navi){
            builder.append(sql).append("\n").append(order);
        }else{
            // 分页
            long rows = navi.getPageRows();
            if(rows * navi.getCurPage() > navi.getTotalRow()){
                // 最后一页不足10条
                rows = navi.getTotalRow() % navi.getPageRows();
            }
            String asc = order;
            String desc = order.replace("ASC", "<A_ORDER>");
            desc = desc.replace("DESC", "ASC");
            desc = desc.replace("<A_ORDER>", "DESC");
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
     * @param run run
     * @param dest 表 如果不指定则根据set解析
     * @param set 集合
     * @param columns 需插入的列
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, LinkedHashMap<String, Column> columns){
        //2000及以下
        StringBuilder builder = run.getBuilder();
        if(null == builder){
            builder = new StringBuilder();
            run.setBuilder(builder);
        }

        LinkedHashMap<String, Column> pks = null;
        PrimaryGenerator generator = checkPrimaryGenerator(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));
        if(null != generator){
            pks = set.getRow(0).getPrimaryColumns();
            columns.putAll(pks);
        }

        builder.append("INSERT INTO ").append(parseTable(dest));
        builder.append("(");

        boolean start = true;
        for(Column column:columns.values()){
            if(!start){
                builder.append(",");
            }
            start = false;
            String key = column.getName();
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
        }
        builder.append(")");
        int dataSize = set.size();
        for(int i=0; i<dataSize; i++){
            DataRow row = set.getRow(i);
            if(null == row){
                continue;
            }
            if(row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())){
                if(null != generator){
                    generator.create(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
                }
                //createPrimaryValue(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
            }
            builder.append("\n SELECT ");
            insertValue(runtime, run, row, true, false,false, columns);
            if(i<dataSize-1){
                //多行数据之间的分隔符
                builder.append("\n UNION ALL ");
            }
        }
    }

    /**
     * 根据Collection创建批量INSERT RunPrepare
     * 2000版本单独处理  insert into tab(nm) select 1 union all select 2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run run
     * @param dest 表 如果不指定则根据set解析
     * @param list 集合
     * @param columns 需插入的列
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, LinkedHashMap<String, Column> columns){
        StringBuilder builder = run.getBuilder();
        if(null == builder){
            builder = new StringBuilder();
            run.setBuilder(builder);
        }
        if(list instanceof DataSet){
            DataSet set = (DataSet) list;
            this.fillInsertContent(runtime, run, dest, set, columns);
            return;
        }

        PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));
        LinkedHashMap<String, Column> pks = null;
        if(null != generator) {
            Object entity = list.iterator().next();
            pks = EntityAdapterProxy.primaryKeys(entity.getClass());
            columns.putAll(pks);
        }

        builder.append("INSERT INTO ").append(parseTable(dest));
        builder.append("(");
        boolean start = true;
        for(Column column:columns.values()){
            if(!start){
                builder.append(",");
            }
            start = false;
            String key = column.getName();
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
        }
        builder.append(")\n ");
        int dataSize = list.size();
        int idx = 0;
        for(Object obj:list){
            builder.append("\n SELECT ");
           /* if(obj instanceof DataRow) {
                DataRow row = (DataRow)obj;
                if (row.hasPrimaryKeys() && BasicUtil.isEmpty(row.getPrimaryValue())) {
                    createPrimaryValue(row, type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
                }
                insertValue(template, run, row, true, false,false, keys);
            }else{*/
                boolean create = EntityAdapterProxy.createPrimaryValue(obj, pks);
                if(!create && null != generator){
                    generator.create(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
                    //createPrimaryValue(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
                }
                insertValue(runtime, run, obj, true, false, false, columns);
           // }
            if(idx<dataSize-1){
                //多行数据之间的分隔符
                builder.append("\n UNION ALL ");

            }
            idx ++;
        }
    }

    /**
     * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
     * @param table 表
     * @return sql
     * @throws Exception 异常
     */
    public List<Run> buildAddCommentRun(DataRuntime runtime, Table table) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun();
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        String comment = table.getComment();
        if(BasicUtil.isNotEmpty(comment)){
            builder.append("EXEC sp_addextendedproperty ");
            builder.append("'MS_Description',");
            builder.append("N'").append(comment).append("',");
            builder.append("'USER',");
            builder.append("'").append(table.getSchema()).append("',");
            builder.append("'TABLE',");
            builder.append("'").append(table.getName()).append("'");
        }

        return runs;
    }

    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception{
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun();
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        String comment = table.getComment();
        if(BasicUtil.isNotEmpty(comment)){
            builder.append("EXEC sp_updateextendedproperty ");
            builder.append("'MS_Description',");
            builder.append("N'").append(comment).append("',");
            builder.append("'USER',");
            builder.append("'").append(table.getSchema()).append("',");
            builder.append("'TABLE',");
            builder.append("'").append(table.getName()).append("'");
        }

        return runs;
    }

    /**
     * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
     * @param column 列
     * @return sql
     * @throws Exception 异常
     */
    public List<Run> buildAddCommentRun(DataRuntime runtime, Column column) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun();
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        String comment = column.getComment();
        if(BasicUtil.isNotEmpty(comment)){

            String schema = column.getSchema();
            if(BasicUtil.isEmpty(schema)){
                schema = column.getTable(true).getSchema();
            }
            builder.append("EXEC sp_addextendedproperty ");
            builder.append("'MS_Description',");
            builder.append("N'").append(comment).append("',");
            builder.append("'USER',");
            builder.append("'").append(schema).append("',");
            builder.append("'TABLE',");
            builder.append("'").append(column.getTableName(true)).append("',");
            builder.append("'COLUMN',");
            builder.append("'").append(column.getName()).append("'");
        }

        return runs;
    }

    /**
     * 修改备注
     *  -- 字段加注释
     * EXEC sys.sp_addextendedproperty @name=N'MS_Description'
     * , @value=N'注释内容'
     * , @level0type=N'SCHEMA'
     * ,@level0name=N'dbo'
     * , @level1type=N'TABLE'
     * ,@level1name=N'表名'
     * , @level2type=N'COLUMN'
     * ,@level2name=N'字段名'
     *
     * @param column 列
     * @return String
     */
    @Override
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Column column) throws Exception{
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun();
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        String comment = null;
        if(null != column.getUpdate()){
            comment = column.getUpdate().getComment();
        }else {
            comment = column.getComment();
        }
        if(BasicUtil.isNotEmpty(comment)){
            String schema = column.getSchema();
            if(BasicUtil.isEmpty(schema)){
                schema = column.getTable(true).getSchema();
            }
            builder.append("EXEC sp_updateextendedproperty ");
            builder.append("'MS_Description',");
            builder.append("N'").append(comment).append("',");
            builder.append("'USER',");
            builder.append("'").append(schema).append("',");
            builder.append("'TABLE',");
            builder.append("'").append(column.getTableName(true)).append("',");
            builder.append("'COLUMN',");
            builder.append("'").append(column.getName()).append("'");
        }

        return runs;
    }

}
