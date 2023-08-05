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

import org.anyline.adapter.PersistenceAdapter;
import org.anyline.dao.AnylineDao;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.param.ConfigParser;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.TablePrepare;
import org.anyline.data.prepare.auto.TextPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.prepare.auto.init.DefaultTextPrepare;
import org.anyline.data.prepare.xml.XMLPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.run.SimpleRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.data.util.ClientHolder;
import org.anyline.data.util.DataSourceUtil;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.*;
import org.anyline.exception.AnylineException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.ACTION.DDL;
import org.anyline.metadata.ACTION.SWITCH;
import org.anyline.metadata.persistence.ManyToMany;
import org.anyline.metadata.persistence.OneToMany;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

@Primary
@Repository("anyline.dao")
public class DefaultDao<E> implements AnylineDao<E> {
	protected static final Logger log = LoggerFactory.getLogger(DefaultDao.class);


	@Autowired(required = false)
	protected static DMListener dmListener;
	@Autowired(required = false)
	protected static DDListener ddListener;

	protected static boolean isBatchInsertRun = false;

	//默认环境,如果没有值则根据当前线程动态获取
	//用于ServiceProxy中生成多个service/dao/jdbc
	protected DataRuntime runtime = null;

	protected DataRuntime runtime(){
		if(null != runtime){
			//固定数据源
			runtime.setDao(this);
			return runtime;
		}
		//可切换数据源
		DataRuntime r = RuntimeHolder.getRuntime();
		if(null != r){
			r.setDao(this);
		}
		return r;
	}


	/**
	 * 是否固定数据源
	 * @return boolean
	 */
	public boolean isFix(){
		return false;
	}
	public DMListener getListener() {
		return dmListener;
	}

	@Autowired(required=false)
	public void setListener(DMListener listener) {
		DefaultDao.dmListener = listener;
	}

	public DataRuntime getRuntime() {
		return runtime;
	}


	public void setRuntime(DataRuntime runtime) {
		this.runtime = runtime;
	}

