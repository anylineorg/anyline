/*
 * Copyright 2006-2022 www.anyline.org
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
import org.anyline.listener.DMListener;
import org.anyline.dao.impl.BatchInsertStore;
import org.anyline.entity.*;
import org.anyline.jdbc.config.ConfigParser;
import org.anyline.jdbc.config.ConfigStore;
import org.anyline.jdbc.config.db.Procedure;
import org.anyline.jdbc.config.db.SQL;
import org.anyline.jdbc.config.db.impl.ProcedureParam;
import org.anyline.jdbc.config.db.run.RunSQL;
import org.anyline.jdbc.config.db.sql.auto.TableSQL;
import org.anyline.jdbc.config.db.sql.auto.impl.TableSQLImpl;
import org.anyline.jdbc.ds.DataSourceHolder;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Index;
import org.anyline.jdbc.entity.Table;
import org.anyline.jdbc.exception.SQLQueryException;
import org.anyline.jdbc.exception.SQLUpdateException;
import org.anyline.listener.DDListener;
import org.anyline.jdbc.util.SQLCreaterUtil;
import org.anyline.util.AdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

@Repository("anyline.dao")
public class AnylineDaoImpl<E> implements AnylineDao<E> {
	protected static final Logger log = LoggerFactory.getLogger(AnylineDaoImpl.class);

	@Autowired(required=false)
	protected JdbcTemplate jdbc;

	@Autowired(required=false)
	protected DMListener listener;

	public JdbcTemplate getJdbc(){
		return jdbc;
	}


	protected BatchInsertStore batchInsertStore = new BatchInsertStore();

	protected static boolean showSQL 				= true	;	//
	protected static boolean showSQLParam 			= true	;
	protected static boolean showSQLWhenError 		= true	;
	protected static boolean showSQLParamWhenError 	= true	;

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
					if(null != listener){
						listener.beforeTotal(this,run);
					}
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
					if(null != listener){
						listener.afterTotal(this, run, total);
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

	@Override
	public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String... conditions) {
		EntitySet<T> list = null;
		try {
			SQL sql = new TableSQLImpl();
			if(AdapterProxy.hasAdapter()){
				sql.setDataSource(AdapterProxy.table(clazz));
			}
			RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createQueryRunSQL(sql, configs, conditions);
			if (showSQL && !run.isValid()) {
				String tmp = "[valid:false]";
				tmp += "[SQL:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
				log.warn(tmp);
			}
			PageNavi navi = run.getPageNavi();
			int total = 0;
			if (run.isValid()) {
				if (null != navi) {
					if(null != listener){
						listener.beforeTotal(this,run);
					}
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
					if(null != listener){
						listener.afterTotal(this, run, total);
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
				list = select(clazz, run.getFinalQueryTxt(), run.getValues());
				if(null != listener){
					listener.afterQuery(this, run, list);

				}
			} else {
				list = new EntitySet<>();
			}
			list.setNavi(navi);
			if (null != navi && navi.isLazy()) {
				PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
			}
		}finally {
			//自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return list;
	}

	public DataSet querys(SQL sql, String ... conditions){
		return querys(sql, null, conditions);
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
	 * @return int
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
	 * @return int
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
		}else{
			if(AdapterProxy.hasAdapter()){
				Map<String,Object> values = AdapterProxy.primaryValues(obj);
				for(Map.Entry entry:values.entrySet()){
					if(BasicUtil.isNotEmpty(entry.getValue())){
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 添加
	 * @param checkParimary   是否需要检查重复主键,默认不检查
	 * @param columns  需要插入的列
	 * @param dest  dest
	 * @param data  data
	 * @return int
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
				Long id = null;
				if(null != values && values.size()>0){
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
					if (cnt == 1) {
						try {
							id =  keyholder.getKey().longValue();
							setPrimaryValue(data, id);
						} catch (Exception e) {}
					}
				}else{
					cnt = getJdbc().update(sql);
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
		int sleep = ConfigTable.getInt("BATCH_INSERT_SLEEP_MILLIS",1000);
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
									Thread.sleep(sleep);
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
			if(AdapterProxy.hasAdapter()){
				String key = AdapterProxy.primaryKey(obj.getClass());
				Field field = AdapterProxy.field(obj.getClass(), key);
				BeanUtil.setFieldValue(obj, field, value);
			}
		}
	}

	/**
	 * 查询
	 * @param sql  sql
	 * @param values  values
	 * @return List
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
	 * @return DataSet
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

	protected <T> EntitySet<T> select(Class<T> clazz, String sql, List<Object> values){
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
		EntitySet<T> set = new EntitySet<>();
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
				if(AdapterProxy.hasAdapter()){
					T row = AdapterProxy.entity(clazz, map);
					set.add(row);
				}else{
					T row = BeanUtil.map2object(map, clazz);
					set.add(row);
				}
			}
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
		String sql = " {";

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
	 * @param navi  navi
	 * @return DataSet
	 */
	@Override
	public DataSet querys(Procedure procedure, PageNavi navi){
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
					int index = 0;
					int first = -1;
					int last = -1;
					if(null != navi){
						first = navi.getFirstRow();
						last = navi.getLastRow();
					}
					while(rs.next()){
						if(
								first ==-1
								|| (index >= first && index <= last)
						){
							DataRow row = new DataRow();
							for(int i=1; i<=cols; i++){
								row.put(rsmd.getColumnName(i), rs.getObject(i));
							}
							set.addRow(row);
						}
						index ++;
						if(first != -1){
							if(index > last){
								break;
							}
							if(first ==0 && last==0){ //只取一行
								break;
							}
						}
					}
					if(null != navi){
						navi.setTotalRow(index);
						set.setNavi(navi);
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
	public int delete(String dest,Object obj, String... columns) {
		int size = 0;
		if(null != obj){
			if(obj instanceof Collection){
				Collection list = (Collection) obj;
				for(Object item:list){
					size += delete(dest, item, columns);
				}
			}else{
				RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createDeleteRunSQL(dest, obj, columns);
				size = exeDelete(run);

			}
		}
		return size;
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
				if(null == values) {
					result = getJdbc().update(sql);
				}else{
					result = getJdbc().update(sql, values.toArray());
				}
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
	 * tables
	 * @param catalog 对于MySQL，则对应相应的数据库，对于Oracle来说，则是对应相应的数据库实例，可以不填，也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名，而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意，其登陆名必须是大写，不然的话是无法获取到相应的数据，而MySQL则不做强制要求。
	 * @param name 一般情况下如果要获取所有的表的话，可以直接设置为null，如果设置为特定的表名称，则返回该表的具体信息。
	 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
	 * @return List
	 */
	public List<Table> tables(String catalog, String schema, String name, String types){
		List<Table> tables = new ArrayList<>();
		try{
			Connection con = DataSourceUtils.getConnection(getJdbc().getDataSource());
			if(null == catalog){
				catalog = con.getCatalog();
			}
			if(null == schema){
				schema = con.getSchema();
			}
			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}
			ResultSet rs = con.getMetaData().getTables(catalog, schema, name, tps );
			while(rs.next()) {
				String tableName = rs.getString("TABLE_NAME");
				if(BasicUtil.isEmpty(tableName)){
					continue;
				}
				Table table = new Table();
				table.setCatalog(BasicUtil.evl(rs.getString("TABLE_CAT"), catalog));
				table.setSchema(BasicUtil.evl(rs.getString("TABLE_SCHEM"), schema));
				table.setName(tableName);
				table.setType(rs.getString("TABLE_TYPE"));
				table.setComment(rs.getString("REMARKS"));
				table.setTypeCat(rs.getString("TYPE_CAT"));
				table.setTypeName(rs.getString("TYPE_NAME"));
				table.setSelfReferencingColumn(rs.getString("SELF_REFERENCING_COL_NAME"));
				table.setRefGeneration(rs.getString("REF_GENERATION"));
				tables.add(table);
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return tables;
	}
	public List<Table> tables(String schema, String name, String types){
		return tables(null, schema, name, types);
	}
	public List<Table> tables(String name, String types){
		return tables(null, null, name, types);
	}
	public List<Table> tables(String types){
		return tables(null, null, null, types);
	}
	public List<Table> tables(){
		return tables(null, null, null, "TABLE");
	}
	public LinkedHashMap<String,Column> columns(Table table){
		return columns(table.getCatalog(), table.getSchema(), table.getName());
	}
	public LinkedHashMap<String,Column> columns(String table){
		return columns(null, null, table);
	}
	public LinkedHashMap<String,Column>  columns(String catalog, String schema, String table){
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();

		Long fr = System.currentTimeMillis();
		try {
			SQL sql = new TableSQLImpl();
			sql.setDataSource(table);
			RunSQL run = SQLCreaterUtil.getCreater(getJdbc()).createQueryRunSQL(sql, null,"1=0");
			SqlRowSet set = getJdbc().queryForRowSet(run.getFinalQueryTxt());
			SqlRowSetMetaData rsm = set.getMetaData();
			for (int i = 1; i <= rsm.getColumnCount(); i++) {
				Column column = column(null, rsm, i);
				columns.put(column.getName(), column);
			}
			//isAutoIncrement isGenerated remark default
			Connection con = DataSourceUtils.getConnection(getJdbc().getDataSource());
			DatabaseMetaData metaData = con.getMetaData();
			if(null == catalog){
				catalog = con.getCatalog();
			}
			if(null == schema){
				schema = con.getSchema();
			}
			ResultSet rs = metaData.getColumns(catalog, schema, table, null);
			while (rs.next()){
				String name = rs.getString("COLUMN_NAME");
				Column column = columns.get(name);
				if(null == column){
					continue;
				}
				column.setCatalog(BasicUtil.evl(rs.getString("TABLE_CAT"), catalog));
				column.setSchema(BasicUtil.evl(rs.getString("TABLE_SCHEM"), schema));
				column.setTable(BasicUtil.evl(rs.getString("TABLE_NAME"), table));
				column(column, rs);
			}
			//主键
			rs = metaData.getPrimaryKeys(catalog, schema, table);
			while (rs.next()){
				String name = rs.getString(4);
				Column column = columns.get(name);
				if(null == column){
					continue;
				}
				column.setPrimaryKey(true);
			}


		}catch (Exception e){
			e.printStackTrace();
		}

		if (showSQL) {
			String random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
			log.warn("{}[columns][table:{}][执行耗时:{}ms]", random, table, System.currentTimeMillis() - fr);
		}
		return columns;
	}
	public boolean add(Column column){
		boolean result = false;
		Long fr = System.currentTimeMillis();
		String random = null;
		String sql = SQLCreaterUtil.getCreater(getJdbc()).createAddRunSQL(column);
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = column.getListener();
		try{
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeAdd(column);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}
		}catch (Exception e){
			e.printStackTrace();
			result = false;
		}

		if (showSQL) {
			log.warn("{}[add column][table:{}][column:{}][result:{}][执行耗时:{}ms]", random, column.getTable(), column.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}

	/**
	 * 修改列
	 * @param column 列
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 */
	private boolean alter(Column column, boolean trigger){

		boolean result = false;
		Long fr = System.currentTimeMillis();
		String random = null;
		String sql = SQLCreaterUtil.getCreater(getJdbc()).createAlterRunSQL(column);

		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = column.getListener();
		try{
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeAlter(column);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}
		}catch (Exception e){
			if(null != listener) {
				boolean exe = false;
				exe = listener.afterAlterException(column, e);
				if(exe){
					result = alter(column, false);
				}
			}else{
				e.printStackTrace();
				result = false;
			}
		}

		if (showSQL) {
			log.warn("{}[update column][table:{}][column:{}][result:{}][执行耗时:{}ms]", random, column.getTable(), column.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	public boolean alter(Column column){
		return alter(column, true);
	}
	public boolean drop(Column column){
		boolean result = false;
		Long fr = System.currentTimeMillis();
		String sql = SQLCreaterUtil.getCreater(getJdbc()).createDropRunSQL(column);
		String random = null;
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = column.getListener();
		try{
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeDrop(column);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}
		}catch (Exception e){
			e.printStackTrace();
			result = false;
		}
		if (showSQL) {
			log.warn("{}[drop column][table:{}][column:{}][result:{}][执行耗时:{}ms]", random, column.getTable(), column.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(column, result);
		}
		return result;
	}

	@Override
	public boolean drop(Table table) {
		boolean result = false;
		Long fr = System.currentTimeMillis();
		String sql = SQLCreaterUtil.getCreater(getJdbc()).createDropRunSQL(table);
		String random = null;
		if(showSQL){
			random = "[SQL:" + System.currentTimeMillis() + "-" + BasicUtil.getRandomNumberString(8) + "][thread:"+Thread.currentThread().getId()+"][ds:"+ DataSourceHolder.getDataSource()+"]";
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		try{
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeDrop(table);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}
		}catch (Exception e){
			e.printStackTrace();
			result = false;
		}

		if (showSQL) {
			log.warn("{}[drop table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}
	private Column column(Column column, SqlRowSetMetaData rsm, int index){
		if(null == column){
			column = new Column();
		}
		try {
			column.setCatalog(BasicUtil.evl(rsm.getCatalogName(index)));
			column.setSchema(BasicUtil.evl(rsm.getSchemaName(index)));
			column.setClassName(rsm.getColumnClassName(index));
			column.setCaseSensitive(rsm.isCaseSensitive(index));
			column.setCurrency(rsm.isCurrency(index));
			column.setComment(rsm.getColumnLabel(index));
			column.setName(rsm.getColumnName(index));
			column.setPrecision(rsm.getPrecision(index));
			column.setScale(rsm.getScale(index));
			column.setDisplaySize(rsm.getColumnDisplaySize(index));
			column.setSigned(rsm.isSigned(index));
			column.setTable(rsm.getTableName(index));
			column.setType(rsm.getColumnType(index));
			column.setTypeName(rsm.getColumnTypeName(index));
		}catch (Exception e){
			e.printStackTrace();
		}
		return column;
	}



	/**
	 * 构建Column
	 * TABLE_CAT=simple
	 * TABLE_SCHEM=null
	 * TABLE_NAME=hr_department
	 * COLUMN_NAME=SCORE
	 * DATA_TYPE=7
	 * TYPE_NAME=FLOAT
	 * COLUMN_SIZE=11
	 * BUFFER_LENGTH=65535
	 * DECIMAL_DIGITS=2
	 * NUM_PREC_RADIX=10
	 * NULLABLE=1
	 * REMARKS=
	 * COLUMN_DEF=null
	 * SQL_DATA_TYPE=0
	 * SQL_DATETIME_SUB=0
	 * CHAR_OCTET_LENGTH=null
	 * ORDINAL_POSITION=4
	 * IS_NULLABLE=YES
	 * SCOPE_CATALOG=null
	 * SCOPE_SCHEMA=null
	 * SCOPE_TABLE=null
	 * SOURCE_DATA_TYPE=null
	 * IS_AUTOINCREMENT=NO
	 * @param column column
	 * @param rs  ResultSet
	 * @return Column
	 */
	private Column column(Column column, ResultSet rs){
		if(null == column){
			column = new Column();
		}
		try {
			column.setScale(BasicUtil.parseInt(rs.getString("DECIMAL_DIGITS"), null));
			column.setPosition(BasicUtil.parseInt(rs.getString("ORDINAL_POSITION"), 0));
			column.setAutoIncrement(BasicUtil.parseBoolean(rs.getString("IS_AUTOINCREMENT"), false));
			column.setGenerated(BasicUtil.parseBoolean(rs.getString("IS_GENERATEDCOLUMN"), false));
			column.setComment(rs.getString("REMARKS"));
			column.setPosition(BasicUtil.parseInt(rs.getString("ORDINAL_POSITION"), 0));
			if (BasicUtil.isEmpty(column.getDefaultValue())) {
				column.setDefaultValue(rs.getObject("COLUMN_DEF"));
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return column;
	}

	/**
	 * 所引
	 * TABLE_CAT=simple
	 * TABLE_SCHEM=null
	 * TABLE_NAME=hr_department
	 * NON_UNIQUE=false
	 * INDEX_QUALIFIER=null
	 * INDEX_NAME=PRIMARY
	 * TYPE=3
	 * ORDINAL_POSITION=1
	 * COLUMN_NAME=ID
	 * ASC_OR_DESC=A
	 * CARDINALITY=81
	 * PAGES=0
	 * @param table table
	 * @return map
	 */
	public LinkedHashMap<String, Index> index(Table table){
		LinkedHashMap<String,Index> indexs = new LinkedHashMap<>();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		String tab = table.getName();
		try {
			Connection con = DataSourceUtils.getConnection(getJdbc().getDataSource());
			if(null == catalog){
				catalog = con.getCatalog();
			}
			if(null == schema){
				schema = con.getSchema();
			}
			DatabaseMetaData metaData = con.getMetaData();
			ResultSet rs = metaData.getIndexInfo(catalog, schema, tab, false, false);

			ResultSetMetaData md = rs.getMetaData();
			LinkedHashMap<String, Column> columns = null;
			while (rs.next()) {
				String name = rs.getString("INDEX_NAME");
				if(null == name){
					continue;
				}
				Index index = indexs.get(name);
				if(null == index){
					index = new Index();
					index.setName(rs.getString("INDEX_NAME"));
					index.setType(rs.getInt("TYPE"));
					index.setUnique(!rs.getBoolean("NON_UNIQUE"));
					index.setCatalog(BasicUtil.evl(rs.getString("TABLE_CAT"), catalog));
					index.setSchema(BasicUtil.evl(rs.getString("TABLE_SCHEM"), schema));
					index.setTable(rs.getString("TABLE_NAME"));
					indexs.put(name, index);
					columns = new LinkedHashMap<>();
					index.setColumns(columns);
				}else {
					columns = index.getColumns();
				}
				String columnName = rs.getString("COLUMN_NAME");
				Column col = table.getColumn(columnName);
				Column column = null;
				if(null != col){
					column = (Column) col.clone();
				}else{
					column = new Column();
					column.setName(columnName);
				}
				String order = rs.getString("ASC_OR_DESC");
				if(null != order && order.startsWith("D")){
					order = "DESC";
				}else{
					order = "ASC";
				}
				column.setOrder(order);
				column.setPosition(rs.getInt("ORDINAL_POSITION"));
				columns.put(column.getName(), column);
			}
			table.setIndexs(indexs);
		}catch (Exception e){
			e.printStackTrace();
		}
		return indexs;
	}


	/**
	 * 参数日志格式化
	 * @param params params
	 * @return String
	 */
	protected String paramLogFormat(List<?> params){
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		if(null != params){
			int idx = 0;
			for(Object param:params){
				builder.append("param").append(idx++).append("=");
				builder.append(param);
				if(null != param){
					builder.append("(").append(param.getClass().getName()).append(")");
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}
	protected String paramLogFormat(List<?> keys, List<?> values) {
		StringBuilder builder = new StringBuilder();
		builder.append("\n");
		if (null != keys && null != values) {
			if(keys.size() == values.size()) {
				int size = keys.size();
				for (int i = 0; i < size; i++) {
					Object key = keys.get(i);
					Object value = values.get(i);
					builder.append(keys.get(i)).append("=");
					builder.append(value);
					if (null != value) {
						builder.append("(").append(value.getClass().getName()).append(")");
					}
					builder.append("\n");
				}
			}else{
				return paramLogFormat(values);
			}
		}
		return builder.toString();

	}

}
