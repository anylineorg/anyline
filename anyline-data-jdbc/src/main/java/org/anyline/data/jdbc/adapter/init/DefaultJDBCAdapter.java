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
 */


package org.anyline.data.jdbc.adapter.init;


import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.init.DefaultDriverAdapter;
import org.anyline.data.cache.PageLazyStore;
import org.anyline.data.handler.*;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
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
import org.anyline.data.util.DataSourceUtil;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.EntitySet;
import org.anyline.entity.PageNavi;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.proxy.CacheProxy;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.LogUtil;
import org.anyline.util.regular.RegularUtil;
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
		Object processor = runtime.getProcessor();
		return (JdbcTemplate) processor;
	}
	/**
	 * UPDATE [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param configs 条件
	 * @param columns 列
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, String dest, Object data, ConfigStore configs, List<String> columns){
		dest = DataSourceUtil.parseDataSource(dest, data);
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean sql_success = false;
		if(null == random){
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareUpdate(runtime, random, dest, data, configs, columns);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareUpdate(runtime, random, dest, data, configs, false, columns);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null == data){
			if(ConfigTable.IS_THROW_SQL_UPDATE_EXCEPTION){
				throw new SQLUpdateException("更新空数据");
			}else{
				log.error("更新空数据");
			}
		}
		long result = 0;
		if(data instanceof DataSet){
			DataSet set = (DataSet)data;
			for(int i=0; i<set.size(); i++){
				result += update(runtime, random, dest, set.getRow(i), configs,  columns);
			}
			return result;
		}

		Run run = buildUpdateRun(runtime, dest, data, configs,false, columns);

		Table table = new Table(dest);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			table.setColumns(columns(runtime, null,false, table, false));
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
		swt = InterceptorProxy.beforeUpdate(runtime, random, run, dest, data, configs, columns);
		if (swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.beforeUpdate(runtime, random, run, dest, data, columns);
		}
		if (swt == ACTION.SWITCH.BREAK) {
			return -1;
		}
		result = update(runtime, random, dest, data, run);
		sql_success = true;
		millis = System.currentTimeMillis() - fr;
		if (null != dmListener) {
			dmListener.afterUpdate(runtime, random, run, result, dest, data, columns, sql_success, result,  millis);
		}
		InterceptorProxy.afterUpdate(runtime, random, run, dest, data, configs, columns, sql_success, result, System.currentTimeMillis() - fr);
		return result;
	}

	/**
	 * exists [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return boolean
	 */
	@Override
	public boolean exists(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		boolean result = false;
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return false;
		}
		Run run = buildQueryRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return false;
		}
		if(null != dmListener){
			dmListener.beforeExists(runtime, random, run);
		}
		long fr = System.currentTimeMillis();
		Map<String, Object> map = map(runtime, random, run);
		if (null == map) {
			result = false;
		} else {
			result = BasicUtil.parseBoolean(map.get("IS_EXISTS"), false);
		}
		Long millis = System.currentTimeMillis() - fr;
		if(null != dmListener){
			dmListener.afterExists(runtime, random, run, true, result, millis);
		}
		return result;
	}
	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param next 是否查下一个序列值
	 * @param names 存储过程名称s
	 * @return DataRow 保存序列查询结果 以存储过程name作为key
	 */
	@Override
	public DataRow sequence(DataRuntime runtime, String random, boolean next, String ... names){
		List<Run> runs = buildQuerySequence(runtime, next, names);
		if (null != runs && runs.size() > 0) {
			Run run = runs.get(0);
			if(!run.isValid()){
				if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
					log.warn("[valid:false][不具备执行条件][sequence:"+names);
				}
				return new DataRow();
			}
			DataSet set = select(runtime, random, true, null, null, run);
			if (set.size() > 0) {
				return set.getRow(0);
			}
		}
		return new DataRow();
	}
	/**
	 * count [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return long
	 */
	public long count(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		long count = -1;
		Long fr = System.currentTimeMillis();
		Run run = null;
		if(null == random){
			random = random(runtime);
		}

		boolean sql_success = false;

		ACTION.SWITCH swt = InterceptorProxy.prepareCount(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if (null != dmListener) {
			swt = dmListener.prepareQuery(runtime, random, prepare, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		run = buildQueryRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		if (null != dmListener) {
			dmListener.beforeCount(runtime, random, run);
		}
		swt = InterceptorProxy.beforeCount(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		fr = System.currentTimeMillis();
		count = count(runtime, random, run);
		sql_success = true;

		if(null != dmListener){
			dmListener.afterCount(runtime, random, run, sql_success, count, System.currentTimeMillis() - fr);
		}
		InterceptorProxy.afterCount(runtime, random, run, sql_success, count, System.currentTimeMillis() - fr);
		return count;
	}
	/**
	 * count [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return long
	 */
	@Override
	public long count(DataRuntime runtime, String random, Run run) {
		long total = 0;
		DataSet set = select(runtime, random, false, null, null, run, run.getTotalQuery(), run.getValues());
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
			ResultSetMetaData rsmd = rs.getMetaData();
			int qty = rsmd.getColumnCount();
			if (!system && (null == metadatas || metadatas.isEmpty())) {
				for (int i = 1; i <= qty; i++) {
					String name = rsmd.getColumnName(i);
					if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
						continue;
					}
					Column column = metadatas.get(name) ;
					column = column(runtime, (Column) column, rsmd, i);
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
				Object value = read(runtime, column, rs.getObject(name), null);
				row.put(false, name, value);
			}
			row.setMetadata(metadatas);
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[封装结果集][result:fail][msg:{}]", e.toString());
			}
		}
		return row;
	}
    protected boolean stream(StreamHandler handler, ResultSet rs, ConfigStore configs, boolean system, DataRuntime runtime, LinkedHashMap<String, Column> metadatas) {
		try {
			if (handler instanceof ResultSetHandler) {
				return ((ResultSetHandler) handler).read(rs);
			} else {
				if (handler instanceof DataRowHandler) {
					DataRowHandler dataRowHandler = (DataRowHandler) handler;
					DataRow row = row(system, runtime, metadatas, rs);
					if (!dataRowHandler.read(row)) {
						return false;
					}
				} else if (handler instanceof EntityHandler) {
					Class clazz = configs.entityClass();
					if (null != clazz) {
						EntityHandler entityHandler = (EntityHandler) handler;
						DataRow row = row(system, runtime, metadatas, rs);
						if (!entityHandler.read(row.entity(clazz))) {
							return false;
						}
					}
				} else if (handler instanceof MapHandler) {
					MapHandler mh = (MapHandler) handler;
					ResultSetMetaData rsmd = rs.getMetaData();
					int cols = rsmd.getColumnCount();
					Map<String, Object> map = new HashMap<>();
					for (int i = 1; i <= cols; i++) {
						map.put(rsmd.getColumnLabel(i), rs.getObject(i));
					}
					if (!mh.read(map)) {
						return false;
					}
				}
			}
		}catch (Exception e){
			return false;
		}
        return true;
    }
 	protected DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run, String sql, List<Object> values){
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
			columns = columns(runtime,  random, false,  new Table( table), false);
		}
		try{
			final DataRuntime rt = runtime;
			final long[] mid = {System.currentTimeMillis()};
			final boolean[] process = {false};
			final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
			metadatas.putAll(columns);
			set.setMetadatas(metadatas);
			JdbcTemplate jdbc = jdbc(runtime);
			StreamHandler _handler = null;
			if(null != configs){
				_handler = configs.stream();
			}
			long[] count = new long[]{0};
			final StreamHandler handler = _handler;
			if(null != handler){

				jdbc.query(con -> {
					PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					ps.setFetchSize(handler.size());
					ps.setFetchDirection(ResultSet.FETCH_FORWARD);
					if (null != values && values.size() > 0) {
						int idx = 0;
						for (Object value : values) {
							ps.setObject(++idx, value);
						}
					}
					return ps;
				}, rs -> {
					if(!process[0]){
						mid[0] = System.currentTimeMillis();
						process[0] = true;
					}
					stream(handler, rs, configs, system, runtime, metadatas);
					count[0] ++;
				});
				//end stream handler
			}else {
				if(null != values && values.size()>0){
					jdbc.query(sql, values.toArray(), new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs) throws SQLException {
							if(!process[0]){
								mid[0] = System.currentTimeMillis();
								process[0] = true;
							}
							DataRow row = row(system, rt, metadatas, rs);
							set.add(row);
						}
					});
				}else {
					jdbc.query(sql, new RowCallbackHandler() {
						@Override
						public void processRow(ResultSet rs) throws SQLException {
							if(!process[0]){
								mid[0] = System.currentTimeMillis();
								process[0] = true;
							}
							DataRow row = row(system, rt, metadatas, rs);
							set.add(row);
						}
					});
				}
				count[0] = set.size();
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
				log.info("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], count[0]);
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
	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param system 系统表不检测列属性
	 * @param table 表
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return DataSet
	 */
	@Override
	public DataSet select(DataRuntime runtime, String random, boolean system, String table, ConfigStore configs, Run run) {
		String sql = run.getFinalQuery();
		if(BasicUtil.isEmpty(sql)){
			return new DataSet();
		}
		List<Object> values = run.getValues();
		return select(runtime, random, system, table, configs, run, sql, values);
	}

	/**
	 * 封装查询结果
	 * @param system 系统表不检测列属性
	 * @param runtime  runtime
	 * @param metadatas metadatas
	 * @param rs jdbc返回结果
	 * @return DataRow
	 */
	protected DataRow row(DataRuntime runtime, boolean system, LinkedHashMap<String, Column> metadatas, ResultSet rs) {
		DataRow row = new DataRow();
		try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int qty = rsmd.getColumnCount();
			if (!system && metadatas.isEmpty()) {
				for (int i = 1; i <= qty; i++) {
					String name = rsmd.getColumnName(i);
					if(null == name || name.toUpperCase().equals("PAGE_ROW_NUMBER_")){
						continue;
					}
					Column column = metadatas.get(name) ;
					column = column(runtime, column, rsmd, i);
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
				Object value = read(runtime, column, rs.getObject(name), null);
				row.put(false, name, value);
			}
			row.setMetadata(metadatas);
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
	/**
	 * select [入口]
	 * <br/>
	 * 对性能有要求的场景调用，返回java原生map集合,结果中不包含元数据信息
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return maps 返回map集合
	 */
	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		List<Map<String,Object>> maps = null;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean sql_success = false;
		Run run = null;
		if(null == random){
			random = random(runtime);
		}
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

		run = buildQueryRun(runtime, prepare, configs, conditions);
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
			maps = maps(runtime, random, configs, run);
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

	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return maps
	 */
	@Override
	public List<Map<String,Object>> maps(DataRuntime runtime, String random, ConfigStore configs, Run run){
		List<Map<String,Object>> maps = null;
		if(null == random){
			random = random(runtime);
		}
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
			StreamHandler _handler = null;
			if(null != configs){
				_handler = configs.stream();
			}
			long[] count = new long[]{0};
			final boolean[] process = {false};
			final StreamHandler handler = _handler;
            final long[] mid = {System.currentTimeMillis()};
			if(null != handler){
				jdbc.query(con -> {
					PreparedStatement ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					ps.setFetchSize(handler.size());
					ps.setFetchDirection(ResultSet.FETCH_FORWARD);
					if (null != values && values.size() > 0) {
						int idx = 0;
						for (Object value : values) {
							ps.setObject(++idx, value);
						}
					}
					return ps;
				}, rs -> {
					if(!process[0]){
						mid[0] = System.currentTimeMillis();
						process[0] = true;
					}
					stream(handler, rs, configs, true, runtime, null);
					count[0] ++;
				});
				maps = new ArrayList<>();
				//end stream handler
			}else {
				if (null != values && values.size() > 0) {
					maps = jdbc.queryForList(sql, values.toArray());
				} else {
					maps = jdbc.queryForList(sql);
				}
                mid[0] = System.currentTimeMillis();
                count[0] = maps.size();
			}
			boolean slow = false;
			long SLOW_SQL_MILLIS = ThreadConfig.check(runtime.getKey()).SLOW_SQL_MILLIS();
			if(SLOW_SQL_MILLIS > 0){
				if(mid[0]-fr > SLOW_SQL_MILLIS){
					slow = true;
					log.warn("{}[SLOW SQL][action:select][millis:{}ms][sql:\n{}\n]\n[param:{}]", random, mid[0]-fr, sql, LogUtil.param(values));
					if(null != dmListener){
						dmListener.slow(runtime, random, ACTION.DML.SELECT,null, sql, values, null, true, maps, mid[0]-fr);
					}
				}
			}
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[执行耗时:{}ms]", random, mid[0] - fr);
			}
			maps = process(runtime, maps);
			if(!slow && ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()){
				log.info("{}[封装耗时:{}ms][封装行数:{}]", random, System.currentTimeMillis() - mid[0], count);
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
	/**
	 * select [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return map
	 */
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

	/**
	 * update [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long update(DataRuntime runtime, String random, String dest, Object data, Run run){
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
	 * insert [入口]<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 列
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns){
		dest = DataSourceUtil.parseDataSource(dest, data);
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		boolean sql_success = false;
		swt = InterceptorProxy.prepareInsert(runtime, random,  dest, data, checkPrimary, columns);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareInsert(runtime, random, dest, data, checkPrimary, columns);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != data && data instanceof DataSet){
			DataSet set = (DataSet)data;
			Map<String,Object> tags = set.getTags();
			if(null != tags && tags.size()>0){
				LinkedHashMap<String, PartitionTable> ptables = ptables(runtime, random, false, new MasterTable(dest), tags, null);
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
		Run run = buildInsertRun(runtime, dest, data, checkPrimary, columns);
		Table table = new Table(dest);
		//提前设置好columns,到了adapter中需要手动检测缓存
		if(ConfigTable.IS_AUTO_CHECK_METADATA){
			table.setColumns(columns(runtime, random,  false, table, false));
		}
		if(null == run){
			return 0;
		}

		long cnt = 0;
		//final String sql = run.getFinalInsert();
		//final List<Object> values = run.getValues();
		long fr = System.currentTimeMillis();
		long millis = -1;

		swt = InterceptorProxy.beforeInsert(runtime, random, run, dest, data, checkPrimary, columns);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeInsert(runtime, random, run, dest, data, checkPrimary, columns);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		cnt = insert(runtime, random, data, run, null);
		if (null != dmListener) {
			dmListener.afterInsert(runtime, random, run, cnt, dest, data, checkPrimary, columns, sql_success, cnt, millis);
		}
		InterceptorProxy.afterInsert(runtime, random, run, dest, data, checkPrimary, columns, sql_success, cnt, System.currentTimeMillis() - fr);
		return cnt;
	}

	/**
	 * insert [执行]
	 * <br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks 需要返回的主键
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Object data, Run run, String[] pks) {
		long cnt = 0;
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
	/**
	 * insert [执行]
	 * <br/>
	 * 有些不支持返回自增的单独执行<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param data data
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @param pks pks
	 * @param simple 没有实际作用 用来标识有些不支持返回自增的单独执行
	 * @return 影响行数
	 */
	@Override
	public long insert(DataRuntime runtime, String random, Object data, Run run, String[] pks, boolean simple) {
		long cnt = 0;
		if(null == random){
			random = random(runtime);
		}
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

	/**
	 * save [入口]
	 * <br/>
	 * 根据是否有主键值确认insert | update<br/>
	 * 执行完成后会补齐自增主键值
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param dest 表
	 * @param data 数据
	 * @param checkPrimary 是否需要检查重复主键,默认不检查
	 * @param columns 列
	 * @return 影响行数
	 */
	@Override
	public long save(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns){

		if(null == random){
			random = random(runtime);
		}
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
			long cnt = 0;
			for(Object item:items){
				cnt += save(runtime, random, dest, item, checkPrimary, columns);
			}
			return cnt;
		}
		return saveObject(runtime, random, dest, data, checkPrimary, columns);
	}

	protected long saveObject(DataRuntime runtime, String random, String dest, Object data, boolean checkPrimary, List<String> columns){
		if(null == data){
			return 0;
		}
		boolean isNew = checkIsNew(data);
		if(isNew){
			return insert(runtime, random, dest, data, checkPrimary, columns);
		}else{
			//是否覆盖(null:不检测直接执行update有可能影响行数=0)
			Boolean override = checkOverride(data);
			ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
			if(null != override){
				RunPrepare prepare = new DefaultTablePrepare(dest);
				Map<String, Object> pvs = checkPv(data);
				ConfigStore stores = new DefaultConfigStore();
				for(String k:pvs.keySet()){
					stores.and(k, pvs.get(k));
				}
				boolean exists = exists(runtime, random, prepare, stores);
				if(exists){
					if(override){
						return update(runtime, random, dest, data, null, columns);
					}else{
						log.warn("[跳过更新][数据已存在:{}({})]",dest, BeanUtil.map2json(pvs));
					}
				}else{
					return insert(runtime, random, dest, data, checkPrimary, columns);
				}
			}else{
				return update(runtime, random, dest, data, null, columns);
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
	 * execute [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
		long result = -1;
		boolean sql_success = false;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		if(null == random){
			random = random(runtime);
		}
		swt = InterceptorProxy.prepareExecute(runtime, random, prepare, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}

		Run run = buildExecuteRun(runtime, prepare, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][RunPrepare:" + ConfigParser.createSQLSign(false, false, prepare.getTable(), configs, conditions) + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long fr = System.currentTimeMillis();

		long millis = -1;
		swt = InterceptorProxy.beforeExecute(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeExecute(runtime, random, run);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		result = execute(runtime, random, run);
		sql_success = true;
		if (null != dmListener) {
			dmListener.afterExecute(runtime, random, run,  sql_success, result, millis);
		}
		InterceptorProxy.afterExecute(runtime, random, run, sql_success, result, System.currentTimeMillis()-fr);
		return result;
	}

	/**
	 * execute [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return 影响行数
	 */
	@Override
	public long execute(DataRuntime runtime, String random, Run run){
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

	/**
	 * procedure [执行]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param procedure 存储过程
	 * @param random  random
	 * @return 影响行数
	 */
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
	/**
	 * select procedure [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param procedure 存储过程
	 * @param navi 分页
	 * @return DataSet
	 */
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

			ACTION.SWITCH swt = InterceptorProxy.prepareQuery(runtime, random, procedure, navi);
			if(swt == ACTION.SWITCH.BREAK){
				return new DataSet();
			}
			swt = InterceptorProxy.beforeQuery(runtime, random, procedure, navi);
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
 ;
		return set;
	}

	/**
	 * select [入口]
	 * <br/>
	 * 返回DataSet中包含元数据信息，如果性能有要求换成maps
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return DataSet
	 */
	@Override
	public DataSet querys(DataRuntime runtime, String random, RunPrepare prepare, ConfigStore configs, String ... conditions){
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
				if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
					// 第一条 query中设置的标识(只查一行)
					total = 1;
				} else {
					// 未计数(总数 )
					if (navi.getTotalRow() == 0) {
						total = count(runtime, random, run);
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
				set = select(runtime, random, false, prepare.getTable(), configs, run);
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

	/**
	 * select [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param clazz 类
	 * @param prepare 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
	 * @param configs 过滤条件及相关配置
	 * @param conditions  简单过滤条件
	 * @return EntitySet
	 * @param <T> Entity
	 */
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

		run = buildQueryRun(runtime, prepare, configs, conditions);
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
				if (navi.getCalType() == 1 && navi.getLastRow() == 0) {
					// 第一条 query中设置的标识(只查一行)
					total = 1;
				}  else {
					// 未计数(总数 )
					if (navi.getTotalRow() == 0) {
						total = count(runtime, random, run);
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
				list = select(runtime, random, clazz, run.getTable(), configs, run);
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
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param clazz entity class
	 * @param table table
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return EntitySet
	 * @param <T> entity.class
	 *
	 */
	protected  <T> EntitySet<T> select(DataRuntime runtime, String random, Class<T> clazz, String table, ConfigStore configs, Run run){
		EntitySet<T> set = new EntitySet<>();
		if(null == random){
			random = random(runtime);
		}
		if(null != configs){
			configs.entityClass(clazz);
		}
		DataSet rows = select(runtime, random, false, table, configs, run);
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

		return set;
	}


	/**
	 * delete [入口]
	 * <br/>
	 * 合成 where column in (values)
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param column 列
	 * @param values 列对应的值
	 * @return 影响行数
	 * @param <T> T
	 */
	@Override
	public <T> long deletes(DataRuntime runtime, String random, String table, String key, Collection<T> values){
		table = DataSourceUtil.parseDataSource(table, null);
		if(null == random){
			random = random(runtime);
		}
		ACTION.SWITCH swt = InterceptorProxy.prepareDelete(runtime, random, table, key, values);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareDelete(runtime, random, table, key, values);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		Run run = buildDeleteRun(runtime, table, key, values);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][table:" +table+ "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long result = exeDelete(runtime, random, run);
		return result;
	}

	/**
	 * delete [入口]
	 * <br/>
	 * 合成 where k1 = v1 and k2 = v2
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param obj entity或DataRow
	 * @param columns 删除条件的列或属性，根据columns取obj值并合成删除条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, String dest, Object obj, String... columns){
		dest = DataSourceUtil.parseDataSource(dest,obj);
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		long size = 0;
		if(null != obj){
			if(obj instanceof Collection){
				Collection list = (Collection) obj;
				for(Object item:list){
					long qty = delete(runtime, random, dest, item, columns);
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
				if(swt == ACTION.SWITCH.BREAK){
					return -1;
				}
				if(null != dmListener){
					swt = dmListener.prepareDelete(runtime, random, dest, obj, columns);
				}
				if(swt == ACTION.SWITCH.BREAK){
					return -1;
				}
				Run run = buildDeleteRun(runtime, dest, obj, columns);
				if(!run.isValid()){
					if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
						log.warn("[valid:false][不具备执行条件][dest:" + dest + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
					}
					return -1;
				}
				size = exeDelete(runtime, random,  run);
			}
		}
		return size;
	}

	/**
	 * delete [入口]
	 * <br/>
	 * 根据configs和conditions过滤条件
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @param configs 查询条件及相关设置
	 * @param conditions  简单过滤条件
	 * @return 影响行数
	 */
	@Override
	public long delete(DataRuntime runtime, String random, String table, ConfigStore configs, String... conditions){
		table = DataSourceUtil.parseDataSource(table, null);
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		swt = InterceptorProxy.prepareDelete(runtime, random, table, configs, conditions);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.prepareDelete(runtime,random, table, configs, conditions);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
 		Run run = buildDeleteRun(runtime, table, configs, conditions);
		if(!run.isValid()){
			if(ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()){
				log.warn("[valid:false][不具备执行条件][table:" + table + "][thread:" + Thread.currentThread().getId() + "][ds:" + runtime.datasource() + "]");
			}
			return -1;
		}
		long result = exeDelete(  runtime, random,  run);
		return result;
	}

	/**
	 * truncate [入口]
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param table 表
	 * @return 1表示成功执行
	 */
	@Override
	public int truncate(DataRuntime runtime, String random, String table){
		table = DataSourceUtil.parseDataSource(table);
		List<Run> runs = buildTruncateRun(runtime, table);
		if(null != runs && runs.size()>0) {
			RunPrepare prepare = new DefaultTextPrepare(runs.get(0).getFinalUpdate());
			return (int)execute(runtime, random, prepare, null);
		}
		return -1;
	}
	/**
	 * 执行删除
	 * @param recover 执行完成后是否根据设置自动还原数据源
	 * @param runtime DataRuntime
	 * @param run 最终待执行的命令和参数(如果是JDBC环境就是SQL)
	 * @return int
	 */
	protected long exeDelete(DataRuntime runtime, String random, Run run){
		long result = -1;
		boolean sql_success = false;
		ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
		long fr = System.currentTimeMillis();
		swt = InterceptorProxy.beforeDelete(runtime, random, run);
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		if(null != dmListener){
			swt = dmListener.beforeDelete(runtime, random, run);
		}
		if(swt == ACTION.SWITCH.BREAK){
			return -1;
		}
		long millis = -1;

		result = execute(runtime, random, run);
		sql_success = true;
		millis = System.currentTimeMillis() - fr;

		if(null != dmListener){
			dmListener.afterDelete(runtime, random, run, sql_success, result, millis);
		}
		InterceptorProxy.afterDelete(runtime, random, run,  sql_success, result, millis);
		return result;
	}

	@Override
	public Database database(DataRuntime runtime, String random, String name){
		if(null == random){
			random = random(runtime);
		}
		Database database = null;
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabaseRun(runtime, name);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
						database = database(runtime, idx++, true, set);
						if(null != database){
							break;
						}
					}
				}
			}catch (Exception e){
				if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[database][{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33),  e.toString());
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[database][result:{}][执行耗时:{}ms]", random, database, System.currentTimeMillis() - fr);
			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[database][result:fail][msg:{}]", e.toString());
			}
		}
		return database;
	}

	@Override
	public LinkedHashMap<String, Database> databases(DataRuntime runtime, String random){
		if(null == random){
			random = random(runtime);
		}
		LinkedHashMap<String,Database> databases = new LinkedHashMap<>();
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryDatabaseRun(runtime);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
						databases = databases(runtime, idx++, true, databases, set);
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
	/**
	 * 缓存表名
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param random 用来标记同一组命令
	 * @param catalog catalog
	 * @param schema schema
	 */
	private void tableMap(DataRuntime runtime, boolean greedy, String random, String catalog, String schema){
		Map<String, String> names = CacheProxy.names(catalog, schema);
		if(null == names || names.isEmpty()){
			if(null == random){
				random = random(runtime);
			}
			DriverAdapter adapter = runtime.getAdapter();
			LinkedHashMap<String, Table> tables = new LinkedHashMap<>();
			boolean sys = false; //根据系统表查询
			try {
				List<Run> runs =buildQueryTableRun(runtime, greedy, null, null, null, null);
				if (null != runs && runs.size() > 0) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, true, (String) null, null, run).toUpperKey();
						tables = tables(runtime, idx++, true, catalog, schema, (LinkedHashMap<String, Table>) null, set);
						CacheProxy.name(tables);
						sys = true;
					}
				}
			}catch (Exception e){
				e.printStackTrace();
			}
			if(!sys){
				try {
					tables = tables(runtime, true,  (LinkedHashMap<String, Table>) null, catalog, schema, null, null);
					CacheProxy.name(tables);
				}catch (Exception e){
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types){
		List<T> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			Table search = new Table();
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					checkSchema(runtime, tmp);
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
				tableMap(runtime, greedy, random, catalog, schema);
				origin = CacheProxy.name(greedy, catalog, schema, pattern);
			}
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
				List<Run> runs = buildQueryTableRun(runtime, greedy, catalog, schema, origin, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
						list = tables(runtime, idx++, true, catalog, schema, list, set);
						//merge(list, tables);
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
			if(list.size() == 0) {
				try {
					list = tables(runtime, true, list, catalog, schema, origin, tps);
					//merge(list, maps);
					/*
					for (String key : jdbcTables.keySet()) {
						if (!tables.containsKey(key.toUpperCase())) {
							T item = jdbcTables.get(key);
							if (null != item) {
								if (greedy || (catalog + "_" + schema).equalsIgnoreCase(item.getCatalog() + "_" + item.getSchema())) {
									tables.put(key.toUpperCase(), item);
								}
							}
						}
					}*/
				} catch (Exception e) {
					if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
						e.printStackTrace();
					}else {
						log.warn("{}[tables][][catalog:{}][schema:{}][pattern:{}][msg:{}]", random, LogUtil.format("根据驱动内置接口补充失败", 33), catalog, schema, origin, e.toString());
					}
				}
			}
			boolean comment = false;
			for(Table table:list){
				if(BasicUtil.isNotEmpty(table.getComment())){
					comment = true;
					break;
				}
			}
			//表备注
			if(!comment) {
				try {
					List<Run> runs = buildQueryTableCommentRun(runtime, catalog, schema, null, types);
					if (null != runs) {
						int idx = 0;
						for (Run run : runs) {
							DataSet set = select(runtime, random, true, (String) null, null, run).toUpperKey();
							list = comments(runtime, idx++, true, catalog, schema, list, set);
							//merge(list, maps);
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
				log.info("{}[tables][catalog:{}][schema:{}][pattern:{}][type:{}][result:{}][执行耗时:{}ms]", random, catalog, schema, origin, types, list.size(), System.currentTimeMillis() - fr);
			}
			if(BasicUtil.isNotEmpty(origin)){
				//有表名的，根据表名过滤出符合条件的
				List<T> tmp = new ArrayList<>();
 				for(T item:list){
					String name = item.getName(greedy);
					if(RegularUtil.match(name, origin)){
						tmp.add(item);
					}
				}
				 list = tmp;
 			}
		}catch (Exception e){
			if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			}else{
				log.error("[tables][result:fail][msg:{}]", e.toString());
			}
		}
		return list;
	}
	private <T extends Table> boolean contains(List<T> tables, T table){
		boolean contains = false;
		if(null != table && null != tables){
			for(Table tab:tables){
				if(tab.getCatalog() == table.getCatalog() && tab.getSchema() == table.getSchema() && table.getName().equalsIgnoreCase(tab.getName())){
					return true;
				}
			}
		}
		return contains;
	}
	private <T extends Table> List<T> merge(List<T> tables, LinkedHashMap<String, T> maps){
		boolean contains = false;
		if(null != tables && null != maps) {
			for (T table : maps.values()) {
				if (!contains(tables, table)) {
					tables.add(table);
				}
			}
		}
		return tables;
	}

	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, String random, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		List<T> list = tables(runtime, random, false, catalog, schema, pattern, types);
		for(T table:list){
			tables.put(table.getName().toUpperCase(), table);
		}
		return tables;
	}


	/**
	 * 根据 DatabaseMetaData 查询表
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param tables 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return LinkedHashMap
	 * @param <T> Table
	 * @throws Exception Exception
	 */
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
	public <T extends Table> List<T> tables(DataRuntime runtime, boolean create, List<T> tables, String catalog, String schema, String pattern, String ... types) throws Exception{
		DataSource ds = null;
		Connection con = null;
		try{
			JdbcTemplate jdbc = jdbc(runtime);
			ds = jdbc.getDataSource();
			con = DataSourceUtils.getConnection(ds);
			DatabaseMetaData dbmd = con.getMetaData();
			ResultSet set = dbmd.getTables( catalog, schema, pattern, types);
			if(null == tables){
				tables = new ArrayList<>();
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
				catalog = BasicUtil.evl(string(keys, "TABLE_CATALOG", set), string(keys, "TABLE_CAT", set), catalog);
				schema = BasicUtil.evl(string(keys, "TABLE_SCHEM", set), schema);
				T table = table(tables, catalog, schema, tableName);
				boolean contains = true;
				if(null == table){
					if(create){
						table = (T)new Table();
						contains = false;
					}else{
						continue;
					}
				}
				table.setCatalog(catalog);
				table.setSchema(schema);
				table.setName(tableName);
				table.setType(BasicUtil.evl(string(keys, "TABLE_TYPE", set), table.getType()));
				table.setComment(BasicUtil.evl(string(keys, "REMARKS", set), table.getComment()));
				table.setTypeCat(BasicUtil.evl(string(keys, "TYPE_CAT", set), table.getTypeCat()));
				table.setTypeName(BasicUtil.evl(string(keys, "TYPE_NAME", set), table.getTypeName()));
				table.setSelfReferencingColumn(BasicUtil.evl(string(keys, "SELF_REFERENCING_COL_NAME", set), table.getSelfReferencingColumn()));
				table.setRefGeneration(BasicUtil.evl(string(keys, "REF_GENERATION", set), table.getRefGeneration()));
				if(!contains) {
					tables.add(table);
				}
			}
		}finally {
			if(!DataSourceUtils.isConnectionTransactional(con, ds)){
				DataSourceUtils.releaseConnection(con, ds);
			}
		}
		return tables;
	}


	/**
	 * 根据查询结构解析表属性
	 * @param index 第几条SQL 对照buildQueryTableRun返回顺序
	 * @param catalog catalog
	 * @param schema schema
	 * @param tables 上一步查询结果
	 * @param set DataSet
	 * @return tables
	 * @throws Exception 异常
	 */
	@Override
	public <T extends Table> LinkedHashMap<String, T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, LinkedHashMap<String, T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new LinkedHashMap<>();
		}
		for(DataRow row:set){
			String _catalog = row.getString("TABLE_CATALOG");
			String _schema = row.getString("TABLE_SCHEMA");
			if(null == _catalog){
				_catalog = catalog;
			}
			if(null == _schema){
				_schema = schema;
			}
			String name = row.getString("TABLE_NAME", "NAME", "TABNAME");
			T table = tables.get(name.toUpperCase());
			if(null == table){
				table = (T)new Table();
			}
			if(null == catalog){
				catalog = row.getString("TABLE_CATALOG");
			}
			if(null == schema){
				schema = row.getString("TABLE_SCHEMA");
			}
			table.setCatalog(_catalog);
			table.setSchema(_schema);
			table.setName(name);
			table.setEngine(row.getString("ENGINE"));
			table.setComment(row.getString("TABLE_COMMENT", "COMMENTS"));
			tables.put(name.toUpperCase(), table);
		}
		return tables;
	}

	@Override
	public <T extends Table> List<T> tables(DataRuntime runtime, int index, boolean create, String catalog, String schema, List<T> tables, DataSet set) throws Exception{
		if(null == tables){
			tables = new ArrayList<>();
		}
		for(DataRow row:set){
			String _catalog = row.getString("TABLE_CATALOG");
			String _schema = row.getString("TABLE_SCHEMA");
			if(null == _catalog){
				_catalog = catalog;
			}
			if(null == _schema){
				_schema = schema;
			}
			String name = row.getString("TABLE_NAME", "NAME", "TABNAME");
			T table = table(tables, catalog, schema, name);
			if(null == table){
				table = (T)new Table();
			}
			table.setCatalog(_catalog);
			table.setSchema(_schema);
			table.setName(name);
			table.setEngine(row.getString("ENGINE"));
			table.setComment(row.getString("TABLE_COMMENT", "COMMENTS"));
			tables.add(table);
		}
		return tables;
	}

	@Override
	public List<String> ddl(DataRuntime runtime, String random, Table table, boolean init){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, table);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
					list = ddl(runtime, idx++, table, list,  set);
				}
				table.setDdls(list);
			}else{
				//数据库不支持的 根据metadata拼装
				LinkedHashMap<String, Column> columns = columns(runtime, random, false, table, true);
				table.setColumns(columns);
				table.setTags(tags(runtime, random, false, table));
				PrimaryKey pk = primary(runtime, random, false, table);
				if (null != pk) {
					for (String col : pk.getColumns().keySet()) {
						Column column = columns.get(col.toUpperCase());
						if (null != column) {
							column.setPrimaryKey(true);
						}
					}
				}
				table.setPrimaryKey(pk);
				table.setIndexs(indexs(runtime, random, false, table, null));
				runs = buildCreateRun(runtime, table);
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

	@Override
	public <T extends View> LinkedHashMap<String, T> views(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String,T> views = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			View search = new View();
			if(null == catalog || null == schema){
				View tmp = new View();
				if(!greedy) {
					checkSchema(runtime, tmp);
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
					LinkedHashMap<String,View> all = views(runtime, random, greedy, catalog, schema, null, types);
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
				List<Run> runs = buildQueryViewRun(runtime, greedy, catalog, schema, pattern, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
						views = views(runtime, idx++, true, catalog, schema, views, set);
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
					LinkedHashMap<String, T> tmps = views(runtime, true, null, catalog, schema, pattern, tps);
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
	/**
	 * 根据DatabaseMetaData
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param create 上一步没有查到的,这一步是否需要新创建
	 * @param views 上一步查询结果
	 * @param catalog catalog
	 * @param schema schema
	 * @param pattern pattern
	 * @param types types "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
	 * @return
	 * @param <T>
	 * @throws Exception
	 */
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
	public List<String> ddl(DataRuntime runtime, String random, View view){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, view);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
					list = ddl(runtime, idx++, view, list,  set);
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
	@Override
	public <T extends MasterTable> LinkedHashMap<String, T> mtables(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String pattern, String types){
		LinkedHashMap<String, T> tables = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			if(null == catalog || null == schema){
				Table tmp = new Table();
				if(!greedy) {
					checkSchema(runtime, tmp);
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
					LinkedHashMap<String, MasterTable> all = mtables(runtime, random, greedy, catalog, schema, null, types);
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
				List<Run> runs = buildQueryMasterTableRun(runtime, catalog, schema, pattern, types);
				if(null != runs) {
					int idx = 0;
					for(Run run:runs) {
						DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
						tables = mtables(runtime, idx++, true, catalog, schema, tables, set);
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
					LinkedHashMap<String, T> tmps = mtables(runtime, true, null, catalog, schema, pattern, tps);
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
	public List<String> ddl(DataRuntime runtime, String random, MasterTable table){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
					list = ddl(runtime, idx++, table, list,  set);
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



	public <T extends PartitionTable> LinkedHashMap<String,T> ptables(DataRuntime runtime, String random, boolean greedy, MasterTable master, Map<String, Object> tags, String name){

		LinkedHashMap<String,T> tables = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		try{
			long fr = System.currentTimeMillis();
			// 根据系统表查询
			try{
				List<Run> runs = buildQueryPartitionTableRun(runtime, master, tags, name);
				if(null != runs) {
					int idx = 0;
					int total = runs.size();
					for(Run run:runs) {
						DataSet set = select(runtime, random, false, (String)null, null, run).toUpperKey();
						tables = ptables(runtime, total, idx++, true, master, master.getCatalog(), master.getSchema(), tables, set);
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
	public List<String> ddl(DataRuntime runtime, String random, PartitionTable table){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, table);
			if (null != runs) {
				int idx = 0;
				for (Run run : runs) {
					DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
					list = ddl(runtime, idx++, table, list,  set);
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

	public <T extends Tag> LinkedHashMap<String, T> tags(DataRuntime runtime, String random, boolean greedy, Table table){
		if(null == table || BasicUtil.isEmpty(table.getName())){
			return new LinkedHashMap();
		}
		LinkedHashMap<String,T> tags = CacheProxy.tags(runtime.getKey(), table.getName());
		if(null != tags && !tags.isEmpty()){
			return tags;
		}

		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		try {
 			if (!greedy) {
				checkSchema(runtime, table);
			}
			String catalog = table.getCatalog();
			String schema = table.getSchema();

			// 先根据系统表查询
			try {
				List<Run> runs = buildQueryTagRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run : runs) {
						DataSet set = select(runtime, random, true, (String) null, null, run).toUpperKey();
						tags = tags(runtime, idx, true, table, tags, set);
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
					tags = tags(runtime, false, tags, table, null);
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
		if(BasicUtil.isEmpty(table.getName())){
			return runs;
		}
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
			String name = row.getString("COLUMN_NAME", "COLNAME");
			T column = columns.get(name.toUpperCase());
			if(null == column){
				column = (T)new Column();
			}
			column.setCatalog(BasicUtil.evl(row.getString("TABLE_CATALOG"), table.getCatalog(), column.getCatalog()));
			column.setSchema(BasicUtil.evl(row.getString("TABLE_SCHEMA", "TABSCHEMA"), table.getSchema(), column.getSchema()));
			column.setTable(table);
			column.setTable(BasicUtil.evl(row.getString("TABLE_NAME", "TABNAME"), table.getName(), column.getTableName(true)));
			column.setName(name);
			if(null == column.getPosition()) {
				column.setPosition(row.getInt("ORDINAL_POSITION","COLNO"));
			}
			column.setComment(BasicUtil.evl(row.getString("COLUMN_COMMENT", "COMMENTS", "REMARKS"), column.getComment()));
			column.setTypeName(BasicUtil.evl(row.getString("DATA_TYPE", "TYPENAME"), column.getTypeName()));
			String def = BasicUtil.evl(row.get("COLUMN_DEFAULT", "DATA_DEFAULT", "DEFAULT"), column.getDefaultValue())+"";
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
				column.setAutoIncrement(row.getBoolean("IDENTITY", null));
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
				column.setNullable(row.getBoolean("IS_NULLABLE", "NULLABLE", "NULLS"));
			}
			//oracle中decimal(18,9) data_length == 22 DATA_PRECISION=18
			Integer len = row.getInt("NUMERIC_PRECISION","PRECISION","DATA_PRECISION");
			if(null == len){
				len = row.getInt("CHARACTER_MAXIMUM_LENGTH","MAX_LENGTH","DATA_LENGTH","LENGTH");
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
					DataSet set = select( runtime, random, true, (String) null, null, run);
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


	/**
	 * 查询表结构
	 * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
	 * @param greedy 查所有库
	 * @param table 表
	 * @param primary 是否检测主键
	 * @return Column
	 * @param <T>  Column
	 */
	@Override
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, String random, boolean greedy, Table table , boolean primary){

		LinkedHashMap<String,T> columns = CacheProxy.columns(runtime.getKey(), table);
		if(null != columns && !columns.isEmpty()){
			return columns;
		}
		long fr = System.currentTimeMillis();
		if(null == random) {
			random = random(runtime);
		}
		try {
			if (!greedy) {
				checkSchema(runtime, table);
			}
			String catalog = table.getCatalog();
			String schema = table.getSchema();

			int qty_total = 0;
			int qty_dialect = 0; //优先根据系统表查询
			int qty_metadata = 0; //再根据metadata解析
			int qty_jdbc = 0; //根据驱动内置接口补充

			// 1.优先根据系统表查询
			try {
				List<Run> runs = buildQueryColumnRun(runtime, table, false);
				if (null != runs) {
					int idx = 0;
					for (Run run: runs) {
						DataSet set = select(runtime, random, true, (String) null, null, run);
						columns = columns(runtime, idx, true, table, columns, set);
						idx++;
					}
				}
				if(null != columns) {
					qty_dialect = columns.size();
					qty_total=columns.size();
				}
			} catch (Exception e) {
				if(primary) {
					e.printStackTrace();
				} if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
					log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据系统表查询失败", 33), catalog, schema, table, e.toString());
				}
			}
			// 根据驱动内置接口补充
			// 再根据metadata解析 SELECT * FROM T WHERE 1=0
			if (null == columns || columns.size() == 0) {
				try {
					List<Run> runs = buildQueryColumnRun(runtime, table, true);
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
				if(null != columns) {
					qty_metadata = columns.size() - qty_dialect;
					qty_total = columns.size();
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据驱动内置接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
			}

			// 根据jdbc接口补充

			if (null == columns || columns.size() == 0) {
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

				if(null != columns) {
					qty_total = columns.size();
					qty_jdbc = columns.size() - qty_metadata - qty_dialect;
				}
			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[columns][catalog:{}][schema:{}][table:{}][total:{}][根据metadata解析:{}][根据系统表查询:{}][根据jdbc接口补充:{}][执行耗时:{}ms]", random, catalog, schema, table, qty_total, qty_metadata, qty_dialect, qty_jdbc, System.currentTimeMillis() - fr);
			}
			//检测主键
			if(ConfigTable.IS_METADATA_AUTO_CHECK_COLUMN_PRIMARY) {
				if (null != columns || columns.size() > 0) {
					boolean exists = false;
					for(Column column:columns.values()){
						if(column.isPrimaryKey() != -1){
							exists = true;
							break;
						}
					}
					if(!exists){
						PrimaryKey pk = primary(runtime, random, false, table);
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
		if(null != columns) {
			CacheProxy.columns(runtime.getKey(), table, columns);
		}else{
			columns = new LinkedHashMap<>();
		}
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
		if(BasicUtil.isEmpty(table.getName())){
			return columns;
		}
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


	/**
	 * 索引
	 * @param table 表
	 * @return map
	 */
	@Override
	public PrimaryKey primary(DataRuntime runtime, String random, boolean greedy, Table table){
		PrimaryKey primary = null;
		if(!greedy) {
			checkSchema(runtime, table);
		}
		String tab = table.getName();
		String catalog = table.getCatalog();
		String schema = table.getSchema();
		if(null == random) {
			random = random(runtime);
		}

		try{
			List<Run> runs = buildQueryPrimaryRun(runtime, table);
			if(null != runs){
				int idx = 0;
				for(Run run:runs){
					DataSet set = select(runtime, random, false, (String)null, null, run).toUpperKey();
					primary = primary(runtime, idx, table, set);
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
	public <T extends ForeignKey> LinkedHashMap<String, T> foreigns(DataRuntime runtime, String random,  boolean greedy, Table table){
		LinkedHashMap<String, T> foreigns = new LinkedHashMap<>();
		if(null == random) {
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		try {
			List<Run> runs = buildQueryForeignsRun(runtime, table);
			if(null != runs){
				int idx = 0;
				for(Run run:runs){
					DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
					foreigns = foreigns(runtime, idx,  table, foreigns, set);
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

	public <T extends Index> LinkedHashMap<String, T> indexs(DataRuntime runtime, String random, boolean greedy, Table table, String name){

		LinkedHashMap<String,T> indexs = null;
		if(null == table){
			table = new Table();
		}
		if(null == random) {
			random = random(runtime);
		}
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
				indexs = indexs(runtime, true, indexs, table, false, false);
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
		List<Run> runs = buildQueryIndexRun(runtime, table, name);

		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
				try {
					indexs = indexs(runtime, idx, true, table, indexs, set);
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
				pk = primary(runtime, random, false, table);
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

	public <T extends Trigger> LinkedHashMap<String, T> triggers(DataRuntime runtime, String random, boolean greedy, Table table, List<Trigger.EVENT> events){
		LinkedHashMap<String,T> triggers = new LinkedHashMap<>();
		if(null == table){
			table = new Table();
		}
		if(null == random){
			random = random(runtime);
		}
		if(!greedy) {
			checkSchema(runtime, table);
		}
		List<Run> runs = buildQueryTriggerRun(runtime, table, events);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null,  null, run).toUpperKey();
				try {
					triggers = triggers(runtime, idx, true, table, triggers, set);
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
	@Override
	public <T extends Procedure> LinkedHashMap<String, T> procedures(DataRuntime runtime, String random, boolean greedy, String catalog, String schema, String name){

		LinkedHashMap<String,T> procedures = new LinkedHashMap<>();
		if(null == random){
			random = random(runtime);
		}

		if(null == catalog || null == schema){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog){
				catalog = tmp.getCatalog();
			}
			if(null == schema){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryProcedureRun(runtime, catalog, schema, name);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
				try {
					procedures = procedures(runtime, idx, true, procedures, set);
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

	@Override
	public List<String> ddl(DataRuntime runtime, String random, Procedure procedure){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, procedure);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
					list = ddl(runtime, idx++, procedure, list,  set);
				}
				if(list.size()>0) {
					procedure.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装

			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[procedure ddl][procedure:{}][result:{}][执行耗时:{}ms]", random, procedure.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.info("{}[procedure ddl][{}][procedure:{}][msg:{}]", random, LogUtil.format("查询存储过程的创建DDL失败", 33), procedure.getName(), e.toString());
			}
		}
		return list;
	}
	@Override
	public <T extends Function> LinkedHashMap<String, T> functions(DataRuntime runtime, String random, boolean recover, String catalog, String schema, String name){
		LinkedHashMap<String,T> functions = new LinkedHashMap<>();
 		if(null == random){
			 random = random(runtime);
		}

		if(null == catalog || null == schema){
			Table tmp = new Table();
			checkSchema(runtime, tmp);
			if(null == catalog){
				catalog = tmp.getCatalog();
			}
			if(null == schema){
				schema = tmp.getSchema();
			}
		}
		List<Run> runs = buildQueryFunctionRun(runtime, catalog, schema, name);
		if(null != runs){
			int idx = 0;
			for(Run run:runs){
				DataSet set = select(runtime, random, true, (String)null, null, run).toUpperKey();
				try {
					functions = functions(runtime, idx, true, functions, set);
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

	@Override
	public List<String> ddl(DataRuntime runtime, String random, Function function){
		List<String> list = new ArrayList<>();
		if(null == random) {
			random = random(runtime);
		}
		try {
			long fr = System.currentTimeMillis();
			List<Run> runs = buildQueryDDLRun(runtime, function);
			if (null != runs && runs.size()>0) {
				//直接查询DDL
				int idx = 0;
				for (Run run : runs) {
					//不要传table,这里的table用来查询表结构
					DataSet set = select(runtime, random, true, null, null, run).toUpperKey();
					list = ddl(runtime, idx++, function, list,  set);
				}
				if(list.size()>0) {
					function.setDdls(list);
				}
			}else{
				//数据库不支持的 根据definition拼装

			}
			if (ConfigTable.IS_SHOW_SQL && log.isInfoEnabled()) {
				log.info("{}[function ddl][function:{}][result:{}][执行耗时:{}ms]", random, function.getName(), list.size(), System.currentTimeMillis() - fr);
			}
		}catch (Exception e) {
			if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
				e.printStackTrace();
			} else if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
				log.info("{}[function ddl][{}][function:{}][msg:{}]", random, LogUtil.format("查询函数的创建DDL失败", 33), function.getName(), e.toString());
			}
		}
		return list;
	}
}
