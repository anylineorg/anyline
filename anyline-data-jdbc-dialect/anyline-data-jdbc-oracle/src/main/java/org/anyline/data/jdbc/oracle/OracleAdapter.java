 
package org.anyline.data.jdbc.oracle;

import org.anyline.dao.AnylineDao;
import org.anyline.data.entity.*;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.SQLAdapter;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.run.TextRun;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.util.*;
import org.anyline.entity.data.Column.STANDARD_DATA_TYPE;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Repository("anyline.data.jdbc.adapter.oracle") 
public class OracleAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {

	public static boolean IS_GET_SEQUENCE_VALUE_BEFORE_INSERT = false;
	@Autowired(required = false) 
	@Qualifier("anyline.dao") 
	protected AnylineDao dao; 

	public DB_TYPE type(){
		return DB_TYPE.ORACLE; 
	}


	public enum DATA_TYPE{
		BFILE{
			public String getName(){return "BFILE";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.BFILE;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		BINARY_DOUBLE{
			public String getName(){return "BINARY_DOUBLE";}   public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.BINARY_DOUBLE;}
			public Object convert(Object value, boolean string){
				return value;
			}},
		BINARY_FLOAT{
			public String getName(){return "BINARY_FLOAT";}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.BINARY_FLOAT;}
			public Object convert(Object value, boolean string){
				return value;
			}},
		BLOB{
			public String getName(){return "BLOB";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.BLOB;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		CHAR{
			public String getName(){return "CHAR";}            public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.CHAR;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		CLOB{
			public String getName(){return "CLOB";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.CLOB;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		DATE{
			public String getName(){return "DATE";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.DATE;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		DECIMAL{
			public String getName(){return "NUMBER";}          public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		FLOAT{
			public String getName(){return "FLOAT";}           public boolean isIgnorePrecision(){return true;}   	public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.FLOAT;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		INT{
			public String getName(){return "NUMBER";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		INTEGER{
			public String getName(){return "NUMBER";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		LONG{
			public String getName(){return "LONG";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		NCHAR{
			public String getName(){return "NCHAR";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NCHAR;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		NCLOB{
			public String getName(){return "NCLOB";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NCLOB;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		NUMBER{
			public String getName(){return "NUMBER";}          public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		NUMERIC{
			public String getName(){return "NUMBER";}          public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		NVARCHAR2{
			public String getName(){return "NVARCHAR2";}     	public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NVARCHAR2;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		RAW{
			public String getName(){return "RAW";}           	public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.RAW;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		REAL{
			public String getName(){return "FLOAT";}           public boolean isIgnorePrecision(){return true;}   	public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.FLOAT;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		ROWID{
			public String getName(){return "ROWID";}         	public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.ROWID;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		SMALINT{
			public String getName(){return "NUMBER";}          public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.NUMBER;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		TIMESTAMP{
			public String getName(){return "TIMESTAMP";}       public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.TIMESTAMP;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		UROWID{
			public String getName(){return "UROWID";}      	public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.UROWID;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		VARCHAR{
			public String getName(){return "VARCHAR";}         public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.VARCHAR;	}
			public Object convert(Object value, boolean string){
				return value;
			}},
		VARCHAR2{
			public String getName(){return "VARCHAR2";}        public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public STANDARD_DATA_TYPE getStandard(){return STANDARD_DATA_TYPE.VARCHAR2;	}
			public Object convert(Object value, boolean string){
				return value;
			}};

		/**
		 * 格式转换
		 * @param value value
		 * @param string 是否板式化成String, 拼接SQL(不用占位符)情况下需要
		 * @return Object
		 */
		public abstract Object convert(Object value, boolean string);
		public abstract STANDARD_DATA_TYPE getStandard();
		public abstract String getName();
		public abstract boolean isIgnorePrecision();
		public abstract boolean isIgnoreScale();
	}

	@Value("${anyline.jdbc.delimiter.oracle:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}

	public OracleAdapter(){
		delimiterFr = "";
		delimiterTo = "";
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
		if(null == navi){
			builder.append(sql).append("\n").append(order); 
		}else{ 
			// 分页 
				builder.append("SELECT "+cols+" FROM( \n");
				builder.append("SELECT TAB_I.* ,ROWNUM AS ROW_NUMBER \n"); 
				builder.append("FROM( \n"); 
				builder.append(sql);
				builder.append("\n").append(order);
				builder.append(")  TAB_I \n");
				builder.append(")  TAB_O WHERE ROW_NUMBER >= "+(first+1)+" AND ROW_NUMBER <= "+(last+1));

		} 
		 
		return builder.toString(); 
		 
	}

	@Override
	public String concat(String ... args){
		return concatOr(args);
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
					if(OracleAdapter.IS_GET_SEQUENCE_VALUE_BEFORE_INSERT) {
						createPrimaryValue(template, set, str);
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
		for(DataRow row:set) {
			if(row.hasPrimaryKeys() && null != primaryGenerator){
				createPrimaryValue(row, type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), row.getPrimaryKeys(), null);
			}

			if(col > 0){
				builder.append("\n\tUNION ALL");
			}
			builder.append("\n\tSELECT ");
			insertValue(template, run, row, true, true,false, keys);
			builder.append(" FROM DUAL ");
			col ++;
		}
		builder.append(") M ");
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
					if(OracleAdapter.IS_GET_SEQUENCE_VALUE_BEFORE_INSERT) {
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
	 * public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, Table> tables(boolean create, LinkedHashMap<String, Table> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception
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
		builder.append("SELECT T.TABLE_NAME, T.OWNER, TC.COMMENTS \n");
		builder.append("FROM SYS.ALL_ALL_TABLES T \n");
		builder.append("LEFT JOIN SYS.ALL_TAB_COMMENTS TC  ON  TC.OWNER  = T.OWNER  AND TC.TABLE_NAME  = T.TABLE_NAME \n");
		builder.append("WHERE T.IOT_NAME IS NULL  AND T.NESTED = 'NO'  AND T.SECONDARY = 'N' ");
		if(BasicUtil.isNotEmpty(schema)){
			builder.append("AND T.OWNER = '").append(schema).append("'");
		}
		if(BasicUtil.isNotEmpty(pattern)){
			builder.append(" AND T.TABLE_NAME = '").append(pattern).append("'");
		}

		sqls.add(builder.toString());
		return sqls;
	}

	@Override
	public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("TABLE_NAME");
			Table table = tables.get(name.toUpperCase());
			if(null == table){
				table = new Table();
			}
			table.setCatalog(catalog);
			table.setSchema(schema);
			table.setName(name);
			table.setComment(row.getString("COMMENTS"));
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
	 * public List<String> buildQueryMasterTableRunSQL(String catalog, String schema, String pattern, String types)
	 * public LinkedHashMap<String, MasterTable> mtables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, MasterTable> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, MasterTable> mtables(boolean create, LinkedHashMap<String, MasterTable> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception
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
	 * public List<String> buildQueryPartitionTableRunSQL(String catalog, String schema, String pattern, String types)
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name)
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags)
	 * public LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception
	 * public LinkedHashMap<String, PartitionTable> ptables(boolean create, LinkedHashMap<String, PartitionTable> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception
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
	 * public List<String> buildQueryColumnRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, Table table, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryColumnRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryColumnRunSQL(table, metadata);
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
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
		return super.columns(index, create, table, columns, set);
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
	 * public List<String> buildQueryTagRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Tag> tags(int index, boolean create, Table table, LinkedHashMap<String, Tag> tags, DataSet set) throws Exception
	 * public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Tag> tags(boolean create, LinkedHashMap<String, Tag> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception
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
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildQueryIndexRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Index> indexs(int index, boolean create, Table table, LinkedHashMap<String, Index> indexs, DataSet set) throws Exception
	 * public LinkedHashMap<String, Index> indexs(boolean create, Table table, LinkedHashMap<String, Index> indexs, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Index> indexs(boolean create, LinkedHashMap<String, Index> indexs, DatabaseMetaData dbmd, Table table, boolean unique, boolean approximate) throws Exception
	 ******************************************************************************************************************/
	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sql
	 */
	@Override
	public List<String> buildQueryIndexRunSQL(Table table, boolean metadata) throws Exception{
		return super.buildQueryIndexRunSQL(table, metadata);
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
	 * public List<String> buildQueryConstraintRunSQL(Table table, boolean metadata)
	 * public LinkedHashMap<String, Constraint> constraints(int constraint, boolean create,  Table table, LinkedHashMap<String, Constraint> constraints, DataSet set) throws Exception
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, SqlRowSet set) throws Exception
	 * public LinkedHashMap<String, Constraint> constraints(boolean create, Table table, LinkedHashMap<String, Constraint> constraints, ResultSet set) throws Exception
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

	/**
	 * 添加表备注(表创建完成后调用,创建过程能添加备注的不需要实现)
	 * @param table 表
	 * @return sql
	 * @throws Exception 异常
	 */
	public String buildCreateCommentRunSQL(Table table) throws Exception {
		return super.buildCreateCommentRunSQL(table);
	}
	@Override
	public List<String> buildAlterRunSQL(Table table) throws Exception{
		return super.buildAlterRunSQL(table);
	}
	/**
	 * 修改表名
	 * ALTER TABLE A RENAME TO B;
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) throws Exception {
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
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Table table) throws Exception{
		String comment = table.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			StringBuilder builder = new StringBuilder();
			builder.append("COMMENT ON TABLE ");
			name(builder, table);
			builder.append(" IS '").append(comment).append("'");
			return builder.toString();
		}else{
			return null;
		}
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
		return super.comment(builder, table);
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
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public List<String> buildCreateRunSQL(MasterTable table)
	 * public String buildCreateCommentRunSQL(MasterTable table)
	 * public List<String> buildAlterRunSQL(MasterTable table)
	 * public String buildDropRunSQL(MasterTable table)
	 * public String buildRenameRunSQL(MasterTable table)
	 * public String buildChangeCommentRunSQL(MasterTable table)
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
	 * public String buildCreateRunSQL(PartitionTable table)
	 * public List<String> buildAlterRunSQL(PartitionTable table)
	 * public String buildDropRunSQL(PartitionTable table)
	 * public String buildRenameRunSQL(PartitionTable table)
	 * public String buildChangeCommentRunSQL(PartitionTable table)
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
	 * public String alterColumnKeyword()
	 * public String buildAddRunSQL(Column column)
	 * public List<String> buildAlterRunSQL(Column column)
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
		return "ALTER";
	}

	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD  UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column) throws Exception{
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER TABLE ");
		name(builder, table);
		// Column update = column.getUpdate();
		// if(null == update){
		// 添加列
		builder.append(" ADD ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, column);
		//}
		return builder.toString();
	}

	/**
	 * 修改列 ALTER TABLE  HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return List
	 */
	@Override
	public List<String> buildAlterRunSQL(Column column) throws Exception{
		return super.buildAlterRunSQL(column);
	}


	/**
	 * 删除列
	 * ALTER TABLE HR_USER DROP COLUMN NAME;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Column column) throws Exception{
		return super.buildDropRunSQL(column);
	}

	/**
	 * 修改列名
	 * 
	 * ALTER TABLE 表名 RENAME COLUMN RENAME 老列名 TO 新列名
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column)  throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable());
		builder.append(" RENAME COLUMN ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" TO ");
		SQLUtil.delimiter(builder, column.getNewName(), getDelimiterFr(), getDelimiterTo());
		return builder.toString();
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
	public List<String> buildChangeTypeRunSQL(Column column) throws Exception{
		List<String> sqls = new ArrayList<>();
		Column update = column.getUpdate();
		String name = column.getName();
		String type = column.getTypeName();
		if(type.contains("(")){
			type = type.substring(0,type.indexOf("("));
		}
		String uname = update.getName();
		String utype = update.getTypeName();
		if(uname.endsWith("_TMP_UPDATE_TYPE")){
			sqls.add(buildDropRunSQL(update));
		}else {
			if (utype != null && utype.contains("(")) {
				utype = utype.substring(0, utype.indexOf("("));
			}
			if (!type.equals(utype)) {
				String tmp_name = column.getName() + "_TMP_UPDATE_TYPE";

				update.setName(tmp_name);
				String rename = buildRenameRunSQL(column);
				sqls.add(rename);

				update.setName(uname);
				String add = buildAddRunSQL(update);
				sqls.add(add);

				StringBuilder builder = new StringBuilder();
				builder.append("UPDATE ");
				name(builder, column.getTable());
				builder.append(" SET ");
				SQLUtil.delimiter(builder, uname, getDelimiterFr(), getDelimiterTo());
				builder.append(" = ");
				SQLUtil.delimiter(builder, tmp_name, getDelimiterFr(), getDelimiterTo());
				sqls.add(builder.toString());

				column.setName(tmp_name);
				String drop = buildDropRunSQL(column);
				sqls.add(drop);

				column.setName(name);
				update.setName(tmp_name);
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append("ALTER TABLE ");
				name(builder, column.getTable());
				builder.append(" MODIFY(");
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
				type(builder, column.getUpdate());
				builder.append(")");
				sqls.add(builder.toString());
			}
		}
		// column.setName(name);
		return sqls;
	}

	/**
	 * 修改默认值
	 * ALTER TABLE MY_TEST_TABLE MODIFY B DEFAULT 2
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Column column) throws Exception{
		Object def = column.getDefaultValue();
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, column.getTable()).append(" MODIFY ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
		builder.append(" DEFAULT ");
		if(null != def){
			format(builder, def);
		}else{
			builder.append("NULL");
		}
		return builder.toString();
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE T  MODIFY C NOT NULL ;
	 * @param column 列
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
			name(builder, column.getTable()).append(" MODIFY ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			if(uNullable == 0){
				builder.append(" NOT ");
			}
			builder.append("NULL");
			return builder.toString();
		}
		return null;
	}

	/**
	 * 修改备注
	 * COMMENT ON COLUMN T.ID IS 'ABC'
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Column column) throws Exception{
		String comment = column.getComment();
		if(BasicUtil.isNotEmpty(comment)) {
			StringBuilder builder = new StringBuilder();
			builder.append("COMMENT ON COLUMN ");
			name(builder, column.getTable()).append(".");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			builder.append(" IS '").append(comment).append("'");
			return builder.toString();
		}else{
			return null;
		}
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
		return super.increment(builder, column);
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
	 * public String buildAddRunSQL(Tag tag)
	 * public List<String> buildAlterRunSQL(Tag tag)
	 * public String buildDropRunSQL(Tag tag)
	 * public String buildRenameRunSQL(Tag tag)
	 * public String buildChangeDefaultRunSQL(Tag tag)
	 * public String buildChangeNullableRunSQL(Tag tag)
	 * public String buildChangeCommentRunSQL(Tag tag)
	 * public List<String> buildChangeTypeRunSQL(Tag tag)
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
	 * @return List
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
			builder.append(" ADD CONSTRAINT ").append(primary.getTableName()).append("_PK").append(" PRIMARY KEY(");
			boolean first = true;
			for(Column column:columns.values()){
				SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
				if(!first){
					builder.append(",");
				}
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
		builder.append(" DROP PRIMARY KEY");
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
	 * public String buildInValue(SQL_BUILD_IN_VALUE value)
	 * public String type2type(String type)
	 * public String type2class(String type)
	 * public void value(StringBuilder builder, Object obj, String key)
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
	public String buildInValue(SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "sysdate";
		}
		return null;
	}
	@Override
	public String type2type(String type){
		if(null != type){
			type = type.toUpperCase();
			if("DATETIME".equals(type)){
				return "TIMESTAMP";
			}
			if("DOUBLE".equals(type)){
				return "DECIMAL";
			}
		}
		return super.type2type(type);
	}
	@Override
	public String type2class(String type){
		return super.type2class(type);
	}

	@Override
	public void value(StringBuilder builder, Object obj, String key){
		Object value = null;
		if(obj instanceof DataRow){
			value = ((DataRow)obj).get(key);
		}else if(EntityAdapterProxy.hasAdapter()){
			Field field = EntityAdapterProxy.field(obj.getClass(), key);
			value = BeanUtil.getFieldValue(obj, field);
		}else{
			value = BeanUtil.getFieldValue(obj, key);
		}
		if(null == value || "NULL".equals(value)){
			builder.append("null");
		}else if(value instanceof String){
			format(builder, value);
		}else if(value instanceof Timestamp
				|| value instanceof java.util.Date
				|| value instanceof java.sql.Date
				|| value instanceof LocalDate
				|| value instanceof LocalTime
				|| value instanceof LocalDateTime
		){
			try {
				Date date = DateUtil.parse(value);
				builder.append("TO_DATE('").append(DateUtil.format(date, DateUtil.FORMAT_DATE_TIME)).append("','yyyy-mm-dd hh24:mi:ss')");
			}catch (Exception e){
				builder.append("null");
				e.printStackTrace();
			}
		}else if(value instanceof Date){
			builder.append("TO_DATE('").append(DateUtil.format((Date)value,DateUtil.FORMAT_DATE_TIME)).append("','yyyy-mm-dd hh24:mi:ss')");
		}else if(value instanceof Number || value instanceof Boolean){
			builder.append(value);
		}else{
			builder.append(value);
		}
	}




}
