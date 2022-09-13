 
package org.anyline.jdbc.config.db.impl.mssql; 

import org.anyline.dao.AnylineDao;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.OrderStore;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.TextSQLImpl;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SQLUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("anyline.jdbc.creater.mssql") 
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater, InitializingBean {
	 
	@Autowired(required = false) 
	@Qualifier("anyline.dao") 
	protected AnylineDao dao; 
	 
	public DB_TYPE type(){
		return DB_TYPE.MSSQL; 
	}

	@Value("${anyline.jdbc.delimiter.mssql:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet() throws Exception {
		setDelimiter(delimiter);
	}

	private static String dbVersion = ConfigTable.getString("DATABASE_VERSION"); 
	public SQLCreaterImpl(){ 
		delimiterFr = "[";
		delimiterTo = "]";
	}

	private String getDbVersion(){ 
		if(null == dbVersion){ 
			DataSet set = dao.querys(new TextSQLImpl("SELECT @@VERSION AS VS")); 
			if(set.size()>0){ 
				dbVersion = set.getString(0,"VS","")+"";
				dbVersion = dbVersion.toUpperCase().replaceAll("\\s{2,}", ""); 
				 
				if(null != dbVersion && dbVersion.contains("SERVER2000")){ 
					dbVersion = "2000"; 
				}else{ 
					dbVersion = "2005"; 
				} 
			}else{ 
				dbVersion = "2005"; 
			} 
		} 
		return dbVersion; 
	} 
	/** 
	 * 查询SQL 
	 * RunSQL 反转调用 
	 * @param run  run
	 * @return String
	 */ 
	@Override 
	public String parseFinalQueryTxt(RunSQL run){ 
		StringBuilder builder = new StringBuilder(); 
		String cols = run.getFetchColumns(); 
		PageNavi navi = run.getPageNavi(); 
		String sql = run.getBaseQueryTxt(); 
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
			//top 
			builder.append("SELECT TOP ").append(last+1).append(" "+cols+" FROM(\n"); 
			builder.append(sql).append("\n) AS _TAB_O \n"); 
			builder.append(order); 
			return builder.toString(); 
		} 
		if(null == navi){ 
			builder.append(sql).append("\n").append(order); 
		}else{ 
			//分页 
			if("2000".equals(getDbVersion())){ 
				int rows = navi.getPageRows(); 
				if(rows * navi.getCurPage() > navi.getTotalRow()){ 
					//最后一页不足10条 
					rows = navi.getTotalRow() % navi.getPageRows(); 
				} 
				String asc = order; 
				String desc = order.replace("ASC", "<A_ORDER>"); 
				desc = desc.replace("DESC", "ASC"); 
				desc = desc.replace("<A_ORDER>", "DESC"); 
				builder.append("SELECT "+cols+" FROM (\n "); 
				builder.append("SELECT TOP ").append(rows).append(" * FROM (\n"); 
				builder.append("SELECT TOP ").append(navi.getPageRows()*navi.getCurPage()).append(" * "); 
				builder.append(" FROM (" + sql + ") AS T0 ").append(asc).append("\n"); 
				builder.append(") AS T1 ").append(desc).append("\n"); 
				builder.append(") AS T2").append(asc); 
			}else{ 
				//2005 及以上 
				if(BasicUtil.isEmpty(order)){ 
					order = "ORDER BY "+ ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
				} 
				builder.append("SELECT "+cols+" FROM( \n"); 
				builder.append("SELECT _TAB_I.* ,ROW_NUMBER() OVER(") 
				.append(order) 
				.append(") AS ROW_NUMBER \n"); 
				builder.append("FROM( \n"); 
				builder.append(sql); 
				builder.append(") AS _TAB_I \n"); 
				builder.append(") AS _TAB_O WHERE ROW_NUMBER BETWEEN "+(first+1)+" AND "+(last+1)); 
			} 
			 
		} 
		 
		return builder.toString(); 
		 
	} 
	@Override 
	public String parseExistsTxt(RunSQL run){ 
		String sql = "IF EXISTS(\n" + run.getBuilder().toString() +"\n) SELECT 1 AS IS_EXISTS ELSE SELECT 0 AS IS_EXISTS"; 
		sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE"); 
		return sql; 
	} 
	public String concat(String ... args){
		return concatAdd(args);
	}


	/**
	 * 修改表名
	 * EXEC SP_RENAME 'A', 'B'
	 * @param table table
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) {
		StringBuilder builder = new StringBuilder();
		builder.append("EXEC SP_RENAME '").append(table.getName()).append("', '").append(table.getUpdate().getName()).append("'");
		return builder.toString();
	}

	/**
	 * 修改列名
	 * EXEC sp_rename '表名.列名', '新列名', 'COLUMN'
	 * @param column column
	 * @return
	 */
	@Override
	public String buildRenameRunSQL(Column column){
		StringBuilder builder = new StringBuilder();
		builder.append("EXEC SP_RENAME '").append(column.getTableName()).append(".").append(column.getName()).append("' , '").append(column.getUpdate().getName()).append("','COLUMN' ");
		return builder.toString();
	}

	/**
	 * 添加新列
	 * ALTER TABLE TAB_A ADD USER_NAME VARCHAR(10)
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildAddRunSQL(Column column){
		column.setCreater(this);
		StringBuilder builder = new StringBuilder();
		Table table = column.getTable();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" ADD ");
		SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
		define(builder, column);
		return builder.toString();
	}

	/**
	 * 主键
	 * CONSTRAINT [PK_BS_DEV] PRIMARY KEY (ID  ASC)
	 * @param builder builder
	 * @param table table
	 * @return builder
	 */
	@Override
	public StringBuilder primary(StringBuilder builder, Table table){
		List<Column> pks = table.getPrimaryKeys();
		if(pks.size()>0){
			builder.append(",CONSTRAINT ").append("PK_").append(table.getName()).append(" PRIMARY KEY (");
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
			}
			builder.append(")");
		}
		return builder;
	}
	/**
	 * 自增长列
	 * @param builder builder
	 * @param column column
	 * @return builder
	 */
	public StringBuilder increment(StringBuilder builder, Column column){
		if(column.isAutoIncrement()){
			builder.append(" IDENTITY(").append(column.getIncrementSeed()).append(",").append(column.getIncrementStep()).append(")");
		}
		return builder;
	}


	/**
	 * 修改默认值
	 * 子类实现
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeDefaultRunSQL(Column column){
		return null;
	}

	/**
	 * 修改非空限制
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeNullableRunSQL(Column column){
		return null;
	}
	/**
	 * 修改备注
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * @param column column
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Column column){
		return null;
	}

	/**
	 * 修改数据类型
	 * 一般不直接调用,如果需要由buildAlterRunSQL内部统一调用
	 * alter table 表名 alter column 字段名 varchar(255) not null
	 * @param column column
	 * @return sql
	 */
	public List<String> buildChangeTypeRunSQL(Column column){
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
	 * 内置函数
	 * @param value SQL_BUILD_IN_VALUE
	 * @return String
	 */
	public String buildInValue(SQL_BUILD_IN_VALUE value){
		if(value == SQL_BUILD_IN_VALUE.CURRENT_TIME){
			return "getdate()";
		}
		return null;
	}
}
