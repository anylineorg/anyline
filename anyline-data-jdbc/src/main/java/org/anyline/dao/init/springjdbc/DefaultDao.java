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


package org.anyline.dao.init.springjdbc;

import org.anyline.dao.AnylineDao;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.dao.init.BatchInsertStore;
import org.anyline.data.entity.*;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.exception.AnylineException;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.Procedure;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.ProcedureParam;
import org.anyline.data.run.Run;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.jdbc.util.SQLAdapterUtil;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Repository("anyline.dao")
public class DefaultDao<E> implements AnylineDao<E> {
	protected static final Logger log = LoggerFactory.getLogger(DefaultDao.class);

	@Autowired(required=false)
	protected JdbcTemplate jdbc;

	@Autowired(required=false)
	protected DMListener listener;

	public JdbcTemplate getJdbc(){
		return jdbc;
	}


	protected BatchInsertStore batchInsertStore = new BatchInsertStore();
 

	protected static boolean isBatchInsertRun = false;
 

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/
	/**
	 * 查询
	 */
	@Override
	public List<Map<String,Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		List<Map<String,Object>> maps = null;
		try {
			JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
			Run run = adapter.buildQueryRun(prepare, configs, conditions);
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
				log.warn(tmp);
			}
			if (run.isValid()) {
				if(null != listener){
					listener.beforeQuery(this,run);
				}
				maps = maps(adapter, run.getFinalQuery(), run.getValues());
				if(null != listener){
					listener.afterQuery(this,run, maps);
				}
				if(null != adapter){
					maps = adapter.process(maps);
				}
			} else {
				maps = new ArrayList<Map<String,Object>>();
			}
		}finally {
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return maps;
	}
	public List<Map<String,Object>> maps(RunPrepare prepare, String ... conditions){
		return maps(prepare, null, conditions);
	}
	/**
	 * 查询
	 */
	@Override
	public DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		DataSet set = null;
		try {
			JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
			Run run = adapter.buildQueryRun(prepare, configs, conditions);
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
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
						// 第一条
						total = 1;
					} else {
						// 未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = getTotal(run.getTotalQuery(), run.getValues());
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
					if(null != listener){
						listener.afterTotal(this, run, total);
					}
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("[查询记录总数][行数:{}]", total);
				}
			}
			if (run.isValid() && (null == navi || total > 0)) {
				if(null != listener){
					listener.beforeQuery(this,run);
				}
				set = select(adapter, run.getFinalQuery(), run.getValues());
				if(null != listener){
					listener.afterQuery(this,run,set);

				}
			} else {
				set = new DataSet();
			}
			set.setDataSource(prepare.getDataSource());
