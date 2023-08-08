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


import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.adapter.init.DefaultDriverAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.data.util.ThreadConfig;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.exception.SQLQueryException;
import org.anyline.exception.SQLUpdateException;
import org.anyline.metadata.*;
import org.anyline.metadata.type.ColumnType;
import org.anyline.proxy.InterceptorProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.LogUtil;
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

/////////////////////////////////////////////////
	protected JdbcTemplate jdbc(DataRuntime runtime){
		Object client = runtime.getClient();
		return (JdbcTemplate) client;
	}
	@Override
	public long total(DataRuntime runtime, String random, Run run) {
		long total = 0;
		DataSet set = select(runtime, random, true, null, run);
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
	}
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

//////////////////////////////////////////////////////////


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
	public <T extends Column> LinkedHashMap<String, T> columns(DataRuntime runtime, boolean create,  Table table, LinkedHashMap<String, T> columns) {
	// 根据系统表查询失败 再根据metadata解析 SELECT * FROM T WHERE 1=0
		JdbcTemplate jdbc = jdbc(runtime);
		String random = random(runtime);
		if (null == columns || columns.size() == 0) {
			try {
				List<Run> runs = buildQueryColumnRun(runtime, table, true);
				if (null != runs) {
					for (Run run : runs) {
						SqlRowSet set = jdbc.queryForRowSet(run.getFinalQuery());
						columns = columns(runtime, true, columns, table, set);
					}
				}
			} catch (Exception e) {
				if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				} else {
					if (ConfigTable.IS_SHOW_SQL && log.isWarnEnabled()) {
						log.warn("{}[columns][{}][catalog:{}][schema:{}][table:{}][msg:{}]", random, LogUtil.format("根据metadata解析失败", 33), table.getCatalog(), table.getSchema(), table.getName(), e.toString());
					}
				}
			}
		}
		// 根据jdbc接口补充

		if (columns.size() == 0) {
			DataSource ds = null;
			Connection con = null;
			try {
				ds = jdbc.getDataSource();
				con = DataSourceUtils.getConnection(ds);
				DatabaseMetaData metadata = null;
				columns = columns(runtime, true, columns, metadata, table, null);
			} catch (Exception e) {
				if (ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
					e.printStackTrace();
				}
			}finally {
				if(!DataSourceUtils.isConnectionTransactional(con, ds)){
					DataSourceUtils.releaseConnection(con, ds);
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
