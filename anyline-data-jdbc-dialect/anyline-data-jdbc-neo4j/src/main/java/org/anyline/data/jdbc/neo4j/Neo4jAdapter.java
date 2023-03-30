package org.anyline.data.jdbc.neo4j;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.Compare;
import org.anyline.exception.SQLException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.DefaultJDBCAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.Variable;
import org.anyline.data.entity.Join;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.run.TextRun;
import org.anyline.data.run.XMLRun;
import org.anyline.util.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@Repository("anyline.data.jdbc.adapter.neo4j")
public class Neo4jAdapter extends DefaultJDBCAdapter implements JDBCAdapter, InitializingBean {
    public DB_TYPE type(){
        return DB_TYPE.Neo4j;
    }
    public Neo4jAdapter(){
        delimiterFr = "`";
        delimiterTo = "`";
    }
    @Value("${anyline.jdbc.delimiter.neo4j:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }

    /* *****************************************************************************************************************
     *
     * 													DML
     *
     * =================================================================================================================
     * INSERT			: 插入
     * UPDATE			: 更新
     * QUERY			: 查询(RunPrepare/XML/TABLE/VIEW/PROCEDURE)
     * EXISTS			: 是否存在
     * COUNT			: 统计
     * EXECUTE			: 执行(原生SQL及存储过程)
     * DELETE			: 删除
     * COMMON			：其他通用
     ******************************************************************************************************************/


    /* *****************************************************************************************************************
     * 													INSERT
     * -----------------------------------------------------------------------------------------------------------------
     * public Run buildInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns)
     * public void createInserts(Run run, String dest, DataSet set,  List<String> keys)
     * public void createInserts(Run run, String dest, Collection list,  List<String> keys)
     *
     * protected Run createInsertRunFromEntity(String dest, Object obj, boolean checkPrimary, List<String> columns)
     * protected Run createInsertRunFromCollection(String dest, Collection list, boolean checkPrimary, List<String> columns)
     *  public int insert(String random, Object data, String sql, List<Object> values, String[] pks) throws Exception
     ******************************************************************************************************************/

    /**
     * 创建INSERT RunPrepare
     * @param dest 表
     * @param obj 实体
     * @param checkPrimary 是否检测主键
     * @param columns 需要抛入的列 如果不指定  则根据实体属性解析
     * @return Run
     */
    @Override
    public Run buildInsertRun(String dest, Object obj, boolean checkPrimary, List<String> columns){
        return super.buildInsertRun(dest, obj, checkPrimary, columns);
    }

    /**
     * 根据DataSet创建批量INSERT RunPrepare
     * CREATE (:Dept{name:1}),(:Dept{name:2}),(:Dept{name:3})
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
        builder.append("CREATE ");

        int dataSize = set.size();
        for(int i=0; i<dataSize; i++){
            DataRow row = set.getRow(i);
            if(null == row){
                continue;
            }
            insertValue("e"+i,run,  dest,  row, keys);
            if(i<dataSize-1){
                builder.append(",");
            }
        }
        builder.append(" RETURN ");
        for(int i=0; i<dataSize; i++){
            if(i>0){
                builder.append(",");
            }
            builder.append(" ID(e").append(i).append(") AS __ID").append(i);
        }
    }


    /**
     * 根据Collection创建批量INSERT
     * create(:Dept{name:1}),(:Dept{name:2}),(:Dept{name:3})
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
        builder.append("CREATE ");
        int dataSize = list.size();
        int idx = 0;
        for(Object obj:list){
            insertValue("e"+idx,run, dest, obj, keys);
            if(idx<dataSize-1){
                builder.append(",");
            }
            idx ++;
        }
        builder.append(" RETURN ");
        for(int i=0; i<dataSize; i++){
            if(i>0){
                builder.append(",");
            }
            builder.append(" ID(e").append(i).append(") AS __ID").append(i);
        }
    }

    /**
     * 根据entity创建 INSERT RunPrepare
     * @param dest
     * @param obj
     * @param checkPrimary
     * @param columns
     * @return Run
     */
    @Override
    protected Run createInsertRunFromEntity(String dest, Object obj, boolean checkPrimary, List<String> columns){
        Run run = new TableRun(this, dest);
        run.setPrepare(new DefaultTablePrepare());
        StringBuilder builder = run.getBuilder();
        if(BasicUtil.isEmpty(dest)){
            throw new SQLException("未指定表");
        }
        // CREATE (emp:Employee{id:123,name:"zh"})
        builder.append("CREATE ");
        insertValue("e0",run, dest, obj, columns);
        builder.append(" RETURN ID(e0) AS __ID0");
        return run;
    }

    /**
     * 根据collection创建 INSERT RunPrepare
     * @param dest 表
     * @param list 对象集合
     * @param checkPrimary 是否检测主键
     * @param columns 需要插入的列,如果不指定则全部插入
     * @return Run
     */
    @Override
    protected Run createInsertRunFromCollection(String dest, Collection list, boolean checkPrimary, List<String> columns){
        Run run = new TableRun(this,dest);
        if(null == list || list.size() ==0){
            throw new SQLException("空数据");
        }
        createInserts(run, dest, list, columns);

        return run;
    }
    /**
     * 生成insert sql的value部分,每个Entity(每行数据)调用一次
     * (:User{name:'ZH',age:20})
     * @param run run
     * @param obj Entity或DataRow
     * @param columns 需要插入的列
     */
    protected void insertValue(String alias, Run run, String dest, Object obj, List<String> columns){
        StringBuilder builder = run.getBuilder();
        if(null == builder){
            builder = new StringBuilder();
            run.setBuilder(builder);
        }
        DataRow row = null;
        if(obj instanceof DataRow){
            row = (DataRow)obj;
            if(row.hasPrimaryKeys() && null != primaryGenerator){
                createPrimaryValue(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
            }
        }else{
            if(EntityAdapterProxy.hasAdapter()){
                EntityAdapterProxy.createPrimaryValue(obj);
            }else{
                if(null != primaryGenerator){
                    createPrimaryValue(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null, null);
                }
            }
        }

        /*确定需要插入的列*/

        List<String> keys = confirmInsertColumns(dest, obj, columns, false);
        // CREATE (e:Employee{id:123,name:"zh"})
        builder.append("(");
        if(BasicUtil.isNotEmpty(alias)){
            builder.append(alias);
        }
        builder.append(":").append(parseTable(dest));
        builder.append("{");
        List<String> insertColumns = new ArrayList<>();
        int size = keys.size();
        for(int i=0; i<size; i++){
            String key = keys.get(i);
            Object value = null;
            if(null != row){
                value = row.get(key);
            }else{
                if(EntityAdapterProxy.hasAdapter()){
                    value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
                }else{
                    value = BeanUtil.getFieldValue(obj, key);
                }
            }
            SQLUtil.delimiter(builder, key, getDelimiterFr(), getDelimiterTo()).append(":");
            if(null != value && value.toString().startsWith("${") && value.toString().endsWith("}")){
                String str = value.toString();
                value = str.substring(2, str.length()-1);
                if(value.toString().startsWith("${") && value.toString().endsWith("}")){
                    builder.append("?");
                    insertColumns.add(key);
                    run.addValues(key, value);
                }else {
                    builder.append(value);
                }
            }else{
                builder.append("?");
                insertColumns.add(key);
                if("NULL".equals(value)){
                    // values.add(null);
                    run.addValues(key, null);
                }else{
                    // values.add(value);
                    run.addValues(key, value);
                }
            }
            if(i<size-1){
                builder.append(",");
            }
        }
        builder.append("})");
        run.setInsertColumns(insertColumns);
    }

    /**
     * 执行 insert
     * @param random random
     * @param data data
     * @param sql sql
     * @param values value
     * @return int
     */
    @Override
    public int insert(String random, Object data, String sql, List<Object> values, String[] pks) throws Exception{
        int cnt = 0;
        DataSource ds = null;
        Connection con = null;
        try{
            ds = jdbc.getDataSource();
            con = DataSourceUtils.getConnection(ds);
            PreparedStatement ps = con.prepareStatement(sql);
            int idx = 0;
            if (null != values) {
                for (Object obj : values) {
                    ps.setObject(++idx, obj);
                }
            }
            ResultSet rs = ps.executeQuery();

            if(data instanceof Collection){
                List<Object> ids = new ArrayList<>();
                Collection list = (Collection) data;
                if(rs.next()) {
                    for(Object item:list){
                        Object id = rs.getObject("__ID"+ cnt);
                        ids.add(id);
                        setPrimaryValue(item, id);
                        cnt++;
                    }
                }
                log.warn("{}[exe insert][生成主键:{}]", random, ids);
            }else{
                if(rs.next()){
                    cnt ++;
                    Object id = rs.getObject("__ID0");
                    setPrimaryValue(data, id);
                    log.warn("{}[exe insert][生成主键:{}]", random, id);
                }
            }


        }finally {
            if(!DataSourceUtils.isConnectionTransactional(con, ds)){
                DataSourceUtils.releaseConnection(con, ds);
            }
        }
        return cnt;
    }

    /**
     * insert执行后 通过KeyHolder获取主键值赋值给data
     * @param random log标记
     * @param data data
     * @param keyholder  keyholder
     * @return boolean
     */
    @Override
    public boolean identity(String random, Object data, KeyHolder keyholder) {
        return false;
    }



    /* *****************************************************************************************************************
     * 													QUERY
     * -----------------------------------------------------------------------------------------------------------------
     * public String parseFinalQuery(Run run)
     * public StringBuilder buildConditionLike(StringBuilder builder, Compare compare)
     * public StringBuilder buildConditionIn(StringBuilder builder, Compare compare, Object value)
     * public List<Map<String,Object>> process(List<Map<String,Object>> list)
     *
     * protected void buildQueryRunContent(XMLRun run)
     * protected void buildQueryRunContent(TextRun run)
     * protected void buildQueryRunContent(TableRun run)
     ******************************************************************************************************************/


    /**
     * MATCH (n)  WHERE n.name='u1' RETURN n  ORDER BY n.age DESC SKIP 0 LIMIT 200
     * @param run  run
     * @return String
     */
    @Override
    public String parseFinalQuery(Run run) {
        if(!(run instanceof TableRun)){
            return run.getBaseQuery();
        }
        StringBuilder builder = new StringBuilder();
        RunPrepare prepare = run.getPrepare();
        builder.append(run.getBaseQuery());
        String cols = run.getQueryColumns();
        String alias = run.getPrepare().getAlias();
        OrderStore orders = run.getOrderStore();
        if(null != orders){
            builder.append(orders.getRunText(getDelimiterFr()+getDelimiterTo()));
        }
        builder.append(" RETURN ");
        List<String> columns = prepare.getColumns();
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
                        builder.append(alias);
                    }else{
                        SQLUtil.delimiter(builder, alias+"."+column, delimiterFr, delimiterTo);
                    }
                }
                if(i<size-1){
                    builder.append(",");
                }
            }
            builder.append(JDBCAdapter.BR);
        }else{
            // 全部查询
            builder.append(alias);
            builder.append(JDBCAdapter.BR);
        }
        builder.append(", ID(").append(alias).append(") AS __ID");
        PageNavi navi = run.getPageNavi();
        if(null != navi){
            int limit = navi.getLastRow() - navi.getFirstRow() + 1;
            if(limit < 0){
                limit = 0;
            }
            builder.append(" SKIP ").append(navi.getFirstRow()).append(" LIMIT ").append(limit);
        }
        String content = builder.toString();
        return content;
    }
    /**
     * 构造 LIKE 查询条件
     * MATCH (n:Dept) where n.name CONTAINS '财务' RETURN n
     * MATCH (n:Dept) where n.name STARTS WITH  '财' RETURN n
     * MATCH (n:Dept) where n.name END WITH  '财' RETURN n
     *
     * @param builder builder
     * @param compare compare
     * @param value value
     * @return StringBuilder
     */
    @Override
    public Object buildConditionLike(StringBuilder builder, Compare compare, Object value){
        if(compare == Compare.LIKE){
            builder.append(" CONTAINS ?");
        }else if(compare == Compare.LIKE_PREFIX || compare == Compare.START_WITH){
            builder.append(" START WITH ?");
        }else if(compare == Compare.LIKE_SUFFIX || compare == Compare.END_WITH){
            builder.append(" END WITH ?");
        }
        return value;
    }

    @Override
    public Object buildConditionFindInSet(StringBuilder builder, String column, Compare compare, Object value) {
        return null;
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
        if(compare== Compare.NOT_IN){
            builder.append(" NOT");
        }
        builder.append(" IN [");
        if(value instanceof Collection){
            Collection<Object> coll = (Collection)value;
            int size = coll.size();
            for(int i=0; i<size; i++){
                builder.append("?");
                if(i < size-1){
                    builder.append(",");
                }
            }
            builder.append("]");
        }else{
            builder.append("= ?");
        }
        return builder;
    }


    /**
     * JDBC执行结果处理
     * return e
     * 只有一个return项时执行
     * [e:{id:1,name:''},e:{id:2,name:''}]
     * 转换成
     * [,{id:1,name:''},{id:2,name:''}]
     * @param list JDBC执行结果
     * @return List
     */
    @Override
    public List<Map<String,Object>> process(List<Map<String,Object>> list){
        List<Map<String,Object>> result = list;
        if(null != list && !list.isEmpty()){
            Map<String,Object> map = list.get(0);
            Set<String> keys = map.keySet();
            String id_key = "__ID";
            boolean mapHashIdKey = BasicUtil.containsString(true, true, keys, "__ID");
            if((2 == keys.size() && keys.contains(id_key)
                    || keys.size() == 1
            )){
                String key = null;
                for(String k:keys){
                    if(!id_key.equalsIgnoreCase(k)){
                        key = k;
                        break;
                    }
                }
                Object chk = list.get(0).get(key);
                if(null != chk && chk instanceof Map) {
                    result = new ArrayList<>();
                    for (Map<String, Object> item : list) {
                        Map<String, Object> value = (Map<String, Object>)item.get(key);
                        if(mapHashIdKey){//MAP中有__ID
                            value.put("id", item.get(id_key));
                        }
                        result.add(value);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 生成基础查询主体
     * @param run run
     */
    protected void buildQueryRunContent(XMLRun run){
    }
    /**
     * 生成基础查询主体
     * @param run run
     */
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
                        if(var.getCompare()== Compare.IN){
                            // 多个值IN
                            String replaceSrc = ":"+var.getKey();
                            String replaceDst = "";
                            for(Object tmp:varValues){
                                run.addValues(var.getKey(),tmp);
                                replaceDst += " ?";
                            }
                            replaceDst = replaceDst.trim().replace(" ", ",");
                            result = result.replace(replaceSrc, replaceDst);
                        }else{
                            // 单个值
                            result = result.replace(":"+var.getKey(), "?");
                            run.addValues(var.getKey(), varValues.get(0));
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
                    run.addValues(var.getKey(), value);
                }
            }
        }

        builder.append(result);
        run.appendCondition();
        run.appendGroup();
        // appendOrderStore();
        run.checkValid();
    }
    /**
     * 生成基础查询主体
     * @param run run
     */
    protected void buildQueryRunContent(TableRun run){
        StringBuilder builder = run.getBuilder();
        RunPrepare prepare =  run.getPrepare();
        String alias = prepare.getAlias();
        if(BasicUtil.isEmpty(alias)){
            alias = "e";
            prepare.setAlias(alias);
        }
        builder.append("MATCH (").append(alias);
        String table = run.getTable();
        if(BasicUtil.isNotEmpty(table)) {
            builder.append(":");
            if (null != run.getSchema()) {
                SQLUtil.delimiter(builder, run.getSchema(), delimiterFr, delimiterTo).append(".");
            }
            SQLUtil.delimiter(builder, run.getTable(), delimiterFr, delimiterTo);
        }
        builder.append(") ");
        /*
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
        }*/
/*        List<Join> joins = prepare.getJoins();
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

       */
        builder.append(" WHERE 1=1 ");
        /*添加查询条件*/
        // appendConfigStore();
        run.appendCondition();
        run.appendGroup();
        run.appendOrderStore();
        run.checkValid();
    }


    /* *****************************************************************************************************************
     * 													COUNT
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
        String sql = run.getBaseQuery() + " RETURN COUNT("+run.getPrepare().getAlias()+") AS CNT";
        return sql;
    }


    /* *****************************************************************************************************************
     * 													EXISTS
     * -----------------------------------------------------------------------------------------------------------------
     * public String parseExists(Run run)
     ******************************************************************************************************************/
    @Override
    public String parseExists(Run run){
        String sql = run.getBaseQuery() + " RETURN COUNT("+run.getPrepare().getAlias()+") > 0  AS IS_EXISTS";
        return sql;
    }

    /* *****************************************************************************************************************
     * 													UPDATE
     * -----------------------------------------------------------------------------------------------------------------
     * protected Run buildUpdateRunFromObject(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns)
     * protected Run buildUpdateRunFromDataRow(String dest, DataRow row, ConfigStore configs, boolean checkPrimary, List<String> columns)
     * protected Run buildDeleteRunContent(TableRun run)
     * protected Run createDeleteRunSQLFromTable(String table, String key, Object values)
     * protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns)
     ******************************************************************************************************************/
    protected Run buildUpdateRunFromObject(String dest, Object obj, ConfigStore configs, boolean checkPrimary, List<String> columns){
        TableRun run = new TableRun(this,dest);
        StringBuilder builder = new StringBuilder();
        // List<Object> values = new ArrayList<Object>();
        List<String> keys = null;
        List<String> primaryKeys = null;
        if(null != columns && columns.size() >0 ){
            keys = columns;
        }else{
            if(EntityAdapterProxy.hasAdapter()){
                keys = EntityAdapterProxy.columns(obj.getClass(), false, true);
            }
        }
        if(EntityAdapterProxy.hasAdapter()){
            primaryKeys = EntityAdapterProxy.primaryKeys(obj.getClass());
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
                if(EntityAdapterProxy.hasAdapter()){
                    Field field = EntityAdapterProxy.field(obj.getClass(), key);
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
                    // values.add(value);
                    run.addValues(key, value);
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
                    if (EntityAdapterProxy.hasAdapter()) {
                        Field field = EntityAdapterProxy.field(obj.getClass(), pk);
                        // values.add(BeanUtil.getFieldValue(obj, field));
                        run.addValues(pk, BeanUtil.getFieldValue(obj, field));
                    } else {
                        // values.add(BeanUtil.getFieldValue(obj, pk));
                        run.addValues(pk, BeanUtil.getFieldValue(obj, pk));
                    }
                }
            }else{
                run.setConfigStore(configs);
                run.init();
                run.appendCondition();
            }
            // run.addValues(values);
        }
        run.setUpdateColumns(updateColumns);
        run.setBuilder(builder);

        return run;
    }
    protected Run buildUpdateRunFromDataRow(String dest, DataRow row, ConfigStore configs, boolean checkPrimary, List<String> columns){
        TableRun run = new TableRun(this,dest);
        StringBuilder builder = new StringBuilder();
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
                    // values.add(value);
                    run.addValues(key, value);
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
                    // values.add(row.get(pk));
                    run.addValues(pk, row.get(pk));
                }
            }else{
                run.setConfigStore(configs);
                run.init();
                run.appendCondition();
            }
            // run.addValues(values);
        }
        run.setUpdateColumns(updateColumns);
        run.setBuilder(builder);

        return run;
    }

    protected Run buildDeleteRunContent(TableRun run){
        RunPrepare prepare =   run.getPrepare();
        StringBuilder builder = run.getBuilder();
        builder.append("DELETE FROM ");
        if(null != run.getSchema()){
            SQLUtil.delimiter(builder, run.getSchema(), delimiterFr, delimiterTo).append(".");
        }

        SQLUtil.delimiter(builder, run.getTable(), delimiterFr, delimiterTo);
        builder.append(JDBCAdapter.BR);
        if(BasicUtil.isNotEmpty(prepare.getAlias())){
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
        run.addValues(key, values);
        run.setBuilder(builder);

        return run;
    }

    protected Run createDeleteRunSQLFromEntity(String dest, Object obj, String ... columns){
        TableRun run = new TableRun(this,dest);
        StringBuilder builder = new StringBuilder();
        builder.append("MATCH (d");
        String table = parseTable(dest);
        if(BasicUtil.isNotEmpty(table)){
            builder.append(":").append(table);
        }
        builder.append(")");
        builder.append(" WHERE ");
        List<String> keys = new ArrayList<>();
        if(null != columns && columns.length>0){
            for(String col:columns){
                keys.add(col);
            }
        }else{
            if(obj instanceof DataRow){
                keys = ((DataRow)obj).getPrimaryKeys();
            }else{
                if(EntityAdapterProxy.hasAdapter()){
                    keys = EntityAdapterProxy.primaryKeys(obj.getClass());
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

                SQLUtil.delimiter(builder, "d."+key, getDelimiterFr(), getDelimiterTo()).append(" = ? ");
                Object value = null;
                if(obj instanceof DataRow){
                    value = ((DataRow)obj).get(key);
                }else{
                    if(EntityAdapterProxy.hasAdapter()){
                        value = BeanUtil.getFieldValue(obj, EntityAdapterProxy.field(obj.getClass(), key));
                    }else{
                        value = BeanUtil.getFieldValue(obj, key);
                    }
                }
                run.addValues(key,value);
            }
        }else{
            throw new SQLUpdateException("删除异常:删除条件为空,delete方法不支持删除整表操作.");
        }
        builder.append(" DELETE d");
        run.setBuilder(builder);

        return run;
    }

    /* *****************************************************************************************************************
     * 													COMMON
     * -----------------------------------------------------------------------------------------------------------------
     *  public String concat(String... args)
     ******************************************************************************************************************/
    @Override
    public String concat(String... args) {
        return null;
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