//		set.setSchema(sql.getSchema());
//		set.setTable(sql.getTable());
			set.setNavi(navi);
			if (null != navi && navi.isLazy()) {
				PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
			}
		}finally {
			// 自动切换回默认数据源
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
			RunPrepare prepare = new DefaultTablePrepare();
			if(AdapterProxy.hasAdapter()){
				prepare.setDataSource(AdapterProxy.table(clazz));
			}
			JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
			Run run = adapter.buildQueryRun(prepare, configs, conditions);
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false]";
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + DataSourceHolder.getDataSource() + "]";
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
						// 第一条
						total = 1;
					} else {
						// 未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = getTotal(run.getTotalQuery(), run.getValues());
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
					if(null != listener){
						listener.afterTotal(this, run, total);
					}
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("[查询记录总数][行数:{}]", total);
				}
			}
			if (run.isValid() && (null == navi || total > 0)) {
				if(null != listener){
					listener.beforeQuery(this,run);
				}
				list = select(adapter, clazz, run.getFinalQuery(), run.getValues());
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
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return list;
	}

	public DataSet querys(RunPrepare prepare, String ... conditions){
		return querys(prepare, null, conditions);
	}

	/**
	 * 查询
	 */
	@Override
	public DataSet selects(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return querys(prepare, configs, conditions);
	}
	public DataSet selects(RunPrepare prepare, String ... conditions){
		return querys(prepare, null, conditions);
	}
	public int count(RunPrepare prepare, ConfigStore configs, String ... conditions){
		int count = -1;
		try{
			Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildQueryRun(prepare, configs, conditions);
			if(null != listener){
				listener.beforeCount(this,run);
			}
			count = getTotal(run.getTotalQuery(), run.getValues());
			if(null != listener){
				listener.afterCount(this,run, count);
			}
		}finally{
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return count;
	}
	public int count(RunPrepare prepare, String ... conditions){
		return count(prepare, null, conditions);
	}
	public boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions){
		boolean result = false;
		try {
			Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildQueryRun(prepare, configs, conditions);
			String txt = run.getFinalExists();
			List<Object> values = run.getValues();

			long fr = System.currentTimeMillis();
			String random = "";
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
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
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, System.currentTimeMillis() - fr, LogUtil.format(result, 34));
				}
			} catch (Exception e) {
				if (ConfigTable.IS_SHOW_SQL_WHEN_ERROR) {
					log.error("[{}][txt:\n{}\n]", random, LogUtil.format("查询异常", 33), prepare);
					log.error("{}[参数][param:{}]", random, paramLogFormat(values));
				}
				throw e;
			}
		}finally {
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	public boolean exists(RunPrepare prepare, String ... conditions){
		return exists(prepare, null, conditions);
	}
	/**
	 * 总记录数
	 * @param sql sql
	 * @param values values
	 * @return int
	 */
	protected int getTotal(String sql, List<Object> values) {
		int total = 0;
		DataSet set = select(null, sql,values);
		total = set.getInt(0,"CNT",0);
		return total;
	}
	/**
	 * 更新记录
	 * @param data		需要更新的数据
	 * @param dest		需要更新的表，如果没有提供则根据data解析
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */
	@Override
	public int update(String dest, Object data, ConfigStore configs, List<String> columns){
		if(null == data){
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				throw new SQLUpdateException("更新空数据");
			}else{
				log.error("更新空数据");
			}
		}
		int result = 0;
		if(data instanceof DataSet){
			DataSet set = (DataSet)data;
			for(int i=0; i<set.size(); i++){
				result += update(dest, set.getRow(i), configs,  columns);
			}
			return result;
		}
		Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildUpdateRun(dest, data, configs,false, columns);
		String sql = run.getFinalUpdate();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]",dest);
			return -1;
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn(random + "[txt:\n{}\n]",sql);
			log.warn(random + "[参数:{}]",paramLogFormat(run.getUpdateColumns(),values));
		}
		/*执行SQL*/
		try{
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeUpdate(this,run, dest, data, columns);
			}
			if(listenerResult) {
				result = getJdbc().update(sql, values.toArray());
				if (null != listener) {
					listener.afterUpdate(this, run, result, dest, data, columns);
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn(random + "[执行耗时:{}ms][影响行数:{}]", System.currentTimeMillis() - fr, LogUtil.format(result, 34));
				}

			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("[{}][txt:\n{}\n]", random, LogUtil.format("更新异常", 33), sql);
				log.error("{}[参数][param:{}]", random, paramLogFormat(run.getUpdateColumns(),values));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("insert异常",e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
		}finally{
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}

	@Override
	public int update(Object data, ConfigStore configs, String ... columns){
		return update(null, data, configs, BeanUtil.array2list(columns));
	}
	@Override
	public int update(String dest, Object data, ConfigStore configs, String ... columns){
		return update(dest, data, configs, BeanUtil.array2list(columns));
	}
	@Override
	public int update(Object data, ConfigStore configs, List<String> columns){
		return update(null, data, configs, columns);
	}
	@Override
	public int update(String dest, Object data, String ... columns){
		return update(dest, data, (ConfigStore) null, BeanUtil.array2list(columns));
	}
	@Override
	public int update(Object data, String ... columns){
		return update(null, data, (ConfigStore) null, BeanUtil.array2list(columns));
	}
	@Override
	public int update(String dest, Object data, List<String> columns){
		return update(dest, data, (ConfigStore) null, columns);
	}
	@Override
	public int update(Object data, List<String> columns){
		return update(null, data, (ConfigStore) null, columns);
	}
	/**
	 * 保存(insert|upate)
	 */
	@Override
	public int save(String dest, Object data, boolean checkPrimary, String ... columns){
		if(null == data){

			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				throw new SQLUpdateException("save空数据");
			}else {
				log.error("save空数据");
			}
		}
		if(data instanceof Collection){
			Collection<?> items = (Collection<?>)data;
			int cnt = 0;
			for(Object item:items){
				cnt += save(dest, item, checkPrimary, columns);
			}
			return cnt;
		}
		return saveObject(dest, data, checkPrimary, columns);

	}

	@Override
	public int save(Object data, boolean checkPrimary, String ... columns){
		return save(null, data, checkPrimary, columns);
	}
	@Override
	public int save(String dest, Object data, String ... columns){
		return save(dest, data, false, columns);
	}
	@Override
	public int save(Object data, String ... columns){
		return save(null, data, false, columns);
	}


	protected int saveObject(String dest, Object data, boolean checkPrimary, String ... columns){
		if(null == data){
			return 0;
		}
		if(checkIsNew(data)){
			return insert(dest, data, checkPrimary, columns);
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
	 * @param checkPrimary   是否需要检查重复主键,默认不检查
	 * @param columns  需要插入的列
	 * @param dest  表
	 * @param data  data
	 * @return int 影响行数
	 */
	@Override
	public int insert(String dest, Object data, boolean checkPrimary, List<String> columns) {
		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		Run run = adapter.buildInsertRun(dest, data, checkPrimary, columns);

		if(null == run){
			return 0;
		}
		int cnt = 0;
		final String sql = run.getFinalInsert();
		final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn(random + "[txt:\n{}\n]",sql);
			log.warn(random + "[参数:{}]",paramLogFormat(run.getInsertColumns(),values));
		}
		try{
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeInsert(this, run, dest, data, checkPrimary, columns);
			}
			if(listenerResult) {
				cnt = adapter.insert(random, data, sql, values, null);
				if (null != listener) {
					listener.afterInsert(this, run, cnt, dest, data, checkPrimary, columns);
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random , System.currentTimeMillis() - fr, LogUtil.format(cnt, 34));
				}
			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("{}[{}][txt:\n{}\n]", random, LogUtil.format("插入异常", 33), sql);
				log.error("{}[参数][param:{}]", random, paramLogFormat(run.getInsertColumns(),values));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("insert异常",e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
		}finally{
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return cnt;
	}

	@Override
	public int insert(Object data, boolean checkPrimary, List<String> columns){
		return insert(null, data, checkPrimary, columns);
	}
	@Override
	public int insert(String dest, Object data, List<String> columns){
		return insert(dest, data, false, columns);
	}
	@Override
	public int insert(Object data, List<String> columns){
		return insert(null, data, false, columns);
	}


	@Override
	public int insert(String dest, Object data, boolean checkPrimary, String ... columns) {
		return insert(dest, data, checkPrimary, BeanUtil.array2list(columns));
	}

	@Override
	public int insert(Object data, boolean checkPrimary, String ... columns){
		return insert(null, data, checkPrimary, BeanUtil.array2list(columns));
	}
	@Override
	public int insert(String dest, Object data, String ... columns){
		return insert(dest, data, false, BeanUtil.array2list(columns));
	}
	@Override
	public int insert(Object data, String ... columns){
		return insert(null, data, false, BeanUtil.array2list(columns));
	}
	@Override
	public int batchInsert(final String dest, final Object data, final boolean checkPrimary, final String ... columns){
		if(null == data){
			return 0;
		}
		if(data instanceof DataSet){
			DataSet set = (DataSet)data;
			int size = set.size();
			for(int i=0; i<size; i++){
				batchInsert(dest, set.getRow(i), checkPrimary, columns);
			}
		}

		String table = DataSourceHolder.parseDataSource(dest,data);//SQLAdapterUtil.getAdapter(getJdbc()).getDataSource(data);
		List<String> cols = SQLAdapterUtil.getAdapter(getJdbc()).confirmInsertColumns(dest, data, BeanUtil.array2list(columns));
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
										listenerResult = listener.beforeBatchInsert(DefaultDao.this,dest, list, checkPrimary, BeanUtil.array2list(columns));
									}
									if(listenerResult) {
										int cnt = insert(dest, list, checkPrimary, columns);
										if (null != listener) {
											listener.afterBatchInsert(DefaultDao.this, cnt, dest, list, checkPrimary, BeanUtil.array2list(columns));
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
	public int batchInsert(Object data, boolean checkPrimary, String ... columns){
		return batchInsert(null, data, checkPrimary, columns);
	}
	@Override
	public int batchInsert(String dest, Object data, String ... columns){
		return batchInsert(dest, data, false, columns);
	}
	@Override
	public int batchInsert(Object data, String ... columns){
		return batchInsert(null, data, false, columns);
	}


	/**
	 * 查询
	 * @param sql  sql
	 * @param values  values
	 * @return List
	 */
	protected List<Map<String,Object>> maps(JDBCAdapter adapter, String sql, List<Object> values){
		List<Map<String,Object>> maps = null;
		if(BasicUtil.isEmpty(sql)){
			throw new SQLQueryException("未指定SQL");
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn(random + "[执行耗时:{}ms]",mid - fr);
			}
			if(null != adapter){
				maps = adapter.process(maps);
			}
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn(random + "[封装耗时:{}ms][封装行数:{}]",System.currentTimeMillis() - mid,maps.size() );
			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("[{}][txt:\n{}\n]",LogUtil.format("查询异常", 33), random, sql);
				log.error("[{}][参数:{}]", random, paramLogFormat(values));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常");
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
		}
		return maps;
	}
	/**
	 * 查询
	 * @param adapter  adapter
	 * @param sql  sql
	 * @param values  values
	 * @return DataSet
	 */
	protected DataSet select(JDBCAdapter adapter, String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			throw new SQLQueryException("未指定SQL");
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn(random + "[执行耗时:{}ms]",mid - fr);
			}
			if(null != adapter) {
				list = adapter.process(list);
			}
			for(Map<String,Object> map:list){
				DataRow row = new DataRow(map);
				row.clearUpdateColumns();
				set.add(row);
			}
			set.setDatalink(DataSourceHolder.getDataSource());
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn(random + "[封装耗时:{}ms][封装行数:{}]",System.currentTimeMillis() - mid,list.size() );
			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("[{}][txt:\n{}\n]",LogUtil.format("查询异常", 33), random, sql);
				log.error("[{}][参数:{}]", random, paramLogFormat(values));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常",e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
		}
		return set;
	}

	protected <T> EntitySet<T> select(JDBCAdapter adapter, Class<T> clazz, String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			throw new SQLQueryException("未指定SQL");
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn(random + "[执行耗时:{}ms]",mid - fr);
			}
			if(null != adapter) {
				list = adapter.process(list);
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
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn(random + "[封装耗时:{}ms][封装行数:{}]",System.currentTimeMillis() - mid,list.size() );
			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("{}[{}][txt:\n{}\n]",random, LogUtil.format("查询异常", 33), sql);
				log.error("{}}[参数:{}]",random,paramLogFormat(values));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常",e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
		}
		return set;
	}
	@Override
	public int execute(RunPrepare prepare, ConfigStore configs, String ... conditions){
		int result = -1;
		Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildExecuteRunSQL(prepare, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false]");
			}
			return -1;
		}
		String txt = run.getFinalExecute();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn(random + "[执行耗时:{}ms][影响行数:{}]", System.currentTimeMillis() - fr, LogUtil.format(result, 34));
				}

			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("{},[{}][txt:\n{}\n]" ,random, LogUtil.format("SQL执行异常", 33),prepare);
				log.error("{}[参数:{}]",random , paramLogFormat(values));
			}
			throw e;
		}finally{
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}
	@Override
	public int execute(RunPrepare prepare, String ... conditions){
		return execute(prepare, null, conditions);
	}

	@Override
	public boolean execute(Procedure procedure){
		boolean result = false;
		List<Object> list = new ArrayList<Object>();
		final List<ProcedureParam> inputs = procedure.getInputs();
		final List<ProcedureParam> outputs = procedure.getOutputs();
		long fr = System.currentTimeMillis();
		String random = "";
		String sql = " {";

		// 带有返回值
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

		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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

						// 带有返回参数
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
							// 注册输出参数
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
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms]", random, System.currentTimeMillis() - fr);
					log.warn("{}[输出参数:{}]", random, list);
				}
			}
		}catch(Exception e){
			result = false;
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("{}[{}][txt:\n{}\n]",random,LogUtil.format("存储过程执行异常", 33), sql);
				log.error("{}[输入参数:{}]",random,paramLogFormat(inputs));
				log.error("{}[输出参数:{}]",random,paramLogFormat(outputs));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("execute异常",e);
				ex.setSql(sql);
				throw ex;
			}
		}finally{
			// 自动切换回默认数据源
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
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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
							if(first ==0 && last==0){ // 只取一行
								break;
							}
						}
					}
					if(null != navi){
						navi.setTotalRow(index);
						set.setNavi(navi);
					}

					set.setDatalink(DataSourceHolder.getDataSource());
					if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
						log.warn("{}[封装耗时:{}ms][封装行数:{}]", rdm, System.currentTimeMillis() - mid,set.size());
					}
					return set;
				}
			});
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("{}[执行耗时:{}ms]", random,System.currentTimeMillis() - fr);
			}
			if(null != listener){
				listener.afterQuery(this,procedure, set);
			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("{}[{}][txt:\n{}\n]",random,LogUtil.format("存储过程查询异常", 33), procedure.getName());
				log.error("{}[输入参数:{}]",random,paramLogFormat(inputs));
				log.error("{}[输出参数:{}]",random,paramLogFormat(inputs));
			}
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常",e);
				throw ex;
			}
		}finally{
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return set;
	}

	public int deletes(String table, String key, Collection<Object> values){
		Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildDeleteRunSQL(table, key, values);
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
		Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildDeleteRunSQL(table, key, list);
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
				log.warn("[delete Collection][影响行数:{}]", LogUtil.format(size, 34));
			}else{
				Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildDeleteRunSQL(dest, obj, columns);
				size = exeDelete(run);

			}
		}
		return size;
	}

	@Override
	public int delete(String table, ConfigStore configs, String... conditions) {
		Run run = SQLAdapterUtil.getAdapter(getJdbc()).buildDeleteRunSQL(table, configs, conditions);
		int result = exeDelete(run);
		return result;
	}

	protected int exeDelete(Run run){
		int result = 0;
		final String sql = run.getFinalDelete();
		final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
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
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, System.currentTimeMillis() - fr, LogUtil.format(result, 34));
				}
				// result = 1;
				if(null != listener){
					listener.afterDelete(this,run, result);
				}
			}
		}catch(Exception e){
			if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
				log.error("{}[{}][txt:\n{}\n]", random, LogUtil.format("删除异常", 33), sql);
				log.error("{}[参数:{}]",random, paramLogFormat(values));
			}
			result = 0;
			e.printStackTrace();
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("delete异常",e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}
		}finally{
			// 自动切换回默认数据源
			if(DataSourceHolder.isAutoDefault()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
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
	 * index			: 索引
	 * constraint		: 约束
	 *
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Database> databases()
	 ******************************************************************************************************************/

	@Override
	public LinkedHashMap<String, Database> databases(){
		LinkedHashMap<String,Database> databases = new LinkedHashMap<>();

		DataSource ds = null;
		Connection con = null;
		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			Map<String,String> table_map = table_maps.get(DataSourceHolder.getDataSource()+"");
			if(null == table_map){
				table_map = new HashMap<>();
				table_maps.put(DataSourceHolder.getDataSource()+"", table_map);
			}
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryDatabaseRunSQL();
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(adapter, sql, null).toUpperKey();
							databases = adapter.databases(idx++, true, databases, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.getMessage());
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[databases][result:{}][执行耗时:{}ms]", random, databases.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return databases;
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Table> tables(String catalog, String schema, String name, String types)
	 * public LinkedHashMap<String, Table> tables(String schema, String name, String types)
	 * public LinkedHashMap<String, Table> tables(String name, String types)
	 * public LinkedHashMap<String, Table> tables(String types)
	 * public LinkedHashMap<String, Table> tables()
	 ******************************************************************************************************************/

	private static Map<String,Map<String,String>> table_maps = new HashMap<>();
	/**
	 * tables
	 * @param catalog 对于MySQL，则对应相应的数据库，对于Oracle来说，则是对应相应的数据库实例，可以不填，也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名，而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意，其登陆名必须是大写，不然的话是无法获取到相应的数据，而MySQL则不做强制要求。
	 * @param pattern 一般情况下如果要获取所有的表的话，可以直接设置为null，如果设置为特定的表名称，则返回该表的具体信息。
	 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Table> tables(String catalog, String schema, String pattern, String types){
		LinkedHashMap<String,Table> tables = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;
		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
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
			Map<String,String> table_map = table_maps.get(DataSourceHolder.getDataSource()+"");
			if(null == table_map){
				table_map = new HashMap<>();
				table_maps.put(DataSourceHolder.getDataSource()+"", table_map);
			}
			if(null != pattern){
				if(table_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败，先查询全部表，生成缓存，再从缓存中不区分大小写查询
					LinkedHashMap<String,Table> all = tables(catalog, schema, null, types);
					for(Table table:all.values()){
						table_map.put(table.getName().toUpperCase(), table.getName());
					}
				}
				if(table_map.containsKey(pattern.toUpperCase())){
					pattern = table_map.get(pattern.toUpperCase());
				}
			}
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryTableRunSQL(catalog, schema, pattern, types);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(adapter, sql, null).toUpperKey();
							tables = adapter.tables(idx++, true, catalog, schema, tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.getMessage());
				}
			}

			// 根据jdbc接口补充
			try {
				ResultSet set = con.getMetaData().getTables(catalog, schema, pattern, tps );
				tables = adapter.tables(true, catalog, schema, tables, set);
			}catch (Exception e){
				log.warn("{}[tables][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据jdbc接口补充失败", 33), catalog, schema, pattern, e.getMessage());
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}

	@Override
	public LinkedHashMap<String,Table> tables(String schema, String name, String types){
		return tables(null, schema, name, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(String name, String types){
		return tables(null, null, name, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(String types){
		return tables(null, null, null, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(){
		return tables(null, null, null, "TABLE");
	}


	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types)
	 * public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types)
	 * public LinkedHashMap<String, MasterTable> mtables(String name, String types)
	 * public LinkedHashMap<String, MasterTable> mtables(String types)
	 * public LinkedHashMap<String, MasterTable> mtables()
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String pattern, String types) {

		LinkedHashMap<String, MasterTable> tables = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;
		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
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
			Map<String,String> table_map = table_maps.get(DataSourceHolder.getDataSource()+"");
			if(null == table_map){
				table_map = new HashMap<>();
				table_maps.put(DataSourceHolder.getDataSource()+"", table_map);
			}
			if(null != pattern){
				if(table_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败，先查询全部表，生成缓存，再从缓存中不区分大小写查询
					LinkedHashMap<String, MasterTable> all = mtables(catalog, schema, null, types);
					for(Table table:all.values()){
						table_map.put(table.getName().toUpperCase(), table.getName());
					}
				}
				if(table_map.containsKey(pattern.toUpperCase())){
					pattern = table_map.get(pattern.toUpperCase());
				}
			}

			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryMasterTableRunSQL(catalog, schema, pattern, types);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(adapter, sql, null).toUpperKey();
							tables = adapter.mtables(idx++, true, catalog, schema, tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.getMessage());
				}
			}

			// 根据jdbc接口补充
			try {
				ResultSet set = con.getMetaData().getTables(catalog, schema, pattern, tps );
				tables = adapter.mtables(false, catalog, schema, tables, set);
			}catch (Exception e){
				log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据jdbc接口补充失败", 33), catalog, schema, pattern, e.getMessage());
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[stables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types) {
		return mtables(null, schema, name, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(String name, String types) {
		return mtables(null, null, name, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(String types) {
		return mtables(null, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables() {
		return mtables("STABLE");
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types)
	 * public LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types)
	 * public LinkedHashMap<String, PartitionTable> ptables(String name, String types)
	 * public LinkedHashMap<String, PartitionTable> ptables(String types)
	 * public LinkedHashMap<String, PartitionTable> ptables()
	 * public LinkedHashMap<String, PartitionTable> ptables(MasterTable table)
	 ******************************************************************************************************************/

	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String name, String types){
		return null;
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String schema, String name, String types){
		return null;
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String name, String types){
		return null;
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String types){
		return null;
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(){
		return null;
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(MasterTable master){
		LinkedHashMap<String,PartitionTable> tables = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;
		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryPartitionTableRunSQL(master);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(adapter, sql, null).toUpperKey();
							tables = adapter.ptables(idx++, true, master, master.getCatalog(), master.getSchema(), tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][stable:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), master.getName(), e.getMessage());
				}
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tables][stable:{}][result:{}][执行耗时:{}ms]", random, master.getName(), tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Column> columns(Table table)
	 * public LinkedHashMap<String, Column> columns(String table)
	 * public LinkedHashMap<String, Column> columns(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, Column> columns(Table table){
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
		long fr = System.currentTimeMillis();
		DataSource ds = null;
		Connection con = null;
		String random = null;
		DatabaseMetaData metadata = null;
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			random = random();
		}

		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		try {
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			metadata = con.getMetaData();;
			if (null == catalog) {
				catalog = con.getCatalog();
				table.setCatalog(catalog);
			}
			if(null == schema){
				schema = con.getSchema();
				table.setSchema(schema);
			}
		}catch (Exception e){}

		// 先根据metadata解析 SELECT * FROM T WHERE 1=0
		try {
			List<String> sqls = adapter.buildQueryColumnRunSQL(table , true);
			if(null != sqls){
				for(String sql:sqls) {
					if (BasicUtil.isNotEmpty(sql)) {
						SqlRowSet set = getJdbc().queryForRowSet(sql);
						columns = adapter.columns(true, table, columns, set);
					}
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.getMessage());
			}
		}

		// 再根据系统表查询
		try{
			List<String> sqls = adapter.buildQueryColumnRunSQL(table, false);
			if(null != sqls){
				int idx = 0;
				for(String sql:sqls){
					if(BasicUtil.isNotEmpty(sql)) {
						DataSet set = select(adapter, sql, null);
						columns = adapter.columns(idx, true, table, columns, set);
					}
					idx ++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.getMessage());
			}
		}

		// 根据jdbc接口补充
		try {
			// isAutoIncrement isGenerated remark default
			ResultSet rs = metadata.getColumns(catalog, schema, table.getName(), null);
			columns = adapter.columns(true, table, columns, rs);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}

		// 主键
		try {
			ResultSet rs = metadata.getPrimaryKeys(catalog, schema, table.getName());
			while (rs.next()) {
				String name = rs.getString(4);
				Column column = columns.get(name.toUpperCase());
				if (null == column) {
					continue;
				}
				column.setPrimaryKey(true);
			}
		}catch (Exception e){

		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[columns][catalog:{}][schema:{}][table:{}][执行耗时:{}ms]", random, catalog, schema, table, System.currentTimeMillis() - fr);
		}
		return columns;
	}
	@Override
	public LinkedHashMap<String,Column> columns(String table){
		return columns(null, null, table);
	}
	@Override
	public LinkedHashMap<String,Column>  columns(String catalog, String schema, String table){
		Table tab = new Table(catalog, schema, table);
		return columns(tab);
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Tag> tags(Table table)
	 * public LinkedHashMap<String, Tag> tags(String table)
	 * public LinkedHashMap<String, Tag> tags(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, Tag> tags(Table table) {

		LinkedHashMap<String,Tag> tags = new LinkedHashMap<>();
		long fr = System.currentTimeMillis();
		DataSource ds = null;
		Connection con = null;
		String random = null;
		DatabaseMetaData metadata = null;
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			random = random();
		}

		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		try {
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			metadata = con.getMetaData();;
			if (null == catalog) {
				catalog = con.getCatalog();
				table.setCatalog(catalog);
			}
			if(null == schema){
				schema = con.getSchema();
				table.setSchema(schema);
			}
		}catch (Exception e){}

		// 先根据metadata解析 SELECT * FROM T WHERE 1=0
		try {
			List<String> sqls = adapter.buildQueryTagRunSQL(table , true);
			if(null != sqls){
				for(String sql:sqls) {
					if (BasicUtil.isNotEmpty(sql)) {
						SqlRowSet set = getJdbc().queryForRowSet(sql);
						tags = adapter.tags(true, table, tags, set);
					}
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tags][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.getMessage());
			}
		}

		// 再根据系统表查询
		try{
			List<String> sqls = adapter.buildQueryTagRunSQL(table, false);
			if(null != sqls){
				int idx = 0;
				for(String sql:sqls){
					if(BasicUtil.isNotEmpty(sql)) {
						DataSet set = select(adapter, sql, null).toUpperKey();
						tags = adapter.tags(idx, true, table, tags, set);
					}
					idx ++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tags][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), catalog, schema, table, e.getMessage());
			}
		}

		// 根据jdbc接口补充
		try {
			// isAutoIncrement isGenerated remark default
			ResultSet rs = metadata.getColumns(catalog, schema, table.getName(), null);
			// 这一步会查出所有列(包括非tag列)
			tags = adapter.tags(false, table, tags, rs);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}

		// 主键
		try {
			ResultSet rs = metadata.getPrimaryKeys(catalog, schema, table.getName());
			while (rs.next()) {
				String name = rs.getString(4);
				Tag tag = tags.get(name.toUpperCase());
				if (null == tag) {
					continue;
				}
				tag.setPrimaryKey(true);
			}
		}catch (Exception e){

		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[tags][catalog:{}][schema:{}][table:{}][执行耗时:{}ms]", random, catalog, schema, table, System.currentTimeMillis() - fr);
		}
		return tags;
	}

	@Override
	public LinkedHashMap<String, Tag> tags(String table) {
		Table tab = new Table();
		tab.setName(table);
		return tags(tab);
	}

	@Override
	public LinkedHashMap<String, Tag> tags(String catalog, String schema, String table) {
		Table tab = new Table();
		tab.setCatalog(catalog);
		tab.setSchema(schema);
		tab.setName(table);
		return tags(tab);
	}


	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Index> indexs(Table table)
	 * public LinkedHashMap<String, Index> indexs(String table)
	 * public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 所引
	 * @param table 表
	 * @return map
	 */
	@Override
	public LinkedHashMap<String, Index> indexs(Table table){
		LinkedHashMap<String,Index> indexs = null;
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		String tab = table.getName();
		DataSource ds = null;
		Connection con = null;
		JDBCAdapter adapter = SQLAdapterUtil.getAdapter(getJdbc());
		try {
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			if(null == catalog){
				catalog = con.getCatalog();
				table.setCatalog(catalog);
			}
			if(null == schema){
				schema = con.getSchema();
				table.setSchema(schema);
			}
			DatabaseMetaData metaData = con.getMetaData();
			ResultSet set = metaData.getIndexInfo(catalog, schema, tab, false, false);
			indexs = adapter.indexs(true, table, indexs, set);
			table.setIndexs(indexs);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return indexs;
	}

	@Override
	public LinkedHashMap<String, Index> indexs(String table) {
		Table tab = new Table();
		tab.setName(table);
		return indexs(tab);
	}

	@Override
	public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table) {
		Table tab = new Table();
		tab.setCatalog(catalog);
		tab.setSchema(schema);
		tab.setName(table);
		return indexs(tab);
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Constraint> constraints(Table table)
	 * public LinkedHashMap<String, Constraint> constraints(String table)
	 * public LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, Constraint> constraints(Table table) {
		return null;
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(String table) {
		return null;
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table) {
		return null;
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
	 * public boolean create(Table table) throws Exception;
	 * public boolean alter(Table table) throws Exception;
	 * public boolean drop(Table table) throws Exception;
	 ******************************************************************************************************************/
	@Override
	public boolean create(Table table) throws Exception {
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(table);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildCreateRunSQL(table);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[create table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}

	@Override
	public boolean alter(Table table) throws Exception {
		boolean result = false;
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		check(table);
		if(!name.equalsIgnoreCase(uname)){
			// 修改表名
			String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildRenameRunSQL(table);
			String random = null;
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				random = random();
				log.warn("{}[txt:\n{}\n]",random,sql);
			}

			DDListener listener = table.getListener();
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeRename(table);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[rename table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
			}
			if(null != listener){
				listener.afterRename(table, result);
			}
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn);
				column.setService(table.getService());
				alter(column);
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
			}
		}
		// 删除列
		if(ConfigTable.IS_DDL_AUTO_DROP_COLUMN) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
				}
			}
		}
		return result;
	}

	/**
	 * 检测 catalog schema
	 * @param table
	 */
	private void check(Table table){
		DataSource ds = null;
		Connection con = null;
		try {
			ds = getJdbc().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			if (null == table.getCatalog()) {
				table.setCatalog(con.getCatalog());
			}
			if (null == table.getSchema()) {
				table.setSchema(con.getSchema());
			}

		}catch (Exception e){
			log.warn("check table exception");
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
	}
	private void check(Column column){
		Table table = column.getTable();
		if(null != table){
			check(table);
			column.setCatalog(table.getCatalog());
			column.setSchema(table.getSchema());
		}
	}
	@Override
	public boolean drop(Table table) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(table);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildDropRunSQL(table);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[drop table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean create(MasterTable table) throws Exception;
	 * public boolean alter(MasterTable table) throws Exception;
	 * public boolean drop(MasterTable table) throws Exception;
	 ******************************************************************************************************************/
	@Override
	public boolean create(MasterTable table) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(table);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildCreateRunSQL(table);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[create master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}
	@Override
	public boolean alter(MasterTable table) throws Exception{
		boolean result = false;
		check(table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		LinkedHashMap<String, Tag> tags = table.getTags();
		LinkedHashMap<String, Tag> utags = update.getTags();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			// 修改表名
			String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildRenameRunSQL(table);
			String random = null;
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				random = random();
				log.warn("{}[txt:\n{}\n]",random,sql);
			}

			DDListener listener = table.getListener();
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeRename(table);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[rename master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
			}
			if(null != listener){
				listener.afterRename(table, result);
			}
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn);
				column.setService(table.getService());
				alter(column);
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
			}
		}
		// 删除列
		if(ConfigTable.IS_DDL_AUTO_DROP_COLUMN) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
				}
			}
		}
		// 更新标签
		for(Tag utag : utags.values()){
			Tag tag = tags.get(utag.getName().toUpperCase());
			if(null != tag){
				// 修改列
				tag.setTable(update);
				tag.setUpdate(utag);
				tag.setService(table.getService());
				alter(tag);
			}else{
				// 添加列
				utag.setTable(update);
				add(utag);
			}
		}
		// 删除标签
		if(ConfigTable.IS_DDL_AUTO_DROP_COLUMN) {
			for (Tag tag : tags.values()) {
				Tag utag = utags.get(tag.getName().toUpperCase());
				if (null == utag) {
					tag.setTable(update);
					drop(tag);
				}
			}
		}
		return result;
	}
	@Override
	public boolean drop(MasterTable table) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(table);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildDropRunSQL(table);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[drop master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean create(PartitionTable table) throws Exception;
	 * public boolean alter(PartitionTable table) throws Exception;
	 * public boolean drop(PartitionTable table) throws Exception;
	 ******************************************************************************************************************/

	@Override
	public boolean create(PartitionTable table) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(table);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildCreateRunSQL(table);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[create partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}
	@Override
	public boolean alter(PartitionTable table) throws Exception{

		boolean result = false;
		check(table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			// 修改表名
			String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildRenameRunSQL(table);
			String random = null;
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				random = random();
				log.warn("{}[txt:\n{}\n]",random,sql);
			}

			DDListener listener = table.getListener();
			boolean exe = true;
			if(null != listener){
				exe = listener.beforeRename(table);
			}
			if(exe) {
				getJdbc().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[rename partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
			}
			if(null != listener){
				listener.afterRename(table, result);
			}
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn);
				column.setService(table.getService());
				alter(column);
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
			}
		}
		// 删除列
		if(ConfigTable.IS_DDL_AUTO_DROP_COLUMN) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
				}
			}
		}
		return result;
	}
	@Override
	public boolean drop(PartitionTable table) throws Exception{

		boolean result = false;
		long fr = System.currentTimeMillis();
		check(table);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildDropRunSQL(table);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[drop partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Column column) throws Exception;
	 * public boolean alter(Table table, Column column) throws Exception;
	 * public boolean alter(Column column) throws Exception;
	 * public boolean drop(Column column) throws Exception;
	 *
	 * private boolean alter(Table table, Column column, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Column column) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		String random = null;
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildAddRunSQL(column);
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = column.getListener();

		boolean exe = true;
		if(null != listener){
			exe = listener.beforeAdd(column);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[add column][table:{}][column:{}][result:{}][执行耗时:{}ms]", random, column.getTableName(), column.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}

	@Override
	public boolean alter(Table table, Column column) throws Exception{
		return alter(table, column, true);
	}
	@Override
	public boolean alter(Column column) throws Exception{
		Table table = column.getTable();
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(column.getCatalog(), column.getSchema(), column.getTableName(), "TABLE");
			if(tables.size() ==0){
				throw new AnylineException("表不存在:"+column.getTableName());
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, column, true);
	}
	@Override
	public boolean drop(Column column) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(column);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildDropRunSQL(column);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = column.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(column);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[drop column][table:{}][column:{}][result:{}][执行耗时:{}ms]", random, column.getTableName(), column.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(column, result);
		}
		return result;
	}

	/**
	 * 修改列
	 * @param column 列
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	private boolean alter(Table table, Column column, boolean trigger) throws Exception{

		boolean result = true;
		long fr = System.currentTimeMillis();
		String random = null;
		check(column);
		List<String> sqls = SQLAdapterUtil.getAdapter(getJdbc()).buildAlterRunSQL(column);

		random = random();
		DDListener listener = column.getListener();
		try{
			for(String sql:sqls) {
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[txt:\n{}\n]", random, sql);
				}
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeAlter(column);
				}
				if (exe) {
					getJdbc().update(sql);
					result = true;
				}
			}
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			log.warn("{}[{}][exception:{}]",random, LogUtil.format("修改Column执行异常", 33), e.getMessage());
			if(trigger && null != listener && !BasicUtil.equalsIgnoreCase(column.getTypeName(), column.getUpdate().getTypeName())) {
				boolean exe = false;
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					exe = listener.afterAlterColumnException(table, column, e);
				}
				log.warn("{}[修改Column执行异常][尝试修正数据][修正结果:{}]", random, exe);
				if (exe) {
					result = alter(table, column, false);
				}
			}else{
				log.warn("{}[修改Column执行异常][中断执行]",random);
				result = false;
				throw e;
			}
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[update column][table:{}][column:{}][qty:{}][result:{}][执行耗时:{}ms]"
					, random, column.getTableName(), column.getName(), sqls.size(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Tag tag) throws Exception;
	 * public boolean alter(Table table, Tag tag) throws Exception;
	 * public boolean alter(Tag tag) throws Exception;
	 * public boolean drop(Tag tag) throws Exception;
	 *
	 * private boolean alter(Table table, Tag tag, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Tag tag) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		String random = null;
		check(tag);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildAddRunSQL(tag);
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = tag.getListener();

		boolean exe = true;
		if(null != listener){
			exe = listener.beforeAdd(tag);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[add tag][table:{}][tag:{}][result:{}][执行耗时:{}ms]", random, tag.getTableName(), tag.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}

	@Override
	public boolean alter(Table table, Tag tag) throws Exception{
		return alter(table, tag, true);
	}
	@Override
	public boolean alter(Tag tag) throws Exception{
		Table table = tag.getTable();
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(tag.getCatalog(), tag.getSchema(), tag.getTableName(), "TABLE");
			if(tables.size() ==0){
				throw new AnylineException("表不存在:"+tag.getTableName());
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, tag, true);
	}
	@Override
	public boolean drop(Tag tag) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		check(tag);
		String sql = SQLAdapterUtil.getAdapter(getJdbc()).buildDropRunSQL(tag);
		String random = null;
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[txt:\n{}\n]",random,sql);
		}
		DDListener listener = tag.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(tag);
		}
		if(exe) {
			getJdbc().update(sql);
			result = true;
		}
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[drop tag][table:{}][tag:{}][result:{}][执行耗时:{}ms]", random, tag.getTableName(), tag.getName(), result, System.currentTimeMillis() - fr);
		}
		if(null != listener){
			listener.afterDrop(tag, result);
		}
		return result;
	}

	/**
	 * 修改标签
	 * @param tag 标签
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	private boolean alter(Table table, Tag tag, boolean trigger) throws Exception{

		boolean result = true;
		long fr = System.currentTimeMillis();
		String random = null;
		check(tag);
		List<String> sqls = SQLAdapterUtil.getAdapter(getJdbc()).buildAlterRunSQL(tag);

		random = random();
		DDListener listener = tag.getListener();
		try{
			for(String sql:sqls) {
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[txt:\n{}\n]", random, sql);
				}
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeAlter(tag);
				}
				if (exe) {
					getJdbc().update(sql);
					result = true;
				}
			}
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			log.warn("{}[{}][exception:{}]",random, LogUtil.format("修改tag执行异常", 33), e.getMessage());
			if(trigger && null != listener && !BasicUtil.equalsIgnoreCase(tag.getTypeName(), tag.getUpdate().getTypeName())) {
				boolean exe = false;
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					exe = listener.afterAlterColumnException(table, tag, e);
				}
				log.warn("{}[修改tag执行异常][尝试修正数据][修正结果:{}]", random, exe);
				if (exe) {
					result = alter(table, tag, false);
				}
			}else{
				log.warn("{}[修改tag执行异常][中断执行]",random);
				result = false;
				throw e;
			}
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[update tag][table:{}][tag:{}][qty:{}][result:{}][执行耗时:{}ms]"
					, random, tag.getTableName(), tag.getName(), sqls.size(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Index index) throws Exception;
	 * public boolean alter(Index index) throws Exception;
	 * public boolean drop(Index index) throws Exception;
	 ******************************************************************************************************************/
	@Override
	public boolean add(Index index) throws Exception {
		return false;
	}

	@Override
	public boolean alter(Index index) throws Exception {
		return false;
	}

	@Override
	public boolean drop(Index index) throws Exception {
		return false;
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Constraint constraint) throws Exception;
	 * public boolean alter(Constraint constraint) throws Exception;
	 * public boolean drop(Constraint constraint) throws Exception;
	 ******************************************************************************************************************/
	@Override
	public boolean add(Constraint constraint) throws Exception {
		return false;
	}

	@Override
	public boolean alter(Constraint constraint) throws Exception {
		return false;
	}

	@Override
	public boolean drop(Constraint constraint) throws Exception {
		return false;
	}

	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 * -----------------------------------------------------------------------------------------------------------------
	 * protected String paramLogFormat(List<?> params)
	 * protected String paramLogFormat(List<?> keys, List<?> values)
	 * private static String random()
	 ******************************************************************************************************************/



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

	private static String random(){
		StringBuilder builder = new StringBuilder();
		builder.append("[SQL:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
				.append("][thread:")
				.append(Thread.currentThread().getId()).append("][ds:").append(DataSourceHolder.getDataSource()).append("]");
		return builder.toString();
	}
}
