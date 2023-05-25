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
 *
 *
 */


package org.anyline.dao.init.springjdbc;

import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.PersistenceAdapter;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.entity.*;
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.jdbc.ds.RuntimeHolder;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.metadata.persistence.ManyToMany;
import org.anyline.data.metadata.persistence.OneToMany;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.Procedure;
import org.anyline.data.prepare.ProcedureParam;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.run.Run;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
@Primary
@Repository("anyline.dao")
public class DefaultDao<E> implements AnylineDao<E> {
	protected static final Logger log = LoggerFactory.getLogger(DefaultDao.class);


	protected static DMListener listener;

	protected static boolean isBatchInsertRun = false;

	//默认环境,如果没有值则根据当前线程动态获取
	//用于ServiceProxy中生成多个service/dao/jdbc
	protected JDBCRuntime runtime = null;

	protected JDBCRuntime runtime(){
		if(null != runtime){
			//固定数据源
			return runtime;
		}
		//可切换数据源
		return RuntimeHolder.getRuntime();
	}


	/**
	 * 是否固定数据源
	 * @return boolean
	 */
	public boolean isFix(){
		return false;
	}
	public DMListener getListener() {
		return listener;
	}

	@Autowired(required=false)
	public void setListener(DMListener listener) {
		DefaultDao.listener = listener;
	}

	public JDBCRuntime getRuntime() {
		return runtime;
	}


	public void setRuntime(JDBCRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public void setDatasource(String datasource) {

	}

	/* *****************************************************************************************************************
	 *
	 * 													DML
	 *
	 ******************************************************************************************************************/

	/**
	 * 查询<br/>
	 * 注意:如果设置了自动还原,maps会自动还原数据源(dao内部执行过程中不要调用除非是一些重载),而protected maps不会
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return maps
	 */

	@Override
	public List<Map<String,Object>> maps(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return maps(true, prepare, configs, conditions);
	}

	protected List<Map<String,Object>> maps(boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		List<Map<String,Object>> maps = null;
		try {

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeBuildQuery(prepare, configs, conditions);
			}
			if(!exe){
				return new ArrayList<>();
			}
			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();
			/*if(null != queryInterceptor){
				int exe = queryInterceptor.before(adapter, prepare, configs, conditions);
				if(exe == -1){
					return new ArrayList<>();
				}
			}*/
			Run run = adapter.buildQueryRun(prepare, configs, conditions);

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().getKey() + "]";
				log.warn(tmp);
			}
			if (run.isValid()) {
				if (null != listener) {
					listener.beforeQuery(run, -1);
				}
				Long fr = System.currentTimeMillis();
				maps = maps(runtime, run.getFinalQuery(), run.getValues());
				if (null != adapter) {
					maps = adapter.process(maps);
				}
				if (null != listener) {
					listener.afterQuery(run, maps, System.currentTimeMillis() - fr);
				}
				/*if(null != queryInterceptor){
					queryInterceptor.after(run, maps, System.currentTimeMillis() - fr);
				}*/

			} else {
				maps = new ArrayList<>();
			}
		}finally {
			// 自动切换回切换前的数据源  runtime有值时表示固定数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return maps;
	}
	public List<Map<String,Object>> maps(RunPrepare prepare, String ... conditions){
		return maps(prepare, null, conditions);
	}

	/**
	 * 查询<br/>
	 * 注意:如果设置了自动还原,querys会自动还原数据源(dao内部执行过程中不要调用除非是一些重载),而select不会
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return DataSet
	 */
	@Override
	public DataSet querys(RunPrepare prepare, ConfigStore configs, String ... conditions) {
		return querys(true, prepare, configs, conditions);
	}
	protected DataSet querys(boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		DataSet set = null;
		try {
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeBuildQuery(prepare, configs, conditions);
			}
			if(!exe){
				return new DataSet();
			}
			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();
			/*
			if(null != queryInterceptor){
				int exe = queryInterceptor.before(adapter, prepare, configs, conditions);
				if(exe == -1){
					return new DataSet();
				}
			}*/
			Run run = adapter.buildQueryRun(prepare, configs, conditions);

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().getKey() + "]";
				log.warn(tmp);
			}
			PageNavi navi = run.getPageNavi();
			int total = 0;
			if (run.isValid()) {
				if (null != navi) {
					if (null != listener) {
						listener.beforeTotal(run);
					}
					Long fr = System.currentTimeMillis();
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
					if (null != listener) {
						listener.afterTotal(run, total, System.currentTimeMillis() - fr);
					}
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("[查询记录总数][行数:{}]", total);
				}
			}
			if (run.isValid()) {
				Long fr = System.currentTimeMillis();
				if(null == navi || total > 0){
					if(null != listener){
						listener.beforeQuery(run, total);
					}
					set = select(runtime, prepare.getTable(), run.getFinalQuery(), run.getValues());
					if(null != listener){
						listener.afterQuery(run, set, System.currentTimeMillis() - fr);
					}
				}else{
					set = new DataSet();
				}/*
				if(null != queryInterceptor){
					queryInterceptor.after(run, set, System.currentTimeMillis() - fr);
				}*/
			} else {
				set = new DataSet();
			}

