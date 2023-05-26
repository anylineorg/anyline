 
package org.anyline.data.jdbc.informix;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.adapter.SQLAdapter;
import org.anyline.data.entity.*;
import org.anyline.data.run.Run;
import org.anyline.data.run.TextRun;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.data.DatabaseType;
import org.anyline.entity.metadata.ColumnType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SQLUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.*;

@Repository("anyline.data.jdbc.adapter.informix")
public class InformixAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	public static Map<Integer,String> column_types = new HashMap<>();
	public static boolean IS_GET_SEQUENCE_VALUE_BEFORE_INSERT = false;

	public DatabaseType type(){
		return DatabaseType.Informix;
	}

	@Value("${anyline.data.jdbc.delimiter.informix:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public InformixAdapter(){
		super();
		delimiterFr = "\"";
		delimiterTo = "\"";
		for (InformixColumnTypeAlias alias: InformixColumnTypeAlias.values()){
			types.put(alias.name(), alias.standard());
		}
	}
	static{
		column_types.put(0,"CHAR");
		column_types.put(1,"SMALLINT");
		column_types.put(2,"INTEGER");
		column_types.put(3,"FLOAT");
		column_types.put(4,"SMALLFLOAT");
		column_types.put(5,"DECIMAL");
		column_types.put(6,"SERIAL");
		column_types.put(7,"DATE");
		column_types.put(8,"MONEY");
		column_types.put(9,"NULL");
		column_types.put(10,"DATETIME");
		column_types.put(11,"BYTE");
		column_types.put(12,"TEXT");
		column_types.put(13,"VARCHAR");
		column_types.put(14,"INTERVAL");
		column_types.put(15,"NCHAR");
		column_types.put(16,"NVARCHAR");
		column_types.put(17,"INT8");
		column_types.put(18,"SERIAL8");
		column_types.put(19,"SET");
		column_types.put(20,"MULTISET");
		column_types.put(21,"LIST");
		column_types.put(22,"ROW");
		column_types.put(23,"COLLECTION");
		column_types.put(24,"UDT");
		column_types.put(40,"LVARCHAR");
		column_types.put(41,"BLOB");
		column_types.put(42,"CLOB");
		column_types.put(43,"BOOLEAN");
	}
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
			int limit = navi.getLastRow() - navi.getFirstRow() + 1;
			if(limit < 0){
				limit = 0;
			}
			String sub = sql.substring(sql.toUpperCase().indexOf("SELECT")+6);
			sql = "SELECT SKIP " + navi.getFirstRow() + " FIRST " + limit + sub;
		}
		return sql; 
	}

	@Override
	public String concat(String ... args){
		return concatFun(args);
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
	public String buildQuerySequence(boolean next, String ... names){
		String key = "CURRVAL";
		if(next){
			key = "NEXTVAL";
		}
		StringBuilder builder = new StringBuilder();
		Run run = null;
		if(null != names && names.length>0) {
			run = new TextRun();
			builder.append("SELECT ");
			boolean first = true;
			for (String name : names) {
				if(!first){
					builder.append(",");
				}
				first = false;
				builder.append(name).append(".").append(key).append(" AS ").append(name);
			}
			builder.append(" FROM DUAL");
		}
		return builder.toString();
	}

	@Override
	public String parseExists(Run run){
		String sql = "SELECT 1 AS IS_EXISTS FROM DUAL WHERE  EXISTS(" + run.getBuilder().toString() + ")";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
		return sql;
	}
	protected void createPrimaryValue(JdbcTemplate template, Collection list, String seq){
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT ").append(seq).append(" AS ID FROM(\n");
		int size = list.size();
		for(int i=0; i<size; i++){
			builder.append("SELECT NULL FROM DUAL\n");
			if(i<size-1){
				builder.append("UNION ALL\n");
			}
		}
		builder.append(") M");
		List<Map<String,Object>> ids = template.queryForList(builder.toString());
		int i=0;
		for(Object obj:list){
			Object value = ids.get(i++).get("ID");
			setPrimaryValue(obj, value);
		}
	}

	/**
	 * 批量插入
	 *
	 * 有序列时 只支持插入同一张表
	 * INSERT INTO CRM_USER(ID, NAME)
	 *  SELECT gloable_seq.nextval  AS ID  , M.* FROM (
	 * 		SELECT  'A1' AS NM FROM  DUAL
	 * 		UNION ALL SELECT    'A2' FROM DUAL
	 * 		UNION ALL SELECT    'A3' FROM DUAL
	 * ) M
	 * @param template JdbcTemplate
	 * @param run run
	 * @param dest dest
	 * @param keys keys
	 */
	@Override
	public void createInserts(JdbcTemplate template, Run run, String dest, DataSet set, List<String> keys){
		if(null == set || set.size() ==0){
			return;
		}
		StringBuilder builder = run.getBuilder();
		DataRow first = set.getRow(0);
		Map<String,String> seqs = new HashMap<>();
		for(String key:keys){
			Object value = first.getStringNvl(key);
			if(null != value && value instanceof String) {
				String str = (String)value;
				if (str.toUpperCase().contains(".NEXTVAL")) {
					if (str.startsWith("${") && str.endsWith("}")) {
						str = str.substring(2, str.length() - 1);
					}
					if(IS_GET_SEQUENCE_VALUE_BEFORE_INSERT) {
						createPrimaryValue(template, set, str);
					}else {
						seqs.put(key, str);
					}
				}
			}
		}
		for(DataRow row:set){
			builder.append("INSERT INTO ");
			SQLUtil.delimiter(builder, dest, getDelimiterFr(), getDelimiterTo()).append(" (");
			boolean start = true;
			for(String key:keys){
				if(!start){
					builder.append(",");
				}
				builder.append(key);
				start = false;
			}
			builder.append(")");
			insertValue(template, run, row, false, false,true, keys);
			builder.append(";");
		}
	}
	@Override
	public void createInserts(JdbcTemplate template, Run run, String dest, Collection list, List<String> keys){
		if(null == list || list.isEmpty()){
			return;
		}
		StringBuilder builder = run.getBuilder();
		if(null == builder){
			builder = new StringBuilder();
			run.setBuilder(builder);
		}
		if(list instanceof DataSet){
			DataSet set = (DataSet) list;
			createInserts(template, run, dest, set, keys);
			return;
		}

		Object first = list.iterator().next();
		Map<String,String> seqs = new HashMap<>();
		for(String key:keys){
			Object value = BeanUtil.getFieldValue(first, key);
			if(null != value && value instanceof String) {
				String str = (String)value;
				if (str.toUpperCase().contains(".NEXTVAL")) {
					if (str.startsWith("${") && str.endsWith("}")) {
						str = str.substring(2, str.length() - 1);
					}
					if(IS_GET_SEQUENCE_VALUE_BEFORE_INSERT) {
						createPrimaryValue(template, list, str);
					}else {
						seqs.put(key, str);
					}
				}
			}
		}
		builder.append("INSERT INTO ");
		SQLUtil.delimiter(builder, dest, getDelimiterFr(), getDelimiterTo()).append(" (");
		int keySize = keys.size();
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			builder.append(key);
			if(i<keySize-1){
				builder.append(", ");
			}
		}
		builder.append(") \n");
		builder.append("SELECT ");
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			String seq = seqs.get(key);
			if(null != seq){
				builder.append(seq);
			}else{
				builder.append("M.").append(key);
			}
			builder.append(" AS ").append(key);
			if(i<keySize-1){
				builder.append(", ");
			}
		}
		builder.append("\nFROM( ");
		keys.removeAll(seqs.keySet());
		int col = 0;

		for(Object obj:list){
			if(obj instanceof DataRow) {
				DataRow row = (DataRow)obj;
				if (row.hasPrimaryKeys() && null != primaryGenerator && BasicUtil.isEmpty(row.getPrimaryValue())) {
					String pk = row.getPrimaryKey();
					if (null == pk) {
						pk = ConfigTable.DEFAULT_PRIMARY_KEY;
					}
					createPrimaryValue(row, type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
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

			if(col > 0){
				builder.append("\n\tUNION ALL");
			}
			builder.append("\n\tSELECT ");
			insertValue(template, run, obj, true, true,false, keys);
			builder.append(" FROM DUAL ");
			col ++;
		}
		builder.append(") M ");
	}

	/**
	 * 执行 insert
	 * @param template JdbcTemplate
	 * @param random random
	 * @param data entity|DataRow|DataSet
	 * @param sql sql
	 * @param values 占位参数值
	 * @return int 影响行数
	 * @throws Exception 异常
	 */
	@Override
	public int insert(JdbcTemplate template, String random, Object data, String sql, List<Object> values, String[] pks) throws Exception{
		int cnt = 0;
		if(data instanceof Collection) {
			if (null == values || values.isEmpty()) {
				cnt = template.update(sql);
			} else {
				int size = values.size();
				Object[] params = new Object[size];
				for (int i = 0; i < size; i++) {
					params[i] = values.get(i);
				}
				cnt = template.update(sql, params);
			}
		}else{
			//单行的可以返回序列号
			pks = new String[]{getPrimayKey(data)};
			cnt = super.insert(template, random, data, sql, values, pks);
		}
		return cnt;
	}

	@Override
	public boolean identity(String random, Object data, KeyHolder keyholder){
		if(data instanceof Collection) {
			return false;
		}else{
			return super.identity(random, data, keyholder);
		}
	}

	/* *****************************************************************************************************************
	 *
	 * 													metadata
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types)
	 * public List<String> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types)
	 * public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, Table> tables(boolean create, LinkedHashMap<String, Table> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception
	 * public LinkedHashMap<String, Table> comments(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception
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
		List<String> sqls = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM SYSTABLES  WHERE TABID > 99 AND TABTYPE = 'T'");
		sqls.add(builder.toString());
		return sqls;
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
	public List<String> buildQueryTableCommentRunSQL(String catalog, String schema, String pattern, String types) throws Exception{
		//return super.buildQueryTableCommentRunSQL(catalog, schema, pattern, types);
		return null;
	}
	@Override
	public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("TABNAME");
			Table table = tables.get(name.toUpperCase());
			if(null == table){
				table = new Table();
			}
			table.setCatalog(catalog);
			table.setSchema(schema);
			table.setName(name);
			tables.put(name.toUpperCase(), table);
		}
		return tables;
	}
	@Override
	public LinkedHashMap<String, Table> tables(boolean create, LinkedHashMap<String, Table> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(create, tables, dbmd, catalog, schema, pattern, types);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, MasterTable> mtables(boolean create, LinkedHashMap<String, MasterTable> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception;
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
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean create, LinkedHashMap<String, MasterTable> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception{
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
	public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception{
		return super.mtables(index, create, catalog, schema, tables, set);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable table, Map<String,Object> tags);
	 * public LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable table, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, PartitionTable> ptables(boolean create, String catalog, MasterTable table, String schema, LinkedHashMap<String, PartitionTable> tables, ResultSet set) throws Exception;
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
	public List<String> buildQueryPartitionTableRunSQL(MasterTable table, Map<String,Object> tags) throws Exception{
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
	public LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception{
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
	public LinkedHashMap<String, PartitionTable> ptables(boolean create, LinkedHashMap<String, PartitionTable> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception{
		return super.ptables(create, tables, dbmd, catalog, schema, master);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryColumnRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, Table table, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
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
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			builder.append("SELECT M.*,F.TABNAME FROM SYSCOLUMNS AS M LEFT JOIN SYSTABLES AS F ON M.TABID = F.TABID\n");
			builder.append("WHERE 1 = 1\n");
			if(BasicUtil.isNotEmpty(catalog)){
			}
			if(BasicUtil.isNotEmpty(schema)){
				builder.append(" AND F.OWNER = '").append(schema).append("'");
			}
			builder.append(" AND F.TABNAME = '").append(table.getName()).append("'");
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
	 * @return columns
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}

		for(DataRow row:set){
			String name = row.getString("COLNAME");
			Column column = columns.get(name.toUpperCase());
			if(null == column){
				column = new Column();
			}
			column.setTable(table);
			column.setTableName(BasicUtil.evl(row.getString("TABNAME"), table.getName(), column.getTableName()));
			column.setName(name);
			Integer coltype = row.getInt("COLTYPE", null);
			if(null != coltype){
				if(coltype >= 256){
					coltype -= 256;
					column.setNullable(false);
				}
				String typeName = column_types.get(coltype);
				column.setTypeName(typeName);
			}
			int len = row.getInt("COLLENGTH",0);
			column.setPrecision(len);
			if(coltype == 8 || coltype == 5){
				int scale = len%256;
				len = len/256;
				column.setPrecision(len, scale);
			}
			if(null == column.getColumnType()) {
				ColumnType columnType = type(column.getTypeName());
				column.setColumnType(columnType);
			}
			columns.put(name.toUpperCase(), column);
		}
		return columns;

	}
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, Table table, SqlRowSet set) throws Exception{
		return super.columns(create, columns, table, set);
	}
	@Override
	public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		return super.columns(create, columns, dbmd, table, pattern);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryTagRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Tag> tags(boolean create, LinkedHashMap<String, Tag> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
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
	 * @return tags
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception{
		return super.tags(index, create, table, tags, set);
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception{
		return super.tags(create, table, tags, set);
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, LinkedHashMap<String, Tag> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		return super.tags(create, tags, dbmd, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryPrimaryRunSQL(Table table) throws Exception
	 * public PrimaryKey primary(int index, Table table, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	public List<String> buildQueryPrimaryRunSQL(Table table) throws Exception{
		List<String> list = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT T.TABNAME AS TABLE_NAME, C.CONSTRNAME AS CONSTRAINT_NAME, C.CONSTRTYPE AS CONSTRAINT_TYPE, K.COLNAME AS COLUMN_NAME\n");
		builder.append("FROM SYSTABLES T\n");
		builder.append("JOIN SYSCONSTRAINTS C ON T.TABID = C.TABID\n");
		builder.append("JOIN SYSINDEXES I ON C.IDXNAME = I.IDXNAME AND C.TABID = I.TABID\n");
		builder.append("JOIN SYSCOLUMNS K ON T.TABID = K.TABID AND I.PART1 = K.COLNO\n");
		builder.append("WHERE  C.CONSTRTYPE = 'P'\n");
		builder.append("AND T.TABNAME = '").append(table.getName()).append("'\n");
		String schema = table.getSchema();
		if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND T.OWNER = '").append(schema).append("'");
		}
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
			primary.addColumn(column);
		}
		return primary;
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryIndexRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Index> indexs(boolean create, LinkedHashMap<String, Index> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception;
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
	 * @return indexs
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception{
		return super.indexs(index, create, table, indexs, set);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception{
		return super.indexs(create, table, indexs, set);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean create, LinkedHashMap<String, Index> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception{
		return super.indexs(create, indexs, dbmd, table, unique, approximate);
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception;
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
	 * @return constraints
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Constraint> constraints(int index , boolean create, Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception{

		return super.constraints(index, create, table, constraints, set);
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception{
		return super.constraints(create, table, constraints, set);
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception{
		return super.constraints(create, table, constraints, set);
	}






	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildCreateRunSQL(Table table)
	 * public String buildCreateCommentRunSQL(Table table);
	 * public List<String> buildAlterRunSQL(Table table)
	 * public List<String> buildAlterRunSQL(Table table, Collection<Column> columns)
	 * public String buildRenameRunSQL(Table table)
	 * public String buildChangeCommentRunSQL(Table table)
	 * public String buildDropRunSQL(Table table)
	 * public StringBuilder checkTableExists(StringBuilder builder, boolean exists)
	 * public StringBuilder primary(StringBuilder builder, Table table)
	 * public StringBuilder comment(StringBuilder builder, Table table)
	 * public StringBuilder name(StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<String> buildCreateRunSQL(Table table) throws Exception{
		return super.buildCreateRunSQL(table);
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
	 * ALTER TABLE A RENAME TO B;
	 * @param table table
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table)  throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" RENAME TO ");
		//去掉catalog schema前缀
		Table update = new Table(table.getUpdate().getName());
		name(builder, update);
		return builder.toString();
	}

	/**
	 * 修改备注
	 * COMMENT ON TABLE T IS 'ABC';
	 * @param table table
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Table table) throws Exception{
		/*String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			StringBuilder builder = new StringBuilder();
			builder.append("COMMENT ON TABLE ");
			name(builder, table);
			builder.append(" IS '").append(comment).append("'");
			return builder.toString();
		}else{
			return null;
		}*/
		return null;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public String buildCreateCommentRunSQL(Table table) throws Exception {
		return buildChangeCommentRunSQL(table);
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


	/**
	 * 创建之前  检测表是否存在
	 * IF NOT EXISTS
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	public StringBuilder checkTableExists(StringBuilder builder, boolean exists){
		return builder;
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
			builder.append(",PRIMARY KEY (");
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
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		String name = table.getName();
		if(BasicUtil.isNotEmpty(catalog)) {
			SQLUtil.delimiter(builder, catalog, getDelimiterFr(), getDelimiterTo()).append(":");
		}
		if(BasicUtil.isNotEmpty(schema)) {
			SQLUtil.delimiter(builder, schema, getDelimiterFr(), getDelimiterTo()).append(".");
		}
		SQLUtil.delimiter(builder, name, getDelimiterFr(), getDelimiterTo());
		return builder;
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildCreateRunSQL(MasterTable table);
	 * public String buildCreateCommentRunSQL(MasterTable table)
	 * public List<String> buildAlterRunSQL(MasterTable table);
	 * public String buildDropRunSQL(MasterTable table);
	 * public String buildRenameRunSQL(MasterTable table);
	 * public String buildChangeCommentRunSQL(MasterTable table);
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
		//return super.buildChangeCommentRunSQL(table);
		return null;
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildCreateRunSQL(PartitionTable table);
	 * public List<String> buildAlterRunSQL(PartitionTable table);
	 * public String buildDropRunSQL(PartitionTable table);
	 * public String buildRenameRunSQL(PartitionTable table);
	 * public String buildChangeCommentRunSQL(PartitionTable table);
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
		//return super.buildChangeCommentRunSQL(table);
		return null;
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String alterColumnKeyword()
	 * public List<String> buildAddRunSQL(Column column, boolean slice)
	 * public List<String> buildAddRunSQL(Column column)
	 * public List<String> buildAlterRunSQL(Column column, boolean slice)
	 * public List<String> buildAlterRunSQL(Column column)
	 * public String buildDropRunSQL(Column column, boolean slice)
	 * public String buildDropRunSQL(Column column)
	 * public String buildRenameRunSQL(Column column)
	 * public List<String> buildChangeTypeRunSQL(Column column)
	 * public String buildChangeDefaultRunSQL(Column column)
	 * public String buildChangeNullableRunSQL(Column column)
	 * public String buildChangeCommentRunSQL(Column column)
	 * public String buildCreateCommentRunSQL(Column column)
	 * public StringBuilder define(StringBuilder builder, Column column)
	 * public StringBuilder type(StringBuilder builder, Column column)
	 * public boolean isIgnorePrecision(Column column);
	 * public boolean isIgnoreScale(Column column);
	 * public Boolean checkIgnorePrecision(String datatype);
	 * public Boolean checkIgnoreScale(String datatype);
	 * public StringBuilder nullable(StringBuilder builder, Column column)
	 * public StringBuilder charset(StringBuilder builder, Column column)
	 * public StringBuilder defaultValue(StringBuilder builder, Column column)
	 * public StringBuilder increment(StringBuilder builder, Column column)
	 * public StringBuilder onupdate(StringBuilder builder, Column column)
	 * public StringBuilder position(StringBuilder builder, Column column)
	 * public StringBuilder comment(StringBuilder builder, Column column)
	 * public StringBuilder checkColumnExists(StringBuilder builder, boolean exists)
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
	public List<String> buildAddRunSQL(Column column, boolean slice) throws Exception{
		return super.buildAddRunSQL(column, slice);
	}
	@Override
	public List<String> buildAddRunSQL(Column column) throws Exception{
		return buildAddRunSQL(column, false);
	}

	/**
	 * 添加列引导
	 * @param builder StringBuilder
	 * @param column column
	 * @return String
	 */
	public StringBuilder addColumnGuide(StringBuilder builder, Column column){
		builder.append(" ADD ");
		return builder;
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
	 * ALTER TABLE T  RENAME  A  to B ;
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column) throws Exception {
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable());
		builder.append(" RENAME ").append(column.getName()).append(" TO ").append(column.getNewName());
		return builder.toString();
	}

	/**
	 * alter table T alter column C type varchar(64);
	 * @param column column
	 * @return String
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
		builder.append(" TYPE ");
		type(builder, update);
		String type = update.getTypeName();
		if(type.contains("(")){
			type = type.substring(0,type.indexOf("("));
		}
		builder.append(" USING ").append(column.getName()).append("::").append(type);
		sqls.add(builder.toString());
		return sqls;
	}


	/**
	 * 修改默认值
	 * ALTER TABLE T ALTER COLUMN C SET DEFAULT 0;
	 * ALTER TABLE T ALTER COLUMN C DROP DEFAULT;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Column column) throws Exception{
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

		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable()).append(" ALTER COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		if(null != def){
			builder.append(" SET DEFAULT '").append(def).append("'");
		}else{
			builder.append(" DROP DEFAULT");
		}
		return builder.toString();
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME DROP NOT NULL
	 * ALTER TABLE TABLE_NAME ALTER COLUMN_NAME SET NOT NULL
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Column column) throws Exception{
		int nullable = column.isNullable();
		int uNullable = column.getUpdate().isNullable();
		if(nullable != -1 && uNullable != -1){
			if(nullable == uNullable){
				return null;
			}

			StringBuilder builder = new StringBuilder();
			builder.append("ALTER TABLE ");
			name(builder, column.getTable()).append(" ALTER ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			if(uNullable == 0){
				builder.append(" SET ");
			}else{
				builder.append(" DROP ");
			}
			builder.append(" NOT NULL");
			return builder.toString();
		}
		return null;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public String buildCreateCommentRunSQL(Column column) throws Exception {
		return buildChangeCommentRunSQL(column);
	}
	/**
	 * 修改备注
	 * COMMENT ON COLUMN T.ID IS 'ABC'
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Column column) throws Exception{
		/*String comment = null;
		Column update = column.getUpdate();
		if(null != update){
			comment = update.getComment();
		}
		if(BasicUtil.isEmpty(comment)){
			comment = column.getComment();
		}
		if(BasicUtil.isNotEmpty(comment)) {
			StringBuilder builder = new StringBuilder();
			builder.append("COMMENT ON COLUMN ");
			name(builder, column.getTable()).append(".");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			builder.append(" IS '").append(comment).append("'");
			return builder.toString();
		}else{
			return null;
		}*/
		return null;
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
				column.setType("SERIAL");
			} else if ("int8".equals(type) || "long".equals(type) || "bigint".equals(type)) {
				column.setType("SERIAL8");
			} else if ("int2".equals(type) || "smallint".equals(type) || "short".equals(type)) {
				column.setType("SERIAL");
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
	 * 列数据类型定义
	 * @param builder builder
	 * @param column 列
	 * @param type 数据类型(已经过转换)
	 * @param isIgnorePrecision 是否忽略长度
	 * @param isIgnoreScale 是否忽略小数
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder type(StringBuilder builder, Column column, String type, boolean isIgnorePrecision, boolean isIgnoreScale){
		if(null == builder){
			builder = new StringBuilder();
		}
		if("datetime".equalsIgnoreCase(type)){
			String dateScale = column.getDateScale();
			if(null == dateScale){
				dateScale = "FRACTION";
			}
			builder.append(type).append(" YEAR TO ").append(dateScale);
		}else{
			return super.type(builder, column, type, isIgnorePrecision, isIgnoreScale);
		}

		return builder;
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
	 * public String buildAddRunSQL(Tag tag);
	 * public List<String> buildAlterRunSQL(Tag tag);
	 * public String buildDropRunSQL(Tag tag);
	 * public String buildRenameRunSQL(Tag tag);
	 * public String buildChangeDefaultRunSQL(Tag tag);
	 * public String buildChangeNullableRunSQL(Tag tag);
	 * public String buildChangeCommentRunSQL(Tag tag);
	 * public List<String> buildChangeTypeRunSQL(Tag tag);
	 * public StringBuilder checkTagExists(StringBuilder builder, boolean exists)
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
	 * @return sqls
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
		//return super.buildChangeCommentRunSQL(tag);
		return null;
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
	 * public String buildAddRunSQL(PrimaryKey primary) throws Exception
	 * public List<String> buildAlterRunSQL(PrimaryKey primary) throws Exception
	 * public String buildDropRunSQL(PrimaryKey primary) throws Exception
	 * public String buildRenameRunSQL(PrimaryKey primary) throws Exception
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
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Index index) throws Exception
	 * public List<String> buildAlterRunSQL(Index index) throws Exception
	 * public String buildDropRunSQL(Index index) throws Exception
	 * public String buildRenameRunSQL(Index index) throws Exception
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

		StringBuilder builder = new StringBuilder();
		if(index.isPrimary()){
			log.warn("[主键索引,忽略删除][index:{}]", index.getName());
		}else {
			builder.append("DROP INDEX ").append(index.getName());
		}
		return builder.toString();
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
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildAddRunSQL(Constraint constraint) throws Exception
	 * public List<String> buildAlterRunSQL(Constraint constraint) throws Exception
	 * public String buildDropRunSQL(Constraint constraint) throws Exception
	 * public String buildRenameRunSQL(Constraint constraint) throws Exception
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
	 * public boolean isBooleanColumn(Column column)
	 * public  boolean isNumberColumn(Column column)
	 * public boolean isCharColumn(Column column)
	 * public String value(Column column, SQL_BUILD_IN_VALUE value)
	 * public String type(String type)
	 * public String type2class(String type)
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
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME) {
			String type = column.getTypeName();
			if ("datetime".equalsIgnoreCase(type)) {
				String scale = column.getDateScale();
				if (null == scale) {
					if ("date".equalsIgnoreCase(type)) {
						scale = "DAY";
					} else {
						scale = "FRACTION";
					}
				}
				return "CURRENT YEAR TO " + scale;
			}else if("date".equalsIgnoreCase(type)){
				return "TODAY";
			}
		}
		return null;
	}




	/* *****************************************************************************************************************
	 *
	 * 														common
	 *
	 *  *****************************************************************************************************************/


}