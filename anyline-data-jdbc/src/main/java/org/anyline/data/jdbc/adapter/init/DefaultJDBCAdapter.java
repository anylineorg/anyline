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


package org.anyline.data.jdbc.adapter.init;


import org.anyline.adapter.PersistenceAdapter;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.adapter.init.DefaultDriverAdapter;
import org.anyline.data.jdbc.runtime.JDBCRuntime;
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
import org.anyline.data.util.ClientHolder;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.*;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.persistence.ManyToMany;
import org.anyline.metadata.persistence.OneToMany;
import org.anyline.metadata.type.ColumnType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.Date;
import java.util.*;


/**
 * SQL生成 子类主要实现与分页相关的SQL 以及delimiter
 */

public abstract class DefaultJDBCAdapter extends DefaultDriverAdapter implements JDBCAdapter {
	protected static final Logger log = LoggerFactory.getLogger(DefaultJDBCAdapter.class);

	@Autowired(required = false)
	protected DMListener dmListener;
	@Autowired(required = false)
	protected DDListener ddListener;

	public DMListener getListener() {
		return dmListener;
	}

	@Autowired(required=false)
	public void setListener(DMListener listener) {
		this.dmListener = listener;
	}

	protected JdbcTemplate jdbc(DataRuntime runtime){
		Object client = runtime.getClient();
		return (JdbcTemplate) client;
	}
	@Override
	public long total(DataRuntime runtime, String random, Run run) {
		long total = 0;
		DataSet set = select(runtime, random, false, null, run, run.getTotalQuery(), run.getValues());
		total = set.getInt(0,"CNT",0);
		return total;
	}

