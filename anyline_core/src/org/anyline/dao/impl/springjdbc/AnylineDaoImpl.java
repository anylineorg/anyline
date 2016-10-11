/* 
 * Copyright 2006-2015 www.anyline.org
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
 *
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
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

import org.anyline.cache.PageLazyStore;
import org.anyline.config.db.PageNavi;
import org.anyline.config.db.Procedure;
import org.anyline.config.db.SQL;
import org.anyline.config.db.SQLCreater;
import org.anyline.config.db.run.RunSQL;
import org.anyline.config.http.ConfigStore;
import org.anyline.dao.AnylineDao;
import org.anyline.dao.impl.BatchInsertStore;
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
	private static Logger log = Logger.getLogger(AnylineDaoImpl.class);
	
	@Autowired(required=false)
	private SQLCreater creater;
	@Autowired(required=false)
	private JdbcTemplate jdbc;
	@Autowired(required=false)
	private DataSource dataSource;			//数据源
	
	
	private BatchInsertStore batchInsertStore = new BatchInsertStore();
	
	private static boolean showSQL = false;
	private static boolean showSQLParam = false;
	private static boolean showSQLWhenError = true;
	private static boolean showSQLParamWhenError = true;
	
	private static boolean isBatchInsertRun = false;
	
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
			if(navi.getLastRow() == 0){
				//第一条
				total = 1;
			}else{
				//未计数(总数 )
				if(navi.getTotalRow() ==0){
					total = getTotal(ds, run.getTotalQueryTxt(), run.getValues());
					navi.setTotalRow(total);	
				}else{
					total = navi.getTotalRow();
				}
			}
		}
		if(null == navi || total > 0){
			set = select(ds, run.getFinalQueryTxt(), run.getValues());
		}else{
			set = new DataSet();
		}
		set.setDataSource(sql.getDataSource());
		set.setSchema(sql.getSchema());
		set.setTable(sql.getTable());
		set.setNavi(navi);
		if(null != navi && navi.isLazy()){
			PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
		} 
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

	public int count(DataSource ds, SQL sql, ConfigStore configs, String ... conditions){
		int count = -1;
		RunSQL run = creater.createQueryRunSQL(sql, configs, conditions);
		count = getTotal(ds, run.getTotalQueryTxt(), run.getValues());
		return count;
	}
	public int count(SQL sql, ConfigStore configs, String ... conditions){
		return count(null, sql, configs, conditions);
	}
	public int count(DataSource ds, SQL sql, String ... conditions){
		return count(ds, sql, null, conditions);
	}
	public int count(SQL sql, String ... conditions){
		return count(null, sql, null, conditions);
	}
	public boolean exists(DataSource ds, SQL sql, ConfigStore configs, String ... conditions){
		boolean result = false;
		RunSQL run = creater.createQueryRunSQL(sql, configs, conditions);
		String txt = run.getExistsTxt();
		List<Object> values = run.getValues();
		
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n" + txt + "]");
			log.warn(random + "[参数:"+ paramLogFormat(values) + "]");
		}
		/*执行SQL*/
		try{
			Map<String,Object> map = null;
			if(null != values && values.size()>0){
				map = jdbc.queryForMap(txt, values.toArray());
			}else{
				map = jdbc.queryForMap(txt);
			}
			if(null == map){
				result = false;
			}else{
				result =  BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
			}
			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis() - fr)+"ms][影响行数:"+result + "]");
			}
		}catch(Exception e){
			log.error(random + "异常:"+e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n" + sql + "]");
				log.error(random + "[异常参数:" + paramLogFormat(values) + "]");
			}
			throw new SQLQueryException("查询异常:"+e);
		}
		return result;
	}
	public boolean exists(SQL sql, ConfigStore configs, String ... conditions){
		return exists(null, sql, configs, conditions);
	}
	public boolean exists(DataSource ds, SQL sql, String ... conditions){
		return exists(ds, sql, null, conditions);
	}
	public boolean exists(SQL sql, String ... conditions){
		return exists(null, sql, null, conditions);
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
		//row.processBeforeSave();								//保存之前预处理
		RunSQL run = creater.createUpdateTxt(dest, obj, false, columns);
		String sql = run.getUpdateTxt();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n"+sql + "]");
			log.warn(random + "[参数:"+ paramLogFormat(values) + "]");
		}
		/*执行SQL*/
		try{
			result = jdbc.update(sql, values.toArray());
			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis() - fr)+"ms][影响行数:"+result + "]");
			}
		//	row.processBeforeDisplay();	//显示之前预处理
		}catch(Exception e){
			log.error(e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+sql);
				log.error(random + "[异常参数:"+paramLogFormat(values));
			}
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
		if(null == run){
			return 0;
		}
		int cnt = 0;
		final String sql = run.getInsertTxt();
		final List<Object> values = run.getValues();
		KeyHolder keyholder = new GeneratedKeyHolder();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n"+sql + "]");
			log.warn(random + "[参数:"+ paramLogFormat(values) + "]");
		}
		try{
			cnt= jdbc.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
					PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
					 int idx = 0;
					 if(null != values){
	                    for(Object obj:values){
	                    	ps.setObject(++idx, obj);
	                    }
					 }
	                return ps;
				}
			}, keyholder);
			if (cnt == 1) {
				try{
					int id = (int)keyholder.getKey().longValue();
					setPrimaryValue(data, id);
				}catch(Exception e){
				}
			}

			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis() - fr)+"ms][影响行数:"+cnt + "]");
			}
		}catch(Exception e){
			log.error(e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+sql + "]");
				log.error(random + "[异常参数:"+paramLogFormat(values) + "]");
			}
			throw new SQLUpdateException("插入异常:"+e);
		}
		return cnt;
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
	

	@Override
	public int batchInsert(final DataSource ds, final String dest, final Object data, final boolean checkPrimary, final String ... columns){
		if(null == data){
			return 0;
		}
		if(data instanceof DataSet){
			DataSet set = (DataSet)data;
			int size = set.size();
			for(int i=0; i<size; i++){
				batchInsert(ds, dest, set.getRow(i), checkPrimary, columns);
			}
		}
		String table = creater.getDataSource(data);
		List<String> cols = creater.confirmInsertColumns(dest, data, columns);
		String strCols = "";
		int size = cols.size();
		for(int i=0; i<size; i++){
			String col = cols.get(i);
			strCols +=  ","+col;
		}
		synchronized (batchInsertStore) {
			batchInsertStore.addData(table, strCols,(DataRow)data);
			if(!isBatchInsertRun){
				isBatchInsertRun = true;
				new Thread(new Runnable(){
					public void run(){
						try{
							while(true){
								DataSet list = batchInsertStore.getDatas();
								if(null != list && list.size()>0){
									insert(ds, dest, list, checkPrimary, columns);
								}else{
									Thread.sleep(1000*10);
								}
							}
						}catch(Exception e){
							log.error(e);
						}
						
					}
				}).start();
			}
		}
		return 0;
	}

	@Override
	public int batchInsert(String dest, Object data, boolean checkPrimary, String ... columns){
		return batchInsert(null, dest, data, checkPrimary, columns);
	}
	@Override
	public int batchInsert(DataSource ds, Object data, boolean checkPrimary, String ... columns){
		return batchInsert(ds, null, data, checkPrimary, columns);
	}
	@Override
	public int batchInsert(Object data, boolean checkPrimary, String ... columns){
		return batchInsert(null, null, data, checkPrimary, columns);
	}
	@Override
	public int batchInsert(DataSource ds, String dest, Object data, String ... columns){
		return batchInsert(ds, dest, data, false, columns);
	}
	@Override
	public int batchInsert(String dest, Object data, String ... columns){
		return batchInsert(null, dest, data, false, columns);
	}
	@Override
	public int batchInsert(DataSource ds, Object data, String ... columns){
		return batchInsert(ds, null, data, false, columns);
	}
	@Override
	public int batchInsert(Object data, String ... columns){
		return batchInsert(null, null, data, false, columns);
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
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n"+sql + "]");
			log.warn(random + "[参数:"+ paramLogFormat(values) + "]");
		}
		DataSet set = new DataSet();
		try{
			List<Map<String,Object>> list = null;
			if(null != values && values.size()>0){
				list = jdbc.queryForList(sql, values.toArray());
			}else{
				list = jdbc.queryForList(sql);
			}
			long mid = System.currentTimeMillis();
			if(showSQL){
				log.warn(random + "[执行耗时:"+(mid - fr)+"ms" + "]");
			}
	        for(Map<String,Object> map:list){
	        	DataRow row = new DataRow(map);
	        	set.add(row);
	        }
			if(showSQL){
				log.warn(random + "[封装耗时:"+(System.currentTimeMillis() - mid)+"ms][封装行数:"+list.size() + "]");
			}
		}catch(Exception e){
			log.error(e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+sql + "]");
				log.error(random + "[异常参数:"+paramLogFormat(values) + "]");
			}
			throw new SQLQueryException("查询异常:"+e+"\nTXT:"+sql+"\nPARAM:"+values);
		}
		return set;
	}
	@Override
	public int execute(DataSource ds, SQL sql, ConfigStore configs, String ... conditions){
		int result = -1;
		RunSQL run = creater.createExecuteRunSQL(sql, configs, conditions);
		String txt = run.getExecuteTxt();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n"+txt + "]");
			log.warn(random + "[参数:"+ paramLogFormat(values) + "]");
		}
		try{
			if(null != values && values.size() > 0){
				result = jdbc.update(txt, values.toArray());
			}else{
				result = jdbc.update(txt);
			}

			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis()-fr)+"ms][影响行数:"+result + "]");
			}
		}catch(Exception e){
			log.error(random+":"+e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+sql + "]");
				log.error(random + "[异常参数:"+paramLogFormat(values) + "]");
			}
			throw new SQLUpdateException(random + "执行异常:"+e+"\nTXT:"+txt+"\nPARAM:"+values);
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
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]"; 
			log.warn(random + "[TXT:\n"+procedure.getName() + "]");
			log.warn(random + "[参数:"+ paramLogFormat(inputValues) + "]");
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
						cs.execute();
					}
		            return result;
		        }
		    });    

			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis()-fr)+"ms]");
				log.warn(random + "[输出参数:" + result + "]");
			}
			procedure.setResult(result);
		}catch(Exception e){
			log.error(random+":"+e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+sql + "]");
				log.error(random + "[异常参数:"+paramLogFormat(inputValues) + "]");
			}
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
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n"+procedure.getName() + "]");
			log.warn(random + "[参数:"+ paramLogFormat(inputValues) + "]");
		}
		final String rdm = random;
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
	        },new CallableStatementCallback<Object>(){  
	            public Object doInCallableStatement(CallableStatement cs) throws SQLException, DataAccessException {  
	                ResultSet rs = cs.executeQuery();
	                DataSet set = new DataSet();
	        		ResultSetMetaData rsmd = rs.getMetaData();
	        		int cols = rsmd.getColumnCount();
	        		for(int i=1; i<=cols; i++){
	        			set.addHead(rsmd.getColumnName(i));
	        		}
	        		long mid = System.currentTimeMillis();
	                while(rs.next()){
	    				DataRow row = new DataRow();
	    				for(int i=1; i<=cols; i++){
	    					row.put(rsmd.getColumnName(i).toUpperCase(), rs.getObject(i));
	    				}
	    				row.processBeforeDisplay();	//显示之前预处理
	    				set.addRow(row);
	    			}
	                if(showSQL){
	    				log.warn(rdm + "[封装耗时:"+(System.currentTimeMillis() - mid)+"ms][封装行数:"+set.size() + "]");
	    			}
	                return set;
	            }  
	        });  
			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis() - fr)+"ms]");
			}
		}catch(Exception e){
			log.error(e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+procedure.getName() + "]");
				log.error(random + "[异常参数:"+paramLogFormat(inputValues) + "]");
			}
			throw new SQLQueryException("查询异常:"+e+"\nPROCEDURE:"+ procedure.getName());
		}
		return set;
	}
	@Override
	public DataSet queryProcedure(Procedure procedure){
		return queryProcedure(null, procedure);
	}

	public int delete(DataSource ds, String table, String key, Collection<Object> values){
		RunSQL run = creater.createDeleteRunSQL(table, key, values);
		int result = exeDelete(run);
		return result;
	}
	public int delete(String table, String key, Collection<Object> values){
		return delete(null, table, key, values);
	}
	public int delete(DataSource ds, String table, String key, String ... values){
		List<String> list = new ArrayList<String>();
		if(null != values){
			for(String value:values){
				list.add(value);
			}
		}
		RunSQL run = creater.createDeleteRunSQL(table, key, list);
		int result = exeDelete(run);
		return result;
	}
	public int delete(String table, String key, String ... values){
		return delete(null, table, key, values);
	}
	@Override
	public int delete(DataSource ds, String dest, Object data, String... columns) {
		RunSQL run = creater.createDeleteRunSQL(dest, data, columns);
		int result = exeDelete(run);
		return result;
	}
	private int exeDelete(RunSQL run){
		int result = 0;
		final String sql = run.getDeleteTxt();
		final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8)+"]";
			log.warn(random + "[TXT:\n" + sql + "]");
			log.warn(random + "[参数:"+ paramLogFormat(values) + "]");
		}
		try{
			result = jdbc.update(
	            new PreparedStatementCreator() {
	                public PreparedStatement createPreparedStatement(Connection con) throws SQLException
	                {
	                    PreparedStatement ps = jdbc.getDataSource().getConnection().prepareStatement(sql);
	                    int idx = 0;
	                    if(null != values){
		                    for(Object obj:values){
		                    	ps.setObject(++idx, obj);
		                    }
	                    }
	                    return ps;
	                }
	            });
			if(showSQL){
				log.warn(random + "[执行耗时:"+(System.currentTimeMillis()-fr)+"ms][影响行数:"+result + "]");
			}
		}catch(Exception e){
			log.error("删除异常:"+e);
			if(showSQLWhenError){
				log.error(random + "[异常TXT:\n"+sql + "]");
				log.error(random + "[异常参数:"+paramLogFormat(values) + "]");
			}
			throw new SQLUpdateException("删除异常:"+e);
		}
		return result;
	}
	/**
	 * 参数日志格式化
	 * @param params
	 * @return
	 */
	private String paramLogFormat(List<?> params){
		String result = "";
		if(null != params){
			int idx = 0;
			for(Object param:params){
				result += " param" + idx++ + "=";
				result += param;
				if(null != param){
					result += "("+param.getClass().getSimpleName() + ")";
				}
			}
		}
		return result;
	}
}