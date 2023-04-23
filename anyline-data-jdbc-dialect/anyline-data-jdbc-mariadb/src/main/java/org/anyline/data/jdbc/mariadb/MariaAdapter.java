 
package org.anyline.data.jdbc.mariadb;

import org.anyline.data.entity.*;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository("anyline.data.jdbc.adapter.mariadb")
public class MariaAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
 
	public DB_TYPE type(){
		return DB_TYPE.MYSQL; 
	} 
	 
	public MariaAdapter(){
		delimiterFr = "`";
		delimiterTo = "`";
	}

	@Value("${anyline.jdbc.delimiter.mariadb:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet()  {
		setDelimiter(delimiter);
	}
	public enum DATA_TYPE{
		BIGINT	            {public String getName(){return "bigint";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BIGINT;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		BINARY	            {public String getName(){return "binary";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BINARY;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		BIT	                {public String getName(){return "bit";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BIT;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		BLOB	            {public String getName(){return "blob";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BLOB;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		CHAR	            {public String getName(){return "char";}                public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.CHAR;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		DATE	            {public String getName(){return "date";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DATE;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		DATETIME	        {public String getName(){return "datetime";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DATETIME;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		DECIMAL	            {public String getName(){return "decimal";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DECIMAL;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		DOUBLE	            {public String getName(){return "double";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DOUBLE;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		ENUM	            {public String getName(){return "enum";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.ENUM;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		FLOAT	            {public String getName(){return "float";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.FLOAT;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		GEOMETRY	        {public String getName(){return "geometry";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.GEOMETRY;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		GEOMETRYCOLLECTION	{public String getName(){return "geometrycollection";}  public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.GEOMETRYCOLLECTION;}
			public Object convert(Object value, boolean string){
				return value;
			}},
		INT	                {public String getName(){return "int";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.INT;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		INTEGER	            {public String getName(){return "int";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.INTEGER;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		JSON	            {public String getName(){return "json";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.JSON;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		LINESTRING	        {public String getName(){return "linestring";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.LINE;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		LONGBLOB	        {public String getName(){return "longblob";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.LONGBLOB;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		LONGTEXT	        {public String getName(){return "longtext";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.LONGTEXT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		MEDIUMBLOB	        {public String getName(){return "mediumblob";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MEDIUMBLOB;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		MEDIUMINT	        {public String getName(){return "mediumint";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MEDIUMINT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		MEDIUMTEXT	        {public String getName(){return "mediumtext";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MEDIUMTEXT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		MULTILINESTRING	    {public String getName(){return "multilinestring";}     public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MULTILINESTRING;    }
			public Object convert(Object value, boolean string){
				return value;
			}},
		MULTIPOINT	        {public String getName(){return "multipoint";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MULTIPOINT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		MULTIPOLYGON	    {public String getName(){return "multipolygon";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MULTIPOLYGON;	   }
			public Object convert(Object value, boolean string){
				return value;
			}},
		NUMERIC	            {public String getName(){return "numeric";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.NUMERIC;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		POINT	            {public String getName(){return "point";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.POINT;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		POLYGON	            {public String getName(){return "polygon";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.POLYGON;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		REAL	            {public String getName(){return "double";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.REAL;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		SET	                {public String getName(){return "set";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.SET;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		SMALLINT	        {public String getName(){return "smallint";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.SMALLINT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		TEXT	            {public String getName(){return "text";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TEXT;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		TIME	            {public String getName(){return "time";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TIME;	           }
			public Object convert(Object value, boolean string){
				return value;
			}},
		TIMESTAMP	        {public String getName(){return "timestamp";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TIMESTAMP;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		TINYBLOB	        {public String getName(){return "tinyblob";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TINYBLOB;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		TINYINT	            {public String getName(){return "tinyint";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TINYINT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		TINYTEXT	        {public String getName(){return "tinytext";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TINYTEXT;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		VARBINARY	        {public String getName(){return "varbinary";}           public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.VARBINARY;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		VARCHAR	            {public String getName(){return "varchar";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.VARCHAR;	       }
			public Object convert(Object value, boolean string){
				return value;
			}},
		YEAR	            {public String getName(){return "numeric";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.YEAR;	           }
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
		public abstract org.anyline.data.entity.DataType getStandard();
		public abstract String getName();
		public abstract boolean isIgnorePrecision();
		public abstract boolean isIgnoreScale();
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
			int limit = navi.getLastRow() - navi.getFirstRow() + 1; 
			if(limit < 0){ 
				limit = 0; 
			} 
			sql += " LIMIT " + navi.getFirstRow() + "," + limit; 
		} 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
 
	public String concat(String ... args){
		return concatFun(args);
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
	 * public List<String> buildQueryTableRunSQL(String catalog, String schema, String pattern, String types);
	 * public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Table> tables(boolean create, LinkedHashMap<String, Table> tables, DatabaseMetaData dbmd, String catalog, String schema, String pattern, String ... types) throws Exception;
	 ******************************************************************************************************************/
	/**
	 * 查询表
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types
	 * @return String
	 */

	/**
	 * 查询主表
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

		builder.append("SELECT * FROM information_schema.TABLES WHERE 1=1 ");
		// 8.0版本中 这个表中 TABLE_CATALOG = def  TABLE_SCHEMA = 数据库名
		/*if(BasicUtil.isNotEmpty(catalog)){
			builder.append(" AND TABLE_SCHEMA = '").append(catalog).append("'");
		}*/
		if(BasicUtil.isNotEmpty(schema)){
			builder.append(" AND TABLE_SCHEMA = '").append(schema).append("'");
		}
		if(BasicUtil.isNotEmpty(pattern)){
			builder.append(" AND TABLE_NAME LIKE '").append(pattern).append("'");
		}
		if(BasicUtil.isNotEmpty(types)){
			String[] tmps = types.split(",");
			builder.append(" AND TABLE_TYPE IN(");
			int idx = 0;
			for(String tmp:tmps){
				if(idx > 0){
					builder.append(",");
				}
				builder.append("'").append(tmp).append("'");
				idx ++;
			}
			builder.append(")");
		}else {
			builder.append(" AND TABLE_TYPE IN ('BASE TABLE','TABLE')");
		}
		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 *
	 * @param index 第几条SQL 对照buildQueryTableRunSQL返回顺序
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set set
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Table> tables(int index, boolean create, String catalog, String schema, LinkedHashMap<String, Table> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			Table table = new Table();
			table.setCatalog(row.getString("TABLE_CATALOG"));
			table.setSchema(row.getString("TABLE_SCHEMA"));
			table.setName(row.getString("TABLE_NAME"));
			table.setEngine(row.getString("ENGINE"));
			table.setComment(row.getString("TABLE_COMMENT"));
			tables.put(table.getName().toUpperCase(), table);
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
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags, String name);
	 * public List<String> buildQueryPartitionTableRunSQL(MasterTable master, Map<String,Object> tags);
	 * public LinkedHashMap<String, PartitionTable> ptables(int total, int index, boolean create, MasterTable master, String catalog, String schema, LinkedHashMap<String, PartitionTable> tables, DataSet set) throws Exception;
	 * public LinkedHashMap<String, PartitionTable> ptables(boolean create, LinkedHashMap<String, PartitionTable> tables, DatabaseMetaData dbmd, String catalog, String schema, MasterTable master) throws Exception;
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
	 * public List<String> buildQueryColumnRunSQL(Table table, boolean metadata);
	 * public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, Table table, SqlRowSet set) throws Exception;
	 * public LinkedHashMap<String, Column> columns(boolean create, LinkedHashMap<String, Column> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception;
	 ******************************************************************************************************************/

	/**
	 * 查询表上的列
	 * @param table 表
	 * @return sql
	 */

	/**
	 * 查询表上的列
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
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
			builder.append("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE 1=1 ");
			/*if(BasicUtil.isNotEmpty(catalog)){
				builder.append(" AND TABLE_CATALOG = '").append(catalog).append("'");
			}*/
			if(BasicUtil.isNotEmpty(schema)){
				builder.append(" AND TABLE_SCHEMA = '").append(schema).append("'");
			}
			builder.append(" AND TABLE_NAME = '").append(table.getName()).append("'");
		}
		sqls.add(builder.toString());
		return sqls;
	}

	/**
	 * 根据查询结果集构造Tag
	 * @param index 第几条SQL 对照 buildQueryColumnRunSQL返回顺序
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public LinkedHashMap<String, Column> columns(int index, boolean create, Table table, LinkedHashMap<String, Column> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			Column column = new Column();
			column.setCatalog(row.getString("TABLE_CATALOG"));
			column.setSchema(row.getString("TABLE_SCHEMA"));
			column.setTable(row.getString("TABLE_NAME"));
			column.setName(row.getString("COLUMN_NAME"));
			column.setPosition(row.getInt("ORDINAL_POSITION", 0));
			column.setComment(row.getString("COLUMN_COMMENT"));
			column.setTypeName(row.getString("DATA_TYPE"));
			column.setDefaultValue(row.get("COLUMN_DEFAULT"));
			column.setNullable(row.getBoolean("IS_NULLABLE", null));
			int len = row.getInt("CHARACTER_MAXIMUM_LENGTH",-1);
			if(len == -1){
				len = row.getInt("NUMERIC_PRECISION",0);
			}
			column.setPrecision(len);
			column.setScale(row.getInt("NUMERIC_SCALE",0));
			column.setCharset(row.getString("CHARACTER_SET_NAME"));
			column.setCollate(row.getString("COLLATION_NAME"));
			columns.put(column.getName().toUpperCase(), column);
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
	 * 不支持
	 * @param table 表
	 * @param metadata 是否根据metadata | 查询系统表
	 * @return sqls
	 */
	@Override
	public List<String> buildQueryTagRunSQL(Table table, boolean metadata) throws Exception{
		return null;
	}

	/**
	 * 不支持
	 * 根据查询结果集构造Tag
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
		return null;
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, Table table, LinkedHashMap<String, Tag> tags, SqlRowSet set) throws Exception{
		return null;
	}
	@Override
	public LinkedHashMap<String, Tag> tags(boolean create, LinkedHashMap<String, Tag> tags, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		return null;
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
	 * public List<String> buildCreateRunSQL(Table table);
	 * public String buildCreateCommentRunSQL(Table table);
	 * public List<String> buildAlterRunSQL(Table table);
	 * public String buildRenameRunSQL(Table table);
	 * public String buildChangeCommentRunSQL(Table table);
	 * public String buildDropRunSQL(Table table);
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
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param table 表
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) throws Exception{
		StringBuilder builder = new StringBuilder();
		builder.append("RENAME TABLE ");
		name(builder, table);
		builder.append(" TO ");
		name(builder, table.getUpdate());
		return builder.toString();
	}
	/**
	 * 修改表备注
	 *  ALTER TABLE T COMMENT 'ABC';
	 * @param table 表
	 * @return sql
	 */
	@Override
	public String buildChangeCommentRunSQL(Table table) {
		String comment = table.getComment();
		if(BasicUtil.isEmpty(comment)){
			return null;
		}
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" COMMENT '").append(comment).append("'");
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
		return super.checkTableExists(builder, exists);
	}


	/**
	 * 主键
	 * @param builder builder
	 * @param table 表
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
				if(BasicUtil.isNotEmpty(order)){
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
	 * public List<String> buildCreateRunSQL(MasterTable table);
	 * public String buildCreateCommentRunSQL(MasterTable table);
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
		return super.buildChangeCommentRunSQL(table);
	}


	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public String buildCreateRunSQL(PartitionTable table);
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
		return "ALTER COLUMN";
	}


	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD COLUMN UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column) throws Exception{
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER TABLE ");
		name(builder, table);
		Column update = column.getUpdate();
		if(null == update){
			// 添加列
			builder.append(" ADD COLUMN ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			// 数据类型
			type(builder, column);
			// 编码
			charset(builder, column);
			// 默认值
			defaultValue(builder, column);
			// 非空
			nullable(builder, column);
			// 更新事件
			onupdate(builder, column);
			// 备注
			comment(builder, column);
			// 位置
			position(builder, column);
		}
		return builder.toString();
	}

	/**
	 * 修改列 ALTER TABLE   HR_USER CHANGE UPT_TIME UPT_TIME datetime   DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP  comment '修改时间' AFTER ID;
	 * @param column 列
	 * @return sqls
	 */
	@Override
	public List<String> buildAlterRunSQL(Column column) throws Exception{
		List<String> sqls = new ArrayList<>();
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER TABLE ");
		name(builder, table);
		Column update = column.getUpdate();
		if(null != update){
			builder.append(" CHANGE ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			if(!BasicUtil.equalsIgnoreCase(column.getName(), update.getTableName())) {
				SQLUtil.delimiter(builder, update.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			}
			define(builder, update);
		}
		sqls.add(builder.toString());
		return sqls;
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
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column) throws Exception {
		return super.buildRenameRunSQL(column);
	}


	/**
	 * 修改数据类型
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Column column) throws Exception{
		return super.buildChangeTypeRunSQL(column);
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
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Column column) throws Exception{
		return super.buildChangeNullableRunSQL(column);
	}
	/**
	 * 修改备注
	 *
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column 列
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Column column) throws Exception{
		return super.buildChangeCommentRunSQL(column);
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
			builder.append(" AUTO_INCREMENT");
		}
		return builder;
	}




	/**
	 * 更新行事件
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	public StringBuilder onupdate(StringBuilder builder, Column column){
		if(column.isOnUpdate() == 1){
			builder.append(" ON UPDATE CURRENT_TIMESTAMP");
		}
		return builder;
	}
	/**
	 * 位置
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder position(StringBuilder builder, Column column){
		Integer position = column.getPosition();
		if(null != position && 0 == position){
			builder.append(" FIRST");
		}else{
			String after = column.getAfter();
			if(BasicUtil.isNotEmpty(after)){
				builder.append(" AFTER ").append(after);
			}
		}
		return builder;
	}

	/**
	 * 备注
	 * @param builder builder
	 * @param column 列
	 * @return builder
	 */
	@Override
	public StringBuilder comment(StringBuilder builder, Column column){
		String comment = column.getComment();
		if(BasicUtil.isNotEmpty(comment)){
			builder.append(" COMMENT '").append(comment).append("'");
		}
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
	 * 不支持
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Tag tag) throws Exception{
		return null;
	}


	/**
	 * 不支持
	 * @param tag 标签
	 * @return sqls
	 */
	@Override
	public List<String> buildAlterRunSQL(Tag tag) throws Exception{
		return null;
	}


	/**
	 * 删除标签
	 * 不支持
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildDropRunSQL(Tag tag) throws Exception{
		return null;
	}


	/**
	 * 修改标签名
	 * 不支持
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Tag tag) throws Exception{
		return null;
	}

	/**
	 * 修改默认值
	 * 不支持
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeDefaultRunSQL(Tag tag) throws Exception{
		return null;
	}

	/**
	 * 修改非空限制
	 * 不支持
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeNullableRunSQL(Tag tag) throws Exception{
		return null;
	}
	/**
	 * 修改备注
	 *
	 * 不支持
	 * @param tag 标签
	 * @return String
	 */
	@Override
	public String buildChangeCommentRunSQL(Tag tag) throws Exception{
		return null;
	}

	/**
	 * 修改数据类型
	 * 不支持
	 * @param tag 标签
	 * @return sql
	 */
	@Override
	public List<String> buildChangeTypeRunSQL(Tag tag) throws Exception{
		return null;
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
	 * public String type(String type)
	 * public String type2class(String type)
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
	public String value(SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "now()";
		}
		return null;
	}

	@Override
	public String type(String type){
		return super.type(type);
	}
	




} 
