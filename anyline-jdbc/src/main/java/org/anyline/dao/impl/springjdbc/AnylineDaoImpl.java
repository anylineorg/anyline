/*
 * Copyright 2006-2020 www.anyline.org
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
 *
 */


package org.anyline.dao.impl.springjdbc;

import org.anyline.cache.PageLazyStore;
import org.anyline.dao.AnylineDao;
import org.anyline.dao.JDBCListener;
import org.anyline.dao.impl.BatchInsertStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.MetaData;
import org.anyline.entity.PageNavi;
import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.impl.ProcedureParam;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.sql.auto.TableSQL;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.jdbc.exception.SQLQueryException;
import org.anyline.jdbc.exception.SQLUpdateException;
import org.anyline.jdbc.util.SQLCreaterUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Repository("anyline.dao")
public class AnylineDaoImpl implements AnylineDao {
	protected static final Logger log = LoggerFactory.getLogger(AnylineDaoImpl.class);

	@Autowired(required=false)
	protected JdbcTemplate jdbc;

	@Autowired(required=false)
	protected JDBCListener listener;

	public JdbcTemplate getJdbc(){
		return jdbc;
	}


	protected BatchInsertStore batchInsertStore = new BatchInsertStore();

	protected static boolean showSQL = true;
	protected static boolean showSQLParam = true;
	protected static boolean showSQLWhenError = true;
	protected static boolean showSQLParamWhenError = true;

