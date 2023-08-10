package org.anyline.data.mongo.adapter;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.init.DefaultDriverAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.mongo.runtime.MongoRuntime;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Condition;
import org.anyline.data.prepare.ConditionChain;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.AutoCondition;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.RunValue;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.exception.SQLException;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.LogUtil;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.util.*;

@Repository("anyline.data.adapter.mongo")
public class MongoAdapter extends DefaultDriverAdapter implements DriverAdapter {

    @Autowired(required = false)
    protected DMListener dmListener;
    @Autowired(required = false)
    protected DDListener ddListener;

    public DMListener getListener() {
        return dmListener;
    }

    @Autowired(required=false)
    public void setListener(DMListener listener) {
        this.dmListener = listener;
    }

    @Override
    public DatabaseType type() {
        return DatabaseType.MongoDB;
    }

    @Override
    public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        return false;
    }

    @Override
    public int update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns) {
        return 0;
    }

    /**
     * insert [入口]<br/>
     * 执行完成后会补齐自增主键值
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表
     * @param data 数据
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 列
     * @return 影响行数
     */
    @Override
    public int insert(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns) {
        return 0;
    }

    /**
     * 根据entity创建 INSERT RunPrepare
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表
     * @param obj 数据
     * @param checkPrimary 是否需要检查重复主键,默认不检查
     * @param columns 需要插入的列
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    protected Run createInsertRun(DataRuntime runtime, String dest, Object obj, boolean checkPrimary, List<String> columns){
        Run run = new TableRun(runtime, dest);
        if(BasicUtil.isEmpty(dest)){
            throw new SQLException("未指定表");
        }

        PrimaryGenerator generator = checkPrimaryGenerator(type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));

        int from = 1;
        DataRow row = null;
        if(obj instanceof Map){
            obj = new DataRow((Map)obj);
        }
        if(obj instanceof DataRow){
            row = (DataRow)obj;
            if(row.hasPrimaryKeys() && null != generator){
                generator.create(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
            }
        }else{
            from = 2;
            boolean create = EntityAdapterProxy.createPrimaryValue(obj, columns);
            LinkedHashMap<String,Column> pks = EntityAdapterProxy.primaryKeys(obj.getClass());
            if(!create && null != generator){
                generator.create(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pks, null);
            }
        }
        run.setFrom(from);

        run.setValue(obj);

        return run;
    }

    /**
     * 执行 insert
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param data entity|DataRow|DataSet
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @param pks pks
     * @return int 影响行数
     * @throws Exception 异常
     */
    @Override
    public int insert(DataRuntime runtime, String random, Object data, Run run, String[] pks) {
        int cnt = 0;
        Object value = run.getValue();
        if(null == value || !run.isValid()){
            if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
                log.warn("[valid:false][不具备执行条件][dest:"+run.getTable()+"]");
            }
            return -1;
        }
        MongoRuntime rt = (MongoRuntime) runtime;
        MongoDatabase database = rt.getDatabase();
        long fr = System.currentTimeMillis();
        try {
            MongoCollection cons = null;
            if(value instanceof List){
                List list = (List) value;
                cons = database.getCollection(run.getTable(), list.get(0).getClass());
                cons.insertMany(list);
            }else if(value instanceof DataSet){
                cons = database.getCollection(run.getTable(), DataRow.class);
                cons.insertMany(((DataSet)value).getRows());
            }else if(value instanceof EntitySet){
                List<Object> datas = ((EntitySet)value).getDatas();
                cons = database.getCollection(run.getTable(),datas.get(0).getClass());
                cons.insertMany(datas);
            }else if(value instanceof Collection){
                Collection items = (Collection) value;
                List<Object> list = new ArrayList<>();
                for(Object item:items){
                    list.add(item);
                }
                cons = database.getCollection(run.getTable(), list.get(0).getClass());
                cons.insertMany(list);
            }else{
                cons = database.getCollection(run.getTable(), value.getClass());
                cons.insertOne(value);
            }
        }catch(Exception e){
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }
            if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
                SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
                throw ex;
            }else{
                if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
                    log.error("{}[{}][table:\n{}\n]\n[param:{}]", random, LogUtil.format("插入异常:", 33)+e, run.getTable());
                }
            }
        }
        return cnt;
    }
    /**
     * 创建查询SQL
     * @param prepare  构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions 简单过滤条件
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
        Run run = null;
        if(prepare instanceof TablePrepare){
            run = new TableRun(runtime, prepare.getTable());
        }else{
            throw new RuntimeException("不支持查询的类型");
        }
        if(null != run){
            run.setRuntime(runtime);
            //如果是text类型 将解析文本并抽取出变量
            run.setPrepare(prepare);
            run.setConfigStore(configs);
            run.addCondition(conditions);
            if(run.checkValid()) {
                //为变量赋值
                run.init();
                //构造最终的查询SQL
                fillQueryContent(runtime, run);
            }
        }
        return run;
    }

    @Override
    protected void fillQueryContent(DataRuntime runtime, TableRun run){
        Bson bson = Filters.empty();
        ConditionChain chain = run.getConditionChain();
        bson = parseCondition(bson, chain);
        run.setFilter(bson);
    }
    private Bson parseCondition(Bson bson, Condition condition){
        if(condition instanceof ConditionChain){
            ConditionChain chain = (ConditionChain)condition;
            bson = parseCondition(bson, chain);
        }else{
            if(condition instanceof AutoCondition){
                AutoCondition auto = (AutoCondition)condition;
                List<RunValue> values = condition.getRunValues();
                String column = condition.getId();
                String join = condition.getJoin();
                Bson create = bson(auto.getCompare(), column, values);
                if(null != create) {
                    if ("or".equalsIgnoreCase(join)) {
                        bson = Filters.or(create);
                    } else {
                        bson = Filters.and(create);
                    }
                }
            }
        }
        return bson;
    }

    private Bson parseCondition(Bson bson, ConditionChain chain){
        String join = chain.getJoin();
        Bson child = Filters.empty();
        List<Condition> conditions = chain.getConditions();
        for(Condition con:conditions){
            child = parseCondition(child, con);
        }
        if("or".equalsIgnoreCase(join)){
            bson = Filters.or(bson, child);
        }else{
            bson = Filters.and(bson, child);
        }
        return bson;
    }
    private Bson bson(Compare compare, String column, List<RunValue> values){
        Bson bson = null;
        if(null != values && !values.isEmpty()) {
            Object value = null;
            boolean multiple = compare.isMultipleValue();
            if (multiple) {
                List<Object> list = new ArrayList<>();
                for (RunValue rv : values) {
                    list.add(rv.getValue());
                }
                value = list;
            } else {
                value = values.get(0).getValue();
            }
            if(compare == Compare.EQUAL){
                bson = Filters.eq(column, value);
            }else if(compare == Compare.GREAT){
                bson = Filters.gt(column, value);
            }
        }
        return bson;
    }
    @Override
    public DataSet select(DataRuntime runtime, String random, boolean system, String table, Run run) {
        long fr = System.currentTimeMillis();
        if(null == random){
            random = random(runtime);
        }
        DataSet set = new DataSet();
        //根据这一步中的JDBC结果集检测类型不准确,如:实际POINT 返回 GEOMETRY 如果要求准确 需要开启到自动检测
        //在DataRow中 如果检测到准确类型 JSON XML POINT 等 返回相应的类型,不返回byte[]（所以需要开启自动检测）
        //Entity中 JSON XML POINT 等根据属性类型返回相应的类型（所以不需要开启自动检测）
        // LinkedHashMap<String,Column> columns = new LinkedHashMap<>();

        if(!system && ThreadConfig.check(runtime.getKey()).IS_AUTO_CHECK_METADATA() && null != table){
            // columns = columns(runtime, false, new Table(null, null, table), null);
        }
        try{
            MongoRuntime rt = (MongoRuntime) runtime;
            MongoDatabase database = rt.getDatabase();
            Bson bson = (Bson)run.getFilter();
            if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
                log.info("{}[collection:{}][filter:{}]", random, run.getTable(), bson);
            }
            FindIterable<DataRow> rows = database.getCollection(run.getTable(), DataRow.class).find(bson);
            PageNavi navi = run.getPageNavi();
            if(null != navi){
                rows.skip((int)navi.getFirstRow()).limit(navi.getPageRows());
            }
            for(DataRow row:rows){
                set.add(row);
            }
            if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
                log.info("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - fr, set.size());
            }
        }catch(Exception e){
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                e.printStackTrace();
            }
            if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
                SQLQueryException ex = new SQLQueryException("query异常:"+e,e);
                throw ex;
            }else{
                if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
                    log.error("{}[{}][collection:\n{}\n]", random, LogUtil.format("查询异常:", 33)+e.toString(), run.getTable());
                }
            }
        }
        return set;
    }

    @Override
    public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        return 0;
    }

    @Override
    public long count(DataRuntime runtime, String random, Run run) {
        return 0;
    }

    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        return null;
    }

    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, Run run) {
        return null;
    }

    @Override
    public Map<String, Object> map(DataRuntime runtime, String random, Run run) {
        return null;
    }

    @Override
    public DataRow sequence(DataRuntime runtime, String random, boolean next, String... names) {
        return null;
    }

    @Override
    public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        return 0;
    }


    @Override
    public int update(DataRuntime runtime, String random, String dest, Object data, Run run) {
        return 0;
    }

    @Override
    public long execute(DataRuntime runtime, String random, Run run) {
        return 0;
    }

    @Override
    public boolean execute(DataRuntime runtime, String random, Procedure procedure) {
        return false;
    }

    @Override
    public <T> int deletes(DataRuntime runtime, String random, String table, String key, Collection<T> values) {
        return 0;
    }

    @Override
    public int delete(DataRuntime runtime, String random, String dest, Object obj, String... columns) {
        return 0;
    }

    @Override
    public int delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions) {
        return 0;
    }

    @Override
    public int truncate(DataRuntime runtime, String random, String table) {
        return 0;
    }

    @Override
    public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi) {
        return null;
    }

    @Override
    public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String... conditions) {
        return null;
    }

    @Override
    public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) {
        return null;
    }


    @Override
    public int insert(DataRuntime runtime, String random, Object data, Run run, String[] pks, boolean simple) {
        return 0;
    }

    @Override
    public int save(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns) {
        return 0;
    }

    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, boolean checkPrimary, LinkedHashMap<String, Column> columns) {
        return null;
    }

    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, boolean checkPrimary, LinkedHashMap<String, Column> columns) {
        return null;
    }

    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run) {
        return null;
    }

    @Override
    public Object createConditionLike(DataRuntime runtime, StringBuilder builder, Compare compare, Object value) {
        return null;
    }

    @Override
    public Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value) {
        return null;
    }

    @Override
    public StringBuilder createConditionIn(DataRuntime runtime, StringBuilder builder, Compare compare, Object value) {
        return null;
    }

    @Override
    public Run buildDeleteRunFromTable(DataRuntime runtime, String table, String key, Object values) {
        return null;
    }

    @Override
    public Run buildDeleteRunFromEntity(DataRuntime runtime, String dest, Object obj, String... columns) {
        return null;
    }

    @Override
    public void checkSchema(DataRuntime runtime, DataSource dataSource, Table table) {

    }

    @Override
    public void checkSchema(DataRuntime runtime, Connection con, Table table) {

    }

    @Override
    public void checkSchema(DataRuntime runtime, Table table) {

    }

    @Override
    public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random) {
        return null;
    }

    @Override
    public Database database(DataRuntime runtime, String random, String name) {
        return null;
    }

    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types) {
        return null;
    }

    @Override
    public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init) {
        return null;
    }

    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types) {
        return null;
    }

    @Override
    public List<String> ddl(DataRuntime runtime, String random, View view) {
        return null;
    }

    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types) {
        return null;
    }

    @Override
    public List<String> ddl(DataRuntime runtime, String random, MasterTable table) {
        return null;
    }

    @Override
    public <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String name) {
        return null;
    }

    @Override
    public List<String> ddl(DataRuntime runtime, String random, PartitionTable table) {
        return null;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary) {
        return null;
    }

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception {
        return null;
    }


    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception {
        return null;
    }


    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean create, Table table, LinkedHashMap<String, T> columns, List<Run> runs) {
        return null;
    }

    @Override
    public Column column(DataRuntime runtime, Column column, ResultSetMetaData rsm, int index) {
        return null;
    }

    @Override
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table) {
        return null;
    }

    @Override
    public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table) {
        return null;
    }

    @Override
    public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table) {
        return null;
    }

    @Override
    public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String name) {
        return null;
    }

    @Override
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events) {
        return null;
    }

    @Override
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String name) {
        return null;
    }

    @Override
    public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, boolean recover, String catalog, String schema, String name) {
        return null;
    }

    @Override
    public String concat(DataRuntime runtime, String... args) {
        return null;
    }
}
