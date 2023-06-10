 
package org.anyline.data.jdbc.mssql;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.adapter.init.SQLAdapter;
import org.anyline.data.entity.*;
import org.anyline.data.run.Run;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.LogUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

/**
 * 2005(9.0)及以上版本
 */
@Repository("anyline.data.jdbc.adapter.mssql") 
public class MSSQLAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {

	public DatabaseType type(){
		return DatabaseType.MSSQL; 
	}
	public String version(){return "2005";}

	@Value("${anyline.data.jdbc.delimiter.mssql:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public MSSQLAdapter(){
		super();
		delimiterFr = "[";
		delimiterTo = "]";
		for (MSSQLColumnTypeAlias alias: MSSQLColumnTypeAlias.values()){
			types.put(alias.name(), alias.standard());
		}
	}


	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/

	/**
	 * 查询SQL
	 * Run 反转调用
	 * @param run  run
	 * @return String
	 */
	@Override
	public String parseFinalQuery(Run run){
		StringBuilder builder = new StringBuilder();
		String cols = run.getQueryColumns();
		PageNavi navi = run.getPageNavi();
		String sql = run.getBaseQuery();
		OrderStore orders = run.getOrderStore();
		int first = 0;
		int last = 0;
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
			// 2005 及以上
			if(BasicUtil.isEmpty(order)){
				order = "ORDER BY "+ ConfigTable.DEFAULT_PRIMARY_KEY;
			}
			builder.append("SELECT "+cols+" FROM( \n");
			builder.append("SELECT _TAB_I.* ,ROW_NUMBER() OVER(")
					.append(order)
					.append(") AS PAGE_ROW_NUMBER_ \n");
			builder.append("FROM( \n");
			builder.append(sql);
			builder.append(") AS _TAB_I \n");
			builder.append(") AS _TAB_O WHERE PAGE_ROW_NUMBER_ BETWEEN "+(first+1)+" AND "+(last+1));
		}
		return builder.toString();

	}
	/**
	 * 根据DataSet创建批量INSERT RunPrepare
	 * 2000版本单独处理  insert into tab(nm) select 1 union all select 2
	 * @param template JdbcTemplate
	 * @param run run
	 * @param dest 表 如果不指定则根据set解析
	 * @param set 集合
	 * @param keys 需插入的列
	 */
	@Override
	public void createInserts(JdbcTemplate template, Run run, String dest, DataSet set, List<String> keys){
		super.createInserts(template, run, dest, set, keys);
	}

	/**
	 * 根据Collection创建批量INSERT RunPrepare
	 * 2000版本单独处理  insert into tab(nm) select 1 union all select 2
	 * @param template JdbcTemplate
	 * @param run run
	 * @param dest 表 如果不指定则根据set解析
	 * @param list 集合
	 * @param keys 需插入的列
	 */
	@Override
	public void createInserts(JdbcTemplate template, Run run, String dest, Collection list,  List<String> keys){
		super.createInserts(template, run, dest, list, keys);
	}

	@Override 
	public String parseExists(Run run){
		String sql = "IF EXISTS(\n" + run.getBuilder().toString() +"\n) SELECT 1 AS IS_EXISTS ELSE SELECT 0 AS IS_EXISTS"; 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	}
	@Override
	public String concat(String ... args){
		return concatAdd(args);
	}




	/* *****************************************************************************************************************
	 *
	 * 													metadata
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
	 ******************************************************************************************************************/

	@Override
	public void checkSchema(Connection con, Table table){
		try {
			if (null == table.getCatalog()) {
				table.setCatalog(con.getCatalog());
			}
			if (null == table.getSchema()) {
				//table.setSchema(con.getSchema());
				table.setSchema("dbo");
			}
		}catch (Exception e){
		}
		table.setCheckSchemaTime(new Date());
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)
	 * List<String> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types)
	 * <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends Table>LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> comments(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryTableRunSQL(catalog, schema, pattern, types);
	}

	/**
	 * 查询表备注
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<String> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT TBS.NAME AS TABLE_NAME ,DS.VALUE AS TABLE_COMMENT\n");
		builder.append("FROM SYS.EXTENDED_PROPERTIES DS\n");
		builder.append("LEFT JOIN SYS.SYSOBJECTS TBS ON DS.MAJOR_ID=TBS.ID \n");
		builder.append("WHERE  DS.MINOR_ID=0 \n");
		if(BasicUtil.isNotEmpty(objectName(pattern))){
			builder.append("TBS.NAME = '").append(pattern).append("'");
		}
		sqls.add(builder.toString());
		return sqls;
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.tables(index, create, catalog, schema, tables, set);
	}
	@Override
	public <T extends Table>LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(create, tables, dbmd, catalog, schema, pattern, types);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types);
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryMasterTableRunSQL(catalog, schema, pattern, types);
	}

	/**
	 * 从jdbc结果中提取表结构
	 * ResultSet set = con.getMetaData().getTables()
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param dbmd DatabaseMetaData
	 * @param pattern pattern
	 * @param types types
	 * @return List
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.mtables(create, tables, dbmd, catalog, schema, pattern, types);
	}


	/**
	 * 从上一步生成的SQL查询结果中 提取表结构
	 * @param index 第几条SQL
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.mtables(index, create, catalog, schema, tables, set);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types);
	 * List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name);
	 * List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags);
	 * <T extends PartitionTable> LinkedHashMap<String, T> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;
	 * <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean create, LinkedHashMap<String, T> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询分区表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryPartitionTableRunSQL(catalog, schema, pattern, types);
	}
	@Override
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name) throws Exception{
		return super.buildQueryPartitionTableRunSQL(master, tags, name);
	}
	@Override
	public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags) throws Exception{
		return super.buildQueryPartitionTableRunSQL(master, tags);
	}

	/**
	 *  根据查询结果集构造Table
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.ptables(total, index, create, master, catalog, schema, tables, set);
	}

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param dbmd DatabaseMetaData
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean create, LinkedHashMap<String, T> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception{
		return super.ptables(create, tables, dbmd, catalog, schema, master);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryColumnRunSQL(Table table, boolean metadata);
	 * <T extends Column> LinkedHashMap<String, T> columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(true:1=0,false:查询系统表)
	 * @return sql
	 */
	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(builder, table);
			builder.append(" WHERE 1=0");
		}else{
			builder.append("SELECT A.NAME AS TABLE_NAME,C.NAME AS COLUMN_NAME, B.VALUE COLUMN_COMMENT , C.PRECISION, C.SCALE, C.COLLATION_NAME, C.IS_NULLABLE, C.MAX_LENGTH  \n");
			builder.append("FROM SYSOBJECTS A \n");
			builder.append("LEFT JOIN  SYS.COLUMNS C ON A.ID = C.OBJECT_ID\n");
			builder.append("LEFT JOIN SYS.EXTENDED_PROPERTIES B ON B.MAJOR_ID = A.ID AND B.MINOR_ID = C.COLUMN_ID\n");
			if (null != table) {
				builder.append("WHERE A.NAME='").append(objectName(table.getName())).append("'");
			}
		}
		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		return super.columns(index, create, table, columns, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		return super.columns(create, columns, table, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		return super.columns(create, columns, dbmd, table, pattern);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryTagRunSQL(Table table, boolean metadata);
	 * <T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception;
	 * <T extends Tag> LinkedHashMap<String, T> tags(boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception;
	 * <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/
	/**
	 *
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryTagRunSQL(table, metadata);
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception{
		return super.tags(index, create, table, tags, set);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception{
		return super.tags(create, table, tags, set);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		return super.tags(create, tags, dbmd, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryIndexRunSQL(Table table, boolean metadata);
	 * <T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception;
	 * <T extends Index> LinkedHashMap<String, T> indexs(boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception;
	 * <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列
	 * @param table 表
	 * @param name name
	 * @return sql
	 */
	@Override
	public List<String> buildQueryIndexRunSQL(Table table, String name){
		return super.buildQueryIndexRunSQL(table, name);
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception{
		return super.indexs(index, create, table, indexs, set);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception{
		return super.indexs(create, table, indexs, set);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception{
		return super.indexs(create, indexs, dbmd, table, unique, approximate);
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryConstraintRunSQL(Table table, boolean metadata);
	 * LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception;
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryConstraintRunSQL(table, metadata);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(int index , boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set) throws Exception{

		return super.constraints(index, create, table, constraints, set);
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception{
		return super.constraints(create, table, constraints, set);
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception{
		return super.constraints(create, table, constraints, set);
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
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildCreateRunSQL(Table table);
	 * String buildCreateCommentRunSQL(Table table);
	 * List<String> buildAlterRunSQL(Table table)
	 * List<String> buildAlterRunSQL(Table table, Collection<Column> columns);
	 * String buildRenameRunSQL(Table table);
	 * String buildChangeCommentRunSQL(Table table);
	 * String buildDropRunSQL(Table table);
	 * StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * StringBuilder primary(StringBuilder builder, Table table)
	 * StringBuilder comment(StringBuilder builder, Table table)
	 * StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<String> buildCreateRunSQL(Table table) throws Exception{
		List<String> sqls = super.buildCreateRunSQL(table);
		//如果有备注 单独生成 super中已包含
		/*String sql = buildChangeCommentRunSQL(table);
		if(BasicUtil.isNotEmpty(sql)) {
			sqls.add(sql);
		}*/
		//列备注 super中已包含
		/*for(Column col:table.getColumns().values()){
			String cmt = col.getComment();
			if(BasicUtil.isNotEmpty(cmt)){
				sqls.add(buildChangeCommentRunSQL(col));
			}
		}*/
		return sqls;
	}

	@Override
	public List<String> buildAlterRunSQL(Table table) throws Exception{
		return super.buildAlterRunSQL(table);
	}

	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	public List<String> buildAlterRunSQL(Table table, Collection<Column> columns) throws Exception{
		return super.buildAlterRunSQL(table, columns);
	}
	/**
	 * 修改表名
	 * EXEC SP_RENAME 'A', 'B'
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("EXEC SP_RENAME '").append(table.getName()).append("', '").append(table.getUpdate().getName()).append("'");
		return builder.toString();
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public String buildCreateCommentRunSQL(Table table) throws Exception {
		String comment = table.getComment();
		if(BasicUtil.isEmpty(comment)){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("EXEC sys.sp_addextendedproperty @name=N'MS_Description'");
		builder.append(",@value=N'").append(comment).append("'");
		builder.append(",@level0type=N'SCHEMA'");
		builder.append(",@level0name=N'").append(table.getSchema()).append("'");
		builder.append(",@level1type=N'TABLE'");
		builder.append(",@level1name=N'").append(table.getName()).append("'");
		return builder.toString();
	}

	@Override
	public String buildChangeCommentRunSQL(Table table) throws Exception{
		String comment = table.getComment();
		if(BasicUtil.isEmpty(comment)){
			return null;
		}
		StringBuilder builder = new StringBuilder();

		builder.append("EXEC sys.sp_updateextendedproperty @name=N'MS_Description'");
		builder.append(",@value=N'").append(comment).append("'");
		builder.append(",@level0type=N'SCHEMA'");
		builder.append(",@level0name=N'").append(table.getSchema()).append("'");
		builder.append(",@level1type=N'TABLE'");
		builder.append(",@level1name=N'").append(table.getName()).append("'");

		return builder.toString();
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Table table) throws Exception{
		return super.buildDropRunSQL(table);
	}


	@Override
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists){
		if(exists) {
			// 仅drop时支持
			return super.checkTableExists(builder, exists);
		}else {
			// create时不支持
			return builder;
		}
	}


	/**
	 * 主键
	 * CONSTRAINT [PK_BS_DEV] PRIMARY KEY (ID  ASC)
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		List<Column> pks = table.primarys();
		if(pks.size()>0){
			builder.append(",CONSTRAINT ").append("PK_").append(table.getName()).append(" PRIMARY KEY (");
			boolean first = true;
			for(Column pk:pks){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				String order = pk.getOrder();
				if(BasicUtil.isNotEmpty(order)){
					builder.append(" ").append(order);
				}
				first = false;
			}
			builder.append(")");
		}
		return builder;
	}

	/**
	 * 备注
	 * 需要单独生成添加注释的SQL
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Table table){
		return builder;
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(StringBuilder builder, Table table){
		return super.name(builder, table);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildCreateRunSQL(View view);
	 * String buildCreateCommentRunSQL(View view);
	 * List<String> buildAlterRunSQL(View view);
	 * String buildRenameRunSQL(View view);
	 * String buildChangeCommentRunSQL(View view);
	 * String buildDropRunSQL(View view);
	 * StringBuilder checkViewExists(StringBuilder builder, boolean exists)
	 * StringBuilder primary(StringBuilder builder, View view)
	 * StringBuilder comment(StringBuilder builder, View view)
	 * StringBuilder name(StringBuilder builder, View view)
	 ******************************************************************************************************************/


	@Override
	public List<String> buildCreateRunSQL(View view) throws Exception{
		return super.buildCreateRunSQL(view);
	}

	@Override
	public String buildCreateCommentRunSQL(View view) throws Exception{
		return super.buildCreateCommentRunSQL(view);
	}


	@Override
	public List<String> buildAlterRunSQL(View view) throws Exception{
		return super.buildAlterRunSQL(view);
	}
	/**
	 * 修改视图名
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param view 视图
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(View view) throws Exception{
		return super.buildRenameRunSQL(view);
	}

	@Override
	public String buildChangeCommentRunSQL(View view) throws Exception{
		return super.buildChangeCommentRunSQL(view);
	}
	/**
	 * 删除视图
	 * @param view 视图
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(View view) throws Exception{
		return super.buildDropRunSQL(view);
	}

	/**
	 * 创建或删除视图时检测视图是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(StringBuilder builder, boolean exists){
		return super.checkViewExists(builder, exists);
	}

	/**
	 * 备注 不支持创建视图时带备注的 在子视图中忽略
	 * @param builder builder
	 * @param view 视图
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, View view){
		return super.comment(builder, view);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildCreateRunSQL(MasterTable table);
	 * String buildCreateCommentRunSQL(MasterTable table)
	 * List<String> buildAlterRunSQL(MasterTable table);
	 * String buildDropRunSQL(MasterTable table);
	 * String buildRenameRunSQL(MasterTable table);
	 * String buildChangeCommentRunSQL(MasterTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<String>  buildCreateRunSQL(MasterTable table) throws Exception{
		return super.buildCreateRunSQL(table);
	}
	@Override
	public List<String> buildAlterRunSQL(MasterTable table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	@Override
	public String buildDropRunSQL(MasterTable table) throws Exception{
		return super.buildDropRunSQL(table);
	}
	@Override
	public String buildRenameRunSQL(MasterTable table) throws Exception{
		return super.buildRenameRunSQL(table);
	}
	@Override
	public String buildChangeCommentRunSQL(MasterTable table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * String buildCreateRunSQL(PartitionTable table);
	 * List<String> buildAlterRunSQL(PartitionTable table);
	 * String buildDropRunSQL(PartitionTable table);
	 * String buildRenameRunSQL(PartitionTable table);
	 * String buildChangeCommentRunSQL(PartitionTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<String>  buildCreateRunSQL(PartitionTable table) throws Exception{
		return super.buildCreateRunSQL(table);
	}
	@Override
	public List<String> buildAlterRunSQL(PartitionTable table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	@Override
	public String buildDropRunSQL(PartitionTable table) throws Exception{
		return super.buildDropRunSQL(table);
	}
	@Override
	public String buildRenameRunSQL(PartitionTable table) throws Exception{
		return super.buildRenameRunSQL(table);
	}
	@Override
	public String buildChangeCommentRunSQL(PartitionTable table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * String alterColumnKeyword()
	 * List<String> buildAddRunSQL(Column column, boolean slice)
	 * List<String> buildAddRunSQL(Column column)
	 * List<String> buildAlterRunSQL(Column column, boolean slice)
	 * List<String> buildAlterRunSQL(Column column)
	 * String buildDropRunSQL(Column column, boolean slice)
	 * String buildDropRunSQL(Column column)
	 * String buildRenameRunSQL(Column column)
	 * List<String> buildChangeTypeRunSQL(Column column)
	 * String buildChangeDefaultRunSQL(Column column)
	 * String buildChangeNullableRunSQL(Column column)
	 * String buildChangeCommentRunSQL(Column column)
	 * String buildCreateCommentRunSQL(Column column)
	 * StringBuilder define(StringBuilder builder, Column column)
	 * StringBuilder type(StringBuilder builder, Column column)
	 * boolean isIgnorePrecision(Column column);
	 * boolean isIgnoreScale(Column column);
	 * Boolean checkIgnorePrecision(String datatype);
	 * Boolean checkIgnoreScale(String datatype);
	 * StringBuilder nullable(StringBuilder builder, Column column)
	 * StringBuilder charset(StringBuilder builder, Column column)
	 * StringBuilder defaultValue(StringBuilder builder, Column column)
	 * StringBuilder increment(StringBuilder builder, Column column)
	 * StringBuilder onupdate(StringBuilder builder, Column column)
	 * StringBuilder position(StringBuilder builder, Column column)
	 * StringBuilder comment(StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/
	@Override
	public String alterColumnKeyword(){
		return super.alterColumnKeyword();
	}

	/**
	 * 添加新列
	 * ALTER TABLE TAB_A ADD USER_NAME VARCHAR(10)
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<String> buildAddRunSQL(Column column, boolean slice) throws Exception{
		List<String> sqls = new ArrayList<>();
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		if(!slice) {
			Table table = column.getTable();
			builder.append("ALTER TABLE ");
			name(builder, table);
		}
		builder.append(" ADD ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, column);
		sqls.add(builder.toString());
		sqls.add(buildCreateCommentRunSQL(column));
		return sqls;
	}



	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sqls
	 */
	@Override
	public List<String> buildAlterRunSQL(Column column, boolean slice) throws Exception{
		return super.buildAlterRunSQL(column, slice);
	}
	@Override
	public List<String> buildAlterRunSQL(Column column) throws Exception{
		return buildAlterRunSQL(column, false);
	}

	

	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Column column, boolean slice) throws Exception{
		return super.buildDropRunSQL(column, slice);
	}


	/**
	 * 修改列名
	 * EXEC sp_rename '表名.列名', '新列名', 'COLUMN'
	 * @param column 列
	 * @return sql
	 */
	@Override
	public String buildRenameRunSQL(Column column) throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("EXEC SP_RENAME '").append(column.getTableName()).append(".").append(column.getName()).append("' , '").append(column.getUpdate().getName()).append("','COLUMN' ");
		return builder.toString();
	}

	/**
	 * 修改数据类型
	 * ALTER TABLE T ALTER COLUMN C VARCHAR (2);
	 * @param column 列
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Column column) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		Column update = column.getUpdate();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable());
		builder.append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" ");
		type(builder, update);
		nullable(builder, update);
		sqls.add(builder.toString());
		return sqls;
	}
	/**
	 * 修改默认值
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Column column) throws Exception{
		return super.buildChangeDefaultRunSQL(column);
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE T ALTER COLUMN C VARCHAR (20) NOT NULL;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Column column) throws Exception{
		Column update = column.getUpdate();
		int nullable = update.isNullable();
		if(nullable == -1){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable()).append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		type(builder, update);
		if(nullable == 0){
			builder.append(" NOT");
		}
		builder.append(" NULL");
		return builder.toString();
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public String buildCreateCommentRunSQL(Column column) throws Exception {
		String comment = column.getComment();
		if(BasicUtil.isEmpty(comment)){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		String schema = column.getSchema();
		if(BasicUtil.isEmpty(schema)){
			schema = column.getTable().getSchema();
		}

		builder.append("EXEC sys.sp_addextendedproperty @name=N'MS_Description'");
		builder.append(",@value=N'").append(comment).append("'");
		builder.append(",@level0type=N'SCHEMA'");
		builder.append(",@level0name=N'").append(schema).append("'");
		builder.append(",@level1type=N'TABLE'");
		builder.append(",@level1name=N'").append(column.getTableName()).append("'");
		builder.append(",@level2type=N'COLUMN'");
		builder.append(",@level2name=N'").append(column.getName()).append("'");

		return builder.toString();
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
	public String buildChangeCommentRunSQL(Column column) throws Exception{
		String comment = null;
		if(null != column.getUpdate()){
			comment = column.getUpdate().getComment();
		}else {
			comment = column.getComment();
		}
		if(BasicUtil.isEmpty(comment)){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		String schema = column.getSchema();
		if(BasicUtil.isEmpty(schema)){
			schema = column.getTable().getSchema();
		}
		builder.append("EXEC sys.sp_updateextendedproperty @name=N'MS_Description'");
		builder.append(",@value=N'").append(comment).append("'");
		builder.append(",@level0type=N'SCHEMA'");
		builder.append(",@level0name=N'").append(schema).append("'");
		builder.append(",@level1type=N'TABLE'");
		builder.append(",@level1name=N'").append(column.getTableName()).append("'");
		builder.append(",@level2type=N'COLUMN'");
		builder.append(",@level2name=N'").append(column.getName()).append("'");
		return builder.toString();
	}


	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(StringBuilder builder, Column column){
		return super.define(builder, column);
	}
	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder type(StringBuilder builder, Column column){
		return super.type(builder, column);
	}

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder nullable(StringBuilder builder, Column column){
		return super.nullable(builder, column);
	}
	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder charset(StringBuilder builder, Column column){
		return super.charset(builder, column);
	}
	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder defaultValue(StringBuilder builder, Column column){
		return super.defaultValue(builder, column);
	}

	/**
	 * 递增列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder increment(StringBuilder builder, Column column){
		if(column.isAutoIncrement() == 1){
			builder.append(" IDENTITY(").append(column.getIncrementSeed()).append(",").append(column.getIncrementStep()).append(")");
		}
		return builder;
	}



	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder onupdate(StringBuilder builder, Column column){
		return super.onupdate(builder, column);
	}

	/**
	 * 位置
	 *
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder position(StringBuilder builder, Column column){
		return super.position(builder, column);
	}
	/**
	 * 备注
	 *
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Column column){
		return super.comment(builder, column);
	}


	/**
	 * 创建或删除列时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkColumnExists(StringBuilder builder, boolean exists){
		return super.checkColumnExists(builder, exists);
	}
	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * String buildAddRunSQL(Tag tag);
	 * List<String> buildAlterRunSQL(Tag tag);
	 * String buildDropRunSQL(Tag tag);
	 * String buildRenameRunSQL(Tag tag);
	 * String buildChangeDefaultRunSQL(Tag tag);
	 * String buildChangeNullableRunSQL(Tag tag);
	 * String buildChangeCommentRunSQL(Tag tag);
	 * List<String> buildChangeTypeRunSQL(Tag tag);
	 * StringBuilder checkTagExists(StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Tag tag) throws Exception{
		return super.buildAddRunSQL(tag);
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<String> buildAlterRunSQL(Tag tag) throws Exception{
		return super.buildAlterRunSQL(tag);
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Tag tag) throws Exception{
		return super.buildDropRunSQL(tag);
	}


	/**
	 * 修改标签名
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Tag tag)  throws Exception{
		return super.buildRenameRunSQL(tag);
	}

	/**
	 * 修改默认值
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Tag tag) throws Exception{
		return super.buildChangeDefaultRunSQL(tag);
	}

	/**
	 * 修改非空限制
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Tag tag) throws Exception{
		return super.buildChangeNullableRunSQL(tag);
	}
	/**
	 * 修改备注
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Tag tag) throws Exception{
		return super.buildChangeCommentRunSQL(tag);
	}

	/**
	 * 修改数据类型
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Tag tag) throws Exception{
		return super.buildChangeTypeRunSQL(tag);
	}

	/**
	 * 创建或删除标签时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkTagExists(StringBuilder builder, boolean exists){
		return super.checkTagExists(builder, exists);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * String buildAddRunSQL(PrimaryKey primary) throws Exception
	 * List<String> buildAlterRunSQL(PrimaryKey primary) throws Exception
	 * String buildDropRunSQL(PrimaryKey primary) throws Exception
	 * String buildRenameRunSQL(PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(PrimaryKey primary) throws Exception{
		StringBuilder builder = new StringBuilder();
		Map<String,Column> columns = primary.getColumns();
		if(columns.size()>0) {
			builder.append("ALTER TABLE ");
			name(builder, primary.getTable());
			builder.append(" ADD PRIMARY KEY (");
			boolean first = true;
			for(Column column:columns.values()){
				if(!first){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
				first = false;
			}
			builder.append(")");

		}
		return builder.toString();
	}
	/**
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param primary 主键
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(PrimaryKey primary) throws Exception{
		return super.buildAlterRunSQL(primary);
	}

	/**
	 * 删除主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(PrimaryKey primary) throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, primary.getTable());
		builder.append(" DROP CONSTRAINT ");
		SQLUtil.delimiter(builder, primary.getName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
	}
	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(PrimaryKey primary) throws Exception{
		return super.buildRenameRunSQL(primary);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/

	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return String
	 */
	public String buildAddRunSQL(ForeignKey foreign) throws Exception{
		return super.buildAddRunSQL(foreign);
	}
	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return List
	 */
	public List<String> buildAlterRunSQL(ForeignKey foreign) throws Exception{
		return super.buildAlterRunSQL(foreign);
	}

	/**
	 * 删除外键
	 * @param foreign 外键
	 * @return String
	 */
	public String buildDropRunSQL(ForeignKey foreign) throws Exception{
		return super.buildDropRunSQL(foreign);
	}

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param foreign 外键
	 * @return String
	 */
	public String buildRenameRunSQL(ForeignKey foreign) throws Exception{
		return super.buildRenameRunSQL(foreign);
	}


	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryPrimaryRunSQL(Table table) throws Exception
	 * PrimaryKey primary(int index, Table table, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryPrimaryRunSQL(Table table) throws Exception{
		List<String> list = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT  *  FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE \n");
		builder.append("WHERE TABLE_NAME='").append(objectName(table.getName())).append("'");
		String catalog = table.getCatalog();
		if(BasicUtil.isNotEmpty(catalog)){
			builder.append("\nAND TABLE_CATALOG = '").append(catalog).append("'");
		}
		String schema = table.getSchema();
		if(BasicUtil.isNotEmpty(schema)){
			builder.append("\nAND TABLE_SCHEMA = '").append(schema).append("'");
		}
		builder.append("\nORDER BY ORDINAL_POSITION");
		list.add(builder.toString());
		return list;
	}
	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	@Override
	public PrimaryKey primary(int index, Table table, DataSet set) throws Exception{
		PrimaryKey primary = table.getPrimaryKey();
		for(DataRow row:set){
			if(null == primary){
				primary = new PrimaryKey();
				primary.setName(row.getString("CONSTRAINT_NAME"));
				primary.setTable(table);
			}
			String col = row.getString("COLUMN_NAME");
			Column column = primary.getColumn(col);
			if(null == column){
				column = new Column(col);
			}
			column.setTable(table);
			column.setPosition(row.getInt("ORDINAL_POSITION",0));
			primary.addColumn(column);
		}
		return primary;
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<String> buildQueryForeignsRunSQL(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的外键
	 * @param table 表
	 * @return sqls
	 */
	public List<String> buildQueryForeignsRunSQL(Table table) throws Exception{
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT F.NAME AS CONSTRAINT_NAME, OBJECT_NAME(F.PARENT_OBJECT_ID) AS TABLE_NAME, COL_NAME(FC.PARENT_OBJECT_ID, FC.PARENT_COLUMN_ID) AS COLUMN_NAME,");
		builder.append(" OBJECT_NAME(F.REFERENCED_OBJECT_ID) AS REFERENCED_TABLE_NAME, COL_NAME(FC.REFERENCED_OBJECT_ID, FC.REFERENCED_COLUMN_ID) AS REFERENCED_COLUMN_NAME \n");
		builder.append("FROM SYS.FOREIGN_KEYS AS F INNER JOIN SYS.FOREIGN_KEY_COLUMNS AS FC ON F.OBJECT_ID = FC.CONSTRAINT_OBJECT_ID \n");
		if(null != table){
			builder.append(" AND OBJECT_NAME(F.PARENT_OBJECT_ID) = '").append(table.getName()).append("'\n");
		}
		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRunSQL 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception{
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
			foreign.addColumn(new Column(row.getString("COLUMN_NAME")).setReference(row.getString("REFERENCED_COLUMN_NAME")).setPosition(row.getInt("ORDINAL_POSITION", 0)));

		}
		return foreigns;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * String buildAddRunSQL(Index index) throws Exception
	 * List<String> buildAlterRunSQL(Index index) throws Exception
	 * String buildDropRunSQL(Index index) throws Exception
	 * String buildRenameRunSQL(Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Index index) throws Exception{
		return super.buildAddRunSQL(index);
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Index index) throws Exception{
		return super.buildAlterRunSQL(index);
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Index index) throws Exception{
		return super.buildDropRunSQL(index);
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Index index) throws Exception{
		return super.buildRenameRunSQL(index);
	}
	/**
	 * 索引备注
	 * @param builder
	 * @param index
	 */
	public void comment(StringBuilder builder, Index index){
		super.comment(builder, index);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * String buildAddRunSQL(Constraint constraint) throws Exception
	 * List<String> buildAlterRunSQL(Constraint constraint) throws Exception
	 * String buildDropRunSQL(Constraint constraint) throws Exception
	 * String buildRenameRunSQL(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Constraint constraint) throws Exception{
		return super.buildAddRunSQL(constraint);
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Constraint constraint) throws Exception{
		return super.buildAlterRunSQL(constraint);
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Constraint constraint) throws Exception{
		return super.buildDropRunSQL(constraint);
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Constraint constraint) throws Exception{
		return super.buildRenameRunSQL(constraint);
	}


	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(Column column)
	 *  boolean isNumberColumn(Column column)
	 * boolean isCharColumn(Column column)
	 * String value(Column column, SQL_BUILD_IN_VALUE value)
	 * String type(String type)
	 * String type2class(String type)
	 ******************************************************************************************************************/

	@Override
	public boolean isBooleanColumn(Column column) {
		return super.isBooleanColumn(column);
	}
	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public  boolean isNumberColumn(Column column){
		return super.isNumberColumn(column);
	}

	@Override
	public boolean isCharColumn(Column column) {
		return super.isCharColumn(column);
	}
	/**
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(Column column, SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "getdate()";
		}
		return null;
	}




}