	/**
	 * 封装查询结果
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	@Override
	public DataRow row(boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas, ResultSet rs) {
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
					column = adapter.column(runtime, (Column) column, rsmd, i);
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
	private DataSet select(DataRuntime runtime, String random, boolean system, String table, Run run,  String sql, List<Object> values){
		if(BasicUtil.isEmpty(sql)){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new DataSet();
			}
		}
		long fr = System.currentTimeMillis();
		if(null == random){
			random = random(runtime);
		}
		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		DataSet set = new DataSet();
		//根据这一步中的JDBC结果集检测类型不准确,如:实际POINT 返回 GEOMETRY 如果要求准确 需要开启到自动检测
		//在DataRow中 如果检测到准确类型 JSON XML POINT 等 返回相应的类型,不返回byte[]（所以需要开启自动检测）
		//Entity中 JSON XML POINT 等根据属性类型返回相应的类型（所以不需要开启自动检测）
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();
		if(!system && ThreadConfig.check(runtime.getKey()).IS_AUTO_CHECK_METADATA() && null != table){
			columns = columns(runtime,  false, new Table( table), false);
		}
		try{
			final DataRuntime rt = runtime;
			final long[] mid = {System.currentTimeMillis()};
			final boolean[] process = {false};
			final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
			metadatas.putAll(columns);
			set.setMetadatas(metadatas);
			JdbcTemplate jdbc = jdbc(runtime);
			if(null != values && values.size()>0){
				jdbc.query(sql, values.toArray(), new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						if(!process[0]){
							mid[0] = System.currentTimeMillis();
						}
						DataRow row = row(system, rt, metadatas, rs);
						set.add(row);
						process[0] = true;
					}
				});
			}else {
				jdbc.query(sql, new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						if(!process[0]){
							mid[0] = System.currentTimeMillis();
						}
						DataRow row = row(system, rt, metadatas, rs);
						set.add(row);
						process[0] = true;
					}
				});
			}
			if(!process[0]){
				mid[0] = System.currentTimeMillis();
			}
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				slow = true;
				if(mid[0] - fr > SLOW_SQL_MILLIS){
					log.warn("{}[SLOW SQL][action:select][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, mid[0] - fr, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, set,mid[0] - fr);
					}
				}
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[执行耗时:{}ms]", random, mid[0] - fr);
			}
			set.setDatalink(runtime.datasource());
			if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], set.size());
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), sql, LogUtil.param(values));
				}
			}
		}
		return set;
	}

	@Override
	public DataSet select(DataRuntime runtime, String random, boolean system, String table, Run run) {
		String sql = run.getFinalQuery();
		List<Object> values = run.getValues();
		return select(runtime, random, system, table, run, sql, values);
	}
	/*
	public DataSet select(DataRuntime runtime, String random, boolean system, String table, Run run){
		String sql = run.getFinalQuery();
		List<Object> values = run.getValues();
		if(BasicUtil.isEmpty(sql)){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new DataSet();
			}
		}
		long fr = System.currentTimeMillis();
		if(null == random){
			random = random(runtime);
		}
		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		DataSet set = new DataSet();
		//根据这一步中的JDBC结果集检测类型不准确,如:实际POINT 返回 GEOMETRY 如果要求准确 需要开启到自动检测
		//在DataRow中 如果检测到准确类型 JSON XML POINT 等 返回相应的类型,不返回byte[]（所以需要开启自动检测）
		//Entity中 JSON XML POINT 等根据属性类型返回相应的类型（所以不需要开启自动检测）
		LinkedHashMap<String,Column> columns = new LinkedHashMap<>();

		if(!system && ThreadConfig.check(runtime.getKey()).IS_AUTO_CHECK_METADATA() && null != table){
			columns = columns(runtime, false, new Table(null, null, table), null);
		}
		try{
			final DataRuntime rt = runtime;
			final long[] mid = {System.currentTimeMillis()};
			final boolean[] process = {false};
			final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
			metadatas.putAll(columns);
			set.setMetadatas(metadatas);
			JdbcTemplate jdbc = jdbc(runtime);
			if(null != values && values.size()>0){
				jdbc.query(sql, values.toArray(), new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						if(!process[0]){
							mid[0] = System.currentTimeMillis();
						}
						DataRow row = row(system, rt, metadatas, rs);
						set.add(row);
						process[0] = true;
					}
				});
			}else {
				jdbc.query(sql, new RowCallbackHandler() {
					@Override
					public void processRow(ResultSet rs) throws SQLException {
						if(!process[0]){
							mid[0] = System.currentTimeMillis();
						}
						DataRow row = row(system, rt, metadatas, rs);
						set.add(row);
						process[0] = true;
					}
				});
			}
			if(!process[0]){
				mid[0] = System.currentTimeMillis();
			}
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				slow = true;
				if(mid[0] - fr > SLOW_SQL_MILLIS){
					log.warn("{}[SLOW SQL][action:select][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, mid[0] - fr, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, set,mid[0] - fr);
					}
				}
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[执行耗时:{}ms]", random, mid[0] - fr);
			}
			set.setDatalink(runtime.datasource());
			if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], set.size());
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), sql, LogUtil.param(values));
				}
			}
		}
		return set;
	}*/

	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, RunPrepare prepare, ConfigStore configs, String ... conditions){
		List<Map<String,Object>> maps = null;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean sql_success = false;
		Run run = null;
		String random = random(runtime);
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return new ArrayList<>();
		}

		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return new ArrayList<>();
		}

		DriverAdapter adapter = runtime.getAdapter();
		run = adapter.buildQueryRun(runtime, prepare, configs, conditions);
		Long fr = System.currentTimeMillis();
			if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
				String tmp = "[valid:false][不具备执行条件]";
				String src = "";
				if (prepare instanceof TablePrepare) {
					src = prepare.getTable();
				} else {
					src = prepare.getText();
				}
				tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
				log.warn(tmp);
			}
			if (run.isValid()) {
				swt = InterceptorProxy.beforeQuery(runtime, random,  run, null);
				if(swt == ACTION.SWITCH.BREAK){
					return new ArrayList<>();
				}
				if (null != dmListener) {
					dmListener.beforeQuery(runtime, random, run, -1);
				}
				maps = maps(runtime, random, run);
				if (null != adapter) {
					maps = adapter.process(runtime, maps);
				}
				sql_success = true;
			} else {
				maps = new ArrayList<>();
			}

		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, sql_success, maps, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, sql_success, maps, null,System.currentTimeMillis() - fr);
		return maps;
	}
	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, String random, Run run){
		List<Map<String,Object>> maps = null;
		String sql = run.getFinalQuery();
		List<Object> values = run.getValues();
		if(BasicUtil.isEmpty(sql)){
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
				throw new SQLQueryException("未指定SQL");
			}else{
				log.error("未指定SQL");
				return new ArrayList<>();
			}
		}
		long fr = System.currentTimeMillis();
		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			if(null != values && values.size()>0){
				maps = jdbc.queryForList(sql, values.toArray());
			}else{
				maps = jdbc.queryForList(sql);
			}
			long mid = System.currentTimeMillis();
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(mid-fr > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:select][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, mid-fr, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT,null, sql, values, null, true, maps, mid);
					}
				}
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[执行耗时:{}ms]", random, mid - fr);
			}
			maps = process(runtime, maps);
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid, maps.size());
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(), e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e.toString(), sql, LogUtil.param(values));
				}
			}
		}
		return maps;
	}
	@Override
	public Map<String,Object> map(DataRuntime runtime, String random, Run run){
		Map<String, Object> map = null;
		String sql = run.getFinalExists();
		List<Object> values = run.getValues();

		long fr = System.currentTimeMillis();
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		/*if(null != values && values.size()>0 && BasicUtil.isEmpty(true, values)){
			//>0:有占位 isEmpty:值为空
		}else{*/
		try {
			JdbcTemplate jdbc = jdbc(runtime);
			if (null != values && values.size() > 0) {
				map = jdbc.queryForMap(sql, values.toArray());
			} else {
				map = jdbc.queryForMap(sql);
			}
		}catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION) {
				throw e;
			}else if (ConfigTable.IS_SHOW_SQL_WHEN_ERROR) {
				log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("查询异常:", 33)+e, sql, LogUtil.param(values));
			}
		}
		//}
		Long millis = System.currentTimeMillis() - fr;
		boolean slow = false;
		long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
		if(SLOW_SQL_MILLIS > 0){
			if(millis > SLOW_SQL_MILLIS){
				slow = true;
				log.warn("{}[SLOW SQL][action:exists][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
				if(null != dmListener){
					dmListener.slow(runtime, random, ACTION.DML.EXISTS, run, sql,  values, null, true, map, millis);
				}
			}
		}
		if (!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[执行耗时:{}ms][封装行数:{}]", random, millis, LogUtil.format(map == null ?0:1, 34));
		}
		return map;
	}
	@Override
	public int update(DataRuntime runtime, String random, String dest, Object data, Run run){
		int result = 0;
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][dest:"+dest+"]");
			}
			return -1;
		}
		String sql = run.getFinalUpdate();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备更新条件][dest:{}]",dest);
			return -1;
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		long millis = -1;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			result = jdbc.update(sql, values.toArray());
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:update][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.UPDATE, run, sql, values, null, true , result, millis);
					}
				}
			}
			if (!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(result, 34));
			}

		}catch(Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if (ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION) {
				SQLUpdateException ex = new SQLUpdateException("update异常:" + e.toString(), e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			} else {
				if (ConfigTable.IS_SHOW_SQL_WHEN_ERROR) {
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("更新异常:", 33) + e.toString(), sql, LogUtil.param(run.getUpdateColumns(), values));
				}
			}
		}
		return result;
	}

	/**
	 * 执行 insert
	 * @param runtime 运行环境主要包含适配器数据源或客户端
	 * @param random random
	 * @param data entity|DataRow|DataSet
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks pks
	 * @return int 影响行数
	 * @throws Exception 异常
	 */
	@Override
	public int insert(DataRuntime runtime, String random, Object data, Run run, String[] pks) {
		int cnt = 0;

		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][dest:"+run.getTable()+"]");
			}
			return -1;
		}
		String sql = run.getFinalInsert();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备执行条件][dest:{}]",run.getTable());
			return -1;
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		long millis = -1;

		KeyHolder keyholder = new GeneratedKeyHolder();
		JdbcTemplate jdbc = jdbc(runtime);
		try {
			cnt = jdbc.update(new PreparedStatementCreator() {
				@Override
				public PreparedStatement createPreparedStatement(Connection con) throws java.sql.SQLException {
					PreparedStatement ps = null;
					if (null != pks && pks.length > 0) {
						//返回多个值
						ps = con.prepareStatement(sql, pks);
					} else {
						ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
					}
					int idx = 0;
					if (null != values) {
						for (Object obj : values) {
							ps.setObject(++idx, obj);
						}
					}
					return ps;
				}
			}, keyholder);

			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:insert][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.INSERT, run, sql, values, null, true, cnt, millis);
					}
				}
			}
			if (!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(cnt, 34));
			}
			identity(runtime, random, data, keyholder);
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("插入异常:", 33)+e.toString(), sql, LogUtil.param(run.getInsertColumns(),values));
				}
			}
		}
		return cnt;
	}
	//有些不支持返回自增的单独执行
	@Override
	public int insert(DataRuntime runtime, String random, Object data, Run run, String[] pks, boolean simple) {
		int cnt = 0;

		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][dest:"+run.getTable()+"]");
			}
			return -1;
		}
		String sql = run.getFinalInsert();
		if(BasicUtil.isEmpty(sql)){
			log.warn("[不具备执行条件][dest:{}]",run.getTable());
			return -1;
		}
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		/*执行SQL*/
		if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		long millis = -1;

		try {

			JdbcTemplate jdbc = jdbc(runtime);
			if (null == values || values.isEmpty()) {
				cnt = jdbc.update(sql);
			} else {
				int size = values.size();
				Object[] params = new Object[size];
				for (int i = 0; i < size; i++) {
					params[i] = values.get(i);
				}
				cnt = jdbc.update(sql, params);
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:insert][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.INSERT, run, sql, values, null, true, cnt, millis);
					}
				}
			}
			if (!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(cnt, 34));
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("insert异常:"+e.toString(),e);
				ex.setSql(sql);
				ex.setValues(values);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]", random, LogUtil.format("插入异常:", 33)+e.toString(), sql, LogUtil.param(run.getInsertColumns(),values));
				}
			}
		}
		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(run.getInsertColumns(),values));
		}
		return cnt;
	}
	@Override
	public int execute(DataRuntime runtime, String random, Run run){
		int result = -1;
		String sql = run.getFinalExecute();
		List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[param:{}]", random, sql, LogUtil.param(values));
		}
		long millis = -1;
		try{
 			JdbcTemplate jdbc = jdbc(runtime);
			if (null != values && values.size() > 0) {
				result = jdbc.update(sql, values.toArray());
			} else {
				result = jdbc.update(sql);
			}
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:execute][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, millis, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.EXECUTE, run, sql, values, null, true, result, millis);
					}
				}
			}
			if (!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[执行耗时:{}ms][影响行数:{}]", random, millis, LogUtil.format(result, 34));
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				throw e;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[param:{}]" , random, LogUtil.format("SQL执行异常:", 33)+e, sql, LogUtil.param(values));
				}
			}
		}
		return result;
	}

	@Override
	public boolean execute(DataRuntime runtime, String random, Procedure procedure){
		boolean result = false;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean sql_success = false;
		List<Object> list = new ArrayList<Object>();
		final List<Parameter> inputs = procedure.getInputs();
		final List<Parameter> outputs = procedure.getOutputs();
		long fr = System.currentTimeMillis();
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

		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, sql, LogUtil.param(inputs), LogUtil.param(outputs));
		}
		long millis= -1;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			list = (List<Object>) jdbc.execute(sql, new CallableStatementCallback<Object>() {
				public Object doInCallableStatement(final CallableStatement cs) throws SQLException, DataAccessException {
					final List<Object> result = new ArrayList<Object>();
					// 带有返回参数
					int returnIndex = 0;
					if (procedure.hasReturn()) {
						returnIndex = 1;
						cs.registerOutParameter(1, Types.VARCHAR);
					}
					for (int i = 1; i <= sizeIn; i++) {
						Parameter param = inputs.get(i - 1);
						Object value = param.getValue();
						if (null == value || "NULL".equalsIgnoreCase(value.toString())) {
							value = null;
						}
						cs.setObject(i + returnIndex, value, param.getType());
					}
					for (int i = 1; i <= sizeOut; i++) {
						Parameter param = outputs.get(i - 1);
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

			sql_success = true;
			procedure.setResult(list);
			result = true;
			millis = System.currentTimeMillis() - fr;

			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[SLOW SQL][action:procedure][millis:{}ms][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, millis, sql, LogUtil.param(inputs), LogUtil.param(list));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.PROCEDURE,null, sql, inputs,  list, true, result, millis);
					}
				}
			}
			if (null != dmListener) {
				dmListener.afterExecute(runtime, random, procedure, result, millis);
			}
			if (!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[执行耗时:{}ms]\n[output param:{}]", random, millis, list);
			}

		}catch(Exception e){
			result = false;
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				SQLUpdateException ex = new SQLUpdateException("execute异常:"+e.toString(),e);
				ex.setSql(sql);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[input param:{}]\n[output param:{}]", random, LogUtil.format("存储过程执行异常:", 33)+e.toString(), sql, LogUtil.param(inputs), LogUtil.param(outputs));
				}
			}
		}
		return result;
	}
	@Override
	public DataSet querys(DataRuntime runtime, String random, Procedure procedure, PageNavi navi){
		DataSet set = null;
		final List<Parameter> inputs = procedure.getInputs();
		final List<Parameter> outputs = procedure.getOutputs();
		if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
			log.info("{}[sql:\n{}\n][input param:{}]\n[output param:{}]", random, procedure.getName(), LogUtil.param(inputs), LogUtil.param(outputs));
		}
		final String rdm = random;
		long millis = -1;
		try{
			/*if(null != queryInterceptor){
				int exe = queryInterceptor.before(procedure);
				if(exe == -1){
					return new DataSet();
				}
			}*/
			ACTION.SWITCH swt = InterceptorProxy.beforeQuery(runtime, random, procedure, navi);
			if(swt == ACTION.SWITCH.BREAK){
				return new DataSet();
			}
			if(null != dmListener){
				dmListener.beforeQuery(runtime, random, procedure);
			}
			final DataRuntime rt = runtime;
			JdbcTemplate jdbc = jdbc(runtime);
			long fr = System.currentTimeMillis();
			set = (DataSet) jdbc.execute(new CallableStatementCreator(){
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
						Parameter param = inputs.get(i-1);
						Object value = param.getValue();
						if(null == value || "NULL".equalsIgnoreCase(value.toString())){
							value = null;
						}
						cs.setObject(i, value, param.getType());
					}
					for(int i=1; i<=sizeOut; i++){
						Parameter param = outputs.get(i-1);
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
					long first = -1;
					long last = -1;
					if(null != navi){
						first = navi.getFirstRow();
						last = navi.getLastRow();
					}
					while(rs.next()){
						if(first ==-1 || (index >= first && index <= last)){
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
							if(first ==0 && last==0){// 只取一行
								break;
							}
						}
					}
					if(null != navi){
						navi.setTotalRow(index);
						set.setNavi(navi);
					}

					set.setDatalink(rt.datasource());
					if(ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
						log.info("{}[封装耗时:{}ms][封装行数:{}]", rdm, System.currentTimeMillis() - mid,set.size());
					}
					return set;
				}
			});
			millis = System.currentTimeMillis() - fr;
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(millis > SLOW_SQL_MILLIS){
					log.warn("{}[SLOW SQL][action:procedure][millis:{}ms][sql:\n{}\n][input param:{}]\n[output param:{}]"
							, random
							, millis
							, procedure.getName()
							, LogUtil.param(inputs)
							, LogUtil.param(outputs));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.PROCEDURE, null, procedure.getName(), inputs, outputs, true, set, millis);
					}
				}
			}
