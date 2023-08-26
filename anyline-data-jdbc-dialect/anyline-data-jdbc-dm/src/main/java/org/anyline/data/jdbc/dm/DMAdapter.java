
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


package org.anyline.data.jdbc.dm;


import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.*;

/**
 * 参考 OracleAdapter
 */
@Repository("anyline.data.jdbc.adapter.dm")
public class DMAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
	
	public static boolean IS_GET_SEQUENCE_VALUE_BEFORE_INSERT = false;


	public DatabaseType type(){
		return DatabaseType.DM;
	}

	@Value("${anyline.data.jdbc.delimiter.dm:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public DMAdapter() {
		super();
		delimiterFr = "\"";
		delimiterTo = "\"";
		for (DMColumnTypeAlias alias : DMColumnTypeAlias.values()) {
			types.put(alias.name(), alias.standard());
		}

		for(DMWriter writer: DMWriter.values()){
			Object[] supports = writer.supports();
			if(null != supports) {
				for(Object support:supports)
					writers.put(support, writer.writer());
			}
		}
		for(DMReader reader: DMReader.values()){
			Object[] supports = reader.supports();
			if(null != supports) {
				for(Object support:supports)
					readers.put(support, reader.reader());
			}
		}
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
	public List<Run> buildQuerySequence(DataRuntime runtime, boolean next, String ... names){
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
				builder.append(name).append(".").append(key).append(" AS ").append(name);
			}
			builder.append(" FROM DUAL");
		}
		return runs;
	}
	@Override
	public String mergeFinalQuery(DataRuntime runtime, Run run){
		String sql = run.getBaseQuery();
		String cols = run.getQueryColumn();
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
			sql += " LIMIT " + navi.getFirstRow() + "," + limit;
		}
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
		return sql;
	}


	@Override
	public String concat(DataRuntime runtime, String ... args){
		return concatOr(args);
	}

	@Override
	public String mergeFinalExists(DataRuntime runtime, Run run){
		String sql = "SELECT 1 AS IS_EXISTS FROM DUAL WHERE  EXISTS(" + run.getBuilder().toString() + ")";
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
		return sql;
	}
	protected boolean createPrimaryValue(DataRuntime runtime, Collection list, String seq){
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
		JdbcTemplate jdbc = jdbc(runtime);
		List<Map<String,Object>> ids = jdbc.queryForList(builder.toString());
		int i=0;
		for(Object obj:list){
			Object value = ids.get(i++).get("ID");
			setPrimaryValue(obj, value);
		}
		return true;
	}

	/**
	 * 批量插入
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dest dest
	 * @param columns  需要插入的列
	 */
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, String dest, DataSet set, LinkedHashMap<String, Column> columns){
		//不要参考oracle,参考mysql就可以
		super.fillInsertContent(runtime, run, dest, set, columns);
	}
	@Override
	public void fillInsertContent(DataRuntime runtime, Run run, String dest, Collection list, LinkedHashMap<String, Column> columns){
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
			this.fillInsertContent(runtime, run, dest, set, columns);
			return;
		}

		Object first = list.iterator().next();
		Map<String, String> seqs = new HashMap<>();
		for(Column column:columns.values()){
			String key = column.getName();
			Object value = BeanUtil.getFieldValue(first, key);
			if(null != value && value instanceof String) {
				String str = (String)value;
				if (str.toUpperCase().contains(".NEXTVAL")) {
					if (str.startsWith("${") && str.endsWith("}")) {
						str = str.substring(2, str.length() - 1);
					}
					if(DMAdapter.IS_GET_SEQUENCE_VALUE_BEFORE_INSERT) {
						createPrimaryValue(runtime, list, str);
					}else {
						seqs.put(key, str);
					}
				}
			}
		}

		PrimaryGenerator generator = checkPrimaryGenerator(type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""));
		LinkedHashMap<String, Column> pks = null;
		if(null != generator) {
			pks = EntityAdapterProxy.primaryKeys(first.getClass());
			columns.putAll(pks);
		}

		builder.append("INSERT INTO ");
		SQLUtil.delimiter(builder, dest, getDelimiterFr(), getDelimiterTo()).append(" (");
		boolean start = true;
		for(Column column:columns.values()){
			if(!start){
				builder.append(", ");
			}
			start = false;
			String key = column.getName();
			builder.append(key);
		}
		builder.append(") \n");
		builder.append("SELECT ");
		start = true;
		for(Column column:columns.values()){
			String key = column.getName();
			String seq = seqs.get(key);
			if(!start){
				builder.append(", ");
			}
			start = false;
			if(null != seq){
				builder.append(seq);
			}else{
				builder.append("M.").append(key);
			}
			builder.append(" AS ").append(key);
		}
		builder.append("\nFROM( ");
		for(String seq:seqs.keySet()) {
			columns.remove(seq.toUpperCase());
		}
		int col = 0;

		for(Object obj:list){
			List<String> inserts = BeanUtil.getMapKeys(columns);
			/*if(obj instanceof DataRow) {
				DataRow row = (DataRow)obj;
				if (row.hasPrimaryKeys() && null != primaryGenerator && BasicUtil.isEmpty(row.getPrimaryValue())) {
					String pk = row.getPrimaryKey();
					if (null == pk) {
						pk = ConfigTable.DEFAULT_PRIMARY_KEY;
					}
					createPrimaryValue(row, type(), dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
				}
			}else{*/
			//创建主键
			boolean create = EntityAdapterProxy.createPrimaryValue(obj, BeanUtil.getMapKeys(columns));
			if(!create && null != generator){
				generator.create(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), BeanUtil.getMapKeys(pks),  null);
				//createPrimaryValue(obj, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), null,  null);
			}

			//}

			if(col > 0){
				builder.append("\n\tUNION ALL");
			}
			builder.append("\n\tSELECT ");
			insertValue(runtime, run, obj, true, true,false, columns);
			builder.append(" FROM DUAL ");
			col ++;
		}
		builder.append(") M ");
	}

	/**
	 * 执行 insert
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data entity|DataRow|DataSet
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return int 影响行数
	 * @throws Exception 异常
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Object data, Run run, String[] pks) {
		long cnt = 0;
		if(data instanceof Collection) {
			insert(runtime, random, data, run, pks, true);
		}else{
			//单行的可以返回序列号
			String pk = getPrimayKey(data);
			if(null != pk){
				pks = new String[]{pk};
			}else{
				pks = null;
			}
			cnt = super.insert(runtime, random, data, run, pks);
		}
		return cnt;
	}

	@Override
	public boolean identity(DataRuntime runtime, String random, Object data, KeyHolder keyholder){
		if(data instanceof Collection) {
			return false;
		}else{
			return super.identity(runtime, random, data, keyholder);
		}
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
	 * List<Run> buildQueryTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * List<Run> buildQueryTableCommentRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception
	 * <T extends Table> LinkedHashMap<String, T> comments(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * List<Run> buildQueryDDLRun(DataRuntime runtime, Table table) throws Exception
	 * public List<String> ddl(DataRuntime runtime, int index, Table table, List<String> ddls, DataSet set)
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
	public List<Run> buildQueryTableRun(DataRuntime runtime, boolean greedy, String catalog, String schema, String pattern, String types) throws Exception{
		/*
		ALL_TABLES：当前登录用户可见的所有表
		DBA_TABLES：数据库中所有表
		USER_TABLES：当前登录用户拥有的所有表
		*/
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
//		builder.append("SELECT T.TABLE_NAME, T.OWNER, TC.COMMENTS \n");
//		builder.append("FROM SYS.ALL_ALL_TABLES T \n");
//		builder.append("LEFT JOIN SYS.ALL_TAB_COMMENTS TC  ON  TC.OWNER  = T.OWNER  AND TC.TABLE_NAME  = T.TABLE_NAME \n");
//		builder.append("WHERE T.IOT_NAME IS NULL  AND T.NESTED = 'NO'  AND T.SECONDARY = 'N' ");
//		if(BasicUtil.isNotEmpty(schema)){
//			builder.append("AND T.OWNER = '").append(schema).append("'");
//		}
//		if(BasicUtil.isNotEmpty(pattern)){
//			builder.append(" AND T.TABLE_NAME = '").append(pattern).append("'");
//		}
//
//		runs.add(run);
//		return runs;
		// jack 2023年5月2日 19点55分 由于之前查询表名的方式会意外失效，特进行调整,兼容table和view
		//增加types列用于后期扩展