	protected static boolean isBatchInsertRun = false;

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
	public List<Map<String,Object>> maps(SQL sql, ConfigStore configs, String ... conditions) {
		List<Map<String,Object>> maps = null;
		try {
			RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createQueryRunSQL(sql, configs, conditions);
			if (showSQL && !run.isValid()) {
				String tmp = "[valid:false]";
				String src = "";
				if (sql instanceof TableSQL) {
					src = sql.getTable();
				} else {
					src = sql.getText();
				}
				tmp += "[SQL:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
				log.warn(tmp);
			}
			if (run.isValid()) {
				if(null != listener){
					listener.beforeQuery(this,run);
				}
				maps = maps(run.getFinalQueryTxt(), run.getValues());
				if(null != listener){
					listener.afterQuery(this,run, maps);
				}
			} else {
				maps = new ArrayList<Map<String,Object>>();
			}
		}finally {
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return maps;
	}
	public List<Map<String,Object>> maps(SQL sql, String ... conditions){
		return maps(sql, null, conditions);
	}
	/**
	 * 查询
	 */
	@Override
	public DataSet querys(SQL sql, ConfigStore configs, String ... conditions) {
		DataSet set = null;
		try {
			RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createQueryRunSQL(sql, configs, conditions);
			if (showSQL && !run.isValid()) {
				String tmp = "[valid:false]";
				String src = "";
				if (sql instanceof TableSQL) {
					src = sql.getTable();
				} else {
					src = sql.getText();
				}
				tmp += "[SQL:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
				log.warn(tmp);
			}
			PageNavi navi = run.getPageNavi();
			int total = 0;
			if (run.isValid()) {
				if (null != navi) {
					if (navi.getLastRow() == 0) {
						//第一条
						total = 1;
					} else {
						//未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = getTotal(run.getTotalQueryTxt(), run.getValues());
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
				}
				if (showSQL) {
					log.warn("[查询记录总数][行数:{}]", total);
				}
			}
			if (run.isValid() && (null == navi || total > 0)) {
				if(null != listener){

					listener.beforeQuery(this,run);
				}
				set = select(run.getFinalQueryTxt(), run.getValues());
				if(null != listener){
					listener.afterQuery(this,run,set);

				}
			} else {
				set = new DataSet();
			}
			set.setDataSource(sql.getDataSource());
			set.addQueryParam("query_config", configs)
					.addQueryParam("query_condition", conditions)
					.addQueryParam("query_order", run.getOrderStore())
					.addQueryParam("query_column", sql.getColumns());
//		set.setSchema(sql.getSchema());
//		set.setTable(sql.getTable());
			set.setNavi(navi);
			if (null != navi && navi.isLazy()) {
				PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
			}
		}finally {
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return set;
	}
	public DataSet querys(SQL sql, String ... conditions){
		return querys(sql, null, conditions);
	}

	public List<String> metadata(String table){
		List<String> list = new ArrayList<>();
		String sql = "SELECT * FROM " + table + " WHERE 1=0";
		SqlRowSet row = getJdbc().queryForRowSet(sql);
		SqlRowSetMetaData rsm = row.getMetaData();
		for (int i = 1; i < rsm.getColumnCount(); i++) {
			list.add(rsm.getColumnName(i));
		}
		return list;
	}
	public List<MetaData> metadatas(String table){
		List<MetaData> list = new ArrayList<MetaData>();
		String sql = "SELECT * FROM " + table + " WHERE 1=0";
		SqlRowSet row = getJdbc().queryForRowSet(sql);
		SqlRowSetMetaData rsm = row.getMetaData();

		for (int i = 1; i < rsm.getColumnCount(); i++) {
			MetaData item = new MetaData();
			item.setCatalogName(rsm.getCatalogName(i));
			item.setClassName(rsm.getColumnClassName(i));
			item.setCaseSensitive(rsm.isCaseSensitive(i));
			item.setCurrency(rsm.isCurrency(i));
			item.setLabel(rsm.getColumnLabel(i));
			item.setName(rsm.getColumnName(i));
			item.setPrecision(rsm.getPrecision(i));
			item.setScale(rsm.getScale(i));
			item.setDisplaySize(rsm.getColumnDisplaySize(i));
			item.setSchema(rsm.getSchemaName(i));
			item.setSigned(rsm.isSigned(i));
			item.setTable(rsm.getTableName(i));
			item.setType(rsm.getColumnType(i));
			item.setTypeName(rsm.getColumnTypeName(i));
			list.add(item);
		}
		return list;
	}
	/**
	 * 查询
	 */
	@Override
	public DataSet selects(SQL sql, ConfigStore configs, String ... conditions) {
		return querys(sql, configs, conditions);
	}
	public DataSet selects(SQL sql, String ... conditions){
		return querys(sql, null, conditions);
	}
	public int count(SQL sql, ConfigStore configs, String ... conditions){
		int count = -1;
		try{
			RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createQueryRunSQL(sql, configs, conditions);
			if(null != listener){
				listener.beforeCount(this,run);
			}
			count = getTotal(run.getTotalQueryTxt(), run.getValues());
			if(null != listener){
				listener.afterCount(this,run, count);
			}
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return count;
	}
	public int count(SQL sql, String ... conditions){
		return count(sql, null, conditions);
	}
	public boolean exists(SQL sql, ConfigStore configs, String ... conditions){
		boolean result = false;
		try {
			RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createQueryRunSQL(sql, configs, conditions);
			String txt = run.getExistsTxt();
			List<Object> values = run.getValues();

			long fr = System.currentTimeMillis();
			String random = "";
			if (showSQL) {
				random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
				log.warn("{}[txt:\n{}\n]", random, txt);
				log.warn("{}[参数:{}]", random, paramLogFormat(values));
			}
			/*执行SQL*/
			try {
				if(null != listener){
					listener.beforeExists(this,run);
				}
				Map<String, Object> map = null;
				if (null != values && values.size() > 0) {
					map = getJdbc().queryForMap(txt, values.toArray());
				} else {
					map = getJdbc().queryForMap(txt);
				}
				if (null == map) {
					result = false;
				} else {
					result = BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
				}
				if(null != listener){
					listener.afterExists(this,run, result);
				}
				if (showSQL) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, System.currentTimeMillis() - fr, result);
				}
			} catch (Exception e) {
				log.error(random + "异常:" + e);
				if (showSQLWhenError) {
					log.error(random + "[异常TXT:\n{}\n]", sql);
					log.error(random + "[异常参数:{}]", paramLogFormat(values));
				}
				throw new SQLQueryException("查询异常:" + e);
			}
		}finally {
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	public boolean exists(SQL sql, String ... conditions){
		return exists(sql, null, conditions);
	}
	/**
	 * 总记录数
	 * @param sql sql
	 * @param values values
	 * @return return
	 */
	protected int getTotal(String sql, List<Object> values) {
		int total = 0;
		DataSet set = select(sql,values);
		total = set.getInt(0,"CNT",0);
		return total;
	}
	/**
	 * 更新记录
	 * @param obj		需要更新的数据  row		需要更新的数据
	 * @param dest	dest
	 * @param columns	需要更新的列  columns	需要更新的列
	 * @return return
	 */
	@Override
	public int update(String dest, Object obj, String ... columns ){
		if(null == obj){
			throw new SQLUpdateException("更新空数据");
		}
		int result = 0;
		if(obj instanceof DataSet){
			DataSet set = (DataSet)obj;
			for(int i=0; i<set.size(); i++){
				result += update(dest, set.getRow(i), columns);
			}
			return result;
		}
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createUpdateTxt(dest, obj, false, columns);
		String sql = run.getUpdateTxt();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]",dest);
			return -1;
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn(random + "[txt:\n{}\n]",sql);
			log.warn(random + "[参数:{}]",paramLogFormat(run.getUpdateColumns(),values));
		}
		/*执行SQL*/
		try{

			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeUpdate(this,run, dest, obj, columns);
			}
			if(listenerResult) {
				result = getJdbc().update(sql, values.toArray());
				if (null != listener) {
					listener.afterUpdate(this, run, result, dest, obj, columns);
				}
				if (showSQL) {
					log.warn(random + "[执行耗时:{}ms][影响行数:{}]", System.currentTimeMillis() - fr, result);
				}

			}
		}catch(Exception e){
			e.printStackTrace();
			if(showSQLWhenError){
				log.error(random + "[异常][txt:\n{}\n]",sql);
				log.error(random + "[异常参数][param:{}]",paramLogFormat(run.getUpdateColumns(),values));
			}
			throw new SQLUpdateException("更新异常:" + e);
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	@Override
	public int update(Object data, String ... columns){
		return update(null, data, columns);
	}
	/**
	 * 保存(insert|upate)
	 */
	@Override
	public int save(String dest, Object data, boolean checkParimary, String ... columns){
		if(null == data){
			throw new SQLUpdateException("保存空数据");
		}
		if(data instanceof Collection){
			Collection<?> items = (Collection<?>)data;
			int cnt = 0;
			for(Object item:items){
				cnt += save(dest, item, checkParimary, columns);
			}
			return cnt;
		}
		return saveObject(dest, data, checkParimary, columns);

	}

	@Override
	public int save(Object data, boolean checkParimary, String ... columns){
		return save(null, data, checkParimary, columns);
	}
	@Override
	public int save(String dest, Object data, String ... columns){
		return save(dest, data, false, columns);
	}
	@Override
	public int save(Object data, String ... columns){
		return save(null, data, false, columns);
	}


	protected int saveObject(String dest, Object data, boolean checkParimary, String ... columns){
		if(null == data){
			return 0;
		}
		if(checkIsNew(data)){
			return insert(dest, data, checkParimary, columns);
		}else{
			return update(dest, data, columns);
		}
	}
	protected boolean checkIsNew(Object obj){
		if(null == obj){
			return false;
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			return row.isNew();
		}
		return false;
	}

	/**
	 * 添加
	 * @param checkParimary   是否需要检查重复主键,默认不检查
	 * @param columns  需要插入的列
	 * @param dest  dest
	 * @param data  data
	 * @return return
	 */
	@Override
	public int insert(String dest, Object data, boolean checkParimary, String ... columns){
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createInsertTxt(dest, data, checkParimary, columns);
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
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn(random + "[txt:\n{}\n]",sql);
			log.warn(random + "[参数:{}]",paramLogFormat(run.getInsertColumns(),values));
		}
		try{

			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeInsert(this,run, dest, data,checkParimary, columns);
			}
			if(listenerResult) {
				cnt = getJdbc().update(new PreparedStatementCreator() {
					@Override
					public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
						PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
						int idx = 0;
						if (null != values) {
							for (Object obj : values) {
								ps.setObject(++idx, obj);
							}
						}
						return ps;
					}
				}, keyholder);
				long id = 0;
				if (cnt == 1) {
					try {
						id =  keyholder.getKey().longValue();
						setPrimaryValue(data, id);
					} catch (Exception e) {}
				}
				if (null != listener) {
					listener.afterInsert(this, run, cnt, dest, data, checkParimary, columns);
				}
				if (showSQL) {
					log.warn(random + "[执行耗时:{}ms][影响行数:{}][主键:{}]", System.currentTimeMillis() - fr, cnt, id);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			if(showSQLWhenError){
				log.error(random + "[异常][txt:\n{}\n]",sql);
				log.error(random + "[异常参数][param:{}]",paramLogFormat(run.getInsertColumns(),values));
			}
			throw new SQLUpdateException("插入异常:" + e);
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return cnt;
	}

	@Override
	public int insert(Object data, boolean checkParimary, String ... columns){
		return insert(null, data, checkParimary, columns);
	}
	@Override
	public int insert(String dest, Object data, String ... columns){
		return insert(dest, data, false, columns);
	}
	@Override
	public int insert(Object data, String ... columns){
		return insert(null, data, false, columns);
	}

	@Override
	public int batchInsert(final String dest, final Object data, final boolean checkParimary, final String ... columns){
		if(null == data){
			return 0;
		}
		if(data instanceof DataSet){
			DataSet set = (DataSet)data;
			int size = set.size();
			for(int i=0; i<size; i++){
				batchInsert(dest, set.getRow(i), checkParimary, columns);
			}
		}

		String table = DataSourceHolder.parseDataSource(dest,data);//SQLCreaterUtil.getCreater(getJdbc()).getDataSource(data);
		List<String> cols = SQLCreaterUtil.getCreater(getJdbc()).confirmInsertColumns(dest, data, columns);
		String strCols = "";
		int size = cols.size();
		for(int i=0; i<size; i++){
			String col = cols.get(i);
			strCols +=  "," +col;
		}
		synchronized (batchInsertStore) {
			batchInsertStore.addData(table, strCols, (DataRow)data);
			if(!isBatchInsertRun){
				isBatchInsertRun = true;
				new Thread(new Runnable(){
					public void run(){
						try{
							while(true){
								DataSet list = batchInsertStore.getDatas();
								if(null != list && list.size()>0){

									boolean listenerResult = true;
									if(null != listener){
										listenerResult = listener.beforeBatchInsert(AnylineDaoImpl.this,dest, list, checkParimary, columns);
									}
									if(listenerResult) {
										int cnt = insert(dest, list, checkParimary, columns);
										if (null != listener) {
											listener.afterBatchInsert(AnylineDaoImpl.this, cnt, dest, list, checkParimary, columns);
										}

									}
								}else{
									Thread.sleep(1000*10);
								}
							}
						}catch(Exception e){
							e.printStackTrace();
						}

					}
				}).start();
			}
		}
		return 0;
	}

	@Override
	public int batchInsert(Object data, boolean checkParimary, String ... columns){
		return batchInsert(null, data, checkParimary, columns);
	}
	@Override
	public int batchInsert(String dest, Object data, String ... columns){
		return batchInsert(dest, data, false, columns);
	}
	@Override
	public int batchInsert(Object data, String ... columns){
		return batchInsert(null, data, false, columns);
	}
	protected void setPrimaryValue(Object obj, long value){
		if(null == obj){
			return;
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			row.put(row.getPrimaryKey(), value);
		}else{
//			String key = BeanUtil.getPrimaryKey(obj.getClass());
//			BeanUtil.setFieldValue(obj, key, value);
		}
	}

	/**
	 * 查询
	 * @param sql  sql
	 * @param values  values
	 * @return return
	 */
	protected List<Map<String,Object>> maps(String sql, List<Object> values){
		List<Map<String,Object>> maps = null;
		if(BasicUtil.isEmpty(sql)){
			throw new SQLQueryException("未指定SQL");
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn(random + "[txt:\n{}\n]",sql);
			log.warn(random + "[参数:{}]",paramLogFormat(values));
		}
		try{
			if(null != values && values.size()>0){
				maps = getJdbc().queryForList(sql, values.toArray());
			}else{
				maps = getJdbc().queryForList(sql);
			}
			long mid = System.currentTimeMillis();
			if(showSQL){
				log.warn(random + "[执行耗时:{}ms]",mid - fr);
			}
			if(showSQL){
				log.warn(random + "[封装耗时:{}ms][封装行数:{}]",System.currentTimeMillis() - mid,maps.size() );
			}
		}catch(Exception e){
			e.printStackTrace();
			if(showSQLWhenError){
				log.error(random + "[异常][txt:\n{}\n]",sql);
				log.error(random + "[异常][参数:{}]",paramLogFormat(values));
			}
			throw new SQLQueryException("查询异常:" + e + "\ntxt:" + sql + "\nparam:" + values);
		}
		return maps;
	}
	/**
	 * 查询
	 * @param sql  sql
	 * @param values  values
	 * @return return
	 */
	protected DataSet select(String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			throw new SQLQueryException("未指定SQL");
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn(random + "[txt:\n{}\n]",sql);
			log.warn(random + "[参数:{}]",paramLogFormat(values));
		}
		DataSet set = new DataSet();
		try{
			List<Map<String,Object>> list = null;
			if(null != values && values.size()>0){
				list = getJdbc().queryForList(sql, values.toArray());
			}else{
				list = getJdbc().queryForList(sql);
			}
			long mid = System.currentTimeMillis();
			if(showSQL){
				log.warn(random + "[执行耗时:{}ms]",mid - fr);
			}
			for(Map<String,Object> map:list){
				DataRow row = new DataRow(map);
				row.clearUpdateColumns();
				set.add(row);
			}
			set.setDatalink(DataSourceHolder.getDataSource());
			if(showSQL){
				log.warn(random + "[封装耗时:{}ms][封装行数:{}]",System.currentTimeMillis() - mid,list.size() );
			}
		}catch(Exception e){
			e.printStackTrace();
			if(showSQLWhenError){
				log.error(random + "[异常][txt:\n{}\n]",sql);
				log.error(random + "[异常][参数:{}]",paramLogFormat(values));
			}
			throw new SQLQueryException("查询异常:" + e + "\ntxt:" + sql + "\nparam:" + values);
		}
		return set;
	}
	@Override
	public int execute(SQL sql, ConfigStore configs, String ... conditions){
		int result = -1;
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createExecuteRunSQL(sql, configs, conditions);
		if(!run.isValid()){
			if(showSQL){
				log.warn("[valid:false]");
			}
			return -1;
		}
		String txt = run.getExecuteTxt();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn(random + "[txt:\n{}\n]", txt);
			log.warn(random + "[参数:{}]",paramLogFormat(values));
		}
		try{

			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeExecute(this,run);
			}
			if(listenerResult) {
				if (null != values && values.size() > 0) {
					result = getJdbc().update(txt, values.toArray());
				} else {
					result = getJdbc().update(txt);
				}

				if (null != listener) {
					listener.afterExecute(this, run, result);
				}
				if (showSQL) {
					log.warn(random + "[执行耗时:{}ms][影响行数:{}]", System.currentTimeMillis() - fr, result);
				}

			}
		}catch(Exception e){
			log.error(random+":" + e);
			if(showSQLWhenError){
				log.error(random + "[异常][txt:\n{}\n]",sql);
				log.error(random + "[异常][参数:{}]",paramLogFormat(values));
			}
			throw new SQLUpdateException(random + "执行异常:" + e + "\nTXT:" + txt + "\nPARAM:" + values);
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	@Override
	public int execute(SQL sql, String ... conditions){
		return execute(sql, null, conditions);
	}
	//	@SuppressWarnings("unchecked")
//	@Override
//	public boolean executeProcedure(Procedure procedure){
//		boolean result = false;
//		List<Object> list = new ArrayList<Object>();
//		final List<String> inputValues = procedure.getInputValues();
//		final List<Integer> inputTypes = procedure.getInputTypes();
//		final List<Integer> outputTypes = procedure.getOutputTypes();
//		long fr = System.currentTimeMillis();
//		String random = "";
//		if(showSQL){
//			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+DataSourceHolder.getDataSource()+"]";
//			log.warn(random + "[txt:\n{}\n]",procedure.getName() );
//			log.warn(random + "[参数:{}]",paramLogFormat(inputValues));
//		}
//		String sql = "{call " +procedure.getName()+"(";
//		final int sizeIn = null == inputTypes? 0 : inputTypes.size();
//		final int sizeOut = null == outputTypes? 0 : outputTypes.size();
//		final int size = sizeIn + sizeOut;
//		for(int i=0; i<size; i++){
//			sql += "?";
//			if(i < size-1){
//				sql += ",";
//			}
//		}
//		sql += ")}";
//		try{
//			list = (List<Object>)getJdbc().execute(sql,new CallableStatementCallback<Object>(){
//		        public Object doInCallableStatement(final CallableStatement cs) throws SQLException, DataAccessException {
//					final List<Object> result = new ArrayList<Object>();
//					for(int i=1; i<=sizeIn; i++){
//						Object value = inputValues.get(i-1);
//						if(null == value || "NULL".equalsIgnoreCase(value.toString())){
//							value = null;
//						}
//						cs.setObject(i, value, inputTypes.get(i-1));
//					}
//					for(int i=1; i<=sizeOut; i++){
//						cs.registerOutParameter(i+sizeIn, outputTypes.get(i-1));
//					}
//		            if(sizeOut > 0){
//						//注册输出参数
//						cs.execute();
//						for(int i=1; i<=sizeOut; i++){
//							final Object output = cs.getObject(sizeIn+i);
//							result.add(output);
//						}
//					}else{
//						cs.execute();
//					}
//		            return result;
//		        }
//		    });
//
//			if(showSQL){
//				log.warn(random + "[执行耗时:{}ms]",System.currentTimeMillis()-fr);
//				log.warn(random + "[输出参数:{}]",list);
//			}
//			procedure.setResult(list);
//			result = true;
//		}catch(Exception e){
//			result = false;
//			log.error(random+":" +e);
//			if(showSQLWhenError){
//				log.error(random + "[异常][txt:\n{}\n]",sql);
//				log.error(random + "[异常][参数:{}]",paramLogFormat(inputValues));
//			}
//			e.printStackTrace();
//			throw new SQLUpdateException("PROCEDURE执行异常:" + e + "\nPROCEDURE:" + procedure.getName() + "\nPARAM:" + procedure.getInputValues());
//		}finally{
//			//自动切换回默认数据源
//			if(DataSourceHolder.isAutoDefault()){
//				DataSourceHolder.recoverDataSource();
//			}
//		}
//		return result;
//	}
	@Override
	public boolean execute(Procedure procedure){
		boolean result = false;
		List<Object> list = new ArrayList<Object>();
		final List<ProcedureParam> inputs = procedure.getInputs();
		final List<ProcedureParam> outputs = procedure.getOutputs();
		long fr = System.currentTimeMillis();
		String random = "";
		String sql = "{";

		//带有返回值
		int returnIndex = 0;
		if(procedure.hasReturn()){
			sql += "? = ";
			returnIndex = 1;
		}
		sql += "call " +procedure.getName()+"(";
		final int sizeIn = inputs.size();
		final int sizeOut = outputs.size();
		final int size = sizeIn + sizeOut;
		for(int i=0; i<size; i++){
			sql += "?";
			if(i < size-1){
				sql += ",";
			}
		}
		sql += ")}";

		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]",random, sql );
			log.warn("{}[输入参数:{}]",random,paramLogFormat(inputs));
			log.warn("{}[输出参数:{}]",random,paramLogFormat(outputs));
		}
		try{

			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeExecute(this,procedure);
			}
			if(listenerResult) {
				list = (List<Object>) getJdbc().execute(sql, new CallableStatementCallback<Object>() {
					public Object doInCallableStatement(final CallableStatement cs) throws SQLException, DataAccessException {
						final List<Object> result = new ArrayList<Object>();

						//带有返回参数
						int returnIndex = 0;
						if (procedure.hasReturn()) {
							returnIndex = 1;
							cs.registerOutParameter(1, Types.VARCHAR);
						}
						for (int i = 1; i <= sizeIn; i++) {
							ProcedureParam param = inputs.get(i - 1);
							Object value = param.getValue();
							if (null == value || "NULL".equalsIgnoreCase(value.toString())) {
								value = null;
							}
							cs.setObject(i + returnIndex, value, param.getType());
						}
						for (int i = 1; i <= sizeOut; i++) {
							ProcedureParam param = outputs.get(i - 1);
							if (null == param.getValue()) {
								cs.registerOutParameter(i + sizeIn + returnIndex, param.getType());
							} else {
								cs.setObject(i + sizeIn + returnIndex, param.getValue(), param.getType());
							}
						}
						cs.execute();
						if (procedure.hasReturn()) {
							result.add(cs.getObject(1));
						}
						if (sizeOut > 0) {
							//注册输出参数
							for (int i = 1; i <= sizeOut; i++) {
								final Object output = cs.getObject(sizeIn + returnIndex + i);
								result.add(output);
							}
						}
						return result;
					}
				});

				procedure.setResult(list);
				result = true;
				if (null != listener) {
					listener.afterExecute(this, procedure, result);
				}
				if (showSQL) {
					log.warn("{}[执行耗时:{}ms]", random, System.currentTimeMillis() - fr);
					log.warn("{}[输出参数:{}]", random, list);
				}
			}
		}catch(Exception e){
			result = false;
			log.error(random+":" +e);
			if(showSQLWhenError){
				log.error("{}[异常][txt:\n{}\n]",random,sql);
				log.error("{}[异常][输入参数:{}]",random,paramLogFormat(inputs));
				log.error("{}[异常][输出参数:{}]",random,paramLogFormat(outputs));
			}
			e.printStackTrace();
			throw new SQLUpdateException("procedure执行异常:" + e + "\nprocedure:" + procedure.getName() + "\ninputs:" + paramLogFormat(inputs)+"\noutputs:"+paramLogFormat(outputs));
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	/**
	 * 根据存储过程查询(MSSQL AS 后必须加 SET NOCOUNT ON)
	 * @param procedure  procedure
	 * @return return
	 */
	@Override
	public DataSet query(final Procedure procedure){
		final List<ProcedureParam> inputs = procedure.getInputs();
		final List<ProcedureParam> outputs = procedure.getOutputs();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]", random, procedure.getName());
			log.warn("{}[输入参数:{}]", random, paramLogFormat(inputs));
			log.warn("{}[输出参数:{}]", random, paramLogFormat(inputs));
		}
		final String rdm = random;
		DataSet set = null;
		try{
			if(null != listener){
				listener.beforeQuery(this,procedure);
			}
			set = (DataSet)getJdbc().execute(new CallableStatementCreator(){
				public CallableStatement createCallableStatement(Connection conn) throws SQLException {
					String sql = "{call " +procedure.getName()+"(";
					final int sizeIn = inputs.size();
					final int sizeOut = outputs.size();
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
						ProcedureParam param = inputs.get(i-1);
						Object value = param.getValue();
						if(null == value || "NULL".equalsIgnoreCase(value.toString())){
							value = null;
						}
						cs.setObject(i, value, param.getType());
					}
					for(int i=1; i<=sizeOut; i++){
						ProcedureParam param = outputs.get(i-1);
						if(null == param.getValue()){
							cs.registerOutParameter(i+sizeIn, param.getType());
						}else{
							cs.setObject(i, param.getValue(), param.getType());
						}

					}
					return cs;
				}
			}, new CallableStatementCallback<Object>(){
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
							row.put(rsmd.getColumnName(i), rs.getObject(i));
						}
						set.addRow(row);
					}
					set.setDatalink(DataSourceHolder.getDataSource());
					if(showSQL){
						log.warn("{}[封装耗时:{}ms][封装行数:{}]", rdm, System.currentTimeMillis() - mid,set.size());
					}
					return set;
				}
			});
			if(showSQL){
				log.warn("{}[执行耗时:{}ms]", random,System.currentTimeMillis() - fr);
			}
			if(null != listener){
				listener.afterQuery(this,procedure, set);
			}
		}catch(Exception e){
			e.printStackTrace();
			if(showSQLWhenError){
				log.error("{}[异常][txt:\n{}\n]",random,procedure.getName());
				log.error("{}[输入参数:{}]",random,paramLogFormat(inputs));
				log.error("{}[输出参数:{}]",random,paramLogFormat(inputs));
			}
			throw new SQLQueryException("查询异常:" + e + "\nPROCEDURE:" + procedure.getName());
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return set;
	}

	public int deletes(String table, String key, Collection<Object> values){
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createDeleteRunSQL(table, key, values);
		int result = exeDelete(run);
		return result;
	}
	public int deletes(String table, String key, String ... values){
		List<String> list = new ArrayList<>();
		if(null != values){
			for(String value:values){
				list.add(value);
			}
		}
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createDeleteRunSQL(table, key, list);
		int result = exeDelete(run);
		return result;
	}
	@Override
	public int delete(String dest, DataSet set, String... columns) {
		int size = 0;
		for(DataRow row:set){
			size += delete(dest, row, columns);
		}
		return size;
	}
	@Override
	public int delete(String dest, DataRow row, String... columns) {
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createDeleteRunSQL(dest, row, columns);
		int result = exeDelete(run);
		return result;
	}

	@Override
	public int delete(String table, ConfigStore configs, String... conditions) {
		RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createDeleteRunSQL(table, configs, conditions);
		int result = exeDelete(run);
		return result;
	}

	protected int exeDelete(RunSQL run){
		int result = 0;
		final String sql = run.getDeleteTxt();
		final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]",random,sql);
			log.warn("{}[参数:{}]",random,paramLogFormat(values));
		}
		try{
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeDelete(this,run);
			}
			if(listenerResult) {
				result = getJdbc().update(sql, values.toArray());
//			result = getJdbc().update(
//	            new PreparedStatementCreator() {
//	                public PreparedStatement createPreparedStatement(Connection con) throws SQLException
//	                {
//	                    PreparedStatement ps = getJdbc().getDataSource().getConnection().prepareStatement(sql);
//	                    int idx = 0;
//	                    if(null != values){
//		                    for(Object obj:values){
//		                    	ps.setObject(++idx, obj);
//		                    }
//	                    }
//	                    return ps;
//	                }
//	            });
				if (showSQL) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, System.currentTimeMillis() - fr, result);
				}
				result = 1;
				if(null != listener){
					listener.afterDelete(this,run, result);
				}
			}
		}catch(Exception e){
			log.error("删除异常:" +e);
			if(showSQLWhenError){
				log.error("{}[异常][txt:\n{}\n]",random,sql);
				log.error("{}[异常][参数:{}]",random, paramLogFormat(values));
			}
			result = 0;
			throw new SQLUpdateException("删除异常:" + e);
		}finally{
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	/**
	 * 参数日志格式化
	 * @param params params
	 * @return return
	 */
	protected String paramLogFormat(List<?> params){
		String result = "\n";
		if(null != params){
			int idx = 0;
			for(Object param:params){
				result += "param" + idx++ + "=";
				result += param;
				if(null != param){
					result += "(" + param.getClass().getSimpleName() + ")";
				}
				result += "\n";
			}
		}
		return result;
	}
	protected String paramLogFormat(List<?> keys, List<?> values) {
		String result = "\n";
		if (null != keys && null != values) {
			if(keys.size() == values.size()) {
				int size = keys.size();
				for (int i = 0; i < size; i++) {
					Object key = keys.get(i);
					Object value = values.get(i);
					result += keys.get(i) + "=";
					result += value;
					if (null != value) {
						result += "(" + value.getClass().getSimpleName() + ")";
					}
					result += "\n";
				}
			}else{
				result = paramLogFormat(values);
			}
		}
		return result;

	}
}