	@Override
	public void setDatasource(String datasource) {}

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
		SWITCH swt = SWITCH.CONTINUE;
		boolean sql_success = false;
		Run run = null;
		DataRuntime runtime = runtime();
		String random = random(runtime);

		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == SWITCH.BREAK){
			return new ArrayList<>();
		}

		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == SWITCH.BREAK){
			return new ArrayList<>();
		}

		DriverAdapter adapter = runtime.getAdapter();
		run = adapter.buildQueryRun(runtime, prepare, configs, conditions);
		Long fr = System.currentTimeMillis();
		try {
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().datasource() + "]";
				log.warn(tmp);
			}
			if (run.isValid()) {
				swt = InterceptorProxy.beforeQuery(runtime, random,  run, null);
				if(swt == SWITCH.BREAK){
					return new ArrayList<>();
				}
				if (null != dmListener) {
					dmListener.beforeQuery(runtime, random, run, -1);
				}
				maps = adapter.maps(runtime, random, run);
				if (null != adapter) {
					maps = adapter.process(runtime, maps);
				}
				sql_success = true;
			} else {
				maps = new ArrayList<>();
			}
		}finally {
			// 自动切换回切换前的数据源  runtime有值时表示固定数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, sql_success, maps, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, sql_success, maps, null,System.currentTimeMillis() - fr);
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
		return querys(null, null, true, prepare, configs, conditions);
	}
	protected DataSet querys(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions) {
		DataSet set = null;
		Long fr = System.currentTimeMillis();
		boolean sql_success = false;
		Run run = null;
		PageNavi navi = null;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		SWITCH swt = SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == SWITCH.BREAK){
			return new DataSet();
		}
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == SWITCH.BREAK){
			return new DataSet();
		}
		try {
			DriverAdapter adapter = runtime.getAdapter();
			run = adapter.buildQueryRun(runtime, prepare, configs, conditions);

			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().datasource() + "]";
				log.warn(tmp);
			}
			navi = run.getPageNavi();
			long total = 0;
			if (run.isValid()) {
				if (null != navi) {
					if (null != dmListener) {
						dmListener.beforeTotal(runtime, random, run);
					}
					fr = System.currentTimeMillis();
					if (navi.getLastRow() == 0) {
						// 第一条
						total = 1;
					} else {
						// 未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = total(runtime, random, run);
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
					if (null != dmListener) {
						dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
					}
					if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
						log.info("[查询记录总数][行数:{}]", total);
					}
				}
			}
			fr = System.currentTimeMillis();
			if (run.isValid()) {
				if(null == navi || total > 0){
					if(null != dmListener){
						dmListener.beforeQuery(runtime, random, run, total);
					}
					swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
					if(swt == SWITCH.BREAK){
						return new DataSet();
					}
					set = select(runtime, random, prepare.getTable(), run);
					sql_success = true;
				}else{
					set = new DataSet();
				}
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
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if(null != dmListener){
			dmListener.afterQuery(runtime, random, run, sql_success, set, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, sql_success, set, navi, System.currentTimeMillis() - fr);
		return set;
	}
	private long total(DataRuntime runtime, String random, Run run){
		long result = 0;
		DataSet set = select(runtime, random, (String)null, run);
		if(set.size()>0){
			result = set.getRow(0).getLong("CNT", 0);
		}
		return result;
	}
	@Override
	public <T> EntitySet<T> querys(Class<T> clazz, ConfigStore configs, String... conditions) {
		return querys(null, null, true, clazz, configs, conditions);
	}
	protected  <T> EntitySet<T> querys(DataRuntime runtime, String random, boolean recover , Class<T> clazz, ConfigStore configs, String... conditions) {
		RunPrepare prepare = new DefaultTablePrepare();
		return querys(runtime, random, recover, prepare, clazz, configs, conditions);
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
		return querys(null, null, true, prepare, clazz, configs, conditions);
	}
	protected <T> EntitySet<T> querys(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String... conditions) {
		EntitySet<T> list = null;
		Long fr = System.currentTimeMillis();
		Run run = null;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		boolean sql_success = false;
		PageNavi navi = null;

		SWITCH swt = SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == SWITCH.BREAK){
			return new EntitySet();
		}

		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == SWITCH.BREAK){
			return new EntitySet();
		}
		try {

			if(BasicUtil.isEmpty(prepare.getDataSource())) {
				//text xml格式的 不检测表名，避免一下步根据表名检测表结构
				if(prepare instanceof TextPrepare || prepare instanceof XMLPrepare){
				}else {
					prepare.setDataSource(EntityAdapterProxy.table(clazz, true));
				}
			}
			DriverAdapter adapter = runtime.getAdapter();


			run = adapter.buildQueryRun(runtime, prepare, configs, conditions);
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().datasource() + "]";
				log.warn(tmp);
			}
			navi = run.getPageNavi();
			long total = 0;
			if (run.isValid()) {
				if (null != navi) {
					if (null != dmListener) {
						dmListener.beforeTotal(runtime, random, run);
					}
					fr = System.currentTimeMillis();
					if (navi.getLastRow() == 0) {
						// 第一条
						total = 1;
					} else {
						// 未计数(总数 )
						if (navi.getTotalRow() == 0) {
							total = total(runtime, random, run);
							navi.setTotalRow(total);
						} else {
							total = navi.getTotalRow();
						}
					}
					if (null != dmListener) {
						dmListener.afterTotal(runtime, random, run, true, total, System.currentTimeMillis() - fr);
					}
				}
				if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
					log.info("[查询记录总数][行数:{}]", total);
				}

			}
			fr = System.currentTimeMillis();
			if (run.isValid()) {
				if((null == navi || total > 0)) {
					swt = InterceptorProxy.beforeQuery(runtime, random, run, navi);
					if(swt == SWITCH.BREAK){
						return new EntitySet();
					}
					if (null != dmListener) {
						dmListener.beforeQuery(runtime, random, run, total);
					}

					fr = System.currentTimeMillis();
					list = select(runtime, random, clazz, run.getTable(), run, ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_SELECT_DEPENDENCY());
					sql_success = false;
				}else{
					list = new EntitySet<>();
				}
			} else {
				list = new EntitySet<>();
			}
			list.setNavi(navi);
			if (null != navi && navi.isLazy()) {
				PageLazyStore.setTotal(navi.getLazyKey(), navi.getTotalRow());
			}
		}finally {
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, sql_success, list, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, sql_success, list, navi, System.currentTimeMillis() - fr);
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

	public DataRow sequence(boolean next, String ... names) {
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		List<Run> runs = adapter.buildQuerySequence(runtime, next, names);

		if (null != runs && runs.size() > 0) {
			Run run = runs.get(0);
			if(!run.isValid()){
				if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
					log.warn("[valid:false][不具备执行条件][sequence:"+names);
				}
				return new DataRow();
			}
			DataSet set = select(runtime, random, null, run);
			if (set.size() > 0) {
				return set.getRow(0);
			}
		}
		return new DataRow();
	}

	@Override
	public long count(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return count(null, null, true, prepare, configs, conditions);
	}
	protected long count(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		long count = -1;
		Long fr = System.currentTimeMillis();
		Run run = null;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}

		boolean sql_success = false;

		SWITCH swt = SWITCH.CONTINUE;
		swt = InterceptorProxy.prepareCount(runtime, random, prepare, configs, conditions);
		if(swt == SWITCH.BREAK){
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == SWITCH.BREAK){
			return -1;
		}
		DriverAdapter adapter = runtime.getAdapter();
		try{
			run = adapter.buildQueryRun(runtime, prepare, configs, conditions);
			if(!run.isValid()){
				if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
					log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().datasource() + "]");
				}
				return -1;
			}
			fr = System.currentTimeMillis();
			if (null != dmListener) {
				dmListener.beforeCount(runtime, random, run);
			}
			swt = InterceptorProxy.beforeCount(runtime, random, run);
			if(swt == SWITCH.BREAK){
				return -1;
			}
			fr = System.currentTimeMillis();
			count = total(runtime, random, run);
			sql_success = true;
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if(null != dmListener){
			dmListener.afterCount(runtime, random, run, sql_success, count, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterCount(runtime, random, run, sql_success, count, System.currentTimeMillis() - fr);
		return count;
	}
	public long count(RunPrepare prepare, String ... conditions){
		return count(prepare, null, conditions);
	}

	@Override
	public boolean exists(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return exists(null, null, true, prepare, configs, conditions);
	}
	protected boolean exists(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		boolean result = false;

		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		SWITCH swt = SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		try {

			DriverAdapter adapter = runtime.getAdapter();
			Run run = adapter.buildQueryRun(runtime, prepare, configs, conditions);
			if(!run.isValid()){
				if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
					log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().datasource() + "]");
				}
				return false;
			}

			/*执行SQL*/
				if(null != dmListener){
					dmListener.beforeExists(runtime, random, run);
				}
				long fr = System.currentTimeMillis();
				Map<String, Object> map = adapter.map(runtime, random, run);
				if (null == map) {
					result = false;
				} else {
					result = BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
				}
				Long millis = System.currentTimeMillis() - fr;
				if(null != dmListener){
					dmListener.afterExists(runtime, random, run, true, result, millis);
				}
		}finally {
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
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
/*	protected int getTotal(String sql, List<Object> values) {
		int total = 0;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DataSet set = select(runtime, random, (String)null, sql, values);
		total = set.getInt(0,"CNT",0);
		return total;
	}*/
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
		return update(null, null, true, dest, data, configs, columns);
	}
	protected int update(DataRuntime runtime, String random, boolean recover, String dest, Object data, ConfigStore configs, List<String> columns){
		dest = DataSourceUtil.parseDataSource(dest, data);
		SWITCH swt = SWITCH.CONTINUE;
		boolean sql_success = false;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareUpdate(runtime, random, dest, data, configs, columns);
		if(swt == SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareUpdate(runtime, random, dest, data, configs, false, columns);
		}
		if(swt == SWITCH.BREAK){
			return -1;
		}
		DriverAdapter adapter = runtime.getAdapter();
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
				result += update(runtime, random, false, dest, set.getRow(i), configs,  columns);
			}
			return result;
		}

		Run run = adapter.buildUpdateRun(runtime, dest, data, configs,false, columns);

		Table table = new Table(dest);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			table.setColumns(columns(runtime, random, false, table));
		}
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		//String sql = run.getFinalUpdate();
		/*if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]",dest);
			return -1;
		}
		List<Object> values = run.getValues();*/
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		/*if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}*/
		long millis = -1;
		try {
			swt = InterceptorProxy.beforeUpdate(runtime, random, run, dest, data, configs, columns);
			if (swt == SWITCH.BREAK) {
				return -1;
			}
			if (null != dmListener) {
				swt = dmListener.beforeUpdate(runtime, random, run, dest, data, columns);
			}
			if (swt == SWITCH.BREAK) {
				return -1;
			}
			result = adapter.update(runtime, random, dest, data, run);
			sql_success = true;
			checkMany2ManyDependencySave(runtime, random, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
			checkOne2ManyDependencySave(runtime, random, data, ConfigTable.ENTITY_FIELD_INSERT_DEPENDENCY, 1);
			millis = System.currentTimeMillis() - fr;
		}catch (Exception e){
			sql_success = false;
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if (null != dmListener) {
			dmListener.afterUpdate(runtime, random, run, result, dest, data, columns, sql_success, result,  millis);
		}
		InterceptorProxy.afterUpdate(runtime, random, run, dest, data, configs, columns, sql_success, result, System.currentTimeMillis() - fr);
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
			SWITCH swt = SWITCH.CONTINUE;
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
			Map<String,Object> values = EntityAdapterProxy.primaryValues(obj);
			for(Map.Entry entry:values.entrySet()){
				if(BasicUtil.isNotEmpty(entry.getValue())){
					return false;
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
		return insert(null, null, true, dest, data, checkPrimary, columns);
	}
	protected int insert(DataRuntime runtime, String random, boolean recover, String dest, Object data, boolean checkPrimary, List<String> columns) {
		dest = DataSourceUtil.parseDataSource(dest, data);
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		SWITCH swt = SWITCH.CONTINUE;
		boolean sql_success = false;
		swt = InterceptorProxy.prepareInsert(runtime,random,  dest, data, checkPrimary, columns);
		if(swt == SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareInsert(runtime, random, dest, data, checkPrimary, columns);
		}
		if(swt == SWITCH.BREAK){
			return -1;
		}

		DriverAdapter adapter = runtime.getAdapter();
		if(null != data && data instanceof DataSet){
			DataSet set = (DataSet)data;
			Map<String,Object> tags = set.getTags();
			if(null != tags && tags.size()>0){
				LinkedHashMap<String, PartitionTable> ptables = ptables(false, new MasterTable(dest), tags);
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
		Run run = adapter.buildInsertRun(runtime, dest, data, checkPrimary, columns);
		Table table = new Table(dest);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			table.setColumns(columns(runtime, random, false, table));
		}
		if(null == run){
			return 0;
		}

		int cnt = 0;
		//final String sql = run.getFinalInsert();
		//final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		long millis = -1;
		try{
			swt = InterceptorProxy.beforeInsert(runtime, random, run, dest, data, checkPrimary, columns);
			if(swt == SWITCH.BREAK){
				return -1;
			}
			if(null != dmListener){
				swt = dmListener.beforeInsert(runtime, random, run, dest, data, checkPrimary, columns);
			}
			if(swt == SWITCH.BREAK){
				return -1;
			}
			cnt = adapter.insert(runtime, random, data, run, null);

			int ENTITY_FIELD_INSERT_DEPENDENCY = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_INSERT_DEPENDENCY();
			checkMany2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
			checkOne2ManyDependencySave(runtime, random, data, ENTITY_FIELD_INSERT_DEPENDENCY, 0);
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if (null != dmListener) {
			dmListener.afterInsert(runtime, random, run, cnt, dest, data, checkPrimary, columns, sql_success, cnt, millis);
		}

		InterceptorProxy.afterInsert(runtime, random, run, dest, data, checkPrimary, columns, sql_success, cnt, System.currentTimeMillis() - fr);
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
	private int checkMany2ManyDependencySave(DataRuntime runtime, String random, Object obj, int dependency, int mode){
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
				checkMany2ManyDependencySave(runtime, random, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			Column pc = EntityAdapterProxy.primaryKey(clazz);
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
						Column joinpc = EntityAdapterProxy.primaryKey(clazz);
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
					insert(runtime, random, false, join.joinTable, set);

				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else{
						log.error("[check Many2ManyDependency Save][result:fail][msg:{}]", e.toString());
					}
				}
			}
		}
		dependency --;
		return result;
	}

	private int checkOne2ManyDependencySave(DataRuntime runtime, String random, Object obj, int dependency, int mode){
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
				checkOne2ManyDependencySave(runtime, random, entity, dependency, mode);
			}
		}else{
			Class clazz = obj.getClass();
			Column pc = EntityAdapterProxy.primaryKey(clazz);
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
					insert(runtime, random, false, join.dependencyTable, items);

				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else{
						log.error("[check One2ManyDependency Save][result:fail][msg:{}]", e.toString());
					}
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
	protected int insert(DataRuntime runtime, String random, boolean recover, String dest, Object data, String ... columns){
		return insert(runtime, random, recover, dest, data, false, BeanUtil.array2list(columns));
	}
	@Override
	public int insert(Object data, String ... columns){
		return insert(null, data, false, BeanUtil.array2list(columns));
	}

	/*
	 * 查询
	 * @param sql  sql
	 * @param values  values
	 * @return List
	protected List<Map<String,Object>> maps(DataRuntime runtime, String random, String sql, List<Object> values){
	 */
/*
	protected List<Map<String,Object>> maps(DataRuntime runtime, String random, Run run){
		DriverAdapter adapter = runtime.getAdapter();
		List<Map<String,Object>> maps = adapter.maps(runtime, random, run);
		return maps;
	}
*/

	/**
	 * 封装查询结果
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	protected static DataRow row(DataRuntime runtime, boolean system, LinkedHashMap<String, Column> metadatas, ResultSet rs) {
		DataRow row = new DataRow();
		try {
			DriverAdapter adapter = runtime.getAdapter();
			ResultSetMetaData rsmd = rs.getMetaData();
			int qty = rsmd.getColumnCount();
			if (!system && metadatas.isEmpty()) {
				for (int i = 1; i <= qty; i++) {
					String name = rsmd.getColumnName(i);
					if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
						continue;
					}
					Column column = metadatas.get(name) ;
					column = adapter.column(runtime, column, rsmd, i);
					metadatas.put(name.toUpperCase(), column);
				}
			}
			for (int i = 1; i <= qty; i++) {
				String name = rsmd.getColumnLabel(i);
				if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
					continue;
				}
				Column column = metadatas.get(name.toUpperCase());
				//Object v = BeanUtil.value(column.getTypeName(), rs.getObject(name));
				Object value = adapter.read(runtime, column, rs.getObject(name), null);
				row.put(false, name, value);
			}
			row.setMetadatas(metadatas);
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[封装结果集][result:fail][msg:{}]", e.toString());
			}
		}
		return row;
	}
/*
	protected DataSet select(DataRuntime runtime, String random, String table, String sql, List<Object> values){
		return select(runtime, random, false, table, sql, values);
	}*/
	protected DataSet select(DataRuntime runtime, String random, String table, Run run){
		if(null == run){
			return new DataSet();
		}
		return select( runtime, random, false, table, run);
	}

	/**
	 * 查询
	 * @param system 系统表不查询表结构
	 * @param runtime runtime
	 * @param table 查询表结构时使用
	 * @param run run
	 * @return DataSet
	protected DataSet select(DataRuntime runtime, String random, boolean system,  String table, String sql, List<Object> values){
	 */
	protected DataSet select(DataRuntime runtime, String random, boolean system, String table, Run run){
		if(null == runtime){
			runtime = runtime();
		}
		DriverAdapter adapter = runtime.getAdapter();
		return adapter.select(runtime, random, system, table, run);
	}

	/**
	 * 查询
	 * @param runtime runtime
	 * @param clazz entity class
	 * @param table table
	 * @param run run
	 * @param dependency 是否加载依赖 >0时加载
	 * @return EntitySet
	 * @param <T> entity.class
	protected <T> EntitySet<T> select(DataRuntime runtime, String random,  Class<T> clazz, String table,  String sql, List<Object> values, int dependency){
	 */
	protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, String table, Run run, int dependency){
		EntitySet<T> set = new EntitySet<>();
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		DataSet rows = select(runtime, random, table, run);
		for(DataRow row:rows){
			T entity = null;
			if(EntityAdapterProxy.hasAdapter(clazz)){
				//jdbc adapter需要参与 或者metadata里添加colun type
				entity = EntityAdapterProxy.entity(clazz, row, null);
			}else{
				entity = row.entity(clazz);
			}
			set.add(entity);
		}
		//检测依赖关系
		if(dependency > 0) {
			checkMany2ManyDependencyQuery(runtime, random, set, dependency);
			checkOne2ManyDependencyQuery(runtime, random, set, dependency);
		}
		return set;
	}
	protected <T> void checkMany2ManyDependencyQuery(DataRuntime runtime, String random, EntitySet<T> set, int dependency) {
		//ManyToMany
		if(set.size()==0 || dependency <= 0){
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "ManyToMany");
		Compare compare = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE();
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
							DataSet items = querys(runtime, random, false, new DefaultTablePrepare(join.joinTable), new DefaultConfigStore(), "++" + join.joinColumn + ":" + primaryValueMap.get(pk.toUpperCase()));

							List<String> ids = items.getStrings(join.inverseJoinColumn);
							BeanUtil.setFieldValue(entity, field, ids);
						} else {
							//通过子表完整查询 List<Department> departments
							//SELECT * FROM HR_DEPARTMENT WHERE ID IN(SELECT DEPARTMENT_ID FROM HR_EMPLOYEE_DEPARTMENT WHERE EMPLOYEE_ID = ?)
							String sql = "SELECT * FROM " + join.dependencyTable + " WHERE " + join.dependencyPk + " IN (SELECT " + join.inverseJoinColumn + " FROM " + join.joinTable + " WHERE " + join.joinColumn + "=?" + ")";
							SimpleRun run = new SimpleRun(sql);
							run.addValue(primaryValueMap.get(pk.toUpperCase()));
							EntitySet<T> dependencys = select(runtime, random, join.itemClass, null, run, dependency);
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
						DataSet allItems = querys(runtime, random, false, new DefaultTablePrepare(join.joinTable), conditions);
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
						DataSet alls = querys(runtime, random, false, new DefaultTextPrepare(sql), conditions);
						for(T entity:set){
							DataSet items = alls.getRows("FK_"+join.joinColumn, idmap.get(entity)+"");
							BeanUtil.setFieldValue(entity, field, items.entity(join.itemClass));
						}
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check Many2ManyDependency query][result:fail][msg:{}]", e.toString());
				}
			}
		}
	}

	protected <T> void checkOne2ManyDependencyQuery(DataRuntime runtime, String random, EntitySet<T> set, int dependency) {
		//OneToMany
		if(set.size()==0 || dependency <= 0){
			return;
		}
		dependency --;
		Class clazz = set.get(0).getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
		String pk = null;
		if(null != pc){
			pk = pc.getName();
		}
		List<Field> fields = ClassUtil.getFieldsByAnnotation(clazz, "OneToMany");
		Compare compare = ThreadConfig.check(runtime.getKey()).ENTITY_FIELD_SELECT_DEPENDENCY_COMPARE();
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
						EntitySet<T> dependencys = querys(runtime, random, false, join.dependencyClass, new DefaultConfigStore().and(join.joinColumn, pv));
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
					EntitySet<T> alls = querys(runtime, random, false, join.dependencyClass, conditions);
					for(T entity:set){
						EntitySet items = alls.gets(join.joinField, idmap.get(entity));
						BeanUtil.setFieldValue(entity, field, items);
					}

				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check One2ManyDependency query][result:fail][msg:{}]", e.toString());
				}
			}

		}
	}
	@Override
	public int execute(RunPrepare prepare, ConfigStore configs, String ... conditions){
		return execute(null, null, true, prepare, configs, conditions);
	}
	protected int execute(DataRuntime runtime, String random, boolean recover, RunPrepare prepare, ConfigStore configs, String ... conditions){
		int result = -1;
		boolean sql_success = false;
		SWITCH swt = SWITCH.CONTINUE;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		DriverAdapter adapter = runtime.getAdapter();
		swt = InterceptorProxy.prepareExecute(runtime, random, prepare, configs, conditions);
		if(swt == SWITCH.BREAK){
			return -1;
		}

		Run run = adapter.buildExecuteRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime().datasource() + "]");
			}
			return -1;
		}
		//String txt = run.getFinalExecute();
		//List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();

		long millis = -1;
		try{

			swt = InterceptorProxy.beforeExecute(runtime, random, run);
			if(swt == SWITCH.BREAK){
				return -1;
			}
			if(null != dmListener){
				swt = dmListener.beforeExecute(runtime, random, run);
			}
			if(swt == SWITCH.BREAK){
				return -1;
			}
			result = adapter.execute(runtime, random, run);/*
			if (null != values && values.size() > 0) {
				result = runtime.getTemplate().update(txt, values.toArray());
			} else {
				result = runtime.getTemplate().update(txt);
			}*/
			sql_success = true;
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if (null != dmListener) {
			dmListener.afterExecute(runtime, random, run,  sql_success, result, millis);
		}
		InterceptorProxy.afterExecute(runtime, random, run, sql_success, result, System.currentTimeMillis()-fr);
		return result;
	}
	@Override
	public int execute(RunPrepare prepare, String ... conditions){
		return execute(prepare, null, conditions);
	}

	@Override
	public boolean execute(Procedure procedure){
		return execute(null, null, true, procedure);
	}
	protected boolean execute(DataRuntime runtime, String random, boolean recover, Procedure procedure){
		boolean result = false;
		SWITCH swt = SWITCH.CONTINUE;
		boolean sql_success = false;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareExecute(runtime, random, procedure);
		if(swt == SWITCH.BREAK){
			return false;
		}
		if(null != dmListener){
			swt = dmListener.prepareExecute(runtime, random, procedure);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			swt = InterceptorProxy.beforeExecute(runtime, random, procedure);
			if(swt == SWITCH.BREAK){
				return false;
			}
			if(null != dmListener){
				swt = dmListener.beforeExecute(runtime, random, procedure);
			}
			if(swt == SWITCH.BREAK){
				return false;
			}
			result = runtime.getAdapter().execute(runtime, random, procedure);
			sql_success = true;
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		InterceptorProxy.afterExecute(runtime, random, procedure, sql_success, result, System.currentTimeMillis()-fr);
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
		return querys(null, null, true, procedure, navi);
	}
	protected DataSet querys(DataRuntime runtime, String random, boolean recover, Procedure procedure, PageNavi navi){
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		boolean sql_success = false;
		long fr = System.currentTimeMillis();
		SWITCH swt = InterceptorProxy.prepareQuery(runtime, random, procedure, navi);
		if(swt == SWITCH.BREAK){
			return new DataSet();
		}

		DataSet set = null;
		try{
			swt = InterceptorProxy.beforeQuery(runtime, random, procedure, navi);
			if(swt == SWITCH.BREAK){
				return new DataSet();
			}
			if(null != dmListener){
				dmListener.beforeQuery(runtime, random, procedure);
			}
			set = runtime.getAdapter().querys(runtime, random, procedure, navi);
			sql_success = true;

		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if(null != dmListener){
			dmListener.afterQuery(runtime, random, procedure, sql_success, set, System.currentTimeMillis()-fr);
		}
		InterceptorProxy.afterQuery(runtime, random, procedure, navi, sql_success, set, System.currentTimeMillis()-fr);
		return set;
	}

	public <T> int deletes(String table, String key, Collection<T> values){
		return deletes(true, table, key, values);
	}
	protected  <T> int deletes(DataRuntime runtime, String random, boolean recover, String table, String key, Collection<T> values){
		table = DataSourceUtil.parseDataSource(table, null);

		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}

		SWITCH swt = InterceptorProxy.prepareDelete(runtime, random, table, key, values);
		if(swt == SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareDelete(runtime, random, table, key, values);
		}
		if(swt == SWITCH.BREAK){
			return -1;
		}
		DriverAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildDeleteRun(runtime, table, key, values);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][table:" +table+ "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		int result = exeDelete(runtime, random, recover, run);
		return result;
	}


	public int deletes(String table, String key, Object ... values){
		return deletes(true, table, key, values);
	}
	protected int deletes(boolean recover, String table, String key, Object ... values){
		table = DataSourceUtil.parseDataSource(table, null);
		DataRuntime runtime = runtime();
		List<Object> list = new ArrayList<>();
		if(null != values){
			for(Object value:values){
				if(value instanceof Collection){
					list.addAll((Collection)value);
				}else {
					list.add(value);
				}
			}
		}
		return deletes(null, null, recover, table, key, list);
	}
	@Override
	public int delete(String dest, Object obj, String... columns) {
		dest = DataSourceUtil.parseDataSource(dest,obj);
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = SWITCH.CONTINUE;
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
				if(log.isInfoEnabled()) {
					log.info("[delete Collection][影响行数:{}]", LogUtil.format(size, 34));
				}
			}else{
				swt = InterceptorProxy.prepareDelete(runtime, random, dest, obj, columns);
				if(swt == SWITCH.BREAK){
					return -1;
				}
				if(null != dmListener){
					swt = dmListener.prepareDelete(runtime, random, dest, obj, columns);
				}
				if(swt == SWITCH.BREAK){
					return -1;
				}
				DriverAdapter adapter = runtime.getAdapter();
				Run run = adapter.buildDeleteRun(runtime, dest, obj, columns);
				if(!run.isValid()){
					if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
						log.warn("[valid:false][不具备执行条件][dest:" + dest + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
					}
					return -1;
				}
				size = exeDelete(runtime, random, true, run);
				if(size > 0 && ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY > 0){
					if(!(obj instanceof DataRow)){
						checkMany2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
						checkOne2ManyDependencyDelete(runtime, random, obj, ConfigTable.ENTITY_FIELD_DELETE_DEPENDENCY );
					}
				}
			}
		}
		return size;
	}
	private int checkMany2ManyDependencyDelete(DataRuntime runtime, String random, Object entity, int dependency){
		int result = 0;
		//ManyToMany
		if(dependency <= 0){
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
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
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check Many2ManyDependency delete][result:fail][msg:{}]", e.toString());
				}
			}
		}
		return result;
	}
	private int checkOne2ManyDependencyDelete(DataRuntime runtime, String random, Object entity, int dependency){
		int result = 0;
		//OneToMany
		if(dependency <= 0){
			return result;
		}
		dependency --;
		Class clazz = entity.getClass();
		Column pc = EntityAdapterProxy.primaryKey(clazz);
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
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.error("[check One2ManyDependency delete][result:fail][msg:{}]", e.toString());
				}
			}
		}
		return result;
	}
	@Override
	public int delete(String table, ConfigStore configs, String... conditions) {
		table = DataSourceUtil.parseDataSource(table, null);
		DataRuntime runtime = runtime();
		String random = random(runtime);

		SWITCH swt = SWITCH.CONTINUE;
		swt = InterceptorProxy.prepareDelete(runtime, random, table, configs, conditions);
		if(swt == SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareDelete(runtime,random, table, configs, conditions);
		}
		if(swt == SWITCH.BREAK){
			return -1;
		}
		DriverAdapter adapter = runtime.getAdapter();
		Run run = adapter.buildDeleteRun(runtime, table, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][table:" + table + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		int result = exeDelete(  runtime, random, true, run);
		return result;
	}

	/**
	 * 执行删除
	 * @param recover 执行完成后是否根据设置自动还原数据源
	 * @param runtime DataRuntime
	 * @param run Run
	 * @return int
	 */
	protected int exeDelete(DataRuntime runtime, String random, boolean recover, Run run){
		int result = -1;
		boolean sql_success = false;
		SWITCH swt = SWITCH.CONTINUE;
		/*final String sql = run.getFinalDelete();
		final List<Object> values = run.getValues();*/
		long fr = System.currentTimeMillis();
		/*if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}*/
		swt = InterceptorProxy.beforeDelete(runtime, random, run);
		if(swt == SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeDelete(runtime, random, run);
		}
		if(swt == SWITCH.BREAK){
			return -1;
		}
		long millis = -1;
		try{/*
			if(null == values) {
				result = runtime.getTemplate().update(sql);
			}else{
				result = runtime.getTemplate().update(sql, values.toArray());
			}*/
			result = runtime.getAdapter().execute(runtime, random, run);
			sql_success = true;
			millis = System.currentTimeMillis() - fr;
		}finally{
			// 自动切换回切换前的数据源
			if(recover && !isFix() && ClientHolder.isAutoRecover()){
				ClientHolder.recoverDataSource();
			}
		}
		if(null != dmListener){
			dmListener.afterDelete(runtime, random, run, sql_success, result, millis);
		}
		InterceptorProxy.afterDelete(runtime, random, run,  sql_success, result, millis);
		return result;
	}


	@Override
	public int truncate(String table){
		table = DataSourceUtil.parseDataSource(table);
		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		List<Run> runs = adapter.buildTruncateRun(runtime, table);
		if(null != runs && runs.size()>0) {
			RunPrepare prepare = new DefaultTextPrepare(runs.get(0).getFinalUpdate());
			return execute(prepare);
		}
		return -1;
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
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													database
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Database> databases()
	 ******************************************************************************************************************/

	@Override
	public LinkedHashMap<String, Database> databases(){
		LinkedHashMap<String,Database> databases = new LinkedHashMap<>();

		////DataSource ds = null;
		////Connection con = null;

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try{
			long fr = System.currentTimeMillis();
			////ds = runtime.getTemplate().getDataSource();
			////con = DataSourceUtils.getConnection(ds);
			// 根据系统表查询
			try{
				List<Run> runs = adapter.buildQueryDatabaseRun(runtime);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, (String)null, run).toUpperKey();
						databases = adapter.databases(runtime, idx++, true, databases, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[databases][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[databases][result:{}][执行耗时:{}ms]", random, databases.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[databases][result:fail][msg:{}]", e.toString());
			}
		}
		return databases;
	}

	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Table> tables(String catalog, String schema, String name, String types)
	 * LinkedHashMap<String, Table> tables(String schema, String name, String types)
	 * LinkedHashMap<String, Table> tables(String name, String types)
	 * LinkedHashMap<String, Table> tables(String types)
	 * LinkedHashMap<String, Table> tables()
	 ******************************************************************************************************************/

	/**
	 * 缓存表名
	 * @param runtime runtime
	 * @param random random
	 * @param catalog catalog
	 * @param schema schema
	 */
	private void tableMap(DataRuntime runtime, String random, String catalog, String schema){
		Map<String,String> names = CacheProxy.names(catalog, schema);
		if(null == names || names.isEmpty()){
			if(null == runtime){
				runtime = runtime();
			}
			if(null == random){
				random = random(runtime);
			}
			DriverAdapter adapter = runtime.getAdapter();
			LinkedHashMap<String, Table> tables = new LinkedHashMap<>();
			boolean sys = false; //根据系统表查询
			try {
				List<Run> runs = adapter.buildQueryTableRun(runtime, null, null, null, null);
				if (null != runs && runs.size() > 0) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, (String) null, run).toUpperKey();
						tables = adapter.tables(runtime, idx++, true, catalog, schema, tables, set);
						sys = true;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			if(!sys){
				try {
					tables = adapter.tables(runtime, true, null, catalog, schema, null, null);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
			if(null != tables){
				for(Table table:tables.values()){
					CacheProxy.name(table.getCatalog(),  table.getSchema(), table.getName(), table.getName());
				}
			}
		}

	}
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
	public <T extends Table> LinkedHashMap<String, T> tables(boolean greedy, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		////DataSource ds = null;
		////Connection con = null;

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try{
			long fr = System.currentTimeMillis();
			////ds = runtime.getTemplate().getDataSource();
			////con = DataSourceUtils.getConnection(ds);
			Table search = new Table();
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					adapter.checkSchema(runtime, tmp);
				}
				if(null == catalog){
					catalog = tmp.getCatalog();
				}
				if(null == schema){
					schema = tmp.getSchema();
				}
			}
			String origin = CacheProxy.name(greedy, catalog, schema, pattern);
			if(null == origin){
				tableMap(runtime, random, catalog, schema);
			}
			origin = CacheProxy.name(greedy, catalog, schema, pattern);
			if(null == origin){
				origin = pattern;
			}
			search.setName(origin);
			search.setCatalog(catalog);
			search.setSchema(schema);

			String[] tps = null;
			if(null != types){
				tps = types.toUpperCase().trim().split(",");
			}
			// 根据系统表查询
			try{
				List<Run> runs = adapter.buildQueryTableRun(runtime, catalog, schema, origin, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, (String)null, run).toUpperKey();
						tables = adapter.tables(runtime, idx++, true, catalog, schema, tables, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
				}
			}

			// 根据系统表查询失败后根据驱动内置接口补充
			if(null == tables || tables.size() == 0) {
				try {
					LinkedHashMap<String, T> jdbcTables = adapter.tables(runtime, true, null, catalog, schema, origin, tps);
					for (String key : jdbcTables.keySet()) {
						if (!tables.containsKey(key.toUpperCase())) {
							T item = jdbcTables.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									tables.put(key.toUpperCase(), item);
								}
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[tables][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			if(null != tables){
				for(Table table:tables.values()){
					if(BasicUtil.isNotEmpty(table.getComment())){
						comment = true;
						break;
					}
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = adapter.buildQueryTableCommentRun(runtime, catalog, schema, null, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							DataSet set = select(runtime, random, (String) null, run).toUpperKey();
							tables = adapter.comments(runtime, idx++, true, catalog, schema, tables, set);
						}
					}
				} catch (Exception e) {
					if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.info("{}[tables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, origin, types, tables.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(origin)){
				LinkedHashMap<String,T> tmps = new LinkedHashMap<>();
				List<String> keys = BeanUtil.getMapKeys(tables);
				for(String key:keys){
					T item = tables.get(key);
					String name = item.getName(greedy);
					if(RegularUtil.match(name, origin)){
						tmps.put(name.toUpperCase(), item);
					}
				}
				tables = tmps;
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[tables][result:fail][msg:{}]", e.toString());
			}
		}
		return tables;
	}

	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(boolean greedy, String schema, String name, String types){
		return tables(greedy, null, schema, name, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(boolean greedy, String name, String types){
		return tables(greedy, null, null, name, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(boolean greedy, String types){
		return tables(greedy, null, null, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(boolean greedy){
		return tables(greedy, null, null, null, "TABLE");
	}

	public <T extends Table> LinkedHashMap<String, T> tables(String catalog, String schema, String pattern, String types){
		return tables(false, catalog, schema, pattern, types);
	}

	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(String schema, String name, String types){
		return tables(false, null, schema, name, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(String name, String types){
		return tables(false, null, null, name, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(String types){
		return tables(false, null, null, types);
	}
	@Override
	public <T extends Table> LinkedHashMap<String,T> tables(){
		return tables(false, null, null, null, "TABLE");
	}

	/**
	 * 查询表的创建SQL
	 * @param table table
	 * @param init 是否还原初始状态(如自增ID)
	 * @return list
	 */
	@Override
	public List<String> ddl(Table table, boolean init){
		List<String> list = new ArrayList<>();

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = adapter.buildQueryDDLRun(runtime, table);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, null, run).toUpperKey();
					list = adapter.ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = columns(table);
				table.setColumns(columns);
				table.setTags(tags(table));
				PrimaryKey pk = primary(table);
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.setPrimaryKey(true);
						}
					}
				}
				table.setPrimaryKey(pk);
				table.setIndexs(indexs(table));
				runs = adapter.buildCreateRun(runtime, table);
				for(Run run:runs){
					list.add(run.getFinalUpdate());
					table.setDdls(list);
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[table ddl][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.info("{}[table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询表的创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, View> views(String catalog, String schema, String name, String types)
	 * LinkedHashMap<String, View> views(String schema, String name, String types)
	 * LinkedHashMap<String, View> views(String name, String types)
	 * LinkedHashMap<String, View> views(String types)
	 * LinkedHashMap<String, View> views()
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
	public <T extends View> LinkedHashMap<String, T> views(boolean greedy, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String,T> views = new LinkedHashMap<>();
		//DataSource ds = null;
		//Connection con = null;

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try{
			long fr = System.currentTimeMillis();
			////ds = runtime.getTemplate().getDataSource();
			////con = DataSourceUtils.getConnection(ds);
			View search = new View();
			if(null == catalog || null == schema){
				View tmp = new View();
				if(!greedy) {
					adapter.checkSchema(runtime, tmp);
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

			DataRow view_map = CacheProxy.getViewMaps(runtime.datasource());
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
				List<Run> runs = adapter.buildQueryViewRun(runtime, catalog, schema, pattern, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, (String)null, run).toUpperKey();
						views = adapter.views(runtime, idx++, true, catalog, schema, views, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[views][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}
			if(null == views || views.size() ==0) {
				// 根据驱动内置接口补充
				try {
					LinkedHashMap<String, T> tmps = adapter.views(runtime, true, null, catalog, schema, pattern, tps);
					for (String key : tmps.keySet()) {
						if (!views.containsKey(key.toUpperCase())) {
							T item = tmps.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									views.put(key.toUpperCase(), item);
								}
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[views][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, pattern, e.toString());
					}
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[views][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, views.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(pattern)){
				LinkedHashMap<String,T> tmps = new LinkedHashMap<>();
				List<String> keys = BeanUtil.getMapKeys(views);
				for(String key:keys){
					T item = views.get(key);
					String name = item.getName(greedy);
					if(RegularUtil.match(name, pattern)){
						tmps.put(name.toUpperCase(), item);
					}
				}
				views = tmps;
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[views][result:fail][msg:{}]", e.toString());
			}
		}
		return views;
	}

	@Override
	public <T extends View> LinkedHashMap<String,T> views(boolean greedy, String schema, String name, String types){
		return views(greedy, null, schema, name, types);
	}
	@Override
	public <T extends View> LinkedHashMap<String,T> views(boolean greedy, String name, String types){
		return views(greedy, null, null, name, types);
	}
	@Override
	public <T extends View> LinkedHashMap<String,T> views(boolean greedy, String types){
		return views(greedy, null, null, types);
	}
	@Override
	public <T extends View> LinkedHashMap<String,T> views(boolean greedy){
		return views(greedy, null, null, null, "TABLE");
	}

	public <T extends View> LinkedHashMap<String, T> views(String catalog, String schema, String pattern, String types){
		return views(false, catalog, schema, pattern, types);
	}

	@Override
	public <T extends View> LinkedHashMap<String,T> views(String schema, String name, String types){
		return views(false, null, schema, name, types);
	}
	@Override
	public <T extends View> LinkedHashMap<String,T> views(String name, String types){
		return views(false, null, null, name, types);
	}
	@Override
	public <T extends View> LinkedHashMap<String,T> views(String types){
		return views(false, null, null, types);
	}
	@Override
	public <T extends View> LinkedHashMap<String,T> views(){
		return views(false, null, null, null, "TABLE");
	}

	/**
	 * 查询view的创建SQL
	 * @param view view
	 * @return list
	 */
	@Override
	public List<String> ddl(View view){
		List<String> list = new ArrayList<>();
		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = adapter.buildQueryDDLRun(runtime, view);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, random, run).toUpperKey();
					list = adapter.ddl(runtime, idx++, view, list,  set);
				}
				view.setDdls(list);
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[view ddl][view:{}][result:{}][执行耗时:{}ms]", random, view.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.info("{}[view ddl][{}][view:{}][msg:{}]", random, LogUtil.format("查询视图创建DDL失败", 33), view.getName(), e.toString());
			}
		}
		return list;
	}
	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, MasterTable> mtables(String catalog, String schema, String name, String types)
	 * LinkedHashMap<String, MasterTable> mtables(String schema, String name, String types)
	 * LinkedHashMap<String, MasterTable> mtables(String name, String types)
	 * LinkedHashMap<String, MasterTable> mtables(String types)
	 * LinkedHashMap<String, MasterTable> mtables()
	 ******************************************************************************************************************/
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String catalog, String schema, String pattern, String types) {

		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		//DataSource ds = null;
		//Connection con = null;

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try{
			long fr = System.currentTimeMillis();
			////ds = runtime.getTemplate().getDataSource();
			////con = DataSourceUtils.getConnection(ds);
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					adapter.checkSchema(runtime, tmp);
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
			DataRow table_map = CacheProxy.getTableMaps(runtime.datasource());
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
				List<Run> runs = adapter.buildQueryMasterTableRun(runtime, catalog, schema, pattern, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, (String)null, run).toUpperKey();
						tables = adapter.mtables(runtime, idx++, true, catalog, schema, tables, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, pattern, e.toString());
				}
			}

			if(null == tables || tables.size() ==0 ) {
				// 根据驱动内置接口补充
				try {
					LinkedHashMap<String, T> tmps = adapter.mtables(runtime, true, null, catalog, schema, pattern, tps);
					for (String key : tmps.keySet()) {
						if (!tables.containsKey(key.toUpperCase())) {
							T item = tmps.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									tables.put(key.toUpperCase(), item);
								}
							}
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[stables][{}][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, pattern, e.toString());
					}
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[stables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, pattern, types, tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[mtables][result:fail][msg:{}]", e.toString());
			}
		} 
		return tables;
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String schema, String name, String types) {
		return mtables(greedy, null, schema, name, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String name, String types) {
		return mtables(greedy, null, null, name, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy, String types) {
		return mtables(greedy, null, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(boolean greedy) {
		return mtables(greedy, "STABLE");
	}

	public <T extends MasterTable> LinkedHashMap<String, T> mtables(String catalog, String schema, String pattern, String types){
		return mtables(false, catalog, schema, pattern, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(String schema, String name, String types) {
		return mtables(false, null, schema, name, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(String name, String types) {
		return mtables(false, null, null, name, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(String types) {
		return mtables(false, null, types);
	}

	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables() {
		return mtables(false, "STABLE");
	}

	/**
	 * 查询MasterTable创建SQL
	 * @param table MasterTable
	 * @return list
	 */
	@Override
	public List<String> ddl(MasterTable table){
		List<String> list = new ArrayList<>();
		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = adapter.buildQueryDDLRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, random, run).toUpperKey();
					list = adapter.ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[master table ddl][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.info("{}[master table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询主表创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
	}
	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, PartitionTable> ptables(String catalog, String schema, String master, String name)
	 * LinkedHashMap<String, PartitionTable> ptables(String schema, String master, String name)
	 * LinkedHashMap<String, PartitionTable> ptables(String master, String name)
	 * LinkedHashMap<String, PartitionTable> ptables(String master)
	 * LinkedHashMap<String, PartitionTable> ptables(MasterTable table)
	 ******************************************************************************************************************/

	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String catalog, String schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return ptables(greedy,mtable, null, name);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String schema, String master, String name){
		return ptables(greedy,null, schema, master, name);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master, String name){
		return ptables(greedy,null, null, master, name);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(boolean greedy, String master){
		return ptables(greedy,null, null, master, null);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean greedy, MasterTable master){
		return ptables(greedy,master, null);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean greedy, MasterTable master, Map<String, Object> tags){
		return ptables(greedy,master, tags, null);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(boolean greedy, MasterTable master, Map<String, Object> tags, String name){
		LinkedHashMap<String,T> tables = new LinkedHashMap<>();
		//DataSource ds = null;
		//Connection con = null;

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try{
			long fr = System.currentTimeMillis();
			//ds = runtime.getTemplate().getDataSource();
			//con = DataSourceUtils.getConnection(ds);
			// 根据系统表查询
			try{
				List<Run> runs = adapter.buildQueryPartitionTableRun(runtime, master, tags, name);
				if(null != runs) {
					int idx = 0;
					int total = runs.size();
					for(Run run:runs) {
						DataSet set = select(runtime, random, (String)null, run).toUpperKey();
						tables = adapter.ptables(runtime, total, idx++, true, master, master.getCatalog(), master.getSchema(), tables, set);
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE){
					e.printStackTrace();
				}else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tables][{}][stable:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), master.getName(), e.toString());
				}
			}

			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[tables][stable:{}][result:{}][执行耗时:{}ms]", random, master.getName(), tables.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[ptables][result:fail][msg:{}]", e.toString());
			}
		}
		return tables;
	}

	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String catalog, String schema, String master, String name){
		MasterTable mtable = new MasterTable(catalog, schema, master);
		return ptables(false,mtable, null, name);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String schema, String master, String name){
		return ptables(false,null, schema, master, name);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master, String name){
		return ptables(false,null, null, master, name);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String, T> ptables(String master){
		return ptables(false,null, null, master, null);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(MasterTable master){
		return ptables(false,master, null);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(MasterTable master, Map<String, Object> tags){
		return ptables(false,master, tags, null);
	}
	@Override
	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(MasterTable master, Map<String, Object> tags, String name){
		return ptables(false, master, tags, name);
	}

	/**
	 * 查询 PartitionTable 创建SQL
	 * @param table PartitionTable
	 * @return list
	 */
	@Override
	public List<String> ddl(PartitionTable table){
		List<String> list = new ArrayList<>();
		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		String random = random(runtime);
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = adapter.buildQueryDDLRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, random, run).toUpperKey();
					list = adapter.ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[partition table ddl][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.info("{}[partition table ddl][{}][table:{}][msg:{}]", random, LogUtil.format("查询子表创建DDL失败", 33), table.getName(), e.toString());
			}
		}
		return list;
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Column> columns(Table table)
	 * LinkedHashMap<String, Column> columns(String table)
	 * LinkedHashMap<String, Column> columns(String catalog, String schema, String table)
	 ******************************************************************************************************************/

	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(boolean greedy, Table table){
		return columns(null, null, greedy, table);
	}
	protected  <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table){

		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		if(null == table || BasicUtil.isEmpty(table.getName())){
			return new LinkedHashMap();
		}

		LinkedHashMap<String,T> columns = CacheProxy.columns(runtime.getKey(), table.getName());
		if(null != columns && !columns.isEmpty()){
			return columns;
		}

		long fr = System.currentTimeMillis();
		//DataSource ds = null;
		//Connection con = null;
		//DatabaseMetaData metadata = null;

		try {
			DriverAdapter adapter = runtime.getAdapter();
			if (!greedy) {
				checkSchema(runtime, table);
			}
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			try {
				//ds = runtime.getTemplate().getDataSource();
				//con = DataSourceUtils.getConnection(ds);
				//metadata = con.getMetaData();
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.warn("[metadata][resutl:fail][msg:{}]", e.toString());
				}
			}
			int qty_dialect = 0; //优先根据系统表查询
			int qty_metadata = 0; //再根据metadata解析
			int qty_jdbc = 0; //根据驱动内置接口补充
			// 优先根据系统表查询
			try {
				List<Run> runs = adapter.buildQueryColumnRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run: runs) {
						DataSet set = select(runtime, random, true, (String) null, run);
						columns = adapter.columns(runtime, idx, true, table, columns, set);
						idx++;
					}
				}
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				} if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			qty_dialect = columns.size();
			// 根据驱动内置接口补充
			// 再根据metadata解析 SELECT * FROM T WHERE 1=0
			if (columns.size() == 0) {
				try {
					columns = adapter.columns(runtime, true, table, columns);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				qty_jdbc = columns.size() - qty_metadata - qty_dialect;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据驱动内置接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, columns.size(), qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
			}
			//检测主键
			if(ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY) {
				if (columns.size() > 0) {
					boolean exists = false;
					for(Column column:columns.values()){
						if(column.isPrimaryKey() != -1){
							exists = true;
							break;
						}
					}
					if(!exists){
						PrimaryKey pk = primary(table);
						if(null != pk){
							LinkedHashMap<String,Column> pks = pk.getColumns();
							if(null != pks){
								for(String k:pks.keySet()){
									Column column = columns.get(k);
									if(null != column){
										column.setPrimaryKey(true);
									}
								}
							}
						}
					}
				}
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[columns][result:fail][table:{}][msg:{}]", random, table, e.toString());
			}
		}
		CacheProxy.columns(runtime.getKey(), table.getName(), columns);
		return columns;
	}
	@Override
	public <T extends Column> LinkedHashMap<String,T> columns(boolean greedy, String table){
		return columns(greedy, null, null, table);
	}
	@Override
	public <T extends Column> LinkedHashMap<String,T>  columns(boolean greedy, String catalog, String schema, String table){
		Table tab = new Table(catalog, schema, table);
		return columns(greedy, tab);
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(Table table){
		return columns(false, table);
	}
	@Override
	public <T extends Column> LinkedHashMap<String,T> columns(String table){
		return columns(false, null, null, table);
	}
	@Override
	public <T extends Column> LinkedHashMap<String,T>  columns(String catalog, String schema, String table){
		Table tab = new Table(catalog, schema, table);
		return columns(false, tab);
	}
	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Tag> tags(Table table)
	 * LinkedHashMap<String, Tag> tags(String table)
	 * LinkedHashMap<String, Tag> tags(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, Table table) {
		return tags(runtime(), greedy, table);
	}
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean greedy, Table table) {
		if(null == table || BasicUtil.isEmpty(table.getName())){
			return new LinkedHashMap();
		}
		LinkedHashMap<String,T> tags = CacheProxy.tags(runtime.getKey(), table.getName());
		if(null != tags && !tags.isEmpty()){
			return tags;
		}

		long fr = System.currentTimeMillis();
		//DataSource ds = null;
		//Connection con = null;
		String random = random(runtime);
		try {
			DriverAdapter adapter = runtime.getAdapter();
			if (!greedy) {
				checkSchema(runtime, table);
			}
			String catalog = table.getCatalog();
			String schema = table.getSchema();
			try {
				//ds = runtime.getTemplate().getDataSource();
				//con = DataSourceUtils.getConnection(ds);
				//metadata = con.getMetaData();
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else{
					log.warn("[][result:fail][msg:{}]", e.toString());
				}
			}


			// 先根据系统表查询
			try {
				List<Run> runs = adapter.buildQueryTagRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, (String) null, run).toUpperKey();
						tags = adapter.tags(runtime, idx, true, table, tags, set);
						idx++;
					}
				}
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
				if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[tags][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			if (null == tags || tags.size() == 0) {
				// 根据驱动内置接口补充
				try {
					// isAutoIncrement isGenerated remark default
					// 这一步会查出所有列(包括非tag列)
					tags = adapter.tags(runtime, false, tags, table, null);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}

			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[tags][catalog:{}][schema:{}][table:{}][执行耗时:{}ms]", random, catalog, schema, table, System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{

			}
		}
		CacheProxy.tags(runtime.getKey(), table.getName(), tags);
		return tags;
	}

	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String table) {
		Table tab = new Table();
		tab.setName(table);
		return tags(greedy, tab);
	}

	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(boolean greedy, String catalog, String schema, String table) {
		Table tab = new Table();
		tab.setCatalog(catalog);
		tab.setSchema(schema);
		tab.setName(table);
		return tags(greedy, tab);
	}
	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(Table table){
		return tags(false, table);
	}

	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(String table) {
		return tags(false, table);
	}

	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(String catalog, String schema, String table) {
		return tags(false, catalog, schema, table);
	}
	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * PrimaryKey primary(Table table)
	 * PrimaryKey primary(String table)
	 * PrimaryKey primary(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表
	 * @return map
	 */
	@Override
	public PrimaryKey primary(boolean greedy, Table table){
		PrimaryKey primary = null;

		DataRuntime runtime = runtime();
		DriverAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			checkSchema(runtime, table);
		}
		String tab = table.getName();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		//DataSource ds = null;
		String random = random(runtime);
		DatabaseMetaData metadata = null;

		try{
			List<Run> runs = adapter.buildQueryPrimaryRun(runtime, table);
			if(null != runs){
				int idx = 0;
				for(Run run:runs){
					DataSet set = select(runtime, random, (String)null, run).toUpperKey();
					primary = adapter.primary(runtime, idx, table, set);
					if(null != primary){
						primary.setTable(table);
					}

					idx ++;
				}
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[primary][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败",33), catalog, schema, table, e.toString());
			}
		}
		table.setPrimaryKey(primary);
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
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * List<Run> buildQueryForeignsRun(DataRuntime runtime, Table table) throws Exception
	 * <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, int index, Table table, LinkedHashMap<String, T> foreigns, DataSet set) throws Exception
	 ******************************************************************************************************************/
	@Override
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(boolean greedy, Table table){
		LinkedHashMap<String, T> foreigns = new LinkedHashMap<>();
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			checkSchema(runtime, table);
		}
		try {
			List<Run> runs = adapter.buildQueryForeignsRun(runtime, table);
			if(null != runs){
				int idx = 0;
				for(Run run:runs){
					DataSet set = select(runtime, random, (String)null, run).toUpperKey();
					foreigns = adapter.foreigns(runtime, idx,  table, foreigns, set);
					idx++;
				}
			}
		}catch (Exception e){
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
		}
		return foreigns;
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Index> indexs(Table table, String name)
	 * LinkedHashMap<String, Index> indexs(String table, String name)
	 * LinkedHashMap<String, Index> indexs(Table table)
	 * LinkedHashMap<String, Index> indexs(String table)
	 * LinkedHashMap<String, Index> indexs(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	/**
	 * 索引
	 * @param table 表
	 * @param name name
	 * @return map
	 */
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table, String name){
		LinkedHashMap<String,T> indexs = null;
		if(null == table){
			table = new Table();
		}
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			checkSchema(runtime, table);
		}
		if(null != table.getName()) {
			//DataSource ds = null;
			//Connection con = null;
			try {
				//ds = runtime.getTemplate().getDataSource();
				//con = DataSourceUtils.getConnection(ds);
				//dbmd.getIndexInfo(table.getCatalog(), table.getSchema(), table.getName(), unique, approximate);
				indexs = adapter.indexs(runtime, true, indexs, table, false, false);
				table.setIndexs(indexs);
			} catch (Exception e) {
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
			} 
			if(BasicUtil.isNotEmpty(name)){
				T index = indexs.get(name.toUpperCase());
				indexs = new LinkedHashMap<>();
				indexs.put(name.toUpperCase(), index);
			}
		}
		List<Run> runs = adapter.buildQueryIndexRun(runtime, table, name);

		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, (String)null, run).toUpperKey();
				try {
					indexs = adapter.indexs(runtime, idx, true, table, indexs, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		Index pk = null;
		for(Index index:indexs.values()){
			if(index.isPrimary()){
				pk = index;
				break;
			}
		}
		if(null == pk) {
			//识别主键索引
			pk = table.getPrimaryKey();
			if (null == pk) {
				pk = primary(table);
			}
			if (null != pk) {
				Index index = indexs.get(pk.getName().toUpperCase());
				if (null != index) {
					index.setPrimary(true);
				} else {
					indexs.put(pk.getName().toUpperCase(), (T) pk);
				}
			}
		}
		return indexs;
	}

	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, Table table){
		return indexs(greedy, table, null);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table, String name) {
		return indexs(greedy, new Table(table), name);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String table) {
		return indexs(greedy, new Table(table), null);
	}

	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(boolean greedy, String catalog, String schema, String table) {
		return indexs(greedy, new Table(catalog, schema, table), null);
	}

	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(Table table, String name){
		return indexs(false, table, name);
	}

	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(Table table){
		return indexs(false, table);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(String table, String name) {
		return indexs(false, table, name);
	}
	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs( String table) {
		return indexs(false, table);
	}

	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(String catalog, String schema, String table) {
		return indexs(false, catalog, schema, table);
	}
	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * LinkedHashMap<String, Constraint> constraints(Table table, String name)
	 * LinkedHashMap<String, Constraint> constraints(String table, String name)
	 * LinkedHashMap<String, Constraint> constraints(Table table)
	 * LinkedHashMap<String, Constraint> constraints(String table)
	 * LinkedHashMap<String, Constraint> constraints(String catalog, String schema, String table)
	 ******************************************************************************************************************/
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints( boolean greedy, Table table, String name) {
		return null;
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints( boolean greedy, Table table) {
		return constraints(greedy, table, null);
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints( boolean greedy, String table) {
		return constraints(greedy, new Table(table));
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints( boolean greedy, String table, String name) {
		return constraints(greedy, new Table(table), name);
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints( boolean greedy, String catalog, String schema, String table) {
		return constraints(greedy, new Table(catalog, schema, table));
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(Table table, String name) {
		return constraints(false, table, name);
	}
	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(Table table) {
		return constraints(false, table);
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(String table) {
		return constraints(false, new Table(table));
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(String table, String name) {
		return constraints(false, new Table(table), name);
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(String catalog, String schema, String table) {
		return constraints(false, new Table(catalog, schema, table));
	}
	/* *****************************************************************************************************************
	 * 													trigger
	 ******************************************************************************************************************/
	@Override
	public <T extends Trigger> LinkedHashMap<String, T> triggers(boolean greedy, Table table, List<Trigger.EVENT> events){
		LinkedHashMap<String,T> triggers = new LinkedHashMap<>();
		if(null == table){
			table = new Table();
		}
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		if(!greedy) {
			checkSchema(runtime, table);
		}
		List<Run> runs = adapter.buildQueryTriggerRun(runtime, table, events);

		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, (String)null, run).toUpperKey();
				try {
					triggers = adapter.triggers(runtime, idx, true, table, triggers, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}

				idx ++;
			}
		}
		return triggers;
	}

	/* *****************************************************************************************************************
	 * 													procedure
	 ******************************************************************************************************************/
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(boolean greedy, String catalog, String schema, String name){
		LinkedHashMap<String,T> procedures = new LinkedHashMap<>();
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		List<Run> runs = adapter.buildQueryProcedureRun(runtime, catalog, schema, name);

		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, (String)null, run).toUpperKey();
				try {
					procedures = adapter.procedures(runtime, idx, true, procedures, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return procedures;
	}


	/* *****************************************************************************************************************
	 * 													function
	 ******************************************************************************************************************/
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(boolean greedy, String catalog, String schema, String name){
		LinkedHashMap<String,T> functions = new LinkedHashMap<>();
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		List<Run> runs = adapter.buildQueryFunctionRun(runtime, catalog, schema, name);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, (String)null, run).toUpperKey();
				try {
					functions = adapter.functions(runtime, idx, true, functions, set);
				}catch (Exception e){
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}
				idx ++;
			}
		}
		return functions;
	}
	/* *****************************************************************************************************************
	 *
	 * 													DDL
	 *
	 * =================================================================================================================
	 * database			: 数据库
	 * table			: 表
	 * master table		: 主表
	 * partition table	: 分区表
	 * column			: 列
	 * tag				: 标签
	 * primary key      : 主键
	 * foreign key		: 外键
	 * index			: 索引
	 * constraint		: 约束
	 * trigger		    : 触发器
	 * procedure        : 存储过程
	 * function         : 函数
	 ******************************************************************************************************************/


	/* *****************************************************************************************************************
	 * 													table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Table table) throws Exception
	 * boolean alter(Table table) throws Exception
	 * boolean drop(Table table) throws Exception
	 * boolean rename(Table origin, String name) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Table meta) throws Exception {
		boolean result = false;
		DDL action = DDL.TABLE_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Table table) throws Exception {
		boolean result = true;
		List<Run> runs = new ArrayList<>();
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();


		DataRuntime runtime = runtime();
		String random = random(runtime);
		DriverAdapter adapter = runtime.getAdapter();
		SWITCH swt = InterceptorProxy.prepare(runtime, random, DDL.TABLE_ALTER, table);
		if(null != ddListener && swt == SWITCH.CONTINUE) {
			swt = ddListener.parepareAlter(runtime, random, table);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		checkSchema(runtime, table);
		checkSchema(runtime, update);

		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, random, table, uname);
			table.setName(uname);
		}
		if(!result){
			return result;
		}

		//修改表备注
		String comment = update.getComment()+"";
		if(!comment.equals(table.getComment())){
			swt = InterceptorProxy.prepare(runtime, random, DDL.TABLE_COMMENT, table);
			if(swt != SWITCH.BREAK) {
				if(BasicUtil.isNotEmpty(table.getComment())) {
					runs.addAll(adapter.buildChangeCommentRun(runtime, update));
				}else{
					runs.addAll(adapter.buildAddCommentRun(runtime, update));
				}
				swt = InterceptorProxy.before(runtime, random, DDL.TABLE_COMMENT, table, runs);
				if(swt != SWITCH.BREAK) {
					long cmt_fr = System.currentTimeMillis();
					result = execute(runtime, random, DDL.TABLE_COMMENT, runs) && result;
					InterceptorProxy.after(runtime, random, DDL.TABLE_COMMENT, table, runs, result, System.currentTimeMillis()-cmt_fr);
				}
			}
		}

		Map<String, Column> cols = new LinkedHashMap<>();

		// 更新列
		for (Column ucolumn : ucolumns.values()) {
			//先根据原列名 找到数据库中定义的列
			Column column = columns.get(ucolumn.getName().toUpperCase());
			//再检测update(如果name不一样需要rename)
			if(null != ucolumn.getUpdate()){
				ucolumn = ucolumn.getUpdate();
			}
			if (null != column) {
				// 修改列
				if (!column.equals(ucolumn)) {
					column.setTable(update);
					column.setUpdate(ucolumn, false, false);
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
			if (column.isDrop()) {
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
				if (column.isDrop() || deletes.contains(column.getName().toUpperCase()) || "drop".equals(column.getAction())) {
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
		boolean change_pk = !cur_define.equalsIgnoreCase(src_define);
		//如果主键有更新 先删除主键 避免alters中把原主键列的非空取消时与主键约束冲突
		if(change_pk){
			LinkedHashMap<String,Column> pks = src_primary.getColumns();
			LinkedHashMap<String,Column> npks = cur_primary.getColumns();
			for(String k:pks.keySet()){
				Column auto = columns.get(k.toUpperCase());
				if(null != auto && auto.isAutoIncrement() == 1){//原主键科自增
					if(!npks.containsKey(auto.getName().toUpperCase())){ //当前不是主键
						auto.setPrimaryKey(false);
						result = execute(runtime, random, DDL.TABLE_ALTER, adapter.buildDropAutoIncrement(runtime, auto)) && result;
					}
				}
			}
			//删除主键
			if(null != src_primary){
				drop(src_primary);
			}
		}
		List<Run> alters = adapter.buildAlterRun(runtime, table, cols.values());
		if(null != alters && alters.size()>0){
			result = execute(runtime, random, DDL.COLUMN_ALTER, alters) && result;
		}
		//在alters执行完成后 添加主键 避免主键中存在alerts新添加的列
		if(change_pk){
			//添加主键
			if(null != cur_primary) {
				add(cur_primary);
			}
		}
		CacheProxy.clear();
		return result;
	}

	@Override
	public boolean drop(Table meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TABLE_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	private List<Run> runs(String sql){
		List<Run> runs = new ArrayList<>();
		runs.add(new SimpleRun(sql));
		return runs;
	}
	/**
	 * 重命名
	 * @param origin 原表
	 * @param name 新名称
	 * @return boolean
	 * @throws Exception DDL异常
	 */

	@Override
	public boolean rename(Table origin, String name) throws Exception {
		return rename(null, null, origin, name);
	}

	protected boolean rename(DataRuntime runtime, String random, Table origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.TABLE_RENAME;
		if(null == runtime){
			runtime = runtime();
		}
		if(null == random){
			random = random(runtime);
		}
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													view
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(View view) throws Exception
	 * boolean alter(View view) throws Exception
	 * boolean drop(View view) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(View meta) throws Exception {
		boolean result = false;
		DDL action = DDL.VIEW_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(View meta) throws Exception {
		boolean result = false;
		DDL action = DDL.VIEW_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action,  meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}

	@Override
	public boolean drop(View meta) throws Exception{
		boolean result = false;
		DDL action = DDL.VIEW_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(View origin, String name) throws Exception {
		return rename(runtime(), origin, name);
	}

	protected boolean rename(DataRuntime runtime, View origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.VIEW_RENAME;
		String random = random(runtime);
		origin.setNewName(name);
		if(null == runtime){
			runtime = runtime();
		}
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													master table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(MasterTable table) throws Exception
	 * boolean alter(MasterTable table) throws Exception
	 * boolean drop(MasterTable table) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(MasterTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.MASTER_TABLE_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean alter(MasterTable table) throws Exception{
		SWITCH swt = SWITCH.CONTINUE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		if (null != ddListener) {
			//swt = ddListener.prepareAlter(runtime, random, table);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		boolean result = true;

		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		LinkedHashMap<String, Tag> tags = table.getTags();
		LinkedHashMap<String, Tag> utags = update.getTags();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();

		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, random, table, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
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
				tag.setUpdate(utag, false, false);
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
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[alter master table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(MasterTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.MASTER_TABLE_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(MasterTable origin, String name) throws Exception {
		return rename(runtime(), origin, name);
	}

	public boolean rename(DataRuntime runtime, MasterTable origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.MASTER_TABLE_RENAME;
		if(null == runtime){
			runtime = runtime();
		}
		String random = random(runtime);
		origin.setNewName(name);

		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				swt = InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													partition table
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(PartitionTable table) throws Exception
	 * boolean alter(PartitionTable table) throws Exception
	 * boolean drop(PartitionTable table) throws Exception
	 ******************************************************************************************************************/

	@Override
	public boolean create(PartitionTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PARTITION_TABLE_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getMasterName(), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean alter(PartitionTable table) throws Exception{
		SWITCH swt = SWITCH.CONTINUE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		if (null != ddListener) {
			//swt = ddListener.prepareAlter(runtime, random, table);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		boolean result = true;

		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, table);
		Table update = table.getUpdate();
		LinkedHashMap<String, Column> columns = table.getColumns();
		LinkedHashMap<String, Column> ucolumns = update.getColumns();
		String name = table.getName();
		String uname = update.getName();
		long fr = System.currentTimeMillis();
		if(!name.equalsIgnoreCase(uname)){
			result = rename(runtime, random, table, uname);
		}
		// 更新列
		for(Column ucolumn : ucolumns.values()){
			Column column = columns.get(ucolumn.getName().toUpperCase());
			if(null != column){
				// 修改列
				column.setTable(update);
				column.setUpdate(ucolumn, false, false);
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
		if (null != ddListener) {

		}
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[alter partition table][table:{}][result:{}][执行耗时:{}ms]", random, table.getName(), result, System.currentTimeMillis() - fr);
		}
		return result;
	}
	@Override
	public boolean drop(PartitionTable meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PARTITION_TABLE_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getMasterName(), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(PartitionTable origin, String name) throws Exception {
		return rename(null, null, origin, name);
	}

	protected boolean rename(DataRuntime runtime, String random, PartitionTable origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.PARTITION_TABLE_RENAME;
		origin.setNewName(name);
		if(null == random){
			random = random(runtime);
		}
		if(null == runtime){
			runtime = runtime();
		}
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][master:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getMasterName(), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}
	/* *****************************************************************************************************************
	 * 													column
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Column column) throws Exception
	 * boolean alter(Table table, Column column) throws Exception
	 * boolean alter(Column column) throws Exception
	 * boolean drop(Column column) throws Exception
	 *
	 * private boolean alter(Table table, Column column, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Column meta) throws Exception{
		boolean result = false;
		DDL action = DDL.COLUMN_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Table table, Column column) throws Exception{
		return alter(table, column, true);
	}
	@Override
	public boolean alter(Column column) throws Exception{
		Table table = column.getTable(true);
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(column.getCatalog(), column.getSchema(), column.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + column.getTableName(true));
				}else{
					log.error("表不存在:" + column.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, column, true);
	}
	@Override
	public boolean drop(Column meta) throws Exception{
		boolean result = false;
		DDL action = DDL.COLUMN_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * 修改列
	 * @param meta 列
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	private boolean alter(Table table, Column meta, boolean trigger) throws Exception{
		boolean result = false;
		DDL action = DDL.COLUMN_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta, false);

		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改Column执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改Column执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == SWITCH.CONTINUE) {
					result = alter(table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean rename(Column origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.COLUMN_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													tag
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Tag tag) throws Exception
	 * boolean alter(Table table, Tag tag) throws Exception
	 * boolean alter(Tag tag) throws Exception
	 * boolean drop(Tag tag) throws Exception
	 *
	 * private boolean alter(Table table, Tag tag, boolean trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Tag meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TAG_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
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
		Table table = tag.getTable(true);
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, tag.getCatalog(), tag.getSchema(), tag.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + tag.getTableName(true));
				}else {
					log.error("表不存在:" + tag.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, tag, true);
	}
	@Override
	public boolean drop(Tag meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TAG_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	/**
	 * 修改标签
	 * @param meta 标签
	 * @param trigger 是否触发异常事件
	 * @return boolean
	 * @throws Exception 异常 SQL异常
	 */
	private boolean alter(Table table, Tag meta, boolean trigger) throws Exception{
		boolean result = false;
		DDL action = DDL.TAG_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta, false);
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}catch (Exception e){
			// 如果发生异常(如现在数据类型转换异常) && 有监听器 && 允许触发监听(递归调用后不再触发) && 由数据类型更新引起
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			log.warn("{}[{}][exception:{}]", random, LogUtil.format("修改TAG执行异常", 33), e.toString());
			if(trigger && null != ddListener && !BasicUtil.equalsIgnoreCase(meta.getTypeName(), meta.getUpdate().getTypeName())) {
				if (ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION != 0) {
					swt = ddListener.afterAlterColumnException(runtime, random, table, meta, e);
				}
				log.warn("{}[修改TAG执行异常][尝试修正数据][修正结果:{}]", random, swt);
				if (swt == SWITCH.CONTINUE) {
					result = alter(table, meta, false);
				}
			}else{
				log.error("{}[修改Column执行异常][中断执行]", random);
				result = false;
				throw e;
			}
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(Tag origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.TAG_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													primary
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(PrimaryKey primary) throws Exception
	 * boolean alter(PrimaryKey primary) throws Exception
	 * boolean drop(PrimaryKey primary) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(PrimaryKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.PRIMARY_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(PrimaryKey primary) throws Exception {
		Table table = primary.getTable(true);
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, primary.getCatalog(), primary.getSchema(), primary.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + primary.getTableName(true));
				}else{
					log.error("表不存在:" + primary.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, primary);
	}
	@Override
	public boolean alter(Table table, PrimaryKey meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PRIMARY_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}
	@Override
	public boolean drop(PrimaryKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.PRIMARY_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(PrimaryKey origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.PRIMARY_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													foreign
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(ForeignKey foreign) throws Exception
	 * boolean alter(ForeignKey foreign) throws Exception
	 * boolean drop(PrimaryKey foreign) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(ForeignKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.FOREIGN_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(ForeignKey meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() == 0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, meta);
	}
	@Override
	public boolean alter(Table table, ForeignKey meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TRIGGER_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}

		return result;
	}
	@Override
	public boolean drop(ForeignKey meta) throws Exception {
		boolean result = false;
		DDL action = DDL.FOREIGN_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean rename(ForeignKey origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.FOREIGN_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}
	/* *****************************************************************************************************************
	 * 													index
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Index index) throws Exception
	 * boolean alter(Index index) throws Exception
	 * boolean drop(Index index) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Index meta) throws Exception {
		boolean result = false;
		DDL action = DDL.INDEX_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Index meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, meta);
	}
	@Override
	public boolean alter(Table table, Index index) throws Exception{
		boolean result = false;
		DDL action = DDL.INDEX_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, index);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, index);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, index);
		List<Run> runs = adapter.buildAlterRun(runtime, index);
		swt = InterceptorProxy.before(runtime, random, action, index, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, index, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, index.getTableName(true), index.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, index, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, index, runs, result, millis);
			}
		}

		return result;
	}
	@Override
	public boolean drop(Index meta) throws Exception {
		boolean result = false;
		DDL action = DDL.INDEX_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Index origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.INDEX_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													constraint
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean add(Constraint constraint) throws Exception
	 * boolean alter(Constraint constraint) throws Exception
	 * boolean drop(Constraint constraint) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Constraint meta) throws Exception {
		boolean result = false;
		DDL action = DDL.CONSTRAINT_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAdd(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAddRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAdd(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAdd(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}

	@Override
	public boolean alter(Constraint meta) throws Exception {
		Table table = meta.getTable(true);
		if(null == table){
			LinkedHashMap<String,Table> tables = tables(false, meta.getCatalog(), meta.getSchema(), meta.getTableName(true), "TABLE");
			if(tables.size() ==0){
				if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
					throw new AnylineException("表不存在:" + meta.getTableName(true));
				}else{
					log.error("表不存在:" + meta.getTableName(true));
				}
			}else {
				table = tables.values().iterator().next();
			}
		}
		return alter(table, meta);
	}
	@Override
	public boolean alter(Table table, Constraint meta) throws Exception{
		boolean result = false;
		DDL action = DDL.CONSTRAINT_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Constraint meta) throws Exception {
		boolean result = false;
		DDL action = DDL.CONSTRAINT_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Constraint origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.CONSTRAINT_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);

		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													trigger
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Trigger trigger) throws Exception
	 * boolean alter(Trigger trigger) throws Exception
	 * boolean drop(Trigger trigger) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean add(Trigger meta) throws Exception {
		boolean result = false;
		DDL action = DDL.TRIGGER_ADD;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean alter(Trigger meta) throws Exception{
		boolean result = false;
		DDL action = DDL.TRIGGER_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size()>1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][namer:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Trigger meta) throws Exception {
		boolean result = false;
		DDL action = DDL.TRIGGER_DROP;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getTableName(true), meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Trigger origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.TRIGGER_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][table:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, origin.getTableName(true), origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}


	/* *****************************************************************************************************************
	 * 													procedure
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Procedure procedure) throws Exception
	 * boolean alter(Procedure procedure) throws Exception
	 * boolean drop(Procedure procedure) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Procedure meta) throws Exception {
		boolean result = false;
		DataRuntime runtime = runtime();
		DDL action = DDL.PRIMARY_ADD;
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random,  action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeCreate(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result,  millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean alter(Procedure meta) throws Exception{
		boolean result = false;
		DDL action = DDL.PROCEDURE_ALTER;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareAlter(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeAlter(runtime, random, meta, runs);
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() >1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]", random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Procedure meta) throws Exception {
		boolean result = true;
		DataRuntime runtime = runtime();
		DDL action = DDL.PROCEDURE_DROP;
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Procedure origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.PROCEDURE_RENAME;
		DataRuntime runtime = runtime();
		origin.setNewName(name);
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);
		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null == ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, origin.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}

	/* *****************************************************************************************************************
	 * 													function
	 * -----------------------------------------------------------------------------------------------------------------
	 * boolean create(Function function) throws Exception
	 * boolean alter(Function function) throws Exception
	 * boolean drop(Function function) throws Exception
	 ******************************************************************************************************************/
	@Override
	public boolean create(Function meta) throws Exception {
		boolean result = true;
		DDL action = DDL.FUNCTION_CREATE;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareCreate(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildCreateRun(runtime, meta);
		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			swt = SWITCH.CONTINUE;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}

			if(null != ddListener){
				swt = ddListener.afterCreate(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}


	@Override
	public boolean alter(Function meta) throws Exception{
		boolean result = false;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DDL action = DDL.FUNCTION_ALTER;
		SWITCH swt  = InterceptorProxy.prepare(runtime, random, action, meta);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);
		List<Run> runs = adapter.buildAlterRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt =  ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}

		long fr = System.currentTimeMillis();
		try {
			result = execute(runtime, random, action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterAlter(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE){
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean drop(Function meta) throws Exception {
		boolean result = false;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		DDL action = DDL.FUNCTION_DROP;
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, meta);
		if(swt == SWITCH.BREAK){
			return false;
		}
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareDrop(runtime, random, meta);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, meta);

		List<Run> runs = adapter.buildDropRun(runtime, meta);
		swt = InterceptorProxy.before(runtime, random, action, meta, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeDrop(runtime, random, meta, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random , action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, meta.getName(), runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterDrop(runtime, random, meta, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE) {
				InterceptorProxy.after(runtime, random, action, meta, runs, result, millis);
			}
		}
		return result;
	}
	@Override
	public boolean rename(Function origin, String name) throws Exception {
		boolean result = false;
		DDL action = DDL.FUNCTION_RENAME;
		DataRuntime runtime = runtime();
		String random = random(runtime);
		origin.setNewName(name);
		SWITCH swt = InterceptorProxy.prepare(runtime, random, action, origin);
		if(swt == SWITCH.BREAK){
			return false;
		}
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.prepareRename(runtime, random, origin);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		DriverAdapter adapter = runtime.getAdapter();
		checkSchema(runtime, origin);

		List<Run> runs = adapter.buildRenameRun(runtime, origin);
		swt = InterceptorProxy.before(runtime, random, action, origin, runs);
		if(null != ddListener && swt == SWITCH.CONTINUE){
			swt = ddListener.beforeRename(runtime, random, origin, runs);
		}
		if(swt == SWITCH.BREAK){
			return false;
		}
		long fr = System.currentTimeMillis();
		try{
			result = execute(runtime, random , action, runs);
		}finally {
			long millis = System.currentTimeMillis() - fr;
			if(runs.size() > 1 && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][name:{}][rename:{}][sqls:{}][result:{}][执行耗时:{}ms]" , random, action, origin.getName(), name, runs.size(), result, millis);
			}
			swt = SWITCH.CONTINUE;
			if(null != ddListener){
				swt = ddListener.afterRename(runtime, random, origin, runs, result, millis);
			}
			if(swt == SWITCH.CONTINUE) {
				InterceptorProxy.after(runtime, random, action, origin, runs, result, millis);
			}
		}
		return result;
	}
	public boolean execute(DataRuntime runtime, String random, ACTION.DDL action, Run run){
		if(null == run){
			return false;
		}
		boolean result = false;
		String sql = run.getFinalUpdate();
		if(BasicUtil.isNotEmpty(sql)) {
			Long fr = System.currentTimeMillis();
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				random = random(runtime);
				log.info("{}[action:{}][ds:{}][sql:\n{}\n]", random, action, runtime.datasource(), sql);
			}
			//runtime.getTemplate().update(sql);
			runtime.getAdapter().update(runtime, random, null, null, run);
			result = true;
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[action:{}][ds:{}][result:{}][执行耗时:{}ms]", random, action, runtime.datasource(), result, System.currentTimeMillis() - fr);
			}
		}
		return result;
	}
	public boolean execute(DataRuntime runtime, String random, ACTION.DDL action, List<Run> runs){
		boolean result = true;
		int idx = 0;
		for(Run run:runs){
			result = execute(runtime, random + "-" + idx++, action, run) && result;
		}
		return result;
	}
	/* *****************************************************************************************************************
	 *
	 * 													common
	 *
	 * -----------------------------------------------------------------------------------------------------------------
	 * void checkSchema(DataRuntime runtime, Table table)
	 * protected String LogUtil.param(List<?> params)
	 * protected String LogUtil.param(List<?> keys, List<?> values)
	 * private static String random()
	 ******************************************************************************************************************/

	public void checkSchema(DataRuntime runtime, Table table){
		if(null != table){
			DriverAdapter adapter = runtime.getAdapter();
			adapter.checkSchema(runtime, table);
		}
	}
	public void checkSchema(DataRuntime runtime, Column column){
		Table table = column.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
			column.setCatalog(table.getCatalog());
			column.setSchema(table.getSchema());
		}
	}
	public void checkSchema(DataRuntime runtime, Index index){
		Table table = index.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
			index.setCatalog(table.getCatalog());
			index.setSchema(table.getSchema());
		}
	}
	public void checkSchema(DataRuntime runtime, Constraint constraint){
		Table table = constraint.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
			constraint.setCatalog(table.getCatalog());
			constraint.setSchema(table.getSchema());
		}
	}
	public void checkSchema(DataRuntime runtime, Trigger trigger){
		Table table = (Table)trigger.getTable(true);
		if(null != table){
			checkSchema(runtime, table);
		}
	}

	public void checkSchema(DataRuntime runtime, Procedure procedure){
		Table table = new Table(procedure.getCatalog(), procedure.getSchema());
		checkSchema(runtime, table);
		procedure.setCatalog(table.getCatalog());
		procedure.setSchema(table.getSchema());
	}

	public void checkSchema(DataRuntime runtime, Function function){
		Table table = new Table(function.getCatalog(), function.getSchema());
		checkSchema(runtime, table);
		function.setCatalog(table.getCatalog());
		function.setSchema(table.getSchema());
	}



	private String random(DataRuntime runtime){
		StringBuilder builder = new StringBuilder();
		builder.append("[SQL:").append(System.currentTimeMillis()).append("-").append(BasicUtil.getRandomNumberString(8))
				.append("][thread:")
				.append(Thread.currentThread().getId()).append("][ds:").append(runtime.datasource()).append("]");
		return builder.toString();
	}

}