			set.setDataSource(prepare.getDataSource());
			set.setNavi(navi);
			if (null != navi && navi.isLazy()) {
				PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
			}
		}finally {
			// 自动还原数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return set;
	}

	@Override
	public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String... conditions) {
		return querys(true, clazz, configs, conditions);
	}
	protected  <T> EntitySet<T> querys(boolean recover , Class<T> clazz, ConfigStore configs, String... conditions) {
		RunPrepare prepare = new DefaultTablePrepare();
		return querys(recover, prepare, clazz, configs, conditions);
	}
	/**
	 * 查询<br/>
	 * 注意:如果设置了自动还原,querys会自动还原数据源(dao内部执行过程中不要调用除非是一些重载),而select不会
	 * @param prepare RunPrepare
	 * @param configs 查询条件
	 * @param conditions 查询条件
	 * @return DataSet
	 */
	@Override
	public <T> EntitySet<T> querys(RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) {
		return querys(true, prepare, clazz, configs, conditions);
	}
	protected <T> EntitySet<T> querys(boolean recover, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) {
		EntitySet<T> list = null;
		try {

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeBuildQuery(prepare, configs, conditions);
			}
			if(!exe){
				return new EntitySet();
			}
			if(BasicUtil.isEmpty(prepare.getDataSource())) {
				if (EntityAdapterProxy.hasAdapter()) {
					prepare.setDataSource(EntityAdapterProxy.table(clazz).getName());
				}
			}

			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();
			/*
			if(null != queryInterceptor){
				int exe = queryInterceptor.before(adapter, prepare, configs, conditions);
				if(exe == -1){
					return new EntitySet<>();
				}
			}*/

			Run run = adapter.buildQueryRun(prepare, configs, conditions);
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().getKey() + "]";
				log.warn(tmp);
			}
			PageNavi navi = run.getPageNavi();
			int total = 0;
			if (run.isValid()) {
				if (null != navi) {
					if (null != listener) {
						listener.beforeTotal(run);
					}
					Long fr = System.currentTimeMillis();
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
					if (null != listener) {
						listener.afterTotal(run, total, System.currentTimeMillis() - fr);
					}
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("[查询记录总数][行数:{}]", total);
				}

			}
			if (run.isValid()) {
				Long fr = System.currentTimeMillis();
				if((null == navi || total > 0)) {
					if (null != listener) {
						listener.beforeQuery(run, total);
					}
					fr = System.currentTimeMillis();
					list = select(runtime, clazz, run.getTable(), run.getFinalQuery(), run.getValues(), ThreadConfig.check(DataSourceHolder.curDataSource()).ENTITY_FIELD_SELECT_DEPENDENCY());
					if (null != listener) {
						listener.afterQuery(run, list, System.currentTimeMillis() - fr);

					}
				}else{
					list = new EntitySet<>();
				}/*
				if(null != queryInterceptor){
					queryInterceptor.after(run, list, System.currentTimeMillis() - fr);
				}*/
			} else {
				list = new EntitySet<>();
			}
			list.setNavi(navi);
			if (null != navi && navi.isLazy()) {
				PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
			}
		}finally {
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
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

	public DataRow sequence(boolean next, String ... names){
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		String sql = adapter.buildQuerySequence(next, names);
		DataSet set = select(runtime, "",  sql, null);
		if(set.size()>0) {
			return set.getRow(0);
		}else{
			return new DataRow();
		}
	}

	public int count(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return count(true, prepare, configs, conditions);
	}
	protected int count(boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		int count = -1;
		try{
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeBuildQuery(prepare, configs, conditions);
			}
			if(!exe){
				return -1;
			}
			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();/*
			if(null != queryInterceptor){
				int exe = queryInterceptor.before(adapter, prepare, configs, conditions);
				if(exe == -1){
					return count;
				}
			}*/
			Run run = adapter.buildQueryRun(prepare, configs, conditions);
			Long fr = System.currentTimeMillis();
			if (null != listener) {
				listener.beforeCount(run);
			}
			fr = System.currentTimeMillis();
			count = getTotal(run.getTotalQuery(), run.getValues());

			if(null != listener){
				listener.afterCount(run, count, System.currentTimeMillis() - fr);
			}/*
			if(null != queryInterceptor){
				queryInterceptor.after(run, count, System.currentTimeMillis() - fr);
			}*/
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return count;
	}
	public int count(RunPrepare prepare, String ... conditions){
		return count(prepare, null, conditions);
	}

	public boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return exists(true, prepare, configs, conditions);
	}
	protected boolean exists(boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		boolean result = false;
		try {

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeBuildQuery(prepare, configs, conditions);
			}
			if(!exe){
				return false;
			}

			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();
			Run run = adapter.buildQueryRun(prepare, configs, conditions);
			String txt = run.getFinalExists();
			List<Object> values = run.getValues();

			long fr = System.currentTimeMillis();
			String random = "";
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]\n[param:{}]", random, txt, paramLogFormat(values));
			}
			/*执行SQL*/
			try {
				if(null != listener){
					listener.beforeExists(run);
				}
				Map<String, Object> map = null;
				if(null != values && values.size()>0 && BasicUtil.isEmpty(true, values)){
					//>0:有占位 isEmpty:值为空
				}else{
					if (null != values && values.size() > 0) {
						map = runtime.getTemplate().queryForMap(txt, values.toArray());
					} else {
						map = runtime.getTemplate().queryForMap(txt);
					}
					if (null == map) {
						result = false;
					} else {
						result = BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
					}
				}
				Long millis = System.currentTimeMillis() - fr;
				if(null != listener){
					listener.afterExists(run, result, millis);
				}
				boolean slow = false;
				long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
				if(SLOW_SQL_MILLIS > 0){
					if(millis > SLOW_SQL_MILLIS){
						slow = true;
						log.warn("{}[SLOW SQL][action:exists][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, txt, paramLogFormat(values));
						if(null != listener){
							listener.slow("exists", run, txt, values, null, millis);
						}
					}
				}
				if (!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(result, 34));
				}

			} catch (Exception e) {
				if (ConfigTable.IS_SHOW_SQL_WHEN_ERROR) {
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), prepare,  paramLogFormat(values));
				}
				if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
					throw e;
				}
			}
		}finally {
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
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
		JDBCRuntime runtime = runtime();
		DataSet set = select(runtime, (String)null, sql,values);
		total = set.getInt(0,"CNT",0);
		return total;
	}
	/**
	 * 更新记录
	 * @param data		需要更新的数据
	 * @param dest		需要更新的表,如果没有提供则根据data解析
	 * @param columns	需要更新的列 如果没有提供则解析data解析
	 * @param configs	更新条件 如果没提供则根据data主键
	 * @return int 影响行数
	 */
	@Override
	public int update(String dest, Object data, ConfigStore configs, List<String> columns){
		return update(true, dest, data, configs, columns);
	}
	protected int update(boolean recover, String dest, Object data, ConfigStore configs, List<String> columns){
		dest = DataSourceHolder.parseDataSource(dest, data);
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeBuildUpdate(dest, data, configs, false, columns);
		}
		if(!exe){
			return -1;
		}
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
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildUpdateRun(dest, data, configs,false, columns);
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
			log.warn("{}[sql:\n{}\n]\n[param:{}]", random, sql, paramLogFormat(run.getUpdateColumns(), values));
		}
		/*执行SQL*/
		try{
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeUpdate(run, dest, data, columns);
			}
			if(listenerResult) {
				result = runtime.getTemplate().update(sql, values.toArray());
				checkMany2ManyDependencySave(runtime, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
				checkOne2ManyDependencySave(runtime, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
				Long millis = System.currentTimeMillis() - fr;
				if (null != listener) {
					listener.afterUpdate(run, result, dest, data, columns, millis);
				}
				boolean slow = false;
				long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
				if(SLOW_SQL_MILLIS > 0){
					if(millis > SLOW_SQL_MILLIS){
						slow = true;
						log.warn("{}[SLOW SQL][action:update][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, paramLogFormat(values));
						if(null != listener){
							listener.slow("update", run, sql, values, null, millis);
						}
					}
				}
				if (!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(result, 34));
				}


			}
		}catch(Exception e){
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("更新异常:", 33)+e.toString(), sql, paramLogFormat(run.getUpdateColumns(),values));
				}
				e.printStackTrace();
			}
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
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
				return -1;
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
		boolean isNew = checkIsNew(data);
		if(isNew){
			return insert(dest, data, checkPrimary, columns);
		}else{
			//是否覆盖(null:不检测直接执行update有可能影响行数=0)
			Boolean override = checkOverride(data);
			boolean exe = true;
			if(null != override){
				RunPrepare prepare = new DefaultTablePrepare(dest);
				Map<String, Object> pvs = checkPv(data);
				ConfigStore stores = new DefaultConfigStore();
				for(String k:pvs.keySet()){
					stores.and(k, pvs.get(k));
				}
				boolean exists = exists(prepare, stores);
				if(exists){
					if(override){
						return update(dest, data, columns);
					}else{
						log.warn("[跳过更新][数据已存在:{}({})]",dest, BeanUtil.map2json(pvs));
					}
				}else{
					return insert(dest, data, checkPrimary, columns);
				}
			}else{
				return update(dest, data, columns);
			}
		}
		return 0;
	}
	protected Boolean checkOverride(Object obj){
		Boolean result = null;
		if(null != obj && obj instanceof DataRow){
			result = ((DataRow)obj).getOverride();
		}
		return result;
	}
	protected Map<String,Object> checkPv(Object obj){
		Map<String,Object> pvs = new HashMap<>();
		if(null != obj && obj instanceof DataRow){
			DataRow row = (DataRow) obj;
			List<String> ks = row.getPrimaryKeys();
			for(String k:ks){
				pvs.put(k, row.get(k));
			}
		}
		return pvs;
	}
	protected boolean checkIsNew(Object obj){
		if(null == obj){
			return false;
		}
		if(obj instanceof DataRow){
			DataRow row = (DataRow)obj;
			return row.isNew();
		}else{
			if(EntityAdapterProxy.hasAdapter()){
				Map<String,Object> values = EntityAdapterProxy.primaryValues(obj);
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
		return insert(true, dest, data, checkPrimary, columns);
	}
	protected int insert(boolean recover,String dest, Object data, boolean checkPrimary, List<String> columns) {
		dest = DataSourceHolder.parseDataSource(dest, data);
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeBuildInsert(dest, data, checkPrimary, columns);
		}
		if(!exe){
			return -1;
		}

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		if(null != data && data instanceof DataSet){
			DataSet set = (DataSet)data;
			Map<String,Object> tags = set.getTags();
			if(null != tags && tags.size()>0){
				LinkedHashMap<String,PartitionTable> ptables = ptables(false, new MasterTable(dest), tags);
				if(ptables.size() != 1){
					String msg = "分区表定位异常,主表:" + dest + ",标签:" + BeanUtil.map2json(tags) + ",分区表:" + BeanUtil.object2json(ptables.keySet());
					if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
						throw new SQLUpdateException(msg);
					}else{
						log.error(msg);
						return -1;
					}
				}
				dest = ptables.values().iterator().next().getName();
			}
		}
		Run run = adapter.buildInsertRun(runtime.getTemplate(), dest, data, checkPrimary, columns);

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
			log.warn("{}[sql:\n{}\n]\n[param:{}]", random, sql, paramLogFormat(run.getInsertColumns(),values));
		}
		try{
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeInsert(run, dest, data, checkPrimary, columns);
			}
			if(listenerResult) {
				cnt = adapter.insert(runtime.getTemplate(), random, data, sql, values, null);
				int ENTITY_FIELD_INSERT_DEPENDENCY = ThreadConfig.check(DataSourceHolder.curDataSource()).ENTITY_FIELD_INSERT_DEPENDENCY();
				checkMany2ManyDependencySave(runtime, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
				checkOne2ManyDependencySave(runtime, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
				Long millis = System.currentTimeMillis() - fr;
				if (null != listener) {
					listener.afterInsert(run, cnt, dest, data, checkPrimary, columns, millis);
				}

				boolean slow = false;
				long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
				if(SLOW_SQL_MILLIS > 0){
					if(millis > SLOW_SQL_MILLIS){
						slow = true;
						log.warn("{}[SLOW SQL][action:insert][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, paramLogFormat(values));
						if(null != listener){
							listener.slow("insert", run, sql, values, null, millis);
						}
					}
				}
				if (!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(cnt, 34));
				}

			}
		}catch(Exception e){
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("插入异常:", 33)+e.toString(), sql, paramLogFormat(run.getInsertColumns(),values));
				}
				e.printStackTrace();
			}
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return cnt;
	}

	/**
	 * 检测级联insert/update
	 * @param runtime runtime
	 * @param obj obj
	 * @param dependency dependency
	 * @param mode 0:inser 1:update
	 * @return int
	 */
	private int checkMany2ManyDependencySave(JDBCRuntime runtime, Object obj, int dependency, int mode){
		int result = 0;
		//ManyToMany
		if(dependency <= 0){
			return result;
		}
		if(obj instanceof DataSet || obj instanceof DataRow || obj instanceof Map){
			return result;
		}
		if(obj instanceof EntitySet){
			EntitySet set = (EntitySet) obj;
			for(Object entity:set){
				checkMany2ManyDependencySave(runtime, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			org.anyline.entity.data.Column pc = EntityAdapterProxy.primaryKey(clazz);
			String pk = null;
			if(null != pc){
				pk = pc.getName();
			}
			List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
			for(Field field:fields) {
				try {
					ManyToMany join = PersistenceAdapter.manyToMany(field);
					//INSERT INTO HR_DEPLOYEE_DEPARTMENT(EMPLOYEE_ID, DEPARTMENT_ID) VALUES();
					Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(obj);
					Object pv = primaryValueMap.get(pk.toUpperCase());
					Object fv = BeanUtil.getFieldValue(obj, field);
					if(null == fv){
						continue;
					}
					DataSet set = new DataSet();
					Collection fvs = new ArrayList();
					if (null == join.dependencyTable) {
						//只通过中间表查主键 List<Long> departmentIds
						if(fv.getClass().isArray()){
							fvs = BeanUtil.array2collection(fv);
						}else if(fv instanceof Collection){
							fvs = (Collection) fv;
						}
					} else {
						//通过子表完整查询 List<Department> departments
						org.anyline.entity.data.Column joinpc = EntityAdapterProxy.primaryKey(clazz);
						String joinpk = null;
						if(null != joinpc){
							joinpk = joinpc.getName();
						}
						if(fv.getClass().isArray()){
							Object[] objs = (Object[])fv;
							for(Object item:objs){
								fvs.add(EntityAdapterProxy.primaryValue(item).get(joinpk.toUpperCase()));
							}
						}else if(fv instanceof Collection){
							Collection objs = (Collection) fv;
							for(Object item:objs){
								fvs.add(EntityAdapterProxy.primaryValue(item).get(joinpk.toUpperCase()));
							}
						}
					}

					for(Object item:fvs){
						DataRow row = new DataRow();
						row.put(join.joinColumn, pv);
						row.put(join.inverseJoinColumn, item);
						set.add(row);
					}
					if(mode == 1) {
						deletes(false, join.joinTable, join.joinColumn, pv + "");
					}
					insert(false, join.joinTable, set);

				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		dependency --;
		return result;
	}

	private int checkOne2ManyDependencySave(JDBCRuntime runtime, Object obj, int dependency, int mode){
		int result = 0;
		//OneToMany
		if(dependency <= 0){
			return result;
		}
		if(obj instanceof DataSet || obj instanceof DataRow || obj instanceof Map){
			return result;
		}
		if(obj instanceof EntitySet){
			EntitySet set = (EntitySet) obj;
			for(Object entity:set){
				checkOne2ManyDependencySave(runtime, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			org.anyline.entity.data.Column pc = EntityAdapterProxy.primaryKey(clazz);
			String pk = null;
			if(null != pc){
				pk = pc.getName();
			}
			List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
			for(Field field:fields) {
				try {
					OneToMany join = PersistenceAdapter.oneToMany(field);
					Object pv = EntityAdapterProxy.primaryValue(obj).get(pk.toUpperCase());
					Object fv = BeanUtil.getFieldValue(obj, field);
					if(null == fv){
						continue;
					}

					if(null == join.joinField){
						throw new RuntimeException(field+"关联属性异常");
					}

					if(null == join.joinColumn){
						throw new RuntimeException(field+"关联列异常");
					}

					if(null == join.dependencyTable){
						throw new RuntimeException(field+"关联表异常");
					}
					if(mode == 1) {
						deletes(false, join.dependencyTable, join.joinColumn, pv + "");
					}
					Collection items = new ArrayList();
					if(fv.getClass().isArray()){
						Object[] objs = (Object[])fv;
						for(Object item:objs){
							BeanUtil.setFieldValue(item, join.joinField, pv);
							items.add(item);
						}
					}else if(fv instanceof Collection){
						Collection cols = (Collection) fv;
						for(Object item:cols){
							BeanUtil.setFieldValue(item, join.joinField, pv);
							items.add(item);
						}
					}
					insert(false, join.dependencyTable, items);

				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}
		dependency --;
		return result;
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
	protected int insert(boolean recover, String dest, Object data, String ... columns){
		return insert(recover, dest, data, false, BeanUtil.array2list(columns));
	}
	@Override
	public int insert(Object data, String ... columns){
		return insert(null, data, false, BeanUtil.array2list(columns));
	}

	/**
	 * 查询
	 * @param sql  sql
	 * @param values  values
	 * @return List
	 */
	protected List<Map<String,Object>> maps(JDBCRuntime runtime, String sql, List<Object> values){
		JDBCAdapter adapter = runtime.getAdapter();
		List<Map<String,Object>> maps = null;
		if(BasicUtil.isEmpty(sql)){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new ArrayList<>();
			}
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[sql:\n{}\n]\n[param:{}]", random, sql, paramLogFormat(values));
		}
		try{
			if(null != values && values.size()>0){
				maps = runtime.getTemplate().queryForList(sql, values.toArray());
			}else{
				maps = runtime.getTemplate().queryForList(sql);
			}
			long mid = System.currentTimeMillis();
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(mid-fr > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:select][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, mid-fr, sql, paramLogFormat(values));
					if(null != listener){
						listener.slow("select",null, sql, values, null, mid);
					}
				}
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("{}[执行耗时:{}ms]", random, mid - fr);
			}
			if(null != adapter){
				maps = adapter.process(maps);
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid, maps.size());
			}
		}catch(Exception e){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(), e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), sql, paramLogFormat(values));
				}
				e.printStackTrace();
			}
		}
		return maps;
	}

	/**
	 * 封装查询结果
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	protected static DataRow row(boolean system, JDBCRuntime runtime, LinkedHashMap<String, org.anyline.entity.data.Column> metadatas, ResultSet rs) {
		DataRow row = new DataRow();
		try {
			JDBCAdapter adapter = runtime.getAdapter();
			ResultSetMetaData rsmd = rs.getMetaData();
			int qty = rsmd.getColumnCount();
			if (!system && metadatas.isEmpty()) {
				for (int i = 1; i <= qty; i++) {
					String name = rsmd.getColumnName(i);
					if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
						continue;
					}
					org.anyline.entity.data.Column column = metadatas.get(name) ;
					column = adapter.column((Column) column, rsmd, i);
					metadatas.put(name.toUpperCase(), column);
				}
			}
			for (int i = 1; i <= qty; i++) {
				String name = rsmd.getColumnLabel(i);
				if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
					continue;
				}
				org.anyline.entity.data.Column column = metadatas.get(name.toUpperCase());
				//Object v = BeanUtil.value(column.getTypeName(), rs.getObject(name));
				Object value = adapter.read(column, rs.getObject(name), null);
				row.put(false, name, value);
			}
			row.setMetadatas(metadatas);
		}catch (Exception e){
			e.printStackTrace();
		}
		return row;
	}

	protected DataSet select(JDBCRuntime runtime, String table, String sql, List<Object> values){
		return select(false, runtime, table, sql, values);
	}

	/**
	 * 查询
	 * @param system 系统表不查询表结构
	 * @param runtime runtime
	 * @param table 查询表结构时使用
	 * @param sql sql
	 * @param values 参数
	 * @return DataSet
	 */
	protected DataSet select(boolean system, JDBCRuntime runtime, String table, String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new DataSet();
			}
		}
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[sql:\n{}\n]\n[param:{}]", random, sql, paramLogFormat(values));
		}
		DataSet set = new DataSet();
		//根据这一步中的JDBC结果集检测类型不准确,如:实际POINT 返回 GEOMETRY 如果要求准确 需要开启到自动检测
		//在DataRow中 如果检测到准确类型 JSON XML POINT 等 返回相应的类型,不返回byte[]（所以需要开启自动检测）
		//Entity中 JSON XML POINT 等根据属性类型返回相应的类型（所以不需要开启自动检测）
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();

		if(!system && ThreadConfig.check(DataSourceHolder.curDataSource()).IS_AUTO_CHECK_METADATA() && null != table){
			columns = CacheProxy.columns(table);
			if(null == columns){
				columns = columns(table);
				CacheProxy.columns(table, columns);
			}
		}
		try{
			final long[] mid = {System.currentTimeMillis()};
			final boolean[] process = {false};
			final LinkedHashMap<String, org.anyline.entity.data.Column> metadatas = new LinkedHashMap<>();
			metadatas.putAll(columns);
			set.setMetadatas(metadatas);
			if(null != values && values.size()>0){
				runtime.getTemplate().query(sql, values.toArray(), new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						if(!process[0]){
							mid[0] = System.currentTimeMillis();
						}
						DataRow row = row(system, runtime, metadatas, rs);
						set.add(row);
						process[0] = true;
					}
				});
			}else {
				runtime.getTemplate().query(sql, new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						if(!process[0]){
							mid[0] = System.currentTimeMillis();
						}
						DataRow row = row(system, runtime, metadatas, rs);
						set.add(row);
						process[0] = true;
					}
				});
			}
			if(!process[0]){
				mid[0] = System.currentTimeMillis();
			}
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				slow = true;
				if(mid[0] - fr > SLOW_SQL_MILLIS){
					log.warn("{}[SLOW SQL][action:select][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, mid[0] - fr, sql, paramLogFormat(values));
					if(null != listener){
						listener.slow("select", null, sql, values, null, mid[0] - fr);
					}
				}
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("{}[执行耗时:{}ms]", random, mid[0] - fr);
			}
			set.setDatalink(DataSourceHolder.curDataSource());
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], set.size());
			}
		}catch(Exception e){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), sql, paramLogFormat(values));
				}
				e.printStackTrace();
			}
		}
		return set;
	}

	/**
	 * 查询
	 * @param runtime runtime
	 * @param clazz entity class
	 * @param table table
	 * @param sql sql
	 * @param values value
	 * @param dependency 是否加载依赖 >0时加载
	 * @return EntitySet
	 * @param <T> entity.class
	 */
	protected <T> EntitySet<T> select(JDBCRuntime runtime, Class<T> clazz, String table,  String sql, List<Object> values, int dependency){
		EntitySet<T> set = new EntitySet<>();
		DataSet rows = select(runtime, table, sql, values);
		for(DataRow row:rows){
			T entity = null;
			if(EntityAdapterProxy.hasAdapter()){
				//jdbc adapter需要参与 或者metadata里添加colun type
				entity = EntityAdapterProxy.entity(clazz, row, null);
			}else{
				entity = row.entity(clazz);
			}
			set.add(entity);
		}
		//检测依赖关系
		if(dependency > 0) {
			checkMany2ManyDependencyQuery(runtime, set, dependency);
			checkOne2ManyDependencyQuery(runtime, set, dependency);
		}
		return set;
	}
	protected <T> void checkMany2ManyDependencyQuery(JDBCRuntime runtime, EntitySet<T> set, int dependency) {
		//ManyToMany
		if(set.size()==0 || dependency <= 0){
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		org.anyline.entity.data.Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		Compare compare = ThreadConfig.check(DataSourceHolder.curDataSource()).ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE();
		for(Field field:fields){
			try {
				ManyToMany join = PersistenceAdapter.manyToMany(field);
				if(Compare.EQUAL == compare || set.size() == 1) {
					//逐行查询
					for (T entity : set) {
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						if (null == join.dependencyTable) {
							//只通过中间表查主键 List<Long> departmentIds
							//SELECT * FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
							DataSet items = querys(false, new DefaultTablePrepare(join.joinTable), new DefaultConfigStore(), "++" + join.joinColumn + ":" + primaryValueMap.get(pk.toUpperCase()));

							List<String> ids = items.getStrings(join.inverseJoinColumn);
							BeanUtil.setFieldValue(entity, field, ids);
						} else {
							//通过子表完整查询 List<Department> departments
							//SELECT * FROM HR_DEPARTMENT WHERE ID IN(SELECT DEPARTMENT_ID FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?)
							String sql = "SELECT * FROM " + join.dependencyTable + " WHERE " + join.dependencyPk + " IN (SELECT " + join.inverseJoinColumn + " FROM " + join.joinTable + " WHERE " + join.joinColumn + "=?" + ")";
							List<Object> params = new ArrayList<>();
							params.add(primaryValueMap.get(pk.toUpperCase()));
							EntitySet<T> dependencys = select(runtime, join.itemClass, null, sql, params, dependency);
							BeanUtil.setFieldValue(entity, field, dependencys);
						}
					}
				}else if(Compare.IN == compare){
					//查出所有相关 再逐行分配
					List pvs = new ArrayList();
					Map<T,Object> idmap = new HashMap<>();
					for(T entity:set){
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						Object pv = primaryValueMap.get(pk.toUpperCase());
						pvs.add(pv);
						idmap.put(entity, pv);
					}
					if (null == join.dependencyTable) {
						//只通过中间表查主键 List<Long> departmentIds
						//SELECT * FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID IN(?,?,?)
						ConfigStore conditions = new DefaultConfigStore();
						conditions.and(join.joinColumn, pvs);
						DataSet allItems = querys(false, new DefaultTablePrepare(join.joinTable), conditions);
						for(T entity:set){
							DataSet items = allItems.getRows(join.joinColumn, idmap.get(entity)+"");
							List<String> ids = items.getStrings(join.inverseJoinColumn);
							BeanUtil.setFieldValue(entity, field, ids);
						}
					} else {
						//通过子表完整查询 List<Department> departments
						//SELECT M.*, F.EMPLOYEE_ID FROM hr_department AS M RIGHT JOIN hr_employee_department AS F ON M.ID = F.DEPARTMENT_ID WHERE F.EMPLOYEE_ID IN (1,2)
						ConfigStore conditions = new DefaultConfigStore();
						conditions.param("JOIN_PVS", pvs);
						String sql = "SELECT M.*, F."+join.joinColumn+" FK_"+join.joinColumn+" FROM " + join.dependencyTable + " M RIGHT JOIN "+join.joinTable+" F ON M." + join.dependencyPk + " = "+join.inverseJoinColumn +" WHERE "+join.joinColumn+" IN(#{JOIN_PVS})";
						DataSet alls = querys(false, new DefaultTextPrepare(sql), conditions);
						for(T entity:set){
							DataSet items = alls.getRows("FK_"+join.joinColumn, idmap.get(entity)+"");
							BeanUtil.setFieldValue(entity, field, items.entity(join.itemClass));
						}
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}

	protected <T> void checkOne2ManyDependencyQuery(JDBCRuntime runtime, EntitySet<T> set, int dependency) {
		//OneToMany
		if(set.size()==0 || dependency <= 0){
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		org.anyline.entity.data.Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		Compare compare = ThreadConfig.check(DataSourceHolder.curDataSource()).ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE();
		for(Field field:fields){
			try {
				OneToMany join = PersistenceAdapter.oneToMany(field);
				if(Compare.EQUAL == compare || set.size() == 1) {
					//逐行查询
					for (T entity : set) {

						Object pv = EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase());
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						//通过子表完整查询 List<AttendanceRecord> records
						//SELECT * FROM HR_ATTENDANCE_RECORD WHERE EMPLOYEE_ID = ?)
						List<Object> params = new ArrayList<>();
						params.add(primaryValueMap.get(pk.toUpperCase()));
						EntitySet<T> dependencys = querys(false, join.dependencyClass, new DefaultConfigStore().and(join.joinColumn, pv));
						BeanUtil.setFieldValue(entity, field, dependencys);

					}
				}else if(Compare.IN == compare){
					//查出所有相关 再逐行分配
					List pvs = new ArrayList();
					Map<T,Object> idmap = new HashMap<>();
					for(T entity:set){
						Map<String, Object> primaryValueMap = EntityAdapterProxy.primaryValue(entity);
						Object pv = primaryValueMap.get(pk.toUpperCase());
						pvs.add(pv);
						idmap.put(entity, pv);
					}
					//通过子表完整查询 List<Department> departments
					//SELECT M.*, F.EMPLOYEE_ID FROM hr_department AS M RIGHT JOIN hr_employee_department AS F ON M.ID = F.DEPARTMENT_ID WHERE F.EMPLOYEE_ID IN (1,2)
					ConfigStore conditions = new DefaultConfigStore();
					conditions.and(join.joinColumn, pvs);
					EntitySet<T> alls = querys(false, join.dependencyClass, conditions);
					for(T entity:set){
						EntitySet items = alls.gets(join.joinField, idmap.get(entity));
						BeanUtil.setFieldValue(entity, field, items);
					}

				}
			}catch (Exception e){
				e.printStackTrace();
			}

		}
	}
	@Override
	public int execute(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return execute(true, prepare, configs, conditions);
	}
	protected int execute(boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		int result = -1;

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildExecuteRun(prepare, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().getKey() + "]");
			}
			return -1;
		}
		String txt = run.getFinalExecute();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[sql:\n{}\n]\n[param:{}]", random, txt, paramLogFormat(values));
		}
		try{

			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeExecute(run);
			}
			if(listenerResult) {
				if (null != values && values.size() > 0) {
					result = runtime.getTemplate().update(txt, values.toArray());
				} else {
					result = runtime.getTemplate().update(txt);
				}
				Long millis = System.currentTimeMillis() - fr;
				boolean slow = false;
				long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
				if(SLOW_SQL_MILLIS > 0){
					if(millis > SLOW_SQL_MILLIS){
						slow = true;
						log.warn("{}[SLOW SQL][action:execute][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, txt, paramLogFormat(values));
						if(null != listener){
							listener.slow("execute", run, txt, values, null, millis);
						}
					}
				}
				if (null != listener) {
					listener.afterExecute(run, result, millis);
				}
				if (!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(result, 34));
				}

			}
		}catch(Exception e){
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				throw e;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]" , random, LogUtil.format("SQL执行异常:", 33)+e.toString(),prepare, paramLogFormat(values));
				}
			}
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
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
		return execute(true, procedure);
	}
	protected boolean execute(boolean recover, Procedure procedure){
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
			log.warn("{}[sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, sql, paramLogFormat(inputs), paramLogFormat(outputs));
		}
		try{
			JDBCRuntime runtime = runtime();
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeExecute(procedure);
			}
			if(listenerResult) {
				list = (List<Object>) runtime.getTemplate().execute(sql, new CallableStatementCallback<Object>() {
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
				Long millis = System.currentTimeMillis() - fr;

				boolean slow = false;
				long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
				if(SLOW_SQL_MILLIS > 0){
					if(millis > SLOW_SQL_MILLIS){
						log.warn("{}[SLOW SQL][action:procedure][millis:{}ms][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, millis, sql, paramLogFormat(inputs), paramLogFormat(list));
						if(null != listener){
							listener.slow("procedure",null, sql, inputs, list, millis);
						}
					}
				}
				if (null != listener) {
					listener.afterExecute(procedure, result, millis);
				}
				if (!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms]\n[output param:{}]", random, millis, list);
				}
			}
		}catch(Exception e){
			result = false;
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("execute异常:"+e.toString(),e);
				ex.setSql(sql);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, LogUtil.format("存储过程执行异常:", 33)+e.toString(), sql, paramLogFormat(inputs), paramLogFormat(outputs));
				}
				e.printStackTrace();
			}
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}

	/**
	 * 根据存储过程查询(MSSQL AS 后必须加 SET NOCOUNT ON)<br/>
	 * @param procedure  procedure
	 * @param navi  navi
	 * @return DataSet
	 */
	@Override
	public DataSet querys(Procedure procedure, PageNavi navi){
		return querys(true, procedure, navi);
	}
	protected DataSet querys(boolean recover, Procedure procedure, PageNavi navi){
		final List<ProcedureParam> inputs = procedure.getInputs();
		final List<ProcedureParam> outputs = procedure.getOutputs();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[sql:\n{}\n][input param:{}]\n[output param:{}]", random, procedure.getName(), paramLogFormat(inputs), paramLogFormat(outputs));
		}
		final String rdm = random;
		DataSet set = null;
		try{
			JDBCRuntime runtime = runtime();
			/*if(null != queryInterceptor){
				int exe = queryInterceptor.before(procedure);
				if(exe == -1){
					return new DataSet();
				}
			}*/
			if(null != listener){
				listener.beforeQuery(procedure);
			}
			set = (DataSet) runtime.getTemplate().execute(new CallableStatementCreator(){
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
						set.addHead(rsmd.getColumnLabel(i));
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
								row.put(rsmd.getColumnLabel(i), rs.getObject(i));
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

					set.setDatalink(DataSourceHolder.curDataSource());
					if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
						log.warn("{}[封装耗时:{}ms][封装行数:{}]", rdm, System.currentTimeMillis() - mid,set.size());
					}
					return set;
				}
			});
			Long millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[SLOW SQL][action:procedure][millis:{}ms][sql:\n{}\n][input param:{}]\n[output param:{}]"
							, random
							, millis
							, procedure.getName()
							, paramLogFormat(inputs)
							, paramLogFormat(outputs));
					if(null != listener){
						listener.slow("procedure", null, procedure.getName(), inputs, outputs, millis);
					}
				}
			}
			if(null != listener){
				listener.afterQuery(procedure, set, millis);
			}
