/* 
 * Copyright 2006-2022 www.anyline.org
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
 *
 *          
 */


package org.anyline.jdbc.adapter;


import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.Compare;
import org.anyline.exception.SQLException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.jdbc.param.ConfigStore;
import org.anyline.jdbc.prepare.RunPrepare;
import org.anyline.jdbc.prepare.Variable;
import org.anyline.jdbc.prepare.auto.AutoPrepare;
import org.anyline.jdbc.prepare.auto.init.Join;
import org.anyline.jdbc.run.Run;
import org.anyline.jdbc.run.TableRun;
import org.anyline.jdbc.run.TextRun;
import org.anyline.jdbc.run.XMLRun;
import org.anyline.jdbc.prepare.auto.TablePrepare;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.util.*;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class SQLAdapter extends SimpleJDBCAdapter implements JDBCAdapter {


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
     * public Run buildInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns)
     * public void createInserts(Run run, String dest, DataSet set,  List<String> keys)
     * public void createInserts(Run run, String dest, Collection list,  List<String> keys)
     * public int insert(String random, Object data, String sql, List<Object> values) throws Exception
     *
     * protected Run createInsertRunFromEntity(String dest, Object obj, boolean checkPrimary, List<String> columns)
     * protected Run createInsertRunFromCollection(String dest, Collection list, boolean checkPrimary, List<String> columns)
     * protected void insertValue(Run run, Object obj, boolean placeholder , List<String> keys)
     ******************************************************************************************************************/
    /**
     * 创建INSERT RunPrepare
     * @param dest 表
     * @param obj 实体
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
     * @return Run
     */
    @Override
    public Run buildInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns){
        return super.buildInsertRun(dest, obj, checkPrimary, columns);
    }

    /**
     * 根据DataSet创建批量INSERT RunPrepare
     * @param run run
     * @param dest 表 如果不指定则根据set解析
     * @param set 集合
     * @param keys 需插入的列
     */
    @Override
    public void createInserts(Run run, String dest, DataSet set,  List<String> keys){
        StringBuilder builder = run.getBuilder();
        if(null == builder){
            builder = new StringBuilder();
            run.setBuilder(builder);
        }
        builder.append("INSERT INTO ").append(parseTable(dest));
        builder.append("(");

        int keySize = keys.size();
        for(int i=0; i<keySize; i++){
            String key = keys.get(i);
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
            if(i<keySize-1){
                builder.append(",");
            }
        }
        builder.append(") VALUES ");
        int dataSize = set.size();
        for(int i=0; i<dataSize; i++){
            DataRow row = set.getRow(i);
            if(null == row){
                continue;
            }
            if(row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())){
                String pk = row.getPrimaryKey();
                if(null == pk){
                    pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
                }
                row.put(pk, primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
            }
            insertValue(run, row, false, false,true, keys);
            if(i<dataSize-1){
                builder.append(",");
            }
        }
    }

    /**
     * 根据Collection创建批量INSERT RunPrepare
     * @param run run
     * @param dest 表 如果不指定则根据set解析
     * @param list 集合
     * @param keys 需插入的列
     */
    @Override
    public void createInserts(Run run, String dest, Collection list,  List<String> keys){
        StringBuilder builder = run.getBuilder();
        if(null == builder){
            builder = new StringBuilder();
            run.setBuilder(builder);
        }
        if(list instanceof DataSet){
            DataSet set = (DataSet) list;
            createInserts(run, dest, set, keys);
            return;
        }
        builder.append("INSERT INTO ").append(parseTable(dest));
        builder.append("(");

        int keySize = keys.size();
        for(int i=0; i<keySize; i++){
            String key = keys.get(i);
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
            if(i<keySize-1){
                builder.append(",");
            }
        }
        builder.append(") VALUES ");
        int dataSize = list.size();
        int idx = 0;
        for(Object obj:list){
            if(obj instanceof DataRow) {
                DataRow row = (DataRow)obj;
                if (row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())) {
                    String pk = row.getPrimaryKey();
                    if (null == pk) {
                        pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
                    }
                    row.put(pk, primaryCreater.createPrimary(type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
                }
                insertValue(run, row, false, false,true, keys);
            }else{
                String pk = null;
                Object pv = null;
                if(AdapterProxy.hasAdapter()){
                    pk = AdapterProxy.primaryKey(obj.getClass());
                    pv = AdapterProxy.primaryValue(obj);
                    AdapterProxy.createPrimaryValue(obj);
                }else{
                    pk = DataRow.DEFAULT_PRIMARY_KEY;
                    pv = BeanUtil.getFieldValue(obj, pk);
                    if(null != primaryCreater && null == pv){
                        pv = primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null);
                        BeanUtil.setFieldValue(obj, pk, pv);
                    }
                }
                insertValue(run, obj, false, false, true, keys);
            }
            if(idx<dataSize-1){
                builder.append(",");
            }
            idx ++;
        }
    }

    /**
     * 根据entity创建 INSERT RunPrepare
     * @param dest
     * @param obj
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns
     * @return Run
     */
    @Override
    protected Run createInsertRunFromEntity(String dest, Object obj, boolean checkPrimary, List<String> columns){
        Run run = new TableRun(this,dest);
        // List<Object> values = new ArrayList<Object>();
        StringBuilder builder = new StringBuilder();
        if(BasicUtil.isEmpty(dest)){
            throw new SQLException("未指定表");
        }
        StringBuilder valuesBuilder = new StringBuilder();
        DataRow row = null;
        if(obj instanceof DataRow){
            row = (DataRow)obj;
            if(row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())){
                String pk = row.getPrimaryKey();
                if(null == pk){
                    pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
                }
                row.put(pk, primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
            }
        }else{
            String pk = null;
            Object pv = null;
            if(AdapterProxy.hasAdapter()){
                pk = AdapterProxy.primaryKey(obj.getClass());
                pv = AdapterProxy.primaryValue(obj);
                AdapterProxy.createPrimaryValue(obj);
            }else{
                pk = DataRow.DEFAULT_PRIMARY_KEY;
                pv = BeanUtil.getFieldValue(obj, pk);
                if(null != primaryCreater && null == pv){
                    pv = primaryCreater.createPrimary(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null);
                    BeanUtil.setFieldValue(obj, pk, pv);
                }
            }
        }

        /*确定需要插入的列*/

        List<String> keys = confirmInsertColumns(dest, obj, columns);
        if(null == keys || keys.size() == 0){
            throw new SQLException("未指定列(DataRow或Entity中没有需要更新的属性值)["+obj.getClass().getName()+":"+BeanUtil.object2json(obj)+"]");
        }
        builder.append("INSERT INTO ").append(parseTable(dest));
        builder.append("(");
        valuesBuilder.append(") VALUES (");
        List<String> insertColumns = new ArrayList<>();
        int size = keys.size();
        for(int i=0; i<size; i++){
            String key = keys.get(i);
            Object value = null;
            if(null != row){
                value = row.get(key);
            }else{
                if(AdapterProxy.hasAdapter()){
                    value = BeanUtil.getFieldValue(obj, AdapterProxy.field(obj.getClass(), key));
                }else{
                    value = BeanUtil.getFieldValue(obj, key);
                }
            }
            String str = null;
            if(value instanceof String){
                str = (String)value;
            }
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
            if(null != str && str.startsWith("${") && str.endsWith("}")){
                value = str.substring(2, str.length()-1);
                valuesBuilder.append(value);
            }else if(null != value && value instanceof SQL_BUILD_IN_VALUE){
                value = buildInValue((SQL_BUILD_IN_VALUE)value);
                valuesBuilder.append(value);
            }else{
                valuesBuilder.append("?");
                insertColumns.add(key);
                if("NULL".equals(value)){
                    addRunValue(run, key, null);
                }else{
                    addRunValue(run, key, value);
                }
            }
            if(i<size-1){
                builder.append(",");
                valuesBuilder.append(",");
            }
        }
        valuesBuilder.append(")");
        builder.append(valuesBuilder);
        run.setBuilder(builder);
        run.setInsertColumns(insertColumns);

        return run;
    }

    /**
     * 根据collection创建 INSERT RunPrepare
     * @param dest 表
     * @param list 对象集合
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要插入的列，如果不指定则全部插入
     * @return Run
     */
    @Override
    protected Run createInsertRunFromCollection(String dest, Collection list, boolean checkPrimary, List<String> columns){
        Run run = new TableRun(this,dest);
        if(null == list || list.size() ==0){
            throw new SQLException("空数据");
        }
        Object first = null;
        if(list instanceof DataSet){
            DataSet set = (DataSet)list;
            first = set.getRow(0);
            if(BasicUtil.isEmpty(dest)){
                dest = DataSourceHolder.parseDataSource(dest,set);
            }
            if(BasicUtil.isEmpty(dest)){
                dest = DataSourceHolder.parseDataSource(dest,first);
            }
        }else{
            first = list.iterator().next();
            if(BasicUtil.isEmpty(dest)) {
                if (AdapterProxy.hasAdapter()) {
                    dest = AdapterProxy.table(first.getClass());
                }
            }
        }
        if(BasicUtil.isEmpty(dest)){
            throw new SQLException("未指定表");
        }
        /*确定需要插入的列*/
        List<String> keys = confirmInsertColumns(dest, first, columns);
        if(null == keys || keys.size() == 0){
            throw new SQLException("未指定列(DataRow或Entity中没有需要更新的属性值)["+first.getClass().getName()+":"+BeanUtil.object2json(first)+"]");
        }
        createInserts(run, dest, list, keys);

        return run;
    }
    /**
     * 生成insert sql的value部分,每个Entity(每行数据)调用一次
     * (1,2,3)
     * (?,?,?)
     * @param run           run
     * @param obj           Entity或DataRow
     * @param placeholder   是否使用占位符(批量操作时不要超出数量)
     * @param scope         是否带(), 拼接在select后时不需要
     * @param alias         是否添加别名
     * @param keys          需要插入的列
     */
    protected void insertValue(Run run, Object obj, boolean placeholder, boolean alias, boolean scope, List<String> keys){
        StringBuilder builder = run.getBuilder();
        int keySize = keys.size();
        if(scope) {
            builder.append("(");
        }
        for(int i=0; i<keySize; i++){
            boolean place = placeholder;
            String key = keys.get(i);
            Object value = BeanUtil.getFieldValue(obj, key);
            if(value != null){
                if(value instanceof SQL_BUILD_IN_VALUE){
                    place = false;
                }else if(value instanceof String){
                    String str = (String)value;
                    if(str.startsWith("${") && str.endsWith("}")){
                        place = false;
                    }
                }
            }
            if(place){
                builder.append("?");
                addRunValue(run, null, value);
            }else {
                value(builder, obj, key);
            }

            if(alias){
                builder.append(" AS ").append(key);
            }
            if (i < keySize - 1) {
                builder.append(",");
            }
        }
        if(scope) {
            builder.append(")");
        }
    }
    public String getPrimayKey(Object obj){
        String key = null;
        if(obj instanceof Collection){
            obj = ((Collection)obj).iterator().next();
        }
        if(obj instanceof DataRow){
            key = ((DataRow)obj).getPrimaryKey();
        }else{
            key = AdapterProxy.primaryKey(obj.getClass());
        }
        return key;
    }
    /**
     * 执行 insert
     * @param random random
     * @param jdbc jdbc
     * @param data entity|DataRow|DataSet
     * @param sql sql
     * @param values 占位参数值
     * @return int 影响行数
     * @throws Exception
     */
    @Override
    public int insert(String random, Object data, String sql, List<Object> values, String[] pks) throws Exception{
        int cnt = 0;
        KeyHolder keyholder = new GeneratedKeyHolder();
        cnt = jdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection con) throws java.sql.SQLException {
                PreparedStatement ps = null;
                if(null != pks && pks.length>0){
                    ps = con.prepareStatement(sql, pks);
                }else {
                    ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                }
                int idx = 0;
                if (null != values) {
                    for (Object obj : values) {
                        ps.setObject(++idx, obj);
                    }
                }
                return ps;
            }
        }, keyholder);
        identity(random, data, keyholder);
        return cnt;
    }
    @Override
    public boolean identity(String random, Object data, KeyHolder keyholder){
        try {
            if(null == keyholder){
                return false;
            }
            List<Map<String,Object>> keys = keyholder.getKeyList();
            String id_key = generatedKey();
            if(null == id_key && keys.size()>0){
                Map<String,Object> key = keys.get(0);
                id_key = key.keySet().iterator().next();
            }
            if(data instanceof Collection){
                List<Object> ids = new ArrayList<>();
                Collection list = (Collection) data;
                if(BasicUtil.isEmpty(id_key)){
                    return false;
                }
                int i = 0;
                int data_size = list.size();
                if(list.size() == keys.size()) {
                    for (Object item : list) {
                        Map<String, Object> key = keys.get(i);
                        Object id = key.get(id_key);
                        ids.add(id);
                        setPrimaryValue(item, id);
                        i++;
                    }
                }else{
                    Object last = keys.get(0).get(id_key);
                    if(last instanceof Number){
                        Long num = BasicUtil.parseLong(last.toString(), null);
                        if(null != num){
                            num = num - data_size + 1;
                            for(Object item:list){
                                setPrimaryValue(item, num++);
                            }
                        }
                    }
                }
                log.warn("{}[exe insert][生成主键:{}]", random, ids);
            }else{
                Object id = keys.get(0).get(id_key);
                setPrimaryValue(data, id);
                log.warn("{}[exe insert][生成主键:{}]", random, id);
            }
        }catch (Exception e){
            log.warn("{}[exe insert][返回主键失败]", random);
            return false;
        }
        return true;
    }

    /* *****************************************************************************************************************
     * 													UPDATE
     * -----------------------------------------------------------------------------------------------------------------
     * protected Run buildUpdateRunFromObject(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns)
     * protected Run buildUpdateRunFromDataRow(String dest, DataRow row, ConfigStore configs, boolean checkPrimary, List<String> columns)
     ******************************************************************************************************************/

    protected Run buildUpdateRunFromObject(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
        TableRun run = new TableRun(this,dest);
        StringBuilder builder = run.getBuilder();
        // List<Object> values = new ArrayList<Object>();
        List<String> keys = null;
        List<String> primaryKeys = null;
        if(null != columns && columns.size() >0 ){
            keys = columns;
        }else{
            if(AdapterProxy.hasAdapter()){
                keys = AdapterProxy.columns(obj.getClass());
            }
        }
        if(AdapterProxy.hasAdapter()){
            primaryKeys = AdapterProxy.primaryKeys(obj.getClass());
        }else{
            primaryKeys = new ArrayList<>();
            primaryKeys.add(DataRow.DEFAULT_PRIMARY_KEY);
        }

        // 不更新主键 除非显示指定
        for(String pk:primaryKeys){
            if(!columns.contains(pk)) {
                keys.remove(pk);
            }
        }
        //不更新默认主键  除非显示指定
        if(!columns.contains(DataRow.DEFAULT_PRIMARY_KEY)) {
            keys.remove(DataRow.DEFAULT_PRIMARY_KEY);
        }

        List<String> updateColumns = new ArrayList<>();
        /*构造SQL*/
        int size = keys.size();
        if(size > 0){
            builder.append("UPDATE ").append(parseTable(dest));
            builder.append(" SET").append(JDBCAdapter.BR_TAB);
            for(int i=0; i<size; i++){
                String key = keys.get(i);
                Object value = null;
                if(AdapterProxy.hasAdapter()){
                    Field field = AdapterProxy.field(obj.getClass(), key);
                    value = BeanUtil.getFieldValue(obj, field);
                }else {
                    value = BeanUtil.getFieldValue(obj, key);
                }
                if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}")){
                    String str = value.toString();
                    value = str.substring(2, str.length()-1);

                    SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ").append(value).append(JDBCAdapter.BR_TAB);
                }else{
                    SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ?").append(JDBCAdapter.BR_TAB);
                    if("NULL".equals(value)){
                        value = null;
                    }
                    updateColumns.add(key);
                    addRunValue(run, key, value);
                }
                if(i<size-1){
                    builder.append(",");
                }
            }
            builder.append(JDBCAdapter.BR);
            builder.append("\nWHERE 1=1").append(JDBCAdapter.BR_TAB);
            if(null == configs) {
                for (String pk : primaryKeys) {
                    builder.append(" AND ");
                    SQLUtil.delimiter(builder, pk, getDelimiterFr(), getDelimiterTo()).append(" = ?");
                    updateColumns.add(pk);
                    if (AdapterProxy.hasAdapter()) {
                        Field field = AdapterProxy.field(obj.getClass(), pk);
                        addRunValue(run, pk, BeanUtil.getFieldValue(obj, field));
                    } else {
                        addRunValue(run, pk, BeanUtil.getFieldValue(obj, pk));
                    }
                }
            }else{
                run.setConfigStore(configs);
                run.init();
                run.appendCondition();
            }
        }
        run.setUpdateColumns(updateColumns);

        return run;
    }
    protected Run buildUpdateRunFromDataRow(String dest, DataRow row, ConfigStore configs, boolean checkPrimary, List<String> columns){
        TableRun run = new TableRun(this,dest);
        StringBuilder builder = run.getBuilder();
        // List<Object> values = new ArrayList<Object>();
        /*确定需要更新的列*/
        List<String> keys = confirmUpdateColumns(dest, row, configs, columns);
        List<String> primaryKeys = row.getPrimaryKeys();
        if(primaryKeys.size() == 0){
            throw new SQLUpdateException("[更新更新异常][更新条件为空,update方法不支持更新整表操作]");
        }

        // 不更新主键 除非显示指定
        for(String pk:primaryKeys){
            if(!columns.contains(pk)) {
                keys.remove(pk);
            }
        }
        //不更新默认主键  除非显示指定
        if(!columns.contains(DataRow.DEFAULT_PRIMARY_KEY)) {
            keys.remove(DataRow.DEFAULT_PRIMARY_KEY);
        }
        List<String> updateColumns = new ArrayList<>();
        /*构造SQL*/
        int size = keys.size();
        if(size > 0){
            builder.append("UPDATE ").append(parseTable(dest));
            builder.append(" SET").append(JDBCAdapter.BR_TAB);
            for(int i=0; i<size; i++){
                String key = keys.get(i);
                Object value = row.get(key);
                if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}") ){
                    String str = value.toString();
                    value = str.substring(2, str.length()-1);
                    SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ").append(value).append(JDBCAdapter.BR_TAB);
                }else{
                    SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ?").append(JDBCAdapter.BR_TAB);
                    if("NULL".equals(value)){
                        value = null;
                    }
                    updateColumns.add(key);
                    addRunValue(run, key, value);
                }
                if(i<size-1){
                    builder.append(",");
                }
            }
            builder.append(JDBCAdapter.BR);
            builder.append("\nWHERE 1=1").append(JDBCAdapter.BR_TAB);
            if(null == configs) {
                for (String pk : primaryKeys) {
                    builder.append(" AND ");
                    SQLUtil.delimiter(builder, pk, getDelimiterFr(), getDelimiterTo()).append(" = ?");
                    updateColumns.add(pk);
                    addRunValue(run, pk,  row.get(pk));
                }
            }else{
                run.setConfigStore(configs);
                run.init();
                run.appendCondition();
            }
        }
        run.setUpdateColumns(updateColumns);

        return run;
    }


    /* *****************************************************************************************************************
     * 													QUERY
     * -----------------------------------------------------------------------------------------------------------------
     * public StringBuilder buildConditionLike(StringBuilder builder, Compare compare)
     * public StringBuilder buildConditionIn(StringBuilder builder, Compare compare, Object value)
     *
     * protected void buildQueryRunContent(XMLRun run)
     * protected void buildQueryRunContent(TextRun run)
     * protected void buildQueryRunContent(TableRun run)
     ******************************************************************************************************************/

    /**
     * 构造 LIKE 查询条件
     * @param builder builder
     * @param compare compare
     * @return StringBuilder
     */
    @Override
    public StringBuilder buildConditionLike(StringBuilder builder, Compare compare){
        if(compare == Compare.LIKE){
            builder.append(" LIKE ").append(concat("'%'", "?" , "'%'"));
        }else if(compare == Compare.LIKE_PREFIX){
            builder.append(" LIKE ").append(concat("?" , "'%'"));
        }else if(compare == Compare.LIKE_SUFFIX){
            builder.append(" LIKE ").append(concat("'%'", "?"));
        }
        return builder;
    }

    /**
     * 构造(NOT) IN 查询条件
     * @param builder builder
     * @param compare compare
     * @param value value
     * @return StringBuilder
     */
    @Override
    public StringBuilder buildConditionIn(StringBuilder builder, Compare compare, Object value){
        if(compare == Compare.NOT_IN){
            builder.append(" NOT");
        }
        builder.append(" IN (");
        if(value instanceof Collection){
            Collection<Object> coll = (Collection)value;
            int size = coll.size();
            for(int i=0; i<size; i++){
                builder.append("?");
                if(i < size-1){
                    builder.append(",");
                }
            }
            builder.append(")");
        }else{
            builder.append("= ?");
        }
        return builder;
    }

    protected void buildQueryRunContent(XMLRun run){
    }
    protected void buildQueryRunContent(TextRun run){
        StringBuilder builder = run.getBuilder();
        RunPrepare prepare = run.getPrepare();
        List<Variable> variables = run.getVariables();
        String result = prepare.getText();
        if(null != variables){
            for(Variable var:variables){
                if(null == var){
                    continue;
                }
                if(var.getType() == Variable.VAR_TYPE_REPLACE){
                    // CD = ::CD
                    Object varValue = var.getValues();
                    String value = null;
                    if(BasicUtil.isNotEmpty(varValue)){
                        value = varValue.toString();
                    }
                    if(null != value){
                        result = result.replace("::"+var.getKey(), value);
                    }else{
                        result = result.replace("::"+var.getKey(), "NULL");
                    }
                }
            }
            for(Variable var:variables){
                if(null == var){
                    continue;
                }
                if(var.getType() == Variable.VAR_TYPE_KEY_REPLACE){
                    // CD = ':CD'
                    List<Object> varValues = var.getValues();
                    String value = null;
                    if(BasicUtil.isNotEmpty(true,varValues)){
                        value = (String)varValues.get(0);
                    }
                    if(null != value){
                        result = result.replace(":"+var.getKey(), value);
                    }else{
                        result = result.replace(":"+var.getKey(), "");
                    }
                }
            }
            for(Variable var:variables){
                if(null == var){
                    continue;
                }
                if(var.getType() == Variable.VAR_TYPE_KEY){
                    // CD = :CD
                    List<Object> varValues = var.getValues();
                    if(BasicUtil.isNotEmpty(true, varValues)){
                        if(var.getCompare() == Compare.IN){
                            // 多个值IN
                            String replaceSrc = ":"+var.getKey();
                            String replaceDst = "";
                            for(Object tmp:varValues){
                                addRunValue(run, var.getKey(), tmp);
                                replaceDst += " ?";
                            }
                            replaceDst = replaceDst.trim().replace(" ", ",");
                            result = result.replace(replaceSrc, replaceDst);
                        }else{
                            // 单个值
                            result = result.replace(":"+var.getKey(), "?");
                            addRunValue(run, var.getKey(), varValues.get(0));
                        }
                    }
                }
            }
            // 添加其他变量值
            for(Variable var:variables){
                if(null == var){
                    continue;
                }
                // CD = ?
                if(var.getType() == Variable.VAR_TYPE_INDEX){
                    List<Object> varValues = var.getValues();
                    String value = null;
                    if(BasicUtil.isNotEmpty(true, varValues)){
                        value = (String)varValues.get(0);
                    }
                    addRunValue(run, var.getKey(), value);
                }
            }
        }

        builder.append(result);
        run.appendCondition();
        run.appendGroup();
        // appendOrderStore();
        run.checkValid();
    }
    protected void buildQueryRunContent(TableRun run){
        StringBuilder builder = run.getBuilder();
        TablePrepare sql = (TablePrepare)run.getPrepare();
        builder.append("SELECT ");
        if(null != sql.getDistinct()){
            builder.append(sql.getDistinct());
        }
        builder.append(JDBCAdapter.BR_TAB);
        List<String> columns = sql.getColumns();
        if(null != columns && columns.size()>0){
            // 指定查询列
            int size = columns.size();
            for(int i=0; i<size; i++){
                String column = columns.get(i);
                if(BasicUtil.isEmpty(column)){
                    continue;
                }
                if(column.startsWith("${") && column.endsWith("}")){
                    column = column.substring(2, column.length()-1);
                    builder.append(column);
                }else{
                    if(column.toUpperCase().contains(" AS ") || column.contains("(") || column.contains(",")){
                        builder.append(column);
                    }else if("*".equals(column)){
                        builder.append("*");
                    }else{
                        SQLUtil.delimiter(builder, column, delimiterFr, delimiterTo);
                    }
                }
                if(i<size-1){
                    builder.append(",");
                }
            }
            builder.append(JDBCAdapter.BR);
        }else{
            // 全部查询
            builder.append("*");
            builder.append(JDBCAdapter.BR);
        }
        builder.append("FROM").append(JDBCAdapter.BR_TAB);
        if(null != run.getSchema()){
            SQLUtil.delimiter(builder, run.getSchema(), delimiterFr, delimiterTo).append(".");
        }
        SQLUtil.delimiter(builder, run.getTable(), delimiterFr, delimiterTo);
        builder.append(JDBCAdapter.BR);
        if(BasicUtil.isNotEmpty(sql.getAlias())){
            // builder.append(" AS ").append(sql.getAlias());
            builder.append("  ").append(sql.getAlias());
        }
        List<Join> joins = sql.getJoins();
        if(null != joins) {
            for (Join join:joins) {
                builder.append(JDBCAdapter.BR_TAB).append(join.getType().getCode()).append(" ");
                SQLUtil.delimiter(builder, join.getName(), delimiterFr, delimiterTo);
                if(BasicUtil.isNotEmpty(join.getAlias())){
                    // builder.append(" AS ").append(join.getAlias());
                    builder.append("  ").append(join.getAlias());
                }
                builder.append(" ON ").append(join.getCondition());
            }
        }

        builder.append("\nWHERE 1=1\n\t");
        /*添加查询条件*/
        // appendConfigStore();
        run.appendCondition();
        run.appendGroup();
        run.appendOrderStore();
        run.checkValid();
    }

    /* *****************************************************************************************************************
     * 													EXISTS
     * -----------------------------------------------------------------------------------------------------------------
     * public String parseExists(Run run)
     ******************************************************************************************************************/

    @Override
    public String parseExists(Run run){
        String sql = "SELECT EXISTS(\n" + run.getBuilder().toString() +"\n)  IS_EXISTS";
        sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE ");
        return sql;
    }

    /* *****************************************************************************************************************
     * 													TOTAL
     * -----------------------------------------------------------------------------------------------------------------
     * public String parseTotalQuery(Run run)
     ******************************************************************************************************************/

    /**
     * 求总数SQL
     * Run 反转调用
     * @param run  run
     * @return String
     */
    @Override
    public String parseTotalQuery(Run run){
        String sql = "SELECT COUNT(0) AS CNT FROM (\n" + run.getBuilder().toString() +"\n) F";
        sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE ");
        return sql;
    }


    /* *****************************************************************************************************************
     * 													DELETE
     * -----------------------------------------------------------------------------------------------------------------
     * protected Run buildDeleteRunContent(TableRun run)
     * protected Run createDeleteRunSQLFromTable(String table, String key, Object values)
     * protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns)
     ******************************************************************************************************************/

    protected Run buildDeleteRunContent(TableRun run){
        AutoPrepare prepare =  (AutoPrepare)run.getPrepare();
        StringBuilder builder = run.getBuilder();
        builder.append("DELETE FROM ");
        if(null != run.getSchema()){
            SQLUtil.delimiter(builder, run.getSchema(), delimiterFr, delimiterTo).append(".");
        }

        SQLUtil.delimiter(builder, run.getTable(), delimiterFr, delimiterTo);
        builder.append(JDBCAdapter.BR);
        if(BasicUtil.isNotEmpty(prepare.getAlias())){
            // builder.append(" AS ").append(sql.getAlias());
            builder.append("  ").append(prepare.getAlias());
        }
        List<Join> joins = prepare.getJoins();
        if(null != joins) {
            for (Join join:joins) {
                builder.append(JDBCAdapter.BR_TAB).append(join.getType().getCode()).append(" ");
                SQLUtil.delimiter(builder, join.getName(), getDelimiterFr(), getDelimiterTo());
                if(BasicUtil.isNotEmpty(join.getAlias())){
                    builder.append("  ").append(join.getAlias());
                }
                builder.append(" ON ").append(join.getCondition());
            }
        }

        builder.append("\nWHERE 1=1\n\t");



        /*添加查询条件*/
        // appendConfigStore();
        run.appendCondition();
        run.appendGroup();
        run.appendOrderStore();
        run.checkValid();

        return run;
    }
    @SuppressWarnings("rawtypes")
    protected Run createDeleteRunSQLFromTable(String table, String key, Object values){
        if(null == table || null == key || null == values){
            return null;
        }
        StringBuilder builder = new StringBuilder();
        TableRun run = new TableRun(this,table);
        builder.append("DELETE FROM ").append(table).append(" WHERE ");
        if(values instanceof Collection){
            Collection cons = (Collection)values;
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
            if(cons.size() > 1){
                builder.append(" IN(");
                int idx = 0;
                for(Object obj:cons){
                    if(idx > 0){
                        builder.append(",");
                    }
                    // builder.append("'").append(obj).append("'");
                    builder.append("?");
                    idx ++;
                }
                builder.append(")");
            }else if(cons.size() == 1){
                for(Object obj:cons){
                    builder.append("=?");
                }
            }else{
                throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
            }
        }else{
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo());
            builder.append("=?");
        }
        addRunValue(run, key, values);

        run.setBuilder(builder);

        return run;
    }
    protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns){
        TableRun run = new TableRun(this,dest);
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM ").append(parseTable(dest)).append(" WHERE ");
        List<String> keys = new ArrayList<>();
        if(null != columns && columns.length>0){
            for(String col:columns){
                keys.add(col);
            }
        }else{
            if(obj instanceof DataRow){
                keys = ((DataRow)obj).getPrimaryKeys();
            }else{
                if(AdapterProxy.hasAdapter()){
                    keys = AdapterProxy.primaryKeys(obj.getClass());
                }
            }
        }
        int size = keys.size();
        if(size >0){
            for(int i=0; i<size; i++){
                if(i > 0){
                    builder.append("\nAND ");
                }
                String key = keys.get(i);

                SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(" = ? ");
                Object value = null;
                if(obj instanceof DataRow){
                    value = ((DataRow)obj).get(key);
                }else{
                    if(AdapterProxy.hasAdapter()){
                        value = BeanUtil.getFieldValue(obj,AdapterProxy.field(obj.getClass(), key));
                    }else{
                        value = BeanUtil.getFieldValue(obj, key);
                    }
                }
                addRunValue(run, key,value);
            }
        }else{
            throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
        }
        run.setBuilder(builder);

        return run;
    }

    /* *****************************************************************************************************************
     * 													COMMON
     * -----------------------------------------------------------------------------------------------------------------
     * protected String concatFun(String ... args)
     * protected String concatOr(String ... args)
     * protected String concatAdd(String ... args)
     ******************************************************************************************************************/

    protected void addRunValue(Run run, String key, Object value){
        /*if(null != value && value instanceof SQL_BUILD_IN_VALUE){
            value = buildInValue((SQL_BUILD_IN_VALUE)value);
            if(null != value){
                value = "${"+value+"}";
            }
        }*/
        run.addValues(key, value);
    }
    /* ************** 拼接字符串 *************** */
    protected String concatFun(String ... args){
        String result = "";
        if(null != args && args.length > 0){
            result = "concat(";
            int size = args.length;
            for(int i=0; i<size; i++){
                String arg = args[i];
                if(i>0){
                    result += ",";
                }
                result += arg;
            }
            result += ")";
        }
        return result;
    }

    protected String concatOr(String ... args){
        String result = "";
        if(null != args && args.length > 0){
            int size = args.length;
            for(int i=0; i<size; i++){
                String arg = args[i];
                if(i>0){
                    result += " || ";
                }
                result += arg;
            }
        }
        return result;
    }
    protected String concatAdd(String ... args){
        String result = "";
        if(null != args && args.length > 0){
            int size = args.length;
            for(int i=0; i<size; i++){
                String arg = args[i];
                if(i>0){
                    result += " + ";
                }
                result += arg;
            }
        }
        return result;
    }

}
