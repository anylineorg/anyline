/* 
 * Copyright 2006-2015 the original author or authors.
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

package org.anyline.dao.impl.springjdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.anyline.config.db.PageNavi;
import org.anyline.config.db.Procedure;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.run.RunSQL;
import org.anyline.config.http.ConfigStore;
import org.anyline.dao.AnylineDao;
import org.anyline.entity.BasicEntity;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
 
@Repository("anylineDao")
public class AnylineDaoImpl implements AnylineDao {
	private static Logger LOG = Logger.getLogger(AnylineDaoImpl.class);

	@Autowired(required=false)
	private SQLCreater creater;
	@Autowired(required=false)
	private JdbcTemplate jdbc;
	@Autowired(required=false)
	private DataSource dataSource;			//数据源
	
	
	
	private static boolean showSQL = false;
	private static boolean showSQLParam = false;
	private static boolean showSQLWhenError = true;
	private static boolean showSQLParamWhenError = true;
	
	public AnylineDaoImpl(){
		showSQL = ConfigTable.getBoolean("SHOW_SQL",showSQL);
		showSQLParam = ConfigTable.getBoolean("SHOW_SQL_PARAM",showSQLParam);
		showSQLWhenError = ConfigTable.getBoolean("SHOW_SQL_WHEN_ERROR",showSQLWhenError);
		showSQLParamWhenError = ConfigTable.getBoolean("SHOW_SQL_PARAM_WHEN_ERROR",showSQLParamWhenError);
	}
	/**
	 * 查询
	 */
	@Override
	public DataSet query(DataSource ds, SQL sql, ConfigStore configs, String ... conditions) {
		DataSet set = null;
		RunSQL run = creater.createQueryRunSQL(sql, configs, conditions);
		PageNavi navi = run.getPageNavi();
		int total = 0;
		if(null != navi){
			total = getTotal(ds, run.getTotalQueryTxt(), run.getValues());
			navi.setTotalRow(total);
		}
		if(null == navi || total > 0){
			set = select(ds, run.getFinalQueryTxt(), run.getValues());
		}else{
			set = new DataSet();
		}
		set.setDataSource(sql.getDataSource());
		set.setAuthor(sql.getAuthor());
		set.setTable(sql.getTable());
		set.setNavi(navi);
		return set;
	}
	public DataSet query(SQL sql, ConfigStore configs, String ... conditions){
		return query(null, sql , configs, conditions);
	}
	public DataSet query(DataSource ds, SQL sql, String ... conditions){
		return query(ds, sql, null, conditions);
	}
	public DataSet query(SQL sql, String ... conditions){
		return query(null, sql, null, conditions);
	}
	/**
	 * 总记录数
	 * @return
	 */
	private int getTotal(DataSource ds, String sql, List<Object> values) {
		int total = 0;
		DataSet set = select(ds,sql,values);
		total = set.getInt("CNT");
		return total;
	}
	/**
	 * 更新记录
	 * @param row		需要更新的数据
	 * @param columns	需要更新的列
	 * @return
	 */
	@Override
	public int update(DataSource ds, String dest, Object obj, String ... columns ){
		if(null == obj){
			throw new SQLUpdateException("更新空数据");
		}
		int result = 0;
		try{
			//row.processBeforeSave();								//保存之前预处理
			RunSQL run = creater.createUpdateTxt(dest, obj, false, columns);
			/*执行SQL*/
			result = jdbc.update(run.getUpdateTxt(), run.getValues().toArray());
		//	row.processBeforeDisplay();	//显示之前预处理
		}catch(Exception e){
			LOG.error(e);
			throw new SQLUpdateException("更新异常:"+e);
		}
		return result;
	}
	@Override
	public int update(String dest, Object data, String ... columns){
		return update(null, dest, data, columns);
	}
	@Override
	public int update(DataSource ds, Object data, String ... columns){
		return update(ds, null, data, columns);
	}
	@Override
	public int update(Object data, String ... columns){
		return update(null, null, data, columns);
	}
	/**
	 * 保存(insert|upate)
	 */
	@Override
	public int save(DataSource ds, String dest, Object data, boolean checkPrimary, String ... columns){
		if(null == data){
			throw new SQLUpdateException("保存空数据");
		}
		if(data instanceof Collection){
			Collection<?> items = (Collection<?>)data;
			int cnt = 0;
			for(Object item:items){
				cnt += save(ds, dest, item, checkPrimary, columns);
			}
			return cnt;
		}
		return saveObject(ds, dest, data, checkPrimary, columns);
		
	}

	@Override
	public int save(String dest, Object data, boolean checkPrimary, String ... columns){
		return save(null, dest, data, checkPrimary, columns);
	}
	@Override
	public int save(DataSource ds, Object data, boolean checkPrimary, String ... columns){
		return save(ds, null, data, checkPrimary, columns);
	}
	@Override
	public int save(Object data, boolean checkPrimary, String ... columns){
		return save(null, null, data, checkPrimary, columns);
	}
	@Override
	public int save(DataSource ds, String dest, Object data, String ... columns){
		return save(ds, dest, data, false, columns);
	}
	@Override
	public int save(String dest, Object data, String ... columns){
		return save(null, dest, data, columns);
	}
	@Override
	public int save(DataSource ds, Object data, String ... columns){
		return save(ds, null, data, false, columns);
	}
	@Override
	public int save(Object data, String ... columns){
		return save(null, null, data, false, columns);
	}
	
	
	
	
	public int inserts(DataSource ds, String dest, Collection items, boolean checkPrimary, String ... columns){
		if(null == items){
			return 0;
		}
		
		return items.size();
	}
	private int saveObject(DataSource ds, String dest, Object data, boolean checkPrimary, String ... columns){
		if(null == data){
			return 0;
		}
		if(checkIsNew(data)){
			return insert(ds,dest, data, checkPrimary, columns);
		}else{
			return update(ds, dest, data, columns);
		}
	}
	private boolean checkIsNew(Object obj){
		if(null == obj){
			return false;
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			return row.isNew();
		}else if(obj instanceof BasicEntity){
			return ((BasicEntity)obj).isNew();
		}
		return false;
	}

	/**
	 * 添加
	 * @param row
	 * 			需要插入的数据
	 * @param checkParimary
	 * 			是否需要检查重复主键,默认不检查
	 * @param columns
	 * 			需要插入的列
	 * @return
	 */
	@Override
	public int insert(DataSource ds, String dest, Object data, boolean checkPrimary, String ... columns){
		RunSQL run = creater.createInsertTxt(dest, data, checkPrimary, columns);
		final String sql = run.getInsertTxt();
		final List<Object> values = run.getValues();
		KeyHolder key=new GeneratedKeyHolder();
		final String primaryKey = creater.getPrimaryKey(data);	
		try{
			jdbc.update(
	            new PreparedStatementCreator() {
	                public PreparedStatement createPreparedStatement(Connection con) throws SQLException
	                {
	                    PreparedStatement ps = jdbc.getDataSource().getConnection().prepareStatement(sql.toString(),new String[] {primaryKey});
	                    int idx = 0;
	                    for(Object obj:values){
	                    	ps.setObject(++idx, obj);
	                    }
	                    return ps;
	                }
	            }, key);

			if(null != key && null != key.getKey()){
				setPrimaryValue(data, key.getKey().intValue());
			}
		}catch(Exception e){
			LOG.error(e);
			throw new SQLUpdateException("插入异常:"+e);
		}
		return 1;
	}

	@Override
	public int insert(String dest, Object data, boolean checkPrimary, String ... columns){
		return insert(null, dest, data, checkPrimary, columns);
	}
	@Override
	public int insert(DataSource ds, Object data, boolean checkPrimary, String ... columns){
		return insert(ds, null, data, checkPrimary, columns);
	}
	@Override
	public int insert(Object data, boolean checkPrimary, String ... columns){
		return insert(null, null, data, checkPrimary, columns);
	}
	@Override
	public int insert(DataSource ds, String dest, Object data, String ... columns){
		return insert(ds, dest, data, false, columns);
	}
	@Override
	public int insert(String dest, Object data, String ... columns){
		return insert(null, dest, data, false, columns);
	}
	@Override
	public int insert(DataSource ds, Object data, String ... columns){
		return insert(ds, null, data, false, columns);
	}
	@Override
	public int insert(Object data, String ... columns){
		return insert(null, null, data, false, columns);
	}
	private void setPrimaryValue(Object obj, int value){
		if(null == obj){
			return;
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			row.put(row.getPrimaryKey(), value);
		}else{
			String key = BeanUtil.getPrimaryKey(obj.getClass());
			BeanUtil.setFieldValue(obj, key, value);
		}
	}
	/**
	 * 查询
	 * @param sql
	 * @param values
	 * @return
	 */
	private DataSet select(DataSource ds, String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			throw new SQLQueryException("未指定SQL");
		}
		if(ConfigTable.getBoolean("SHOW_SQL")){
			LOG.info("\n"+sql);
			LOG.info(values);
		}
		DataSet set = new DataSet();
		try{
			List<Map<String,Object>> list = null;
			if(null != values && values.size()>0){
				list = jdbc.queryForList(sql, values.toArray());
			}else{
				list = jdbc.queryForList(sql);
			}
	        for(Map<String,Object> map:list){
	        	DataRow row = new DataRow(map);
	        	set.add(row);
	        }
		}catch(Exception e){
			LOG.error(e);
			throw new SQLQueryException("查询异常:"+e+"\nSQL:"+sql+"\nPARAM:"+values);
		}
		return set;
	}
	@Override
	public int execute(DataSource ds, SQL sql, ConfigStore configs, String ... conditions){
		int result = -1;
		RunSQL run = creater.createExecuteRunSQL(sql, configs, conditions);
		String txt = run.getExecuteTxt();
		List<Object> values = run.getValues();

		if(ConfigTable.getBoolean("SHOW_SQL")){
			LOG.info(txt);
			LOG.info(values);
		}
		try{
			if(null != values && values.size() > 0){
				result = jdbc.update(txt, values.toArray());
			}else{
				result = jdbc.update(txt);
			}
		}catch(Exception e){
			LOG.error(e);
			throw new SQLUpdateException("执行异常:"+e+"\nSQL:"+txt+"\nPARAM:"+values);
		}
		return result; 
	}
	@Override
	public int execute(DataSource ds, SQL sql, String ... conditions){
		return execute(ds, sql, null, conditions);
	}
	@Override
	public int execute(SQL sql, ConfigStore configs, String ... conditions){
		return execute(null, sql, configs, conditions);
	}
	@Override
	public int execute(SQL sql, String ... conditions){
		return execute(null, sql, null, conditions);
	}
	@SuppressWarnings("unchecked")
	@Override
	public List<Object> executeProcedure(DataSource ds, Procedure procedure){
		List<Object> result = new ArrayList<Object>();
		final List<String> inputValues = procedure.getInputValues();
		final List<Integer> inputTypes = procedure.getInputTypes();
		final List<Integer> outputTypes = procedure.getOutputTypes();

		if(ConfigTable.getBoolean("SHOW_SQL")){
			LOG.info(procedure.getName());
			LOG.info(inputValues);
		}
		String sql = "{call "+procedure.getName()+"(";
		final int sizeIn = null == inputTypes? 0 : inputTypes.size();
		final int sizeOut = null == outputTypes? 0 : outputTypes.size();
		final int size = sizeIn + sizeOut;
		for(int i=0; i<size; i++){
			sql += "?";
			if(i < size-1){
				sql += ",";
			}
		}
		sql += ")}";
		try{
			result = (List<Object>)jdbc.execute(sql,new CallableStatementCallback<Object>(){     
		        public Object doInCallableStatement(final CallableStatement cs) throws SQLException, DataAccessException {
					final List<Object> result = new ArrayList<Object>();
					for(int i=1; i<=sizeIn; i++){
						Object value = inputValues.get(i-1);
						if(null == value || "NULL".equalsIgnoreCase(value.toString())){
							value = null;
						}
						cs.setObject(i, value, inputTypes.get(i-1));
					}
					for(int i=1; i<=sizeOut; i++){
						cs.registerOutParameter(i+sizeIn, outputTypes.get(i-1));     
					}
		            if(sizeOut > 0){
						//注册输出参数
						cs.execute();
						for(int i=1; i<=sizeOut; i++){
							final Object output = cs.getObject(sizeIn+i);
							result.add(output);
						}
					}else{
						boolean exeResult = cs.execute();
					}
		            return result;
		        }
		    });     
		}catch(Exception e){
			LOG.error(e);
			throw new SQLUpdateException("PROCEDURE执行异常:"+e+"\nPROCEDURE:"+procedure.getName()+"\nPARAM:"+procedure.getInputValues());
		}
		return result;
	}

	@Override
	public synchronized List<Object> executeProcedure(Procedure procedure){
		return executeProcedure(null, procedure);
	}
	/**
	 * 根据存储过程查询(MSSQL AS 后必须加 SET NOCOUNT ON)
	 * @param procedure
	 * @param inputs
	 * @return
	 */
	@Override
	public DataSet queryProcedure(DataSource ds, final Procedure procedure){
		final List<String> inputValues = procedure.getInputValues();
		final List<Integer> inputTypes = procedure.getInputTypes();
		final List<Integer> outputTypes = procedure.getOutputTypes();

		if(ConfigTable.getBoolean("SHOW_SQL")){
			LOG.info(procedure.getName());
			LOG.info(inputValues);
		}
		DataSet set = null;
		try{
			set = (DataSet)jdbc.execute(new CallableStatementCreator(){  
	            public CallableStatement createCallableStatement(Connection conn) throws SQLException {  
	            	String sql = "{call "+procedure.getName()+"(";
	        		final int sizeIn = null == inputTypes? 0 : inputTypes.size();
	        		final int sizeOut = null == outputTypes? 0 : outputTypes.size();
	        		final int size = sizeIn + sizeOut;
	        		for(int i=0; i<size; i++){
	        			sql += "?";
	        			if(i < size-1){
	        				sql += ",";
	        			}
	        		}
	        		sql += ")}";
	        		
	                CallableStatement cs = conn.prepareCall(sql);
	                for(int i=1; i<=sizeIn; i++){
						Object value = inputValues.get(i-1);
						if(null == value || "NULL".equalsIgnoreCase(value.toString())){
							value = null;
						}
						cs.setObject(i, value, inputTypes.get(i-1));
					}
					for(int i=1; i<=sizeOut; i++){
						cs.registerOutParameter(i+sizeIn, outputTypes.get(i-1));     
					}
	                return cs;  
	            }  
	        },new CallableStatementCallback(){  
	            public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {  
	                ResultSet rs = cs.executeQuery();
	                DataSet set = new DataSet();
	        		ResultSetMetaData rsmd = rs.getMetaData();
	        		int cols = rsmd.getColumnCount();
	        		for(int i=1; i<=cols; i++){
	        			set.addHead(rsmd.getColumnName(i));
	        		}
	                while(rs.next()){
	    				DataRow row = new DataRow();
	    				for(int i=1; i<=cols; i++){
	    					row.put(rsmd.getColumnName(i).toUpperCase(), rs.getObject(i));
	    				}
	    				row.processBeforeDisplay();	//显示之前预处理
	    				set.addRow(row);
	    			}
	                return set;
	            }  
	        });  
		}catch(Exception e){
			LOG.error(e);
			throw new SQLQueryException("查询异常:"+e+"\nPROCEDURE:"+ procedure.getName());
		}
		return set;
	}
	@Override
	public DataSet queryProcedure(Procedure procedure){
		return queryProcedure(null, procedure);
	}

	@Override
	public int delete(DataSource ds, String dest, Object data, String... columns) {
		RunSQL run = creater.createDeleteRunSQL(dest, data, columns);
		final String sql = run.getDeleteTxt();
		final List<Object> values = run.getValues();
		if(ConfigTable.getBoolean("SHOW_SQL")){
			LOG.info(sql);
			LOG.info(values);
		}
		try{
			jdbc.update(
	            new PreparedStatementCreator() {
	                public PreparedStatement createPreparedStatement(Connection con) throws SQLException
	                {
	                    PreparedStatement ps = jdbc.getDataSource().getConnection().prepareStatement(sql);
	                    int idx = 0;
	                    for(Object obj:values){
	                    	ps.setObject(++idx, obj);
	                    }
	                    return ps;
	                }
	            });
		}catch(Exception e){
			LOG.error(e);
			throw new SQLUpdateException("删除异常:"+e);
		}
		return 1;
	}
 
}