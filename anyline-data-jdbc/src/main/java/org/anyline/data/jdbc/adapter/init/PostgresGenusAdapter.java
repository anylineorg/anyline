package org.anyline.data.jdbc.adapter.init;

import org.anyline.data.jdbc.adapter.init.alias.PostgresGenusTypeMetadataAlias;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.metadata.*;
import org.anyline.metadata.adapter.MetadataAdapterHolder;
import org.anyline.metadata.adapter.PrimaryMetadataAdapter;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.RegularUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

public abstract class PostgresGenusAdapter extends AbstractJDBCAdapter {
    public PostgresGenusAdapter(){
        super();
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.CHAR, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 0, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.TEXT, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.BOOLEAN, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1,1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.BYTES, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 0, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.BLOB, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1,1,1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.INT, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", "NUMERIC_PRECISION", null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.FLOAT, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", "NUMERIC_PRECISION", "NUMERIC_SCALE", 1, 0, 0));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.DATE, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.TIME, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.DATETIME, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.TIMESTAMP, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.COLLECTION, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.GEOMETRY, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));
        MetadataAdapterHolder.reg(type(), TypeMetadata.CATEGORY.OTHER, new TypeMetadata.Config("CHARACTER_MAXIMUM_LENGTH", null, null, 1, 1, 1));

        for(PostgresGenusTypeMetadataAlias alias: PostgresGenusTypeMetadataAlias.values()){
            reg(alias);
        }
    }
    @Override
    public boolean supportCatalog() {
        return super.supportSchema();
    }

    @Override
    public boolean supportSchema() {
        return super.supportSchema();
    }

    private static Map<Type, String> types = new HashMap<>();
    static {
        types.put(Table.TYPE.NORMAL, "BASE TABLE");
        types.put(Table.TYPE.VIEW, "VIEW");
        types.put(View.TYPE.NORMAL, "VIEW");
        types.put(BaseMetadata.TYPE.TABLE, "BASE TABLE");
        types.put(BaseMetadata.TYPE.VIEW, "VIEW");
    }
    @Override
    public String name(Type type){
        return types.get(type);
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
     * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch)
     * public String batchInsertSeparator()
     * public boolean supportInsertPlaceholder()
     * protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns)
     * protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns)
     * public String generatedKey()
     * [命令执行]
     * long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks);
     * long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run);
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
    public long insert(DataRuntime runtime, String random, int batch, Table dest, Object data, ConfigStore configs, List<String> columns){
        return super.insert(runtime, random, batch, dest, data, configs, columns);
    }
    /**
     * insert [命令合成]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 需要插入的数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildInsertRun(DataRuntime runtime, int batch, Table dest, Object obj, ConfigStore configs, List<String> columns){
        return super.buildInsertRun(runtime, batch, dest, obj, configs, columns);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param set 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, DataSet set, ConfigStore configs, LinkedHashMap<String, Column> columns){
        super.fillInsertContent(runtime, run, dest, set, configs, columns);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 填充inset命令内容(创建批量INSERT RunPrepare)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 需要插入的数据集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     */
    @Override
    public void fillInsertContent(DataRuntime runtime, Run run, Table dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns){
        super.fillInsertContent(runtime, run, dest, list, configs, columns);
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
    public LinkedHashMap<String, Column> confirmInsertColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns, boolean batch){
        return super.confirmInsertColumns(runtime, dest, obj, configs, columns, batch);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 批量插入数据时, 多行数据之间分隔符
     * @return String
     */
    @Override
    public String batchInsertSeparator(){
        return ",";
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 插入数据时是否支持占位符
     * @return boolean
     */
    @Override
    public boolean supportInsertPlaceholder(){
        return true;
    }
    /**
     * insert [命令合成-子流程]<br/>
     * 设置主键值
     * @param obj obj
     * @param value value
     */
    @Override
    protected void setPrimaryValue(Object obj, Object value){
        super.setPrimaryValue(obj, value);
    }
    /**
     * insert [命令合成-子流程]<br/>
     * 根据entity创建 INSERT RunPrepare由buildInsertRun调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj 数据
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    protected Run createInsertRun(DataRuntime runtime, Table dest, Object obj, ConfigStore configs, List<String> columns){
        return super.createInsertRun(runtime, dest, obj, configs, columns);
    }

    /**
     * insert [命令合成-子流程]<br/>
     * 根据collection创建 INSERT RunPrepare由buildInsertRun调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param list 对象集合
     * @param columns 需要插入的列，如果不指定则根据data或configs获取注意会受到ConfigTable中是否插入更新空值的几个配置项影响
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    protected Run createInsertRunFromCollection(DataRuntime runtime, int batch, Table dest, Collection list, ConfigStore configs, List<String> columns){
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
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @param pks 需要返回的主键
     * @return 影响行数
     */
    @Override
    public long insert(DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String[] pks){
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
     * Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns)
     * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns)
     * LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns)
     * [命令执行]
     * long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run)
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
    public long update(DataRuntime runtime, String random, int batch, String dest, Object data, ConfigStore configs, List<String> columns){
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
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildUpdateRun(DataRuntime runtime, int batch, String dest, Object obj, ConfigStore configs, List<String> columns){
        return super.buildUpdateRun(runtime, batch, dest, obj, configs, columns);
    }
    @Override
    public Run buildUpdateRunFromEntity(DataRuntime runtime, String dest, Object obj, ConfigStore configs, LinkedHashMap<String, Column> columns){
        return super.buildUpdateRunFromEntity(runtime, dest, obj, configs, columns);
    }
    @Override
    public Run buildUpdateRunFromDataRow(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, LinkedHashMap<String, Column> columns){
        return super.buildUpdateRunFromDataRow(runtime, dest, row, configs, columns);
    }
    @Override
    public Run buildUpdateRunFromCollection(DataRuntime runtime, int batch, String dest, Collection list, ConfigStore configs, LinkedHashMap<String, Column> columns){
        return super.buildUpdateRunFromCollection(runtime, batch, dest, list, configs, columns);
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
    public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, DataRow row, ConfigStore configs, List<String> columns){
        return super.confirmUpdateColumns(runtime, dest, row, configs, columns);
    }
    @Override
    public LinkedHashMap<String, Column> confirmUpdateColumns(DataRuntime runtime, String dest, Object obj, ConfigStore configs, List<String> columns){
        return super.confirmUpdateColumns(runtime, dest, obj, configs, columns);
    }
    /**
     * update [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param data 数据
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return 影响行数
     */
    @Override
    public long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, Run run){
        return super.update(runtime, random, dest, data, configs, run);
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
    public long save(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
        return super.save(runtime, random, dest, data, configs, columns);
    }

    @Override
    protected long saveCollection(DataRuntime runtime, String random, Table dest, Collection<?> data, ConfigStore configs, List<String> columns){
        return super.saveCollection(runtime, random, dest, data, configs, columns);
    }
    @Override
    protected long saveObject(DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, List<String> columns){
        return super.saveObject(runtime, random, dest, data, configs, columns);
    }
    @Override
    protected Boolean checkOverride(Object obj){
        return super.checkOverride(obj);
    }
    @Override
    protected Map<String, Object> checkPv(Object obj){
        return super.checkPv(obj);
    }

    /**
     * 是否是可以接收数组类型的值
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @param key key
     * @return boolean
     */
    @Override
    protected boolean isMultipleValue(DataRuntime runtime, TableRun run, String key){
        return super.isMultipleValue(runtime, run, key);
    }
    @Override
    protected boolean isMultipleValue(Column column){
        return super.isMultipleValue(column);
    }
    /**
     * 过滤掉表结构中不存在的列
     * @param table 表
     * @param columns columns
     * @return List
     */
    @Override
    public LinkedHashMap<String, Column> checkMetadata(DataRuntime runtime, Table table, ConfigStore configs, LinkedHashMap<String, Column> columns){
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
     * void fillQueryContent(DataRuntime runtime, Run run)
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
    public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
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
    public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi){
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
    public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions){
        return super.selects(runtime, random, prepare, clazz, configs, conditions);
    }

    /**
     * select [命令执行-子流程]<br/>
     * DataRow转换成Entity
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param clazz entity class
     * @param table table
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return EntitySet
     * @param <T> entity.class
     *
     */
    @Override
    protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, Table table, ConfigStore configs, Run run){
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
     * @param conditions  简单过滤条件
     * @return maps 返回map集合
     */
    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return super.maps(runtime, random, prepare, configs, conditions);
    }
    /**
     * select[命令合成]<br/> 最终可执行命令<br/>
     * 创建查询SQL
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 过滤条件及相关配置
     * @param conditions  简单过滤条件
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildQueryRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return super.buildQueryRun(runtime, prepare, configs, conditions);
    }

    /**
     * 查询序列cur 或 next value
     * @param next  是否生成返回下一个序列 false:cur true:next
     * @param names 序列名
     * @return String
     */
    @Override
    public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names){
        List<Run> runs = new ArrayList<>();
        String key = "CURRVAL";
        if(next){
            key = "NEXTVAL";
        }
        if(null != names && names.length>0) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            builder.append("SELECT ");
            boolean first = true;
            for (String name : names) {
                if(!first){
                    builder.append(",");
                }
                first = false;
                builder.append(key).append("('").append(name).append("') AS ").append(name);
            }
        }
        return runs;
    }

    /**
     * select[命令合成-子流程] <br/>
     * 构造查询主体
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     */
    @Override
    public void fillQueryContent(DataRuntime runtime, Run run){
        super.fillQueryContent(runtime, run);
    }
    @Override
    protected void fillQueryContent(DataRuntime runtime, XMLRun run){
        super.fillQueryContent(runtime, run);
    }
    @Override
    protected void fillQueryContent(DataRuntime runtime, TextRun run){
        super.fillQueryContent(runtime, run);
    }
    @Override
    protected void fillQueryContent(DataRuntime runtime, TableRun run){
        super.fillQueryContent(runtime, run);
    }
    /**
     * select[命令合成-子流程] <br/>
     * 合成最终 select 命令 包含分页 排序
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return String
     */
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run) {
        return super.pageLimitOffset(runtime, run);
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
    public Object createConditionFindInSet(DataRuntime runtime, StringBuilder builder, String column, Compare compare, Object value, boolean placeholder) {
        return super.createConditionFindInSet(runtime, builder, column, compare, value, placeholder);
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
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
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
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return maps
     */
    @Override
    public List<Map<String, Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run){
        return super.maps(runtime, random, configs, run);
    }
    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
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
    public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names){
        return super.sequence(runtime, random, next, names);
    }

    /**
     * select [命令执行-子流程]<br/>
     * JDBC执行完成后的结果处理
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param list JDBC执行返回的结果集
     * @return  maps
     */
    @Override
    public List<Map<String, Object>> process(DataRuntime runtime, List<Map<String, Object>> list){
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
    public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return super.count(runtime, random, prepare, configs, conditions);
    }
    /**
     * count [命令合成]<br/>
     * 合成最终 select count 命令
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return String
     */
    @Override
    public String mergeFinalTotal(DataRuntime runtime, Run run){
        return super.mergeFinalTotal(runtime, run);
    }

    /**
     * count [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return long
     */
    @Override
    public long count(DataRuntime runtime, String random, Run run){
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
    public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return super.exists(runtime, random, prepare, configs, conditions);
    }
    @Override
    public String mergeFinalExists(DataRuntime runtime, Run run){
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
    public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions)  {
        return super.execute(runtime, random, prepare, configs, conditions);
    }

    @Override
    public long execute(DataRuntime runtime, String random, int batch, ConfigStore configs, RunPrepare prepare, Collection<Object> values){
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
    public boolean execute(DataRuntime runtime, String random, Procedure procedure){
        return super.execute(runtime, random, procedure);
    }
    /**
     * execute [命令合成]<br/>
     * 创建执行SQL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param configs 查询条件及相关设置
     * @param conditions  简单过滤条件
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildExecuteRun(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
        return super.buildExecuteRun(runtime, prepare, configs, conditions);
    }
    @Override
    protected void fillExecuteContent(DataRuntime runtime, XMLRun run){
        super.fillExecuteContent(runtime, run);
    }
    @Override
    protected void fillExecuteContent(DataRuntime runtime, TextRun run){
        super.fillExecuteContent(runtime, run);
    }
    @Override
    protected void fillExecuteContent(DataRuntime runtime, TableRun run){
        super.fillExecuteContent(runtime, run);
    }

    /**
     * execute [命令合成-子流程]<br/>
     * 填充 execute 命令内容
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     */
    @Override
    public void fillExecuteContent(DataRuntime runtime, Run run){
        super.fillExecuteContent(runtime, run);
    }
    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
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
     * Run buildDeleteRun(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns)
     * Run buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String column, Object values)
     * List<Run> buildTruncateRun(DataRuntime runtime, String table)
     * Run buildDeleteRunFromTable(DataRuntime runtime, int batch, String table, ConfigStore configs,String column, Object values)
     * Run buildDeleteRunFromEntity(DataRuntime runtime, String table, ConfigStore configs, Object obj, String ... columns)
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
    public <T> long deletes(DataRuntime runtime, String random, int batch, String table, ConfigStore configs, String key, Collection<T> values){
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
    public long delete(DataRuntime runtime, String random, String dest, ConfigStore configs, Object obj, String... columns){
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
    public long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions){
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
    public long truncate(DataRuntime runtime, String random, Table table){
        return super.truncate(runtime, random, table);
    }

    /**
     * delete[命令合成]<br/>
     * 合成 where k1 = v1 and k2 = v2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param dest 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildDeleteRun(DataRuntime runtime, Table dest, ConfigStore configs, Object obj, String ... columns){
        return super.buildDeleteRun(runtime, dest, configs, obj, columns);
    }

    /**
     * delete[命令合成]<br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param key 根据属性解析出列
     * @param values values
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildDeleteRun(DataRuntime runtime, int batch, String table, ConfigStore configs, String key, Object values){
        return super.buildDeleteRun(runtime, batch, table, configs, key, values);
    }

    @Override
    public List<Run> buildTruncateRun(DataRuntime runtime, String table){
        return super.buildTruncateRun(runtime, table);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 合成 where column in (values)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源
     * @param column 列
     * @param values values
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildDeleteRunFromTable(DataRuntime runtime, int batch, Table table, ConfigStore configs, String column, Object values) {
        return super.buildDeleteRunFromTable(runtime, batch, table, configs, column, values);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 合成 where k1 = v1 and k2 = v2
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表 如果不提供表名则根据data解析, 表名可以事实前缀&lt;数据源名&gt;表示切换数据源 如果为空 可以根据obj解析
     * @param obj entity或DataRow
     * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
     * @return Run 最终执行命令 如果是JDBC类型库 会包含 SQL 与 参数值
     */
    @Override
    public Run buildDeleteRunFromEntity(DataRuntime runtime, Table table, ConfigStore configs, Object obj, String... columns) {
        return super.buildDeleteRunFromEntity(runtime, table, configs, obj, columns);
    }

    /**
     * delete[命令合成-子流程]<br/>
     * 构造查询主体 拼接where group等(不含分页 ORDER)
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     */
    @Override
    public void fillDeleteRunContent(DataRuntime runtime, Run run){
        super.fillDeleteRunContent(runtime, run);
    }

    /**
     * delete[命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param configs 查询条件及相关设置
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return 影响行数
     */
    @Override
    public long delete(DataRuntime runtime, String random, ConfigStore configs, Run run){
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
     * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
     * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
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
     * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set)
     * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set)
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
     ******************************************************************************************************************/

    /**
     * database[调用入口]<br/>
     * 当前数据库
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return Database
     */
    @Override
    public Database database(DataRuntime runtime, String random){
        Catalog catalog = catalog(runtime, random);
        if(null != catalog){
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
    public String product(DataRuntime runtime, String random){
        return super.product(runtime, random);
    }
    /**
     * database[调用入口]<br/>
     * 当前数据源 数据库类型
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @return String
     */
    public String version(DataRuntime runtime, String random){
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
    public List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name){
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
    public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name){
        return super.databases(runtime, random, name);
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
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT * FROM PG_DATABASE WHERE DATISTEMPLATE=FALSE");
        if(BasicUtil.isNotEmpty(name)){
            builder.append(" AND datname = '").append(name).append("'");
        }
        return runs;
    }
    /**
     * database[结果集封装]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param databases 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception Exception
     */
    @Override
    public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set) throws Exception {
        if(null == databases){
            databases = new LinkedHashMap<>();
        }
        for(DataRow row:set){
            Database database = new Database();
            database.setName(row.getString("DATNAME"));
            databases.put(database.getName().toUpperCase(), database);
        }
        return databases;
    }
    @Override
    public List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set) throws Exception {
        return super.databases(runtime, index, create, databases, set);
    }
	/**
	 * database[结果集封装]<br/>
	 * 当前database 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
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
	 * @param create 上一步没有查到的, 这一步是否需要新创建
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
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @param set 查询结果集
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, int index, boolean create, String product, DataSet set){
		return super.product(runtime, index, create, product, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 product
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param product 上一步查询结果
	 * @return product
	 * @throws Exception 异常
	 */
	@Override
	public String product(DataRuntime runtime, boolean create, String product){
		return super.product(runtime, create, product);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据查询结果集构造 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @param set 查询结果集
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, int index, boolean create, String version, DataSet set){
		return super.version(runtime, index, create, version, set);
	}

	/**
	 * database[结果集封装]<br/>
	 * 根据JDBC内置接口 version
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param version 上一步查询结果
	 * @return version
	 * @throws Exception 异常
	 */
	@Override
	public String version(DataRuntime runtime, boolean create, String version){
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
     * List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set)
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
     * List<Catalog> catalogs(DataRuntime runtime, boolean create, List<Catalog> catalogs, DataSet set)
     * LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set)
     * Catalog catalog(DataRuntime runtime, int index, boolean create, DataSet set)
     ******************************************************************************************************************/
    /**
     * catalog[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, String random, String name){
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
    public List<Catalog> catalogs(DataRuntime runtime, String random, boolean greedy, String name){
        return super.catalogs(runtime, random, greedy, name);
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
    public List<Run> buildQueryCatalogsRun(DataRuntime runtime, boolean greedy, String name) throws Exception {
        return super.buildQueryCatalogsRun(runtime, greedy, name);
    }
    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果集构造 Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public LinkedHashMap<String, Catalog> catalogs(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Catalog> catalogs, DataSet set) throws Exception {
        return super.catalogs(runtime, index, create, catalogs, set);
    }
    /**
     * catalog[结果集封装]<br/>
     * 根据查询结果集构造 Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public List<Catalog> catalogs(DataRuntime runtime, int index, boolean create, List<Catalog> catalogs, DataSet set) throws Exception {
        return super.catalogs(runtime, index, create, catalogs, set);
    }
	/**
     * catalog[结果集封装]<br/>
     * 根据驱动内置接口补充 catalog
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @return databases
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
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param catalogs 上一步查询结果
     * @return databases
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
	 * @param create 上一步没有查到的, 这一步是否需要新创建
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
	 * @param create 上一步没有查到的, 这一步是否需要新创建
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
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, String random, String name)
	 * List<Database> databases(DataRuntime runtime, String random, boolean greedy, String name)
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
	 * LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, DataSet set)
	 * List<Database> databases(DataRuntime runtime, int index, boolean create, List<Database> databases, DataSet set)
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
	 ******************************************************************************************************************/
    /**
     * schema[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param catalog catalog
     * @param name 名称统配符或正则
     * @return LinkedHashMap
     */
    @Override
    public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, String random, Catalog catalog, String name){
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
    public List<Schema> schemas(DataRuntime runtime, String random, boolean greedy, Catalog catalog, String name){
        return super.schemas(runtime, random, greedy, catalog, name);
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
     * 根据查询结果集构造 Database
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDatabaseRun 返回顺序
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param schemas 上一步查询结果
     * @param set 查询结果集
     * @return databases
     * @throws Exception 异常
     */
    @Override
    public LinkedHashMap<String, Schema> schemas(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Schema> schemas, DataSet set) throws Exception {
        return super.schemas(runtime, index, create, schemas, set);
    }
    @Override
    public List<Schema> schemas(DataRuntime runtime, int index, boolean create, List<Schema> schemas, DataSet set) throws Exception {
        return super.schemas(runtime, index, create, schemas, set);
    }

	/**
	 * schema[结果集封装]<br/>
	 * 当前schema 根据查询结果集
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param index 第几条SQL 对照 buildQuerySchemaRun 返回顺序
	 * @param create 上一步没有查到的, 这一步是否需要新创建
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
	 * @param create 上一步没有查到的, 这一步是否需要新创建
	 * @param schema 上一步查询结果
	 * @return schema
	 * @throws Exception 异常
	 */
	@Override
	public Schema schema(DataRuntime runtime, boolean create, Schema schema) throws Exception {
		return super.schema(runtime, create, schema);
	}

    /* *****************************************************************************************************************
     * 													table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, boolean struct)
     * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, String types, boolean struct)
     * [命令合成]
     * List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
     * List<Run> buildQueryTablesCommentRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
     * <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set)
     * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types)
     * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
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
     * @param types  BaseMetadata.TYPE.
     * @param struct 是否查询表结构
     * @return List
     * @param <T> Table
     */
    @Override
    public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types, int struct){
        return super.tables(runtime, random, greedy, catalog, schema, pattern, types, struct);
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
    protected void tableMap(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema){
        super.tableMap(runtime, random, greedy, catalog, schema);
    }

    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern, int types, int struct){
        return super.tables(runtime, random, catalog, schema, pattern, types, struct);
    }

    /**
     * table[命令合成]<br/>
     * 查询表,不是查表中的数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  BaseMetadata.TYPE.
     * @return String
     */
    @Override
    public List<Run> buildQueryTablesRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT M.*, obj_description(F.relfilenode,'pg_class')  AS TABLE_COMMENT\n");
        builder.append("FROM  INFORMATION_SCHEMA.TABLES AS M\n");
        builder.append("LEFT JOIN pg_namespace AS N ON N.NSPNAME = M.table_schema\n");
        builder.append("LEFT JOIN pg_class AS F ON M.TABLE_NAME = F.relname AND N.oid = F.relnamespace\n");
        builder.append("LEFT JOIN pg_inherits AS I ON I.inhrelid = F.oid\n");//继承关系
        builder.append("WHERE (I.inhrelid IS NULL  OR F.relpartbound IS NULL)\n"); //过滤分区表(没有继承自其他表或 继承自其他表但是子表不是分区表)
        if(!empty(schema)){
            builder.append(" AND M.table_schema = '").append(schema.getName()).append("'");
        }
        if(BasicUtil.isNotEmpty(pattern)){
            builder.append(" AND M.table_name LIKE '").append(pattern).append("'");
        }
        return runs;
    }

    /**
     * table[命令合成]<br/>
     * 查询表备注
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types BaseMetadata.TYPE.
     * @return String
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
    public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
        return super.tables(runtime, index, create, catalog, schema, tables, set);
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
    public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception {
        return super.tables(runtime, index, create, catalog, schema, tables, set);
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
     * @param types types BaseMetadata.TYPE.
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
     * @param types types BaseMetadata.TYPE.
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.tables(runtime, create, tables, catalog, schema, pattern, types);
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
    public <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
        return super.comments(runtime, index, create, catalog, schema, tables, set);
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
    public <T extends Table> List<T> comments(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, List<T> tables, DataSet set) throws Exception {
        return super.comments(runtime, index, create, catalog, schema, tables, set);
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
    public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init){
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
    public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set){
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
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> views, DataSet set)
     * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, View view)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, View view)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set)
     ******************************************************************************************************************/

    /**
     * view[调用入口]<br/>
     * 查询视图
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  BaseMetadata.TYPE.
     * @return List
     * @param <T> View
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types){
        return super.views(runtime, random, greedy, catalog, schema, pattern, types);
    }
    /**
     * view[命令合成]<br/>
     * 查询视图
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types BaseMetadata.TYPE.
     * @return List
     */
    @Override
    public List<Run> buildQueryViewsRun(DataRuntime runtime, boolean greedy, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryViewsRun(runtime, greedy, catalog, schema, pattern, types);
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
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> views, DataSet set) throws Exception {
        return super.views(runtime, index, create, catalog, schema, views, set);
    }
    /**
     * view[结果集封装]<br/>
     * 根据根据驱动内置接口补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param views 上一步查询结果
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types BaseMetadata.TYPE.
     * @return views
     * @throws Exception 异常
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.views(runtime, create, views, catalog, schema, pattern, types);
    }

    /**
     * view[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param view 视图
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, View view){
        return super.ddl(runtime, random, view);
    }

    /**
     * view[命令合成]<br/>
     * 查询view DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param view view
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, View view) throws Exception {
        return super.buildQueryDdlsRun(runtime, view);
    }

    /**
     * view[结果集封装]<br/>
     * 查询 view DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param view view
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, View view, List<String> ddls, DataSet set){
        return super.ddl(runtime, index, view, ddls, set);
    }
    /* *****************************************************************************************************************
     * 													master table
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types)
     * [命令合成]
     * List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
     * [结果集封装]<br/>
     * <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types)
     * [调用入口]
     * List<String> ddl(DataRuntime runtime, String random, MasterTable table)
     * [命令合成]
     * List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table)
     * [结果集封装]<br/>
     * List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set)
     ******************************************************************************************************************/

    /**
     * master table[调用入口]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param greedy 贪婪模式 true:查询权限范围内尽可能多的数据
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types  BaseMetadata.TYPE.
     * @return List
     * @param <T> MasterTable
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern, int types){
        return super.masterTables(runtime, random, greedy, catalog, schema, pattern, types);
    }
    /**
     * master table[命令合成]<br/>
     * 查询主表
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param pattern 名称统配符或正则
     * @param types types
     * @return String
     */
    @Override
    public List<Run> buildQueryMasterTablesRun(DataRuntime runtime, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.buildQueryMasterTablesRun(runtime, catalog, schema, pattern, types);
    }

    /**
     * master table[结果集封装]<br/>
     *  根据查询结果集构造Table
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryMasterTablesRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @param set 查询结果集
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, int index, boolean create, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
        return super.masterTables(runtime, index, create, catalog, schema, tables, set);
    }
    /**
     * master table[结果集封装]<br/>
     * 根据根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param catalog catalog
     * @param schema schema
     * @param tables 上一步查询结果
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends MasterTable> LinkedHashMap<String, T> masterTables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, Catalog catalog, Schema schema, String pattern, int types) throws Exception {
        return super.masterTables(runtime, create, tables, catalog, schema, pattern, types);
    }

    /**
     * master table[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param table MasterTable
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, MasterTable table){
        return super.ddl(runtime, random, table);
    }
    /**
     * master table[命令合成]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table MasterTable
     * @return List
     */
    @Override
    public List<Run> buildQueryDdlsRun(DataRuntime runtime, MasterTable table) throws Exception {
        return super.buildQueryDdlsRun(runtime, table);
    }
    /**
     * master table[结果集封装]<br/>
     * 查询 MasterTable DDL
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryDdlsRun 返回顺序
     * @param table MasterTable
     * @param ddls 上一步查询结果
     * @param set sql执行的结果集
     * @return List
     */
    @Override
    public List<String> ddl(DataRuntime runtime, int index, MasterTable table, List<String> ddls, DataSet set){
        return super.ddl(runtime, index, table, ddls, set);
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
     * <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set)
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
    public <T extends PartitionTable> LinkedHashMap<String,T> partitionTables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String pattern){
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
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT M.*, obj_description(F.relfilenode,'pg_class')  AS TABLE_COMMENT\n");
        builder.append("FROM  INFORMATION_SCHEMA.TABLES AS M\n");
        builder.append("LEFT JOIN pg_namespace AS N ON N.NSPNAME = M.table_schema\n");
        builder.append("LEFT JOIN pg_class AS F ON M.TABLE_NAME = F.relname AND N.oid = F.relnamespace\n");
        builder.append("LEFT JOIN pg_inherits AS I ON I.inhrelid = F.oid\n");//继承关系
        builder.append("LEFT JOIN pg_class AS FM ON FM.oid = I.inhparent AND N.oid = FM.relnamespace\n");//主表
        builder.append("WHERE FM.relname ='").append(master.getName()).append("'\n");
        String schema = master.getSchemaName();
        if(!empty(schema)){
            builder.append(" AND M.table_schema = '").append(schema).append("'");
        }
        if(BasicUtil.isNotEmpty(name)){
            builder.append(" AND M.table_name LIKE '").append(name).append("'");
        }

        return runs;
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
        return buildQueryPartitionTablesRun(runtime, master, tags, null);
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
    public <T extends PartitionTable> LinkedHashMap<String, T> partitionTables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, Catalog catalog, Schema schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception {
        return super.partitionTables(runtime, total, index, create, master, catalog, schema, tables, set);
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
    public List<String> ddl(DataRuntime runtime, String random, PartitionTable table){
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
    public List<String> ddl(DataRuntime runtime, int index, PartitionTable table, List<String> ddls, DataSet set){
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
     * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
     * <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception;
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
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table, boolean primary){
        return super.columns(runtime, random, greedy, table, primary);
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
    public <T extends Column> List<T> columns(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, Table table){
        return super.columns(runtime, random, greedy, catalog, schema, table);
    }
    /**
     * column[命令合成]<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
     * @return sqls
     */
    @Override
    public List<Run> buildQueryColumnsRun(DataRuntime runtime, Table table, boolean metadata) throws Exception {
        List<Run> runs = new ArrayList<>();
        String catalog = null;
        String schema = null;
        String name = null;
        checkName(runtime, null, table);
        if(null != table){
            name = table.getName();
            catalog = table.getCatalogName();
            schema = table.getSchemaName();
        }
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        if(metadata){
            builder.append("SELECT * FROM ");
            name(runtime, builder, table);
            builder.append(" WHERE 1=0");
        }else{
            builder.append("SELECT M.*,pg_catalog.format_type ( FA.ATTTYPID, FA.ATTTYPMOD ) AS FULL_TYPE,FD.DESCRIPTION AS COLUMN_COMMENT \n");
            builder.append("FROM INFORMATION_SCHEMA.COLUMNS M\n");
            builder.append("LEFT JOIN PG_CLASS FC ON FC.RELNAME = M.TABLE_NAME\n");
            builder.append("LEFT JOIN PG_ATTRIBUTE FA ON FA.ATTNAME = M.COLUMN_NAME AND FA.ATTRELID = FC.OID\n");
            builder.append("LEFT JOIN PG_DESCRIPTION FD ON FD.OBJOID = FC.OID AND FD.OBJSUBID = M.ORDINAL_POSITION\n");
            builder.append("WHERE 1 = 1\n");
            if(BasicUtil.isNotEmpty(catalog)){
                builder.append(" AND M.TABLE_CATALOG = '").append(catalog).append("'");
            }
            if(!empty(schema)){
                builder.append(" AND M.TABLE_SCHEMA = '").append(schema).append("'");
            }
            if(BasicUtil.isNotEmpty(name)) {
                builder.append(" AND M.TABLE_NAME = '").append(name).append("'");
            }
            builder.append("\nORDER BY M.TABLE_NAME");
        }
        return runs;
    }

    /**
     * column[命令合成]<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param metadata 是否根据metadata(true:SELECT * FROM T WHERE 1=0,false:查询系统表)
     * @return sqls
     */
    @Override
    public List<Run> buildQueryColumnsRun(DataRuntime runtime, Catalog catalog, Schema schema, List<Table> tables, boolean metadata) throws Exception {
        List<Run> runs = new ArrayList<>();
        Table table = null;
        if(!tables.isEmpty()){
            table = tables.get(0);
        }
        if(null != table){
            checkName(runtime, null, table);
            catalog = table.getCatalog();
            schema = table.getSchema();
        }
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();

        builder.append("SELECT M.*,pg_catalog.format_type ( FA.ATTTYPID, FA.ATTTYPMOD ) AS FULL_TYPE,FD.DESCRIPTION AS COLUMN_COMMENT \n");
        builder.append("FROM INFORMATION_SCHEMA.COLUMNS M\n");
        builder.append("LEFT JOIN PG_CLASS FC ON FC.RELNAME = M.TABLE_NAME\n");
        builder.append("LEFT JOIN PG_ATTRIBUTE  FA ON FA.ATTNAME = M.COLUMN_NAME AND FA.ATTRELID = FC.OID\n");
        builder.append("LEFT JOIN PG_DESCRIPTION FD ON FD.OBJOID = FC.OID AND FD.OBJSUBID = M.ORDINAL_POSITION\n");
        builder.append("WHERE 1 = 1\n");
        if(!empty(catalog)){
            builder.append(" AND M.TABLE_CATALOG = '").append(catalog.getName()).append("'");
        }
        if(!empty(schema)){
            builder.append(" AND M.TABLE_SCHEMA = '").append(schema.getName()).append("'");
        }
        in(runtime, builder, "M.TABLE_NAME", Table.names(tables));
        builder.append("\nORDER BY M.TABLE_NAME");

        return runs;
    }
    /**
     * column[结果集封装]<br/>
     *  根据查询结果集构造Tag
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条SQL 对照 buildQueryColumnsRun返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param columns 上一步查询结果
     * @param set 查询结果集
     * @return tags tags
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception {
        set.changeKey("UDT_NAME","DATA_TYPE");
        return super.columns(runtime, index, create, table, columns, set);
    }
    @Override
    public <T extends Column> List<T> columns(DataRuntime runtime, int index, boolean create, Table table, List<T> columns, DataSet set) throws Exception {
        return super.columns(runtime, index, create, table, columns, set);
    }

    /**
     * column[结果集封装]<br/>
     * 解析JDBC get columns结果
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param columns 上一步查询结果
     * @param pattern 名称
     * @throws Exception 异常
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception {
        return super.columns(runtime, create, columns, table, pattern);
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
    public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table){
        return super.tags(runtime, random, greedy, table);
    }
    /**
     * tag[命令合成]<br/>
     * 查询表上的列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param metadata 是否需要根据metadata
     * @return sqls
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

    /* *****************************************************************************************************************
     * 													primary
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table)
     * [命令合成]
     * List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception
     * [结构集封装]
     * <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set)
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
    public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table){
        return super.primary(runtime, random, greedy, table);
    }

    /**
     * primary[命令合成]<br/>
     * 查询表上的主键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return sqls
     */
    @Override
    public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        checkName(runtime, null, table);
        StringBuilder builder = run.getBuilder();
        //test_pk_pkey	| p	| {2,1}	| 	PRIMARY KEY (id, name)
        builder.append("SELECT  m.conname, pg_get_constraintdef(m.oid, true) AS define\n");
        builder.append("FROM pg_constraint m \n");
        builder.append("LEFT JOIN pg_namespace ns ON m.connamespace = ns.oid \n");
        builder.append("LEFT JOIN pg_class ft ON m.conrelid = ft.oid \n");
        builder.append("WHERE ft.relname = '").append(table.getName()).append("'");
        Schema schema = table.getSchema();
        if(!empty(schema)){
            builder.append(" AND ns.nspname = '").append(schema.getName()).append("'");
        }
        return runs;
    }

    /**
     * primary[结构集封装]<br/>
     *  根据查询结果集构造PrimaryKey
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param table 表
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T init(DataRuntime runtime, int index, T primary, Table table, DataSet set) throws Exception {
        if(set.size()>0){
            DataRow row = set.getRow(0);
            primary = (T)new PrimaryKey();
            //conname 	    |contype	|conkey |  define
            //test_pk_pkey	| p			| {2,1}	| 	PRIMARY KEY (id, name)
            primary.setName(row.getString("CONNAME"));
            String define = row.getString("DEFINE");
            String[] cols = RegularUtil.cut(define, "(",")").split(",");
            for(String col:cols){
                Column column = new Column(col.trim().replace("\"",""));
                column.setTable(table);
                primary.addColumn(column);
            }
        }
        return primary;
    }

    /**
     * primary[结构集封装]<br/>
     * 根据查询结果集构造PrimaryKey更多属性
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param table 表
     * @param set sql查询结果
     * @throws Exception 异常
     */
    @Override
    public <T extends PrimaryKey> T detail(DataRuntime runtime, int index, T primary, Table table, DataSet set) throws Exception {
        return super.detail(runtime, index, primary, table, set);
    }
    /**
     * primary[结构集封装-依据]<br/>
     * 读取primary key元数据结果集的依据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return PrimaryMetadataAdapter
     */
    @Override
    public PrimaryMetadataAdapter primaryMetadataAdapter(DataRuntime runtime){
        return super.primaryMetadataAdapter(runtime);
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
    public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random, boolean greedy, Table table){
        return super.foreigns(runtime, random, greedy,table);
    }
    /**
     * foreign[命令合成]<br/>
     * 查询表上的外键
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @return sqls
     */
    @Override
    public List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        checkName(runtime, null, table);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT TC.CONSTRAINT_NAME,TC.TABLE_NAME AS TABLE_NAME, KCU.COLUMN_NAME AS COLUMN_NAME, KCU.ORDINAL_POSITION,CCU.TABLE_NAME AS REFERENCED_TABLE_NAME, CCU.COLUMN_NAME AS REFERENCED_COLUMN_NAME\n");
        builder.append("FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS TC\n");
        builder.append("JOIN INFORMATION_SCHEMA.KEY_COLUMN_USAGE AS KCU ON TC.CONSTRAINT_NAME = KCU.CONSTRAINT_NAME\n");
        builder.append("JOIN INFORMATION_SCHEMA.CONSTRAINT_COLUMN_USAGE AS CCU ON CCU.CONSTRAINT_NAME = TC.CONSTRAINT_NAME\n");
        builder.append("WHERE TC.CONSTRAINT_TYPE = 'FOREIGN KEY'\n");
        if(null != table){
            String name = table.getName();
            if(BasicUtil.isNotEmpty(name)){
                builder.append(" AND TC.TABLE_NAME = '").append(name).append("'\n");
            }
        }
        builder.append("ORDER BY KCU.ORDINAL_POSITION");
        return runs;
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
        if(null == foreigns){
            foreigns = new LinkedHashMap<>();
        }
        for(DataRow row:set){
            String name = row.getString("CONSTRAINT_NAME");
            T foreign = foreigns.get(name.toUpperCase());
            if(null == foreign){
                foreign = (T)new ForeignKey();
                foreign.setName(name);
                foreign.setTable(row.getString("TABLE_NAME"));
                foreign.setReference(row.getString("REFERENCED_TABLE_NAME"));
                foreigns.put(name.toUpperCase(), foreign);
            }
            Table refTable = new Table(row.getString("REFERENCED_CATALOG_NAME"),row.getString("REFERENCED_SCHEMA_NAME"),row.getString("REFERENCED_TABLE_NAME"));
            Column reference = new Column(row.getString("REFERENCED_COLUMN_NAME"));
            reference.setTable(refTable);
            foreign.addColumn(new Column(row.getString("COLUMN_NAME")).setReference(reference).setPosition(row.getInt("ORDINAL_POSITION", 0)));

        }
        return foreigns;
    }

    /* *****************************************************************************************************************
     * 													index
     * -----------------------------------------------------------------------------------------------------------------
     * [调用入口]
     * <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern)
     * <T extends Index> LinkedHashMap<T, Index> indexs(DataRuntime runtime, String random, Table table, String pattern)
     * [命令合成]
     * List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name)
     * [结果集封装]<br/>
     * <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set)
     * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set)
     * <T extends Index> List<T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate)
     * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate)
     ******************************************************************************************************************/
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
    @Override
    public <T extends Index> List<T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String pattern){
        return super.indexs(runtime, random, greedy, table, pattern);
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
    public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, Table table, String pattern){
        return super.indexs(runtime, random, table, pattern);
    }
    /**
     * index[命令合成]<br/>
     * 查询表上的索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param name 名称
     * @return sqls
     */
    @Override
    public List<Run> buildQueryIndexesRun(DataRuntime runtime, Table table, String name){
        return super.buildQueryIndexesRun(runtime, table, name);
    }

    /**
     * index[结果集封装]<br/>
     *  根据查询结果集构造Index
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param indexs 上一步查询结果
     * @param set 查询结果集
     * @return indexs indexs
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception {
        return super.indexs(runtime, index, create, table, indexs, set);
    }
    /**
     * index[结果集封装]<br/>
     *  根据查询结果集构造Index
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryIndexesRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param indexs 上一步查询结果
     * @param set 查询结果集
     * @return indexs indexs
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> List<T> indexs(DataRuntime runtime, int index, boolean create, Table table, List<T> indexs, DataSet set) throws Exception {
        return super.indexs(runtime, index, create, table, indexs, set);
    }

    /**
     * index[结果集封装]<br/>
     * 根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param unique 是否唯一
     * @param approximate 索引允许结果反映近似值
     * @return indexs indexs
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> List<T> indexs(DataRuntime runtime, boolean create, List<T> indexs, Table table, boolean unique, boolean approximate) throws Exception {
        return super.indexs(runtime, create, indexs, table, unique, approximate);
    }
    /**
     * index[结果集封装]<br/>
     * 根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param table 表
     * @param unique 是否唯一
     * @param approximate 索引允许结果反映近似值
     * @return indexs indexs
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception {
        return super.indexs(runtime, create, indexs, table, unique, approximate);
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
    public <T extends Constraint> List<T> constraints(DataRuntime runtime, String random, boolean greedy, Table table, String pattern){
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
    public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, String random, Table table, Column column, String pattern){
        return super.constraints(runtime, random, table, column, pattern);
    }

    /**
     * constraint[命令合成]<br/>
     * 查询表上的约束
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param pattern 名称通配符或正则
     * @return sqls
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
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events){
        return super.triggers(runtime, random, greedy, table, events);
    }
    /**
     * trigger[命令合成]<br/>
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     * @param events 事件 INSERT|UPDATE|DELETE
     * @return sqls
     */
    public List<Run> buildQueryTriggersRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events){
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("SELECT * FROM INFORMATION_SCHEMA.TRIGGERS WHERE 1=1");
        if(null != table){
            checkName(runtime, null, table);
            Schema schemae = table.getSchema();
            String name = table.getName();
            if(BasicUtil.isNotEmpty(schemae)){
                builder.append(" AND TRIGGER_SCHEMA = '").append(schemae).append("'");
            }
            if(BasicUtil.isNotEmpty(name)){
                builder.append(" AND EVENT_OBJECT_TABLE = '").append(name).append("'");
            }
        }
        if(null != events && events.size()>0){
            builder.append(" AND(");
            boolean first = true;
            for(Trigger.EVENT event:events){
                if(!first){
                    builder.append(" OR ");
                }
                builder.append("EVENT_MANIPULATION ='").append(event);
            }
            builder.append(")");
        }
        return runs;
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
    public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception {
        if(null == triggers){
            triggers = new LinkedHashMap<>();
        }
        for(DataRow row:set){
            String name = row.getString("TRIGGER_NAME");
            T trigger = triggers.get(name.toUpperCase());
            if(null == trigger){
                trigger = (T)new Trigger();
            }
            trigger.setName(name);
            Table tab = new Table(row.getString("EVENT_OBJECT_TABLE"));
            tab.setSchema(row.getString("TRIGGER_SCHEMA"));
            tab.setCatalog(row.getString("TRIGGER_CATALOG("));
            trigger.setTable(tab);
            boolean each = false;
            if("ROW".equalsIgnoreCase(row.getString("ACTION_ORIENTATION"))){
                each = true;
            }
            trigger.setEach(each);
            try{
                String[] events = row.getStringNvl("EVENT_MANIPULATION").split(",");
                String time = row.getString("ACTION_TIMING");
                trigger.setTime(Trigger.TIME.valueOf(time));
                for(String event:events) {
                    trigger.addEvent(Trigger.EVENT.valueOf(event));
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            trigger.setDefinition(row.getString("ACTION_STATEMENT"));

            triggers.put(name.toUpperCase(), trigger);

        }
        return triggers;
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
     * <T extends Procedure> List<T> procedures(DataRuntime runtime, boolean create, List<T> procedures, DataSet set) throws Exception;
     * <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, boolean create, LinkedHashMap<String, T> procedures, DataSet set) throws Exception;
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
    public <T extends Procedure> List<T> procedures(DataRuntime runtime, String random, boolean greedy, Catalog catalog, Schema schema, String pattern){
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
    public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, Catalog catalog, Schema schema, String pattern){
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
    public List<String> ddl(DataRuntime runtime, String random, Procedure procedure){
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
    public List<String> ddl(DataRuntime runtime, int index, Procedure procedure, List<String> ddls, DataSet set){
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
     * <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
     * <T extends Function> List<T> functions(DataRuntime runtime, boolean create, List<T> functions, DataSet set) throws Exception;
     * <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception;
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
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param name 名称统配符或正则
     * @return sqls
     */
    @Override
    public List<Run> buildQueryFunctionsRun(DataRuntime runtime, Catalog catalog, Schema schema, String name) {
        return super.buildQueryFunctionsRun(runtime, catalog, schema, name);
    }

    /**
     * function[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param functions 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Function> List<T> functions(DataRuntime runtime, int index, boolean create, List<T> functions, DataSet set) throws Exception {
        return super.functions(runtime, index, create, functions, set);
    }
    /**
     * function[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param index 第几条查询SQL 对照 buildQueryConstraintsRun 返回顺序
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param functions 上一步查询结果
     * @param set 查询结果集
     * @return LinkedHashMap
     * @throws Exception 异常
     */
    @Override
    public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, T> functions, DataSet set) throws Exception {
        return super.functions(runtime, index, create, functions, set);
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
     *
     * function[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta Function
     * @return ddl
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, Function meta){
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
    public List<String> ddl(DataRuntime runtime, int index, Function function, List<String> ddls, DataSet set){
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
     * 查询表上的 Trigger
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog catalog
     * @param schema schema
     * @param name 名称统配符或正则
     * @return sqls
     */
    @Override
    public List<Run> buildQuerySequencesRun(DataRuntime runtime, Catalog catalog, Schema schema, String name) {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        String catalogName = null;
        String schemaName = null;
        if(null != catalog){
            catalogName = catalog.getName();
        }
        if(null != schema){
            schemaName = schema.getName();
        }
        builder.append("SELECT * FROM pg_sequences WHERE 1=1\n");
        if(BasicUtil.isNotEmpty(catalogName)){
            builder.append(" AND SEQUENCEOWNER = '").append(catalogName).append("'");
        }
        if(BasicUtil.isNotEmpty(schemaName)){
            builder.append(" AND SCHEMANAME = '").append(schemaName).append("'");
        }
        if(BasicUtil.isNotEmpty(name)){
            builder.append(" AND SEQUENCENAME = '").append(name).append("'");
        }
        return runs;
    }

    /**
     * sequence[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
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
        if(null == sequences){
            sequences = new ArrayList<>();
        }
        for(DataRow row:set){
            String name = row.getString("SEQUENCENAME");
            Sequence sequence = new Sequence(name);
            sequences.add((T)init(sequence, row));
        }
        return sequences;
    }
    /**
     * sequence[结果集封装]<br/>
     * 根据查询结果集构造 Trigger
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
        if(null == sequences){
            sequences = new LinkedHashMap<>();
        }
        for(DataRow row:set){
            String name = row.getString("SEQUENCENAME");
            Sequence sequence = sequences.get(name.toUpperCase());
            sequences.put(name.toUpperCase(), (T)init(sequence, row));
        }
        return sequences;
    }
    protected Sequence init(Sequence sequence, DataRow row){
        if(null == sequence){
            sequence = new Sequence();
        }
        sequence.setName(row.getString("SEQUENCENAME"));
        sequence.setCatalog(row.getString("SEQUENCEOWNER"));
        sequence.setSchema(row.getString("SCHEMANAME"));
        sequence.setLast(row.getLong("LAST_NUMBER", (Long)null));
        sequence.setMin(row.getLong("MIN_VALUE", (Long)null));
        sequence.setStart(row.getLong("START_VALUE", (Long)null));
        sequence.setMax(row.getLong("MAX_VALUE", (Long)null));
        sequence.setIncrement(row.getInt("INCREMENT_BY", 1));
        sequence.setCache(row.getInt("CACHE_SIZE", null));
        sequence.setCycle(row.getBoolean("CYCLE", null));
        return sequence;
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
     *
     * sequence[调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta Sequence
     * @return ddl
     */
    @Override
    public List<String> ddl(DataRuntime runtime, String random, Sequence meta){
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
    public List<String> ddl(DataRuntime runtime, int index, Sequence sequence, List<String> ddls, DataSet set){
        return super.ddl(runtime, index, sequence, ddls, set);
    }

    /* *****************************************************************************************************************
     * 													common
     * ----------------------------------------------------------------------------------------------------------------
     */
    /**
     *
     * 根据 catalog, schema, name检测tables集合中是否存在
     * @param tables tables
     * @param catalog catalog
     * @param schema schema
     * @param name name
     * @return 如果存在则返回Table 不存在则返回null
     * @param <T> Table
     */
    @Override
    public <T extends Table> T table(List<T> tables, Catalog catalog, Schema schema, String name){
        return super.table(tables, catalog, schema, name);
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
    public <T extends Schema> T schema(List<T> schemas, Catalog catalog, String name){
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
    public <T extends Catalog> T catalog(List<T> catalogs, String name){
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
    public <T extends Database> T database(List<T> databases, String name){
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
     * 是否支持DDL合并
     * @return boolean
     */
    public boolean slice(){
        return true;
    }
    /**
     * ddl [执行命令]
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param meta BaseMetadata(表,列等)
     * @param action 执行命令
     * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
     * @return boolean
     */
    @Override
    public boolean execute(DataRuntime runtime, String random, BaseMetadata meta, ACTION.DDL action, Run run){
        return super.execute(runtime, random, meta, action, run);
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
     * 创建表,执行的SQL通过meta.ddls()返回
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
     * 修改表,执行的SQL通过meta.ddls()返回
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
     * 删除表,执行的SQL通过meta.ddls()返回
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
     * 重命名表,执行的SQL通过meta.ddls()返回
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
    public String keyword(BaseMetadata meta)
{
        return "TABLE";
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
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        builder.append("ALTER TABLE ");
        name(runtime, builder, meta);
        builder.append(" RENAME TO ");
        //去掉catalog schema前缀
        Table update = new Table(meta.getUpdate().getName());
        name(runtime, builder, update);
        return runs;
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
     * 创建表完成后追加列备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Column meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 表
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendColumnCommentRun(DataRuntime runtime, Table meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        if(null != meta){
            LinkedHashMap<String, Column> columns = meta.getColumns();
            if(null != columns){
                for(Column column:columns.values()){
                    runs.addAll(buildChangeCommentRun(runtime, column));
                }
            }
        }
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
        return buildChangeCommentRun(runtime, meta);
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
        List<Run> runs = new ArrayList<>();
        String comment = meta.getComment();
        if(BasicUtil.isNotEmpty(comment)) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            builder.append("COMMENT ON TABLE ");
            name(runtime, builder, meta);
            builder.append(" IS '").append(comment).append("'");
        }
        return runs;
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
    public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists){
        return super.checkTableExists(runtime, builder, exists);
    }

    /**
     * table[命令合成-子流程]<br/>
     * 检测表主键(在没有显式设置主键时根据其他条件判断如自增),同时根据主键对象给相关列设置主键标识
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table 表
     */
    @Override
    public void checkPrimary(DataRuntime runtime, Table table){
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
    public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table meta){
        PrimaryKey primary = meta.getPrimaryKey();
        LinkedHashMap<String, Column> pks = null;
        String name = null;
        if(null != primary){
            pks = primary.getColumns();
            name = primary.getName();
        }else{
            pks = meta.primarys();
            name = "pk_" + meta.getName();
        }
        if(!pks.isEmpty()){
            checkName(runtime, null, meta);
            builder.append(",CONSTRAINT ");
            delimiter(builder, name);
            builder.append(" PRIMARY KEY (");
            Column.sort(primary.getPositions(), pks);
            //不支持 asc desc
            delimiter(builder, Column.names(pks));
            builder.append(")");
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
    public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Table meta){
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
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table meta){
        //单独添加备注
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
        return super.partitionBy(runtime, builder, meta);
    }

    /**
     * table[命令合成-子流程]<br/>
     * 子表执行分区依据(相关主表及分区值)
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
     * 创建视图,执行的SQL通过meta.ddls()返回
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
     * 修改视图,执行的SQL通过meta.ddls()返回
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
     * 删除视图,执行的SQL通过meta.ddls()返回
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
     * 重命名视图,执行的SQL通过meta.ddls()返回
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
        if (null == builder) {
            builder = new StringBuilder();
        }
        builder.append("CREATE");
        if(meta.isMaterialize()){
            builder.append(" MATERIALIZED");
        }
        builder.append(" VIEW ");
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
    public StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists){
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
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, View meta){
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
     * 创建主表,执行的SQL通过meta.ddls()返回
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
     * 修改主表,执行的SQL通过meta.ddls()返回
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
     * 删除主表,执行的SQL通过meta.ddls()返回
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
     * 重命名主表,执行的SQL通过meta.ddls()返回
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
        meta.setKeyword("TABLE");
        Table tab = meta;
        return super.buildCreateRun(runtime, tab);
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
     * 创建分区表,执行的SQL通过meta.ddls()返回
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
     * 修改分区表,执行的SQL通过meta.ddls()返回
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
     * 删除分区表,执行的SQL通过meta.ddls()返回
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
     * 创建分区表,执行的SQL通过meta.ddls()返回
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
        meta.setKeyword("TABLE");
        Table tab = meta;
        return buildCreateRun(runtime, tab);
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
     * 添加列,执行的SQL通过meta.ddls()返回
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
     * 修改列,执行的SQL通过meta.ddls()返回
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
     * 修改列,执行的SQL通过meta.ddls()返回
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
     * 删除列,执行的SQL通过meta.ddls()返回
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
     * 重命名列,执行的SQL通过meta.ddls()返回
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
    @Override
    public List<Run> buildAddRun(DataRuntime runtime, Column meta) throws Exception {
        return buildAddRun(runtime, meta, false);
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
    @Override
    public List<Run> buildAlterRun(DataRuntime runtime, Column meta) throws Exception {
        return buildAlterRun(runtime, meta, false);
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

    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Column meta) throws Exception {
        return super.buildDropRun(runtime, meta);
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
    public List<Run> buildRenameRun(DataRuntime runtime, Column meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        checkName(runtime, null, meta);
        StringBuilder builder = run.getBuilder();
        builder.append("ALTER TABLE ");
        name(runtime, builder, meta.getTable(true));
        builder.append(" RENAME ");
        delimiter(builder, meta.getName());
        builder.append(" TO ");
        delimiter(builder, meta.getUpdate().getName());
        meta.setName(meta.getUpdate().getName());
        return runs;
    }

    /**
     * column[命令合成-子流程]<br/>
     * 修改数据类型
     * alter table T alter column C type varchar(64);
     * 一般不直接调用,如果需要由buildAlterRun内部统一调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 列
     * @return String
     */
    @Override
    public List<Run> buildChangeTypeRun(DataRuntime runtime, Column meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        Column update = meta.getUpdate();
        builder.append("ALTER TABLE ");
        name(runtime, builder, meta.getTable(true));
        builder.append(" ALTER COLUMN ");
        delimiter(builder, meta.getName());
        builder.append(" TYPE ");
        type(runtime, builder, update);
        String type = update.getTypeName();
        if(type.contains("(")){
            type = type.substring(0,type.indexOf("("));
        }
        builder.append(" USING ");
        delimiter(builder, meta.getName());
        builder.append("::").append(type);
        return runs;
    }

    /**
     * column[命令合成-子流程]<br/>
     * 修改表的关键字
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @return String
     */
    @Override
    public String alterColumnKeyword(DataRuntime runtime){
        return super.alterColumnKeyword(runtime);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 添加列引导
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder StringBuilder
     * @param meta 列
     * @return String
     */
    @Override
    public StringBuilder addColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta){
        return super.addColumnGuide(runtime, builder, meta);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 删除列引导
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder StringBuilder
     * @param meta 列
     * @return String
     */
    @Override
    public StringBuilder dropColumnGuide(DataRuntime runtime, StringBuilder builder, Column meta){
        return super.dropColumnGuide(runtime, builder, meta);
    }

    /**
     * column[命令合成-子流程]<br/>
     * 修改默认值<br/>
     * ALTER TABLE T ALTER COLUMN C SET DEFAULT 0;<br/>
     * ALTER TABLE T ALTER COLUMN C DROP DEFAULT;<br/>
     * 一般不直接调用,如果需要由buildAlterRun内部统一调用<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 列
     * @return String
     */
    @Override
    public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        Object def = null;
        if(null != meta.getUpdate()){
            def = meta.getUpdate().getDefaultValue();
        }else {
            def = meta.getDefaultValue();
        }
        if(null != def){
            String str = def.toString();
            if(str.contains("::")){
                str = str.split("::")[0];
            }
            str = str.replace("'","");
            def = str;
        }

        builder.append("ALTER TABLE ");
        name(runtime, builder, meta.getTable(true)).append(" ALTER COLUMN ");
        delimiter(builder, meta.getName());
        if(null != def){
            builder.append(" SET DEFAULT '").append(def).append("'");
        }else{
            builder.append(" DROP DEFAULT");
        }
        return runs;
    }

    /**
     * column[命令合成-子流程]<br/>
     * 修改非空限制
     * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME DROP NOT NULL
     * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME SET NOT NULL
     * 一般不直接调用,如果需要由buildAlterRun内部统一调用
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 列
     * @return String
     */
    @Override
    public List<Run> buildChangeNullableRun(DataRuntime runtime, Column meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        int nullable = meta.isNullable();
        int uNullable = meta.getUpdate().isNullable();
        if(nullable != -1 && uNullable != -1){
            if(nullable != uNullable) {
                builder.append("ALTER TABLE ");
                name(runtime, builder, meta.getTable(true)).append(" ALTER ");
                delimiter(builder, meta.getName());
                if (uNullable == 0) {
                    builder.append(" SET ");
                } else {
                    builder.append(" DROP ");
                }
                builder.append(" NOT NULL");
                meta.setNullable(uNullable);
            }
        }
        return runs;
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
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Column meta) throws Exception {
        List<Run> runs = new ArrayList<>();
        String comment = null;
        Column update = meta.getUpdate();
        if(null != update){
            comment = update.getComment();
        }
        if(BasicUtil.isEmpty(comment)){
            comment = meta.getComment();
        }
        if(BasicUtil.isNotEmpty(comment)) {
            Run run = new SimpleRun(runtime);
            runs.add(run);
            StringBuilder builder = run.getBuilder();
            builder.append("COMMENT ON COLUMN ");
            name(runtime, builder, meta.getTable(true)).append(".");
            delimiter(builder, meta.getName());
            builder.append(" IS '").append(comment).append("'");
        }
        return runs;
    }

    /**
     * column[命令合成-子流程]<br/>
     * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column 列
     * @return sql
     * @throws Exception 异常
     */
    /**
     * 创建表完成后追加表备注,创建过程能添加备注的不需要实现与comment(DataRuntime runtime, StringBuilder builder, Table meta)二选一实现
     * @param meta 列
     * @return sql
     * @throws Exception 异常
     */
    @Override
    public List<Run> buildAppendCommentRun(DataRuntime runtime, Column meta) throws Exception {
        return buildChangeCommentRun(runtime, meta);
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
    public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column meta) throws Exception {
        return super.buildDropAutoIncrement(runtime, meta);
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
    public StringBuilder define(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action){
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
    public StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists){
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
    public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta){

        String type = meta.getTypeName();
        if(null == type){
            type ="";
        }
        type = type.toLowerCase();
        //创建并自增时 或 非自增改自增时 用serial 其他情况用int
        boolean serial = false;
        if(ACTION.DDL.COLUMN_ADD == meta.getAction() && meta.isAutoIncrement() == 1){
            serial = true;
        }else {
            Column update = meta.getUpdate();
            if(null != update && update.isAutoIncrement() !=1 && meta.isAutoIncrement() == 1){
                serial = true;
            }
        }
        if(serial){
            if ("int4".equals(type) || "int".equals(type) || "integer".equals(type)) {
                meta.setType("SERIAL4");
            } else if ("int8".equals(type) || "long".equals(type) || "bigint".equals(type)) {
                meta.setType("SERIAL8");
            } else if ("int2".equals(type) || "smallint".equals(type) || "short".equals(type)) {
                //9.2.0
                meta.setType("SERIAL2");
            }else{
                meta.setType("SERIAL8");
            }
        }else if(type.equals("int") || type.contains("long") || type.contains("serial") || type.contains("short")){
            if ("serial4".equals(type) || "int".equals(type) || "integer".equals(type)) {
                meta.setType("int4");
            } else if ("serial8".equals(type) || "long".equals(type) || "bigint".equals(type)) {
                meta.setType("int8");
            } else if ("serial2".equals(type) || "smallint".equals(type) || "short".equals(type)) {
                meta.setType("int2");
            }else{
                meta.setType("int8");
            }
        }
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
    public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column meta, String type, int ignoreLength, int ignorePrecision, int ignoreScale){
        return super.type(runtime, builder, meta, type, ignoreLength, ignorePrecision, ignoreScale);
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
    public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column meta, ACTION.DDL action){
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
    public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column meta){
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
    public StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column meta){
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
    public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Column meta){
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
    public StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column meta){
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
    public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column meta){
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
    public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column meta){
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
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column meta){
        //单独生成备注
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
     * List<Run> buildAddRun(DataRuntime runtime, Tag meta)
     * List<Run> buildAlterRun(DataRuntime runtime, Tag meta)
     * List<Run> buildDropRun(DataRuntime runtime, Tag meta)
     * List<Run> buildRenameRun(DataRuntime runtime, Tag meta)
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
    public List<Run> buildAddRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildAddRun(runtime, meta);
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
    public List<Run> buildAlterRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildAlterRun(runtime, meta);
    }

    /**
     * tag[命令合成]<br/>
     * 删除标签
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 标签
     * @return String
     */
    @Override
    public List<Run> buildDropRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildDropRun(runtime, meta);
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
    public List<Run> buildRenameRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildRenameRun(runtime, meta);
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
    public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildChangeDefaultRun(runtime, meta);
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
    public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildChangeNullableRun(runtime, meta);
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
    public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildChangeCommentRun(runtime, meta);
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
    public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag meta) throws Exception {
        return super.buildChangeTypeRun(runtime, meta);
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
    public StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists){
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
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        LinkedHashMap<String,Column> columns = meta.getColumns();
        if(null != columns && !columns.isEmpty()) {
            if(!slice(slice)) {
                builder.append("ALTER TABLE ");
                name(runtime, builder, meta.getTable(true));
            }
            builder.append(" ADD PRIMARY KEY (");
            Column.sort(meta.getPositions(), columns);
            delimiter(builder, Column.names(columns));
            builder.append(")");

        }
        return runs;
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
    public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey origin, PrimaryKey meta) throws Exception {
        return super.buildAlterRun(runtime, origin, meta);
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
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        if(!slice(slice)) {
            builder.append("ALTER TABLE ");
            name(runtime, builder, meta.getTable(true));
        }
        builder.append(" DROP CONSTRAINT ");
        delimiter(builder, meta.getName());
        return runs;
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
     * 添加索引
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 索引
     * @return String
     */
    @Override
    public List<Run> buildAppendIndexRun(DataRuntime runtime, Table meta) throws Exception {
        return super.buildAppendIndexRun(runtime, meta);
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
        List<Run> runs = new ArrayList<>();
        Run run = new SimpleRun(runtime);
        runs.add(run);
        StringBuilder builder = run.getBuilder();
        if(meta.isPrimary()){
            log.info("[主键索引,忽略删除][index:{}]", meta.getName());
        }else {
            builder.append("DROP INDEX ");
            delimiter(builder, meta.getName());
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
    public StringBuilder type(DataRuntime runtime, StringBuilder builder, Index meta){
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
    public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Index meta){
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
    public StringBuilder each(DataRuntime runtime, StringBuilder builder, Trigger meta){
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
    public StringBuilder parameter(DataRuntime runtime, StringBuilder builder, Parameter parameter){
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
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, DataSource datasource, T meta){
        super.checkSchema(runtime, datasource,meta);
    }

    @Override
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, Connection con, T meta){
        super.checkSchema(runtime, con, meta);
    }
    /**
     * 根据运行环境识别 catalog与schema
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta BaseMetadata
     * @param <T> BaseMetadata
     */
	@Override
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, T meta){
        super.checkSchema(runtime, meta);
    }

	/**
	 * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta BaseMetadata
	 * @param catalog catalog
	 * @param schema schema
     * @param overrideMeta 如果meta中有值，是否覆盖
     * @param overrideRuntime 如果runtime中有值，是否覆盖，注意结果集中可能跨多个schema，所以一般不要覆盖runtime,从con获取的可以覆盖ResultSet中获取的不要覆盖
	 * @param <T> BaseMetadata
	 */
	@Override
    public <T extends BaseMetadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema, boolean overrideRuntime, boolean overrideMeta){
        super.correctSchemaFromJDBC(runtime, meta, catalog, schema, overrideRuntime, overrideMeta);
    }

	/**
	 * 识别根据jdbc返回的catalog与schema,部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta BaseMetadata
	 * @param catalog catalog
	 * @param schema schema
	 * @param <T> BaseMetadata
	 */
	@Override
	public <T extends BaseMetadata> void correctSchemaFromJDBC(DataRuntime runtime, T meta, String catalog, String schema){
		correctSchemaFromJDBC(runtime, meta, catalog, schema, false, true);
	}

	/**
	 * 在调用jdbc接口前处理业务中的catalog,schema,部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
	 * @param catalog catalog
	 * @param schema schema
	 * @return String[]
	 */
	@Override
	public String[] correctSchemaFromJDBC(String catalog, String schema){
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
    public String columnMetadataLengthRefer(DataRuntime runtime, TypeMetadata meta){
        return super.columnMetadataLengthRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字有效位数列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataPrecisionRefer(DataRuntime runtime, TypeMetadata meta){
        return super.columnMetadataPrecisionRefer(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字小数位数列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataScaleRefer(DataRuntime runtime, TypeMetadata meta){
        return super.columnMetadataScaleRefer(runtime, meta);
    }
    public String insertHead(ConfigStore configs){
        return super.insertHead(configs);
    }
    public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns){
        StringBuilder builder = new StringBuilder();
        Boolean override = null;
        if(null != configs){
            override = configs.override();
        }
        if(null != override){
            builder.append(" ON CONFLICT");
            Constraint constraint = configs.overrideByConstraint();
            if(null != constraint){
                //ON CONFLICT ON CONSTRAINT 约束
                builder.append(" ON CONSTRAINT ");
                delimiter(builder, constraint.getName());
            }else {
                List<String> bys = configs.overrideByColumns();
                if (null == bys) {
                    bys = configs.getPrimaryKeys();
                }
                if (null == bys) {
                    bys = new ArrayList<>();
                }
                if (bys.isEmpty()) {
                    bys.add(ConfigTable.DEFAULT_PRIMARY_KEY);
                }
                //ON CONFLICT ( id )
                builder.append("(").append(BeanUtil.concat(bys)).append(")");
            }
            if(override){
                builder.append(" DO UPDATE SET ");
                boolean first = true;
                for(Column column:columns.values()){
                    if(!first){
                        builder.append(",");
                    }
                    first = false;
                    builder.append(column.getName()).append(" = EXCLUDED.");
                    delimiter(builder, column.getName());
                }
            }else{
                builder.append(" DO NOTHING");
            }

        }
        return builder.toString();
    }
   

    /**
     * 内置函数 多种数据库兼容时需要
     * @param value SQL_BUILD_IN_VALUE
     * @return String
     */
    @Override
    public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value){
        if(value == SQL_BUILD_IN_VALUE.CURRENT_DATETIME){
            return "now()";
        }
        return null;
    }
    /**
     * 拼接字符串
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param args args
     * @return String
     */
    @Override
    public String concat(DataRuntime runtime, String ... args){
        return concatOr(runtime, args);
    }

    /**
     * 伪表
     * @return String
     */
    protected String dummy(){
        return super.dummy();
    }
}