/*			if(null != queryInterceptor){
				queryInterceptor.after(procedure, set, millis);
			}*/
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("{}[执行耗时:{}ms]", random, millis);
			}
		}catch(Exception e){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(),e);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[input param:{}]\n[output param:{}]"
							, random
							, LogUtil.format("存储过程查询异常:", 33)+e.toString()
							, procedure.getName()
							, paramLogFormat(inputs)
							, paramLogFormat(outputs));
				}
				e.printStackTrace();
			}
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return set;
	}

	public int deletes(String table, String key, Collection<Object> values){
		table = DataSourceHolder.parseDataSource(table, null);
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildDeleteRun(table, key, values);
		int result = exeDelete(true, runtime, run);
		return result;
	}

	public int deletes(String table, String key, String ... values){
		return deletes(true, table, key, values);
	}
	protected int deletes(boolean recover, String table, String key, String ... values){
		table = DataSourceHolder.parseDataSource(table, null);
		List<String> list = new ArrayList<>();
		if(null != values){
			for(String value:values){
				list.add(value);
			}
		}

		boolean exe = true;
		if(null != listener){
			exe = listener.beforeBuildDelete(table, key, list);
		}
		if(!exe){
			return -1;
		}
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildDeleteRun(table, key, list);
		int result = exeDelete(recover, runtime, run);
		return result;
	}
	@Override
	public int delete(String dest, Object obj, String... columns) {
		dest = DataSourceHolder.parseDataSource(dest,obj);
		int size = 0;
		if(null != obj){
			if(obj instanceof Collection){
				Collection list = (Collection) obj;
				for(Object item:list){
					int qty = delete(dest, item, columns);
					//如果不执行会返回-1
					if(qty > 0){
						size += qty;
					}
				}
				if(log.isWarnEnabled()) {
					log.warn("[delete Collection][影响行数:{}]", LogUtil.format(size, 34));
				}
			}else{
				boolean exe = true;
				if(null != listener){
					exe = listener.beforeBuildDelete(dest, obj, columns);
				}
				if(!exe){
					return -1;
				}
				JDBCRuntime runtime = runtime();
				JDBCAdapter adapter = runtime.getAdapter();
				Run run = adapter.buildDeleteRun(dest, obj, columns);
				size = exeDelete(true, runtime, run);
				if(size > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0){
					if(!(obj instanceof DataRow)){
						checkMany2ManyDependencyDelete(runtime, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
						checkOne2ManyDependencyDelete(runtime, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
					}

				}
			}
		}
		return size;
	}
	private int checkMany2ManyDependencyDelete(JDBCRuntime runtime, Object entity, int dependency){
		int result = 0;
		//ManyToMany
		if(dependency <= 0){
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		org.anyline.entity.data.Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		for(Field field:fields) {
			try {
				ManyToMany join = PersistenceAdapter.manyToMany(field);
				//DELETE FROM HR_DEPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
				deletes(false, join.joinTable, join.joinColumn, EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase())+"");

			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return result;
	}
	private int checkOne2ManyDependencyDelete(JDBCRuntime runtime, Object entity, int dependency){
		int result = 0;
		//OneToMany
		if(dependency <= 0){
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		org.anyline.entity.data.Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		for(Field field:fields) {
			try {
				OneToMany join = PersistenceAdapter.oneToMany(field);
				//DELETE FROM HR_DEPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?
				deletes(false, join.dependencyTable, join.joinColumn, EntityAdapterProxy.primaryValue(entity).get(pk.toUpperCase())+"");

			}catch (Exception e){
				e.printStackTrace();
			}
		}
		return result;
	}
	@Override
	public int delete(String table, ConfigStore configs, String... conditions) {
		table = DataSourceHolder.parseDataSource(table, null);
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeBuildDelete(table, configs, conditions);
		}
		if(!exe){
			return -1;
		}
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildDeleteRun(table, configs, conditions);
		int result = exeDelete(true, runtime, run);
		return result;
	}

	/**
	 * 执行删除
	 * @param recover 执行完成后是否根据设置自动还原数据源
	 * @param runtime JDBCRuntime
	 * @param run Run
	 * @return int
	 */
	protected int exeDelete(boolean recover, JDBCRuntime runtime, Run run){
		int result = 0;
		final String sql = run.getFinalDelete();
		final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		String random = "";
		if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
			random = random();
			log.warn("{}[sql:\n{}\n]\n[param:{}]", random, sql, paramLogFormat(values));
		}
		try{
			boolean listenerResult = true;
			if(null != listener){
				listenerResult = listener.beforeDelete(run);
			}
			if(listenerResult) {
				if(null == values) {
					result = runtime.getTemplate().update(sql);
				}else{
					result = runtime.getTemplate().update(sql, values.toArray());
				}
				Long millis = System.currentTimeMillis() - fr;
				boolean slow = false;
				long SLOW_SQL_MILLIS = ThreadConfig.check(DataSourceHolder.curDataSource()).SLOW_SQL_MILLIS();
				if(SLOW_SQL_MILLIS > 0){
					slow = true;
					if(millis > SLOW_SQL_MILLIS){
						log.warn("{}[SLOW SQL][action:delete][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, paramLogFormat(values));
						if(null != listener){
							listener.slow("delete", run, sql, values, null, millis);
						}
					}
				}
				if(null != listener){
					listener.afterDelete(run, result, millis);
				}
				if (!slow && ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(result, 34));
				}
				// result = 1;
			}
		}catch(Exception e){
			result = 0;
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("delete异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("删除异常:", 33)+e.toString(), sql, paramLogFormat(values));
				}
				e.printStackTrace();
			}
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && DataSourceHolder.isAutoRecover()){
				DataSourceHolder.recoverDataSource();
			}
		}
		return result;
	}


	@Override
	public int truncate(String table){
		table = DataSourceHolder.parseDataSource(table);
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		String sql = adapter.buildTruncateSQL(table);
		RunPrepare prepare = new DefaultTextPrepare(sql);;
		return execute(prepare);
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
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryDatabaseRunSQL();
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
							databases = adapter.databases(idx++, true, databases, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
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

	/**
	 * tables
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
	 * @param pattern 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
	 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, Table> tables(boolean greedy, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String,Table> tables = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			Table search = new Table();
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					adapter.checkSchema(con, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			search.setName(pattern);
			search.setCatalog(catalog);
			search.setSchema(schema);

			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}

			DataRow table_map = CacheProxy.getTableMaps(DataSourceHolder.curDataSource()+"_"+types);
			if(null != pattern){
				if(table_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败,先查询全部表,生成缓存,再从缓存中不区分大小写查询
					LinkedHashMap<String,Table> all = tables(greedy, catalog, schema, null, null);
					if(!greedy) {
						for (Table table : all.values()) {
							if ((catalog + "_" + schema).equals(table.getCatalog() + "_" + table.getSchema())) {
								table_map.put(table.getName(greedy).toUpperCase(), table.getName(greedy));
							}
						}
					}
				}
				if(table_map.containsKey(search.getName(greedy).toUpperCase())){
					pattern = table_map.getString(search.getName(greedy).toUpperCase());
				}else{
					pattern = search.getName(greedy);
				}
			}
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryTableRunSQL(catalog, schema, pattern, types);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
							tables = adapter.tables(idx++, true, catalog, schema, tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}

			// 根据系统表查询失败后根据jdbc接口补充
			if(null == tables || tables.size() == 0) {
				try {
					LinkedHashMap<String, Table> jdbcTables = adapter.tables(true, null, con.getMetaData(), catalog, schema, pattern, tps);
					for (String key : jdbcTables.keySet()) {
						if (!tables.containsKey(key.toUpperCase())) {
							Table item = jdbcTables.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									tables.put(key.toUpperCase(), item);
								}
							}
						}
					}
				} catch (Exception e) {
					log.warn("{}[tables][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据jdbc接口补充失败", 33), catalog, schema, pattern, e.toString());
				}
			}
			//表备注
			try{
				List<String> sqls = adapter.buildQueryTableCommentRunSQL(catalog, schema, null, types);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
							tables = adapter.comments(idx++, true, catalog, schema, tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, tables.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(pattern)){
				LinkedHashMap<String,Table> tmps = new LinkedHashMap<>();
				List<String> keys = BeanUtil.getMapKeys(tables);
				for(String key:keys){
					Table item = tables.get(key);
					String name = item.getName(greedy);
					if(RegularUtil.match(name, pattern)){
						tmps.put(name.toUpperCase(), item);
					}
				}
				tables = tmps;
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
	public LinkedHashMap<String,Table> tables(boolean greedy, String schema, String name, String types){
		return tables(greedy, null, schema, name, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(boolean greedy, String name, String types){
		return tables(greedy, null, null, name, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(boolean greedy, String types){
		return tables(greedy, null, null, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(boolean greedy){
		return tables(greedy, null, null, null, "TABLE");
	}

	public LinkedHashMap<String, Table> tables(String catalog, String schema, String pattern, String types){
		return tables(false, catalog, schema, pattern, types);
	}

	@Override
	public LinkedHashMap<String,Table> tables(String schema, String name, String types){
		return tables(false, null, schema, name, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(String name, String types){
		return tables(false, null, null, name, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(String types){
		return tables(false, null, null, types);
	}
	@Override
	public LinkedHashMap<String,Table> tables(){
		return tables(false, null, null, null, "TABLE");
	}


	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, View> views(String catalog, String schema, String name, String types)
	 * public LinkedHashMap<String, View> views(String schema, String name, String types)
	 * public LinkedHashMap<String, View> views(String name, String types)
	 * public LinkedHashMap<String, View> views(String types)
	 * public LinkedHashMap<String, View> views()
	 ******************************************************************************************************************/

	/**
	 * views
	 * @param greedy 贪婪模式 true:如果不填写catalog或schema则查询全部 false:只在当前catalog和schema中查询
	 * @param catalog 对于MySQL,则对应相应的数据库,对于Oracle来说,则是对应相应的数据库实例,可以不填,也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
	 * @param schema 可以理解为数据库的登录名,而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意,其登陆名必须是大写,不然的话是无法获取到相应的数据,而MySQL则不做强制要求。
	 * @param pattern 一般情况下如果要获取所有的表的话,可以直接设置为null,如果设置为特定的表名称,则返回该表的具体信息。
	 * @param types 以逗号分隔  "TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"
	 * @return List
	 */
	@Override
	public LinkedHashMap<String, View> views(boolean greedy, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String,View> views = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			View search = new View();
			if(null == catalog || null == schema){
				View tmp = new View();
				if(!greedy) {
					adapter.checkSchema(con, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			search.setName(pattern);
			search.setCatalog(catalog);
			search.setSchema(schema);

			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}else{
				tps = new String[]{"VIEW"};
			}

			DataRow view_map = CacheProxy.getViewMaps(DataSourceHolder.curDataSource()+"");
			if(null != pattern){
				if(view_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败,先查询全部表,生成缓存,再从缓存中不区分大小写查询
					LinkedHashMap<String,View> all = views(greedy, catalog, schema, null, types);
					if(!greedy) {
						for (View view : all.values()) {
							if ((catalog + "_" + schema).equals(view.getCatalog() + "_" + view.getSchema())) {
								view_map.put(view.getName(greedy).toUpperCase(), view.getName(greedy));
							}
						}
					}
				}
				if(view_map.containsKey(search.getName(greedy).toUpperCase())){
					pattern = view_map.getString(search.getName(greedy).toUpperCase());
				}else{
					pattern = search.getName(greedy);
				}
			}
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryViewRunSQL(catalog, schema, pattern, types);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
							views = adapter.views(idx++, true, catalog, schema, views, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}

			// 根据jdbc接口补充
			try {
				LinkedHashMap<String,View> tmps = adapter.views(true, null, con.getMetaData(), catalog, schema, pattern, tps);
				for(String key:tmps.keySet()){
					if(!views.containsKey(key.toUpperCase())) {
						View item = tmps.get(key);
						if (null != item) {
							if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
								views.put(key.toUpperCase(), item);
							}
						}
					}
				}
			}catch (Exception e){
				log.warn("{}[views][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据jdbc接口补充失败", 33), catalog, schema, pattern, e.toString());
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[views][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, views.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(pattern)){
				LinkedHashMap<String,View> tmps = new LinkedHashMap<>();
				List<String> keys = BeanUtil.getMapKeys(views);
				for(String key:keys){
					View item = views.get(key);
					String name = item.getName(greedy);
					if(RegularUtil.match(name, pattern)){
						tmps.put(name.toUpperCase(), item);
					}
				}
				views = tmps;
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return views;
	}

	@Override
	public LinkedHashMap<String,View> views(boolean greedy, String schema, String name, String types){
		return views(greedy, null, schema, name, types);
	}
	@Override
	public LinkedHashMap<String,View> views(boolean greedy, String name, String types){
		return views(greedy, null, null, name, types);
	}
	@Override
	public LinkedHashMap<String,View> views(boolean greedy, String types){
		return views(greedy, null, null, types);
	}
	@Override
	public LinkedHashMap<String,View> views(boolean greedy){
		return views(greedy, null, null, null, "TABLE");
	}

	public LinkedHashMap<String, View> views(String catalog, String schema, String pattern, String types){
		return views(false, catalog, schema, pattern, types);
	}

	@Override
	public LinkedHashMap<String,View> views(String schema, String name, String types){
		return views(false, null, schema, name, types);
	}
	@Override
	public LinkedHashMap<String,View> views(String name, String types){
		return views(false, null, null, name, types);
	}
	@Override
	public LinkedHashMap<String,View> views(String types){
		return views(false, null, null, types);
	}
	@Override
	public LinkedHashMap<String,View> views(){
		return views(false, null, null, null, "TABLE");
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
	public LinkedHashMap<String, MasterTable> mtables(boolean greedy, String catalog, String schema, String pattern, String types) {

		LinkedHashMap<String, MasterTable> tables = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;
		
			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					adapter.checkSchema(con, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}
			DataRow table_map = CacheProxy.getTableMaps(DataSourceHolder.curDataSource()+"_"+types);
			if(null != pattern){
				if(table_map.isEmpty()){
					// 如果是根据表名查询、大小写有可能造成查询失败,先查询全部表,生成缓存,再从缓存中不区分大小写查询
					LinkedHashMap<String, MasterTable> all = mtables(catalog, schema, null, types);
					for(Table table:all.values()){
						table_map.put(table.getName().toUpperCase(), table.getName());
					}
				}
				if(table_map.containsKey(pattern.toUpperCase())){
					pattern = table_map.getString(pattern.toUpperCase());
				}
			}

			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryMasterTableRunSQL(catalog, schema, pattern, types);
				if(null != sqls) {
					int idx = 0;
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
							tables = adapter.mtables(idx++, true, catalog, schema, tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}

			// 根据jdbc接口补充
			try {
				LinkedHashMap<String,MasterTable> tmps = adapter.mtables(true, null, con.getMetaData(), catalog, schema, pattern, tps);
				for(String key:tmps.keySet()){
					if(!tables.containsKey(key.toUpperCase())) {
						MasterTable item = tmps.get(key);
						if (null != item) {
							if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
								tables.put(key.toUpperCase(), item);
							}
						}
					}
				}
			}catch (Exception e){
				log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据jdbc接口补充失败", 33), catalog, schema, pattern, e.toString());
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
	public LinkedHashMap<String, MasterTable> mtables(boolean greedy, String schema, String name, String types) {
		return mtables(greedy, null, schema, name, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean greedy, String name, String types) {
		return mtables(greedy, null, null, name, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean greedy, String types) {
		return mtables(greedy, null, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(boolean greedy) {
		return mtables(greedy, "STABLE");
	}

	public LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String pattern, String types){
		return mtables(false, catalog, schema, pattern, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types) {
		return mtables(false, null, schema, name, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(String name, String types) {
		return mtables(false, null, null, name, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables(String types) {
		return mtables(false, null, types);
	}

	@Override
	public LinkedHashMap<String, MasterTable> mtables() {
		return mtables(false, "STABLE");
	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String master, String name)
	 * public LinkedHashMap<String, PartitionTable> ptables(String schema, String master, String name)
	 * public LinkedHashMap<String, PartitionTable> ptables(String master, String name)
	 * public LinkedHashMap<String, PartitionTable> ptables(String master)
	 * public LinkedHashMap<String, PartitionTable> ptables(MasterTable table)
	 ******************************************************************************************************************/

	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean greedy, String catalog, String schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return ptables(greedy,mtable, null, name);
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean greedy, String schema, String master, String name){
		return ptables(greedy,null, schema, master, name);
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean greedy, String master, String name){
		return ptables(greedy,null, null, master, name);
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(boolean greedy, String master){
		return ptables(greedy,null, null, master, null);
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(boolean greedy, MasterTable master){
		return ptables(greedy,master, null);
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(boolean greedy, MasterTable master, Map<String, Object> tags){
		return ptables(greedy,master, tags, null);
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(boolean greedy, MasterTable master, Map<String, Object> tags, String name){
		LinkedHashMap<String,PartitionTable> tables = new LinkedHashMap<>();
		DataSource ds = null;
		Connection con = null;
		
			JDBCRuntime runtime = runtime();
			JDBCAdapter adapter = runtime.getAdapter();
		String random = random();
		try{
			long fr = System.currentTimeMillis();
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			// 根据系统表查询
			try{
				List<String> sqls = adapter.buildQueryPartitionTableRunSQL(master, tags, name);
				if(null != sqls) {
					int idx = 0;
					int total = sqls.size();
					for(String sql:sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
							tables = adapter.ptables(total, idx++, true, master, master.getCatalog(), master.getSchema(), tables, set);
						}
					}
				}
			}catch (Exception e){
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][stable:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), master.getName(), e.toString());
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

	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return ptables(false,mtable, null, name);
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String schema, String master, String name){
		return ptables(false,null, schema, master, name);
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String master, String name){
		return ptables(false,null, null, master, name);
	}
	@Override
	public LinkedHashMap<String, PartitionTable> ptables(String master){
		return ptables(false,null, null, master, null);
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(MasterTable master){
		return ptables(false,master, null);
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(MasterTable master, Map<String, Object> tags){
		return ptables(false,master, tags, null);
	}
	@Override
	public LinkedHashMap<String,PartitionTable> ptables(MasterTable master, Map<String, Object> tags, String name){
		return ptables(false, master, tags, name);
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Column> columns(Table table)
	 * public LinkedHashMap<String, Column> columns(String table)
	 * public LinkedHashMap<String, Column> columns(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, Column> columns(boolean greedy, Table table){
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
		if(null == table || BasicUtil.isEmpty(table.getName())){
			return columns;
		}
		long fr = System.currentTimeMillis();
		DataSource ds = null;
		Connection con = null;
		String random = null;
		DatabaseMetaData metadata = null;
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			random = random();
		}

		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		}
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		try {
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			metadata = con.getMetaData();;
		}catch (Exception e){}
		int qty_dialect  = 0 ; //优先根据系统表查询
		int qty_metadata = 0 ; //再根据metadata解析
		int qty_jdbc	 = 0 ; //根据jdbc接口补充
		// 优先根据系统表查询
		try{
			List<String> sqls = adapter.buildQueryColumnRunSQL(table, false);
			if(null != sqls){
				int idx = 0;
				for(String sql:sqls){
					if(BasicUtil.isNotEmpty(sql)) {
						DataSet set = select(true, runtime, (String)null, sql, null);
						columns = adapter.columns(idx, true, table, columns, set);
					}
					idx ++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
			}
		}
		qty_dialect = columns.size();
		// 再根据metadata解析 SELECT * FROM T WHERE 1=0
		if(columns.size() == 0) {
			try {
				List<String> sqls = adapter.buildQueryColumnRunSQL(table, true);
				if (null != sqls) {
					for (String sql : sqls) {
						if (BasicUtil.isNotEmpty(sql)) {
							SqlRowSet set = runtime.getTemplate().queryForRowSet(sql);
							columns = adapter.columns(true, columns, table, set);
						}
					}
				}
			} catch (Exception e) {
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.toString());
				}
			}
			qty_metadata = columns.size() - qty_dialect;
		}
		// 根据jdbc接口补充

		if(columns.size() == 0) {
			try {
				columns = adapter.columns(true, columns, metadata, table, null);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (!DataSourceUtils.isConnectionTransactional(con, ds)) {
					DataSourceUtils.releaseConnection(con, ds);
				}
			}
			qty_jdbc = columns.size() - qty_metadata - qty_dialect;
		}
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据jdbc接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, columns.size(), qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
		}
		return columns;
	}
	@Override
	public LinkedHashMap<String,Column> columns(boolean greedy, String table){
		return columns(greedy, null, null, table);
	}
	@Override
	public LinkedHashMap<String,Column>  columns(boolean greedy, String catalog, String schema, String table){
		Table tab = new Table(catalog, schema, table);
		return columns(greedy, tab);
	}
	@Override
	public LinkedHashMap<String, Column> columns(Table table){
		return columns(false, table);
	}
	@Override
	public LinkedHashMap<String,Column> columns(String table){
		return columns(false, null, null, table);
	}
	@Override
	public LinkedHashMap<String,Column>  columns(String catalog, String schema, String table){
		Table tab = new Table(catalog, schema, table);
		return columns(false, tab);
	}
	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Tag> tags(Table table)
	 * public LinkedHashMap<String, Tag> tags(String table)
	 * public LinkedHashMap<String, Tag> tags(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, Tag> tags(boolean greedy, Table table) {
		LinkedHashMap<String,Tag> tags = new LinkedHashMap<>();

		if(null == table || BasicUtil.isEmpty(table.getName())){
			return tags;
		}
		long fr = System.currentTimeMillis();
		DataSource ds = null;
		Connection con = null;
		String random = null;
		DatabaseMetaData metadata = null;
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			random = random();
		}

		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			checkSchema(runtime, table);
		}
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		try {
			ds = runtime.getTemplate().getDataSource();
			con = DataSourceUtils.getConnection(ds);
			metadata = con.getMetaData();
		}catch (Exception e){}

		// 先根据metadata解析 SELECT * FROM T WHERE 1=0
		try {
			List<String> sqls = adapter.buildQueryTagRunSQL(table , true);
			if(null != sqls){
				for(String sql:sqls) {
					if (BasicUtil.isNotEmpty(sql)) {
						SqlRowSet set = runtime.getTemplate().queryForRowSet(sql);
						tags = adapter.tags(true, table, tags, set);
					}
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tags][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.toString());
			}
		}

		// 再根据系统表查询
		try{
			List<String> sqls = adapter.buildQueryTagRunSQL(table, false);
			if(null != sqls){
				int idx = 0;
				for(String sql:sqls){
					if(BasicUtil.isNotEmpty(sql)) {
						DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
						tags = adapter.tags(idx, true, table, tags, set);
					}
					idx ++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[tags][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), catalog, schema, table, e.toString());
			}
		}

		// 根据jdbc接口补充
		try {
			// isAutoIncrement isGenerated remark default
			// 这一步会查出所有列(包括非tag列)
			tags = adapter.tags(false, tags, metadata, table, null);
		}catch (Exception e){
			e.printStackTrace();
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[tags][catalog:{}][schema:{}][table:{}][执行耗时:{}ms]", random, catalog, schema, table, System.currentTimeMillis() - fr);
		}
		return tags;
	}

	@Override
	public LinkedHashMap<String, Tag> tags(boolean greedy, String table) {
		Table tab = new Table();
		tab.setName(table);
		return tags(greedy, tab);
	}

	@Override
	public LinkedHashMap<String, Tag> tags(boolean greedy, String catalog, String schema, String table) {
		Table tab = new Table();
		tab.setCatalog(catalog);
		tab.setSchema(schema);
		tab.setName(table);
		return tags(greedy, tab);
	}
	@Override
	public LinkedHashMap<String, Tag> tags(Table table){
		return tags(false, table);
	}

	@Override
	public LinkedHashMap<String, Tag> tags(String table) {
		return tags(false, table);
	}

	@Override
	public LinkedHashMap<String, Tag> tags(String catalog, String schema, String table) {
		return tags(false, catalog, schema, table);
	}
	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * public PrimaryKey primary(Table table)
	 * public PrimaryKey primary(String table)
	 * public PrimaryKey primary(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表
	 * @return map
	 */
	@Override
	public PrimaryKey primary(boolean greedy, Table table){
		PrimaryKey primary = null;

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		}
		String tab = table.getName();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		DataSource ds = null;
		Connection con = null;
		String random = null;
		DatabaseMetaData metadata = null;
		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			random = random();
		}

		try{
			List<String> sqls = adapter.buildQueryPrimaryRunSQL(table);
			if(null != sqls){
				int idx = 0;
				for(String sql:sqls){
					if(BasicUtil.isNotEmpty(sql)) {
						DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
						primary = adapter.primary(idx, table, set);
						if(null != primary){
							primary.setTable(table);
						}
					}
					idx ++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[primary][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), catalog, schema, table, e.toString());
			}
		}
		return primary;
	}

	@Override
	public PrimaryKey primary(boolean greedy, String table) {
		Table tab = new Table();
		tab.setName(table);
		return primary(greedy,tab);
	}

	@Override
	public PrimaryKey primary(boolean greedy, String catalog, String schema, String table) {
		Table tab = new Table();
		tab.setCatalog(catalog);
		tab.setSchema(schema);
		tab.setName(table);
		return primary(greedy,tab);
	}
	@Override
	public PrimaryKey primary(Table table){
		return primary(false, table);
	}

	@Override
	public PrimaryKey primary(String table) {
		return primary(false, table);
	}

	@Override
	public PrimaryKey primary(String catalog, String schema, String table) {
		return primary(false, catalog, schema, table);
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Index> indexs(Table table, String name)
	 * public LinkedHashMap<String, Index> indexs(String table, String name)
	 * public LinkedHashMap<String, Index> indexs(Table table)
	 * public LinkedHashMap<String, Index> indexs(String table)
	 * public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表
	 * @param name name
	 * @return map
	 */
	@Override
	public LinkedHashMap<String, Index> indexs(boolean greedy, Table table, String name){
		LinkedHashMap<String,Index> indexs = null;
		if(null == table){
			table = new Table();
		}
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		}
		if(null != table.getName()) {
			DataSource ds = null;
			Connection con = null;
			try {
				ds = runtime.getTemplate().getDataSource();
				con = DataSourceUtils.getConnection(ds);
				indexs = adapter.indexs(true, indexs, con.getMetaData(), table, false, false);
				table.setIndexs(indexs);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (!DataSourceUtils.isConnectionTransactional(con, ds)) {
					DataSourceUtils.releaseConnection(con, ds);
				}
			}
			if(BasicUtil.isNotEmpty(name)){
				Index index = indexs.get(name.toUpperCase());
				indexs = new LinkedHashMap<>();
				indexs.put(name.toUpperCase(), index);
			}
		}
		List<String> sqls = adapter.buildQueryIndexRunSQL(table, name);

		if(null != sqls){
			int idx = 0;
			for(String sql:sqls){
				if(BasicUtil.isNotEmpty(sql)) {
					DataSet set = select(runtime, (String)null, sql, null).toUpperKey();
					try {
						indexs = adapter.indexs(idx, true, table, indexs, set);
					}catch (Exception e){
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return indexs;
	}

	@Override
	public LinkedHashMap<String, Index> indexs(boolean greedy, Table table){
		return indexs(greedy, table, null);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean greedy, String table, String name) {
		return indexs(greedy, new Table(table), name);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(boolean greedy, String table) {
		return indexs(greedy, new Table(table), null);
	}

	@Override
	public LinkedHashMap<String, Index> indexs(boolean greedy, String catalog, String schema, String table) {
		return indexs(greedy, new Table(catalog, schema, table), null);
	}

	@Override
	public LinkedHashMap<String, Index> indexs(Table table, String name){
		return indexs(false, table, name);
	}

	@Override
	public LinkedHashMap<String, Index> indexs(Table table){
		return indexs(false, table);
	}
	@Override
	public LinkedHashMap<String, Index> indexs(String table, String name) {
		return indexs(false, table, name);
	}
	@Override
	public LinkedHashMap<String, Index> indexs( String table) {
		return indexs(false, table);
	}

	@Override
	public LinkedHashMap<String, Index> indexs(String catalog, String schema, String table) {
		return indexs(false, catalog, schema, table);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public LinkedHashMap<String, Constraint> constraints(Table table, String name)
	 * public LinkedHashMap<String, Constraint> constraints(String table, String name)
	 * public LinkedHashMap<String, Constraint> constraints(Table table)
	 * public LinkedHashMap<String, Constraint> constraints(String table)
	 * public LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean greedy, Table table, String name) {
		return null;
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean greedy, Table table) {
		return constraints(greedy, table, null);
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean greedy, String table) {
		return constraints(greedy, new Table(table));
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean greedy, String table, String name) {
		return constraints(greedy, new Table(table), name);
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(boolean greedy, String catalog, String schema, String table) {
		return constraints(greedy, new Table(catalog, schema, table));
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(Table table, String name) {
		return constraints(false, table, name);
	}
	@Override
	public LinkedHashMap<String, Constraint> constraints(Table table) {
		return constraints(false, table);
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(String table) {
		return constraints(false, new Table(table));
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(String table, String name) {
		return constraints(false, new Table(table), name);
	}

	@Override
	public LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table) {
		return constraints(false, new Table(catalog, schema, table));
	}
	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * table			: 表
	 * view 			: 视图
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
	 * public boolean create(Table table) throws Exception
	 * public boolean alter(Table table) throws Exception
	 * public boolean drop(Table table) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Table table) throws Exception {
		boolean result = false;
		long fr = System.currentTimeMillis();

		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		List<String> sqls = adapter.buildCreateRunSQL(table);
		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeCreate(table);
		}
		if(exe) {
			for(String sql:sqls) {
				if (BasicUtil.isNotEmpty(sql)) {
					String random = null;
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						random = random();
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					runtime.getTemplate().update(sql);
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[create table][table:{}][执行耗时:{}ms]", random, table.getName(), System.currentTimeMillis() - fr);
					}
				}
			}
			result = true;
		}

		if(null != listener){
			listener.afterCreate(table, result);
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

		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		adapter.checkSchema(runtime.getTemplate().getDataSource(), update);
		//修改表备注
		String comment = update.getComment()+"";
		if(!comment.equals(table.getComment())){
			String sql = adapter.buildChangeCommentRunSQL(update);
			if(null != sql) {
				runtime.getTemplate().update(sql);
			}
		}
		if(!name.equalsIgnoreCase(uname)){
			// 修改表名
			String sql = adapter.buildRenameRunSQL(table);
			if(BasicUtil.isNotEmpty(sql)) {
				String random = null;
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					random = random();
					log.warn("{}[sql:\n{}\n]", random, sql);
				}

				DDListener listener = table.getListener();
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeRename(table);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}

				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[rename table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
				}
				if (null != listener) {
					listener.afterRename(table, result);
				}
			}
		}

		Map<String, Column> cols = new LinkedHashMap<>();

		// 更新列
		for (Column ucolumn : ucolumns.values()) {
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if (null != column) {
				// 修改列
				if (!column.equals(ucolumn)) {
					column.setTable(update);
					column.setUpdate(ucolumn);
					column.setService(table.getService());
					/*
					alter(column);
					result = true;*/
					column.setAction("alter");
					cols.put(column.getName().toUpperCase(), column);
				}
			} else {
				// 添加列
				ucolumn.setTable(update);
				/*
				add(ucolumn);
				result = true;*/
				ucolumn.setAction("add");
				cols.put(ucolumn.getName().toUpperCase(), ucolumn);
			}
		}
		List<String> deletes = new ArrayList<>();
		// 删除列(根据删除标记)
		for (Column column : ucolumns.values()) {
			if (column.isDelete()) {
				/*drop(column);*/
				deletes.add(column.getName().toUpperCase());
				column.setAction("drop");
				cols.put(column.getName().toUpperCase(), column);
			}
		}
		// 删除列(根据新旧对比)
		if (table.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if (column instanceof Tag) {
					continue;
				}
				if (column.isDelete() || deletes.contains(column.getName().toUpperCase()) || "drop".equals(column.getAction())) {
					//上一步已删除
					continue;
				}

				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					/*
					drop(column);
					result = true;*/
					column.setAction("drop");
					cols.put(column.getName().toUpperCase(), column);
				}
			}
		}
		List<String> alters = adapter.buildAlterRunSQL(table, cols.values());
		if(null != alters && alters.size()>0){
			//DDL合并
			for(String sql:alters){
				if(BasicUtil.isNotEmpty(sql)) {
					String random = null;
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						random = random();
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					DDListener listener = table.getListener();
					boolean exe = true;
					if (null != listener) {
						exe = listener.beforeDrop(table);
					}
					if (exe) {
						result = false;
						runtime.getTemplate().update(sql);
						result = true;
					}

					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[alter table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
					}

					if (null != listener) {
						listener.afterAlter(table, cols.values(), result);
					}
				}
			}
		}
		//主键
		PrimaryKey src_primary = primary(table);
		PrimaryKey cur_primary = update.getPrimaryKey();
		String src_define = "";
		String cur_define = "";
		if(null != src_primary){
			src_define= BeanUtil.concat(src_primary.getColumns().values(),"name", ",");
		}
		if(null != cur_primary){
			cur_define= BeanUtil.concat(cur_primary.getColumns().values(),"name", ",");
		}

		if(!cur_define.equalsIgnoreCase(src_define)){
			//删除主键
			if(null != src_primary){
				drop(src_primary);
			}
			//添加主键
			if(null != cur_primary) {
				add(cur_primary);
			}
		}
		CacheProxy.clearTableMaps(DataSourceHolder.curDataSource()+"");
		return result;
	}

	@Override
	public boolean drop(Table table) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		String sql = adapter.buildDropRunSQL(table);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = table.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(table);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				CacheProxy.clearTableMaps(DataSourceHolder.curDataSource() + "");
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(table, result);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean create(View view) throws Exception
	 * public boolean alter(View view) throws Exception
	 * public boolean drop(View view) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(View view) throws Exception {
		boolean result = false;
		long fr = System.currentTimeMillis();
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), view);
		List<String> sqls = adapter.buildCreateRunSQL(view);
		DDListener listener = view.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeCreate(view);
		}
		if(exe) {
			for(String sql:sqls) {
				if (BasicUtil.isNotEmpty(sql)) {
					String random = null;
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						random = random();
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					runtime.getTemplate().update(sql);
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[create view][view:{}][执行耗时:{}ms]", random, view.getName(), System.currentTimeMillis() - fr);
					}
				}
			}
			result = true;
		}

		if(null != listener){
			listener.afterCreate(view, result);
		}
		return result;
	}

	@Override
	public boolean alter(View view) throws Exception {
		boolean result = false;
		View update = (View)view.getUpdate();
		String name = view.getName();
		String uname = update.getName();

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), view);
		adapter.checkSchema(runtime.getTemplate().getDataSource(), update);
		if(!name.equalsIgnoreCase(uname)){
			String sql = adapter.buildRenameRunSQL(view);
			if(BasicUtil.isNotEmpty(sql)) {
				String random = null;
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					random = random();
					log.warn("{}[sql:\n{}\n]", random, sql);
				}

				DDListener listener = view.getListener();
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeRename(view);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}

				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[rename view][view:{}][result:{}][执行耗时:{}ms]", random, view.getName(), result, System.currentTimeMillis() - fr);
				}
				if (null != listener) {
					listener.afterRename(view, result);
				}
			}
			CacheProxy.clearViewMaps(DataSourceHolder.curDataSource()+"");
		}

		return result;
	}

	@Override
	public boolean drop(View view) throws Exception{
		boolean result = false;
		long fr = System.currentTimeMillis();

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), view);
		String sql = adapter.buildDropRunSQL(view);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = view.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(view);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				CacheProxy.clearViewMaps(DataSourceHolder.curDataSource() + "");
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop view][view:{}][result:{}][执行耗时:{}ms]", random, view.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(view, result);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean create(MasterTable table) throws Exception
	 * public boolean alter(MasterTable table) throws Exception
	 * public boolean drop(MasterTable table) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(MasterTable table) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		List<String> sqls = adapter.buildCreateRunSQL(table);
		String random = null;

		DDListener listener = table.getListener();
		boolean exe = true;
		if(null != listener){
			exe = listener.beforeDrop(table);
		}
		if(exe) {
			for(String sql:sqls) {
				if (BasicUtil.isNotEmpty(sql)) {
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						random = random();
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					runtime.getTemplate().update(sql);
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[create master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
					}
				}
			}
			result = true;
		}
		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}
	@Override
	public boolean alter(MasterTable table) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
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
			String sql = adapter.buildRenameRunSQL(table);
			if(BasicUtil.isNotEmpty(sql)) {
				String random = null;
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					random = random();
					log.warn("{}[sql:\n{}\n]", random, sql);
				}

				DDListener listener = table.getListener();
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeRename(table);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}

				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[rename master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
				}
				if (null != listener) {
					listener.afterRename(table, result);
				}
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
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
				result = true;
			}
		}
		// 删除列
		if(table.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
					result = true;
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
				result = true;
			}else{
				// 添加列
				utag.setTable(update);
				add(utag);
				result = true;
			}
		}
		// 删除标签
		if(table.isAutoDropColumn()) {
			for (Tag tag : tags.values()) {
				Tag utag = utags.get(tag.getName().toUpperCase());
				if (null == utag) {
					tag.setTable(update);
					drop(tag);
					result = true;
				}
			}
		}
		return result;
	}
	@Override
	public boolean drop(MasterTable table) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		String sql = adapter.buildDropRunSQL(table);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = table.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(table);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(table, result);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean create(PartitionTable table) throws Exception
	 * public boolean alter(PartitionTable table) throws Exception
	 * public boolean drop(PartitionTable table) throws Exception
	 ******************************************************************************************************************/

	@Override
	public boolean create(PartitionTable table) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);

		List<String> sqls = adapter.buildCreateRunSQL(table);

		DDListener listener = table.getListener();
		boolean exe = true;
		if (null != listener) {
			exe = listener.beforeDrop(table);
		}
		if (exe) {
			for(String sql:sqls) {
				if (BasicUtil.isNotEmpty(sql)) {
					String random = null;
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						random = random();
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					runtime.getTemplate().update(sql);
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[create partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
					}
				}
			}
			result = true;
		}

		if(null != listener){
			listener.afterDrop(table, result);
		}
		return result;
	}
	@Override
	public boolean alter(PartitionTable table) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			// 修改表名
			String sql = adapter.buildRenameRunSQL(table);
			if(BasicUtil.isNotEmpty(sql)) {
				String random = null;
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					random = random();
					log.warn("{}[sql:\n{}\n]", random, sql);
				}

				DDListener listener = table.getListener();
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeRename(table);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}

				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[rename partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
				}
				if (null != listener) {
					listener.afterRename(table, result);
				}
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
				result = true;
			}else{
				// 添加列
				ucolumn.setTable(update);
				add(ucolumn);
				result = true;
			}
		}
		// 删除列
		if(table.isAutoDropColumn()) {
			for (Column column : columns.values()) {
				if(column instanceof Tag){
					continue;
				}
				Column ucolumn = ucolumns.get(column.getName().toUpperCase());
				if (null == ucolumn) {
					column.setTable(update);
					drop(column);
					result = true;
				}
			}
		}
		return result;
	}
	@Override
	public boolean drop(PartitionTable table) throws Exception{

		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
		String sql = adapter.buildDropRunSQL(table);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = table.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(table);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(table, result);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Column column) throws Exception
	 * public boolean alter(Table table, Column column) throws Exception
	 * public boolean alter(Column column) throws Exception
	 * public boolean drop(Column column) throws Exception
	 *
	 * private boolean alter(Table table, Column column, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Column column) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		checkSchema(runtime, column);
		String random = null;

		List<String> sqls = adapter.buildAddRunSQL(column);
		DDListener listener = column.getListener();

		boolean exe = true;
		if(null != listener){
			exe = listener.beforeAdd(column);
		}
		for(String sql:sqls) {
			if(BasicUtil.isEmpty(sql)){
				continue;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}
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
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + column.getTableName());
				}else{
					log.error("表不存在:" + column.getTableName());
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, column, true);
	}
	@Override
	public boolean drop(Column column) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		checkSchema(runtime, column);
		String sql = adapter.buildDropRunSQL(column);

		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = column.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(column);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop column][table:{}][column:{}][result:{}][执行耗时:{}ms]", random, column.getTableName(), column.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(column, result);
			}
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
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, column);
		List<String> sqls = adapter.buildAlterRunSQL(column, false);

		random = random();
		DDListener listener = column.getListener();
		try{
			for(String sql:sqls) {
				if (BasicUtil.isNotEmpty(sql)) {
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					boolean exe = true;
					if (null != listener) {
						exe = listener.beforeAlter(column);
					}
					if (exe) {
						runtime.getTemplate().update(sql);
						result = true;
					}
				}
			}
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改Column执行异常", 33), e.toString());
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
				log.error("{}[修改Column执行异常][中断执行]", random);
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
	 * public boolean add(Tag tag) throws Exception
	 * public boolean alter(Table table, Tag tag) throws Exception
	 * public boolean alter(Tag tag) throws Exception
	 * public boolean drop(Tag tag) throws Exception
	 *
	 * private boolean alter(Table table, Tag tag, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Tag tag) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, tag);
		String sql = adapter.buildAddRunSQL(tag);
		if(BasicUtil.isNotEmpty(sql)) {
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = tag.getListener();

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeAdd(tag);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[add tag][table:{}][tag:{}][result:{}][执行耗时:{}ms]", random, tag.getTableName(), tag.getName(), result, System.currentTimeMillis() - fr);
			}
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
			LinkedHashMap<String,Table> tables = tables(false, tag.getCatalog(), tag.getSchema(), tag.getTableName(), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + tag.getTableName());
				}else {
					log.error("表不存在:" + tag.getTableName());
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, tag, true);
	}
	@Override
	public boolean drop(Tag tag) throws Exception{
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		checkSchema(runtime, tag);
		String sql = adapter.buildDropRunSQL(tag);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = tag.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(tag);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop tag][table:{}][tag:{}][result:{}][执行耗时:{}ms]", random, tag.getTableName(), tag.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(tag, result);
			}
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
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, tag);
		List<String> sqls = adapter.buildAlterRunSQL(tag);

		random = random();
		DDListener listener = tag.getListener();
		try{
			for(String sql:sqls) {
				if (BasicUtil.isNotEmpty(sql)) {
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[sql:\n{}\n]", random, sql);
					}
					boolean exe = true;
					if (null != listener) {
						exe = listener.beforeAlter(tag);
					}
					if (exe) {
						runtime.getTemplate().update(sql);
						result = true;
					}
				}
			}
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改tag执行异常", 33), e.toString());
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
				log.error("{}[修改tag执行异常][中断执行]", random);
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
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(PrimaryKey primary) throws Exception
	 * public boolean alter(PrimaryKey primary) throws Exception
	 * public boolean drop(PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(PrimaryKey primary) throws Exception {
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, primary);
		String sql = adapter.buildAddRunSQL(primary);
		if(BasicUtil.isNotEmpty(sql)) {
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = primary.getListener();

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeAdd(primary);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[add primary][table:{}][primary:{}][result:{}][执行耗时:{}ms]", random, primary.getTableName(), primary.getName(), result, System.currentTimeMillis() - fr);
			}
		}
		return result;
	}

	@Override
	public boolean alter(PrimaryKey primary) throws Exception {
		Table table = primary.getTable();
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, primary.getCatalog(), primary.getSchema(), primary.getTableName(), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + primary.getTableName());
				}else{
					log.error("表不存在:" + primary.getTableName());
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, primary);
	}
	@Override
	public boolean alter(Table table, PrimaryKey primary) throws Exception{
		boolean result = true;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, primary);
		List<String> sqls = adapter.buildAlterRunSQL(primary);

		random = random();
		DDListener listener = primary.getListener();
		for(String sql:sqls) {
			if (BasicUtil.isNotEmpty(sql)) {
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[sql:\n{}\n]", random, sql);
				}
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeAlter(primary);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}
			}
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[update primary][table:{}][primary:{}][qty:{}][result:{}][执行耗时:{}ms]"
					, random, primary.getTableName(), primary.getName(), sqls.size(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(PrimaryKey primary) throws Exception {
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		checkSchema(runtime, primary);
		String sql = adapter.buildDropRunSQL(primary);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = primary.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(primary);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop index][table:{}][index:{}][result:{}][执行耗时:{}ms]", random, primary.getTableName(), primary.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(primary, result);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Index index) throws Exception
	 * public boolean alter(Index index) throws Exception
	 * public boolean drop(Index index) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Index index) throws Exception {
		boolean result = false;

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, index);
		String sql = adapter.buildAddRunSQL(index);
		if(BasicUtil.isNotEmpty(sql)) {
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = index.getListener();

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeAdd(index);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[add index][table:{}][index:{}][result:{}][执行耗时:{}ms]", random, index.getTableName(), index.getName(), result, System.currentTimeMillis() - fr);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Index index) throws Exception {
		Table table = index.getTable();
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, index.getCatalog(), index.getSchema(), index.getTableName(), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + index.getTableName());
				}else{
					log.error("表不存在:" + index.getTableName());
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, index);
	}
	@Override
	public boolean alter(Table table, Index index) throws Exception{
		boolean result = true;

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, index);
		List<String> sqls = adapter.buildAlterRunSQL(index);

		random = random();
		DDListener listener = index.getListener();
		for(String sql:sqls) {
			if (BasicUtil.isNotEmpty(sql)) {
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[sql:\n{}\n]", random, sql);
				}
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeAlter(index);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}
			}
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[update index][table:{}][index:{}][qty:{}][result:{}][执行耗时:{}ms]"
					, random, index.getTableName(), index.getName(), sqls.size(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(Index index) throws Exception {
		boolean result = false;

		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		checkSchema(runtime, index);
		String sql = adapter.buildDropRunSQL(index);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = index.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(index);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop index][table:{}][index:{}][result:{}][执行耗时:{}ms]", random, index.getTableName(), index.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(index, result);
			}
		}
		return result;
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * public boolean add(Constraint constraint) throws Exception
	 * public boolean alter(Constraint constraint) throws Exception
	 * public boolean drop(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Constraint constraint) throws Exception {
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, constraint);
		String sql = adapter.buildAddRunSQL(constraint);
		if(BasicUtil.isNotEmpty(sql)) {
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = constraint.getListener();

			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeAdd(constraint);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[add constraint][table:{}][constraint:{}][result:{}][执行耗时:{}ms]", random, constraint.getTableName(), constraint.getName(), result, System.currentTimeMillis() - fr);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Constraint constraint) throws Exception {
		Table table = constraint.getTable();
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, constraint.getCatalog(), constraint.getSchema(), constraint.getTableName(), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + constraint.getTableName());
				}else{
					log.error("表不存在:" + constraint.getTableName());
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, constraint);
	}
	@Override
	public boolean alter(Table table, Constraint constraint) throws Exception{
		boolean result = true;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		String random = null;
		checkSchema(runtime, constraint);
		List<String> sqls = adapter.buildAlterRunSQL(constraint);

		random = random();
		DDListener listener = constraint.getListener();
		for(String sql:sqls) {
			if (BasicUtil.isNotEmpty(sql)) {
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[sql:\n{}\n]", random, sql);
				}
				boolean exe = true;
				if (null != listener) {
					exe = listener.beforeAlter(constraint);
				}
				if (exe) {
					runtime.getTemplate().update(sql);
					result = true;
				}
			}
		}

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
			log.warn("{}[update constraint][table:{}][constraint:{}][qty:{}][result:{}][执行耗时:{}ms]"
					, random, constraint.getTableName(), constraint.getName(), sqls.size(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(Constraint constraint) throws Exception {
		boolean result = false;
		
		JDBCRuntime runtime = runtime();
		JDBCAdapter adapter = runtime.getAdapter();
		long fr = System.currentTimeMillis();
		checkSchema(runtime, constraint);
		String sql = adapter.buildDropRunSQL(constraint);
		if(BasicUtil.isNotEmpty(sql)) {
			String random = null;
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				random = random();
				log.warn("{}[sql:\n{}\n]", random, sql);
			}
			DDListener listener = constraint.getListener();
			boolean exe = true;
			if (null != listener) {
				exe = listener.beforeDrop(constraint);
			}
			if (exe) {
				runtime.getTemplate().update(sql);
				result = true;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[drop constraint][table:{}][constraint:{}][result:{}][执行耗时:{}ms]", random, constraint.getTableName(), constraint.getName(), result, System.currentTimeMillis() - fr);
			}
			if (null != listener) {
				listener.afterDrop(constraint, result);
			}
		}
		return result;
	}

	public void checkSchema(JDBCRuntime runtime, Table table){
		if(null != table){
			JDBCAdapter adapter = runtime.getAdapter();
			adapter.checkSchema(runtime.getTemplate().getDataSource(), table);
			table.setCatalog(table.getCatalog());
			table.setSchema(table.getSchema());
		}
	}
	public void checkSchema(JDBCRuntime runtime, Column column){
		Table table = column.getTable();
		if(null != table){
			checkSchema(runtime, table);
			column.setCatalog(table.getCatalog());
			column.setSchema(table.getSchema());
		}
	}
	public void checkSchema(JDBCRuntime runtime, Index index){
		Table table = index.getTable();
		if(null != table){
			checkSchema(runtime, table);
			index.setCatalog(table.getCatalog());
			index.setSchema(table.getSchema());
		}
	}
	public void checkSchema(JDBCRuntime runtime, Constraint constraint){
		Table table = constraint.getTable();
		if(null != table){
			checkSchema(runtime, table);
			constraint.setCatalog(table.getCatalog());
			constraint.setSchema(table.getSchema());
		}
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
		if(null != params && params.size() > 0){
			builder.append("\n");
			int idx = 0;
			for(Object param:params){
				builder.append("param").append(idx++).append("=");
				builder.append(param);
				if(null != param){
					builder.append("(").append(ClassUtil.type(param)).append(")");
				}
				builder.append("\n");
			}
		}
		return builder.toString();
	}
	protected String paramLogFormat(List<?> keys, List<?> values) {
		StringBuilder builder = new StringBuilder();
		if (null != keys && null != values && keys.size() > 0) {
			builder.append("\n");
			if(keys.size() == values.size()) {
				int size = keys.size();
				for (int i = 0; i < size; i++) {
					Object key = keys.get(i);
					Object value = values.get(i);
					builder.append(keys.get(i)).append("=");
					builder.append(value);
					if (null != value) {
						builder.append("(").append(ClassUtil.type(value)).append(")");
					}
					builder.append("\n");
				}
			}else{
				return paramLogFormat(values);
			}
		}else if(null != values){
			builder.append(BeanUtil.concat(values, true)).append("\n");
		}
		return builder.toString();

	}

	private String random(){
		StringBuilder builder = new StringBuilder();
		builder.append("[SQL:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
				.append("][thread:")
				.append(Thread.currentThread().getId()).append("][ds:").append(runtime().getKey()).append("]");
		return builder.toString();
	}

}
