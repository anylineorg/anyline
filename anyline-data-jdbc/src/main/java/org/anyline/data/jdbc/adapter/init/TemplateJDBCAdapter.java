package org.anyline.data.jdbc.adapter.init;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.metadata.BaseMetadata;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.LinkedHashMap;

//@Repository("anyline.data.jdbc.adapter.数据库类型简写")
public abstract class TemplateJDBCAdapter extends AbstractJDBCAdapter {

/*    public DatabaseType type(){
        return DatabaseType.NONE;
    }

    public TemplateJDBCAdapter(){
        super();
        delimiterFr = "`";
        delimiterTo = "`";
        for (MySQLTypeMetadataAlias alias: MySQLTypeMetadataAlias.values()){
            this.alias.put(alias.name(), alias.standard());
        }
        for(MySQLWriter writer: MySQLWriter.values()){
            reg(writer.supports(), writer.writer());
        }
        for(MySQLReader reader: MySQLReader.values()){
            reg(reader.supports(), reader.reader());
        }
    }
    @Value("${anyline.data.jdbc.delimiter.数据库类型简写:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }*/

    /* *****************************************************************************************************************
     *
     * 													复制过程
     * 1.添加TypeMetadataAlias
     * 2.如果有类型转换需要添加writer reader
     * 3.放工以上注释
     * 4.复制TemplateAdapter到这里
     *
     *  ***************************************************************************************************************/

    /* *****************************************************************************************************************
     *
     * 														JDBC
     *
     *  ***************************************************************************************************************/

    @Override
    public <T extends BaseMetadata> void checkSchema(DataRuntime runtime, DataSource dataSource, T meta){
        super.checkSchema(runtime, dataSource, meta);
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
	 * 识别根据jdbc返回的catalog与schema, 部分数据库(如mysql)系统表与jdbc标准可能不一致根据实际情况处理<br/>
	 * 注意一定不要处理从SQL中返回的，应该在SQL中处理好
	 * @param meta BaseMetadata
	 * @param catalog catalog
	 * @param schema schema
	 * @param override 如果meta中有值，是否覆盖
	 * @param <T> BaseMetadata
	 */
	@Override
    public <T extends BaseMetadata> void correctSchemaFromJDBC(T meta, String catalog, String schema, boolean override){
        super.correctSchemaFromJDBC(meta, catalog, schema, override);
    }
	@Override
	public <T extends BaseMetadata> void correctSchemaFromJDBC(T meta, String catalog, String schema){
		super.correctSchemaFromJDBC(meta, catalog, schema);
	}

	/**
	 * 在调用jdbc接口前处理业务中的catalog, schema, 部分数据库(如mysql)业务系统与dbc标准可能不一致根据实际情况处理<br/>
	 * @param catalog catalog
	 * @param schema schema
	 * @return String[]
	 */
	@Override
	public String[] correctSchemaFromJDBC(String catalog, String schema){
		return super.correctSchemaFromJDBC(catalog, schema);
	}

    /**
     * insert[命令执行后]
     * insert执行后 通过KeyHolder获取主键值赋值给data
     * @param random log标记
     * @param data data
     * @param keyholder  keyholder
     * @return boolean
     */
    @Override
    public boolean identity(DataRuntime runtime, String random, Object data, ConfigStore configs, KeyHolder keyholder){
        return super.identity(runtime, random, data, configs, keyholder);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据长度列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataLength(DataRuntime runtime, TypeMetadata meta){
        return super.columnMetadataLength(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字有效位数列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataPrecision(DataRuntime runtime, TypeMetadata meta){
        return super.columnMetadataPrecision(runtime, meta);
    }

    /**
     * column[结果集封装]<br/>(方法1)<br/>
     * 元数据数字小数位数列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta TypeMetadata
     * @return String
     */
    @Override
    public String columnMetadataScale(DataRuntime runtime, TypeMetadata meta){
        return super.columnMetadataScale(runtime, meta);
    }
    public String insertFoot(ConfigStore configs, LinkedHashMap<String, Column> columns){
        return super.insertFoot(configs, columns);
    }
    /**
     *
     * column[结果集封装-子流程](方法2)<br/>
     * 方法(2)表头内部遍历
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column column
     * @param rsm ResultSetMetaData
     * @param index 第几列
     * @return Column
     */

    @Override
    public Column column(DataRuntime runtime, Column column, ResultSetMetaData rsm, int index){
        return super.column(runtime, column, rsm, index);
    }


    /**
     * column[结果集封装]<br/>(方法3)<br/>
     * 有表名的情况下可用<br/>
     * 根据jdbc.datasource.connection.DatabaseMetaData获取指定表的列数据
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param columns columns
     * @param dbmd DatabaseMetaData
     * @param table 表
     * @param pattern 列名称通配符
     * @return LinkedHashMap
     * @param <T> Column
     * @throws Exception 异常
     */

    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception {
        return super.columns(runtime, create, columns, dbmd, table, pattern);
    }


    /**
     * column[结果集封装-子流程](方法3)<br/>
     * 方法(3)内部遍历
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column column
     * @param rs ResultSet
     * @return Column
     */
    @Override
    public Column column(DataRuntime runtime, Column column, ResultSet rs){
        return super.column(runtime, column, rs);
    }


    /**
     * column[结果集封装]<br/>(方法4)<br/>
     * 解析查询结果metadata(0=1)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的, 这一步是否需要新创建
     * @param columns columns
     * @param table 表
     * @param set SqlRowSet由spring封装过的结果集ResultSet
     * @return LinkedHashMap
     * @param <T> Column
     * @throws Exception
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception {
        return super.columns(runtime, create, columns, table, set);
    }

    /**
     * column[结果集封装-子流程](方法4)<br/>
     * 内部遍历<br/>
     * columns(DataRuntime runtime, boolean create, LinkedHashMap columns, Table table, SqlRowSet set)遍历内部<br/>
     * 根据SqlRowSetMetaData获取列属性 jdbc.queryForRowSet(where 1=0)
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column 获取的数据赋值给column如果为空则新创建一个
     * @param rsm 通过spring封装过的SqlRowSet获取的SqlRowSetMetaData
     * @param index 第几列
     * @return Column
     */
    @Override
    public Column column(DataRuntime runtime, Column column, SqlRowSetMetaData rsm, int index){
        return super.column(runtime, column, rsm, index);
    }

    /**
     * query[结果集封装-子流程]
     * 封装查询结果行, 在外层遍历中修改rs下标
     * @param system 系统表不检测列属性
     * @param runtime  runtime
     * @param metadatas metadatas
     * @param rs jdbc返回结果
     * @return DataRow
     */
    @Override
    public DataRow row(boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ConfigStore configs, ResultSet rs){
        return super.row(system, runtime, metadatas, configs, rs);
    }

    /**
     * 内置函数 多种数据库兼容时需要
     * @param value SQL_BUILD_IN_VALUE
     * @return String
     */
    @Override
    public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value){
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
    protected String dummy(){
        return super.dummy();
    }
    /* *****************************************************************************************************************
     *
     * 														具体数据库
     *
     *  ***************************************************************************************************************/
}
