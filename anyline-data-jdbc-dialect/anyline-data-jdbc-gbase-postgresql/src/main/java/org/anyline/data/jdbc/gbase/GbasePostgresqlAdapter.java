 
package org.anyline.data.jdbc.gbase;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.adapter.init.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;
/**
 * 参考 PostgresqlAdapter
 */
@Repository("anyline.data.jdbc.adapter.gbase")
public class GbasePostgresqlAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	
	public DatabaseType type(){
		return DatabaseType.GBase;
	}
	@Value("${anyline.data.jdbc.delimiter.gbase:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public GbasePostgresqlAdapter(){
		super();
		delimiterFr = "\"";
		delimiterTo = "\"";
		for (GbaseColumnTypeAlias alias : GbaseColumnTypeAlias.values()) {
			types.put(alias.name(), alias.standard());
		}
	}
	/* *****************************************************************************************************
	 *
	 * 											DML
	 *
	 * ****************************************************************************************************/
	@Override 
	public String parseFinalQuery(Run run){
		String sql = run.getBaseQuery(); 
		String cols = run.getQueryColumns(); 
		if(!"*".equals(cols)){
			String reg = "(?i)^select[\\s\\S]+from"; 
			sql = sql.replaceAll(reg,"SELECT "+cols+" FROM "); 
		} 
		OrderStore orders = run.getOrderStore(); 
		if(null != orders){
			sql += orders.getRunText(getDelimiterFr()+getDelimiterTo());
		} 
		PageNavi navi = run.getPageNavi(); 
		if(null != navi){
			long limit = navi.getLastRow() - navi.getFirstRow() + 1; 
			if(limit < 0){
				limit = 0; 
			} 
			sql += " LIMIT " + limit + " OFFSET " + navi.getFirstRow(); 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
 
	public String concat(String ... args){
		return concatOr(args);
	}



	/* *****************************************************************************************************************
	 *
	 * 														DML
	 *
	 *  *****************************************************************************************************************/

	/**
	 * 查询序列cur 或 next value
	 * @param next  是否生成返回下一个序列 false:cur true:next
	 * @param names 序列名
	 * @return String
	 */
	public List<Run> buildQuerySequence(boolean next, String ... names){
		List<Run> runs = new ArrayList<>();
		String key = "CURRVAL";
		if(next){
			key = "NEXTVAL";
		}
		if(null != names && names.length>0) {
			Run run = new SimpleRun();
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
	 * function         : 函数
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)
	 * List<Run> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types)
	 * <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> comments(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * List<Run> buildQueryDDLRunSQL(Table table) throws Exception
	 * public List<String> ddl(int index, Table table, List<String> ddls, DataSet set)
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
	public List<Run> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryTableRunSQL(catalog, schema, pattern, types);
	}

	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryTableCommentRunSQL(catalog, schema, pattern, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.tables(index, create, catalog, schema, tables, set);
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(create, tables, runtime, catalog, schema, pattern, types);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types);
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception;
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception;
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
	public List<Run> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryMasterTableRunSQL(catalog, schema, pattern, types);
	}

	/**
	 * 从jdbc结果中提取表结构
	 * ResultSet set = con.getMetaData().getTables()
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param runtime runtime
	 * @return List
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.mtables(create, tables, runtime, catalog, schema, pattern, types);
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
	 * List<Run> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types);
	 * List<Run> buildQueryPartitionTableRunSQL(MasterTable table, Map<String,Object> tags);
	 * LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable table, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;
	 * LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable table, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception;
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
	public List<Run> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryPartitionTableRunSQL(catalog, schema, pattern, types);
	}
	@Override
	public List<Run> buildQueryPartitionTableRunSQL(MasterTable table, Map<String,Object> tags) throws Exception{
		return super.buildQueryPartitionTableRunSQL(table, tags);
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
	 * @param runtime runtime
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean create, LinkedHashMap<String, T> tables, DataRuntime runtime, String catalog, String schema, MasterTable master) throws Exception{
		return super.ptables(create, tables, runtime, catalog, schema, master);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryColumnRunSQL(Table table, boolean metadata);
	 * <T extends Column> LinkedHashMap<String, T> columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception;
	 * <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, DataRuntime runtime, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(true:1=0,false:查询系统表)
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(builder, table);
			builder.append(" WHERE 1=0");
		}else{
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			builder.append("SELECT M.* ,FD.DESCRIPTION AS COLUMN_COMMENT FROM INFORMATION_SCHEMA.COLUMNS M\n");
			builder.append("LEFT JOIN PG_CLASS FC ON FC.RELNAME = M.TABLE_NAME\n");
			builder.append("LEFT JOIN PG_DESCRIPTION FD ON FD.OBJOID = FC.OID AND FD.OBJSUBID = M.ORDINAL_POSITION\n");
			builder.append("WHERE 1 = 1\n");
			if(BasicUtil.isNotEmpty(catalog)){
				builder.append(" AND M.TABLE_CATALOG = '").append(catalog).append("'");
			}
			if(BasicUtil.isNotEmpty(schema)){
				builder.append(" AND M.TABLE_SCHEMA = '").append(schema).append("'");
			}
			builder.append(" AND M.TABLE_NAME = '").append(objectName(table.getName())).append("'");
		}
		return runs;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		set.changeKey("UDT_NAME","DATA_TYPE");
		return super.columns(index, create, table, columns, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		return super.columns(create, columns, table, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(boolean create, LinkedHashMap<String, T> columns, DataRuntime runtime, Table table, String pattern) throws Exception{
		return super.columns(create, columns, runtime, table, pattern);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTagRunSQL(Table table, boolean metadata);
	 * <T extends Tag> LinkedHashMap<String, T> tags(int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception;
	 * <T extends Tag> LinkedHashMap<String, T> tags(boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception;
	 * <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DataRuntime runtime, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/
	/**
	 *
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryTagRunSQL(table, metadata);
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRunSQL返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags
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
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean create, LinkedHashMap<String, T> tags, DataRuntime runtime, Table table, String pattern) throws Exception{
		return super.tags(create, tags, runtime, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPrimaryRunSQL(Table table) throws Exception
	 * PrimaryKey primary(int index, Table table, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryPrimaryRunSQL(Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		//test_pk_pkey	| p	| {2,1}	| 	PRIMARY KEY (id, name)
		builder.append("SELECT  m.conname,  pg_get_constraintdef(m.oid, true) AS define\n");
		builder.append("FROM pg_constraint m \n");
		builder.append("LEFT JOIN pg_namespace ns ON m.connamespace = ns.oid \n");
		builder.append("LEFT JOIN pg_class ft ON m.conrelid = ft.oid \n");
		builder.append("WHERE ft.relname = '").append(objectName(table.getName())).append("'");
		String schema = table.getSchema();
		if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND ns.nspname = '").append(schema).append("'");
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public PrimaryKey primary(int index, Table table, DataSet set) throws Exception{
		PrimaryKey primary = null;
		if(set.size()>0){
			DataRow row = set.getRow(0);
			primary = new PrimaryKey();
			//conname 	    |contype	|conkey |  define
			//test_pk_pkey	| p			| {2,1}	| 	PRIMARY KEY (id, name)
			primary.setName(row.getString("conname"));
			String define = row.getString("define");
			String[] cols = RegularUtil.cut(define, "(",")").split(",");
			for(String col:cols){
				Column column = new Column(col.trim().replace("\"", ""));
				column.setTable(table);
				primary.addColumn(column);
			}
		}
		return primary;
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRunSQL(Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的外键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryForeignsRunSQL(Table table) throws Exception{
		return super.buildQueryForeignsRunSQL(table);
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
		return super.foreigns(index, table, foreigns, set);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryIndexRunSQL(Table table, boolean metadata);
	 * <T extends Index> LinkedHashMap<String, T> indexs(int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception;
	 * <T extends Index> LinkedHashMap<String, T> indexs(boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception;
	 * <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DataRuntime runtime, Table table, boolean unique, boolean approximate) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列
	 * @param table 表
	 * @param name name
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryIndexRunSQL(Table table, String name){
		return super.buildQueryIndexRunSQL(table, name);
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs
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
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean create, LinkedHashMap<String, T> indexs, DataRuntime runtime, Table table, boolean unique, boolean approximate) throws Exception{
		return super.indexs(create, indexs, runtime, table, unique, approximate);
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryConstraintRunSQL(Table table, boolean metadata);
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
	public List<Run> buildQueryConstraintRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryConstraintRunSQL(table, metadata);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints
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
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTriggerRunSQL(Table table, List<Trigger.EVENT> events)
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * 查询表上的trigger
	 * @param table 表
	 * @param events INSERT|UPATE|DELETE
	 * @return sqls
	 */

	@Override
	public List<Run> buildQueryTriggerRunSQL(Table table, List<Trigger.EVENT> events) {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT * FROM INFORMATION_SCHEMA.TRIGGERS WHERE 1=1");
		if(null != table){
			String schemae = table.getSchema();
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
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRunSQL 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */

	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception{
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


	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Table table)
	 * List<Run> buildAddCommentRunSQL(Table table);
	 * List<Run> buildAlterRunSQL(Table table)
	 * List<Run> buildAlterRunSQL(Table table, Collection<Column> columns)
	 * List<Run> buildRenameRunSQL(Table table)
	 * List<Run> buildChangeCommentRunSQL(Table table)
	 * List<Run> buildDropRunSQL(Table table)
	 * StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * StringBuilder primary(StringBuilder builder, Table table)
	 * StringBuilder comment(StringBuilder builder, Table table)
	 * StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRunSQL(Table table) throws Exception{
		return super.buildCreateRunSQL(table);
	}


	@Override
	public List<Run> buildAlterRunSQL(Table table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Table table, Collection<Column> columns) throws Exception{
		return super.buildAlterRunSQL(table, columns);
	}
	/**
	 * 修改表名
	 * ALTER TABLE A RENAME TO B;
	 * @param table table
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Table table)  throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" RENAME TO ");
		//去掉catalog schema前缀
		Table update = new Table(table.getUpdate().getName());
		name(builder, update);
		return runs;
	}

	/**
	 * 修改备注
	 * COMMENT ON TABLE T IS 'ABC';
	 * @param table table
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRunSQL(Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("COMMENT ON TABLE ");
			name(builder, table);
			builder.append(" IS '").append(comment).append("'");
		}
		return runs;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAddCommentRunSQL(Table table) throws Exception {
		return buildChangeCommentRunSQL(table);
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Table table) throws Exception{
		return super.buildDropRunSQL(table);
	}


	/**
	 * 创建之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists){
		return super.checkTableExists(builder, exists);
	}


	/**
	 * 主键
	 * CONSTRAINT PK_BS_DEV PRIMARY KEY (ID ASC)
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		List<Column> pks = table.primarys();
		if(pks.size()>0){
			builder.append(",CONSTRAINT ").append("PK_").append(table.getName()).append(" PRIMARY KEY (");
			int idx = 0;
			for(Column pk:pks){
				if(idx > 0){
					builder.append(",");
				}
				SQLUtil.delimiter(builder, pk.getName(), getDelimiterFr(), getDelimiterTo());
				String order = pk.getOrder();
				if(null != order){
					builder.append(" ").append(order);
				}
				idx ++;
			}
			builder.append(")");
		}
		return builder;
	}


	/**
	 * 备注
	 *
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Table table){
		//return super.comment(builder, table);
		//单独添加备注
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
	 * List<Run> buildCreateRunSQL(View view);
	 * List<Run> buildAddCommentRunSQL(View view);
	 * List<Run> buildAlterRunSQL(View view);
	 * List<Run> buildRenameRunSQL(View view);
	 * List<Run> buildChangeCommentRunSQL(View view);
	 * List<Run> buildDropRunSQL(View view);
	 * StringBuilder checkViewExists(StringBuilder builder, boolean exists)
	 * StringBuilder primary(StringBuilder builder, View view)
	 * StringBuilder comment(StringBuilder builder, View view)
	 * StringBuilder name(StringBuilder builder, View view)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRunSQL(View view) throws Exception{
		return super.buildCreateRunSQL(view);
	}

	@Override
	public List<Run> buildAddCommentRunSQL(View view) throws Exception{
		return super.buildAddCommentRunSQL(view);
	}


	@Override
	public List<Run> buildAlterRunSQL(View view) throws Exception{
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
	public List<Run> buildRenameRunSQL(View view) throws Exception{
		return super.buildRenameRunSQL(view);
	}

	@Override
	public List<Run> buildChangeCommentRunSQL(View view) throws Exception{
		return super.buildChangeCommentRunSQL(view);
	}
	/**
	 * 删除视图
	 * @param view 视图
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(View view) throws Exception{
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
	 * List<Run> buildCreateRunSQL(MasterTable table);
	 * List<Run> buildAddCommentRunSQL(MasterTable table)
	 * List<Run> buildAlterRunSQL(MasterTable table);
	 * List<Run> buildDropRunSQL(MasterTable table);
	 * List<Run> buildRenameRunSQL(MasterTable table);
	 * List<Run> buildChangeCommentRunSQL(MasterTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRunSQL(MasterTable table) throws Exception{
		return super.buildCreateRunSQL(table);
	}
	@Override
	public List<Run> buildAlterRunSQL(MasterTable table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	@Override
	public List<Run> buildDropRunSQL(MasterTable table) throws Exception{
		return super.buildDropRunSQL(table);
	}
	@Override
	public List<Run> buildRenameRunSQL(MasterTable table) throws Exception{
		return super.buildRenameRunSQL(table);
	}
	@Override
	public List<Run> buildChangeCommentRunSQL(MasterTable table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(PartitionTable table);
	 * List<Run> buildAlterRunSQL(PartitionTable table);
	 * List<Run> buildDropRunSQL(PartitionTable table);
	 * List<Run> buildRenameRunSQL(PartitionTable table);
	 * List<Run> buildChangeCommentRunSQL(PartitionTable table);
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRunSQL(PartitionTable table) throws Exception{
		return super.buildCreateRunSQL(table);
	}
	@Override
	public List<Run> buildAlterRunSQL(PartitionTable table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	@Override
	public List<Run> buildDropRunSQL(PartitionTable table) throws Exception{
		return super.buildDropRunSQL(table);
	}
	@Override
	public List<Run> buildRenameRunSQL(PartitionTable table) throws Exception{
		return super.buildRenameRunSQL(table);
	}
	@Override
	public List<Run> buildChangeCommentRunSQL(PartitionTable table) throws Exception{
		return super.buildChangeCommentRunSQL(table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * String alterColumnKeyword()
	 * List<Run> buildAddRunSQL(Column column, boolean slice)
	 * List<Run> buildAddRunSQL(Column column)
	 * List<Run> buildAlterRunSQL(Column column, boolean slice)
	 * List<Run> buildAlterRunSQL(Column column)
	 * List<Run> buildDropRunSQL(Column column, boolean slice)
	 * List<Run> buildDropRunSQL(Column column)
	 * List<Run> buildRenameRunSQL(Column column)
	 * List<Run> buildChangeTypeRunSQL(Column column)
	 * List<Run> buildChangeDefaultRunSQL(Column column)
	 * List<Run> buildChangeNullableRunSQL(Column column)
	 * List<Run> buildChangeCommentRunSQL(Column column)
	 * List<Run> buildAddCommentRunSQL(Column column)
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
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Column column, boolean slice) throws Exception{
		return super.buildAddRunSQL(column);
	}


	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return sqls
	 */
	@Override
	public List<Run> buildAlterRunSQL(Column column, boolean slice) throws Exception{
		return super.buildAlterRunSQL(column, slice);
	}
	@Override
	public List<Run> buildAlterRunSQL(Column column) throws Exception{
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
	public List<Run> buildDropRunSQL(Column column, boolean slice) throws Exception{
		return super.buildDropRunSQL(column, slice);
	}

	/**
	 * 修改列名
	 * ALTER TABLE T  RENAME  A  to B ;
	 * @param column column
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Column column) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable(true));
		builder.append(" RENAME ").append(column.getName()).append(" TO ").append(column.getUpdate().getName());
		column.setName(column.getUpdate().getName());
		return runs;
	}

	/**
	 * alter table T alter column C type varchar(64);
	 * @param column column
	 * @return String
	 */
	@Override
	public List<Run> buildChangeTypeRunSQL(Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Column update = column.getUpdate();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable(true));
		builder.append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" TYPE ");
		type(builder, update);
		String type = update.getTypeName();
		if(type.contains("(")){
			type = type.substring(0,type.indexOf("("));
		}
		builder.append(" USING ").append(column.getName()).append("::").append(type);
		return runs;
	}


	/**
	 * 修改默认值
	 * ALTER TABLE T ALTER COLUMN C SET DEFAULT 0;
	 * ALTER TABLE T ALTER COLUMN C DROP DEFAULT;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRunSQL(Column column) throws Exception{
		Object def = null;
		if(null != column.getUpdate()){
			def = column.getUpdate().getDefaultValue();
		}else {
			def = column.getDefaultValue();
		}
		if(null != def){
			String str = def.toString();
			if(str.contains("::")){
				str = str.split("::")[0];
			}
			str = str.replace("'","");
			def = str;
		}

		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable(true)).append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		if(null != def){
			builder.append(" SET DEFAULT '").append(def).append("'");
		}else{
			builder.append(" DROP DEFAULT");
		}
		return runs;
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME DROP NOT NULL
	 * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME SET NOT NULL
	 * @param column column
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRunSQL(Column column) throws Exception{
		List<Run> runs = new ArrayList<>();

		int nullable = column.isNullable();
		int uNullable = column.getUpdate().isNullable();
		if(nullable != -1 && uNullable != -1){
			if(nullable != uNullable) {
				Run run = new SimpleRun();
				runs.add(run);
				StringBuilder builder = run.getBuilder();
				builder.append("ALTER TABLE ");
				name(builder, column.getTable(true)).append(" ALTER ");
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
				if (uNullable == 0) {
					builder.append(" SET ");
				} else {
					builder.append(" DROP ");
				}
				builder.append(" NOT NULL");
				column.setNullable(uNullable);
			}
		}
		return runs;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAddCommentRunSQL(Column column) throws Exception {
		return buildChangeCommentRunSQL(column);
	}
	/**
	 * 修改备注
	 * COMMENT ON COLUMN T.ID IS 'ABC'
	 * @param column column
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRunSQL(Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		String comment = null;
		Column update = column.getUpdate();
		if(null != update){
			comment = update.getComment();
		}
		if(BasicUtil.isEmpty(comment)){
			comment = column.getComment();
		}
		if(BasicUtil.isNotEmpty(comment)) {
			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("COMMENT ON COLUMN ");
			name(builder, column.getTable(true)).append(".");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			builder.append(" IS '").append(comment).append("'");

		}
		return runs;
	}



	/**
	 * 取消自增
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildDropAutoIncrement(Column column) throws Exception{
		return super.buildDropAutoIncrement(column);
	}
	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(StringBuilder builder, Column column){
		// 如果有递增列 通过数据类型实现
		if(column.isAutoIncrement() == 1){
			String type = column.getTypeName().toLowerCase();
			if ("int4".equals(type) || "int".equals(type) || "integer".equals(type)) {
				column.setType("SERIAL4");
			} else if ("int8".equals(type) || "long".equals(type) || "bigint".equals(type)) {
				column.setType("SERIAL8");
			} else if ("int2".equals(type) || "smallint".equals(type) || "short".equals(type)) {
				//9.2.0
				column.setType("SERIAL2");
			}else{
				column.setType("SERIAL8");
			}
		}
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
	 * 通过数据类型实现,这里不需要
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder increment(StringBuilder builder, Column column){
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
		//return super.comment(builder, column);
		//单独生成备注
		return builder;
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
	 * List<Run> buildAddRunSQL(Tag tag);
	 * List<Run> buildAlterRunSQL(Tag tag);
	 * List<Run> buildDropRunSQL(Tag tag);
	 * List<Run> buildRenameRunSQL(Tag tag);
	 * List<Run> buildChangeDefaultRunSQL(Tag tag);
	 * List<Run> buildChangeNullableRunSQL(Tag tag);
	 * List<Run> buildChangeCommentRunSQL(Tag tag);
	 * List<Run> buildChangeTypeRunSQL(Tag tag);
	 * StringBuilder checkTagExists(StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Tag tag) throws Exception{
		return super.buildAddRunSQL(tag);
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return sqls
	 */
	@Override
	public List<Run> buildAlterRunSQL(Tag tag) throws Exception{
		return super.buildAlterRunSQL(tag);
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Tag tag) throws Exception{
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
	public List<Run> buildRenameRunSQL(Tag tag)  throws Exception{
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
	public List<Run> buildChangeDefaultRunSQL(Tag tag) throws Exception{
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
	public List<Run> buildChangeNullableRunSQL(Tag tag) throws Exception{
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
	public List<Run> buildChangeCommentRunSQL(Tag tag) throws Exception{
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
	public List<Run> buildChangeTypeRunSQL(Tag tag) throws Exception{
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
	 * List<Run> buildAddRunSQL(PrimaryKey primary) throws Exception
	 * List<Run> buildAlterRunSQL(PrimaryKey primary) throws Exception
	 * List<Run> buildDropRunSQL(PrimaryKey primary) throws Exception
	 * List<Run> buildRenameRunSQL(PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Map<String,Column> columns = primary.getColumns();
		if(columns.size()>0) {
			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("ALTER TABLE ");
			name(builder, primary.getTable(true));
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
		return runs;
	}
	/**
	 * 修改主键
	 * 有可能生成多条SQL
	 * @param primary 主键
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(PrimaryKey primary) throws Exception{
		return super.buildAlterRunSQL(primary);
	}

	/**
	 * 删除主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(builder, primary.getTable(true));
		builder.append(" DROP CONSTRAINT ");
		SQLUtil.delimiter(builder, primary.getName(), getDelimiterFr(), getDelimiterTo());
		return runs;
	}
	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(PrimaryKey primary) throws Exception{
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
	public List<Run> buildAddRunSQL(ForeignKey foreign) throws Exception{
		return super.buildAddRunSQL(foreign);
	}
	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(ForeignKey foreign) throws Exception{
		return super.buildAlterRunSQL(foreign);
	}

	/**
	 * 删除外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildDropRunSQL(ForeignKey foreign) throws Exception{
		return super.buildDropRunSQL(foreign);
	}

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildRenameRunSQL(ForeignKey foreign) throws Exception{
		return super.buildRenameRunSQL(foreign);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRunSQL(Index index) throws Exception
	 * List<Run> buildAlterRunSQL(Index index) throws Exception
	 * List<Run> buildDropRunSQL(Index index) throws Exception
	 * List<Run> buildRenameRunSQL(Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Index index) throws Exception{
		return super.buildAddRunSQL(index);
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Index index) throws Exception{
		return super.buildAlterRunSQL(index);
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Index index) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(index.isPrimary()){
			log.warn("[主键索引,忽略删除][index:{}]", index.getName());
		}else {
			builder.append("DROP INDEX ").append(index.getName());
		}
		return runs;
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Index index) throws Exception{
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
	 * List<Run> buildAddRunSQL(Constraint constraint) throws Exception
	 * List<Run> buildAlterRunSQL(Constraint constraint) throws Exception
	 * List<Run> buildDropRunSQL(Constraint constraint) throws Exception
	 * List<Run> buildRenameRunSQL(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRunSQL(Constraint constraint) throws Exception{
		return super.buildAddRunSQL(constraint);
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Constraint constraint) throws Exception{
		return super.buildAlterRunSQL(constraint);
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Constraint constraint) throws Exception{
		return super.buildDropRunSQL(constraint);
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Constraint constraint) throws Exception{
		return super.buildRenameRunSQL(constraint);
	}


	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Trigger trigger) throws Exception
	 * List<Run> buildAlterRunSQL(Trigger trigger) throws Exception;
	 * List<Run> buildDropRunSQL(Trigger trigger) throws Exception;
	 * List<Run> buildRenameRunSQL(Trigger trigger) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRunSQL(Trigger trigger) throws Exception{
		return super.buildCreateRunSQL(trigger);
	}
	public void each(StringBuilder builder, Trigger trigger){
		super.each(builder, trigger);
	}
	/**
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param trigger 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRunSQL(Trigger trigger) throws Exception{
		return super.buildAlterRunSQL(trigger);
	}

	/**
	 * 删除触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildDropRunSQL(Trigger trigger) throws Exception{
		return super.buildDropRunSQL(trigger);
	}

	/**
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRunSQL(Trigger trigger) throws Exception{
		return super.buildRenameRunSQL(trigger);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Procedure procedure) throws Exception
	 * List<Run> buildAlterRunSQL(Procedure procedure) throws Exception;
	 * List<Run> buildDropRunSQL(Procedure procedure) throws Exception;
	 * List<Run> buildRenameRunSQL(Procedure procedure) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildCreateRunSQL(Procedure procedure) throws Exception{
		return super.buildCreateRunSQL(procedure);
	}

	/**
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param procedure 存储过程
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Procedure procedure) throws Exception{
		return super.buildAlterRunSQL(procedure);
	}

	/**
	 * 删除存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildDropRunSQL(Procedure procedure) throws Exception{
		return super.buildDropRunSQL(procedure);
	}

	/**
	 * 修改存储过程名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildRenameRunSQL(Procedure procedure) throws Exception{
		return super.buildRenameRunSQL(procedure);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRunSQL(Function function) throws Exception
	 * List<Run> buildAlterRunSQL(Function function) throws Exception;
	 * List<Run> buildDropRunSQL(Function function) throws Exception;
	 * List<Run> buildRenameRunSQL(Function function) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 添加函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildCreateRunSQL(Function function) throws Exception{
		return super.buildCreateRunSQL(function);
	}

	/**
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param function 函数
	 * @return List
	 */
	public List<Run> buildAlterRunSQL(Function function) throws Exception{
		return super.buildAlterRunSQL(function);
	}

	/**
	 * 删除函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildDropRunSQL(Function function) throws Exception{
		return super.buildDropRunSQL(function);
	}

	/**
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildRenameRunSQL(Function function) throws Exception{
		return super.buildRenameRunSQL(function);
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

	/**
	 * 是否是boolean类型
	 * @param column 列
	 * @return boolean
	 */
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

	/**
	 * 是否是字符类型
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public boolean isCharColumn(Column column) {
		return super.isCharColumn(column);
	}
	/**
	 * 内置函数 多种数据库兼容时需要
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	@Override
	public String value(Column column, SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "now()";
		}
		return null;
	}




} 
