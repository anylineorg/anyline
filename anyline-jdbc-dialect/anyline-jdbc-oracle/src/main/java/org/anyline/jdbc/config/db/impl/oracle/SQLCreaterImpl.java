 
package org.anyline.jdbc.config.db.impl.oracle;

import org.anyline.dao.AnylineDao;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.config.db.impl.BasicSQLCreaterImpl;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.util.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository("anyline.jdbc.creater.oracle") 
public class SQLCreaterImpl extends BasicSQLCreaterImpl implements SQLCreater, InitializingBean {
	 
	@Autowired(required = false) 
	@Qualifier("anyline.dao") 
	protected AnylineDao dao; 

	public DB_TYPE type(){
		return DB_TYPE.ORACLE; 
	}

	@Value("${anyline.jdbc.delimiter.oracle:}")
	private String delimiter;

	@Override
	public void afterPropertiesSet() throws Exception {
		setDelimiter(delimiter);
	}

	public SQLCreaterImpl(){ 
		delimiterFr = "";
		delimiterTo = "";
	}
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
		if(null == navi){ 
			builder.append(sql).append("\n").append(order); 
		}else{ 
			//分页 
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

	@Override
	public void createInsertsTxt(StringBuilder builder, String dest, DataSet set, List<String> keys){
		builder.append("INSERT ALL \n");
		String head = "INTO " + dest + " (";
		int keySize = keys.size();
		for(int i=0; i<keySize; i++){
			String key = keys.get(i);
			head += key;
			if(i<keySize-1){
				head += ", ";
			}
		}
		head += ") ";

		int dataSize = set.size();
		for(int i=0; i<dataSize; i++){
			DataRow row = set.getRow(i);
			if(null == row){
				continue;
			}
			if(row.hasPrimaryKeys() && null != primaryCreater && BasicUtil.isEmpty(row.getPrimaryValue())){
				String pk = row.getPrimaryKey();
				if(null == pk){
					pk = ConfigTable.getString("DEFAULT_PRIMARY_KEY", "ID");
				}
				row.put(pk, primaryCreater.createPrimary(this.type(),dest.replace(getDelimiterFr(), "").replace(getDelimiterTo(), ""), pk, null));
			}
			builder.append(head).append("VALUES ");
			insertValue(builder, row, keys);
			builder.append(" \n");
		}
		builder.append("SELECT 1 FROM DUAL");
	}

	@Override
	public void value(StringBuilder builder, Object obj, String key){
		Object value = null;
		if(obj instanceof DataRow){
			value = ((DataRow)obj).get(key);
		}
		if(AdapterProxy.hasAdapter()){
			Field field = AdapterProxy.field(obj.getClass(), key);
			value = BeanUtil.getFieldValue(obj, field);
		}else{
			value = BeanUtil.getFieldValue(obj, key);
		}
		if(null == value || "NULL".equals(value)){
			builder.append("null");
		}else if(value instanceof String){
			String str = value.toString();
			if(str.startsWith("${") && str.endsWith("}") && !BeanUtil.isJson(value)){
				str = str.substring(2, str.length()-1);
			}else{
				str = "'" + str.replace("'", "''") + "'";
			}
			builder.append(str);
		}else if(value instanceof Timestamp
				|| value instanceof java.util.Date
				|| value instanceof java.sql.Date
				|| value instanceof LocalDate
				|| value instanceof LocalTime
				|| value instanceof LocalDateTime
		){
			Date date = DateUtil.parse(value);
			builder.append("TO_DATE('").append(DateUtil.format(date,DateUtil.FORMAT_DATE_TIME)).append("','yyyy-mm-dd hh24:mi:ss')");
		}else if(value instanceof Date){
			builder.append("TO_DATE('").append(DateUtil.format((Date)value,DateUtil.FORMAT_DATE_TIME)).append("','yyyy-mm-dd hh24:mi:ss')");
		}else if(value instanceof Number || value instanceof Boolean){
			builder.append(value.toString());
		}else{
			builder.append(value.toString());
		}
	}

	/**
	 * 修改表名
	 * ALTER TABLE A RENAME TO B;
	 * @param table table
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Table table) {
		StringBuilder builder = new StringBuilder();
		builder.append("ALTER TABLE ");
		name(builder, table);
		builder.append(" RENAME TO ");
		name(builder, table.getUpdate());
		return builder.toString();
	}



	/**
	 * 修改列名
	 * 子类实现
	 * ALTER TABLE 表名 RENAME COLUMN RENAME 老列名 TO 新列名
	 * @param column column
	 * @return String
	 */
	@Override
	public String buildRenameRunSQL(Column column) {
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
	 * 修改默认值
	 * ALTER TABLE MY_TEST_TABLE MODIFY B DEFAULT 2
	 * @param column column
	 * @return String
	 */
	public String buildChangeDefaultRunSQL(Column column){
		Object def = column.getDefaultValue();
		if(null != def){
			StringBuilder builder = new StringBuilder();
			builder.append("ALTER TABLE ");
			name(builder, column.getTable()).append(" MODIFY ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			builder.append(" DEFAULT '").append(def).append("'");
			return builder.toString();
		}
		return null;
	}

	/**
	 * 修改非空限制
	 * ALTER TABLE T  MODIFY C NOT NULL ;
	 * @param column column
	 * @return String
	 */
	public String buildChangeNullableRunSQL(Column column){
		Boolean nullable = column.isNullable();
		Boolean uNullable = column.getUpdate().isNullable();
		if(null != nullable && null != uNullable){
			if(nullable == uNullable){
				return null;
			}

			StringBuilder builder = new StringBuilder();
			builder.append("ALTER TABLE ");
			name(builder, column.getTable()).append(" MODIFY ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo());
			if(!uNullable){
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
	 * @param column column
	 * @return String
	 */
	public String buildChangeCommentRunSQL(Column column){
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
	 * 修改数据类型
	 * 1.ADD NEW COLUMN
	 * 2.FORMAT VALUE
	 * 3.MOVE VALUE
	 * alter table tb modify (name nvarchar2(20))
	 * @param column column
	 * @return sql
	 */
	public List<String> buildChangeTypeRunSQL(Column column){
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

		return sqls;
	}
	@Override
	public String alterColumnKeyword(){
		return "ALTER";
	}
	/**
	 * 添加列
	 * ALTER TABLE  HR_USER ADD  UPT_TIME datetime CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP comment '修改时间' AFTER ID;
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
		//Column update = column.getUpdate();
		//if(null == update){
			//添加列
			builder.append(" ADD ");
			SQLUtil.delimiter(builder, column.getName(), getDelimiterFr(), getDelimiterTo()).append(" ");
			define(builder, column);
		//}
		return builder.toString();
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

}