/*			if(null != queryInterceptor){
				queryInterceptor.after(procedure, set, millis);
			}*/
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[执行耗时:{}ms]", random, millis);
			}
		}catch(Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}
			if(ConfigTable.IS_THROW_SQL_QUERY_EXCEPTION){
				SQLQueryException ex = new SQLQueryException("query异常:"+e.toString(),e);
				throw ex;
			}else{
				if(ConfigTable.IS_SHOW_SQL_WHEN_ERROR){
					log.error("{}[{}][sql:\n{}\n]\n[input param:{}]\n[output param:{}]"
							, random
							, LogUtil.format("存储过程查询异常:", 33)+e.toString()
							, procedure.getName()
							, LogUtil.param(inputs)
							, LogUtil.param(outputs));
				}
			}
		}
		return set;
	}
	@Override
	public DataSet querys(DataRuntime runtime, String random,  RunPrepare prepare, ConfigStore configs, String ... conditions){
		DataSet set = null;
		Long fr = System.currentTimeMillis();
		boolean sql_success = false;
		Run run = null;
		PageNavi navi = null;

		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return new DataSet();
		}
		//query拦截
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return new DataSet();
		}

		run = buildQueryRun(runtime, prepare, configs, conditions);

		if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled() && !run.isValid()) {
			String tmp = "[valid:false][不具备执行条件]";
			String src = "";
			if (prepare instanceof TablePrepare) {
				src = prepare.getTable();
			} else {
				src = prepare.getText();
			}
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, src, configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
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
				if(swt == ACTION.SWITCH.BREAK){
					return new DataSet();
				}
				set = select(runtime, random, false, prepare.getTable(), run);
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

		if(null != dmListener){
			dmListener.afterQuery(runtime, random, run, sql_success, set, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, sql_success, set, navi, System.currentTimeMillis() - fr);
		return set;
	}
	@Override
	public <T> EntitySet<T> selects(DataRuntime runtime, String random, RunPrepare prepare, Class<T> clazz, ConfigStore configs, String ... conditions){
		if(null == prepare){
			prepare = new DefaultTablePrepare();
		}
		EntitySet<T> list = null;
		Long fr = System.currentTimeMillis();
		Run run = null;
 		boolean sql_success = false;
		PageNavi navi = null;

		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return new EntitySet();
		}
		swt = InterceptorProxy.prepareQuery(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return new EntitySet();
		}

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
			tmp += "[RunPrepare:" + ConfigParser.createSQLSign(false, false, clazz.getName(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]";
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
						total = adapter.total(runtime, random, run);
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
				if(swt == ACTION.SWITCH.BREAK){
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

		if (null != dmListener) {
			dmListener.afterQuery(runtime, random, run, sql_success, list, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterQuery(runtime, random, run, sql_success, list, navi, System.currentTimeMillis() - fr);
		return list;
	}

	/**
	 * 查询
	 * @param runtime 运行环境主要包含适配器数据源或客户端
	 * @param clazz entity class
	 * @param table table
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param dependency 是否加载依赖 >0时加载
	 * @return EntitySet
	 * @param <T> entity.class
	protected <T> EntitySet<T> select(DataRuntime runtime, String random,  Class<T> clazz, String table,  String sql, List<Object> values, int dependency){
	 */
	protected <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, String table, Run run, int dependency){
		EntitySet<T> set = new EntitySet<>();

		if(null == random){
			random = random(runtime);
		}

		DataSet rows = select(runtime, random, false, table, run);
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
							DataSet items = querys(runtime, random, new DefaultTablePrepare(join.joinTable), new DefaultConfigStore(), "++" + join.joinColumn + ":" + primaryValueMap.get(pk.toUpperCase()));
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
						DataSet allItems = querys(runtime, random,   new DefaultTablePrepare(join.joinTable), conditions);
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
						DataSet alls = querys(runtime, random,   new DefaultTextPrepare(sql), conditions);
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
						EntitySet<T> dependencys = selects(runtime, random, null, join.dependencyClass, new DefaultConfigStore().and(join.joinColumn, pv));
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
					EntitySet<T> alls = selects(runtime, random, null, join.dependencyClass, conditions);
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
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexs, Table table, boolean unique, boolean approximate) throws Exception{
		DataSource ds = null;
		Connection con = null;
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		JdbcTemplate jdbc = jdbc(runtime);
		try{
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet set = dbmd.getIndexInfo(table.getCatalog(), table.getSchema(), table.getName(), unique, approximate);
			Map<String, Integer> keys = keys(set);
			LinkedHashMap<String, Column> columns = null;
			while (set.next()) {
				String name = string(keys, "INDEX_NAME", set);
				if(null == name){
					continue;
				}
				T index = indexs.get(name.toUpperCase());
				if(null == index){
					if(create){
						index = (T)new Index();
						indexs.put(name.toUpperCase(), index);
					}else{
						continue;
					}
					index.setName(string(keys, "INDEX_NAME", set));
					//index.setType(integer(keys, "TYPE", set, null));
					index.setUnique(!bool(keys, "NON_UNIQUE", set, false));
					index.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), table.getCatalog()));
					index.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), table.getSchema()));
					index.setTable(string(keys, "TABLE_NAME", set));
					indexs.put(name.toUpperCase(), index);
					columns = new LinkedHashMap<>();
					index.setColumns(columns);
					if(name.equalsIgnoreCase("PRIMARY")){
						index.setCluster(true);
						index.setPrimary(true);
					}else if(name.equalsIgnoreCase("PK_"+table.getName())){
						index.setCluster(true);
						index.setPrimary(true);
					}
				}else {
					columns = index.getColumns();
				}
				String columnName = string(keys, "COLUMN_NAME", set);
				Column col = table.getColumn(columnName.toUpperCase());
				Column column = null;
				if(null != col){
					column = (Column) col.clone();
				}else{
					column = new Column();
					column.setName(columnName);
				}
				String order = string(keys, "ASC_OR_DESC", set);
				if(null != order && order.startsWith("D")){
					order = "DESC";
				}else{
					order = "ASC";
				}
				column.setOrder(order);
				column.setPosition(integer(keys,"ORDINAL_POSITION", set, null));
				columns.put(column.getName().toUpperCase(), column);
			}
		}finally{
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return indexs;
	}

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, boolean create, LinkedHashMap<String, T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		DataSource ds = null;
		Connection con = null;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet set = dbmd.getTables( catalog, schema, pattern, types);
			if(null == tables){
				tables = new LinkedHashMap<>();
			}
			Map<String,Integer> keys = keys(set);
			while(set.next()) {
				String tableName = string(keys, "TABLE_NAME", set);

				if(BasicUtil.isEmpty(tableName)){
					tableName = string(keys, "NAME", set);
				}
				if(BasicUtil.isEmpty(tableName)){
					continue;
				}
				T table = tables.get(tableName.toUpperCase());
				if(null == table){
					if(create){
						table = (T)new Table();
						tables.put(tableName.toUpperCase(), table);
					}else{
						continue;
					}
				}
				table.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalog));
				table.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schema));
				table.setName(tableName);
				table.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), table.getType()));
				table.setComment(BasicUtil.evl(string(keys, "REMARKS", set), table.getComment()));
				table.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), table.getTypeCat()));
				table.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), table.getTypeName()));
				table.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), table.getSelfReferencingColumn()));
				table.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), table.getRefGeneration()));
				tables.put(tableName.toUpperCase(), table);

				// table_map.put(table.getType().toUpperCase()+"_"+tableName.toUpperCase(), tableName);
			}
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}

	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, boolean create, LinkedHashMap<String, T> views, String catalog, String schema, String pattern, String ... types) throws Exception{
		DataSource ds = null;
		Connection con = null;
		try {
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();

			ResultSet set = dbmd.getTables(catalog, schema, pattern, new String[]{"VIEW"});

			if (null == views) {
				views = new LinkedHashMap<>();
			}
			Map<String, Integer> keys = keys(set);
			while (set.next()) {
				String viewName = string(keys, "TABLE_NAME", set);

				if (BasicUtil.isEmpty(viewName)) {
					viewName = string(keys, "NAME", set);
				}
				if (BasicUtil.isEmpty(viewName)) {
					continue;
				}
				T view = views.get(viewName.toUpperCase());
				if (null == view) {
					if (create) {
						view = (T) new View();
						views.put(viewName.toUpperCase(), view);
					} else {
						continue;
					}
				}
				view.setCatalog(BasicUtil.evl(string(keys, "TABLE_CAT", set), catalog));
				view.setSchema(BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schema));
				view.setName(viewName);
				view.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), view.getType()));
				view.setComment(BasicUtil.evl(string(keys, "REMARKS", set), view.getComment()));
				view.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), view.getTypeCat()));
				view.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), view.getTypeName()));
				view.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), view.getSelfReferencingColumn()));
				view.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), view.getRefGeneration()));
				views.put(viewName.toUpperCase(), view);
			}
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return  views;
	}




	@Override
	public void checkSchema(DataRuntime runtime, DataSource dataSource, Table table){
		if(null == table || null != table.getCheckSchemaTime()){
			return;
		}
		Connection con = null;
		try {
			if (null == table.getCatalog() || null == table.getSchema()) {
				con = DataSourceUtils.getConnection(dataSource);
				checkSchema(runtime, con, table);
			}
		}catch (Exception e){
			log.warn("[check schema][fail:{}]", e.toString());
		}finally {
			if(null != con && !DataSourceUtils.isConnectionTransactional(con, dataSource)){
				DataSourceUtils.releaseConnection(con, dataSource);
			}
		}
	}

	@Override
	public void checkSchema(DataRuntime runtime, Connection con, Table table){
		try {
			if (null == table.getCatalog()) {
				table.setCatalog(con.getCatalog());
			}
			if (null == table.getSchema()) {
				table.setSchema(con.getSchema());
			}
		}catch (Exception e){
		}
		table.setCheckSchemaTime(new Date());
	}
	@Override
	public void checkSchema(DataRuntime runtime, Table table){
		JdbcTemplate jdbc = jdbc(runtime);
		checkSchema(runtime, jdbc.getDataSource(), table);
	}
	@Override
	public Column column(DataRuntime runtime, Column column, SqlRowSetMetaData rsm, int index){
		if(null == column) {
			column = new Column();
			try {
				column.setCatalog(BasicUtil.evl(rsm.getCatalogName(index)));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getCatalogName]");
			}
			try {
				column.setSchema(BasicUtil.evl(rsm.getSchemaName(index)));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getSchemaName]");
			}
			try {
				column.setClassName(rsm.getColumnClassName(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnClassName]");
			}
			try {
				column.setCurrency(rsm.isCurrency(index));
			} catch (Exception e) {
				column.setCaseSensitive(rsm.isCaseSensitive(index));
				log.debug("[获取MetaData失败][驱动未实现:isCurrency]");
			}
			try {
				column.setOriginalName(rsm.getColumnName(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnName]");
			}
			try {
				column.setName(rsm.getColumnLabel(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnLabel]");
			}
			try {
				column.setPrecision(rsm.getPrecision(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getPrecision]");
			}
			try {
				column.setScale(rsm.getScale(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getScale]");
			}
			try {
				column.setDisplaySize(rsm.getColumnDisplaySize(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnDisplaySize]");
			}
			try {
				column.setSigned(rsm.isSigned(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:isSigned]");
			}
			try {
				column.setTable(rsm.getTableName(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getTableName]");
			}
			try {
				column.setType(rsm.getColumnType(index));
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnType]");
			}
			try {
				String jdbcType = rsm.getColumnTypeName(index);
				column.setJdbcType(jdbcType);
				if(BasicUtil.isEmpty(column.getTypeName())) {
					column.setTypeName(jdbcType);
				}
			} catch (Exception e) {
				log.debug("[获取MetaData失败][驱动未实现:getColumnTypeName]");
			}

			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
		}
		return column;
	}
	@Override
	public Column column(DataRuntime runtime, Column column, ResultSetMetaData rsm, int index){
		if(null == column){
			column = new Column();
		}
		try{
			column.setCatalog(BasicUtil.evl(rsm.getCatalogName(index)));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getCatalogName]");
		}
		try{
			column.setSchema(BasicUtil.evl(rsm.getSchemaName(index)));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getSchemaName]");
		}
		try{
			column.setClassName(rsm.getColumnClassName(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnClassName]");
		}
		try{
			column.setCaseSensitive(rsm.isCaseSensitive(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:isCaseSensitive]");
		}
		try{
			column.setCurrency(rsm.isCurrency(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:isCurrency]");
		}
		try{
			column.setOriginalName(rsm.getColumnName(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnName]");
		}
		try{
			column.setName(rsm.getColumnLabel(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnLabel]");
		}
		try{
			column.setPrecision(rsm.getPrecision(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getPrecision]");
		}
		try{
			column.setScale(rsm.getScale(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getScale]");
		}
		try{
			column.setDisplaySize(rsm.getColumnDisplaySize(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnDisplaySize]");
		}
		try{
			column.setSigned(rsm.isSigned(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:isSigned]");
		}
		try{
			column.setTable(rsm.getTableName(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getTableName]");
		}
		try {
			column.setType(rsm.getColumnType(index));
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnType]");
		}
		try {
			//不准确 POINT 返回 GEOMETRY
			String jdbcType = rsm.getColumnTypeName(index);
			column.setJdbcType(jdbcType);
			if(BasicUtil.isEmpty(column.getTypeName())) {
				column.setTypeName(jdbcType);
			}
		}catch (Exception e){
			log.debug("[获取MetaData失败][驱动未实现:getColumnTypeName]");
		}
		ColumnType columnType = type(column.getTypeName());
		column.setColumnType(columnType);
		return column;
	}


	/**
	 * 1 构造查询列的QL
	 * @param table 表
	 * @param metadata 是否根据metadata(true:1=0,false:查询系统表,由子类实现)
	 * @return sql
	 */
	@Override
	public List<Run> buildQueryColumnRun(DataRuntime runtime, Table table, boolean metadata) throws Exception{
		List<Run> runs = new ArrayList<>();
		Run run = new SimpleRun();
		runs.add(run);
		StringBuilder builder = run.getBuilder();
		if(metadata){
			builder.append("SELECT * FROM ");
			name(runtime, builder, table);
			builder.append(" WHERE 1=0");
		}
		return runs;
	}

	/**
	 * 3.1 根据查询结果解析列属性
	 * @param index 第几条SQL 对照 buildQueryColumnRun返回顺序
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param table 表
	 * @param columns 上一步查询结果
	 * @param set set
	 * @return columns columns
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, int index, boolean create, Table table, LinkedHashMap<String, T> columns, DataSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String name = row.getString("COLUMN_NAME");
			T column = columns.get(name.toUpperCase());
			if(null == column){
				column = (T)new Column();
			}
			column.setCatalog(BasicUtil.evl(row.getString("TABLE_CATALOG"), table.getCatalog(), column.getCatalog()));
			column.setSchema(BasicUtil.evl(row.getString("TABLE_SCHEMA"), table.getSchema(), column.getSchema()));
			column.setTable(table);
			column.setTable(BasicUtil.evl(row.getString("TABLE_NAME"), table.getName(), column.getTableName(true)));
			column.setName(name);
			if(null == column.getPosition()) {
				column.setPosition(row.getInt("ORDINAL_POSITION", null));
			}
			column.setComment(BasicUtil.evl(row.getString("COLUMN_COMMENT","COMMENTS"), column.getComment()));
			column.setTypeName(BasicUtil.evl(row.getString("DATA_TYPE"), column.getTypeName()));
			String def = BasicUtil.evl(row.get("COLUMN_DEFAULT", "DATA_DEFAULT"), column.getDefaultValue())+"";
			if(BasicUtil.isNotEmpty(def)) {
				while(def.startsWith("(") && def.endsWith(")")){
					def = def.substring(1, def.length()-1);
				}
				column.setDefaultValue(def);
			}
			if(-1 == column.isAutoIncrement()){
				column.setAutoIncrement(row.getBoolean("IS_IDENTITY", null));
			}
			if(-1 == column.isAutoIncrement()){
				column.setAutoIncrement(row.getBoolean("IS_AUTOINCREMENT", null));
			}
			if(-1 == column.isAutoIncrement()){
				if(row.getStringNvl("EXTRA").toLowerCase().contains("auto_increment")){
					column.setAutoIncrement(true);
				}
			}

			//主键
			String column_key = row.getString("COLUMN_KEY");
			if("PRI".equals(column_key)){
				column.setPrimaryKey(1);
			}


			//非空
			if(-1 == column.isNullable()) {
				column.setNullable(row.getBoolean("IS_NULLABLE", "NULLABLE"));
			}
			//oracle中decimal(18,9) data_length == 22 DATA_PRECISION=18
			Integer len = row.getInt("NUMERIC_PRECISION","PRECISION","DATA_PRECISION");
			if(null == len){
				len = row.getInt("CHARACTER_MAXIMUM_LENGTH","MAX_LENGTH","DATA_LENGTH");
			}
			column.setPrecision(len);
			if(null == column.getScale()) {
				column.setScale(row.getInt("NUMERIC_SCALE", "SCALE", "DATA_SCALE"));
			}
			if(null == column.getCharset()) {
				column.setCharset(row.getString("CHARACTER_SET_NAME"));
			}
			if(null == column.getCollate()) {
				column.setCollate(row.getString("COLLATION_NAME"));
			}
			if(null == column.getColumnType()) {
				ColumnType columnType = type(column.getTypeName());
				column.setColumnType(columnType);
			}
			columns.put(name.toUpperCase(), column);
		}
		return columns;
	}


	/**
	 * 2 执行查询列的SQL
	 * @param runtime
	 * @param random
	 * @param create
	 * @param table
	 * @param columns
	 * @param runs
	 * @return
	 * @param <T>
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean create, Table table, LinkedHashMap<String, T> columns, List<Run> runs) {
		try {
			if (null != runs) {
				int idx = 0;
				for (Run run: runs) {
					DataSet set = select( runtime, random, true, (String) null, run);
					columns = columns(runtime, idx, true, table, columns, set);
					idx++;
				}
			}
		} catch (Exception e) {
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), table.getCatalog(), table.getSchema(), table.getName(), e.toString());
			}
		}
		return columns;
	}


	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> columns) {
		return null;
	}
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean greedy, Table table , boolean primary){
		LinkedHashMap<String,T> columns = CacheProxy.columns(runtime.getKey(), table.getName());
		if(null != columns && !columns.isEmpty()){
			return columns;
		}
		long fr = System.currentTimeMillis();
		String random = random(runtime);
		try {
			if (!greedy) {
				checkSchema(runtime, table);
			}
			String catalog = table.getCatalog();
			String schema = table.getSchema();

			int qty_dialect = 0; //优先根据系统表查询
			int qty_metadata = 0; //再根据metadata解析
			int qty_jdbc = 0; //根据驱动内置接口补充

			// 1.优先根据系统表查询
			try {
				List<Run> runs = buildQueryColumnRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run: runs) {
						DataSet set = select(runtime, random, true, (String) null, run);
						columns = columns(runtime, idx, true, table, columns, set);
						idx++;
					}
				}
			} catch (Exception e) {
				if(primary) {
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
					List<Run> runs = buildQueryColumnRun(table, true);
					if (null != runs) {
						for (Run run  : runs) {
							SqlRowSet set = ((JDBCRuntime)runtime).jdbc().queryForRowSet(run.getFinalQuery());
							columns = columns(runtime, true, columns, table, set);
						}
					}
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
							log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), catalog, schema, table, e.toString());
						}
					}
				}
				qty_metadata = columns.size() - qty_dialect;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据驱动内置接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, columns.size(), qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
			}

			// 根据jdbc接口补充

			if (columns.size() == 0) {
				DataSource ds = null;
				Connection con = null;
				DatabaseMetaData metadata = null;
				try {
					ds = ((JDBCRuntime)runtime).jdbc().getDataSource();
					con = DataSourceUtils.getConnection(ds);
					metadata = con.getMetaData();
					columns = columns(runtime, true, columns, metadata, table, null);
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}
				}finally {
					if (!DataSourceUtils.isConnectionTransactional(con, ds)) {
						DataSourceUtils.releaseConnection(con, ds);
					}
				}
				qty_jdbc = columns.size() - qty_metadata - qty_dialect;
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据jdbc接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, columns.size(), qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
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
						PrimaryKey pk = primary(runtime, false, table);
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
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, SqlRowSet set) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		SqlRowSetMetaData rsm = set.getMetaData();
		for (int i = 1; i <= rsm.getColumnCount(); i++) {
			String name = rsm.getColumnName(i);
			if(BasicUtil.isEmpty(name)){
				continue;
			}
			T column = columns.get(name.toUpperCase());
			if(null == column){
				if(create){
					column = (T)column(runtime, column, rsm, i);
					if(BasicUtil.isEmpty(column.getName())) {
						column.setName(name);
					}
					columns.put(column.getName().toUpperCase(), column);
				}
			}
		}
		return columns;
	}

	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, DatabaseMetaData dbmd, Table table, String pattern) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		ResultSet set = dbmd.getColumns(catalog, schema, table.getName(), pattern);
		Map<String,Integer> keys = keys(set);
		while (set.next()){
			String name = set.getString("COLUMN_NAME");
			if(null == name){
				continue;
			}
			String columnCatalog = string(keys,"TABLE_CAT", set, null);
			String columnSchema = string(keys,"TABLE_SCHEM", set, null);
			if(!BasicUtil.equalsIgnoreCase(catalog, columnCatalog)){
				continue;
			}
			if(!BasicUtil.equalsIgnoreCase(schema, columnSchema)){
				continue;
			}


			T column = columns.get(name.toUpperCase());
			if(null == column){
				if(create) {
					column = (T)new Column(name);
					columns.put(name.toUpperCase(), column);
				}else {
					continue;
				}
			}
			String remark = string(keys, "REMARKS", set, column.getComment());
			if("TAG".equals(remark)){
				column = (T)new Tag();
			}
			column.setCatalog(columnCatalog);
			column.setSchema(columnSchema);
			column.setComment(remark);
			column.setTable(BasicUtil.evl(string(keys,"TABLE_NAME", set, table.getName()), column.getTableName(true)));
			column.setType(integer(keys, "DATA_TYPE", set, column.getType()));
			column.setType(integer(keys, "SQL_DATA_TYPE", set, column.getType()));
			String jdbcType = string(keys, "TYPE_NAME", set, column.getTypeName());
			if(BasicUtil.isEmpty(column.getTypeName())) {
				//数据库中 有jdbc是支持的类型 如果数据库中有了就不用jdbc的了
				column.setTypeName(jdbcType);
			}
			column.setJdbcType(jdbcType);
			column.setPrecision(integer(keys, "COLUMN_SIZE", set, column.getPrecision()));
			column.setScale(integer(keys, "DECIMAL_DIGITS", set, column.getScale()));
			column.setNullable(bool(keys, "NULLABLE", set, column.isNullable()));
			column.setDefaultValue(value(keys, "COLUMN_DEF", set, column.getDefaultValue()));
			column.setPosition(integer(keys, "ORDINAL_POSITION", set, column.getPosition()));
			column.setAutoIncrement(bool(keys,"IS_AUTOINCREMENT", set, column.isAutoIncrement()));
			ColumnType columnType = type(column.getTypeName());
			column.setColumnType(columnType);
			column(runtime, column, set);
			column.setName(name);
		}

		// 主键
		ResultSet rs = dbmd.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName());
		while (rs.next()) {
			String name = rs.getString(4);
			Column column = columns.get(name.toUpperCase());
			if (null == column) {
				continue;
			}
			column.setPrimaryKey(true);
		}
		return columns;
	}


	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Table table, String pattern) throws Exception{
		if(null == columns){
			columns = new LinkedHashMap<>();
		}

		DataSource ds = null;
		Connection con = null;

		try{
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);

			String catalog = table.getCatalog();
			String schema = table.getSchema();
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet set = dbmd.getColumns(catalog, schema, table.getName(), pattern);
			Map<String,Integer> keys = keys(set);
			while (set.next()){
				String name = set.getString("COLUMN_NAME");
				if(null == name){
					continue;
				}
				String columnCatalog = string(keys,"TABLE_CAT", set, null);
				String columnSchema = string(keys,"TABLE_SCHEM", set, null);
				if(!BasicUtil.equalsIgnoreCase(catalog, columnCatalog)){
					continue;
				}
				if(!BasicUtil.equalsIgnoreCase(schema, columnSchema)){
					continue;
				}


				T column = columns.get(name.toUpperCase());
				if(null == column){
					if(create) {
						column = (T)new Column(name);
						columns.put(name.toUpperCase(), column);
					}else {
						continue;
					}
				}
				String remark = string(keys, "REMARKS", set, column.getComment());
				if("TAG".equals(remark)){
					column = (T)new Tag();
				}
				column.setCatalog(columnCatalog);
				column.setSchema(columnSchema);
				column.setComment(remark);
				column.setTable(BasicUtil.evl(string(keys,"TABLE_NAME", set, table.getName()), column.getTableName(true)));
				column.setType(integer(keys, "DATA_TYPE", set, column.getType()));
				column.setType(integer(keys, "SQL_DATA_TYPE", set, column.getType()));
				String jdbcType = string(keys, "TYPE_NAME", set, column.getTypeName());
				if(BasicUtil.isEmpty(column.getTypeName())) {
					//数据库中 有jdbc是支持的类型 如果数据库中有了就不用jdbc的了
					column.setTypeName(jdbcType);
				}
				column.setJdbcType(jdbcType);
				column.setPrecision(integer(keys, "COLUMN_SIZE", set, column.getPrecision()));
				column.setScale(integer(keys, "DECIMAL_DIGITS", set, column.getScale()));
				column.setNullable(bool(keys, "NULLABLE", set, column.isNullable()));
				column.setDefaultValue(value(keys, "COLUMN_DEF", set, column.getDefaultValue()));
				column.setPosition(integer(keys, "ORDINAL_POSITION", set, column.getPosition()));
				column.setAutoIncrement(bool(keys,"IS_AUTOINCREMENT", set, column.isAutoIncrement()));
				ColumnType columnType = type(column.getTypeName());
				column.setColumnType(columnType);
				column(runtime, column, set);
				column.setName(name);
			}

			// 主键
			ResultSet rs = dbmd.getPrimaryKeys(table.getCatalog(), table.getSchema(), table.getName());
			while (rs.next()) {
				String name = rs.getString(4);
				Column column = columns.get(name.toUpperCase());
				if (null == column) {
					continue;
				}
				column.setPrimaryKey(true);
			}
		}catch (Exception e){

		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return columns;
	}

	@Override
	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> tags, SqlRowSet set)", 37));
		}
		if(null == tags){
			tags = new LinkedHashMap<>();
		}
		return tags;
	}

	@Override
	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> indexs, SqlRowSet set)", 37));
		}
		if(null == indexs){
			indexs = new LinkedHashMap<>();
		}
		return indexs;
	}

	@Override
	public <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set) throws Exception{
		if(log.isDebugEnabled()) {
			log.debug(LogUtil.format("子类(" + this.getClass().getName().replace("org.anyline.data.jdbc.config.db.impl.", "") + ")未实现 <T extends Constraint> LinkedHashMap<String, T> constraints(DataRuntime runtime, boolean create, Table table, LinkedHashMap<String, T> constraints, SqlRowSet set)", 37));
		}
		if(null == constraints){
			constraints = new LinkedHashMap<>();
		}
		return constraints;
	}

}