/*
		builder.append(" SELECT * FROM (" );
		builder.append(" SELECT A.TABLE_NAME, B.COMMENTS, 'TABLE' TABLE_TYPE FROM USER_TABLES A, USER_TAB_COMMENTS B WHERE A.TABLE_NAME = B.TABLE_NAME");
		builder.append(" UNION ALL ");
		builder.append(" SELECT A.VIEW_NAME,  B.COMMENTS, 'VIEW'  TABLE_TYPE FROM USER_VIEWS  A, USER_TAB_COMMENTS B WHERE A.VIEW_NAME = B.TABLE_NAME");
		builder.append(" ) T WHERE 1=1");

		if(BasicUtil.isNotEmpty(pattern)){
			builder.append(" AND TABLE_NAME LIKE '").append(pattern).append("'");
		}
*/
		//需要跨schema查询
		builder.append("SELECT M.OWNER AS TABLE_SCHEMA, M.OBJECT_NAME AS TABLE_NAME, M.OBJECT_TYPE AS TABLE_TYPE, F.COMMENTS FROM ALL_OBJECTS   M LEFT JOIN ALL_TAB_COMMENTS   F \n");
		builder.append("ON M.OBJECT_NAME = F.TABLE_NAME  AND M.OWNER = F.OWNER AND M.object_type = F.TABLE_TYPE \n");
		builder.append("WHERE M.OWNER NOT IN('SYS','PUBLIC','SYSTEM') AND M.OBJECT_TYPE IN('TABLE','VIEW')");
		if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND M.OWNER = '").append(schema).append("'");
		}
		if(BasicUtil.isNotEmpty(types)){
			String[] tmps = types.split(",");
			builder.append(" AND M.OBJECT_TYPE IN(");
			int idx = 0;
			for(String tmp:tmps){
				if(idx > 0){
					builder.append(",");
				}
				builder.append("'").append(tmp).append("'");
				idx ++;
			}
			builder.append(")");
		}
		return runs;
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
	public List<Run> buildQueryTableCommentRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT * FROM USER_TAB_COMMENTS\n");
		if(BasicUtil.isNotEmpty(pattern)){
			builder.append("WHERE TABLE_NAME = '").append(objectName(runtime, pattern)).append("'");
		}
		return runs;
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.tables(runtime, index, create, catalog, schema, tables, set);
	}

	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, List<T> tables, DataSet set) throws Exception{
		return super.tables(runtime, index, create, catalog, schema, tables, set);
	}
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(runtime, create, tables, catalog, schema, pattern, types);
	}
	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.tables(runtime, create, tables, catalog, schema, pattern, types);
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryViewRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set) throws Exception
	 * <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, String catalog, String schema, String pattern, String ... types) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询视图
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */
	@Override
	public List<Run> buildQueryViewRun(DataRuntime runtime, boolean greedy, String catalog, String schema, String pattern, String types) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT A.VIEW_NAME,A.TEXT DEFINITION_SQL,  B.COMMENTS, 'VIEW'  TABLE_TYPE FROM USER_VIEWS  A, USER_TAB_COMMENTS B WHERE A.VIEW_NAME = B.TABLE_NAME");
		if(BasicUtil.isNotEmpty(pattern)){
			builder.append(" AND TABLE_NAME LIKE '").append(objectName(runtime, pattern)).append("'");
		}
		return runs;
	}

	/**
	 *
	 * @param index 第几条SQL 对照buildQueryViewRun返回顺序
	 * @param catalog catalog
	 * @param schema schema
	 * @param views 上一步查询结果
	 * @param set DataSet
	 * @return views
	 * @throws Exception 异常
	 */
	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> views, DataSet set) throws Exception{
		if(null == views){
			views = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("VIEW_NAME");
			T view = views.get(name.toUpperCase());
			if(null == view){
				view = (T)new View();
			}
			view.setCatalog(catalog);
			view.setSchema(schema);
			view.setName(name);
			view.setComment(row.getString("COMMENTS"));
			view.setDefinition(row.getString("DEFINITION_SQL"));
			views.put(name.toUpperCase(), view);
		}
		return views;
	}


	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryMasterTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception
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
	public List<Run> buildQueryMasterTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryMasterTableRun(runtime, catalog, schema, pattern, types);
	}

	/**
	 * 从jdbc结果中提取表结构
	 * ResultSet set = con.getMetaData().getTables()
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param catalog catalog
	 * @param schema schema
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return List
	 */
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		return super.mtables(runtime, create, tables, catalog, schema, pattern, types);
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
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.mtables(runtime, index, create, catalog, schema, tables, set);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types)
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name)
	 * List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags)
	 * <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception
	 * <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, MasterTable master) throws Exception
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
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, String catalog, String schema, String pattern, String types) throws Exception{
		return super.buildQueryPartitionTableRun(runtime, catalog, schema, pattern, types);
	}
	@Override
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags, String name) throws Exception{
		return super.buildQueryPartitionTableRun(runtime, master, tags, name);
	}
	@Override
	public List<Run> buildQueryPartitionTableRun(DataRuntime runtime, MasterTable master, Map<String,Object> tags) throws Exception{
		return super.buildQueryPartitionTableRun(runtime, master, tags);
	}

	/**
	 *  根据查询结果集构造Table
	 * @param total 合计SQL数量
	 * @param index 第几条SQL 对照 buildQueryMasterTableRun返回顺序
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
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(DataRuntime runtime, int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		return super.ptables(runtime, total, index, create, master, catalog, schema, tables, set);
	}

	/**
	 * 根据JDBC
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param master 主表
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, MasterTable master) throws Exception{
		return super.ptables(runtime, create, tables, catalog, schema, master);
	}


	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata)
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception
	 * <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata(true:1=0,false:查询系统表)
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		List<Run> runs = new ArrayList<>();
		if(BasicUtil.isEmpty(table.getName())){
			return runs;
		}
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(runtime, builder, table);
			builder.append(" WHERE 1=0");
		}else{
			builder.append("SELECT M.*, F.COMMENTS AS COLUMN_COMMENT FROM USER_TAB_COLUMNS    M \n");
			builder.append("LEFT JOIN USER_COL_COMMENTS F ON M.TABLE_NAME = F.TABLE_NAME AND M.COLUMN_NAME = F.COLUMN_NAME\n");
			if (BasicUtil.isNotEmpty(table)) {
				builder.append("WHERE M.TABLE_NAME = '").append(objectName(runtime, table.getName())).append("'");
			}
		}
		return runs;
	}

	/**
	 *
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		set.removeColumn("CHARACTER_SET_NAME");
		return super.columns(runtime, index, create, table, columns, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		return super.columns(runtime, create, columns, table, set);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
		return super.columns(runtime, create, columns, table, pattern);
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata)
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception
	 * <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception
	 ******************************************************************************************************************/
	/**
	 *
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryTagRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		return super.buildQueryTagRun(runtime, table, metadata);
	}

	/**
	 *  根据查询结果集构造Tag
	 * @param index 第几条查询SQL 对照 buildQueryTagRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param tags 上一步查询结果
	 * @param set set
	 * @return tags tags
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> tags, DataSet set) throws Exception{
		return super.tags(runtime, index, create, table, tags, set);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception{
		return super.tags(runtime, create, table, tags, set);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tags, Table table, String pattern) throws Exception{
		return super.tags(runtime, create, tags, table, pattern);
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, boolean metadata)
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception
	 * <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列
	 * @param table 表
	 * @param name name
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryIndexRun(DataRuntime runtime, Table table, String name){
		return super.buildQueryIndexRun(runtime, table, name);
	}

	/**
	 *
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param indexs 上一步查询结果
	 * @param set set
	 * @return indexs indexs
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> indexs, DataSet set) throws Exception{
		return super.indexs(runtime, index, create, table, indexs, set);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception{
		return super.indexs(runtime, create, table, indexs, set);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception{
		return super.indexs(runtime, create, indexs, table, unique, approximate);
	}


	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, boolean metadata)
	 * LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception
	 * <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的约束
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<Run> buildQueryConstraintRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		return super.buildQueryConstraintRun(runtime, table, metadata);
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param constraints 上一步查询结果
	 * @param set set
	 * @return constraints constraints
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, int index , boolean create, Table table, LinkedHashMap<String, T> constraints, DataSet set) throws Exception{
		return super.constraints(runtime, index, create, table, constraints, set);
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception{
		return super.constraints(runtime, create, table, constraints, set);
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, ResultSet set) throws Exception{
		return super.constraints(runtime, create, table, constraints, set);
	}


	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events)
	 * <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set)
	 ******************************************************************************************************************/
	/**
	 * 查询表上的trigger
	 * @param table 表
	 * @param events INSERT|UPATE|DELETE
	 * @return sqls
	 */

	@Override
	public List<Run> buildQueryTriggerRun(DataRuntime runtime, Table table, List<Trigger.EVENT> events) {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT * FROM USER_TRIGGERS WHERE 1=1");
		if(null != table){
			String schemae = table.getSchema();
			String tableName = table.getName();
			if(BasicUtil.isNotEmpty(schemae)){
				builder.append(" AND TABLE_OWNER = '").append(schemae).append("'");
			}
			if(BasicUtil.isNotEmpty(tableName)){
				builder.append(" AND TABLE_NAME = '").append(tableName).append("'");
			}
		}
		if(null != events && events.size()>0){
			builder.append(" AND(");
			boolean first = true;
			for(Trigger.EVENT event:events){
				if(!first){
					builder.append(" OR ");
				}
				builder.append("TRIGGERING_EVENT ='").append(event);
			}
			builder.append(")");
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造Constraint
	 * @param index 第几条查询SQL 对照 buildQueryConstraintRun 返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param triggers 上一步查询结果
	 * @param set DataSet
	 * @return constraints constraints
	 * @throws Exception 异常
	 */

	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> triggers, DataSet set) throws Exception{
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
			Table tab = new Table(row.getString("TABLE_NAME"));
			tab.setSchema(row.getString("TABLE_OWNER"));
			trigger.setTable(tab);
			try{
				boolean each = false;
				//TRIGGER_NAME AFTER INSERT ON TABLE_NAME FOR EACH ROW
				String des = row.getStringNvl("DESCRIPTION").toUpperCase();
				if(des.contains("ROW")){
					each = true;
				}
				trigger.setEach(each);
				String[] tmps = des.split(" ");
				trigger.setTime(Trigger.TIME.valueOf(tmps[1]));
				trigger.addEvent(Trigger.EVENT.valueOf(tmps[2]));
			}catch (Exception e){
				e.printStackTrace();
			}
			trigger.setDefinition(row.getString("TRIGGER_BODY"));

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
	 * List<Run> buildCreateRun(DataRuntime runtime, Table table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Table table);
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table)
	 * List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns)
	 * List<Run> buildRenameRun(DataRuntime runtime, Table table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Table table)
	 * List<Run> buildDropRun(DataRuntime runtime, Table table)
	 * StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 * StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table)
	 * StringBuilder name(DataRuntime runtime, StringBuilder builder, Table table)
	 ******************************************************************************************************************/


	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Table table) throws Exception {
		List<Run> runs = new ArrayList<>();
		if(BasicUtil.isNotEmpty(table.getComment())){
			Run run = new SimpleRun();
			StringBuilder builder = run.getBuilder();
			runs.add(run);
			builder.append(" COMMENT ON TABLE ");
			name(runtime, builder, table);
			builder.append("  IS '").append(table.getComment()).append("'");
		}
		return runs;
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	/**
	 * 修改列
	 * 有可能生成多条SQL,根据数据库类型优先合并成一条执行
	 * @param table 表
	 * @param columns 列
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Table table, Collection<Column> columns) throws Exception{
		return super.buildAlterRun(runtime, table, columns);
	}
	/**
	 * 修改表名
	 * ALTER TABLE A RENAME TO B;
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Table table) throws Exception {
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, table);
		builder.append(" RENAME TO ");
		//去掉catalog schema前缀
		Table update = new Table(table.getUpdate().getName());
		name(runtime, builder, update);
		return runs;
	}

	/**
	 * 修改备注
	 * COMMENT ON TABLE T IS 'ABC';
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("COMMENT ON TABLE ");
			name(runtime, builder, table);
			builder.append(" IS '").append(comment).append("'");
		}
		return runs;
	}
	/**
	 * 删除表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Table table) throws Exception{
		return super.buildDropRun(runtime, table);
	}


	@Override
	public StringBuilder checkTableExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return builder;
	}


	/**
	 * 主键
	 * CONSTRAINT PK_BS_DEV PRIMARY KEY (ID ASC)
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder primary(DataRuntime runtime, StringBuilder builder, Table table){
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
				if(null != order){
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
	 * 不支持在创建表时带备注，创建后单独添加 buildAppendCommentRun(DataRuntime runtime, Table)
	 * @param builder builder
	 * @param table 表
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Table table){
		return builder;
	}

	/**
	 * 构造完整表名
	 * @param builder builder
	 * @param table 表
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder name(DataRuntime runtime, StringBuilder builder, Table table){
		return super.name(runtime, builder, table);
	}


	/**
	 * 创建或删除视图时检测视图是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return StringBuilder
	 */
	@Override
	public StringBuilder checkViewExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return builder;
	}

	@Override
	public List<Run> buildAppendCommentRun(DataRuntime runtime, View view) throws Exception{
		return buildAppendCommentRun(runtime, (Table)view);
	}

	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildAlterRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildDropRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildRenameRun(DataRuntime runtime, MasterTable table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table)
	 ******************************************************************************************************************/
	/**
	 * 创建主表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildDropRun(runtime, table);
	}
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildRenameRun(runtime, table);
	}
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, MasterTable table) throws Exception{
		return super.buildChangeCommentRun(runtime, table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table)
	 * List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table)
	 * List<Run> buildDropRun(DataRuntime runtime, PartitionTable table)
	 * List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table)
	 ******************************************************************************************************************/
	/**
	 * 创建分区表
	 * @param table 表
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildCreateRun(runtime, table);
	}
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildAlterRun(runtime, table);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildDropRun(runtime, table);
	}
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildRenameRun(runtime, table);
	}
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, PartitionTable table) throws Exception{
		return super.buildChangeCommentRun(runtime, table);
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * String alterColumnKeyword(DataRuntime runtime)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAddRun(DataRuntime runtime, Column column)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildAlterRun(DataRuntime runtime, Column column)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice)
	 * List<Run> buildDropRun(DataRuntime runtime, Column column)
	 * List<Run> buildRenameRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Column column)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Column column)
	 * List<Run> buildAppendCommentRun(DataRuntime runtime, Column column)
	 * StringBuilder define(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column)
	 * boolean isIgnorePrecision(DataRuntime runtime, Column column);
	 * boolean isIgnoreScale(DataRuntime runtime, Column column);
	 * Boolean checkIgnorePrecision(DataRuntime runtime, String datatype);
	 * Boolean checkIgnoreScale(DataRuntime runtime, String datatype);
	 * StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column)
	 * StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	@Override
	public String alterColumnKeyword(DataRuntime runtime){
		return "ALTER";
	}

	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD  UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Column column, boolean slice) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(!slice) {
			Table table = column.getTable(true);
			builder.append("ALTER TABLE ");
			name(runtime, builder, table);
		}
		// Column update = column.getUpdate();
		// if(null == update){
		// 添加列
		builder.append(" ADD ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(runtime, builder, column);
		//}
		runs.addAll(buildAppendCommentRun(runtime, column));
		return runs;
	}

	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @param slice 是否只生成片段(不含alter table部分，用于DDL合并)
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Column column, boolean slice) throws Exception{
		return super.buildAlterRun(runtime, column, slice);
	}


	

	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column column, boolean slice) throws Exception{
		return super.buildDropRun(runtime, column, slice);
	}
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Column column) throws Exception{
		return buildDropRun(runtime, column, false);
	}

	/**
	 * 修改列名
	 *
	 * ALTER TABLE 表名 RENAME COLUMN RENAME 老列名 TO 新列名
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Column column)  throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, column.getTable(true));
		builder.append(" RENAME COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" TO ");
		SQLUtil.delimiter(builder, column.getUpdate().getName(), getDelimiterFr(), getDelimiterTo());
		column.setName(column.getUpdate().getName());
		return runs;
	}


	/**
	 * 修改数据类型
	 * 1.ADD NEW COLUMN
	 * 2.FORMAT VALUE
	 * 3.MOVE VALUE
	 * alter table tb modify (name nvarchar2(20))
	 * @param column 列
	 * @return sql
	 */
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Column update = column.getUpdate();
		String name = column.getName();
		String type = column.getTypeName();
		if(type.contains("(")){
			type = type.substring(0,type.indexOf("("));
		}
		String uname = update.getName();
		String utype = update.getTypeName();
		if(uname.endsWith("_TMP_UPDATE_TYPE")){
			runs.addAll(buildDropRun(runtime, update));
		}else {
			if (utype != null && utype.contains("(")) {
				utype = utype.substring(0, utype.indexOf("("));
			}
			if (!type.equals(utype)) {
				String tmp_name = column.getName() + "_TMP_UPDATE_TYPE";

				update.setName(tmp_name);
				runs.addAll(buildRenameRun(runtime, column));

				update.setName(uname);
				runs.addAll(buildAddRun(runtime, update));

				StringBuilder builder = new StringBuilder();
				builder.append("UPDATE ");
				name(runtime, builder, column.getTable(true));
				builder.append(" SET ");
				SQLUtil.delimiter(builder, uname, getDelimiterFr(), getDelimiterTo());
				builder.append(" = ");
				SQLUtil.delimiter(builder, tmp_name, getDelimiterFr(), getDelimiterTo());
				runs.add(new SimpleRun(builder));

				column.setName(tmp_name);
				List<Run> drop = buildDropRun(runtime, column);
				runs.addAll(drop);

				column.setName(name);
				update.setName(name);
				column.setNullable(update.isNullable());

			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("ALTER TABLE ");
				name(runtime, builder, column.getTable(true));
				builder.append(" MODIFY(");
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
				type(runtime, builder, column.getUpdate());
				builder.append(")");
				runs.add(new SimpleRun(builder));
			}
		}
		// column.setName(name);
		return runs;
	}

	/**
	 * 修改默认值
	 * ALTER TABLE MY_TEST_TABLE MODIFY B DEFAULT 2
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Object def = null;
		if(null != column.getUpdate()){
			def = column.getUpdate().getDefaultValue();
		}else {
			def = column.getDefaultValue();
		}
		builder.append("ALTER TABLE ");
		name(runtime, builder, column.getTable(true)).append(" MODIFY ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" DEFAULT ");
		if(null != def){
			def = write(runtime, column, def, false);
			//format(builder, def);
			builder.append(def);
		}else{
			builder.append(" NULL");
		}
		return runs;
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE T  MODIFY C NOT NULL ;
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		int nullable = column.isNullable();
		int uNullable = column.getUpdate().isNullable();
		if(nullable != -1 && uNullable != -1){
			if(nullable == uNullable){
				return runs;
			}

			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("ALTER TABLE ");
			name(runtime, builder, column.getTable(true)).append(" MODIFY ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			if(uNullable == 0){
				builder.append(" NOT ");
			}
			builder.append(" NULL");
			column.setNullable(uNullable);
		}
		return runs;
	}

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param column 列
	 * @return sql
	 * @throws Exception 异常
	 */
	public List<Run> buildAppendCommentRun(DataRuntime runtime, Column column) throws Exception {
		return buildChangeCommentRun(runtime, column);
	}
	/**
	 * 修改备注
	 * COMMENT ON COLUMN T.ID IS 'ABC'
	 * @param column 列
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Column column) throws Exception{
		List<Run> runs = new ArrayList<>();
		String comment = null;
		if(null != column.getUpdate()){
			comment = column.getUpdate().getComment();
		}else {
			comment = column.getComment();
		}
		if(BasicUtil.isNotEmpty(comment)) {
			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("COMMENT ON COLUMN ");
			name(runtime, builder, column.getTable(true)).append(".");
			Column update = column.getUpdate();
			String name = null;
			if(null != update){
				name = update.getName();
			}else{
				name = column.getName();
			}
			SQLUtil.delimiter(builder, name, getDelimiterFr(), getDelimiterTo());
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
	public List<Run> buildDropAutoIncrement(DataRuntime runtime, Column column) throws Exception{
		return super.buildDropAutoIncrement(runtime, column);
	}
	/**
	 * 定义列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder define(DataRuntime runtime, StringBuilder builder, Column column){
		return super.define(runtime, builder, column);
	}
	/**
	 * 数据类型
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder type(DataRuntime runtime, StringBuilder builder, Column column){
		return super.type(runtime, builder, column);
	}

	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder nullable(DataRuntime runtime, StringBuilder builder, Column column){
		return super.nullable(runtime, builder, column);
	}
	/**
	 * 编码
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder charset(DataRuntime runtime, StringBuilder builder, Column column){
		return super.charset(runtime, builder, column);
	}
	/**
	 * 默认值
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder defaultValue(DataRuntime runtime, StringBuilder builder, Column column){
		return super.defaultValue(runtime, builder, column);
	}

	/**
	 * 递增列
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder increment(DataRuntime runtime, StringBuilder builder, Column column){
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
	public StringBuilder onupdate(DataRuntime runtime, StringBuilder builder, Column column){
		return super.onupdate(runtime, builder, column);
	}

	/**
	 * 位置
	 *
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder position(DataRuntime runtime, StringBuilder builder, Column column){
		return super.position(runtime, builder, column);
	}
	/**
	 * 备注
	 *
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder comment(DataRuntime runtime, StringBuilder builder, Column column){
		return super.comment(runtime, builder, column);
	}


	/**
	 * 创建或删除列时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkColumnExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkColumnExists(runtime, builder, exists);
	}
	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildAlterRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildDropRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildRenameRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag)
	 * List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag)
	 * StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists)
	 ******************************************************************************************************************/

	/**
	 * 添加标签
	 * ALTER TABLE  HR_USER ADD TAG UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildAddRun(runtime, tag);
	}


	/**
	 * 修改标签 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param tag 标签
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildAlterRun(runtime, tag);
	}


	/**
	 * 删除标签
	 * ALTER TABLE HR_USER DROP TAG NAME;
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildDropRun(runtime, tag);
	}


	/**
	 * 修改标签名
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Tag tag)  throws Exception{
		return super.buildRenameRun(runtime, tag);
	}

	/**
	 * 修改默认值
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeDefaultRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeDefaultRun(runtime, tag);
	}

	/**
	 * 修改非空限制
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeNullableRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeNullableRun(runtime, tag);
	}
	/**
	 * 修改备注
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public List<Run> buildChangeCommentRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeCommentRun(runtime, tag);
	}

	/**
	 * 修改数据类型
	 *
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<Run> buildChangeTypeRun(DataRuntime runtime, Tag tag) throws Exception{
		return super.buildChangeTypeRun(runtime, tag);
	}

	/**
	 * 创建或删除标签时检测是否存在
	 * @param builder builder
	 * @param exists exists
	 * @return sql
	 */
	@Override
	public StringBuilder checkTagExists(DataRuntime runtime, StringBuilder builder, boolean exists){
		return super.checkTagExists(runtime, builder, exists);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 * List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 * List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Map<String,Column> columns = primary.getColumns();
		if(columns.size()>0) {
			Run run = new SimpleRun();
			runs.add(run);
			StringBuilder builder = run.getBuilder();
			builder.append("ALTER TABLE ");
			name(runtime, builder, primary.getTable(true));
			builder.append(" ADD CONSTRAINT ").append(primary.getTableName(true)).append("_PK").append(" PRIMARY KEY(");
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
	public List<Run> buildAlterRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		return super.buildAlterRun(runtime, primary);
	}

	/**
	 * 删除主键
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("ALTER TABLE ");
		name(runtime, builder, primary.getTable(true));
		builder.append(" DROP PRIMARY KEY");
		return runs;
	}
	/**
	 * 修改主键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param primary 主键
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, PrimaryKey primary) throws Exception{
		return super.buildRenameRun(runtime, primary);
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 ******************************************************************************************************************/

	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildAddRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildAddRun(runtime, foreign);
	}
	/**
	 * 添加外键
	 * @param foreign 外键
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildAlterRun(runtime, foreign);
	}

	/**
	 * 删除外键
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildDropRun(runtime, foreign);
	}

	/**
	 * 修改外键名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param foreign 外键
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, ForeignKey foreign) throws Exception{
		return super.buildRenameRun(runtime, foreign);
	}

	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception
	 * PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的主键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryPrimaryRun(DataRuntime runtime, Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT A.*,  B.COLUMN_NAME , B.POSITION \n");
		builder.append("FROM ALL_CONSTRAINTS A, ALL_CONS_COLUMNS B WHERE B.OWNER =A.OWNER AND A.TABLE_NAME =B.TABLE_NAME \n");
		builder.append("AND A.CONSTRAINT_TYPE = 'P'\n");
		builder.append("AND A.TABLE_NAME = '").append(objectName(runtime, table.getName())).append("'\n");
		if(BasicUtil.isNotEmpty(table.getSchema())){
			builder.append(" AND A.OWNER = '").append(table.getSchema()).append("'");
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryIndexRun 返回顺序
	 * @param table 表
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public PrimaryKey primary(DataRuntime runtime, int index, Table table, DataSet set) throws Exception{
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
			column.setPosition(row.getInt("POSITION",0));
			primary.addColumn(column);
		}
		return primary;
	}


	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的外键
	 * @param table 表
	 * @return sqls
	 */
	public List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		builder.append("SELECT UC.CONSTRAINT_NAME, UC.TABLE_NAME, KCU.COLUMN_NAME, UC.R_CONSTRAINT_NAME, RC.TABLE_NAME AS REFERENCED_TABLE_NAME, RCC.COLUMN_NAME AS REFERENCED_COLUMN_NAME, RCC.POSITION AS ORDINAL_POSITION\n");
		builder.append("FROM USER_CONSTRAINTS UC \n");
		builder.append("JOIN USER_CONS_COLUMNS KCU ON UC.CONSTRAINT_NAME = KCU.CONSTRAINT_NAME \n");
		builder.append("JOIN USER_CONSTRAINTS RC ON UC.R_CONSTRAINT_NAME = RC.CONSTRAINT_NAME \n");
		builder.append("JOIN USER_CONS_COLUMNS RCC ON RC.CONSTRAINT_NAME = RCC.CONSTRAINT_NAME AND KCU.POSITION = RCC.POSITION");
		if(null != table){
			if(BasicUtil.isNotEmpty(table.getCatalog())){
				builder.append(" AND OWNER = '").append(table.getCatalog()).append("'\n");
			}
			builder.append(" AND UC.TABLE_NAME = '").append(table.getName()).append("'\n");
		}
		return runs;
	}

	/**
	 *  根据查询结果集构造PrimaryKey
	 * @param index 第几条查询SQL 对照 buildQueryForeignsRun 返回顺序
	 * @param table 表
	 * @param foreigns 上一步查询结果
	 * @param set sql查询结果
	 * @throws Exception 异常
	 */
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception{
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
	 * List<Run> buildAddRun(DataRuntime runtime, Index index) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Index index) throws Exception
	 * List<Run> buildDropRun(DataRuntime runtime, Index index) throws Exception
	 * List<Run> buildRenameRun(DataRuntime runtime, Index index) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildAddRun(runtime, index);
	}
	/**
	 * 修改索引
	 * 有可能生成多条SQL
	 * @param index 索引
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildAlterRun(runtime, index);
	}

	/**
	 * 删除索引
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Index index) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		Table table = index.getTable(true);
		if(index.isPrimary()){
			builder.append("ALTER TABLE ");
			name(runtime, builder, table);
			builder.append(" DROP CONSTRAINT ").append(index.getName());
		}else {
			builder.append("DROP INDEX ").append(index.getName());
		}
		return runs;
	}
	/**
	 * 修改索引名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param index 索引
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Index index) throws Exception{
		return super.buildRenameRun(runtime, index);
	}
	/**
	 * 索引备注
	 * @param builder
	 * @param index
	 */
	public void comment(DataRuntime runtime, StringBuilder builder, Index index){
		super.comment(runtime, builder, index);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildAddRun(DataRuntime runtime, Constraint constraint) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Constraint constraint) throws Exception
	 * List<Run> buildDropRun(DataRuntime runtime, Constraint constraint) throws Exception
	 * List<Run> buildRenameRun(DataRuntime runtime, Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 添加约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildAddRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildAddRun(runtime, constraint);
	}
	/**
	 * 修改约束
	 * 有可能生成多条SQL
	 * @param constraint 约束
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildAlterRun(runtime, constraint);
	}

	/**
	 * 删除约束
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildDropRun(runtime, constraint);
	}
	/**
	 * 修改约束名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param constraint 约束
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Constraint constraint) throws Exception{
		return super.buildRenameRun(runtime, constraint);
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
	 * 添加触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildCreateRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildCreateRun(runtime, trigger);
	}
	public void each(DataRuntime runtime, StringBuilder builder, Trigger trigger){
		super.each(runtime, builder, trigger);
	}
	/**
	 * 修改触发器
	 * 有可能生成多条SQL
	 * @param trigger 触发器
	 * @return List
	 */
	@Override
	public List<Run> buildAlterRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildAlterRun(runtime, trigger);
	}

	/**
	 * 删除触发器
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildDropRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildDropRun(runtime, trigger);
	}

	/**
	 * 修改触发器名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param trigger 触发器
	 * @return String
	 */
	@Override
	public List<Run> buildRenameRun(DataRuntime runtime, Trigger trigger) throws Exception{
		return super.buildRenameRun(runtime, trigger);
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Procedure procedure) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Procedure procedure) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 添加存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildCreateRun(runtime, procedure);
	}

	/**
	 * 修改存储过程
	 * 有可能生成多条SQL
	 * @param procedure 存储过程
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildAlterRun(runtime, procedure);
	}

	/**
	 * 删除存储过程
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildDropRun(runtime, procedure);
	}

	/**
	 * 修改存储过程名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param procedure 存储过程
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Procedure procedure) throws Exception{
		return super.buildRenameRun(runtime, procedure);
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildCreateRun(DataRuntime runtime, Function function) throws Exception
	 * List<Run> buildAlterRun(DataRuntime runtime, Function function) throws Exception;
	 * List<Run> buildDropRun(DataRuntime runtime, Function function) throws Exception;
	 * List<Run> buildRenameRun(DataRuntime runtime, Function function) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 添加函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildCreateRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildCreateRun(runtime, function);
	}

	/**
	 * 修改函数
	 * 有可能生成多条SQL
	 * @param function 函数
	 * @return List
	 */
	public List<Run> buildAlterRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildAlterRun(runtime, function);
	}

	/**
	 * 删除函数
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildDropRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildDropRun(runtime, function);
	}

	/**
	 * 修改函数名
	 * 一般不直接调用,如果需要由buildAlterRun内部统一调用
	 * @param function 函数
	 * @return String
	 */
	public List<Run> buildRenameRun(DataRuntime runtime, Function function) throws Exception{
		return super.buildRenameRun(runtime, function);
	}

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *------------------------------------------------------------------------------------------------------------------
	 * boolean isBooleanColumn(DataRuntime runtime, Column column)
	 *  boolean isNumberColumn(DataRuntime runtime, Column column)
	 * boolean isCharColumn(DataRuntime runtime, Column column)
	 * String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value)
	 * String type(String type)
	 * String type2class(String type)
	 * void value(DataRuntime runtime, StringBuilder builder, Object obj, String key)
	 ******************************************************************************************************************/

	@Override
	public boolean isBooleanColumn(DataRuntime runtime, Column column) {
		return super.isBooleanColumn(runtime, column);
	}
	/**
	 * 是否同数字
	 * @param column 列
	 * @return boolean
	 */
	@Override
	public  boolean isNumberColumn(DataRuntime runtime, Column column){
		return super.isNumberColumn(runtime, column);
	}

	@Override
	public boolean isCharColumn(DataRuntime runtime, Column column) {
		return super.isCharColumn(runtime, column);
	}
	/**
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "sysdate";
		}
		return null;
	}

}
